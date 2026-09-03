# PassVault security model

Last reviewed: 2026-09-03

## Scope and assumptions

PassVault protects a local vault against offline database/backup theft and accidental exposure while the
application is locked. It assumes the operating system, process, UI input path, and libsodium implementation are not
fully compromised while the vault is unlocked. Malware with process-memory, accessibility, keyboard, or clipboard
access can defeat application-layer protections.

There is no server, account, network sync, telemetry, or analytics boundary in this repository.

## Key hierarchy

```text
master password characters
          | strict UTF-8, then lowercase ASCII hex
historical KDF input bytes + 16-byte random salt
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

The repository encodes the master password directly from mutable characters to mutable UTF-8 bytes, then converts
those bytes to mutable lowercase hexadecimal bytes to preserve the original KDF format. Both buffers are wiped
best-effort after derivation. Unlock, onboarding, and password-change flows synchronously copy accepted input into an
owned `SensitiveText` and drop their observable `String` state before navigation or asynchronous repository work;
onboarding keeps only that mutable wrapper between its two password screens. Completion handlers wipe the owned
buffers even when a coroutine is cancelled before its body starts. Managed UI/IME strings and garbage-collected copies
remain outside that guarantee. The KEK is not persisted. Room
stores the salt, Argon2id operations (2–10), memory (32–256 MiB), a fixed serialized parallelism value of `1`, the
wrapped VEK, nonces, and an authenticated verification record. The current libsodium binding exposes no lanes
argument, so unlock rejects a persisted parallelism value other than `1` before derivation. Unlock validates metadata
bounds before doing expensive cryptographic work, derives the KEK, unwraps the VEK, authenticates the verification
record, and only then publishes the unlocked session. See [`VAULT_FORMAT.md`](VAULT_FORMAT.md) for the complete
parameter contract. The shared crypto suite pins both production KDF profiles byte-for-byte to independently generated
Argon2id v1.3 reference vectors on Desktop, Android host, and iOS simulator targets.

Changing the master password rewraps the same VEK, so record data does not need re-encryption. Vault session
transitions are serialized. Lock immediately removes and wipes the repository-owned VEK buffer best-effort.

Biometric unlock is an optional second route to the same VEK. It never stores the master password or KEK:

```text
Face ID / Touch ID / strong Android biometric / Windows Hello
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
remains the recovery and fallback path. iOS checks enrollment with a non-prompting metadata-only Keychain query;
`NSUserDefaults` is only a repaired cache. It retries failed deletion without hiding the item and reconciles service
items that do not match the repository's single active vault. When a fresh install has no vault, it deletes all items
under PassVault's biometric service without needing to recover their former vault identifiers.

On macOS, Touch ID directly guards a device-only Keychain item using `biometryCurrentSet`. On Windows, an exact
platform WebAuthn credential and its authenticated PRF output derive the AES-GCM key that wraps the VEK; Windows
Hello is not used as a cosmetic prompt before reading a separately accessible secret. The bridge additionally
verifies fresh challenge/assertion ownership, ES256 signatures, the resident credential's RP/vault ownership, and
device-bound/removable state. Unauthenticated local envelope IDs are never credential-deletion authority. Linux
remains master-password-only. See
[`DESKTOP_BIOMETRIC_UNLOCK.md`](DESKTOP_BIOMETRIC_UNLOCK.md) for the platform threat boundaries and release gates.

## Persisted data boundary

Credential summary and secret payloads, TOTP setup keys and parameters, folder/tag payloads, passwords in history,
and attachment filenames use XChaCha20-Poly1305 with unique random nonces. Associated data and derived keys
bind each encrypted payload to its record identity and purpose. They do not bind the record's structural routing
columns. Keyed deterministic blind indexes support exact normalized folder/tag comparisons without plaintext values;
credential titles are searched only after decrypting records in memory.

TOTP codes are derived in memory from the encrypted setup key and authoritative device time. Codes and countdowns
are not written to Room or backups. QR payloads are handled locally, parsed with strict size/type/Base32/parameter
bounds, and never sent to a service. Android and iOS camera capture and Desktop-selected image decoding remain OS
and process trust boundaries.

