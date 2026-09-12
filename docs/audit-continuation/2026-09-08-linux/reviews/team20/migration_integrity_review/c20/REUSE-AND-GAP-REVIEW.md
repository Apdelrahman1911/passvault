# C20 independent migration / persistence review — retained-evidence reuse

Reviewer: `/root/migration_integrity_review`  
Paired author: `/root/migration_integrity_author`  
Status: **bounded historical reuse accepted; exact v2 proposal source-accepted**.
See `PROPOSAL-v2-REVIEW.md`; its one new method remains unexecuted.

This is not a new test execution, finding, family closure, build admission or
release-readiness claim. Root alone owns build/test/CI/Git/cleanup. No application
or helper was imported/executed. Reads stayed within W source and retained
handoff evidence; packed report/XML data was decoded in memory with bounded
standard-library reads and exact selected-blob hash checks, never extracted to
runtime paths. No held runtime, sibling, private input or stopped procedure was
opened. One initial `rg` display of the one-line evidence index was oversized
and truncated; subsequent index queries selected JSON descriptors before output.
That display is not an execution result or evidence of a product error.

## Instructions and current context

Read `AUDIT_HANDOFF.md`, handoff `START_HERE.md`, `PERMISSIONS.md`, `ASSEMBLY.md`,
`EVIDENCE.md`, publication README, W `AGENTS.md`, supplied
`database-migration-integrity` and `secure-backup-and-blob-storage` skills, the
migration test-matrix reference, C19 report and relevant current ledger entries.
The parent supplied C19 `3b2130f` / tree `603ed0fb`; no Git operation independently
refreshed those identifiers here.

All STOP / NO-RETRY / CLOSED / native-refusal / HOLD boundaries survive. Nothing
here changes protected refs, build1017001, versions, dependencies, identities,
platform/provider requirements or owner design decisions.

## Existing evidence must be reused, not reinvented

The handoff historical summary was previously independently adopted. This review
also read the exact two compact result reports and five XML blobs below. They
are original historical events, **not additions to C19's XML accounting**.

All original paths below are relative to
`remediation-reports/20260905T222925Z/` in `docs/audit-handoff/evidence-index.json`.
They are locators, not executable/current runtime paths.

| Historical run | Exact retained result | Qualified outcome |
|---|---|---|
| `storage-fresh-migrated6-02` | `verification/results-storage-fresh-migrated6-02.json`, SHA256 `4353cd23b70ae4ce688143c7ac1b35f13be527c4ee2166303ff18644c402eef0` | Six passing host Room/SQLite methods in three XMLs; one method loops schema1–4. |
| `storage-bootstrap-recovery6-02` | `verification/results-storage-bootstrap-recovery6-02.json`, SHA256 `4699dc902234c1cfd810f97756a6d00fe9fa99fe303053ede84d6a1243968d5d` | Six passing synthetic bootstrap/recovery methods in two XMLs. |

XML prefix:
`evidence/runs/<run>/artifacts/worktree/core/database/build/test-results/desktopTest/`.

| Run | XML suffix | SHA256 | Methods |
|---|---|---|---:|
| migration6 | `TEST-com.passvault.core.database.CredentialFolderForeignKeyMigrationTest.xml` | `05aaea419f54183d8a3804e57fedc793edda8dd5dda407fd399a4d031439b688` | 2 |
| migration6 | `TEST-com.passvault.core.database.CredentialFolderForeignKeyTest.xml` | `fca7b2b9bb69b5d584f7202efae92bd0881bf7681524b7abe2ffd31ea06af2dc` | 2 |
| migration6 | `TEST-com.passvault.core.database.VaultMigrationTest.xml` | `435ee59cfec9fa6feae61a63c2ea374665dc90b833409803163ea09c38584b07` | 2 |
| recovery6 | `TEST-com.passvault.core.database.VaultDatabaseBootstrapTest.xml` | `4a750f4a3da198a89c219d59709ece85c553ca06f43dd69ecde313d05443ec3b` | 3 |
| recovery6 | `TEST-com.passvault.core.database.VaultDatabaseRecoveryStorageTest.xml` | `e81de6b641055d0c510f8c7be342aa3a6a3ccba1f9f6cb54749156e1aa50ef1c` | 3 |

