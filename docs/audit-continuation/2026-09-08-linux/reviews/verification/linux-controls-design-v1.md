# New exact-source Linux runner controls — proposal, not admission

Author: `/root/verification`. Independent control-source reviewers requested:
`/root` and `/root/storage`. Root is the sole executor and launcher/admission
author; this file does not reserve or consume a build/test slot.

**No import, syntax/compile check, control case, project command or application
test has been executed by the control author. All22 controls are source-only.**
These are new controls of the newly authored successor, not execution/import of
an archived runner or replay of the nineteen historical inert specifications.

## Frozen subjects

- New runner: `scripts/audit/linux_database_validation.py`, SHA-256
  `346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279`,
  55,614 bytes / 1,079 LF. The reviewer snapshot is
  `linux-runner-frozen-v3.py.txt` with identical bytes.
- Runner PLAN: `reviews/linux-runner/PLAN.md`, SHA-256
  `74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f`.
- New controls: `linux_runner_controls.py`, corrected review freeze v1 SHA-256
  `7fa09e994c5d92b2000d918e76d5b44d3ffbd131f6c7a14e699c60b0a487456b`,
  39,231 bytes / 821 LF. Any correction invalidates this control freeze and
  requires an exact after-image review before execution.
- Preserved, challenged initial source: `linux-controls-draft-v0.py.txt`, SHA-256
  `36ffcea943de2ae6cf9381d0349b7da981588d6f2ae0d48fd3794ff96df481c6`,
  36,979 bytes / 776 LF. Its original design is `linux-controls-design-v0.md`,
  SHA-256 `44920bc7d528024803ebe0f91605b84b66b5d43c4b4bb6bf1a43b7e376752778`.

The runner's authors are `/root/storage` and `/root`; root's eventual runner
approval is **OWNER_COAUTHOR_APPROVAL**, not an independent vote. The runner's
independent source review is `/root/verification`. Conversely this controls
file is verification-authored and must be challenged by another agent.

## Interface: small root launcher, not another generic runner

The controls have **no executable standalone CLI**; running the file directly
refuses. The independently reviewed, fixed root launcher may load these exact
bytes with a non-`__main__` name and call:

```text
run_controls(scratch_fd=original_retained_fd,
             scratch_path=fixed_path,
             original_scratch={dev,ino,uid,mode},
             source_bytes=exact_captured_successor_bytes,
             emit=durable_bounded_event_writer,
             cancelled=monotone_launcher_cancellation_latch)
```

No arbitrary task/command/test-list override exists. The target runner SHA is
hardcoded and rechecked. Only the exact successor's module definitions are
loaded with a non-`__main__` name; neither its `main()` nor operational
admission/clone/build/cleanup sequence is invoked. There is no historical code
execution/import. The global `R`/`E`/`CANCEL` substitutions are **synthetic unit
fixtures only**, never operational admission, original runtime repair or
authorization to re-adopt old objects.

Root must independently freeze/review its minimal launcher **before running**.
The launcher must:

1. Require the actual approver-authored exact control/runner/design/launcher
   hashes, fresh time window and fixed purposes; preserve the independently
   reviewed approvals. Read/parse/hash the same stable images and revalidate
   the originals under the original coordination lock before mutation.
2. Bind original ancestors/parents and the same current coordination lock;
   attest no other audit-owned local or CI build/test is active. A Windows
   failure/cleanup HOLD must stay recorded; finishing that job is not a pass
   and does not authorize its retry.
3. Use the separately pinned system Python3.12 executable with `-I -B`, no
   optimization, a single-threaded launcher,
   private umask, monotone signal handling installed before allocation and
   root-owned fresh scratch. No ambient project import path, init script,
   network request, JVM/compiler, application/test worker, emulator or server.
4. Exclusively create **only**
   `/root/projects/PassVault/audit-runtime-linux-runner-controls-01`, mode0700,
   with durable pre-allocation intent and original descriptor/pin capture.
   It must be absent before that allocation and empty at handoff. No adoption,
   normalization, replacement namespace or automatic retry after ambiguity.
