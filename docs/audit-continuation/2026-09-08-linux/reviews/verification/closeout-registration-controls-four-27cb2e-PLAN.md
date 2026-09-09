# Four registration controls — source proposal, not execution admission

Author `/root/verification`; owner/executor `/root`; proposed independent control
reviewer `/root/build_config` (author of the separate17/28 controls, **not** these
four or the closer). `/root/editor` authored both subjects and may challenge test
fidelity, but that is not an independent review of their own correction.

**SOURCE ONLY / NOT EXECUTED / NOT ADMITTED.** No syntax/AST parsing, compilation,
import, control invocation, namespace creation, process probe, syscall fault
injection or cleanup has occurred while authoring this source/PLAN. Root must
obtain independent source and actual-instance/launch/cleanup approval before the
single proposed invocation. A callable definition is not an execution permit.

## 1. Exact narrow purpose and inputs

Exercise grouped infrastructure finding **LCL-OWN-001**, not a product PVA family:
normal directory-FD registration and one deterministic second-map-store
`MemoryError` for each of closer `Directories.open` and NEW outer
`Originals.directory`. Verify actual acquired descriptor settlement and original
parent/registry preservation, not just the presence of three corrective lines.

| Subject | SHA-256 | Bytes / LF |
| --- | --- | --- |
| `scripts/audit/linux_database_closeout.py` | `8437c693d5a1becb8ed06ed549a5e837b8e4820c83a047c841fbcc0289f4b517` |59609/1100|
| `reviews/linux-closeout/LAUNCH_CLOSEOUT.py` | `50473240372643cc30dc54f12e4f3cf818d6ab68e0f4f27b62929cdae4684a5c` |48017/867|
| This directory's `CLOSEOUT_REGISTRATION_CONTROLS.py` | `0a012823b44c2c2bf2e7e81e185cecc324639d210529d1886ab6651d5efa0802` |27259/521|

Review-relative paths are under
`/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux`.
The narrow successor review is
`reviews/baseline-coverage/CLOSEOUT-REGISTRATION-SUCCESSOR-REVIEW.md`; it accepts
only the source correction pending regression and instance review. The full NEW
outer review remains separate. If its source changes, this callable rejects the
changed whole-file digest; even a harmless pin-only change requires an explicit
source/reviewer rebind, never replacement bytes accepted automatically.

The callable receives exact source **bytes**, checks full SHA-256/size/LF and
parses them only in a future admitted invocation. It selects these original AST
nodes without editing their bodies, predicates or constants:

- closer: `Hold`, `require`, `identity`, `absolute`, `Directories`;
- outer: the exact `LIMITS` assignment, `Hold`, `require`, `pin`, `Originals`.

Selected node order/uniqueness must match exactly. Only those definitions are
compiled into fresh non-`__main__` namespaces. Neither entire helper is imported
or executed. Only constructors, `open`/`directory` and one `close` per subject
instance are invoked. `read`, `bind`, `verify`, `check_time`, admission, main,
lock/journal/forest/deletion/process/stop paths are **not** exercised. Referenced
globals needed only by those uncalled methods are not supplied to turn this into
a general helper instance. The outer's original bootstrap deadline initialization
is retained, but its directory method does not enforce that deadline; the
callable's external work checks are a distinct control.

The private namespace's `os` facade delegates the actual directory
`open`/`fstat`/`stat`/`close` syscalls and records each acquisition and close. It
restricts paths/flags/FD ownership, never globally monkeypatches `os`, returns no
invented close or `fstat` success, and does not alter the source's registry logic.
Its duplicate-close rejection is a safety guard: a repeated call is recorded and
fails the control **without** issuing another syscall against an ambiguous
numeric FD. Such a guarded attempt is not reported as a second kernel close.

Old b167/0bed helper bodies, rejected outer before-images, the consumed validation
runner and all archived application/recovery helpers are **never executed**.
The current17 launcher-contract and28 journal/bootstrap controls remain different
files, namespaces, protocols and denominators; these four are not added to them.

