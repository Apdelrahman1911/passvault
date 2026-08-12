#!/usr/bin/env bash

# Verify a GitHub-release third-party license archive against canonical sources.

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <third-party-license-zip>" >&2
    exit 2
fi

archive_path="$1"
license_source_directory="$repository_root/THIRD_PARTY_LICENSES"
if [[ ! -f "$archive_path" || -L "$archive_path" ]]; then
    echo "Third-party license archive is missing or unsafe: $archive_path" >&2
    exit 1
fi
if [[ ! -d "$license_source_directory" || -L "$license_source_directory" ]]; then
    echo "Canonical third-party license directory is missing or unsafe." >&2
    exit 1
fi
unsafe_license_entry="$(find "$license_source_directory" -mindepth 1 -maxdepth 1 \
    \( -type l -o ! -type f \) -print -quit)"
if [[ -n "$unsafe_license_entry" ]]; then
    echo "Canonical third-party license directory contains an unsafe entry." >&2
    exit 1
fi
command -v unzip >/dev/null 2>&1 || {
    echo "unzip is required to verify the third-party license archive." >&2
    exit 1
}

verification_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-license-archive.XXXXXX")"
cleanup() {
    if [[ "$verification_root" == "${TMPDIR:-/tmp}"/passvault-license-archive.* &&
          -d "$verification_root" ]]; then
        rm -rf -- "$verification_root"
    fi
}
trap cleanup EXIT

expected_entries="$verification_root/expected"
actual_entries="$verification_root/actual"
archive_listing="$verification_root/listing"
find "$license_source_directory" -mindepth 1 -maxdepth 1 -type f -print |
    while IFS= read -r source_path; do
        file_name="$(basename "$source_path")"
        if [[ ! "$file_name" =~ ^[A-Za-z0-9._+-]+$ || -L "$source_path" ]]; then
            echo "Canonical third-party license is unsafe: $file_name" >&2
            exit 1
        fi
        printf 'THIRD_PARTY_LICENSES/%s\n' "$file_name"
    done |
    LC_ALL=C sort >"$expected_entries"
unzip -Z1 "$archive_path" | LC_ALL=C sort >"$actual_entries"
unzip -Z -l "$archive_path" >"$archive_listing"

if [[ ! -s "$expected_entries" ]] || ! cmp -s "$expected_entries" "$actual_entries"; then
    echo "Third-party license archive has a missing, extra, duplicate, or unsafe entry." >&2
    exit 1
fi

while IFS= read -r entry_name; do
    source_path="$repository_root/$entry_name"
    entry_type="$(awk -v entry_name="$entry_name" '$NF == entry_name { print substr($1, 1, 1) }' \
        "$archive_listing")"
    if [[ "$entry_type" != "-" ]]; then
        echo "Archived third-party license is not a regular file: $entry_name" >&2
        exit 1
    fi
    if ! unzip -p "$archive_path" "$entry_name" | cmp -s "$source_path" -; then
        echo "Archived third-party license is stale: $entry_name" >&2
        exit 1
    fi
done <"$expected_entries"

echo "Third-party license archive verified."
