# Independent native-tray fixture source review

Reviewer `/root`; author `/root/editor_review`; 2026-09-10.
**ACCEPT THE BOUNDED ONE-CASE SOURCE; NO COMPILE, NATIVE EXECUTION OR CLOSURE.**

Fixture: `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/tray/DesktopSystemTrayNativeIntegrationTest.kt`,
SHA256 `0a70e40b519ec22e33a075467ec1961a40e8c9c091c43d53c31915e818f1baec`, 26087B/483LF.
Author note: `reviews/editor-independent/PVA027-NATIVE-TRAY-FIXTURE-DESIGN.md`,
SHA256 `dbbdaafdabd628a2570735f6379b6e0937e9f6ae322e9e2c4b7f05484feb2126`.
Complete fixture and note read; surrounding production DesktopSystemTray and
DesktopTrayStrings resolver read, exact EN/AR resource literals checked. Relevant
Desktop-native/localization instruction snapshots applied; their omitted tools
were not substituted or invoked.

## Reachability and counterexamples

- Public production constructor reaches real AWT install/update on the EDT; no
  injected fake platform, reflection, direct action/listener call or product seam.
  Same TrayIcon, PopupMenu and MenuItem identities distinguish updating a live
  owner from reinstalling a new tray. Real getString(environment, resource)
  resolves captured EN/AR/EN; hardcoded expected values are assertions, not inputs
  to setup. Default-locale changes are deliberately later than snapshots.
- The public factory's JVM behavior is not proved by the source archive. A
  cached/wrong snapshot must fail the strict Arabic assertion at runtime. No
  source-review claim of successful resource switching is made.
- Native marker coordinates require bounded three-colour density/geometry.
  A MouseEvent from the exact real icon is required before keyboard input;
  one stage-current callback and dismissal are required after Enter. Stale
  listeners, ignored right-clicks and unprocessed keyboard input do not satisfy
  the oracle. Callback may be any of show/lock/exit; this is not all-action proof.
- Popup geometry and post-Down pixels are readiness, not text/shaping/OCR or
  semantic selection proof. Three actual screenshots must independently show
  correct English/Arabic native labels, shaping, clipping and highlight. Tooltip
  field equality is not displayed-tooltip proof. Synthetic locator is explicitly
  not product-icon rendering. The full Settings/publisher/window-effect chain,
  packaging, other desktops and screen-reader/device coverage stay separate.

## Isolation, cleanup and compatibility challenge

Absent opt-in skips before Toolkit/resources. Opted-in malformed required fields
fail, not skip. Private paths must be normalized, non-symlink,0700; auth0600 and
JVM/environment HOME/TMP agree. These checks do not establish authority by
caller assertion: root must bind original directories, actual private socket,
X display/auth/WM/minimal systray configuration and owned process family. In
particular the fixture's bus path existence check is not a socket/UID census.
The fixture starts no helper or repository/storage operation and adds no dependency.

Held releases register before native presses. Cleanup independently attempts
release, observer/image restoration, the original tray cleanup, owned frame
removal, image-cache and all Locale-category restoration. Marker memory is not
flushed while a native owner could remain. Image output forces the original
channel before its stream closes it, then forces its directory; only four fixed
exclusive compact synthetic files are allowed. No test/evidence/source deletion.
EDT waits can time out with a native call still running; cancel(false) is not
settlement. Root's fresh supervisor must enforce process/namespace bounds and
original-wrapper stop before validated disposable-root cleanup. Soft60s and5s
waits are not hard wall-clock or global-idleness guarantees.

This is useful, narrower than rerunning the completed GUI/editor batch. One
prospective case/three stages/zero executions. Source acceptance does not close
PVA027 or authorize launching a panel. No source modification was required by
this review. All STOP/NO-RETRY/CLOSED/old-HOLD and non-publishing restrictions
remain. Only ordinary reads/hashes and this compact permanent note were used;
no Git/build/CI/helper import, display/bus/panel, temp cache or background task.
