# PVA-007/031 editor residual: independent source review

Reviewer `/root/c20_cleanup_review`, 2026-09-12; author `/root/c20_cleanup_author`.

**SOURCE_ONLY_ACCEPTED_FOR_ROOT_INTEGRATION.** No requested code change. The
proposal adds one useful native toolbar/discard-dialog retention boundary inside
one already pending Room test; it does not add a test method, a product finding,
a verified fix, a passed case or a family closure. Integration and execution are
root-owned and are not granted by this review.

W = `/root/projects/PassVault/passvault-linux`.
B = `docs/audit-continuation/2026-09-08-linux` relative to W.
A = `B/reviews/team20/resume12/editor_residual_author`.

## Exact reviewed bytes

| Object | Bytes / LF | SHA-256 |
|---|---:|---|
| W `shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRoomIntegrationTest.kt`, before | 37913 / 726 | `109a0534da3023b7cc1f951ba6c4f8b57f8e0e78f718bef765f7d05cc3223417` |
| A `CredentialEditorRoomIntegrationTest.kt.proposed.txt`, after | 40037 / 763 | `bec3d1d8d6be6aad88585103788975f7292010282422a2bbe844efb2d5309b7c` |
| A `EDITOR-TOOLBAR-RETENTION.patch` | 2968 / 53 | `77b25931860dbc1ae0cce1c9bc8ed9eb134ccfdeed2fb832595f53145f7da885` |
| A `EDITOR-RESIDUAL-PROPOSAL.md` | 6255 / 94 | `82f60aa5e94874cc92403e084ccfc8300919d4e89f5d1d6d7cad4252c4050a2e` |
| A `SOURCE-RECEIPT.json` | 4219 / 94 | `c626348a331f0f27c34ee256491ad49a159e2116b8e93f20cf9ae4ed356468fc` |

Independently recomputed the before/after unified diff: it exactly equals the
sealed patch. Only one call/comment plus one driver method are added, 37 LF;
`@Test` remains 1 before and after. No existing helper, admission, assertion,
cleanup, production source, Main/NavHost fixture, dependency or build setting is
changed. W still matched the before bytes at this review's comparison point;
that observation is not an instruction to overwrite a later root integration.

All ten additional named source/evidence pins in the author's receipt matched
independent byte/size/LF reads. Relevant current form/router/model, rendering
fixture, dirty-Back test and English resource paths are therefore bound by that
receipt. Additional focused source read:
`feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/ui/components/CustomFieldsEditor.kt`,
15335 bytes / 446 LF, SHA-256
`dd60f4a65b4cf951a8ba94b2b6e4cab8440d1a33942eb046f1d74ccfcf9b31db`.
These are file identities, not a Git/tree or runtime identity.

## Independent challenges

1. **Ordinary reachability is established, not injected.** The real form's
   toolbar `Back` IconButton constructs `OnCancelClick`; the router's leave
   handler has an ordinary not-busy/dirty branch that opens the confirmation.
   The real dialog's `Keep editing` constructs `OnDiscardCancel`, which clears
   that flag. Existing setup has completed load, native Add and native inline
   editing; the extension checks not-busy and no prior discard dialog. It
   neither invokes `onEvent` directly nor introduces a fake repository success,
   private callback, scheduler gate, artificial stall or backend rejection.
2. **The native/framework boundary is actually exercised by the proposed
   oracle.** Existing `clickEvent` sends Robot mouse press/release after owned
   active-window, stable geometry and enabled-target checks, then observes the
   named production callback count. `Back` and `Keep editing` use the existing
   exact-one PUSH_BUTTON cardinality. Read-only accessibility locates targets;
   it never invokes accessible actions or mutates text. Warning acceptance
   requires both the VM flag and a visible `Discard changes?` node; dismissal
   requires both flag clearance and absence from the owned visible tree. The
   native title/control realization still needs an actual admitted run; source
   review cannot establish its runtime cardinality or focus behavior.
3. **Retention uses meaningful independent state boundaries.** It snapshots
   all accepted tuples and the pending name/value/secret tuple, checks both
   remain identical after dismissing the warning, and checks dirty state.
   The rendered editable name is reached by the existing scroll driver and its
   text compared; checkbox checked state is observed separately, not claimed
   as a screenshot or plaintext-secret oracle. Existing page Save then consumes
   the still-open draft; the unchanged real encrypted repository/Room close,
   new bootstrap, unlock and fresh VM supply the planned persistence oracle.
   There is no additional Room generation, crypto operation, test method or
   standalone validation cycle merely for this extension.
