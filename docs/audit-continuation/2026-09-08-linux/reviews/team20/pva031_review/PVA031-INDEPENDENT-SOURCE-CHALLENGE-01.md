# PVA031 independent supported-input/rejection challenge

Reviewer: `/root/pva031_review`, 2026-09-11. **Source-scope review complete;
paired investigator report review pending. KEEP OPEN; no new defect, patch,
execution, closure or central-ledger edit.**

W = `/root/projects/PassVault/passvault-linux`; B = W's
`docs/audit-continuation/2026-09-08-linux`. Source is the frozen C17 work area
identified by the continuation/pause records; no Git/T identity command was run.
Named current bytes were hashed below. This is not fresh whole-tree identity,
continuous source immutability, device or framework event-delivery verification.

## Independent result

The original idle new/loaded-at-50 defect has meaningful accepted capacity and
native GUI evidence. Do not rerun it. The conditional source mismatch also
remains: the composed `canConfirm` Boolean can disagree with current VM state,
and `onEvent: Unit` cannot tell the modal whether an addition was accepted.
**A supported first-populated confirmation reaching current count/busy rejection
has not been established by this inspection.** Conversely, finding no such
app-owned producer is not proof that every platform's queued input delivery is
impossible.

The useful remaining question is narrow: can a supported event sequence deliver
a populated confirmation against changed current admission while the dialog is
still legitimately accepting input? Separate that from both a bare duplicate of
an accepted confirmation and new input delivered after acceptance has already
requested dialog dismissal. The latter needs its own reachability and intended
input-lifetime disposition; it is not automatically loss of the first draft.

No blanket acknowledgment redesign, competing-editor simulation, repeat of
successful checks or new broad platform campaign is justified by this note.
PVA031 status remains `IMPLEMENTED — VERIFICATION BLOCKED` pending root's
independently supported disposition of the bounded gap and retained qualifiers.

## Guards and counterexamples inspected

All locations are relative to W. Short file names below resolve through the
binding table.

