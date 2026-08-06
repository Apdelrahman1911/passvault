#!/usr/bin/env bash

set -euo pipefail

if [[ "${1:-}" != "--apply" ]]; then
    echo "Usage: $0 --apply" >&2
    echo "The apply flag is required because this command creates Google Cloud and GitHub resources." >&2
    exit 2
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
private_root="$repository_root/release/private"
values_file="$private_root/values.env"
report_file="$private_root/generated/secret-upload-report.md"

cd "$repository_root"

for command_name in gh gcloud jq; do
    if ! command -v "$command_name" >/dev/null 2>&1; then
        echo "$command_name is required." >&2
        exit 1
    fi
done

if ! gh auth status; then
    echo "GitHub authentication is invalid. Run: gh auth login -h github.com" >&2
    exit 1
fi

if ! git check-ignore -q release/private/values.env || [[ -n "$(git ls-files release/private)" ]]; then
    echo "release/private/ is not safely ignored and untracked; OIDC configuration stopped." >&2
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
if [[ "$authenticated_account" != "${GITHUB_DEPLOYMENT_APPROVER:-}" ||
    "$accessible_repository" != "${GITHUB_REPOSITORY:-}" ]]; then
    echo "GitHub account or repository confirmation failed." >&2
    exit 1
fi

active_google_account="$(gcloud auth list --filter=status:ACTIVE --limit=1 --format='value(account)')"
if [[ -z "$active_google_account" ]]; then
    echo "Google Cloud authentication is missing. Run: gcloud auth login" >&2
    exit 1
fi

./scripts/validate-private-release-config.sh
ruby ./scripts/validate-app-store-connect.rb

actual_project_number="$(gcloud projects describe "$GOOGLE_CLOUD_PROJECT_ID" --format='value(projectNumber)')"
if [[ "$actual_project_number" != "$GOOGLE_CLOUD_PROJECT_NUMBER" ]]; then
    echo "Google Cloud project ID and project number do not match." >&2
    exit 1
fi

echo "Confirmed GitHub account: $authenticated_account"
echo "Confirmed GitHub repository: $accessible_repository"
echo "Confirmed Google account: $active_google_account"
echo "Confirmed Google Cloud project: $GOOGLE_CLOUD_PROJECT_ID ($GOOGLE_CLOUD_PROJECT_NUMBER)"
read -r -p "Type $GITHUB_REPOSITORY to confirm this destination: " confirmed_repository
if [[ "$confirmed_repository" != "$GITHUB_REPOSITORY" ]]; then
    echo "Google/GitHub destination was not confirmed; no resources were changed." >&2
    exit 1
fi

gcloud services enable \
    androidpublisher.googleapis.com \
    iamcredentials.googleapis.com \
    sts.googleapis.com \
    --project "$GOOGLE_CLOUD_PROJECT_ID" >/dev/null

pool_id="github-passvault"
beta_provider_id="github-passvault-beta"
production_provider_id="github-passvault-production"
beta_service_account_id="passvault-play-beta"
production_service_account_id="passvault-play-prod"

if ! gcloud iam workload-identity-pools describe "$pool_id" --location=global \
    --project "$GOOGLE_CLOUD_PROJECT_ID" >/dev/null 2>&1; then
    gcloud iam workload-identity-pools create "$pool_id" --location=global \
        --project "$GOOGLE_CLOUD_PROJECT_ID" \
        --display-name="PassVault GitHub Actions" >/dev/null
fi

attribute_mapping='google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.environment=assertion.environment'
beta_condition="assertion.repository=='$GITHUB_REPOSITORY' && (assertion.environment=='mobile-beta' || assertion.environment=='mobile-external-beta')"
production_condition="assertion.repository=='$GITHUB_REPOSITORY' && assertion.environment=='mobile-production'"

upsert_provider() {
    local provider_id="$1"
    local display_name="$2"
    local condition="$3"
    if gcloud iam workload-identity-pools providers describe "$provider_id" \
        --workload-identity-pool="$pool_id" --location=global \
        --project "$GOOGLE_CLOUD_PROJECT_ID" >/dev/null 2>&1; then
        gcloud iam workload-identity-pools providers update-oidc "$provider_id" \
            --workload-identity-pool="$pool_id" --location=global \
            --project "$GOOGLE_CLOUD_PROJECT_ID" \
            --issuer-uri="https://token.actions.githubusercontent.com" \
            --attribute-mapping="$attribute_mapping" \
            --attribute-condition="$condition" \
            --display-name="$display_name" >/dev/null
    else
        gcloud iam workload-identity-pools providers create-oidc "$provider_id" \
            --workload-identity-pool="$pool_id" --location=global \
            --project "$GOOGLE_CLOUD_PROJECT_ID" \
            --issuer-uri="https://token.actions.githubusercontent.com" \
            --attribute-mapping="$attribute_mapping" \
            --attribute-condition="$condition" \
            --display-name="$display_name" >/dev/null
    fi
}

