#!/usr/bin/env ruby

require "json"
require "time"

def required(name)
  value = ENV[name]&.strip
  abort("Missing required environment variable: #{name}") if value.nil? || value.empty?
  value
end

version = required("APP_VERSION")
build_number = Integer(required("BUILD_NUMBER"), 10)
source_commit = required("SOURCE_COMMIT").downcase
source_tree = required("SOURCE_TREE").downcase
candidate_tag = required("CANDIDATE_TAG")

semantic_version = /\A(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\z/
abort("APP_VERSION must be semantic versioning without a suffix") unless version.match?(semantic_version)
unless build_number.between?(1_000_000, 2_100_000_000)
  abort("BUILD_NUMBER must be within the reserved Android release range")
end
abort("SOURCE_COMMIT must be a full Git SHA") unless source_commit.match?(/\A[0-9a-f]{40}\z/)
abort("SOURCE_TREE must be a full Git tree SHA") unless source_tree.match?(/\A[0-9a-f]{40}\z/)
expected_tag = "v#{version}-rc.#{build_number}"
abort("CANDIDATE_TAG must match #{expected_tag}") unless candidate_tag == expected_tag

manifest = {
  schemaVersion: 2,
  marketingVersion: version,
  buildNumber: build_number,
  sourceCommit: source_commit,
  sourceTree: source_tree,
  candidateTag: candidate_tag,
  createdAt: Time.now.utc.iso8601,
  android: {
    packageName: required("ANDROID_PACKAGE_NAME"),
    signingCertificateSha256: required("ANDROID_SIGNING_SHA256").delete(":").upcase,
    artifactReceiptSha256: required("ANDROID_ARTIFACT_RECEIPT_SHA256").downcase,
    internal: required("ANDROID_INTERNAL_STATE"),
    external: required("ANDROID_EXTERNAL_STATE"),
  },
  ios: {
    bundleId: required("IOS_BUNDLE_ID"),
    appStoreAppId: required("APP_STORE_APP_ID"),
    signingIdentitySha1: required("IOS_SIGNING_SHA1").delete(":").upcase,
    artifactReceiptSha256: required("IOS_ARTIFACT_RECEIPT_SHA256").downcase,
    internal: required("IOS_INTERNAL_STATE"),
    external: required("IOS_EXTERNAL_STATE"),
  },
}

puts JSON.pretty_generate(manifest)