## 2. Four exact cases and real effects

| Ordered ID | Required effect |
| --- | --- |
| `R01_closer_nominal_registration` |Ordinary dictionaries; five original parents then one child register; no local close; final `Directories.close()` attempts each of six actual FDs once; all return and immediate real `fstat` reports EBADF |
| `R02_closer_second_pin_store_memoryerror` |After the first FD-map store really succeeds, the child pin-map store throws the exact synthetic MemoryError once; the child is removed from FD authority before one local close; five parent entries/pins/FDs remain intact; final close settles only those five |
| `R03_outer_nominal_registration` |Same positive registration/real-FD checks for `Originals.directory`; one final close consumes its six FD-map entries |
| `R04_outer_second_pin_store_memoryerror` |Same specific second-store failure and once-only local close for outer; retained five parents stay authoritative until normal final close consumes them |

The fault mapping is a `dict` subclass installed **only as the child subject's
pin map after parent registration**. Before raising
`MemoryError('SYNTHETIC_SECOND_PIN_INSERTION')`, it requires the ordinary FD map
to contain the exact newly acquired child descriptor and the unchanged five
parents, and the pin map to contain only the original parent pins. It never
stores the new pin or clears/repairs the FD map. Unexpected keys, repeated
insertion or a different exception origin fail the control. This simulates one
store exception; it does not allocate large data or create host memory pressure.

Each case records original numeric descriptor lifetimes, source close origin,
close-call count versus actual syscall-attempt count, normal close return and
immediate kernel EBADF observation. Parent FD/pin snapshots, post-target and
post-close maps are retained. Before the final subject close, each original
parent FD is still live with the original pin. Borrowed scratch and separately
held fixture observer FDs stay live; their pins are rechecked after subject close.

**Different post-close contracts matter:** closer `Directories.close()` leaves
the closed numeric values in its map; outer `Originals.close()` pops that map.
Both retain their existing pin maps. Controls assert those actual source
contracts, do not require an invented common empty-map behavior, and never call
either subject's `close` twice or resume a failed registration.

No file/FD allocator or evidence callback runs between the fault target's local
close and final subject-close checks. There is **no deliberate numeric FD reuse,
dup/dup2 sentinel or unrelated descriptor experiment**. Ordinary kernel reuse
between fully settled cases is not a reuse test; records are scoped to each
acquisition lifetime. These cases do not demonstrate real OOM, first-map-store
failure, interrupted/failed `close`, actual reused-number harm, full main unwind
or whole-helper admission. A fault-control PASS means the expected failed
registration settled correctly, **not** that production MemoryError is success.

## 3. Fixed scratch and root-facing callable

Only prospective scratch:

`/root/projects/PassVault/audit-runtime-linux-closeout-registration-controls-01`

It is **not created by this source task**. A separately admitted root launcher
must reject occupancy, create it exclusively0700 through original parent
descriptors, retain its original descriptor/pin and record durable original
allocation before loading/calling the controls. No adoption/rename/retry of an
occupied or partially consumed namespace is authorized.

The future call is exactly:

```python
run_controls(
    scratch_fd=original_root_owned_scratch_fd,
    original_directories=original_five_directory_pin_map,
    closer_source=exact_8437_bytes,
    outer_source=exact_5047_bytes,
    emit=root_durable_bounded_event_emitter,
    cancelled=root_latched_cancellation_reader,
)
```

No CLI, configurable path, environment override, arbitrary case selection,
helper import or test runner is provided. `CONSUMED` becomes true before work
and forbids a second call in that loaded namespace; root also must prohibit
reloading it for a retry. The original pin map has exactly the paths `/`, `/root`,
`/root/projects`, `/root/projects/PassVault` and the scratch, each with integer
`dev,ino,uid,mode`. These are **original instance observations**, not values to
guess from this document or historical macOS identities.

