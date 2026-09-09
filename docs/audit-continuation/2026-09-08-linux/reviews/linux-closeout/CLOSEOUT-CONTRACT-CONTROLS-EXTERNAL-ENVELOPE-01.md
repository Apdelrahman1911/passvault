# Separate17 contract controls — narrow external-envelope binding

Root-authored, 2026-09-09. **Prospective until the independently reviewed actual
request and sole-slot preflight; not an invocation or retry.** This reuses the
accepted supervision contract instead of another equivalent framework/review
cycle. Independent challenge is combined with this batch's filled-instance review.

## Exact changed subject and command

- Launcher `LAUNCH_CONTRACT_CONTROLS_01.py`:
  `c482383a6e0d7cdf960e8ba99cce4c591675d02d34a49db62b354f00d42303f2`.
- Launch PLAN: `0fa6395e4303ebd2f84f77021629c1bd58cf6f47ac34e82ae4bc1e45406e5065`.
- Independent launcher source review:
  `8b025e331ddc06fefe62d29f8218ee8105390d51dd32ed4a20732f86c22d3729`.
- Callable `328cf3bc`, design `6d60685d`, independent review `6cdfa2fc` and all
  thirteen literal launcher inputs are bound in the actual request, not inferred
  from these short labels. Helper8437 and METHODS105 are source inputs only.

Root uses `/bin/bash`, `login=false`, no PTY, continuation-worktree cwd, exactly:

```text
exec /usr/bin/timeout --foreground --signal=TERM --kill-after=5s 200s /usr/bin/python3 -I -B /root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/linux-closeout/LAUNCH_CONTRACT_CONTROLS_01.py
```

This is one serial control job/two processes (timeout and its direct Python
child). The source spawns no descendants. The120s internal timer/60s grace and
external200s/5s kill grace are unchanged from component28 supervision. The
accepted limits, tool-entry and environment absence checks, cooperative freeze,
original Linux lock/parents, resource floors, interrupted-run HOLD and exact
birth/exit reconciliation are inherited from `CLOSEOUT-CONTROLS-EXTERNAL-ENVELOPE-01.md`
SHA `6476c10e3086afac5af013bd5f77885d4f1e383aa83d8c0c9d204140ccef588e`
and its independent source review
`efd3e640fbd66832c1d8ccc625c060883d64e009ce329e948c3cc74e588fdd2f`.
Its literal help evidence, not rounded duplicate metadata, supports interface
self-description only. No new signal test, GNU fetch or hardware proof is needed
or claimed. Actual pins must use fresh exact Python integers.

## Risk, smaller scope and distinct results

These17 controls cover the newly corrected full receipt contract: two valid
branches, schema rejection and two actual synthetic-file hash/pin mismatches.
The component28 journal/bootstrap cases do not cover that contract. Keep distinct
namespaces: `audit-runtime-linux-closeout-contract-controls-01` and
`CLOSEOUT-CONTRACT-CONTROLS-events-01.jsonl`. No adoption of component28 scratch,
request or case results. The literal17 results,37 control emissions, eight-file
original-only cleanup/shared-finally outcome, original empty-root removal and
external exit0 must be independently reconciled before a qualified batch PASS.

The accepted launcher installs cleanup before effects, bounds512MiB address
space/1MiB individual writes/256KiB journal, checks12GiB/25% launch and8GiB/20%
running floors, and preserves uncertainty/partial evidence without automatic
retry. No application/Gradle/old-runner execution, source/report/shared-cache/SDK
deletion, production signing, CI, publication or extra case is added. Wrapper
stop is NOT_APPLICABLE. Existing STOP/NO-RETRY/CLOSED, PVA029 FAIL, Windows HOLD,
PVD, hardware and1017001 boundaries remain. Hashes/105 fixture metadata are not
actual database allocations or application test executions.
