# Main followups17 — shared fixture, two separately gated source candidates

Author `/root/editor`, 2026-09-10. Independent combined-material challenge: `/root/native_review`.
**CANDIDATE ONLY: not adopted, compiled, executed or execution-admitted.**
B = `docs/audit-continuation/2026-09-08-linux`; paths below are relative to the canonical source tree.
This completes the previously accepted PVU-003 adoption proposal plus the separate PVA-027 property-only
adapter. It is not another Main/Room harness, application fix, current GUI03 selection change or closure.

## Exact frozen material and reversible layering

Target, read-only: `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`.
Base SHA256 `32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5`, 40,561 B / 755 LF.
The editable-only AX correction in that base is preserved, not reauthored or runtime-verified here.
Root owns the current C16 Detekt02 then corrected-original-Main/Room integration03 queue.

Inputs were read as bounded source/data, with SHA256 and no-follow stable metadata guards:

| Input under B/reviews/ | SHA256 | Bytes / LF |
|---|---|---|
| `editor/pvu003-followup/PVU003-ACTUAL-MAIN-ADOPTION.patch.txt` | `1bec90498dfee2375b74bcab23053214755a9a504fe943352c538084d04de23a` | 21,002 / 367 |
| `editor/pvu003-followup/PVU003-ACTUAL-MAIN-ADOPTION-NOTE.md` | `8a8a027f5dfeaa91ce044eceee812303a7434644e6d699957bffe03fc83bc5ff` | 10,576 / 133 |
| `native-independent/pvu003-followup/PVU003-ACTUAL-MAIN-ADOPTION-INDEPENDENT.md` | `a2eb8d102a3dd1efbfb8a6dc2de484c505e643eaa00bd10b8fe990c521426c8a` | retained independent proposal acceptance |
| `android32/pva027-followup/PVA027-MAIN-SETTINGS-PROPAGATION.kt.txt` | `fe8beaecef2a6155a582383347dc3abe6edabd1498ed3eda4679df54caf3dcd9` | 5,790 / 96 |
| `android32/pva027-followup/PVA027-MAIN-SETTINGS-FOLLOWUP-ASSESSMENT.md` | `9ba6efb54360d6fc92f0030429be28a5ae1f416f1bed171f8926f271abc1d08d` | 11,463 / 158 |
| `editor-independent/PVA027-MAIN-SETTINGS-PROPAGATION-INDEPENDENT.md` | `fa96cf398f55fd93db07bbc33aac1adc1cb3b6e8f4f432cb3f53da11b754b80a` | 5,867 / 79 |
| `baseline-coverage/C16-RESIDUAL-SOFTWARE-PRIORITIES.md` | `0a91946dbc833fd69743d6c4ce9e59404a279fb7a77e53e0dc7607117b970b56` | 10,720 / 77 |

Deliverables, all under `B/reviews/editor/main-followups17/`:

| Artifact | SHA256 | Bytes / LF |
|---|---|---|
| `CredentialMainNavHostRoomIntegrationTest.kt.candidate.txt` | `96c995d0f74d7df8196fca26770e104ad2d2d2dbb9e1462790e4f7dcca294911` | 66,206 / 1,208 |
| `MAIN-FOLLOWUPS17.patch.txt` | `3ffc0176b16f7168ed1c330f13b41fd4dd06bc8a9b8d877b5b59f25ca7c270d3` | 34,295 / 601 |
| `DELTA.json` | `71864ed7109f80d5c3128426b5dc4b777173cc80c9969bace4d02d2c1d69c7b1` | 5,767 / 129 |

The original PVU-003 proposal's **14 hunks** were applied exactly as text in memory first, yielding its
expected intermediate `8a666a5df0c303d50ab57aa4ef51907f7c3a06ef69f6c7679519d5e992867f6c`,
53,492 B / 976 LF. That intermediate was not written as another source/harness. The PVA-027 adapter and
PVA-only viewport locator were then added. Adjacent changes coalesce in the **13-hunk combined diff**;
this does not drop any prior hunk. The new literal inverse recovers the exact intermediate; the original
proposal inverse then recovers the exact base. Independently applicable combined forward/reverse text
also equals candidate/base. These are text equality proofs, not parser, compiler or application tests.

`DELTA.json` records byte-equal preserved blocks:
- Original credential scenario and generic native primitives: 10,855 B / 208 LF,
  `3011e24b7827b26c8b5a4c82b5464edcda1f1dc901aa97fd237ab8502fe01c7a`.
