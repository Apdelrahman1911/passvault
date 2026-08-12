#!/usr/bin/env bash

# Preserve the hosted runner's user keychain search list while a release-only
# keychain is active. Paths are passed as array elements and are never eval'd.

passvault_capture_user_keychains() {
    local output_path="$1"
    local line keychain_path

    : > "$output_path"
    chmod 600 "$output_path"
    while IFS= read -r line; do
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
    done < <(security list-keychains -d user)
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
