# Linux database closeout v1 — independent source challenge

**REVISE. No helper execution, import, syntax check, cleanup or filled instance
acceptance is authorized.** This is an unexecuted source-proposal review, not
`ACCEPT-baseline_coverage.json` and not a postrun cleanup receipt.

Helper author: `/root/editor`. Source reviewer: `/root/baseline_coverage`.
Design contributors: `/root` and `/root/baseline_coverage`; disclosed rather than
described as independent rediscovery. Runner coauthors remain `/root/storage`
and `/root`; `/root/verification` owns its separate F01–F07 review. Root remains
the sole build/test/execution owner. The Windows cleanup HOLD and released slot
do not authorize this or another job.

## Exact scope and method

I read all 1,041 LF / 57,457 bytes of the frozen proposed helper, SHA-256
`a0001a0e5e9e399f7a9f27f627564cdfc99753b9143a35b9dd6bc0d79c7029c4`,
and all 423 LF / 25,769 bytes of its PLAN, SHA-256
`15b2fb04baeba8e849bf50f8c3ff6cfdb67f8c355b89e6e5706df9e1eda2a696`.
The inert copies `linux-closeout-v1.py.txt` and `linux-closeout-plan-v1.md`
match those exact bytes. The paired JSON report binds these and the earlier
disclosed design inputs. No code was imported, compiled or run.

Line numbers below refer to the frozen Python text. Reachability and negative
controls are source counterexamples, **not executed tests**. The companion
runner identity observed for interface review is
`346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279`;
its PLAN is
`74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f`.
Those are not final F acceptance or prospective agreement to changed bytes.

## Required corrections

### LC-C05-01 — prior closeout journal mutation is not checked before append

**REVISE C05.** The author raised this in self-review; I independently confirmed
the reachable counterexample and surrounding checks rather than claiming to
have originated it.

`Journal.event` (271–293) compares the current descriptor only to the original
dev/ino/UID/mode/link tuple, then compares the pathname to that *current*
descriptor. It does not compare the last completed full regular-file pin or
require the preappend length to equal `self.used`. A same-inode, same-size
rewrite of an earlier record can therefore change mtime/ctime and bytes without
failing these checks. The next sequential write has the expected final length,
so line 288 also passes. A later `terminal_commit` can be emitted despite the
already-durable history no longer matching this writer's last observation.

The prior exclusive create, parent/file fsync, size cap and original inode
checks do not detect this counterexample. Replacing the pathname or adding a
hard link is a different case and is already guarded; an external append can
cause the late length check to fail but is not safely rejected before writing.

The successor must preserve the last completed full pin and expected length,
check both *before* any next append, and commit the successor pin/sequence/used
only after successful write, fsync and descriptor/name/expected-length proof.
Any partial write, failed durability or detected drift must latch an incomplete
journal and stop further deletion/appends; never repair or replace the evidence.
Do not claim these metadata checks are cryptographic history authentication or
an atomic defence against hostile same-UID writers, timestamp-resolution effects
or mutations after the final point observation. The cooperative freeze and
external outcome/evidence review remain necessary.

Required separately admitted control effects, not executed here:

- A normal multi-event journal keeps literal order, byte length, pins and hashes;
  positive progress must not be disabled by using the original empty-file pin
  forever or by cancellation polling during a best-effort HOLD receipt.
- An equal-size prior-record rewrite with changed mutable pin fields is refused
  before another write; the old records are not silently normalized.
- Preappend truncation, unexpected appended bytes, pathname replacement and
  link-count drift are refused without adopting a new journal.
- Partial write / zero write / fsync failure and postwrite name or pin drift do
  not advance the completed state or permit a later successful event/deletion.

### LC-C06-01 — bootstrap omits the advertised deadline/cancellation polling

**REVISE C06.** Also raised by the author and independently confirmed.

`Directories.tick` is a no-op at line 172. Although the start clock and signal
latch are installed at 942–945, all admission input reads, durable launcher
evidence reads, directory binding and under-lock rehash (949–960) occur before
the real callback is installed at 967–969. Line 961 checks cancellation and
request expiry only after this work; it does not check the 900-second bound.

