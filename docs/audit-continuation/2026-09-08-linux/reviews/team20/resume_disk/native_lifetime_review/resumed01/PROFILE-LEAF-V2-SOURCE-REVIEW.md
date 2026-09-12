# Future exact profile-entry cleanup V2 — independent source/API review

2026-09-12. Reviewer `/root/native_review_c20`; author `/root/native_author`.
**ACCEPT_FUTURE_PROFILE_LEAF_V2_SOURCE_ONLY.** No integration, actual-source,
instance, execution, cleanup-success or recovery admission is issued here.

## Exact accepted delta

Author leaf under B`/reviews/team20/resume_disk/`:
`native_lifetime_author/resumed01/profile_leaf_source01/v2/`.

| File | Bytes / LF | SHA256 |
| --- | --- | --- |
| `PROFILE-LEAF-SOURCE-PACKET.json` | 8078 /170 | `ae35c8016669c545eb07fe5cfe1568a836dcf5baa0eb780f4e341bf192230bc9` |
| `windows_jna_graph_01.py.txt` | 89490 /1470 | `ac79e75fade8b0b8e4cf6d6df22865973d7565757a12c192191e1fab18908c10` |
| `SOURCE-SCOPE.txt` | 8778 /127 | `28d243e5a7a6fdf875445b8cdf0cf8c42d834b8987925594dc4e1bd2b476c0a5` |
| `REQUEST.template.json` | 1831 /35 | `12c782b407874688fa9a6d06fe84d23cc0292512c68be0f098bd2a0ca51cf442` |
| `PROFILE-LEAF-DELTA.diff.txt` | 14111 /221 | `930e1e533dc648790126cc1d6cfe7d7ca915364e02c11b88f77f9f1e3e61a219` |
| `V1-TO-V2.diff.txt` | 3825 /51 | `33c2053a3e91a1d9610e9982016eb97e0ada838a9add2e792eb99bea01f27f8e` |

The21 declared baseline/review/source/preserved-V1/control/API-document tuples
matched. Both full helper diffs were independently reconstructed exactly. The
request differs only in helper/scope hashes; init, workflow, selected commands,
resource/deadline/capacity limits and all actual-binding/false-attestation gates
remain unchanged. Parent graph source review `06e33f5d...9832bde50` and future
feasibility review `e159e01d...fa4709af` are reused for their unchanged scopes.
Complete new methods, changed caller/finalization paths, scope/request and API
paragraphs were read; the unchanged80KB framework was not needlessly re-audited.

## API proof, including the mount-point counterexample

Root's exact Microsoft text receipt is
`root/NATIVE-WINDOWS-API-DOCS01/RECEIPT.json`, SHA256
`46e19e3f24d559973c5fdb76a37eff693346484e39255690b0c01b07e3631d5d`.
Those pages are reference data, not execution instructions.

- **CreateFileW general flag contract** (text258–266): normal reparse processing
  does not occur with OPEN_REPARSE_POINT; the open attempts the reparse point.
  With original plain ancestry, this binds the entry itself, irrespective of
  target payload. This proof is not limited to its later symbolic-link remarks.
- **SetFileInformationByHandle** changes information for the supplied hFile;
  class4 is FILE_DISPOSITION_INFO and deletion requires DELETE access. That
  structure contains one BOOLEAN. The helper retains the same original entry
  handle through metadata, class4 disposition and one close; it never substitutes
  a target pathname or mount-management operation.
- **GetFileInformationByHandleEx** class9 is FILE_ATTRIBUTE_TAG_INFO: two DWORDs.
  The declaration/signature and native DWORD4/structure8 guard match the supplied
  API contract. Query results must reconcile with before/after metadata from
  that same handle. No reparse payload is requested.
- The official tags reference names MOUNT_POINT without a numeric value. The
  source therefore uses admitted Windows Python's named
  `stat.IO_REPARSE_TAG_MOUNT_POINT`, rejects missing/noninteger/out-of-range
  values, and has no numeric fallback. The old run's actual tag remains UNKNOWN.
- MOUNT_POINT can mean a volume mount point, not a benign profile junction.
  Safety follows from **entry-bound handle disposition**, not target locality,
  ownership, emptiness or subtype. RemoveDirectory junction prose and
  DeleteVolumeMountPoint are neither substituted as proof nor invoked. Driver/
  filesystem refusal remains HOLD, not a promise of successful removal.

## Lifecycle and counterexample review