| Candidate transition | Independent source observation | Consequence/limit |
|---|---|---|
| Open Add at count 50 | `CustomFieldsEditor:74–76` checks the composed count/busy admission on both click and enabled state; VM `:480–483` derives admission from `canSave && count < 50`. | Settled full-capacity behavior is covered; not a current-VM acknowledgment contract. |
| First modal confirm at count 49 | Dialog `:374–379` reads remembered name/value, synchronously invokes its parent, then clears name/value. Parent `:108–110` synchronously calls the only production `OnCustomFieldAdded` producer and requests dismissal. Router `:152–165` calls `add`; form support `:293–312` immediately checks current count then adds. | No suspend/queued add transaction appears in this call chain. Existing 50 accepted fields remain protected by the VM cap for sequential events. No concurrent-producer safety claim. |
| Bare second confirm after first accepted | The same click lambda re-reads the remembered `name` which the first call cleared. The nonblank guard is false even if the composed `canConfirm` is still true. | A bare double activation does not dispatch a populated second addition. It is not an executed rejected-backend retention witness. |
| Input arrives after accepted confirm but before disposal | `:367–369` still directly assign remembered local fields. There is no explicit accepted/closed owner guard in these input lambdas; confirm still has captured admission. | If supported delivery repopulates the name and confirms before disposal, the second event may reach the count guard. The first tuple was already accepted and dismissal requested. Reachability and whether this later input is within PVA031's retained-input contract are unproved; do not conflate it with a rejected first draft. |
| Another same-editor field producer fills the slot | App source search found one production `OnCustomFieldAdded` construction (`CustomFieldsEditor:109`). Row updates replace existing IDs; row removal decreases count. VM loads are one-shot, not a subscribed repository field stream. | GUI02's direct-VM fill is controlled interference, not evidence of another supported UI editor feeding this VM. This inventory is not universal framework callback/lifecycle proof. |
| Save starts while modal is populated | Page Save is `CredentialEditScreen:162–164`. Page password **IME Done** also emits Save at `:281–283`; saying there is no Save input producer would be wrong. Modal text fields do not inherit that page keyboard action. | `saveCredential:206–226` gates current `canSave`, commits existing-row drafts, validates and sets `isSaving=true` synchronously before launch. A settled modal does not contain a Save control; delivery of a queued underlying page IME/control callback while it owns input has not been proven absent or present. |
| Generate/delete/attachment/load makes busy true | VM load `:111–121`, delete `:275–281`, generator `:338–345`, attachment `beginOperation:258–262` all set the relevant busy flag before launching asynchronous work. Their callbacks originate outside the Add modal. | Async tails clear busy/update metadata, password or attachments; they do not independently start a new busy interval or append a custom field. Delete belongs to the separate detail entry. No ordinary modal-owned busy starter was found. |
| Global keyboard/menu input | `KeyboardShortcuts` and `AppCommand` contain no Save command. Desktop generator shortcut dispatches navigation `GENERATOR` (`PassVaultDesktopWindow:194–201`), not this VM's password generator. `PassVaultNavigationHost:460–547` routes commands through current navigation policy. | These are not hidden same-VM Save/Add writers. Native platform queue/focus behavior was not inspected or executed. |
| Load or another editor replaces this VM's state | Create/edit adapters independently obtain `koinViewModel`, register their own policy/effects, and initialize through keyed `LaunchedEffect` (`VaultRouteAdapters:101–153`). DI defines a ViewModel, not a singleton (`AppModule:203–232`); Nav3 has the ViewModel-store decorator (`PassVaultNavigationHost:328–339`). | No normal same-entry background reload or second sharing editor was found. Loading starts with `isLoading=true`; completion clears loading and installs fields, not a ready-editor spontaneous reload. |
| Lock/owner teardown | `clearForLock:314–319` cancels jobs, clears wrapped values, advances revision and resets to `CredentialState()`. Registered entry cleanup and guarded root replacement exist (`VaultUiSecurityCoordinator:53–81`, host `:367–423`); `onCleared:93–95` also clears. | Reset state has count 0, `isBusy=false`, but `canSave=false`. Router rejects edits on **isBusy**, not all `!canSave`; `add` checks only count. A hypothetical old event after reset is therefore not the alleged full/busy rejection witness. This is not a security clearance or proof that stale mutations are impossible. |
| Back/navigation guard supplied as disproof of all input | Current credential Back policy reads `viewModel.state.value` synchronously (`VaultRouteAdapters:219–250`); navigation/effect callbacks have token checks. Edit screen still receives direct `viewModel::onEvent` at `:128/:152`. | Token/policy checks protect their own boundaries; they are not a token guard on each modal input/Add event. Local Add dialog state also is not part of the VM's `hasUnsavedChanges`/Back predicate. Owner-disposal/forward-input behavior stays separately qualified, not silently covered by this count review. |

The actual rejection conditions differ from the admission predicate. In
particular, `!canSave` after lock is not interchangeable with router `isBusy`
rejection. Treating those predicates as identical would create an invalid stale
rejection argument.

## Existing evidence retained, not rerun

- Linux03 `CredentialCustomFieldCapacityTest`: 7 cases, zero failures/errors/
  skips. The busy/unloaded case (`:69–85`) tests the state predicate; it is not a
  rendered modal rejection. Real VM final-slot acceptance/rejection and page
  Save are other cases. The rejection at `:110–126` has no modal to observe.
- Linux03 `CredentialCustomFieldDraftTest`: 13 cases, zero failures/errors/
  skips. Existing-row drafts and duplicate row Save/late row edit checks are
  not Add-dialog remembered-input oracles.
- GUI02 editor XML: 3 cases, only **one** PVA031 capacity case, zero failures/
  errors/skips. `CredentialEditorRenderingTest:159–169` explicitly settles after
  direct VM injection before clicking disabled Add. Its `REJECTED_CONFIRM`
  assertion label does not mean a backend-rejected confirmation was dispatched.
- GUI02's native typing, settled disabled input retention, remove/accepted Add,
  50 unique surviving/entered tuples and page Save/fresh-VM reload are meaningful
  evidence. Production form/VM plus deep-copy synthetic repository are not
  Room/disk, full NavHost, IME, screen-reader or physical-device proof. This
  review reused the independent actual-result qualification and reread the XML
  and relevant fixture; it did not repeat prior screenshot/runtime inspection.
