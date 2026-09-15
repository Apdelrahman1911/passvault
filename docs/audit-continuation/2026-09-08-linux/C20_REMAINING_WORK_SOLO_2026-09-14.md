# C20 remaining work — September 15, 2026

**INCOMPLETE / NOT RELEASE READY.** This is a continuation, not a new audit.
Linux host; root is the sole implementation/build/CI/cleanup owner, with one
independent reviewer (`current_ledger`). Protected refs, tags and build1017001
remain unchanged. No signing, production publication or Store action is authorized.

## Latest engineering and verification

- Windows05 ran **both after-flush selectors in one native CMake build** on
  Windows2022/MSVCx64/SDK10.0.26100.0. Raw XML records **2 PASS**, no failures,
  errors or skips. Independent actual review and root adoption accept these two scoped cases.
- The cases exercise real FlushFileBuffers, share-zero read exclusion, unchanged
  destination/sentinel before release, normal replacement, and controlled child
  death retaining the original destination and owned flushed staging bytes.
  Each native fixture validated identities/content before its own cleanup.
- **Overall CI FAILED:** outer cleanup refused a reparse entry at
  `home/AppData/Local/Microsoft/Windows/INetCache/Content.IE5`; no target read or
  deletion. Original Job termination/zero is recorded, but whole-runtime cleanup
  is **HOLD**, not a pass. No replay, recovery, probe or deletion is authorized.
  The cause/target of the reparse entry is not established.
- Source commit `bed177cadebe79fdc8aec9781a3510e598d3d59c`, tree
  `b882d28b4bdce19f254491e4956e73d56c9a438f`; request-only activation
  `0f3b0c4ce912098a7eed353116b631a6e474422f`; original run **34919661732**, attempt1.
  Compact33files/78737bytes retained; transport ZIP and original observer
  temporary HOME/TMP directories removed. No Gradle invocation/stop obligation.
- GUI25 focus/readiness correction is independently source-reviewed and installed,
  **not executed**. GUI24's one failed case and Image03's binding failure remain
  preserved. Image04 reached source staging but failed its8GiB disk guard before
  Gradle: zero cases; whole runtime remains HOLD.
- Already accepted Linux Main/Room and EN/AR/EN tray/Lock/Exit checks, macOS real-JNA
  lifetime check and five ASan/UBSan cases are reused, not rerun unchanged.

Evidence: `reviews/team20/resume_disk/root/windows-after-flush-ci05/` and
`publication04/windows-activate-instance05/`; report29 and prior canonical ledger
bytes are preserved under `reviews/team20/resume_disk/root/report-refresh30/PREVIOUS-*`.
The live scheduler is `reviews/team20/resume_disk/root/EXECUTION_SLOT.json`.

## Progress — separate denominators

- Confirmed families: **27/39 qualified closures (69.2%);12 open**.
- Original confirmed families: **19/25 (76%);6 open**.
- Original suspicions: **2/12 conclusively resolved (16.7%);10 open**.
- Eight original design explanations are documented; **owner decisions separate**.

No percentage measures overall readiness. Windows05 adds2 independently accepted passing XML cases, not a family closure.
The previous268 mixed XML become270 after adoption:266 application/native/regression/investigation/
control +3 infrastructure +1 producer. This is **not270 passes**. Prior failures,
zero-case attempts and separate non-XML controls remain separately classified.

## Every open confirmed family

All12 have implemented/source-reviewed correction code. That is not closure:
missing or failing material evidence may reveal further required code changes.

| ID | Exact remaining implementation/verification boundary |
|---|---|
| PVA001 | Supported32-bit Android native loading/KDF known answers and legacy strictUTF8→lowercasehex compatibility; synthetic create/unlock/backup. Actual minified/native output and scoped DEX/ELF metadata already accepted; not runtime proof. Explicit ARM32 coverage required. |
| PVA007 | Remaining dirty Back/tab/Add, rapid/pre-frame and delivered keyboard/IME, accessibility/RTL and mobile lifecycle editor contracts. Reuse accepted Room, failed-Save acknowledgement and GUI16 Main/Room evidence. |
| PVA008 | Seven prepared iOS attachment cancellation/adoption methods, then applicable physical-device file protection/provider/lifecycle evidence. |
| PVA009 | Actual Android ClipboardManager unavailable-read, recopy and cleanup ownership; host-injected tests do not prove framework behavior. |
| PVA010 | Active-native cancellation/late return, concurrency/provider and packaged lifetime; reuse scoped macOS real-JNA and five ASan/UBSan passes. No universal leak/TSan/hardware claim. |
| PVA014 | Prepared Apple prompt-property check and actual displayed prompt language/propagation on supported targets. |
| PVA027 | Installed-package and remaining supported-platform tray language validation. GUI20 development-Linux EN/AR/EN, tooltip, native Lock/Exit and Room evidence are already accepted. |
| PVA029 | Permitted bounded diagnosis of five retained capture-cleanup failures (44PASS/5FAIL out of49), any necessary repair, discriminating verification under fresh authority. No automatic retry of original helper; exact signal/wait causes unknown. |
| PVA030 | Actual Android system-locale change and application lifecycle refresh; batch with PVA009 on suitable isolated target. |
| PVA031 | Real Main/input, rapid/pre-frame, rejection, accessibility/RTL and mobile draft-retention contracts shared with PVA007. No retention guarantee after legitimate owner disposal. |
| PVA036 | Windows05 normal-release/controlled-child-death evidence independently accepted. Remaining owned TMP/TEMP crash/application-cancellation cleanup boundaries are separate. Neither universal power-loss durability nor orphan recovery is proved. Whole CI runtime cleanup failed separately. |
| PVA037 | Permitted whole-create/retrieve-caller post-PRF AAD and post-KDF AES/later allocation-cut evidence; existing guard4/actualPRK2/KDF1 are insufficient for all callers. No new provider/refactoring/instrumentation seam currently admitted. |

