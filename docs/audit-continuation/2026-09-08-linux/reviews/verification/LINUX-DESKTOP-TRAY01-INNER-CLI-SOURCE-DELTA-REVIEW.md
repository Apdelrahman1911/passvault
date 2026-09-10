# TRAY01 inner/init CLI delta — independent source review

2026-09-10; reviewer `/root/verification`, author `/root/build_config`.
**SUPPORT the exact corrected inner/init delta as UNADMITTED SOURCE ONLY.**
One concrete failure-path defect was independently challenged and corrected below.
This is not an outer review, source/instance binding, launch/cleanup admission,
executed regression, or PVA-027 closure. The reviewer authored the separate outer
and does **not** self-approve it.

## Bound source and comparison

`W=/root/projects/PassVault/passvault-linux`;
`B=W/docs/audit-continuation/2026-09-08-linux`.

| Current file | SHA256 | Bytes / LF |
| --- | --- | --- |
| `scripts/audit/linux_desktop_tray_01.py` | `21ac362b8dd6d2ba64721aeceb4a6addb56ec42a00d32d219eb8366073ff3332` | 78384 / 1316 |
| `scripts/audit/desktop_tray_01.init.gradle` | `5a7d86bf12fbf0937ff6e4ab805eba5258c097f5da30ccb375d4dc60969482e5` | 9229 / 163 |
| `B/reviews/desktop-tray-inner/PLAN.md` | `bb578c4c369ca753d08bfd7d94a9ee3594f0c8f80b8bfb759c9feb590b9b89f0` | 26971 / 429 |

Compared the complete textual delta against frozen, consumed GUI02 sources:
inner `4dc98da8134a12da8e6992348d7fd4a30b269e81350866b9b34f0b9f7905e676`;
init `cdc6ffe9c5cef7e4d8cf156fd78fdf653f88f23dd8496f7a7ff616552d372c08`.
They were read as inert data, never imported or replayed. No separate retained
bytes of the historical e2ce7383/6b4e22b6 tray scaffold were available in this lane;
its recorded hashes remain history, not a claimed byte comparison or reason to
probe retired stores.

Twelve generic methods are text-identical to GUI02: `cancelled`, `check_inputs`,
`resources`, `settle`, `command`, `signal_child`, `state`, `stop`,
`helper_limits`, `start_helper`, `await_socket`, `namespace_row`.
The `Files` and namespace-preflight bodies also match. Fourteen-section
name/size/hash data receipt `b8e9d9` has canonical-list SHA256
`3e4d0f109273ebb745f395afd4b94b19f56748a53db75ffeecda1d9e1254e6de`;
the twelve methods were rechecked after correction in `6768be`.
GUI-monitor/cleanup additions and fixed selection/environment changes were
reviewed separately, not mislabeled as unchanged generic code.

## R1: required stop wrongly aborted by an already-known native exit

The initially reviewed inner was
`0839b7b337ea35741c545d0510d9753e1689c15d7471ad718769b90231820447`
(78131B/1313LF). Its native-owner monitor raised whenever an original pidfd was
terminal. Reachable counterexample: an owner exits during render; `render`'s
finally starts the required wrapper `--stop` **before** `finish_gui` sets
`gui_shutdown_started`. A stop lasting to the next five-second resource sample
hits that same terminal pidfd, records a command error and TERM/KILL-aborts the
otherwise viable stop. Adequate resources and an ordinarily stoppable Gradle
worker do not guard this path. It can manufacture stop-failed HOLD and prevent
normal allowlisted cleanup after a validation failure.

The author independently confirmed this control flow; root authorized a narrow
correction. In the exact current source:

- Original-owner termination is a monotone `unexpected_exit` validation flag,
  like direct-helper termination; it no longer raises solely for known exit.
- A new gate immediately after `resources(True)`, before render intent/launch,
  rejects an already-observed original-owner death.
