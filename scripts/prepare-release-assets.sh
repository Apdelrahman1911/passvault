#!/usr/bin/env bash

set -euo pipefail

if [[ $# -lt 3 || $# -gt 4 ]]; then
    echo "Usage: $0 <download-directory> <output-directory> <version> [test|production]" >&2
    exit 1
fi

download_directory="$1"
output_directory="$2"
version="$3"
release_kind="${4:-production}"

if [[ "$release_kind" != "test" && "$release_kind" != "production" ]]; then
    echo "Release kind must be test or production." >&2
    exit 1
fi

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

required_extensions=(exe msi deb rpm)
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

dmg_matches=()
while IFS= read -r -d '' matched_path; do
    dmg_matches+=("$matched_path")
done < <(find "$download_directory" -type f -iname '*.dmg' -print0)
if (( ${#dmg_matches[@]} != 2 )); then
    echo "Expected exactly two architecture-specific .dmg artifacts, found ${#dmg_matches[@]}." >&2
    exit 1
fi
for architecture in arm64 x64; do
    matches=()
    for matched_path in "${dmg_matches[@]}"; do
        [[ "$(basename "$matched_path")" == *"-$architecture.dmg" ]] && matches+=("$matched_path")
    done
    if (( ${#matches[@]} != 1 )); then
        echo "Expected exactly one macOS $architecture DMG." >&2
        exit 1
    fi
    install -m 0644 "${matches[0]}" "$output_directory/$(basename "${matches[0]}")"
done

commit_sha="${GITHUB_SHA:-unknown}"
cat >"$output_directory/RELEASE-MANIFEST.txt" <<EOF
PassVault release manifest
Version: $version
Commit: $commit_sha

Release kind: $release_kind

Signature policy:
- Windows EXE and MSI: $(if [[ "$release_kind" == production ]]; then echo 'Authenticode signing is mandatory and verified.'; else echo 'intentionally unsigned testing artifacts.'; fi)
- macOS DMGs (arm64 and x64): $(if [[ "$release_kind" == production ]]; then echo 'Developer ID signing and notarization are mandatory and verified.'; else echo 'intentionally unsigned testing artifacts.'; fi)
- Linux DEB and RPM: integrity-protected by this SHA-256 manifest; no platform signature.
- Always verify downloaded files against SHA256SUMS.txt before installation.
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

echo "Prepared six desktop packages, a release manifest, and verified SHA-256 checksums."
