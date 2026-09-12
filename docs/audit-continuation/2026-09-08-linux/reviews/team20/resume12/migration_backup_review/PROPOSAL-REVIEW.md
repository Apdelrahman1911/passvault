# Historical-layout-origin backup: independent proposal review

Reviewer `/root/c20_cleanup_review`; 2026-09-12 UTC.

**SOURCE_ONLY_ACCEPTED_FOR_ROOT_INTEGRATION.** One meaningful new regression
declaration, no product defect/fix or executed test. Root alone may apply the
exact after-image after rechecking the preimage, bind it into a current source
identity, and admit the focused execution with retention and cleanup. This is
not build/test admission, runtime success or closure of historical-producer gaps.

`B=/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux`.
`A=B/reviews/team20/resume12/cleanup_author/historical_backup`.
The sole prospective permanent path is
`core/database/src/desktopTest/kotlin/com/passvault/core/database/backup/VaultBackupUnicodePaginationTest.kt`.

| Exact object | Bytes | SHA256 |
| --- | ---: | --- |
| A/before/(permanent path) | 27452 | acc3c5a8d29f2f976ed5dfd5612b325624915953937ff48b438ad6e5bcb63554 |
| A/after/(permanent path) | 39219 | a136ec3fe0ca70b469f8b6ecf2555da65f1a480d2d39a43d21255175f96b944d |
| A/PROPOSED.patch | 14013 | 1b29964b6d19245589a42bad27beee20f1d3ab723434728bc8ffbe2eb28ad8a0 |
| A/PLAN.md | 4675 | 8107b5503a4b4f4bbfb5f15929b166deb7a7047992ff995c0a5b9bfba79db091 |
| A/SOURCE-RECEIPT.json | 2291 | 651d88215d14284b0ab316669ff13d90a5e7a6a0ca5c29c1e0e2766178a0b017 |

## Independent scope and why this is not redundant

Read the supplied migration/backup skills, relevant production boundaries and
existing test fixture, plus the prior paired migration matrix/reuse reviews.
Their historical successes and source-generation limits remain authoritative.
Existing real schema1–4 migration fixtures use ciphertext byte sentinels; they
do not demonstrate usable authenticated encrypted records through backup/restore.
Existing current-schema pagination/streaming tests exercise real crypto/Room
and logical compatibility, but do not establish a historical physical-layout
origin. No repeat of the all-version migration loop, prior six pagination methods
or39 streaming cases is justified merely by this addition.

Independently verified bounded, stable, leaf-no-follow reads of the exact packet
and current permanent preimage. An independently generated full unified text
delta exactly equals the supplied patch; no original source line is removed.
Six existing test bodies and original helpers remain unchanged. Additions are
imports, one documented class-size suppression, one test and five private helpers;
source grows534→732 physical LF. No production/schema/dependency/version/build
wiring is modified. No candidate was imported, compiled or run.

Selected surrounding source identities (review was focused, not whole-file or
transitive recertification):

| Source under W | SHA256 |
| --- | --- |
| core/database/schemas/com.passvault.core.database.VaultDatabase/1.json | 76985152ec6593ee7eb13343bf30e86b1fd7beacbb27e89c4a2cfcb286eeb586 |
| core/database/src/commonMain/kotlin/com/passvault/core/database/VaultMigrations.kt | e1e16717e2cc178824955a96971aa6fbe02d2bb70a704f56b8acec68221346b6 |
| core/database/src/commonMain/kotlin/com/passvault/core/database/dao/VaultBackupDao.kt | 51813c6a5f9ff5a4ba542e5bb5043b2747066dfc68d6d3c41054de82da2e8281 |
| core/database/src/commonMain/kotlin/com/passvault/core/database/backup/VaultBackupService.kt | 68d3e276432489d2c4efb4b854cd203a5f0f3f380565fe292c7e3b49d9be2c12 |
| core/database/src/commonMain/kotlin/com/passvault/core/database/backup/VaultBackupV2Service.kt | aee2b621484150fe3807cec66d0a3ecd5eeff3eaf907099b839b084520a86bf4 |
| core/database/build.gradle.kts | e83ca3a79cafd99f25d9d5a34c4db15f30ba1784a2e7613cfb44d9cbd7ffdbbc |

## Reachability, guards and attempted counterexamples

