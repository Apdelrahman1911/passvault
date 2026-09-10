# Linux desktop tray01 inner/init — grounded source, not admitted

## Final three-case delta — supersedes tray-only workload below

2026-09-10, `/root/build_config`; **source only, zero executions/closure credit**.
Current frozen inner `scripts/audit/linux_desktop_tray_01.py`:
`994602c5df621a9f7a775921968338fca9415d895a5cb1fcb50a534edc84fb10`, 87116B/1438LF.
Current frozen init `scripts/audit/desktop_tray_01.init.gradle`:
`5b875cad3086f7b0f700ab531e02bccf50d0b3d4d401fa9f63e88d65ecb81560`, 10830B/187LF.
Readback receipt `6a39f8`. COMMIT/TREE/MEMBERS, FROZEN source/init and
PANEL_ROUTE_ADMITTED remain None; this source cannot run without root's later
publication and independently reviewed instance/coordination/cleanup admission.

Prior tray-only acceptance is preserved: inner21ac362b…3332/init5a7d86bf…82e5,
review `B/reviews/verification/LINUX-DESKTOP-TRAY01-INNER-CLI-SOURCE-DELTA-REVIEW.md`
SHA256 `44b3412fa818ff67574e4a218337c4cddbd9530def9dfae62341ccf37404c96b`.
Its full PLAN is retained below as history (previous hashbb578c4c…89f0).
No separate prebatch inner snapshot is claimed. `/root/native_review` now reviews
the final delta; `/root/verification` owns outer, independently checked by
`/root/editor_review`. No prior approval is extended to this batch automatically.

### Exact cases, startup boundaries and commands

- app-desktop retains the one tray class/method/hash documented below.
- app-desktop adds `com.passvault.desktop.DesktopApplicationLifecycleIntegrationTest`,
  method `composeExitReturnsThroughCleanupBeforeOwningJvmExit`, fixture SHA256
  `422cbfd7b85a08585b99519b94f41296ed24ea1dce133f47f4fdab2ddf5a85b9`.
- shared adds `com.passvault.shared.credential.CredentialEditorRoomIntegrationTest`,
  method `native capacity draft persists through page Save and a fresh Room database reopen`,
  fixture SHA256 `a67ba81ad875a4c611208d30b51e40caf87cf5381622f5d6ababf38d2953cff9`.

Both new source cases are independently accepted; review paths are
`reviews/native-independent/PVA039-IMPLEMENTATION-REGRESSION-INDEPENDENT.md`
(beb2d3e5…f379) and `EDITOR-ROOM-INTEGRATION-INDEPENDENT.md` (01472b3e…ab1c),
relative to B. Room's earlier de0159df tuple was superseded only by its accepted
secondary-interruption correction. Main9a67a8a5…ae93 and loop helperfc8c553e…6bfac
are additionally matched against full manifest hashes by inner PRODUCTION_SOURCE.

Two serial Test tasks, **three cases / three XML suites**, not task-count success.
One online prepare collects both actual Test classpaths/classes without any Test.
It emits exactly two bounded runtime records with selectedClasses lists. One
offline render selects only app tray+lifecycle and shared Room; shared mustRunAfter
app, max-workers1/maxParallelForks1/forkEvery0 and existing strict wrapper flags.
App Test3min/shared6min; render900s restores GUI02's two-task envelope. Prepare3600s,
stop600s each, global6000s, resource/settlement bounds and source/EOL guards remain.

WORKERS now desktop-tray and editor-room, with the same eight private children.
App tray HOME stays G/home. Room gets initially empty R/workers/editor-room/home
as HOME, user.home and passvault.editor.roomHome; its XDG/native/tmp roots stay
worker-private. English/US, syntheticDisplay, non-headless, accessibility=true
and no ambient COMPOSE_DISABLE_ACCESSIBILITY/Wayland apply before Room startup.
NO_AT_BRIDGE=1 stays best effort, not accessibility/no-activation proof. Both
workers explicitly receive private org.sqlite.tmpdir alongside JNA/tmp properties.

Add original empty0700 R/lifecycle and E/lifecycle-evidence. Lifecycle properties
select these, private DISPLAY/XAUTHORITY, and the **complete actual** app Test
classpath injected at doFirst before its worker starts; no java.class.path fallback.
Its maximum two serial child JVMs (256MiB each;30s post-start plus5s captured-child
settlement) use fresh default/production mode homes. They clear their environment;
they do not inherit the parent bus/NO_AT_BRIDGE. Fixture captured-Process cleanup
is not abrupt-parent proof: existing original PID1/private-domain descendant
capture/pidfds/TERM-KILL/wait/settlement covers survivors before evidence or R
removal. No command/resources/stop/native-owner/finish_gui signalling expansion.

