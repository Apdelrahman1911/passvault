#!/usr/bin/env ruby
# frozen_string_literal: true

# Non-publishing boundary test: real Git ONLY in a newly created synthetic graph,
# fake gh with an exact finite request queue, scrubbed HOME/config/credentials.
# No signed certificates, network, store API or existing repository is modified.
require "digest"
require "fileutils"
require "json"
require "open3"
require "pathname"
require "rbconfig"
require "tmpdir"
require "yaml"
require_relative "verify-candidate-attestation"

ROOT = Pathname.new(__dir__).parent
RUBY = RbConfig.ruby
GIT = "/usr/bin/git"
REPOSITORY = "fixture-owner/fixture-repo"
WORKFLOW = "#{REPOSITORY}/.github/workflows/testing-release.yml"
CASES = []
FAILURES = []

# This dedicated regression process has the same sole-reaper/default-SIGCHLD
# contract as the helper CLI. No installed application or other process changes.
Signal.trap("CHLD", "DEFAULT")

def assert(value, message = "assertion failed")
  raise message unless value
end

def check(name)
  yield
  CASES << name
  puts "PASS #{name}"
rescue StandardError => error
  FAILURES << name
  warn "FAIL #{name}: #{error.message}"
end

def command(environment, directory, *argv, input: "")
  Open3.capture3(environment, *argv.map(&:to_s), chdir: directory.to_s,
    stdin_data: input, unsetenv_others: true)
end

def git!(environment, directory, *argv, input: "")
  output, diagnostic, status = command(environment, directory, GIT, *argv, input: input)
  assert(status.success?, "Synthetic Git command failed: #{argv.first}: #{diagnostic}")
  output.strip
end

def verified_row(digest)
  { "verificationResult" => { "signature" => { "certificate" => { "sourceRepositoryDigest" => digest } } } }
end

# Observe real calls only while a finite capture fixture is running. This never
# substitutes a process/provider result or signals an additional PID. Recording
# attempts (including ESRCH) prevents a successful reap from hiding a later kill.
module CaptureProcessEvents
  def spawn(*arguments, **options)
    pid = super
    Thread.current[:passvault_capture_events]&.push(["spawn", pid])
    pid
  end

  def kill(signal, *pids)
    pids.each { |pid| Thread.current[:passvault_capture_events]&.push([signal, pid]) }
    super
  end

  def waitpid2(pid, flags = 0)
    result = super
    Thread.current[:passvault_capture_events]&.push(["reap", result.first]) if result
    result
  end

  def waitpid(pid, flags = 0)
    result = super
    Thread.current[:passvault_capture_events]&.push(["reap", result]) if result
    result
  end
end
Process.singleton_class.prepend(CaptureProcessEvents)

