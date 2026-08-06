#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "Usage: $0 <testflight-csv> <play-testers-text>" >&2
    exit 2
fi

testflight_file="$1"
play_file="$2"
email_pattern='^[^[:space:]@,]+@[^[:space:]@,]+\.[^[:space:]@,]+$'

for file in "$testflight_file" "$play_file"; do
    if [[ ! -s "$file" || -L "$file" ]]; then
        echo "A tester input is missing, empty, or unsafe." >&2
        exit 1
    fi
done

testflight_count=0
while IFS=, read -r first_name last_name email extra || [[ -n "${first_name:-}${last_name:-}${email:-}" ]]; do
    first_name="${first_name%$'\r'}"
    last_name="${last_name%$'\r'}"
    email="${email%$'\r'}"
    if (( testflight_count == 0 )) &&
        [[ "$first_name,$last_name,$email" == "first_name,last_name,email" ]]; then
        testflight_count=1
        continue
    fi
    if [[ -n "${extra:-}" || -z "$first_name" || -z "$last_name" ||
        ! "$email" =~ $email_pattern || "$email" == *.invalid ]]; then
        echo "The TestFlight tester CSV has an invalid row." >&2
        exit 1
    fi
    testflight_count=$((testflight_count + 1))
done < "$testflight_file"

if (( testflight_count < 2 )); then
    echo "The TestFlight tester CSV has no tester rows." >&2
    exit 1
fi

play_count=0
while IFS= read -r email || [[ -n "$email" ]]; do
    email="${email%$'\r'}"
    [[ -z "$email" || "$email" == \#* ]] && continue
    if [[ ! "$email" =~ $email_pattern || "$email" == *.invalid ]]; then
        echo "The Play tester list contains an invalid email." >&2
        exit 1
    fi
    play_count=$((play_count + 1))
done < "$play_file"

if (( play_count == 0 )); then
    echo "The Play tester list has no tester emails." >&2
    exit 1
fi

echo "Tester files validated ($((testflight_count - 1)) TestFlight, $play_count Play)."
