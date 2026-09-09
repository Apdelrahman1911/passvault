# Linux controls source v4 — minimal fixture correction, fresh02 proposal

Author: `/root/verification`; 2026-09-09 UTC. Independent finding and exact
after-image reviewer: `/root/storage`. Root alone owns launcher, coordination,
filled-instance admission and any execution. **SOURCE ONLY; this design is not
an approval, a slot reservation, a retry authority or database admission.**

## Bound source and inherited contract

- New, distinct source: `linux_runner_controls_v4.py`, SHA-256
  `dffa884b09c7786e763c9becc3b0b220242b5c9555288bf1052e9e16912fa426`,
  43,063 bytes / 893 LF. Source revision4 is not execution attempt4.
- Exact two-line delta: `LINUX-CONTROLS-V4.patch`, SHA-256
  `6ddef57c4463e97e170ba106e8786763f426aac7ca597f2b0798ea6b2c0210f5`,
  941 bytes / 20 LF.
- Unchanged subject: `scripts/audit/linux_database_validation.py`, SHA-256
  `346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279`;
  unchanged subject PLAN SHA-256
  `74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f`.
- The full prior control design is retained verbatim at
  `LINUX-CONTROLS-DESIGN.md` and `linux-controls-design-executed-v3.md`, both
  SHA-256 `1292305d4a0f1729be7016002c9f1bc99f37487077aea4f4f3d1aa1854723921`.
  Its unchanged interface, 22 named cases, per-case boundaries, denial guards,
  descriptor-error sink, original-identity cleanup, budgets, qualifications and
  required external terminal/exit adjudication apply to this proposal. This
  document overrides only its source binding, execution namespace and historical
  source-only status as specified below. Read both documents for review; neither
  is executable authority.

## Preserved failed execution — not replaced or reclassified

Root executed the previous exact source **once** at namespace01. The unchanged
`linux_runner_controls.py` and `linux-controls-executed-v3.py.txt` both have
SHA-256 `320f363e5f9bdce0e806a624f7e9fd9fc943690be57f8c75c9f43f865c04903d`.
The prior source-only design remains a historical pre-execution checkpoint,
not a claim that the source has still never been executed by root.

- Original request: `CONTROL-REQUEST.json`, SHA-256
  `8d2e20fcb074d5ffeb1775f15e7116971dab8fa46448634fb68dfdcce9539931`.
- Original journal: `CONTROL-events.jsonl`, SHA-256
  `75fcebbd3c666518f9b2160d186e83ad9eebb9b8774c016eae043e88e40c6eb7`,
  123,873 bytes / 213 records, including 22 ordered case-result records:
  20 `PASS`, 2 `FAIL`. `/root/editor_review` independently adjudicates their
  semantics, execution and cleanup; this author design is not that review.
- Original external receipt: `CONTROL-LAUNCH-RECEIPT.json`, SHA-256
  `39a7daf48e4096fab5245c858f71aef1fe7eae507dd88b424078774fca1217f1`;
  tool chunk `8b0448`, exit1. The consumed source, design, approvals, launcher,
  journal and receipts remain unchanged. The namespace01 admission cannot be
  replayed, reused or silently relabeled as namespace02.
- `LC17_direct_already_zombie` and `LC22_direct_death_during_capture` failed
  `TypeError: object of type 'NoneType' has no len()` before the subject direct
  child boundary. This is author finding `C-FIXTURE-01`; it is not an established
  subject-runner regression, a new PVA family or an application test result.

## Exactly two source changes

1. **Fixture correction, line475:**
   `check(payload is None or len(payload) <= 32, 'fake child payload cap')`.
   `None` is the already-intended no-pipe sentinel used by both direct-death
   cases. It now reaches the existing `self.stdout = None` / non-None pipe guard
   without calling `len(None)`. There is no pipe or descriptor allocation on
   that sentinel path. Every non-None payload still has its original32-byte cap;
   the real-pipe command controls and descriptor-settlement paths are unchanged.
   No case assertion, mock schedule, expected disposition, target SHA, subject
   implementation or execution restriction changes.
2. **Separate namespace binding, line27:** the fixed scratch path becomes
   `/root/projects/PassVault/audit-runtime-linux-runner-controls-02`. This is
   not part of the semantic fix and does not authorize creation. It is required
   to prevent the new proposal from accepting the consumed namespace01. The
   existing `run_controls` exact-path, original-pin, mode0700 and empty-directory
   checks remain unchanged.

Changing `None` to `b''` at the callsites is deliberately not the fix: it would
allocate a real pipe in the direct-only fixture, whose function has no pipe
teardown. Removing the payload cap would unnecessarily change real-pipe safety.
The exact sentinel guard avoids both compatibility and cleanup regressions.

## Prospective evidence and non-authority

The original22 case names, order and classifications remain unchanged. A
separately admitted **one changed-source controls execution** would need to
show the two formerly unreachable mocked branches as well as retain meaningful
evidence for the other20 cases. `LC17` must reach the same-birth zombie path;
`LC22` must reach `ProcessCompleted` during fresh capture. Their unchanged
assertions require one observed fake-child poll with exit0, no owned process or
pidfd, and the expected `direct_child_already_exited` journal event. A passing
mock is still not real `waitpid`, pidfd, signal-race or application evidence.

Root must first obtain actual independent after-image review and separately
review/freeze a distinct launcher, PLAN, request, original parent/lock/interpreter
pins, time window and author/independent approvals. Root's proposed new launcher
and evidence names use suffix02; exact hashes belong in the later request, not
guessed here. All old files and the consumed namespace01 stay out of mutation
scope. Drift, failure, cancellation, occupation or cleanup uncertainty consumes
the new instance; there is no automatic retry, replacement adoption or fallback
to the old launcher. Only one audit-owned build/test job may run locally or in
CI, with root the sole executor.

Unchanged control limits: 15s per full case including cleanup, 180s total
cooperative control budget, 512KiB emitted-control evidence, 32KiB fixture bytes
and24 tracked members per case. The separately reviewed launcher supplies its
own bounded outer execution/cleanup handling and resource checks. All original
scratch-member cleanup and descriptor uncertainty handling remain required,
even after assertion failure. No Gradle wrapper is launched, so wrapper stop
is `NOT_APPLICABLE_NO_WRAPPER_LAUNCH`, not a successful Gradle stop observation.

The author performed only bounded source/data reads and exclusive writes of
permanent source/review files: **no import, syntax/compile probe, control/test
execution, subject/control child process, daemon, emulator or generated build output**. New
source execution evidence is zero until root supplies separately admitted
results. The old20/2 result remains independently reportable regardless of any
later outcome. Current105 selected application executions remain zero.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED,
the original Windows failure/cleanup HOLD, hardware gaps, eight PVD boundaries,
occupied1017001 and all protected-ref/dependency/version/identity/signing/store
restrictions remain. Qualified-closure delta is zero: original19/25, all22/37,
original suspicions2/12; these are separate denominators, not readiness scores.
