# PVU-006 — independent onboarding/terminal caller challenge

Reviewer: `/root/editor_review`; author challenged: `/root/native_review`; 2026-09-10.
**ACCEPT CONDITIONAL SOURCE NARROWING ONLY. Remain UNRESOLVED / VERIFICATION BLOCKED.**
The real automatic caller is useful additional investigation, not a confirmed harmful schedule or fix.

## Binding and continuity

Reviewed `../native-independent/PVU006-TERMINAL-SOURCE-CONTINUATION.md`, SHA-256
`5b8dcda1b1e45e662e2e99766dd8d5d096ec07fd65b871a4ea0fe93aab9756cd`.
Preserve prior `../verification/PVU006-CURRENT-SOURCE.md` SHA
`5f7652d07deb935cf5378eb896000fc51d374713f731f7488b7a2ffe88f56a7f` and its independent
`../build-config/PVU006-CURRENT-SOURCE-REVIEW.md` SHA
`7a688f2c9915158362252d2970b7e6084e5ca6e4ec54c3110c097b6852faa0cd`.

All 17 production and three test paths newly read below matched their hashes in those inventories:
OnboardingViewModel; VaultRepositoryImpl; Main; DesktopShutdownCoordinator; DesktopSessionCleanup;
PassVaultDesktopWindow; AppModule; AppDatabaseLifecycle; VaultDatabaseBootstrap; VaultMetadataDao;
PassVaultNavigationHost; PassVaultApp; AuthRouteAdapters; NavigationBackCoordinator; OnboardingScreen;
MasterPasswordConfirmationScreen; VaultLockRetry; DesktopShutdownCoordinatorTest;
OnboardingViewModelTest; BiometricUnlockFreshnessIntegrationTest. This binds focused spans, not full-file
coverage, a valid Git tree, a clean workspace or executed cases. Root owns later publication binding.

## Attempts to disprove reachability/harm

1. **Not a fabricated later button press.** Auth adapters49–58, confirmation UI90–99/286–307 and
   OnboardingViewModel226–280 supply an enabled, validated Confirm that launches one real createJob;
   that same job calls create and then unlock. Native Window close remains enabled and goes directly
   through requestClose (PassVaultDesktopWindow130–136/158–181), not guarded navigation Back.
   Nonetheless, clearForLock301–305 cancels that job, and ensureActive261/273 plus the completion
   password wipe245 are real counterguards. The ordinary beforePop registration is not terminal
   singleton disposal: NavigationBackCoordinator173–174 only unregisters it.
2. **The interstage window is constrained.** Create holds transition→operation→session mutexes for
   its entire operation (VaultRepositoryImpl136–160/659–664), including real insert and null-reason
   Locked publication. Onboarding has no explicit suspend/yield/delay between successful create return,
   ensureActive/state update and its unlock call. Do not insert an arbitrary await there and call it a
   shipped schedule. Concurrent Default cleanup is real (AppModule78; coordinator89–99/116–119), so
   source still does not rule out a mutex handoff to the terminal lock. But it must actually finish the
   Locked/null-key short path592–595 AND release its intent375 before unlock snapshots239–244.
   Pending/newer lock intent rejects admission/commit816–843; a completed ordinary lock intentionally
   permits a genuinely fresh attempt. This is not a regression proof against PVA-033.
3. **The observer must be measured, not omitted.** NavigationHost155–180 collects the session and
   supplies ObserveSessionSecurity367–387; a delivered reasoned lock calls onboarding.clearForLock424.
   PassVaultApp285–314 handles a completed lock whose Locking state was conflated. Create's null reason
   is not that event. A disposed collector is not a singleton clear; equally, merely having a disposal
   path does not prove the real observer missed this transition. Observe delivery/clear and disposal
   ordering rather than suppressing the observer in a witness.
