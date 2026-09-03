#!/usr/bin/env ruby

require "digest"
require "json"
require "pathname"
require "time"

MAX_RECEIPT_BYTES = 128 * 1024
MAX_FILE_BYTES = 16 * 1024 * 1024 * 1024
ARTIFACT_POLICIES = {
  "linuxDeb" => ["linux", "x64", "package"],
  "linuxRpm" => ["linux", "x64", "package"],
  "windowsX64AppImage" => ["windows", "x64", "app-image"],
  "macosArm64AppImage" => ["macos", "arm64", "app-image"],
  "macosX64AppImage" => ["macos", "x64", "app-image"],
}.freeze

def required_string(document, key)
  value = document.fetch(key)
  unless value.is_a?(String) && !value.empty? && value == value.strip
    abort("#{key} must be a canonical non-empty string")
  end
  value
end

receipt_path = Pathname.new(ARGV.fetch(0) do
  abort(
    "Usage: #{$PROGRAM_NAME} <receipt> <version> <build> <commit> <tree> " \
      "[artifact-directory [artifact-key ...]]",
  )
end).expand_path
expected_version = ARGV.fetch(1)
expected_build = Integer(ARGV.fetch(2), 10)
expected_commit = ARGV.fetch(3).downcase
expected_tree = ARGV.fetch(4).downcase
artifact_root_argument = ARGV[5]
requested_keys = ARGV.drop(6)

unless receipt_path.file? && !receipt_path.symlink? &&
       receipt_path.size.positive? && receipt_path.size <= MAX_RECEIPT_BYTES
  abort("Desktop artifact receipt is missing, unsafe, empty, or too large")
end

document = JSON.parse(receipt_path.read(encoding: "UTF-8"))
expected_root_keys = %w[
  schemaVersion marketingVersion buildNumber candidateTag sourceCommit sourceTree
  sourceArtifactRun createdAt artifacts
]
unless document.is_a?(Hash) && document.keys.sort == expected_root_keys.sort
  abort("Desktop artifact receipt fields are unexpected or incomplete")
end
abort("Unsupported desktop artifact receipt schema") unless document.fetch("schemaVersion") == 1

version = required_string(document, "marketingVersion")
unless version.match?(/\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/) &&
       version == expected_version
  abort("Desktop artifact receipt version does not match")
end
build_number = document.fetch("buildNumber")
unless build_number.is_a?(Integer) && build_number.between?(1_000_000, 2_100_000_000) &&
       build_number == expected_build
  abort("Desktop artifact receipt build number does not match")
end
unless required_string(document, "candidateTag") == "v#{version}-rc.#{build_number}"
  abort("Desktop artifact receipt candidate tag does not match")
end
commit = required_string(document, "sourceCommit")
tree = required_string(document, "sourceTree")
unless commit.match?(/\A[0-9a-f]{40}\z/) && commit == expected_commit
  abort("Desktop artifact receipt source commit does not match")
end
unless tree.match?(/\A[0-9a-f]{40}\z/) && tree == expected_tree
  abort("Desktop artifact receipt source tree does not match")
end

run = document.fetch("sourceArtifactRun")
expected_run_keys = %w[repository workflow runId runAttempt]
unless run.is_a?(Hash) && run.keys.sort == expected_run_keys.sort
  abort("Desktop artifact source run is malformed")
