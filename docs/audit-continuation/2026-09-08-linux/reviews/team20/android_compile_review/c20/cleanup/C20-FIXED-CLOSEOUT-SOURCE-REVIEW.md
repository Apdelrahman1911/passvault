# C20 fixed closeouts: independent source-only review

Reviewer: `/root/android_compile_review`. Date: 2026-09-11 UTC.

**Disposition: SOURCE_ONLY_ACCEPTED_FOR_ROOT_BINDING, static04 first.**
Both exact v1 sources below are nonblocking for the narrowly described cooperative
cleanup-only design. Neither UNBOUND request is admitted. This report is **not**
`ACCEPT_EXACT_ONCE`, not a current custody/mount/resource attestation, and not
permission to execute, retry, chain cleanups, build, test, or stop anything.
No runnable ACCEPT was created by this review.

## Exact packet

Here `B = docs/audit-continuation/2026-09-08-linux` under
`/root/projects/PassVault/passvault-linux`, and
`A = B/reviews/team20/android_compile_author/c20/cleanup`.

| File under A | Bytes | SHA256 |
| --- | ---: | --- |
| DETEKT04-CLOSEOUT.py.txt | 43446 | e9e43ca9c363a771e0427e6809c73da715e3739c9bbc0673e050e65dc2ffcbb2 |
| FOCUSED01-CLOSEOUT.py.txt | 44218 | 3632e97bafcf6d42a78667deb176c8eaf2dfdd871f6af2da74ea7d2bcdda50fa |
| DETEKT04-REQUEST.UNBOUND.json | 1500 | 23a02f40941855adf033657b65733de2a01ce9ed28b02de928d1f32805118f02 |
| FOCUSED01-REQUEST.UNBOUND.json | 1512 | 1d94d460c7df9621b7796471319aa124a5efd0521be0dcaa8dedbc86477e5d05 |
| PLAN.md | 11839 | 74232c80856bb2000f2891e05433c2d350784e1344fd5eca54f241c52a52e9b4 |
| SOURCE-RECEIPT.json | 10581 | 46634158cc67e3aad96982a155856b0ec385b202ca658a06f13c7193111baf1b |
| DETEKT04-CLOSEOUT.patch | 32923 | 988d580daeaa929014c6c13c6062d14371c681e1a098a71a75ae0f5d32ec3712 |
| FOCUSED01-CLOSEOUT.patch | 34846 | 64240a8cdbfc1f53c2fae50b4bf360ca50a61636fe0d8b86959e54746464addb |

Static has 747 LF; focused has 749 LF. Prospective fixed promoted locations are
`B/reviews/detekt04-closeout01/CLOSEOUT.py` and
`B/reviews/focused-regression01-closeout01/CLOSEOUT.py`; root alone may promote.
The patches are explanatory, not authorization to edit/replay old Detekt02.

## Independent work and retained-data agreement

Completed the full static source read, complete focused-vs-static text delta,
both request templates, complete PLAN/receipt, and retained Detekt02 source.
Independently rehashed all 38 exact artifact/input paths in the pinned author
receipt, including every named phase/log, original launch/actual-review inputs,
index-copy record and the saved focused XML. Leaf no-follow bounded reads and
stable before/after file metadata were used in the post-release hash pass.
No held R was opened or inspected. No candidate/old-helper import, evaluation,
AST/syntax check, build, test, stop, Git, SDK, network or proc operation occurred.

Both 13-hunk patches reconstruct their candidate exactly from the 35097-byte,
617-LF Detekt02 baseline (SHA256
`32a759cee52311edac2e08da4a26867ce7f85ced099d06c1ae6c883baa4d916c`)
and reconstruct the baseline in reverse, entirely in memory. Independently
confirmed the 18 named unchanged function bodies and complete finalization tail;
this is text equivalence, not execution testing.