- The frozen source pins below match the capacity/editor pins in the earlier
  bounded gap challenge. No changed relevant byte was found to invalidate those
  successes. This does not assert fresh execution of all C17 source.

## Smallest remaining material evidence and scope decision

1. Establish a supported **same-owner** input sequence, not arbitrary direct VM
   interference or a raw lambda invoked after its owner is already closed.
   The only residual mechanisms from this read are platform delivery/order
   around queued underlying actions, or late text plus confirm before the
   accepted modal is physically disposed. Neither is demonstrated here.
2. A bounded production-dialog witness, if separately designed/admitted by
   root, must jointly observe: composed admission still true, exact nonblank
   entered tuple, current VM admission/rejection cause, whether a real event was
   dispatched, unchanged accepted tuples/count, and dialog/input lifetime.
   Disabled-button suppression must not be reported as backend rejection.
3. If the supported event model excludes the relevant sequence, preserve exact
   queue/focus/disposal evidence and adopt that *bounded* reachability result.
   If only postacceptance input remains possible, explicitly decide the
   same-owner/input-lifetime applicability before demanding retention or
   treating it as the original rejected draft. Do not infer a new family.
4. Any actual rejection/loss witness must be independently challenged before a
   minimal correction. No correction or acknowledgment API is currently
   recommended; no execution has been proposed/admitted by this review.

The original rapid/keyboard/a11y/LTR–RTL requirements remain recorded. Shared
predicate/no platform branch is useful applicability evidence, not proof of
Android/iOS/IME/assistive activation ordering. GUI02 demonstrates native ASCII
editing, not all keyboard activation. Mobile Back, physical Android/iOS input,
RTL/layout/accessibility and lifecycle qualifications must remain unresolved in
their current records or be moved only by explicit root adoption with an exact
remaining readiness destination. No silent waiver or automatic full-platform
rerun. Historical G7 setup/plugin failure and unknown cause remain, with later
successes not retroactively diagnosing it. Universal retention after owner
**disposal**, concurrent-editor coordination and external-persistence/backend
acknowledgments remain original exclusions, not added closure requirements.

## Byte bindings

Hashes are SHA-256 from bounded current W file reads. Source rows use the file
name and full relative path; no held runtime was opened.

