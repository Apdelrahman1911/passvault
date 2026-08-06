# PassVault privacy behavior

Last source review: 2026-08-03

This is a technical description of the current application, not legal advice or a claim of GDPR/CCPA/PIPEDA
certification. The repository contains no verified company, website, privacy email, or policy owner.

## Data processing in this build

PassVault has no account, cloud-sync, advertising, analytics, telemetry, crash-upload, or other application network
service. Vault content, search text, settings, generated passwords, TOTP setup data, QR enrollment payloads, and
verification codes are processed locally.

Local storage includes:

- a Room database with application-layer encrypted sensitive record payloads;
- unencrypted structural metadata such as identifiers, timestamps, types, favorite flags, row counts, and
  relationships;
- local theme, auto-lock, clipboard-expiry, and screenshot-protection preferences;
- an optional per-vault biometric-enrollment marker plus an OS-protected VEK on supported mobile platforms; and
- user-selected encrypted `.pvault` files outside the app's private storage.

An optional TOTP setup key is stored inside its Login credential's encrypted payload. Verification codes and
countdowns are generated from that key and device time and are not persisted. Mobile QR enrollment requests camera
access; Desktop enrollment reads only the image explicitly selected by the user.

The database file itself is not SQLCipher. Settings are persistent but are not represented as encrypted DataStore.
The biometric credential is device-only and does not contain the master password or KEK. The master and backup
passwords are not intentionally persisted. They exist in UI/runtime memory while an operation
needs them, and managed-runtime copies cannot be guaranteed wipeable.

## User-controlled exports and clipboard

Backups are created only after a user action and are encrypted with a separate backup password. PassVault does not
ship CSV or plaintext export. Android uses the system document picker; Desktop uses a native file dialog. Once a
backup is saved to a user-selected location, retention and sharing are controlled by the user and operating system.

Copied credentials and TOTP codes enter the operating-system clipboard and may be observable by the OS or other
software. The app
can clear its own still-current value after the configured timeout but cannot revoke content another process has
already read.

## Platform notes

Android uses `FLAG_SECURE` on sensitive windows when enabled, but operating-system or privileged capture cannot be
guaranteed impossible. Mobile biometric unlock relies on Android Keystore or Apple Keychain/LocalAuthentication;
PassVault does not receive biometric templates. Desktop biometric unlock, autofill, attachment files, production iOS
runtime support, and cloud synchronization are not shipped.

## Deletion and access

Users can view and edit records while the vault is unlocked and can create an encrypted backup. Deleting the
application through the operating system removes app-private data subject to platform backup/cache behavior.
PassVault currently has no remote copy to delete and no operator capable of recovering a forgotten master password.

The publisher must perform a legal/privacy review and add verified ownership, jurisdiction, retention, contact, and
store-disclosure information before public distribution.
