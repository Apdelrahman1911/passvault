# Build status

Last updated: 2026-07-29

This is a command summary. The persistent audit and remaining release gates are in
[`docs/PRODUCTION_READINESS_AUDIT.md`](docs/PRODUCTION_READINESS_AUDIT.md).

## Verified in this checkout

| Command | Result |
|---|---|
| `.\gradlew.bat test` | Passed; 310 Desktop/JVM plus 299 Android host tests, 0 failures/errors/skips |
| `.\gradlew.bat :core:database:desktopTest` | Passed, including encrypted repository and backup integration tests |
| `.\gradlew.bat :feature:credential:desktopTest` | Passed, including rapid-submit and lock-during-save regressions |
| `.\gradlew.bat :app-desktop:compileKotlinDesktop` | Passed |
| `.\scripts\smoke-test-desktop-release.ps1 -TimeoutSeconds 30` | Passed; the packaged release remained alive for 30 seconds and was closed with no leftover process |
| `.\gradlew.bat :app-android:assembleStandardDebug` | Passed; Standard debug APK produced |
| `.\gradlew.bat :app-android:verifyStandardDebugComposeResources --warning-mode all` | Passed; required shared Compose strings bundle is present in the APK |
| `.\gradlew.bat :app-android:installStandardDebug` plus clean ADB launch/Logcat monitor | Passed on `AQM-LX1` Android 10; app remained foregrounded with no fatal/resource/navigation exceptions |
| `.\gradlew.bat check -x detekt` | Passed, including Android lint |
| `.\gradlew.bat :app-android:lintStandardRelease` | Passed; one intentional `UseKtx` productivity warning |
| `.\gradlew.bat :app-android:assembleStandardRelease` | Passed with R8/resource shrinking; unsigned release APK produced |
| `.\gradlew.bat :app-desktop:packageReleaseDistributionForCurrentOS` | Passed; 116,308,992-byte EXE and 115,605,023-byte MSI for version 1.0.1 |
| `.\gradlew.bat :core:designsystem:compileKotlinIosSimulatorArm64 :shared:compileKotlinIosSimulatorArm64` | Passed on Windows as compile-only future-iOS verification |
| `.\gradlew.bat help --warning-mode all` | Passed; deprecated Gradle 10 source-set DSL and deprecated Instant aliases have been removed |
| Two consecutive `.\gradlew.bat verifyDependencies` runs | Passed against SHA-256 dependency metadata; configuration cache stored and then reused |
| Combined tests, non-Detekt checks, dependency verification, Android release/lint, and Desktop package command | Passed after the Desktop launcher repair |

Compose Desktop ProGuard is intentionally disabled for release images because its transformation broke reflection,
JNI registration, JNA callbacks, and Navigation 3 bytecode in the packaged runtime. The release image still uses
jlink/jpackage; Android R8 and resource shrinking remain enabled. The other build notices are non-fatal: iOS
simulator runtime tests require macOS, and Android lint has one intentional productivity warning for a checked
`SharedPreferences.commit()` call whose Boolean failure result would be discarded by the suggested KTX helper.

## Not accepted as passed

- `detekt` was explicitly skipped by the user. Its existing report contains five findings, recorded in the audit
  ledger; no baseline or suppression was added.
- Android release signing and signature verification require publisher-owned credentials.
- The actual Windows Desktop release image startup smoke passed; detailed
  resize/focus/menu/tray/keyring/file-dialog interaction still needs a human graphical matrix. Other target packages
  require their operating systems and signing/notarization credentials.
- The physical Android 10 install/cold-start smoke passes. The remaining device matrix covers lifecycle, process
  recreation, IME, rotation, file picker, screenshot/clipboard behavior, and UI/accessibility interaction.
- iOS runtime verification, which requires macOS/Xcode.

Do not disable crypto or remove module dependencies to make a build pass. File-lock interruptions on Windows are
runner failures and are not recorded as successful builds.
