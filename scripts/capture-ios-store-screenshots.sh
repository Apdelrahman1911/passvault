#!/usr/bin/env bash

set -euo pipefail

app_path="${1:-}"
output_root="${2:-}"
device_id="${3:-${IOS_SCREENSHOT_DEVICE:-}}"
device_class="${4:-iphone}"
bundle_id="${IOS_SCREENSHOT_BUNDLE_ID:-com.passvault.ios}"

if [[ ! -d "$app_path" || -L "$app_path" || -z "$output_root" ||
    ! "$device_id" =~ ^[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}$ ||
    ( "$device_class" != iphone && "$device_class" != ipad ) ]]; then
    echo "Usage: $0 <simulator-app> <output-root> <device-udid> iphone|ipad" >&2
    exit 2
fi
if [[ ! "$bundle_id" =~ ^[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)+$ ||
    -L "$output_root" || ( -e "$output_root" && ! -d "$output_root" ) ]]; then
    echo "The iOS screenshot bundle ID or output path is invalid." >&2
    exit 1
fi
mkdir -p "$output_root"
if find "$output_root" -type l -print -quit | grep -q .; then
    echo "The iOS screenshot output tree contains a symlink." >&2
    exit 1
fi
if command -v magick >/dev/null; then
    imagemagick=(magick)
elif command -v convert >/dev/null; then
    imagemagick=(convert)
else
    echo "ImageMagick is required to create store-compatible PNG24 screenshots." >&2
    exit 1
fi

device_state="$(xcrun simctl list devices --json |
    ruby -rjson -e '
      target = ARGV.fetch(0)
      devices = JSON.parse(STDIN.read).fetch("devices").values.flatten
      puts devices.find { |device| device["udid"] == target }&.fetch("state", "")
    ' "$device_id")"
if [[ "$device_state" != Booted ]]; then
    echo "The requested iOS screenshot simulator is not booted." >&2
    exit 1
fi

for locale in en-US ar-SA; do
    locale_root="$output_root/$locale"
    destination="$locale_root/01-welcome-$device_class.png"
    mkdir -p "$locale_root"
    if [[ -e "$destination" || -L "$destination" ]]; then
        echo "Refusing to overwrite an existing iOS store screenshot: $destination" >&2
        exit 1
    fi
    xcrun simctl terminate "$device_id" "$bundle_id" 2>/dev/null || true
    xcrun simctl uninstall "$device_id" "$bundle_id" 2>/dev/null || true
    xcrun simctl install "$device_id" "$app_path"
    xcrun simctl launch "$device_id" "$bundle_id" \
        -AppleLanguages "($locale)" -AppleLocale "$locale" >/dev/null
    sleep 4
    xcrun simctl io "$device_id" screenshot "$destination" >/dev/null
    "${imagemagick[@]}" "$destination" -alpha off PNG24:"$destination"
done

if cmp -s \
    "$output_root/en-US/01-welcome-$device_class.png" \
    "$output_root/ar-SA/01-welcome-$device_class.png"; then
    echo "iOS locale switching produced identical $device_class screenshots." >&2
    exit 1
fi

echo "Captured the isolated iOS $device_class welcome screen for en-US and ar-SA."
