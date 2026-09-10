# Checkpoint 15 — Desktop results, driver correction and remaining work

**INCOMPLETE / NOT READY.** Dedicated continuation source/evidence, not a release.
Published parent C14: `f7f3ca91cef14b7b2341114c44c460907061f883` /
`4b405be08458c035b9dd61807acab66659ccd864`. The containing Git commit/tree and
`publication/CHECKPOINT-15-PUBLISHED.json` identify C15 after its actual publication.

## Done

- Preserved the original handoff and independently reconciled C14 publication.
  No protected ref, tag, version, identity, dependency or occupied build1017001 changed.
- Ran the newly admitted integration02 batch once: **2 PASS, 1 FAIL, 1 UNSTARTED**.
  The lifecycle case distinguishes default Compose process exit from the corrected
  production return/finally/continuation seam. The native tray case verifies
  English→Arabic→English labels and native popup input. An independent reviewer
  inspected all three PNGs for legibility, shaping, clipping and selection outline.
- Preserved the actual failure: Main's accessibility snapshot threw an NPE while
  polling after the Unlock click, before its editor/navigation assertions. Seed
  succeeded; Main reported `DRIVER_FAILURE`; verify never started. Exit137 is
  consistent with fixture failure containment, **not proof of OOM or a production
  shutdown defect**. The separate shared Room test was unstarted, not skipped.
- Independently reviewed and applied a minimal **fixture-only correction**:
  read text payloads only from editable fields, which are the only payload
  consumers. Navigation metadata, native inputs and behavior/persistence assertions
  remain unchanged. No blanket exception catch, dependency patch or new product
  finding. The corrected fixture has not yet run; NPE resolution is not claimed.
- Independently accepted integration02's original stop/settlement/allowlisted
  cleanup evidence. Prepare0/stop0, render1/stop0; source-before/after matched.
  The original disposable runtime's **25,132 entries were removed**; compact
  evidence is retained: **100 files / 269,330 bytes**. No additional stop or
  cleanup replay is needed. Only scheduling and the transient T-reader freeze
  were released; every earlier held runtime/store remains untouched.
- Independently source-accepted new integration03 selecting only corrected Main and unstarted
  Room. Passing lifecycle/tray cases are not repeated. Serial Detekt remains a
  separate already-reviewed static-only route: merging its Git-inventory setup
  into the GUI runner would add complexity without useful confidence.

Primary evidence: `runs/linux-desktop-integration02/`,
`reviews/storage/INTEGRATION02-ACTUAL-RESULT-REVIEW.json`,
`reviews/baseline-coverage/C15-INTEGRATION02-ACCOUNTING-REVIEW.json`, and
`reviews/editor-independent/navhost-draft/MAIN-NAVHOST-EDITABLE-TEXT-ONLY-INDEPENDENT.md`.
The consumed request is preserved as inert `reviews/desktop-integration02/REQUEST-CONSUMED.json.txt`.

## Progress and estimated remaining effort

| Defined measure | Completed | Remaining |
|---|---:|---:|
| Original confirmed families | **19/25 — 76%** | 6 |
| All confirmed families | **26/39 — 66.7%** | 13 |
| Original suspicions conclusively resolved | **2/12 — 16.7%** | 10 |
| Original design explanations | **8/8 documented** | Owner decisions separate |

Since handoff, four additional qualified closures are 033/034/035/038; added
confirmed families are 038/039. No closure numerator changes in C15. These are
**not overall readiness percentages**. There are now **199 application-intended,
native, investigation and historical-control execution events**, including the
new failed Main attempt, **+3 infrastructure controls +1 producer =203 XML elements**.
This is not199 passes, unique behaviors, assertions, tasks or physical-device tests.

