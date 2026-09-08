# PVA-010: renewed bounded caller-lifetime challenge

Reviewer: `/root/native`, not the historical implementation/test author.
Disposition: **SOURCE CORRECTION SUPPORTED; NO TARGET EXECUTION/CLOSURE**.

Read current `JnaDesktopBiometricBridge.kt` in full (404 LF), SHA-256
`772d17b80f6e131aeed8cf763efa59cb909eb0696f777da555382b91f6113232`, and
the corresponding test source's 1–130 and 260–355 windows (492-LF file), SHA-256
`dd5e2db95fd8d8e37f90710a85285001a43ed1f128a0f92d4b978d8f4f2565b8`.
These are additional bounded source reads, not new test events or whole-project
coverage. Both paths are below `app-desktop/src/`, in their existing
`desktopMain`/`desktopTest` security/biometric package directories.

## Attempted counterexamples

1. **Worker returns while close is paused before native cancel.** Close acquires
   the lifecycle lock, marks closed, snapshots the active operation and reserves
   one native-call reference before unlocking. The worker can clear its own
   count/active operation, but the destroy predicate still sees the cancellation
   reservation. Close's finally releases it even when native cancellation throws.
   This addresses the historical missed-reference race rather than merely
   extending a timeout.
2. **Operation is admitted before close but reaches the native-call guard after
   close.** `withNativeCall` rechecks closed under the lifecycle lock before
   incrementing/entering native code. The operation finally releases its active
   ID and attempts destruction; it cannot use a destroyed context through that
   guard. `claimNativeContextDestroyLocked` requires both zero native calls and
   no active operation and records a single claim.
3. **Close snapshots no operation, then a preempted operation CAS publishes an
   ID.** The lifecycle recheck still rejects that operation before native entry;
   a previously claimed destruction is not repeated by its finally block.
4. **Native cancel throws.** The outer finally decrements the reservation rather
   than relying on the caught result. The existing paused/throwing fake tests
   explicitly require one destruction and reject destruction before cancel
   completion. They are real JVM scheduling controls around a fake `NativeApi`,
   not real C++ allocator/provider/sanitizer evidence.

## Unresolved boundaries remain separate

The raw close cancellation still occurs before the condition-wait timeout is
started; this source review does not resolve the separately recorded PVU-009
terminal cancellation/deadline concern. No new finding, severity or execution
authorization follows here, and no prohibited procedure was investigated.

The proposed Windows native14 run does not call this Kotlin/JNA wrapper or
exercise an in-flight real native cancellation. ABI loading and an idle context
destroy cannot substitute for that missing evidence. Native lifetime,
sanitizer/provider and packaged termination evidence remains BLOCKED; prior
qualified fake-thread evidence is preserved without upgrade or invalidation.
