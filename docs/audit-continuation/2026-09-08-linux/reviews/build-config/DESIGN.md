# LC-C05/C06 — proposed inert controls, not execution admission

Original author: `/root/build_config`; exact-subject v3 rebind author: `/root`.
Independent source challenger: `/root/android32`
(v3 successor review **pending**, not inferred from earlier helper acceptance). Root is the
only execution/build owner. `/root/storage` authors the separate proposed outer
launcher; `/root/editor_review` independently challenges that launcher.

This successor preserves the rejected v1 as `CLOSEOUT-CONTROLS.v1.py.txt`
(`acb44cae0c8ac31729f043477fa19683b20487d13fcef74965f1dd71f96d79c4`)
and `DESIGN.v1.md`
(`ab4c9a02873652b9a60482fab2f522aff33202ab1ee075f73271e986b642cc20`).
The consolidated independent source review is
`../android32/CLOSEOUT-CONTROLS-V1-SOURCE-REVIEW.md`
(`61568e00dbd6f2b16d9dbf9d8b8d99bc7d07175163f4b17f9e7da6bcf8d4ca7c`)
and paired JSON
(`5f7021de07eb6dfca45d593ceb0c20d9b4353931a4574286dee28ccfde141adb`).
It required LCC-ORACLE-01's discriminating B10 pin-only oracle, LCC-DOC-01's
retained-original Linux coordination qualification, LCC-DOC-02's historical/current
denominator distinction and the reporting limitations stated below. V1 was
**REVISE / NOT_ADMITTED**, not an executed test failure. The v2 code successor
was B10's same-content real rewrite/readback oracle and the adjacent B11 actual
readback assertion; IDs/count/interface/source subject were unchanged at v2.
V2 was subsequently independently accepted for its exact b167 subject only;
all execution admission remains pending.
An initial successor-edit patch was refused for stale context; both active v1
hashes were rechecked unchanged before the corrected source edit. No code ran.

V3 preserves exact v2 as `CLOSEOUT-CONTROLS.v2.py.txt`
(`bdee7e285f45f78e56d99fc6ef122767de00400d952f1e68c1d47b5d9bf35a53`)
and `DESIGN.v2.md`
(`1e23c7432b9ea297e96ab56e7bd4742e5019af5c0726ce6ad28a2bdc8de3c8bb`).
The old `../android32/CLOSEOUT-CONTROLS-SOURCE-ACCEPTANCE.json`
(`f564f4a4ee1063538562f17bd7f3b4ebd0f3eaf982edf79fffa0d3445aac57ae`)
is historical v2 acceptance, not v3 authority. V3 changes only the three literal
subject hash/size/LF constants; all28 case bodies, order, callable interface,
bounds and original-only fixture cleanup are unchanged. A root authoring patch
was refused for a context mismatch after the two v2 snapshots were saved;
both active v2 hashes were rechecked unchanged (tool477762) before correction.
No target code was parsed, imported, compiled or executed by that edit.

This is a finite callable for **28 new control cases**, currently **0 executed**.
The 13 journal cases and 15 bootstrap/rehash/HOLD cases target effects requested
in the accepted closeout reviews. They add **zero application test/family closure
credit**, do not inherit the older capture/control count, and do not discharge
the unfilled actual C01–C07 closeout instance obligations. This source proposal
is not an admission to execute, import, compile, syntax-test or allocate scratch.

## Exact subject and prior review boundary

Paths below are relative to `/root/projects/PassVault/passvault-linux`:

