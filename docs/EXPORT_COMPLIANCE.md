# iOS Encryption and Export-Compliance Record

Last technical review: 6 August 2026

This document is engineering evidence, not legal advice or an independent
export classification. The publisher completed App Store Connect’s encryption
questionnaire with France excluded. Apple reported that no export-compliance
documentation is required for that distribution scope. The repository status
is therefore `EXEMPT_APPROVED`, and `IOS_FRANCE_AVAILABLE` must remain `false`.

## Audited release artifact

The audit used Xcode 26.5 to create and locally verify a signed generic-device
Release archive. The archive was built in a temporary keychain and deleted after
inspection; it was not uploaded to App Store Connect.

| Property | Audited value |
| --- | --- |
| Bundle ID | `com.passvault.ios` |
| Version / build | `1.0.1` / `1000001` |
| Target | arm64, minimum iOS 18.5 |
| Executable SHA-256 | `62fef5e36eba8eaa889f034de7405267c1489af28bffdf275ad9110f08772435` |
| dSYM UUID | `9403D74D-9C2F-34F5-AF4B-8BE55A8B3219` |
| `ITSAppUsesNonExemptEncryption` | `false`; no documentation is required for the approved non-France scope |
| `ITSEncryptionExportComplianceCode` | Absent; Apple issued and required no code |
| Signing certificate SHA-256 | `9A:5B:6D:B6:2B:D4:E4:7B:AF:63:30:A9:D4:4D:CB:63:36:35:38:A5:D3:84:AD:AB:32:CC:7E:28:B5:E7:2A:F2` |
| Embedded provisioning profile | `6493524d-9e4f-46a2-b256-0802afeba3e0`; matches the signing certificate |

The Release framework was built with
`./gradlew :shared:linkReleaseFrameworkIosArm64`. Its static archive SHA-256
was `abc22d7f8d92c40b29c3103e6eace9a6cea27009203854d3af0ba7730f46d014`.
Fourteen matching Release dSYM symbols confirm that the final application
contains XChaCha20-Poly1305, Argon2 password hashing, BLAKE2b, and libsodium
random-generation implementations; Okio HMAC/SHA remains linked for TOTP.

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

## Current App Store Connect determination

[Apple’s export-compliance overview](https://developer.apple.com/help/app-store-connect/manage-app-information/overview-of-export-compliance)
requires a determination when an app uses, accesses, contains, implements, or
incorporates encryption. Apple’s
[documentation table](https://developer.apple.com/help/app-store-connect/reference/export-compliance-documentation-for-encryption/)
states that an app using an industry-standard algorithm outside Apple’s OS
requires a French encryption declaration when distributed in France.

The App Store Connect questionnaire was completed on 6 August 2026 using the
actual behavior above and with France excluded from distribution. Apple’s result
was **no documentation required**. No French declaration, US document, Apple
approval code, or other file was requested or issued.

`ITSAppUsesNonExemptEncryption = false` records that documentation is not
required for the approved distribution configuration. It does **not** mean that
PassVault has no encryption: PassVault continues to include and use standard
third-party cryptography through libsodium and Okio.

The durable questionnaire facts are:

| Declaration fact | Recorded result |
| --- | --- |
| PassVault uses encryption | **Yes** |
| Contains proprietary/non-standard cryptography | **No** |
| Contains standard third-party cryptography | **Yes** — libsodium and Okio |
| France included in distribution | **No** |
| Documentation required for the selected scope | **No**, per App Store Connect |

## France release constraint

The no-documentation result is valid only while France remains excluded. The
release configuration must keep `IOS_FRANCE_AVAILABLE=false`; release tooling
also checks App Store Connect’s actual territory availability before any iOS
store upload. France must never be enabled silently.

Before enabling France later, the Account Holder or authorized release manager
must reopen **Apps → PassVault → App Information → App Encryption
Documentation**, complete a new review for French availability, upload the
French encryption declaration if Apple requests it, and record the renewed
result. Only then may the availability constraint, compliance status, plist
flag, or an Apple-issued code be changed. See
[Apple’s submission procedure](https://developer.apple.com/help/app-store-connect/manage-app-information/determine-and-upload-app-encryption-documentation/).