end
repository = required_string(run, "repository")
unless repository.match?(%r{\A[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+\z})
  abort("Desktop artifact source repository is invalid")
end
expected_repository = ENV["EXPECTED_SOURCE_REPOSITORY"]&.strip
expected_repository = ENV["GITHUB_REPOSITORY"]&.strip if expected_repository.nil? || expected_repository.empty?
if expected_repository && !expected_repository.empty? && repository != expected_repository
  abort("Desktop artifact source repository does not match")
end
unless required_string(run, "workflow") == ".github/workflows/testing-release.yml"
  abort("Desktop artifact source workflow is invalid")
end
run_id = run.fetch("runId")
run_attempt = run.fetch("runAttempt")
abort("Desktop artifact source run ID is invalid") unless run_id.is_a?(Integer) && run_id.positive?
unless run_attempt.is_a?(Integer) && run_attempt.between?(1, 1_000_000)
  abort("Desktop artifact source run attempt is invalid")
end

created_at = required_string(document, "createdAt")
begin
  parsed_time = Time.iso8601(created_at)
rescue ArgumentError
  abort("Desktop artifact receipt creation time is invalid")
end
unless created_at.match?(/\A\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z\z/) && parsed_time.utc_offset.zero?
  abort("Desktop artifact receipt creation time must be canonical UTC")
end

artifacts = document.fetch("artifacts")
unless artifacts.is_a?(Hash) && artifacts.keys.sort == ARTIFACT_POLICIES.keys.sort
  abort("Desktop artifact receipt has an unexpected promotion-input set")
end
escaped_version = Regexp.escape(version)
artifacts.each do |key, entry|
  expected_entry_keys = %w[platform architecture role fileName sizeBytes sha256]
  unless entry.is_a?(Hash) && entry.keys.sort == expected_entry_keys.sort
    abort("Desktop artifact entry is malformed: #{key}")
  end
  platform, architecture, role = ARTIFACT_POLICIES.fetch(key)
  unless required_string(entry, "platform") == platform &&
         required_string(entry, "architecture") == architecture &&
         required_string(entry, "role") == role
    abort("Desktop artifact entry has unexpected platform metadata: #{key}")
  end
  name = required_string(entry, "fileName")
  unless name.match?(/\A[A-Za-z0-9._+-]+\z/) && File.basename(name) == name
    abort("Desktop artifact entry has an unsafe file name: #{key}")
  end
  valid_name = case key
  when "linuxDeb"
    name.end_with?(".deb") &&
      name.delete_suffix(".deb").match?(/(?:\A|[^0-9.])#{escaped_version}(?:[^0-9.]|\z)/)
  when "linuxRpm"
    name.end_with?(".rpm") &&
      name.delete_suffix(".rpm").match?(/(?:\A|[^0-9.])#{escaped_version}(?:[^0-9.]|\z)/)
  when "windowsX64AppImage"
    name == "PassVault-#{version}-windows-x64-app-image.zip"
  when "macosArm64AppImage"
    name == "PassVault-#{version}-macos-arm64-app-image.zip"
  when "macosX64AppImage"
    name == "PassVault-#{version}-macos-x64-app-image.zip"
  else
    false
  end
  abort("Desktop artifact entry has an unexpected file name: #{key}") unless valid_name
  size = entry.fetch("sizeBytes")
  unless size.is_a?(Integer) && size.positive? && size <= MAX_FILE_BYTES
    abort("Desktop artifact entry has an invalid size: #{key}")
  end
  digest = required_string(entry, "sha256")
  abort("Desktop artifact entry has an invalid digest: #{key}") unless digest.match?(/\A[0-9a-f]{64}\z/)
end

if artifact_root_argument
  artifact_root = Pathname.new(artifact_root_argument).expand_path
  unless artifact_root.directory? && !artifact_root.symlink?
    abort("Desktop artifact directory is unsafe")
  end
  keys = requested_keys.empty? ? ARTIFACT_POLICIES.keys : requested_keys
  unless keys.uniq.length == keys.length && (keys - ARTIFACT_POLICIES.keys).empty?
    abort("Unknown or duplicate desktop artifact key requested")
  end
  keys.each do |key|
    entry = artifacts.fetch(key)
    path = artifact_root.join(entry.fetch("fileName"))
    unless path.file? && !path.symlink? && path.dirname.realpath == artifact_root.realpath
      abort("Desktop promotion input is missing or unsafe: #{key}")
    end
    abort("Desktop promotion input size differs: #{key}") unless path.size == entry.fetch("sizeBytes")
    unless Digest::SHA256.file(path).hexdigest == entry.fetch("sha256")
      abort("Desktop promotion input digest differs: #{key}")
    end
  end
elsif !requested_keys.empty?
  abort("Artifact keys require an artifact directory")
end

puts "Validated Desktop promotion inputs from workflow run #{run_id}."