C18 `B/reviews/checkpoint18/source-prepare01/SOURCE.json` is 1879327 bytes,
SHA256 `a34aee1c7ea19fb693adf67437ec67e8b9e0ee34232936cb76127764c7b242d3`.
Derived 3432 unique leaves (3358 mode100644, 74 mode100755), 113336811 raw bytes,
931 proper source ancestor directories / 932 including checkout; largest leaf
11439084 bytes. The 735191-byte compact sorted-row encoding including LF hashes to
`df744d8d7a3836853500d96b465af56206122736b2b1300d7c4059380f557878`.
Ordered OIDs are 140712 bytes, SHA256
`ddee058c4a4cf9a8ab1eb63a323ab0b10dab19a4c4a43214f95e17bc2ff5fb64`.
Commit `6489252e88ad553a867d67578eff45a402e62a48`, tree
`57d338a931ab0fb4e072aabcbbfd27bead8ef08a`. Both EOL qualifications remain;
observed T checkout pins are not runtime leaf pins. Fresh cleanup comparison is
to this raw C18 representation, not current W/C19/T.

For both cohorts, independently verified allocation equals outer allocation;
original directory membership/pin derivations, source buffer structure, and all
phase records equal their inner copies with only record_image removed. Each
phase/command log was also freshly matched to its complete original eight-field
saved-file pin and SHA256 in permanent E. This evidence-file check does not
assert the current state of any runtime source, buffer or index in R.

| Original cohort fact | static04 | focused01 |
| --- | --- | --- |
| R inode / E inode / checkout inode | 307671 / 307670 / 307674 | 323407 / 323406 / 323410 |
| Original R dirs / total intent dirs | 14 / 17 | 32 / 35 |
| Derived raw dirs / required dirs | 945 / 945 | 963 / 966 |
| Required original files | 3435 | 3435 |
| Base runtime hashed bytes | 227546164 | 226995791 |
| Maximum with two cache-original reads | 235934772 | 235384399 |
| Original build/stop command exits | detekt143 / detekt-stop0 | focused143 / focused-stop0 |
| Outer child exits; external exit | [0,70]; 70 | [0,1]; 70 |
| Historical churn; first unowned parent-namespace positive | 1; javac | 6; java |

Static's four metadata commands exited0. All original phases are settled;
all-required-stops and namespace-empty-before-exit are true. These are original
cohort facts, not a new stop or universal writer-exclusion proof. Both retain
source_before=true and source_after=false, cleanup_safe=false,
independent_semantic_acceptance=false. No fresh cleanup source check repairs the
original failure. Positive historical actors remain unwaived and unattributed.

Static index-copy JSON equals intent.index_copy equals outer.index_copy. Its
original sealed local git-index is 553274 bytes, inode13787839, mode33024, SHA256
`372f4404428ad77b58ca0bd5580318dcbe46b7ecba8de6231114c07d8eb360ef`.
The original git-metadata directory inode307686 must be empty during closeout;
no T/config-alias traversal is authorized. Focused has no index or metadata alias.
Each has a pinned 113515367-byte source.blobs image, SHA256
`11b749e0038eeb0acf68c9fbcc5b33889d3c7aba82823aaecda48d39c62a8c42`.

Static retained zero captured reports, 19 exact report absences (18 analyzer,
one Problems), and all seven static tasks UNSTARTED. Focused retained **one**
2901-byte XML with 12 case observations, not 12 XML files. Its exact saved bytes
and eight-field saved pin match the original capture, SHA256
`846b2c4a69e4259b0498c56a85fdd59391c3c98d0e62746d55c817956bd46163`;
R-relative path is
`checkout/core/database/build/test-results/desktopTest/TEST-com.passvault.core.database.repository.BiometricUnlockFreshnessIntegrationTest.xml`,
saved under E at
`xml/focused-TEST-com.passvault.core.database.repository.BiometricUnlockFreshnessIntegrationTest.xml`.
Mapping remains false: no complete20, Desktop success, static/test/native upgrade.

## Source safety judgment and required qualifications

The design retains fixed R/E/source/request/approval paths and original lock,
original STOP0/settlement/failure bindings, source/index rechecks, exact allocated
subroot pins, current caller-namespace mount guards, bounded complete inventory,
and bottom-up descriptor-relative pin/membership-checked deletion. It adds no
argument-selected target, subprocess, extra stop, process census/other-process
signals, namespace entry, mount action, or generic cleanup manager. The exact
original lock tuple is
`(24,14189001,0,33152,1,0,1788910891124735946,1788910891124735946)`;
no replacement/creation is permitted. Original parent pins remain dev23:
R-parent ino580 mode0755; E-parent ino54473 mode0700; lock-parent ino51356 mode0700.

