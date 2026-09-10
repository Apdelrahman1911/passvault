# PVU-002 — bounded Desktop UI-admission feasibility

Author: `storage`; independent source challenge: `editor_review`.
Scope: source-only continuation, 2026-09-09. **No new finding, fix, test,
execution admission or closure. PVU-002 remains unresolved.**

## Disposition

No qualifying finite witness has been established within the currently admitted
partial-window scope. A real `ComposeWindow` plus the production credential
ViewModel and file-backed Room is not, by itself, the shipped lock/owner/input
boundary. Do not add a direct locked-delete test, fabricated ViewModel state,
manually delayed owner clearing, or a substitute lock observer to manufacture it.

This is not a proof that a future real-host witness is impossible. In particular,
the shared inactivity path below remains a candidate for separately admitted
navigation/lifecycle/DI integration. A bounded attempt that misses actual
post-lock, pre-clear admission must be **INCONCLUSIVE**, not a passing resolution.
This review stops before that integration or any execution.

## Revalidated surrounding guards and distinct lock paths

- The real confirmation is a Material3 `AlertDialog` button, enabled only while
  not busy and present only while its target resolves (`S01`:272–312). The
  production controller additionally needs a credential ID, delete target,
  loaded credential and no busy operation (`S02`:161–183,258–262).
- Production `clearForLock()` cancels tracked work, including attachment work,
  and synchronously resets the credential state (`S03`:314–335). Its real
  registration comes from the Koin navigation-entry ViewModel factory
  (`S04`:188–234), with synchronous coordinator owner clearing (`S05`:53–82).
  Constructing a ViewModel alone does not reproduce that lifecycle.
- The real shared host collects repository session state, then its private
  `ObserveSessionSecurity` effect clears entry owners before application owners,
  clipboard cleanup and guarded root replacement (`S06`:143–209,356–424).
  Holding clipboard cleanup therefore does not expose a pre-owner-clear window.
  Repository publication includes `Locking` before terminal `Locked`
  (`S07`:583–641); do not assume clearing starts only after terminal lock.
- **Native manual/minimize/focus path:** `DesktopWindowProtection.lock()`
  installs its curtain and requests iconification before invoking the security
  listener (`S08`:98–108). The real listener belongs to the private
  `DesktopWindowEffects` in the actual Desktop host (`S09`:208–280).
  Omitting those native steps to create a race is not a faithful witness.
  Conversely, source ordering alone does not prove that all input to an owned
  Compose dialog is excluded by the glass pane/window manager.
- **Shared inactivity path:** the actual host invokes its real timer and
  repository lock directly (`S06`:630–646), with a normalized minimum of one
  minute (`S10`:17–31). Desktop session-title handling only calls native
  `unlock()` for `Unlocked` (`S09`:329–345); there is no source basis to assert
  that every inactivity lock traverses the native curtain. The internal
  `PassVaultNavigationHost` can be invoked by same-module tests, retaining its
  actual observer and Koin/Nav3 ownership. That possible smaller real-host
  integration is not the already admitted partial-widget test, and its native
  input/scheduling equivalence has not been established here.

## Persistence is constructible; UI admission is still missing

The existing public Desktop `createDatabaseBootstrap()` builds real file-backed
Room at startup `user.home/.passvault/vault.db` (`S11`:18–73). Its existing public
bootstrap methods provide health-gated opening, the stable Room handle, and
terminal `checkpointAndClose()` (`S12`:50–165). Thus new Room dependencies are
not established as an essential blocker to a future fixture. This is only a
source construction observation, not proof that opening or teardown succeeded.

A future fixture would require a fresh, independently admitted synthetic startup
home/storage lifetime and proper disposal before reopening through a fresh
bootstrap. Instantiating the real Desktop module also introduces OS clipboard,
preferences, biometric, tray and file-service boundaries (`S13`:27–51); a private
X display or changed home alone is not safe full-application admission. The
actual public `PassVaultApp` also performs bootstrap and navigation startup
(`S14`:45–77,201–240). Neither it nor the real native host may inherit the
rendering-only fixture's admission: that fixture expressly excludes application
entry, Koin, vault, clipboard and biometric providers (`S15`:48–56).

Even a genuinely delivered post-lock click is not durable deletion evidence.
Production deletion performs guarded lookup and Room row deletion before
best-effort noncancellable object unlink (`S16`:231–249); subsequent ViewModel
cancellation can affect the outcome. Row presence and object presence must be
observed separately after proper teardown/reopen. Reopen is not power-loss
proof.

