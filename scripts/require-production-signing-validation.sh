#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <candidate-tag>" >&2
    exit 2
fi

candidate_tag="$1"
: "${GH_TOKEN:?GH_TOKEN is required}"
: "${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is required}"
: "${GITHUB_SHA:?GITHUB_SHA is required}"

if [[ ! "$candidate_tag" =~ ^v(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)-rc\.[1-9][0-9]*$ ]] ||
   [[ ! "$GITHUB_REPOSITORY" =~ ^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$ ]] ||
   [[ ! "$GITHUB_SHA" =~ ^[0-9a-f]{40}$ ]]; then
    echo "Candidate tag, repository, or exact source SHA is invalid." >&2
    exit 1
fi

for command_name in gh jq; do
    command -v "$command_name" >/dev/null 2>&1 || {
        echo "$command_name is required to verify production signing." >&2
        exit 1
    }
done

temporary_root="$(mktemp -d "${RUNNER_TEMP:-${TMPDIR:-/tmp}}/passvault-signing-gate.XXXXXX")"
cleanup() {
    if [[ "$temporary_root" == "${RUNNER_TEMP:-${TMPDIR:-/tmp}}"/passvault-signing-gate.* &&
        -d "$temporary_root" ]]; then
        find "$temporary_root" -type f -exec rm -f -- {} + 2>/dev/null || true
        rmdir "$temporary_root" 2>/dev/null || true
    fi
}
trap cleanup EXIT

artifact_name="production-signed-$candidate_tag"
gh api "/repos/$GITHUB_REPOSITORY/actions/artifacts?name=$artifact_name&per_page=100" \
    > "$temporary_root/artifacts.json"
run_id="$(
    jq -r --arg name "$artifact_name" --arg sha "$GITHUB_SHA" '
      .artifacts |
      map(select(
        .name == $name and
        .expired == false and
        .workflow_run.head_sha == $sha
      )) |
      sort_by(.created_at) | last | .workflow_run.id // empty
    ' "$temporary_root/artifacts.json"
)"
if [[ ! "$run_id" =~ ^[1-9][0-9]*$ ]]; then
    echo "No unexpired production signing validation exists for $candidate_tag at $GITHUB_SHA." >&2
    exit 1
fi

gh api "/repos/$GITHUB_REPOSITORY/actions/runs/$run_id" > "$temporary_root/run.json"
jq -e --arg sha "$GITHUB_SHA" '
  .conclusion == "success" and
  .event == "workflow_dispatch" and
  .head_branch == "release" and
  .head_sha == $sha and
  .path == ".github/workflows/production-signing-validation.yml"
' "$temporary_root/run.json" >/dev/null || {
    echo "The signing artifact was not produced by a successful protected validation run." >&2
    exit 1
}

printf 'PRODUCTION_SIGNING_VALIDATION=PASS\n'
printf 'PRODUCTION_SIGNING_VALIDATION_RUN_ID=%s\n' "$run_id"
