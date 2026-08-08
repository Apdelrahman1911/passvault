#!/usr/bin/env bash

set -euo pipefail

apk_path="${1:-}"
output_root="${2:-}"
package_name="com.passvault.android.storescreenshot"
capture_password="CaptureOnlyVaultPassphrase2026"

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
      bounds = node.attributes.fetch("bounds").to_s
      match = bounds.match(/\[(\d+),(\d+)\]\[(\d+),(\d+)\]/)
      exit 1 unless match
      left, top, right, bottom = match.captures.map(&:to_i)
      puts "#{(left + right) / 2} #{(top + bottom) / 2}"
    ' "$label"
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

tap_text_and_wait() {
    local source_label="$1"
    local destination_label="$2"
    local point
    local x
    local y

    for _ in 1 2 3 4 5; do
        if text_center "$destination_label" >/dev/null; then
            return
        fi
        if point="$(text_center "$source_label")"; then
            read -r x y <<< "$point"
            adb shell input tap "$x" "$y"
        else
            adb shell input swipe 540 1650 540 450 350
        fi
        sleep 1
    done
    echo "Unable to navigate from '$source_label' to '$destination_label'." >&2
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
        master_password_field_label="كلمة المرور الرئيسية"
        confirm_password_label="تأكيد كلمة المرور الرئيسية"
        confirm_password_field_label="تأكيد كلمة المرور"
        create_vault_label="إنشاء خزنة مشفّرة"
    else
        get_started_label="Get started"
        continue_label="Continue"
        security_label="Security overview"
        master_password_label="Create a strong master password"
        master_password_field_label="Master password"
        confirm_password_label="Confirm master password"
        confirm_password_field_label="Confirm password"
        create_vault_label="Create encrypted vault"
    fi
    adb shell pm clear "$package_name" >/dev/null
    adb shell cmd locale set-app-locales "$package_name" --user 0 --locales "$locale" || true
    adb shell am start -W -n "$package_name/com.passvault.android.MainActivity" >/dev/null

    sleep 2
    adb shell input swipe 540 1650 540 500 450
    capture "$locale_root/01-welcome.png"
    tap_text_and_wait "$get_started_label" "$master_password_label"
    adb shell input keyevent KEYCODE_BACK
    sleep 1
    capture "$locale_root/02-master-password.png"
    tap_text_after_scrolling "$master_password_field_label"
    adb shell input text "$capture_password"
    adb shell input keyevent KEYCODE_BACK
    sleep 2
    tap_text_and_wait "$continue_label" "$confirm_password_label"
    adb shell input keyevent KEYCODE_BACK
    sleep 1
    capture "$locale_root/03-confirm-password.png"
    tap_text_after_scrolling "$confirm_password_field_label"
    adb shell input text "$capture_password"
    adb shell input keyevent KEYCODE_BACK
    sleep 2
    tap_text_and_wait "$create_vault_label" "$security_label"
    capture "$locale_root/04-security-model.png"
    adb shell am force-stop "$package_name"

    if cmp -s "$locale_root/01-welcome.png" "$locale_root/02-master-password.png" ||
        cmp -s "$locale_root/02-master-password.png" "$locale_root/03-confirm-password.png" ||
        cmp -s "$locale_root/03-confirm-password.png" "$locale_root/04-security-model.png"; then
        echo "Android navigation produced duplicate store screenshots for $locale." >&2
        exit 1
    fi
done

echo "Captured four isolated Android onboarding screenshots for en-US and ar."
