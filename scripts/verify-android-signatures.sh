#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
apk_path="${1:-}"
aab_path="${2:-}"
fingerprint_path="${3:-$repository_root/release/android/passvault-release-cert.sha256}"

if [[ -z "$apk_path" ]]; then
    apk_path="$(find "$repository_root/app-android/build/outputs/apk/standard/release" \
        -maxdepth 1 -type f -name '*.apk' -print 2>/dev/null | sort | head -n 1)"
fi
if [[ -z "$aab_path" ]]; then
    aab_path="$(find "$repository_root/app-android/build/outputs/bundle/standardRelease" \
        -maxdepth 1 -type f -name '*.aab' -print 2>/dev/null | sort | head -n 1)"
fi

for required_file in "$apk_path" "$aab_path" "$fingerprint_path"; do
    if [[ -z "$required_file" || ! -f "$required_file" || -L "$required_file" ]]; then
        echo "A required Android signature-verification input is missing or unsafe." >&2
        exit 1
    fi
done

sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [[ -z "$sdk_root" && -f "$repository_root/local.properties" ]]; then
    sdk_root="$(sed -n 's/^sdk\.dir=//p' "$repository_root/local.properties" | head -n 1)"
    sdk_root="${sdk_root//\\:/:}"
    sdk_root="${sdk_root//\\\\/\\}"
fi
if [[ -z "$sdk_root" || ! -d "$sdk_root/build-tools" ]]; then
    echo "ANDROID_SDK_ROOT, ANDROID_HOME, or local.properties must identify the Android SDK." >&2
    exit 1
fi

apksigner="$(find "$sdk_root/build-tools" -mindepth 2 -maxdepth 2 -type f \
    -name apksigner -print | sort | tail -n 1)"
if [[ -z "$apksigner" || ! -x "$apksigner" ]]; then
    echo "apksigner was not found in the Android SDK build-tools." >&2
    exit 1
fi

expected_fingerprint="$(tr -cd '0-9A-Fa-f' < "$fingerprint_path" | tr '[:lower:]' '[:upper:]')"
if [[ ! "$expected_fingerprint" =~ ^[0-9A-F]{64}$ ]]; then
    echo "The pinned Android SHA-256 fingerprint is invalid." >&2
    exit 1
fi

apk_verification="$($apksigner verify --verbose --print-certs "$apk_path" 2>&1)" || {
    echo "APK cryptographic signature verification failed." >&2
    exit 1
}
apk_fingerprint="$(printf '%s\n' "$apk_verification" |
    sed -n 's/.*certificate SHA-256 digest:[[:space:]]*//p' |
    head -n 1 | tr -cd '0-9A-Fa-f' | tr '[:lower:]' '[:upper:]')"
unset apk_verification
if [[ "$apk_fingerprint" != "$expected_fingerprint" ]]; then
    echo "APK signer fingerprint does not match the pinned PassVault upload certificate." >&2
    exit 1
fi

if ! jarsigner -verify "$aab_path" >/dev/null 2>&1; then
    echo "AAB JAR signature verification failed." >&2
    exit 1
fi
aab_fingerprint="$(keytool -printcert -jarfile "$aab_path" 2>/dev/null |
    awk -F'SHA256:' '/SHA256:/ { gsub(/[^0-9A-Fa-f]/, "", $2); print toupper($2); exit }')"
if [[ "$aab_fingerprint" != "$expected_fingerprint" ]]; then
    echo "AAB signer fingerprint does not match the pinned PassVault upload certificate." >&2
    exit 1
fi

echo "Android APK and AAB signatures match the pinned PassVault upload certificate."
