---
name: ios-compose-navigation-rtl
description: "Implement, debug, or review Compose Multiplatform Navigation 3 on iOS, especially library-owned interactive swipe-back, fast-flick settlement, LTR left-edge and Arabic/RTL right-edge behavior, UIKit semantic direction synchronization, runtime language changes, transition ownership, and Back-guard eligibility."
---

# iOS Compose Navigation and RTL

Use supported Compose/Navigation APIs to provide one native-feeling interactive Back path whose edge follows the effective layout direction and whose eligibility obeys application policy.

Also load `$cross-platform-back-policy` whenever a screen can guard or consume Back.

## Stable invariants

1. One component owns interactive progress, completion/cancellation, and settlement. Prefer the navigation library.
2. LTR Back begins at the physical left/start edge; RTL Back begins at the physical right/start edge.
3. Compose `LocalLayoutDirection`, UIKit semantic direction, and the navigation input system agree before and after runtime language changes.
4. A destination that is not `PopNow` cannot visually reveal a previous entry.
5. Slow drag stays one-to-one with finger progress; release settlement is not replaced by an arbitrary global tween.
6. Push, toolbar, and programmatic pop direction match the current layout direction; platform-specific timing does not silently alter Android or Desktop.
7. Only one edge is active for Back unless the product explicitly defines a second edge action.
8. Lock, background, host detachment, and stale callbacks cannot finish an unauthorized pop.

## Preferred implementation

### Host controller

Use a single `ComposeUIViewController` and let Compose UI install its supported start-edge navigation input. Do not wrap it in a second `UINavigationController` merely to obtain swipe-back.

Set the UIKit semantic direction before the view first enters a window:

```kotlin
val controller = ComposeUIViewController(
    configure = {
        opaque = false
        // Keep the supported default start-edge Back input.
        // Leave the end edge disabled unless it has an explicit product role.
    },
    content = { App() },
)
applyNativeLayoutDirection(controller, initialDirection())
```

### Direction synchronization

The app language provider should update both Compose and UIKit:

```kotlin
@Composable
fun ProvideAppLanguage(direction: LayoutDirection, content: @Composable () -> Unit) {
    val controller = LocalUIViewController.current
    SideEffect { applyNativeLayoutDirection(controller, direction) }
    CompositionLocalProvider(LocalLayoutDirection provides direction, content = content)
}
```

A verified compatibility implementation for Compose UI 1.11.x is:

```kotlin
fun applyNativeLayoutDirection(controller: UIViewController, direction: LayoutDirection) {
    val view = controller.view
    val semantic = if (direction == LayoutDirection.Rtl) {
        UISemanticContentAttributeForceRightToLeft
    } else {
        UISemanticContentAttributeForceLeftToRight
    }
    if (view.semanticContentAttribute == semantic) return
    view.semanticContentAttribute = semantic
    if (view.window != null) view.didMoveToWindow()
}
```

Why the conditional refresh exists: in the verified Compose UI version, its public start-edge input derives the physical edge from the host view's effective UIKit direction during window attachment. Refresh only after an actual semantic change and only while attached. This is version-conscious compatibility logic, not a universal UIKit rule. Reinspect official source and retest after every Compose UI/navigation-event upgrade.

Never call `didMoveToWindow()` on every recomposition.

### Back-policy eligibility

For `PopNow`, give `NavDisplay` the full active entry list and let its interactive navigation own progress and settlement. For `HandleInPlace` or `Blocked`, make the previous entry unavailable to interactive rendering and install a consuming Navigation Event handler:

```kotlin
val visibleEntries = if (backDisposition == BackDisposition.PopNow) {
    activeEntries
} else {
    activeEntries.takeLast(1)
}

NavDisplay(
    entries = visibleEntries,
    onBack = { backCoordinator.completeInteractivePop() },
)
```

This is essential when the library decides gesture eligibility by the presence of previous entries.

### Platform transition direction and timing

First inspect and test the pinned library's push, regular-pop, and predictive-pop defaults in both
LTR and RTL. Do not assume Navigation 2 and Navigation 3—or two versions of either—have equivalent
motion merely because both use framework defaults.

The verified dependency comparison found:

- Navigation Compose 2.9.2 iOS defaults: `200 ms` linear transitions;
- Navigation 3 UI 1.1.1 iOS defaults: `500 ms` regular and predictive transition base;
- Compose UI 1.11.1: release velocity selects completion/cancellation, but velocity magnitude is not
  carried into `NavDisplay` to shorten settlement;
- Navigation 3 UI 1.1.1: completion duration is approximately
  `(1 - progress) * transitionDuration`, and cancellation is
  `progress * transitionDuration`.

This can make a short velocity-qualified flick retain nearly 500 ms of motion even though slow
interactive tracking is correct.

If the product accepts the framework default, leave it untouched. If physical comparison proves a
material mismatch, isolate an iOS-only override using the public `NavDisplay` parameters:

```kotlin
NavDisplay(
    entries = visibleEntries,
    transitionSpec = platformTransitionSpec(layoutDirection),
    popTransitionSpec = platformPopTransitionSpec(layoutDirection),
    predictivePopTransitionSpec = platformPredictivePopTransitionSpec(layoutDirection),
    onBack = ::completeAuthorizedPop,
)
```

For the verified PassVault implementation, all three iOS transforms use a `200 ms` base. Regular
push/pop retain Navigation 3's iOS cubic curve. Predictive content uses `LinearEasing`, so seeking
the transition fraction remains one-to-one with finger progress. Navigation 3 still owns
completion, cancellation, and settlement; the application does not launch a post-release
animation.

