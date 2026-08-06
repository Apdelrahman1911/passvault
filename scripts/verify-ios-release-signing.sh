#!/usr/bin/env bash

set -euo pipefail

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "Signed iOS archive verification requires macOS." >&2
    exit 1
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
private_root="$repository_root/release/private"
values_file="$private_root/values.env"

load_values() {
    local line key value
    while IFS= read -r line || [[ -n "$line" ]]; do
        line="${line%$'\r'}"
        [[ -z "$line" || "$line" =~ ^[[:space:]]*# || "$line" != *=* ]] && continue
        key="${line%%=*}"
        value="${line#*=}"
        [[ "$key" =~ ^[A-Z][A-Z0-9_]*$ ]] || continue
        case "$key" in
            PATH|IFS|BASH_ENV|ENV|SHELLOPTS|BASHOPTS|CDPATH|GLOBIGNORE|HOME|PWD|TMPDIR|LD_*|DYLD_*)
                echo "values.env contains an unsafe variable name." >&2
                exit 1
                ;;
        esac
        printf -v "$key" '%s' "$value"
    done < "$values_file"
}

cd "$repository_root"
if ! git check-ignore -q release/private/values.env || [[ -n "$(git ls-files release/private)" ]]; then
    echo "release/private/ is not safely ignored and untracked." >&2
    exit 1
fi

./scripts/validate-private-release-config.sh >/dev/null
load_values

for required_name in IOS_DISTRIBUTION_CERTIFICATE_FILE IOS_DISTRIBUTION_CERTIFICATE_PASSWORD \
    IOS_PROVISIONING_PROFILE_FILE APPLE_TEAM_ID IOS_BUNDLE_ID; do
    if [[ -z "${!required_name:-}" ]]; then
        echo "A required iOS signing input is missing." >&2
        exit 1
    fi
done

certificate_path="$repository_root/$IOS_DISTRIBUTION_CERTIFICATE_FILE"
profile_path="$repository_root/$IOS_PROVISIONING_PROFILE_FILE"
case "$certificate_path" in "$private_root"/*) ;; *) echo "Unsafe certificate path." >&2; exit 1 ;; esac
case "$profile_path" in "$private_root"/*) ;; *) echo "Unsafe profile path." >&2; exit 1 ;; esac

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
trap 'status=$?; echo "Signed iOS verification failed at script line $LINENO (status $status)." >&2; exit "$status"' ERR

export IOS_DISTRIBUTION_CERTIFICATE_PASSWORD
/usr/bin/openssl pkcs12 -in "$certificate_path" -clcerts -nokeys \
    -passin env:IOS_DISTRIBUTION_CERTIFICATE_PASSWORD \
    -out "$verification_root/distribution.pem" >/dev/null 2>&1
/usr/bin/openssl pkcs12 -in "$certificate_path" -nocerts -nodes \
    -passin env:IOS_DISTRIBUTION_CERTIFICATE_PASSWORD 2>/dev/null |
    /usr/bin/openssl pkey -pubout -out "$verification_root/private-public.pem" >/dev/null 2>&1
/usr/bin/openssl x509 -in "$verification_root/distribution.pem" -pubkey -noout \
    > "$verification_root/certificate-public.pem"
cmp -s "$verification_root/private-public.pem" "$verification_root/certificate-public.pem"

expected_sha1="$(/usr/bin/openssl x509 -in "$verification_root/distribution.pem" \
    -fingerprint -sha1 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
expected_sha256="$(/usr/bin/openssl x509 -in "$verification_root/distribution.pem" \
    -fingerprint -sha256 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
codesign_identity="${expected_sha1//:/}"

profile_plist="$verification_root/profile.plist"
security cms -D -i "$profile_path" > "$profile_plist"
profile_uuid="$(/usr/libexec/PlistBuddy -c 'Print :UUID' "$profile_plist")"
profile_name="$(/usr/libexec/PlistBuddy -c 'Print :Name' "$profile_plist")"
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

keychain_password="$(/usr/bin/openssl rand -hex 32)"
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

export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
archive_path="$verification_root/PassVault.xcarchive"
build_log="$verification_root/xcodebuild.log"
link_map="$verification_root/PassVault-LinkMap.txt"
version_name="$(awk -F= '$1 == "VERSION_NAME" { print $2 }' version.properties)"
version_code="$(awk -F= '$1 == "VERSION_CODE" { print $2 }' version.properties)"
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
    INFOPLIST_KEY_ITSAppUsesNonExemptEncryption=NO \
    LD_GENERATE_MAP_FILE=YES \
    LD_MAP_FILE_PATH="$link_map" \
    > "$build_log" 2>&1; then
    tail -n 80 "$build_log" >&2
    exit 1
fi

app_path="$archive_path/Products/Applications/PassVault.app"
app_plist="$app_path/Info.plist"
codesign --verify --deep --strict --verbose=2 "$app_path" >/dev/null 2>&1
plist_encryption="$(/usr/libexec/PlistBuddy -c \
    'Print :ITSAppUsesNonExemptEncryption' "$app_plist")"
if [[ "$plist_encryption" != false ]]; then
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
    /usr/bin/openssl base64 -d -A > "$verification_root/archive-profile-cert.der"
archive_profile_sha1="$(/usr/bin/openssl x509 -inform DER \
    -in "$verification_root/archive-profile-cert.der" -fingerprint -sha1 -noout |
    sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
archive_profile_sha256="$(/usr/bin/openssl x509 -inform DER \
    -in "$verification_root/archive-profile-cert.der" -fingerprint -sha256 -noout |
    sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
[[ "$archive_profile_sha1" == "$expected_sha1" && "$archive_profile_sha256" == "$expected_sha256" ]]

codesign -d --extract-certificates="$verification_root/signed-cert" "$app_path" >/dev/null 2>&1
signed_sha1="$(/usr/bin/openssl x509 -inform DER -in "$verification_root/signed-cert0" \
    -fingerprint -sha1 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
signed_sha256="$(/usr/bin/openssl x509 -inform DER -in "$verification_root/signed-cert0" \
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