- A mid-Test death remains a final validation failure, even if another wrapper
  appears or the Test XML passes. The original pidfd/flag is never replaced.
- Required bounded stop and original cleanup can proceed. Missing descriptors,
  real disk/RAM/tmpfs/output faults, identity failures and existing cancellation
  handling remain enforced. No early shutdown flag, retry or cleanup bypass.

Receipt `6768be` reverses exactly these two text edits and reconstructs the
entire challenged0839 SHA256; no other correction delta is hidden. Final
validation still requires no original-owner unexpected exit. This fixes the
source-reached counterexample without claiming a native failure-injection run,
successful stop, or completed runtime regression.

## Grounded route, guards and counterexamples

- **Private configuration, not backend-path guessing.** After authenticated Xvfb
  and the original private D-Bus socket, before WM/panel/Test, one initial
  `xfconf-query --channel xfce4-panel --list` must be empty (or its exact empty
  message). Existing/default properties refuse the route; no reset occurs.
  Ten fixed typed creates install configver2, panel[1], plugin[6]=systray and the
  bounded panel/icon properties. `--create` alone is not exclusive; the fresh
  private session plus empty-channel gate matters.
- **Exact readback.** Complete verbose output must be ten unique matching
  properties, followed by exact singleton-array readbacks for panels/plugin IDs.
  Scalar substitutes, extras, wrong values or diagnostics fail before panel.
  All fourteen query commands use the unchanged bounded logger/supervisor,
  twenty seconds each, with no shell/interpolation or automatic retry.
- **Grounded foreground/plugin route.** Retained panel `main.c`:347–410 uses
  the session bus and foreground `gtk_main`; no restart/control argument is
  supplied. Application source:199–223,368–478 explains config-version migration
  and the panel/plugin arrays. Module:328–379 and wrapper:224–255 establish
  API2.0 external-wrapper/module/id/socket construction. External source:
  705–720,734–864 contains real child/respawn behavior; it is not assumed absent.
- **Original native owners.** After the four direct helpers, at most ten seconds
  of read-only observation requires exactly one XFCONF and one WRAPPER among
  remaining members of the proven, originally empty private PID/mount/net
  domain (at most64 members). Stable birth/namespace/exe/cwd is rechecked after
  original pidfd capture, with terminal descriptors rejected. An unexpected
  descendant, missing owner, partial capture or exited direct helper cannot
  establish Test readiness. Executable names classify already-owned namespace
  members; they do not grant host-PID adoption/signalling authority.
- **No replacement success, not an all-time history claim.** Original pidfd
  termination is sticky and checked again before shutdown. Upstream can respawn
  before the first stable capture; finite snapshots do not prove no transient
  activation or no prior crash. After capture a replacement cannot erase the
  original failure. Actual docking/menu/input belongs to the fixture, not a
  process-name or WM-readiness assertion.
- **Cleanup stays original-domain based.** Reverse direct-helper TERM/KILL,
  original waits, bounded descendant capture/signalling and whole-namespace
  settlement are retained. The two new monitor pidfds only observe original
  owners and close once; they do not add signal targets. Descriptor/settlement
  uncertainty remains HOLD. The three new success fields are not prerequisites
  for cleaning a safely settled setup failure with no complete owner capture.
  Existing source/stop/evidence/mount/outer gates still decide safe exit0/1;
  this source review grants no deletion authority.

Grounding index `B/reviews/desktop-tray-inner/source-grounding/HTTP-RECEIPTS.jsonl`
SHA256 `1af390f0851826ec37367f3f346150df5e84ea5a0db91cd85c2d3c84dccc4482`
has22 rows for12 consumed requests. All seven retained inert members
(177197B) independently match its size/hash rows. Query source SHA256
`24fa9e45090a5a402602e97c829839d8e5a4dc0d4a81035bbe81f47c81b9d73f`
grounds options/create/type/force-array, unsupported-array verbose output and
direct-array output. Its delegated scalar-string conversion implementation was
not retained: exact runtime formatting remains a fail-closed assertion, not
source-only native evidence. Download/archive hashes and installed-package facts
are recorded author observations, not reviewer re-downloads or reproducible
binary-build proof. GET6's unknown metadata, explicit subsequent extensions and
GET7 mirror-policy hold remain preserved.

