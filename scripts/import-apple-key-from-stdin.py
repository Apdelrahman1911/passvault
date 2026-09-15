#!/usr/bin/env python3
"""Give security(1) a complete in-memory pipe, not a live producer stream.

Its file importer sizes input using fstat before reading. On Darwin, a pipe's
size is its currently buffered byte count, not the eventual stream length.
Never write the unencrypted key to disk or put it in an argument/environment.
"""
import os
import signal
import subprocess
import sys

MAX_KEY_BYTES = 64 * 1024


def complete_pipe(key):
    if not key or len(key) > MAX_KEY_BYTES:
        raise ValueError("Apple private-key input is empty or exceeds the size limit.")
    reader, writer = os.pipe()
    try:
        # Do not block before the consumer exists if an unexpected key exceeds
        # this host's pipe capacity. Partial writes must never reach security.
        os.set_blocking(writer, False)
        if os.write(writer, key) != len(key):
            raise ValueError("Apple private-key pipe capacity is insufficient.")
        os.close(writer)
        writer = None
        if os.fstat(reader).st_size != len(key):
            raise ValueError("Apple private-key pipe snapshot is incomplete.")
        return reader
    except BaseException:
        os.close(reader)
        raise
    finally:
        if writer is not None:
            os.close(writer)


def main():
    if sys.platform != "darwin" or len(sys.argv) != 2:
        print("Usage on macOS: import-apple-key-from-stdin.py <existing-keychain>", file=sys.stderr)
        return 2
    keychain = sys.argv[1]
    if os.path.islink(keychain) or not os.path.isfile(keychain):
        print("The destination keychain must be an existing regular file.", file=sys.stderr)
        return 2

    def timeout(_signal, _frame):
        raise TimeoutError("Apple private-key input did not finish.")

    reader = None
    try:
        signal.signal(signal.SIGALRM, timeout)
        signal.alarm(30)
        key = sys.stdin.buffer.read(MAX_KEY_BYTES + 1)
        signal.alarm(0)
        reader = complete_pipe(key)
        del key
        # The sole consumer is launched only after EOF, complete buffering and
        # the size check. Preserve non-extractability and the trusted tool list.
        return subprocess.run([
            "/usr/bin/security", "import", "/dev/stdin", "-k", keychain,
            "-t", "priv", "-f", "openssl", "-x",
            "-T", "/usr/bin/codesign", "-T", "/usr/bin/security",
        ], stdin=reader, timeout=30, check=False).returncode
    except (ValueError, TimeoutError) as error:
        print(str(error), file=sys.stderr)  # Fixed messages only, never key data.
        return 1
    except (OSError, subprocess.TimeoutExpired):
        print("Unable to complete the in-memory Apple private-key import.", file=sys.stderr)
        return 1
    finally:
        signal.alarm(0)
        if reader is not None:
            os.close(reader)


if __name__ == "__main__":
    sys.exit(main())
