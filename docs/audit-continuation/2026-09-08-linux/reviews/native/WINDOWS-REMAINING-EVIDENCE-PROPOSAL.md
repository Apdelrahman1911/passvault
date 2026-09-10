# Remaining Windows evidence: bounded source-only proposal

2026-09-09; author `/root/native`, independently challenged by
`/root/native_review`. Verdict: **worthwhile partial evidence, conditionally
feasible; NOT implementation acceptance, execution admission or closure**.
Windows04's frozen current-source 14 cases must run first under root's existing
admission. No native/test/CMake/helper/workflow/selection edits, baseline
retrieval/reconstruction, execution, Git or network action by this agent occurred.
Root subsequently supplied the historical image for inert source reads only.

| Finding / missing evidence | Bounded next work and discriminating controls | Remaining boundary |
| --- | --- | --- |
| PVA-036 historical comparison | Prefer a separate writer-only test target against the **exact retained full instrumented historical source image**, only after root's new admission. Reuse the eight existing real-Win32 synthetic fixtures: validation failure, DACL failure and collision must reach their particular decisive staging-absence/sentinel assertion with every earlier precondition true; success, empty/oversized payload, empty suffix and real rename denial must pass. Preserve acquired-handle/callback checks, the closed DELETE probe, exact directory membership, original destination bytes, and successful cleanup/terminal outcome. Maximum-size and post-blocker replacement controls must also use the selected historical writer. | No Hello/hardware is needed. The supplied image's exact identity/old branches are reconciled below, not compiled or execution-admitted. A crash, setup failure, timeout, arbitrary nonzero exit or expected-failure inversion is not a qualified red. Strict historical-only dispatch must reject no-argument, guard and provider routes, excluding tests that require newer declarations. This is instrumented source, not a shipped historical binary. |
| PVA-037 production PRK allocation cut | One synthetic direct call to actual `derive_wrapping_key`, using real CNG, can supplement the four current live-array guard units. `info.reserve` precedes PRK population; only the **second** HMAC's `hash_object` vector allocation follows successful extract. Arm after that HMAC's successful BCrypt open/property guards, immediately at the real vector construction, matched to the same live PRK pointer, call sequence and object size. Require one actual matching allocator interception, disarmed before throwing and on every escape; unexpected/duplicate allocations or library differences invalidate qualification. Read-only, allocation-free, `noexcept` observers inside the production guard destructor must record the exact live PRK pointer/length, nonzero-before/zero-after state and event counts; the catch reads saved flags only. | No Hello/hardware is needed for this **PRK-only** cut. A callback throwing before vector allocation or a process-wide nth-allocation arm before CNG is not equivalent. Require an unarmed successful derive/reachability control, caught target `bad_alloc`, unchanged input/unrelated canaries and fault-path caller output, valid success-path output, and final disarmed state. Normal derive explicitly tail-wipes PRK before its guard destructor, so its populated-state control belongs at the second-HMAC site, not the normal destructor. A successful roundtrip is not a fixed KDF vector. |
| PVA-037 caller PRF/wrapping arrays | Keep create/retrieve cuts separate. Their guards occur after real availability, operation/cancellation, inventory, WebAuthn and result/signature/authenticator gates; post-copy AAD and post-KDF AES allocations are not reached by current metadata-absence/null-context controls. | The PRK test cannot close these caller-array gaps. An additional fake-provider seam/refactoring could provide narrower synthetic evidence, but is outside this proposal. Genuine authenticated protected-PRF release/unwrap still requires distinct provider/device evidence; hosted Windows is not physical Hello proof. |