## Init, accounting and mandatory remaining boundaries

One app-desktop class/method remains selected:
`DesktopSystemTrayNativeIntegrationTest` /
`same real tray refreshes English Arabic English and accepts native popup input`,
fixture SHA256
`0a70e40b519ec22e33a075467ec1961a40e8c9c091c43d53c31915e818f1baec`.
The task graph admits no Test during online preparation and only the selected
app-desktop Test task offline. Lazy classpath/class-directory dependencies do not
execute tests. Worker startup properties/environment match the fixture's private
G/home/config/cache/data/runtime, G/Xauthority and G/bus contract; JNA/SQLite
remain worker-isolated. NO_AT_BRIDGE is explicitly **new**, best effort only,
not inherited GUI02 behavior or accessibility/no-activation proof.

One exact XML case, three PNGs and one trace remain prospective. XML rejects
missing/extra/wrong/failing/skipped cases; producer hashes/names or classpath
records alone cannot pass. Pixels are adopted only after owned-worker settlement,
without treating byte preservation as visual review. The successful-path source
count is **78 inner evidence members**, within the internal80 guard, plus six
outer members = **84 total E files**. Counts are not observed outputs or tests.
The one-case/two-minute Test and300s render bounds do not admit any future
lifecycle/Room batch or its extra XML/child receipts.

Before any launch root must still:
1. Finalize and independently review any new lifecycle/Room tuple, filters,
   per-task classpath mapping, worker/evidence directories and exact aggregate
   count; this review covers tray1 only.
2. Synchronize the outer's new QUERY image and success-only
   `panel_configuration_verified_before_launch`,
   `original_tray_owners_captured_before_test`, `original_tray_owners_settled`
   guards/records, then obtain another agent's exact outer review. The old
   scaffold already includes XFCONF; QUERY is the currently missing addition
   (18 inner inputs /24 outer images after propagation, not an actual capture).
   This reviewer makes no acceptance claim for their own outer.
3. Bind publication/raw-source/EOL identities, final helper/tool images and fresh
   original instance/coordination/cleanup evidence. COMMIT/TREE/MEMBERS/FROZEN
   and PANEL_ROUTE_ADMITTED remain None; the inner fails before intake/workload.
   The genuine storage approval path does not create approval by itself.

PLAN's initially stale constants/hashes/GET6-only status was independently
challenged; its current bound version supersedes that draft while retaining
the failure history, R1, pending scope and corrected inner/total counts.
No PVA/PVU/PVD denominator changes follow. Full Settings/publication/window-effect
integration, visual tooltip/product icon, accessibility, inverse locale,
packaged/other-platform and hardware evidence remain separate.

## Reviewer operations and restrictions

Read repository AGENTS and supplied native-interop/localization skills/checklist;
no unavailable skill helper was substituted. Only bounded inert regular-file
reads, text comparisons/hashes and this exclusive permanent review write occurred.
No helper/fixture import, AST/syntax check, mock harness, build/test, query/GUI,
Git/network, process/namespace/mount probe, held-root access or cleanup execution.
Reader `04af95` hit an overbroad keyword-output guard; focused source ranges
replaced that data-view request. It was not a helper/test failure or retry.

Preserve PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry,
G7/G8 CLOSED, all old HOLD/consumed scopes, native-author refusal provenance,
separate PVD decisions, protected-ref/nonpublishing and mobile1017001 restrictions.
No temporary/cache/generated application files or background workers were created.
**Zero executed cases, runtime observations, newly qualified closures or conclusive
suspicion resolutions.**