5. Retain compact source/admission/launcher receipts and a durable capped event
   journal **outside scratch**. `emit()` must persist its event before returning;
   a missing/failed emit or terminal receipt is HOLD, never success. The helper
   caps its own emitted JSON at512KiB; the launcher must enforce its own stdout,
   stderr, input and total evidence caps too. A suggested outer evidence cap is
   1MiB, not permission to discard failures or overwrite prior evidence.
6. Bind and observe disk/RAM before and after, and at case/event boundaries
   during a long run. This task allocates only small synthetic files, pipes and
   Python state, not Gradle caches. Suggested external duration is240s with
   adequate separately bounded cleanup grace; helper soft bounds are180s total
   and15s/case. Synchronous syscalls can outlast a polling target; external hard
   interruption may leave explicit partial/HOLD state. No cgroup/RSS hard cap
   is claimed by the helper.
7. On return, independently reconcile all22 unique outcomes, evidence, failure
   and cleanup results, final cancellation and the launcher exit. The helper's
   preterminal report is **not** the launcher's terminal commit or admission of
   the database run.
8. Close out the original scratch root through its retained original parent
   and target descriptor only after known member cleanup and verified emptiness.
   Retain root/partial-state evidence on uncertainty; never infer success from
   later absence or automatically retry. Record durable intent/outcome and
   parent fsync. Close the launcher's original descriptors and establish its
   process exit/settlement. Do not call Gradle `--stop`: no wrapper is invoked,
   so that obligation is **NOT_APPLICABLE**, not a synthetic successful stop.

## Meaningful boundaries exercised by the22 new controls

| Controls | Boundary and discriminating outcome |
| --- | --- |
| LC01 | Real unchanged file returns exact bytes and identity; avoids vacuous reject-everything credit. |
| LC02–03 | Real synthetic symlink/hardlink inputs are refused by the actual single-link no-follow reader. |
| LC04 | Original file is really changed between reader pre-stat and open; original pre/open identity must reject it. |
| LC05 | Bytes/metadata really change after the first fd read; post-fd identity must reject them. |
| LC06 | Original file is retained under another synthetic name and a new pathname object substituted after fd read; final pathname identity must reject it. |
| LC07 | Original parent directory is retained and a new parent substituted after fd read; original ancestor pin must reject it. |
| LC08 | The real exclusive-file fd is captured, parent fsync fails by exact injected hook, and actual `fstat` must return EBADF afterward. The partial original file is still explicitly cleaned up. |
| LC09–10 | Actual `Run.command` pipe/log/journal collection sees a fake child's0/23 result. The0 control succeeds;23 remains sticky failure with original stop obligation, not clean-settlement success. |
| LC11–12 | Mocked cancellation before launch versus pending after durable launch intent: neither forks; only the latter retains the original pre-fork stop obligation. These are not OS signal-race tests. |
| LC13 | A mocked preexisting stop obligation allows exactly one fake stop Popen; a second request is refused and remains failure, not retry authority. No Gradle stop occurs. |
| LC14 | A real small pipe crosses a reduced4-byte retained-log cap; actual collector truncates retained bytes, latches failure and requests only the fake-owned cancellation path. |
| LC15–16 | Mocked complete two-pass `/proc` rows contain an after-only relevant birth or numeric PID reuse; both HOLD without granting ownership. |
| LC17–18 | Mock direct/owned zombies do not fabricate live-cwd churn; direct completion requires the fake child's explicit poll result. |
| LC19 | The same-birth zombie takes the no-signal branch; a different birth is refused. All actual signal APIs stay prohibited. |
| LC20–21 | Mock parent identity changes before or after child capture; actual admission refuses. The latter also uses a real duplicated private-file fd as an explicitly fake pidfd and proves that acquired fd closes on rejection. |
| LC22 | Mock same-birth death during ownership capture uses the narrow completed-direct exception rather than swallowing generic ownership errors. |

