# PassVault — Independent Deep Security Audit

Audited commit: `6a6336e6ff9e0cac56b274700345ec73d4c4d5e3` (branch `main`)
Audit date: 2026-08-22
Auditor: independent review, read-only. No production code was modified.
Working tree verified clean before and after (`git status --porcelain` empty).

---

## 1. Executive Summary

PassVault is a local-first Kotlin Multiplatform password manager (Android, iOS, JVM Desktop) built on libsodium XChaCha20-Poly1305 AEAD, Argon2id password hashing, and a wrapped-vault-key hierarchy with BLAKE2b subkey derivation.

**The cryptographic core is genuinely well engineered.** I attempted to break the AEAD envelope, the key hierarchy, the backup container, and the attachment container, and could not. Specifically verified as sound:

- Every persisted ciphertext is bound by associated data to its record identity and purpose. Ciphertext transplantation between credentials, attachments, vaults, and backups fails authentication.
- The `.pvault` v2 backup format authenticates its header, KDF parameters, record type, record index, and length in the AAD of *every* record, plus a SHA-256 transcript and an authenticated FINAL record. Reorder, drop, duplicate, splice, and truncation attacks all fail.
- Attachment containers authenticate chunk index, type, size, terminal marker, and the full ownership quadruple.
- TOTP is RFC 6238 conformant. The test file contains the genuine Appendix B vectors, and I independently recomputed all 18 values with a separate HMAC implementation — all match, including the leading-zero and post-2^31 cases.
- Windows Hello is not a cosmetic prompt: it uses WebAuthn PRF with a TPM-backed platform authenticator to release real key material, rejects syncable passkeys, and verifies assertion signatures against a pinned public key.
- Zero runtime network communication is strongly supported by evidence: no networking library in the dependency graph, no `INTERNET` permission, no ATS entry, no `network.client` entitlement, and no networking symbol anywhere in production source.

**The risk is concentrated outside the primitives**, in five areas:

1. **Data-loss defects.** A cancellation window during backup restore can delete the attachment blobs the just-committed restore depends on (BKP-01). A publicly reachable recovery routine can sweep every attachment object outside the serializing mutex (ATT-01).
2. **Availability of the lock itself.** `withUnlockedSession` holds the session mutex across arbitrary caller work — including an interactive biometric prompt — so auto-lock can be blocked indefinitely, and a failed auto-lock stalls until the next user input (SES-01, SES-06).
3. **Metadata disclosure.** The Room database is not SQLCipher. Credential type, folder graph, tag graph, favorites, millisecond timestamps, attachment sizes and MIME types, folder icons, and Argon2 parameters are all plaintext. Ciphertext is unpadded, so password lengths leak.
4. **Platform hardening gaps.** Desktop `lock()` only iconifies the window rather than drawing its existing opaque curtain synchronously; the Windows native bridge checksum lives beside the DLL in a user-writable directory; macOS entitlements disable library validation.
5. **Verification that overstates itself.** `Argon2Test.kt` never calls Argon2 and contains no known-answer vectors. Several crypto tests assert nothing at all. The documentation — in particular `PENTEST.md` — states that Desktop biometric unlock does not ship, when a full Windows WebAuthn bridge and macOS Touch ID path are present in the tree.

**Verdict: not production-ready today**, but the gap is closable. No finding indicates a broken cryptographic construction. The blocking items are two data-loss defects, two lock-liveness defects, and a documentation set that materially misrepresents the shipped attack surface.

**Counts:** 2 Critical, 9 High, 24 Medium, 21 Low, 12 Informational — 68 findings total.

---

## 2. Repository Snapshot

| Property | Value |
|---|---|
| Repository | https://github.com/Apdelrahman1911/passvault |
| Branch | `main` |
| HEAD SHA | `6a6336e6ff9e0cac56b274700345ec73d4c4d5e3` |
| HEAD commit | Merge pull request #24 (`codex/fix-macos-intel-candidate`) |
| Commit date | 2026-08-14 |
| Tracked files | 631 |
| Working tree | clean (no modifications, before and after audit) |
| Submodules | none (`.gitmodules` absent) |
| Git LFS | not in use |
| App version | 1.0.5 (versionCode 1000034), channel `RC` |

### Toolchain

| Component | Version |
|---|---|
| JDK (host) | 21.0.9 LTS (Oracle) |
| Gradle | 9.6.1 (SHA-256 pinned in wrapper properties) |
| Kotlin | 2.4.10 |
| KSP | 2.3.10 |
| Compose Multiplatform | 1.11.1 (Material3 1.9.0) |
| Android Gradle Plugin | 9.3.1 |
| Android compileSdk / targetSdk / minSdk | 37 / 37 / 24 |
| iOS deployment target | 18.5 |
| Desktop targets | Windows x64, macOS (arm64 + x64), Linux x64 |
| Room | 2.8.4 (schema v3, `exportSchema = true`) |
| SQLite | androidx sqlite-bundled 2.6.2 (**not** SQLCipher) |
| Crypto | `com.ionspin.kotlin:multiplatform-crypto-libsodium-bindings:0.9.5` |
| Argon2 | libsodium `crypto_pwhash`, `ALG_DEFAULT` = Argon2id |
| TOTP | hand-written over okio 3.17.0 HMAC (`hmacSha1/256/512`) |
| QR | ZXing core 3.5.4 (decode-only artifact) |
| Biometric | androidx.biometric 1.1.0; native C++/ObjC++ bridge via JNA 5.18.1 |
| DI | Koin 4.2.2 |
| Coroutines / Serialization | 1.11.0 / 1.11.0 |
| Static analysis | detekt **2.0.0-alpha.5** (pre-release) |

Dependencies were **not** updated during this audit.

---

## 3. Audit Scope and Methodology

The audit was executed as 18 parallel workstreams with disjoint file ownership, followed by a consolidated cross-workstream review performed by the lead auditor.

| ID | Workstream | Ownership |
|---|---|---|
| W0 | Snapshot, inventory, toolchain | root config |
| W1 | Cryptographic core | `core/crypto` |
| W2 | Android platform | `app-android`, Android source sets |
| W3 | Desktop platform | `app-desktop`, native bridge, desktop source sets |
| W4 | iOS platform | `iosApp`, `iosMain` source sets |
| W5 | CI/CD, dependency verification | `.github`, `gradle/verification-metadata.xml` |
| W6 | Backup format, parser, restore | `core/database/.../backup`, `feature/backup` |
| W7 | Attachment subsystem | `core/database/.../attachment` |
| W8 | Room schema, migrations, DAOs | `core/database` entities/dao/migrations |
| W9 | TOTP and QR | `core/otp`, TOTP UI |
| W10 | Session, lock, throttling, biometric | `VaultRepositoryImpl`, `core/security`, unlock |
| W11 | Domain model, generator, health | `core/domain`, generator, health |
| W12 | Compose secret state | feature ViewModels, `SecureTextField` |
| W13 | Navigation security | `core/navigation`, `shared/navigation` |
| W14 | Test suite quality | all `*Test` source sets |
| W15 | Documentation claim verification | all `.md`, `site/` |
| W16 | Build/test/detekt execution | Gradle execution |
| W17 | Network isolation, logging, scripts, secrets | `scripts/`, repo-wide grep |

**Method.** Security-sensitive code was read line by line, not sampled. Binary formats (`.pvault`, `.pva`, the AEAD envelope) were reverse-engineered from source rather than taken from documentation. Every documentation claim was treated as an assertion requiring independent proof. Existing audit artifacts (`PENTEST.md`, `docs/PRODUCTION_READINESS_AUDIT.md`) were treated as *input only*.

**Independent verification performed:**
- Recomputed all 18 RFC 6238 Appendix B TOTP vectors with a separate HMAC implementation.
- Hand-proved the rejection-sampling uniformity of the password generator for all bounds 1..65535.
- Computed effective password entropy for three generator configurations.
- Executed the build, the test suite, detekt, and both dependency-verification tasks on the frozen tree.
- Confirmed the unwipeable-`String` hex encoding of the master password by direct source inspection.

**Limits.** The host is Windows. No Apple toolchain, no biometric hardware, no Android device, no Ruby, and no CMake were available. Findings on iOS, macOS, hardware biometrics, localization validation, and the compiled native bridge are source-derived and explicitly marked unverified.


---

## 4. Threat Model

### Attackers considered

| # | Attacker | In scope |
|---|---|---|
| T1 | Steals the Room database file only | Yes |
| T2 | Steals the entire application data directory | Yes |
| T3 | Obtains a `.pvault` backup file | Yes |
| T4 | Reads exported/staged plaintext attachments | Yes |
| T5 | Temporary access to an unlocked device | Yes |
| T6 | Persistent local OS account access (same user) | Yes |
| T7 | Malicious Android application (separate UID) | Yes |
| T8 | Malicious Desktop application (same user, no sandbox) | Yes |
| T9 | Clipboard observer / clipboard-history service | Yes |
| T10 | Screenshot / screen recording / accessibility service | Yes |
| T11 | Process-memory inspection, heap dump, core dump | Partial |
| T12 | Crash dump / swap / pagefile / hibernation | Partial |
| T13 | Device or filesystem backup (iCloud, Auto Backup, Time Machine) | Yes |
| T14 | Rooted Android / jailbroken iOS | Partial |
| T15 | Fully compromised desktop OS | **Non-goal** |
| T16 | Malicious file supplied to import/restore | Yes |
| T17 | Corrupted database / corrupted backup | Yes |
| T18 | Attacker who can *write* to the database file | Yes |
| T19 | Downgrade to older app/DB/backup format | Yes |
| T20 | Weak user-chosen master password | Yes |
| T21 | Offline brute force against stolen vault material | Yes |
| T22 | Malicious QR containing hostile TOTP data | Yes |
| T23 | Malicious filename / attachment metadata | Yes |
| T24 | Supply-chain compromise (dependency, action, plugin) | Yes |
| T25 | Compromised CI or release signing credentials | Yes |

### Expected protection

- Vault contents (passwords, notes, TOTP seeds, card and identity fields, custom fields, attachments, password history) remain confidential against T1, T2, T3, T4 without the master password.
- Any modification to persisted ciphertext, backup contents, or attachment contents is **detected**, not silently accepted (T17, T18).
- Ciphertext cannot be moved between records, credentials, vaults, or backups (T18).
- The vault locks on background, inactivity, and explicit request, and locking removes the usable key (T5).
- No vault data leaves the device by network (T24 partially, privacy generally).
- Biometric unlock is bound to an OS-protected secret, not an application boolean (T14 partially).

### Explicit non-goals

- **A fully compromised OS (T15) is not defended against.** Once an attacker executes code as the user with the vault unlocked, the keys are in that process.
- **Guaranteed memory erasure is not achievable** on JVM/ART. Kotlin `String` is immutable; wiping is best-effort on `CharArray`/`ByteArray` only.
- **Secure deletion of files is not claimed** and is not achievable over modern flash/COW filesystems.
- **Offline brute force is bounded only by Argon2id and master-password entropy.** In-app throttling is irrelevant to it.
- **Desktop screen-capture prevention does not exist.** This is honestly documented.
- **Structural metadata in the Room file is deliberately plaintext.** Assessed below, not assumed harmless.

Severity throughout is *impact x realistic reachability under this model*.

---

## 5. Security Goals and Non-Goals — Assessment

| Goal | Status |
|---|---|
| Confidentiality of secret payloads at rest | **Met.** Field-level AEAD; no plaintext secret column exists. |
| Integrity/authenticity of every ciphertext | **Met.** AAD binds record identity and purpose everywhere I checked. |
| Ciphertext non-transplantation | **Met** for credentials, attachments, backups, vaults. |
| Backup tamper evidence | **Met** for format v2. Legacy v1 path is weaker. |
| Lock removes usable key material | **Partially met.** Key is zeroed, but lock can be blocked or stall (SES-01, SES-06). |
| Biometric bound to OS-protected secret | **Met** on Android, iOS, macOS, Windows. StrongBox not requested on Android. |
| No network communication | **Met** (strongly supported). |
| Metadata confidentiality | **Not a goal**, and correctly documented as such in `SECURITY_MODEL.md` — but omitted from `SECURITY.md` and `PENTEST.md`. |
| Data durability / no silent loss | **Not met.** See BKP-01, ATT-01, ATT-03. |
| Guaranteed memory erasure | **Correctly disclaimed**, but the stated *mechanism* is wrong (CRY-02). |

---

## 6. Architecture Overview

Clean layered KMP architecture, 17 Gradle modules.

```
app-android      app-desktop      iosApp
      \              |              /
       \             |             /
        +--------- shared ---------+        DI (Koin), navigation host,
                     |                      lifecycle, auto-lock, clipboard
        +------------+------------+
        |            |            |
   feature/*    core/designsystem  core/navigation
   (onboarding, unlock, vault, credential,
    generator, health, settings, backup)
        |
   core/database  --->  core/crypto
        |                    |
   core/domain <------------ +        core/security   core/otp   core/testing
```

**Ownership boundaries, verified:**

- `core/crypto` is the only module invoking libsodium. Android and Desktop "engines" are thin delegates to the shared `LibsodiumCryptoEngine` — no platform crypto divergence.
- `VaultRepositoryImpl` is the **sole** runtime owner of the Vault Encryption Key. All state transitions serialize on one `Mutex`.
- Repositories obtain the VEK only through `withUnlockedSession { }`, which leases a *copy* and wipes it in `finally`.
- Platform capabilities (biometric key store, clipboard, screenshot protection, file stores, settings) are `expect`/interface boundaries implemented per platform and injected by Koin.
- `core/domain` holds no platform or crypto dependency.

**Concurrency model.** Structured concurrency throughout; `Dispatchers.Default` for crypto, `Dispatchers.IO` for file work, `NonCancellable` around commit/cleanup regions. Session transitions are mutex-serialized.

**Error model.** `Result<T>` at repository boundaries with deliberately uniform, non-informative error strings — a correct choice that avoids oracles. `CancellationException` is consistently rethrown rather than converted into a domain failure.

---

## 7. Module Dependency Map

| Module | Depends on | Notes |
|---|---|---|
| `core:domain` | (none) | Pure models, policies, repository interfaces |
| `core:crypto` | `core:domain`, libsodium | Only libsodium consumer |
| `core:database` | `core:domain`, `core:crypto`, `core:security`, Room, okio | Owns VEK session |
| `core:security` | `core:domain` | Biometric/clipboard/screenshot/UI-security contracts |
| `core:otp` | `core:domain`, okio | HMAC via okio |
| `core:navigation` | `core:domain`, kotlinx-serialization | Typed nav keys |
| `core:designsystem` | Compose | `SecureTextField`, theme, `UiText` |
| `core:testing` | `core:domain` | Fakes — including a stub crypto engine |
| `feature:*` | `core:domain`, `core:designsystem`, `core:navigation`, `core:security` | No direct crypto |
| `shared` | all core + all feature | DI graph, nav host, lifecycle |
| `app-android` / `app-desktop` / `iosApp` | `shared` | Platform entry points, platform adapters |

No cycles. Features do not reach into `core:crypto` or `core:database` internals.

---

## 8. Sensitive Data Flow Map

```
Master password (CharArray in SensitiveText)
  -> toUtf8ByteArray()  [CRY-02: creates an unwipeable String]
  -> toHexString()      [CRY-02: creates a SECOND unwipeable String]
  -> Argon2id(salt16, ops, mem) -> KEK(32B)
  -> XChaCha20-Poly1305 unwrap(wrappedVek, AAD="VEK_WRAP") -> VEK(32B)
  -> verify against encryptedVerificationRecord (AAD="verification")
  -> VEK stored in VaultRepositoryImpl.currentVek (session)

VEK -> keyed BLAKE2b subkeys (domain "passvault-subkey-v1\0")
  |- "record:<credentialId>"      -> credential summary + secret payloads
  |- "history:<historyId>"        -> password history entries
  |- "attachment:<context>"       -> attachment filename + container
  |- "blind-index:title:<value>"  -> title_hash (UNUSED - see DB-04)
  |- "backup" / "search" / "duplicate" -> declared, usage varies

Credential plaintext
  -> JSON (kotlinx) -> ByteArray
  -> encrypt(recordKey, AAD "passvault:credential:<id>:<summary|secret>:v1")
  -> CryptoEnvelope.encode -> Room BLOB column
  -> decrypt on read -> domain model -> ViewModel -> Compose

Backup: Room rows -> BackupEntityBinaryCodec -> per-record AEAD
  (AAD = header || domain || type || index || len) -> .pvault
Attachment: plaintext stream -> 256KiB chunks -> per-chunk AEAD
  (AAD = domain || attachmentId || credentialId || keyCtx || mime || type || index || size)
  -> objects/<uuid>.pva
```

---

## 9. Cryptographic Architecture

### Primitives

| Purpose | Algorithm | Source |
|---|---|---|
| Password hashing | Argon2id (`crypto_pwhash`, `ALG_DEFAULT`) | `LibsodiumCryptoEngine.kt:60-67` |
| AEAD | XChaCha20-Poly1305-IETF | `LibsodiumCryptoEngine.kt:115-120` |
| Subkey derivation | keyed BLAKE2b (`crypto_generichash`) | `LibsodiumCryptoEngine.kt:220-224` |
| CSPRNG | `LibsodiumRandom.buf` | `LibsodiumCryptoEngine.kt:95, 112` |
| Constant-time compare | manual XOR accumulate | `LibsodiumCryptoEngine.kt:237-244` |

### Envelope format

```
CryptoEnvelope (persisted form)
  +0   4B   magic  50 56 02 00
  +4   NB   XChaCha20-Poly1305 ciphertext
  +4+N 16B  Poly1305 tag  (included in the libsodium ciphertext)
Nonce (24B) stored in a sibling column, never inside the envelope.
```

`decrypt` **requires** the magic (`LibsodiumCryptoEngine.kt:165-167`) and a minimum length, so legacy or truncated payloads are rejected before reaching the AEAD. `CryptoEnvelope.normalize` handles a historical duplicated-trailing-tag representation, stripping the duplicate only when it is byte-identical to the preceding tag.

### Domain separation

`deriveSubkey` builds `"passvault-subkey-v1\0" || decimalLength || 0x00 || context` and hashes it under the VEK. The explicit length prefix plus NUL separator makes the encoding injective — two different contexts cannot collide. This is correct and better than the naive concatenation used for the filename AAD (ATT-05).

### Assessment

The construction is sound: random data key wrapped by a password-derived key, per-purpose subkeys, authenticated encryption everywhere, verification record separate from the wrap, and no unauthenticated fallback path. I found no way to make `decrypt` return plaintext without tag verification, and no code path that uses ciphertext before authentication succeeds.

---

## 10. Complete Key Hierarchy

```
                 Master password (user)
                          |
              Argon2id (salt 16B, ops 3, mem 64MB, p=1)
                          |
                        KEK 32B  --- never persisted
                          |
        XChaCha20-Poly1305, AAD "VEK_WRAP"
                          |
                    wrapped VEK  --- vault_metadata.wrapped_vek
                          |
                        VEK 32B  --- session only, zeroed on lock
                          |
           keyed BLAKE2b, domain "passvault-subkey-v1\0"
     +--------+--------+--------+---------+---------+---------+
     |        |        |        |         |         |         |
 record:  history:  attachment:  backup   search  duplicate  blind-index:
  <id>     <id>      <ctx>                                    title:<v>
```

**Long-lived secrets and their locations:**

| Secret | Lives where | Cleared |
|---|---|---|
| Master password | `SensitiveText` CharArray + **2 unwipeable Strings** | Partially (CRY-02) |
| KEK | Local `DerivedKey`, wiped in `finally` | Yes |
| VEK | `VaultRepositoryImpl.currentVek` | Zeroed on lock |
| VEK lease copy | Local to `withUnlockedSession` | Wiped in `finally` |
| Subkeys | Local per operation | Wiped in `finally` |
| Biometric-wrapped VEK | Android Keystore-wrapped blob in prefs; iOS/macOS Keychain; Windows envelope file | On unenroll/invalidation |
| Backup key | Derived per backup from backup password | Wiped after use |

Separation is genuinely enforced. Backup keys derive from the backup password, **not** from the VEK, so a compromised backup password cannot yield the vault key.

---

## 11. Vault Unlock Flow

```
UnlockViewModel.unlock(password: String)   [SES-08: String, not CharArray]
  -> SensitiveText.from(password)
  -> VaultRepositoryImpl.unlock()  [sessionMutex.withLock — whole body]
       1. MasterPasswordPolicy.acceptsExisting(1..1024)
       2. reject if already unlocked
       3. wipe any residual currentVek
       4. if failedAttempts >= 3: delay(min((n-2)*500ms, 5000ms))   [SES-03]
       5. state = Unlocking
       6. load + validateMetadataForUnlock()   [strict bounds — good]
       7. unwrapVaultKey: Argon2id -> KEK -> unwrap VEK (AAD VEK_WRAP)
       8. verifyVaultKey: AEAD-decrypt verification record (AAD "verification")
       9. openSession: currentVek = copy; state = Unlocked(sessionId)
      10. failedAttempts = 0
   on failure: failedAttempts++, state = Locked, uniform error string
   finally: candidateVek wiped
```

Metadata validation (`VaultRepositoryImpl.kt:503-518`) enforces format version, algorithm id, salt length, Argon2 bounds, parallelism, envelope sizes, and nonce lengths **before** the KDF runs. This correctly blocks a KDF-parameter downgrade attack from a tampered database.

---

## 12. Biometric Unlock Flow

```
ENROLL   (requires an already-unlocked session)
  withUnlockedSession { vek -> keyStore.enroll(vaultId, vek) }   [SES-01: mutex held across OS prompt]

Android  KeyGenParameterSpec: AES-256-GCM, userAuthenticationRequired,
         AUTH_BIOMETRIC_STRONG, timeout 0 (per-use),
         invalidatedByBiometricEnrollment=true; no StrongBox [AND-03]
         -> GCM ciphertext + IV in MODE_PRIVATE prefs
iOS      Keychain generic password, kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
         kSecAccessControlBiometryCurrentSet, synchronizable=false
macOS    same class + flags, kSecUseDataProtectionKeychain=YES, fresh LAContext
Windows  WebAuthn resident credential, PRF/hmac-secret, UV required, platform
         attachment, credProtect; PRF -> HKDF -> AES-GCM envelope, DACL-protected

RETRIEVE -> unlockWithBiometricKey(vaultKey)
  1. reject if length != 32
  2. if already unlocked -> return existing sessionId   [SES-05: skips verification]
  3. verifyBiometricVaultKey -> AEAD verification record decrypt
  4. openSession
  on rejection: BiometricVaultKeyRejectedException -> keyStore.delete (self-heal)
```

**The key released by biometrics is always cryptographically verified against the vault** before it becomes the session key. There is no boolean-gated bypass. Windows and macOS both release real key material rather than an "authenticated" flag.

---

## 13. Credential Encryption Flow

```
Credential (domain)
  -> validateCredential(): ids, lengths, unicode safety, URL hosts, TOTP shape
  -> summary  = {title, usernameHint, emailHint, passwordHealth}
     secret   = {username,email,password,urls,notes,recoveryCodes,apiKeys,
                 licenseKeys,customFields,totp,passwordChangedAt}
  -> recordKey = deriveSubkey(VEK, "record:<credentialId>")
  -> encrypt(summary, recordKey, AAD "passvault:credential:<id>:summary:v1")
     encrypt(secret,  recordKey, AAD "passvault:credential:<id>:secret:v1")
  -> CryptoEnvelope.encode -> summary_payload / secret_payload (+ nonces)
  -> updateCredentialWithTagsAndHistory  [single Room transaction — verified atomic]
```

Read reverses this and additionally cross-checks that `summary.usernameHint == secret.username` and `summary.emailHint == secret.email`, which prevents an attacker with DB write access from pairing one record's summary with another's secret. All intermediate byte arrays are wiped in `finally`.



---

## 14. Backup Encryption Flow (`.pvault` v2)

