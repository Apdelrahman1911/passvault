#!/usr/bin/env bash

set -euo pipefail

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "Signed iOS archive verification requires macOS." >&2
    exit 1
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
private_root="$repository_root/release/private"
values_file="$private_root/values.env"
# shellcheck source=scripts/lib/dotenv.sh
source "$repository_root/scripts/lib/dotenv.sh"
# shellcheck source=scripts/lib/pkcs12-validation.sh
source "$repository_root/scripts/lib/pkcs12-validation.sh"

cd "$repository_root"
if ! git check-ignore -q release/private/values.env || [[ -n "$(git ls-files release/private)" ]]; then
    echo "release/private/ is not safely ignored and untracked." >&2
    exit 1
fi

private_file_is_safe() {
    ruby -I "$repository_root/scripts" -r lib/private_path -e \
        'exit(PassVault::PrivatePath.regular_file_within?(ARGV.fetch(0), ARGV.fetch(1)) ? 0 : 1)' \
        "$1" "$private_root"
}

if ! private_file_is_safe "$values_file"; then
    echo "release/private/values.env is missing or traverses an unsafe path." >&2
    exit 1
fi
passvault_dotenv_load_file "$values_file"
passvault_select_openssl
openssl_binary="$PASSVAULT_OPENSSL_BINARY"

for required_name in IOS_DISTRIBUTION_CERTIFICATE_FILE IOS_DISTRIBUTION_CERTIFICATE_PASSWORD \
    IOS_PROVISIONING_PROFILE_FILE APPLE_TEAM_ID IOS_BUNDLE_ID \
    EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE; do
    if [[ -z "${!required_name:-}" ]]; then
        echo "A required iOS signing input is missing." >&2
        exit 1
    fi
done
if [[ "$IOS_BUNDLE_ID" != com.passvault.ios || ! "$APPLE_TEAM_ID" =~ ^[A-Z0-9]{10}$ ]]; then
    echo "The iOS signing verifier requires the canonical Store bundle and a valid Apple team ID." >&2
    exit 1
fi

