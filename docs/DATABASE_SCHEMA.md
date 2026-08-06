# Database schema

Last reviewed: 2026-08-03

The authoritative Room declaration is `core/database/.../VaultDatabase.kt`. Current database version: **1**.
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
| `attachment_records` | reserved attachment metadata | filename is encrypted; MIME, size, path reference, IDs, and time are visible |
| `migration_state` | migration audit state | contains no vault secret by design |
| `current_version_info` | schema-health singleton | contains no vault secret |
| `corruption_logs` | internal corruption/recovery record | must never contain credentials, keys, ciphertext, or raw sensitive exceptions |

Attachment rows do not imply attachment-file support. This application neither creates nor opens attachment files;
the table is retained in version 1 so existing metadata and backups can be preserved.

## Relationships and indexes

- Attachment and password-history rows cascade when their credential is deleted.
- Credential/tag and credential/folder cross-references use composite primary keys and foreign-key cascades.
- Credential queries index blind title, folder, favorite, type, and relevant timestamps.
- Folder hierarchy and ordering, tag visual grouping, and relationship reverse lookups are indexed.
- Repositories validate folder/tag existence and synchronize `credential_records.folder_id` with the compatibility
  folder cross-reference inside a transaction.

## Encryption representation

Persisted protected values use a versioned XChaCha20-Poly1305 envelope plus associated data that binds purpose and
record identity. Legacy nonce columns remain part of schema version 1; the repository validates the envelope rather
than treating an unauthenticated blob as plaintext. Exact normalized title/folder/tag equality uses deterministic
keyed BLAKE2b blind indexes. Search beyond exact indexed lookup decrypts records only while unlocked.

A Login credential's optional TOTP setup key and parameters are serialized only inside its encrypted secret payload.
Adding this field changes the application vault-format marker to 2 but does not add a plaintext column or increment
the Room schema version.

## Transactions and migration

Credential payload/relationship/history mutations and full backup replacement are transactional. Restore validates
and decrypts the entire snapshot before the replacement transaction and locks the active session.

There is no schema migration yet because this checkout has no prior released schema fixture. Before incrementing
version 1, add Room migrations and tests using an actual exported prior schema; destructive fallback is prohibited.