# Failure-only passive formatting. Never observe a process or serialize outputs,
# exception messages, paths, backtraces or unrestricted object representations.
module CaptureFailureDiagnostic
  SCHEMA = "capture-sequence-diagnostic-v1"
  UNAVAILABLE = '{"schema":"capture-sequence-diagnostic-v1","format_error":"unavailable"}'

  def self.integer?(value, bits)
    value.is_a?(Integer) && value >= -(2**(bits - 1)) && value < 2**(bits - 1)
  end

  def self.text(value, limit, pattern, fallback = "redacted")
    value.is_a?(String) && value.bytesize <= limit && value.ascii_only? && pattern.match?(value) ? value : fallback
  end

  def self.class_label(value)
    text(value.class.name, 96, /\A[A-Za-z_][A-Za-z0-9_]*(?:::[A-Za-z_][A-Za-z0-9_]*)*\z/, "unclassified")
  end

  def self.event_value(value, tag: false)
    result = { "type" => class_label(value) }
    if value.nil? || integer?(value, 64) || (tag && value.is_a?(String) && %w[spawn reap TERM KILL].include?(value))
      result["value"] = value
    else
      result["redacted"] = true
    end
    result
  end

  def self.format(events, pending)
    prefix = events.first(16).map do |event|
      row = { "type" => class_label(event) }
      if event.is_a?(Array)
        row["arity"] = event.length
        if event.length == 2
          row["tag"] = event_value(event[0], tag: true)
          row["target"] = event_value(event[1])
        end
      end
      row
    end
    chain = []
    seen = []
    current = pending
    cycle = false
    4.times do
      break if current.nil?
      if seen.any? { |item| item.equal?(current) }
        cycle = true
        break
      end
      seen << current
      row = { "class" => class_label(current) }
      if current.is_a?(SystemCallError)
        number = current.errno
        row["errno"] = integer?(number, 32) ? number : nil
        row["errno_redacted"] = true unless integer?(number, 32)
      end
      chain << row
      current = current.cause
    end
    cycle ||= !current.nil? && seen.any? { |item| item.equal?(current) }
    revision = defined?(RUBY_REVISION) ? RUBY_REVISION : nil
    runtime = {
      "engine" => text(RUBY_ENGINE, 16, /\A[A-Za-z0-9_-]+\z/),
      "version" => text(RUBY_VERSION, 24, /\A[0-9.]+\z/),
      "patchlevel" => integer?(RUBY_PATCHLEVEL, 32) ? RUBY_PATCHLEVEL : "redacted",
      "revision" => integer?(revision, 64) ? revision : text(revision, 64, /\A[0-9a-fA-F]+\z/),
      "platform" => defined?(RUBY_PLATFORM) ? text(RUBY_PLATFORM, 64, /\A[A-Za-z0-9._-]+\z/) : "redacted",
    }
    encoded = JSON.generate(
      "schema" => SCHEMA, "event_count" => events.length,
      "event_prefix" => prefix, "events_truncated" => events.length > 16,
      "pending_exception_chain" => chain, "cause_cycle" => cycle,
      "cause_chain_truncated" => !current.nil? && !cycle, "ruby" => runtime,
    )
    encoded.bytesize <= 8192 ? encoded : UNAVAILABLE
  rescue StandardError
    UNAVAILABLE
  end
end


def checked_capture(environment, *arguments, **options)
  events = []
  Thread.current[:passvault_capture_events] = events
  PassVault::CandidateAttestation.capture(environment, *arguments, **options)
ensure
  Thread.current[:passvault_capture_events] = nil
  pending_at_capture_exit = $!
  pid = events.first&.last
  assert(pid.is_a?(Integer) && pid.positive?, "No synthetic child ownership event")
  normal = [["spawn", pid], ["reap", pid]]
  terminated = [["spawn", pid], ["TERM", -pid], ["KILL", -pid], ["reap", pid]]
  message = "Child signals/reap did not preserve the owned PID anchor"
  unless [normal, terminated].include?(events)
    message += " | capture_diagnostic_v1=" + CaptureFailureDiagnostic.format(events, pending_at_capture_exit)
  end
  assert([normal, terminated].include?(events), message)
end

def verify(environment, directory, subject, commit, tree, output:, exit_code: 0, policy: {}, invoke_gh: true)
  options = { "--repo" => REPOSITORY, "--signer-workflow" => WORKFLOW,
    "--source-ref" => "refs/heads/testing", "--candidate-commit" => commit, "--candidate-tree" => tree }.merge(policy)
  request = {
    "argv" => ["gh", "attestation", "verify", subject.to_s,
      "--repo", options.fetch("--repo"), "--signer-workflow", options.fetch("--signer-workflow"),
      "--source-ref", options.fetch("--source-ref"), "--deny-self-hosted-runners",
      "--cert-oidc-issuer", "https://token.actions.githubusercontent.com",
      "--predicate-type", "https://slsa.dev/provenance/v1", "--format", "json"],
    "stdout" => output, "stderr" => "inert diagnostic, deliberately not JSON\n", "exit" => exit_code,
  }
  state_path = environment.fetch("PASSVAULT_FAKE_STATE")
  File.write(state_path, JSON.generate("requests" => invoke_gh ? [request] : [], "calls" => []))
  before = Digest::SHA256.file(subject).hexdigest
  result = command(environment, directory, RUBY, ROOT.join("scripts/verify-candidate-attestation.rb"),
    subject, *options.to_a.flatten, "--deny-self-hosted-runners")
  state = JSON.parse(File.read(state_path))
  assert(!state["violation"] && state.fetch("requests").empty?, "Unexpected or missing gh call")
  assert(state.fetch("calls").length == (invoke_gh ? 1 : 0), "Unexpected verification count")
  assert(Digest::SHA256.file(subject).hexdigest == before, "Attestation verification modified its subject")
  result
