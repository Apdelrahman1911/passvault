#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=scripts/lib/dotenv.sh
source "$repository_root/scripts/lib/dotenv.sh"
# shellcheck source=scripts/lib/pkcs12-validation.sh
source "$repository_root/scripts/lib/pkcs12-validation.sh"

temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-private-validator-test.XXXXXX")"
chmod 700 "$temporary_root"
cleanup() {
    unset PASSVAULT_TEST_P12_PASSWORD IOS_DISTRIBUTION_CERTIFICATE_PASSWORD
    if [[ "$temporary_root" == "${TMPDIR:-/tmp}"/passvault-private-validator-test.* &&
        -d "$temporary_root" ]]; then
        find "$temporary_root" -type f -exec chmod 600 {} + 2>/dev/null || true
        rm -rf -- "$temporary_root"
    fi
}
trap cleanup EXIT

fixture_password='p@$$ w`rd ! # ; $(must-not-run) \ exact'
dotenv_file="$temporary_root/values.env"
execution_marker="$temporary_root/dotenv-was-sourced"
{
    printf 'IOS_DISTRIBUTION_CERTIFICATE_PASSWORD="%s"\n' "$fixture_password"
    printf '%s\n' 'PUBLISHER_NAME_EN=PassVault Test Publisher With Spaces'
    printf '%s\n' "PUBLISHER_NAME_AR='ناشر باس فولت للاختبار'"
    printf 'UNTRUSTED_VALUE=$(touch %s)\n' "$execution_marker"
} > "$dotenv_file"
chmod 600 "$dotenv_file"

passvault_dotenv_load_file "$dotenv_file"
[[ "$IOS_DISTRIBUTION_CERTIFICATE_PASSWORD" == "$fixture_password" ]]
[[ "$PUBLISHER_NAME_EN" == "PassVault Test Publisher With Spaces" ]]
[[ "$PUBLISHER_NAME_AR" == "ناشر باس فولت للاختبار" ]]
[[ ! -e "$execution_marker" ]]

if grep -E '^[[:space:]]*(source|\.)[[:space:]]+[^#]*(values\.env|\$values_file)' \
    "$repository_root/scripts/validate-private-release-config.sh" \
    "$repository_root/scripts/configure-google-oidc.sh" \
    "$repository_root/scripts/configure-github-mobile-release.sh" \
    "$repository_root/scripts/verify-ios-release-signing.sh" >/dev/null; then
    echo "A release script sources the dotenv file." >&2
    exit 1
fi

passvault_select_openssl
certificate_file="$temporary_root/test-distribution.pem"
private_key_file="$temporary_root/test-distribution-key.pem"
legacy_p12="$temporary_root/legacy-distribution.p12"
"$PASSVAULT_OPENSSL_BINARY" req -x509 -newkey rsa:2048 -nodes -days 1 \
    -subj '/CN=Apple Distribution: Validator Test/OU=TESTTEAM01/O=PassVault Test/C=US' \
    -keyout "$private_key_file" -out "$certificate_file" >/dev/null 2>&1
export PASSVAULT_TEST_P12_PASSWORD="$fixture_password"
"$PASSVAULT_OPENSSL_BINARY" pkcs12 -export -legacy \
    -in "$certificate_file" -inkey "$private_key_file" -out "$legacy_p12" \
    -passout env:PASSVAULT_TEST_P12_PASSWORD >/dev/null 2>&1

validation_output="$temporary_root/valid-output"
mkdir -m 700 "$validation_output"
passvault_validate_pkcs12 "$PASSVAULT_OPENSSL_BINARY" "$legacy_p12" \
    PASSVAULT_TEST_P12_PASSWORD "$validation_output"
[[ "$PASSVAULT_P12_STATUS" == "valid" ]]

PASSVAULT_TEST_P12_PASSWORD='definitely-wrong-test-password'
export PASSVAULT_TEST_P12_PASSWORD
wrong_password_output="$temporary_root/wrong-password-output"
mkdir -m 700 "$wrong_password_output"
if passvault_validate_pkcs12 "$PASSVAULT_OPENSSL_BINARY" "$legacy_p12" \
    PASSVAULT_TEST_P12_PASSWORD "$wrong_password_output"; then
    echo "A wrong PKCS#12 password was accepted." >&2
    exit 1
fi
[[ "$PASSVAULT_P12_STATUS" == "invalid_password" ]]

echo "Private release validator regression tests passed."
