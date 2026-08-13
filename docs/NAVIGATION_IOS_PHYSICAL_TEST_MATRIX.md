# iOS navigation physical-device verification

This matrix is intentionally a manual release gate. Simulator compilation and policy/unit tests do
not prove UIKit edge recognition, frame pacing, interruption behavior, or ProMotion motion quality.

Run the matrix on the exact candidate build. Record device model, iOS version, refresh rate, app
language, build number, and result. Repeat it after upgrades to Compose Multiplatform, Navigation 3,
NavigationEvent, Xcode, or iOS.

## Required devices

- one supported iPhone at 60 Hz;
- one ProMotion iPhone at 120 Hz where available;
- one supported iPad in portrait and landscape;
- a real device with the app language set to English;
- a real device with the app language set to Arabic.

## Gesture and direction matrix

For every row, first navigate at least two entries deep. Confirm the screen tracks the finger with
no jump, duplicate pop, wrong-edge response, exposed protected content, or competing animation.

| Scenario | LTR expected result | RTL/Arabic expected result |
| --- | --- | --- |
| Forward push | Completes with the documented 200 ms base toward logical Start | Completes with the same base in the mirrored direction |
| Slow continuous drag past completion threshold | Left-edge drag right tracks and pops once | Right-edge drag left tracks and pops once |
| Fast short flick toward Back | Completes once with library-owned settlement and no 500 ms tail | Completes once in the mirrored direction with no 500 ms tail |
| Short low-velocity swipe | Cancels and returns to the current screen | Cancels and returns to the current screen |
| Reverse direction before release | Cancels naturally; no stack mutation | Cancels naturally; no stack mutation |
| Gesture cancellation/interruption | Current screen is fully restored | Current screen is fully restored |
| Repeated gestures after each completed push | Exactly one pop per completed gesture | Exactly one pop per completed gesture |
| Physical end-edge swipe | Does not navigate | Does not navigate |
| Toolbar/programmatic Back | Pops with the documented 200 ms base | Pops with the same base and correctly mirrored transition |

## Back-policy matrix

- Open a clean credential detail/edit route: interactive Back may begin and pop once.
- Make an editor dirty: an edge gesture must not reveal the prior screen; completion opens/handles
  the local discard policy only.
- Open each confirmation/dialog/search transient state: Back dismisses local state without a pop.
- Start an import/export/backup or other blocked operation: the previous screen must not become
  visible and the stack must not change.
- Trigger Back rapidly from gesture, toolbar, and keyboard/external input: only the first authorized
  mutation applies.
- Lock during an interactive gesture: no protected previous scene may remain visible; unlock is the
  only displayed root after the transition.

## Lifecycle and runtime changes

- Begin a gesture, background the app, then foreground it. Confirm no stale completion or revealed
  protected screen.
- Background while on a nested screen, allow auto-lock, foreground, unlock, and confirm only a
  validated restored destination returns.
- Change English to Arabic at runtime, navigate forward, then verify only the right edge is Back.
- Change Arabic to English at runtime, navigate forward, then verify only the left edge is Back.
- Rotate iPhone/iPad during and between navigation operations and verify stack and layout state.
- Detach/re-attach through a system sheet, picker, or interruption and verify edge input remains
  singular and correctly directed.

## Tab, state, and performance checks

- Build a nested stack in every top-level tab, switch repeatedly, and verify each stack and scroll
  position returns unchanged.
- Reselect the active tab and verify only that tab pops to root.
- Enter a nested destination through every enabled external/notification path and verify Back leads
  to the deterministic owning root.
- Inspect recompositions while scrolling. Pointer moves must not cause `PassVaultNavigationHost`,
  `NavDisplay`, or route adapters to recompose once per event.
- On 120 Hz hardware, use slow drags and fast flicks while watching for dropped frames, delayed
  settlement, discontinuity on release, or a second non-interactive animation.
- Compare release settlement separately from interactive dragging. Drag progress must remain
  finger-linked; after release, completion is bounded by `(1 - progress) * 200 ms` and cancellation
  by `progress * 200 ms` for the pinned Navigation 3 implementation.

## Acceptance record

Navigation is physically accepted only when every applicable row passes on iPhone and iPad in both
directions, no guarded route reveals a previous scene, no repeated input produces a duplicate pop,
and no severe or repeatable frame pacing defect remains. Attach screen recordings for any failure;
do not hide a library regression with private recognizer manipulation or an arbitrary global tween.
