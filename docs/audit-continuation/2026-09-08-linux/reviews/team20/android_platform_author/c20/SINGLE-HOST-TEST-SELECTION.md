# C20 recopy method — exact existing host-test selection

Source/evidence-only, 2026-09-11. Root applied the accepted method; this read
matches `AndroidClipboardServiceTest.kt` SHA-256
`ea3b535b99390d4850269bcc2089c8f6e26c1096cd17052ef361fae3ba5b3332`.
No build, task listing, test, archive/helper execution or target probe occurred.

| Binding | Exact value |
|---|---|
| Existing host Test task | `:app-android:testDebugUnitTest` |
| Class | `com.passvault.android.security.AndroidClipboardServiceTest` |
| Single method | `newSensitiveCopyDoesNotInheritAnUnavailablePendingClearOrPriorExpiry` |
| Exact per-task include/filter | `com.passvault.android.security.AndroidClipboardServiceTest.newSensitiveCopyDoesNotInheritAnUnavailablePendingClearOrPriorExpiry` |
| Expected default XML under the admitted source worktree | `app-android/build/test-results/testDebugUnitTest/TEST-com.passvault.android.security.AndroidClipboardServiceTest.xml` |

**Source grounding:** root `build.gradle.kts:531–533` explicitly lists this path
in `androidHostTestPaths`; this is not a guessed KMP `testAndroidHostTest` name.
Root file SHA-256 `6c35802a0f779baa1b4d5a65396a07824653761882ddfbfaa718a46c59fa5ee0`.
`app-android/build.gradle.kts` SHA-256
`c7a382773cb11383d583b6fd1dcead85aac30ccc0a38f81995f535dc1029da42`:
ordinary debug is declared at582–588; unit tests include Android resources,
use JUnit and the existing JVM argument at630–638; `libs.kotlin.test.junit` is
the test dependency at814. No new test dependency or test engine is required.

**Historical actual-result projection:**
`docs/audit-handoff/current/issue-to-fix.json:4669–4728`, SHA-256
`5782666330bc2eb017e42b7a8725db1efdf54223462f3648c8acf25cb21b245e`,
records run `android-g5-host7-dao-binding-01`, six fake ClipboardService methods
with six passes/no errors/failures/skips, each mapped to exactly the above
`testDebugUnitTest` XML suffix. Recorded XML SHA-256:
`f893f67467da7a21141a96ced2f038de48ad43bdd008da3a100847c3f5f8a438`.
Recorded result/review identities are `1e33e080...` / `2a7abed0...` in that row.
These are inherited independently reviewed result projections, **not a fresh
read of packed XML**, a new run or permission to reopen a CLOSED namespace.

For the fresh shared batch, bind only this method on this Test task, fail when
no method matches, and require exactly one successful, non-skipped XML testcase
with the exact class/method. Reject extra cases; do not use class-wide/wildcard
filters or a root `test`/`check` aggregate. Compilation of other methods does not
constitute replay, but the six old methods must not execute. Preserve the shared
batch's existing per-task isolation, ownership, source, timeout and cleanup
guards; any XML output relocation by its init must be reflected exactly in its
new result inventory. The path above is the established default, not an override
for that inventory.

This method uses the existing injected fake access and two controlled expiry
gates, not an Android Application/ClipboardManager/Activity or instrumented
device. Do not select `connected*`, instrumentation, release/storeScreenshot,
assembly/package or a KDF task, and do not add the framework isolation bundle
argument. It supplies no actual-framework, SDK-license/guest-isolation, KDF/ABI,
PVA-009/030 closure or physical-device evidence. Root owns fresh admission.
