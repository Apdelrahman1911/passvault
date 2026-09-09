# PVA-038 — narrow patch and regression source freeze V1

Author `/root/editor`; 2026-09-09 UTC. **SOURCE ONLY; independent patch review
pending `/root/storage`; no execution or closure credit.** Do not treat this
note, a test declaration, an assumed skip, or the frozen handoff105 selection as
evidence that the patch works at runtime.

## Identity and scope

The independently confirmed finding is root's
`../biometric-confirmation/ROOT-PVA-038-REVIEW.md`, SHA-256
`407da44d881319017607d1c9662e937c5ad23878490bc56168117b10dbff9d9d`, JSON
`8cd66918d13af25d14bda9cfd2c113fa0b57cf69f65aaeaa5551b866fe7781d5`.
It concerns an ordinary cold Desktop loader-I/O exception, after a valid
candidate has been released, incorrectly mapped to rejected key and hence an
enrollment-delete **attempt**. Persistent enrollment loss requires successful
deletion. It is not a vault-data deletion or authentication bypass finding.

Working source base: commit `f18995e6633904a6845e09eab8a481635290894d`, tree
`8948bb6c92b5baaf406f838b48bf8e31064d1195`; re-read during this author turn.
Repository preimage SHA-256:
`f2fc2ff02926e62230eff5e4723b2a68998649eee5f758f5ff99df93bcf61469`.
The companion manifest identifies three uncommitted after-images. They alone
are the authorized production/test edit scope; no generated mirror, crypto
adapter, dependency, workflow, service, handoff105 file, or protected ref changed
by this author. Root owns committing, staging, execution, cleanup and publication.

Earlier preconfirmation proposal `BIOMETRIC-VERIFIER-FOLLOWUP.md/json` remains
unchanged, including its then-current denominator, six source-data read receipts,
POM/source-vs-runtime qualification and excluded counterexamples. The original
`NAV-FRESH-SUS-001` handoff row is historical; this patch does not rewrite it.

## Production contract

Only `VaultRepositoryImpl.kt` changes, at its verification helpers:

1. The explicit decrypted-value size check still rejects a non-32-byte value and
   still wipes that returned buffer in `finally`. It now throws a private
   `InvalidVerificationPlaintextException` instead of an indistinguishable
   general `require` exception. It remains an `IllegalArgumentException` subtype.
2. Biometric verification converts only that marker and the existing
   `CiphertextAuthenticationException` into `BiometricVaultKeyRejectedException`.
   In particular, ordinary provider `IllegalArgumentException` is not treated as
   authenticated rejection merely because the typed auth exception shares its
   superclass. Loader/provider exceptions propagate to the existing generic
   unlock failure, which the unchanged service maps to `INTERNAL_ERROR` without
   attempting enrollment deletion.
3. Cancellation no longer needs a catch/rethrow in this helper: neither narrow
   catch matches it. The outer repository and service cancellation handling is
   untouched. Candidate-copy, verification-plaintext and returned-key wipes stay
   at their existing ownership boundaries.
4. Repository identity, lock generation/preemption, wrong-sized candidate,
   already-open session, metadata/envelope validation, DAO errors and
   last-access/session publication guards are unchanged. Metadata rejection
   remains outside the invalidating helper. Password unlock still catches the
   private malformed marker through its unchanged generic failure path; no
   password/KDF bytes, envelope, AAD, dimensions, failed-attempt accounting or
   on-disk format change is introduced.

This deliberately preserves the prior malformed-plaintext policy, even though
the real fixed-length authenticated envelope should decrypt to exactly 32 bytes.
It is not a new reachable malformed-AEAD finding or a widened PVD design decision.

The shared CryptoEngine contract and current shipped Libsodium mapper type real
AEAD rejection. The exact Kotlin/Native iOS AEAD actual source was **not** acquired
by the earlier proposal; no new independent iOS actual/binary/runtime claim is
made here. Original mobile/native compatibility and physical-device gaps remain.

## Permanent regression declarations (not executed cases)

`BiometricVerificationFailureIntegrationTest.kt` contains **9** new declarations.
Its unchanged parent fixture supplies real in-memory Room and real Desktop
crypto, but creation/enrollment warms the provider. The recording adapter passes
real decrypt through unless an explicitly named routing injection is set. The
protected-key store is synthetic, not an OS biometric implementation.

