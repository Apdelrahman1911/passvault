#!/usr/bin/env bash

set -euo pipefail

app_path="${1:-}"
output_root="${2:-}"
bundle_id="${IOS_SCREENSHOT_BUNDLE_ID:-com.passvault.ios}"

if [[ ! -d "$app_path" || -z "$output_root" ]]; then
    echo "Usage: $0 <simulator-app> <output-root>" >&2
    exit 2
fi

device_id="$(xcrun simctl list devices booted --json |
    ruby -rjson -e 'puts JSON.parse(STDIN.read).fetch("devices").values.flatten.first&.fetch("udid", "")')"
if [[ -z "$device_id" ]]; then
    echo "A booted 6.9-inch iOS simulator is required." >&2
    exit 1
fi

xcrun simctl install "$device_id" "$app_path"
for locale in en-US ar-SA; do
    locale_root="$output_root/$locale"
    mkdir -p "$locale_root"
    xcrun simctl terminate "$device_id" "$bundle_id" 2>/dev/null || true
    xcrun simctl uninstall "$device_id" "$bundle_id" 2>/dev/null || true
    xcrun simctl install "$device_id" "$app_path"
    xcrun simctl launch "$device_id" "$bundle_id" \
        -AppleLanguages "($locale)" -AppleLocale "$locale" >/dev/null
    sleep 4
    xcrun simctl io "$device_id" screenshot "$locale_root/01-welcome.png" >/dev/null
done

echo "Captured the isolated iOS welcome screen for en-US and ar-SA."
