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
# shellcheck disable=SC1091 # Resolved from the runtime repository root.
source "$repository_root/scripts/lib/dotenv.sh"
# shellcheck disable=SC1091 # Resolved from the runtime repository root.
source "$repository_root/scripts/lib/pkcs12-validation.sh"

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

# Optional inputs must come from the ignored configuration file, not from an
# unrelated parent-shell environment inherited by this mutation script.
for optional_name in \
    GOOGLE_CLOSED_TEST_GROUP TESTFLIGHT_INTERNAL_EMAILS \
    TESTFLIGHT_EXTERNAL_TESTERS_FILE PLAY_CLOSED_TESTERS_FILE \
    WINDOWS_TIMESTAMP_URL \
    WINDOWS_CERTIFICATE_FILE WINDOWS_CERTIFICATE_PASSWORD WINDOWS_PFX_POLICY_CONFIRMATION \
    WINDOWS_AZURE_CLIENT_ID WINDOWS_AZURE_TENANT_ID WINDOWS_AZURE_SUBSCRIPTION_ID \
    WINDOWS_ARTIFACT_SIGNING_ENDPOINT WINDOWS_ARTIFACT_SIGNING_ACCOUNT \
    WINDOWS_ARTIFACT_SIGNING_PROFILE \
    WINDOWS_SIGNPATH_API_TOKEN WINDOWS_SIGNPATH_ORGANIZATION_ID \
    WINDOWS_SIGNPATH_PROJECT_SLUG WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG \
    WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG; do
    unset "$optional_name"
done
passvault_dotenv_load_file "$values_file"
./scripts/validate-private-release-config.sh

passvault_select_openssl || {
    echo "OpenSSL is required to derive pinned production-signing fingerprints." >&2
    exit 1
}
openssl_binary="$PASSVAULT_OPENSSL_BINARY"
signing_validation_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-signing-config.XXXXXX")"
cleanup_signing_validation() {
    unset PASSVAULT_WINDOWS_P12_PASSWORD PASSVAULT_MACOS_P12_PASSWORD \
        PASSVAULT_IOS_P12_PASSWORD
    if [[ "$signing_validation_root" == "${TMPDIR:-/tmp}"/passvault-signing-config.* &&
        -d "$signing_validation_root" ]]; then
        find "$signing_validation_root" -type f -exec chmod 600 {} + 2>/dev/null || true
        find "$signing_validation_root" -type f -exec rm -f -- {} + 2>/dev/null || true
        find "$signing_validation_root" -depth -type d -exec rmdir {} + 2>/dev/null || true
    fi
}
trap cleanup_signing_validation EXIT

metadata_archive_path="$signing_validation_root/store-metadata.tar.gz"
metadata_validation_root="$signing_validation_root/store-metadata-validation"
mkdir -m 700 "$metadata_validation_root"
ruby scripts/create-store-metadata-archive.rb "$private_root" "$metadata_archive_path" >/dev/null
ruby scripts/extract-store-metadata-archive.rb \
    "$metadata_archive_path" "$metadata_validation_root" >/dev/null

# Consumed through repo_variable_sources indirection when local-pfx is selected.
# shellcheck disable=SC2034
WINDOWS_SIGNING_CERTIFICATE_SHA256=""
if [[ "$WINDOWS_SIGNING_BACKEND" == local-pfx ]]; then
    export PASSVAULT_WINDOWS_P12_PASSWORD="$WINDOWS_CERTIFICATE_PASSWORD"
    mkdir -m 700 "$signing_validation_root/windows"
    passvault_validate_pkcs12 "$openssl_binary" \
        "$repository_root/$WINDOWS_CERTIFICATE_FILE" PASSVAULT_WINDOWS_P12_PASSWORD \
        "$signing_validation_root/windows" || {
            echo "The validated Windows PKCS#12 input could not be reopened safely." >&2
            exit 1
        }
    # shellcheck disable=SC2034
    WINDOWS_SIGNING_CERTIFICATE_SHA256="$($openssl_binary x509 \
        -in "$PASSVAULT_P12_CERTIFICATE_FILE" -fingerprint -sha256 -noout |
        sed 's/^[^=]*=//' | tr -cd '0-9A-Fa-f' | tr '[:lower:]' '[:upper:]')"
    derived_windows_publisher_name="$(ruby -ropenssl -e '
      certificate = OpenSSL::X509::Certificate.new(File.binread(ARGV.fetch(0)))
      common_name = certificate.subject.to_a.find { |name, _value, _type| name == "CN" }&.fetch(1)
      abort if common_name.nil? || common_name.empty?
      print common_name
    ' "$PASSVAULT_P12_CERTIFICATE_FILE")"
    if [[ "$derived_windows_publisher_name" != "$WINDOWS_EXPECTED_PUBLISHER_NAME" ]]; then
        echo "WINDOWS_EXPECTED_PUBLISHER_NAME does not match the local-PFX certificate." >&2
        exit 1
    fi
fi

export PASSVAULT_MACOS_P12_PASSWORD="$MACOS_CERTIFICATE_PASSWORD"
mkdir -m 700 "$signing_validation_root/macos"
passvault_validate_pkcs12 "$openssl_binary" \
    "$repository_root/$MACOS_CERTIFICATE_FILE" PASSVAULT_MACOS_P12_PASSWORD \
    "$signing_validation_root/macos" || {
        echo "The validated macOS PKCS#12 input could not be reopened safely." >&2
        exit 1
    }
# The following derived values are consumed through repo_variable_sources indirection.
# shellcheck disable=SC2034
MACOS_DEVELOPER_ID_CERTIFICATE_SHA256="$($openssl_binary x509 \
    -in "$PASSVAULT_P12_CERTIFICATE_FILE" -fingerprint -sha256 -noout |
    sed 's/^[^=]*=//' | tr -cd '0-9A-Fa-f' | tr '[:lower:]' '[:upper:]')"
