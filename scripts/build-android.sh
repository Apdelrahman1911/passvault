#!/usr/bin/env bash

# Build explicit PassVault Android build types.
# Usage: ./scripts/build-android.sh [--debug] [--release|--google]
#        [--all] [--clean] [--test]

set -euo pipefail

build_debug=false
build_release=false
clean=false
run_tests=false
signing_environment_present=false

for signing_variable in KEYSTORE_PATH KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD; do
    if [[ -n "${!signing_variable:-}" ]]; then
        signing_environment_present=true
        break
    fi
done

while (( $# > 0 )); do
    case "$1" in
        --debug) build_debug=true ;;
        --release|--google) build_release=true ;;
        --all)
            build_debug=true
            build_release=true
            ;;
        --clean) clean=true ;;
        --test) run_tests=true ;;
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

if [[ "$build_debug" == false && "$build_release" == false ]]; then
    build_debug=true
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

gradle_arguments=()
[[ "$clean" == true ]] && gradle_arguments+=(clean)
if [[ "$build_debug" == true ]]; then
    gradle_arguments+=(
        :app-android:assembleDebug
        :app-android:verifyDebugComposeResources
    )
fi
if [[ "$build_release" == true ]]; then
    gradle_arguments+=(
        :app-android:assembleRelease
        :app-android:bundleRelease
        :app-android:lintRelease
        :app-android:verifyReleasePackageContents
    )
fi
if [[ "$build_release" == true || "$signing_environment_present" == true ]]; then
    # Signing credentials must never be serialized into Gradle's configuration cache.
    gradle_arguments+=(--no-configuration-cache)
fi
if [[ "$run_tests" == true ]]; then
    gradle_arguments+=(test detekt verifyDependencies)
fi

./gradlew "${gradle_arguments[@]}"

echo "Android build completed. Artifacts:"
find app-android/build/outputs -type f \( -name '*.apk' -o -name '*.aab' \) -print 2>/dev/null | sort
