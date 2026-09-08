#!/usr/bin/env ruby
# frozen_string_literal: true

# Nonpublishing production-boundary fixtures. Only fake gh/git/security are
# reachable, with scrubbed credentials/HOME and exact, fail-closed call queues.
require "digest"
require "fileutils"
require "json"
require "open3"
require "pathname"
require "rbconfig"
require "tmpdir"
require "yaml"
require_relative "lib/strict_json"

ROOT = Pathname.new(__dir__).parent
RUBY = RbConfig.ruby
REPOSITORY = "fixture-owner/fixture-repo"
COMMIT = "a" * 40
TREE = "b" * 40
CURRENT = "c" * 40
VERSION = "1.2.3"
BUILD = "1001001"
FAILURES = []
CASES = []

def assert(value, message = "assertion failed")
  raise message unless value
end

def check(name)
  yield
  CASES << name
  puts "PASS #{name}"
rescue StandardError => error
  FAILURES << "#{name}: #{error.message}"
  warn "FAIL #{FAILURES.last}"
end

def workflow(name)
  YAML.safe_load(ROOT.join(".github/workflows", name).read, aliases: false)
end

def step(document, job, name)
  document.fetch("jobs").fetch(job).fetch("steps").find { |entry| entry["name"] == name } ||
    raise("Missing production step: #{name}")
end

def with_fixture
  Dir.mktmpdir("passvault-release-regressions.") do |path|
    root = Pathname.new(path).realpath
    %w[bin home tmp].each { |name| root.join(name).mkdir }
    state = root.join("provider.json")
    state.write(JSON.generate("requests" => [], "calls" => []))
    shim = "#!#{RUBY}\nload #{ROOT.join('scripts/testdata/release-regressions/fake-provider.rb').to_s.inspect}\n"
    %w[gh git security curl wget xcodebuild xcrun codesign spctl fastlane gradle java keytool osascript].each do |name|
      root.join("bin", name).write(shim)
      root.join("bin", name).chmod(0o700)
    end
    environment = {
      "PATH" => [root.join("bin"), File.dirname(RUBY), ENV.fetch("PATH")].join(File::PATH_SEPARATOR),
      "HOME" => root.join("home").to_s, "TMPDIR" => root.join("tmp").to_s,
      "LANG" => "en_US.UTF-8", "LC_ALL" => "en_US.UTF-8",
      "GH_TOKEN" => "inert-fixture-token", "GITHUB_TOKEN" => "inert-fixture-token",
      "GITHUB_REPOSITORY" => REPOSITORY, "GIT_CONFIG_NOSYSTEM" => "1", "GIT_CONFIG_GLOBAL" => File::NULL,
      "PASSVAULT_FIXTURE_ROOT" => root.to_s, "PASSVAULT_FAKE_STATE" => state.to_s,
    }
    yield root, environment
    result = JSON.parse(state.read)
    assert(!result["violation"], "Provider double rejected an unexpected call")
    remaining = result.fetch("requests")
    assert(remaining.empty?, "Expected provider calls were not made: #{remaining.map { |entry| entry['argv'] }}")
  end
end

def run(environment, *arguments, cwd: ROOT)
  Open3.capture3(environment, *arguments.map(&:to_s), chdir: cwd.to_s, unsetenv_others: true)
end

def cli(environment, script, *arguments)
  run(environment, RUBY, ROOT.join("scripts", script), *arguments)
end

def queue(environment, requests)
  File.write(environment.fetch("PASSVAULT_FAKE_STATE"), JSON.generate("requests" => requests, "calls" => []))
end

def calls(environment)
  JSON.parse(File.read(environment.fetch("PASSVAULT_FAKE_STATE"))).fetch("calls")
end

def request(*argv, stdout: "", exit_code: 0, files: nil)
  { "argv" => argv, "stdout" => stdout, "exit" => exit_code }.tap { |value| value["files"] = files if files }
end

def api(method, path, code, payload = nil, fields: {})
  argv = ["gh", "api", "--include", "--method", method, "repos/#{REPOSITORY}/#{path}"]
  fields.each { |key, value| argv.concat(["--raw-field", "#{key}=#{value}"]) }
  request(*argv, stdout: "HTTP/2.0 #{code} Fixture\r\nContent-Type: application/json\r\n\r\n#{payload && JSON.generate(payload)}",
    exit_code: code.between?(200, 299) ? 0 : 1)
end

def mobile_receipt(platform)
  kinds = platform == "android" ? %w[aab apk r8-mapping] : %w[ipa xcarchive link-map]
  files = kinds.to_h { |kind| ["fixture-#{platform}-#{kind}.bin", "inert #{platform} #{kind}\n"] }
  receipt = {
    "schemaVersion" => 1, "platform" => platform, "marketingVersion" => VERSION,
    "buildNumber" => BUILD.to_i, "sourceCommit" => COMMIT, "sourceTree" => TREE,
    "createdAt" => "2026-09-01T00:00:00Z",
    "identifier" => platform == "android" ? "com.passvault.android" : "com.passvault.ios",
    "signingFingerprint" => platform == "android" ? "D" * 64 : "E" * 40,
    "artifacts" => kinds.map do |kind|
      name = "fixture-#{platform}-#{kind}.bin"
      { "kind" => kind, "fileName" => name, "sizeBytes" => files.fetch(name).bytesize,
        "sha256" => Digest::SHA256.hexdigest(files.fetch(name)) }
    end,
  }
  [receipt, files]
end

