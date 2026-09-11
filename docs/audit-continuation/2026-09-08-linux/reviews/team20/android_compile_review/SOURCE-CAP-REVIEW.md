# AndroidCompile02 — applied-base and separate cap-delta review

Reviewer `/root/android_compile_review`; author `/root/android_compile_author`.
2026-09-11. B = `docs/audit-continuation/2026-09-08-linux`.

**ACCEPT_CAP_SOURCE_PROPOSAL_ONLY_ALL_BINDINGS_UNBOUND.** The exact installed
base images match the previously accepted proposals. The separate cap patch is
reviewed but not applied by this reviewer; root alone controls its integration.
No exact-instance approval or execution admission is issued.

## Integration and immutable proposal identities

After root's explicit three-new-path release, I read the three installed source
files and compared their complete bytes with the original new-file patch bodies.
All match exactly; those original patch bytes also retain the accepted hashes:

| Original patch in author's directory | SHA256 |
| --- | --- |
| `ANDROID-COMPILE02-INIT.patch.txt` | `bf68a4646a253e28b53717242a35f37b406fd4909936d828a231d3dbdf140758` |
| `ANDROID-COMPILE02-INNER.patch.txt` | `ddc62e40fc8aec8fab7e9d6c844f78e77e606509314717e1ba83ff53e80516f4` |
| `ANDROID-COMPILE02-OUTER.patch.txt` | `fa109886f52a4856123c77f35ffbc543bb55873bdda6fc8b1d7d9c3a16a8befc` |

`SOURCE-INTEGRATION.json` SHA256
`a2fba289c8ece07b59f961c02e0dfdb16b80ccc75b7a77bc0209448bf3edaea3`
agrees with these observed installed identities. The original source review
`SOURCE-PROPOSAL-REVIEW.md` remains unchanged and retains its qualifications.

| Source | Installed base SHA256 | Proposed cap-after SHA256 |
| --- | --- | --- |
| `scripts/audit/android_compile_02.init.gradle` | `fccb4952a892ca5d0a223a6484d63497000b76e4a08eae8dc260d8534729bcd9` | unchanged |
| `scripts/audit/linux_android_compile_02.py` | `a10b713e5b7a760475d7a87ed11ee7d9cc9bf4e106f29604c01369b238cfcaf6` | `7391da07d72ff55ce9cbaa26ba31ca8e435f83a2b3063f962719b8d40519c93b` |
| `B/reviews/android-compile02-outer/LAUNCH.py` | `c43deecd539de80c530b391257bac5caea9de394c333aa75a1236df3fa88a962` | `5f00a8109adad00c90546473e8632e25c3233da9369f7b017cd172571fe6a6f3` |

Cap patch: `B/reviews/team20/android_compile_author/ANDROID-COMPILE02-SOURCE-CAPS.patch.txt`,
3849 bytes, SHA256 `378726d37efc095a421dd9402176725e5787e39e072d485426f3f12b62071e5b`.
Rationale `SOURCE-CAP-DELTA.md` SHA256
`47d590765991e20c24ab44977b21de72d8ba709192bfc0b7fcbb331fbb884025`.

I reconstructed the cap-after text in memory, checking all six hunk positions,
context and counts against the installed files. A separate exact replacement
comparison yields the same whole images: four functional replacements and two
ceiling-comment changes, nothing else. Inner becomes63320B/1038LF; outer becomes
63574B/1059LF; init remains12826B/195LF. No helper was imported or executed.

## Need, arithmetic and complete coupling

Root supplied approximately3344 ordinary non-Git W payload files, not a reviewed
final manifest. I did not repeat a filesystem census. That planning count exceeds
the old3196 bound; dropping audit reports to fit it would not be an acceptable fix.

Every source OID is validated as40 lowercase ASCII hexadecimal characters, then
receives exactly one LF. Distinct paths with repeated OIDs are not deduplicated.
Consequently the fixed transport requirement is41 bytes per exact source member:

