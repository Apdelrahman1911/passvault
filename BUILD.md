# Build and release verification

Last reviewed: 2026-08-11

## Prerequisites

- JDK 17
- checked-in Gradle wrapper
- Android SDK for Android builds
- platform packaging tools for Desktop installers
- publisher-controlled signing/notarization credentials for distributable releases

Do not commit `local.properties`, keystores, certificates, signing passwords, real vaults, or `.pvault` exports.

## Development

```bash
./gradlew test
./gradlew detekt verifyDependencies
./gradlew :app-android:assembleDebug
./gradlew :app-desktop:run
```

Android has no product flavors. `debug` is the local PassVault Dev application
(`com.passvault.android.debug`); `release` is the Store application (`com.passvault.android`). Android Studio's
`app-android` Debug variant is therefore the safe default for Run. F-Droid packaging is retired.

The shared Xcode `PassVault` scheme uses Debug for Run/Test/Analyze and Release for Profile/Archive. Debug installs
PassVault Dev (`com.passvault.ios.debug`); Release preserves the App Store/TestFlight identity
(`com.passvault.ios`).

The CI-only `storeScreenshot` APK shares the Debug application ID and must be built separately with
`./gradlew :app-android:assembleStoreScreenshot`. Do not combine screenshot packaging, every host/iOS test, and the
optimized Release APK/AAB into one highly parallel Gradle invocation; the workflows intentionally isolate those
memory-heavy stages and cap workers.

## Release verification

```bash
./gradlew clean test detekt check verifyDependencies
./gradlew :app-android:assembleRelease :app-android:bundleRelease \
  -Ppassvault.versionName=1.2.3 -Ppassvault.versionCode=1002003
./gradlew :app-desktop:packageReleaseDistributionForCurrentOS -Ppassvault.versionName=1.2.3
```

`passvault.versionName` must be semantic `major.minor.patch`; a new mobile upload build number must be above the
canonical floor in `version.properties` and within Android's supported range. The Android release workflow fails if
signing secrets are absent. Desktop packages produced by CI are explicitly unsigned verification artifacts until
publisher signing/notarization is configured.

Run the Desktop package task only on its target OS. macOS notarization and iOS validation require a macOS runner.
Release readiness also requires an actual license, verified owner/support/security contact, store metadata, and an
independent security review; the repository cannot invent these.

Current command evidence and unresolved external gates are recorded in
[`docs/PRODUCTION_READINESS_AUDIT.md`](docs/PRODUCTION_READINESS_AUDIT.md).

## Dependency verification

`verifyDependencies` enforces the repository's reviewed checksum-only trust boundary; it is not a publisher-identity
claim. Review [`docs/DEPENDENCY_VERIFICATION.md`](docs/DEPENDENCY_VERIFICATION.md) before adding or changing a
checksum, repository, trusted-artifact rule, or signature policy. Never regenerate verification metadata merely to
make an unexplained dependency-verification failure pass.
