# C20 migration / fault / persistence integrity — bounded author disposition

**INDEPENDENTLY SOURCE ACCEPTED / NO AUTHOR APPLICATION OR EXECUTION. One new test
declaration proposed; no product fix, new finding, family closure or readiness
claim.** Parent-supplied C19 is
`3b2130f` / `603ed0fb`; this lane did not use Git to refresh it.

Author: `/root/migration_integrity_author`; independent reviewer:
`/root/migration_integrity_review`. All writes are confined to this directory.
Root alone owns source application, build/test/CI/Git and runtime cleanup.

Final read-only observation found W already at the accepted after-image
`5d80abe70b5667a4c874e65c6b61e56f80625260bbeae8002b3ba0eb3c5de40c`.
The author did not apply it. **Do not reapply the patch.** Root's integration,
admission and any later runtime results remain separate receipts. A read-command
heading incorrectly described this check as "still at before image"; the actual
returned hash is the accepted after-image, not the before-image or unknown drift.

Read the handoff, current permissions/assembly, W instructions, supplied migration
and secure-backup skills/references, C19 and relevant current ledger entries.
Skill helper commands remain dormant. No helper, build, test, application,
native/provider probe, network request or cleanup ran in this lane. This is not
another whole-project audit or a substitute for the existing issue ledger.

## 1. Reuse before adding work

The paired review resolves two existing historical PASS gates, **six methods
each**, to their exact result/XML blobs and the historical source manifest:

- `storage-fresh-migrated6-02`: result SHA256
  `4353cd23b70ae4ce688143c7ac1b35f13be527c4ee2166303ff18644c402eef0`.
- `storage-bootstrap-recovery6-02`: result SHA256
  `4699dc902234c1cfd810f97756a6d00fe9fa99fe303053ede84d6a1243968d5d`.
- Both run source-manifest blobs: SHA256
  `54d82172e3ef3ed73695ff5418c1308973307262d47eac022c3e182951822d34`;
  distinct recorded canonical source identity:
  `3cd8973f34dc87edc11057879d5fca7182d3b16a6c8f14496cad3d488fe7669a`.
- Seventeen bounded current-before-image files match the historical entries:
  schemas1–5, migrations/database/bootstrap, all three platform builders, the
  relevant five test files and module build wiring. Full descriptors and exact
  qualifications are in
  `../../migration_integrity_review/c20/REUSE-AND-GAP-REVIEW.md`.

These are **old events**, not twelve newly run tests and not additions to C19's
215 mixed XML-element accounting. Exact source equality is bounded, not a new
transitive runtime/platform guarantee. The supplemental review below also
resolves older selected evidence; unselected methods are not globally unexecuted.

All names below carry the retained framework suffix `[desktop]`.

| Class | Retained selected passing method(s) | Reuse scope |
|---|---|---|
| `CredentialFolderForeignKeyMigrationTest` | `every shipped schema migrates to version five without losing the credential graph`; `failed version five migration restores the intact version four graph` | Actual exported schema1–4 opened through production migrations; selected graph/ciphertext and canonical folder behavior; real4→5 SQL-error rollback. The four-version loop is **one method**, not four tests. |
| `CredentialFolderForeignKeyTest` | `fresh schema declares and enforces the credential folder foreign key`; `direct folder deletion nulls the canonical pointer without deleting ciphertext` | Current FK enforcement and SET NULL without credential ciphertext loss. |
| `VaultMigrationTest` | `fresh version five schema contains only justified blind indexes`; `version one data survives the folder and tag index migration` | Fresh5 and real1→5 with representative folder/tag blind-index query plans. This later XML selected two; all six have separately qualified older evidence below. |
| `VaultDatabaseBootstrapTest` | `damaged leaf is detected before Room and preserved byte for byte`; `migration failure stays unavailable and rolls back without corruption recovery`; `fresh and existing databases pass both health gates` | Real pre-open physical corruption preservation; migration SQL failure cannot authorize reset; fresh/reopen health gates. Not all eleven current declarations. |
| `VaultDatabaseRecoveryStorageTest` | `failed preservation rolls every moved source back into place`; `startup restores an interrupted preservation before inspecting SQLite`; `missing main file with an orphaned sidecar cannot become a fresh database` | Compensation, synthetic interrupted preservation and orphan-sidecar refusal. This later XML selected three; all five have separately qualified older evidence below. `Error` injection is not real process death. |

