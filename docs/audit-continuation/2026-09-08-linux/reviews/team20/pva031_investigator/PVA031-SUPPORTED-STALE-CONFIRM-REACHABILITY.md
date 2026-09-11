# PVA031 — supported stale-confirm reachability, source-only finish

Author: `/root/pva031_investigator`; 2026-09-11. Independent counterpart:
`/root/pva031_review`. W is `/root/projects/PassVault/passvault-linux`; B is
`docs/audit-continuation/2026-09-08-linux` relative to W.

**Disposition: bounded producer/guard investigation finished; supported populated
stale-confirm rejection NOT ESTABLISHED. Keep PVA031 open. No patch justified by
this investigation, no new defect/fix/case/closure, and no execution proposal.**
This is neither universal impossibility nor completion/waiver of the historical
rapid/keyboard/a11y/LTR–RTL/mobile qualifications. Root alone adjudicates status.

## 1. Evidence reused, not rerun or enlarged

The current row remains `IMPLEMENTED — VERIFICATION BLOCKED`. Original variants
are idle new/loaded credentials at50; original grouping explicitly asserted no
race/escalation and excluded universal owner-disposal/concurrent-editor/backend-
ack retention. Read the saved pause, TEAM_20_RESUME, prescribed handoffs and
focused navigation/security/back/localization instruction snapshots. Their old
commands were treated as dormant.

- Linux03 has seven actual successful capacity cases: count admission,
  busy/unloaded **predicate** restrictions, final-slot acceptance, direct-VM
  full-capacity rejection preserving accepted fields, removal, load, page Save.
  The busy/unloaded case is not an actual populated-modal rejection test.
- Thirteen successful draft siblings concern existing-row owned drafts, not the
  new modal's remembered tuple. They cannot supply a rejected-new-input oracle.
- GUI02 has one relevant capacity case within three editor cases: native Add at50
  is disabled; native removal gives49; native text input populates the real modal;
  controlled direct-VM fill gives50; **after real-frame settling**, clicking the
  disabled confirm leaves the composed name/value; removal then accepted native
  Add/page Save/fresh-VM reload preserve the tuple and surviving fields.
  `PVA031_REJECTED_CONFIRM_DISPOSED_DIALOG` is only an assertion label: the disabled
  click did not execute a rejected VM Add. The repositories there are synthetic
  deep-copy in-memory owners, not Room/disk or another live editor.
- Existing accepted actual-result reviews and the evidence-gap challenge are
  reused. No new XML, screenshots, helper execution, runtime observation or
  independent rerun/recount is claimed. Pending GUI03/Room complements remain
  separate and cannot by declaration prove this pre-frame boundary.

`CredentialCustomFieldCapacityTest.kt:68–85,110–126` and
`CredentialEditorRenderingTest.kt:140–190,260–280,318–339` were reread as source.
Four core production hashes and both named test hashes still match those printed
in `reviews/baseline-coverage/PVA031-EVIDENCE-GAP-CHALLENGE-01.md`. This is a bounded
current-byte comparison, not a new whole-tree/raw-transport verification.

## 2. Actual app boundary and owner model

Line references below use the pinned files in section6.

1. `CustomFieldsEditor.kt:62,74–76,104–112` owns one Boolean `showAddDialog` and one
   private `AddCustomFieldDialog`. The production construction of
   `OnCustomFieldAdded` is at109. Caller searches across app-android, app-desktop,
   core, feature and shared Kotlin source (excluding test/build directories) found
   no second production Add producer. The only form caller is
   `CredentialEditScreen.kt:325–329`.
2. The UI receives a composed immutable `CredentialState`.
   `canAddCustomField = !isBusy && (isNewCredential || isCredentialLoaded) && count<50`
   (`CredentialViewModel.kt:480–483`). The composed `canConfirm` Boolean is not a
   live VM read or an acceptance acknowledgement.
3. `AddCustomFieldDialog:349–379` stores name/value/secret in remembered mutable
   state. Confirm re-reads `name`, checks composed `canConfirm && name.isNotBlank()`,
   calls its Unit callback, then clears name/value. The parent callback dispatches
   `OnCustomFieldAdded` and sets `showAddDialog=false` before returning. All of this
   app-level call chain is synchronous: `CredentialViewModel.onEvent:204` →
   `CredentialEventRouter.handle:32–44,152–165` → `CredentialCustomFieldEditor.add`.
   There is no app-owned Add queue, suspension or persistence acknowledgement.
