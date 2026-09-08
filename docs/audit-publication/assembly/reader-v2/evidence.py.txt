#!/usr/bin/env python3
"""Read-only PassVault handoff transport checks. Never run packed code or extract it."""

import argparse
import hashlib
import json
import lzma
import os
from pathlib import Path, PurePosixPath
import re
import stat
import sys
import tarfile
import unicodedata


HERE = Path(__file__).resolve().parent
ROOT = HERE.parent.parent
MIB = 1024 * 1024
SOURCE_RAW_SHA256 = "2f029f9cefd3262313e960b1bcc3cabd875d02ebe31a44e6e78f946bb3e98f1f"
SOURCE_CANONICAL_SHA256 = "7fd2dab3eac6269baede8a0ca550f0cbdfee20391a12dfbd9ac84cdac5401d91"


def require(condition, message):
    if not condition:
        raise ValueError(message)


def digest(data):
    return hashlib.sha256(data).hexdigest()


def relative_path(value):
    require(isinstance(value, str) and value and "\\" not in value, "invalid path")
    require(not value.startswith("/") and "\x00" not in value, "invalid absolute/NUL path")
    require(all(p not in ("", ".", "..") for p in value.split("/")), "invalid path part")
    require(str(PurePosixPath(value)) == value, "noncanonical path")
    require(not any(unicodedata.category(c).startswith("C") for c in value), "control/format path")
    return value