Reverse-engineered from `BackupV2Codec.kt` and `VaultBackupV2Service.kt`. Big-endian throughout.

```
HEADER — 44 bytes, plaintext, but authenticated as AAD of EVERY record
  +0   8B  magic  50 56 42 41 43 4B 02 00   ("PVBACK",02,00)
  +8   4B  formatVersion == 2
  +12  4B  argon2 opsLimit  (accepted range 2..10)
  +16  4B  argon2 memLimit  (accepted range 32MiB..256MiB)
  +20  4B  parallelism == 1
  +24  4B  chunkBytes == 262144
  +28 16B  salt (CSPRNG)

key = Argon2id(password, salt, ops, mem, 32)

RECORD — 41-byte frame + ciphertext
  +0   1B  type (1..14)
  +1   8B  recordIndex  (must equal expected sequential index)
  +9   4B  plaintextSize (0..typeMax)
  +13 24B  nonce (fresh CSPRNG per record)
  +37  4B  ciphertextSize == plaintextSize + 20
  +41  NB  50 56 02 00 || XChaCha20(pt) || Poly1305[16]

AAD (83 bytes) = header[44]
              || "passvault:backup-record:v2"
              || u8 type || u64 index || u32 plaintextSize

STREAM
  1 MANIFEST -> 2 METADATA -> 3 FOLDER* -> 4 TAG* -> 5 CREDENTIAL*
  -> 6 CRED_FOLDER_REF* -> 7 CRED_TAG_REF* -> 8 ATTACHMENT*
  -> 9 PASSWORD_HISTORY* -> 10 METADATA_END
  -> (11 ATTACHMENT_START, 12 CONTENT*, 13 ATTACHMENT_END) per attachment
  -> 14 FINAL {u64 recordCountBeforeFinal, u32 managedAttachmentCount, u64 totalObjectBytes}
  -> strict EOF (requireExhausted: no trailing bytes, declared size must match)

Plus a SHA-256 transcript over the header and each metadata record's
type/index/length/nonce/tag, compared in constant time across two passes.
```

### Authentication matrix

| Element | Authenticated | Mechanism |
|---|---|---|
| magic, version, KDF params, salt | Yes | In AAD of every record |
| record type | Yes | AAD |
| record order | Yes | AAD index + sequential `require` |
| record length | Yes | AAD + post-decrypt equality |
| per-type counts | Yes | MANIFEST is record 0, sealed |
| total counts / object bytes | Yes | FINAL record |
| metadata transcript | Yes | SHA-256 cross-pass, constant-time compare |
| attachment content | Yes | Per-record AAD + FINAL counters |

**Reorder, drop, duplicate, splice, truncate, and cross-file transplantation all fail.** Because the per-file random salt is in the AAD, a record from backup A cannot authenticate in backup B even under the same password. KDF parameter bounds are checked *before* `deriveKey` is invoked, so a malicious header cannot demand 1 GB of Argon2 memory.

---

## 15. Attachment Encryption Flow (`.pva`)

```
HEADER — 16B
  +0   8B  magic 50 56 41 54 54 00 01 00
  +8   4B  formatVersion == 1
  +12  4B  chunkSize == 262144

RECORD — 41B frame + ciphertext (same shape as backup)
  type: 1 = DATA, 2 = FINAL
  FINAL payload (16B) = u64 totalPlaintextBytes || u64 chunkCount

AAD per record (length-prefixed — injective, unlike the filename AAD)
  "passvault:attachment-content:v1"
  || u32len attachmentId  || u32len credentialId
  || u32len keyDerivationContext || u32len mimeType
  || u32 recordType || u64 recordIndex || u32 plaintextSize

key = deriveSubkey(VEK, "attachment:<keyDerivationContext>")
storage = objects/<uuid-v4>.pva   (path shape strictly validated)
```

**Ownership binding is complete.** The blob is bound to `attachmentId`, `credentialId`, `keyDerivationContext`, and `mimeType`. Swapping `keyDerivationContext` alone yields the right key but the wrong AAD and fails. Swapping `credentialId` fails. Cross-vault transplantation fails because the subkey derives from a per-vault VEK. Chunk reorder, duplication, and truncation all fail (index in AAD, monotonic counter check, authenticated FINAL with total bytes and chunk count, plus strict EOF).

One residual: no explicit vault identifier appears in the AAD. Separation rests entirely on VEK uniqueness. Any future feature that clones a VEK across vaults would open cross-vault transplantation.

---

## 16. Review Coverage

| Classification | Files | Reviewed | Method |
|---|---|---|---|
| Production Kotlin | 225 | 225 | Line-by-line for security-sensitive; full read otherwise |
| Tests | 82 | 82 | 9 read fully, 73 reviewed by name/assertion analysis |
| Scripts | 85 | 85 | Skimmed for injection, secrets, traversal, strictness |
| Documentation | 50 | 50 | Claim-by-claim verification against source |
| Release/fastlane | 41 | 41 | Signing, provenance, promotion |
| Config/resources | 36 | 36 | Manifests, plists, entitlements, rules |
| Build (Gradle) | 27 | 27 | Full read of root + module scripts |
| Third-party licenses | 23 | 23 | Classified; attribution checked |
| iOS/Swift/Xcode | 19 | 19 | Full read; pbxproj by targeted grep |
| CI/CD workflows | 12 | 12 | Full read, all triggers/permissions/`uses:` |
| Binary assets | 12 | 12 | Classified, not decompiled (icons, PNGs, wrapper jar) |
| Website | 10 | 10 | Claims cross-checked |
| Native C++/ObjC++ | 8 | 8 | Full read of Windows + macOS bridge |
| Other | 1 | 1 | `.gitkeep`-class |
| **Total** | **631** | **631** | Every tracked file accounted for |

Approximate LOC: **61,862** total code (Kotlin/Gradle/Swift/C++/ObjC++), of which **14,629** is test code. Crypto and security-critical code reviewed line-by-line: ~9,800 LOC. Platform adapter code: ~11,400 LOC.

**Non-reviewable, classified only:** 12 binary assets (PNG/ICO/ICNS icons, `gradle-wrapper.jar`). The wrapper jar is validated by `gradle/actions/wrapper-validation` in CI and by a pinned `distributionSha256Sum`.

---

## 17. File-by-File Coverage Ledger

Grouped by logical set; every tracked path is covered by exactly one row.

| Path / group | Class | ~LOC | Reviewed | Review areas | Findings |
|---|---|---|---|---|---|
| `core/crypto/src/**` (7 prod) | crypto | 830 | Yes | Argon2, AEAD, subkeys, RNG, generator, wiping | CRY-01..05, GEN-01..04 |
| `core/crypto/src/commonTest/**` (6) | test | 1,150 | Yes | KAT coverage, tamper coverage | TST-01..05 |
| `core/database/.../backup/**` (6) | crypto/storage | 3,420 | Yes | Format, parser, restore atomicity | BKP-01..15 |
| `core/database/.../attachment/**` (4) | crypto/storage | 1,610 | Yes | Container, blob store, lifecycle | ATT-01..10 |
| `core/database/.../repository/**` (7) | storage | 2,890 | Yes | Session, credential crypto, validation | SES-01..09, DB-* |
| `core/database/.../dao/**` (7) | storage | 940 | Yes | SQL, transactions, injection | DB-06, DB-07 |
| `core/database/.../entity/**` (7) | storage | 620 | Yes | Schema, plaintext columns, FKs | DB-01..05, DB-08 |
| `core/database/VaultMigrations.kt`, `VaultDatabase.kt` | storage | 190 | Yes | Migration safety | (safe) |
| `core/database/schemas/*.json` (3) | generated | 1,780 | Classified | Column extraction by grep | DB-01 |
| `core/database/src/*Test/**` (10) | test | 4,100 | Yes | Real-crypto integration, migration | TST-05..08 |
| `core/domain/src/commonMain/**` (10) | domain | 1,540 | Yes | SensitiveText, policies, validation | DOM-01..04 |
| `core/domain/src/commonTest/**` (8) | test | 1,020 | Skim | Assertion quality | TST-09 |
| `core/security/src/**` (7) | security | 780 | Yes | Biometric/clipboard/screenshot contracts | SES-07, DSK-05 |
| `core/otp/src/**` (2) | crypto | 640 | Yes | RFC 6238, Base32, URI parser | OTP-01..06 |
| `core/navigation/src/**` (9+5 test) | navigation | 1,180 | Yes | Route keys, restoration, external nav | NAV-01..03 |
| `core/designsystem/src/**` (17) | ui | 2,240 | Yes | SecureTextField, theme, a11y | UI-01..03 |
| `core/testing/src/**` (5+3 test) | test-support | 690 | Yes | FakeCryptoEngine stub analysis | TST-01 |
| `feature/backup/**` (9+1 test) | feature | 2,180 | Yes | Export/import UI, password policy | BKP-09..11 |
| `feature/credential/**` (14+2 test) | feature | 3,960 | Yes | Attachment/TOTP controllers, editors | ATT-03, OTP-04, UI-02 |
| `feature/vault/**` (11+4 test) | feature | 2,410 | Yes | List, 2FA screen, seed lifetime | OTP-05 |
| `feature/health/**` (8+2 test) | feature | 1,290 | Yes | Reuse detection, plaintext lifetime | HLT-01..03 |
| `feature/generator/**` (5+1 test) | feature | 890 | Yes | Generator UI, entropy display | GEN-04 |
| `feature/settings/**` (8+3 test) | feature | 1,830 | Yes | Change password, biometric toggle | UI-02 |
| `feature/onboarding/**` (6+1 test) | feature | 1,120 | Yes | Master password creation | UI-02, DOM-02 |
| `feature/unlock/**` (2+1 test) | feature | 640 | Yes | Unlock VM, error uniformity | SES-08, SES-09 |
| `shared/src/commonMain/**` (24) | shared | 3,480 | Yes | DI, nav host, auto-lock, clipboard | SES-06, NAV-02 |
| `shared/src/androidMain/**` (4) | android | 210 | Yes | DB/attachment providers | (safe) |
| `shared/src/desktopMain/**` (4) | desktop | 240 | Yes | Storage paths, permissions | DSK-08 |
| `shared/src/iosMain/**` (13) | ios | 2,690 | Yes | Keychain, file protection, clipboard | IOS-01..09 |
| `shared/src/*Test/**` (14) | test | 1,940 | Skim | Nav/lock policy tests | TST-07 |
| `app-android/src/main/**` (11 kt) | android | 1,870 | Yes | Manifest, keystore, clipboard, staging | AND-01..08 |
| `app-android/src/main/res/**` (19) | resource | 340 | Yes | Backup rules, FileProvider paths | AND-05 |
| `app-android/src/{debug,test}/**` (5) | test/debug | 280 | Yes | Debug strings, cleanup tests | TST-10 |
| `app-android/build.gradle.kts`, `proguard-rules.pro` | build | 780 | Yes | Variants, signing, R8 | AND-07, AND-08 |
| `app-desktop/src/desktopMain/**` (17) | desktop | 3,120 | Yes | Window, tray, biometric, stores | DSK-01..12 |
| `app-desktop/native/**` (8) | native | 2,940 | Yes | WebAuthn PRF, Touch ID, ABI | DSK-02, DSK-06, DSK-09 |
| `app-desktop/src/desktopTest/**` (9) | test | 1,340 | Skim | Bridge/loader/cleanup tests | TST-11 |
| `app-desktop/build.gradle.kts`, resources (5) | build | 1,480 | Yes | Packaging, entitlements, verification | DSK-03 |
| `iosApp/**` (19) | ios | 1,210 | Yes | Info.plist, entitlements, pbxproj, Swift | IOS-02, IOS-06, IOS-10 |
| `.github/workflows/**` (11) + dependabot | ci-cd | 4,180 | Yes | Triggers, permissions, pinning, provenance | CI-01..08 |
| `scripts/**` (85) | scripts | 9,640 | Yes | Secrets, injection, traversal, strictness | SCR-01..03 |
| `gradle/verification-metadata.xml` | build | 11,818 | Grep | 2,946 artifacts, trust rules | CI-03 |
| `gradle/libs.versions.toml`, wrapper, `gradlew*` | build | 210 | Yes | Versions, pinning | CI-04, CI-05 |
| `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties` | build | 490 | Yes | Custom verify tasks, repos | CI-06 |
| `detekt.yml` | config | 68 | Yes | Disabled rules | CI-05 |
| `release/**` (41) | release | 2,180 | Yes | Cert, provenance, store assets | SCR-02 |
| `fastlane/**` (2) | release | 320 | Yes | Promotion invariants | (safe) |
| `docs/**` (25) | docs | 6,900 | Yes | Claim verification | DOC-01..10 |
| Root `*.md` (10) | docs | 3,240 | Yes | Claim verification | DOC-01..10 |
| `agent-skills/**` (15) | docs | 1,780 | Skim | Non-shipping guidance | (none) |
| `site/**` (10) | website | 1,120 | Yes | Public claims | DOC-03 |
| `THIRD_PARTY_LICENSES/**` (23), `NOTICE.txt`, `THIRD_PARTY_NOTICES.md` | licenses | 3,900 | Yes | Attribution completeness | LIC-01 |
| Binary assets (12) | binary | n/a | Classified | Icons, PNGs, wrapper jar | (none) |
| `.editorconfig`, `.gitattributes`, `.gitignore`, `.ruby-version`, `Gemfile*`, `version.properties`, `LICENSE.txt` | config | 320 | Yes | Ignore rules, versions | (safe) |

**Ledger reconciled against `git ls-files`: 631 / 631 accounted for.**

---

## 18. Build and Test Results

Executed on the frozen tree, Windows host, JDK 21.0.9, Gradle 9.6.1.

| Task | Result | Notes |
|---|---|---|
| `gradlew --version` | PASS | Gradle 9.6.1 / Kotlin 2.3.21 |
| `verifyDependencies` | **PASS (executed)** | "Validated SHA-256 dependency verification metadata (775,259 bytes)" |
| `verifyReleaseVersion` | **PASS (executed)** | "Validated cross-platform release version 1.0.5 (1000034)" |
| `:core:crypto:desktopTest` +3 | PASS but **UP-TO-DATE** | No test JVM launched this run |
| `detekt --continue` | PASS but **UP-TO-DATE** | 20 tasks up-to-date; 2 aggregator tasks NO-SOURCE |
| `test --continue` | **FAIL** (3 task failures) | See below |
| `:app-desktop:compileKotlinDesktop` | PASS (UP-TO-DATE) | 28 executed / 87 up-to-date |
| `:app-android:assembleDebug` | PASS (UP-TO-DATE) | APK 36.37 MB produced |

### Test totals

**1,160 tests — 1 failure, 0 errors, 0 skipped**, across 134 `testsuite` XML files. Counted by summing `tests`/`failures`/`errors`/`skipped` attributes over all `**/build/test-results/**/*.xml`.

### Failures analysed

1. `AndroidAttachmentPlaintextCleanupTest.startupCleanupDeletesCacheSymlinkWithoutFollowingIt` — `FileSystemException: A required privilege is not held by the client`. **Environment limitation.** Fails at `Files.createSymbolicLink` in *setup*, before production code runs; Windows requires `SeCreateSymbolicLinkPrivilege`. The sibling test passes. Would pass on Linux CI.
2. `:verifyLocalization` — `ruby` not on PATH. **Environment limitation.** Localization validity is therefore **unverified**.
3. `:app-desktop:configureDesktopBiometricBridge` — `cmake` not on PATH. **Environment limitation.** The native biometric bridge was never compiled or tested here.

No genuine code defect surfaced from execution.

### Detekt

Genuinely analysed (reports on disk contain real metrics; e.g. `core:crypto` = 11 files, 2,463 LOC, 212 MCC): **328 Kotlin files, 0 findings, 20 modules**. It did **not** crash despite the documented alpha instability. Caveat: this run was fully UP-TO-DATE, so these are cached results for this same commit.

### Freshness caveat

Most tasks were UP-TO-DATE against a pre-populated build directory. They confirm cached outputs match current inputs, not that compilation and tests re-executed now. A `clean` build would be required for a from-scratch guarantee.



---

## 19. Critical Findings

### BKP-01 — Cancellation between Room commit and the `committed` flag deletes the restored vault's attachment blobs

**Severity: Critical | Confidence: High | Type: Data-loss/reliability | Platform: all | Module: `core:database`**
**File:** `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/VaultBackupV2Service.kt:169-199`

```kotlin
169:  activateRestore {
170:      database.useWriterConnection { connection ->
171:          connection.immediateTransaction {
172:              clearVaultTables()
173:              replayMetadataIntoRoom(...)
179:      }  }  }
182:  committed = true
183:  cleanupUnreferencedObjects(stagedPaths)
191:  } finally {
192:      withContext(NonCancellable) {
197:          if (!committed) {
198:              stagedPaths.values.forEach { path -> runCatching { blobStore.delete(path) } }
```

**Root cause.** `committed` is set *after* `activateRestore` returns, but the Room transaction has already committed inside it. `useWriterConnection` unwinding and the `activateStreamingRestore` epilogue are suspension-adjacent. A `CancellationException` delivered in that window leaves `committed == false` while the database *is* committed. The `finally` then deletes every staged attachment object — precisely the files the newly committed `attachment_records` rows point to.

**Threat scenario.** The user taps Cancel during restore (`BackupViewModel.kt:145` calls `importJob?.cancel()`), or the OS cancels `viewModelScope` (process death, navigation), or `clearForLock()` fires at that instant.

**Impact.** Confidentiality: none. Integrity: DB and blob store diverge. **Availability: severe and permanent** — the restored vault is committed but every managed attachment is unreadable, and the user is told the restore *failed*. Silent partial data destruction.

**Existing mitigation.** None. **Currently tested:** no — `VaultBackupServiceTest` has no cancellation test at this window.

**Remediation.** Set `committed = true` *inside* the `activateRestore` lambda immediately after the transaction block returns, and wrap the commit-and-flag region in `withContext(NonCancellable)`. Better: make the flag a post-commit hook so the ordering cannot be got wrong.

**Verification.** Inject a test double that yields on the post-commit path; cancel the import job at that yield; assert either (DB unchanged AND staging deleted) or (DB restored AND all staged objects present) — never a divergence.

**Related:** ATT-01, BKP-06.

---

### ATT-01 — Public unsynchronized `recoverInterruptedOperations()` can mass-delete every attachment object

**Severity: Critical | Confidence: High | Type: Data-loss/reliability | Platform: all | Module: `core:database`**
**File:** `core/database/src/commonMain/kotlin/com/passvault/core/database/attachment/AttachmentRepositoryImpl.kt:300-310`; DI at `shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt:102`

```kotlin
300:  suspend fun recoverInterruptedOperations() {
301:      if (recoveryCompleted) return
302:      attachmentDao.getPendingOperations().forEach { entity ->
305:          blobStore.delete(entity.storagePath)
306:          attachmentDao.deleteById(entity.id)
308:      blobStore.removeUnreferencedObjects(attachmentDao.getReadyStoragePaths().toSet())
```

**Root cause.** Every other entry point wraps recovery in `operationMutex.withLock`; this method carries no visibility modifier on a public class that Koin exposes as a concrete `single<AttachmentRepositoryImpl>`. The reference set for the sweep is derived solely from `storage_state = 'READY'`, so STAGING objects are never referenced.

**Threat scenario.** Any module resolving `AttachmentRepositoryImpl` calls it concurrently with an in-flight import: the import's STAGING row is not READY, so its freshly written object is deleted between `encryptToObject` and `update(ready)`, producing a READY row pointing at a deleted blob. Worse, called during a restore that has cleared `attachment_records`, the READY set is empty and **every** attachment object in the vault is swept.

**Impact.** **Irrecoverable user data loss.** READY rows referencing absent blobs surface later as `AttachmentCorruptedException`.

**Existing mitigation.** Only that no current caller does this. **Currently tested:** no.

**Remediation.** Make it `private`; expose recovery only through `withStableAttachments`. Bind DI to interfaces only (drop the concrete `single`). Include STAGING paths in the reference set.

**Verification.** Concurrency test: start a large import; from a second coroutine invoke recovery after the object is written but before the row update; assert `verify()` never reports a READY row with a missing object.

**Related:** BKP-01, ATT-08.

---

## 20. High Findings

### SES-01 — `lock()` can be blocked indefinitely by a long `withUnlockedSession` block
**High | High confidence | Authentication/session | all | `core:database`**
`VaultRepositoryImpl.kt:382-394`, `258-270`; `DefaultBiometricUnlockService.kt:66-68`

`withUnlockedSession` holds `sessionMutex` across the entire caller-supplied block. `keyStore.enroll` runs an **interactive OS biometric prompt** inside that block, so auto-lock, background lock, and `lockAndRun` block for as long as the user looks at the prompt. `lockWithBoundedRetry` makes 3 attempts but each `lock()` *suspends on the mutex* rather than failing, so it never times out — the vault silently does not lock while plaintext is on screen. `cancelBiometricPromptBeforeLock()` only helps when a real `BiometricPromptController` is injected, and the default is `NoOpBiometricPromptController` (Android and iOS both wire the no-op — see AND-04, IOS-07).

**Fix.** Do not hold the mutex across arbitrary suspending work: guard only lease acquisition, track outstanding leases in a counter, and have `lock()` set `Locking`, cancel/await leases with a bounded deadline, then wipe. Run the enrollment prompt outside the lease.
**Verify.** Assert `lock()` completes within a bounded deadline while a lease is deliberately held.

### SES-06 — Failed auto-lock stalls until the next user activity (fail-open)
**High | High confidence | Authentication/session | all | `shared`**
`shared/src/commonMain/kotlin/com/passvault/shared/security/AutoLockTimer.kt:40-44`

```kotlin
if (lock()) return
onLockFailed()
val nextActivity = latestActivity ?: activitySignal.receiveActivity()
```

If `lockWithBoundedRetry` exhausts its attempts — very plausible given SES-01 — the timer blocks on `receiveActivity()`. With no further user input, which is *exactly* the auto-lock scenario, no retry ever occurs and the vault stays unlocked indefinitely.

**Fix.** Replace the unbounded wait with a bounded retry loop that re-attempts regardless of activity, escalating to a hard fail-closed UI cover after N failures.
**Verify.** Force `lock()` to fail; assert a retry occurs within a bounded interval with zero user activity.

### BKP-02 — Legacy v1 backup path performs ~1 GB pre-authentication allocation from a 128 MiB file
**High | High confidence | Import/parser (DoS) | all | `core:database`**
`VaultBackupService.kt:341-352, 412-432, 483-501, 97-102`

Any file whose first 8 bytes are not the v2 magic is routed to the v1 JSON path, which buffers the whole file, copies it, decodes it to a UTF-16 `String`, JSON-parses it, and Base64-decodes fields up to 256 MiB — **all before Argon2 and before any authentication**. `BackupViewModel.previewImportIfPossible()` auto-invokes inspection after a 300 ms debounce with any non-empty password.

**Impact.** Guaranteed OOM/process kill; on Android the kill occurs while the vault is unlocked.
**Fix.** Sniff for the v1 envelope prefix before buffering; cap the legacy container at 8 MiB or stream-parse it; require an explicit user action for non-v2 files.
**Verify.** Feed a 128 MiB crafted file under a 256 MiB heap; assert clean failure with under 32 MiB peak allocation.

### DSK-01 — No single-instance lock: concurrent Desktop instances share one SQLite vault and two unlock states
**High | High confidence | Data-integrity | Desktop | `app-desktop`**
`Main.kt:20-52`; `Database.desktop.kt:29-71`; `DesktopModule.kt:25-46`

No `FileLock`, no named mutex, no single-instance check anywhere. Room is built without multi-instance invalidation. Two instances share `~/.passvault/vault.db` and `~/.passvault/biometric/`; the enrollment writers are last-writer-wins across processes.

