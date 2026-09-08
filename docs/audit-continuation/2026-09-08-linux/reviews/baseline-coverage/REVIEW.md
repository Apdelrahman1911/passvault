# Independent baseline and coverage review

Reviewer: `/root/baseline_coverage`; original ledger/continuation author: `/root`.
Scope: baseline metadata, coverage qualifications and continuation accounting.
This is **not** a new whole-project source review or application verification.

## Baseline observations

The preserved clone and the continuation worktree initially identify commit
`9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, tree
`05014e9f635131d5db06701e4013b4b5a746465a`. The preserved clone's observed status
was clean on `codex/remediation-handoff-20260908`. Continuation writes are on
`codex/audit-continuation-linux-20260908`. The observed remote-tracking main,
testing, release and `v1.0.7-rc.1017001` identities equal the documented handoff
identities. This reviewer did not fetch, push, move a ref or query Store state.

### Transport failure retained — no retry

After reading all 277 lines of the supplied read-only reader, the documented
`verify-source` command was invoked once in the preserved clone under a
120-second bound. It **REFUSED with exit 2**: `file type/size/links`.
`transport.log` preserves the command and output. Because the shell used `set -e`,
the following planned `verify` command was **not reached**; it is not a pass.

The reader requires each regular input leaf's device to equal its directory
root's device. A subsequent bounded metadata diagnosis observed directory device
23 but regular, single-link file device 24 for both checkouts' manifest, package,
reader and AGENTS files. The first source-manifest read therefore fails before
source iteration. This is an independently confirmed filesystem/admission
mismatch, **not evidence that any G12 file hash is wrong**. The preliminary broad
metadata diagnostic produced truncated tool output; it is not retained or cited
as a complete inventory. `INPUTS.json` retains the compact selected observations.

No supplied-reader guard was weakened, no old runner was executed/imported, no
archive was extracted and no automatic retry occurred. The published **809 exact
raw members plus two declared PowerShell checkout-EOL members** remains historical
transport evidence until a distinct independently reviewed current transport
route establishes its own precise result. A Git-object check may prove pinned
Git payload bytes but does not alone prove actual checkout bytes or LF coverage.

## Independent count reconciliation

I read current top-level statuses (not embedded historical author statuses) for
all 37 PVA families and all 12 projected PVU outcomes. They support:

| Measure | Established | Remaining |
| --- | ---: | ---: |
| Original PVA-001–025 qualified closures | 19/25 (76%) | 6 |
| All PVA-001–037 qualified closures | 22/37 (59.5%) | 15 |
| Original PVU-001–012 conclusive outcomes | 2/12 (16.7%) | 10 |
| Original PVD-001–008 explanations | 8/8 documented | Owner choices separate |

These are separate denominators. PVU-010 is confirmed as PVA-027, whose runtime
verification remains blocked. PVU-012 is a scoped advisory-applicability false
positive, not blanket dependency clearance. No count is overall readiness.

The 15 open confirmed families remain PVA-001, 007, 008, 009, 010, 014, 027, 029,
030, 031, 033, 034, 035, 036 and 037. PVA-029 retains the actual 49-check result
(44 pass/five fail), with no automatic retry. PVA-031 retains setup failure before
assertions (zero cases/XML). PVA-033/034/035 retain the seven-class, five-file,
105-source-method **unexecuted** selection; neither obsolete 115/130 counts nor
compilation/task completion can replace it.

The historical verification adoption retains 70 status/run events, 684 passing
JUnit execution events (623 within PASS gates, 61 within BLOCKED gates), and 402
literal unnormalized IDs that are not unique logical-test counts. These remain
historical adopted results; this reviewer has not re-executed or independently
reparsed their complete XML set. Android fake tests, Desktop's overall BLOCKED
40-case gate, the latest G5 242-case suite, and static PVA-030 predicates keep
their original boundaries. No fresh runtime pass is established here.

## Coverage disposition and required continuation accounting

`coverage-CURRENT.json` is **accepted as qualified historical accounting**, not
re-awarded fresh review credit. It records 129,167 physical LF attributed across
714 owned text/documentation rows; 97 excluded rows keep zero credit, including
46 binary rows with null LF. Its exact original object and all normative links
must remain unchanged and reachable.

The 18 independent conditions, 12 materializer limits, 35 exact normative review
references and 176 predecessor qualifier elements remain binding in their
original scopes. In particular: 19 false script eligibility flags, six absent
original range hashes, 14 timestamp qualifications in 8/6 groups, 80 metadata-only
reads supplying zero semantic LF, cutoff123, and all failed/corrected
administrative diagnostics are not repaired or erased by this review. I inspected
the current objects and their referenced identities; I have not freshly decoded
and re-established the entire ancestral review chain.

For the continuation ledger:

1. Bind the checkpoint commit/tree and raw-manifest identity, frozen handoff
   qualifiers, actual new source hashes and exact changed/additional path set.
2. Preserve the G12 denominator as a historical denominator. Handoff reports and
   new validation infrastructure are not silently added as reviewed application
   LF. Record them in a separate infrastructure/review scope.
3. Carry historical attribution only for unchanged **complete** source tuples.
   An edited file invalidates its whole prior identity's current LF credit; do
   not carry prefix/suffix, unchanged hunk or shifted-line credit. Record raw G12,
   Git blob and checkout-EOL identities separately for the two PowerShell files.
4. Keep new authors' source-read declarations and independent challenge ranges
   separate from hash-only inspection, test-source method counts, mock behavior,
   actual test results and hardware evidence. Do not invent a fresh 100% review.
5. Count newly confirmed families only after independent reachability/guard/
   counterexample/compatibility challenge. Grouped variants do not automatically
   grow the denominator; suspicions remain outside it until adjudicated.
6. Do not raise closure counts for authored fixes or passing infrastructure
   checks. Each affected finding keeps its essential regression/target gap until
   independently reconciled evidence satisfies the particular claim.
7. Issue final report acceptance only against the exact frozen continuation
   ledger/report/source identities. This initial review is not advance acceptance
   of reports or patches that have not yet been written.

## Safety and resources

Only read-only Git/document/metadata operations and one failed read-only transport
invocation were performed, plus this review's compact report writes. No Gradle,
native build, test server, emulator, application, persistent worker, temporary
extraction or cache was created. Wrapper stop/build deletion are **NOT_APPLICABLE
to this review**, not satisfaction of earlier CLOSED obligations. Early point
samples showed approximately 4.3 GiB free disk and 22 GiB available RAM, below the
12 GiB disk launch floor. The later `INPUTS.json` sample showed 14,196,617,216 bytes
available disk and 30,626,254,848 bytes available RAM without this agent deleting
anything. That external resource change is not build admission: refresh every
relevant volume and all independent execution prerequisites before launch.
No unrelated process or file was altered.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry and G7/G8 CLOSED
remain binding. Eight PVD choices remain separate; UTF-8 to lowercase ASCII hex
KDF input compatibility is required now. No product/version/dependency/identity,
signing/publication/release or execution authority follows from this review.