With a signal latched during an early multi-chunk input read, subsequent chunks
and other admission work continue. With slow but individually returning I/O,
the whole-helper deadline can pass before the next effective check. No deletion
is reached on the ordinary final refusal path, but the PLAN's whole-helper soft
bound and timely cancellation-between-operations claim are not implemented for
this phase. Per-file/aggregate caps and RLIMIT_AS do not fix elapsed-time polling.
The later destructive-commit cancellation fence does not cover this bootstrap.

Install a cheap monotonic-deadline / latched-and-pending-signal callback before
admission. Use it at bounded read/setup/binding/rehash and other potentially long
loop boundaries, including before consuming the closeout journal. Preserve the
later original-lock/resource/process/mount guard rather than treating bootstrap
observations as original runtime authority. Avoid recursive guard callbacks from
`Directories.open`, and keep a bounded best-effort terminal-HOLD journal write
possible after a deadline or cancellation has already been latched.

The existing soft-syscall, hard-kill, host-loss and outer-launcher limitations
must remain. This change cannot make a blocked filesystem syscall interruptible
or turn a failed/consumed attempt into retry permission.

Required separately admitted control effects, not executed here:

- A deadline already expired before admission and a signal already latched
  before the first input read refuse without journal creation or deletion.
- Cancellation/deadline changes during chunked reads, directory binding and
  under-lock rehash are observed at the next bounded callback, not only after
  all controls finish. Already-opened owned descriptors still settle.
- A prospective valid interval and small valid bootstrap remain reachable;
  no blanket callback recursion, failure to preserve HOLD evidence, process
  launch, signal, stop replay or altered fixed-path cleanup mode is introduced.

Meaningful inert controls require their own exact source/harness/namespace and
independent execution/retention admission. These expectations do not authorize
running the real fixed-path helper, changing its namespace, importing archived
helpers or treating a mocked assertion as completed Linux cleanup evidence.

## C01–C07 source dispositions and independent challenge

`ACCEPT` in this table applies only to the specified unexecuted source contract.
All seven actual-instance obligations remain **UNFILLED** until the original
run, launcher, TARGETS/EVIDENCE/REQUEST, exact independent reviews and current
coordination evidence exist and are separately challenged. Nothing here fills
the normative postrun acceptance file.

| Obligation | Source disposition | Challenge and qualification |
| --- | --- | --- |
| C01 scope/protection | ACCEPT | Literal 33 tops, fixed entry, original 35-allocation contract and source-disjoint prefixes prevent whole-runtime/checkout deletion. R, checkout, source/permanent tests/schemas, E, toolchains and existing `.git` remain outside deletion. Every nested `.git`, `reports` and `test-results` name and required ancestor is retained in place, even after XML copying. Unknown outside roots are not automatically allowed. Protected symlink/special/hard-link anomalies conservatively HOLD the entire preflight rather than becoming deletable. |
| C02 original/descriptor/device/mount authority | ACCEPT | Bootstrap directory observations must equal the complete independently sealed original set; under-lock recheck follows. Every top needs original adjacent allocation intent/original and original parent witness, not a current observation. Directory/regular devices derive separately from fresh originals, not historical 23/24. All-tree no-follow FD/pin/UID/type/link/device checks, retained ancestor chain checks and target-or-descendant mount refusal support bounded cooperative authority. Sampled races are not an atomic inode-conditional kernel sandbox. |
| C03 source/request/review/launcher/evidence binding | ACCEPT | Literal 15 inputs, exact hashes/pins, original runner acceptances, request, complete unique terminal and external completed launcher are conjunctive. Original command log hashes/lengths and all available XML copies/original pins reconcile; missing XML inventory needs a literal original unavailable-directory error. Fsynced E contents stay complete and unchanged except the sole new closeout journal. Role strings are not cryptographic authentication; actual independent filled-instance evidence review remains mandatory. Final runner/interface bytes remain separately pending. |
| C04 stop/settlement/coordination | ACCEPT | The original stop branch requires prelaunch obligation, exact wrapper/environment/budget authority, started complete error-free exit-0 stop and settled RESULT. The alternative requires affirmative complete-journal no-Gradle proof, not missing partial evidence. Same original nonblocking flock, fresh owner no-local/CI-overlap/frozen-producer attestations and the new helper's fresh conservative process/mount checks remain required. No process is killed/adopted and no stop is replayed. See the separate integration qualification below. |
| C05 bounded preflight/durable lifecycle | REVISE | All 33 full-tree snapshots and durable intents precede the first unlink; descriptor-relative leaf/dir checks, exact membership, fsync and real top rmdir distinguish REMOVED from retained/report-pruned outcomes. Post-run descendant snapshots are not creation records. LC-C05-01 nevertheless invalidates the proposed journal integrity claim and must be corrected before source acceptance. |
| C06 cancellation/failure/resources/consumed HOLD | REVISE | The 512-MiB address-space cap, forest/evidence/input caps, resource floors, masked final destructive commitment, monotone latch, no retry and partial-removal/not-all-durable HOLD semantics are appropriately scoped. Descriptor-close failure requires external nonzero/HOLD even after terminal0. LC-C06-01 leaves initial soft deadline/cancellation checks ineffective and must be corrected. |
| C07 source/reports/outcomes/no overclaim | ACCEPT | Tracked checkout bytes/lengths/executable semantics are checked before and after; original XML is retained and bound, protected report graphs are verified, and unknown/new schema outputs are never deletion targets. Source drift conservatively prevents cleanup rather than authorizing source repair. Logical and stat-block accounting is not measured net freed disk. Terminal0 is bounded closeout only and insufficient without completed outer exit0; no application/hardware PASS or issue closure follows. |

