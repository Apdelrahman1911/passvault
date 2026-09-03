#!/usr/bin/env ruby

require "json"
require "digest"
require "open3"
require "optparse"
require "pathname"

SOURCE_EXTENSIONS = %w[.kt .kts].freeze
SECURITY_EXTENSIONS = %w[
  .bash .c .cc .cpp .cxx .h .hh .hpp .java .kt .kts .m .mm .ps1 .py .rb .sh .swift .yaml .yml
].freeze
SECURITY_EXCLUDED_PREFIXES = %w[
  agent-skills/
  scripts/testdata/security-analysis/
].freeze
EXPECTED_OPENGREP_VERSION = "1.29.0"
EXPECTED_CANARY_RULE = "passvault.kotlin.insecure-cipher-transformation"
CANARY_PATH = "scripts/testdata/security-analysis/Canary.kt"

def fail_policy(message)
  warn(message)
  exit(1)
end

def tracked_files(root)
  output, error, status = Open3.capture3(
    "git",
    "-C",
    root,
    "ls-files",
    "--cached",
    "--others",
    "--exclude-standard",
    "-z",
  )
  fail_policy("Unable to inventory source files: #{error.strip}") unless status.success?

  output.split("\0").reject(&:empty?).sort.each do |relative_path|
    fail_policy("Source path contains a line break: #{relative_path.inspect}") if relative_path.match?(/[\r\n]/)
    absolute_path = File.join(root, relative_path)
    fail_policy("Source path is not a regular in-tree file: #{relative_path}") unless
      File.file?(absolute_path) && !File.symlink?(absolute_path)
  end
end