upsert_provider "$beta_provider_id" "PassVault GitHub beta" "$beta_condition"
upsert_provider "$production_provider_id" "PassVault GitHub production" "$production_condition"

ensure_service_account() {
    local account_id="$1"
    local display_name="$2"
    local email="$account_id@$GOOGLE_CLOUD_PROJECT_ID.iam.gserviceaccount.com"
    if ! gcloud iam service-accounts describe "$email" --project "$GOOGLE_CLOUD_PROJECT_ID" \
        >/dev/null 2>&1; then
        gcloud iam service-accounts create "$account_id" --project "$GOOGLE_CLOUD_PROJECT_ID" \
            --display-name="$display_name" >/dev/null
    fi
}

ensure_service_account "$beta_service_account_id" "PassVault Play beta publisher"
ensure_service_account "$production_service_account_id" "PassVault Play production publisher"

beta_service_account="$beta_service_account_id@$GOOGLE_CLOUD_PROJECT_ID.iam.gserviceaccount.com"
production_service_account="$production_service_account_id@$GOOGLE_CLOUD_PROJECT_ID.iam.gserviceaccount.com"

validate_keyless_service_account() {
    local service_account="$1"
    local user_managed_keys project_roles
    user_managed_keys="$(gcloud iam service-accounts keys list \
        --iam-account="$service_account" --managed-by=user \
        --project "$GOOGLE_CLOUD_PROJECT_ID" --format='value(name)')"
    if [[ -n "$user_managed_keys" ]]; then
        echo "A release service account has a user-managed key. Revoke it before continuing." >&2
        exit 1
    fi
    project_roles="$(gcloud projects get-iam-policy "$GOOGLE_CLOUD_PROJECT_ID" \
        --flatten='bindings[].members' \
        --filter="bindings.members:serviceAccount:$service_account" \
        --format='value(bindings.role)')"
    if [[ -n "$project_roles" ]]; then
        echo "A release service account has project-level roles. Remove them before continuing." >&2
        exit 1
    fi
}

validate_keyless_service_account "$beta_service_account"
validate_keyless_service_account "$production_service_account"

principal_prefix="principalSet://iam.googleapis.com/projects/$GOOGLE_CLOUD_PROJECT_NUMBER/locations/global/workloadIdentityPools/$pool_id/attribute.environment"

for environment_name in mobile-beta mobile-external-beta; do
    gcloud iam service-accounts add-iam-policy-binding "$beta_service_account" \
        --project "$GOOGLE_CLOUD_PROJECT_ID" \
        --role=roles/iam.workloadIdentityUser \
        --member="$principal_prefix/$environment_name" >/dev/null
done

gcloud iam service-accounts add-iam-policy-binding "$production_service_account" \
    --project "$GOOGLE_CLOUD_PROJECT_ID" \
    --role=roles/iam.workloadIdentityUser \
    --member="$principal_prefix/mobile-production" >/dev/null

beta_provider="projects/$GOOGLE_CLOUD_PROJECT_NUMBER/locations/global/workloadIdentityPools/$pool_id/providers/$beta_provider_id"
production_provider="projects/$GOOGLE_CLOUD_PROJECT_NUMBER/locations/global/workloadIdentityPools/$pool_id/providers/$production_provider_id"

set_environment_variable() {
    local environment_name="$1"
    local variable_name="$2"
    local value="$3"
    printf '%s' "$value" |
        gh variable set "$variable_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null
}

for environment_name in mobile-beta mobile-external-beta; do
    set_environment_variable "$environment_name" GOOGLE_WORKLOAD_IDENTITY_PROVIDER "$beta_provider"
    set_environment_variable "$environment_name" GOOGLE_SERVICE_ACCOUNT "$beta_service_account"
done

set_environment_variable mobile-production GOOGLE_WORKLOAD_IDENTITY_PROVIDER "$production_provider"
set_environment_variable mobile-production GOOGLE_SERVICE_ACCOUNT "$production_service_account"

verification_failures=0
declare -a oidc_rows=()

append_oidc_row() {
    oidc_rows+=("| $1 | $2 | $3 | \`$4\` | $5 | $6 |")
}

