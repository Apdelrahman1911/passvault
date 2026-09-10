# Desktop application lifecycle boundary — source implementation note

Author: /root/editor; 2026-09-10. Root owns grouping, ledger, execution and publication.

Current source tuple update: the independent-review corrections at the end supersede only the original
test pin below; Main/helper remain unchanged. The current test is 422cbfd7... (9659 bytes / 201 LF).

**Implemented; independent patch/test review requested; not compiled or executed.** This is root's narrow
PVA-039 normal-exit bypass, discovered during PVU-006 investigation. It neither resolves all PVU-006 nor
establishes the original onboarding create-to-unlock harm. No closure credit is requested.

## Basis and bounded product change

The pre-patch independent review at reviews/native-independent/PVU006-COMPOSE-EXIT-DEFAULT-INDEPENDENT.md
(relative to this continuation directory) accepted the reachable API-default problem:
SHA-256 01e681a426e0e868bdea6f4efa19a400bb6c1d4d4c66c57ec594f2381c9a5579, 10,295 bytes.
It challenged normal/exceptional exit, Compose disposal, the separate security scope, once-only shutdown
and the production clipboard shutdown hook.

Retained inert source under reviews/editor-independent/pvu006-compose-application-source/:
desktopMain__androidx__compose__ui__window__Application.desktop.kt.txt has SHA-256
27fd8bbb1b7618be7579e08afbce349f67a08c9a31719f39ad93a0e532a3765e (10,369 bytes);
the final RECEIPT.json has SHA-256 ccd2a3724275a514a4cfca5bed076c6e2c908bce9fa004d4da6ca6e441401a9d
(19,289 bytes). Source lines 59–61 document false for caller continuation; 105–120 implement the
default-true real process exit after awaitApplication. A matching 1.11.1 source coordinate is an association,
not independently established source-to-binary equivalence. This lane did not load that dependency.

The only Main delta removes its application import and routes its one production call through
runDesktopApplicationLoop. That new ten-line helper calls real Compose
application(exitProcessOnExit = false, content = content), without copying coordinator logic or injecting
fake exit behavior. A data-only inverse of these two Main changes matches the pre-edit SHA-256
9457f71fefe63de14ddd80d91b14b5b281175ee7c9bcdb113ac6a8ae15452616.

Preserved guards and compatibility:

- Native close still enters requestClose, secures/hides the window, launches coordinator cleanup, then
  calls real Compose exitApplication. Real Compose composition/effect disposal is retained.
- Caller return permits the existing Main finally/requestShutdown/awaitCleanup path to run.
  The existing AtomicBoolean prevents duplicate cleanup after a normal close.
- The 2,500ms timeout, failure/incomplete reports, conditional scope cancel/stopKoin, owned instance-lock
  scope and outer Main System.exit(exitCode) are unchanged. Normal exit can now spend the existing wait.
- That timeout bounds only the coordinator latch, not synchronous prompt cancellation, Compose/native
  disposal, stopKoin or JVM hooks. This is not a universal whole-process deadline.
- Old-behavior cleanup may partly or fully finish before exit. Compose disposes native bindings;
  production DesktopClipboardService separately registers a still-owned-selection shutdown hook.
  “No cleanup occurs” and “clipboard cleanup never runs” would be false.
- No schema, persistence/recovery semantics, singleton lock policy, startup-exception scope, dependencies,
  product identity/version, platform selection or PVD decision is changed. No data loss or real provider
  completion has been demonstrated here.

## Frozen author tuple

Paths are relative to the ordinary editing workspace; these are not whole-tree or Git identities.
Root must independently bind publication source. Native review was sent this tuple.

| File | SHA-256 | Bytes / LF |
| --- | --- | --- |
| app-desktop/src/desktopMain/kotlin/com/passvault/desktop/Main.kt | 9a67a8a5c2f05331e91ec5c18f6317a3562bd26e2c720105a8a21773edd8ae93 | 7614 / 253 |
| app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopApplicationLoop.kt | fc8c553ee02f6c84fc535ea0672436b9688dde6753101f9d952858a05516bfac | 402 / 10 |
| app-desktop/src/desktopTest/kotlin/com/passvault/desktop/DesktopApplicationLifecycleIntegrationTest.kt | ad91994b7c96c948b7289c7d3c7b009ed896926cc8a54ea6308780bfdaa92d3f | 8877 / 189 |

