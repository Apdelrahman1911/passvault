# Selected corrections and verification

This integration takes selected completed corrections, not the continuation's
entire tree/history. The 73 selected source/dependency files include the unchanged
existing `scripts/lib/macos-keychain.sh`; the commit changes 72 product/test/tooling
files plus six durable documentation files, not 73 code changes.
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
