#!/usr/bin/env bash

set -euo pipefail

timeout_seconds="${1:-30}"
if [[ ! "$timeout_seconds" =~ ^[0-9]+$ ]] || (( timeout_seconds < 5 || timeout_seconds > 120 )); then
    echo "Usage: $0 [timeout-seconds: 5-120]" >&2
    exit 2
fi

case "$(uname -s)" in
    Darwin)
        launcher="app-desktop/build/compose/binaries/main-release/app/PassVault.app/Contents/MacOS/PassVault"
        command=("$launcher")
        ;;
    Linux)
        launcher="app-desktop/build/compose/binaries/main-release/app/PassVault/bin/PassVault"
        if ! command -v xvfb-run >/dev/null 2>&1; then
            echo "xvfb-run is required to smoke-test the Linux desktop package." >&2
            exit 1
        fi
        command=(xvfb-run -a "$launcher")
        ;;
    *)
        echo "This smoke test supports Linux and macOS only." >&2
        exit 2
        ;;
esac

if [[ ! -x "$launcher" ]]; then
    echo "The packaged PassVault launcher is missing or not executable." >&2
    exit 1
fi

temporary_parent="${TMPDIR:-/tmp}"
temporary_parent="${temporary_parent%/}"
smoke_root="$(mktemp -d "$temporary_parent/passvault-desktop-smoke.XXXXXX")"
chmod 700 "$smoke_root"
cleanup() {
    local original_status=$?
    trap - EXIT INT TERM
    if [[ -n "${process_id:-}" ]] && kill -0 "$process_id" 2>/dev/null; then
        kill "$process_id" 2>/dev/null || true
        wait "$process_id" 2>/dev/null || true
    fi
    case "$smoke_root" in
        "$temporary_parent"/passvault-desktop-smoke.*)
            rm -rf -- "$smoke_root"
            ;;
        *)
            echo "Refusing to remove an unexpected desktop-smoke path." >&2
            original_status=1
            ;;
    esac
    exit "$original_status"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

smoke_java_options="-Duser.home=$smoke_root"
if [[ -n "${JAVA_TOOL_OPTIONS:-}" ]]; then
    smoke_java_options="$JAVA_TOOL_OPTIONS $smoke_java_options"
fi
JAVA_TOOL_OPTIONS="$smoke_java_options" "${command[@]}" >"$smoke_root/launcher.log" 2>&1 &
process_id=$!

for (( elapsed = 0; elapsed < timeout_seconds; elapsed += 1 )); do
    if ! kill -0 "$process_id" 2>/dev/null; then
        set +e
        wait "$process_id"
        exit_code=$?
        set -e
        process_id=""
        echo "The packaged PassVault launcher exited early with code $exit_code." >&2
        exit 1
    fi
    sleep 1
done

echo "The packaged PassVault launcher remained running for $timeout_seconds seconds."
