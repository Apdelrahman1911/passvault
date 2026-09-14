# PVA037 real-caller allocation cut01 — boundary only

**No permitted real-caller test afterimage is available under current scope.
No test code, provider/refactoring seam, production change or runner was authored.**
Root was notified of the bound and explicitly directed preserving it and moving to
an updated owner brief. This is not a new defect, test, closure or hardware-only verdict.

## Smallest missing cut and why it cannot be substituted

The earliest missing creation cut is actual `create_windows_hello_credential`2001
calling `encode_envelope_aad` reserve810–814. Its real caller PRF array is populated
(1995–1998), while the wrapping array remains zero (1999–2000). It would not prove
populated-wrapping erasure. Retrieval2236 is a separate caller with separate guards.
The later create/retrieve AES object allocations330/383 require successful KDF and
AES setup; they do not bypass the caller/provider gates. Later creation allocations
likewise remain behind the same prerequisites. Ciphertext assign335 is not gated on
successful key generation; no stronger or exhaustive reachability claim is made.

Current production is still99579B/
`93746b0cdb399aa17dd8c3c915e907244d4e86e5b566c51714ccc7f80310bd5d`.
Its only PVA037 registration/target hooks (70–80,90–96,249–255,281–283) concern the
**derivation-local PRK and HMAC allocation**, not caller PRF/wrapping ownership or
the AAD/AES allocation sites. Current test120–195 requires that exact registered
PRK pointer; another32-byte array is not equivalent. Test203–263 invokes the real
KDF helper directly, not a production create/retrieve caller.

Create first crosses availability/operation/cancellation1815–1835, real WebAuthn
creation/result/authenticator1911–1957 and inventory1975–1989. Retrieve adds valid
metadata/inventory2065–2101 and real assertion/identity/signature2165–2218.
`WebAuthnApi`1263–1403 owns private system-DLL pointers; availability1510–1544 uses
real WinRT. There is no current injectable caller-entry path. The retained native
proposal explicitly excluded a new provider/refactoring seam, and the current
ledger/root direction preserves that boundary. Production instrumentation edits
are also outside this test-only/preserve-C++ task.

Therefore a fake provider/refactoring, loader substitution, copied caller body,
guard-only test, direct helper call, nearby throw or approximate allocation count
would either cross the boundary or fail to test the requested real caller. None
was attempted. PVU008 provider backing-content/destroy obligations remain separate;
no provider experiment or refusal reformulation is offered.

## Current identities versus retained evidence

Current security test:75135B/
`27be38ad9ed1292b085e706f8fc4b829eb1c98dcf59e4ceb9c53957ed26c7f0c`.
Current CMake:11398B/
`5bd3de7d16958c466ad740b2c14800f2f8b81e115323ad486ecf5a08f02a036e`.
These now include separately installed PVA036 after-flush source and differ from
the older48464/9982 whole-file affinity claim. Do not copy the prior three-file equality
statement as if it described these current files. Production C++ remains identical.

Accepted guard4/actual-PRK2 and Windows4's **one** KDF1 case with **two** literal vectors
remain retained per-method evidence, not new execution or whole-current-binary
proof. No replay is needed or allowed here. Existing guard-unit selectors and KDF
source are not the absent caller controls. PVA036 after-flush implementation is
separate, installed/unrun per root; it does not fill PVA037.

## Verified delivery scope

OUTCOME.json binds three current source files, four retained authority documents
and a current PVA037 ledger-row snapshot. Only bounded source/JSON/hash reads and
this fresh owned directory were used. No project/helper import/eval/AST/compile,
native execution, CI/Git/network, provider/SDK/cache/process probe, cleanup or stop.
Zero source/test afterimages, cases, fixes, closures or owned workers/stop duties.
All PVA037 OPEN, Windows FAIL/HOLD/UNKNOWN, PVU008 restrictions, PVU007 STOP,
PVU011 NO RETRY/no inquiry, PVA029 no automatic retry, G7/G8 CLOSED, protected
refs/tags/build1017001/versions/dependencies/identities/signing/Store remain.
