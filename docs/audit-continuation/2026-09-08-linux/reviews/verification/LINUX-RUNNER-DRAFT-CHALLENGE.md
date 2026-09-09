# Independent challenge of the new Linux runner draft

Reviewer: `/root/verification`; author: `/root/storage`.
**SOURCE REVIEW ONLY — CORRECTIONS/INPUTS PENDING; NO EXECUTION ACCEPTANCE.**

This continues the existing runner obligations, not a new application finding.
The reviewer did not edit, import, compile, execute or test the proposed runner.
Root remains the sole build/test, execution-admission and closeout owner.

## Inspected identities and scope

- Original draft snapshot: `linux-runner-draft-v0.py.txt`, SHA-256
  `3aa229662c34015cd2e00416d9a1a7dfc2996e1c5499a8d4e16507147b16405f`,
  45,735 bytes / 918 LF. All lines were read.
- Subsequent live draft, observed twice with SHA-256
  `9d5a5200ff52474cc6036be7c5cf899f90d0659842adc9b55fa3bea144ab6e3b`:
  complete diff from the snapshot and the affected directory/process components
  were read. This was author work in progress, **not a final freeze**.
- `reviews/linux-runner/PLAN.md` initial source proposal was read. REQUEST,
  SOURCE and the final normative acceptances were not yet available/reviewed.
- `docs/audit-handoff/current/RUNNER_READINESS.md`, the method/display mapping,
  build-config `REVIEW.md` and `JDK17-OPTIONS-REVIEW.md` informed the challenge.
  No old runner/helper was imported or executed.

Any later corrected source requires exact-byte re-review; this draft assessment
must not be substituted for that acceptance. Counterexamples below are source
schedules, **not observed runtime incidents or executed inert checks**.

## Concrete challenges sent to author and root

### D01 — Original stop authority was incompletely rebound

The original draft's cleanup path rehashes only `gradlew` before launching it
with `--stop`. The checked-in script executes
`gradle/wrapper/gradle-wrapper.jar`, which uses
`gradle/wrapper/gradle-wrapper.properties`. An unchanged script with changed
JAR/properties passes the narrow guard and invokes that changed authority
before the later source-after check can reject it.

The pre-build complete source check is a surrounding guard, but it does not
establish the later mutable wrapper inputs at cleanup launch. Requested:
rebind all three tracked wrapper authority files to original SOURCE bytes and
qualify the downloaded distribution authority. This is a cross-cutting
source/stop-authority obligation, not a new numbered PVA family. An early
message loosely called it F05; F05 actually concerns process observations.

Author acknowledged the gap and proposed correction; not yet reverified here.

### D02 — Acquired descriptor can escape the failure cleanup

`Directories.new_file` opens an exclusive file, then calls `fsync(parent)`
before returning its fd. A parent-fsync exception leaves that acquired fd
unregistered and inaccessible to the caller's normal `finally`. Similar
acquisition-order scrutiny applies to command-log/selector setup. Interpreter
exit ultimately closes descriptors; that is not the claimed explicit per-task
settlement or successful evidence durability.

Author acknowledged the new-file edge. The later live draft separately closes
an acquired pidfd when its immediate identity check fails; that change is
source credit, not an executed failure-path result.

### D03 — Zombie special case causes ordinary command completion to HOLD

In live draft `9d5a5200…`, `process_row` represents a zombie's absent cwd as
the empty string, making `inside=False`.

1. A fast already-zombie unreaped direct Popen child reaches `direct -> admit`,
   whose ownership predicate requires `inside`. This raises `Refused`; the
   `direct` handler only handles `FileNotFoundError` via poll/exit evidence.
2. A previously admitted owned process becoming a zombie reaches `scan`'s
   unconditional owned-cwd guard and adds permanent
   `owned_identity_or_cwd_churn`, although it is correctly excluded from the
   live list afterward. The command loop calls `monitor/scan` **before**
   `child.poll`, so normal successful completion can reach this schedule.

Requested: treat a positively birth-bound zombie as non-live/non-signal without
inventing cwd churn; positively observe an unreaped direct child's exit before
using the narrow already-exited exception. No unrelated zombie gains ownership
or signal authority.

### D04 — Numeric after-only set is not a complete final birth-bound sample

The second process pass enumerates only `after_pid_integers - before`.
An existing numeric PID whose birth/relevance/cwd changes after its first row
read is not re-read from the final set. In particular, PID reuse can leave the
old irrelevant row in the decision while the new relevant birth is present in
the final enumeration. The per-row double read guards only its own earlier
read interval.

