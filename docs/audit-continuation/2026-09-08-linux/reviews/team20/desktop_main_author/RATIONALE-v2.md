# Main/NavHost/Room Detekt03 proposal v2 — source only, unapplied

Author `/root/desktop_main_author`; independent reviewer `/root/desktop_main_review`.
**PROPOSED, NOT APPLIED; independent acceptance and root freeze release required.**
Only `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`
is proposed for change. No production source, other fixture, helper, dependency,
configuration, central ledger or test declaration changes. C17 application/helpers/T
remain frozen. Root reported GUI03 exit70 during preparation before tests; that is
not a fixture failure or a runtime result for this proposal.

## Exact identities

- Current source: SHA256 `cdb4002e4be7ca4c2c7c6dcb42a586ad5429618693c8c38c63bbcfc52302a2c4`,
  66,405 bytes / 1,212 LF; unchanged at the post-resume source-data check.
- Candidate: `MAIN-NAV-DETEKT03-v2.patch.txt`, SHA256
  `aae3502ccd616d322ba0798f131d0bb6258de287812e482314ace5d6c4510fcd`,
  15,729 bytes / 291 LF.
- Proposed after-image, reconstructed **in memory only**: SHA256
  `1c7cf84fec50c6e161d2e3e282066c372a8950b0c6f2bd7c08b1fa423d769761`,
  68,741 bytes / 1,251 LF. This is not an applied source/tree identity.
- `SOURCE-IDENTITIES-v2.json` includes the 26 exact retained diagnostics and source
  comparisons. Retained `runs/linux-detekt03/reports/app-desktop-checkstyle.xml`
  SHA256 is `ccf904daa964e1004cce4b9b9d0ce25266971ff7042f966fd883bd288eba1471`.

V1 is retained and **superseded**, not another integration option. Source accounting
found its environment-only extraction could leave runRole at61 against limit60;
v2 also extracts the three existing log/event assertion lines. The first v2
composition stopped before writes on an author data-comparison assertion. Inspection
proved its extraction delimiters differed by one LF; the corrected comparison
explicitly checks that sole delimiter. This was not a compiler, test or helper run.

## Eight diagnostics corrected by small source transformations

All references below are original source lines, not proposed line numbers.

| Diagnostic | Proposed correction and preserved contract |
| --- | --- |
| LongMethod122, runRole | Move the unchanged environment map/private-session validation/XDG creation into roleEnvironment at the same call position, and the three unchanged log-read/forbidden-log/event assertions into assertRoleEvidence. Source line accounting suggests59 versus limit60; only future Detekt can qualify the actual result. |
| ComplexCondition406 | Return from the passive AWT listener on a non-MouseEvent before applying the unchanged left-button press/release predicate. The type guard is explicit; no new native input or logging. |
| CyclomaticComplexMethod518 | Move only the pure languageOption selector to private pva027LanguageOption. Its role/name/prefix/uniqueness predicate is unchanged; no owner, listener, observer, frame, VM lookup or native click is moved. |
| MaxLineLength472/536/594 | Wrap one assertNotNull expression and place the two long inline comments above their unchanged calls. All proposed non-import lines were checked as at most120 characters. |
| ReturnCount615 | Replace the two null-screen early returns with nullable let expressions. Missing target still returns null; target actionability is checked before missing target.screen can return null. Ancestor bounds/window/exact-scroll checks still precede missing scroll.screen; enabled/viewport/target-size checks still follow it. Two returns, unchanged polling versus failure distinction. |
| ReturnCount976 | Initialize ancestor from unique(matching field titles)?.parent. Missing title or missing parent yields the same null result. The nearest ancestor with exactly one Edit still wins; zero or multiple edits still continue upward. Duplicate field-name rejection remains in unique. Two returns. |

runRole still validates file bounds then zero child exit before reading log/trace;
all those assertions remain inside its original try/catch. The ProcessBuilder argv,
private environment values, captured child assignment, waits/deadlines, forced-failure
containment, settlement receipt and original finally retain their order. No next
seed/Main/verify role is allowed after any failed assertion or cleanup.

## Eighteen intentional boundary diagnostics: precise suppression requests

These are **specific proposed false-positive dispositions**, not a module/file/class
waiver or a claim that exceptions are generally safe. Each annotation names only its
rule(s) on the listed function and carries a reason. Independent acceptance is still
required. No exception type, handler body, rethrow, assertion, log content, cancellation,
interruption restoration or cleanup attempt is removed or replaced with runCatching.

### TooGenericExceptionCaught — thirteen existing catches, ten functions

| Function / original catch lines | Why retaining Throwable is required here |
| --- | --- |
| runRole181 | Aggregate primary assertion/native failure while the already-captured child and bound roots are settled; never replace the primary failure with cleanup failure. |
| CredentialMainNavHostRoomProbe.main252/262 | These are isolated driver/process terminal boundaries. AssertionError and native failures must produce terminal diagnostics/failed outcome rather than escape an Exception-only catch and lose the retained failure receipt. |
| roomRole298 | Preserve all failures while every acquired real vault/bootstrap/biometric/Koin owner receives its existing independent close attempt; then restore interruption/rethrow. |
| NativeMainNav.run350/357 | The queued EDT Quit boundary must report before Main may System.exit; the driver boundary must retain failure while releasing captured input. |
| pvu003ChooserHomeThenCancel431/443 | Preserve partial mouse-press failure and scenario failure while its original one-shot native cancel and listener removal still execute. |
| pva027Click708; withKey789; click803 | A native press may partly succeed before throwing; ownership is captured before press, release remains in finally, and the primary/suppressed failures remain observable. |
| MainNavFailures.release1042 | Each cleanup attempt must be independent even after AssertionError/native failure; existing aggregation/interruption behavior is unchanged and the caller still throws afterward. |
| onEdt1148 | Every failed caller wait cancels pending FutureTask work and rethrows; narrowing to Exception would omit errors. This still cannot stop already-started EDT/native work. |

