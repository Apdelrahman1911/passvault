#!/usr/bin/env ruby

require "json"

allow_pending = ARGV.delete("--allow-pending")
path = ARGV.fetch(0) do
  abort("Usage: #{$PROGRAM_NAME} [--allow-pending] <candidate-manifest.json> [expected-commit]")
end
expected_commit = ARGV[1]&.downcase
document = JSON.parse(File.read(path, encoding: "UTF-8"))

def require_string(document, *keys)
  value = keys.reduce(document) { |value_at_key, key| value_at_key.fetch(key) }
  abort("#{keys.join('.')} must be a non-empty string") unless value.is_a?(String) && !value.strip.empty?
  value.strip
end

abort("Unsupported candidate manifest schema") unless document["schemaVersion"] == 1
version = require_string(document, "marketingVersion")
abort("Invalid marketingVersion") unless version.match?(/\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/)
build_number = document["buildNumber"]
abort("Invalid buildNumber") unless build_number.is_a?(Integer) && build_number >= 1_000_000
commit = require_string(document, "sourceCommit").downcase
abort("Invalid sourceCommit") unless commit.match?(/\A[0-9a-f]{40}\z/)
abort("Candidate commit does not match #{expected_commit}") if expected_commit && commit != expected_commit
tag = require_string(document, "candidateTag")
abort("Candidate tag does not match version/build") unless tag == "v#{version}-rc.#{build_number}"

require_string(document, "sourceTree")
require_string(document, "android", "packageName")
android_fingerprint = require_string(document, "android", "signingCertificateSha256").delete(":")
abort("Invalid Android signing fingerprint") unless android_fingerprint.match?(/\A[0-9A-Fa-f]{64}\z/)
require_string(document, "ios", "bundleId")
require_string(document, "ios", "appStoreAppId")
ios_fingerprint = require_string(document, "ios", "signingIdentitySha1").delete(":")
abort("Invalid iOS signing fingerprint") unless ios_fingerprint.match?(/\A[0-9A-Fa-f]{40}\z/)

%w[android ios].each do |platform|
  %w[internal external].each do |channel|
    state = require_string(document, platform, channel)
    ready_states = %w[completed approved in_beta_testing]
    pending_states = %w[submitted submitted_for_review] if allow_pending
    accepted_states = ready_states + Array(pending_states)
    abort("#{platform}.#{channel} is not ready: #{state}") unless accepted_states.include?(state.downcase)
  end
end

status = allow_pending ? "valid candidate provenance" : "valid for production"
puts "Candidate #{tag} has #{status} (commit #{commit}, build #{build_number})."
