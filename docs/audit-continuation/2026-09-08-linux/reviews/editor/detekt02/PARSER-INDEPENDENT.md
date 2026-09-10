# Detekt02 isolated parser — independent source review and five controls

Reviewer `/root/editor`, 2026-09-10; parser author `/root/build_config`.
**SOURCE-ACCEPTED for the exact isolated function only; controls UNEXECUTED.**
This is not full-helper execution, collection/cleanup admission, a Detekt pass,
an application/hardware test, or closure credit. Root alone owns any fresh,
separately admitted execution and its coordination/resource/cleanup record.

## Exact binding

- New holder `scripts/audit/linux_detekt_02.py` at this review:
  SHA256 `2cc2bb7ab582bcf75893ddd6c52343646b4cfcdc077939d027e0f3c56309a1b6`,
  53,977 bytes /937 LF. Its collector is separately challenged below; do not
  import or execute this holder as a module to obtain the parser.
- **Only lines124–163 inclusive**, including final LF:
  SHA256 `cb30e52bf1c4c1c622a22794158cd6631cbe8542631d46aa748d4f8501294e90`,
  1,853 bytes /40 LF. Exact start is `def parse_task_headers(lines, allowed):`
  followed by LF. End immediately before the two separator LF preceding
  `def proc_read(path, cap):`. Independently extracted and hash-checked as data.
- Function `parse_task_headers(lines, allowed)` depends only on stdlib
  `re.fullmatch` and ordinary builtins. It references no helper classes, global
  path constants, I/O, imports, process operations, command runner or cleanup.
  A holder amendment is not accepted by association: root must check the exact
  extraction hash again and separately bind the final whole-helper source.
- Reviewer control data `PARSER-CONTROLS.json`:
  SHA256 `341a26c350d77604a957f433e72fb116fd2532d254dbabf02f5c8c762a1607f2`,
  17,311 bytes /569 LF. It contains the full23 allowed identities, five literal
  line lists and independently derived literal expected return objects. It is
  JSON data, not executable test/helper code. No expected result was obtained
  by running or translating the implementation.

## Independent semantic challenge

1. The fixed caller set has22 generic analyzer identities plus the Ruby coverage
   guard. Unknown grammar-valid tasks cannot become recognized events. Malformed
   `> Task ` headers are errors rather than silently removed evidence. Every such
   header still contributes to the bounded header count.
2. Each task begins UNSTARTED. A first bare or recognized suffix is accepted;
   the only legal repeat is **one bare followed by one recognized suffix**.
   Bare/bare, suffix/bare, suffix/suffix and any third recognized header produce
   an illegal-transition error. A recognized FAILED always overwrites that
   task's outcome with FAILED, even if its transition is illegal. Once FAILED
   has been recorded, later bare/terminal headers cannot launder it.
3. The function has no exit-status argument and no PASS output. BARE is merely
   normalized log evidence. First UP-TO-DATE/FROM-CACHE/SKIPPED may normalize,
   but the collector's separate exact expected-outcome rule rejects those as
   completed fresh analysis. Parser errors likewise prevent mapping. After the
   header bound, the parser records an explicit overflow error, so ignored
   later headers cannot produce a successful full mapping.
4. With the fixed23 identities, outcomes<=23, recognized events<=46 and errors
   <=64. Extra headers yield an explicit bounded error; no unbounded event
   accumulation. The collector independently limits the retained input log to
   4MiB. The function's iterable input is not a general external-input API.
5. Root `:detekt` still depends on all21 subproject analyzers. Source proof is
   `build.gradle.kts` SHA256
   `6c35802a0f779baa1b4d5a65396a07824653761882ddfbfaa718a46c59fa5ee0`,
   24,486 bytes, especially lines370–393. `--continue` does not promise to run
   a dependent root after a prerequisite fails. P3 deliberately leaves that
   root UNSTARTED. All analyzer tasks also retain their coverage dependency;
   a failed Ruby guard may block the whole analysis. No new graph, second
   invocation, rule/source exclusion, Test task or retry is proposed here.

## Five controls — prospective, not claimed results

| ID | Discriminator | Independently expected outcome |
| --- | --- | --- |
| P1 | Retained01 three-header projection: coverage bare, Android bare, Android FAILED. | count3 /unique2 /no errors; 1BARE, 1FAILED, 21UNSTARTED. Both Android events retained. |
| P2 | All23 identities, core/feature NO-SOURCE, other21 bare. | count23 /unique23 /no errors; 21BARE, 2NO-SOURCE. Normalization only, not PASS. |
| P3 | Coverage and all21 subprojects; Android bare then FAILED; root omitted. | count23 /unique22 /no errors; 19BARE, 2NO-SOURCE, Android FAILED, root UNSTARTED. |
| P4 | Android bare, FAILED, then bare. | count3 /unique1; Android stays FAILED and exact illegal-transition error names line3. |
| P5 | Unknown `:app-android:test`, then `:app-android:detekt UNKNOWN`. | count2 /unique0 /no events /23UNSTARTED; unknown-task error line1, malformed-header error line2. |

P1 is independently bound to retained data only:
`docs/audit-continuation/2026-09-08-linux/runs/linux-detekt01/logs/detekt.log`,
SHA256 `13c30804b78f425f8ec21b204ed117ba3143990f0b99a232b073bde1342e4218`,
71,728 bytes /287 LF. Actual headers are exactly lines71,74,87; the control's
indexes1,2,3 are relative to its projection. Neither failed01 nor any archived
helper was replayed. The other four controls are synthetic header data. Five
parser-only cases must remain distinct from23 task identities, findings,
coverage, application cases, mocks and hardware evidence.

## Separate whole-collector objection — preserved, not hidden

At initial holder `2cc2bb7a…`, `preserve_reports` catches all
`FileNotFoundError` from `Files.read` as ordinary absence. `Files.read` reads
an observed leaf then stats its pathname again. Leaf disappearance at that
final stat raises the same exception; the collector can discard already-read
bytes, add only an absence, and leave `reports_preserved=True`. Surviving
parents and source-after's generated-output exclusions do not guarantee a
later guard will catch this leaf case. The author accepted this source
counterexample and is preparing one conservative correction. This is an
administrative collector objection, not evidence of an observed runtime race,
a production finding/family, or license for a mocked/race experiment.

This narrow parser acceptance **does not accept the initial whole collector**.
Full-helper review and corrected exact pins remain separate. Consumed01,
STOP/NO-RETRY/CLOSED/HOLD restrictions, G7/G8, PVU-007/PVU-011/PVA-029 and PVD
boundaries are unchanged. No source/build/test/helper import, syntax/AST or
runtime probe was performed in this review. Only bounded no-follow data reads,
text/hash inspection and two small permanent reviewer files were used; no
owned workers, caches or generated build outputs were created.
