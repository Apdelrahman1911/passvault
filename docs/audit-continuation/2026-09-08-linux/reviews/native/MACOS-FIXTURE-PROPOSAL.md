# Proposed macOS native fixture correction

2026-09-09; author `/root/native`; independent challenger `/root/native_review`.
**PENDING INDEPENDENT FINDING CHALLENGE; SOURCE ONLY, NO EXECUTION ADMISSION.**
Scope alias `MAC-FIXTURE-001` is test-infrastructure/admission preparation, not
a new PVA product family or a change to confirmed-family denominators.

## Exact original source and reachability

- `app-desktop/native/biometric-bridge/tests/macos/passvault_biometric_macos_security_test.mm`:
  SHA-256 `0dcfe3a1e07aee49ca36463931b5a23a9d0302d431f9eb9f5e98726245f8797b`,
  all 151 lines read.
- Production `.mm`: SHA-256
  `ba07db83b6ff8b482f4fd1d40afea2d10419c454d7856a6a1eb4ba92aa5542c9`;
  focused guards/lifecycle reads, not a new full-file review.
- `CMakeLists.txt`: SHA-256
  `5db2d9b77a00a8308c00843ec28c5fa727e01fb292c34bf96b3e3b109bee2b23`;
  APPLE + BUILD_TESTING registers the executable and CTest case. No execution
  of that registration has been performed by this continuation.

The proposed defect is a reachable **test-fixture cleanup/isolation gap**:
`mkdtemp` succeeds at 41–43; path allocations and many `PV_TEST_CHECK` conditions
follow; that macro returns directly. Only the final 147–148 remove the
directories. A failing check or caught C++ exception after acquisition can skip
that tail. The raw created biometric context can likewise bypass its explicit
destroy. The literal `/tmp` template ignores a caller's isolated TMPDIR.

Counterexamples are retained: `mkdtemp` failure creates no child; successful
completion attempts both removals; `mkdtemp` normally creates a fresh private
0700 child. This is not evidence of every run leaking, actual escaped execution,
real-vault access or `/tmp` inherently being private-data access.

The symlink check at 100 uses `/etc/passwd`, but production `read_metadata`
156–176 rejects a symlink by `lstat` before `open(O_NOFOLLOW)`. **No actual read
of `/etc/passwd` is alleged.** It is nevertheless an unsuitable synthetic-only
negative fixture and weak discrimination: its permissions, length and content
can still reject it even if symlink protections regress. A private, valid
synthetic metadata target with a direct positive read makes the negative oracle
more meaningful without exposing a system file.

## Proposed minimal source boundary

1. Require an explicit `PASSVAULT_NATIVE_TEST_PARENT` pointing to an independently
   admitted, private owned parent. No ambient HOME, default/first path, literal
   `/tmp`, or automatic fallback. An environment variable is a caller contract,
   not proof that execution/storage is admitted. Keep one exclusively created
   child under the opened no-follow parent; do not adopt a colliding name.
2. Install a noncopyable test-only directory owner **before** acquisition. Make
   throwing path/name allocations before exclusive creation. Record the parent
   and child identities; use retained directory descriptors for bounded,
   nonrecursive cleanup of exact fixture leaf names and the known `biometric`
   subdirectory. Never follow symlink targets or scan/delete sibling/parent
   trees. Unexpected files/identities or failed closes/deletions are HOLD, not
   broad recovery permission. No destructor retry after an explicit cleanup
   attempt, including failure.
3. Separate test body from a top-level caught-unwind/result boundary so early
   returns and ordinary C++ exceptions invoke already-installed cleanup; observe
   cleanup success/absence before final process status. Guard the normal
   `pv_bio_create` pointer immediately after return. Preserve the busy-destroy
   checks and explicitly qualify their separate async/abnormal-failure limits
   rather than silently declaring all native lifetime failures fixed.
4. Replace the system symlink target with a separate valid synthetic metadata
   file, private permissions and expected exact bytes. Direct positive reads
   before/after the symlink rejection must pass. Keep it distinct from
   `context->metadata_path` (`macos-v1.meta`): both retrieve calls must still
   return missing before any Keychain/provider access. Do not call delete APIs
   that can sweep real Keychain service items.
5. Add focused source-declared fixture controls for normal/early-return and
   C++-exception cleanup, with observable child absence and a sibling synthetic
   sentinel that remains intact **before** its own independent teardown. Count
   actual later CTest cases separately from assertions. If separate case
   registration is used, change only the APPLE test block, not Windows commands,
   production ABI, dependencies, identities or versions.

No cleanup safety is inferred for fatal signals, forced process death, hostile
same-user path replacement, uncertain close/unlink results, runtime library
faults or stalled native workers. A later runner still needs exact execution,
resource/process ownership and cleanup admission. This correction is not an
Apple workflow or permission to run the whole native suite.

## Required independent disposition before implementation

Challenge finding reachability, success-path counterexamples, the current
symlink guard, the new direct-positive/negative oracle, allocation/acquisition
order, exact cleanup ownership and the provider boundary. Confirm only the
test-fixture gap if supported; leave product/PVD and hardware conclusions
unchanged. The later exact patch and regression design require a second
independent challenge. No code change has preceded this proposal.

All Windows01 FAIL/HOLD/14-unstarted evidence, accepted executable/helper/request,
inert observability proposal, Apple Kotlin supplement, physical-device gaps and
STOP/NO-RETRY/CLOSED/build1017001 restrictions remain unchanged. Only source
inspection, hashing and this permanent report were performed; no build, test,
native/helper import, CI or cleanup recovery was executed.
