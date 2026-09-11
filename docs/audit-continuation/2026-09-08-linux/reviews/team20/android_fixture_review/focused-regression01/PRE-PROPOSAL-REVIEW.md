# Focused regression01 — independent scope and mechanism challenge

Reviewer `/root/android_fixture_review`, 2026-09-11. New assignment, distinct
from the completed Android32 fixture review. **SOURCE/DATA ONLY.** Supports the
bounded scope below, not an unseen helper, an exact instance or execution.

## Scope independently checked before receiving the proposal

Authority is root's new assignment plus the two accepted source reviews:

- `B/reviews/team20/credential_review/REVIEW.md`, SHA256
  `fe9b1882fd673b79f8cf4dfe7e039d5eadff0abbc94350b505016746fb354904`.
- `B/reviews/team20/database_review/FINAL-PROPOSAL-REVIEW.md`, SHA256
  `6fe7803c781b03c2200f49d43da4823d068f345530ea07fadaa03c154605e1d9`.

Here B is `W/docs/audit-continuation/2026-09-08-linux`.
The exact credential selector names were read from that review's
`PROPOSAL-DATA-CHECK.json`; the twelve biometric names were checked against the
database review and current source.

| Target | Class | Selected / source declarations |
| --- | --- | ---: |
| `:feature:credential:desktopTest` | `com.passvault.feature.credential.presentation.CredentialCustomFieldSaveFreshnessTest` | 6 / 11 |
| `:feature:credential:desktopTest` | `com.passvault.feature.credential.presentation.CredentialCustomFieldDraftTest` | 2 / 13 |
| `:core:database:desktopTest` | `com.passvault.core.database.repository.BiometricUnlockFreshnessIntegrationTest` | 12 / 12 |
| `:app-desktop:compileTestKotlinDesktop` | Compile-only; no test class selected | 0 cases |

Exactly20 existing methods, three XML suites and two Gradle Test tasks. Every
selected name resolves once in current source and once in retained Linux03 XML
as `(exact FQCN, literal source method + '[desktop]')`. Historical XML checks
were data reads, not test execution or new-source runtime credit:

| Input | SHA256 |
| --- | --- |
| Current Freshness source | `d492ed289c21904c6f1145580aaf2980bd99e9a2ae6ccaf624b03c3d4faef0af` |
| Current Draft source | `b45ded2bb7e092e73975f7b7d4e77c8e649ae72f1b7f07db9445b47559c176c2` |
| Current biometric Freshness source | `1108e4b0d4353c840974ac0ed57331e257947e9bded200a52bc73afd3b9d3e0e` |
| Linux03 Freshness XML (11 old cases) | `e0f7b8092853452cd13d2808106a2f2aa1e48b56671f762dd25cc5582fcf9b42` |
| Linux03 Draft XML (13 old cases) | `16520c20e9f4465b635e6d22d9fb2b5841c1eb002c7ae16288163f19a6e1cb40` |
| Linux03 biometric Freshness XML (12 old cases) | `634bedc13ac6038c72d34887b791971e20550eef8413ccfc8b8d7fb775c333c8` |

Source paths are the named classes under credential commonTest and database
desktopTest. XML paths are under `B/runs/linux-isolated-batch03/xml/`, named
`ordinary-TEST-<FQCN>.xml`. Source excerpts, existing fixture cleanup and JVM
module wiring were independently inspected. Prior patch/body reviews are reused
within their qualifications rather than repeated as a new product audit.

Whole credential-class filters would execute24 methods, not the selected8;
“first six” is a review locator, not a reliable test-execution ordering rule.
Use literal complete selectors. No new test, capacity7 rerun, all31/all166,
provider producer/consumers, Android task discovery, shared/GUI test, static
duplicate, native authentication or application launch belongs in this batch.

## Minimal useful execution shape — prospective only

One wrapper invocation can request the two Test tasks and Desktop compile task,
followed by one original wrapper stop. These fixtures do not need the former
five cold-provider phases or additional worker launches for Desktop compilation.
Database requires its real in-memory Room/crypto provider plus synthetic key
store; credential uses synthetic persistence/crypto with real VM ownership.
Neither is OS biometric or rendered/IME proof, and the eight credential cases
do not force a failed-CAS race. Existing test finally/tearDown logic must remain.

Keep separate private database/credential worker HOME/tmp/JNA/SQLite/XDG roots,
JDK17 startup properties, one worker/fork, no parallel/configure-on-demand,
no build/configuration cache and strict dependency verification. No inherited
provider fixture/mode, GUI display/agent, JVM-option override or real user storage.
The native Desktop bridge is conditional on macOS/Windows in the inspected
source; the requested Linux compile target is not an app Test/run/package task.
Actual realized graph evidence remains required rather than guessed.

