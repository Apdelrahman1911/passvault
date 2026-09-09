# Linux containment feasibility — read-only metadata

Author: `/root/verification`. Observation began **2026-09-09T11:00:04Z**.
Tools `c42c8f` and `b599e1` both exited 0. Independent challenge pending.

**No currently exposed delegated writable cgroup-v2 containment interface was identified in the inspected
mount namespace.** Stop here: these observations do not admit another application/build execution or clear
Linux02's cleanup HOLD. They also do not prove that creating a new mount/namespace is technically impossible;
creation was neither authorized for this task nor attempted.

## Exact relevant observations

`/proc/self/cgroup`:

```text
0::/
```

All mountinfo rows were filtered for ` - cgroup2 ` or ` - cgroup `: **none**. The applicable `/sys` mount was:

```text
23 29 0:20 / /sys rw,relatime - sysfs  rw
```

Read-only `stat`/filesystem metadata:

| Path | Type/mode | UID:GID | Device/inode | Filesystem/type |
| --- | --- | --- | --- | --- |
| `/sys` | directory, `0555` | `0:0` | `20/1` | `sysfs` / `62656572` |
| `/sys/fs` | directory, `0755` | `0:0` | `20/2` | `sysfs` / `62656572` |
| `/sys/fs/cgroup` | directory, `0555` | `0:0` | `20/3` | `sysfs` / `62656572` |

The following exact `/sys/fs/cgroup/` interface paths were absent:
`cgroup.controllers`, `cgroup.subtree_control`, `cgroup.type`, `cgroup.procs`, `cgroup.kill`.
No process membership file was read; no unrelated processes were enumerated.

Thus the visible `0::/` membership record is **not** evidence of a writable cgroup-v2 control surface or
delegated subtree. `/sys/fs/cgroup` is a directory on sysfs, not a cgroup-v2 mount. Calling this a
“read-only cgroup2 mount” would also be inaccurate: the covering mount reports **rw sysfs**.
There are no exposed controllers/delegation files at this conventional path to inspect further.

Self metadata (not a capability-execution test):

```text
Uid/Gid:          0 0 0 0 / 0 0 0 0
Groups:           0
CapInh/CapAmb:    0000000000000000 / 0000000000000000
CapPrm/Eff/Bnd:   000001ffffffffff / 000001ffffffffff / 000001ffffffffff
NoNewPrivs:       0
Seccomp/filters:  0 / 0
cgroup namespace cgroup:[4026531835]
PID namespace    pid:[4026531836]
PID-for-children pid:[4026531836]
user namespace   user:[4026531837]
mount namespace  mnt:[4026531841]
uid_map/gid_map:  0 0 4294967295 / 0 0 4294967295
```

The reported capability mask includes `CAP_SYS_ADMIN`; **capability absence is not the blocker established
here**. Nor do that mask, numeric namespace IDs or identity maps demonstrate usable host mount/namespace
authority. Self and PID-for-children name the same existing PID namespace. This limited check establishes
neither a prepared disposable audit-specific PID domain with an outside owner, nor whether a new one could
be created. No `mount`, `unshare`, namespace transition, child-workload launch or capability probe was tried.

## Consequence and boundary

The current metadata supplies no admitted stronger positively-owned descendant-settlement primitive.
An external provider would need to expose/identify an actually delegated writable cgroup-v2 subtree (or an
independently owned isolated execution domain), with independently reviewed resource accounting, ownership,
settlement and cleanup semantics, before it could be relied on. Whether such a provider can do so remains an
external question; this report neither designs a replacement observer nor recommends an automatic Linux03.

Unclassified/vanished global process observations are not reclassified, ignored or hidden by this result.
Global resource conflicts, Linux02's unstarted product cases and cleanup HOLD remain for root's independent
result reconciliation. No held scratch/evidence/recovery scope or archived helper was accessed.

Only self metadata readers and this necessary permanent report were used. **Zero** builds/tests/helper
imports, cgroup writes/access-mutation probes, mounts, namespaces, caches, temporary runtime artifacts or
workload workers were created. Metadata readers exited; wrapper `--stop` is inapplicable to this task.
Root remains sole build owner and owns shared-resource monitoring, held-scope decisions and future admission.
