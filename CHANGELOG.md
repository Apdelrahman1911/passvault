# Changelog

This checkout has no verifiable release history. Entries below describe the current unreleased hardening work and
must not be interpreted as proof that version 1.0.0 was publicly shipped or independently audited.

## Unreleased

### Security

- Replaced ambiguous persisted ciphertext with versioned XChaCha20-Poly1305 envelopes and authenticated associated
  data.
- Added Argon2id metadata bounds, purpose-separated keyed-BLAKE2b subkeys, encrypted folder/tag/title blind indexes,
  cancellation safety, and best-effort key/buffer wiping.
- Removed the unused Desktop keyring prototype and replaced the incomplete mobile biometric prototype with Android
  auth-per-use Keystore wrapping and iOS device-only, enrollment-bound Keychain storage.
- Added clipboard ownership-aware expiration and Android screenshot protection.
- Added encrypted raw-database integration tests, wrong-key/tamper/nonce tests, and sensitive-error assertions.

### Data integrity

- Added a strict versioned `.pvault` format with independent backup password, preview validation, size/relationship
  limits, wrong-password/corruption handling, and transactional replacement restore.
- Made credential/tag/history updates transactional and synchronized the legacy folder cross-reference with the
  canonical credential folder column.
- Added parent/cycle/duplicate validation for folders and tags and additional repository precondition checks.
- Added persistent theme, lock, screenshot, and clipboard settings.

### UI and lifecycle

- Consolidated duplicate screens and state machines.
- Replaced the monolithic navigation shell with application-owned Navigation 3 state, typed routes, independent tab
  stacks, feature route adapters, bounded navigation commands, and one guarded Back policy across platforms.
- Added secure route quarantine/restoration and supported iOS interactive Back behavior with mirrored Arabic/RTL
  direction and an explicit 200 ms iOS transition base without private recognizer hooks.
- Reworked onboarding, unlock, vault, credential detail/editor, generator, health, settings, and backup surfaces with
  shared semantic tokens, secure fields, error/empty/loading states, and compact/expanded layouts.
- Added operation-job admission/cancellation, rapid-submit protection, and sensitive editor-state cleanup on lock.
- Added Android background lock wiring and Desktop window/lifecycle protections.

### Build and documentation

- Made the root `test` task execute the complete Desktop/JVM test graph.
- Added committed SHA-256 Gradle dependency verification metadata.
- Corrected Android flavor task paths, release-version propagation, Android signing gates, and unsigned Desktop
  artifact handling in CI.
- Removed unsupported cloud, CSV, attachment-file, certification, and support-contact claims; documented the
  implemented opt-in mobile biometric boundary without claiming physical-device assurance.

### Known release gates

- Android device/emulator UI and lifecycle verification.
- Desktop graphical smoke testing and publisher signing.
- Physical iOS/Android biometric and lifecycle verification.
- A previous-release schema fixture before database migration testing can exist.
- Independent security review and publisher-provided license/contact/distribution metadata.
