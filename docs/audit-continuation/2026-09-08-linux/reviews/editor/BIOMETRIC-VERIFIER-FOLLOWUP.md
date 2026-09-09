# NAV-FRESH-SUS-001 — bounded source follow-up

Author: `/root/editor`; independent challenge requested from `/root`.
Recorded: 2026-09-09 UTC. **PROPOSED SOURCE COUNTEREXAMPLE, NOT CONFIRMED.**
No production/test patch, new confirmed family, closure, runtime reproduction or
hardware result is claimed. The original frozen investigation remains
`UNRESOLVED / VERIFICATION BLOCKED`, outcome `UNVERIFIED / BLOCKED`.

## Starting point and source identity

This continues the first exact row of
`docs/audit-handoff/current/additional-investigations.json` (SHA-256
`dfc0be5063993d43f47fcf47669d5822e6fe0837c03ca060b1248097cbaed044`).
It does not restart or reclassify the stopped original investigations.
The row already established the broad verifier-to-enrollment-delete routing,
cancellation/metadata/session guards, and absence of a demonstrated legitimate
non-authentication exception from **initialized** production decrypt. A fake
CryptoEngine failure was explicitly insufficient. Those qualifications stand.

Inspected continuation HEAD: `f18995e6633904a6845e09eab8a481635290894d`, tree
`8948bb6c92b5baaf406f838b48bf8e31064d1195`. All four originally bound application
files are unchanged from the preserved `9bdf9559...` handoff. The engine and
CryptoEngine hashes still equal the row's old G3/G4 hashes. Repository and service
hashes differ from that older row; the newer attempt/repository/lock-generation
checks were inspected, not silently skipped. Current exact file/range bindings,
including cold-start callers, are in the companion JSON. Physical file lengths
are identities, not whole-file coverage credit.

## New bounded observation: initialization is inside decrypt's mapped operation

The cold-start case differs from an already initialized decrypt:

1. `LibsodiumCryptoEngine` starts with `initialized=false`. Its mutex-protected
   `ensureInitialized` calls `LibsodiumInitializer.initialize()` before setting
   true (24–35). `decrypt` invokes it **inside** `cryptoOperation` (154–181).
2. Exact retained JVM initializer source from libsodium-bindings 0.9.5 calls
   `SharedLibraryLoader.get().load(...)` for macOS/Windows before `Native.load`
   and `sodium_init` (retained initializer 17–53). There is no catch in that path.
3. The retained resource-loader 2.0.2 source calls `createMainTempDirectory`
   before resource/JAR handling; that calls `Files.createTempDirectory`.
   `SharedLibraryLoader.load` catches `IOException`/`URISyntaxException` and
   wraps them in `ResourceLoaderException` (46–74). The newly transport-verified
   exception class **extends RuntimeException** (14–22), not `Error`.
4. Thus a legitimate temporary-directory I/O failure is not an AEAD wrong-key
   verdict. The engine returns that `Exception` in `Result.failure` (286–305).
   `verifyVaultKey` rethrows it at `.getOrThrow()` (702–714), and
   `verifyBiometricVaultKey` converts it to `BiometricVaultKeyRejectedException`
   (719–726). No outer generic handler intervenes: the repository handles the
   typed rejection before its generic catch (350–355), and the service then
   **attempts** `keyStore.delete` and returns `INVALIDATED` (123–127).
5. Persistent enrollment loss follows only if that deletion succeeds. A
   non-cancellation deletion failure is ignored by the existing branch, so
   source alone establishes the attempt/classification, not that every device
   successfully erases enrollment. No session is published and no plaintext
   disclosure or unauthenticated unlock follows.

The fifth new text capture is the 0.9.5 JVM POM: lines46–51 declare
resource-loader 2.0.2 at runtime. The current catalog pins 0.9.5 and current
verification metadata contains both relevant components. The POM itself says
Gradle metadata is richer/preferred. This is a declared source dependency edge,
**not** a fresh resolved graph, loaded-binary identity or source/binary-equivalence
test. Existing initializer/SharedLibraryLoader excerpt captures retain their
disclosed earlier ad-hoc data-reader qualifications; no new transport claim for
those captures is made. The sixth new text read separately verifies the full
ResourceLoader source, including its constructor and I/O method boundaries.

