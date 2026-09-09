# Strict28 external envelope — independent bounded source acceptance

**ACCEPT_BOUNDED_PROSPECTIVE_EXTERNAL_ENVELOPE_PENDING_FRESH_INSTANCE.**
Author `/root`; independent reviewer `/root/editor_review`. Exact envelope
`6476c10e3086afac5af013bd5f77885d4f1e383aa83d8c0c9d204140ccef588e` (8390B/131LF),
launcher67a84793/PLANa4e59258 and prior exact source reviewfc0d2297 remain distinct.
This is not an actual request, approval, slot, invocation, test or cleanup admission.

The exact prospective root command is:

```text
exec /usr/bin/timeout --foreground --signal=TERM --kill-after=5s 200s /usr/bin/python3 -I -B /root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/linux-closeout/LAUNCH_CONTROLS_01.py
```

It uses root exec_command, `/bin/bash`, login=false, no PTY, current worktree.
The shell is replaced by timeout: **one job, two owned processes**, supervisor
and direct Python child. The accepted launcher/callable create no descendants.
Foreground supervision does not extend to hypothetical children of COMMAND.
Initial TERM200s and subsequent KILL5s are emergency failure handling; inner
120+60s Python cancellation/self-exit is separate. No kernel/D-state/host-loss,
startup-loader isolation, full monotonic duration or actual signal effect is proved.

## Actual interface metadata and preserved limitations

Root's single metadata observation `/usr/bin/timeout --help` is retained in
TIMEOUT-HELP-METADATA-01.json, SHA3bb473783038fbd5464e92f260c6fa4381fcbe77ed64a8d1c1bb65b8af4cb6c4,
14087B/100LF; tooldd845f exit0, help exit0,2146B stdout/0stderr. Exact raw helper
JSON contains matching pre/post Python and timeout full pins/hashes. The observed
help explicitly documents foreground's lack of child-of-COMMAND timeout coverage,
kill-after measured from initial signal, default TERM, and124/125/126/127/137 failure
statuses. Normal help return reaped that one metadata child, not a global census.
The command-interface self-description is sufficient for this bounded prospective
contract; **not independent implementation/manual proof or a signal regression**.
No workload COMMAND, controls or project helper was supplied to timeout --help.

Installed timeout manual and coreutils copyright were absent. Bounded installed
package data showed coreutils9.4-3ubuntu6.2/amd64 and its manifest; these establish
metadata, not semantics. Root explicitly supplied the help alternative with its
limits. No external documentation fetch, invented repository or target retry was
performed. An independent binary/source/manual semantic proof remains unclaimed.

**Material receipt-copy discrepancy retained:** raw tool_result.output is exact;
the duplicated observed.source_before/after ctime_ns values were rounded by the
author's JS JSON.parse/Number copy. Python raw1787292627933100530 became
1787292627933100500; timeout raw1787292627976100720 became1787292627976100600,
in both before and after. Exhaustive comparison found exactly those four values
different; stdout is unchanged (SHA5d66a7ae507a4e647c32e62b64231f45697a00aa9b7e7de3dcaf4702424aa1ea).
Root independently confirmed the copy defect and agreed that **duplicated observed
pins are not usable originals**. No receipt edit or metadata rerun is necessary:
raw data remain intact, and the forthcoming request must use fresh Python integer
JSON without JS number reserialization. This review does not silently normalize
rounded values or promote metadata pins into fresh actual-instance authority.

## Counterexamples and actual-instance obligations

V1's one-process prose was independently rejected; it remains at762ce031 in the
rejected-v1 snapshot. Corrected6476 explicitly counts two processes and adds
startup/environment and foreground qualifications. Root's intermediate DATA-writer
text-match failure aff308 exited1 after preserving v1; active v1 was rechecked
unchanged before correction. These are authoring/source findings, not runtime
incidents, executed controls or new product-family closures.

Before actual28: fresh shell resolution/hash/full pins/ancestry; Python symlink
and resolved binary/timeout originals; all12 source inputs; retained original Linux
bootstrap/lock and derived ancestry; absent new scratch/journal; root-only slot,
cooperative team/source/environment freeze, current resource floors and expiry.
Require exported BASH_ENV,ENV,SHELLOPTS,BASHOPTS,PS4,LD_PRELOAD,LD_LIBRARY_PATH and
BASH_FUNC_* absent; record booleans only, never values/referenced files. This is
cooperative provenance, not a hostile tool-server/startup/loader guarantee. The
actual request must bind6476 and its exact command; reviewer must author genuine
filled-instance acceptance. No source report substitutes for that event.

Only all28 named effects/COMPLETE original-only cleanup,58 literal control events,
original empty-top removal, descriptor settlement and observed external exit0 can
support this batch's qualified PASS after independent result review. Combined tool
output/time and child journal clocks have distinct scopes; no invented separate
streams/supervisor birth identity. After-only checks use the journal's exact child
PID/starttime without PID-reuse signal authority. Admission refusal before journal
may lack that witness. Unknown exit,124/137, signal, partial evidence, drift,
ambiguous cleanup/close or resource failure remains consumed HOLD/FAIL/no retry.
The foreground envelope never grants later pathname-based cleanup after death.

Fresh actual admission remains pending. Separate17/registration/whole-helper,
application, original-run settlement and hardware obligations are not inherited.
Counts unchanged19/25 original closures;22/38 all confirmed;2/12 suspicions;
8 PVD explanations/owner choices separate. Reviewer0 target executions,0 tests,
0 closures. Root's1 help metadata observation is not a test case. PVU007STOP,
PVU011NO-RETRY,PVA029FAIL/noauto,G7/G8CLOSED,WindowsFAIL/cleanupHOLD/no retry,
old-runner/publication/protected-ref/dependency/version/identity/signing/store/
occupied1017001 and synthetic/hardware constraints remain unchanged.

Only these compact permanent review files were written. All data-reader FDs are
closed; no reviewer scratch/cache/build/temp files, workers/daemons/background
jobs, process probes, signals or deletions exist. No wrapper invocation/stop duty
was created and no older duty was discharged.
