# AndroidCompile02 — separate source-member/OID cap proposal

Author `/root/android_compile_author`, 2026-09-11. **CAP PATCH UNAPPLIED; ALL BINDINGS UNBOUND.**
B = `docs/audit-continuation/2026-09-08-linux`. Root released only the three fresh02
source destinations for integration. Their exact independently accepted base images
were installed from absent paths; see `SOURCE-INTEGRATION.json`, SHA256
`a2fba289c8ece07b59f961c02e0dfdb16b80ccc75b7a77bc0209448bf3edaea3`.
Original new-file patches, consumed01 and the accepted baseline rationale are unchanged.

Root reports approximately3344 ordinary W payload files for the required C18 source
and evidence. That is a supplied planning fact, not this author's fresh census or a
final manifest. The existing3196-member/128KiB OID limits cannot carry that selection.
Root requested a bounded3500-member/144KiB successor, not exclusions or a larger
task graph. Source-cap integration still awaits independent review/root direction.

## Exact minimal patch and images

`ANDROID-COMPILE02-SOURCE-CAPS.patch.txt`:3849 bytes, SHA256
`378726d37efc095a421dd9402176725e5787e39e072d485426f3f12b62071e5b`.
It changes four functional literals and two explanatory comments in the new inner
and outer only. No init edit is needed. `MEMBERS` remains `None`, not3500.

| Image | Accepted/integrated base SHA256 | Proposed cap-after SHA256 |
|---|---|---|
| Init | `fccb4952a892ca5d0a223a6484d63497000b76e4a08eae8dc260d8534729bcd9` | unchanged |
| Inner | `a10b713e5b7a760475d7a87ed11ee7d9cc9bf4e106f29604c01369b238cfcaf6` | `7391da07d72ff55ce9cbaa26ba31ca8e435f83a2b3063f962719b8d40519c93b` |
| Outer | `c43deecd539de80c530b391257bac5caea9de394c333aa75a1236df3fa88a962` | `5f00a8109adad00c90546473e8632e25c3233da9369f7b017cd172571fe6a6f3` |

Init remains12826B/195LF. Inner63316B becomes63320B, still1038LF. Outer63570B becomes
63574B, still1059LF. These are source-text identities, not populated FROZEN bindings.
W-relative destinations are `scripts/audit/android_compile_02.init.gradle`,
`scripts/audit/linux_android_compile_02.py` and `B/reviews/android-compile02-outer/LAUNCH.py`.

## Coupling trace across all three controls

Line numbers are unchanged by this patch; they refer to the integrated fresh02 base.

| Location | Proposed treatment and reason |
|---|---|
| Inner46; outer54 | Update only ceiling comment; retain `MEMBERS = None`. |
| Inner948; outer811 | Both pre-intake `1 <= MEMBERS <= 3196` guards become3500. No one-sided acceptance. |
| Outer579, `oid_requests` | Byte ceiling128KiB becomes144KiB; retain exact `len(data) == MEMBERS * 41`. |
| Outer593, same function | Original written OID-file recapture also becomes144KiB; avoids a stale post-write refusal. |
| Outer559–575, `source_inventory` | Unchanged exact manifest-list count equals MEMBERS, safe unique paths,40-hex OIDs and fixed modes. Repeated OIDs at distinct paths remain repeated requests, not deduplicated/excluded source members. |
| Outer597–610, `check_oid_input` | No hard128KiB literal: it derives exact byte comparison/size from the original `data`, with descriptor/name/hash/pin checks before and after child use. No change needed. |
| Outer612–687, `materialize` | Iterates the exact expected sequence on both passes, validates OID/order/framing/count/hash/trailing bytes; separate source-stream and per-file limits remain. |
| Outer689–718, `source_check` | Processes every expected member and returns its actual count plus raw identity; no separate3196 literal. Per-file32MiB and180s bounds remain. |
| Outer891/907/919–962 | Allocation/intent carries the exact MEMBERS; original OID descriptor and both pre/post checks remain. No alternate stdin or summary-only source transport. |
| Inner349–378, `intake` | Exact outer/source member equality and uniqueness already use MEMBERS; SOURCE bytes remain capped32MiB. No stale count literal here. |
| Inner424–446, `source_check` | Full checks still count exact MEMBERS; the original stop-only wrapper subset remains exactly3. Existing180s scan bound remains. |
| Init entire195LF | No manifest-member/OID transport bound. Its26-task envelope, four fixed required-source paths, input/output traversal and receipt limits are different domains and unchanged. |

Both3196 functional occurrences and both explanatory3196 comments are accounted
for. Only the two **OID-specific**128KiB uses change: SDK metadata/alias reads and
parent mountinfo128KiB checks are deliberately untouched. Generic64KiB read chunks
remain chunks, not a total-input cap. No new parser/helper or task action is added.

## Arithmetic, not execution evidence

One validated40-byte ASCII OID plus LF per source member gives41 bytes/member:

| Members | OID bytes | Consequence |
|---:|---:|---|
| 3196 | 131036 | Original128KiB=131072 leaves36 bytes. |
| 3197 | 131077 | Exceeds original byte cap as well as original count ceiling. |
| 3344 | 137104 | Root's approximate selection would not fit the original bounds. |
| 3500 | 143500 | Fits proposed144KiB=147456 with3956 bytes spare. |
| 3501 | 143541 | Byte cap alone would fit, but both exact member guards still reject. |

Root must bind the actual reviewed final C18 count, manifest, source/index and
control images later.3500 is not a declaration that3500 files exist, nor evidence
that the final payload fits every other independent bound. Unexpected excess
still refuses; no report exclusion, source truncation or automatic cap expansion.

## Independent limits and literal reviewer identity

Retain source/blob stream128MiB;32MiB per-source-file/default manifest reads;4MiB
index cap;64KiB config/exclude cap; existing manifest/intent/result/evidence caps;
4GiB runtime logical threshold/200000 inventory entries;250000 cleanup inventory;
all source/materialization/compile/stop/outer time bounds and resource floors.
Neither member count nor OID arithmetic proves future blob bytes, index bytes,
source-path fanout, resource headroom or elapsed time. No need to widen these
limits has been demonstrated, so none is changed.

The already accepted outer858 literal requires exactly
`approval['reviewer'] == '/root/android_compile_review'`, matching root's assigned
independent counterpart and distinct from author/executor. Its approval reference
remains `B/reviews/team20/android_compile_review/INSTANCE-ACCEPT.json`. Request/run/
purpose/format/hash/source-image equality guards are unchanged. Review is requested
explicitly for that role literal as well as this cap delta; no approval packet is
created and the role string is not authentication or an actual acceptance. Root's
external genuine-review requirement and later exact-instance admission still apply.

All source/read-root/offline/instance pins remain unbound. Android task scope,
exact androidJar type/leaf/direct edges, ten receipt roles, source/class checks,
SDK operation, cancellation/stop/settlement/retention and cleanup semantics remain
unchanged. No APK/device/native/signing/Store scope or runtime/closure credit.
GUI03/C17 controls/evidence, T and held runtimes remain untouched; all STOP/NO-RETRY/
CLOSED/old HOLD restrictions, native-agent refusal and occupied1017001 persist.

Only released source integration plus bounded source-text/hash/diff/arithmetic and
these new proposal records were performed. No helper import/execution/AST/syntax
check, build/test, Git/T, process/SDK/runtime probe or central-ledger edit occurred.