Real new file/pipe/syscall behavior is distinct from mocked process records,
fake Popen, fake stop, fake pidfd and fake signal-pending state. None proves
actual OS process ancestry, pidfd signaling, waitpid, host exclusivity, real
signal timing, Gradle behavior, native loading or105 application cases. The
source-reviewed masked terminal boundary and whole operational admission remain
separate; these controls do not execute `main()` or claim exhaustive coverage.

## Synthetic cleanup and failure behavior

Each fixed `LCxx` area is exclusively created under the original scratch fd,
mode0700, with durable allocation intent and original capture. Only explicitly
created tiny fixture members are registered. The helper retains their original
fd-derived object identity; intentional content, link and rename changes are
recorded fixture operations, not rebasing authority from current pathnames.

Before any removal it checks the exact member set and original identities,
then uses retained original parent descriptors and per-target intent/outcome,
parent fsync and verified absence. It never calls `rmtree`, walks arbitrary
source/cache trees, follows a symlink for deletion, uses process-name killing,
or deletes scratch root itself. A symlink exists only as an expressly synthetic
reader fixture targeting another member of its own area. There is no invented
regular-file/parent-directory same-device equality on this VPS.

An unknown/replaced member, incomplete allocation or cleanup/fsync/receipt
failure is **HOLD**; that area is not retried and further controls stop when
cleanup remains HOLD. Original owned descriptors are closed; uncertain objects
are retained for separate root disposition. A failed assertion with successful
known-original cleanup can be followed by the next distinct case, never the
same case again. Missing cases remain not-run. Only the original launcher's
separately reviewed final empty-root closeout can finish resource cleanup.

## Independent source challenge and correction v1

`/root/storage` independently reviewed all776 initial source lines and raised
**C-FD-01 / REVISE**: after `Area.__init__` acquired a directory fd, a failing
pin/mode check, parent fsync or original-allocation receipt occurred before the
constructor returned. Its caller therefore had no `Area` to close. A nested
constructor likewise had not been registered in the parent's members. This is
a reachable **source failure schedule**, not an observed control incident.

The author accepted that challenge. V1 attempts one acquisition-local close on
every pre-construction-success exception, preserves the original failure (and
any close failure) and retains the partial namespace/HOLD; it neither registers
an incomplete object afterward nor deletes/adopts it. The independently flagged
`capture_open` fstat/registration window and `parent_guard` token-fstat window
now have equivalent immediate close scope. The fake-pidfd dup/registration
window also closes its acquired original if registration fails.

Retained-fd settlement now attempts every known original despite another close
failure, preserves settlement errors as case FAIL/cleanup HOLD, and never
retries a possibly released fd number. These changes do not add cases, broaden
cleanup authority or claim successful close after a failed close syscall.
All22 controls remain unexecuted; this after-image requires independent review
and fresh root launcher/admission before any import or execution.

## Current accounting and resources

At source freeze:22 declared controls, **zero executed controls**, zero real
child processes/signals, zero Gradle/compiler/application tests and zero PVA
closures. The105 selected application methods retain their prior source-only
status. Product denominators and all STOP/NO-RETRY/CLOSED/hardware/PVD limits
are unchanged.

A read-only resource observation while writing showed worktree available
27,339,600KiB, `/tmp`19,397,552KiB, MemAvailable38,862,556KiB of65,855,360KiB.
This was not an execution reservation or reclamation by the author. Only this
small permanent controls source/design and compact reviewer snapshots were
created; no runtime, cache, build artifact, worker, server or emulator exists
from this authoring task. An installed Git clone manpage lookup was unavailable;
no fallback was fetched or Git clone invoked. A prior `cp --no-clobber` emitted
a portability warning while preserving a reviewer snapshot; the verified v3
copy used `--update=none`. No source overwrite or test result is inferred.

The v1 correction's read-only resource sample was worktree available
27,020,412KiB, `/tmp`22,375,800KiB, MemAvailable45,976,776KiB of65,855,360KiB,
with no configured swap. This author performed no reclamation or execution;
the compact v0 source/design snapshots are permanent review evidence, not
temporary build outputs.
