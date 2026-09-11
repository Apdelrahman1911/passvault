# Focused regression01 — independent partial C18 application verification

Reviewer `/root/android_fixture_review`, 2026-09-11.

**VERIFIED: exactly the accepted partial source bindings are applied.** This
verifies source/control bytes only, not final instance admission or execution.
DEVICE and EXPECTED_LOCK remain UNBOUND; no genuine instance approval is issued.

Authority/acceptance: sibling `PROPOSAL-REVIEW.md`, SHA256
`92a2a3bd600ed258c4380fb395f081bb761628e83ac7d875fb65591e4bee2f08`.
Exact author patch SHA256:
`0821857df32152dfde1bfd4fcf2626931ddf918b8c69378ed62b2bc4692417c4`.
Author `APPLICATION-RECEIPT.json` was read/hash-checked (4474 bytes), SHA256
`bb1fa293253a417e2f73507fc42c9a78635980285fb81d650ed3f7ff6b51562b`;
independent actual-file comparison, not that receipt alone, supports this result.

## Actual readback

| Applied control | Bytes | SHA256 |
| --- | ---: | --- |
| CLASSES | 1408 | `c973df4ac8a9e0057d01a17f0266b46c4e3c1873fd35cd72185a8cec859bda53` |
| INNER | 40158 | `d84cc3fdfb097fcae00c16c1b18404c7a2cdf638e357d45d6dbc94df15e84715` |
| OUTER, still partial | 51279 | `a855d2cf04727c39115e17b52c73ae7334edbe3d06971f99a4a9aab68b5f2eea` |

All three actual files equal their sealed before-image plus only the independently
reconstructed root-released constants/cells, byte-for-byte. CLASSES uses the
three authoritative checkout_sha256 fields. Hash order is CLASSES -> INNER ->
OUTER, with SOURCE and unchanged INIT/METHODS independently rechecked. No other
source bytes, ledger columns, selectors, unrelated None state, mechanisms or
scope changed.

The one shared C18 SOURCE remains1879327 bytes/hash
`a34aee1c7ea19fb693adf67437ec67e8b9e0ee34232936cb76127764c7b242d3`,
commit `6489252e88ad553a867d67578eff45a402e62a48`, tree
`57d338a931ab0fb4e072aabcbbfd27bead8ef08a`,3432 members.
INIT remains `e945045a8de9db6b8b773b1a49fade926b6dd55494a4f65cc6bc089816c16a79`;
METHODS remains `eed756d95856424716cdce0c7b1dafd3986acd6636b45ea95933ea5e8a1c22b5`.
The six-control identities and raw-published-unbound versus fresh-bound-control
qualification in the accepted proposal/data review remain applicable. The
immutable source capture was not replaced with the newly bound control bytes.

GITDIR is only the explicitly released fixed current-T selector. Device/lock,
actual tool/store/parent/namespace/runtime facts and final request/approval remain
root-owned future work. Further device/lock binding changes OUTER's hash and
requires the final packet to bind that exact new image. This partial hash is not
final launch authority; bound INNER is not standalone execution permission.

The author applied the released delta. This reviewer only read bounded source/
evidence data and wrote lane reports: no helper execution/import/AST, builds/
tests, live probes, Git/T/proc/runtime/SDK/network operations or cleanup.
Exactly20 existing methods/three XML/two Tests plus Desktop compile-only remains
the prospective scope; zero new executed cases or stop obligations. All prior
limitations, GUI03 HOLD and STOP/NO-RETRY/CLOSED fences remain unchanged.
