# C20 KDF known-answer: exact two-file inert delta

Author `/root/native_security_author`; 2026-09-11.
**UNAPPLIED / UNCOMPILED / UNEXECUTED.** Root released this inert preparation
after the once-only independently reviewed oracle calculation. Canonical
integration remains root-only after independent patch acceptance.

`KDF-KNOWN-ANSWER.patch.txt` is 3925 bytes/83LF, SHA256
`133aba239b8040ae23c56621ba5c02a3407bbda8a85a324d12105adfbf33d268`.
It contains only58 added lines:54 in the existing Windows security test and4 in
existing CMake. No production/native API, executable, allocator hook, fixture,
platform runner, workflow, dependency, SDK, signing or format change.

## Exact source bindings

Paths are relative to `app-desktop/native/biometric-bridge/` in W.
After-images were constructed as inert strings for these identities, not written
to canonical source or compiled. The standard unified diff reconstructs them.

| Path | Before bytes / LF / SHA256 | Proposed after bytes / LF / SHA256 |
| --- | --- | --- |
| `tests/windows/passvault_biometric_windows_security_test.cpp` | 37041 /870 /`ed5f23e50ca24d01f12c99d2800fab06ad93d42e004656676b87400ad49e588a` | 39578 /924 /`b33f12ddf18f519e83dbac57797032a25192bbeb5bf071c9d62697c763576653` |
| `CMakeLists.txt` | 9363 /230 /`976be93860deacf21db1542f26c5a4b2fbcc09d84cabba6990a0518666e409d8` | 9534 /234 /`e27e27f3c1750c88c308341273410785b72de3c79af9a25f0d3e30c295a555fe` |

Production `src/windows/passvault_biometric_windows.cpp` stays unchanged at
`a7c0c97a3b09156afe463c2b1c825bae8330ac07488e9f2667baa2dcab27f215`.

## Meaningful new oracle and narrow dispatch

- Proposed test618-667 enforces32-byte PRF/salt/hash contracts with static_assert,
  then calls the existing real-CNG `derive_wrapping_key` twice with the admitted
  public synthetic inputs. Each call must return true and match all32 expected
  bytes through std::array comparison, not the production comparison helper.
- The full literal arrays at622-631 match the root's two correctly named outputs.
  No expected byte is calculated through the implementation at test runtime.
  Original PRF/salt/vault/changed-vault and unrelated sentinel snapshots are
  checked after each result. These checks are not general out-of-bounds, universal
  erasure or provider-memory proofs.
- One actual-output array is reset to a nonmatching sentinel before each call.
  Its live ScopedArrayWipe covers all assertion returns and C++ unwinding; the
  inputs and expectations are public synthetic values. No freed-storage read,
  fault injection, resource exhaustion, real credential or logging is added.
- The helper is inside the existing non-PRK/non-historical preprocessor block.
  Normal-only main693-695 accepts exactly argc2/`--kdf-known-answer` and returns
  before argc1/random/temp/context/locale/security setup. PRK-only and historical
  binaries still reject this route; existing no-argument and other selectors
  keep their behavior. No old passing selector is added to a run selection.
- CMake193-196 registers only `passvault_biometric_windows_kdf_known_answer`,
  invoking the existing normal security executable with that one argument.
  Target flags, opt-in historical/PRK definitions and every old registration are
  unchanged. This adds one pending CTest with two fixed vector checks, not two
  native executions or another platform harness.

## Oracle provenance, not a native pass

All following paths are relative to B.

| Evidence | SHA256 |
| --- | --- |
| `reviews/team20/root/C20-KDF-ORACLE-ACTUAL.json` | `40752a01b817ff9c4fa79a0f7f0305ed59f84eff453cdf3aa769be9acfde68b3` |
| `reviews/team20/root/C20-KDF-ORACLE-STDOUT.json` | `adb3f5f07e418f7e717d1fcb030e6cf1ff10fdeca3c84b4e943f7f9e7df56e9e` |
| `reviews/team20/android_fixture_review/c20/KDF-ORACLE-SOURCE-REVIEW.md` | `58e8b21470906a6cf6c5232b30a81c95656a79407ef51220d94f087b3f83666a` |
| `reviews/team20/android_fixture_review/c20/KDF-ORACLE-ACTUAL-REVIEW.md` | `fa401c192a4a301a3b44a17107da25f2e2dccda3f098a7ce1b0c3a47f8772e83` |

Root calculated once (exit0; exact reviewed stdin; empty stderr). This author
read the exact retained receipt/stdout/reviews and did not recalculate. The
calculator's stdlib HMAC and explicit-pad paths share hashlib SHA256: independent
of the application CNG helper, not two independently verified crypto providers.

Independent full delta/dispatch challenge: `/root/desktop_other_review`.
Separate literal-only reconciliation: `/root/android_fixture_review`.
No execution is admitted by either this proposal or prospective path eligibility.
Actual Windows/CNG compatibility of the new selector remains unverified until a
fresh root-only exact source/instance/resource/retention/cleanup admission and
independently reconciled execution; old Windows05 helpers/requests stay consumed.

No family/PVU closure or case count changes. Mac02 and24 Windows05 successes keep
their precise qualifications. Native refusal/caller cuts, PVU008 destruction
behavior, PVU009 latency, real JNA/native/packaged/device gaps, all original HOLDs,
PVU007 STOP, PVU011 NO RETRY, PVA02949/44/5FAIL, G7/G8 CLOSED and1017001 remain.
