# NEW Detekt02 outer — independent source-only delta review

**Disposition: ACCEPT_EXACT_FROZEN_OUTER_DELTA_ONLY; UNBOUND / UNADMITTED.**
Reviewer: `/root/verification`; author: `/root/build_config`.
No concrete blocker in this bounded outer delta. This is not C16 binding, instance approval, execution/coordination/cleanup admission, report-retention proof, or a passing result. No C16 commit/tree is supplied or inferred.

## Exact source and reuse

All paths below are relative to `W=/root/projects/PassVault/passvault-linux`; `B=docs/audit-continuation/2026-09-08-linux`.

| Inert input | SHA-256 | Bytes / LF |
| --- | --- | --- |
| Consumed01 `B/reviews/detekt01-outer/LAUNCH.py` | `a0be29d62dfaab5db7b8b4638cc28b70829bd84750a9f403181ac9f42c0cc091` | 53689 / 912 |
| NEW02 `B/reviews/detekt02-outer/LAUNCH.py` | `545de8da07253be6153533d8e97b583aab3b1c78f9d39f0c399d277509d4ce3a` | 53397 / 912 |
| Author `B/reviews/detekt02-outer/DELTA.json` | `dbab82d50b659bfd650b32245d05fd631ed6c363f1d894981bfb0ce25bc64f85` | 6623 / 157 |
| Relevant inner contract `scripts/audit/linux_detekt_02.py` | `e5b2352080d4eafc2d22c3a50a7d3f4b31f4b324672cab1e59bdef8b91a2d294` | 55245 / 955 |
| Referenced init `scripts/audit/detekt_02.init.gradle` | `2ff75e0d26f4bd2d7bca7a0c62d929fed9174115ad20b665e5b10caabe51b9e6` | 2838 / 55 |

Reuse the accepted01 outer reasoning, rather than reopening unchanged machinery:
- `B/reviews/verification/LINUX-DETEKT01-SOURCE-DELTA-REVIEW.md`, SHA `a3f6935fc90b92d76d1719cc9121889cae106a31b0f6baf816fd8353700defb8`, 17050 bytes.
- `B/reviews/verification/LINUX-DETEKT01-INDEX-CONSTANTS-REVIEW.md`, SHA `b7dfd133511db63f3cc8ff9a492f42f0256f904c90fbe12b42833ccc17b3c6a8`, 9128 bytes.
- The previously independently verified C15 literal-binding inverse links those reviews to the exact consumed01 source above; no functional acceptance is inferred from its actual failed run.

The first two reviews were rehashed. The complete current outer textual diff was read. Independent literal forward construction and inverse recovery both matched the exact input bytes: five namespace replacements (`detekt01` 10 occurrences, `detekt_01` 2, `Detekt01` 2, `DETEKT01` 2, `DETEKT-01` 1), plus six one-occurrence binding substitutions described below. After namespace normalization, **the entire body from `def require` onward is byte-identical**, SHA `74e691d74b3fa9643db20b23c6f2a41ce7ea0e97dad83651ac96b07a0fa2c4a1`. This is an inert text proof, not target import, AST/syntax validation, execution, or tests.

## Fresh labels and genuinely unbound intake

The six other replacements clear EXCLUDE_STATE; change SOURCE to `B/reviews/detekt02/source-prepare01/SOURCE.json`; clear the COMMIT/TREE/MEMBERS block; clear DEVICE; clear EXPECTED_LOCK; and update the exact inner/init control hashes while clearing FROZEN[SOURCE].

R, E, SELF, REQUEST, APPROVAL, RUN, PURPOSE and all request/approval/result/allocation/receipt labels are distinctly02. The shared coordination-lock and publication-store *paths* remain intentional, not inherited identities or authority. The old runtime/evidence namespace is not referenced as a target. No01 source or held runtime was changed or accessed for recovery.

Outer L680–697 rejects the unbound None values before request intake, lock acquisition, allocation, or child launch. Root must bind its actual fresh source/index/store/tool/exclude/device/original-lock facts, then obtain the genuinely fresh request and independent instance review. L709–723 still binds run/purpose/commit/tree, exact coordination, request hash, reviewer, and all four source hashes; L724–746 retains original lock, image, parent, manifest and two checkout-EOL qualification checks. L747 still rejects occupied new R/E names. This report deliberately creates **no** `LINUX-DETEKT02-INSTANCE-ACCEPT.json`.

## Failure versus uncertainty: independently challenged route

**Correction to an overbroad working interpretation:** consumed01 did not reject every nonzero inner exit. Its L806 already accepted isolated child0/1, while requiring Git child0. The actual01 child70 was rejected as uncertainty. NEW02 adds no permissive return-code change.