| Input | SHA-256 | Bytes / LF |
| --- | --- | --- |
| `scripts/audit/linux_database_closeout.py` | `8437c693d5a1becb8ed06ed549a5e837b8e4820c83a047c841fbcc0289f4b517` | 59,609 / 1,100 |
| `docs/audit-continuation/2026-09-08-linux/reviews/linux-closeout/PLAN.md` | `0b21ebfee0134c2853cfa8f3d17f9fef1b9acf812329581a93c0790c69930c5d` | 34,814 / 561 |
| `.../reviews/baseline-coverage/LINUX-CLOSEOUT-SOURCE-REVIEW-v1.md` | `d51f668387791584c80ac418cf0d1716fa85a20ebf21e236ff85ff251b1c7ecf` | prior rejected source review retained |
| `.../reviews/baseline-coverage/LINUX-CLOSEOUT-SOURCE-REVIEW-v2.md` | `8f5ce3844c4fff97200282b8ed16e11887ee3d74a44151a32671f6764b2dd8bf` | source correction review only |
| `.../reviews/baseline-coverage/LINUX-CLOSEOUT-SOURCE-ACCEPTANCE.md` | `9680e82520b0e47e0341ea581ee259cc98a511d841b892ecd031c2e8ecd15286` | historical b167/cffc acceptance; whole-contract viability subsequently rejected |
| `.../reviews/baseline-coverage/CLOSEOUT-LAUNCHER-CONTRACT-SUCCESSOR-REVIEW.json` | `1b84d746f7483970a5349a963b45d7b68c31e32a70d741f5ace08b4346c1fe5a` | narrow intermediate evidence-field correction review, not final hash acceptance |
| `.../reviews/baseline-coverage/CLOSEOUT-REGISTRATION-SUCCESSOR-REVIEW.json` | `c4b7e9d03dc8e5160c0f6cf628cbd4ee97e7dcb27707d73059c61f17576c6370` | final subject registration/source delta review only |

This is a real selected-code delta, not only a new hash: `Directories.open`
now removes its newly registered `fds[key]` entry before the local close if
the paired `pins[key]` insertion fails. That prevents a second close attempt on
the stale entry during later settlement. These28 cases do not inject that
second-store failure; separately authored four registration controls remain
unexecuted and outside this callable. No actual FD reuse or unrelated close was
observed. The other selected definitions and nested `bootstrap_tick` are unchanged.
The helper's two `evidence_files` schema consumers are outside this selection;
separate17 full-contract controls remain unexecuted and are not silently added.
Source acceptance of this rebound callable and its final launcher chain must
be obtained separately before any instance can be admitted.

The callable refuses any helper hash/byte/LF mismatch. After a future independent
admission only, it parses those supplied bytes as data and selects the literal
top-level definitions `Hold`, `require`, `canonical`, `sha`, `identity`,
`absolute`, `Directories`, `Journal`, and `recheck_inputs`. It also selects the
literal nested `main.bootstrap_tick` AST definition. Nothing rewrites those
bodies. The callback is compiled at the selected module scope, with its former
closure inputs (`started`, `CANCEL`, `LIMITS`, `SIGNALS`) explicitly supplied in
that isolated subject namespace; this is **not main/closure integration proof**.

The exact helper's imports, fixed runtime paths/constants, top-level statements,
`main`, `admission`, `Guard`, `Forest`, process/mount/resource probes, actual
locks, target traversals and deletion functions are **never compiled or run**.
The old validation runner and archived helpers are never read/imported/executed.
Only the newly authored harness's small, original-bound fixture cleanup uses
`unlink`/`rmdir`. No Gradle, application, database, emulator or native build starts.

## Callable/launcher ownership contract

The only public entry is:

```text
run_controls(*, scratch_fd, scratch_path, original_scratch,
             source_bytes, emit, cancelled) -> terminal dictionary
```

The separate **new** outer source paths, not yet admitted or supplied by this
proposal, are `reviews/linux-closeout/LAUNCH_CONTROLS_01.py` and
`reviews/linux-closeout/CLOSEOUT-CONTROLS-LAUNCH-PLAN.md`. No old launcher,
consumed scratch namespace or occupied evidence output may be replayed/reused.
Fresh independent admission must bind the **retained verified original Linux
coordination lock**; do not recreate, replace or adopt a different current inode.
Historical macOS lock/process identities and consumed-run permission are not
authority for this new Linux attempt. Fresh admission does not mean a new lock.

