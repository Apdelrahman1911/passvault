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
repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

: "${MACOS_IDENTITY:?MACOS_IDENTITY is required}"
: "${MACOS_TEAM_ID:?MACOS_TEAM_ID is required}"
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
result_path="$(mktemp -t passvault-notary-result)"
app_from_dmg=false

cleanup() {
    if [[ -n "$mount_path" ]]; then
        hdiutil detach "$mount_path" -quiet >/dev/null 2>&1 || true
        rmdir "$mount_path" >/dev/null 2>&1 || true
    fi
    rm -f "$result_path"
}
trap cleanup EXIT

hdiutil verify "$dmg_path"

if [[ -z "$app_path" || ! -d "$app_path" || -L "$app_path" ]]; then
    mount_path="$(mktemp -d -t passvault-dmg)"
    hdiutil attach "$dmg_path" -mountpoint "$mount_path" -nobrowse -readonly -quiet
    app_count="$(find "$mount_path" -maxdepth 2 -type d -name 'PassVault.app' | wc -l | tr -d ' ')"
    if [[ "$app_count" != 1 ]]; then
        echo "Expected exactly one PassVault.app in the DMG; found $app_count." >&2
        exit 1
    fi
    app_path="$(find "$mount_path" -maxdepth 2 -type d -name 'PassVault.app' -print)"
    app_from_dmg=true
fi

if [[ -z "$app_path" || ! -d "$app_path" ]]; then
    echo "PassVault.app was not found for signature verification." >&2
    exit 1
fi

"$repository_root/scripts/verify-macos-release-artifact.sh" "$app_path"

if [[ -n "$mount_path" ]]; then
    hdiutil detach "$mount_path" -quiet
    rmdir "$mount_path"
    mount_path=""
fi

codesign --force --timestamp --sign "$MACOS_IDENTITY" "$dmg_path"
if [[ "$app_from_dmg" == true ]]; then
    mount_path="$(mktemp -d -t passvault-signed-dmg)"
    hdiutil attach "$dmg_path" -mountpoint "$mount_path" -nobrowse -readonly -quiet
    app_count="$(find "$mount_path" -maxdepth 2 -type d -name 'PassVault.app' | wc -l | tr -d ' ')"
    if [[ "$app_count" != 1 ]]; then
        echo "Expected exactly one PassVault.app in the signed DMG; found $app_count." >&2
        exit 1
    fi
    app_path="$(find "$mount_path" -maxdepth 2 -type d -name 'PassVault.app' -print)"
fi
"$repository_root/scripts/verify-macos-release-artifact.sh" "$app_path" "$dmg_path" false
if [[ -n "$mount_path" ]]; then
    hdiutil detach "$mount_path" -quiet
    rmdir "$mount_path"
    mount_path=""
fi

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

mount_path="$(mktemp -d -t passvault-notarized-dmg)"
hdiutil attach "$dmg_path" -mountpoint "$mount_path" -nobrowse -readonly -quiet
stapled_app_count="$(find "$mount_path" -maxdepth 2 -type d -name 'PassVault.app' | wc -l | tr -d ' ')"
if [[ "$stapled_app_count" != 1 ]]; then
    echo "The notarized DMG must contain exactly one PassVault.app; found $stapled_app_count." >&2
    exit 1
fi
stapled_app_path="$(find "$mount_path" -maxdepth 2 -type d -name 'PassVault.app' -print)"
"$repository_root/scripts/verify-macos-release-artifact.sh" \
    "$stapled_app_path" "$dmg_path" true

echo "macOS Developer ID signature, notarization, stapling, and Gatekeeper checks passed."
