#!/usr/bin/env bash

set -euo pipefail

role="${1:-}"
package_name="${ANDROID_PACKAGE_NAME:-}"
access_token="${GOOGLE_OAUTH_ACCESS_TOKEN:-}"

if [[ "$role" != "beta" && "$role" != "production" ]]; then
    echo "Usage: $0 beta|production" >&2
    exit 2
fi
if [[ "$package_name" != "com.passvault.android" ]]; then
    echo "The OIDC check is restricted to com.passvault.android." >&2
    exit 1
fi
if [[ -z "$access_token" ]]; then
    echo "A short-lived federated Google OAuth token is required." >&2
    exit 1
fi

temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-play-access.XXXXXX")"
chmod 700 "$temporary_root"
api_root="https://androidpublisher.googleapis.com/androidpublisher/v3/applications/$package_name"
edit_id=""
edit_deleted=false

api_request() {
    local method="$1"
    local url="$2"
    local output_file="$3"
    local body_file="${4:-}"
    local arguments=(
        --silent --show-error
        --request "$method"
        --header "Authorization: Bearer $access_token"
        --header "Accept: application/json"
        --output "$output_file"
        --write-out '%{http_code}'
    )
    if [[ -n "$body_file" ]]; then
        arguments+=(--header "Content-Type: application/json" --data-binary "@$body_file")
    fi
    curl "${arguments[@]}" "$url"
}

safe_api_error() {
    local operation="$1"
    local status_code="$2"
    local response_file="$3"
    local api_status
    api_status="$(jq -r '.error.status // "UNKNOWN"' "$response_file" 2>/dev/null || true)"
    echo "$operation failed: HTTP $status_code ($api_status)." >&2
}

delete_edit() {
    local response_file status_code
    [[ -z "$edit_id" || "$edit_deleted" == "true" ]] && return 0
    response_file="$temporary_root/delete-edit.json"
    status_code="$(api_request DELETE "$api_root/edits/$edit_id" "$response_file" || true)"
    if [[ "$status_code" == "200" || "$status_code" == "204" || "$status_code" == "404" ]]; then
        edit_deleted=true
        echo "PLAY_EDIT_DELETED=PASS"
        return 0
    fi
    safe_api_error "Disposable edit deletion" "$status_code" "$response_file"
    return 1
}

cleanup() {
    local original_status=$?
    unset GOOGLE_OAUTH_ACCESS_TOKEN access_token
    if [[ -n "$edit_id" && "$edit_deleted" != "true" ]]; then
        delete_edit || original_status=1
    fi
    if [[ "$temporary_root" == "${TMPDIR:-/tmp}"/passvault-play-access.* && -d "$temporary_root" ]]; then
        find "$temporary_root" -type f -exec chmod 600 {} + 2>/dev/null || true
        find "$temporary_root" -type f -exec rm -f -- {} + 2>/dev/null || true
        find "$temporary_root" -depth -type d -exec rmdir {} + 2>/dev/null || true
    fi
    exit "$original_status"
}
trap cleanup EXIT
trap 'exit 130' INT TERM

empty_body="$temporary_root/empty.json"
printf '{}\n' > "$empty_body"
edit_response="$temporary_root/create-edit.json"
create_status="$(api_request POST "$api_root/edits" "$edit_response" "$empty_body")"
if [[ "$create_status" != "200" ]]; then
    safe_api_error "OIDC/package access" "$create_status" "$edit_response"
    exit 1
fi
edit_id="$(jq -r '.id // empty' "$edit_response")"
if [[ -z "$edit_id" ]]; then
    echo "Play returned no disposable edit identifier." >&2
    exit 1
fi

tracks_response="$temporary_root/tracks.json"
tracks_status="$(api_request GET "$api_root/edits/$edit_id/tracks" "$tracks_response")"
if [[ "$tracks_status" != "200" ]]; then
    safe_api_error "Track visibility" "$tracks_status" "$tracks_response"
    exit 1
fi

listings_response="$temporary_root/listings.json"
listings_status="$(api_request GET "$api_root/edits/$edit_id/listings" "$listings_response")"
if [[ "$listings_status" != "200" ]]; then
    safe_api_error "Listing visibility" "$listings_status" "$listings_response"
    exit 1
fi

production_track_visible="$(jq -r 'any(.tracks[]?; .track == "production")' "$tracks_response")"
track_count="$(jq -r '.tracks | length' "$tracks_response")"
listing_count="$(jq -r '.listings | length' "$listings_response")"

echo "PLAY_OIDC_AUTH=PASS"
echo "PLAY_ROLE=$role"
echo "PLAY_PACKAGE_VISIBLE=$package_name"
echo "PLAY_TRACK_READ=PASS"
echo "PLAY_LISTING_READ=PASS"
echo "PLAY_TRACK_COUNT=$track_count"
echo "PLAY_LISTING_COUNT=$listing_count"
echo "PLAY_PRODUCTION_TRACK_VISIBLE=$production_track_visible"

delete_edit
trap - EXIT INT TERM
unset GOOGLE_OAUTH_ACCESS_TOKEN access_token
find "$temporary_root" -type f -exec chmod 600 {} + 2>/dev/null || true
find "$temporary_root" -type f -exec rm -f -- {} + 2>/dev/null || true
find "$temporary_root" -depth -type d -exec rmdir {} + 2>/dev/null || true
echo "PLAY_ACCESS_CHECK=PASS"