def included_projects(root)
  settings = File.read(File.join(root, "settings.gradle.kts"), encoding: "UTF-8")
  settings.scan(/["'](:[A-Za-z0-9_-]+(?::[A-Za-z0-9_-]+)*)["']/).flatten.uniq.sort
end

def project_directory(project_path)
  project_path.delete_prefix(":").tr(":", "/")
end

def verify_minimum(label, actual, expected)
  fail_policy("#{label} coverage fell below its reviewed floor: #{actual} < #{expected}") if actual < expected
end

def load_baseline(path)
  baseline = JSON.parse(File.read(path, encoding: "UTF-8"))
  fail_policy("Static-analysis baseline schema is unsupported") unless baseline["schemaVersion"] == 1
  baseline
rescue JSON::ParserError, Errno::ENOENT => error
  fail_policy("Unable to read static-analysis baseline: #{error.message}")
end

def verify_inventory(root, baseline, files)
  detekt = baseline.fetch("detekt")
  projects = included_projects(root)
  expected_projects = detekt.fetch("minimumProjectSourceCounts").keys.sort
  fail_policy("Static-analysis baseline does not match settings.gradle.kts") unless projects == expected_projects

  kotlin_files = files.select { |path| SOURCE_EXTENSIONS.include?(File.extname(path).downcase) }
  gradle_scripts = kotlin_files.select { |path| path.end_with?(".gradle.kts") }
  covered = gradle_scripts.dup

  detekt.fetch("minimumProjectSourceCounts").each do |project, minimum|
    prefix = "#{project_directory(project)}/src/"
    project_sources = kotlin_files.select { |path| path.start_with?(prefix) }
    verify_minimum("Detekt #{project}", project_sources.length, minimum)
    covered.concat(project_sources)
  end

  ignored_fixture = kotlin_files.select { |path| path.start_with?("scripts/testdata/security-analysis/") }
  uncovered = kotlin_files - covered - ignored_fixture
  fail_policy("Kotlin files are outside the configured Detekt roots: #{uncovered.join(', ')}") unless uncovered.empty?
  verify_minimum("Detekt aggregate", covered.uniq.length, detekt.fetch("minimumFileCount"))
  verify_minimum("Detekt Gradle scripts", gradle_scripts.length, detekt.fetch("minimumGradleScriptCount"))

  security_targets = files.select do |path|
    SECURITY_EXTENSIONS.include?(File.extname(path).downcase) &&
      SECURITY_EXCLUDED_PREFIXES.none? { |prefix| path.start_with?(prefix) }
  end
  groups = {
    "kotlin" => security_targets.count { |path| SOURCE_EXTENSIONS.include?(File.extname(path).downcase) },
    "apple" => security_targets.count { |path| %w[.m .mm .swift].include?(File.extname(path).downcase) },
    "native" => security_targets.count do |path|
      %w[.c .cc .cpp .cxx .h .hh .hpp].include?(File.extname(path).downcase)
    end,
    "automation" => security_targets.count do |path|
      %w[.bash .ps1 .py .rb .sh .yaml .yml].include?(File.extname(path).downcase)
    end,
  }
  security = baseline.fetch("securityAnalysis")
  verify_minimum("Security analysis aggregate", security_targets.length, security.fetch("minimumFileCount"))
  security.fetch("minimumGroupCounts").each do |group, minimum|
    fail_policy("Unknown security-analysis baseline group: #{group}") unless groups.key?(group)
    verify_minimum("Security analysis #{group}", groups.fetch(group), minimum)
  end

  security_targets
rescue KeyError => error
  fail_policy("Static-analysis baseline is incomplete: #{error.message}")
end

def normalized_scanned_path(root, raw_path)
  path = Pathname.new(raw_path)
  absolute_path = path.absolute? ? path.cleanpath : Pathname.new(root).join(path).cleanpath
  root_path = Pathname.new(root).realpath
  relative = absolute_path.relative_path_from(root_path).to_s
  fail_policy("Analyzer reported an out-of-tree path: #{raw_path}") if relative == ".." || relative.start_with?("../")
  relative
rescue ArgumentError
  fail_policy("Analyzer reported an out-of-tree path: #{raw_path}")
end

def load_result(path)
  JSON.parse(File.read(path, encoding: "UTF-8"))
rescue JSON::ParserError, Errno::ENOENT => error
  fail_policy("Unable to read analyzer result: #{error.message}")
end

def verify_result(root, path, expected_targets, baseline)
  result = load_result(path)
  fail_policy("Unexpected OpenGrep version: #{result['version'].inspect}") unless
    result["version"] == EXPECTED_OPENGREP_VERSION
  errors = Array(result["errors"])
  partial_parsing = errors.each_with_object([]) do |error, paths|
    type = Array(error["type"])
    next unless error["level"] == "warn" && type.first == "PartialParsing"

    paths << error["path"]
  end
  fail_policy("OpenGrep reported a non-allowlisted analysis error") unless partial_parsing.length == errors.length

  allowed_partial_parsing = baseline.fetch("securityAnalysis").fetch("allowedPartialParsing")
  fail_policy("OpenGrep partial-parser coverage changed: #{partial_parsing.sort.inspect}") unless
    partial_parsing.sort == allowed_partial_parsing.keys.sort
  allowed_partial_parsing.each do |relative_path, expected_sha256|
    actual_sha256 = Digest::SHA256.file(File.join(root, relative_path)).hexdigest
    fail_policy("A partial-parser allowlisted file changed and requires review: #{relative_path}") unless
      actual_sha256 == expected_sha256
  end

  findings = Array(result["results"])
  unless findings.empty?
    summary = findings.map do |finding|
      location = "#{finding['path']}:#{finding.dig('start', 'line')}"
      "#{finding['check_id']} at #{location}"
    end
    fail_policy("Security analysis found #{findings.length} issue(s):\n#{summary.join("\n")}")
  end

  scanned = Array(result.dig("paths", "scanned")).map do |raw_path|
    normalized_scanned_path(root, raw_path)
  end.uniq.sort
  missing = expected_targets - scanned
  unexpected = scanned - expected_targets
  fail_policy("OpenGrep coverage mismatch; missing=#{missing.inspect}, unexpected=#{unexpected.inspect}") unless
    missing.empty? && unexpected.empty?

  puts "OpenGrep #{EXPECTED_OPENGREP_VERSION} analyzed #{scanned.length} reviewed source files with no findings."
  puts "Reviewed #{partial_parsing.length} file-specific Kotlin parser limitations against pinned file hashes."
end

def verify_canary(root, path)
  result = load_result(path)
  fail_policy("Unexpected OpenGrep version in canary result") unless result["version"] == EXPECTED_OPENGREP_VERSION
  fail_policy("OpenGrep canary reported analysis errors") unless Array(result["errors"]).empty?
  findings = Array(result["results"])
  matching = findings.select { |finding| finding["check_id"].to_s.end_with?(EXPECTED_CANARY_RULE) }
  fail_policy("OpenGrep canary did not produce exactly one expected finding") unless
    findings.length == 1 && matching.length == 1
  scanned = Array(result.dig("paths", "scanned")).map do |raw_path|
    normalized_scanned_path(root, raw_path)
  end
  fail_policy("OpenGrep canary did not scan only #{CANARY_PATH}") unless scanned == [CANARY_PATH]

  puts "OpenGrep failure canary produced the expected #{EXPECTED_CANARY_RULE} finding."
end

options = {
  root: File.expand_path("..", __dir__),
  mode: :verify_inventory,
}
OptionParser.new do |parser|
  parser.on("--root PATH") { |path| options[:root] = File.expand_path(path) }
  parser.on("--baseline PATH") { |path| options[:baseline] = File.expand_path(path) }
  parser.on("--list-security-targets") { options[:mode] = :list_targets }
  parser.on("--verify-result PATH") do |path|
    options[:mode] = :verify_result
    options[:result] = File.expand_path(path)
  end
  parser.on("--verify-canary-result PATH") do |path|
    options[:mode] = :verify_canary
    options[:result] = File.expand_path(path)
  end
end.parse!

root = File.realpath(options.fetch(:root))
baseline_path = options.fetch(
  :baseline,
  File.join(root, ".github", "security-analysis", "coverage-baseline.json"),
)
files = tracked_files(root)
baseline = load_baseline(baseline_path)
targets = verify_inventory(root, baseline, files)

case options.fetch(:mode)
when :verify_inventory
  puts "Static-analysis coverage floor is satisfied (#{targets.length} security-analysis targets)."
when :list_targets
  puts targets
when :verify_result
  verify_result(root, options.fetch(:result), targets, baseline)
when :verify_canary
  verify_canary(root, options.fetch(:result))
else
  fail_policy("Unsupported verification mode")
end
