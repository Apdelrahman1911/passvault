# Testing

Last reviewed: 2026-08-01

Use JDK 17 and the checked-in Gradle wrapper. `gradlew test` is a repository-owned aggregate task that depends on all
Desktop/JVM and Android host-test tasks; it must not be replaced by Gradle's ambiguous unqualified selector.

## Main commands

```bash
./gradlew test
./gradlew detekt
./gradlew check
./gradlew check -x detekt
./gradlew verifyDependencies
./gradlew :app-android:assembleStandardDebug
./gradlew :app-android:verifyStandardDebugComposeResources
./gradlew :app-android:assembleStandardRelease
./gradlew :app-android:lintStandardRelease
./gradlew :app-desktop:compileKotlinDesktop
./gradlew :app-desktop:createReleaseDistributable
./gradlew :app-desktop:packageReleaseDistributionForCurrentOS
./gradlew :core:designsystem:compileKotlinIosSimulatorArm64 :shared:compileKotlinIosSimulatorArm64
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
./gradlew help --warning-mode all
```

On macOS, Android Studio with the Kotlin Multiplatform plugin discovers the `PassVault` Xcode scheme after Gradle
sync. Select that run configuration and an iOS simulator. The same host can be checked directly with Xcode:

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme PassVault \
  -configuration Debug -destination 'generic/platform=iOS Simulator' build
```

On Windows, verify the packaged runtime after creating the release image:

```powershell
.\scripts\smoke-test-desktop-release.ps1 -TimeoutSeconds 30
```

Run focused suites while editing, for example:

```bash
./gradlew :core:crypto:desktopTest
./gradlew :core:database:desktopTest
./gradlew :feature:unlock:desktopTest
```

## Coverage intent

- Crypto: round trip, wrong key, tamper, nonce uniqueness, KDF bounds, domain separation, cancellation, and sizes.
- Database/repositories: real Room plus real crypto, raw-row confidentiality, locked access, relationships,
  transactions, counts, and corrupt rows.
- Backup: round trip, wrong password, tamper/truncation/version/limits, referential integrity, preview, and rollback.
- Presentation: validation, rapid submit, cancellation, errors, lock cleanup, filters/sort, and settings persistence.
- Platform: clipboard ownership, lifecycle lock, screenshot flag, file pickers, focus/IME, and graphical behavior.

Shared fakes must deep-copy sensitive arrays/models and represent failure/lock behavior. Tests should assert domain
behavior rather than private implementation detail.

## Evidence limits

The aggregate currently contains 609 tests: 310 Desktop/JVM and 299 Android host, with no failures, errors, or
skips in the final run.

Android host tests exercise portable and Android-compilation behavior on the JVM, but do not prove lifecycle,
IME, screenshot, file-picker, accessibility, or native device integration. Those need an emulator/device. The iOS
framework and SwiftUI host build on macOS/Xcode, but interactive workflows and the missing backup document picker
still need device/simulator validation. A Desktop release is not startup-verified by `:app-desktop:run`: build
`:app-desktop:createReleaseDistributable` and run the packaged-release smoke script. The release workflow uses this
guard before uploading Windows artifacts. The final Windows image remained running for 30 seconds, while
visual/focus/file-dialog behavior still needs an interactive human graphical session.

Android `check` depends on `:app-android:verifyStandardDebugComposeResources`. This opens the generated APK and
fails if the shared design-system Compose string bundle was not merged into Android assets.

These limits must remain explicit in the audit ledger.

Do not disable tests, add `|| true`, accept flaky retries as success, or use a static-analysis baseline to hide new
findings. Never place real credentials or backups in fixtures or test output.