| Declaration purpose | Intended oracle / boundary |
| --- | --- |
| Returned provider `RuntimeException(IOException)` | INTERNAL_ERROR, no deletion/session, unchanged last access, received/candidate wipe, later actual verified success |
| Thrown provider `IllegalStateException` | INTERNAL_ERROR, no deletion/session, received/candidate wipe |
| General provider `IllegalArgumentException` | Must not be confused with its typed-auth subclass |
| Cancellation delivered as `Result.failure` | Same cancellation propagates; enrollment retained; keys wiped |
| Synthetic 31-byte successful verifier result | Explicit retained invalidation policy and plaintext/key wipes; defensive mock, not real AEAD evidence |
| Different 32-byte enrolled key | Actual AEAD typed rejection, INVALIDATED/delete attempt, no session, wiped arrays |
| One-bit verification-tag tamper | Actual AEAD typed rejection through structurally valid metadata; same fail-closed oracles |
| 23-byte metadata nonce | No verifier call/candidate copy, generic failure, no deletion, OS-returned key wiped |
| Provider failure then password fallback | After removing injection, real password unlock succeeds and enrollment survives |

Collectors inspect publication history, not only final `isUnlocked`. The new
repository is locked and synthetic store disposed in finalization before the
inherited Room teardown; collector settlement uses a non-cancellable cleanup
context. This is source design, not a new runtime cleanup attestation. None of
the injected failures demonstrates a real cold-provider exception.

`BiometricProviderInitializationFaultIntegrationTest.kt` contains **4** new
declarations: **one fixture producer and three consumer checks**. Thus there are
13 declarations overall, zero newly executed tests, zero new task completions,
zero mock/runtime/native/hardware passes and zero earned coverage/closure credit.
No invocation occurred even to establish actual SKIPPED counts.

## Actual-provider fault design and unfilled admission

No subprocess, helper, download, native operation or Gradle invocation is launched
by this author. The prospective dedicated worker design is intentionally not
part of the ordinary frozen105 selection. With no `PASSVAULT_PVA038_MODE`, JUnit
reports an explicit assumption skip; **SKIP is not PASS**. With opt-in set but
the wrong method selected, the test fails rather than silently skipping a
misconfigured phase.

The four method/mode pairs in the source are:

1. `prepare public synthetic fixture in a separate provider process` / `prepare`:
   real provider derives a test KEK (2 operations, 32 MiB, fixed public test salt
   and password), wraps the fixed public 32-byte test key, and authenticates a
   fixed 32-byte verification plaintext with the unchanged `verification` AAD.
   It emits one compact `PVA038_FIXTURE=pva038-v1:...` line containing only four
   ciphertext/nonce hex fields, never a real application export. Buffers clear.
2. `fresh real provider authenticates the fixture and retains password fallback`
   / `success`: consumes that **same** 317-character fixture in a fresh JVM;
   verifies real biometric-repository success, then real password unwrap with
   the exact expected key. This is the mandatory authenticated fixture control.
3. `fresh real provider rejects a wrong key with typed authentication failure`
   / `wrong-key`: same fixture/new JVM, one-bit-changed synthetic enrolled key;
   requires actual `CiphertextAuthenticationException`, invalidation, no
   publication or last-access write and key wipes.
4. `actual cold resource loader IO failure keeps a valid enrollment` / `loader-io`:
   same fixture/new JVM and correct key; `java.io.tmpdir` is an owned zero-byte
   regular-file blocker. Requires actual
   `com.goterl.resourceloader.ResourceLoaderException` with an `IOException`
   cause whose stack includes `ResourceLoader.createMainTempDirectory` and
   `java.nio.file.Files.createTempDirectory`. It must leave provider initialization
   false, return INTERNAL_ERROR, retain enrollment with zero delete calls, never
   publish/write last access, and wipe transferred keys. Wrong exception, native
   linkage Error, early harness failure or successful initialization is not PASS.

The consumer uses a precomputed in-memory metadata DAO rather than Room, so
SQLite native extraction cannot intercept the intended fault. It uses real
`DesktopCryptoEngine` through the recording delegate with injection fields unset.
It observes `LibsodiumInitializer.isInitialized` reflectively before any target
operation and rejects a warm provider. That is source/runtime API coupling to
verify, not asserted binary/source equivalence. No dependency is added: the
existing desktop Kotlin test/JUnit stack must still be compilation-verified.

