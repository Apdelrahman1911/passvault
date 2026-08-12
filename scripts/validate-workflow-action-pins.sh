#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
workflow_root="${1:-$repository_root/.github/workflows}"

if [[ ! -d "$workflow_root" ]]; then
    echo "Workflow directory does not exist: $workflow_root" >&2
    exit 1
fi

list_action_references() {
    while IFS= read -r -d '' workflow_file; do
        awk '
            /^[[:space:]]*uses:[[:space:]]+/ {
                printf "%s:%d:%s\n", FILENAME, FNR, $0
            }
        ' "$workflow_file"
    done < <(
        find "$workflow_root" -type f \( -name '*.yml' -o -name '*.yaml' \) -print0
    )
}

failure=0
while IFS=: read -r file line_number reference; do
    reference="${reference#"${reference%%[![:space:]]*}"}"
    reference="${reference#uses:}"
    reference="${reference#"${reference%%[![:space:]]*}"}"
    reference="${reference%%[[:space:]]*}"

    case "$reference" in
        ./*)
            continue
            ;;
        *@*)
            revision="${reference##*@}"
            if [[ ! "$revision" =~ ^[0-9a-f]{40}$ ]]; then
                echo "$file:$line_number uses mutable action reference $reference" >&2
                failure=1
            fi
            ;;
        *)
            echo "$file:$line_number has an invalid action reference: $reference" >&2
            failure=1
            ;;
    esac
done < <(list_action_references)

if (( failure != 0 )); then
    exit 1
fi

echo "All external GitHub Actions are pinned to immutable commits."
