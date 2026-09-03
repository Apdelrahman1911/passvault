#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
private_root="$repository_root/release/private"
values_file="$private_root/values.env"
report_file="$private_root/generated/secret-upload-report.md"
# shellcheck disable=SC1091 # Resolved from the runtime repository root.
source "$repository_root/scripts/lib/dotenv.sh"
# shellcheck disable=SC1091 # Resolved from the runtime repository root.
source "$repository_root/scripts/lib/pkcs12-validation.sh"
temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-release-validation.XXXXXX")"
chmod 700 "$temporary_root"

private_file_is_safe() {
    ruby -I "$repository_root/scripts" -r lib/private_path -e \
        'exit(PassVault::PrivatePath.regular_file_within?(ARGV.fetch(0), ARGV.fetch(1)) ? 0 : 1)' \
        "$1" "$private_root"
}

private_directory_is_safe() {
    ruby -I "$repository_root/scripts" -r lib/private_path -e \
        'exit(PassVault::PrivatePath.directory_within?(ARGV.fetch(0), ARGV.fetch(1)) ? 0 : 1)' \
        "$1" "$private_root"
}

cleanup() {
    if [[ -d "$temporary_root" ]]; then
        find "$temporary_root" -type f -exec chmod 600 {} + 2>/dev/null || true
        find "$temporary_root" -type f -exec rm -f -- {} + 2>/dev/null || true
        find "$temporary_root" -depth -type d -exec rmdir {} + 2>/dev/null || true
    fi
    unset PASSVAULT_VALIDATION_STOREPASS
    unset PASSVAULT_VALIDATION_KEYPASS
    unset PASSVAULT_VALIDATION_P12PASS
}
trap cleanup EXIT

cd "$repository_root"

if ! git check-ignore -q release/private/values.env; then
    echo "release/private/ is not ignored; validation stopped before reading private inputs." >&2
    exit 1
fi

if [[ -n "$(git ls-files release/private)" ]]; then
    echo "A path below release/private/ is tracked; validation stopped." >&2
    exit 1
fi

if ! private_file_is_safe "$values_file"; then
    echo "release/private/values.env is missing or traverses an unsafe path." >&2
    exit 1
fi
if ! ruby -e '
  contents = File.binread(ARGV.fetch(0)).force_encoding(Encoding::UTF_8)
  abort unless contents.valid_encoding? && !contents.include?("\0") &&
    contents.bytesize <= 1024 * 1024
' "$values_file" >/dev/null 2>&1; then
    echo "release/private/values.env must be valid UTF-8 without NUL bytes and at most 1 MiB." >&2
    exit 1
fi

declare -a report_rows=()
failure_count=0

sanitize_report_text() {
    local value="$1"
    value="${value//$'\n'/ }"
    value="${value//$'\r'/ }"
    value="${value//|/\/}"
    value="${value//\`/ }"
    printf '%s' "$value"
}

record_result() {
    local name scope configured source validation action
    name="$(sanitize_report_text "$1")"
    scope="$(sanitize_report_text "$2")"
    configured="$(sanitize_report_text "$3")"
    source="$(sanitize_report_text "$4")"
    validation="$(sanitize_report_text "$5")"
    action="$(sanitize_report_text "$6")"
    report_rows+=("| $name | $scope | $configured | \`$source\` | $validation | $action |")
}

fail_result() {
    failure_count=$((failure_count + 1))
    record_result "$@"
}

required_values=(
    PUBLISHER_NAME_EN PUBLISHER_NAME_AR COPYRIGHT_HOLDER_EN COPYRIGHT_HOLDER_AR
    COUNTRY_OR_JURISDICTION SUPPORT_EMAIL SECURITY_EMAIL PROJECT_URL
    PRIVACY_POLICY_URL SUPPORT_URL ANDROID_PACKAGE_NAME IOS_BUNDLE_ID APP_STORE_SKU
    APP_STORE_APP_ID APPLE_TEAM_ID ASC_ISSUER_ID ASC_KEY_ID APP_REVIEW_CONTACT_NAME
    APP_REVIEW_EMAIL APP_REVIEW_PHONE GOOGLE_CLOUD_PROJECT_ID GOOGLE_CLOUD_PROJECT_NUMBER
    GITHUB_REPOSITORY GITHUB_DEPLOYMENT_APPROVER KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD
    IOS_DISTRIBUTION_CERTIFICATE_PASSWORD TESTFLIGHT_EXTERNAL_GROUP
    WINDOWS_SIGNING_BACKEND WINDOWS_EXPECTED_PUBLISHER_NAME
    MACOS_CERTIFICATE_PASSWORD
    EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE
    RELEASE_NOTES_EN_FILE RELEASE_NOTES_AR_FILE PRIVACY_TEXT_EN_FILE PRIVACY_TEXT_AR_FILE
    ANDROID_UPLOAD_KEYSTORE_FILE IOS_DISTRIBUTION_CERTIFICATE_FILE
    IOS_PROVISIONING_PROFILE_FILE ASC_PRIVATE_KEY_FILE STORE_METADATA_EN_FILE
    STORE_METADATA_AR_FILE STORE_DESCRIPTION_EN_FILE STORE_DESCRIPTION_AR_FILE
    MACOS_CERTIFICATE_FILE MACOS_PROVISIONING_PROFILE_FILE
)
optional_values=(
    GOOGLE_CLOSED_TEST_GROUP TESTFLIGHT_INTERNAL_EMAILS
    TESTFLIGHT_EXTERNAL_TESTERS_FILE PLAY_CLOSED_TESTERS_FILE
    PLAY_USERS_FILE PLAY_SERVICE_ACCOUNT_PERMISSION_MODE
    WINDOWS_TIMESTAMP_URL
    WINDOWS_CERTIFICATE_FILE WINDOWS_CERTIFICATE_PASSWORD WINDOWS_PFX_POLICY_CONFIRMATION
    WINDOWS_AZURE_CLIENT_ID WINDOWS_AZURE_TENANT_ID WINDOWS_AZURE_SUBSCRIPTION_ID
    WINDOWS_ARTIFACT_SIGNING_ENDPOINT WINDOWS_ARTIFACT_SIGNING_ACCOUNT
    WINDOWS_ARTIFACT_SIGNING_PROFILE
    WINDOWS_SIGNPATH_API_TOKEN WINDOWS_SIGNPATH_ORGANIZATION_ID
    WINDOWS_SIGNPATH_PROJECT_SLUG WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG
    WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG
)

# The ignored values file is the sole source of release configuration. Ambient
# variables must not silently fill a missing declaration and then be uploaded.
for name in "${required_values[@]}" "${optional_values[@]}"; do
    unset "$name"
done

load_values() {
    local line loaded_keys=$'\n'
    while IFS= read -r line || [[ -n "$line" ]]; do
        if ! passvault_dotenv_parse_line "$line"; then
            fail_result "values.env" "Local only" "No" "values.env" \
                "Invalid dotenv entry: $PASSVAULT_DOTENV_ERROR" \
                "Use a literal NAME=value entry; matching single or double quotes are supported."
            continue
        fi
        if [[ "$PASSVAULT_DOTENV_KIND" == "entry" ]]; then
            if [[ "$loaded_keys" == *$'\n'"$PASSVAULT_DOTENV_KEY"$'\n'* ]]; then
                fail_result "values.env" "Local only" "No" "values.env" \
                    "Duplicate dotenv key: $PASSVAULT_DOTENV_KEY" \
                    "Keep exactly one literal entry for each setting."
                continue
            fi
            loaded_keys+="$PASSVAULT_DOTENV_KEY"$'\n'
            printf -v "$PASSVAULT_DOTENV_KEY" '%s' "$PASSVAULT_DOTENV_VALUE"
        fi
    done < "$values_file"
}

load_values

if ! passvault_select_openssl; then
    echo "OpenSSL is required for release validation." >&2
    exit 1
