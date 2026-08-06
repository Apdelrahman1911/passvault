#!/usr/bin/env bash

set -euo pipefail

if [[ "${1:-}" != "--apply" ]]; then
    echo "Usage: $0 --apply" >&2
    echo "The apply flag is required because this command changes GitHub environments, variables, and secrets." >&2
    exit 2
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
private_root="$repository_root/release/private"
values_file="$private_root/values.env"
report_file="$private_root/generated/secret-upload-report.md"

cd "$repository_root"

if ! command -v gh >/dev/null 2>&1; then
    echo "GitHub CLI is required." >&2
    exit 1
fi

# This must be the first external-state check. No GitHub mutation occurs before it.
if ! gh auth status; then
    echo "GitHub authentication is invalid. Run: gh auth login -h github.com" >&2
    exit 1
fi

if ! git check-ignore -q release/private/values.env || [[ -n "$(git ls-files release/private)" ]]; then
    echo "release/private/ is not safely ignored and untracked; GitHub configuration stopped." >&2
    exit 1
fi

load_values() {
    local line key value
    while IFS= read -r line || [[ -n "$line" ]]; do
        line="${line%$'\r'}"
        [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
        [[ "$line" == *=* ]] || continue
        key="${line%%=*}"
        value="${line#*=}"
        [[ "$key" =~ ^[A-Z][A-Z0-9_]*$ ]] || continue
        case "$key" in
            PATH|IFS|BASH_ENV|ENV|SHELLOPTS|BASHOPTS|CDPATH|GLOBIGNORE|HOME|PWD|TMPDIR|LD_*|DYLD_*)
                echo "values.env contains an unsafe variable name." >&2
                exit 1
                ;;
        esac
        printf -v "$key" '%s' "$value"
    done < "$values_file"
}

load_values

authenticated_account="$(gh api user --jq .login)"
accessible_repository="$(gh repo view "${GITHUB_REPOSITORY:-}" --json nameWithOwner --jq .nameWithOwner)"

if [[ -z "${GITHUB_REPOSITORY:-}" || "$accessible_repository" != "$GITHUB_REPOSITORY" ]]; then
    echo "The configured GitHub repository does not match the accessible repository." >&2
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
echo "Deployment approver: ${GITHUB_DEPLOYMENT_APPROVER:-missing}"
read -r -p "Type $GITHUB_REPOSITORY to confirm this destination: " confirmed_repository
if [[ "$confirmed_repository" != "$GITHUB_REPOSITORY" ]]; then
    echo "GitHub destination was not confirmed; no GitHub settings were changed." >&2
    exit 1
fi

./scripts/validate-private-release-config.sh
ruby ./scripts/validate-app-store-connect.rb

reviewer_id="$(gh api "users/$GITHUB_DEPLOYMENT_APPROVER" --jq .id)"

configure_environment() {
    local environment_name="$1"
    local require_review="$2"
    local payload
    if [[ "$require_review" == "true" ]]; then
        payload="$(jq -n --argjson reviewer_id "$reviewer_id" '{
            wait_timer: 0,
            reviewers: [{ type: "User", id: $reviewer_id }],
            prevent_self_review: false,
            deployment_branch_policy: {
                protected_branches: false,
                custom_branch_policies: true
            }
        }')"
    else
        payload='{
            "wait_timer": 0,
            "reviewers": [],
            "prevent_self_review": false,
            "deployment_branch_policy": {
                "protected_branches": false,
                "custom_branch_policies": true
            }
        }'
    fi

    printf '%s' "$payload" |
        gh api --method PUT "repos/$GITHUB_REPOSITORY/environments/$environment_name" \
            --input - >/dev/null

    if ! gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
        --jq '.branch_policies[] | select(.name == "main" and .type == "branch") | .id' |
        grep -q .; then
        gh api --method POST \
            "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
            -f name=main -f type=branch >/dev/null
    fi
}

configure_environment mobile-beta false
configure_environment mobile-external-beta true
configure_environment mobile-production true

repo_variable_names=(
    PUBLISHER_NAME PUBLISHER_NAME_AR COPYRIGHT_HOLDER COPYRIGHT_HOLDER_AR
    COUNTRY_OR_JURISDICTION SUPPORT_EMAIL SECURITY_EMAIL PRIVACY_POLICY_URL SUPPORT_URL
    PROJECT_URL ANDROID_PACKAGE_NAME IOS_BUNDLE_ID APPLE_TEAM_ID APP_STORE_APP_ID
    APP_STORE_SKU ASC_ISSUER_ID ASC_KEY_ID GOOGLE_CLOUD_PROJECT_ID
    GOOGLE_CLOUD_PROJECT_NUMBER GITHUB_DEPLOYMENT_APPROVER
)
repo_variable_sources=(
    PUBLISHER_NAME_EN PUBLISHER_NAME_AR COPYRIGHT_HOLDER_EN COPYRIGHT_HOLDER_AR
    COUNTRY_OR_JURISDICTION SUPPORT_EMAIL SECURITY_EMAIL PRIVACY_POLICY_URL SUPPORT_URL
    PROJECT_URL ANDROID_PACKAGE_NAME IOS_BUNDLE_ID APPLE_TEAM_ID APP_STORE_APP_ID
    APP_STORE_SKU ASC_ISSUER_ID ASC_KEY_ID GOOGLE_CLOUD_PROJECT_ID
    GOOGLE_CLOUD_PROJECT_NUMBER GITHUB_DEPLOYMENT_APPROVER
)

