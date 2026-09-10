# Detekt01 collector failure — bounded successor proposal

Author: `/root/build_config`, 2026-09-10. **SOURCE/DATA ONLY; NOT ADMISSION.**
No helper was imported/executed, no project command/test was run, no runtime
was inspected, and consumed01/outer/init/T/GUI03 were not changed. This is an
administrative collector correction proposal, not a new product finding or
closure. Root approval and another agent's challenge precede implementation.

## Exact evidence and cause

- Frozen `scripts/audit/linux_detekt_01.py`:
  `fceab30448da506edd3010d432bc489e238defcaa6f01f455ba205b095d2e2f7`.
- Retained `runs/linux-detekt01/logs/detekt.log`, relative to this continuation:
  `13c30804b78f425f8ec21b204ed117ba3143990f0b99a232b073bde1342e4218`.
- `scripts/audit/detekt_01.init.gradle`:
  `5beb8165ae491258c0fae5a5a4f119cbe778157d6a577aacff79fe93fc33ae97`.
- `build.gradle.kts`:
  `6c35802a0f779baa1b4d5a65396a07824653761882ddfbfaa718a46c59fa5ee0`;
  its hash and the init hash match C15's raw and checkout manifest tuples.
- C15: commit `8f42274b04e206ff7254ca33d686a9666fce6723`,
  tree `3a8f53dddd54f5c42c34f772975f02619be118d5`; manifest SHA256
  `1a501d764306c1729cef49a411207ca840740d5fe6e284555767182478b55b23`.

Log line74 is `> Task :app-android:detekt`; line87 is
`> Task :app-android:detekt FAILED`. These are two headers for one task, not two
task executions. Lines75–77 announce HTML/SARIF/Checkstyle generation; lines78–84
give seven diagnostics, and lines94–95 retain the analyzer failure. Report-file
bytes/existence were **not** checked by this agent; generation messages are not
a substitute for the actual reports. Root reports actual outer70 / Gradle1.

The helper's lines653–656 demand unique task IDs across all headers and throw
before the report-copy loop at lines662–699. Consequently this legitimate
bare-to-FAILED transition prevents preservation of available static reports.
The later `dict(events)` is unreachable, not an adequate independent safeguard.
Failure/cleanup reconciliation for consumed01 belongs to root; this proposal
does not repair or replay that outcome.

## Minimal successor scope

Keep the existing fixed helper/outer architecture, task/source allowlists,
strict dependency verification, serial execution, stop/settlement obligations,
descriptor-bound file checks and resource/deadline limits. No generic runner,
new dependency, rule suppression, source exclusion, task exclusion, ignoreFailures,
application change, or extra validation invocation. Only a newly named/frozen
successor may receive the following collector delta and optional static-only
command/init delta; do not edit consumed01.

### 1. Retain raw reports before parsing/rejecting task mapping

Within `preserve_reports`, perform two bounded passes rather than rejecting
headers/inventory before copying:

1. Enumerate the existing 22 `TASKS` entries, not paths supplied by log text.
   For each module, the exact three paths are
   `<module>/build/reports/detekt/detekt.{xml,html,sarif}` (root omits the module
   prefix). These are the exact paths declared in all 22 retained inventory rows.
   A successor must require its declared report map to equal these fixed paths;
   changed paths are mapping failure, never permission to traverse a new path.
2. Use the existing stable, owned, single-link, no-follow `Files.read` and
   evidence writer. Keep the 4MiB/file, 32MiB aggregate and 66-report ceilings;
   no directory scan or additional artifacts. Save available raw bytes and
   their exact original/saved descriptors before XML/JSON/schema validation.
   Account for absent files explicitly. An empty/malformed but safely captured
   file is retained and subsequently invalid, not discarded as inconvenient.
3. A missing report is evidence of absence, not an I/O success or test pass.
   Unsafe/path-drift/read/write/budget failures must be recorded as collection
   uncertainty; they cannot set `reports_preserved=True` or allow cleanup.
   Continue only independently safe allowlisted captures, and stop capture at
   the existing byte bound. Do not relax ownership or resource checks.
4. Then parse the retained log/inventory, normalize headers, validate each
   retained report and derive mapping. A malformed inventory/header/report must
   not prevent retaining other available allowed reports. Catch bounded
   semantic errors separately and write `STATIC-REPORTS.json` with captures,
   absences, collection errors, validation/mapping errors and task accounting.
   Preserve raw files even when semantic validation fails. DTD/entity declarations
   remain rejected before XML parsing; preserve the existing format checks.

`reports_preserved` means all allowed present reports were safely retained and
absences accounted for; it does not mean reports are valid, all tasks ran, or
Detekt passed. Missing/malformed semantic evidence keeps `mapping_ok=False`.
Malformed checkstyle files contribute no invented finding count; any subtotal
must be labeled as findings in successfully parsed retained reports only.
The existing source/stop/settlement/Git/evidence gates remain mandatory.

### 2. Exact header normalization, never last-write-wins

