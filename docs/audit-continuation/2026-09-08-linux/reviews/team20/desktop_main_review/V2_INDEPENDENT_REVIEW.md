# Main/NavHost/Room Detekt03 v2 — independent source disposition

Reviewer `/root/desktop_main_review`, 2026-09-11.
**ACCEPT EXACT UNAPPLIED V2 SOURCE PROPOSAL, including the eighteen specifically
reasoned boundary diagnostics below. No required source correction identified in
this bounded delta. This is not application, compilation, Detekt/runtime success,
execution/cleanup admission, freeze release or a claim that no bug is possible.**

Root explicitly resumed source-only work after reporting GUI03 terminal70,
preparation143 and zero test cases. That preparation failure is not evidence
against this fixture's logic. Its actual-result/cleanup disposition belongs to
root and the separately assigned reviewer; it was not reviewed here. C17
application/tests/helpers and T remain frozen. The earlier baseline's
“unconsumed GUI03” description is historical, not the current execution state.

## Exact reviewed identities

The sole proposed source path is
`app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`.
Author paths below are relative to `reviews/team20/desktop_main_author/`.

| Input / hypothetical result | SHA-256 |
| --- | --- |
| Current before-image, 66,405 bytes / 1,212 LF | `cdb4002e4be7ca4c2c7c6dcb42a586ad5429618693c8c38c63bbcfc52302a2c4` |
| `MAIN-NAV-DETEKT03-v2.patch.txt`, 15,729 bytes / 291 LF | `aae3502ccd616d322ba0798f131d0bb6258de287812e482314ace5d6c4510fcd` |
| `SOURCE-IDENTITIES-v2.json` | `af2ebd4e4f8cb750a2a03ebca419ec911e8ca141f849b296ab3d7c1ea6e85d21` |
| `RATIONALE-v2.md` | `92730e53c78d5632cbb35c2524ae6d7568c40dd7ee30e4a224f6e5fac22f5dca` |
| Independently reconstructed after-image, 68,741 bytes / 1,251 LF, **memory only** | `1c7cf84fec50c6e161d2e3e282066c372a8950b0c6f2bd7c08b1fa423d769761` |

The full baseline fixture and focused actual Main/Window/NavHost/Settings/chooser/
Room contracts were inspected before the coordination pause. Reuse
`BASELINE_CONTRACT_REVIEW.md` and `BASELINE_SOURCE_BINDINGS.json` for that bounded
source challenge; this is not a restarted whole-project audit. The before-image
matched the retained C17 manifest tuple, not a fresh Git/index observation. V1
remains superseded and is not an accepted alternate candidate.

Twenty exact-context hunks reconstruct the claimed after-image and reverse
exactly to the current source. `V2_SOURCE_DATA_REVIEW.json` at SHA-256
`e302c6c472b4f51dda403473c569bb3d109944303ccab3d7c8941a5407fa8c6e`
records independent source-data checks. Its historical pending-rationale field
is superseded by this completed semantic disposition. No after-image was written
or project/helper code parsed as executable code, imported or executed.

## Eight source corrections — semantic and compatibility challenge

1. **runRole length:** `roleEnvironment` keeps the same map entries, original
   private-session checks and four XDG child creations, in the same order and at
   the same call point before ProcessBuilder/start. It returns the same map as a
   read-only interface; the caller only copies it into the builder. No acquisition
   crosses an exception/cleanup boundary. `assertRoleEvidence` contains the same
   log read, forbidden-log predicates and exact ordered-event assertion. It runs
   inside the original try, after bounds and zero-exit checks and before finally;
   thrown assertions/I/O failures still reach the original failure accumulator.
   Child argv, capture-before-wait, deadlines, log/trace monitoring, forced-failure
   containment and settlement finally matched independently. Added private call
   frames can change failure stack text, not its diagnostic/exit/event oracle.
2. **Passive mouse condition:** `return@AWTEventListener` for non-MouseEvent ends
   only that SAM invocation, not the chooser scenario. Such an event previously
   did nothing; for a MouseEvent, the identical left-button press/release, owned
   frame/rectangle and delivery-bit predicate remains. Kotlin's explicit type
   guard supports the same smart cast. No listener/input ownership or sampling
   phase is moved.
3. **Settings complexity:** the extracted private `pva027LanguageOption` has the
   same role/name/prefix/uniqueness predicate and captures only the supplied label.
   Neither call's timing changes. Owner/listener snapshots, native clicks, singleton
   lookup, distinct stage receipts and English/Arabic/English assertions remain
   untouched. Initial SYSTEM→English alone still cannot establish an update;
   Arabic and the return to English remain the meaningful changed-value checks.
4. **Three long lines:** one assertion is wrapped and two comments move above
   unchanged calls. No literal, expected value, selector or oracle is removed.
5. **pva027Locate returns:** both nullable `let` blocks are synchronous, ordinary
   inline Kotlin, without a new callback/thread/owner. Missing target still
   returns null. A present target must pass actionability before null geometry
   can return null. When target geometry exists, the same bounded ascending
   ancestry, same-window and exact-single-scroll assertions run before nullable
   scroll geometry; scroll enabled/viewport/target-size assertions remain after
   it. Thus absent geometry remains polling, while malformed/ambiguous geometry
   remains failure. No fallback scroll node or assertNotNull substitution exists.