for index in "${!repo_variable_names[@]}"; do
    variable_name="${repo_variable_names[$index]}"
    source_name="${repo_variable_sources[$index]}"
    printf '%s' "${!source_name}" |
        gh variable set "$variable_name" --repo "$GITHUB_REPOSITORY" >/dev/null
done

set_text_secret() {
    local secret_name="$1"
    local environment_name="$2"
    local value="$3"
    printf '%s' "$value" |
        gh secret set "$secret_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null
}

set_binary_secret() {
    local secret_name="$1"
    local environment_name="$2"
    local relative_path="$3"
    local absolute_path="$repository_root/$relative_path"
    openssl base64 -A -in "$absolute_path" |
        gh secret set "$secret_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null
}

set_metadata_archive_secret() {
    local environment_name="$1"
    tar -czf - -C "$private_root" \
        release-notes-en.md release-notes-ar.md \
        privacy-en.md privacy-ar.md \
        store-metadata-en.env store-metadata-ar.env \
        store-description-en.md store-description-ar.md |
        openssl base64 -A |
        gh secret set STORE_METADATA_ARCHIVE_BASE64 --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null
}

for environment_name in mobile-beta mobile-external-beta mobile-production; do
    set_binary_secret KEYSTORE_BASE64 "$environment_name" "$ANDROID_UPLOAD_KEYSTORE_FILE"
    set_text_secret KEYSTORE_PASSWORD "$environment_name" "$KEYSTORE_PASSWORD"
    set_text_secret KEY_ALIAS "$environment_name" "$KEY_ALIAS"
    set_text_secret KEY_PASSWORD "$environment_name" "$KEY_PASSWORD"
    set_binary_secret IOS_DISTRIBUTION_CERTIFICATE_BASE64 "$environment_name" \
        "$IOS_DISTRIBUTION_CERTIFICATE_FILE"
    set_text_secret IOS_DISTRIBUTION_CERTIFICATE_PASSWORD "$environment_name" \
        "$IOS_DISTRIBUTION_CERTIFICATE_PASSWORD"
    set_binary_secret IOS_PROVISIONING_PROFILE_BASE64 "$environment_name" \
        "$IOS_PROVISIONING_PROFILE_FILE"
    set_binary_secret ASC_PRIVATE_KEY_BASE64 "$environment_name" "$ASC_PRIVATE_KEY_FILE"
    set_metadata_archive_secret "$environment_name"
done

set_binary_secret TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64 mobile-external-beta \
    "$TESTFLIGHT_EXTERNAL_TESTERS_FILE"
set_binary_secret PLAY_CLOSED_TESTERS_BASE64 mobile-external-beta "$PLAY_CLOSED_TESTERS_FILE"

set_environment_variable() {
    local environment_name="$1"
    local variable_name="$2"
    local value="$3"
    printf '%s' "$value" |
        gh variable set "$variable_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null
}

set_environment_variable mobile-external-beta GOOGLE_CLOSED_TEST_GROUP "$GOOGLE_CLOSED_TEST_GROUP"
set_environment_variable mobile-external-beta TESTFLIGHT_EXTERNAL_GROUP "$TESTFLIGHT_EXTERNAL_GROUP"

for environment_name in mobile-beta mobile-external-beta mobile-production; do
    set_environment_variable "$environment_name" EXPORT_COMPLIANCE_STATUS "$EXPORT_COMPLIANCE_STATUS"
done

for environment_name in mobile-external-beta mobile-production; do
    set_text_secret APP_REVIEW_PHONE "$environment_name" "$APP_REVIEW_PHONE"
    set_environment_variable "$environment_name" APP_REVIEW_CONTACT_NAME "$APP_REVIEW_CONTACT_NAME"
    set_environment_variable "$environment_name" APP_REVIEW_EMAIL "$APP_REVIEW_EMAIL"
done

