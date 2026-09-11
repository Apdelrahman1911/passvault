# C18 current-overlay integration review

Reviewer: `/root/coverage_review`  
Report written: `2026-09-11T02:22:07.472524+00:00`  
Reviewed ledger snapshot: `2026-09-11T02:01:53.720186+00:00`  
Data review completed: `2026-09-11T02:19:20.094520+00:00`

## Disposition and scope

**ACCEPT bounded C18 overlay counting and evidence identity, with limits.** No material counting or evidence-identity discrepancy was found in this bounded review. This is **not final checkpoint acceptance**, fresh semantic patch approval, application validation, cleanup authorization, a new full source freeze, or publication approval.

Detailed pins and comparisons are in `C18-INTEGRATION-REVIEW.json` (38,402 bytes), SHA-256:

`513a249f95d34cae6dc2eb06cd9d494e6066ee7d95bba59e4cb6b9c9a730712c`

Only source/retained-data reads and Python standard-library JSON, hash and in-memory text comparisons were used. No builds, tests, project/helper execution or imports, Git/T access, CI/network/process/SDK/runtime probes, cleanup, source/central-ledger edits, or subagents. Writes were confined to this review lane. The unchanged 51 XML files and historical blocks were not recounted or re-reviewed.

## Integration identity and registry accounting

- The root release and integration receipt identify the same **10 accepted patches**. Their patch hashes and referenced independent-review hashes match retained files; each accepted patch hash is explicitly present in its review.
- **18 file-edit events affect 17 unique source paths:** 16 modified existing files and **one newly created file**, `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/BackupDatabaseTextOrder.kt`. `VaultRepositoryImpl.kt` has sequential database02 then database03 edits.
- Planned release after-images, actual receipt after-images, all three ledgers' after-images and all 17 current source byte images agree.
- Independently reversing **79 unified-diff hunks / 18 file-edit events in memory** recovered all 16 C17 existing-file hashes/lengths and absence of the sole new file. No patch was applied to disk. This verifies byte ancestry, not Kotlin semantics or runtime behavior.
- The coverage registry changes **31 -> 36**: 19 complete rows unchanged, 12 existing rows updated, five rows added, none removed. **Five added registry rows do not mean five new repository files:** four files already existed at C17; only `BackupDatabaseTextOrder.kt` is newly created.
- All 36 current SHA-256/byte/physical-LF tuples match the reviewed W files. All 31 historical before-tuples match pinned C17 metadata. The 19 unchanged complete rows compare equal. The historical 31-row records remain retained; broader old ancestry/authorial semantics are inherited, not independently recreated.
- All 36 current rows carry null new semantic-LF credit and false equal-line credit. No changed `@Test` declaration counts or added/removed `@Test` diff lines were found. This is a textual declaration comparison, not parsing, compilation or execution.
- The new `desktop_integration03_execution`, `c18_application_integration` and `pva031_supported_input_followup` blocks agree across ISSUE, VERIFICATION and COVERAGE. All linked new evidence hashes match.

Primary root/source pins (full ledger, source-row and review pins are in the JSON):

| Artifact | SHA-256 |
|---|---|
| `reviews/team20/root/W-APPLICATION-EDIT-RELEASE.json` | `25987165fcbb1316f9e6db5cac22d1aadc50d27a71bc912feb15e04ec53bdc80` |
| `reviews/team20/root/W-APPLICATION-INTEGRATION.json` | `fd7a6cd140a4dac29845997e26618fcf09fe5028377542bdc67303f1987a1aa7` |
| C17 `reviews/checkpoint17/source-prepare01/SOURCE.json` | `e03d29e54974413ff893f96d9bcf0c70bbf11eeeaf114ac96296987456db1f7f` |

The retained C17 before-source commit is `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49`, tree `d1bd6ca5b18d08ff3ff15896d9a78af9016be799`. No Git/ref verification or complete C18 commit/tree/freeze is established by this report.

## Counts and coverage credit remain unchanged

| Population | Qualified result | Remaining |
|---|---:|---:|
| Original confirmed issues | 19/25 (76%) | 6 |
| All confirmed issues | 26/39 (66.7%) | 13 |
| Original suspicions | 2/12 (16.7%) | 10 |

The eight PVD explanations remain separate from owner decisions; existing UTF-8 -> lowercase ASCII hex password/backup compatibility remains mandatory. Open PVA IDs remain 001, 007, 008, 009, 010, 014, 027, 029, 030, 031, 036, 037 and 039.

