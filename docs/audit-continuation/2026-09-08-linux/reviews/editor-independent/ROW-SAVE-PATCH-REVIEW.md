# PVA-007 row Save correction — independent source review

Reviewer `/root/editor_review`; product/test author `/root/editor`.
2026-09-08. **SOURCE ACCEPTED; EXECUTION PENDING; NO CLOSURE OR RUN ADMISSION.**

## Sealed after-image and finding ownership

`ROW-SAVE-PATCH-INPUTS.json` binds the four product files, complete new test and
bounded supporting reads to HEAD `488ab465125234e02bfbd28b3e7579251341cb54`, tree
`4f4c594ff19e62e19f641304b95eb6c086781ae7`, with the row correction still dirty.
The finding's independent confirmation and immutable before-images remain in
`ROW-SAVE-FINDING.md` and `BASELINE-INPUTS.json`. This reviewer originated that
existing-family variant; `/root/editor` challenged it, authored the correction,
and this reviewer separately challenged the correction. No new family is added.

| Sealed file | SHA-256 |
| --- | --- |
| CredentialViewModel.kt | `409a96629397c68cacd4e8b2e30a776ae77164ada6c742c56b401c21fe2db8df` |
| CredentialEventRouter.kt | `1e3f1181835b734e90da7ac7e3b602ee0a3d55ff957646a430b9b09279e08522` |
| CredentialFormSupport.kt | `8742df885951a2c3db6c2081c89345cd7d2a373e35b957f0f8b07c44c456d9b7` |
| CustomFieldsEditor.kt | `1c7d5759bdb6e4b59f65fa0fd97c9a36c7f64d4000df82ccd333fea15e3204b5` |
| CredentialCustomFieldSaveFreshnessTest.kt | `94131184ff94fec3f4c243385a61af3f731ed146dd681a7f7857e323a7b0f35c` |

Full paths, raw byte/LF identities and reviewed ranges are in the manifest.
Hashes are source bindings, not executable results or whole-project coverage.

## Product challenge

- The rendered row now emits only `OnCustomFieldEditSaved(field.id)`. A callback
  created for an older draft cannot itself submit that draft's stale tuple.
  `CredentialEventRouter` routes this event into `commitDraft`; exhaustive
  category membership also makes the existing busy-state edit guard apply.
- `commitDraft` reads the current map entry inside **each** `updateField` CAS
  attempt. A failed CAS cannot subsequently install its outdated captured draft;
  an unused sensitive replacement is cleared before the next attempt. This retry
  property was source-reviewed, not observed under forced concurrent execution.
- A missing/cancelled/removed draft or absent field causes a no-op. A current
  blank name is rejected while retaining the pending draft and stored owner,
  even if the old composed Save button was enabled. This uses the same blank-name
  requirement as page Save; it does not introduce trimming or a new persisted
  format.
- PVA-026 remains intact: an unchanged name/value/secret tuple retains the live
  stored field/value owner and clears only the unused replacement. A changed
  successful adoption clears the replaced owner, not the published replacement.
  Duplicate row Save with no remaining draft does nothing.
- Explicit `OnCustomFieldUpdated` remains a separate payload-based API: it still
  accepts the supplied tuple, clamps name/value by Unicode code points, works
  without a live draft, and leaves its historical blank-name rejection to page
  Save. Its previous equality-ownership behavior is preserved by the shared
  implementation. The change is not a silent reinterpretation of this API.
- Page Save's existing current-draft adoption, Cancel, remove, busy flags and
  state-based Back decision remain unchanged. No dependency, application
  identity, version, persistence schema, PVD choice or platform capability changes.

No blocking defect was found in this **row-Save** after-image. Source acceptance
does not imply every input callback or rendered interaction is fresh.

## Regression assertions and cleanup challenge

The sealed new class has **six declared test methods / 362 physical LF**. The
unchanged `CredentialCustomFieldDraftTest` has 13 methods; selecting both complete
classes would select 19 declarations. **Zero test cases were executed here.**

