# One editor → Room integration case: source feasibility

Author `/root/android32`, 2026-09-10. **Viable, not authored or executed.** No
Git/build/probe, application storage, clipboard, Koin or old runtime accessed.
Root retains execution/publication ownership; independent challenge is pending.

## Concrete reuse and dependency boundary

- `shared/.../CredentialEditorRenderingTest.kt` already drives the production
  `CredentialEditScreen`/`CredentialViewModel` with AWT input and fresh
  `ViewModelStore`s. Its private `Editor` fixes persistence to
  `FakeCredentialRepository`; retain the three passed GUI02 methods unchanged.
  One new method can reuse that driver with a narrowly supplied real backend,
  plus an exact-cardinality selector for the last of 50 Edit buttons.
- `RepositorySecurityIntegrationTest.kt:2146+` supplies the wiring:
  `DesktopCryptoEngine`, `VaultRepositoryImpl(dao, crypto, VaultKeyHierarchy)`,
  `CredentialRepositoryImpl` with five real DAOs/session, and
  `FolderRepositoryImpl`. Its fixture is **in-memory and in another module's
  test source**, not a reusable shared dependency; reproduce only this small
  setup. Use synthetic master password/credential, no attachments or biometrics.
- Directly copying its `BundledSQLiteDriver` builder into shared is not justified:
  `core:database` declares SQLite as `implementation`, not exported API. Instead
  existing public Desktop **`createDatabaseBootstrap()`** provides real Room,
  bundled SQLite, migrations and IO dispatcher without a new dependency/product
  seam. `shared` already has API dependencies on database/crypto. The builder
  hardcodes `user.home/.passvault/vault.db`; it has no public explicit-path option.

## Mandatory isolated caller prerequisite

Before calling that factory, require a newly admitted, dedicated test-JVM
`user.home` matching an explicit fixture-parent receipt: canonical no-link,
owned/private directory, fresh nonce identity, absent `.passvault`. Root must
bind original parent identity and cleanup authority. No developer/default home
fallback, in-process property switching or application DI. This is a **fresh
instance prerequisite**, not inherited GUI02 authority. Never call preservation,
reset, corruption or recovery helpers; STOP/NO-RETRY/G7/G8 remain unchanged.

## Exactly one materially new case

1. Open/verify the synthetic database, create/unlock its synthetic vault, seed
   49 fields through the real repository; clear temporary sensitive values.
2. Native Add creates field50. Native Edit changes its name/value/secret tuple;
   leave that row draft pending. Click **page Save**, not row Save; await the
   real save-completion state, not task0 or a fixed sleep.
3. Uncompose/clear the old owner and **join its jobs off the EDT**, lock its
   session and require `checkpointAndClose()` success. Open a **new bootstrap,
   session/repositories and VM** against the same synthetic disk file; unlock
   with the synthetic password. No reused Room connection or reseeding.
4. Assert all50 `(id,name,value,isSecret)` tuples and unchanged49 survivors,
   empty drafts/clean loaded state, disabled Add and rendered name/masking.
   This covers production encrypted serialization/DAO/disk reopen missing from
   GUI02; count50 also reaches the real repository's capacity validation.

Finally clear/join owners, lock, checkpoint/close and preserve compact results;
only independently admitted settled cleanup may remove that new synthetic
bundle/parent. Failure or uncertain settlement is HOLD, not recursive deletion.
Select only the new method in a fresh reviewed run: no49/GUI02 unchanged rerun.

**Limits:** successful-save integration only; not backend rejection/races,
full NavHost dirty Back/tab/forward timing, screen-reader/IME/key-repeat/RTL,
Android predictive Back, iOS gestures or arbitrary owner-disposal retention.
One new case would not close PVA-007/031 or change any denominator by itself.

Observed source SHA256 (no new commit identity claimed): GUI fixture
`6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a`;
Desktop builder `2aa4d34c71de4dae66a79e6d93af187a44642e4a504ff2a592d82c60245f39a7`;
repository test `4ab2395754910b118e9f56b11044fed7f316caa6b597fcb590d10a39ac70d9df`.