### Additional historical reuse — not a manufactured rerun backlog

The exact supplementary note is
`../../migration_integrity_review/c20/SUPPLEMENTAL-HISTORICAL-CLASS-REUSE.md`,
SHA256 `8ee3ace8923f1bb82a275caf282168390ca46ec5ceffd9cec2dea02019a9fb54`.
It hash-verifies selected raw XML and retained source/delivery provenance from
the original `database-full-desktop-resumed` run:

- **All six `VaultMigrationTest` methods passed.** This includes the v2 legacy
  defaults/quota control, v3 dependent/title-hash success, v1 migration-body
  failure and v3 rebuild failure that the later migration6 selection omitted.
  The test, migrations and schema files are unchanged from the retained baseline
  delivery and match the later source manifest. Reuse is through that retained
  provenance, **not a new baseline Git comparison or current Linux execution**.
- **All five `VaultDatabaseRecoveryStorageTest` methods passed.** The test is
  unchanged. PVA004 changed the containing production file only above the local
  storage implementation; thus its two diagnostics controls have bounded
  source-impact-qualified reuse, not whole-file baseline equality. The three
  move/recovery controls additionally have the exact-current-file later evidence.
- The baseline's **eight bootstrap methods passed**, but the implementation and
  test were modified for PVA004. Preserve those observations without promoting
  them to a current eleven-case pass. The later `storage-bootstrap-handoff-02`
  XML/source manifest exactly binds four additional current guard methods:
  second-gate damage, Room-claim revocation, failed-factory revocation and Room
  exclusion during preservation. Together with the table's three distinct
  bootstrap methods there are **seven exact-current-file historical method
  observations**, not a fresh seven-case batch. The remaining four names have
  baseline observations with a source-generation limit, not zero-execution claims.

The supplementary note gives every exact name/hash and the changed-source
qualification. No existing method is added to a repeat queue merely because a
later run selected fewer methods. Neither old result overlap nor source
declarations add anything to current execution counters.

Linux03's **166 regression methods + one separate producer** are also existing
evidence, not a requested replay. The105 selected database methods were
freshness/repository/streaming/pagination tests, **not these historical migration
classes**. Its39 `VaultBackupStreamingTest` methods reuse real Room/crypto/blob
I/O for wrong-password/tamper/truncation, authenticated source-switch rollback,
staging/capacity/output faults, source ownership and pre/post-commit cancellation.
Its six Unicode/pagination integrations also reuse finite logical-V1 compatibility.
The actual result review is
`../../../verification/LINUX-ISOLATED-BATCH03-ACTUAL-RESULT-REVIEW.json`, SHA256
`f428020cf798125507c514c8b4d632dc58ab4f401494a29b0eaeb5001799df45`.

Those executions bind C4, not a newly run C19/C20 tree. C18's independently
accepted SQLite comparator extraction and fixture-only changes retain their
source/static qualifications; they do not justify ceremonial reruns or a new
runtime-pass label. Relevant evidence is the C18 integration entry in
`VERIFICATION_LEDGER.json` and `reviews/team20/database_review/ACCEPTANCE.json`.

## 2. Physical database migration obligations

Room database schema, vault crypto format, backup container version and backup
metadata schema are different version spaces. Current Room is5; the supported
physical predecessors are1–4. All platform builders register only explicit
1→2→3→4→5 edges. There is no direct shortcut migration or destructive fallback
to add/test, and no schema/dependency/version change is proposed.

