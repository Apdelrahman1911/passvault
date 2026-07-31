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

Never regenerate the key after publishing. Use the store-specific key upgrade
or rotation process if compromise is suspected.

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
