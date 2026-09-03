# Local vault format

Last reviewed: 2026-09-03

This document describes the live local-vault format. It is not a claim that a standalone protobuf or portable
directory format exists.

## Container

The local vault is a Room/SQLite database at the platform-managed application path. Database schema version 5 stores
structural metadata and application-encrypted records. The SQLite file itself is not SQLCipher-encrypted.

An existing database must pass a bounded, read-only SQLite `quick_check` before Room opens and a second check after
Room validates or migrates it. A failed check does not change the file. With explicit confirmation, PassVault moves
the unopened database bundle and encrypted attachment directory into protected recovery storage before creating a
new empty vault. Those preserved files are salvage material, not a supported backup container; portable recovery uses
a fully authenticated `.pvault` backup.

The application-level vault format starts at version 1. Saving the first credential with a TOTP authenticator
atomically raises the metadata marker to version 2; it is never lowered. This reader accepts versions 1 and 2. The
TOTP fields remain inside the encrypted credential payload. Room versions 2 and 3 add justified blind-index lookup
indexes and attachment object state/version columns. Version 4 removes the unused credential title index; version 5
enforces the credential-to-folder pointer and repairs legacy orphans. Every change uses an explicit non-destructive
migration.

## Key material

```text
master password characters -> strict UTF-8 -> lowercase ASCII hex bytes
lowercase hex bytes + random Argon2 salt -> Argon2id KEK
random 32-byte VEK -> XChaCha20-Poly1305 wrapped with KEK
VEK + purpose/record context -> keyed BLAKE2b subkeys
```

The KEK is never persisted. `vault_metadata` stores bounded Argon2 parameters, the wrapped VEK, and an encrypted
verification record. Lowercase hexadecimal encoding is a historical, compatibility-critical part of the KDF input;
changing it to raw UTF-8 would change every KEK. The UTF-8 and hexadecimal buffers are mutable and cleared
best-effort after use. Unlock publishes the VEK only after both unwrap and verification authentication succeed.

### Argon2id parameters

| Parameter | Accepted value | Format behavior |
|---|---:|---|
| operations | 2–10 | Stored per vault and passed to the KDF. |
| memory | 32–256 MiB | Stored per vault and passed to the KDF. |
| parallelism | `1` | Stored for format compatibility and rejected on unlock if different. |

The current libsodium `crypto_pwhash` binding exposes operations and memory but no lanes/parallelism argument.
PassVault therefore writes and requires `1`; persisting another value would claim KDF work the binding cannot perform.

Vault creation and each master-password change benchmark libsodium's 2-operation/64 MiB interactive probe once. A
result below 50 ms selects the 4-operation/64 MiB profile; every slower result and any benchmark failure selects three
operations at 64 MiB. The wider accepted ranges above are reader safety/compatibility bounds, not writer targets.
Memory remains fixed deliberately: CPU timing does not prove that a mobile process can safely allocate 256 MiB, and
no supported low-memory Android/iOS device matrix or product unlock-latency budget justifies that availability risk.
A future memory tier needs those measurements; changing the backup profile additionally requires a new backup format
version.

## Record envelopes

Current protected fields use crypto envelope version 2:

- XChaCha20-Poly1305 authenticated encryption;
- a fresh random 24-byte nonce per encryption;
- purpose- and record-bound associated data;
- strict version/length parsing; and
- failure on wrong key, modified ciphertext, nonce, or associated data.

Credential summary and secret payloads are separate to allow list rendering without decrypting passwords and notes.
Vault format 2 may include one TOTP setup key and its issuer, account label, algorithm, digits, and period in the
encrypted secret payload of a Login credential. Generated codes and countdown state are never persisted.
Folder/tag payloads, password history, and attachment filenames have separate key contexts. Keyed blind indexes are
deterministic and reveal equality for the same normalized value within one vault; they are not plaintext hashes.

## Visible metadata

Database theft can reveal row counts, identifiers, credential types, favorites, timestamps, relationships, folder
hierarchy IDs, visual colors/icons, and attachment MIME/size/opaque-path metadata. It must not reveal credential
titles, usernames, URLs, passwords, notes, custom-field values, tag/folder names, history passwords, or attachment
filenames or TOTP setup keys from protected payloads.

## Backup compatibility

The portable format is the separately encrypted `.pvault` container documented in
[BACKUP_FORMAT.md](BACKUP_FORMAT.md). Copying the live database is not a supported backup protocol. Attachment file
bytes live as independently authenticated encrypted app-private objects outside Room and are included by format-2
backups.