| Actual source-path conditions, not observed executions | Outer result if every remaining guard succeeds |
| --- | --- |
| Ordinary static failure; inner exit1; complete safety/source/stop/settlement/Git proofs; attempted reports preserved | Allowlisted cleanup may finish; `VALIDATION_FAILED_CLEANED`; return **1**, never PASS |
| Inner exit0 with attempted Detekt, preserved reports, static_mapping_ok and validation_mapping_ok all true, plus all safety proofs | Cleanup may finish; `COMPLETED_PENDING_INDEPENDENT_RECONCILIATION`; return0, not independent PASS |
| Git nonzero, inner70/other exit, latched reasons, missing/false safety proofs or uncertain retention | No normal-success route; HOLD/non-success, no automatic recovery/retry |
| Later source/index/image/lock, namespace, mount, resource, cleanup or finalization uncertainty | Exception/latched reason/nonzero exit remains authoritative; partial cleanup is not reclassified as completion |

Reachability is explicit, not a mock assertion: inner L849–866 retains the ordinary failed command state, attempts the original stop and settlement, then preserves reports. L914–943 separates `safe` from `passed`: a completed nonzero analysis with safe evidence returns1, while unsafe evidence returns70. Outer L806 and L835–867 then require matching identities, true original safety proofs, fixed static selector and **zero declared application cases**, ordered phase contract, and retention for an attempted Detekt phase. Only code0 is additionally required to carry successful static/validation mapping. L871–876 rechecks source, authority and host watch, invokes the unchanged cleanup routine, then returns the original code. No code1 branch produces a success status.

Necessary distinction: missing/malformed inner safety/phase/identity records or uncertain read/write/retention prevent this safe route. Completely retained analyzer bytes that fail semantic parsing, a known absent expected analyzer report, or contradictory task mapping may instead establish **recorded validation failure**, not analysis PASS. Inner L705–744 only records absence at an initial entry lookup; disappearance of a pinned directory or an observed file, read/write drift and capture-budget failure are collection errors. L822–836 makes preservation depend on no collection errors, separately from semantic/header validity. This contract does not authorize deletion of uncertain evidence. The new inner/parser implementation has separate assigned source reviewers; this review inspects its boundary contract only and claims no parser regression execution.

Inner final evidence-write/readback/close failure returns70. Outer default status70, exceptions, receipt/descriptor-close failures and latched reasons preserve non-success (L878–912). A preterminal receipt, even one reporting removal, is not the authoritative external exit or independent cleanup reconciliation.

## Unchanged safety boundary and limitations

Exact body equality preserves the original process identity/pidfd/namespace settlement, original lock/flock/parent binding, fixed environment/images and aliases, raw-blob transport plus sealed-index/source checks, exclusivity/resource watches, complete original-directory/allowlist cleanup, mount absence checks, cancellation/final closes, and all time/file/byte/count caps. No new stop, signal, adoption, metadata/index fabrication, unmount, cleanup retry, or old-runtime recovery route was added.

The author's94/95 whole-E count is explicitly prospective source accounting, not measured retention. No outer cap was increased. Report capture and new parser behavior are separate source-review lanes; this outer acceptance does not silently certify their execution, real report formats, all22 analyzer completion, global idleness, physical hardware, or cleanup after interruption. Existing generic-interpreter screening, hostile/outside mutation, blocked syscall, no-escape and cooperative time-bound qualifications remain.

## Evidence and handoff

Inert reader tool chunks: `ca88f6` (identities/full diff), `1bd526` (independent forward/inverse and outer contract), `dd41e5` (inner return boundary/prior review hashes), `79517e` (retention-versus-semantic-failure boundary). All four short-lived readers returned exit0. Their output is source-review evidence, **zero target executions/test cases**. Inputs were frozen-hash checked again immediately before exclusive creation of this report.

This lane used bounded, component-wise no-follow, single-link regular-file reads with original identity checks; it did not import/execute/compile/syntax-check either runner or parser, run Git/network/CI, probe a host/process/tool/SDK/runtime, signal/stop/delete anything, or access held Detekt01 runtime. Only this compact permanent review was created, exclusive0600 with file/parent fsync and exact readback/hash; no cache, temporary file, build artifact or background worker was created. Root remains sole build/Git/CI/process/cleanup owner.

No closure or denominator change. Preserve actual01 failure, held-runtime/report-retention limitation and consumed scope; all earlier successes, rejections, grouped variants and unresolved gaps; PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 recorded failure/no automatic retry, G7/G8 CLOSED; eight PVD boundaries and genuine hardware gaps; protected refs/tags, signing/store/publication and occupied1017001 fences. A future run requires root's separately admitted C16 binding, coordination, exact new instance and cleanup scope. This source acceptance is not that admission.
