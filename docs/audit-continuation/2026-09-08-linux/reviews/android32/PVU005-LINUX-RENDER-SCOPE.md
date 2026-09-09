# PVU-005: Linux rendering scope and permanent fixture

Author `/root/android32`, 2026-09-09. **SOURCE ONLY; NOT COMPILED, RUN OR ADMITTED.**
Reference product C4 `da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`. One additional permanent test declaration,
not a product patch or confirmed finding. Independent challenge: `/root/editor`.

## Decision and existing outcome

**No external Linux display/tool-install block identified.** Fresh display/worker/
cleanup admission and actual runtime viability remain pending. Android image
licensing does not block this separate slice. Do not delay or silently broaden
root's fixed current167 cycle for it.

The preserved outcome establishes logical opaque-curtain retention after secured
ACK, not visible obstruction or an authentication bypass. Current protection
still installs on lock and removes on already-Unlocked/cleanup; its ACK only
allows restore. The existing JFrame property case returns early headless and
does not inspect actual Compose/Skia pixels. The historical independent review
traced Compose1.11.1's heavyweight SkiaSurface, not alternate SwingGraphics.
No new production defect is claimed by this proposal.

## Read-only local metadata, 07:08:33 UTC

- Host x86_64; this agent's DISPLAY/WAYLAND_DISPLAY/XAUTHORITY were unset. That
  is **not** authority to adopt any existing display or proof no server exists.
- `/usr/bin/Xvfb` (2,064,864B), `/usr/bin/xfwm4` (416,432B), `xauth`, `xdpyinfo`,
  `xwininfo`, `xprop` are present. No executable/version/help invocation ran.
- Selected `/usr/lib/x86_64-linux-gnu` paths exist for X11/Xext/Xi/Xrender/Xtst,
  GL/EGL/GLX-Mesa and Mesa's software DRI library; DejaVuSans exists. File presence
  does not prove successful linking, a renderer or a WM's iconification behavior.
- `/usr/lib/jvm/java-17-openjdk-amd64/release` records Ubuntu17.0.20/Linux/x86_64;
  no JVM was launched. Root must select/hash its actual JDK/tool executables.
- No network request, X connection, screen capture, private state inspection,
  app process or dependency/SDK/tool installation was performed.

## Fixture and meaningful oracle

`app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/DesktopCurtainRenderingTest.kt`
uses existing `kotlin.test`/JUnit4, actual ComposeWindow and production
DesktopWindowProtection. It never starts Main/Koin, a repository, clipboard,
biometric provider or external helper. A synthetic focusable Foundation
clickable Button-role control and BasicText increment only an in-memory counter;
this is not Material3 or the production Unlock screen. No dependency was added.
Native callbacks also record the protection lock flag; they cannot authenticate
or open a vault.

One case, ordered phases (not five tests):

1. On the fresh display, show a640×480 window with distinctive safe pixel pattern
   and real Button. Require heavyweight Canvas, actual selected render API/
   Compose-native handle, client geometry, baseline cropped Robot pixels and
   native mouse + Space activation.
2. Replace only the content with a distinctive synthetic unlocked pattern; prove
   it is visible. Call production lock. Withhold secured ACK, request the in-app
   restore and then set the real window's extendedState=NORMAL. Observe native
   WindowStateEvents, re-iconification and settled marker absence. No fabricated
   WindowEvent or internal cross-module restore flag is used.
3. Replace content with the same safe surface. Await a real Compose frame-clock
   signal, then call onVaultContentSecured. **The frame callback precedes screen
   presentation and is not pixel evidence.** An iconified-frame timeout is named
   SETUP/ACK-progress failure; never restore early to bypass it. This synthetic
   signal is not the production repository/scrubbing/guarded-route/epoch ACK.
4. Before any unlock(), freeze native restore/geometry, safe pixel observation,
   native mouse/key results, their actual callback lock snapshots and independent
   before/after isLocked observations. A failed
   pixel/input observation is retained, not immediately lost to an assertion.
5. On the **same window, safe content and geometry**, call unlock as an explicit
   synthetic already-Unlocked control. Recheck pixels/input, then assert the
   earlier postACK result. A failed comparator/native restore/setup cannot be
   labelled a proved curtain obstruction. Finite settled sampling is not proof
   of zero transient exposure; other OS/render APIs and full app/auth flow remain.

Main's Linux Java2D/AA properties must be supplied before worker AWT startup;
fixture mirrors its GTK-then-system-LAF attempts and records fallback outcomes.
No renderer override, software-forced substitution, offscreen Compose snapshot,
JFrame paint call or whole-screen capture is used. Only27 fixed palette positions are asserted, not all screen/text pixels;
independent visual review of the five client-rectangle PNGs is still needed.
While iconified, that same rectangle can show the private synthetic display
background, never an ambient desktop. PNGs and compact observations use
exclusive writes into an already-empty,
root-created evidence directory; ImageIO uses bounded-size in-memory buffering.
Teardown separately attempts protection cleanup and ComposeWindow.dispose on
EDT and preserves errors. Root must handle native/EDT hangs and worker settlement.

## Compile reuse and next root-owned action

Prospective task/filter, **not a runnable admission**:
` :app-desktop:desktopTest --tests com.passvault.desktop.security.DesktopCurtainRenderingTest `.

The current167 selection already compiles shared/core/feature dependencies.
This fixture can reuse those imminent-needed outputs/private dependency cache,
but adds app-desktop main/test compilation and its existing
`compose.desktop.currentOs` Linux Skiko runtime. `shared:desktopTest` has no
explicit currentOs declaration; do not assume its headless composition tests
prove the actual runtime. Existing app desktopTest has Kotlin test and inherited
Compose/JNA; no new dependency or generic framework is needed. Source wiring
sets nativeBiometricPlatform=null on Linux, so the guarded native biometric
CMake/CTest edges are absent; packaging/publishing tasks are not selected.
Actual realized graph/artifact viability still require root observation.

