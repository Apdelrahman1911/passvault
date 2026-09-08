# PVA-007 cross-control input freshness — independent confirmation

Originator `/root/editor`; independent challenger `/root/editor_review`.
2026-09-08. **SOURCE CONFIRMED; EXISTING OPEN FAMILY; ZERO TEST EXECUTIONS.**

This is a separate boundary of unfinished rendered custom-field editing, not a
new family or a failure of the id-only row-Save fix. The exact reviewed row-Save
after-image is bound in `INPUT-CALLBACK-FINDING-INPUTS.json` and
`ROW-SAVE-PATCH-INPUTS.json`, based on HEAD
`488ab465125234e02bfbd28b3e7579251341cb54`. These are before-images for a separately
authorized input correction and must remain distinguishable from its successor.

## Witness and surrounding guards

1. A loaded, non-busy row has composed draft
   `D0 = (name0, value0, isSecret=false)`. `state.canSave` enables the editor and
   all three input controls (`CustomFieldsEditor` 90–98, 132–165).
2. Its value input receives `value1`. The composed handler emits
   `D0.copy(value=value1)` through `OnCustomFieldDraftChanged`, and the synchronous
   VM handler stores that tuple (`CustomFieldsEditor` 189–213;
   `CredentialFormSupport` 217–230). The new text has been accepted into owned VM
   draft state, not merely an IME's unsubmitted composing buffer.
3. Before the row is recomposed, activating the secret toggle emits
   `D0.copy(isSecret=true)` (`CustomFieldsEditor` 229–240). Its callback still
   includes **value0**, despite changing only the secret control.
4. `changeDraft` verifies a draft exists, clamps the submitted name/value, then
   replaces the complete map value. It does not merge unedited properties from
   the current map entry. The accepted `value1` is overwritten by `value0`.
5. Either the corrected id-only row Save or existing page Save can now correctly
   adopt the latest map value yet still persist `value0`: the earlier accepted
   text is already absent. Dirty/confirmation state cannot restore its contents.

The same mechanism applies to name→value, value→name, and an earlier secret edit
followed by an older text callback. It is enough to establish one two-control
schedule; this review does not count each permutation as another finding.

The pinned official Compose Desktop source separates scheduled effects from
frame-driven recomposition. Pointer dispatch prepares measure/layout and flushes
effects afterward; it does not require a full row recomposition before dispatch.
See the previously preserved `FRAMEWORK-SOURCE.json` and
`FRAMEWORK-POINTER-SOURCE.json`. These are same-coordinate upstream source excerpts,
not bytecode-matched artifacts or observed platform/UI schedules. Layout may
subcompose in some situations; the claim is absence of an unconditional freshness
guarantee, not that recomposition can never intervene.

## Attempted disproof and compatibility constraints

- The router rejects edit mutations while busy, but the witness is loaded and
  non-busy throughout. Editing a draft does not itself set a busy flag.
- The map-existence guard rejects a late callback after draft cancellation or
  removal, but both witness callbacks target the same still-active draft.
- Same-control text callbacks carry the entire **current text for that control**.
  With unchanged sibling properties, successive same-control callbacks are a
  working counterexample. This does not protect a different control's property.
- An intervening row recomposition supplies a current tuple and prevents the
  witness. That successful common schedule is not an enforced ordering guard.
- `SecureTextField` forwards the supplied text `onValueChange` to
  `OutlinedTextField` (38–113); its visibility state/effects do not merge the
  draft's other properties. The plain non-secret path already establishes the
  witness, so no secure IME/native behavior is assumed.
- PVA-026 owner preservation, current row Save and current page Save operate
  after this overwrite. None can reconstruct a lost earlier value. Their existing
  successful evidence is preserved.
- `OnCustomFieldDraftChanged` is a whole-tuple API used by existing tests and
  callers. A correction should add property-only UI events or an equivalent
  latest-state transform, preserving its explicit full-tuple meaning rather than
  silently treating arbitrary submitted differences as a field-specific patch.

The independently confirmed scope was sent to root; root authorized a bounded
property-only input correction in the already-owned four product paths and tests.
This report itself grants no additional execution/cleanup authority. Every such
after-image and regression must still be independently challenged.

No rendered event, application, native/IME or hardware test ran, and no delivery
frequency is claimed. PVA-007 remains unclosed, denominators and all
STOP/NO-RETRY/CLOSED, PVD and publication restrictions stay unchanged. This review
created only compact source-bound documentation and no temporary/build output or
long-lived worker.
