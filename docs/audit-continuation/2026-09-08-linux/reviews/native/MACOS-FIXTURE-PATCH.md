# MAC-FIXTURE-001: source patch submission

2026-09-09; author `/root/native`; requested reviewer `/root/native_review`.
**IMPLEMENTED SOURCE ONLY; INDEPENDENT PATCH CHALLENGE PENDING AT SUBMISSION.
ZERO MACOS EXECUTIONS; NO EXECUTION OR RECOVERY ADMISSION.**

This is the independently supported test-fixture/admission gap, not a new
product PVA, reopened stopped concern or PVD redesign. The prepatch review is
`../native-independent/MACOS-FIXTURE-FINDING-REVIEW.json`, SHA-256
`af52955f85171d5ce6812cbab40aebacb16c7f570b222bae8e9f668b57bc0610`.
The frozen proposal remains historical, including its then-pending label.

## Exact source and scope

`MACOS-FIXTURE-PATCH-INPUTS.json` has SHA-256
`3755b0e18b8e4ed94870c03621c5c022993af4ca763660e2bc01c9dae207acc9`.
It binds the three changed/added paths, before/after bytes, focused context
reads and unchanged intervals against observed base commit
`f18995e6633904a6845e09eab8a481635290894d`, tree
`8948bb6c92b5baaf406f838b48bf8e31064d1195`. These working-tree file tuples do
not claim a postpatch whole-tree identity or cover other agents' work.

| Path beneath `app-desktop/native/biometric-bridge/` | SHA-256 | Physical LF |
| --- | --- | ---: |
| `tests/macos/passvault_biometric_macos_security_test.mm` | `946bda34cb6b536ab347539637e1f60bc41532d49e1cd08dc655398e9972fab4` | 459 |
| `CMakeLists.txt` | `f72420bc39a5c5dad514b60eb0583880fc329e0a1a57a9b5dd12c8eeeb3c54e3` | 187 |
| `tests/macos/README.md` | `01211c798abc0a3a4134b1606ee9e57ec3d136ef12f540b3a08dd4a2b3691c61` | 57 |

Production macOS code and Gradle bytes are unchanged. The exact existing
`verify_bounded_busy_destroy` interval and Windows/common CMake tail match
their before-images; see the manifest's inclusive LF interval hashes.

## Mechanism and counterexamples

- The noncopyable fixture guard is installed before `create()`. All path/name
  string allocations finish before the exclusive child `mkdirat`; existing
  parents/colliding children are not adopted. Required
  `PASSVAULT_NATIVE_TEST_PARENT` has no fallback. A final trailing slash or dot
  component is rejected so it cannot defeat the intended final-entry
  `O_NOFOLLOW` check. Effective UID/private mode bits and retained directory
  identities are checked; ancestors and ACLs still require runner admission.
- Parent/root/biometric descriptors and dev/inode identities restrict cleanup
  to this newly created child. Only `metadata`, `macos-v1.meta` and
  `synthetic-metadata` are eligible leaves. `fstatat(AT_SYMLINK_NOFOLLOW)` checks
  type/UID/link count before `unlinkat`; symlink targets, siblings and the
  supplied parent are not removed. Cleanup is nonrecursive, not a wildcard
  sweep of writer `.tmp.*` names.
- A collision grants no ownership. Other failed exclusive child creation is
  conservatively HOLD/uncertain, not inferred absence or a retry. A successfully
  created directory whose descriptor identity cannot be bound is likewise HOLD.
  Unknown files, changed identities, hard links and close/unlink/rmdir failures
  cannot yield fixture success. Independently safe exact-leaf removal may still
  proceed after an earlier leaf failure; no recursive recovery is introduced.
- Cleanup has one attempt and caches its result. All opened descriptors receive
  one close attempt even after removal failure. A failed/ambiguous close is
  HOLD, not retried against a potentially reused descriptor. Successful root
  removal is followed by an actual `fstatat` ENOENT check through the retained
  parent. The aggregate fixture status forces a nonzero final process result.
- Ordinary failed checks now unwind the scope; the new top-level C++ catch
  ensures ordinary thrown exceptions have a handler. The normal create-returned
  native context is immediately guarded by a `unique_ptr` deleter and released
  at its old point on success. The failure macro prints its source line but
  returns **1**, avoiding POSIX low-byte exit truncation after the file grows
  past 255 lines. Invalid selectors and unexpected C++ exceptions also fail.