## Other unfinished work

**Suspicions, not confirmed defects:** PVU001 production caller/provider lease
settlement; PVU002 durable DAO post-lock-delete witness (prior refusal preserved);
PVU003 actual chooser/Home/cancel (GUI25 prepared, disk blocked); PVU004 iOS
interactive gesture/Back/scene and LTR/RTL keyboard timing; PVU005 remaining
backend lock curtain; PVU006 Room/terminal late-publication tail (refusal preserved);
PVU008 provider content/destruction contract (no experiment admitted); PVU009
native cancellation latency. **PVU007 STOP and PVU011 NO RETRY remain unresolved**;
they must not be investigated/reformulated. PVU010/PVU012 accepted outcomes remain.

**Integration/packaging:** finish Android framework preparation/final-package
correspondence after interrupted ACTION07 (no retry authorized); installed Desktop
image/launch/native lifetime and legal-document/native-carrier/OpenJDK notice
correspondence; remaining Apple/Windows target evidence; supported artifact
provenance and source/overlay/schema/resource/dependency/workflow reconciliation.
JNA Apache2.0 and FreeType FTL are already selected; no invented blanket licensing
blocker. Publisher-controlled SUPPORT_EMAIL is needed before a future Debian
release, not general compilation. Actual publication is outside this work.

**Eight PVD choices:** metadata/database protection, future versionedKDF redesign
(legacy compatibility mandatory now), best-effort memory erasure, iOS clipboard/
background tradeoffs, external-viewer plaintext, optional new-enrollment TOTP
minimum, transactional restore/compensation versus durable recovery protocol,
and ownership/resources or explicit deferral for required platform evidence.
Do not silently redesign these boundaries or count deferral as a passed test.

## Blockers and what the owner must supply

| Blocker | Needed resource/decision |
|---|---|
| Local Linux chooser/installed image | More free disk: observed about7.9GiB;8GiB inner floor must still hold **after** source staging (~0.3GiB), preferably restore12GiB. Add roughly5GiB available capacity or identify positively disposable non-HOLD data. No shared/uncertain/HOLD files will be deleted to force admission. |
| Android32 | Isolated ADB-accessible supported32-bit target, OS/API/ABI details; explicit ARM32 coverage. Alternatively verified applicable SDK-image license authority and usable approved image/runner.64-bit-only is insufficient. |
| Android framework | Licensed isolated emulator/device. Physical Keystore/biometric claims additionally need a suitable physical device and interactive tester. |
| iOS software | Eligible adequately provisioned Apple-Silicon/Xcode/JDK17 host or repository-accessible runner and simulator. Prior7GiB runner failed memory admission; no adequate replacement allocation established. Ten prepared methods:7attachments +prompt property +VEK roundtrip +wrongKEK rejection. |
| Physical Apple | Supported iPhone/iPad with OS/device details and tester for protection, biometrics/provider, scene/input; hardware keyboard where applicable. |
| Interactive Windows | Isolated supported Windows with configured Hello, provider/version and documented backing-content lifetime, plus tester. Hosted CI does not prove it. |
| Restricted witnesses | Permitted narrow mechanism or independently produced synthetic evidence for PVA029/PVA037 and refused investigations. No blanket authority overrides STOP/NO-RETRY/CLOSED/tool refusals. |
| Product scope | Supported-platform/distribution scope and dispositions for eight PVD choices; verified public SUPPORT_EMAIL before Debian publication. Omitted authoritative validator location only if indispensable; it has not been supplied/run. |

All G7/G8 and WindowsGraph03 CLOSED scopes remain closed. Their blockers are not
merely hardware shortages. Historical held workspaces cannot be reclaimed without
the required separate authority; later runs do not discharge old obligations.

## Schedule, estimates and definition of finished

One heavy job at a time; source work/review/preparation overlap it. Batch related
boundary checks, retain failures, and reuse unaffected successes. Windows05's
native case durations were about0.046s and0.038s; compilation/runner setup and
cleanup are separate. A short successful case is not a successful CI cleanup.

- **Current feasible closeout:** Windows actual review is complete; compact ledger
  reconciliation and dedicated-branch publication remain. Roughly30–90minutes
  hands-on if no new discrepancy; not an overall completion estimate.
- **Linux chooser/image after disk relief:** previous compile/render cycles about
 10–20minutes each. Several hands-on hours are plausible if prepared checks pass;
 native-focus and installed-image behavior remain uncertain. No unconditional date.
- **Android/iOS software:** target allocation, license/toolchain and build runtime
  are unresolved; measure the first admitted target cohort before estimating the
  remainder. Preparation is not execution.
- **Remaining native/provider/restricted witnesses:** mechanism/authority and
 scope must be settled before a reliable engineering estimate. Physical testing
 has unbounded external waiting until devices/testers/contracts are supplied.

The old1–3hour estimate covered only a success-assumed Linux/Desktop slice. It
excluded whole-project verification, physical devices, owner decisions and release
readiness. No defensible overall date exists until these specific unknowns resolve.

Actionable engineering is finished only when all permitted feasible fixes and
material checks have independently accepted source-bound evidence, owned workers
are settled, safe disposable cleanup is complete (or explicitly blocked), and
handoff/reconciliation is current. **Open or blocked is not complete.** Release
readiness additionally needs applicable compatibility/security/platform/artifact/
legal/physical gates and owner dispositions for agreed scope. Store approval,
production signing and publishing remain separate and unauthorized.
