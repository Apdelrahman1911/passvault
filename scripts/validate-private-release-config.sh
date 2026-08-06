#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
private_root="$repository_root/release/private"
values_file="$private_root/values.env"
report_file="$private_root/generated/secret-upload-report.md"
temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-release-validation.XXXXXX")"
chmod 700 "$temporary_root"

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

if [[ ! -f "$values_file" || -L "$values_file" ]]; then
    echo "release/private/values.env is missing or is a symlink." >&2
    exit 1
fi

declare -a report_rows=()
failure_count=0

sanitize_report_text() {
    local value="$1"
    value="${value//$'\n'/ }"
    value="${value//|/\/}"
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

load_values() {
    local line key value
    while IFS= read -r line || [[ -n "$line" ]]; do
        line="${line%$'\r'}"
        [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
        if [[ "$line" != *=* ]]; then
            fail_result "values.env" "Local only" "No" "values.env" "Invalid line format" \
                "Use NAME=value format without shell commands."
            continue
        fi
        key="${line%%=*}"
        value="${line#*=}"
        if [[ ! "$key" =~ ^[A-Z][A-Z0-9_]*$ ]]; then
            fail_result "values.env" "Local only" "No" "values.env" "Invalid variable name" \
                "Use uppercase letters, numbers, and underscores only."
            continue
        fi
        case "$key" in
            PATH|IFS|BASH_ENV|ENV|SHELLOPTS|BASHOPTS|CDPATH|GLOBIGNORE|HOME|PWD|TMPDIR|LD_*|DYLD_*)
                fail_result "values.env" "Local only" "No" "values.env" "Unsafe variable name" \
                    "Remove shell and process-control variables from values.env."
                continue
                ;;
        esac
        printf -v "$key" '%s' "$value"
    done < "$values_file"
}

load_values

required_values=(
    PUBLISHER_NAME_EN PUBLISHER_NAME_AR COPYRIGHT_HOLDER_EN COPYRIGHT_HOLDER_AR
    COUNTRY_OR_JURISDICTION SUPPORT_EMAIL SECURITY_EMAIL PROJECT_URL
    PRIVACY_POLICY_URL SUPPORT_URL ANDROID_PACKAGE_NAME IOS_BUNDLE_ID APP_STORE_SKU
    APP_STORE_APP_ID APPLE_TEAM_ID ASC_ISSUER_ID ASC_KEY_ID APP_REVIEW_CONTACT_NAME
    APP_REVIEW_EMAIL APP_REVIEW_PHONE GOOGLE_CLOUD_PROJECT_ID GOOGLE_CLOUD_PROJECT_NUMBER
    GITHUB_REPOSITORY GITHUB_DEPLOYMENT_APPROVER KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD
    IOS_DISTRIBUTION_CERTIFICATE_PASSWORD TESTFLIGHT_EXTERNAL_GROUP
    EXPORT_COMPLIANCE_STATUS IOS_FRANCE_AVAILABLE
    RELEASE_NOTES_EN_FILE RELEASE_NOTES_AR_FILE PRIVACY_TEXT_EN_FILE PRIVACY_TEXT_AR_FILE
    ANDROID_UPLOAD_KEYSTORE_FILE IOS_DISTRIBUTION_CERTIFICATE_FILE
    IOS_PROVISIONING_PROFILE_FILE ASC_PRIVATE_KEY_FILE STORE_METADATA_EN_FILE
    STORE_METADATA_AR_FILE STORE_DESCRIPTION_EN_FILE STORE_DESCRIPTION_AR_FILE
)

for name in "${required_values[@]}"; do
    if [[ -z "${!name:-}" ]]; then
        fail_result "$name" "Input validation" "No" "values.env:$name" "Missing" \
            "Fill this value in release/private/values.env."
    else
        record_result "$name" "Input validation" "Not uploaded" "values.env:$name" "Present" "None"
    fi
done

email_pattern='^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'
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
    IFS=',' read -r -a internal_testers <<< "$TESTFLIGHT_INTERNAL_EMAILS"
    for internal_tester in "${internal_testers[@]}"; do
        internal_tester="${internal_tester//[[:space:]]/}"
        if [[ ! "$internal_tester" =~ $email_pattern || "$internal_tester" == *.invalid ]]; then
            internal_testers_valid=false
            break
        fi
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
    if [[ -n "$value" && ! "$value" =~ ^https://[^[:space:]]+$ ]]; then
        fail_result "$name" "Input validation" "No" "values.env:$name" "Invalid URL" \
            "Provide a public HTTPS URL."
    fi
done

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

if [[ -n "${APP_STORE_APP_ID:-}" && ! "${APP_STORE_APP_ID}" =~ ^[0-9]{8,15}$ ]]; then
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

if [[ -n "${ASC_ISSUER_ID:-}" && ! "${ASC_ISSUER_ID}" =~ ^[0-9a-fA-F-]{36}$ ]]; then
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
    if [[ ! -s "$absolute_path" || -L "$absolute_path" ]]; then
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

check_private_file ANDROID_UPLOAD_KEYSTORE_FILE || true
android_keystore="$checked_private_file"
check_private_file IOS_DISTRIBUTION_CERTIFICATE_FILE || true
ios_certificate="$checked_private_file"
check_private_file IOS_PROVISIONING_PROFILE_FILE || true
ios_profile="$checked_private_file"
check_private_file ASC_PRIVATE_KEY_FILE || true
asc_private_key="$checked_private_file"

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
    extracted_ios_certificate="$temporary_root/ios-distribution-cert.pem"
    extracted_ios_private_key="$temporary_root/ios-private-key.pem"
    if openssl pkcs12 -in "$ios_certificate" -clcerts -nokeys \
        -passin env:PASSVAULT_VALIDATION_P12PASS -out "$extracted_ios_certificate" >/dev/null 2>&1 &&
        openssl pkcs12 -in "$ios_certificate" -nocerts -nodes \
        -passin env:PASSVAULT_VALIDATION_P12PASS -out "$extracted_ios_private_key" >/dev/null 2>&1 &&
        openssl x509 -in "$extracted_ios_certificate" -checkend 0 -noout >/dev/null 2>&1; then
        ios_fingerprint="$(openssl x509 -in "$extracted_ios_certificate" -fingerprint -sha256 -noout |
            sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
        ios_sha1_fingerprint="$(openssl x509 -in "$extracted_ios_certificate" -fingerprint -sha1 -noout |
            sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
        ios_subject="$(openssl x509 -in "$extracted_ios_certificate" -subject -noout -nameopt RFC2253)"
        certificate_team="$(printf '%s' "$ios_subject" | sed -n 's/.*OU=\([^,]*\).*/\1/p')"
        certificate_public_key="$(openssl x509 -in "$extracted_ios_certificate" -pubkey -noout |
            openssl pkey -pubin -outform DER 2>/dev/null | openssl dgst -sha256 -r | awk '{print $1}')"
        private_public_key="$(openssl pkey -in "$extracted_ios_private_key" -pubout -outform DER 2>/dev/null |
            openssl dgst -sha256 -r | awk '{print $1}')"
        if [[ -z "$certificate_public_key" || "$certificate_public_key" != "$private_public_key" ]]; then
            fail_result "iOS distribution certificate" "Signing validation" "No" \
                "${IOS_DISTRIBUTION_CERTIFICATE_FILE}" "Certificate and private key do not match" \
                "Export the Apple Distribution identity together with its private key."
        elif [[ "$ios_subject" != *"CN=Apple Distribution:"* && "$ios_subject" != *"CN=iPhone Distribution:"* ]]; then
            fail_result "iOS distribution certificate" "Signing validation" "No" \
                "${IOS_DISTRIBUTION_CERTIFICATE_FILE}" "Not an Apple Distribution identity" \
                "Export an Apple Distribution certificate, not a development certificate."
        elif [[ -n "${APPLE_TEAM_ID:-}" && -n "$certificate_team" && "$certificate_team" != "$APPLE_TEAM_ID" ]]; then
            fail_result "iOS distribution certificate" "Signing validation" "No" \
                "${IOS_DISTRIBUTION_CERTIFICATE_FILE}" "Valid; SHA-256 $ios_fingerprint; Team ID mismatch" \
                "Provide a distribution certificate issued to APPLE_TEAM_ID."
        else
            record_result "iOS distribution certificate" "Signing validation" "Ready" \
                "${IOS_DISTRIBUTION_CERTIFICATE_FILE}" \
                "Valid; SHA-1 $ios_sha1_fingerprint; SHA-256 $ios_fingerprint" "None"
        fi
    else
        fail_result "iOS distribution certificate" "Signing validation" "No" \
            "${IOS_DISTRIBUTION_CERTIFICATE_FILE}" "Invalid P12, password, certificate, or private key" \
            "Export the Apple Distribution identity with its private key and correct password."
    fi
fi

decode_mobileprovision() {
    local input="$1"
    local output="$2"
    : > "$output"
    if [[ -x /usr/bin/security ]] && /usr/bin/security cms -D -i "$input" > "$output" 2>/dev/null &&
        [[ -s "$output" ]]; then
        return 0
    fi
    : > "$output"
    if openssl cms -inform DER -verify -noverify -in "$input" -out "$output" >/dev/null 2>&1 &&
        [[ -s "$output" ]]; then
        return 0
    fi
    : > "$output"
    openssl smime -inform DER -verify -noverify -in "$input" -out "$output" >/dev/null 2>&1 &&
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
        profile_expiration_epoch="$(date -j -f '%Y-%m-%dT%H:%M:%SZ' "$profile_expiration" '+%s' 2>/dev/null || true)"
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
                "$profile_plist" 2>/dev/null | openssl base64 -d -A > "$embedded_certificate" 2>/dev/null &&
                openssl x509 -inform DER -in "$embedded_certificate" -noout >/dev/null 2>&1; then
                embedded_sha1="$(openssl x509 -inform DER -in "$embedded_certificate" \
                    -fingerprint -sha1 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
                embedded_sha256="$(openssl x509 -inform DER -in "$embedded_certificate" \
                    -fingerprint -sha256 -noout | sed 's/^[^=]*=//' | tr '[:lower:]' '[:upper:]')"
                [[ "$embedded_sha256" == "${ios_fingerprint:-}" ]] && profile_certificate_match=true
                [[ -n "$embedded_fingerprint_summary" ]] && embedded_fingerprint_summary+="; "
                embedded_fingerprint_summary+="#${certificate_index} SHA-1 $embedded_sha1, SHA-256 $embedded_sha256"
            fi
        done
        expected_profile_app_id="${APPLE_TEAM_ID:-}.${IOS_BUNDLE_ID:-}"
        if [[ -z "$profile_team" || "$profile_team" != "${APPLE_TEAM_ID:-}" ||
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
        [[ ! -s "$absolute_path" || -L "$absolute_path" ]]; then
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
mv "$report_tmp" "$report_file"
chmod 600 "$report_file"

if (( failure_count > 0 )); then
    echo "Private release validation failed. Review release/private/generated/secret-upload-report.md." >&2
    exit 1
fi

echo "Private release validation passed without exposing private values."
