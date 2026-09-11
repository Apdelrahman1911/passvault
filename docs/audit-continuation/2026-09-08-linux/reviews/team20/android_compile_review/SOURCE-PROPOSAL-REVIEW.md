# AndroidCompile02 — independent unapplied source review

Reviewer `/root/android_compile_review`; author `/root/android_compile_author`.
2026-09-11. B = `docs/audit-continuation/2026-09-08-linux`.

**ACCEPT_UNAPPLIED_SOURCE_DELTA_ONLY_ALL_BINDINGS_UNBOUND.** No material blocker
was identified within the reviewed delta. This is not permission to apply,
an exact-instance approval, execution admission, compiler success, whole-runner
certification or a claim that no bugs are possible. C17 application/tests/helpers
and T remain frozen until root separately reconciles the freeze.

## Exact reviewed proposals

Files below are in `B/reviews/team20/android_compile_author/`. Each patch is a
single new-file addition from `/dev/null`; no consumed01 destination is modified.

| Patch | Bytes | SHA256 |
| --- | ---: | --- |
| `ANDROID-COMPILE02-INIT.patch.txt` | 13104 | `bf68a4646a253e28b53717242a35f37b406fd4909936d828a231d3dbdf140758` |
| `ANDROID-COMPILE02-INNER.patch.txt` | 64435 | `ddc62e40fc8aec8fab7e9d6c844f78e77e606509314717e1ba83ff53e80516f4` |
| `ANDROID-COMPILE02-OUTER.patch.txt` | 64751 | `fa109886f52a4856123c77f35ffbc543bb55873bdda6fc8b1d7d9c3a16a8befc` |

After-images were reconstructed **only as in-memory text**, not installed:

| Proposed destination | Bytes / LF | SHA256 |
| --- | ---: | --- |
| `scripts/audit/android_compile_02.init.gradle` | 12826 / 195 | `fccb4952a892ca5d0a223a6484d63497000b76e4a08eae8dc260d8534729bcd9` |
| `scripts/audit/linux_android_compile_02.py` | 63316 / 1038 | `a10b713e5b7a760475d7a87ed11ee7d9cc9bf4e106f29604c01369b238cfcaf6` |
| `B/reviews/android-compile02-outer/LAUNCH.py` | 63570 / 1059 | `c43deecd539de80c530b391257bac5caea9de394c333aa75a1236df3fa88a962` |

Author rationale `SOURCE-DELTA.md` SHA256:
`67d1bd50a3bb15a8f49b40a75fc4a607492608552a6176584c8aafe4d479b4ec`.
Its scope/identity/cleanup qualifications agree with the reviewed text. All four
proposal/rationale hashes were reconfirmed after root's source-only resume.

## Independent method and grounding

Read the complete init and every reconstructed inner/outer diff against the
current bound01 sources, plus relevant original graph, collector, stop,
finalization, admission and cleanup surroundings. New-file hunk counts match.
The author's final two line wraps were reversed as text: each reconstructed
earlier complete image reproduces the previously reviewed SHA256 exactly.
No hidden source change was inferred from a summary or a truncated diff.

`BASELINE-CHALLENGE.md` records the separate retained-evidence/source investigation:
all ten actual-review binding hashes/sizes match; graph/finished bodies equal
original log markers3508/3510; all14 nodes are reachable from DEVICE; only
androidJar lies outside the original25 names. All34 retained C17 non-doc Gradle
source tuples and four required crypto/harness tuples matched. That investigation
preceded the proposals rather than merely endorsing the author's explanation.

The current bound01 init/inner/outer hashes still matched the baseline while the
proposal comparisons were made. The three proposed W destinations were absent.
No live T/R/SDK identity, checkout, process or cleanup state was inspected.

## Scope and counterexample review

1. Both text-extracted domains are equal26-name sets: exactly `androidJar` added
   to the original25, nothing removed. The init graph ceiling tightens64 to26;
   the inner header domain25 becomes26 and graph2..25 becomes3..26. This fixes
   both otherwise-stale inner ceilings without enlarging the source-member cap.
2. The new pre-action check requires exact path `:core:crypto:androidJar`, exact
   `org.gradle.jvm.tasks.Jar_Decorated`, enabled, and four empty outgoing edge
   lists. Its sole incoming relationship must be DEVICE.dependencies. DEVICE
   must also directly depend on MAIN; both real compilers remain required and
   enabled. Missing/wrong/disabled Jar, another Jar name/type, extra outgoing or
   incoming reference, and loss of the direct MAIN edge fail these predicates.
   The collector checks the same structure after preserving original bytes.