### Requirements before any future execution

- Root and an independent reviewer must separately admit **exact source identity,
  dependency graph/binaries, test selection, commands, worker JVM arguments,
  coordination slot, wall/resource bounds, process ownership/settlement and
  cleanup**. Nothing in this note admits a run or reopens any old restriction.
- Use checked-in wrapper/JDK 17, one worker, non-daemon, configure-on-demand
  disabled, dependency verification intact. One audit-owned build/test job at a
  time across local and CI. Do not treat Gradle-client `-D` as proof those values
  reached a test JVM; the test checks actual startup arguments and properties.
- Each method requires its own fresh worker and exact method filter. Bind
  prepare's complete successfully finalized output hash to all three consumer
  receipts. A fault result without the successful authenticated fixture control
  and real wrong-key control is incomplete, even if the fault method passed.
- Independently admit one unique `pva038-*` instance root per worker with owned,
  empty `home`, `jna`, `tmp` directories; fault uses `tmp-blocker`. The tests check
  absolute normalized real paths, final file types, and exact `-Duser.home`,
  `-Djna.tmpdir`, `-Djava.io.tmpdir` arguments. These are supplementary guards,
  not proof of ownership, launch coordination, complete environmental isolation
  or race-resistant lifecycle cleanup. A separately valid JNA temp root avoids
  deliberately substituting its static-initializer failure for the finding.
- Proposed upper bounds for later review (not current authority): 120 seconds
  per worker, 512 MiB Java heap, 128 MiB native allocation allowance for controls,
  128 MiB instance output allowance, compact capped stdout/XML. Compilation's
  independent allowance still needs admission. A normal or wrong-key unsupported
  platform/native failure must be recorded; do not retell it as provider proof or
  automatically retry unchanged checks.
- Preserve source/dependency hashes, exact worker command/arguments, compact
  logs/XML and the synthetic fixture; wait for owned JVM settlement before any
  native-output cleanup. Run the appropriate wrapper `--stop` in admitted cleanup.
  Delete only instance-generated allowlisted native/temp/build outputs after
  identity checks. No shared cache, SDK/toolchain, source, permanent regression,
  report, vault, personal home or unrelated process is cleanup authority.
- Invalid startup temp may prevent a framework/worker from reaching the test.
  That is an operational blocker/failed attempt, not this concrete witness.
  No harness/sandbox/CLI recipe has been admitted or trialed here; no old runner
  is to be executed/imported to fill this gap.

Linux execution could cover the shared JVM loader/service boundary with a
synthetic key-release store, not a supported Linux biometric feature. Mac/Windows
real protected-key reachability remains separately source-qualified, and hosted
or mocked results cannot replace physical Touch ID/Hello/iPhone security evidence.

## Inspection and accounting qualifications

This source turn used file navigation, scoped Git source/identity reads, hashes,
LF/declaration/line-width inspection and bounded writes only. A guessed standalone
CryptoEnvelope filename and a nonexistent desktop crypto-test directory produced
navigation diagnostics; the correct `CryptoEngine.kt` definition was then read.
One early combined display was truncated; cited guard/fixture portions were
subsequently displayed in focused reads. No semantic claim relies on missing or
truncated output. Source line widths are not compilation/Detekt evidence.

The two new test sources total 29,834 bytes at V1; they and compact permanent
reports are retained for upcoming verification. No build, test, fixture, daemon,
worker, temporary native output or cache was created by this author. Root retains
resource/process coordination. No author cleanup obligation was created by these
source writes. Fatal interruption/host-loss limitations of later native execution
are unresolved, not promised away by a test `finally`.

Accounting remains **19/25 original confirmed qualified closures (76%),6 open**;
**22/38 all confirmed qualified closures (57.9%),16 open**; **2/12 original
suspicions resolved,10 open**. Eight PVD explanations and owner decisions stay
separate. PVA-038 is confirmed with a proposed source fix, **not qualified closed**.
PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED,
Windows01 FAIL/cleanup HOLD, protected refs/tags, signing/publication, dependencies,
identities, versions and occupied mobile1017001 restrictions remain unchanged.