### Exact retained inventory and failure behavior

XML inventory now visits each module once and maps each path to its fixture,
preventing duplicate app XML adoption. Retain exactly3 expected suite files for
success, up to8 total readable XML on failure; wrong/extra/missing/skipped cases
cannot pass. Existing three tray PNGs plus observations.txt remain unchanged.
Lifecycle retains default/production .events(4096B), .log(256KiB), .exit(16B):
six files, <=532512B total. Raw default0/two-event and production23/four-event
bytes must match in addition to exact JUnit XML. These modes remain **one case**.

After whole namespace settlement, preserve at most one original hs_err_pidN.log
per validated private mode home as default.hs-error.log / production.hs-error.log
in E/lifecycle-evidence, <=1MiB each, strict text, stable no-follow/single-link/
owner checks. Multiplicity/path/size/type/identity violations mean explicit HOLD,
not truncation, deletion or retry. Any retained crash blocks successful mapping.
No binary/archive/database upload. LIFECYCLE-RESULT.json records raw identities.

Successful inner file inventory:25 command log/INTENT pairs=50; four helper pairs=8;
16 JSON metadata; three XML; four tray captures; six lifecycle receipts: **87**.
Command labels are prepare/prepare-stop/render/render-stop; loopback-up/readback,
private-tmpfs; xauthority/display-readiness/wm-root-readiness/wm-self-readiness;
panel-channel-empty/readback; panel-property-00…09; panel-array-01/07.
The16 JSON are SOURCE-BEFORE/AFTER; PHASE-prepare/isolation/gui-start/render;
GUI-PREFLIGHT/READINESS/CLEANUP; PANEL-CONFIG; RUNTIME-CLASSPATH; INNER-PREFLIGHT/
RESULT; XML/PIXEL/LIFECYCLE-RESULT. Count analysis is source only, not observation.
Cap94 =87 +two optional crash texts +five additional XML. The112MiB cap is for
**inner Files.evidence only**, not global E. Outer adds six normal files, plus
optional CANCEL on a failure/post-inner exception: success93 total E; conservative
maximum101, not100. No change to inner count94 is needed for outer-owned CANCEL.

Result schema: declared_rendering_cases3/declared_xml_suites3/declared_test_tasks2;
preserved_xml_suites is actual retained count (success3). Safety additionally needs
lifecycle_evidence_preserved, true even for validly retained empty/partial failure.
Success additionally needs lifecycle_mapping_ok, typed crash count0, expected
lifecycle files6 and planned child modes2; existing tray success-only booleans stay.
Outer bf109d60…11dc mirrors these; identities remain unbound. Lifecycle is loop-seam
return/exit ordering, not full Main/provider completion; Room is encrypted capacity
save/reopen, not navigation/races/IME; tray visual and all hardware/PVD limitations
remain. All historical STOP/NO-RETRY/CLOSED/HOLD restrictions below remain intact.

## Preserved accepted tray-only PLAN — historical, workload superseded above

2026-09-10; author `/root/build_config`. **Currently one PVA-027 native case;
ZERO executions, native observations or closure credit.** This PLAN and
`scripts/audit/linux_desktop_tray_01.py` / `desktop_tray_01.init.gradle` are owned
here, plus the separately authorized inert source-grounding receipt/files below.
Root owns Git, source publication, original allocation/lock, all execution,
outer supervision and cleanup; `/root/verification` authors the separate outer.
All stopped/no-retry/closed scopes, old HOLD allocations and retired stores stay
untouched. No existing helper was imported, executed, repaired or replayed.

## Superseding current status: still deliberately cannot run

`COMMIT`, `TREE`, `MEMBERS`, manifest/init `FROZEN` identities and
`PANEL_ROUTE_ADMITTED` remain **None**, not borrowed checkpoint values. `main()`
rejects these before workload execution. Bare `PANEL_ARGV`, channel name and the
ten typed properties are now concrete; `panel_configuration()` implements
empty-channel creation and complete active-channel readback through xfconf-query.
No backend XML filename/path is guessed or written. The former unconditional
method rejection and unresolved argv/channel claims describe the initial draft
only, superseded by the frozen sources below.

