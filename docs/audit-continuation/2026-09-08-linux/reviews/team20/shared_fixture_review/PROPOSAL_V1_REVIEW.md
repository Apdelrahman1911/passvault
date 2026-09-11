# Shared fixtures v1 — independent exact-proposal review

Reviewer `/root/shared_fixture_review`, author `/root/shared_fixture_author`,
2026-09-11. **ACCEPT the exact UNAPPLIED v1 source proposal with the35 individually
qualified fixture exceptions below. This is not application verification, a
Detekt pass, a source-freeze release, execution admission, or a family closure.**

## Exact reviewed identity

Author files, relative to `B/reviews/team20/shared_fixture_author/`:

- `SHARED-FIXTURE-DETEKT03-v1.patch.txt`:9601B, SHA256
  `843430ffbb9ff85c9f2a8e772f8e7e5123306af85abf9fe61e8d08da74913146`.
- `PROPOSAL-v1.md`: SHA256
  `a5a9913d9db51e814d310b1a0903d299d9ce2dcec6a14d59e63cc6c27f791fac`.

Reviewer evidence in this directory:

- `REVIEW_INPUTS.json`, SHA256
  `7b322828e5236e9d21da6d84f104f5678012c390c70020c8bec47098d2575247`:
  original37 diagnostic tuples, matching Checkstyle/SARIF locations and exact
  C17 fixture/retained-evidence identities.
- `BASELINE_REVIEW.md`, SHA256
  `ebba8757972980aa048857cc5f1d65c75526920091422a5151bcdd921a1405ec`:
  independently read real persistence, native input/snapshot, pre-frame oracle,
  cleanup/cancellation contracts and existing evidence limitations.
- `PROPOSAL_V1_COMPARISON.json`, SHA256
  `388e71a23bf9161cd8e7b729437edc9447966cef1d5e95470ec90863dab2d1e9`:
  exact in-memory hunk/context/after-hash comparison, without writing source.

| Fixture basename | C17 before SHA256 | Proposed after SHA256 |
| --- | --- | --- |
| CredentialEditorRenderingTest.kt | `6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a` | `4e6fec750f0f995d459128111c13aa002d7c033980673ea7788742a6bf2d899c` |
| CredentialEditorRoomIntegrationTest.kt | `291c47edcca2de797d1a669947a2ded246ec5b32c120feec8843a2d41ec9ef57` | `109a0534da3023b7cc1f951ba6c4f8b57f8e0e78f718bef765f7d05cc3223417` |
| CredentialBackFreshnessCompositionTest.kt | `8df5c4f32ab2002bc602c6963f0ac1f86bfb1ea2518f5f3a9cda15e981bd75d0` | `f0614a75dfe0109e0effbd06e1903d1812d6be7c0fd9a97ef34a61ec4d6d42c3` |

Full paths are in the comparison report. All18 hunks matched exact current
source context. The only removed executable lines are the two old title/parent
initializations in each native driver; the only added executable expressions
are the two corresponding nullable-parent initializations. Other additions are
18 exact rule-specific `@Suppress` lines and explanatory line comments. No
imports, declarations, assertions, timing, cleanup code, seeds, runtime helpers,
test method names or source configuration changed. All11 declared test methods
(Rendering3/Room1/Back7), assertion/failure lines and all unrelated bytes remain.
Current source readback still matches all three **before** hashes.

## Two selector corrections: accept

Rendering original470 and Room original533, `Editor.dialogAdd` ReturnCount:

`unique(titleMatches)?.parent` has the same outcomes as the prior unique title
plus early-null return. Missing title or null parent skips the loop and returns
null. Duplicate titles still fail `unique`, not choose the first. A non-null
parent enters the identical loop at the identical index. Exactly-two-editable
controls, both named fields, parent traversal, the first qualifying ancestor
and its unique Add candidate remain unchanged. Missing/ambiguous Add at that
first qualifying ancestor still cannot fall through and choose a higher one.
Malformed parent/provider errors are not caught or downgraded. The loop's two
remaining returns keep this control flow without a new owner/helper/test.

This is source equivalence reasoning, not an executed accessibility test.

