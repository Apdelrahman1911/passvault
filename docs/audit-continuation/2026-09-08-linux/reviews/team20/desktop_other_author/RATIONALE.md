# Remaining Desktop fixtures — unapplied proposals

Author `/root/desktop_other_author`, 2026-09-11.

**SOURCE PROPOSALS ONLY.** C17 application/tests/helpers and T remain untouched.
No compile, Detekt, application test, helper execution/import, Git, CI or
SDK/runtime/process probe occurred in this lane. Root's explicit source-only
resume after GUI03 preparation exit70/zero cases did not release the freeze.

`SOURCE_HASHES.json` binds the originals, exact active patches and proposed
after-images. `SOURCE_HASHES.partial.json` is superseded interim metadata.
Active proposals are **01-lifecycle**, **02-tray-v2**, and **03-curtain**;
**never apply 02-tray v1**. All are `.patch.txt`, not applied source.

## Bounded diagnostic mapping

Retained authority is `runs/linux-detekt03/reports/app-desktop-checkstyle.xml`
under B. These are 21 of app-desktop's 47 static diagnostics, not new product
families or test cases. The other 26 belong to the excluded Main/NavHost fixture.

| Fixture / original locations | Proposal |
| --- | --- |
| Lifecycle: LongMethod72 | Extract identical environment/XDG setup, called at the same point before ProcessBuilder construction. |
| Lifecycle: ThrowingExceptionFromFinally138 | Precisely justify the existing primary-aware terminal boundary; leave its finally, captured child, waits, exit evidence and interruption logic intact. |
| Tray: LongMethod175 | Extract selected-popup screenshot retention, preserving image-finally ownership and original-channel force before stream close. |
| Tray: CyclomaticComplexMethod337 | Extract native-owner cleanup and trace-writing steps; keep first/suppressed error and interruption ownership in `close`. |
| Tray: ReturnCount272 | Equivalent density/proximity conjunction after the unchanged null/geometry guard; proximity stays short-circuited. |
| Tray: generic catches101/320/321/342 and finally throw323 | Precisely annotated primary/cleanup boundaries; no catch narrowing or swallowed failures. |
| Tray: generic catch465 | Completion-flag finally cancels the exact FutureTask only on unsuccessful get, still without interrupting running native work. |
| Curtain: LongMethod65 / complexity65 | One admitted invocation owns a private scenario; named setup, baseline, deferred restore, ACK, removal-control and final-oracle phases. |
| Curtain: complexity306 / ReturnCount306 | Extract the identical scope predicate; sequential `blockedAt` stages stop later native actions without changing failure observations. |
| Curtain: generic catches99/276/285/286/291/299 | Precise optional-LAF status, primary-failure and independent-cleanup boundary annotations, with the same catch types and retention. |

## Preserved behavior and evidence

- Lifecycle still invokes the real default/production Compose application
  boundary in two child modes. Default exit0 versus production exit23 and exact
  event ordering remain discriminating. The entire child-probe suffix is
  byte-identical. Environment directory creation occurs at its original point.
- Native tray still uses the public production constructor and exact icon,
  popup and item identities; EN→AR→EN resources, observed right-click before
  Down/Enter, fresh stage callbacks and no-extra-action assertions are unchanged.
  A release is registered before native press, attempted in finally even after
  partial failure, and retained for close when release fails. Marker flushing
  still requires observed native removal, not merely a cleanup method return.
- Tray `NativeCleanupState` is live through late EDT completion. Observation
  reads remain after ImageIO and all Locale categories are restored. Root still
  owns settlement of timed-out native calls; no new visibility/settlement
  guarantee is claimed.
- Curtain state is created only after the original opt-in and fresh-evidence
  checks. The same real Compose content/callback body is extracted into a
  composable; native window attachment, frame signal, focus and show order remain.
  Immutable window/robot/rectangle references and already-captured post-ACK
  observations are passed between phases; no live result becomes an early
  snapshot. All post-ACK observations precede the explicit unlock/removal control.