Before that launcher may call this function, root and its independent reviewer
must bind the exact harness/DESIGN/helper/review/launcher bytes, fresh original
Linux coordination and no-overlap evidence, current resource floors, durable
evidence destination and its original parent/name/FD witnesses, one-shot request,
external cancellation/timeout/settlement contract and bounded cleanup. The outer
entry must use `/usr/bin/python3 -I -B` and `umask077`, must not spawn a competing
build/test job, and must never treat these source documents as that admission.

The launcher **exclusively creates**, records and retains an original FD and
no-follow parent/name witness for:

```text
/root/projects/PassVault/audit-runtime-linux-closeout-controls-01
```

It must be genuinely new, empty, owned by the executing UID and mode0700. The
callable requires its exact spelling and an `original_scratch` dictionary with
**exactly** `dev`, `ino`, `uid`, `mode`, as observed at creation. It checks the
borrowed original FD and pathname; it independently retains no-follow ancestor
directory FDs for its own fixture lifetime. The callable neither creates this
top directory nor closes its borrowed FD nor removes its top name. Root owns
those responsibilities. A pre-existing directory cannot be adopted merely
because empty or otherwise plausible. All evidence must be outside scratch.

`source_bytes` is the bounded exact accepted helper byte string; the callable
does not open the real helper or any old runtime. `emit(dict)` must durably retain
bounded external evidence **or raise**; swallowing evidence failure is forbidden.
`cancelled()` must expose the outer monotone cancellation latch and must not
clear it, throw asynchronously from the outer signal handler or start work.
The outer installs its handling **before** creating scratch. Callback exceptions,
source/entry mismatches and interrupted terminal delivery are outer HOLD, not
evidence of successful control completion.

## Finite cases and effect assertions

Each case gets one fresh subject namespace and a separate original-FD fixture
registry. Fixtures are cleaned and descriptors settled **before** advancing to
the next case. A failing case stops the suite; later case bodies remain unstarted.
The fixed names admitted for a case are only its exact case ID plus `.a`, `.b`,
`.old`, or `.link`. The implementation below does not use all four in any case.

### Journal / LC-C05 — 13 planned cases

| ID | Scheduled effect and required observation |
| --- | --- |
| `J01_progress` | Real exclusive private journal creation and three completed records. Parse actual bytes; require ordered sequences0,1,2, increasing completed length, current last-completed FD pin and unchanged stable original dev/ino/uid/mode/nlink. |
| `J02_equal_rewrite` | Real in-process `pwrite` changes `seed` to `sEed` in the old record with exactly unchanged length. Require observed pin drift and rejection before any subsequent journal write/fsync. A coarse filesystem that does not expose drift is a failed control/HOLD, not an invented pass. |
| `J03_truncate` | Real truncation by one byte; require prior completed pin/length rejection and no append I/O. |
| `J04_external_append` | Real out-of-writer `pwrite` adds one byte; require prior pin/length rejection and no append I/O. This is not a second process. |
| `J05_name_replace` | Rename the original to its allowlisted `.old`; exclusively create/capture a distinct synthetic `.a` replacement. Require prior-pin or pathname rejection (rename may change original ctime), never original adoption or append. |
| `J06_hardlink` | Real link to the known `.link`, nlink2; require rejection before append I/O. |
| `J07_positive_short_writes` | Delegate every source write to a real at-most7-byte `os.write`. Require multiple real calls, complete parsed record and completed state progression. |
| `J08_zero_write` | Explicit injected return0, no actual write. Require refusal, unchanged real bytes, unchanged completed state and incomplete latch. |
| `J09_partial_then_zero` | First call really writes7 bytes; second is injected0. Require exactly7 residual bytes beyond the old completed length and no completed-state advance. |
| `J10_fsync_error` | A real whole record is written, then only the source journal's fsync gets injected `EIO` rather than an actual successful fsync. Require parsed residual record, unchanged completed state and incomplete latch; no claim about physical durability. |
| `J11_postwrite_name` | After a real record write and real fsync, perform the same known original rename/new replacement. Require the source's final postappend descriptor/name/length rejection. |
| `J12_postwrite_mode` | After real write/fsync, change the original mode600 to400 by real `fchmod`/fsync. FD and pathname still agree; original stable mode no longer does. Require postappend rejection. |
| `J13_postwrite_nlink` | After real write/fsync, add known real hardlink. FD/name still agree but stable original nlink differs; require postappend rejection. |

