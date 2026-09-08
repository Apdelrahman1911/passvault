#!/usr/bin/env python3
"""Read pinned handoff Git blobs as inert data; never extract or import evidence.

The frozen POSIX reader refuses this VPS's directory/file device split. This
separately reviewed reader uses Git's content-addressed committed objects, not a
weakened pathname reader. It does NOT verify current checkout bytes or modes.
No project, historical helper, packed program, credential or network is run.
"""

import argparse
import hashlib
import json
import lzma
import os
from pathlib import Path
import re
import selectors
import signal
import subprocess
import sys
import tarfile
import time
import unicodedata


ROOT = Path(__file__).resolve().parents[2]
COMMIT = "9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed"
TREE = "05014e9f635131d5db06701e4013b4b5a746465a"
PREFIX = "docs/audit-handoff/"
MIB = 1024 * 1024
ENV = {
    "PATH": "/usr/bin:/bin",
    "LANG": "C.UTF-8",
    "GIT_CONFIG_NOSYSTEM": "1",
    "GIT_CONFIG_GLOBAL": "/dev/null",
    "GIT_CONFIG_SYSTEM": "/dev/null",
    "GIT_OPTIONAL_LOCKS": "0",
    "GIT_NO_REPLACE_OBJECTS": "1",
    "GIT_NO_LAZY_FETCH": "1",
    "GIT_TERMINAL_PROMPT": "0",
    "GIT_ALLOW_PROTOCOL": "",
}
CANCELLED = None


def cancelled(signum, _frame):
    global CANCELLED
    CANCELLED = signum


def require(condition, message):
    if not condition:
        raise ValueError(message)


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def relative_path(value):
    require(isinstance(value, str) and value and "\\" not in value, "invalid path")
    require(not value.startswith("/"), "absolute path")
    require(all(part not in ("", ".", "..") for part in value.split("/")), "path part")
    require(not any(unicodedata.category(c).startswith("C") for c in value), "control path")
    return value


def unique_object(pairs):
    result = {}
    for key, value in pairs:
        require(key not in result, "duplicate JSON key")
        result[key] = value
    return result


def parse_json(data):
    return json.loads(data, object_pairs_hook=unique_object)


