# Isolated-batch01: independent inner/init source review

Reviewer `/root/verification`; 2026-09-09. **Corrected inner/init source component
supported, conditional on independently accepted outer code and an exact fresh
instance. This is NOT execution admission or a test result.** No target import,
syntax/AST check, build, test, namespace/process probe or old-runtime access was
performed. Source text and retained evidence were read as data only.

## Frozen identities

Paths below are relative to `/root/projects/PassVault/passvault-linux`.

| Source | SHA-256 | Bytes / LF |
| --- | --- | --- |
| `scripts/audit/linux_isolated_batch.py`, corrected | `15cbad1ce8af84d6fb1f56a92ba70494148ffc0a19e3ca9af7eb16d2dfa0ada5` | 37171 / 669 |
| `scripts/audit/isolated_batch_tests.init.gradle` | `24a301c6cc8818eef5e61c8a10fe03a4b4f781bd4abac4ab4266bc6985583ab1` | 5430 / 101 |

Both hashes were independently read; the corrected inner and unchanged init
were completely source-read. The earlier unexecuted inner reviewed first was
`8703e1b8bb2774596aec6a17969830706e94fa9e2cb9dae016efa0546dc62e97`,
35214B/637LF. The final combined770LF exceeds the author's soft650LF target;
this report does not claim that target was met.

The full source identity is **C4**, commit
`da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`, all1572 manifest members.
It is not the current publication tree or a dirty-copy representation.
Independently read input SHA-256 values:

- `reviews/current-cycle/SOURCE.json`:
  `2e8b82c91215bbfee9cf4a2a3f09bad386fc8515d7fd20263fbbfa5fe1217003`.
- `reviews/build-config/current-cycle01/CLASSES.tsv`:
  `95e6ed4f8e5cbb92e4e6840a08e16613b14674555f7f99cd24e34b3ddcd18bda`.
- `reviews/build-config/current-cycle01/METHODS.tsv`:
  `10cb11cd6392e43ab7ec3e94e2f348ccd2caf6cd2127830fe9e1287ea10210be`.

Those three paths are relative to `docs/audit-continuation/2026-09-08-linux/`.
The inner checks raw Git blob SHA-1/mode before and after, preserves both
checkout-EOL qualifications, and forbids `.git`. Raw blobs are not normalized
checkout bytes. `/root/build_config` supplied message-only Git-free source
feasibility, **not** a report/admission or executed task-graph proof. Matching
the12 selected source files in a later tree does not make its full archive C4.

## Reachability, guards and counterexamples reviewed

- Fixed fresh runtime is
  `/root/projects/PassVault/audit-runtime-linux-isolated-batch01`; evidence is
  `runs/linux-isolated-batch01/` under the continuation directory. The inner
  neither materializes/deletes source nor signals a host PID. Original private
  directory identities must match outer intake. Stable no-follow regular-file
  reads require owner/single-link/bounds and before/open/after identity; file
  device need not equal containing-directory device.
- PID1/PPID0, distinct original PID/mount namespace identities, and proc self
  mapping precede any process-membership scan or build. Mountinfo partitions
  literal ` - ` and keeps exactly three positional suffix slots: only SOURCE
  may be empty. Nonempty type/superoptions, nonpropagation tags, proc device/type
  and security flags remain required. The independently retained grammar report
  `reviews/native-independent/NAMESPACE01-MOUNTINFO-CONTRACT-REVIEW.md` was read
  and its hash verified as
  `541afd152ebaa7fa6cc5af0a0d2455c1c54f34c90effe8b9f139fec2024af7c6`.
  This does not identify the unretained namespace01 failing row. Absence of
  propagation relationships is not an exclusive MS_PRIVATE-class claim.
- A PID namespace does not contain work delegated to outside daemons by IPC.
  Here the fixed clean client environment makes HOME, Java `user.home`,
  temporary/JNA/SQLite locations, Gradle registry/cache, Kotlin/Native data,
  Android user data and XDG locations private. Gradle is non-daemon/one-worker,
  Kotlin compiler execution is in-process; shared daemon registries are not
  supplied. Test JVM startup properties select private per-worker home/temp/JNA
  paths after removing inherited JVM-option variables. The loader-IO worker
  alone gets its original zero-file temporary-path blocker. JDK17 is fixed;
  dependency verification remains strict, SDK/JDK auto-download is disabled,
  and no release/package/whole-suite command is selected.
