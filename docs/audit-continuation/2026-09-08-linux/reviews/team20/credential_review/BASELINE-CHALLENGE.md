# Credential Detekt03 — independent baseline challenge

Reviewer: `/root/credential_review`; 2026-09-11. **Source/evidence only.**
This baseline was inspected before receiving the author's patch. Application,
tests and helpers remain frozen; this is not execution or integration admission.

## Bound source and retained finding

Retained C17: `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49`, tree
`d1bd6ca5b18d08ff3ff15896d9a78af9016be799`. The four current source hashes agree
with `reviews/checkpoint17/source-prepare01/SOURCE.json` (SHA256
`e03d29e54974413ff893f96d9bcf0c70bbf11eeeaf114ac96296987456db1f7f`). These are
retained C17 bindings and a bounded current-file comparison, not a fresh Git check.

All following source paths are under `feature/credential/src/`:

| Path | Current SHA256 | Retained diagnostic |
| --- | --- | --- |
| `commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialEventRouter.kt` | `b1f18f9e32beff113d0c6d26dc16fc3b9e24460ef6039a3dc144554815431f5a` | line160, MaxLineLength |
| `commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialFormSupport.kt` | `b3403f38c94808df3d9f9d227353fc0505bb0b3848d20a69da63689424a2a66c` | line345, ReturnCount4/limit2 |
| `commonTest/kotlin/com/passvault/feature/credential/presentation/CredentialCustomFieldCapacityTest.kt` | `b851a3fab0ea44109a6456896fb2936b99408aaeabb111c66d1bb96b1d1993bf` | line207, MaxLineLength |
| `commonTest/kotlin/com/passvault/feature/credential/presentation/CredentialCustomFieldSaveFreshnessTest.kt` | `d492ed289c21904c6f1145580aaf2980bd99e9a2ae6ccaf624b03c3d4faef0af` | line460, ThrowingExceptionFromFinally |

Exact retained diagnostic report:
`runs/linux-detekt03/reports/feature-credential-checkstyle.xml`, SHA256
`fcca61d425dab71e0916e1ed97749f1c98ec142b9519f9ce2f3c1e2fcabde63c`.
Detekt03 is a consumed static failure, not four application failures or test cases.

## Material counterexamples any correction must survive

1. **Failed normal CAS:** a state change between snapshot and CAS must not wipe
   the still-live replaced value. Wipe only this attempt's replacement, then read
   the next state, draft and field again. Hoisting a secret allocation or draft
   outside the retry loop would be a material semantic change.
2. **Equality-CAS / duplicate explicit update:** StateFlow can accept an equal
   update without installing its new reference. Unchanged fields must retain the
   original `SensitiveText`; only the unused replacement is cleared. Clearing
   the old value unconditionally would corrupt the live state. The existing
   draft tests explicitly exercise this with a dirty editor and repeated update.
3. **Latest blank draft:** row Save must reject the latest blank name, retain its
   draft and live field, and publish only the error. A lost invalid-state CAS
   must re-read; it must not terminate as though error publication succeeded.
4. **Absent identity/draft:** cancelled, removed, unknown or locked row Save
   performs no field creation or secret allocation. Explicit update remains a
   different API: no active draft is required, supplied bounds remain enforced,
   and blank-name validation remains the later page-Save concern.
5. **Cleanup versus primary failure:** the fixture catches `Throwable`, retains
   and rethrows the primary, and attaches cleanup failure as suppressed. A
   cleanup-only failure still fails the test. Removing the throw, swallowing
   cleanup, or moving cleanup out of `finally` would weaken the oracle. The
   diagnostic is locally intentional because the throw occurs only when there
   is no primary failure to discard; precise method-local suppression is a
   defensible alternative to restructuring this test owner.
6. **Partial fixture setup / cancellation:** nullable repository/store/job owners
   allow cleanup after partial acquisition. `closeFixture` remains
   `NonCancellable`, attempts store clear, job cancel, bounded join/completion
   assertion, repository reset and Main reset even after earlier cleanup errors.
   It aggregates failures instead of reporting an incomplete teardown as success.

The two long lines need only wrapping; property-only event payload and positional
synthetic constructor arguments must not change. No new helper or permanent test
is needed merely to satisfy these metrics. A completion flag in the existing CAS
loop can preserve owner boundaries; replacing several returns with jumps may
instead trigger the independent loop-jump rule. Exact proposal review is separate.

## Guards and oracle limits

Inspected relevant VM save/load/lock cancellation, router category/busy guard,
custom-field UI/dialog, domain `SensitiveText.equals/clear`, and existing
freshness/draft/capacity tests. Router busy rejection occurs before custom-field
mutation; `canAddCustomField = canSave && size < 50`. Row editing and new Add are
different owners. None of this establishes an atomic new-Add acknowledgement or
universal rejection-input retention after disposal.

PVA031 stays open. The Add dialog has synchronous confirm followed by name/value
clearing; disabled confirm is not a backend-rejection witness. The main-page
password IME `onDone` Save action is not evidence that it is reachable while the
modal owns input. The dedicated PVA031 reviewer was told this distinction.

The retained independently reconciled Linux03 selection has freshness11, draft13
and capacity7 successful cases; these remain historical, not executions in this
review. Existing cases test event/state/ownership behavior with synthetic copied
persistence, not a forced failed-CAS interleaving or complete rendered/device race
matrix. No new family or closure is asserted.

## Authority and scope accounting

Read TEAM_20_RESUME, saved pause, handoff START_HERE/PERMISSIONS/ASSEMBLY,
PUBLISHED_PAYLOAD, W AGENTS, focused production-readiness/security/navigation
instruction snapshots and applicable source/evidence. All skill commands remain
dormant under the task's narrower authority.

One initial `find .. -name AGENTS.md -print` traversed sibling directory names
and listed AGENTS paths including publication/held-runtime checkouts. No sibling
file content was opened or modified. This scope lapse was reported to root;
subsequent access was W-bounded. No Git, build, test, CI, helper execution/import,
process/SDK probe, cleanup or application/runtime action occurred. Only this
review directory is writable by this assignment. STOP/NO-RETRY/CLOSED, consumed
runs, held runtimes and GUI03/application freeze remain unchanged.