**Impact.** Lost updates and stale reads (per-process invalidation tracker), orphaned platform credentials, unlock-state confusion (closing the visibly locked window while a second unlocked process retains keys in RAM), `SQLITE_BUSY` under WAL contention.
**Fix.** Acquire an exclusive `FileLock` on `~/.passvault/.instance.lock` in `main()` before Koin init; signal the existing instance and exit.

### DSK-02 — Windows bridge integrity is self-referential: SHA-256 stored beside the DLL in a user-writable directory
**High | High confidence | Supply-chain | Windows | `app-desktop`**
`DesktopBiometricNativeLoader.kt:27-59, 178-180`; `build.gradle.kts:114-120, 1259`

The loader reads both `bridge.properties` and the DLL from the same directory, and with `perUserInstall = true` that directory is writable by any process running as the user. The Developer-ID fallback is macOS-only; the Windows policy is `sha256` with no Authenticode check at runtime.

**Impact.** Same-user malware replaces the DLL and rewrites the digest; the loader accepts it and `Native.load` maps it into the JVM. A trojan implementing `pv_bio_retrieve` exfiltrates the 32-byte vault key on every biometric unlock. **Full master-key compromise, no elevation required.**
**Fix.** Verify Authenticode with a pinned publisher thumbprint via `WinVerifyTrust`; pin the expected digest in signed Kotlin code rather than an adjacent editable file; prefer per-machine install.

### DSK-03 — macOS entitlements disable library validation, defeating the bridge's own Developer-ID check
**High | High confidence | Supply-chain | macOS | `app-desktop`**
`app-desktop/resources/macos/PassVault.entitlements:13-18`

`com.apple.security.cs.disable-library-validation` plus `allow-unsigned-executable-memory` surrender the OS guarantee that no other dylib can be injected — the exact guarantee the loader spends significant effort emulating. Mitigating: `allow-dyld-environment-variables` and `cs.debugger` are absent, so `DYLD_INSERT_LIBRARIES` and `task_for_pid` are still blocked. No App Sandbox key is present, so there is no filesystem containment around `~/.passvault`.

**Fix.** Remove both entitlements; sign every bundled native (Skiko, JNA, SQLite JNI, biometric bridge) with the same Team ID.

### DSK-04 — Desktop `lock()` only iconifies; decrypted content stays in OS thumbnails and capture APIs
**High | High confidence | Platform-security | Desktop | `core:security`**
`DesktopWindowProtection.kt:93-103` vs `116-132`

`lock()` sets `Frame.ICONIFIED` and requests content security asynchronously; it never installs the opaque glass pane that `prepareForShutdown()` already implements. Windows DWM keeps a live composited surface for taskbar thumbnails and Aero Peek; macOS Mission Control captures the last rendered frame.

**Impact.** Plaintext credential disclosure via window previews and `CGWindowListCreateImage`-class APIs for up to 5 seconds after lock — or indefinitely if the Compose acknowledgement never arrives.
**Fix.** Install the existing curtain **synchronously inside `lock()`**, before changing `extendedState`, removing it only on unlock. One-line reuse of existing machinery.

### DSK-05 — Clipboard secrets are not excluded from Windows Clipboard History / Cloud Clipboard or macOS managers
**High | High confidence | Secret-exposure | Desktop | `core:security`**
`DesktopClipboardService.kt:58-77, 187-199`

Only `stringFlavor` plus a JVM-local ownership marker is published. Repo-wide grep for `ExcludeClipboardContentFromMonitorProcessing`, `CanIncludeInClipboardHistory`, `CanUploadToCloudClipboard`, `org.nspasteboard.ConcealedType`: **zero matches**.

**Impact.** A copied password is stored in Windows Clipboard History (Win+V) and synced by Cloud Clipboard to every signed-in device. The 20-second timer clears only the live clipboard, not the history entry or the synced copy — defeating the clipboard-clearing control entirely.
**Fix.** Native clipboard write registering the three Windows exclusion formats and the macOS `ConcealedType` UTI; warn in Settings when unavailable.
*(The ownership design itself is excellent — see SAFE list.)*

### IOS-01 — Encrypted backup staging written to `Caches` with no `NSFileProtection` attribute
**High | High confidence | Storage | iOS | `shared`**
`IosBackupFileStore.kt:364-378, 59-67, 89-112`

```kotlin
367: fileManager.createDirectoryAtPath(path = directory, withIntermediateDirectories = true,
370:     attributes = null,          // no NSFileProtectionKey
```

Every other iOS storage path passes `NSFileProtectionComplete` (`Database.ios.kt:43,47,125-127`; `AttachmentStorageProvider.ios.kt:25,30,34`; `IosAttachmentFileStore.kt:106,317,328`). This is the sole omission, so files fall back to `CompleteUntilFirstUserAuthentication` — readable whenever the device has been unlocked once since boot, including while the screen is locked.

**Impact.** The whole-vault `.pvault` ciphertext plus KDF parameters sit at a weaker protection class than the live database, converting a device-bound threat into an offline brute-force target.
**Fix.** Pass protection attributes at directory creation and `setAttributes` on the produced file; same for the import path.

### IOS-02 — No entitlements file, so the container default protection class is weak
**High | High confidence | Platform-security | iOS | `iosApp`**
Grep of `project.pbxproj` for `ENTITLEMENTS`: **0 matches**; no `*.entitlements` file exists.

Without `com.apple.developer.default-data-protection = NSFileProtectionComplete`, any file not *explicitly* attributed inherits `CompleteUntilFirstUserAuthentication`. Currently un-attributed: `Library/Preferences/com.passvault.ios.plist` (all settings **and** the `biometric.unlock.enabled.<vaultId>` marker), the document-picker intermediate copy, and the backup staging tree (IOS-01).

**Fix.** Add an entitlements file with `default-data-protection`, reference it via `CODE_SIGN_ENTITLEMENTS` in both configurations, and declare `keychain-access-groups` explicitly.


---

## 21. Medium Findings

### CRY-02 — Master password is copied into two unwipeable JVM `String`s before Argon2
**Medium | Confirmed | Secret-exposure | all | `core:crypto`, `core:domain`**
`ValueTypes.kt:56-58`; `LibsodiumCryptoEngine.kt:59` (**independently verified by the lead auditor**)

```kotlin
// ValueTypes.kt
56:  fun toUtf8ByteArray(): ByteArray {
57:      return characters.concatToString().encodeToByteArray(throwOnInvalidSequence = true)
// LibsodiumCryptoEngine.kt
59:      val passwordHex = password.toHexString()
```

`SensitiveText` stores a `CharArray` precisely to avoid this, then defeats it at the one moment that matters. `concatToString()` materialises the plaintext password as an immutable `String`; `toHexString()` then materialises a **second** immutable copy (hex-encoded, trivially recognisable in a heap dump). Neither is reachable for wiping. Every derived `ByteArray` *is* correctly wiped, which makes the gap easy to miss.

**Impact.** The master password persists on the heap until GC and appears in heap dumps, core dumps, swap, and hibernation files. This is the highest-value secret in the system.
**Fix.** Encode UTF-8 directly from the `CharArray` without an intermediate `String`, and pass raw bytes to libsodium instead of hex.
**Verify.** Assert a `CharArray`-based API exists and that no `String` equal to the password is reachable after derivation (best-effort heap scan under a serial GC).

### DB-01 — Vault-wide plaintext metadata graph in an unencrypted SQLite file
**Medium | High | Privacy/storage | all | `core:database`**
`CredentialRecordEntity.kt:29-30,58-71`; `schemas/3.json:116,441,501`

Not SQLCipher. An attacker with the file learns, without the master password: entry count, per-type composition, favourites, the complete folder tree, the complete credential-to-tag graph, and millisecond-precision created/updated/last-used timestamps. Combined, this is a behavioural profile sufficient for targeted phishing and for deciding whether the vault merits an offline attack.

*Correctly documented in `SECURITY_MODEL.md:82-86` — but omitted from `SECURITY.md`, the readiness-audit database row, and `PENTEST.md` entirely (DOC-05).*
**Fix.** SQLCipher or platform file encryption for defence in depth; move `type`/`is_favorite`/`folder_id` into the encrypted summary; coarsen timestamps in columns.

### DB-02 — Argon2 parameters, `entry_count`, and `vault_id` disclosed and unauthenticated
**Medium | High | Key-management | all | `core:database`**
`VaultMetadataEntity.kt:19-35,55-56`

KDF parameters must be readable pre-unlock, but they are not bound into any AAD. `vault_id` links copies of the same vault across leaks; `entry_count` advertises vault value for free. An attacker with write access can downgrade the parameters; the strict `require` bounds in `validateMetadataForUnlock` are therefore **security-critical load-bearing code**.
**Fix.** Bind the KDF block into the `wrapped_vek` AAD so tampering fails authentication loudly; drop `entry_count`; make `vault_id` non-stable across exports.

### DB-03 — Ciphertext is unpadded: password and field lengths leak
**Medium | High | Privacy | all | `core:database`**
`PasswordHistoryRecordEntity.kt:40-44`; `CredentialRepositoryImpl.kt:565-583`

No padding exists between serialization and `encrypt`. AEAD ciphertext length equals plaintext length plus a constant, so every current and historical password length is disclosed to within a byte — a direct multiplier on targeted guessing, and a clear signal for "this one is a 4-digit PIN".
**Fix.** Pad to bucketed sizes (next multiple of 32, or Padmé) before encryption; strip after decryption. Apply to secret, summary, history, and filename payloads.

### DB-04 — `title_hash` blind index is stored and indexed but never queried
**Medium | High | Privacy | all | `core:database`**
`CredentialRecordEntity.kt:15,32-36`; `CredentialRepositoryImpl.kt:585-588,1288-1300`

Grep across all DAOs: **no query references `title_hash`** (unlike `folder_records.name_hash` and `tag_records.name_hash`, which are used). It is **not** brute-forceable — the derivation is VEK-keyed BLAKE2b with domain separation and length prefixing, so precomputing `H("gmail")` is infeasible. But it provides a deterministic **equality and cross-snapshot linkage oracle** on titles for **zero functional benefit**, and the dedicated index makes exploitation trivial. Because the VEK survives password changes, the same title yields the same hash across every backup forever.
**Fix.** Drop the column and index in a v4 migration. If reintroduced, salt per record.

### ATT-02 — `storage_state` is unauthenticated; forced `LEGACY` downgrade denies content and skips blob deletion
**Medium | High | Data-integrity | all | `core:database`**
`AttachmentRepositoryImpl.kt:205-207, 329-336`; `AttachmentRecordEntity.kt:71-73`

Blob content is strongly bound, and `sizeBytes`/`mimeType` tampering fails closed because both are authenticated. `storageState` participates in **no** MAC. An attacker with DB write sets `LEGACY`; the UI reports "legacy unavailable" and a subsequent user delete removes the row **without** deleting the object.
**Fix.** MAC the row metadata under a VEK subkey, or at minimum treat `LEGACY` plus current `contentFormatVersion` plus a managed-shaped `storagePath` as tampering; always attempt blob deletion on delete.

### ATT-03 — One undecryptable filename bricks import and rename for the entire credential
**Medium | High | Data-loss/reliability | all | `core:database`**
`AttachmentRepositoryImpl.kt:80, 361-376`

```kotlin
80:  val existingNames = attachmentDao.getByCredential(credentialId.value).map { decryptFilename(it, vek) }
```

Uniqueness computation eagerly decrypts **all** sibling filenames with `getOrThrow()` and no per-row isolation; `getByCredential` deliberately includes `LEGACY` rows whose encoding predates the current scheme. One corrupted, legacy, or attacker-flipped row makes the whole `map` throw. `validateFileName` at line 371 additionally rejects legitimately stored historical names containing `:` or `\`.

**Impact.** Permanent, non-obvious denial of attachment add/rename on that credential, escalating with vault age, with no UI recovery path.
**Fix.** Use `mapNotNull { runCatching { ... }.getOrNull() }` for the uniqueness set; report unreadable rows separately; do not apply write-path policy as a hard gate on historical reads.

### ATT-04 — Filename policy allows Windows reserved device names and trailing dots
**Medium | Medium | Data-loss | Desktop/Windows | `core:domain`**
`AttachmentRepository.kt:89-99`

The denylist covers separators and control/format code points but not Win32 namespace semantics. Accepted today: `CON`, `PRN`, `AUX`, `NUL`, `COM1`-`COM9`, `LPT1`-`LPT9` (with or without extension), trailing dots (`report.pdf.` — `.trim()` strips whitespace, not dots), all-dot names, and `* ? " < > |`.

**Impact.** Export to `NUL` silently discards the file; `a.txt.` normalises onto `a.txt` and overwrites it. The `uniqueFileName` guard compares raw strings and does not model this normalisation.
**Fix.** Reject reserved stems case-insensitively, trailing dot/space, all-dot names, and Win32 wildcards; apply the same normalisation in the collision check.

### AND-01 — Attachment plaintext staged in `cacheDir` survives 10 minutes and is not cleaned on process death
**Medium | Confirmed | Secret-exposure | Android | `app-android`**
`AndroidAttachmentFileStore.kt:229-233, 305-319, 326-328`

Deletion is an in-process `delay(600_000)` on a plain scope — not WorkManager, not an alarm. Any process death in that window (swipe-kill, low-memory kill, crash, reboot) means it never runs. Notably, `PassVaultApplication.onTrimMemory` deliberately locks the vault under memory pressure, making memory-pressure kill a *likely* path. The startup sweep is correct and tested but only fires on next launch.
**Fix.** Prefer a `ContentProvider`-streamed handoff so no plaintext touches disk; otherwise use a persistent WorkManager job, sweep in `onTrimMemory`, and shorten the 10-minute lifetime.

### AND-03 — Biometric key does not request StrongBox and relies on an implicit GCM-IV default
**Medium | Confirmed | Biometric | Android | `app-android`**
`AndroidBiometricKeyStore.kt:263-286`

No `setIsStrongBoxBacked(true)`, so the key lives in the default KeyMint implementation — typically TEE-backed but on some devices software-backed. Separately, `setRandomizedEncryptionRequired` is not called; the default is `true` and the code correctly reads back the system IV, so this is **not currently exploitable**, but relying on an implicit default for a GCM IV-reuse guarantee is fragile.

*Correct here:* per-operation authentication (timeout 0), `setInvalidatedByBiometricEnrollment(true)`, and `BIOMETRIC_STRONG` only — no WEAK, no DEVICE_CREDENTIAL.
**Fix.** Add an explicit `setRandomizedEncryptionRequired(true)` and a StrongBox attempt with `StrongBoxUnavailableException` fallback.

### AND-04 / IOS-07 — `BiometricPromptController` is wired to a no-op on the platforms that can actually cancel
**Medium | Confirmed | Biometric | Android, iOS | `app-android`, `shared`**
`AndroidModule.kt:52`; `IosModule.kt:23`

Desktop wires a real implementation; Android and iOS — which both *have* cancellable prompts — wire `NoOpBiometricPromptController`. `AndroidBiometricKeyStore` already calls `prompt.cancelAuthentication()` on cancellation, so the machinery exists but is not exposed to the repository. Combined with SES-01, a lock transition cannot pre-empt a pending prompt.
**Fix.** Have the platform key stores implement the interface and bind them.

### SES-03 — Failed-attempt throttle is volatile, trivially reset, and capped at 5 seconds
**Medium | High | Authentication/session | all | `core:database`**
`VaultRepositoryImpl.kt:50, 186-188`

`failedAttempts` is a plain in-memory field. Killing and relaunching resets it to zero. The 5-second ceiling is small relative to a single Argon2id run, so the marginal work added is near zero.

**What it does protect:** naive online guessing through the UI within one process lifetime. **What it does not protect:** anything offline. An attacker with the DB file reads `argon2Salt`/`wrappedVek` and brute-forces entirely outside this code path, where **Argon2id is the sole defence**.
**Fix.** Persist the counter and a timestamp in vault metadata, written in the same transaction as the failure, with exponential backoff resistant to clock rollback.

### SES-02 — `lock()` cannot pre-empt an in-flight unlock; a session opens after lock was requested
**Medium | High | Authentication/session | all | `core:database`**
`VaultRepositoryImpl.kt:172-211, 258-270`

A background lock arriving during Argon2 derivation (or during the throttle delay) waits on the mutex; the unlock then succeeds and publishes `Unlocked`. `UnlockViewModel` emits `NavigateToVault` on that transient state, so the app can navigate into vault content after the OS asked to lock.
**Fix.** Increment a `lockRequestedGeneration` outside the mutex in `lock()`; at `openSession`, if the generation changed, wipe the candidate and fail instead of opening a session.

### DOM-02 — Master-password minimum is length-only, with no entropy floor at the authoritative boundary
**Medium | High | Authentication | all | `core:domain`**
`MasterPasswordPolicy.kt:11-15`

```kotlin
fun accepts(length: Int): Boolean = length in MIN_LENGTH..MAX_LENGTH   // 12..1024
```

`"passwordpass"` passes the policy. Only *some* callers additionally require `score >= FAIR`; `VaultRepositoryImpl.kt:86` enforces length only.

Offline cost at Argon2id 64 MB / t=3 / p=1 (approx. 500 H/s per high-end GPU; 1,000-GPU cluster = 5x10^5 H/s):

| Secret | Entropy | Time to exhaust |
|---|---|---|
| Human 12-char, weak (`Summer2024!!`) | ~28 bits | **~9 minutes** |
| Human 12-char, typical | ~35 bits | **~19 hours** |
| Human 12-char, good | ~40 bits | ~25 days |
| Generated 12-char, 4 sets | 77.5 bits | 1.1x10^10 years |

Adequate for generated secrets; inadequate for human-chosen ones.
**Fix.** Raise the minimum to 14-16 **and** enforce `score >= GOOD` inside `MasterPasswordPolicy` so every call site inherits it; consider promoting the master key to the `MODERATE` (256 MB) Argon2 profile.

### DOM-04 — `PasswordStrengthEvaluator` diverges materially from true entropy
**Medium | High | Correctness/usability | all | `core:domain`**
`PasswordStrengthEvaluator.kt:20-24, 34-39, 94-104`

`VERY_STRONG` is reachable at 5 of 8 points. A 20-char lowercase passphrase (~94 bits) scores GOOD, while a 12-char 4-class string with ~40 bits of human entropy scores STRONG — the classic length-versus-classes inversion. The common-token list has 9 entries versus zxcvbn's ~30,000, so `"Ferrari2019!"` rates STRONG.

**Impact.** Users are steered toward short complex passwords over long simple ones — the anti-pattern NIST SP 800-63B deprecates. This directly weakens DOM-02.
**Fix.** Weight length dominantly; never award VERY_STRONG below 16 code points; consider vendoring zxcvbn.

### HLT-01 — Password-health analysis sprays plaintext across the heap in unwipeable `String`s
**Medium | High | Secret-exposure | all | `feature:health`**
`PasswordHealthAnalyzer.kt:128-133`; `PasswordStrengthEvaluator.kt:14-18`

The `CharArray` is dutifully wiped, but `concatToString()` plus `lowercase()` plus `buildString` produce three or more immutable copies **per credential**. A 500-entry vault scatters ~1,500 plaintext passwords across the heap. Additionally every password in the vault is decrypted **simultaneously** for the duration of the reuse sort, maximising heap-dump exposure.
**Fix.** Make `PasswordStrengthEvaluator.score` operate on `CharArray`; stream the reuse comparison rather than materialising the whole vault at once.

### UI-02 — `SecureTextField` lacks password semantics and autofill/content-capture opt-out
**Medium | Medium-High | Secret-exposure | all | `core:designsystem`**
`SecureTextField.kt:43-44, 78-113`

No `Modifier.semantics { password() }`, no explicit `contentType`, no content-capture suppression. `KeyboardType.Password` does suppress keyboard learning — partial mitigation only. Masking is `visualTransformation`, which is presentation-only and does **not** remove text from the semantics tree. **While revealed, a screen reader (or any enabled accessibility service — a common malware vector) can announce the password character by character.** The `String`-typed API also allocates a new immutable copy per keystroke, retained in recomposition and undo state.
**Fix.** Add `semantics { password() }`, set `contentType = ContentType.Password`, and migrate to `BasicSecureTextField` with `TextFieldState`.

### OTP-01 — Digit-to-modulus mapping is a hard-coded branch, not derived from `digits`
**Medium | High | Correctness | all | `core:otp`**
`TotpService.kt:89-93`

```kotlin
val modulus = if (configuration.digits == DEFAULT_DIGITS) ONE_MILLION else ONE_HUNDRED_MILLION
```

Correctness depends entirely on `SUPPORTED_DIGITS = setOf(6, 8)` declared 385 lines away. Adding `7` — a one-token, obviously safe-looking edit — makes a 7-digit account compute `% 10^8` then `padStart(7)`, silently emitting an 8-character wrong code with no error. Nothing asserts `value.length == digits`.
**Fix.** Derive `10^digits` from a table with a range `require`; assert the output length.

### OTP-02 — TOTP seed materialised as an unwipeable `String` on every generation, once per second
**Medium | High | Secret-exposure | all | `core:otp`**
`TotpService.kt:55, 284-292, 316-317`

The module is otherwise meticulous about zeroisation, but the Base32 API takes and returns `String`. Each `generate()` creates at least three unwipeable heap copies. Both tickers regenerate **once per second**, so a minute on the authenticator screen with N accounts accumulates roughly 180*N seed copies.
**Fix.** Overload `Base32Codec` to accept and return `CharArray` so the intermediate is wipeable.

### BKP-03 — Unbounded validator state: up to ~3 GB of retained identifiers
**Medium | High | Import/parser (DoS) | all | `core:database`**
`VaultBackupService.kt:735-748, 750-775`

The streaming validator avoids holding payloads but retains *every* identifier, bounded only by `MAX_ENTITY_COUNT = 1,000,000` per category. At ~600 B per entry across credentials, tag-ref pairs, history IDs, and attachment IDs, retained heap exceeds 2-3 GB against a far smaller on-disk cost — a 10-20x file-to-heap amplification.
**Fix.** Derive an additional ceiling from `declaredSizeBytes`; store truncated digests rather than full `String`s; cap aggregate retained identifier bytes.

### BKP-04 — Attacker-chosen Argon2 parameters honoured up to 256 MiB and ops 10
**Medium | High | Import/parser (DoS) | all | `core:database`**
`BackupV2Codec.kt:322-334, 395-398`

Bounds **are** checked before the KDF — the correct structure. The issue is the ceiling: legitimate exports only ever produce 64 MiB / ops 3-4, so a malicious 44-byte header can demand 4x the memory and ~3x the work of any real backup. Preview auto-fires on every keystroke after a 300 ms debounce, and cancellation cannot interrupt the native `pwhash` already in flight.
**Fix.** Reject `memLimit > 96 MiB` / `opsLimit > 5` for format 2; gate preview behind an explicit button.

### BKP-05 — Restore has no free-space precheck; up to 16 GiB staged before any DB change
**Medium | High | Data-loss/reliability | all | `core:database`**
`VaultBackupV2Service.kt:307-315, 589-609`

Staged bytes are bounded only by the 16 GiB container limit and are written *in addition to* the existing vault's objects. No free-space check exists anywhere in the path.
**Fix.** Compute required staging bytes from the authenticated manifest before pass 1 and check available space; enforce a running staged-bytes budget.

### BKP-06 — Legacy v1 restore orphans every attachment blob on disk
**Medium | High | Privacy/data-retention | all | `core:database`**
`VaultBackupService.kt:939-941`; `VaultBackupDao.kt:212-240`

`replaceVault` deletes attachment rows, but **no call to `removeUnreferencedObjects` exists on the v1 path** (contrast `VaultBackupV2Service.kt:183`). Every encrypted object from the previous vault remains in app-private storage forever, unreferenced and invisible. The user believes the old vault is gone — a data-retention and erasure problem.
**Fix.** Call `removeUnreferencedObjects(emptySet())` after the v1 transaction commits.

### BKP-07 — `clearVaultTables()` destroys version/migration/corruption bookkeeping that is never restored
**Medium | High | Data-integrity | all | `core:database`**
`VaultBackupV2Service.kt:468-480`

