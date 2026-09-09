# PVA-038 — independent patch and regression-design review

Reviewer: `/root/storage`; application/test author: `/root/editor`.
Date: 2026-09-09 UTC. **ACCEPT — SOURCE/DESIGN ONLY.**

This review adds **no compilation, test execution, runtime reproduction, hardware
evidence, execution admission or qualified closure**. Root remains the sole
build/test/CI, cleanup, staging and publication owner. The reviewer did not edit
the application or the author's test files. Earlier coauthorship of a database
runner does not make this reviewer independent of that runner; this report is
independent only of the editor-authored PVA-038 patch/test design.

## Exact review boundary

The source base observed was commit
`f18995e6633904a6845e09eab8a481635290894d`, tree
`8948bb6c92b5baaf406f838b48bf8e31064d1195`. This is a base identity, **not a commit
containing the uncommitted patch**. Root must bind any later build to the actual
committed source/test tree and execution admission. Frozen after-images were
rehashed immediately before writing this report:

| Path (relative to repository) | SHA-256 | Bytes / LF |
| --- | --- | --- |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt` | `aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630` | 37053 /846 |
| `core/database/src/desktopTest/kotlin/com/passvault/core/database/repository/BiometricVerificationFailureIntegrationTest.kt` | `79e9d892d5aa0711facce39968ce6ce8d3a1a9dcefe47853af70e62e14168b10` | 14243 /348 |
| `core/database/src/desktopTest/kotlin/com/passvault/core/database/repository/BiometricProviderInitializationFaultIntegrationTest.kt` | `25a05900371704e67c96b3940869a8529490e0e2ca2d927c1267311418e4f0d0` | 16180 /356 |

Production preimage: SHA-256
`f2fc2ff02926e62230eff5e4723b2a68998649eee5f758f5ff99df93bcf61469`,
36754 bytes /842 LF. The full production diff, both complete new test files,
author V1/V2 notes, and the rejected-V1-to-V2 test correction were inspected.
The accompanying JSON records additional source/evidence bindings.

## Finding and reachability challenge

Root's independent finding confirmation is retained separately in
`reviews/biometric-confirmation/ROOT-PVA-038-REVIEW.md` (SHA-256
`407da44d881319017607d1c9662e937c5ad23878490bc56168117b10dbff9d9d`).
This patch review does not convert its source witness into a runtime result.

The concrete witnessed route requires supported Desktop biometrics, valid
metadata, successful protected-key retrieval, and a still-cold app crypto
engine. `LibsodiumCryptoEngine.decrypt` invokes `ensureInitialized` inside
`cryptoOperation`. Retained JVM libsodium 0.9.5 initialization calls the resource
loader's ordinary `load` method. Retained resource-loader 2.0.2 source calls
`Files.createTempDirectory` and wraps an `IOException` as
`ResourceLoaderException` (`RuntimeException`). The failed result reaches
`getOrThrow`; the old broad verifier catch mislabeled that operational failure
as a rejected biometric vault key, causing the service to attempt enrollment
deletion and report `INVALIDATED`.

Surrounding guards were challenged: opaque repository-bound attempts and lock
generation checks precede invalidation; wrong-sized keys, already-open sessions,
service/repository metadata validation, candidate-copy timing, final session
publication/last-access ordering, cancellation and cleanup still apply. The
loader witness is in an ordinary method after singleton construction, not only
a hypothesized static initializer. Logger/linkage `Error` behavior and a warm
engine are excluded. The source/POM edge is not a freshly resolved dependency
graph or binary/source-equivalence proof. Root separately traced cold-start
guards/eager viewmodels/Room and macOS protected release; this reviewer did not
newly re-read Windows native success end-to-end. Linux has no native biometric
release path here.

Impact remains bounded: **enrollment loss requires successful deletion**. This
does not establish vault-content loss, authentication bypass or premature session
publication. Password fallback remains available.

## Patch challenge and compatibility

The sole production hunk replaces an undifferentiated plaintext-size `require`
with a private `InvalidVerificationPlaintextException : IllegalArgumentException`
and maps only that marker and `CiphertextAuthenticationException` to biometric
key rejection. Plaintext wiping remains in `finally`. All other provider
exceptions, including ordinary `IllegalArgumentException`, reach the existing
outer generic failure path, so the unchanged service returns `INTERNAL_ERROR`
without deleting enrollment. Removing the old explicit cancellation rethrow is
safe at this catch boundary because neither new narrow catch matches it.

Actual wrong-key/tag authentication rejection still maps to invalidation; the
existing malformed-verification-plaintext policy is deliberately preserved.
For a real, fixed-length 52-byte verification envelope, a successful AEAD decrypt
should yield 32 bytes; a mocked 31-byte success is a defensive-policy case, not a
new production reachability witness. The password caller still handles the
private marker through its unchanged generic failure path.

Production DI uses `LibsodiumCryptoEngine`; Android/Desktop wrappers delegate to
the common engine, whose API advertises typed authentication failure and whose
adapter maps `AeadCorrupedOrTamperedDataException`. Retained common/Android AEAD
source supports that mapping. **Exact iOS/Kotlin-Native AEAD implementation and
native runtime/binary equivalence remain unverified.** No new external source
acquisition was performed for this report. A generic-tag-error counterexample in
`core/testing/.../FakeCryptoEngine.kt` was considered: that fake differs from the
typed API contract; no biometric repository/service test using it was found.
This does not justify restoring a broad catch or silently changing that fake.

No data format, AAD, envelope, KDF, password bytes/normalization, dependency,
version, identity, publication or PVD product boundary is redesigned.

## Independent rejection T01, preserved and corrected

The original routing file (SHA-256
`22a47793df143dcd9db299e04f2a83451e97011886b64da9b5245770fc78a017`,
13654 bytes /336 LF) is retained verbatim as
`reviews/editor/PVA-038-ROUTING-REJECTED-V1.kt.txt`.

Its malformed-metadata test changed the nonce to 23 bytes **before**
`service.unlock`, then expected `Locked` plus a returned key to wipe. Independent
review rejected that oracle: the first service call to `repository.getMetadata`
validates metadata and fails before a vault ID, retrieval or repository unlock.
The fresh repository remains `Uninitialized`; no key buffers exist. The author
independently challenged and confirmed this diagnosis against the exact guards.
This was a **source-discovered test defect, not an executed FAIL and not a new
PVA family**.

V2 now snapshots and requires unchanged session state, no `Unlocked`
publication/unlocked repository, enrollment retained, zero deletion/retrieval/
verifier calls and null returned/candidate keys. The synthetic store's retrieval
counter is inert. Removing the invalid optional missing-candidate branch
strengthens the other assertions: every post-release helper call now requires
both distinct buffers and their wipes. The full correction was reviewed; the
production and provider-test files remained unchanged. Author V2 expressly
withdraws V1's erroneous metadata-test wipe claim; V1 evidence is not overwritten.
No further blocking source defect was established for the frozen tuple.

## Regression inventory — declarations, not results

**Routing: nine declarations.** The real in-memory Room/Desktop crypto fixture
is warm; the OS key store is synthetic and provider injections are explicit:

1. Returned provider `RuntimeException(IOException)`: internal error/preservation,
   then real successful unlock after removing injection.
2. Thrown operational `IllegalStateException`: internal error/preservation.
3. Ordinary `IllegalArgumentException`: must not become authentication rejection.
4. Returned `CancellationException`: propagation and wipe checks; injected
   cancellation is not physical or full cancellation-race proof.
5. Mocked 31-byte plaintext: retained invalidation policy and wipes.
6. Actual wrong 32-byte key through real AEAD: typed rejection and invalidation.
7. Actual tag tamper in structurally valid metadata: rejection and invalidation.
8. Corrected T01 pre-release metadata guard, not a post-release wipe schedule.
9. Injected provider failure followed by real password fallback once cleared.

The fixture uses `NonCancellable` collector cancellation/join, repository lock,
synthetic-store disposal and the inherited fixture's Room closure. These are
source-level cleanup checks, not observed process/worker settlement. Existing
security/freshness siblings were read but not changed; the previously frozen
105-test selection does **not** verify this later patch.

**Actual provider: four declarations = one fixture producer plus three
prospective consumer regressions.** Without `PASSVAULT_PVA038_MODE`, methods
would use explicit JUnit assumptions to SKIP, not pass. No skip was executed.
Each mode requires its own separately admitted fresh JDK 17 test worker:

| Mode | Designed evidence / limitation |
| --- | --- |
| `prepare` | Real provider creates the public synthetic fixture; compact 317-character `pva038-v1:<four hex fields>` output. This is a producer, not a regression closure. |
| `success` | The same fixture's real verification and password unwrap succeed with the expected key. |
| `wrong-key` | The same fixture with a one-bit key change produces actual typed rejection, invalidation, no session/last-access, and wipes. |
| `loader-io` | Correct validated fixture, zero-byte owned `tmp-blocker` as worker-startup `java.io.tmpdir`, separate valid JNA/home roots; requires actual loader/IO cause and `ResourceLoader.createMainTempDirectory`/`Files.createTempDirectory` stack evidence. Engine stays uninitialized, result is internal error, deletion/last-access/session publication remain absent and keys are wiped. |

The fault process uses an in-memory DAO, **not Room**, and observes the real
provider; injection fields must be null. Reflection reads the initializer's
state without a dependency addition. No subprocess is launched by the test.
JUnit/reflection APIs and compilation remain unverified. Completed producer
output must bind all consumers; startup JVM arguments must be proven on the
actual worker, not merely supplied to the Gradle client. A framework failure
before the target is a failed attempt/blocker, not the required witness.

There is **no admitted command, startup-argument delivery or fresh-worker
orchestration in this source review**. Exact sources/dependencies, fresh JVM
identity, environment, original owned directories/locks, one local/CI slot,
time/RAM/output bounds, wrapper stop, worker settlement and allowlisted cleanup
need fresh root admission and independent challenge. The author's suggested
120-second worker, 512-MiB heap and 128-MiB native/output allowances are proposals,
not execution authority. Linux can establish a JVM provider/service component
route with synthetic key release, not native OS release or physical security.
Exact iOS and genuine hardware gaps remain BLOCKED where needed.

## Inspection and resource qualifications

Some initial combined displays truncated; the pertinent files/diffs were later
read completely or in focused ranges. One broad search of a single-line handoff
JSON produced oversized truncated output; no semantic/native claim relies on
it. A bounded object-only follow-up against the 6,048,018-byte evidence index
located common/Android AEAD captures, not an exact native capture. Guessed
`build-logic` and `core/crypto/src/desktopTest` paths did not exist; correct module
and test/build files were subsequently read. No project module, runner or test
was imported, compiled or executed. No runtime artifacts or background process
were created by this lane, and no shared cache/source/report was deleted.

The latest earlier resource observation in this lane was workspace free
31,485,720 KiB, `/tmp` free 21,266,972 KiB, `MemAvailable` 43,587,648 KiB of
65,855,360 KiB; these are observations, not owned-resource attribution or an
execution reservation.

## Accounting / durable limits

This report contributes **0 executed cases, 0 task results, 0 hardware tests and
0 qualified closures**. Thirteen declarations are not thirteen executed cases:
nine routing declarations, one producer and three prospective fresh-provider
consumer regressions. PVA-038 remains open pending meaningful admitted evidence
and any remaining compatibility gaps. As recorded for this review: original
confirmed **19/25 (76%), 6 open**; all confirmed **22/38 (57.9%), 16 open**;
original suspicions **2/12, 10 open**. Eight PVD explanations and owner decisions
remain separate; none is an overall readiness percentage.

PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED,
Windows01 FAIL/cleanup HOLD, archived/unadmitted-runner restrictions, protected
refs, non-publication, and occupied mobile build 1017001 remain unchanged.
