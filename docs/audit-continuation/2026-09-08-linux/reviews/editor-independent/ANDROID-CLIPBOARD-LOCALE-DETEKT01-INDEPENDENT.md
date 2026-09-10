# Android clipboard/locale Detekt01 delta — independent source review

Reviewer `/root/editor_review`, 2026-09-10; source author `/root/android32`.
**Accept exact source decomposition for root adoption. No functional blocker found in this
delta. Not a Detekt pass, Android case execution, fixture admission or finding closure.**

## Bound inputs and independent check

B = `docs/audit-continuation/2026-09-08-linux`.
Target = `app-android/src/androidTest/kotlin/com/passvault/android/audit/AndroidClipboardLocaleInstrumentationTest.kt`.

| Input | SHA256 | Bytes/LF |
|---|---|---|
| Before, independently recovered in memory | `3762edc0f9ac2d26ba9ab68249a55004fd4cb3f08e01a2d337d23d3fbb1aab1f` | 28533/504 |
| Current after source | `580c71e5fc5c917b7f62535282fa3436a3eada15e35ac7afa3bcd584f890c29d` | 29184/529 |
| B/reviews/android32/detekt01/ANDROID-CLIPBOARD-LOCALE-DETEKT01.patch.txt | `8517df83cd9d45cfac06a30d49ad6ba038e523c6df53b28f15bcdd247cef5c98` | 11967/220 |
| Same directory ANDROID-CLIPBOARD-LOCALE-DETEKT01-DELTA.md | `ae515b86fa23c4f6546d7079f78fd3520d5e022f9f4fbcb4e21f7c6fcf4abe2b` | 5787/86 |
| B/runs/linux-detekt01/logs/detekt.log | `13c30804b78f425f8ec21b204ed117ba3143990f0b99a232b073bde1342e4218` | 71728/287 |

Read the full inert six-hunk diff and author note; validated every hunk against after bytes,
reconstructed before solely in memory and independently matched its exact hash. No Git,
T/store, deleted-runtime file or helper was used. After35–194/332–428/471–529 was read for
surrounding ownership, exception and terminal contracts. Hashes are not whole-file audit credit.

Retained actual log78–84 has **seven diagnostics, not seven test cases**: ComplexCondition
at old91/167/347, CyclomaticComplexMethod at103/325, LongMethod at103 and
ThrowingExceptionFromFinally at353. The source addresses those particular shapes through
deny/inventory/predicate extraction, setup/fixed-loop/readiness decomposition and final
rethrow relocation. It adds no Detekt suppression; compiler DEPRECATION follows recycle().
Actual diagnostics remain failed evidence until a separately admitted root-owned check.

## Independent behavior challenge

- **Admission and inventory:** null arguments still fail before helpers; isolation value,
  unknown keys and optional inert output metadata retain their ordered short-circuit checks.
  Optional-key presence and <=4096 string requirement remain. The harmless set-allocation
  difference for null arguments is explicit, not an authorization change. The new inventory
  helper preserves started!=2 || passed!=2; problem/interruption checks remain first.
- **Case/cleanup boundaries:** setup and fixed-case loop are moved without new dispatch,
  suspension, catch or recovery boundaries. Debug/API/package guards and locale capture occur
  in the same sequence; existing try covers both helpers. Two names/bodies, start/pass counts,
  status values,90s case bounds,30s cleanup bound and phase/error reporting are preserved.
  onStart still aggregates cleanup failure, observes terminal interruption, fails incomplete
  inventory and restores interruption after reporting. Cleanup and following source are unchanged.
- **AX ordering:** focused Main precedes root acquisition. Each visited node retains package/
  window and128-node/20-depth/32-child guards before text/children. Heading getters precede
  button getters, invisible nodes still avoid text, and clickable/enabled short-circuit order
  is unchanged. Per-attempt counters, DFS child order, child-before-parent recycling, ambiguity
  rejection and conditional final focus check are preserved. No readiness fallback/input or
  new attempt is added.
- **Exception/recycle precedence:** manual source-path analysis, not four executed tests:
  read+recycle success returns normally; read P alone rethrows the same P; recycle R alone
  throws the same R; both rethrow P with R suppressed. mergeFailure516–518 guards identical
  objects, so self-suppression is still avoided. The new helper holds P until finally completes,
  then throws outside finally; recursive unwinding preserves nested suppression order and
  performs no new interrupt mutation. Additional private stack frames are an intended mechanical
  difference, not a change to reported phase/type or failure-object identity.

No stronger cleanup guarantee follows: root.windowId still occurs before visit/recycle as
before, and native/framework stalls and AX-connection settlement still need the external
owner. This review neither fixes nor newly confirms a failure at those unchanged boundaries.
Author's documented initial text-extraction mismatch remains a source-data comparison issue,
not a test failure hidden by a runtime retry.

## Disposition

PVA-009/PVA-030 and all prior Android/physical-device limits stay unchanged. The two existing
fixed cases were not run here. No extra test loop is requested for this mechanical refactor;
no compile/Detekt success, execution permission or closure credit is manufactured.
Zero new cases, executed tasks, findings, product fixes or denominator changes from this review.

Only static readers and this sealed note; no source edits, helper execution/import, builds,
Git/network, runtime/process/SDK/cache probes or background workers. No temporary/generated
runtime outputs created. Preserve T/consumed Detekt01 inputs and all STOP/NO-RETRY/CLOSED/
HOLD/consumed scopes including PVU-007/PVU-011/PVA-029 and G7/G8. Root remains sole admission,
build, cleanup and publication owner; all non-publishing/PVD/hardware restrictions remain.
