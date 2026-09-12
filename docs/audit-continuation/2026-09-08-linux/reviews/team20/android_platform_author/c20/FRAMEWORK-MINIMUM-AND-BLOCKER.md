# C20 Android PVA-009 / PVA-030 — minimum useful framework work

Author `/root/android_platform_author`, 2026-09-11. **Source-only proposal and
feasibility disposition; no execution, target admission, product correction or
closure.** Independent challenger: `/root/android_platform_review`.

## Decision: reuse the accepted two cases, not another runner

Read the handoff entry, START_HERE/PERMISSIONS/ASSEMBLY, repository AGENTS, C19,
pertinent C14/C18 and current ledger entries, supplied secure-platform-biometric-
unlock/localization instructions and references, C14 fixture acceptance, and C16
fixture delta review. Historical commands and platform instructions remain
dormant. No packed-evidence reader or stopped/refused procedure was reopened.

The existing source already supplies the smallest **wired** actual-framework
selection without weakening its inventory: two serial fixed instrumentation
cases. This is not JUnit source, and its argument guard rejects filters. Do not
add a generic runner or silently run only one case under the old two-case claim.

| Existing case | Useful observation, if separately admitted and actually completed | Still not established by it |
|---|---|---|
| `realClipboardOwnershipAndForegroundRetry` | Real Main/Koin/ClipboardManager; different ownership label with identical synthetic text survives explicit clear; two stopped/unfocused/null endpoints span a real wait; retained cleanup retires after actual foreground/focus | Continuous access denial; private-timer or exclusive callback causality; external-UID replacement, clipboard history/OEM behavior, API24–28 |
| `frameworkLocaleRecreationAndExplicitToSystem` | Same-PID real Main recreation; actual singleton SettingsViewModel/store and EN/AR/SYSTEM transitions; current process defaults and production default-input prompt strings follow the supplied Activity configuration | Device-global configuration propagation, cold process, rendered RTL/assistive behavior, displayed prompt, enrollment or physical security |

The two methods, guard, timeout values, readiness observation and cleanup must
remain unchanged unless a genuine independently challenged source/oracle defect
is found. This review found no such defect in the examined production/fixture
path. Existing non-pass outcomes are not reasons to weaken the fixture:

- Actual readiness must precede test activity. No bootstrap/open/retry call or
  fallback accessibility driver may replace the bounded read-only onboarding
  gate. The real merged button/heading mapping remains unobserved.
- An unavailable/null clip is not proven absent. Missing retained ownership or
  background endpoints must remain non-pass; onStop itself may request clear.
- Per-case status success is insufficient. Require the exact fixed inventory,
  successful terminal result, in-process cleanup, no interruption/failure, and
  independently reconciled outer target/UiAutomation/app-worker/storage cleanup.
  Native stalls or an already-running main callback are not settled by a timeout.
- Application/Koin can start before in-test guards. The whole synthetic target,
  Debug storage and clipboard—including host sharing—must be admitted **before**
  instrumentation launch. Neither a package name nor an opt-in token proves this.

C19 Compile03 establishes crypto main/device-source compilation only. It did not
compile this app instrumentation source or produce its APK/framework outcomes.
The current two-case fixture is the accepted C16 decomposition of the C14 fixture;
the inspected C18/C19 material does not supply new execution credit for it.

## Exact source/data identities read

Paths are relative to W. Hashing here was a passive source/data read, not project
execution, a hostile-mutation freeze or an application correctness proof.

| Path | SHA-256 |
|---|---|
| `app-android/src/androidTest/kotlin/com/passvault/android/audit/AndroidClipboardLocaleInstrumentationTest.kt` | `580c71e5fc5c917b7f62535282fa3436a3eada15e35ac7afa3bcd584f890c29d` |
| `app-android/src/main/kotlin/com/passvault/android/security/AndroidClipboardService.kt` | `db1b9e5387e7059719e0f49b4a8c9340f8abcf210d16a808a5408c17443e00cf` |
| `app-android/src/test/kotlin/com/passvault/android/security/AndroidClipboardServiceTest.kt` | `b4a69761ef3f6920ee586f070b5a22a7d552151850bce2718e1a7c06b5c91b64` |
| `shared/src/androidMain/kotlin/com/passvault/shared/platform/AppLanguageProvider.android.kt` | `a6bbb6c47d27b1e79b326152f5c091b80909839f84240fa23cd4dc9c61c57edc` |
| `app-android/src/main/kotlin/com/passvault/android/MainActivity.kt` | `f8842b501b62cbf2e621f667c05358aee1f9d73c4cf468f6ee7815d690d1b82f` |
| `app-android/build.gradle.kts` | `c7a382773cb11383d583b6fd1dcead85aac30ccc0a38f81995f535dc1029da42` |

