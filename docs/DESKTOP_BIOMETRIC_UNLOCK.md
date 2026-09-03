# Desktop biometric unlock

Last reviewed: 2026-08-14

## Support matrix

| Platform | User-facing method | Protection mechanism | Status |
| --- | --- | --- | --- |
| macOS arm64/x64 | Touch ID | `LocalAuthentication` plus a device-only Keychain item guarded by `biometryCurrentSet` | Implemented; physical Touch ID validation required for each release |
| Windows x64 | Windows Hello | Platform WebAuthn credential PRF output, HKDF-SHA-256, and AES-256-GCM | Implemented for Windows with WebAuthn API v8/PRF support; physical Windows Hello validation required |
| Linux | Unavailable | Master password only | Intentional fail-closed policy |

Windows Hello is deliberately not labelled fingerprint authentication. Windows may satisfy user verification with
face, fingerprint, or the Windows Hello PIN according to the user's OS configuration. Linux remains unsupported
because the repository has no Linux API that can cryptographically gate release of application key material with
equivalent, portable guarantees.

## Shared security boundary

Desktop implements the existing `BiometricKeyStore` boundary; it does not introduce another unlock/session model.
The complete path is:

```text
unlocked vault session
  -> explicit biometric enrollment
  -> OS-protected VEK copy

OS authentication and protected key release
  -> candidate 32-byte VEK
  -> DefaultBiometricUnlockService
  -> authenticated vault verification record
  -> publish unlocked session only after verification
```

The master password and KEK never cross the native bridge. A released VEK is copied into managed memory only long
enough for the existing repository verification path, then wiped best-effort. Changing the master password rewraps
the same VEK, so a valid biometric enrollment remains valid. Backup restore retires the old enrollment before Room
replacement and retains the existing best-effort rollback behavior.

Only one Desktop biometric operation may be active. Kotlin uses a non-queuing coroutine `Mutex` admission guard;
rapid duplicate calls fail immediately instead of replaying a stale prompt. The JNA wrapper rejects a second native
operation, and each native implementation also has its own operation slot. Native
prompt cancellation is connected to coroutine cancellation, manual/session lock, minimize/focus-lock transitions,
window detachment, and process shutdown. Operation identifiers prevent a stale cancellation callback from cancelling
a newer prompt. A cancellation that races native entry always arms a one-shot signal scoped to the owning prompt,
even if its blocking worker has not executed its first instruction. The signal is cleared only after that worker and
any native cleanup finish, so it cannot poison a later unlock attempt.

Every repository `lock`/`lockAndRun` transition invokes the platform prompt controller before waiting for an active
VEK session lease. Cancellation failure cannot veto locking; the repository still waits for the lease, wipes its
owned key, and reaches the terminal locked state. This keeps shared inactivity/background/restore locks equivalent
to Desktop menu, focus, minimize, and shutdown locks.

## Native ABI and loading

The reviewed C ABI is in
`app-desktop/native/biometric-bridge/include/passvault_biometric.h`. It exposes fixed-size vault hashes and VEKs,
opaque contexts, capability queries, enrollment/retrieval/deletion, explicit cancellation, and a versioned ABI.
JNA passes sensitive inputs through off-heap buffers and clears those buffers in `finally` paths.

The bridge is built by CMake from repository source and staged by Gradle under a fixed platform directory. The
manifest has exactly five bounded fields: ABI, platform, library name, integrity policy, and SHA-256. Development
layouts require an exact checksum match before JNA loads the file. An installed macOS application additionally
requires the owning app and bridge to have matching timestamped Developer ID Application signatures with Hardened
Runtime; an ad-hoc package is intentionally insufficient for installed Touch ID testing.

Unsigned testing-candidate DMGs remain intentionally buildable, and their packaging gate accepts only the exact staged
checksum or the valid ad-hoc signatures produced during packaging. Because macOS can leave an Intel app launcher
unsigned while Apple Silicon launchers are linker-signed, the unsigned packaging path explicitly ad-hoc signs and
deep-verifies the completed owning `.app` before its bridge gate runs. It refuses to replace any existing invalid or
non-ad-hoc signature. The runtime still keeps Touch ID unavailable in those installed candidates. A build requested
with `MACOS_SIGN=true`, or a local validation requested with
`-Ppassvault.requireInstalledMacOsBiometric=true`, must pass the full Developer ID gate before packaging can succeed.