# shellcheck disable=SC2034
MACOS_SIGNING_IDENTITY="$(ruby -ropenssl -e '
  certificate = OpenSSL::X509::Certificate.new(File.binread(ARGV.fetch(0)))
  common_name = certificate.subject.to_a.find { |name, _value, _type| name == "CN" }&.fetch(1)
  abort unless common_name&.start_with?("Developer ID Application:")
  print common_name
' "$PASSVAULT_P12_CERTIFICATE_FILE")"
# shellcheck disable=SC2034
MACOS_NOTARIZATION_TEAM_ID="$APPLE_TEAM_ID"

private_tester_path() {
    local relative_path="$1"
    local candidate_path
    if [[ "$relative_path" != release/private/* ]]; then
        return 1
    fi
    candidate_path="$repository_root/$relative_path"
    if ! ruby -I "$repository_root/scripts" -r lib/private_path -e \
        'exit(PassVault::PrivatePath.regular_file_within?(ARGV.fetch(0), ARGV.fetch(1)) ? 0 : 1)' \
        "$candidate_path" "$private_root"; then
        return 1
    fi
    printf '%s' "$candidate_path"
}

testflight_testers_ready=false
testflight_testers_path=""
if testflight_testers_path="$(private_tester_path "${TESTFLIGHT_EXTERNAL_TESTERS_FILE:-}")" &&
    "$repository_root/scripts/validate-mobile-tester-files.sh" testflight \
        "$testflight_testers_path" >/dev/null 2>&1; then
    testflight_testers_ready=true
fi

play_testers_ready=false
play_testers_path=""
if play_testers_path="$(private_tester_path "${PLAY_CLOSED_TESTERS_FILE:-}")" &&
    "$repository_root/scripts/validate-mobile-tester-files.sh" play \
        "$play_testers_path" >/dev/null 2>&1; then
    play_testers_ready=true
fi

email_pattern='^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'
play_group_ready=false
if [[ -n "${GOOGLE_CLOSED_TEST_GROUP:-}" && "$GOOGLE_CLOSED_TEST_GROUP" =~ $email_pattern &&
    "$GOOGLE_CLOSED_TEST_GROUP" != *.invalid ]]; then
    play_group_ready=true
fi

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

ruby ./scripts/validate-app-store-connect.rb

reviewer_id="$(gh api "users/$GITHUB_DEPLOYMENT_APPROVER" --jq .id)"

configure_environment() {
    local environment_name="$1"
    local require_review="$2"
    local allowed_branch="$3"
    local prevent_self_review="${4:-true}"
    local payload
    if [[ "$require_review" == "true" ]]; then
        if [[ "$prevent_self_review" != "true" && "$prevent_self_review" != "false" ]]; then
            echo "Invalid prevent-self-review policy for $environment_name." >&2
            exit 1
        fi
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
    else
        payload='{
            "wait_timer": 0,
            "reviewers": [],
            "prevent_self_review": false,
            "can_admins_bypass": false,
            "deployment_branch_policy": {
                "protected_branches": false,
                "custom_branch_policies": true
            }
        }'
    fi

    printf '%s' "$payload" |
        gh api --method PUT "repos/$GITHUB_REPOSITORY/environments/$environment_name" \
            --input - >/dev/null

    while IFS= read -r policy_id; do
        [[ -z "$policy_id" ]] && continue
        gh api --method DELETE \
            "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies/$policy_id" \
            >/dev/null
    done < <(gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
        --jq ".branch_policies[] | select(.name != \"$allowed_branch\" or .type != \"branch\") | .id")

    if ! gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
        --jq ".branch_policies[] | select(.name == \"$allowed_branch\" and .type == \"branch\") | .id" |
        grep -q .; then
        gh api --method POST \
            "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
            -f name="$allowed_branch" -f type=branch >/dev/null
    fi
}

configure_environment mobile-beta false testing false
configure_environment mobile-external-beta true testing true
configure_environment release-promotion true testing false
configure_environment mobile-production true release true
configure_environment play-access-beta false main false
configure_environment play-access-production true main true

# Branch promotion is an authorization boundary, not a credential boundary.
# Keep this environment empty even if a stale environment with the same name
# existed before the protected promotion workflow was installed.
while IFS= read -r secret_name; do
    [[ -z "$secret_name" ]] && continue
    gh secret delete "$secret_name" --env release-promotion \
        --repo "$GITHUB_REPOSITORY"
done < <(gh secret list --env release-promotion --repo "$GITHUB_REPOSITORY" \
    --json name --jq '.[].name')
while IFS= read -r variable_name; do
    [[ -z "$variable_name" ]] && continue
    gh variable delete "$variable_name" --env release-promotion \
        --repo "$GITHUB_REPOSITORY"
done < <(gh variable list --env release-promotion --repo "$GITHUB_REPOSITORY" \
    --json name --jq '.[].name')

# These values are consumed through the source-name indirection below.
# shellcheck disable=SC2034
ANDROID_UPLOAD_CERTIFICATE_SHA256="$(openssl x509 \
    -in "$repository_root/release/android/passvault-release-cert.pem" \
    -noout -fingerprint -sha256 | cut -d= -f2 | tr -d ':')"
# shellcheck disable=SC2034
export PASSVAULT_IOS_P12_PASSWORD="$IOS_DISTRIBUTION_CERTIFICATE_PASSWORD"
# shellcheck disable=SC2034
IOS_DISTRIBUTION_CERTIFICATE_SHA1="$($openssl_binary pkcs12 -legacy \
    -in "$repository_root/$IOS_DISTRIBUTION_CERTIFICATE_FILE" \
    -clcerts -nokeys -passin env:PASSVAULT_IOS_P12_PASSWORD 2>/dev/null |
    "$openssl_binary" x509 -noout -fingerprint -sha1 | cut -d= -f2 | tr -d ':')"
unset PASSVAULT_IOS_P12_PASSWORD

repo_variable_names=(
    PUBLISHER_NAME PUBLISHER_NAME_AR COPYRIGHT_HOLDER COPYRIGHT_HOLDER_AR
    COUNTRY_OR_JURISDICTION SUPPORT_EMAIL SECURITY_EMAIL PRIVACY_POLICY_URL SUPPORT_URL
    PROJECT_URL ANDROID_PACKAGE_NAME IOS_BUNDLE_ID APPLE_TEAM_ID APP_STORE_APP_ID
    APP_STORE_SKU ASC_ISSUER_ID ASC_KEY_ID GOOGLE_CLOUD_PROJECT_ID
    GOOGLE_CLOUD_PROJECT_NUMBER DEPLOYMENT_APPROVER ANDROID_UPLOAD_CERTIFICATE_SHA256
    IOS_DISTRIBUTION_CERTIFICATE_SHA1
    WINDOWS_SIGNING_BACKEND WINDOWS_EXPECTED_PUBLISHER_NAME
    MACOS_SIGNING_IDENTITY MACOS_NOTARIZATION_TEAM_ID
    MACOS_DEVELOPER_ID_CERTIFICATE_SHA256
)
repo_variable_sources=(
    PUBLISHER_NAME_EN PUBLISHER_NAME_AR COPYRIGHT_HOLDER_EN COPYRIGHT_HOLDER_AR
    COUNTRY_OR_JURISDICTION SUPPORT_EMAIL SECURITY_EMAIL PRIVACY_POLICY_URL SUPPORT_URL
    PROJECT_URL ANDROID_PACKAGE_NAME IOS_BUNDLE_ID APPLE_TEAM_ID APP_STORE_APP_ID
    APP_STORE_SKU ASC_ISSUER_ID ASC_KEY_ID GOOGLE_CLOUD_PROJECT_ID
    GOOGLE_CLOUD_PROJECT_NUMBER GITHUB_DEPLOYMENT_APPROVER ANDROID_UPLOAD_CERTIFICATE_SHA256
    IOS_DISTRIBUTION_CERTIFICATE_SHA1
    WINDOWS_SIGNING_BACKEND WINDOWS_EXPECTED_PUBLISHER_NAME
    MACOS_SIGNING_IDENTITY MACOS_NOTARIZATION_TEAM_ID
    MACOS_DEVELOPER_ID_CERTIFICATE_SHA256
)
if [[ "$WINDOWS_SIGNING_BACKEND" == local-pfx ]]; then
    repo_variable_names+=(WINDOWS_SIGNING_CERTIFICATE_SHA256)
    repo_variable_sources+=(WINDOWS_SIGNING_CERTIFICATE_SHA256)
else
    gh variable delete WINDOWS_SIGNING_CERTIFICATE_SHA256 \
        --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
fi
if [[ "$WINDOWS_SIGNING_BACKEND" == signpath ]]; then
    gh variable delete WINDOWS_TIMESTAMP_URL \
        --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
    for environment_name in \
        mobile-beta mobile-external-beta mobile-production \
        play-access-beta play-access-production; do
        gh variable delete WINDOWS_TIMESTAMP_URL --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
    done
else
    repo_variable_names+=(WINDOWS_TIMESTAMP_URL)
    repo_variable_sources+=(WINDOWS_TIMESTAMP_URL)
fi

windows_azure_variable_names=(
    WINDOWS_AZURE_CLIENT_ID WINDOWS_AZURE_TENANT_ID WINDOWS_AZURE_SUBSCRIPTION_ID
    WINDOWS_ARTIFACT_SIGNING_ENDPOINT WINDOWS_ARTIFACT_SIGNING_ACCOUNT
    WINDOWS_ARTIFACT_SIGNING_PROFILE
)
windows_signpath_variable_names=(
    WINDOWS_SIGNPATH_ORGANIZATION_ID WINDOWS_SIGNPATH_PROJECT_SLUG
    WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG
)
windows_remote_variable_names=(
    "${windows_azure_variable_names[@]}"
    "${windows_signpath_variable_names[@]}"
)

# Keep production signing-resource identifiers behind the production
# environment gate even though they are not credentials themselves.
for variable_name in "${windows_remote_variable_names[@]}"; do
    gh variable delete "$variable_name" --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
done

for index in "${!repo_variable_names[@]}"; do
    variable_name="${repo_variable_names[$index]}"
    source_name="${repo_variable_sources[$index]}"
    printf '%s' "${!source_name}" |
        gh variable set "$variable_name" --repo "$GITHUB_REPOSITORY" >/dev/null
done

# Repository-owned values must not be shadowed by stale environment copies.
for environment_name in \
    mobile-beta mobile-external-beta mobile-production \
    play-access-beta play-access-production; do
    environment_variable_names="$(gh variable list --env "$environment_name" \
        --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
    for variable_name in "${repo_variable_names[@]}"; do
        if grep -Fxq "$variable_name" <<< "$environment_variable_names"; then
            gh variable delete "$variable_name" --env "$environment_name" \
                --repo "$GITHUB_REPOSITORY" >/dev/null
        fi
    done
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
    if ! ruby -I "$repository_root/scripts" -r lib/private_path -e \
        'exit(PassVault::PrivatePath.regular_file_within?(ARGV.fetch(0), ARGV.fetch(1)) ? 0 : 1)' \
        "$absolute_path" "$private_root"; then
        echo "Refusing to upload an unsafe private file for $secret_name." >&2
        return 1
    fi
    "$openssl_binary" base64 -A -in "$absolute_path" |
        gh secret set "$secret_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null
}

set_metadata_archive_secret() {
    local environment_name="$1"
    "$openssl_binary" base64 -A -in "$metadata_archive_path" |
        gh secret set STORE_METADATA_ARCHIVE_BASE64 --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null
}

set_binary_secret KEYSTORE_BASE64 mobile-beta "$ANDROID_UPLOAD_KEYSTORE_FILE"
set_text_secret KEYSTORE_PASSWORD mobile-beta "$KEYSTORE_PASSWORD"
set_text_secret KEY_ALIAS mobile-beta "$KEY_ALIAS"
set_text_secret KEY_PASSWORD mobile-beta "$KEY_PASSWORD"
set_binary_secret IOS_DISTRIBUTION_CERTIFICATE_BASE64 mobile-beta \
    "$IOS_DISTRIBUTION_CERTIFICATE_FILE"
set_text_secret IOS_DISTRIBUTION_CERTIFICATE_PASSWORD mobile-beta \
    "$IOS_DISTRIBUTION_CERTIFICATE_PASSWORD"
set_binary_secret IOS_PROVISIONING_PROFILE_BASE64 mobile-beta \
    "$IOS_PROVISIONING_PROFILE_FILE"

all_desktop_production_secret_names=(
    WINDOWS_CERTIFICATE_BASE64 WINDOWS_CERTIFICATE_PASSWORD
    WINDOWS_SIGNPATH_API_TOKEN
    MACOS_CERTIFICATE_BASE64 MACOS_CERTIFICATE_PASSWORD MACOS_PROVISIONING_PROFILE_BASE64
    MACOS_NOTARIZATION_APPLE_ID MACOS_NOTARIZATION_PASSWORD
)
desktop_production_secret_names=(
    MACOS_CERTIFICATE_BASE64 MACOS_CERTIFICATE_PASSWORD MACOS_PROVISIONING_PROFILE_BASE64
    MACOS_NOTARIZATION_APPLE_ID MACOS_NOTARIZATION_PASSWORD
)
if [[ "$WINDOWS_SIGNING_BACKEND" == local-pfx ]]; then
    desktop_production_secret_names+=(WINDOWS_CERTIFICATE_BASE64 WINDOWS_CERTIFICATE_PASSWORD)
    set_binary_secret WINDOWS_CERTIFICATE_BASE64 mobile-production "$WINDOWS_CERTIFICATE_FILE"
    set_text_secret WINDOWS_CERTIFICATE_PASSWORD mobile-production "$WINDOWS_CERTIFICATE_PASSWORD"
    gh secret delete WINDOWS_SIGNPATH_API_TOKEN --env mobile-production \
        --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
elif [[ "$WINDOWS_SIGNING_BACKEND" == signpath ]]; then
    desktop_production_secret_names+=(WINDOWS_SIGNPATH_API_TOKEN)
    set_text_secret WINDOWS_SIGNPATH_API_TOKEN mobile-production "$WINDOWS_SIGNPATH_API_TOKEN"
    for secret_name in WINDOWS_CERTIFICATE_BASE64 WINDOWS_CERTIFICATE_PASSWORD; do
        gh secret delete "$secret_name" --env mobile-production \
            --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
    done
else
    for secret_name in \
        WINDOWS_CERTIFICATE_BASE64 WINDOWS_CERTIFICATE_PASSWORD WINDOWS_SIGNPATH_API_TOKEN; do
        gh secret delete "$secret_name" --env mobile-production \
            --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
    done
fi
set_binary_secret MACOS_CERTIFICATE_BASE64 mobile-production "$MACOS_CERTIFICATE_FILE"
set_text_secret MACOS_CERTIFICATE_PASSWORD mobile-production "$MACOS_CERTIFICATE_PASSWORD"
set_binary_secret MACOS_PROVISIONING_PROFILE_BASE64 mobile-production \
    "$MACOS_PROVISIONING_PROFILE_FILE"
set_text_secret MACOS_NOTARIZATION_APPLE_ID mobile-production "$MACOS_NOTARIZATION_APPLE_ID"
set_text_secret MACOS_NOTARIZATION_PASSWORD mobile-production "$MACOS_NOTARIZATION_PASSWORD"

mobile_scoped_secret_names=(
    KEYSTORE_BASE64 KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD
    IOS_DISTRIBUTION_CERTIFICATE_BASE64 IOS_DISTRIBUTION_CERTIFICATE_PASSWORD
    IOS_PROVISIONING_PROFILE_BASE64 ASC_PRIVATE_KEY_BASE64 STORE_METADATA_ARCHIVE_BASE64
    APP_REVIEW_PHONE TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64 PLAY_CLOSED_TESTERS_BASE64
    "${all_desktop_production_secret_names[@]}"
)

for environment_name in mobile-beta mobile-external-beta mobile-production; do
    set_binary_secret ASC_PRIVATE_KEY_BASE64 "$environment_name" "$ASC_PRIVATE_KEY_FILE"
    set_metadata_archive_secret "$environment_name"
done

upload_only_secret_names=(
    KEYSTORE_BASE64 KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD
    IOS_DISTRIBUTION_CERTIFICATE_BASE64 IOS_DISTRIBUTION_CERTIFICATE_PASSWORD
    IOS_PROVISIONING_PROFILE_BASE64
)
for environment_name in mobile-external-beta mobile-production; do
    for secret_name in "${upload_only_secret_names[@]}"; do
        gh secret delete "$secret_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
    done
done
for environment_name in mobile-beta mobile-external-beta; do
    for secret_name in "${all_desktop_production_secret_names[@]}"; do
        gh secret delete "$secret_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
    done
done

if [[ "$testflight_testers_ready" == "true" ]]; then
    set_binary_secret TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64 mobile-external-beta \
        "$TESTFLIGHT_EXTERNAL_TESTERS_FILE"
else
    gh secret delete TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64 --env mobile-external-beta \
        --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
fi
if [[ "$play_testers_ready" == "true" ]]; then
    set_binary_secret PLAY_CLOSED_TESTERS_BASE64 mobile-external-beta "$PLAY_CLOSED_TESTERS_FILE"
else
    gh secret delete PLAY_CLOSED_TESTERS_BASE64 --env mobile-external-beta \
        --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
fi

# Older setup revisions could leave these values at repository scope. Remove
# every duplicate after the protected environment copies have been installed.
for secret_name in "${mobile_scoped_secret_names[@]}"; do
    gh secret delete "$secret_name" --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
done
for secret_name in APP_REVIEW_PHONE TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64 \
    PLAY_CLOSED_TESTERS_BASE64; do
    gh secret delete "$secret_name" --env mobile-beta \
        --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
done
for secret_name in TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64 PLAY_CLOSED_TESTERS_BASE64; do
    gh secret delete "$secret_name" --env mobile-production \
        --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
done
for environment_name in play-access-beta play-access-production; do
    for secret_name in "${mobile_scoped_secret_names[@]}"; do
        gh secret delete "$secret_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
    done
done

set_environment_variable() {
    local environment_name="$1"
    local variable_name="$2"
    local value="$3"
    printf '%s' "$value" |
        gh variable set "$variable_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null
}

if [[ "$WINDOWS_SIGNING_BACKEND" == azure-artifact-signing ]]; then
    for variable_name in "${windows_azure_variable_names[@]}"; do
        set_environment_variable mobile-production "$variable_name" "${!variable_name}"
    done
elif [[ "$WINDOWS_SIGNING_BACKEND" == signpath ]]; then
    for variable_name in "${windows_signpath_variable_names[@]}"; do
        set_environment_variable mobile-production "$variable_name" "${!variable_name}"
    done
fi
for variable_name in "${windows_remote_variable_names[@]}"; do
    selected=false
    if [[ "$WINDOWS_SIGNING_BACKEND" == azure-artifact-signing ]] && \
        [[ " ${windows_azure_variable_names[*]} " == *" $variable_name "* ]]; then
        selected=true
    elif [[ "$WINDOWS_SIGNING_BACKEND" == signpath ]] && \
        [[ " ${windows_signpath_variable_names[*]} " == *" $variable_name "* ]]; then
        selected=true
    fi
    if [[ "$selected" != true ]]; then
        gh variable delete "$variable_name" --env mobile-production \
            --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
    fi
done
for environment_name in \
    mobile-beta mobile-external-beta play-access-beta play-access-production; do
    for variable_name in "${windows_remote_variable_names[@]}"; do
        gh variable delete "$variable_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
    done
done

if [[ "$play_group_ready" == "true" ]]; then
    set_environment_variable mobile-external-beta GOOGLE_CLOSED_TEST_GROUP "$GOOGLE_CLOSED_TEST_GROUP"
else
    gh variable delete GOOGLE_CLOSED_TEST_GROUP --env mobile-external-beta \
        --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
fi
set_environment_variable mobile-beta TESTFLIGHT_EXTERNAL_GROUP "$TESTFLIGHT_EXTERNAL_GROUP"
set_environment_variable mobile-external-beta TESTFLIGHT_EXTERNAL_GROUP "$TESTFLIGHT_EXTERNAL_GROUP"
set_environment_variable mobile-production TESTFLIGHT_EXTERNAL_GROUP "$TESTFLIGHT_EXTERNAL_GROUP"

for environment_name in mobile-beta mobile-external-beta mobile-production; do
    set_environment_variable "$environment_name" EXPORT_COMPLIANCE_STATUS "$EXPORT_COMPLIANCE_STATUS"
    set_environment_variable "$environment_name" IOS_FRANCE_AVAILABLE "$IOS_FRANCE_AVAILABLE"
done

for environment_name in mobile-external-beta mobile-production; do
    set_text_secret APP_REVIEW_PHONE "$environment_name" "$APP_REVIEW_PHONE"
    set_environment_variable "$environment_name" APP_REVIEW_CONTACT_NAME "$APP_REVIEW_CONTACT_NAME"
    set_environment_variable "$environment_name" APP_REVIEW_EMAIL "$APP_REVIEW_EMAIL"
done
# App-review contact data is unrelated to Google Play OIDC access checks and must not remain there.
gh secret delete APP_REVIEW_PHONE --env play-access-production \
    --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
for variable_name in APP_REVIEW_CONTACT_NAME APP_REVIEW_EMAIL; do
    gh variable delete "$variable_name" --env play-access-production \
        --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1 || true
done

expected_mobile_beta_secrets=(
    KEYSTORE_BASE64 KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD
    IOS_DISTRIBUTION_CERTIFICATE_BASE64 IOS_DISTRIBUTION_CERTIFICATE_PASSWORD
    IOS_PROVISIONING_PROFILE_BASE64 ASC_PRIVATE_KEY_BASE64 STORE_METADATA_ARCHIVE_BASE64
)
expected_external_secrets=(
    ASC_PRIVATE_KEY_BASE64 STORE_METADATA_ARCHIVE_BASE64 APP_REVIEW_PHONE
)
if [[ "$testflight_testers_ready" == "true" ]]; then
    expected_external_secrets+=(TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64)
fi
if [[ "$play_testers_ready" == "true" ]]; then
    expected_external_secrets+=(PLAY_CLOSED_TESTERS_BASE64)
fi
expected_production_secrets=(ASC_PRIVATE_KEY_BASE64 STORE_METADATA_ARCHIVE_BASE64 APP_REVIEW_PHONE)
expected_production_secrets+=("${desktop_production_secret_names[@]}")

repository_variables="$(gh variable list --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
repository_secrets="$(gh secret list --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
mobile_beta_secrets="$(gh secret list --env mobile-beta --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
external_secrets="$(gh secret list --env mobile-external-beta --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
production_secrets="$(gh secret list --env mobile-production --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
release_promotion_secrets="$(gh secret list --env release-promotion --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
external_variables="$(gh variable list --env mobile-external-beta --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
production_variables="$(gh variable list --env mobile-production --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
release_promotion_variables="$(gh variable list --env release-promotion --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
beta_variables="$(gh variable list --env mobile-beta --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
play_beta_secrets="$(gh secret list --env play-access-beta --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
play_production_secrets="$(gh secret list --env play-access-production --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
play_beta_variables="$(gh variable list --env play-access-beta --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"
play_production_variables="$(gh variable list --env play-access-production --repo "$GITHUB_REPOSITORY" --json name --jq '.[].name')"

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
        WINDOWS_CERTIFICATE_BASE64) echo "values.env:WINDOWS_CERTIFICATE_FILE" ;;
        WINDOWS_CERTIFICATE_PASSWORD) echo "values.env:WINDOWS_CERTIFICATE_PASSWORD" ;;
        WINDOWS_SIGNPATH_API_TOKEN) echo "values.env:WINDOWS_SIGNPATH_API_TOKEN" ;;
        MACOS_CERTIFICATE_BASE64) echo "values.env:MACOS_CERTIFICATE_FILE" ;;
        MACOS_CERTIFICATE_PASSWORD) echo "values.env:MACOS_CERTIFICATE_PASSWORD" ;;
        MACOS_PROVISIONING_PROFILE_BASE64) echo "values.env:MACOS_PROVISIONING_PROFILE_FILE" ;;
        MACOS_NOTARIZATION_APPLE_ID) echo "values.env:MACOS_NOTARIZATION_APPLE_ID" ;;
        MACOS_NOTARIZATION_PASSWORD) echo "values.env:MACOS_NOTARIZATION_PASSWORD" ;;
        *) echo "documented private input" ;;
    esac
}

environment_variable_source() {
    case "$1" in
        GOOGLE_CLOSED_TEST_GROUP) echo "values.env:GOOGLE_CLOSED_TEST_GROUP" ;;
        TESTFLIGHT_EXTERNAL_GROUP) echo "values.env:TESTFLIGHT_EXTERNAL_GROUP" ;;
        EXPORT_COMPLIANCE_STATUS) echo "values.env:EXPORT_COMPLIANCE_STATUS" ;;
        IOS_FRANCE_AVAILABLE) echo "values.env:IOS_FRANCE_AVAILABLE" ;;
        APP_REVIEW_CONTACT_NAME) echo "values.env:APP_REVIEW_CONTACT_NAME" ;;
        APP_REVIEW_EMAIL) echo "values.env:APP_REVIEW_EMAIL" ;;
        WINDOWS_AZURE_CLIENT_ID | WINDOWS_AZURE_TENANT_ID | WINDOWS_AZURE_SUBSCRIPTION_ID | \
            WINDOWS_ARTIFACT_SIGNING_ENDPOINT | WINDOWS_ARTIFACT_SIGNING_ACCOUNT | \
            WINDOWS_ARTIFACT_SIGNING_PROFILE | WINDOWS_SIGNPATH_ORGANIZATION_ID | \
            WINDOWS_SIGNPATH_PROJECT_SLUG | WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG | \
            WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG) echo "values.env:$1" ;;
        *) echo "values.env" ;;
    esac
}

for index in "${!repo_variable_names[@]}"; do
    variable_name="${repo_variable_names[$index]}"
    source_name="${repo_variable_sources[$index]}"
    actual_value="$(gh variable get "$variable_name" --repo "$GITHUB_REPOSITORY" 2>/dev/null || true)"
    if grep -Fxq "$variable_name" <<< "$repository_variables" &&
        [[ "$actual_value" == "${!source_name}" ]]; then
        append_github_row "$variable_name" "Repository variable" "Yes" \
            "values.env:$source_name" "Exact value exists at expected scope" "None"
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

verify_absent_environment_secrets() {
    local environment_name="$1"
    local actual_names="$2"
    shift 2
    local secret_name
    for secret_name in "$@"; do
        if grep -Fxq "$secret_name" <<< "$actual_names"; then
            verification_failures=$((verification_failures + 1))
            append_github_row "$secret_name" "$environment_name secret" "Yes" \
                "$(secret_source "$secret_name")" "Secret exists outside its approved scope" \
                "Rerun configuration and remove the stale environment secret."
        else
            append_github_row "$secret_name" "$environment_name secret" "No" \
                "$(secret_source "$secret_name")" "Absent as required" "None"
        fi
    done
}

verify_absent_environment_secrets mobile-beta "$mobile_beta_secrets" \
    APP_REVIEW_PHONE TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64 PLAY_CLOSED_TESTERS_BASE64 \
    "${all_desktop_production_secret_names[@]}"
verify_absent_environment_secrets mobile-external-beta "$external_secrets" \
    "${upload_only_secret_names[@]}" "${all_desktop_production_secret_names[@]}"
verify_absent_environment_secrets mobile-production "$production_secrets" \
    "${upload_only_secret_names[@]}" \
    TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64 PLAY_CLOSED_TESTERS_BASE64
case "$WINDOWS_SIGNING_BACKEND" in
local-pfx)
    inactive_windows_secret_names=(WINDOWS_SIGNPATH_API_TOKEN)
    ;;
signpath)
    inactive_windows_secret_names=(WINDOWS_CERTIFICATE_BASE64 WINDOWS_CERTIFICATE_PASSWORD)
    ;;
*)
    inactive_windows_secret_names=(
        WINDOWS_CERTIFICATE_BASE64 WINDOWS_CERTIFICATE_PASSWORD WINDOWS_SIGNPATH_API_TOKEN
    )
    ;;
esac
verify_absent_environment_secrets mobile-production "$production_secrets" \
    "${inactive_windows_secret_names[@]}"
verify_absent_environment_secrets play-access-beta "$play_beta_secrets" \
    "${mobile_scoped_secret_names[@]}"
verify_absent_environment_secrets play-access-production "$play_production_secrets" \
    "${mobile_scoped_secret_names[@]}"

for secret_name in "${mobile_scoped_secret_names[@]}"; do
    if grep -Fxq "$secret_name" <<< "$repository_secrets"; then
        verification_failures=$((verification_failures + 1))
        append_github_row "$secret_name duplicate" "Repository secret" "Yes" \
            "$(secret_source "$secret_name")" "Broader-scope duplicate still exists" \
            "Delete the repository-level secret and keep only protected environment copies."
    else
        append_github_row "$secret_name duplicate" "Repository secret" "No" \
            "$(secret_source "$secret_name")" "Absent as required" "None"
    fi
done

verify_environment_variables() {
    local environment_name="$1"
    local actual_names="$2"
    shift 2
    local variable_name actual_value
    for variable_name in "$@"; do
        actual_value="$(gh variable get "$variable_name" --env "$environment_name" \
            --repo "$GITHUB_REPOSITORY" 2>/dev/null || true)"
        if grep -Fxq "$variable_name" <<< "$actual_names" &&
            [[ "$actual_value" == "${!variable_name}" ]]; then
            append_github_row "$variable_name" "$environment_name variable" "Yes" \
                "$(environment_variable_source "$variable_name")" "Exact value exists at expected scope" "None"
        else
            verification_failures=$((verification_failures + 1))
            append_github_row "$variable_name" "$environment_name variable" "No" \
                "$(environment_variable_source "$variable_name")" "Missing after configuration" "Rerun the GitHub configuration script."
        fi
    done
}

expected_external_variables=(
    TESTFLIGHT_EXTERNAL_GROUP APP_REVIEW_CONTACT_NAME APP_REVIEW_EMAIL EXPORT_COMPLIANCE_STATUS
    IOS_FRANCE_AVAILABLE
)
if [[ "$play_group_ready" == "true" ]]; then
    expected_external_variables+=(GOOGLE_CLOSED_TEST_GROUP)
fi
verify_environment_variables mobile-external-beta "$external_variables" \
    "${expected_external_variables[@]}"
expected_production_variables=(
    TESTFLIGHT_EXTERNAL_GROUP APP_REVIEW_CONTACT_NAME APP_REVIEW_EMAIL EXPORT_COMPLIANCE_STATUS
    IOS_FRANCE_AVAILABLE
)
case "$WINDOWS_SIGNING_BACKEND" in
azure-artifact-signing)
    selected_remote_variable_names=("${windows_azure_variable_names[@]}")
    inactive_remote_variable_names=("${windows_signpath_variable_names[@]}")
    ;;
signpath)
    selected_remote_variable_names=("${windows_signpath_variable_names[@]}")
    inactive_remote_variable_names=("${windows_azure_variable_names[@]}")
    ;;
*)
    selected_remote_variable_names=()
    inactive_remote_variable_names=("${windows_remote_variable_names[@]}")
    ;;
esac
expected_production_variables+=("${selected_remote_variable_names[@]}")
verify_environment_variables mobile-production "$production_variables" \
    "${expected_production_variables[@]}"
verify_environment_variables mobile-beta "$beta_variables" \
    TESTFLIGHT_EXTERNAL_GROUP EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE

verify_absent_environment_variables() {
    local environment_name="$1"
    local actual_names="$2"
    shift 2
    local variable_name
    for variable_name in "$@"; do
        if grep -Fxq "$variable_name" <<< "$actual_names"; then
            verification_failures=$((verification_failures + 1))
            append_github_row "$variable_name override" "$environment_name variable" "Yes" \
                "Repository variable" "Environment value shadows the canonical repository value" \
                "Rerun configuration and remove the stale environment override."
        else
            append_github_row "$variable_name override" "$environment_name variable" "No" \
                "Repository variable" "Absent as required" "None"
        fi
    done
}

verify_absent_environment_variables mobile-beta "$beta_variables" "${repo_variable_names[@]}"
verify_absent_environment_variables mobile-external-beta "$external_variables" \
    "${repo_variable_names[@]}"
verify_absent_environment_variables mobile-production "$production_variables" \
    "${repo_variable_names[@]}"
verify_absent_environment_variables play-access-beta "$play_beta_variables" \
    "${repo_variable_names[@]}"
verify_absent_environment_variables play-access-production "$play_production_variables" \
    "${repo_variable_names[@]}"

verify_absent_environment_variables mobile-beta "$beta_variables" \
    "${windows_remote_variable_names[@]}"
verify_absent_environment_variables mobile-external-beta "$external_variables" \
    "${windows_remote_variable_names[@]}"
verify_absent_environment_variables play-access-beta "$play_beta_variables" \
    "${windows_remote_variable_names[@]}"
verify_absent_environment_variables play-access-production "$play_production_variables" \
    "${windows_remote_variable_names[@]}"
if ((${#inactive_remote_variable_names[@]} > 0)); then
    verify_absent_environment_variables mobile-production "$production_variables" \
        "${inactive_remote_variable_names[@]}"
fi

for variable_name in "${windows_remote_variable_names[@]}"; do
    if grep -Fxq "$variable_name" <<< "$repository_variables"; then
        verification_failures=$((verification_failures + 1))
        append_github_row "$variable_name duplicate" "Repository variable" "Yes" \
            "values.env:$variable_name" "Broader-scope remote-signing value still exists" \
            "Delete it and retain it only in mobile-production."
    else
        append_github_row "$variable_name duplicate" "Repository variable" "No" \
            "values.env:$variable_name" "Absent as required" "None"
    fi
done
if [[ "$WINDOWS_SIGNING_BACKEND" == signpath ]]; then
    if grep -Fxq WINDOWS_TIMESTAMP_URL <<< "$repository_variables" ||
        grep -Fxq WINDOWS_TIMESTAMP_URL <<< "$beta_variables" ||
        grep -Fxq WINDOWS_TIMESTAMP_URL <<< "$external_variables" ||
        grep -Fxq WINDOWS_TIMESTAMP_URL <<< "$production_variables" ||
        grep -Fxq WINDOWS_TIMESTAMP_URL <<< "$play_beta_variables" ||
        grep -Fxq WINDOWS_TIMESTAMP_URL <<< "$play_production_variables"; then
        verification_failures=$((verification_failures + 1))
        append_github_row "WINDOWS_TIMESTAMP_URL" "GitHub variables" "Yes" \
            "not used by SignPath" "Stale timestamp override exists" \
            "Delete it; the SignPath policy owns timestamp selection."
    else
        append_github_row "WINDOWS_TIMESTAMP_URL" "GitHub variables" "No" \
            "not used by SignPath" "Absent as required" "None"
    fi
fi

for environment_name in mobile-beta mobile-external-beta release-promotion mobile-production; do
    if gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name" >/dev/null 2>&1; then
        append_github_row "$environment_name" "GitHub environment" "Yes" \
            "GitHub API" "Environment exists" "None"
    else
        verification_failures=$((verification_failures + 1))
        append_github_row "$environment_name" "GitHub environment" "No" \
            "GitHub API" "Environment missing" "Rerun environment configuration."
    fi
done

if [[ "$testflight_testers_ready" != "true" ]]; then
    append_github_row "TESTFLIGHT_EXTERNAL_TESTERS_CSV_BASE64" "mobile-external-beta secret" \
        "Deferred" "release/private/testflight-external-testers.csv" \
        "No real tester rows; not uploaded" "Add real testers before external TestFlight."
fi
if [[ "$play_testers_ready" != "true" ]]; then
    append_github_row "PLAY_CLOSED_TESTERS_BASE64" "mobile-external-beta secret" \
        "Deferred" "release/private/play-closed-testers.txt" \
        "No real tester emails; not uploaded" "Add real testers before Play closed testing."
fi
if [[ "$play_group_ready" != "true" ]]; then
    append_github_row "GOOGLE_CLOSED_TEST_GROUP" "mobile-external-beta variable" \
        "Not required" "values.env:GOOGLE_CLOSED_TEST_GROUP" \
        "Not configured; Play Console email-list testing is preferred" \
        "None; populate the validated tester file before Play closed testing."
fi
append_github_row "TESTFLIGHT_INTERNAL_EMAILS" "App Store Connect users" "Manual/deferred" \
    "values.env:TESTFLIGHT_INTERNAL_EMAILS" "Not stored in GitHub" \
    "Add real App Store Connect users before assigning internal testers."

for environment_name in \
    mobile-beta mobile-external-beta release-promotion mobile-production \
    play-access-beta play-access-production; do
    case "$environment_name" in
    mobile-beta | mobile-external-beta | release-promotion)
        expected_branch=testing
        ;;
    mobile-production)
        expected_branch=release
        ;;
    play-access-beta | play-access-production)
        expected_branch=main
        ;;
    esac
    custom_policy="$(gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name" \
        --jq '.deployment_branch_policy.custom_branch_policies')"
    branch_policy="$(gh api \
        "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
        --jq ".branch_policies[] | select(.name == \"$expected_branch\" and .type == \"branch\") | .name")"
    branch_policy_count="$(gh api \
        "repos/$GITHUB_REPOSITORY/environments/$environment_name/deployment-branch-policies" \
        --jq '.branch_policies | length')"
    if [[ "$custom_policy" == "true" && "$branch_policy" == "$expected_branch" &&
        "$branch_policy_count" == 1 ]]; then
        append_github_row "$environment_name $expected_branch policy" \
            "GitHub environment protection" "Yes" \
            "GitHub API" "Only the configured $expected_branch branch is allowed" "None"
    else
        verification_failures=$((verification_failures + 1))
        append_github_row "$environment_name $expected_branch policy" \
            "GitHub environment protection" "No" \
            "GitHub API" "$expected_branch-only deployment policy missing" \
            "Rerun environment configuration."
    fi
done

for environment_name in \
    mobile-external-beta release-promotion mobile-production play-access-production; do
    reviewers="$(gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name" \
        --jq '.protection_rules[] | select(.type == "required_reviewers") | .reviewers[].reviewer.login')"
    reviewer_count="$(grep -c . <<< "$reviewers" || true)"
    prevent_self_review="$(gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name" \
        --jq '.protection_rules[] | select(.type == "required_reviewers") | .prevent_self_review')"
    expected_prevent_self_review=true
    if [[ "$environment_name" == "release-promotion" ]]; then
        expected_prevent_self_review=false
    fi
    if [[ "$reviewer_count" == 1 && \
        "$prevent_self_review" == "$expected_prevent_self_review" ]] && \
        grep -Fxiq "$GITHUB_DEPLOYMENT_APPROVER" <<< "$reviewers"; then
        append_github_row "$environment_name reviewer" "GitHub environment protection" "Yes" \
            "values.env:GITHUB_DEPLOYMENT_APPROVER" \
            "Required reviewer exists and prevent-self-review matches policy" "None"
    else
        verification_failures=$((verification_failures + 1))
        append_github_row "$environment_name reviewer" "GitHub environment protection" "No" \
            "values.env:GITHUB_DEPLOYMENT_APPROVER" \
            "Required reviewer or prevent-self-review policy mismatch" \
            "Rerun environment configuration and verify the intended reviewer policy."
    fi
done

for environment_name in release-promotion mobile-production; do
    can_admins_bypass="$(gh api "repos/$GITHUB_REPOSITORY/environments/$environment_name" \
        --jq '.can_admins_bypass')"
    if [[ "$can_admins_bypass" == "false" ]]; then
        append_github_row "$environment_name administrator bypass" \
            "GitHub environment protection" "Yes" \
            "GitHub environment settings" "Administrators cannot bypass deployment protection" "None"
    else
        verification_failures=$((verification_failures + 1))
        append_github_row "$environment_name administrator bypass" \
            "GitHub environment protection" "No" \
            "GitHub environment settings" "Administrators can bypass deployment protection" \
            "Disable administrator bypass in the environment settings, then rerun verification."
    fi
done

if [[ -z "$release_promotion_secrets" && -z "$release_promotion_variables" ]]; then
    append_github_row "release-promotion credential scope" "GitHub environment" "Yes" \
        "GitHub API" "No secrets or variables are present" "None"
else
    verification_failures=$((verification_failures + 1))
    append_github_row "release-promotion credential scope" "GitHub environment" "No" \
        "GitHub API" "Unexpected secrets or variables are present" \
        "Rerun environment configuration to remove every value."
fi

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
