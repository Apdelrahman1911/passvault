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
    testflight_line=0
    testflight_header_seen=false
    testflight_emails=$'\n'
    while IFS=, read -r first_name last_name email extra || [[ -n "${first_name:-}${last_name:-}${email:-}" ]]; do
        testflight_line=$((testflight_line + 1))
        first_name="${first_name%$'\r'}"
        last_name="${last_name%$'\r'}"
        email="${email%$'\r'}"

        if [[ -z "$first_name" && -z "$last_name" && -z "$email" && -z "${extra:-}" ]]; then
            continue
        fi

        if [[ "$testflight_header_seen" == "false" ]]; then
            if [[ "$first_name,$last_name,$email" != "first_name,last_name,email" ||
                -n "${extra:-}" ]]; then
                echo "The TestFlight tester CSV must begin with the exact first_name,last_name,email header." >&2
                exit 1
            fi
            testflight_header_seen=true
            continue
        fi

        if [[ -n "${extra:-}" || -z "$first_name" || -z "$last_name" ||
            ! "$email" =~ $email_pattern || "$email" == *.invalid ]]; then
            echo "The TestFlight tester CSV has an invalid or placeholder row at line $testflight_line." >&2
            exit 1
        fi
        normalized_email="$(printf '%s' "$email" | tr '[:upper:]' '[:lower:]')"
        if [[ "$testflight_emails" == *$'\n'"$normalized_email"$'\n'* ]]; then
            echo "The TestFlight tester CSV repeats an email at line $testflight_line." >&2
            exit 1
        fi
        testflight_emails+="$normalized_email"$'\n'
        testflight_count=$((testflight_count + 1))
    done < "$tester_file"

    if [[ "$testflight_header_seen" == "false" ]]; then
        echo "The TestFlight tester CSV is missing its header." >&2
        exit 1
    fi
    if (( testflight_count == 0 )); then
        echo "The TestFlight tester CSV has no real tester rows; external TestFlight is disabled." >&2
        exit 1
    fi

    echo "TestFlight tester file validated ($testflight_count tester(s))."
    ;;
play)
    play_count=0
    play_line=0
    play_emails=$'\n'
    while IFS= read -r email || [[ -n "$email" ]]; do
        play_line=$((play_line + 1))
        email="${email%$'\r'}"
        [[ -z "$email" || "$email" == \#* ]] && continue
        if [[ ! "$email" =~ $email_pattern || "$email" == *.invalid ]]; then
            echo "The Play tester list contains an invalid or placeholder email at line $play_line." >&2
            exit 1
        fi
        normalized_email="$(printf '%s' "$email" | tr '[:upper:]' '[:lower:]')"
        if [[ "$play_emails" == *$'\n'"$normalized_email"$'\n'* ]]; then
            echo "The Play tester list repeats an email at line $play_line." >&2
            exit 1
        fi
        play_emails+="$normalized_email"$'\n'
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
