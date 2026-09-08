# Disclosure: two earlier inline dependency-source data reads

Author/reviewer of this disclosure: `/root/build_config`.

These reads produced `LOADER-EXCERPTS.json` and `LOADER-FOLLOWUP.json`. They were
**new, ad-hoc inline Python readers**, not the supplied POSIX transport verifier
and not the subsequently admitted Git reader. They must not be silently equated
with either tool's independent review or integrity guarantees. They provided
selected historical dependency **source data**, not application executions,
transport qualification, runtime authority or binary/source equivalence.

## Command and source-retention qualification

Both command envelopes were:

```text
python3 - <<'PY'
<inline standard-library JSON/XZ/TAR data selection and excerpt code>
PY
```

Working directory: `/root/projects/PassVault/passvault-linux`. No Gradle wrapper,
Java/native library, application/recovery helper or old validation runner was
executed/imported by either reader. Captured source was decoded as text, and the
SQLite sources JAR was inspected as an archive in memory; no packed file was
extracted to disk. The inline bodies were not saved as repository executables
and were **not independently source-admitted before these reads**. A byte-exact
script source/hash and independent pre-execution review therefore cannot be
supplied by these retained reports. The operations/bounds below describe the
actual methods, not a fabricated exact-command transcript.

The invocations also lacked `-I -B`; they were not an isolated Python-import
environment proof. No captured payload was intentionally imported or executed.
Further historical reads were switched to the separately reviewed, isolated
Git-only reader; the original frozen POSIX reader's refusal was not weakened.

## First read: `LOADER-EXCERPTS.json`

1. Read this checkout's `docs/audit-handoff/evidence-index.json` and
   `docs/audit-handoff/PACKAGE.json` using `Path.read_bytes` and ordinary JSON
   parsing. Select exact known public-source records for the libsodium0.9.5
   initializer, JNA5.19.1 `Native`/`NativeLibrary`, and resource-loader2.0.2
   `ResourceLoader`/`SharedLibraryLoader`. Full record paths, byte sizes and
   SHA-256s are retained in the output JSON.
2. Read selected compressed packs001 and009 as bytes, with a32MiB compressed
   cap, and compare size/SHA-256 with `PACKAGE.json`.
3. Decode with `lzma.LZMADecompressor(FORMAT_XZ, memlimit=32MiB)` using at most
   1MiB output per call and a72MiB total expanded cap. Refuse truncated or
   trailing XZ input. Expanded contents remained in memory.
4. Open `tarfile.open(fileobj=io.BytesIO(expanded), mode="r:")`. Select only the
   exact `blobs/<sha256>` names associated with the requested records; require
   each selected member to be regular and its indexed size at most200,000bytes.
   Read at most200,001bytes, then compare exact length and SHA-256.
5. Print/retain entire sources shorter than100lines or selected
   loader/temp/property regex intervals. Write the compact JSON excerpts to the
   review directory. Source was never evaluated as Python/Java/Kotlin.

The command returned exit0 in approximately0.5seconds. There was no persistent
child process, disk extraction, downloaded binary or disposable build output.

## Second read: `LOADER-FOLLOWUP.json`

1. Use the same checkout manifest/index and XZ/data-reading approach for exact
   records in packs002 and009, limiting selected regular members to100,000bytes
   and checking their indexed hashes.
2. Read the captured SQLite-bundled2.6.2 source JAR:
   `remediation-reports/20260905T222925Z/evidence/g8-pagination-navigation-public-sqlite-bundled-2.6.2-sources.jar`,
   14,841bytes, SHA-256
   `3b4d8478f8c2a6b4d67d6460b2caeb23a046206f8787234d8adb8b6d708ba6b2`.
3. Inspect that JAR with `zipfile.ZipFile` over an in-memory buffer; require at
   most100members and at most1MiB total declared expanded size. Read only
   matching loader/JVM Kotlin entries and retain their hashes/text. No member
   was extracted, compiled, linked or imported. **The JAR did not contain the
   JVM loader source**: its common expect/native no-op sources do not establish
   JVM native extraction or `org.sqlite.tmpdir` support.
4. Read additional selected `ResourceLoader.java` intervals concerning native
   permissions, temp-directory creation and deletion. Persist only compact
   `LOADER-FOLLOWUP.json`.

The command returned exit0 in approximately0.5seconds. All in-memory archive
handles/interpreters ended. No JVM, daemon, build worker or background job was
started; wrapper `--stop` was not applicable to either data read.

## Integrity and admission limitations

- Selected pack/blob hashes were checked against the **checkout** manifest and
  index. These two inline readers did not separately Git-bind those metadata
  bytes to the checkpoint, verify full package/TAR membership or implement
  duplicate-key/canonical-TAR validation.
- They did not implement no-follow/stable-file-descriptor/root/device binding.
  The32/72MiB archive caps are not a bound on total interpreter RSS; in-memory
  buffers and copies coexist. There was no separately installed wall-clock
  timeout in these two original command envelopes.
- This is not an approved replacement for the supplied transport verifier and
  gives no evidence for historical helper execution, checkout preservation or
  reopening G7/G8. Their source observations must remain qualified accordingly.
- The subsequently accepted reader at
  `scripts/audit/read_handoff_git.py`, SHA-256
  `f1e0c83a50c1b9c327ff49c5c6444cefa9206fd3c1825ba37f80cc3b7794b304`,
  binds committed objects and has its own independent review. Its successful
  later reads do not retrospectively add missing safeguards to these commands.

Only compact review evidence remains. No source, permanent test/report,
shared cache, SDK or toolchain was deleted. These observations add zero test
cases, closures or conclusive suspicion resolutions.