fi
openssl_binary="$PASSVAULT_OPENSSL_BINARY"

for name in "${required_values[@]}"; do
    if [[ -z "${!name:-}" ]]; then
        fail_result "$name" "Input validation" "No" "values.env:$name" "Missing" \
            "Fill this value in release/private/values.env."
    else
        record_result "$name" "Input validation" "Not uploaded" "values.env:$name" "Present" "None"
    fi
done

email_pattern='^[^[:space:]@,]+@[^[:space:]@,]+\.[^[:space:]@,]+$'
for name in SUPPORT_EMAIL SECURITY_EMAIL APP_REVIEW_EMAIL; do
    value="${!name:-}"
    if [[ -n "$value" && ( ! "$value" =~ $email_pattern || "$value" == *.invalid ) ]]; then
        fail_result "$name" "Input validation" "No" "values.env:$name" "Invalid email" \
            "Provide a monitored, non-placeholder email address."
    fi
done

if [[ -z "${GOOGLE_CLOSED_TEST_GROUP:-}" ]]; then
    record_result "GOOGLE_CLOSED_TEST_GROUP" "Optional Play group configuration" "Not required" \
        "values.env:GOOGLE_CLOSED_TEST_GROUP" "Intentionally empty; email-list testing is preferred" \
        "None; populate play-closed-testers.txt before Play closed testing."
elif [[ ! "$GOOGLE_CLOSED_TEST_GROUP" =~ $email_pattern || "$GOOGLE_CLOSED_TEST_GROUP" == *.invalid ]]; then
    record_result "GOOGLE_CLOSED_TEST_GROUP" "Play closed testing" "Deferred" \
        "values.env:GOOGLE_CLOSED_TEST_GROUP" "Not a real group email" \
        "Clear it or replace it only if a Google Group is deliberately selected; it will not be uploaded."
else
    record_result "GOOGLE_CLOSED_TEST_GROUP" "Play closed testing" "Ready" \
        "values.env:GOOGLE_CLOSED_TEST_GROUP" "Valid group email format" "None"
fi

if [[ -z "${TESTFLIGHT_INTERNAL_EMAILS:-}" ]]; then
    record_result "TESTFLIGHT_INTERNAL_EMAILS" "Internal TestFlight assignment" "Deferred" \
        "values.env:TESTFLIGHT_INTERNAL_EMAILS" "Intentionally empty" \
        "Add real App Store Connect user emails before assigning internal testers."
else
    internal_testers_valid=true
    internal_tester_emails=$'\n'
    IFS=',' read -r -a internal_testers <<< "$TESTFLIGHT_INTERNAL_EMAILS"
    for internal_tester in "${internal_testers[@]}"; do
        internal_tester="${internal_tester//[[:space:]]/}"
        normalized_internal_tester="$(printf '%s' "$internal_tester" | tr '[:upper:]' '[:lower:]')"
        if [[ ! "$internal_tester" =~ $email_pattern || "$internal_tester" == *.invalid ||
            "$internal_tester_emails" == *$'\n'"$normalized_internal_tester"$'\n'* ]]; then
            internal_testers_valid=false
            break
        fi
        internal_tester_emails+="$normalized_internal_tester"$'\n'
    done
    if [[ "$internal_testers_valid" == "true" ]]; then
        record_result "TESTFLIGHT_INTERNAL_EMAILS" "Internal TestFlight assignment" "Ready" \
            "values.env:TESTFLIGHT_INTERNAL_EMAILS" "Valid email-list format" "None"
    else
        record_result "TESTFLIGHT_INTERNAL_EMAILS" "Internal TestFlight assignment" "Deferred" \
            "values.env:TESTFLIGHT_INTERNAL_EMAILS" "Not a real internal tester list" \
            "Replace it before assigning internal testers; it will not be uploaded."
    fi
fi

for name in PROJECT_URL PRIVACY_POLICY_URL SUPPORT_URL; do
    value="${!name:-}"
    if [[ -n "$value" ]] && ! ruby -ruri -e '
      uri = URI.parse(ARGV.fetch(0))
      abort unless uri.is_a?(URI::HTTPS) && uri.host && !uri.host.empty? &&
        uri.userinfo.nil? && uri.fragment.nil?
    ' "$value"; then
        fail_result "$name" "Input validation" "No" "values.env:$name" "Invalid URL" \
            "Provide a public HTTPS URL."
    fi
done

if [[ -n "${WINDOWS_TIMESTAMP_URL:-}" ]] && ! ruby -ruri -e '
  uri = URI.parse(ARGV.fetch(0))
  abort unless (uri.is_a?(URI::HTTP) || uri.is_a?(URI::HTTPS)) &&
    uri.host && !uri.host.empty? && uri.userinfo.nil? && uri.fragment.nil?
' "$WINDOWS_TIMESTAMP_URL"; then
    fail_result "WINDOWS_TIMESTAMP_URL" "Signing validation" "No" \
        "values.env:WINDOWS_TIMESTAMP_URL" "Invalid RFC 3161 URL" \
        "Use a trusted HTTP or HTTPS timestamp service URL."
fi

