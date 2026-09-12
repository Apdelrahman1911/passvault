# C20 Desktop GUI04 — four requirements, no new scenario patch

Author `/root/desktop_integration_author`; paired reviewer
`/root/desktop_main_review`. **INCOMPLETE / NOT ADMITTED.** This is a bounded
source/retained-evidence proposal, not runnable authority or a completion claim.
No product, test, init, runner, request, approval or central-ledger edit is proposed
or applied. No new harness, collector or test declaration is needed here.

W = `/root/projects/PassVault/passvault-linux`;
B = `W/docs/audit-continuation/2026-09-08-linux`.
The retained C19 publication receipt identifies
`3b2130fce1d15c7686f068ae994e4cb8a80714de` /
`603ed0fbe94721d75893ba3307119523189eca85`. This lane did not query Git/T or
establish current ref/source-custody facts. Root remains sole build/Git/CI/cleanup
owner. Read `00-SCOPE-DEVIATION.md`: an initial out-of-scope sibling
directory/name/metadata traversal occurred and is not characterized as “no probes.”
No follow-up inspection of any held root was performed.

## 1. Reuse the accepted packet, not consumed GUI03

Starting packet: `B/reviews/team20/android_fixture_author/desktop-integration04/proposal01/`.
Its paired `android_fixture_review/desktop-integration04/proposal01/DELTA-REVIEW.json`
is `bc3d4e978435deb2edb02bbd896ede281c05785790ad670302154346889af875`:
**accepted inert source delta only**, not promoted or instance-approved.

`02-REFERENCE-BINDINGS.json` binds nineteen explicit W source/evidence leaves.
Current Main fixture `1c7cf84fec50c6e161d2e3e282066c372a8950b0c6f2bd7c08b1fa423d769761`
and Room fixture `109a0534da3023b7cc1f951ba6c4f8b57f8e0e78f718bef765f7d05cc3223417`
match that packet's four selection cells and accepted C18 manifest
`checkout_sha256` rows. The two production bindings (Main and application loop)
match too. These four bounded comparisons are **not a new whole-C19 manifest,
source freeze, fresh fixture compilation or target verification**.

GUI03 remains consumed: preparation143, original stop0, terminal70, all four
cases unstarted, zero XML/task headers, source-after/cleanup-safe false and
original runtime/control/evidence HOLD. Static04 and focused01 are also now
consumed/HOLD; historical preparation-basis prose describing them as pending is
superseded by C19. Focused01's twelve database XML observations do not establish
Desktop test compilation, final source acceptance or successful batch cleanup.
No additional stop, retry, old-root access or ownership adoption follows.

## 2. Exact existing requirements and discriminants

These are **requirements expressed in source**, not proven executable/compiled
cases. All four still lack actual current-GUI04 outcomes. Use the unchanged
source-reviewed implementations and their earlier independent reviews.

### R1 — real Room Save/reopen; PVA007/PVA031 complement

- Class: `com.passvault.shared.credential.CredentialEditorRoomIntegrationTest`.
- Method: `native capacity draft persists through page Save and a fresh Room database reopen`.
- Task: `:shared:desktopTest`; fixture lines89–139,360–395.
- Real boundary: native Robot Add turns the real 49-field editor into50; preserve
  all49 original tuples and50 unique IDs. Native editing leaves the new row's
  name/value/secret draft open; native page Save, **without row Save**, must consume
  that draft. Detach form, clear/join old VM, real vault lock and Room
  checkpoint/close, fresh bootstrap/repositories/VM, same database file identity,
  exact50 decoded tuples, clean/no-draft state, disabled Add and masked secret.
- Counterexamples rejected by existing assertions: saving only committed row
  state, changing survivors, returning an old VM/Room object, replacing the DB
  file, ignoring page Save or silently dropping the secret flag.
- Not proven: full NavHost, same-owner queued rejection, backend acknowledgment,
  arbitrary owner-disposal retention, IME/mobile/rapid/pre-frame behavior. A
  disabled Add assertion is not evidence that a rejected callback executed.

### R2 — actual Main dirty navigation and final durable state; PVA007

- Class: `com.passvault.desktop.CredentialMainNavHostRoomIntegrationTest`.
- Method: `actual Main guards dirty Back tab and Add without changing durable Room tuples`.
- Task: `:app-desktop:desktopTest`; fixture lines83–120,745–808.
- Three serial fresh JVM roles: seed real Room; actual Main/Window/NavHost and
  native unlock/input; post-Main-exit fresh verify of original Room tuples/file
  identity. The Main role opens a native inline draft, exercises toolbar Back,
  delivered Escape and Settings-tab leave, chooses Keep editing each time and
  verifies the tuple and active Home. Add then Discard must return to detail,
  not replay forward Add. A clean Settings→Home switch must succeed.
- The clean-tab positive control rejects a false implementation that blocks all
  navigation. Exact ordered13 seed/Main/verify events and child exits0 are required;
  forced child termination, DRIVER_FAILURE or terminal cleanup diagnostics cannot
  pass. Final unchanged tuples reject durable unintended draft writes.