def candidate(root, schema: 2)
  manifest = {
    "schemaVersion" => schema, "marketingVersion" => VERSION, "buildNumber" => BUILD.to_i,
    "sourceCommit" => COMMIT, "sourceTree" => TREE, "candidateTag" => "v#{VERSION}-rc.#{BUILD}",
    "createdAt" => "2026-09-01T00:00:00Z",
  }
  %w[android ios].each do |platform|
    receipt, files = mobile_receipt(platform)
    content = JSON.generate(receipt)
    root.join("#{platform}-artifact-receipt.json").write(content)
    files.each { |name, bytes| root.join(name).write(bytes) }
    section = { "artifactReceiptSha256" => Digest::SHA256.hexdigest(content),
      "internal" => "completed", "external" => platform == "android" ? "completed" : "approved" }
    if platform == "android"
      section.merge!("packageName" => receipt.fetch("identifier"), "signingCertificateSha256" => receipt.fetch("signingFingerprint"))
    else
      section.merge!("bundleId" => receipt.fetch("identifier"), "signingIdentitySha1" => receipt.fetch("signingFingerprint"),
        "appStoreAppId" => "1234567890")
    end
    manifest[platform] = section
  end
  if schema == 3
    policies = {
      "linuxDeb" => ["linux", "x64", "package", "PassVault-#{VERSION}.deb"],
      "linuxRpm" => ["linux", "x64", "package", "PassVault-#{VERSION}.rpm"],
      "windowsX64AppImage" => ["windows", "x64", "app-image", "PassVault-#{VERSION}-windows-x64-app-image.zip"],
      "macosArm64AppImage" => ["macos", "arm64", "app-image", "PassVault-#{VERSION}-macos-arm64-app-image.zip"],
      "macosX64AppImage" => ["macos", "x64", "app-image", "PassVault-#{VERSION}-macos-x64-app-image.zip"],
    }
    desktop = {
      "schemaVersion" => 1, "marketingVersion" => VERSION, "buildNumber" => BUILD.to_i,
      "candidateTag" => manifest.fetch("candidateTag"), "sourceCommit" => COMMIT, "sourceTree" => TREE,
      "createdAt" => "2026-09-01T00:00:00Z",
      "sourceArtifactRun" => { "repository" => REPOSITORY, "workflow" => ".github/workflows/testing-release.yml",
        "runId" => 1, "runAttempt" => 1 },
      "artifacts" => policies.to_h do |key, (platform, architecture, role, name)|
        bytes = "inert artifact #{key}\n"
        root.join(name).write(bytes)
        [key, { "platform" => platform, "architecture" => architecture, "role" => role,
          "fileName" => name, "sizeBytes" => bytes.bytesize, "sha256" => Digest::SHA256.hexdigest(bytes) }]
      end,
    }
    raw = JSON.generate(desktop)
    root.join("desktop-artifact-receipt.json").write(raw)
    manifest["desktop"] = { "artifactReceiptSha256" => Digest::SHA256.hexdigest(raw) }
  end
  root.join("manifest.json").write(JSON.generate(manifest))
  manifest
end

strict_json_cases = {
  "valid" => {
    "empty_object" => '{}',
    "empty_array" => '[]',
    "scalar_null" => 'null',
    "scalar_false" => 'false',
    "scalar_number" => '12.5',
    "scalar_string" => '"synthetic"',
    "all_value_types" => '{"none":null,"no":false,"yes":true,"number":3,"array":[1,"text"],"object":{}}',
    "sibling_keys" => '{"left":{"name":1},"right":{"name":2}}',
    "array_sibling_keys" => '[{"name":1},{"name":2}]',
    "escaped_distinct_keys" => '{"\\u0061":1,"a\\u0062":2}',
    "member_text_in_string" => '{"text":"{\\"name\\":1,\\"name\\":2}","name":3}',
  },
  "duplicate" => {
    "top_level" => '{"name":1,"name":2}',
    "same_value" => '{"name":1,"name":1}',
    "after_null" => '{"name":null,"name":1}',
    "after_false" => '{"name":false,"name":1}',
    "nested_object" => '{"outer":{"name":1,"name":2}}',
    "inside_array" => '[{"name":1,"name":2}]',
    "escaped_top_level" => '{"name":1,"\\u006eame":2}',
    "escaped_nested" => '{"outer":[{"\\u006eame":1,"name":2}]}',
    "unicode_key" => '{"é":1,"\\u00e9":2}',
    "supplementary_key" => '{"🔐":1,"\\ud83d\\udd10":2}',
    "empty_key" => '{"":1,"":2}',
    "escaped_quote_key" => '{"a\\"b":1,"\\u0061\\u0022b":2}',
  },
}

# Run the same contract through the selected JSON gem and the bundled default
# parser without gems. This installs/upgrades nothing and catches both modern
# post-dedup object_class conversion and the legacy per-member []= callback.
strict_json_contract = <<~'RUBY'
  require ARGV.fetch(0)
  cases = JSON.parse(ARGV.fetch(1))
  puts "Strict JSON runtime: Ruby #{RUBY_VERSION}; JSON #{JSON::VERSION}"
  cases.fetch("valid").each do |name, source|
    expected = JSON.parse(source)
    actual = PassVault::StrictJson.parse(source)
    raise "Valid JSON changed: #{name}" unless actual == expected
  end
  cases.fetch("duplicate").each do |name, source|
    rejected = false
    begin
      PassVault::StrictJson.parse(source)
    rescue JSON::ParserError
      rejected = true
    end
    raise "Duplicate JSON accepted: #{name}" unless rejected
  end
  puts "Strict JSON contract: #{cases.fetch('valid').length} valid and #{cases.fetch('duplicate').length} duplicate controls"
