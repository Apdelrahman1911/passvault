# GUI02 independent actual source-binding review

Reviewer /root/verification; binding author /root; 2026-09-10.
**SUPPORT_EXACT_SOURCE_BINDING_ONLY. NO INSTANCE OR EXECUTION ADMISSION.**
No concrete binding-only blocker found. This is a focused continuation of the
accepted GUI02 body reviews, not a whole-harness re-audit, runtime result, fresh
coordination finding or authority to allocate R/E. All earlier qualifications
and STOP/NO-RETRY/CLOSED/HOLD scopes remain.

W=/root/projects/PassVault/passvault-linux;
B=W/docs/audit-continuation/2026-09-08-linux.

## Frozen tuple

Workload P=0d06721b7f8bdc17513a6ab51ffbc5523e68b230;
tree=dd04904c1deb691bd6aeed756dd6ceab8ea97837; 2177 raw members.
These are the external bound images, NOT the unbound helper bytes inside P.

| Image | SHA256 | Bytes/LF |
| --- | --- | --- |
| W/scripts/audit/linux_desktop_gui_02.py | 4dc98da8134a12da8e6992348d7fd4a30b269e81350866b9b34f0b9f7905e676 | 68060/1154 |
| B/reviews/desktop-gui02-outer/LAUNCH.py | 3f23deba21bfc0a1ed6218ace9809180df5f5e9a8c0f605015fb8cf65e4cff2b | 44874/766 |
| W/scripts/audit/desktop_gui_02.init.gradle | cdc6ffe9c5cef7e4d8cf156fd78fdf653f88f23dd8496f7a7ff616552d372c08 | 8988/163 |
| B/reviews/desktop-gui02/SOURCE.json | 6d593b947d76f0a7b3e7929a7ae54e6968cb0a36b69bf78d771b33226bd396ee | 1007418/22007 |
| B/reviews/desktop-gui02/SOURCE-CAPTURE.json | 5b89c5c90f40b34ff92a633fb2e93f3174a65451aaf8642ce8cdc4c9822fe0a9 | 7331/205 |
| B/reviews/desktop-gui02/BINDING.json | 24a5ec9135aa315aa295d60cb11520a76934aa3d09e80e6c88cf78a12ee00709 | 2368/60 |

Reused and rehashed accepted source-only reviews:
- B/reviews/verification/LINUX-DESKTOP-GUI02-OUTER-SOURCE-DELTA-REVIEW.md,
  10164B, SHA256 1a4784706ffeaafece85b9338d058db23e6bbdf8745a4158d753113205e1cb80.
- B/reviews/editor-independent/GUI02-INIT-INNER-SOURCE-REVIEW-01.json,
  15212B, SHA256 1ce9688d8e2360f25c7da14cc3a6b4b94e3a04c0f47139eece5a17f8b76753f3.
The previously read outer/inner plans remain unchanged (SHA256
f5e3c7503f0d411d721374ae8549808f3f76a5b4d9d787ba5ab329279589d389 and
2d40c1f0e296071fbc12d20d703abfc5b236e16ade73feda8dc2f18babc107de).
Their pending-binding descriptions are historical source-stage qualifications,
not a representation that the current SOURCE is still the pending template.

## Complete binding delta and preservation

Under root's explicit ordinary-file allowance, both P baseline helper files
were read at /root/projects/PassVault/passvault-publication-20260910-01.
No .git access or Git invocation was performed by this reviewer.
Complete inert diffs (inner19 lines, outer39 lines; receipt bcaad7 exit0)
change only inner COMMIT/TREE/MEMBERS/SOURCE hash and outer corresponding
constants, GITDIR, DEVICE, EXPECTED_LOCK and FROZEN inner/source hashes.
No command, fixture, selection, init, supervisor, namespace, cleanup or semantic
acceptance body changed. Accepted body reasoning is reused only for these
unchanged bodies; it does not import old execution authority.

P's unbound inner: 67912B, SHA256
4d7750ad6342ca85c036b81f111022c2fdb8ccaa7b3909500bbb9d668d2790de,
computed raw Git blob OID 0b8a173103539bc17e4434be05e8f905395e5b0a.
P's unbound outer: 44600B, SHA256
0390e12249d584c333656e80e83f8a353b111fb07ba07c7f6acbf4c32e627398,
computed raw Git blob OID 12947f582b459be939d2ded825ae94d489abcf04.
Both match the actual SOURCE inventory exactly. P also retains the1371B
pending SOURCE template (SHA256
380921986806346adf572f67568d2bacf1d29bbc7bcf358d601439322a3988d8).
The external actual manifest and bound helpers are not claimed as their own
members. The unchanged8988B init matches both external binding and P row.

