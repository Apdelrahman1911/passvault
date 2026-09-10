# PVA-009 / PVA-030 — finite corrected instrumentation source delta

Author: `/root/android32`; independent source challenge: `/root/editor` (separate
verdict required). This note supersedes the original candidate's readiness, interval
and cleanup descriptions, not its preserved rejection or execution limitations.
**Zero compilation, builds, instrumentation cases or runtime probes executed.**

## Exact inputs and preservation

| Input | SHA-256 | Bytes / LF |
|---|---|---|
| Current `app-android/src/androidTest/kotlin/com/passvault/android/audit/AndroidClipboardLocaleInstrumentationTest.kt` | `3762edc0f9ac2d26ba9ab68249a55004fd4cb3f08e01a2d337d23d3fbb1aab1f` | 28,533 / 504 |
| Rejected source retained here as inert `ANDROID-CLIPBOARD-LOCALE-REJECTED-A2ED698.kt.txt` | `a2ed69836a68a1291f6673cc87ffc708cfc1594fc0387381585ff08fa067dbf1` | 22,445 / 407 |
| Unchanged historical author note `ANDROID-CLIPBOARD-LOCALE-INSTRUMENTATION-CANDIDATE.md` | `dc0146d197a217984818c130852ec2d22399f8afe3c139a144ca037f37e8e914` | 8,425 / 123 |

The rejected bytes were the permanent test-path input to initial review. The retained
snapshot was renamed from a newly created `.kt` archive to `.kt.txt` for inert
publication; no independently reviewed or published archive input was discarded.
The original author note describes **rejected a2ed698**, not the current fixture.
That rejection was a source-review result, not an executed Android failure. Raw hashes
and LF counts describe the Linux checkout files, not newly established runtime evidence.

The inventory remains exactly two prospective cases:
`realClipboardOwnershipAndForegroundRetry` and
`frameworkLocaleRecreationAndExplicitToSystem`. No third harness/case, production,
dependency, manifest, identity, version or SDK change was added. Root alone owns any
separately reviewed Gradle instrumentation-runner wiring and publication source binding.

## Finite changes requested by independent challenge

1. **No active bootstrap retry.** Removed the duplicate `openAndVerify()` probe and
   bootstrap imports. The first focused real Main must instead expose ready-only
   English onboarding, before clipboard/background/recreation/ViewModel lookup.
   Root expressly authorized this one bounded read-only framework UiAutomation gate,
   not general UI driving. `FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES` is the sole flag;
   every node's exact Debug package/window is checked before text/child traversal.
   Limits are 128 nodes, depth 20 and 32 children; one visible heading `Welcome to
   PassVault` and one visible enabled clickable `Get started` are required. Missing,
   wrong, oversized or ambiguous evidence cannot pass. Acquired nodes are recycled;
   recycle errors cannot overwrite the primary traversal failure. No action/input,
   listener, serviceInfo mutation, shell, permission-adoption or hidden/reflected
   disconnect/destroy API was introduced.
2. **Preserve failure and interruption.** Callback-unregister failure is independently
   caught and suppressed behind prior cleanup failures, never substituted for them.
   Self-suppression is guarded. Static failure phases and a bounded primary/suppressed
   class tree are retained without arbitrary exception messages or clipboard data.
   Interrupt observation prevents success; clearing it is limited to attempting
   cooperative cleanup, with restoration after terminal reporting. Terminal aggregate
   retains the original primary with cleanup failures attached.
3. **Exact process-locale restoration.** Snapshot LocaleList, general default, its
   selected list index, DISPLAY and FORMAT defaults. Reject an absent index before
   any mutation. Restore with `LocaleList.setDefault(savedList, savedIndex)` so a
   nonzero selected default cannot silently reorder the saved list. Mark mutation
   before its first attempt; do not mutate defaults
   during cleanup if setup never touched them. After every captured Main is destroyed,
   restore and verify all four. Clear the per-Activity override in `finally`. No
   device-global locale setting or production restore mechanism is changed. Reviewer
   identified the nonzero-index edge in intermediate source
   `e56a01a791b6b5aa566d6d366fc493639fe51a181ce724f3df8220f4d0fd28e0`
   (28,244 bytes / 501 LF); that intermediate was not accepted or executed. The final
   delta is one index field, two pre-mutation validation lines and indexed restore.