### Independent root challenge retained: static initialization versus method I/O

Root challenged whether `SharedLibraryLoader.get()` initializes its singleton by
calling a ResourceLoader constructor that already creates the temporary directory.
An exception during class initialization could instead become
`ExceptionInInitializerError`, outside the mapped `Exception` path. This was a
material counterexample to inspect, not a reason to silently relabel all failures.

The newly read exact full ResourceLoader source has only logger/ArrayList fields
at36–40 and a permission-populating constructor at42–54. The declaration-wide
scan identifies the numeric constant at301 as its only static value field; no
static initialization calls the temp method. `createMainTempDirectory` at403–409
is invoked from `copyToTempDirectory`68. The saved SharedLibraryLoader source
calls that method inside `load`'s try after `get()` has successfully returned.
The proposed directory-creation I/O witness therefore occurs during ordinary
method execution, not during singleton construction. Logger/class-initialization
errors remain excluded. Later file-copy/permission I/O paths also exist but were
not silently substituted for a failed witness. Root's final independent
disposition is still pending; no application finding is confirmed here.

## Cold-start admission and countervailing caller guards

The proposed schedule requires an existing valid vault/enrollment and a supported,
successfully admitted Desktop native bridge. It does **not** assume an unavailable
bridge can retrieve a key, or that a failed database bootstrap reaches unlock.

- `Main` creates Koin and the shutdown coordinator; the latter resolves services
  and stores shutdown callbacks rather than invoking crypto (55–64; coordinator
  155–171). `AppModule` constructs the singleton engine without initializing it
  (67–79). `VaultKeyHierarchy` construction stores the engine only.
- `PassVaultApp` loads preferences and opens/verifies the database (47–79).
  The Desktop database factory uses Room/BundledSQLite; the successful bootstrap
  path performs storage/SQLite checks, not sodium initialization. Existing vault
  metadata routes to `AuthRoute.Unlock` (192–230).
- The navigation host eagerly resolves Vault, Settings, Backup, Onboarding and
  Unlock viewmodels (94–139); this was not treated as harmless without checking.
  Vault's initial `getAllSummaries` is guarded by `withUnlockedSession` before
  decrypt; the initial repository has no session/key and refuses the lease.
  Backup refresh reads metadata only; settings loads preferences/metadata/status;
  onboarding construction does not create a vault; Unlock probes existence.
- Generator initialization would call crypto, but its viewmodel is obtained
  inside `GeneratorEntry`, not the host's eager injections. Authentication-root
  selection sends only authentication entries to `NavDisplay` (host219–339).
  A previously used generator or earlier successful password/crypto operation
  would instead make the engine warm; that schedule is excluded here.
- Production service status/enablement checks perform metadata validation,
  reconcile/capability/contains calls, and cancellation handling (28–51,96–120).
  Desktop key-store calls hash the vault ID with Java `MessageDigest` and use the
  separately loaded biometric bridge, not this CryptoEngine (19–72,140–149).
  Bridge retrieval returns its guarded 32-byte output (85–122). The inspected
  macOS native success path copies the authenticated Keychain bytes (812–902);
  this source trace is not physical Touch ID evidence.
- The new opaque attempt, repository identity, generation, key-size and session
  guards all precede verification (repository280–359). Valid metadata, exact
  nonce/envelope sizes and supported normalized format remain prerequisites
  (741–762). Ordinary metadata/DAO/openSession failures are still outside the
  mapped helper and do not take its invalidation branch.

A narrow legitimate schedule is: finish working Room/native-bridge startup and
status checks; the engine is still cold; an I/O condition then prevents creation
of a *new* resource-loader temporary directory while existing DB/bridge/Keychain
reads still work; the OS releases the correct candidate; first verifier decrypt
attempts sodium initialization and receives the wrapped I/O exception. Temporary
storage permission/quota/I/O conditions can be independent of the already-open
database and existing protected material. This is a source counterexample to
challenge, **not** a fault injected on this VPS or a measured device schedule.

