#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
reviewed_entitlements="$repository_root/iosApp/iosApp/iosApp.entitlements"

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "Exported iOS artifact verification requires macOS." >&2
    exit 1
fi
if ! command -v python3 >/dev/null 2>&1; then
    echo "python3 is required to verify iOS entitlements." >&2
    exit 1
fi

if [[ $# -ne 8 ]]; then
    echo "Usage: $0 <archive> <ipa> <team-id> <bundle-id> <profile-uuid> <version> <build-number> <link-map>" >&2
    exit 2
fi

if [[ ! -f "$reviewed_entitlements" || -L "$reviewed_entitlements" ]]; then
    echo "The reviewed iOS entitlements file is missing or unsafe." >&2
    exit 1
fi

archive_path="$1"
ipa_path="$2"
expected_team="$3"
expected_bundle="$4"
expected_profile_uuid="$5"
expected_version="$6"
expected_build="$7"
link_map="$8"

case "${EXPORT_COMPLIANCE_STATUS:-}" in
    EXEMPT_APPROVED) expected_encryption=false ;;
    NON_EXEMPT_APPROVED) expected_encryption=true ;;
    *)
        echo "Approved export compliance is required for artifact verification." >&2
        exit 1
        ;;
esac

archive_app="$archive_path/Products/Applications/PassVault.app"
if [[ ! -d "$archive_app" || ! -f "$ipa_path" || ! -f "$link_map" ]]; then
    echo "The signed archive, exported IPA, or link map is missing." >&2
    exit 1
fi

verification_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-ios-export-verify.XXXXXX")"
chmod 700 "$verification_root"
cleanup() {
    if [[ "$verification_root" == "${TMPDIR:-/tmp}"/passvault-ios-export-verify.* &&
        -d "$verification_root" ]]; then
        find "$verification_root" -type f -exec chmod 600 {} + 2>/dev/null || true
        rm -rf -- "$verification_root"
    fi
}
trap cleanup EXIT

unzip -q "$ipa_path" -d "$verification_root/ipa"
ipa_app_count="$(find "$verification_root/ipa/Payload" -mindepth 1 -maxdepth 1 \
    -type d -name '*.app' | wc -l | tr -d ' ')"
if [[ "$ipa_app_count" != 1 ]]; then
    echo "The IPA must contain exactly one application bundle." >&2
    exit 1
fi
ipa_app="$(find "$verification_root/ipa/Payload" -mindepth 1 -maxdepth 1 \
    -type d -name '*.app' -print -quit)"

verify_app() {
    local label="$1"
    local app="$2"
    local plist="$app/Info.plist"
    local embedded_profile="$app/embedded.mobileprovision"
    local profile_plist="$verification_root/$label-profile.plist"
    local entitlements="$verification_root/$label-entitlements.plist"
    local bundle version build encryption profile_uuid
    local get_task_allow beta_reports profile_expiration profile_expiration_epoch

    "$repository_root/scripts/verify-legal-notice-bundle.sh" "$app" >/dev/null
    codesign --verify --deep --strict --verbose=2 "$app" >/dev/null 2>&1
    bundle="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "$plist")"
    version="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleShortVersionString' "$plist")"
    build="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleVersion' "$plist")"
    encryption="$(/usr/libexec/PlistBuddy -c 'Print :ITSAppUsesNonExemptEncryption' "$plist")"
    [[ "$bundle" == "$expected_bundle" ]]
    [[ "$version" == "$expected_version" ]]
    [[ "$build" == "$expected_build" ]]
    [[ "$encryption" == "$expected_encryption" ]]
    if /usr/libexec/PlistBuddy -c 'Print :ITSEncryptionExportComplianceCode' \
        "$plist" >/dev/null 2>&1; then
        echo "$label contains an unexpected export-compliance code." >&2
        exit 1
    fi

    security cms -D -i "$embedded_profile" > "$profile_plist"
    profile_uuid="$(/usr/libexec/PlistBuddy -c 'Print :UUID' "$profile_plist")"
    [[ "$profile_uuid" == "$expected_profile_uuid" ]]
    get_task_allow="$(/usr/libexec/PlistBuddy -c \
        'Print :Entitlements:get-task-allow' "$profile_plist")"
    beta_reports="$(/usr/libexec/PlistBuddy -c \
        'Print :Entitlements:beta-reports-active' "$profile_plist")"
    profile_expiration="$(sed -n '/<key>ExpirationDate<\/key>/{n;s/.*<date>\(.*\)<\/date>.*/\1/p;}' \
        "$profile_plist")"
    profile_expiration_epoch="$(date -j -u -f '%Y-%m-%dT%H:%M:%SZ' \
        "$profile_expiration" '+%s' 2>/dev/null || true)"
    if [[ "$get_task_allow" != false || "$beta_reports" != true ||
        -z "$profile_expiration_epoch" || "$profile_expiration_epoch" -le "$(date -u '+%s')" ]] ||
       /usr/libexec/PlistBuddy -c 'Print :ProvisionedDevices' "$profile_plist" >/dev/null 2>&1 ||
       [[ "$(/usr/libexec/PlistBuddy -c 'Print :ProvisionsAllDevices' \
           "$profile_plist" 2>/dev/null || true)" == true ]]; then
        echo "$label does not contain a current App Store distribution profile." >&2
        exit 1
    fi

    codesign -d --entitlements :- "$app" > "$entitlements" 2>/dev/null
    python3 "$repository_root/scripts/verify-ios-entitlements.py" \
        "$reviewed_entitlements" \
        "$entitlements" \
        "$expected_team" \
        "$expected_bundle" \
        > "$verification_root/$label-normalized-entitlements.json"

    codesign -d --extract-certificates="$verification_root/$label-cert" "$app" >/dev/null 2>&1
    openssl x509 -inform DER -in "$verification_root/$label-cert0" \
        -fingerprint -sha1 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]' \
        > "$verification_root/$label-sha1"
    openssl x509 -inform DER -in "$verification_root/$label-cert0" \
        -fingerprint -sha256 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]' \
        > "$verification_root/$label-sha256"
}