certificate_path="$repository_root/$IOS_DISTRIBUTION_CERTIFICATE_FILE"
profile_path="$repository_root/$IOS_PROVISIONING_PROFILE_FILE"
case "$certificate_path" in "$private_root"/*) ;; *) echo "Unsafe certificate path." >&2; exit 1 ;; esac
case "$profile_path" in "$private_root"/*) ;; *) echo "Unsafe profile path." >&2; exit 1 ;; esac
if ! private_file_is_safe "$certificate_path" || ! private_file_is_safe "$profile_path"; then
    echo "The iOS signing certificate or provisioning profile is missing or unsafe." >&2
    exit 1
fi

verification_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-ios-signed-verify.XXXXXX")"
keychain_path="$verification_root/release.keychain-db"
installed_profile=""
installed_profile_by_script=false
original_keychains=()
while IFS= read -r keychain_line; do
    keychain_line="${keychain_line#*\"}"
    keychain_line="${keychain_line%\"*}"
    [[ -n "$keychain_line" ]] && original_keychains+=("$keychain_line")
done < <(security list-keychains -d user)

cleanup() {
    IOS_DISTRIBUTION_CERTIFICATE_PASSWORD=""
    keychain_password=""
    if [[ "$installed_profile_by_script" == true &&
        "$installed_profile" == "$HOME/Library/MobileDevice/Provisioning Profiles/"*.mobileprovision ]]; then
        rm -f -- "$installed_profile"
    fi
    security delete-keychain "$keychain_path" >/dev/null 2>&1 || true
    if (( ${#original_keychains[@]} > 0 )); then
        security list-keychains -d user -s "${original_keychains[@]}" >/dev/null 2>&1 || true
    fi
    if [[ "$verification_root" == "${TMPDIR:-/tmp}"/passvault-ios-signed-verify.* &&
        -d "$verification_root" ]]; then
        find "$verification_root" -type f -exec chmod 600 {} + 2>/dev/null || true
        rm -rf -- "$verification_root"
    fi
}
trap cleanup EXIT
report_failure() {
    local failure_status="$1"
    local failure_line="$2"
    echo "Signed iOS verification failed at script line $failure_line (status $failure_status)." >&2
    exit "$failure_status"
}
trap 'report_failure "$?" "$LINENO"' ERR

export IOS_DISTRIBUTION_CERTIFICATE_PASSWORD
p12_validation_root="$verification_root/pkcs12-validation"
mkdir -m 700 "$p12_validation_root"
passvault_validate_pkcs12 "$openssl_binary" "$certificate_path" \
    IOS_DISTRIBUTION_CERTIFICATE_PASSWORD "$p12_validation_root"
cp "$PASSVAULT_P12_CERTIFICATE_FILE" "$verification_root/distribution.pem"

expected_sha1="$("$openssl_binary" x509 -in "$verification_root/distribution.pem" \
    -fingerprint -sha1 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
expected_sha256="$("$openssl_binary" x509 -in "$verification_root/distribution.pem" \
    -fingerprint -sha256 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
codesign_identity="${expected_sha1//:/}"

profile_plist="$verification_root/profile.plist"
security cms -D -i "$profile_path" > "$profile_plist"
profile_uuid="$(/usr/libexec/PlistBuddy -c 'Print :UUID' "$profile_plist")"
profile_name="$(/usr/libexec/PlistBuddy -c 'Print :Name' "$profile_plist")"
if [[ ! "$profile_uuid" =~ ^[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}$ ||
    -z "$profile_name" || "$profile_name" == *$'\n'* || "$profile_name" == *$'\r'* ]]; then
    echo "The provisioning profile UUID or name is invalid." >&2
    exit 1
fi
profile_directory="$HOME/Library/MobileDevice/Provisioning Profiles"
mkdir -p "$profile_directory"
installed_profile="$profile_directory/$profile_uuid.mobileprovision"
if [[ -e "$installed_profile" ]]; then
    if ! cmp -s "$profile_path" "$installed_profile"; then
        echo "A different installed profile has the same UUID; archive stopped safely." >&2
        exit 1
    fi
else
    cp "$profile_path" "$installed_profile"
    chmod 600 "$installed_profile"
    installed_profile_by_script=true
fi

keychain_password="$("$openssl_binary" rand -hex 32)"
security create-keychain -p "$keychain_password" "$keychain_path"
security set-keychain-settings -lut 21600 "$keychain_path"
security unlock-keychain -p "$keychain_password" "$keychain_path"
security import "$certificate_path" -k "$keychain_path" \
    -P "$IOS_DISTRIBUTION_CERTIFICATE_PASSWORD" -A -t cert -f pkcs12 >/dev/null 2>&1
security set-key-partition-list -S apple-tool:,apple: -s \
    -k "$keychain_password" "$keychain_path" >/dev/null 2>&1
security list-keychains -d user -s "$keychain_path"
if ! security find-identity -v -p codesigning "$keychain_path" |
    awk -v expected="$codesign_identity" '$2 == expected { found = 1 } END { exit !found }'; then
    echo "The expected Apple Distribution identity is not usable in the temporary keychain." >&2
    exit 1
fi
IOS_DISTRIBUTION_CERTIFICATE_PASSWORD=""
unset IOS_DISTRIBUTION_CERTIFICATE_PASSWORD
keychain_password=""

JAVA_HOME="$(/usr/libexec/java_home -v 17)"
export JAVA_HOME
archive_path="$verification_root/PassVault.xcarchive"
build_log="$verification_root/xcodebuild.log"
link_map="$verification_root/PassVault-LinkMap.txt"
version_name="$(awk -F= '$1 == "VERSION_NAME" { print $2 }' version.properties)"
version_code="$(awk -F= '$1 == "VERSION_CODE" { print $2 }' version.properties)"
case "$EXPORT_COMPLIANCE_STATUS" in
    EXEMPT_APPROVED)
        encryption_build_setting=NO
        expected_encryption=false
        ;;
    NON_EXEMPT_APPROVED)
        encryption_build_setting=YES
        expected_encryption=true
        ;;
    *)
        echo "Approved export compliance is required." >&2
        exit 1
        ;;
esac
export EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE
./scripts/validate-ios-export-compliance.sh
if ! xcodebuild archive \
    -project iosApp/iosApp.xcodeproj \
    -scheme PassVault \
    -configuration Release \
    -destination 'generic/platform=iOS' \
    -archivePath "$archive_path" \
    TEAM_ID="$APPLE_TEAM_ID" \
    DEVELOPMENT_TEAM="$APPLE_TEAM_ID" \
    PRODUCT_BUNDLE_IDENTIFIER="$IOS_BUNDLE_ID" \
    MARKETING_VERSION="$version_name" \
    CURRENT_PROJECT_VERSION="$version_code" \
    CODE_SIGN_STYLE=Manual \
    PROVISIONING_PROFILE_SPECIFIER="$profile_name" \
    CODE_SIGN_IDENTITY="$codesign_identity" \
    INFOPLIST_KEY_ITSAppUsesNonExemptEncryption="$encryption_build_setting" \
    LD_GENERATE_MAP_FILE=YES \
    LD_MAP_FILE_PATH="$link_map" \
    > "$build_log" 2>&1; then
    tail -n 80 "$build_log" >&2
    exit 1
fi

app_path="$archive_path/Products/Applications/PassVault.app"
app_plist="$app_path/Info.plist"
"$repository_root/scripts/verify-legal-notice-bundle.sh" "$app_path" >/dev/null
codesign --verify --deep --strict --verbose=2 "$app_path" >/dev/null 2>&1
plist_encryption="$(/usr/libexec/PlistBuddy -c \
    'Print :ITSAppUsesNonExemptEncryption' "$app_plist")"
if [[ "$plist_encryption" != "$expected_encryption" ]]; then
    echo "The archived application does not contain the approved encryption flag." >&2
    exit 1
fi
if /usr/libexec/PlistBuddy -c 'Print :ITSEncryptionExportComplianceCode' \
    "$app_plist" >/dev/null 2>&1; then
    echo "An unexpected export-compliance code is present in the archive." >&2
    exit 1
fi

archive_profile_plist="$verification_root/archive-profile.plist"
security cms -D -i "$app_path/embedded.mobileprovision" > "$archive_profile_plist"
archive_profile_uuid="$(/usr/libexec/PlistBuddy -c 'Print :UUID' "$archive_profile_plist")"
[[ "$archive_profile_uuid" == "$profile_uuid" ]]
/usr/bin/plutil -extract DeveloperCertificates.0 raw -o - "$archive_profile_plist" 2>/dev/null |
    "$openssl_binary" base64 -d -A > "$verification_root/archive-profile-cert.der"
archive_profile_sha1="$("$openssl_binary" x509 -inform DER \
    -in "$verification_root/archive-profile-cert.der" -fingerprint -sha1 -noout |
    sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
archive_profile_sha256="$("$openssl_binary" x509 -inform DER \
    -in "$verification_root/archive-profile-cert.der" -fingerprint -sha256 -noout |
    sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
[[ "$archive_profile_sha1" == "$expected_sha1" && "$archive_profile_sha256" == "$expected_sha256" ]]

codesign -d --extract-certificates="$verification_root/signed-cert" "$app_path" >/dev/null 2>&1
signed_sha1="$("$openssl_binary" x509 -inform DER -in "$verification_root/signed-cert0" \
    -fingerprint -sha1 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
signed_sha256="$("$openssl_binary" x509 -inform DER -in "$verification_root/signed-cert0" \
    -fingerprint -sha256 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
[[ "$signed_sha1" == "$expected_sha1" && "$signed_sha256" == "$expected_sha256" ]]

sodium_symbols="$(find "$app_path" -type f -print0 |
    while IFS= read -r -d '' candidate; do
        if file "$candidate" 2>/dev/null | grep -q 'Mach-O'; then
            nm "$candidate" 2>/dev/null || true
        fi
    done | grep -Ec '(_crypto_|_sodium_)' || true)"
sodium_identifiers="$(find "$app_path" -type f -print0 |
    while IFS= read -r -d '' candidate; do
        if file "$candidate" 2>/dev/null | grep -q 'Mach-O'; then
            strings "$candidate" 2>/dev/null || true
        fi
    done | grep -Ec 'crypto_aead_xchacha20poly1305|crypto_pwhash_argon2id|sodium_init' || true)"
sodium_linked_symbols="$(LC_ALL=C awk '
    /^# Symbols:/ { live = 1; next }
    /^# Dead Stripped Symbols:/ { live = 0 }
    live && /sodium|crypto_aead_xchacha20poly1305|crypto_pwhash_argon2id/ { count++ }
    END { print count + 0 }
' "$link_map")"
if [[ ! "$sodium_linked_symbols" =~ ^[0-9]+$ || "$sodium_linked_symbols" -le 0 ]]; then
    echo "The live link-map symbols lack expected libsodium implementation evidence." >&2
    exit 1
fi

archive_team="$(codesign -dv --verbose=4 "$app_path" 2>&1 |
    awk -F= '/^TeamIdentifier=/{ print $2; exit }')"
[[ "$archive_team" == "$APPLE_TEAM_ID" ]]
app_sha256="$(shasum -a 256 "$app_path/PassVault" | awk '{ print $1 }')"

export_options="$verification_root/ExportOptions.plist"
export_path="$verification_root/export"
/usr/bin/plutil -create xml1 "$export_options"
/usr/libexec/PlistBuddy -c 'Add :method string app-store-connect' "$export_options"
/usr/libexec/PlistBuddy -c 'Add :signingStyle string manual' "$export_options"
/usr/libexec/PlistBuddy -c "Add :teamID string $APPLE_TEAM_ID" "$export_options"
/usr/libexec/PlistBuddy -c 'Add :manageAppVersionAndBuildNumber bool false' "$export_options"
/usr/libexec/PlistBuddy -c 'Add :uploadSymbols bool true' "$export_options"
/usr/libexec/PlistBuddy -c 'Add :provisioningProfiles dict' "$export_options"
/usr/libexec/PlistBuddy -c "Add :provisioningProfiles:$IOS_BUNDLE_ID string $profile_name" \
    "$export_options"
if ! xcodebuild -exportArchive \
    -archivePath "$archive_path" \
    -exportPath "$export_path" \
    -exportOptionsPlist "$export_options" \
    >> "$build_log" 2>&1; then
    tail -n 80 "$build_log" >&2
    exit 1
fi
ipa_count="$(find "$export_path" -maxdepth 1 -type f -name '*.ipa' | wc -l | tr -d ' ')"
if [[ "$ipa_count" != 1 ]]; then
    echo "The signed archive must export exactly one IPA; found $ipa_count." >&2
    exit 1
fi
ipa_path="$(find "$export_path" -maxdepth 1 -type f -name '*.ipa' -print)"
./scripts/verify-ios-exported-artifact.sh \
    "$archive_path" "$ipa_path" "$APPLE_TEAM_ID" "$IOS_BUNDLE_ID" "$profile_uuid" \
    "$version_name" "$version_code" "$link_map"

printf 'SIGNED_IOS_ARCHIVE=PASS\n'
printf 'P12_SHA1=%s\n' "$expected_sha1"
printf 'P12_SHA256=%s\n' "$expected_sha256"
printf 'PROFILE_CERT_SHA1=%s\n' "$archive_profile_sha1"
printf 'PROFILE_CERT_SHA256=%s\n' "$archive_profile_sha256"
printf 'PROFILE_UUID=%s\n' "$archive_profile_uuid"
printf 'TEAM_ID=%s\n' "$archive_team"
printf 'INFO_PLIST_NON_EXEMPT_ENCRYPTION=%s\n' "$plist_encryption"
printf 'EXPORT_COMPLIANCE_CODE=ABSENT\n'
printf 'LIBSODIUM_SYMBOLS=%s\n' "$sodium_symbols"
printf 'LIBSODIUM_MACHO_IDENTIFIERS=%s\n' "$sodium_identifiers"
printf 'LIBSODIUM_LIVE_LINK_SYMBOLS=%s\n' "$sodium_linked_symbols"
printf 'APP_EXECUTABLE_SHA256=%s\n' "$app_sha256"