##35 intentional diagnostic exceptions: individual acceptance

Original locations below match retained Checkstyle; R=Rendering, D=Room,
B=BackFreshness. No file suppression, Detekt baseline, config/exclusion edit,
disabled assertion or generic success fallback is present. Metric diagnostics
are numerically accurate: acceptance is a bounded fixture-design exception, not
a claim that Detekt miscounted or that these functions are now shorter.

| Exact annotated symbol | Original diagnostic locations | Independent disposition |
| --- | --- | --- |
| R `withEditor` | TooGenericExceptionCaught201 | Accept. Catch/rethrow preserves AssertionError/native/setup primary through finally. Narrowing to Exception can leave primary null and let teardown replace the original assertion failure. |
| R private `Editor` | TooManyFunctions214 | Accept for this private15-operation driver. Operations share actual window/input/VM ownership; splitting solely for11 creates no substantive new boundary and obscures teardown authority. |
| R `Editor.start` | LongMethod230 | Accept for this67-line admitted-start boundary. Prerequisites precede registered acquisitions, composition and actual frame/native readiness. A60-line cap alone does not justify moving acquisition into a new owner or widening this patch. |
| R `Editor.withKey` | TooGenericExceptionCaught366,373; ThrowingExceptionFromFinally375 | Accept. The key is tracked before press; partial press/action failure still releases. Primary error is rethrown, secondary release attached; a release-only failure must fail the case. |
| R `Editor.click` | TooGenericExceptionCaught400,407; ThrowingExceptionFromFinally409 | Accept the analogous mouse boundary. Existing fresh-target assertions stay outside/unchanged; partial native press still attempts release and leaves failed cleanup tracking intact. |
| R `Editor.close` | CyclomaticComplexMethod581 | Accept. Visible owner-by-owner release/cancel/join order plus interrupted-state capture/restoration is intentional cleanup state, not a complexity-driven feature algorithm. |
| R `Editor.close.release` | TooGenericExceptionCaught585 | Accept **local-function scope** only. Assertion/native failure in one action is retained without skipping later owner cleanup. |
| R `onEdt` | TooGenericExceptionCaught682 | Accept. Any failed bounded FutureTask wait must cancel a pending action and rethrow; no blanket swallowing. It still cannot cancel an already running native/EDT action. |
| D existing test method | TooGenericExceptionCaught134 | Accept. Body/setup AssertionError remains primary across exact owner teardown. Opt-in/home prerequisite and every persistence oracle remain unchanged. |
| D private `Editor` | TooManyFunctions218 | Accept for this private14-operation real-Room/native driver. Bootstrap generations and input/VM/window lifetime are one fixture authority, not a production public API. |
| D `Editor.closeRoom` | CyclomaticComplexMethod394; TooGenericExceptionCaught401,412 | Accept. Both attempt flags precede work; lock failure must still reach close. Keep completed-job precondition, success-only closed flag, distinct-error suppression and interrupt restoration. No automatic retry is introduced. |
| D `Editor.withKey` | TooGenericExceptionCaught450,453; ThrowingExceptionFromFinally453 | Accept same registered key/primary-versus-release-only boundary as R. Source body is identical. |
| D `Editor.click` | TooGenericExceptionCaught476,479; ThrowingExceptionFromFinally479 | Accept same registered mouse/primary-versus-release-only boundary as R. Source body is identical. |
| D `Editor.close` | CyclomaticComplexMethod622 | Accept. Preserve all cancellations before bounded joins, reverse Room teardown, exact closed/window checks, current-model/event clearing and interrupt restoration. |
| D `Editor.close.release` | TooGenericExceptionCaught626 | Accept local aggregation so one failure cannot skip later independent owners; no broad class-level catch exemption. |
| D `onEdt` | TooGenericExceptionCaught709 | Accept failed-wait cancel(false)/rethrow exactly as R. |
| B `withEditorComposition` | LongMethod181; ThrowsCount181; TooGenericExceptionCaught234,240,245,251; ThrowingExceptionFromFinally242,253 | Accept this nested failure-preserving setup/teardown and outer Main restoration boundary. Four throws surface primary or cleanup-only failures, not unrelated branches. Collapsing finally/rethrows risks lost assertion evidence or resetting Main before cleanup settlement. |
| B `closeFixture.release` | TooGenericExceptionCaught270 | Accept local independent release aggregation inside NonCancellable cleanup, including assertion/cancellation failures. Each remaining cancellation/join/reset is still attempted. |