Root authorized bounded installed metadata/source-data reads and the twelve
public GETs recorded below, not `--help`, a query, panel, bus or setup probe.
The existing private `--session` bus plus owned namespace remains the proposed
route; no new restrictive bus framework is substituted. Actual route/instance
admission, tool/source binding, coordination and cleanup are still separate and
pending. No ambient/default panel, full desktop session, migration, restart or
guessed flag is admitted here. File existence/source grounding is not runtime
proof. `/root/verification` is independently challenging the frozen inner/init.

Root intends to add one separately authored app-desktop lifecycle regression and
may batch a shared real-Room editor case in the same prepare/render cycle. Their
exact class/method/hash are not yet bound here: the current tray-only selection
and one-case count are **not the final batch**. Any narrow two-module/three-case
delta needs its own independent review; it does not alter this frozen tuple now.

## Reused source and bounded changes

Accepted GUI02 was read/copied **as text only**, with unchanged source baseline:

- `scripts/audit/linux_desktop_gui_02.py` SHA256
  `4dc98da8134a12da8e6992348d7fd4a30b269e81350866b9b34f0b9f7905e676`.
- `scripts/audit/desktop_gui_02.init.gradle` SHA256
  `cdc6ffe9c5cef7e4d8cf156fd78fdf653f88f23dd8496f7a7ff616552d372c08`.

Raw-blob/source checks, fresh PID1/mount namespace intake, original parent net
then private-net transition, 64MiB private `/tmp`, descriptor/identity I/O,
resource sampling, command logging, original pidfd signalling, per-phase wrapper
stop, namespace settlement, XML/pixel retention and qualified cleanup machinery
are reused, not a newly invented general-purpose harness. Existing GUI02 code,
fixture and execution authority are unchanged. No archived runner is imported.

The intentional deltas are fixed tray01 path/wire names; one app-desktop
class instead of curtain1/editor3; one `desktop-tray` worker; one runtime-classpath
record; one case / three PNGs plus a trace; fixture-required private session
HOME/XDG/TMP/bus layout; panel/plugin/wrapper/query INPUTS additions; helper bound4
instead of3; grounded channel setup and original native-owner pidfd monitoring;
new best-effort `NO_AT_BRIDGE=1`; trace16KiB / aggregate4MiB limits; and render
command budget300s rather than the old two-task900s. AST-only source comparison
receipt `d547d3` found `check_inputs`, `resources`, `settle`, `command`,
`signal_child`, `state`, `stop`, `helper_limits`, `start_helper`, `await_socket`
and `namespace_row` unchanged. Parsing inert source is not import/test evidence.

Current frozen source hashes (read back in receipt `94d839`):

- inner: `21ac362b8dd6d2ba64721aeceb4a6addb56ec42a00d32d219eb8366073ff3332`,
  78384B/1316LF;
- init: `5a7d86bf12fbf0937ff6e4ab805eba5258c097f5da30ccb375d4dc60969482e5`,
  9229B/163LF.

These are source-data hashes, not final manifest/instance binding or independent
acceptance. Historical initial scaffold identities were inner
`e2ce7383a697d820dde6eccd5529f7bcdc69bcbff1b173c18b15da56a60a163d`
(69107B/1176LF) and init
`6b4e22b67fb2d989d9a415e06ad5e6ec794546c393b5908863520346d422036c`
(8983B/160LF); that draft had unresolved panel argv/channel/XML and always
rejected `panel_configuration()`. It never ran and was not independently admitted.

## Current one-case wire and worker boundary (batch not final)

`W=/root/projects/PassVault/passvault-linux`,
`B=W/docs/audit-continuation/2026-09-08-linux`.

- Runtime `R=/root/projects/PassVault/audit-runtime-linux-desktop-tray01`.
- Evidence `E=B/runs/linux-desktop-tray01`; manifest
  `B/reviews/desktop-tray/SOURCE.json`.
- Private session `G=/tmp/passvault-desktop-tray01` in the NEW private tmpfs;
  mode0700 `home`, `runtime`, `cache`, `config`, `data`, `state` children.
- Outer argc/wire remains original parent PID/mnt/net namespace strings after
  the absolute inner filename (argc4); formats are
  `passvault-linux-desktop-tray01-outer-v1` and `...-inner-v1`; RUN_ID is
  `linux-desktop-tray01`.
