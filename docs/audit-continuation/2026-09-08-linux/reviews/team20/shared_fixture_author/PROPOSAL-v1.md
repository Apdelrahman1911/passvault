# Shared fixture Detekt03 proposal — v1, UNAPPLIED

**PROPOSED ONLY; independent acceptance and root freeze release pending.**
No application/test/helper bytes changed. C17/GUI03 and all STOP/NO-RETRY/HOLD
boundaries remain intact. This is not a Detekt pass or application verification.

## Exact inputs and proposal

All paths below are relative to W. Frozen C17 is
`0563e31adc9a66aefc3e74b99b9a24d17bdcdd49` /
`d1bd6ca5b18d08ff3ff15896d9a78af9016be799` (retained source-before/after identity;
no new Git operation). The retained shared report is
`runs/linux-detekt03/reports/shared-checkstyle.xml` relative to B, SHA256
`3057beffcffb93e21bbc1a7e706cd364812758b9c71d7fbe4b8b45de9de8e5fb`:
**37 diagnostics = Rendering13 + Room15 + BackFreshness9**, not test cases.

Patch: `SHARED-FIXTURE-DETEKT03-v1.patch.txt`, SHA256 `843430ffbb9ff85c9f2a8e772f8e7e5123306af85abf9fe61e8d08da74913146` (9601 bytes).

| Frozen source | Before SHA256 | Proposed after-image SHA256 |
|---|---|---|
| `shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRenderingTest.kt` | `6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a` | `4e6fec750f0f995d459128111c13aa002d7c033980673ea7788742a6bf2d899c` |
| `shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRoomIntegrationTest.kt` | `291c47edcca2de797d1a669947a2ded246ec5b32c120feec8843a2d41ec9ef57` | `109a0534da3023b7cc1f951ba6c4f8b57f8e0e78f718bef765f7d05cc3223417` |
| `shared/src/desktopTest/kotlin/com/passvault/shared/navigation/CredentialBackFreshnessCompositionTest.kt` | `8df5c4f32ab2002bc602c6963f0ac1f86bfb1ea2518f5f3a9cda15e981bd75d0` | `f0614a75dfe0109e0effbd06e1903d1812d6be7c0fd9a97ef34a61ec4d6d42c3` |

## Scope and precise rule dispositions

Two minimal ReturnCount corrections (Rendering470, Room533): initialize
`ancestor` from the unique title's nullable parent rather than return early for
an absent title. `unique` still executes and rejects duplicates; no-title and
parentless-title still return null. The same first ancestor with exactly two
editable named fields is examined, and an absent/ambiguous Add never falls
through to a higher ancestor. The candidate loop, tree reads and all assertions
are otherwise byte-identical.

The other **35 diagnostics request narrow intentional-boundary exemptions**,
not a blanket rule change or repaired runtime behavior. Every annotation is
on the smallest relevant existing function/local function or the two private
Editor owner classes; no file annotation, baseline, exclusion or config change.
The independent reviewer must accept each rationale below before integration.
Original line numbers refer to the frozen source, not the proposed after-image.

| Source / exact symbol | Existing diagnostics | Justification |
|---|---|---|
| Rendering `withEditor` | TooGenericExceptionCaught201 | Preserve any primary assertion/native/setup/cancellation failure while always invoking owner teardown. Catching only Exception can let a cleanup error replace an AssertionError. |
| Rendering `Editor` | TooManyFunctions214 | One private form/native driver owns its input state, VM/window resources and ordered cleanup. Splitting public fixture operations merely to reach11 would fragment ownership. |
| Rendering `Editor.start` | LongMethod230 | Keep prerequisite/evidence admission before registered VM/window acquisition, then actual frame/native readiness, as one67-line ordered boundary; no redundant helper just for60. |
| Rendering `Editor.withKey` | TooGenericExceptionCaught366,373; ThrowingExceptionFromFinally375 | Register before native press, always release, preserve primary plus suppressed secondary error, and fail on release-only error. Assertion/native Error paths must remain covered. |
| Rendering `Editor.click` | TooGenericExceptionCaught400,407; ThrowingExceptionFromFinally409 | Same partial-acquisition/release-only/primary-error contract for mouse input. Geometry/freshness/enabled guards remain unchanged. |
| Rendering `Editor.close` / its local `release` | CyclomaticComplexMethod581 / TooGenericExceptionCaught585 | One explicit owned-resource cancellation/settlement sequence; each failure must not skip later owners. Interruption detection/restoration and primary/suppressed propagation remain intact. Generic-catch exemption is local to `release`. |
| Rendering `onEdt` | TooGenericExceptionCaught682 | Any failed bounded wait must cancel(false) a queued action before propagation, without interrupting an already-running EDT action. |
| Room existing test method | TooGenericExceptionCaught134 | Preserve the actual body/setup assertion or native failure while still closing its exact owner. No test method/body/oracle added, removed or weakened. |
| Room `Editor` | TooManyFunctions218 | One private real-Room/native driver owns the registered bootstrap generations, VM/window/input state and their teardown. No artificial security-owner split. |
| Room `Editor.closeRoom` | CyclomaticComplexMethod394; TooGenericExceptionCaught401,412 | Preserve lockAttempted/closeAttempted-before-attempt, lock failure still reaching close, VM-isCompleted guard, distinct-error suppression, and interrupt restoration. No automatic retry or success-flag relaxation. |
| Room `Editor.withKey` | TooGenericExceptionCaught450,453; ThrowingExceptionFromFinally453 | Registered key release on every exit; propagate release-only failure or attach secondary failure to the existing primary. |
| Room `Editor.click` | TooGenericExceptionCaught476,479; ThrowingExceptionFromFinally479 | Same registered mouse-release boundary; no native target/input changes. |
| Room `Editor.close` / its local `release` | CyclomaticComplexMethod622 / TooGenericExceptionCaught626 | Keep cancel-all then separately bounded join, reverse Room close, closed/window assertions, sensitive references clearing and interrupt/error order. Generic-catch exemption is local to `release`. |
| Room `onEdt` | TooGenericExceptionCaught709 | Preserve cancel(false) for every failed wait as in Rendering. |
| Back `withEditorComposition` | LongMethod181; ThrowsCount181; TooGenericExceptionCaught234,240,245,251; ThrowingExceptionFromFinally242,253 | Nested Main override/restoration and fixture setup/teardown form one failure-preserving boundary. The four throws rethrow primary or surface cleanup-only failure, not unrelated exceptional exits. Main reset must remain outside fixture cleanup and run after any body/setup/cleanup failure. |
| Back `closeFixture.release` | TooGenericExceptionCaught270 | NonCancellable cleanup must retain even assertion/cancellation failures while attempting each remaining independent cancellation/join/reset. |

