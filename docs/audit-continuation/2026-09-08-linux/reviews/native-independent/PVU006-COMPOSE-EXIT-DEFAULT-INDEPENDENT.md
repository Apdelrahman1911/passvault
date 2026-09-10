# PVU-006 — independent Compose default-exit challenge

Reviewer: `/root/native_review`; source-contract finding author: `/root/editor_review`; 2026-09-10.

**ACCEPT the narrow source-level defect: normal successful Compose exit bypasses Main's application-owned
terminal finally/deadline. Runtime regression remains unexecuted.** This is distinct from the unproved
onboarding create→unlock interstage harm. Neither that hypothesis nor all of PVU-006 is resolved here.
Root owns grouping/ledger changes; this reviewer made no product change or closure claim.

## Independently inspected contract and reachability

- The retained, inert `Application.desktop.kt.txt` at
  `reviews/editor-independent/pvu006-compose-application-source/` has SHA-256
  `27fd8bbb1b7618be7579e08afbce349f67a08c9a31719f39ad93a0e532a3765e` (10,369 bytes).
  I read the source directly before reading the author's appended conclusions. Its lines 105–120 set
  `exitProcessOnExit: Boolean = true`, run the real `awaitApplication`, then call `exitProcess(0)`;
  line 40 imports `kotlin.system.exitProcess`. KDoc 59–61 explicitly says code after application will not
  run unless the flag is false. Lines 192–239 implement real composition/recomposer disposal.
- Current observed pre-patch `Main.kt:3,117–125` imports that API and omits the flag. A real enabled native
  window close calls `requestClose` (`PassVaultDesktopWindow.kt:130–136,172–180`); keyboard Quit, tray Exit
  and menu Quit are also wired to that handler (202,235,378). This does not depend on a fabricated event,
  onboarding race or an unsupported in-process runtime rebuild.
- `DesktopShutdownCoordinator.kt:48–60` starts cleanup then immediately invokes Compose's real
  `exitApplication` callback. Cleanup runs through `scope.launch` (89–121,155–170), bound to the separate
  `CoroutineScope(SupervisorJob() + Dispatchers.Default)` (`AppModule.kt:78`). These are not child jobs
  of Compose's awaitApplication. Compose's promise to finish its own effects/compositions is not an await
  of that independent scope's four tracked cleanup tasks.
- After successful Compose completion, default process exit precedes a return to `Main.kt:60`.
  Successful JVM process termination does not unwind the caller's ordinary try/finally stack. Thus
  `Main.kt:61–64` does not execute on this normal path: its `finishDesktopRuntime` latch wait, incomplete
  branch, and completed-path scope cancellation/stopKoin (128–140) are bypassed. This is an API-default
  misuse, not a Kotlin finally/compiler defect. The intended later `System.exit(exitCode)` at 30–31
  does not rescue work that an earlier successful process exit prevents reaching.

Dependency qualification: I independently read version pin 1.11.1 (`libs.versions.toml:7`), Desktop
`compose.desktop.currentOs`/MainKt configuration (`app-desktop/build.gradle.kts:1067,1085–1087`), and the
matching verification-metadata component at 9948–9955. The capture's final `RECEIPT.json`, SHA-256
`ccd2a3724275a514a4cfca5bed076c6e2c908bce9fa004d4da6ca6e441401a9d` (19,289 bytes), preserves its original
network/source capture plus the author's conclusions. It reports binary SHA-256
`442d4a2f0d95054453b68191304dca995267e72fcdcbbc0a63997bff4dcdf949`, matching checked metadata and root's
runtime identity. The sources JAR is a matching-coordinate source association, **not independently proven
source-to-binary correspondence**. I performed no remote lookup, bytecode inspection or dependency execution.

## Attempts to disprove or narrow the concern

1. **Cleanup is not universally absent.** Some/all launched tasks may finish before process termination.
   Real Compose disposal detaches the biometric window and cleans tray/window protection
   (`PassVaultDesktopWindow.kt:320–324`). Those are real mitigations, not an await of repository/database
   cleanup and not evidence that every provider completed.
2. **A concrete JVM hook exists.** A bounded Desktop/shared production source scan found and I read
   `DesktopClipboardService.kt:43–61,152–160`: the production constructor enables a shutdown hook that
   clears only a still-owned clipboard selection and releases its reference. `DesktopModule.kt:31`
   binds that public constructor. Therefore “clipboard cleanup never runs” is incorrect. This hook
   neither calls Main's finally nor joins the coordinator's four tasks; its success/latency is unobserved.
   Other runtime/dependency shutdown mechanisms were not exhaustively audited here.
3. **Exceptional paths differ.** If awaitApplication throws before exitProcess, Main can unwind into
   its finally. A policy that rejects System.exit by throwing also differs from successful exit. Neither
   disproves the reachable normal-exit bypass. Headless Main exits before startup and is not its witness.
