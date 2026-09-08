# Root admission — one Windows native-14 request

2026-09-08, root `/root`, continuation branch only. This admits the exact
independently accepted packet below, **not** a successful run or a closure.

- Helper SHA-256: `67bcd0aeaa03fa829d55fecfbd640d185163805e4c0a067b281bd807333af243`.
- Workflow SHA-256: `643fcba6cb393bd6bbd8497cfd03a52ccc43fa217a9d1d728c453d29ddb87b8c`.
- Source bindings SHA-256: `27152712c1ac99ea1fcf739210764b0a5cabf8f45f1edbec2ecb3b143d2ebcb9`.
- Independent acceptance SHA-256: `7f4d33ff3e4860c9a28b2bf45b51d2957f3f049b88b422466fdd6ccaec94db63`.

Root read the complete helper and workflow, the independent acceptance and
source-scope challenge, and the command/environment/cleanup contract in
`WINDOWS_ADMISSION.md`. The latter's exact command vectors and fixed ordered
14-case inventory are incorporated without change. The source commit/tree
will be the containing commit; the request-only direct child binds them
explicitly before activation. The independent reviewer will check that actual
committed pair and child before root pushes the activation.

## Essential scope and limits

PVA-036's eight writer cases and PVA-037's four synthetic array-guard cases
need real Windows APIs and MSVC exception unwinding; Linux cannot provide that
evidence. The two reviewed ABI/CNG siblings exercise compatibility, not Hello
enrollment or a physical-device prompt. This is an unsigned, native-only
validation: no Gradle, app packaging, signing, store access, release candidate
or occupied build 1017001 is involved. Historical red controls, production-cut
allocation injection, PVA-010 native concurrency and genuine hardware remain
outside this batch. A pass is not a family closure.

Use only preinstalled windows-2022 x64, Python >=3.11, VS2022, CMake/CTest >=3.25
and Windows SDK 10.0.26100.0. Missing requirements fail without installation or
fallback. One worker; 900-second command budget, 20-minute job bound; owned
Windows Job limits 3 GiB and 16 children; sampled 12 GiB/25% launch and
8 GiB/20% running floors. Attempt 1 only, no automatic retry.

Original handles and cleanup are installed before native commands. Every
command's owned Job must settle before another starts. After evidence capture,
only the handle-bound, allowlisted generated namespace is removed. Retain
compact logs, per-case XML, journals and hashes, never DLL/EXE/PDB binaries;
artifacts expire after three days. Cleanup failure/interruption is HOLD, not
inferred success from runner disposal. No historical recovery scope is reopened.

## Cross-host coordination

Root reserves the sole audit-owned local/CI build/test slot for this request
in `../../EXECUTION_SLOT.json`. All agents remain source/review-only; root will
not launch local builds, tests, emulators or other CI while it is reserved.
The reservation is held through terminal CI state **and** independent
result/cleanup reconciliation. Git/source inspection and small report writes
are not competing builds. Actions has a constant audit concurrency group and
does not cancel other runs; only the reviewed one-path request push can start
this workflow on the dedicated branch.

At 21:50:32 UTC, a fresh GitHub API observation found zero Actions runs on the
continuation branch. No audit-owned local builds/workers had been launched;
a process-name-only point sample found no Java/Gradle/native/emulator/ADB
matches. This is a sampled observation, not a global host lease. The VPS
recently had ~28 GiB free and ~43 GiB available RAM; Windows independently
checks its actual hosted resources before each command.

The same fresh ref observation confirmed main/testing/release/handoff and the
candidate tag unchanged; continuation remote was
`488ab465125234e02bfbd28b3e7579251341cb54`. Only remote-tracking refs were fetched.
No protected branch/tag or unrelated work is modified.
