# C20 KDF-ORACLE — literal-only patch reconciliation

Reviewer `/root/android_fixture_review`; 2026-09-11.
B = `/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux`.

**ACCEPT both exact32-byte literal correspondences in the inert proposed patch.**
No recalculation, compilation, native/platform success, canonical edit or full
patch-promotion decision was performed by this reviewer.

## Binding

Author artifacts under B/`reviews/team20/native_security_author/c20/`:

- `KDF-KNOWN-ANSWER.patch.txt`, independently read complete and hashed:
  `133aba239b8040ae23c56621ba5c02a3407bbda8a85a324d12105adfbf33d268`.
- `KDF-KNOWN-ANSWER-PROPOSAL.md`, independently read complete and hashed:
  `bd791df4e4b770df39025e6db4cbae7d7cc541c7f3ed32b2ffad2d320386c945`.

Canonical before identities were rechecked directly: Windows security test
`ed5f23e50ca24d01f12c99d2800fab06ad93d42e004656676b87400ad49e588a`;
CMake `976be93860deacf21db1542f26c5a4b2fbcc09d84cabba6990a0518666e409d8`.
The source paths are the existing files under `app-desktop/native/biometric-bridge/`.

The proposed test-after identity
`b33f12ddf18f519e83dbac57797032a25192bbeb5bf071c9d62697c763576653`
is supplied by the author and separately confirmed by desktop_other_review's
in-memory reconstruction; this reviewer did not duplicate that reconstruction.
The direct literal acceptance here is bound to the exact patch hash above.

## Exact correspondence

Patch12-21 supplies only literal byte tokens in the two constexpr arrays.
Independent inert extraction/string comparison, without source evaluation or
cryptographic operations, counted exactly32 bytes in each and found:

| Test array | Retained named vector | Full matching32-byte hex |
| --- | --- | --- |
| `expected_ascending` | `ascending` | `977ea1dce4266794464932f34640331ee4d58bfbd1257467be3d7cc378714caf` |
| `expected_changed_vault` | `vault_first_byte_zero` | `b83f58ef0f9297d7ff8a3487e6f2d6572aedb78eca9f2bb9a386462d3224dc20` |

The retained data is B/`reviews/team20/root/C20-KDF-ORACLE-STDOUT.json`, SHA-256
`adb3f5f07e418f7e717d1fcb030e6cf1ff10fdeca3c84b4e943f7f9e7df56e9e`.
Its reviewed source/root execution provenance is preserved in this directory's
`KDF-ORACLE-SOURCE-REVIEW.md` (`58e8b21470906a6cf6c5232b30a81c95656a79407ef51220d94f087b3f83666a`)
and `KDF-ORACLE-ACTUAL-REVIEW.md` (`fa401c192a4a301a3b44a17107da25f2e2dccda3f098a7ce1b0c3a47f8772e83`).

Patch9 statically requires PRF/salt/hash sizes32, addressing the external SDK
constant qualification. Its32-iteration inputs are exactly PRF47..66, salt33..52,
vault01..20; the alternate vault changes only byte0 to00. Each respective derive
call must return true and its complete std::array output must equal the correctly
named literal array. No native-derived expected value, partial/nonzero-only
comparison, oracle-file loading or encrypt/decrypt roundtrip replaces the golden.
The provenance path is a comment, not a runtime input. This is source assessment,
not proof that an SDK compiler accepted the static assertions.

Full helper cleanup semantics, unchanged dispatch, two-file scope, CMake and
proposed after-image validation remain desktop_other_review's complementary
review. Canonical integration and any fresh admitted Windows execution remain
root-only. Zero additional application cases/closures; all prior native refusal,
consumed failure/HOLD and STOP/NO-RETRY/CLOSED boundaries remain unchanged.

Only bounded inert reads, hashes, JSON/text/literal comparisons and this own report
were performed. No calculator or helper execution/import, KDF recomputation, AST
parse, build/test, SDK/runtime/held-R probe, Git, network, cleanup, central-ledger
edit, new worker or stop obligation.
