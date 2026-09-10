# PVA-027 — actual-Main Settings follow-up, bounded source assessment

Author: `/root/android32`; independent challenge required before adoption.
**Proposal only; zero new module cases, compilation, builds or runtime operations.**
No source input of root's Detekt/integration03 work was changed. The completed
integration02 native endpoint case was read through its current ledger disposition,
not replayed, imported or relabeled as full application evidence.

## Conclusion and exact deliverable

The corrected actual-Main infrastructure can support one small **partial** case:
real Settings/Appearance radio input EN → AR → EN, the real existing Settings singleton,
and exact labels/tooltip **properties** on the same already-installed native tray.
It cannot, using only its current primitives, also establish **displayed tooltip**
and actual tray **Lock/Exit callback behavior**. Those require the separate concrete
native-input/evidence work identified below; they are software prerequisites, not a
claimed hardware impossibility or reason to abandon the remaining PVA-027 work.

Inert member fragment here: `PVA027-MAIN-SETTINGS-PROPAGATION.kt.txt`, SHA-256
`fe8beaecef2a6155a582383347dc3abe6edabd1498ed3eda4679df54caf3dcd9`, 5,790 bytes / 96 LF.
It is deliberately **not** a compilation unit, new generic driver, registered test,
complete adoption patch or executable runner. Its seven required imports and insertion
point inside `NativeMainNav` are explicit. Parent source actually read/hash-bound:
`app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`,
`32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5` (40,561 bytes / 755 LF).
That parent's editable-only AX correction is reused, not reauthored or runtime-verified
by this lane. Any later parent change requires rebinding; root owns integration03 results.

## Current PVA-027 evidence is preserved

Read snapshot of `docs/audit-continuation/2026-09-08-linux/ISSUE_LEDGER.json`:
`031cbec176dc0925974851151aec534de8b361ac34220668989cdd511d5ff84b`.
PVA-027 remains **IMPLEMENTED — TARGET RUNTIME VERIFICATION BLOCKED**. Its current
integration02 source binding is commit `f7f3ca91cef14b7b2341114c44c460907061f883`,
tree `4b405be08458c035b9dd61807acab66659ccd864`; independent result review
`reviews/storage/INTEGRATION02-ACTUAL-RESULT-REVIEW.json`,
`254b133bbdf4b4f8e903cb3b53b3739518f5d8a3d57a81f6a744d17dfadccf2f`.
The ledger records one targeted native tray case passing EN/AR/EN, native Show callback
receipts, right presses, dismissal and owned endpoint cleanup, with independently viewed
popup crops. XML `99878d9760cd80d9ce52a099aa9711f334f03a81c0d54a75f96c4f44f28db27d`.
These are ledger-bound preserved results, not a fresh image or runtime review here.

The current remaining requirement explicitly includes full Settings→publisher→outer
effect→tray propagation, displayed tooltip, actual lock/exit actions and other-platform/
packaged behavior. Historical fake tray five/shared publisher two remain qualified;
the base handoff ledger (`docs/audit-handoff/current/issue-to-fix.json`,
`5782666330bc2eb017e42b7a8725db1efdf54223462f3648c8acf25cb21b245e`) was consulted only
for PVA-027's source/guard/remaining-requirement record. No closure denominator changes.

## Reachable production chain, not injected endpoint input

1. Settings' **App language** item dispatches `OnAppearanceClick`; the real navigation
   adapter presents `AppearanceSettingsScreen`. Its language `ThemeOption` rows use
   `Modifier.selectable(role = Role.RadioButton)` and call `OnLanguageChanged`.
2. The existing single `SettingsViewModel` changes its observable state and schedules
   preference persistence. The fragment reads that already-consumed singleton only
   after the real Appearance radio is visible; it never calls its event method.
3. `VerifiedPassVaultApp` observes that state, enters `AppLanguageProvider`, and publishes
   the resulting resource environment. Startup's outer provider does not publish it.
4. Actual `PassVaultDesktopWindow` observes the publication. Its environment-keyed
   `LaunchedEffect` resolves `desktopTrayStrings` and calls its remembered real
   `DesktopSystemTray.setup`. The already-installed handle updates the three menu labels
   and `TrayIcon.toolTip`; public menu listeners remain and invoke the current callback
   indirection. The fragment does not call setup, inject a tray, replace callbacks,
   mutate process locale, publish an environment, or read a fake tray platform.

Focused source identities (hash is whole-file identity, not whole-file review credit):

