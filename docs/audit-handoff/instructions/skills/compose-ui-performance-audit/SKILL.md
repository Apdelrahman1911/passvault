---
name: compose-ui-performance-audit
description: Diagnose and remediate Compose or Compose Multiplatform performance defects involving excessive recomposition, high-frequency input, unstable state, scrolling, navigation hosts, effects, retained UI, or lifecycle-driven work. Use when Layout Inspector shows unexpected recompositions, scrolling or gestures stutter, a state/effect key changes per input event, or a UI performance change needs reproducible evidence.
---

# Compose UI Performance Audit

Prove the invalidation path before optimizing it. Preserve behavior while moving high-frequency signals out of the composition graph.

## Core invariants

1. UI state represents values that affect rendering; it is not an event bus.
2. Pointer moves, key repeats, sensor updates, activity heartbeats, and native callbacks must not invalidate broad composition roots unless pixels depend on each event.
3. Event delivery must be bounded or conflated so producers cannot create an unbounded backlog.
4. Long-lived collectors have explicit owners and cancellation boundaries.
5. Performance work must preserve lifecycle, security, navigation, and accessibility semantics.
6. Report measured or instrumented evidence, not only a subjective improvement.

## Investigation workflow

### 1. Establish a reproducible baseline

Choose one interaction and record:

- device, refresh rate, build type, compiler/runtime versions, and tracing tools;
- recomposition counts for the affected screen and its major ancestors;
- frame timing, skipped frames, allocation/GC activity, and CPU where available;
- expected state changes versus pointer/key/native event counts.

Disable inspector-only overlays when measuring final frame behavior. Treat counts as diagnostic evidence, not an absolute quality score.

### 2. Trace invalidation to its owner

Inspect:

- `mutableStateOf`, `StateFlow.collectAsState`, snapshots, derived state, and composition locals;
- `LaunchedEffect`, `DisposableEffect`, `remember`, and `rememberSaveable` keys;
- unstable parameters, recreated collections/lambdas, and broad state holders;
- pointer/nested-scroll/key modifiers and platform callback adapters;
- navigation hosts or root composables that read rapidly changing state;
- lifecycle collectors and timers restarted by event-derived keys.

Use [`scripts/scan_compose_events.sh`](scripts/scan_compose_events.sh) as a lead generator, then verify every result manually.

### 3. Classify the signal

- **Render state:** collect in Compose at the narrowest consumer.
- **Derived render state:** use `derivedStateOf` only when it reduces meaningful invalidations.
- **One-shot UI event:** deliver to the active UI owner with explicit consumption semantics.
- **Domain/activity signal:** keep outside Compose in a bounded/conflated channel or flow.
- **Lifecycle state:** model explicitly; do not infer it from incidental recomposition.

Do not hide a bad state boundary with blanket `@Stable`, equality tricks, or global throttling.

## Remediation patterns

### High-frequency activity signals

Use a non-Compose signal object with capacity one or equivalent conflation. The consumer resets a single timer from the newest activity and never queues one job per event.

```kotlin
class UserActivitySignal {
    private val events = Channel<Unit>(capacity = Channel.CONFLATED)
    val flow: Flow<Unit> = events.receiveAsFlow()

    fun record() {
        events.trySend(Unit)
    }
}
```

Keep timeout/session logic in a separately testable state machine. Scope its collector to the owning session or application service, cancel it on teardown, reject stale session callbacks, and define exactly what background/foreground transitions do.

### Scroll and layout state

- Hoist only state that another owner needs.
- Read rapidly changing scroll state in drawing/layout or `snapshotFlow` when composition does not need every value.
- Use stable item keys and content types for lazy containers.
- Avoid allocating transformed collections, formatters, or controllers in item composition.
- Connect collapsing app bars through supported nested-scroll state consistently across screens.

### Navigation and platform gestures

Do not assume navigation animation stutter and recomposition share one cause. Measure them separately. Interactive gesture progress should remain owned by the navigation/gesture implementation; application state should expose eligibility and outcomes, not mirror every pixel of progress through a root composable.

## Required tests

- Send at least tens of thousands of activity events and prove bounded consumption, one active collector, correct timeout reset, and no stale-session lock.
- Test lifecycle pause/resume, lock/unlock, cancellation, retry, failure, and rapid repeated activity.
- Add a composition-count regression harness around the narrowest stable boundary practical for the project.
- Exercise long lists, nested scroll, state restoration, and navigation transitions in profile/release-like builds.
- Verify Android, iOS, and Desktop separately when platform input or rendering differs.

Read [`references/performance-evidence.md`](references/performance-evidence.md) for a repeatable evidence template.

## Acceptance gates

- Normal scrolling no longer makes root/navigation recompositions scale with pointer-event count.
- No unbounded queue, orphan coroutine, duplicate timer, or stale event survives its owner.
- Rendered behavior and auto-lock/security timing remain correct at boundaries.
- Frame and recomposition evidence includes before/after conditions and limitations.
- Any physical-device-only verification is listed explicitly rather than inferred from host tests.

## Failure patterns

- Using a Compose counter as a `LaunchedEffect` key for each pointer move.
- Replacing an event backlog with lossy behavior without documenting why conflation is correct.
- Measuring debug + inspector overhead and presenting it as production timing.
- Declaring success because leaf composables recompose less while a root host still invalidates.
- Globally changing animation duration to mask an interactive-gesture settlement problem.
