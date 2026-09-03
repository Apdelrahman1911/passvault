#!/usr/bin/env bash

# Imports a password-protected PKCS#12 signing identity without placing the
# password or an unencrypted private key in a child process argument list.

set +x
set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "Usage: $0 <pkcs12-path> <keychain-path>" >&2
    exit 2
fi

pkcs12_path="$1"
keychain_path="$2"
repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "Apple signing identities can only be imported on macOS." >&2
    exit 1
fi
if [[ ! -f "$pkcs12_path" || -L "$pkcs12_path" ]]; then
    echo "The PKCS#12 input must be a regular, non-symlink file." >&2
    exit 1
fi
if [[ ! -f "$keychain_path" || -L "$keychain_path" ]]; then
    echo "The destination keychain must already exist and must not be a symlink." >&2
    exit 1
fi

# The caller supplies one password line on standard input. It remains a shell
# value and is forwarded to OpenSSL over an inherited file descriptor, never
# as argv or an exported environment variable.
pkcs12_password="$(cat)"
if [[ -z "$pkcs12_password" || "$pkcs12_password" == *$'\n'* ||
    "$pkcs12_password" == *$'\r'* || ${#pkcs12_password} -gt 4096 ]]; then
    echo "The PKCS#12 password must be one non-empty line of at most 4096 characters." >&2
    exit 1
fi

# shellcheck disable=SC1091 # Resolved from the runtime repository root.
source "$repository_root/scripts/lib/pkcs12-validation.sh"
if ! passvault_select_openssl; then
    echo "OpenSSL is required to import the Apple signing identity." >&2
    exit 1
fi
openssl_binary="$PASSVAULT_OPENSSL_BINARY"

umask 077
temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-pkcs12-import.XXXXXX")"
certificate_file="$temporary_root/certificates.pem"

cleanup() {
    pkcs12_password=""
    unset pkcs12_password
    if [[ "$temporary_root" == "${TMPDIR:-/tmp}"/passvault-pkcs12-import.* &&
        -d "$temporary_root" ]]; then
        find "$temporary_root" -type f -exec chmod 600 {} + 2>/dev/null || true
        find "$temporary_root" -type f -exec rm -f -- {} + 2>/dev/null || true
        find "$temporary_root" -depth -type d -exec rmdir {} + 2>/dev/null || true
    fi
}
trap cleanup EXIT
trap 'exit 1' HUP INT TERM

read_pkcs12() {
    "$openssl_binary" pkcs12 -legacy -in "$pkcs12_path" \
        -passin fd:3 "$@" 3<<<"$pkcs12_password"
}

if ! read_pkcs12 -info -noout >/dev/null 2>&1; then
    echo "The PKCS#12 input or password is invalid or unsupported." >&2
    exit 1
fi

certificate_public_key_sha256="$({
    read_pkcs12 -clcerts -nokeys 2>/dev/null |
        "$openssl_binary" x509 -pubkey -noout 2>/dev/null |
        "$openssl_binary" pkey -pubin -outform DER 2>/dev/null |
        shasum -a 256 | awk '{ print $1 }'
} || true)"
private_public_key_sha256="$({
    read_pkcs12 -nocerts -nodes 2>/dev/null |
        "$openssl_binary" pkey -pubout -outform DER 2>/dev/null |
        shasum -a 256 | awk '{ print $1 }'
} || true)"
if [[ ! "$certificate_public_key_sha256" =~ ^[0-9a-f]{64}$ ||
    "$certificate_public_key_sha256" != "$private_public_key_sha256" ]]; then
    echo "The PKCS#12 certificate and private key do not match." >&2
    exit 1
fi

if ! read_pkcs12 -nokeys > "$certificate_file" 2>/dev/null ||
    ! /usr/bin/security import "$certificate_file" \
        -k "$keychain_path" -t cert -f pemseq >/dev/null; then
    echo "Unable to import the Apple signing certificate chain." >&2
    exit 1
fi

if ! read_pkcs12 -nocerts -nodes 2>/dev/null |
    "$openssl_binary" pkey -traditional 2>/dev/null |
    /usr/bin/security import /dev/stdin \
        -k "$keychain_path" -t priv -f openssl -x \
        -T /usr/bin/codesign -T /usr/bin/security >/dev/null; then
    echo "Unable to import the Apple signing private key." >&2
    exit 1
fi

pkcs12_password=""
unset pkcs12_password
echo "Imported the Apple signing identity without a password argument."