The callable opens/retains four original ancestor observer descriptors, validates
all parent-name/FD pins and the borrowed scratch, and requires it initially
empty. It creates only four empty0700 child directories: `closer-normal`,
`closer-fault`, `outer-normal`, `outer-fault`. Each exclusive creation has its own
original pin and held observer FD before testing. No regular fixture file,
application storage, cache, build output, log or receipt is written inside the
scratch. Root's compact permanent evidence is outside cleanup scope.

## 4. Time, disk, RAM and failure discipline

- Callable work at most30s and a **common45s deadline**, leaving15s for ordinary
  cleanup; never reset a clock after a case failure. At most2000 work ticks.
  Fixed source parsing and directory loops are bounded. Checks before syscalls
  are soft/cooperative; they cannot terminate a blocked kernel call or emitter.
- Root must separately review its once-only foreground launcher/command with a
 60s outside wall bound including callable setup/settlement margin and installed
  cancellation/cleanup before any load or mkdir. No such launcher/command or
  actual instance is supplied/admitted by this PLAN. Self timers/traps must not
  claim survival of SIGKILL/controller/host loss; no blocking emergency output
  may precede an enforced exit. Signals target only that originally owned work.
- Four empty child directories plus scratch: proposed **64KiB logical and
  allocated footprint**, checked after every creation via actual `st_size` and
  `st_blocks*512`. No data file writes. The filesystem must support this tiny
  layout; a directory-allocation syscall can overshoot a monitoring threshold,
  so this is not a hard quota. A crossing aborts work and attempts only safe
  original cleanup, with the violation retained, never a silent limit increase.
- Frozen helper inputs total107,626 bytes. No large archive/cache, SDK, native
  library or dependency/tool download is used. Root's launcher should cap this
  metadata job at256MiB owned RSS and64KiB retained evidence; these are proposed
  admission ceilings, **not enforced by a heap flag or this callable**. An exact
  independently accepted implementation/qualification is required before use.
- Preserve launch host floors12GiB free/25% RAM available (plus the tiny growth
  reservation), running8GiB/20%, with root's fresh before/after observations and
  bounded external monitoring during an unexpectedly long invocation. This
  author did not conduct new resource/process probes or grant a host lease.
- At most six explicit subject directory-FD acquisitions per case:24 for four
  normal completions. Eight explicit observer FDs are retained by the callable.
  Thus32 explicit `os.open` lifetimes normally have32 corresponding actual
  once-close attempts. Bounded `scandir` context managers have their own
  iterator-internal lifetimes;32 is **not** all kernel allocations or a test
  count. The borrowed scratch and root emitter/reader/lock FDs belong to root.

Any unexpected case failure stops later cases. Later cases remain unstarted;
there is no automatic retry, permissive result repair, fallback profile or
generated extra case. A started case without a result is explicitly distinct
from an unstarted case. A failing or oversized emitter/payload is HOLD, never
silent truncation into a complete run. No application test count follows.

## 5. Cleanup even when a case fails — exact limits

Each subject gets one `close` call in the normal path or its `finally`, never
both. If it leaves any actually acquired, original and **never-close-attempted**
descriptor, the facade's fixed final settlement attempts only that descriptor
once and marks the case FAIL regardless of settlement success. This is own
single-call component cleanup, not retry of a prior `close`, generic recovery,
another helper invocation or authority to act on old runtime/FD/PID evidence.
Any already-attempted close, including an error or uncertain outcome, is never
retried. No process-name scan, process signal, FD replacement or adoption occurs.

Arena cleanup is installed by `run_controls`' `finally` before fixtures exist.
An ordinary test FAIL does **not** suppress independently safe cleanup of these
four original empty directories. It validates each created child's held FD/pin,
its original path under the borrowed original scratch and emptiness, then issues
one descriptor-relative `rmdir`. Successful return, same original FD with
`nlink==0`, name absence and parent fsync are needed for a proved removal. A
failed child validation/removal does not skip validation/removal of other safe
originals. Unknown entries, replacement, contents or incomplete original
registration are retained with HOLD; no recursion or guessed cleanup is allowed.

