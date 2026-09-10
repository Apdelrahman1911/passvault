# Main followups17: independent material-integration review

Reviewer `/root/native_review`; author `/root/editor`; 2026-09-10.
**ACCEPT THE UNADOPTED SOURCE CANDIDATE ONLY. No required correction identified.**
This is independent review of material integration and new PVA-027 geometry/glue,
not a repeated whole-fixture audit, compilation result or execution admission.
Root alone owns adoption after its current C16 checkpoint and any later validation.
The module/frozen payload and current GUI03 selection were not changed here.

## Exact identities and earlier acceptance reused

`W=/root/projects/PassVault/passvault-linux`;
`B=W/docs/audit-continuation/2026-09-08-linux`.
Candidate artifacts are under `B/reviews/editor/main-followups17/`:

| Input | SHA256 | Bytes / LF |
|---|---|---|
| `CredentialMainNavHostRoomIntegrationTest.kt.candidate.txt` | `96c995d0f74d7df8196fca26770e104ad2d2d2dbb9e1462790e4f7dcca294911` | 66206 / 1208 |
| `MAIN-FOLLOWUPS17.patch.txt` | `3ffc0176b16f7168ed1c330f13b41fd4dd06bc8a9b8d877b5b59f25ca7c270d3` | 34295 / 601 |
| `DELTA.json` | `71864ed7109f80d5c3128426b5dc4b777173cc80c9969bace4d02d2c1d69c7b1` | 5767 / 129 |
| `AUTHOR-NOTE.md` | `08adfe0bbbe34345ceef5d691e0022e84b06a2e05b0d98dbc31016e469fbcb04` | 17334 / 204 |
| Read-only module base `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt` | `32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5` | 40561 / 755 |

Reuse my `reviews/native-independent/pvu003-followup/PVU003-ACTUAL-MAIN-ADOPTION-INDEPENDENT.md`
(`a2eb8d102a3dd1efbfb8a6dc2de484c505e643eaa00bd10b8fe990c521426c8a`)
and `/root/editor_review`'s `reviews/editor-independent/PVA027-MAIN-SETTINGS-PROPAGATION-INDEPENDENT.md`
(`fa96cf398f55fd93db07bbc33aac1adc1cb3b6e8f4f432cb3f53da11b754b80a`).
Both were read/hash-bound. The prior PVA assessment `9ba6efb54360d6fc92f0030429be28a5ae1f416f1bed171f8926f271abc1d08d`
and fragment `fe8beaecef2a6155a582383347dc3abe6edabd1498ed3eda4679df54caf3dcd9`
were also read. Their qualified production-chain/partial-oracle reasoning is reused,
not upgraded into new coverage. Twelve existing chain/resource sources were separately
hash-rebound against those retained pins: Settings VM/route, AppModule/PassVaultApp,
Desktop provider/publication, Main/window/tray/strings and English/Arabic resources.
That continuity check is not a second whole-source semantic audit.

## Exact delta and preservation challenge

- Applied only the earlier inert patch `1bec90498dfee2375b74bcab23053214755a9a504fe943352c538084d04de23a`
  as exact text in memory:14 hunks reproduce `8a666a5df0c303d50ab57aa4ef51907f7c3a06ef69f6c7679519d5e992867f6c`,
  53492 B/976 LF; its reverse recovers base32c. No intermediate source file was written.
- Independently applied the combined13-hunk patch with exact context/line counts,
  without fuzz: forward equals candidate96c; reverse equals base32c. Adjacent hunk
  coalescing is not omitted PVU work. Inspected the derived candidate-versus-accepted-
  intermediate difference as inert text, rather than rereviewing unchanged PVU code.
- All four DELTA preserved-block hashes, lengths and bytes independently match:
  original credential scenario/native primitives10855 B/208 LF; legacy EVENTS and
  terminal diagnostics858 B/17 LF; real Room role2417 B/40 LF; accepted PVU observer
  members9662 B/156 LF. The observer also equals the original accepted fragment
  `76d198977e09cfd38e664cd957f5c3aa159c76488843e4b4dad36667b175cbd3`
  with enclosing indentation only. New PVA body matches DELTA's10989 B/194 LF hash.

## Case/receipt/ownership challenge

Candidate83-267,312-359,1053-1073,1167-1208 retain one serial seed/Main/verify owner.
The original method selects CREDENTIAL; each followup has its own single-method class,
exact true opt-in and enum branch. Missing opt-in ignores the case, never passes it;
opted-in missing display fails before the common assumption. Child flags derive from
that fixed enum and are checked again; invalid enum/arity cannot silently select a case.

PVA class `Pva027MainSettingsTrayPropagationIntegrationTest` requires
`passvault.mainnav.pva027SettingsTray=true`. Its main-role writer/parent contract is
exactly EN1, AR, EN2, DRIVER_ASSERTIONS_COMPLETE, QUIT_KEY_CALLS_COMPLETE, with the
full `PVA027_MAIN_TRAY_` prefixes on the first three lines. Seed/verify retain their
original two-line contracts. Original CREDENTIAL accepts no PVA/PVU scenario receipt;
only PVU main accepts its one observation regex before the two terminal markers.
Unknown/extra/missing/order-wrong events fail parent equality. Receipt failure, nonzero
child exit, inherited forbidden log or containment/settlement failure prevents the
next role or a pass. Receipt counts9 (PVA) and7 (PVU) are not test-case counts.

