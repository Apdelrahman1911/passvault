#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
apk_path="${1:-}"
aab_path="${2:-}"
fingerprint_path="${3:-$repository_root/release/android/passvault-release-cert.sha256}"

if [[ -z "$apk_path" ]]; then
    apk_matches=()
    while IFS= read -r -d '' match; do
        apk_matches+=("$match")
    done < <(find "$repository_root/app-android/build/outputs/apk/standard/release" \
        -maxdepth 1 -type f -name '*.apk' -print0 2>/dev/null)
    if (( ${#apk_matches[@]} != 1 )); then
        echo "Expected exactly one Standard release APK; found ${#apk_matches[@]}." >&2
        exit 1
    fi
    apk_path="${apk_matches[0]}"
fi
if [[ -z "$aab_path" ]]; then
    aab_matches=()
    while IFS= read -r -d '' match; do
        aab_matches+=("$match")
    done < <(find "$repository_root/app-android/build/outputs/bundle/standardRelease" \
        -maxdepth 1 -type f -name '*.aab' -print0 2>/dev/null)
    if (( ${#aab_matches[@]} != 1 )); then
        echo "Expected exactly one Standard release AAB; found ${#aab_matches[@]}." >&2
        exit 1
    fi
    aab_path="${aab_matches[0]}"
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
if ! command -v ruby >/dev/null 2>&1; then
    echo "Ruby is required to select the newest compatible Android build-tools version." >&2
    exit 1
fi

apksigner="$(find "$sdk_root/build-tools" -mindepth 2 -maxdepth 2 -type f \
    -name apksigner -print0 | ruby -rrubygems -e '
      paths = STDIN.read.split("\0").reject(&:empty?)
      compatible = paths.select do |path|
        Gem::Version.correct?(File.basename(File.dirname(path)))
      end
      puts compatible.max_by { |path| Gem::Version.new(File.basename(File.dirname(path))) }
    ')"
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
    awk -F'digest:' '/certificate SHA-256 digest:/ {
        value = $2
        gsub(/[^0-9A-Fa-f]/, "", value)
        print toupper(value)
    }' | LC_ALL=C sort -u)"
unset apk_verification
if [[ "$apk_fingerprint" != "$expected_fingerprint" ]]; then
    echo "APK must have exactly the pinned PassVault upload-certificate signer." >&2
    exit 1
fi

if jarsigner -verify -strict "$aab_path" >/dev/null 2>&1; then
    jarsigner_status=0
else
    jarsigner_status=$?
fi
# jarsigner strict status 4 reports the expected untrusted/self-signed chain.
# The Android upload certificate is separately pinned by its exact SHA-256 digest below.
if (( jarsigner_status != 0 && jarsigner_status != 4 )); then
    echo "AAB strict JAR signature verification failed." >&2
    exit 1
fi
aab_fingerprint="$(keytool -printcert -jarfile "$aab_path" 2>/dev/null |
    awk -F'SHA256:' '/SHA256:/ {
        value = $2
        gsub(/[^0-9A-Fa-f]/, "", value)
        print toupper(value)
    }' | LC_ALL=C sort -u)"
if [[ "$aab_fingerprint" != "$expected_fingerprint" ]]; then
    echo "AAB must have exactly the pinned PassVault upload-certificate signer." >&2
    exit 1
fi

echo "Android APK and AAB signatures match the pinned PassVault upload certificate."
