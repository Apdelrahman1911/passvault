#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <version>" >&2
    exit 1
fi

requested_version="$1"
repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
properties_path="$repository_root/version.properties"

if [[ ! -f "$properties_path" ]]; then
    echo "version.properties was not found." >&2
    exit 1
fi

property_value() {
    local key="$1"
    awk -F= -v key="$key" '
        $0 !~ /^[[:space:]]*#/ && $1 == key {
            value = substr($0, index($0, "=") + 1)
            gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
            print value
            found = 1
            exit
        }
        END { if (!found) exit 1 }
    ' "$properties_path"
}

version_major="$(property_value VERSION_MAJOR)"
version_minor="$(property_value VERSION_MINOR)"
version_patch="$(property_value VERSION_PATCH)"
version_name="$(property_value VERSION_NAME)"
version_code="$(property_value VERSION_CODE)"
build_number="$(property_value BUILD_NUMBER)"
release_channel="$(property_value RELEASE_CHANNEL)"
minimum_supported_version="$(property_value MIN_SUPPORTED_VERSION)"

for value_name in \
    version_major version_minor version_patch version_code build_number minimum_supported_version; do
    value="${!value_name}"
    if [[ ! "$value" =~ ^(0|[1-9][0-9]*)$ ]]; then
        echo "$value_name must be a non-negative integer." >&2
        exit 1
    fi
done

expected_version_name="$version_major.$version_minor.$version_patch"
if [[ "$version_name" != "$expected_version_name" ]]; then
    echo "VERSION_NAME does not match VERSION_MAJOR.MINOR.PATCH." >&2
    exit 1
fi

if [[ "$requested_version" != "$version_name" ]]; then
    echo "Requested release version does not match VERSION_NAME in version.properties." >&2
    exit 1
fi

if [[ ! "$requested_version" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
    echo "Release version must use MAJOR.MINOR.PATCH semantic versioning." >&2
    exit 1
fi

expected_version_code=$((10#$version_major * 1000000 + 10#$version_minor * 1000 + 10#$version_patch))
if (( version_code != expected_version_code )); then
    echo "VERSION_CODE does not match the documented version-code formula." >&2
    exit 1
fi

if (( version_code <= 0 || version_code > 2100000000 )); then
    echo "VERSION_CODE is outside Android's supported positive range." >&2
    exit 1
fi

if (( build_number <= 0 )); then
    echo "BUILD_NUMBER must be positive." >&2
    exit 1
fi

if (( minimum_supported_version > version_code )); then
    echo "MIN_SUPPORTED_VERSION cannot exceed VERSION_CODE." >&2
    exit 1
fi

case "$release_channel" in
    ALPHA|BETA|RC|RELEASE) ;;
    *)
        echo "RELEASE_CHANNEL must be ALPHA, BETA, RC, or RELEASE." >&2
        exit 1
        ;;
esac

required_metadata=(
    PUBLISHER_NAME
    COPYRIGHT_HOLDER
    SUPPORT_EMAIL
    SECURITY_EMAIL
    PRIVACY_POLICY_URL
    PROJECT_URL
)

missing_metadata=()
for name in "${required_metadata[@]}"; do
    if [[ -z "${!name:-}" ]]; then
        missing_metadata+=("$name")
    fi
done

if (( ${#missing_metadata[@]} > 0 )); then
    echo "Missing release metadata: ${missing_metadata[*]}" >&2
    exit 1
fi

placeholder_pattern='example\.(com|invalid)|localhost|127\.0\.0\.1|your verified|change[ -]?me|todo'
for name in "${required_metadata[@]}"; do
    value="${!name}"
    if grep -Eiq "$placeholder_pattern" <<<"$value"; then
        echo "$name contains a placeholder and cannot be used for a release." >&2
        exit 1
    fi
done

email_pattern='^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'
for name in SUPPORT_EMAIL SECURITY_EMAIL; do
    if [[ ! "${!name}" =~ $email_pattern ]]; then
        echo "$name is not a valid email address." >&2
        exit 1
    fi
done

for name in PRIVACY_POLICY_URL PROJECT_URL; do
    if [[ ! "${!name}" =~ ^https://[^[:space:]]+$ ]]; then
        echo "$name must be an HTTPS URL." >&2
        exit 1
    fi
done

if [[ -n "${ANDROID_STORE_LISTING_URL:-}" ]]; then
    if [[ ! "$ANDROID_STORE_LISTING_URL" =~ ^https://[^[:space:]]+$ ]] ||
        grep -Eiq "$placeholder_pattern" <<<"$ANDROID_STORE_LISTING_URL"; then
        echo "ANDROID_STORE_LISTING_URL must be a non-placeholder HTTPS URL." >&2
        exit 1
    fi
fi

echo "Release metadata validated for PassVault $requested_version ($release_channel)."