| Count | Canonical OID bytes | Assessment |
| ---: | ---: | --- |
| 3196 | 131036 | Fits original128KiB=131072 by36 bytes. |
| 3344 | 137104 | Would exceed both original member and byte ceilings. |
| 3500 | 143500 | Fits proposed144KiB=147456 by3956 bytes. |
| 3501 | 143541 | Fits the byte limit alone, but both member guards reject it. |

The exact changes account for every relevant hard count/byte ceiling:

- Inner948 and outer811: both `type(MEMBERS) is int` admission predicates become
  `1 <= MEMBERS <= 3500`. Their binding checks still precede intake/allocation.
  Comments at inner46/outer54 change, but `MEMBERS` itself stays `None`.
- Outer579: canonical OID generation retains `len(data) == MEMBERS * 41` and
  raises only its byte ceiling128KiB to144KiB, before creating the input file.
- Outer593: original OID-file recapture also uses144KiB. Updating just the writer
  would leave a stale readback refusal; this coupled site is included.
- Outer `source_inventory` still requires exactly MEMBERS unique safe paths and
  valid OIDs/modes. Allocation/intent and inner intake retain exact member/source
  agreement and uniqueness. Nothing filters, truncates or omits report members.
- `check_oid_input` derives its entire comparison from the original exact
  `data`; it checks bytes, size, hash, descriptor/name identity before and after
  child use. No other hard128KiB cap or alternate stdin exists in that routine.
- Materialization still consumes the exact expected sequence on both passes,
  checking framing/OID/order/hash/trailing bytes. Outer and inner full-source
  checks remain tied to exact MEMBERS; stop's wrapper-only subset stays3.
- Init has no source-manifest or OID-transport count domain. Its26-task envelope,
  four required source paths, traversal and ten receipt roles must not change.

These are source/data/arithmetic conclusions, not executed boundary tests.
The3500 limit remains a ceiling, not evidence of3500 files or a bound source count.

## Independent limits and failure compatibility

Only two OID-specific128KiB occurrences change. SDK metadata/alias bounds and
parent mountinfo128KiB checks are unchanged. So are64KiB read chunks,128MiB source
blob stream,32MiB per-file/default manifest reads,4MiB index,64KiB config/exclude,
intent/result/evidence limits,4GiB runtime threshold,200000 runtime inventory,
250000 cleanup inventory, source/materialization/compile/stop deadlines and
resource floors. The exact replacement comparison excludes unrelated enlargement.

More permitted members do not prove the eventual manifest, index, blob bytes,
path fanout or running resources fit those independent limits. Root must bind
and review the actual final complete source/index/images. Any remaining excess
must stay a refusal, not an automatic expansion, report exclusion or retry.
Original cancellation/retention/stop/settlement/cleanup paths are unchanged;
this small source-transport limit change creates no new cleanup authority.

## Exact reviewer role and retained fences

I explicitly accept the existing source role constraint
`approval['reviewer'] == '/root/android_compile_review'` at outer858 and its
matching own-directory approval reference. This is the counterpart root assigned,
not `/root/verification`; the cap patch changes neither. The role string is not
authentication. Root must obtain this reviewer's genuine later exact-instance
decision with the complete request/source bindings; no author-generated approval,
old approval or this source report substitutes for it. `INSTANCE-ACCEPT.json` is
not created by this review.

All commit/tree/count/source/read-root/offline/device/lock and instance bindings
remain UNBOUND. The exact Jar leaf/type/direct-edge scope and compiler evidence
requirements remain accepted only with their earlier qualifications. Consumed01,
GUI03/C17 controls/evidence, T and held runtime obligations stay separate and
untouched. Root's three-new-file integration release is not a general freeze lift.

Reviewer activity: bounded W source/patch/report reads, in-memory text/hash/data
comparisons and this own-directory report only. No build/test, Git/CI, helper
import/execution/AST, runtime/process/SDK probe, cleanup or central-ledger edit.
No outstanding tool session, background job or new stop duty. No new cases,
findings, closures or denominator changes. All STOP/NO-RETRY/CLOSED, native-agent
refusal, PVD/protected-ref/signing/Store/publication and occupied1017001 fences
remain. This report does not request another review loop or execution.
