#!/usr/bin/env bash

set -euo pipefail

if [[ "${1:-}" != "--apply" ]]; then
    echo "Usage: $0 --apply" >&2
    echo "Validates private inputs, then configures GitHub and keyless Google access." >&2
    exit 2
fi

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

if ! command -v gh >/dev/null 2>&1; then
    echo "GitHub CLI is required." >&2
    exit 1
fi

# Authentication is deliberately the first external-state check.
if ! gh auth status; then
    echo "GitHub authentication is invalid. Run: gh auth login -h github.com" >&2
    exit 1
fi

if ! command -v gcloud >/dev/null 2>&1; then
    for google_sdk_root in /opt/homebrew/share/google-cloud-sdk /usr/local/share/google-cloud-sdk; do
        if [[ -x "$google_sdk_root/bin/gcloud" ]]; then
            PATH="$google_sdk_root/bin:$PATH"
            export PATH
            break
        fi
    done
fi

for command_name in gcloud jq ruby; do
    if ! command -v "$command_name" >/dev/null 2>&1; then
        echo "$command_name is required before configuration can start." >&2
        exit 1
    fi
done

if ! git check-ignore -q release/private/values.env || [[ -n "$(git ls-files release/private)" ]]; then
    echo "release/private/ is not safely ignored and untracked; configuration stopped." >&2
    exit 1
fi

./scripts/validate-private-release-config.sh
ruby ./scripts/validate-app-store-connect.rb

./scripts/configure-github-mobile-release.sh --apply
./scripts/configure-google-oidc.sh --apply

echo "Mobile release configuration and name/scope verification completed."
