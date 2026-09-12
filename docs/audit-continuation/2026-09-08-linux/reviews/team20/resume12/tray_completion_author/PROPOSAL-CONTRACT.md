# PVA027 same-case native tray completion proposal

Status: inert source proposal; independent fixture review pending. Root alone may
integrate/admit/execute it. No new case, product/dependency change, build, test,
application launch, cleanup, passing result or closure is claimed here.

## Source identities

- Permanent target: `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`.
- Unchanged before SHA256: `1c7cf84fec50c6e161d2e3e282066c372a8950b0c6f2bd7c08b1fa423d769761`.
- Proposed after-image: `CredentialMainNavHostRoomIntegrationTest.kt.proposal.txt`;
  SHA256 `992cd7bf24fdb96f5d12c38ef105d50300bca161141230b4c177d4c85938eb6f`.
- Inert delta: `TRAY-COMPLETION.patch.txt`;
  SHA256 `69a5fe6e87aa760a00ba3bf7578e166f6e791257b5fd93f78775b5c14cd377d8`.

Rejected V1 after-image and patch are retained as
`CredentialMainNavHostRoomIntegrationTest.kt.rejected-v1.txt` (SHA256
`69ae5e56f2a2afbc72cae7404381e67a5f55dbb794d032373070366d34c01c77`) and
`TRAY-COMPLETION.rejected-v1.patch.txt` (SHA256
`748d969a92eba18fc016a1a03e88b8b6806b76198610da26532c090d8fa21101`).
Independent reviewer found that whole-display differencing can include live
Main hover/focus repaints and reject a real tooltip/menu before execution. V2
masks the stationary original Main frame only for readiness; it retains full,
unmasked bounded corner crops instead of a potentially truncated difference box.

## Risk and smallest useful extension

Retained INTEGRATION02 evidence already establishes EN→AR→EN native menu
rendering and Show callbacks. It does not establish displayed tooltips or real
Main Lock/Exit effects. Do not repeat that successful endpoint or substitute
counter-lambda callbacks for application behavior.

Extend only the already-unexecuted
`Pva027MainSettingsTrayPropagationIntegrationTest` case in GUI4. Preserve its
actual Main/Settings/publisher/installed-tray chain and Room seed/verify roles.
GUI4 stays four cases/four expected XML cases in two serial Test tasks.

- Retain five private-display PNG crops, at most 700×400 and 1 MiB each:
  `tray-tooltip-en1.png`, `tray-tooltip-ar.png`, `tray-tooltip-en2.png`,
  `tray-lock-selection.png`, `tray-exit-selection.png`.
- Use production-installed tray/menu/listeners with a temporary synthetic
  three-color image locator. Restore its original image and remove own observers;
  production owns native tray removal and application shutdown.
- Native Lock must deliver the exact owned MenuItem event. Observe unlocked real
  protection/session before the production listener and locked protection after
  that unchanged listener in the same EDT event dispatch; then await real Locked
  repository state and an iconified frame. This excludes focus-timer lock as a
  substitute cause without assuming that a recent focus gain resets its timer.
- Select native Exit while still locked. Restore own image/listener mutations
  before Enter. Record pre-production exact Exit event receipt; require original
  parent Main exit zero and fresh Room verification. Do not assume post-System.exit
  callbacks/finalizers execute. This PVA027 branch no longer uses Ctrl+Q.
- No Show/re-unlock, restored-curtain, preference-persistence, packaged-icon,
  other-backend or hardware claim. No old runner/helper replay.

## Ordered evidence delta

PVA027 main role changes from five to eleven events, in this exact order:

1. `PVA027_MAIN_TRAY_EN1`
2. `PVA027_TOOLTIP_EN1`
3. `PVA027_MAIN_TRAY_AR`
4. `PVA027_TOOLTIP_AR`
5. `PVA027_MAIN_TRAY_EN2`
6. `PVA027_TOOLTIP_EN2`
7. `PVA027_NATIVE_LOCK_DISPATCH`
8. `PVA027_MAIN_LOCKED`
9. `DRIVER_ASSERTIONS_COMPLETE`
10. `PVA027_EXIT_KEY_CALLS_COMPLETE`
11. `PVA027_NATIVE_EXIT_EVENT`

Seed two + main eleven + verify two = fifteen events, not fifteen tests.
Other GUI4 case counts/contracts remain unchanged. Existing 27 role files plus
five crops become 32 evidence files, not 32 cases. The GUI control author owns
updating all shared fixture hash cells, exact event/count contract, PNG
allowlisting/bounds/retention and visual-evidence qualifications independently.

## Required actual evidence / limitations

Pixel geometry is readiness only: independent visual inspection must confirm
English/Arabic tooltip text, Arabic shaping/clipping and selected Lock/Exit menu
items, with every expected menu label and the complete uncut tooltip visible.
The actual marker must be wholly within a 64-pixel screen corner, outside the
original-frame mask; a different location fails rather than moving Main or
falling back. Its fixed corner neighborhood is min(screen, 700×400), including
original overlay pixels over Main. This is a runtime eligibility gate, not a
claim about unobserved panel placement. Stationary-frame/readiness checks do not
prove full native bounds, visual correctness or absence of clipping. Event
identity and real state/parent settlement prove effects, not crop presence alone.
A wrong MenuItem, premature lock, observer failure, missing/visually incomplete
crop, bad ordering, nonzero/unknown exit or failed Room verification must reject
the corresponding claim.

The probe has a 60-second soft deadline and five-second bounded polling phases;
existing 150-second driver and 200-second parent bounds remain. Root's fresh
execution, private GUI/custody, whole-run bounds and hard-settlement/cleanup
admission remain mandatory. No existing GUI4 instance/source admission silently
covers this delta. Root must bind accepted fixture/control hashes together before
the one intended targeted validation cycle. No runtime is admitted by this note.
