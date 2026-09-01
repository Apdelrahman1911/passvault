#!/usr/bin/env bash

set -euo pipefail

if [[ $# -lt 4 || $# -gt 5 ]]; then
    echo "Usage: $0 <embedded.provisionprofile> <entitlements> <team-id> <bundle-id> [certificate-sha256]" >&2
    exit 2
fi

profile_path="$1"
entitlements_path="$2"
team_id="$3"
bundle_id="$4"
expected_certificate_sha256="${5:-}"
expected_application_identifier="$team_id.$bundle_id"

if [[ "$(uname -s)" != Darwin ]]; then
    echo "macOS provisioning-profile validation must run on macOS." >&2
    exit 1
fi
if [[ ! "$team_id" =~ ^[A-Z0-9]{10}$ ]]; then
    echo "The macOS Apple Team ID must contain exactly 10 uppercase letters or digits." >&2
    exit 1
fi
if [[ ! "$bundle_id" =~ ^[A-Za-z0-9][A-Za-z0-9.-]*[A-Za-z0-9]$ ]]; then
    echo "The macOS bundle identifier is invalid." >&2
    exit 1
fi
if [[ ! -f "$profile_path" || -L "$profile_path" || "$(basename "$profile_path")" != embedded.provisionprofile ]]; then
    echo "A real, non-symlink embedded.provisionprofile is required." >&2
    exit 1
fi
if [[ ! -f "$entitlements_path" || -L "$entitlements_path" ]]; then
    echo "A real, non-symlink macOS entitlements file is required." >&2
    exit 1
fi

for command_name in security plutil openssl date mktemp; do
    command -v "$command_name" >/dev/null 2>&1 || {
        echo "$command_name is required for macOS provisioning-profile validation." >&2
        exit 1
    }
done

profile_plist="$(mktemp -t passvault-macos-profile)"
profile_certificate=""
cleanup() {
    rm -f -- "$profile_plist"
    if [[ -n "$profile_certificate" ]]; then
        rm -f -- "$profile_certificate"
    fi
}
trap cleanup EXIT

security cms -D -i "$profile_path" -o "$profile_plist" >/dev/null
plutil -lint "$profile_plist" >/dev/null
plutil -lint "$entitlements_path" >/dev/null

profile_value() {
    plutil -extract "$1" raw -o - "$profile_plist" 2>/dev/null
}

profile_entitlement_value() {
    /usr/libexec/PlistBuddy -c "Print :Entitlements:$1" "$profile_plist" 2>/dev/null
}

entitlement_value() {
    /usr/libexec/PlistBuddy -c "Print :$1" "$entitlements_path" 2>/dev/null
}

[[ "$(profile_value TeamIdentifier.0)" == "$team_id" ]] || {
    echo "The macOS provisioning profile belongs to a different Apple team." >&2
    exit 1
}
[[ "$(profile_entitlement_value com.apple.developer.team-identifier)" == "$team_id" ]] || {
    echo "The macOS provisioning profile has the wrong team entitlement." >&2
    exit 1
}
[[ "$(profile_entitlement_value com.apple.application-identifier)" == "$expected_application_identifier" ]] || {
    echo "The macOS provisioning profile does not authorize $bundle_id." >&2
    exit 1
}
[[ "$(profile_value ProvisionsAllDevices)" == true ]] || {
    echo "The macOS provisioning profile is not a Developer ID distribution profile." >&2
    exit 1
}
if profile_entitlement_value get-task-allow 2>/dev/null | grep -Fqx true; then
    echo "The macOS provisioning profile permits debugging and is not a production Developer ID profile." >&2
    exit 1
fi

profile_platforms="$(/usr/libexec/PlistBuddy -c 'Print :Platform' "$profile_plist" 2>/dev/null)"
if ! grep -Eq '(^|[[:space:]])(OSX|macOS)($|[[:space:]])' <<<"$profile_platforms"; then
    echo "The provisioning profile does not target macOS." >&2
    exit 1
fi
profile_groups="$(/usr/libexec/PlistBuddy -c 'Print :Entitlements:keychain-access-groups' "$profile_plist" 2>/dev/null)"
if ! grep -Fqx "    $expected_application_identifier" <<<"$profile_groups" &&
    ! grep -Fqx "    $team_id.*" <<<"$profile_groups"; then
    echo "The provisioning profile does not authorize PassVault's Keychain access group." >&2
    exit 1
fi

expiration="$(profile_value ExpirationDate)"
expiration_epoch="$(date -u -j -f '%Y-%m-%dT%H:%M:%SZ' "$expiration" '+%s' 2>/dev/null || true)"
current_epoch="$(date -u '+%s')"
if [[ -z "$expiration_epoch" || "$expiration_epoch" -le "$current_epoch" ]]; then
    echo "The macOS provisioning profile is expired or has an invalid expiration date." >&2
    exit 1
fi

[[ "$(entitlement_value com.apple.application-identifier)" == "$expected_application_identifier" ]] || {
    echo "The signed-app entitlements have the wrong application identifier." >&2
    exit 1
}
[[ "$(entitlement_value com.apple.developer.team-identifier)" == "$team_id" ]] || {
    echo "The signed-app entitlements have the wrong Apple team." >&2
    exit 1
}
[[ "$(entitlement_value keychain-access-groups:0)" == "$expected_application_identifier" ]] || {
    echo "The signed-app entitlements have the wrong Keychain access group." >&2
    exit 1
}
for entitlement in com.apple.security.cs.allow-jit; do
    [[ "$(entitlement_value "$entitlement")" == true ]] || {
        echo "Required JVM entitlement is missing: $entitlement" >&2
        exit 1
    }
done
for entitlement in \
    com.apple.security.cs.allow-unsigned-executable-memory \
    com.apple.security.cs.disable-library-validation; do
    if entitlement_value "$entitlement" | grep -Fqx true; then
        echo "Forbidden macOS hardened-runtime exception: $entitlement" >&2
        exit 1
    fi
done
if entitlement_value com.apple.security.get-task-allow 2>/dev/null | grep -Fqx true; then
    echo "The PassVault macOS entitlements must not permit debugging." >&2
    exit 1
fi

if [[ -n "$expected_certificate_sha256" ]]; then
    normalized_expected="$(printf '%s' "$expected_certificate_sha256" |
        tr -cd '0-9A-Fa-f' | tr '[:lower:]' '[:upper:]')"
    [[ "$normalized_expected" =~ ^[0-9A-F]{64}$ ]] || {
        echo "The expected Developer ID certificate SHA-256 fingerprint is invalid." >&2
        exit 1
    }
    certificate_count="$(plutil -extract DeveloperCertificates xml1 -o - "$profile_plist" |
        grep -c '<data>')"
    certificate_matched=false
    for (( certificate_index = 0; certificate_index < certificate_count; certificate_index += 1 )); do
        profile_certificate="$(mktemp -t passvault-profile-certificate).der"
        profile_value "DeveloperCertificates.$certificate_index" |
            openssl base64 -d -A -out "$profile_certificate"
        actual_fingerprint="$(openssl x509 -inform DER -in "$profile_certificate" -noout -fingerprint -sha256 |
            sed 's/^[^=]*=//' | tr -cd '0-9A-Fa-f' | tr '[:lower:]' '[:upper:]')"
        rm -f -- "$profile_certificate"
        profile_certificate=""
        if [[ "$actual_fingerprint" == "$normalized_expected" ]]; then
            certificate_matched=true
            break
        fi
    done
    [[ "$certificate_matched" == true ]] || {
        echo "The provisioning profile does not authorize the pinned Developer ID certificate." >&2
        exit 1
    }
fi

echo "Validated Developer ID profile for $expected_application_identifier with its biometric Keychain entitlement."
