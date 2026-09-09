# Linux database closeout author record

Author: `/root/editor`. Root alone executes, commits and pushes. This lane wrote
only the new closeout source and reports. It did not import/compile/execute a
helper, test/build/launch an application, delete outputs, signal processes or
start workers. No cleanup obligation from an author-owned build was created.

## Current V2 source checkpoint — exact source/PLAN accepted, NOT execution

| File | SHA-256 | Bytes | Physical LF |
| --- | --- | ---: | ---: |
| `scripts/audit/linux_database_closeout.py` | `b16774ff6d0c14ece40dbe2c75684a2b07e7c90c9da4373ec0a6e12e87cc7bec` | 59183 | 1094 |
| `reviews/linux-closeout/PLAN.md` | `cffc69357a0c4f0c5e46fd4d282439ece513eb2f166074c05f2ab4e679f8a57d` | 29298 | 474 |

V2 implements only the two independently confirmed V1 infrastructure revisions
below. Journal state advances only after initial/preappend/postappend full pin,
name, expected-length and durability checks. A cheap bootstrap deadline/latch
callback now covers bounded reads/fsyncs/binds/verifications, source/event/parent
loops, setup, under-lock rehash and pre-journal consumption. The later full
original-lock/resource/process/mount guard remains conjunctive; directory opens
and the bounded HOLD journal path stay nonpolling to avoid recursion or losing
failure evidence. The900s main-clock versus interpreter/stdlib loading/outer-bound
qualification is explicit. Targets, protection fence, roles, limits and controls
are unchanged. The event filtering preserves original owned-birth refusal/omission
semantics rather than acquiring PID authority from a bare launch observation.

`/root/baseline_coverage` independently accepted both V2 code corrections after
reviewing the entire successor diff, affected caller/failure context and six
byte-reconciled unchanged regions. The source review is
`../baseline-coverage/LINUX-CLOSEOUT-SOURCE-REVIEW-v2.json`, SHA-256
`572d9ae3eadbad03d5ee2ef8758609411c69ef7874c4e848b0d053384e0183f7`,
with companion Markdown SHA-256
`8f5ce3844c4fff97200282b8ed16e11887ee3d74a44151a32671f6764b2dd8bf`.
This source acceptance supplies no V2 execution/control/cleanup pass or filled
instance acceptance. All seven C01–C07 actual-instance obligations remain
unfilled by this author record.

### LC-C03-DOC-01 — narrow PLAN qualification, unchanged helper

The V2 reviewer found the pre-existing PLAN sentence claiming rejection of all
nonfinite JSON values. The helper author separately challenged and confirmed
the observation: `parse_constant` rejects literal `NaN`/`Infinity`/`-Infinity`,
and duplicate keys are rejected, but default `parse_float` can overflow an
exponent such as `1e400` to infinity in unused/extra metadata or launcher
`limitations`. Critical admission fields have independent exact/bounded guards;
neither author nor reviewer identified an unsafe cleanup path. Actual instance
review still must inspect truthful shapes and values. This is a prose correction,
not a parser/schema/authority/dependency redesign or a new application finding.

The only PLAN diff qualifies that sentence and its surrounding paragraph; it
does not assert universal finite-number/RFC validation. The exact prequalification
V2 PLAN is preserved at `pre-qualification-v2-PLAN.md`, SHA-256
`05ef7315917cf1d26106d1d69be47709738ea2bc5ee7d5805864749cbb77ba61`,
28699 bytes /466 LF. The helper remains byte-exact `b16774...`.
The independent reviewer completed the narrow final pair rebind on 2026-09-09
UTC: helper unchanged, only the agreed literal PLAN prose replacement. Its exact
source-proposal acceptance is
`../baseline-coverage/LINUX-CLOSEOUT-SOURCE-ACCEPTANCE.json`, SHA-256
`c47bb8c9d39d32ab21d9f7d288c2239aafc847fe260e40bbee0e391fbcd6a58e`,
with companion Markdown SHA-256
`9680e82520b0e47e0341ea581ee259cc98a511d841b892ecd031c2e8ecd15286`.
All seven proposal source contracts are accepted with the prior explicit
limitations. LC-C03-DOC-01 is resolved at documentation/source-review level only;
meaningful controls remain unexecuted and all seven actual-instance obligations
remain unfilled. No normative `ACCEPT-*.json` was authored here on behalf of
another role. The source acceptance is not execution permission.