if [[ -n "${WINDOWS_EXPECTED_PUBLISHER_NAME:-}" ]] && {
    ((${#WINDOWS_EXPECTED_PUBLISHER_NAME} > 200)) ||
        [[ "$WINDOWS_EXPECTED_PUBLISHER_NAME" == *$'\n'* ||
           "$WINDOWS_EXPECTED_PUBLISHER_NAME" == *$'\r'* ]]
}; then
    fail_result "WINDOWS_EXPECTED_PUBLISHER_NAME" "Signing validation" "No" \
        "values.env:WINDOWS_EXPECTED_PUBLISHER_NAME" "Invalid publisher name" \
        "Use the exact bounded certificate simple name shown by the selected signing provider."
fi

require_windows_backend_value() {
    local name="$1"
    if [[ -z "${!name:-}" ]]; then
        fail_result "$name" "Windows signing backend" "No" "values.env:$name" "Missing" \
            "Add the selected backend value to release/private/values.env."
    else
        record_result "$name" "Windows signing backend" "Local only" "values.env:$name" "Present" "None"
    fi
}

uuid_pattern='^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'
case "${WINDOWS_SIGNING_BACKEND:-}" in
azure-artifact-signing)
    require_windows_backend_value WINDOWS_TIMESTAMP_URL
    for name in \
        WINDOWS_AZURE_CLIENT_ID WINDOWS_AZURE_TENANT_ID WINDOWS_AZURE_SUBSCRIPTION_ID \
        WINDOWS_ARTIFACT_SIGNING_ENDPOINT WINDOWS_ARTIFACT_SIGNING_ACCOUNT \
        WINDOWS_ARTIFACT_SIGNING_PROFILE; do
        require_windows_backend_value "$name"
    done
    for name in WINDOWS_AZURE_CLIENT_ID WINDOWS_AZURE_TENANT_ID WINDOWS_AZURE_SUBSCRIPTION_ID; do
        if [[ -n "${!name:-}" && ! "${!name}" =~ $uuid_pattern ]]; then
            fail_result "$name" "Windows signing backend" "No" "values.env:$name" "Invalid UUID" \
                "Copy the exact Azure identifier."
        fi
    done
    if [[ -n "${WINDOWS_ARTIFACT_SIGNING_ENDPOINT:-}" ]] && ! ruby -ruri -e '
      uri = URI.parse(ARGV.fetch(0))
      abort unless uri.is_a?(URI::HTTPS) && uri.host&.end_with?(".codesigning.azure.net") &&
        uri.path.match?(%r{\A/?\z}) && uri.query.nil? && uri.fragment.nil? && uri.userinfo.nil?
    ' "$WINDOWS_ARTIFACT_SIGNING_ENDPOINT"; then
        fail_result "WINDOWS_ARTIFACT_SIGNING_ENDPOINT" "Windows signing backend" "No" \
            "values.env:WINDOWS_ARTIFACT_SIGNING_ENDPOINT" "Invalid Artifact Signing endpoint" \
            "Copy the HTTPS region endpoint from the Azure Artifact Signing account."
    fi
    for name in WINDOWS_ARTIFACT_SIGNING_ACCOUNT WINDOWS_ARTIFACT_SIGNING_PROFILE; do
        value="${!name:-}"
        if [[ -n "$value" && ( ! "$value" =~ ^[A-Za-z][A-Za-z0-9-]{2,98}[A-Za-z0-9]$ ||
            "$value" == *--* ) ]]; then
            fail_result "$name" "Windows signing backend" "No" "values.env:$name" \
                "Invalid resource name" "Copy the exact Azure Artifact Signing resource name."
        fi
    done
    if [[ "${WINDOWS_TIMESTAMP_URL:-}" != "http://timestamp.acs.microsoft.com" &&
        "${WINDOWS_TIMESTAMP_URL:-}" != "http://timestamp.acs.microsoft.com/" ]]; then
        fail_result "WINDOWS_TIMESTAMP_URL" "Windows signing backend" "No" \
            "values.env:WINDOWS_TIMESTAMP_URL" "Unexpected Artifact Signing timestamp authority" \
            "Use http://timestamp.acs.microsoft.com as recommended by Microsoft."
    fi
    for name in WINDOWS_CERTIFICATE_FILE WINDOWS_CERTIFICATE_PASSWORD WINDOWS_PFX_POLICY_CONFIRMATION; do
        if [[ -n "${!name:-}" ]]; then
            fail_result "$name" "Windows signing backend" "No" "values.env:$name" \
                "PFX input conflicts with Azure Artifact Signing" "Remove the unused local-PFX value."
        fi
    done
    for name in WINDOWS_SIGNPATH_API_TOKEN WINDOWS_SIGNPATH_ORGANIZATION_ID \
        WINDOWS_SIGNPATH_PROJECT_SLUG WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG \
        WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG; do
        if [[ -n "${!name:-}" ]]; then
            fail_result "$name" "Windows signing backend" "No" "values.env:$name" \
                "SignPath input conflicts with Azure Artifact Signing" \
                "Remove the unused SignPath value."
        fi
    done
    ;;
signpath)
    for name in \
        WINDOWS_SIGNPATH_API_TOKEN WINDOWS_SIGNPATH_ORGANIZATION_ID \
        WINDOWS_SIGNPATH_PROJECT_SLUG WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG \
        WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG; do
        require_windows_backend_value "$name"
    done
    if [[ -n "${WINDOWS_SIGNPATH_API_TOKEN:-}" &&
        ( ${#WINDOWS_SIGNPATH_API_TOKEN} -gt 2048 ||
            "$WINDOWS_SIGNPATH_API_TOKEN" == *[[:space:]]* ) ]]; then
        fail_result "WINDOWS_SIGNPATH_API_TOKEN" "Windows signing backend" "No" \
            "values.env:WINDOWS_SIGNPATH_API_TOKEN" "Invalid API token shape" \
            "Use the bounded single-line SignPath submitter token exactly as issued."
    fi
    if [[ -n "${WINDOWS_SIGNPATH_ORGANIZATION_ID:-}" &&
        ! "$WINDOWS_SIGNPATH_ORGANIZATION_ID" =~ $uuid_pattern ]]; then
        fail_result "WINDOWS_SIGNPATH_ORGANIZATION_ID" "Windows signing backend" "No" \
            "values.env:WINDOWS_SIGNPATH_ORGANIZATION_ID" "Invalid organization UUID" \
            "Copy the exact SignPath organization ID."
    fi
    for name in WINDOWS_SIGNPATH_PROJECT_SLUG WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG \
        WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG; do
        value="${!name:-}"
        if [[ -n "$value" && ! "$value" =~ ^[A-Za-z0-9][A-Za-z0-9._-]{0,99}$ ]]; then
            fail_result "$name" "Windows signing backend" "No" "values.env:$name" \
                "Invalid SignPath slug" "Copy the exact bounded SignPath slug."
        fi
    done
    for name in WINDOWS_CERTIFICATE_FILE WINDOWS_CERTIFICATE_PASSWORD \
        WINDOWS_PFX_POLICY_CONFIRMATION WINDOWS_TIMESTAMP_URL \
        WINDOWS_AZURE_CLIENT_ID WINDOWS_AZURE_TENANT_ID WINDOWS_AZURE_SUBSCRIPTION_ID \
        WINDOWS_ARTIFACT_SIGNING_ENDPOINT WINDOWS_ARTIFACT_SIGNING_ACCOUNT \
        WINDOWS_ARTIFACT_SIGNING_PROFILE; do
        if [[ -n "${!name:-}" ]]; then
            fail_result "$name" "Windows signing backend" "No" "values.env:$name" \
                "Input conflicts with SignPath signing" "Remove the unused backend value."
        fi
    done
    ;;
local-pfx)
    for name in WINDOWS_CERTIFICATE_FILE WINDOWS_CERTIFICATE_PASSWORD \
        WINDOWS_PFX_POLICY_CONFIRMATION WINDOWS_TIMESTAMP_URL; do
        require_windows_backend_value "$name"
    done
    if [[ "${WINDOWS_PFX_POLICY_CONFIRMATION:-}" != \
        "I_CONFIRM_CA_AUTHORIZED_EXPORTABLE_PRODUCTION_KEY" ]]; then
        fail_result "WINDOWS_PFX_POLICY_CONFIRMATION" "Windows signing policy" "No" \
            "values.env:WINDOWS_PFX_POLICY_CONFIRMATION" "Required CA authorization confirmation missing" \
            "Use Azure Artifact Signing, or confirm only an existing key whose issuing CA permits this CI use."
    fi
    for name in \
        WINDOWS_AZURE_CLIENT_ID WINDOWS_AZURE_TENANT_ID WINDOWS_AZURE_SUBSCRIPTION_ID \
        WINDOWS_ARTIFACT_SIGNING_ENDPOINT WINDOWS_ARTIFACT_SIGNING_ACCOUNT \
        WINDOWS_ARTIFACT_SIGNING_PROFILE; do
        if [[ -n "${!name:-}" ]]; then
            fail_result "$name" "Windows signing backend" "No" "values.env:$name" \
                "Azure input conflicts with local-PFX signing" "Remove the unused Azure value."
        fi
    done
    for name in WINDOWS_SIGNPATH_API_TOKEN WINDOWS_SIGNPATH_ORGANIZATION_ID \
        WINDOWS_SIGNPATH_PROJECT_SLUG WINDOWS_SIGNPATH_SIGNING_POLICY_SLUG \
        WINDOWS_SIGNPATH_ARTIFACT_CONFIGURATION_SLUG; do
        if [[ -n "${!name:-}" ]]; then
            fail_result "$name" "Windows signing backend" "No" "values.env:$name" \
                "SignPath input conflicts with local-PFX signing" \
                "Remove the unused SignPath value."
        fi
    done
    ;;
*)
    if [[ -n "${WINDOWS_SIGNING_BACKEND:-}" ]]; then
        fail_result "WINDOWS_SIGNING_BACKEND" "Windows signing backend" "No" \
            "values.env:WINDOWS_SIGNING_BACKEND" "Unsupported backend" \
            "Use exactly azure-artifact-signing, signpath, or local-pfx."
    fi
    ;;
esac

if [[ "${ANDROID_PACKAGE_NAME:-}" != "com.passvault.android" ]]; then
    fail_result "ANDROID_PACKAGE_NAME" "Application identity" "No" "values.env:ANDROID_PACKAGE_NAME" \
        "Unexpected package" "Use com.passvault.android before the first Play upload."
fi

if [[ "${IOS_BUNDLE_ID:-}" != "com.passvault.ios" ]]; then
    fail_result "IOS_BUNDLE_ID" "Application identity" "No" "values.env:IOS_BUNDLE_ID" \
        "Unexpected bundle ID" "Use com.passvault.ios before the first App Store upload."
