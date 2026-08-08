#!/usr/bin/env bash

set -euo pipefail

track="${1:-}"
build_number="${2:-}"
package_name="${ANDROID_PACKAGE_NAME:-}"
access_token="${GOOGLE_OAUTH_ACCESS_TOKEN:-}"

if [[ ! "$track" =~ ^(internal|beta|production)$ || ! "$build_number" =~ ^[1-9][0-9]*$ ]]; then
    echo "Usage: $0 internal|beta|production <build-number>" >&2
    exit 2
fi
if [[ -z "$package_name" || -z "$access_token" ]]; then
    echo "ANDROID_PACKAGE_NAME and GOOGLE_OAUTH_ACCESS_TOKEN are required." >&2
    exit 1
fi

temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-play-track.XXXXXX")"
edit_id=""
cleanup() {
    status=$?
    if [[ -n "$edit_id" ]]; then
        curl --silent --show-error --request DELETE \
            --header "Authorization: Bearer $access_token" \
            "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/$package_name/edits/$edit_id" \
            >/dev/null || true
    fi
    if [[ "$temporary_root" == "${TMPDIR:-/tmp}"/passvault-play-track.* ]]; then
        find "$temporary_root" -type f -exec rm -f -- {} +
        rmdir "$temporary_root" 2>/dev/null || true
    fi
    exit "$status"
}
trap cleanup EXIT INT TERM

api_root="https://androidpublisher.googleapis.com/androidpublisher/v3/applications/$package_name"
create_status="$(curl --silent --show-error --request POST \
    --header "Authorization: Bearer $access_token" \
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
test -n "$edit_id"

track_status="$(curl --silent --show-error \
    --header "Authorization: Bearer $access_token" \
    --output "$temporary_root/track.json" \
    --write-out '%{http_code}' \
    "$api_root/edits/$edit_id/tracks/$track")"
[[ "$track_status" == 200 ]] || {
    echo "Could not inspect Play track $track (HTTP $track_status)." >&2
    exit 1
}

if ! jq -e --arg build "$build_number" '
    any(.releases[]?;
      (.versionCodes // [] | map(tostring) | index($build)) != null and
      (.status == "completed" or .status == "inProgress")
    )
' "$temporary_root/track.json" >/dev/null; then
    echo "Build $build_number is not active on Google Play track $track." >&2
    exit 1
fi

echo "Google Play build $build_number is active on $track."