- Request `B/requests/LINUX-DESKTOP-TRAY-01.json`; independent instance approval
  `B/reviews/storage/LINUX-DESKTOP-TRAY01-INSTANCE-ACCEPT.json`, reviewer
  `/root/storage` (not the outer/inner authors; pending, not presumed present).
- One `R/workers/desktop-tray` with GUI02's same eight empty CHILDREN allocations;
  native worker HOME and JVM user.home become `G/home`; XDG config/cache/data
  become `G/config`, `G/cache`, `G/data`; XDG_RUNTIME_DIR and TMPDIR/TMP/TEMP plus
  JVM java.io.tmpdir become `G/runtime`; XDG_STATE_HOME is `G/state`. JNA and
  SQLite temporary roots remain the private worker `jna` and `sqlite` children.
- DISPLAY `:88`, single1024x768x24 authenticated Xvfb; no ambient Wayland,
  SESSION_MANAGER, GUI/session/JVM-option inheritance. XAUTHORITY is
  `G/Xauthority`, regular0600, synthetic cookie provided only over stdin.
- Session bus address must be `unix:path=G/bus` (the fixture forbids GUI02's old
  `G/runtime/bus`). No host/system bus address is forwarded; system-bus env
  points at `G/runtime/no-system-bus`. Both GUI helpers and render Test receive
  new `NO_AT_BRIDGE=1`, best effort only: accepted GUI02 did NOT already have it.
  This is neither accessibility nor absence-of-activation proof.

Inner INPUTS preserve GUI02's SELF/INIT/SOURCE, JDK java/release, Xvfb, xauth,
xfwm4, dbus-daemon, xfconfd, mount, ip, xdpyinfo and xprop, adding:

- `/usr/bin/xfce4-panel`;
- `/usr/lib/x86_64-linux-gnu/xfce4/panel/plugins/libsystray.so`;
- `/usr/lib/x86_64-linux-gnu/xfce4/panel/wrapper-2.0`;
- `/usr/bin/xfconf-query`.

Installed XML/service metadata are source-review data, not silently added
execution INPUTS. Any final additional fixed inputs must be synchronized with
outer and independently reviewed. Their old feasibility hashes are not fresh
instance bindings. No xwininfo, desktop session launcher or guessed tray probe
is added. X/WM queries and helper liveness are NOT a tray-installation oracle.

## Concrete private-channel route and native-owner boundary

After authenticated Xvfb geometry and the original private foreground DBus/socket,
and **before WM/panel**, `panel_configuration()` runs once using GUI_ENV. Its
14 bounded20s non-Gradle commands use the unchanged `command()` logger/supervisor:

1. `xfconf-query --channel xfce4-panel --list` must produce empty bytes or exactly
   `Channel "xfce4-panel" contains no properties.\n`. Never reset/adopt a channel.
2. Ten `--property NAME --create --type TYPE --set VALUE` commands, adding
   `--force-array` to the two singleton arrays. `--create` alone is not exclusive;
   the preceding empty-channel requirement is material.
3. Complete `--list --verbose` must contain exactly ten unique matching properties.
   The CLI source represents arrays as `<<UNSUPPORTED>>`; two direct array queries
   must additionally return exactly `Value is an array with 1 items:\n\n1\n` and
   `Value is an array with 1 items:\n\n6\n`.

The fixed map is `/configver` int2; `/panels` int array[1]; panel1 position string
`p=6;x=0;y=0`, length uint100, position-locked booltrue, icon-size uint16, size
uint26, plugin-ids int array[6]; `/plugins/plugin-6` string `systray` and its
`square-icons` booltrue. No backend XML path, reset, broad packaged default,
migration helper, full session, restart flag or custom bus service framework.
`PANEL-CONFIG.json` retains the successful readback before any panel launch.
Existing WM checks then precede exactly one bare foreground `/usr/bin/xfce4-panel`.

`capture_tray_owners()` requires the original admitted private domain, four direct
helpers and verified channel. A bounded10s read-only startup observation (<=64
namespace members) excludes PID1/direct helpers and requires exactly one actual
XFCONF and one WRAPPER executable, rejecting unexpected startup descendants.
Stable birth/ns/exe/cwd, original pidfd capture and recheck precede Test. Executable
names classify already owned private-domain members, never authorize host adoption.
Original native-owner pidfds are sampled during GUI resource checks; terminal
originals/replacement after this first stable capture cannot become a passing
replacement. Exit observations are sticky validation failures, not resource faults;
a fresh pre-Test resource sample and flag gate reject an already-observed exit.
Cleanup checks unexpected early exits, keeps namespace-based signalling
authority unchanged, terminal-checks and closes monitoring pidfds once afterward.

