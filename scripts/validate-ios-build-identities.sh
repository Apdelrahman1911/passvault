#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
project="$repository_root/iosApp/iosApp.xcodeproj"
scheme="$repository_root/iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/PassVault.xcscheme"
shared_configuration="$repository_root/iosApp/Configuration/Config.xcconfig"
localized_info_plists=(
    "$repository_root/iosApp/iosApp/en.lproj/InfoPlist.strings"
    "$repository_root/iosApp/iosApp/ar.lproj/InfoPlist.strings"
)

for command in xcodebuild ruby; do
    command -v "$command" >/dev/null 2>&1 || {
        echo "$command is required to validate iOS build identities." >&2
        exit 1
    }
done
if [[ ! -d "$project" || -L "$project" || ! -f "$scheme" || -L "$scheme" ||
      ! -f "$shared_configuration" || -L "$shared_configuration" ]]; then
    echo "The PassVault Xcode project or shared scheme is missing or unsafe." >&2
    exit 1
fi
if grep -Eq '^[[:space:]]*PRODUCT_BUNDLE_IDENTIFIER[[:space:]]*=' "$shared_configuration"; then
    echo "The shared Xcode configuration must not override per-configuration bundle identities." >&2
    exit 1
fi
for localized_info_plist in "${localized_info_plists[@]}"; do
    if [[ ! -f "$localized_info_plist" || -L "$localized_info_plist" ]]; then
        echo "A required localized InfoPlist.strings file is missing or unsafe." >&2
        exit 1
    fi
    if grep -Eq '^[[:space:]]*"?CFBundleDisplayName"?[[:space:]]*=' "$localized_info_plist"; then
        echo "Localized InfoPlist.strings must not override the per-configuration display name." >&2
        exit 1
    fi
done

temporary_parent="${TMPDIR:-/tmp}"
temporary_parent="${temporary_parent%/}"
[[ -n "$temporary_parent" && -d "$temporary_parent" && ! -L "$temporary_parent" ]] || {
    echo "The temporary directory is missing or unsafe." >&2
    exit 1
}
temporary_root="$(mktemp -d "$temporary_parent/passvault-ios-identities.XXXXXX")"
cleanup() {
    if [[ -n "${temporary_root:-}" && -d "$temporary_root" &&
          "$temporary_root" == "$temporary_parent"/passvault-ios-identities.* ]]; then
        find "$temporary_root" -type f -exec rm -f -- {} + 2>/dev/null || true
        find "$temporary_root" -depth -type d -exec rmdir {} + 2>/dev/null || true
    fi
}
trap cleanup EXIT

setting_value() {
    local settings_file="$1"
    local setting_name="$2"

    awk -F ' = ' -v setting="$setting_name" '
        $1 ~ "^[[:space:]]*" setting "$" {
            sub(/^[[:space:]]+/, "", $2)
            print $2
            found = 1
            exit
        }
        END { if (!found) exit 1 }
    ' "$settings_file"
}

validate_configuration() {
    local configuration="$1"
    local expected_bundle_id="$2"
    local expected_display_name="$3"
    local settings_file="$temporary_root/$configuration.settings"

    if ! xcodebuild \
          -project "$project" \
          -scheme PassVault \
          -configuration "$configuration" \
          -sdk iphoneos \
          -derivedDataPath "$temporary_root/DerivedData-$configuration" \
          -showBuildSettings \
          CODE_SIGNING_ALLOWED=NO \
          > "$settings_file" 2> "$temporary_root/$configuration.xcodebuild.log"; then
        cat "$temporary_root/$configuration.xcodebuild.log" >&2
        exit 1
    fi

    local bundle_id display_name product_name effective_configuration
    bundle_id="$(setting_value "$settings_file" PRODUCT_BUNDLE_IDENTIFIER)"
    display_name="$(setting_value "$settings_file" INFOPLIST_KEY_CFBundleDisplayName)"
    product_name="$(setting_value "$settings_file" PRODUCT_NAME)"
    effective_configuration="$(setting_value "$settings_file" CONFIGURATION)"

    if [[ "$bundle_id" != "$expected_bundle_id" ||
          "$display_name" != "$expected_display_name" ||
          "$product_name" != PassVault ||
          "$effective_configuration" != "$configuration" ]]; then
        echo "Unexpected $configuration iOS identity settings." >&2
        echo "Bundle: $bundle_id; display name: $display_name; product: $product_name." >&2
        exit 1
    fi
}

validate_configuration Debug com.passvault.ios.debug "PassVault Dev"
validate_configuration Release com.passvault.ios PassVault

ruby -rrexml/document -e '
  document = REXML::Document.new(File.read(ARGV.fetch(0), encoding: "UTF-8"))
  expected = {
    "TestAction" => "Debug",
    "LaunchAction" => "Debug",
    "AnalyzeAction" => "Debug",
    "ProfileAction" => "Release",
    "ArchiveAction" => "Release",
  }
  expected.each do |action, configuration|
    node = REXML::XPath.first(document, "/Scheme/#{action}")
    abort("Missing #{action} in PassVault.xcscheme") unless node
    actual = node.attributes["buildConfiguration"]
    abort("#{action} must use #{configuration}, found #{actual}") unless actual == configuration
  end
' "$scheme"

echo "iOS Debug and Release identities and scheme actions are correctly separated."