Every negative journal case also checks `ok=False` and unchanged `last`, `used`
and `sequence`. It makes **one separately scheduled negative guard call** on
that incomplete synthetic journal and requires immediate `already incomplete`
rejection with unchanged I/O counters/state. This is not an automatic retry,
recovery, replay or resumed execution of an actual closeout/helper namespace.
Residual hashes and lengths use original retained read FDs, not the replacement
pathname. The intentionally incomplete synthetic files are never run evidence.

### Bootstrap / LC-C06 and rehash/HOLD guards — 15 planned cases

| ID | Scheduled effect and required observation |
| --- | --- |
| `B01_valid_bootstrap` | Exact real original directory binding, valid input bytes/pin/hash, EOF, real input and parent fsync, directory verify, and closed input descriptor. |
| `B02_cancel_before_read` | Set the isolated synthetic CANCEL latch before read. Require callback refusal with zero source file opens/reads. No OS signal is sent. |
| `B03_deadline_before_read` | Explicitly pre-expired start state evaluated by the real monotonic clock. Require deadline refusal with zero source file opens/reads. |
| `B04_cancel_during_read` | First real read is limited to4 bytes, then sets the isolated CANCEL latch. Require refusal before any second read and closure of the original input FD. |
| `B05_deadline_during_read` | First real4-byte read, then bounded real150ms sleep against a100ms source budget. Require refusal before second read, not a mocked clock result; close input FD. |
| `B06_cancel_during_bind` | First binding tick opens `/` read-only; at second tick set synthetic CANCEL. Require exact refusal before the next directory open. |
| `B07_deadline_during_bind` | Same reached binding boundary, real150ms sleep against100ms budget. Require refusal with only the first original bound. |
| `B08_directory_replace` | Capture original empty child directory and exact pin set; rename it to `.old`, exclusively create/capture distinct replacement. Fresh source binding must reject the exact original directory map. No file read/write may follow. |
| `B09_rehash_unchanged` | Source `recheck_inputs` reads real unchanged bytes with keep=False, validates original pin and SHA, closes its input FD and returns. |
| `B10_rehash_pin` | Real same-content `pwrite`/fsync; require actual readback bytes/hash unchanged but observed pin changed, retaining the old expected pin. Require rehash rejection: removing only its pin comparison would incorrectly accept this case despite the unchanged digest. Missing observable pin drift is HOLD/no retry. |
| `B11_rehash_digest` | Real same-size byte change (`only` to `ONLY`), then **explicit seal-input injection** supplies the current pin with earlier captured bytes. Require the actual computed content digest, independently of pin mismatch, to reject. |
| `B12_rehash_cancel` | Two real synthetic inputs. Set synthetic CANCEL after first input's real EOF; require the second input to be refused before its file open/read. |
| `B13_rehash_deadline` | Same between-input boundary, with real150ms sleep against100ms budget; require refusal before second file open/read. |
| `B14_hold_after_cancel` | Create/complete a valid journal, then source bootstrap callback rejects the synthetic CANCEL latch. An explicit best-effort HOLD record must still complete and advance real bytes/state, without calling the cancelled bootstrap callback. |
| `B15_hold_after_deadline` | Same HOLD availability after the pre-expired bootstrap state is refused; complete the real record without calling the expired bootstrap callback. |

