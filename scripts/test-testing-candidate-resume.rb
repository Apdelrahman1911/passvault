#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require_relative "lib/testing_candidate_resume"

failures = []
def check(failures, message)
  return if yield

  failures << message
end

version = "1.0.6"
build = "1015001"
commit_a = "852a765f4970a4bfe0171d3cf5c435052e742701"
commit_b = "5316377df67cff6b46a4b611c71465959efa9820"
tree = "49b2ddde937e78b50cc9ede42907579e8b8e8fb9"

android_receipt = {
  "schemaVersion" => 1,
  "platform" => "android",
  "marketingVersion" => version,
  "buildNumber" => 1_015_001,
  "sourceCommit" => commit_a,
  "sourceTree" => tree,
  "createdAt" => "2026-09-01T02:55:52Z",
  "identifier" => "com.passvault.android",
  "signingFingerprint" => "7D4D1120B1D19F5BB942E06C600F2B6481E5E682475223FA4E7DC7B27CDB1037",
  "artifacts" => [
    { "kind" => "aab", "fileName" => "PassVault-1.0.6-1015001.aab", "sizeBytes" => 10, "sha256" => "a" * 64 },
    { "kind" => "apk", "fileName" => "PassVault-1.0.6-1015001.apk", "sizeBytes" => 11, "sha256" => "b" * 64 },
    { "kind" => "r8-mapping", "fileName" => "r8-mapping.txt", "sizeBytes" => 12, "sha256" => "c" * 64 },
  ],
}
ios_receipt = {
  "schemaVersion" => 1,
  "platform" => "ios",
  "marketingVersion" => version,
  "buildNumber" => 1_015_001,
  "sourceCommit" => commit_a,
  "sourceTree" => tree,
  "createdAt" => "2026-09-01T03:38:22Z",
  "identifier" => "com.passvault.ios",
  "signingFingerprint" => "4215337727F71EB97A737D480F3989D65056D243",
  "artifacts" => [
    { "kind" => "ipa", "fileName" => "PassVault-1.0.6-1015001.ipa", "sizeBytes" => 13, "sha256" => "d" * 64 },
    { "kind" => "xcarchive", "fileName" => "PassVault-1.0.6-1015001.xcarchive.zip", "sizeBytes" => 14, "sha256" => "e" * 64 },
    { "kind" => "link-map", "fileName" => "PassVault-LinkMap.txt", "sizeBytes" => 15, "sha256" => "f" * 64 },
  ],
}

check(failures, "confirmation must match version and build") do
  PassVault::TestingCandidateResume.validate_confirmation!(
    "resume:1.0.6:1015001",
    version: version,
    build_number: build,
  )
  true
rescue RuntimeError
  false
end

check(failures, "wrong confirmation must be rejected") do
  PassVault::TestingCandidateResume.validate_confirmation!(
    "resume:1.0.6:1015002",
    version: version,
    build_number: build,
  )
  false
rescue RuntimeError => error
  error.message.include?("resume:1.0.6:1015001")
end

check(failures, "blank confirmation must be rejected") do
  PassVault::TestingCandidateResume.validate_confirmation!(
    "",
    version: version,
    build_number: build,
  )
  false
rescue RuntimeError
  true
end

successful_android_run = {
  "id" => 33_463_801_066,
  "head_sha" => commit_a,
  "jobs" => [
    { "name" => "mobile-internal / Android internal", "conclusion" => "success" },
    { "name" => "mobile-internal / iOS internal", "conclusion" => "failure" },
  ],
  "artifacts" => [
    { "id" => 1, "name" => "mobile-receipt-android-1015001" },
    { "id" => 11, "name" => "mobile-signed-android-1015001" },
  ],
}
successful_ios_run = {
  "id" => 33_465_901_191,
  "head_sha" => commit_a,
  "jobs" => [
    { "name" => "mobile-internal / Android internal", "conclusion" => "failure" },
    { "name" => "mobile-internal / iOS internal", "conclusion" => "success" },
  ],
  "artifacts" => [
    { "id" => 4, "name" => "mobile-receipt-ios-1015001" },
    { "id" => 14, "name" => "mobile-signed-ios-1015001" },
  ],
}

