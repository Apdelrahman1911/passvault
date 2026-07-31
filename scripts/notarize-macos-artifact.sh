#!/usr/bin/env bash

set -euo pipefail

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "macOS notarization must run on macOS." >&2
    exit 1
fi

if [[ $# -lt 1 || $# -gt 3 ]]; then
    echo "Usage: $0 <dmg-path> [app-path] [notarization-log-path]" >&2
    exit 1
fi

dmg_path="$1"
app_path="${2:-}"
report_path="${3:-app-desktop/build/reports/macos-notarization.json}"

: "${MACOS_IDENTITY:?MACOS_IDENTITY is required}"
: "${MACOS_KEYCHAIN_PATH:?MACOS_KEYCHAIN_PATH is required}"
: "${MACOS_NOTARY_PROFILE:?MACOS_NOTARY_PROFILE is required}"

for command_name in codesign hdiutil security spctl xcrun; do
    if ! command -v "$command_name" >/dev/null 2>&1; then
        echo "$command_name is required for macOS release verification." >&2
        exit 1
    fi
done

if [[ ! -f "$dmg_path" ]]; then
    echo "DMG not found: $dmg_path" >&2
    exit 1
fi

mount_path=""
result_path="$(mktemp -t passvault-notary-result).json"

cleanup() {
    if [[ -n "$mount_path" ]]; then
        hdiutil detach "$mount_path" -quiet >/dev/null 2>&1 || true
        rmdir "$mount_path" >/dev/null 2>&1 || true
    fi
    rm -f "$result_path"
}
trap cleanup EXIT

hdiutil verify "$dmg_path"

if [[ -z "$app_path" || ! -d "$app_path" ]]; then
    mount_path="$(mktemp -d -t passvault-dmg)"
    hdiutil attach "$dmg_path" -mountpoint "$mount_path" -nobrowse -readonly -quiet
    app_path="$(find "$mount_path" -maxdepth 2 -type d -name 'PassVault.app' -print -quit)"
fi

if [[ -z "$app_path" || ! -d "$app_path" ]]; then
    echo "PassVault.app was not found for signature verification." >&2
    exit 1
fi

codesign --verify --deep --strict --verbose=2 "$app_path"
signature_details="$(codesign --display --verbose=4 "$app_path" 2>&1)"

if ! grep -Fq "Authority=$MACOS_IDENTITY" <<<"$signature_details"; then
    echo "The app signer does not match MACOS_IDENTITY." >&2
    exit 1
fi

if ! grep -Eq '^Timestamp=' <<<"$signature_details"; then
    echo "The app signature does not contain a secure Apple timestamp." >&2
    exit 1
fi

if ! grep -Eq '^CodeDirectory .*flags=.*\(runtime\)' <<<"$signature_details"; then
    echo "The app signature does not enable Hardened Runtime." >&2
    exit 1
fi

entitlements_path="$(mktemp -t passvault-entitlements).plist"
if codesign --display --entitlements :- "$app_path" >"$entitlements_path" 2>/dev/null; then
    if grep -A1 -F 'com.apple.security.get-task-allow' "$entitlements_path" | grep -Fq '<true/>'; then
        rm -f "$entitlements_path"
        echo "Release app contains the forbidden get-task-allow entitlement." >&2
        exit 1
    fi
fi
rm -f "$entitlements_path"

if [[ -n "$mount_path" ]]; then
    hdiutil detach "$mount_path" -quiet
    rmdir "$mount_path"
    mount_path=""
fi

codesign --force --timestamp --sign "$MACOS_IDENTITY" "$dmg_path"
codesign --verify --strict --verbose=2 "$dmg_path"

mkdir -p "$(dirname "$report_path")"
xcrun notarytool submit "$dmg_path" \
    --keychain-profile "$MACOS_NOTARY_PROFILE" \
    --keychain "$MACOS_KEYCHAIN_PATH" \
    --wait \
    --no-progress \
    --output-format json >"$result_path"

status="$(plutil -extract status raw -o - "$result_path")"
submission_id="$(plutil -extract id raw -o - "$result_path")"

xcrun notarytool log "$submission_id" \
    --keychain-profile "$MACOS_NOTARY_PROFILE" \
    --keychain "$MACOS_KEYCHAIN_PATH" \
    "$report_path"

if [[ "$status" != "Accepted" ]]; then
    cat "$report_path" >&2
    echo "Apple notarization was not accepted: $status" >&2
    exit 1
fi

xcrun stapler staple "$dmg_path"
xcrun stapler validate "$dmg_path"
spctl --assess --type open --context context:primary-signature --verbose=4 "$dmg_path"

echo "macOS Developer ID signature, notarization, stapling, and Gatekeeper checks passed."
