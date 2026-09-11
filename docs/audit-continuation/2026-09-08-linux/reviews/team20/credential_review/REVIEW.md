# Independent credential Detekt03 proposal review

Reviewer: `/root/credential_review`; 2026-09-11.

**Disposition: ACCEPT SOURCE ONLY, UNAPPLIED.** No material regression was found
in this bounded challenge of the exact four-file proposal. The single private
fixture-method suppression is independently accepted for the documented reason
below. This is not a Detekt/test pass, integration permission, PVA031 closure or
a claim that all possible bugs are excluded.

Application/tests/helpers and the C17/GUI03 freeze remain intact. Root alone may
reconcile that freeze, authorize integration of these four paths, and admit new
exact-source validation. No consumed runner is reusable execution authority.

## Exact packet reviewed

Paths below are relative to `docs/audit-continuation/2026-09-08-linux` (`B`).

| Artifact | SHA256 |
| --- | --- |
| `reviews/team20/credential_author/credential-detekt03.patch.txt` (4,035 bytes; four files/five hunks) | `8d680148698fc42be221c1019273129a4db65aee82a945896c0b2b84b1ff5ec3` |
| `reviews/team20/credential_author/SOURCE_BINDINGS.json` | `9076369b0923dae14a7c825cc3dece347a3030dbf6d55bf883a08097aa5a560a` |
| `reviews/team20/credential_author/RATIONALE.md` | `1b7334881893661e88a256063aa020ea13a2ebad7095ba7d468f699fa61f9f23` |
| `reviews/team20/credential_review/PROPOSAL-DATA-CHECK.json` | `ff355586d3b6331e926b2b573c3416d26d665410d6fd175529c36a7e4c6cd156` |
| `reviews/team20/credential_review/BINDING-DISCLOSURE-REFRESH.json` | `e5826ab6961ef8c7d1cf0c0972ffcf083b9e45b9ecc40075d31a6cf3bbdad0de` |

The original proposal data check is preserved verbatim. On explicit source-only
resume, the reviewer verified the author binding changed only its instruction-
discovery disclosure: removing that added field and restoring the old execution
wording reconstructs the prior binding SHA256
`b9acb52e2d21cee41ba61567fe809382054df70c687f38bcd45f72e64fc62e25` exactly. All source/proposal
tuples, patch and rationale are unchanged. The refresh record above connects
that earlier comparison to the current binding; no design review or test was
repeated. GUI03 actual-result/cleanup disposition remains root-owned and does
not release the source freeze or validate this proposal.

`BASELINE-CHALLENGE.md` records the independent pre-proposal source review and
four full source paths/before hashes. They match the retained C17 manifest,
`reviews/checkpoint17/source-prepare01/SOURCE.json`, SHA256
`e03d29e54974413ff893f96d9bcf0c70bbf11eeeaf114ac96296987456db1f7f`:
commit `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49`, tree
`d1bd6ca5b18d08ff3ff15896d9a78af9016be799`. This is a retained binding, not a
fresh Git observation. Current source stayed at those before hashes.

The reviewer reconstructed the proposal **only in memory as text**, checking
every hunk context and before/after line count, then independently matched all
four proposed hashes to the author's record:

| Source filename (full path bound in the data check) | Proposed SHA256 |
| --- | --- |
| `CredentialEventRouter.kt` | `0423e8e78c70fa6944efa771feb33feb91169f7e458bf22036e21067fbf4c255` |
| `CredentialFormSupport.kt` | `73e9e401ae34d6f7e4f562d2cad2ed66738c1c1d450c5b3720daecad3ae38e7d` |
| `CredentialCustomFieldCapacityTest.kt` | `5bd1b2316812977c733f74b8ee15929907cb47c040cfe3577f0062152e9acff4` |
| `CredentialCustomFieldSaveFreshnessTest.kt` | `68eb476cd47b9cda26c86b1ecb2ce9e64f167770291a818beb41400e96953819` |

These byte comparisons are not Kotlin parsing, compilation or analyzer results.

## Decision on each retained diagnostic

### 1. Router line160: accept whitespace-only wrapping

`OnCustomFieldDraftValueChanged` still invokes
`customFields.changeDraftValue(event.fieldId, event.value)` synchronously.
No whole-draft snapshot is substituted for the changed property, no callback
return is changed, and no busy/category admission guard is modified. All router
tokens remain identical under whitespace splitting.

### 2. FormSupport line345: accept local loop termination, not owner extraction

`updateField` alone gains `finished`, changes its loop condition, records invalid
CAS completion, and replaces normal successful return with completion. The
existing loser wipe is placed in the failed-CAS `else`. Everything outside this
method is byte-identical. No draft, state snapshot or secret allocation is moved
outside its attempt; no new helper, ownership abstraction or persistence contract
is introduced to satisfy ReturnCount.

The reviewer challenged these outcomes directly against the old method:

| Outcome | Why the exact proposal preserves the old behavior |
| --- | --- |
| Missing current draft | Original guard return remains before field lookup/allocation. |
| Missing field | Original guard return remains before allocation; no resurrection. |
| Blank required name, error CAS fails | `finished=false`, then existing continue; read a fresh snapshot/draft/field on retry. |
| Blank required name, error CAS succeeds | Same error publication; preserve draft/value. Continue reaches only a false loop condition, not a new state read or allocation. |
| Normal update CAS fails | Explicit else clears only this attempt's unused replacement; `finished` remains false and the next attempt rereads state. |
| CAS succeeds, tuple unchanged | Clear unused replacement only; retain the original live field/value owner, including equal-state CAS without reference replacement. |
| CAS succeeds, tuple changed | Publish replacement, then clear the old owner only. Set completion and exit; the loser wipe is unreachable. |