selected = PassVault::TestingCandidateResume.select_receipt_sources(
  [successful_android_run, successful_ios_run],
  version: version,
  build_number: build,
  source_commit: commit_a,
)
check(failures, "failed Android upload receipt must be ignored") do
  selected.fetch("android").map { |candidate| candidate.fetch("run_id") } == ["33463801066"]
end
check(failures, "failed iOS archive receipt must be ignored") do
  selected.fetch("ios").map { |candidate| candidate.fetch("run_id") } == ["33465901191"]
end

receipt_only_run = {
  "id" => 42,
  "head_sha" => commit_a,
  "jobs" => [
    { "name" => "mobile-internal / Android internal", "conclusion" => "success" },
  ],
  "artifacts" => [
    { "id" => 5, "name" => "mobile-receipt-android-1015001" },
  ],
}
begin
  PassVault::TestingCandidateResume.select_receipt_sources(
    [receipt_only_run],
    version: version,
    build_number: build,
    source_commit: commit_a,
  )
  failures << "a successful Android job without its signed artifact must be rejected"
rescue RuntimeError => error
  unless error.message.include?("signed artifact bundle")
    failures << "missing signed-artifact error was unclear: #{error.message}"
  end
end

older_build_run = {
  "id" => 31_825_750_974,
  "head_sha" => "c109a33b0123456789abcdef0123456789abcdef",
  "jobs" => [
    { "name" => "mobile-internal / Android internal", "conclusion" => "success" },
    { "name" => "mobile-internal / iOS internal", "conclusion" => "success" },
  ],
  "artifacts" => [
    { "id" => 61, "name" => "mobile-receipt-android-1013001" },
    { "id" => 62, "name" => "mobile-signed-android-1013001" },
    { "id" => 63, "name" => "mobile-receipt-ios-1013001" },
    { "id" => 64, "name" => "mobile-signed-ios-1013001" },
  ],
}
older = PassVault::TestingCandidateResume.select_receipt_sources(
  [older_build_run, successful_android_run, successful_ios_run],
  version: version,
  build_number: build,
)
check(failures, "successful jobs from another VERSION_CODE must be ignored") do
  older.fetch("android").map { |candidate| candidate.fetch("run_id") } == ["33463801066"] &&
    older.fetch("ios").map { |candidate| candidate.fetch("run_id") } == ["33465901191"]
end

wrong_commit_run = successful_android_run.merge("head_sha" => commit_b)
ignored = PassVault::TestingCandidateResume.select_receipt_sources(
  [wrong_commit_run],
  version: version,
  build_number: build,
  source_commit: commit_a,
)
check(failures, "receipts from another commit must be ignored") do
  ignored.fetch("android").empty? && ignored.fetch("ios").empty?
end

same_tree_later_run = {
  "id" => 55,
  "head_sha" => "f814e567a249926b7e7ce1d2f7acddaa551735e4",
  "head_tree" => tree,
  "jobs" => [
    { "name" => "mobile-internal / Android internal", "conclusion" => "success" },
  ],
  "artifacts" => [
    { "id" => 41, "name" => "mobile-receipt-android-1015001" },
    { "id" => 42, "name" => "mobile-signed-android-1015001" },
  ],
}
same_tree_selected = PassVault::TestingCandidateResume.select_receipt_sources(
  [same_tree_later_run],
  version: version,
  build_number: build,
  source_commit: commit_a,
  source_tree: tree,
)
check(failures, "a later testing SHA with the original tree may be searched") do
  same_tree_selected.fetch("android").map { |candidate| candidate.fetch("run_id") } == ["55"]
end

different_tree_run = successful_android_run.merge(
  "id" => 56,
  "head_sha" => "f814e567a249926b7e7ce1d2f7acddaa551735e4",
  "head_tree" => "71819213325c80ab76af594a83d50aba4bcf3385",
)
different_tree_selected = PassVault::TestingCandidateResume.select_receipt_sources(
  [different_tree_run],
  version: version,
  build_number: build,
  source_commit: commit_a,
  source_tree: tree,
)
check(failures, "a later testing SHA with a different tree must be ignored") do
  different_tree_selected.fetch("android").empty?
end

resolved = PassVault::TestingCandidateResume.resolve_original_candidate([android_receipt, ios_receipt])
check(failures, "resumed receipts must resolve to the original candidate commit and tree") do
  resolved == { "sourceCommit" => commit_a, "sourceTree" => tree }
end