4. **Room is a substantial competing barrier.** AppModule86–95 binds the actual shared Room instance
   and AppDatabaseLifecycle to checkpointAndClose145–178, which checkpoints then really calls close.
   A late unlock needs metadata, valid real crypto, then successful updateLastAccessed before the
   generation-guarded key copy/publication (VaultRepositoryImpl256–277/733–742). Closed-Room I/O failure
   safely defeats this candidate; it is not no-vault/delete authority. Conversely, do not collapse SQL
   completion, coroutine return, Room close and openSession commit into one timestamp: source supplies
   no database-lifetime check in commit. A successful required I/O followed by a delayed continuation
   is an ordering question, not proof that such a harmful continuation occurred. No stalled writer or
   mocked successful DAO call after closure is justified by this trace.
5. **Process death limits the claim.** The coordinator starts lock→database cleanup and requests
   application exit without waiting (48–60). Main intends one runtime, conditional finish128–140,
   then System.exit31; no continuing-runtime restart loop was found. Main117–125 uses bare Compose
   application { ... }: repository source alone does NOT verify the pinned helper's default exit
   behavior or guarantee reaching the later finally/explicit System.exit. Do not infer a guaranteed
   2.5-second live-JVM window from the latch. No dependency-contract guess is needed to retain the
   missing externally observed process-death boundary. UI-state completion, an unrooted cycle, or
   transient work before actual death is not demonstrated permanent retention/data loss/hang.

## Smallest meaningful next software evidence

The gap is **software lifetime/ordering**, not inherently iPhone/Windows hardware. A smaller actual
ViewModel/real-repository scheduling probe can test necessary admission edges, but an inserted interstage
pause, omitted observer or fake close cannot establish terminal reachability; a negative sample cannot
rule out all schedules or close PVU-006. Existing source tests do not supply it: the shutdown tests use
operation/exit callbacks, onboarding's cancellation-named test never submits Confirm, and freshness100–110
is a deliberately fresh ordinary unlock. Their successful prior evidence is not discarded or rerun here.

If root prioritizes this candidate, the smallest decisive next observation is one separately admitted,
isolated actual Desktop close during legitimate synthetic onboarding, retaining real observer/disposal,
coordinator, bootstrap and Room behavior. Correlate only non-secret owner/operation markers for create
return, terminal lock/intent release, automatic unlock admission, delivered clearForLock/disposal, required
last-access I/O return, session commit, actual Room close, and external process death. No artificial long
writer, replacement exit callback or invented post-close unlock event. A successful late session must
actually be observed and assessed against death/cleanup before promoting harm. Preserve countercontrols:
delivered cancellation, still-pending lock rejection, closed-Room I/O rejection, and allowed genuinely
fresh ordinary unlock. A clean sample is useful bounded evidence, not universal lifetime closure.

No harness/run/retry is authorized by this recommendation; fresh independent execution, coordination,
isolation, bounds and cleanup review remain necessary. The absent result/admission is the present blocker,
not a missing skill URL, mandatory hosted platform, or a reason to weaken the existing STOP/HOLD scopes.

## Accounting and source-read limitations

Production/test/fixture edits0; builds/tests/runtime invocations and executed cases0; new confirmed
families/closures/conclusive suspicion resolutions0. Only this independent note is new; the accepted native
tray fixture remains frozen. No Git, CI, network, helper import, application storage, shared-cache access,
process probe, temporary runtime or background worker was used. No build cleanup claim is made.

Read receipts:12add5 report; cce4d7 prior reports/instructions; 08cc37 output was truncated, so relevant
repository/coordinator spans were reread; c5806b printed required repository lines then exited1 on a
reader upper-bound IndexError (requested853 beyond846, no source mutation); ecdfb9/6ab1c6/ee4698/0f7b16
completed bounded follow-ups; b47660 supporting skill snapshots. These reader outcomes are not test cases.
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 recorded failure/no automatic retry, G7/G8 CLOSED, all other HOLDs,
ordinary-lock compatibility, the eight PVD boundaries and unrelated hardware gaps remain unchanged.
