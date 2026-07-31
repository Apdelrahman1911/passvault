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
    mapfile -d '' matches < <(
        find "$download_directory" -type f -iname "*.$extension" -print0
    )
    if (( ${#matches[@]} != 1 )); then
        echo "Expected exactly one .$extension artifact, found ${#matches[@]}." >&2
        exit 1
    fi

    source_path="${matches[0]}"
    file_name="$(basename "$source_path")"
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
    find . -maxdepth 1 -type f ! -name 'SHA256SUMS.txt' -printf '%f\0' |
        sort -z |
        xargs -0 sha256sum >SHA256SUMS.txt
    sha256sum --check SHA256SUMS.txt
)

echo "Prepared seven packages, a release manifest, and verified SHA-256 checksums."