RUBY

{ "selected" => [], "bundled_default" => ["--disable-gems"] }.each do |mode, flags|
  check("PVA-015 strict JSON #{mode} decoded-key contract") do
    with_fixture do |_root, environment|
      stdout, stderr, status = run(environment, RUBY, *flags, "-e", strict_json_contract,
        ROOT.join("scripts/lib/strict_json.rb"), JSON.generate(strict_json_cases))
      assert(status.success?, "Strict JSON #{mode} contract failed: #{stdout}\n#{stderr}")
      puts stdout
      assert(calls(environment).empty?)
    end
  end
end

check("PVA-015 strict JSON fails closed if a backend ignores both duplicate controls") do
  with_fixture do |_root, environment|
    source = <<~'RUBY'
      require ARGV.fetch(0)
      def JSON.parse(_source, **_options)
        {} # Deliberately broken, non-forwarding parser double in this child only.
      end
      begin
        PassVault::StrictJson.parse('{"valid":1}')
      rescue JSON::ParserError => error
        abort("Wrong enforcement failure") unless error.message == "JSON parser cannot enforce unique object fields"
        exit 0
      end
      abort("Unenforced duplicate-key policy was accepted")
    RUBY
    stdout, stderr, status = run(environment, RUBY, "-e", source, ROOT.join("scripts/lib/strict_json.rb"))
    assert(status.success?, "Unsupported parser did not fail closed: #{stdout}\n#{stderr}")
    assert(calls(environment).empty?)
  end
end

check("PVA-015 retained public schema2 receipts, immutable bytes and strict scope") do
  with_fixture do |_root, environment|
    public_root = ROOT.join("scripts/testdata/release-regressions/schema2-1017001")
    identities = JSON.parse(public_root.join("public-fixture-provenance.json").read)
    identities.fetch("files").each do |name, digest|
      assert(Digest::SHA256.file(public_root.join(name)).hexdigest == digest, "Retained public metadata changed")
    end
    manifest = public_root.join("readiness-manifest.json")
    document = JSON.parse(manifest.read)
    arguments = [manifest, document.fetch("sourceCommit"), document.fetch("sourceTree")]
    assert(cli(environment, "validate-candidate-manifest.rb", "--mobile-only", *arguments).last.success?)
    assert(!cli(environment, "validate-candidate-manifest.rb", *arguments).last.success?, "Desktop scope accepted schema2")
    assert(cli(environment, "validate-candidate-artifact-provenance.rb", "--mobile-only", manifest,
      public_root, *arguments.drop(1)).last.success?)
    pending = public_root.join("candidate-manifest.json")
    assert(!cli(environment, "validate-candidate-manifest.rb", "--mobile-only", pending, *arguments.drop(1)).last.success?)
    assert(cli(environment, "validate-candidate-manifest.rb", "--mobile-only", "--allow-pending", pending,
      *arguments.drop(1)).last.success?)
    assert(calls(environment).empty?)
  end
end

[2, 3].each do |schema|
  check("PVA-015 synthetic schema#{schema} valid receipt/source/artifact binding") do
    with_fixture do |root, environment|
      candidate(root, schema: schema)
      manifest = root.join("manifest.json")
      flags = schema == 2 ? ["--mobile-only"] : []
      assert(cli(environment, "validate-candidate-manifest.rb", *flags, manifest, COMMIT, TREE).last.success?)
      assert(cli(environment, "validate-candidate-artifact-provenance.rb", *flags, manifest, root, COMMIT, TREE).last.success?)
      %w[android ios].each do |platform|
        receipt = root.join("#{platform}-artifact-receipt.json")
        assert(cli(environment, "validate-mobile-artifact-receipt.rb", receipt, platform, VERSION, BUILD, COMMIT, TREE, root).last.success?)
      end
      if schema == 3
        root.join("desktop-artifact-receipt.json").unlink
        assert(!cli(environment, "validate-candidate-artifact-provenance.rb", manifest, root, COMMIT, TREE).last.success?)
        assert(cli(environment, "validate-candidate-artifact-provenance.rb", "--mobile-only", manifest, root, COMMIT, TREE).last.success?)
      end
    end
  end
end

%w[wrong_commit wrong_tree wrong_build pending malformed unknown_field receipt_digest receipt_tree receipt_version receipt_signer artifact_size artifact_hash duplicate_json].each do |fault|
  check("PVA-015 rejects #{fault}") do
    with_fixture do |root, environment|
      manifest = candidate(root)
      path = root.join("manifest.json")
      receipt_path = root.join("android-artifact-receipt.json")
      receipt = JSON.parse(receipt_path.read)
      case fault
      when "wrong_commit" then manifest["sourceCommit"] = "f" * 40
      when "wrong_tree" then manifest["sourceTree"] = "f" * 40
      when "wrong_build" then manifest["buildNumber"] += 1
      when "pending" then manifest["ios"]["external"] = "submitted_for_review"
      when "unknown_field" then manifest["desktop"] = { "artifactReceiptSha256" => "0" * 64 }
      when "receipt_digest" then manifest["android"]["artifactReceiptSha256"] = "0" * 64
      when "receipt_tree" then receipt["sourceTree"] = "f" * 40
      when "receipt_version" then receipt["marketingVersion"] = "9.9.9"
      when "receipt_signer" then receipt["signingFingerprint"] = "F" * 64
      when "artifact_size" then receipt["artifacts"][0]["sizeBytes"] += 1
      when "artifact_hash" then receipt["artifacts"][0]["sha256"] = "0" * 64
      end
      if fault.start_with?("receipt_") && fault != "receipt_digest" || fault.start_with?("artifact_")
        receipt_path.write(JSON.generate(receipt))
        manifest["android"]["artifactReceiptSha256"] = Digest::SHA256.file(receipt_path).hexdigest
      end
      path.write(JSON.generate(manifest))
      path.write("{") if fault == "malformed"
      if fault == "duplicate_json"
        original = path.read
        mutated = original.sub('"schemaVersion":2', '"schemaVersion":3,"schemaVersion":2')
        assert(mutated != original && mutated.scan('"schemaVersion":').length == 2,
          "Duplicate-key fixture failed to add a second member")
        path.write(mutated)
      end
      result = if fault.start_with?("artifact_")
        cli(environment, "validate-mobile-artifact-receipt.rb", receipt_path, "android", VERSION, BUILD, COMMIT, TREE, root)
      elsif fault == "pending"
        cli(environment, "validate-candidate-manifest.rb", "--mobile-only", path, COMMIT, TREE)
      else
        cli(environment, "validate-candidate-artifact-provenance.rb", "--mobile-only", path, root, COMMIT, TREE)
      end
      assert(!result.last.success?, "Invalid candidate accepted: #{fault}")
    end
  end
