# Selected corrections and verification

The original selective commit `beb131add14a53ad13d9b7ebb30a5ab4738adec2` takes
completed corrections, not the continuation's entire tree/history. Its73 selected
source/dependency files include unchanged `scripts/lib/macos-keychain.sh`; that
commit changes72 product/test/tooling files plus six durable documentation files,
not73 code changes. The later12-file CI/resource-safety follow-up is separate.
No version, application identity, dependency version, schema,
release tag, occupied build, or Store state is changed by the selection.

| Area | Selected corrections | Deliberately not included |
|---|---|---|
| Password scoring | PVA-002 numeric-sequence scoring | Android32 KDF PVA-001 |
| Database / recovery / backup | PVA-003/004/005/006/011/012/013: batching, produced-result and stream ownership, startup recovery, attachment compensation/framing; permanent tests | Open platform attachment operations |
| Biometric / Unicode / test oracle | PVA-033/034/035/038: session freshness, discriminating oracle, SQLite UTF ordering, preserving enrollment after cold-provider failure | New native/provider claims |
| Editor | PVA-026 narrow equal-update SensitiveText ownership repair and two regression cases | PVA-007/031 unfinished editor API/UI changes |
| Desktop | PVA-005 retrieve ownership (existing ABI retained); PVA-039 returning from Compose loop into Main cleanup; permanent tests | PVA-010/014 native lifetime/prompt changes and other open Desktop work |
| Release automation | PVA-015–025/028/032 completed validation/promotion/cleanup guards with required fixtures/tooling | PVA-029 attestation replacement and failing runner; unfinished Android policy suite |

The additional `BackupDatabaseTextOrder.kt` dependency is included. The existing
Desktop prompt-coordinator regression remains in its baseline test file, in a
separate test class to satisfy Detekt; its body and assertions are unchanged. The legacy attestation gate is
retained: excluding an unfinished replacement does not close PVA-029.

## Actual checks

- **107 release regression cases PASS**, 2026-09-15, 23.82 seconds, including
  all 20 iOS-profile cleanup scenarios once. Source-bound to the selective
  integration before documentation additions; independent actual-result review
  accepted. Synthetic CLI/provider/workflow boundaries, not live signing,
  GitHub attestations, Store upload, hardware, or Apple-provider proof.
- The Ruby default and explicitly selected parser modes both used Ruby 3.2.3 /
  JSON 2.6.3. They are not evidence of two different JSON-library versions.
- **184 JUnit cases PASS** in 17 XML suites, zero failures/errors/skips, across
  four targeted tasks: domain 4, database 163, credential 2, Desktop 15.
  This includes actual Room migrations/rollback, backup/Unicode/ciphertext and
  ownership/cancellation behavior. Injected biometric failures are not provider
  hardware evidence. The four opt-in cold-provider methods and real Compose
  process-boundary method were outside this run, not counted as executed/skipped.
- The same batch's aggregate command **FAILED**: domain/database/credential
  Detekt passed, but Desktop Detekt reported exactly one `TooManyFunctions`
  issue (12 public tests versus the limit of 11). The unchanged baseline prompt
  method was moved to its own class in the same file; no product/test-body change.
  Targeted corrected Desktop Detekt: **PASS**, 2m13s, checkstyle XML has zero
  findings; original-wrapper stop and owned cleanup also passed. Do not erase the original failure.
- All selected tests and affected Desktop graph compiled in the original batch.
  The later mechanical test-class relocation is subject to separate source/static
  review, not claimed as freshly re-executed JUnit evidence.
- Protected CI and GitHub approval: **PENDING**. Independent selective-source
  review is recorded separately in the evidence bundle; it is not a GitHub approval.

The release-regression invocation retained exact source hashes and stdout/stderr, observed the
original process group settled, and removed its empty owned private HOME/TMP.
No Gradle process was started by that release-regression invocation. Historical evidence remains at the
exact continuation commit linked in README; source-affinity checks must be stated
per reused result. No blanket inheritance of the continuation's closure totals. In particular,
PVA-038 does not close the retained `PVA038_IOS_NATIVE_AEAD` mobile native gate.

PowerShell is stored as LF Git blobs with the repository's existing CRLF checkout
attributes. Blob equality must not be reported as raw historical checkout-byte
equality. No permanent tests, licenses, migration exports or required release
fixtures are removed as documentation clutter.

The original Gradle invocation took 9m47s (controller including wrapper acquisition
and cleanup approximately 10m). Its read-only dependency-cache path was one level
too deep, so Gradle used the isolated writable cache with strict verification.
That was an efficiency warning, not a dependency-integrity bypass. The original
wrapper stop exited0, owned processes settled, source hashes remained unchanged,
and validated generated roots/private cache were removed. The corrected static
check uses the proper shared read-only cache root without deleting that cache.

## Protected-merge CI preparation

New hosted-CI cleanup has six synthetic process/output safety cases, actually
passed on Linux after review (including success/failure exit status, descendant
settlement, bounded owned termination, tracked/symlink cleanup refusal, compact
report retention and non-hosted admission refusal). This is not Windows execution
or a proof of cleanup under forced runner loss. Target-host checks precede Desktop
batches; original normal CI coverage remains required.