| Obligation | Established/reusable | Exact remaining qualification or action |
|---|---|---|
| Each1/2/3/4→5 open path, including chained routing | Existing real-Room four-version fixture and matching production migration source. | **REUSE.** Do not author another all-version success loop. Mobile/Apple execution is separate from builder-source equality. |
| 1→2 blind indexes | Existing representative before/after plans and preserved folder/tag rows. | **REUSE.** No new index is proposed. A representative benchmark or every DAO query plan is not supplied by this fixture and is not a prerequisite to the new fault test. |
| 2→3 attachment columns/defaults | Current schema validation and real1/2→5 graph fixture; existing permanent `version two legacy attachment metadata survives version three migration` explicitly asserts0/LEGACY, path and quota semantics. Its older PASS is source-compatible under the supplement. | **REUSE.** Do not rerun solely because the later two-method `VaultMigrationTest` XML omitted it. |
| 3→4 removal of unused title hash and dependent rebuild | Existing1–4 loop forces generated Room schema validation and preserves selected dependent bytes/relationships. Permanent v3 success and v3 rollback have source-compatible older PASS evidence. | **REUSE**, preserving the supplement's provenance limits. No duplicate SQL-error test or original-method replay is needed solely for ceremony. |
| 4→5 canonical folder integrity | Existing all-version graph, FK enforcement/deletion and4→5 real SQL-error rollback. | **REUSE.** Dangling canonical pointers deliberately become NULL and folder cross-refs are rebuilt from that pointer; preserving stale cross-refs would be the wrong oracle. |
| Entire multi-edge transaction rejected by final Room validation, then usable on a clean new open | Existing tests fail within individual migration edges, not after a completed1→5 chain is rejected by generated Room validation. | **NEW MINIMAL PROPOSAL:** exactly one method in the existing FK migration fixture; details below. Zero executions. |
| Full schema equivalence / all-field success preservation | Existing Room migration-time validator checks generated structure; selected row/FK/ciphertext assertions and exported artifacts are meaningful evidence. | Not an exhaustive comparison of every seeded/non-seeded field, trigger, identity and fresh exported5 object. The new rollback snapshot must not be relabeled as exhaustive successful-transform coverage. |
| Wrong/corrupt database rejection | Existing real physical corruption/bootstrap refusal. New proposal specifically covers a valid-SQL, wrong **post-migration** schema. | Current-version wrong-identity, unsupported-version/downgrade and arbitrary altered-DDL-with-matching-identity combinations are not proved by this new method. No product defect or automatic extra matrix is inferred. |

The canonical folder correction is not a promise to retain every old redundant
relationship unchanged. The fixture's BLOB values are finite synthetic byte
sentinels, **not authenticated encrypted credentials that can be unlocked**.

## 3. Backup / fault / recovery / persistence residuals

| Risk | Reuse / actual boundary | Remaining obligation, owner and prerequisites |
|---|---|---|
| Ordinary logical backup compatibility | Existing legacy title-hash compatibility source, Linux03 logical-V1 roundtrip and streaming metadata-schema2 attachment compatibility. Current binary codec supports metadata1–3; these are not Room schemas1–3. | Reuse exact successful cases. Do not infer every format/metadata/physical-schema combination or a prior application producer. |
| A backup produced from an actual supported historical database layout restores into5 | Fresh5 plus logical-V1 mutation/restore does not prove historical database-origin compatibility. The migration graph's BLOB sentinels cannot stand in for valid backup ciphertext. | Still requires a separately source-reviewed, synthetic historical-layout/format fixture with valid encrypted metadata and documented producer provenance, followed by real production restore/unlock/data checks. Root owns admission; no old app/candidate rebuild, real backup or unsupported format reconstruction is authorized. No fixture is invented in this patch. |
| Restore/import rollback, source mutation, storage/provider-style faults and cancellation | Reuse39 Linux03 streaming cases at their real Room/crypto/synthetic filesystem boundaries, plus exact historical attachment evidence when source-bound. | No duplicate streaming suite. Synthetic provider callbacks are not Android/iOS provider behavior or a guarantee of atomic external destinations. |
| Preservation compensation and interrupted preservation recovery | Reuse the three historical recovery cases and bootstrap physical-corruption gate. | Real process death, fsync/power loss, provider faults and every move boundary remain distinct. An injected `Error` is not crash durability. |
| WAL/terminal persistence | Existing bootstrap WAL bundle, missing clean-close sidecars and terminal checkpoint have baseline PASS observations with the supplement's PVA004 changed-source limit. New proposal targets failed-upgrade recovery and closed/reopened resulting state. | Preserve those observations; require source-impact/claim-specific review, not an automatic repeat backlog. Neither same-JVM reopen nor an in-memory restore is file-backed process-restart/power-loss proof. |
| Blob orphan/delete cleanup across restart | Permanent `AttachmentRepositoryTest` has staged-object recovery and metadata-first deletion/cleanup-failure controls using real local objects. | Reuse source-bound historical outcomes; its recovery call with an injected orphan in a live fixture is not an actual killed/restarted process. Any stronger file-backed Room + real-blob restart test is a separate bounded follow-on, not completion inferred from mocks. |
| Peak memory / scale | Existing finite/boundary tests and streaming structure are retained. | Actual platform memory profiling, every maximum-size/historical schema fixture and every external provider remain separate. No new storage architecture, plaintext staging or larger synthetic data is proposed. |

