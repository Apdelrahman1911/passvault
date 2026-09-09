# Required qualifications to the frozen MAC-FIXTURE-001 packet

2026-09-09; author `/root/native`; requested by independent challenger
`/root/native_review`. **SOURCE ONLY; ZERO EXECUTIONS; NO MACOS ADMISSION.**

This addendum corrects the reading of the unchanged submission report
`MACOS-FIXTURE-PATCH.md` (`c8594961cb636da41e0a7a6b97b3a4fee84b8aec10257262c970f7c0ecdd7985`)
and inputs (`3755b0e18b8e4ed94870c03621c5c022993af4ca763660e2bc01c9dae207acc9`).
The frozen source/test/CMake/README and original packet are preserved, not
silently edited. Independent final disposition must bind this addendum too.

1. **Allocation scope:** the pre-`mkdirat` allocation statement covers only
   `NativeFixture::create()`'s owned member path/name strings. Later probe
   `created_name` copying and unchanged production writer temporary-path
   allocations do occur after acquisition, under the installed fixture guard.
   This is not a claim that all later test allocations precede acquisition or
   that any exception/fatal-abnormal path necessarily settles native workers.
2. **Descriptor scope:** `FixtureCleanup.descriptors_closed` and the packet's
   close-once claims concern only the fixture's retained parent/root/biometric
   directory descriptors. They do not observe every descriptor opened by the
   unchanged production helpers: for example, `fsync_directory` ignores its
   own close result. Nor do they prove native-context or worker settlement.
   A fixture `settled` diagnostic is therefore not whole-process cleanup
   clearance. These outcomes remain part of future runner/target admission
   and result qualification, not source-only cleanup success.
3. **Complete Windows/common interval:** the original manifest's matching
   before **65–179** / after **73–187** interval begins at the Windows `.rc`
   source entry. Its hash is correct for those bytes, but it omits the
   `elseif(WIN32)`, `add_library` and `.cpp` lines and thus alone does not bind
   the *complete* Windows branch. The full inclusive-LF interval is before
   **62–179** / after **70–187**, and both hash to
   `72f7ebd4a170f4923ba16ffd213a552feee03a19aec62d4add7ed12b67499c0e`.
   The author re-read and independently reproduced this reviewer-supplied
   complete interval. Full CMake remains
   `f72420bc39a5c5dad514b60eb0583880fc329e0a1a57a9b5dd12c8eeeb3c54e3`.

No code change or new case declaration follows from these qualifications.
There are still three added source-declared fixture cases, zero executions/XML,
no new product family or qualified closure, and no Windows01 retry/input
rewrite. Caller prerequisites, cooperative-host/physical-device limits, all
STOP/NO-RETRY/CLOSED restrictions and protected publication boundaries remain
as recorded. This source-only addendum created no build/cache/temp output or
background worker and discharged no earlier cleanup obligation.
