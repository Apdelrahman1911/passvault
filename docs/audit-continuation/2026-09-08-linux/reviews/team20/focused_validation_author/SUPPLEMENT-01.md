# Static04 supplement01 — genuine reviewer and coupled source-count bound

**Source delta only; initially UNAPPLIED pending independent review.** Root
explicitly released only the three NEW04 control paths. The accepted26-hunk
base is now integrated there: `INTEGRATION-01.json`, SHA256
`59a71c35a49f511f138ae1c97807018dd7be2f8b744560956d8bf624025487e9`.
All three03 predecessors remained byte-identical afterward. The earlier PLAN
and reconstruction remain historical evidence of the prospective base, not a
claim that those NEW04 paths are still absent.

Root then requested exactly these six supplementary literal replacements:

| Base04 site | Before | Proposed after | Purpose |
| --- | --- | --- | --- |
| Inner891 | member ceiling3196 | member ceiling3500 | Admit the complete future source membership, not a truncated manifest. |
| Outer804 | member ceiling3196 | member ceiling3500 | Same pre-intake ceiling in both sides. |
| Outer575 | canonical OID bytes≤128KiB | ≤144KiB | Match the larger finite member domain. |
| Outer589 | original `source.oids` capture128KiB | capture144KiB | Retain/check the same original canonical input. |
| Outer28 | prior verification approval path | `reviews/team20/focused_validation_review/INSTANCE-ACCEPT.json` | Genuine newly designated exact-instance reviewer. |
| Outer843 | `/root/verification` | `/root/focused_validation_review` | Require that actual reviewer identity; no impersonation. |

Patch: `SUPPLEMENT-01.patch.txt`, SHA256
`f4e12727dc9c012b9777885263a686dd8cb2b67f453cbb52a6e68b717e6cf361`.
`SUPPLEMENT-01-MANIFEST.json` binds beforeimages, literal replacements and
proposed final unbound afterimages. Inverse literal reconstruction recovers the
integrated base exactly. Init is unchanged at
`a5feccc963f6b96565143837701a8522e63f8fb84d142cd2de624f354a4d2d3d`.

## Count/input coupling and preserved checks

`source_inventory()` still requires `len(files)==MEMBERS`, unique safe relative
paths, full40-hex raw Git object IDs, expected modes and the original wrapper.
It retains each member in manifest order, including repeated OIDs at distinct
paths. `oid_requests()` emits40 ASCII hex bytes plus LF per member, and still
requires **exactly `MEMBERS*41` bytes**, not merely a loose cap.

- Old3196×41 =131,036B ≤128KiB (131,072B):36B margin.
- Root's approximate required3344×41 =137,104B, exceeding128KiB by6,032B.
  This reported approximate count is motivation, **not a new source binding**.
- New3500×41 =143,500B ≤144KiB (147,456B):3,956B margin.
- The separate3500-member guard remains tighter than144KiB alone. It still
  rejects3501 members; `MEMBERS` remains `None` until root binds the actual
  complete source. No auto-expansion, source omission or guessed count.

The two input-cap sites are coupled: creation's exact-length/cap predicate and
the immediate original `source.oids` capture. `check_oid_input()` has no separate
128KiB ceiling: it rereads the same original descriptor in65,536B chunks, rejects
each chunk beyond `len(data)`, and requires exact final length/hash/pins before
and after the child. Those checks and both call sites are unchanged. Partial
writes, original descriptor ownership, seeks, fsync, framing, original child
settlement, cancellation, timeouts and evidence/cleanup are not altered.

Count consumers remain tied to the bound value, not3196/3500 magic assumptions:
outer source inventory/allocation/intake and materialization iteration; inner
intake source/unique-member counts and full before/after scans; source receipt
membership. The stop-only scan remains exactly three wrapper members. No other
member-ceiling literal exists in these two controls.

## Other limits explicitly NOT enlarged

| Independent bound | Retained behavior |
| --- | --- |
| Mount metadata | Inner `131072`B readers and outer128KiB+sentinel/read check;2048-row bounds unchanged. These are **not OID input caps**. |
| Raw Git blob stream |128MiB live/final output, materialization/recapture bounds unchanged;32MiB per member, exact header/OID/order/framing/EOF/digests unchanged. |
| Ordinary full index |4MiB input/copy/verification bound unchanged. No split/sparse/incomplete/fake index. |
| Source/input data |32MiB source-manifest/tool/general-input read and per-source bounds unchanged; metadata config/excludes65,536B, request1MiB, approval65,536B. |
| Static/report evidence |Same six analyzers plus coverage and exact16 justified task omissions;18 required reports+optional Problems;19 capture slots;4MiB per static report,1MiB Problems, combined32MiB. No source/report exclusion. |
| Total inner evidence |96 files/64MiB; original per-command4MiB retention remains. No resource or result enlargement. |
| Runtime inventory |6GiB logical bytes,30,000 directories/230,000 examined entries/200,000 files; outer250,000-entry cleanup bound unchanged. |
| Time/resources |Outer6000s/work5250s; static3600s/stop600s; existing180s source/transport scans; launch12GiB/25% RAM and running8GiB/20% unchanged. |
| Ownership/process/coordination |Original lock, pidfd/namespace proofs,8192-entry/5s host screen, original settlement, tool quiescence and sole root build/CI owner unchanged. |

The larger member ceiling does **not** establish that the future payload fits
these other unchanged bounds or prove future resource availability. Root's
fresh exact source/index/input facts and admission must establish applicability;
a genuine excess remains refusal, not permission to exclude reports or widen
anything else. No future source, Git store or runtime was probed here.

## Genuine approval, not relabeled source acceptance

The changed path is for a **future exact-instance** approval. It is not the
existing paired `ACCEPTANCE.json` for source review. The approval's exact schema,
format, `ACCEPT_EXACT_NEW_INSTANCE` disposition, purpose/run, request SHA256 and
four source-image hashes remain required and are frozen/rechecked under the
original lock. The designated reviewer must genuinely issue the new packet
after reviewing the actual final source, omissions, tools and instance facts.
No approval or request is created by this supplement.

`COMMIT`, `TREE`, `MEMBERS`, `EXCLUDE_STATE`, both FROZEN maps, outer DEVICE and
EXPECTED_LOCK remain unbound. C17/03 evidence is not reissued under04 identities.
The17 application afterimages reported by root are outside this source-author
edit scope; their future source identity and omitted171-input reuse proof remain
root/independent work. No GUI03 result/HOLD/cleanup change or automatic retry.

Only bounded source-text reads, copied-source readback, exact literal/diff/hash
comparisons and own supplemental reports were performed. No helper import,
execution, compilation, AST, build/test, Git/T/CI, process/SDK/runtime probe.
All STOP/NO-RETRY/CLOSED/HOLD, device/PVD and publication fences persist.
