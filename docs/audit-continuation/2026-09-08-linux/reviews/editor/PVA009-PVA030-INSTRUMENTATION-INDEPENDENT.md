# PVA-009 / PVA-030 Instrumentation — final independent source review

2026-09-10; reviewer `/root/editor`; fixture author `/root/android32`; runner-hunk author `/root`.
**SOURCE ACCEPT for publication of the exact final fixture and one-line runner wiring below.**
This is not compilation, execution admission or target-runtime verification. Zero cases executed;
PVA-009/PVA-030 remain **IMPLEMENTED — TARGET RUNTIME VERIFICATION BLOCKED**. No closure credit.

## Exact accepted inputs

- `app-android/src/androidTest/kotlin/com/passvault/android/audit/AndroidClipboardLocaleInstrumentationTest.kt`
  SHA-256 `3762edc0f9ac2d26ba9ab68249a55004fd4cb3f08e01a2d337d23d3fbb1aab1f`; 28,533 bytes / 504 LF.
- `app-android/build.gradle.kts`, actual root-written postimage:
  `c7a382773cb11383d583b6fd1dcead85aac30ccc0a38f81995f535dc1029da42`; 30,600 bytes / 828 LF.
  Independently removing exactly the following sole added line after `defaultConfig`'s
  `applicationId = "com.passvault.android"` reproduces preimage
  `bc4aef06f156617d15b41bfbf41d58cc4b455c4fcd15152f8ed31a4b8fcc7625` (30,492 bytes / 827 LF):
  `testInstrumentationRunner = "com.passvault.android.audit.AndroidClipboardLocaleInstrumentationTest"`.
- Author delta `../android32/ANDROID-CLIPBOARD-LOCALE-CORRECTED-DELTA.md`:
  `af27e317612a31ae5aef6074f9ded8f7a2f39cd05c882b632dc75cb05ad71fc6`; 8,861 bytes / 122 LF.
- Earlier independent scope/production bindings: `PVA009-PVA030-INSTRUMENTATION-FEASIBILITY.md`,
  `bac420ab5f01af0b73947d6ce8cb131c63c3b3b8613f594e612b140fb42426a4`; 10,589 bytes / 143 LF.
  That note was conditional authoring feasibility, not acceptance of a concrete fixture.

Exactly two fixed prospective cases remain: `realClipboardOwnershipAndForegroundRetry` and
`frameworkLocaleRecreationAndExplicitToSystem`. The ordinary Debug build contract was checked against
actual source (exact `.debug` package, debuggable, non-screenshot mode); no `testBuildType` override was
present. Shared exports the referenced settings/core APIs. The runner hunk adds no dependency, manifest
Activity, build type, application identity, version or SDK change; the separate core Android32 KDF runner
is untouched. AGP source/default-debug wiring is not generated-manifest/APK or compilation evidence.

## Independent challenge and finite corrections

1. **Active readiness probe rejected.** The original direct `VaultDatabaseBootstrap.openAndVerify()`
   is not a read-only readiness observation: after failure it can retry the ordinary Main bootstrap.
   Both imports and the extra call were removed. Root separately authorized one bounded read-only
   UiAutomation readiness gate after actual Main focus, before clipboard/background/recreation/VM work.
2. **Gate reachability and safety checked.** Production Ready -> VerifiedPassVaultApp -> resolved
   absent-vault Onboarding -> OnboardingScreen -> WelcomeScreen supports the selected English heading
   and start-button literals. Heading semantics and the enabled-default Material3 Button are real source,
   not an invented test-only marker. The author delta records the exact five readiness-source hashes;
   those files and focused chain were independently read/hash-bound. Every node is checked for exact Debug
   package and the same root window before text/child traversal. Only the non-suppressing UiAutomation flag
   is used; bounds are 128 nodes, depth 20, 32 children. The gate requires exactly one visible heading and
   one visible enabled clickable start match. No actions, scrolling, input, shell, permission adoption,
   listeners, serviceInfo mutation or hidden/reflected connection-release method is added. Acquired nodes
   are recycled with primary traversal failures preserved.