4. Router rejects Add when current `isBusy` because Add is an edit mutation
   (`CredentialEventRouter:33,342–353,389–400`). Otherwise `FormSupport:294–312`
   rejects at current count>=50 with a limit error, or creates and appends a field.
   Both return Unit. The count check is outside `state.update`, so this is **not**
   a general atomic concurrent-caller capacity guarantee. No such supported
   concurrent producer was established; arbitrary direct-VM calls would assume it.
5. Create/edit/detail adapters each request an entry `koinViewModel`; Nav3 has the
   ViewModelStore decorator (`VaultRouteAdapters:76–153`, `NavigationHost:328–339`,
   `AppModule:203–227`). Load/create run in effects keyed to that owner and route
   arguments. `getById` is a one-shot load, not a live credential subscription.
   Another editor's save does not, in this source path, push a field into this
   editor's existing VM. This is source ownership reasoning, not a concurrent-
   editor or complete Nav3/lifecycle runtime guarantee.

## 3. Producers that could invalidate composed admission

| State change | Production source | Supported-path consequence / limit |
| --- | --- | --- |
| Count increases | Sole modal Add, `FormSupport:294–312` | First accepted Add can consume the last slot, but the same callback also hides the dialog and synchronously clears its name/value. See the distinct late-input counterexample below. |
| Other field edits | `FormSupport:200–290,315–382` | Row draft/Save/update map existing rows; remove decreases count. Page Save commits existing-row drafts, not new-modal input. None independently fills an extra slot. |
| Credential load | `ViewModel:111–155`; adapters90–92,147–149 | Loading state is installed synchronously before launching `getById`; the loading form has no editor (`EditScreen:85–93`). Success installs loaded fields and makes the record editable. No supported reload trigger while a populated modal remains owned was found. Initial/re-entry load is not evidence of an independently arriving same-owner count increase. |
| `isSaving=true` | `ViewModel:206–226` | Validation and existing-draft adoption run first, then the busy flag is set before launching persistence. Producers are page Save at `EditScreen:162–164` and **main password-field IME Done at282–284**. A local keyboard Save producer exists; it must not be described as absent. |
| `isDeleting=true` | `ViewModel:275–281`; `EventRouter:178–186` | Set before launching delete. UI delete/confirm belongs to detail, whose VM is entry-scoped, not an additional control on the populated edit modal. |
| `isGeneratingPassword=true` | `ViewModel:338–344`; `EditScreen:270–285` | Set before generator launch. The editor's password Generate control is separate from the modal. Desktop Ctrl/Cmd+Shift+G opens the generator route; it does **not** call this VM's generator (`DesktopWindow:194–201`). |
| `isAttachmentBusy=true` | `AttachmentController:76–110,131–181,258–267` | Every import/open/export/rename/delete calls synchronous `beginOperation` before launch, including before awaiting a file chooser. No chooser-completion producer that newly sets busy after a modal was populated was found. |
| Async completion unrelated to capacity | `ViewModel:170–201,229–272,345–382,399–420`; `AttachmentController`; `TotpController:105–117,160–187` | Folder/TOTP/usage updates do not set these busy flags or increase custom-field count. Save/generation/attachment tails clear their busy flag; they do not independently start another one. Load completion is the initialization case above. |

Synchronous busy admission is a real guard fact, **not a guarantee that every
possible framework input schedule sees an updated frame**. In the ordinary
single-modal interaction, page controls are outside the dialog, and a busy action
begun before Add is composed does not already possess a populated Add draft.
However, app source alone does not prove every delayed page IME/accessibility/key
callback is suppressed, nor every collection/recomposition ordering. A callback
delivered from outside the modal while its input is still owned could supply the
otherwise missing busy producer. No retained native witness establishes that.

### Keyboard, menus, and modal text fields

`KeyboardShortcuts.kt:17–88`, `MenuBar.kt:18–75,79–219`, and the closed
`AppCommand` enum/consumer (`AppCommandDispatcher:29–41`, `NavigationHost:460–562`)
have no global credential Save/Add command. Their generator command navigates;
it is not `CredentialEvent.OnGeneratePasswordClick`. The host preview key path
handles Back/Escape, not Save (`NavigationHost:229–236,285–286`).

The modal has ordinary TextFields and optional `SecureTextField`, bounded local
`onValueChange`s and no app-defined keyboard/a11y alternate Add dispatch
(`CustomFieldsEditor:359–440`). `SecureTextField:42–55,78–105` defaults to
`KeyboardActions.Default` with no Generate callback; the modal does not inherit
the main password field's explicit Save-on-Done. Framework button activation by
Enter/Space or accessibility would still reach the guarded button callback if
delivered to that button. This does not certify focus routing, queued IME,
assistive technology, RTL rendering or device input behavior.

### Lock/Back are not an invented rejection proof

