# Independent Apple/PVA-010 source-scope supplement

Reviewer: `/root/native_review`; subject author: `/root/native`.
Disposition: **SOURCE QUALIFICATIONS SUPPORTED; NO NEW EXECUTION ADMISSION**.
This supplements, and does not expand or modify, the accepted Windows14 packet.
No project helper was imported/executed; no compiler, test, application, native
provider, simulator, CI run or recovery procedure was launched.

The separately captured `SOURCE-SUPPLEMENT.json` binds the reviewed author notes,
current product/test bytes and read ranges. These are bounded source reads,
not additional executed cases, semantic-coverage credit or family closure.

## Apple selection and attempted counterexamples

The complete 242-LF iOS attachment test supports the author's seven
source-declared methods. Real Foundation file operations surround deterministic
coroutine/protection/move seams. The cancellation oracles inspect absence before
outer fixture deletion; outer cleanup therefore cannot manufacture those
assertions. The forced-copy branch substitutes a false move result, not a
measured OS move failure. `NonCancellable` draining explicitly bounds both
dispatcher sides (142–155, 235–240), but source intent is not observed worker
settlement. Fixture deletion return values are ignored (209, 223).

The complete 24-LF iOS prompt test has one source-declared method, not two test
cases merely because it iterates English/Arabic. It configures actual
`LAContext` objects and invalidates them in `finally`; it does not display a
prompt or authenticate. These eight methods are sensible future source targets,
not a proved executable runner selection or current runtime evidence.

The complete 151-LF macOS native security test cannot safely be appended to a
future batch on the assumption that host `TMPDIR` contains it. Line 41 uses
literal `/tmp/passvault-biometric-native-test.XXXXXX`. `PV_TEST_CHECK` returns
directly on failure (8–12); many checks precede the only final directory removal
(147–148), with no whole-fixture RAII owner. `verify_bounded_busy_destroy` models
operation state with an `LAContext`, not real Touch ID authentication or the
Kotlin/JNA caller's interleaving.

Additional independently challenged fixture qualification: line 100 creates a
symlink to `/etc/passwd`. Current `read_metadata` rejects symlinks/nonregular
files via `lstat` before `open`, which additionally specifies `O_NOFOLLOW`
(implementation 156–176). **No actual `/etc/passwd` read is alleged.** Before
future execution or deliberately regressed controls, replace the target with
owned synthetic valid metadata so the oracle stays discriminating without
leaving the synthetic-data boundary. This is an admission/fixture qualification,
not a new product finding. The author independently agreed with this challenge.

Future Apple work still needs separately authored/reviewed fixture correction or
an exact safe containment contract, actual simulator namespace/toolchain/task
binding, root's sole build slot, and observed worker/storage cleanup. A host
environment variable does not establish the simulator's effective temporary
directory. Physical iPhone protection, lock/background/picker behavior and
biometric prompting remain hardware BLOCKED. No Apple workflow is admitted here.

## PVA-010 caller lifetime: correction supported, boundary preserved

The complete 404-LF implementation and complete 492-LF test file were read.
The author's four race challenges are supported:

1. Close reserves its cancellation reference while holding `lifecycleLock`
   before releasing the lock (153–170). A returning ordinary worker cannot
   destroy the context while that reservation remains.
2. A previously admitted operation still rechecks `closed` under the lifecycle
   lock in `withNativeCall` before entering native code (189–220).
3. An operation ID published after close's no-operation snapshot cannot bypass
   that recheck; its `finally` cannot repeat an already claimed destruction.
4. The cancellation reservation is released in `finally`, including a thrown
   native cancellation. The destroy predicate requires closed, zero native
   calls, no active operation, and an unclaimed destruction (245–251).

The two paused-cancellation methods (69–112) use real JVM latch/executor
scheduling around a fake `NativeApi` (260–305). They require no premature
destruction and exactly one eventual destruction, including the throwing
cancellation branch. The separate 25-ms close-wait test (168–201) returns
immediately from its fake cancellation entry; it does not prove a bound on a
blocked real `pv_bio_cancel`. Raw close cancellation still precedes the
condition-wait deadline. The separately recorded PVU-009 concern is unresolved;
no new conclusion or restricted procedure is introduced.

`shutdownNow` is an interruption request, not observed executor termination;
the fixtures do not call `awaitTermination`, and their recursive-delete return
values are ignored. Positive-path futures do not establish cleanup on every
assertion/failure schedule. The author independently agreed that any future
JVM/native execution needs separate worker-settlement/filesystem evidence.
This does not invalidate or upgrade the historical qualified fake-thread
evidence. Windows14 does not call this wrapper or exercise real in-flight native
cancellation, provider/allocator/sanitizer behavior or packaged termination.

## Boundaries and cleanup

Original confirmed closures remain **19/25**; all confirmed **22/37**; original
suspicions conclusively resolved **2/12**. No count changes arise from this
source-only review. The eight PVD design explanations/owner decisions remain
separate. PVU-007 STOP, PVU-011 NO RETRY, PVA-029 recorded FAIL/no automatic retry,
and G7/G8 closed execution/recovery/cache/helper scopes remain untouched.

Only compact permanent reviewer reports were created. No build artifacts,
caches, workers or temporary outputs were created; wrapper stop is not applicable
to this review and discharges no historical obligation. Resource observation:
29 GiB filesystem availability and about 40.8 GiB available RAM at review time;
no processes were stopped or files removed outside this report scope.