## Unfinished requirements, not a new execution recipe

A separately reviewed real-host proposal must retain normal UI loading and
confirmation, actual navigation owner registration/clearing, and the selected
shipped lock path. It must distinguish native input submitted only after
observing real lock publication from pre-lock queued/admitted input; demonstrate
actual admission before owner clearing without pausing that clearing; and bind
the resulting real row/object persistence observations to those events. Retain
pre-lock and post-clear controls. Wrong-owner and Room-failure component controls
do not substitute for the missing UI witness.

No production hooks, replacement platform contracts, dependencies or product
boundaries are proposed. No held runtime, application/recovery helper, old
runner, build/test/CI, process or cache probe was accessed. STOP, NO-RETRY,
FAIL/no-automatic-retry and CLOSED restrictions remain unchanged. This work
created only this compact source report and changes no progress denominator.

Independent challenge:
`../editor-independent/PVU002-WITNESS-FEASIBILITY-REVIEW-01.json`.
The independent reader confirmed the two distinct lock paths and rejected both
an overbroad curtain-based refutation and an artificial partial-widget witness.

## Exact reviewed checkout-byte identities

Paths below are relative to the continuation worktree. SHA-256 identifies the
bytes read for this review, **not** a current Git commit/tree or executed-source
identity. The earlier `REPORT.md` and its handoff bindings are preserved; this
note does not relabel that historical proof or inherit runtime/EOL authority.

| ID | Source path | SHA-256 |
| --- | --- | --- |
| S01 | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/ui/components/CredentialAttachmentSection.kt` | `0723eb3221bf09f5d8076040736a03fd96c2e508d37023ee914037bb7fd899f4` |
| S02 | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialAttachmentController.kt` | `61f3d3a8b9c17baaef3d7def87919d5e831c48c6ac160ee0f6e5cb16cd92412c` |
| S03 | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialViewModel.kt` | `b9a9c5e6a9e9a5eee86a13a639fa32826dbfdfbd09fa81a032935a37f58f0611` |
| S04 | `shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt` | `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
| S05 | `core/security/src/commonMain/kotlin/com/passvault/core/security/VaultUiSecurityCoordinator.kt` | `1a6c7ac9be3cd4ca5979ab9b09eced6b323b0282d1529b1c7039175d6fbc19e4` |
| S06 | `shared/src/commonMain/kotlin/com/passvault/shared/navigation/PassVaultNavigationHost.kt` | `50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006` |
| S07 | `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt` | `aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630` |
| S08 | `core/security/src/desktopMain/kotlin/com/passvault/desktop/security/DesktopWindowProtection.kt` | `92c473e21c262972983b111b61adc18610bc17fcea92c6cc2b7f65ea4d5ee128` |
| S09 | `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/PassVaultDesktopWindow.kt` | `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702` |
| S10 | `core/domain/src/commonMain/kotlin/com/passvault/core/domain/repository/AppSettingsStore.kt` | `241b3ff51e25e49d39e2faaa0d045e54953e8db9b09aa05a5a849430f4480c5a` |
| S11 | `core/database/src/desktopMain/kotlin/com/passvault/core/database/Database.desktop.kt` | `2aa4d34c71de4dae66a79e6d93af187a44642e4a504ff2a592d82c60245f39a7` |
| S12 | `core/database/src/commonMain/kotlin/com/passvault/core/database/VaultDatabaseBootstrap.kt` | `5726afb24093afa8c56c57321d64f0ea747db69910f0b7aa984e9d18a86f2598` |
| S13 | `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/di/DesktopModule.kt` | `7b70040fe488c9825090548b3d8ca4a2e4f34c1989b072d2571d74c9647cd253` |
| S14 | `shared/src/commonMain/kotlin/com/passvault/shared/PassVaultApp.kt` | `75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737` |
| S15 | `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/DesktopCurtainRenderingTest.kt` | `6e5f8190e94008f3e3c41b62b3d1d75cdca471057e2de57497829d5c42191c57` |
| S16 | `core/database/src/commonMain/kotlin/com/passvault/core/database/attachment/AttachmentRepositoryImpl.kt` | `58693cb59658f4cbba4aca4c6e1cc42c7efc69b0a7aa98a9128bf91ebea3929e` |