### PrintStackTrace — three terminal calls only

Original254/264 in child main and353 in the queued EDT boundary remain unchanged.
The exact synthetic isolated child stderr is redirected to the bounded private log;
these are retained terminal evidence, not production debug printing. The existing
MAIN_NAV_FAILURE marker/forbidden-log contract and failed outcome remain. Replacing
this with a lazy application logger could depend on the graph that failed or race
Main's terminal exit. Only those two terminal-boundary functions request this rule's
suppression; no file-level printing exemption is proposed.

### Chooser LongMethod370 and CyclomaticComplexMethod370

Keep this one-shot security/cleanup owner in one lexical try/finally rather than
redistribute ownership solely to meet a cosmetic threshold. The method captures
model/chooser/listener/exportAttempted/input state, performs exactly one Home native
press/release, samples, cancels the native chooser once, then removes its passive
listener. Splitting those phases merely to satisfy length/complexity would make its
partial-acquisition and exceptional-path contract harder to inspect. Only this
function requests these two complexity suppressions; runRole and the Settings
scenario instead receive the small extractions described above.

## Discriminating behavior retained

- Credential scenario: actual Main/Window/NavHost native dirty toolbar Back,
  Escape, tab and Add/Discard flow; draft assertions; clean-tab positive control;
  serial real Room seed/reopen and exact ordered durable tuples, IDs and secret flags.
- Chooser: exportAttempted before save-button input; listener ownership before
  timed EDT registration; unchanged modal owner/focus/client/occlusion checks;
  unchanged mousePressed/pressedKeys capture; mutable homeDuring/homeAfter OR
  sampling and20/10 observation loops; exact one Escape cancel in finally, no
  callback/dispose/Save/file selection; post-cancel sampling only if no earlier
  failure; listener removal after cancel attempt even if cancellation fails.
  Observation bits and Home outcomes are not forced to a passing value.
- Settings: actual Settings route then native English/Arabic/English radio clicks;
  unchanged singleton lookup timing; same Frame/TrayIcon/PopupMenu/MenuItems and
  reference-equal listeners; enabled state and sampled translated property checks;
  no direct event/setup invocation or tray/window/environment mutation.
- The locator comment now describes separate policies rather than claiming that
  every credential/PVU003 primitive remains byte-for-byte unchanged by this lint
  proposal. Semantic preservation is the claim; fieldEdit necessarily changes text.

Source comparisons retained three Test annotations, thirteen Throwable catches,
three terminal stack-print calls and all assertion/require/check call counts; key
builder/capture/wait/settlement blocks matched. These checks supplement review,
**not** Kotlin compilation, Detekt success or a proof that no bug is possible.
Existing geometry/toolkit assumptions, five-second EDT wait limits, hard-deadline
requirements, non-atomic tray observations and physical/platform gaps remain.

## Smallest future validation scope — no invocation authority

1. Root must first reconcile consumed GUI03 disposition and explicitly release the
   relevant source freeze; independent proposal acceptance alone cannot apply it.
2. The narrow new static target for this file is **:app-desktop:detekt**, retaining
   its existing verifyStaticAnalysisCoverage dependency, strict verification and
   serial/JDK17/wrapper/one-worker/non-daemon/no-CoD requirements. This may join
   the independently reviewed affected-module static04 selection. Do not rerun the
   all22/root aggregate for reassurance. The other21 Desktop diagnostics belong
   to desktop_other_author, so this proposal alone does not claim module success.
3. Test-source compilation and any justified live validation remain unverified and
   require separate root admission. No new permanent test is needed for this
   semantic-preserving proposal. If changed driver/locator code justifies live
   validation, the affected existing app classes are CredentialMainNavHostRoomIntegrationTest,
   Pvu003NativeChooserAdmissionIntegrationTest and Pva027MainSettingsTrayPropagationIntegrationTest;
   retain their exact distinct XML/event oracles and fresh isolated case roots.
   Do not silently rebind the consumed GUI03 instance, replay successful
   lifecycle/tray endpoint cases, or rerun unrelated/shared cases solely for lint.

No Git/T access, build/test/GUI/CI/packaging, helper execution/import, process/SDK/
runtime probe, cleanup or central-ledger change occurred in this lane. All writes
are own-directory unapplied proposals/source identities/this rationale. No application
case or new family closure is claimed. PVU007 STOP, PVU011 NO RETRY, PVA029's retained
failure/no automatic retry, G7/G8 CLOSED, held runtimes, original-lock and publication/
identity/dependency/occupied1017001 fences remain intact. Root owns all admission,
actual-result reconciliation, application and publication decisions.
