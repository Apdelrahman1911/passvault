# Detekt02 successor — independent corrected-source acceptance

Reviewer `/root/editor`, 2026-09-10; author `/root/build_config`.
**ACCEPT exact corrected source, UNBOUND / NOT EXECUTION-ADMITTED.**
The initial collector objection is resolved by one finite source correction;
its evidence is retained, not silently replaced. This review executes zero
helpers, controls, builds, tests, runtime/SDK/process probes or Git commands.
No application finding/family, Detekt pass, readiness or closure is credited.

## Exact accepted source and independent inverse

| Data object | SHA256 | Bytes /LF |
| --- | --- | --- |
| `scripts/audit/linux_detekt_02.py` corrected | `e5b2352080d4eafc2d22c3a50a7d3f4b31f4b324672cab1e59bdef8b91a2d294` | 55,245 /955 |
| `scripts/audit/detekt_02.init.gradle` | `2ff75e0d26f4bd2d7bca7a0c62d929fed9174115ad20b665e5b10caabe51b9e6` | 2,838 /55 |
| Corrected `preserve_reports`, lines688–840 | `b200d1da3da63898503ea7bc97e3df259eebe609e94946cc7e80d741241c6b41` | 10,134 /153 |
| Unchanged pure parser, lines124–163 | `cb30e52bf1c4c1c622a22794158cd6631cbe8542631d46aa748d4f8501294e90` | 1,853 /40 |

`SOURCE-INVERSE.json` SHA256
`80ecd9c0a1de3626ed23015e06a70b97e459e8e8fb3dda330917c75c70247da4`
(3,819 bytes /90 LF) contains the complete input pins and independent data-only
proof. No patch was applied to source:

1. Invert the single34-LF correction diff: recover exact initial02
   `2cc2bb7ab582bcf75893ddd6c52343646b4cfcdc077939d027e0f3c56309a1b6`.
2. Remove precisely the40-LF parser and two separator LF; restore the
   namespace-normalized predecessor collector; invert the five fixed literals
   and four namespace labels. This recovers byte-for-byte frozen01 inner
   `fceab30448da506edd3010d432bc489e238defcaa6f01f455ba205b095d2e2f7`.
3. Invert the two init literals and namespace labels: recover exact01 init
   `5beb8165ae491258c0fae5a5a4f119cbe778157d6a577aacff79fe93fc33ae97`.
4. Independently regenerate both unified text diffs; each equals the retained
   author diff byte-for-byte. Neither predecessor nor successor was imported,
   parsed as executable code or executed.

Author artifacts remain under `reviews/build-config/detekt02/`:
`DELTA.json` `389486f3…`, `SUCCESSOR-DELTA.patch` `05658c15…`,
`CONTROL-SPEC.md` `95a1e5ff…` are the preserved initial generation.
`REVIEW-CORRECTION.patch` `93ae4c2b…` and `REVIEW-CORRECTION.json`
`42f4d348…` bind the corrected inner; initial DELTA's whole-inner after-image
must not be mistaken for the current source.

## Challenge resolved: initial absence is not read/drift uncertainty

The rejected initial collector caught every `FileNotFoundError` from
`Files.read` as absence. An observed report leaf can disappear after its bytes
are read but before the final pathname stat. Parents can remain stable; the
failed read leaves no retained leaf descriptor and generated reports are not
source-after members. Thus surrounding checks do not guarantee detection.
The author independently accepted that concrete source counterexample.

Corrected lines706–731 instead:

- validate the parent through existing `Files.directory` **outside** the
  absence catch, then perform an explicit no-follow entry stat;
- classify only that initial stat's ENOENT as absence, recording the actual
  missing relative component; even that classification rejects a previously
  pinned missing directory;
- pin each observed ancestor at descriptor admission and compare its original
  identity, preserving the established directory identity rules;
- if the leaf was observed, call the unchanged stable bounded `Files.read` and
  compare its returned full file pin to the initial observed pin;
- route any later ENOENT, unsafe type/link/ownership, identity replacement,
  read/write error or other uncertainty into `collection_errors`, never normal
  absence. Such uncertainty keeps `reports_preserved=False` and cleanup HOLD.

Counterexamples checked by reasoning, not filesystem mocks/races: initially
missing unpinned ancestor or leaf; pinned parent disappearing; observed parent
replacement; leaf disappearing during read; leaf replacement between initial
lookup and stable read; symlink/non-directory ancestor; unsafe leaf; evidence
write failure. Normal initial absence is still compatible with a failed task
that never emitted a report. No unsafe bytes are fabricated as preserved.

## Retention precedes semantics; failed analysis cannot become PASS