- The old `/etc/passwd` symlink was already rejected before an actual read;
  no system-file disclosure is alleged. It is replaced by a private valid
  synthetic target with direct positive/read-byte-equality checks before and
  after the symlink negative. This strengthens a follow-symlink regression
  oracle without claiming to isolate each redundant lstat/open guard. The
  target remains separate from `biometric/macos-v1.meta`; intended retrieval
  calls still encounter missing metadata before Keychain lookup. No enrollment,
  authentication, deletion or real provider-data scenario is added.

## Regression declarations, not runtime evidence

The original security case is preserved and now uses the fixture. Three new
APPLE-only CTest registrations select normal, assertion-return and caught
C++-exception controls. There are **three added case declarations, zero new
executions and zero XML results**. Including the existing security and shared
ABI entries, APPLE source declares five CTest cases; that is not five runs or
an assertion count.

Each new control gives the subject a synthetic regular file and a symlink to
an independently guarded sibling's valid sentinel. A late injection marker
prevents an ordinary setup failure from passing as the intended early return;
the exception case requires its unique synthetic exception. The caller checks
one attempt, acquired child, successful cleanup/absence/close observations and
then independently observes child ENOENT via the still-live sibling's parent
descriptor. It checks the sibling sentinel's dev/inode/mode/size and exact
decoded bytes **before sibling cleanup**. These real-filesystem declarations
are stronger than mock-only cleanup assertions but remain unexecuted.

The normal control explicitly cleans once, then leaves scope; the early-return
and exception controls require destructor cleanup. The early-return probe's
intentional diagnostic is not an unexpected failed CTest case if the enclosing
case's controls succeed. A future run must retain both selector/result identity
and cleanup diagnostics, not treat log text or a successful build as case proof.

## Compatibility and retained gaps

**Existing macOS Gradle/Desktop test callers now fail closed unless they supply
the explicitly admitted private parent.** Gradle's `testDesktopBiometricBridge`
currently invokes CTest without creating or setting it, and Desktop `Test`
tasks depend on that task. This intentional test prerequisite is documented
prominently in the new README; behavior-identical caller compatibility is not
claimed. No production ABI, application storage, version, dependency, identity
or product boundary changes.

The directory guard is not a complete native-worker/process settlement proof.
The existing busy-destroy function is byte-identical, including its async
launch/failure and possible abnormal scheduling limitations. Its name does not
establish a hard elapsed bound. The ordinary context deleter attempts the
existing guarded destroy; this does not prove destruction under every native
library or exceptional failure. Fatal signals, forced death, abnormal Objective-C
exceptions, synchronous syscall/stdio stalls, ACL/ancestor substitution and
name-check/unlink races remain outside the cooperative source guarantee.

Mac target compilation, actual filesystem case results, native framework
behavior, source/environment/resource/process/interruption/cleanup admission
and physical-device evidence remain missing. The fixture is not a runner and
does not authorize even a compile-only or hosted Mac invocation. No missing
authoritative skill tool was needed for this source-only correction.

The CMake file hash changed despite an identical Windows branch; any future
Windows proposal needs fresh source binding/review. **Do not rewrite or reuse
Windows01's consumed request or its accepted executable inputs.** Windows01
remains FAIL, 14 planned cases UNSTARTED and filesystem cleanup HOLD. Its
unknown descendant/reparse/parent-exit details stay unknown. The observability
patch remains inert and unadmitted; no Windows retry or recovery occurred.

All PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED
and non-publishing/protected-ref/build1017001 restrictions are unchanged.
Denominators stay original **19/25**, all confirmed **22/37**, original suspicions
**2/12**; eight design explanations remain separate from owner decisions.

## Work and resources

Only source inspection/editing, hashing and a bounded owned-source whitespace
inspection were performed by this author. That `git diff --check` returned 0
for the two tracked code/CMake edits; it is not compilation or a test result.
Point sample: 32,088,412 KiB available disk and 44,567,748 KiB MemAvailable,
not execution admission. No build/test/helper/native import, CI, archive,
temporary executable, cache or background worker was created by this task.
Only permanent source/tests/docs/compact review records were retained; no
unrelated files/processes, SDKs/toolchains or shared caches were removed.
