# Threat model

Last reviewed: 2026-07-28

## Assets

Master passwords, the vault encryption key (VEK), credential secrets and notes, folder/tag names, password history,
backup passwords, decrypted backups, and clipboard values are sensitive. Database relationship/timing metadata is
privacy-relevant even where it is intentionally not encrypted.

## Trust boundaries

- The locked local database or copied `.pvault` file may be controlled by an attacker.
- The application process and OS are trusted only while the user is actively using an unlocked vault.
- Android/Desktop file pickers, clipboard managers, window managers, keyrings, filesystems, and crash facilities are
  platform boundaries, not cryptographic peers.
- Build repositories and CI artifacts are supply-chain boundaries.

There is no server, cloud, account, telemetry, analytics, browser extension, biometric, or remote synchronization
boundary in this codebase.

## Defended scenarios

| Threat | Implemented control |
|---|---|
| Offline database theft | Argon2id-protected wrapped random VEK; application-level authenticated record encryption |
| Modified/wrong ciphertext | XChaCha20-Poly1305 authentication, contextual AAD, strict envelope parsing |
| Reused nonce | fresh cryptographic random nonce; automated uniqueness regression tests |
| Cross-record substitution | record/purpose-specific key contexts and AAD |
| Plaintext equality index theft | vault-keyed deterministic BLAKE2b blind indexes |
| Malicious/corrupt backup | bounded/versioned parser, authenticated container, full validation before transactional replace |
| Wrong backup password | authenticated decryption failure without database write |
| Newer clipboard data erased | ownership token/value check before expiration clear |
| Background/manual exposure | centralized session lock and best-effort VEK wipe |
| Screenshot capture on Android | `FLAG_SECURE` on sensitive host windows |
| Dependency replacement | committed SHA-256 Gradle verification metadata |

## Residual and out-of-scope threats

- Malware, an accessibility service, debugger, keylogger, or memory reader on an unlocked endpoint can read secrets.
- Managed Kotlin strings, garbage-collected copies, swap/pagefile, hibernation, and OS crash dumps cannot be
  guaranteed wipeable.
- Structural SQLite metadata remains observable; blind indexes reveal equality within a vault.
- Android overlay/accessibility behavior and Desktop capture/keyring behavior require platform hardening beyond
  common code.
- No biometric unlock is present. A future implementation would require an OS-protected cryptographic key operation,
  enrollment invalidation, fallback policy, and device tests.
- Publisher signing/notarization, secure update delivery, an independent penetration test, and a disclosure channel
  are external release dependencies.

## Security invariants

- Never persist the master password, KEK, plaintext VEK, or decrypted record payload.
- Never log or expose a password, key, decrypted value, ciphertext, raw SQL, or sensitive path in UI errors.
- Never access encrypted repositories while locked.
- Never accept unknown crypto/backup versions or invalid KDF parameters.
- Never mutate vault tables until an imported backup is completely read, authenticated, decoded, and validated.
- Never clear clipboard data that no longer belongs to PassVault.
- Never represent a confirmation dialog or unsupported adapter as biometric authentication.

Automated tests are regression evidence, not an independent security certification. Exact verification status lives
in [PRODUCTION_READINESS_AUDIT.md](PRODUCTION_READINESS_AUDIT.md).

