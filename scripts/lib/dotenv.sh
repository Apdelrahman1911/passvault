#!/usr/bin/env bash

# Parse dotenv files as data. Never source or evaluate them as shell code.

PASSVAULT_DOTENV_KIND=""
PASSVAULT_DOTENV_KEY=""
PASSVAULT_DOTENV_VALUE=""
PASSVAULT_DOTENV_ERROR=""

passvault_dotenv_key_is_unsafe() {
    case "$1" in
        PATH|IFS|BASH_ENV|ENV|SHELLOPTS|BASHOPTS|CDPATH|GLOBIGNORE|HOME|PWD|OLDPWD|TMPDIR|\
        LD_*|DYLD_*|BASH_*|EUID|UID|PPID|RANDOM|SECONDS|LINENO|GROUPS|FUNCNAME|DIRSTACK|\
        PIPESTATUS|OPTARG|OPTIND|SHLVL|MACHTYPE|OSTYPE|HOSTTYPE|HOSTNAME|PS0|PS1|PS2|PS4)
            return 0
            ;;
    esac
    return 1
}

passvault_dotenv_parse_line() {
    local line="$1"
    local key value first_character last_character value_length

    PASSVAULT_DOTENV_KIND=""
    PASSVAULT_DOTENV_KEY=""
    PASSVAULT_DOTENV_VALUE=""
    PASSVAULT_DOTENV_ERROR=""

    line="${line%$'\r'}"
    if [[ "$line" =~ ^[[:space:]]*$ || "$line" =~ ^[[:space:]]*# ]]; then
        PASSVAULT_DOTENV_KIND="ignored"
        return 0
    fi
    if [[ "$line" != *=* ]]; then
        PASSVAULT_DOTENV_ERROR="expected NAME=value"
        return 1
    fi

    key="${line%%=*}"
    value="${line#*=}"
    if [[ ! "$key" =~ ^[A-Z][A-Z0-9_]*$ ]]; then
        PASSVAULT_DOTENV_ERROR="invalid variable name"
        return 1
    fi
    if passvault_dotenv_key_is_unsafe "$key"; then
        PASSVAULT_DOTENV_ERROR="unsafe variable name: $key"
        return 1
    fi

    value_length=${#value}
    if (( value_length > 0 )); then
        first_character="${value:0:1}"
        last_character="${value:value_length-1:1}"
        if [[ "$first_character" == "'" || "$first_character" == '"' ]]; then
            if (( value_length < 2 )) || [[ "$last_character" != "$first_character" ]]; then
                PASSVAULT_DOTENV_ERROR="unterminated quoted value for $key"
                return 1
            fi
            value="${value:1:value_length-2}"
        fi
    fi

    PASSVAULT_DOTENV_KIND="entry"
    PASSVAULT_DOTENV_KEY="$key"
    PASSVAULT_DOTENV_VALUE="$value"
}

passvault_dotenv_load_file() {
    local input_file="$1"
    local line line_number=0 loaded_keys=$'\n'

    if [[ ! -f "$input_file" || -L "$input_file" ]]; then
        echo "The dotenv input is missing or unsafe." >&2
        return 1
    fi
    if ! ruby -e '
      contents = File.binread(ARGV.fetch(0)).force_encoding(Encoding::UTF_8)
      abort unless contents.valid_encoding? && !contents.include?("\0") &&
        contents.bytesize <= 1024 * 1024
    ' "$input_file" >/dev/null 2>&1; then
        echo "The dotenv input must be valid UTF-8, contain no NUL bytes, and not exceed 1 MiB." >&2
        return 1
    fi

    while IFS= read -r line || [[ -n "$line" ]]; do
        line_number=$((line_number + 1))
        if ! passvault_dotenv_parse_line "$line"; then
            echo "Invalid dotenv entry at line $line_number: $PASSVAULT_DOTENV_ERROR." >&2
            return 1
        fi
        if [[ "$PASSVAULT_DOTENV_KIND" == "entry" ]]; then
            if [[ "$loaded_keys" == *$'\n'"$PASSVAULT_DOTENV_KEY"$'\n'* ]]; then
                echo "Duplicate dotenv key at line $line_number: $PASSVAULT_DOTENV_KEY." >&2
                return 1
            fi
            loaded_keys+="$PASSVAULT_DOTENV_KEY"$'\n'
            printf -v "$PASSVAULT_DOTENV_KEY" '%s' "$PASSVAULT_DOTENV_VALUE"
        fi
    done < "$input_file"
}
