# Independent source challenge: new Linux runner controls

Reviewer: `/root/storage`. Controls author: `/root/verification`.
The reviewer is a **coauthor of the subject runner**, not an independent
reviewer of that runner. This is independent review of the separately authored
**controls source and its declared evidence boundaries only**.

**Current disposition: ACCEPT_SOURCE_ONLY at controls v3 / separate root
launcher and execution admission still required.** No controls,
runner imports, compile/syntax probes, application tests, builds or cleanup
operations have been executed by this reviewer for this review. A source
acceptance cannot grant the root launcher's separate execution admission.

Final accepted controls: SHA-256
`320f363e5f9bdce0e806a624f7e9fd9fc943690be57f8c75c9f43f865c04903d`,
43,044 bytes / 893 LF. Final accepted design: SHA-256
`1292305d4a0f1729be7016002c9f1bc99f37487077aea4f4f3d1aa1854723921`,
17,723 bytes / 264 LF. The initial complete files plus every complete v0-to-v3
diff and changed surrounding code were reviewed. Any further source/design
change invalidates this exact after-image acceptance.

## Reviewed initial identities

All 776 LF of `reviews/verification/linux_runner_controls.py` and its complete
design were displayed/read, with relevant `Directories`, `Journal`, `Run` and
`Processes` definitions compared against the exact subject runner:

| Input | SHA-256 |
| --- | --- |
| Initial controls, 36,979 bytes / 776 LF | `36ffcea943de2ae6cf9381d0349b7da981588d6f2ae0d48fd3794ff96df481c6` |
| Initial `LINUX-CONTROLS-DESIGN.md` | `44920bc7d528024803ebe0f91605b84b66b5d43c4b4bb6bf1a43b7e376752778` |
| Subject `scripts/audit/linux_database_validation.py`, 55,614 bytes / 1,079 LF | `346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279` |
| Subject `reviews/linux-runner/PLAN.md` | `74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f` |

The root launcher, its actual approval packet, original scratch/parent/lock
pins, external duration and final settlement are **outside this source review**.
They must be reviewed/admitted independently before any control is run.

## C-FD-01 — constructor failure before caller ownership registration

**Initial disposition: REVISE. Evidence: source failure schedule, not execution.**

`Area.__init__` creates a synthetic directory and acquires `self.fd`, then
checks its metadata, fsyncs the parent and emits the original-allocation receipt.
There is no local close-on-error around those steps. At either caller:

```text
area = Area(...)             # run_controls: area is still None until return
child = Area(...)            # directory: child is not in parent.members yet
```

the caller cannot reach the acquired descriptor if the constructor raises.
For example, directory creation/open succeed, then the parent fsync raises EIO
or the durable `fixture_allocation_original` emit fails. The outer cleanup sees
`area is None`, or the child is absent from the parent's registered members.
The descriptor is not explicitly closed by that cleanup path. The incomplete
namespace must remain HOLD; attempting to re-adopt or remove it is not the fix.

The controls author accepted this challenge and proposed closing the local
descriptor on all pre-success exceptions, retaining failure/partial state.
The author also agreed to inspect the equivalent local acquisition windows in
`capture_open` and `parent_guard` and to prevent one descriptor-close failure
from suppressing the other original-close attempts or hiding case HOLD.
The complete v0-to-v1 diff and changed surrounding code were then reviewed:

- Corrected v1 controls: SHA-256
  `7fa09e994c5d92b2000d918e76d5b44d3ffbd131f6c7a14e699c60b0a487456b`,
  39,231 bytes / 821 LF.
- V1 design: SHA-256
  `beb25c70d76f396b215f116c3d776147f070f9a4463ef29412cf9e0119fe8de0`.
- Preserved `linux-controls-draft-v0.py.txt` and `linux-controls-design-v0.md`
  matched the two original hashes above.

**C-FD-01 after-image: ACCEPT_SOURCE_ONLY.** Constructor failure now attempts
one local close before returning control; no incomplete object is registered,
deleted or re-adopted. The acquisition-local scopes in the noted callbacks
were likewise corrected. This is not an executed FD-closure observation.

