#!/usr/bin/env bash

set -euo pipefail

if [[ "$(uname -s)" != Darwin ]]; then
    echo "macOS release verification must run on macOS." >&2
    exit 1
fi

if [[ $# -lt 1 || $# -gt 3 ]]; then
    echo "Usage: $0 <app-path> [dmg-path] [require-stapled]" >&2
    exit 2
fi

app_path="$1"
dmg_path="${2:-}"
require_stapled="${3:-false}"

: "${MACOS_IDENTITY:?MACOS_IDENTITY is required}"
: "${MACOS_TEAM_ID:?MACOS_TEAM_ID is required}"

if [[ ! -d "$app_path" || -L "$app_path" ]]; then
    echo "A real application bundle is required: $app_path" >&2
    exit 1
fi
if [[ ! "$MACOS_IDENTITY" == "Developer ID Application:"* ]]; then
    echo "MACOS_IDENTITY must be a Developer ID Application identity." >&2
    exit 1
fi
if [[ ! "$MACOS_TEAM_ID" =~ ^[A-Z0-9]{10}$ ]]; then
    echo "MACOS_TEAM_ID must be a 10-character Apple Team ID." >&2
    exit 1
fi
case "$require_stapled" in true|false) ;; *)
    echo "require-stapled must be true or false." >&2
    exit 2
    ;;
esac

for command_name in codesign file find otool plutil readlink ruby shasum spctl xcrun; do
    command -v "$command_name" >/dev/null 2>&1 || {
        echo "$command_name is required for macOS release verification." >&2
        exit 1
    }
done

special_entry="$(find "$app_path" ! -type f ! -type d ! -type l -print -quit)"
if [[ -n "$special_entry" ]]; then
    echo "The application bundle contains an unsupported special filesystem entry: $special_entry" >&2
    exit 1
fi
while IFS= read -r -d '' symlink_path; do
    symlink_target="$(readlink "$symlink_path")"
    if [[ -z "$symlink_target" || "$symlink_target" == /* ]]; then
        echo "The application bundle contains an unsafe symlink: $symlink_path" >&2
        exit 1
    fi
    if ! APP_ROOT="$app_path" SYMLINK_PATH="$symlink_path" ruby -e '
      root = File.realpath(ENV.fetch("APP_ROOT"))
      resolved = File.realpath(ENV.fetch("SYMLINK_PATH"))
      exit(resolved.start_with?(root + File::SEPARATOR) ? 0 : 1)
    '; then
        echo "The application bundle contains a broken or escaping symlink: $symlink_path" >&2
        exit 1
    fi
done < <(find "$app_path" -type l -print0)

entitlements_path=""
cleanup() {
    case "$entitlements_path" in
        */passvault-code-entitlements.*) rm -f -- "$entitlements_path" ;;
    esac
}
trap cleanup EXIT

verify_signature_details() {
    local candidate="$1"
    local details
    codesign --verify --strict --verbose=2 "$candidate" >/dev/null 2>&1
    details="$(codesign --display --verbose=4 "$candidate" 2>&1)"
    grep -Fqx "Authority=$MACOS_IDENTITY" <<<"$details" || {
        echo "Unexpected signer on nested code: $candidate" >&2
        exit 1
    }
    grep -Fqx "TeamIdentifier=$MACOS_TEAM_ID" <<<"$details" || {
        echo "Unexpected Apple team on nested code: $candidate" >&2
        exit 1
    }
    grep -Eq '^Timestamp=' <<<"$details" || {
        echo "Secure timestamp is missing from nested code: $candidate" >&2
        exit 1
    }
    grep -Eq '^CodeDirectory .*flags=.*\(runtime\)' <<<"$details" || {
        echo "Hardened Runtime is missing from nested code: $candidate" >&2
        exit 1
    }

    entitlements_path="$(mktemp -t passvault-code-entitlements)"
    if codesign --display --entitlements :- "$candidate" >"$entitlements_path" 2>/dev/null; then
        if plutil -extract com.apple.security.get-task-allow raw -o - \
            "$entitlements_path" 2>/dev/null | grep -Fqx true; then
            rm -f -- "$entitlements_path"
            entitlements_path=""
            echo "Forbidden get-task-allow entitlement on: $candidate" >&2
            exit 1
        fi
    fi
    rm -f -- "$entitlements_path"
    entitlements_path=""
}

codesign --verify --deep --strict --verbose=2 "$app_path" >/dev/null 2>&1
verify_signature_details "$app_path"

bridge_count="$(find "$app_path" -type f -name 'libpassvault_biometric.dylib' | wc -l | tr -d ' ')"
manifest_count="$(find "$app_path" -type f -name 'bridge.properties' | wc -l | tr -d ' ')"
if [[ "$bridge_count" != 1 || "$manifest_count" != 1 ]]; then
    echo "Expected exactly one macOS biometric bridge and manifest; found $bridge_count bridge(s) and $manifest_count manifest(s)." >&2
    exit 1