Each acquired observer FD gets one close attempt even after case/removal errors,
with return and immediate EBADF checks recorded separately. All actual close
attempts are final; no retry after EINTR/uncertain result. The source's synthetic
MemoryError does not establish cleanup under catastrophic real allocation
failure: failure while constructing a receipt/cleanup list, Python termination,
host loss or an interrupted allocation-to-registry boundary can leave incomplete
evidence or residuals. No universal OOM, crash-proof or hostile-UID guarantee is
claimed. Name/FD comparisons assume cooperative freeze and are point checks,
not atomic immunity to another same-UID namespace mutator.

The callable **never closes or removes the borrowed scratch root**. Root must
retain its original parent/scratch authority through the call and, after the
independently reconciled child/FD result, validate that exact root's identity and
emptiness, remove it once, close its own original FDs and verify foreground
settlement. A normal test FAIL can still permit that independently safe original
empty-root cleanup; a root/child/ownership/settlement uncertainty is HOLD, not
permission for generic recovery. Keep original pins/evidence for a separately
admitted residual disposition. G7/G8 stay CLOSED.

## 6. Durable protocol and actual acceptance

The root callback must write each event once, bounded and durably, returning only
after its reviewed completed-write/fsync/original-file checks. No failed event
may be replayed. Event bodies are at most8KiB; returned packet at most32KiB.
Root reserves at most64KiB total retained control evidence with its exact
allocation/write handling independently reviewed. The normal event sequence is:

1. `begin`, fixed four IDs and both subject hashes;
2. `case_start` R01; 3. `case_result` R01;
4. `case_start` R02; 5. `case_result` R02;
6. `case_start` R03; 7. `case_result` R03;
8. `case_start` R04; 9. `case_result` R04;
10. `cleanup`, actual fixture/observer outcome.

All carry the fixed marker and monotonic sequence. The returned receipt is a
separate completion object, **not an eleventh emitted event**. Root must durably
bind it to the same invocation and reconcile emitted/returned per-case data,
exact ordered IDs, starts, four results, zero unstarted/started-without-result,
FD lifetimes, registry snapshots, cleanup and external actual tool exit. A shell
exit0, compilation, assertions about mock counts, list of four names or a partial
transcript is not completion evidence.

The strongest callable status is
`FOUR_COMPONENT_CONTROLS_PASS_PENDING_ROOT_SETTLEMENT`; it requires four PASS
results, no failures/emitter failure, complete fixture cleanup and common
deadline compliance. It is not global slot release, scratch-root removal,
independent outcome adoption, whole-helper approval, database admission or a
product closure. Root and an independent reviewer must reconcile actual external
timing/settlement and the original root closeout before any final result claim.

Before execution, root additionally needs frozen callable/PLAN/helper/outer/
independent-review hashes, actual JDK-independent Python3.12 binary identity,
the exact `-I -B` non-optimized foreground launch source/command, original
source-reader/evidence/parent/scratch/coordination observations, fresh sole-slot
approval, resource bounds and interruption/cleanup disposition. Use umask077.
This is one metadata-only process, no subprocess/thread/network/build/test
server/emulator/daemon; wrapper `--stop` is **NOT_APPLICABLE_NO_WRAPPER_LAUNCH**,
not a reason to replay an old Gradle command.

## 7. Accounting and unchanged boundaries

Authoring adds zero executed controls, application cases, native calls, closures
or conclusive suspicions. Current denominators19/25 original closures,22/38 all
closures and2/12 original suspicions remain separate; eight PVD explanations/
owner choices remain separate. Even four future passing component cases are
**not** additions to the separate17/28 case sets or the105 database methods.

PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 recorded FAIL/no automatic retry,
Windows FAIL/cleanup HOLD, G7/G8 CLOSED and the old-runner restriction persist.
No source/dependency/version/identity/protected-ref/signing/store/publication or
occupied1017001 change is made by this task. Only these two permanent compact
source/PLAN files were authored. All foreground source/data readers exited;
there are no author-created temporary artifacts, caches, runtime directories or
background workers to remove, and no claim about other owners' obligations.
