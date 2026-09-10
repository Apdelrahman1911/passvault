# PVA-027 — one native tray fixture (source only)

Owner: `/root/editor_review`; independent reviewer/execution owner: `/root`.
New source: `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/tray/DesktopSystemTrayNativeIntegrationTest.kt`.
SHA-256: `0a70e40b519ec22e33a075467ec1961a40e8c9c091c43d53c31915e818f1baec`; 26087 bytes, 483 LF lines.
**One prospective test case, three language stages; zero executions/compilations. No closure or denominator change.**

## Boundary and oracle

- Uses public `DesktopSystemTray()` and real production `desktopTrayStrings`; no fake platform,
  reflection, direct listener/action invocation, production seam, Settings/Koin/bootstrap or real storage.
- Public `getSystemResourceEnvironment()` snapshots EN and AR while temporarily owning JVM Locale.
  Resolves EN→AR→EN under later English default and asserts exact reviewed production strings.
  Restores general, DISPLAY and FORMAT defaults independently. The public common API is verified by
  `reviews/desktop-tray-source-api/RECEIPT.json` (SHA `51eb894b8ca85fa8245189d3687835946c6b901f334eb9236c3ed187fcfad43e`);
  desktop actual was absent from that public source archive, so strict runtime resource assertions remain essential.
- Retains the same real installed TrayIcon, PopupMenu and three MenuItems. It changes only public
  icon image to an opaque three-colour synthetic locator. One native right-click per stage must produce
  a public MouseEvent from that exact icon before Down/Enter is allowed. Each stage must yield exactly
  one current-stage synthetic callback and dismiss the native popup; no side-effect retries.
- Bounded unique colour geometry targets the icon. Popup-sized screen change outside icon/blank-frame
  masks is readiness only (not OCR). Three cropped screenshots need independent EN/AR text, shaping,
  clipping and selection/highlight review. A post-Down pixel change is required, but not treated as text or
  selection semantics. Down does not assume a native initial selection: show/lock/exit
  are all harmless synthetic counters; the actual selected callback is recorded, not all three exercised.
- Gaps stay explicit: full Settings→publisher→private window effect, visual tooltip, product-icon pixels,
  all-action semantics, screen reader, packaged image and non-Linux/hardware behavior are NOT proved.

## Required fresh root admission — not granted by this file

Absent `passvault.pva027.syntheticDisplay` skips before Toolkit/resource access. An opted-in missing
boundary fails, never skips. Root must select exactly this one case with zero skips in a dedicated JVM;
no parallel locale/native tests. The fixture does not prove that caller-supplied properties own a session.

Required properties: `passvault.pva027.syntheticDisplay`, `.privateSessionDir`, `.evidenceDir` (all use
that full prefix). Local X11 DISPLAY must match, WAYLAND_DISPLAY must be absent/blank, JDK17/Linux,
one screen at (0,0), 640–1280×480–1024, identity display transform. Root independently owns/proves the
private display, single minimal systray manager, window manager, private D-Bus and auth/process family.
No helper is launched by this fixture; no installed default desktop configuration is assumed safe.

Session/evidence directories must be existing, normalized absolute, 0700, no symlink ancestors; evidence
must be empty. Session children `home/config/cache/data/runtime` map exactly to HOME/XDG_CONFIG_HOME/
XDG_CACHE_HOME/XDG_DATA_HOME/XDG_RUNTIME_DIR. JVM user.home matches home; JVM java.io.tmpdir and TMPDIR
match runtime. `Xauthority` is an existing0600 regular file matched by XAUTHORITY (contents never read).
DBUS_SESSION_BUS_ADDRESS must be `unix:path=<session>/bus` with optional32-hex guid; that path must exist
and not be a symlink. Root must separately verify actual socket, ownership, isolation and service identities.

60-second soft scenario budget; each EDT/observation/resource wait is at most5seconds (native/blocking
work is NOT hard-preemptible). Read-only screen polling sleeps250ms, at most20 captures per wait;
each screen buffer≈5MiB, at most three retained during selection. A separately reviewed root supervisor must bound worker/cleanup wall time,
RAM and the whole manager/helper family. No execution/import/probe or original-runner reuse is admitted.

## Owned cleanup and compact evidence

Held native releases are registered before each press. Failure cleanup attempts every owned release,
removes only the added observer, restores the original image, calls only this tray's cleanup and disposes
only its blank Frame. It requires the real tray list empty and frame non-displayable before claiming
those boundaries settled. Marker memory is not flushed if tray removal is uncertain. Entry/later interrupts
and primary/suppressed failures are preserved; uncertain EDT/native work remains external HOLD, not proof
of worker/process settlement. Root retains wrapper --stop, owned process settlement and allowlisted cleanup.

Only `01-native-en.png`, `02-native-ar.png`, `03-native-en.png`, `observations.txt`: exclusive0600 creates,
original channel force before close, parent-directory force, ≤1MiB/file, ≤16KiB trace. ImageIO uses memory
output; its public disk-cache flag is temporarily disabled (also for production icon decoding) and restored
independently during cleanup. Screenshots contain only independently admitted synthetic display content. No test
file deletion or directory creation; root retains evidence and separately settles/cleans runtime outputs.

Source dependencies: production tray SHA `d1023a5bc9573e97eaa3089cf433107f0e009f22e67e01c4fd4f1e77e0e28d58`;
production resolver SHA `423c0857c03d7f1e7ad57669140afe1d7f25ac52cf2dadd038e973fc7999ac75`.
Frozen GUI02/C11 four-case workload, production/init/workflow/harness sources and central ledgers untouched.
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 recorded failure/no automatic retry, G7/G8 CLOSED and all other
consumed/HOLD scopes remain unchanged. Root must independently challenge this new fixture before admission.

Read receipts this resumed source work: `54f3c4` API/tray; `8294ab` inert size-guard exit1 before large
Gradle source output (no mutation); `293ea7` bounded skill/accepted-fixture/source spans. No build/test,
helper, process probe, Git, network, dependency install, shared-cache or held-runtime access was performed.

Own self-pass: `bcdc28` exited2 on a line-wrap guard before any writes; that failed inert edit
check is preserved, not test evidence. Final source separates planned/completed stages and trace persistence
from scenario/owned-object status. No compilation or native execution was attempted.