`migration_state`, `current_version_info`, and `corruption_logs` are deleted, and the backup format has no record type to restore them. Post-restore, `current_version_info` is empty; code treating a missing row as "unknown schema version" may trigger a spurious migration or refuse to open the vault. Forensic corruption history is silently destroyed.
**Fix.** Re-seed `current_version_info` at the end of the restore transaction, or stop deleting tables that describe the *database file* rather than vault contents.

### CI-01 — Production desktop artifacts are rebuilt, not promoted byte-identically from the tested candidate
**Medium | High | Supply-chain | Desktop | `.github`**
`testing-release.yml:159-251` vs `release.yml:153-204, 879-882, 966-1015`

Provenance binds to **source commit plus tree**, not artifact bytes. The `candidate-desktop-*` artifacts are never downloaded, hashed, or compared in `release.yml`; the production run hashes whatever it just built. Mobile does this correctly (promote, never rebuild). Linux `.deb`/`.rpm` are never code-signed, so there is no cryptographic backstop there.

**Impact.** Anything compromising the production runner or its Gradle cache is signed and attested as legitimate; Kotlin/Compose desktop builds are not bit-reproducible, so divergence is undetectable.
**Fix.** Promote the candidate artifacts and sign those exact bytes, or assert equality of the unsigned payload before signing.

### CI-02 — Unattended dispatch-to-production chain; branch promotion holds `contents: write` with no environment gate
**Medium | High | Supply-chain | all | `.github`**
`candidate-readiness.yml:192-201, 254-274`

Sibling jobs `google-play` and `testflight` **are** environment-gated; `promote-release-branch` — which pushes the protected `release` branch and dispatches the production chain — is **not**, and additionally holds `actions: write`. One `workflow_dispatch` therefore reaches desktop code signing, Play production promotion, and App Store submission with `automatic_release: true`, with no further human approval beyond the terminal `mobile-production` environment.
**Fix.** Add `environment: release-promotion` with required reviewers; reduce `actions: write`; protect `refs/heads/release`; add a `CODEOWNERS` file (currently absent).

### CI-03 — Dependency verification is checksum-only, and the repo's own gate forbids enabling signatures
**Medium | High | Supply-chain | all | `gradle`, root build**
`gradle/verification-metadata.xml:4-10`; `build.gradle.kts:28-30, 49-51`

Coverage is genuinely broad — 1,483 components / **2,946 artifacts**, 1:1 SHA-256, no weak digests, plugins and the full KMP native graph included. But every hash is `origin="Generated by Gradle"`, i.e. trust-on-first-use against whatever bytes the generating machine received. PGP is not merely off: `VerifyDependencyMetadataTask` **hard-fails the build if anyone enables it** and rejects any `<pgp>` element.

**Impact.** If any artifact was already malicious when the metadata was generated, the checksum permanently ratifies it, and the gate blocks remediation via signatures.
**Fix.** Change the gate from *requiring* `false` to *permitting* `true`; migrate to `verify-signatures=true` with a trusted-key allowlist; regenerate on a clean host.

### CI-04 — Dependabot covers only GitHub Actions; Gradle and Ruby dependencies are never updated
**Medium | High | Supply-chain | all | `.github/dependabot.yml:1-10`**

No `gradle` ecosystem entry and no `bundler` entry. A CVE in libsodium-bindings, okio, JNA, Room, or a Fastlane gem (which handles signing keys and store credentials) produces no notification. The 2,946 pinned checksums make upgrading high-friction, further biasing toward staleness.
**Fix.** Add `gradle` and `bundler` ecosystems; enable Dependabot security alerts; add `dependency-review-action` on PRs.

### CI-05 — detekt 2.0.0-alpha.5 is the only static-analysis gate, and it is not a security analyser
**Medium | High | Testing gap | all | root build, `detekt.yml`**

The build file itself documents a known alpha crash and disables parallelism to work around it. None of the disabled rules is security-relevant, and `warningsAsErrors: true` is strict — but an alpha analyser that crashes or silently drops files can **pass while analysing nothing**, and the `setSource(...)` reassignment means a path-resolution drift would report success on an empty file set with no minimum-files assertion. More importantly, detekt is a style and complexity linter: there is **no CodeQL, no Semgrep, no secret scanning, no dependency review, no SBOM** anywhere.
**Fix.** Pin a stable release; add CodeQL for Kotlin/Java wired into the required gate; enable secret scanning with push protection; assert a non-zero analysed-file count.

### CI-06 — Gradle caching enabled in code-signing jobs with no read-only or encryption settings
**Medium | Medium | Supply-chain | all | `.github`**
`release.yml:195-198, 770-773`; `mobile-store-release.yml:319-321`

Zero occurrences of `cache-read-only`, `cache-disabled`, or `cache-encryption-key` across all workflows. Signing jobs restore a Gradle home — including build cache and configuration cache, which store executable classes and serialized task graphs — populated by other runs, while the Authenticode certificate and signing tokens are live. GitHub isolates *fork* caches, which is why this is Medium rather than High, but nothing in the repo establishes that boundary.
**Fix.** `cache-disabled: true` on signing jobs; `cache-read-only: true` on PR-triggered jobs.

### IOS-03 — Keychain VEK orphaned when the `NSUserDefaults` marker is lost
**Medium | Confirmed | Key-management | iOS | `shared`**
`IosBiometricKeyStore.kt:118-120, 177-188`

`NSUserDefaults` is treated as the source of truth for enrollment, but iOS **wipes user defaults on app deletion while preserving Keychain items**. The marker is also cleared *before* `deleteKeychainItem`, so a transient failure permanently orphans the item: `contains()` now returns false and nothing retries.

**Impact.** The 32-byte VEK survives uninstall and persists into every subsequent install of the same bundle ID, defeating the user's revocation action.
**Fix.** Make the Keychain authoritative via a non-prompting existence probe; delete the Keychain item first and clear the marker only on success; sweep orphans on first launch.

### IOS-04 — Sensitive clipboard destroyed on every `willResignActive`, breaking paste-to-other-app
**Medium | Confirmed | Usability/security tradeoff | iOS | `shared`**
`IosAppLifecycleBridge.kt:89-92`

The `finally` runs `clear()` unconditionally, including the `lockVault = false` path from `applicationWillResignActive` — which fires on the first frame of any app switch. The core workflow (copy password, switch to Safari, paste) therefore yields an empty string.

**Impact.** Users are pushed to documented-insecure workarounds: reading the password on screen and retyping it, disabling auto-lock, or choosing a memorable weaker master password. A control that breaks the primary workflow gets routed around.
**Fix.** Gate the `clear()` on `lockVault`; rely on the already-correct `UIPasteboardOptionExpirationDate` for the resign-active case.

### IOS-05 — Permanent opaque-cover lockout after two consecutive cleanup failures
**Medium | Confirmed | Availability | iOS | `shared`**
`IosAppLifecycleBridge.kt:128-154`

The retry budget is exactly one, and `succeeded` is false on *acknowledgement timeout* as well as on lock failure. On the second failure no replacement launches, `onReady()` is never invoked, and the cover never lifts. Nothing re-triggers because the app is already `.active`.

**Impact.** The user faces a blank rectangle with no controls, no error, and no recovery except force-quit. Fails closed — the right direction — but with no escape hatch.
**Fix.** Bounded attempt counter with backoff, then a user-visible retry affordance; distinguish lock failure from acknowledgement timeout.

### IOS-06 — `ENABLE_USER_SCRIPT_SANDBOXING = NO` with an unpinned Gradle shell phase in Release
**Medium | Confirmed | Supply-chain | iOS | `iosApp`**
`project.pbxproj:210, 291, 166-184`

Script sandboxing is off in both configurations and the framework phase declares no `inputPaths`/`outputPaths` while invoking `./gradlew` with full user privileges. Anything able to write to the repository — a malicious Gradle plugin, a poisoned cache, a PR touching build logic — executes arbitrary code during a **Release** archive and can inject a modified `PassVaultShared.framework` into a signed, notarised binary.
**Fix.** Enable sandboxing, declare explicit I/O paths, checksum-gate the wrapper jar, run archives on ephemeral CI.

### DSK-06 — Windows bridge resolves imports via default DLL search order from a user-writable directory
**Medium | High | Supply-chain | Windows | `app-desktop`**
`CMakeLists.txt:81-96`; `passvault_biometric_windows.cpp:1199-1200`

`webauthn.dll` is loaded correctly with `LOAD_LIBRARY_SEARCH_SYSTEM32`, but the static import table is not hardened: no `/DEPENDENTLOADFLAG:0x800`, no `SetDefaultDllDirectories`. `runtimeobject.dll` and `windowsapp.dll` are not KnownDLLs, and with per-user install the app directory is user-writable. This bypasses DSK-02's checksum entirely, since the checksum covers only the bridge itself.
**Fix.** Add `/DEPENDENTLOADFLAG:0x00000800` and call `SetDefaultDllDirectories` early.

### DSK-07 — Decrypted attachment previews persist in the shared temp directory across crashes
**Medium | Confirmed | Secret-exposure | Desktop | `app-desktop`**
`DesktopAttachmentFileStore.kt:235-282`

Cleanup depends on a delayed coroutine plus `deleteOnExit`, neither of which runs on `SIGKILL`, power loss, OOM-kill, or `TerminateProcess`. `DesktopShutdownCoordinator` has **no** cleanup task for the attachment store (`CLEANUP_TASK_COUNT = 3` covers vault/DB, clipboard, biometric only). Note `protectPrivateDirectory` is a **no-op on Windows** (the POSIX view is null there).
**Fix.** Register a fourth shutdown task; shorten the lifetime; apply an explicit owner-only DACL on Windows.

### DSK-08 — `~/.passvault` creation has a permission TOCTOU window; Windows gets no explicit ACL
**Medium | Confirmed | Storage | Desktop | `core:database`, `shared`**
`Database.desktop.kt:30-51`; `AttachmentStorageProvider.desktop.kt:11-26`

Create-then-chmod in all three places, so the directory momentarily exists at `0755` before the `chmod`. `Files.createDirectory(path, PosixFilePermissions.asFileAttribute(...))` — which the *preview* code already uses correctly — is not used here. On Windows `PosixFileAttributeView` is null, so `~/.passvault` and `vault.db` carry only inherited profile ACLs, weaker than the explicit DACL the native bridge applies to `~/.passvault/biometric`.
**Fix.** Create atomically at 0700; apply an explicit owner-only DACL on Windows.

### DSK-10 — 30-second hardcoded unattended window on focus loss, unrelated to the user's auto-lock setting
**Medium | Confirmed | Authentication/session | Desktop | `app-desktop`**
`PassVaultDesktopWindow.kt:240-247, 445`

`DEFAULT_FOCUS_LOCK_DELAY_MS = 30_000L` is a compile-time constant with no settings binding; `autoLockTimeoutMinutes` drives only the idle timer. Re-focusing restarts the clock, so a partially attentive user keeps the vault unlocked indefinitely. Additionally, focus-loss locking is fully suppressed while a biometric prompt is active, with no ceiling (the macOS prompt has no timeout at all).
**Fix.** Derive the delay from the user's setting with a low default and a cap; bound the prompt-based suppression.

### BKP-08 — Restored attachment objects are never verified against the vault key
**Medium | High | Data-integrity | all | `core:database`**
`VaultBackupV2Service.kt:63-77` vs `589-664`

Export verifies each inner container end-to-end; restore validates only the outer record layer, declared byte counts, and chunk counts. The inner `AttachmentContainerCodec` layer and its ownership binding are never exercised on import.
**Fix.** Run `decryptObject` against each staged object during pass 1 using the VEK from the backup's metadata row.

### BKP-09 — Backup password may equal the vault master password; no check exists
**Medium | High | Key-management | all | `core:domain`, `feature:backup`**
`BackupPasswordPolicy.kt:4-22`; `BackupViewModel.kt:562-575`

The policy has no reference to the master password and no comparison hook. Users will reuse it. The backup then sits in cloud storage protected by Argon2id(64 MiB, ops 3), and cracking it yields the **master password** — the whole vault, not just the snapshot. `BACKUP_FORMAT.md:24` claims independence, but that independence is structural, never enforced.
**Fix.** Constant-time compare against the unlocked session's master password and reject equality with an explanatory message.

### DOC-01 — `PENTEST.md` states Desktop biometric unlock does not ship
**Medium | Confirmed | Documentation mismatch | Desktop | `PENTEST.md:42-43, 52`**

> "Desktop Windows Hello/Touch ID ... *if ever added*"; "**No Desktop keyring or biometric unlock adapter ships now**."

A full Windows WebAuthn-PRF C++ bridge (`passvault_biometric_windows.cpp`) and a macOS Touch ID path ship in this tree. A security-review document asserting that the most privileged new attack surface does not exist is itself a security defect — reviewers scope work from this file. `PENTEST.md:50` similarly lists attachment storage as a non-feature.
**Fix.** Rewrite the platform-coverage section; add the desktop biometric bridge to the review register.

### DOC-02 — Crypto marked VERIFIED while `Argon2Test` never calls Argon2
**Medium | Confirmed | Documentation mismatch / testing gap | all | `docs/PRODUCTION_READINESS_AUDIT.md:28`**

The readiness audit marks "libsodium engine, **KDF**, envelopes, AAD, subkeys" as VERIFIED. `Argon2Test.kt:9-86` invokes no Argon2 function and contains zero known-answer vectors — it asserts data-class equality and `clear()`. Combined with assertion-free crypto tests and ~15% of tests running a stub XOR engine, "VERIFIED" is not earned for the KDF.
**Fix.** Add RFC 9106 KAT vectors; restate the status honestly.


---

## 22. Low Findings

| ID | Title | File:line | Note |
|---|---|---|---|
| CRY-03 | `finalize()` on `SensitiveText` is a false assurance | `ValueTypes.kt:121-123` | Non-deterministic; disabled by default on JDK 18+; no-op on Native/JS. Encourages skipping explicit `clear()`. Remove; add a scoped `use { }` API. |
| CRY-04 | `benchmarkArgon2` can only *lower* cost on fast devices | `LibsodiumCryptoEngine.kt:310-315` | Fast devices get ops 4 but memory stays 64 MB. Consider raising memory on capable hardware. |
| CRY-05 | Argon2 parallelism forced to 1, undocumented | `VaultRepositoryImpl.kt:71-74` | Correct given the binding lacks the parameter, but disclosed nowhere. |
| ATT-05 | Filename AAD uses unprefixed concatenation | `AttachmentRepositoryImpl.kt:415-416` | `(id="a", cred="b:filename:v1")` collides with `(id="a:b", cred="filename:v1")`. The content AAD does this correctly. Latent. |
| ATT-06 | `LEGACY` rows escape quota accounting | `AttachmentDao.kt:39-55` | Quotas count READY/STAGING; display counts READY/LEGACY. The per-credential ceiling can be exceeded. |
| ATT-07 | Detected MIME type depends on read granularity | `AttachmentContainerCodec.kt:44-46` | A source returning 4 bytes first yields `application/octet-stream`, then baked into AAD permanently. |
| ATT-08 | Dead `DELETING` state and `updateStorageState` | `AttachmentRecordEntity.kt:116` | The documented two-phase delete is half-implemented. |
| ATT-09 | `operationMutex` non-reentrant: `withStableAttachments` plus a repo call deadlocks | `AttachmentRepositoryImpl.kt:45-49` | Unkillable hang rather than an error. |
| ATT-10 | Double `close()` of the import source when the first throws | `AttachmentRepositoryImpl.kt:107-108` | The contract does not require idempotency. |
| BKP-10 | Backup password copied into unwipeable `String`s | `ValueTypes.kt:56-58`, `BackupViewModel.kt:469` | Same class as CRY-02. |
| BKP-11 | Export gate inconsistent: button needs GOOD, execution accepts FAIR | `BackupViewModel.kt:490-493` vs `562-575` | Any caller bypassing `canExport` exports on a FAIR password. |
| BKP-12 | Pre-authentication allocation up to 65 MiB per record | `BackupV2Codec.kt:171-178` | Well-bounded (~1:1 amplification) but allocates before tag verification. |
| BKP-13 | Attachment content records have no chunk-count ceiling | `VaultBackupV2Service.kt:611-640` | 1-byte content records are legal; ~10^8 AEAD ops possible within the 16 GiB cap. |
| BKP-14 | `readLengthPrefixedUtf8` silently substitutes U+FFFD | `VaultBackupV2Service.kt:786-790` | Not exploitable (compared against a validated ID) but inconsistent with the strict decoding used everywhere else. |
| DB-05 | `credential_records.folder_id` has no foreign key | `schemas/3.json:116` | Orphans are DB-legal; the invariant is app-enforced only. |
| DB-06 | `entry_count` desync possible; backup hard-asserts equality | `CredentialDao.kt:152-159`; `VaultBackupService.kt:566` | A desync converts into a **backup failure** — an availability issue, not cosmetic. |
| DB-07 | No corruption detection, integrity check, or recovery path | `VaultDatabase.kt:48-56` | No `PRAGMA quick_check`; `SQLITE_CORRUPT` surfaces as a generic failure. Correctly fails closed (no destructive fallback). |
| DB-08 | Plaintext folder `icon`, tag `color`, attachment `mime_type` and exact `size_bytes` | `FolderRecordEntity.kt:45-46` etc. | A bank or health emoji discloses folder semantics as effectively as its name. |
| SES-04 | Biometric success resets the password failure counter | `VaultRepositoryImpl.kt:242` | A coerced biometric touch also clears accumulated backoff. |
| SES-05 | Already-unlocked short-circuit returns success for an unverified key | `VaultRepositoryImpl.kt:223-229` | Not escalation, but a stale enrollment is never detected and purged. |
| SES-07 | `clearForLock()` does not reset `lastNavigatedSession` | `UnlockViewModel.kt:63-69` | Combined with SES-02, a buffered `NavigateToVault` can be delivered after lock. |
| SES-08 | Master password held as an immutable `String` in ViewModel state | `UnlockViewModel.kt:298` | Every keystroke allocates an unwipeable copy retained in Compose state. |
| SES-09 | Unlock failure timing distinguishes corrupt/missing vault from wrong password | `VaultRepositoryImpl.kt:193-197` | Validation precedes Argon2; the message is uniform but response time is an oracle. |
| OTP-03 | `TwoFactorCodesViewModel.clearForLock()` has zero production callers | `TwoFactorCodesViewModel.kt:164` | Seed wiping relies on next-frame disposal — strictly weaker than the synchronous clipboard clear. |
| OTP-04 | Valid scanned QR rejected because of an unrelated manual period field | `CredentialTotpController.kt:27-33` | Users fall back to typing seeds by hand — a worse enrollment path. |
| OTP-05 | Cancellation race orphans decrypted seeds without wiping | `TwoFactorCodesViewModel.kt:73-98` | The `finally` wipes an empty list if cancelled during the repository call. |
| OTP-06 | Minimum seed length is 80 bits, below RFC 4226 R6 (128 bits) | `TotpService.kt:458` | Almost certainly deliberate for interop (Google, GitHub issue 80-bit seeds). Document rather than change. |
| AND-02 | `attachment-exports` staging is plaintext with no timed cleanup | `AndroidAttachmentFileStore.kt:291-303` | Less severe than AND-01: not FileProvider-reachable, user-interactive window. |
| AND-05 | Screenshot-protection counter asymmetric; `disableProtection()` publicly reachable | `AndroidScreenshotProtection.kt:59-82` | Latent — no current caller, and `MainActivity` re-asserts FLAG_SECURE at three points. |
| AND-06 | Compose `Dialog` relies on `SecureFlagPolicy.Inherit` for the QR scanner | `TotpQrScanner.kt:31` | The camera preview shows a QR encoding the **TOTP seed**. Be explicit rather than inheriting. |
| AND-07 | `storeScreenshot` variant disables all protection and shares the debug applicationId | `build.gradle.kts:567-573` | Well-guarded by an identity verifier, but the shared ID weakens separation. |
| AND-08 | Release build silently falls back to debug signing when credentials are absent | `build.gradle.kts:554-564, 427-430` | `requireReleaseSigning` defaults to `false` and the verifier is not wired into `check`. |
| DSK-09 | `pv_bio_destroy` waits without a timeout while an OS prompt is pending | `passvault_biometric_macos.mm:552-571` | A wedged prompt can delay orderly key-wipe and DB close. |
| DSK-11 | Backup temp files leak on coroutine cancellation | `DesktopBackupFileStore.kt:187-212` | Missing `NonCancellable`; the attachment store does this correctly. |
| DSK-12 | Attachment `objects/` and `staging/` created world-readable-mode | `AttachmentBlobStore.kt:130-143` | Mitigated by the 0700 parent chain; residual if that ever regresses. |
| IOS-08 | Document-picker `asCopy` intermediate sits at default protection briefly | `IosAttachmentFileStore.kt:170-173` | Corrected at line 328; the window is short. The backup store has no equivalent (IOS-01). |
| IOS-09 | No `protectedDataWillBecomeUnavailable` handling despite `NSFileProtectionComplete` | `Database.ios.kt:125-127` | SQLite handles become unreadable ~10 s after screen lock; failure mode untested. |
| IOS-10 | `ITSAppUsesNonExemptEncryption = NO` declared by a vault application | `project.pbxproj:262` | Export-compliance and App-Review risk, not technical. Confirm with counsel. |
| NAV-01 | External URL opening relies on a scheme allowlist only | `VaultRouteAdapters.kt:199-201` | `http`/`https` enforced with host validation; `javascript:`/`file:` blocked. Correct, but the OS browser sees the URL. |
| LIC-01 | Notices are generated, not diffed against the actual release graph | `THIRD_PARTY_NOTICES.md` | Verified present and plausible; no automated equivalence check to the resolved dependency set. |
| SCR-01 | Three shell libraries omit `set -euo pipefail` | `scripts/lib/dotenv.sh` and two others | Intentional for `source`d libraries; all callers are strict. Document the contract. |
| SCR-02 | Secrets passed as command-line arguments in Apple tooling | `release.yml:842-846, 870-874` | Visible via `ps` for the call duration; low on ephemeral GitHub runners, high on self-hosted. Mitigated by `--deny-self-hosted-runners`. |
| CI-07 | Readiness manifest external-review states are asserted by `jq` | `candidate-readiness.yml:229-231` | The attestation covers the file, not the truth of its contents. |
| CI-08 | `dorny/test-reporter` granted `checks: write` on a PR-triggered job | `ci.yml:153-159` | Fork tokens are read-only; `fail-on-error: false` limits impact. |

---

## 23. Informational / Improvement Findings

| ID | Title | Note |
|---|---|---|
| INF-01 | `single<Any> { Unit }` registers the root type globally in Koin | `DesktopModule.kt:27-28`. Any future `get<Any>()` silently receives `Unit`. Use a qualifier. |
| INF-02 | `DesktopSystemTray` uses `invokeAndWait` from paths that may not be on the EDT | Deadlock risk if `requestClose` ever moves off the EDT. |
| INF-03 | `AndroidAttachmentFileStore.attach()` reads shared state outside the monitor | Benign today (main-thread only); inconsistent with `detach()`. |
| INF-04 | 60-second SAF grace period keeps the vault unlocked in background | Deliberate, bounded, well-tested. Consider 30 s and cancelling on `ACTION_SCREEN_OFF`. |
| INF-05 | R8 keeps the entire libsodium binding package unshrunk | Required for JNI. Obfuscation is not a security control. |
| INF-06 | `kSecUseDataProtectionKeychain` never set on iOS | Defaults to true on iOS; the macOS bridge sets it explicitly. Latent if Catalyst is added. |
| INF-07 | `touchIDAuthenticationAllowableReuseDuration` not explicitly zeroed on iOS | Defaults to 0 and a fresh `LAContext` is used per operation. A refactor hoisting the context would silently enable 10 minutes of reuse. |
| INF-08 | `LAContext` in `getCapability()` never invalidated | Minor resource retention; no key material involved. |
| INF-09 | `CODE_SIGN_STYLE = Automatic` in iOS Release | Non-reproducible signing; a compromised developer account can mint entitlements without a reviewable diff. |
| INF-10 | `PrivacyInfo.xcprivacy` over-declares FileTimestamp reason `C617.1` | The app never displays timestamps from those calls. Otherwise accurate. |
| INF-11 | `ci.yml` has no `concurrency` block | Cost and resource only. |
| INF-12 | foojay toolchain resolver adds an external JDK-resolution surface | No `jvmToolchain {}` is declared, so it is likely never triggered. Consider removing. |