The complete fixture, ClipboardService/host test, locale provider and Main were
read. Surrounding bootstrap/settings/DI/lifecycle/prompt code was examined in
focused excerpts: same singleton SettingsViewModel; application configuration
via LocalConfiguration rather than overridden process default; Main resume/focus
retry; lifecycle clear; actual helper default argument rather than test-supplied
prompt strings. No full-project or whole-file coverage is claimed for those
surrounding excerpts. Some broad report output was truncated and a few guessed
source paths were absent; only actual bounded reads support this disposition.

Prior independent decisions retained:
`reviews/editor/PVA009-PVA030-INSTRUMENTATION-INDEPENDENT.md` and
`reviews/editor-independent/ANDROID-CLIPBOARD-LOCALE-DETEKT01-INDEPENDENT.md`,
relative to B. Their source acceptance is not current target admission.

## Existing 64-bit metadata does not establish a usable target

Coordinated with Android32 lead `/root/android_compile_author`; no new probe was
requested or performed. Read only the existing retained report excerpts:

- `B/reviews/android32/REPORT.md`, SHA-256
  `b800ecf4e48dc51eee34ca5bfb6ede09d55ac2b9fa7d555910fd7d1c3fd660e5`,
  lines135–157 and211–229: existing image metadata advertises API35/revision2/
  x86_64, an empty 32-bit ABI list and no native bridge. This can satisfy the
  fixed pair's API29+ requirement **on paper**, not a current boot/usage/isolation
  admission. The historical no-KVM observation and emulator version metadata
  do not establish software emulation either feasible or impossible today.
- `B/reviews/android32/ANDROID32-ADMISSION-PREREQUISITES.md`, SHA-256
  `5c2966372abb1aebb56175a7062e4ceef8fa990875a2c6d332c9b136e3397089`,
  lines60–82: an existing 41-byte SDK-license marker is metadata, not proof of the
  authorized principal's acceptance for this precise component/use. Do not run
  a license command, manufacture agreement or infer permission from compilation.

No accepted usable 64-bit guest, current target isolation, or owner license/use
scope was supplied in this lane. **Execution is genuinely target/admission
blocked.** The minimum missing root facts are:

1. A currently authorized synthetic API29+ target and exact existing image/tool
   usage rights; root determines whether an already-documented acceptance covers
   it, rather than assuming a new download or blanket agreement is needed.
2. A fresh whole-device/user/host-clipboard isolation boundary and fresh ordinary
   Debug app data before Application startup; no installed personal device,
   installed Store app, personal clipboard, account or vault access.
3. Exact source/build/APK/target and synthetic-storage identities, resource and
   sole-slot admission, and independent bounded startup/success/failure/
   cancellation/worker/framework/storage cleanup review. Root owns all actions.

There is no need to import Android32's separate prospective old-image/32-bit
requirements into this API29+ slice. A 64-bit framework result would give no
KDF/ABI/ARM32, minified artifact or physical-device proof. Conversely an API24
32-bit target could not run this unchanged fixed pair. Share only an actually
compatible and separately admitted target; do not duplicate KDF work.

## Safe distinct progress while blocked

`CLIPBOARD-RECOPY-WITNESS.UNAPPLIED.patch.txt` proposes one host-only method in
the existing ClipboardServiceTest; `CLIPBOARD-RECOPY-WITNESS.md` explains its
independent oracle. It targets recopy after a pending unreadable clear, absent
from the six inspected existing methods. It is **coverage-only**, not a new
finding, production fix, adopted case, framework pass or extra runner. It remains
inert pending independent review/root decision, with zero compilation/execution.

No production/test/helper/central-ledger source was edited. Only this lane's new
C20 report/proposal files were written. No Git, build, CI, test, SDK/ADB/device,
network, app/helper import/execution, runtime/process probe or cleanup occurred.
No held runtime was traversed. No source/dependency/version/application identity
or build1017001 changed. STOP/NO-RETRY/CLOSED/HOLD/native-refusal, PVD/hardware and
publication limits remain; PVA-009/PVA-030 status and all closure counts stay.
