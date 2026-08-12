# PassVault

PassVault is an offline-first password manager built with Kotlin Multiplatform and Compose Multiplatform.
Android, iOS, and JVM Desktop are release targets. `iosApp/` is the SwiftUI/Xcode host that embeds, signs, and
ships the shared Compose application on iPhone and iPad.

## Shipped capabilities

- Local vault creation, password unlock, mobile biometric unlock, manual lock, background/inactivity lock, and
  failed-attempt throttling.
- Authenticated XChaCha20-Poly1305 encryption with Argon2id-derived key-encryption keys and per-purpose subkeys.
- Credential creation, editing, deletion, favorites, folders, tags, search, filters, and password history.
- Per-login TOTP authenticators with local code generation, QR enrollment, and encrypted setup-key storage.
- Password and passphrase generation plus local weak, reused, and old-password analysis.
- Independently encrypted, authenticated file attachments with controlled import, preview/export, rename, deletion,
  credential ownership binding, and automatic cleanup of private plaintext staging files.
- Streaming, versioned, password-protected `.pvault` backup creation, validation preview, attachment preservation, and
  transactional Room replacement.
- Clipboard expiration with ownership checks, Android screenshot protection, persistent theme/accent/auto-lock/
  clipboard settings, and responsive Compose layouts for compact and expanded windows.

## Deliberate limitations

- PassVault has no cloud service or cross-device synchronization.
- Biometric unlock is opt-in per vault on Android and iOS; Desktop and devices without an
  enrolled supported biometric continue to use the master password.
- Saved TOTP authenticators protect the external accounts they belong to; they do not add a second factor to
  PassVault vault unlock.
- CSV/plaintext import and export are not shipped.
- Managed attachments are limited to 100 MiB per file, 20 files and 512 MiB per credential. Format-2 backups stream
  metadata and attachment objects with a 16 GiB complete-container limit; legacy format-1 imports remain bounded and
  do not contain attachment bytes.
- The Room schema is version 3. Explicit, non-destructive migrations preserve versions 1 and 2; destructive fallback
  is not configured.
- Release signing, notarization, and store publication require credentials and infrastructure outside this
  repository.

The detailed, evidence-backed status is maintained in
[`docs/PRODUCTION_READINESS_AUDIT.md`](docs/PRODUCTION_READINESS_AUDIT.md).

## Project structure

```text
app-android/       Android application and platform services
app-desktop/       JVM Desktop application and platform services
iosApp/            SwiftUI/Xcode release host for the shared iOS framework
shared/            shared app composition, navigation, and dependency injection
core/
  crypto/          cryptographic engine and key hierarchy
  database/        Room schema, DAOs, encrypted repositories, and backup service
  domain/          domain models, validation, and repository contracts
  otp/             TOTP parsing and RFC 6238 code generation
  security/        clipboard, screenshot, biometric, and UI-security boundaries
  designsystem/    themes, tokens, and reusable Compose controls
  navigation/      Navigation 3 route and command contracts
  testing/         shared deterministic fakes
feature/           onboarding, unlock, vault, credential, generator, health,
                   settings, and backup modules
```

## Build and verification

Use JDK 17 and the checked-in wrapper. The Android application has `standard` and `fdroid` product flavors.

```bash
# All Desktop/JVM and Android host unit/integration suites
./gradlew test

# Android debug APK
./gradlew :app-android:assembleStandardDebug

# Android unsigned/release verification
./gradlew :app-android:assembleStandardRelease

# Desktop compile and local run
./gradlew :app-desktop:compileKotlinDesktop
./gradlew :app-desktop:run

# iOS simulator framework compile (requires macOS/Xcode)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Static analysis and dependency checksum policy
./gradlew detekt
./gradlew verifyDependencies
```

Platform package tasks are available through `./gradlew :app-desktop:tasks`. Run only the package task for the
current operating system.

To run iOS from Android Studio on macOS, enable the Kotlin Multiplatform plugin, sync the project with Gradle,
select the generated `PassVault` run configuration, and choose an iOS simulator. The configuration is derived from
`iosApp/iosApp.xcodeproj`; its Xcode build phase builds and embeds `PassVaultShared` automatically.

## Security notes

Never commit real vaults, backup files, signing material, credentials, `local.properties`, or sensitive logs.
PassVault encrypts record payloads at the application layer; the Room database file itself is not SQLCipher.
Security changes require focused tests and review against
[`docs/SECURITY_MODEL.md`](docs/SECURITY_MODEL.md).

The source is licensed under Apache License 2.0 in [`LICENSE.txt`](LICENSE.txt).
The bilingual privacy and support site sources are tracked under `site/`; release
configuration supplies the verified publisher and contact values at deployment.
Production artifact authorization and key-custody rules are published in the
[`PassVault code-signing policy`](docs/CODE_SIGNING_POLICY.md).
