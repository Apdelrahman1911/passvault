> **Update26:** GUI21 stopped before Gradle at a validation input-map KeyError.
> Zero test cases or XML; no Gradle stop was owed. Independent review accepted
> only the original pre-build failure and logical-slot retirement. Its runtime
> remains HOLD/NOT DELETED; namespace-empty was not measured. GUI22 input transport
> correction is in source review, not a passing test. Windows02 seed-stage diagnostic
> source is independently accepted and installed; narrow source publication is in
> progress. Current counts remain263 mixed XML /27 of39 qualified closures.

> **Update 25 (2026-09-14):** The 1,696-file continuation checkpoint was pushed as
> `b88856529bb276fab7db988410658c32185b8de8` (tree `597ace3ecb5c2fc744b3d62d13703cc24c14beaa`).
> Restricted Windows batch `34901080019` compiled successfully, but its first
> synthetic seed write failed before the after-flush boundary: **1 XML FAIL,
> second case UNSTARTED, zero after-flush passes**. Independent review accepted
> the evidence and original worker settlement; the entire failed runtime remains
> **HOLD / NOT DELETED**. No automatic retry or family closure.
> Current accounting: **263 mixed XML elements, not 263 passes**; 259 application/
> native/regression/investigation/control + 3 infrastructure + 1 producer.
> Separate non-XML controls: 6 screen + 4 terminal-policy + 8 disposal-policy.
> Linux chooser probe/inline fixture source is independently reviewed; fresh
> GUI21 original invocation39817 ended before Gradle; see Update26. Successful GUI20
> and macOS native/JNA/sanitizer checks are not being repeated.
> The remaining work below is still applicable except older Windows “not yet run”
> and publication “not yet pushed” descriptions, which this update supersedes.
> **27/39 confirmed closures; 12 open. INCOMPLETE / NOT READY.**

# C20 remaining work — current continuation, 2026-09-14

**INCOMPLETE / NOT READY.** Root implements and owns all builds/Git/cleanup; one agent reviews only.
This document replaces stale scheduling prose; previous exact report is retained in
`reviews/team20/resume_disk/root/report-refresh24/PREVIOUS-C20_REMAINING_WORK_SOLO_2026-09-14.md`.

## Current accepted evidence

- **27/39 confirmed families have qualified closures (69.2%); 12 open.** Original families: **19/25 (76%)**.
  Original suspicions: **2/12 conclusively resolved (16.7%)**, ten unresolved.
  Eight PVD explanations are documented; owner decisions are separate. These are not overall readiness percentages.
- **263 mixed XML elements, not 263 passes**; six screen-control, four terminal-observation and eight Windows disposal-policy controls remain separate (all nonXML). The five new sanitizer XML cases are two native and three filesystem-fixture cases.
- GUI16: three actual cases, **one Main/native dirty-editor + durable Room reopen PASS; two Settings-lookup FAILs**.
  Main uses paired nonshipping readiness instrumentation. This is not full editor/mobile/installed-image closure.
  Both original wrapper stops succeeded; 28,231/28,231 runtime entries removed.
- GUI17: **compilation failure, zero XML and zero GUI cases**. The revised test fixture inferred a nullable
  traversal variable. One-line nonnullable correction was independently source-reviewed; GUI18 subsequently crossed this compilation barrier and reached different runtime guards. Its failures remain recorded.
  Original wrapper stop succeeded; 26,933/26,933 runtime entries removed; failed run independently adopted.
- Android04 R8/native producer and scoped DEX/ELF metadata evidence remain accepted and will not be repeated.
  Framework/package ACTION01 failed before task actions (zero compiler/package/runtime credit); cleanup accepted.
  ACTION02 also failed before task actions: premature carrier provider realization before Android resource generation.
  Original stop succeeded; 15,199/15,199 runtime entries removed. ACTION03 traversed that guard but failed File-versus-Path conversion; accepted cleanup. ACTION04 traversed the correction and failed primitive byte[].take header inspection before the compiler. It ran164 task actions/zero test cases; original stop succeeded and40018/40018 disposable entries were removed. Both header sites now have independently reviewed Arrays.copyOf corrections. ACTION05 was rejected at its first launcher guard before allocation or any build because root omitted the required clean-environment wrapper; exit70/HOLD/NOT_ATTEMPTED and zero cases are retained, independently adjudicated. ACTION06 reached compilation but failed on a hidden two-argument LocaleList.setDefault API in the fixture (165 tasks, zero cases); stop0 and40037/40037 entries removed, independently accepted. Public one-argument restoration plus pre-mutation index-zero precondition is independently reviewed and installed. ACTION07 was interrupted by SIGTERM during startup (origin unknown), exit70/action143, zero cases and no compiler/package success. Original stop0/namespaceempty; source-after acceptance absent and cleanup HOLD/NOT_ATTEMPTED. Independent actual review accepted the interrupted observation and logical-slot retirement only; no automatic retry. No ACTION05 replay occurred.
