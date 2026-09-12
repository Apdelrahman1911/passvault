# PVA-036 — three narrow reported-I/O-status regressions

2026-09-12 UTC. Author `/root/c20_windows_author`; independent challenger
`/root/c20_windows_review`. **INERT AFTERIMAGES; no canonical application or
execution.** Root separately requested these untested I/O-status branches after
the bounded prewrite-crash/cancel substitutes were rejected as low value.

`FILES.json` pins the three exact before/after images and `PATCH.diff.txt`.
No baseline/test correction is reapplied, and no earlier native case is replayed.

## Why an additional seam is necessary

The existing suffix/validation/protection callbacks all precede WriteFile. They
cannot reliably trigger a later write, flush or close error while retaining the
real writer handle and ordinary cleanup. Invalidating that handle, introducing a
cross-process lock/cancellation race or exhausting storage would either conflate
failures or add unnecessary risk/infrastructure. None is selected.

The patch adds one defaulted internal template I/O policy. `FileWriterWin32Io`
forwards exactly to WriteFile with `overlapped=nullptr`, FlushFileBuffers and
WindowsHandle.close. The production wrapper explicitly supplies that policy.
The short-circuit order, original create/handle ownership, close-gated deletion,
move flags, return behavior and real security callbacks remain unchanged. No
new catches, allocation, provider call, macro, library, exported ABI, dependency,
format or recovery policy is introduced into the production route. Existing
three-callback calls retain the default real-I/O policy; historical test source
and its old signature remain separate and untouched.

## Three new normal-target cases

All use `passvault_biometric_windows_security_test --file-io-case <case>`.

| Case / CTest suffix | Actual native work before injected status | Required pre-teardown outcome |
|---|---|---|
| `write_failure` | CREATE_NEW, actual handle/owner/DACL validation; WriteFile writes the exact first2 bytes, then policy returns false; no flush | One real successful close; observed same temporary ID/2-byte prefix before deletion; temporary absent afterward; original destination ID and4 bytes preserved |
| `flush_failure` | Actual full5-byte WriteFile and successful FlushFileBuffers, then policy returns false | One real successful close; observed same temporary ID/all5 bytes before deletion; temporary absent; original destination ID/bytes preserved |
| `close_failure` | Actual full write, successful flush and successful real close, then policy reports false | Caller conservatively retains the same owned5-byte temporary, does not replace destination, and returns false; fixture later removes only its synthetic tree |

CTest names are `passvault_biometric_windows_file_io_` plus the exact suffix.
They are three declarations and **zero executions**, not OS-fault demonstrations.
Definitions/dispatch sit inside the normal-only guard; neither historical nor
PRK-only executables can select them. The old eight-case and KDF branches remain
unchanged. CMake adds only those three registrations, not a target or flag.

## Meaningful oracles and compatibility safeguards

- Every case creates a fresh exclusive synthetic subdirectory beneath admitted
  TEMP/TMP, using the existing fixture and actual directory-security operations.
  Four/five fixed bytes are not an enrolled envelope, real vault or user secret.
- The initial destination snapshot closes its original read handle **before**
  invoking the writer. It cannot conceal a mistaken rename with a sharing lock.
- Validation captures actual temporary file ID/volume/creation time and empty
  single-link state. No fake CREATE_NEW or substituted filesystem is used.
- The close callback first closes the real writer handle, then uses one original
  read handle to bind exact ID/size/bytes and explicit close before returning to
  production deletion. It cannot keep a read handle alive to block cleanup.
- Call order1235 or12345, exact write/flush/close counts, successful underlying
  statuses, actual transferred size and same-object snapshots must all hold.
  A skipped callback, failed setup, earlier accidental error or policy bypass
  cannot count as the intended passing control.
- Destination content/identity and exact directory membership are checked before
  fixture teardown. Write/flush controls require no temporary; the close-status
  control requires precisely the originally created retained temporary.
- Successful terminal output records each case, status-injection qualification,
  temporary/destination IDs and completed fixture cleanup. Original whole-helper
  retention/settlement/cleanup and final source qualification remain separate.

## Evidence ceiling and proposed batching

These are **mixed real-Win32-file plus deterministic reported-status controls**.
The OS is required to succeed at the selected underlying calls before the policy
reports the chosen error. In particular, the close case is not a real failed
CloseHandle, leaked live handle or kernel fault. No naturally occurring disk
error, power loss, arbitrary crash, actual enrollment cancellation/compensation,
provider lifecycle, physical Hello or broad durable atomicity is demonstrated.

Subject to exact independent acceptance, root may compile the existing normal
target once and run just KDF1 plus these3 new cases in one non-publishing CI job.
Do not add old successes or an unrelated JNA/native lane to that four-case batch.
Actual compiler/selected-image/argv/XML/source/cleanup review is still required.
No family closure or historical Windows05/G7/G8 cleanup discharge follows.
All STOP/NO-RETRY/native-refusal, protected-ref/tag, PVD, dependency/version/identity,
signing/Store and occupied1017001 boundaries remain unchanged.
