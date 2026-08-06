#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

./scripts/test-private-release-validator.sh >/dev/null

export PUBLISHER_NAME="PassVault test publisher"
export COPYRIGHT_HOLDER="PassVault test contributors"
export SUPPORT_EMAIL="support@passvault.test"
export SECURITY_EMAIL="security@passvault.test"
export PRIVACY_POLICY_URL="https://passvault.test/privacy"
export SUPPORT_URL="https://passvault.test/support"
export PROJECT_URL="https://passvault.test/project"

version="$(awk -F= '$1 == "VERSION_NAME" { print $2 }' version.properties)"

test "$(tr -d '\r\n' < release/android/passvault-upload-alias.txt)" = "passvault-upload"
expected_android_fingerprint="$(tr -cd '0-9A-Fa-f' < release/android/passvault-release-cert.sha256 | \
    tr '[:lower:]' '[:upper:]')"
test "$expected_android_fingerprint" = \
    "7D4D1120B1D19F5BB942E06C600F2B6481E5E682475223FA4E7DC7B27CDB1037"
actual_android_fingerprint="$(keytool -printcert -file release/android/passvault-release-cert.pem 2>/dev/null | \
    awk -F'SHA256:' '/SHA256:/ { gsub(/[^0-9A-Fa-f]/, "", $2); print toupper($2); exit }')"
test "$actual_android_fingerprint" = "$expected_android_fingerprint"
wrong_version="0.0.0"
if [[ "$version" == "$wrong_version" ]]; then
    wrong_version="9.9.9"
fi

./scripts/validate-release-metadata.sh "$version" >/dev/null

if ./scripts/validate-release-metadata.sh "$wrong_version" >/dev/null 2>&1; then
    echo "Mismatched release version was accepted." >&2
    exit 1
fi

temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-release-test.XXXXXX")"
cleanup() {
    case "$temporary_root" in
        "${TMPDIR:-/tmp}"/passvault-release-test.*)
            rm -rf -- "$temporary_root"
            ;;
        *)
            echo "Refusing to remove unexpected test path: $temporary_root" >&2
            ;;
    esac
}
trap cleanup EXIT

play_csv_header='Email,Status,Expiration time,Invitation time,App permissions,Account permissions'
play_beta_email='beta-validator@passvault-test.iam.gserviceaccount.com'
play_production_email='production-validator@passvault-test.iam.gserviceaccount.com'
play_strict_csv="$temporary_root/play-strict.csv"
play_admin_csv="$temporary_root/play-admin.csv"
play_global_csv="$temporary_root/play-global.csv"
play_other_app_csv="$temporary_root/play-other-app.csv"
play_admin_permissions='CAN_VIEW_FINANCIAL_DATA;CAN_MANAGE_PERMISSIONS;CAN_REPLY_TO_REVIEWS;CAN_MANAGE_PUBLIC_APKS;CAN_MANAGE_TRACK_APKS;CAN_MANAGE_TRACK_USERS;CAN_MANAGE_PUBLIC_LISTING;CAN_MANAGE_DRAFT_APPS;CAN_MANAGE_ORDERS;CAN_MANAGE_APP_CONTENT;CAN_VIEW_NON_FINANCIAL_DATA;CAN_VIEW_APP_QUALITY;CAN_MANAGE_DEEPLINKS'
{
    printf '%s\n' "$play_csv_header"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:CAN_MANAGE_TRACK_APKS;CAN_VIEW_NON_FINANCIAL_DATA},\n' \
        "$play_beta_email"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:CAN_MANAGE_PUBLIC_APKS;CAN_MANAGE_PUBLIC_LISTING;CAN_VIEW_NON_FINANCIAL_DATA},\n' \
        "$play_production_email"
} > "$play_strict_csv"
{
    printf '%s\n' "$play_csv_header"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:%s},\n' "$play_beta_email" "$play_admin_permissions"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:%s},\n' "$play_production_email" "$play_admin_permissions"
} > "$play_admin_csv"
{
    printf '%s\n' "$play_csv_header"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:%s},CAN_MANAGE_PUBLIC_APKS_GLOBAL\n' \
        "$play_beta_email" "$play_admin_permissions"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:%s},\n' "$play_production_email" "$play_admin_permissions"
} > "$play_global_csv"
{
    printf '%s\n' "$play_csv_header"
    printf '%s,ACCESS_GRANTED,,,{com.other.app:%s},\n' "$play_beta_email" "$play_admin_permissions"
    printf '%s,ACCESS_GRANTED,,,{com.passvault.android:%s},\n' "$play_production_email" "$play_admin_permissions"
} > "$play_other_app_csv"