## C-FD-02 — operation-level descriptor uncertainty loses cleanup HOLD

**V1 disposition: REVISE. Evidence: source failure schedule, not execution.**

For example, `parent_guard` has a failing `os.close(token_fd)` in its final
settlement. It preserves that failure and raises `ControlFailure` with the
descriptor-settlement HOLD text. However, `run_controls` catches it as an
ordinary operation error. Subsequent successful `Area.cleanup()` overwrites
the case cleanup status with `ORIGINAL_SYNTHETIC_MEMBERS_REMOVED`, and the next
case starts. An acquisition-local close failure inside an already constructed
area has the same classification problem.

The overall controls report still fails, so this is **not** a false overall
pass. But an unresolved descriptor-close outcome does not remain case cleanup
HOLD as the design promises. The correction must propagate descriptor
uncertainty independently of ordinary assertion failure, attempt other still
authorized original closes, never retry an ambiguous fd number, and stop
subsequent cases on that uncertainty.

V2 controls `26c8908575684f1c7ddc804129015b1f4ad45d14ef87532c8393da3a38f176f6`
(42,154 bytes / 878 LF) and design
`fa023a997c9ec9d8566403e37ac884cfc59cf2ad22f8942228fe5e2907fdfcdc`
were reviewed against the exact retained v1. Typed `DescriptorHold` plus a
bounded cause/context walk correctly preserves the original parent-token
failure schedule through ordinary exception propagation; main no longer lets
successful namespace removal clear that signal.

**Residual at v2: REVISE.** A real fake-child pipe-writer close can fail inside
`FakeChild` construction. `close_original` raises `DescriptorHold`, but the
actual subject's `Run.collect_command` catches that fake-Popen exception and
converts it into record/journal error strings before returning. The later
positive-command assertion raises an ordinary `ControlFailure` with no
cause/context link to the consumed descriptor exception. Main therefore still
sees no descriptor uncertainty, reports successful namespace cleanup and can
start the next case. Overall FAIL is preserved; cleanup HOLD is not. The
author was asked to retain uncertainty monotonically outside exception
propagation, or equivalently handle this conversion boundary explicitly.

The subject's `Directories.close()` can itself abort on its first close error.
Wrapping that in HOLD is appropriate; it does not prove that every subsequent
subject-owned close was attempted. Already-attempted fd numbers must not be
retried. Remaining helper-owned close attempts and root's separately verified
process exit are distinct from successful subject-descriptor settlement. No
change to the frozen subject runner is requested by this controls review.

**C-FD-02 final v3 after-image: ACCEPT_SOURCE_ONLY.** The controls module now
binds an active, case-local list before allocation. Creating `DescriptorHold`
appends to that list immediately, before the tested subject can consume the
exception into strings. Main uses the same list after required cleanup to
force case FAIL/cleanup HOLD and stop further cases. It does not erase the
list on an ordinary return or successful namespace removal. The active
binding ends only after durable case-result emission; ambiguous reentry is
refused. V2's exact source/design snapshots were verified before this diff.

Thus the parent-token and swallowed-fake-Popen source schedules both preserve
descriptor uncertainty at the reviewed v3 boundary. The design now correctly
qualifies the subject's aborted close sequence and requires root's separate
process-exit settlement. No actual close failure, case, import or root
settlement has been executed or verified by this source acceptance.

## Full-case soft-time qualification

V1 checks the 15-second case bound before file/descriptor cleanup, then records
`result.seconds` after cleanup. A slow cleanup can therefore leave a case PASS
with recorded seconds above the declared per-case bound. The author was asked
to enforce the observed full-case soft bound after required cleanup, or narrow
the design/receipt explicitly to the operation-only bound. No cleanup should
be skipped to satisfy a time target. The overall 180-second target and outer
launcher's bound remain separate. This is a source observation, not an actual
timeout or stress-test result.

