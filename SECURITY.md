# PassVault security

PassVault is security-sensitive software under active hardening. It has internal source review and automated tests,
but no independent audit, penetration-test certification, MASVS certification, or public release attestation.

## Implemented controls

- Argon2id derives a key-encryption key from the master password and a per-vault random salt.
- A random 256-bit vault-encryption key is wrapped with XChaCha20-Poly1305 and authenticated associated data.
- Record, folder, tag, history, blind-index, attachment-metadata, and attachment-content keys are purpose-separated
  with keyed BLAKE2b.
- Persisted encrypted values use versioned authenticated envelopes and unique random 24-byte nonces.
- The supported backup format derives an independent key from a separate backup password, authenticates a streamed
  record transcript, stages attachment objects, and validates the complete snapshot before transactional Room
  replacement; previous-vault biometric-key deletion is a separate OS boundary.
- Each attachment is an independently authenticated, size-bounded, atomically written app-private object. Its owner,
  identity, declared size, MIME metadata, and format are cryptographically bound; database staging states and cleanup
  prevent partial files or rows from becoming visible.
- Manual/background/inactivity lock clears the active vault-key buffer best-effort; unlock failures are throttled.
- Opt-in mobile biometric unlock releases the same VEK through an auth-per-use Android Keystore key or a
  device-only iOS Keychain item bound to the current biometric enrollment; the candidate VEK must still authenticate
  the vault verification record before a session is published.
- Clipboard expiry clears only content still owned by PassVault. Android sensitive screens use `FLAG_SECURE`.
- Database, cryptographic, parser, and file details are mapped to non-sensitive user errors at UI boundaries.
- Gradle dependencies are checked against committed SHA-256 verification metadata.

Managed UI/IME strings and garbage-collected copies cannot be guaranteed wipeable. Inside the KDF boundary,
PassVault encodes `SensitiveText` directly into mutable UTF-8 and compatibility-preserving hexadecimal buffers and
clears those buffers after derivation; this narrows exposure but cannot guarantee erasure on a managed runtime. The
Room database file is not SQLCipher; sensitive record payloads are encrypted by the repository layer, while
timestamps, identifiers, types, favorite flags, and relationship metadata remain visible to someone who obtains the
database.

## Unsupported security claims

Desktop biometric vault unlock is not shipped. PassVault also has no cloud sync, TLS/certificate-pinning boundary,
root/jailbreak detection, account service, or CSV export. Mobile biometric and attachment-picker source/automated
tests do not substitute for physical-device prompt, provider, cancellation, backgrounding, and cleanup evidence.

## Reporting vulnerabilities

This checkout does not include a verified security email, GPG key, response SLA, project owner, or hosted disclosure
channel. Until the publisher adds and verifies one, use a private channel controlled by the repository owner and do
not attach master passwords, decrypted credentials, or real `.pvault` files. Do not open a public issue for an
unpatched vulnerability.

## Release checklist

- [ ] Independent expert review and penetration test.
- [ ] Android device tests for lifecycle locking, screenshot protection, clipboard ownership, backup/attachment
  storage pickers, attachment preview/export, provider failure, and cancellation.
- [ ] Android and iOS physical-device biometric tests for success, cancellation, lockout, backgrounding, process
  recreation, and enrollment-change invalidation.
- [ ] Desktop graphical smoke tests for focus/lock, clipboard, tray, minimize/focus concealment, and file dialogs;
  portable screenshot prevention is not claimed.
- [x] Exported Room schemas 1/2/3 and non-destructive 1 -> 3, 2 -> 3, fresh-schema, query-plan, and rollback tests.
- [ ] Publisher ownership/disclosure contacts plus release signing and notarization are independently verified; the
  repository's Apache-2.0 license and third-party notices are included in release artifacts.
- [ ] Full tests, static analysis, Android release/R8 build, and current-host Desktop package pass from a clean
  checkout.

Implementation detail and current evidence are recorded in
[`docs/SECURITY_MODEL.md`](docs/SECURITY_MODEL.md) and
[`docs/PRODUCTION_READINESS_AUDIT.md`](docs/PRODUCTION_READINESS_AUDIT.md).
