# Independent Windows native test-scope challenge

Reviewer: `/root/native_review`; implementation/workflow author: `/root/native`.
Status: **SOURCE SCOPE SUPPORTED; EXECUTION NOT ADMITTED**. No compilation,
CTest, native/provider operation, application launch or helper import/execution
was performed. This reviewer changed no native source, tests, CMake or workflow.

## Exact source and scope

Containing observed handoff commit:
`9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`; tree:
`05014e9f635131d5db06701e4013b4b5a746465a`.
Application inputs below are unchanged at the observation. Other agents' new
continuation reports are not part of this native-source identity. These are
SHA-256 byte identities, not semantic coverage or target-runtime results.

All paths are relative to `app-desktop/native/biometric-bridge/`:

| Path | SHA-256 |
| --- | --- |
| `CMakeLists.txt` | `5db2d9b77a00a8308c00843ec28c5fa727e01fb292c34bf96b3e3b109bee2b23` |
| `include/passvault_biometric.h` | `dc76bea46e1abc0a2950f55b549fe68cf1399d256756e85ad0a79ef52b228b85` |
| `src/windows/passvault_biometric_windows.cpp` | `8ae2d6294ca523c763bf055bb3acee755b63e0867f7fb98e3926980c74fe6630` |
| `src/windows/passvault_biometric_windows.rc` | `13ef2e1a88a6a8d6c7f7e4ce8b679cdf75f32a8ff35e6883875c3bdc6295a39c` |
| `tests/passvault_biometric_abi_test.cpp` | `354d7e1904d46a2155774052ce20c476763c97f9ed293b94c4bce54208fa3785` |
| `tests/windows/passvault_biometric_windows_security_test.cpp` | `15d1f00afbd5f5be78c5abcfe7f8a6ea74ebbe15496154849a490a77138ab633` |

CMake (179 lines), ABI header (120 lines), resource (33 lines), ABI test (28 lines) and
Windows test (549 lines) were displayed in full, with truncated middle test
display separately recovered. Windows implementation inspection was bounded:
1–859, 1040–1080, 1200–1225, 1500–1528, 1618–1660, 1910–2100, 2140–2475,
plus declaration/provider-entry searches. Supplemental admission-stage reads
checked AES/envelope helpers, context defaults and the pure struct/status
predicates rather than assuming their names established provider exclusion.
No whole-file or whole-project semantic review is claimed.

## Meaningful oracles and attempted counterexamples

### PVA-036: eight file cases

CTest names are `passvault_biometric_windows_file_` followed by exactly:
`success`, `validation_failure`, `dacl_failure`, `collision`, `empty_payload`,
`oversized_payload`, `empty_suffix`, `rename_failure`.

The test includes the actual implementation (test line 4). Dispatch at 313–316
returns before general security/provider paths. Every fixture exclusively
creates a random synthetic child of `temp_directory_path` (100–114); real Win32
file creation, handle sharing, reads, replacement and cleanup surround three
explicit deterministic suffix/validation/DACL seams (162–183). Production
`write_secure_file_atomic` supplies real operations (implementation 710–715).

- Failed exclusive creation grants no ownership (670–673). The collision oracle
  first proves DELETE access then closes that probe (test 132–139), avoiding a
  false pass from an open probe that would block the historical wrong delete.
  It then asserts retained sentinel content and exactly two files before
  teardown (203–205).
- Validation/DACL failure takes the actually-created share-zero handle and
  requires close before DeleteFileW (674–680). Tests observe a valid empty
  handle, expected call counts, unchanged destination and no temporary before
  fixture cleanup (169–209). Teardown cannot manufacture that assertion.
- Rename failure is real destination sharing denial, not a fake MoveFileExW;
  closing that blocker permits a subsequent positive replacement (217–221).
- Empty/oversized data and empty suffix check precreation guards; the positive
  control includes the exact maximum envelope size (16 KiB).

These are eight authored cases, **zero newly executed cases**. Source reasoning
supports discrimination of the recorded three historical failure branches,
not an executed original-fail/corrected-pass comparison. No historical
instrumented source/helper is authorized for replay.

### PVA-037: four guard cases