Upstream can internally respawn a crashed external wrapper. This design does NOT
prove no pre-capture respawn/history, no native crash ever, hostile-root/no-escape
safety, or actual tray support/docking from process liveness. Source review of these
guards and the final outer delta remains independent of authorship/execution.

Independent source challenge `/root/verification` found a slow-stop counterexample
in the preceding frozen inner
`0839b7b337ea35741c545d0510d9753e1689c15d7471ad718769b90231820447`
(78131B/1313LF): if an original owner dies during render, its permanently readable
pidfd raised from `gui_resources()` again while the required wrapper `--stop` ran,
before `finish_gui()` set the shutdown flag. The next5s sample could spuriously
TERM/KILL that otherwise viable stop and manufacture failure/HOLD. This is a
source-reached counterexample, not an executed failure. Root authorized only the
current sticky-observation/pre-Test-gate correction: a mid-Test exit still makes
final validation fail, without aborting mandatory stop solely for that known exit.
Actual disk/RAM/tmpfs/output/cancellation/descriptor checks, `command()`/`stop()`
and shutdown-flag order are unchanged. Independent final-delta review is pending;
no mock or contrived native-owner death run was performed or credited.

## Prospective commands, resources and cleanup

The new init mode is `passvault.audit.desktopTray01Mode=prepare|render`.
Online preparation is `:auditDesktopTray01Prepare`: dependency-bearing Test
classes/classpath collections for **app-desktop only**, no Test scheduled.
Compiled selected class and actual resolved classpath are required; precisely
one bounded `DESKTOP_TRAY01_RUNTIME_CLASSPATH=` record is preserved. JNA/Linux
Skiko artifact presence is resolution evidence, not native-load execution.

After original wrapper stop AND empty original namespace, transition to private
net with loopback only and private tmpfs; establish the separately accepted
minimal tray session. The current, not-yet-final offline selection is solely
`:app-desktop:desktopTest --tests
com.passvault.desktop.tray.DesktopSystemTrayNativeIntegrationTest --offline`.
Task graph must contain that one Test task only. Other production dependencies
may compile; this is not execution of other tests or Detekt.

Both commands use checked-in wrapper, JDK17, strict dependency verification,
max-workers1, non-daemon/nonparallel, configure-on-demand/configuration-cache/
build-cache disabled, in-process Kotlin compiler and no JDK/SDK autodownload.
Test worker maxheap512m, maxParallelForks1, forkEvery0; English/US startup locale.
The fixture temporarily changes dedicated-worker defaults only to capture public
resource environments and restores them; source acceptance is not proof those
public factories resolve the right language at runtime.

Budgets inherited except render: online3600s, render300s, each required original
wrapper `--stop`600s INCLUDING its20s TERM/KILL tail, aggregate workload6000s,
shared namespace-settlement120s, individual setup commands20s, helper/socket
startup bounds10s; fixture soft60s / per-wait5s, Test task2min. These cooperative
budgets do not prove hard-interruption/no-escape or global idle; original outer
supervision and owned process/namespace settlement remain mandatory.

Floors remain launch12GiB/25% available RAM, running8GiB/20%; runtime logical6GiB,
200000files/230000examined entries, private tmpfs64MiB/4096inodes with8MiB/64inode
floor. Source copies/caches/output deletion belong to root's original allowlist
and stable retained descriptors AFTER required stops, waits and evidence.
Never delete permanent source/tests/reports, shared caches, SDK/toolchains,
old HOLD/closed paths, or signal unrelated/host PIDs.

The prospective direct helper set is xvfb, dbus, wm, panel (max4); shutdown is
reverse original direct order, TERM then KILL, original pidfds/waits. External
systray wrapper and Xfconf activation are NEW route-specific monitored owners;
their concrete source path still needs fresh execution admission. Cleanup captures
only positively proven original private-domain
remainders with stable birth/ns/exe/cwd and pidfd, max64. It does not authorize
blanket host name/PGID cleanup, restart, repeated panel runs or no-retry bypass.

## Exact test/evidence accounting, not task-count success

Fixture `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/tray/`
`DesktopSystemTrayNativeIntegrationTest.kt`, SHA256
`0a70e40b519ec22e33a075467ec1961a40e8c9c091c43d53c31915e818f1baec`.
Independent root review `B/reviews/desktop-tray/ROOT-SOURCE-REVIEW.md`, SHA256
`7259ac8a3d3ff28b1ac6613325da676f4246f07a6460d5e9f228c536436256c8`.

