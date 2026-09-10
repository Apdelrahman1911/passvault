# PVA-009 / PVA-030 — two-case Android instrumentation candidate

Author: `/root/android32`. Independent reviewer: `/root/editor` (review requested).
Root authorized only one new Android test file, not Gradle wiring or execution.
This candidate is outside the C14 execution selection. **Zero builds, compilations,
Android/device/SDK operations or product tests were performed by this lane.**

## Source identity and unchanged evidence

New file: `app-android/src/androidTest/kotlin/com/passvault/android/audit/AndroidClipboardLocaleInstrumentationTest.kt`.
Candidate SHA-256 `a2ed69836a68a1291f6673cc87ffc708cfc1594fc0387381585ff08fa067dbf1`, 22,445 bytes / 407 LF.
It contains exactly two prospective fixed instrumentation cases, not two executed tests:

1. `realClipboardOwnershipAndForegroundRetry`
2. `frameworkLocaleRecreationAndExplicitToSystem`

The checked-in AGP Android32 harness remains untouched:
`core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt`,
SHA-256 `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310`.
Only its bare `android.app.Instrumentation` fixed-inventory/status pattern informed this
new file. No AndroidX test runner, test dependency, replacement Activity/manifest,
cryptographic probe, identity, application version or production source change was added.

The directly readable handoff `current/issue-to-fix.json` (under `docs/audit-handoff/`)
has raw SHA-256 `5782666330bc2eb017e42b7a8725db1efdf54223462f3648c8acf25cb21b245e`.
Its PVA-009 and PVA-030 current statuses remain **IMPLEMENTED — TARGET RUNTIME
VERIFICATION BLOCKED**. Existing six fake clipboard methods and historical locale
static predicates/negative comparison remain qualified historical evidence, not fresh
results or replaced by this candidate. No closure denominator changes.

The inspected read-only `evidence.py show` refused `file type/size/links` when trying
to read the original PVA-009 packed finding: this source transport presents directories
on device 23 and regular single-link files on device 24. The refusal was preserved;
no helper retry/modification, alternate packed reader, archive extraction, or archived
code import/execution followed. Root authorized using the detailed current issue rows
for this prospective test-only scope. The packed original independent reviews were
**not freshly read**. Their current ledger dispositions are not represented as new
independent review. Focused localization/RTL and mobile identity instruction snapshots
were read; no generated skill mirror or unavailable authoritative repository was edited.

## Intended observations, not new guarantees

**PVA-009:** Actual `MainActivity`, real application/Koin `AndroidClipboardService`,
and real `ClipboardManager`. First positively identify a newly written synthetic
owned clip. Replace it with identical text/different synthetic label through the real
manager, and assert explicit service cleanup preserves that replacement. This same-UID
replacement control does not establish an external-copy boundary or timeout behavior.

Next copy a new owned clip, move the real Main task to background, and require real
stopped/unfocused state and an unavailable (`null`) manager read. After at least 5.5
seconds of real elapsed time, require the unavailable interval and retained cleanup
ownership, then return the same Main through real platform resume/focus callbacks.
No `service.onForeground()` or manually delivered Activity lifecycle callback is used.
A pass requires own cleanup authority retired and an empty clipboard after actual
foreground. A surviving original clip is an assertion failure; an unobtainable owned/
unavailable interval is an explicit evidence-precondition error, **never a pass/skip**.

`null` can mean absence as well as access denial. The real `onStop` coordinator can
request cleanup before the timer: elapsed time does not prove timer causality. An OS
or instrumentation target that cannot expose the required interval provides no PVA-009
runtime success. API 24–28, OEM behavior, history, external UID revocation and measured
expiry-cause/latency remain outside this API29+ fixed selection.

**PVA-030:** The public framework `Instrumentation.newActivity` hook supplies a
per-Activity EN/AR `Configuration` before attach/theme/resource access, without a
replacement Activity or injected Compose locals. Actual `MainActivity.recreate()`
must retire one captured instance and create/focus a different one in the same
instrumented process. The real singleton `SettingsViewModel` receives EN/AR/SYSTEM
events. Production `PassVaultApp`/Android language provider must update both actual
process defaults and the published/default-input app-authored biometric prompt strings
to independent English/Arabic literals. No expected strings are supplied to the adapter,
no locale setter is used to satisfy an observation, and no biometric prompt is launched.

This is a test-controlled **Activity resource configuration/recreation** observation,
not a device-language Settings change or proof of real system locale propagation. It
does not measure Compose direction/geometry, rendered native prompt language or physical
security. Initial process-local EN seeding is an explicit control before the first Main
composition, not genuine device cold-start locale evidence. Preference readback is from
the real synthetic store; matching data does not by itself prove save-job or all storage
settlement. No product compatibility/design boundary was redesigned.

## Admission and cleanup boundaries

The entire Android device/user/clipboard and fresh ordinary Debug application storage
must be independently admitted **before instrumentation launch**; Android can initialize
Application/Koin before `onStart`. Device ownership also has to cover host/emulator
clipboard-sharing paths. An application ID does not isolate the device clipboard.
The required acknowledgement argument and runtime `com.passvault.android.debug`,
`BuildConfig.DEBUG`, `BUILD_TYPE == debug`, and non-storeScreenshot checks are secondary
refusals, **not proofs of freshness, device identity, authority or privacy**.

The test never reads/restores prior clipboard data. Only expected synthetic clipboard
labels are cleared; unknown replacements are preserved with a cleanup failure. Captured
Main instances are finished, all must report destroyed before original process LocaleList/
Locale restoration, and the new-Activity override is cleared in `finally`. Cleanup errors
are retained and prevent terminal instrumentation success. Koin/Room are not replaced
or directly shut down by the test; external owned app-process/device settlement and
allowlisted synthetic storage disposal remain mandatory and explicitly unproved.

Each case has a 90-second cooperative timeout, condition waits are 10 seconds, and owned
main-thread postings have 5-second waits/removal for still-queued callbacks. Cleanup has
a separate 30-second cooperative timeout. None can force-settle a blocked Binder/native
call or already-running main callback; a separately reviewed external hard bound and
failure/cancellation cleanup must exist before any invocation. Case failure stops the
fixed selection: unstarted cases remain absent, not invented passes/skips. Terminal
counts, raw status transcript/XML, exact APK/source identity and outer cleanup evidence
must be independently reconciled before accepting actual test results.

No new runner was executed/imported. No SDK/license/emulator/ADB/network/CI/Git operation
occurred in this lane. No caches/build outputs/background application processes were
created here; only small permanent test/source-review files were authored.

## Effective build/API gaps

`app-android` has no instrumentation runner wiring yet. Root owns any future narrowly
reviewed `testInstrumentationRunner` wiring. Actual AGP test classpath, Android internal
friend visibility for `androidBiometricPromptText`, public pre-resource Activity hook,
Main task refocus behavior and runtime observations are uncompiled/unexecuted software
and execution-admission gaps, not fabricated hardware failures. No dependency change is
presumed necessary or authorized. The reviewer must challenge these before adoption.

All STOP/NO-RETRY/CLOSED and newer held runtime/storage scopes remain unchanged. This
source candidate permits no automatic replay/retry, publication or new execution scope.