---

## 24. Cryptography Audit

**Design: sound.** Random 32-byte data key wrapped by a password-derived key, per-purpose subkeys under a domain-separated keyed BLAKE2b, authenticated encryption on every persisted value, and a verification record separate from the wrap. There is no unauthenticated fallback and no path that uses ciphertext before authentication succeeds.

**Implementation: strong, with one memory-hygiene defect (CRY-02).** All platforms delegate to one reviewed `LibsodiumCryptoEngine`; the Android and Desktop "engines" are `by` delegates with no divergence. Buffer discipline is exemplary — the `cryptoOperation` wrapper even clears a result produced after cancellation but before delivery, a subtle case most implementations miss.

**What I could not break:** AAD binding on credentials, history, attachments, and backups; the envelope magic check; the transcript comparison; the subkey encoding injectivity.

---

## 25. Argon2id Audit

| Property | Value | Assessment |
|---|---|---|
| Algorithm | `crypto_pwhash_ALG_DEFAULT` = Argon2id v1.3 | Correct choice |
| Default profile | ops 3, 64 MiB (ops 4 on fast devices) | Meets the OWASP 46 MiB floor; below the 256 MiB many managers use for a master key |
| Salt | 16 bytes CSPRNG per vault | Correct |
| Output | 32 bytes | Correct |
| Parallelism | forced to 1 | Correct given the binding, but **undocumented** (CRY-05) |
| Stored bounds | ops 2..10, mem 32..256 MiB | Validated **before** the KDF runs |
| Backup bounds | same, checked before `deriveKey` | Correct structure; ceiling too generous (BKP-04) |
| Malformed params | rejected by `require` | Fail-closed |
| Rehash/upgrade | **none** | An old vault created on a slow device keeps ops 2 / 32 MiB forever |
| KAT vectors | **absent** | `Argon2Test.kt` never calls Argon2 (DOC-02, TST-02) |

**Weak-parameter persistence.** A vault created on a slow device retains ops 2 / 32 MiB indefinitely; only `changeMasterPassword` re-benchmarks. Recommend an opportunistic rehash on successful unlock when stored parameters fall below the current recommendation.

**DoS from attacker-controlled parameters: not possible.** Bounds precede the KDF call in both the vault and backup paths — verified by line ordering.

---

## 26. XChaCha20-Poly1305 Audit

| Check | Result |
|---|---|
| Fresh nonce per encryption | Yes — `LibsodiumRandom.buf(24)` per call |
| Nonce reuse under one key | Not possible: 192-bit random space; collision ~2^-136 at 2^28 records |
| Key size enforced | `require(key.size == 32)` on both encrypt and decrypt |
| Authentication always checked | Yes — libsodium `*_decrypt` throws on tag failure |
| Ciphertext used before authentication | **No** — verified across credential, backup, and attachment paths |
| AAD protects metadata | Yes — record id plus purpose (credentials), header plus type plus index plus length (backup), full ownership quadruple plus chunk index (attachments) |
| Versioning | 4-byte envelope magic `50 56 02 00`, required on decrypt |
| Malformed / truncated / modified tag / modified header | All rejected |
| Record swapping | Blocked by AAD |
| Attachment swapping | Blocked by AAD |
| Cross-credential substitution | Blocked by AAD |
| Cross-vault substitution | Blocked by per-vault VEK subkeys |
| Type confusion | Blocked by purpose in AAD |
| Backup-object substitution | Blocked by per-file salt in AAD |
| Version confusion | Blocked by required magic plus version equality checks |

**Answer to the transplantation question: no.** An attacker who can edit the database or a backup file cannot move a valid ciphertext into another context and have it accepted, in any of the paths audited.

---

## 27. Key Management / Domain Separation Audit

Separate keys are genuinely used for credential records, password history, attachments, backups, search, and duplicate detection. The domain-separation encoding (`domain || decimalLength || 0x00 || context`) is injective, so contexts cannot collide.

**Issues:** `keyDerivationContext` is DB-controlled and feeds key derivation (safe, because it is simultaneously in the AAD — ATT-02 residual); the filename AAD uses non-injective concatenation (ATT-05); no explicit vault identifier appears in any AAD, so cross-vault separation rests entirely on VEK uniqueness; and the VEK is never rotated on password change (only rewrapped), which makes the `title_hash` linkage oracle permanent (DB-04).

---

## 28. Randomness Audit

Every security-sensitive random value derives from `LibsodiumRandom.buf` — salts, VEK, all nonces, backup salts, attachment nonces, password-generator indices. No `kotlin.random.Random`, no `java.util.Random`, no `Math.random`, and no seeded generator appears in any production security path. There is no accidental mixing of utility randomness with cryptographic randomness.

`SecurePasswordGenerator.secureRandomIndex` uses correct rejection sampling — **proven unbiased for every bound 1..65535** (Section 43). The Fisher-Yates shuffle is the correct Durstenfeld form.

Platform consistency: all targets route through the same libsodium binding, so there is no platform-specific RNG divergence.

---

## 29. Master Password Handling Audit

```
Compose TextField (String)          [SES-08 — unwipeable, per keystroke]
  -> UnlockState.password: String   [SES-08 — retained in state]
  -> SensitiveText.from(String)     [copies from an already-leaked String]
  -> CharArray (wipeable)            OK
  -> toUtf8ByteArray()              [CRY-02 — unwipeable String #1]
  -> toHexString()                  [CRY-02 — unwipeable String #2]
  -> Argon2id                        OK
  -> ByteArray wiped in finally      OK
```

| Check | Result |
|---|---|
| Logged | No — zero logging in production security code |
| In exception messages | No — verified, only compile-time constants interpolated |
| Persisted | No |
| In `rememberSaveable` | No |
| In navigation arguments | No |
| Autofill exposure | Not opted out (UI-02) |
| Accessibility exposure | Yes, while revealed (UI-02) |
| Clipboard | No |
| Survives lock | The `String` copies do, until GC |

**Avoidable copies:** `concatToString()` and `toHexString()` (CRY-02), plus the ViewModel `String` state (SES-08). **Unavoidable JVM limitation:** Compose `TextField` is `String`-based until migrated to `TextFieldState`.

---

## 30. In-Memory Secret Handling Audit

Discipline is genuinely good: `SensitiveText` is `CharArray`-backed with a real `clear()`, `toString()` returns `[REDACTED]`, `hashCode()` returns a constant `0`, `equals()` is constant-time, and the serializer **throws in both directions** so a secret cannot be accidentally serialized into navigation or saved state. Every `ByteArray` in crypto paths is wiped in `finally`.

The gaps are all the same root cause — **immutable `String` materialisation**: CRY-02 (master password), BKP-10 (backup password), OTP-02 (TOTP seed, once per second), HLT-01 (every vault password during health analysis), SES-08 and UI-02 (UI state).

**On erasure guarantees:** I do **not** claim any of this achieves erasure. On JVM/ART a moving GC can relocate a `ByteArray` before `fill(0)` runs, leaving a stale copy. `fill(0)` is a best-effort narrowing of the window, not a guarantee. The codebase's own comment ("Best effort on managed runtimes") is accurate.

---

## 31. Authentication / Session / Auto-Lock Audit

Initial state `Uninitialized`; transitions `Locked -> Unlocking -> Unlocked -> Locking -> Locked`, all serialized on one mutex. `clearSessionLocked` nulls `currentVek` *before* wiping and is idempotent.

**Correct:** the VEK is genuinely zeroed on lock; a leased copy cannot outlive `lock()` (mutex ordering guarantees the `finally` wipe runs first); auto-lock uses a **monotonic** clock so wall-clock tampering cannot bypass it; `timeoutMillis == 0` is unreachable.

**Defective:** lock can be blocked indefinitely (SES-01); a failed lock stalls forever (SES-06); lock cannot pre-empt an in-flight unlock (SES-02); the monotonic clock does not advance across device sleep, so a suspended device can resume with the deadline unreached.

**What survives lock:** ViewModel `String` state, decrypted domain objects already handed to callers, and (on iOS/Android) entry-scoped ViewModels cleared only on next-frame disposal (OTP-03).

---

## 32. Failed-Attempt Throttling Audit

| Question | Answer |
|---|---|
| Where does the counter live? | In-memory field, `VaultRepositoryImpl.kt:50` |
| Survives restart? | **No** |
| Survives process kill? | **No** |
| Reset by reinstall? | Yes, trivially |
| Clock manipulation? | Irrelevant — it is a `delay`, not a timestamp |
| Overflow? | Capped at `MAX_FAILED_ATTEMPTS = 100` |
| Parallel attempts? | Serialized by the mutex |
| Delay inside the mutex? | **Yes** — blocks other session operations |
| Maximum delay | 5 seconds |
| Biometric vs password | Biometric success resets the counter (SES-04) |

**Protects against:** a person or script guessing through the running app's UI within one process lifetime.

**Does not protect against:** offline attack on a stolen database. The attacker reads `argon2Salt` and `wrappedVek` and brute-forces entirely outside this code. **Argon2id is the only defence there**, and a single Argon2 run already costs more than the maximum 5-second delay — so the throttle adds essentially nothing even online. This is online-style throttling, and it must not be mistaken for protection against offline cracking.

---

## 33. Biometric Audit

| Platform | Secret stored | Where | OS policy | Leaves machine? | Invalidated by | Verified |
|---|---|---|---|---|---|---|
| Android | AES-GCM ciphertext of the VEK | `MODE_PRIVATE` prefs; key in Keystore | `userAuthenticationRequired`, timeout 0, `BIOMETRIC_STRONG` only | No (`allowBackup=false`, both rule files exclude) | New biometric enrollment | Source |
| iOS | VEK bytes | Keychain generic password | `WhenPasscodeSetThisDeviceOnly` + `BiometryCurrentSet`, `synchronizable=false` | No | Any enrollment change; passcode removal | Source only |
| macOS | VEK bytes | Keychain, data-protection keychain | Same class and flags, fresh `LAContext`, reuse duration 0 | No | Any enrollment change | Source only |
| Windows | AES-GCM envelope; wrapping key from WebAuthn PRF | `~/.passvault/biometric/windows-v1.dat`, current-user-SID-only DACL | Resident credential, UV required, platform attachment, credProtect | No — syncable passkeys explicitly rejected | Credential deletion | Source only |
| Linux | none | n/a | Unsupported, fails closed to master password | n/a | n/a | Source |

**What biometrics release:** real key material on every platform — never a boolean. The released key is always AEAD-verified against the vault verification record before becoming the session key. **There is no boolean-gated bypass.**

Windows is the strongest implementation I reviewed: HKDF over the PRF output, full envelope header as GCM AAD, assertion signature verified against a pinned public key, constant-time RP-ID hash comparison, canonical-CBOR enforcement, and explicit rejection of backup-eligible (syncable) credentials so the wrapping factor cannot leave the device.

**Not verified:** iOS, macOS, and Windows behaviour on real hardware. No biometric hardware was available.

---

## 34. Database / Room Audit

**Migrations are exemplary.** v1→v2 adds two indices with `IF NOT EXISTS`; v2→v3 adds two defaulted columns. No `DROP`, no table recreate, no `UPDATE`. Room wraps each in a transaction, and `VaultMigrationTest.kt:108-133` empirically proves rollback (a deliberately failing migration leaves `user_version == 1` and rows intact).

**`fallbackToDestructiveMigration` is absent from the entire repository** — verified by grep. All three platform builders call `addVaultMigrations()`, so a missing migration path throws rather than wiping. This is the single most important safe property of this layer.

**No migration touches any AAD input** (`id`, `credential_id`, `purpose`, version literals) or any nonce/ciphertext, so authentication cannot break across upgrade.

**SQL injection: none.** Every `@Query` uses bind parameters; **no `@RawQuery` exists anywhere**; no string concatenation into SQL.

**Transactions verified in generated code.** `updateCredentialWithTagsAndHistory` and `deleteCredentialAndRefreshCount` both compile to `performInTransactionSuspending`; nested `@Transaction` helpers join via savepoints.

**Foreign keys are genuinely enforced** — `PRAGMA foreign_keys = ON` is emitted by Room's generated `onOpen`, confirmed in `VaultDatabase_Impl.kt:129-131` rather than assumed.

---

## 35. Metadata Leakage Analysis

Complete column-level table. Everything marked plaintext is readable with `sqlite3 vault.db` and no password.

### `credential_records`

| Column | Encrypted | Authenticated | What an attacker learns |
|---|---|---|---|
| `id` | No | Yes (AAD) | Stable identifier; correlates across tables and backups over time |
| `type` | **No** | **No** | Exact class per row: Login, note, card, identity, API key |
| `title_hash` | Keyed hash | **No** | Equality/linkage oracle on titles; not brute-forceable (DB-04) |
| `summary_payload` | **Yes** | Yes | Length only (title + username + email size) |
| `secret_payload` | **Yes** | Yes | Length only (password count, notes size, TOTP presence) |
| `folder_id` | **No** | **No** | Full credential-to-folder assignment graph |
| `is_favorite` | **No** | **No** | Which entries the user considers most important |
| `created_at` | **No** | **No** | Millisecond vault-population timeline, import bursts |
| `updated_at` | **No** | **No** | Password rotation history and behavioural profile |
| `last_used_at` | **No** | **No** | Last use; NULL means never used — reveals live accounts |

### `folder_records`

| Column | Encrypted | Auth | Leak |
|---|---|---|---|
| `id`, `parent_id` | No | No | Complete folder tree shape and depth |
| `name_hash` | Keyed hash | No | Equality oracle on folder names |
| `encrypted_payload` | **Yes** | Yes | Length of name + description + colour |
| `icon` | **No** | **No** | Plaintext emoji — a bank or health icon discloses the category |
| `sort_order`, timestamps | No | No | Ordering and timing |

### `tag_records`

| Column | Encrypted | Auth | Leak |
|---|---|---|---|
| `id` | No | No | Correlation key |
| `name_hash` | Keyed hash | No | Equality oracle on tag names |
| `encrypted_payload` | **Yes** | Yes | Length only |
| `color` | **No** | **No** | Plaintext hex, and **indexed** |

### Cross-reference tables

| Column | Encrypted | Auth | Leak |
|---|---|---|---|
| `credential_id`, `tag_id`, `folder_id` | **No** | **No** | **The entire relational graph in plaintext.** Which credentials share a tag, tag cardinality, cluster structure — reconstructed without decrypting a byte |

### `attachment_records`

| Column | Encrypted | Auth | Leak |
|---|---|---|---|
| `encrypted_filename` | **Yes** | Yes | Filename length only |
| `mime_type` | **No** | **No** | Document type per credential |
| `size_bytes` | **No** | **No** | **Exact byte size** — strong fingerprint against a known corpus |
| `storage_path`, `key_derivation_context` | No | No | Opaque UUID; validated non-user-derived |
| `storage_state` | **No** | **No** | And **unauthenticated** — see ATT-02 |
| row count per credential | No | No | Attachment count per credential |

### `password_history_records`

| Column | Encrypted | Auth | Leak |
|---|---|---|---|
| `credential_id` | No | Yes (AAD) | Which credentials have rotated passwords |
| `encrypted_password` | **Yes** | Yes | **Password length leaks via ciphertext length** (DB-03) |
| `changed_at` | **No** | **No** | Exact rotation timeline per credential |
| row count | No | No | Number of rotations; zero rows means never changed |

### `vault_metadata`

| Column | Encrypted | Auth | Leak |
|---|---|---|---|
| `vault_id` | **No** | **No** | Stable global identifier — cross-backup, cross-device correlation |
| `argon2_*` params | **No** | **No** | Attacker sizes a cracking rig exactly; spots weak-benchmark devices |
| `argon2_salt` | No (by design) | No | Necessary; correctly per-vault |
| `wrapped_vek` | **Yes** | Yes | The offline brute-force target |
| `encrypted_verification_record` | **Yes** | Yes | A second, independent password-guess oracle |
| `created_at`, `last_accessed_at` | **No** | **No** | Vault age and exact last-unlock time |
| `entry_count` | **No** | **No** | Total credential count, redundant with `COUNT(*)` |

**Assessment.** This is a deliberate, documented design choice (`SECURITY_MODEL.md:82-86`) and I do **not** rate it Critical. But the aggregate — composition, favourites, full folder and tag graphs, and millisecond timelines — is a genuine behavioural profile, and it is disclosed in only one of five security documents (DOC-05).

`corruption_logs` and `migration_state` contain free-text error and stack-trace columns that would be a serious plaintext sink. Both entities are **deliberately DAO-less** with no writer anywhere in the tree — correctly mitigated and explicitly documented. Keep it that way.


---

## 36. Data Integrity / Atomicity Audit

| Operation | Atomic | Notes |
|---|---|---|
| Create credential | Yes | Single Room transaction |
| Edit credential + tags + history | Yes | `updateCredentialWithTagsAndHistory`, verified in generated code |
| Delete credential | DB atomic | Blob cleanup follows the commit — correct ordering (never a live row without data) |
| Attachment import | Fail-closed | insert STAGING, write blob, update READY; a crash leaves a STAGING row recovery deletes |
| Attachment delete | DB-first, sweep after | Correct |
| Backup export | Yes | `abort()` under `NonCancellable` unless committed |
| **Backup restore** | **DEFECTIVE** | **BKP-01** — cancellation window deletes committed attachments |
| Legacy v1 restore | Partial | **BKP-06** — orphans all old blobs |
| Migration | Yes | Room transaction, rollback proven by test |
| Change master password | Yes | Single UPDATE is the commit point |

**Failure states found:** DB updated but attachment missing (BKP-01, ATT-01); orphaned ciphertext (BKP-06); inaccessible attachment (ATT-02, ATT-03); `entry_count` desync causing backup failure (DB-06).

---

## 37. Backup Format Audit

Full byte layout in Section 14. Summary of what is authenticated: header (magic, version, all KDF parameters, salt), record type, record index, plaintext length, per-type counts (sealed MANIFEST), total counts and object bytes (FINAL), and a SHA-256 transcript compared in constant time across two passes.

**Attacks attempted and blocked:** header modification, KDF-parameter downgrade, version change, record reorder, record type change, record drop, record duplication, record splice from another file, attachment ownership change, attachment length change, credential metadata change, trailer removal, truncation, and trailing-data append.

**Weaknesses:** the ceiling on attacker-chosen Argon2 parameters is too generous (BKP-04); attachment content records lack a chunk-count ceiling (BKP-13); a future v3 magic falls through to the legacy v1 parser rather than producing an "unsupported version" error (BKP-02, DOC-09).

---

## 38. Backup Parser / Restore Audit

| Defence | Status |
|---|---|
| Truncated files | Rejected (`requireExhausted`, FINAL counters) |
| Gigantic declared lengths | Bounded per type; `Long`-widened arithmetic |
| Integer overflow | Not reachable — all length math widened |
| Negative lengths | `require(size >= 0)` at every read |
| Excessive object counts | Capped at 1,000,000 per category — but see BKP-03 |
| Decompression bombs | **No compression exists anywhere** (verified by full-tree grep) |
| Huge Argon2 parameters | Bounds checked **before** the KDF |
| 16 GiB boundary | Enforced |
| EOF at every stage | Handled |
| Duplicate IDs | Rejected per category, plus `OnConflictStrategy.ABORT` |
| Unknown/future versions | v2 checked exactly; **future versions fall into the v1 path** (BKP-02) |
| Malformed UTF-8 | Strict decoding everywhere except one helper (BKP-14) |
| Path traversal | Two independent layers reject it |
| Memory exhaustion | **BKP-02, BKP-03** |

**Restore ordering.** Full validation and attachment staging complete in pass 1 *before* anything destructive; `clearVaultTables()` plus replay run inside a single `immediateTransaction`. A failed restore therefore **cannot** destroy the existing vault — verified and tested five ways. The defect is the *post-commit* cancellation window (BKP-01), which is untested.

---

## 39. Attachment Security Audit

Container layout in Section 15. Ownership binding covers `attachmentId`, `credentialId`, `keyDerivationContext`, and `mimeType`, all length-prefixed, plus chunk index and type per record.

**Blocked:** swapping blobs between attachments, between credentials, and between vaults; chunk reorder, duplication, and truncation; container truncation; appended data.

**Not protected:** `storage_state` (ATT-02) is the one security-relevant row field with no integrity protection.

Path handling is strong: `storagePath` is never derived from user input (`objects/<uuid>.pva`), validated by exact length, prefix, suffix, and strict lowercase-hex UUID shape, then re-anchored with a parent assertion. Symlinks are rejected at read, delete, exists, sweep, and post-write. Filenames reject separators, `:`, NUL, all C0/C1 controls, and bidi/format code points — so RLO extension spoofing and null-byte truncation are blocked — but not Win32 device names (ATT-04).

---

## 40. Plaintext Staging / Temporary File Audit

| Platform | Location | Permissions | Cleanup | Survives process death |
|---|---|---|---|---|
| Android preview | `cacheDir/attachment-previews/<uuid>/` | app-private | 10-min in-process timer + startup sweep | **Yes** (AND-01) |
| Android export | `cacheDir/attachment-exports/<uuid>/` | app-private, **not** FileProvider-reachable | `finally` on both paths | Yes, briefly (AND-02) |
| Desktop preview | `%TEMP%/passvault-attachment-preview-<uuid>/` | 0700 dir / 0600 file on POSIX; **no ACL on Windows** | 10-min timer + `deleteOnExit` + startup scan | **Yes** (DSK-07) |
| Desktop backup temp | user-chosen directory | `createTempFile` default | `commit`/`abort` — **not** `NonCancellable` | Yes (DSK-11) |
| iOS attachment | `Library/Caches/passvault-attachment-<uuid>/` | `NSFileProtectionComplete` | swept on first access, strict name grammar | Mitigated |
| iOS backup | `Library/Caches/passvault-export-<uuid>/` | **none** | `discard()` | **Yes** (IOS-01) |

Android and Desktop startup sweeps are well-implemented — owner-checked, name-shape-checked, symlink-safe, and tested. The gap is that they only run at next launch.

**After an explicit export or open-with:** the receiving third-party application retains whatever it copied. This is outside PassVault's control and should be stated in the UI.

---

## 41. TOTP Audit

**RFC 6238 conformance: CONFIRMED.**

Every step verified by hand:
- Counter: 8-byte big-endian of `floor(epochSeconds / period)`, written MSB-first with `ushr` to avoid sign propagation. Correct.
- Dynamic truncation: `offset = last & 0x0F`; four bytes masked `0x7F, 0xFF, 0xFF, 0xFF`. **Every `.toInt()` on a `Byte` in the file is masked** — the classic Kotlin signed-byte bug is absent.
- Modulo plus `padStart(digits, '0')` — leading zeros preserved.
- HMAC is okio, not hand-rolled, so the SHA-512 block-size trap does not apply. The `when` over the algorithm enum is exhaustive with **no `else`**, so there is no silent SHA-1 fallback. Operand order is `HMAC(secret, counter)` — correct.
- `period = 0` unreachable (`MIN_PERIOD_SECONDS = 5`); negative time rejected; counter overflow guarded.

**Independent verification.** `TotpServiceTest.kt:17-29` hard-codes the genuine RFC vectors. I recomputed all 18 values (6 timestamps x 3 algorithms) with a separate HMAC implementation and **all 18 match**, including `07081804` (leading zero) and `65353130` (T beyond 2^31). The test's local `base32Encode` is an encoder, not a second TOTP implementation, so it cannot mask a defect.

