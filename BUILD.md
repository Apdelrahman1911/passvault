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
./gradlew :app-android:assembleStandardDebug
./gradlew :app-desktop:run
```

Android has `standard` and `fdroid` product flavors. Use an explicit flavor/build type in CI; a generic
`assembleDebug` can build more variants than intended.

## Release verification

```bash
./gradlew clean test detekt check verifyDependencies
./gradlew :app-android:assembleStandardRelease :app-android:bundleStandardRelease \
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