B09–B13 cover the exact `recheck_inputs` **function used at under-lock call sites**;
they take **no actual or synthetic lock** and are not proof of main's lock-order,
admission integration, later Guard timing or real run immutability. B03/B15
explicitly inject start state; B05/B07/B13 use actual elapsed monotonic time. An
overloaded environment that expires before the intended boundary fails the
control/HOLD rather than silently changing the intended case. All original input
FD closure assertions inspect the subject's tracked live descriptors.
The nonempty pending-OS-signal refusal branch is not exercised: the synthetic
latch cases send no OS signal. Source sampling of an empty pending set is not
physical signal-delivery, actual handler or real closeout-main evidence.

## Injections, cleanup and uncertainty

`SubjectOS` is a per-subject delegate, **not a process-global monkeypatch**.
Default calls use actual Linux OS functions. Real I/O, byte hashes, pin deltas,
completed state and named rejection reasons are checked together. Zero writes,
the fsync EIO, synthetic CANCEL, pre-expired start and B11's mismatched seal input
are explicitly injected; they do not become hardware/process failure evidence.
No code sends signals, enumerates/kills workers or touches the clipboard/vaults.

The independent fixture arena captures a duplicate original FD immediately after
exclusive file creation, before any fixture write, plus an original read FD.
Child directories are exclusively created0700 and immediately bound to their
own retained descriptors. Any allocation/capture ambiguity is HOLD. Planned
rename, replacement, mode and hard-link mutations update only known original
records and allowed aliases after successful operations. The distinct new
replacement has its **own** original record; it never replaces the old record.
Full expected file pins are refreshed following known writes; the stable original
dev/ino/uid and only the one explicitly allowed mode transition must still agree.

The caller's `finally` first closes source-created journal/directory/input FDs.
Unexpected live source descriptors or close uncertainty remain HOLD even if a
known descriptor can then be closed. Cleanup never retries a possibly completed
close. It next validates original scratch/ancestors, exact allowed member set,
all retained original file/name pins, tracked hardlinks and empty child directories
**before the first cleanup unlink**. It removes only each validated known name
through the borrowed original scratch FD, checks original link transitions,
fsyncs that original parent and verifies emptiness; it independently closes all
retained fixture/ancestor descriptors. A partial or uncertain operation stops
destructive cleanup, closes known descriptors best-effort and returns HOLD.
No unknown name, replacement, subtree, shared cache, source/test/report or old
runtime is adopted, traversed or deleted. Cleanup is not replayed.

The cleanup field `removed_names` counts only names whose destructive call,
original/link proof and parent fsync all completed. It is **not a raw count of all
successful unlink/rmdir calls**: an intervening failure can leave a removed name
unrepresented in that number. Such partial cleanup remains HOLD; this field is
not a recovery inventory. Per-fixture original pins are retained in the callable's
in-memory original-FD registry, not a separately emitted external allocation
census. Actual results plus later empty scratch are source-bound evidence, not
independent reconstruction of every allocation/name. Lost original witnesses
cannot be replaced by a pathname-only cleanup or a new automatic attempt.

This behavior applies on ordinary success, assertion failure, cancellation and
deadline as long as Python reaches the installed finally. Cleanup itself does
not consult the cancelled subject or outer latch; its finite known-object work
must be allowed to settle. A last outer poll after cleanup can still mark HOLD.
Uncatchable termination, kernel/filesystem hangs, descriptor close uncertainty,
power loss and host failure are **not guaranteed recoverable**. Original/terminal
uncertainty is consumed HOLD/no automatic retry, not permission to clean by path.
The separately admitted outer must bind its own timeout/observation/settlement
and original empty-scratch removal; this callable cannot certify those actions.

## Bounds and actual-result mapping

