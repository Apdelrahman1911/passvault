# Credential Detekt03 proposal — 2026-09-11

**UNAPPLIED. Four retained static diagnostics, not four product findings.**
Author: `/root/credential_author`; independent counterpart: `/root/credential_review`.
Root must reconcile the GUI03/C17 freeze and explicitly authorize exact source
paths before integration. No application/test/helper source or central ledger was
edited. `credential-detekt03.patch.txt` is inert text; `SOURCE_BINDINGS.json` binds
the four measured before-images, proposed after-images and retained evidence.
The C17 commit/tree in that record are retained context, not a fresh Git check.

## Minimal changes

| Retained diagnostic | Proposal | Preserved contract |
| --- | --- | --- |
| `CredentialEventRouter.kt:160` — MaxLineLength | Wrap the value-event branch after `->`. | Identical event, field id, supplied value and synchronous dispatch; no admission/return change. |
| `CredentialFormSupport.kt:345` — ReturnCount | Local `finished` controls the existing retry loop; two missing-owner guard returns remain. | Same snapshot, CAS, allocation and owner clearing on every attempt; no helper or owner extraction. |
| `CredentialCustomFieldCapacityTest.kt:207` — MaxLineLength | Wrap the existing `CustomField` constructor arguments. | Same four arguments in the same order; no test/assertion/fixture change. |
| `CredentialCustomFieldSaveFreshnessTest.kt:460` — ThrowingExceptionFromFinally | Comment and exact rule suppression on private test fixture `withEditor` only. | Every existing body/cleanup statement remains byte-identical. Suppression requires independent acceptance. |

The final diagnostic is intentionally challenged as a guarded test-boundary false
positive, not suppressed throughout a file/module or via config/baseline. The
throw happens only when there is **no** primary failure to discard. If the body
already failed, the exact primary throwable (including cancellation) is rethrown
and cleanup failure is attached as suppressed. Rewriting this ownership boundary
solely to hide a diagnostic is not proposed.

## `updateField` outcome equivalence

Each attempt still reads `state.value`, derives the current draft and searches
that same snapshot for the field, in the same order. Neither a draft nor a
`SensitiveText` owner is hoisted across retries. The only added state is a local
Boolean recording that this attempt has finished.

| Outcome | Original and proposed behavior |
| --- | --- |
| Missing draft | Immediate return before field search, allocation or mutation. |
| Missing field | Immediate return before allocation or mutation. |
| Required name blank; error CAS fails | No replacement allocated; restart from a fresh state snapshot. |
| Required name blank; error CAS succeeds | Preserve draft/value, publish the same error, finish. Proposal's `continue` evaluates only `!finished` before exit. |
| Valid draft; update CAS fails | Clear only this attempt's unused replacement; retry with a fresh snapshot/draft/field. |
| Update CAS succeeds; tuple unchanged | Preserve live field/value owner, clear only unused replacement, finish. Equality-based CAS may succeed without installing a new state reference. |
| Update CAS succeeds; tuple changed | Clear only the replaced owner after publication; retain the new owner, finish. |

The explicit failed-CAS `else` is essential: accepted replacements must not fall
through to the loser cleanup. Exceptions from the unchanged operations still
propagate; no catch, retry policy, cancellation handling, error clearing,
`isDirty`, draft removal, input bounds or Boolean return API changes.

Replacing returns with breaks was rejected: it would add multiple break/continue
statements, a secondary Detekt rule in the retained SARIF. A local termination
flag avoids that problem without extracting security-sensitive ownership logic.
The candidate has two returns and the original single continue; a future actual
Detekt result is still required, not inferred from those counts.

## Guarded fixture cleanup remains unchanged

| Body | Cleanup | Preserved result |
| --- | --- | --- |
| Success | Success | Complete normally. |
| Success | Failure | Fail with cleanup throwable, never silently pass. |
| Failure/cancellation | Success | Original body throwable remains primary. |
| Failure/cancellation | Failure | Original remains primary; attach cleanup throwable. |

`closeFixture` still runs under `NonCancellable`: clear the owned store, cancel
the real VM job, bounded-join it, reset the synthetic repository, then reset
Main, attempting later actions even after an earlier cleanup failure. No timeout,
assertion or suppression aggregation was changed. No injected/trivial permanent
test was added just to satisfy lint.

## Overlap and retained qualifications

PVA031 investigator was notified and confirmed source-only coordination. This
proposal does not change Add, `canAddCustomField`, modal input storage, accepted
confirmation clearing, busy admission, Save callbacks or owner disposal. It does
not prove or disprove PVA031's broader stale-confirm/input-retention scenarios.

The existing independently accepted property/row source hashes match the three
overlapping before-images in `INPUT-CALLBACK-PATCH-REVIEW.md`. Its forced-CAS-retry
argument was source-only and remains so. The retained Linux03 actual review lists
11 freshness, 13 draft and seven capacity cases successfully executed; that is
historical evidence for the old bytes, not a run of this proposal. GUI02/native,
IME/mobile/RTL and hardware qualifications remain separate. No family is closed.

## Smallest reliable future validation recommendation

After independent acceptance, root freeze release, fresh exact-source binding
and execution admission only:

1. Include **`:feature:credential:detekt`** in the reviewed future focused-static
   candidate, preserving the existing inventory guard/config. Do not repeat all
   22 analyzers or loosen rules to claim success.
2. For the changed loop, select these **eight existing methods** in a freshly
   admitted `:feature:credential:desktopTest` execution:

   `com.passvault.feature.credential.presentation.CredentialCustomFieldSaveFreshnessTest`
   - `row Save captured before further edits adopts the latest draft and preserves another row`
   - `a latest blank name rejects stale enabled row Save without losing its draft`
   - `unchanged and duplicate row Saves preserve the live value owner`
   - `late row Save cannot resurrect an unknown cancelled removed or locked draft`
   - `busy generation rejects row Save while the pending draft still exists`
   - `explicit update keeps supplied payload bounds and does not become draft Save`

   `com.passvault.feature.credential.presentation.CredentialCustomFieldDraftTest`
   - `duplicate row Save preserves the live value and page Save persists it`
   - `unchanged row Save in a dirty editor never clears the live owner`

These exercise changed/unchanged adoption, missing-owner/blank/busy/lock rejection,
input/API compatibility and explicit-update equality-CAS ownership. They do not
force failed CAS, so no concurrent-race execution claim may follow. Reuse prior
unchanged capacity/GUI/other cases instead of replaying them for formatting or a
fixture-only annotation. No build/test/Detekt/helper execution occurred here.

Permanent STOP/NO-RETRY/CLOSED/held-runtime restrictions and root-only build,
cleanup, admission and publication authority remain intact.
