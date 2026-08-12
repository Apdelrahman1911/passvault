#!/usr/bin/env bash

# Verify an installed/package resource directory against the canonical legal set.

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <legal-bundle-directory>" >&2
    exit 2
fi

bundle_directory="$1"
license_source_directory="$repository_root/THIRD_PARTY_LICENSES"
license_bundle_directory="$bundle_directory/THIRD_PARTY_LICENSES"

if [[ ! -d "$bundle_directory" || -L "$bundle_directory" ]]; then
    echo "Legal bundle directory is missing or unsafe: $bundle_directory" >&2
    exit 1
fi
if [[ ! -d "$license_source_directory" || -L "$license_source_directory" ]]; then
    echo "Canonical third-party license directory is missing or unsafe." >&2
    exit 1
fi
if [[ ! -d "$license_bundle_directory" || -L "$license_bundle_directory" ]]; then
    echo "Bundled third-party license directory is missing or unsafe." >&2
    exit 1
fi

for legal_document in LICENSE.txt NOTICE.txt THIRD_PARTY_NOTICES.md; do
    source_path="$repository_root/$legal_document"
    bundled_path="$bundle_directory/$legal_document"
    if [[ ! -f "$source_path" || -L "$source_path" ||
          ! -f "$bundled_path" || -L "$bundled_path" ]] ||
       ! cmp -s "$source_path" "$bundled_path"; then
        echo "Bundled legal document is missing, unsafe, or stale: $legal_document" >&2
        exit 1
    fi
done

expected_count="$(find "$license_source_directory" -mindepth 1 -maxdepth 1 -type f |
    wc -l | tr -d ' ')"
actual_count="$(find "$license_bundle_directory" -mindepth 1 -maxdepth 1 -type f |
    wc -l | tr -d ' ')"
if [[ "$expected_count" == 0 || "$actual_count" != "$expected_count" ]]; then
    echo "Bundled third-party license count is wrong: $actual_count; expected $expected_count." >&2
    exit 1
fi
unsafe_license_entry="$(find "$license_source_directory" "$license_bundle_directory" \
    -mindepth 1 -maxdepth 1 \( -type l -o ! -type f \) -print -quit)"
if [[ -n "$unsafe_license_entry" ]]; then
    echo "Third-party license directories must contain only regular, non-symlinked files." >&2
    exit 1
fi

while IFS= read -r -d '' source_path; do
    file_name="$(basename "$source_path")"
    if [[ ! "$file_name" =~ ^[A-Za-z0-9._+-]+$ ]] ||
       ! cmp -s "$source_path" "$license_bundle_directory/$file_name"; then
        echo "Bundled third-party license is missing or stale: $file_name" >&2
        exit 1
    fi
done < <(find "$license_source_directory" -mindepth 1 -maxdepth 1 -type f -print0)

echo "Installed legal notice bundle verified."