The table covers exactly35 findings:21 generic catches,6 primary-aware finally
throws,3 cleanup complexity metrics,2 private-driver function counts,2 ordered
boundary lengths, and1 nested-boundary throw count. An annotation is not an
approval to expand future bodies or suppress new unrelated defects; reconsider
these fixed symbol scopes whenever their logic changes.

## Unchanged critical behavior independently checked

- Real Room save still reaches the encrypted repository's transactional DAO
  update. The test still checks49 exact survivors, one pending secret row draft,
  page Save, form detach/VM settlement, vault lock/checkpoint/close, same-file
  fresh-bootstrap reopen without reseeding and50 exact tuples in a fresh VM.
  No persistence fallback, fake substitution or transaction weakening exists.
- Room editable-only accessibility payload capture and all tree/text/geometry/
  owner/cardinality bounds remain unchanged. Rendering remains real native
  input with copied-fake persistence and fixed exclusive synthetic screenshots.
- All body/primary versus cleanup-only propagation, pressed-input registration,
  interrupt handling, Room single-attempt flags and NonCancellable Back joins
  are byte-identical. No suppression weakens a runtime assertion or catches a
  newly ignored error. Existing limitations, including self-suppression/native
  stalls or failures outside cooperative bounds, are not universally disproved.
- The seven Back composition cases retain no-drain pre-frame assertions, stale
  composed policy, applied-composition count, route/full-stack consequences,
  synchronous save blocking, row-Cancel compatibility and disposal fail-closed.
  Their real registration/VM/navigator remains distinct from rendered NavDisplay
  or native gestures. No fake screenshot/accessibility/gesture proof is inferred.
- Synthetic admitted storage only; no root allocation, filesystem deletion,
  credential access, native enumeration or helper/runtime operation is added.

## Verification recommendation and mandatory qualifications

1. Keep v1 **UNAPPLIED** until root explicitly reconciles/releases the C17/GUI03
   freeze and assigns these exact paths. GUI03 is accepted but still recorded
   **UNINVOKED/UNCONSUMED**; GUI02 and Detekt03 are consumed. Even annotations
   change bound source bytes. This review cannot amend the existing request.
2. In a fresh independently admitted focused static04 batch, run only the
   relevant `:shared:detekt` selection alongside other actually affected modules,
   retaining the existing static-coverage dependency, strict verification and
   report triplet. This review predicts rule targeting; no postchange lint or
   compiler result exists. No full all22 rerun follows from these changes.
3. Preserve existing successful Rendering3 and Back7 evidence with exact-source
   qualifications, rather than rerunning them merely for annotations. If Room
   remains genuinely pending on a successor source, its one existing native
   Add/page-Save/reopen case exercises the modified selector meaningfully;
   separately bind/admit it. No additional trivial tests or standalone parser/
   synthetic controls are needed. If C17 GUI03 runs first, keep its actual
   identity and do not relabel it as runtime verification of the new hashes.
4. Family/PVU/PVD counts, physical-device/provider/native/IME/RTL gaps and all
   STOP/NO-RETRY/CLOSED/HOLD/publication restrictions remain unchanged. No
   guarantee of zero possible bugs, universal cleanup or production readiness.

Author's procedural note records an initial unintended sibling held-runtime/
publication directory-name listing, reported to root. This reviewer did not
repeat it; accepting these source bytes does not retroactively authorize that
operation. Author also clarified the prose's no-replay warning: GUI03 has not
become consumed. Neither qualification changes the exact proposed patch.

This reviewer performed bounded W source/retained-data reads, in-memory textual
comparison and own report writes only. No source writes, build/test/Git/CI,
project-helper import/execution, process/SDK/runtime probe, cleanup, central
ledger change or duplicate Telegram notification. Zero new application cases;
all foreground reads are closed and no persistent task was started.
