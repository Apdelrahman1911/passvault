# Initial macOS native-five validation plan

2026-09-09; author `/root/native`; requested independent reviewer
`/root/native_review`; sole build/CI/commit/push owner `/root`.
**ACTIVE SOURCE-ONLY DESIGN, NOT EXECUTION ADMISSION. NO RUNNER/WORKFLOW OR
REQUEST IS IMPLEMENTED BY THIS PACKET. ZERO TARGET EXECUTIONS.**

This is separate from the frozen MAC-FIXTURE-001 source correction and root's
C3 publication. It proposes one important NON-PUBLISHING Intel macOS native
batch, not an Apple/Windows matrix, simulator, app launch or replacement release
candidate. Android32 remains first risk priority. No audit-owned Mac job may be
queued or started until root's local database execution, workers and required
closeout have settled and root explicitly transfers the single execution slot.

## 1. Five exact cases and limited meaning

Linux cannot execute these Objective-C++/Foundation/LocalAuthentication/Mach-O
paths. Root clarified that this proposal may include **test-owned** `LAContext`
construction, localized-property reads/writes and `invalidate`. It may NOT
include capability/enrollment inquiries, `canEvaluatePolicy`, `evaluatePolicy`,
Keychain enrollment/retrieval/deletion, OS dialogs, authentication, real vault
data, personal clipboard or hardware operations. The following source paths
return before those forbidden operations; invoking a null/missing-metadata
application ABI entry is not a Keychain retrieval experiment.

| Order / exact CTest name | Finding/scope | Meaning of a later qualified pass |
| --- | --- | --- |
| 1 `passvault_biometric_macos_fixture_normal` | MAC-FIXTURE-001 | Explicit fixture cleanup once, observed child absence and unchanged sibling sentinel before sibling teardown |
| 2 `passvault_biometric_macos_fixture_early_return` | MAC-FIXTURE-001 | Intended assertion-return reached; destructor cleanup and independent filesystem observations |
| 3 `passvault_biometric_macos_fixture_cpp_exception` | MAC-FIXTURE-001 | Dedicated ordinary C++ exception reached/caught; destructor cleanup and independent filesystem observations |
| 4 `passvault_biometric_abi` | PVA-014 compatibility sibling | Actual Mach-O ABI version/additive localized symbols, null-context rejection and synthetic output wiping, not prompt language |
| 5 `passvault_biometric_macos_security` | MAC-FIXTURE-001; bounded PVA-014/PVA-010 native siblings | Synthetic metadata permissions/symlink controls, missing-metadata retrieval/wipe, English/Arabic LAContext property/UTF-8/error-mapping checks, two synthetic busy-destroy schedules |

These are **five existing CTest declarations**, including the three recently
added fixture controls. There are zero new declarations, executions, XML
results or family closures from this plan. The fifth case is deliberately last
because its existing async/native failure schedules need stronger settlement
evidence than fixture directory status alone.

### Source reachability and counterexamples

- ABI test 1–28 supplies null contexts. Production enrollment 702–715 returns
  at 709–714 before LAContext/authentication; retrieval 812–835 wipes the
  synthetic output then rejects null context before provider access.
- The security case creates a private, newly empty `biometric` directory.
  `pv_bio_create` 539–571 selects `biometric/macos-v1.meta`; both retrieval calls
  return NOT_ENABLED at 834–835 before Keychain lookup at 867. The fixture's
  valid metadata/symlink targets are distinct from that name. No call to
  capability, contains, delete or successful enrollment is selected.
- `configure_biometric_context` 414–424 sets reason/fallback/reuse properties;
  `localized_prompt_reason` 426–434 performs bounded NSString decoding. This
  is LocalAuthentication API activity, **not** “no native API calls,” displayed
  text, authentication or key release.
- Existing busy-destroy 217–240 uses mutex bookkeeping, test-owned LAContext
  objects, one `std::async` operation per schedule and destroy 574–600. There
  is no authentication. The raw-context/async-launch exceptional schedule,
  possible blocking before/at `destroy.get()`, guarded destroy's swallowed
  failures and forced-death behavior are not repaired by this plan. A timeout
  label or fixture `settled` line is not complete worker settlement proof.
- The combined symlink negative now has valid synthetic direct-positive
  controls; it does not isolate every redundant lstat/O_NOFOLLOW guard.
  Existing successful/rejected historical evidence and every remaining
  platform/provider/packaged/Kotlin-interleaving limitation stay separate.