| Production source | SHA-256 |
|---|---|
| `feature/settings/.../ui/SettingsScreen.kt` | `ab34bfa20fcb176701dc3ff6b2414191acf759b5aa912f84d8bd63825f0bbd74` |
| `feature/settings/.../ui/SettingsComponents.kt` | `60435f4084c9479d1c2c3bc02fbf5f83f63b76faeb64c1dea2a95b3d41fb5677` |
| `feature/settings/.../ui/AppearanceSettingsScreen.kt` | `2b381898ad707dbb3f1637c0e588898c4f8c76415f194fee2e46aba395a841d1` |
| `feature/settings/.../presentation/SettingsViewModel.kt` | `a19baf0425234acdae2c2d923d74dbabf1e2b6a4822d923d80b9cccabfd6d723` |
| `shared/.../navigation/adapters/SettingsRouteAdapters.kt` | `6339edcfe9a09916a5485894e287c40697e85909158c0c348e51b486b8073c06` |
| `shared/.../di/AppModule.kt` | `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
| `shared/.../PassVaultApp.kt` | `75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737` |
| `shared/src/desktopMain/.../platform/AppLanguageProvider.desktop.kt` | `6d5be09814302ea0bb25cd1b4142ed883b930d892dff9166dda2b495c4212d59` |
| `shared/src/desktopMain/.../platform/DesktopAppResourceEnvironmentPublication.kt` | `3d56735ae5639254d9df13c6c6411fbeeed2c1c0b6c8bcab9a865f4d790d68da` |
| `app-desktop/.../Main.kt` | `9a67a8a5c2f05331e91ec5c18f6317a3562bd26e2c720105a8a21773edd8ae93` |
| `app-desktop/.../PassVaultDesktopWindow.kt` | `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702` |
| `app-desktop/.../tray/DesktopSystemTray.kt` | `d1023a5bc9573e97eaa3089cf433107f0e009f22e67e01c4fd4f1e77e0e28d58` |
| `app-desktop/.../tray/DesktopTrayStrings.kt` | `423c0857c03d7f1e7ad57669140afe1d7f25ac52cf2dadd038e973fc7999ac75` |

Abbreviated common source paths expand to `src/commonMain/kotlin/com/passvault/` plus
the module package; Desktop paths use `src/desktopMain/kotlin/com/passvault/desktop/`
or `/shared/` respectively. English/Arabic literals were read from
`core/designsystem/src/commonMain/composeResources/values/strings.xml`
(`5605f102cca37a9fddf7fe50ecd598c5460168fd9eb6149d4d7a8318f644b118`) and
`values-ar/strings.xml` (`66d4273670b44b6b3fd21000ce28d8fbea8b2a9b6f074de0f642051099762a30`).

## What the small fragment would observe and reject

Real native unlock precedes Settings; a real Appearance radio precedes the read-only
singleton lookup. Fresh synthetic default SYSTEM is required before explicit EN; then
the fixed English `Arabic` row and Arabic `الإنجليزية` row drive the round trip.
At each boundary, same Main frame, installed icon, popup, three enabled items and exact
listener reference lists must remain; all four native string properties must match
independent literals and the real Settings choice must match without its error state.
No source-level expected strings are passed to production localization APIs.

Freezing labels in English fails the Arabic stage; updating labels but not tooltip
fails; replacing the installed owner/menu/listeners fails. Missing/ambiguous radio or
merged Settings names fails under existing strict selectors, not an alternative direct
event. Actual Compose AX mapping/visibility is still a runtime prerequisite. Transient
old strings are bounded polls, not ignored exceptions. None of this observes pixels or
invokes a tray callback: a stale inner callback or unrendered tooltip can still pass
this **property-only** case. Listener identity is not callback-behavior evidence.
Preference saves are asynchronous; state/error/property samples do not prove persistence
or save-job settlement. Only synthetic settings would be changed; root must still own
fresh storage and final application/worker disposal.

## Exact obstacle to the complete native follow-up

The Main driver's snapshot visits the captured frame and its owned AWT windows. Its
native click primitive requires an active owned window and AX target geometry. There
is currently no admitted locator/oracle for the separate installed tray icon, native
popup rows or hover tooltip. `DesktopSystemTrayNativeIntegrationTest.kt`
(`0a70e40b519ec22e33a075467ec1961a40e8c9c091c43d53c31915e818f1baec`) has private synthetic
marker/pixel/input helpers, not reusable public methods. Its endpoint owns a blank frame,
installs the tray itself and accepts **any** Show/Lock/Exit callback after Down/Enter.
Copying that acceptance into Main can execute Exit in the first language stage; it does
not prove which real action ran. Calling ActionListeners, AccessibleAction or `setup`
directly would bypass the missing native evidence, not fill it.

Actual Main configures lock-on-minimize while unlocked and a 30-second focus-loss lock
(`PassVaultDesktopWindow.kt`:238–246,453). `DesktopWindowProtection.kt`
(`92c473e21c262972983b111b61adc18610bc17fcea92c6cc2b7f65ea4d5ee128`, under
`core/security/src/desktopMain/kotlin/com/passvault/desktop/security/`) confirms that
minimizing to test Show can itself lock; focus-loss timing can confound a later Lock
state. No product security guard should be disabled to simplify a tray test.

A complete **new** narrow case would need independently reviewed exact-icon native
input/ownership; bounded hover/popup crops and independent visible-text/highlight review;
actual intended menu-item event receipts rather than endpoint callback alternatives;
state observations that account for minimize/focus auto-lock; and a terminal native
Exit-input receipt ordered before Main's real exit/cleanup. This can be Linux software
work, but it is additional source/evidence admission, not existing primitive reuse or
a reason to replay the accepted endpoint. No missing authoritative tool/repository or
physical-hardware blocker was established by this assessment.

## Minimal future adoption boundary

The fragment requires a distinct one-case class/explicit opt-in and fixed receipt
contract, not insertion into the original dirty-editor scenario. The three new stage
receipts precede inherited `DRIVER_ASSERTIONS_COMPLETE`/`QUIT_KEY_CALLS_COMPLETE` only
after a reviewed case adapter admits them; the original `EVENTS` map rejects them.
Reuse one serial seed/Main/verify owner, private bus/home/storage/environment checks,
deadline/input cleanup and actual Main shutdown; preserve the original case unchanged.
Coordinate with the separate inert PVU-003 case-adapter proposal rather than independently
merging another enum/launcher. No adapter was authored or applied here.

Fresh independent source/adoption review and root-owned source/coordination/cleanup
admission remain mandatory. This proposal admits no current/old-run replay or retry,
hardware assurance, broad case selection, callback mock credit, new product finding,
PVD redesign or closure. All STOP/NO-RETRY/CLOSED/held scopes remain. Only these small
permanent review files were written; no runtime temporary files, caches, generated
outputs or application/background processes were created or removed by this lane.