- GUI18: corrected fixture compiled in 7m28s; two actual cases both FAILED (tray geometry stability; chooser focus).
  No crops. Both original wrapper stops succeeded and original GUI/namespace settlement is recorded; independent actual review has been adopted.
  Outer cleanup hit a stale three-case assertion in its two-case scope: runtime HOLD / deletion NOT_ATTEMPTED.
  Do not erase this failure or delete the HOLD runtime. A proposed focus-delay-only repair is not established and will not be blindly rerun.
- macOS ordinary-dylib + existing real-JNA pre-provider lifetime check: run34885844060 passed one actual case and original cleanup, independently adopted. Native/JNA scope only, not full PVA010.
- GUI19: one actual XML case PASS; property chain, native Lock/Exit and endpoint Room oracle independently accepted. Four visual crops accepted, but the final English tooltip is blank: full visible EN/AR/EN is NOT established. Both original wrapper stops succeeded and inner namespace settlement is recorded. Outer exited70 on unreadable-original-child namespace guard: cleanup HOLD/NOT_ATTEMPTED. Logical build slot retired by independent review only, not cleanup or global-idleness acceptance. No chooser/GUI16 repeat. The non-flat-tooltip paint-readiness correction and fresh GUI20 controls are independently source-reviewed and installed. GUI20 now independently passed one actual XML case and all five visual crops (EN/AR/EN, native Lock/Exit), with15events/3roles0, both original stops0 and27677/27677 runtime entries removed. It completes this development-Linux integration slice, not installed-image/all-platform or family closure. No GUI19 replay or HOLD recovery is inferred.
- macOS ASan+UBSan run34893068802 attempt1: **5 PASS, zero failures/errors/skips**, with exact compile/link/symbol/architecture evidence. Original private runtime removal and all observer settlement were independently accepted and adopted. Controller26.44seconds;21compact artifacts/36139bytes retained, transportZIPremoved. Source6f1200d7737fdbc6aed54d3d0c68fc86ed2853dd; activation4675ae486a5114508ff655122cf33783305ec81e. No active-provider, TouchID, JNA-combined, packaged-loader, TSan/leak or family-closure credit.
- Four real-child/injected-lookup-error terminal-observation controls passed and were independently accepted. These justify the prospective bounded original-child wait policy, not a kernel-race reproduction or cleanup of GUI19 HOLD.

Live execution authority/status: `reviews/team20/resume_disk/root/EXECUTION_SLOT.json`.
All historical HOLD/UNKNOWN, STOP/NO-RETRY and CLOSED scopes remain unchanged.

## Each open confirmed issue

All twelve have implemented, source-reviewed correction code. “Implemented”
does not mean a fix is ready to close; missing or failing tests may expose more
implementation work. Each new result still needs independent adjudication and
exact-source/cleanup reconciliation.