Let `A = TASKS.keys() + {':verifyStaticAnalysisCoverage'}`: 23 task identities,
zero application cases. Consider every line beginning `> Task `; full-match
the existing path/suffix grammar. A malformed/unknown header or task is a
mapping error rather than silently ignored text. Keep line numbers and original
header records; at most 46 headers, at most 23 distinct allowed task IDs.

Per task, accept only these sequences:

| Raw sequence | Normalized evidence status |
| --- | --- |
| no header | `UNSTARTED` |
| one bare header | `BARE` (not a standalone success claim) |
| one recognized suffixed header | that suffix |
| one bare header followed by one recognized suffixed header | that terminal suffix |

Recognized suffixes remain `NO-SOURCE`, `UP-TO-DATE`, `FROM-CACHE`, `SKIPPED`,
`FAILED`. Any other repetition/order, including FAILED then bare, repeated
FAILED, two bare headers, or two terminal suffixes, is invalid. A recorded
FAILED can never be overwritten or converted into success. Accepting the
bare-to-FAILED transition as valid logging does **not** accept the analyzer.

Keep planned inventory separate: exactly 22 unique known task rows, exact
snapshot-derived source sets under the existing exclusions, exact report maps.
Do not count inventory rows as executed tasks or source-engine visitation.

`mapping_ok` requires all 23 task identities observed, no header/inventory/report
validation errors, coverage `BARE`, nonempty analyzers `BARE` with all three
nonempty valid reports, and empty-source analyzers `NO-SOURCE`. Missing, FAILED,
SKIPPED, UP-TO-DATE or FROM-CACHE evidence cannot certify a nonempty analyzer.
Even valid `BARE` mapping cannot establish PASS without a complete Gradle exit0
and **all** existing safety/final-result gates. Keep unsuccessful analysis and
collector validity as separate facts; no task-count/compilation success shortcut.

### 3. Optional `--continue`, static-only

Recommend adding `--continue` to the successor's fixed **COMMAND only**, e.g.
`[..., ':detekt', '--continue'] + FLAGS`; do not put it in shared FLAGS or STOP.
Its successor init should require `start.continueOnFailure` along with the
unchanged exact 23-task graph/no-Test/one-worker/serial/strict checks.

The pinned build lines371 and378 make every analyzer depend on the Ruby coverage
guard; line393 makes root `:detekt` depend on all 21 subproject analyzers. Thus:

- A failed subproject analyzer need not starve independent subproject analyzers
  under `--continue`, assuming coverage succeeded and no other prerequisite or
  resource/deadline failure blocks them. There is no promised saving or outcome.
- Root's own Gradle-Kotlin analysis remains `UNSTARTED` if any subproject fails;
  **do not** claim full 22-analyzer coverage or rewrite/remove that dependency.
- A failed coverage guard can still block every analyzer. `--continue` neither
  overrides failed dependencies nor changes Gradle's nonzero result.
- Keep the existing deadline; deadline/resource/cancellation failures stop work
  as before. No automatic retries, second root-only invocation, or relaxed PASS.
- This is not proposed for GUI03, application tests, native work or consumed01.

## Five prospective in-memory controls — NOT executed tests

Use small synthetic logs/report byte maps and predeclared expected results.
Do not import/execute the old helper to run them. A future independently admitted
check may exercise only the new extracted pure collector logic; no Gradle/task
execution, disk/runtime probe or helper main is implied here.

| Control | Synthetic input and discriminating expectation |
| --- | --- |
| 1. Observed failure transition | Coverage bare; Android bare then FAILED; 22 valid planned rows; Android's three valid reports with seven synthetic XML errors; Gradle1. Three headers normalize to two task identities, Android FAILED, 21 other analyzers UNSTARTED. All three available reports retained; parser valid; mapping/result false. |
| 2. Complete success candidate | All 23 identities once; the two empty modules NO-SOURCE, other analyzers and coverage bare; 22 exact plans; 60 valid reports for 20 nonempty analyzers; Gradle0. Mapping true, but final PASS still requires the independent existing runtime/source/stop/Git/evidence gates. |
| 3. Continue with blocked root | Coverage plus all 21 subprojects observed; Android bare then FAILED, other subprojects have expected statuses; no root header; 57 valid reports for the 19 nonempty subprojects; Gradle1. Preserve partial reports; root UNSTARTED, never presumed success; mapping/result false. |
| 4. Failure laundering | Otherwise success-shaped input, but Android headers are bare, FAILED, bare and exit0 is claimed. Reports still retained; illegal transition cannot overwrite FAILED; mapping/result false despite the claimed exit. |
| 5. Untrusted inventory mapping | Otherwise success-shaped input with all 60 expected safe reports, but one planned report map points outside its module (e.g. `../outside.xml`). Preserve all fixed-path reports first; never read the injected path; reject mapping/result. |

Additional source-review challenges: unknown/suffixed headers must not disappear
from parsing; duplicate/malformed plan rows, a missing required format, malformed
XML/SARIF, unsafe file metadata and byte-bound exhaustion must all remain
non-PASS. Runtime admission, fresh source binding, outer/schema/accounting
alignment and independent review remain root-owned prerequisites.
