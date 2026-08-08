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

text_center() {
    local label="$1"

    adb shell uiautomator dump /sdcard/passvault-window.xml >/dev/null
    adb exec-out cat /sdcard/passvault-window.xml | ruby -rrexml/document -e '
      label = ARGV.fetch(0)
      document = REXML::Document.new(STDIN.read)
      node = nil
      REXML::XPath.each(document, "//*") do |candidate|
        if candidate.attributes["text"] == label || candidate.attributes["content-desc"] == label
          node = candidate
          break
        end
      end
      exit 1 unless node
      bounds = node.attributes.fetch("bounds")
      match = bounds.match(/\[(\d+),(\d+)\]\[(\d+),(\d+)\]/)
      exit 1 unless match
      left, top, right, bottom = match.captures.map(&:to_i)
      puts "#{(left + right) / 2} #{(top + bottom) / 2}"
    ' "$label"
}

wait_for_text() {
    local label="$1"

    for _ in 1 2 3 4 5; do
        if text_center "$label" >/dev/null; then
            return
        fi
        sleep 1
    done
    echo "Expected Android screen text was not visible: $label" >&2
    exit 1
}

tap_text_after_scrolling() {
    local label="$1"
    local point
    local x
    local y

    for _ in 1 2 3 4 5; do
        if point="$(text_center "$label")"; then
            read -r x y <<< "$point"
            adb shell input tap "$x" "$y"
            sleep 1
            return
        fi
        adb shell input swipe 540 1650 540 450 350
        sleep 1
    done
    echo "Unable to find and tap Android screen text: $label" >&2
    exit 1
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
    if [[ "$locale" == "ar" ]]; then
        get_started_label="ابدأ"
        continue_label="متابعة"
        security_label="نظرة عامة على الأمان"
        master_password_label="أنشئ كلمة مرور رئيسية قوية"
    else
        get_started_label="Get started"
        continue_label="Continue"
        security_label="Security overview"
        master_password_label="Create a strong master password"
    fi
    adb shell pm clear "$package_name" >/dev/null
    adb shell cmd locale set-app-locales "$package_name" --user 0 --locales "$locale" || true
    adb shell am start -W -n "$package_name/com.passvault.android.MainActivity" >/dev/null

    capture "$locale_root/01-welcome.png"
    adb shell input swipe 540 1650 540 500 450
    capture "$locale_root/02-security-features.png"
    tap_text_after_scrolling "$get_started_label"
    wait_for_text "$security_label"
    capture "$locale_root/03-security-model.png"
    tap_text_after_scrolling "$continue_label"
    wait_for_text "$master_password_label"
    capture "$locale_root/04-master-password.png"
    adb shell am force-stop "$package_name"

    if cmp -s "$locale_root/02-security-features.png" "$locale_root/03-security-model.png" ||
        cmp -s "$locale_root/03-security-model.png" "$locale_root/04-master-password.png"; then
        echo "Android navigation produced duplicate store screenshots for $locale." >&2
        exit 1
    fi
done

echo "Captured four isolated Android onboarding screenshots for en-US and ar."