- Producer uses the existing real file-backed Room fixture, DesktopCryptoEngine,
  repositories and production key hierarchy. Seeded credentials, folders, tags
  and history must decrypt successfully before the producer locks/closes. This
  is not mock ciphertext, a fresh empty vault or a pre-migrated origin relabeled1.
- The next file is created from genuine exported schema1 DDL/indices/setup,
  including its Room identity and user_version. Only seven fixed selected
  tables are copied from the closed owned seed through a parameter-bound ATTACH.
  Independently compared schema1/5 selected column names, affinities and
  nullability: the only removed source field is credential title_hash. The test
  gives that deprecated field zero32 explicitly; it is not claimed to be a
  historical blind-index producer. Production3→4 drops it. Live encrypted
  payloads/nonces, wrapped key/salt/KDF values and folder/tag hashes are copied.
  Schema resource indices are either arrays or absent, matching the new parser.
- Real version1, exported identity, title_hash presence and FK validity are
  asserted before Room opens the origin. `addVaultMigrations()` is unchanged
  and registers the full1→2→3→4→5 chain. No shortcut, destructive fallback,
  modified migration or arbitrary old application/helper is introduced.
- Origin opens through production migration, unlocks using persisted metadata
  and decrypts the graph before public V2 export. Content-aware entity equality
  checks migration preservation: metadata and entity equals implementations
  actually compare byte contents, not ByteArray identities. Canonical sorting
  ignores result order; only intentional access time and derived entry count
  are normalized. The final persisted entry count is asserted independently.
- The bounded256KiB encrypted backup is written CREATE_NEW, then read again
  after origin closure. Public backup/restore still executes real codec, KDF,
  authentication and Room replacement. Existing19-byte fragmented memory
  transport adapters do not fake those results. This deliberately does not
  claim platform-provider streaming or a hostile concurrent-file mutation test.
- A distinct-password destination vault is created/unlocked, with a sentinel
  credential. Successful restore must replace that state, close its input and
  remove the sentinel. Destination session/Room then close. A new repository
  must unlock solely using restored metadata and the original source master
  password; success cannot rely on a retained source or destination session key.
  Final secret/title, folder/tag/history assertions and the full selected raw
  entity snapshot reject missing/partial rows, altered ciphertext/nonces,
  relabeled IDs, changed keys and relationship loss in this fixture.
- Every new staged cleanup is installed before using its constructed resource,
  runs NonCancellable, locks sessions before Room close and retains primary
  errors with cleanup failures suppressed. Final deletion reuses the existing
  owned-temp-root guard and no-FOLLOW_LINKS tree walk. No source/permanent report,
  shared cache/toolchain or arbitrary external path is a fixture cleanup target.
  No extra per-run process/service is introduced by this source proposal.

The first reviewer JSON-field comparison diagnostic raised `KeyError: notNull`
because exported nullable fields omit that false property. The corrected
source-data comparison accounted for omitted=false and matched all seven tables.
This was an administrative read-only diagnostic, not a Kotlin/application test
failure, candidate execution or a reason to change the correct candidate parser.

## Exact remaining verification and limits

New method:
`schema one encrypted records survive current backup restore and a fresh reopen`.
Root should select only this method in the existing Desktop test task and batch
it with already-needed database verification if admitted. Expected credit is
**one** JUnit testcase, not seven table checks, multiple opens or task counts.
Compilation/static analysis alone will not establish this persistence/crypto
contract. Current resource floors and independent execution/retention/cleanup
admission still apply; no invocation or automatic retry follows this review.

This is current production-generated synthetic crypto in historical Room1
layout, migrating to5 and using current V2 backup. It does not establish old
application binary/backup provenance, every historical crypto/container/metadata
combination, all predecessor origins, attachment content, hardware/provider
behavior, actual process death, fsync/power-loss durability or physical erasure.
No legacy attachment boundary is redesigned; there are intentionally no
attachments. Old qualified results remain qualified, not fresh C20 executions.

The skills' omitted scripts remain unavailable/dormant and were not substituted.
Activity was source/evidence reading, bounded text/JSON/hash comparison and this
new review. No Git, build/test, runtime/proc/SDK/network probe, old-helper import,
cleanup or product edit. All HOLD/STOP/NO-RETRY/CLOSED/native-refusal/protected-ref/
owner-design/build1017001 boundaries persist. Zero cases or closures are added.
