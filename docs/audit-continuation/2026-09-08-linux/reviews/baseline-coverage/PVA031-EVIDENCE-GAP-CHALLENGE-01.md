# PVA-031 — bounded evidence-gap challenge

Reviewer: `/root/baseline_coverage`; 2026-09-11. **KEEP OPEN: one narrow admission/rejection boundary remains unproved. No closure recommendation or ledger change.** This is not the final C18 coverage review.

## What already has meaningful evidence

The original handoff row identifies idle new/loaded credentials at50: Add was offered using only `canSave`; the canonical limit rejected field51, while the Unit-returning modal callback cleared/closed its input. It expressly says “no race/escalation asserted” and excludes universal owner-disposal/concurrent-editor/backend-ack guarantees.

- Linux03 actually passed **7 capacity cases**: new/loaded count admission, existing busy/unloaded restrictions, final-slot acceptance, direct VM capacity rejection preserving accepted fields, removal, load and page Save. The **13 draft siblings** concern existing-row owned drafts; they do **not** prove rejected new-modal input retention.
- GUI02 actually passed **one PVA031 native capacity case** within its three-case editor suite. At50, native Add is disabled and clicking it creates no dialog/change. Remove gives49; native typing populates the modal; controlled direct-VM fill returns50; after a real-frame settle, disabled confirmation leaves the modal/input intact. Removing the injected field permits native Add,50 unique surviving/added tuples, page Save and fresh-VM reload.
- These are real production form/VM callbacks with synthetic deep-copy repositories, not compilation or mock assertions alone. They are **not** Room/disk, pre-frame rejection, competing-editor or physical-device evidence. Preserve the prior accepted screenshots/reviews; no rerun is needed.

Current related production/test bytes still equal their retained Linux03/GUI02 raw-source bindings (GUI fixture introduced after Linux03). No source drift invalidates these successes.

## Material boundary versus wider qualifications

**Rapid/pre-frame admission is the narrow unresolved item.** `CredentialViewModel.kt:480–483` computes the shared count/busy predicate; `CustomFieldsEditor.kt:74–76,104–111,344–381` passes a *composed Boolean*, checks it, then treats `onEvent(...): Unit` as success and closes/clears. `CredentialEventRouter.kt:32–44,152–165` can reject busy edits; `CredentialFormSupport.kt:293–312` can reject a full current VM independently.

Thus a stale enabled confirmation followed by current-state rejection is not covered by the new UI guard or an acknowledgment contract. This is a conditional source counterexample, **not newly demonstrated user-reachable concurrency**. It must not be promoted into a new confirmed family without establishing the supported route. Conversely, the old “rapid” requirement cannot be called completed merely because the settled frame is correct.

**Backend rejection needs careful naming.** Linux03 already executes the real VM's full-capacity rejection, but has no modal to observe. GUI02 clicks a disabled confirm, so its assertion label “REJECTED_CONFIRM” does not mean a rejected backend callback ran. The relevant missing boundary is local same-editor admission/dismissal, not a blanket requirement for external persistence acknowledgments or a redesigned concurrent-editor product contract. The original exclusions remain exclusions.

**Keyboard/IME/a11y/RTL/mobile are not a separate platform matrix for this count defect.** GUI02 already uses native ASCII keyboard editing. Enter/Space confirmation, IME composition, accessibility activation, RTL layout and mobile Back remain untested qualifications; do not mark them passed. In the reviewed app path they reach the same guarded button/callback, and the count predicate has no locale/platform branch. No independent route bypassing those guards was established here. Broader input, layout, Back/disposal and genuine-device readiness work should remain explicitly tracked, rather than automatically require Apple/Windows/mobile builds for PVA031. A concrete alternate route or regression would change that assessment.

The original ledger explicitly requested rapid/keyboard/a11y/LTR–RTL validation. **Do not delete or silently waive that history.** Any later reclassification of broader requirements needs explicit root adoption with their unresolved/readiness destination retained. Historical G7 plugin failure and unknown cause also remain; later successful tests are not a diagnosis or retry of G7.

## Smallest meaningful missing witness

First establish the supported same-editor stale-confirm route, rather than assume another editor can mutate this VM. Then one bounded local production-dialog witness should distinguish:

