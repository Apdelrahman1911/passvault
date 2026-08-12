#!/usr/bin/env bash

# Build PassVault packages supported by the current operating system.
# Usage: ./scripts/build-desktop.sh [--linux|--windows|--macos|--all]
#        [--portable] [--clean] [--sign]

set -euo pipefail

requested_platform=""
portable=false
clean=false
sign=false

while (( $# > 0 )); do
    case "$1" in
        --linux) requested_platform="linux" ;;
        --windows) requested_platform="windows" ;;
        --macos) requested_platform="macos" ;;
        --all) requested_platform="current" ;;
        --portable) portable=true ;;
        --clean) clean=true ;;
        --sign) sign=true ;;
        --help|-h)
            sed -n '2,4s/^# //p' "$0"
            exit 0
            ;;
        *)
            echo "Unknown option: $1" >&2
            exit 2
            ;;
    esac
    shift
done

case "$(uname -s)" in
    Linux*) current_platform="linux" ;;
    Darwin*) current_platform="macos" ;;
    CYGWIN*|MINGW*|MSYS*) current_platform="windows" ;;
    *)
        echo "Unsupported operating system: $(uname -s)" >&2
        exit 1
        ;;
esac

if [[ -n "$requested_platform" && "$requested_platform" != current &&
      "$requested_platform" != "$current_platform" ]]; then
    echo "Desktop packages must be built on their target OS; this host is $current_platform." >&2
    exit 2
fi

if [[ "$sign" == true && "$current_platform" != macos ]]; then
    echo "--sign currently configures macOS signing only." >&2
    exit 2
fi
if [[ "$sign" == true ]]; then
    : "${MACOS_IDENTITY:?MACOS_IDENTITY is required with --sign}"
    export MACOS_SIGN=true
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

gradle_arguments=()
[[ "$clean" == true ]] && gradle_arguments+=(:app-desktop:clean)
gradle_arguments+=(:app-desktop:createReleaseDistributable)
case "$current_platform" in
    linux) gradle_arguments+=(:app-desktop:packageReleaseDeb :app-desktop:packageReleaseRpm) ;;
    windows) gradle_arguments+=(:app-desktop:packageReleaseMsi :app-desktop:packageReleaseExe) ;;
    macos) gradle_arguments+=(:app-desktop:packageReleaseDmg) ;;
esac
[[ "$portable" == true ]] && gradle_arguments+=(:app-desktop:packagePortable)

./gradlew "${gradle_arguments[@]}"

echo "Desktop build completed. Artifacts:"
find app-desktop/build/compose/binaries app-desktop/build/distributions \
    -type f \( -name '*.deb' -o -name '*.rpm' -o -name '*.msi' -o -name '*.exe' \
    -o -name '*.dmg' -o -name '*.zip' \) -print 2>/dev/null | sort