The database intentionally exposes structural metadata needed for queries: record identifiers, credential type,
favorite state, timestamps, folder/tag relationship identifiers, attachment MIME/size/opaque-path metadata, and
row counts. That metadata is neither confidential nor cryptographically bound to the encrypted payloads, so direct
database tampering can alter routing or availability without an AEAD failure. Search results are produced from
decrypted in-memory records while unlocked; there is no plaintext search index on disk.

At process startup, an existing database first passes a read-only `PRAGMA quick_check(1)` before Room can migrate or
query it. The real Room connection is checked again after open/migration. Corruption fails closed to recovery UI;
PassVault never repairs or destructively recreates the damaged vault automatically. A confirmed pre-open failure may
be moved together with SQLite sidecars and encrypted attachments into protected recovery storage before a fresh vault
is created; mobile recovery data is also excluded from device backups. Diagnostics are stored outside SQLite as a
bounded timestamp plus enumerated code; paths, driver messages, stack traces, and vault data are never recorded.

Attachment bytes live outside Room in random app-private object names. Every object is chunked and independently
encrypted with XChaCha20-Poly1305 under a per-attachment subkey derived from the VEK. Associated data binds the
attachment ID, owning credential ID, independent key context, detected MIME type, record type/index, and plaintext
length. An authenticated final record binds total bytes and chunks. Atomic staging, Room operation states, startup
recovery, and post-commit orphan cleanup keep partial imports/deletes from becoming visible or silently losing a live
row's object. Android registers a short-deadline, reboot-persistent cleanup lease before creating any plaintext preview
or export staging file and also requests a sweep when the vault leaves its unlocked state. Desktop preview files use
one-minute leases, tracked cleanup on session lock and bounded process shutdown, and owner-only POSIX modes or a
protected current-user Windows DACL. A viewer-held Windows handle can delay deletion, and a guarded next-launch sweep
remains the hard-crash backstop. Version-1/2 metadata-only rows remain explicit unavailable `LEGACY` records.

## Backup boundary

New `.pvault` version 2 exports use a separate backup password, fresh salt, Argon2id, and ordered authenticated
XChaCha20-Poly1305 records. Before deriving a key, the reader admits only the two historical writer profiles: 64 MiB
with three or four operations. Room metadata is encoded and validated one row at a time; attachment objects are
carried in 256 KiB outer records. Restore authenticates the entire stream and stages objects, then rewinds and replays
only metadata in one Room transaction. A SHA-256 transcript over authenticated header/record proofs binds both passes.
Legacy version 1 remains readable at its historical in-memory bounds and omits attachment rows/bytes. Restore locks
the vault and deletes the previous OS biometric enrollment. Every restore format reconciles encrypted attachment
objects only after the Room replacement commits and while attachment mutation is serialized; cleanup failure does not
misreport the committed restore as failed and is shown as a warning. A Room rollback triggers a best-effort
re-enrollment of the prior VEK. The OS key store and Room still cannot share one transaction. See
[`BACKUP_FORMAT.md`](BACKUP_FORMAT.md) and [`BACKUP_CAPACITY.md`](BACKUP_CAPACITY.md).

## Session and platform controls

- Failed unlock attempts use progressive delays.
- Manual, inactivity, and application-background signals request a central vault lock.
- The iOS native privacy cover remains opaque until the shared UI acknowledges post-lock scrubbing. Lock failures and
  acknowledgement stalls use separate bounded retries; exhaustion exposes only a localized retry surface over the
  cover and never reveals the protected Compose hierarchy.
- Android enables screenshot blocking for sensitive content and uses the Storage Access Framework for backup files.
- Clipboard expiration verifies a random ownership token/value before clearing, so newer unrelated clipboard data is
  preserved.
- iOS sensitive copies are local-only and carry both an OS expiration date and an ownership-aware fallback timer.
  Background locking preserves that bounded copy for cross-app paste; manual, inactivity, memory-pressure, and
  restore locks request immediate clearing.
