#!/usr/bin/env ruby

require "json"

repository_root = File.expand_path("..", __dir__)
record_path = if ARGV.empty?
                File.join(repository_root, "release", "google-play", "app-content.json")
              else
                File.expand_path(ARGV.fetch(0), repository_root)
              end
manifest_path = File.join(repository_root, "app-android", "src", "main", "AndroidManifest.xml")
android_build_path = File.join(repository_root, "app-android", "build.gradle.kts")
privacy_path = File.join(repository_root, "docs", "PRIVACY.md")
security_path = File.join(repository_root, "docs", "SECURITY_MODEL.md")
workflow_path = File.join(repository_root, ".github", "workflows", "mobile-store-release.yml")
fastfile_path = File.join(repository_root, "fastlane", "Fastfile")

def fail_validation(message)
  warn "Google Play readiness validation failed: #{message}"
  exit 1
end

fail_validation("declaration record is missing") unless File.file?(record_path) && !File.symlink?(record_path)
record = JSON.parse(File.read(record_path, encoding: "UTF-8"))
manifest = File.read(manifest_path, encoding: "UTF-8")
android_build = File.read(android_build_path, encoding: "UTF-8")
privacy = File.read(privacy_path, encoding: "UTF-8")
security = File.read(security_path, encoding: "UTF-8")
workflow = File.read(workflow_path, encoding: "UTF-8")
fastfile = File.read(fastfile_path, encoding: "UTF-8")

fail_validation("unexpected schema") unless record.fetch("schemaVersion") == 1
fail_validation("unexpected package") unless record.fetch("packageName") == "com.passvault.android"
fail_validation("Android applicationId mismatch") unless android_build.include?('applicationId = "com.passvault.android"')
fail_validation("Play app must be categorized as an app") unless record.fetch("appType") == "APP"
fail_validation("Play category must be Tools") unless record.fetch("category") == "TOOLS"
fail_validation("required bilingual locales changed") unless record.fetch("locales").sort == %w[ar en-US]

declared_source_permissions = record.fetch("sourcePermissions").sort
declared_packaged_permissions = record.fetch("packagedPermissions").sort
manifest_permissions = manifest.scan(/<uses-permission android:name="([^"]+)"/).flatten.sort
fail_validation("source permission inventory differs from AndroidManifest.xml") unless
  declared_source_permissions == manifest_permissions
fail_validation("source permissions are missing from the packaged inventory") unless
  (declared_source_permissions - declared_packaged_permissions).empty?

expected_inherited_permissions = %w[
  android.permission.ACCESS_NETWORK_STATE
  android.permission.USE_FINGERPRINT
  com.passvault.android.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
]
fail_validation("packaged dependency-permission inventory changed") unless
  (declared_packaged_permissions - declared_source_permissions).sort == expected_inherited_permissions.sort

merged_manifest_path = File.join(
  repository_root,
  "app-android", "build", "intermediates", "merged_manifests", "standardRelease",
  "processStandardReleaseManifest", "AndroidManifest.xml",
)
if File.file?(merged_manifest_path)
  merged_manifest = File.read(merged_manifest_path, encoding: "UTF-8")
  merged_permissions = merged_manifest.scan(/<uses-permission android:name="([^"]+)"/).flatten.sort
  fail_validation("built Standard release permission inventory changed") unless
    merged_permissions == declared_packaged_permissions
end

forbidden_permissions = %w[
  android.permission.ACCESS_COARSE_LOCATION
  android.permission.ACCESS_FINE_LOCATION
  android.permission.AD_ID
  android.permission.INTERNET
  android.permission.MANAGE_EXTERNAL_STORAGE
  android.permission.READ_CONTACTS
  android.permission.READ_MEDIA_IMAGES
  android.permission.READ_MEDIA_VIDEO
  android.permission.READ_PHONE_STATE
  android.permission.RECORD_AUDIO
  com.android.vending.BILLING
]
present_forbidden = forbidden_permissions & declared_packaged_permissions
fail_validation("unexpected sensitive/network permission: #{present_forbidden.join(', ')}") unless present_forbidden.empty?

data_safety = record.fetch("dataSafety")
network = record.fetch("networkBehavior")
monetization = record.fetch("monetization")
declarations = record.fetch("declarations")
audience = record.fetch("audience")
testing = record.fetch("testing")

fail_validation("data collection declaration changed") unless data_safety.fetch("collectsOrSharesRequiredUserData") == false
fail_validation("data types must be empty") unless data_safety.fetch("dataTypes").empty?
fail_validation("PassVault has no remote account") unless data_safety.fetch("allowsAccountCreation") == false
fail_validation("remote account-deletion URL must not be claimed") unless data_safety.fetch("remoteAccountDeletionUrlRequired") == false
fail_validation("ads declaration changed") unless monetization.fetch("containsAds") == false
fail_validation("Advertising ID declaration changed") unless monetization.fetch("usesAdvertisingId") == false
fail_validation("Play Billing declaration changed") unless monetization.fetch("usesPlayBilling") == false
fail_validation("children declaration changed") unless audience.fetch("designedForChildren") == false
fail_validation("financial declaration changed") unless declarations.fetch("financialFeatures") == "NONE"
fail_validation("health declaration changed") unless declarations.fetch("healthFeatures") == "NONE"

network.each do |name, value|
  fail_validation("network behavior #{name} must remain false") unless value == false
end

fail_validation("privacy record no longer states no network collection") unless
  privacy.include?("no account, cloud-sync, advertising, analytics, telemetry, crash-upload")
fail_validation("security model no longer states the no-network boundary") unless
  security.include?("There is no server, account, network sync, telemetry, or analytics boundary")

fail_validation("internal preparation must remain enabled") unless testing.fetch("internalTrackPrepared") == true
fail_validation("closed testing must remain enabled") unless testing.fetch("closedTestingDeferred") == false
fail_validation("open testing must remain disabled") unless testing.fetch("openTestingEnabled") == false
fail_validation("production testing gate must remain enabled") unless testing.fetch("productionRequiresTestingConfirmation") == true
fail_validation("production workflow testing phrase is missing") unless
  workflow.include?("I_CONFIRM_REQUIRED_TESTING_COMPLETED") && fastfile.include?("I_CONFIRM_REQUIRED_TESTING_COMPLETED")
fail_validation("open-testing lane must fail closed") unless
  fastfile.match?(/lane :open do.*?UI\.user_error!/m)

serialized = JSON.generate(record)
fail_validation("placeholder or draft marker found") if serialized.match?(/example\.invalid|\bTODO\b|\bDRAFT\b|CHANGE_ME/i)

puts "Google Play declaration record matches the Android package, permissions, privacy boundary, and release gates."
