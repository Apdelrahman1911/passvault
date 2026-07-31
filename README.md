# PassVault

PassVault is an offline-first password manager built with Kotlin Multiplatform and Compose Multiplatform.
Android and JVM Desktop are the current application targets. Shared source sets remain compatible with a future
iOS host, but this repository does not contain a production iOS application.

## Shipped capabilities

- Local vault creation, password unlock, manual lock, background/inactivity lock, and failed-attempt throttling.
- Authenticated XChaCha20-Poly1305 encryption with Argon2id-derived key-encryption keys and per-purpose subkeys.
- Credential creation, editing, deletion, favorites, folders, tags, search, filters, and password history.
- Password and passphrase generation plus local weak, reused, and old-password analysis.
- Versioned, password-protected `.pvault` backup creation, validation preview, and transactional replacement restore.
- Clipboard expiration with ownership checks, Android screenshot protection, persistent security/theme settings, and
  responsive Compose layouts for compact and expanded windows.

## Deliberate limitations

- PassVault has no cloud service or cross-device synchronization.
- Biometric vault unlock is not shipped. There is no biometric UI or platform adapter in the release graph; the
  master password is the supported unlock path.
- CSV/plaintext import and export are not shipped.
- Attachment file storage is not shipped. The current schema can preserve encrypted attachment metadata during a
  backup, but backup files never package attachment bytes.
- Database schema version 1 is the only released schema in this checkout; no upgrade migration can be claimed until
  a previous release fixture exists.
- Release signing, notarization, and store publication require credentials and infrastructure outside this
  repository.

The detailed, evidence-backed status is maintained in
[`docs/PRODUCTION_READINESS_AUDIT.md`](docs/PRODUCTION_READINESS_AUDIT.md).

## Project structure

```text
app-android/       Android application and platform services
app-desktop/       JVM Desktop application and platform services
shared/            shared app composition, navigation, and dependency injection
core/
  crypto/          cryptographic engine and key hierarchy
  database/        Room schema, DAOs, encrypted repositories, and backup service
  domain/          domain models, validation, and repository contracts
  security/        clipboard, screenshot, keyring, lifecycle, and settings boundaries
  designsystem/    themes, tokens, and reusable Compose controls
  navigation/      Navigation 3 route and command contracts
  testing/         shared deterministic fakes
feature/           onboarding, unlock, vault, credential, generator, health,
                   settings, and backup modules
```

## Build and verification

Use JDK 17 and the checked-in wrapper. The Android application has `standard` and `fdroid` product flavors.

```bash
# All shared Desktop/JVM unit and integration suites
./gradlew test

# Android debug APK
./gradlew :app-android:assembleStandardDebug

# Android unsigned/release verification
./gradlew :app-android:assembleStandardRelease

# Desktop compile and local run
./gradlew :app-desktop:compileKotlinDesktop
./gradlew :app-desktop:run

# Static analysis and dependency checksum policy
./gradlew detekt
./gradlew verifyDependencies
```

Platform package tasks are available through `./gradlew :app-desktop:tasks`. Run only the package task for the
current operating system.

## Security notes

Never commit real vaults, backup files, signing material, credentials, `local.properties`, or sensitive logs.
PassVault encrypts record payloads at the application layer; the Room database file itself is not SQLCipher.
Security changes require focused tests and review against
[`docs/SECURITY_MODEL.md`](docs/SECURITY_MODEL.md).

This checkout does not include a license file or an official support endpoint. Add verified project ownership,
licensing, support, signing, and disclosure details before public distribution.
