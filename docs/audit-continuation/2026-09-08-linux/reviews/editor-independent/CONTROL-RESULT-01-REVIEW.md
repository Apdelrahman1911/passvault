# NEW Linux controls 01 — independently reconciled FAIL

Reviewer `/root/editor_review`; execution owner `/root`; controls author
`/root/verification`. Reviewed 2026-09-09 at 00:38 UTC. No reexecution.

`CONTROL-RESULT-01-REVIEW.json` binds the full source/admission and observations
(SHA-256 `cb7e5629fad46d4287f6e65043fd0ba8b544bf14519991120a12da5d3fb3aede`).
The journal is `75fcebbd3c666518f9b2160d186e83ad9eebb9b8774c016eae043e88e40c6eb7`
(123,873 bytes, 213 consecutive records); root's external receipt is
`39a7daf48e4096fab5245c858f71aef1fe7eae507dd88b424078774fca1217f1`.
All seven admitted input images/full pins, 17 original ancestors, original
coordination lock and fixed Python image/pin still matched during review.

## Actual cases, not task counts

One consumed attempt: **22 controls attempted, 20 PASS, 2 FAIL, zero unstarted**.
Every ordered case and its full evidence were read; the preterminal report's
22 result objects exactly duplicate the individual journal records. Real child
processes, real process signals and application tests: **zero**.

**LC17 and LC22 failed before their intended subject boundary.** Both call
`direct_death`, which constructs `FakeChild(0, None, 910101)`. The constructor's
unconditional `len(payload)` at line 475 raises the recorded
`TypeError: object of type 'NoneType' has no len()` before its later None guard,
stdout assignment or `pipe2`. The process-row patch and `Processes.direct` call
at line 671 are not reached. Their journal rows (177, 208) contain no evidence
payload. This is local helper finding **LC-FIXTURE-01**, not a subject/database
product defect or evidence that the direct-zombie behavior passed.

The actual controls author independently counterchecked the source and retained
journal: preceding `MemoryJournal`/`Processes` creation is only in-memory state;
the failed `FakeChild` acquires no FD. The outer per-case `Area` allocation is
separate and is included in cleanup reconciliation. Proposed corrected source
or another namespace receives no credit from this result.

The successful real-file/fault-hook cases remain useful evidence. Fake Popen,
process rows, cancellation, signals, Gradle/stop argv and resource records remain
mock evidence. LC21's duplicated ordinary FD is not an actual pidfd. Nothing
here proves a JVM, daemon, real child lifecycle, hardware behavior or 105
application methods. Expected negative-control errors are not extra failed cases.

## Original cleanup and process settlement

The complete namespace ledger reconciles **24 original fixture directories**
with 24 rmdir intent/observation pairs, and **25 regular originals plus two link
aliases** with 27 unlink intent/observation pairs. All 51 deletion-intent pins
match their recorded original lifecycles. Two intentional renames reconcile
their remapped cleanup paths; no separate rename-success event is invented.
No pending or live synthetic ledger objects remain.

All 22 rows report successful namespace cleanup and explicitly false descriptor
HOLD. Original scratch inode 669871/dev23 is bound before use and removed at
events 210/211. Terminal event 212 reports cleanup
`ORIGINAL_EMPTY_SCRATCH_REMOVED`, no cancellation/timeout or launcher exception,
unchanged source inputs, and **exit 1**, not PASS.

Root actually observed tool `8b0448` exit 1 with empty **combined** output.
Its original PID 8117/starttime 18227925 was absent afterward; this reviewer
independently observed both that PID and scratch absent at 00:38 UTC. Exact
no-real-child source scope plus actual process exit supplies final OS descriptor
settlement; pathname absence alone is not FD proof. Individual close-return
traces and a host-wide process census are not claimed. `gradlew --stop` is N/A:
no wrapper was launched. The tiny coordination metadata remains needed, intact.

## Bounds and retained timing disagreement

Recorded batch monotonic duration is 0.2103432939911727 s; the longest case is
0.016969640011666343 s. These satisfy their 180/15-second soft limits. Three real
launcher resource samples meet the launch/running floors; minimum recorded disk
availability is 23,665,139,712 bytes and RAM availability 46,253,379,584 bytes.
Maximum sampled self RSS is 212,196 KiB. Fake resource rows are excluded.

**Do not promote the tool's 0.255451913 s to full invocation elapsed time.** It is
shorter than the journal's first-to-terminal wall-timestamp span, 0.311654695 s.
Root confirms no separately retained full external monotonic entry/exit pair;
an approximately 0.4 s tool envelope is not that proof either. Both observations
are preserved with their different/uncertain clock scopes. No hard 240-second
invocation guarantee, signal-race test or continuous resource quota is claimed.

## Slot recommendation and unchanged boundaries

**Root may release this completed failed-controls slot after recording this
review.** The no-child process and original synthetic namespace are settled;
functional FAIL and timing qualification remain. The reviewer does not release
the slot or admit a retry, another controls instance, or database execution.

The consumed journal/request/approvals and explicit approval-count erratum stay
unchanged. Windows run 34284083351 remains **FAIL / cleanup HOLD, zero started and
all 14 unstarted**. PVU-007 STOP, PVU-011 NO RETRY, PVA-029's failure/no automatic
retry, G7/G8 CLOSED, old-runner prohibition, PVD/hardware and publication
restrictions remain. Product progress stays **19/25**, **22/37**, and **2/12**;
these are separate denominators, with no new product finding or qualified closure.

Reviewer activity was bounded data/source reading and compact permanent review
writing only. Metadata commands completed with all owned descriptors closed;
no helper import, syntax probe, control/application run, cache/build output,
daemon, worker or emulator was created, and no unrelated resource was removed.