Native input release and actual Main's original Quit/terminal ownership are unchanged.
PVA adds read-only tray observation, not an installer/listener or competing cleanup
owner. Room seed/verify close attempts and final durable tuple assertions stay intact.
Each eventual selected case needs fresh disjoint roots; no current GUI03 filter,
helper, classpath admission, XML mapping or execution scope was expanded.

## PVA native viewport/English-Arabic challenge

Inspected new candidate518-711 and existing surrounding native/AX/terminal guards.
For material layout support, focused reads/hash bindings were:
- SettingsScreen `ab34bfa20fcb176701dc3ff6b2414191acf759b5aa912f84d8bd63825f0bbd74`;
  SettingsComponents `60435f4084c9479d1c2c3bc02fbf5f83f63b76faeb64c1dea2a95b3d41fb5677`;
  AppearanceSettingsScreen `2b381898ad707dbb3f1637c0e588898c4f8c76415f194fee2e46aba395a841d1`
  under `feature/settings/src/commonMain/kotlin/com/passvault/feature/settings/ui/`.
- `core/designsystem/src/commonMain/kotlin/com/passvault/core/designsystem/platform/SystemBarLayout.kt`
  `01333a63dd376c74f4302ca9c8d5299f9abb88111a7ae4ed535b557cf01a5b98`,68 lines;
  lines27-37 wrap actual verticalScroll. Non-lazy screen Columns, padded clickable
  Settings rows and nested selectable RadioButton language rows support the intended
  route; they do not prove toolkit AX-role/rectangle exposure.

The actual native unlock/selected Settings tab precede App language; a safely visible
English radio precedes read-only singleton lookup. No VM event, resource publisher,
locale setter, tray setup, AccessibleAction or fake-stall fallback was introduced.
The exact native labels English -> Arabic -> الإنجليزية accompany required states
ENGLISH -> ARABIC -> ENGLISH and independent four-field EN/AR/EN expected strings.
Fresh SYSTEM is required first; Arabic/English-return challenge real value changes.

Each PVA locate selects a target and walks its parents from the SAME bounded snapshot;
stale index/parent values are not paired with a different node list. It requires the
active original frame, actionable/enabled target, decreasing ancestor indices/depth48,
and exactly one target SCROLL_PANE ancestor. The ancestor rectangle intersects client/
dock-safe bounds and is inset8px, with positive bounded viewport and target sizes.
Complete containment plus100ms stable target/context/ancestor/viewport samples precede
clicking. Fresh whole binding and active-frame checks follow pointer movement before
native wheel/press. At most35 vertical wheel steps are attempted; no alternate point,
horizontal/direct-action fallback or radio-click retry was added. Source-only review
found no introduced stale-snapshot parent walk or English-only coordinate shortcut.

Missing/ambiguous AX ancestry, clipped/oversized target, focus/identity instability or
absent safe geometry cannot yield a passing case; x-only clipping may instead exhaust
the vertical bound. **Actual SCROLL_PANE mapping, full unclipped English/Arabic target
rectangles and successful native hit delivery remain runtime-unverified.** Sampled
AX rectangles are not visual hit-test proof; this source acceptance does not resolve
those prerequisites or authorize a run to discover them.

## Counterexamples, resource/cleanup and accounting

PVA observes separate ordered state/error and tray-property EDT samples, not atomic
propagation or preference-save settlement. Same icon/popup/item/listener references
can survive a remove/re-add between samples or stale callback indirection. Matching
tooltip fields do not prove displayed tooltips; there are no native tray callbacks,
Show/Lock/Exit, icon pixels, continuous-registration, packaging or other-platform
results. Focus/minimize protections remain intact. Those outstanding Linux software
checks are not mislabeled as hardware/repository impossibilities.

PVU's unchanged chooser-visible Home/cancel observation still cannot prove/disprove
the private-job/A-V cycle, deletion, deadlock or data-loss hypothesis. Its150s driver
deadline can preclude any Escape;5s EDT waits cannot settle already-started work.
Parent90/200/90s role caps and one5s settlement attempt each remain;512m is heap, not
RSS. Forced child containment is not a passing close. No stronger cleanup/retry
promise or new owner follows; root needs fresh reviewed source/class/flag, coordination,
resource, display/bus/storage, evidence and wrapper/worker/output cleanup admission.

27 exact input images were rebound before this exclusive0600 report, file/parent
fsync and readback/hash. Only bounded data/source reads, in-memory text equality and
this permanent report were performed. No module/frozen edits, project-source/helper
import or execution, AST/syntax probe, build/test/GUI/Git/network, runtime/process/
cache/SDK/T/index/store traversal or signaling. W is not assigned a HEAD/tree.
No runtime/temp/cache output or persistent worker was created; wrapper stop N/A.
Root retains sole build/resource/cleanup/publication ownership.

There are3 source Test methods (original1 plus prospective2), **zero new adopted,
compiled or executed cases, findings, fixes or qualified closures** from this lane.
All denominators, prior successes/failures/rejections/grouping, PVU-007 STOP,
PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED, Windows05 HOLD,
all consumed/HOLD/retired-store boundaries, the separately refused native task,
PVD/hardware/publication and occupied1017001 restrictions remain unchanged.