4. **A real counterexample limits the claim.** Accepted Add already made the
   editor dirty. Even broken draft-only dirtiness could still show this warning;
   this test must not claim to prove draft-only dirtiness. The existing logical
   DraftTest covers that distinction. This fixture has neither a NavHost nor
   an effect observer; an unexpected navigation effect would not establish a
   visible navigation consequence here. Therefore no no-navigation, protected
   entry, central/OS Back, tab/forward, pre-SideEffect or owner-disposal claim is
   accepted. The author explicitly preserves these limits.
5. **Nonredundancy is narrow and sufficient.** Prior GUI02 covers native page
   Save, row Save/Cancel and controlled capacity, not the rendered toolbar and
   warning-dialog roundtrip. Logical Back/no-effect coverage cannot verify
   those native controls and modal wiring. Conversely, another real-backend
   success/acknowledgement test would duplicate the existing Room Save/reopen
   boundary. Keyboard/IME, key repeat, RTL and rejected/late backend-ack cases
   would need their own genuine framework/platform input witness; no such
   witness is manufactured or required as a surrogate in this patch.
6. **Compatibility and cleanup remain bounded.** The helper uses only existing
   driver/model/data types and native input infrastructure. The English/LTR
   Linux opt-in, private-home checks, 240-second driver deadline, bounded
   individual waits, existing VM/native/Room teardown and caller-owned final
   cleanup are unchanged. No new worker, server, directory, secret read, file
   deletion or external authority is introduced. These unchanged source
   guards do not constitute a cleanup execution or resource-floor waiver.

## Reused evidence and remaining work

- `B/reviews/verification/LINUX-ISOLATED-BATCH03-ACTUAL-RESULT-REVIEW.json`,
  SHA-256 `f428020cf798125507c514c8b4d632dc58ab4f401494a29b0eaeb5001799df45`:
  preserve 49 logical/Composition editor-navigation cases. The 13 draft and
  7 capacity cases are subsets, not added credit. The current matching Back
  test body directly dispatches toolbar/central events and checks effects;
  it is not a native toolbar/dialog test.
- `B/reviews/verification/LINUX-DESKTOP-GUI02-ACTUAL-RESULT-REVIEW.json`,
  SHA-256 `66f5cd60d4d245a845f2792ae97119f4384c31462267deefa3b141bcfdbe42a6`:
  preserve 3 actual native editor cases, not all 4 GUI02 cases (the fourth is
  the distinct Desktop curtain case). These successes retain their original
  exact source/runtime and cleanup qualifications; current source edits do not
  silently extend their executed pins.
- Reuse the already sealed PVA031 investigator/paired result, respectively
  `daebc5e0ab913ce89fb820d15d2ce6b7b9d16d9a4f3b410f3419961c35b717d2`
  and `9be25fe6e705efce7a8a581f9d505b8363ba84181c06cd8fa629efc58998c0e1`,
  at the receipt's exact locators. No new supported-input producer search or
  reopening is undertaken. Disabled confirmation remains different from an
  executed rejected callback; real late input/old confirmation and owner
  dismissal qualifications remain unresolved.

Root may integrate only against matching before bytes, then include this
extension in the already necessary single Room test under fresh exact admission.
Current status is **unexecuted**: no compilation, XML pass or cleanup pass is
provided here. A later result must bind the actual integrated source/classpath,
matching method XML with no skips/failures/errors and admitted final settlement
and cleanup. No historical GUI02 replay, fresh full matrix, new helper request or
automatic build/retry is proposed. Main fixtures remain the Apple pair's scope.

This review adds zero findings, closures, suspicion resolutions or executed
cases and does not recalculate central totals. PVA007/PVA031 remain open under
all existing unverified qualifications, including central/mobile/predictive Back,
key/IME/repeat/RTL, screen-reader, supported late-delivery/lifecycle and genuine
platform/device boundaries. The eight PVD explanations/decisions stay separate.

## Reviewer activity and restrictions

Only bounded source/evidence reads, standard-library byte/hash/diff comparison
and this permanent reviewer-leaf creation were performed. Comparisons completed
successfully; they are source-consistency checks, not application tests. No
project/helper import or execution, build/test/CI, Git/network, process/SDK/cache
or held-runtime probe, cleanup helper, source/control/central-ledger edit or
subagent was used. No disposable output, daemon, test server, emulator or
background worker was created; wrapper stop is not applicable.

Root alone owns integration and any execution/cleanup. Mandatory resource floors,
all HOLDs and consumed admissions, PVU007 STOP, PVU011 NO RETRY, PVA029 recorded
failure/no automatic retry, G7/G8 CLOSED, native refusal, protected publication
boundaries and occupied build1017001 are unchanged.