- Curtain retains 27/27 positive pixel matches, zero pre-ACK unlocked hits,
  actual native iconify/restore counters, withheld ACK until iconified frame
  progress, exact geometry, and pointer/key deltas1 with callback-lock snapshots
  `[false,false]` / `[true,true]` / `[false,false]`. No callback is invoked directly.
- Input still checks activity before resolving its target, then current scope
  before move, before press, before key and after key. Missing/changed scope
  remains a named observation. Once blocked, later native groups do not run.
  The showing guard still precedes `locationOnScreen`; input releases stay in
  their original finally blocks. Capture/sampling/wait/EDT support is unchanged.
- Curtain separately attempts protection cleanup, window disposal and trace
  writing, preserving the first error and suppressed later errors before
  rethrow. The trace's pre-write outcome remains distinct from write/XML/worker
  success. No new test or suppressed assertion was added.

## Precise exception-rule challenges

No file/class suppression, baseline/config exclusion, disabled check or broad
exception-swallowing helper is proposed. Lifecycle `runChild` adds only the
finally rule to its existing generic-catch annotation: its cleanup-only error
must fail, whereas a primary assertion/interruption keeps priority. Tray's
single test and local `release` retain all error types for cleanup; `nativeInput`
retains its existing partial-press/finally behavior with both exact rules named.
Curtain's `run` and `close` retain assertions/errors/interruption across cleanup;
its small optional-LAF helper records any `Exception` just as before, while
selected-LAF and real renderer observations remain authoritative. These are
intentional test boundaries, not policy waivers for ordinary production code.

## Independent review and retained rejection

The paired reviewer accepts exact lifecycle and tray-v2 proposals in
`../desktop_other_review/02-LIFECYCLE-TRAY-PATCH-REVIEW.md`, SHA256
`873af272cddf06cb1b2e9fff3e24c74cf7bf9c28722391898e5709ef98608f49`.
Curtain exact-patch acceptance is sealed in
`../desktop_other_review/03-CURTAIN-PATCH-REVIEW.md`, SHA256
`da2c6d63072a5abef46bd9531f1c5d13de1e06048a375f3370139122f13c4fcc`.
All three acceptances are source-only and subject to root freeze release.

Tray v1 returned a `Pair` immediately after native cleanup. The reviewer found
that a timed-out EDT call could finish during later default restoration and
update the original captured flags; v1's early snapshot could change trace
evidence. V2 preserves live state and original read timing. V1 bytes and hashes
remain explicitly rejected/superseded, not an executed defect or new PVA family.

An author formatting-only data edit stopped before any write because substring
matching counted two differently indented rectangle lines (Python assertion
failure). A corrected whole-line mapping completed; this was proposal-text
preparation, not project/helper validation. No candidate or application result
was inferred from that failed edit.

## Verification limit and smallest next step

`TEXT_COMPARISONS.json` records in-memory hunk reconstruction, source-hash
preservation, unchanged assertion/message spelling counts and test annotations,
and candidate line lengths. Those checks are not Kotlin parsing, Detekt or
semantic proof. All three fixture source files still match the recorded originals.

Only root may release the freeze, integrate independently accepted exact paths
and admit subsequent work. The smallest prospective static scope is affected
`:app-desktop:detekt`, with its intended full Desktop source set and existing
JDK17/wrapper/serial/strict-verification policy. Static analysis does not prove
new Composable declaration compatibility. If root requires a separate minimal
compile-only qualification, the reviewer identifies
`:app-desktop:compileTestKotlinDesktop`, with its own exact prerequisite graph
and independent admission; no GUI/Test invocation is implied.
Do not rerun already-supported
lifecycle/tray/curtain endpoints merely for lint, or relabel their old XML as
execution of these after-images. A concrete new incompatibility must be scoped
separately. All STOP/NO-RETRY/CLOSED and held-runtime boundaries remain intact;
physical/authentication/no-flash/Settings/tooltip qualifications are unchanged.
