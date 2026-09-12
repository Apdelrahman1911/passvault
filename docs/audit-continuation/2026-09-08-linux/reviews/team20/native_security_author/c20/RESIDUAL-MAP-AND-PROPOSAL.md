# C20 native security: residual map and narrow known-answer proposal

Author: `/root/native_security_author`; independent challenge requested from
`/root/desktop_other_review`; 2026-09-11.

**SOURCE / RETAINED-DATA ONLY. No confirmed new defect, product patch, test
execution, closure, runner, request or execution admission.** Root supplied the
C19 checkpoint identity; this author performed no Git operation. All paths below
are relative to W unless prefixed by B, where B is
`docs/audit-continuation/2026-09-08-linux`.

## Reuse, not replay

- Mac02 run34430921453 retains five independently qualified CTests: two
  ABI/security cases and three real-filesystem fixture lifecycle controls.
  Foundation/LAContext properties and synthetic native busy/destroy schedules
  are useful native evidence, not actual protected Keychain/Touch ID operation,
  displayed prompt language, a real JNA-to-native race or packaged termination.
  Original normal cleanup and helper exit0 retain the exact review's cooperative
  closed-workload/original-handle qualifications; older failures/HOLDs survive.
- Windows05 run34439762763 retains 24 qualified events: eight current real-Win32
  writers, eight exact historical writer controls (three observed old defects
  and five valid controls), four live-array guard units, two real-CNG PRK cases,
  one linked-DLL ABI case and one native security/locale/crypto case. These are
  not 24 newly authored tests, hardware tests or family closures.
- Windows05 helper exit1 / whole-generated-root **filesystem HOLD** remains.
  Original Job termination followed by observed zero is not normal final drain
  or identification of its remaining member. Earlier case-local fixture teardown
  does not cure the later no-follow reparse cleanup refusal. The target was not
  read, followed or deleted; its content/creator/benignity remain unknown.
- Current Windows implementation, security test and CMake bytes independently
  match the three hashes recorded by the Windows05 actual review (table below).
  No unchanged case sequence needs replay merely to restate these results.

Actual reviews, read for substantive cases, scope, settlement and limitations:

| B-relative path | SHA-256 |
| --- | --- |
| `reviews/native-independent/MACOS-FOCUSED-02-ACTUAL-REVIEW.json` | `73aef6e8b91a225594a378e812785c2e162f961ed62fde0be547516eae0b8285` |
| `reviews/native-independent/WINDOWS-COHORT-05-ACTUAL-REVIEW.json` | `b8432123f1dcef6149c172df901a7c03f4214a732003e185bd8f39990a107116` |

This is reuse of independently adjudicated retained results, not a fresh
recollection or independent re-execution of their complete raw evidence chains.

## Actionable software evidence versus external/custody requirements

| Scope | Still missing; correct classification | C20 disposition |
| --- | --- | --- |
| PVA-010 | Real JNA-to-C/C++ in-flight lifetime/concurrency and sanitizer evidence; exact packaged-image lifecycle. Some are software/target gaps, not inherently physical-biometric gaps. Mac02's native synthetic busy/destroy cases and the prior two fake-NativeApi JVM cases cover different boundaries. | No new source defect found in the bounded current reservation/drain trace. Reuse existing evidence. No replacement lifetime harness or native execution proposed. |
| PVA-014 | Actual selected-language prompt rendering/focus/accessibility on Android/Apple targets. Windows05 proves additive ABI/reason validation, not an additional app-rendered Windows language defect or displayed Hello prompt. | External target/interactive evidence remains. No localization patch or unchanged reason/ABI replay. |
| PVA-036 | The normal-path eight current cases and eight historical controls are already verified. The normative residual separately asks for safe synthetic TMP/TEMP crash/cancellation/cleanup evidence with exact identities. | Fresh crash/cancellation evidence is a genuine software-lifecycle gap, not proof that the old held root can be recovered. No crash fixture, cleanup redesign, replay or old-host inspection proposed here. Original unavailable cleanup custody cannot be backfilled by a new success. |
| PVA-037 | A fixed known-output KDF compatibility check is absent. Existing normal PRK evidence checks changed/nonzero output; envelope roundtrip derives on both sides. Post-PRF AAD and post-KDF AES/later caller PRF/wrapping-array production cuts also remain unexecuted. | Only the distinct fixed known-answer source proposal below is selected for challenge. No allocator-hook extension, fake-PRF provider seam, caller-cut harness or production refactor. Four guard units and the two actual PRK cases retain their narrow completed credit. |
| PVU-008 | Exact applicable provider-object **content** lifetime contract or separately admitted CNG-only destroy-status/resource comparison. Live backing allocation already disproves freed-before-destroy; correct digest/ciphertext does not establish destruction status. | Still unresolved, not a confirmed leak/crash/undefined behavior. Reuse the completed bounded contract search and independent correction; no repeated search, speculative ordering patch or new provider experiment. |
| PVU-009 | Actual target-native prompt/cancellation latency with timestamps and ownership/containment. Source establishes the conditional uncovered cancellation boundary, not an observed native stall. | No new hang claim or timeout patch. A fake latch would only restate conditional ordering and is not selected as a substitute for target evidence. |
| Packaged/native source boundary | Actual target architecture, trusted installed loader path, signature/integrity enforcement and full packaged lifecycle are not established by an unsigned test DLL/dylib or a loader-helper fake. | Preserve package/signing/host prerequisites. No development-path substitution, signature bypass, production signing or release action. |
| Physical provider/device | Genuine protected Hello PRF creation/assertion/unwrap, Keychain/Touch ID, enrollment changes, invalidation, lockout, cancellation and lifecycle during a real prompt. | External authorized synthetic target/device access remains necessary; compilation, fixed vectors and synthetic provider structs cannot close it. |
| Prior held-host cleanup | Historical missing ownership/final-close/deletion proof on inaccessible original hosts, plus every consumed failed/refused run and its original identity. | Not a new application test and not repairable by renaming or recreating a namespace. No old runtime traversal, cleanup, retry or discharge claim. |

