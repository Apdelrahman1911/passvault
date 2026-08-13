#!/usr/bin/env bash

set -euo pipefail

if (( $# != 2 )); then
    echo "Usage: $0 <candidate-commit> <approved-main-commit>" >&2
    exit 64
fi

candidate_input="$1"
main_input="$2"

require_commit() {
    local label="$1"
    local value="$2"
    local object_type

    if [[ ! "$value" =~ ^[0-9A-Fa-f]{40}$ ]]; then
        echo "$label must be a full Git commit SHA." >&2
        return 1
    fi
    if ! object_type="$(git cat-file -t "$value" 2>/dev/null)" || [[ "$object_type" != commit ]]; then
        echo "$label does not identify an available Git commit." >&2
        return 1
    fi

    printf '%s' "$value" | tr '[:upper:]' '[:lower:]'
}

candidate_commit="$(require_commit "Candidate commit" "$candidate_input")"
main_commit="$(require_commit "Approved main commit" "$main_input")"

if git merge-base --is-ancestor "$main_commit" "$candidate_commit"; then
    printf 'Candidate %s contains current main commit %s.\n' "$candidate_commit" "$main_commit"
    exit 0
fi

if ! merge_base="$(git merge-base "$main_commit" "$candidate_commit" 2>/dev/null)" || \
    [[ -z "$merge_base" ]]; then
    echo "Candidate and current main do not share repository history." >&2
    exit 1
fi

candidate_tree="$(git rev-parse "$candidate_commit^{tree}")"
main_tree="$(git rev-parse "$main_commit^{tree}")"
if [[ "$candidate_tree" != "$main_tree" ]]; then
    echo "Candidate neither contains current main nor has its exact Git tree." >&2
    exit 1
fi

printf 'Candidate %s is the exact protected-rebase tree of main %s (tree %s).\n' \
    "$candidate_commit" "$main_commit" "$candidate_tree"
