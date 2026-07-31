#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

export PUBLISHER_NAME="PassVault test publisher"
export COPYRIGHT_HOLDER="PassVault test contributors"
export SUPPORT_EMAIL="support@passvault.test"
export SECURITY_EMAIL="security@passvault.test"
export PRIVACY_POLICY_URL="https://passvault.test/privacy"
export PROJECT_URL="https://passvault.test/project"

version="$(awk -F= '$1 == "VERSION_NAME" { print $2 }' version.properties)"
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
    sha256sum --check SHA256SUMS.txt >/dev/null
)

rm -f "$input_directory/PassVault-$version.rpm"
if ./scripts/prepare-release-assets.sh \
    "$input_directory" \
    "$temporary_root/missing-output" \
    "$version" >/dev/null 2>&1; then
    echo "Missing required release artifact was accepted." >&2
    exit 1
fi

echo "Release automation tests passed."
