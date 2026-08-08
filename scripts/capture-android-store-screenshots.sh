#!/usr/bin/env bash

set -euo pipefail

apk_path="${1:-}"
output_root="${2:-}"
package_name="com.passvault.android.storescreenshot"

if [[ ! -f "$apk_path" || -z "$output_root" ]]; then
    echo "Usage: $0 <store-screenshot-apk> <output-root>" >&2
    exit 2
fi
command -v adb >/dev/null
if ! command -v convert >/dev/null; then
    echo "ImageMagick's convert command is required to create store-compatible PNG24 screenshots." >&2
    exit 1
fi

adb install -r "$apk_path" >/dev/null

assert_capture_ready() {
    local ui_dump

    if ! adb shell uiautomator dump /sdcard/passvault-window.xml >/dev/null; then
        echo "Unable to inspect the emulator UI before screenshot capture." >&2
        exit 1
    fi
    ui_dump="$(adb exec-out cat /sdcard/passvault-window.xml)"
    if [[ "$ui_dump" == *"isn't responding"* ||
        "$ui_dump" == *"is not responding"* ||
        "$ui_dump" == *"Close app"* ]]; then
        echo "Android displayed a system crash or ANR dialog; refusing to capture store screenshots." >&2
        exit 1
    fi
}

capture() {
    local destination="$1"
    sleep 2
    assert_capture_ready
    adb exec-out screencap -p > "$destination"
    convert "$destination" -alpha off PNG24:"$destination"
}

for locale in en-US ar; do
    locale_root="$output_root/$locale/images/phoneScreenshots"
    mkdir -p "$locale_root"
    adb shell pm clear "$package_name" >/dev/null
    adb shell cmd locale set-app-locales "$package_name" --user 0 --locales "$locale" || true
    adb shell am start -W -n "$package_name/com.passvault.android.MainActivity" >/dev/null

    capture "$locale_root/01-welcome.png"
    adb shell input swipe 540 1650 540 500 450
    capture "$locale_root/02-security-features.png"
    adb shell input tap 540 1720
    capture "$locale_root/03-security-model.png"
    adb shell input tap 540 1720
    capture "$locale_root/04-master-password.png"
    adb shell am force-stop "$package_name"
done

echo "Captured four isolated Android onboarding screenshots for en-US and ar."