def git(arguments, limit):
    # Only the fixed local Git binary and fixed plumbing operations below run.
    argv = ["/usr/bin/git", "--no-replace-objects", "-c", "core.hooksPath=/dev/null",
            "-c", "core.fsmonitor=false", "-C", str(ROOT), *arguments]
    require(CANCELLED is None, "interrupted before Git launch")
    child = None
    output, error = bytearray(), bytearray()
    try:
        # The signal handler only latches; it cannot interrupt Popen's PID registration.
        # An interrupt crossing launch is failed, never retried, and reaped below.
        child = subprocess.Popen(argv, env=ENV, stdin=subprocess.DEVNULL,
                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        deadline = time.monotonic() + 30
        with selectors.DefaultSelector() as selector:
            for pipe, value in ((child.stdout, (output, limit)), (child.stderr, (error, 65536))):
                os.set_blocking(pipe.fileno(), False)
                selector.register(pipe, selectors.EVENT_READ, value)
            while selector.get_map():
                require(CANCELLED is None, "interrupted during Git read")
                require(time.monotonic() < deadline, "local Git plumbing timed out")
                for key, _ in selector.select(timeout=0.1):
                    chunk = os.read(key.fd, 65536)
                    if not chunk:
                        selector.unregister(key.fileobj)
                        continue
                    target, cap = key.data
                    require(len(target) + len(chunk) <= cap, "Git live output bound")
                    target.extend(chunk)
            child.wait(timeout=max(0.01, deadline - time.monotonic()))
        require(CANCELLED is None and child.returncode == 0, "Git failed or interrupted")
        return bytes(output)
    finally:
        if child is not None:
            # Cleanup is installed for success, failure and latched INT/TERM before
            # any read. Only this unreaped direct child can be signalled, once.
            if child.poll() is None:
                child.kill()
            try:
                child.wait(timeout=5)
            except subprocess.TimeoutExpired:
                raise ValueError("owned Git child settlement unresolved, PID=" + str(child.pid)) from None
            finally:
                child.stdout.close()
                child.stderr.close()


def objects():
    require(git(["rev-parse", "--verify", COMMIT + "^{tree}"], 100).decode().strip() == TREE,
            "checkpoint tree differs")
    listing = git(["ls-tree", "-r", "-l", "-z", COMMIT, "--", PREFIX], 2 * MIB)
    result = {}
    for entry in listing.split(b"\0"):
        if not entry:
            continue
        metadata, raw_path = entry.split(b"\t", 1)
        mode, kind, oid, raw_size = metadata.split()
        path = relative_path(raw_path.decode("utf-8", "strict"))
        require(path.startswith(PREFIX) and path[len(PREFIX):] not in result, "tree path")
        require(mode in (b"100644", b"100755") and kind == b"blob", "nonregular Git object")
        require(re.fullmatch(b"[0-9a-f]{40}", oid) is not None, "Git object ID")
        size = int(raw_size)
        require(0 <= size <= 32 * MIB, "Git object size")
        result[path[len(PREFIX):]] = (oid.decode(), size)
    require(0 < len(result) < 1000, "handoff object count")
    return result


def blob(records, path, limit):
    oid, size = records[relative_path(path)]
    require(size <= limit, "requested blob bound")
    data = git(["cat-file", "blob", oid], limit)
    require(len(data) == size, "Git blob length")
    header = b"blob " + str(size).encode("ascii") + b"\0"
    require(hashlib.sha1(header + data).hexdigest() == oid, "Git object digest")
    return data


def package_and_index(git_objects):
    manifest = parse_json(blob(git_objects, "PACKAGE.json", 2 * MIB))
    require(manifest["format"] == 1, "package format")
    records = {}
    for row in manifest["files"]:
        path = relative_path(row["path"])
        require(path not in records and path in git_objects, "package path")
        require(type(row["bytes"]) is int and 0 <= row["bytes"] <= 32 * MIB, "package size")
        require(re.fullmatch(r"[0-9a-f]{64}", row["sha256"]) is not None, "package digest")
        records[path] = row
    require(set(records) | {"PACKAGE.json"} == set(git_objects), "package/tree membership")

    def checked(path, limit):
        row = records[path]
        data = blob(git_objects, path, limit)
        require(len(data) == row["bytes"] and sha256(data) == row["sha256"], "package mismatch")
        return data

    index = parse_json(checked("evidence-index.json", 16 * MIB))
    require(index["format"] == 1 and len(index["files"]) < 30000, "index format/size")
    paths, by_pack = {}, {}
    hashes = {}
    for row in index["files"]:
        path = relative_path(row["path"])
        digest = row["sha256"]
        pack = row["pack"]
        size = row["bytes"]
        require(path not in paths, "duplicate index path")
        require(re.fullmatch(r"[0-9a-f]{64}", digest) is not None, "index digest")
        require(type(size) is int and 0 <= size <= 16 * MIB, "index blob size")
        require(re.fullmatch(r"evidence-packs/evidence-[0-9]{3}\.tar\.xz", pack) is not None
                and pack in records, "unbound pack")
        require(digest not in hashes or hashes[digest] == (size, pack), "inconsistent alias")
        hashes[digest] = (size, pack)
        paths[path] = row
        by_pack.setdefault(pack, {})[digest] = size
    require(set(by_pack) == {p for p in records if p.startswith("evidence-packs/")}, "pack set")
    return records, paths, by_pack, checked


def read_pack(packed, expected, wanted=None):
    # Same explicit decoder/TAR bounds as the supplied transport reader. No extraction.
    decoder = lzma.LZMADecompressor(format=lzma.FORMAT_XZ, memlimit=32 * MIB)
    expanded = bytearray()
    next_input = packed
    while not decoder.eof:
        expanded.extend(decoder.decompress(next_input, max_length=min(MIB, 72 * MIB + 1 - len(expanded))))
        next_input = b""
        require(len(expanded) <= 72 * MIB, "expanded archive bound")
        require(decoder.eof or not decoder.needs_input, "truncated XZ")
    require(not decoder.unused_data and len(expanded) % 512 == 0, "XZ/TAR framing")
    seen, selected, position, ended = set(), None, 0, False
    while position + 512 <= len(expanded):
        header = bytes(expanded[position:position + 512])
        position += 512
        if header == bytes(512):
            require(position + 512 <= len(expanded) and not any(expanded[position:]), "TAR EOF")
            ended = True
            break
        require(header[257:263] == b"ustar\0", "unsupported TAR framing")
        member = tarfile.TarInfo.frombuf(header, "utf-8", "strict")
        require(member.type in (tarfile.REGTYPE, tarfile.AREGTYPE) and not member.linkname,
                "nonregular/extended TAR member")
        require(re.fullmatch(r"blobs/[0-9a-f]{64}", member.name) is not None, "TAR path")
        require(member.mode == 0o644 and member.uid == member.gid == member.mtime == 0, "TAR metadata")
        digest = member.name.split("/")[1]
        require(digest in expected and digest not in seen and member.size == expected[digest], "TAR member")
        end = position + member.size
        padded = (end + 511) // 512 * 512
        require(padded <= len(expanded) and not any(expanded[end:padded]), "TAR bounds/padding")
        data = bytes(expanded[position:end])
        require(sha256(data) == digest, "TAR blob hash")
        seen.add(digest)
        if digest == wanted:
            selected = data
        position = padded
    require(ended and seen == set(expected), "TAR membership/EOF")
    return selected


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=("list", "show", "verify"))
    parser.add_argument("path", nargs="?")
    args = parser.parse_args()
    require(args.path is None if args.action == "verify" else args.path is not None, "argument shape")
    records, paths, packs, checked = package_and_index(objects())
    if args.action == "list":
        for path in sorted(paths):
            if path.startswith(args.path):
                print(path)
    elif args.action == "show":
        row = paths[relative_path(args.path)]
        data = read_pack(checked(row["pack"], 32 * MIB), packs[row["pack"]], row["sha256"])
        require(data is not None, "missing selected blob")
        value = data.decode("utf-8", "strict")
        require(not any(unicodedata.category(c) in ("Cc", "Cf") and c not in "\n\t"
                        for c in value.replace("\r\n", "\n")),
                "binary or terminal-control text refused")
        require(CANCELLED is None, "interrupted before display")
        sys.stdout.write(value)
    else:
        for path in records:
            checked(path, 32 * MIB)
        for pack, expected in sorted(packs.items()):
            read_pack(checked(pack, 32 * MIB), expected)
        print(json.dumps({"status": "PASS_COMMITTED_TRANSPORT_ONLY", "commit": COMMIT, "tree": TREE,
                          "package_files": len(records), "indexed_paths": len(paths),
                          "distinct_blobs": sum(len(p) for p in packs.values()), "packs": len(packs),
                          "application_tests": 0, "checkout_verification": False}, sort_keys=True))


if __name__ == "__main__":
    # A hard kill/host loss cannot run finally. This is not crash-proof cleanup.
    signal.signal(signal.SIGINT, cancelled)
    signal.signal(signal.SIGTERM, cancelled)
    try:
        main()
        require(CANCELLED is None, "interrupted before terminal exit")
    except (ValueError, KeyError, OSError, UnicodeError, lzma.LZMAError, tarfile.TarError,
            subprocess.TimeoutExpired) as error:
        print("REFUSED: " + str(error), file=sys.stderr)
        sys.exit(2)
