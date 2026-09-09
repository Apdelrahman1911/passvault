# PVU-005 Linux fixture: independent source challenge

Reviewer `/root/editor`; fixture/scope author `/root/android32`; 2026-09-09.

**ACCEPT_BOUNDED_FIXTURE_SOURCE_ONLY_PENDING_COMPILE_RUNTIME_AND_FRESH_INSTANCE.**
This accepts the corrected fixture design, not execution admission, a product
patch, a passing regression or resolution of PVU-005. No new concrete ordinary-path
source blocker remains from this bounded review. The original rendering suspicion
remains **UNRESOLVED / VERIFICATION BLOCKED**.

## Exact inputs and extent

Containing product checkpoint C4: commit
`da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`. The new fixture/report were uncommitted
worktree additions at review; this is not a clean-tree or atomic whole-tree claim.
Paths below are relative to the worktree unless prefixed `R/`, which means
`docs/audit-continuation/2026-09-08-linux/reviews/`.

| Input | SHA-256 | Bytes / LF |
| --- | --- | ---: |
| `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/security/DesktopCurtainRenderingTest.kt` (v2) | `6e5f8190e94008f3e3c41b62b3d1d75cdca471057e2de57497829d5c42191c57` | 22441 / 425 |
| `R/android32/PVU005-LINUX-RENDER-SCOPE.md` (v2) | `864089972054889f1fec2496eabfaebfdfff00e615381b3a398a17a7f5c4bec2` | 11782 / 175 |
| `R/android32/pvu005-before-images/DesktopCurtainRenderingTest-5d032a61.kt.txt` | `5d032a613ea5174ecdd4cebebac0b3b853cc6d9fd232e1b459103b6116b8f8cf` | 19993 / 382 |
| `R/android32/pvu005-before-images/PVU005-LINUX-RENDER-SCOPE-42ada317.md.txt` | `42ada31792b82edceea6aaa18113a9461003e271271c328f2e2cc713d64a440b` | 9496 / 138 |

Read both complete fixture/scope versions and the exact v1-to-v2 source diff;
matched all four hashes. V1 was rejected before execution and remains inert.
The source identities in the sealed v2 scope bind the inspected protection,
existing tests, native caller, session cleanup, Main setup, shared navigation
host and build/version inputs. Point-in-time Git comparisons found these
production/configuration files unchanged from preserved handoff `9bdf9559...`.
Additional inspected inputs:

| Input | SHA-256 |
| --- | --- |
| `core/security/src/commonMain/kotlin/com/passvault/core/security/VaultUiSecurityCoordinator.kt` | `1a6c7ac9be3cd4ca5979ab9b09eced6b323b0282d1529b1c7039175d6fbc19e4` |
| `shared/src/commonMain/kotlin/com/passvault/shared/LockTransitionCleanup.kt` | `db7164ca5a916bc80a76f074c88bd8f84902e243213840b95a8cda0555429f3e` |
| `core/designsystem/build.gradle.kts` | `38e51bb1d16a35032a9b6d0d987b6e97ca98c4f062e41c8b4468e762fd369abb` |

Whole-file hashes are byte bindings, not whole-file semantic coverage. Surrounding
inspection focused on lock/restore/curtain/cleanup, caller ACK and unlock guards,
Main's benign Linux settings, dependency exposure and Linux task gating. The
author's host/tool-presence observations and current167 scheduling statements are
not this reviewer's independent runtime or resource observations.

## Independently found fixture defects and corrections

**F01 — partial marker exposure could satisfy v1's absence oracle.** V1
198/208/256 negated a conjunction over 27 RGB samples. One occluded sample with
26 still matching the unlocked marker made `!matches(UNLOCKED)` true. This is an
inert source counterexample, not an executed leak or a new product finding.
V2 190-216,269,363-379 counts each matching sample (0..27), requires zero unlocked
hits before ACK and all 27 for positive pattern controls. The inspected partial
case can no longer satisfy the absence predicate. This does not inspect every
pixel or eliminate transient-exposure uncertainty.

**F02 — v1's removal comparator could target stale screen coordinates.** V1
246-254 called `interact` even after geometry/visibility failure; its input helper
checked activity and containment only against the old client rectangle. An
active moved window could receive Robot input outside its actual client region.
V2 254-264 gates comparator input, and 318-348 checks current displayability,
showing/activity/iconification, exact client geometry and unchanged wholly
contained control before move, press and key groups. A mismatch returns a named
blocked observation and emits no further group. Checked the surrounding callback
and failure classification: setup/geometry failures do not become curtain findings.
These checks are not an atomic native-display grab or hostile-writer containment.