**V2 after-image: ACCEPT_SOURCE_ONLY for this time-bound correction.** The
observed operation plus fixture/descriptor cleanup time is now sampled before
result emission and compared with 15 seconds; an overrun sets case FAIL after
required cleanup is attempted. Cancellation is sampled again at that boundary.
The design expressly excludes durable result-emission latency from that case
sample and retains the overall/outer-launcher qualifications.

## Reviewed useful boundaries and countercontrols

- LC01 provides a positive unchanged-file control; rejecting every input cannot
  satisfy it. LC02/03 use real synthetic symlink/hardlink inputs.
- LC04/05/06 really mutate content or replace the pathname at distinct hooks
  inside the actual stable reader. LC07 retains/replaces the parent directory.
  Hooks set their one-shot latch before nested fixture operations, avoiding
  recursive hook re-entry. These are real file schedules with injected call
  hooks, not natural-race or hostile-filesystem proofs.
- LC08 injects a parent-fsync failure after a real exclusive-file acquisition
  and requires actual EBADF from the retired descriptor; its partial fixture
  remains registered for known-original cleanup.
- LC09/10 exercise the actual command collector with fake children but real
  small pipes/log/journal IO. Exit23 must remain sticky failure; a cleanly
  settled fake child does not erase command failure.
- LC11/12 distinguish cancellation before intent from synthetic pending
  cancellation after durable intent. Neither launches a real child; only the
  latter preserves the mocked launch's original stop obligation.
- LC13 asserts one fake stop and refuses a second request. No wrapper stop
  actually occurs; the helper correctly records it NOT_APPLICABLE.
- LC14 reduces the real collector's retained-log limit to four bytes, checks
  retained bytes and error state, and records only fake-owned cancellation.
- LC15/16 distinguish after-only relevant births and PID reuse using two
  explicitly mocked process snapshots, without granting ownership.
- LC17/18/19 distinguish same-birth zombies from live-cwd drift or a wrong
  birth. No OS signal or waitpid evidence is claimed.
- LC20/21 bracket child admission with the parent's original identity. LC21
  uses a real duplicate of a private file descriptor, explicitly **not a real
  pidfd**, and checks closure when the final parent check fails.
- LC22 tests the narrow same-birth death exception during direct admission,
  not a generic exception-swallowing path or actual OS process race.

The fixed controls do not invoke `main()` or the application, clone, Gradle,
whole operational admission, production native loading or hardware behavior.
The real/mocked distinctions must survive any later pass report. Twenty-two
declared cases are not an exhaustive F01–F07 or whole-runner validation claim.

## Cleanup, resource and status qualifications

The helper receives the original fresh scratch descriptor/pin from root. It
does not create or remove that root, walk/delete arbitrary trees, or adopt a
preexisting namespace. Known synthetic member sets and original identities are
checked before removal. It retains uncertainty and stops after cleanup HOLD;
a failed assertion with successful original cleanup may proceed only to the
next distinct fixed case, never retry the same case.

The helper's 15-second per-case and 180-second total bounds are polling targets,
not syscall hard deadlines. The 512-KiB emitted-evidence cap does not replace
the launcher's external evidence/resource enforcement. Missing receipt,
propagated exception, late cancellation or failed root cleanup is HOLD, not
success inferred from the helper's preterminal report or a later empty path.

This review used text/metadata reads and small permanent review files only.
A read-only sample showed 27,193,272 KiB available on the worktree filesystem,
23,102,892 KiB on `/tmp`, and 47,669,856 KiB MemAvailable of 65,855,360 KiB.
This reviewer started no build, worker, server, emulator or background job and
created no runtime, cache or generated artifact. No cleanup/retry authority is
created by these observations. Product closure/test counts remain unchanged.

One read-only snapshot lookup initially used the nonexistent v1 `draft`
filename; the design's actual `linux-controls-frozen-v1.py.txt` name was then
used and its expected hash verified. Diff exit1 denoted displayed source
changes, not a failed test. No outcome rests on the missing lookup.