## Unchanged oracles and limitations

- Rendering: all3 existing cases retain native input/callback counts, pending
  draft page Save, row Save/Cancel, deep-copy repository/new VM reload, capacity
  transitions, retained composed input and bounded synthetic screenshots.
- Room: all1 existing case retains real Desktop crypto/encrypted repositories,
  fresh admitted home, registered bootstrap before work, 49 surviving tuples,
  field50 pending draft with secret flag, page Save, form detach, old VM clear/
  join, vault lock/checkpoint/close, new bootstrap/repositories/VM, same fileKey,
  fresh Room instance, exact50 tuples, capacity and rendered masking checks.
- Back: all7 existing cases retain the pre-frame no-drain timing, production
  registration/navigator, Snapshot/frame publication order, Back/forward/save
  freshness oracles and explicit disposal. NonCancellable per-owner cleanup
  remains before outer Main reset. No UI/native/device evidence is inferred.
- No extra helper/test declarations. No touched API, ownership, timeout,
  callback, seed, sentinel, assertion, sensitive-data clearing or interrupt logic.
  The annotation-only error paths preserve current behavior; this proposal does
  not certify all possible primary/suppression/native-failure combinations.

## Data-only checks performed and smallest useful future validation

Standard-library source/data comparison only, not compilation or project helper
execution: source SHA256 pins matched before and after proposal generation;
exact replacement counts and inverse reconstruction matched; existing named
methods/@Test counts and assertion-starting source lines were unchanged; all
new source lines have no trailing whitespace and are at most120 characters.

| Fixture | Existing @Test count (unchanged) | Assertion-starting lines (unchanged) |
|---|---:|---:|
| `CredentialEditorRenderingTest.kt` | 3 | 72 |
| `CredentialEditorRoomIntegrationTest.kt` | 1 | 73 |
| `CredentialBackFreshnessCompositionTest.kt` | 7 | 51 |

After root explicitly reconciles/releases GUI03/C17 freeze and adopts an
independently accepted exact patch, the smallest diagnostic check for this lane
is **`:shared:detekt`**, retaining its existing required
`:verifyStaticAnalysisCoverage` dependency and all three report formats. The
planned focused static04 can combine the five affected modules serially; this
lane gives no reason to repeat all22 analyzers or unchanged modules that passed.
No root aggregate Detekt or new runtime test is requested solely for annotations.
The two equivalent title-null rewrites require independent source challenge;
if root needs behavioral confirmation, scope it to existing relevant editor
cases in a newly admitted request, never replay consumed GUI02/03 by implication.
The actual Room GUI03 case remains governed by its frozen C17 request, not this
proposal. No build/test/runtime pass is claimed here.

## Procedural boundary note

An initial instruction-discovery find unintentionally listed AGENTS/handoff
path names in sibling held-runtime/publication directories. No file contents
there were read, executed or modified; root was informed and no listed sibling path was followed.
All subsequent reads were bounded W source/retained evidence, and all writes
are only in this proposal directory. No Git, build/test/CI, project-helper
execution/import, process/SDK probe, runtime-content read, central-ledger edit,
source edit, cleanup or Telegram duplication occurred; the path-listing exception
is explicitly recorded above.