fi

if [[ "${APP_STORE_SKU:-}" != "passvault-ios-2026" ]]; then
    fail_result "APP_STORE_SKU" "Application identity" "No" "values.env:APP_STORE_SKU" \
        "Unexpected SKU" "Use passvault-ios-2026 for the new App Store record."
fi

if [[ -n "${APP_STORE_APP_ID:-}" && ! "${APP_STORE_APP_ID}" =~ ^[1-9][0-9]{7,14}$ ]]; then
    fail_result "APP_STORE_APP_ID" "Apple identifier" "No" "values.env:APP_STORE_APP_ID" \
        "Invalid numeric Apple ID" "Copy the numeric Apple ID from App Store Connect."
fi

if [[ -n "${APPLE_TEAM_ID:-}" && ! "${APPLE_TEAM_ID}" =~ ^[A-Z0-9]{10}$ ]]; then
    fail_result "APPLE_TEAM_ID" "Apple identifier" "No" "values.env:APPLE_TEAM_ID" \
        "Invalid Team ID" "Copy the ten-character Team ID from Apple membership details."
fi

if [[ -n "${ASC_KEY_ID:-}" && ! "${ASC_KEY_ID}" =~ ^[A-Z0-9]{10}$ ]]; then
    fail_result "ASC_KEY_ID" "Apple identifier" "No" "values.env:ASC_KEY_ID" \
        "Invalid key ID" "Copy the ten-character App Store Connect API key ID."
fi

if [[ -n "${ASC_ISSUER_ID:-}" &&
    ! "${ASC_ISSUER_ID}" =~ ^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$ ]]; then
    fail_result "ASC_ISSUER_ID" "Apple identifier" "No" "values.env:ASC_ISSUER_ID" \
        "Invalid issuer ID" "Copy the issuer UUID from App Store Connect."
fi

if [[ -n "${GOOGLE_CLOUD_PROJECT_ID:-}" && ! "${GOOGLE_CLOUD_PROJECT_ID}" =~ ^[a-z][a-z0-9-]{4,28}[a-z0-9]$ ]]; then
    fail_result "GOOGLE_CLOUD_PROJECT_ID" "Google identifier" "No" "values.env:GOOGLE_CLOUD_PROJECT_ID" \
        "Invalid project ID" "Copy the Google Cloud project ID, not its display name."
fi

if [[ -n "${GOOGLE_CLOUD_PROJECT_NUMBER:-}" && ! "${GOOGLE_CLOUD_PROJECT_NUMBER}" =~ ^[0-9]{6,20}$ ]]; then
    fail_result "GOOGLE_CLOUD_PROJECT_NUMBER" "Google identifier" "No" "values.env:GOOGLE_CLOUD_PROJECT_NUMBER" \
        "Invalid project number" "Copy the numeric Google Cloud project number."
fi

if [[ -n "${GITHUB_REPOSITORY:-}" && ! "${GITHUB_REPOSITORY}" =~ ^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$ ]]; then
    fail_result "GITHUB_REPOSITORY" "GitHub identifier" "No" "values.env:GITHUB_REPOSITORY" \
        "Invalid owner/repository" "Use the exact owner/repository name."
fi

if [[ -n "${GITHUB_DEPLOYMENT_APPROVER:-}" &&
    ! "${GITHUB_DEPLOYMENT_APPROVER}" =~ ^[A-Za-z0-9]([A-Za-z0-9-]{0,37}[A-Za-z0-9])?$ ]]; then
    fail_result "GITHUB_DEPLOYMENT_APPROVER" "GitHub identifier" "No" \
        "values.env:GITHUB_DEPLOYMENT_APPROVER" "Invalid GitHub username" \
        "Use the exact GitHub username that approves protected deployments."
fi

if [[ -n "${APP_REVIEW_CONTACT_NAME:-}" &&
    ! "${APP_REVIEW_CONTACT_NAME}" =~ ^[^[:space:]]+([[:space:]]+[^[:space:]]+)+$ ]]; then
    fail_result "APP_REVIEW_CONTACT_NAME" "App Review contact" "No" \
        "values.env:APP_REVIEW_CONTACT_NAME" "A first and last name are required" \
        "Provide the real two-part App Review contact name."
fi