4. **Partial replacement-write cleanup.** Register the known synthetic replacement
   label before attempting the manager write. Unknown replacement labels remain
   preserved with a cleanup error rather than cleared speculatively.
5. **Correct real-wait origin and narrow oracle.** The 5.5-second wait starts only
   after actual stopped/unfocused state and the first null manager sample. Recheck
   stopped/resumed/focus and null at the second endpoint before sampling retained
   ownership. Record `pva009.backgroundEndpointIntervalMillis` and
   `pva009.unavailableEndpointsThenForegroundRetirement`. This proves neither continuous
   unavailability nor timer causality: lifecycle clear and OS-empty reads remain
   alternatives. Same-text/different-label control still exercises explicit clear only.

## Readiness source binding and external connection boundary

`shared/src/commonMain/kotlin/com/passvault/shared/PassVaultApp.kt`
(`75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737`)
performs the ordinary startup bootstrap and admits `VerifiedPassVaultApp` only for
`Ready`; actual vault-existence resolution must select `AuthRoute.Onboarding`.
`shared/src/commonMain/kotlin/com/passvault/shared/navigation/adapters/AuthRouteAdapters.kt`
(`3d2541f8751a562ede692bae7f6e4f8b91322dcc6d84d8457bb3ca59a14609f3`)
maps that entry to `OnboardingScreen`, whose production source
`feature/onboarding/src/commonMain/kotlin/com/passvault/feature/onboarding/ui/OnboardingScreen.kt`
(`32220a096262954a626c29e7958cc11c915c708908ff5ec966aba9307a433fb9`)
delegates to `WelcomeScreen`. Its production source
`feature/onboarding/src/commonMain/kotlin/com/passvault/feature/onboarding/ui/WelcomeScreen.kt`
(`cb536ff30bfca9678da8c0c843a57da35f4ae2236db484aafb23f8ab1385d290`)
provides the welcome heading semantics and start button. The independent English
literals match `core/designsystem/src/commonMain/composeResources/values/strings.xml`
(`5605f102cca37a9fddf7fe50ecd598c5460168fd9eb6149d4d7a8318f644b118`).
Actual Compose accessibility exposure (including merged clickable button text) and
simultaneous visibility within the admitted viewport remain unexecuted, not assumed
successful; this read-only gate will not scroll/click or weaken those predicates.

UiAutomation's connection is owned by instrumentation, not a fixture-created public
closeable. `passvault.axConnectionSettlement` explicitly records
`INSTRUMENTATION_FINISH_AND_OUTER_OWNER_REQUIRED`. Neither recycling nodes nor
`passvault.inProcessCleanup=true` proves connection release. Ordinary finish, failed
finish, blocked Binder/native calls, cancellation and process/device settlement need
fresh independently reviewed **external** ownership, hard bounds and cleanup admission.

## Retained limits and status

Whole synthetic device/user/clipboard (including host sharing) and fresh Debug storage
must be admitted before instrumentation can initialize Application/Koin; argument and
package guards prove none of those boundaries. No prior clipboard is read/restored.
Locale evidence is actual Main recreation with test-controlled per-Activity framework
configuration, real SettingsViewModel/store readback and default-input production prompt
strings, not device-global locale propagation, Compose RTL geometry, displayed biometric
UI, save-job settlement or hardware security. API29+ endpoint observations do not close
API24–28/OEM/external-UID/history gaps. Existing cooperative 90-second case, 10-second
condition, 5-second main-post and 30-second cleanup bounds do not replace an outer bound.

The one inspected `evidence.py show` refusal (`file type/size/links`, source transport
directory device23/file device24) remains preserved: no retry, alternate packed reader,
extraction, import or guard relaxation occurred. Root allowed detailed current ledger
rows for this prospective scope; packed original reviews were **not freshly read**.
PVA-009/PVA-030 remain **IMPLEMENTED — TARGET RUNTIME VERIFICATION BLOCKED**; no closure
denominator, historical result, PVD design limitation or STOP/NO-RETRY/CLOSED/held scope
changes. No Git, CI/network, SDK/license/emulator/ADB, worker/process probe or runner
execution/import was performed by this lane. Only small permanent source/review files
were written; no build outputs, caches, runtime temporary files or background jobs
were created. Independent source acceptance, if granted, is not execution admission.