3. **Cleanup failures and interruption preserved.** Callback-unregister failure no longer replaces an
   earlier cleanup failure; self-suppression is guarded. Bounded phase/type and primary/suppressed class-tree
   receipts avoid arbitrary exception messages or clipboard values. Observed interruption prevents success;
   temporary clearing is solely for cooperative cleanup and the flag is restored after terminal reporting.
   Per-case PASS reports precede aggregate cleanup: an outer consumer must also require the terminal result.
   Cleanup failure prevents terminal RESULT_OK; an isolated case status must not be published as a clean run.
4. **Exact locale restoration corrected.** Save LocaleList, Java default, its selected list index, DISPLAY
   and FORMAT defaults. Reject a missing selected index before any mutation and record mutation before the
   first setter. Restore only after captured Main Activities are destroyed, use indexed LocaleList restore,
   then restore/verify general and category defaults; clear the Activity override in finally. Intermediate
   `e56a01a...` used the one-argument restore and could reorder a saved nonzero-selected-index list. The final
   four-edit delta below closes that counterexample without changing device-global settings.
5. **Partial-write ownership corrected.** Register the fixed known synthetic replacement label before
   manager write, so a partial write is not abandoned as unowned cleanup. Unknown labels are never cleared
   speculatively; failure/readability uncertainty stays non-pass.
6. **Elapsed interval corrected and oracle narrowed.** The 5.5-second wait now starts after actual stopped,
   unfocused state and the first null manager sample, not the earlier copy timestamp. Both endpoints require
   stopped/unresumed/unfocused and null reads. A slow transition cannot consume the intended interval.
   These are two unavailable endpoint observations, not continuous policy denial or private-timer proof.

## Preserved non-pass / compatibility boundaries

- Application/Koin/Main startup may precede in-test checks. Before launch, an outer owner must independently
  admit entirely synthetic device/user/clipboard (including host sharing), fresh dedicated ordinary Debug
  app storage, target process ownership, hard bounds and cleanup. Bundle opt-in, API29+, exact package,
  DEBUG, BUILD_TYPE == debug and non-screenshot guards do not establish those safety boundaries.
- Clipboard case uses real target ClipboardManager, the actual singleton service and Main lifecycle/focus
  callbacks. The same-text/different-known-label control tests explicit clear ownership, not timeout
  preservation or an external UID. Reading containsSensitive on a readable replacement can retire ownership;
  the control does not do that before its explicit-clear oracle. Null clipboard reads can also mean OS-empty,
  and Main's onStop coordinator itself requests clear. Real wait plus foreground retirement therefore does
  not isolate private timeout or callback causality. Missing interval/ownership evidence cannot pass.
  API24-28, OEM/history/external-UID behavior remain unproved.
- Locale case uses framework-created/recreated real Main instances in one PID with a pre-attach per-Activity
  override, actual SettingsViewModel/store readback and literal independent process/prompt-input expectations.
  The Android helper uses its default production-published strings input, not supplied expected strings.
  No direct publisher or manual lifecycle callback substitutes for the real paths. This is not device-global
  locale propagation, rendered RTL geometry, displayed biometric UI, save-job settlement, cold-process
  control, enrollment or physical-device security evidence.
- Actual merged Compose accessibility mapping (especially clickable button text) and simultaneous viewport
  visibility have not been observed. Missing/ambiguous/bounds-exceeding readiness must fail, not trigger a
  weaker fallback or generic driver. Source acceptance does not predict that native gate will pass.
- Cleanup removes only positively matched known synthetic clipboard labels and finishes only captured Main
  Activities. It neither captures/restores prior clipboard nor independently closes Koin/Room, deletes storage
  or kills packages. Recycling AX nodes and `passvault.inProcessCleanup=true` do not prove UiAutomation or
  application worker settlement. `passvault.axConnectionSettlement` explicitly remains
  `INSTRUMENTATION_FINISH_AND_OUTER_OWNER_REQUIRED`; real finish/Binder/cancellation/outer device settlement
  are unexecuted prerequisites. Cooperative 90s/case, 10s/condition, 5s main-post and 30s cleanup bounds do not
  replace fresh independently reviewed outer hard bounds. No execution authority is imported from another lane.

