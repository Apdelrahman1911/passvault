# PassVault security model

Last reviewed: 2026-07-28

## Scope and assumptions

PassVault protects a local vault against offline database/backup theft and accidental exposure while the
application is locked. It assumes the operating system, process, UI input path, and libsodium implementation are not
fully compromised while the vault is unlocked. Malware with process-memory, accessibility, keyboard, or clipboard
access can defeat application-layer protections.

There is no server, account, network sync, telemetry, or analytics boundary in this repository.

## Key hierarchy

```text
master password bytes + 16-byte random salt
                 |
              Argon2id
                 |
                KEK
                 |
 XChaCha20-Poly1305, AAD "VEK_WRAP"
                 |
        random 32-byte VEK in memory
                 |
  keyed BLAKE2b, length-prefixed contexts
                 |
 record/folder/tag/history/blind-index subkeys
```

The master password is converted to a byte array only for derivation and wiped best-effort afterward. The KEK is
not persisted. Room stores the salt, bounded Argon2 parameters, wrapped VEK, nonces, and an authenticated
verification record. Unlock validates metadata bounds before doing expensive cryptographic work, derives the KEK,
unwraps the VEK, authenticates the verification record, and only then publishes the unlocked session.

Changing the master password rewraps the same VEK, so record data does not need re-encryption. Vault session
transitions are serialized. Lock immediately removes and wipes the repository-owned VEK buffer best-effort.

## Persisted data boundary

Credential summary and secret payloads, folder/tag payloads, passwords in history, and attachment filenames use
XChaCha20-Poly1305 with unique random nonces and record-specific associated data. Keyed deterministic blind indexes
support exact normalized title/folder/tag comparisons without plaintext values.

The database intentionally exposes structural metadata needed for queries: record identifiers, credential type,
favorite state, timestamps, folder/tag relationship identifiers, attachment MIME/size/path metadata, and row
counts. Search results are produced from decrypted in-memory records while unlocked; there is no plaintext search
index on disk.

Attachment file bytes are not implemented. The reserved attachment schema is not evidence of encrypted file
storage.

## Backup boundary

`.pvault` version 1 uses a separate backup password, fresh salt, Argon2id, and XChaCha20-Poly1305 with AAD
`passvault:backup:v1`. Strict size/version/parameter/entity/referential checks run before preview or restore. Restore
locks the vault and replaces all vault tables in one Room transaction. Attachment metadata may be retained but file
bytes are never packaged. See [`BACKUP_FORMAT.md`](BACKUP_FORMAT.md).

## Session and platform controls

- Failed unlock attempts use progressive delays.
- Manual, inactivity, and application-background signals request a central vault lock.
- Android enables screenshot blocking for sensitive content and uses the Storage Access Framework for backup files.
- Clipboard expiration verifies a random ownership token/value before clearing, so newer unrelated clipboard data is
  preserved.
- Desktop platform-keyring lookup fails closed; it never substitutes a plaintext preference file.
- No biometric UI or platform adapter is present. Master-password unlock is the only shipped authentication path.

## Error and memory policy

User-facing paths do not expose SQL, parser, file path, ciphertext, or cryptographic exception text. Cancellation is
re-thrown rather than converted into failure. Mutable sensitive byte arrays are wiped in `finally` blocks where
practical. Managed strings, UI state snapshots, garbage-collected copies, swap/pagefile behavior, and OS crash dumps
cannot be guaranteed controllable by common Kotlin code.

## Validated threats

Automated tests verify wrong-key and tampered-ciphertext rejection, nonce uniqueness, locked repository access,
encrypted raw database rows, backup wrong-password/corruption/transaction behavior, rapid state events, and
lock-during-edit cleanup. This is useful regression evidence, not an independent security certification.

## Residual risks and release dependencies

- A compromised unlocked endpoint can read displayed/decrypted values.
- Structural database metadata remains observable.
- No schema-upgrade migration can be tested until an actual earlier release fixture exists.
- Android device lifecycle and Desktop graphical/keyring behavior need platform smoke tests.
- A biometric feature must not ship until an OS-protected key operation gates VEK unwrap and enrollment changes fail
  safely.
- Publisher signing, notarization, update security, disclosure contacts, and external review are outside this
  checkout.