expected_shared_mobile_secrets=(
    KEYSTORE_BASE64 KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD
    IOS_DISTRIBUTION_CERTIFICATE_BASE64 IOS_DISTRIBUTION_CERTIFICATE_PASSWORD
    IOS_PROVISIONING_PROFILE_BASE64 ASC_PRIVATE_KEY_BASE64 STORE_METADATA_ARCHIVE_BASE64
)
expected_mobile_beta_secrets=("${expected_shared_mobile_secrets[@]}")
expected_external_secrets=(
    "${expected_shared_mobile_secrets[@]}" APP_REVIEW_PHONE
    TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64 PLAY_CLOSED_TESTERS_BASE64
)
expected_production_secrets=("${expected_shared_mobile_secrets[@]}" APP_REVIEW_PHONE)

repository_variables="$(gh variable list --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
mobile_beta_secrets="$(gh secret list --env mobile-beta --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
external_secrets="$(gh secret list --env mobile-external-beta --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
production_secrets="$(gh secret list --env mobile-production --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
external_variables="$(gh variable list --env mobile-external-beta --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
production_variables="$(gh variable list --env mobile-production --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
beta_variables="$(gh variable list --env mobile-beta --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"

verification_failures=0
declare -a github_rows=()

append_github_row() {
    github_rows+=("| $1 | $2 | $3 | \`$4\` | $5 | $6 |")
}

secret_source() {
    case "$1" in
        KEYSTORE_BASE64) echo "release/private/files/android-upload-keystore.jks" ;;
        KEYSTORE_PASSWORD) echo "values.env:KEYSTORE_PASSWORD" ;;
        KEY_ALIAS) echo "values.env:KEY_ALIAS" ;;
        KEY_PASSWORD) echo "values.env:KEY_PASSWORD" ;;
        IOS_DISTRIBUTION_CERTIFICATE_BASE64) echo "release/private/files/ios-distribution-certificate.p12" ;;
        IOS_DISTRIBUTION_CERTIFICATE_PASSWORD) echo "values.env:IOS_DISTRIBUTION_CERTIFICATE_PASSWORD" ;;
        IOS_PROVISIONING_PROFILE_BASE64) echo "release/private/files/ios-provisioning-profile.mobileprovision" ;;
        ASC_PRIVATE_KEY_BASE64) echo "release/private/files/app-store-connect-key.p8" ;;
        STORE_METADATA_ARCHIVE_BASE64) echo "release/private bilingual metadata files" ;;
        APP_REVIEW_PHONE) echo "values.env:APP_REVIEW_PHONE" ;;
        TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64) echo "release/private/testflight-external-testers.csv" ;;
        PLAY_CLOSED_TESTERS_BASE64) echo "release/private/play-closed-testers.txt" ;;
        *) echo "documented private input" ;;
    esac
}

environment_variable_source() {
    case "$1" in
        GOOGLE_CLOSED_TEST_GROUP) echo "values.env:GOOGLE_CLOSED_TEST_GROUP" ;;
        TESTFLIGHT_EXTERNAL_GROUP) echo "values.env:TESTFLIGHT_EXTERNAL_GROUP" ;;
        EXPORT_COMPLIANCE_STATUS) echo "values.env:EXPORT_COMPLIANCE_STATUS" ;;
        APP_REVIEW_CONTACT_NAME) echo "values.env:APP_REVIEW_CONTACT_NAME" ;;
        APP_REVIEW_EMAIL) echo "values.env:APP_REVIEW_EMAIL" ;;
        *) echo "values.env" ;;
    esac
}

for index in "${!repo_variable_names[@]}"; do
    variable_name="${repo_variable_names[$index]}"
    source_name="${repo_variable_sources[$index]}"
    if grep -Fxq "$variable_name" <<< "$repository_variables"; then
        append_github_row "$variable_name" "Repository variable" "Yes" \
            "values.env:$source_name" "Name exists at expected scope" "None"
    else
        verification_failures=$((verification_failures + 1))
        append_github_row "$variable_name" "Repository variable" "No" \
            "values.env:$source_name" "Missing after configuration" "Rerun the GitHub configuration script."
    fi
done

verify_environment_secrets() {
    local environment_name="$1"
    local actual_names="$2"
    shift 2
    local secret_name
    for secret_name in "$@"; do
        if grep -Fxq "$secret_name" <<< "$actual_names"; then
            append_github_row "$secret_name" "$environment_name secret" "Yes" \
                "$(secret_source "$secret_name")" "Name exists at expected scope" "None"
        else
            verification_failures=$((verification_failures + 1))
            append_github_row "$secret_name" "$environment_name secret" "No" \
                "$(secret_source "$secret_name")" "Missing after configuration" "Rerun the GitHub configuration script."
        fi
    done
}

verify_environment_secrets mobile-beta "$mobile_beta_secrets" "${expected_mobile_beta_secrets[@]}"
verify_environment_secrets mobile-external-beta "$external_secrets" "${expected_external_secrets[@]}"
verify_environment_secrets mobile-production "$production_secrets" "${expected_production_secrets[@]}"

