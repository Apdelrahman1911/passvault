#!/usr/bin/env bash

set -euo pipefail

status="${EXPORT_COMPLIANCE_STATUS:-}"
france_available="${IOS_FRANCE_AVAILABLE:-}"
plist_path="${1:-}"

case "$france_available" in
    true|false) ;;
    *)
        echo "IOS_FRANCE_AVAILABLE must be exactly true or false." >&2
        exit 1
        ;;
esac

case "$status" in
    EXEMPT_APPROVED)
        expected_non_exempt=false
        if [[ "$france_available" != "false" ]]; then
            echo "EXEMPT_APPROVED requires France to remain excluded from App Store availability." >&2
            echo "Reopen App Encryption Documentation before enabling France." >&2
            exit 1
        fi
        ;;
    NON_EXEMPT_APPROVED)
        expected_non_exempt=true
        ;;
    PENDING|"")
        echo "An approved export-compliance status is required." >&2
        exit 1
        ;;
    *)
        echo "Unsupported EXPORT_COMPLIANCE_STATUS." >&2
        exit 1
        ;;
esac

if [[ -z "$plist_path" ]]; then
    exit 0
fi

if [[ ! -f "$plist_path" || -L "$plist_path" ]]; then
    echo "The built application Info.plist is missing or unsafe." >&2
    exit 1
fi

read_plist_value() {
    local key="$1"
    if [[ -x /usr/libexec/PlistBuddy ]]; then
        /usr/libexec/PlistBuddy -c "Print :$key" "$plist_path" 2>/dev/null
    elif command -v plutil >/dev/null 2>&1; then
        plutil -extract "$key" raw -o - "$plist_path" 2>/dev/null
    else
        ruby -rrexml/document -e '
          document = REXML::Document.new(File.binread(ARGV.fetch(0)))
          entries = document.elements.to_a("/plist/dict/*")
          index = entries.index { |entry| entry.name == "key" && entry.text == ARGV.fetch(1) }
          exit 1 unless index && entries[index + 1]
          value = entries[index + 1]
          puts(value.name == "true" ? "true" : value.name == "false" ? "false" : value.text)
        ' "$plist_path" "$key" 2>/dev/null
    fi
}

actual_non_exempt="$(read_plist_value ITSAppUsesNonExemptEncryption || true)"
actual_non_exempt="$(printf '%s' "$actual_non_exempt" | tr '[:upper:]' '[:lower:]')"
if [[ "$actual_non_exempt" != "$expected_non_exempt" ]]; then
    echo "Built Info.plist has an unexpected ITSAppUsesNonExemptEncryption value." >&2
    exit 1
fi

if [[ "$status" == "EXEMPT_APPROVED" ]] &&
    read_plist_value ITSEncryptionExportComplianceCode >/dev/null 2>&1; then
    echo "An Apple export-compliance code must not be invented for EXEMPT_APPROVED." >&2
    exit 1
fi

echo "Built iOS export-compliance configuration is valid."