## Preserved V1 source checkpoint — independently REVISE

| File | SHA-256 | Bytes | Physical LF |
| --- | --- | ---: | ---: |
| `reviews/linux-closeout/rejected-v1.py.txt` | `a0001a0e5e9e399f7a9f27f627564cdfc99753b9143a35b9dd6bc0d79c7029c4` | 57457 | 1041 |
| `reviews/linux-closeout/rejected-v1-PLAN.md` | `15b2fb04baeba8e849bf50f8c3ff6cfdb67f8c355b89e6e5706df9e1eda2a696` | 25769 | 423 |

Report-relative paths are under the current Linux continuation unless absolute.
The exact source/PLAN were sent to `/root/baseline_coverage` for independent
implementation and contract challenge. That reviewer previously supplied design
advice, disclosed in PLAN; root/design contribution is not counted as a separate
independent source vote. Its full independent C01–C07 disposition is **REVISE**
for C05/C06; the other source contracts are accepted only within their recorded
limits. All actual-instance obligations remain unfilled. Exact review:
`../baseline-coverage/LINUX-CLOSEOUT-SOURCE-REVIEW-v1.json`, SHA-256
`18a2871618bbf75c2a1e6843aa1aacd1a5fc2257b655883f2ba9eec7f1d074bf`,
and companion Markdown SHA-256
`d51f668387791584c80ac418cf0d1716fa85a20ebf21e236ff85ff251b1c7ecf`.

V1 reads the successor runner as hash-bound data only. The inspected runner
source checkpoint was `346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279`,
with PLAN `74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f`.
Actual later admission must bind final accepted runner/source/request/launcher
bytes, not these author inspection hashes as permanent execution authority.

The original allocation schema is35 original directories: R, checkout and33
named temporary/cache/generated roots. Only those33 roots are disposal candidates;
source, schemas, `.git`, reports and test-results remain protected. No current
filesystem descendant sampler is treated as creation proof. Separate filled
postrun REQUEST/TARGETS/EVIDENCE/launcher/acceptance documents do not exist by
virtue of writing this proposal.

## V1 revisions — author disclosures independently challenged and confirmed

1. Journal appends should compare the last full post-write regular-file pin and
   expected current length before touching the file, not merely the original
   dev/inode/UID/mode/link fields plus current descriptor/path equality.
   The independent reviewer separately confirmed that an intervening same-inode,
   same-length write is not presently rejected before append. V1 must be revised.
2. Stable input reads during admission currently run before Guard installation.
   Install a cheap bootstrap deadline/cancellation tick so the documented900s
   and monotone interruption checks cover those reads too. The outer hard-kill/
   blocking-syscall limitations remain separate; this is not a watchdog process.

Both are fixed in V2 and independently source-accepted by the separate V2 review
above. The exact V1 snapshots remain frozen; no accepting execution document
should bind that known-incomplete proposal. Preserve rejection and meaningful
unexecuted control requirements, rather than inventing a runtime failure or
claiming a green suite.

## Evidence and limitations

Only text/metadata inspection, static hashing/counting and authorized source/report
writes occurred. An early combined inspection command ended2 because future
`linux-runner/SOURCE.json` and this then-uncreated `linux-closeout` directory did
not exist; this was a preparation read diagnostic, not a project test execution
or successful admission. No missing originals were fabricated to hide it.
Static no-clobber copies preserved V1 and their hashes matched. GNU `cp` emitted
its non-portable `-n` behavior warning twice; the combined metadata command
exited0. This diagnostic was not suppressed and the copies are inert permanent
review evidence, not build caches or permission to replay a rejected helper.

Application executions/cases/closures added: **zero**. Helper lifecycle test cases
executed: **zero**. The baseline19/25 original confirmed,22/37 all confirmed and
2/12 suspicion denominators are unchanged by this work. Original source coverage
qualifications, PVA-029 FAIL/no automatic retry, PVU-007 STOP, PVU-011 NO RETRY,
G7/G8 CLOSED scopes, PVD owner choices and candidate1017001 restrictions remain.

Root must separately bind the accepted exact source/PLAN and final runner review,
obtain any required narrowly admitted inert controls, build admission, actual
original run outcomes, filled postrun closeout acceptance, bounded one-shot
execution and independent outcome review. A descriptor-close failure/outer
nonzero or incomplete journal is HOLD, not successful cleanup inferred from
terminal text or later absent paths.