- Desktop sensitive writes also request Windows Clipboard History/Cloud Clipboard exclusion and publish the macOS
  concealed/transient pasteboard conventions. These platform hints cannot bind malicious or non-cooperating readers.
- Copying a TOTP code uses the same ownership-aware clipboard path as other credential values.
- Android, iOS, macOS, and supported Windows systems expose explicit biometric/platform enrollment in Security
  settings and an unlock action beside the password field. Unsupported systems, including Linux, fail closed to
  master-password unlock.
- Desktop focus-loss locking uses an independent 30-second budget, stricter than the minimum one-minute inactivity
  preference. Only time outside PassVault accumulates, and focus changes do not replenish the budget. An app-owned
  native biometric prompt may defer a due focus lock, but only until an absolute 60-second focus-loss deadline; prompt
  end immediately re-evaluates the budget. Minimize, inactivity, explicit lock, and shutdown remain effective.
- Any repository lock/restore transition first requests cancellation of the platform biometric prompt, then proceeds
  fail-closed even if the platform cancellation API reports an error.

## Error and memory policy

User-facing paths do not expose SQL, parser, file path, ciphertext, or cryptographic exception text. Cancellation is
re-thrown rather than converted into failure. Mutable sensitive byte arrays are wiped in `finally` blocks where
practical. Managed strings, UI state snapshots, garbage-collected copies, swap/pagefile behavior, and OS crash dumps
cannot be guaranteed controllable by common Kotlin code.

## Validated threats

Automated tests verify wrong-key and biometric-candidate rejection, tampered-ciphertext rejection, nonce uniqueness,
locked repository access,
encrypted raw database rows, backup wrong-password/corruption/two-pass transcript/transaction/cancellation behavior,
attachment tamper/swap/truncation/size behavior, rapid state events, and
lock-during-edit cleanup. RFC 6238 vectors cover all supported hash algorithms, and repository tests verify that TOTP
setup keys are encrypted and survive format-2 backup restore. This is useful regression evidence, not an independent
security certification.

## Residual risks and release dependencies

- A compromised unlocked endpoint can read displayed/decrypted values.
- Incorrect device time produces invalid TOTP codes; PassVault does not synchronize the clock.
- Structural database metadata remains observable.
- `quick_check` detects SQLite structural damage, not authenticated payload tampering or the logical loss of a
  committed WAL that is no longer present; encrypted-record authentication and verified backups remain necessary.
- Structural routing metadata is not authenticated by record-payload AAD; a format/schema migration is needed to
  bind credential type/folder/favorite/timestamps and relationship rows without breaking existing records.
- Biometric-key deletion and Room restore cannot be atomic together. A failed Room replacement after successful
  key deletion leaves the old database intact; PassVault attempts to restore the former enrollment, but an OS key
  store failure can still require the user to enable biometric unlock again.
- Records written by an earlier build with newly rejected malformed Unicode or unsafe single-line control/bidi
  characters now fail closed; a compatibility migration is needed if such records exist in a real prior vault.
- Encrypted credential, attachment-filename, and password-history payloads are capped at 32 MiB during validation;
  a real prior fixture is needed before deciding whether an oversized legacy record requires migration support.
- Format-2 metadata is bounded per row rather than per vault. A deliberately maximal legacy-compatible credential
  record can still require roughly 130–195 MiB of transient managed memory; ordinary repository-created rows are far
  smaller. Format-1 compatibility import retains its older high-amplification JSON/Base64 path.
- Android device lifecycle and Desktop graphical/window-protection behavior need platform smoke tests.
- Face ID, Touch ID, Android biometrics, and Windows Hello prompts, cancellation, lockout, process restart, and
  enrollment/credential invalidation require physical-device smoke tests; compilation and common/repository tests
  cannot prove OS UI behavior. Windows Hello credentials represent the Windows Hello account and do not guarantee
  Apple's exact biometric-current-set invalidation semantic.
- Publisher signing, notarization, update security, disclosure contacts, and external review are outside this
  checkout.
