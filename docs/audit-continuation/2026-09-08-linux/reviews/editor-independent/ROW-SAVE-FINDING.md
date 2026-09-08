# PVA-007 row Save freshness — continued existing family

Originator: `/root/editor_review`; independent challenger: `/root/editor`.
2026-09-08. **SOURCE CONFIRMED; NO RUNTIME EXECUTION OR NEW FAMILY/CLOSURE.**

This is another concrete boundary of the already-open PVA-007 requirement for
rendered edit→row Save/Cancel. It does not invalidate the earlier page-Save
adoption correction or the independent PVA-026 equal-owner fix. Neither declared
this pre-frame row callback verified. The separate Back freshness patch does not
change this path.

## Before-image identity and source schedule

The handoff before-images are bound in `BASELINE-INPUTS.json`:

- `CustomFieldsEditor.kt`, SHA-256
  `bb3f089979b76deb407430b2f0b442227a294c748b4a5ea2bb69c693e0b458f7`;
- `CredentialEventRouter.kt`,
  `a5251e738a65921fb03b7b9b6603bc8afd4fec6cc45565a988d52669231d2a00`;
- `CredentialFormSupport.kt`,
  `7fa675ec4de10ea34b0f7f8887abe9c86f735f967b91975b51d5c710545b1a28`;
- `CredentialViewModel.kt`,
  `c6e83a2367f7bab80a70753bd31e9572f707c8deca128ec7debb5f2f80bf41af`.

Full paths/ranges and immutable containing commit/tree are in that manifest.

1. A loaded, non-busy row is editing an initially nonblank draft `D0`, so its
   composed Save button is enabled (`CustomFieldsEditor` 132–140, 246–257).
2. Its input accepts `D1` through `OnCustomFieldDraftChanged`; the production
   handler synchronously publishes `D1` to the VM draft map
   (`CustomFieldsEditor` 173–215; `CredentialFormSupport` 217–230).
3. Before a new row composition, the existing Save callback still constructs
   `OnCustomFieldUpdated` using **D0's name, value and secret flag**, not D1
   (`CustomFieldsEditor` 137–138).
4. `CredentialEventRouter` 159–160 forwards those arguments. `update` uses them
   to retain/replace the stored field, then removes the current draft map entry
   and marks the form dirty (`CredentialFormSupport` 307–338). It does not check
   or adopt the newer D1. With D0 equal to the initial persisted field, the
   PVA-026 equality guard correctly retains that field owner but still drops D1.
5. A later page Save sees no remaining row draft to adopt. The accepted D1 is
   already lost from authoritative edit state; being globally dirty does not
   recover its contents. No real persistence or personal vault was exercised.

The pinned Desktop scene dispatches pointer events after measure/layout and
flushes effects afterward; it does not require full credential recomposition
before every click. `FRAMEWORK-POINTER-SOURCE.json` retains exact bounded
scheduling excerpts and hashes. `FRAMEWORK-SOURCE.json` binds the separate
render/frame mechanism. This does **not** claim that layout can never perform
subcomposition, or that an actual delivered click/IME schedule was tested.

## Independent challenge and counterexamples

`/root/editor` independently checked the current field/input guards, router and
owner replacement loop and confirmed the schedule. Its challenge found:

- Busy events are rejected, but the witness starts and remains non-busy.
- The old callback's enabled/nonblank guard passes; it checks the composed D0,
  not the newly accepted D1. A newly blank D1 also needs current-state rejection
  rather than silently committing the older D0.
- After a recomposition, a fresh callback would send D1; that is a working
  counterexample, not a guarantee for all dispatched input.
- Page Save calls `commitDrafts` and reads current VM drafts. Its adoption logic
  is not this failure and must remain intact.
- PVA-026's equal-value ownership safeguard prevents wiping the retained stored
  owner; it does not preserve the different uncommitted draft that this path
  removes. Do not revert it or group it as another new family.
- A late row Save after row Cancel currently still applies captured update
  arguments to an existing field. An id-only draft-commit path can deliberately
  no-op when that live draft no longer exists.

The challenger proposed a separate id-only row-save event/current-draft commit
path, keeping the explicit `OnCustomFieldUpdated` API and ownership semantics for
its existing callers. Root remains the only path/admission owner. Any such
after-image requires independent review and real meaningful regression results;
this finding report authorizes no build, UI/native run or test execution.

No new denominator entry follows: original confirmed families stay 19/25,
all confirmed families 22/37, and original suspicions 2/12 at this review
checkpoint. PVA-007 remains unclosed. STOP/NO-RETRY/CLOSED scopes, PVD choices and
publication/build-1017001 restrictions are unchanged.