3. No invented Jar-to-MAIN ordering or dependency is added. The retained Jar is
   a leaf preceding MAIN; its type/edges do not prove inputs, outputs, archive
   contents or arbitrary-action safety. Frozen source and strictly verified
   plugins remain the explicit ordinary trust boundary. No wildcard Jar,
   task-skip workaround, dependency rewrite, extra selector or APK/AAR/test/
   install/sign/publish scope is introduced.
4. Exactly10 successful receipt roles remain. Only MAIN and DEVICE require fresh
   BARE/non-skipped/non-cache/non-NO-SOURCE actions and class readbacks. Jar must
   be enabled and represented in the graph/header mapping, but has no separate
   fresh-action/archive oracle; its noncompiler header suffix is not newly
   prohibited. Do not report a verified Jar archive or a third compiler success.
5. All four required-source pins are null/None, not inherited C17 hashes.
   ReadRoots/offline, COMMIT/TREE/MEMBERS/FROZEN/exclude/device/lock bindings are
   likewise cleared. Outer binding checks precede request intake/allocation;
   inner hash-shape checks precede intake; init null/hash checks precede task
   actions. Init configuration/resolution may still precede its graph rejection;
   a source-only proposal is not authority to invoke it as an UNBOUND probe.

## Error, cancellation and cleanup compatibility

Exact text comparison found the init input/output/readback tail, inner Files/
namespace routines, class-readback/mapping tail, and all outer functions before
main unchanged. The inner compile/finally block is identical after the run-label
rename. The complete diffs introduce no other resource/mount/stop/cleanup change.

Raw receipt bytes are still retained before semantic interpretation. The new
Jar checks live in the existing semantic-mapping block: refusal can coexist with
preserved evidence and safely cleaned failed execution. Retention/readback,
ownership, cancellation, original stop or namespace uncertainty still prevents
cleanup acceptance. The original safe-failure/outer return path, descriptor-bound
allowlist cleanup and actual-terminal qualification remain; no retry is added.

Carry the prior `HELPER-SOURCE-INDEPENDENT.md` and consumed01 actual-review
qualifications rather than calling this a new full runner audit. Keep the one
DEVICE invocation, Gradle9.7.1/JDK17/one worker/non-daemon/no-CoD/strict verification,
no SDK/JDK acquisition, original stop,3196-member ceiling,900s/600s compile/stop,
6000s/5250s outer/work bounds,12GiB/25% and8GiB/20% floors,4GiB logical inventory,
4MiB command-log and32MiB evidence/readback bounds. These remain cooperative
userspace/point-observation limits, not hostile-root or hard-resource guarantees.

## Remaining root gates and result limits

The new SOURCE path is an unbound reference, not a demand to duplicate a valid
capture. Root must reconcile the still-current freeze and final independently
reviewed crypto fixture, choose/bind the exact source and control images, and
obtain separate fresh instance/coordination/execution admission. Path/pin changes
need exact review. The proposed `INSTANCE-ACCEPT.json` reference in this reviewer
directory is **not created or granted by this report**. Old approval cannot satisfy
the new run/purpose/formats and exact reviewer/request/source checks.

Compile01 remains consumed failure before target compilers with its original
accepted cleanup. Root's separately reported GUI03 terminal70/zero cases does
not release this freeze or settle its cleanup HOLD; this lane makes no GUI03
adjudication. All held runtimes and STOP/NO-RETRY/CLOSED fences remain unchanged.

No source syntax check, helper import/execution, build, test, Git/CI, SDK/process
probe or deletion occurred. Writes are only this lane's reports. The initial
overbroad instruction-filename locator disclosure remains in the baseline report;
no T/held file contents were opened or later inspected. Root's GUI03 tool fence
was observed, with no outstanding sessions or background jobs, until explicit
source-only resume. No Gradle-stop obligation arose for this review.

Zero application/native/KDF/XML cases, findings, closures or denominator changes.
Actual Android32, JNA/ABI/DEX/minification, business flows, API24 consent/usable
target and hardware gates remain separate and unverified. Eight PVD limitations,
native-agent refusal, protected refs/signing/Store/publication and occupied1017001
boundaries remain. No further review loop or execution is requested here.