| Issue | What is still required |
| --- | --- |
| **PVA-001 — Android32 KDF ABI** | Actual R8/native outputs and scoped DEX-definition/ELF-export metadata are independently accepted. Native loading and target-runtime evidence remain. Run known answers and historical UTF-8-to-lowercase-hex password compatibility on a supported 32-bit Android runtime, including synthetic create/unlock/backup compatibility. Explicitly distinguish ARM32 from x86 evidence. |
| **PVA-007 — editor Save/Back data loss** | Room and the separate failed-Save acknowledgement contract now have bounded passing evidence. GUI16 real Main/native dirty-editor + durable Room reopen passed with paired nonshipping readiness instrumentation. Remaining dirty Back/tab/Add and rapid-input contracts must retain their exact scope. Remaining rapid/pre-frame, delivered keyboard/IME, accessibility, RTL and Android/iOS lifecycle contracts are not covered by the VM pass. |
| **PVA-008 — iOS attachment cancellation plaintext** | Execute the seven prepared cancellation/adoption methods; obtain applicable real-device file-protection and provider/lifecycle evidence. A simulator cannot establish physical file protection. |
| **PVA-009 — Android clipboard cleanup ownership** | Test actual ClipboardManager unavailable-read, recopy and cleanup-ownership behavior. Existing injected host-policy evidence is insufficient for the framework boundary. |
| **PVA-010 — Desktop native cancellation/context lifetime** | The real-JNA pre-provider lifetime case now passed on macOS x64. Five ASan/UBSan native/fixture cases now also passed. Complete active-native cancellation/late return, concurrency, provider and packaged lifetime evidence; no TSan/leak or hardware claim follows from the scoped sanitizer batch. A Linux fake or source review cannot replace the affected native backend. |
| **PVA-014 — biometric prompt language** | Run prepared Apple prompt-property check, then observe actual displayed prompt language and propagation on supported targets. Property assertions alone do not establish OS-displayed text. |
| **PVA-027 — installed tray language** | GUI20 Settings-to-tray EN/AR/EN, tooltip and native Lock/Exit passed; all five crops independently accepted, with endpoint Room evidence. Remaining supported-platform and installed-package validation is separate. GUI19 blank-crop failure remains preserved; do not rerun GUI20 unchanged. |
| **PVA-029 — attestation identity** | Preserve **44 PASS / 5 FAIL** out of 49 checks. Establish a permissible bounded diagnosis of the five failures, make any required repairs, then obtain discriminating verification under fresh authority. No automatic retry or replay of the failed helper. The five failures were narrowed at checkpoint7 to capture-cleanup fixtures, not five candidate/invocation-policy failures; exact signal/wait causes remain unknown. |
| **PVA-030 — Android SYSTEM locale refresh** | Exercise actual system-locale change and application lifecycle refresh. Can share a suitable target/build cohort with PVA-009; static checks and graph nodes are not framework execution. |
| **PVA-031 — capacity rejection loses Add draft** | Preserve new Room and editor acknowledgement evidence. Finish real Main/input, rapid/pre-frame, rejection, accessibility/RTL and mobile contracts shared with PVA-007. Do not claim retention after legitimate owner disposal. |
| **PVA-036 — Windows temporary-file lifetime/ownership** | Execute prepared after-FlushFileBuffers normal-release and controlled-child-death cases; finish applicable owned TMP/TEMP crash/cancellation cleanup evidence. Not a universal power-loss or orphan-recovery claim. |
| **PVA-037 — Windows allocation failure bypasses erasure** | Obtain a permitted whole-create/retrieve-caller allocation-cut mechanism after PRF/AAD and KDF/AES/later allocations. Preserve existing guard4/actual-PRK2/KDF1 evidence; it does not cover all callers. Current scope does not authorize a new provider/refactoring/instrumentation seam. |

## Other unfinished work — separate categories

### Suspicions, not confirmed defects

- **PVU-001:** real production caller/provider lock/lease schedule and settlement.
- **PVU-002:** post-lock delete admission with real durable DAO effect. Prior
  authoring refusal remains; no alternate-agent or rephrasing bypass.
- **PVU-003:** actual chooser/Home/cancel behavior; GUI18 reached the real export chooser but its focused-modal guard and cleanup focus wait failed. Native Home/cancel evidence remains missing; an AWT/native-focus assumption needs investigation. A new isolated read-only native probe and test-adapter integration are prepared as unreviewed source only, not installed or executed.
- **PVU-004:** iOS interactive gesture/Back/scene timing and keyboard LTR/RTL.
- **PVU-005:** remaining platform/backend lock-curtain behavior. Existing Linux
  SOFTWARE_FAST nonreproduction is not a universal disproof.
- **PVU-006:** real Room/terminal late-publication tail; no executable witness
  resulted from the refused authoring attempts.
- **PVU-007:** STOP; unresolved and not to be investigated or reformulated.
- **PVU-008:** applicable provider backing-content/destruction contract or
  permitted evidence; no provider experiment currently admitted.
- **PVU-009:** measured native cancellation/late-return latency.
- **PVU-011:** NO RETRY, no containment relaxation or procedure inquiry.

