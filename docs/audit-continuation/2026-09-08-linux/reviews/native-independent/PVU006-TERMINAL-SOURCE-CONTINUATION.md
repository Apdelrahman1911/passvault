# PVU-006 — terminal late-work source continuation

Author: `/root/native_review`; 2026-09-10. Independent challenge of this note is pending.

**UNRESOLVED / VERIFICATION BLOCKED. No new confirmed finding, fix, test or closure.**
This distinct bounded source task does not retry a refused native task or any closed execution scope.
It adds one real automatic caller to the prior lifetime investigation, but does not establish its harmful
terminal schedule. No product/test change is justified merely by that possibility.

## Continuity and source identity

- Continue `ISSUE_LEDGER.json` original-suspicions PVU-006, observed SHA-256
  `1e30b599decdac0a9dca07573ffb05789054b8b4d2289a552938c3a2fb6dfb21`.
- Preserve `reviews/verification/PVU006-CURRENT-SOURCE.md`, SHA-256
  `5f7652d07deb935cf5378eb896000fc51d374713f731f7488b7a2ffe88f56a7f`, and its independent acceptance
  `reviews/build-config/PVU006-CURRENT-SOURCE-REVIEW.md`, SHA-256
  `7a688f2c9915158362252d2970b7e6084e5ca6e4ec54c3110c097b6852faa0cd`.
  All fifteen source-file hashes in the former's complete inventory were freshly compared and still match.
  That is a per-file byte binding, **not a claim that this editing workspace has a valid HEAD/tree**.
  Root owns later publication/tree binding; the retired workspace Git store was not touched.
- Prior editor and storage source outcomes remain: `reviews/editor/REPORT.md`
  `24f36c35411e2d2c88679dfd6820822681cecda18f2626f7c3f92cb44d3abd69`; `reviews/storage/REPORT.md`
  `fedae4cb95697f26e36d6da26cc3804075644096f32061c7c00c492de7f139a0`.
  The embedded historical PVU-006 outcome/review and unavailable original NAV-H5 evidence remain as
  qualified in the earlier note; no archived evidence helper was executed or imported.
- Read root AGENTS and supplied `desktop-native-interop-lifecycle` / `production-readiness-audit`
  snapshots. Additional cited bytes are listed below. Hashing is not full-file coverage or an executed case.

## Guards that still prevent an easy defect claim

1. `AppModule.kt:78,190–201` still separates the application coroutine scope from the five ordinary
   singleton ViewModels. `Main.kt:20–31,55–69,128–140` still intends one runtime followed by System.exit;
   no shipped continuing-JVM rebuild loop is established. The 2,500ms latch wait is not a bound on all
   disposal/process death. Incomplete cleanup still skips scope cancellation and stopKoin.
2. Pending Preferences IO is not itself harm. `SettingsViewModel.kt:287–313,504–525` retains save
   serialization, revision/cancellation guards and deliberate ordinary-lock persistence.
   `DesktopAppSettingsStore.kt:40–49` has a real IO dispatch plus synchronous writes/flush; an intended
   late save alone is neither data loss nor an old-runtime overwrite. This pass proves neither.
3. An already-started password unlock is not sufficient: `VaultRepositoryImpl.kt:239–244,361–377,
   733–742,816–843` records lock intent, rejects stale generation before/after last-access IO, and refuses
   publication. Session leases are separately revoked/cancelled/wiped at 490–507,549–567,583–648.
   A fresh ordinary unlock after lock must remain compatible; PVA-033's existing freshness integration
   deliberately tests that case, not terminal ownership.
4. Closed-database errors are not no-vault/delete authority: `DefaultBiometricUnlockService.kt:28–51`
   reconciles null only after `exists().getOrNull() == false`. Unlock status/timer continuation alone
   does not demonstrate harmful native work or externally rooted permanent retention. Window cleanup
   detaches listeners, stops its timer, clears frame/restore state (`DesktopWindowProtection.kt:237–252`);
   a queued completion is not automatically a restored sensitive window.

## Additional caller: onboarding's automatic create → unlock stage

This is a **candidate schedule to challenge**, not a reproduced defect or an invented post-exit UI event.

- Shipped entry/producer: `AuthRouteAdapters.kt:49–58` uses the singleton onboarding owner;
  `OnboardingScreen.kt:35–45` forwards its events; `MasterPasswordConfirmationScreen.kt:90–99,286–307`
  produces Confirm from the enabled submit control. `OnboardingViewModel.kt:46–51,226–246` applies
  busy/confirmation validation and starts `createJob` in its independent viewModelScope.
