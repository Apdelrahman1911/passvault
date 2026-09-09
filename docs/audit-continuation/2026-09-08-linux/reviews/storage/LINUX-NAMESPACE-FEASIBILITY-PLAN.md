# Linux namespace feasibility — finite SELF metadata, source proposal only

Author `/root/storage`; independent reviewer `/root/verification`; root alone
may execute after separate exact caller/instance review, fresh admission and
slot release. **No namespace, mount, payload, syntax or capability probe was
executed by this author.** No Gradle/application/test/03 launch is proposed.

## Question and immutable scope

Can the existing host create one private mount + child PID namespace, execute
one finite Python SELF-metadata process as its PID1, and normally reap it?
This is not a replacement process observer or general workload qualification.
01/02 unknown/HOLD/no-retry evidence remains unchanged; no old runtime is read,
adopted, cleaned, entered or reclassified. No cgroup, user/network namespace,
global mount, privilege-policy change, external worker or dependency install.
Missing writable cgroup support does not establish impossibility: reported
SYS_ADMIN alone also does not prove effective syscall/proc-mount permission.

## Existing tool evidence, and its limits

Installed package data `/var/lib/dpkg/status`: util-linux
`2.39.3-9ubuntu6.5`, amd64. Installed `/usr/bin/unshare` read **as binary data**
contains version2.39.3 and PID/mount/fork/kill-child/private/mount-proc help,
including default kill-child SIGKILL and original-parent-pidfd error literals.
Installed man/doc files are absent despite package file-list entries.
Observed unshare image SHA-256
`51bcc77ba5db162c80028f861f0a2770d728c1de80773816d863f28d7a817adb`;
Python selector `python3.12`, real image SHA-256
`1643dacd9feaedc58f3cc581e4d22577dfe25c09b10282936186ccf0f2e61118`.

One root-authorized bounded public reference read, never executed/imported:
`https://raw.githubusercontent.com/util-linux/util-linux/v2.39.3/sys-utils/unshare.c`
SHA-256 `1ae76f987f4d2dbc1260eb7329f3b5f1d40f02f3fd6b5c31862a8ab6bcd4e1e8`,
32371B/1134LF. Data was streamed to tool output; no local reference file exists.

Relevant upstream order, not proof of exact Ubuntu binary/source equivalence:
- L139–166: `private` means recursive `MS_PRIVATE`; change `/` propagation.
- L922–927: persistence/mapping helpers are conditional; neither is requested.
- L929–930: namespace unshare must succeed before continuing.
- L943–958: block INT/TERM; when compiled with `UL_HAVE_PIDFD`, open original
  parent pidfd before fork. L963–966 restores the child's previous signal mask.
- L984–1006: parent waits exact child and propagates exit/signal outcome.
- L1009–1033: child sets `PR_SET_PDEATHSIG(SIGKILL)` then polls original parent
  pidfd; if parent already died, child exits rather than missing that race.
- L1054–1055: recursive private propagation **before** L1066–1080 proc mount
  with NOSUID/NOEXEC/NODEV; L1129–1131 execs the literal program.

Compiled-branch strings support identification, not disassembly/source-package
equivalence. Linux's PID-namespace contract kills remaining namespace members
when PID1 exits; `--kill-child` links init lifetime to its unshare parent. This
finite probe does not experimentally test descendant kill or interruption.
No claim is made that the existing distro/kernel is bug-free or all workloads
can be safely admitted using this mechanism.

## Exact proposed child argv and isolation

Root must bind absolute executable/selector/source images and original outer
PID/mount namespace identities in its fresh packet. Not a runnable shell recipe:

```text
/usr/bin/unshare --mount --pid --fork --kill-child=SIGKILL
  --propagation=private --mount-proc=/proc --
  /usr/bin/python3 -I -B -S <absolute LINUX-NAMESPACE-FEASIBILITY-PROBE.py>
  <original outer pid:[inode]> <original outer mnt:[inode]>
```

