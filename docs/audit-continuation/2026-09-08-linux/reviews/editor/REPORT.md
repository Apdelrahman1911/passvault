# Editor/navigation continuation — bounded author report

Author `/root/editor`; independent finding/patch challenger `/root/editor_review`.
Linux continuation, 2026-09-08. **SOURCE WORK; NO NEW EXECUTED TEST CASES OR CLOSURE.**

This continues PVA-007/PVA-031 and PVU-004/005/006 from the published outcomes. It
does not restart their original investigations or replace historical evidence.
Root alone owns build admission/execution, central ledgers, commits and pushes.

## Source identity and instructions

- Preserved handoff commit `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, tree
  `05014e9f635131d5db06701e4013b4b5a746465a`.
- Work was authored in the separate `codex/audit-continuation-linux-20260908`
  worktree. Per-file before/after hashes and source-declared method identities
  are in this directory's `SOURCE-INPUTS.json`. Dirty after-images are not
  represented as the immutable handoff tree. Root's later freeze/commit binds
  the integrated continuation.
- During this work root committed the accepted Back patch at
  `488ab465125234e02bfbd28b3e7579251341cb54`, tree
  `4f4c594ff19e62e19f641304b95eb6c086781ae7`. The source manifest records that
  containing HEAD and separately identifies still-dirty row/input after-images.
- Applicable handoff/start/permissions/assembly/publication qualifications,
  current issue/outcome/coverage/verification ledgers, regression prerequisites,
  runner readiness, PVD decisions, repository AGENTS and focused navigation,
  Back, restoration, Desktop lifecycle and UI skill snapshots were consulted.
  The generated `agent-skills` mirror was not independently edited.
- Original macOS paths, locks, process identities and historical successful
  tests remain evidence only. No old validation runner/helper was executed or
  imported. No old G7/G8 application, recovery or cache scope was reopened.

## PVA-007: action-policy freshness before the next frame

**Author-originated, independently confirmed and after-image source accepted.**
`../editor-independent/FINDING-REVIEW.md`, `BASELINE-INPUTS.json`,
`FRAMEWORK-SOURCE.json` and `BACK-PATCH-REVIEW.md` contain the independent
challenge. `BACK-PATCH-INPUTS-v2.json` binds the accepted four-file after-image.

An accepted inline edit changes the production VM synchronously, while the
old registered composed policy can still be `PopNow` until `SideEffect` runs.
Central Escape/Back can then pop directly instead of asking the VM to discard;
same-tab reselection can reset the stack behind the stale forward guard.
Pinned Compose Desktop source does not impose full recomposition before every
key event. This is a source-reachable schedule, not an observed native input
run. Toolbar Back is a counterexample: it already asks the VM directly.
Different-tab selection preserves the tab stack and is **not** alleged to be
data loss by itself.

The correction adds an optional current `BackPolicy` pair to registration.
Each decision checks the host/entry token first and resolves one
disposition/forward-blocking pair from the live credential VM. All three
credential adapters use the same remembered-by-VM supplier. Existing snapshot
keys still drive rendering updates; other registrations, callback/disposal
identity, root fallback and entry/session guards keep their old behavior.
Busy/dirty action completion can reject before another composition, without
inventing a gesture-origin token or redesigning navigation/restoration.

Two new common coordinator methods exercise one-read policy resolution and
host/stale-session rejection. The stale-session control reactivates the **same
route key** with the old registration retained, so it needs the session guard,
not merely a missing map entry. Seven new Desktop source-declared methods use
real `Composition`/`Recomposer`, production registration/VM/navigator, a
controlled frame clock and copied in-memory fake persistence. They check:

1. inline edit reaches discard confirmation before another composition;
2. an attempted same-tab reset behind the production gate preserves the stack;
3. newly dirty interactive completion is rejected;
4. synchronous Save admission rejects stale clean Back/forward/completion;
5. row Cancel restores clean Back before replacing the dirty registration;
6. an unchanged draft permits one pop and rejects a duplicate completion;
7. disposing the actual registration leaves the guarded route fail closed.

Independent review strengthened the forward/session controls, stabilized the
supplier, and required separate bounded settlement of both the Recomposer's
runner and its own effect lifecycle. Cleanup preserves primary assertion
failures plus all cleanup failures. This history is retained in the independent
report; it is not an executed test-failure history.

**Limits:** no rendered CredentialEditScreen/NavDisplay, actual key/pointer/IME
delivery, native preview/gesture, LTR/RTL or physical-device evidence. In
particular, the live completion guard cannot retract an already rendered prior
entry before recomposition; visual eligibility remains unproved. PVU-004's
held-gesture origin/over-pop concern is not solved by this patch.

## PVA-007: row Save must not carry an old composed draft

**Reviewer-originated, independently confirmed by this author; product and
regression-source after-image independently accepted.**
See `../editor-independent/ROW-SAVE-FINDING.md` for the independent origin,
before-image identities, reachability schedule and counterexamples, and
`ROW-SAVE-PATCH-REVIEW.md` / `ROW-SAVE-PATCH-INPUTS.json` for the initial
six-method patch seal. Later input changes are explicitly rebound rather than
presented as unchanged files; see `BACK-ROW-INPUT-INTEGRATION-REBIND.json`.

The old row Save closure constructed `OnCustomFieldUpdated` from its captured
draft. After a newer draft had been accepted, an old Save closure could apply
the old tuple and remove the live draft. PVA-026's equality-owner guard avoids
wiping the live value but does not preserve a different draft being removed.
Page Save is a counterexample: it already reads live `commitDrafts()`.
Busy guards do not disprove a non-busy witness; an enabled old nonblank name
does not validate a newer blank name. Pinned pointer source is retained in
`FRAMEWORK-POINTER-SOURCE.json`; layout may subcompose, but there is no mandatory
full credential recomposition barrier established before every click.

Root approved a narrow id-only `OnCustomFieldEditSaved` event. The row now sends
only its field ID; `commitDraft` rereads that field's authoritative draft on
each CAS attempt. Missing draft/field is a no-op. A current blank name is
rejected while preserving the draft and stored field. The shared update helper
retains PVA-026's unchanged-value owner and wipes only unused or replaced owners
at the appropriate point, including unused replacements after failed CAS.
The explicit `OnCustomFieldUpdated` API still uses/clamps its supplied payload,
even without a draft; its previous page-Save blank-name validation boundary is
unchanged. Page Save, Cancel, capacity limits and original 13 draft tests remain.

Six new common source-declared regression methods cover:

1. an event captured before later edits commits the latest name/value/secret
   tuple, retains another row's draft and persists copied values on page Save;
2. a newly blank name rejects an old enabled Save and retains the draft;
3. unchanged and duplicate Save preserve the live sensitive-value owner;
4. unknown/absent/cancelled/removed/locked drafts cannot be resurrected;
5. generation-busy rejection is tested **while the draft still exists**, then
   the same event is accepted after the busy operation settles;
6. explicit-update payload/clamping/without-draft compatibility remains.

These are production VM/event/state assertions with fake copied persistence and
fake crypto, **not actual composed row callbacks, clicks, IME or device tests**.
The new fixture owns the ViewModel through a `ViewModelStore`, captures its
scope Job, clears/cancels and boundedly joins it before restoring Main, and
clears fake copies even when other cleanup fails. Cleanup timeouts use the test
scheduler, not a claim of noncooperative native/process termination.

## PVA-007: cross-control input callbacks must not replace sibling properties

**Author-originated, independently confirmed; minimal root-approved correction
and regression source independently accepted.** See
`../editor-independent/INPUT-CALLBACK-FINDING.md`,
`INPUT-CALLBACK-FINDING-INPUTS.json`, `INPUT-CALLBACK-PATCH-INPUTS.json` and the
`INPUT-CALLBACK-PATCH-REVIEW.md` review. This remains the existing PVA-007 family.

The name/value/secret input callbacks copied a captured *whole* draft. Accepted
text followed before a new row composition by a secret toggle could restore
old text while changing the secret flag. `SecureTextField` forwards the input
callback without a live-draft merge. The VM's whole-draft handler then accepts
the tuple and overwrites its newer sibling property. Neither row nor page Save
can reconstruct an edit already overwritten there. The non-busy loaded witness
meets the guards; same-control full-text updates and intervening recomposition
are counterexamples, not a full-dispatch guarantee.

The UI now sends three property-only draft events, carrying field ID plus just
name, value or secret flag. The production helper transforms only that property
of the latest owned draft inside `StateFlow.update`, rereading on CAS retry.
No active draft means no change: input cannot recreate cancelled/removed/locked
drafts or clear stored-field owners. Frontend/backend Unicode limits and the
busy gate remain. Existing whole-tuple `OnCustomFieldDraftChanged` remains an
explicit bounded tuple replacement for its callers/tests. Row Save, page Save,
Cancel, capacity and PVA-026 ownership behavior are preserved.

Five added source-declared methods reuse the reviewed fixture rather than
duplicate lifecycle cleanup. They cover (1) all six property-order permutations
with another row's draft intact, then one row→page copied fake roundtrip;
(2) Unicode limits and latest sibling properties; (3) absent/cancelled/removed/
unknown/locked no-op controls; (4) all three events rejected while busy with a
still-active draft; (5) whole-tuple explicit replacement compatibility. The six
permutations are **iterations inside one method**, not six test cases or six
persistence runs. Only the last permutation proceeds to persistence.

The final new class has **11 methods: 6 row-Save + 5 cross-control**. Removing
only the added-five-method span from its bytes in memory reproduces the
independently reviewed initial six-method file exactly (SHA-256
`94131184ff94fec3f4c243385a61af3f731ed146dd681a7f7857e323a7b0f35c`,
16,670 bytes / 362 LF). This is source-preservation metadata, not execution.
No rendered IME/pointer/frame frequency, double-toggle, platform delivery or
device guarantee is inferred from property-event source assertions. These
concrete accepted fixes do not close all editor input/rendered requirements.

## PVA-031 and the three assigned unresolved investigations

| ID | Continued outcome | Required missing evidence / restriction |
| --- | --- | --- |
| PVA-031 | Delivered capacity source correction remains: Add and confirmation use `canAddCustomField`, with VM capacity rejection intact. No new patch. | Existing 7 capacity methods remain unexecuted for the recorded failed setup. Its settings/plugin-resolution failure and zero XML are preserved, not rewritten as a pass or retried automatically. Rendered capacity/Add/remove/save, still-composed dialog input, rapid/keyboard/a11y/LTR/RTL work remains. |
| PVU-004 | Retain unresolved integrated held-gesture/other-Back origin concern. Current Back completion still selects a current registration. | Need actual pinned NavDisplay/input/recognizer integration with intervening scene frames and cancellation/guard/session controls, followed by separately admitted physical iPad/hardware-keyboard LTR/RTL evidence. A fabricated coordinator sequence is not observed over-pop. |
| PVU-005 | Retain logical glass-pane-after-secured-ACK observation; no proved visual obstruction or auth bypass. No patch. | Need selected actual ComposeWindow/Skia heavy-surface rendering evidence through synthetic lock→ACK→restore, independently checking safe Unlock pixels/usability and logical lock. Plain JFrame properties and headless early-return tests do not settle pixels. |
| PVU-006 | Five ordinary Koin single ViewModels still lack an explicit terminal scope owner/onClose hook; harmful externally rooted lifetime remains unproved. No patch. | Terminal native cover/ACK/unmount→application-scope cancellation→database close→stopKoin are real mitigations. Need repeated actual runtime lifecycle with rooted task/resource/late-callback evidence; an unrooted cycle or fake indefinitely suspended synchronous iOS preference operation is not proof. Ordinary-lock persistence is not silently redesigned as terminal cancellation. |

The original 13 draft controls and their historical G5 successful evidence are
preserved. Those historical executions are not current-tree results, and they
do not certify the newly authored callbacks or rendered UI. PVA-026 remains a
separate previously qualified correction whose compatibility controls are
preserved, not another closure earned here.

## Counts, verification and resources

- New declarations at this checkpoint: **20** = 2 common coordinator + 7 Desktop
  composition + 6 common row-Save + 5 cross-control methods. The three complete
  classes declare 29 methods including 9 unchanged coordinator controls.
  **New executions: 0.**
- `SOURCE-INPUTS.json` is a byte/hash/name/line inventory, not Kotlin compilation,
  test XML, branch coverage, device evidence or a runner admission. Source-only
  diff/whitespace checks do not establish completed fixes.
- Counts remain **19/25 original confirmed families**, **22/37 all confirmed
  families**, **2/12 original suspicions**. These are separate denominators, not
  overall readiness. PVA-007/PVA-031 remain open; no suspicion is resolved here.
- Root must independently admit any future exact-source Linux test execution,
  wrapper/JDK17 graph, synthetic HOME/TMP, one-worker/non-daemon/confinement,
  process settlement and cleanup. No candidate command is permission to retry
  an archived G7/G8 invocation. Test declarations do not waive the gate.
- This agent ran source/metadata reads and compact permanent-source/report
  writes only. No Gradle/compiler/test/application/packaging command, persistent
  worker, emulator, server or source-archive cache was started. There are no
  agent-owned generated build outputs to delete; no shared cache, SDK, toolchain,
  source, permanent test, report or unrelated process was removed. Wrapper stop
  was not applicable because no wrapper invocation occurred.
- A later read-only resource sample showed about 29 GiB free on the worktree's
  overlay and about 43,005 MiB available RAM; this was communicated to root. It
  does not retroactively open the earlier resource/admission gate or assert a
  resource reservation/global idle state.
- PVU-007 STOP, PVU-011 NO RETRY, PVA-029's failed/no-automatic-retry result and
  G7/G8 CLOSED scopes remain. No Store/dependency/version/identity, protected
  branch, tag, signing, publication or occupied build 1017001 action occurred.
  All eight PVD explanations and outstanding owner decisions remain separate.
