---
name: navigation-verification
description: "Plan and execute production-readiness verification for Kotlin Multiplatform and Compose Navigation 3 changes. Use for unit/state-machine tests, route serialization, stack/restoration/security checks, lifecycle and rapid-input races, Android Back/predictive Back, Desktop keys, iOS LTR/RTL gestures, recomposition evidence, build matrices, dependency checks, and final navigation audits."
---

# Navigation Verification

Use this before declaring navigation complete. Adapt commands to the repository; do not replace missing physical-device evidence with assumptions.

## Gate philosophy

Navigation is a state machine plus platform interaction. A green screenshot or one happy-path UI test is not sufficient. Require evidence at four layers:

1. pure routes/state/policies;
2. adapter and lifecycle integration;
3. platform builds and automated behavior;
4. physical gesture and restoration behavior where tooling cannot reproduce it.

Freeze the candidate tree before final gates. Record the exact commit if available; otherwise record dirty-worktree status and a diff checksum/list. Never claim clean-worktree proof when unrelated or uncommitted changes exist.

## 1. Static architecture audit

Search for regressions:

```bash
rg -n 'rememberNavController|NavHost\(|backStack\s*[+.-]?=|\.add\(.*Route|Channel\.UNLIMITED'
rg -n 'UIScreenEdgePan|gestureRecognizer|recognizer.*class|fixed.*tween|durationMillis'
rg -n 'activityGeneration|mutableStateOf\(.*activity'
```

Interpret results; tests and legitimate local data structures can match. Confirm:

- one active navigation engine;
- one production stack mutation boundary;
- route adapters are feature-owned;
- no screen receives mutable stacks or a global navigator without justification;
- entry-owned ViewModels are not application singletons;
- no private recognizer/class-order hack;
- no unbounded navigation command source;
- no protected restoration before authentication;
- no platform-specific alternate logical stacks.

Run formatter/static analysis and dependency verification required by the repo.

## 2. Route contract tests

Test every route, not representative routes:

- construction with valid arguments;
- invalid/boundary argument rejection;
- serialization encode/decode round-trip;
- stable serialized-name/snapshot contract;
- exhaustive route-kind registry;
- exactly one route adapter;
- exactly one owning stack unless documented;
- minimal payload review (no secrets/domain objects).

Adding a route must fail a registry test until ownership, adapter, serializer, and relevant external mapping are updated.

## 3. Navigator state-machine tests

Cover:

- push, duplicate top, chosen existing-route semantics, and optional multi-instance identity;
- pop, root protection, pop-to-root, root replacement;
- tab selection, preservation, reselection, rapid alternation;
- active-route/root/tab token validation;
- inactive entry, inactive host, old session, and locked access rejection;
- rapid/double click and concurrent/stale effect behavior;
- mutation-result classification, including diagnosable programmer errors;
- bounded command input under high volume;
- no replay after collectors/lifecycle restart.

Use high-volume tests (for example 100,000 submissions) for bounded/conflated dispatchers and assert retained pending capacity, not only runtime completion.

## 4. Back-policy tests

For every Back source and disposition:

- `PopNow` pops once and performs cleanup;
- `HandleInPlace` changes only local state;
- `Blocked` consumes without stack or visual previous-entry exposure;
- root policy returns exit/dismiss semantics as documented;
- dirty editor and discard confirmation;
- dialog, search/local pane, and transient UI priority;
- active import/export/save/security operation;
- duplicate interactive/programmatic completion;
- stale session/entry and host detach;
- replacement registration cleanup;
- tab-root behavior;
- forward navigation cannot bypass a relevant leave guard.

Test the function that projects visible entries: non-`PopNow` must expose only the current entry.

## 5. Tabs and restoration tests

- Independent history for every top-level destination.
- State/scroll/saveable restoration after tab switching.
- Tab reselection behavior.
- Compact/expanded/tablet/desktop logical equivalence.
- Deep link selects correct tab and builds canonical history.
- Configuration recreation restores allowed state.
- Process recreation begins at the correct security root.
- Protected route keys remain quarantined until successful auth.
- Wrong root, cross-tab route, malformed ID, deleted/stale entity, repository error, oversized stack, and unauthorized state fail closed.
- Lock/unlock, onboarding completion, vault/account reset, and session failure.
- No protected entry or ViewModel/saveable state is composed while locked.

## 6. External-navigation tests

- Every supported raw input and typed intent.
- Malformed/unsupported/overlong/control/traversal/encoded-separator inputs.
- Duplicate and distinct-identical deliveries.
- Rapid replacement/conflation and bounded handled-ID history.
- One observable pending application path; no dual-collector race.
- Auth-gated deferral and onboarding rejection.
- Entity authorization, stale data, cancellation, and repository failure.
- Background/foreground and session replacement.
- Deterministic stack and Back result for every intent.
- Dirty/blocked screen leave policy.

## 7. ViewModel and resource tests

- Entry state survives allowed configuration/tab restoration.
- Popped/reset entries release ViewModels and registrations.
- Re-entering creates the intended fresh or restored instance.
- Sensitive `onCleared` paths wipe state.
- Central lock cleanup reaches application/session-scoped state.
- Effect collectors start before actions that may synchronously emit.
- Stale collectors cannot navigate after leaving composition.
- Gesture/navigation handlers, platform observers, and dispatchers detach without leaks.

