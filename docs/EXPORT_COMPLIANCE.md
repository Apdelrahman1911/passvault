# iOS Encryption and Export-Compliance Record

Last technical review: 6 August 2026

This document is engineering evidence, not legal advice, an export
classification, or an approval. `EXPORT_COMPLIANCE_STATUS` must remain
`PENDING` until the publisher completes Apple’s questionnaire and obtains any
required US or French documentation.

## Audited release artifact

The audit used Xcode 26.5 to create an unsigned, generic-device Release archive
with signing explicitly disabled. It is not an uploadable App Store artifact.

| Property | Audited value |
| --- | --- |
| Bundle ID | `com.passvault.ios` |
| Version / build | `1.0.1` / `1000001` |
| Target | arm64, minimum iOS 18.5 |
| Executable SHA-256 | `51b51dd0d2b36ee5d52345ec606c2ab595266b7d2f55f6dcf125484c516b8166` |
| dSYM UUID | `3CB7F6A1-20D9-3728-AB04-A024A65856A3` |
| `ITSAppUsesNonExemptEncryption` | Unset; no determination has been asserted |

The Release framework was built with
`./gradlew :shared:linkReleaseFrameworkIosArm64`. Its static archive SHA-256
was `abc22d7f8d92c40b29c3103e6eace9a6cea27009203854d3af0ba7730f46d014`.
The app and dSYM UUIDs match. dSYM symbols confirm that the final executable
contains XChaCha20-Poly1305, Argon2 password hashing, BLAKE2b, libsodium random
generation, and Okio HMAC/SHA implementations.

## Cryptographic software in the app

| Library or platform facility | Algorithms used | PassVault purpose |
| --- | --- | --- |
| IonSpin `multiplatform-crypto-libsodium-bindings-iosarm64` 0.9.5, statically embedding libsodium | Argon2id, XChaCha20-Poly1305-IETF, keyed BLAKE2b, libsodium CSPRNG | Derive the master/backup key; wrap the vault key; encrypt and authenticate vault records and `.pvault` backups; derive domain-separated subkeys and blind indexes; generate keys, salts, and nonces |
| Okio `okio-iosarm64` 3.17.0 | HMAC-SHA-1, HMAC-SHA-256, HMAC-SHA-512 | RFC 6238 time-based one-time password generation on the device |
| Apple Security framework and Keychain | Apple-supplied device data protection | Store the vault encryption key in a `WhenPasscodeSetThisDeviceOnly`, `biometryCurrentSet` Keychain item for optional biometric unlock |
| Apple LocalAuthentication framework | Face ID or Touch ID authentication supplied by iOS | Authorize access to the Keychain item; PassVault never receives a biometric template |

The resolved `iosArm64CompileKlibraries` graph and dependency verification file
identify the two application crypto dependencies above. The Release executable
links Apple `Security` and `LocalAuthentication`. It does not link SQLCipher,
OpenSSL, CommonCrypto, CryptoKit, or a networking/TLS library. The Room/SQLite
database is not encrypted as a whole; sensitive payloads are encrypted by the
application before storage.

The static libsodium framework contains additional unused primitives because
they ship in the library archive. Dead stripping and matching Release dSYM
symbols were used to distinguish algorithms present in the library from the
ones used by PassVault.

## Scope and implementation boundary

- **Authentication:** TOTP uses application-supplied Okio HMAC code. Biometric
  authorization and device protection are supplied by Apple.
- **Local vault storage:** encryption is implemented by PassVault through the
  statically linked libsodium dependency; it is not limited to Apple OS crypto.
- **Backup encryption:** the app uses its own Argon2id and
  XChaCha20-Poly1305 construction with a separate user password.
- **Secure communication:** the application currently has no account, sync,
  analytics, telemetry, or other application network service. Website and
  store connections occur outside the application artifact and use the host
  platform’s HTTPS implementation.
- **Algorithms:** all algorithms used by PassVault are published,
  industry-standard algorithms. No proprietary, unpublished, or non-standard
  cryptographic algorithm or protocol was found.

Encryption is therefore **not limited to encryption supplied by Apple’s
operating system** and is not limited to authentication. It covers secure local
storage and user-created encrypted backups.

## Apple and France decision gate

[Apple’s export-compliance overview](https://developer.apple.com/help/app-store-connect/manage-app-information/overview-of-export-compliance)
requires a determination when an app uses, accesses, contains, implements, or
incorporates encryption. Apple’s
[documentation table](https://developer.apple.com/help/app-store-connect/reference/export-compliance-documentation-for-encryption/)
states that an app using an industry-standard algorithm outside Apple’s OS
requires a French encryption declaration when distributed in France.

**France is not currently approved as part of the release plan.** No tracked
availability selection or completed French declaration exists. Keep France
unselected in App Store Connect until the publisher deliberately confirms the
territory and any required declaration is accepted. Do not infer a US
classification or exemption from this technical record.

Before TestFlight external testing or App Store submission:

1. Complete App Store Connect’s encryption questionnaire using the behavior
   documented here.
2. Decide whether France will be offered. If yes, prepare and submit the French
   declaration before enabling that territory.
3. Determine the applicable US export classification or exemption with
   qualified advice where needed.
4. Store Apple’s approved status or key, then set
   `EXPORT_COMPLIANCE_STATUS` to the corresponding approved release value.
5. Only after approval, set the correct `ITSAppUsesNonExemptEncryption` value
   (and any Apple-provided compliance key) in the Release Info.plist.

Apple evaluates documentation case by case and recommends completing it before
TestFlight App Review or App Review. See
[Apple’s submission procedure](https://developer.apple.com/help/app-store-connect/manage-app-information/determine-and-upload-app-encryption-documentation/).