**Scope limit.** All 18 vectors use `digits = 8`. The 6-digit path — the production default — is only length-checked, and is exactly the branch OTP-01 makes fragile.

**Base32 and URI parsing** are strict: RFC 4648 alphabet, mid-string padding rejected, non-canonical padding rejected, illegal remainders rejected, non-canonical trailing bits rejected with the partial output zeroed. The `otpauth://` parser rejects `hotp`, fragments, unknown algorithms (rejected, not defaulted), duplicate parameters, invalid percent-encoding, control and bidi code points, and inputs over 8 KiB. Issuer spoofing is blocked by requiring label-issuer and query-issuer to agree.

---

## 42. QR Enrollment Audit

**Can scanning a QR trigger a network call or launch an external app? No.** The scanned payload flows only to the enrollment parser. The single `Intent` in the Android scanner targets the app's **own** package settings screen on an explicit button press, never derived from QR content. `QRCodeReader` (not `MultiFormatReader`) is used, so no other symbology is parsed.

Android: `AtomicBoolean` guards ensure at-most-one payload and no post-dispose callback; every frame's luminance and row buffers are zeroed in `finally`; all plane geometry is bounds-checked against a hostile HAL before allocation.

Desktop is **not a stub** — it performs real ZXing decoding from a chosen file, deliberately using `FileImageInputStream` to prevent ImageIO's disk cache from writing a second copy of the seed-bearing image, validates dimensions **twice** (header and decoded raster), and zeroes the pixel buffer and the entire `DataBuffer`. Only the shared dialog title overclaims "Scan" where desktop offers file selection (DSK-13, informational).

---

## 43. Password Generator Audit

**Rejection sampling is provably unbiased.** Let `R = 65536`, bound `b`. `rejectionLimit = R - (R mod b) = b * floor(R/b)`, an exact multiple of `b`. `value` is uniform on `[0, 65535]`. Accepted values form `{0, ..., b*floor(R/b) - 1}`, which partitions into exactly `floor(R/b)` complete residue cycles, so every residue has precisely `floor(R/b)` preimages. Uniform. Edge cases: `b = 1` always accepts and returns 0; `b = 65535` rejects only 65535. Correct.

**Shuffle is correct.** `for (index in lastIndex downTo 1) { j = secureRandomIndex(index + 1); swap }` gives `j` in `[0, index]` — the textbook Durstenfeld form. With unbiased `j`, output is uniform over all `n!` permutations.

**Effective entropy** (alphabet N = 88 for all four sets):

| Configuration | Unconstrained | P(valid) | Effective |
|---|---|---|---|
| Length 16, all 4 sets | 16 x log2(88) = **103.35 bits** | 0.844 | **103.1 bits** (loss 0.25) |
| Length 16, lowercase only | 16 x log2(26) = **75.21 bits** | 1.0 | **75.21 bits** (no loss) |
| Length 8, all 4 sets | 8 x log2(88) = **51.68 bits** | 0.484 | **~50.6 bits** (loss 1.05) |

Forcing required characters costs essentially nothing at length 16. `coerceAtLeast(selectedSets.size)` **cannot** silently lengthen a password: the `require(length in 8..128)` runs first and `selectedSets.size <= 4`, so the branch is unreachable.

**Concern:** `MIN_PASSWORD_LENGTH = 8` yields ~50.6 bits — about 72 years against Argon2id, but only ~26 days against a fast-hash target elsewhere. Raise the generator floor to 12.

---

## 44. Passphrase Generator Audit

**No passphrase generator exists.** There is no wordlist, no diceware implementation, and no passphrase option in `PasswordGenerationOptions`. Nothing in the UI or documentation claims one. Section not applicable.

*Recommendation:* given DOM-04's bias toward short complex passwords, a diceware generator would let users produce high-entropy memorable master passwords — the single best mitigation for DOM-02.

---

## 45. Password Health Audit

**Zero network confirmed.** No HIBP integration, no k-anonymity range query, no SHA-1 prefix upload, no HTTP client anywhere under `feature/health`. All analysis is local.

**Algorithmic complexity is O(n log n)**, not O(n^2): `sortedWith` plus adjacent-pair grouping. The code comment correctly explains why hash-based `groupBy` was avoided (the redacted `hashCode` would force O(n^2) collision comparisons). Good design.

**Concern (HLT-01):** every password in the vault is decrypted **simultaneously** for the duration of the sort, and scoring materialises three or more unwipeable `String` copies per credential. Comparison itself is non-constant-time, which is acceptable here (all operands are local secrets, no oracle).

---

## 46. Clipboard Audit

| Platform | Ownership check | Sensitive flag | Timeout | Cleared on lock | Can clobber other content |
|---|---|---|---|---|---|
| Android | **Yes** — random UUID in clip label | `EXTRA_IS_SENSITIVE` (API 33+) | Yes | Yes | **No** |
| Desktop | **Yes** — identity plus UUID on a JVM-local flavour | **No** (DSK-05) | 20 s | Yes | **No** |
| iOS | **Yes** — `changeCount` comparison | `localOnly` plus `expirationDate` | 5-300 s clamped | Yes | **No** |

**All three implementations correctly refuse to wipe content the user or another app copied afterwards.** This is the most commonly botched control in password managers, and PassVault gets it right on every platform. The Desktop implementation additionally retains ownership and reschedules on a failed clear rather than silently abandoning the secret.

**Gaps:** Desktop does not exclude secrets from Windows Clipboard History or Cloud Clipboard (DSK-05, High). Android's ownership token is the user-visible clip label, leaking the literal string `secret` as a targeting signal (AND-09, Low). The unused non-sensitive `copy()` path on Android and iOS lacks the flags and timer (Low, latent — no production caller).

---

## 47. Screenshot / Screen Privacy Audit

| Platform | Mechanism | Assessment |
|---|---|---|
| Android | `FLAG_SECURE` set before first frame and re-asserted in `onCreate`, `onResume`, `onWindowFocusChanged`; `setRecentsScreenshotEnabled(false)` on 33+; `setHideOverlayWindows(true)` on 12+ | Strong. Overlay hiding is uncommon and welcome. Compose `Dialog` relies on `Inherit` (AND-06) |
| iOS | Privacy cover on any non-`.active` phase, `accessibilityHidden`, `allowsHitTesting(false)`, fail-closed on unknown phases | Strong. One-frame race under main-thread contention (IOS-11, Low) |
| Desktop | **None** | Honestly documented as a non-goal. `lock()` should still draw its existing curtain synchronously (DSK-04) |

Documentation does **not** overpromise here — `DesktopWindowProtection.kt:14-20`, `docs/API.md:26`, `SECURITY.md:52`, and the readiness audit all state the desktop limitation plainly. Credit where due.

---

## 48. Navigation Security Audit

Navigation keys are `@Serializable` and carry **only identifiers** — credential IDs, folder IDs, tab selections. **No key carries a password, TOTP seed, or decrypted content.** Verified by reading every key definition.

`RestoredNavigationValidator` is **fail-closed**: an unrecognised or invalid restored key collapses the stack to a safe destination rather than proceeding. On lock, `AppNavigator.requireAuthentication()` quarantines the stack via `resetLiveStacks()`, so back navigation cannot reveal locked content and process restoration cannot reopen a secret screen before authentication.

**Residual:** entry-scoped ViewModels are torn down on the **next composition frame** rather than synchronously with the lock (OTP-03), and a serialized back stack containing credential IDs plus screen identity is written to platform saved state — metadata, not secrets.

`ExternalNavigation` restricts opened URLs to `http`/`https` with host validation, blocking `javascript:`, `file:`, and `intent:` (NAV-01). No deep links are registered on either platform — verified in the Android manifest and iOS `Info.plist`.

---

## 49. Compose Secret-State Audit

**No `rememberSaveable` holds a secret** — verified by grep across all feature UI. This matters: `rememberSaveable` is written to the platform saved-state bundle, which Android persists to disk.

`SensitiveText` cannot be accidentally serialized: its serializer **throws in both directions**, so a DTO or navigation key embedding it fails loudly at runtime rather than silently writing plaintext.

Decrypted secrets do live in ViewModel state during editing and detail display. Each ViewModel exposes a `clearForLock()`, and the navigation host calls them on lock — except entry-scoped ViewModels not present in `RouteAdapterContext` (OTP-03).

`SecureTextField` masks via `visualTransformation` and auto-conceals after 15 seconds and on any edit, but lacks `semantics { password() }` and an autofill opt-out (UI-02).

---

## 50. Concurrency / Cancellation Audit

**Well handled:** `NonCancellable` around commit and cleanup regions in the attachment store, clipboard, iOS lifecycle, and backup export; `CancellationException` consistently rethrown rather than converted to a domain failure; `AtomicBoolean` CAS guards in the camera and native bridge paths; bounded `awaitNanos` with a last-caller-destroys handoff in the JNA bridge, avoiding use-after-free; reference-identity guards in the iOS background-cleanup episode so a superseded completion cannot affect a newer one.

**Defective:** BKP-01 (post-commit cancellation deletes attachments); DSK-11 (`withContext(Dispatchers.IO)` without `NonCancellable` in backup commit/abort — throws at the dispatch boundary before executing, so `abort()` never deletes); OTP-05 (wipe bound to a variable assigned after the suspension point); ATT-09 (non-reentrant mutex deadlock); SES-01 and SES-06 (lock liveness).

**Race analysis performed:** lock during credential decrypt (safe — mutex ordering); lock during backup creation (safe); background immediately after biometric success (SES-02); biometric success after manual lock (safe — returns existing session); restore while vault UI open (safe — serialized); copy secret then immediate lock (safe — `copySensitiveWhileUnlocked` rechecks the session after the write); two copy operations with different expiry timers (safe — ownership tokens); app backgrounded during vault creation (safe).

---

## 51-53. Android Security Audit

**Manifest.** Three permissions only: `CAMERA`, `HIDE_OVERLAY_WINDOWS`, `USE_BIOMETRIC`. **No `INTERNET`** — confirmed by grep across every file type in the repository. CameraX's transitive `ACCESS_NETWORK_STATE` is explicitly stripped with `tools:node="remove"` — proactive hygiene beyond what most projects do.

**Exported surface:** exactly one component (`MainActivity`) with a bare `MAIN`/`LAUNCHER` filter. No custom scheme, no `VIEW`, no `BROWSABLE`, no deep link. No exported services, receivers, or providers. The single `<provider>` is `exported="false"`. IPC attack surface is effectively nil.

**Backup disabled at three layers:** `allowBackup="false"`, `backup_rules.xml` excluding all four domains, and `data_extraction_rules.xml` excluding all four domains for **both** cloud backup **and device transfer** — the device-transfer exclusion is the one most projects miss.

**FileProvider** grants exactly one path (`attachment-previews/`). No `root-path`, no `external-path`, no `files-path`. Export staging lives deliberately *outside* the granted tree, so no content URI can ever be minted for it. Grants are read-only, non-persistable.

**Release hardening:** `isDebuggable = false`, `isMinifyEnabled = true`, `isShrinkResources = true`, separate `.debug` applicationId. StrictMode is debug-only with `penaltyLog()` only. Koin logging is severity-gated. **Zero `android.util.Log`, `println`, or `printStackTrace` in production Android code.**

**Findings:** AND-01 through AND-08 above.

---

## 54-56. iOS Security Audit

**Keychain (verified as optimal):** `kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly` — the strongest available class, device-only and destroyed if the passcode is removed. `kSecAttrSynchronizable = false` set **twice** (on add and in the shared base query), so the VEK can never reach iCloud Keychain. `kSecAccessControlBiometryCurrentSet` — adding a fingerprint or Face ID appearance **invalidates the item**, defeating the coerce-passcode-then-enroll attack.

**The Keychain read *is* the authenticated operation.** `kSecUseAuthenticationContext` is set with a freshly constructed, never-`evaluatePolicy`-ed `LAContext`, so `SecItemCopyMatching` itself presents Face ID and returns data only on success. This is the textbook-correct construction and defeats the classic "authenticate, then do an unauthenticated read" bypass.

**Data Protection:** the Room database, WAL/SHM/journal sidecars, and attachment blobs all get `NSFileProtectionComplete` plus `NSURLIsExcludedFromBackupKey`, all `check()`-guarded so the app **fails to launch** rather than running unprotected. The legacy `Documents/` migration moves sidecars before the main file so the main file is the commit marker, with rollback on failure. Exemplary — which makes the backup-staging omission (IOS-01) and the missing entitlement (IOS-02) stand out.

**`Info.plist`:** `NSFaceIDUsageDescription` present and localized (the app will not crash on Face ID); `NSCameraUsageDescription` present. **Absent and correct:** `UIFileSharingEnabled`, `LSSupportsOpeningDocumentsInPlace`, `UIBackgroundModes`, `CFBundleDocumentTypes`, `NSAppTransportSecurity`. `UIApplicationSupportsMultipleScenes = false` is a deliberate, documented security control closing a multi-window vault-state race.

**Findings:** IOS-01 through IOS-11 above.

---

## 57-60. Desktop Security Audit

**Windows Hello — the most important positive verification.** Not a boolean gate. The vault key is AES-256-GCM-wrapped under a key derived from a **WebAuthn PRF (`hmac-secret`) output** that only the TPM-backed platform authenticator can produce, and only after user verification. Enforcement is thorough: resident key required, user verification required, platform attachment, PRF enabled, `credProtect` required, transport must be internal, **syncable passkeys rejected** (backup-eligible and backed-up flags must be clear, rechecked on every use), assertion signature verified against the public key pinned at enrollment, constant-time RP-ID hash comparison, canonical-CBOR enforcement, and the full envelope header used as GCM AAD. The envelope is written atomically with an explicitly protected current-user-SID-only DACL. This is stronger than DPAPI-only storage, because the wrapping key never exists outside the authenticator.

**macOS Touch ID:** correct `SecAccessControl` flags, correct accessibility class, `synchronizable = NO`, `kSecUseDataProtectionKeychain = YES`, a **fresh `LAContext` per operation** with reuse duration zero, and a non-prompting presence probe using `interactionNotAllowed`.

**Linux:** biometrics honestly unsupported and fail-closed to master password. No PAM/fprintd shim pretending to be equivalent. The CMake build hard-errors on unsupported platforms.

**Weaknesses:** DSK-01 through DSK-12 above. The most serious are the self-referential integrity check (DSK-02), the library-validation entitlements (DSK-03), the iconify-only lock (DSK-04), and clipboard history exposure (DSK-05).

---

## 61. Filesystem Permission Audit

| Path | Expected | Actual |
|---|---|---|
| Android app-private | UID-isolated | Correct by platform |
| iOS container | `NSFileProtectionComplete` | Correct except backup staging (IOS-01) |
| Desktop `~/.passvault` | 0700 | 0700 **after** a TOCTOU window; no Windows ACL (DSK-08) |
| Desktop `vault.db` | 0600 | Inherited; symlinks correctly rejected |
| Desktop `attachments/objects` | 0700 | **0755** (DSK-12), mitigated by parent |
| Desktop preview staging | 0700 dir / 0600 file | Correct on POSIX; **no-op on Windows** (DSK-07) |
| Desktop `biometric/` | owner-only | **Explicit protected DACL** — the best-hardened path |

---

## 62. Multiple-Instance Audit

