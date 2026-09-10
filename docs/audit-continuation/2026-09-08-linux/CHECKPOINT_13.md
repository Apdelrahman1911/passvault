# Checkpoint 13 — Desktop exit repair and focused integration preparation

**INCOMPLETE / NOT READY.** Dedicated-branch source/evidence checkpoint only.
It does not activate CI, authorize a build, replace a candidate, or reopen a held scope.

## Completed since checkpoint 12

- Published C12 at `6220812e4602369d4853b763465a92e6291bb971` /
  `3defb2a459ffbbddb51b3baa0f088d438edf74c3`. Its initial publication failed
  because raw Windows XML was omitted from the whitespace exception list;
  the separately admitted staged finalization succeeded. Both outcomes and the
  original author/reviewer omission are retained. No application checks repeated.
- Independently confirmed **PVA-039**: Compose's default normal exit terminates
  the JVM before Main's outer finalizer/cleanup wait. Changed the production
  application-loop wrapper to `exitProcessOnExit=false`, preserving Main's
  existing coordinator, timeout, lock release and outer process exit.
  Existing cleanup tasks and the ownership-aware clipboard shutdown hook are
  counterguards: this is not proof that all cleanup was absent or data was lost.
- Independently reviewed that repair and one real child-JVM regression. The
  proposed case compares actual default Compose exit with the production wrapper,
  using two serial fresh JVMs and a synthetic caller-finally marker. **Not yet
  compiled or executed**; no full-Main/provider cleanup or PVU-006 closure claim.
- Independently reviewed one new editor integration case: actual native input,
  production form/ViewModel, real encrypted repository/Room, field50/pending row
  draft, page Save, old VM settlement, database close/reopen, and exact50 tuple
  reload through fresh owners. Review corrected secondary-interruption loss in
  fixture cleanup. **Not yet executed**; no full NavHost/IME/mobile evidence.
- Grounded the new minimal private tray session in the installed panel/xfconf
  contracts. Final workload is **three JUnit cases / three XML suites / two serial
  Test tasks**: native tray, lifecycle, real-Room editor. Two lifecycle children
  are not two extra JUnit cases. Source acceptance and actual admission remain
  separate; no unchanged Linux03 or GUI02 rerun is planned.

## iOS01 — actual failure, no product tests

Request-only activation is `d4b72e0a30e2b53283a3f23b13b942afc6cbd38b` /
`291a75853d8f81c5b1da5b4dfe653896cf79a1bc`, the parent of this checkpoint.
Run **34454557777 / attempt1** failed its launch RAM gate before Xcode,
private-runtime allocation, simulator creation or Gradle. Checkout and bounded
metadata/evidence operations did occur.

- **0 passed, 0 failed product cases, 10 UNSTARTED, 0 XML**.
- The recorded free+speculative kernel-equivalent RAM was186,630,144B out of
  7,516,192,768B (2.48%), below25%. Disk passed. This is not general available
  RAM, measured application memory demand, or proof all Apple runners fail.
- The raw unlaunched command intent retains `exit:null` / `settlement:UNKNOWN`.
  The empty Xcode log does not establish an Xcode invocation.
- Independent actual review supports **scheduling-only release** after the exact
  terminal job and a fresh eight-terminal-run branch observation. The raw
  `PENDING_REQUIRE_ACTUAL_HELPER_EXIT_ZERO` evidence/ancestor-close qualification
  remains unmet at helperexit1. Neither cleanup PASS nor a proven close failure
  follows. No automatic retry, floor relaxation or speculative CI loop.

See `reviews/storage/IOS01-ACTUAL-RESULT-REVIEW.json`,
`reviews/ios-focused01/SLOT-RELEASE.json` and compact `runs/ios-focused01/`.

## Progress — separate denominators

| Measure | Completed | Remaining |
|---|---:|---:|
| Original confirmed families | **19/25 — 76%** | 6 |
| All confirmed families | **26/39 — 66.7%** | 13 |
| Original suspicions conclusively resolved | **2/12 — 16.7%** | 10 |
| Original PVD explanations | **8/8 documented** | Owner decisions separate |

The denominator increased for PVA-039; no closure was lost. Since handoff there
are four additional qualified closures (033/034/035/038) and two new families
(038/039). **196 application/native regression, investigation and historical
control events +3 infrastructure fixture controls +1 producer =200 XML elements**
remain unchanged. They are not unique production-only tests or task counts.
No overall-readiness percentage is defensible.

## What still needs doing

