# Native release verification commands

Run these against final artifacts, not intermediate build directories. Pin expected public certificate fingerprints and Team/publisher identities in repository policy where appropriate.

## Android

```bash
apksigner verify --verbose --print-certs app-release.apk
jarsigner -verify -verbose -certs app-release.aab
bundletool dump manifest --bundle app-release.aab
```

Compare the APK/AAB signer certificate digest to the existing Store upload certificate. Inspect package ID, version code/name, permissions, SDK levels, split/ABI content, native libraries, and debuggable/profileable flags.

## iOS

```bash
codesign --verify --strict --verbose=4 Payload/App.app
codesign -d --entitlements :- Payload/App.app
security cms -D -i Payload/App.app/embedded.mobileprovision
otool -L Payload/App.app/App
lipo -archs Payload/App.app/App
```

Inspect every extension/framework, archive metadata, distribution profile, Team ID, application identifier, entitlements, architectures, and exported IPA. Use `xcodebuild -exportArchive` and the platform validation/upload tool in validation-only mode before upload.

## macOS

```bash
codesign --verify --strict --verbose=4 App.app
codesign -dv --verbose=4 App.app
codesign -d --entitlements :- App.app
spctl --assess --type execute --verbose=4 App.app
xcrun notarytool history --keychain-profile PROFILE
xcrun stapler validate App.app
```

Enumerate all Mach-O files and nested bundles; verify them individually before verifying the outer app. Parse CodeDirectory flags as a set: `(adhoc,runtime)` contains the runtime flag and must not be rejected by exact-string matching.

For DMGs, verify the image signature, notarization result, staple, and mounted application independently. Require a notary result whose status is `Accepted`; a successful submission command is not enough.

## Windows

```powershell
Get-AuthenticodeSignature -FilePath .\App.exe | Format-List *
signtool verify /pa /all /v .\App.exe
signtool verify /pa /all /v .\Installer.exe
```

Require `Valid`, the expected publisher certificate and thumbprint, Code Signing EKU, a trusted chain, and a valid RFC 3161 or approved Authenticode timestamp. Enumerate and verify publisher-owned EXE/DLL files inside the installed image as well as the installer.

## Cleanup injection

Force a failure immediately after credential import and again after signing. Prove that ephemeral keychains/certificate stores, decoded credentials, temporary provisioning profiles, signing requests, and unsigned artifacts are gone. Never print secret values during cleanup verification.
