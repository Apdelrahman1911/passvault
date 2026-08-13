#!/usr/bin/env ruby

require "digest"
require "json"
require "pathname"
require "time"

MAX_ARTIFACT_BYTES = 8 * 1024 * 1024 * 1024
PLATFORM_KINDS = {
  "android" => %w[aab apk r8-mapping],
  "ios" => %w[ipa xcarchive link-map],
}.freeze
PLATFORM_IDENTIFIERS = {
  "android" => "com.passvault.android",
  "ios" => "com.passvault.ios",
}.freeze

def required_environment(name)
  value = ENV[name]&.strip
  abort("Missing required environment variable: #{name}") if value.nil? || value.empty?
  value
end

def canonical_file(path_text)
  path = Pathname.new(path_text).expand_path
  abort("Artifact path must be an existing regular file: #{path_text}") unless path.file? && !path.symlink?

  size = path.size
  abort("Artifact must be non-empty: #{path_text}") unless size.positive?
  abort("Artifact exceeds the 8 GiB receipt limit: #{path_text}") if size > MAX_ARTIFACT_BYTES
  abort("Artifact name is not canonical: #{path.basename}") unless path.basename.to_s.match?(/\A[A-Za-z0-9._+-]+\z/)
  path
end

platform = required_environment("RECEIPT_PLATFORM")
kinds = PLATFORM_KINDS.fetch(platform) { abort("RECEIPT_PLATFORM must be android or ios") }
version = required_environment("RECEIPT_VERSION")
build_number = Integer(required_environment("RECEIPT_BUILD_NUMBER"), 10)
source_commit = required_environment("RECEIPT_SOURCE_COMMIT").downcase
source_tree = required_environment("RECEIPT_SOURCE_TREE").downcase
identifier = required_environment("RECEIPT_IDENTIFIER")
signing_fingerprint = required_environment("RECEIPT_SIGNING_FINGERPRINT").delete(":").upcase

abort("Invalid marketing version") unless version.match?(/\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/)
abort("Invalid build number") unless build_number.between?(1_000_000, 2_100_000_000)
abort("Invalid source commit") unless source_commit.match?(/\A[0-9a-f]{40}\z/)
abort("Invalid source tree") unless source_tree.match?(/\A[0-9a-f]{40}\z/)
abort("Unexpected #{platform} Store identity") unless identifier == PLATFORM_IDENTIFIERS.fetch(platform)
expected_fingerprint_length = platform == "android" ? 64 : 40
unless signing_fingerprint.match?(/\A[0-9A-F]{#{expected_fingerprint_length}}\z/)
  abort("Invalid #{platform} signing fingerprint")
end

artifacts = kinds.map do |kind|
  environment_name = "RECEIPT_#{kind.tr("-", "_").upcase}_PATH"
  path = canonical_file(required_environment(environment_name))
  {
    kind: kind,
    fileName: path.basename.to_s,
    sizeBytes: path.size,
    sha256: Digest::SHA256.file(path).hexdigest,
  }
end

receipt = {
  schemaVersion: 1,
  platform: platform,
  marketingVersion: version,
  buildNumber: build_number,
  sourceCommit: source_commit,
  sourceTree: source_tree,
  createdAt: Time.now.utc.strftime("%Y-%m-%dT%H:%M:%SZ"),
  identifier: identifier,
  signingFingerprint: signing_fingerprint,
  artifacts: artifacts,
}

puts JSON.pretty_generate(receipt)