Payload is the adjacent fixed source, not arbitrary Python/argv. No user-ID
mapping, namespace-persistence filename, bind mount, chroot or shell is used.
Mount operations occur only after successful new mount namespace creation,
with private propagation before mounting its proc. The unshare parent shares
the new mount namespace with its child; root caller stays outside. No host proc
is remounted; no namespace handle/persistent mount persists outside the pair.
New namespace existence does not hide arbitrary filesystem writes: the payload
has none, and namespace isolation is not a security sandbox against hostile UID.

Payload refuses unless PID1/PPID0 and isolated/no-site/no-bytecode entry. It reads
only self namespace links, bounded `/proc/self/status`/`mountinfo`, and `/proc`
device metadata. It verifies distinct parent namespaces, private propagation,
visible NOSUID/NODEV/NOEXEC proc and self status Pid/Tgid/NSpid1/PPid0. Output is
one at-most1024B JSON line, no host process list, mount paths or private data.
No child, file, cache, server or daemon is created by the payload.

## Root's still-required minimal outer caller — not supplied/admitted here

- One fresh never-used evidence allocation outside held scopes. Freeze exact
  PLAN/payload/tool images and originals, record current slot/disk/RAM state;
  no competing audit job. Root binds both original parent namespace IDs and
  proves its own original mount/PID namespaces unchanged afterwards.
- Clean explicit environment: PATH `/usr/bin:/bin`, LANG/LC_ALL `C.UTF-8`, TZ
  UTC only; cwd `/`, stdin DEVNULL, bounded separate stdout/stderr pipes, close
  unrelated FDs, own session. Python `-S` excludes site hooks; `-I -B` excludes
  user Python configuration/current import directory and bytecode output.
- Before Popen install cancellation/finally handling, zero core-dump limit and
  a monotonic deadline.
  Acquire the original direct unshare child's pidfd before reaping it; preserve
  exact direct-child ownership and wait result. Do not use PID-name matching,
  ambient sweeps, process-group kills, namespace-ID guessing or old observers.
- Waiting unshare blocks INT/TERM in the referenced implementation: do not
  assume TERM is forwarded. On cancellation/deadline, signal SIGKILL only via
  the original owned pidfd, once, then bounded wait/reap. Normal exit needs no
  signal. Failure to establish ownership/settlement is a retained HOLD, never
  permission to adopt another process, retry or force a host-wide cleanup.
- Cap command time10s plus final direct-child wait5s; logs4KiB each, total
  evidence64KiB. Payload has3s alarm,2s CPU,64MiB address-space,32-FD and zero
  core-dump limits; no quota/cgroup claim. Root's collector must enforce bounds.
  These apply to this tiny command, not Gradle/native-RSS performance claims.
- Preserve actual returncode, original pidfd/wait accounting, raw bounded
  stdout/stderr and hashes. An internal metadata line is not external exit.
  No wrapper was attempted, so there is no Gradle-stop duty to invent.

The caller itself is an essential pending independent source-review item; this
PLAN is not a substitute or authority for an unreviewed caller implementation.
The author has not created a second lifecycle framework or another runner.

## Settlement, interruption and result accounting

Normal unshare exit0 + exact one valid metadata line + root's positive direct
wait and unchanged-parent-namespace proof establishes only this invocation's
namespace/proc/finite-init feasibility. Source-backed PID1-exit behavior grounds
the ownership domain; no additional descendants are deliberately launched just
to manufacture a cleanup test. No product test/case/finding closure is earned.

With no persistent namespace handles, namespace member exit drops private proc
and mount resources; root must not issue any host umount/rm cleanup. Retain compact
evidence/source only. Parent KILL relies on the described PDEATHSIG/pidfd race
handling; interruption is not labelled a successful descendant-cleanup test.
Outer SIGKILL/host loss can prevent finally/evidence; kernel-blocked syscalls can
outlast a userspace deadline. Mark settlement unproven/HOLD, never silently pass.

If fixed unshare/private-propagation/proc-mount permission fails, retain the exact
failure as a capability-stage result. Do not try user namespaces, sudo, seccomp/
AppArmor changes, cgroup remounts, extra capabilities or another command variant.
Stop at the precise denied/unknown contract; further work needs fresh review,
not an automatic retry. Actual kernel support, the final caller and fresh
coordination are the remaining admission gaps, not a proven external-only wall.