if [[ -n "${APP_REVIEW_PHONE:-}" ]]; then
    app_review_phone_digits="$(printf '%s' "$APP_REVIEW_PHONE" | tr -cd '0-9')"
    if [[ ! "$APP_REVIEW_PHONE" =~ ^[+0-9().[:space:]-]+$ ||
        ${#app_review_phone_digits} -lt 7 || ${#app_review_phone_digits} -gt 20 ]]; then
        fail_result "APP_REVIEW_PHONE" "App Review contact" "No" \
            "values.env:APP_REVIEW_PHONE" "Invalid phone number" \
            "Provide a reachable international App Review phone number."
    fi
fi

if [[ -n "${TESTFLIGHT_EXTERNAL_GROUP:-}" &&
    ( "${TESTFLIGHT_EXTERNAL_GROUP}" == *$'\n'* ||
      "${TESTFLIGHT_EXTERNAL_GROUP}" == *$'\r'* ||
      ${#TESTFLIGHT_EXTERNAL_GROUP} -gt 100 ) ]]; then
    fail_result "TESTFLIGHT_EXTERNAL_GROUP" "TestFlight configuration" "No" \
        "values.env:TESTFLIGHT_EXTERNAL_GROUP" "Invalid group name" \
        "Use a single-line external group name of at most 100 characters."
fi

case "${EXPORT_COMPLIANCE_STATUS:-}" in
    EXEMPT_APPROVED|NON_EXEMPT_APPROVED)
        record_result "EXPORT_COMPLIANCE_STATUS" "Production legal gate" "Not uploaded" \
            "values.env:EXPORT_COMPLIANCE_STATUS" "Approved status supplied" "Retain supporting determination."
        ;;
    PENDING|"")
        fail_result "EXPORT_COMPLIANCE_STATUS" "Production legal gate" "No" \
            "values.env:EXPORT_COMPLIANCE_STATUS" "Pending" \
            "Obtain and approve the Apple export-compliance determination."
        ;;
    *)
        fail_result "EXPORT_COMPLIANCE_STATUS" "Production legal gate" "No" \
            "values.env:EXPORT_COMPLIANCE_STATUS" "Unsupported value" \
            "Use PENDING, EXEMPT_APPROVED, or NON_EXEMPT_APPROVED."
        ;;
esac

case "${IOS_FRANCE_AVAILABLE:-}" in
    false)
        record_result "IOS_FRANCE_AVAILABLE" "App Store availability gate" "Not uploaded" \
            "values.env:IOS_FRANCE_AVAILABLE" "France explicitly excluded" \
            "Reopen App Encryption Documentation before changing this value."
        ;;
    true)
        if [[ "${EXPORT_COMPLIANCE_STATUS:-}" == "EXEMPT_APPROVED" ]]; then
            fail_result "IOS_FRANCE_AVAILABLE" "App Store availability gate" "No" \
                "values.env:IOS_FRANCE_AVAILABLE" \
                "France cannot be enabled with the no-documentation determination" \
                "Set France back to false or complete renewed compliance review before release."
        else
            record_result "IOS_FRANCE_AVAILABLE" "App Store availability gate" "Not uploaded" \
                "values.env:IOS_FRANCE_AVAILABLE" "France enabled under a separate compliance state" \
                "Verify the corresponding approved declaration before release."
        fi
        ;;
    *)
        fail_result "IOS_FRANCE_AVAILABLE" "App Store availability gate" "No" \
            "values.env:IOS_FRANCE_AVAILABLE" "Invalid boolean" \
            "Use exactly false while France is excluded, or true only after renewed approval."
        ;;
esac

if [[ -n "${EXPORT_COMPLIANCE_STATUS:-}" && -n "${IOS_FRANCE_AVAILABLE:-}" ]]; then
    if ! EXPORT_COMPLIANCE_STATUS="$EXPORT_COMPLIANCE_STATUS" \
        IOS_FRANCE_AVAILABLE="$IOS_FRANCE_AVAILABLE" \
        "$repository_root/scripts/validate-ios-export-compliance.sh" >/dev/null 2>&1; then
        fail_result "iOS export policy" "Release configuration" "No" \
            "values.env:EXPORT_COMPLIANCE_STATUS" "Status and France scope are inconsistent" \
            "Keep France excluded for EXEMPT_APPROVED or complete renewed compliance review."
    else
        record_result "iOS export policy" "Release configuration" "Ready" \
            "values.env:EXPORT_COMPLIANCE_STATUS" "Status and France scope agree" "None"
    fi
fi

resolve_private_file() {
    local relative_path="$1"
    if [[ "$relative_path" != release/private/* || "$relative_path" == *".."* ]]; then
        return 1
    fi
    printf '%s/%s' "$repository_root" "$relative_path"
}

checked_private_file=""

check_private_file() {
    local variable_name="$1"
    local relative_path="${!variable_name:-}"
    local absolute_path
    checked_private_file=""
    if [[ -z "$relative_path" ]] || ! absolute_path="$(resolve_private_file "$relative_path")"; then
        fail_result "$variable_name" "Local private file" "No" "values.env:$variable_name" \
            "Unsafe path" "Use a path below release/private/."
        return 1
    fi
    if [[ ! -s "$absolute_path" ]] || ! private_file_is_safe "$absolute_path"; then
        fail_result "$variable_name" "Local private file" "No" "$relative_path" \
            "Missing, empty, or symlink" "Add a non-empty regular file at the documented path."
        return 1
    fi
    record_result "$variable_name" "Local private file" "Local only" "$relative_path" "File present" "None"
    checked_private_file="$absolute_path"
}

android_keystore=""
ios_certificate=""
ios_profile=""
asc_private_key=""
windows_certificate=""
macos_certificate=""

check_private_file ANDROID_UPLOAD_KEYSTORE_FILE || true
android_keystore="$checked_private_file"
check_private_file IOS_DISTRIBUTION_CERTIFICATE_FILE || true
ios_certificate="$checked_private_file"
check_private_file IOS_PROVISIONING_PROFILE_FILE || true
ios_profile="$checked_private_file"
check_private_file ASC_PRIVATE_KEY_FILE || true
asc_private_key="$checked_private_file"
if [[ "${WINDOWS_SIGNING_BACKEND:-}" == local-pfx ]]; then
    check_private_file WINDOWS_CERTIFICATE_FILE || true
    windows_certificate="$checked_private_file"
fi
check_private_file MACOS_CERTIFICATE_FILE || true
macos_certificate="$checked_private_file"
check_private_file MACOS_PROVISIONING_PROFILE_FILE || true

if [[ -n "$android_keystore" && -n "${KEYSTORE_PASSWORD:-}" && -n "${KEY_ALIAS:-}" && -n "${KEY_PASSWORD:-}" ]]; then
    expected_android_alias="$(tr -d '\r\n' < release/android/passvault-upload-alias.txt)"
    if [[ "$KEY_ALIAS" != "$expected_android_alias" ]]; then
        fail_result "Android upload alias" "Signing validation" "No" "values.env:KEY_ALIAS" \
            "Configured alias does not match the canonical upload alias" \
            "Set KEY_ALIAS to $expected_android_alias; do not replace or modify the keystore."
        android_keystore=""
    else
        record_result "Android upload alias" "Signing validation" "Ready" \
            "values.env:KEY_ALIAS" "Canonical alias $expected_android_alias" "None"
    fi
fi

if [[ -n "$android_keystore" && -n "${KEYSTORE_PASSWORD:-}" && -n "${KEY_ALIAS:-}" && -n "${KEY_PASSWORD:-}" ]]; then
    export PASSVAULT_VALIDATION_STOREPASS="$KEYSTORE_PASSWORD"
    export PASSVAULT_VALIDATION_KEYPASS="$KEY_PASSWORD"
    exported_certificate="$temporary_root/android-upload-cert.pem"
    copied_keystore="$temporary_root/key-password-check.p12"
    if keytool -list -keystore "$android_keystore" -storepass:env PASSVAULT_VALIDATION_STOREPASS \
        -alias "$KEY_ALIAS" >/dev/null 2>&1 &&
        keytool -importkeystore -srckeystore "$android_keystore" \
        -srcstorepass:env PASSVAULT_VALIDATION_STOREPASS -srcalias "$KEY_ALIAS" \
        -srckeypass:env PASSVAULT_VALIDATION_KEYPASS -destkeystore "$copied_keystore" \
        -deststoretype PKCS12 -deststorepass passvault-validation-only \
        -destkeypass passvault-validation-only -noprompt >/dev/null 2>&1 &&
        keytool -exportcert -rfc -keystore "$android_keystore" \
        -storepass:env PASSVAULT_VALIDATION_STOREPASS -alias "$KEY_ALIAS" \
        -file "$exported_certificate" >/dev/null 2>&1; then
        android_fingerprint="$(keytool -printcert -file "$exported_certificate" 2>/dev/null |
            awk -F'SHA256:' '/SHA256:/ { gsub(/[[:space:]]/, "", $2); print toupper($2); exit }')"
        tracked_fingerprint="$(tr -cd '0-9A-Fa-f' < release/android/passvault-release-cert.sha256 | tr '[:lower:]' '[:upper:]')"
        normalized_android_fingerprint="${android_fingerprint//:/}"
        if [[ ${#normalized_android_fingerprint} -ne 64 ]]; then
            fail_result "Android upload certificate" "Signing validation" "No" \
                "${ANDROID_UPLOAD_KEYSTORE_FILE}" "Fingerprint unavailable" \
                "Verify that the selected alias contains a private-key entry."
        elif [[ "$normalized_android_fingerprint" != "$tracked_fingerprint" ]]; then
            fail_result "Android upload certificate" "Signing validation" "No" \
                "${ANDROID_UPLOAD_KEYSTORE_FILE}" "Valid key; SHA-256 $android_fingerprint; pinned certificate differs" \
                "Do not upload yet. Adopt this new certificate in the tracked public fingerprint after explicit review."
        else
            record_result "Android upload certificate" "Signing validation" "Ready" \
                "${ANDROID_UPLOAD_KEYSTORE_FILE}" "Valid; SHA-256 $android_fingerprint" "None"
        fi
    else
        fail_result "Android upload keystore" "Signing validation" "No" \
            "${ANDROID_UPLOAD_KEYSTORE_FILE}" "Alias, store password, or key password invalid" \
            "Correct KEYSTORE_PASSWORD, KEY_ALIAS, and KEY_PASSWORD without replacing the original file."
    fi
fi

if [[ -n "$ios_certificate" && -n "${IOS_DISTRIBUTION_CERTIFICATE_PASSWORD:-}" ]]; then
    export PASSVAULT_VALIDATION_P12PASS="$IOS_DISTRIBUTION_CERTIFICATE_PASSWORD"
    p12_validation_root="$temporary_root/pkcs12-validation"
    mkdir -m 700 "$p12_validation_root"
    if passvault_validate_pkcs12 "$openssl_binary" "$ios_certificate" \
        PASSVAULT_VALIDATION_P12PASS "$p12_validation_root"; then
        extracted_ios_certificate="$PASSVAULT_P12_CERTIFICATE_FILE"
        if ! "$openssl_binary" x509 -in "$extracted_ios_certificate" -checkend 0 -noout \
            >/dev/null 2>&1; then
            fail_result "iOS distribution certificate" "Signing validation" "No" \
                "${IOS_DISTRIBUTION_CERTIFICATE_FILE}" "Apple Distribution certificate is expired" \
                "Obtain a current certificate and profile before release."
        else
            ios_fingerprint="$("$openssl_binary" x509 -in "$extracted_ios_certificate" \
                -fingerprint -sha256 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
            ios_sha1_fingerprint="$("$openssl_binary" x509 -in "$extracted_ios_certificate" \
                -fingerprint -sha1 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
            ios_subject="$("$openssl_binary" x509 -in "$extracted_ios_certificate" \
                -subject -noout -nameopt RFC2253)"
            certificate_team="$(printf '%s' "$ios_subject" | sed -n 's/.*OU=\([^,]*\).*/\1/p')"
            if [[ "$ios_subject" != *"CN=Apple Distribution:"* && \
                "$ios_subject" != *"CN=iPhone Distribution:"* ]]; then
                fail_result "iOS distribution certificate" "Signing validation" "No" \
                    "${IOS_DISTRIBUTION_CERTIFICATE_FILE}" "Not an Apple Distribution identity" \
                    "Export an Apple Distribution certificate, not a development certificate."
            elif [[ -n "${APPLE_TEAM_ID:-}" && "$certificate_team" != "$APPLE_TEAM_ID" ]]; then
                fail_result "iOS distribution certificate" "Signing validation" "No" \
                    "${IOS_DISTRIBUTION_CERTIFICATE_FILE}" \
                    "Valid; SHA-256 $ios_fingerprint; Team ID mismatch" \
                    "Provide a distribution certificate issued to APPLE_TEAM_ID."
            else
                record_result "iOS distribution certificate" "Signing validation" "Ready" \
                    "${IOS_DISTRIBUTION_CERTIFICATE_FILE}" \
                    "Valid; SHA-1 $ios_sha1_fingerprint; SHA-256 $ios_fingerprint" "None"
            fi
        fi
    else
        case "$PASSVAULT_P12_STATUS" in
            invalid_password)
                p12_validation_message="Invalid PKCS#12 password"
                p12_validation_action="Correct IOS_DISTRIBUTION_CERTIFICATE_PASSWORD in values.env."
                ;;
            unsupported_legacy_cipher)
                p12_validation_message="Unsupported legacy PKCS#12 cipher or provider"
                p12_validation_action="Install Homebrew OpenSSL 3 with its legacy provider enabled."
                ;;
            missing_certificate)
                p12_validation_message="PKCS#12 contains no readable distribution certificate"
                p12_validation_action="Verify the existing PKCS#12 export includes its certificate."
                ;;
            missing_private_key)
                p12_validation_message="PKCS#12 contains no readable private key"
                p12_validation_action="Verify the existing PKCS#12 export includes its private key."
                ;;
            certificate_private_key_mismatch)
                p12_validation_message="PKCS#12 certificate and private key do not match"
                p12_validation_action="Use an identity export whose certificate and private key are paired."
                ;;
            *)
                p12_validation_message="Invalid PKCS#12 structure or MAC"
                p12_validation_action="Verify the existing PKCS#12 file integrity and stored password."
                ;;
        esac
        fail_result "iOS distribution certificate" "Signing validation" "No" \
            "${IOS_DISTRIBUTION_CERTIFICATE_FILE}" "$p12_validation_message" "$p12_validation_action"
    fi
    unset PASSVAULT_VALIDATION_P12PASS
