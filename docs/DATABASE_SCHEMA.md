# Database schema

Last reviewed: 2026-08-11

The authoritative Room declaration is `core/database/.../VaultDatabase.kt`. Current database version: **3**.
Schema export is enabled and generated schemas belong under the configured schema directory.

## Tables

| Table | Purpose | Sensitive boundary |
|---|---|---|
| `vault_metadata` | vault/crypto versions, vault ID, Argon2id parameters, wrapped VEK, verification record, counts | salts/parameters are public; VEK and verification record are authenticated ciphertext |
| `credential_records` | credential type, blind title index, summary/secret payloads, folder, favorite, timestamps | summary and secret are encrypted separately |
| `folder_records` | hierarchy/order plus folder payload | name is represented by a keyed blind index; payload is encrypted |
| `tag_records` | tag payload and visual color | name uses a keyed blind index; payload is encrypted |
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
- Credential queries index blind title, folder, favorite, type, and relevant timestamps.
- Folder hierarchy and ordering, tag visual grouping, and relationship reverse lookups are indexed.
- Version 2 adds indexes for the actual exact-name lookup predicates on `folder_records.name_hash` and
  `tag_records.name_hash`. The values are keyed BLAKE2b blind indexes rather than plaintext names. Migration tests
  demonstrate that these queries change from full table scans in version 1 to the named indexes in version 2/3.
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

Android, iOS, and Desktop database builders register both migrations explicitly. Exported schemas 1, 2, and 3 are
kept as test fixtures. Migration tests cover 1 → 3, 2 → 3, a fresh version-3 schema, query-plan use, and transactional
rollback after an injected migration failure. Destructive fallback is not configured.
