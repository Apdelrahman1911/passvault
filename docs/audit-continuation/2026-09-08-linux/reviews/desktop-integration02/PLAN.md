# Linux Desktop integration02 — four-case source-only delta

Author `/root/build_config`, 2026-09-10. **NOT EXECUTED OR ADMITTED.** Root alone
owns publication, builds, source transport, coordination, original allocations,
cleanup and admission. `/root/verification` owns the separate new outer;
`/root/storage` independently reviews this bounded inner/init delta.

## Reuse and exact new source

This copies consumed focused3 bodies as inert text; it never imports or executes
old/new helpers. TRAY01's three cases remain **all UNSTARTED**. Adding the accepted
actual-Main case makes a materially different future four-case selection, not a
TRAY01, Linux03 or GUI02 replay. No consumed source or held runtime is modified.

| Source | SHA256 | Bytes / LF |
|---|---|---:|
| New `scripts/audit/linux_desktop_integration_02.py` | `bb9dfafaa3906bf34c1aaaf05b52364ce8b52564323cc6602f38dbf2fec3acac` | 95212 / 1551 |
| New `scripts/audit/desktop_integration_02.init.gradle` | `3ba456dcc48d956eb2ff266ccfb31138ce3612f2b6aa5674194f09b0d563b8ef` | 11470 / 193 |
| Unchanged consumed `scripts/audit/linux_desktop_tray_01.py` | `94c747e2558afe541da37dd162c47dc2440f6041170fccb9d220126cbf1c26e9` | 87333 / 1438 |
| Unchanged consumed `scripts/audit/desktop_tray_01.init.gradle` | `5b875cad3086f7b0f700ab531e02bccf50d0b3d4d401fa9f63e88d65ecb81560` | 10830 / 187 |

Accepted focused3 source mechanics remain documented in
`../desktop-tray-inner/PLAN.md` and
`../native-independent/DESKTOP-TRAY01-FINAL-BATCH-INNER-INDEPENDENT.md`.
Historical acceptance does not admit integration02. The delta adds one fixture,
two directories, exact receipt/crash preservation and result fields, adjusts
selection/timing/counts, and replaces scope/bindings; the reviewed R1/R2 corrections
below update only the fixture hash and new private display geometry. No generic
runner or signalling/stop/panel/source-EOL/resource machinery change is introduced.

## Exact four cases and task execution contract

Two serial Test tasks, **four cases / four expected XML suites**, not four tasks:

| Task | Class (package prefix shown) | Single exact method |
|---|---|---|
| `:app-desktop:desktopTest` | `com.passvault.desktop.tray.DesktopSystemTrayNativeIntegrationTest` | `same real tray refreshes English Arabic English and accepts native popup input` |
| same | `com.passvault.desktop.DesktopApplicationLifecycleIntegrationTest` | `composeExitReturnsThroughCleanupBeforeOwningJvmExit` |
| same | `com.passvault.desktop.CredentialMainNavHostRoomIntegrationTest` | `actual Main guards dirty Back tab and Add without changing durable Room tuples` |
| `:shared:desktopTest` | `com.passvault.shared.credential.CredentialEditorRoomIntegrationTest` | `native capacity draft persists through page Save and a fresh Room database reopen` |

All four exact fixture hashes and the previously pinned Main/loop hashes remain
manifest-checked in `SELECTIONS`/`PRODUCTION_SOURCE`. The new permanent Main fixture
is `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`,
SHA256 `7a0dd50d7caa6cf3cfad729f7393a0e1802fbf65b57d3cceae7c5c64ac94961f`
(40423B/753LF), including the independently accepted R1 inheritance correction.
Historical adoption `../android32/navhost-draft/MODULE-ADOPTION.json`
(`9d434cac…870cc`) binds prior `8ffefea4…1922a9` to `7eee7bc0…0ec9d` with only two
comment changes; that record is not a claim that R1 was comment-only. The earlier
`MAIN-NAVHOST-ROOM-INDEPENDENT-SOURCE-REVIEW.md` (`c0b1e1ea…21c219e`) under
`../editor-independent/navhost-draft/` remains intact. Runner author edits no
fixture/product/dependency; the fixture author separately performed R1.

New mode key `passvault.audit.desktopIntegration02Mode` selects either
`:auditDesktopIntegration02Prepare` (lazy compilation/classpath resolution, no
Test scheduled) or the exact offline app-three then shared-one selection.
Preparation emits two `DESKTOP_INTEGRATION02_RUNTIME_CLASSPATH=` records; filtering
does not exclude other test sources from compilation. The shared task remains
`mustRunAfter` app. One worker, one fork, no parallelism/daemon/CoD/caches, JDK17,
in-process Kotlin, strict dependency verification and checked-in wrapper stay.
No Detekt is added to this raw-source/no-admitted-Git-inventory selection.

