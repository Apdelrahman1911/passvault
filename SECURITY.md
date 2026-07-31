# PassVault security

PassVault is security-sensitive software under active hardening. It has internal source review and automated tests,
but no independent audit, penetration-test certification, MASVS certification, or public release attestation.

## Implemented controls

- Argon2id derives a key-encryption key from the master password and a per-vault random salt.
- A random 256-bit vault-encryption key is wrapped with XChaCha20-Poly1305 and authenticated associated data.
- Record, folder, tag, history, blind-index, and attachment-metadata keys are purpose-separated with keyed BLAKE2b.
- Persisted encrypted values use versioned authenticated envelopes and unique random 24-byte nonces.
- The supported backup format derives an independent key from a separate backup password and validates the complete
  snapshot before a transactional restore.
- Manual/background/inactivity lock clears the active vault-key buffer best-effort; unlock failures are throttled.
- Clipboard expiry clears only content still owned by PassVault. Android sensitive screens use `FLAG_SECURE`.
- Database, cryptographic, parser, and file details are mapped to non-sensitive user errors at UI boundaries.
- Gradle dependencies are checked against committed SHA-256 verification metadata.

Managed Kotlin/JVM strings and garbage-collected copies cannot be guaranteed wipeable. The Room database file is not
SQLCipher; sensitive record payloads are encrypted by the repository layer, while timestamps, identifiers, types,
favorite flags, and relationship metadata remain visible to someone who obtains the database.

## Unsupported security claims

Biometric vault unlock is not shipped, and no biometric UI, dependency, or platform adapter is part of the release
graph. PassVault also has no cloud sync, TLS/certificate-pinning boundary, root/jailbreak detection, account service,
CSV export, or attachment-file pipeline.

## Reporting vulnerabilities

This checkout does not include a verified security email, GPG key, response SLA, project owner, or hosted disclosure
channel. Until the publisher adds and verifies one, use a private channel controlled by the repository owner and do
not attach master passwords, decrypted credentials, or real `.pvault` files. Do not open a public issue for an
unpatched vulnerability.

## Release checklist

- [ ] Independent expert review and penetration test.
- [ ] Android device tests for lifecycle locking, screenshot protection, clipboard ownership, and storage pickers.
- [ ] Desktop graphical smoke tests for focus/lock, clipboard, tray, keyring, and file dialogs.
- [ ] Previous-release database migration fixture and test, when such a release exists.
- [ ] Publisher-provided license, ownership, disclosure contact, signing, and notarization.
- [ ] Full tests, static analysis, Android release/R8 build, and current-host Desktop package pass from a clean
  checkout.

Implementation detail and current evidence are recorded in
[`docs/SECURITY_MODEL.md`](docs/SECURITY_MODEL.md) and
[`docs/PRODUCTION_READINESS_AUDIT.md`](docs/PRODUCTION_READINESS_AUDIT.md).