This bounded selection intentionally implements only the high-value uncovered
chain/validation/reopen case. Remaining larger provenance/platform/durability
requirements are explicit, not silently closed or expanded into another audit.
There is no independently supported real product correction from this lane.

## 4. Exact one-method proposal

Permanent file ownership is exactly
`core/database/src/desktopTest/kotlin/com/passvault/core/database/CredentialFolderForeignKeyMigrationTest.kt`.
The original two methods and original helpers are unchanged. `OWNERSHIP.json`
pins the exact15919-byte before-image,22952-byte after-image and8272-byte unified
patch. No build wiring, schema, version, dependency, migration or production path
changes. Existing `desktopTest` schema-resource wiring is reused.

New method:
`Room validation failure rolls back the full upgrade and a clean reopen persists it`.

Independent source acceptance:
`../../migration_integrity_review/c20/PROPOSAL-v2-REVIEW.md`, SHA256
`d44dd62e03274dd06c7e1d5b6a254873f48c6dd801ea5566af0c89a94b9321eb`.
The following are proposed assertions, **not observed runtime results**.

1. Build the existing synthetic graph from exported schema1, including both
   credential rows, ciphertext/nonce sentinels, folder/tag cross-refs, attachment
   and history. Capture version, Room identity (as a row), SQLite schema DDL and
   every column of every seeded application row.
2. Execute production1→2,2→3,3→4,4→5, then **test-only valid SQL** drops one
   required current index. A marker after the DROP proves this point was reached.
3. Force actual Room open via DAO. Require `IllegalStateException` and Room's
   `Migration didn't properly handle` diagnostic naming `credential_records`.
   A fixture failure, unknown exception or failure before the injection is not
   a passing oracle. The diagnostic contract is pending actual runtime evidence.
4. Close failed Room, then compare the full original finite semantic snapshot.
   This detects committed earlier indexes/columns/rebuilds, lost old folder
   cross-refs or changed bytes. Do **not** require raw file-byte equality across
   legitimate SQLite journal/header bookkeeping.
5. Reopen with unmodified production migrations, assert both credential rows and
   canonical-folder outcomes, close, assert schema5 and no FK violations, then
   reopen/close again and compare the resulting committed snapshot. The final
   comparison is intended to establish persistence of that resulting state, not independently all
   original→current value transformations; reuse the existing success matrix.

Cells use SQLite `typeof` plus hex-encoded BLOB-cast contents and retain duplicates
with deterministic ordering. The finite fixture uses INTEGER/TEXT/BLOB/NULL;
this is not a generalized bit-preserving REAL-value serializer. No fake DAO,
mock-only transaction or simulated `throw` replaces Room validation. This is
test-owned corruption of a synthetic migration, not a finding about production.

New-method cleanup is limited to its freshly created, non-symlink temp root and
four exact SQLite filenames; unknown entries refuse directory deletion. New
Room-close and directory-cleanup failures preserve a primary failure, adding
distinct secondary failures as suppressed. The reviewer-challenged v1 is
retained in `versions/v1/`; v2 adds only this cleanup-failure preservation.
No cleanup was executed while authoring this proposal.

## 5. Admission and unchanged boundaries

Prospective task: `:core:database:desktopTest`, **only the one named method** in
`com.passvault.core.database.CredentialFolderForeignKeyMigrationTest`.
Expected result is one exact XML testcase with the frozen `[desktop]` suffix,
not a replay of the original two methods or the166 Linux03 regressions. This is
a selection proposal, **not a command or current execution admission**.

Root must first verify exact patch/preimage ownership, adopt the independently
accepted bytes, and bind a genuinely quiet host, exclusive owner, source/runner/task
and new isolated HOME/TMP/evidence/cleanup admission. Use existing JDK17/wrapper,
one worker, non-daemon/no configure-on-demand, intact dependency verification and
serial affected static checks. Preserve exact command, exit, all expected method
results, original stop and owned-settlement/allowlisted cleanup evidence. Pending
current static qualification is not erased by this author patch.

Neither held runtime access/deletion nor helper/collector/stop replay follows.
PVU007 STOP, PVU011 NO RETRY, PVA02949/44PASS/5FAIL, G7/G8 CLOSED, native refusal,
all HOLDs, protected refs, PVD/target requirements and occupied build1017001 stay
unchanged. Counts stay C19's19/25 original and26/39 total qualified closures.
