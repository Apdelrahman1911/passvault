# C20 applied recopy method — narrow independent selection review

2026-09-11; `/root/android_platform_review`. Source/retained-data only.
**ACCEPT the exact applied test after-image and prospective one-method host
selection. Not current task-graph, execution or cleanup admission.** This adds
no framework-fixture review or Android32 work to the earlier dispositions.

## Applied bytes

- Actual W `app-android/src/test/kotlin/com/passvault/android/security/AndroidClipboardServiceTest.kt`
  is SHA-256 `ea3b535b99390d4850269bcc2089c8f6e26c1096cd17052ef361fae3ba5b3332`,
  matching the previously independently computed 190-LF / 6,633-byte after-image.
- Passive text-stream removal of only inserted lines109–148 independently
  reproduces preimage `b4a69761ef3f6920ee586f070b5a22a7d552151850bce2718e1a7c06b5c91b64`.
  The six prior methods/helper/fake/imports remain unchanged. The file now has
  seven declared host methods, not seven fresh executions.
- Root's B-relative `reviews/team20/root/C20-KOTLIN-TEST-INTEGRATION.json`,
  SHA-256 `c80dcd359ae8d23c40fd5930a43c5172ccffb3c45dc672b35d430fd5a90c980f`,
  records this exact applied after-image, with no execution. Earlier inert-only
  reviewer notes remain historical pre-adoption records, not stale instructions
  to apply the patch again. This review covers only its Android row.

## Exact source-grounded selection

Reviewed author note, B-relative:
`reviews/team20/android_platform_author/c20/SINGLE-HOST-TEST-SELECTION.md`,
SHA-256 `0b21bbc488528504cceaf04e60becce4140da85e074251d74a986ca5c93477e5`.

| Binding | Accepted prospective value |
|---|---|
| Test task | `:app-android:testDebugUnitTest` |
| Exact per-task filter | `com.passvault.android.security.AndroidClipboardServiceTest.newSensitiveCopyDoesNotInheritAnUnavailablePendingClearOrPriorExpiry` |
| Required fresh result | Exactly one successful, non-skipped testcase of that exact class/method; reject no match and extras |
| Default relative XML | `app-android/build/test-results/testDebugUnitTest/TEST-com.passvault.android.security.AndroidClipboardServiceTest.xml` |

Independent source grounding:

- Root `build.gradle.kts:531–533` explicitly includes the Android app task in
  `androidHostTestPaths`; `552–561` wires that list into the root aggregate.
  File SHA-256 `6c35802a0f779baa1b4d5a65396a07824653761882ddfbfaa718a46c59fa5ee0`.
  The aggregate itself is deliberately **not** selected.
- App `build.gradle.kts:394–397,582–588,630–638,814` supplies the Android
  application plugin, ordinary Debug variant, JUnit unit-test wiring and existing
  Kotlin/JUnit dependency. File SHA-256
  `c7a382773cb11383d583b6fd1dcead85aac30ccc0a38f81995f535dc1029da42`.
- Catalog `gradle/libs.versions.toml` binds the plugin and dependency; SHA-256
  `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a`.
- Handoff `current/issue-to-fix.json:4669–4728`, SHA-256
  `5782666330bc2eb017e42b7a8725db1efdf54223462f3648c8acf25cb21b245e`,
  retains six prior fake-method result identities under this exact XML suffix
  for `android-g5-host7-dao-binding-01`, XML identity
  `f893f67467da7a21141a96ced2f038de48ad43bdd008da3a100847c3f5f8a438`.
  This is a read of the current historical projection, **not** a fresh packed-XML
  read, reuse of a CLOSED runtime or new verification of the historical run.

## Challenges preserved for the focused batch

1. Apply the exact filter to **this Test task**, not a root/global or class-wide
   wildcard selection. Fail on no match. The six old methods must not execute;
   compilation of their containing file is not a replay. The actual shared-batch
   init/selection reviewer must bind and check its final per-task filter behavior.
2. One selected Test task is not one task-graph node. Android resources are enabled
   for these unit tests, so normal SDK/resource/compiler dependencies may remain.
   Source references and old XML do not prove the current graph, environment,
   dependency availability or SDK-use admission. No Gradle task listing/dry run
   or build was performed to obtain this note.
3. The XML path is relative to the independently admitted source worktree, not
   permission to build in W. If the focused init relocates output, bind the actual
   new location in its inventory; reject stale/reused XML or extra testcases.
   Exit0 without the exact fresh non-skipped method outcome is insufficient.
4. This uses the existing fake access and controlled expiry gates. No real
   Application/Main/ClipboardManager/device is launched. Do not add instrumentation
   bundle arguments, `connected*`, release/storeScreenshot, assembly/package,
   KDF or root `test`/`check` tasks. It gives no OS-time/foreground/framework proof.
5. Execution, source/graph/SDK/resource/ownership admission, aggregate result mapping,
   cancellation and cleanup remain with root and the focused batch's independent
   reviewers. **This note accepts none of their uninspected executable controls.**

## Result and limits

One new permanent host method is now applied; **zero fresh executions or closure
credit**. Production and the accepted two-case framework fixture remain unchanged.
PVA-009/PVA-030 actual-framework/target/license/isolation gaps remain. Crypto
compile03 is not repeated or relabeled as app-host-test compilation.

Only this new reviewer report was written. No source/central edit, Git/CI/network,
SDK/ADB/process/held-runtime/device probe, application/helper import or execution,
build/test or cleanup occurred. Preserve all STOP/NO-RETRY/CLOSED/native-refusal/
HOLD restrictions, PVA-029 49/44/5 FAIL/no automatic retry, build1017001, protected
refs and version/dependency/identity/PVD/hardware/account/license/Store limits.
Root remains the sole build, Git/CI and cleanup owner.
