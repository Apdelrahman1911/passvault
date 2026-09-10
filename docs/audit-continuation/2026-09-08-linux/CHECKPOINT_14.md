# Checkpoint 14 — Desktop integration, Android regressions and execution reconciliation

**INCOMPLETE / NOT READY.** Non-activating continuation source/evidence only.
No release, protected-branch change, new build or recovery admission follows.

## What has been done

- Preserved and independently reconciled C13 publication:
  `d3d46db51d9fa69a4060250e71e0477c6b930d3a` /
  `245160648cb79873f41da41969a5b48d4c2057e1`. Its 76 selected paths and 22 completed,
  reaped commands include one nonforce continuation-branch push. No product pass.
- Added one permanent **actual-Main/native-NavHost/real-Room regression**. Its
  prospective flow uses three serial JVM roles: seed/close real Room, drive the
  production Main and navigation graph, then reopen in a fresh process and compare
  exact durable state. It covers dirty Back/tab/Add/discard and clean-tab paths
  without replacing the navigation graph or persistence with fakes.
- Independent review found a **fixture false-pass race**, not a new PVA family:
  a timed-out queued EDT action could later exit Main before failure reporting.
  The accepted asynchronous terminal action records errors before queued key
  handling. Rejected inputs remain reproducible through inert snapshots/patch.
  Root's initial adoption changed only two documentation lines. A subsequent
  independent challenge found child `environment.clear()` dropped the private
  bus/XDG/bridge controls. A separately accepted 26-line correction now validates
  and forwards those original values into each child. It does not prove endpoint
  authentication or absence of native activation. **One case, three roles,
  thirteen ordered stage events; zero compilation/execution.**
- Independently source-accepted fresh **integration02**: the three TRAY01-unstarted
  tray/lifecycle/editor-Room cases plus this Main/NavHost case, sharing two serial
  Test tasks and four XML suites. Actual task-time `desktopTest.classpath` is
  forwarded to the three roles; private runtime/evidence and bounded role logs
  remain required. A separate source-fit challenge changed only the new private
  display to 1280×1024: 1024×768 can put fresh Main partially offscreen; 1600×1000
  exceeds the unchanged tray fixture cap. No product/default-window or fixture
  guard was widened. Actual WM fit remains unobserved. Source review is not
  instance/execution admission; all instance/source bindings remain unset.
- Independently source-accepted an **inert PVU003** real-Export chooser → one native Home attempt
  → native cancel snippet. It does not select an output, delete data, inject a
  fake DAO stall, prove an admitted navigation action or demonstrate a deadlock.
  No permanent-test adoption, runtime witness or conclusive outcome. Its inherited
  150s deadline may prevent cleanup Escape; ambiguous/failed input/listener/EDT
  cleanup requires outer hard-settlement/HOLD, not assumed native cancellation.
- Independently qualified Apple feasibility: changing only to an Intel label
  cannot execute the unchanged ARM64-only iOS ten-case selection. This does not
  prove all Apple runners infeasible or authorize new targets/dependencies,
  resource-floor relaxation or another CI run.
- Added an independently source-accepted **two-case Android instrumentation fixture**
  and one-line ordinary test-runner wiring, without new dependencies or application
  identity/version changes. It uses actual Main/Koin/ClipboardManager for replacement
  ownership and background/foreground observations, and real Activity recreation for
  EN/AR/SYSTEM/default prompt-input checks. No manual foreground callback or extra
  bootstrap/recovery retry is used. Independent challenge corrected cleanup error/
  interruption retention, clipboard-label ownership, background interval anchoring
  and exact process-locale restoration. Rejected input remains an inert snapshot.
  **Zero compilation/execution.** A fresh synthetic device/user and Debug storage
  must be authorized before Application startup; tokens/package names cannot prove
  isolation. Null clipboard reads are denial or absence, not timer-causality proof.
  The bounded read-only onboarding accessibility gate, framework-connection cleanup,
  actual target/API behavior, device-global locale propagation, rendered RTL/prompts
  and physical security remain unverified. This is separate from Android32's four
  already-authored cases and from the four-case Desktop integration selection.

