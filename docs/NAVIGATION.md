# Navigation

Last reviewed: 2026-07-28

PassVault has one live Navigation 3 host: `shared/src/commonMain/.../PassVaultApp.kt`. Serializable route keys live
in `core:navigation/NavigationKeys.kt`. Feature-local duplicate graphs and unreachable dialog/deep-link routes were
removed.

## Routes

```text
authentication
  Onboarding -> CreatePassword -> ConfirmPassword -> SecurityExplanation -> Vault
  Unlock -> Vault

vault
  Vault -> CredentialDetail
  Vault -> CredentialCreate
  CredentialDetail -> CredentialEdit

tools
  Generator
  Health

settings
  Settings -> Security
           -> Appearance
           -> Data
  Data -> Backup -> Import
                 -> Export
```

The initial route is selected from vault existence/session state. A process restart does not restore a decrypted
route or sensitive editor state; it re-evaluates vault state and requires unlock when appropriate. Manual,
background, and inactivity lock replace the stack with `Unlock`. New-vault completion replaces the authentication
stack with `Vault`.

Features emit navigation effects. The shared host performs stack changes, validates credential IDs, and clears
sensitive feature state on exit/lock. Composables do not receive a platform navigation controller.

Deep links are intentionally unsupported because accepting external credential identifiers introduces an
authentication and data-disclosure boundary that has not been designed. Dialogs and sheets are local UI state, not
fake route types.

