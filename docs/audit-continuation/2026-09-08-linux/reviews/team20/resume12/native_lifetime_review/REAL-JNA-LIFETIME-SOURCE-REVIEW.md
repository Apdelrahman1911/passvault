# PVA-010 ordinary real-JNA lifetime: independent source review

Reviewer: `/root/c20_remaining_review`; author: `/root/c20_remaining_author`.
Disposition: **ACCEPT_OPT_IN_SOURCE_ONLY; EXECUTION_NOT_ADMITTED**.
This review performed source reads/comparisons only: zero builds, tests, native
loads, helper imports, probes, network/Git operations or cleanup. Root remains
the sole integration, execution, coordination, publication and cleanup owner.

## Exact accepted inputs

Paths are relative to `reviews/team20/resume12/native_lifetime_author/`:

| Input | SHA-256 | Bytes / LF |
| --- | --- | --- |
| `JnaDesktopBiometricNativeLifetimeIntegrationTest.kt.txt` | `4a604009911522d2ea952b37c44a3ec19724e146b2eb781016137065bbbdedf6` | 18594 / 393 |
| `PROPOSAL.md` | `c1495980db1abf4954d58c8bee2a0b2eb00ad8d190e67fcce3a2b86f03f67ee2` | 11180 / 162 |

The proposed sole permanent path is
`app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/biometric/JnaDesktopBiometricNativeLifetimeIntegrationTest.kt`.
This acceptance does not state that root installed it. No production, native,
build, dependency, signing, release or application-identity edit is proposed.
All nine source hashes in the proposal's source-binding table matched the
reviewed current files. Current Windows anchors are retrieve2061,
create2268/destroy2300, localized-retrieve2492 and cancel2583; line hints in the
proposal predate adjacent source movement. The hash-bound paths are authoritative.

## Independent challenges and disposition

V1 was not accepted. Its exact source `37cab85b4d84af853011b375514071f2daeff5fe2365b2627e669a5cce766704`
and proposal `78c9df4929895a0585d529c6b0f2f180cd2608afcefd9c2192de473c342b9cbd`
remain byte-identical under the author's `v1/`. One bounded correction round
resolved all three material source objections:

1. Cleanup uncertainty now strongly retains the **available** actual proxy,
   library, observer and executor for the fresh Test worker's lifetime; local
   references alone were not a defensible no-GC-unload promise.
2. Target-load attempt and successful proxy capture are recorded separately.
   An attempted load without a captured actual handle produces HOLD, never the
   cleanup-settled marker. Inaccessible residue remains an outer-owner duty;
   no automatic destruction or unload retry was added.
3. Actual JNA pointer and `size_t` widths must each be eight bytes after input
   admission and before target-library loading, supplementing target strings.

## Reachability, counterexample and compatibility

- The production close reservation occurs under the lifecycle lock before
  cancellation reaches its first native instruction. The proposed managed
  pre-entry gate therefore targets the original race, not a made-up provider
  delay. Retrieval must produce the exact production `NotEnabled` singleton;
  normal cancellation/status, pointer/operation identity, zero early destroy
  requests, one returned final destruction and close idempotence are explicit.
- Both current native implementations return NOT_ENABLED on absent synthetic
  metadata before native operation/provider activation. Creation touches only
  the newly admitted synthetic root. The observer allows only ABI/create/
  retrieve/cancel/destroy; unexpected exports refuse before delegation.
- A broken reservation would record an early destruction request and fail the
  oracle. Withholding that unsafe native free protects the deliberate inverse;
  it does not hide the request or convert fallback disposal into success. This
  is a source counterexample argument, **not** an executed mutation result.
- Finally releases both gates and settles only the captured executor before
  context cleanup/unload. Attempted-but-unproved destruction and uncertain
  native calls remain HOLD without retry. Primary failure and interruption are
  retained. The test deletes no directory; external process termination,
  wrapper stop and allowlisted output reconciliation remain root obligations.
- Default unselected execution explicitly skips. A selected invalid/missing
  input fails; passing task status or a standalone marker is not one passing
  non-skipped JUnit case. JDK17 and existing JNA/JUnit are unchanged. Compilation
  against the exact JNA5.19.1 Java getter mapping and affected static qualification
  remain pending; neither a compiler nor Detekt was run for this review.

## Confidence per cost and smallest remaining scope

This adds **modest but meaningful** combined real-FFI/context/status/pre-entry
ordering integration beyond the two fake lifetime cases and native-only busy/
destroy checks. Existing `DesktopBiometricNativeLoaderTest` also contains a real
loader/ABI smoke path (hash `c9c01d06aaa03ca79d7c364a2e395835a3d8bcee638e4558e8e20247bee1d20c`);
this is not a claim to be the first real-JNA load test or evidence that that
conditionally selected smoke path previously ran.

Accept the opt-in permanent source for an already-necessary affected Desktop
compilation. Do **not** justify a new generic harness or broad expensive matrix
with this 393-line one-case fixture. Target execution is worthwhile only when
one ordinary library and one exact JUnit case can be safely batched. The current
native-host `Test` hook adds staging/full CTest, and the native build is untargeted
with bare `--parallel`; a normal filtered `desktopTest` or naive new Test task is
not narrow. The proposal correctly leaves isolated compile-only task wiring,
the final graph, commands, source/binary/architecture identities, resource bounds,
fresh private runtime and independent execution/cleanup admission **unwritten
and unadmitted**. No blanket task exclusions or prior-suite replay is accepted.

One passing future case could support only this partial pre-entry boundary.
Native-active-call concurrency, sanitizer, packaged-loader/signature/lifecycle,
provider cancellation, hardware behavior and PVU-009 latency remain separate.
No full PVA-010 closure, XML/pass increase or denominator change follows here.
Current remaining-map seals stay untouched. PVU-007 STOP, PVU-011 NO RETRY,
PVA-029's 49/44PASS/5FAIL/no automatic retry, G7/G8 CLOSED and original native
refusal remain binding; no allocator/caller-cut/protected-provider procedure is
admitted or reformulated by this source acceptance.
