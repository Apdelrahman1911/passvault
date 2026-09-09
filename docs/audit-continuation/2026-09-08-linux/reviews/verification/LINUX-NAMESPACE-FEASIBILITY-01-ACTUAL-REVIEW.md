# Independent namespace01 actual-result reconciliation

Reviewer `/root/verification`; executor/source author `/root`.
**CONSUMED FAIL / NO RETRY. Final namespace-feasibility PASS not established.**
The retained normal error-return/direct-wait evidence supports releasing **only this consumed scheduling
reservation**, not a general cleanup guarantee, another execution, or any earlier HOLD.

## Exact admitted and actual inputs

- Request `663643572db5d1b180af73f6bfdea44f331922233b6624e8701fe19672c64fad` and independent admission
  `b049cb01792425ba3f0f4285602188e0d13e98438cef45f3e5438b41ea9a57d9` remain bound to caller `1512cc71…`,
  payload `96eb1d54…` and plan `bb1f71d1…`; full hashes are retained in that admission/source review.
- `requests/LINUX-NAMESPACE-FEASIBILITY-01-ROOT-LAUNCH.json`, SHA-256
  `a984bc553e7ba2c966e6fdf90a2b1f54fc864babc52e72fd4ea92f6d88921d3e`, 4289B / 131LF.
- `reviews/storage/NAMESPACE01-EXTERNAL-RESULT.json`, SHA-256
  `ed2fb7e9fed8df808feb625f804f5c706e5343cd0c308a4151a0c8b01cf22611`, 2300B / 55LF.

Both actual receipts were completely read as data in `c94ec3` (exit 0). Launch at 12:21:15.301239Z was
inside the request window ending 12:43:40.109306Z. It binds the accepted source/tool/lock pins, original
private evidence parent and namespace pair, absent destination, exact argv, sole-slot/quiescence declaration,
and renewed resource observations. These are reconciled retained root observations, not reviewer runtime probes.
The intent's closer-to-entry free disk 19,590,627,328B and available RAM 38,379,962,368B remain above the
specified entry floors. No measured peak/resource-exhaustion or wall-time guarantee is inferred.

## Original evidence reconciliation

Tool `39b8ab` (exit 0) read only the five admitted new evidence files through no-follow directory/file fds.
All were stable root-owned regular mode0600/nlink1 files, exact lengths/hashes, with no additional name.
Original parent dev23/inode642474/UID0/mode0700 matched. Current E dev23/inode846349/UID0/mode0700/nlink2
matches INTENT's original directory identity; directory time changes from record creation are not treated
as identity changes. Full observed file pins are retained in the tool output; no old runtime was inspected.

Under `runs/linux-namespace-feasibility01/`:

| Original file | Bytes | SHA-256 |
| --- | ---: | --- |
| `CHILD.json` | 61 | `e53bba963eba6d7bbd5e1935cbf8142b29565be5d83aac4050f1c14198b600da` |
| `INTENT.json` | 828 | `249b18f0e79f08e5185e88b983a18b4beb713bb2936c144650d788674f427a81` |
| `RESULT.json` | 451 | `7ac1eaed030743c11d1847667177e19d09f39338c5576a8e4d25a011bd9a01f2` |
| `stderr.log` | 41 | `9a88981efe75b5b34b9b9c18f25973cafefb129247c3b68040c603d52e1852d6` |
| `stdout.log` | 0 | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |

Total original run evidence: **1381B**. Data/hash comparisons are not additional tests or probe invocations.

## Actual outcome and reached stage

- The once-only external tool result `407390` records caller **exit1**, no running-session continuation,
  with only its identity line: caller PID16331/start22550030, original PID/mount namespaces.
- INTENT matches the admitted literal unshare/Python command, clean environment and cwd `/`.
  CHILD identifies the original unreaped direct child PID16332 and pidfd ownership.
- RESULT reports direct wait completed, child exit1, **no pidfd KILL attempt**, exactly `unshare_exit_1`
  in errors, and equal before/after `pid:[4026531836]` / `mnt:[4026531841]` namespaces.
- stdout is empty. stderr is exactly `NAMESPACE_SELF_REFUSED: mountinfo fields\n` (41 bytes).
  No final metadata JSON was emitted and the full predicate was not satisfied.

**Narrow source-order inference:** with the frozen payload, that refusal comes after isolated entry,
PID1/PPID0, distinct-from-original namespace checks, successful limit requests, bounded self-status parsing
with Pid/Tgid/NSpid1/PPid0, bounded mountinfo read/count and proc-device metadata. This run reached that
payload guard; it is **not an observed unshare EPERM or pre-entry capability-denial result**. The referenced
utility source places private-propagation/proc setup before payload exec, with its previously recorded
installed-binary/kernel qualifications. This does not establish all mount propagation/visible-proc checks,
exclusive MS_PRIVATE class, every capability, or final feasibility success.

The failing mountinfo row was not retained by this deliberately non-disclosing payload. Its exact field
contract/cause is not adjudicated here; the separate native review owns that source-only hypothesis.
No altered parser, recaptured namespace data or second invocation was used by this reviewer.

## Settlement and release limits

The corrected caller's source supplies genuine unreaped-child ownership/default SIGCHLD, normal direct wait
and post-result handle closure. Recorded wait1/no-signal and terminal caller exit1, with no traceback or
post-result close error in the retained external output, fit its normal error-return path after finally.
Thus the evidence supports the finite direct-child settlement/consumed scheduling release; it does not
turn the PRETERMINAL result into a feasibility PASS merely because the expected validation failure ended.
No new settlement-HOLD/pipe-close/close-failure error is recorded for this invocation.

Private init exit and unshare waiting provide the reviewed source-backed lifetime argument. There was no
empirical cancellation, descendant-kill, kernel-object reclamation, arbitrary-descendant/global-inactivity
test or fresh reviewer liveness sweep. In particular, the unshare-parent→init PDEATHSIG contract is not an
outer-caller-hard-kill guarantee. Keep all those limitations; release only the namespace01 scheduling slot.

Preserve this failure and necessary small evidence. No automatic retry/alternate flags, general workload,
Gradle/03, wrapper stop, host umount/rm, old runtime adoption, recovery, source-store write or publication is
authorized. Linux01/02, Mac/Windows and all PVU/PVA/G7/G8 fences remain unchanged; no unknown row is reclassified.

Accounting: **one executor namespace invocation; zero product tests/regressions/closure credit**. Reviewer
execution/probe/import count: **0**. Only bounded source-bound data/metadata readers and this permanent report
were used; readers closed their fds and exited. No temporary file, cache, namespace, workload worker or new
cleanup duty was created by this review. Root retains central ledgers, scheduling changes and resource monitoring.
