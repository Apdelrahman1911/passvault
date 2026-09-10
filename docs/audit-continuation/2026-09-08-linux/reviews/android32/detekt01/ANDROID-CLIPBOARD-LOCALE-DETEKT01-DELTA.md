# Android clipboard/locale fixture — Detekt01 decomposition delta

Author: `/root/android32`; independent challenger: `/root/editor_review`.
Root expressly released **only this W module file** after Detekt01 terminal exit70,
then authorized this inert reversible diff and note. T/store, bound Detekt scripts,
init, manifest/request and all other module inputs were untouched.
**Zero new builds, Detekt invocations, compilations, Android/SDK/device operations or
runtime probes by this lane.** This is not a claim the seven diagnostics now pass.

## Bound before, after and retained actual diagnostics

Target: `app-android/src/androidTest/kotlin/com/passvault/android/audit/AndroidClipboardLocaleInstrumentationTest.kt`.

| Artifact | SHA-256 | Bytes / LF |
|---|---|---|
| Before, preserved reversibly by the inert diff | `3762edc0f9ac2d26ba9ab68249a55004fd4cb3f08e01a2d337d23d3fbb1aab1f` | 28,533 / 504 |
| Frozen after candidate | `580c71e5fc5c917b7f62535282fa3436a3eada15e35ac7afa3bcd584f890c29d` | 29,184 / 529 |
| `ANDROID-CLIPBOARD-LOCALE-DETEKT01.patch.txt` in this directory | `8517df83cd9d45cfac06a30d49ad6ba038e523c6df53b28f15bcdd247cef5c98` | 11,967 / 220 |
| `runs/linux-detekt01/logs/detekt.log` under this continuation | `13c30804b78f425f8ec21b204ed117ba3143990f0b99a232b073bde1342e4218` | 71,728 / 287 |

The six-hunk diff is +96/-71 source LF (net +25 LF/+651 bytes), generated as inert
text from the exact pre-edit tool-memory snapshot and current file, not from T or Git.
It retains the previously accepted before bytes; existing author/reviewer evidence
for that source remains unchanged. Seven real diagnostics at old locations remain:

| Old diagnostic | After decomposition |
|---|---|
| ComplexCondition:91, arguments | `onCreate`:88–96; deny predicates:98–106 |
| ComplexCondition:167, terminal inventory | `onStart`:137; incomplete inventory helper:190 |
| ComplexCondition:347, visible enabled start button | visible-text/button helpers:372–376 |
| CyclomaticComplexMethod:103; LongMethod:103 | `onStart`:108–145; unchanged setup:147–170; fixed loop:172–188 |
| CyclomaticComplexMethod:325, onboarding readiness | acquisition:339–349; tree:351–370; small predicate/recycle helpers |
| ThrowingExceptionFromFinally:353 | node read/recycle:379–385, same final primary thrown **after** finally |

## Behavior/ordering challenge

Argument checks still short-circuit: null → isolation value → unknown keys → optional
inert output metadata. Metadata is neither opened nor written; null/wrong-type/over4096
values remain refused when its key is present. No accepted argument/key/value changed.
The allowed-key set is now allocated only for non-null arguments; this has no admission
or external-side-effect change. Setup and the entire fixed-case loop were moved with
indentation only. `onStart` retains its original catch/finally, cleanup-error aggregation,
failure-stage assignment, terminal interrupt observation/reporting/restoration and
2/2 inventory requirement. No timeout, case, receipt, diagnostic string or runtime
qualification was changed.

AX acquisition still first requires focused Main. Each node's package/window guard and
128-node/20-depth/32-child bound precede text or child traversal. Heading then button
getters retain their original short-circuit order; there is no new text read when an
invisible node previously avoided one. Children are traversed in the same order, with
child recycling before parent recycling. Ambiguity checks precede the final focused-
Main observation. No fallback, input, extra bootstrap/readiness attempt or AX connection
release claim was introduced.

The node helper now holds the primary Throwable until recycling finishes and rethrows
after `finally`, rather than rethrowing in `catch`/`finally`. Source-level precedence:

| Read outcome | Recycle outcome | Result before and after |
|---|---|---|
| Success | Success | Normal return |
| Failure P | Success | Throw the same P |
| Success | Failure R | Throw the same R |
| Failure P | Failure R | Throw the same P with R suppressed; identical-object self-suppression remains guarded |

This preserves nested primary/suppressed ordering and does not clear or restore any
interrupt in the helper. Existing outer interruption handling remains byte-identical.
Extra private call frames are expected; source identity/line numbers change, not the
reported phase/class fields or failure object identity. Existing compiler-only
`@Suppress("DEPRECATION")` follows `recycle()` to its helper; **no Detekt suppression or
rule/config/dependency change** was added.

Text-only comparisons (not executed tests) confirmed: header/fields/case inventory;
dedented setup and fixed loop; `onStart` except the declared helper substitutions;
`newActivity`, both actual case bodies and intervening framework helpers; and cleanup
through EOF are unchanged. Quoted-token multiset is unchanged. An initial unanchored
catch-marker comparison falsely reported loop/wrapper inequality; exact LF anchoring
corrected that extraction error. No source change or runtime retry followed that false
comparison. The independent reviewer must separately verify the reversible delta and
these semantic claims; author text comparisons are not independent execution evidence.

PVA-009/PVA-030 retain their target-runtime gaps and unchanged evidence scope. Detekt
diagnostics are preserved as failed evidence until a fresh root-owned admitted check
exists; no automatic retry, target Android success or closure credit follows this edit.
Only the one authorized module source and these small permanent review artifacts were
written. No generated/runtime outputs, caches, background processes or cleanup task
were created. All STOP/NO-RETRY/CLOSED/held boundaries remain unchanged.
