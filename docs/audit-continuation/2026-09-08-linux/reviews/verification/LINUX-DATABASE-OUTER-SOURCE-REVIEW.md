# Fixed database outer launcher — independent source/design review

Reviewer: `/root/verification`; 2026-09-09 UTC. Launcher author:
`/root/storage`; design contributor and sole executor: `/root`.
**ACCEPT_SOURCE_DESIGN_ONLY_WITH_EXPLICIT_LIMITS.** This is not the normative
`reviews/linux-runner/ACCEPT-verification.json`, a filled-instance approval,
execution, controls result, successful wrapper stop or database test result.

## Exact subjects and review scope

| Input | SHA-256 | Bytes / LF |
| --- | --- | ---: |
| `reviews/linux-runner/LAUNCH_DATABASE.py` | `ee46cfd974de0c7241763b251c6258a5b8a058cdaa4146fa029573c21e3812c1` | 37,477 / 715 |
| Corrected `reviews/linux-runner/LAUNCH-PLAN.md` | `c5bb383165e2933a3d39da67b532613ffa71007c70e9dc38037f41e363a87a29` | 11,441 / 200 |
| Preserved inner `scripts/audit/linux_database_validation.py` | `346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279` | 55,614 / 1,079 |
| Inner `reviews/linux-runner/PLAN.md` | `74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f` | 21,126 / 318 |

The complete715-line outer source and original PLAN were read (`6eb148`,
`5289fe`), then the inner admission/binding/stop/terminal contract and current
closeout `RUN/LAUNCHER.json` schema were compared (`1c24fc`, `507f28`). All
review operations used source/data text reads and exact evidence copies, not
imports, compilation, syntax probes, runner execution, old-helper replay or
process-control experiments.

The launcher bytes are additionally retained exactly at
`linux-database-outer-frozen-v1.py.txt`. No source implementation was modified
by this reviewer. Root's role in the inner runner remains coauthor approval,
not an independent vote: inner authors are `/root/storage` and `/root`; this
reviewer is not an author of either the inner or outer runner. This reviewer
authored the separate controls, which `/root/storage` independently reviewed.

