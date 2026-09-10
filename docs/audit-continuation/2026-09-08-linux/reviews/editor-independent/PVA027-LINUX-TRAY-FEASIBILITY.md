# PVA-027: Linux tray runtime feasibility, not execution admission

2026-09-10; `/root/editor_review`. **Material software-only Linux route exists;
actual tray installation/rendering remains unproved. Not a hardware-only gap.**
This is bounded source/package-metadata assessment only; no fixture was authored,
no panel/display/application/probe was launched, and no case or closure is earned.

## Preserved scope

Current overlay PVA-027 is `IMPLEMENTED — TARGET RUNTIME VERIFICATION BLOCKED`.
The normative handoff row is in `docs/audit-handoff/current/issue-to-fix.json`,
SHA256 `5782666330bc2eb017e42b7a8725db1efdf54223462f3648c8acf25cb21b245e`.
Its LOW/P3 installed-tray language scope, accepted source, shared2/fake-tray5
qualified passes and remaining full Settings/native/accessibility/OS-inverse-
locale limits remain intact. They are not new executions or re-reviewed closures.
The referenced prior independent source review remains
`reviews/PVA-027-independent-storage.json`, SHA256
`3c28759d83fcd45a67920b39199b9adf55e42f221598939660acef5bf38e5ccf`;
its current row qualification, not an unqualified historical PENDING, was used.

Read repository AGENTS and supplied `desktop-native-interop-lifecycle` skill
(SHA256 `00a586157a50ae912290199061a55c1568ce678de67c080b56ffd1bd89220d17`),
its interop checklist (`53b37dddba4a12df4e8d87639e8656d7f6fbc5ae285b415f9f836c2fe22289a6`),
and `localization-rtl-accessibility` skill
(`3e2f319f8afcf776e492feabbd825ea0934afd5ae6578b746ec154b387f0f7b5`).
No skill helper was run or substituted.

## Ordinary installed metadata: sufficient to propose, not to launch

`/var/lib/dpkg/status` reports installed amd64 xfce4-panel4.18.4-1ubuntu0.1,
xfconf4.18.1-1build3, xfwm4:4.18.0-1build3, Xvfb:21.1.12-1ubuntu1.6,
D-Bus1.14.10-4ubuntu4.1 and OpenJDK17 JRE17.0.20+8-1~24.04; xauth is installed.
These are package records, not executable/loadability or private-session proof.

- `/usr/bin/xfce4-panel` is a regular executable,367576 B, matching root's passive
  observation. `libsystray.so` exists,207080 B. The packaged plugin description
  explicitly says it provides **legacy systray items** as well as StatusNotifier
  items; this is relevant to Java AWT's X11 tray, not merely an indicator-only host.
- `systray.desktop` declares module `systray`, unique `SCREEN`, API2.0 and
  **`X-XFCE-Internal=FALSE`**. The actual `wrapper-2.0` executable exists,35136 B.
  A panel-only PID assumption would omit a required potential child owner.
- The shipped default.xml channel has configver2 and a `systray` plugin, but also
  two panels, launchers, audio/power/notification/actions and other plugins. Do
  **not** start that broad default desktop or copy ambient user configuration.
- Xfconf's ordinary D-Bus service file names `org.xfce.Xfconf` and executable
  `/usr/lib/x86_64-linux-gnu/xfce4/xfconf/xfconfd`. Private-bus activation/ownership
  must be accounted for, not mistaken for an unrelated persistent daemon.
- `/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf` exists,759720 B. Presence is not
  evidence of chosen AWT font, Arabic shaping, correct bidi order or glyph coverage.
- `/usr/bin/xwininfo` exists (51592 B; SHA256
  `4b3a8b4c4f95942d579f667f52d192372303312265346fbde604b51b673404d2`),
  but was not invoked and is not admitted as a locator/tool by this report.
- Package-listed `xfce4-panel.1.gz` and README.md are absent in the installed
  filesystem. No guessed foreground/restart/control flags are promoted to an
  authoritative launch contract; no `--help`, install, fetch or upgrade was used.

