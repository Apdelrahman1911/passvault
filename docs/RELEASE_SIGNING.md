# Release Signing

Release signing credentials are publisher-owned secrets. They must never be
committed, attached to issues, printed in logs, or stored as plain-text CI
artifacts. The repository contains only public certificates, pinned
fingerprints, validation scripts, and secret-name documentation.

## Android

The pinned public certificate is stored in
`release/android/passvault-release-cert.pem`; its SHA-256 fingerprint is stored
beside it. CI verifies both the APK and AAB against this fingerprint, so an
artifact signed with an unexpected key is rejected.

PassVault is already registered and has uploaded builds in Google Play. The
canonical upload key was explicitly adopted on 6 August 2026. Its required alias is
`passvault-upload` (also pinned in
`release/android/passvault-upload-alias.txt`) and its certificate SHA-256 is:

```text
7D:4D:11:20:B1:D1:9F:5B:B9:42:E0:6C:60:0F:2B:64:81:E5:E6:82:47:52:23:FA:4E:7D:C7:B2:7C:DB:10:37
```

The signed `release` APK and AAB must both match this certificate. Do
not generate or substitute a new keystore: an arbitrary key cannot update the
existing Play app. Keep the registered keystore in the ignored path named by
`ANDROID_UPLOAD_KEYSTORE_FILE` in `release/private/values.env`, with encrypted
offline backups. If it is lost or compromised, use Google Play Console's upload
key reset process and record the approved rotation before changing the pinned
public certificate.

Build and verify local signed artifacts with:

```powershell
./scripts/build-signed-android.ps1
```

Use the signed-build script instead of invoking Gradle directly while signing
environment variables are present. If a direct Gradle invocation is absolutely
necessary, pass `--no-configuration-cache`; otherwise Gradle can persist
keystore passwords from the configuration environment in its local cache.
`scripts/build-android.sh` enforces this automatically whenever any Android
signing variable is nonblank.

The build script reads the canonical ignored `values.env`, requires the pinned
alias, runs release lint and final-manifest checks, and verifies both signatures.
Upload these four secrets only through `scripts/configure-mobile-release.sh`.
They belong only in the `mobile-beta` environment because later stages promote
the exact existing build and must not receive upload-key material:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Never regenerate the key. Use the store-specific key upgrade or rotation
process if compromise is suspected.

## Windows

Public Windows releases require a public-trust Authenticode identity and trusted
timestamp. A self-signed certificate is not a production substitute. Select
exactly one `WINDOWS_SIGNING_BACKEND`; validators reject mixed configuration:

- `signpath` (recommended starting point for this open-source project): the key
  remains in the provider HSM. CI submits immutable request archives containing
  only the unsigned PassVault launcher or the two PassVault installers. It waits
  for policy approval/signing, independently verifies the returned file set,
  publisher, chain, and timestamp, applies it atomically, and deletes transient
  unsigned GitHub artifacts. The tracked provider configuration is
  `release/windows/signpath-artifact-configuration.xml`.
- `azure-artifact-signing`: GitHub authenticates through an environment-bound
  OIDC federated credential; no Azure client secret or certificate key enters
  GitHub. This route requires an eligible Public Trust account/profile and the
  narrow certificate-profile signer role.
- `local-pfx`: permitted only for an existing CA-authorized exportable key. The
  validator requires an explicit policy acknowledgement, validates the private
  key and Code Signing EKU (`1.3.6.1.5.5.7.3.3`), derives a SHA-256 certificate
  pin, imports the PFX into the ephemeral current-user store, and removes every
  imported identity on success or failure.

All backends preserve valid timestamped vendor signatures and allow the
PassVault publisher identity to sign only `PassVault.exe` plus the final
PassVault EXE/MSI installers. They verify every nested EXE/DLL and both
installers with Windows policy and require a timestamp. The MSI is extracted
after signing to prove that its native payload is byte-for-byte identical to
the already-signed app image.

The MSI upgrade UUID is permanently fixed as
`B3B60257-BA42-4233-AF33-5CECFA171EB0`. It must not change between releases.

See `PRODUCTION_SIGNING_HANDOFF.md` for the exact provider setup, environment
secret/variable names, safe encoding commands, eligibility limitations, and
manual approval boundary. See `CODE_SIGNING_POLICY.md` for custody and
authorization rules.

## macOS

Direct macOS distribution requires an Apple Developer Program membership and a
`Developer ID Application` certificate. Export that certificate and its private
key as a password-protected PKCS #12 file, then configure:

GitHub `mobile-production` environment secrets:

- `MACOS_CERTIFICATE_BASE64`
- `MACOS_CERTIFICATE_PASSWORD`
- `MACOS_NOTARIZATION_APPLE_ID`
- `MACOS_NOTARIZATION_PASSWORD` (an Apple app-specific password)

GitHub repository variables:

- `MACOS_SIGNING_IDENTITY` (the full `Developer ID Application: ...` identity)
- `MACOS_NOTARIZATION_TEAM_ID` (the 10-character Apple Developer Team ID)
- `MACOS_DEVELOPER_ID_CERTIFICATE_SHA256` (derived from the supplied P12)

Delete any repository-level copies of all Windows/macOS secret values after the
environment secrets are present. Remote Windows resource identifiers also live
only as `mobile-production` environment variables. The selected backend and
expected publisher may remain repository variables because neither is a
credential.

CI creates a temporary keychain, validates the certificate identity, Team ID,
expiry, private-key pairing, and pinned SHA-256 fingerprint, stores notarization
credentials in that keychain, and removes it after the job. Every Mach-O object
inside the app must have the expected Developer ID signer/Team ID, a secure
timestamp, Hardened Runtime, strict `codesign` validity, and no
`get-task-allow`. The DMG must receive an `Accepted` result from `notarytool`,
have its ticket stapled and validated, and pass Gatekeeper checks for both the
DMG and mounted app. The complete notarization log is retained privately.

The protected validation workflow freezes these signed artifacts without
publishing them. Stable publication later consumes that exact attested bundle
and does not rebuild. See `PRODUCTION_SIGNING_HANDOFF.md` for every private input,
safe upload command, exact GitHub scope, and credential source.
