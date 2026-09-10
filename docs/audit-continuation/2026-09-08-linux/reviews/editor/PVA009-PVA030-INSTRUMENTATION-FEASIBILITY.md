# PVA-009 / PVA-030 Android Instrumentation — independent feasibility challenge

2026-09-10; reviewer `/root/editor`, proposed author `/root/android32`.
**Conditionally feasible for narrow source authoring, NOT an accepted executable fixture or execution admission.**
Author is preparing one app-android androidTest file containing two fixed bare-framework Instrumentation cases.
This note predates that concrete file; its eventual exact bytes need a separate independent source review.
No new dependency, manifest Activity, Gradle edit, generic driver, timing search or device matrix is proposed here.

## Current evidence is preserved

The current published `docs/audit-handoff/current/issue-to-fix.json` matches
`5782666330bc2eb017e42b7a8725db1efdf54223462f3648c8acf25cb21b245e` (908650 B).
Only PVA-009/PVA-030 current rows and focused source inform this review; embedded historical statuses remain
historical. Both rows say IMPLEMENTED — TARGET RUNTIME VERIFICATION BLOCKED. Six prior fake Clipboard
methods and static locale predicates are not relabeled native execution. OEM/history/expiry details,
same-process device language propagation, native prompt, cold-process and rendering qualifications remain.

The author's old packed-evidence reader refused mixed-device directories/files. That refusal is preserved;
this reviewer did not substitute a packed-evidence reader or reopen its archived payloads. A separate local
262144-byte bound initially declined this current JSON before content read; exact metadata established its
908650-byte size, then a fixed 1MiB current-data read/hash and two-row projection were used. A verbose output
projection truncated; a narrowed current-fields projection completed the affected reading. Neither was a
project command, runtime retry, baseline reset or renewed authority for any historical restricted helper.

## Startup / safety admission — hard prerequisites

A fresh **entirely synthetic Android device/user/clipboard and fresh dedicated Debug application state** must
be independently established OUTSIDE instrumentation, before launching it. Manifest names the real
PassVaultApplication; Application.onCreate initializes Koin, and Main composes real database/bootstrap and
settings paths and attaches native services. In-test Bundle opt-in/package/debug checks cannot retroactively
authorize that startup or prove clipboard/storage isolation. No personal clipboard capture/restore, real
vault/backup, tester data, biometric enrollment or signing material may be read by the fixture.

BuildConfig.DEBUG and package `.debug` are insufficient by themselves: storeScreenshot inherits Debug and
uses the same suffix. Require exact `com.passvault.android.debug`, actual `BUILD_TYPE == "debug"`, DEBUG,
and `!STORE_SCREENSHOT_MODE`. Supported API / real focus accessibility / target-UID behavior must be bound
in future root admission, not assumed from an SDK integer or instrumentation privilege. Unsatisfied native
preconditions must emit non-pass evidence, not assumptions/skips that look like successful verification.
Root alone owns the later runner wiring, launch, resource bounds, package/device settlement and cleanup.

## Case 1 — actual clipboard ownership and real lifecycle retry

The actual concrete AndroidClipboardService and its ClipboardService binding resolve to one Koin singleton.
Main.onResume and actual onWindowFocusChanged(true) call its onForeground; onStop invokes the real lifecycle
coordinator, whose lock-finally also calls clipboard.clear. These are reachable production hooks, not grounds
to invoke onForeground directly in a purported lifecycle regression.

Require a foreground/focused positive anchor: the actual manager exposes the service's synthetic text and
new ownership label before backgrounding. The stopped/no-focus phase must expose an unavailable/null read
and retained sensitive ownership across a real elapsed interval of at least the 5000ms minimum. If early
lifecycle cleanup, privileged background reads, lost focus evidence, OS clearing or another condition prevents
that interval, the case is non-pass, not silently weakened. Only real framework resumption/focus callbacks
may provide the retry; observe actual resulting clipboard absence and retired service ownership afterward.

Two important inference limits were challenged and agreed with the author before drafting:

1. `primaryClip == null` alone cannot prove Android policy denial: absence is also mapped to Unavailable.
   Even anchored observations cannot prove that the owned clip continuously survived an inaccessible interval
   or distinguish OS auto-clear from every application cleanup effect. Preserve that qualification.
2. A five-second wait does not isolate the private timeout as the cause of pending cleanup: actual onStop can
   request clear first. This is real elapsed-interval plus real foreground-retry evidence, not proof a particular
   private timer fired. No fake timeout, manual foreground call or competing lifecycle owner may fill the gap.

A same-text/different-known-synthetic-label replacement is useful real-provider ownership control, but not an
external-UID copy/revocation claim. Calling containsSensitive on a readable replacement forgets ownership
and cancels clearJob (service:75–80,117–124); doing so before expiry can invalidate a timer-preservation oracle.
Author accordingly narrowed this control to explicit clear ownership, not claimed timeout preservation.
Cleanup may remove only positively matched known synthetic labels, must not inspect/restore a previous clip,
and must preserve failures when ownership/readability or actual callback settlement cannot be established.