Both are **fixture-source corrections, unexecuted**, not PVA families, runtime
failures, product fixes or additional test cases. Reviewer changed neither fixture
version nor production source.

## Reachability, guards and counterexamples retained

- Actual protection installs the curtain before iconification; in-app and native
  restore paths defer while locked and unsecured. ACK permits restore without
  clearing `isLocked` or removing the curtain. Native caller removal remains tied
  to already-Unlocked session state or cleanup. A plain JFrame pane property does
  not prove visibility above the selected heavyweight Compose surface.
- Production ACK follows repository lock/owned-clipboard cleanup, a requested
  epoch, shared terminal Locked/safe-route and sensitive-state cleanup guards,
  then a frame-clock/recheck boundary. The fixture intentionally does **not** run
  that repository/coordinator/navigation path. Its synthetic safe-content boolean
  and direct ACK are renderer inputs, not real authentication or cleanup evidence.
- V2 uses an actual ComposeWindow, requires a displayable heavyweight Canvas,
  and drives real WindowStateEvents through native restore. Safe and unlocked
  baseline patterns plus native input precede lock. Post-ACK pixels/input/lock
  observations precede the same-window, same-safe-content removal comparator;
  ordinary negative observations survive that comparator instead of being erased.
- Native mouse/Space increments and actual callback lock snapshots must match;
  post-ACK callbacks require `[true, true]`, baseline/removal `[false, false]`.
  No callback is invoked as a mock. The control is explicitly Foundation
  clickable/Role.Button plus BasicText, **not Material3 or production Unlock UI**.
  This avoids assuming a Material3 compile export from implementation-only edges
  without adding dependencies. It is not an observed compiler-failure correction.
- Typed `renderApi`/`windowHandle` replace unchecked reflection; their exact
  public API shape still needs compilation. No dependency JAR or native library
  was loaded here. Linux source gating excludes the conditional biometric
  CMake/CTest edges; actual selected task graph and resolved artifacts are pending.
- Missing opt-in skips before GUI creation; explicit bad display/headless/setup
  cannot return an early PASS. Frame progress while iconified is a bounded
  precondition to investigate, not presumed successful drawing. A frame callback
  precedes presentation; Robot readback supplies the eventual pixel observation.

## Remaining admission, evidence and cleanup obligations

Exactly **one declared test method, zero executions**. Its ordered phases, five
PNG destinations, 27 sampled positions and polling iterations are not test counts.
Independent visual review of the compact client crops remains necessary; the
assertions are not full-frame/text inspection or no-flash proof. Exceptions,
missing ACK/frame progress, failed comparators or setup failures must remain
distinct from a reproduced curtain defect. No other OS, hardware, packaged app,
full authentication flow or portable screenshot-prevention result follows.

The scope's two-minute outer worker ceiling and 12 MiB image/text proposal are
not current resource or execution admission. Five-second loops cannot bound a
blocked EDT, Robot, native call or filesystem operation. Root and its independent
instance reviewer must bind the original private X display/WM/worker and evidence
directories, effective JDK17/Compose/Skiko/native identities and actual API,
pre-start Java2D/AA/LAF configuration, exact command, sole slot, resource limits,
artifact retention, wrapper `--stop`, native/worker settlement and allowlisted
cleanup before execution. System-property opt-in and an empty directory alone
do not establish that ownership. No ambient display or private storage is allowed.

Source cleanup separately attempts protection cleanup and window disposal on EDT,
closes image/output streams and retains errors. A hang can still prevent later
steps. `preEvidenceWriteOutcome` is explicitly before its own write can fail;
actual XML, worker and cleanup outcomes control interpretation. Root must preserve
available synthetic evidence and report any unsettled process/output honestly.

This review used source/data reads, hashes/diffs and this permanent report only:
no build, import, helper/probe, window, Robot, temporary workspace or runtime
output. No process/cache cleanup was needed in this lane. Root owns any future
execution and cleanup. All STOP/NO-RETRY/CLOSED, candidate/publication and PVD
boundaries remain intact; no closure denominator or current167 selection changed.