Each listed XML has zero failures/errors/skips. The two migration-class cases
are `every shipped schema migrates to version five without losing the credential
graph[desktop]` and `failed version five migration restores the intact version
four graph[desktop]`. The loop is **one method**, not four extra methods.

The historical summary
`reviews/historical-verification-summary-independent-storage-v1/REPORT.md`
(SHA256 `3203641c46cc682aca73826fbc93cf530d16528390a6c4c3b996a4153f18fa42`)
classifies both overall gates PASS but explicitly disclaims exhaustive
field-preservation/schema equivalence, provider evidence, real process death,
fsync/power loss and all-platform qualification. Its selected result catalog
(`RESULT-CATALOG.json`, SHA256
`2c05abde5fce3a9f507dc47f4a1f354c8ce4c3481dd8645483a6b0ac955d27ff`)
and both original compact reports agree on these methods and qualifications.
This review does not replay the historical runner, command logs or journals.

## Source binding

Both run-specific `source-manifest.json` locators resolve to the same retained
blob: 166447 bytes, SHA256
`54d82172e3ef3ed73695ff5418c1308973307262d47eac022c3e182951822d34`.
The recorded run canonical source identity is separately
`3cd8973f34dc87edc11057879d5fca7182d3b16a6c8f14496cad3d488fe7669a`.
These different digests identify different representations, not a mismatch.

Seventeen relevant W files were byte-hashed and exactly match their historical
manifest entries. Paths below are relative to `core/database/`:

| Path | SHA256 |
|---|---|
| `build.gradle.kts` | `e83ca3a79cafd99f25d9d5a34c4db15f30ba1784a2e7613cfb44d9cbd7ffdbbc` |
| `schemas/com.passvault.core.database.VaultDatabase/1.json` | `76985152ec6593ee7eb13343bf30e86b1fd7beacbb27e89c4a2cfcb286eeb586` |
| `schemas/com.passvault.core.database.VaultDatabase/2.json` | `3c09e59f2ab2b59a2535c82a404b9af27b9e3934f88e4161c1a9478cdef38d94` |
| `schemas/com.passvault.core.database.VaultDatabase/3.json` | `080ac957b9dcdb58bc4950cb3e1f6a8279a0432e2a3ea580ba3aa0c84add9136` |
| `schemas/com.passvault.core.database.VaultDatabase/4.json` | `9dc4f187784ca4a409b258debab1fbf9ad8a700778e060c44b98d9d4d8d14224` |
| `schemas/com.passvault.core.database.VaultDatabase/5.json` | `431ed8a2dbfcde5c132f45a2e4443894d45aa0010bf9b6616780f62be3cd354b` |
| `src/androidMain/kotlin/com/passvault/core/database/Database.android.kt` | `29cb20792690ce8947458e81cac3e47b789d247de0b09ea2265b5506aa23eb67` |
| `src/commonMain/kotlin/com/passvault/core/database/VaultDatabase.kt` | `c727909e0b8e765ea56e8badec5ccea0d579967cabe1eb84ad57bbb2c79972d6` |
| `src/commonMain/kotlin/com/passvault/core/database/VaultDatabaseBootstrap.kt` | `5726afb24093afa8c56c57321d64f0ea747db69910f0b7aa984e9d18a86f2598` |
| `src/commonMain/kotlin/com/passvault/core/database/VaultMigrations.kt` | `e1e16717e2cc178824955a96971aa6fbe02d2bb70a704f56b8acec68221346b6` |
| `src/desktopMain/kotlin/com/passvault/core/database/Database.desktop.kt` | `2aa4d34c71de4dae66a79e6d93af187a44642e4a504ff2a592d82c60245f39a7` |
| `src/desktopTest/kotlin/com/passvault/core/database/CredentialFolderForeignKeyMigrationTest.kt` | `d3ade5f151b1653d32de802d1c777b5337ec35e430b1943661b2c94bf8fa196a` |
| `src/desktopTest/kotlin/com/passvault/core/database/CredentialFolderForeignKeyTest.kt` | `9457ae13947fc8c61ab2861e2460a4f3e91ef94bc9932827d5bc0e9b545bf240` |
| `src/desktopTest/kotlin/com/passvault/core/database/VaultDatabaseBootstrapTest.kt` | `5cfb78294ea63f66b9fb07f7b694f69afd6d7e0a30907cbc8bb47774b472f2b4` |
| `src/desktopTest/kotlin/com/passvault/core/database/VaultDatabaseRecoveryStorageTest.kt` | `46c1e4acc0619987cc78b75c924a10710b9d5b00cbcf1c2991fc64a77399c06e` |
| `src/desktopTest/kotlin/com/passvault/core/database/VaultMigrationTest.kt` | `6c8e1438ba5db4b634c770cd26218ff96b400272dd3399896a06ea503be78f33` |
| `src/iosMain/kotlin/com/passvault/core/database/Database.ios.kt` | `60c14e6b6d1436246d11653c93116dc9f3567e318044ff140c6cad4059573d6f` |