## Case 2 — genuine Activity recreation with deliberately controlled framework locale

Public Instrumentation.newActivity can intercept actual Main creation before attach/theme/resource use and
apply a fresh per-Activity override Configuration. Recreate must really destroy the captured old Main and
create/resume a different instance in the SAME process; manually calling lifecycle methods, swapping Compose
locals or setting process defaults at each assertion would not challenge the stale-snapshot defect.
Initial process EN seeding is allowed only before the first Main, inside the pre-admitted synthetic process.

The real provider reads LocalConfiguration.current.locales, applies LocaleList plus Java Locale defaults,
and publishes app-authored native strings. Actual singleton SettingsViewModel language events persist EN,
AR and SYSTEM choices; the mounted app consumes settings. Verify literal independent EN/AR expectations
against actual framework configuration, process defaults, currentNativeBiometricPromptStrings and the
Android helper's DEFAULT strings input. Supplying expected strings to the helper would bypass the publisher.
No shown biometric prompt, enrollment, direct publish call or per-checkpoint default-locale reset belongs here.

This deliberately injected per-Activity framework configuration is not device-global Settings propagation.
Process locale/prompt-input changes are not rendered Compose RTL, native prompt display, OS-owned text or
physical security evidence. A same-process two-case runner also does not satisfy the separate cold-process
controls. Preserved original limitations must not be closed by this narrower control.

Save both LocaleList and Java Locale defaults (including any independently changed category defaults if used).
Clear the override intent, finish/await only captured owned Main instances, and restore process defaults only
after their compositions are destroyed; otherwise production composition may overwrite the restoration.
Restore the synthetic settings through their actual owner as applicable, retain cleanup failures, and never
close Koin/Room independently merely because tests finish. Root's dedicated-package termination/settlement
remains necessary; a returned instrumentation status is not proof all application jobs or storage are closed.

## Source bindings and activity receipt

Focused guarded sections were read; full-file hashes are identities, not whole-file coverage claims:

- `app-android/src/main/AndroidManifest.xml`
  `5bd74cede2d0328c76ca7ba6ea1876df2491267d44077d54bce25df40226ab4b`

- `app-android/src/main/kotlin/com/passvault/android/PassVaultApplication.kt`
  `b448c69b3b7722dea1f49b6e59d43bb7dfc7c803a9837ea76c3856614147862f`

- `app-android/src/main/kotlin/com/passvault/android/MainActivity.kt`
  `f8842b501b62cbf2e621f667c05358aee1f9d73c4cf468f6ee7815d690d1b82f`

- `app-android/src/main/kotlin/com/passvault/android/security/AndroidClipboardService.kt`
  `db1b9e5387e7059719e0f49b4a8c9340f8abcf210d16a808a5408c17443e00cf`

- `app-android/src/main/kotlin/com/passvault/android/di/AndroidModule.kt`
  `de2403c4a0268ba789cc74ea4f2b0a3513683764db43ea77bb1dd5aa7730b1de`

- `app-android/src/main/kotlin/com/passvault/android/lifecycle/AndroidLifecycleLockCoordinator.kt`
  `c46f8c0db11d2560cb0d70a621d9f22c82a8b0c743723d5c1ed9bca0a66de7d2`

- `shared/src/androidMain/kotlin/com/passvault/shared/platform/AppLanguageProvider.android.kt`
  `a6bbb6c47d27b1e79b326152f5c091b80909839f84240fa23cd4dc9c61c57edc`

- `shared/src/commonMain/kotlin/com/passvault/shared/platform/NativeBiometricPromptStrings.kt`
  `98191d6632af9679abccaf3102be2fee8d71d92a445d5323face6034dbb11c96`

- `app-android/src/main/kotlin/com/passvault/android/security/AndroidBiometricKeyStore.kt`
  `20500c5423ee0586572c0e9bdab78f071c616033b5fe132fbb8ae738aca4125e`

- `feature/settings/src/commonMain/kotlin/com/passvault/feature/settings/presentation/SettingsViewModel.kt`
  `a19baf0425234acdae2c2d923d74dbabf1e2b6a4822d923d80b9cccabfd6d723`

- `shared/src/commonMain/kotlin/com/passvault/shared/PassVaultApp.kt`
  `75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737`

- `app-android/build.gradle.kts`
  `bc4aef06f156617d15b41bfbf41d58cc4b455c4fcd15152f8ed31a4b8fcc7625`

All listed source/current-data identities were rebound before this exclusive0600/fsync/readback review write.
Only this small permanent reviewer note was created. No author/module edit, source/helper import, syntax/AST
probe, Git, build/test, SDK/device/process/cache probe, native clipboard or locale operation, CI or network.
No temporary/cache/runtime output or persistent worker was created; foreground readers reaped; wrapper stop
is N/A. All STOP/NO-RETRY/CLOSED/HOLD restrictions, PVA-029 recorded failure/no automatic retry and PVD/hardware
boundaries remain. **Zero executed cases, new findings, fixes or closures; all denominators unchanged.**