end

Dir.mktmpdir("passvault-candidate-attestation.") do |path|
  root = Pathname.new(path).realpath
  %w[bin home tmp empty-template graph].each { |name| root.join(name).mkdir }
  # Unlike the release-provider fixture, ONLY git is real here. GH cannot forward.
  # Keep even interrupted fixture cleanup free of links into shared toolchains.
  root.join("bin/git").write("#!/bin/sh\nexec #{GIT} \"$@\"\n")
  root.join("bin/git").chmod(0o700)
  shim = "#!#{RUBY}\nload #{ROOT.join('scripts/testdata/release-regressions/fake-provider.rb').to_s.inspect}\n"
  %w[gh curl wget ssh security xcodebuild xcrun codesign fastlane gradle java osascript].each do |name|
    root.join("bin", name).write(shim)
    root.join("bin", name).chmod(0o700)
  end
  environment = {
    "PATH" => [root.join("bin"), File.dirname(RUBY), "/usr/bin", "/bin"].join(File::PATH_SEPARATOR),
    "HOME" => root.join("home").to_s, "TMPDIR" => root.join("tmp").to_s,
    "LANG" => "C", "LC_ALL" => "C", "GIT_CONFIG_NOSYSTEM" => "1", "GIT_CONFIG_GLOBAL" => File::NULL,
    "GIT_TERMINAL_PROMPT" => "0", "GIT_ALLOW_PROTOCOL" => "file", "GIT_NO_REPLACE_OBJECTS" => "1",
    "GIT_AUTHOR_NAME" => "Synthetic Fixture", "GIT_AUTHOR_EMAIL" => "fixture@example.invalid",
    "GIT_COMMITTER_NAME" => "Synthetic Fixture", "GIT_COMMITTER_EMAIL" => "fixture@example.invalid",
    "GIT_AUTHOR_DATE" => "2000-01-01T00:00:00Z", "GIT_COMMITTER_DATE" => "2000-01-01T00:00:00Z",
    "PASSVAULT_FIXTURE_ROOT" => root.to_s, "PASSVAULT_FAKE_STATE" => root.join("provider.json").to_s,
    "GH_TOKEN" => "inert-fixture-token", "GITHUB_TOKEN" => "inert-fixture-token",
  }
  root.join("provider.json").write(JSON.generate("requests" => [], "calls" => []))
  graph = root.join("graph")
  git!(environment, root, "init", "--bare", "--initial-branch=testing", "--template=#{root.join('empty-template')}", graph)
  blob = git!(environment, graph, "hash-object", "-w", "--stdin", input: "inert source\n")
  tree = git!(environment, graph, "mktree", input: "100644 blob #{blob}\tfixture.txt\n")
  other_blob = git!(environment, graph, "hash-object", "-w", "--stdin", input: "changed source\n")
  other_tree = git!(environment, graph, "mktree", input: "100644 blob #{other_blob}\tfixture.txt\n")
  parent = git!(environment, graph, "commit-tree", tree, input: "synthetic parent\n")
  candidate = git!(environment, graph, "commit-tree", tree, "-p", parent, input: "synthetic candidate C\n")
  descendant = git!(environment, graph, "commit-tree", tree, "-p", candidate, input: "synthetic invocation D\n")
  changed = git!(environment, graph, "commit-tree", other_tree, "-p", candidate, input: "changed invocation\n")
  unrelated = git!(environment, graph, "commit-tree", tree, input: "unrelated same tree\n")
  tag = git!(environment, graph, "mktag", input: "object #{candidate}\ntype commit\ntag fixture\ntagger Synthetic <fixture@example.invalid> 946684800 +0000\n\nsynthetic tag\n")
  git!(environment, graph, "update-ref", "refs/heads/testing", descendant)
  subject = root.join("candidate.json")
  subject.write(JSON.generate("sourceCommit" => candidate, "sourceTree" => tree, "inert" => true))
  bounded_child = root.join("bounded-child.rb")
  bounded_child.write(<<~'RUBY')
    File.write(ARGV.fetch(1), Process.pid.to_s)
    case ARGV.fetch(0)
    when "valid"
      STDOUT.write("ok"); STDERR.write("inert diagnostic")
    when "stdout"
      STDOUT.write("x" * 256)
    when "stderr"
      STDERR.write("x" * 256)
    when "combined"
      STDOUT.write("x" * 64); STDERR.write("x" * 65)
    when "wait"
      sleep 3
    else
      abort("Unexpected inert child mode")
    end
  RUBY
  assert_reaped = lambda do |pid_file|
    pid = Integer(pid_file.read)
    begin
      Process.kill(0, pid) # Liveness query only, never a name-based process kill.
      raise "Synthetic child was not reaped"
    rescue Errno::ESRCH
      true
    end
  end
  check("PVA-029 bounded capture separates stdout and reaps a valid child") do
    pid_file = root.join("valid.pid")
    output, status = checked_capture(environment, RUBY, bounded_child.to_s,
      "valid", pid_file.to_s, timeout_seconds: 2, output_limit: 128, inherit_environment: false)
    assert(status.success? && output == "ok")
    assert_reaped.call(pid_file)
  end
  %w[stdout stderr combined].each do |mode|
    check("PVA-029 bounded capture rejects finite #{mode} overflow and reaps child") do
      pid_file = root.join("#{mode}.pid")
      error = nil
      begin
        checked_capture(environment, RUBY, bounded_child.to_s,
          mode, pid_file.to_s, timeout_seconds: 2, output_limit: 128, inherit_environment: false)
      rescue PassVault::CandidateAttestation::Invalid => caught
        error = caught
      end
      assert(error&.message == "Verifier subprocess exceeded its output limit")
      assert_reaped.call(pid_file)
    end
  end
  check("PVA-029 bounded capture times out and reaps its synthetic sleeper") do
    pid_file = root.join("timeout.pid")
    error = nil
    begin
      checked_capture(environment, RUBY, bounded_child.to_s,
        "wait", pid_file.to_s, timeout_seconds: 1, output_limit: 128, inherit_environment: false)
    rescue PassVault::CandidateAttestation::Invalid => caught
      error = caught
    end
    assert(error&.message == "Verifier subprocess exceeded its time limit")
    assert_reaped.call(pid_file)
  end
  check("PVA-029 cancellation reaps its synthetic child and joins the trigger thread") do
    pid_file = root.join("cancel.pid")
    caller = Thread.current
    trigger = Thread.new do
      deadline = Process.clock_gettime(Process::CLOCK_MONOTONIC) + 2
      ready = -> { pid_file.exist? && pid_file.read.match?(/\A[1-9][0-9]*\z/) }
      until ready.call || Process.clock_gettime(Process::CLOCK_MONOTONIC) >= deadline
        sleep 0.02
      end
      caller.raise(Interrupt, "inert cancellation") if ready.call
    end
    interrupted = false
    begin
      checked_capture(environment, RUBY, bounded_child.to_s,
        "wait", pid_file.to_s, timeout_seconds: 4, output_limit: 128, inherit_environment: false)
    rescue Interrupt => error
      interrupted = error.message == "inert cancellation"
    ensure
      trigger.kill unless trigger.join(2)
      trigger.join
    end
    assert(interrupted)
    assert_reaped.call(pid_file)
  end

  { "exact C" => [candidate, true], "equal-tree descendant D" => [descendant, true],
    "uppercase certificate digest" => [descendant.upcase, true], "changed tree descendant" => [changed, false],
    "unrelated identical tree" => [unrelated, false], "reverse ancestry" => [parent, false],
    "unavailable object" => ["f" * 40, false], "annotated tag object" => [tag, false] }.each do |name, (digest, expected)|
    check("PVA-029 production helper / real Git: #{name}") do
      result = verify(environment, graph, subject, candidate, tree, output: JSON.generate([verified_row(digest)]))
      assert(result.last.success? == expected, result[1])
    end
  end

  { "missing identity" => {}, "null identity" => verified_row(nil), "numeric identity" => verified_row(1),
    "blank identity" => verified_row(""), "short identity" => verified_row(candidate[0, 7]),
    "whitespace identity" => verified_row(" #{candidate}"),
    "predicate identity only" => { "verificationResult" => { "statement" => { "predicate" => { "sourceCommit" => candidate } } } },
    "raw bundle identity only" => { "attestation" => verified_row(candidate) },
    "wrong certificate nesting" => { "verificationResult" => { "signature" => { "certificate" => { "extensions" => { "sourceRepositoryDigest" => candidate } } } } },
    "wrong verified row type" => { "verificationResult" => [verified_row(candidate)] } }.each do |name, row|
    check("PVA-029 rejects #{name}") do
      assert(!verify(environment, graph, subject, candidate, tree, output: JSON.generate([row])).last.success?)
    end
  end

  { "empty verified results" => "[]", "object instead of array" => JSON.generate(verified_row(candidate)),
    "malformed JSON" => "[{", "diagnostics mixed into stdout" => "notice\n#{JSON.generate([verified_row(candidate)])}",
    "duplicate identity keys" => JSON.generate([verified_row(candidate)]).sub(
      '"sourceRepositoryDigest":', '"sourceRepositoryDigest":"' + descendant + '","sourceRepositoryDigest":') }.each do |name, output|
    check("PVA-029 rejects #{name}") do
      assert(!verify(environment, graph, subject, candidate, tree, output: output).last.success?)
    end
  end

  check("PVA-029 gh failure cannot be rescued by apparently valid JSON") do
    result = verify(environment, graph, subject, candidate, tree,
      output: JSON.generate([verified_row(candidate)]), exit_code: 1)
    assert(!result.last.success? && result[1].include?("signature or signer policy"))
  end
  check("PVA-029 rejects too many otherwise well-shaped verified rows") do
    rows = Array.new(PassVault::CandidateAttestation::MAX_VERIFIED_ROWS + 1) { verified_row(candidate) }
    result = verify(environment, graph, subject, candidate, tree, output: JSON.generate(rows))
    assert(!result.last.success? && result[1].include?("Too many verified attestation results"))
  end
  check("PVA-029 accepts one complete qualifying verified row, not every row") do
    rows = [verified_row(unrelated), nil, verified_row(changed), verified_row(descendant)]
    assert(verify(environment, graph, subject, candidate, tree, output: JSON.generate(rows)).last.success?)
  end
  check("PVA-029 never mixes identity fields from different verified rows") do
    rows = [{ "verificationResult" => { "signature" => {} } }, { "signature" => verified_row(candidate) }, verified_row(changed)]
    assert(!verify(environment, graph, subject, candidate, tree, output: JSON.generate(rows)).last.success?)
  end
  check("PVA-029 exact C still requires recorded tree to match the real commit") do
    assert(!verify(environment, graph, subject, candidate, other_tree, output: JSON.generate([verified_row(candidate)])).last.success?)
  end
  check("PVA-029 missing candidate cannot pass an equal string digest") do
    missing = "e" * 40
    assert(!verify(environment, graph, subject, missing, tree, output: JSON.generate([verified_row(missing)])).last.success?)
  end
  { "foreign repository/workflow pair" => { "--repo" => "other/repo" },
    "different workflow" => { "--signer-workflow" => "#{REPOSITORY}/.github/workflows/untrusted.yml" },
    "wrong source ref" => { "--source-ref" => "refs/heads/main" },
    "non-SHA candidate" => { "--candidate-commit" => "main" },
    "invalid tree" => { "--candidate-tree" => "--help" } }.each do |name, policy|
    check("PVA-029 rejects policy input before gh: #{name}") do
      assert(!verify(environment, graph, subject, candidate, tree, output: "", policy: policy, invoke_gh: false).last.success?)
    end
  end
  check("PVA-029 readiness signer uses the same retained policy and source proof") do
    policy = { "--signer-workflow" => "#{REPOSITORY}/.github/workflows/candidate-readiness.yml" }
    assert(verify(environment, graph, subject, candidate, tree,
      output: JSON.generate([verified_row(descendant)]), policy: policy).last.success?)
  end

  check("PVA-028 real Git reproduces depth-one failure and full-history success") do
    shallow = root.join("shallow")
    full = root.join("full")
    # file:// forces Git transport depth semantics, not a local hardlinked copy.
    [shallow, full].each do |destination|
      depth = destination == shallow ? ["--depth=1"] : []
      git!(environment, root, "clone", "--no-checkout", "--no-tags", "--template=#{root.join('empty-template')}",
        *depth, "file://#{graph}", destination)
    end
    assert(git!(environment, shallow, "rev-parse", "HEAD") == descendant)
    assert(git!(environment, shallow, "rev-parse", "HEAD^{tree}") == tree)
    assert(!command(environment, shallow, GIT, "rev-parse", "--verify", "#{candidate}^{tree}").last.success?)
    assert(!command(environment, shallow, GIT, "merge-base", "--is-ancestor", candidate, descendant).last.success?)
    assert(git!(environment, full, "rev-parse", "#{candidate}^{tree}") == tree)
    assert(command(environment, full, GIT, "merge-base", "--is-ancestor", candidate, descendant).last.success?)
    assert(verify(environment, full, subject, candidate, tree, output: JSON.generate([verified_row(descendant)])).last.success?)
    assert(!verify(environment, shallow, subject, candidate, tree, output: JSON.generate([verified_row(descendant)])).last.success?)
    assert(verify(environment, shallow, subject, descendant, tree, output: JSON.generate([verified_row(descendant)])).last.success?)
  end
