# PVA-029 — bounded source follow-up of the five retained failures

Author: `/root/build_config`. **Retain IMPLEMENTED — REGRESSION VERIFICATION FAILED.**
No production/test change, new finding, retry admission or closure credit. Root owns execution and the ledger.

## Evidence continued, not replayed

- Handoff issue ledger: `docs/audit-handoff/current/issue-to-fix.json`, SHA-256
  `5782666330bc2eb017e42b7a8725db1efdf54223462f3648c8acf25cb21b245e`, PVA-029 row.
- Current continuation row still preserves the same failure; observed `ISSUE_LEDGER.json` SHA-256
  `de9399e4cbbf034498be1ae22fc5798f1c951e4cdbcc3518d1066af7e0aa1fc4` and `VERIFICATION_LEDGER.json`
  SHA-256 `b377b7b5c341aa9ef758cc876883b8eaf9d4a1e77b6edf2030e326713915d0d3`.
- Two exact historical data lookups only, under root's explicit named-reader permission:
  - `remediation-reports/20260905T222925Z/verification/results-g7-candidate-diagnostic-01.json`,
    SHA-256 `0f21ca6bfda800018419d3fd511dfbc664937d21072e1ed6cb6dcfa881256aef`, 11,196 bytes.
  - `remediation-reports/20260905T222925Z/reviews/g7-candidate-diagnostic-01-actual-independent-navigation-v1.json`,
    SHA-256 `76aaf9a6267aaaf6aad76fffddb2b64bb052f47e865176dd32b1259f32b86b0d`, 23,974 bytes.

The already-admitted `scripts/audit/read_handoff_git.py` was rehashed first:
`f1e0c83a50c1b9c327ff49c5c6444cefa9206fd3c1825ba37f80cc3b7794b304`. Each named `show` used
`/usr/bin/timeout --signal=TERM --kill-after=10s 180s /usr/bin/python3 -I -B` and completed exit 0
(tool `e6d054`; second reader `0d9fde` settled in `c5f748`). These are inert, hash-bound data reads,
not archived runner execution, independent reconstruction of the entire historical run, or new tests.
They reconfirm the missing syscall identity; no third lookup or expanded tooling was pursued.

## The five failures, precisely

The retained G7 run has **49 named checks: 44 PASS / 5 FAIL / 0 skipped**. This includes 48 PVA-029
checks (43/5) and one passing PVA-028 shallow/full-history check. There are 42 functional synthetic
checks (37/5) and seven passing static wiring checks, not 49 real-provider/hardware tests. G7 did not
earn additional unique-case credit over G6. Its bounded EPERM diagnostics must not be backdated as
the cause of G6, whose original log lacked them.

All five failed names are in `scripts/test-candidate-attestation.rb:282–330`:

| Retained check | Pending exception at capture exit |
| --- | --- |
| finite stdout overflow and child reap | EPERM / errno 1, caused by `CandidateAttestation::Invalid` |
| finite stderr overflow and child reap | same |
| finite combined overflow and child reap | same |
| timeout and synthetic sleeper reap | same |
| cancellation, child reap and trigger-thread join | EPERM / errno 1, caused by `Interrupt` |

Every complete diagnostic is the correctly typed three-event prefix `spawn(pid), TERM(-pid), KILL(-pid)`.
The required successful `reap(pid)` marker is absent. There is no target-sign/type/ordering mismatch
within that prefix. The recorder logs signal attempts **before** the real call, but reap only **after**
a successful real wait. Thus a KILL marker does not establish KILL returned; no reap marker does not
establish a wait was never entered. `$!` is sampled after helper unwinding, not at the failing primitive.
The exact originating syscall and policy/host cause remain **UNDERDETERMINED**.

The unchanged sequence assertion raises before the five later expected-error and liveness checks.
Cancellation's trigger-thread cleanup has an `ensure`, but its existence does not turn that failed
case into observed successful child settlement. Historical first-seen zombie/sleeper samples and
run-level CLOSED/cleanup/scoped-stop success are not the missing per-capture reap evidence.

## Product versus oracle challenge

`verify-candidate-attestation.rb:32–113` uses a private process group, sole direct-child reaper,
interrupt-masked ownership transitions, combined-output limits, separate stdout, and TERM→grace→KILL
before the final wait. Only ESRCH is swallowed around the group signals. A different signal error
can unwind before that wait; cleanup closes the pipes but does not thereby prove child settlement.
The CLI's `SystemCallError` handler exits nonzero: the recorded error is not evidence of accepted
invalid provenance. Conversely, failing closed does not establish successful resource cleanup.

