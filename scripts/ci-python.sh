#!/usr/bin/env bash
# Use the hosted runner's Python3 without assuming a Unix `python` alias.
set -euo pipefail
if command -v python3 >/dev/null 2>&1; then
    exec python3 "$@"
fi
exec python "$@"
