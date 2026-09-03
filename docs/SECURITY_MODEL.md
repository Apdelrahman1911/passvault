# PassVault security model

Last reviewed: 2026-09-03

## Scope and assumptions

PassVault protects a local vault against offline database/backup theft and accidental exposure while the
application is locked. It assumes the operating system, process, UI input path, and libsodium implementation are not
fully compromised while the vault is unlocked. Malware with process-memory, accessibility, keyboard, or clipboard
access can defeat application-layer protections.

There is no server, account, network sync, telemetry, or analytics boundary in this repository.

Android Release uses R8, but identifier obfuscation is not a confidentiality control. The pinned IonSpin Android
artifact loads libsodium through JNA and publishes no consumer rules. JNA resolves `JnaLibsodiumInterface` method names
and `Structure` field names at runtime, while the ordinary Kotlin wrappers are directly referenced. PassVault therefore
keeps the complete binding package as a conservative reliability policy until a minified Release device matrix covers
every production crypto path. The project rule does not suppress binding warnings, and `verifyAndroidR8Policy` rejects
additional PassVault-specific keep rules or a package-wide libsodium `-dontwarn`.

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

This ordering is deliberate because persisted KDF parameters are attacker-controlled. Missing or malformed metadata
and a wrong password return the same generic error, but their latency is not normalized: vault existence is already
exposed by startup routing, and a database attacker can inspect the structural state directly. The difference reveals
no password content. Performing a dummy KDF or holding every failure to an arbitrary device-dependent floor would add
CPU, memory, battery, and lock-transition cost without protecting a secret in the stated local threat model.

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
every decrypt. The key spec explicitly requires randomized encryption, encryption supplies no IV, and the
provider-generated IV is stored with the wrapped VEK. The key is invalidated when biometric enrollment changes.
PassVault does not claim or require StrongBox backing: it uses the device's default KeyMint/Keymaster implementation,
whose hardware isolation is device-dependent. This matches the stated OS trust boundary. A future StrongBox
preference would be optional, device-dependent hardening rather than a minimum guarantee and would require a physical
compatibility and performance matrix. iOS stores the VEK in a
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

The deployed attachment-filename AAD v1 encoding uses colon-separated UUID identifiers. Its shared read/write
builder rejects delimiter-bearing identifiers, preserving existing ciphertext bytes while making that format invariant
fail closed. Any future identifier scheme that permits the delimiter must introduce a versioned, length-prefixed AAD
encoding with explicit legacy migration rather than silently changing the authenticated bytes.

TOTP codes are derived in memory from the encrypted setup key and authoritative device time. Codes and countdowns
are not written to Room or backups. Decrypted setup-key batches remain producer-owned through a cancellation-bound
lease until the authenticator ViewModel atomically takes them; cancellation before transfer wipes the batch, and
screen teardown wipes transferred inputs. QR payloads are handled locally, parsed with strict
size/type/Base32/parameter bounds, and never sent to a service. Android and iOS camera capture and Desktop-selected
image decoding remain OS and process trust boundaries.