The app Test `doFirst` takes **`test.classpath.asPath` at task execution**, rejects
empty, and sets both lifecycle and Main child classpath properties before its
worker starts. No `java.class.path`, guessed classpath or Gradle-client `-D`
substitute. New properties are `passvault.mainnav.syntheticDisplay=:88`,
`passvault.mainnav.runtimeDir=R/mainnav`, `passvault.mainnav.evidenceDir=E/mainnav-evidence`,
and `passvault.mainnav.childClasspath=<that actual Test classpath>`.

## Isolation and time/resource bounds

`B=docs/audit-continuation/2026-09-08-linux` under the working source checkout;
new fixed scope is `linux-desktop-integration02`:

- `R=/root/projects/PassVault/audit-runtime-linux-desktop-integration02`.
- `E=B/runs/linux-desktop-integration02`.
- `G=/tmp/passvault-desktop-integration02` exists only in the new private tmpfs.
- Source manifest: `B/reviews/desktop-integration02/source-prepare01/SOURCE.json`.
- `COMMIT`, `TREE`, `MEMBERS`, both `FROZEN` hashes and
  `PANEL_ROUTE_ADMITTED` are **None**: entry fails closed, not execution authority.

Outer must allocate separate empty0700 `R/mainnav` and `E/mainnav-evidence` as
original pinned directories. Existing Room worker, tray/display and lifecycle
homes stay separate. Main's parent creates `R/mainnav/home`, then at most three
serial fresh role directories/JVMs: `seed`, `main`, `verify`. Each gets its own
tmp/preferences/XDG children; the shared synthetic home is only for that one case.
The main role invokes production Main, including tray/window protection/instance
lock; seed/verify use real Koin/Room. Normal absent-recovery preflight is not
recovery authority. Main child environments remain cleared but explicitly validate
and forward the four original private-session values described under R1. Existing
private X/WM/panel/network and whole owned-namespace settlement remain required;
address strings/namespace isolation alone are not no-host-bus/activation proof.

App Test ceiling **600s** covers nominal tray60 + lifecycle70 + Main395
(90/200/90s plus three5s settlements), leaving75s normal overhead. These are soft
fixture/task envelopes, not bounds on blocking native/EDT/filesystem work.
Shared stays360s. Render becomes **1100s**: 600+360+140s task/Gradle overhead;
the old900s could not encompass the new task ceilings. Prepare3600s, each wrapper
stop600s including its20s tail, inner6000s, outer work5250s/end6000s/drain750s
remain unchanged. Root accepted this limited timing delta. **Ceilings are not
additive reservations:** setup/earlier phases may consume the overall budget;
cancel, preserve the failure and settle rather than silently extend any clock.

Unchanged launch/running floors:12/8GiB free disk and25/20% RAM; live5s resource
checks, bounded runtime inventories,6GiB logical runtime cap and64MiB/4096-inode
private tmpfs. Main child `-Xmx512m` is not a total RSS/native-memory cap. Retained
trace/log limits are polling/adoption limits, not hard disk quotas. Fresh host
quietness, exact tool/store/lock/namespace admission remain external prerequisites.

## Independently challenged corrections before final source disposition

**R1 — Main child guards.** Storage's preserved before-review record is
`../storage/INTEGRATION02-INNER-INIT-BEFORE-REVIEW.json`, SHA256
`9f730eb96fe12c6d87e680884ee298d8c060e86443a9b2e4c4d922b63670c6f2`.
It preserves the initial inner `d61b7d01…d8bab7`/PLAN `a4d9a862…52d4bf2` and
challenge: clearing child env lost the private session/nonexistent system bus and
bridge settings; private PID/net and `/tmp` alone do not exclude a default
filesystem host bus outside `/tmp`. No actual host access or new product finding
was claimed. Root authorized fixture author `/root/android32`'s minimal26-line
fix; `/root/editor_review` independently accepted new `7a0dd50d…94961f` in
`../editor-independent/navhost-draft/MAIN-NAVHOST-PRIVATE-BUS-INDEPENDENT-DELTA.md`,
SHA256 `9d57dec6c8ce1165bea243a2937dc36a027c85d25e3577a3283348d50e9e404a`.

Parent and each Main role now validate original canonical `G/Xauthority`, owned
0700 G and G/runtime, `XDG_RUNTIME_DIR=G/runtime`, session address `unix:path=G/bus`,
system address `unix:path=G/runtime/no-system-bus`, `NO_AT_BRIDGE=1` and explicit
no-follow system-guard absence. All four original values are forwarded; no
fallback/default/extra-address substitution. Existing init supplies exactly them.
Endpoint ownership/selection, same-user races and native activation remain fresh
admission qualifications; this is not a universal no-host-access guarantee or a
change to the retained lifecycle case. Inner changes only its Main fixture hash.

