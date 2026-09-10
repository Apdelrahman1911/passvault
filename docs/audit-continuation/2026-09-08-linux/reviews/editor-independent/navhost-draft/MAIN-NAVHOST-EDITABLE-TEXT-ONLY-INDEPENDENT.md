# Main/NavHost editable-text-only delta: independent source challenge

Reviewer `/root/editor_review`; author `/root/android32`; 2026-09-10.
**SOURCE ACCEPT for this bounded fixture correction only.** This is not proof
that the observed NPE is resolved, a passing test, freeze release or retry admission.

All paths below are under `docs/audit-continuation/2026-09-08-linux/` unless stated.

| Input | SHA-256 |
|---|---|
| Permanent Main fixture, frozen40423B/753LF | `7a0dd50d7caa6cf3cfad729f7393a0e1802fbf65b57d3cceae7c5c64ac94961f` |
| android32/navhost-draft/MAIN-NAVHOST-EDITABLE-TEXT-ONLY.patch (reviews/) | `7f883bdbb31478d2b1fa9d5a8dfad7bae148c84c3ec5904fa57e21f87a8ff046` |
| Same directory proposal note | `ab83f01adec9356a6a94be6cd7ea4abb64b7bd7148cfdfa63e6eaaf10e6f169b` |
| Projected fixture40561B/755LF, never written/executed here | `32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5` |
| reviews/desktop-integration02/EXTERNAL-RESULT.json | `9bf8da5015a9ced2acaa35538e51a79eed1e08c13a964c067a4d524e457b8574` |

Independently reconstructed the sole16-to18-line hunk at428, with2 removals and
4 additions. Exact inverse recovers every frozen7a0d byte. Only the captured
editable interface/comment, text-reader receiver and editable flag change;
private-bus, terminal-EDT, scenario, events, bounds and cleanup remain unchanged.
No whole-fixture/dependency/framework re-audit was performed.

## Actual failure, narrowly attributed

Read six compact retained E files against the inventory above: main log/events/
exit, seed events/exit and Main XML. Main log
`5435f264d33ed527a3b5162757a94435f5c1ef6abd48b2ba90d22e13b1a55797`
shows FutureTask/ExecutionException -> ComposeAccessibleText.getCharCount589 ->
snapshot.visit432 -> scenario291. Main XML
`c3e7b3f0aa2afa4468915381d20972a2e04098db617d8453de253fb4e5a1402d`
records exactly one failing case, MAIN_NAV_DRIVER_FAILED at parent runRole139.
Main events contain only DRIVER_FAILURE; main exit is137. Seed's two events/0
are not reopened durability; inventory has no verify-role receipts.

Password-entry and Unlock-click helpers returned before line291. This does not
prove successful unlock, an identified offending node, entry into edit mode or
any dirty-navigation assertion. Parent139 notices the failure marker while the
child lives, then finally150 uses captured-child force containment.137 is
consistent with that path, not OOM, clean Main shutdown or exact signal-issuer
proof. SKIKO's GL fallback warning establishes no NPE cause. Generic non-editable
text wrapper failure is plausible but unproven; editable/stale/disposed provider
failure remains possible. No product or dependency defect is inferred.

## Consumers, guards and counterexamples

The only MainNavAx.text consumers are readText346 and readable replaceText359;
both select textField541, which already requires editable. Names/roles/actions/
state drive credential-card, buttons, tabs, dialog, field-edit ancestry and all
navigation assertions. Those metadata reads and positive controls remain.
Requesting arbitrary non-editable payloads was unnecessary for this scenario.

AccessibleEditableText supplies the AccessibleText API. Capture it once and use
that same object for the bounded payload read and editable flag; this does not
claim an atomic tree or stable provider internals. No catch/default/fallback or
assertion is added/removed. The following distinctions survive:

- Non-editable labels may have null/throwing text wrappers: their payload is not
  queried, but their names/roles/actions and required selection assertions remain.
- Null editable interface cannot satisfy textField; absent required targets fail
  the bounded await. Empty editable content stays empty string. Oversized lengths
  fail0..1024; expected-value mismatches and duplicate targets still fail.
- Getter/charCount/character-reader failure on an editable or disposed node still
  propagates through onEdt/await/driver failure. No exception becomes a null target
  or successful missing-dialog assertion. Provider misuse is not repaired here.
- Password readable=false suppresses equality verification, **not** all snapshot
  reads of exposed editable password payloads. No masking/cleartext-security claim.
- Nullable getAtIndex append behavior is unchanged. Existing equality checks are
  not a universal malformed-provider/null-character validator. This narrows the
  proposal wording; author explicitly agreed, with no further patch change.
- Snapshotting a disabled field does not authorize input: click requires enabled,
  replaceText requires focus, and active-window/native geometry checks remain.

Direct sibling snippets confirm Rendering6e505b6e...501-510 and Rooma67ba81a...
564-573 use the same broad old accessibleText pattern, not a protective precedent.
Their source and successful evidence are not changed/replayed; integration02 Room
was unstarted. No new sibling failure or generalized accessibility success follows.

## Adoption and remaining evidence

Root owns freeze release/application and exact-source rebinding. Any future run
requires fresh independently reviewed scope/instance/coordination/cleanup admission;
consumed integration02 is not revived. Main remains one case/three serial roles/
thirteen required events with no Main pass or closure credit from this correction.
Other actual-case/visual/cleanup adjudication belongs to its independent lanes,
not this source review. Preserve all failures, STOP/NO-RETRY/CLOSED/HOLD scopes,
PVD/hardware/publication/occupied1017001 boundaries and separate denominators.

Only stable bounded source/retained-evidence reads, inert one-hunk forward/inverse
and this exclusive0600 review write occurred. Inputs re-pinned; file+parent fsync
and exact readback checked. No permanent source edit, helper import/AST/syntax,
build/test/Git/dependency/tool/process/runtime probe, deleted-root access, cache,
background worker or temporary runtime output. No new acceptance JSON was created.
