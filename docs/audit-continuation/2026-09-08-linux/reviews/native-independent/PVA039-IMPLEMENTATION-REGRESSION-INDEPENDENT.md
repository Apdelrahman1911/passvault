# PVA-039 — independent implementation and regression source review

Reviewer: /root/native_review, independent of /root/editor; 2026-09-10.

## Disposition

**Accept the narrow production repair and prospective regression source after two requested test-hardening corrections.**
This is source acceptance, not execution/cleanup admission, an observed passing case, or qualified closure.
The author did not independently verify their own fix. Root owns grouping, ledgers, execution and publication.

The separate frozen PVA039-GROUPING-INDEPENDENT.md (SHA-256
a67270f1619e7ecb964f16c644db211ffe5731be03317157960859a599fd6456; 4,531 bytes / 52 LF)
accepts a distinct PVA-039 normal-Compose-exit bypass, not PVA-010 native-context accounting and not
resolution of original PVU-006's singleton/late-operation suspicion. This report adds no closure credit.

## Exact reviewed source identity

W = /root/projects/PassVault/passvault-linux. W is an editing workspace with a retired Git store;
no Git object, HEAD, tree, commit or clean-worktree claim is made. Root must bind publication and any run
to the admitted full source/classpath identity. SHA-256 pins below are exact file bytes, not a Git tree.

| File (relative to W) | SHA-256 | Bytes / LF |
| --- | --- | --- |
| app-desktop/src/desktopMain/kotlin/com/passvault/desktop/Main.kt | 9a67a8a5c2f05331e91ec5c18f6317a3562bd26e2c720105a8a21773edd8ae93 | 7,614 / 253 |
| app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopApplicationLoop.kt | fc8c553ee02f6c84fc535ea0672436b9688dde6753101f9d952858a05516bfac | 402 / 10 |
| app-desktop/src/desktopTest/kotlin/com/passvault/desktop/DesktopApplicationLifecycleIntegrationTest.kt | 422cbfd7b85a08585b99519b94f41296ed24ea1dce133f47f4fdab2ddf5a85b9 | 9,659 / 201 |
| docs/audit-continuation/2026-09-08-linux/reviews/editor/DESKTOP-APPLICATION-LIFECYCLE-SOURCE-NOTE.md | 13d28f9b1f4f3a56640d5f15e81a35bd6a36c6f7f1c768a015c1dd89e7d4f584 | 13,974 / 187 |

The independently reviewed original test iteration was
ad91994b7c96c948b7289c7d3c7b009ed896926cc8a54ea6308780bfdaa92d3f (8,877 bytes / 189 LF).
The initial author note was
9b5f34997967f1c0d1d0eac4cd43ebb88006ab93d76cb9b8ae31f2854ca03dbe (11,772 bytes / 159 LF).
These iterations are review history only; the table is the current source tuple.

## Production reachability, guards and compatibility

1. The existing Main has one production launch call. Its only patch changes remove the Compose
   application import and call runDesktopApplicationLoop instead (Main line 117). An independent inert
   inverse of exactly those two changes reconstructs prepatch SHA-256
   9457f71fefe63de14ddd80d91b14b5b281175ee7c9bcdb113ac6a8ae15452616.
2. The new ten-line helper calls real application(exitProcessOnExit = false, content = content); it
   does not copy the shutdown coordinator or supply fake exit behavior. The recorded Compose 1.11.1
   source explicitly documents false for code after application, and its default true path calls real
   exitProcess(0) after awaitApplication. That capture's source-coordinate association is not independent
   source-to-binary equivalence. The prospective negative/positive child test is therefore important.
3. Main lines 54–68 already place requestShutdown and finishDesktopRuntime in finally, followed by
   the owned instance-lock release and outer process exit. Returning from real Compose now permits this
   normal-exit route. The coordinator, window close route, Compose disposal, Koin ownership, timeout
   constant and Main's outer System.exit(exitCode) are otherwise unchanged by this patch.
