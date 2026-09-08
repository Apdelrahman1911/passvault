---
name: mobile-app-identities-and-services
description: Design, implement, inspect, or verify separate local Development and canonical Store identities for Android and iOS, including application names, build types/configurations, schemes, release workflows, Firebase, Google Services, APNs, OAuth, deep links, associated domains, entitlements, icons, splash screens, and launch assets. Use when local builds collide with Store apps or identifier-dependent services change.
---

# Mobile App Identities and Services

Keep local development installable beside the real Store application while every testing and production channel uses the canonical Store identity.

## Default model

```text
Local Debug/Development
  -> development name
  -> canonical identifier plus an explicit debug suffix

Store Release
  -> production name
  -> existing canonical identifier
  -> TestFlight / Play testing / production
```

Prefer ordinary Debug and Release configurations/build types. Add flavors or extra schemes only when a concrete independent dimension requires them.

## Inspection

### Android

Inspect Gradle build types/flavors, `applicationId`, suffixes, namespaces, resource values, manifest placeholders, merged manifests, signing configs, variant filters, IDE run configurations, screenshot/test variants, and CI task names.

### iOS

Inspect Xcode project settings, `.xcconfig` files, schemes and their Run/Archive actions, `PRODUCT_BUNDLE_IDENTIFIER`, `PRODUCT_NAME`, entitlements, provisioning, compiled `Info.plist`, extensions, and KMP framework configuration.

### Identifier-dependent services

Inventory Firebase/Crashlytics, Google Services, APNs, Keychain access groups, associated domains, URL schemes, OAuth callbacks, app groups, backend allowlists, and deep-link contracts. Determine whether local Debug actually needs each service before requesting another registration.

### Branding and launch assets

Inspect Android adaptive/legacy/monochrome icons, splash theme/foreground safe zone, and generated resources. Inspect iOS asset catalogs and native launch-screen configuration; verify the compiled bundle rather than only source previews.

## Remediation

- Preserve the existing Store IDs and signing registrations.
- Give local Debug a clearly different name and suffix-derived identifier.
- Make Android Studio's normal Run variant and Xcode's normal Run action use Development.
- Make Archive and every CI upload explicitly use Release.
- Do not introduce `.testing` identities for TestFlight or Play tracks.
- Provide separate service configuration only when needed; otherwise disable unsupported Debug integrations clearly.
- Keep screenshot automation isolated under the Development identity and never weaken production capture protection.

## Verification

- Build and inspect Android Debug and Release APK/AAB manifests and labels.
- Inspect effective Xcode Debug/Release build settings and compiled `Info.plist` values.
- Co-install Debug and Store builds on physical devices.
- Assert release workflows reject development identifiers before signing/upload.
- Test every enabled service callback under the correct identity.
- Verify extensions and Keychain/app groups remain compatible.
- Inspect splash/icon output on supported Android API levels and iPhone/iPad; test light/dark and masking.

## Failure lessons

- TestFlight and Play tracks are not environments.
- Pressing Run must not overwrite or impersonate the Store application.
- Changing only the bundle ID can silently break APNs, Keychain, OAuth, or Firebase.
- Android splash foreground geometry differs from the launcher icon.
- An iOS launch image with a baked background can appear as a box even when the app icon looks correct.
- Deleting the Store app to install local Debug indicates identity separation is incomplete.

## Resources

- Run [`scripts/report_mobile_identities.sh`](scripts/report_mobile_identities.sh) to collect likely identity declarations without modifying the project.
- Read [`references/identity-service-matrix.md`](references/identity-service-matrix.md) before changing IDs, schemes, entitlements, or service files.