| Installed file | Bytes | Observed SHA256 |
| --- | ---: | --- |
| `/usr/bin/xfce4-panel` | 367576 | `64447a123c52f98310d95b0937d5b1c28bbb6035a7294c1be85cae2fefae38b1` |
| `/usr/lib/x86_64-linux-gnu/xfce4/panel/plugins/libsystray.so` | 207080 | `2dba1f4e223fd78c2aa501ae89680506422efbd3c98911ab4f02ab29372bdc8d` |
| `/usr/lib/x86_64-linux-gnu/xfce4/panel/wrapper-2.0` | 35136 | `b1ddd412d41d9a69d0fc20bde9c1be6ac2b9a96d5d4cdade8b209fd7e8a509de` |
| `/etc/xdg/xfce4/panel/default.xml` | 4674 | `87b38ed482f67a54f13c6a1fe002a44c833ca03b2d5adffc75a74884be4e9b95` |
| `/usr/share/xfce4/panel/plugins/systray.desktop` | 6991 | `3d3871b3b680ddef283f961daed2ede583271d797d5564025f3d68606af703f2` |
| `/usr/share/dbus-1/services/org.xfce.Xfconf.service` | 89 | `8efc17b9481641c7ab3d18101675fcc9b3205bd1da33c055e2067ab215a9dbb1` |

These are fresh data-read bindings, **not future tool/instance admission**.

## Smallest useful new fixture: real native adapter/rendering only

A bounded new `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/tray/`
`DesktopSystemTrayRenderingTest.kt` is materially useful: one opt-in EN→AR→EN
case using the **public production `DesktopSystemTray()` constructor**, not its
fake platform constructor. No production extraction or new dependency is needed
for that boundary. Proposed source owner: `/root/editor` (if root assigns it),
with a different agent reviewing its source and later actual evidence. Root
retains the sole build slot and separately assigns panel/supervisor ownership.

The case should establish, in one isolated native lifetime:

1. Real AWT support **and successful actual installation**; unsupported/rejected
   add is a setup failure, not an assumed pass or opt-in skip. The production
   adapter catches installation exceptions, so a setup call returning is not proof.
2. One retained synthetic window and exactly one actual process-owned `TrayIcon`.
   Read public `SystemTray.trayIcons` and its popup/menu fields on EDT; compare
   the same icon and window identities across EN→AR→EN. No reflective/private
   seam, handle replacement, artificial fake tray or unrelated desktop icon.
3. Resolve each language through production `desktopTrayStrings` with an explicit
   resource environment; keep the startup JVM/OS locale fixed. This is a
   **test-controlled language driver**, not a shipped Settings-language selection.
   Real fields/tooltip must match expected resource strings in each state.
4. Open the actual docked native popup and hover the actual icon using bounded
   synthetic native input, retain compact menu/tooltip pixels, and exercise at
   least one actual native menu callback. The callbacks may be synthetic counters,
   explicitly not real vault-lock/shutdown/security-guard evidence. Public AWT
   strings alone are not native rendering, Arabic shaping or tooltip-display proof.
   A safe locator for the actually docked icon/menu must be independently reviewed;
   package metadata or a guessed coordinate does not establish that target.
5. Install cleanup before creating the window/tray, remove only the owned icon,
   settle EDT/native work and dispose only the owned window. Preserve failure
   pixels/XML and rely on root's separately admitted hard deadline/worker settlement;
   `invokeLater`, an EDT barrier or a completed test method is not process cleanup.

One case with three language states and their captures is **one test**, not a
matrix or three executed tests. Reuse existing passing fake failure/owner cases;
do not rerun them simply to rename them native. A small fixed menu/tooltip capture
set plus one raw Test XML is sufficient; no application binaries/archive artifacts.
No test source or proposed fixture is part of frozen GUI01/GUI02/C11 authority.

## Why that fixture cannot close the full Settings chain

The current production window remembers the real system tray and collects
`rememberDesktopAppResourceEnvironment`; its private `DesktopWindowEffects`
resolves resources in a language-keyed effect and updates that installed tray
(`PassVaultDesktopWindow`:64–75,208–237). `VerifiedPassVaultApp`:140–150 obtains
actual Settings state and publishes only inside its language provider. The
publisher retains owner identity and prevents stale disposal clearing replacement
(`DesktopAppResourceEnvironmentPublication`:19–46). Startup content does not publish.

`PublishAppResourceEnvironment` is **internal to shared** and the outer effect is
private; copied wiring, direct publication or reflective access in an app-desktop
fixture is not the shipped end-to-end path. The small native fixture above can
remove the real-AWT rendering gap, but **not** prove Settings UI→verified content→
publication→outer effect while the existing window/handle remain alive.
A full-chain case would need the actual stable `PassVaultDesktopWindow` plus real
verified `PassVaultApp`/Settings UI under separately admitted synthetic bootstrap,
Koin, preferences/storage and session ownership. That is a distinct larger scope,
not a reason to skip the feasible native boundary or to silently weaken its claim.
No production visibility/API redesign is proposed to fake that integration.