verify_app archive "$archive_app"
verify_app ipa "$ipa_app"

if ! cmp -s \
    "$verification_root/archive-normalized-entitlements.json" \
    "$verification_root/ipa-normalized-entitlements.json"; then
    echo "The archive and exported IPA entitlement sets differ." >&2
    exit 1
fi

cmp -s "$verification_root/archive-sha1" "$verification_root/ipa-sha1"
cmp -s "$verification_root/archive-sha256" "$verification_root/ipa-sha256"

archive_executable="$archive_app/$(/usr/libexec/PlistBuddy -c \
    'Print :CFBundleExecutable' "$archive_app/Info.plist")"
ipa_executable="$ipa_app/$(/usr/libexec/PlistBuddy -c \
    'Print :CFBundleExecutable' "$ipa_app/Info.plist")"
libsodium_live_symbols="$(LC_ALL=C awk '
    /^# Symbols:/ { live = 1; next }
    /^# Dead Stripped Symbols:/ { live = 0 }
    live && /sodium|crypto_aead_xchacha20poly1305|crypto_pwhash_argon2id/ { count++ }
    END { print count + 0 }
' "$link_map")"
if [[ ! "$libsodium_live_symbols" =~ ^[0-9]+$ || "$libsodium_live_symbols" -le 0 ]]; then
    echo "The signed archive link map lacks live libsodium implementation evidence." >&2
    exit 1
fi

# App Store export strips symbol names, so verify that the stripped IPA executable
# is the same Mach-O build whose archive contains the live libsodium implementation.
xcrun dwarfdump --uuid "$archive_executable" | awk '{ print $2, $3 }' \
    > "$verification_root/archive-uuids"
xcrun dwarfdump --uuid "$ipa_executable" | awk '{ print $2, $3 }' \
    > "$verification_root/ipa-uuids"
if [[ ! -s "$verification_root/archive-uuids" ]] ||
   ! cmp -s "$verification_root/archive-uuids" "$verification_root/ipa-uuids"; then
    echo "The archive and exported IPA Mach-O identifiers do not match." >&2
    exit 1
fi

ipa_sha256="$(shasum -a 256 "$ipa_path" | awk '{ print $1 }')"
printf 'IOS_EXPORTED_ARTIFACT=PASS\n'
printf 'MARKETING_VERSION=%s\n' "$expected_version"
printf 'BUILD_NUMBER=%s\n' "$expected_build"
printf 'BUNDLE_ID=%s\n' "$expected_bundle"
printf 'TEAM_ID=%s\n' "$expected_team"
printf 'PROFILE_UUID=%s\n' "$expected_profile_uuid"
printf 'SIGNING_CERTIFICATE_SHA1=%s\n' "$(<"$verification_root/ipa-sha1")"
printf 'SIGNING_CERTIFICATE_SHA256=%s\n' "$(<"$verification_root/ipa-sha256")"
printf 'INFO_PLIST_NON_EXEMPT_ENCRYPTION=%s\n' "$expected_encryption"
printf 'EXPORT_COMPLIANCE_CODE=ABSENT\n'
printf 'MACHO_UUID_MATCH=PASS\n'
printf 'LIBSODIUM_IMPLEMENTATION=PRESENT_IN_VERIFIED_ARCHIVE\n'
printf 'LIBSODIUM_LIVE_LINK_SYMBOLS=%s\n' "$libsodium_live_symbols"
printf 'IPA_SHA256=%s\n' "$ipa_sha256"