4. In finishDesktopRuntime (127–140), awaitCleanup still receives 2,500 ms. Its incomplete branch
   records fail-closed termination and returns without cancelling scope/stopping Koin; completed cleanup
   retains cancellation/stopKoin. That is a latch wait bound, NOT proof of a globally bounded finalizer,
   bounded close operations, bounded shutdown hooks, or guaranteed process death within 2.5 seconds.
5. Existing cleanup jobs can finish before the old Compose exit. Compose still disposes its composition
   and detaches window/tray/protection resources. The production clipboard service also has its own JVM
   shutdown hook, restricted to still-owned selection; absence of ALL clipboard cleanup is not claimed.
   Exceptional paths can already unwind caller finally. Testing a SecurityManager interception would
   therefore not establish the ordinary bypass, and this test does not use one.
6. No dependency, Store identity/version, release boundary, singleton/product contract, native provider
   cancellation contract or clipboard ownership policy is redesigned here. Original PVU-006's proposed
   create-to-unlock late-operation harm remains unproved under the previously documented guards.

The frozen basis report PVU006-COMPOSE-EXIT-DEFAULT-INDEPENDENT.md is
01e681a426e0e868bdea6f4efa19a400bb6c1d4d4c66c57ec594f2381c9a5579 (10,295 bytes / 128 LF).
It retains the dependency-source and receipt pins, native/clipboard counterexamples and limitations.

## Regression oracle independently challenged

The source declares ONE JUnit method: composeExitReturnsThroughCleanupBeforeOwningJvmExit.
A complete execution would run two child JVM modes serially; failure of the first can prevent the second.
Two children are neither two test cases nor two independent audit build/test jobs.

- Without passvault.lifecycle.syntheticDisplay it explicitly assumption-skips: zero verification credit.
  After opt-in it requires matching restricted DISPLAY, no Wayland value, graphical JDK 17, fresh
  nonoverlapping runtime/evidence directories, full supplied Test runtime classpath and absolute regular
  final-component-no-follow XAUTHORITY. It deliberately has no java.class.path worker-launcher fallback.
- Each child uses a real Compose Window with BasicText, waits two actual frame callbacks, verifies a
  displayable/showing peer, force-writes WINDOW_READY, and dispatches an actual AWT WINDOW_CLOSING event.
  The real onCloseRequest writes CLOSE_REQUEST and calls real exitApplication.
- Historical/default mode uses the real dependency application default and must exit 0 with EXACTLY
  WINDOW_READY and CLOSE_REQUEST. That demonstrates the old caller-finally bypass in a disposable JVM.
- Production mode calls the actual production helper and must additionally record CALLER_CLEANUP from
  the caller's finally and then CONTINUATION, before a real exitProcess(23). Exact ordering, exact event
  membership and different actual exit status distinguish continuation from default process termination.
- Trace appends use FileChannel with NOFOLLOW_LINKS and force(true). The parent retains separate child
  logs and observed decimal exit files. A complete case has six compact evidence files; partial failure
  may leave fewer. No missing file or skipped case is a success witness.
- There is no fake exit callback, intercepted System.exit, full Main invocation, Koin, vault/database
  fixture, clipboard read, native biometric provider or real secret. The synthetic finally marker is
  deliberately NOT represented as provider cleanup, full-Main cleanup, or an actual 2,500-ms deadline test.

A passing admitted run would establish the real Compose/process-return seam plus this helper's behavior
against the resolved runtime binary. Together with the narrow Main source mapping this is meaningful
regression evidence for PVA-039, but by itself it does not execute every Main/coordinator/provider path,
prove pixels, prove physical hardware security, or resolve the wider PVU-006 suspicion.

## Concrete independent corrections and re-review

First iteration child finally could replace a deadline/log/assertion/interruption failure with a later
settlement or evidence-write exception. I requested preservation, not suppression of cleanup failure.
Final test lines 104–144 now retain/rethrow the primary Throwable, including AssertionError; child cleanup
failure becomes an IllegalStateException with its real cause and explicit runtime-cleanup BLOCKED context.
It is attached as suppressed to the primary, or thrown when no primary exists. This preserves evidence of
both ordinary failure stages. InterruptedException from either stage restores the flag; the entering
cleanup interruption is temporarily cleared for bounded wait and restored in nested finally. A new
interruption can still leave settlement unproved: it is failure/outer cleanup responsibility, not success.

