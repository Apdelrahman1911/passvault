#!/usr/bin/env ruby

require "digest"
require "json"
require "pathname"
require "time"

MAX_RECEIPT_BYTES = 64 * 1024
MAX_ARTIFACT_BYTES = 8 * 1024 * 1024 * 1024
PLATFORM_KINDS = {
  "android" => %w[aab apk r8-mapping],
  "ios" => %w[ipa xcarchive link-map],
}.freeze
PLATFORM_IDENTIFIERS = {
  "android" => "com.passvault.android",
  "ios" => "com.passvault.ios",
}.freeze

def canonical_string(document, key)
  value = document.fetch(key)
  abort("#{key} must be a canonical non-empty string") unless value.is_a?(String) && !value.empty? && value == value.strip
  value
end

receipt_path = ARGV.fetch(0) do
  abort("Usage: #{$PROGRAM_NAME} <receipt.json> <platform> <version> <build> <commit> <tree> [artifact-directory]")
end
expected_platform = ARGV.fetch(1)
expected_version = ARGV.fetch(2)
expected_build = Integer(ARGV.fetch(3), 10)
expected_commit = ARGV.fetch(4).downcase
expected_tree = ARGV.fetch(5).downcase
artifact_directory = ARGV[6]
abort("Too many arguments") if ARGV.length > 7

receipt = Pathname.new(receipt_path).expand_path
unless receipt.file? && !receipt.symlink? && receipt.size.positive? && receipt.size <= MAX_RECEIPT_BYTES
  abort("Artifact receipt is missing, unsafe, empty, or too large")
end

document = JSON.parse(receipt.read(encoding: "UTF-8"))
abort("Artifact receipt root must be an object") unless document.is_a?(Hash)
expected_keys = %w[
  schemaVersion platform marketingVersion buildNumber sourceCommit sourceTree createdAt
  identifier signingFingerprint artifacts
]
abort("Artifact receipt fields are unexpected or incomplete") unless document.keys.sort == expected_keys.sort
abort("Unsupported artifact receipt schema") unless document["schemaVersion"] == 1

platform = canonical_string(document, "platform")
abort("Unexpected receipt platform") unless platform == expected_platform && PLATFORM_KINDS.key?(platform)
version = canonical_string(document, "marketingVersion")
abort("Unexpected receipt version") unless version == expected_version
build_number = document.fetch("buildNumber")
abort("Unexpected receipt build number") unless build_number.is_a?(Integer) && build_number == expected_build
source_commit = canonical_string(document, "sourceCommit")
source_tree = canonical_string(document, "sourceTree")
abort("Unexpected receipt source commit") unless source_commit == expected_commit && source_commit.match?(/\A[0-9a-f]{40}\z/)
abort("Unexpected receipt source tree") unless source_tree == expected_tree && source_tree.match?(/\A[0-9a-f]{40}\z/)

created_at = canonical_string(document, "createdAt")
begin
  parsed_time = Time.iso8601(created_at)
rescue ArgumentError
  abort("Invalid receipt creation time")
end
unless created_at.match?(/\A\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z\z/) && parsed_time.utc_offset.zero?
  abort("Receipt creation time must be canonical UTC")
end

identifier = canonical_string(document, "identifier")
identifier_pattern = platform == "android" ? /\A[A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)+\z/ : /\A[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+\z/
abort("Invalid platform identifier") unless identifier.match?(identifier_pattern)
abort("Unexpected #{platform} Store identity") unless identifier == PLATFORM_IDENTIFIERS.fetch(platform)
fingerprint = canonical_string(document, "signingFingerprint")
fingerprint_length = platform == "android" ? 64 : 40
abort("Invalid signing fingerprint") unless fingerprint.match?(/\A[0-9A-F]{#{fingerprint_length}}\z/)

artifacts = document.fetch("artifacts")
expected_kinds = PLATFORM_KINDS.fetch(platform)
unless artifacts.is_a?(Array) && artifacts.length == expected_kinds.length
  abort("Artifact receipt has the wrong artifact count")
end

seen_names = {}
artifacts.each_with_index do |artifact, index|
  abort("Artifact entry must be an object") unless artifact.is_a?(Hash)
  expected_artifact_keys = %w[kind fileName sizeBytes sha256]
  abort("Artifact entry fields are unexpected or incomplete") unless artifact.keys.sort == expected_artifact_keys.sort

  kind = canonical_string(artifact, "kind")
  abort("Artifact kinds are missing or out of canonical order") unless kind == expected_kinds.fetch(index)
  file_name = canonical_string(artifact, "fileName")
  abort("Unsafe artifact file name") unless file_name.match?(/\A[A-Za-z0-9._+-]+\z/) && File.basename(file_name) == file_name
  abort("Duplicate artifact file name") if seen_names[file_name]
  seen_names[file_name] = true
  size = artifact.fetch("sizeBytes")
  abort("Invalid artifact size") unless size.is_a?(Integer) && size.positive? && size <= MAX_ARTIFACT_BYTES
  sha256 = canonical_string(artifact, "sha256")
  abort("Invalid artifact SHA-256") unless sha256.match?(/\A[0-9a-f]{64}\z/)

  next unless artifact_directory

  root = Pathname.new(artifact_directory).expand_path
  abort("Artifact directory is unsafe") unless root.directory? && !root.symlink?
  path = root.join(file_name)
  unless path.file? && !path.symlink? && path.dirname.realpath == root.realpath
    abort("Receipt artifact is missing or unsafe: #{file_name}")
  end
  abort("Artifact size does not match receipt: #{file_name}") unless path.size == size
  abort("Artifact digest does not match receipt: #{file_name}") unless Digest::SHA256.file(path).hexdigest == sha256
end

puts "Validated #{platform} artifact receipt for #{version} build #{build_number}."