play_validator_arguments=(
    --package com.passvault.android
    --beta-email "$play_beta_email"
    --production-email "$play_production_email"
)
ruby ./scripts/validate-play-service-accounts.rb --csv "$play_strict_csv" \
    "${play_validator_arguments[@]}" --mode STRICT_LEAST_PRIVILEGE >/dev/null
ruby ./scripts/validate-play-service-accounts.rb --csv "$play_admin_csv" \
    "${play_validator_arguments[@]}" --mode PASSVAULT_APP_ADMIN_ACCEPTED >/dev/null
if ruby ./scripts/validate-play-service-accounts.rb --csv "$play_admin_csv" \
    "${play_validator_arguments[@]}" --mode STRICT_LEAST_PRIVILEGE >/dev/null 2>&1; then
    echo "App-level Admin was accepted without the explicit security-exception mode." >&2
    exit 1
fi
if ruby ./scripts/validate-play-service-accounts.rb --csv "$play_global_csv" \
    "${play_validator_arguments[@]}" --mode PASSVAULT_APP_ADMIN_ACCEPTED >/dev/null 2>&1; then
    echo "Account/global Play permissions were accepted by the app-Admin exception." >&2
    exit 1
fi
if ruby ./scripts/validate-play-service-accounts.rb --csv "$play_other_app_csv" \
    "${play_validator_arguments[@]}" --mode PASSVAULT_APP_ADMIN_ACCEPTED >/dev/null 2>&1; then
    echo "Play access to another app was accepted by the PassVault-only exception." >&2
    exit 1
fi

valid_asc_key="$temporary_root/valid-asc-key.p8"
traditional_asc_key="$temporary_root/valid-asc-key-legacy.pem"
invalid_asc_key="$temporary_root/invalid-asc-key.p8"
ruby -ropenssl -e '
  key = OpenSSL::PKey::EC.generate("prime256v1")
  File.binwrite(ARGV.fetch(0), key.to_pem)
' "$traditional_asc_key"
openssl pkcs8 -topk8 -nocrypt -in "$traditional_asc_key" -out "$valid_asc_key"
printf '%s\n' '-----BEGIN PRIVATE KEY-----' 'invalid' '-----END PRIVATE KEY-----' > "$invalid_asc_key"
./scripts/validate-app-store-connect-key.rb "$valid_asc_key" >/dev/null
if ./scripts/validate-app-store-connect-key.rb "$invalid_asc_key" >/dev/null 2>&1; then
    echo "Invalid App Store Connect key was accepted." >&2
    exit 1
fi

input_directory="$temporary_root/input"
output_directory="$temporary_root/output"
mkdir -p "$input_directory"

for extension in apk aab exe msi dmg deb rpm; do
    printf 'PassVault %s test artifact\n' "$extension" >"$input_directory/PassVault-$version.$extension"
done

GITHUB_SHA=0123456789abcdef \
    ./scripts/prepare-release-assets.sh \
    "$input_directory" \
    "$output_directory" \
    "$version" >/dev/null

if [[ "$(find "$output_directory" -maxdepth 1 -type f | wc -l | tr -d ' ')" != "9" ]]; then
    echo "Unexpected number of consolidated release files." >&2
    exit 1
fi

