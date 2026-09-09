# Corrected focused macOS five: independent source review v2

Reviewer `/root/native_review`; author `/root/native`; 2026-09-09.
**QUALIFIED CONCRETE SOURCE ACCEPTANCE for root's separate fresh execution
admission. MF-R1 source-corrected; no new material source blocker identified
under the expressly limited trusted-stock-tool/closed-workload assumption.**
This is not helper execution, runtime validation, cleanup admission, five passed
cases, a family closure or hardware evidence. Root alone owns the shared slot.

## Exact current packet and preserved rejection

| Input | LF / bytes | SHA-256 |
| --- | ---: | --- |
| `scripts/audit/macos_focused_validation.py` | 617 / 33401 | `4448517d4615a34090d2acc61cd8eb872d3e8c1445fd711e8d7aa146f243b69c` |
| `.github/workflows/audit-macos-focused-validation.yml` | 68 / 2645 | `3d9c947eaacaabec26a8bea26810305ec37021773a83edee38ee1887e1356ac5` |
| `reviews/native/MACOS-FOCUSED-VALIDATION-PLAN.md` | 102 / 6615 | `bfa26b13cdab4477b9f5e118b0b7369184be1af10b9c9c47441c66fabe9d9f07` |

Review-relative paths start at `docs/audit-continuation/2026-09-08-linux/`.
The whole initial packet was independently read; this revision's complete diff
and finalization context were read, with current/unchanged input hash checks.
The archived `reviews/native/MACOS-FOCUSED-HELPER.rejected-v1.py.txt` exactly
matches606LF/32562B/SHA256
`98fa2ccae3ec771fc4f3b841d38b61a9267546da8e149818646b20d6bd5f8f72`.
The v1 independent challenge remains unchanged: Markdown
`fd41a9572028cd20d33302adb9a28a956c1d50fed47f703d38af7f562c09a14c`, JSON
`1d8089ec2404ef20e51d9c1e2866f3d4261ea817ed47d6287780a5f537910bfd`.

## MF-R1 delta independently challenged

These are **manual source traces, not executed signal controls**:

- Helper569-573 now includes cancellation/uncertainty in failure selection and
  serializes the flags before the final evidence snapshot. A flag already set
  cannot produce success merely because generated-tree cleanup completed.
- A signal during final evidence write/fsync or closing original descriptors is
  detected at587-590 and forces failure/exit1. The earlier JSON retains its
  pending-close qualification; it is not sufficient evidence of success.
- A signal while formatting/printing the console is still checked at594. A
  signal after `finish()` returns but before handler restoration is captured by
  the remaining handler and checked at613.
- Main611-612 restores default SIGINT/SIGTERM/SIGHUP dispositions after cleanup
  has finished. A later signal terminates the process instead of being swallowed
  into an unchecked flag. No second cleanup attempt is introduced.
- Normal no-signal success remains conditional on all five returned commands,
  raw XML retention, qualified cleanup, error-free evidence and closes, actual
  helper exit0, and independent result review. Missing/nonzero actual helper
  outcome cannot be overridden by an earlier JSON or console success snapshot.

The author agreed the original counterexample and independently explained the
minimal correction. The only changed source is finalization; workflow, workload,
filesystem deletion authority and case declarations are unchanged. This closes
the identified **source-review objection**, not a tested runner-defect family.

## Retained substantive review and compatibility qualifications

The v1 report's positive-path/failure-path review remains applicable: exact
branch/add-only request and checkout/native identity checks; minimal immutable
workflow; five exact selectors; bounded command/evidence/cleanup handling;
no-follow original handles; strict private-root ACL requirements and the narrow
ancestor deny-delete-only allowance; no fixture-residue adoption or cleanup
retry. All five native hashes in v1 were revalidated against unchanged reviewed
source. Their assertions and provider guards are reused, not silently re-audited.

Normal settlement is **qualified to synchronous trusted installed tools and the
reviewed closed workload**. Actual normal returns plus EOF and fixture checks
are not proof against arbitrary detached descendants. Nonzero CTest, forced
stop, lost EOF or ambiguous launch prohibits later cases/tree deletion; raw
failure XML is merely a bounded snapshot. Normal nonzero configure/build cleanup
depends on the explicitly accepted synchronous stock-tool assumption. Hosted
runner disposal remains containment, not observed worker/fixture cleanup.

No custom XML parser or source-capture framework is prerequisite in this new
packet. Actual exits/raw XML require independent manual reconciliation: exactly
the selected case, failures/errors/skips, logs, source and cleanup outcome.
The intentional early-return diagnostic is not by itself a test failure.
Stock metadata output is trusted fixed-output buffering with a post-completion
cap, not a generic streaming-output guarantee. Installed versions, runner
architecture, ACL layout and resource floors remain **expected**, not observed;
drift must fail closed without installation, fallback or automatic retry.

## Precise next boundary

Root must bind the reviewed packet to the actual source commit/tree, then
separately admit and record the new add-only activation commit/parent/tree/nonce,
exact five-case command scope, target requirements/bounds/cleanup and idle shared
local/CI slot before pushing the request. This report creates no request or
execution authority. It does not revive the old partial/capture framework, whose
failed/unexecuted history remains unchanged, or license retry of any closed run.

No helper execution/import, syntax/control probe, build/test, GET, CI query,
activation or runtime artifact/worker was created by this review. Only compact
permanent reports were written; no cleanup task/process was left for root.
No product family, suspicion, design or closure count changes. Physical iPhone,
Touch ID and Windows Hello remain BLOCKED. PVU-007 STOP, PVU-011 NO RETRY,
PVA-029 failure/no automatic retry, G7/G8 CLOSED, Windows01 failure/filesystem
HOLD, protected refs/publication, identities/dependencies and build1017001 fences
are unchanged.
