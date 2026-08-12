# Local vault format

Last reviewed: 2026-08-11

This document describes the live local-vault format. It is not a claim that a standalone protobuf or portable
directory format exists.

## Container

The local vault is a Room/SQLite database at the platform-managed application path. Database schema version 3 stores
structural metadata and application-encrypted records. The SQLite file itself is not SQLCipher-encrypted.

The application-level vault format starts at version 1. Saving the first credential with a TOTP authenticator
atomically raises the metadata marker to version 2; it is never lowered. This reader accepts versions 1 and 2. The
TOTP fields remain inside the encrypted credential payload. Room versions 2 and 3 independently add justified
blind-index lookup indexes and attachment object state/version columns through explicit non-destructive migrations.

## Key material

```text
master password bytes + random Argon2 salt
                  -> Argon2id KEK
random 32-byte VEK -> XChaCha20-Poly1305 wrapped with KEK
VEK + purpose/record context -> keyed BLAKE2b subkeys
```

The KEK is never persisted. `vault_metadata` stores bounded Argon2 parameters, the wrapped VEK, and an encrypted
verification record. Unlock publishes the VEK only after both unwrap and verification authentication succeed.

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
