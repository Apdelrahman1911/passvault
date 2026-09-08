---
name: secure-platform-biometric-unlock
description: Design, implement, diagnose, or audit biometric unlock on Android, iOS, macOS, and Windows, including OS-protected key release, enrollment, invalidation, cancellation, lockout, fallback, restart, session publication, native bridges, lifecycle interruption, and unsupported-platform behavior. Use whenever Face ID, Touch ID, Android biometrics, Windows Hello, or biometric settings and unlock flows are added or changed.
---

# Secure Platform Biometric Unlock

Treat biometrics as an optional OS-protected route to an application key, never as a cosmetic prompt before reading an independently accessible secret.

Load `$desktop-native-interop-lifecycle` when a JVM Desktop implementation introduces or changes a native biometric bridge, loader, package, prompt lifecycle, or shutdown behavior.

## Security invariant

```text
successful OS authentication and protected key operation
  -> candidate application key
  -> authenticate existing vault/account verification state
  -> publish a new valid session
```

Do not publish an unlocked session before the final application verification succeeds.

## Threat model and boundaries

Assume attackers can copy local application files but cannot compromise the unlocked OS, biometric subsystem, or process memory. Require:

- no persistent plaintext application key or master password;
- device/account-bound protected material;
- authenticated encryption and application/vault identity binding;
- enrollment/key invalidation handling;
- one active operation with no queued prompt backlog;
- password recovery that does not weaken the biometric path;
- best-effort wiping of mutable managed/off-heap/native buffers;
- strict native library loading when a bridge is used.

Read [`references/platform-security.md`](references/platform-security.md) for platform-specific primitives and limitations.

## Inspection

Trace enrollment, retrieval, deletion, disable, re-enable, password change, vault/account reset, restore, restart, and lock. Identify:

- where the protected application key is created and stored;
- whether the prompt is cryptographically tied to release/decryption;
- what application record validates a released candidate key;
- when the authenticated session and navigation root change;
- error/cancellation mapping and password fallback;
- operation serialization, stale callbacks, background/minimize/focus/lock/shutdown behavior;
- capability names and unsupported-platform behavior;
- entitlements, Keychain/Keystore groups, native bridge packaging, signing, and architecture.

## Implementation workflow

1. Require an already authenticated session before initial enrollment.
2. Create new protected material before retiring the prior enrollment; switch authority atomically where the OS permits.
3. Bind ciphertext/AAD or platform credential context to format, application/vault identity, credential identity, and critical metadata.
4. Validate the released candidate through existing authenticated repository state.
5. Publish the same post-authentication session transition used by password unlock.
6. Use a non-queuing operation guard and opaque operation IDs.
7. Connect cancellation to explicit lock, lifecycle loss, host detach, and terminal shutdown without allowing cancellation failure to veto lock.
8. Distinguish cancelled, failed, locked out/busy, unavailable, not enrolled, invalidated, corrupted, and unsupported states.
9. Keep master-password fallback reachable and explicit.
10. Leave platforms without equivalent cryptographic protection password-only.

## Platform rules

- Android: require an auth-per-use hardware/OS-protected key and the application's accepted strong-authenticator policy; validate enrollment invalidation behavior on target API levels.
- iOS/macOS: use LocalAuthentication with a device-only Keychain access-control policy appropriate to the threat model; use current-biometric-set invalidation when required.
- Windows: do not call Windows Hello “fingerprint.” Require key release/derivation from a verified Windows credential operation, not `UserConsentVerifier` followed by an ordinary file read.
- Linux: expose unavailable unless an implementation proves equivalent cryptographic key-release guarantees.

## Verification

Automate success, wrong candidate key, tamper, corrupt/missing material, cancellation races, concurrent calls, stale callbacks, disable/re-enable, password/key rotation, restart, reset, restore, and cleanup. Verify unsupported platforms fail closed.

Physically test success, failure, cancellation, lockout, enrollment changes, device credential changes, restart, app update, fallback, lock/background/minimize/shutdown during prompt, and platform-specific invalidation. Compilation or a fake provider is not physical biometric evidence.

## Failure lessons

- A successful biometric UI prompt is neither secure key release nor a complete application unlock.
- Password and biometric success must converge on one session/navigation transition.
- First cold-start biometric use may legitimately require password-authenticated enrollment/bootstrap.
- Focus loss caused by an owned OS prompt must not self-cancel enrollment, but suppression must not extend to arbitrary dialogs.
- Windows Hello may use face, fingerprint, or PIN and does not provide Apple's exact current-biometric-set semantic.