- One synchronous Python callable; no threads, subprocesses, server or daemon.
- Total control-body budget60s, polled before source I/O/ticks and around cases;
  this is a cooperative bound, not preemption of blocking OS calls or `emit`.
  The separate outer must allow a small explicit cleanup/settlement margin.
- Each regular fixture and selected source journal is capped4096 bytes. Fixed
  cases use at most two simultaneously live original regular files, or two
  empty child directories; no recursion into fixture subtrees. A conservative
  per-case ceiling is four allowed names and24 harness-owned FDs, excluding the
  launcher's borrowed FD/evidence/coordination descriptors.
- A successful complete run emits at most58 dictionaries: one suite begin,
  28 case begins/results and one terminal. Require the outer's durable sink cap
  of256KiB. Retain compact JSONL/result, exact hashes and narrow external
  observations, not copied applications/caches/toolchains or binary archives.
- Root must enforce current launch floors12GiB free/25% available RAM and ongoing
  floors8GiB/20%, with a narrow memory bound in its separate launcher plan. Values
  observed during source authoring do not admit a future execution.

Each case result contains `case_body_started`, effect observations/rejection and
I/O counters as relevant, plus fixture-cleanup status, removed-name count and
close/cleanup failures. The terminal distinguishes `planned_cases`,
`attempted_cases`, `case_bodies_started`, `passed_cases` and `unstarted_cases`.
Only all28 passed case bodies **and** COMPLETE original-only fixture cleanup,
successfully retained evidence and the separately observed successful outer
settlement can support a reported full control pass. Descriptor/name counts,
journal records and assertions are not additional test cases.
The terminal contains counters, **not a case-results array**. The outer must
durably retain and independently reconcile each exact scheduled case-result
dictionary, its body-start flag, effect and cleanup outcome, in the declared
order. A terminal PASS without the complete28 individual results is insufficient.

The function returns, rather than exiting, with `status=PASS` or `HOLD`; uncaught
entry/evidence exceptions must also become outer HOLD/nonzero. The launcher must
check its still-monotone cancellation state and original witnesses at terminal
settlement, never convert missing/failed evidence into PASS, and never resume
the remaining cases under this consumed namespace. Independent actual-result
review is still required after any admitted run. There is no actual result yet.

This is cooperative, frozen-namespace evidence, **not** a hostile-UID sandbox,
cryptographic journal authentication, atomic-kernel filesystem proof or hardware
security evidence. In particular, arbitrary same-inode old-record rewriting
*during* the writer's own append is not claimed detectable merely by these pins.
Normal own-write mtime/ctime changes remain compatible; the completed-state pin
guards only the source's stated sampling boundaries.

## Preserved restrictions and current credit

PVU-007 STOP, PVU-011 NO-RETRY, PVA-029's actual FAIL/no automatic retry, G7/G8
CLOSED execution/recovery, Windows operational FAIL/cleanup HOLD and all candidate
1017001, protected-ref/version/identity/dependency/signing/store/publication
restrictions remain untouched. No authoritative skill repository is invented;
the generated skill mirror is not edited. The preserved handoff commit/tree is
not modified by this proposal, and no old macOS process/lock witness is reused.

The handoff baseline was **19/25 original confirmed**, **22/37 all confirmed**
and **2/12 original suspicions**. Root's independently registered PVA-038 makes
the current all-confirmed count **22/38**: no additional closure. Original19/25
and conclusive suspicions2/12 remain separate; this controls lane adds0 to every
closure count. Eight PVD design explanations/owner decisions stay separate.
Source authoring retains these two active permanent files and the four
root-authorized compact inert v1/v2 before-images; independent review files remain
their reviewer's separate evidence.
No build, test, import, compile, temporary scratch, cache, large artifact or owned
background process has been created by the author. No wrapper-stop obligation
arises from this source-only lane. Pending independent review and future actual
instance/launcher admission are not silently skipped or represented as success.