end

helper_jobs = {
  "candidate-readiness.yml" => { "candidate" => 2, "promote-release-branch" => 2 },
  "production-release.yml" => { "candidate" => 2 }, "mobile-store-release.yml" => { "validate" => 2 },
  "production-signing-validation.yml" => { "candidate" => 2 }, "publish-stable-release.yml" => { "candidate" => 2 },
  "release.yml" => { "prepare" => 3, "assemble-release" => 2 },
}
helper_jobs.each do |file, jobs|
  check("PVA-029 all intended consumers and their own history: #{file}") do
    document = YAML.safe_load(ROOT.join(".github/workflows", file).read, aliases: false)
    jobs.each do |job_name, count|
      job = document.fetch("jobs").fetch(job_name)
      steps = job.fetch("steps")
      checkouts = steps.select { |entry| entry["uses"].to_s.start_with?("actions/checkout@") }
      assert(checkouts.one? && checkouts.first.dig("with", "fetch-depth") == 0)
      body = steps.map { |entry| entry["run"].to_s }.join("\n")
      assert(body.scan("ruby scripts/verify-candidate-attestation.rb ").length == count)
      assert(body.scan("--candidate-commit ").length == count && body.scan("--candidate-tree ").length == count)
      assert(body.scan("--source-ref refs/heads/testing").length == count)
      assert(body.scan("--deny-self-hosted-runners").length == count)
      assert(!body.include?("gh attestation verify"), "Testing-source consumer retained exact-invocation-only verification")
    end
  end
end
check("PVA-029 exact original-upload and final Desktop verification boundaries remain strict") do
  resume = ROOT.join("scripts/resume-testing-candidate-receipts.rb").read
  assert(resume.include?('"--source-digest", original.fetch("sourceCommit")'))
  stable = YAML.safe_load(ROOT.join(".github/workflows/publish-stable-release.yml").read, aliases: false)
  raw = stable.fetch("jobs").values.flat_map { |job| Array(job["steps"]).map { |entry| entry["run"].to_s } }.join("\n")
  assert(raw.include?('--source-digest "$GITHUB_SHA"'))
  assert(raw.include?("--source-ref refs/heads/release"))
  assert(raw.include?("$GITHUB_REPOSITORY/.github/workflows/production-signing-validation.yml"))
end

puts "#{CASES.length} passed; #{FAILURES.length} failed; synthetic Git and mocked gh only"
exit(FAILURES.empty? ? 0 : 1)
