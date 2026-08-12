# Threat model

Last reviewed: 2026-08-11

## Assets

Master passwords, the vault encryption key (VEK), credential secrets and notes, TOTP setup keys and generated codes,
folder/tag names, password history, backup passwords, decrypted backups, and clipboard values are sensitive.
Database relationship/timing metadata is privacy-relevant even where it is intentionally not encrypted.

## Trust boundaries

- The locked local database or copied `.pvault` file may be controlled by an attacker.
- The application process and OS are trusted only while the user is actively using an unlocked vault.
- Mobile cameras and biometric services, Android/Desktop file pickers, clipboard managers, window managers,
  Keystore/Keychain, filesystems, and crash facilities are platform boundaries, not cryptographic peers.
- Build repositories and CI artifacts are supply-chain boundaries.

There is no server, cloud, account, telemetry, analytics, browser extension, or remote synchronization boundary in
this codebase.

## Defended scenarios

| Threat | Implemented control |
|---|---|
| Offline database theft | Argon2id-protected wrapped random VEK; application-level authenticated record encryption |
| Modified/wrong ciphertext | XChaCha20-Poly1305 authentication, contextual AAD, strict envelope parsing |
| Reused nonce | fresh cryptographic random nonce; automated uniqueness regression tests |
| Encrypted-payload substitution between record IDs or purposes | record/purpose-specific key contexts and AAD |
| Plaintext equality index theft | vault-keyed deterministic BLAKE2b blind indexes |
| Malicious/corrupt backup | bounded/versioned parser, authenticated container, full validation before transactional replace |
| Wrong backup password | authenticated decryption failure without database write |
| Newer clipboard data erased | ownership token/value check before expiration clear |
| Background/manual exposure | centralized session lock and best-effort VEK wipe |
| Screenshot capture on Android | `FLAG_SECURE` on sensitive host windows |
| Copied mobile device with biometric enabled | Android Keystore auth-per-use key or device-only iOS Keychain item; candidate VEK is verified before session publication |
| Biometric enrollment changes | Android key invalidation and iOS `biometryCurrentSet`; failed release removes app enrollment state |
| Malformed TOTP enrollment | strict local URI/Base32 parsing, bounded parameters, and QR image limits |
| Dependency replacement | committed SHA-256 Gradle verification metadata |

## Residual and out-of-scope threats

- Malware, an accessibility service, debugger, keylogger, or memory reader on an unlocked endpoint can read secrets.
- Managed Kotlin strings, garbage-collected copies, swap/pagefile, hibernation, and OS crash dumps cannot be
  guaranteed wipeable.
- Structural SQLite metadata remains observable; blind indexes reveal equality within a vault.
- Credential type/folder/favorite/timestamps, folder/tag relationships, row ordering, and other routing metadata are
  not included in record-payload AAD. A database attacker can tamper with that structural metadata without an AEAD
  failure; authenticated payload substitution between record IDs or purposes is still rejected. Binding routing
  metadata requires a versioned AAD/schema migration.
- New backup import streams independently bounded metadata rows and 256 KiB attachment records, using a two-pass
  authenticated transcript before atomic Room replacement. A maximum-size 65 MiB metadata row can still cause a
  roughly 130–195 MiB transient managed-memory peak. Legacy format-1 compatibility retains its 128 MiB JSON/Base64
  container and 64 MiB snapshot limits and can amplify memory substantially on constrained devices.
- Room replacement and deletion of the previous vault's OS biometric item cannot share a transaction. A Room
  failure after biometric deletion leaves the prior database intact. PassVault attempts to restore the former
  enrollment, but an OS key-store failure can still require biometric re-enrollment.
- Android overlay/accessibility behavior and Desktop capture/window behavior require platform hardening beyond
  common code.
- Biometric prompts and enrollment invalidation depend on OS behavior and require physical-device testing. A
  compromised unlocked process can still copy the active VEK before platform enrollment.
- Publisher signing/notarization, secure update delivery, an independent penetration test, and a disclosure channel
  are external release dependencies.

## Security invariants

- Never persist the master password, KEK, plaintext VEK, or decrypted record payload.
- Never log or expose a password, key, decrypted value, ciphertext, raw SQL, or sensitive path in UI errors.
- Never access encrypted repositories while locked.
- Never accept unknown crypto/backup versions or invalid KDF parameters.
- Never mutate vault tables until an imported backup is completely read, authenticated, decoded, and validated.
- Never clear clipboard data that no longer belongs to PassVault.
- Never publish a biometric session until an OS-protected operation releases a candidate VEK and the authenticated
  vault verification record accepts it.

Automated tests are regression evidence, not an independent security certification. Exact verification status lives
in [PRODUCTION_READINESS_AUDIT.md](PRODUCTION_READINESS_AUDIT.md).