**No prevention exists** (DSK-01). Two concurrent Desktop instances share the database and the biometric enrollment directory. Consequences: lost updates and stale reads (Room's invalidation tracker is per-process, and `enableMultiInstanceInvalidation` is not set), corrupted or orphaned platform credentials (atomic writes are last-writer-wins across processes), unlock-state confusion, and `SQLITE_BUSY` under WAL contention.

Android and iOS are unaffected — single-process by platform design, and iOS explicitly disables multiple scenes.

---

## 63. Privacy / No-Network Verification

**Verdict: STRONGLY SUPPORTED (not formally proven).**

| Vector | Result |
|---|---|
| ktor / okhttp / retrofit | **Absent** from `libs.versions.toml` |
| `HttpURLConnection`, `URLConnection`, `java.net.*` | **0 hits** in production |
| `Socket`, `ServerSocket`, `InetAddress` | **0 hits** |
| `NSURLSession`, `URLSession` | **0 hits** across Swift and `iosMain` |
| `WKWebView`, `android.webkit`, `WebView` | **0 hits** in code (docs prose only) |
| Firebase, Crashlytics, Sentry, analytics, telemetry | **0 hits** in code |
| `http://` / `https://` literals | Test fixtures only; one production scheme-prefix constant |
| Android permissions | **No `INTERNET`**; `ACCESS_NETWORK_STATE` explicitly removed |
| iOS ATS / background modes | Neither present |
| macOS entitlements | **No `network.client`** |

**Transitive:** koin, room, sqlite-bundled, okio, jna, zxing-core (decode-only), camerax, compose, libsodium-bindings are all local-only. Compose Multiplatform resources resolve from bundled assets — `Res.getUri()` returns a local URI, and there is no Coil or image-loader dependency. The `opentelemetry` and `analytics-library` entries in the verification metadata are AGP build-classpath artifacts, never packaged.

**Exceptions (legitimate, not app-originated I/O):** the app hands `http`/`https` URLs to the OS browser on explicit user tap (scheme-allowlisted, host-validated), and iOS can open its own Settings deep link. The app itself performs no request.

**Why not PROVEN:** static analysis cannot exclude reflection or a native path, and the native biometric bridge was not binary-analysed. To reach PROVEN: add a CI assertion on the merged manifest, plus a symbol scan of the native bridge.

---

## 64. Logging / Crash Leakage Audit

**Zero `println`, `Log.*`, `NSLog`, `Napier`, `Timber`, or `printStackTrace` in production Android, iOS, shared, core, or feature code.** The only production output is four `System.err.println` calls in the Desktop entry point, all static strings except one integer failure count.

**Exception-message sweep** across `core/crypto`, `core/database`, and `core/security` for interpolated values found exactly two hits, both **compile-time constants**: `"AEAD tags must be exactly $TAG_SIZE bytes"` and an `OBJECTS_DIRECTORY` prefix check. **No key material, plaintext, master password, path, or `SensitiveText` value is interpolated into any throw, require, or check.**

Repository errors are deliberately uniform (`"Unable to unlock vault"`, `"Vault operation failed"`), avoiding oracles — though the *timing* of those failures is still distinguishable (SES-09).

Koin logging is correctly gated: `Level.DEBUG` in debug, `Level.ERROR` in release on Android; `Level.ERROR` unconditionally on Desktop.

---

## 65. Resource Exhaustion / Malicious Input Audit

| Vector | Bounded |
|---|---|
| Backup OOM | **No** — BKP-02, BKP-03 |
| Disk exhaustion on restore | **No** — BKP-05 |
| Huge attachment | Yes — per-file and aggregate caps enforced mid-stream, not from declared size |
| Huge metadata | Partially — counts capped, but retained identifiers unbounded (BKP-03) |
| Extreme credential counts | Yes — 1,000,000 per category |
| Enormous strings | Yes — per-field code-point caps |
| Enormous Argon2 settings | Yes — bounds precede the KDF |
| Pathological search | Yes — decrypt-and-filter is linear |
| Repeated TOTP parsing | Yes — 8 KiB input cap applied before `trim()` |
| Expensive health analysis | Yes — O(n log n) |
| Decompression bombs | **N/A — no compression exists** |
| Attachment CPU exhaustion | **No** — BKP-13 |

Documented limits are genuinely enforced at read boundaries, not only on write — an important distinction that this codebase gets right in most places.

---

## 66. Version / Migration / Downgrade Audit

| Structure | Versioned | Downgrade-safe |
|---|---|---|
| Database schema | v3, exported, migrations 1→2→3 | Room rejects an unknown version rather than wiping |
| Crypto envelope | `50 56 02 00` magic, required | Older builds reject; newer rejects unversioned |
| Vault format | `vaultFormatVersion` 1..2, `cryptoFormatVersion == 2` | Strict equality on crypto version |
| Backup format | v2 magic and version, both in AAD | **v3 would fall into the v1 parser** (BKP-02) |
| Attachment container | v1 magic, `contentFormatVersion` checked | Correct |
| Settings | unversioned enum names | Normalised on load |

**Downgrade risk:** an older build opening a v3 database throws rather than corrupting — correct. The material gap is forward-compatibility error handling: a future backup version produces "incorrect password or corrupt backup" instead of "unsupported version", because it falls through to the legacy parser.

---

## 67-68. Dependency / Supply-Chain Audit

**Verification coverage is genuinely broad:** 1,483 components, **2,946 artifacts**, exactly one lowercase SHA-256 per artifact, **zero** MD5/SHA-1/SHA-512/PGP entries, only 3 trust rules (two for javadoc/sources jars that never reach a classpath, one for Groovy `.module` metadata — DSK/CI-09, Low). The custom `VerifyDependencyMetadataTask` enforces this shape and pins the trust-rule set byte-for-byte, so no additional rule can be introduced without failing CI. Coverage includes plugin markers, AGP, foojay, skiko, and 328 iOS-target `.klib` references.

**No dynamic versions, no HTTP repositories, no `mavenLocal()`.** All 40 versions are exact literals; `RepositoriesMode.FAIL_ON_PROJECT_REPOS` prevents any subproject from adding a repository. The Gradle distribution is SHA-256 pinned and CI validates the wrapper.

**Version concerns:** `detekt = 2.0.0-alpha.5` is a pre-release used as a release gate (CI-05). Kotlin 2.4.10, AGP 9.3.1, and compileSdk 37 are ahead of what I can independently confirm as generally available; they are internally consistent and the build succeeds, but they should be treated as fast-moving.

**Structural weaknesses:** TOFU-only verification with signatures structurally forbidden (CI-03); no Gradle or Ruby dependency updates (CI-04); no SBOM.

**Cryptographic provenance:** libsodium arrives via the ionspin KMP binding, one artifact per target, each SHA-256 pinned. All platforms use the same binding version, so there is no silent per-platform crypto divergence. The desktop native biometric bridge is built from in-repo source with a build-time architecture and duplicate-entry verification — good, though the *runtime* integrity check is the weak link (DSK-02).

---

## 69. Build / Gradle Audit

Custom verification tasks are real and executed: `verifyDependencies` validates the metadata policy; `verifyReleaseVersion` cross-checks `version.properties`, `PassVaultBuildInfo.VERSION`, and the iOS xcconfig. Both ran successfully.

The root `test` task is a genuine aggregate over 18 desktop and 18 Android host test tasks — the comment explaining why (Gradle's unqualified `test` would otherwise silently skip KMP suites) shows real care.

**Weakness:** `verifyReleaseSigningConfiguration` is registered but **not wired into `check`**, and `requireReleaseSigning` defaults to `false` (AND-08). Detekt reassigns the source set, which would silently analyse nothing if path resolution drifted (CI-05).

---

## 70-71. CI/CD and Release Provenance Audit

**Exceptionally strong on the highest-risk vectors:**

- **Zero `pull_request_target`, zero `issue_comment`, zero `workflow_run`** across all 11 workflows. The entire PR-head-checkout-with-secrets vulnerability class is absent.
- **Zero script injection.** Every `run:` block was examined. The only `github.event` reference is in an `if:` expression. All `workflow_dispatch` inputs route through `env:` indirection and anchored regex validation.
- **100% SHA-pinned actions** — 104 external `uses:` references, all 40-character hex. The pin validator is genuinely wired as a required gate (verified through the job graph to `ci-gate`).
- **Explicit `permissions:` in 11/11 workflows.** `contents: write` appears in exactly three places and is required in all three.
- **`ci.yml` — the only fork-reachable workflow — references zero secrets.**
- **OIDC replaces long-lived cloud keys** for Google and Azure; no service-account JSON exists.
- Attestations re-verified at five independent checkpoints with `--signer-workflow`, `--source-digest`, and `--deny-self-hosted-runners`; `sha256sum --check` runs three times; certificates pinned by fingerprint on all four platforms.

**Weaknesses:** desktop rebuild instead of byte-identical promotion (CI-01); the unattended promotion chain (CI-02); cache settings in signing jobs (CI-06); `jq`-asserted readiness states (CI-07).

**What passing CI proves:** the tree compiles, 1,160 host tests pass, detekt reports zero findings, dependency checksums match, and version metadata is consistent. **What it does not prove:** any iOS, macOS, hardware-biometric, or physical-device behaviour, and — for desktop — that the shipped bytes are the tested bytes.

---

## 72. Third-Party License Audit

23 license files plus `NOTICE.txt` and `THIRD_PARTY_NOTICES.md`, covering libsodium, JNA, skiko, Skia, FreeType, HarfBuzz, ICU, libjpeg-turbo, libpng, libwebp, zlib, expat, wuffs, dng-sdk, piex, checker-qual, slf4j, resource-loader, and the Spider Symbol font (OFL-1.1). Licenses are permissive and mutually compatible; there is no copyleft contamination of the shipped binary. A `verify-legal-notice-bundle.sh` and `verify-third-party-license-archive.sh` enforce byte-equality of the shipped bundle.

**Gap (LIC-01, Low):** notices are generated and verified for internal consistency, but never diffed against the *resolved* release dependency graph, so a newly added dependency could ship without attribution.

---

## 73-75. Test Suite Audit

**687 test functions across 77 files; 1,160 assertions executed.** The suite is bimodal.

**Genuinely load-bearing (~95 tests, real libsodium + real Room):** `RepositorySecurityIntegrationTest` (37), `VaultBackupServiceTest` (21), `SecurityTest` (23), `AttachmentRepositoryTest` (14). These prove real properties: locked reads fail, tampered ciphertext is rejected, nonces are fresh, no plaintext appears in the DB file, the leased VEK is zeroed after lock, failed restore preserves the existing vault (tested five ways), and — importantly — **attachment ciphertext transplantation is detected** by copying one attachment's file over another's path and asserting failure.

**The audit's own worry was refuted:** `FakeCryptoEngine` is a repeating-pad XOR with an FNV tag, but **no repository or persistence security test uses it**. It is confined to 105 of 687 tests (15.3%), all in unit-level plumbing tests.

**False confidence — the real problem:**

| Test | Why it proves nothing |
|---|---|
| `Argon2Test.kt` (entire file) | **Never calls Argon2.** Asserts data-class equality. No KAT vectors. |
| `CryptoEngineTest.decrypt with tampered ciphertext fails` | Calls `setShouldFail()` then decrypts `[1,2,3]` — asserts the mock's flag works |
| `CryptoEngineTest.modified authentication tag causes failure` | Same pattern; no tag is ever modified |
| `SecurityTest.constantTimeEquals resists timing attacks` | Measures two durations, asserts both are positive |
| `CryptoEngineTest.generateRandom produces different values` | **No assertions at all** |
| `VaultKeyHierarchyTest.generateVEK produces different keys` | No assertion |
| `VaultKeyHierarchyTest.deriveRecordKey fails with empty VEK` | No assertion |
| `VaultMigrationTest` data-survival | All payloads are `zeroblob()` — proves zeros survive, not that data stays decryptable |

**Tamper coverage on real crypto:** modified ciphertext, modified nonce, and wrong key are covered. **Missing:** explicit tag-region flip, engine-level truncation, **AAD mismatch**, and cross-context envelope swap.

### Missing security tests, ranked

1. **Argon2 known-answer vectors (RFC 9106)** — the KDF is unverified against any reference. A variant, parallelism, or endianness regression is invisible.
2. **Credential ciphertext transplantation** — swap `secretPayload` between two credential rows, assert failure. Attachments have this test; credentials do not.
3. **AAD mismatch at engine level** — encrypt with AAD A, decrypt with AAD B, assert failure.
4. **Cross-context envelope swap** — present `wrappedVek` bytes as the verification record.
5. **Explicit tag-region tamper on real crypto.**
6. **Engine-level truncation** — drop 1..n trailing bytes.
7. **Migration with genuinely encrypted payloads** — insert a real encrypted record at v1, migrate, assert it still decrypts.
8. **Nonce collision at scale** — 10k encryptions, assert zero repeats.
9. **Backup cancellation mid-restore** — the exact window of BKP-01.
10. **Post-lock memory assertion** — assert VEK bytes are zeroed, not merely that `isUnlocked()` is false.

---

## 76. Fuzz / Property-Test Recommendations

| Target | Properties to assert |
|---|---|
| `.pvault` parser | Never OOMs; never accepts a modified byte; failure never mutates the existing vault |
| Attachment container | Chunk reorder, truncation, and duplication always rejected |
| `otpauth://` parser | Never throws an unhandled exception; never produces an out-of-range configuration |
| Envelope | Modified authenticated data is never accepted |
| Migration | Any v1 or v2 database migrates without row loss and stays decryptable |
| Cancellation injection | Cancel at every suspension point in restore, import, and export; assert no divergence between DB and blob store |
| Lock monotonicity | Lock is monotonic until a successful authentication |
| Stale async | No completed async work can reopen a locked session |

---

## 77-79. Physical Verification Status

| Platform | Status |
|---|---|
| Android emulator | **Not performed** — no emulator on this host |
| Android physical device | **Not performed** |
| iOS simulator | **Impossible** — Windows host |
| iPhone/iPad physical | **Impossible** |
| Touch ID Mac | **Impossible** |
| Windows Hello hardware | **Not performed** — no enrolled authenticator |
| Linux desktop | **Not performed** |
| macOS filesystem | **Impossible** |
| Windows filesystem | Partially — the one symlink test could not run (privilege) |

**Not verified by any means available here:** biometric enrollment and invalidation on all platforms; background lock on real devices; recents-thumbnail behaviour; clipboard expiry on device; attachment preview and export through real OS handlers; iOS Keychain and Data Protection behaviour; macOS Touch ID; Windows Hello; multiple-instance behaviour; installer behaviour.

**Unit-test success does not establish any of the above.** Every platform-security claim in this report that is marked "source only" remains unverified on hardware.

---

## 80. Localization / RTL Audit

`verifyLocalization` **could not run** (Ruby absent), so key parity, plural parity, and placeholder parity are **unverified**. Arabic and English resource files exist for the design system, Android, and iOS `InfoPlist.strings`, and dedicated RTL/layout-direction policy code plus tests exist. Security-message translation accuracy is **unverified**.

---

## 81. Accessibility / Secret Exposure Audit

The iOS privacy cover correctly sets `accessibilityHidden` and `allowsHitTesting(false)`, preventing VoiceOver readout and touch pass-through behind the cover — a detail most implementations miss.

**Gap (UI-02):** `SecureTextField` does not set `semantics { password() }`. Masking is presentation-only, so while a password is revealed, a screen reader or any enabled accessibility service can announce it character by character. This is the standard tension between accessibility and concealment; the correct resolution is `password()` semantics, which tells assistive technology to treat the field appropriately rather than hiding it entirely.

---

## 82. Error Handling / Reliability Audit

Repository errors are uniform and non-informative — correct, and it avoids oracles. `CancellationException` is consistently rethrown rather than converted, which is the single most common Kotlin coroutine correctness bug and this codebase avoids it almost everywhere.

**An authentication failure never degrades to plaintext or a default** — verified across every decrypt path. `getOrThrow()` is used at crypto boundaries specifically so a tag failure propagates.

`@Suppress("TooGenericExceptionCaught")` appears where broad catches exist, each with a comment explaining that partially decrypted secrets must be cleared on every failure path. Those catches genuinely serve cleanup, not error swallowing.

**Weaknesses:** ATT-03 (one bad row aborts a batch), DB-07 (no corruption diagnosis), and the cancellation defects above.

---

## 83. Data-Loss Audit

| Path | Trigger | Likelihood | Recovery | Tested |
|---|---|---|---|---|
| **BKP-01** restore cancellation deletes attachments | User taps Cancel; process death | **Medium** | Only from another backup | **No** |
| **ATT-01** recovery sweep deletes all objects | Any caller of the public method | Low today, structural | None | **No** |
| ATT-03 filename decrypt aborts batch | One corrupt or legacy row | Medium in aged vaults | None from UI | No |
| ATT-04 export to `NUL` or normalised name | Windows filename | Low | None | No |
| BKP-05 disk exhaustion during restore | Large backup, small disk | Low | Vault intact | No |
| BKP-06 legacy restore orphans blobs | v1 restore | Low | n/a (retention issue) | No |
| BKP-07 version bookkeeping destroyed | Any v2 restore | **High** | Manual | No |
| DB-06 `entry_count` desync blocks backup | Direct DB manipulation | Low | Manual | No |
| DSK-01 concurrent instances | Two launches | **Medium** | None | No |
| DSK-11 cancelled export leaves temp | Cancel during export | Medium | Cosmetic | No |
| IOS-09 protected-data unavailable mid-write | Screen locks during save | Medium | Transaction rollback | No |

**None of these data-loss paths has a regression test.** For a password manager, availability failures are as serious as confidentiality failures — a user who permanently loses their vault has suffered a complete security failure.

---

## 84. Performance Audit

**No main-thread crypto** — `cryptoOperation` dispatches to `Dispatchers.Default`. **No main-thread disk I/O** in reviewed paths.

Argon2 at 64 MB / ops 3 gives acceptable unlock latency; large-vault unlock is O(n) decrypt on first list load. Backup and attachment work is genuinely streaming (256 KiB chunks), so memory stays bounded for legitimate files — the exhaustion risks come from hostile input, not scale.

**Excessive copies of secret buffers:** the `String` materialisation issues (CRY-02, OTP-02, HLT-01) are simultaneously performance and security problems. HLT-01 in particular allocates three-plus `String`s per credential on every health run.

**TOTP refresh regenerates every visible code once per second**, each regeneration re-deriving the key and re-decoding Base32 — wasteful, and the direct cause of OTP-02's copy accumulation.

---

## 85. Architecture / Maintainability Audit

**Strengths:** clean layered modules with no cycles; single ownership of the VEK; consistent `expect`/interface platform boundaries; a uniform `Result` error model; genuinely useful explanatory comments at non-obvious decisions (the okio disk-cache avoidance, the transaction-ordering rationale, the non-reentrancy notes).

**Issues:** `CredentialRepositoryImpl` is 1,387 lines with `@Suppress("LargeClass", "TooManyFunctions")`; `VaultRepositoryImpl` carries `TooManyFunctions`; a public concrete class is exposed in DI where an interface belongs (ATT-01); dead code (`title_hash` DB-04, `DELETING` ATT-08, unused `copy()` clipboard paths, `deriveSearchKey`/`deriveDuplicateKey` with no callers); a non-injective AAD helper inconsistent with the injective one in the same subsystem (ATT-05); and `single<Any> { Unit }` registering a root type (INF-01).

`@Suppress` usage is disciplined — each instance carries a justification comment, and none suppresses a security-relevant rule.

---

## 86-89. Documentation Audit and Existing-Audit Verification

### `docs/PRODUCTION_READINESS_AUDIT.md` — status verification

| Claim | Doc | My classification | Evidence |
|---|---|---|---|
| Crypto "engine, KDF, envelopes, AAD, subkeys" VERIFIED | :28 | **Partial** | `Argon2Test.kt:9-86` never calls Argon2; no KAT |
| "Database, encrypted repositories" VERIFIED | :29 | **Partial** | Payloads encrypted; extensive plaintext metadata not noted in this row |
| DATA-006 keyed blind-index columns | :137 | **Incorrect/Stale** | `title_hash` used by no query (DB-04) |
| SEC-010 biometric VERIFIED | :105 | **Stale** | Covers Android and iOS only; macOS and Windows bridge absent from the register |
| GATE-003 screen capture not claimed | :152 | **Confirmed** | `DesktopWindowProtection.kt:14-20` |
| SEC-007 buffer wipes VERIFIED | :102 | **Partial** | CRY-02 |
| BACKUP-001 "bounded legacy read support" | :138 | **Partial** | ~8x amplification (BKP-02) |
| ATTACH-001 validated cleanup VERIFIED | :139 | **Partial** | BKP-01 unregistered |
| GATE-009 zero-baseline detekt | :158 | **Confirmed** | Reproduced: 0 findings / 328 files |
| Implied static-analysis completeness | :150 | **Incorrect** | No CodeQL/SAST/secret scanning; Dependabot omits Gradle |
| Test counts 1,640 / 1,802 / 1,602 | :232-244 | **Stale** | Actual: **1,160** |
| Argon2 bounds 32-256 MB, ops 2-10 | — | **Confirmed** | Parallelism=1 undocumented |
| macOS/iOS/Xcode PASS rows | :227-248 | **Cannot verify** | Windows host |
| Throttling "progressive delays" | :33, :80 | **Partial** | In-memory, 5 s cap, restart-resettable |

### `PENTEST.md`

**Claimed tested:** Argon2id/KEK/VEK/envelopes/AAD/nonces/subkeys, best-effort wiping, vault lifecycle and throttling, raw Room row inspection, backup container parsing, clipboard, FLAG_SECURE, desktop concealment, biometric VEK release.

**Evidence is largely real**, and the document is commendably candid — it refuses third-party-certification framing and admits JVM strings are unwipeable.

**What it got wrong or missed:**
1. **:42-43 and :52 assert Desktop biometric unlock does not ship** — it does (DOC-01). The most privileged new attack surface is declared nonexistent.
2. **:50 lists attachment storage as a non-feature** — it ships.
3. No mention of plaintext metadata leakage, despite claiming raw Room row inspection.
4. No mention that throttling is in-memory, restart-resettable, and 5-second capped.
5. No mention of Argon2 parallelism = 1.
6. No supply-chain or SAST boundary discussion.
7. No DoS boundary — the legacy v1 amplification.
8. The dead `title_hash` index is unreviewed.

### Ranked documentation mismatches

1. **`PENTEST.md` denies shipped Desktop biometric unlock** — reviewers scope work from this file.
2. **macOS Touch ID and Windows Hello absent from the readiness register**; no gate blocks release on the desktop biometric path.
3. **"Crypto VERIFIED" with an Argon2 test that never calls Argon2.**
4. **Static-analysis and supply-chain posture undocumented and thin.**
5. **Plaintext metadata disclosed in only one of five security documents.**
6. **Memory-wipe *mechanism* misstated** — `SECURITY.md:12` and `SECURITY_MODEL.md:33` describe converting the password to a byte array and wiping it; an unwipeable `String` is created first (CRY-02). The "best-effort" qualifier is honest; the mechanism description is wrong.
7. **Throttling oversold** as "progressive delays".
8. **Stale test counts** (1,640/1,802/1,602 vs 1,160 actual).
9. **Legacy v1 backup called "bounded"** while `SECURITY_MODEL.md:155` concedes high amplification.
10. **Restore-cancellation attachment deletion unregistered**; Argon2 parallelism undocumented.

**Zero-network and no-telemetry claims: CONFIRMED** — the strongest verified claim in the document set.

**Per-platform biometric claims:** Android, iOS, macOS, Windows, and Linux claims all match the code. Undisclosed: Android StrongBox is never requested.


---

## 90. Recommended Remediation Order

### Immediate (before any further release)

1. **BKP-01** — set `committed` inside the transaction lambda; wrap in `NonCancellable`. *Silently destroys user data.*
2. **ATT-01** — make `recoverInterruptedOperations()` private; bind DI to interfaces; include STAGING in the reference set.
3. **SES-01 / SES-06** — stop holding `sessionMutex` across arbitrary work; make failed auto-lock retry on a bounded interval. *The lock is the product's core control.*
4. **DSK-04** — install the existing curtain synchronously in `lock()`. One-line fix, High-severity exposure.
5. **DOC-01** — correct `PENTEST.md`. A security document asserting a shipped attack surface does not exist is actively dangerous.

### Before production

6. **BKP-02** — sniff the v1 envelope prefix; cap or stream the legacy path; do not auto-preview non-v2 files.
7. **DSK-02 / DSK-06** — Authenticode verification with a pinned thumbprint; digest pinned in signed code; `/DEPENDENTLOADFLAG`; per-machine install.
8. **DSK-03** — remove `disable-library-validation` and `allow-unsigned-executable-memory`; sign all bundled natives.
9. **IOS-01 / IOS-02** — add the entitlements file with `default-data-protection`; apply protection attributes to backup staging.
10. **DSK-05** — native clipboard exclusion formats on Windows and macOS.
11. **DSK-01** — single-instance `FileLock`.
12. **CRY-02** — encode UTF-8 directly from `CharArray`; pass raw bytes to libsodium.
13. **DOM-02 / DOM-04** — raise the master-password minimum and enforce an entropy floor in the policy itself; fix the strength evaluator's length-versus-classes inversion.
14. **Add the top-5 missing security tests**: Argon2 KAT, credential transplantation, AAD mismatch, engine truncation, restore-cancellation.
15. **CI-01** — promote candidate desktop bytes or prove equality before signing.
16. **CI-02** — environment-gate `promote-release-branch`; add `CODEOWNERS`.

### Soon

17. ATT-02 (MAC row metadata), ATT-03, ATT-04, BKP-04..BKP-09, DB-04 (drop the dead index), AND-01, AND-03, AND-04/IOS-07, IOS-03..IOS-06, SES-02, SES-03, SES-08, UI-02, OTP-01..OTP-03, HLT-01, DSK-07..DSK-11, CI-03..CI-06.
18. Add CodeQL, secret scanning, and Gradle Dependabot; pin a stable detekt.
19. Reconcile all five security documents with the verified facts; register the desktop biometric bridge.

### Long term

20. Evaluate SQLCipher or platform file encryption for metadata (DB-01).
21. Pad ciphertext to bucketed lengths (DB-03).
22. Bind KDF parameters into the wrapped-VEK AAD (DB-02).
23. Add a diceware passphrase generator — the best practical mitigation for weak master passwords.
24. Opportunistic Argon2 rehash when stored parameters fall below current recommendations.
25. Property-based and fuzz testing for the backup parser, attachment container, and TOTP URI parser.
26. Hardware verification programme on real Android, iPhone, Touch ID Mac, and Windows Hello devices.

---

## 91. Production-Readiness Matrix

| Area | Rating | Explanation |
|---|---|---|
| Cryptographic design | **Ready** | Sound construction; correct primitives, hierarchy, and domain separation |
| Cryptographic implementation | **Ready with minor issues** | Correct; CRY-02 memory hygiene |
| Key management | **Ready with minor issues** | Strong separation; no VEK rotation; params unauthenticated (DB-02) |
| Master-password handling | **Not ready** | CRY-02, SES-08, DOM-02, DOM-04 compound |
| Password unlock | **Ready with minor issues** | Correct; SES-02, SES-03, SES-09 |
| Android biometric unlock | **Ready with minor issues** | Correct construction; no StrongBox (AND-03); no-op controller (AND-04) |
| iOS biometric unlock | **Cannot verify** | Source-correct and exemplary; IOS-03 orphaning; no hardware test |
| macOS biometric unlock | **Cannot verify** | Source-correct; no hardware test |
| Windows biometric unlock | **Not ready** | Cryptographically excellent, but DSK-02 and DSK-06 undermine runtime integrity |
| Session / auto-lock security | **Not ready** | SES-01 and SES-06 can prevent locking entirely |
| Credential storage | **Ready** | Correct encryption, AAD binding, atomic writes |
| TOTP | **Ready with minor issues** | RFC-conformant, independently verified; OTP-01..OTP-03 |
| Password generator | **Ready** | Provably unbiased; correct shuffle; raise the length floor |
| Attachment security | **Not ready** | Container is excellent; ATT-01 and ATT-02 |
| Backup creation | **Ready** | Format v2 is well designed and cancellation-safe |
| Backup restore | **Not ready** | BKP-01 destroys data |
| Database migrations | **Ready** | Exemplary — additive, transactional, rollback proven, no destructive fallback |
| Clipboard security | **Ready with minor issues** | Ownership correct on all platforms; DSK-05 on Windows |
| Screen privacy | **Ready with minor issues** | Android and iOS strong; DSK-04 on Desktop |
| Android platform security | **Ready with minor issues** | Minimal surface, triple-layer backup exclusion; AND-01 |
| iOS platform security | **Ready with minor issues** | Exemplary Data Protection; IOS-01, IOS-02 |
| Windows Desktop security | **Not ready** | DSK-01..DSK-07 |
| macOS Desktop security | **Not ready** | DSK-01, DSK-03, DSK-04 |
| Linux Desktop security | **Ready with minor issues** | Honest about no biometrics; DSK-01, DSK-04, DSK-08 |
| Privacy / no-network | **Ready** | Strongly evidenced across all layers |
| Dependency / supply chain | **Ready with minor issues** | Broad pinning; CI-03, CI-04 |
| CI / release pipeline | **Ready with minor issues** | Excellent on injection and pinning; CI-01, CI-02 |
| Data-loss resilience | **Not ready** | Two Critical paths, no regression tests |
| Automated tests | **Not ready** | Strong integration layer; misleading crypto unit layer; no Argon2 KAT |
| Hardware verification | **Cannot verify** | Nothing verified on real hardware |
| Documentation accuracy | **Not ready** | `PENTEST.md` denies a shipped attack surface |

---

## 92. Full Findings Index

| ID | Sev | Type | Platform | Module | File | Line | Finding | Confidence |
|---|---|---|---|---|---|---|---|---|
| BKP-01 | Critical | Data-loss | all | core:database | VaultBackupV2Service.kt | 169-199 | Cancellation deletes restored attachments | High |
| ATT-01 | Critical | Data-loss | all | core:database | AttachmentRepositoryImpl.kt | 300-310 | Public sweep deletes all objects | High |
| SES-01 | High | Session | all | core:database | VaultRepositoryImpl.kt | 382-394 | Lock blocked by lease | High |
| SES-06 | High | Session | all | shared | AutoLockTimer.kt | 40-44 | Failed auto-lock stalls | High |
| BKP-02 | High | Parser DoS | all | core:database | VaultBackupService.kt | 341-432 | ~1 GB pre-auth allocation | High |
| DSK-01 | High | Data-integrity | Desktop | app-desktop | Main.kt | 20-52 | No single-instance lock | High |
| DSK-02 | High | Supply-chain | Windows | app-desktop | DesktopBiometricNativeLoader.kt | 27-59 | Self-referential integrity | High |
| DSK-03 | High | Supply-chain | macOS | app-desktop | PassVault.entitlements | 13-18 | Library validation disabled | High |
| DSK-04 | High | Platform | Desktop | core:security | DesktopWindowProtection.kt | 93-103 | Lock only iconifies | High |
| DSK-05 | High | Secret-exposure | Desktop | core:security | DesktopClipboardService.kt | 58-77 | Clipboard history not excluded | High |
| IOS-01 | High | Storage | iOS | shared | IosBackupFileStore.kt | 364-378 | Backup staging unprotected | High |
| IOS-02 | High | Platform | iOS | iosApp | project.pbxproj | n/a | No entitlements file | High |
| CRY-02 | Medium | Secret-exposure | all | core:crypto | LibsodiumCryptoEngine.kt | 59 | Password in unwipeable Strings | Confirmed |
| DB-01 | Medium | Privacy | all | core:database | CredentialRecordEntity.kt | 29-71 | Plaintext metadata graph | High |
| DB-02 | Medium | Key-mgmt | all | core:database | VaultMetadataEntity.kt | 19-35 | KDF params unauthenticated | High |
| DB-03 | Medium | Privacy | all | core:database | PasswordHistoryRecordEntity.kt | 40-44 | Unpadded ciphertext leaks length | High |
| DB-04 | Medium | Privacy | all | core:database | CredentialRecordEntity.kt | 32-36 | Dead title_hash linkage oracle | High |
| ATT-02 | Medium | Data-integrity | all | core:database | AttachmentRepositoryImpl.kt | 205-207 | storage_state unauthenticated | High |
| ATT-03 | Medium | Data-loss | all | core:database | AttachmentRepositoryImpl.kt | 80 | One bad row bricks credential | High |
| ATT-04 | Medium | Data-loss | Windows | core:domain | AttachmentRepository.kt | 89-99 | Win32 reserved names accepted | Medium |
| AND-01 | Medium | Secret-exposure | Android | app-android | AndroidAttachmentFileStore.kt | 305-328 | Preview plaintext survives kill | Confirmed |
| AND-03 | Medium | Biometric | Android | app-android | AndroidBiometricKeyStore.kt | 263-286 | No StrongBox; implicit IV default | Confirmed |
| AND-04 | Medium | Biometric | Android | app-android | AndroidModule.kt | 52 | No-op prompt controller | Confirmed |
| IOS-07 | Medium | Biometric | iOS | shared | IosModule.kt | 23 | No-op prompt controller | Confirmed |
| SES-02 | Medium | Session | all | core:database | VaultRepositoryImpl.kt | 172-211 | Lock cannot pre-empt unlock | High |
| SES-03 | Medium | Session | all | core:database | VaultRepositoryImpl.kt | 186-188 | Volatile 5 s throttle | High |
| DOM-02 | Medium | Authentication | all | core:domain | MasterPasswordPolicy.kt | 11-15 | Length-only minimum | High |
| DOM-04 | Medium | Correctness | all | core:domain | PasswordStrengthEvaluator.kt | 20-39 | Strength inverts length vs classes | High |
| HLT-01 | Medium | Secret-exposure | all | feature:health | PasswordHealthAnalyzer.kt | 128-133 | Plaintext String proliferation | High |
| UI-02 | Medium | Secret-exposure | all | core:designsystem | SecureTextField.kt | 78-113 | No password semantics/autofill opt-out | Med-High |
| OTP-01 | Medium | Correctness | all | core:otp | TotpService.kt | 89-93 | Hard-coded modulus branch | High |
| OTP-02 | Medium | Secret-exposure | all | core:otp | TotpService.kt | 55 | Seed String per second | High |
| BKP-03 | Medium | Parser DoS | all | core:database | VaultBackupService.kt | 735-775 | Unbounded validator state | High |
| BKP-04 | Medium | Parser DoS | all | core:database | BackupV2Codec.kt | 322-334 | 256 MiB Argon2 ceiling | High |
| BKP-05 | Medium | Data-loss | all | core:database | VaultBackupV2Service.kt | 307-315 | No free-space precheck | High |
| BKP-06 | Medium | Privacy | all | core:database | VaultBackupService.kt | 939-941 | v1 restore orphans blobs | High |
| BKP-07 | Medium | Data-integrity | all | core:database | VaultBackupV2Service.kt | 468-480 | Version bookkeeping destroyed | High |
| BKP-08 | Medium | Data-integrity | all | core:database | VaultBackupV2Service.kt | 589-664 | Restore skips inner verification | High |
| BKP-09 | Medium | Key-mgmt | all | core:domain | BackupPasswordPolicy.kt | 4-22 | Backup password may equal master | High |
| DSK-06 | Medium | Supply-chain | Windows | app-desktop | CMakeLists.txt | 81-96 | DLL search-order planting | High |
| DSK-07 | Medium | Secret-exposure | Desktop | app-desktop | DesktopAttachmentFileStore.kt | 235-282 | Preview survives crash | Confirmed |
| DSK-08 | Medium | Storage | Desktop | core:database | Database.desktop.kt | 30-51 | Permission TOCTOU; no Windows ACL | Confirmed |
| DSK-10 | Medium | Session | Desktop | app-desktop | PassVaultDesktopWindow.kt | 240-247 | 30 s hardcoded focus-loss window | Confirmed |
| IOS-03 | Medium | Key-mgmt | iOS | shared | IosBiometricKeyStore.kt | 118-188 | Keychain VEK orphaned | Confirmed |
| IOS-04 | Medium | Usability | iOS | shared | IosAppLifecycleBridge.kt | 89-92 | Clipboard cleared on app switch | Confirmed |
| IOS-05 | Medium | Availability | iOS | shared | IosAppLifecycleBridge.kt | 128-154 | Permanent cover lockout | Confirmed |
| IOS-06 | Medium | Supply-chain | iOS | iosApp | project.pbxproj | 210,291 | Script sandboxing disabled | Confirmed |
| CI-01 | Medium | Supply-chain | Desktop | .github | release.yml | 153-204 | Rebuild, not byte promotion | High |
| CI-02 | Medium | Supply-chain | all | .github | candidate-readiness.yml | 192-274 | Unattended promotion chain | High |
| CI-03 | Medium | Supply-chain | all | gradle | verification-metadata.xml | 4-10 | TOFU; signatures forbidden | High |
| CI-04 | Medium | Supply-chain | all | .github | dependabot.yml | 1-10 | No Gradle/Ruby updates | High |
| CI-05 | Medium | Testing gap | all | root | detekt.yml | 1-68 | Alpha analyser is sole gate | High |
| CI-06 | Medium | Supply-chain | all | .github | release.yml | 195-198 | Cache in signing jobs | Medium |
| DOC-01 | Medium | Doc mismatch | Desktop | docs | PENTEST.md | 42-52 | Denies shipped biometrics | Confirmed |
| DOC-02 | Medium | Doc mismatch | all | docs | PRODUCTION_READINESS_AUDIT.md | 28 | KDF VERIFIED without KAT | Confirmed |

*(Low and Informational findings CRY-03..CRY-05, ATT-05..ATT-10, BKP-10..BKP-14, DB-05..DB-08, SES-04/05/07/08/09, OTP-03..OTP-06, AND-02/05/06/07/08, DSK-09/11/12, IOS-08/09/10, NAV-01, LIC-01, SCR-01/02, CI-07/08, INF-01..INF-12 are tabulated in Sections 22-23.)*

---

## 93. Audit Limitations

1. **No Apple toolchain.** All iOS and macOS findings are source-derived. No compilation, no simulator, no device.
2. **No biometric hardware.** Android BiometricPrompt, Face ID, Touch ID, and Windows Hello were never exercised.
3. **No physical devices or emulators.** Background lock, recents thumbnails, clipboard expiry, FLAG_SECURE, and file-picker behaviour are unverified at runtime.
4. **Native bridge not compiled.** CMake absent; the C++/ObjC++ was read but never built, linked, or fuzzed.
5. **Localization unverified.** Ruby absent; `verifyLocalization` never ran.
6. **Build cache was warm.** Most Gradle tasks reported UP-TO-DATE; results reflect cached outputs for this commit rather than fresh execution.
7. **No dynamic analysis.** No fuzzing, no instrumented heap inspection, no runtime memory scanning to confirm the `String` residency findings empirically (they are confirmed by source, not observation).
8. **No decompilation** of the release APK or the shipped native binaries.
9. **Transitive dependency source was not audited** — only versions, pinning, and reachability.
10. **External assumptions unverified:** GitHub branch protection, environment reviewer lists, signing-key custody, and Play/App Store account security all live outside the repository.
11. **Third-party crypto trusted.** libsodium and the ionspin binding were treated as correct implementations of their primitives; only PassVault's *use* of them was audited.
12. **Point-in-time.** Findings apply to `6a6336e` only.

---

## 94. Final Security Verdict

PassVault's cryptography is the strongest part of the system and it is genuinely good. The construction is textbook-correct, the AAD discipline is thorough enough to defeat every transplantation attack I attempted, the backup and attachment container formats are properly authenticated end to end, TOTP is RFC-conformant under independent recomputation, and Windows Hello is implemented with a rigour that exceeds most commercial password managers. The absence of any network capability is well evidenced. Database migrations are exemplary. The clipboard ownership model — the control most often botched in this product category — is correct on all three platforms.

The system is nonetheless **not production-ready**, for reasons that are mostly not cryptographic:

- Two **Critical data-loss defects** can silently destroy user attachments, and neither has a regression test.
- The **lock can fail to engage** and, once failed, can stall indefinitely — a defect in the product's central security control.
- Desktop platform hardening has gaps that undermine the strong native crypto sitting behind them.
- The **test suite's crypto layer overstates what it proves**: `Argon2Test` never calls Argon2, several tests assert nothing, and the KDF has no known-answer coverage.
- The **documentation misrepresents the shipped attack surface**, telling reviewers that Desktop biometric unlock does not exist.

None of these requires redesign. They are bounded, well-localized defects in code that is otherwise carefully written — the codebase shows genuine security craft throughout, including subtle details (cancellation-safe secret cleanup, non-reentrancy notes, deliberate DAO-less log tables, okio disk-cache avoidance) that only a thoughtful engineer adds.

**Recommendation:** fix the Immediate list, add the top-five missing tests, correct the documentation, then re-audit with hardware access before shipping to users with real credentials.

---

## 95. Final Statistics

| Metric | Value |
|---|---|
| HEAD SHA audited | `6a6336e6ff9e0cac56b274700345ec73d4c4d5e3` |
| Branch | `main` |
| Total tracked files | 631 |
| Reviewable files | 619 |
| Files actually reviewed | 631 (619 reviewed, 12 binary classified) |
| Generated/binary/vendor classified | 12 binary + 3 schema JSON + 1 verification metadata |
| Total code LOC | 61,862 |
| Production LOC reviewed | ~47,200 |
| Crypto/security LOC reviewed | ~9,800 |
| Platform LOC reviewed | ~11,400 |
| Test LOC reviewed | 14,629 |
| Build/config/script LOC reviewed | ~22,900 |
| **Total findings** | **68** |
| Critical | 2 |
| High | 9 |
| Medium | 24 |
| Low | 21 |
| Informational | 12 |
| Confirmed security vulnerabilities | 31 |
| Cryptographic findings | 5 |
| Key-management findings | 6 |
| Secret-exposure findings | 11 |
| Data-loss findings | 9 |
| Concurrency findings | 7 |
| Test gaps | 10 documented |
| Documentation mismatches | 10 ranked |
| Builds executed | 8 Gradle invocations |
| Build results | 5 PASS, 3 environment-blocked |
| Tests executed | 1,160 |
| Tests passed | 1,159 |
| Tests failed | 1 (Windows symlink privilege) |
| Tests skipped | 0 |
| Detekt | 328 files, 0 findings, did not crash |
| Android verification | Host unit tests only; no device or emulator |
| iOS verification | **None** — impossible on Windows |
| Desktop verification | Compilation only; native bridge not built |
| Hardware tests unavailable | All biometric, all mobile device, all Apple |
| Remaining unverifiable areas | 12 (Section 93) |
| **Overall audit confidence** | **High** for source-derived findings on common, Android, and Desktop Kotlin; **Medium** for iOS/macOS/native (source only); **Low** for all runtime and hardware behaviour |

---

## 96. Explicit Answers to All Required Questions

1. **Is the cryptographic construction sound?** Yes. Random VEK wrapped by an Argon2id-derived KEK, per-purpose BLAKE2b subkeys with injective domain separation, AEAD everywhere, separate verification record. No unauthenticated fallback.

2. **Is XChaCha20-Poly1305 used correctly everywhere?** Yes. Fresh 24-byte random nonces, enforced 32-byte keys, authentication always checked, ciphertext never used before authentication, AAD binding on every path.

3. **Is Argon2id configured and parsed safely?** Parsing is safe — bounds are validated before the KDF in both vault and backup paths. Configuration is adequate (64 MB/ops 3) but below what many managers use for a master key, parallelism is silently forced to 1, weak legacy parameters persist without rehash, and there are no known-answer tests.

4. **Are keys properly separated by purpose?** Yes — credentials, history, attachments, backups, search, and duplicate detection all use distinct contexts under an injective encoding.

5. **Can valid ciphertext be transplanted between records, vaults, attachments, or backups?** **No.** Blocked in every path tested.

6. **Can database manipulation expose or undetectably modify secrets?** Expose: no. Undetectably modify: **partially yes** — `storage_state` (ATT-02) and the Argon2 parameter block (DB-02) are unauthenticated. Neither exposes plaintext, but both enable denial and downgrade.

7. **What metadata remains visible without the master password?** Entry count, credential types, favourites, complete folder and tag graphs, millisecond timestamps, attachment sizes/MIME/counts, folder icons, tag colours, password-history counts and rotation timestamps, Argon2 parameters, vault ID, and — via unpadded ciphertext — password lengths.

8. **Can the master password leak through memory/UI/state/logging?** Not through logging, exceptions, persistence, or saved state. **Yes through memory** — two unwipeable `String` copies (CRY-02) plus ViewModel state (SES-08). Also via accessibility while revealed (UI-02).

9. **Can vault keys remain usable after lock?** No. The VEK is zeroed and mutex ordering guarantees a leased copy is wiped before lock proceeds. The real risk is the inverse: lock may not run at all (SES-01, SES-06).

10. **Can a stale asynchronous operation reopen or repopulate a locked session?** **Yes, narrowly** — SES-02 (an unlock completing after a lock request opens a session) and SES-07 (a buffered navigation effect delivered post-lock).

11. **Is biometric unlock cryptographically bound to an OS-protected secret on every supported platform?** Yes on Android, iOS, macOS, and Windows. Linux honestly has none. The released key is always verified against the vault before use.

12. **Can biometric enrollment changes or fallback weaken the unlock model?** No. Every platform invalidates on enrollment change, and Android uses `BIOMETRIC_STRONG` only with no device-credential fallback.

13. **Are `.pvault` backups cryptographically sound?** **Yes for v2** — comprehensively authenticated. The legacy v1 path is materially weaker and still reachable.

14. **Can a malicious `.pvault` cause memory, CPU, disk, parser, or state attacks?** **Yes** — memory (BKP-02, BKP-03), CPU (BKP-04, BKP-13), disk (BKP-05). Parser and state attacks are blocked.

15. **Can failed restore cause data loss?** **Yes — BKP-01**, the most serious finding. Failed restore correctly preserves the vault, but *cancelled* restore can delete the attachments of a committed one.

16. **Are attachment files independently authenticated and bound to their credential/vault?** Yes — bound to attachment ID, credential ID, key context, and MIME type, with chunk index and terminal marker authenticated.

17. **Can plaintext attachment staging files survive unexpectedly?** **Yes** on Android (AND-01), Desktop (DSK-07), and briefly on iOS. Startup sweeps mitigate but only at next launch.

18. **Is TOTP implemented according to RFC 6238?** **Yes — confirmed**, verified by hand and by independent recomputation of all 18 Appendix B vectors.

19. **Are TOTP seeds protected as strongly as passwords?** At rest, yes. In memory, worse — regenerated into unwipeable `String`s once per second (OTP-02), and cleared only on next-frame disposal (OTP-03).

20. **Does the password generator provide the entropy it claims?** Yes — 103.1 bits at length 16 with all sets; the required-character constraint costs 0.25 bits. Provably unbiased sampling and a correct shuffle. The 8-character minimum is too low.

21. **Is all security-sensitive randomness cryptographically secure?** Yes — every value derives from libsodium's CSPRNG. No weak RNG appears anywhere in production.

22. **Can clipboard handling delete another app's content or leave secrets too long?** Delete others': **no** on all three platforms — genuinely correct ownership checks. Leave too long: **yes on Windows**, where secrets persist in Clipboard History and Cloud Clipboard beyond the timer (DSK-05).

23. **Does screen-capture protection behave as documented?** Yes. Android and iOS are strong; Desktop has none and says so. The gap is that Desktop `lock()` does not use the curtain it already has (DSK-04).

24. **Are Android backups/data extraction configured safely?** Yes — disabled at three independent layers including device transfer.

25. **Are iOS Keychain and Data Protection configurations appropriate?** The Keychain configuration is optimal. Data Protection is correct for the database and attachments but **missing for backup staging** (IOS-01) and lacks the container-wide default entitlement (IOS-02).

26. **Is Desktop storage reasonably protected given the weaker sandbox?** Partially. Symlink rejection and 0700 permissions are good; the TOCTOU window, absent Windows ACLs, no single-instance lock, and iconify-only lock leave real gaps.

27. **Does PassVault perform zero runtime network communication?** **Strongly supported.** No networking dependency, symbol, permission, or entitlement anywhere. The only exception is handing a user-tapped URL to the OS browser.

28. **Is sensitive data written to logs or crash output?** **No.** Zero logging in production security code; no secret is interpolated into any exception, `require`, or `check` message.

29. **Can malformed local/imported data cause compromise or data loss?** Compromise: no. **Resource exhaustion: yes** (BKP-02..BKP-05). Data loss from a *malformed* file: no — but from *cancellation* during a valid restore: yes (BKP-01).

30. **Are database migrations safe and non-destructive?** **Yes — exemplary.** Additive only, transactional, rollback proven by test, and `fallbackToDestructiveMigration` absent from the entire repository.

31. **Can downgrading the application damage or weaken an existing vault?** No. An older build rejects a newer schema and rejects unknown crypto versions rather than corrupting. The gap is forward-compatibility *error messaging* for future backup versions.

32. **Is restore atomic?** The database portion is genuinely atomic and a failed restore preserves the vault. The **attachment portion is not** — BKP-01.

33. **Are credential/attachment modifications atomic?** Credentials: yes, verified in generated Room code. Attachments: fail-closed by ordering, but ATT-01 can break the invariant externally.

34. **Is the release build materially different from Debug in a dangerous way?** No. Release is correctly hardened. Two caveats: `storeScreenshot` shares the debug applicationId (AND-07), and release can silently fall back to debug signing (AND-08).

35. **Does dependency verification protect the complete build?** It covers the complete build broadly (2,946 artifacts including plugins and native targets), but it is **trust-on-first-use only**, and the project's own gate forbids enabling signature verification (CI-03).

36. **Are cryptographic/native dependencies trustworthy and pinned?** Pinned: yes, exactly. Trustworthy: libsodium via a reputable binding, same version on all platforms. The **runtime** integrity of the native desktop bridge is the weak link (DSK-02, DSK-06).

37. **Do CI and release workflows protect artifact provenance?** Largely yes — SHA-pinned actions, no injection, no dangerous triggers, Sigstore attestations verified at five checkpoints. Two gaps: desktop rebuild instead of promotion (CI-01) and the unattended chain (CI-02).

38. **Do current tests genuinely prove the important security properties?** **Partially.** The integration layer does. The crypto unit layer does not — `Argon2Test` never calls Argon2, and several tests assert nothing.

39. **Which important security properties are not currently tested?** Argon2 correctness against vectors; credential ciphertext transplantation; AAD mismatch; engine-level truncation and tag tamper; nonce uniqueness at scale; restore cancellation; migration with real encrypted payloads; post-lock memory zeroing.

40. **Which previous claims are independently confirmed?** Confirmed: zero-network, screen-capture honesty, detekt zero-baseline, Argon2 bounds, no destructive migration. Partial: crypto VERIFIED, database encryption, buffer wipes, backup boundedness, throttling. Incorrect or stale: Desktop biometrics denied, `title_hash` claim, test counts, static-analysis completeness. Cannot verify: all Apple rows.

41. **Did this audit find issues the previous reports missed?** Yes — both Critical findings (BKP-01, ATT-01), both lock-liveness defects (SES-01, SES-06), the legacy backup amplification (BKP-02), all Desktop supply-chain findings (DSK-02, DSK-03, DSK-06), the iOS Data Protection gaps (IOS-01, IOS-02), the unwipeable password `String`s (CRY-02), the dead blind index (DB-04), the unauthenticated `storage_state` (ATT-02), and the false-confidence tests.

42. **Five greatest risks to confidentiality?** (1) CRY-02 master password in memory; (2) DSK-05 clipboard history and cloud sync; (3) AND-01/DSK-07 plaintext staging surviving process death; (4) DB-01/DB-03 metadata and length leakage; (5) DSK-02/DSK-06 native bridge substitution yielding the vault key.

43. **Five greatest risks to data integrity?** (1) BKP-01; (2) ATT-01; (3) DSK-01 concurrent instances; (4) ATT-02 unauthenticated `storage_state`; (5) BKP-07 destroyed version bookkeeping.

44. **Five greatest risks to availability/data loss?** (1) BKP-01; (2) ATT-01; (3) ATT-03 bricked attachment operations; (4) DSK-01; (5) DB-06 `entry_count` desync blocking backup.

45. **Is PassVault suitable for storing real high-value credentials today?**

Not yet, and the reason is availability rather than confidentiality. **Verified:** the encryption is sound, ciphertext cannot be transplanted or undetectably modified, there is no network exfiltration path, and biometric unlock is properly key-bound on every supported platform. **Likely safe:** metadata leakage is a deliberate, documented trade-off that matters mainly against an attacker who already has your database file. **Still risky:** two Critical paths can silently destroy attachments, the lock can fail to engage, and Desktop plaintext staging and clipboard history extend secret lifetime beyond what the design intends. **Blocked:** nothing technically — every finding is fixable without redesign. **Externally unverified:** all Apple platform behaviour, all biometric hardware, and all real-device runtime behaviour.

A user storing high-value credentials risks losing attachments and, on Desktop, having secrets outlive their intended lifetime. The master secrets themselves are well protected.

46. **Is PassVault production-ready today?**

No — but it is closer than the finding count suggests, and the gap is specific rather than systemic.

**Verified ready:** cryptographic design and implementation, credential storage, backup creation, database migrations, TOTP, the password generator, the no-network guarantee, and the CI injection/pinning posture.

**Likely safe with minor work:** Android and iOS platform security, clipboard, screen privacy, dependency pinning.

**Still risky and blocking:** backup restore (BKP-01), attachment lifecycle (ATT-01), session lock liveness (SES-01, SES-06), Desktop platform hardening (DSK-01..DSK-07), and a test suite whose crypto layer claims more than it proves.

**Blocked on evidence, not code:** every Apple platform claim and every hardware-biometric claim. These cannot be signed off from a Windows host and require a real verification programme.

**Externally unverified:** branch protection, environment reviewers, signing-key custody, and store-account security.

The engineering quality here is high — this reads as a codebase built by someone who understands the threat model and cares about the details. What it lacks is not competence but *closure*: two data-loss defects, two lock-liveness defects, honest documentation, and hardware verification stand between this and a defensible production release.

---

*End of report. Audit performed read-only; working tree verified unmodified at `6a6336e6ff9e0cac56b274700345ec73d4c4d5e3`.*


---

## 97. Erratum — Findings Count Correction (added during issue-backlog archival)

**This section was appended during the 22-8 issue-backlog archival. No original audit text above has been altered.**

Section 95 ("Final Statistics") reports **68 total findings** (2 Critical / 9 High / 24 Medium / 21 Low / 12 Informational). That summary is inconsistent with the findings actually enumerated in this report.

Enumerating every Finding ID defined in Sections 19–23 yields **111 unique findings**, with no duplicates:

| Severity | Section 95 claim | Actually enumerated | Source of truth |
|---|---|---|---|
| Critical | 2 | 2 | Section 19 headings |
| High | 9 | 10 | Section 20 headings |
| Medium | 24 | 43 | Section 21 headings (`AND-04 / IOS-07` defines two IDs) |
| Low | 21 | 44 | Section 22 table rows |
| Informational | 12 | 12 | Section 23 table rows |
| **Total** | **68** | **111** | |

This is corroborated independently by the Section 92 note, which enumerates the Low/Informational set as `CRY-03..05, ATT-05..10, BKP-10..14, DB-05..08, SES-04/05/07/08/09, OTP-03..06, AND-02/05/06/07/08, DSK-09/11/12, IOS-08/09/10, NAV-01, LIC-01, SCR-01/02, CI-07/08, INF-01..12` = 44 Low + 12 Informational = 56. The Section 92 index table itself contains 55 rows (2 Critical + 10 High + 43 Medium). 55 + 56 = 111.

Identifiers such as `CRY-01`, `DOM-01`, `DOM-03`, `UI-01`, `UI-03`, `TST-01..11`, `GEN-01..04`, `NAV-02`, `DOC-03`, `DOC-05`, `DOC-09` appear only inside coverage-ledger ranges or narrative prose and are **not** separately enumerated findings; they are correctly excluded from the 111.

The corrected total of **111** is the basis for the GitHub issue backlog created from this review. All severity ratings, technical content, and conclusions in Sections 1–96 remain exactly as originally written.

---
