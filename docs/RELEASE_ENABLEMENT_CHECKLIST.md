# Release Enablement Checklist

This checklist tracks the repository work required to turn a verified build into
a signed, publishable PassVault release. A checked item means the implementation
and its repository-level verification are complete. Publisher-owned credentials,
legal identity, and store-account actions remain external by design.

## Product identity and branding

- [x] Product name fixed as **PassVault**.
- [x] Canonical Android application ID fixed as `com.passvault.android`.
- [x] Google Play application ID fixed as `com.passvault.android`; the Release
  build type is the canonical artifact for every Play track.
- [x] Local Android Debug uses `com.passvault.android.debug`; F-Droid and testing
  flavors are retired.
- [x] Local iOS Debug uses `com.passvault.ios.debug`; TestFlight and App Store
  archives preserve `com.passvault.ios`.
- [x] Desktop bundle ID fixed as `com.passvault.desktop`.
- [x] Version source centralized in `version.properties`.
- [x] User-supplied icon artwork is present in the application resources.
- [x] Android adaptive, legacy, monochrome, and splash assets use the approved
  PassVault artwork.
- [ ] Windows ICO, macOS ICNS, and Linux PNG assets supplied by the user.
- [ ] Bilingual store feature graphics and screenshot set supplied under
  `release/store-assets/` and accepted by the production validator.
- [ ] User-supplied branding source and usage rights documented.

## Signing and packaging

- [x] Android signing values load from environment variables or local Gradle
  properties and signing material is ignored by source control.
- [x] Android signed artifacts are cryptographically verified in release CI
  against a pinned release-certificate SHA-256 fingerprint.
- [x] Windows Authenticode signing and verification are wired into release CI.
- [x] Windows stable upgrade identity is finalized; publisher identity is a
  validated external release input.
- [x] macOS signing/notarization inputs are validated before release packaging.
- [x] Linux maintainer and Apache-2.0 license metadata are supplied from release metadata.
- [x] Local signing setup and certificate rotation are documented.

## Store and legal material

- [x] User-approved Google Play/App Store bilingual descriptions are present in
  the ignored private intake and pass validation.
- [x] The bilingual privacy policy is public at the stable HTTPS Pages routes and
  matches the reviewed local-first behavior.
- [ ] Apache-2.0 project license and notice are included in packages.
- [x] Third-party acknowledgements cover direct production dependencies and
  the material transitive native/runtime projects shipped in Desktop packages.
- [ ] Security policy, backup guide, and recovery guide are publication-ready.
- [x] Publisher contact fields are fail-closed release inputs rather than
  fictional or hardcoded data.

## Automation

- [x] Signed Android release workflow builds and verifies APK/AAB artifacts.
- [x] Signed Windows workflow signs and verifies the launcher, EXE, and MSI.
- [x] macOS workflow imports a certificate, signs, notarizes, and validates DMG.
- [x] Linux packages are produced with release metadata.
- [x] Release artifacts and verified checksums are published through one
  tag-bound GitHub Release after all platform jobs succeed.
- [x] Release workflow rejects missing identity, contact, or signing inputs.
- [x] Protected internal, external-beta, and production mobile environments,
  keyless Google OIDC setup, Fastlane uploads, metadata rendering, and
  secret-name verification are implemented.
- [x] Production requires protected approval, exact confirmation, and validated
  public store assets; iOS production submits the exact tested build for review
  with automatic release after approval.

## iOS host and production integration

- [x] `PassVaultShared` is exported as a static iOS framework.
- [x] SwiftUI/Xcode host, Face ID Keychain adapter, clipboard/preferences, and
  native encrypted-backup Files picker are implemented.
- [x] iOS simulator compilation is included in CI.
- [x] Manual signing/archive/export and App Store Connect API upload are wired.
- [ ] Publisher certificate/profile/API key and Apple console declarations are supplied.

## Safe third-party import

- [ ] Import is additive and never replaces the current vault.
- [ ] CSV input is UTF-8, size, row, column, and field bounded.
- [ ] Bitwarden, LastPass, 1Password, and generic header mappings are supported.
- [ ] Unknown formats, invalid URLs, NUL bytes, and malformed CSV are rejected or
  surfaced as non-sensitive warnings.
- [ ] Parsed credentials are encrypted before one atomic database transaction.
- [ ] Import plans and source bytes are wiped when practical.
- [ ] The UI previews only counts/format/warnings and never sensitive values.
- [ ] Parser, wrong-format, boundary, and transactional import tests pass.

## Verification

- [x] Shared/Desktop and Android host tests pass.
- [x] Android Debug, screenshot, and Release variants compile with only the two approved application identities.
- [x] Android lint/R8 release verification passes.
- [ ] Desktop runtime smoke test and Windows EXE/MSI packaging pass.
- [x] iOS simulator compilation passes.
- [x] Release documentation and implementation-status records are updated.
- [ ] Detekt and the final aggregate verification pass.
