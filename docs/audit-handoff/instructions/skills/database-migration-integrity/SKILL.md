---
name: database-migration-integrity
description: Design, implement, inspect, or verify non-destructive Room and SQLite schema migrations, indexes, encrypted or blind lookup columns, foreign keys, exported schemas, fresh installs, rollback behavior, and upgrade paths from every supported production version. Use whenever entities, columns, indexes, relations, database versions, or backup-compatible persistence change.
---

# Database Migration Integrity

Prove that every supported existing database upgrades without data loss and that the new schema solves a measured need.

## Inspection

Inventory:

- current and supported prior schema versions;
- exported schema artifacts and build configuration;
- every platform database builder and registered migration;
- destructive fallback or implicit auto-migration behavior;
- DAO query predicates, sort orders, joins, and actual query plans;
- foreign keys, cascades, uniqueness, defaults, nullability, and indexes;
- encrypted fields, blind indexes, and privacy implications;
- backup/restore schema assumptions.

Run `scripts/report_room_schemas.py` against the exported schema directory to expose missing versions, duplicate versions, and identity-hash inconsistencies.

## Remediation workflow

1. Capture the unchanged production schema fixture before editing.
2. Bump the schema version once for the coherent migration batch.
3. Write explicit migrations for each supported transition; add direct upgrade coverage when builders or test infrastructure can skip intermediate versions.
4. Preserve rows and ciphertext. Never use destructive fallback for an existing user database.
5. Add indexes only for real predicates and demonstrate plan improvement with `EXPLAIN QUERY PLAN`.
6. For sensitive equality lookups, prefer keyed blind indexes or another reviewed privacy-preserving representation; do not add plaintext search columns by convenience.
7. Export the new schema and update diagrams/contracts that claim the old shape.
8. Confirm backup/restore understands both migrated and fresh schemas.

## Required tests

- Upgrade from every supported production version to current.
- Chained and direct upgrade paths where applicable.
- Fresh current-version installation.
- Migrated schema equivalence with the exported fresh schema.
- Row count, primary/foreign key, ciphertext, null/default, and relationship preservation.
- Query-plan use of every justified new index plus a representative benchmark.
- Injected migration failure with transaction rollback and unchanged prior data.
- Wrong/corrupt schema rejection without destructive recovery.
- Backup from prior schema restored through the documented compatibility path.

## Failure lessons

- Adding an index is still a schema migration.
- A successful fresh install says nothing about existing users.
- Declaring an index does not prove the DAO query uses it.
- Auto/destructive migration can hide missing data-preservation logic.
- Plaintext indexes can silently weaken an encrypted local data model.
- Exported schema artifacts are release inputs, not disposable generated files.

## Resources

- Run [`scripts/report_room_schemas.py`](scripts/report_room_schemas.py) before and after migration work.
- Read [`references/migration-test-matrix.md`](references/migration-test-matrix.md) for the required fixture and rollback matrix.