fi

validate_desktop_pkcs12() {
    local label="$1"
    local certificate_path="$2"
    local password_name="$3"
    local expected_identity="$4"
    local certificate_source_name="$5"
    local validation_root extracted_certificate subject team fingerprint eku

    if [[ -z "$certificate_path" || -z "${!password_name:-}" ]]; then
        return 0
    fi
    validation_root="$temporary_root/${label// /-}-pkcs12"
    mkdir -m 700 "$validation_root"
    export PASSVAULT_DESKTOP_P12_PASSWORD="${!password_name}"
    if ! passvault_validate_pkcs12 "$openssl_binary" "$certificate_path" \
        PASSVAULT_DESKTOP_P12_PASSWORD "$validation_root"; then
        fail_result "$label certificate" "Signing validation" "No" \
            "values.env:$certificate_source_name" \
            "Invalid PKCS#12 container, password, private key, or certificate pairing" \
            "Export the existing production identity as a password-protected PKCS#12 file."
        unset PASSVAULT_DESKTOP_P12_PASSWORD
        return
    fi
    unset PASSVAULT_DESKTOP_P12_PASSWORD
    extracted_certificate="$PASSVAULT_P12_CERTIFICATE_FILE"
    if ! "$openssl_binary" x509 -in "$extracted_certificate" -checkend 0 -noout \
        >/dev/null 2>&1; then
        fail_result "$label certificate" "Signing validation" "No" \
            "$certificate_path" "Certificate is expired" "Renew it before production signing."
        return
    fi
    subject="$($openssl_binary x509 -in "$extracted_certificate" -subject -noout -nameopt RFC2253)"
    fingerprint="$($openssl_binary x509 -in "$extracted_certificate" -fingerprint -sha256 -noout |
        sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
    if [[ "$expected_identity" == windows ]]; then
        eku="$($openssl_binary x509 -in "$extracted_certificate" -ext extendedKeyUsage -noout 2>/dev/null || true)"
        if [[ "$eku" != *"Code Signing"* && "$eku" != *"1.3.6.1.5.5.7.3.3"* ]]; then
            fail_result "$label certificate" "Signing validation" "No" "$certificate_path" \
                "Code Signing EKU is missing" "Obtain a trusted Windows Authenticode certificate."
            return
        fi
    else
        team="$(printf '%s' "$subject" | sed -n 's/.*OU=\([^,]*\).*/\1/p')"
        if [[ "$subject" != *"CN=Developer ID Application:"* ||
            "$team" != "${APPLE_TEAM_ID:-}" ]]; then
            fail_result "$label certificate" "Signing validation" "No" "$certificate_path" \
                "Not a Developer ID Application identity for APPLE_TEAM_ID" \
                "Export the Developer ID Application identity for the configured Apple team."
            return
        fi
    fi
    record_result "$label certificate" "Signing validation" "Ready" "$certificate_path" \
        "Valid paired private key and certificate; SHA-256 $fingerprint" "None"
}

validate_desktop_pkcs12 \
    "Windows" "$windows_certificate" WINDOWS_CERTIFICATE_PASSWORD windows WINDOWS_CERTIFICATE_FILE
validate_desktop_pkcs12 \
    "macOS" "$macos_certificate" MACOS_CERTIFICATE_PASSWORD macos MACOS_CERTIFICATE_FILE

decode_mobileprovision() {
    local input="$1"
    local output="$2"
    : > "$output"
    if [[ -x /usr/bin/security ]] && /usr/bin/security cms -D -i "$input" > "$output" 2>/dev/null &&
        [[ -s "$output" ]]; then
        return 0
    fi
    : > "$output"
    if "$openssl_binary" cms -inform DER -verify -noverify -in "$input" -out "$output" >/dev/null 2>&1 &&
        [[ -s "$output" ]]; then
        return 0
    fi
    : > "$output"
    "$openssl_binary" smime -inform DER -verify -noverify -in "$input" -out "$output" >/dev/null 2>&1 &&
        [[ -s "$output" ]]
}

