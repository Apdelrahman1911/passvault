# Fixed Linux database outer launcher — source proposal only

Author: `/root/storage`. Design contributor and sole execution owner: `/root`.
Independent source/filled-instance reviewer: `/root/verification`.
Root's owner execution approval is not another independent vote.

**Not admitted or executed.** No import, syntax check, compilation, runner,
build, test, cleanup or process signal was performed in preparing this source.
Review the exact source and actual filled controls before one possible launch.
The consumed controls-01 result remains a separate failure: LC17 and LC22 failed
in the controls fixture before reaching their intended target. This launcher
does not remedy that failure or authorize a controls retry/database run.

## Fixed purpose, source and entry

Supervise exactly one original NEW Linux PVA-033/034/035 database runner. This
does not replay the old runner or any archived execution/recovery helper.
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED,
hardware gaps, product boundaries and publication restrictions remain intact.

| Name | Fixed absolute path |
| --- | --- |
| BASE | `/root/projects/PassVault` |
| W | BASE + `/passvault-linux` |
| RUN | W + `/docs/audit-continuation/2026-09-08-linux/reviews/linux-runner` |
| R | BASE + `/audit-runtime-linux-db-01` |
| E | W + `/docs/audit-continuation/2026-09-08-linux/runs/linux-database-01` |
| lock | BASE + `/.audit-coordination-linux-20260908/build.lock` |

Outer entry, with no options, path overrides, retry or dry-run mode:

```
/usr/bin/python3 -I -B /root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/linux-runner/LAUNCH_DATABASE.py
```

Only child argv:

```
['/usr/bin/python3', '-I', '-B', '/root/projects/PassVault/passvault-linux/scripts/audit/linux_database_validation.py']
```

The resolved interpreter must be `/usr/bin/python3.12`, Python3.12 without
optimization, matched to the original request's actual binary bytes and pin.
Child cwd is W. Its entire inherited environment is exactly PATH=`/usr/bin:/bin`,
LANG=`C.UTF-8`, LC_ALL=`C.UTF-8`, TZ=`UTC`; the child independently supplies its
reviewed JDK17/private build environment and original checked-in wrapper.
Parent applies no inherited address-space/file-size rlimits to Gradle.

Frozen runner SHA-256:
`346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279`.
Runner PLAN SHA-256:
`74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f`.
Method inventory SHA-256:
`40ac2f07ea2c98d1a320e75bb59d7bbc6b3ec954efd94b349257103aaccd8979`.
Build checkpoint commit `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`,
tree `05014e9f635131d5db06701e4013b4b5a746465a`.
1198 actual tracked checkout files, not the raw G12 subset; 14 Git-normalized
PowerShell checkout forms but only TWO raw-G12 EOL qualifications. Seven fixed
classes/105 exact methods are an inventory, not executed-test or closure counts.

## Additive actual-instance admission

No new generic REQUEST family. Preserve the runner's seven REQUEST/eight
acceptance bindings, four immutable input pins, F01–F07 obligations and inner
authors/roles unchanged. The whole actual REQUEST hash also binds the additive
outer fields. Literal dict equality in `admission()` is normative.

`REQUEST.outer_launcher` has format `passvault-linux-database-outer-v1`, author
`/root`, purpose `ONE_ORIGINAL_LINUX_DATABASE_OUTER_SUPERVISOR`, actual
`launcher_sha256` and `plan_sha256`, and `input_pins` of only this launcher and
this PLAN. It binds exact `argv`, `runner_argv`, four-key `child_environment`,
source `LIMITS`, ordered four `evidence_paths`, `slot_owner=/root`,
`no_other_audit_local_or_ci_job=true`, `source_and_inputs_frozen=true` and the
ordered `limits_acknowledged` list below.

Each actual `ACCEPT-root.json` / `ACCEPT-verification.json` adds `outer_launcher`
with the same purpose, `launcher_authors=[/root/storage]`,
`design_contributors=[/root]`, `disposition=ACCEPT`, actual launcher/plan/whole
request hashes, and identical ordered acknowledgements. Outer `review_role` is
respectively `OWNER_EXECUTION_APPROVAL` / `INDEPENDENT_SOURCE_REVIEW`.
Cooperative authorship/freeze is not authenticated merely by JSON role labels.
Root authors REQUEST and its acceptance; verification authors its acceptance.
The launcher author does not fill them or the later LAUNCHER.json receipt.

Ordered acknowledgements:

1. `ONE_ORIGINAL_DIRECT_CHILD_NO_DESCENDANT_AUTHORITY`
2. `RUNNER_OWNS_ORIGINAL_STOP_NO_PARENT_STOP_OR_RETRY`
3. `COOPERATIVE_SLOT_AND_SOURCE_FREEZE_NOT_HOST_SANDBOX`
4. `POINT_SAMPLED_RESOURCES_AND_SOFT_SYSCALL_DEADLINES`
5. `FORCED_INTERRUPTED_OR_UNSETTLED_EXIT_IS_HOLD`
6. `ROOT_TOOL_EXIT_AND_SEPARATE_CLOSEOUT_REQUIRED`

Input reads use stable no-follow, owned, non-writable-by-group/world, single-link
regular files with full metadata pins and byte caps. Duplicate JSON keys and
named NaN/Infinity constants are refused; this is not generic finite-number
validation (an overflow exponent in an unused extra field can decode as infinity).
Literal authoritative field equality and independent actual-instance review
remain required. Original input/source/REQUEST/actual acceptance/interpreter images are
captured, compared under the original lock probe, and revalidated after direct
child exit. Required original ancestor directories must match REQUEST pins.
The parent verifies only its required directory subset and source-manifest
identity/count; the runner owns the full1198-file proof and XML interpretation.

