# Clone-handoff storage: evidence viewer v2 — inert-source review

## Disposition

**CONDITIONAL_SOURCE_REVIEW_ONLY.** The four concrete findings against v1 appear addressed in the exact v2 text below. No additional concrete source defect was identified within this bounded review. This is neither permission to execute the viewer nor a transport/archive PASS, source-tree/commit acceptance, build admission, security closure, or publication approval.

This is a new review. It does not overwrite or retroactively change the v1 REVISE disposition in `VIEWER-REVIEW.md` (6853 bytes; SHA-256 `711f2bf4dfda837217d84d03abf3a34cca7aab353b3e454f38dd019ad4e2ac84`).

## Exact inputs and authority

- Reviewed inert source: R/`handoff-publication/20260908T061500Z/reader-v2/evidence.py`; 13208 bytes; mode 0600; 277 LF-delimited lines; SHA-256 `27c97a3df01da76efbec6b22041f5c2157a297a328969986928d22ae7edc1f0d`.
- Declared source transport metadata: R/`handoff-publication/20260908T061500Z/reader-v2/current/source-transport.json`; 423256 bytes; mode 0600; SHA-256 `4b1f8ea5e78742faf39622a8291fb42c6b277c5d06d428fca0c3733e1611769c`. H009B hash-checked the full bounded file, displaying only container shape/scalars and its two checkout-difference records.
- G12 raw source-manifest SHA-256 `2f029f9cefd3262313e960b1bcc3cabd875d02ebe31a44e6e78f946bb3e98f1f`; canonical manifest identity `7fd2dab3eac6269baede8a0ca550f0cbdfee20391a12dfbd9ac84cdac5401d91`. The v2 constants retain these distinct identities.
- R aliases the existing named remediation-report root; the intended executable location is NEW/`docs/audit-handoff/evidence.py`, not the R snapshot. This review does not infer that the intended location or later published bytes equal the reviewed snapshot.
- Applied the named copied release-provenance-and-promotion skill (5503 bytes, SHA-256 `d7259a4997f13e21783a721224236540a00b4c06ee8902e0c41abb9592ce4c67`) only for immutable-byte binding and authority separation. No skill utility was executed. A committed skill-collection revision was not established.

The root owns all packaging, source changes, Git operations, execution, final provenance checks, and central statuses. No PACK/INDEX/PACKAGE generation is admitted by this source-only disposition.

## Prior concrete findings and observed v2 changes

### V-F01 — descriptor and pathname safety: addressed in text

Lines 42–97 explicitly require POSIX/no-follow support. A real root directory is opened with directory/no-follow flags, then each relative parent is checked and opened descriptor-relative. Parents and leaves must remain on the root device. The leaf must be a regular single-link file within its cap before a no-follow/nonblocking open; full file identity is compared before/opened/after-read/after-path. Parent links and the root descriptor/path identity are checked again before returning data, and descriptors are closed in finally blocks.

These changes address the earlier check/open race and special-file blocking concerns in the reviewed access path. They are per-file observations, not a claim of a whole-operation atomic freeze, provenance of the interpreter/source bootstrap, or immutable future filesystem state. POSIX-only behavior is explicit and is not treated here as a defect.

### V-F02 — bounded XZ/TAR interpretation: addressed in text

Lines 147–157 cap the checked compressed input at 32 MiB, use an XZ-only decoder with a 32 MiB decoder memory limit, bound each produced output chunk, reject expanded output above 72 MiB, reject truncated input, and reject trailing/concatenated XZ data.

Lines 162–190 interpret individual 512-byte headers rather than delegating archive iteration/extraction. They require USTAR framing, regular files only, content-addressed blob names, normalized numeric metadata, exact expected size/hash/membership, bounded zero padding, and at least two zero EOF blocks with all remaining data zero. Extensions such as PAX/long-name records are rejected as nonregular member types before their bodies are interpreted. Only the fixed-size header is passed to TarInfo.frombuf.

