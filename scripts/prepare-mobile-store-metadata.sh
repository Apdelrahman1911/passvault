#!/usr/bin/env bash

set -euo pipefail
umask 077

if [[ $# -ne 3 ]]; then
    echo "Usage: $0 <private-metadata-directory> <output-directory> <version-code>" >&2
    exit 2
fi

source_directory="$1"
output_root="$2"
version_code="$3"
repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
public_assets="$repository_root/release/store-assets"

if [[ ! -d "$public_assets" || -L "$public_assets" ]] ||
    find "$public_assets" -type l -print -quit | grep -q .; then
    echo "The public store-assets tree is missing or contains an unsafe symlink." >&2
    exit 1
fi

if [[ ! -d "$source_directory" || -L "$source_directory" ]]; then
    echo "The private metadata directory is missing or unsafe." >&2
    exit 1
fi
source_root="$(cd "$source_directory" && pwd -P)"

if [[ -L "$output_root" || ( -e "$output_root" && ! -d "$output_root" ) ]]; then
    echo "The metadata output path must be a real directory." >&2
    exit 1
fi
mkdir -p "$output_root"
if find "$output_root" -mindepth 1 -print -quit | grep -q .; then
    echo "The metadata output directory must be empty." >&2
    exit 1
fi

for name in COPYRIGHT_HOLDER SUPPORT_URL PROJECT_URL PRIVACY_POLICY_URL; do
    if [[ -z "${!name:-}" ]]; then
        echo "$name is required to prepare complete store metadata." >&2
        exit 1
    fi
done

if [[ ! "$version_code" =~ ^[1-9][0-9]*$ ]] ||
    (( ${#version_code} > 10 )) || (( 10#$version_code > 2100000000 )); then
    echo "The version code must be a canonical integer from 1 through 2100000000." >&2
    exit 1
fi

required_files=(
    release-notes-en.md release-notes-ar.md
    privacy-en.md privacy-ar.md
    store-metadata-en.env store-metadata-ar.env
    store-description-en.md store-description-ar.md
)
for name in "${required_files[@]}"; do
    if [[ ! -s "$source_root/$name" || -L "$source_root/$name" ]]; then
        echo "Required store metadata is missing or unsafe: $name" >&2
        exit 1
    fi
done

metadata_keys=(
    STORE_NAME
    APPLE_SUBTITLE
    PLAY_SHORT_DESCRIPTION
    APPLE_KEYWORDS
    APPLE_PROMOTIONAL_TEXT
    TESTFLIGHT_BETA_DESCRIPTION
    TESTFLIGHT_WHAT_TO_TEST
)

validate_metadata_file() {
    local file="$1"
    local expected_description_file="$2"
    ruby -I "$repository_root/scripts" -r lib/dotenv -e '
      values = PassVault::Dotenv.load(ARGV.fetch(0))
      expected_description_file = ARGV.fetch(1)
      expected = ARGV.drop(2)
      missing = expected - values.keys
      unknown = values.keys - expected - ["FULL_DESCRIPTION_FILE"]
      abort("Missing store metadata keys: #{missing.join(", ")}") unless missing.empty?
      abort("Unknown store metadata keys: #{unknown.join(", ")}") unless unknown.empty?
      if values.key?("FULL_DESCRIPTION_FILE")
        legacy_path = values.fetch("FULL_DESCRIPTION_FILE")
        components = legacy_path.split("/", -1)
        safe_legacy_path = legacy_path.bytesize <= 256 &&
          components.last == expected_description_file &&
          components.all? do |component|
            !["", ".", ".."].include?(component) &&
              component.match?(/\A[A-Za-z0-9][A-Za-z0-9._-]*\z/)
          end
        abort("Legacy FULL_DESCRIPTION_FILE does not identify the approved description file") unless
          safe_legacy_path
      end
    ' "$file" "$expected_description_file" "${metadata_keys[@]}"
}

read_metadata() {
    local file="$1"
    local key="$2"
    ruby -I "$repository_root/scripts" -r lib/dotenv -e '
      values = PassVault::Dotenv.load(ARGV.fetch(0))
      STDOUT.write(values.fetch(ARGV.fetch(1)))
    ' "$file" "$key"
}

plain_markdown() {
    awk '
        BEGIN { heading_skipped = 0 }
        !heading_skipped && /^[[:space:]]*#[[:space:]]+/ { heading_skipped = 1; next }
        heading_skipped && !content && /^[[:space:]]*$/ { next }
        { content = 1; print }
    ' "$1"
}

write_field() {
    local value="$1"
    local maximum="$2"
    local target="$3"
    if [[ -z "$value" || ${#value} -gt $maximum ||
        "$value" == *$'\n'* || "$value" == *$'\r'* ]]; then
        echo "Store field for $(basename "$target") is empty or exceeds $maximum characters." >&2
        exit 1
    fi
    printf '%s\n' "$value" > "$target"
}

write_markdown_field() {
    local source="$1"
    local maximum="$2"
    local target="$3"
    local value
    value="$(plain_markdown "$source")"
    if [[ -z "$value" || ${#value} -gt $maximum ]]; then
        echo "Store content for $(basename "$target") is empty or exceeds $maximum characters." >&2
        exit 1
    fi
    printf '%s\n' "$value" > "$target"
}

mkdir -p \
    "$output_root/android/en-US/changelogs" "$output_root/android/ar/changelogs" \
    "$output_root/ios/en-US" "$output_root/ios/ar-SA" \
    "$output_root/testflight/en-US" "$output_root/testflight/ar-SA" \
    "$output_root/policy/en-US" "$output_root/policy/ar-SA"

write_field "2026 $COPYRIGHT_HOLDER" 100 "$output_root/ios/copyright.txt"

for locale_spec in "en:en-US:en-US" "ar:ar:ar-SA"; do
    IFS=: read -r source_locale play_locale apple_locale <<< "$locale_spec"
    metadata_file="$source_root/store-metadata-$source_locale.env"
    description_file="$source_root/store-description-$source_locale.md"
    notes_file="$source_root/release-notes-$source_locale.md"

    validate_metadata_file "$metadata_file" "$(basename "$description_file")"
    store_name="$(read_metadata "$metadata_file" STORE_NAME)"
    apple_subtitle="$(read_metadata "$metadata_file" APPLE_SUBTITLE)"
    play_short_description="$(read_metadata "$metadata_file" PLAY_SHORT_DESCRIPTION)"
    apple_keywords="$(read_metadata "$metadata_file" APPLE_KEYWORDS)"
    apple_promotional_text="$(read_metadata "$metadata_file" APPLE_PROMOTIONAL_TEXT)"
    beta_description="$(read_metadata "$metadata_file" TESTFLIGHT_BETA_DESCRIPTION)"
    what_to_test="$(read_metadata "$metadata_file" TESTFLIGHT_WHAT_TO_TEST)"

    write_field "$store_name" 30 "$output_root/android/$play_locale/title.txt"
    write_field "$play_short_description" 80 "$output_root/android/$play_locale/short_description.txt"
    write_markdown_field "$description_file" 4000 \
        "$output_root/android/$play_locale/full_description.txt"
    write_markdown_field "$notes_file" 500 \
        "$output_root/android/$play_locale/changelogs/$version_code.txt"

    write_field "$store_name" 30 "$output_root/ios/$apple_locale/name.txt"
    write_field "$apple_subtitle" 30 "$output_root/ios/$apple_locale/subtitle.txt"
    write_field "$apple_keywords" 100 "$output_root/ios/$apple_locale/keywords.txt"
    write_field "$apple_promotional_text" 170 "$output_root/ios/$apple_locale/promotional_text.txt"
    write_field "$PROJECT_URL" 255 "$output_root/ios/$apple_locale/marketing_url.txt"
    write_field "$PRIVACY_POLICY_URL" 255 "$output_root/ios/$apple_locale/privacy_url.txt"
    write_field "$SUPPORT_URL" 255 "$output_root/ios/$apple_locale/support_url.txt"
    write_markdown_field "$description_file" 4000 "$output_root/ios/$apple_locale/description.txt"
    write_markdown_field "$notes_file" 4000 "$output_root/ios/$apple_locale/release_notes.txt"

    write_field "$beta_description" 4000 "$output_root/testflight/$apple_locale/description.txt"
    write_field "$what_to_test" 4000 "$output_root/testflight/$apple_locale/what_to_test.txt"
    plain_markdown "$source_root/privacy-$source_locale.md" > "$output_root/policy/$apple_locale/privacy.txt"
done

while IFS= read -r generated_file; do
    if [[ ! -s "$generated_file" ]]; then
        echo "Generated store field is empty: $generated_file" >&2
        exit 1
    fi
done < <(find "$output_root" -type f -print)

for locale in en-US ar; do
    asset_source="$public_assets/android/$locale/images"
    asset_destination="$output_root/android/$locale/images"
    if [[ -e "$asset_source/featureGraphic.png" &&
        ( ! -f "$asset_source/featureGraphic.png" || -L "$asset_source/featureGraphic.png" ) ]]; then
        echo "Android feature graphic is unsafe for locale $locale." >&2
        exit 1
    fi
    if [[ -f "$asset_source/featureGraphic.png" ]]; then
        if [[ ! -f "$asset_source/icon.png" || -L "$asset_source/icon.png" ]]; then
            echo "Android icon is missing or unsafe for locale $locale." >&2
            exit 1
        fi
        mkdir -p "$asset_destination/phoneScreenshots"
        cp "$asset_source/icon.png" "$asset_destination/icon.png"
        cp "$asset_source/featureGraphic.png" "$asset_destination/featureGraphic.png"
        find "$asset_source/phoneScreenshots" -maxdepth 1 -type f -name '*.png' \
            -exec cp {} "$asset_destination/phoneScreenshots/" \;
    fi
done

for locale in en-US ar-SA; do
    asset_source="$public_assets/ios/$locale"
    asset_destination="$output_root/ios-screenshots/$locale"
    if find "$asset_source" -maxdepth 1 -type f -name '*.png' -print -quit | grep -q .; then
        mkdir -p "$asset_destination"
        find "$asset_source" -maxdepth 1 -type f -name '*.png' -exec cp {} "$asset_destination/" \;
    fi
done

echo "Mobile store metadata prepared for build $version_code."
