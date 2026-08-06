# PassVault implementation status

Last reviewed: 2026-08-05

The authoritative issue-by-issue status and command evidence is
[`PRODUCTION_READINESS_AUDIT.md`](PRODUCTION_READINESS_AUDIT.md). This file is a concise capability summary and
must not be used as proof that a release gate passed.

## Implemented

- Android and JVM Desktop application hosts.
- A SwiftUI/Xcode iOS host that embeds the static `PassVaultShared` framework, supports native Files backup
  import/export, and can be manually signed and archived by protected CI.
- Shared Compose navigation, onboarding, vault unlock, vault list, credential details/editor, password generator,
  password health, settings, and encrypted backup/restore surfaces.
- Room KMP schema and encrypted vault, credential, folder, tag, relationship, history, and metadata repositories.
- Argon2id master/backup password derivation; XChaCha20-Poly1305 authenticated encryption; random vault encryption
  key; purpose-separated record keys; versioned crypto envelopes.
- Manual, background, and inactivity locking; failed-unlock throttling; settings persistence.
- Opt-in Android strong-biometric and iOS Face ID/Touch ID vault unlock backed by Keystore/Keychain and verified VEK
  session publication; master-password fallback remains available.
- Clipboard expiry with replacement ownership checks and Android screenshot blocking.
- Password-protected `.pvault` creation, validation preview, and transactional replacement restore.
- Per-login TOTP storage and code generation, manual/URI enrollment, Android/iOS camera QR scanning, and Desktop QR
  image import.
- Responsive design tokens/components and compact/expanded layouts for the primary workflows.
- Centralized Compose resources for every reviewed user-facing surface, including Desktop native chrome; typed
  `UiText` keeps resource identifiers and format arguments unresolved until the active locale is available.
- Shared Desktop/JVM and Android host aggregate tests, real-libsodium tests, Room repository boundary tests, backup
  tests, and ViewModel regression tests.
- SHA-256 Gradle dependency verification metadata.

## Intentionally unavailable

- Desktop biometric vault unlock.
- Cloud synchronization, accounts, analytics, network breach lookup, and automatic cloud backup.
- CSV or any plaintext credential import/export.
- Attachment-file creation, viewing, or packaging. The version-1 schema can preserve legacy encrypted attachment
  metadata, but there is no attachment feature module or file pipeline.
- Store publication without publisher-supplied identity, signing inputs, policy decisions, graphics, and approval.

## Verified and remaining release evidence

- The TOTP and biometric state/repository tests pass on Desktop/JVM; Android biometric code and the iOS simulator
  Keychain target compile. Actual system prompts and enrollment invalidation still require physical-device tests.
- The aggregate `test` task can still exceed the pre-existing `CryptoBenchmarkTest` one-millisecond random-generation
  threshold while suites run concurrently; the same benchmark passes in isolation. Treat the aggregate test gate as
  open until that performance test is made load-independent.
- A physical Android 10 Standard debug install and cold-start Logcat smoke passes. The Android KMP resource
  configuration now packages the shared design-system bundle, and Android `check` asserts the bundle exists.
- Shared Back navigation uses an older-Android-compatible list removal operation; the reported credential-editor
  Back crash signature did not recur during the post-fix device monitor.
- The actual packaged Windows Desktop 1.0.1 release launcher passes a controlled startup smoke, and
  shared/design-system iOS simulator compilation passes on Windows.
- Desktop release ProGuard is disabled because it corrupts runtime reflection/JNI/JNA/Navigation contracts in this
  dependency graph; jlink/jpackage remain enabled and Android release shrinking remains enabled through R8.
- Deprecated Gradle 10 Kotlin DSL source-set delegates and `kotlinx.datetime.Instant` aliases have been removed.
- Detekt was skipped at the user's request. Five Android-adapter maintainability findings remain recorded in the
  production-readiness ledger.
- The remaining Android device matrix covers lifecycle, screenshot flag, Storage Access Framework, IME, rotation,
  process recreation, and accessibility behavior beyond the completed install/cold-start smoke.
- Desktop interactive resize, keyboard, tray, clipboard, keyring, accessibility, and file-dialog behavior still
  requires a human graphical matrix beyond the successful startup smoke.
- The iOS simulator framework and SwiftUI host compile, and the native backup picker compiles. Interactive iPhone
  validation plus a signed archive/upload run remain outstanding.
- There is no previous released schema fixture, so a database upgrade migration cannot be implemented or tested yet.
- Distribution ownership, license, support/security-contact details, Android signing, and Desktop
  signing/notarization must be supplied by the publisher.

No document in this repository should claim those external or unverified items are complete.