- Legacy `EVENTS` and forbidden terminal diagnostics: 858 B / 17 LF,
  `6f8ce959f02efec0a0bd7b6a56c060fbc2560b686b943e02b4b6742d8bf17666`.
- Real `roomRole`: 2,417 B / 40 LF,
  `7cad7d0259a1584a8da61439eb6a4b70563fc1314fd23334986c22b55af5d48b`.
- Accepted PVU-003 observation members: 9,662 B / 156 LF,
  `6fc7f069ae03e240b37975f7aa52287a542ed152653637fff4318f149224f201`.

## Separate cases, receipts and shared ownership

There are exactly **three source `@Test` methods**: original credential one plus two prospective followups.
No method was added to the actual module. New registration/adoption, class/property forwarding and all
execution remain root-owned. Opt-ins are scope guards, not execution authority; absent opt-in ignores a
case and supplies no passing evidence. Once opted in, missing synthetic-display configuration fails before
the shared display-assumption path. Child case selection is a closed enum and its exact opt-in is required
again inside the child; the parent forwards each flag from that enum, not from arbitrary inherited flags.

| Separate class | Required exact opt-in | Fixed case / main receipt |
|---|---|---|
| `Pvu003NativeChooserAdmissionIntegrationTest` | `passvault.mainnav.pvu003Chooser=true` | `PVU003_CHOOSER`: one exact observation line, then the two original terminal markers |
| `Pva027MainSettingsTrayPropagationIntegrationTest` | `passvault.mainnav.pva027SettingsTray=true` | `PVA027_SETTINGS_TRAY`: the exact five lines below |

PVU-003 observation shape remains
`PVU003_CHOOSER_HOME=[01];AFTER_CANCEL=[01];FRAME_DELIVERY=[0-9a-f]`.
Its shared seed/main/verify trace contract totals seven role events; any allowed observation value remains
an observation, not a forced defect/disproof outcome. Interpretation and native modal/cleanup limits are
preserved in the prior note and independent review, not silently strengthened here.

PVA-027 exact Main receipt, in order:
1. `PVA027_MAIN_TRAY_EN1`
2. `PVA027_MAIN_TRAY_AR`
3. `PVA027_MAIN_TRAY_EN2`
4. `DRIVER_ASSERTIONS_COMPLETE`
5. `QUIT_KEY_CALLS_COMPLETE`

Its seed/main/verify contract totals nine role events. These are receipt events, **not nine test cases**.
The original credential trace accepts none of the new scenario events. Every case shares the same one
serial synthetic Room seed -> actual Main -> real Room reopen/tuple verification owner, same bound trace
writer, error diagnostics, private home/bus/environment guards and Main terminal protocol. No second
fixture, parallel child, graph bootstrap, application cleanup owner or parent-process AWT/Room use is added.
Each separately selected case still needs its own fresh disjoint runtime/evidence roots. There is no helper,
Gradle/test-filter or current GUI03 adapter change, and no use of occupied integration02 runtime roots.

## Reachable PVA-027 production chain

Candidate lines 518-606 retain the fragment's actual-Main property scenario, with concrete route/viewport
preconditions added rather than injecting a ViewModel event. Native unlock precedes the Settings tab;
selected Settings precedes the native App language row click. A real English radio is found and made
safely visible before reading the Settings singleton already consumed by the real Appearance route.
No fixture call sets Locale, invokes `onEvent`, publishes a resource environment, calls tray setup,
installs/replaces listeners or executes any native tray callback/accessibility action.

Focused source reads support the chain; whole-file hashes below are **identities, not whole-file coverage**:

