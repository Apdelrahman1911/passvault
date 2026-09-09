# One NEW inert-control launcher — proposal, not execution permission

Author/sole executor: `/root`. Launcher independent reviewer: `/root/editor_review`.
Control author: `/root/verification`; control-source independent reviewer:
`/root/storage`. The exact new runner's authors remain storage/root, with
verification its independent source reviewer. These roles are not interchangeable.

No command in this plan has run. This is not the old nineteen-control runner,
G7/G8 recovery, PVA-029 retry, Windows retry, or application test admission.
PVU-007 STOP and PVU-011 NO RETRY remain completely outside scope.

## Purpose and exact invocation

Exercise the 22 cases in the newly reviewed `linux_runner_controls.py`, and only
the definitions it needs from new runner SHA-256
`346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279`.
Neither runner `main()` nor admission/clone/Gradle/real process/cleanup execution
is called. Stable-reader mutations really change small synthetic objects; child,
process, pidfd and signal controls remain expressly mocked. A successful control
batch is not 105 application passes or permission to run the database batch.

One prospective entry, with no arguments/overrides:

```
/usr/bin/python3 -I -B /root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/verification/RUN_CONTROLS.py
```

System interpreter resolves to `/usr/bin/python3.12`; its exact executable path,
SHA-256 and original full regular-file tuple are required in REQUEST's `python`
object and rechecked under lock and at exit (32MiB binary read cap). Normal installed
standard-library/loader trust remains disclosed, not complete interpreter-library
provenance. No project import path or bytecode output.
No subprocess, shell, JVM, compiler, native test, network, emulator or server is
launched by this helper. A fixed non-`__main__` module namespace loads the accepted
control and successor bytes only after same-image under-lock admission.

## Fresh coordination/bootstrap, before requesting actual acceptance

The root and reviewer first independently accept this bootstrap description.
Root then performs only the following metadata allocation, not controls/tests:

1. Retain no-follow directory descriptors from `/` through
   `/root/projects/PassVault` and the existing review directory. Record their
   original `dev,ino,uid,mode` tuples in a new durable exclusive
   `CONTROL-BOOTSTRAP.jsonl` under this review directory, before mutation.
2. Confirm `BASE/.audit-coordination-linux-20260908` absent. Exclusively mkdir
   mode0700 under the original BASE descriptor; immediately acquire and retain
   its original descriptor/tuple; fsync parent and record original. Partial
   creation is HOLD, never current-path adoption or automatic retry.
3. Exclusively create only its `build.lock`, mode0600, regular, single link,
   no-follow. Acquire fd-derived original full tuple, fsync lock and parent,
   durably record original and close descriptors. Do not write this lock later.
   The directory/lock stay as small shared coordination metadata needed for the
   upcoming database admission; they are not disposable build artifacts.
4. Scratch `BASE/audit-runtime-linux-runner-controls-01` remains absent. No runtime
   or case directory is precreated/adopted. Windows job terminal state and its
   continuing cleanup HOLD are independently reconciled already; root refreshes
   CI/owned-process observations and reserves the sole audit execution slot before
   launch. Other agents must not run tests/builds; advisory flock is cooperative,
   not proof of whole-host exclusivity.

If the coordination path already exists, stop this bootstrap and reconcile its
earlier original creation evidence. Do not silently replace/adopt it. Any
unexpected state is a separate independently reviewed decision.

## Filled request and actual author-specific acceptances

Root writes `CONTROL-REQUEST.json` exclusively after bootstrap and source freeze.
It contains:

- `format=passvault-linux-controls-request-v1`, `author=/root`,
  `run_id=linux-runner-controls-01`; fixed `scratch` and `lock` as in source.
- `no_other_audit_local_or_ci_job=true`, based on fresh root observation/coordination,
  not future activity prevention. `created_unix_seconds`/`expires_unix_seconds`
  span no more than 3600 seconds and include the launch.
- `bindings`: exact literal SHA-256 of source `INPUTS` keys: `launcher`,
  `launch_plan`, `controls`, `design`, `runner`, `runner_plan`, `controls_review`.
  Last is the actual storage-authored final accepting JSON, not a root-created vote.
- `input_pins`: absolute INPUTS paths to original regular-file tuples
  `dev,ino,uid,mode,nlink,size,mtime_ns,ctime_ns`.
- `directory_pins`: exactly all original directories from `/` through every input,
  request/review, BASE and lock parent. No symlink ancestors, missing original or
  invented file/directory same-device equality. Directory tuples omit mutable
  size/mtime/link count.
- `lock_pin`: original full regular-file tuple from bootstrap, not a fresh adoption.
- `python`: `{path,sha256,identity}` for the fixed resolved executable above;
  its original `/usr/bin` ancestor chain belongs in `directory_pins` too.

