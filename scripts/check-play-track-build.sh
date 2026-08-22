#!/usr/bin/env bash

set -euo pipefail

track="${1:-}"
build_number="${2:-}"
package_name="${ANDROID_PACKAGE_NAME:-}"
access_token="${GOOGLE_OAUTH_ACCESS_TOKEN:-}"

if [[ ! "$track" =~ ^(internal|alpha|beta|production)$ || ! "$build_number" =~ ^[1-9][0-9]*$ ]] ||
    (( ${#build_number} > 10 )) || (( 10#$build_number > 2100000000 )); then
    echo "Usage: $0 internal|alpha|beta|production <build-number>" >&2
    exit 2
fi
if [[ "$package_name" != "com.passvault.android" || -z "$access_token" ]]; then
    echo "ANDROID_PACKAGE_NAME and GOOGLE_OAUTH_ACCESS_TOKEN are required." >&2
    exit 1
fi
for command_name in curl jq ruby; do
    command -v "$command_name" >/dev/null 2>&1 || {
        echo "$command_name is required." >&2
        exit 1
    }
done

curl_common=(--silent --show-error --connect-timeout 15 --max-time 60)

umask 077
temporary_parent="${TMPDIR:-/tmp}"
temporary_parent="${temporary_parent%/}"
temporary_root="$(mktemp -d "$temporary_parent/passvault-play-track.XXXXXX")"
chmod 700 "$temporary_root"
edit_id=""
auth_header="$temporary_root/authorization-header.txt"
printf 'Authorization: Bearer %s\n' "$access_token" > "$auth_header"
cleanup() {
    local original_status=$?
    local delete_status
    trap - EXIT INT TERM
    if [[ -n "$edit_id" ]]; then
        delete_status="$(curl "${curl_common[@]}" --request DELETE \
            --header "@$auth_header" \
            --output "$temporary_root/delete-edit.json" \
            --write-out '%{http_code}' \
            "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/$package_name/edits/$edit_id" \
            || true)"
        if [[ "$delete_status" != 200 && "$delete_status" != 204 && "$delete_status" != 404 ]]; then
            echo "Could not delete the disposable Play edit (HTTP $delete_status)." >&2
            original_status=1
        fi
    fi
    unset GOOGLE_OAUTH_ACCESS_TOKEN access_token
    if [[ "$temporary_root" == "$temporary_parent"/passvault-play-track.* ]]; then
        find "$temporary_root" -type f -exec rm -f -- {} +
        rmdir "$temporary_root" 2>/dev/null || true
    else
        echo "Refusing to remove an unexpected Play-track temporary path." >&2
        original_status=1
    fi
    exit "$original_status"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

api_root="https://androidpublisher.googleapis.com/androidpublisher/v3/applications/$package_name"
create_status="$(curl "${curl_common[@]}" --request POST \
    --header "@$auth_header" \
    --header 'Content-Type: application/json' \
    --data '{}' \
    --output "$temporary_root/edit.json" \
    --write-out '%{http_code}' \
    "$api_root/edits")"
[[ "$create_status" == 200 ]] || {
    echo "Could not create a read-only Play edit (HTTP $create_status)." >&2
    exit 1
}
edit_id="$(jq -r '.id // empty' "$temporary_root/edit.json")"
[[ "$edit_id" =~ ^[A-Za-z0-9._-]+$ ]] || {
    echo "Play returned an invalid disposable edit identifier." >&2
    exit 1
}

track_status="$(curl "${curl_common[@]}" --retry 2 --retry-delay 1 --retry-all-errors \
    --header "@$auth_header" \
    --output "$temporary_root/track.json" \
    --write-out '%{http_code}' \
    "$api_root/edits/$edit_id/tracks/$track")"
[[ "$track_status" == 200 ]] || {
    echo "Could not inspect Play track $track (HTTP $track_status)." >&2
    exit 1
}

ruby "$(dirname "$0")/verify-play-track-response.rb" \
    "$temporary_root/track.json" "$track" "$build_number"
