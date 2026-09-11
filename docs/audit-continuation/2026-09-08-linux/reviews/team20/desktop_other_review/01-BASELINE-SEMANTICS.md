# Desktop remaining fixtures — independent baseline review

Reviewer `/root/desktop_other_review`; 2026-09-11. Paired author:
`/root/desktop_other_author`.

**PREPROPOSAL SOURCE REVIEW ONLY.** No patch accepted/applied by this report;
no new run, product finding, family closure or execution permission. C17 source,
tests/helpers and pending GUI03 bindings remain frozen. This lane excludes
`CredentialMainNavHostRoomIntegrationTest.kt`.

Read TEAM_20_RESUME, both saved pause notes, AUDIT_HANDOFF, prescribed
START_HERE/PERMISSIONS/ASSEMBLY/PUBLISHED_PAYLOAD, repository AGENTS and focused
Desktop-native lifecycle, navigation verification and secure-restoration skills.
Reviewed the three complete fixtures, relevant production loop/tray/window
protection, current ledger qualifications and retained actual evidence below.

## Exact current inputs

Paths relative to W, unless stated otherwise. C17 identity is inherited from the
pause/resume authority, not a new Git observation. SHA-256 binds bytes, not
correctness or whole-tree coverage.

| Source | SHA-256 |
| --- | --- |
| `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/DesktopApplicationLifecycleIntegrationTest.kt` | `422cbfd7b85a08585b99519b94f41296ed24ea1dce133f47f4fdab2ddf5a85b9` |
| `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/DesktopCurtainRenderingTest.kt` | `6e5f8190e94008f3e3c41b62b3d1d75cdca471057e2de57497829d5c42191c57` |
| `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/tray/DesktopSystemTrayNativeIntegrationTest.kt` | `0a70e40b519ec22e33a075467ec1961a40e8c9c091c43d53c31915e818f1baec` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopApplicationLoop.kt` | `fc8c553ee02f6c84fc535ea0672436b9688dde6753101f9d952858a05516bfac` |
| `core/security/src/desktopMain/kotlin/com/passvault/desktop/security/DesktopWindowProtection.kt` | `92c473e21c262972983b111b61adc18610bc17fcea92c6cc2b7f65ea4d5ee128` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/tray/DesktopSystemTray.kt` | `d1023a5bc9573e97eaa3089cf433107f0e009f22e67e01c4fd4f1e77e0e28d58` |
| `detekt.yml` | `ef1abd634b35c339037856ee5f00b584a74d6a9783c6c0629637adbd8bc1af5e` |

Retained diagnostic authority:
`B/runs/linux-detekt03/logs/detekt.log`, SHA-256
`f5e6ba8d91ca7d552c2c4f44133e5cc1d856dbd4481b139107c41655aabd4f97`,
lines 106–126. Exactly **21 diagnostics**, not 21 tests or product defects:

| Fixture | Rule and original source lines |
| --- | --- |
| Lifecycle (2) | LongMethod 72; ThrowingExceptionFromFinally 138 |
| Curtain (10) | CyclomaticComplexMethod 65/306; LongMethod 65; TooGenericExceptionCaught 99/276/285/286/291/299; ReturnCount 306 |
| Tray (9) | CyclomaticComplexMethod 337; LongMethod 175; ThrowingExceptionFromFinally 323; TooGenericExceptionCaught 101/320/321/342/465; ReturnCount 272 |

## Guards and counterexamples a proposal must preserve

### Lifecycle

- Opt-in precedes graphical work; synthetic X11, JDK17, disjoint fresh
  runtime/evidence roots and explicit Test runtime classpath are asserted.
  Child environment is cleared, then rebuilt with owned HOME/TMP/XDG locations.
- Exactly two serial child JVM modes and one JUnit method. Default real Compose
  exit must produce only `WINDOW_READY,CLOSE_REQUEST` and exit0. Production
  `application(exitProcessOnExit=false)` must additionally pass caller finally
  and continuation, exiting23. Merely exiting0 in both branches cannot pass.
- `runChild` 104–144 owns a captured child, bounds log/trace/deadline, forcibly
  settles only that child if still alive, and records its exit after settlement.
  Generic `Throwable` preserves assertion/error and interruption identity.
- The flagged throw in finally is **conditional on no primary failure**. With a
  primary, the cleanup error is suppressed instead. Moving this into an unguarded
  finally throw, returning despite cleanup failure, clearing interruption without
  restoration, or replacing the captured child with an ambient process query
  would be regressions. Extraction may be safe but is not necessary to correct a
  demonstrated cleanup bug; exact narrow suppression needs independent rationale.

### Curtain

- Synthetic Foundation Button-role control, direct safe-content state and manual
  secured ACK are intentionally **not** production Unlock/authentication, Main,
  coordinator epoch, repository or route cleanup evidence. Production retains
  its curtain after secured ACK; removal follows already-unlocked state/cleanup.
- Keep actual heavyweight Compose Canvas/native-handle checks and actual native
  iconify/restore event counters. Withhold ACK until new iconified frame progress;
  do not restore early merely to make a hidden renderer progress.
- Preserve all 27 positive sample matches and **zero** pre-ACK unlocked hits.
  The previously rejected predicate `!allSamplesMatch` would accept 26 exposed
  unlocked samples and one missing sample. This lint work must not reintroduce it.
- Post-ACK lock, geometry, pixels and native input observations occur **before**
  the explicit unlock/removal control. Failure observations survive that control;
  moving assertions/control state too early can erase the discriminating result.