Entry owners register for central cleanup (`AppModule:229–233`,
`VaultUiSecurityCoordinator:60–81`). `NavigationHost:367–395,413–424` clears them
before secure root replacement. `CredentialViewModel.clearForLock:314–336`
cancels work, clears sensitive accepted values and resets to `CredentialState()`.
The reset has `isBusy=false`, count0, and no loaded/new credential. Thus `canSave`
and `canAddCustomField` become false, **but router Add does not separately test
loaded/new/canSave**. An artificially delivered old Add after reset is not the
claimed full/busy rejection; in this implementation it can append to the reset
noneditable state. This review does not security-clear that lifecycle window.

Navigation/effect token checks (`VaultRouteAdapters:192–214,225–240`) guard
navigation/effects, not the direct `viewModel::onEvent` passed to the form. The
Add dialog's local fields/show flag are not in `CredentialState` or its
`hasUnsavedChanges`/Back policy. Do not treat those navigation guards as a proven
modal-input lifetime or old-event guard. Lock, Back/dismissal and owner disposal
remain separately qualified; retaining plaintext universally across lock/disposal
would not follow from PVA031's original count contract.

## 4. Counterexamples tested against the source argument

**Bare duplicate confirm does not prove a populated rejection.** Start at49 with
name A/value B and composed admission true. The first callback appends the50th
field, sets show=false and clears name/value synchronously. A second sequential
callback with no intervening input re-reads an empty remembered name and does not
dispatch Add. The originally accepted A/B tuple remains in VM state. The callback
does not capture A/B as an immutable precomposition payload. This disproves only
that specific app-level sequential trace, not all input/lifecycle schedules.

**Postacceptance late input is a different, still conditional trace.** Before
owner disposal/recomposition, `onNameChange`/`onValueChange` remain ordinary local
writes with no closed/owner-generation guard (`CustomFieldsEditor:367–369`):

1. accept A/B at49 → count50, show=false, name/value empty;
2. deliver a subsequent name/value edit C/D to those callbacks before disposal;
3. deliver the old confirm callback while its composed admission is still true;
4. it dispatches C/D; current full count rejects; Unit return still causes hiding
   and clearing of C/D.

The source permits this callback sequence **if the supported native/framework
delivery window exists**. It has not been demonstrated. It loses a distinct late
tuple, not the accepted first tuple; calling it a bare double-click would conceal
the missing premise. Whether such post-dismissal input remains inside the intended
retention contract must not be silently decided by this note.

**Busy rejection also needs an actual supported producer.** Given a populated
modal, invoke a current-VM page Save/generate/attachment action, leave composed
admission stale, then invoke confirm: router drops Add while busy and the UI would
close/clear. That is a valid conditional callback counterexample, not proof that
the user can invoke that page action while the modal owns input. A pending page
IME Done or other delayed page-action delivery is not ruled out by this source
review. Simply calling `model.onEvent` from a test would skip the disputed edge.

**Controlled fill is likewise not independent user reachability.** A direct-VM
fill can invalidate admission before old confirmation; GUI02 deliberately fills
but then settles before clicking a disabled button. Its success is useful and
unchanged; neither repeating it nor removing its settle without justifying the
producer proves a real competing editor/user route.

No evidence supplied here changes original G7 plugin-failure history/cause,
creates another confirmed family, or justifies an acknowledgement redesign.

## 5. Exact supported scope and remaining work

Root may use this report to say: **all identified application producers were
traced; ordinary sequential same-modal Add does not furnish the previously
assumed populated rejection; supported stale callback/input delivery remains
unproved.** It cannot say “no stale callback is possible,” “busy/lock guards make
Unit success safe everywhere,” “rapid validation passed,” or “PVA031 closed.”

The smallest unresolved witness remains a supported delivery route with all of:
live owned populated input; old composed admission=true; current same-VM full/busy
rejection condition; actual delivered Add callback; and observation of both
accepted fields and exact modal tuple/lifetime. Candidate edges are late input →
confirm before disposal, or delayed page action → busy while a populated modal
remains owned. Either demonstrate that edge through supported input or establish
its relevant framework guard; arbitrary private-callback/direct-VM injection is
not by itself a user-reachability proof. No test/run admission is requested here.

Keep the original rapid/keyboard/a11y/LTR–RTL requirements recorded. GUI02 already
qualifies native ASCII editing, not Enter/Space activation, IME composition,
screen-reader activation, RTL or mobile Back. Broader layout/disposal/platform and
physical-device readiness work remains unresolved in its existing destination;
any reclassification requires explicit root adoption, not deletion or an
automatic broad-platform rerun. RealRoom complements do not replace the missing
pre-frame producer/dispatch witness. General concurrent-editor/backend persistence
acknowledgement guarantees remain outside the original bounded promise.

