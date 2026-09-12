# PVA027 same-case tray completion — independent source review

Reviewer: `/root/c20_apple_review`; author: `/root/c20_apple_author`.

**ACCEPTED, source-only, for root integration. Not execution admission or a
passing application result.** This review read source/evidence and wrote only
this review. No build, test, native probe, helper execution, Git operation or
cleanup ran. Root remains the sole execution/build/cleanup owner.

## Exact reviewed inputs

Author files are under `../tray_completion_author/`.

| Input | SHA256 |
| --- | --- |
| Permanent fixture before | `1c7cf84fec50c6e161d2e3e282066c372a8950b0c6f2bd7c08b1fa423d769761` |
| `CredentialMainNavHostRoomIntegrationTest.kt.proposal.txt` | `992cd7bf24fdb96f5d12c38ef105d50300bca161141230b4c177d4c85938eb6f` |
| `TRAY-COMPLETION.patch.txt` | `69a5fe6e87aa760a00ba3bf7578e166f6e791257b5fd93f78775b5c14cd377d8` |
| `PROPOSAL-CONTRACT.md` | `8ea1e8d5342cd2aca9e1353803a26e54e3be1e7d13df0f4f3c9941d526b00d4f` |

The after-image is 88480 bytes / 1620 physical LF. The permanent target is
`app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`;
its before hash remained unchanged at final review. Root must verify adoption
and bind the eventual source identity; this is not a Git-tree review.

## Independent challenges and disposition

1. **Causality, not timing.** Focus-loss allowance accumulates across focus gains;
   a recently active window does not prove that a tray action caused its lock.
   The proposal requires the exact native Lock MenuItem ActionEvent, real
   repository `Unlocked` and protection false in the Toolkit pre-observer, then
   protection true in a passive listener appended after the unchanged original
   listener on the same EDT dispatch and same event object. Original listener
   identity/order is checked. A timer/minimize lock already in effect fails;
   ordinary queued AWT timer events cannot supply the same-dispatch transition.
   The subsequent real `Locked` repository state and iconification are awaited.
   No production listener, `setLockListener`, repository lock or callback is
   invoked/replaced by this fixture. Unsupported Toolkit delivery/order fails;
   it has no fake fallback. This also discriminates a stale startup-session
   callback that would refuse the later unlocked Main action.
2. **Actual repository type.** `getSessionState()` is declared `Flow`, not
   `StateFlow`. The checked cast is source-supported by the real Koin binding
   to `VaultRepositoryImpl`, which returns `asStateFlow()`. It fails if that
   implementation contract changes rather than blocking the EDT or injecting
   a state source. Kotlin types and surrounding callback wiring were reviewed;
   compilation has not been performed by this reviewer.
3. **Terminal ordering.** PVA027 alone changes from Ctrl+Q to native Exit while
   locked. Own image/mouse/post-lock mutations are restored before Enter. The
   queued EDT input block releases Enter and forces its issuance receipt before
   the resulting ActionEvent can dispatch. The exact Exit pre-observer forces
   its receipt and removes itself before the original callback can terminate
   Main. There is no post-System.exit-finalizer claim. The existing parent
   still requires original Main exit zero, ordered events, absence of failure
   and security-cleanup diagnostics, then fresh real Room verification. An
   image update that dismisses the native popup or unsupported keyboard
   selection cannot pass merely from a crop: the exact Exit event is required.
4. **Readiness counterexample corrected before execution.** V1 compared the
   entire screen except the tray marker. Main hover/ripple/focus/decorative
   repaints can enlarge that difference and reject a real native surface.
   V1 was rejected unexecuted, not recorded as an application failure. V2
   captures and checks stationary Main bounds, excludes that frame only from
   readiness differences, and rejects a marker overlapping that mask. It
   retains the full original-pixel corner neighborhood, not the partly masked
   difference box, so native rows over Main are not silently removed. The
   marker must actually fit a 64-pixel screen corner; no panel placement is
   assumed, no window is moved and there is no fallback. Readiness still does
   not establish full native bounds or visual completeness.
5. **Ownership and bounded evidence.** Marker/listener acquisition is captured
   before partial mutation; failure cleanup attempts all independent releases.
   Original callbacks remain installed. The marker is flushed only after image
   restoration returns. Native right press and keyboard releases preserve
   failure status. Images use an in-memory stream, original-channel force before
   stream close, private create-new files, fixed names and post-write size checks.
   Native/EDT timeouts can still outlive a caller: existing captured-child and
   outer GUI hard settlement remain necessary, not proved by local finally.