## 8. Performance evidence

Measure before and after under equivalent conditions:

- navigation host/AppNavigator/NavDisplay composition or invalidation counts during 1,000+ pointer-move/scroll activity signals;
- current screen recomposition during scrolling;
- interactive Back frame behavior;
- push/pop base duration and post-release settlement at representative progress values;
- retained entries and ViewModels per tab;
- stack size under repeated navigation;
- startup/navigation serialization cost if snapshots are large.

For activity tracking, require pointer/key events to use a non-Compose conflated signal. Assert that navigation-host recompositions do not scale with event count. Report actual counts and test methodology; do not infer performance only from code.

Do not overstate small theoretical differences that are below measurable noise.

When comparing navigation versions, inspect the pinned platform source. Framework defaults are not
UX-equivalent contracts: the verified Navigation Compose 2.9.2 iOS transition is `200 ms`, while
Navigation 3 UI 1.1.1 uses a `500 ms` base. Test interactive drag tracking separately from release
settlement. If an iOS-only public transition override is justified, assert its base duration and
linear predictive-progress specification, and prove Android/Desktop still delegate to their prior
defaults. Do not downgrade the navigation architecture merely to inherit an older default.

## 9. Repository build matrix

Run the repository's actual commands. A typical KMP matrix includes:

```bash
./gradlew detekt verifyDependencies test check
./gradlew :app-android:lintRelease :app-android:verifyReleasePackageContents
xcodebuild -project <project> -scheme <scheme> \
  -configuration Release -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO build
./gradlew :app-desktop:createReleaseDistributable
git diff --check
```

Adjust Android flavor tasks explicitly. Verify Android lint/R8/manifest/DEX namespace/package contents/resources/ABI/native/legal checks when the repo supplies them. Inspect the built iOS app's bundle ID, executable, resources, and linkage. Inspect and launch Desktop packaged runtime when required.

Do not invent credentials or metadata to bypass protected packaging. Report a missing external input as a protected gate, while still running all source-level compilation/tests possible.

Unsigned local validation is not production-signing proof. Do not sign, upload, notarize, publish, or release without authorization.

## 10. iOS physical matrix

Test iPhone and iPad, English/LTR and Arabic/RTL, 60 Hz and 120 Hz hardware when available:

- slow full drag;
- fast flick;
- fast short flick released at low progress, specifically checking the remaining settlement tail;
- short low-velocity swipe;
- cancellation and reversal;
- repeated/rapid gesture and navigation;
- toolbar/programmatic Back;
- opposite edge rejection;
- runtime language switch;
- dirty editor/dialog/local transient state;
- blocked operation;
- lock and background/foreground during gesture;
- view detach/reattach;
- expected tab/history/scroll restoration.

Record device, OS, refresh rate, build identity, direction, expected, actual, and evidence. Automated logical-edge tests do not replace this matrix.

## 11. Android and Desktop manual checks

Android:

- system Back and toolbar match;
- predictive Back preview only for `PopNow`;
- guarded states reveal no prior protected screen;
- task/activity recreation and process restore;
- compact/tablet/foldable layouts share history.

Desktop:

- Escape/Back key-down behavior;
- window resize/adaptive layout;
- close-at-root policy;
- packaged launcher and runtime modules;
- no key-repeat duplicate pops.

## 12. Final sibling-defect review

After tests pass, inspect the complete diff line by line. For every modified subsystem, search sibling paths:

- another adapter still mutates a stack directly;
- another guarded screen forgot registration;
- another command source is unbounded;
- another platform uses an alternate Back path;
- another route is missing ownership/serialization;
- another sensitive ViewModel is singleton scoped;
- another restoration path activates before validation;
- another layout creates separate logical state.

Run `git diff --check` last and state whether the worktree was already dirty.

## Minimum acceptance gate

Navigation is production-ready only when:

- all route/state/back/restoration/external tests pass with zero failures;
- static analysis and dependency verification pass;
- supported platform debug/release compilation and applicable packaging pass;
- no uncontrolled mutation/old engine/private gesture workaround remains;
- protected restoration is proven fail closed;
- pointer activity does not cause navigation-host recomposition scaling;
- Android/Desktop Back paths match policy;
- iOS LTR/RTL physical matrix passes, or the verdict clearly remains conditional on those genuinely physical checks;
- all blocked gates identify the exact missing external input rather than being reported as code success.

## Result format

Report:

- exact tree/commit identity and worktree condition;
- changed files by subsystem;
- intentional behavior changes and deviations;
- commands with exit status, task/test totals, failures/errors/skips;
- artifact identities and hashes where useful;
- before/after performance evidence;
- automated iOS direction/settlement findings;
- exact physical/manual checks remaining;
- newly discovered and fixed sibling defects;
- explicit READY, CONDITIONALLY READY, or NOT READY verdict with reasons.

## Version boundary

The verified reference used Compose Multiplatform 1.11.1, Navigation 3 runtime 1.1.4, JetBrains Navigation 3 UI 1.1.1, Navigation Event runtime 1.1.2, Navigation Event Compose UIKit 1.0.1, Lifecycle 2.11.0, and Koin 4.2.2. Re-run the full matrix and inspect relevant official source after navigation, Compose, lifecycle, DI, Kotlin, Gradle, Xcode, or Android toolchain upgrades.
