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
keychain_paths=()
cleanup() {
    local result=$?
    local owned_keychain all_deleted=true
    trap - EXIT
    for owned_keychain in "${keychain_paths[@]:-}"; do
        [[ -n "$owned_keychain" ]] || continue
        if ! /usr/bin/security delete-keychain "$owned_keychain" >/dev/null 2>&1; then
            echo "Synthetic test keychain cleanup failed; retaining its private root." >&2
            touch "$temporary_root/CLEANUP_HOLD"
            result=1
            all_deleted=false
        fi
    done
    if [[ "$all_deleted" == true &&
        "$temporary_root" == "${TMPDIR:-/tmp}"/passvault-apple-signing-test.* &&
        -d "$temporary_root" && ! -L "$temporary_root" ]]; then
        if ! rm -rf -- "$temporary_root"; then
            echo "Synthetic signing fixture cleanup failed." >&2
            result=1
        fi
    fi
    unset fixture_password keychain_password
    exit "$result"
}
trap cleanup EXIT
trap 'exit 1' HUP INT TERM
keychain_password="$(openssl rand -hex 32)"
# Command-looking characters verify that the password is data, not shell input.
# shellcheck disable=SC2016
fixture_password='fixture p@$$word; $(must-not-run) `still-data`'

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

keychain_paths+=("$keychain_path") # Partial creation still requires deletion.
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

# Reproduce the legacy live-pipe problem and test the fix on a fresh keychain,
# not an already-imported key that could mask a failed second import.
keychain_path="$temporary_root/fragmented.keychain-db"
keychain_paths+=("$keychain_path")
/usr/bin/security create-keychain -p "$keychain_password" "$keychain_path"
/usr/bin/security unlock-keychain -p "$keychain_password" "$keychain_path"
/usr/bin/security import "$certificate_path" -k "$keychain_path" -t cert -f pemseq >/dev/null
fragmented_key() {
    "$openssl_binary" pkey -in "$private_key_path" -traditional 2>/dev/null |
        bash "$repository_root/scripts/ci-python.sh" -c '
import sys, time
data = sys.stdin.buffer.read()
try:
    sys.stdout.buffer.write(data[:1]); sys.stdout.buffer.flush()
    time.sleep(1)
    sys.stdout.buffer.write(data[1:]); sys.stdout.buffer.flush()
except BrokenPipeError:
    pass  # Expected only when the legacy importer rejects the incomplete pipe.
'
}
# Hold the remainder while security samples the pipe. Unlike a sleep-based
# negative control, its byte count cannot become complete on a slow CI host.
if "$openssl_binary" pkey -in "$private_key_path" -traditional 2>/dev/null |
    bash "$repository_root/scripts/ci-python.sh" -c '
import os, subprocess, sys
data = sys.stdin.buffer.read()
reader, writer = os.pipe()
try:
    os.write(writer, data[:1])
    try:
        result = subprocess.run([
            "/usr/bin/security", "import", "/dev/stdin", "-k", sys.argv[1],
            "-t", "priv", "-f", "openssl", "-x",
            "-T", "/usr/bin/codesign", "-T", "/usr/bin/security",
        ], stdin=reader, timeout=10, check=False)
    except subprocess.TimeoutExpired:
        sys.exit(0)  # A true stream reader invalidates this negative control.
    sys.exit(result.returncode)
finally:
    os.close(reader); os.close(writer)
' "$keychain_path" > "$output_path" 2>&1; then
    echo "Legacy live-pipe negative control did not reject incomplete input promptly." >&2
    exit 1
fi
if /usr/bin/security find-key -t private -s "$keychain_path" >/dev/null 2>&1; then
    echo "The negative control left a private key; fragmented import would be masked." >&2
    exit 1
fi
for input_size in 0 65537; do
    if bash "$repository_root/scripts/ci-python.sh" -c \
        'import sys; sys.stdout.buffer.write(b"x" * int(sys.argv[1]))' "$input_size" |
        bash "$repository_root/scripts/ci-python.sh" \
            "$repository_root/scripts/import-apple-key-from-stdin.py" "$keychain_path" \
            > "$output_path" 2>&1; then
        echo "The private-key helper accepted empty or oversized input." >&2
        exit 1
    fi
    grep -Fq 'input is empty or exceeds the size limit' "$output_path"
done
fragmented_key | bash "$repository_root/scripts/ci-python.sh" \
    "$repository_root/scripts/import-apple-key-from-stdin.py" "$keychain_path" > "$output_path"
/usr/bin/security find-identity -p codesigning "$keychain_path" > "$temporary_root/fragmented-identities.txt"
grep -Fq "$identity_name" "$temporary_root/fragmented-identities.txt"
/usr/bin/security find-key -t private -s "$keychain_path" >/dev/null
if /usr/bin/security export -k "$keychain_path" -t privKeys -f pkcs12 \
    -P fixture-output-password -o "$temporary_root/fragmented-export.p12" \
    > "$temporary_root/export-output.txt" 2>&1; then
    echo "The fragmented-input private key remained extractable." >&2
    exit 1
fi
echo "Legacy incomplete-pipe refusal and complete fragmented-input import verified."

echo "Apple signing secret-handling tests passed."
