# Closeout-controls launcher: preliminary BODY review — REVISE

Reviewer `/root/editor_review`, independent of author `/root/storage`.
Recorded `2026-09-09T03:31:22.321080+00:00`. **Source-only; no final ACCEPT or execution/instance admission.**

## Exact unaccepted subjects

- `linux-closeout/LAUNCH_CONTROLS_01.py`: `7b1ab171d1e03f15e87ebdc3f413047618adba8db15f7d25c538ca1b162d2f46` — 36,459B /660LF.
- `linux-closeout/CLOSEOUT-CONTROLS-LAUNCH-PLAN.md`: `1a3fdc16d537606a2baf49133dbe1f783653a2ad28379c9a69acecff4983a940` — 22,212B /341LF.

Both files were completely read with bounded no-follow/pin/hash checks. No target import, compilation, syntax probe, invocation, test or process probe occurred.

## Independently counterchallenged revisions

| ID | Source finding | Guard/qualification | Required narrow correction |
| --- | --- | --- | --- |
| **LCL-OWN-001** | L177 can publish `fds[key]` then fail inserting `pins[key]`. L179 closes but leaves the first registry entry; final `d.close()` can attempt it again. | Discovery is pre-journal; error prevents controls/PASS. No descriptor reuse, unrelated-FD effect or product impact demonstrated. This is the partial-registration once-only contract defect, not a runtime test result. | Unpublish partial registration before constructor close, or use failure-safe single-entry publication. No uncertain-close retry. |
| **LCL-TIMER-001** | Second alarm's stderr write can block before its `finally: _exit(124)` after the one-shot timer expired. Bounded bytes are not bounded elapsed time. | Existing Python/kernel/host caveats remain; no blocked transport, hang or signal experiment was observed. | Direct emergency self-exit without potentially blocking diagnostics; external exit124/missing terminal is HOLD. |
| **LCL-DOC-001** | PLAN236 says tracking advances after protocol validation; the durable-emission list actually advances before validation. | Intentional invalid-event retention plus sticky `failed` prevents false PASS. Documentation only. | Distinguish durable emissions from validated rows/protocol state. |

The reviewer sent all three concrete counterexamples to `/root/storage`; that author independently confirmed each, retained the limiting guards above and proposed the narrow corrections. Root was notified. This is not author self-acceptance of a fix: **no successor fix has yet been reviewed here**.

## Guards and evidence boundaries retained

- Original scratch cleanup requires durable allocation authority, original parent/target pins, emptiness, durable intent, last-moment checks, one fd-relative rmdir, parent fsync and durable observed outcome. Partial/unknown state stays HOLD; no adoption, recursive cleanup or replay.
- Journal completion checks pins/length before append and after fsync. Failure sticks. Invalid control rows remain distinctly nested outer records.
- Fixed28 ordered IDs, begin/result pairing, actual body-start and COMPLETE cleanup for PASS, reconstructed terminal counts/unstarted list, exact returned/emitted terminal and58 durable control emissions are required. Compilation, records and task counts are not extra test cases.
- A provisional terminal0 is insufficient: final descriptor closure, actual outer exit0 and independent own PID/starttime settlement remain separate. Timers/resource observations are qualified cooperative evidence, not guaranteed syscall/host preemption.
- Per-member originals are **not** externally emitted by this28-control harness. Later absence cannot reconstruct a complete original-member census or authorize recovery.

## Separate unresolved prerequisite

Root-confirmed **LC-LAUNCHER-CONTRACT-001** leaves helper `b16774ff...` whole-helper viability **HOLD**. Its historical source acceptance and the current controls `bdee7e28...` / component review `f564f4a4...` do not cover excluded `run_contract`, main, admission, Guard, Forest or actual closeout. This reviewer does not adjudicate that separate defect in this preliminary body scope. **No automatic hash/order rebind.**

Preserve these exact drafts before editing. Independently review the author fixes and eventual exact source chain, then obtain a separate filled fresh root/independent instance packet before any root-owned invocation. No step is supplied by this report.

## Accounting / safety

**0 executions,0 application cases,0 new product families,0 qualified closures.** No global denominator is retallied here. Frozen PLAN reports19/25 original,22/38 all and2/12 suspicions; eight PVD explanations/owner decisions remain separate.

Controls01's20PASS/2FAIL, controls02's22PASS/0FAIL and their prior failures/limitations remain unchanged. PVU-007 STOP, PVU-011 NO RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED, Windows FAIL/HOLD/all14 unstarted/no retry, old-runner prohibition and publication/hardware boundaries remain intact.

Only this compact permanent report pair was created. No reviewer scratch, caches, generated build outputs, workers, signals, deletions or wrapper obligation arose. Companion JSON retains source-read pins, detailed counterexamples and a resource-only observation; that observation is not host/slot admission.