Arabic layout/accessibility, actual focus behavior, OS-Arabic/app-English inverse
startup, other Desktop platforms/packaged images and any physical-device claims
remain separate. This fixed Linux sequence needs no physical security hardware.

## Fresh execution/cleanup prerequisites — no command admission here

Reuse accepted GUI containment **as reviewed source primitives only**, in a new
root-owned scope: private authenticated Xvfb, appropriate WM, private D-Bus and
isolated HOME/XDG/TMP/JNA/SQLite state. Author a fresh minimal panel channel with
only one systray plugin; do not edit `/etc`, load default launchers/session apps,
reuse ambient panel/bus/display, or activate network/signing/store workflows.

The panel, external plugin wrapper, Xfconf and any D-Bus/GTK accessibility or
migration activation introduce new helper/descendant and startup behavior. Exact
launch arguments, one-tray selection ownership/readiness, process identity,
activation policy and cleanup need fresh independent source/instance admission.
AWT `isSupported()` plus actual docking/rendering must be witnessed; file presence
and an Xvfb/WM-ready result do not prove a working tray. Unexpected children, an
unsettled native call, lost ownership or failed cleanup remain HOLD, not permission
to kill unrelated processes, relaunch a panel or retry unchanged work.

Root must record one exact source/command selection, bounded setup/test/drain,
resource floors and compact evidence retention before any run. Use JDK17, the
checked-in wrapper, one worker/non-daemon/configure-on-demand off and strict
verification; run the appropriate original wrapper stop and verify owned-worker
settlement before allowlisted temporary-output removal. Keep permanent tests,
reports, shared caches/toolchains and all old scopes outside cleanup.

## Source identities, limitations and reviewer accounting

All following current source hashes match the preserved handoff row; this source
assessment did not change them or assert a new published commit/tree:

| Source path | SHA256 |
| --- | --- |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/PassVaultDesktopWindow.kt` | `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/tray/DesktopSystemTray.kt` | `d1023a5bc9573e97eaa3089cf433107f0e009f22e67e01c4fd4f1e77e0e28d58` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/tray/DesktopTrayStrings.kt` | `423c0857c03d7f1e7ad57669140afe1d7f25ac52cf2dadd038e973fc7999ac75` |
| `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/tray/DesktopSystemTrayLocalizationTest.kt` | `2cf77d047953eba1fb945a73ba0838c0c2f8f487c593d185a031165cd6c2d2bf` |
| `shared/src/commonMain/kotlin/com/passvault/shared/PassVaultApp.kt` | `75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737` |
| `shared/src/commonMain/kotlin/com/passvault/shared/platform/AppResourceEnvironment.kt` | `309a1d26980ef52edb905500ac12023635c0481cac0cdff98e3602fdbf01a65c` |
| `shared/src/desktopMain/kotlin/com/passvault/shared/platform/DesktopAppResourceEnvironmentPublication.kt` | `3d56735ae5639254d9df13c6c6411fbeeed2c1c0b6c8bcab9a865f4d790d68da` |
| `shared/src/desktopTest/kotlin/com/passvault/shared/platform/DesktopAppResourceEnvironmentTest.kt` | `ea791aeaf7e45998f52c6c3b2610285d622b317d58f57821db258177635230c0` |

Successful bounded source/metadata receipts: `1bf658`, `28f0c6`, `686e6e`,
`2d8d87`, `b15efa`, `0d92c4`, `970330`. Retained read-only limitations:
`c120be` and `2cf1e1` exited1 on overbroad ledger-output size/count guards before
package reads; later field-specific reads supplied the relevant row. `42117e`
exited1 on guessed App.kt after printing the actual PassVaultApp.kt source path;
`0d92c4` read the correct bound source. `0479d2` printed complete default/plugin
metadata then exited1 at the genuinely missing package-listed manpage; remaining
source reads and README absence were separately recorded. These are data-read
limitations, not application tests, runtime failures or retry admission.

Only this compact permanent report was created with exclusive0600, file+parent
fsync and exact readback; metadata comparisons use integer nanoseconds. No build,
Git/CI, package/probe/help execution, display/panel/app launch, driver import,
network/install, shared-cache/old-HOLD access, native input, control or cleanup
operation occurred. No temporary/generated/runtime outputs or persistent workers
were made. **Zero executed cases, new fixes or qualified closures.**
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED,
all consumed scopes and frozen GUI01/GUI02 remain unchanged; no central ledger edit.