def read_file(root, relative, limit):
    # This reader intentionally refuses platforms without POSIX descriptor-relative access.
    require(os.name == "posix" and hasattr(os, "O_NOFOLLOW"), "POSIX no-follow reader required")
    relative_path(relative)
    def identity(info):
        return (info.st_dev, info.st_ino, info.st_uid, info.st_mode, info.st_nlink,
                info.st_size, info.st_mtime_ns, info.st_ctime_ns)
    def directory_identity(info):
        return (info.st_dev, info.st_ino, info.st_uid, info.st_mode, info.st_nlink)
    root_before = root.lstat()
    require(stat.S_ISDIR(root_before.st_mode), "root is not a real directory")
    descriptors = []
    edges = []
    leaf = None
    try:
        current = os.open(root, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
        descriptors.append(current)
        require(directory_identity(os.fstat(current)) == directory_identity(root_before), "root changed")
        for component in relative.split("/")[:-1]:
            before = os.stat(component, dir_fd=current, follow_symlinks=False)
            require(stat.S_ISDIR(before.st_mode) and before.st_dev == root_before.st_dev,
                    "non-directory or cross-device parent")
            child = os.open(component, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW, dir_fd=current)
            descriptors.append(child)
            require(directory_identity(before) == directory_identity(os.fstat(child)), "parent changed")
            edges.append((current, component, child, directory_identity(before)))
            current = child
        name = relative.split("/")[-1]
        before = os.stat(name, dir_fd=current, follow_symlinks=False)
        require(stat.S_ISREG(before.st_mode) and before.st_nlink == 1 and
                before.st_dev == root_before.st_dev and before.st_size <= limit, "file type/size/links")
        leaf = os.open(name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK, dir_fd=current)
        require(identity(before) == identity(os.fstat(leaf)), "file changed before open")
        parts = []
        size = 0
        while size <= limit:
            part = os.read(leaf, min(65536, limit + 1 - size))
            if not part:
                break
            parts.append(part)
            size += len(part)
        require(size == before.st_size and size <= limit, "file length changed")
        require(identity(before) == identity(os.fstat(leaf)) ==
                identity(os.stat(name, dir_fd=current, follow_symlinks=False)), "file changed during read")
        for parent, component, child, expected in reversed(edges):
            require(expected == directory_identity(os.fstat(child)) ==
                    directory_identity(os.stat(component, dir_fd=parent, follow_symlinks=False)),
                    "parent changed during read")
        require(directory_identity(root_before) == directory_identity(root.lstat()) ==
                directory_identity(os.fstat(descriptors[0])), "root changed during read")
        return b"".join(parts), before
    finally:
        if leaf is not None:
            os.close(leaf)
        for descriptor in reversed(descriptors):
            os.close(descriptor)


def package():
    raw, _ = read_file(HERE, "PACKAGE.json", 2 * MIB)
    result = json.loads(raw)
    require(result["format"] == 1, "unsupported package format")
    records = result["files"]
    require(isinstance(records, list) and len(records) < 1000, "invalid package file list")
    paths = [relative_path(row["path"]) for row in records]
    require(len(paths) == len(set(paths)), "duplicate package path")
    for row in records:
        require(type(row["bytes"]) is int and 0 <= row["bytes"] <= 32 * MIB, "package member size")
        require(re.fullmatch(r"[0-9a-f]{64}", row["sha256"]), "package member hash")
    return result, {row["path"]: row for row in records}


def checked_file(records, name, limit):
    row = records[name]
    data, _ = read_file(HERE, name, limit)
    require(len(data) == row["bytes"] and digest(data) == row["sha256"], "hash mismatch: " + name)
    return data


def index(records):
    value = json.loads(checked_file(records, "evidence-index.json", 16 * MIB))
    require(value["format"] == 1, "unsupported index format")
    rows = value["files"]
    require(isinstance(rows, list) and len(rows) < 30000, "invalid index length")
    result = {}
    blobs = {}
    for row in rows:
        name = relative_path(row["path"])
        require(name not in result, "duplicate indexed path")
        require(re.fullmatch(r"[0-9a-f]{64}", row["sha256"]), "invalid blob hash")
        require(type(row["bytes"]) is int and 0 <= row["bytes"] <= 16 * MIB, "blob size")
        require(re.fullmatch(r"evidence-packs/evidence-[0-9]{3}\.tar\.xz", row["pack"]), "pack path")
        require(row["pack"] in records, "unbound pack")
        identity = (row["bytes"], row["pack"])
        require(row["sha256"] not in blobs or blobs[row["sha256"]] == identity,
                "inconsistent same-hash alias")
        blobs[row["sha256"]] = identity
        result[name] = row
    require({name for name in records if name.startswith("evidence-packs/")} ==
            {row["pack"] for row in rows}, "package/index pack-set mismatch")
    return result, blobs


def read_pack(records, name, expected, wanted=None):
    # Compressed input is hash-checked before decompression; no archive extraction.
    packed = checked_file(records, name, 32 * MIB)
    decoder = lzma.LZMADecompressor(format=lzma.FORMAT_XZ, memlimit=32 * MIB)
    expanded = bytearray()
    next_input = packed
    while not decoder.eof:
        expanded.extend(decoder.decompress(next_input, max_length=min(MIB, 72 * MIB + 1 - len(expanded))))
        next_input = b""
        require(len(expanded) <= 72 * MIB, "expanded archive bound")
        require(decoder.eof or not decoder.needs_input, "truncated xz stream")
    require(not decoder.unused_data, "trailing/concatenated xz data")
    require(len(expanded) % 512 == 0, "unaligned TAR")
    seen = set()
    selected = None
    position = 0
    ended = False
    while position + 512 <= len(expanded):
        header = bytes(expanded[position:position + 512])
        position += 512
        if header == bytes(512):
            require(position + 512 <= len(expanded) and not any(expanded[position:]), "TAR EOF/trailing data")
            ended = True
            break
        # Plain USTAR only: extension metadata is rejected before interpreting its body.
        require(header[257:263] == b"ustar\x00", "unsupported TAR framing")
        member = tarfile.TarInfo.frombuf(header, "utf-8", "strict")
        require(member.type in (tarfile.REGTYPE, tarfile.AREGTYPE) and not member.linkname,
                "nonregular/extended archive member")
        require(re.fullmatch(r"blobs/[0-9a-f]{64}", member.name), "unexpected archive path")
        require(member.mode == 0o644 and member.uid == member.gid == member.mtime == 0,
                "nonnormalized archive metadata")
        sha = member.name.split("/")[1]
        require(sha in expected and sha not in seen, "unknown/duplicate archive member")
        require(member.size == expected[sha][0] and 0 <= member.size <= 16 * MIB, "member size")
        end = position + member.size
        padded = (end + 511) // 512 * 512
        require(padded <= len(expanded) and not any(expanded[end:padded]), "TAR member/padding bounds")
        data = bytes(expanded[position:end])
        require(digest(data) == sha, "blob hash mismatch")
        seen.add(sha)
        if sha == wanted:
            selected = data
        position = padded
    require(ended, "missing two-block TAR EOF")
    require(seen == set(expected), "archive membership mismatch")
    return selected


def verify_source():
    raw, _ = read_file(HERE, "current/source-manifest-v12.json", MIB)
    require(digest(raw) == SOURCE_RAW_SHA256, "source manifest raw hash")
    rows = json.loads(raw)
    canonical = json.dumps(rows, sort_keys=True, separators=(",", ":")).encode()
    require(digest(canonical) == SOURCE_CANONICAL_SHA256 and len(rows) == 811, "source identity")
    _, records = package()
    transport = json.loads(checked_file(records, "current/source-transport.json", MIB))
    differences = {row["path"]: row for row in transport["checkout_differences"]}
    require(set(differences) == {
        "scripts/test-windows-checksum-boundary.ps1",
        "scripts/update-desktop-biometric-checksum.ps1",
    }, "unexpected declared line-ending changes")
    seen = set()
    exact = 0
    normalized = 0
    for row in rows:
        path = relative_path(row["path"])
        require(path not in seen, "duplicate source path")
        seen.add(path)
        data, info = read_file(ROOT, path, 16 * MIB)
        if len(data) == row["bytes"] and digest(data) == row["sha256"]:
            exact += 1
        else:
            require(path in differences, "source mismatch: " + path)
            expected = differences[path]
            original = checked_file(records, expected["raw_copy"], MIB)
            require(len(original) == row["bytes"] and digest(original) == row["sha256"],
                    "raw source original mismatch")
            checkout = original.replace(b"\r\n", b"\n").replace(b"\n", b"\r\n")
            require(data == checkout and digest(data) == expected["checkout_sha256"],
                    "unexpected change beyond declared EOL transport: " + path)
            normalized += 1
        if os.name != "nt":
            require(bool(info.st_mode & 0o111) == bool(row["mode"] & 0o111), "source execute mode: " + path)
    print("PASS transport: %d exact G12 raw members; %d declared checkout-EOL members." % (exact, normalized))
    print("Executable bits checked on POSIX. Not an extra-file inventory, semantic review,")
    print("application test, fresh coverage or build admission; raw originals remain distinct.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=("list", "show", "verify", "verify-source"))
    parser.add_argument("path", nargs="?")
    args = parser.parse_args()
    if args.action == "verify-source":
        require(args.path is None, "verify-source takes no path")
        verify_source()
        return
    _, records = package()
    files, blobs = index(records)
    if args.action == "list":
        for name in sorted(files):
            if args.path is None or name.startswith(args.path):
                print(name)
        return
    if args.action == "show":
        require(args.path in files, "path not included; consult omissions.json")
        row = files[args.path]
        members = {sha: value for sha, value in blobs.items() if value[1] == row["pack"]}
        data = read_pack(records, row["pack"], members, row["sha256"])
        require(data is not None, "selected blob absent")
        text = data.decode("utf-8", errors="strict")
        require("\x00" not in text, "binary payload refused")
        # Neutralize non-text terminal controls without altering stored evidence.
        sys.stdout.write("".join(c if c in "\n\t" or not unicodedata.category(c).startswith("C")
                                else "\\u%04x" % ord(c) for c in text))
        return
    require(args.path is None, "verify takes no path")
    for name in records:
        checked_file(records, name, 32 * MIB)
    packs = sorted({value[1] for value in blobs.values()})
    for name in packs:
        read_pack(records, name, {sha: value for sha, value in blobs.items() if value[1] == name})
    print("PASS: %d indexed paths, %d exact blobs, %d packs." % (len(files), len(blobs), len(packs)))
    print("Transport integrity only; omitted evidence and all runtime/verification blockers remain.")


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, KeyError, TypeError, tarfile.TarError, lzma.LZMAError) as error:
        print("REFUSED: " + str(error), file=sys.stderr)
        raise SystemExit(2)
