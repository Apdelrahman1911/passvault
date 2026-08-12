#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

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

if [[ ! -d "$download_directory" || -L "$download_directory" ]]; then
    echo "Downloaded artifact directory was not found: $download_directory" >&2
    exit 1
fi

if [[ ! "$version" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
    echo "Invalid release version: $version" >&2
    exit 1
fi
escaped_version="${version//./\\.}"
version_token_pattern="(^|[^0-9.])${escaped_version}([^0-9.]|$)"

commit_sha="${GITHUB_SHA:-$(git -C "$repository_root" rev-parse HEAD 2>/dev/null || true)}"
commit_sha="$(printf '%s' "$commit_sha" | tr '[:upper:]' '[:lower:]')"
if [[ ! "$commit_sha" =~ ^[0-9a-f]{40}$ ]]; then
    echo "A full source commit SHA is required for release provenance." >&2
    exit 1
fi

if [[ -L "$output_directory" || ( -e "$output_directory" && ! -d "$output_directory" ) ]]; then
    echo "Release output path must be a real directory: $output_directory" >&2
    exit 1
fi
mkdir -p "$output_directory"
if find "$output_directory" -mindepth 1 -print -quit | grep -q .; then
    echo "Release output directory must be empty: $output_directory" >&2
    exit 1
fi
output_directory="$(cd "$output_directory" && pwd -P)"

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
    file_stem="${file_name%.*}"
    if [[ ! "$file_stem" =~ $version_token_pattern ]]; then
        echo "Release artifact name does not contain the requested version: $file_name" >&2
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
    file_name="$(basename "${matches[0]}")"
    file_stem="${file_name%.*}"
    if [[ ! "$file_name" =~ ^[A-Za-z0-9._+-]+$ || ! "$file_stem" =~ $version_token_pattern ]]; then
        echo "macOS release artifact name is invalid or has the wrong version: $file_name" >&2
        exit 1
    fi
    destination_path="$output_directory/$file_name"
    if [[ -e "$destination_path" ]]; then
        echo "Duplicate release artifact name: $file_name" >&2
        exit 1
    fi
    install -m 0644 "${matches[0]}" "$destination_path"
done

legal_documents=(LICENSE.txt NOTICE.txt THIRD_PARTY_NOTICES.md)
for legal_document in "${legal_documents[@]}"; do
    source_path="$repository_root/$legal_document"
    if [[ ! -f "$source_path" || -L "$source_path" ]]; then
        echo "Required release legal document is missing or unsafe: $legal_document" >&2
        exit 1
    fi
    install -m 0644 "$source_path" "$output_directory/$legal_document"
done

third_party_license_source="$repository_root/THIRD_PARTY_LICENSES"
unsafe_license_entry="$(find "$third_party_license_source" -mindepth 1 -maxdepth 1 \
    \( -type l -o ! -type f \) -print -quit 2>/dev/null || true)"
if [[ ! -d "$third_party_license_source" || -L "$third_party_license_source" ||
      -n "$unsafe_license_entry" ]]; then
    echo "Required third-party license directory is missing or unsafe." >&2
    exit 1
fi
license_file_count=0
while IFS= read -r -d '' source_path; do
    file_name="$(basename "$source_path")"
    if [[ ! "$file_name" =~ ^[A-Za-z0-9._+-]+$ ]]; then
        echo "Third-party license name contains unsupported characters: $file_name" >&2
        exit 1
    fi
    (( license_file_count += 1 ))
done < <(find "$third_party_license_source" -mindepth 1 -maxdepth 1 -type f -print0)
if (( license_file_count == 0 )); then
    echo "The third-party license directory must not be empty." >&2
    exit 1
fi
command -v zip >/dev/null 2>&1 || {
    echo "zip is required to prepare the third-party license sidecar." >&2
    exit 1
}
third_party_license_archive="$output_directory/THIRD_PARTY_LICENSES.zip"
(
    cd "$repository_root"
    find THIRD_PARTY_LICENSES -mindepth 1 -maxdepth 1 -type f -print |
        LC_ALL=C sort |
        zip -X -q "$third_party_license_archive" -@
)
"$repository_root/scripts/verify-third-party-license-archive.sh" \
    "$third_party_license_archive" >/dev/null

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

Legal notice policy:
- Installed Android, iOS, and Desktop application images contain the canonical PassVault legal set.
- This GitHub release contains LICENSE.txt, NOTICE.txt, and THIRD_PARTY_NOTICES.md as direct sidecars.
- THIRD_PARTY_LICENSES.zip is an exact archive of the reproduced third-party license and notice texts.
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

echo "Prepared six desktop packages, exact legal sidecars, a release manifest, and verified SHA-256 checksums."