The permanent exclusive E/closeout01 is consumed before any R access. Before
unlink, the source requires complete no-follow inventory, exact raw sources and
transport/index/report files, durable inventory readback, original evidence
rechecks, metadata/membership rechecks, and a second mount guard. Any mount
at/below R, including a same-device bind, rejects. Mountinfo accepts empty source
fields. Removal rechecks each pin/member, then root emptiness/binding, parent
fsync, absent name, open-root nlink0, and exact file/directory/logical-byte totals.
Final evidence, inventory, saved-cache and mount checks remain. Receipt success
is preterminal: descriptor closure, stdout and actual tool exit still require
independent reconciliation.

**Static E/reports qualification:** its PINS entry is an original-intent binding,
not a fresh executed directory-pin check: static captured no report files, so the
source does not freshly open E/reports. It is outside deletion and preserved.
Do not describe this as a complete current E inventory or as fresh validation of
every PINS entry. Every removable R allocated-directory pin is freshly enforced.
Focused worker allowance is limited to database/credential and their eight exact
allocated private leaves apiece, not a blanket workers-prefix allowance.

The only new report exception is the exact private-cache shape
`gradle-home/caches/9.7.1/groovy-dsl/<32 lowercase hex>/reports/` under the original
pinned gradle-home. At most four roots, sixteen regular files, 1MiB/file, 4MiB
total, eight descendant levels and 512 JSON-encoded relative-path bytes. Every
regular descendant, including empty files, is copied raw to exclusive0600 flat
files under fresh0700 OUT/CACHE-REPORTS. The record/inventory retains original
and saved pins, hashes, exact mapping and empty-directory structure. Fsync and
readback precede deletion; originals and copies are rehashed before unlink and
saved membership/bytes afterward. Hash accounting charges two original reads.
This is opaque conservation, not cache-content provenance, publication authority
or test credit. Outside this exact exception, unknown real reports/test-results,
TEST XML and crash/worker diagnostics still HOLD. Focused binary test-results
are not silently allowed; their existence is unknown without a future admitted
inventory. The old Detekt02 cache-report refusal removed zero files/directories;
its consumed/HOLD state is not reopened by this design.

Bounds remain 180 cooperative seconds, 40000 entries, 16MiB inventory, 2GiB logical
bytes, 256MiB runtime hashes, 64KiB receipt, runtime depth64/path8192 characters.
Launch floors12GiB disk/25% RAM; running floors8GiB/20%. No hard kernel-stall
termination, hostile-root/atomic-unlink guarantee, global-idle/no-escape proof,
secure erasure, physical reclamation or successful future cleanup is claimed.
Uncertainty, limits, unexpected objects/modes/links, cancel/deadline, partial
removal or descriptor-close uncertainty remain HOLD; no automatic retry.

## Root handoff: still separate from execution admission

1. Root may promote the exact static04 source bytes only, then provide a fresh
   exact REQUEST binding its current slot SHA, own pid/mnt namespace values and
   true cooperative known-launcher/no-known-scheduler/write/mount/evidence-freeze
   attestations. Root need not claim universal unknown-writer exclusion.
2. An independent exact bound-request review and five-field approval are still
   required, followed by root's separate one-shot execution admission. The
   current templates deliberately retain false/UNBOUND/null authority fields.
3. Retain the cooperative freeze through complete external reconciliation.
   Whole-R success cannot follow from the preterminal receipt alone. On HOLD or
   terminal ambiguity, do not retry or repurpose the consumed evidence directory.
4. Focused01 needs its own fresh binding/acceptance/admission after static
   reconciliation, or an explicit new root decision on HOLD. No auto-chain.

This review changes no central ledger, G7/G8 or other old-scope HOLD, consumed/
STOP/NO-RETRY/CLOSED/native-refusal state, protected ref, publication authority,
original store/lock, or build1017001 fence. Root's separate currentpoint02 is not
adopted as old-R custody. Writes are restricted to this new reviewer report.
