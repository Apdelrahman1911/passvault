#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
exec ruby "$repository_root/scripts/validate-workflow-action-pins.rb" "$@"