1. composed admission is still true and a populated modal exists;
2. the same VM's current admission has become false before the next relevant frame;
3. an actual confirmation callback is delivered, versus merely suppressed;
4. current accepted fields/count, dispatch/rejection, modal lifetime and exact entered tuple are observed together.

A rejected addition must not be counted as acceptance/dismissal if the intended retained-input contract covers that transition. If the route is impossible under the supported event model, establish that reachability/guard fact instead of inventing an acknowledgment redesign. Preserve any counterexample/failure.

**Existing tests do not supply this combined witness.** CapacityTest110–126 has rejection without a modal; RenderingTest159–169 explicitly settles before the disabled click; DraftTest's duplicate-save/late-edit cases are different existing-row ownership. Repeating these successes or adding broad platform runs would not close this gap. No test/helper or execution is proposed/admitted by this note.

## Exact retained bindings

B = `docs/audit-continuation/2026-09-08-linux`.

| Evidence | SHA256 |
|---|---|
| Current `B/ISSUE_LEDGER.json`, PVA031 row30 | `0518fc89feab108b883132b033b25f2cb06e349de2997b2889065924822c9181` |
| `docs/audit-handoff/current/issue-to-fix.json`, row30 | `5782666330bc2eb017e42b7a8725db1efdf54223462f3648c8acf25cb21b245e` |
| `B/reviews/verification/LINUX-ISOLATED-BATCH03-ACTUAL-RESULT-REVIEW.json` | `f428020cf798125507c514c8b4d632dc58ab4f401494a29b0eaeb5001799df45` |
| `B/reviews/verification/LINUX-DESKTOP-GUI02-ACTUAL-RESULT-REVIEW.json` | `66f5cd60d4d245a845f2792ae97119f4384c31462267deefa3b141bcfdbe42a6` |
| Retained Linux03 capacity XML /7 cases | `a4823377e8012d29de7fba266188f0ba10cdd14e996caa5eb6bd7e4951f8d2a1` |
| Retained Linux03 draft XML /13 cases | `16520c20e9f4465b635e6d22d9fb2b5841c1eb002c7ae16288163f19a6e1cb40` |
| Retained GUI02 editor XML /3 cases, only1 capacity | `c70e65a32e8178d62200c3ffaa520054e8654a967c810e34d1f11e0ebfd9d27a` |

Current source pins: `CustomFieldsEditor.kt` dd60f4a65b4cf951a8ba94b2b6e4cab8440d1a33942eb046f1d74ccfcf9b31db; `CredentialViewModel.kt` b9a9c5e6a9e9a5eee86a13a639fa32826dbfdfbd09fa81a032935a37f58f0611; `CredentialFormSupport.kt` b3403f38c94808df3d9f9d227353fc0505bb0b3848d20a69da63689424a2a66c; `CredentialEventRouter.kt` b1f18f9e32beff113d0c6d26dc16fc3b9e24460ef6039a3dc144554815431f5a. CapacityTest b851a3fab0ea44109a6456896fb2936b99408aaeabb111c66d1bb96b1d1993bf; DraftTest b45ded2bb7e092e73975f7b7d4e77c8e649ae72f1b7f07db9445b47559c176c2; RenderingTest6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a.

Linux03 tested C4 da8ff89b9a8579017d5d524e628dff5251f2bb90 / cf0a8a702e7cd6948236be18b491bb5a21b5886e; GUI02 tested0d06721b7f8bdc17513a6ab51ffbc5523e68b230 / dd04904c1deb691bd6aeed756dd6ceab8ea97837. Current named bytes were compared to those retained manifest/blob records, not live Git/T. XML counts/names and zero failures/errors/skips were reread as data, not rerun.

Only bounded inert W reads/comparisons and this exclusive0600 permanent note; no new tests, helper/source edits, ledger changes, build/CI, Git/network, T/R/proc/SDK/cache probe or temporary/background work. New cases/closures:0; denominators unchanged. All STOP/NO-RETRY/CLOSED/HOLD/PVD/hardware/publication/1017001 fences persist. Wrapper stop N/A. Quiesced after sealing for root-owned Detekt03; no further source reads in this task.