Second, each child now has -XX:-CreateCoredumpOnCrash and an explicit private
-XX:ErrorFile=<child-home>/hs_err_pid%p.log (line 96). This closes the avoidable unowned child crash-output
location in the command. It does not establish parent-JVM crash policy, hard whole-runtime disk limits
or recovery from abrupt parent loss; the author addendum explicitly assigns those to outer admission.
No child crash artifact or process was produced by this source review.

Both requested changes are accepted on direct reread of the final frozen test and author addendum.
Main/helper remained byte-identical throughout this bounded correction. No additional product change
or speculative cleanup redesign is required by this review.

## Fresh outer admission still required before any invocation

- Root must establish current source/classpath/toolchain identities, synthetic display ownership and
  authentication, source/runtime/evidence root ownership and safe ancestor chains. The test checks
  final components and lexical nonoverlap; it does NOT self-prove ancestor safety, hardlink/ownership
  policy, true path identity, a private X session, or exact complete runtime classpath.
- Child environment is cleared and rebuilt with synthetic display/auth, private HOME/tmp and per-child
  XDG cache/config/data/state, fixed PATH/locale/timezone, private JNA/SQLite extraction paths, 256-MiB
  heap, one active processor and disabled perfdata. Source flags are not measured RSS/disk guarantees.
- Install reviewed cleanup and process-containment controls before launch, including appropriate parent
  crash policy, wall/RAM/disk limits and independently reviewed ownership/settlement rules. ProcessBuilder
  start is outside the child's 30-second post-start deadline; 100-ms polling and the 256-KiB log limit can
  overshoot. Trace size has a 4,096-byte observed bound. These are cooperative checks, not hard quotas.
- Parent cleanup can forcibly terminate only the captured direct child and waits up to five seconds.
  Direct-child exit is not proof of descendant/Test-worker/global settlement. New interruption, failed
  start, failed wait/write or abrupt parent loss remains a partial/failure outcome. Do not remove runtime
  roots or claim settled workers from a missing child handle or successful direct-child wait alone.
- The fixture intentionally contains no recursive remover. Preserve compact XML/logs/traces/exit files,
  source hashes and necessary synthetic crash diagnostics; after proven owned settlement remove only
  independently validated allowlisted generated/runtime outputs. Keep permanent tests/reports and shared
  caches/toolchains. No large binaries or private runtime archive belong in publication.
- Root remains sole build owner: JDK 17 and checked-in Gradle wrapper, one worker, non-daemon,
  configure-on-demand disabled, serial Detekt, dependency verification intact, one audit-owned local/CI
  build/test job at a time, original wrapper --stop and owned worker settlement. This source review
  supplies no run admission, retry admission, deletion admission, display session or CI authorization.

## Evidence/accounting and restrictions

Declared JUnit cases: 1. Maximum child modes in a complete case: 2 serial. Actual builds, compiled tests,
executed cases, runtime observations and hardware tests in this review: 0. No closure numerator changes.
Root records PVA-039 grouping/denominator updates separately; no original-confirmed or suspicion credit.

Only bounded inert source/report reads, byte hashing/inversion and this new permanent report occurred.
No subject import/execution, AST/syntax probe, build/test/helper invocation, Git/store probe, network,
runtime/storage traversal or process probe occurred. Stable file fields and exact bytes were checked.
No runtime/cache/build artifacts, temporary task files, background workers or daemons were created;
wrapper --stop and runtime cleanup are not applicable to this source-only task. Root owns host resource
monitoring. This report is written exclusive/no-follow with file/parent fsync and exact readback hash.

Preserve PVU-007 STOP, PVU-011 NO RETRY, PVA-029 recorded failure/no automatic retry, G7/G8 CLOSED,
Windows05 filesystem HOLD, other consumed/HOLD scopes, rejected/grouped variants and all eight PVD
design boundaries. Windows05's earlier passing CTests do not erase its helper/job failure or filesystem
reparse HOLD. No old validation/recovery helper was replayed and no platform change bypasses admission.
PVA-027's separate Settings-to-native-tray work and every other unfinished item remain separate.
