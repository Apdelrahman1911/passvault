#!/usr/bin/env bash

# PKCS#12 validation helpers. Passwords are provided only through a named
# environment variable and are never printed or included in diagnostics.

PASSVAULT_OPENSSL_BINARY=""
PASSVAULT_P12_STATUS=""
PASSVAULT_P12_CERTIFICATE_FILE=""
PASSVAULT_P12_PRIVATE_KEY_FILE=""

passvault_select_openssl() {
    local candidate
    for candidate in \
        /opt/homebrew/opt/openssl@3/bin/openssl \
        /opt/homebrew/opt/openssl/bin/openssl \
        /usr/local/opt/openssl@3/bin/openssl \
        /usr/local/opt/openssl/bin/openssl; do
        if [[ -x "$candidate" ]]; then
            PASSVAULT_OPENSSL_BINARY="$candidate"
            return 0
        fi
    done
    if candidate="$(command -v openssl 2>/dev/null)" && [[ -n "$candidate" && -x "$candidate" ]]; then
        PASSVAULT_OPENSSL_BINARY="$candidate"
        return 0
    fi
    PASSVAULT_OPENSSL_BINARY=""
    return 1
}

passvault_classify_pkcs12_failure() {
    local diagnostic_file="$1"
    if grep -Eiq 'mac verify (error|failure)|invalid password|maybe wrong password' "$diagnostic_file"; then
        PASSVAULT_P12_STATUS="invalid_password"
    elif grep -Eiq 'unknown option.*legacy|unrecognized option.*legacy|unsupported|RC2-[0-9]+-CBC|legacy provider|unable to load provider|inner_evp_generic_fetch' \
        "$diagnostic_file"; then
        PASSVAULT_P12_STATUS="unsupported_legacy_cipher"
    else
        PASSVAULT_P12_STATUS="invalid_container"
    fi
}

passvault_validate_pkcs12() {
    local openssl_binary="$1"
    local p12_file="$2"
    local password_environment_name="$3"
    local output_directory="$4"
    local diagnostic_file certificate_file private_key_file certificate_public_key key_public_key

    PASSVAULT_P12_STATUS=""
    PASSVAULT_P12_CERTIFICATE_FILE=""
    PASSVAULT_P12_PRIVATE_KEY_FILE=""
    diagnostic_file="$output_directory/pkcs12-diagnostic.txt"
    certificate_file="$output_directory/pkcs12-certificate.pem"
    private_key_file="$output_directory/pkcs12-private-key.pem"
    certificate_public_key="$output_directory/pkcs12-certificate-public.der"
    key_public_key="$output_directory/pkcs12-private-public.der"
    : > "$diagnostic_file"
    chmod 600 "$diagnostic_file"

    if ! "$openssl_binary" pkcs12 -legacy -in "$p12_file" \
        -passin "env:$password_environment_name" -info -noout \
        >/dev/null 2>"$diagnostic_file"; then
        passvault_classify_pkcs12_failure "$diagnostic_file"
        return 1
    fi

    : > "$diagnostic_file"
    if ! "$openssl_binary" pkcs12 -legacy -in "$p12_file" -clcerts -nokeys \
        -passin "env:$password_environment_name" -out "$certificate_file" \
        >/dev/null 2>"$diagnostic_file" ||
        ! "$openssl_binary" x509 -in "$certificate_file" -noout >/dev/null 2>&1; then
        if grep -Eiq 'unsupported|RC2-[0-9]+-CBC|legacy provider|unable to load provider|inner_evp_generic_fetch' \
            "$diagnostic_file"; then
            PASSVAULT_P12_STATUS="unsupported_legacy_cipher"
        else
            PASSVAULT_P12_STATUS="missing_certificate"
        fi
        return 1
    fi

    : > "$diagnostic_file"
    if ! "$openssl_binary" pkcs12 -legacy -in "$p12_file" -nocerts -nodes \
        -passin "env:$password_environment_name" -out "$private_key_file" \
        >/dev/null 2>"$diagnostic_file" ||
        ! "$openssl_binary" pkey -in "$private_key_file" -check -noout >/dev/null 2>&1; then
        if grep -Eiq 'unsupported|legacy provider|unable to load provider|inner_evp_generic_fetch' \
            "$diagnostic_file"; then
            PASSVAULT_P12_STATUS="unsupported_legacy_cipher"
        else
            PASSVAULT_P12_STATUS="missing_private_key"
        fi
        return 1
    fi

    if ! "$openssl_binary" x509 -in "$certificate_file" -pubkey -noout 2>/dev/null |
        "$openssl_binary" pkey -pubin -outform DER -out "$certificate_public_key" 2>/dev/null ||
        ! "$openssl_binary" pkey -in "$private_key_file" -pubout -outform DER \
            -out "$key_public_key" 2>/dev/null ||
        ! cmp -s "$certificate_public_key" "$key_public_key"; then
        PASSVAULT_P12_STATUS="certificate_private_key_mismatch"
        return 1
    fi

    PASSVAULT_P12_STATUS="valid"
    PASSVAULT_P12_CERTIFICATE_FILE="$certificate_file"
    PASSVAULT_P12_PRIVATE_KEY_FILE="$private_key_file"
}