The exact-vector oracle checks the stated unreaped-PID anchor/no-signal-after-reap contract; the
retained prefix does not expose an oracle formatting bug. No evidence warrants dropping its terminal
reap requirement, accepting an EPERM path, replacing group termination with an unproven PID-only
fallback, changing host permissions, or waiting/reaping unconditionally in a way that could hang or
release the anchor before later group signalling. Such changes would need a separately demonstrated
cause and independently reviewed cleanup contract, not a wish to obtain 49 green lines.

## Invocation-identity compatibility is a separate, preserved correction

The production helper still verifies successful `gh` subject/signature and repository, approved
testing/readiness workflow, testing ref, OIDC issuer, SLSA predicate and hosted-runner policy **before**
reading the certificate digest from the same verified result row. Candidate C must resolve to its
recorded complete tree T; certificate D must equal C, or descend from C with complete tree T. It
rejects unrelated/changed/reverse-history substitutes and never fetches objects named by verifier JSON.
Strict JSON rejects duplicate fields. None of the five capture fixtures exercises live attestation
issuance, and their failures do not identify a flaw in that C/T versus invocation-D policy.

Focused current reads of Candidate Readiness confirm it retains the candidate identity when creating
readiness JSON, attests under the actual invocation, and requires its own full-history checkout and
manual promotion guard. Stable consumers use the helper for testing/readiness artifacts; final signed
Desktop verification still requires exact release `GITHUB_SHA`. Original mobile-upload discovery still
requires `original.sourceCommit`. Do not retag/rebuild a candidate or broaden these separate boundaries.

All eleven current PVA-029 changed-file SHA-256 values match the exact generation-9 source table
preserved in the handoff row; no relevant source drift was found. This is scoped byte reconciliation,
not a fresh execution of the historical seven static wiring predicates or full release re-audit.
Key independently read source identities:

```text
0ccf11b45e6687117583bd7b04fd8c42678773d9a8c29de3a0032af5798e396f  scripts/verify-candidate-attestation.rb
29f8f28db9126c9e00f6bc4517f79ad1fb6b775467abefd6161f2a0581927102  scripts/test-candidate-attestation.rb
7e56bcf461c156434ffe3df228bb5be6234094395dd8530f14d146b71ac0acfb  docs/CANDIDATE_ATTESTATION_POLICY.md
b17a0d94f2e4a04f83a77fa99ff4a6300d3eaea4961bff31bb302ba6a667acd2  .github/workflows/candidate-readiness.yml
65d91bad705bbe9b3216d07fac954869d39f48ae6253c9a09b950a0bf0cad9f1  .github/workflows/publish-stable-release.yml
3207352d5b088841c8bc4de6b4cdc3dab13186f664c34d02702fa1c286cbaf6f  scripts/lib/strict_json.rb
980b4d84c7cb1e557b2dd1df9bea534a203b31b62af49e7493d6c75591d88d05  scripts/resume-testing-candidate-receipts.rb
```

## Smallest legitimate next action / blocker

**No production or oracle correction is justified by the available diagnosis.** Preserve the existing
policy repair, all 49 checks and failed results. The missing external evidence is operation-local
identification of the error/return at the signal/wait boundary with the actual applicable Ruby/host
contract; runtime constants alone are not an installed-interpreter/kernel identity. Real verified
certificate output and hosted workflow behavior remain separate unobserved requirements. A Linux
pass, changed permissions, or a fresh hosted run would not retroactively diagnose the historical error.

This report qualifies the policy document's intended signal/reap behavior as **not regression-verified**;
it does not silently rewrite that document or offer a speculative cleanup patch. Any eventual narrow
capture correction must retain nonzero failure, bounded work, owned identity, anchor ordering and all
candidate/source guards, and receive independent challenge plus fresh explicit execution admission.

PVA-029 has **no automatic retry**; G7/G8 remain CLOSED; PVU-007 STOP and PVU-011 NO-RETRY are untouched.
No signing, store, tag, protected-ref, API, dependency, version, identity or occupied-1017001 change.
No build, test, application, syntax probe or archived program was run. No temporary extraction/cache
or output was created; only this permanent report is new. Both admitted data-reader commands finished.
Wrapper stop is inapplicable to these reads, not a discharge of root's future execution/cleanup duties.