Root must separately approve source/test changes, sole slot, fresh private
Xvfb+WM session (e.g.1024×768×24, no TCP/ambient X authority or D-Bus session),
fresh HOME/XDG/JNA/TMP and a new worker/evidence directory. A WM may create session
helpers: own/settle those too, never stop an unrelated server. Required worker
properties are `passvault.pvu005.syntheticDisplay` matching DISPLAY,
`passvault.pvu005.evidenceDir`, `sun.java2d.xrender=true`,
`awt.useSystemAAFontSettings=on`, `swing.aatext=true`, with actual non-headless
JDK17. Missing opt-in is XML SKIP; opted-in mismatch/headless/setup is FAIL,
not an early-return PASS. Bind exact Compose1.11.1/Skiko0.144.6 resolved JAR/native
hashes and observed API, not only declared versions or system properties.

Proposal for admission review: one filtered case,2min outer worker ceiling,
separately bounded build/setup/cleanup, <=12MiB PNG/text retention plus compact
XML/logs; inherited host resource floors/serial/strict wrapper policy still
apply. Wrapper --stop and owned display/WM/worker cleanup must be installed
before launch. Share outputs only if root explicitly schedules this next;
otherwise perform normal allowlisted cleanup, not indefinite speculative cache
retention. Source success or even this one Linux rendering pass changes no
closure denominator without independent exact evidence reconciliation.

## Narrow independent-review corrections (source only)

Exact rejected v1 before-images are retained as inert text under
`reviews/android32/pvu005-before-images/`:

- `DesktopCurtainRenderingTest-5d032a61.kt.txt` SHA-256
  `5d032a613ea5174ecdd4cebebac0b3b853cc6d9fd232e1b459103b6116b8f8cf`.
- `PVU005-LINUX-RENDER-SCOPE-42ada317.md.txt` SHA-256
  `42ada31792b82edceea6aaa18113a9461003e271271c328f2e2cc713d64a440b`.

Editor independently found F01 (negated full-pattern match admitted partial
marker exposure) and F02 (removal input could use stale client geometry).
These are fixture defects, **not new product findings or executed failures**.
The current revision counts each of27 palette samples and requires ZERO unlocked
hits preACK; safe/full marker checks require all27. Comparator input is gated
by current geometry/pixels; the input path rechecks displayable/showing/active/
uniconified window, exact current client rectangle and contained unchanged
control before pointer move/press and key emission. Mismatch blocks further
input and records the reason; it is not called curtain obstruction. This is not
an atomic native-display grab; fresh isolated-display admission remains required.

Also records actual callback lock snapshots; names the inner status
`preEvidenceWriteOutcome` (XML/worker/cleanup remain authoritative); uses typed
ComposeWindow renderApi/windowHandle so future compilation checks their public
shape rather than hiding that gap behind reflection. Their exact pinned API
shape is still **uncompiled here**. Material3 compile export was unestablished;
the revision uses already-declared Foundation, without dependency edits, instead
of asserting an inherited Material3 API. V2 awaits the independent delta vote;
no control framework, product patch, build or GUI operation was added.

## Input binding

Whole-file SHA-256 below identifies bytes, not whole-file semantic review;
production inspection was focused on the previously identified curtain/caller/
ACK/build intervals. The new fixture alone was authored in this lane.

| Input | SHA-256 |
| --- | --- |
| `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/DesktopCurtainRenderingTest.kt` | `6e5f8190e94008f3e3c41b62b3d1d75cdca471057e2de57497829d5c42191c57` |
| `core/security/src/desktopMain/kotlin/com/passvault/desktop/security/DesktopWindowProtection.kt` | `92c473e21c262972983b111b61adc18610bc17fcea92c6cc2b7f65ea4d5ee128` |
| `core/security/src/desktopTest/kotlin/com/passvault/desktop/security/DesktopWindowProtectionTest.kt` | `1c3d957eb1d7c46337db682b1829f75f5ef8c66e43fb43be3d012f8895515501` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/PassVaultDesktopWindow.kt` | `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopSessionCleanup.kt` | `865184fb667e705f43b4d8ab29f5b0b04024d0abd7c0d664aed09d5b439dc5d3` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/Main.kt` | `9457f71fefe63de14ddd80d91b14b5b281175ee7c9bcdb113ac6a8ae15452616` |
| `shared/src/commonMain/kotlin/com/passvault/shared/navigation/PassVaultNavigationHost.kt` | `50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006` |
| `app-desktop/build.gradle.kts` | `381ef53b5bc4ae775aa2322dbaea02588d499111d2a32e2ca3cdb2557363acde` |
| `shared/build.gradle.kts` | `826cc234a59c05efdbbcd6a0ea62f25362f0a54d588e996582903f44d09949ad` |
| `gradle/libs.versions.toml` | `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a` |
| `docs/audit-handoff/current/unresolved-investigations-outcome-only.json` | `420b7eb1a419aabec37cbc323bc7d9a9b32ca160395dcc2d5f109488820eed19` |
| `docs/audit-continuation/2026-09-08-linux/reviews/build-config/CURRENT-SOURCE-REGRESSION-BATCHING.md` | `925099337dd71eb5fe7b19bb5ef39dc2ce60051820751575c8a994f64bb7aa19` |

Source-only preparation created this report and the permanent fixture, no
runtime PNGs/logs/cache/build output or background worker. All prior STOP,
NO-RETRY/CLOSED and publication restrictions remain unchanged.