6. **fieldEdit returns:** both old and new ancestor variables have the same nullable
   parent-index type. Missing/parentless title returns null; duplicate titles
   still fail through `unique`. The unchanged loop chooses the nearest ancestor
   with exactly one Edit and continues for zero **or more than one** match. Its
   ancestor selection, node traversal and original target policy are not weakened.

All imports and three Test declarations remain; no dependency/API/toolchain
change is required by the proposed private helpers/SAM label/nullable expressions.
These source compatibility judgments are not compiler results. Source arithmetic
suggests runRole 70→59 and the Settings method 16→14; only actual focused Detekt
can establish those results and detect any new diagnostic. All proposed non-import
lines were independently checked at <=120 characters.

## Eighteen precise intentional-boundary dispositions accepted

| Rule / original sites | Independent disposition |
| --- | --- |
| TooGenericExceptionCaught: runRole181; child main252/262; roomRole298; NativeMainNav.run350/357; chooser431/443; pva027Click708; withKey789; click803; MainNavFailures.release1042; onEdt1148 | Accept the ten function-scoped annotations for these thirteen existing catches. They preserve AssertionError/native/EDT failures, original primary/suppressed handling and independent release attempts; narrowing to Exception or returning fallback success would weaken these explicit test/terminal boundaries. |
| PrintStackTrace: child main254/264; queued EDT boundary353 | Accept only the two annotated terminal-boundary functions. All three calls remain, with the original failure markers/private child-log redirection and rejection contract. No application logger/graph dependency or swallowed evidence is introduced. |
| LongMethod370 and CyclomaticComplexMethod370: pvu003ChooserHomeThenCancel only | Accept this bounded acquisition/input/one-shot native-cancel/listener-finally owner remaining contiguous. The exemption is specific to this intentional experiment's exception/cleanup ownership, not a general threshold waiver. Its ordinary condition/line diagnostics are still corrected. |

There is no file/class/module suppression, changed baseline/config/exclusion,
disabled test or exception-handler body change. The unchanged thirteen Throwable
catches, three terminal prints and assertion/require/check lexical counts were
independently checked; counts alone would not prove preservation, so each hunk
and its assertion order were also reviewed.

## Oracles, ownership and limitations remain

Real seed/Main/verify roles remain serial, with actual Main owning its application
cleanup. Exact Room identities/ordered tuples, draft retention, dirty Back/Escape/
tab/Add behavior, Add-discard-not-forward-replay and the clean-tab positive control
remain. Final durable equality still cannot exclude a transient reverted write,
and the finite input sequence is not a global keyboard/race guarantee.

Chooser `exportAttempted` and listener captures still precede possibly partial
acquisition. The one Home press/release, 20/10 sampling loops, modal/owner/focus/
occlusion guards, mutable OR observations, one native Escape cancel, null-file
check, success-only postcancel sampling and independent listener removal preserve
both normal and exceptional paths. Its receipt values are observations, not a
forced Home outcome or proof about the unexercised pre-chooser/delete/A-V cycle.

Settings still observes the same installed frame/icon/popup/items/listeners,
not a direct setup or injected environment. Same listener identity can wrap wrong
callbacks; property samples cannot establish displayed tooltip, continuous
registration, actual Lock/Exit or preference-save settlement. Five-second EDT
waits do not stop begun native work; input release failures and outer hard
settlement/HOLD obligations are not waived. No successful lifecycle/native-tray
endpoint is rerun or promoted into these different missing caller-chain results.

## Smallest useful verification, not new execution authority

After root reconciles the consumed GUI03 disposition and explicitly releases
only the assigned source freeze, the accepted after-image needs exact binding.
The meaningful lint check is `:app-desktop:detekt` in the independently coordinated
focused static selection, retaining the coverage prerequisite, strict dependency
verification and existing serial/JDK17/wrapper/one-worker/non-daemon/no-CoD rules.
This fixture accounts for26 diagnostics; the other21 Desktop diagnostics and
module-wide result are not disposed of by this review.

One actual `:app-desktop:compileTestKotlinDesktop`, possibly already required by
necessary future validation, is the smallest useful compatibility check for the
new helper/SAM/nullable-expression source. Do not run it twice for reassurance,
add permanent tests solely for lint, rerun successful lifecycle/tray endpoints,
or broaden to an all-tests/all22 aggregate. This semantic-preserving delta does
not itself justify an additional costly GUI test campaign. Existing unexecuted
GUI complements remain separate campaign gaps; any later runtime attempt needs
its own justified scope/admission, not a replay or silent rebind of GUI03.

## Activity and fences

Only bounded source/report reads, in-memory text comparisons and own-directory
reports were used. The first comparison command failed a reviewer global-marker
uniqueness assertion after exact proposal reconstruction, before any report/source
write; the corrected comparison scopes that marker to runRole. The failure is
retained in the data report, not classified as a subject/compiler/test failure.
The seven direct source/proposal/report input hashes above were rechecked before
report creation; other source bindings remain the baseline's bounded observations.
No Git/T, build/test/GUI/CI/process/SDK/runtime/network probe, helper
execution/import, cleanup, source/config/central-ledger edit or subagent occurred.

Zero application executions/new tests/findings/closures/denominator changes are
attributed to this review. C17 freezes, PVU007 STOP, PVU011 NO RETRY, PVA029's
recorded failure/no automatic retry, G7/G8 CLOSED, old runtime/lock/publication/
identity/dependency/occupied1017001 restrictions persist. Root alone owns
integration, actual-result reconciliation, admission and publication.