R must be absent, original E owned0700 and empty. All four outer output files
and RUN/LAUNCHER.json must be absent. Exclusive first output creation consumes
this attempt: retain partial evidence after refusal, never repair or re-adopt.
The original lock is briefly acquired for admission, then **released before
Popen** so the child can acquire its own lock. Root's sole execution slot and
source/input freeze coordinate this deliberate handoff. A later lock-available
probe after direct wait is not descendant settlement or hostile-host exclusivity.

## Supervision, cancellation and limits

Default SIGCHLD and initially unblocked managed signals are required. Latched
INT/TERM/HUP handlers are installed before admission/fork. Those signals are
blocked through durable launch intent/Popen commitment; the child's preexec
restores the original mask. Exactly one direct Popen is allowed. An ambiguous
fork/preexec failure is HOLD; no alternative launch or setup/launch retry.

Capture the original direct PID/start_ticks/ppid/uid with two proc-row reads.
Missing birth is never filled later from a current PID. Only this parent's and
its original unreaped direct child's proc rows are read; no process enumeration,
unrelated argv/environment or descendant/name/group authority. Popen poll/wait
provides actual direct exit; disappearance is not a wait result.

On cancellation, timeout, resource, pipe or persistence failure: at most one TERM
attempt against the same original unreaped direct child, 900s grace, then at most
one KILL attempt and60s final wait. Fresh birth/UID/ppid checks precede signaling;
zombies are not signaled. Unknown identity, kernel blocking or failed collection
can leave explicit HOLD. Exceptional cleanup continues only that original
child/grace. The parent never calls wrapper --stop, settles descendants, deletes
anything, repairs evidence or discharges/retries the child's original stop duty.

| Bound | Value |
| --- | --- |
| Bootstrap / postchecks | 120s each |
| Outer soft duration | 6000s from parent supervision creation |
| TERM grace / final KILL wait | 900s /60s |
| Pipe EOF after direct exit | 30s |
| Resource target sample interval | 5s |
| Each retained output | 1MiB |
| Each ordinary input / interpreter bytes | 4MiB /32MiB |
| Launch disk / MemAvailable fraction | 12GiB /25% |
| Running disk / MemAvailable fraction | 8GiB /20% |

Both stdout and stderr are independently drained without unbounded buffering.
Excess beyond each cap is counted/discarded, permanently latches failure and
starts owned direct-child cleanup. Resource observations cover BASE and E disk
availability plus MemTotal/MemAvailable; they are point samples, not reservations.
Synchronous syscalls, fork/preexec, host loss and uncatchable SIGKILL can exceed
polling bounds or prevent cleanup/evidence. Root must separately admit an
external tool deadline covering6000+900+60+30+120s plus setup/receipt margin and
honest interruption handling. Moving to CI/another OS supplies no retry authority.

## Retained evidence and actual external receipt

Exactly four permanent outer files, ordered under RUN (not inside child E):
`LAUNCHER-stdout.log`, `LAUNCHER-stderr.log`, `LAUNCHER-resources.json`,
`LAUNCHER-processes.json`. Resource/process files are append-built objects with
format, records and preterminal fields. Incomplete JSON remains HOLD; no repair.
Every append compares the last completed full pin/length, then advances state
only after write/fsync and original stable fields/path/expected-length checks.
Failed writers never resume. Original descriptor closes are attempted; ambiguous
fd numbers are not retried. No artifacts, caches, reports or temporary files are
deleted by this parent. Original stop/worker proof and separately reviewed
closeout remain necessary to discharge resource cleanup obligations.

The process summary is expressly PRETERMINAL. `completed=true` requires observed
normal runner0/1, complete pipes and no resource/source/evidence/cancellation
errors. An outer0 can mean collected runner1, never application PASS. Its
`outer_exit_intent` cannot prove this parent's actual tool exit. Late descriptor
close failure forces actual outer1; root must compare tool exit and final stderr,
not trust a preceding preterminal0. Terminal masked sampling does not exclude
signals or host loss after commitment.

Only after observing actual tool completion, root may author RUN/LAUNCHER.json
using the exact schema in `../linux-closeout/PLAN.md`, section RUN/LAUNCHER.json:
original eight bindings plus actual root/verification acceptance hashes; actual
start/finish times enclosing child journal; observed runner/outer exit; justified
completion/no-timeout/no-interruption/sole-slot assertions; honest nonempty
limitations; and the four ordered evidence path/hash/full-identity objects.
The parent never writes that receipt or claims to observe its own process exit.
Independent actual-instance review and the separately admitted fixed closeout
remain required. No application closure credit comes from preparing this plan.

## Preparation record

The first source-write apply_patch was rejected for a malformed patch terminator
before creating the file. A first PLAN attempt failed in the JavaScript tool
wrapper parser (unescaped template backticks), before file writing or hashing.
Neither was Python syntax testing or execution; both remain recorded. A resumed
read used an incorrect controls path under scripts/audit and returned missing-file
errors, then read the existing reviews/verification path without changing it.

Independent `/root/verification` source review identified the original PLAN's
overbroad nonfinite-JSON claim. PLAN SHA-256
`4b569b5292838dccee5b0dcbbd0c14b78070746cba51087b5596b0e8ddcba0fc`
was clarified above without changing helper bytes or execution authority. No
parser example was executed; this is a documentation precision correction.