- `interact` 306–349 gates native input with current displayability/showing,
  activity/iconification, exact client rectangle, unchanged nonempty wholly
  contained control, and rechecks before move, press, key and after key. Missing
  target returns a named blocked observation. Guard extraction must preserve
  short-circuit order: do not call `locationOnScreen` after the showing guard
  fails or emit later native groups after scope changes.
- Pointer/key deltas must each be1; callback lock snapshots must be
  `[false,false]` at baseline/removal and `[true,true]` post-ACK. No direct callback
  invocation, mock input or renamed passing assertion can replace these oracles.
- Test catches all scenario failures, separately attempts protection cleanup and
  window disposal, preserves suppressed cleanup/write errors and finally rethrows
  the first failure. Each screenshot/output stream is closed and images flushed.
  `preEvidenceWriteOutcome` deliberately predates write success; actual XML and
  root worker settlement remain authoritative. Native/EDT stalls remain root's
  hard-deadline responsibility, not solved by source decomposition.

### Native tray

- Opt-in and private session/home/config/cache/data/runtime/auth/bus/evidence
  guards precede Toolkit/resources. Actual public production tray constructor,
  original icon/popup/menu-item identity and EN→AR→EN captured environments stay.
- Native right-button event from the exact TrayIcon precedes Down/Enter input.
  Pixel deltas exclude owned frame/icon and require size/density/proximity;
  popup readiness alone is not text/shaping/highlight proof. Preserve per-stage
  fresh callback and cumulative no-extra-action/right-press assertions.
- `nativeInput` registers release **before** press. Release is attempted even if
  press partially fails; a failed release stays in `heldInput` for close. A cleanup
  error cannot replace a press error, and release-only failure cannot pass.
- `close` independently attempts all input/observer/image/tray/frame cleanup,
  verifies removal before flushing the synthetic image, restores ImageIO cache
  and all three Locale defaults, writes bounded original-file evidence, and
  restores interruption even after multiple cleanup failures. Removing the tray
  attempt must not imply successful removal: production cleanup catches native
  exceptions, so the public-empty verification remains essential.
- EDT timeout/failure cancels the captured FutureTask without interrupting native
  code (`cancel(false)`). It does not prove a stalled already-running EDT task
  settled. Keep this distinction and outer cleanup obligation.
- Precise `Throwable`-boundary suppression can be justified; changing catches to
  `Exception`, swallowing AssertionError, conflating primary/cleanup failures or
  restructuring ownership merely for a complexity threshold cannot.

## Previously supported evidence retained, not re-executed

- `B/reviews/storage/INTEGRATION02-ACTUAL-RESULT-REVIEW.json`
  SHA `254b133bbdf4b4f8e903cb3b53b3739518f5d8a3d57a81f6a744d17dfadccf2f`.
  Read actual lifecycle/tray XML and trace receipts: one PASS each, zero errors,
  failures or skips; default exit0 and production exit23 match the exact event
  sequences above. Tray shows callbacks `0:show,1:show,2:show`, rightPresses1/2/3,
  all three stages dismissed, owned tray removed and frame disposed. These two
  PASS outcomes do not erase integration02's separate Main-driver FAIL/unstarted
  Room case or any historical held runtime.
- Lifecycle XML SHA
  `ede424dbfe5452f891875ae5f74520cd5edda5cf337032afe15692108b706a59`;
  tray XML SHA
  `99878d9760cd80d9ce52a099aa9711f334f03a81c0d54a75f96c4f44f28db27d`;
  tray observations SHA
  `b1da4ea080258488a97093998dca9e9468d5e22e9050b4a769bb49a02e3be76b`.
- `B/reviews/verification/LINUX-DESKTOP-GUI02-ACTUAL-RESULT-REVIEW.json`
  SHA `66f5cd60d4d245a845f2792ae97119f4384c31462267deefa3b141bcfdbe42a6`.
  Read actual curtain XML/observations: one PASS on Linux amd64/JDK17/Xvfb/Metal
  LAF/SOFTWARE_FAST with GL fallback warning. Post-ACK all27 safe hits and native
  pointer/key1 each while locked; pre-ACK zero sampled unlocked hits. Observations
  SHA `ccef7fe58f92a6b7282eea1c4930424e4944234b7921bacfce2e84f63c8badc3`.
  **Not reproduced on this tuple**, not universal PVU005 resolution/no-flash or
  hardware/authentication proof. Prior independent PNG review is attributed to
  its original reviewer; this lane did not newly inspect those images.

## Smallest meaningful prospective validation

After root explicitly reconciles the GUI03 freeze: review exact unapplied patch
bytes, original-source hashes and semantic/oracle equivalence first. If changes
remain declaration-local diagnostics or semantic-preserving private extraction,
include only affected `:app-desktop:detekt` in the future independently admitted
focused static candidate, with unchanged configuration/dependency checks and
the full intended Desktop source set. It is not permission to execute it now.
Do not rerun successful lifecycle/tray/curtain endpoints merely for lint, invent
new tests, or repeat all22 Detekt modules. If source movement cannot be shown
equivalent, report that specific gap rather than broadening execution silently.

All source/ledger/runtime writes, builds/tests, Git/CI, helper execution/import,
SDK/process/runtime probes and delegation are outside this lane. Only this
reviewer's reports may be written. STOP/NO-RETRY/CLOSED, original-lock,
held-runtime, source-freeze and publication restrictions remain binding.
