# Repository Guidelines

## Project Structure & Module Organization

PassVault is a Kotlin Multiplatform project using Compose. `app-android/` and `app-desktop/` contain platform entry points; `shared/` assembles shared application code. Reusable layers live under `core/` (`domain`, `data`, `database`, `crypto`, `security`, `designsystem`, `navigation`, and `testing`), while user flows are separated under `feature/`. Modules follow Kotlin source sets such as `src/commonMain`, `src/androidMain`, `src/desktopMain`, and matching test directories. Android resources are in `app-android/src/main/res`; architecture and security references are in `docs/`.

## Build, Test, and Development Commands

Use JDK 17 and the checked-in Gradle wrapper (`.\gradlew.bat` on Windows; `./gradlew` elsewhere).

- `./gradlew :app-android:assembleDebug` builds the local PassVault Dev APK.
- `./gradlew :app-desktop:run` launches the desktop app locally.
- `./gradlew test` runs all unit tests.
- `./gradlew :core:crypto:test` tests one module.
- `./gradlew detekt` runs Kotlin static analysis.
- `./gradlew check` runs the broader verification suite.
- `./gradlew koverHtmlReport` generates coverage reports where configured.

Bash helpers in `scripts/` wrap Android, desktop, test, and release workflows.
Android has no product flavors: Debug uses `com.passvault.android.debug`, while Release and every Google Play track
use `com.passvault.android`. The Xcode `PassVault` scheme similarly runs Debug as `com.passvault.ios.debug` and
archives Release as `com.passvault.ios` for TestFlight/App Store.

## Coding Style & Naming Conventions

Follow `.editorconfig`: four-space indentation for Kotlin/KTS, LF endings, trailing whitespace removal, and a 120-character limit (100 in UI code). Trailing commas are encouraged. Use `PascalCase` for types and composables, `camelCase` for functions and properties, and lowercase package names. Run Detekt before submitting; do not edit generated files under `build/`.

## Testing Guidelines

Use `kotlin.test`; coroutine tests use `kotlinx-coroutines-test`, with Turbine and AssertK available. Name files `*Test.kt` and prefer descriptive backtick test names, for example ``fun `unlock rejects invalid password`()``. Put shared tests in `commonTest` and platform-specific tests in their corresponding source set. Documented minimum coverage is 90% domain, 85% data, 95% crypto, 85% ViewModels, and 60% UI.

## Commit & Pull Request Guidelines

This checkout has no Git history to inspect. Follow the README’s short, imperative, sentence-case style, such as `Add vault import validation`, and use branches like `feature/import-validation`. Pull requests should explain behavior and security impact, link relevant issues, list verification commands, and include screenshots for UI changes. Keep changes focused and update affected documentation.

## Security & Configuration

Never commit `local.properties`, signing files, credentials, vault exports, or real secrets. Avoid logging sensitive values; use the project’s redacted value types. Crypto, storage, and authentication changes require focused tests and review against `docs/SECURITY_MODEL.md`.
