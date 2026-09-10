# C15 source-prepare01: independent delta and preparation acceptance

Reviewer: `/root/verification`; author/executor: `/root`; date: 2026-09-10.

**ACCEPT ONE EXACT METADATA-ONLY PREPARATION INVOCATION.** This is neither
Detekt/GUI03 execution admission nor a runtime, build-slot or later-instance
approval. No predecessor execution authority is reused.

## Reviewed identities

All paths below are relative to
`docs/audit-continuation/2026-09-08-linux/`.

- Current `reviews/desktop-integration03/source-prepare01/SOURCE-PREPARE.py`:
  17,766 bytes, 288 LF, SHA256
  `677f3f675bd8df24e436535712e96acd94ee9c28eeb2309d1b4895f67bcd62b9`.
- Initial unbound03: 17,566 bytes, SHA256
  `44221895b48f10a67e3ed3b0f4bae9a76483cd24389aacdbd03d7ac79bd0b9f7`.
- Consumed `reviews/desktop-integration02/source-prepare01/SOURCE-PREPARE.py`:
  17,751 bytes, 288 LF, SHA256
  `999261dcda91b1ce28480d539c621065d4c640f0e0aca070d096386610c57a41`.
- Prior whole-body acceptance
  `reviews/editor-independent/INTEGRATION02-SOURCE-PREPARE01-REVIEW.json`:
  6,867 bytes, SHA256
  `d6c9b357fa37cedc821220cd622bb3f1faef61f92027bed147501a2b09fa71bc`.
- Fresh `reviews/desktop-integration03/SOURCE-STORE-ADMISSION.json`:
  4,518 bytes, SHA256
  `d6019b8fd192d8db6bb472c725fa451b06aec5bb2e7385979fd0fd62e326a3fb`.
- `publication/CHECKPOINT-15-PUBLISHED.json`: 51,545 bytes, SHA256
  `d596b0a69f4a158943365eaef8a3088e169aec441dbacd7b3d9c13421164b3ca`.

P = `8f42274b04e206ff7254ca33d686a9666fce6723`;
tree = `3a8f53dddd54f5c42c34f772975f02619be118d5`.
T = `/root/projects/PassVault/passvault-publication-20260910-01`;
G = T/`.git`. This is the retained publication clone, not a retired store.

## Complete delta and fresh binding challenge

The complete inert textual diff has exactly nine single-occurrence substitutions:
header, D, P/tree, FACTS path/hash, PUB path/hash, report format, publication
status and two diagnostic labels. Independently applying all nine to consumed02
equals every current03 byte; reversing them equals every consumed02 byte.
Reversing only the three P/TREE, FACTS_SHA and PUB_SHA assignment lines to
`None` recovers the exact initial unbound03 hash above. Functions, control flow,
Git argv/environment, guards, EOL derivation, output protocol and cleanup are
unchanged. Reuse the prior accepted machinery and its limitations; no new
general-helper review or replay is required.

Fresh facts, bound constants and publication receipt agree on P/tree/T and
`PUBLISHED_EXACT_C15_SOURCE`. Retained publication results record the exact
commit/tree, successful dedicated-branch push and matching remote-after P;
protected refs/handoff/tag are unchanged from that receipt's before observation.
Those are root-recorded results, not reviewer network observations.

The retained basic core/public-origin/explicit-branch config text reconstructs
its exact 435-byte SHA256. HEAD, current branch and unchanged shallow-boundary
bytes similarly reconstruct their recorded hashes and lengths (56/41/41).
The shallow boundary remains `b2e826fd534be8fefede169d28e30c22e37530a9`.
Facts bind original T/G/parent/lock identities, metadata pins and publication
hash/size; live identity checks remain the preparer's responsibility under
root's explicit T/store/checkout/index/config/excludes freeze. No live store,
lock, tool or process probe was made by this reviewer.

For preparation coordination only, the retained 18:04 UTC point records 322
name rows, no positive candidate, 19,239,403,520 free disk bytes and
29,713,108,992 available RAM bytes. Its linked branch-only CI observation
records eight completed rows, exit0/reaped, no activation. SHA256:
- `reviews/detekt01/c15-preflight/HOST-POINT.json`:
  `59a2412dc7a11271fa1808fe2be3a75ab47f97726a4a87ed87088e36aa4e3104`.