1. A previously captured id-only Save adopts the latest live tuple, preserves
   another pending row and its value owner, wipes the replaced owner, and reaches
   a copied fake-repository roundtrip through production page Save.
2. A newly blank name defeats the formerly enabled Save without losing the new
   draft; page Save also refuses it, preserving fake persisted data.
3. Equal and duplicate row Saves retain the correct live sensitive owner, and a
   later changed Save preserves that newly adopted owner across another duplicate.
4. Unknown, no-draft, cancelled, removed and cleared-for-lock cases cannot
   resurrect a draft or field. The remove/lock assertions distinguish state and
   owner cleanup from fake persisted contents.
5. Password generation holds the VM busy **while a draft really exists**. Save
   is rejected in that state and accepted after the scheduled generation ends;
   this is not a vacuous busy test with no draft left to save.
6. Explicit update remains payload-based despite a different pending draft,
   retains supplementary-Unicode bounds, and preserves no-draft/blank compatibility.

These use the real ViewModel, router and editor with synthetic data and copying
fake persistence/crypto. Capturing an event models an older callback; the fixture
does **not** construct the actual row, deliver a click, operate an IME, exercise
Room/SQLCipher/native storage, or reopen an application screen. The new id-only
API did not exist in the before-image; no before-fail/after-pass or mutant run has
been performed. Source wiring and nontrivial ownership/persistence assertions are
more than compilation or a mock-only assertion, but execution remains necessary.

The fixture owns its ViewModel through `ViewModelStore`. Cleanup clears the store,
cancels and bounded-joins the actual VM scope while its test Main dispatcher is
still installed, clears copying repository ownership, and restores Main even when
another cleanup action fails. Primary assertion errors retain secondary cleanup
errors as suppressed exceptions. Synthetic seed ownership and inspection copies
are explicitly cleared. The timeouts are test-scheduler bounds, not wall-clock
or noncooperative native/process termination; root's separate process cleanup
admission remains mandatory for any future run.

## Integration, remaining work and resource discipline

`BACK-ROW-INTEGRATION-REBIND.json` explicitly supersedes the old supporting VM
whole-file identity in `BACK-PATCH-INPUTS-v2.json`. The four Back product/test
hashes are unchanged; the earlier VM Save/lock ranges and dirty-state predicate
are byte-identical. The only VM change is the new event declaration. The row
router/editor/UI deltas were re-read against the live Back policy: they do not
weaken its busy/dirty/confirmation checks or change its disposal/session guards.
**Back source acceptance is retained**, not promoted to an executed result.

During this review `/root/editor` independently raised a separate cross-control
`draft.copy` overwrite. This reviewer confirmed it; see
`INPUT-CALLBACK-FINDING.md`. The present row patch reads the latest authoritative
draft but cannot recover input already overwritten by another stale input
callback. Root authorized a separate property-only input correction after this
seal. Any resulting overlapping product/test identities require a new manifest
and review, not silent reuse of this six-method after-image.

PVA-007 remains open; rendered row/name/value/secret interactions, row/page
Save/reopen/Cancel, IME/lifecycle/native input and genuine hardware evidence remain
separate. Original confirmed families remain **19/25**, all confirmed families
**22/37**, original suspicions **2/12** at this source-only checkpoint. None is an
overall readiness percentage. STOP/NO-RETRY/CLOSED, publication/build-1017001 and
PVD restrictions are unchanged.

Only source/metadata reads and compact review writes occurred. No Gradle/test/app
process, daemon, emulator, temporary build/cache or archive was created; wrapper
stop is not applicable to these inspections. No shared cache, source, permanent
test/report or unrelated process was removed. At 21:29:02 UTC, the filesystem had
29,793,676 KiB available and RAM had 43,212 MiB available. Report-only files remain
small and are necessary resumable evidence.
