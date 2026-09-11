# Curtain fixture diagnostics — independent patch review

Reviewer `/root/desktop_other_review`, 2026-09-11; author
`/root/desktop_other_author`.

**ACCEPT EXACT UNAPPLIED PROPOSAL FOR SOURCE INTEGRATION ONLY, SUBJECT TO ROOT'S
EXPLICIT FREEZE RELEASE.** No patch was applied. Compilation/static/runtime
validation of the after-image remains unexecuted; this is neither admission nor
PVU005 resolution, new product finding or a claim of no possible bugs.

## Exact inputs and review method

- Source:
  `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/DesktopCurtainRenderingTest.kt`
- Original SHA-256:
  `6e5f8190e94008f3e3c41b62b3d1d75cdca471057e2de57497829d5c42191c57`
  (22441 bytes / 425 lines).
- Proposal: `B/reviews/team20/desktop_other_author/03-curtain-unapplied.patch.txt`.
- Patch SHA-256:
  `d277399bf24fa95abd2753315f257add2f30dbca6d18d7f5543bb0db0741d291`.
- Independently reconstructed **in-memory only** after-image SHA-256:
  `175feed3cd716d7881c48b3fcba906513a0e47f0599a7251e8de17f5b728e533`
  (25631 bytes / 524 lines).

Read all eight hunks against the complete original fixture and the production
window-protection/evidence context recorded in `01-BASELINE-SEMANTICS.md`.
Standard-library text comparisons independently verified exact original hunk
contexts, patch/after hashes and unchanged original source. No project/helper
code, compiler, analyzer or runtime was executed/imported.

The comparisons also verified:

- Render-content body identical modulo indentation after its private Composable
  extraction; cleanup try-blocks byte-identical.
- `capture`, `matchingSamples`, `await`, `hasHeavyCanvas`, `onEdt`, pixel/input
  data classes and remaining constant tail byte-identical.
- Same 36 assertion calls (23 true, 10 equals, 3 false) and the same multiplicities
  of all 21 `PVU005_*` diagnostic labels. Counts/labels are supporting data, not
  proof of oracle equivalence or new test executions; predicates were reviewed
  separately below.

## Independent semantic challenge

### Admission, state and rendering

All admission checks still run in the sole original JUnit method, before the
per-invocation `RenderingScenario` constructs its protection/state/window holder.
Original initialization order is preserved; missing opt-in still skips before
GUI creation, while bad admitted setup still fails. The new inner helper is
created once per invocation, not shared/singleton state. Its `run` retains the
scenario failure until its own finally cleanup/evidence attempts finish.

Window/LAF/geometry/render-API/heavyweight checks, real native listener wiring,
safe/unlocked marker palettes, FocusRequester, `LaunchedEffect(safe)`, real frame
wait, Button-role callbacks and callback-lock snapshots remain. The new private
Composable still reads the same mutable safe-content state within composition;
it does not move that read outside Compose or change the effect key. This is a
source compatibility assessment, not a compiled/re-rendered proof of the new
composition boundary. No production curtain or authentication path was changed.

### Ordered scenario and discriminating oracles

The named phases preserve:

1. Real window measure, all27 safe baseline hits, pointer/key1 each with
   `[false,false]`, then all27 unlocked-marker hits.
2. Real lock/iconification and zero unlocked hits, in-app/native restore with ACK
   withheld, actual restore/re-iconification counters and one security request.
3. A new iconified safe frame before direct ACK; post-ACK restore/geometry/pixels,
   logical lock and actual input captured **before** removal control.
4. Explicit synthetic unlock/removal, same-region geometry and safe pixels,
   native pointer/key1 each with `[false,false]`, then final suspect assertions.

`LockedRestoreObservation` stores already-captured immutable booleans/pixels/input
before removal. Combining `postAckLocked && afterInputLocked` at construction is
equivalent to the original later conjunction of those same boolean snapshots;
unlike the rejected tray-v1 flags, neither source is asynchronously mutated.
It does not resample lock state after removal. Setup/control failures and genuine
negative post-ACK observations therefore retain their original classification.

