# PVA-007 Back freshness correction — independent after-image review

Reviewer `/root/editor_review`, author `/root/editor`, 2026-09-08.
**SOURCE ACCEPTED; TEST EXECUTION PENDING; NOT A PVA-007 CLOSURE OR RUN ADMISSION.**

## Exact reviewed change

`BACK-PATCH-INPUTS-v2.json` binds the dirty-worktree after-images to the containing
handoff HEAD, including the newly authored/untracked composition test. Its four
changed-file identities are:

| File, abbreviated by package | SHA-256 | Physical LF |
| --- | --- | ---: |
| shared/navigation/NavigationBackCoordinator.kt | `7f0f546301a87c0bd758d121a118d8325b2fe366561655d1ee5c8e1b248126db` | 181 |
| shared/navigation/adapters/VaultRouteAdapters.kt | `4a155566bd433f2905f6e13137d1b8e7b03da06a54fd9e12b706b4f7d3061b98` | 271 |
| shared/commonTest/navigation/NavigationBackCoordinatorTest.kt | `e777006c470eded3f4f8d8183c74857229a6cb1004dc98add17c6eab6743fa14` | 263 |
| shared/desktopTest/navigation/CredentialBackFreshnessCompositionTest.kt | `8df5c4f32ab2002bc602c6963f0ac1f86bfb1ea2518f5f3a9cda15e981bd75d0` | 373 |

Full repository paths and supporting-source hashes are in that JSON. These
lengths identify files, not whole-project coverage or executable test counts.
The immutable before-image finding challenge is `FINDING-REVIEW.md` with
`BASELINE-INPUTS.json`. No frozen handoff evidence was rewritten.

## Product challenge

- The coordinator selects the valid active registration and checks host state
  before consulting its optional current policy. Each eligibility/action call
  reads one disposition/forward-blocking pair; it does not combine values from
  different VM state reads. Missing registration, root fallback, stale session,
  active-entry identity and lifecycle behavior remain unchanged.
- Only credential registration supplies the new live policy. The detail,
  create and edit callers all use the same wrapper. Its provider reads
  `viewModel.state.value` and the existing `credentialBackDisposition`, not a
  captured state snapshot. An accepted edit can therefore change the action
  decision before the next `SideEffect` installs a new registration.
- The old composed disposition and forward flag still key registration changes
  and rendering invalidation. The provider is remembered by ViewModel identity;
  this avoids needlessly changing its `rememberUpdatedState` value on every
  child recomposition. Callback updates and identity-aware disposal remain.
- Existing registrations without a live provider retain their prior value-based
  behavior. Constructor defaults preserve existing call sites. No route keys,
  restoration schema, dependencies, identities, persisted format, Store version,
  PVD choice or native gesture implementation change.
- Clean credential Back still pops directly; newly dirty Back reaches the
  production VM's current-state `requestLeave` and confirmation. Newly busy
  state consumes Back and denies forward/completion. Interactive *completion*
  rereads the current policy; it does not establish pre-frame visual eligibility.

No additional product defect was found in this bounded after-image. In particular,
merely returning a live value does not automatically make the previously rendered
NavDisplay entry list fresh. That separate visual boundary remains blocked.

## Regression source quality and independent corrections

There are **nine new declared methods**: seven in the new Desktop composition
fixture and two added common coordinator methods. Running both complete classes
would select **18 declared methods**, including nine unchanged existing common
methods. **Zero were executed by this reviewer.** These are not nine/18 passes,
JUnit XML counts, task counts or additional historical execution events.

The composition fixture uses a real Compose `Composition`/`Recomposer`, real
`RegisterCredentialBack`, the production ViewModel and real navigator. It keeps
the composed clean disposition/counter unchanged between a synchronous draft
edit and the action. Fake credential persistence returns independent copied
secrets; the initial synthetic fixture is cleared after seeding. No Room/native
provider/application/clipboard or real vault is used.

Covered source assertions:

1. inline change before composition reaches VM discard confirmation without pop;
2. the shell-equivalent gate followed by same-HOME reselection cannot discard the
   current stack;
3. interactive completion rejects the newly dirty editor;
4. synchronous Save admission blocks stale clean Back/forward/completion;
5. row Cancel restores a clean direct-Back path before replacing the dirty
   registration;
6. unchanged draft stays clean, permits one pop and rejects duplicate completion;
7. disposing the actual registration makes the remaining guarded route fail closed;
8. each coordinator action reads exactly one current pair;
9. inactive host and an old session registration are rejected without evaluating
   their live provider.

The reviewer requested and rechecked these refinements rather than accepting
weak guards at face value:

- The first stale-session draft tested only a changed route/map lookup. The final
  test reactivates the **same** detail route after authentication, leaving the old
  registration in the map; `NavigationState.activateMain` restores its quarantined
  history, so token/session validation is actually necessary to pass.
- Forward testing now attempts the same-tab mutation behind the gate and compares
  the entire stack, rather than checking only a Boolean with no consequence.
- Cancelling the recomposer runner alone is not settling its disjoint effect
  job. Final cleanup requests every cancellation before joining, then separately
  joins the runner, Recomposer shutdown and captured ViewModel scope with bounded
  timeouts while Main is still installed. It disposes composition, clears the
  ViewModelStore, wipes fake repository copies and restores Main even if another
  cleanup step throws. Primary assertion and multiple cleanup failures are
  retained through suppressed exceptions. Official runtime source supporting
  cancel/join and idempotent disposal is in `CLEANUP-FRAMEWORK-SOURCE.json`.

`BACK-PATCH-INPUTS.json` retains the penultimate 355-line fixture identity; v2
supersedes only that fixture after primary-failure preservation was added. This
was a source-review refinement, not an executed failed application test.

Timeouts run on the test coroutine scheduler. They do not establish wall-clock
or noncooperative native/process termination. Future execution still requires
root's independently admitted process/resource/wrapper-stop/evidence cleanup.

## Remaining limits and disposition

- This is real **registration composition**, not rendered UI: the no-node applier
  does not construct CredentialEditScreen, NavDisplay, material text fields,
  actual key/pointer dispatch, IME or platform gestures. The tests intentionally
  control the pre-frame interval. Source scheduling evidence supports reachability;
  no delivered-input frequency or physical-device result has been observed.
- The same-tab test uses the production gate and navigator but reproduces the
  host's tiny conditional rather than composing the shell; no UI wiring test is
  inferred. The row-Cancel case is a compatibility control, not a claim that the
  former VM-mediated asynchronous clean Back could never complete.
- No original-fail/corrected-pass execution or compiler result exists. The core
  pre-frame assertions discriminate removal of live credential wiring at source
  level; no mutant was executed. Existing logical passes are preserved unchanged.
- A separate row-Save stale-argument lead within already-open PVA-007 was raised
  by this reviewer and independently confirmed by `/root/editor`. The present
  four-file Back patch does not repair that event path; follow its separate
  correction/review instead of declaring the editor complete.
- Rendered edit→Save/reopen/row Save/Cancel, native/predictive eligibility,
  delivered Desktop keys/repeat, lifecycle/IME and physical Android/iOS LTR/RTL
  work remain distinct. PVA-007 stays open. All original audit denominators,
  frozen qualifications and STOP/NO-RETRY/CLOSED restrictions remain unchanged.

Only source/metadata reads and compact report writes ran here. A bounded
three-tracked-file `git diff --check` returned 0; it did not include the then
untracked new test and is not a build, test or general whitespace gate. No build
artifact, source archive cache, daemon, test server or background worker was
created; wrapper stop is not applicable to these inspection commands. No shared
cache, source, permanent test, report or unrelated process was removed.