GITDIR now names the publication clone's .git. BINDING and capture agree on
reported directory identity [dev23,ino1080546,uid0,mode16832], separate
directory/file devices23/24, and original-lock report:
dev24,ino14189001,uid0,mode33152,nlink1,bytes0,
mtime_ns=1788910891124735946,ctime_ns=1788910891124735946.
Integer nanoseconds were not passed through JavaScript floating-point values.
These are supplied data pins, NOT reviewer observations of current store,
device, lock, parent or namespace authority. They require genuine fresh
request/intake checks before entry; mere constant filling is insufficient.

## Independent manifest/data reconciliation

Receipts 6de47d and 02450a exited0. Entire SOURCE bytes were read/hash checked
and all2177 rows mechanically reconciled as inert JSON/data:
- Exact eight-field rows,2177 distinct safe relative paths, allowed100644/100755
  modes, lowercase40/64-hex fields and bounded nonnegative integer sizes.
  git_size equals raw_size; no .git member, control/backslash path, empty/dot/
  parent component or file/ancestor collision. gradlew is100755.
- 2039 distinct blob OIDs occur at2177 paths. Repeated OIDs have consistent
  raw size/SHA256; repeated objects were NOT deduplicated from batch input.
- Pure stdlib hashing reconstructs all786 directory trees from modes, names
  and blob OIDs using Git's directory-slash ordering. Root is exactly
  dd04904c1deb691bd6aeed756dd6ceab8ea97837; recursive file order matches SOURCE.
  This authenticates inventory structure relative to the supplied tree,
  not a separately observed ref/commit ancestry or every raw payload.
- Raw sizes sum78144733B; expected per-member Git batch framing sums78258103B.
  Ordered OID-newline input reconstructs89257B, SHA256
  effba7fc8d0817d4f417a67412f5270da24217195c936cb6ad0b4ad3bfc55588.
  Reconstructed ls-tree -r -z -l output is309434B, SHA256
  e1c29d35bc56f046dd60499f6611d26f1175122d7a122dc7a922fd9cdc20d9aa.
  Reconstructed P/tree newline output is82B, SHA256
  ffc63988116799bbe6ce1f627b5e4351822c950c68dd464eef6f33929c129ce0.
  All exactly match capture. These computations did not execute Git.
- Embedded capture equals external capture except its two external-only
  source_sha256/source_bytes fields. Those fields, BINDING's capture/source
  hashes, all four image hashes/sizes, P/tree/member count/representation and
  reported store identity agree. JSON duplicate keys were rejected.
- Capture reports four bounded root-owned read-only Git children, all exit0,
  reaped direct children, empty stderr and60s per-command limits. Its reported
 78258103B batch-stream SHA256 is
  26e19af2ed035f30bc54d69cc4ed3e4c300af9afcf0aea07d1dfac274e3f6c87.
  This reviewer did not reread or regenerate that payload stream. Per-row raw
  hashes outside the separately read helper/init files remain captured source
  evidence, not a second payload extraction or whole-corpus source review.
  Capture's explicit abnormal-descendant-settlement-not-established HOLD/no
  retry qualification is preserved; direct-child reaping is not universal
  descendant settlement.

## Fixture and EOL binding

Both actual fixture rows exactly equal capture's rows and inner SELECTIONS'
frozen SHA256s; raw/git/checkout sizes and hashes coincide:
- DesktopCurtainRenderingTest.kt,22441B, blob
  1bf75ba16573f13226d0364d7e5f1e7ca557693d, SHA256
  6e5f8190e94008f3e3c41b62b3d1d75cdca471057e2de57497829d5c42191c57;
  one declared case.
- CredentialEditorRenderingTest.kt,34363B, blob
  df75a5a37c07e118c4b88fc6807edb4fc569dfce, SHA256
  6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a;
  three declared cases.
Prior fixture/body reviews remain the source reasoning. No new fixture-body
audit, native loading or rendered behavior is claimed by row reconciliation.