No partial-sample absence bug is reintroduced: pre-ACK absence is still exactly
zero sampled unlocked hits, positive controls still require all27. Post-ACK
native input remains conditional on safe pixels and valid geometry; a failed
condition does not become success by omitting the corresponding final assertion.

### Native input: early returns become sequential blocked stages

The separate `inputScopeFailure` retains the ordered short-circuit checks:
displayable/showing, active/non-iconified, exact client rectangle, unchanged
control, nonempty wholly contained target. In particular, no screen-location read
is moved before the showing check. The target is still a copied Rectangle.

| Counterexample / stage | Preserved result and remaining native work |
| --- | --- |
| Inactive after bounded await | Same immediate `before-pointer:NOT_ACTIVE`, no input |
| Missing target | `CONTROL_MISSING`; nullable target never passed to scope helper, no input |
| Scope changes before move | Same `before-move:*`, no move/press/key |
| Scope changes after move | Same `before-press:*`, no press/key, deltas0/0 |
| Scope changes after pointer group | Same `before-key:*`, retained pointer delta, key0, no key press |
| Scope changes after key group | Same `after-key:*`, retained pointer/key deltas |

No extra scope probe, wait or native group is introduced. Delta snapshots retain
their relative point before the subsequent scope check; callback snapshots are
still read when constructing the final observation. Every mouse/key release
remains in its original finally, including partial-press failure. The final
`worked` condition still requires null blockedAt and deltas1/1. This is not an
atomic display grab or a new guarantee against externally racing native state.

## Precise suppressions accepted

- `RenderingScenario.run` (candidate line103): its single `Throwable` catch keeps
  an assertion/error/interruption as primary, then rethrows after cleanup.
- `RenderingScenario.close` (candidate line354): original four catch sites retain
  independent protection cleanup, window disposal, cleanup-dispatch and trace
  write failures. Narrowing them to `Exception` would lose assertion/error
  retention. First/suppressed failure logic and final throw are unchanged.
- `setLookAndFeel` (candidate line151): preserves the existing optional-LAF
  `Exception`-to-status boundary, including possible runtime exceptions; selected
  LAF/status remain recorded and actual rendered pixels/input still determine
  the fixture result. It does not newly swallow `Throwable` or hide an opted-in
  setup failure behind a PASS return.

Each annotation names only `TooGenericExceptionCaught` on the exact boundary;
none excludes the test file/class, suppresses complexity wholesale, changes the
Detekt configuration/baseline or relaxes any guard. The original two complexity,
one length, six generic-catch and one return-count diagnostics account for this
lane's **10 diagnostics**, not ten tests or product defects.

## Minimum useful validation and preserved limits

After root reconciles the frozen source and independently admits a fresh
candidate, include only affected `:app-desktop:detekt` with the intended complete
Desktop source set in the focused static selection. That task does not establish
Kotlin/Compose compiler compatibility. If root needs a separate compile-only
qualification for the new private declarations, the bounded existing target is
`:app-desktop:compileTestKotlinDesktop` (declared `jvm("desktop")`, observed in
retained GUI02/integration02 prepare logs); it needs its own admitted prerequisite
graph and must not implicitly authorize a GUI/Test task. No execution authorized
by this report, no new permanent test, no all22 rerun.

Retain the prior GUI02 curtain PASS in its original exact-source scope and its
**not-reproduced on heavyweight SOFTWARE_FAST** qualification. It is not fresh
execution of this after-image, universal backend/platform clearance, no-flash,
real Unlock/coordinator/authentication or hardware proof. A successful unchanged
GUI endpoint need not be replayed merely for this static remediation. Historical
cleanup, STOP/NO-RETRY/CLOSED and original-lock obligations stay separate; C17
source/helpers/T remain frozen. Only this reviewer's report files were written.
