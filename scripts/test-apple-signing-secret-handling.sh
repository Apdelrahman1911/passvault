#!/usr/bin/env bash

set -euo pipefail

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "Apple signing secret-handling runtime test skipped outside macOS."
    exit 0
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck disable=SC1091 # Resolved from the runtime repository root.
source "$repository_root/scripts/lib/pkcs12-validation.sh"
passvault_select_openssl || {
    echo "OpenSSL is required for the Apple signing secret-handling test." >&2
    exit 1
}
openssl_binary="$PASSVAULT_OPENSSL_BINARY"

temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-apple-signing-test.XXXXXX")"
keychain_path="$temporary_root/test.keychain-db"
keychain_password="$(openssl rand -hex 32)"
# Command-looking characters verify that the password is data, not shell input.
# shellcheck disable=SC2016
fixture_password='fixture p@$$word; $(must-not-run) `still-data`'

cleanup() {
    /usr/bin/security delete-keychain "$keychain_path" >/dev/null 2>&1 || true
    if [[ "$temporary_root" == "${TMPDIR:-/tmp}"/passvault-apple-signing-test.* &&
        -d "$temporary_root" ]]; then
        find "$temporary_root" -type f -exec chmod 600 {} + 2>/dev/null || true
        find "$temporary_root" -type f -exec rm -f -- {} + 2>/dev/null || true
        find "$temporary_root" -depth -type d -exec rmdir {} + 2>/dev/null || true
    fi
    fixture_password=""
    unset fixture_password
}
trap cleanup EXIT
trap 'exit 1' HUP INT TERM

certificate_path="$temporary_root/certificate.pem"
private_key_path="$temporary_root/private-key.pem"
pkcs12_path="$temporary_root/identity.p12"
output_path="$temporary_root/import-output.txt"
import_temporary_root="$temporary_root/import-temporary"
identity_name="Developer ID Application: PassVault Fixture (TESTTEAM01)"
mkdir -m 700 "$import_temporary_root"

"$openssl_binary" req -x509 -newkey rsa:2048 -nodes -days 1 \
    -subj "/CN=$identity_name" \
    -addext "basicConstraints=critical,CA:FALSE" \
    -addext "keyUsage=critical,digitalSignature" \
    -addext "extendedKeyUsage=codeSigning" \
    -keyout "$private_key_path" -out "$certificate_path" >/dev/null 2>&1
"$openssl_binary" pkcs12 -export -legacy \
    -in "$certificate_path" -inkey "$private_key_path" -out "$pkcs12_path" \
    -passout fd:3 3<<<"$fixture_password" >/dev/null 2>&1

/usr/bin/security create-keychain -p "$keychain_password" "$keychain_path"
/usr/bin/security unlock-keychain -p "$keychain_password" "$keychain_path"
printf '%s\n' "$fixture_password" |
    TMPDIR="$import_temporary_root" \
    "$repository_root/scripts/import-apple-signing-certificate.sh" \
        "$pkcs12_path" "$keychain_path" > "$output_path"

if [[ "$(<"$output_path")" == *"$fixture_password"* ]]; then
    echo "The certificate import logged its password." >&2
    exit 1
fi
/usr/bin/security find-identity -p codesigning "$keychain_path" > "$temporary_root/identities.txt"
grep -Fq "$identity_name" "$temporary_root/identities.txt"
/usr/bin/security find-key -t private -s "$keychain_path" >/dev/null
if /usr/bin/security export -k "$keychain_path" -t privKeys -f pkcs12 \
    -P fixture-output-password \
    -o "$temporary_root/exported-private.p12" > "$temporary_root/export-output.txt" 2>&1; then
    echo "The imported Apple signing private key remained extractable." >&2
    exit 1
fi

if printf '%s\n' 'incorrect fixture password' |
    TMPDIR="$import_temporary_root" \
    "$repository_root/scripts/import-apple-signing-certificate.sh" \
        "$pkcs12_path" "$keychain_path" > "$output_path" 2>&1; then
    echo "The Apple signing importer accepted an incorrect password." >&2
    exit 1
fi
if [[ "$(<"$output_path")" == *'incorrect fixture password'* ]]; then
    echo "The certificate import logged an incorrect password." >&2
    exit 1
fi

if find "$import_temporary_root" -maxdepth 1 -type d \
    -name 'passvault-pkcs12-import.*' -print -quit | grep -q .; then
    echo "The Apple signing importer left a temporary directory behind." >&2
    exit 1
fi

echo "Apple signing secret-handling tests passed."