verify_environment_variables() {
    local environment_name="$1"
    local actual_names="$2"
    shift 2
    local variable_name
    for variable_name in "$@"; do
        if grep -Fxq "$variable_name" <<< "$actual_names"; then
            append_github_row "$variable_name" "$environment_name variable" "Yes" \
                "$(environment_variable_source "$variable_name")" "Name exists at expected scope" "None"
        else
            verification_failures=$((verification_failures + 1))
            append_github_row "$variable_name" "$environment_name variable" "No" \
                "$(environment_variable_source "$variable_name")" "Missing after configuration" "Rerun the GitHub configuration script."
        fi
    done
}

verify_environment_variables mobile-external-beta "$external_variables" \
    GOOGLE_CLOSED_TEST_GROUP TESTFLIGHT_EXTERNAL_GROUP \
    APP_REVIEW_CONTACT_NAME APP_REVIEW_EMAIL EXPORT_COMPLIANCE_STATUS
verify_environment_variables mobile-production "$production_variables" \
    APP_REVIEW_CONTACT_NAME APP_REVIEW_EMAIL EXPORT_COMPLIANCE_STATUS
verify_environment_variables mobile-beta "$beta_variables" EXPORT_COMPLIANCE_STATUS

for environment_name in mobile-beta mobile-external-beta mobile-production; do
    if gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name" >/dev/null 2>&1; then
        append_github_row "$environment_name" "GitHub environment" "Yes" \
            "GitHub API" "Environment exists" "None"
    else
        verification_failures=$((verification_failures + 1))
        append_github_row "$environment_name" "GitHub environment" "No" \
            "GitHub API" "Environment missing" "Rerun environment configuration."
    fi
done

append_github_row "TESTFLIGHT_INTERNAL_EMAILS" "App Store Connect users" "Manual" \
    "values.env:TESTFLIGHT_INTERNAL_EMAILS" "Validated locally; not stored in GitHub" \
    "Add these addresses as App Store Connect users and internal testers."

for environment_name in mobile-beta mobile-external-beta mobile-production; do
    custom_policy="$(gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name" \
        --jq '.deployment_branch_policy.custom_branch_policies')"
    main_policy="$(gh api \
        "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
        --jq '.branch_policies[] | select(.name == "main" and .type == "branch") | .name')"
    if [[ "$custom_policy" == "true" && "$main_policy" == "main" ]]; then
        append_github_row "$environment_name main policy" "GitHub environment protection" "Yes" \
            "GitHub API" "Only the configured main branch is allowed" "None"
    else
        verification_failures=$((verification_failures + 1))
        append_github_row "$environment_name main policy" "GitHub environment protection" "No" \
            "GitHub API" "Main-only deployment policy missing" "Rerun environment configuration."
    fi
done

for environment_name in mobile-external-beta mobile-production; do
    reviewers="$(gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name" \
        --jq '.protection_rules[] | select(.type == "required_reviewers") | .reviewers[].reviewer.login')"
    if grep -Fxiq "$GITHUB_DEPLOYMENT_APPROVER" <<< "$reviewers"; then
        append_github_row "$environment_name reviewer" "GitHub environment protection" "Yes" \
            "values.env:GITHUB_DEPLOYMENT_APPROVER" "Required reviewer exists" "None"
    else
        verification_failures=$((verification_failures + 1))
        append_github_row "$environment_name reviewer" "GitHub environment protection" "No" \
            "values.env:GITHUB_DEPLOYMENT_APPROVER" "Required reviewer missing" \
            "Rerun environment configuration and verify the repository plan supports reviewers."
    fi
done

append_github_row "Google OIDC providers" "Environment variables" "No" \
    "Google Cloud project" "Not configured by this script" \
    "Run scripts/configure-google-oidc.sh --apply after gcloud authentication."

report_tmp="$(mktemp "$private_root/generated/github-report.XXXXXX")"
chmod 600 "$report_tmp"
{
    cat "$report_file"
    echo
    echo "## GitHub Configuration Verification"
    echo
    echo "| Name | Destination scope | Configured | Source | Validation status | Required action |"
    echo "|---|---|---:|---|---|---|"
    printf '%s\n' "${github_rows[@]}"
    echo
    echo "Authenticated account and target repository were verified before configuration."
    echo "Secret values cannot be read back from GitHub; only name and scope existence were verified."
} > "$report_tmp"
mv "$report_tmp" "$report_file"
chmod 600 "$report_file"

if (( verification_failures > 0 )); then
    echo "GitHub configuration completed with verification failures. Review the private report." >&2
    exit 1
fi

echo "GitHub environments, variables, and secret names were configured and independently verified."
