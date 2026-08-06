# PassVault security model

Last reviewed: 2026-08-03

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

Mobile biometric unlock is an optional second route to the same VEK. It never stores the master password or KEK:

```text
Face ID / Touch ID / strong Android biometric
                    |
       OS-protected key operation
                    |
                  VEK
                    |
 authenticated vault verification record
                    |
              vault session
```

Android encrypts the VEK with an auth-per-use AES-GCM key in Android Keystore and requires a strong biometric for
every decrypt. The key is invalidated when biometric enrollment changes. iOS stores the VEK in a
`WhenPasscodeSetThisDeviceOnly` Keychain item using `biometryCurrentSet`, so it is neither synchronized nor restored
to another device and becomes inaccessible after enrollment changes. In both cases the released candidate VEK must
authenticate the existing vault verification record before the repository publishes a session. The master password
remains the recovery and fallback path.

## Persisted data boundary

Credential summary and secret payloads, TOTP setup keys and parameters, folder/tag payloads, passwords in history,
and attachment filenames use XChaCha20-Poly1305 with unique random nonces and record-specific associated data. Keyed
deterministic blind indexes support exact normalized title/folder/tag comparisons without plaintext values.

TOTP codes are derived in memory from the encrypted setup key and authoritative device time. Codes and countdowns
are not written to Room or backups. QR payloads are handled locally, parsed with strict size/type/Base32/parameter
bounds, and never sent to a service. Android and iOS camera capture and Desktop-selected image decoding remain OS
and process trust boundaries.

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
- Copying a TOTP code uses the same ownership-aware clipboard path as other credential values.
- Desktop platform-keyring lookup fails closed; it never substitutes a plaintext preference file.
- Android and iOS expose explicit biometric enrollment in Security settings and a biometric action beside the
  password field. Unsupported platforms and devices fail closed to master-password unlock.

## Error and memory policy

User-facing paths do not expose SQL, parser, file path, ciphertext, or cryptographic exception text. Cancellation is
re-thrown rather than converted into failure. Mutable sensitive byte arrays are wiped in `finally` blocks where
practical. Managed strings, UI state snapshots, garbage-collected copies, swap/pagefile behavior, and OS crash dumps
cannot be guaranteed controllable by common Kotlin code.

## Validated threats

Automated tests verify wrong-key and biometric-candidate rejection, tampered-ciphertext rejection, nonce uniqueness,
locked repository access,
encrypted raw database rows, backup wrong-password/corruption/transaction behavior, rapid state events, and
lock-during-edit cleanup. RFC 6238 vectors cover all supported hash algorithms, and repository tests verify that TOTP
setup keys are encrypted and survive format-2 backup restore. This is useful regression evidence, not an independent
security certification.

## Residual risks and release dependencies

- A compromised unlocked endpoint can read displayed/decrypted values.
- Incorrect device time produces invalid TOTP codes; PassVault does not synchronize the clock.
- Structural database metadata remains observable.
- No schema-upgrade migration can be tested until an actual earlier release fixture exists.
- Android device lifecycle and Desktop graphical/keyring behavior need platform smoke tests.
- Face ID/Touch ID and Android biometric prompts, cancellation, lockout, process recreation, and enrollment-change
  invalidation still require physical-device smoke tests; compilation and common/repository tests cannot prove OS UI
  behavior.
- Publisher signing, notarization, update security, disclosure contacts, and external review are outside this
  checkout.
