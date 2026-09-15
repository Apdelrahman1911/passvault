#!/usr/bin/env bash
# Use the hosted runner's Python3 without assuming a Unix `python` alias.
set -euo pipefail
# Native Windows Python must not resolve the unrelated System32 WSL bash.exe.
bash_path="${BASH:?Expected Bash executable from caller}"
if [[ "${RUNNER_OS:-}" == Windows ]]; then
    case "$bash_path" in
        *.[eE][xX][eE]) ;;
        *) bash_path="${bash_path}.exe" ;;
    esac
    bash_path="$(cygpath -am "$bash_path")"
fi
export PASSVAULT_CI_BASH="$bash_path"
if command -v python3 >/dev/null 2>&1; then
    exec python3 "$@"
fi
exec python "$@"