The full release-automation shell suite exposed stale source assertions during
integration: old PENTEST wording, the pre-batching Android step/private-temp path,
and the retired best-effort tester-secret deletion call. Assertions were aligned
with the truthful scope and hardened implementations; no product or security gate
was removed. Initial failed invocations remain recorded. One invocation observed
its process group still present immediately on exit and did not retain the PGID;
its empty private directories remain UNKNOWN/HOLD. Later bounded runs do not
retroactively close that observation. The subsequent run settled its groups and
removed its private roots; its six guard cases and eight entitlement cases passed,
but the whole shell command failed on the stale deletion predicate. No whole-suite
success is inferred from these partial results. Protected CI must run the corrected
full suite; prior107 release cases remain separately source-qualified above.

The corrected379-line source-validation suffix then passed separately in0.97s;
its process group settled and private directories were removed. Its inherited
`Release automation tests passed.` footer refers only to that extracted suffix,
not a fresh full-suite pass. Source-affinity review verified the already-executed
prefix was unchanged apart from a line-number-only failure diagnostic. Do not
repeat that prefix locally for reassurance; the normal CI runs the whole suite.

## First protected CI result and reviewed parser pin

Run34941908484 passed wrapper/dependency verification, then correctly failed the
security-analysis gate because the changed `VaultBackupV2Service.kt` no longer
matched its previously reviewed partial-parser hash. It reported0 findings over
553files/11rules, with the same five existing partial-parser limitations; this is
not complete parsed-source clearance. No Gradle unit/platform batch followed.
Both executed batches reported cleanupPASS; dependency wrapper stop returned0.

The full912-line backup service and all11security rules received independent
review against the selected source and retained real backup/Unicode/pagination
regressions. Only that existing file pin is refreshed to
`aee2b621484150fe3807cec66d0a3ecd5eeff3eaf907099b839b084520a86bf4`.
No rule, exclusion, parser-limited file list, other hash or coverage floor changes.
The service itself is unchanged by this pin refresh. The corrected source still
requires normal CI; the failed run is not relabelled successful.

Actual first-run checkout was GitHub-generated merge
`53acb726339c57eca6abe66be914374fb5de063b`, not its earlier preliminary merge SHA.
The executed commit was read back and has the reviewed tree
`312791149ba33ebd9d67aaf67819ab075e1f0b84` and exact main/integration parents.

## CI02 integration evidence and validation-guard correction

On source `4fc32944df1672dfb502b1e80671c500b1a067bd`, actual PR checkout
`87efe8c82b7633cac6adc006063ed204acc285c2` has tree
`d2da382909d5e2d26a36271de597ab7976c7529c`. Wrapper/dependency verification,
security analysis, unit tests and Detekt passed. The198JUnitXML files report
1604passed executions and5expected skips, not1609executed or unique tests.
Four dedicated cold-provider methods and one real-Compose opt-in were skipped.
Five partial-parser source limitations remain; hardware/beta gates are unchanged.

The shell suite exited0, but independent log review caught its missing `rg` command:
the pilot production-path guard was skipped because an `if` swallowed exit127.
Current pilot source contains neither forbidden token, but that is source evidence,
not an executed guard. The guard now uses already-required Ruby outside a conditional,
with five synthetic regressions covering allowed input, both prohibited tokens,
missing workflow input and a missing Ruby executable. No dependency was added.
The five new cases passed in an isolated Linux run (0.57s), with Bash/Ruby syntax
checks, unchanged source hashes and settled workers/private cleanup. This was a
focused subset, not a rerun of the original107cases or the entire shell suite.

CI02 was deliberately cancelled as superseded before downstream platform completion;
Android cancellation retained cleanupPASS/original-wrapper-stop0, alongside the four
completed Linux batch cleanupPASS receipts. Its CI Gate did not pass. Completed
unit/static evidence remains qualified; no Android/Desktop/iOS completion is claimed.
The corrected source still requires normal protected CI and independent GitHub approval.

## CI03 Windows launcher failure and parallel CI correction

Run34946387329 on `a3132bb` passed dependency/unit/static, Android validation and
Linux Desktop jobs. Windows failed two of six synthetic guard cases before Gradle:
native Python resolved bare `bash` to the Windows WSL launcher rather than Git Bash.
The owner requested cancellation and parallel hosted jobs. The cancelled macOS
batch stopped its wrapper but retained cleanupHOLD for a remaining private-home
process; hosted VM disposal is not a positive cleanup receipt. Six other completed
batch cleanup receipts passed. No Windows/macOS completion is inferred.

The shell now passes its actual Bash path explicitly, converted to native format
onWindows; missing/invalid Windows configuration fails closed without PATH fallback.
JobObject assignment-before-start, wrapper-stop and per-runner limits stay intact.
Independent hosted jobs now fan out after dependency preflight; the all-job gate
still requires success. The first local attempt hit a Bash diagnostic quoting error
before Python (no tests ran); its empty private directories remain HOLD. After the
diagnostic-only correction, all eight synthetic guard cases passed onLinux (1.639s),
as did Bash syntax and the existing workflow-security validator, with source hashes
unchanged and owned groups settled/private directories removed. Real Windows path,
JobObject and build verification still require the new hosted run.
