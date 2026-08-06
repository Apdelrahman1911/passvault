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

details_response="$temporary_root/details.json"
details_status="$(api_request GET "$api_root/edits/$edit_id/details" "$details_response")"
if [[ "$details_status" != "200" ]]; then
    safe_api_error "App-details visibility" "$details_status" "$details_response"
    exit 1
fi

production_track_visible="$(jq -r 'any(.tracks[]?; .track == "production")' "$tracks_response")"
track_count="$(jq -r '.tracks | length' "$tracks_response")"
listing_count="$(jq -r '.listings | length' "$listings_response")"
listing_locales="$(jq -r '[.listings[]?.language] | sort | join(",")' "$listings_response")"
track_names="$(jq -r '[.tracks[]?.track] | sort | join(",")' "$tracks_response")"
release_count="$(jq -r '[.tracks[]?.releases[]?] | length' "$tracks_response")"
default_language="$(jq -r '.defaultLanguage // "UNSET"' "$details_response")"
contact_email_configured="$(jq -r '((.contactEmail // "") | length) > 0' "$details_response")"
contact_phone_configured="$(jq -r '((.contactPhone // "") | length) > 0' "$details_response")"
contact_website_configured="$(jq -r '((.contactWebsite // "") | length) > 0' "$details_response")"