1. **Only one fresh entry.** The literal whole G-relative name is selected before
   generic refusal. G plus six ancestors must be original retained plain,
   single-linked, same-volume directories. Generic reparse refusal remains for
   every other path; no old Windows4/05 root, path override, adoption or retry
   channel exists. Original stop/Job/evidence/close-error gates still precede
   the cleanup call.
2. **No second chance to an uncertain handle.** The special open uses DELETE|
   READ_ATTRIBUTES, read sharing only, OPEN_EXISTING and both directory/no-follow
   flags. Before return, query/validation/journal failure closes that acquired
   handle once; the main inventory never also owns it. Returned handles enter
   the existing close-attempt set. Disposition failure gets one final close;
   close failure is never retried. Original failure details remain in the record
   and close failures remain globally fatal to operational success.
3. **Refusal remains fail-closed.** Stable observed snapshot/tag is recorded
   before later type/link/volume/attribute/tag refusal. Wrong tags, missing APIs,
   unexpected attributes or ancestry refuse during all-before-any inventory,
   before any cleanup deletion. Later drift may follow earlier allowed removals
   and leaves partial-cleanup HOLD; this is not transactional all-or-nothing
   cleanup. No failure is converted to success because a later pathname is absent.
4. **No descent through the admitted reparse.** Only a valid directory
   MOUNT_POINT returns a nonrecursive profile record. The directory stack now
   explicitly excludes reparse attributes. Depth-first removal ordering keeps
   all seven ancestor handles alive for leaf checks/disposition/absence.
5. **Ordinary exact-name compatibility is retained.** V2 returns the same plain
   handle with profile=None to the normal walker. Ordinary children can be
   removed without comparing an obsolete directory timestamp or granting alias
   no-descent/absence credit. The author's preserved V1 lacked that distinction;
   it is not accepted by this review. Ancestor custody compares identity/type/
   links, not size/write timestamps changed by legitimate child deletion.
6. **Removal evidence is not just API success.** After same-snapshot/tag and
   ancestor rechecks, class4 plus one successful close precede bounded name-only
   enumeration of the original checked plain parent. The leaf is not reopened,
   resolved, queried as a directory or scanned. A remaining casefolded name,
   deadline/API/close/ancestry failure prevents ENTRY_REMOVED. Whole-root success
   still needs original root close/absence and the existing terminal predicate.
7. **No stronger race or target claim is inferred.** Microsoft explicitly exempts
   attributes/extended attributes from sharing restrictions. Read sharing blocks
   incompatible data-write/delete opens, not all metadata mutations. Parent
   enumeration is path-based between original-handle ancestry checks under the
   inherited cooperative, non-hostile namespace assumption; it is not atomic
   handle-relative enumeration or proof against adversarial parent mutation.
   Target flags describe operations through this entry, not knowledge of an
   unknown target or a guarantee it was never accessed through another separately
   owned path. `target_unchanged_verified=false` remains essential. There is no
   target-content observation or forensic unchanged guarantee here.

## Remaining actual gates

Root may integrate the accepted source delta only after binding actual Sfix and
its shared SOURCE, current controls/reviewer nonce and independent instance,
platform/resources/global quiet slot, triggers and original cleanup admission.
The next already-needed authorized Windows job can supply actual API/tag/entry
removal evidence if the branch naturally occurs. If absent, record the exception
unexercised. Do not add cleanup-only CI or retry old Windows4/05. Ordinary-DLL/
real-JNA composition still requires its own bounded integration/admission; this
zero-case graph helper does not admit those workloads. A future graph pass also
needs actual graph/source/log/stop/Job/whole-cleanup independent reconciliation.

Zero application runs/cases, product fixes/findings, observed cleanup successes
or family closures are added. PVU007 STOP, PVU011 NO RETRY/no procedure inquiry,
PVA02949/44PASS/5FAIL/no automatic retry, G7/G8 CLOSED, original native refusal,
all prior HOLDs, PVD decisions, protected refs/tags/versions/dependencies/
identities/signing/Store/build1017001 remain unchanged.

Activity: named inert source/public-reference reads, exact data-only tuple/diff
comparison and this exclusive review write. One guessed scope filename was
absent; the packet's exact SOURCE-SCOPE.txt was then read. No helper import/AST/
compile/execute, Windows/native call, network/Git/build/test, old-root/target/
SDK/runtime/cache/process probe, deletion, daemon or stop obligation occurred.
