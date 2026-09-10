# PVA-027 — focused Settings-to-installed-tray source challenge

/root/editor, 2026-09-10. **No new concrete source defect confirmed; full-chain/native evidence remains open.**
Continues, not restarts, reviews/editor-independent/PVA027-LINUX-TRAY-FEASIBILITY.md
(SHA-256 c15601532f4d96be50b8cf0028b6cf278df36155bbb5bc47507fb0df0f20da3b).
Its accepted shared2/fake-tray5 qualifications remain historical evidence, not executions here.

**The actual source chain is connected.** AppearanceSettingsScreen:123–127,164–177 sends real EN/AR
OnLanguageChanged events. SettingsRouteAdapters:68–72 passes the context SettingsViewModel; NavigationHost:102
and VerifiedPassVaultApp:141 obtain the same AppModule:200 singleton, disproving a separate-screen-VM hypothesis.
SettingsViewModel:152–153,287–312 updates its StateFlow immediately, revision-guards/cancels serialized saves,
and rejects stale initial preference loads at78–95. VerifiedPassVaultApp:149–150 publishes inside its selected
language provider. The Desktop provider:15–22,31–42 intentionally changes JVM Locale and supplies a new
identity-scoped AppLanguageDensity, not a recreated application/window. A fixed OS/startup baseline is useful;
requiring JVM Locale to stay fixed would contradict the shipped full-chain behavior.

**Guards/counterexamples retained.** Startup/unavailable/recovery content does not publish (PassVaultApp:117,
149–150); system fallback before verified content is not a stale-language defect. Publication uses a remembered
owner and owner-checked disposal, preventing old disposal from clearing a replacement. The outer window
remembers its tray/window state; its language-keyed effect resolves all strings against one environment and
checks cancellation before setup (PassVaultDesktopWindow:71–75,224–237). Setup updates the existing handle on
EDT instead of adding another; failed update retains ownership (DesktopSystemTray:54–83,224–229). Lock callbacks
use current session state. These guards reject the simple “language switch necessarily recreates/leaks a tray”
hypothesis, not every native race. Save failure leaves optimistic language plus an error: changed native labels
would not establish persistence durability. SystemTray.isSupported or setup returning also cannot prove add
succeeded: native rejection is caught, and a handle is returned only after tray.add.

**Precisely missing:** an observed real Settings EN→AR→EN selection through verified PassVaultApp's publisher
and the actual outer effect, with the same native window and successfully installed AWT TrayIcon retained,
plus actual popup/tooltip rendering. The proposed direct public-constructor/native-adapter fixture is useful
but bypasses Settings, publication and that private effect; even a pass cannot close this entire chain.
No copied publisher, reflected private method, test-local wiring or source assertion substitutes for it.
Existing Arabic shaping/accessibility, inverse OS/app-locale, other-platform and packaged limits stay separate.
No full-app/bootstrap/recovery fixture is authored or admitted by this note.

**Byte bindings:** the five production files PassVaultApp, DesktopAppResourceEnvironmentPublication,
PassVaultDesktopWindow, DesktopTrayStrings and DesktopSystemTray still match the reused report's exact pins.
Additional reads below bind this source challenge (not a Git/tree or runtime identity).
S = feature/settings/src/commonMain/kotlin/com/passvault/feature/settings
C = shared/src/commonMain/kotlin/com/passvault/shared
D = shared/src/desktopMain/kotlin/com/passvault/shared/platform

| Path | SHA-256 |
| --- | --- |
| S/ui/AppearanceSettingsScreen.kt | 2b381898ad707dbb3f1637c0e588898c4f8c76415f194fee2e46aba395a841d1 |
| S/presentation/SettingsViewModel.kt | a19baf0425234acdae2c2d923d74dbabf1e2b6a4822d923d80b9cccabfd6d723 |
| C/navigation/adapters/SettingsRouteAdapters.kt | 6339edcfe9a09916a5485894e287c40697e85909158c0c348e51b486b8073c06 |
| C/navigation/PassVaultNavigationHost.kt | 50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006 |
| C/di/AppModule.kt | 9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25 |
| C/platform/AppLanguageDensity.kt | ce6a1fdd060b14cc84ebe4993b91c82f57ca19ee6e7af26161ac4e9a818e32f4 |
| D/AppLanguageProvider.desktop.kt | 6d5be09814302ea0bb25cd1b4142ed883b930d892dff9166dda2b495c4212d59 |

Bounded source/data reads and this one permanent note only; **zero new findings/fixes/cases/executions**.
A guessed presentation/SettingsScreen.kt was absent; the actual ui source was read. No runtime, Git/network,
subject import, build/session/input/fixture edit, generated output, cache or persistent process was created.
No cleanup authority follows. Root retains resource/execution admission; GUI02/C12, PVU-007 STOP, PVU-011
NO RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED, consumed/HOLD and PVD scopes are unchanged.