TOTP enrollment accepts decoded setup keys from 10 through 128 bytes. The 10-byte floor is a deliberate compatibility
exception to RFC 4226 section 4 requirement R6's 16-byte minimum: PassVault imports keys chosen by account issuers,
does not generate them, and the published
[Google Authenticator Key URI example](https://github.com/google/google-authenticator/wiki/Key-Uri-Format) itself
contains a 10-byte key. Rejecting that established format would prevent enrollment without strengthening the issuer's
account. Keys below 10 bytes are rejected, while 16 bytes or more remain preferred. A uniformly random 10-byte key has
at most a 2^80 brute-force space; PassVault cannot establish how much entropy an issuer used. Existing vault and backup
entries follow the same floor so upgrades do not disable their codes. This exception means setup-key admission does
not claim full RFC 4226 R6 conformance; RFC 6238 code calculation and its supported-algorithm vectors remain unchanged.

The database intentionally exposes structural metadata needed for queries: record identifiers, credential type,
favorite state, timestamps, folder/tag relationship identifiers, attachment MIME/size/opaque-path metadata, and
row counts. That metadata is neither confidential nor cryptographically bound to the encrypted payloads, so direct
database tampering can alter routing or availability without an AEAD failure. Search results are produced from
decrypted in-memory records while unlocked; there is no plaintext search index on disk.

Folder icons and tag colors are schema-retained visible metadata; the current folder-creation UI writes no icon and
there is no tag-creation editor, but compatible imports can contain both. New attachment MIME values come from a small
fixed detector set. Exact attachment size supports SQL quota accounting and authenticated length checks and
is also inferable from the unpadded encrypted-object framing, so bucketing only the Room column would not hide it.
Moving any of these fields requires an explicit database/payload/backup migration rather than silently changing the
deployed format. Current-schema tests enumerate every protected and reviewed-visible column and index.

At process startup, an existing database first passes a read-only `PRAGMA quick_check(1)` before Room can migrate or
query it. The real Room connection is checked again after open/migration. Corruption fails closed to recovery UI;
PassVault never repairs or destructively recreates the damaged vault automatically. A confirmed pre-open failure may
be moved together with SQLite sidecars and encrypted attachments into protected recovery storage before a fresh vault
is created; mobile recovery data is also excluded from device backups. Diagnostics are stored outside SQLite as a
bounded timestamp plus enumerated code; paths, driver messages, stack traces, and vault data are never recorded.

Desktop acquires its process lock and secures `~/.passvault` before constructing the dependency graph or opening
SQLite. Missing POSIX directories, including the attachment `objects/` and `staging/` children, are created with
owner-only `0700` permissions in the creation operation, and pre-existing directories are repaired and verified before
use. Missing Windows directories atomically inherit their parent DACL; PassVault verifies that an ACL is present rather
than replacing system-managed SYSTEM and administrator entries. A deliberately broadened profile policy and
privileged local administrators remain operating-system trust decisions, not controls the application can safely
override.

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
XChaCha20-Poly1305 records. Before any new export, the repository derives a candidate vault key from the proposed
backup password and existing vault metadata, compares it with the active VEK in constant time, and rejects an exact
master-password match. The master password is not retained or exposed, and imports of older same-password backups
remain compatible. This prevents exact reuse but cannot make a trivially modified passphrase independent; users must
still choose and safely retain a distinct, unpredictable backup passphrase. Before deriving an import key, the reader
admits only the two historical writer profiles: 64 MiB with three or four operations. Room metadata is encoded and
validated one row at a time; attachment objects are carried in 256 KiB outer records. Restore authenticates the entire
stream and stages objects, then rewinds and replays only metadata in one Room transaction. A SHA-256 transcript over
authenticated header/record proofs binds both passes.
Legacy version 1 remains readable at its historical in-memory bounds and omits attachment rows/bytes. Restore locks
the vault and deletes the previous OS biometric enrollment. Every restore format reconciles encrypted attachment
objects only after the Room replacement commits and while attachment mutation is serialized; cleanup failure does not
misreport the committed restore as failed and is shown as a warning. A Room rollback triggers a best-effort
re-enrollment of the prior VEK. The OS key store and Room still cannot share one transaction. See
[`BACKUP_FORMAT.md`](BACKUP_FORMAT.md) and [`BACKUP_CAPACITY.md`](BACKUP_CAPACITY.md).

## Session and platform controls

- Failed password unlocks use a process-local progressive delay after three failures, capped at five seconds; the UI
  separately applies a 30-second cooldown after five failures. Both reset on process restart and are bounded
  interactive-abuse friction, not a durable anti-guessing boundary. An offline attacker can omit or modify an
  unauthenticated counter copied with the vault, while tampering with the original counter could deny its owner access.
  Offline guessing resistance therefore comes from the master-password policy and per-attempt Argon2id cost.
- Manual, inactivity, and application-background signals request a central vault lock.
- The iOS native privacy cover remains opaque until the shared UI acknowledges post-lock scrubbing. Lock failures and
  acknowledgement stalls use separate bounded retries; exhaustion exposes only a localized retry surface over the
  cover and never reveals the protected Compose hierarchy.
- iOS keeps `NSFileProtectionComplete` for the Room database and sidecars. Its protected-data callback locks and
  scrubs the session, unmounts Compose, checkpoints/closes Room, and stops Koin; a fresh runtime is allowed only after
  protected data becomes available again. Teardown failures remain behind the native recovery cover.
- The iOS container defaults to `NSFileProtectionComplete`. Attachment picker copies are additionally re-protected in
  the delegate before they cross a coroutine boundary and are deleted rather than returned if that assertion fails.
- Android enables screenshot blocking for sensitive content and uses the Storage Access Framework for backup files.
  App-initiated document pickers retain a bounded return grace, but a non-exported screen-off observer active only for
  those flows revokes the grace and requests the normal background lock immediately. The policy records screen-off
  before `Activity.onStop`, so either lifecycle/broadcast ordering fails closed.
- Clipboard expiration verifies a random ownership token/value before clearing, so newer unrelated clipboard data is
  preserved.
- iOS sensitive copies are local-only and carry both an OS expiration date and an ownership-aware fallback timer.
  Background locking preserves that bounded copy for cross-app paste; manual, inactivity, memory-pressure, and
  restore locks request immediate clearing.
- Desktop sensitive writes also request Windows Clipboard History/Cloud Clipboard exclusion and publish the macOS
  concealed/transient pasteboard conventions. These platform hints cannot bind malicious or non-cooperating readers.
- Copying a TOTP code uses the same ownership-aware clipboard path as other credential values.
- Opening a credential URL requires an explicit tap and hands only its validated `http` or `https` URL to the operating
  system handler. The validator rejects embedded credentials, malformed authorities, whitespace, backslashes, and
  unsafe code points; scheme-less values default to `https`. Local/private destinations remain valid because PassVault
  does not resolve or fetch them. After the handoff, browser history or sync, extensions, system DNS, and the network
  path may observe the URL. No username, password, TOTP setup key, note, or custom field is appended.
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
- Simulator tests cover the iOS protected-data state machine and shutdown ordering, but Class A key eviction,
  lock-time forced I/O/WAL behavior, and repeated lock/unlock cycles still require a passcode-enabled physical iPhone.
- Face ID, Touch ID, Android biometrics, and Windows Hello prompts, cancellation, lockout, process restart, and
  enrollment/credential invalidation require physical-device smoke tests; compilation and common/repository tests
  cannot prove OS UI behavior. Windows Hello credentials represent the Windows Hello account and do not guarantee
  Apple's exact biometric-current-set invalidation semantic.
- Publisher signing, notarization, update security, disclosure contacts, and external review are outside this
  checkout.
