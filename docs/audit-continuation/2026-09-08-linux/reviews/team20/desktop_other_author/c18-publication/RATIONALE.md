# C18 source-publication helper — new UNBOUND text candidate

Author `/root/desktop_other_author`, 2026-09-11. Root owns actual Git, config,
payload, final selection and admission. This is **not an invocation request**.

- Candidate: `CHECKPOINT-18-PUBLISH.py.txt`, SHA256
  `85f9840ecaf4fa5657f63ae11b3e9eb20560de9492d0173c4b5842e7b28a1345`.
- Consumed predecessor read **as text only**:
  `B/publication/CHECKPOINT-17-PUBLISH.py`, SHA256
  `3232ee3a4fbc6efbfc691c94f2a5abe9f712336aabe0753a9ca866dfe61dfaf4`.
- Intended future root-owned destination:
  `B/publication/CHECKPOINT-18-PUBLISH.py`. It was not written by this lane.
- Exact derivation: `C17-TO-C18-PUBLISHER.patch.txt`; identities are also in
  `SOURCE_HASHES.json`. No helper was executed, imported or parsed as Python;
  only bounded source/data reads, string transformations and hashes were used.
  No T, Git store, process, runtime, SDK, credential or resource probe occurred.

## Small, explicit delta

1. C18 config/payload/whitespace-log/status literals replace C17 counterparts.
   The recipe remains new: C17 is consumed, never a replay or recovery target.
2. `BASE`/`BASE_TREE` still come from future root config, but must equal retained
   C17 commit `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49` and tree
   `d1bd6ca5b18d08ff3ff15896d9a78af9016be799`. The payload, actual T HEAD/tree,
   index and sole-parent checks continue to bind that same predecessor. No
   fresh Git observation is claimed here.
3. Require genuine final reviewer `/root/coverage_review`, disposition
   `ACCEPT_C18_SOURCE_PUBLICATION`, and the exact selected-payload SHA. Earlier
   implementation/helper acceptance is not that final coverage disposition.
4. Require `removals == []`; remove all old inert-draft unlink, zero-mode index
   deletion and base-entry-pop code. The two C17 removals are not repeated.
   The complete new index/tree remains base plus exact selected after-images.
5. Raise only selected-plus-companion capacity400→450, now checked before
   descriptor content reads as well as after uniqueness checking. Root's
   preliminary392-plus-forthcoming estimate is not the actual final selection.
6. Before `frozen(S, allrows)`, require canonical relative paths with no parent
   traversal/newline/NUL, `.git` component or `.github/workflows/` selection.
   This covers both selected rows and companions. It prevents path aliases
   bypassing the exact request namespace and avoids retired-store/config reads.
7. Permit only the following exact leaves within canonical
   `docs/audit-continuation/2026-09-08-linux/requests/`:
   - `LINUX-DETEKT-03.json`
   - `LINUX-ANDROID-COMPILE-01.json`
   - `LINUX-DESKTOP-TRAY-01.json`
   - `LINUX-DESKTOP-INTEGRATION-02.json`
   - `LINUX-DESKTOP-INTEGRATION-03.json`
   Unknown, nested and lowercase platform-trigger paths in that subtree fail.
   Final coverage review must bind their actual consumed evidence; a filename
   is not consumption, success, execution admission or authorization to retry.
8. Take authored whitespace paths from final
   `CFG['authored_whitespace_paths']`: nonempty list, at most450 unique strings,
   exact selected/companion subset, equal to the independently reviewed payload
   list. No stale C17 path names or all-evidence formatting sweep is inherited.
   The existing raw-evidence exclusion and prior whitespace FAIL qualification
   remain verbatim. Only final reviewed authored paths receive `diff --check`.
9. Enforce the GUI03 prospective release's original `T/.git/config` hash and
   full eight-field pin using the existing bounded, stable `read_file` primitive.
   The first read is under the original coordination lock, after original T
   identity/inventory checks and before the first Git command. Its SHA256 and
   all eight stat fields must equal the fixed released values. A second stable
   read is after the final Git/remote check, inventory and resource check, still
   under that lock and before success/unlock. Exact raw bytes plus the full pin
   must equal the accepted pre-read. Only SHA256 and the metadata pin are added
   to the receipt, never raw config contents. No config write is introduced.

## Nonactivation and retained control evidence

The four existing audit workflows retain exact lowercase platform-specific
request filters: `ios-focused-01.json`, `macos-focused-02.json`,
`macos-native-01.json`, `windows-cohort-05.json`. The five uppercase Linux leaves
do not match those source filters. No workflow is selected or changed. This
bounded source observation is not a fresh remote/no-CI observation; root still
owns that admission and final-manifest review.

Inert exact-reviewed `B/reviews/.../REQUEST.json` records remain publishable as
evidence, including unfinished/unbound controls. They are outside the trigger
request subtree and must not be silently omitted from the handoff merely because
their basename resembles an active request. Publishing their bytes neither
executes nor admits them.

