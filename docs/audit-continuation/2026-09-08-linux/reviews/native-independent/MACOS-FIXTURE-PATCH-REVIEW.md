# MAC-FIXTURE-001: independent patch challenge

2026-09-09; reviewer `/root/native_review`; author `/root/native`.
**QUALIFIED SOURCE ACCEPTANCE; TARGET COMPILATION/RUNTIME STILL PENDING.
ZERO EXECUTIONS OR XML; NO MACOS EXECUTION/CLEANUP ADMISSION.**

This disposition requires the frozen patch **and** the author's requested
`MACOS-FIXTURE-PATCH-QUALIFICATIONS.md` addendum, SHA-256
`f821e6db983d0cc647710a7edf1f227fdcb27e1c6e5154a04814356a9a65e97c`.
It does not accept broader readings of the original packet. MAC-FIXTURE-001
is a test-infrastructure/admission correction, not a new product PVA family,
a reopening of stopped work, or a qualified remediation closure.

## Exact inputs and independent work

The accompanying JSON binds all packet/context inputs, reviewed intervals,
prior finding review and retained Windows identities. The three patch tuples
under `app-desktop/native/biometric-bridge/` are:

| Path | SHA-256 | Physical LF |
| --- | --- | ---: |
| `tests/macos/passvault_biometric_macos_security_test.mm` | `946bda34cb6b536ab347539637e1f60bc41532d49e1cd08dc655398e9972fab4` | 459 |
| `CMakeLists.txt` | `f72420bc39a5c5dad514b60eb0583880fc329e0a1a57a9b5dd12c8eeeb3c54e3` | 187 |
| `tests/macos/README.md` | `01211c798abc0a3a4134b1606ee9e57ec3d136ef12f540b3a08dd4a2b3691c61` | 57 |

The reviewer read the entire fixture, CMake, README, report, manifest and
addendum; compared original bytes and focused unchanged production/Gradle
context; and independently recomputed hashes/interval equality. This was
source/data inspection only: no compilation, helper execution/import, test,
CI, native provider invocation or recovery. The reviewer changed no reviewed
source. Observed base commit was `f18995e6633904a6845e09eab8a481635290894d`,
tree `8948bb6c92b5baaf406f838b48bf8e31064d1195`; reviewed working-file tuples
are not a claim to cover concurrent agents' changes or a postpatch Git tree.

## Challenges and disposition

1. **Reachable leak paths and acquisition.** The prior finding's successful
   `mkdtemp` followed by assertion-return or string exception was reachable;
   failed acquisition owns nothing and successful old cleanup remain preserved
   counterexamples. The new noncopyable guard exists before `create()`.
   Only its owned member path strings finish allocating before exclusive
   `mkdirat`. Probe-name copies and production temporary-path allocations
   happen later under the installed guard. Root collisions are not adopted;
   uncertain creation or acquired-but-unbound identities yield failure/HOLD,
   not an inferred removal success. Returning false before acquiring a child
   can still settle owned descriptors, but the caller's failed create check
   prevents that from turning into a successful test.
2. **Cleanup authority is narrow, cooperative and once-only.** Retained
   parent/root/biometric directory fds plus UID/private-mode/dev/inode checks
   constrain deletion. Only `metadata`, `macos-v1.meta`, `synthetic-metadata`
   and the acquired child directories are eligible; leaf checks use nofollow
   stat and unlink never follows a symlink target. No parent/sibling sweep,
   hard-link adoption or recursive unknown-file deletion was introduced.
   A failure may be followed by independently safe exact-leaf attempts, but
   cannot turn the aggregate result back to success. Unknown writer `.tmp.*`
   residue is HOLD. Explicit cleanup caches its result, and the destructor
   does not repeat a failed attempt. Successful removal needs actual ENOENT.
3. **Close/result scope.** Each of the fixture's three retained directory
   descriptors receives at most one close attempt, including after removal
   failure; ambiguous close is not retried. `descriptors_closed` does **not**
   observe production helper fds or native workers. In particular the unchanged
   `fsync_directory` ignores its own close result. `all_cleanup_ok` preserves
   any observed fixture failure through final exit, but a fixture `settled`
   log is not whole-process clearance. Diagnostics are best-effort stdio,
   not an independent settlement record or a hard-time guarantee.