FQCN `com.passvault.desktop.tray.DesktopSystemTrayNativeIntegrationTest`; exactly
one method `same real tray refreshes English Arabic English and accepts native
popup input`. Expected suite `DesktopSystemTrayNativeIntegrationTest[desktop]`,
test name is the method plus `[desktop]`. One anticipated app-desktop XML file
maps exactly that one case with0 failures/errors/skips. Missing opt-in skip,
compile-only, wrong methods, extra XML or task counts do not pass.

Retained XML under `E/xml/`:
`app-desktop--TEST-com.passvault.desktop.tray.DesktopSystemTrayNativeIntegrationTest.xml`.
The initially EMPTY `E/tray-pixels` permits exactly:
`01-native-en.png`, `02-native-ar.png`, `03-native-en.png`, `observations.txt`.
Each PNG<=1MiB; trace<=16384B; aggregate<=4MiB. All four originals require force,
nofollow identity preservation after owned worker settlement. Partial evidence
is preserved on failure, never promoted to a native pass. No PNG decode/OCR is
performed by this helper. Independent text/shaping/clipping/highlight review,
raw JUnit XML and root process evidence are mandatory for a qualified claim.

Other compact standard outputs retain GUI02 names: logs + command/helper
INTENTs; OUTER-INTENT/INNER-PREFLIGHT; SOURCE-BEFORE/AFTER;
RUNTIME-CLASSPATH; PHASE-prepare/isolation/gui-start/render;
GUI-PREFLIGHT/READINESS/CLEANUP; XML-RESULT; PIXEL-RESULT; INNER-RESULT, plus the
new PANEL-CONFIG and bounded query-command logs/INTENTs.
`declared_rendering_cases=1` is a typed integer (outer refuses bool True);
new descriptive fields say3 language stages,3 PNGs,1 trace. No new success
boolean pretends helper readiness establishes native tray installation.
New `panel_configuration_verified_before_launch`,
`original_tray_owners_captured_before_test`, `original_tray_owners_settled` and
`original_tray_owners` records qualify success, not a claim that a safely settled
pre-GUI failure must be unclean. Outer must mirror QUERY, actual XFCONF input and
these success-only checks; propagation is pending its author and independent review.
The inner compact-evidence count limit is still80. `/root/verification` independently
counted the current one-tray success as78 inner evidence members; outer adds6,
thus84 total E members, not80. This is source accounting, not runtime observation.
Reconcile the actual final batch/XML/child-receipt budget before binding, not by
silently increasing the limit or pretending it already admits that pending batch.

This tests a real AWT production adapter with same tray/menu/item identities,
captured resource EN→AR→EN, actual right-click before Down/Enter, one current
stage callback, dismissal and owned tray/frame cleanup. Synthetic locator is
NOT product-icon rendering. Neither strings nor trace establish displayed tooltip,
Settings→publisher→window effects, inverse OS-locale startup, accessibility,
packaged Desktop image, other OS, mobile/physical security behavior or a PVD
change. Whole PVA-027 remains qualified by its unresolved surrounding boundaries.

## Fresh installed metadata data-read receipt

Source-only receipt `940da3`; each regular file bounded<=128KiB and stable
identity/size across read. No symlinks followed for the file itself.

| File | Bytes | SHA256 |
| --- | ---: | --- |
| `/etc/xdg/xfce4/panel/default.xml` |4674|`87b38ed482f67a54f13c6a1fe002a44c833ca03b2d5adffc75a74884be4e9b95`|
| `/usr/share/xfce4/panel/plugins/systray.desktop` |6991|`3d3871b3b680ddef283f961daed2ede583271d797d5564025f3d68606af703f2`|
| `/usr/share/dbus-1/services/org.xfce.Xfconf.service` |89|`8efc17b9481641c7ab3d18101675fcc9b3205bd1da33c055e2067ab215a9dbb1`|
| `/usr/share/doc/xfce4-panel/copyright` |5465|`5b4e3fc5d76feb1794709fe4ceb34ca67d9dd78b410a0c8639691e1af46fb462`|

Minimal factual snippets only, NOT a copied launch configuration:

```xml
<channel name="xfce4-panel" version="1.0">
  <property name="configver" type="int" value="2"/>
  <property name="panels" type="array">
    <value type="int" value="1"/>
    <value type="int" value="2"/>
    <!-- the real default contains two panels and many forbidden extra plugins -->
```

