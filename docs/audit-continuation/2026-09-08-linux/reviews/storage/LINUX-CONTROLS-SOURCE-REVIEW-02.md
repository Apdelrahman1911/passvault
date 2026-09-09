# Controls v4 — independent fixture finding and exact-source review

Reviewer: `/root/storage`; 2026-09-09 UTC. Controls author: `/root/verification`.
Disposition: **ACCEPT_SOURCE_ONLY**, not execution admission or a verified fix.
Root alone owns execution/coordination/cleanup. `/root/editor_review` owns the
separate original result adjudication and new outer-launcher review.

## Exact after-image and preserved failed history

- New controls `../verification/linux_runner_controls_v4.py` SHA-256
  `dffa884b09c7786e763c9becc3b0b220242b5c9555288bf1052e9e16912fa426`,
  43,063 bytes /893 LF.
- New design `../verification/LINUX-CONTROLS-V4-DESIGN.md` SHA-256
  `b52d60fcd8eb2cfb07275d3508977284a5ed8a46209b09ec8cdf669e8dd8c192`,
  7,399 bytes /118 LF.
- Exact two-line patch `../verification/LINUX-CONTROLS-V4.patch` SHA-256
  `6ddef57c4463e97e170ba106e8786763f426aac7ca597f2b0798ea6b2c0210f5`.
- Inherited design remains literal SHA-256
  `1292305d4a0f1729be7016002c9f1bc99f37487077aea4f4f3d1aa1854723921`.
- Consumed controls01 source remains literal SHA-256
  `320f363e5f9bdce0e806a624f7e9fd9fc943690be57f8c75c9f43f865c04903d`.
  Both old source/design match their separately retained pre-correction snapshots.
- Original journal remains
  `75fcebbd3c666518f9b2160d186e83ad9eebb9b8774c016eae043e88e40c6eb7`;
  original external receipt remains
  `39a7daf48e4096fab5245c858f71aef1fe7eae507dd88b424078774fca1217f1`.
  Root observed outer exit1, 22 case-result records,20 PASS/2 FAIL. This review
  inspected the literal failure/terminal records and receipt, but does not replace
  editor_review's full independent execution/cleanup reconciliation.

The prior independent source review did not catch this fixture bug. Its original
MD/JSON remain unchanged, SHA-256 `547f216209c99c5ee7446476c7e3e937fa4f39bd580fa9f60965bd4c107e878b`
and `bfdc0e7ebabc06078195c91e5f6014ca446895c8e22d0fb5f31848a963079432`.
No new after-image result is substituted for the failed consumed attempt.

## C-FIXTURE-01 independently confirmed

Both `LC17_direct_already_zombie` and `LC22_direct_death_during_capture` call
`direct_death`, which constructs `FakeChild(0, None, 910101)` before installing
its process-row schedule or invoking `Processes.direct`. The old constructor's
unconditional `len(payload)` raises TypeError before the later intentional
`if payload is not None` no-pipe guard. Thus the recorded failures are reached
through actual case routes, but **before** the intended subject boundary.

`MemoryJournal` and `Processes` construction before this call is in-memory;
`FakeChild` has acquired no pipe descriptor at the failure. This does not erase
surrounding Area allocation/cleanup. The failures establish a controls fixture
bug, not a production/runner defect or two executed subject-boundary checks.

Independent surrounding-subject reading confirms prospective reachability:
LC17 supplies an already-zombie stable/owned-UID row, leading to one fake poll
and `direct_child_already_exited`. LC22 supplies live then same-birth zombie rows;
`admit` raises `ProcessCompleted` before pidfd allocation, and `direct` handles
it through one fake poll and that event. Both unchanged oracles require exit0,
no owned process/pidfd and exactly one poll. These are mocked schedules, never
real waitpid, pidfd, process-ancestry, signal-race or hardware proof.

## Minimal patch and compatibility counterexamples

The complete old-to-new diff has exactly two modified lines:

1. Constructor guard becomes `payload is None or len(payload) <= 32`.
   Exact-None short-circuits length evaluation; child fields still initialize,
   stdout remains None and no pipe is allocated. Non-None inputs evaluate the
   same old length guard. Empty and1–32-byte payloads retain original pipe,
   write/close and error-sink behavior; >32-byte payloads refuse before allocation.
   Unsupported non-length scalar inputs still fail as before. This is not a
   generalized production input interface or an exception-swallowing workaround.
2. Fixed SCRATCH01 becomes SCRATCH02. This is separate prospective namespace
   binding, not the semantic fix or authority to create/reuse anything. Exact
   scratch path/original pin/mode0700/empty-root checks remain unchanged.

Counterexamples challenged: deleting the cap weakens real-pipe safety; replacing
None with empty bytes creates a pipe without a direct_death teardown path;
returning early before assigning child fields breaks the fake-poll oracle.
The chosen guard avoids all three. Assertions, case order/classification,
subject hash, denial guards, process scopes, descriptor HOLD sink, cleanup
allowlist and budgets are otherwise byte-for-byte unchanged.

Earlier C-FD-01, C-FD-02 and whole-case15s corrections remain inherited, with
all associated rejected drafts/failure limitations. Source acceptance is not
proof that those safety/error boundaries have executed successfully now.

## Verification and admission still required

This reviewer read the whole two-line diff, full new design/diagnosis, changed
surroundings, exact subject direct/admit paths and retained old review contract.
No runner/control source was imported, compiled, syntax-checked or executed.
The subject runner remains `346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279`;
its PLAN remains `74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f`.
This reviewer is a subject-runner coauthor; **no independent runner approval**
is claimed. Independence here applies only to verification-authored controls.

Any changed-source execution needs fresh independently accepted launcher/PLAN,
actual request/reviews, original scratch/ancestor/lock/interpreter pins, sole
local-or-CI slot, bounded resource/time/cancellation handling and external
terminal/exit/cleanup reconciliation. New meaningful evidence must show both
previously unreachable mocked paths and preserve the other20 case outcomes.
Compilation alone, two mock PASS labels, or an unobserved preterminal status
cannot establish complete controls or application verification.

Old namespace01 admission is consumed. No automatic retry, replay, replacement
adoption, old-runner execution or database admission is authorized here. Drift
invalidates this exact after-image acceptance. PVU-007 STOP, PVU-011 NO RETRY,
PVA-029 FAIL/no automatic retry, G7/G8 CLOSED and every hardware/PVD/protected-ref/
dependency/version/identity/signing/store/build1017001 restriction remain.

New-source controls executed:0. Application cases:0. Qualified-closure delta:0.
The separate denominators remain19/25 original confirmed,22/37 all confirmed,
2/12 original suspicions; none is an overall readiness percentage.

Only two small permanent independent review files were created by this task.
No artifact, cache, runtime, worker, daemon, server or emulator was produced;
wrapper stop is NOT_APPLICABLE_NO_WRAPPER_LAUNCH. No permanent evidence/source,
shared caches or toolchains were removed. Latest read-only sample showed
workspace22,878,704KiB free, /tmp21,495,152KiB and MemAvailable44,902,056KiB of
65,855,360KiB; these are bounded point observations, not resource reservations.