PVA-014 real displayed prompts, physical Touch ID/iPhone security, PVA-010's
actual JVM/native/provider/packaged lifetime scenarios, iOS PVA-008 tests and
Windows Hello remain **outside this batch / BLOCKED where already recorded**.

## 2. Exact source binding before any run

Observed starting HEAD remains `f18995e6633904a6845e09eab8a481635290894d`, tree
`8948bb6c92b5baaf406f838b48bf8e31064d1195`. It is **not** the future postpatch
source commit: the accepted fixture/CMake after-images are still separately
bound working-file tuples while root prepares C3.

| Path beneath `app-desktop/native/biometric-bridge/` | SHA-256 |
| --- | --- |
| `CMakeLists.txt` | `f72420bc39a5c5dad514b60eb0583880fc329e0a1a57a9b5dd12c8eeeb3c54e3` |
| `include/passvault_biometric.h` | `dc76bea46e1abc0a2950f55b549fe68cf1399d256756e85ad0a79ef52b228b85` |
| `src/macos/passvault_biometric_macos.mm` | `ba07db83b6ff8b482f4fd1d40afea2d10419c454d7856a6a1eb4ba92aa5542c9` |
| `tests/passvault_biometric_abi_test.cpp` | `354d7e1904d46a2155774052ce20c476763c97f9ed293b94c4bce54208fa3785` |
| `tests/macos/passvault_biometric_macos_security_test.mm` | `946bda34cb6b536ab347539637e1f60bc41532d49e1cd08dc655398e9972fab4` |

The full header (120 LF), ABI test (28 LF), CMake (187 LF) and accepted fixture
(459 LF), plus focused production guards above, were inspected. This is not
new whole-project coverage. Fixture acceptance is
`../native-independent/MACOS-FIXTURE-PATCH-REVIEW.json`, SHA-256
`2a9564df192f9bc67c8a1966049b86d589d6ac79dd50e3f8e2aa9afe5aa3e6d9`, including
the mandatory author qualifications `f821e6db983d0cc647710a7edf1f227fdcb27e1c6e5154a04814356a9a65e97c`.
That source-only acceptance is **not a runner/CI acceptance**.

Before activation, root must seal a new commit **S** and tree **T** containing
the independently reviewed final helper/workflow/source-map/plan and normative
independent acceptance. A separate activation commit must have parent S and
change only the new one-shot request. The helper must validate actual HEAD,
parent/tree, clean checkout, exact one-path diff, fixed case list and hashes
from stable no-follow single-capture input bytes before allocation, then repeat
the binding under its original namespace/coordination authority. Reject stale,
self-authored, wrong-purpose or merely “pending” reviewer attestations. Record
both source S/T and actual activation HEAD/tree. S/T are deliberately unfilled
in this initial plan; no historical lock/PID/inode becomes Linux/Mac authority.

## 3. Current official image declarations, not an observed runner

Root authorized three bounded anonymous source-only HTTPS GETs. `curl -q` used
no credential/config input, no authentication header, no redirect following,
HTTPS-only transport, 5-second connect and 20-second total limits. Responses
went to stdout and an isolated `python3 -I -B` standard-library data/hash
reader with matching byte caps; no downloaded program was executed/imported.
No temporary/download/cache file or background worker was created. All three
captures completed with exit 0. These are source captures, **not tests**.

| UTC capture | Exact URL | Bytes / cap | SHA-256 |
| --- | --- | --- | --- |
| `2026-09-09T02:00:39.141535+00:00` | `https://api.github.com/repos/actions/runner-images/git/ref/heads/main` | 349 / 32768 | `13753aa23fe4591f67bd691840f9ad0f53fed6a40782d617d593545a07fda0e8` |
| `2026-09-09T02:01:10.080231+00:00` | `https://raw.githubusercontent.com/actions/runner-images/57fdccbc4a47d85e23cc79eaeb63cb8ae0e997b5/README.md` | 21398 / 131072 | `e2282f1ccf4a8c952433ada336e1eeda9eaffe97592c9b9c334fcaac6115f63d` |
| `2026-09-09T02:01:40.069780+00:00` | `https://raw.githubusercontent.com/actions/runner-images/57fdccbc4a47d85e23cc79eaeb63cb8ae0e997b5/images/macos/macos-15-Readme.md` | 24192 / 262144 | `1de4b226036b0d24fca39de51eea4c44285b336dfd3b1db2056bcf0aca639f23` |