The original settled-phase prerequisite is unchanged. When collection is
allowed, all66 fixed analyzer report paths plus the one optional diagnostic
are considered **before** report/log/header/inventory validation. Neither a
malformed plan, unknown task nor duplicate header chooses paths or suppresses
safe raw capture. Empty or malformed safe files are copied before rejection.
Aggregate over-budget capture explicitly records uninspected remainder and
blocks safe cleanup; other collection errors likewise cannot become absence.

Retained01 log SHA256
`13c30804b78f425f8ec21b204ed117ba3143990f0b99a232b073bde1342e4218`
contains22 unique planned analyzer identities. Independently reading those
JSON rows as data confirmed **every planned report map matches the fixed02
paths**. This establishes compatibility of naming with actual retained01
metadata, not a new successful analysis or per-file engine-visitation proof.

After capture, Checkstyle must be nonempty XML with the expected root and no
DTD/entity declarations; SARIF must be nonempty bytes decoding to a JSON
object (even an empty object qualifies); HTML is checked for nonempty bytes
only. This is the existing limited format contract, **not
full SARIF/HTML schema validation**. Optional Gradle problems HTML is opaque
diagnostic evidence and grants no analyzer credit. Findings are explicitly a
subtotal of successfully parsed retained Checkstyle reports, not full coverage.

The plan requires exactly22 known unique rows, exact source sets and exact
fixed report maps. Header grammar/identity/transition errors block mapping;
all23 task identities must be observed. Coverage must be BARE, nonempty
analyzers BARE with all three minimally accepted reports, empty analyzers
NO-SOURCE. FAILED, UNSTARTED, cache/skip suffixes, invalid plans or missing
required reports cannot qualify. `STATIC-REPORTS.json` is written before
preservation/mapping flags are committed.

`PARSER-INDEPENDENT.md` (`2aa1a21f…`) documents strict first-header/one
bare-to-terminal normalization and sticky FAILED reasoning.
`PARSER-CONTROLS.json` SHA256
`341a26c350d77604a957f433e72fb116fd2532d254dbabf02f5c8c762a1607f2`
contains the five independent expected-value controls. P1 uses actual01
headers71/74/87; P3 leaves a failed root dependency UNSTARTED despite
`--continue`. These are five prospective parser-only cases, not23 tests or
application/hardware evidence. Any root execution receipt is separate.

## Unchanged safety and finite capacity

The independent inverse proves no undisclosed delta to Files read/write/
verify/close, source/index/Git proofs, resource checks, pidfd settlement,
namespace lifecycle, original stop and final result machinery. Only the
explicit fresh labels/identity placeholders, parser, collector and continue
contract differ. `--continue` is in the one COMMAND, **not FLAGS or STOP**.
The init still admits only the exact generic analyzer-plus-Ruby graph, no Test
or graph expansion, one worker, serial/no-CoD/no-cache/strict verification.
The original3600s analysis and600s stop bounds are not expanded.

Final safety still requires source-before/after, all required original stops,
owned settlement, completed command evidence, stable Git/index bindings and
preserved reports for an attempted analysis. PASS additionally needs a real
successful Gradle phase, mapping and no accumulated errors. Semantic analysis
failure may safely exit1 only if all original safety proofs really hold;
retention/safety uncertainty remains exit70/HOLD. No failed stop, source or
settlement proof is discharged by the new parser or report summary.

- 66 analyzer candidates, <=4MiB each; one explicit problems HTML <=1MiB.
- Combined capture budget stays32MiB; failed/partial writes are charged.
- Completed inner file maximum rises86→87:67 captures +6 logs +6 command
  intents +8 JSON (SOURCE BEFORE/AFTER, INNER/GIT PREFLIGHT, two PHASE records,
  STATIC-REPORTS, INNER-RESULT). Existing inner cap remains96 files /64MiB.
- With20 nonempty analyzers, the nominal count would be80 without /81 with
  the diagnostic. This is arithmetic, not actual files/tasks/cases or a bound
  on outer-owned files; root must account for outer evidence separately.

COMMIT/TREE/MEMBERS/FROZEN[SOURCE]/EXCLUDE_STATE remain None. The SOURCE path
is only a C16 preparation placeholder. This acceptance grants no final source,
outer/instance/tool/store/exclude/coordination/cleanup identity admission.
Root alone owns those fresh gates and execution. Consumed01, GUI03 freeze,
STOP/NO-RETRY/CLOSED/HOLD scopes, PVU-007/PVU-011/PVA-029, G7/G8 and PVD
boundaries remain intact. No automatic retry is authorized. Reviewer work
used bounded no-follow data reads, hashes/text diffs and small permanent
review files only; no workers, temporary caches or generated build outputs.