4. **No downstream harm is manufactured.** Source proves the skipped owner-controlled wait/finalization,
   not actual database loss, an unpurged plaintext preview, permanent key retention or a hung process.
   The existing 2,500ms value bounds only the latch wait, not Compose disposal, synchronous prompt work,
   later stopKoin or JVM shutdown hooks. Restoring that path cannot prove a universal terminal deadline.

## Minimal patch recommendation and compatibility

Use the documented API setting on the **actual production call**:

```kotlin
application(exitProcessOnExit = false) {
    // existing production content unchanged
}
```

Keep the existing finally/coordinator and outer `main` System.exit authority. This preserves real Compose
close/disposal and the selected exit code while allowing the intended latch boundary to execute. The
coordinator's AtomicBoolean (79–86) makes the finally's second requestShutdown a no-op after requestClose,
so this does not require another ordinary lock, duplicate cleanup or a new JVM shutdown hook.

Normal close can now spend the existing cleanup wait interval; preserve its incomplete-report path and
conditional scope cancellation/stopKoin. Do not silently clear all singleton scopes on ordinary lock,
redesign restore/persistence semantics, change dependencies/identities, or claim this fixes early startup
exceptions outside the inner finally or existing native/disposal/physical-device limitations.

## Smallest useful real regression — recommendation, not admission

- Run the real pinned Compose entry in a **fresh child JVM** with an actually rendered synthetic window
  and controlled close, exercising the same production call/seam, not a test-local copy with false.
  A narrow production seam is reasonable if required; it must not duplicate coordinator logic.
- Parent-side evidence must distinguish real process exit from return: require a non-secret marker
  produced by the caller's finally and completion of its bounded cleanup ordering, then externally
  observe child termination. The old production default must fail that marker oracle. Exit code 0
  alone would accept the original bug. A finite synthetic cleanup marker is useful control-flow evidence,
  not a real Room/provider cleanup result or the old onboarding witness.
- **Do not trap System.exit by throwing SecurityException or substitute a fake exit callback.** Throwing
  unwinds finally and tests the opposite behavior from successful process termination. Nor can a
  headless pre-start exit or a helper the production Main never uses establish this regression.
- Existing `DesktopShutdownCoordinatorTest.kt:19–51,90–119,163–181` exercises callback operations and
  virtual-scheduler settlement, not actual Compose/process exit; it cannot detect leaving Main unchanged.
  Keep its once-only/failure controls, but do not label them a new real lifecycle regression.
- Root must separately admit the exact source, isolated synthetic storage/display, resource/time bounds,
  parent/child settlement and allowlisted cleanup before running. No old helper or closed scope is
  reopened by this recommendation. Linux can establish this common JVM/API boundary; platform-native,
  packaged and genuine hardware requirements remain separate.

## Observed source identities and authority

These SHA-256 values bind pre-patch bytes read in the ordinary editing workspace, not a W HEAD/tree or
whole-worktree claim. Other authors may subsequently implement the fix; root owns publication binding.

```text
381ef53b5bc4ae775aa2322dbaea02588d499111d2a32e2ca3cdb2557363acde  app-desktop/build.gradle.kts
7156e03eb7e5d2ea7636a9320a49aeaf15d0694522d88f78836b075c74e9043d  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopShutdownCoordinator.kt
9457f71fefe63de14ddd80d91b14b5b281175ee7c9bcdb113ac6a8ae15452616  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/Main.kt
794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/PassVaultDesktopWindow.kt
7b70040fe488c9825090548b3d8ca4a2e4f34c1989b072d2571d74c9647cd253  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/di/DesktopModule.kt
e689fbba229f372e128d3525cc03f85ff7682d955d191d1f74c915b692193550  app-desktop/src/desktopTest/kotlin/com/passvault/desktop/DesktopShutdownCoordinatorTest.kt
0e5d97c6bd182e5e732fc68b5861f267305e05b1d51a1378a57406c849e08c4a  core/security/src/desktopMain/kotlin/com/passvault/desktop/security/DesktopClipboardService.kt
49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a  gradle/libs.versions.toml
21ab6f9c2873325558f5bb96c7e7f68bfbb674e2a7ce867ffe19dabdae60c299  gradle/verification-metadata.xml
9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25  shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt
```

Source/report reads and one exclusive permanent review write only. Product/test edits, builds/tests,
actual cases and executions by this reviewer: **0**. No Git/network/process probes, library/helper imports,
old-store access, caches, archives, temporary runtime or background workers were created. Wrapper stop
and generated-output cleanup are not applicable; this does not discharge any prior HOLD. PVU-007 STOP,
PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED, Windows05 filesystem HOLD and all other
recorded fences/PVD/hardware qualifications remain. No frozen C12 note was edited.
