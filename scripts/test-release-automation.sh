#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

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
./scripts/validate-mobile-tester-files.sh \
    "$mobile_fixture/testflight-external-testers.csv" \
    "$mobile_fixture/play-closed-testers.txt" >/dev/null

echo "Release automation tests passed."
