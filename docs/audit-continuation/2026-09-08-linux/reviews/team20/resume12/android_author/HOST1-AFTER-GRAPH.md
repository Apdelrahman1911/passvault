# Android clipboard host1 — finite integration after graph evidence

2026-09-12, `/root/c20_android_author`. **CONDITIONAL SOURCE CONTRACT ONLY.**
No allocation, binding, graph observation, Test action or cleanup is admitted.
The purpose is to finish one meaningful host regression, not another framework
fixture or whole Android build.

## Fixed subject and result

- Task: `:app-android:testDebugUnitTest`.
- Include filter:
  `com.passvault.android.security.AndroidClipboardServiceTest.newSensitiveCopyDoesNotInheritAnUnavailablePendingClearOrPriorExpiry`.
- XML: `app-android/build/test-results/testDebugUnitTest/TEST-com.passvault.android.security.AndroidClipboardServiceTest.xml`.
- Exactly **one unsuffixed method**, one suite, one actual Test task; no older
  six methods. Prerequisite task counts are not test counts.
- Test source SHA-256:
  `ea3b535b99390d4850269bcc2089c8f6e26c1096cd17052ef361fae3ba5b3332`.
- Production `AndroidClipboardService.kt` SHA-256:
  `db1b9e5387e7059719e0f49b4a8c9340f8abcf210d16a808a5408c17443e00cf`.
- App `build.gradle.kts` SHA-256:
  `c7a382773cb11383d583b6fd1dcead85aac30ccc0a38f81995f535dc1029da42`.

This test exercises the production ownership/timeout state machine against a
synthetic fake clipboard: unavailable clear of A, recopy B, prior cooperative
expiry release and foreground preserve B, then B's independent expiry clears it.
It does not force an uncancellable stale callback, concurrent scheduler race,
Android `ClipboardManager`, physical clipboard protection or PVA009 closure.
No product/fixture edit is proposed.

## Inputs that must come from the admitted graph, not guesses

The graph receipt supplies exact paths, concrete types, Test/Exec/JavaExec/archive
classification, enabled state and dependency/finalizer/ordering references.
Independently review each observed task's role against frozen build source and
the pinned plugin implementation where necessary. Bind a finite table for host1;
the observed graph is **not itself** an allowlist. Host1 rejects missing, added,
type-changed or edge-changed tasks before actions. Unknown out-of-graph ordering
references and any finalizer need a separate supported disposition, not expansion.

| Observed role | Required disposition before host1 actions |
|---|---|
| Selected unit Test | Exact real `Test`, sole enabled Test, filters/worker/XML policies below. |
| Kotlin/Java compiler or compile-library intermediate | Exact path/type/edges; output only in the fresh copied checkout's reviewed build subtree. Not app/native/package execution. |
| Debug unit resource/manifest preparation | Exact implementation/output role proves no APK/AAB, app assembly, install, signing or distributable. A `package*` spelling alone neither admits nor disproves this role. |
| `prepareAndroidLegalAssets` | Review source-defined `FileSystemOperations.sync` destination and physical no-symlink containment before action; source inputs remain read-only legal files. It deletes only within the positively owned reviewed output root. |
| Other archive task | Exact narrow compile prerequisite and output content/role must be established; no blanket `AbstractArchiveTask` or name exception. |
| Exec/JavaExec, app packaging, minification, signing, install, connected/instrumentation/device, publication or unrelated platform task | Refuse. Do not add an exemption to make a refused graph run. |

For every admitted generating task, bind the allowed output roots and validate
physical no-follow containment under its fresh run before actions. Do not rely
on graph01's lexical output record or use unknown generic outputs to broaden
scope. Existing reviewed plugin/configuration trust is explicit; these listeners
are not a security sandbox against arbitrary configuration code.

## Minimal implementation delta, not a replacement lifecycle

Use a **new independent host1 run** with its own R/E/request and the already
reviewed SDK/read-only-view/private-namespace lifecycle as text. Never rename or
reuse the graph01 instance. Keep strict dependency verification, JDK17, checked-in
wrapper, one Gradle worker, no daemon/parallel/configure-on-demand/cache/rerun
bypass, automatic SDK/JDK acquisition off and one original applicable stop.

1. Replace graph01's deliberate refusal/exit1 protocol with the exact finite
   host action table and normal command-exit0 mapping. Do not remove graph01's
   refusal and run it again. New host1 must not be dry-run and must refuse CLI
   `--tests`, task exclusions, continue-on-failure, alternate selectors and
   provider-mode overrides.
2. Text-reuse only the per-Test worker policy from
   `scripts/audit/focused_regression_01.init.gradle`
   (`e945045a8de9db6b8b773b1a49fade926b6dd55494a4f65cc6bc089816c16a79`):
   exact JDK executable,512MiB heap,maxParallelForks1,forkEvery0,
   ignoreFailures=false and startup system properties for isolated HOME/TMP/JNA.
   Add explicit isolated SQLite temp and Android-user roots for this worker.
   The outer allocates these original worker directories before any invocation;
   the new inner includes them in its original allocation contract.
3. Preserve the clean allowlisted client environment. The worker receives only
   that reviewed environment with private worker HOME/TMP/XDG/SQLite/Android
   roots; remove JVM option variables and all stale provider-mode/root/fixture
   overrides. Do not forward personal HOME/config or signing inputs. Verify
   effective Test executable, heap/fork/environment/startup properties before
   actions; configuration intent alone is not runtime confinement proof.
4. Set only this include pattern through `Test.filter`, require empty excludes
   and command-line includes, failOnNoMatchingTests=true and ignoreFailures=false.
   Keep the repository's existing JUnit engine and Android-resources setting;
   do not disable resources or replace the tested production boundary merely
   to shorten the graph. Runtime inventory and external-device prohibition
   remain active.
5. Reuse the focused result collector's strict XML/type/count rules as text,
   narrowed to this exact single class/method/path; no generic normalization,
   extra XML, stale/cache/no-source/up-to-date substitution or task-count credit.
   Preserve raw XML/log/command/source/result identities outside all deletion
   roots. Require actual executed/didWork Test state,0 failures/errors/skips,
   exact started/complete exit0, original stop0, owned settlement and original
   source-after/evidence/cleanup predicates. A partial XML observation does not
   override an overall failed/held invocation.
6. The independent reviewer scores the new result against the already accepted
   fixture review. Do not run a second mock/assertion suite or repeat six old
   cases for reassurance. Increment case accounting only for actual retained
   outcomes and with their terminal/source qualification.

Only concrete graph-supported task/output guards and final bindings remain to
write after graph01. No speculative Android task list, reusable generic manager,
configuration probe retry or current execution permission is supplied here.
Root remains the only build/CI/Git/cleanup owner. The successful crypto Compile03
and all restrictions/owner-blocked emulator licensing are unchanged.
