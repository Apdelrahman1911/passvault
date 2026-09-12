# C20 Android clipboard / SYSTEM locale — independent source review

2026-09-11; reviewer `/root/android_platform_review`; paired author
`/root/android_platform_author`. W is `passvault-linux`; B is
`docs/audit-continuation/2026-09-08-linux`. The task-supplied C19 publication is
`3b2130f` / tree `603ed0fb`; this reviewer performed no Git/ref verification.

**Preserve the accepted two-case fixture and production corrections. No new
production defect or necessary framework-fixture/oracle change was established.**
PVA-009/PVA-030 remain **IMPLEMENTED — TARGET RUNTIME VERIFICATION BLOCKED**.
This is source review, not compilation, execution admission, device proof or closure.

## Exact current inputs and inherited acceptance

Paths below are relative to W. Whole-file hashes identify bytes; they do not award
whole-file semantic or equal-line historical execution credit.

| Input | SHA-256 |
|---|---|
| `app-android/src/androidTest/kotlin/com/passvault/android/audit/AndroidClipboardLocaleInstrumentationTest.kt` | `580c71e5fc5c917b7f62535282fa3436a3eada15e35ac7afa3bcd584f890c29d` |
| `app-android/build.gradle.kts` | `c7a382773cb11383d583b6fd1dcead85aac30ccc0a38f81995f535dc1029da42` |
| `app-android/src/main/kotlin/com/passvault/android/security/AndroidClipboardService.kt` | `db1b9e5387e7059719e0f49b4a8c9340f8abcf210d16a808a5408c17443e00cf` |
| `app-android/src/test/kotlin/com/passvault/android/security/AndroidClipboardServiceTest.kt` | `b4a69761ef3f6920ee586f070b5a22a7d552151850bce2718e1a7c06b5c91b64` |
| `app-android/src/main/kotlin/com/passvault/android/MainActivity.kt` | `f8842b501b62cbf2e621f667c05358aee1f9d73c4cf468f6ee7815d690d1b82f` |
| `app-android/src/main/kotlin/com/passvault/android/PassVaultApplication.kt` | `b448c69b3b7722dea1f49b6e59d43bb7dfc7c803a9837ea76c3856614147862f` |
| `app-android/src/main/kotlin/com/passvault/android/lifecycle/AndroidLifecycleLockCoordinator.kt` | `c46f8c0db11d2560cb0d70a621d9f22c82a8b0c743723d5c1ed9bca0a66de7d2` |
| `app-android/src/main/kotlin/com/passvault/android/di/AndroidModule.kt` | `de2403c4a0268ba789cc74ea4f2b0a3513683764db43ea77bb1dd5aa7730b1de` |
| `shared/src/commonMain/kotlin/com/passvault/shared/SessionBoundClipboard.kt` | `c5931804bc5569ca3fafac23600bb81df2b8d0906002874746a5a86183a036d0` |
| `shared/src/androidMain/kotlin/com/passvault/shared/platform/ClipboardLockPolicy.android.kt` | `03a9d43af553ed689ce283fc98e61933c141fa441a195c7fa09337bd2cd91dc4` |
| `shared/src/androidMain/kotlin/com/passvault/shared/platform/AppLanguageProvider.android.kt` | `a6bbb6c47d27b1e79b326152f5c091b80909839f84240fa23cd4dc9c61c57edc` |
| `shared/src/commonMain/kotlin/com/passvault/shared/platform/NativeBiometricPromptStrings.kt` | `98191d6632af9679abccaf3102be2fee8d71d92a445d5323face6034dbb11c96` |
| `shared/src/commonMain/kotlin/com/passvault/shared/PassVaultApp.kt` | `75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737` |
| `shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt` | `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
| `feature/settings/src/commonMain/kotlin/com/passvault/feature/settings/presentation/SettingsViewModel.kt` | `a19baf0425234acdae2c2d923d74dbabf1e2b6a4822d923d80b9cccabfd6d723` |
| `app-android/src/main/kotlin/com/passvault/android/settings/AndroidAppSettingsStore.kt` | `b17e4f118c8dd889f3f1cc04bcbea5ca305681431356a0d9800ae5c4fd9a059f` |
| `app-android/src/main/kotlin/com/passvault/android/security/AndroidBiometricKeyStore.kt` | `20500c5423ee0586572c0e9bdab78f071c616033b5fe132fbb8ae738aca4125e` |
| `app-android/src/main/AndroidManifest.xml` | `5bd74cede2d0328c76ca7ba6ea1876df2491267d44077d54bce25df40226ab4b` |
| `gradle/libs.versions.toml` | `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a` |

Read the current fixture in full (529 LF), the concrete clipboard service and six
host methods, current locale/prompt publication, Main/Application and the focused
bootstrap, settings, DI, session-copy and lifecycle guards. The biometric keystore
read was its prompt construction/default-input helper, not a fresh whole-keystore
security audit. Read the handoff safety/assembly material, C19, current PVA-009/030
rows and the supplied localization/secure-platform-biometric skills and references.

The C14 fixture `3762edc0...` is historical, not the current after-image.
Inherited B-relative independent reviews were read and rebound:

- `reviews/editor/PVA009-PVA030-INSTRUMENTATION-INDEPENDENT.md`,
  `f731206327042f1e02a53260a7cb0d64c930114d70932256e27dbf98fd46ed16`.
- `reviews/editor-independent/ANDROID-CLIPBOARD-LOCALE-DETEKT01-INDEPENDENT.md`,
  `add5f99382ceddd5be14e62a65e28230441c48d1fcc10bbfc7f5003f587cd043`.

The latter accepts the C16 decomposition to current `580c71e5...`, with no added
case or strengthened runtime claim. Original packed reviews were not reopened;
their supplied-reader refusal and all historical evidence qualifications remain.

## Independent challenges and dispositions

### Clipboard ownership, foreground and caller guards

1. `AndroidModule:63–64` binds both interfaces to one service. Main `onResume`
   and real focus gain call that service's `onForeground`; the fixture does not
   substitute a fake manager or invoke those callbacks manually.
2. Service `43–54,72–112` serializes ownership. Unavailable reads retain the token;
   a readable different label retires only local ownership. Expiry/clear mark
   pending cleanup, and foreground does nothing to a merely unexpired owned clip.
   A provider-clear exception preserves pending ownership. A fresh own copy
   cancels its predecessor's job and resets the pending flag. No missing guard
   was established in these inspected paths.
3. Fixture `208–215` tests same text under a different known label through
   **explicit clear**, before the ownership query can itself retire the token.
   It does not prove external-UID replacement or preservation until expiry.
4. Fixture `218–240` anchors actual owned text/token, observes stopped/unfocused
   null endpoints separated by 5.5 seconds, then requires retained ownership and
   real foreground retirement plus an empty clipboard. Missing required conditions
   cannot pass. Null means unavailable **or empty** in the real adapter; two samples
   are not continuous denial. Main's `onStop` coordinator requests clipboard clear
   in its lock-finally, so neither timer causality nor the individual foreground
   callback's causality is isolated. These existing qualifications are necessary.
5. The six host methods use an injected fake access boundary, a manual deferred
   expiry and `Dispatchers.Unconfined`. Their readable null-label case is a known
   replacement; real `primaryClip == null` instead maps to Unavailable. Do not
   promote fake-provider successes to real ClipboardManager/OS expiry evidence.
6. Production user copies have the additional `copySensitiveWhileUnlocked`
   pre/post-session checks and a non-cancellable authorized copy/cleanup section;
   Android does not preserve sensitive clipboard on background lock. The fixture
   intentionally calls the service directly with synthetic text while on fresh
   onboarding. That is useful service/framework evidence, not an end-to-end vault
   copy/authentication/session-race test or a production authentication bypass.

### SYSTEM locale, recreation and prompt inputs

1. The current provider keys its application on `language` and
   `LocalConfiguration.current.locales`, rather than rereading its own mutated
   process default as SYSTEM authority. Empty framework locales use the explicit
   English base fallback; explicit English/Arabic remain separate choices.
2. The fixture controls only the pre-attach Activity configuration through public
   `newActivity`, then requires destruction and focused recreation of distinct
   actual Main instances in the same PID. It does not reset process defaults at
   each assertion or directly invoke the native-string publisher. This challenges
   the stale process-lifetime snapshot within the stated controlled-config scope.
3. Shared DI makes SettingsViewModel a singleton consumed by the real mounted
   app. Actual language events and store load readback are exercised. The prompt
   assertions call the Android helper with its **default production-published
   strings**, compared against independent English/Arabic literals, not injected
   expected helper arguments.
4. Store readback plus momentarily absent error is not proof that the preferences
   save job settled successfully or that a cold process reads durable bytes.
   Settings state is published before its asynchronous save, and readback can
   precede terminal save/error publication. The existing limitation is correct;
   do not count this as durable-persistence verification.
5. Process defaults and helper inputs are not actual rendered RTL, system-wide
   locale propagation, shown biometric text, OS-owned buttons, TalkBack behavior,
   cold-start controls or physical authentication/key protection. No displayed
   prompt, enrollment or device-global setting is requested by this fixture.

### Startup, cancellation and cleanup

- Manifest/Application/Koin initialization can precede the in-test API/package/
  Debug/argument checks. Entirely synthetic device/user/clipboard (including host
  sharing) and fresh ordinary Debug application storage must already be admitted.
  `STORE_SCREENSHOT_MODE` and exact ordinary Debug checks correctly reject a
  screenshot variant; they cannot prove isolation.
- Ready onboarding is observed through the accepted bounded read-only AX gate,
  not a second active database-bootstrap call. Actual node merge/viewport mapping
  remains unobserved; a missing, ambiguous or oversized tree must fail without a
  weaker fallback. Do not change the gate speculatively to make an unknown target
  pass.
- Clipboard cleanup removes only positively known synthetic labels. Failed copy
  readback, unknown labels or inaccessible state can require outer cleanup and
  prevent success; retaining that non-pass is safer than speculative clearing.
- Captured Activities must be destroyed before restoring LocaleList with its
  saved selected index and general/DISPLAY/FORMAT defaults. Callback-unregister
  and other cleanup failures retain primary/suppressed errors. Observed interruption
  prevents success and is restored after terminal reporting.
- Cooperative 90s/case, 10s/condition, 5s/main-post and 30s/cleanup limits cannot
  terminate an already executing main callback or stalled Binder/framework call.
  Removing an enqueued callback does not cancel one already executing. Aggregate
  terminal success and independently reconciled outer ownership/settlement remain
  mandatory; individual case PASS statuses cannot discharge cleanup.
- Finishing Activities/recycling AX nodes does not settle UiAutomation, Koin,
  Room, singleton ViewModel saves, application scopes or device storage. The
  existing explicit external-settlement requirement is necessary, not a claimed
  cleanup defect that this lane can fix by killing a package or closing a graph.

## Target/admission feasibility and smallest next step

The current ledger leaves target/SDK-license admission unanswered. This reviewer
obtained no new target, account, license or host-resource facts and performed no
SDK/ADB/network/process/held-runtime probe. The blocker was reported promptly to
root and the paired author; no outer runner was generated.

Version-catalog source says compile/target SDK37 and minimum SDK24; the fixed
two-case instrumentation requires **API29+**. An eventual Android32 target below
API29 would not admit these cases. An approved API29+ emulator may supply bounded
actual-framework evidence but cannot supply the missing physical security/OEM
matrix. SDK/image metadata, a package suffix, a license file or another lane's
successful compilation is not target/isolation/license/cleanup authorization.
No dependency, target level, identity or version change is proposed.

Root should first establish whether a usable explicitly authorized synthetic
target exists. Only then does exact source/APK/target binding, ordinary Debug
storage preparation, fixed selection, host-sharing policy, execution bounds,
owned-worker/UiAutomation settlement and allowlisted cleanup admission become
actionable. Android32 retains first risk priority. C19 crypto compilation already
succeeded and must not be repeated here; it is neither compilation of this
app-android fixture nor execution of its framework cases.

The paired author's possible one-method fake-provider coverage delta is separate
from this unchanged framework-fixture disposition. Any concrete inert proposal
requires its own independent review and root adoption; it cannot unblock target
verification or add a defect closure.

## Accounting and boundaries

**Zero new builds, compilations, executed tests, framework cases, production
findings/fixes or qualified closures from this review.** The two existing fixed
framework case declarations remain unexecuted/unadmitted. Historical six fake
clipboard methods and locale static evidence keep their original exact-source
and execution qualifications; no denominator is increased by source reading.

Only this reviewer-owned permanent note was written. No production, central ledger
or runner was edited. No APK/SDK/private-data/held-runtime access, Git/CI operation,
helper import/execution, native clipboard/device operation, build or cleanup ran.
No background worker or disposable runtime was created; wrapper stop is N/A for
these source/data reads, not proof of settlement for another run.

Preserve PVU-007 STOP, PVU-011 NO RETRY, PVA-029 **49 checks / 44 PASS / 5 FAIL**
with no automatic retry, G7/G8 CLOSED, native-refusal and every consumed/HOLD
boundary. Root alone owns builds, admission, Git/CI and cleanup. Protected refs,
build1017001, versions, dependencies, identities, PVD choices, hardware/account/
license/Store/release restrictions remain unchanged.