## Limits and rejected overclaims

- A warm engine skips this loader call. No new legitimate transient exception
  from an already initialized production AEAD operation was demonstrated.
- Failures before successful bootstrap/contains/retrieve cannot be moved into
  the verifier catch. Native linkage `Error`s are not this `Exception` path.
- Android's initializer bypasses the Desktop resource-loader branch. Native iOS
  initialization/decrypt source was not obtained here. This Desktop candidate
  must not be generalized to either mobile platform; Linux's biometric runtime
  is explicitly unavailable. Windows native success internals were not reread
  end-to-end here; the JVM loader/common bridge source scope is distinguished
  from the separately traced macOS return path.
- The newly read common AEAD declaration documents authentication rejection.
  Android's exact decrypt33–55 allocates ciphertext-minus-tag output, calls native
  AEAD, and maps failed validation to its AEAD exception. Fixed valid repository
  dimensions rule out negative-length input counterexamples. An undocumented
  native return code is not assumed to be a legitimate transient failure.
- `GeneralLibsodiumException` is a RuntimeException, but no legitimate supported
  `sodium_init` failure schedule is inferred merely from that declaration.
- Cancellation is separately rethrown throughout. Managed `secureWipe` is `fill`;
  it supplies no OS-provider exception and no forensic wiping proof.
- A fake CryptoEngine alone still proves only abstract routing. No application,
  native, mock or hardware test was run; no new regression cases were authored.

## Proposed disposition and next evidence

Request independent root challenge of this **Desktop cold-initialization
non-authentication failure** and its current guards before confirmation or patch.
The original row remains frozen. A narrow eventual correction would need to
distinguish authentication/format rejection from provider initialization failure
without weakening candidate verification, wrong-key invalidation, cancellation,
generation guards, secret cleanup, or existing format/password compatibility.
No design/product boundary change is proposed.

Meaningful later regression evidence would require real provider initialization
failure with fixed valid synthetic metadata/key and an independently admitted
fresh process/fixture; compare normal success, actual authentication rejection,
cancellation and metadata/open-session failure. A synthetic key-store boundary
would remain a mock of OS release, not a physical biometric pass. This report
does not authorize that execution or any retry of an earlier blocked run.

## Actual data reads, resources and diagnostics

Root separately authorized **six source-data `show` operations only** through
the independently accepted Linux Git-blob reader `f1e0c83a...`; its initial root
verify had exit0. Each invocation rehashed that exact reader, used isolated
Python `-I -B`, the explicit180s TERM/10s KILL wrapper, serial execution, and
bounded regular stdout/stderr files. Commands, times, exits and exact captures
are retained in `biometric-verifier-sources/` and bound by the companion JSON.
All six completed exit0 with empty stderr and the exact indexed hashes; no
timeout, signal/child-settlement error or retry occurred. The reviewed reader
waits for its owned Git children in finally; no separate raw process census is
claimed. Hard-kill/host-loss settlement limitations remain.

No Java/Kotlin/native application code, old runner or packed helper was
executed/imported/compiled; no dependencies downloaded, shared caches changed,
runtime storage or temporary extraction created. Gradle stop is not applicable
to these data reads. Only compact permanent source captures/reports were added.
Foreground source-navigation diagnostics included missing guessed paths/globs,
one mislocated verify receipt, and a one-line JSON-index search that exceeded
output display limits. These were read/navigation errors, not build/test failures;
no semantic proof relies on truncated content, and no failed data-reader invocation
was retried. Focused later reads supplied the cited complete ranges.

Counts remain19/25 original confirmed closures,22/37 all confirmed closures and
2/12 conclusive original suspicions. Eight PVD explanations/owner choices,
hardware gaps, STOP/NO-RETRY/CLOSED scopes, prior Windows failure/cleanup HOLD and
protected-reference/publication/candidate1017001 fences are unchanged.