`CONTROL-ACCEPT-root.json` and `CONTROL-ACCEPT-editor_review.json` are written by
their actual approvers, after reviewing the completed request and freeze:

- `format=passvault-linux-controls-acceptance-v1`, correct `reviewer` (`/root` or
  `/root/editor_review`), `launcher_author=/root`;
- `purpose=ONE_NEW_EXACT_SOURCE_INERT_CONTROL_EXECUTION`, `disposition=ACCEPT`;
- `request_sha256` and exactly identical seven `bindings`.

Root approval is author approval, not an independent review. Reviewer acceptance
does not exist until the reviewer supplies it. The launcher stable-reads the same
bytes it parses/hashes and captures REQUEST/approval identities before opening the
original lock; it repeats all reads under that lock and requires exact equality.
The complete original directory set, lock and freshness are checked before any
scratch allocation. One exclusive `CONTROL-events.jsonl` consumes the admission;
it is never overwritten/reused even if no case starts. No failed import/run retry.

## Bounds, evidence and cleanup installed before load

Before reading/loading control code the launcher installs monotone INT/TERM/HUP
latches and a 240-second alarm. The first alarm latches failure, raises a timeout
and arms 60 seconds of cleanup grace. A second alarm exits124 without deletion or
any signal to another process: incomplete/missing terminal state is HOLD. Python
signal delivery/syscalls, SIGKILL, host loss and interpreter crash can prevent
timely cleanup; no hard kernel wall-time or hostile-UID guarantee is claimed.
This external-to-controls timer is independent of 180-second overall/15-second
per-case helper soft limits. Launcher address space is limited to512MiB and each
written file to1MiB. This is not whole-host RAM isolation.

Text inputs cap512KiB each (Python binary32MiB), journal768KiB, fixture members/bytes are bounded in the
reviewed helper. The journal durably records case evidence/failures/cleanup before
emit returns. A failed append/fsync/pin check is sticky and cannot become success.
Disk/RAM launch floors12GiB/25%, running8GiB/20% are checked before allocation,
at journal/event boundaries at a5-second sampling target, and finally. Synchronous
operations can delay sampling. Low resources latch cancellation, preserve failure
and still allow known-original bounded cleanup.

Tool wrapper captures launcher stdout/stderr, exit and elapsed time with a small
output budget (launcher emits only bounded failure lines, not control outputs).
Root retains those observations in exclusive `CONTROL-LAUNCH-RECEIPT.json` after
actual interpreter exit. No claim of completed outer exit comes solely from an
in-process terminal event. The receipt binds journal/admission/source hashes, exact
argv, observed exit, elapsed time, bounded stdout/stderr, `/proc` own-PID/starttime
settlement and fresh resource observations. Root does not inspect unrelated
command lines/environment, kill names/groups, or call Gradle `--stop` (N/A: no wrapper).

Scratch creation has prior durable intent, retained original parent/target FDs and
pins, mode0700, verified emptiness and a durable original record before cleanup
authority is enabled. Earlier partial allocation stays HOLD. The reviewed control helper removes only
its registered synthetic members by retained descriptors; uncertain partial state
stops further cases with HOLD. Root's launcher removes the scratch root **only**
when those original fd/path/parent identities still match and the root is empty.
It records intent, revalidates the original lock and BASE/scratch fd/path/emptiness
after that potentially slow journal I/O, performs one original-parent `rmdir`, fsyncs parent, checks
absence and records outcome. No recursive delete, source/cache/report deletion,
name-based kill, current-inode adoption or automatic retry. Every owned fd closes
on normal/failure paths; single-process exit supplies final OS descriptor closure.
If namespace/IO/receipt ambiguity prevents cleanup, retain the tiny partial tree
and evidence; do not normalize it or convert eventual absence into success.

Terminal commit blocks observed INT/TERM/HUP/ALRM, samples pending cancellation and
requires the exact22 ordered names, PASS outcomes, false descriptor-HOLD and true
per-case/total soft-bound markers, consistent22/22/22 report counters and no not-run
names, original synthetic cleanup, successful empty
root removal, final unchanged inputs, resource floors and durable evidence. A
signal after that explicitly defined sample is outside its terminal promise.
Root independently reconciles literal cases and cleanup, not just helper status;
another agent must challenge the results before any database execution admission.

Current status: source proposal only, zero controls/application cases executed.
All product denominators and Windows FAIL/HOLD remain unchanged. No archived
helpers, real vaults/backups/clipboard/private data, dependency/identity/version/
Store changes, protected branch/tag movement, signing, publishing or build1017001.