if [[ -n "$ios_profile" ]]; then
    profile_plist="$temporary_root/profile.plist"
    if decode_mobileprovision "$ios_profile" "$profile_plist"; then
        profile_team="$(/usr/libexec/PlistBuddy -c 'Print :TeamIdentifier:0' "$profile_plist" 2>/dev/null || true)"
        profile_app_id="$(/usr/libexec/PlistBuddy -c 'Print :Entitlements:application-identifier' "$profile_plist" 2>/dev/null || true)"
        profile_entitlement_team="$(/usr/libexec/PlistBuddy \
            -c 'Print :Entitlements:com.apple.developer.team-identifier' "$profile_plist" 2>/dev/null || true)"
        profile_uuid="$(/usr/libexec/PlistBuddy -c 'Print :UUID' "$profile_plist" 2>/dev/null || true)"
        profile_debug="$(/usr/libexec/PlistBuddy -c 'Print :Entitlements:get-task-allow' "$profile_plist" 2>/dev/null || true)"
        profile_beta="$(/usr/libexec/PlistBuddy -c 'Print :Entitlements:beta-reports-active' \
            "$profile_plist" 2>/dev/null || true)"
        profile_creation="$(sed -n '/<key>CreationDate<\/key>/{n;s/.*<date>\(.*\)<\/date>.*/\1/p;}' \
            "$profile_plist")"
        profile_expiration="$(sed -n '/<key>ExpirationDate<\/key>/{n;s/.*<date>\(.*\)<\/date>.*/\1/p;}' "$profile_plist")"
        profile_expiration_epoch="$(date -j -u -f '%Y-%m-%dT%H:%M:%SZ' "$profile_expiration" '+%s' 2>/dev/null || true)"
        profile_has_devices=false
        if /usr/libexec/PlistBuddy -c 'Print :ProvisionedDevices' "$profile_plist" >/dev/null 2>&1; then
            profile_has_devices=true
        fi
        profile_all_devices="$(/usr/libexec/PlistBuddy -c 'Print :ProvisionsAllDevices' \
            "$profile_plist" 2>/dev/null || true)"
        profile_certificate_count="$(/usr/bin/plutil -extract DeveloperCertificates xml1 -o - \
            "$profile_plist" 2>/dev/null | grep -c '<data>' || true)"
        profile_certificate_match=false
        embedded_fingerprint_summary=""
        for ((certificate_index = 0; certificate_index < profile_certificate_count; certificate_index++)); do
            embedded_certificate="$temporary_root/profile-certificate-$certificate_index.der"
            if /usr/bin/plutil -extract "DeveloperCertificates.$certificate_index" raw -o - \
                "$profile_plist" 2>/dev/null | "$openssl_binary" base64 -d -A > "$embedded_certificate" 2>/dev/null &&
                "$openssl_binary" x509 -inform DER -in "$embedded_certificate" -noout >/dev/null 2>&1; then
                embedded_sha1="$("$openssl_binary" x509 -inform DER -in "$embedded_certificate" \
                    -fingerprint -sha1 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
                embedded_sha256="$("$openssl_binary" x509 -inform DER -in "$embedded_certificate" \
                    -fingerprint -sha256 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
                [[ "$embedded_sha256" == "${ios_fingerprint:-}" ]] && profile_certificate_match=true
                [[ -n "$embedded_fingerprint_summary" ]] && embedded_fingerprint_summary+="; "
                embedded_fingerprint_summary+="#${certificate_index} SHA-1 $embedded_sha1, SHA-256 $embedded_sha256"
            fi
        done
        expected_profile_app_id="${APPLE_TEAM_ID:-}.${IOS_BUNDLE_ID:-}"
        if [[ ! "$profile_uuid" =~ ^[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}$ ||
            -z "$profile_team" || "$profile_team" != "${APPLE_TEAM_ID:-}" ||
            -z "$profile_app_id" || "$profile_app_id" != "$expected_profile_app_id" ||
            "$profile_entitlement_team" != "${APPLE_TEAM_ID:-}" ]]; then
            fail_result "iOS provisioning profile" "Signing validation" "No" \
                "${IOS_PROVISIONING_PROFILE_FILE}" "Team, bundle ID, or team entitlement mismatch" \
                "Create an App Store profile for APPLE_TEAM_ID and com.passvault.ios."
        elif [[ "$profile_debug" != "false" || "$profile_beta" != "true" ||
            "$profile_has_devices" == "true" || "$profile_all_devices" == "true" ]]; then
            fail_result "iOS provisioning profile" "Signing validation" "No" \
                "${IOS_PROVISIONING_PROFILE_FILE}" "Not an App Store distribution profile" \
                "Download an App Store profile without development or ad-hoc devices."
        elif [[ -z "$profile_expiration_epoch" || "$profile_expiration_epoch" -le "$(date '+%s')" ]]; then
            fail_result "iOS provisioning profile" "Signing validation" "No" \
                "${IOS_PROVISIONING_PROFILE_FILE}" "Provisioning profile is expired or has an invalid expiration" \
                "Create and download a current App Store provisioning profile."
        elif [[ "$profile_certificate_count" -lt 1 || -z "$embedded_fingerprint_summary" ]]; then
            fail_result "iOS provisioning profile" "Signing validation" "No" \
                "${IOS_PROVISIONING_PROFILE_FILE}" "No readable distribution certificate is embedded" \
                "Regenerate the App Store profile with the supplied Apple Distribution certificate selected."
        elif [[ -n "${ios_fingerprint:-}" && "$profile_certificate_match" != "true" ]]; then
            fail_result "iOS provisioning profile" "Signing validation" "No" \
                "${IOS_PROVISIONING_PROFILE_FILE}" \
                "P12 SHA-1 ${ios_sha1_fingerprint:-unavailable}, SHA-256 $ios_fingerprint does not match embedded certificates: $embedded_fingerprint_summary" \
                "Regenerate the profile with the supplied Apple Distribution certificate selected."
        else
            record_result "iOS provisioning profile" "Signing validation" "Ready" \
                "${IOS_PROVISIONING_PROFILE_FILE}" \
                "Valid App Store profile; UUID $profile_uuid; Team $profile_team; bundle ${IOS_BUNDLE_ID}; get-task-allow false; beta-reports-active true; no provisioned devices; created $profile_creation; expires $profile_expiration; matching certificates: $embedded_fingerprint_summary" \
                "None"
        fi
    else
        fail_result "iOS provisioning profile" "Signing validation" "No" \
            "${IOS_PROVISIONING_PROFILE_FILE}" "Invalid mobileprovision file" \
            "Download a valid App Store provisioning profile from Apple Developer."
    fi
fi

if [[ -n "$asc_private_key" ]]; then
    if grep -q '^-----BEGIN PRIVATE KEY-----$' "$asc_private_key" &&
        ruby "$repository_root/scripts/validate-app-store-connect-key.rb" \
            "$asc_private_key" >/dev/null 2>&1; then
        record_result "App Store Connect private key" "Authentication validation" "Ready" \
            "${ASC_PRIVATE_KEY_FILE}" "Valid PKCS#8 private key; key ID and issuer format valid" \
            "API ownership is verified online immediately before GitHub configuration."
    else
        fail_result "App Store Connect private key" "Authentication validation" "No" \
            "${ASC_PRIVATE_KEY_FILE}" "Invalid private key" \
            "Use the original AuthKey_*.p8 downloaded once from App Store Connect."
    fi
fi

validate_text_file() {
    local variable_name="$1"
    local language="$2"
    local path
    check_private_file "$variable_name" || true
    path="$checked_private_file"
    [[ -z "$path" ]] && return
    if ! ruby -e '
      contents = File.binread(ARGV.fetch(0)).force_encoding(Encoding::UTF_8)
      abort unless contents.valid_encoding? && !contents.include?("\0") &&
        contents.bytesize <= 512 * 1024
    ' "$path"; then
        fail_result "$variable_name" "Metadata validation" "No" "${!variable_name}" \
            "Not valid UTF-8 or exceeds 512 KiB" \
            "Save the approved $language text as a small UTF-8 file without NUL bytes."
        return
    fi
    if grep -Eiq 'replace this placeholder|example\.invalid|todo|draft|change[ -]?me|استبدل هذا النص|استبدل هذا' "$path"; then
        fail_result "$variable_name" "Metadata validation" "No" "${!variable_name}" \
            "Placeholder content" "Replace all placeholder content with approved $language text."
        return
    fi
    if [[ "$language" == "Arabic" ]] && ! grep -q '[ء-ي]' "$path"; then
        fail_result "$variable_name" "Metadata validation" "No" "${!variable_name}" \
            "Arabic text not detected" "Provide reviewed Arabic content."
        return
    fi
    record_result "$variable_name" "Metadata validation" "Ready" "${!variable_name}" \
        "Non-placeholder $language content" "None"
}

