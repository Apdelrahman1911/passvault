#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "Usage: $0 <testflight|play> <tester-file>" >&2
    exit 2
fi

tester_type="$1"
tester_file="$2"
email_pattern='^[^[:space:]@,]+@[^[:space:]@,]+\.[^[:space:]@,]+$'

if [[ ! -s "$tester_file" || -L "$tester_file" ]]; then
    echo "The requested tester input is missing, empty, or unsafe; distribution is disabled." >&2
    exit 1
fi

case "$tester_type" in
testflight)
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
            echo "The TestFlight tester CSV has an invalid or placeholder row." >&2
            exit 1
        fi
        testflight_count=$((testflight_count + 1))
    done < "$tester_file"

    if (( testflight_count < 2 )); then
        echo "The TestFlight tester CSV has no real tester rows; external TestFlight is disabled." >&2
        exit 1
    fi

    echo "TestFlight tester file validated ($((testflight_count - 1)) tester(s))."
    ;;
play)
    play_count=0
    while IFS= read -r email || [[ -n "$email" ]]; do
        email="${email%$'\r'}"
        [[ -z "$email" || "$email" == \#* ]] && continue
        if [[ ! "$email" =~ $email_pattern || "$email" == *.invalid ]]; then
            echo "The Play tester list contains an invalid or placeholder email." >&2
            exit 1
        fi
        play_count=$((play_count + 1))
    done < "$tester_file"

    if (( play_count == 0 )); then
        echo "The Play tester list has no real tester emails; Play closed testing is disabled." >&2
        exit 1
    fi

    echo "Play tester file validated ($play_count tester(s))."
    ;;
*)
    echo "Unknown tester type: use testflight or play." >&2
    exit 2
    ;;
esac