- Not proven: no transient writes, pre-SideEffect race, key-repeat/IME/mobile
  guards, concurrent lock, every shutdown callback or a hard terminal deadline.
  Main's actual Ctrl+Q issuance/exit is not PVU006's create→unlock interstage witness.

### R3 — real modal chooser/Home/cancel observation; PVU003 boundary only

- Class: `com.passvault.desktop.Pvu003NativeChooserAdmissionIntegrationTest`.
- Method: `actual Main samples Home while the export chooser is modal then cancels`.
- Task: `:app-desktop:desktopTest`; fixture lines212–221,383–540.
- Native unlock→Settings→Data Management→Export, policy-valid synthetic
  passphrase, native Save encrypted backup, actual owned application-modal
  FileDialog with `isExporting=true`. Require focused owned dialog, live enabled
  Home outside the chooser rectangle, unchanged input target/geometry; issue one
  native Home press/release. Observe selected Home and four passive frame-input
  bits. Issue one native Escape to that same focused chooser; require no selected
  file, idle export and no success message. Preserve actual observation values.
- Receipt is exactly `PVU003_CHOOSER_HOME=[01];AFTER_CANCEL=[01];FRAME_DELIVERY=[0-9a-f]`
  plus the two terminal events; either sampled Home result is an observation, not
  a predetermined finding. Three-role real Room/exit verification still applies.
- No destination selected, output committed, delete invoked, fake DAO stall,
  writer hold or active Compose callback used. Zero passive bits do not establish
  why input was blocked. This cannot prove/disprove the conditional A/V lock cycle:
  actual tab admission, export-job survival, loaded nonbusy detail/delete admission
  and same-lifetime real A/V acquire/wait/release ordering remain missing. The
  single driver deadline can preclude Escape; uncertainty remains failure/HOLD,
  never a retry, forced-cancel success or universal deadlock disproof.

### R4 — Settings publisher/window-to-same-tray properties; PVA027

- Class: `com.passvault.desktop.Pva027MainSettingsTrayPropagationIntegrationTest`.
- Method: `actual Main Settings updates installed tray properties through English Arabic English`.
- Task: `:app-desktop:desktopTest`; fixture lines225–234,543–736.
- Native actual Main unlock→Settings→App language, then English→Arabic→English.
  Observe the already-used real Settings VM (no injected publisher/onEvent) and
  installed tray tooltip/menu-label properties. Require identical Main frame,
  TrayIcon, PopupMenu, three enabled menu items and original listener identities.
  The exact English/Arabic labels and EN1/AR/EN2 event order are normative.
- Updating only tray properties without the chosen Settings state, or replacing
  the tray/menu on language changes, fails these assertions. The fixture uses
  the real caller chain, not a substitute publisher or manual tray-label update.
- Not proven: native tray callbacks, displayed tooltip, persistence/continuous
  ownership, icon pixels, packaged/other-platform behavior or exhaustive RTL/a11y.
  Ordered state/property reads are non-atomic. Reuse, do not rerun, the already
  passing native endpoint popup/Show-callback evidence.

## 3. Minimum future execution/result contract — root only

The accepted init selects exactly **four suites in two serial Test tasks**:
Room first, then the three app siblings; app sibling order is unspecified and
fail-fast can leave siblings unstarted. No lifecycle/tray endpoint case is selected.
The preparation task `:auditDesktopIntegration04Prepare` may schedule no Test
task; it must actually compile/find the four selected class files and retain the
resolved real Test classpath. Focused01 has not discharged that requirement.
Render uses the exact existing init/inner argv (inner lines156–167), including
offline mode, strict dependency verification, wrapper/JDK17 and one worker.
This paragraph is not an invocation or launch recipe.

Required XML for each class C is its module's
`build/test-results/desktopTest/TEST-C.xml`: one exact testcase, `classname=C`,
`name=<method above>[desktop]`, and suite name `<simple class>[desktop]`.
Every expected suite must have tests1/failures0/errors0/skipped0 and no child
failure/error/skipped element. Missing/extra XML, assumed opt-in skips and
unstarted fail-fast siblings are not passes. Retain the existing aggregate bound
of8 XML candidates and exact expected four-path set.

Retain all three Main-case mappings, each seed/main/verify `.events/.log/.exit`
(nine child roles and27 child files are not nine/27 cases). Preserve original
command/exit, source-before/after, GUI route/budget, original required stops,
owned-domain settlement and safe-new-root cleanup independently. A green Test
task or classpath alone supplies none of those guarantees. Reuse fixed
5250s work/6000s outer clock without renewal and current admission floors;
native/EDT/syscall stalls remain qualified, not hard-time promises.

## 4. Minimal C19 binding, currently deliberately UNBOUND

The old packet is explicitly **C18**, not C19: SOURCE
`B/reviews/checkpoint18/source-prepare01/SOURCE.json`, SHA
`a34aee1c7ea19fb693adf67437ec67e8b9e0ee34232936cb76127764c7b242d3`,
commit `6489252e88ad553a867d67578eff45a402e62a48`, tree
`57d338a931ab0fb4e072aabcbbfd27bead8ef08a`, members3432. Merely changing
COMMIT/TREE or relabeling that manifest as C19 must fail the exact manifest gates.