validate_text_file RELEASE_NOTES_EN_FILE English
validate_text_file RELEASE_NOTES_AR_FILE Arabic
validate_text_file PRIVACY_TEXT_EN_FILE English
validate_text_file PRIVACY_TEXT_AR_FILE Arabic
validate_text_file STORE_METADATA_EN_FILE English
validate_text_file STORE_METADATA_AR_FILE Arabic
validate_text_file STORE_DESCRIPTION_EN_FILE English
validate_text_file STORE_DESCRIPTION_AR_FILE Arabic

prepared_metadata="$temporary_root/prepared-mobile-store-metadata"
if COPYRIGHT_HOLDER="${COPYRIGHT_HOLDER_EN:-}" \
    SUPPORT_URL="${SUPPORT_URL:-}" \
    PROJECT_URL="${PROJECT_URL:-}" \
    PRIVACY_POLICY_URL="${PRIVACY_POLICY_URL:-}" \
    "$repository_root/scripts/prepare-mobile-store-metadata.sh" \
        "$private_root" "$prepared_metadata" 1 >/dev/null 2>&1; then
    record_result "Bilingual store metadata" "Metadata validation" "Ready" \
        "release/private metadata files" \
        "All required fields are present and within App Store and Google Play character limits" "None"
else
    fail_result "Bilingual store metadata" "Metadata validation" "No" \
        "release/private metadata files" \
        "A required field is missing, empty, or exceeds its store character limit" \
        "Correct the eight bilingual metadata files, then rerun validation."
fi

optional_private_file() {
    local variable_name="$1"
    local relative_path="${!variable_name:-}"
    local absolute_path
    checked_private_file=""
    if [[ -z "$relative_path" ]] || ! absolute_path="$(resolve_private_file "$relative_path")" ||
        [[ ! -s "$absolute_path" ]] || ! private_file_is_safe "$absolute_path"; then
        return 1
    fi
    checked_private_file="$absolute_path"
}

optional_private_file TESTFLIGHT_EXTERNAL_TESTERS_FILE || true
testflight_testers="$checked_private_file"
if [[ -n "$testflight_testers" ]] &&
    "$repository_root/scripts/validate-mobile-tester-files.sh" testflight "$testflight_testers" >/dev/null 2>&1; then
        record_result "TestFlight external testers" "Tester validation" "Ready" \
            "${TESTFLIGHT_EXTERNAL_TESTERS_FILE}" "CSV header and emails valid" "None"
else
    record_result "TestFlight external testers" "External TestFlight only" "Deferred" \
        "${TESTFLIGHT_EXTERNAL_TESTERS_FILE:-values.env:TESTFLIGHT_EXTERNAL_TESTERS_FILE}" \
        "Missing, empty, or placeholder-only; not eligible for upload" \
        "Add real first_name,last_name,email rows before external TestFlight."
fi

optional_private_file PLAY_CLOSED_TESTERS_FILE || true
play_testers="$checked_private_file"
if [[ -n "$play_testers" ]] &&
    "$repository_root/scripts/validate-mobile-tester-files.sh" play "$play_testers" >/dev/null 2>&1; then
        record_result "Play closed testers" "Tester validation" "Ready" \
            "${PLAY_CLOSED_TESTERS_FILE}" "Email list valid" "None"
else
    record_result "Play closed testers" "Play closed testing only" "Deferred" \
        "${PLAY_CLOSED_TESTERS_FILE:-values.env:PLAY_CLOSED_TESTERS_FILE}" \
        "Missing, empty, or placeholder-only; not eligible for upload" \
        "Add one real tester email per line before Play closed testing."
fi

PLAY_USERS_FILE="${PLAY_USERS_FILE:-release/private/play-users.csv}"
play_permission_mode="${PLAY_SERVICE_ACCOUNT_PERMISSION_MODE:-STRICT_LEAST_PRIVILEGE}"
check_private_file PLAY_USERS_FILE || true
play_users_file="$checked_private_file"
if [[ -n "$play_users_file" ]]; then
    play_validation_output="$temporary_root/play-service-account-validation.txt"
    play_validation_error="$temporary_root/play-service-account-validation.err"
    : > "$play_validation_output"
    : > "$play_validation_error"
    chmod 600 "$play_validation_output" "$play_validation_error"
    beta_play_service_account="passvault-play-beta@${GOOGLE_CLOUD_PROJECT_ID:-missing}.iam.gserviceaccount.com"
    production_play_service_account="passvault-play-prod@${GOOGLE_CLOUD_PROJECT_ID:-missing}.iam.gserviceaccount.com"
    if ruby "$repository_root/scripts/validate-play-service-accounts.rb" \
        --csv "$play_users_file" \
        --package "${ANDROID_PACKAGE_NAME:-}" \
        --beta-email "$beta_play_service_account" \
        --production-email "$production_play_service_account" \
        --mode "$play_permission_mode" \
        >"$play_validation_output" 2>"$play_validation_error"; then
        if [[ "$play_permission_mode" == "PASSVAULT_APP_ADMIN_ACCEPTED" ]]; then
            play_permission_summary="Active; com.passvault.android only; no account/global permissions; app-level Admin exception accepted"
            record_result "Google Play app-level Admin security exception" "Local release policy" \
                "Explicitly accepted" "values.env:PLAY_SERVICE_ACCOUNT_PERMISSION_MODE" \
                "PassVault-only Admin includes financial, permission management, reviews, production/testing, listings, drafts, orders, policy, quality, and deep-link capabilities" \
                "Re-audit play-users.csv after every Play permission change; never add account/global access."
        else
            play_permission_summary="Active; com.passvault.android only; no account/global permissions; strict role-specific least privilege"
            record_result "Google Play service-account permission policy" "Local release policy" \
                "Strict" "default validator policy" "Beta and production permissions are separated" "None"
        fi
        record_result "Google Play beta service account" "Play app permission validation" "Ready" \
            "$PLAY_USERS_FILE" "$play_permission_summary" "None"
        record_result "Google Play production service account" "Play app permission validation" "Ready" \
            "$PLAY_USERS_FILE" "$play_permission_summary" "None"
    else
        fail_result "Google Play service-account permissions" "Play app permission validation" "No" \
            "$PLAY_USERS_FILE" "Access state, package scope, account/global access, or permission policy mismatch" \
            "Correct the Play grants, export a fresh user list, and retain strict mode unless an explicit exception is approved."
    fi
fi

report_tmp="$temporary_root/secret-upload-report.md"
{
    echo "# PassVault Secret Upload Report"
    echo
    echo "Generated locally. No secret values are included."
    echo
    echo "| Name | Destination scope | Configured | Source | Validation status | Required action |"
    echo "|---|---|---:|---|---|---|"
    printf '%s\n' "${report_rows[@]}"
    echo
    if (( failure_count == 0 )); then
        echo "Local validation passed. GitHub and Google configuration still require explicit apply steps."
    else
        echo "Local validation failed with $failure_count issue(s). Nothing was uploaded."
    fi
    echo
    echo "This report intentionally excludes passwords, phone numbers, tester details, private keys, and Base64 data."
} > "$report_tmp"
chmod 600 "$report_tmp"
report_directory="$(dirname "$report_file")"
if ! private_directory_is_safe "$report_directory"; then
    echo "The private report directory is missing or unsafe." >&2
    exit 1
fi
mv "$report_tmp" "$report_file"
chmod 600 "$report_file"

if (( failure_count > 0 )); then
    echo "Private release validation failed. Review release/private/generated/secret-upload-report.md." >&2
    exit 1
fi

echo "Private release validation passed without exposing private values."