The first capture named commit `57fdccbc4a47d85e23cc79eaeb63cb8ae0e997b5`.
Selected official README lines 31–36 distinguish `macos-15-intel` **x64** from
`macos-15` **arm64**; macOS14 is deprecated. Selected image README lines
8–10 declare macOS15.7.9 / Darwin24.6.0 / image20260824.0482.1; 29 and 80 declare
Python3.14.7 and CMake4.4.2; 167 lists Xcode16.4 build16F6 at
`/Applications/Xcode_16.4.app`; 180 associates it with SDK `macosx15.5`.
These were bounded selected-line reads, not an independent image audit.

Proposed **single target**: standard `macos-15-intel`, native x86_64 (no Rosetta,
ARM slice, larger runner or matrix), installed Xcode16.4/16F6 with macOS15.5
SDK, CMake/CTest4.4.2 from the same installed prefix and Python3.14.7. Do not
silently select Xcode26/Homebrew LLVM/another SDK when a requirement is absent.
Use process-local `DEVELOPER_DIR`, never change system `xcode-select` settings.
Do not install/update tools or dependencies. Actual runner OS/architecture,
image version, selected paths, tool hashes, Xcode/SDK versions and architecture
must be recorded and match the separately sealed admission before compilation.
The image label itself is mutable; the declared image version above is not
future attestation. Drift/missing tools requires HOLD and review, not retry or
unreviewed substitution. No physical-device inference follows from this target.

## 4. Commands to seal in the new implementation

The following vectors are **specifications, not commands to run now**. W is
the exact activation checkout; U is the fresh original generated namespace;
E is its separately retained compact evidence sibling. TC is the admitted
`/Applications/Xcode_16.4.app/Contents/Developer`; SDK/CC/CXX are exact outputs
from that selected installation, revalidated and hash-recorded. CMAKE/CTEST
are the sealed matching installed executables, not arbitrary PATH lookups.

With cleanup/ownership handlers already installed, perform bounded read-only
source/tool preflight: exact Git HEAD/parent/tree/status/diff queries; OS/image
and `uname -m`; `/usr/bin/xcodebuild -version` (version only, never build/archive);
`/usr/bin/xcrun --sdk macosx15.5 --show-sdk-path`, `--show-sdk-version`, and
`--find clang` / `--find clang++`; CMAKE/CTEST `--version`; compiler `--version`.
Resolve/hash the selected binaries and relevant SDK settings, not shared caches
or private signing stores. No Xcode project/application scheme is opened.

```text
CMAKE -S W/app-desktop/native/biometric-bridge -B U/build
  -G "Unix Makefiles" -DBUILD_TESTING=ON -DCMAKE_BUILD_TYPE=Release
  -DCMAKE_OSX_ARCHITECTURES=x86_64 -DCMAKE_OSX_SYSROOT=SDK
  -DCMAKE_C_COMPILER=CC -DCMAKE_CXX_COMPILER=CXX -DCMAKE_OBJCXX_COMPILER=CXX
  -DCMAKE_EXPORT_COMPILE_COMMANDS=ON
CMAKE --build U/build --config Release --target
  passvault_biometric_macos_security_test passvault_biometric_abi_test
  --parallel 1 --verbose
CTEST --test-dir U/build -C Release --show-only=json-v1
CTEST --test-dir U/build -C Release --parallel 1 --timeout 30
  --no-tests=error --output-on-failure --output-junit E/case-NN.xml
  -R ^<one fixed escaped exact name from section 1>$
```

The last vector runs once for each of the five ordered cases, stopping at the
first failure/timeout/ownership or cleanup uncertainty. Later cases remain
**UNSTARTED**, not passed/skipped. Inventory must contain exactly five names
and the expected exact U/build executable/selector vectors before a case can
run. Do not run aggregate `ctest` or reuse a release workflow. No Gradle, JVM,
Detekt, Kotlin/native, simulator, emulator, app packaging, install, signing,
store action or build1017001 is involved. JDK17/checked-in wrapper/one-worker/
non-daemon/serial-Detekt/dependency-verification rules remain binding for any
separate Gradle scope; wrapper `--stop` here is **NOT_APPLICABLE**, not a claim
to have stopped any historical Gradle obligation.