## What actually ran, failed or remained unstarted

### TRAY01 — consumed operational failure, cleanup held

The external result is **70**. Host coordination detected a positive Java name
of unknown ownership during preparation; the original preparation wrapper
exited 143 and its sole applicable `--stop` exited 0. Rendering did not start;
no second stop is owed.

- **0 passed, 0 failed product cases, 3 UNSTARTED, 0 XML**.
- Raw transport verified 2,495 members in two passes (82,499,214 payload bytes).
  This is raw materialization, not a normalized checkout or test count.
- Source-before succeeded; source-after/final acceptance did not complete.
  Missing source-after is not evidence of source mutation.
- Original private-domain/direct-child settlement supports scheduling release,
  not global idleness or successful runtime cleanup. Other Java ownership/cause/
  settlement remains unknown; the later unstable name observation need not be
  the same process. No unrelated-process ownership investigation, adoption or
  signalling followed the bounded name observations.
- **Runtime cleanup NOT_ATTEMPTED / original runtime and attempt inputs HOLD.**
  Do not probe, adopt, modify, clean or retry them. Only the audit scheduling
  reservation and new publication store's transient active-use freeze released.

Compact evidence: `runs/linux-desktop-tray01/` (18 files/46,837 bytes),
`reviews/desktop-tray/EXTERNAL-RESULT.json`,
`reviews/storage/TRAY01-ACTUAL-RESULT-REVIEW.json` and the exact slot release.
Earlier metadata preparation01 failure and preparation02 success remain recorded.

### Detekt — no execution, host scheduling block

The C13 actual-index/constants and tool-path source checks were independently
accepted. At 12:21:39 UTC the fresh host preflight reported positive Java names of
unknown ownership/identity.
**No request, helper, raw reader, runtime, slot reservation, task or case started.**
The unused prospective input freeze was released separately; the prepared helper
is not consumed, but unchanged host probing/retry is not useful. An uninterrupted
VPS build window is needed. A C14 publication changes the source/index identity;
C13 bindings cannot certify C14. Rebind only the meaningful new source/index delta
before any later admission. Serial Detekt is still unfinished, not silently waived.

IOS01 remains 10 UNSTARTED/0 XML with its unmet evidence-close qualification.
Linux03, Mac02, Windows05 and GUI02 results are preserved without unchanged reruns.
Windows05's 24 passing CTests do not erase its overall helper failure/cleanup HOLD.

## Progress — defined denominators, not overall readiness

| Measure | Qualified/conclusive | Remaining |
|---|---:|---:|
| Original confirmed families | **19/25 — 76%** | 6 |
| All confirmed families | **26/39 — 66.7%** | 13 |
| Original suspicions | **2/12 — 16.7%** | 10 |
| Original PVD explanations | **8/8 documented** | Owner choices separate |

Since handoff: four additional qualified closures (033/034/035/038), two added
confirmed families (038/039). PVA039's repair is source-accepted, not yet closed.
No numerator changed in C14. **196 application/native regression, investigation
and historical-control events +3 infrastructure fixture controls +1 producer
=200 XML elements** remain the executed total, not unique production-only tests.
The 31 application/test/configuration byte identities are not whole-project semantic coverage;
historical ranges, reference gaps, exclusions and checkout-EOL qualifications stay.

## Everything still open