### Integration, packaging and reconciliation

1. Tray development-Linux case is complete; finish the remaining chooser investigation; do not repeat the accepted GUI16 Main/Room case without an affecting change.
2. Android minified/native output and scoped metadata inspection are accepted; finish framework preparation and final-package correspondence;
   actual target execution is separate from graph/preparation success.
3. Complete installed Desktop image/runtime validation through a permitted new
   route. Image01 failed and remains HOLD; no replay, probing or disposal is
   authorized by this report. Development GUI tests are not installed-image proof.
4. Obtain remaining Apple and Windows software/native/security evidence. The
   consumed WindowsGraph03 scope remains CLOSED and remote cleanup UNKNOWN/HOLD.
5. Establish actual artifact-to-notice/native-carrier correspondence, installed
   legal-document bytes/file sets and relevant bundled OpenJDK legal content.
   JNA Apache-2.0 and FreeType FTL have already been selected: do not invent a new
   blanket license-election blocker.
6. Reconcile source/overlays, generated schemas/resources, dependency verification,
   workflow/trigger pinning, supported artifacts and provenance. Reuse unaffected
   successful checks. Preserve PVA-029's failure.
7. Reconcile all issue variants, investigation dispositions and coverage limits
   against a frozen identity, obtain independent final review, and push compact
   handoff information to the dedicated continuation branch only. The current
   late selection and push are unfinished.

### Eight owner design decisions, not eight extra defects

Accept or separately commission changes to: metadata/whole-database protection;
versioned KDF-input redesign (legacy compatibility remains mandatory); best-effort
memory erasure; iOS clipboard/background tradeoffs; external-viewer plaintext
retention; optional new-enrollment-only TOTP minimum; transactional restore plus
compensation versus a new durable recovery protocol; and ownership/resources or
explicit deferral for required platform evidence. A redesign is outside these
verification estimates and must not be silently undertaken.

## What the owner must supply for blocked work

| External need | Exact resource or information needed |
| --- | --- |
| Android32 | An isolated ADB-accessible supported 32-bit Android target with OS/API/ABI details, or verified authority for the applicable SDK image license and a usable approved image/runner. A 64-bit-only image is insufficient; shipped ARM32 coverage must be explicit. |
| Android framework/security | A licensed isolated emulator/device for clipboard and locale/lifecycle checks. Actual Keystore/biometric claims also need a suitable physical device and interactive tester. These may share a target where capabilities match. |
| Apple software | A sufficiently provisioned eligible Apple-Silicon host/runner with compatible JDK17, Xcode and iOS simulator. Prepared iOS cohort: seven attachment methods, prompt-property, VEK-wrap roundtrip and wrong-KEK rejection. Prior 7 GiB host failed memory admission. The separate real-JNA Desktop witness already passed on macOS x64; do not repeat it. Additional arm64 evidence is separate only where required by the supported scope. |
| Physical Apple behavior | Supported iPhone/iPad, OS/device details and a tester for protection, biometric/provider, scene and input behavior; hardware keyboard for applicable keyboard claims. Simulator/macOS cannot prove these. |
| Windows software — root-actionable; no new owner approval presently needed | A genuinely permitted new validation prerequisite/scope, preserving CLOSED Graph03. Existing non-publishing CI authorization is retained, but is not permission to replay or rename the closed attempt. New PVA036 after-flush two-case direct-CMake source has independent acceptance and is installed inert; eight disposal-policy controls and source-contained instance are accepted; publication, exact request/activation and actual validation remain. This does not reopen Graph03. |
| Interactive Windows/provider | Isolated supported Windows machine with configured Windows Hello, relevant provider/version and documented content-lifetime contract, plus a tester. Hosted CI is insufficient for interactive-device claims. |
| Restricted evidence mechanisms | Applicable narrowly scoped authority/permitted mechanism or independently produced synthetic evidence for PVA-029, PVA-037 and the refused investigation witnesses. No authority in this report overrides tool refusal, STOP, NO-RETRY or CLOSED restrictions. Do not send real vaults or credentials. |
| Distribution/product choices | Confirm supported platform/feature/distribution scope and disposition the eight PVD choices. Supply a verified publisher-controlled public SUPPORT_EMAIL before any future public Debian release; this is not a general compilation blocker or publication authorization. |
| Missing exact validator | Only if indispensable, provide the verified authoritative location of the omitted evidence-ledger validator. It has not been supplied or run; manual reconciliation is not that tool. |

