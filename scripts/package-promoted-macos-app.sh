#!/usr/bin/env bash

set -euo pipefail

if [[ "$(uname -s)" != Darwin ]]; then
    echo "A promoted macOS app image must be packaged on macOS." >&2
    exit 1
fi
if [[ $# -ne 3 ]]; then
    echo "Usage: $0 <app-image-zip> <output-root> <version>" >&2
    exit 2
fi

archive_path="$1"
output_root="$2"
version="$3"
repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

: "${MACOS_JPACKAGE_JAVA_HOME:?MACOS_JPACKAGE_JAVA_HOME is required}"
: "${MACOS_IDENTITY:?MACOS_IDENTITY is required}"
: "${MACOS_TEAM_ID:?MACOS_TEAM_ID is required}"
: "${MACOS_KEYCHAIN_PATH:?MACOS_KEYCHAIN_PATH is required}"
: "${MACOS_PROVISIONING_PROFILE_PATH:?MACOS_PROVISIONING_PROFILE_PATH is required}"
: "${PUBLISHER_NAME:?PUBLISHER_NAME is required}"
: "${COPYRIGHT_HOLDER:?COPYRIGHT_HOLDER is required}"

if [[ ! "$version" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
    echo "Invalid macOS package version: $version" >&2
    exit 2
fi
if [[ ! -f "$archive_path" || -L "$archive_path" ]]; then
    echo "The promoted macOS app-image archive is missing or unsafe." >&2
    exit 1
fi
if [[ ! -f "$MACOS_PROVISIONING_PROFILE_PATH" || -L "$MACOS_PROVISIONING_PROFILE_PATH" ]]; then
    echo "The validated macOS provisioning profile is missing or unsafe." >&2
    exit 1
fi
jpackage="$MACOS_JPACKAGE_JAVA_HOME/bin/jpackage"
if [[ ! -x "$jpackage" ]]; then
    echo "JDK 21 jpackage is required for macOS signing and packaging." >&2
    exit 1
fi
for command_name in codesign ditto find plutil python3 readlink; do
    command -v "$command_name" >/dev/null 2>&1 || {
        echo "$command_name is required for promoted macOS packaging." >&2
        exit 1
    }
done

if [[ -L "$output_root" || ( -e "$output_root" && ! -d "$output_root" ) ]]; then
    echo "The macOS package output root is unsafe." >&2
    exit 1
fi
mkdir -p "$output_root"
if find "$output_root" -mindepth 1 -print -quit | grep -q .; then
    echo "The macOS package output root must be empty." >&2
    exit 1
fi
app_root="$output_root/app"
dmg_root="$output_root/dmg"
mkdir "$app_root" "$dmg_root"

python3 - "$archive_path" <<'PYTHON'
import pathlib
import posixpath
import stat
import sys
import zipfile

archive = pathlib.Path(sys.argv[1])
with zipfile.ZipFile(archive) as zipped:
    entries = zipped.infolist()
    if not entries or len(entries) > 50_000:
        raise SystemExit("The promoted macOS app-image archive has an invalid entry count.")
    seen = set()
    symlinks = set()
    expanded = 0
    for entry in entries:
        name = entry.filename
        if not name or "\\" in name or "\x00" in name or name.startswith("/"):
            raise SystemExit("The promoted macOS app-image archive contains an unsafe path.")
        parts = pathlib.PurePosixPath(name.rstrip("/")).parts
        if not parts or parts[0] not in {"PassVault.app", "__MACOSX"}:
            raise SystemExit("The promoted macOS app-image archive has an unexpected root.")
        if any(part in {"", ".", ".."} for part in parts):
            raise SystemExit("The promoted macOS app-image archive contains a traversal path.")
        normalized = "/".join(parts)
        comparison_name = normalized.casefold()
        if comparison_name in seen:
            raise SystemExit("The promoted macOS app-image archive contains a duplicate path.")
        seen.add(comparison_name)
        expanded += entry.file_size
        if entry.file_size < 0 or entry.file_size > 2 * 1024**3 or expanded > 4 * 1024**3:
            raise SystemExit("The promoted macOS app-image archive is too large when expanded.")
        mode = (entry.external_attr >> 16) & 0o170000
        if mode not in {0, stat.S_IFREG, stat.S_IFDIR, stat.S_IFLNK}:
            raise SystemExit("The promoted macOS app-image archive contains a special entry.")
        if mode == stat.S_IFLNK:
            if parts[0] != "PassVault.app" or entry.file_size > 4096:
                raise SystemExit("The promoted macOS app-image archive contains an unsafe symbolic link.")
            try:
                target = zipped.read(entry).decode("utf-8")
            except (UnicodeDecodeError, RuntimeError):
                raise SystemExit("The promoted macOS app-image archive contains an invalid symbolic link.")
            if not target or target.startswith("/") or "\\" in target or "\x00" in target:
                raise SystemExit("The promoted macOS app-image archive contains an unsafe symbolic link.")
            resolved = posixpath.normpath(posixpath.join(posixpath.dirname(normalized), target))
            if resolved != "PassVault.app" and not resolved.startswith("PassVault.app/"):
                raise SystemExit("The promoted macOS app-image archive contains an escaping symbolic link.")
            symlinks.add(normalized)
    for normalized in (entry.filename.rstrip("/") for entry in entries):
        if any(normalized.startswith(link + "/") for link in symlinks):
            raise SystemExit("The promoted macOS app-image archive contains content beneath a symbolic link.")
PYTHON

ditto -x -k "$archive_path" "$app_root"
app_path="$app_root/PassVault.app"
top_level_count="$(find "$app_root" -mindepth 1 -maxdepth 1 -print | wc -l | tr -d ' ')"
if [[ "$top_level_count" != 1 || ! -d "$app_path" || -L "$app_path" ]]; then
    echo "The restored macOS app image has an unexpected shape." >&2
    exit 1
fi
special_entry="$(find "$app_path" ! -type f ! -type d ! -type l -print -quit)"
if [[ -n "$special_entry" ]]; then
    echo "The restored macOS app contains a special filesystem entry." >&2
    exit 1
fi
while IFS= read -r -d '' symlink_path; do
    symlink_target="$(readlink "$symlink_path")"
    if [[ -z "$symlink_target" || "$symlink_target" == /* ]] ||
       ! APP_ROOT="$app_path" SYMLINK_PATH="$symlink_path" python3 - <<'PYTHON'
import os
import pathlib

root = pathlib.Path(os.environ["APP_ROOT"]).resolve(strict=True)
target = pathlib.Path(os.environ["SYMLINK_PATH"]).resolve(strict=True)
try:
    target.relative_to(root)
except ValueError:
    raise SystemExit(1)
PYTHON
    then
        echo "The restored macOS app contains an escaping or broken symbolic link." >&2
        exit 1
    fi
done < <(find "$app_path" -type l -print0)

bundle_id="$(plutil -extract CFBundleIdentifier raw -o - "$app_path/Contents/Info.plist")"
bundle_version="$(plutil -extract CFBundleShortVersionString raw -o - "$app_path/Contents/Info.plist")"
if [[ "$bundle_id" != com.passvault.desktop || "$bundle_version" != "$version" ]]; then
    echo "The promoted macOS app identity or version is invalid." >&2
    exit 1
fi
profile_destination="$app_path/Contents/embedded.provisionprofile"
if [[ -e "$profile_destination" || -L "$profile_destination" ]]; then
    echo "The unsigned candidate app unexpectedly contains a provisioning profile." >&2
    exit 1
fi
install -m 0600 "$MACOS_PROVISIONING_PROFILE_PATH" "$profile_destination"

app_entitlements="$repository_root/app-desktop/resources/macos/PassVault.entitlements"
runtime_entitlements="$repository_root/app-desktop/resources/macos/PassVault.runtime.entitlements"
for entitlements in "$app_entitlements" "$runtime_entitlements"; do
    if [[ ! -f "$entitlements" || -L "$entitlements" ]]; then
        echo "A required macOS entitlement file is missing or unsafe: $entitlements" >&2
        exit 1
    fi
done

# jpackage's installer mode copies a predefined image but does not sign it.
# First use its dedicated app-image signing mode so its internal metadata is
# updated, then restore the narrower runtime entitlement policy used by the
# Compose packaging task before sealing the outer bundle again.
"$jpackage" \
    --type app-image \
    --app-image "$app_path" \
    --mac-sign \
    --mac-signing-key-user-name "$MACOS_IDENTITY" \
    --mac-signing-keychain "$MACOS_KEYCHAIN_PATH" \
    --mac-package-signing-prefix com.passvault. \
    --mac-entitlements "$app_entitlements" \
    --verbose

while IFS= read -r -d '' runtime_code; do
    if [[ -x "$runtime_code" || "$runtime_code" == *.dylib ]]; then
        codesign -vvvv --timestamp --options runtime --force \
            --prefix com.passvault. \
            --sign "$MACOS_IDENTITY" \
            --keychain "$MACOS_KEYCHAIN_PATH" \
            --entitlements "$runtime_entitlements" \
            "$runtime_code"
    fi
done < <(find "$app_path/Contents/runtime" -type f -print0)
codesign -vvvv --timestamp --options runtime --force \
    --prefix com.passvault. \
    --sign "$MACOS_IDENTITY" \
    --keychain "$MACOS_KEYCHAIN_PATH" \
    --entitlements "$runtime_entitlements" \
    "$app_path/Contents/runtime"
codesign -vvvv --timestamp --options runtime --force \
    --prefix com.passvault. \
    --sign "$MACOS_IDENTITY" \
    --keychain "$MACOS_KEYCHAIN_PATH" \
    --entitlements "$app_entitlements" \
    "$app_path"

"$repository_root/scripts/verify-macos-release-artifact.sh" "$app_path"

"$jpackage" \
    --type dmg \
    --app-image "$app_path" \
    --dest "$dmg_root" \
    --name PassVault \
    --description "A secure password manager with end-to-end encryption" \
    --copyright "© 2026 $COPYRIGHT_HOLDER. All rights reserved." \
    --app-version "$version" \
    --vendor "$PUBLISHER_NAME" \
    --license-file "$repository_root/LICENSE.txt" \
    --mac-package-name PassVault \
    --mac-package-identifier com.passvault.desktop \
    --mac-app-category public.app-category.productivity \
    --icon "$repository_root/app-desktop/resources/macos/icon.icns" \
    --verbose

dmg_count="$(find "$dmg_root" -maxdepth 1 -type f -name '*.dmg' | wc -l | tr -d ' ')"
if [[ "$dmg_count" != 1 ]]; then
    echo "jpackage did not create exactly one macOS DMG." >&2
    exit 1
fi

echo "Packaged the receipt-verified macOS app image without rebuilding it."