The native-author cybersecurity refusal and two partial-edit/root-completion
provenance in `B/reviews/native/WINDOWS-COHORT-05-SCOPE.md:9-12` remain unchanged.
This proposal does not resume or relabel that refusal, extend the allocator
instrumentation, or reformulate a held/consumed run into another platform harness.

## Smallest distinct candidate: fixed KDF known answer, existing target only

**A proposal, not an accepted patch. The golden bytes and their independent
derivation are NOT_SUPPLIED / NOT_COMPUTED in this source-only turn.** No oracle
generator, placeholder source patch, helper, workflow or runnable request is
created. Root must first decide whether to release this bounded preparation;
independent review must reject an oracle derived by the implementation under test.

### Source reason

`passvault_biometric_windows.cpp:272-296` computes:

1. `PRK = HMAC-SHA256(key = salt[32], message = PRF[32])`;
2. `output = HMAC-SHA256(key = PRK, message = domain || vault_hash[32] || 0x01)`.

The domain is the literal 45 ASCII bytes
`PassVault Windows Hello vault-key wrapping v1`, excluding its C-string NUL;
the second message is 78 bytes. The PRK guard remains in normal production use.
This is the current application-specific one-block derivation, not an assertion
that an arbitrary published HKDF vector automatically fits these fixed inputs.

The current security test at763-782 derives both wrapping keys through the same
function before checking encrypt/decrypt roundtrip. At254, the separate normal
PRK control only checks changed/nonzero output. Both useful controls can pass
without freezing the exact compatibility bytes. No current-source KDF defect is
asserted by this missing oracle.

### Prospective two-path delta; no production edit

Only after root release and exact independent challenge:

- Add one normal-current-target-only function and one early selector to
  `app-desktop/native/biometric-bridge/tests/windows/passvault_biometric_windows_security_test.cpp`.
  The selector would be exactly `--kdf-known-answer` with argc2, placed before
  the existing argc1/random-suffix/temp/context route. Keep PRK-only and historical
  dispatch branches closed to it. Do not call enroll/retrieve, WebAuthn inventory,
  random generation, filesystem/context setup, AES, or provider destroy-status
  instrumentation. The only native primitive calls are the existing real-CNG
  HMAC path inside `derive_wrapping_key`; this is not a provider-free test.
- Register only `passvault_biometric_windows_kdf_known_answer` in existing
  `CMakeLists.txt`, invoking the existing normal security executable with that
  selector. No new executable, platform harness, CMake option, macro, SDK,
  dependency or compiler/linker/hardening change. The old selectors and
  historical/PRK opt-in behavior must remain unchanged.
- Proposed inputs reuse the public synthetic ascending pattern already present
  in the general security case: PRF `0x47..0x66`, salt `0x33..0x52`, vault hash
  `0x01..0x20`, each exactly32 bytes. A second fixed vector changes only the
  first vault-hash byte from `0x01` to `0x00`. These are two assertions within
  one proposed CTest execution, not two executed cases or real secrets.
- Before implementation acceptance, independently fix and retain **both full
  32-byte expected outputs**, the exact input/domain/counter bytes, derivation
  method and provenance. Oracle preparation is a separate root-only bounded
  action; this document does not authorize it. Never compute expected values at
  test runtime via this helper, its `hmac_sha256`, or production `kKdfInfo`.
- The eventual test must require fixed32-size contracts, successful Boolean
  returns, complete output equality to literal expected arrays, and unchanged
  input/canary bytes. Use live `PV_TEST_CHECK`, not an NDEBUG-disabled assertion,
  nonzero-output-only predicate or encrypt/decrypt roundtrip. Compare bytes
  independently of the implementation's comparison helper. Synthetic mutable
  outputs should be scoped for cleanup on every assertion-return path; no
  departed-stack observation or private/real key logging.