- The same legitimate job calls repository `create` then separately calls `unlock`, without another user
  event (`OnboardingViewModel.kt:249–280`). Create releases the repository's exclusive transition after
  inserting metadata and setting `Locked(reason=null)` (`VaultRepositoryImpl.kt:136–160,659–664`).
- Native close remains reachable while setup is busy: `PassVaultDesktopWindow.kt:130–132,158–181`
  calls shutdown directly. The navigation Back guard is real but not this native-close path.
  `AuthRouteAdapters.kt:99–118` binds clearForLock only as beforePop;
  `NavigationBackCoordinator.kt:126–127,173–174` merely unregisters during composition disposal.
  There is no explicit singleton terminal clear in those disposal hooks.
- Conditional interleaving: after create releases its locks, the shutdown Default-scope lock could finish
  before that job starts its second, password-unlock call. If it sees null currentVek plus Locked,
  `requestLock` takes its short path (592–595), and lock releases its intent (374–375). A subsequently
  started unlock snapshots the newer generation; the stale-attempt guard does not reject it merely
  because its caller began setup earlier. This is a boundary difference from PVA-033, not its reopening.
- **Missing necessary links:** actual ordering relative to Compose observer disposal, the terminal lock,
  Room checkpoint/close, successful late session publication and process exit has not been observed.
  `ObserveSessionSecurity` would cancel onboarding if it observes the reasoned lock before disposal
  (`PassVaultNavigationHost.kt:367–387,419–424`; `PassVaultApp.kt:285–314`). Create's null-reason Locked
  is not itself that lock event. The default shutdown lock reason is Manual (`VaultLockRetry.kt:14–23`).
  Room may close before late metadata/last-access IO, making unlock fail safely. Successful publication
  requires crypto validation and a successful `updateLastAccessed` before `openSession` commits;
  `VaultDatabaseBootstrap.kt:145–178` really checkpoints and closes Room, although its mutex is not the
  repository transition mutex. Intended System.exit may end the process before any harmful late work.

Accordingly, source supplies a legitimate automatic caller and a non-excluded interstage admission
hypothesis, **not** proof of a successful late unlock, permanent key retention, data loss or process hang.
There is no independently accepted new PVA row and no proposed blanket ordinary-lock cancellation.

## What would discriminate it, and what currently blocks that conclusion

The missing witness is software lifetime/ordering evidence, **not inherently physical hardware**. A
separately admitted Linux Desktop/Room investigation could correlate non-secret operation/owner markers
for this actual setup job with native close, actual composition disposal, successful terminal lock,
actual Room close, any later successful session publication, and externally observed process death.
Use only fresh synthetic storage/passwords; never print keys. A real late successful session after the
terminal lock is materially stronger than a UI-state callback or a retained cycle.

A narrower real-repository/actual-ViewModel scheduling probe can first falsify the admission hypothesis,
but it is not terminal-runtime proof. An inserted await in a repository decorator, a delayed fake close,
mock DAO success after closure, direct extra unlock event or omitted normal observer must be labelled as
synthetic; none alone establishes the shipped sequence. Relevant countercontrols are: delivered
clearForLock cancels the original setup; lock pending across an admitted unlock prevents publication;
Room already closed rejects required IO; a genuinely fresh ordinary post-lock unlock remains allowed.
A meaningful full predicate retains actual coordinator/bootstrap/Room behavior instead of replacing
all three with callbacks. No harness was authored or admitted solely to manufacture this possibility.

Existing test source does not fill that gap: `DesktopShutdownCoordinatorTest.kt:19–51,163–181` uses
operation/exit callbacks; `OnboardingViewModelTest.kt:188–200` clears prepared state with FakeVaultRepository
without starting Confirm/createJob; `BiometricUnlockFreshnessIntegrationTest.kt:100–110` proves a fresh
ordinary attempt, not a terminal singleton lifetime. These are coverage qualifications, not failed cases.

The present blocker is the absent independently reviewed, properly isolated software witness/admission
and actual result. It is not an unavailable authoritative skill URL, a mandatory Apple/Windows runner,
or evidence that Linux execution would be safe merely by moving old work there. This note requests no
run, retry, existing-scope expansion or speculative implementation; root can decide whether this narrower
candidate merits further work after independent challenge. Other PVU-006 platform/lifetime gaps remain.

