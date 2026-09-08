# Platform biometric security reference

Recheck current official platform APIs and security behavior after OS/toolchain upgrades. The architectural invariant is stable: the protected cryptographic operation and application-session validation must both succeed.

## Android

Use a non-exportable Android Keystore key configured for each-use authentication and the product's accepted authenticator strength. Couple `BiometricPrompt` to the cryptographic operation. Decide explicitly whether device credential fallback is allowed. Test key invalidation, enrollment changes, lockout, OS upgrade, backup/restore, and API-level differences on real hardware.

Do not prompt successfully and then read a wrapped vault key decryptable by an ordinary application key.

## iOS and macOS

Store device-only protected material in Keychain with `SecAccessControl` and an access policy matching the threat model. Use biometric-current-set invalidation when enrollment changes must invalidate unlock. Evaluate through `LAContext`, keep prompt/reason strings localized, distinguish cancellation from invalidation/lockout, and delete stale material after authenticated recovery.

On macOS JVM applications, use a narrow signed native bridge. Package and sign the bridge before the outer app, validate architecture/ABI/integrity through the production loader, and include the required usage description/entitlements.

## Windows

`UserConsentVerifier` can prove that a prompt succeeded but, by itself, does not cryptographically gate a file read. Use a Windows Hello-backed credential/key operation—such as a product-reviewed Passport/Hello key design—whose private operation is necessary to unwrap or derive the application key. Bind credential identity and application/vault context, and handle account/device/enrollment/key deletion.

Name the feature “Windows Hello,” because the user may authenticate with face, fingerprint, or PIN.

## Linux

Keep biometric unlock unavailable unless the selected desktop stack can provide device/account-bound, non-exportable, authentication-gated cryptographic key release with defined invalidation and trustworthy application integration. A fingerprint PAM prompt followed by a normal file read is not equivalent.

## Application validation

After platform release, authenticate the candidate key against an existing application verification record before publishing the session. This catches stale, corrupt, wrong-vault, or swapped protected material. Use the same session/root transition as password success.

## Physical matrix

For every supported biometric platform verify:

- first enrollment from an authenticated vault;
- cold start and in-session lock/unlock;
- success, user cancel, system cancel, lockout, unavailable, not enrolled;
- password fallback, disable/re-enable, master-key rotation, reset, and restore;
- enrollment/device-credential changes and stale material recovery;
- app update/restart plus lock/background/minimize/shutdown during a prompt;
- two rapid attempts and a late callback from an obsolete operation.