The explicit else is material. Simply replacing the successful return with a
flag while keeping an unconditional trailing `replacementValue.clear()` would
wipe the newly published secret. This proposal does **not** do that. Likewise,
unconditionally wiping `replaced.value` on equality-CAS or failed CAS would
corrupt the live owner; the proposal preserves the discriminating branch.

Operations that can throw, their order and existing propagation are unchanged.
The added Boolean assignment/condition does not add cancellation handling,
delay, retry limits, state mutation or cleanup policy. Explicit update still
accepts its bounded supplied tuple without requiring an active draft, and its
blank-name check remains a page-Save concern. Row Save still uses the latest
owned draft and rejects a latest blank name without discarding it.

The method has two remaining return statements and one existing continue, no
break. This avoids the secondary many-jumps concern without demanding cosmetic
loop revisions. Actual analyzer acceptance still requires the future static run.
No forced failed-CAS runtime coverage is claimed by this source reasoning.

### 3. Capacity test line207: accept constructor layout only

Same id, name, synthetic sensitive value and alternating secret flag, in the same
argument order. The only non-whitespace token addition is the permitted trailing
comma. Every assertion, test name, input bound and cleanup operation remains
unchanged. No new test is added to work around lint.

### 4. Freshness test line460: accept exact private-fixture suppression

The added `@Suppress("ThrowingExceptionFromFinally")` applies only to private
`TestScope.withEditor`; it does not cover the class, other functions, module,
configuration or baseline. The preceding comment states the actual exception
precedence. Removing these two inserted lines reproduces the entire old test
file byte-for-byte, including all assertions and cleanup.

This is a guarded test-boundary false positive, not suppressed evidence of a
discarded primary failure:

| Body result | Cleanup result | Existing and proposed outcome |
| --- | --- | --- |
| Success | Success | Normal completion. |
| Success | Failure | Throw cleanup failure; cannot become a false pass. |
| Failure/cancellation | Success | Rethrow the original primary. |
| Failure/cancellation | Failure | Original stays primary; add cleanup failure as suppressed. |

Partial repository/store/job ownership remains nullable and `closeFixture`
remains in finally. Its `NonCancellable` block still attempts store clear, job
cancel, bounded join/completion assertion, repository reset and Main reset in
order; later releases are attempted after earlier failures. Existing primary and
cleanup handling is preserved rather than extracted or weakened to appease the
rule. This acceptance is specific to these unchanged guarded bytes; future
cleanup edits need their own review.

## Adjacent scope and evidence preserved

Read relevant VM save/load/lock cancellation, router busy/category guards,
domain equality/destructive clearing, custom-field UI ownership and existing
freshness/draft/capacity oracles. Baseline challenge records exact relevant
contracts and the source-only limitations.

This patch does not change Add, the 50-field limit, `canAddCustomField`, modal
input ownership, callback acknowledgement, accepted-confirmation clearing,
busy admission or disposal. It supplies no new PVA031 reachability/retention
claim. Row-save late-event tests cannot be promoted into a universal Add-dialog
retention or backend acknowledgement guarantee. The main-page password IME
Save action is distinct from an established modal/global Save key producer.
Independent PVA031 investigation/review remains a separate lane.

Retained Linux03 freshness11/draft13/capacity7 success is old exact-source
evidence, not execution of this proposal. Retained Detekt03 remains the consumed
failure with four credential diagnostics. GUI02 scope and GUI03's separately owned
execution/result/cleanup gate, mobile/IME/RTL/device gaps and all publication qualifications survive.
No finding-family counts or central ledgers are changed by this review.

## Smallest materially useful future validation

Only after root reconciles the freeze and admits a newly bound execution:

1. Include `:feature:credential:detekt` in the separately reviewed focused-static
   candidate; preserve inventory guards, configuration and dependency checks.
   Do not repeat the all22 cycle or replay Detekt03.
2. Because the loop control actually changes, requalify **eight existing methods**
   via the real `:feature:credential:desktopTest` target: the first six
   `CredentialCustomFieldSaveFreshnessTest` methods plus
   `CredentialCustomFieldDraftTest`'s `duplicate row Save preserves the live value
   and page Save persists it` and `unchanged row Save in a dirty editor never
   clears the live owner`. All eight complete, exact method selectors are in
   `PROPOSAL-DATA-CHECK.json`; each resolves once in current source.

This is focused verification of newly changed control, not replay permission
for an old runner. It covers latest/blank/absent/busy/locked row paths, explicit
update compatibility, changed adoption and equality-CAS owner preservation.
It does **not** force a failed CAS or prove device/rendered race behavior.

No capacity7, GUI, all31 credential, all166 regression or unchanged-module replay
is justified by the two wraps or unchanged fixture annotation. No new permanent
test or cleanup helper is requested for lint. None of the above runs occurred
in this review; source acceptance must not be relabeled as runtime verification.

## Access and permanent fences

Only report files in `reviews/team20/credential_review/` were written. Reads,
source hashing and in-memory standard-library text/data comparison were the
only work; there was no project code/helper import or execution, build/test,
Git/CI, process/SDK probe, cleanup or source application. The initial sibling
AGENTS filename-enumeration scope lapse is explicitly recorded in
`BASELINE-CHALLENGE.md` and reported to root; no sibling content was opened.
No broad zero-access claim is substituted for that fact.

PVU007 STOP, PVU011 NO RETRY, PVA029 retained failures/no automatic retry, G7/G8
CLOSED, consumed-run and held-runtime obligations remain untouched. Root-only
build/admission/cleanup/publication ownership and exact C17/GUI03 restrictions
remain authoritative.