Rejected V1 bytes remain independently identifiable:
`CredentialMainNavHostRoomIntegrationTest.kt.rejected-v1.txt` =
`69ae5e56f2a2afbc72cae7404381e67a5f55dbb794d032373070366d34c01c77`;
`TRAY-COMPLETION.rejected-v1.patch.txt` =
`748d969a92eba18fc016a1a03e88b8b6806b76198610da26532c090d8fa21101`.

## Required result/control contract

This extends one already-unexecuted PVA027 case, not a new endpoint or harness.
Existing credential/PVU003 event contracts and seed/verify roles are unchanged.
GUI4 remains four expected cases in two serial Test tasks; these are planned
counts, not executions.

PVA027 main events, exactly ordered:

```text
PVA027_MAIN_TRAY_EN1
PVA027_TOOLTIP_EN1
PVA027_MAIN_TRAY_AR
PVA027_TOOLTIP_AR
PVA027_MAIN_TRAY_EN2
PVA027_TOOLTIP_EN2
PVA027_NATIVE_LOCK_DISPATCH
PVA027_MAIN_LOCKED
DRIVER_ASSERTIONS_COMPLETE
PVA027_EXIT_KEY_CALLS_COMPLETE
PVA027_NATIVE_EXIT_EVENT
```

Two seed + eleven main + two verify = fifteen events, not fifteen tests.
Five exact PNG names: `tray-tooltip-en1.png`, `tray-tooltip-ar.png`,
`tray-tooltip-en2.png`, `tray-lock-selection.png`, `tray-exit-selection.png`.
Each is nonempty, at most 700x400 and 1 MiB. Existing 27 role files plus these
five files are 32 evidence files, not cases. GUI4 controls must consume the
accepted fixture hash and update event, retention and visual qualifications
together; their separate independent review is not supplied by this review.

After admitted execution, an independent visual reviewer must see the complete
expected English/Arabic/English tooltip text, Arabic shaping and no clipped
text, and all expected native menu labels with Lock/Exit respectively selected.
PNG presence/header, masked geometry and the callback receipts cannot replace
that inspection. Missing/incomplete images, wrong native target, premature
lock, failure receipt, bad ordering, unknown/nonzero exit or failed real Room
verification reject the affected claim. The observed corner/layout eligibility,
Toolkit behavior and image-update/popup interaction remain actual-run gates.

## Source ancestry and retained limitations

Relevant surrounding production identities read:

| Source | SHA256 |
| --- | --- |
| `DesktopSystemTray.kt` | `d1023a5bc9573e97eaa3089cf433107f0e009f22e67e01c4fd4f1e77e0e28d58` |
| `PassVaultDesktopWindow.kt` | `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702` |
| `DesktopShutdownCoordinator.kt` | `7156e03eb7e5d2ea7636a9320a49aeaf15d0694522d88f78836b075c74e9043d` |
| `DesktopWindowProtection.kt` | `92c473e21c262972983b111b61adc18610bc17fcea92c6cc2b7f65ea4d5ee128` |
| `DesktopFocusLossLockPolicy.kt` | `747e1ff80ee4a85876fbef29f260e9e13d1752942549ce389510e42c715dae82` |
| `VaultRepositoryImpl.kt` | `ce012cc2b0ffc76783b081f31285c7247bdc6192e4301f6af637c2df5cdac8e8` |
| `AppModule.kt` | `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
| `DesktopModule.kt` | `7b70040fe488c9825090548b3d8ca4a2e4f34c1989b072d2571d74c9647cd253` |

Preserve the historical native-menu/Show endpoint result under
`reviews/storage/INTEGRATION02-ACTUAL-RESULT-REVIEW.json`, SHA256
`254b133bbdf4b4f8e903cb3b53b3739518f5d8a3d57a81f6a744d17dfadccf2f`.
That endpoint passed, but the aggregate integration02 did not; this proposal
does not rerun or expand that historical proof. Earlier caller-chain caveats in
`reviews/editor-independent/PVA027-MAIN-SETTINGS-PROPAGATION-INDEPENDENT.md`,
SHA256 `fa96cf398f55fd93db07bbc33aac1adc1cb3b6e8f4f432cb3f53da11b754b80a`,
remain accurate for their earlier property-only generation.

No native Show/re-unlock, restored-curtain, preference persistence, packaged
product-icon, other Desktop backend or physical-device security claim is added.
No product/dependency/version/identity change, protected-ref movement,
build1017001 action, owner/PVD decision or STOP/NO-RETRY/CLOSED restriction is
altered. **Zero application executions and zero qualified closures are added.**
