#!/usr/bin/env bash

# Run PassVault host tests or the explicit Android standard-debug device suite.
# Usage: ./scripts/run-tests.sh [--unit|--desktop|--android|--all]
#        [--ci] [--clean] [--no-daemon] [--parallel]

set -euo pipefail

suite="host"
ci=false
clean=false
no_daemon=false
parallel=false

while (( $# > 0 )); do
    case "$1" in
        --unit) suite="host" ;;
        --desktop) suite="desktop" ;;
        --android) suite="android" ;;
        --all) suite="all" ;;
        --ci) ci=true; no_daemon=true ;;
        --clean) clean=true ;;
        --no-daemon) no_daemon=true ;;
        --parallel) parallel=true ;;
        --coverage)
            echo "Coverage is not configured in this repository." >&2
            exit 2
            ;;
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

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

gradle_options=()
[[ "$no_daemon" == true ]] && gradle_options+=(--no-daemon)
[[ "$parallel" == true ]] && gradle_options+=(--parallel)

[[ "$clean" == true ]] && ./gradlew clean "${gradle_options[@]}"

run_android_device_tests() {
    if ! command -v adb >/dev/null 2>&1 ||
       ! adb devices | awk 'NR > 1 && $2 == "device" { found = 1 } END { exit !found }'; then
        echo "An online Android device or emulator is required for --android/--all." >&2
        return 1
    fi
    ./gradlew :app-android:connectedStandardDebugAndroidTest "${gradle_options[@]}"
}

case "$suite" in
    host) ./gradlew test "${gradle_options[@]}" ;;
    desktop) ./gradlew desktopTest "${gradle_options[@]}" ;;
    android) run_android_device_tests ;;
    all)
        ./gradlew test "${gradle_options[@]}"
        run_android_device_tests
        ;;
esac

if [[ "$ci" == true ]]; then
    ./gradlew detekt verifyDependencies --continue "${gradle_options[@]}"
fi

echo "Requested PassVault test suite passed."
