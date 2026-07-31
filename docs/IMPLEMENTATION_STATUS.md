# PassVault implementation status

Last reviewed: 2026-07-29

The authoritative issue-by-issue status and command evidence is
[`PRODUCTION_READINESS_AUDIT.md`](PRODUCTION_READINESS_AUDIT.md). This file is a concise capability summary and
must not be used as proof that a release gate passed.

## Implemented

- Android and JVM Desktop application hosts.
- Shared Compose navigation, onboarding, vault unlock, vault list, credential details/editor, password generator,
  password health, settings, and encrypted backup/restore surfaces.
- Room KMP schema and encrypted vault, credential, folder, tag, relationship, history, and metadata repositories.
- Argon2id master/backup password derivation; XChaCha20-Poly1305 authenticated encryption; random vault encryption
  key; purpose-separated record keys; versioned crypto envelopes.
- Manual, background, and inactivity locking; failed-unlock throttling; settings persistence.
- Clipboard expiry with replacement ownership checks and Android screenshot blocking.
- Password-protected `.pvault` creation, validation preview, and transactional replacement restore.
- Responsive design tokens/components and compact/expanded layouts for the primary workflows.
- Centralized Compose resources for every reviewed user-facing surface, including Desktop native chrome; typed
  `UiText` keeps resource identifiers and format arguments unresolved until the active locale is available.
- Shared Desktop/JVM and Android host aggregate tests, real-libsodium tests, Room repository boundary tests, backup
  tests, and ViewModel regression tests.
- SHA-256 Gradle dependency verification metadata.

## Intentionally unavailable

- Biometric vault unlock. No biometric UI, dependency, or platform adapter is part of the release graph.
- Cloud synchronization, accounts, analytics, network breach lookup, and automatic cloud backup.
- CSV or any plaintext credential import/export.
- Attachment-file creation, viewing, or packaging. The version-1 schema can preserve legacy encrypted attachment
  metadata, but there is no attachment feature module or file pipeline.
- A production iOS app.

## Verified and remaining release evidence

- Non-Detekt checks, 609 tests, dependency verification, Android release/R8, and current-host Windows EXE/MSI
  packaging pass after the final code edits.
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
- iOS runtime and Xcode application validation require macOS/Xcode; compile-only simulator verification passes.
- There is no previous released schema fixture, so a database upgrade migration cannot be implemented or tested yet.
- Distribution ownership, license, support/security-contact details, Android signing, and Desktop
  signing/notarization must be supplied by the publisher.

No document in this repository should claim those external or unverified items are complete.