The new helper/test were created private (0600); intended publication mode is ordinary Git 100644.
Main's original filesystem mode is preserved. No generated mirror or frozen GUI02/C12 record was edited.

## Regression oracle — prospective, not execution evidence

One declared JUnit method:
DesktopApplicationLifecycleIntegrationTest.composeExitReturnsThroughCleanupBeforeOwningJvmExit.

A complete passing case runs two **serial fresh child JVMs**, not two Gradle jobs; early failure can prevent
the second launch. Each creates the same real Compose Window with synthetic BasicText, awaits two frame
callbacks, requires a showing/displayable native peer, force-writes WINDOW_READY, and dispatches actual
AWT WINDOW_CLOSING through the event queue. Real onCloseRequest records CLOSE_REQUEST and invokes actual
ApplicationScope.exitApplication.

| Mode | Actual entry | Required exit | Required ordered events |
| --- | --- | --- | --- |
| default | Dependency application(content = content), default true | 0 | WINDOW_READY, CLOSE_REQUEST |
| production | Production runDesktopApplicationLoop(content) | 23 | WINDOW_READY, CLOSE_REQUEST, CALLER_CLEANUP, CONTINUATION |

Child caller finally records CALLER_CLEANUP; ordinary continuation records CONTINUATION and invokes real
exitProcess(23). Event writes use a no-follow append FileChannel and force(true). Parent requires the real
exit code and exact event order, not merely exit zero. Default is an executable old-behavior control.
Reverting the actual helper to default true would lose the production markers and change its exit code.

No fake Compose, mocked exit callback, SecurityManager exception trap or Gradle-worker termination is used.
No Koin, vault, DB, provider, personal clipboard, tester data or signing material is opened. This exercises
the production loop seam, **not full Main**. The finite synthetic finally marker establishes return/ordering,
not completion of the coordinator's real providers or its 2,500ms wait. Frame/peer checks are not pixel,
screenshot, packaged, Apple/Windows or hardware evidence. Existing virtual-scheduler coordinator tests
remain separate and alone cannot detect this API misuse.

## Fresh root-only execution prerequisites

This note is **not execution admission**. Root must independently review exact source/tool/runtime,
coordination, synthetic display, cleanup and publication bindings before any invocation.

Required injected properties:

- passvault.lifecycle.syntheticDisplay: admitted synthetic X11 DISPLAY (:N or :N.0), matching environment;
  no nonblank WAYLAND_DISPLAY, non-headless JDK 17.
- passvault.lifecycle.runtimeDir: fresh empty absolute normalized private directory in removable R.
- passvault.lifecycle.evidenceDir: distinct/nonoverlapping fresh empty absolute normalized retained E.
- passvault.lifecycle.childClasspath: the actual complete Gradle Test runtime classpath.
  There is no java.class.path fallback; the Gradle worker-launcher classpath is insufficient.

An absolute regular no-follow XAUTHORITY file from the admitted synthetic session is also required.
Missing syntheticDisplay is an explicit JUnit assumption skip with **zero verification credit**. After
opt-in, bad/missing setup fails. This fixture never launches a display session.

Each child uses the current JDK's absolute bin/java, Xmx256m, one active processor and disabled perfdata.
It has its own home/tmp/XDG cache/config/data/state under R, plus private JNA/SQLite tmp properties.
The child environment is cleared then replaced with synthetic display/auth, private storage, fixed PATH,
locale/timezone. Production Linux Java2D properties are retained. Root must verify actual tool/runtime
and X identities; this lane has not probed them.

The post-start child deadline is 30s, with 100ms wait/log polls; final captured-child settlement allows 5s.
Log limit is a cooperative 256KiB and final trace limit 4096 bytes. Polling can overshoot; ProcessBuilder.start
is outside the post-start deadline. Fresh outer wall/RAM/disk/process limits remain necessary.
Parent finally forcibly terminates only its captured live child when needed, awaits settlement, writes
the observed exit, and restores its entering interruption flag. A new interrupt, failed child start,
failed evidence write or final-wait failure means failure/partial evidence, not proven settlement.
Descendant/runtime settlement remains root's outer responsibility.

A complete case retains exactly six compact files:
default.events, default.log, default.exit, production.events, production.log, production.exit.
Decimal exit files are written after captured-child settlement. Failed/early attempts may leave a subset;
absence is not success. Review bounded synthetic diagnostics before publication. Retain case XML and exact
source hashes; do not upload application binaries, archives or private runtime directories.

