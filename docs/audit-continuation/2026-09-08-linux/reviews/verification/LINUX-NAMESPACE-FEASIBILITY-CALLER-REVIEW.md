# Independent finite namespace caller source review

Author `/root`; reviewer `/root/verification`.
**ACCEPT_CORRECTED_CALLER_SOURCE_ONLY; exact actual instance/admission remains pending.**
No caller/payload/namespace execution, import, AST/syntax check, forced-child control or old-scope access occurred.

## Source binding and preserved preexecution corrections

Root-owned path: `reviews/storage/LINUX-NAMESPACE-FEASIBILITY-CALLER.py`.

- First source: `4c9cef74a2acb71a85a0065157b05aba018a9d22f0c958006de178d2cc2b98d0`, 9715B / 203LF;
  completely read in tool `b3232f` (exit 0). **Not admitted or executed.**
- Corrected source: `1512cc719b06ff952bb0edd48941777becc0542674b7af560d1502a4613f4d01`, 9972B / 207LF;
  corrected regions inspected in `142621` (exit 0). Reversing exactly the two literal edits in memory
  reproduced the first hash and 9715B in `a32294` (exit 0). That is source-delta metadata, not a test execution.
- Prior inner/source review remains unchanged:
  `LINUX-NAMESPACE-FEASIBILITY-SOURCE-REVIEW.md`,
  `a18db6fc4ebb3d1f36fec0a7911e1ac146c13baafaa05dcd3092869df62e5499`.
  This review did not rerun or re-review the unchanged inner payload/upstream source.

Two useful contract issues were independently challenged and root corrected them before any admission:

1. **Genuine wait-status ownership, not synthetic success.** First source did not normalize SIGCHLD.
   An inherited ignored/auto-reap disposition is not repaired by a clean environment or Python `-I/-S`.
   Installed `/usr/lib/python3.12/subprocess.py`, read as text only, SHA-256
   `baa9f9138d8d20df6284f67e7d2e790f847f65e2c5370de322d54cccd737f2d9`, explicitly substitutes zero on
   ECHILD/ChildProcessError (`_internal_poll`, L1993–2002; `_try_wait`, L2008–2018). A pidfd is not itself
   evidence that Popen recovered an actual exit status. **Surrounding counterexample retained:** referenced
   unshare's own exact wait may emit ECHILD stderr, which the caller rejects. No complete-packet false PASS
   or observed failure was established. Corrected L61 sets `SIGCHLD` to `SIG_DFL` before spawn; this fresh,
   single-owner caller has no other child waiter. Exact wait no longer relies on that secondary stderr guard.
2. **Signal failure must not skip wait.** First L148–154 put pidfd KILL and bounded wait in one try block.
   Natural exit between poll and signal can yield ESRCH; a signal error then skipped the requested wait.
   Corrected L149–158 records a sticky `owned_signal_failed_no_retry` error but still reaches its one
   bounded wait. No second signal, numerical-PID fallback or adoption was added. Failure to settle stays HOLD.

Root agreed the objections and the narrower no-demonstrated-whole-packet-false-PASS qualification. These are
preexecution source corrections, not completed operational fixes or new product finding/closure credits.

## Accepted narrow contracts and remaining limits

- **Fixed command/source.** Exactly one literal unshare command launches the previously accepted fixed
  PID1 self-metadata payload. It is not a generic runner. Image hashes and the Python selector are checked;
  the actual outer invocation must use the admitted `-I -B -S`, **without `-O`/optimization**, no caller args,
  and exact clean environment. Assertions are not authority for arbitrary alternate Python entry flags.
- **Positive child ownership.** Cancellation handlers are installed before spawn and record signals without
  interrupting the Popen-object assignment. SIGCHLD is normalized, and the original unreaped direct child's
  pidfd is obtained before any `poll`/`wait`. KILL is once-only through that fd, never a guessed PID/group.
  If acquiring the pidfd fails, no unowned signal is substituted; bounded wait/error evidence must remain
  conservative. Normal completion needs no signal. Hard kill of the caller is not linked by PDEATHSIG to
  unshare itself; the reviewed unshare-parent→init contract does not magically add that missing outer link.
- **Coordination/evidence.** Nonblocking flock uses the exact existing Linux lock fd tuple; a new exclusive
  evidence directory is created under the private runs parent. Records use its open directory fd and
  exclusive no-follow creation; source, held runs and caches are not deleted. These checks rely on the
  actual packet's unchanged, root-controlled source/lock/ancestor paths, not protection from a hostile
  privileged filesystem actor. The historical tuple is not automatically current Linux execution authority.
- **Bounds.** Nonblocking separate stdout/stderr buffers cap retained logs at 4096B each; truncation is a
  sticky failure. The collection deadline is monotonic 10s and the final direct-child wait is 5s. It is not
  a hard bound on bootstrap, Popen internals, filesystem fsync or kernel-blocked operations. The caller also
  requests zero cores, 128MiB address space and 64 FDs; these are not cgroup/host-resource guarantees.
  Disk/RAM entry checks do not authorize hidden conflicts or a general Gradle workload.
- **Result/cleanup ordering.** A normal outcome requires exact metadata conditions, no stderr/errors,
  actual direct wait, unchanged outer namespaces and eventual caller success. `RESULT.json` explicitly says
  PRETERMINAL; it precedes selector/pidfd/evidence/lock closure. Closing errors therefore force an external
  nonzero result rather than making the preterminal record final. Record-writing failures can prevent a
  receipt while finally still attempts closure; no missing receipt is treated as success.
- **Interruptions remain qualified.** A parent kill attempt/wait alone is not empirical proof that namespace
  init/descendants settled. No forced cancellation or descendant-kill test is proposed. Root must reconcile
  actual exit and evidence, preserve any HOLD, and not hide unknown global conflicts or claim broader cleanup.
  The inner mount-label/bootstrap and installed-binary/kernel qualifications in the preceding review remain.

No further blocking source defect was found for this finite proposal under these boundaries. This statement
does not certify arbitrary concurrent callers, a hostile host, every interruption or an unobserved syscall result.

## Essential actual-instance gate — not yet accepted

Root must supply the exact invocation/source identities, current immutable-source/selector/tool pins,
current original Linux lock and private evidence-parent tuples, never-used evidence destination, original
outer PID/mount namespaces, resource snapshot, and actual02 independent reconciliation/slot release.
Those inputs must be reviewed **before** entry, not reconstructed as permission after a successful line.
No held 01/02 scope may be adopted; no Gradle/03, alternative namespace flags, privilege changes or retry
is admitted here. Global resource-conflict and unknown-ownership restrictions remain.

New executed product cases, namespace invocations and closure credit in this review: **0**. Only bounded
source/data readers, an in-memory literal hash comparison and this permanent report were used. Readers
exited; no worker, cache, download or temporary runtime object was created. Wrapper stop is inapplicable.
