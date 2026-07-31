# Release Enablement Checklist

This checklist tracks the repository work required to turn a verified build into
a signed, publishable PassVault release. A checked item means the implementation
and its repository-level verification are complete. Publisher-owned credentials,
legal identity, and store-account actions remain external by design.

## Product identity and branding

- [x] Product name fixed as **PassVault**.
- [x] Canonical Android application ID fixed as `com.passvault.android`.
- [x] Google Play application ID fixed as `com.passvault.android.play`.
- [x] F-Droid application ID fixed as `com.passvault.android.fdroid`.
- [x] Desktop bundle ID fixed as `com.passvault.desktop`.
- [x] Version source centralized in `version.properties`.
- [ ] User-supplied icon master added in a future branding pass.
- [ ] Android adaptive, legacy, monochrome, and splash assets replaced with
  user-supplied artwork in a future branding pass.
- [ ] Windows ICO, macOS ICNS, and Linux PNG assets supplied by the user.
- [ ] Store feature graphic and screenshot set supplied by the user.
- [ ] User-supplied branding source and usage rights documented.

## Signing and packaging

- [x] Android signing values load from environment variables or local Gradle
  properties and signing material is ignored by source control.
- [x] Android signed artifacts are cryptographically verified in release CI
  against a pinned release-certificate SHA-256 fingerprint.
- [x] Windows Authenticode signing and verification are wired into release CI.
- [x] Windows stable upgrade identity is finalized; publisher identity is a
  validated external release input.
- [ ] macOS signing/notarization inputs are validated before release packaging.
- [ ] Linux maintainer and license metadata are supplied from release metadata.
- [ ] Local signing setup and certificate rotation are documented.

## Store and legal material

- [ ] Google Play short and full descriptions are ready.
- [ ] F-Droid metadata is ready.
- [ ] Privacy policy is publication-ready and references the shipped behavior.
- [ ] Apache-2.0 project license and notice are included in packages.
- [ ] Third-party acknowledgements cover direct production dependencies.
- [ ] Security policy, backup guide, and recovery guide are publication-ready.
- [ ] Publisher contact fields are fail-closed release inputs rather than
  fictional or hardcoded data.

## Automation

- [x] Signed Android release workflow builds and verifies APK/AAB artifacts.
- [x] Signed Windows workflow signs and verifies the launcher, EXE, and MSI.
- [ ] macOS workflow imports a certificate, signs, notarizes, and validates DMG.
- [ ] Linux packages are produced with release metadata.
- [ ] Release artifacts and checksums are attached to a draft release.
- [ ] Release workflow rejects missing identity, contact, or signing inputs.

## Future iOS integration

- [ ] `PassVaultShared` is exported as a static iOS framework.
- [ ] A minimal SwiftUI/Xcode host scaffold documents application startup.
- [ ] iOS platform dependency boundaries and remaining adapters are documented.
- [ ] iOS simulator framework compilation is included in verification.

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

- [ ] Shared/Desktop and Android host tests pass.
- [ ] Android debug and release variants compile.
- [ ] Android lint/R8 release verification passes.
- [ ] Desktop runtime smoke test and Windows EXE/MSI packaging pass.
- [ ] iOS simulator compilation passes.
- [ ] Release documentation and implementation-status records are updated.
- [x] Detekt is excluded from this work at the user's explicit request.