## Execution order and estimates

1. ACTION07 interruption/originalstop/namespace and logical-slot retirement are independently accepted. SIGTERM origin is unknown, not a compiler/disk failure; original HOLD outputs remain and no retry is authorized. Android compile/package validation is still incomplete.
2. Eight in-memory Windows disposal-policy controls passed and are independently accepted; these are not Windows runtime tests. Source-contained nonce/native-input acceptance is complete; publish source with the updated compact handoff, then freshly admit one Windows native batch containing both after-flush cases.
3. Continue chooser/native ownership and installed-image/legal/provenance source preparation while CI runs. GUI20 and both successful macOS native/JNA and sanitizer cohorts will not be repeated unchanged.
4. Normal12GiB free space remains unmet (about9.4GiB at latest observation). One-heavy-job restriction and exact run-specific resource/cleanup guards remain. Uncertain or HOLD artifacts are not safe cleanup targets.
5. Reconcile exact source identities, issue variants/investigations and platform gaps; independent review and full compact continuation publication. No overall completion claim or new family closure.

### Estimates, not deadlines

- Android and Desktop preparation/render cycles have recently taken roughly 10–20 minutes each when they reach execution. The completed sanitizer controller actually took 26.44 seconds, excluding GitHub queue/setup. Bounds are not predictions: GUI controller100minutes including cleanup, Android action40minutes plus originalstop/cleanup. Remaining outcomes are unknown.
- Remaining batching/adoption/publication/report work is likely several hands-on hours if the prepared checks pass. Failures can require another code/fixture correction and a discriminating rerun; no unconditional1–3hour whole-project promise is supportable.
- Desktop chooser/installed image and restricted Windows/allocation witnesses have unresolved mechanisms; reliable engineering time is not yet known. Diagnose the lawful nativefocus/ownership and image route before forecasting their completion.
- Actual supported Android32/framework and iOS simulator checks cannot be timed reliably without the target/runner details. Once supplied, installation/toolchain/build runtime must be measured separately from hands-on work.
- Physical Android/iOS/Windows testing and owner decisions have unbounded external waiting until devices/testers/contracts and scope dispositions are supplied. Store approval/publication is separate and unauthorized.

The consumed **1–3-hour estimate covered only a success-assumed Linux/Desktop slice**, not all engineering, physical gates or release readiness. The earlier2–4week number is not a measured remaining schedule and is not used here. No reliable overall date exists until the still-failing software gates and external target availability are resolved. Preserved successful checks will not be repeated without an affecting change.

## Definition of finished

Actionable engineering is finished only when all permitted feasible fixes and
material boundary checks have independently reviewed source-bound evidence,
owned workers are settled, disposable outputs safely removed and the handoff
reconciled. Open/blocked items are not completed items.

Release readiness additionally requires confirmed defects and applicable
integration/native/compatibility/artifact/legal/provenance and physical gates
satisfied for an agreed scope, explicit owner dispositions of design limitations
and investigations, and independent final reconciliation. Deferral is not a pass.

Production signing, candidate replacement, Store submission/approval and actual
publishing remain separate and unauthorized. Protected branches/tags,
identities/versions and occupied build1017001 remain unchanged.

## Preservation and exact evidence

Current accepted records: `runs/linux-desktop-integration16/`, `runs/linux-desktop-integration17/`,
`runs/linux-android-release-action04/`, `runs/linux-android-framework-package-action01/` and their named
independent reviews/root adoptions under `reviews/team20/resume_disk/`.

Discovery scope/custody variances01–06 remain separately retained; no blanket no-access or original-custody claim.
The retired W Git store and historical HOLD runtimes must not be probed, cleaned or reused.
The small21-file sanitizer-prerequisite checkpoint and request-only activation were pushed and independently accepted; protected refs are unchanged. The previous full handoff selection contains1373named files/about19.29MB and is stale; it must be refreshed with current results and reviews. Canonical activation requests and three locally retained third-party .class captures are omitted from public transport. This is not full privacy/semantic clearance: latest Android/GUI20 results, reviews and final freshness must be joined before publication. No protected ref, tag, Store identity/version or build1017001 change.