An exact three-path graph/outcome marker in the retained Gradle log is a
proportionate way to distinguish compile execution from a bare task header.
Require exactly the two selected Test paths, unchanged exact method filters and
the requested compile path. Success cannot be inferred from SKIPPED, NO-SOURCE,
UP-TO-DATE, omitted/disabled compile or twenty unrelated XML cases. Reject
unexpected executable/GUI/device/publication/static tasks; do not reject every
ordinary JVM Jar/KSP/resource dependency or use broad full-path `sign` matching
(`:core:designsystem` is a concrete false positive). Do not create a generic graph
framework or retry/discovery campaign merely to select these proven literals.

The XML mapper should preserve original files first and require exact3 paths,
FQCN/name multisets, suite names,12/6/2 counts and zero failure/error/skip nodes.
Reject duplicates/missing/extras and stale source/outputs. Missing results after
compiler failure mean unstarted/partial tests, not twenty failures or passes.
Declared counts remain distinct from actual cases; setup/cleanup/helper roles
and the compile action contribute zero test cases.

## Mechanism provenance and integration hazards sent to author/root

Read these helpers **as text only**, without importing/executing/parsing AST:

| Basis | SHA256 |
| --- | --- |
| `scripts/audit/linux_isolated_batch_03.py` | `4ceaef097a1db97b673e7233aac5ff77acf817d298c2aa8adc33ec86be075c79` |
| `scripts/audit/isolated_batch_tests_03.init.gradle` | `6e333500d4a9d56599bd22417d89005d26940240228b1f0710773f5f441d0f45` |
| `B/reviews/detekt03-outer/LAUNCH.py` | `829000bc8403215b4313d197caeedc21c2f45ba1064b8c6e4378e53b5c5421b4` |
| `B/reviews/desktop-integration03-outer/LAUNCH.py` | `24560eb22ee51f3c743476213ed6a9b15acd748566156090cfd66d2b6be8273e` |

The consumed Linux03 result supplies prior mechanism evidence, not new execution
authority. Its C4/1572 source, retired Gitdir, old pins and five-mode workload
must not be transplanted. Fresh source commit/tree/member count/manifest, every
class/source/helper/image/instance binding, original lock/device/tool facts and
current root publication store admission remain UNBOUND until root supplies them.

Initial challenge identified a concrete mismatch: current Detekt03 outer expects
static-only sealed-index/read-only Git mounts and GIT-PREFLIGHT/result fields,
which the Linux03 Test inner does not produce. Do not forge those proofs or leave
an unwritten incompatible outer prerequisite. Author then identified the smaller
current GUI03 raw-source outer, without static index/Ruby mechanics, as a textual
basis. This direction is acceptable in principle, subject to exact delta review.
GUI03's actual terminal70/no-tests/current-R HOLD is **not** successful mechanism
execution and remains unrelated and untouched.

The new outer must be complete, wholly UNBOUND and current-T-only: preserve exact
raw-object request/stream framing/digest/source checks and descriptor-bound new
R cleanup. Remove GUI tools/roles/directories/result consumers, not cleanup
checks. Align parent/argv/preflight/ownership to the proven pid+mnt interface
without stale `net` fields or a new private-network/no-escape claim. Preserve the
accepted host-screen text and its fail-closed uncertainty/departure rules.

## Cancellation, stop and cleanup counterexamples

Preserve build intent before launch, exactly one original stop obligation after
intent, original wrapper/JDK/source verification on stop, cancellation handling
that does not skip stop, same direct-child pidfd identity, monotonic TERM/KILL
state, bounded pipe drainage and private PID1 namespace settlement. No Java-name
killing, PID substitution, fresh replacement runtime or automatic stop retry.

Retain the600s original stop allowance and120s cumulative namespace settlement
when adapting the one-cycle schedule; any smaller work budget must leave their
reserve. Current outer supplies an original work deadline: do not silently
reset it to inner-start time. The exterior750s drain is not permission to restart
work after cancellation. Resource12GiB/25% launch and8GiB/20% running floors,
bounded file/inventory/log/XML evidence and original source-before/after remain.
Heap flags/point samples are not aggregate kernel containment or global-idle proof.

Failure of compilation/test mapping can allow cleanup **only** when original
stop/settlement/source/evidence safety proofs and actual terminal0/1 permit it.
Inner/outer70, cancellation uncertainty, missing source-after, failed stop,
changed/mounted/unknown R, unclassified process ownership or descriptor-close
failure cannot be normalized into success or blanket deletion authority. Preserve
the complete allowlisted original FD-bound snapshot before removal, mount checks,
each original entry identity, parent fsync and final actual exit. A preterminal
JSON receipt is not the final close/exit outcome. Earlier held runtimes are never
traversal or cleanup candidates.

## Status

Independent initial findings were sent to author/root before an exact draft.
Scope and minimal design supported; exact source/selector/result/outer review
pending. Source/helpers/T remain frozen; root alone may reconcile, bind, admit,
execute, clean and edit central records. STOP/NO-RETRY/CLOSED, native refusal,
occupied1017001 and all old consumed failures/holds remain unchanged.

Only this reviewer's reports were written. No builds/tests, Git/CI, process/SDK/
runtime or broad sibling probe, helper execution/import/AST, network operation,
source application, central-ledger edit or worker occurred. Source-data readers
completed. No new wrapper-stop obligation or new application result was created.