### C04 interface qualification from the separate runner reviewer

`/root/verification` independently checked the runner-to-closeout integration at
the exact companion identities above. Its collaboration receipt confirms that
the runner's `settled` starts false and is assigned only from `Processes.settle`:
three consecutive empty samples, direct-child polling, and `not self.unknown`.
An exception leaves false. The closer requires that exact accepted runner's
`RESULT.owned_settled == true`, complete journal terminal and completed launcher.
The required 35 original allocations and `source_bound(before)` exclude an
unverified partial checkout from the alternate no-Gradle path.

There is **no separate positive settlement journal row**. This is a source-bound
RESULT boolean, not retained raw three-snapshot/waitpid evidence, and is not
standalone fresh settlement authority. The closer's own new process samples and
root's coordination/producer freeze remain conjunctive gates before deletion.
The historical owned-birth set intentionally omits bare `launch_observed` PIDs
and fast-exited children lacking a birth observation; those do not authorize PID
adoption. Fresh samples still reject observed runtime/buildlike live births and
uncertainty. Neither review proves absence of all escaped/short-lived workers or
a global host census. Unrelated `/proc` churn may conservatively cause HOLD.

This is a bounded interface check, not another F01–F07 review by this reviewer,
nor an acceptance of future runner revisions or guessed original run evidence.

## Preserved limits and next handoff

The entire 1198-file actual-checkout contract is distinct from the historical
811-member raw manifest; both PowerShell checkout-EOL qualifications remain.
The 105-method/seven-class/five-file Linux selection is still unexecuted here.
No application cases, family closures, suspicion outcomes or semantic LF credit
were added. Denominators remain 19/25 original confirmed, 22/37 all confirmed,
2/12 original suspicions; eight PVD explanations/owner decisions stay separate.

Preserve PVU-007 STOP, PVU-011 NO RETRY, PVA-029's actual FAIL/no automatic retry,
G7/G8 CLOSED, Windows cleanup HOLD and all protected branch/tag, dependency,
version/identity, signing/store/publication and candidate/build 1017001 fences.
No old runner/helper was imported or executed, and no runtime/lock was adopted.

Author should provide a focused successor helper/PLAN with new exact hashes,
preserving v1 rejection and the meaningful control gaps. I will challenge the
successor bytes separately. Even eventual source acceptance is only prebuild
design/source acceptance; actual original-run and fresh postrun instance review,
source-bound controls, outer-launcher setup, root approval, execution and
independent outcome adoption remain distinct unfinished steps.

Only compact permanent reports and inert snapshots were written. This review
created no build/cache/runtime/archive, daemon, emulator, test server, cleanup
process or wrapper-stop obligation. Foreground data/hash/Git reads ended. Recent
resource observations were about 25.6 GiB worktree disk and 44.8 GiB RAM
available; these are point observations, not a reservation. No unrelated output,
source/report, shared cache, SDK/toolchain or process was removed.