for provider_spec in \
    "$beta_provider_id|$GITHUB_REPOSITORY|mobile-beta|mobile-external-beta" \
    "$production_provider_id|$GITHUB_REPOSITORY|mobile-production"; do
    IFS='|' read -r provider_id expected_repository expected_environment extra_environment \
        <<< "$provider_spec"
    provider_condition_actual="$(gcloud iam workload-identity-pools providers describe "$provider_id" \
        --workload-identity-pool="$pool_id" --location=global \
        --project "$GOOGLE_CLOUD_PROJECT_ID" --format='value(attributeCondition)' 2>/dev/null || true)"
    if [[ "$provider_condition_actual" == *"$expected_repository"* &&
        "$provider_condition_actual" == *"$expected_environment"* &&
        ( -z "$extra_environment" || "$provider_condition_actual" == *"$extra_environment"* ) ]]; then
        append_oidc_row "$provider_id" "Google Workload Identity provider" "Yes" \
            "Google Cloud project" "Repository/environment restriction verified" "None"
    else
        verification_failures=$((verification_failures + 1))
        append_oidc_row "$provider_id" "Google Workload Identity provider" "No" \
            "Google Cloud project" "Provider restriction missing or incorrect" "Rerun OIDC configuration."
    fi
done

for service_account in "$beta_service_account" "$production_service_account"; do
    if gcloud iam service-accounts describe "$service_account" \
        --project "$GOOGLE_CLOUD_PROJECT_ID" >/dev/null 2>&1; then
        append_oidc_row "$service_account" "Google service account" "Yes" \
            "Google Cloud project" "Service account exists; no JSON key created" \
            "Invite this non-secret email to the Play Console app."
    else
        verification_failures=$((verification_failures + 1))
        append_oidc_row "$service_account" "Google service account" "No" \
            "Google Cloud project" "Service account missing" "Rerun OIDC configuration."
    fi
done


verify_workload_binding() {
    local service_account="$1"
    local environment_name="$2"
    local expected_member="$principal_prefix/$environment_name"
    local actual_members
    actual_members="$(gcloud iam service-accounts get-iam-policy "$service_account" \
        --project "$GOOGLE_CLOUD_PROJECT_ID" \
        --flatten='bindings[].members' \
        --filter='bindings.role:roles/iam.workloadIdentityUser' \
        --format='value(bindings.members)')"
    if grep -Fxq "$expected_member" <<< "$actual_members"; then
        append_oidc_row "$environment_name identity binding" "Google service-account IAM" "Yes" \
            "GitHub environment claim" "Workload Identity User binding exists" "None"
    else
        verification_failures=$((verification_failures + 1))
        append_oidc_row "$environment_name identity binding" "Google service-account IAM" "No" \
            "GitHub environment claim" "Expected binding missing" "Rerun OIDC configuration."
    fi
}

verify_workload_binding "$beta_service_account" mobile-beta
verify_workload_binding "$beta_service_account" mobile-external-beta
verify_workload_binding "$production_service_account" mobile-production

for service_account in "$beta_service_account" "$production_service_account"; do
    validate_keyless_service_account "$service_account"
    append_oidc_row "$service_account key policy" "Google service account" "Yes" \
        "Google IAM" "No user-managed keys or project-level roles" "None"
done

for environment_name in mobile-beta mobile-external-beta mobile-production; do
    environment_variables="$(gh variable list --env "$environment_name" \
        --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
    for variable_name in GOOGLE_WORKLOAD_IDENTITY_PROVIDER GOOGLE_SERVICE_ACCOUNT; do
        if grep -Fxq "$variable_name" <<< "$environment_variables"; then
            append_oidc_row "$variable_name" "$environment_name variable" "Yes" \
                "OIDC resource identifier" "Name exists at expected scope" "None"
        else
            verification_failures=$((verification_failures + 1))
            append_oidc_row "$variable_name" "$environment_name variable" "No" \
                "OIDC resource identifier" "Missing after configuration" "Rerun OIDC configuration."
        fi
    done
done

append_oidc_row "$beta_service_account" "Play Console app permissions" "No" \
    "Users and permissions" "External action required" \
    "Invite with Release apps to testing tracks and Manage testing tracks permissions."
append_oidc_row "$production_service_account" "Play Console app permissions" "No" \
    "Users and permissions" "External action required" \
    "Invite with Release to production and Manage store presence permissions."

report_tmp="$(mktemp "$private_root/generated/oidc-report.XXXXXX")"
chmod 600 "$report_tmp"
{
    cat "$report_file"
    echo
    echo "## Google OIDC Verification"
    echo
    echo "| Name | Destination scope | Configured | Source | Validation status | Required action |"
    echo "|---|---|---:|---|---|---|"
    printf '%s\n' "${oidc_rows[@]}"
    echo
    echo "No Google service-account JSON key was created or stored."
} > "$report_tmp"
mv "$report_tmp" "$report_file"
chmod 600 "$report_file"

if (( verification_failures > 0 )); then
    echo "Google OIDC configuration completed with verification failures. Review the private report." >&2
    exit 1
fi

echo "Google OIDC resources and GitHub environment identifiers were configured and verified."