| File | Relative path / SHA-256 |
|---|---|
| CustomFieldsEditor | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/ui/components/CustomFieldsEditor.kt` — `dd60f4a65b4cf951a8ba94b2b6e4cab8440d1a33942eb046f1d74ccfcf9b31db` |
| CredentialViewModel | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialViewModel.kt` — `b9a9c5e6a9e9a5eee86a13a639fa32826dbfdfbd09fa81a032935a37f58f0611` |
| CredentialEventRouter | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialEventRouter.kt` — `b1f18f9e32beff113d0c6d26dc16fc3b9e24460ef6039a3dc144554815431f5a` |
| CredentialFormSupport | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialFormSupport.kt` — `b3403f38c94808df3d9f9d227353fc0505bb0b3848d20a69da63689424a2a66c` |
| CredentialAttachmentController | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialAttachmentController.kt` — `61f3d3a8b9c17baaef3d7def87919d5e831c48c6ac160ee0f6e5cb16cd92412c` |
| CredentialEditScreen | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/ui/CredentialEditScreen.kt` — `22d878e85647fc3abb0522fccf3d8593aeb4671c5ba823d35501122c65edc4b4` |
| SecureTextField | `core/designsystem/src/commonMain/kotlin/com/passvault/core/designsystem/components/SecureTextField.kt` — `5708b81cea22eba7dc6dddf6370469dcd6146e79dc14939ac21cc598d559cc5a` |
| VaultRouteAdapters | `shared/src/commonMain/kotlin/com/passvault/shared/navigation/adapters/VaultRouteAdapters.kt` — `4a155566bd433f2905f6e13137d1b8e7b03da06a54fd9e12b706b4f7d3061b98` |
| PassVaultNavigationHost | `shared/src/commonMain/kotlin/com/passvault/shared/navigation/PassVaultNavigationHost.kt` — `50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006` |
| NavigationBackCoordinator | `shared/src/commonMain/kotlin/com/passvault/shared/navigation/NavigationBackCoordinator.kt` — `7f0f546301a87c0bd758d121a118d8325b2fe366561655d1ee5c8e1b248126db` |
| AppModule | `shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt` — `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
| VaultUiSecurityCoordinator | `core/security/src/commonMain/kotlin/com/passvault/core/security/VaultUiSecurityCoordinator.kt` — `1a6c7ac9be3cd4ca5979ab9b09eced6b323b0282d1529b1c7039175d6fbc19e4` |
| AppCommandDispatcher | `core/navigation/src/commonMain/kotlin/com/passvault/core/navigation/AppCommandDispatcher.kt` — `a6272cd7d10831d5077f4012912a5e4aa520882fb6b8c5b4ed3626d137688dd9` |
| KeyboardShortcuts | `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/components/KeyboardShortcuts.kt` — `116059ec84f39cbf9f75210a638dc5c1c8a987903c6cf2663290c82437a826c5` |
| PassVaultDesktopWindow | `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/PassVaultDesktopWindow.kt` — `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702` |
| CapacityTest | `feature/credential/src/commonTest/kotlin/com/passvault/feature/credential/presentation/CredentialCustomFieldCapacityTest.kt` — `b851a3fab0ea44109a6456896fb2936b99408aaeabb111c66d1bb96b1d1993bf` |
| RenderingTest | `shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRenderingTest.kt` — `6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a` |

Evidence below is relative to B:

| Evidence | SHA-256 |
|---|---|
| `reviews/baseline-coverage/PVA031-EVIDENCE-GAP-CHALLENGE-01.md` | `b4ded70060148f7ed058c271559cd79b6136fa70bcbf9a305b2cf74286281f46` |
| `reviews/verification/LINUX-ISOLATED-BATCH03-ACTUAL-RESULT-REVIEW.json` | `f428020cf798125507c514c8b4d632dc58ab4f401494a29b0eaeb5001799df45` |
| `reviews/verification/LINUX-DESKTOP-GUI02-ACTUAL-RESULT-REVIEW.json` | `66f5cd60d4d245a845f2792ae97119f4384c31462267deefa3b141bcfdbe42a6` |
| `runs/linux-isolated-batch03/xml/ordinary-TEST-com.passvault.feature.credential.presentation.CredentialCustomFieldCapacityTest.xml` | `a4823377e8012d29de7fba266188f0ba10cdd14e996caa5eb6bd7e4951f8d2a1` |
| `runs/linux-isolated-batch03/xml/ordinary-TEST-com.passvault.feature.credential.presentation.CredentialCustomFieldDraftTest.xml` | `16520c20e9f4465b635e6d22d9fb2b5841c1eb002c7ae16288163f19a6e1cb40` |
| `runs/linux-desktop-gui02/xml/shared--TEST-com.passvault.shared.credential.CredentialEditorRenderingTest.xml` | `c70e65a32e8178d62200c3ffaa520054e8654a967c810e34d1f11e0ebfd9d27a` |

## Method and fences

Read Team20, saved pause, required handoff/permissions/assembly/publication,
repository AGENTS and focused audit/navigation/secure-restoration/Back/iOS/
localization-verification instruction snapshots. Commands shown in them were
inert guidance, not execution permission. Relevant original/current PVA031
ledger rows and source/fixture/results were read as data; early combined output
truncation was followed by focused reads. No complete unrelated-ledger/source
review is claimed.

Only ordinary bounded source/evidence reads and this report creation occurred.
No source/test/helper edits, project-code execution/import, build, tests, CI,
Git/T, SDK/cache/held-runtime/process probe, secrets, network, cleanup, new agent
or central-ledger changes. No foreground/background tool session remains from
these reads. Wrapper stop is N/A. C17/GUI03 freeze, root-only admission and
quiescence, PVU007 STOP, PVU011 NO RETRY, PVA029 failure/no automatic retry,
G7/G8 CLOSED, old HOLDs and native refusal, eight separate PVDs, genuine-device
limits and protected publication/1017001 restrictions remain binding.