## Rejected inputs preserved; exact final delta

The initial source is retained inert as `../android32/ANDROID-CLIPBOARD-LOCALE-REJECTED-A2ED698.kt.txt`,
`a2ed69836a68a1291f6673cc87ffc708cfc1594fc0387381585ff08fa067dbf1` (22,445 bytes / 407 LF),
independently matched to the initial reviewed bytes. The unchanged historical author note
`../android32/ANDROID-CLIPBOARD-LOCALE-INSTRUMENTATION-CANDIDATE.md`,
`dc0146d197a217984818c130852ec2d22399f8afe3c139a144ca037f37e8e914` (8,425 bytes / 123 LF),
describes that rejected input, not the accepted fixture. Initial-to-intermediate corrections received
independent data-only diff review. These were source rejections/corrections, not executed Android failures.
The following compact delta preserves the unaccepted intermediate alongside the final source: independently
reversing these exact four edits in memory reproduced SHA-256
`e56a01a791b6b5aa566d6d366fc493639fe51a181ce724f3df8220f4d0fd28e0` (28,244 bytes / 501 LF).
No intermediate source was imported, compiled or executed, and no extra full snapshot is necessary.

```diff
--- e56a01a-intermediate-unaccepted
+++ 3762edc-final-source-accepted
@@ -65,4 +65,5 @@
     private var originalLocales: LocaleList? = null
     private var originalLocale: Locale? = null
+    private var originalLocaleIndex = -1
     private var originalDisplayLocale: Locale? = null
     private var originalFormatLocale: Locale? = null
@@ -116,4 +117,6 @@
                 originalDisplayLocale = Locale.getDefault(Locale.Category.DISPLAY)
                 originalFormatLocale = Locale.getDefault(Locale.Category.FORMAT)
+                originalLocaleIndex = requireNotNull(originalLocales).indexOf(requireNotNull(originalLocale))
+                requireEvidence(originalLocaleIndex >= 0, "Original default is absent from the process locale list")
                 // A process-local initial control, before the first Main/provider composition only.
                 overrideLocale = Locale.ENGLISH
@@ -385,5 +388,5 @@
             onMain {
                 if (defaultsTouched) {
-                    LocaleList.setDefault(requireNotNull(originalLocales))
+                    LocaleList.setDefault(requireNotNull(originalLocales), originalLocaleIndex)
                     Locale.setDefault(requireNotNull(originalLocale))
                     Locale.setDefault(Locale.Category.DISPLAY, requireNotNull(originalDisplayLocale))
```

## Activity / ledger receipt

Review used bounded no-follow source/data reads, stable metadata tuples excluding atime, hash checks and
in-memory text differences, not syntax/AST or compiler probes. Final fixture, actual Gradle postimage,
author delta and feasibility note were rebound before this exclusive0600/fsync/readback reviewer write.
The author's packed-evidence mixed-device refusal remains preserved; this reviewer did not reopen the old
packed payload through another reader. Detailed current rows were previously root-authorized; original
unavailable packed reviews were not freshly read. No denominator or historical evidence was revised here.

Only this small permanent review was created in this final sealing step. No Git, source/helper import,
build/test, SDK/device/process/cache probe, native clipboard/locale operation, CI or network was performed.
No build outputs, caches, runtime temporary files or background workers were created; foreground data readers
settled, wrapper stop is N/A. Root remains sole execution/build/cleanup/publication owner. All STOP, NO-RETRY,
CLOSED, consumed/HOLD scopes, PVU-007, PVU-011, PVA-029 failure/no automatic retry, G7/G8 and PVD/hardware
boundaries remain unchanged. **Zero executed cases, new production findings/fixes or qualified closures.**