Production signing changes native-library bytes:

- Windows signs and timestamps `passvault_biometric.dll`, then
  `update-desktop-biometric-checksum.ps1` binds the signed bytes into the app image before installers are created.
- macOS permits the original checksum or a strict Developer ID fallback during initial integrity evaluation, but an
  installed layout always requires the strict Developer ID result. The fallback requires the bridge and owning
  `.app` to pass `/usr/bin/codesign --verify --strict`, use the same nonempty 10-character Team ID, identify as
  Developer ID Application code, contain Hardened Runtime, and contain secure timestamps. Release verification
  independently validates every nested Mach-O file, the app, the DMG, notarization, staple, and Gatekeeper state.

Installed resource paths are derived from the bundled `java.home` layout before any development override is
considered. An unsupported architecture, missing bridge, malformed manifest, checksum mismatch, invalid Developer ID fallback,
ABI mismatch, symlink, unsafe directory, or load error becomes an unavailable biometric capability. It never falls
back to loading a system-search-path library.

## macOS design

The bridge uses `LAPolicyDeviceOwnerAuthenticationWithBiometrics`; it does not accept the device password as a
biometric substitute. Enrollment performs a fresh Touch ID evaluation and creates a new Keychain item with:

- `kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly`;
- `kSecAccessControlBiometryCurrentSet`;
- the Data Protection Keychain;
- synchronization disabled;
- an authentication reuse duration of zero.

Retrieval asks Keychain to return item data through a fresh `LAContext`, so Touch ID guards the actual VEK release;
there is no separate prompt followed by an ordinary secret read. Changing the enrolled biometric set invalidates the
item. A generation marker whose Keychain item has disappeared is reported as invalidated—not silently disabled—and
is retired before master-password re-enrollment. Removing the device passcode, Keychain corruption, missing
generation metadata, or an authentication-policy failure fails closed.

The app-private metadata file contains only a magic/version marker, the SHA-256 vault identifier, and a random
generation nonce. The VEK remains in Keychain. Metadata is owner-only, non-symlink, fixed-size, atomically replaced,
and directory-synced. Re-enrollment creates the new item first, atomically switches metadata, and only then retires
the old item. A failed switch deletes the new item and preserves the old enrollment.

## Windows design

Capability naming comes from the Windows platform. `UserConsentVerifier.CheckAvailabilityAsync` distinguishes
available, not configured, busy/locked, and unavailable states. It is only a capability signal. It is never used as a
standalone authorization prompt.

Actual key protection uses `webauthn.dll`, loaded from System32 with explicit function resolution. PassVault requires
WebAuthn API v8 because creation-time PRF evaluation was added in that API. Enrollment requests:

- the platform authenticator only;
- required user verification;
- a resident, device-bound credential;
- required `credProtect` user verification;
- ES256 only;
- the PRF extension with a fresh 32-byte salt;
- no attestation conveyance.

The relying-party identifier is `passvault.kiramanga.me`, with origin
`https://passvault.kiramanga.me`. This is a stable local cryptographic namespace, not a registered deep-link or
network contract. The bridge requires the credential-management API to mark the PassVault credential as removable
so disable/reset can retire it, and separately requires internal authenticator transport. It rejects
backup-eligible/backed-up credentials, missing user presence/verification flags, a wrong RP hash, a different
credential ID, malformed COSE keys, missing PRF output, or non-internal transport.

The 32-byte PRF result is available only through a successful assertion for the exact Windows Hello credential. It
feeds HKDF-SHA-256 with a fresh local salt and vault-bound context. The resulting 256-bit key wraps the VEK using
AES-256-GCM in Windows CNG. Associated data binds the format version, vault hash, RP hash, ES256 public key, PRF and
KDF salts, nonce, and credential ID. Retrieval verifies the fresh assertion's ES256 signature over authenticator data
and the fresh client-data challenge before it attempts AEAD decryption.

