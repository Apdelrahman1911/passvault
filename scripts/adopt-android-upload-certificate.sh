#!/usr/bin/env bash

set -euo pipefail

if [[ "${1:-}" != "--apply" ]]; then
    echo "Usage: $0 --apply" >&2
    echo "This updates only the tracked public Android certificate and fingerprint." >&2
    exit 2
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
values_file="$repository_root/release/private/values.env"
keystore_file="$repository_root/release/private/files/android-upload-keystore.jks"
temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-android-cert.XXXXXX")"
chmod 700 "$temporary_root"

cleanup() {
    unset PASSVAULT_ADOPT_STOREPASS PASSVAULT_ADOPT_KEYPASS
    find "$temporary_root" -type f -exec chmod 600 {} + 2>/dev/null || true
    find "$temporary_root" -type f -exec rm -f -- {} + 2>/dev/null || true
    find "$temporary_root" -depth -type d -exec rmdir {} + 2>/dev/null || true
}
trap cleanup EXIT

cd "$repository_root"
if ! git check-ignore -q release/private/values.env || [[ -n "$(git ls-files release/private)" ]]; then
    echo "release/private/ is not safely ignored and untracked." >&2
    exit 1
fi
if [[ ! -s "$values_file" || ! -s "$keystore_file" || -L "$values_file" || -L "$keystore_file" ]]; then
    echo "The private values file or Android upload keystore is missing or unsafe." >&2
    exit 1
fi

read_value() {
    local key="$1"
    awk -F= -v key="$key" '
        $0 !~ /^[[:space:]]*#/ && $1 == key {
            print substr($0, index($0, "=") + 1)
            found = 1
            exit
        }
        END { if (!found) exit 1 }
    ' "$values_file"
}

KEYSTORE_PASSWORD="$(read_value KEYSTORE_PASSWORD)"
KEY_ALIAS="$(read_value KEY_ALIAS)"
KEY_PASSWORD="$(read_value KEY_PASSWORD)"
if [[ -z "$KEYSTORE_PASSWORD" || -z "$KEY_ALIAS" || -z "$KEY_PASSWORD" ]]; then
    echo "Android signing values are incomplete." >&2
    exit 1
fi

expected_alias="$(tr -d '\r\n' < release/android/passvault-upload-alias.txt)"
if [[ "$KEY_ALIAS" != "$expected_alias" ]]; then
    echo "KEY_ALIAS must match the canonical Android upload alias: $expected_alias" >&2
    exit 1
fi

export PASSVAULT_ADOPT_STOREPASS="$KEYSTORE_PASSWORD"
export PASSVAULT_ADOPT_KEYPASS="$KEY_PASSWORD"
candidate_certificate="$temporary_root/candidate.pem"
key_check="$temporary_root/key-check.p12"
if ! keytool -importkeystore -srckeystore "$keystore_file" \
    -srcstorepass:env PASSVAULT_ADOPT_STOREPASS -srcalias "$KEY_ALIAS" \
    -srckeypass:env PASSVAULT_ADOPT_KEYPASS -destkeystore "$key_check" \
    -deststoretype PKCS12 -deststorepass passvault-adoption-check \
    -destkeypass passvault-adoption-check -noprompt >/dev/null 2>&1 ||
    ! keytool -exportcert -rfc -keystore "$keystore_file" \
    -storepass:env PASSVAULT_ADOPT_STOREPASS -alias "$KEY_ALIAS" \
    -file "$candidate_certificate" >/dev/null 2>&1; then
    echo "The JKS alias, store password, or key password is invalid." >&2
    exit 1
fi

fingerprint="$(keytool -printcert -file "$candidate_certificate" 2>/dev/null |
    awk -F'SHA256:' '/SHA256:/ { gsub(/[[:space:]]/, "", $2); print toupper($2); exit }')"
if [[ ! "${fingerprint//:/}" =~ ^[0-9A-F]{64}$ ]]; then
    echo "The public certificate SHA-256 fingerprint could not be determined." >&2
    exit 1
fi

echo "Candidate public certificate SHA-256: $fingerprint"
read -r -p "Type ADOPT to pin this new upload certificate: " confirmation
if [[ "$confirmation" != "ADOPT" ]]; then
    echo "Certificate adoption cancelled; no tracked file was changed." >&2
    exit 1
fi

install -m 0644 "$candidate_certificate" release/android/passvault-release-cert.pem
printf '%s\n' "$fingerprint" > release/android/passvault-release-cert.sha256
echo "The tracked public Android upload certificate and SHA-256 fingerprint were updated."
echo "The original private keystore was not changed."