- The ordinary invocation declares163 regressions across13 classes. Four cold,
  offline Test invocations separately declare one fixture producer and three
  regressions. The unchanged317-character synthetic producer value is extracted
  from its exact XML record, retained once, and supplied unchanged to all three
  consumers. The init forces each cold Test task, not all compilation tasks.
  Total declaration is **166 regressions +1 producer**, not167 regressions.
- XML preservation is bounded and precedes success mapping. Exact classes,
  literal methods, predeclared `[desktop]` decoration, multiplicity, suite
  counters and absence of failure/error/skip nodes are checked. Expected
  captures are13 ordinary plus four cold. Missing XML after a compile failure
  can be recorded as preserved absence, but mapping is false; compilation,
  task counts, stdout and producer data alone never establish test success.
- Every attempted phase marks original-wrapper stop required before launch.
  Once-only stop still checks the original wrapper's three files and JDK even
  after other input drift/cancellation. Direct children are pidfd-bound before
  polling; no numeric-PID signal fallback is used. Incomplete direct command,
  log/evidence uncertainty, failed stop, nonempty namespace or cancellation
  blocks cleanup-safe. Namespace reaping/settlement has120s aggregate allowance.
  Complete failed tests can preserve evidence and allow cleanup without passing
  validation. Finalization/descriptor uncertainty forces exit70 even if an
  earlier result JSON was already written; outer must refuse cleanup then.

## Independently requested corrections and remaining qualifications

1. The original loop sampled `now` before potentially expensive resource work.
   The corrected loop recomputes it after resource/cancellation checks. The
   new600s stop budget includes a20s TERM/KILL tail, but these checks remain
   **cooperative under syscall/I/O delay**. A reviewed outer pidfd/wait envelope
   is still necessary; this is not empirical hard-deadline proof.
2. Live traversal of an actively changing Gradle directory can legitimately
   see ENOENT after enumeration. Treating every such sample as provenance
   failure would consume this one-shot attempt unnecessarily. The correction
   narrowly records vanished noncritical interior entries/scans as incomplete
   resource point samples. Root/all ORIGINAL_DIRS disappearance and every
   non-ENOENT error still fail; surviving nodes retain no-follow/type/UID checks.
   Examined-entry caps include skipped entries. No missing-file tolerance was
   added to exact final source/input/evidence/cleanup checks.

Resource sampling retains at most256 compact records, flags unfinished/vanished
samples, and explicitly disclaims ownership/cleanup proof. Limits remain30s per
traversal,230000 examined entries,200000 files,30000 directories,6GiB observed
logical data, launch/running free-byte floors12/8GiB, and MemAvailable fractions
25/20%. These are point samples, not continuous or exhaustive accounting; no
free-inode floor is implemented or claimed. Host conflict screening must be
reviewed separately and cannot inherit this runtime-only ENOENT policy.

## Not supplied by this component review

The complete outer, actual fresh request, device model, materialized raw-C4
archive, original directory/lock/input pins, genuine reviewer-owned approval,
root's external approval-hash freeze, host/CI coordination and cleanup admission
still require review. No actual acceptance JSON has been issued here. A claimed
inner `validation_mapping_ok` will still require independent actual-XML and
semantic review; its `independent_semantic_acceptance` is deliberately false.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029's recorded failure/no automatic retry,
G7/G8 CLOSED, old Linux01/02 HOLD/consumed states, consumed namespace01 FAIL/no
retry, and failed Windows/macOS restrictions remain unchanged. No old helper or
runner was imported/replayed. Closure denominators remain19/25 original and
22/38 all confirmed; original suspicions2/12 and eight PVD explanations remain
separate. This source review adds no executed test, closure, hardware or cleanup
credit. Only this permanent compact report was created; no owned workers,
caches, runtime allocations or temporary outputs were left by the reviewer.
