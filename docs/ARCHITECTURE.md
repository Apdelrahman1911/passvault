# Architecture

Last reviewed: 2026-07-28

PassVault is a local-only Kotlin Multiplatform application. Android and JVM Desktop are the application hosts.
Shared modules also declare iOS-compatible source sets where their dependencies support them, but no production
iOS host is shipped.

## Module map

| Module | Responsibility |
|---|---|
| `app-android` | Android lifecycle, `FLAG_SECURE`, clipboard, document picker, preferences, and app entry point |
| `app-desktop` | Compose window, keyboard/tray integration, clipboard, file dialog, preferences, and app entry point |
| `shared` | Koin composition, Room construction, theme ownership, session-to-navigation coordination, and the only live `NavDisplay` |
| `core:domain` | Immutable domain models, typed identifiers, validation, health logic, and repository/settings contracts |
| `core:crypto` | libsodium-backed Argon2id, XChaCha20-Poly1305, random data, constant-time comparison, and subkey derivation |
| `core:database` | Room schema/DAOs, encrypted repositories, vault session, and versioned backup service |
| `core:security` | Platform-neutral clipboard, screenshot/window, and keyring boundaries |
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
events. Platform work is exposed through common interfaces and bound by the Android/Desktop Koin module.

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

There is no cloud/account/network service, biometric unlock, plaintext/CSV export, attachment-file pipeline, or
production iOS host. Reserved attachment metadata remains in schema version 1 for format preservation only.

