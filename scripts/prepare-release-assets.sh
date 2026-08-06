#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 3 ]]; then
    echo "Usage: $0 <download-directory> <output-directory> <version>" >&2
    exit 1
fi

download_directory="$1"
output_directory="$2"
version="$3"

if [[ ! -d "$download_directory" ]]; then
    echo "Downloaded artifact directory was not found: $download_directory" >&2
    exit 1
fi

if [[ ! "$version" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
    echo "Invalid release version: $version" >&2
    exit 1
fi

mkdir -p "$output_directory"
if find "$output_directory" -mindepth 1 -print -quit | grep -q .; then
    echo "Release output directory must be empty: $output_directory" >&2
    exit 1
fi

required_extensions=(apk aab exe msi dmg deb rpm)
for extension in "${required_extensions[@]}"; do
    matches=()
    while IFS= read -r -d '' matched_path; do
        matches+=("$matched_path")
    done < <(find "$download_directory" -type f -iname "*.$extension" -print0)
    if (( ${#matches[@]} != 1 )); then
        echo "Expected exactly one .$extension artifact, found ${#matches[@]}." >&2
        exit 1
    fi

    source_path="${matches[0]}"
    file_name="$(basename "$source_path")"
    if [[ ! "$file_name" =~ ^[A-Za-z0-9._+-]+$ ]]; then
        echo "Release artifact name contains unsupported characters: $file_name" >&2
        exit 1
    fi
    destination_path="$output_directory/$file_name"
    if [[ -e "$destination_path" ]]; then
        echo "Duplicate release artifact name: $file_name" >&2
        exit 1
    fi
    install -m 0644 "$source_path" "$destination_path"
done

commit_sha="${GITHUB_SHA:-unknown}"
cat >"$output_directory/RELEASE-MANIFEST.txt" <<EOF
PassVault release manifest
Version: $version
Commit: $commit_sha

Signature policy:
- Android APK and AAB: signed and checked against the pinned release certificate.
- Windows launcher, EXE, and MSI: Authenticode-signed, timestamped, and trust-verified.
- macOS DMG: Developer ID-signed, notarized, stapled, and Gatekeeper-verified.
- Linux DEB and RPM: integrity-protected by this SHA-256 manifest; no platform signature.
EOF

(
    cd "$output_directory"
    if command -v sha256sum >/dev/null 2>&1; then
        checksum_tool=(sha256sum)
    else
        checksum_tool=(shasum -a 256)
    fi
    find . -maxdepth 1 -type f ! -name 'SHA256SUMS.txt' -print |
        sed 's#^./##' | LC_ALL=C sort |
        while IFS= read -r file_name; do
            "${checksum_tool[@]}" "$file_name"
        done >SHA256SUMS.txt
    "${checksum_tool[@]}" --check SHA256SUMS.txt
)

echo "Prepared seven packages, a release manifest, and verified SHA-256 checksums."