Independent challenge should attempt to disprove domain/NUL/counter/input-order
and fixed-vault binding, reject a circular golden result, inspect preprocessor
dispatch isolation and ensure no existing passing case is silently reselected.
Exact old/new source hashes and a bounded diff are needed before promotion.

### Evidence ceiling and admission

A future successful known-answer selector would establish those two exact
current-helper/CNG compatibility outputs only. It would not establish upstream
caller identity validation, envelope/AAD serialization, production allocation
cuts, PVU-008 destruction behavior, JNA ownership, genuine Hello, hardware or
any full-family closure. No count or ledger status changes now.

Any eventual build/test remains root-only after a fresh independently reviewed
source/command/instance/coordination/resource/retention/cleanup admission. Neither
old Windows05/Mac02 helpers nor their requests are reusable execution authority.
No current build slot or new run namespace is requested here. This lane must not
stall higher-priority safely admitted Android32/current Linux work.

## Read identities and scope

Hashes bind whole files, not whole-file semantic coverage. Native paths below
are relative to `app-desktop/native/biometric-bridge/`:

| Input | Bytes / LF | SHA-256 | Actual semantic read scope |
| --- | --- | --- | --- |
| `src/windows/passvault_biometric_windows.cpp` | 99069 /2600 | `a7c0c97a3b09156afe463c2b1c825bae8330ac07488e9f2667baa2dcab27f215` | 1-416,773-812,1958-2035,2206-2264; bounded declaration/initializer locator |
| `tests/windows/passvault_biometric_windows_security_test.cpp` | 37041 /870 | `ed5f23e50ca24d01f12c99d2800fab06ad93d42e004656676b87400ad49e588a` | 1-68,202-275,538-870 |
| `CMakeLists.txt` | 9363 /230 | `976be93860deacf21db1542f26c5a4b2fbcc09d84cabba6990a0518666e409d8` | 1-230 |
| `src/macos/passvault_biometric_macos.mm` | 35723 /959 | `ba07db83b6ff8b482f4fd1d40afea2d10419c454d7856a6a1eb4ba92aa5542c9` | identity only; retained actual review reused |
| `tests/macos/passvault_biometric_macos_security_test.mm` | 20245 /459 | `946bda34cb6b536ab347539637e1f60bc41532d49e1cd08dc655398e9972fab4` | identity only; retained actual review reused |

Additional bounded current Kotlin reads:

- `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/security/biometric/JnaDesktopBiometricBridge.kt`,
  SHA256 `772d17b80f6e131aeed8cf763efa59cb909eb0696f777da555382b91f6113232`,
  16004 bytes/404LF, ranges1-185 and255-345: counted close cancellation still
  precedes the timed drain; no new PVA-010/PVU-009 conclusion beyond the map.
- Same package `DesktopBiometricNativeLoader.kt`, SHA256
  `58bfb35a24738dafa765216aba023371088c697c5b5024462b1e2bc09ecbe787`,
  22534 bytes/473LF, range1-260: packaged path/integrity/signature requirements
  cannot be replaced with development/native-test loading.
- `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopShutdownCoordinator.kt`,
  SHA256 `7156e03eb7e5d2ea7636a9320a49aeaf15d0694522d88f78836b075c74e9043d`,
  6062 bytes/172LF, range1-172: synchronous cancellation still precedes concealment
  and cleanup launch; conditional source ordering is not observed latency.

Normative base ledger SHA256
`5782666330bc2eb017e42b7a8725db1efdf54223462f3648c8acf25cb21b245e`;
selected PVA010/014/036/037 remaining rows only. PVU outcome projection SHA256
`420b7eb1a419aabec37cbc323bc7d9a9b32ca160395dcc2d5f109488820eed19`;
only PVU008/009 substantively inspected. Current C19/six continuation rows,
native/biometric skills and references, handoff permissions/assembly, PVA010
source challenge, PVU008 author/independent corrected outcome, native scope and
Windows05 source scope were also read. Historical top-level no-execution prose
does not override later independently accepted actual results.

Inspection qualifications: one ad-hoc JSON display initially assumed an object
where the base ledger is a list and raised AttributeError; the corrected
standard-library data read selected the named rows. Broad selected-row displays
truncated; decisive remaining/source-proof and actual-result fields were then
read separately without truncation. A guessed loader basename was absent; an
exact package-directory listing located the actual loader. These were inert
read/display issues, not application failures or archived-helper retries.

Only this compact permanent proposal is written. No application/helper import,
compiler/test/provider/SDK/process probe, network, Git/CI, runtime traversal,
cleanup, source edit or oracle calculation occurred. No temporary runtime or
background worker was created. PVU007 STOP, PVU011 NO RETRY, PVA02949/44/5FAIL,
G7/G8 CLOSED, all consumed/refusal/HOLD scopes, eight separate PVD decisions,
protected refs, dependencies, identities, versions, signing, Store and occupied
build1017001 remain unchanged.