The inherited continuation inventory is **199 mixed application-intended/native/investigation/historical-control events + 3 infrastructure + 1 producer = 203 XML elements**. It is not 203 successful application tests. The earlier 51-file recount retains one Main fixture-driver failure and zero error/skipped elements; it was not repeated. C18 integration and GUI03 add **zero executions, zero test methods, zero closures and zero new semantic-LF credit**.

Historical G12 coverage remains 129,167 physical LF, 714 owned members, 97 exclusions and 811 total, with mixed attribution rather than fresh semantic coverage. Its 18 conditions, 12 materializer limits, 35 references and 176 predecessor qualifier elements remain applicable. The missing authoritative `production-readiness-audit/scripts/validate_evidence_ledger.py` remains **NOT_SUPPLIED_NOT_RUN**.

## Consumed outcomes and unresolved status

**GUI03:** independently reconciled consumed failure, not a pending invocation. Preparation143, original stop0, inner/external70; zero task headers/cases/XML and four UNSTARTED. `source_before=true`, `source_after=false`, `cleanup_safe=false`; current-runtime cleanup remains **HOLD / NOT_ATTEMPTED**. Scheduler-only release does not clear that HOLD. The detailed foreign-to-admitted-namespace Java observation identifies no responsible owner; outer kill flags are false, but the inner helper may signal its original wrapper. No blanket no-signalling claim is adopted. Current R, original GUI03 controls/evidence and T/index/config remain frozen pending genuinely new current-R-only closeout. No replay, extra stop or generic old-HOLD cleanup is authorized.

GUI03 independent reconciliation JSON SHA-256: `d94b78c84909e7e2728e4ad4da2eae874814f573a01e005ded088f2d88302830`.

**Detekt03:** retains its independently reconciled consumed failure, 14 qualified clean modules, five failed modules/103 diagnostics, two NO-SOURCE groups and unstarted root analysis of 21 Gradle-script inputs. Zero application/JUnit cases. The coverage guard's PASS is not OpenGrep execution. **AndroidCompile01** remains the consumed precompiler exact decorated-Jar graph omission with zero cases/target compiler actions; independently accepted original cleanup does not validate compile02/new helper work.

**PVA031:** the paired bounded source investigation is accepted as **inconclusive; keep open**. No supported first-populated stale-confirm rejection or justified new patch was established. A bare duplicate sees a synchronously cleared name; page-password IME Done is a real Save producer, not a modal/global route. Direct `onEvent` lacks a navigation-token guard. Supported queued underlying/busy delivery and late input before disposal remain unproved. This is neither universal disproof nor a retention-after-disposal promise, and earns no closure or conclusive-PVU credit.

## Remaining work and preserved limits

1. A genuinely new, independently reviewed/admitted **current-GUI03-R-only closeout**; old HOLDs and consumed controls remain untouched.
2. Fresh Android compile/source binding and **focused root + five-module static qualification** for the applied after-images. Fourteen prior clean modules are reusable only under the exact reviewed source/input reuse contract, not blanket advance credit.
3. The proposed **20 existing regressions = 12 real-Room biometric + 6 credential freshness + 2 draft**, plus Desktop-test compilation. These are neither new declarations nor executed tests; exact selectors, helper review and admission are separate. Do not repeat unaffected pagination/provider/lifecycle/tray successes merely for reassurance.
4. Real Android32 target/ABI/KDF/business/minified/ARM32 evidence; Apple/native/provider/physical obligations; separate PVD/owner decisions; provenance, package, legal and Store gates. Fresh compile02 is not target image authorization or target proof.
5. Helper/source/publication integration and the exact final independent checkpoint review. No clean-tree/ref/publication acceptance, current-R probe, new helper admission or whole-source semantic rereview is included here.

Preserve PVU007 STOP, PVU011 NO RETRY, PVA029's historical **49 checks / 44 PASS / 5 FAIL** without automatic retry, G7/G8 CLOSED, every consumed/HOLD obligation and the earlier native-agent refusal without retry/rephrasing. The original lock is never recreated. Strict dependency verification, protected refs/1017001 and publication restrictions remain unchanged. Rejected v1 proposals, original execution failures and disclosed reviewer/precheck reader errors remain historical evidence, not silently replaced by later accepted material. No blanket suppression/baseline or new owner decision is inferred.

The inherited rough **2-4 engineering weeks** is conditional planning excluding external waits/new findings, not a promised completion date or readiness percentage. This review leaves final overall checkpoint acceptance pending. No active tool/background sessions remain.