The biometric directory and each newly-created envelope receive a protected current-user-only Windows DACL. The
encrypted envelope is bounded to 16 KiB, read without following reparse points, and written by create-new temporary
file, flush, and atomic replace. It contains no plaintext VEK. Corruption, tag failure, key
loss, credential loss, wrong vault ownership, or invalid assertion retires the unusable enrollment and requires the
master password. Re-enrollment and disable enumerate only PassVault credentials whose exact RP and 32-byte vault user
ID match; unrelated Windows Hello/passkey credentials are never touched. A newly created credential must also be
resident, removable, device-bound, and visible through the platform credential inventory before PassVault persists
an envelope for it. Retrieval rechecks that the envelope credential still belongs to that exact RP/vault pair. A
returned resident user handle, when present, must also match the vault hash. Corrupt local envelope bytes are never
trusted as authority to delete a credential; invalidation enumerates only by the trusted caller vault identity.

Credential inventory is authoritative only while Windows Hello reports available. Windows documents that a locked
authenticator can temporarily omit credentials from `WebAuthNGetPlatformCredentialList`; PassVault therefore
preserves a well-formed local enrollment during lockout/unavailability and reports the capability failure instead of
misclassifying a temporary omission as permanent credential loss. Missing credentials invalidate enrollment only
after the platform authenticator is available again. A Windows Hello reset is reported as not enrolled; explicit
disable can retire the now-unusable local envelope immediately, or the next authoritative inventory check will
invalidate it before password-authenticated re-enrollment.

Replacing an enrollment makes the new authenticated envelope authoritative before older matching credentials are
retired. Windows does not offer a transaction spanning its credential store and the local envelope, so a failed
best-effort cleanup can leave an old platform credential orphaned. It has no authoritative PRF salt/ciphertext pair
and cannot unlock the current vault; disable/re-enable retries vault-scoped cleanup. An orphaned OS credential left
by app uninstallation likewise has neither the current local PRF salt nor ciphertext and cannot unlock a vault.

Windows Hello does not provide Apple's `biometryCurrentSet` semantic. Adding another face/fingerprint may not
invalidate an existing Windows Hello credential because the credential represents the user's Windows Hello account,
not a specific sensor set. Device/account credential reset, key deletion, unsupported PRF, and credential corruption
do invalidate the path. This difference is an OS property and must not be hidden in UI or release evidence.

## Desktop lifecycle and UX

Security settings always show the platform capability explicitly:

- Touch ID on macOS;
- Windows Hello on Windows;
- biometrics unavailable on Linux/unsupported systems.

Not enrolled, temporarily locked, unavailable, cancelled, invalidated, and disabled are distinct states. Every error
offers the master password as the recovery path. Enabling requires an already authenticated unlocked vault session.
Disabling deletes the platform material and its relationship metadata.

An owned OS prompt can temporarily take focus from the application. During that prompt, a due focus-loss lock may be
deferred so enrollment does not cancel itself, but never beyond 60 seconds from the first external focus loss.
Minimize, explicit lock, inactivity lock, and shutdown remain armed. The independent focus-loss policy allows 30
seconds per unlocked session and accumulates only time spent outside PassVault across focus changes; briefly returning
to the app does not replenish that budget. When the prompt finishes, PassVault immediately re-evaluates the cumulative
budget on the Swing event thread. This suppression must never be widened to arbitrary file dialogs or background
activity.

Desktop shutdown is terminal and bounded. The first close request immediately protects the window, removes the tray
surface, cancels an active biometric prompt, and asks Compose to exit. Vault locking/database closure, owned-clipboard
clearing, and native biometric teardown then run as independent cleanup boundaries; one blocked cryptographic or OS
provider cannot prevent the others from running or keep the window open. The process gives cleanup 2.5 seconds, then
terminates fail-closed so process teardown reclaims in-memory keys. The JNA bridge itself waits at most 1.5 seconds for
an in-flight native call. It never frees a context still in use: a late returning call performs deferred destruction,
while a call that never returns is reclaimed only with terminal process exit. These deadlines apply only to app
shutdown and do not weaken the normal lock path or allow the app to continue after an unsuccessful lock.

