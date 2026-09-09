# PVA-038 — independent finding confirmation, not runtime verification

Reviewer `/root`; finding author `/root/editor`. 2026-09-09 UTC.
**CONFIRMED SOURCE DEFECT: cold Desktop crypto initialization failure is
misclassified as a rejected biometric key, attempting enrollment deletion.**
No patch or runtime reproduction is accepted by this finding review.

This continues additional investigation `NAV-FRESH-SUS-001`, not any stopped
scope or one of the original twelve PVUs. The frozen handoff row remains intact.
Author follow-up: `../editor/BIOMETRIC-VERIFIER-FOLLOWUP.md`, SHA-256
`a66061a860213b33d25a91585d4bed1e940bb992013cb76acd966f3fd6839362`, and JSON
`4cce4fba3ecefb24a7b3967c2d8e87f7c14eae1fbf46b653e4aec188daa490b5`.
Exact independently rehashed source/report bindings accompany this review.

## Reachability and attempted disproof

1. The current repository admits a valid, current, repository-bound biometric
   attempt while locked; validates metadata, key and envelope dimensions; then
   calls `verifyBiometricVaultKey` before publishing a session. The valid-key
   verification decrypt calls `ensureInitialized` inside `cryptoOperation`.
   A non-cancellation `Exception` is returned as failure, not authenticated data.
2. The retained JVM 0.9.5 initializer invokes `SharedLibraryLoader.get().load`
   on supported macOS/Windows. The retained 2.0.2 loader calls
   `copyToTempDirectory` inside its ordinary `load` method's `try`; that reaches
   `Files.createTempDirectory`. An I/O failure is wrapped as
   `ResourceLoaderException extends RuntimeException`. No authentication has
   occurred at that point. The POM declares this dependency edge; it is not a
   freshly resolved/loaded binary graph or source/binary equivalence proof.
3. I challenged **static-initializer wrapping**: if the same I/O occurred while
   initializing the singleton, `ExceptionInInitializerError` could bypass this
   catch. Full retained ResourceLoader constructor/declaration inspection shows
   only logger/collections/permissions initialization there. The witnessed I/O
   is an ordinary method call after `get()` returns. This specific counterexample
   therefore does not defeat the witness. Other linkage/logger `Error`s remain
   excluded; no claim about all possible initializer failures follows.
4. I challenged **eager initialization**: Desktop DI only constructs the engine;
   startup Room/bootstrap and metadata/status reads do not initialize sodium.
   The five eagerly resolved viewmodels do not force crypto while locked:
   Vault's initial read is lease-guarded, Backup/Settings/Unlock read metadata or
   preferences, and Onboarding construction does not create a vault. Generator
   resolution is inside its authenticated entry, not the eager host injections.
   Prior successful crypto use would warm the engine and is excluded.
5. I challenged **platform-store ordering**: the independently loaded Desktop
   bridge and Java vault-ID hash do not use the application CryptoEngine. A
   successful valid Keychain/native retrieval can precede first sodium use.
   The macOS native return path was traced; this is not a physical Touch ID
   result. Windows shares the JVM/service route, but its native success internals
   were not newly reread end to end for this finding. Linux biometrics are
   unavailable, and no Linux real-provider biometric reachability is claimed.
6. A narrow legitimate schedule is successful database/bridge startup followed
   by inability to create a new resource-loader temporary directory, while the
   existing database and protected enrollment remain readable. The correct key
   is retrieved; first verifier decrypt fails in loader I/O. This does not assume
   that failed startup, failed retrieval, malformed metadata, or an already-warm
   AEAD operation reaches the same branch.

## Fault and impact

`verifyVaultKey(...).getOrThrow()` propagates that non-authentication failure.
The broad verifier `catch (Exception)` converts it to
`BiometricVaultKeyRejectedException`; the repository preserves that typed failure,
and `DefaultBiometricUnlockService` consequently **attempts** `keyStore.delete`
and returns `INVALIDATED`. That contradicts the verifier's authentication-only
invalidation contract. Existing metadata/DAO/open-session errors are outside
this helper and correctly follow different routing.

Persistent enrollment loss requires successful deletion. A failed deletion is
not proof of loss; no database/password/vault contents are erased by this chain,
no invalid candidate is accepted, and no unlocked session is published. Password
fallback remains. This is an avoidable availability/convenience-data defect,
not a biometric bypass or justification for redesigning PVD boundaries.

## Narrow corrective contract and remaining evidence

- Preserve all candidate, repository/generation, cancellation, session and wipe
  guards. Invalidation must distinguish explicit authentication/verification-
  format rejection from initialization/provider failure. A malformed decrypted
  verification value needs an explicit retained policy, not a blanket catch.
- Do not change dependencies, identities, versions, encryption envelopes, or
  lowercase ASCII-hex password compatibility. No biometric auto-enrollment or
  weakened fallback/verification is authorized.
- Independently review the patch and permanent regression oracles. Mocked
  routing tests can supplement, not replace, an admitted actual provider-
  initialization failure witness with valid synthetic metadata/key, normal
  success and real wrong-key rejection. Bind that run to its actual dependency
  graph and fresh process. No such invocation is admitted here.
- Physical OS key-release/Hello/iPhone security gaps stay separate and BLOCKED.
  The frozen handoff105-method selection does not test a later PVA-038 patch.

No new application, mock, native or hardware case executed in this review.
No semantic whole-file/LF coverage credit is inferred from hashes or author
range inventories. Earlier ad-hoc loader-excerpt transport qualifications and
six later permitted source-data reads remain distinct. Root's later source
navigation included missing guessed service paths and overlong display output;
the correct exact service was subsequently read in full. These were inspection
diagnostics, not test failures or evidence supplied by truncated output.

## Accounting and safety

This independently confirmed additional family becomes **PVA-038**. Qualified
closures remain22, so all-confirmed accounting is now **22/38 (57.9%),16 open**.
Original confirmed **19/25 (76%),6 open**, original suspicions **2/12,10 open**, and
eight PVD explanations/owner decisions are unchanged. No overall readiness claim.

All PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED,
Windows01 FAIL/cleanup HOLD, physical-device, protected-ref, signing/publication
and occupied1017001 restrictions remain. No private storage, temporary fixture,
worker, SDK/cache mutation, build or application execution was used here.