| Priority/workstream | Remaining work and blocker | Conditional active effort |
|---|---|---:|
| Android32 **001** | Highest risk priority: owner SDK-license scope and usable32-bit target; then actual ABI/KDF, create/unlock/backup compatibility and relevant packaged/minified/ARM32 evidence. Preserve lowercase-hex encoding. No SDK/device action admitted. | **3–5 days after access** |
| Linux **007/027/031/039** and static | Compile/run the four-case focused integration batch; independently reconcile actual behavior and cleanup; settled-source serial Detekt. Needs a fresh reviewed source/instance and uninterrupted build window. Do not retry original TRAY01. | **1–2 days after scheduling** |
| Editor/storage/session **007/031**, PVU001/002/003/005/006 | Actual Main navigation/Room behavior first; then only justified fresh-input/post-lock/chooser admission and terminal-ordering witnesses. Existing49 logical and4 GUI events stay valid within their scopes. No fake stalled schedule counts as a production race. IME/RTL/mobile and harmful-late-work gaps remain. | **2–5 days**, findings may stay inconclusive |
| Android **009/030** | Compile and execute the accepted two-case fixture with fresh whole-device/user/Debug-data and framework-connection cleanup admission. Complete remaining genuine cold-process/system-locale and device behavior separately; source, fakes and static predicates are not device proof. | **1–3 days after target access** |
| Apple **008/014**, native crypto | Seven attachment, two crypto and one prompt-property case remain unstarted. Need a compatible justified runner with sufficient admitted resources; physical iPhone protection/picker/biometric behavior stays hardware-BLOCKED. | **1–3 days after runner access**, hardware separate |
| Native **010/014/036/037**, PVU008/009 | Preserve Mac02/Windows05 narrow passes. Remaining real concurrency/provider/sanitizer/packaged lifecycle, crash/cancellation, PRF/wrapping-array and fixed-output boundaries need actual target evidence. Physical Hello/biometric behavior is external. Do not retry/rephrase the recorded native-agent refusal. | **4–8 days after target access**, hardware separate |
| Workflow **029** | Historical49 checks retain44PASS/5FAIL; exact capture-cleanup failing primitive is still unknown. No automatic retry, speculative policy/oracle patch or failure erasure. | **Not scheduled under current restriction** |
| Final integration/provenance | Remaining applicable migration/operational-fault/package/provenance/legal/account gates from the inherited matrix; no candidate replacement, signing, Store action or publication authority. Missing authoritative `validate_evidence_ledger.py`: **NOT_SUPPLIED_NOT_RUN**. | **3–6 days**, external decisions separate |

Allowed unresolved suspicions: **PVU001/002/003/004/005/006/008/009**. PVU004 needs
actual iOS interactive input. **PVU007 STOP and PVU011 NO RETRY are not executable
backlog.** Eight PVD design/product boundaries remain separate owner decisions;
compatibility is not redesigned to simplify verification.

The overlapping campaign estimate remains **roughly 2–4 engineering weeks**, not
the sum of this table or a promised release date. External waiting time, prohibited
scopes and newly discovered defects are excluded. There is no honest single
"everything finished" percentage or calendar date while those gates remain open.

## Resuming safely and efficiently

1. Read the three current ledgers, slot and this report; preserve all failures.
2. Continue bounded source work while build scheduling/targets are unavailable.
   Independent review should challenge only meaningful changes, not replay the
   entire audit. Android's two cases are source-accepted only; the PVU003 fragment
   remains unadopted. Neither belongs in an execution batch without fresh admission.
3. Bind any fresh integration02/Detekt instance to its exact committed source,
   toolchain, original coordination/cleanup identities and genuine independent
   acceptance. No old request/helper/held runtime is reusable authority.
4. Run one audit build/test job at a time, smallest useful scope, no unchanged
   successful expensive reruns. Preserve exact results and stop the applicable
   original wrapper; verify owned worker settlement and remove only validated
   allowlisted disposable outputs. Keep cleanup failures explicit.

At 14:30 UTC root's resource-only observation reported 18,574,639,104 B disk free and
33,781,448,704 B available RAM out of 67,435,888,640 B. This is a point observation,
not process exclusivity/admission or current allocated-size proof of held roots.
Subsequent C14 source-only report/publication preparation created no new build
cache, temporary runtime or background worker. Earlier TRAY01 allocated its held
runtime. Held roots are not safe garbage-collection targets; shared caches,
SDKs/toolchains, permanent source/tests/reports and unrelated processes stay intact.

Ordinary edits: `passvault-linux`. **Git/publication only:**
`passvault-publication-20260910-01`; never build there or use W's retired store.
The original handoff commit/tree remain preserved. C14's fresh explicit selection,
independent report/publication review and resulting identity belong in
`publication/CHECKPOINT-14-*`. A report is not an execution receipt.
Dedicated continuation branch only; no main/testing/release/tag/protection/version/
identity/dependency/signing/Store or occupied mobile **1017001** changes.
