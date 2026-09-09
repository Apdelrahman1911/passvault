# Independent namespace-feasibility source review

Reviewer `/root/verification`; author `/root/storage`.
**ACCEPT_NARROW_SOURCE_PROPOSAL_ONLY — caller, instance and execution admission remain pending.**
No namespace/capability probe, payload import/parse/compile/execution, mount or workload was run by this reviewer.

## Exact inputs and direct inspection

Both adjacent author files under `reviews/storage/` were read completely as text in tool `f864e7` (exit 0):

| Input | SHA-256 | Bytes / LF |
| --- | --- | --- |
| `LINUX-NAMESPACE-FEASIBILITY-PLAN.md` | `bb1f71d1b3e20f13b619d7bb2346efc74df77e97256bf86fb6eea46d3f1f4763` | 8414 / 135 |
| `LINUX-NAMESPACE-FEASIBILITY-PROBE.py` | `96eb1d54c690fd19181a76a5abfeff874cde42ef2e87aa89260a6c29daea1fee` | 3717 / 78 |

Root authorized one independent public **data** read of
`https://raw.githubusercontent.com/util-linux/util-linux/v2.39.3/sys-utils/unshare.c`.
Tool `61c2b3` exited 0; SHA-256
`1ae76f987f4d2dbc1260eb7329f3b5f1d40f02f3fd6b5c31862a8ab6bcd4e1e8`, 32371B / 1134LF matched the author.
Read cap 65536B, connection/time bounds 10s/20s, no retry, no saved download or execution/import of the data.
Direct retained excerpts cover L925–966,974–1037,1049–1083,1124–1134, not every upstream line.

Selected exact numbered lines (noncontiguous; omitted lines are not represented):

```text
929: 	if (-1 == unshare(unshare_flags))
930: 		err(EXIT_FAILURE, _("unshare failed"));
948: #ifdef UL_HAVE_PIDFD
949: 		if (kill_child_signo != 0) {
951: 			fd_parent_pid = pidfd_open(getpid(), 0);
955: #endif
958: 		pid = fork();
984: 	if (pid) {
985: 		if (waitpid(pid, &status, 0) == -1)
989: 			return WEXITSTATUS(status);
1009: 	if (kill_child_signo != 0) {
1010: 		if (prctl(PR_SET_PDEATHSIG, kill_child_signo) < 0)
1012: #ifdef UL_HAVE_PIDFD
1018: 			int nfds = poll(pollfds, 1, 0);
1027: 			if (nfds)
1028: 				exit(EXIT_FAILURE);
1054: 	if ((unshare_flags & CLONE_NEWNS) && propagation)
1055: 		set_propagation(propagation);
1079: 		if (mount("proc", procmnt, "proc", MS_NOSUID|MS_NOEXEC|MS_NODEV, NULL) != 0)
1080: 			err(EXIT_FAILURE, _("mount %s failed"), procmnt);
1129: 	if (optind < argc) {
1130: 		execvp(argv[optind], argv + optind);
```

## Challenge findings and qualifications

1. **Tool/source contract.** The directly read upstream order puts namespace creation before fork,
   blocks parent INT/TERM (L943–947), restores the child's prior mask (L963–966), waits the exact child,
   and, conditionally on `UL_HAVE_PIDFD`, closes the parent-death-before-`prctl` race using an original-parent
   pidfd acquired before fork and checked after installing PDEATHSIG. Propagation precedes proc mount and
   program exec. The precise `private`→`MS_REC|MS_PRIVATE` parser/helper mapping at L139–166 is the author's
   source attribution, not part of this reviewer's directly retained excerpt. Installed distro image/version
   hashes in PLAN19–29 are also author metadata, not independently executed/disassembled binary equivalence.
   In particular, version/help/error strings do not establish compiled-branch or running-kernel behavior.
2. **Private mount/ownership boundary.** Proposed literal argv requests only PID/mount namespaces, fork,
   KILL-child, recursive-private propagation and private proc; no user mappings or persistent namespace paths.
   Upstream unshare parent and init share the **new** mount namespace; the root caller remains outside.
   No payload action writes a file, opens a namespace handle, spawns another child or performs another mount.
   Dropping this pair can release its private mount resources; root must not perform host `umount` cleanup.
   PID/mount namespaces are not resource quotas or a security sandbox against a hostile privileged payload.
3. **Actual evidence predicate.** The fixed payload checks isolated/no-site/no-bytecode entry, PID1/PPID0,
   caller-supplied original namespace syntax and inequality, bounded self status/mountinfo, proc-local
   Pid/Tgid/NSpid1/PPid0, and proc device/type/flags. It tolerates stacked nonvisible proc-device rows and
   requires all matching rows to have NOSUID/NODEV/NOEXEC. No global process list is read. A ≤1024B JSON line
   proves neither caller input authenticity nor exit; only a separately reviewed caller can bind originals,
   collect the complete bounded output, positively wait the original child and observe unchanged outer namespaces.
4. **Agreed mount-label qualification.** PROBE61–62 rejects `shared:`, `master:` and `propagate_from:`, but
   permits `unbindable`. The JSON `private_mount_propagation` field therefore means absence of shared/slave
   propagation links, **not empirical proof of exclusively MS_PRIVATE mount class**. Unbindable is also
   nonpropagating, so this is not a shared-mount leakage bypass. Requested flags and observed predicate remain distinct.
5. **Agreed timing/signal qualification.** Imports and preflight precede handlers/limits; the payload does
   not reset the inherited signal mask. Its 3s alarm is a request, not a standalone delivery/whole-command
   deadline. CPU/address-space/FD/core limits similarly are not bootstrap or total-host bounds. The pending
   outer caller must protect startup, cancellation and collection with its own admitted monotonic bound,
   original positive pidfd, once-only KILL escalation and exact wait. Parent TERM forwarding cannot be assumed.
6. **Kernel/settlement limitation.** Linux PID-namespace init-exit member termination is the underlying
   documented contract, not an observed cancellation/descendant-kill result here. A successful finite
   init-only probe would establish that invocation's namespace/proc/normal-wait feasibility, not general
   Gradle descendant settlement. Kernel-blocked exit can outlast user-space deadlines; parent KILL/wait
   alone must not be relabelled an empirically verified init/namespace-cleanup result. Outer hard kill/host
   loss can also prevent final evidence. Unproved ownership/settlement remains HOLD, with no PID adoption,
   global sweep, group kill, alternate flags or automatic retry.

The author agreed items 4–5 by message and retained both input hashes unchanged; no source change or new
control was requested. No blocking defect was found in this **finite inner source proposal** under these
qualifications. This is not acceptance of the absent caller or proof that the environment permits the syscalls.

## Admission/accounting boundary

Root alone may advance only after actual02 independent result reconciliation, execution-slot release and
fresh exact caller/instance admission. The caller is an essential unresolved review item, not paperwork that
this note replaces. It must preserve unknown guards and global resource-conflict accounting; no held 01/02
scope is adopted, entered, cleaned or reclassified. No Gradle/03/application execution is authorized here.

New actual cases/executions, capability successes, product changes and closure credit: **0**. Only source/data
readers and this compact permanent note were used; readers exited, no temporary download/cache/runtime object
or worker remains from this review. Wrapper stop is inapplicable. Source facts do not establish an external-only
blocker; local feasibility remains untested until a separately admitted invocation supplies actual evidence.