begin
  PassVault::TestingCandidateResume.resolve_original_candidate(
    [android_receipt, ios_receipt.merge("sourceCommit" => commit_b)],
  )
  failures << "receipts from two original commits must be rejected"
rescue RuntimeError
  true
end

chosen = PassVault::TestingCandidateResume.choose_unique_sources(
  selected,
  {
    "android:33463801066:mobile-receipt-android-1015001" => android_receipt,
    "ios:33465901191:mobile-receipt-ios-1015001" => ios_receipt,
  },
)
check(failures, "unique successful receipts must be chosen") do
  chosen.fetch("android").fetch("run_id") == "33463801066" &&
    chosen.fetch("ios").fetch("run_id") == "33465901191"
end

duplicate_android_run = successful_android_run.merge(
  "id" => 99,
  "artifacts" => [
    { "id" => 21, "name" => "mobile-receipt-android-1015001" },
    { "id" => 22, "name" => "mobile-signed-android-1015001" },
  ],
)
conflicting_receipt = Marshal.load(Marshal.dump(android_receipt))
conflicting_receipt["artifacts"][0]["sha256"] = "1" * 64
begin
  PassVault::TestingCandidateResume.choose_unique_sources(
    PassVault::TestingCandidateResume.select_receipt_sources(
      [successful_android_run, duplicate_android_run, successful_ios_run],
      version: version,
      build_number: build,
      source_commit: commit_a,
    ),
    {
      "android:33463801066:mobile-receipt-android-1015001" => android_receipt,
      "android:99:mobile-receipt-android-1015001" => conflicting_receipt,
      "ios:33465901191:mobile-receipt-ios-1015001" => ios_receipt,
    },
  )
  failures << "conflicting successful Android receipts must be rejected"
rescue RuntimeError => error
  unless error.message.include?("Conflicting successful android receipts")
    failures << "conflict error should name the android receipts: #{error.message}"
  end
end

identical_duplicate = {
  "id" => 33_463_809_999,
  "head_sha" => commit_a,
  "jobs" => [
    { "name" => "mobile-internal / Android internal", "conclusion" => "success" },
  ],
  "artifacts" => [
    { "id" => 31, "name" => "mobile-receipt-android-1015001" },
    { "id" => 32, "name" => "mobile-signed-android-1015001" },
  ],
}
begin
  identical_choice = PassVault::TestingCandidateResume.choose_unique_sources(
    PassVault::TestingCandidateResume.select_receipt_sources(
      [successful_android_run, identical_duplicate, successful_ios_run],
      version: version,
      build_number: build,
      source_commit: commit_a,
    ),
    {
      "android:33463801066:mobile-receipt-android-1015001" => android_receipt,
      "android:33463809999:mobile-receipt-android-1015001" => android_receipt,
      "ios:33465901191:mobile-receipt-ios-1015001" => ios_receipt,
    },
  )
  check(failures, "identical successful receipts must keep the earliest run") do
    identical_choice.fetch("android").fetch("run_id") == "33463801066"
  end
rescue RuntimeError => error
  failures << "identical successful receipts raised: #{error.message}"
end

begin
  PassVault::TestingCandidateResume.choose_unique_sources(
    { "android" => [], "ios" => selected.fetch("ios") },
    { "ios:33465901191:mobile-receipt-ios-1015001" => ios_receipt },
  )
  failures << "missing Android receipts must be rejected"
rescue RuntimeError => error
  unless error.message.include?("No successful android")
    failures << "missing Android error was unclear: #{error.message}"
  end
end

begin
  PassVault::TestingCandidateResume.bind_receipt!(
    android_receipt.merge("sourceCommit" => commit_b),
    platform: "android",
    version: version,
    build_number: build,
    source_commit: commit_a,
    source_tree: tree,
  )
  failures << "receipt bound to another commit must be rejected"
rescue RuntimeError
  true
end

begin
  PassVault::TestingCandidateResume.bind_receipt!(
    android_receipt.merge("buildNumber" => 1_015_002),
    platform: "android",
    version: version,
    build_number: build,
    source_commit: commit_a,
    source_tree: tree,
  )
  failures << "receipt bound to another build must be rejected"
rescue RuntimeError
  true
end

if failures.empty?
  puts "Testing candidate resume selection is fail-closed."
  exit 0
end

warn(failures.join("\n"))
exit 1