Default panel1 has position `p=6;x=0;y=0`, length uint100, locked true,
icon-size uint16, size uint26, plugin-ids int array. Its plugin6 is string
`systray`, square-icons true. Desktop metadata explicitly says
`X-XFCE-Module=systray`, `X-XFCE-Unique=SCREEN`, `X-XFCE-Internal=FALSE`, API2.0
and legacy systray support. Service Name `org.xfce.Xfconf`, bare Exec
`/usr/lib/x86_64-linux-gnu/xfce4/xfconf/xfconfd`.

Copyright identifies **only** `https://www.xfce.org/` as the official upstream
location. It names `panel/main.c`, `panel/panel-application.c`,
`migrate/main.c`, `migrate/migrate-default.c` and external-wrapper source members,
not an exact repository/archive URL or foreground/config contract. Root was
asked to obtain a finite public source chain; no URL/repo was invented here.

## Additional installed source-data reads, not tool execution

`/usr/share/dbus-1/session.conf`:3561B SHA256
`206f009ddcf909422f3651c687b4623a5780fb7486c6d650e5332f57c00d6be1`.
It has EXTERNAL auth and standard_session_servicedirs plus /etc session/include.d/
local overrides. That is possible activation scope, not observed unsafe behavior.
The specifically named dbus-daemon manpage is absent. Data receipt `9ab1a7` then
rejected `/usr/share/doc/xfce4-panel/changelog.Debian.gz` as nonregular before
reading it; the later changelog.gz path was not reached. No symlink target or
substitute manual was read. Root corrected the old GUI02 NO_AT_BRIDGE assumption;
the explicit new best-effort delta and limitations above preserve that correction.

Later separately authorized receipt `7ed5fc` read `/usr/bin/xfconf-query`
(35808B/0755), SHA256
`ce6c572c4d2f431981600051a212b255ed2b04a02d48266fb26c6436b9b2258f`;
`/usr/share/man/man1/xfconf-query.1.gz` is genuinely absent.
`/usr/share/doc/xfconf/changelog.Debian.gz` (992B), SHA256
`69b466033bb2798ba7674304defe2936e0adb2697a7d7e9585cf8535c6aeb88b`,
reports4.18.1-1build3 with subsequent no-change Ubuntu rebuild entries after
upstream4.18.1. Its apt-get suggestion was not executed or treated as authority.
Tool hashes here are historical read data, not fresh execution INPUT bindings.

## Completed bounded public-source grounding (not run admission)

Root authorized6 public GETs, then2 additional, then a separate4-GET xfconf
source chain. **All12 GETs are consumed; no automatic further request.** Each
was bounded30s and4MiB response; decoded aggregate16647101B stayed below32MiB.
Redirects were explicit, with no auth/cookies/proxies, Git, installs or source
execution. Archive processing occurred only in memory: complete member metadata
rejected traversal/links/special entries before fixed inert outputs; no archive
member path became an output path. Seven relevant source-text files and one
compact receipt remain, no tar/HTML/cache/temp archive or background process.

Index/chronology: `B/reviews/desktop-tray-inner/source-grounding/HTTP-RECEIPTS.jsonl`,
13556B/22LF, SHA256
`1af390f0851826ec37367f3f346150df5e84ea5a0db91cd85c2d3c84dccc4482`.

GET1–5 successful200 receipts bind the actual link chain from
`https://www.xfce.org/download` to
`https://archive.xfce.org/src/`, `src/xfce/`, `xfce4-panel/`, then `4.18/`.
The actual4.18 index explicitly links the4.18.4 tar; URLs were not invented.

GET6 at that observed source URL returned a bounded response, but data script
receipt2fa065 failed its status200/identity-encoding gate BEFORE writing the
response row. Exact status, location, encoding, size and hash are genuinely
unretained, not guessed as a redirect or download failure. No decompression,
archive iteration, source member write or application command followed. The
receipt explicitly records null unknowns and that consumed request. It remains
unknown, not retrospectively diagnosed from later requests.

After root's explicit extension, GET7 retained302/Location at
`https://xfce.mirror.wearetriple.com/src/xfce/xfce4-panel/4.18/xfce4-panel-4.18.4.tar.bz2`
(receipt `5a57da`). The then-narrow host gate refused this delegation. GET8 waited
for root's explicit authorization of that exact observed HTTPS mirror URL; it
returned200,1624451B, SHA256
`32304f82094ea3779741f968dc851032d8790eb78f3aa01676520b96cfacfb54`.
Receipt `3283f1` decoded12236800B/512 safe members and retained six inert sources.
Neither the GET6 failure nor GET7 policy hold was silently erased/retried.

