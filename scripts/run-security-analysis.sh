#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
rules="$repository_root/.github/security-analysis/opengrep.yml"
fixtures="$repository_root/scripts/testdata/security-analysis"
opengrep_version="1.29.0"
temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/passvault-security-analysis.XXXXXX")"
downloaded_tool=""

cleanup() {
    case "$temporary_root" in
        "${TMPDIR:-/tmp}"/passvault-security-analysis.*)
            rm -rf -- "$temporary_root"
            ;;
        *)
            echo "Refusing to remove unexpected security-analysis path: $temporary_root" >&2
            ;;
    esac
}
trap cleanup EXIT

sha256_file() {
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum "$1" | awk '{ print $1 }'
    else
        shasum -a 256 "$1" | awk '{ print $1 }'
    fi
}

install_opengrep() {
    local operating_system architecture asset expected_sha256 output_path actual_sha256
    operating_system="$(uname -s)"
    architecture="$(uname -m)"

    case "$operating_system/$architecture" in
        Linux/x86_64)
            asset="opengrep_manylinux_x86"
            expected_sha256="3365ef49d04893e01338d85d9bbd49b2bd5261ad4c9c0df0a6a0f8d44232ae13"
            ;;
        Linux/aarch64|Linux/arm64)
            asset="opengrep_manylinux_aarch64"
            expected_sha256="db3cda6e6e53251a3874e62b7c8493c281508480b3f3b4db554be41583b21174"
            ;;
        Darwin/x86_64)
            asset="opengrep_osx_x86"
            expected_sha256="7173bd701491b58e1d1f62c24470ca0be124ecd63885c4d6293cbf71fd706508"
            ;;
        Darwin/arm64)
            asset="opengrep_osx_arm64"
            expected_sha256="dacc12a24e95b22c8b1ab55be1777b6eb877a922c5571a95b9a8de30f3963438"
            ;;
        *)
            echo "Unsupported OpenGrep host: $operating_system/$architecture" >&2
            return 1
            ;;
    esac

    output_path="$temporary_root/opengrep"
    curl --fail --location --silent --show-error \
        "https://github.com/opengrep/opengrep/releases/download/v${opengrep_version}/${asset}" \
        --output "$output_path"
    actual_sha256="$(sha256_file "$output_path")"
    if [[ "$actual_sha256" != "$expected_sha256" ]]; then
        echo "OpenGrep checksum mismatch for $asset." >&2
        return 1
    fi
    chmod 700 "$output_path"
    downloaded_tool="$output_path"
}

if [[ -n "${OPENGREP_BIN:-}" ]]; then
    opengrep="$OPENGREP_BIN"
    if [[ ! -f "$opengrep" || -L "$opengrep" || ! -x "$opengrep" ]]; then
        echo "OPENGREP_BIN must name a regular executable file." >&2
        exit 1
    fi
else
    install_opengrep
    opengrep="$downloaded_tool"
fi

actual_version="$($opengrep show version)"
if [[ "$actual_version" != "$opengrep_version" ]]; then
    echo "Expected OpenGrep $opengrep_version, found $actual_version." >&2
    exit 1
fi

cd "$repository_root"
"$opengrep" validate "$rules"
"$opengrep" test --strict --config "$(dirname "$rules")" "$fixtures/rules"

canary_result="$temporary_root/canary.json"
set +e
"$opengrep" scan \
    --config "$rules" \
    --disable-nosem \
    --disable-version-check \
    --error \
    --jobs 1 \
    --json \
    --max-target-bytes 0 \
    --strict \
    "$fixtures/Canary.kt" > "$canary_result"
canary_status=$?
set -e
if (( canary_status != 1 )); then
    echo "OpenGrep failure canary returned $canary_status instead of the expected finding status 1." >&2
    exit 1
fi
ruby scripts/verify-static-analysis-coverage.rb --verify-canary-result "$canary_result"

targets=()
while IFS= read -r target; do
    [[ -z "$target" ]] || targets+=("$target")
done < <(ruby scripts/verify-static-analysis-coverage.rb --list-security-targets)
if (( ${#targets[@]} == 0 )); then
    echo "Security-analysis target inventory is empty." >&2
    exit 1
fi

analysis_result="$temporary_root/analysis.json"
set +e
"$opengrep" scan \
    --config "$rules" \
    --disable-nosem \
    --disable-version-check \
    --error \
    --jobs 1 \
    --json \
    --max-target-bytes 0 \
    "${targets[@]}" > "$analysis_result"
analysis_status=$?
set -e
if (( analysis_status > 1 )); then
    echo "OpenGrep failed with status $analysis_status." >&2
    exit "$analysis_status"
fi
ruby scripts/verify-static-analysis-coverage.rb --verify-result "$analysis_result"
if (( analysis_status != 0 )); then
    echo "OpenGrep reported security findings." >&2
    exit "$analysis_status"
fi
