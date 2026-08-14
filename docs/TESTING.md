# Testing

Last reviewed: 2026-08-14

Use JDK 17 and the checked-in Gradle wrapper. `gradlew test` is a repository-owned aggregate task that depends on all
Desktop/JVM and Android host-test tasks; it must not be replaced by Gradle's ambiguous unqualified selector.

## Main commands

```bash
./gradlew test
./gradlew detekt
./gradlew check
./gradlew check -x detekt
./gradlew verifyDependencies
./gradlew :app-android:assembleDebug
./gradlew :app-android:verifyDebugComposeResources
./gradlew :app-android:verifyAndroidApplicationIdentities
./gradlew :app-android:assembleRelease
./gradlew :app-android:lintRelease
./gradlew :app-desktop:compileKotlinDesktop
./gradlew :app-desktop:stageDesktopBiometricBridge :app-desktop:desktopTest
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
./scripts/validate-ios-build-identities.sh
```

On Windows, verify the packaged runtime after creating the release image:

```powershell
.\scripts\smoke-test-desktop-release.ps1 -TimeoutSeconds 30
```

During the protected production-signing flow only, bind the signed bridge bytes before packaging installers:

```powershell
.\scripts\update-desktop-biometric-checksum.ps1 `
  -RuntimePath app-desktop/build/compose/binaries/main-release/app
```

Run focused suites while editing, for example:

```bash
./gradlew :core:crypto:desktopTest
./gradlew :core:database:desktopTest
./gradlew :feature:unlock:desktopTest
./gradlew :app-desktop:testDesktopBiometricBridge
```

## Coverage intent

- Crypto: round trip, wrong key, tamper, nonce uniqueness, KDF bounds, domain separation, cancellation, and sizes.
- Database/repositories: real Room plus real crypto, raw-row confidentiality, locked access, relationships,
  transactions, counts, and corrupt rows.
- Backup: round trip, wrong password, tamper/truncation/version/limits, referential integrity, preview, and rollback.
- Presentation: validation, rapid submit, cancellation, errors, lock cleanup, filters/sort, and settings persistence.
- Platform: clipboard ownership, lifecycle lock, screenshot flag, file pickers, focus/IME, graphical behavior, native
  biometric ABI/integrity loading, macOS metadata/path/error invariants, Windows envelope crypto/tamper,
  credential-identity and temporary-inventory invariants, prompt serialization, cancellation, and focus-lock
  coordination.

Shared fakes must deep-copy sensitive arrays/models and represent failure/lock behavior. Tests should assert domain
behavior rather than private implementation detail.

## Evidence limits

The repository-owned `test` aggregate currently contains 1,277 tests: 702 Desktop/JVM and 575 Android tests (554
shared Android-host tests plus 21 application tests). The complete clean `check` result set adds 554 iOS simulator
tests, for 1,831 tests total with no failures, errors, or skips.

Android host tests exercise portable and Android-compilation behavior on the JVM, but do not prove lifecycle,
IME, screenshot, file-picker, accessibility, or native device integration. Those need an emulator/device. The iOS
framework and SwiftUI host build on macOS/Xcode, but interactive backup/attachment pickers, viewers/share sheets,
biometrics, lifecycle behavior, and layouts still need device/simulator validation. A Desktop release is not
startup-verified by `:app-desktop:run`: build
`:app-desktop:createReleaseDistributable` and run the packaged-release smoke script. The release workflow uses this
guard before uploading Windows artifacts. The final Windows image remained running for 30 seconds, while
visual/focus/file-dialog behavior still needs an interactive human graphical session.

Touch ID and Windows Hello cannot be security-validated by an unauthenticated CI runner. Follow the physical matrix
in `DESKTOP_BIOMETRIC_UNLOCK.md` on installed signed packages. At minimum validate enable, restart unlock,
cancellation, lockout/recovery, invalidation/reset, disable/re-enable, password change, restore, focus loss, minimize,
shutdown during a prompt, and update/install behavior. Confirm the candidate key is rejected after protected platform
material is removed or replaced.

Android `check` depends on `:app-android:verifyDebugComposeResources` and application-identity validation. The
package check opens the generated APK and
fails if the shared design-system Compose string bundle was not merged into Android assets.

These limits must remain explicit in the audit ledger.

Do not disable tests, add `|| true`, accept flaky retries as success, or use a static-analysis baseline to hide new
findings. Never place real credentials or backups in fixtures or test output.