grep -Fq '0123456789abcdef' "$output_directory/RELEASE-MANIFEST.txt"
(
    cd "$output_directory"
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum --check SHA256SUMS.txt >/dev/null
    else
        shasum -a 256 --check SHA256SUMS.txt >/dev/null
    fi
)

rm -f "$input_directory/PassVault-$version.rpm"
if ./scripts/prepare-release-assets.sh \
    "$input_directory" \
    "$temporary_root/missing-output" \
    "$version" >/dev/null 2>&1; then
    echo "Missing required release artifact was accepted." >&2
    exit 1
fi

mobile_fixture="$repository_root/scripts/testdata/mobile-store"
mobile_output="$temporary_root/mobile-store"
./scripts/prepare-mobile-store-metadata.sh \
    "$mobile_fixture" "$mobile_output" "123" >/dev/null
test -s "$mobile_output/android/en-US/changelogs/123.txt"
test -s "$mobile_output/android/ar/full_description.txt"
test -s "$mobile_output/ios/en-US/description.txt"
test -s "$mobile_output/ios/ar-SA/release_notes.txt"
test -s "$mobile_output/ios/en-US/support_url.txt"
test -s "$mobile_output/ios/copyright.txt"
test -s "$mobile_output/testflight/en-US/what_to_test.txt"

overlong_description_fixture="$temporary_root/overlong-description"
cp -R "$mobile_fixture" "$overlong_description_fixture"
awk 'BEGIN { print "# Description"; print ""; for (i = 0; i < 4001; i++) printf "x"; print "" }' \
    > "$overlong_description_fixture/store-description-en.md"
if ./scripts/prepare-mobile-store-metadata.sh \
    "$overlong_description_fixture" "$temporary_root/overlong-description-output" "123" \
    >/dev/null 2>&1; then
    echo "An overlong store description was accepted." >&2
    exit 1
fi

overlong_notes_fixture="$temporary_root/overlong-notes"
cp -R "$mobile_fixture" "$overlong_notes_fixture"
awk 'BEGIN { print "# Release notes"; print ""; for (i = 0; i < 501; i++) printf "x"; print "" }' \
    > "$overlong_notes_fixture/release-notes-en.md"
if ./scripts/prepare-mobile-store-metadata.sh \
    "$overlong_notes_fixture" "$temporary_root/overlong-notes-output" "123" \
    >/dev/null 2>&1; then
    echo "Google Play release notes over 500 characters were accepted." >&2
    exit 1
fi

./scripts/validate-mobile-tester-files.sh \
    testflight "$mobile_fixture/testflight-external-testers.csv" >/dev/null
./scripts/validate-mobile-tester-files.sh \
    play "$mobile_fixture/play-closed-testers.txt" >/dev/null

empty_testers="$temporary_root/empty-testers.txt"
placeholder_testers="$temporary_root/placeholder-testers.txt"
empty_testflight="$temporary_root/empty-testflight.csv"
placeholder_testflight="$temporary_root/placeholder-testflight.csv"
: > "$empty_testers"
printf '%s\n' 'tester@example.invalid' > "$placeholder_testers"
printf '%s\n' 'first_name,last_name,email' > "$empty_testflight"
printf '%s\n' 'first_name,last_name,email' 'Test,User,tester@example.invalid' > "$placeholder_testflight"
if ./scripts/validate-mobile-tester-files.sh play "$empty_testers" >/dev/null 2>&1; then
    echo "An empty Play tester list was accepted for distribution." >&2
    exit 1
fi
if ./scripts/validate-mobile-tester-files.sh play "$placeholder_testers" >/dev/null 2>&1; then
    echo "A placeholder Play tester list was accepted for distribution." >&2
    exit 1
fi
if ./scripts/validate-mobile-tester-files.sh testflight "$empty_testflight" >/dev/null 2>&1; then
    echo "A header-only TestFlight tester list was accepted for distribution." >&2
    exit 1
fi
if ./scripts/validate-mobile-tester-files.sh testflight "$placeholder_testflight" >/dev/null 2>&1; then
    echo "A placeholder TestFlight tester list was accepted for distribution." >&2
    exit 1
