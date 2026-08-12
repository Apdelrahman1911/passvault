#!/usr/bin/env bash

# Run the local, non-publishing PassVault release verification matrix.

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

version="$(awk -F= '$1 == "VERSION_NAME" { print $2; exit }' version.properties)"
if [[ -z "$version" ]]; then
    echo "VERSION_NAME is missing from version.properties." >&2
    exit 1
fi

./scripts/validate-release-metadata.sh "$version"
./scripts/test-release-automation.sh
./gradlew test detekt verifyDependencies --continue
./gradlew \
    :app-android:assembleStandardRelease \
    :app-android:bundleStandardRelease \
    :app-android:lintStandardRelease \
    :app-android:assembleFdroidRelease \
    :app-android:lintFdroidRelease \
    :app-android:verifyReleasePackageContents \
    --no-configuration-cache \
    -Ppassvault.versionName="$version"
ruby scripts/validate-google-play-readiness.rb
./gradlew \
    :app-desktop:createReleaseDistributable \
    :app-desktop:packageReleaseDistributionForCurrentOS \
    -Ppassvault.versionName="$version"

case "$(uname -s)" in
    Darwin|Linux) ./scripts/smoke-test-desktop-release.sh 30 ;;
    CYGWIN*|MINGW*|MSYS*)
        command -v pwsh >/dev/null 2>&1 || {
            echo "PowerShell 7 is required for the Windows desktop smoke test." >&2
            exit 1
        }
        pwsh -File scripts/smoke-test-desktop-release.ps1 -TimeoutSeconds 30
        ;;
    *)
        echo "Unsupported operating system for desktop release verification." >&2
        exit 1
        ;;
esac

echo "Local release verification passed for PassVault $version."