No recursive remover exists in this fixture. Root must install independently admitted outer cleanup first,
use the checked-in JDK17 wrapper, one worker/non-daemon/no configure-on-demand with dependency verification
intact, enforce one audit-owned local/CI job, preserve evidence, run the original wrapper's --stop, verify
owned settlement, then remove only validated allowlisted generated/R outputs. New macOS/Windows work needs
separate admission; this Linux X setup proves no genuine device behavior. Old helpers/runners stay closed.

## Authoring diagnostics and execution limits

- First source-writer request failed in JavaScript before nested tool/file invocation:
  ReferenceError: kind is not defined (Kotlin interpolation in a JS template). Corrected writing succeeded.
- Earlier guessed DesktopSessionCurtainRenderingTest.kt and root-directory *RenderingTest.kt glob returned
  absence. Actual security/DesktopCurtainRenderingTest.kt was subsequently read. These were not tests.
- First note preparation stopped on full before/after os.stat_result equality for the new test, before note
  creation. The differing field was not retained; no cause is proved. Full-stat equality included atime,
  unsuitable for immutable-byte comparison. Later bounded reads used dev/ino/uid/mode/nlink/size/mtime_ns/
  ctime_ns plus exact bytes/hashes successfully.
- A subsequent note-writer request failed in the JS parser, “Unexpected identifier 'reviews'”, before any
  nested tool/file operation, due unescaped Markdown backticks in its template. This successful writer
  serializes the note separately. Neither authoring failure is an application/runtime test result.
- Reversing only known test formatting wraps and the new .exit evidence additions matched initial SHA-256
  7e6c90c17dad478399b7891f7e60f2d129f147124a4f9b9d3ba8ceb266945502. One @Test, 189 LF and lines <=120
  were checked as data only. The exact Main inverse was checked during this successful note creation.

Bounded source/data reads, assigned edits and this permanent note only. Declared methods: **1**;
prospective complete-case children: **2**; actual cases/builds/executions in this lane: **0**.
No subject import, AST/syntax probe, compile, build, test, Git, network, host/runtime/cache probe or execution
admission occurred. No runtime/cache/build artifact, daemon or background worker was created; no wrapper
stop or runtime deletion arises from this source-only task. Root retains host disk/RAM supervision.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029 recorded failure/no automatic retry, G7/G8 CLOSED, all consumed/HOLD
scopes and PVD/product/hardware boundaries are preserved. PVA-027's separate Settings-to-native-tray work
is unfinished and not silently replaced by this lifecycle task.

## Independent patch-review corrections — current source tuple

/root/native_review requested two concrete corrections after reading the original three source files.
Both are implemented for independent re-review, not executed:

1. Retain the original Throwable from child launch/wait/assertion, rethrow it unchanged, and attach a
   cleanup failure as a suppressed IllegalStateException with its real cause and explicit runtime-cleanup
   BLOCKED context. With no primary failure, that cleanup report is thrown. InterruptedException from
   either primary work or cleanup restores the current thread's flag; the entering cleanup flag is still
   restored in finally. Failure evidence is no longer replaced by a later wait or exit-file failure.
   The narrow TooGenericExceptionCaught suppression permits assertion preservation, not swallowing.
2. Add -XX:-CreateCoredumpOnCrash to each child and route HotSpot -XX:ErrorFile to that child's private
   owned home/hs_err_pid%p.log. No such file exists from this unexecuted work. These child controls do not
   replace admission of the parent JVM, whole-runtime disk limits or outer crash/abrupt-parent cleanup.
   Root must preserve compact necessary failure diagnostics before validated R cleanup, if any occur.

**Current test**:
app-desktop/src/desktopTest/kotlin/com/passvault/desktop/DesktopApplicationLifecycleIntegrationTest.kt
SHA-256 422cbfd7b85a08585b99519b94f41296ed24ea1dce133f47f4fdab2ddf5a85b9, 9,659 bytes / 201 LF.
Its original ad91994b... source tuple above is retained as authoring history, not the current execution pin.
Main remains 9a67a8a5c2f05331e91ec5c18f6317a3562bd26e2c720105a8a21773edd8ae93 (7614 / 253);
DesktopApplicationLoop remains fc8c553ee02f6c84fc535ea0672436b9688dde6753101f9d952858a05516bfac (402 / 10).
Source-only data checks still find one @Test and lines <=120; no Kotlin syntax/compiler or runtime claim.
One prospective complete case / maximum two serial child modes / zero observed executions remains.
