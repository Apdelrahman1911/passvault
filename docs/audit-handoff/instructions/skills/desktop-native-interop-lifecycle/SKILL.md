---
name: desktop-native-interop-lifecycle
description: Design, implement, diagnose, or audit JVM Desktop integration with native C, C++, Objective-C, WinRT, JNA, JNI, CMake, packaged libraries, and blocking OS APIs. Use for ABI design, ownership, architecture-specific builds, secure native loading, cancellation, window lifecycle, app shutdown hangs, or Gradle packaging tasks that differ by operating system.
---

# Desktop Native Interop Lifecycle

Keep the JVM/native boundary explicit, bounded, architecture-correct, and safe throughout startup, use, cancellation, and shutdown.

## Stable invariants

1. Publish a versioned C ABI with fixed-width types, exact bounds, and explicit ownership.
2. Never free a native context while a call can still use it.
3. Permit only one operation when the native provider is not concurrency-safe; reject duplicates without a backlog.
4. Load only an expected app-owned library after path, symlink, architecture, ABI, and integrity validation.
5. Build and test on every claimed target architecture.
6. Make terminal shutdown bounded and fail-closed without weakening ordinary lock/cleanup behavior.
7. Register host-specific Gradle tasks lazily so unrelated hosts still configure.

## Inspection

Trace:

- ABI headers, exported symbols, structure layout, encoding, calling convention, and version negotiation;
- allocation/free responsibility for every pointer and callback;
- managed, direct, and native copies of sensitive buffers;
- thread affinity, blocking calls, callbacks, cancellation, and stale operation IDs;
- CMake/toolchain targets and Gradle task dependencies;
- staged development paths and installed runtime paths;
- loader fallback behavior, symlinks, permissions, checksums/signatures, and architecture;
- window close, tray/menu removal, prompt cancellation, database closure, clipboard cleanup, JVM hooks, and process exit.

## Implementation workflow

1. Define the ABI and write failure-first contract tests before binding it.
2. Wrap sensitive inputs in bounded off-heap buffers and clear them in `finally` paths.
3. Give every asynchronous/blocking operation an opaque ID. Route cancellation only to the owning operation.
4. Keep a context alive until the native call returns; use deferred destruction for late completion.
5. Stage one target-specific library plus a small manifest containing ABI, platform, architecture, filename, integrity policy, and hash.
6. Reject global search paths, temporary extraction fallbacks, malformed manifests, wrong architecture, and symlinks.
7. Make platform tasks conditional/lazy. Do not eagerly resolve a macOS packaging task on Windows or Linux.
8. On terminal close, conceal UI first, then run independent bounded cleanup boundaries. If cleanup exceeds the documented terminal deadline, terminate rather than leave secrets and a hung visible process.

## Verification

- Compile and run native tests on each target runner.
- Test wrong ABI, missing symbols, malformed sizes, allocator/free failures, double-free prevention, callback-after-cancel, and concurrent attempts.
- Inspect architecture with platform tools and reject mixed/wrong binaries deliberately.
- Modify the staged library and manifest independently and prove loading fails before native execution.
- Launch the exact packaged image, exercise the native operation, close during an active call, and assert no leftover process.
- Capture thread dumps/hang samples for failures; distinguish an idle UI run loop from shutdown threads waiting on locks.
- Run configuration tests from every supported host to expose eager task lookup.

## Failure lessons

- Cross-compilation success is not target-runtime proof.
- Ownership ambiguity becomes crashes, leaks, or secret retention.
- Cancellation can race native entry before the first instruction.
- Clearing a cancellation flag too early can poison the next operation.
- A normal development run does not prove packaged resource layout.
- JVM shutdown hooks can deadlock behind UI/native cleanup; terminal deadlines must be explicit.

## Resources

- Run [`scripts/inspect_native_library.sh`](scripts/inspect_native_library.sh) to collect a non-mutating path/type/hash/architecture report.
- Read [`references/interop-checklist.md`](references/interop-checklist.md) for ABI, ownership, loader, and shutdown review tables.

