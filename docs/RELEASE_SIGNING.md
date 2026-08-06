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

PassVault has not previously been uploaded to Google Play. The canonical first
upload key was explicitly adopted on 6 August 2026. Its required alias is
`passvault-upload` (also pinned in
`release/android/passvault-upload-alias.txt`) and its certificate SHA-256 is:

```text
7D:4D:11:20:B1:D1:9F:5B:B9:42:E0:6C:60:0F:2B:64:81:E5:E6:82:47:52:23:FA:4E:7D:C7:B2:7C:DB:10:37
```

The signed `standardRelease` APK and AAB must both match this certificate. Do
not regenerate or substitute the keystore when creating the first Play app;
register this certificate as the Play upload key.

Generate signing material once:

```powershell
./scripts/create-android-signing-material.ps1
```

The script creates `release/private/android/`, which is ignored by Git. It
refuses to overwrite an existing key. Back up the complete directory to two
encrypted offline locations before publishing an APK. Losing the key can make
directly distributed APKs impossible to update. With Google Play App Signing,
this repository key should be registered as the upload key and Google's app
signing certificate must also be archived and pinned separately.

Build and verify local signed artifacts with:

```powershell
./scripts/build-signed-android.ps1
```

Configure the four GitHub repository secrets listed in the generated
`GITHUB-SECRETS.txt` file:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Never regenerate the key. Use the store-specific key upgrade or rotation
process if compromise is suspected.

## Windows

Public Windows releases require a trusted Authenticode code-signing certificate
with the Code Signing EKU (`1.3.6.1.5.5.7.3.3`). A locally generated
self-signed certificate is not a production substitute because it will not
establish publisher trust on users' machines.

Export the publisher certificate and private key as a password-protected PFX,
then configure these GitHub repository secrets:

- `WINDOWS_CERTIFICATE_BASE64`: the entire PFX encoded as one Base64 string.
- `WINDOWS_CERTIFICATE_PASSWORD`: the PFX export password.

Optional repository variable `WINDOWS_TIMESTAMP_URL` selects the RFC 3161
timestamp service. The default is `http://timestamp.digicert.com`. The workflow
imports the PFX into the ephemeral runner's current-user certificate store,
checks its expiry, private key, and Code Signing EKU, signs the application
launcher before packaging, signs the final EXE and MSI, verifies Windows trust,
checks the expected signer thumbprint, requires a timestamp, and removes the
certificate from the runner.

The MSI upgrade UUID is permanently fixed as
`B3B60257-BA42-4233-AF33-5CECFA171EB0`. It must not change between releases.

## macOS

Direct macOS distribution requires an Apple Developer Program membership and a
`Developer ID Application` certificate. Export that certificate and its private
key as a password-protected PKCS #12 file, then configure:

GitHub repository secrets:

- `MACOS_CERTIFICATE_BASE64`
- `MACOS_CERTIFICATE_PASSWORD`
- `MACOS_NOTARIZATION_APPLE_ID`
- `MACOS_NOTARIZATION_PASSWORD` (an Apple app-specific password)

GitHub repository variables:

- `MACOS_SIGNING_IDENTITY` (the full `Developer ID Application: ...` identity)
- `MACOS_NOTARIZATION_TEAM_ID` (the 10-character Apple Developer Team ID)

CI creates a temporary keychain, validates the certificate and identity, stores
notarization credentials in that keychain, and removes it after the job. The DMG
is accepted only if the app has the expected Developer ID signer and secure
timestamp, does not request `get-task-allow`, passes strict `codesign`
verification, receives an `Accepted` result from Apple's `notarytool`, has its
ticket stapled, and passes Gatekeeper assessment. The complete notarization log
is retained as a CI artifact.
