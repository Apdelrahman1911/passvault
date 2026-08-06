# Architecture

Last reviewed: 2026-08-05

PassVault is a local-only Kotlin Multiplatform application with Android, JVM Desktop, and SwiftUI-hosted iOS targets.
The iOS target embeds the shared Compose framework and has protected store archive/upload automation; publisher
credentials and physical-device release evidence remain external gates.

## Module map

| Module | Responsibility |
|---|---|
| `app-android` | Android lifecycle, `FLAG_SECURE`, biometric Keystore/prompt, clipboard, document picker, preferences, and app entry point |
| `app-desktop` | Compose window, keyboard/tray integration, clipboard, file dialog, preferences, and app entry point |
| `iosApp` | SwiftUI/Xcode host that builds, signs, and embeds the static `PassVaultShared` framework |
| `shared` | Koin composition, Room construction, theme ownership, session-to-navigation coordination, and the only live `NavDisplay` |
| `core:domain` | Immutable domain models, typed identifiers, validation, health logic, and repository/settings contracts |
| `core:crypto` | libsodium-backed Argon2id, XChaCha20-Poly1305, random data, constant-time comparison, and subkey derivation |
| `core:otp` | Strict TOTP setup parsing and RFC 6238 code generation for SHA-1, SHA-256, and SHA-512 |
| `core:database` | Room schema/DAOs, encrypted repositories, vault session, and versioned backup service |
| `core:security` | Platform-neutral biometric, clipboard, screenshot/window, and keyring boundaries |
| `core:designsystem` | Semantic theme tokens and reusable responsive/feedback/form controls |
| `core:navigation` | Serializable route keys and app-command dispatcher |
| `core:testing` | Deterministic fakes and shared test data only |
| `feature:*` | Onboarding, unlock, vault, credential, generator, health, settings, and backup UI/state |

`core:data` contains cross-platform repository contract tests. Production implementations live in `core:database`
because their responsibilities include both Room transactions and the application encryption boundary.

## Dependency and data flow

```text
platform app
    -> shared app composition
        -> feature ViewModel/state
            -> core:domain repository contract
                -> core:database encrypted repository
                    -> Room DAO
                -> core:crypto + active vault session
```

Features do not receive Room entities or encryption primitives. Composables receive explicit state and emit feature
events. Platform work is exposed through common interfaces and bound by platform Koin modules. The iOS host
implements settings, clipboard, biometrics, QR scanning, and bounded encrypted-backup import/export through
`UIDocumentPickerViewController`.

The credential feature owns QR enrollment adapters: Android and iOS scan live camera metadata, while Desktop reads
a bounded user-selected image. Every payload is passed through the same strict `core:otp` parser before it can enter
credential state. TOTP generation uses device time and does not call a network service.

The active vault session is owned by `VaultRepositoryImpl`, which implements both `VaultRepository` and the narrow
`VaultSessionManager` used by encrypted repositories. A repository operation must obtain a session-key copy and
fails while locked. Locking serializes the transition and wipes the repository-owned key buffer best-effort.

## State and concurrency

Feature state uses `StateFlow`; one-shot navigation and feedback use `SharedFlow`. Mutating operations have admission
guards or jobs to prevent rapid duplicate submission. Cancellation remains cancellation instead of being mapped to
a generic failure. Database, file, and cryptographic operations execute from coroutine-backed state handlers rather
than directly in Composables.

ViewModels are application-scoped Koin singletons because the shared host coordinates state between multiple routes.
Sensitive feature state is explicitly cleared on lock/route exit. Changing this lifetime requires route-scoped
handoff design, not merely replacing `single` with `factory`.

Biometric enrollment is an explicit unlocked-vault action. `DefaultBiometricUnlockService` copies the active VEK to
the platform `BiometricKeyStore`; Android encrypts it with an auth-per-use Keystore key, while iOS stores it as a
device-only Keychain item bound to the current biometric set. On biometric unlock, the repository authenticates its
encrypted verification record with the released VEK before creating a session. The master password remains the
fallback and is never stored for biometric use.

## Persistence boundary

Room schema version is 1 and schema export is enabled. Record payloads are encrypted before DAO writes; the database
file itself is not SQLCipher. Identifiers, record types, timestamps, favorites, relationships, and selected visual or
attachment metadata remain structural plaintext. See [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) and
[SECURITY_MODEL.md](SECURITY_MODEL.md).

## Design system and responsive UI

`core:designsystem` owns semantic light/dark colors, type, spacing, shapes, elevation, motion, responsive breakpoints,
feedback states, secure text input, and navigation scaffolds. Feature layouts choose compact or expanded composition
and constrain readable/form widths instead of stretching a phone layout across a desktop window.

## Deliberate non-features

There is no cloud/account/network service, Desktop biometric unlock, plaintext/CSV export, attachment-file pipeline,
or production iOS host. Reserved attachment metadata remains in schema version 1 for format preservation only.