Requested: account for the complete final birth-bound rows and conservatively
classify genuine birth/cwd churn, not only numerically new PIDs. Retain the
explicit point-sampling limit: this cannot prove no births after the defined
last sample or provide a hostile-host/global-idle lease. Unknown processes do
not acquire signal authority.

### D05 — SQLite extraction claim exceeds the then-available source proof

The initial PLAN describes generic `java.io.tmpdir` confinement as the relevant
reviewed guarantee. Build-config confirmed directly that the earlier SQLite
source capture did not include the JVM loader: it contained common expect and
native no-op code. Thus SQLite JVM extraction remains an M03 gap, not a proven
effect of the supplementary `org.sqlite.tmpdir` option.

Root requested a new bounded official SQLite JVM source read and Gradle
toolchain-property review from build-config. Bind the resulting exact packet
and its limits before acceptance; do not silently transfer JNA/resource-loader
proof to SQLite. Native binaries/source equivalence and actual forked-worker
options remain distinct evidence levels.

### D06 — Existing inert-check prerequisite needs an explicit disposition

RUNNER_READINESS F04 prerequisite 3 requires separately admitted exact-source
inert checks of stable-reader mutation schedules before future use. The new
PLAN instead treats import/compile/inert controls as optional and unadmitted.
This reviewer asked root for a clear disposition/new bounded admission rather
than silently dropping the unfinished prerequisite. No such check is claimed
or executed in this report; historical inert specifications remain inert.

## Narrow source credit; remaining acceptance prerequisites

- **F01:** Sticky command/error/cancellation accounting, pre-fork original stop
  obligation, explicit masked launch/terminal boundaries, one stop attempt and
  distinct semantic disposition are meaningful improvements. Need exact final
  source review, wrapper-authority correction and honest residual handling.
- **F02:** Presealed empty evidence namespace, externally original directory/
  lock/input tuples, same-capture parse/hash and whole under-lock comparison
  address earlier bootstrap design defects in principle. Actual final
  REQUEST/SOURCE/pins/lock and launcher receipt still require review.
- **F03:** No recursive deletion is implemented. Retain-all is the documented
  permitted alternative, **not completed cleanup**. Root must have a prompt,
  separately independently reviewed closeout path; no G7/G8 reopening/adoption.
- **F04:** No-follow single-link bounded stable reads and original ancestor
  verification merit source credit. They are not atomic whole-tree or
  hostile-same-UID protection; D01/D02/D06 remain material.
- **F05:** Birth/live-parent ownership, pidfds/narrow unreaped-child fallback,
  fresh pre-signal checks and unknown/HOLD policy are appropriate intent.
  D03/D04 must be addressed; no actual process settlement was observed here.
- **F06:** Actual reviewer-authored normative documents, exact purpose/target/
  source/command/environment hashes and distinct non-author reviewers are
  checked, rather than accepting root labels for arbitrary historical reports.
  No final reviewer attestation has yet been authored by this reviewer.
- **F07:** Exact immutable seven-class/105-method display mapping, mandatory
  regular nonempty XML set, exact counters/names and failure/error/skip checks
  are source credit. Mechanical mapping never becomes independent semantic
  PASS. Current executions remain **zero**.

## Inspection and resource record

Two bounded lookups guessed nonexistent `reviews/linux-runner` (before author
created it) and `docs/audit-handoff/RUNNER_READINESS.md`; discovery identified
the actual `current/` file. A later lookup guessed `reviews/build_config`
instead of `reviews/build-config`, and a configuration grep included absent
`build-logic`. Those inspection errors are not hidden test results or retries.
One combined source display was truncated; no claim of full root Gradle-file
review relies on the missing display. Focused earlier configuration review and
the build-config packet remain separately qualified.

A subsequent read-only resource observation recorded worktree available
29,177,164 KiB, `/tmp` available18,837,872 KiB, and MemAvailable44,063,680 KiB
of65,855,360 KiB. These are point observations, not reservations or cleanup by
this reviewer. No build/test/compiler, daemon, emulator or persistent worker
was launched. No runtime/cache/generated build output was created or deleted;
wrapper `--stop` is not applicable to this source-review task.

All PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 FAIL/no automatic retry and G7/G8
CLOSED restrictions remain. Real vaults/backups/clipboard, private signing,
publication/candidate1017001, PVD owner choices and all coverage denominators
are unchanged.