**Prospective ownership, only after actual14 and fresh root approval:** native
may modify at most these three current code paths:
`tests/windows/passvault_biometric_windows_security_test.cpp`, `CMakeLists.txt`
and `src/windows/passvault_biometric_windows.cpp`, relative to
`app-desktop/native/biometric-bridge/`. The source change would be compile-time,
test-only PRK observation/target-site hooks, absent from the normal DLL/ABI;
single-thread fixed-POD armed state must perform no allocation or logging.
Use existing CMake, SDK/CNG and dependencies with preserved hardening/unwind
flags. **The retained historical image is an additional executable-source
input**, even if stored as evidence; this is not a claim of at most three total
inputs. If root requires that stricter ceiling, a body-only writer-unit control
needs separately reviewed exact extraction/symbol adaptation bound to the
passively matched supporting dependencies below. Do not
embed 96 KB into a test to evade the count, transplant unverified history, or
replace production source. Reviewer must challenge the actual future patch;
root alone owns any new selection, execution, coordination and cleanup admission.

## Exact source and historical bindings

All identities are SHA-256 bytes, not executed coverage. Native source/test/
CMake were passively read and independently rehashed by the reviewer. Historical
report/image copies and root's two inert-capture receipts are under this
document's directory.

| Input | SHA-256 |
| --- | --- |
| Current Windows implementation | `8ae2d6294ca523c763bf055bb3acee755b63e0867f7fb98e3926980c74fe6630` |
| Current Windows security test | `15d1f00afbd5f5be78c5abcfe7f8a6ea74ebbe15496154849a490a77138ab633` |
| Current CMake | `f72420bc39a5c5dad514b60eb0583880fc329e0a1a57a9b5dd12c8eeeb3c54e3` |
| `HISTORICAL-PVA036-PATCH-REVIEW.json` | `18cdb223f9fb2b7d9eec7c5a9677ff7f304c5e98594b03bddc3f2e6921dac6ab` |
| `HISTORICAL-PVA037-GUARD-REVIEW.json` | `ce86e18c138d82b5639dcf005e3ec7bf520fe434772991d04eef8f1dd5fab9ff` |
| `HISTORICAL-PVA036-037-CAPTURE-RECEIPT.json` | `311b5c750f45765a7848b1cc5d56fa25bbe85fe2321c8621647ebdf5d02638e9` |
| `HISTORICAL-PVA036-WINDOWS.cpp.txt` (96,218 bytes / 2,539 LF) | `4f041972054d403dba6ddef5f4971c9614aa0c8f9ba0f054dc7e89289bf8016b` |
| `HISTORICAL-PVA036-WINDOWS-CAPTURE-RECEIPT.json` | `75f52ed88da104950ce038390e430c9d099fbf8d7a2b072f549d1aa1a1f5efff` |

Root's source receipt names original
`remediation-reports/20260905T222925Z/reviews/pva036-patch-root-v1/baseline-instrumented/windows.cpp.txt`.
Native rehashed it and read historical 387–725: writer 626–688 retains both
the combined early unowned/delete-before-close branch and old late unconditional
cleanup. Passive complete-file diff shows only the current writer correction
and PVA-037 guard additions. Historical supporting contracts 387–624 are
byte-identical to current 405–642 (`WindowsHandle`, ownership/DACL/directory,
read and suffix helpers); wrapper 682–688 equals current 710–716, and constants
are unchanged. This reduces the body-only dependency uncertainty but accepts no
extraction, adaptation or executable. The historical review separately identifies
`baseline-instrumentation-only.patch.txt` (2,287 bytes, SHA-256
`6cded6644e83589b446584b273d427b8b0e1d62b9cce8e75a2931e30a42ca61d`);
that patch provenance remains report-derived, not a fresh patch read.

No denominator changes, test counts or runtime credit arise here. Normal C++
unwind is not termination recovery, actual memory exhaustion, provider-object
ordering/PVU-008, freed-memory inspection or universal/physical erasure.
PVD-003 and all other design/owner decisions remain separate. PVU-007 STOP,
PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED, hardware and
publication restrictions remain unchanged. A prior bounded index search
unexpectedly emitted/truncated a minified large line; it was disclosed to root
and not repeated, and exact report reads replaced reliance on that output.
No temporary runtime products or owned background workers were created; no
runtime cleanup was invoked or needed for this source-only note.
