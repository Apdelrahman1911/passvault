---
name: native-release-signing-and-packaging
description: Design, implement, audit, or verify production signing and packaging for Android, iOS, macOS, and Windows, including nested native code, certificates, profiles, hardened runtime, notarization, stapling, Gatekeeper, Authenticode timestamps, architecture coverage, secret scoping, and validation-only release gates. Use before publishing native artifacts or whenever signing, packaging, certificates, installers, or native components change.
---

# Native Release Signing and Packaging

Prove the identity and integrity of the final installable artifact, not merely one nested binary or one build step.

Load `$release-provenance-and-promotion` when signed output must remain bound to a reviewed candidate or later promotion/publication.

## Security rules

1. Never invent, commit, print, or attach private credentials.
2. Validate public identity, private-key pairing, purpose/EKU, expiry, and expected fingerprint before signing.
3. Sign only reviewed publisher-owned files. Preserve valid vendor signatures.
4. Verify independently after signing, packaging, notarization, download, and extraction.
5. Run a protected no-publication validation before any public release.
6. Remove ephemeral key stores and unsigned request artifacts on every exit path.
7. Bind signed output to the release-provenance manifest.

## Inspection workflow

Inventory all final artifacts and recursively enumerate executable/native content. Determine:

- Android keystore alias and upload certificate expected by the existing store app;
- iOS archive bundle, distribution certificate, profile, Team ID, entitlements, and exported IPA;
- every macOS Mach-O, helper, framework, JAR native library, outer `.app`, and DMG;
- every publisher-owned Windows EXE/DLL and final EXE/MSI installer;
- supported CPU architectures and which runner/toolchain creates each artifact;
- credential scopes, cleanup, logs, signing backends, and human approvals.

Do not treat local unsigned compilation as signing evidence.

## Remediation workflow

### Android and iOS

- Preserve the existing Store application identity and registered signing identity.
- Verify Android APK and AAB against a pinned public upload certificate.
- Verify the Xcode archive before export and the IPA after export.
- Record Mach-O UUIDs, bundle IDs, profiles, entitlements, and signed artifact hashes.
- Keep signing credentials only in the environment that creates the one tested mobile build.

### macOS

- Import the Developer ID identity into an ephemeral keychain.
- Sign nested code before the containing application and sign the final distribution image as required.
- Require the expected Team ID, Developer ID identity, secure timestamp, hardened runtime, and absence of debug entitlements.
- Submit the final artifact to notarization, require `Accepted`, staple, validate the staple, and check Gatekeeper for both image and mounted application.
- Treat an unsigned outer `.app` as invalid even when every inspected nested library is signed.
- For unsigned test packages, accept only a documented checksum-bound/ad-hoc policy. Never replace an existing invalid or non-ad-hoc signature automatically.

### Windows

- Prefer HSM/provider signing or OIDC-backed public-trust signing.
- If an exportable PFX is explicitly authorized, import it ephemerally and remove it reliably.
- Require Code Signing EKU, expected publisher, trusted chain, and timestamp.
- Sign publisher-owned native runtime code before installers, then verify extracted installer payload equals the signed application image.

## Verification details

Parse signing metadata structurally. Do not match exact display strings such as `(runtime)` when tools may emit combined flags such as `(adhoc,runtime)`. Read complete bounded process output and check exit status.

Use the commands and artifact hierarchy in [`references/platform-verification.md`](references/platform-verification.md). At minimum verify:

- signatures and identities of every nested executable object;
- outer application and installer signatures;
- timestamps, hardened runtime, entitlements, profiles, architecture, and package contents;
- accepted notarization, staple, and Gatekeeper state;
- checksums before and after artifact transport;
- cleanup after forced failure;
- validation-only mode performs no publication.

## Credential handoff

Produce one private handoff table with: secret/variable name, format, source, scope, environment, consuming workflow, test/production purpose, rotation, and safe encoding command. Keep public fingerprints in the repository only when policy requires pins. Never expose private values while verifying configuration.

## Failure lessons

- A successful nested signature does not prove the owning app is valid.
- Notarization submission is not notarization acceptance.
- A stapled DMG does not prove the mounted app has the correct identity.
- Exact flag-string matching rejects valid combined flags.
- Signing may change native bytes; bind or refresh integrity metadata only after independently verifying the signed result.
- A self-signed certificate is not a public production substitute.

## Resources

- Run [`scripts/inspect_macos_bundle.sh`](scripts/inspect_macos_bundle.sh) for a non-mutating bundle inventory.
- Read [`references/platform-verification.md`](references/platform-verification.md) before platform signing or verification.