**R2 — new display fit.** Editor/outer review found fresh Main derives initial
position(-88,0) on1024x768 before native WM behavior, conflicting with its complete
client-containment oracle; product `PassVaultDesktopWindow.kt:405-430` remains
unchanged (`794a8fdb…2e702`). Root chose **1280x1024x24**, yielding1200x800 at(40,112)
from the source defaults, not reliance on WM correction. Inner changes exactly
three geometry literals (GUI-PREFLIGHT, Xvfb argv, readiness regexp); outer changes
one expected-screen literal. No fixture or product geometry is widened.

All four source interfaces were checked: tray requires one origin0 screen,
identity transform and width640..1280/height480..1024, so proposed1600x1000 was
rejected.1280x1024 remains within its original full-capture bound; only≤700x400
popup crops are saved, with unchanged1MiB/file and4MiB aggregate evidence caps.
Main keeps client200..1400 by150..1000 and whole-display containment; Room's
800x640 window at(48,48) keeps client200..1000 by200..800 and containment; lifecycle
has no fixed-screen constraint. Bounds are source compatibility, **not observed
WM, scaling, geometry, focus, memory or PNG success**. Keep those runtime oracles,
resource floors and all four cases; fail/retain rather than expand bounds on error.

## Main receipts, failures and exact inventory delta

After **whole owned-namespace settlement**, adopt nine producer files using the
accepted stable no-follow/single-link/owner/original-descriptor/force discipline:
`seed`, `main`, `verify` each has `.events`≤8192B, `.log`≤256KiB, `.exit`≤16B;
aggregate≤811056B. Preserve empty/partial valid failure evidence, but success needs
all nine, raw exact13 ordered event lines across the three traces, all three
`0\n` exits and no fixture terminal-diagnostic markers in the logs **plus XML**.
Roles/events are not extra test cases or proof of physical input state.

Under each validated0700 same-device role directory, preserve at most one exact
`hs_err_pidN.log`,≤1MiB UTF-8 printable text, as `role.hs-error.log` in Main evidence.
No recursive crash search, database/binary/archive upload or truncation fallback.
Wrong member/type/ownership/identity, multiplicity, oversize or failed descriptor
close means HOLD before runtime removal. Any retained crash prevents Main mapping
success. `home` remains disposable encrypted synthetic runtime, never an upload.
`MAINNAV-RESULT.json` binds preserved identities, actual bytes, mapping/crashes,
one declared case, three planned roles and13 required events.

The inner result adds `mainnav_evidence_preserved`, `mainnav_mapping_ok`,
`mainnav_crash_diagnostics`, `expected_mainnav_files=9`, and
`mainnav_child_roles_planned=3`. Cleanup safety requires preservation even on a
validly retained partial/empty failure; success also requires exact mapping and
zero crashes. Existing source/stops/settlement/evidence/visual restrictions stay.

Source-derived successful inner inventory:25 command log/intent pairs=50,
four helper pairs=8,17 metadata JSON,4 XML,4 tray captures,6 lifecycle receipts,
9 Main receipts = **98 files**. No new subprocess command/helper is added by the
inner; JVM role processes are fixture children, not inner command count or cases.
Failure cap **107** =98+2 lifecycle crash texts+3 Main crash texts+4 extra XML
(still at most8 XML total). **112MiB remains an inner-owned-evidence cap only**.
With the agreed outer's six normal files, global E is104 on success; conservative
failure maximum114 adds its optional CANCEL. No count is an observed execution.

## Review/admission and preserved limitations

Storage reviews only this source delta; separate outer and instance reviews,
published full source/EOL manifest, root-owned source binding, fresh request,
tools/store/lock/original roots and quiet exclusive build slot are still required.
Neither copies nor notes authorize a retry. Source authoring/diff/hash reads only:
zero compilation, tests, helper imports/execution, Git, network, SDK, runtime
probes, background workers or temporary application/cache outputs.

Main's intended evidence concerns final durable tuples and bounded native dirty
Back/tab/Add guards, not transient writes, all navigation/race/IME or comprehensive
shutdown paths. Existing tray visual review and true hardware gaps remain separate.
No closure denominator changes. Preserve PVU-007 STOP, PVU-011 NO-RETRY, PVA-029
failure/no automatic retry, G7/G8 CLOSED, IOS01/TRAY01 consumed and all held-root/
recovery restrictions; no PVD, release identity/version/dependency or publishing
boundary changes. Root decides whether any future fresh admission is justified.