Only after root supplies an independently accepted current whole-C19 SOURCE and
its custody/index disposition, the necessary unchanged-control delta is:

1. Inner SOURCE/COMMIT/TREE/MEMBERS/FROZEN[SOURCE] at lines38,68–73 and outer
   equivalents37,42–44,56: bind the **same actual** C19 manifest/path/hash/count
   and retained C19 identity. Keep the two declared checkout-EOL qualifications;
   compare all selected fixture/production `checkout_sha256` rows. Do not derive
   member count from319 publication changes, a W scan or guessed additions.
2. Both entry ceilings currently admit at most3432 (inner1391/outer657), not3500.
   Bind a finite manifest-derived ceiling if needed. OID input is `41*N` bytes
   with no repeated-OID deduplication; both outer write guard442 and reread456
   use144KiB. Change those together **only if the actual count requires it**.
   Check `max(git_size)<=32MiB` and full batch framing
   `sum(git_size + 48 + len(str(git_size)))<=128MiB`; do not speculatively broaden
   unrelated bounds or discard manifest members to fit. Accepted C19 N/size/cap
   values are not supplied to this lane and remain UNBOUND.
3. Init semantics, four methods and production/scenario bodies need no change.
   Recompute the dependent hash chain only: accepted SOURCE/unchanged INIT →
   final INNER → final OUTER; request pins all four. Do not create a self-hash
   cycle, duplicate SOURCE collector or silently alter old GUI03/proposal bytes.
4. Keep inner `PANEL_ROUTE_ADMITTED` UNBOUND until independent GUI-specific route
   admission; keep outer DEVICE and EXPECTED_LOCK UNBOUND until fresh exact
   root-owned facts for the original coordination object and tool/parent/alias
   sets. Boolean/int/lock-schema/under-lock checks must not be weakened or supplied
   from old receipts. New GUI04 runtime/evidence absence/allocation, private X/bus/
   panel route and namespace/domain ownership require their own current admission.
5. A genuine fresh request, matching final review and final root decision are
   separate. The existing outer expects `android_fixture_review`'s exact
   `INSTANCE-ACCEPT.json`/`ACCEPT_EXACT_NEW_INSTANCE`; this paired source review is
   not that signature. Retaining that reviewer contract needs no code change;
   any root-assigned reviewer change needs its own exact literal/hash delta and
   genuine approval. No proposal or point host observation supplies an approval.

No current-C19 source/host/route/lock bindings, new source collector, root-only
probe or independently accepted launch window was provided in this lane.
`focused_validation_author` owns the shared C20 static/eight-credential/Desktop
test-compilation feasibility packet; **no GUI in that batch**. Root owns any common
source/index/host capture. This report does not duplicate either owner.

## 5. Stop preparation here while admission is blocked

C19's independently challenged outside-owned Java/javac screening supports no
causal screen repair or exemption. A genuinely coordinated quiet window is
required; a point-idle sample does not ensure exclusivity. No old namespace,
shared cache or held output may be adopted to bypass it. Do not elaborate another
runner merely to obtain a new attempt. Root can resume the minimal binding above
when genuine new facts exist.

Reuse the accepted GUI02 native form/fake-persistence successes and Linux03
editor logical/Composition passes with their original source/scope; they are
not real Room persistence. Reuse integration02's successful lifecycle-return
seam and installed native tray endpoint with their original limitations; they
are not the full Main/Settings chain. The integration02 Main driver NPE/failure
and its separately accepted editable-only observation correction remain recorded.

The accepted PVA031 supported-input result found no supported first-populated
stale-confirm rejection: bare duplicate sees the synchronously cleared name;
page-password IME Done is a real page Save producer, not modal/global Save;
lock reset is busy=false/count0, not a full/busy rejection. Same-owner late input
and old-confirm-before-disposal need real framework evidence, not direct onEvent.
No acknowledgment-API or other speculative patch is justified here.

PVU006's real terminal/onboarding gap remains separate: real observer/disposal,
create→automatic unlock, lock intent/cancellation, actual required Room I/O/close
and external process death must share one observed lifetime. Do not omit the
observer, insert an interstage pause or mock successful post-close DAO work.
The historical bare-Compose-exit question was separately fixed as PVA039 and has
a passing return seam; it is not reopened or counted as PVU006 resolution.

**Result: no supported additional source defect/correction identified within
this bounded task; no new patch, declarations, execution, closure or conclusive
PVU outcome.** Four GUI requirements and all wider input/platform/native limits
remain open. PVA029 retains49 checks/44PASS/5FAIL/no automatic retry; PVU007 STOP,
PVU011 NO RETRY, G7/G8 CLOSED, native-refusal, every HOLD, protected branches,
dependencies/versions/identities, eight PVD boundaries and occupied1017001 remain.
Only own-lane source/evidence documents were written; no build/GUI/helper import
or execution, Git/network, signalling, cleanup or new stop duty occurred.