If regular programmatic/toolbar pop is physically hard-coded while interactive Back is correct,
derive regular motion from `LayoutDirection` and predictive motion from the public swipe-edge
argument. Keep the override platform-only:

```kotlin
actual fun platformPredictivePopTransitionSpec(
    direction: LayoutDirection,
): AnimatedContentTransitionScope<Scene<AppRoute>>.(Int) -> ContentTransform = { edge ->
    val towards = when (edge) {
        NavigationEvent.EDGE_LEFT -> SlideDirection.Right
        NavigationEvent.EDGE_RIGHT -> SlideDirection.Left
        else -> logicalBackDirection(direction)
    }
    predictivePopTransform(towards, durationMillis = 200, easing = LinearEasing)
}
```

Android/Desktop actuals should use their default transition unless their UX independently requires a change.

## Fast flick and settlement diagnosis

When slow dragging tracks correctly but release feels slow or wrong:

1. Identify which layer owns gesture progress.
2. Inspect the pinned official Navigation/Compose source or docs, not private runtime class names.
3. Confirm whether release velocity determines complete/cancel.
4. Confirm whether settlement duration scales with remaining progress.
5. Search the app for custom transition specs, fixed tweens, recognizer delegates, duplicate back callbacks, or two navigation stacks.
6. Separate recomposition/performance issues from direction/settlement ownership.

In the verified Nav3 UI 1.1.1 implementation, interactive progress is library-owned, release
velocity participates only in completion/cancellation, and settlement duration scales with the
remaining fraction of the selected transition. Evaluate drag tracking and post-release settlement
as separate behaviors. A supported transition spec may change the base duration; the application
must not add a second tween or coroutine after release. Revalidate these details after version
changes.

## Cancellation and interruptions

Verify all paths:

- slow short drag returns cleanly;
- direction reversal follows the finger and then cancels/completes correctly;
- system cancellation does not mutate the stack;
- recognizer failure before a valid start causes no pop;
- background/lock/view detachment removes eligibility and invalidates callbacks;
- a completed interactive event pops exactly once;
- a stale completion after toolbar/programmatic Back is rejected;
- repeated gestures do not retain a handler, recognizer, or coroutine.

Do not infer `.failed` behavior from enum names alone; exercise it on the pinned library/hardware if automatable APIs do not expose it.

## Forbidden anti-patterns

- Finding recognizers by private Compose class name.
- Depending on private recognizer ordering or changing delegates behind the library.
- Installing a competing custom edge pan over the supported navigation event input.
- Adding an application-owned post-release tween or changing every platform globally to compensate for an iOS fast-flick symptom.
- Downgrading to an older navigation architecture solely because its framework default happens to be shorter.
- Using a second UIKit navigation stack around the Compose stack without a deliberate architecture.
- Enabling both physical edges when only logical start should go Back.
- Updating Compose direction but leaving UIKit semantic direction stale.
- Refreshing window attachment on every recomposition.
- Checking a Back guard only after the previous protected entry was revealed.
- Allowing toolbar Back and swipe Back to use different policies or directions.

## Automated checks

- Logical edge maps LTR to left and RTL to right.
- Changing app language updates the direction policy.
- Non-`PopNow` entry projection exposes only the current entry.
- Interactive completion invokes one guarded navigator mutation.
- Stale/duplicate completion cannot pop twice.
- Lock/background/host inactive state rejects completion.
- Regular pop uses the expected logical edge without changing other platform transitions.
- The iOS push/pop/predictive specs share the documented base duration.
- Predictive transition content uses linear progress; regular transitions retain the selected curve.
- Android and Desktop continue using their prior defaults unless independently justified.
- No private recognizer names or custom fixed-duration navigation tween remain in source.
- Navigation-host recomposition does not scale with pointer movement.

## Physical-device matrix

Run on iPhone and iPad, and on 120 Hz hardware when available, in English/LTR and Arabic/RTL:

- slow full drag;
- fast short flick with sufficient velocity;
- short low-velocity release/cancel;
- reverse direction mid-gesture;
- repeated Back gestures;
- toolbar Back after gesture cancellation;
- fast gesture immediately after push;
- runtime language switch without reinstall;
- dirty editor and visible confirmation dialog;
- blocked import/export/save operation;
- lock during gesture;
- background/foreground during or after gesture;
- host/view detach/reattach;
- correct physical edge and rejection of the opposite edge;
- expected prior screen, tab history, and scroll state after completion.

Record OS, device, refresh rate, dependency versions, direction, result, and any video/screen capture reference.

## Acceptance criteria

- Slow drag is continuously finger-linked.
- A valid fast flick settles quickly and naturally without a second animation.
- Cancellation/reversal returns to exactly the current screen.
- LTR uses only the left/start edge; RTL uses only the right/start edge.
- Toolbar/programmatic pop animates from the correct logical direction.
- Guarded/blocked screens never reveal the previous entry.
- Completion mutates the stack once.
- No private UIKit/Compose implementation-detail manipulation exists.
- Automated checks pass and the full physical matrix has no blocker.

## Version boundary

Verified against Compose Multiplatform 1.11.1, JetBrains Navigation 3 UI 1.1.1, Navigation 3 runtime 1.1.4, Navigation Event runtime 1.1.2, and JetBrains Navigation Event Compose UIKit 1.0.1. PassVault selected a `200 ms` iOS-only base after comparing the pinned Navigation 3 `500 ms` default with Navigation Compose 2.9.2's `200 ms` default. Gesture installation, release thresholds, default durations, settlement calculation, transition curves, regular-pop direction, and the `didMoveToWindow()` refresh are version-specific. Reinspect official source and repeat the entire iOS matrix after upgrades.
