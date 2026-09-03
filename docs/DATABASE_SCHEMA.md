# Database schema

Last reviewed: 2026-09-03

The authoritative Room declaration is `core/database/.../VaultDatabase.kt`. Current database version: **5**.
Schema export is enabled and generated schemas belong under the configured schema directory.

## Tables

| Table | Purpose | Sensitive boundary |
|---|---|---|
| `vault_metadata` | vault/crypto versions, vault ID, Argon2id parameters, wrapped VEK, verification record, counts | salts/parameters are public; `argon2_parallelism` is fixed at `1` because the binding has no lanes parameter; VEK and verification record are authenticated ciphertext |
| `credential_records` | credential type, summary/secret payloads, folder, favorite, timestamps | summary and secret are encrypted separately; identifiers, type, folder, favorite, and timestamps are structural plaintext |
| `folder_records` | hierarchy/order plus folder payload | name is encrypted and has a keyed blind index; identifiers, hierarchy, icon, order, and timestamps are structural plaintext |
| `tag_records` | tag payload and visual color | name is encrypted and has a keyed blind index; identifiers, color, and timestamps are structural plaintext |
| `credential_folder_cross_ref` | compatibility relationship row | identifiers are structural plaintext |
| `credential_tag_cross_ref` | credential-to-tag relationship | identifiers are structural plaintext |
| `password_history_records` | previous credential passwords | password is encrypted; relationship/time are visible |
| `attachment_records` | metadata for independently encrypted attachment objects | filename is encrypted; MIME, size, opaque path reference, IDs, format/state, and time are visible |
| `migration_state` | migration audit state | contains no vault secret by design |
| `current_version_info` | schema-health singleton | contains no vault secret |
| `corruption_logs` | internal corruption/recovery record | must never contain credentials, keys, ciphertext, or raw sensitive exceptions |

Attachment bytes are stored outside Room in app-private, randomly named objects. Each managed object is independently
encrypted and authenticated; the attachment and owning credential identities, format, declared size, MIME type, and
key-derivation context are bound into its authenticated container metadata. Room states coordinate crash recovery
and prevent partially imported objects from becoming visible. Version-1/2 metadata-only attachment rows migrate to
the explicit `LEGACY` state and remain visible as unavailable legacy metadata instead of being destroyed.

## Relationships and indexes

- Attachment and password-history rows cascade when their credential is deleted.
- Credential/tag and credential/folder cross-references use composite primary keys and foreign-key cascades.
- `credential_records.folder_id` references `folder_records.id`; direct folder deletion sets the canonical pointer to
  `NULL`, while the compatibility cross-reference cascades.
- Credential queries index folder, favorite, type, and relevant timestamps.
- Folder hierarchy and ordering, tag visual grouping, and relationship reverse lookups are indexed.
- Version 2 adds indexes for the actual exact-name lookup predicates on `folder_records.name_hash` and
  `tag_records.name_hash`. The values are keyed BLAKE2b blind indexes rather than plaintext names. Migration tests
  demonstrate that these queries change from full table scans in version 1 to the named indexes in later versions.
- Repositories validate folder/tag existence and synchronize `credential_records.folder_id` with the compatibility
  folder cross-reference inside a transaction.

## Encryption representation

Persisted protected values use a versioned XChaCha20-Poly1305 envelope plus associated data that binds purpose and
record identity. Legacy nonce columns remain part of schema version 1; the repository validates the envelope rather
than treating an unauthenticated blob as plaintext. Exact normalized title/folder/tag equality uses deterministic
keyed BLAKE2b blind indexes. Search beyond exact indexed lookup decrypts records only while unlocked.

A Login credential's optional TOTP setup key and parameters are serialized only inside its encrypted secret payload.
Adding this field changed the application vault-format marker to 2 without adding a plaintext column.

## Transactions and migration

Credential payload/relationship/history mutations and Room replacement during restore are transactional. Restore
authenticates and validates input before activation and locks the active session.

The non-destructive migration chain is:

- **1 → 2:** add the two justified folder/tag blind-index lookup indexes; no row is rewritten.
- **2 → 3:** add `content_format_version` and `storage_state` to attachment metadata. Existing rows receive
  `0`/`LEGACY`, preserving their metadata while distinguishing them from managed encrypted objects.
- **3 → 4:** remove the unused credential title blind index while retaining credential ciphertext and every dependent
  relationship row.
- **4 → 5:** add `credential_records.folder_id → folder_records.id ON DELETE SET NULL`; orphaned pointers are healed
  to `NULL`, and the compatibility cross-reference is rebuilt from the canonical column.

Android, iOS, and Desktop database builders register every migration explicitly. Exported schemas 1 through 5 are
kept as test fixtures. Migration tests cover every supported source version through version 5, fresh-schema foreign
key enforcement, query-plan use, dependent-row and ciphertext preservation, orphan repair, and transactional rollback
after injected migration failures. Destructive fallback is not configured.

## Startup integrity and recovery

The application owns one lazy Room instance behind `VaultDatabaseBootstrap`. Existing files receive a read-only,
no-follow `PRAGMA quick_check(1)` before Room opens; a second check runs through Room after schema validation and any
migration. SQLite corruption/not-a-database results are distinct from permission, storage, and migration failures.

Corruption never triggers `fallbackToDestructiveMigration`, `VACUUM`, or automatic repair. Before Room has opened a
damaged bundle, the user can explicitly move the database, `-wal`/`-shm`/`-journal` sidecars, and encrypted attachment
directory into app-private recovery storage, then create a fresh vault and restore a verified `.pvault` backup. Moves
are rolled back on failure and the main database moves last as the commit marker. On restart, a recovery directory
without that marker is rolled back before SQLite can open; orphan sidecars also block fresh-database creation. Bounded enum-only health events live
outside the database because a damaged database is not a reliable or safe diagnostic sink; `corruption_logs` remains
DAO-less and must not receive raw exceptions.
