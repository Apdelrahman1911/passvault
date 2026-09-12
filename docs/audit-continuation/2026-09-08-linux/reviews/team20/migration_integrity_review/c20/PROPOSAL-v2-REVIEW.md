# C20 independent review — one post-validation migration rollback regression

Reviewer: `/root/migration_integrity_review`  
Author: `/root/migration_integrity_author`  
Disposition: **ACCEPT_SOURCE_ONLY_BOUNDED_TEST_ADDITION**.

No product defect is established. One meaningful missing regression is proposed;
no runtime, compilation, static-analysis or release gate is passed by this review.
Root alone may apply and separately admit validation. All existing HOLD,
STOP/NO-RETRY/CLOSED/native-refusal and publication/version/identity/dependency/
schema5/build1017001/owner-design restrictions remain unchanged.

## Exact accepted bytes

Target:
`core/database/src/desktopTest/kotlin/com/passvault/core/database/CredentialFolderForeignKeyMigrationTest.kt`.

| Object | Bytes | SHA256 |
|---|---:|---|
| W before-image | 15919 | `d3ade5f151b1653d32de802d1c777b5337ec35e430b1943661b2c94bf8fa196a` |
| Author `c20/after/<target>` | 22952 | `5d80abe70b5667a4c874e65c6b61e56f80625260bbeae8002b3ba0eb3c5de40c` |
| Author `c20/PROPOSED.patch` | 8272 | `c08390088ba109b505735b8287b39428286a341b427cd3215b676683c2c6cce0` |

Author artifact root:
`docs/audit-continuation/2026-09-08-linux/reviews/team20/migration_integrity_author/`.

The reviewer read the full existing class, current migration implementation,
bootstrap/storage implementation, exported-schema metadata, build/resource wiring
and all three platform builders, then read both proposal diffs and the relevant
after-image ranges. A standard-library, **data-only in-memory** reconstruction
confirmed all four v2 unified-diff hunks match W without fuzz and produce the
exact accepted after-image. Nothing was applied/executed/imported by that check.
The two existing test bodies are byte-identical; declaration count is two→three.

The reviewer initially compared the v1 patch text with a `difflib`-generated
diff. Its false textual-equality result came from equivalent hunk placement,
not an invalid patch or product failure. Exact v2 reconstruction is the relevant
mechanical source check. A separate historical-report data query also had a
`TypeError` from treating a count as a list; no project body ran and the corrected
query used the explicit delivery file list. Neither diagnostic is a test case.

## What the new method actually exercises

New method:
`Room validation failure rolls back the full upgrade and a clean reopen persists it`.
Expected ordinary desktop framework decoration is `[desktop]`; final root
selection/result mapping remains separate.

1. Build an isolated, synthetic schema1 SQLite database using the existing,
   exported-schema fixture and credential graph.
2. Open real Room with production 1→2, 2→3, 3→4, then a final migration that
   executes production 4→5 and deliberately drops one required current index.
3. Let **Room's post-migration validator** reject the resulting schema. The hook
   executes valid SQL; it does not manufacture an exception or bypass the driver.
4. Close failed Room and compare the complete fixture semantic snapshot against
   the old snapshot: version, schema DDL, Room master identity and all populated
   application table columns/rows.
5. Open with the unmodified production migration registration, require both
   credentials and expected folder/orphan outcomes, close, and check version5
   and foreign-key integrity.
6. Construct another real Room instance, read the retained credential bytes,
   close and compare the resulting committed semantic snapshot again.

This is a new **post-validation / whole-chain rollback and later-open** boundary,
not another copy of historical schema1–4 success or migration-body SQL-error
tests. Those existing successes remain reused under `REUSE-AND-GAP-REVIEW.md`.

## Counterexamples and false-positive controls

- Failure during fixture creation or an earlier migration cannot satisfy the
  test: the marker is set only after the successful final required-index DROP.
- Arbitrary setup/SQL errors cannot pass the negative oracle: it requires
  `IllegalStateException` plus Room's `Migration didn't properly handle` diagnostic
  and `credential_records`. A future diagnostic change would need deliberate
  test review; an unrelated failure must not be relabeled a pass.
- Room creation alone is not mistaken for migration: actual DAO access forces
  database open. The control then uses the unchanged `addVaultMigrations()` path.
- The rollback snapshot records row multiplicity and ordered per-cell values
  using `typeof` and hex-encoded cast bytes. It distinguishes NULL, empty TEXT,
  empty BLOB and nonempty data for the seeded INTEGER/TEXT/BLOB/NULL fixture.
  All tables have deterministic finite columns; no fake DAO or fake SQLite is
  substituted.
- The old orphan pointer and both old folder cross-reference rows occur in the
  before/after rollback snapshot, so a partial final-step normalization cannot
  be accepted as a rollback.
- The test does not demand raw SQLite file-byte equality after legitimate Room
  open/journal bookkeeping. Schema DDL/version/master identity and seeded values
  are the intended preservation boundary, not page locations or WAL headers.
- `requireNotNull` guards both successful-upgrade credentials. A nullable folder
  assertion alone therefore cannot silently accept row loss.
- The committed→second-open snapshot proves persistence of the **resulting
  committed state**. It is not by itself an exhaustive original→current field
  transformation oracle. Historical graph success is reused separately; do not
  advertise new exhaustive migration equivalence from this one method.

## Cleanup review and revision

The reviewer challenged v1's possibility of losing a primary assertion to a
secondary close/delete exception. V2 adds a private inline cleanup wrapper only
for the new method's directory and three Room handles. It rethrows primary
failure/cancellation, attaches distinct cleanup failures as suppressed, and
still fails on cleanup-only errors. Existing methods/helpers were not refactored.

Directory creation is a unique `Files.createTempDirectory` fixture. Removal
requires its expected prefix, non-symlink root, canonical default JVM temp parent,
and four fixed regular-file leaves (`vault.db`, `-wal`, `-shm`, `-journal`) with
no-follow checks. It never walks recursively; unexpected residual entries refuse
the final directory deletion rather than expanding authority. These are ordinary
cooperative synthetic-fixture controls, not hostile same-user filesystem custody
or proof of actual runtime cleanup. Root's independent invocation/worker/retention/
cleanup admission remains necessary.

## Compatibility and remaining verification

- Test-only edit; no migration, entity, schema artifact, version, builder,
  dependency, encryption, backup format, key derivation or product-policy change.
- Original two FK migration methods and historical result accounting unchanged.
- Exactly **one new declared method; zero executions**. No full-class rerun is
  required merely to recreate the already-supported two old results.
- This uses artificial ciphertext-shaped BLOB fixtures, not a real-user vault,
  historical application-produced encrypted backup or proof of successful
  cryptographic decryption after every upgrade.
- No power loss/process death/fsync guarantee, provider/Android/iOS parity,
  wrong-current-identity rejection, maximum-size fixture, general REAL-value
  serialization or exhaustive fresh/migrated schema equivalence is established.
- Compilation and this exact selected method's observed result remain pending.
  Source acceptance grants neither run admission nor automatic retry after a
  failure. The existing diagnostic and static qualifications stay explicit.