| Source | Relevant proof / SHA256 |
|---|---|
| `feature/settings/src/commonMain/kotlin/com/passvault/feature/settings/ui/SettingsScreen.kt` | Non-lazy scroll Column; App language dispatches OnAppearanceClick, 192-209. `ab34bfa20fcb176701dc3ff6b2414191acf759b5aa912f84d8bd63825f0bbd74` |
| Same `ui/AppearanceSettingsScreen.kt` | Scroll Column, 99-107; language panel, 123-177; real selectable RadioButton, 354-362. `2b381898ad707dbb3f1637c0e588898c4f8c76415f194fee2e46aba395a841d1` |
| `core/designsystem/src/commonMain/kotlin/com/passvault/core/designsystem/platform/SystemBarLayout.kt` | Fully read 2,390 B; scaffoldVerticalScroll wraps real verticalScroll, 27-37. `01333a63dd376c74f4302ca9c8d5299f9abb88111a7ae4ed535b557cf01a5b98` |
| `feature/settings/src/commonMain/kotlin/com/passvault/feature/settings/presentation/SettingsViewModel.kt` | Event 152-153; state change before asynchronous save/error, 287-313. `a19baf0425234acdae2c2d923d74dbabf1e2b6a4822d923d80b9cccabfd6d723` |
| `shared/src/commonMain/kotlin/com/passvault/shared/navigation/adapters/SettingsRouteAdapters.kt` | Real Settings/Appearance entries and events, 37-73. `6339edcfe9a09916a5485894e287c40697e85909158c0c348e51b486b8073c06` |
| `shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt` | Actual Settings singleton, 200. `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
| `shared/src/commonMain/kotlin/com/passvault/shared/PassVaultApp.kt` | Verified app observes Settings, language provider and publication, 140-150. `75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737` |
| `shared/src/desktopMain/kotlin/com/passvault/shared/platform/AppLanguageProvider.desktop.kt` | Real production locale/RTL provider, not a fixture setter. `6d5be09814302ea0bb25cd1b4142ed883b930d892dff9166dda2b495c4212d59` |
| Same platform `DesktopAppResourceEnvironmentPublication.kt` | Actual owner/environment publication, 27-46. `3d56735ae5639254d9df13c6c6411fbeeed2c1c0b6c8bcab9a865f4d790d68da` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/PassVaultDesktopWindow.kt` | Remembered real tray 71-100; resource-keyed effect 224-235; minimize/focus lock guards 238-246 unchanged. `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702` |
| Same desktop `tray/DesktopSystemTray.kt` | setup updates installed handle 48-85; real AWT construction/update 195-235. `d1023a5bc9573e97eaa3089cf433107f0e009f22e67e01c4fd4f1e77e0e28d58` |

Exact English/Arabic resource entries were read in
`core/designsystem/src/commonMain/composeResources/values/strings.xml`
(`5605f102cca37a9fddf7fe50ecd598c5460168fd9eb6149d4d7a8318f644b118`) and
`values-ar/strings.xml` (`66d4273670b44b6b3fd21000ce28d8fbea8b2a9b6f074de0f642051099762a30`).
Independent expected literals challenge all four native fields:
- English: `PassVault Password Manager`, `Show PassVault`, `Lock Vault`, `Exit`.
- Arabic: `مدير كلمات المرور PassVault`, `إظهار PassVault`, `قفل الخزنة`, `خروج`.

Fresh SYSTEM under the inherited en-US launch is required. Native radio labels are precisely `English`,
`Arabic`, then Arabic `الإنجليزية`; choices are ENGLISH -> ARABIC -> ENGLISH. Initial SYSTEM/English alone
would not challenge translation; the Arabic and English-return stages do. The fixture leaves the disposable
synthetic preference ENGLISH, not restored SYSTEM; no extra reset callback/receipt or real settings access.

The observed process-local SystemTray must expose exactly one icon, its five-entry popup and the three
real action items at 0/2/4, with nonempty listeners. Subsequent samples require the same icon/popup/items
and exact listener reference lists, enabled items, original live active Main frame and exact labels/tooltip
**fields**. Expected strings are never fed into a production localization endpoint.

## PVA-only bounded native viewport locator

Candidate lines 608-711 supply the missing concrete Settings/Arabic geometry adapter while leaving the
original generic native primitives untouched. `pva027Locate` runs on EDT, selects from one AX snapshot,
and walks that target's parent/index chain **within that same list**. Strictly decreasing ancestor indices
and depth <=48 reject malformed ancestry. The selected actionable/enabled target and ancestors must belong
to the original active Main frame. Exactly one observed SCROLL_PANE ancestor of this target is required;
there is no any-scroll-node, client-only, missing-target or direct-event fallback.

The ancestor's screen rectangle is intersected with the existing client/dock-excluding safe area, inset 8px.
A positive viewport (width 100..1400, height 80..1000) and target no larger than it are required. Complete
AX target-rectangle containment and 100ms stable samples of target context/rectangle, scroll-ancestor
context and derived viewport precede a click. Native scrolling is at the safe viewport center, at most
35 wheel steps, with vertical direction derived from target versus viewport y. Each pointer move is
followed by a new same-snapshot locate and identity/geometry comparison, then another active-frame check
before wheel/click. A click uses the safe target center and captured failure-preserving press/release;
there is no coordinate alternative, AccessibleAction call or radio-click retry.

