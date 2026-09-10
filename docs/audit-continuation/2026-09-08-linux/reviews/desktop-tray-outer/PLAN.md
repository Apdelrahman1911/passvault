# TRAY01 outer scaffold — source only, not execution admission

Author: `/root/verification`, 2026-09-10. Root owns all build, Git, CI, process,
instance and cleanup operations. This lane authors only this plan and the adjacent
new `LAUNCH.py`; no old helper is imported or replayed.

## Exact candidate and preserved predecessor

`W=/root/projects/PassVault/passvault-linux`;
`B=W/docs/audit-continuation/2026-09-08-linux`.

- New outer: `B/reviews/desktop-tray-outer/LAUNCH.py`.
  SHA256 `f437d04b04a27c4bea0884c3e2fad46422aa2a61b787ccc118a169abdd11f75d`; 44613 bytes / 770 LF.
- Frozen, consumed GUI02 predecessor (read as inert text only):
  `B/reviews/desktop-gui02-outer/LAUNCH.py`,
  SHA256 `3f23deba21bfc0a1ed6218ace9809180df5f5e9a8c0f605015fb8cf65e4cff2b`,
  44874 bytes / 766 LF.
- Its accepted independent source-delta reasoning remains
  `B/reviews/verification/LINUX-DESKTOP-GUI02-OUTER-SOURCE-DELTA-REVIEW.md`,
  SHA256 `1a4784706ffeaafece85b9338d058db23e6bbdf8745a4158d753113205e1cb80`.
  Reused source reasoning is NOT new source, instance, execution or cleanup
  approval, nor permission to touch a consumed/HOLD runtime.
- Fixture read binding, NOT an execution binding:
  `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/tray/DesktopSystemTrayNativeIntegrationTest.kt`,
  SHA256 `0a70e40b519ec22e33a075467ec1961a40e8c9c091c43d53c31915e818f1baec`.
  Root separately challenged this fixture; this author does not self-approve it.
- New inner/init owner: `/root/build_config`. Those files, their exact final
  hashes, route contract and review are pending; no predecessor hash is reused
  as a pin for them.

The complete `def require` through pre-`main` generic bodies and the entire
`__main__` supervision/finalization body are text-identical to the frozen
predecessor. This preserves raw Git transport, descriptor/pidfd/original-parent
ownership, namespace supervision, bounded resource watch, cancellation and
original-runtime cleanup; no generic runner or recovery machinery was added.

## Narrow prospective scope and fields

- Finding: PVA-027, installed native Linux tray language refresh.
- R: `/root/projects/PassVault/audit-runtime-linux-desktop-tray01`.
- E: `B/runs/linux-desktop-tray01`.
- SOURCE: `B/reviews/desktop-tray/SOURCE.json`.
- Inner/init: `scripts/audit/linux_desktop_tray_01.py` and
  `scripts/audit/desktop_tray_01.init.gradle`.
- RUN/PURPOSE: `linux-desktop-tray01` / `ONE_LINUX_DESKTOP_TRAY01`.
- Request: `B/requests/LINUX-DESKTOP-TRAY-01.json`.
- Independent approval:
  `B/reviews/storage/LINUX-DESKTOP-TRAY01-INSTANCE-ACCEPT.json`;
  mandatory reviewer `/root/storage`, NOT this outer's author.
  Root independently reviews the outer delta; storage reviews the inner delta
  and any future exact instance. No approval exists by virtue of these names.
- One worker `desktop-tray`; only E/logs, E/xml and E/tray-pixels are allocated
  as evidence children. Session G is `/tmp/passvault-desktop-tray01`,
  authenticated display `:88`, 1024x768x24.
- New outer/request/approval/allocation/receipt/inner formats use
  `passvault-linux-desktop-tray01-...-v1`. The eleven-key request and seven-key
  genuine approval schemas, exact source/packet image binding and unchanged
  coordination attestation (`/root`, agents quiescent, no CI) remain.
- Proposed inner direct helpers are Xvfb, private D-Bus, xfwm4 and one panel.
  Outer permits at most four recorded direct helpers, replacing GUI02's three.
  This is a schema bound, NOT proof of correct panel/descendant supervision.
- Success requires integer `declared_rendering_cases == 1`, not boolean True,
  plus unchanged XML-mapping, complete-capture, source, original stop, private
  namespace/tmpfs and settlement gates. The explicit integer test prevents
  Python's True==1 from weakening the new one-case contract.

The sole selected case is
`com.passvault.desktop.tray.DesktopSystemTrayNativeIntegrationTest` /
`same real tray refreshes English Arabic English and accepts native popup input`.
One EN->AR->EN sequence is one case, not three tests. Proposed inner contract:
one online `:auditDesktopTray01Prepare` (no Test), original wrapper stop and
empty owned namespace, then one offline `:app-desktop:desktopTest --tests`
selection of that FQCN, with `passvault.audit.desktopTray01Mode`, one app-desktop
runtime classpath and no shared/curtain/editor/Detekt Test selection.
These command details require the final init/inner's independent source review
and exact request; this plan itself does not authorize them.