This is a bounded file binding, not a complete transitive/runtime identity or
fresh C20 Linux execution. Platform-builder equality is not device execution.

## Independent source challenge and coverage distinctions

- All three production builders call shared `addVaultMigrations()`, registering
  exactly 1→2, 2→3, 3→4, 4→5 with no destructive fallback. The exported artifacts
  remain versions1–5, eleven entities each; schema5 stays version5.
- Existing FK migration fixture reconstructs genuine exported prior schemas,
  sets their identity and `user_version`, enables foreign keys, seeds encrypted
  fields and dependencies, and forces real Room opening via DAO access. It
  checks both non-null credentials, summary/secret bytes, tag/attachment/history,
  deliberate orphan-folder normalization, current FK and foreign-key integrity.
  This is not a logical backup/fresh-schema substitute.
- Existing 4→5 rollback invokes the production migration then causes an actual
  SQLite statement error. It verifies old version, old FK shape, orphan pointer,
  selected credential bytes and dependent counts. Existing `VaultMigrationTest`
  also declares failure-at1→2 and failure-at3→4 controls and v2 attachment/default
  coverage; those additional methods are not credited as executions in the
  selected historical migration6 XMLs.
- Existing bootstrap covers real structural corruption, fresh open, migration
  SQL-error→Unavailable with preservation denied, and Room-lifetime ownership
  fences. Recovery storage covers move compensation and an `Error`-simulated
  interrupted move; it is not an OS process-crash/power-loss proof.
- Linux03's successfully retained166 focused methods include current-schema
  Unicode pagination/logical backup controls. Reuse them in their own scope;
  they do **not** establish a historical schema1–4 upgrade or a backup produced
  by a historical application build.
- Row survival plus Room's migration-time schema validator is useful, but is
  not a newly observed exhaustive all-field/all-table/trigger/current-export
  equivalence check. No new index or benchmark is being introduced here, so
  unrelated performance work is not a prerequisite to this bounded review.

## Minimal possible remaining validation, not confirmed defects

A bounded search in the relevant database desktop tests/shared source found no
test for well-formed SQLite rejected for a mismatched Room identity/unsupported
schema, or post-migration-validation rejection at the end of the whole1→5 chain
followed by a successful new production open. These are evidence gaps, not
observed bad product behavior or authority to patch migration policy.

Challenges sent to the author:

1. Do not duplicate existing schema1–4 success and per-edge SQL-error tests.
2. Prefer one discriminating real Room validation failure at the end of1→5 if
   extending coverage: execute production migrations, make only a test-owned
   required-index omission, let generated Room validation reject, prove the
   injection was reached and old semantic state remains, then reopen through
   unmodified production migrations. An intentionally wrong migration is not
   evidence that production currently performs that mutation.
3. Reject broad `assertFails` success when fixture/preflight failure could be
   responsible. Bind the real open/validation path and verify normal controls.
4. Compare semantic schema/identity/version plus all seeded bytes and metadata
   after closing failed Room. Journal mode/WAL/header bookkeeping can legitimately
   change raw file bytes; such a difference alone is not data loss. Attachment
   sentinel bytes and absence of destructive recovery remain direct assertions.
5. A wrong stored identity is distinct from structurally altered schema carrying
   a matching trusted identity. Do not claim arbitrary schema-corruption
   rejection from the narrower identity case.

Root must separately review/admit any application-test invocation. No operation
is scheduled by this note, and no passed historical method needs a blind replay.
