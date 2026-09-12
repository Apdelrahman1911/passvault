# PVA-010: one ordinary real-JNA lifetime boundary

Status: **source proposal; independently challenged separately; zero executions**.
Author: `/root/c20_remaining_author`. Reviewer: `/root/c20_remaining_review`.
Root alone owns integration, Git, tool/runtime admission, builds, CI and cleanup.

Proposed sole permanent source path:
`app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/biometric/JnaDesktopBiometricNativeLifetimeIntegrationTest.kt`.
The adjacent `.kt.txt` is an inert afterimage, not an installed test or helper.
No production/native/build/dependency change is proposed. The sealed remaining
work map is unchanged. This proposal does not alter any family/PVU denominator.

## Specific additional confidence

The existing two close-cancellation JVM tests use fake pointers/APIs. This case
uses the current ordinary DLL/dylib through JNA's typed `NativeApi`, a real
native context, actual ABI/create/retrieve/cancel/destroy calls, and controlled
**managed** gates. With an original empty synthetic data directory, native
retrieve returns `NOT_ENABLED` before provider activation. The observer pauses
that return and pauses close cancellation **before** native entry. When the
retrieval worker finishes, the real context must not have received a destruction
request; after cancellation enters/returns, one exported destroy must return.

This adds FFI/context/status/ordering integration evidence, not an active native
operation, protected-provider or physical-device claim. A returned void destroy
is not allocator instrumentation or a proof that every memory byte was erased.
The actual native-active-operation, sanitizer, packaged-loader/signing and
hardware gaps remain open. Existing Mac native-only checks remain distinct.

## Meaningful oracles and safe inverse

- Absent `passvault.audit.jnaLifetime.enabled` is an explicit JUnit SKIP.
  A selected value other than `1`, unsupported target/JDK, missing/mismatched
  input, wrong hash/size/name, wrong real path, actual wrong ABI or unavailable
  library fails; no selected fallback/pass/no-op.
- Before loading: target/JDK17, declared ABI1, canonical absolute library/data
  paths, empty data root, exact library size and SHA-256. Factory checks the
  **actual** exported ABI after loading and before real context creation.
- After creation and after the ordering check, the original data root contains
  only an empty `biometric` directory. No enrollment, existing metadata, vault,
  real backup, clipboard, capability probe or provider call is requested.
- Observe actual native retrieve status5 and require the exact production
  `NotEnabled` exception from the worker, not just `Future.get` completion.
  Match the real pointer and operation ID at cancellation; require status0,
  zero destroy requests while cancellation is gated, one final destruction
  request/return, no premature requests, no unreported callback failure, and
  repeated managed close idempotence.
- Only five ordinary exports are admitted by the observer. Capability, window,
  contains, enroll, delete and any unexpected export refuse before native entry.
- A deliberately broken managed inverse would request destruction while an
  observed call is pending. The observer records/fails that request but withholds
  actual destruction; it never intentionally calls a freed pointer. This is a
  source argument, **not an executed mutation/inverse result**.
- Finally release both gates, settle only the captured two-worker executor,
  then dispose a known live context once if normal managed destruction was
  withheld. Fallback cleanup never counts as a regression success. An attempted
  but unproven native destruction is not retried. Preserve the primary failure
  and interruption, with cleanup failure suppressed. Unknown settlement is
  HOLD: no explicit unload, context free, directory deletion or namespace change.
- Root must preserve compact log/XML and verify its original worker process has
  settled before allowlisted runtime/output cleanup. No deletion is in the test.
  Only the final PASS marker **plus the exact one-case passing XML and successful
  original cleanup reconciliation** can support this partial boundary. A SKIP,
  isolated cleanup marker or task success is insufficient.

## Narrow target execution: remaining admission, not a runnable receipt

Required: one supported current native JVM (`windows-x64`, `macos-x64` or
`macos-arm64`) on JDK17, existing JNA5.19.1/JUnit4/test dependencies, and a current
normal platform library with exact source/binary/architecture identity. Linux's
unavailable fallback is not this test. Prefer batching one target case with an
already necessary target compilation; do not repeat all prior native tests.

The ordinary `desktopTest --tests ...` command is **not narrow** on these hosts:
`app-desktop/build.gradle.kts:1023-1030` attaches staging and full native CTest to
every `Test`; the native build at 899-922 lacks a target and has bare `--parallel`.
Creating a named `Test` alone inherits the same hook. No broad `-x` or default
task invocation is proposed, and unchanged native suites are not new evidence.

Root's smallest prospective route, requiring separate exact source/configuration
review before execution:

