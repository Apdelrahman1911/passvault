# NEW Linux inert-control launcher — independent source challenge v1

Reviewer `/root/editor_review`; launcher/plan author `/root`.
2026-09-08. **LAUNCHER REVISE; NO IMPORT/EXECUTION ADMISSION.**

`CONTROL-LAUNCH-INPUTS-v1.json` binds the complete 368-line launcher and 157-line
plan at SHA-256 `485ffb42486576d52ec9c00a1db78f5ee09ac8748538275f7b8542acfbe72b6f`
and `7a39a167b49ccfef50ed2be099529b2025efa6ac3ad20043189e2def82f870a3`.
The reviewer read both entirely, the current new runner PLAN, and the controls
design/callable-return contract. The controls' full independent source review
belongs to `/root/storage`; this does not count as another full controls review.

## CL-R01 — repeat original authority after durable deletion intent

Lines 318–321 check the original BASE and scratch fd/name identity and emptiness.
Line 322 then calls `journal.emit`, which performs pathname checks, optional
filesystem/proc resource sampling, writes and fsyncs before line 323 removes the
scratch name. There is no renewed original-target/emptiness check after that I/O.
Moreover the original coordination lock fd/path is not revalidated after its
admission-time checks (270–276), including before closeout or terminal success.

A pathname or coordination object replacement during that interval would not
be rejected before the destructive call or terminal decision; later absence
alone cannot show that the original root was the removed object. This is a
source contract gap, not an observed hostile-host race or application defect.
The cooperative freeze narrows the threat model but should not replace the
specific original-identity checks promised by this admission.

Requested bounded correction: retain the existing original pins; add a small
lock revalidation and repeat BASE/scratch fd+name identity and emptiness after
the durable removal intent, immediately before the one rmdir. Recheck original
coordination before the terminal decision. Do not retry, discover replacement
originals or claim this makes pathname deletion an atomic inode-conditional
kernel operation. The remaining cooperative race qualification stays explicit.

## CL-R02 — reconcile literal cases and explicit HOLD/bound markers

Lines 342–347 require 22 unique names, PASS outcomes, a cleanup string and the
helper's aggregate PASS status. They do not compare the names/order with the
actual fixed 22-case contract or inspect the newly explicit
`descriptor_settlement_hold`, `within_case_soft_bound`, report identity/counts,
`not_run`, and total-bound fields. An internally inconsistent returned report
could therefore satisfy this launcher predicate. This is a contract-strengthening
requirement, **not a claim that current reviewed controls are known to produce
such an inconsistent report**.

Requested bounded correction: reconcile the exact ordered case names from the
frozen reviewed declaration; require no descriptor HOLD, successful namespace
cleanup and respected per-case/total soft bounds; reconcile fixed runner hash,
expected/executed/passed counts and empty `not_run`. Genuine process/signal and
application counts remain zero. A helper namespace that happens to be empty must
not erase its descriptor/cleanup HOLD. Original empty-root removal may describe
namespace cleanup separately; only actual process exit establishes final OS fd
settlement after uncertainty. No database admission follows automatically.

## Acceptable scope and independent challenges already applied

- Fixed absolute Linux entry, isolated/no-bytecode/no-optimization Python3.12,
  stable same-image input reads, actual root/reviewer approvals, prospective
  request interval, private original flock and exclusive journal are appropriate
  for this one newly defined control batch. No arbitrary command/task/namespace
  override, recursive deletion, child process, network, JVM or old runner entry
  appears in the launcher.
- The bootstrap **description is source-acceptable** for the finite first new
  coordination allocation only: retained no-follow ancestors, durable original
  journal, absent private directory, exclusive regular 0600 single-link lock,
  original capture/fsync, no re-adoption and partial-state HOLD. A read-only
  observation at 23:35:18 UTC found that coordination path absent; this is not
  creation evidence or permission to invent its later original tuple.
- Before actual controls, require the final storage-authored accepting controls
  review, exact corrected source, actual bootstrap evidence, completed request
  and reviewer-authored normative approval. Merely hashing a file named
  `controls_review` does not replace the independent filled-instance check of
  its disposition, author and exact subjects. No such normative approval has
  been supplied by this reviewer at this checkpoint.
- The reviewer checked timer/cancellation setup before allocation, the 240s
  alarm and separate 60s emergency grace, 512MiB address-space/1MiB file limits,
  capped stable reads/journal, launch/running disk/RAM floors, exclusive scratch
  creation, durable original-allocation admission, partial-allocation retention,
  descriptor-only empty-root cleanup and terminal masked-signal boundary.
- Synchronous syscalls and Python signal delivery can overrun polling/alarm
  targets. SIGKILL, host loss or emergency exit can prevent cleanup or a terminal
  receipt. Descriptor-close failure may abort later explicit closes, but actual
  single-process exit closes them through the OS and must remain non-success
  with an outer receipt. A terminal journal event alone is not an observed exit.
- The actual controls use real tiny file/pipe mutations but fake child/process,
  pidfd and signal observations. Their temporary standard-module patches are
  scoped inside the callable and must have unwound before launcher's terminal
  reconciliation. They do not execute the subject `main()`, real Gradle stop,
  ownership/signal behavior, or 105 application methods. Wrapper stop is N/A,
  not an executed successful stop.

No generic framework, new product family, closure or old-scope retry is proposed.
The 22 controls remain unexecuted by this reviewer. PVU-007/PVU-011/PVA-029,
G7/G8, Windows FAIL/cleanup HOLD, PVD/hardware and publication restrictions stay
unchanged. No actual or automatic control/database invocation is authorized here.

Only text/metadata reads and compact review writes ran. No import, syntax check,
compiler, control/test/app process, daemon, scratch directory or temporary cache
was created by this review. At 23:35:18 UTC disk availability was 24,997,620 KiB
and available RAM was 44,536 MiB. No source/evidence/shared cache or unrelated
process was removed; there is no reviewer-owned build worker to stop.