| Work still needed | Current boundary | Conditional active effort |
|---|---|---:|
| Android32 **001**, highest risk | Owner SDK-license scope and usable synthetic32-bit target; then ABI/KDF/create/unlock/backup compatibility and relevant packaged/minified/ARM32 evidence. Keep historical lowercase-hex encoding. | **3–5 days after access** |
| Linux **007/027/031/039** and static | Fresh exact-source/instance admission for corrected Main + unstarted Room; serial Detekt. PVA039 has meaningful lifecycle evidence but static remains pending. Tray endpoint is not full Settings propagation/tooltip/all actions. | **1–2 days**, subject to results |
| Remaining permitted software suspicions | PVU001/002/003/005/006 need genuine input/admission/lock/chooser/late-work witnesses, not artificial DAO stalls or fake success. PVU003 adoption remains an inert proposal. IME/RTL/mobile and rapid/rejection boundaries remain. | **2–5 days**, may remain inconclusive |
| Android **009/030** | Two accepted instrumentation cases unexecuted; fresh isolated device/user/Debug storage and framework cleanup admission needed. Cold-process/system-locale and physical behavior remain separate. | **1–3 days after access** |
| Apple **008/014**, native crypto and PVU004 | IOS01 retains10UNSTARTED/0XML and unmet evidence-close qualification; need compatible sufficient-resource target. Physical iPhone protection/picker/biometric/interactive behavior requires hardware. | **1–3 days after runner access**, hardware separate |
| Native **010/014/036/037**, PVU008/009 | Preserve narrow Mac02/Windows05 results; remaining real provider/concurrency/lifetime/packaged and hardware evidence is incomplete. Windows05 still overallFAIL/cleanupHOLD. Do not retry/rephrase the recorded native-agent refusal. | **4–8 days after target access**, hardware separate |
| Workflow **029** | Historical49 checks remain44PASS/5FAIL. Exact failing capture-cleanup primitive unresolved; no automatic retry or speculative patch. | **Not scheduled under current restriction** |
| Final integration/provenance/owner gates | Inherited migration/operational-fault/package/provenance/legal/account requirements remain. Missing authoritative `validate_evidence_ledger.py`: **NOT_SUPPLIED_NOT_RUN**. PVD choices, Store/release/signing and physical-device gates are separate. | **3–6 days**, external waits excluded |

Overall planning range remains **roughly2–4 engineering weeks**, with overlapping
workstreams; not the sum of this table or a promised release date. External waits,
prohibited scopes and new defects are excluded. Linux and software investigation
work is not mislabeled hardware-only.

## Resume without repetition

1. Use current `ISSUE_LEDGER.json`, `VERIFICATION_LEDGER.json`, `COVERAGE_DELTA.json`
   and `EXECUTION_SLOT.json`. Independent C15 coverage/publication review is separate
   from execution admission. All exact evidence is bound to its own source identity.
2. Rebind the existing unexecuted Detekt route to the settled source/index, then
   finish integration03 instance admission and run only its two remaining cases.
   Static-first avoids compiling GUI inputs that diagnostics might require changing.
   No old helper/request/runtime is reusable authority. No unchanged passing reruns.
3. Root alone owns builds/CI/Git and cleanup. One audit job across Linux/CI; JDK17,
   checked-in wrapper, one worker, non-daemon, CoD off, serial Detekt and strict
   dependency verification. Install cleanup before invocation and preserve outcomes.
4. Preserve **PVU007 STOP, PVU011 NO RETRY, PVA029 failure/no automatic retry,
   G7/G8 CLOSED**, TRAY01 and every old runtime/store/Windows/iOS HOLD or qualification.
   No unrelated process, source, permanent test/report, shared cache or SDK deletion.

Ordinary edits: `passvault-linux` (W). Git/publication only:
`passvault-publication-20260910-01` (T); never build there or use W's retired store.
The 31 current application/test/configuration byte identities are not a fresh
whole-project semantic review. All historical coverage/reference/EOL limits survive.

At17:03UTC disk free was approximately19GiB and RAM available approximately29GiB.
These are point observations, not exclusivity or net-reclaimed-byte measurements.
Subsequent source/report preparation creates no build runtime/cache or service;
permanent evidence and old held roots are not disposable garbage.
