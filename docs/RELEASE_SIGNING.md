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