## Build and release integration

Required developer tools:

- CMake 3.25 or newer;
- Apple Clang/Xcode frameworks on macOS;
- Visual Studio 2022 C++ build tools plus a current Windows SDK/C++/WinRT headers on Windows;
- JDK 17 and the checked-in Gradle wrapper.

Relevant Gradle tasks are:

```bash
./gradlew :app-desktop:configureDesktopBiometricBridge
./gradlew :app-desktop:buildDesktopBiometricBridge
./gradlew :app-desktop:testDesktopBiometricBridge
./gradlew :app-desktop:stageDesktopBiometricBridge
./gradlew :app-desktop:desktopTest
```

`prepareDesktopAppResources`, Desktop run tasks, Desktop tests, app images, and package tasks depend on the reviewed
bridge when the host is macOS or Windows. Linux builds intentionally omit it. Production uses the existing Desktop
signing inputs; biometric support adds no private credential or repository secret. The Windows release signing
catalog now admits exactly the PassVault launcher and the reviewed bridge as unsigned PassVault-owned runtime code.
The macOS release validator already requires a Developer ID signature, Hardened Runtime, and timestamp on every
nested Mach-O object, including this bridge.

## Automated and manual verification

Automated coverage must keep proving:

- fixed vault-ID hashing and 32-byte VEK bounds;
- native ABI plus Windows AES-GCM round-trip/tamper, envelope-bound identity, device-bound credential, and
  authenticator-flag/temporary-inventory-availability checks on a Windows runner;
- macOS owner-only metadata, symlink/mode rejection, and LocalAuthentication error mapping on a macOS runner;
- failure mapping, cancellation, invalidation, and managed/off-heap wiping paths;
- no concurrent prompt backlog;
- prompt-lifetime focus-lock coordination;
- Linux and unsupported architectures stay unavailable;
- native ABI/capability loading from both development and packaged resource layouts;
- modified bridge bytes and malformed manifests fail before loading;
- Windows signed-byte checksum binding and publisher/timestamp verification;
- macOS nested signing/Hardened Runtime/notarization/Gatekeeper verification;
- candidate and production packaging include exactly the expected architecture-specific bridge.

Before shipping on macOS, use real Touch ID hardware to verify enable, unlock after restart, cancel, failed scan,
lockout/recovery, enrolled-finger change invalidation, passcode removal, re-enable, disable, password change, restore,
focus loss, minimize, background/foreground, shutdown during prompt, upgrade, and both arm64/x64 packages where
hardware is available.

Before shipping on Windows, use a supported Windows 11 x64 device to verify Windows Hello face/fingerprint/PIN
according to the machine configuration, enable, unlock after restart, cancel, failure, lockout/recovery, credential
reset, re-enable, disable, password change, restore, focus loss, minimize, shutdown during prompt, app update, MSI/EXE
install, and signed DLL/installer verification. Also prove that deleting or replacing the WebAuthn credential makes
the envelope unusable and that copying the envelope to a different Windows account/device cannot release the VEK.

No automated test or virtual CI runner can substitute for those OS-authenticator checks. A release is not
biometric-verified merely because the native code compiled.

## Troubleshooting

- **Unavailable on Windows:** confirm Windows Hello is configured and the OS exposes WebAuthn API v8 with PRF. Older
  builds deliberately stay password-only.
- **Unavailable on macOS:** confirm the Mac has Touch ID enrolled and a device passcode. Intel Macs without Touch ID
  are password-only.
- **Available but enable fails:** inspect only redacted application status; never log native paths, credential IDs,
  PRF output, VEKs, or Keychain data. Retry after a password unlock.
- **Invalidated after an OS change:** unlock with the master password and enable biometric unlock again.
- **Packaged app differs from local run:** run the platform release signature/checksum validator. Do not bypass the
  bridge manifest or load a library from `PATH`, `java.library.path`, or a temporary extraction directory.