end

mobile_workflow = workflow("mobile-store-release.yml")
check("PVA-015 mobile scope never requires Desktop and protected Desktop scope remains strict") do
  %w[production-release.yml mobile-store-release.yml].each do |name|
    runs = workflow(name).fetch("jobs").values.flat_map { |job| Array(job["steps"]).map { |entry| entry["run"].to_s } }.join("\n")
    assert(runs.include?("validate-candidate-manifest.rb --mobile-only"))
    assert(runs.include?("validate-candidate-artifact-provenance.rb --mobile-only"))
    assert(runs.include?("for platform in android ios; do"))
    assert(!runs.include?("--pattern '*-artifact-receipt.json'"))
    assert(runs.include?("--candidate-commit") && runs.include?("ruby scripts/verify-candidate-attestation.rb"))
  end
  %w[release.yml production-signing-validation.yml publish-stable-release.yml].each do |name|
    assert(!ROOT.join(".github/workflows", name).read.include?("--mobile-only"))
  end
end

check("PVA-016 workflow-derived shell values stay in env, with approval and lock gates") do
  mobile_workflow.fetch("jobs").each_value do |job|
    Array(job["steps"]).each do |entry|
      assert(!entry["run"].to_s.match?(/\$\{\{\s*(inputs\.|needs\.validate\.|steps\.version\.)/), "Workflow data was inserted into shell source")
    end
  end
  assert(mobile_workflow.fetch("concurrency").fetch("cancel-in-progress") == false)
  assert(mobile_workflow.fetch("jobs").fetch("promote-ios").fetch("environment") == "${{ needs.validate.outputs.environment }}")
  gate = step(mobile_workflow, "validate", "Require the correct protected branch and confirmations").fetch("run")
  assert(gate.include?("I_APPROVE_PRODUCTION") && gate.include?("I_CONFIRM_REQUIRED_TESTING_COMPLETED"))
end

["1.0.7", "", "01.0.7", "1.0", "1.0.7\nforged=value", '$(printf interpreted)', '1.0.7"; false; #'].each do |version|
  check("PVA-016 real version run block #{version.inspect}") do
    with_fixture do |root, environment|
      output = root.join("output")
      root.join("version.properties").write("VERSION_NAME=1.2.3\nVERSION_CODE=1001001\n")
      script = step(mobile_workflow, "validate", "Validate version and public release metadata").fetch("run")
      environment.merge!("REQUEST_VERSION" => version, "REQUEST_BUILD_NUMBER" => "1017001", "REQUEST_CHANNEL" => "production", "GITHUB_OUTPUT" => output.to_s)
      result = run(environment, "bash", "--noprofile", "--norc", "-euo", "pipefail", "-c", script, cwd: root)
      expected = ["1.0.7", ""].include?(version)
      assert(result.last.success? == expected, result[1])
      assert(expected ? output.read.include?("version=#{version.empty? ? '1.2.3' : version}\n") : !output.exist?)
      assert(calls(environment).empty?)
    end
  end
end

check("PVA-017 workflow publishes only the already verified immutable bundle") do
  publish = workflow("publish-stable-release.yml").fetch("jobs").values.flat_map { |job| Array(job["steps"]) }
    .find { |entry| entry["name"] == "Publish immutable stable GitHub release" }
  assert(publish.fetch("run").include?("scripts/publish-immutable-github-release.rb"))
  assert(!publish.fetch("run").match?(/gradlew|xcodebuild|--clobber|--target/))
end

%w[absent matching annotated mismatched annotated_mismatched concurrent_matching concurrent_mismatched existing_release api_failure].each do |scenario|
  check("PVA-017 real publication CLI #{scenario}") do
    with_fixture do |root, environment|
      assets = root.join("assets"); assets.mkdir; assets.join("fixture.txt").write("frozen inert artifact\n")
      ref = "git/ref/tags/v#{VERSION}"
      object = { "object" => { "type" => "commit", "sha" => COMMIT } }
      wrong = { "object" => { "type" => "commit", "sha" => "f" * 40 } }
      annotated = { "object" => { "type" => "tag", "sha" => "e" * 40 } }
      expected = %w[absent matching annotated concurrent_matching].include?(scenario)
      requests = [api("GET", "releases/tags/v#{VERSION}", scenario == "existing_release" ? 200 : scenario == "api_failure" ? 500 : 404, {})]
      unless %w[existing_release api_failure].include?(scenario)
        requests << api("GET", ref, %w[absent concurrent_matching concurrent_mismatched].include?(scenario) ? 404 : 200,
          scenario.start_with?("annotated") ? annotated : scenario == "mismatched" ? wrong : object)
        if scenario.start_with?("annotated")
          requests << api("GET", "git/tags/#{'e' * 40}", 200, scenario == "annotated_mismatched" ? wrong : object)
        elsif %w[absent concurrent_matching concurrent_mismatched].include?(scenario)
          requests << api("POST", "git/refs", scenario == "absent" ? 201 : 422, {},
            fields: { "ref" => "refs/tags/v#{VERSION}", "sha" => COMMIT })
          requests << api("GET", ref, 200, scenario == "concurrent_mismatched" ? wrong : object)
        end
      end
      if expected
        notes = "Exact production-validated Desktop packages; SHA256SUMS.txt and release-provenance.json " \
          "bind every asset to the tested candidate, Desktop promotion inputs, and mobile receipts. " \
          "No artifact was rebuilt or re-signed during publication."
        requests << request("gh", "release", "create", "v#{VERSION}", assets.join("fixture.txt").to_s,
          "--repo", REPOSITORY, "--verify-tag", "--generate-notes", "--title", "PassVault #{VERSION}", "--notes", notes)
        requests << api("GET", ref, 200, scenario == "annotated" ? annotated : object)
        requests << api("GET", "git/tags/#{'e' * 40}", 200, object) if scenario == "annotated"
      end
      queue(environment, requests)
      result = cli(environment, "publish-immutable-github-release.rb", VERSION, COMMIT, assets)
      assert(result.last.success? == expected, result[1])
      assert(calls(environment).none? { |argv| argv.include?("PATCH") || argv.include?("--force") })
      assert(expected || calls(environment).none? { |argv| argv[0..2] == %w[gh release create] })
    end
  end
end

def resume_requests(multiple_pages: false, changed_tree: false, fault: nil)
  run = { "id" => 1, "path" => ".github/workflows/testing-release.yml", "head_sha" => COMMIT }
  jobs = %w[Android iOS].map { |platform| { "name" => "mobile-internal / #{platform} internal", "conclusion" => "success" } }
  artifacts = %w[android ios].flat_map do |platform|
    %w[receipt signed].map { |kind| { "id" => 1, "name" => "mobile-#{kind}-#{platform}-#{BUILD}" } }
  end
  page = lambda { |key, values| JSON.generate(multiple_pages ? [{ key => [] }, { key => values }] : [{ key => values }]) }
  requests = [request("git", "rev-parse", "#{CURRENT}^{tree}", stdout: "#{changed_tree ? 'f' * 40 : TREE}\n")]
  runs_payload = fault == "empty_runs" ? page.call("workflow_runs", []) : page.call("workflow_runs", [run])
  runs_payload = '{"workflow_runs":[]} {"workflow_runs":[]}' if fault == "concatenated_pages"
  runs_payload = JSON.generate([{ "workflow_runs" => "bad" }]) if fault == "malformed_runs"
  requests << request("gh", "api", "--paginate", "--slurp", "repos/#{REPOSITORY}/actions/workflows/testing-release.yml/runs?branch=testing&per_page=100",
    stdout: runs_payload, exit_code: fault == "transport_error" ? 1 : 0)
  return requests if %w[empty_runs concatenated_pages malformed_runs transport_error].include?(fault)

  requests << request("gh", "api", "--paginate", "--slurp", "repos/#{REPOSITORY}/actions/runs/1/jobs?per_page=100",
    stdout: fault == "malformed_jobs" ? JSON.generate([{ "jobs" => [nil] }]) : page.call("jobs", jobs))
  return requests if fault == "malformed_jobs"

  requests << request("gh", "api", "--paginate", "--slurp", "repos/#{REPOSITORY}/actions/runs/1/artifacts?per_page=100",
    stdout: fault == "malformed_artifacts" ? JSON.generate([{ "artifacts" => {} }]) : page.call("artifacts", artifacts))
  return requests if fault == "malformed_artifacts"

  %w[android ios].each do |platform|
    receipt, files = mobile_receipt(platform)
    receipt["buildNumber"] += 1 if fault == "wrong_build"
    requests << request("gh", "run", "download", "1", "--repo", REPOSITORY, "--name", "mobile-receipt-#{platform}-#{BUILD}", "--dir", "ANY_TEMP",
      files: { "#{platform}-artifact-receipt.json" => JSON.generate(receipt) })
    requests << request("gh", "run", "download", "1", "--repo", REPOSITORY, "--name", "mobile-signed-#{platform}-#{BUILD}", "--dir", "ANY_TEMP", files: files)
    return requests if changed_tree || fault == "wrong_build"

    requests << request("git", "rev-parse", "#{COMMIT}^{tree}", stdout: "#{TREE}\n")
    requests << request("git", "merge-base", "--is-ancestor", COMMIT, CURRENT, exit_code: fault == "unrelated" ? 1 : 0)
    return requests if fault == "unrelated"

    files.each_key do |_name|
      requests << request("gh", "attestation", "verify", "ANY_TEMP", "--repo", REPOSITORY, "--signer-workflow",
        "#{REPOSITORY}/.github/workflows/testing-release.yml", "--source-ref", "refs/heads/testing", "--source-digest", COMMIT, "--deny-self-hosted-runners")
    end
  end
  requests << request("git", "merge-base", "--is-ancestor", COMMIT, CURRENT)
  requests
end

%w[one_page two_pages changed_tree wrong_build unrelated empty_runs concatenated_pages malformed_runs malformed_jobs malformed_artifacts transport_error].each do |scenario|
  check("PVA-018/019 real resume CLI #{scenario}") do
    with_fixture do |root, environment|
      changed_tree = scenario == "changed_tree"
      fault = %w[one_page two_pages changed_tree].include?(scenario) ? nil : scenario
      queue(environment, resume_requests(multiple_pages: scenario == "two_pages", changed_tree: changed_tree, fault: fault))
      output = root.join("resumed")
      result = cli(environment, "resume-testing-candidate-receipts.rb", "--version", VERSION, "--build-number", BUILD,
        "--source-commit", CURRENT, "--source-tree", changed_tree ? "f" * 40 : TREE,
        "--confirmation", "resume:#{VERSION}:#{BUILD}", "--output-dir", output)
      expected = %w[one_page two_pages].include?(scenario)
      assert(result.last.success? == expected, result[1])
      assert(output.join("resume-receipt-sources.json").exist? == expected)
      assert(output.join("android-artifact-receipt.json").exist? == expected)
      assert(calls(environment).none? { |argv| argv.include?("POST") || argv.include?("PATCH") || argv.include?("upload") })
    end
  end
end

check("PVA-018 external distribution depends on successful read-only resume") do
  job = workflow("testing-release.yml").fetch("jobs").fetch("mobile-external")
  assert(job.fetch("needs").include?("resume-internal-receipts"))
  assert(job.fetch("if").include?("needs.resume-internal-receipts.result == 'success'"))
  resume_step = step(workflow("testing-release.yml"), "resume-internal-receipts", "Resume attested internal receipts for this exact candidate")
  assert(resume_step.fetch("run").include?('--source-tree "$source_tree"'))
end

check("PVA-028 resumed publication has its own complete candidate history") do
  publication = workflow("testing-release.yml").fetch("jobs").fetch("publish-candidate")
  checkouts = publication.fetch("steps").select do |entry|
    entry["uses"].to_s.start_with?("actions/checkout@")
  end
  assert(checkouts.one?, "Publication must have one unambiguous checkout")
  assert(checkouts.first.dig("with", "fetch-depth") == 0,
    "Publication cannot inherit Git history from another job; original resume commits must remain available")
  manifest = step(workflow("testing-release.yml"), "publish-candidate", "Create and validate candidate manifest")
  assert(manifest.fetch("run").include?('recorded_tree="$(git rev-parse "$source_commit^{tree}")"'))
  assert(manifest.fetch("run").include?('git merge-base --is-ancestor "$source_commit" "$GITHUB_SHA"'))
  assert(manifest.fetch("run").include?('if [[ "$desktop_build_tree" != "$source_tree" ]]; then'))
end

keychain_source = ROOT.join("scripts/verify-ios-release-signing.sh").read
check("PVA-021 standalone verifier capture, ownership and restore are production-wired") do
  assert(keychain_source.include?('source "$repository_root/scripts/lib/macos-keychain.sh"'))
  assert(keychain_source.index('passvault_capture_user_keychains "$original_keychains_file"') < keychain_source.index("security create-keychain"))
  assert(keychain_source.include?("keychain_creation_attempted=true\nsecurity create-keychain"))
  assert(!keychain_source.include?("done < <(security list-keychains"))
end

%w[capture_failed capture_empty capture_malformed success later_failure cancellation activation_failed restore_failed
   profile_cleanup_failed root_cleanup_failed].each do |scenario|
  check("PVA-021 production cleanup/capture block with fake security #{scenario}") do
    with_fixture do |root, environment|
      block = keychain_source[keychain_source.index('verification_root="')...keychain_source.index("export IOS_DISTRIBUTION_CERTIFICATE_PASSWORD")]
      # Exact production block includes capture, cleanup and signal traps, but
      # excludes private input loading and every certificate/Xcode operation.
      script = "set -euo pipefail\nsource #{ROOT.join('scripts/lib/macos-keychain.sh').to_s.inspect}\n" + block
      original = ["/fixture/keychains/login with spaces.keychain-db", "/fixture/keychains/system.keychain-db"]
      capture = original.map { |path| "    #{path.inspect}\n" }.join
      capture = "" if %w[capture_failed capture_empty].include?(scenario)
      capture = "not a quoted path\n" if scenario == "capture_malformed"
      requests = [request("security", "list-keychains", "-d", "user", stdout: capture, exit_code: scenario == "capture_failed" ? 1 : 0)]
      cleanup_failure = %w[profile_cleanup_failed root_cleanup_failed].include?(scenario)
      if cleanup_failure
        # Intercept only these synthetic cleanup cases. No failed rm is ever
        # forwarded to a real command; the outer disposable fixture owns cleanup.
        root.join("bin/rm").write("#!#{RUBY}\nload #{ROOT.join('scripts/testdata/release-regressions/fake-provider.rb').to_s.inspect}\n")
        root.join("bin/rm").chmod(0o700)
      end
      if !scenario.start_with?("capture_")
        script += "keychain_creation_attempted=true\nsecurity create-keychain -p fixture \"$keychain_path\"\n"
        script += "passvault_activate_release_keychain \"$keychain_path\" \"$original_keychains_file\"\n"
        if scenario == "profile_cleanup_failed"
          script += <<~'SH'
            installed_profile="$HOME/Library/MobileDevice/Provisioning Profiles/00000000-0000-0000-0000-000000000000.mobileprovision"
            mkdir -p -- "$(dirname "$installed_profile")"
            printf '%s\n' 'inert synthetic profile, no signing material' > "$profile_staging"
            ruby -e 'File.link(ARGV.fetch(0), ARGV.fetch(1))' "$profile_staging" "$installed_profile"
            installed_profile_by_script=true
          SH
        end
        script += scenario == "cancellation" ? "kill -TERM \"$$\"\n" : "exit #{scenario == 'later_failure' ? 19 : 0}\n"
        requests << request("security", "create-keychain", "-p", "fixture", "ANY_TEMP")
        requests << request("security", "list-keychains", "-d", "user", "-s", "ANY_TEMP", *original, exit_code: scenario == "activation_failed" ? 1 : 0)
        requests << request("rm", "-f", "--", "ANY_TEMP", exit_code: 1) if scenario == "profile_cleanup_failed"
        requests << request("security", "list-keychains", "-d", "user", "-s", *original, exit_code: scenario == "restore_failed" ? 1 : 0)
        requests << request("security", "delete-keychain", "ANY_TEMP")
        requests << request("rm", "-rf", "--", "ANY_TEMP", exit_code: scenario == "root_cleanup_failed" ? 1 : 0) if cleanup_failure
      end
      queue(environment, requests)
      result = run(environment, "bash", "--noprofile", "--norc", "-c", script)
      assert(result.last.success? == (scenario == "success"), result[1])
      assert(result.last.exitstatus == 19) if scenario == "later_failure"
      assert(result.last.exitstatus == 143) if scenario == "cancellation"
      assert(calls(environment).length == 1) if scenario.start_with?("capture_")
      if cleanup_failure
        assert(result.last.exitstatus == 1, "Cleanup failure must remain a failed verification")
        assert(calls(environment).include?(["security", "list-keychains", "-d", "user", "-s", *original]),
          "Cleanup failure skipped original keychain restoration")
        assert(calls(environment).any? { |argv| argv[0, 2] == ["security", "delete-keychain"] },
          "Cleanup failure skipped owned keychain deletion")
        expected_error = scenario == "profile_cleanup_failed" ? "temporary installed provisioning profile" : "temporary signed-verification directory"
        assert(result[1].include?(expected_error), "Cleanup failure diagnostic was lost")
      end
    end
  end
end

# Execute the actual installation and EXIT-cleanup blocks with synthetic files;
# this adds no native signing/provider or application-launch dependency.
require_relative "test-ios-profile-cleanup"
PassVault::IosProfileCleanupTest::SCENARIOS.each do |scenario|
  check("PVA-032 production profile install/cleanup #{scenario}") do
    PassVault::IosProfileCleanupTest.verify_scenario(scenario)
  end
end

pin = "a" * 40
{
  "named_pinned" => ["- name: Checkout\n  uses: actions/checkout@#{pin}", true],
  "unnamed_pinned" => ["- uses: actions/checkout@#{pin}", true],
  "inline_quoted_pinned" => ["- {uses: 'actions/checkout@#{pin}'}", true],
  "local" => ["- uses: ./.github/actions/fixture", true],
  "named_mutable" => ["- name: Checkout\n  uses: actions/checkout@main", false],
  "unnamed_mutable" => ["- uses: actions/checkout@main", false],
  "inline_mutable" => ["- {uses: \"actions/checkout@v7\"}", false],
  "duplicate_hidden" => ["- uses: actions/checkout@main\n  uses: actions/checkout@#{pin}", false],
  "local_traversal" => ["- uses: ./../outside", false],
  "invalid_yaml" => ["- uses: [", false],
}.each do |name, (body, expected)|
  check("PVA-022 structural action scanner #{name}") do
    with_fixture do |root, environment|
      workflows = root.join("workflows"); workflows.mkdir
      workflows.join("fixture.yml").write("name: Fixture\non: push\njobs:\n  test:\n    steps:\n    #{body.gsub("\n", "\n    ")}\n")
      result = run(environment, "bash", ROOT.join("scripts/validate-workflow-action-pins.sh"), workflows)
      assert(result.last.success? == expected, result[1])
    end
  end
end

check("PVA-022 exhaustive current workflow references") do
  with_fixture do |_root, environment|
    counts = { remote: 0, local: 0 }
    walk = lambda do |value|
      case value
      when Hash
        if value.key?("uses")
          counts[value.fetch("uses").start_with?("./") ? :local : :remote] += 1
        end
        value.each_value { |child| walk.call(child) }
      when Array then value.each { |child| walk.call(child) }
      end
    end
    Dir.glob(ROOT.join(".github/workflows/*.{yml,yaml}").to_s).each { |path| walk.call(YAML.safe_load(File.read(path), aliases: false)) }
    result = run(environment, "bash", ROOT.join("scripts/validate-workflow-action-pins.sh"))
    assert(result.last.success?, result[1])
    assert(result[0].include?("#{counts[:remote]} external, #{counts[:local]} local"), "Not all structural uses entries were inspected")
  end
end

%w[delete_success already_missing delete_failed stale_readback readback_failed].each do |scenario|
  check("PVA-023 real obsolete-secret CLI #{scenario}") do
    with_fixture do |_root, environment|
      path = "environments/mobile-external-beta/secrets/TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64"
      code = scenario == "already_missing" ? 404 : scenario == "delete_failed" ? 503 : 204
      requests = [api("DELETE", path, code)]
      unless scenario == "delete_failed"
        requests << api("GET", path, scenario == "stale_readback" ? 200 : scenario == "readback_failed" ? 403 : 404, {})
      end
      queue(environment, requests)
      result = cli(environment, "delete-github-environment-secret.rb", REPOSITORY, "mobile-external-beta", "TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64")
      assert(result.last.success? == %w[delete_success already_missing].include?(scenario), result[1])
    end
  end
end

check("PVA-023 configurator enforces conditional absence for both optional secrets") do
  source = ROOT.join("scripts/configure-github-mobile-release.sh").read
  %w[testflight play].each do |platform|
    secret = platform == "testflight" ? "TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64" : "PLAY_CLOSED_TESTERS_BASE64"
    assert(source.match?(/else\n    ruby scripts\/delete-github-environment-secret\.rb[^\n]*\n        mobile-external-beta #{secret}\nfi/))
    assert(source.match?(/if \[\[ "\$#{platform}_testers_ready" != true \]\]; then\n    verify_absent_environment_secrets[^\n]*\n        #{secret}\nfi/))
  end
end

%w[canonical alternate_path canonical_placeholder canonical_wrong_language symlink].each do |scenario|
  check("PVA-024 canonical payload validation/archive #{scenario}") do
    with_fixture do |root, environment|
      metadata = root.join("metadata"); FileUtils.cp_r(ROOT.join("scripts/testdata/mobile-store"), metadata)
      mapping = {
        "RELEASE_NOTES_EN_FILE" => "release-notes-en.md", "RELEASE_NOTES_AR_FILE" => "release-notes-ar.md",
        "PRIVACY_TEXT_EN_FILE" => "privacy-en.md", "PRIVACY_TEXT_AR_FILE" => "privacy-ar.md",
        "STORE_METADATA_EN_FILE" => "store-metadata-en.env", "STORE_METADATA_AR_FILE" => "store-metadata-ar.env",
        "STORE_DESCRIPTION_EN_FILE" => "store-description-en.md", "STORE_DESCRIPTION_AR_FILE" => "store-description-ar.md",
      }
      values = root.join("values.env")
      values.write(mapping.map { |key, name| "#{key}=release/private/#{name}\n" }.join)
      if scenario == "alternate_path"
        FileUtils.cp(metadata.join("store-description-ar.md"), metadata.join("approved-ar.md"))
        values.write(values.read.sub("STORE_DESCRIPTION_AR_FILE=release/private/store-description-ar.md", "STORE_DESCRIPTION_AR_FILE=release/private/approved-ar.md"))
      elsif scenario == "canonical_placeholder"
        metadata.join("store-description-ar.md").write("استبدل هذا النص\n")
      elsif scenario == "canonical_wrong_language"
        metadata.join("store-description-ar.md").write("English only canonical description\n")
      elsif scenario == "symlink"
        metadata.join("store-description-ar.md").unlink
        File.symlink(ROOT.join("scripts/testdata/mobile-store/store-description-ar.md"), metadata.join("store-description-ar.md"))
      end
      result = cli(environment, "validate-store-metadata-inputs.rb", metadata, values)
      assert(result.last.success? == (scenario == "canonical"), result[1])
      if scenario != "alternate_path"
        archive = root.join("metadata.tar.gz")
        created = cli(environment, "create-store-metadata-archive.rb", metadata, archive)
        assert(created.last.success? == (scenario == "canonical"), created[1])
        assert(archive.exist? == (scenario == "canonical"))
      end
      assert(calls(environment).empty?)
    end
  end
end

check("PVA-024 readiness, renderer and actual archive snapshot use the shared contract") do
  private_validator = ROOT.join("scripts/validate-private-release-config.sh").read
  configurator = ROOT.join("scripts/configure-github-mobile-release.sh").read
  renderer = ROOT.join("scripts/prepare-mobile-store-metadata.sh").read
  assert(private_validator.include?('"$private_root" "$values_file"'))
  assert(private_validator.include?("validate-store-metadata-inputs.rb"))
  assert(renderer.include?("validate-store-metadata-inputs.rb"))
  assert(configurator.include?('prepare-mobile-store-metadata.sh "$metadata_validation_root"'))
  assert(configurator.index('prepare-mobile-store-metadata.sh "$metadata_validation_root"') < configurator.index("set_metadata_archive_secret()"))
end

check("PVA-020 Windows checksum receives actual restored image root for every backend") do
  windows = workflow("release.yml").fetch("jobs").fetch("build-desktop-windows").fetch("steps")
  checksum = windows.find { |entry| entry["run"].to_s.include?("update-desktop-biometric-checksum.ps1") }
  assert(checksum.fetch("run").include?("-RuntimePath app-desktop/build/compose/binaries/main-release/app/PassVault"))
  assert(!checksum.key?("if"), "Checksum is not shared by every signing backend")
  restore = windows.find { |entry| entry["run"].to_s.include?("restore-promoted-windows-app-image.ps1") }
  assert(restore.fetch("run").include?("-OutputRoot app-desktop/build/compose/binaries/main-release/app"))
end

check("PVA-025 checklist current Room scope is explicit and unexecuted gate unchecked") do
  source = ROOT.join("core/database/src/commonMain/kotlin/com/passvault/core/database/VaultDatabase.kt").read
  version = source[/version\s*=\s*(\d+)/, 1]
  checklist = ROOT.join("RELEASE_CHECKLIST.md").read
  assert(checklist.include?("- [ ] Every supported prior Room schema (1, 2, 3, and 4) upgrades non-destructively to schema #{version}"))
  assert(!checklist.include?("to schema 3, and a"))
  assert(checklist.include?("build `1017001` is already allocated and immutable"))
end

if FAILURES.any?
  abort("#{FAILURES.length} release regression cases failed; #{CASES.length} passed.\n#{FAILURES.join("\n")}")
end
puts "#{CASES.length} deterministic release regression cases passed; no real providers, builds, signing or publication used."