The fixed inner source remains the preserved handoff commit
`9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, tree
`05014e9f635131d5db06701e4013b4b5a746465a`. SOURCE SHA-256
`3b34d8c862d33ccda9df0d77bb4507a2504c0612a236f36d6d89a60c8a7485ce`
and METHOD-INVENTORY SHA-256
`40ac2f07ea2c98d1a320e75bb59d7bbc6b3ec954efd94b349257103aaccd8979`
were independently reviewed previously, not re-executed here. The1198 file
binding and two EOL qualifications are distinct from the105 method inventory,
historical XML overlaps and any future actual case results.

## Admission, coordination and source identity

- The outer process has one absolute no-option entry and one literal child
  argv: Python3 `-I -B` running only the newly authored fixed inner script.
  It does not import the child, take a task override, invoke an old runner,
  select another namespace or launch any retry. Python3.12, no optimization,
  realpath and original interpreter bytes/metadata are required. The child
  receives only the four fixed PATH/LANG/LC_ALL/TZ environment keys; the inner
  runner remains responsible for the separate reviewed JDK17/private Gradle
  environment. No resource limit is silently inherited into Gradle by this
  supervisor.
- `admission()` captures REQUEST, both source/PLAN pairs, SOURCE, METHODS and
  the actual root/reviewer approvals, then the interpreter. Owned bounded
  no-follow regular-file reads require single links, non-group/world-writable
  modes and matching complete pre/open/post tuples and original ancestor FDs.
  Exact images are checked under the original nonblocking lock probe and after
  direct-child exit; the outer launcher verifies its needed directory subset,
  not all1198 source members. The inner admission performs that full contract.
- The existing seven request/eight acceptance bindings, four inner immutable
  input pins and F01–F07 acceptance fields are unchanged. Additive
  `outer_launcher` request/approval objects bind this exact helper/PLAN, whole
  request, entry/child argv, environment, limits, four evidence paths, source
  freeze and actual roles. The inner script still independently rejects wrong
  task/environment/limit/toolchain/acknowledgement values. Outer validation is
  not represented as duplicating every inner check.
- The original lock descriptor/name is checked before and after flock. The
  parent intentionally releases its probe **before** Popen so its child may
  acquire that same lock. Holding it across launch would deadlock/refuse the
  child's nonblocking admission; claiming the probe is a continuous lease would
  instead overstate exclusivity. Actual root coordination, no-other-local/CI-job
  attestation and source/input freeze cover this cooperative handoff. A later
  lock-available observation is not worker settlement or host-wide exclusivity.
- R must be absent, E must be the original owned0700 empty directory, and all
  four outer outputs plus `RUN/LAUNCHER.json` must be absent. Exclusive first
  output creation consumes the instance. A partial output, drift or ambiguity
  stays retained; there is no repair, namespace adoption or replay path.

JSON role strings and file hashes do not cryptographically authenticate an
agent. Actual collaboration/author provenance and independent filled-instance
review remain required; so do the separate actual permissions and fresh pins.

## Original child authority and cleanup admission

- Latched INT/TERM/HUP handlers are installed before admission and fork; managed
  signals must initially be unblocked and SIGCHLD must be default. The managed
  mask covers durable launch intent/Popen commitment and is restored in child
  preexec and parent. Exactly one Popen exists. Ambiguous launch/preexec failure
  is HOLD, not authority to launch again or infer an unrecorded child identity.
- `direct_row` reads only this parent's PID or its original unreaped direct
  Popen PID, twice, checking start ticks/ppid/UID consistency. There is no process
  enumeration, unrelated argv/environment inspection or group/name/descendant
  signal authority. Missing initial birth is never replaced by a later current
  PID. The initial child must have this parent and UID.
- Before a signal, Popen poll checks wait state and a fresh double row must
  match the original PID/start/ppid/UID and this still-live parent. Same-birth
  zombies are not signaled. Under the single-threaded/default-SIGCHLD premise,
  the still-unreaped original direct PID cannot be reused between its fresh
  check and Popen's send-signal wait recheck. This source argument is not an
  executed pidfd, waitpid-race or hostile-host proof.
- Cancellation, resource/output/persistence/collection errors and the outer
  timeout latch failure. At most one TERM attempt is followed by900s grace,
  at most one KILL attempt, then60s final wait. Attempt flags are set even when
  authority is refused; later sampling is not an adoption or signal retry.
  Exceptional collection cleanup uses the same original child and elapsed
  grace, not another collector/launch. Failure to settle remains explicit HOLD.
- The outer supervisor never runs the wrapper, retries/discharges its child's
  original stop obligation, signals descendants, removes runtime members or
  cleans caches. Inner wrapper stop/worker evidence and separately admitted
  generated-output closeout remain conjunctive duties. Killing the supervisor
  or observing its exit is not proof those duties succeeded.

Potential fast exit is distinguished from disappearance: only observed Popen
poll/wait supplies the direct exit code; a missing original birth still fails
the outer completion predicate. No `/proc` absence or current numeric PID is
substituted for an actual collected exit.

## Evidence, descriptor and resource behavior

Four fixed permanent evidence leaves under RUN are exclusively created with
owned0600 single-link originals. Constructor failures close the acquired local
FD; successful writers retain their original FD. Every append verifies the last
completed full file tuple/length before writing, fsyncs, then checks original
stable fields, path identity and expected new length. A failed writer never
resumes or adopts current metadata. Append failure does not become a successful
closeout merely because the process later exits.

Stdout/stderr each retain at most1MiB and are independently polled/drained.
Excess is counted/discarded and permanently fails collection; resource and
process append-built JSON each have the same1MiB cap. Truncated JSON stays
incomplete, not repaired. Original pipe closes are marked attempted before
close, avoiding a retry of an ambiguous old FD number. Normal finalization has
separate retained-writer/directory close attempts; blocking or hard interruption
can still prevent them. Observed close failure changes actual outer disposition.
No bounded-source reader, outer output or synthetic review file is a Gradle
cache or an application result.

Launch floors are12GiB disk and25% MemAvailable; ongoing floors8GiB/20%, with
5s target sampling, BASE/E disk observations and no reservation claim. Ordinary
inputs cap4MiB, interpreter32MiB. Bootstrap and postchecks are each120s; outer
soft time is6000s from supervision creation, TERM grace900s, KILL wait60s,
post-direct-exit pipe EOF30s. Synchronous IO/fork/preexec/signal delivery can
overrun these cooperative checks. There is no generic wall-clock/RSS guarantee.

Root must separately record an external whole-tool envelope covering at least
6000+900+60+30+120 =7110s plus setup/receipt margin and honest interruption
handling before any database launch. This source review supplies no external
timer or permission to kill unrelated work. SIGKILL, blocked syscalls and host
loss can still prevent settlement/evidence; such a result is HOLD, not a reason
to relax source, ownership, cleanup or closed-scope restrictions.

## Terminal and closeout integration — no cached PASS

The process JSON summary is expressly PRETERMINAL. Its `completed` predicate
requires the original child, observed normal runner0 or1, complete pipes and no
resource/source/evidence/cancellation/timeout errors. The parent blocks managed
signals and samples pending/latches before its terminal commitment. Signals
after that sample and host loss remain outside the promise.

For **this helper**, normally collected runner1 can yield outer0, because an
application/inner-run failure is not automatically a collection failure.
Conversely **outer1 means incomplete/HOLD**, not merely failed application
tests. A late process-file/lock/directory close can force actual outer1 after
the preterminal summary proposed0. Therefore root must reconcile actual tool
exit and any final outer stderr with the saved summary; a preterminal true/0
alone cannot fill a truthful `completed=true` receipt or establish cleanup.

Only root can subsequently author `RUN/LAUNCHER.json`, after observing actual
tool completion. The reviewed schema requires original eight bindings plus
literal actual acceptance hashes; original child argv; start/finish times
enclosing the entire inner journal; observed runner/outer exits; truthful
completion, no timeout/interruption and sole-slot attestations; nonempty honest
limitations; four ordered evidence paths/hashes/full identities. The helper
does not write this external receipt or pretend to observe its own process exit.

That receipt is necessary, not sufficient, for the independent closeout's
actual original-journal/terminal/stop/full-allocation/source/fresh-settlement
gates. A normal, completely collected failed database run can in principle be
cleaned without inventing test PASS, but partial source allocation, failed or
ambiguous original stop, missing journal/terminal, live/unknown processes or
incomplete launcher evidence retain HOLD. The accepted closeout design is not
a postrun instance acceptance or new recovery authority.

## L-JSON-01 — independently challenged prose precision, corrected

Original PLAN SHA-256
`4b569b5292838dccee5b0dcbbd0c14b78070746cba51087b5596b0e8ddcba0fc`
said duplicate/nonfinite JSON was refused. The source rejects duplicate keys
and named NaN/Infinity constants, but `parse_constant` does not intercept an
overflow exponent such as1e400 in unused metadata. This was a source-derived
documentation counterexample, not an executed parser test or a demonstrated
authorization bypass. Used normative fields/equality and actual-instance review
remain constraints; arbitrary unused numeric extras are not globally validated.

The author independently confirmed the finding and narrowed only PLAN prose,
retaining the source hash and authority. Current c5bb PLAN explicitly preserves
that limitation. A bounded stable text read and in-memory reversal of exactly
the prose replacement/history appendix (`4c4ac2`, exit0) recovered the original
observed4b56 hash, independently proving no other PLAN delta. Neither the parser
example nor launcher code was executed. No launcher source fix is claimed.

Preservation limitation: `6ee883` exclusively wrote the exact ee46 source
snapshot, then exited1 because the author had already changed PLAN before its
old-hash snapshot read. No old-PLAN destination was created and no old literal
snapshot is claimed. The old full read/hash receipt and derived delta binding
are retained distinctly from a physical snapshot; the failed operation was not
a build, validation execution or consumed-admission retry.

## Remaining operational prerequisites and counts

1. The original controls01 remains20 recorded PASS /2 FAIL and consumed; its
   independent result/cleanup adjudication is separate. Corrected controls v4
   dffa884b…/designb52d60fc… has independent **source-only** review
   `reviews/storage/LINUX-CONTROLS-SOURCE-REVIEW-02.json`, SHA-256
   `727a0884b82962425548c90ce01b9c9c501912be2617efa477662fc7c3dccd96`.
   Source review is not successful meaningful new22 execution.
2. Actual root REQUEST and both actual approvals, original pins, JDK/SDK/SQLite
   qualifications, external deadline/cleanup envelope and complete F01–F07
   review are still required. This report does not fill or replace them.
3. Immediately before launch, root must secure the sole audit-local/CI slot,
   coordinate team/source/producer quiescence, recheck resources and originals,
   and preserve all old failure/NO-RETRY/CLOSED evidence.
4. After launch, exact terminal/tool/XML/process/stop/source evidence needs
   independent adjudication and the fixed closer needs its separate actual
   postrun instance admission.105 source methods are not105 executed cases.

Review execution counts: zero launcher/runner imports, syntax probes, controls,
application cases, native tests or hardware tests. Qualified-closure delta0:
19/25 original confirmed,22/37 all confirmed,2/12 original suspicions remain
separate denominators, not readiness percentages.

At00:50:57Z: worktree free32,104,680KiB, /tmp free21,387,180KiB,
MemAvailable44,644,840KiB /65,855,360KiB, no swap. These are point observations;
ambient disk changes are not attributed to this review. This task created only
necessary permanent source/review evidence, no build outputs/caches/temp roots,
daemon, worker or emulator. All data-read/write FDs were closed. No wrapper was
invoked, so wrapper stop is NOT_APPLICABLE. Nothing unrelated, no shared cache,
SDK, toolchain, source, permanent test or report was deleted/stopped.

PVU-007 STOP; PVU-011 NO RETRY; PVA-029 failure/no automatic retry; G7/G8 CLOSED;
the Windows failure/cleanup HOLD; hardware gaps; eight PVD boundaries;
occupied1017001; protected refs, identities, dependencies, versions, signing,
store and publication restrictions all remain unchanged.