CTest names are `passvault_biometric_windows_secret_` followed by exactly:
`normal_scope`, `early_return`, `allocation_exception`, `nested_exception`.

Dispatch at 318–322 precedes all filesystem/provider fixtures. The actual
noncopyable/nonmoving, noexcept borrowed-array guard (implementation 70–85)
is tested on still-live synthetic arrays. Static type-trait assertions alone
are not counted as cases. Runtime oracles at test 301–308 inspect every byte
of the owned arrays and an unrelated nonzero array after normal scope, early
return or explicit `std::bad_alloc` unwinding. Nested unwind populates all three
secret arrays; the first allocation-cut model deliberately leaves wrapping
key zero. No freed-memory observation or allocation exhaustion occurs.

Source guards are registered before secrets at implementation 253–254,
1955–1960 and 2190–2195. Caller output is separately owned and not wiped by a
borrowed local guard. An explicit exception in a synthetic lambda is not actual
WebAuthn/CNG allocator-cut injection or proof of successful authenticated VEK
retrieval. PVD-003 universal-memory-erasure limitations are unchanged.

### Proposed two sibling compatibility registrations

`/root/native` proposed adding exactly `passvault_biometric_abi` and
`passvault_biometric_windows_security` to the same one-shot invocation. This
review supports their source-level safety **subject to root's explicit expanded
admission**; no silent extension from 12 to 14 is assumed.

- ABI test checks the real DLL ABI version, resolves both additive localized
  symbols and tests null-context rejection plus output clearing. Valid reason
  parsing then null-context guards (2393 and 2411, after 2456 output wipe) return
  without enrollment, authentication or private state. A test-linked unsigned
  DLL is an intermediate, not a packaged/production artifact.
- General security test exclusively creates a fresh synthetic root and asserts
  metadata absence (339–341). Valid localized retrieval returns NOT_ENABLED at
  implementation 2028–2029 before availability (2044), credential inventory
  (2050) or WebAuthn entry (2077). Malformed-reason tests are discriminating:
  expected INTERNAL_ERROR differs from the valid NOT_ENABLED control. Destroy
  has no active operation/cancellation ID here, so cannot call native cancel.
- The remaining general suite uses real CNG HMAC/AES on synthetic envelopes,
  tamper/malformed rejection and pure credential/authenticator struct predicates.
  It does **not** enumerate/create/delete an OS credential. This supports a
  roundtrip/tamper control, not an independently fixed known-answer KDF vector.

Existing workflows' trigger headers do not run on the dedicated continuation
branch push. No reusable release/signing workflow is included by this proposal.

## Admission conditions still required

Fresh exact workflow/helper/command review, branch/request-only once trigger,
two-sided source identity, toolchain/architecture/exception configuration,
one owner across local and CI, 12 GiB/25% launch and 8 GiB/20% running floors,
bounded logs/time/output, isolated TEMP/TMP, owned-worker settlement and safe
allowlisted cleanup must be accepted before any invocation. On Windows,
compiled Release evidence must establish C++20, MSVC/SDK, x64, exception unwind
and preserved warning/hardening flags; source options alone do not prove the
effective compiler command. No signing/store secret, install/package/upload or
mobile candidate build is needed.

The existing test fixture's `remove_all` operates only after its exclusive
synthetic-root creation. This is a cooperative owned-namespace test, not a
hostile same-user/path-replacement sandbox. Fresh runner cleanup must not
expand that ownership to arbitrary TEMP, source, shared cache or toolchain
paths. Interruption, missing XML, compile failure and cleanup ambiguity cannot
become PASS or automatic retry authority.

PVA-010 native in-flight lifetime and PVA-014 actual prompt rendering remain
BLOCKED. Hosted Windows is not physical Windows Hello evidence. PVA-036/037
remain target-verification-blocked until meaningful executed evidence is
independently reconciled; no family closure is claimed by this report.

PVU-007 STOP; PVU-011 NO RETRY; PVA-029 recorded FAIL/no automatic retry;
G7/G8 execution/recovery/cache/helper scopes CLOSED. The archived runner was
not executed/imported. Gradle stop/large-output cleanup is NOT_APPLICABLE to
this source-only review; no build workers or temporary build products were
created. Only this compact permanent review is written.