Exactly15 raw-versus-calculated-checkout differences match capture: gradlew.bat
and its14 named scripts/*.ps1 entries. Both historical EOL qualifications
reconcile to their script row and archived raw-copy row:
- test-windows-checksum-boundary.ps1: raw-copy/raw Git11703B,
  SHA25617e5d8e8a30ec0ececf31fd20453938a904ab657573b80cccaee72fe35943141;
  calculated checkout11940B,
  SHA2567c9dae59e786612de3852b28ee8bb1ee64ba3b97d31af8d9f3d50f0617c818c7.
- update-desktop-biometric-checksum.ps1: historical raw-copy4213B,
  SHA2563d7221596a01c46b23100f7f05b6c0ebd246af141ce943b3f63f4704be19a396;
  raw Git4132B,
  SHA256aa399f28b78df8a81a46e3be198022e8dce34dfd8bd7b57e91b71a3e5a7ed203;
  calculated checkout4221B,
  SHA256855c8f71a8437d2469917ff75dc6d17b3ac34c041d3cfbc8d8b6e15410541aa1.
This verifies recorded raw/Git/calculated-checkout relationships, not fresh EOL
transformation of every payload or inherited raw-byte runtime coverage.
Future admitted GUI transport is RAW_GIT_BLOBS, not a filtered checkout.

## Remaining admission and result gates

No genuine GUI02 request/instance approval is granted here, and the true
INSTANCE-ACCEPT filename was not written. This review establishes neither
current CI status nor agent quiescence. After serial Windows05 terminal,
root must obtain a genuinely fresh request, source-store/config preservation,
20-image/four-original-parent and caller pid/mnt/net provenance, device and
original lock evidence, plus coordination/cleanup admission. Strict
agents_quiescent:true and no_ci:true apply; Linux03's parallel-source allowance
does not transfer. No old pins, R/E allocation, replay or automatic retry.

The four source images for eventual approval are final bound outer, inner,
unchanged init and actual SOURCE from the frozen tuple above, not P's unbound
helper hashes. All20 outer images/14 inner images need the original GUI02
pin schema. Only the exact /usr/bin/python3 -> python3.12 link exception is
allowed with both target pin and original link_pin/link_target; Detekt's
direct-alias scheme is not imported. Launch must use /usr/bin/python3.12
-I -B -S and absolute SELF with the exact scrubbed outer ENV:
PATH=/usr/bin:/bin, LANG=LC_ALL=C.UTF-8, TZ=UTC. No ambient resolution authority
or unreviewed target/tool execution follows from this report.

Original one-slot, JDK17/wrapper, strict verification, resource/time/cleanup
limits and limitations remain. Detekt and iOS were excluded from root's C11
selection and are not admitted or verified by GUI02. This binding review is
not a revalidation of the entire100-file C11 publication selection.
Any later execution needs original runtime/stop/settlement/cleanup evidence
and independent actual XML/classpath/native-input/pixel semantic review.

**Four prospective cases/two selected Test tasks; zero actual cases, new
findings, fixes or qualified closures from this review. Denominators unchanged.**
PVU-005/PVA-007/PVA-031 rendering questions remain unexecuted here. Editor
production-form/new-VM coverage uses a deep-copy fake repository, not Room/
disk/full NavHost or real competing editor/backend acknowledgment. Mobile/IME/
RTL, physical iPhone/Hello, full-app auth and universal zero-exposure claims
remain excluded. All eight PVD limitations/owner decisions stay separate.
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED,
all consumed/HOLD scopes, GUI01 restrictions, protected-branch/publication
fences and occupied mobile1017001 are unchanged.

## Reviewer execution/resource record

Bounded inline python3 -I -B -S stdlib readers performed stable component-wise
no-follow ordinary-file reads, inert textual diffs and JSON/hash calculations.
Receipts bcaad7,6de47d,02450a,69a31b,3504aa exited0. The69a31b repeated review
display truncated a middle span;3504aa displayed that span fully. An initial
JavaScript orchestration-template SyntaxError occurred before any shell/tool
command; it produced no file and was not target syntax checking or a retry of
restricted execution. Earlier completed preparation receipts remain preserved.

Immediately before sealing, the tuple and reused review/plan hashes were
rechecked. Only this compact permanent report was created exclusive0600,
fsynced with its parent and exactly read back. No project/helper import,
syntax/AST check, build/test/Git/network, target launch, .git/old-store/held-root/
SDK/cache/lock access, process/namespace/mount probe, signal or deletion.
No temporary/cache/runtime output, daemon, server or persistent worker was
created; bounded in-memory data were released at foreground completion. All
reviewer file/directory descriptors were closed. Resource samples in capture
are root's observations, not fresh reviewer measurements.
