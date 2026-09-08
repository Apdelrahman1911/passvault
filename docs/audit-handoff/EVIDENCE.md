# Evidence map and portability

`current/` contains exact copies of the current high-value ledgers, source
identity, owner decisions, runner disposition and next-regression matrix.
The explicitly named `unresolved-investigations-outcome-only.json` is the one
derived current summary, not an exact copy: stopped rows are reduced to their
unchanged status and boundary; the other ten rows retain exact JSON values.
The full frozen source changes and permanent regression tests are already in
the branch. **Do not reapply historical patches.**

The larger historical report collection is deduplicated into bounded compressed
`evidence-packs/*.tar.xz` files. `evidence-index.json` maps each original
workspace-relative report path to an exact SHA-256 blob and pack. This preserves
included bytes and their original references without putting hundreds of MB of
duplicate journals/snapshots into every checkout. Nothing in these packs is
automatically extracted, imported or executable.

## Read-only access

Inspect `evidence.py` before use. It is a small standard-library-only reader,
not the historical validation runner. It never extracts files, imports packed
code, starts processes, contacts the network or edits source/storage.
The descriptor-based reader deliberately requires POSIX no-follow support
(macOS/Linux); native Windows execution is refused rather than silently using
weaker pathname checks. Its XZ decoder has an explicit memory bound and total
decoded-TAR bound, and accepts only normalized ordinary USTAR blob members.
These are transport controls, not an adversarial same-user isolation guarantee.

From the clone root:

```text
python3 -B docs/audit-handoff/evidence.py list remediation-reports/20260905T222925Z/reviews/g12-
python3 -B docs/audit-handoff/evidence.py show remediation-reports/20260905T222925Z/findings/issue-to-fix.json
python3 -B docs/audit-handoff/evidence.py verify
python3 -B docs/audit-handoff/evidence.py verify-source
```

These are read-only handoff-integrity operations, **not application tests** or
build admission. `show` emits only UTF-8 text and refuses binary payloads. Use
the index to inspect a binary's recorded hash/provenance without launching it.

## Resolving historical references

- Within an R report, `reviews/...`, `verification/...`, `findings/...`,
  `coverage/...`, `evidence/...`, `tools/...` and `delivery/...` usually resolve
  under `remediation-reports/20260905T222925Z/`.
- Within an AR report, resolve under `audit-reports/20260905T083114Z/` instead.
- Workspace-absolute `/Users/abdelrahman/Projects/passvault/...` references are
  capture-time paths. Remove that historical workspace prefix for index lookup,
  **not** for path-based execution/deletion/recovery.
- W application references resolve to this clone only when the exact source
  generation/hash also matches. Older G5/G11 uncommitted W references may require
  their retained preimages/after-images, not current G12 bytes. A and the
  committed baseline can be inspected through Git at the recorded full commit.
- Primary-checkout, old runtime/cache/home/temp and device/inode references are
  historical observations, not accessible or authorized clone resources.
- Some hashes refer to older versions of mutable central files. Use the exact
  immutable snapshot named by the historical review, not today's file with the
  same short name. The index permits finding all retained same-hash aliases.

The mechanical reference ledger states its descriptor-selection rules and typed
omissions/aliases separately. These resolution hints alone are not proof of
transitive closure; unresolved references remain explicit.

## Omissions and privacy boundaries

`omissions.json` explicitly lists withheld or unavailable members and reasons.
It is part of the checkpoint, not an invitation to recreate excluded procedures.
Raw filename-associated PVU-007/PVU-011 procedural captures are withheld from the
portable pack; their outcome and STOP/NO-RETRY status remain in current ledgers.
Original unresolved-investigation ledger blobs are also withheld in favor of
the explicit current outcome projection. This does not claim that every mention
of stopped scopes has disappeared from mixed historical reports; such mentions
remain historical evidence and must not be used to reopen those scopes.
Two official-web-page captures contain API-key-shaped embedded site data; their
raw blobs and aliases are withheld, not silently rewritten to keep old hashes.
The original local evidence is preserved. Relevant authoritative URLs and the
already reviewed conclusions remain in adjacent research/review records.

Only audit-owned reports/source snapshots are considered for packaging; no
private vault, installed application, signing/configuration folder or shared
cache was opened for it. Static marker checks and bounded reviewer inspection
are not a mathematical guarantee that arbitrary text contains no secrets.
The publication screening record states its exact scope and limitations.

Any other absent reference must remain explicitly unavailable until settled;
do not invent an artifact or treat a missing proof as a pass. The pack's
integrity check proves transport identity and membership only, not source
correctness, review completeness, runtime results or fresh platform guarantees.

Git does not preserve old 0600/0700 modes, inode/device values or observation
times. Those are retained as report text. Pack headers are normalized inert
regular files. JSON large integer/stat values must not be rounded through a
JavaScript number roundtrip when independently checking historical evidence.

## Git line-ending transport is not raw-byte identity

The unchanged application `.gitattributes` checks PowerShell/batch files out
with CRLF. Two G12 working files had LF/mixed endings. Their ordinary Git clone
checkouts therefore differ byte-for-byte from the frozen raw source:
`scripts/test-windows-checksum-boundary.ps1` and
`scripts/update-desktop-biometric-checksum.ps1`.

`current/source-transport.json` binds original, normalized Git blob and expected
checkout hashes without accepting arbitrary whitespace changes. Exact originals
are preserved in `raw-source/` as inert `.raw` data; the handoff-only attributes
disable normalization of evidence. The source viewer reports exact-member and
declared-EOL-member counts separately. There is no claim that the two changed raw
identities already have a fresh runtime or coverage pass. Freeze the actual
validation tree, or deliberately restore the exact two originals only in a
separate synthetic-validation worktree, and independently bind the resulting
identity before execution. Do not replay an old materialization helper.
