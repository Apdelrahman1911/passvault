#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"
wrapper_jar="$repository_root/gradle/wrapper/gradle-wrapper.jar"
checksum_file="$repository_root/gradle/wrapper/gradle-wrapper.jar.sha256"

for path in "$wrapper_jar" "$checksum_file"; do
    if [[ ! -f "$path" || -L "$path" ]]; then
        echo "Gradle wrapper verification input is missing or unsafe: $path" >&2
        exit 1
    fi
done

checksum_lines="$(wc -l < "$checksum_file" | tr -d '[:space:]')"
if [[ "$checksum_lines" != 1 ]] ||
   ! LC_ALL=C grep -Eq '^[0-9a-f]{64}$' "$checksum_file"; then
    echo "Gradle wrapper checksum file must contain one lowercase SHA-256 value." >&2
    exit 1
fi
expected_checksum="$(tr -d '\r\n' < "$checksum_file")"

if command -v shasum >/dev/null 2>&1; then
    actual_checksum="$(shasum -a 256 "$wrapper_jar" | awk '{ print $1 }')"
elif command -v sha256sum >/dev/null 2>&1; then
    actual_checksum="$(sha256sum "$wrapper_jar" | awk '{ print $1 }')"
else
    echo "shasum or sha256sum is required to verify the Gradle wrapper." >&2
    exit 1
fi

if [[ "$actual_checksum" != "$expected_checksum" ]]; then
    echo "Gradle wrapper JAR checksum mismatch." >&2
    echo "Expected: $expected_checksum" >&2
    echo "Actual:   $actual_checksum" >&2
    exit 1
fi

echo "Gradle wrapper JAR checksum verified: $actual_checksum"
