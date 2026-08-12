# Internal contracts

Last reviewed: 2026-08-11

PassVault has no network or public HTTP API. This document identifies the stable internal boundaries; source code is
authoritative for signatures.

## Domain repositories

- `VaultRepository`: vault existence, creation, password unlock/change, lock, state, and metadata.
- `CredentialRepository`: credential observation/query, create/update/delete, favorites, folders/tags, and history.
- `FolderRepository`: observe/create/update/delete folders with hierarchy, duplicate, and cycle validation.
- `TagRepository`: observe/create/update/delete tags and maintain credential relationships.
- `AppSettingsStore`: persisted theme, accent color, auto-lock timeout, and clipboard-expiry preferences.

Repositories return `Result` or flows of domain models. UI code never receives Room entities. An encrypted repository
operation fails safely when the vault session is locked.

## Security and platform boundaries

- `CryptoEngine`: random bytes, Argon2id, XChaCha20-Poly1305, keyed subkey derivation, and constant-time comparison.
- `VaultSessionManager`: a narrow active-key access boundary implemented by the vault repository.
- `ClipboardService`: sensitive copy with ownership-aware expiration.
- `ScreenshotProtection`: Android sensitive-window display control.
- `DesktopWindowProtection`: Desktop minimize/focus locking and native-window concealment owned by the Desktop host;
  it does not claim portable screenshot prevention.
- `BiometricKeyStore`: platform storage for a VEK gated by an OS biometric-only key policy.
- `BiometricUnlockService`: enrollment, status, removal, and verified session unlock for the current vault.
- `BackupFileStore`: bounded platform document/file selection and read/write.
- `TotpService`: strict Base32/`otpauth://totp` enrollment parsing and time-based code generation.

Biometric unlock never accepts a Boolean prompt result as proof of vault access. Android releases the VEK only
through an auth-per-use Keystore cipher; iOS releases a device-only Keychain item protected by the current biometric
set. `VaultRepositoryImpl` then authenticates the vault verification record before publishing the session.

## Presentation contracts

Each feature exposes immutable state, a sealed event set, and one-shot effects. `shared/PassVaultApp.kt` translates
effects to the single Navigation 3 back stack. Platform navigation controllers and database/crypto objects are not
passed into feature Composables.

## Compatibility rules

- Crypto and backup envelopes are explicitly versioned; unknown versions fail closed.
- New database schemas require exported-schema review and migrations from every supported released version.
- Domain serialization used inside encrypted records must remain backward-readable before a format version changes.
- Internal exception text, SQL, paths, ciphertext, and passwords must not cross into user-facing messages.