listing_status() {
    local locale="$1"
    jq -r --arg locale "$locale" '
        [.listings[]? | select(.language == $locale)] | first as $listing |
        if $listing == null then "MISSING"
        elif (($listing.title // "") | length) > 0 and
             (($listing.shortDescription // "") | length) > 0 and
             (($listing.fullDescription // "") | length) > 0 then "COMPLETE"
        else "PARTIAL"
        end
    ' "$listings_response"
}

listing_character_count() {
    local locale="$1"
    local field="$2"
    jq -r --arg locale "$locale" --arg field "$field" '
        [.listings[]? | select(.language == $locale)] | first as $listing |
        if $listing == null then 0 else (($listing[$field] // "") | length) end
    ' "$listings_response"
}

image_count() {
    local locale="$1"
    local image_type="$2"
    local response_file="$temporary_root/images-${locale}-${image_type}.json"
    local status_code
    status_code="$(api_request GET \
        "$api_root/edits/$edit_id/listings/$locale/$image_type" "$response_file" || true)"
    if [[ "$status_code" == "200" ]]; then
        jq -r '.images | length' "$response_file"
    elif [[ "$status_code" == "404" ]]; then
        echo 0
    else
        safe_api_error "Image inventory ($locale/$image_type)" "$status_code" "$response_file"
        echo "UNAVAILABLE"
    fi
}

track_release_count() {
    local track="$1"
    jq -r --arg track "$track" \
        '[.tracks[]? | select(.track == $track) | .releases[]?] | length' "$tracks_response"
}

track_country_count() {
    local track="$1"
    local response_file="$temporary_root/countries-${track}.json"
    local status_code
    status_code="$(api_request GET \
        "$api_root/edits/$edit_id/countryAvailability/$track" "$response_file" || true)"
    if [[ "$status_code" == "200" ]]; then
        jq -r '.countries | length' "$response_file"
    elif [[ "$status_code" == "204" || "$status_code" == "400" || "$status_code" == "404" ]]; then
        echo "NOT_CONFIGURED"
    else
        safe_api_error "Country availability ($track)" "$status_code" "$response_file"
        echo "UNAVAILABLE"
    fi
}

track_group_count() {
    local track="$1"
    local response_file="$temporary_root/testers-${track}.json"
    local status_code
    status_code="$(api_request GET \
        "$api_root/edits/$edit_id/testers/$track" "$response_file" || true)"
    if [[ "$status_code" == "200" ]]; then
        jq -r '.googleGroups | length' "$response_file"
    elif [[ "$status_code" == "400" || "$status_code" == "404" ]]; then
        echo 0
    else
        safe_api_error "Tester-group inventory ($track)" "$status_code" "$response_file"
        echo "UNAVAILABLE"
    fi
}

en_us_listing_status="$(listing_status en-US)"
ar_listing_status="$(listing_status ar)"
en_us_icon_count="$(image_count en-US icon)"
en_us_feature_count="$(image_count en-US featureGraphic)"
en_us_phone_count="$(image_count en-US phoneScreenshots)"
ar_icon_count="$(image_count ar icon)"
ar_feature_count="$(image_count ar featureGraphic)"
ar_phone_count="$(image_count ar phoneScreenshots)"
internal_country_count="$(track_country_count internal)"
production_country_count="$(track_country_count production)"
internal_group_count="$(track_group_count internal)"
beta_group_count="$(track_group_count beta)"

validate_response="$temporary_root/validate-edit.json"
validate_status="$(api_request POST "$api_root/edits/$edit_id:validate" "$validate_response" || true)"
if [[ "$validate_status" == "200" ]]; then
    empty_edit_validation="PASS"
else
    validation_api_status="$(jq -r '.error.status // "UNKNOWN"' "$validate_response" 2>/dev/null || true)"
    empty_edit_validation="HTTP_${validate_status}_${validation_api_status}"
fi

echo "PLAY_OIDC_AUTH=PASS"
echo "PLAY_ROLE=$role"
echo "PLAY_PACKAGE_VISIBLE=$package_name"
echo "PLAY_TRACK_READ=PASS"
echo "PLAY_LISTING_READ=PASS"
echo "PLAY_DETAILS_READ=PASS"
echo "PLAY_TRACK_COUNT=$track_count"
echo "PLAY_TRACK_NAMES=$track_names"
echo "PLAY_RELEASE_COUNT=$release_count"
for audited_track in internal alpha beta production; do
    echo "PLAY_${audited_track^^}_RELEASE_COUNT=$(track_release_count "$audited_track")"
done
echo "PLAY_LISTING_COUNT=$listing_count"
echo "PLAY_LISTING_LOCALES=$listing_locales"
echo "PLAY_PRODUCTION_TRACK_VISIBLE=$production_track_visible"
echo "PLAY_DEFAULT_LANGUAGE=$default_language"
echo "PLAY_CONTACT_EMAIL_CONFIGURED=$contact_email_configured"
echo "PLAY_CONTACT_PHONE_CONFIGURED=$contact_phone_configured"
echo "PLAY_CONTACT_WEBSITE_CONFIGURED=$contact_website_configured"
echo "PLAY_LISTING_EN_US=$en_us_listing_status"
echo "PLAY_LISTING_EN_US_TITLE_CHARACTERS=$(listing_character_count en-US title)"
echo "PLAY_LISTING_EN_US_SHORT_DESCRIPTION_CHARACTERS=$(listing_character_count en-US shortDescription)"
echo "PLAY_LISTING_EN_US_FULL_DESCRIPTION_CHARACTERS=$(listing_character_count en-US fullDescription)"
echo "PLAY_LISTING_AR=$ar_listing_status"
echo "PLAY_LISTING_AR_TITLE_CHARACTERS=$(listing_character_count ar title)"
echo "PLAY_LISTING_AR_SHORT_DESCRIPTION_CHARACTERS=$(listing_character_count ar shortDescription)"
echo "PLAY_LISTING_AR_FULL_DESCRIPTION_CHARACTERS=$(listing_character_count ar fullDescription)"
echo "PLAY_IMAGES_EN_US_ICON=$en_us_icon_count"
echo "PLAY_IMAGES_EN_US_FEATURE_GRAPHIC=$en_us_feature_count"
echo "PLAY_IMAGES_EN_US_PHONE_SCREENSHOTS=$en_us_phone_count"
echo "PLAY_IMAGES_AR_ICON=$ar_icon_count"
echo "PLAY_IMAGES_AR_FEATURE_GRAPHIC=$ar_feature_count"
echo "PLAY_IMAGES_AR_PHONE_SCREENSHOTS=$ar_phone_count"
echo "PLAY_INTERNAL_COUNTRY_COUNT=$internal_country_count"
echo "PLAY_PRODUCTION_COUNTRY_COUNT=$production_country_count"
echo "PLAY_INTERNAL_GOOGLE_GROUP_COUNT=$internal_group_count"
echo "PLAY_BETA_GOOGLE_GROUP_COUNT=$beta_group_count"
echo "PLAY_EMAIL_LIST_TESTERS_API=UNSUPPORTED"
echo "PLAY_PRODUCTION_ACCESS_ELIGIBILITY_API=UNSUPPORTED"
echo "PLAY_EMPTY_EDIT_VALIDATION=$empty_edit_validation"

delete_edit
trap - EXIT INT TERM
unset GOOGLE_OAUTH_ACCESS_TOKEN access_token
find "$temporary_root" -type f -exec chmod 600 {} + 2>/dev/null || true
find "$temporary_root" -type f -exec rm -f -- {} + 2>/dev/null || true
find "$temporary_root" -depth -type d -exec rmdir {} + 2>/dev/null || true
echo "PLAY_ACCESS_CHECK=PASS"