GET9–12 used the separately authorized actual earlier `src/xfce/xfconf/` link,
then observed4.18/4.18.1 tar and same HTTPS mirror redirect. Receipt `b1c62e`
retained the200 tar facts:629881B, SHA256
`d9714751bbcfdc5a59340da6ef8ddfc0807221587b962d907f97dc0a8a002257`;
decoded4392960B/261 safe members. Only one of the two permitted relevant source
files was needed/retained. No backend XML implementation was downloaded/read.

All following names are relative to `source-grounding/`:

| Inert source file | Bytes | SHA256 |
| --- | ---: | --- |
| `xfce4-panel-4.18.4--panel--main.c.txt` |15381|`255726bac57af29602f152fe7260e6c4c628f27283348ad45690d5bd3a42b6f0`|
| `xfce4-panel-4.18.4--panel--panel-application.c.txt` |59148|`6156ad7043a517b79203fe56d801d4cd11a250750d37f3fc94b14596650aff58`|
| `xfce4-panel-4.18.4--panel--panel-module.c.txt` |18875|`3be31d306f13e26ff715414b0bbb97aef641d49fbb47fd76e00df422ac6b08e9`|
| `xfce4-panel-4.18.4--panel--panel-plugin-external.c.txt` |45569|`dd8b36c996ce8bf70db1639324192958e2a44b95aac113a2b488e9bcd573714b`|
| `xfce4-panel-4.18.4--panel--panel-plugin-external-wrapper.c.txt` |16241|`78d5611bde817b24271425828223b7373e9814be4c4b06206ca462e909111d10`|
| `xfce4-panel-4.18.4--migrate--main.c.txt` |4360|`12ff06f4c00c065d215085883aab0129965b0c3f748a68f3a3973efbf15bbb2f`|
| `xfconf-4.18.1--xfconf-query--main.c.txt` |17623|`24fa9e45090a5a402602e97c829839d8e5a4dc0d4a81035bbe81f47c81b9d73f`|

Grounded route facts: bare panel remains in foreground `gtk_main`; TERM/INT quit
while SIGUSR1 is a separate restart path (never sent here). Missing/old configver
launches migration, whose missing `/panels` branch loads broad defaults; thus the
verified configver2 and singleton panel/plugin arrays precede panel startup.
Application loading walks configured IDs; the module uses the external systray
API2.0 metadata; the wrapper argv identifies the actual module/id/socket/name and
inherits environment/cwd. External-wrapper respawn exists, qualified above.
The complete xfconf-query main source grounds create/set/type/array/list/readback
syntax. It calls `_xfconf_string_from_gvalue`; that implementation was not retained
or read. Runtime formatting or native success is not claimed from source alone.

## Authoring limitations / next owner

Repository AGENTS plus supplied native-interop and localization skills/checklist
were reread as source; no omitted skill helper was substituted or run. An initial
`find .. -name AGENTS.md` instruction locator enumerated adjacent runtime checkout
AGENTS paths; no files in those returned runtime paths were read or executed.
Subsequent reads stayed in assigned source paths and specifically authorized
metadata/source-data scopes. No retired Git store access/probe.

Source-copy edit receipt `f72edd` failed at the transient stdin authoring script's
unterminated string before evaluating/writing anything. Corrected in-memory text
copy `770d18` exclusively created the two new sources, fsynced and byte-read-back.
That was an authoring error, not a helper/build/test failure. Further route edits
are frozen at the current hashes for independent review. No helper/fixture import,
compile, Test, query, GUI launch, runtime cleanup or closure credit by this agent.
Only assigned permanent source/PLAN and the bounded inert source grounding remain;
no temporary archives/caches, generated application outputs or background workers.

Next: finish independent challenge of this exact tuple; bind the actual small
batch and synchronise QUERY/XFCONF inputs and success-only result guards with
outer author; independently review that final delta. Root separately supplies
publication, fresh execution/coordination/cleanup admission and any resource-bound
run. All PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry,
G7/G8 CLOSED, old HOLD/consumed scopes and nonpublishing/candidate1017001
restrictions remain unchanged. These source facts change no readiness denominator.