Expected compact synthetic producer outputs under E/tray-pixels are exactly
`01-native-en.png`, `02-native-ar.png`, `03-native-en.png`,
`observations.txt`: at most 1MiB per file and 16384B for the trace. The fixture
requires an initially empty evidence directory, dedicated G/home/config/cache/
data/runtime directories0700, G/Xauthority0600 and a private G/bus; root's new
inner must satisfy that contract. Only one exact app-desktop Test XML, compact
logs, classpath fingerprints and receipts are needed. No application binaries,
build archives or cache are retained as evidence. The final inner must separately
bound/adopt these outputs; the outer success flags do not replace independent
XML, actual native-input, text/shaping/highlight and pixel reconciliation.

## Explicit pending gates — do not execute this scaffold

`COMMIT`, `TREE`, `MEMBERS`, `GITDIR`, `DEVICE`, `EXPECTED_LOCK`
and every `FROZEN` source value are deliberately `None`.
No tool/instance/request hashes or original pins have been fabricated or copied.
The original early guard fails closed before request/approval reads, lock
acquisition, runtime/evidence allocation or any child launch.
Do not fill these fields merely because a previous GUI02 check passed.

Ordinary installed-data feasibility report:
`B/reviews/editor-independent/PVA027-LINUX-TRAY-FEASIBILITY.md`,
SHA256 `c15601532f4d96be50b8cf0028b6cf278df36155bbb5bc47507fb0df0f20da3b`.
It supports only these three new image-path declarations:

1. `/usr/bin/xfce4-panel`.
2. `/usr/lib/x86_64-linux-gnu/xfce4/panel/plugins/libsystray.so`.
3. `/usr/lib/x86_64-linux-gnu/xfce4/panel/wrapper-2.0`.

The existing xfconfd image declaration remains. This scaffold therefore lists
17 inner inputs / 23 outer images; each still needs actual fresh capture.
Only the inherited exact `/usr/bin/python3 -> python3.12` image-link exception
exists. Package/file presence is not launch, activation, tray-selection,
configuration or ownership evidence. The minimal one-systray panel XML, exact
panel argv, external plugin wrapper and xfconfd activation/descendant lifecycle
are **PENDING**; broad shipped panel defaults, guessed help/flags or ambient
session config are not substituted. Build_config/root own the finite source/
metadata investigation. Any additional required metadata images, readiness
result fields or body delta must be made explicit and independently reviewed
before binding or admission; this scaffold grants none.

Root must publish the selected exact workload, bind complete raw Git modes/blobs,
commit/tree/member count and a freshly reviewed source store, preserve both
checkout-EOL qualifications, capture actual original parent/store/lock/device/
namespace/tool identities, and obtain independent review of the final source
tuple. A genuine frozen new request and independent exact-instance, coordination
and cleanup approval remain a separate subsequent gate. The unselected tray
scaffold is outside the frozen C12/iOS publication payload.

## Resource, cleanup and evidence boundaries

Inherited outer ceilings remain 6000s total, 5250s work, 750s original-child
drain, one original pidfd kill/one final5s wait. They are cooperative userspace
bounds, not guarantees against blocked syscalls/kernel failure or hard parent
loss. Entry disk/RAM floors12GiB/25%; running8GiB/20%; private tmpfs64MiB/4096
inodes. Preserve JDK17, checked-in wrapper, one worker/non-daemon execution,
configure-on-demand off and strict dependency verification. Root remains the
sole build owner, serial across local and CI, and owns resource monitoring.

No cleanup authority derives from a pathname, successful prior GUI02 run, R/E
allocation receipt, or this text. Only original new R is eligible after safe
inner exit0/1, independently bound original namespace settlement, source/image/
packet rechecks, mount refusal and complete original descriptor-bound allowlisted
snapshot. Exit70, partial failure or uncertainty stays HOLD with no recovery
helper, alternate kill target or automatic retry. E, permanent source/tests/
reports, unrelated work, shared caches, SDKs/toolchains and old held/consumed
runtimes are excluded from deletion. The inherited cooperative-host,
nonexhaustive process-screen/generic-interpreter, hard-parent-loss and
blocking-syscall limitations are unchanged.

This candidate yields zero executions, XML/pixel results, new fixes or closures.
Even a passing native case would use a test-controlled language driver and
synthetic callback counters/locator image, not the real Settings/publication/
window-effect chain, product-icon rendering, visual tooltip, full authentication,
accessibility or complete Arabic-layout proof. No hardware, Apple/Windows or
packaged-runtime result follows. PVA-027's remaining scope and all separate PVD
boundaries remain.

Preserve PVU-007 STOP, PVU-011 NO-RETRY, PVA-029's failure/no automatic retry,
G7/G8 CLOSED, every old HOLD/consumed scope, native-author refusal provenance
and hardware gaps. No protected refs, release/version/identity/dependency,
signing/store/upload/publication or occupied mobile1017001 action.

Author operations were bounded inert regular-file reads, text copying/diffing,
hashing and exclusive permanent writes only; no helper execution/import/AST/
syntax check, build/test, Git/network, process/namespace/mount probe, old-root
access, cache/temp file or background worker. Descriptors close before reader/
writer exit. Independent root challenge of this exact outer is still pending.
