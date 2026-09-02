#!/usr/bin/env bash

set -euo pipefail

if [[ "${1:-}" != "--apply" || -n "${2:-}" ]]; then
    echo "Usage: $0 --apply" >&2
    echo "Configures only release-promotion, mobile-production, and desktop-production." >&2
    exit 2
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
values_file="$repository_root/release/private/values.env"
# shellcheck disable=SC1091 # Resolved from the runtime repository root.
source "$repository_root/scripts/lib/dotenv.sh"

cd "$repository_root"

for command_name in gh jq; do
    if ! command -v "$command_name" >/dev/null 2>&1; then
        echo "$command_name is required." >&2
        exit 1
    fi
done
if ! gh auth status; then
    echo "GitHub authentication is invalid. Run: gh auth login -h github.com" >&2
    exit 1
fi

# A caller may provide these two non-secret values explicitly when the ignored
# mobile credential bundle is intentionally incomplete (for example, while
# Desktop publishing is deferred). The private file remains the normal source.
if [[ -f "$values_file" ]]; then
    passvault_dotenv_load_file "$values_file"
fi

if [[ ! "${GITHUB_REPOSITORY:-}" =~ ^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$ ]]; then
    echo "GITHUB_REPOSITORY must be an exact owner/repository name." >&2
    exit 1
fi
if [[ ! "${GITHUB_DEPLOYMENT_APPROVER:-}" =~ ^[A-Za-z0-9]([A-Za-z0-9-]{0,37}[A-Za-z0-9])?$ ]]; then
    echo "GITHUB_DEPLOYMENT_APPROVER must be an exact GitHub username." >&2
    exit 1
fi
if [[ -n "$(git ls-files release/private)" ]]; then
    echo "release/private must remain untracked." >&2
    exit 1
fi

authenticated_account="$(gh api user --jq .login)"
accessible_repository="$(gh repo view "$GITHUB_REPOSITORY" --json nameWithOwner --jq .nameWithOwner)"
if [[ "$accessible_repository" != "$GITHUB_REPOSITORY" ]]; then
    echo "The configured GitHub repository is not accessible." >&2
    exit 1
fi
remote_url="$(git remote get-url origin)"
case "$remote_url" in
    "https://github.com/$GITHUB_REPOSITORY.git"|"git@github.com:$GITHUB_REPOSITORY.git") ;;
    *)
        echo "The current Git remote does not match GITHUB_REPOSITORY." >&2
        exit 1
        ;;
esac

echo "Authenticated GitHub account: $authenticated_account"
echo "Target GitHub repository: $accessible_repository"
echo "Deployment approver: $GITHUB_DEPLOYMENT_APPROVER"
read -r -p "Type $GITHUB_REPOSITORY to confirm this destination: " confirmed_repository
if [[ "$confirmed_repository" != "$GITHUB_REPOSITORY" ]]; then
    echo "GitHub destination was not confirmed; no settings were changed." >&2
    exit 1
fi

reviewer_id="$(gh api "users/$GITHUB_DEPLOYMENT_APPROVER" --jq .id)"

configure_environment() {
    local environment_name="$1"
    local allowed_branch="$2"
    local prevent_self_review="$3"
    local payload
    payload="$(jq -n \
        --argjson reviewer_id "$reviewer_id" \
        --argjson prevent_self_review "$prevent_self_review" '{
          wait_timer: 0,
          reviewers: [{ type: "User", id: $reviewer_id }],
          prevent_self_review: $prevent_self_review,
          can_admins_bypass: false,
          deployment_branch_policy: {
            protected_branches: false,
            custom_branch_policies: true
          }
        }')"
    printf '%s' "$payload" |
        gh api --method PUT "repos/$GITHUB_REPOSITORY/environments/$environment_name" \
            --input - >/dev/null

    while IFS= read -r policy_id; do
        [[ -z "$policy_id" ]] && continue
        gh api --method DELETE \
            "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies/$policy_id" \
            >/dev/null
    done < <(gh api \
        "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
        --jq ".branch_policies[] | select(.name != \"$allowed_branch\" or .type != \"branch\") | .id")

    if ! gh api \
        "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
        --jq ".branch_policies[] | select(.name == \"$allowed_branch\" and .type == \"branch\") | .id" |
        grep -q .; then
        gh api --method POST \
            "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
            -f name="$allowed_branch" -f type=branch >/dev/null
    fi
}

verify_environment() {
    local environment_name="$1"
    local allowed_branch="$2"
    local prevent_self_review="$3"
    local environment_json branch_json
    environment_json="$(gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name")"
    branch_json="$(gh api \
        "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies")"
    jq -e \
        --arg reviewer "$GITHUB_DEPLOYMENT_APPROVER" \
        --argjson prevent_self_review "$prevent_self_review" '
          .can_admins_bypass == false and
          .deployment_branch_policy.protected_branches == false and
          .deployment_branch_policy.custom_branch_policies == true and
          ([.protection_rules[] | select(.type == "required_reviewers")] | length) == 1 and
          ([.protection_rules[] | select(.type == "required_reviewers")][0] |
            .prevent_self_review == $prevent_self_review and
            (.reviewers | length) == 1 and
            .reviewers[0].type == "User" and
            .reviewers[0].reviewer.login == $reviewer)
        ' <<< "$environment_json" >/dev/null
    jq -e --arg branch "$allowed_branch" '
      .total_count == 1 and
      (.branch_policies | length) == 1 and
      .branch_policies[0].type == "branch" and
      .branch_policies[0].name == $branch
    ' <<< "$branch_json" >/dev/null
}

# release-promotion intentionally permits its sole owner/reviewer to approve
# their own Candidate Readiness dispatch. The mobile request workflow instead
# hands off to github-actions[bot], preserving self-review prevention at the
# production Store boundary. Desktop has a distinct gate and branch policy.
configure_environment release-promotion testing false
configure_environment mobile-production main true
configure_environment desktop-production release true

verify_environment release-promotion testing false
verify_environment mobile-production main true
verify_environment desktop-production release true

echo "Release environment policies were configured and verified."