1. Reuse an exact independently admitted current ordinary library if available;
   otherwise configure one fresh native build directory with `BUILD_TESTING=OFF`,
   Release, explicit host architecture/generator, and existing compiler settings.
   Build only `passvault_biometric` with `--config Release --parallel 1`. No CTest,
   fixture/control/instrumentation targets, packaging or signing. Bind the actual
   DLL/dylib path, size, SHA-256, exported ABI1 and target-machine architecture.
2. Use existing `desktopTestClasses` compilation and `desktopTest` runtime
   classpath. A new isolated audit `Test` may copy those classes/classpath, but
   its explicit dependencies must be set **after all supported-host global Test
   hooks have applied**, to compilation only. Preserve standard task behavior.
   Independently inspect/reject any resulting native-build/stage/CTest, second
   Test, packaging, publication or unrelated test dependency before execution.
   No such init/task has been authored/admitted by this proposal.
3. Invoke only the isolated task through the checked-in wrapper with JDK17,
   `--no-daemon --max-workers=1 --no-configure-on-demand`, in-process Kotlin,
   intact dependency verification, one fresh Test worker, `maxParallelForks=1`
   and only class/method
   `JnaDesktopBiometricNativeLifetimeIntegrationTest.closeReservationProtectsRealContextUntilCancellationReturns`.
   Require no-match failure, ordinary JUnit4 XML, no other Test task, and these
   exact Test-worker properties under prefix `passvault.audit.jnaLifetime.`:
   `enabled=1`, `target`, `abi=1`, `library`, `libraryBytes`, `librarySha256`,
   `dataDirectory`. Values must come from the newly admitted source/runner/run,
   never historical paths or guessed current identities.
4. Root must record the actual source commit/tree and input tuples, exact
   expanded native/wrapper/Test command and dependency graph, pinned runner/
   compiler/JDK/SDK/JNA identities, original empty private HOME/TMP/data roots,
   coordination and cleanup authority **before** execution. Suggested bounds:
   one native target plus one test, 512MiB Test JVM, one worker/CPU, two test-owned
   threads; gate/worker waits10s each and an outer one-case limit120s. Overall
   compilation budget must be admitted from the actual target environment, not
   assumed from this source-only proposal. No automatic retry on a failed/HOLD
   invocation. Root retains compact log/XML/hashes only, invokes the original
   wrapper `--stop`, verifies owned settlement and removes only admitted outputs.

If that integration cannot remain small and independently safe, retain this
source test as pending target execution and report the exact graph/admission gap;
do not build a new generic validation framework or claim PVA-010 closed.

## Source binding used for this proposal

Paths below are relative to the continuation checkout. Hashes bind the exact
read source, not execution or production/native correctness.

| Source | SHA-256 |
| --- | --- |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/security/biometric/JnaDesktopBiometricBridge.kt` | `772d17b80f6e131aeed8cf763efa59cb909eb0696f777da555382b91f6113232` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/security/biometric/DesktopBiometricNativeLoader.kt` | `58bfb35a24738dafa765216aba023371088c697c5b5024462b1e2bc09ecbe787` |
| `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/biometric/JnaDesktopBiometricBridgeTest.kt` | `dd5e2db95fd8d8e37f90710a85285001a43ed1f128a0f92d4b978d8f4f2565b8` |
| `app-desktop/build.gradle.kts` | `381ef53b5bc4ae775aa2322dbaea02588d499111d2a32e2ca3cdb2557363acde` |
| `app-desktop/native/biometric-bridge/CMakeLists.txt` | `89fa2928be4da01c17f09cc0813fbf1f284f76ecc6d05a20176389cff9903b10` |
| `app-desktop/native/biometric-bridge/include/passvault_biometric.h` | `dc76bea46e1abc0a2950f55b549fe68cf1399d256756e85ad0a79ef52b228b85` |
| `app-desktop/native/biometric-bridge/src/macos/passvault_biometric_macos.mm` | `ba07db83b6ff8b482f4fd1d40afea2d10419c454d7856a6a1eb4ba92aa5542c9` |
| `app-desktop/native/biometric-bridge/src/windows/passvault_biometric_windows.cpp` | `93746b0cdb399aa17dd8c3c915e907244d4e86e5b566c51714ccc7f80310bd5d` |
| `gradle/libs.versions.toml` | `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a` |

Native provider-free early returns: macOS retrieve812-842/cancel936-959;
Windows retrieve2050-2059/cancel2572-2600. Context create/destroy:
macOS535-603; Windows2259-2317. Existing managed fake lifetime tests:69-110.

All PVU-007 STOP, PVU-011 NO RETRY, PVA-029 retained failure/no automatic retry,
G7/G8 CLOSED and original native refusal remain unchanged. No refused provider,
allocator instrumentation, secret-array caller-cut or historical helper is
inspected, activated, imported or replayed by this case. No release candidate,
protected branch/tag, dependency, product version/identity or build1017001 moves.