**Rejected v1 retained:** `CHECKPOINT-18-PUBLISH.v1-rejected.py.txt`, SHA256
`a756c0e76174327ffc31e426b0afa2afe2a707df6c3cb573dac3daa3834b4e45`.
Its global `REQUEST.json` basename rejection was overbroad. Root clarified that
only the canonical request subtree needs the exact-five restriction; the active
candidate removes that global rejection. V1 was never run/imported and must not
be substituted for the active candidate.

## Original config preservation — v3 only

Fixed expected values come from the retained independent GUI03 prospective
release review, not a new observation of T:
`B/reviews/team20/pva031_review/gui03-closeout/PUBLICATION-RELEASE-REVIEW.json`,
SHA256 `8a4868834e2ace3a63cde9afffa71acd18418f3153f220c5dce22bf477bcff7c`.

- Config SHA256: `036c10a0cc4303fa7de6578390ad7c8094d2a64796f8bb6a6b63cc7954f662d5`.
- Full pin: dev24, ino14293053, uid0, mode33152, nlink1, bytes435,
  mtime_ns1789001766988319322, ctime_ns1789001766988319322.
- `size` in the stat comparison maps to `bytes` in the receipt pin. Atime is
  deliberately outside the released pin; all eight released fields are checked.
- Both prospective observations would be durable SHA/metadata receipt entries.
  The normal success path requires both; a failure or hard loss may prevent the
  final read and remains HOLD with final preservation unproven. No added retry,
  abnormal cleanup framework or inferred after-check is claimed. The two reads
  are not a continuous/hostile-host proof and do not repair older terminal gaps.

**Superseded v2 retained:** `CHECKPOINT-18-PUBLISH.v2-superseded.py.txt`, SHA256
`88e7229c5a96dce5c6bba4b20c51eea59481127fbad4305dc432c79b24e77fcf`.
Root added the mandatory config-preservation condition before source-review
sealing. V2 has no such guard, was never run/imported and
must not be promoted. V3 differs from v2 by exactly the two guard insertions;
`TEXT_COMPARISONS.json` explicitly qualifies the changed control span rather
than describing it as byte-identical.

## Preserved controls, not renewed authority

The existing publication-only transport remains: original coordination lock and
parent identities; T root/parent identities; no alternates/external Git store;
exact clean base index/worktree and dedicated branch; selected raw-byte/mode
hashes before/under lock/after copy/precommit; exact whole index and tree;
protected-ref comparisons before commit/push and after the one nonforce dedicated
branch push; hooks/signing/automatic maintenance disabled only in command scope;
no force, GC, prune, repack, build, CI launch, wrapper, cleanup adoption or
protected branch/tag/version/identity/dependency/signing/Store change.

Unchanged quantitative controls include 512MiB/100000-file T inventory,
16MiB per-file reads, 600s total, 60s ordinary Git/120s push, 5s direct-child
settlement, 15s resource sampling, 12GiB/25% launch and 8GiB/20% running floors,
4MiB stdout/1MiB stderr and 131072-byte stdin limits. Selection growth does not
raise those limits. Necessary source/evidence/T remain retained; no deletion
path is added. Original stores and all prior HOLD/STOP/NO-RETRY/CLOSED outcomes
remain untouched.

Inherited limits remain: config/manifest/final-review freshness and authentic
owner binding need root's separate admission; source acceptance is not actual
publication success. The helper's normal direct-child reaping does not establish
abnormal descendant or hard-loss settlement. Actual tool-terminal objects and
independent postpublication reconciliation remain necessary; old terminal gaps
are not repaired by this derivation. No automatic retry is authorized.

## Root bindings still UNBOUND

No actual C18 config, payload or acceptance was authored here. Root must bind
the exact C17 predecessor, final selection SHA and companion tuples, final
coverage-review path/SHA, equal config/payload authored-whitespace lists,
original namespace-parent facts, fresh slot hash/state, full protected remote
map, new receipt name/commit message and separate once-only admission to this
candidate's final accepted SHA. The intended new config/log/payload/receipt
identities must not reuse consumed C17 files. Root must inspect the final
selection, not use a directory glob or treat450 as permission to include more
than its reviewed paths.

Independent `/root/desktop_other_review` accepted exact v3 as
**ACCEPT_C18_PUBLISHER_TEXT_DELTA_ONLY_WITH_FINAL_BINDING_HOLDS** in
`B/reviews/team20/desktop_other_review/04-C18-PUBLISHER-DELTA-REVIEW.md`, SHA256
`0bc67298495bfd1fc53b07a93875e00e4b579826810290dfcb08980d806ceff7`.
The full report was read; its source-only acceptance is not the genuine final
`ACCEPT_C18_SOURCE_PUBLICATION` disposition and fills no root binding/admission.

The review additionally retains root's final exclusion of every `.github` path
(the candidate guard itself names workflow descendants), real consumed-request
reconciliation, and fresh no-CI/external-state admission. Safe request names
alone do not rule out generic pull-request-triggered CI. Config receipt
observations are not standalone PASS records: both equality gates must pass;
early or post-push failure remains HOLD for actual side-effect/terminal
reconciliation, never repair/reset/replay. This source-only acceptance grants
no source/T freeze release, Git/CI authority, publication success or readiness.