fi
bridge_path="$(find "$app_path" -type f -name 'libpassvault_biometric.dylib' -print)"
bridge_manifest="$(find "$app_path" -type f -name 'bridge.properties' -print)"
if [[ "$(dirname "$bridge_path")" != "$(dirname "$bridge_manifest")" ]]; then
    echo "The macOS biometric bridge and manifest are not co-located." >&2
    exit 1
fi
case "$(uname -m)" in
    arm64) biometric_platform="macos-arm64" ;;
    x86_64) biometric_platform="macos-x64" ;;
    *)
        echo "Unsupported macOS release architecture: $(uname -m)" >&2
        exit 1
        ;;
esac
expected_bridge_directory="$app_path/Contents/app/resources/native/$biometric_platform"
if [[ "$(dirname "$bridge_path")" != "$expected_bridge_directory" ]]; then
    echo "The macOS biometric bridge is outside its reviewed platform resource directory." >&2
    exit 1
fi
if [[ "$(find "$expected_bridge_directory" -mindepth 1 -maxdepth 1 -print | wc -l | tr -d ' ')" != 2 ]]; then
    echo "The macOS biometric bridge resource directory contains unexpected entries." >&2
    exit 1
fi
if [[ "$(wc -l < "$bridge_manifest" | tr -d ' ')" != 5 ]] ||
    ! grep -Fqx 'abi=1' "$bridge_manifest" ||
    ! grep -Fqx "platform=$biometric_platform" "$bridge_manifest" ||
    ! grep -Fqx 'library=libpassvault_biometric.dylib' "$bridge_manifest" ||
    ! grep -Fqx 'integrity=sha256-or-developer-id' "$bridge_manifest" ||
    ! grep -Eq '^sha256=[0-9a-f]{64}$' "$bridge_manifest"; then
    echo "The macOS biometric bridge manifest violates release policy." >&2
    exit 1
fi
if ! otool -D "$bridge_path" | tail -n +2 | grep -Fqx '@loader_path/libpassvault_biometric.dylib'; then
    echo "The macOS biometric bridge has an unsafe install name." >&2
    exit 1
fi
manifest_checksum="$(sed -n 's/^sha256=//p' "$bridge_manifest")"
actual_checksum="$(shasum -a 256 "$bridge_path" | awk '{print $1}')"
if [[ "$actual_checksum" != "$manifest_checksum" ]]; then
    # Signing changes Mach-O bytes after the unsigned staging checksum is
    # emitted. The mandatory nested Developer ID verification below is the
    # authenticated release binding in this case.
    verify_signature_details "$bridge_path"
fi

signed_macho_count=0
while IFS= read -r -d '' candidate; do
    if file -b "$candidate" | grep -Eq 'Mach-O|universal binary'; then
        verify_signature_details "$candidate"
        (( signed_macho_count += 1 ))
    fi
done < <(find "$app_path" -type f -print0)

if (( signed_macho_count == 0 )); then
    echo "The application bundle contains no signed Mach-O code." >&2
    exit 1
fi

if [[ -n "$dmg_path" ]]; then
    if [[ ! -f "$dmg_path" || -L "$dmg_path" ]]; then
        echo "A real DMG is required: $dmg_path" >&2
        exit 1
    fi
    codesign --verify --strict --verbose=2 "$dmg_path" >/dev/null 2>&1
    dmg_details="$(codesign --display --verbose=4 "$dmg_path" 2>&1)"
    grep -Fqx "Authority=$MACOS_IDENTITY" <<<"$dmg_details" || {
        echo "The DMG signer does not match MACOS_IDENTITY." >&2
        exit 1
    }
    grep -Fqx "TeamIdentifier=$MACOS_TEAM_ID" <<<"$dmg_details" || {
        echo "The DMG signer does not match MACOS_TEAM_ID." >&2
        exit 1
    }
    grep -Eq '^Timestamp=' <<<"$dmg_details" || {
        echo "The DMG signature lacks a secure timestamp." >&2
        exit 1
    }
fi

if [[ "$require_stapled" == true ]]; then
    [[ -n "$dmg_path" ]] || {
        echo "Stapled verification requires a DMG path." >&2
        exit 1
    }
    xcrun stapler validate "$dmg_path"
    spctl --assess --type open --context context:primary-signature --verbose=4 "$dmg_path"
    spctl --assess --type exec --verbose=4 "$app_path"
fi

echo "Verified $signed_macho_count nested Mach-O object(s), the app signature, and requested DMG gates."
