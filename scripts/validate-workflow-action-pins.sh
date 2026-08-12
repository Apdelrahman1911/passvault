#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
workflow_root="${1:-$repository_root/.github/workflows}"

if [[ ! -d "$workflow_root" ]]; then
    echo "Workflow directory does not exist: $workflow_root" >&2
    exit 1
fi

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
done < <(rg -n --no-heading '^[[:space:]]*uses:[[:space:]]+' "$workflow_root")

if (( failure != 0 )); then
    exit 1
fi

echo "All external GitHub Actions are pinned to immutable commits."