Inspect compact CMake cache/compile-command/verbose evidence for actual Apple
toolchain, architecture, expected source, ABI export and existing hardening
flags; reject unexpected commands/source/task expansion. Retain Mach-O type/
x86_64 identity and SHA-256 of the dylib and two test executables, **not binaries**.

## 5. Fresh process and resource admission remains to be implemented

**There is no accepted macOS process controller in this packet.** Do not treat
Windows Job, Linux pidfd/cgroup or historical macOS PID/lock semantics as if
they port automatically. A future helper must bind each owned command's
original process identity before application work, record parent exit
separately from worker settlement, and account for original compiler/linker/
CTest/test descendants without dropping after-only births. A zero CMake/CTest
exit or a fixture cleanup Boolean alone cannot establish settlement.

Unreaped direct-child ownership can protect that direct PID from reuse, but
does not automatically protect an observed descendant PID. Birth snapshots,
command names, process groups, CWD or a copied journal alone do not authorize
signaling. No group/name killing, borrowed PID adoption, unanchored descendant
kill or global daemon stop is allowed. The exact Mac identity/signal/observation
primitive and CTest's own timeout behavior need independent source challenge
and narrowly admitted real-process controls before project commands. This
plan does not assert a kernel PID-bound kill or complete fork tracker exists.

If an original worker cannot be proved settled, or an identity/signal/close is
ambiguous, stop advancement, retain FAIL/HOLD and do not retry the action or
delete possibly live outputs. Root may cancel only this audit-owned run; host
disposal/cancellation is external containment, **not observed cleanup success**.
Do not keep launching checks while treating those obligations as discharged.
Required controls must cover ordinary settlement, cancellation/timeout,
retained-descendant and reuse/unrelated-process refusal, signal/close failure
and interruption limits. They are currently requirements, not executed tests
or authorization to create a general-purpose runner framework.

Proposed bounds: one job, one build worker, 900-second global command budget;
configure120s, build420s, inventory/preflight tools30s each, CTest30s with45s
outer case bound, and120s reserved cleanup. Actions job timeout20min leaves
bounded initialization/upload allowance. A global expiry/cancellation gates
new launches on observation; synchronous native calls and check-to-call races
must not be described as hard real-time guarantees. No automatic retries.

Maintain handoff floors on every relevant filesystem: **12GiB available disk
and25% available physical RAM before launch; 8GiB/20% during work**, sampled
at most5s apart plus phase boundaries. The Mac available-memory observation
and unit/definition need explicit source review; do not substitute Linux
`/proc/meminfo`, an unreviewed `vm_stat` formula or total RAM for availability.
Owned process-count/RSS and generated-output/log limits also need exact
implementation guards. Point samples are not global exclusivity or a resource
lease. Root alone manages the cross-local/CI slot and unrelated-work response.

## 6. Original private storage and cleanup before commands

Proposed U/E are fresh absent names below the newly bound runner-temp parent,
using exact run_id/attempt1 and root's new one-shot nonce. They are NOT aliases
or adopted leftovers. Bootstrap intent and partial creation must be recorded;
retain original parent/root descriptors and identities, with no re-adoption if
binding fails. Do not reuse original-host identities or recover old scopes.

Create only owned private U children for `build`, `home`, `tmp`, `cache`, logs
and `native-parent`. The latter is the explicit
`PASSVAULT_NATIVE_TEST_PARENT`; it must be newly empty, effective-UID owned,
private-mode, and independently admitted for ancestor/ACL safety. The fixture
itself does not validate all ancestors or ACLs. The exact Mac ACL/parent check
still needs implementation/review, not a mode-only privacy assertion. Never
use ambient `/tmp`/HOME or a fallback parent.

Build/test child environment must be assembled from a fixed allowlist, not a
copy of the runner environment: admitted tool PATH/DEVELOPER_DIR/SDK, synthetic
HOME/TMPDIR/TMP/TEMP/cache paths, explicit native parent, C locale and parallel
limits1. Exclude signing/Store/GitHub/runtime credentials, DYLD injection,
compiler/CMake init overrides, user package/preset configuration and profile
hooks. Do not dump ambient environment or read real Keychains/vaults/backups.