## 6. Current byte pins (SHA256)

Paths are relative to W. Prefixes below are only table abbreviations:
P=`feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/`;
U=`feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/ui/`;
N=`shared/src/commonMain/kotlin/com/passvault/shared/navigation/`;
D=`app-desktop/src/desktopMain/kotlin/com/passvault/desktop/`.

| File | SHA256 |
| --- | --- |
| U`components/CustomFieldsEditor.kt` | `dd60f4a65b4cf951a8ba94b2b6e4cab8440d1a33942eb046f1d74ccfcf9b31db` |
| P`CredentialViewModel.kt` | `b9a9c5e6a9e9a5eee86a13a639fa32826dbfdfbd09fa81a032935a37f58f0611` |
| P`CredentialEventRouter.kt` | `b1f18f9e32beff113d0c6d26dc16fc3b9e24460ef6039a3dc144554815431f5a` |
| P`CredentialFormSupport.kt` | `b3403f38c94808df3d9f9d227353fc0505bb0b3848d20a69da63689424a2a66c` |
| P`CredentialAttachmentController.kt` | `61f3d3a8b9c17baaef3d7def87919d5e831c48c6ac160ee0f6e5cb16cd92412c` |
| P`CredentialTotpController.kt` | `42091983ff2d8bf54f567655c8f9ddd6b22a5a3b0474a985feb825b4d3af7715` |
| U`CredentialEditScreen.kt` | `22d878e85647fc3abb0522fccf3d8593aeb4671c5ba823d35501122c65edc4b4` |
| `core/designsystem/src/commonMain/kotlin/com/passvault/core/designsystem/components/SecureTextField.kt` | `5708b81cea22eba7dc6dddf6370469dcd6146e79dc14939ac21cc598d559cc5a` |
| N`adapters/VaultRouteAdapters.kt` | `4a155566bd433f2905f6e13137d1b8e7b03da06a54fd9e12b706b4f7d3061b98` |
| N`PassVaultNavigationHost.kt` | `50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006` |
| `shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt` | `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
| `core/security/src/commonMain/kotlin/com/passvault/core/security/VaultUiSecurityCoordinator.kt` | `1a6c7ac9be3cd4ca5979ab9b09eced6b323b0282d1529b1c7039175d6fbc19e4` |
| `core/navigation/src/commonMain/kotlin/com/passvault/core/navigation/AppCommandDispatcher.kt` | `a6272cd7d10831d5077f4012912a5e4aa520882fb6b8c5b4ed3626d137688dd9` |
| D`components/KeyboardShortcuts.kt` | `116059ec84f39cbf9f75210a638dc5c1c8a987903c6cf2663290c82437a826c5` |
| D`components/MenuBar.kt` | `7bc61e28d039828b4ef401067817c3894dae1b591417dc224f43134ec5b18ca0` |
| D`PassVaultDesktopWindow.kt` | `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702` |
| `feature/credential/src/commonTest/kotlin/com/passvault/feature/credential/presentation/CredentialCustomFieldCapacityTest.kt` | `b851a3fab0ea44109a6456896fb2936b99408aaeabb111c66d1bb96b1d1993bf` |
| `shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRenderingTest.kt` | `6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a` |
| B`/reviews/baseline-coverage/PVA031-EVIDENCE-GAP-CHALLENGE-01.md` | `b4ded70060148f7ed058c271559cd79b6136fa70bcbf9a305b2cf74286281f46` |
| B`/reviews/verification/LINUX-ISOLATED-BATCH03-ACTUAL-RESULT-REVIEW.json` | `f428020cf798125507c514c8b4d632dc58ab4f401494a29b0eaeb5001799df45` |
| B`/reviews/verification/LINUX-DESKTOP-GUI02-ACTUAL-RESULT-REVIEW.json` | `66f5cd60d4d245a845f2792ae97119f4384c31462267deefa3b141bcfdbe42a6` |

Only bounded source/evidence reads, source-data hashing and this exclusive report
were performed. C17 application/tests/helpers stay unchanged. No builds/tests,
project/helper execution/import, Git/T, network/CI, process/SDK/cache/held-runtime
probing, source/central-ledger edits, subagents or temporary/background work.
Wrapper stop is not applicable to these inert reads. PVU007 STOP; PVU011 NO RETRY;
PVA02949checks44PASS5FAIL/no automatic retry; G7/G8 CLOSED; all HOLD/cleanup,
native-refusal, eight PVD/device/publication/1017001 fences remain untouched.
