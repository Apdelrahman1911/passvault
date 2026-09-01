#!/usr/bin/env bash

# This file is a sourced library, never a standalone script. It intentionally
# omits `set -euo pipefail`: shell options would leak into the caller. Callers
# must enable strict mode before sourcing this library.

# Preserve the hosted runner's user keychain search list while a release-only
# keychain is active. Paths are passed as array elements and are never eval'd.

passvault_capture_user_keychains() {
    local output_path="$1"
    local keychain_list line keychain_path captured_count=0

    if ! keychain_list="$(security list-keychains -d user)"; then
        echo "Unable to read the current user keychain search list." >&2
        return 1
    fi
    if [[ -z "$keychain_list" ]]; then
        echo "The current user keychain search list is unexpectedly empty." >&2
        return 1
    fi

    : > "$output_path"
    chmod 600 "$output_path"
    while IFS= read -r line || [[ -n "$line" ]]; do
        line="${line#"${line%%[![:space:]]*}"}"
        line="${line%"${line##*[![:space:]]}"}"
        if [[ "$line" != \"*\" ]]; then
            echo "Unexpected keychain-list output; refusing to replace the search list." >&2
            return 1
        fi
        keychain_path="${line#\"}"
        keychain_path="${keychain_path%\"}"
        if [[ "$keychain_path" != /* || "$keychain_path" == *$'\n'* || "$keychain_path" == *$'\r'* ]]; then
            echo "Unsafe keychain path in the current search list." >&2
            return 1
        fi
        printf '%s\n' "$keychain_path" >> "$output_path"
        captured_count=$((captured_count + 1))
    done <<< "$keychain_list"
    if (( captured_count == 0 )); then
        echo "The current user keychain search list is unexpectedly empty." >&2
        return 1
    fi
}

passvault_activate_release_keychain() {
    local release_keychain="$1"
    local original_list_path="$2"
    local keychain_path
    local -a keychains=("$release_keychain")

    while IFS= read -r keychain_path; do
        [[ -z "$keychain_path" || "$keychain_path" == "$release_keychain" ]] ||
            keychains+=("$keychain_path")
    done < "$original_list_path"
    security list-keychains -d user -s "${keychains[@]}"
}

passvault_restore_user_keychains() {
    local original_list_path="$1"
    local keychain_path
    local -a keychains=()

    [[ -f "$original_list_path" && ! -L "$original_list_path" ]] || return 0
    while IFS= read -r keychain_path; do
        [[ -z "$keychain_path" ]] || keychains+=("$keychain_path")
    done < "$original_list_path"
    security list-keychains -d user -s "${keychains[@]}"
}