Install finalization/cancellation handling **before** tool/build/test launch.
After each phase preserve bounded evidence and verify original worker
settlement. Keep only artifacts needed for later admitted cases; each fixture
must settle its own children before another case. After the final case/failure,
preserve compact evidence and remove only validated original allowlisted U
generated outputs after process settlement. Use retained-descriptor no-follow
bounded traversal with same-device/UID/type/link/identity checks, durable
per-deletion intent and verified removal; no pathname `rmtree`, `git clean`,
wildcards, source/report/shared-cache/SDK/toolchain deletion or blind retries.
Exact traversal/FD/resource bounds and cooperative unlink races remain to be
independently specified and reviewed in helper source.

**Do not automatically sweep `native-parent` after a fixture HOLD.** A failed
fixture cleanup has already consumed its attempt, and the runner does not
inherit deletion authority for its uncertain child. Observe emptiness after
known worker settlement; remove the runner-owned parent only if empty and its
original identity matches. A remaining child, unknown writer `.tmp.*`, failed
close or ambiguous removal remains HOLD for separate admission. Independently
safe other output cleanup may proceed only when its authority is established.
Hard kill/runner loss can interrupt cleanup/evidence/upload; retain that limit
rather than claim hosted disposal completed these deletions.

## 7. Dedicated branch-only workflow and compact evidence

Proposed future paths, notified to root but **not created/edited/activated**:
`scripts/audit/macos_native_validation.py`,
`.github/workflows/audit-macos-native-validation.yml`, and the one-shot
`docs/audit-continuation/2026-09-08-linux/requests/macos-native-01.json`.
They require separate source implementation/independent review/root permission.
No Windows helper, consumed request, stopped helper or release workflow is
reused, imported or invoked.

Use only a push trigger for that request path on
`codex/audit-continuation-linux-20260908`; no protected-ref, PR, dispatch,
schedule, tag, release, reusable workflow or environment trigger. Require
exact repository/ref, push-not-deleted and attempt1 again at job/helper entry.
Validate the request-only activation diff even if platform path-filter behavior
is broader than expected. No request change is included in ordinary source or
documentation pushes. Root must verify no queued/running local or audit CI job
before activation and keep the slot until actual settlement/result review.

One `macos-15-intel` job, `contents: read` only, no environment/secrets bindings,
no write/OIDC/deployment permission; shared audit concurrency group
`passvault-audit-validation-20260908`, `cancel-in-progress: false`. Do not cancel
unrelated jobs or silently cancel a still-obligated audit predecessor. Proposed
immutable action pins, independently rechecked before implementation/admission:
`actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1` and
`actions/upload-artifact@043fb46d1a93c77aae656e7c1c64a875d1fc6a0a`.
Checkout exact activation SHA, depth2, credentials not persisted, no LFS or
submodules. No release/signing action or repository/store secret is exposed.

Retain only request/source/command/toolchain/architecture/resource/ownership/
cleanup records, bounded logs, inventory and five exact per-case XMLs. Proposed
limits are2MiB per retained command log,512KiB per XML,16MiB total evidence;
overflow/truncation or non-EOF received-stream hashes must be explicit and may
not become a semantic pass. Do not upload `.app`, dylib, executable, object,
CMake build tree, Gradle cache, Xcode archive or large binary. Artifact retention
is3 days; root promptly retains necessary compact evidence/hashes in the branch.

Each XML must be bounded, nonempty, unambiguous and contain exactly its selected
case identity; reject extra/missing/duplicate/error/failure/skipped outcomes,
unsafe XML declarations or wrong mappings. Combine actual exits/XMLs and
cleanup evidence in independent postrun adjudication. Count cases, not CMake
tasks/assertions or XML filenames. Preserve failures/unstarted cases and
operational cleanup blocks even when some case executions individually pass.

## 8. Honest completion boundary and next permitted work

This initial source scope/command plan needs independent challenge. Still
missing: final helper/workflow/request schema and normative review; actual Mac
process/memory/ACL/cleanup primitives and controls; source S/T; current admitted
run/image facts; root's released local-DB slot and specific execution admission.
No implementation or execution acceptance follows from writing the plan.

Only source inspection, the three approved public metadata reads and this
permanent plan were performed in this subtask. No target process/test/build,
CI, simulator, native helper import, temporary artifact/cache or background
worker was created. Prior cleanup obligations were neither retried nor
discharged. Windows01 stays **FAIL / filesystem HOLD /14 UNSTARTED**; its
observability patch stays inert and accepted executable inputs stay unchanged.
PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED,
protected refs, versions/dependencies/identities, build1017001 and publication
fences remain binding. Counts stay original19/25, all22/37, suspicions2/12;
eight design explanations are not owner approvals or readiness percentages.
