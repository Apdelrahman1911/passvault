# Database migration test matrix

## Required fixtures

Preserve an exported schema and a representative database fixture for every supported production version. Fixtures must use synthetic data only and cover nulls/defaults, maximum lengths/counts, encrypted values, relationships, and rows affected by the migration.

| Path | Assertions |
|---|---|
| Fresh current install | Schema equals current exported artifact. |
| Each prior version -> current | All data and constraints preserved. |
| Sequential migrations | Every registered edge executes. |
| Direct/open-to-current path | Builder finds a valid path; no destructive fallback. |
| Injected migration failure | Transaction rolls back and original DB remains readable by its old schema. |
| Corrupt/unexpected schema | Fails closed without deleting user data. |
| Prior backup -> current restore | Format and schema compatibility remain deliberate. |

## Index proof

Capture the representative DAO SQL and run `EXPLAIN QUERY PLAN` before and after. Record data cardinality and timing across warm/cold runs. Require the plan to use the intended index for the real predicate/order; remove redundant indexes that only add write/storage cost.

For sensitive equality lookup, assess whether the index exposes plaintext, frequency, ordering, or correlation. Prefer a keyed blind index where equality is required and leakage is accepted by the threat model. Rotate or rebuild it through an authenticated migration if the key changes.

## Schema equivalence

After migrating a fixture, compare tables, columns, affinities, nullability, defaults, primary keys, foreign keys, indexes, triggers, and Room identity metadata with a fresh current database. Row data comparisons alone cannot detect a structurally incomplete migration.

## Rollback boundary

SQLite transactions protect database changes, not filesystem blobs or OS key stores. When a migration touches external state, use staged versioned objects and recovery markers; do not claim cross-resource atomicity.
