#!/usr/bin/env ruby

require "json"
require "time"

allow_pending = ARGV.delete("--allow-pending")
path = ARGV.fetch(0) do
  abort("Usage: #{$PROGRAM_NAME} [--allow-pending] <candidate-manifest.json> [expected-commit] [expected-tree]")
end
expected_commit = ARGV[1]&.downcase
expected_tree = ARGV[2]&.downcase
abort("Too many arguments") if ARGV.length > 3
if expected_commit && !expected_commit.match?(/\A[0-9a-f]{40}\z/)
  abort("Expected commit must be a full Git SHA")
end
if expected_tree && !expected_tree.match?(/\A[0-9a-f]{40}\z/)
  abort("Expected tree must be a full Git SHA")
end
unless File.file?(path) && !File.symlink?(path) && File.size(path) <= 64 * 1024
  abort("Candidate manifest is missing, unsafe, or too large")
end
document = JSON.parse(File.read(path, encoding: "UTF-8"))
abort("Candidate manifest root must be a JSON object") unless document.is_a?(Hash)

def require_string(document, *keys)
  value = keys.reduce(document) { |value_at_key, key| value_at_key.fetch(key) }
  unless value.is_a?(String) && !value.empty? && value == value.strip
    abort("#{keys.join('.')} must be a canonical non-empty string")
  end
  value
end

abort("Unsupported candidate manifest schema") unless document["schemaVersion"] == 3
expected_root_keys = %w[
  schemaVersion marketingVersion buildNumber sourceCommit sourceTree candidateTag createdAt android ios desktop
]
abort("Candidate manifest has unexpected or missing fields") unless document.keys.sort == expected_root_keys.sort
expected_platform_keys = {
  "android" => %w[packageName signingCertificateSha256 artifactReceiptSha256 internal external],
  "ios" => %w[bundleId appStoreAppId signingIdentitySha1 artifactReceiptSha256 internal external],
  "desktop" => %w[artifactReceiptSha256],
}
expected_platform_keys.each do |platform, expected_keys|
  section = document[platform]
  unless section.is_a?(Hash) && section.keys.sort == expected_keys.sort
    abort("Candidate manifest #{platform} fields are unexpected or incomplete")
  end
end
version = require_string(document, "marketingVersion")
abort("Invalid marketingVersion") unless version.match?(/\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/)
build_number = document["buildNumber"]
unless build_number.is_a?(Integer) && build_number.between?(1_000_000, 2_100_000_000)
  abort("Invalid buildNumber")
end
commit = require_string(document, "sourceCommit")
abort("Invalid sourceCommit") unless commit.match?(/\A[0-9a-f]{40}\z/)
abort("Candidate commit does not match #{expected_commit}") if expected_commit && commit != expected_commit
tag = require_string(document, "candidateTag")
abort("Candidate tag does not match version/build") unless tag == "v#{version}-rc.#{build_number}"
created_at = require_string(document, "createdAt")
begin
  parsed_created_at = Time.iso8601(created_at)
rescue ArgumentError
  abort("Invalid createdAt")
end
abort("createdAt must use UTC") unless parsed_created_at.utc_offset.zero?
unless created_at.match?(/\A\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z\z/) &&
       parsed_created_at.strftime("%Y-%m-%dT%H:%M:%SZ") == created_at
  abort("createdAt must use canonical UTC format")
end

source_tree = require_string(document, "sourceTree")
abort("Invalid sourceTree") unless source_tree.match?(/\A[0-9a-f]{40}\z/)
abort("Candidate tree does not match #{expected_tree}") if expected_tree && source_tree != expected_tree
android_package = require_string(document, "android", "packageName")
unless android_package.match?(/\A[A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)+\z/)
  abort("Invalid android.packageName")
end
abort("Unexpected Android Store identity") unless android_package == "com.passvault.android"
android_fingerprint = require_string(document, "android", "signingCertificateSha256")
abort("Invalid Android signing fingerprint") unless android_fingerprint.match?(/\A[0-9A-F]{64}\z/)
android_receipt = require_string(document, "android", "artifactReceiptSha256")
abort("Invalid Android artifact-receipt digest") unless android_receipt.match?(/\A[0-9a-f]{64}\z/)
ios_bundle_id = require_string(document, "ios", "bundleId")
unless ios_bundle_id.match?(/\A[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+\z/)
  abort("Invalid ios.bundleId")
end
abort("Unexpected iOS Store identity") unless ios_bundle_id == "com.passvault.ios"
app_store_app_id = require_string(document, "ios", "appStoreAppId")
abort("Invalid ios.appStoreAppId") unless app_store_app_id.match?(/\A[1-9]\d{7,14}\z/)
ios_fingerprint = require_string(document, "ios", "signingIdentitySha1")
abort("Invalid iOS signing fingerprint") unless ios_fingerprint.match?(/\A[0-9A-F]{40}\z/)
ios_receipt = require_string(document, "ios", "artifactReceiptSha256")
abort("Invalid iOS artifact-receipt digest") unless ios_receipt.match?(/\A[0-9a-f]{64}\z/)
desktop_receipt = require_string(document, "desktop", "artifactReceiptSha256")
abort("Invalid Desktop artifact-receipt digest") unless desktop_receipt.match?(/\A[0-9a-f]{64}\z/)

required_states = {
  ["android", "internal"] => %w[completed],
  ["android", "external"] => %w[completed],
  ["ios", "internal"] => %w[completed],
  ["ios", "external"] => %w[approved in_beta_testing],
}
required_states[["ios", "external"]] += %w[submitted submitted_for_review] if allow_pending
required_states.each do |(platform, channel), accepted_states|
  state = require_string(document, platform, channel).downcase
  abort("#{platform}.#{channel} is not ready: #{state}") unless accepted_states.include?(state)
end

status = allow_pending ? "valid candidate provenance" : "valid for production"
puts "Candidate #{tag} has #{status} (commit #{commit}, build #{build_number})."
