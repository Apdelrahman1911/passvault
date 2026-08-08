#!/usr/bin/env bash

set -euo pipefail

apply=false
if [[ "${1:-}" == "--apply" ]]; then apply=true; fi

repository="${GITHUB_REPOSITORY:-}"
if [[ -z "$repository" ]]; then
    repository="$(gh repo view --json nameWithOwner --jq .nameWithOwner)"
fi
default_sha="$(gh api "repos/$repository/git/ref/heads/main" --jq .object.sha)"

ensure_branch() {
    local branch="$1"
    if gh api "repos/$repository/git/ref/heads/$branch" >/dev/null 2>&1; then
        echo "$branch already exists."
        return
    fi
    if [[ "$apply" != true ]]; then
        echo "Would create $branch at $default_sha."
        return
    fi
    gh api --method POST "repos/$repository/git/refs" \
        -f ref="refs/heads/$branch" -f sha="$default_sha" >/dev/null
    echo "Created $branch at $default_sha."
}

protect_review_branch() {
    local branch="$1"
    local payload
    payload="$(mktemp "${TMPDIR:-/tmp}/passvault-branch-policy.XXXXXX")"
    trap 'rm -f -- "$payload"' RETURN
    jq -n '{
      required_status_checks: {
        strict: true,
        contexts: ["Validate Gradle Wrapper", "Run Tests"]
      },
      enforce_admins: false,
      required_pull_request_reviews: {
        dismiss_stale_reviews: true,
        require_code_owner_reviews: false,
        required_approving_review_count: 1,
        require_last_push_approval: false
      },
      restrictions: null,
      required_linear_history: true,
      allow_force_pushes: false,
      allow_deletions: false,
      block_creations: false,
      required_conversation_resolution: true,
      lock_branch: false,
      allow_fork_syncing: true
    }' > "$payload"
    if [[ "$apply" == true ]]; then
        gh api --method PUT "repos/$repository/branches/$branch/protection" \
            --input "$payload" >/dev/null
        echo "Protected $branch with PR review and CI requirements."
    else
        echo "Would protect $branch with PR review and CI requirements."
    fi
    rm -f -- "$payload"
    trap - RETURN
}

protect_release_branch() {
    local payload
    payload="$(mktemp "${TMPDIR:-/tmp}/passvault-release-policy.XXXXXX")"
    trap 'rm -f -- "$payload"' RETURN
    jq -n '{
      required_status_checks: null,
      enforce_admins: false,
      required_pull_request_reviews: null,
      restrictions: null,
      required_linear_history: true,
      allow_force_pushes: false,
      allow_deletions: false,
      block_creations: false,
      required_conversation_resolution: false,
      lock_branch: false,
      allow_fork_syncing: false
    }' > "$payload"
    if [[ "$apply" == true ]]; then
        gh api --method PUT "repos/$repository/branches/release/protection" \
            --input "$payload" >/dev/null
        echo "Protected release against force pushes and deletion."
    else
        echo "Would protect release against force pushes and deletion."
    fi
    rm -f -- "$payload"
    trap - RETURN
}

ensure_branch testing
ensure_branch release
protect_review_branch main
protect_review_branch testing
protect_release_branch

if [[ "$apply" != true ]]; then
    echo "Dry run only. Re-run with --apply to change GitHub."
fi