Horizontal/oversized targets cannot pass the full-containment oracle. Oversize fails immediately; an
x-position-only clipping case has no horizontal input fallback and may exhaust the bounded vertical-wheel
loop. Wheel spacing of 80ms is not an acknowledgement of Compose/frame/persistence progress. The existing
150s overall driver and bounded awaits still apply; this is not an unbounded locale search.

**Source layout does not prove runtime AX mapping.** Whether the actual toolkit exposes this target's
SCROLL_PANE ancestor, full unclipped target rectangles and stable English/Arabic geometry is unverified.
A missing/ambiguous role, target, safe viewport, focus or identity must fail, not cause guessed input.
The checks are sampled AX geometry, not continuous native registration or visual hit-testing proof.
No dependency/toolkit/runtime probe or imported helper was used to claim otherwise.

## Counterexamples, cleanup and adoption limits retained

- PVA state/error and tray properties are read in **separate ordered EDT actions**, not atomically.
  Transient values may differ between the reads. A later preference-save error or superseded save can
  escape the sample: matching state/fields is not persistence or async-job settlement evidence.
- Removing/re-adding the same native icon between samples can pass reference equality. Listener references
  can be unchanged while their callback indirection is stale/wrong. No continuous ownership or behavior
  inference is made. A tooltip property can match without any displayed tooltip; these checks contain no
  popup/tooltip pixels, native Show/Lock/Exit actions or icon-rendering evidence.
- Current real focus-loss/minimize security guards are not disabled. Full Main tray-input work needs its
  own intended-action oracle and bounded native geometry/evidence; copying the old endpoint's any-callback
  setup would not prove Main behavior and could terminate the process too early. That remaining Linux
  software work is not relabeled as a hardware or missing-authoritative-repository impossibility.
- PVU-003 remains only chooser/Home/cancel precursor sampling. Modal Home observations, export busy/idle,
  cancel and preserved Room tuples cannot establish the private-job/lock-cycle/deletion/deadlock hypothesis
  or universally disprove it. No DAO stall, export destination, destructive confirmation or lock injection.
- The unchanged 150s driver deadline also bounds cleanup awaits and can preclude PVU's Escape entirely.
  A 5s EDT timeout cannot stop already-started EDT/native work. Listener removal/input-release/focus or
  interruption failures remain non-pass and require outer settlement/HOLD handling, not a retry claim.
- Parent roles remain 90s seed / 200s Main / 90s verify, one 5s child-settlement attempt each, serial.
  Per-child `-Xmx512m` is a heap cap, not an RSS guarantee. Forced child containment is not a passing
  terminal close. The original Main terminal owner alone closes its graph; no competing dispose path.

Root must decide adoption only after current C16 capture and independent combined-source review. Then a
fresh source identity, exact separately selected classes/opt-ins, JDK17/classpath/private display/bus,
fresh synthetic roots, per-case trace/XML/log expectations, time/RAM bounds, coordination, settlement,
wrapper stop and allowlisted generated-output cleanup require fresh independent execution admission.
Nothing here expands GUI03 or admits a current/old helper replay. Original successful endpoint/fake/source
evidence and failures remain distinct; other-platform/package/real-hardware gaps and all PVD limits remain.

## Authoring accounting

Only this owned proposal directory's small permanent after-image, delta, manifest and note were written.
Foreground bounded no-follow source/data readers and in-memory text transformations only; no source/helper
import, AST/syntax/compile probe, build/test/Git, held-runtime/process/cache/SDK probe, real vault, clipboard,
network, temporary build output or background worker. Stable read guards exclude atime. One initial guessed
priority-note path was absent; root supplied its correct path before use. A truncated combined data-read
output was completed with narrower source reads; this was not a runtime retry. Wrapper stop is N/A for
this data-only lane; no caches/toolchains/shared outputs were deleted or processes killed.

No new executed cases, product findings/fixes, qualified closures or denominator changes. The two proposed
followups remain partial software evidence candidates, not completed remediation. All STOP/NO-RETRY/
CLOSED/HOLD/consumed scopes, PVU-007/PVU-011, PVA-029's failure/no automatic retry, G7/G8, PVD separations,
non-publishing branch authority and occupied build1017001 restrictions remain unchanged.
