# C20 KDF-ORACLE — independent inert-source review

Reviewer: `/root/android_fixture_review`; 2026-09-11.
W = `/root/projects/PassVault/passvault-linux`; B = W/`docs/audit-continuation/2026-09-08-linux`.

**ACCEPT the exact calculator source for separate root-only invocation binding.**
This is source acceptance, not execution admission, generated-vector verification,
Windows test success, patch promotion or native-family closure. No expected output
was computed by this reviewer. Root retains invocation and integration authority.

## Exact inputs independently read

Whole-file hashes bind bytes; semantic coverage is only the stated ranges.

| W-relative input | SHA-256 | Read scope |
| --- | --- | --- |
| `app-desktop/native/biometric-bridge/src/windows/passvault_biometric_windows.cpp` | `a7c0c97a3b09156afe463c2b1c825bae8330ac07488e9f2667baa2dcab27f215` | 1-90,180-307 |
| `app-desktop/native/biometric-bridge/tests/windows/passvault_biometric_windows_security_test.cpp` | `ed5f23e50ca24d01f12c99d2800fab06ad93d42e004656676b87400ad49e588a` | 242-273,613-651,730-790 |
| `app-desktop/native/biometric-bridge/CMakeLists.txt` | `976be93860deacf21db1542f26c5a4b2fbcc09d84cabba6990a0518666e409d8` | hash only; full test/CMake/dispatch review belongs to desktop_other_review |
| B-relative `reviews/team20/native_security_author/c20/RESIDUAL-MAP-AND-PROPOSAL.md` | `0f6fb05e54845735d7b1716837733ffa112dccb463fb86ce7e7c41f0614f1025` | complete 197-line proposal |
| B-relative `reviews/team20/native_security_author/c20/KDF-ORACLE-PROPOSAL.md` | `b54d8e4447c7747183aa0578106bf3b893c6933644a2ad63a7a4645f1c25b228` | complete 82-line proposal |

The sole fenced Python source, proposal lines17-50 inclusive, was independently
hashed as inert text: **34 LF / 1583 bytes**, SHA-256
`4ef1bec5271a9a6da9a40d779084843f07444cc11551ce98b3980e680f8fbcf2`.
The fences, Markdown and line numbers are not stdin. Final LF is included.

## Algorithm and byte contract

- This is Windows Hello wrapping-key derivation, not password/PBKDF2/Argon2,
  and no cross-platform-equivalence claim is made.
- Production51 supplies the exact 45 ASCII bytes
  `PassVault Windows Hello vault-key wrapping v1`. Production285-288 excludes
  its C-string NUL, appends the raw vault-hash bytes, then binary byte `0x01`.
  The Python bytes literal/concatenation agrees: not hex text, ASCII `1`, a
  length prefix, an extra delimiter, a second block or a trailing NUL.
- Production230-269 opens SHA256 with the CNG HMAC flag. Its key arguments feed
  BCryptCreateHash; its data arguments feed BCryptHashData. Production290-293
  therefore does **PRK = HMAC-SHA256(key=salt32, data=PRF32)**, followed by
  **output32 = HMAC-SHA256(key=PRK32, data=domain45 || vault32 || 01)**.
  Python hmac.new arguments and digest extraction agree; output is not truncated.
- Literal public inputs are PRF `47..66`, salt `33..52`, vault `01..20` in hex,
  each32 bytes. Python range endpoints are exclusive. The second vault is only
  `vault[0]:01->00`; bytes1-31, PRF, salt, domain and counter are unchanged.
  Each second HMAC message is78 bytes. JSON hex encoding occurs after derivation.
- Production kHashBytes/kSaltBytes are32; kPrfBytes names the external
  WEBAUTHN_CTAP_ONE_HMAC_SECRET_LENGTH constant. No SDK/header probe was performed.
  The proposed test must enforce its promised fixed32-byte contracts (including
  PRF) before any eventual accepted Windows execution; do not infer an observed
  target constant or compilation from this source review.

## Non-circular oracle and boundedness

The calculator neither calls, imports, parses nor reads the implementation/test
under review, and never obtains an expected result from native roundtrip output.
Current test763-782 derives on both sides of AES roundtrip; test254 checks only
changed/nonzero output. Neither is an independent fixed known answer.

For these known32-byte keys, explicit_hmac pads with32 zero bytes to the SHA256
64-byte block, XORs every block byte with36/5c, and computes
SHA256(opad || SHA256(ipad || message)). This is the correct short-key HMAC
construction. Long-key normalization is unnecessary for exactly these inputs.
The expansion cross-check uses its separately reconstructed PRK. Both methods
share hashlib SHA256; they are **not two independent SHA implementations**, a
CNG/provider validation, or an independently published vector.

The fixed two-iteration loop makes four hmac.new calls and four explicit HMAC
calls (eight explicit hashlib.sha256 calls), with fixed32/78-byte messages and
two JSON records. Pad comprehensions are bounded to64 bytes each. There is no
input-dependent search, randomness, application/runtime inspection, external
input, dynamic evaluation, subprocess or network operation in the source body.
Imports are only hashlib/hmac/json; their normal stdlib loading is not a claim
that interpreter execution performs no filesystem reads. The body performs no
file writes or source reads and emits only the compact public-synthetic JSON.

Proposed `python3 -I -B -S -` retains assertions, ignores Python environment
configuration, excludes user-site/current-directory import additions, disables
site initialization and bytecode writes. This review does not resolve/admit the
interpreter executable or its environment. Root must separately bind the exact
existing interpreter/argv and the reviewed stdin; no -O or source substitution.

## Required next evidence / ceiling

Root may invoke only under its own admission and retain exact stdin identity,
argv, exit/stdout/stderr and proposal identity. Require exit0, no unexpected
stderr/output, exactly the two named full32-byte results, and successful built-in
agreement/distinctness assertions. Failure is not a valid vector or retry
permission. This review has no resulting output identity or test literal claim.
After a result exists, independently reconcile its inputs and both64-hex outputs
with the proposed literal arrays, then review the exact test/CMake delta. Full
Boolean-return and literal-byte assertions must not become runtime regeneration,
nonzero-only checks or another native roundtrip. Expected source must remain
independent of production kKdfInfo. Future Windows execution is separate.

No source edit, helper/application import, AST parse, calculator invocation,
vector calculation, build/test, SDK/provider/process probe, Git, network, held-R
access, cleanup or central-ledger change occurred. Only bounded inert reads,
identity hashing/counting and this own report were performed. Native refusal,
Windows05 filesystem HOLD, prior consumed failures, all STOP/NO-RETRY/CLOSED
boundaries, GUI/focused source-after holds and occupied1017001 remain unchanged.
No new worker, runtime namespace or stop obligation was created.
