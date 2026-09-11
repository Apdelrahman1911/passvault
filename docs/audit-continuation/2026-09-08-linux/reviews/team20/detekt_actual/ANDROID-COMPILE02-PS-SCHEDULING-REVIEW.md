# Optional Compile02 scheduling metadata check

Reviewer: `/root/detekt_actual` · 2026-09-11

**ACCEPT_OPTIONAL_ONE_FOREGROUND_PID_COMM_SCHEDULING_CHECK_ONLY.**
No execution was performed by this reviewer. No build, process ownership,
cleanup, current-reference absence or Compile02 instance admission is granted.
Consumed GUI03/observer01 and all held-runtime fences remain unchanged.

## Fixed proposal and interpretation

Root may optionally invoke exactly `/usr/bin/ps -eo pid=,comm=` once in the
foreground, retaining only the requested PID/comm metadata and complete tool
result. No argv/env/cwd/fd/exe fields, held-runtime reads, new runner/harness,
process-signalling action or broader survey is added. This reviews the requested
command/output contract, not an audit of procps binary internals.

Accept a scheduling observation only from **complete exit0 output, no remaining
session or truncation, and full retained stdout at most65,536 bytes**. A token
limit alone is not proof of that byte bound/completeness. Require nonempty,
well-formed PID/comm rows with unambiguous fields/line boundaries and no duplicate
PID; malformed/control/ambiguous rows, warnings, missing data, nonzero exit or
oversize/truncated output mean **DEFER_ROOT_BUILD**, not a silent skip. Do not
expand into private fields to resolve ambiguity. Parse data directly after
completion; do not execute output or import the outer helper.

Use the exact **case-sensitive `fullmatch`** BUILDLIKE pattern from reviewed outer
lines71–73, not substring matching or a substituted observer01 matcher:

```text
(java|javac|gradle.*|Gradle.*|kotlinc.*|kotlin.*|Kotlin.*|xcodebuild|clang.*|gcc.*|g\+\+.*|cc|c\+\+|cc1.*|cmake|ninja|make|gmake|ctest|mvn.*|msbuild|dotnet|pytest.*|cargo|rustc|jpackage|jlink|aapt2?|d8|r8|zipalign|adb|emulator.*|qemu-system.*)
```

- Any matching row: **defer root's build**; PID/name is neither owned nor killable.
- Any ambiguous result: **defer**, with no automatic broadening/replay here.
- Complete negative list: only **no regex match in this finite visible point
  list**. It is not global/continuous idleness, namespace ownership, absence of
  interpreter-hidden builds, cleanup proof, or permission to launch Compile02.

## Existing outer gates and efficiency value

Source read as data: `B/reviews/android-compile02-outer/LAUNCH.py`,64,272 bytes,
SHA256 `153a4c6f19b61c940fc30d8f8819ee65b70af5803a28bf5252ebe9d2672d6c48`. Focused source/policy review only; no helper import/execution/AST,
process/R/T/SDK probe or new test was performed.

The source takes the original nonblocking lock at line865 and performs authority
checks plus **`watch(entry=True)` at877–878, before E/R allocation at887–888 and
before any child**. That entry watch combines12GiB/25% resource floors with the
pidfd-backed exact BUILDLIKE screen. At entry there is no original owned child,
so a positive competitor cannot receive an owned-domain exemption.

`drive()` calls `tick()` before spawning. `tick()` and the active-child supervisor
refresh watch when at least5s have elapsed; running floors are8GiB/20%. This is
cooperative periodic checking, not an instantaneous scan before every spawn,
continuous idleness or a hard syscall bound. Candidate observation does not
signal unrelated processes; existing original-child cancellation supervision
is a separate admitted mechanism.

**The optional ps check adds no required safety gate.** Existing entry checks
already reject a visible conflict before expensive Gradle compilation or runtime
allocation. A cheap pre-scheduling list can nevertheless avoid consuming a
one-shot admitted launcher/packet when a competitor is already obvious. It may
be skipped as an efficiency choice, but once used its positive/ambiguous result
must not be treated as negative or used to bypass the original gates.

This outer remains **partially bound / instance-UNBOUND**; its device/lock/exclude
and genuine instance prerequisites still prevent admission. Neither this review
nor a negative ps list fills those bindings. The final exact instance must retain
its own independent approval, original under-lock watch and running checks.