This addresses the unbounded decoder-dictionary and pre-yield metadata concerns raised against v1. These finite constants are source-level guards, not measured peak-memory/CPU evidence or actual validation of a particular archive. No archive was opened or decompressed by this reviewer.

### V-F03 — orphan package packs: addressed in text

Lines 140–141 require equality between PACKAGE paths under evidence-packs/ and the set of packs referenced by the index. Lines 263–267 check listed transport files and check the exact member set of every indexed pack. The earlier package-only orphan-pack case can no longer silently escape archive-member inspection through that path.

The reviewed code still depends on the externally frozen package/index/viewer bytes for authenticity. A self-consistent manifest is not itself a trusted publication signature or Git identity.

### V-F04 — terminal controls: addressed in text

Lines 33–39 reject Unicode category C* in paths in addition to absolute, backslash, NUL, empty, dot, and dot-dot components. The list path therefore no longer prints accepted control/format characters from indexed names.

Lines 256–260 require strict UTF-8 and no NUL for displayed content, then escape category C* except LF/tab. CR, C1 terminal controls, and bidi format controls are no longer passed through this display path. This escaping changes the display, not the retained blob bytes.

## Source-transport branch, not source equivalence approval

Lines 194–231 retain a hard-pinned raw/canonical G12 manifest and 811 members. Non-raw matching is restricted to the two named Windows source paths below. For such a member, the checked raw original must match the G12 size/hash; the only computed alternative is CRLF-to-LF followed by LF-to-CRLF; actual bytes and the declared checkout hash must match that alternative. POSIX executable-bit checks remain explicit. The printed claim separates raw-exact and declared checkout-EOL counts and disclaims semantic/application-test/build coverage.

The hash-checked transport metadata itself says these are expected identities computed before staging, with actual index/commit/clone reconciliation required separately. Its two declarations are:

| Source path | Raw bytes / SHA-256 | Expected checkout bytes / SHA-256 |
| --- | --- | --- |
| scripts/test-windows-checksum-boundary.ps1 | 11703 / 17e5d8e8a30ec0ececf31fd20453938a904ab657573b80cccaee72fe35943141 | 11940 / 7c9dae59e786612de3852b28ee8bb1ee64ba3b97d31af8d9f3d50f0617c818c7 |
| scripts/update-desktop-biometric-checksum.ps1 | 4213 / 3d7221596a01c46b23100f7f05b6c0ebd246af141ce943b3f63f4704be19a396 | 4221 / 855c8f71a8437d2469917ff75dc6d17b3ac34c041d3cfbc8d8b6e15410541aa1 |

This report records those declarations, not an independently observed checkout transformation or actual Git blob/index/commit/clone result. Raw originals/final metadata bindings remain separate packaging evidence; no script semantics, stopped investigation, checksum regeneration, or runtime result is inferred from these byte declarations.

The earlier SOURCE-COPY-CHECK.json remains a distinct observation: all 811 NEW raw tuples matched at that read, including all 122 changed members. It is not rewritten into a later 809+2 checkout claim.

## Administrative observations and non-actions

- H009 stopped before metadata content reading because a reviewer-selected 65536-byte cap was below the actual 423256-byte size. Parent supplied the full pin; H009B verified it under an explicit 1 MiB bound. This was not a hash mismatch, viewer failure, or runtime gate retry.
- H010 stopped at the named open of the assumed reader-v2/raw-source/ directory because it was absent. No raw-original content was read. Exact original locations were requested rather than enumerating or probing alternatives. This is a snapshot-location assumption, not a defect proven in the viewer.
- No viewer/helper/project import or execution, syntax/AST check, archive extraction, build, test, network, private-data read, process/resource probe, primary/old-root traversal, source edit, Git mutation, or central-status change occurred in this review.
- G7/G8 remain CLOSED; PVA029 remains FAIL/no automatic retry; PVU007 remains STOP/no investigation or reformulation; PVU011 remains NO RETRY/no containment relaxation.
- Final frozen transport and actual publication checks remain root-owned. No new requirement beyond verification of those already-declared byte bindings is created by this conditional source-only report.
