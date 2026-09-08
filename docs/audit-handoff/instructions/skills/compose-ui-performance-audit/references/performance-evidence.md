# Compose performance evidence template

## Reproduction contract

Record:

- exact commit/tree and build type;
- target/device, OS, CPU, refresh rate, and power/thermal state;
- Compose/compiler/navigation versions;
- interaction script and duration;
- inspector/profiler configuration;
- warm-up and number of repetitions.

## Before/after table

| Metric | Before | After | Acceptance bound |
|---|---:|---:|---:|
| Input events | | | Same workload |
| Root/navigation recompositions | | | Does not scale with event count |
| Target leaf recompositions | | | Explain expected state changes |
| Slow/frozen frames | | | Project-defined release threshold |
| P50/P95/P99 frame time | | | Refresh-rate appropriate |
| Peak allocation / GC pauses | | | No regression |
| Active collectors/jobs after exit | | | Zero obsolete owners |

Capture counts with a stable instrumentation boundary. A root may be re-entered without every descendant recomposing; report precisely what the tool measures.

## Activity/timer stress protocol

1. Start one unlocked session and collector.
2. Emit at least 10,000–100,000 activity events faster than consumption.
3. Prove bounded/conflated delivery and at most one scheduled timeout.
4. Advance virtual time around the exact timeout boundary.
5. Change session, lock, background, foreground, retry a failed lock, and emit stale events.
6. Assert no old session extends or locks the new session and no job remains after teardown.

## Navigation gesture protocol

Measure interactive drag and post-release settlement independently. Run slow drag, fast flick, short/cancelled swipe, reversal, repeated gestures, button back, and programmatic back in LTR and RTL. A duration change that fixes button back may leave gesture settlement or direction wrong.

## Interpretation cautions

- Debug builds and Layout Inspector add overhead.
- Recomposition count alone does not prove dropped frames.
- Stable annotations can hide stale UI if the contract is false.
- Sampling profiles can miss short allocation spikes.
- Simulator performance does not replace physical high-refresh-rate verification.