| Workstream | Remaining work / current blocker |
|---|---|
| Android32 **PVA-001**, highest risk priority | SDK-license scope and usable32-bit target first; then real ABI/KDF, create/unlock/backup compatibility and relevant packaged/minified/ARM32 evidence. Existing lowercase-hex encoding is mandatory. No download/emulator/ADB is admitted. |
| Editor/storage **007/031** | Execute the new real-Room case; then actual full-NavHost dirty Back/tab/forward, accepted backend rejection/pre-frame and input/RTL/mobile contracts. Passing logical49/rendered3 are preserved, not rerun. |
| Desktop **027/039** | Execute the new native tray and lifecycle cases. Full Settings-to-installed-tray propagation and non-Linux behavior are separate from direct tray updates; full terminal/observer/Room harmful-late-work inquiry remains PVU-006. |
| Apple **008/014** and native crypto | iOS01's seven attachment, two crypto and one prompt-property declarations are unstarted. A genuinely different justified runner/target scope requires fresh admission. Physical iPhone file-protection/picker/biometric evidence remains external. |
| Android **009/030** | Actual isolated clipboard ownership/lifecycle and EN/AR/SYSTEM/recreation/cold-process behavior; fakes/static predicates do not close these. |
| Native **010/014/036/037** | Preserve Mac02/Windows05 narrow passes. Remaining concurrency/provider/sanitizer/packaged lifecycle, crash/cancellation, PRF/wrapping-array and fixed-output requirements stay open. Windows05 cleanup HOLD/no automatic retry survives. Physical Hello/biometric behavior needs real hardware. |
| Workflow **029** | Preserve49 historical checks (44PASS/5FAIL); exact failing capture-cleanup primitive remains unknown. No speculative policy/oracle patch or automatic retry. |
| Static/integration | Settled-source serial Detekt with actual index/tool binding; other applicable migration/native operational-fault/package/provenance qualifications remain in the inherited matrix, not silently waived. |

Permitted unresolved suspicions are **PVU001/002/003/004/005/006/008/009**.
Real UI/provider admission and cancellation guards must precede any claimed
race/deadlock witness. Linux software work remains useful, especially full
NavHost/storage ordering, fresh-post-lock input admission, chooser/Tab admission
before mutex-cycle investigation, and real terminal ordering. Source-only
NavHost/PVU003 drafts are separate from this frozen three-case workload; no
fake stalls or unwired cancellation schedules count as production defects.
**PVU007 STOP and PVU011 NO RETRY are not executable backlog.**

The eight PVD boundaries, hardware/account/legal/owner/Store requirements and
full remaining-scope detail stay separate in `docs/audit-handoff/current/OWNER_DESIGN_DECISIONS.md` and
`CHECKPOINT_12.md`. Independent prioritization/estimate:
`reviews/baseline-coverage/REMAINING-WORK-AFTER-IOS01.md`.

## Conditional estimate and immediate continuation

- Prepared Desktop batch + Detekt + actual-result review: **0.5–1.5 active days**,
  assuming no new failure. Heavy jobs remain serial.
- Next prioritized Linux navigation/admission witness tranche: **2–5 working
  days**; a correct investigation may remain inconclusive rather than yield a fix.
- Android target work: **3–5 working days after license/target access**.
- Apple/Windows/native remainder: **4–8 working days**, subject to target access
  and new findings. Other investigations/integration/reporting: **3–6 days**.

The overlapping campaign estimate remains **roughly2–4 engineering weeks**, not
a summed task total or promised finish date. External waits, stopped scopes and
new defects are excluded. Continue useful Linux work rather than waiting on hardware.

## Source, publication and resource boundaries

Application/test byte registry now covers28 changed paths at their stated review
scopes, not fresh whole-file semantic-LF or whole-project coverage. Earlier
coverage/range/EOL/reference qualifications remain. The exact selection, review
and resulting commit/tree belong in the C13 publication receipt. Raw evidence
is preserved; whitespace checking targets authored source rather than repeating
known raw-log/XML formatting diagnostics.

Ordinary edits remain in `passvault-linux`; Git only in the independently admitted
`passvault-publication-20260910-01` store. No Git/cleanup/recovery touches the retired
store or old held runtime roots. The original handoff commit/tree are preserved.
Root alone executes; fresh source/instance/coordination/cleanup admission is still
required before the focused batch. JDK17/wrapper/oneworker/non-daemon/CoDoff/strict
verification/serial Detekt, original-wrapper stop and owned-worker settlement
remain mandatory. Remove only validated disposable outputs; never shared caches,
SDKs/toolchains, source, permanent tests/reports, or held roots.

Root-reported Linux point observation at09:22UTC was about19GiB disk free /32GiB available RAM
(tool observation, not an independent publication-review resource sample);
no new local runtime or build worker was allocated during this source phase.
No temporary download archive was retained. Refresh resources at admission and
during execution. Missing `validate_evidence_ledger.py` remains
**NOT_SUPPLIED_NOT_RUN**, not silently replaced.

All STOP/NO-RETRY/CLOSED/HOLD restrictions and actual failures survive. No protected
branch, tag, version, application identity, dependency, signing/store/release or
occupied **1017001** changes. Desktop publication remains deferred.