## Additional cited source/test hashes

Repository-relative SHA-256; the unchanged fifteen-file inventory is bound above by its retained report.

```text
e689fbba229f372e128d3525cc03f85ff7682d955d191d1f74c915b692193550  app-desktop/src/desktopTest/kotlin/com/passvault/desktop/DesktopShutdownCoordinatorTest.kt
5726afb24093afa8c56c57321d64f0ea747db69910f0b7aa984e9d18a86f2598  core/database/src/commonMain/kotlin/com/passvault/core/database/VaultDatabaseBootstrap.kt
46cc5e5175e4f72d978857513651e2c30ff3c5586caefab6e7a10bc4ba85bcb5  core/database/src/commonMain/kotlin/com/passvault/core/database/dao/VaultMetadataDao.kt
431356205bb300aeb0a7c14759eef0bbf09583d3100082e5dd0103ac731a90f5  core/database/src/commonMain/kotlin/com/passvault/core/database/repository/DefaultBiometricUnlockService.kt
1108e4b0d4353c840974ac0ed57331e257947e9bded200a52bc73afd3b9d3e0e  core/database/src/desktopTest/kotlin/com/passvault/core/database/repository/BiometricUnlockFreshnessIntegrationTest.kt
2aec57f79a3010e8bae270cffc3bf9e5761a033c8c8af0a147c50b3476d305d6  core/domain/src/commonMain/kotlin/com/passvault/core/domain/model/VaultSessionState.kt
67e34bd0116ab988478fd4d1e11de63feac6b442f95a6b43d4f215cc14b8d648  core/domain/src/commonMain/kotlin/com/passvault/core/domain/repository/CredentialRepository.kt
8ddb72a3ee44a2f6a8b87aba5474b5ade6369cf33d1a9943bfe2ec7e6e6f1520  core/domain/src/commonMain/kotlin/com/passvault/core/domain/repository/VaultLockRetry.kt
92c473e21c262972983b111b61adc18610bc17fcea92c6cc2b7f65ea4d5ee128  core/security/src/desktopMain/kotlin/com/passvault/desktop/security/DesktopWindowProtection.kt
53b5d638779e7374e924175201f376b9fe63a284ba4e079c10ecc5a1c78ac4b6  feature/onboarding/src/commonMain/kotlin/com/passvault/feature/onboarding/ui/MasterPasswordConfirmationScreen.kt
32220a096262954a626c29e7958cc11c915c708908ff5ec966aba9307a433fb9  feature/onboarding/src/commonMain/kotlin/com/passvault/feature/onboarding/ui/OnboardingScreen.kt
8d95bbdeeb32932eee7e5b9e307596e04c1cf4a847e1f26a1830783ec32b9f98  feature/onboarding/src/commonTest/kotlin/com/passvault/feature/onboarding/presentation/OnboardingViewModelTest.kt
75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737  shared/src/commonMain/kotlin/com/passvault/shared/PassVaultApp.kt
4ac0de3356964b3317743218ffab8f8feaa126213f61a085f59173053c759e6a  shared/src/commonMain/kotlin/com/passvault/shared/di/AppDatabaseLifecycle.kt
7f0f546301a87c0bd758d121a118d8325b2fe366561655d1ee5c8e1b248126db  shared/src/commonMain/kotlin/com/passvault/shared/navigation/NavigationBackCoordinator.kt
3d2541f8751a562ede692bae7f6e4f8b91322dcc6d84d8457bb3ca59a14609f3  shared/src/commonMain/kotlin/com/passvault/shared/navigation/adapters/AuthRouteAdapters.kt
```

## Accounting and authority

Production/test edits **0**; builds/tests/runtime invocations **0**; actual cases/pass/fail **0**;
new confirmed findings/qualified closures/conclusive PVU resolutions **0**. Root owns central ledgers,
the sole build slot, resource monitoring and publication. No denominator changes are claimed.
Only inert source/report reads and this exclusive small permanent report write occurred; no application
storage/provider, clipboard, Git/network/process probe, helper import/execution, cache, archive, temporary
runtime or background worker was created. Wrapper stop/generated-output cleanup are not applicable to
this source-only task and do not discharge previous obligations. PVU-007 STOP, PVU-011 NO RETRY,
PVA-029 FAIL/no automatic retry, G7/G8 CLOSED, Windows05 filesystem HOLD, other HOLDs and all PVD/hardware
boundaries remain unchanged. Independent challenge of this report must precede any status promotion.