4. **Ordinary unwinding and failure reporting.** The real assertion macro
   returns 1, not a source line subject to POSIX low-byte truncation. Scoped
   return and ordinary C++ exception unwinding reach the fixture; the top-level
   catch makes unexpected ordinary exceptions a failed result. The normal
   create-returned context is immediately put in a `unique_ptr` before checks,
   with its successful release point retained. This does not repair/prove the
   existing async busy-destroy exceptional schedules, abnormal Objective-C
   behavior, fatal signals, forced death or native-library settlement.
5. **Non-vacuous declared filesystem controls.** Each new selector acquires
   a subject child containing a synthetic regular file and a symlink to a
   separately guarded sibling sentinel. The late injection marker rejects
   accidental setup failure masquerading as the intended early return; the
   exception must have its dedicated type. Normal uses explicit cleanup then
   destruction; the other two require destructor cleanup. Returned fields
   alone are insufficient: after the probe has unwound, the enclosing case
   checks actual subject ENOENT using the sibling's still-live parent fd and
   checks sibling dev/inode/mode/size plus decoded sentinel bytes **before**
   sibling teardown. Those declarations exercise real syscalls, not mocks;
   they are still unexecuted. Failure-injected acquisition/close/unlink and
   interruption schedules are not claimed as tested by these three cases.
6. **Symlink and provider reachability.** The original `/etc/passwd` target
   was already rejected by lstat/O_NOFOLLOW; no actual system-file disclosure
   is alleged. The new valid owned synthetic target has positive reads/byte
   checks before and after the negative symlink check. This strengthens the
   combined nofollow oracle, but does not isolate every redundant guard: e.g.
   replacing only lstat while retaining O_NOFOLLOW may still reject. The
   synthetic target is not `biometric/macos-v1.meta`; the unchanged retrieval
   flow returns NOT_ENABLED on missing metadata before Keychain access.
   No enrollment, authentication or real provider-data case is added.
7. **Caller and CMake compatibility.** The existing macOS Gradle task does
   not create/set `PASSVAULT_NATIVE_TEST_PARENT`; Desktop JVM Test tasks depend
   on it. Those callers intentionally now fail closed without an independently
   admitted parent. README/report make this test prerequisite explicit; this
   is not behavior-identical caller compatibility or new runner authority.
   No production ABI, app identity, dependency, version or product boundary
   changes. The original Windows/common interval hash was correct but began
   three lines after WIN32's header. Independently verified full before
   **62–179** / after **70–187** interval hashes to
   `72f7ebd4a170f4923ba16ffd213a552feee03a19aec62d4add7ed12b67499c0e`.
   The source patch changes only the APPLE CTest registration block in CMake.

No further functional defect was found within this explicitly limited source
scope. This is not proof that the patch compiles or behaves on macOS.

## Coverage and retained blockers

Three added CTest declarations are `passvault_biometric_macos_fixture_normal`,
`passvault_biometric_macos_fixture_early_return` and
`passvault_biometric_macos_fixture_cpp_exception`. Including existing security
and shared ABI entries, APPLE declares five cases. **Declarations: 3 added;
new executions: 0; new XML: 0; new product families: 0; qualified closures: 0.**
They do not alter any ledger denominator or convert source proofs to tests.

Fresh reviewed Mac source/toolchain/architecture/environment, isolated parent,
coordination, process/resource bounds, interruption and cleanup admission are
still required before any build/test. No compile-only/CI exception is implied.
Ancestor/ACL substitution, retained-fd/name races and synchronous syscall/stdio
stalls remain cooperative-host limitations. Hardware Touch ID/iPhone security
and Windows Hello/interactive-device claims remain separate genuine gaps.

Windows01 stays operational **FAIL**, filesystem cleanup **HOLD**, all **14
planned cases UNSTARTED**; its unknown descendant/reparse/parent-exit details
remain unknown. The helper, workflow and consumed request are unchanged.
CMake's whole-file hash has changed, so no future Windows proposal may reuse
that consumed source binding. The observability proposal remains inert.
PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED,
PVD/owner decisions, protected refs, non-publishing and build1017001 fences
remain unchanged. This source acceptance grants no retry or recovery scope.

## Reviewer resource/cleanup record

Only compact permanent reports were created. No task-owned build/cache,
archive, temporary executable, worker, daemon, emulator or background task was
created; no wrapper stop, deletion or process termination was necessary or
invoked. No other agent's files/processes, SDK/toolchain or shared cache was
removed. Point sample during review: 32,008,248 KiB available disk and
43,145,556 KiB MemAvailable; this is not execution admission. Prior outstanding
cleanup obligations are not discharged by this review.