fi

exempt_plist="$temporary_root/exempt-Info.plist"
non_exempt_plist="$temporary_root/non-exempt-Info.plist"
invented_code_plist="$temporary_root/invented-code-Info.plist"
printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<plist version="1.0"><dict>' \
    '<key>ITSAppUsesNonExemptEncryption</key><false/>' \
    '</dict></plist>' > "$exempt_plist"
printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<plist version="1.0"><dict>' \
    '<key>ITSAppUsesNonExemptEncryption</key><true/>' \
    '</dict></plist>' > "$non_exempt_plist"
printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<plist version="1.0"><dict>' \
    '<key>ITSAppUsesNonExemptEncryption</key><false/>' \
    '<key>ITSEncryptionExportComplianceCode</key><string>invented-code</string>' \
    '</dict></plist>' > "$invented_code_plist"

EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED IOS_FRANCE_AVAILABLE=false \
    ./scripts/validate-ios-export-compliance.sh "$exempt_plist" >/dev/null
EXPORT_COMPLIANCE_STATUS=NON_EXEMPT_APPROVED IOS_FRANCE_AVAILABLE=true \
    ./scripts/validate-ios-export-compliance.sh "$non_exempt_plist" >/dev/null
if EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED IOS_FRANCE_AVAILABLE=true \
    ./scripts/validate-ios-export-compliance.sh "$exempt_plist" >/dev/null 2>&1; then
    echo "France was allowed with the no-documentation export status." >&2
    exit 1
fi
if EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED IOS_FRANCE_AVAILABLE=false \
    ./scripts/validate-ios-export-compliance.sh "$non_exempt_plist" >/dev/null 2>&1; then
    echo "A non-exempt plist was accepted for the approved no-documentation state." >&2
    exit 1
fi
if EXPORT_COMPLIANCE_STATUS=EXEMPT_APPROVED IOS_FRANCE_AVAILABLE=false \
    ./scripts/validate-ios-export-compliance.sh "$invented_code_plist" >/dev/null 2>&1; then
    echo "An invented Apple export-compliance code was accepted." >&2
    exit 1
fi

grep -Fq 'I_CONFIRM_REQUIRED_TESTING_COMPLETED' .github/workflows/mobile-store-release.yml
grep -Fq 'I_CONFIRM_REQUIRED_TESTING_COMPLETED' fastlane/Fastfile
grep -Fq 'lane :open do' fastlane/Fastfile
grep -Fq 'INFOPLIST_KEY_ITSAppUsesNonExemptEncryption = NO;' \
    iosApp/iosApp.xcodeproj/project.pbxproj
grep -Fq 'Enforce App Store France availability constraint' \
    .github/workflows/mobile-store-release.yml
grep -Fq 'IOS_FRANCE_AVAILABLE' fastlane/Fastfile
bash -n scripts/verify-ios-release-signing.sh
ruby -c scripts/validate-play-service-accounts.rb >/dev/null
bash -n scripts/verify-play-oidc-access.sh
grep -Fq 'VERIFY_ONLY_NO_UPLOAD' .github/workflows/play-access-check.yml
grep -Fq 'PLAY_EDIT_DELETED=PASS' scripts/verify-play-oidc-access.sh
grep -Fq 'api_request DELETE' scripts/verify-play-oidc-access.sh
if grep -Eiq '(edits/.*/commit|/bundles|upload_to_play_store|fastlane)' \
    scripts/verify-play-oidc-access.sh .github/workflows/play-access-check.yml; then
    echo "The Play access check contains a store upload or edit-commit path." >&2
    exit 1
fi
ruby -c scripts/configure-app-store-connect-beta.rb >/dev/null
grep -Fq 'INFO_PLIST_NON_EXEMPT_ENCRYPTION=%s' scripts/verify-ios-release-signing.sh
grep -Fq 'EXPORT_COMPLIANCE_CODE=ABSENT' scripts/verify-ios-release-signing.sh

echo "Release automation tests passed."
