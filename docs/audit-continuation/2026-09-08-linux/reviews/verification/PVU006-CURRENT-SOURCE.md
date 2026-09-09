# PVU-006 — current Desktop terminal-lifetime narrowing

Author: `/root/verification`. Source-only continuation; independent challenge pending from `/root/build_config`.

**Retain UNRESOLVED / VERIFICATION BLOCKED, not a confirmed defect. No production patch and no closure credit.**
The missing singleton-ViewModel terminal owner remains visible. This trace does not establish harmful externally
rooted work after actual disposal, permanent retention, or successful/bounded process termination.

## Binding and preserved evidence

- Observed HEAD: `da8ff89b9a8579017d5d524e628dff5251f2bb90`;
  tree: `cf0a8a702e7cd6948236be18b491bb5a21b5886e`.
- The fifteen production files inventoried below matched HEAD in the scoped `git diff --quiet HEAD` check
  (exit 0, tool `d5492a`). This is **not** a whole-worktree cleanliness assertion.
- Against handoff `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, fourteen named files are unchanged.
  The sole named-file delta is `VaultRepositoryImpl.kt`: the verification-plaintext/authentication error
  classification correction. Its per-instance `_sessionState` declaration and getter are unchanged;
  the delta does not supply a singleton terminal owner (source diff, tool `754e95`).
- Continued from `docs/audit-handoff/current/unresolved-investigations-outcome-only.json`, PVU-006 lines 291–347,
  SHA-256 `420b7eb1a419aabec37cbc323bc7d9a9b32ca160395dcc2d5f109488820eed19`, including the embedded
  `/root/remed_storage` independent review `b4d97cf14a572dc119968ce6d4576b3b5a0988166ba595b4edb286cc669ccff1`.
  Historical Koin `SingleInstanceFactory.drop` semantics are attributed to that review, not a fresh dependency
  import/execution. Named historical NAV-H5/report files were not located in the supplied handoff; the embedded
  outcome preserves their conclusions rather than inventing unavailable artifacts.
- Previous Linux disposition: `reviews/editor/REPORT.md` line 184 (relative to this continuation directory),
  SHA-256 `24f36c35411e2d2c88679dfd6820822681cecda18f2626f7c3f92cb44d3abd69`.
  The frozen iOS cover/ACK/unmount guards, synchronous NSUserDefaults counterexample, failed-metadata≠no-vault
  qualification, and fake-bridge-test limitations remain; this Desktop trace does not replace them.

## Current source: reachable work versus terminal guards

1. **Ownership gap, not demonstrated leak.** `AppModule.kt:78,190–201` creates an injected application scope
   separately from five ordinary Koin single ViewModels: Onboarding, Unlock, Vault, Settings and Backup.
   Their `viewModelScope` work has no supplied application/terminal owner or binding `onClose` hook.
   Navigation-entry-scoped ViewModelStore registrations at 203 onward do not clear these five ordinary singles.
   `VaultRepositoryImpl.kt:62,106` still supplies a per-instance session flow: a suspended VM/repository/flow
   cycle alone is not evidence of an external root, permanent retention or cross-runtime interference.
2. **The normal Desktop entry point intends a process boundary, not an in-process restart loop.**
   `Main.kt:20–31,36–69,105–125` starts the owned Koin/runtime once and calls `System.exit(exitCode)` after
   `runDesktopApplication` returns. Headless main exits before Koin/application startup. Thus repeated
   stopKoin/rebuild inside a continuing JVM is not the normal shipped entry-point schedule traced here.
   This narrows the historical accumulation scenario; it does not prove no work before actual process death.
3. **Cleanup is real source mitigation, with important conditions.**
   `DesktopShutdownCoordinator.kt:48–67,79–133` makes cleanup once-only, cancels a prompt, requests window
   preparation/exit, and tracks vault-lock→database-close, own clipboard clear, preview purge and biometric-host
   close on its injected scope. `DesktopSessionCleanup.kt:30–37` retains the bounded-lock retry/NonCancellable
   guard. `PassVaultDesktopWindow.kt:158–181,294–324` supplies hide/save/tray and disposal hooks that detach the
   prompt listener/native host and clean tray/window protection. These are not observations of actual
   ComposeWindow/Skia disposal, nor do these hooks establish clearing the five singleton ViewModels.
4. **Do not overstate the finally or deadline.** `Main.kt:128–140,254` waits up to 2,500ms on the cleanup latch;
   only a completed report reaches injected-scope cancellation and `stopKoin`. An incomplete report returns
   toward the intended process exit without those two calls. Initialization/coordinator creation precede the
   inner launch/finally, so an early caught startup exception can also return toward exit without that finish
   path. The latch timeout is not a universal 2.5s bound on disposal, Koin stopping or JVM death. No terminal
   execution, shutdown-hook behavior or successful process exit was observed in this lane.
5. **Ordinary lock is intentionally different.** `PassVaultNavigationHost.kt:367–424` performs session/UI-ACK
   cleanup, not a terminal singleton `onCleared` call. Onboarding/Vault cancel active work and wipe state;
   Settings keeps its session collector and preference-save job; Unlock can restart status work and has a
   30-second lockout timer; Backup can start import-selection discard and deliberately preserve validated
   restore work. Those distinctions are existing compatibility guards, not permission for blanket lock-time
   scope cancellation. Linux `DesktopBiometricRuntime.kt:20–43` selects the unavailable implementation, so
   Apple/Windows native prompt retention is not a demonstrated Linux external root.
6. **Concrete Desktop asymmetry, not an invented iOS suspension.**
   `DesktopModule.kt:41–43` binds Java Preferences to `DesktopAppSettingsStore`. The latter performs load/save
   in `withContext(Dispatchers.IO)`; save executes synchronous `put`/`putInt`/`flush` calls (lines 16–49).
   `SettingsViewModel.kt:287–313,504–525` serializes saves with its mutex/revision/cancellation guards but does
   not cancel that job on ordinary lock. An already queued/running IO save is a concrete candidate for
   transient externally scheduled work; unlike the iOS synchronous-provider counterexample, an IO dispatch
   boundary is present. Source alone does not timestamp it against actual disposal/database close/exit or
   demonstrate a late write, data loss, an old-to-new-runtime overwrite, or permanent retention. Synchronous
   Preferences calls also do not become cooperative cancellation points merely because the wrapper suspends.

## Remaining gap and stopping point

The smallest informative next observation is a **separately admitted actual graphical Desktop terminal
schedule**, with synthetic isolated settings/storage and identified actual Koin/singleton instances. Correlate
one reachable in-flight preferences/status/timer operation's owner and completion markers with actual window
disposal, database close, Koin stop **if reached**, and externally observed process exit. Distinguish a real
pending dispatcher/IO root from an otherwise unrooted flow cycle, and intended preference persistence from
harm. This describes an evidence predicate, **not** an execution plan/admission or a claim one happy-path run
closes PVU-006. Ordinary-lock compatibility and other platform runtime-rebuild cases remain separate.

No material source-only route to settle those observations was identified; no speculative lifecycle patch is
offered. A headless early return, fake indefinitely suspended provider, mocked close callbacks, forced in-JVM
restart or compilation alone cannot supply the missing shipped-path evidence. Existing STOP/NO-RETRY/CLOSED
restrictions remain unchanged; no archived helper, old runner or closed execution/recovery scope was used.

## Exact production source inventory (SHA-256)

Paths are repository-relative. These hashes bind inspected source, not executed cases or full-file coverage.

```text
9457f71fefe63de14ddd80d91b14b5b281175ee7c9bcdb113ac6a8ae15452616  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/Main.kt
7156e03eb7e5d2ea7636a9320a49aeaf15d0694522d88f78836b075c74e9043d  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopShutdownCoordinator.kt
865184fb667e705f43b4d8ab29f5b0b04024d0abd7c0d664aed09d5b439dc5d3  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopSessionCleanup.kt
794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/PassVaultDesktopWindow.kt
7b70040fe488c9825090548b3d8ca4a2e4f34c1989b072d2571d74c9647cd253  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/di/DesktopModule.kt
af1b629a53e824deea9a10df607b60c6099e6f11ac680578d57a8b7447ca2170  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/settings/DesktopAppSettingsStore.kt
111d03d9d074f0dc46dfcd670bcf2b3669b6a9104236fdb88287d41ad2b2e496  app-desktop/src/desktopMain/kotlin/com/passvault/desktop/security/biometric/DesktopBiometricRuntime.kt
9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25  shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt
50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006  shared/src/commonMain/kotlin/com/passvault/shared/navigation/PassVaultNavigationHost.kt
ae02e90a35bb2809414796df9357a5e42be8223be3a7ef614155cf00cdaea06c  feature/onboarding/src/commonMain/kotlin/com/passvault/feature/onboarding/presentation/OnboardingViewModel.kt
5c0c5511caf2cfa17df92cdb10ab1513ef98bbe7fa64798173106c57ff4811a0  feature/unlock/src/commonMain/kotlin/com/passvault/feature/unlock/presentation/UnlockViewModel.kt
879a2a7ba8208004f619b22109708ae4f1232e3d4acd5266522b1afc97dd20f2  feature/vault/src/commonMain/kotlin/com/passvault/feature/vault/presentation/VaultViewModel.kt
a19baf0425234acdae2c2d923d74dbabf1e2b6a4822d923d80b9cccabfd6d723  feature/settings/src/commonMain/kotlin/com/passvault/feature/settings/presentation/SettingsViewModel.kt
afd73521b4fc0d1b06af29bdc7d4940f4646507a66fcc4e55d801f2448060e63  feature/backup/src/commonMain/kotlin/com/passvault/feature/backup/presentation/BackupViewModel.kt
aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630  core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt
```

## Accounting and resource boundary

New production/test changes: **0**. Test/build/runtime executions and actual cases: **0**. No new confirmed
finding or conclusive suspicion resolution. Root retains the central issue/coverage ledgers and sole build slot.
Only bounded source readers, Git metadata/diffs and this permanent report were used; no application storage,
preferences provider, clipboard, dependency imports, worker, cache or temporary runtime object was created.
Wrapper stop is not applicable to this source-only task. Root owns shared-resource monitoring and future admission.
