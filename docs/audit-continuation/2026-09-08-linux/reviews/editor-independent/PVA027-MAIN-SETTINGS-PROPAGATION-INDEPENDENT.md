# PVA-027 actual-Main Settings proposal — independent source challenge

Reviewer `/root/editor_review`, 2026-09-10; proposal author `/root/android32`.
**Accept bounded property-only feasibility, with the qualifications below. Not a complete
adoption patch, executed case, runtime pass or execution admission.** Root owns adoption.

## Exact inputs and review scope

B = `docs/audit-continuation/2026-09-08-linux`; all SHA-256 values below bind actual bytes.

- B/reviews/android32/pva027-followup/PVA027-MAIN-SETTINGS-PROPAGATION.kt.txt:
  `fe8beaecef2a6155a582383347dc3abe6edabd1498ed3eda4679df54caf3dcd9`, 5790B/96LF.
- Same directory PVA027-MAIN-SETTINGS-FOLLOWUP-ASSESSMENT.md:
  `9ba6efb54360d6fc92f0030429be28a5ae1f416f1bed171f8926f271abc1d08d`, 11463B/158LF.
- app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt:
  `32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5`, 40561B/755LF.

Both author documents were read completely. Production source pins in the assessment
were hash-guarded; semantic review was focused, not whole-file coverage. Relevant ranges:
SettingsScreen187–210; SettingsComponents88–132; AppearanceSettingsScreen123–179/346–410;
SettingsViewModel148–154/178–187/287–313; SettingsRouteAdapters37–73;
AppModule194–215; PassVaultApp139–165; both small Desktop language/publication files;
Main104–124; PassVaultDesktopWindow71–100/185–279; DesktopSystemTray48–85/195–235;
DesktopTrayStrings11–17 and exact English/Arabic resource entries. NavigationHost98–108/
127–135 was additionally read at `50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006`.
Prior independent parent-fixture review is retained, not reauthored here.

The author's ledger pin `031cbec176dc0925974851151aec534de8b361ac34220668989cdd511d5ff84b`
is historical context. A current-ledger guard found
`7cc63b424d1472fb5133001f7106baecc8d30eaefcd911a06dceec960f988794`/195404B and refused
before parsing. Root requested no ledger rereview; this note makes no new current-ledger audit claim.

## Challenge results

1. **Reachable production path.** Real App language navigation, selectable radio roles,
   localized labels, existing singleton injection and actual Main composition are source-supported.
   The proposal obtains the singleton only after the Appearance route is visible; it never
   calls its event method, setup, a native listener or an accessibility action. Real native
   input remains the inherited fixture's responsibility. Exact runtime AX names/geometry,
   scrolling and correction of the prior snapshot NPE remain unproved by these reads.
2. **Appropriate partial oracle.** Verified content publishes the language-resolved resource
   environment; the outer window's keyed effect resolves tray strings and updates the installed
   AWT handle. The fragment samples exact tooltip/menu properties, enabled action items and
   icon/popup/item/listener reference equality. The literals match resources. SYSTEM under the
   parent's en-US launch followed by EN does not alone challenge an update: AR then EN supplies
   the meaningful changed-value challenges. Direct locale/resource/tray injection is absent.
3. **Counterexamples remain.** Listener references can remain unchanged while callback
   indirection is stale/wrong. Tooltip properties can match without a displayed tooltip.
   Removing/re-adding the same icon between samples can pass the identity assertions: these
   are sampled identity checks, not continuous native registration/ownership proof.
   State/error and tray properties are read in separate EDT actions. They are ordered,
   non-atomic observations; a future adapter must retain that qualification or deliberately
   co-sample them. Async preference saves can fail later, or be superseded; null error plus
   matching labels is not persistence or save-settlement evidence. These limits do not defeat
   the explicitly partial property proposal, but prohibit broader success labels.
4. **Not directly adoptable.** Its three new receipts are absent from the parent's exact
   EVENTS map739–746. A separate opt-in fixed-case adapter and original terminal/serial-role
   ownership must be reviewed before use; do not append this to the dirty-editor case or
   quietly widen integration03. No adapter, registered case or execution is supplied here.
5. **Native follow-up still needed.** Current frame/owned-window targeting is not a locator
   or action oracle for the separate tray. The earlier endpoint helper's any-callback receipt
   cannot establish intended real Main Lock/Exit behavior; Exit can terminate the observation.
   Minimize/focus-loss locking must not be misattributed to a menu click. This is identified
   Linux software/evidence work, not a demonstrated hardware or missing-repository impossibility.
   I authored the earlier tray fixture: this review does not independently approve that own
   fixture or rereview/replay its successful endpoint evidence.

## Accounting and restrictions

PVA-027 full Settings propagation, displayed tooltip, actual Lock/Exit and other-platform/
packaged requirements are not closed. Preserve the qualified integration02 endpoint pass,
prior fake tests and the parent's unverified editable-only correction separately.
Zero new tests, runs, findings, fixes, closures or denominator changes from this review.

Only static source/retained-document reads and this sealed note; no fixture/product edits,
Git, builds, helper imports/execution, runtime/process/SDK/cache probes or background workers.
No temporary/generated runtime outputs were created. All STOP/NO-RETRY/CLOSED/HOLD/consumed
restrictions, PVU-007/PVU-011/PVA-029 and G7/G8, root's Detekt freeze, independent execution/
cleanup admission, PVD/hardware and non-publishing/build1017001 limits remain unchanged.
