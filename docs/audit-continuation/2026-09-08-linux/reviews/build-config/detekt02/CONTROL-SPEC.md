# Detekt02 parser/control and retention contract

Author `/root/build_config`, 2026-09-10. **Source-only; no controls executed.**
The exact source/delta/function hashes are in `DELTA.json`; the adjacent patch
is inert review data, not instructions to modify consumed01. Root owns all
execution, final source binding, outer alignment and cleanup admission.

## Extractable pure function

`parse_task_headers(lines, allowed)`, inner lines124–163 inclusive, is exactly
40 LF. Read its exact byte range identified in `DELTA.json` (including its final
LF); it depends only on stdlib `re.fullmatch` and ordinary builtins. No helper
module/class, top-level helper imports, I/O, process, project or runtime are
needed. No permission to execute/import either full helper follows from this.

Input: a finite list of synthetic log lines and the fixed23 allowed identities
(the22 entries in `TASKS`, plus `:verifyStaticAnalysisCoverage`). Return keys:

- `events`: recognized allowed headers, each `{line, task, suffix}`; suffix is
  null for bare. Line indexes are relative to the input projection, not the old
  full log. Unknown/malformed header text remains in the separately retained log.
- `outcomes`: all23 identities, initially UNSTARTED. First bare becomes BARE;
  a single recognized suffix, or bare followed by one recognized suffix, records
  that suffix. FAILED is sticky even when another transition is invalid.
- `errors`: bounded grammar/unknown-task/transition/overflow diagnostics. Any
  error defeats semantic mapping, even if other headers look complete.
- `header_count`: all `> Task ` lines, including malformed/unknown/overflow.
- `unique_tasks`: distinct allowed task identities with recognized headers.

Header processing is bounded to46 headers; outcomes to23 identities and errors
 to64 entries. More input headers produce an explicit bound error, never PASS.
BARE is **not** a standalone success state. The caller additionally requires
exact inventory/source/report mapping and every original final Gradle/safety
conjunction; this function does not accept exits or return a PASS verdict.

## Five meaningful prospective parser-only controls

These replace the broader fifth inventory/path example in the earlier proposal
for the narrow extractable-function check. Retention, unsafe-path handling and
format checks remain separately reviewable source behavior, not fake parser
coverage. The independent reviewer supplies/finalizes exact vectors; root alone
may execute an independently admitted exact-function extraction.

| ID | Input | Required discriminator |
| --- | --- | --- |
| P1 | Exactly `> Task :verifyStaticAnalysisCoverage`, `> Task :app-android:detekt`, `> Task :app-android:detekt FAILED`. These are the retained01 lines71/74/87 as a three-line header projection, not replay. | header_count3, unique_tasks2, errors empty; Android FAILED, coverage BARE, other21 analyzers UNSTARTED; events preserve both Android headers. |
| P2 | All23 identities once; `:core:detekt` and `:feature:detekt` have NO-SOURCE; the rest are bare. | header_count23, unique_tasks23, errors empty; exactly2 NO-SOURCE and21 BARE. This only establishes normalization, not complete analysis or final PASS. |
| P3 | Coverage plus all21 subprojects; Android bare then FAILED; other subprojects have P2 statuses; omit root `:detekt`. | header_count23, unique_tasks22, errors empty; root UNSTARTED, Android FAILED,2 NO-SOURCE,19 BARE. A root dependency failure is not completion even with --continue. |
| P4 | Android bare, then FAILED, then bare. | header_count3, unique_tasks1; Android remains FAILED and illegal-transition error names line3. Last-write-wins must not launder failure. |
| P5 | `> Task :app-android:test` then `> Task :app-android:detekt UNKNOWN`. | header_count2, unique_tasks0, events empty, all23 UNSTARTED; unknown-task error at1 and malformed-header error at2. Neither header may silently disappear into a success-shaped mapping. |

No report-retention exercise, Gradle execution, application case or hardware
behavior is established by these five controls. Their count must stay separate.

## Collector/safety contract and limits

Before parsing any report/log/inventory semantics, capture only66 fixed Detekt
report paths plus optional `build/reports/problems/problems-report.html`. The
latter saves as `reports/gradle-problems.html`, opaque diagnostic evidence only.
All available bytes are copied through existing descriptor-bound ownership,
no-follow and stable-image checks. Missing paths are explicitly recorded.
Unexpected log-supplied paths are never read: declared maps must equal the fixed
maps afterward. Empty/malformed but safely retained files fail required semantic
mapping without suppressing preservation of other available files.

Collection errors (unsafe/read/write/drift/budget uncertainty) keep
`reports_preserved=False` and block cleanup-safe success. Semantic validation
failures, FAILED/UNSTARTED tasks, malformed headers/plans/formats or missing
required reports keep `mapping_ok=False` but do not invent a failed stop/source/
settlement proof if those independent proofs actually succeeded. Summary JSON
must itself be retained before `reports_preserved` is set. The final unchanged
safety conjunction remains authoritative; a normal failed analysis may exit1
with safe cleanup, while any safety uncertainty remains exit70/HOLD.

- Detekt:66 maximum files, each <=4MiB. New diagnostic:1 file, <=1MiB.
- All67 candidates share the **unchanged32MiB** capture budget, including bytes
  charged for failed/partial evidence writes. An over-budget next candidate
  stops capture, records uninspected remaining paths, and blocks safe cleanup.
- Existing whole-inner limit stays96 files/64MiB. Conservative maximum rises
  explicitly from86 to87:67 captures +6 logs +6 command intents +8 other JSON.
  The other JSONs are SOURCE BEFORE/AFTER, INNER/GIT PREFLIGHT, two PHASE records,
  STATIC-REPORTS and INNER-RESULT. Outer-created files are separate root scope.
- With C15's20 nonempty analyzers, nominal complete files would be80 without
  diagnostic /81 with diagnostic. These are capacities, not actual counts or
  a guarantee about the still-unbound C16 source or outputs.
- Static COMMAND alone gets --continue; FLAGS and STOP do not. Init requires
  continueOnFailure and the unchanged fixed static graph. Root's analyzer still
  depends on all21 subprojects; a subproject failure can leave root UNSTARTED.
  Failed coverage can block every analyzer. No dependency rewrite, extra task,
  second invocation, new deadline, retry, rule suppression or GUI03 change.

Source placeholders remain deliberately unbound. New outer/source-instance
bindings and independent source review/admission are required. Consumed01,
PVA-029 failure/no automatic retry, PVU-007 STOP, PVU-011 NO-RETRY, G7/G8 CLOSED,
all held scopes and hardware/publication boundaries remain unchanged.