- `reviews/detekt01/c15-preflight/CI-ROWS.json`:
  `d7a18e514bb9e323bd1a54dcadd4b33f9bbfddd2a609763433d8889f9a0da76e`.
- `reviews/detekt01/c15-preflight/CI-OBSERVATION.json`:
  `ca9156b2ba2ee0aa1d5f6af88351ed6a5d7ac66791e1da153b14b616db90c90f`.

This negative retained point is not a global-settlement, continuing-exclusivity,
resource-reservation or build-launch guarantee. Root maintains sole ownership.

## Once-only admitted operation and expected evidence

Root alone may invoke, once, exactly:

```text
/usr/bin/python3 -I -B -S /root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/desktop-integration03/source-prepare01/SOURCE-PREPARE.py
```

Reviewed bytes/data and the source freeze must remain unchanged. Fixed isolated
root entry, original no-follow/nonblocking exclusive lock, original directory
and input pins, absence guards and fresh output-name checks must pass.
Exactly two read-only `/usr/bin/git` commands are admitted under the unchanged
fixed argv/environment, cwd T and explicit G:
`rev-parse P P^{tree}`; `ls-tree -r -z -l --full-tree P`.
No network, cat-file, additional Git, old-store access, checkout mutation,
binding edit, runtime allocation or build/test/CI command is admitted.

Expected evidence: exact 82-byte P/tree response; complete NUL inventory within
2 MiB/3,000 regular-blob entries; one observed bounded buffer per checkout file;
one exclusive0600, fsynced/read-back `SOURCE.json` / `SOURCE-CAPTURE.json`
pair in the new source-prepare01 directory, each below 2 MiB. Member count is
an actual output, not the predecessor's 2,607 copied forward.
Retain external final stdout/exit and reconcile both Git exits/reaping,
manifest/capture/stdout hashes and final closure. Preterminal success fields
alone do not establish a successful preparation.

The raw/EOL contract remains qualified: exact Git mode/OID/size and blob-header
SHA1; only the 15 pinned historical paths may attempt exact CRLF-to-LF
candidates. Unknown/non-exact mismatch refuses. Both historical qualifications
require unchanged script AND raw-copy raw-Git tuples. Checkout fields are
observed buffers; calculated raw candidates are not cat-file transport
observations, atomic-checkout proof or application/runtime equivalence.

Unchanged bounds: 180s cooperative work, 30s/Git; 32 MiB/file and separately
128 MiB total raw/checkout; initial 12 GiB free disk/25% available RAM, later
8 GiB/20%. Normal Git children are reaped; lock/descriptors/streams/selectors
close. Abnormal direct-child terminate/wait4s then kill/wait4s is not descendant
settlement or a hard deadline. No runtime/cache/build/generated binary is
created, no Gradle stop duty arises, and lock/retained evidence must not be
deleted. Failure, drift, hard interruption or uncertain cleanup means HOLD,
preserve partial outputs and external failure, **no automatic retry**.

Root's proposed Detekt-first then two-case GUI03 use of this source requires
separate fresh execution/binding/instance admissions and continued unchanged
source. C13's unused preparatory HOLD is not Detekt execution evidence.

## Preservation and reviewer activity

All consumed/HOLD source and application outcomes remain, including original
source-prepare01's missing-ancestor failure, its consumed successor success
and integration02's recorded failed validation. PVU-007 STOP, PVU-011 NO-RETRY,
PVA-029 failure/no automatic retry, G7/G8 CLOSED, old stores/recovery scopes,
eight PVD boundaries, hardware gaps and publication/occupied1017001 fences
are unchanged. This note supplies zero application cases, closure credit,
readiness percentage or demonstrated Main/NPE resolution.

Activity: bounded no-follow inert source/retained-JSON reads, textual diff,
literal forward/inverse checks and hashes only; no helper execution/import,
AST/syntax test, Git, build, CI, live host/tool/store probe or temporary runtime.
Only this new exclusive0600 review was written, file/parent fsynced and stable
readback checked. Root owns all subsequent execution and actual reconciliation.
