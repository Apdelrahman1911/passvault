# Checkpoint 3 publication qualifications — read before execution

This additive notice qualifies the frozen checkpoint3 selection without changing
its source/evidence bytes. It is not an execution, cleanup or release approval.

## Newly discovered closer contract blocker

Before publication, `editor` reported and `baseline_coverage` independently
confirmed that `linux_database_closeout.py` at SHA-256 `b16774ff...` requires the
same ordinary JSON `LAUNCHER.json` field, `evidence`, to be both the fixed evidence
directory string and a list of evidence-file descriptors. Those requirements
cannot both hold. The strict JSON reader rejects duplicate keys. Admission fails
before the closer acquires its lock or creates its journal/deletes outputs.
This is a fail-closed validation-infrastructure defect, not a new product family
or an observed destructive operation.

Read `../reviews/baseline-coverage/CLOSEOUT-LAUNCHER-CONTRACT-DEFECT-REVIEW.json`
and its paired Markdown for exact source identities, reachability, counterexamples
and the independent reviewer's disclosed earlier miss. The frozen source
acceptance and checkpoint3's descriptions of it are **historical acceptance only**;
they do not establish a currently viable prompt-closeout design. The database
build remains **BLOCKED** until a corrected successor, meaningful regression and
fresh applicable source/instance approvals satisfy the existing prebuild gates.
No build, closer invocation or retry was performed in discovering this defect.
The unrelated empty-evidence bootstrap remains subject to its separate exact
request, expiry, approvals, sole-slot reservation and fresh preflight.

## Other required precision

- Current accounting is **19/25 original qualified closures, 22/38 all confirmed
  qualified closures, 2/12 original suspicions conclusively resolved**. Frozen
  specialist text carrying 22/37 is stale inherited accounting, not necessarily
  evidence that PVA-038 postdated that text. Eight PVD explanations remain
  separate from owner decisions. Zero new application cases or closures.
- The selected checkpoint's PVA-038 9-routing/4-provider description is tentative,
  author-reported excluded work, not an accepted C3 test inventory. Later patch
  source/reviews stay outside this selection and require separate publication.
- The original whole-index whitespace check remains **FAIL/exit2**. Its eight
  diagnostics are exact preserved blank-context patch records. The independently
  reviewed qualification does not rewrite that outcome. The retained raw log
  itself adds expected evidence-log diagnostics in an expanded index; record the
  actual later check rather than suppressing or sanitizing them. The separate
  five-path source check was exit0, not compilation or runtime verification.

All original STOP, NO-RETRY and CLOSED scopes, failures, hardware limitations,
synthetic-only/sole-executor safeguards, protected-ref/publication boundaries and
occupied mobile build1017001 remain unchanged. Unlisted active agent work is
neither staged nor admitted by publication of this selected checkpoint.
