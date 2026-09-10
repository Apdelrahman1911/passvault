# Independent iOS10 storage / runner-boundary challenge

Reviewer `/root/storage`; proposal/fixture author `/root/android32`; 2026-09-10.
**ACCEPTED SOURCE DIRECTION AND FINAL FIXTURE GUARD ONLY. NO EXECUTION ADMISSION.**
Root remains sole build/test/Git/CI owner. This is a point-read review, not a
whole-tree freeze, workflow/helper review, simulator observation or family closure.

## Exact review inputs

Paths below are repository-relative except the three review notes, which are
under `docs/audit-continuation/2026-09-08-linux/`.

| Input | SHA256 |
| --- | --- |
| `reviews/android32/IOS-FOCUSED-VALIDATION-PLAN.md` | `caebd58bb7c964e36b206f57dfaec0a9968457456ddc68171977fd4079e25e65` |
| `reviews/android32/IOS-FOCUSED-FEASIBILITY.md` | `b12138ca015a97e61c0f06381d7aad0f3b2cd51e03c5f3cf08c85d229b5509db` |
| `reviews/native/APPLE-KOTLIN2410-INDEPENDENT.md` | `949b7dab3427f6861866258ccccb007d64e32c9fee93dae594fbbcc45a90da17` |
| `shared/src/iosTest/kotlin/com/passvault/shared/platform/IosAttachmentFileStoreTest.kt` | `406c47fd2ea71afe50a57acfbc5050bcda05a3050e94a16c5555a5e053bdc337` |
| `shared/src/iosTest/kotlin/com/passvault/shared/platform/IosBiometricPromptStringsTest.kt` | `9e18f0bd193ba3308982ec05b45ab6d025e40f9b8b35baf880aa9ff66d640400` |
| `core/crypto/src/commonTest/kotlin/com/passvault/core/crypto/SecurityTest.kt` | `867a98c43713ce879e129953e4fcdd4c314f3e737a85a5b7e7b78ed5360639f6` |
| `core/database/build.gradle.kts` | `e83ca3a79cafd99f25d9d5a34c4db15f30ba1784a2e7613cfb44d9cbd7ffdbbc` |
| `gradle.properties` | `323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235` |
| `.gitattributes` | `8884ed2a100ce791326a3a8d8d4c12a5e0f68d96ecb382fd3a6612ad827604c5` |

Both complete proposal versions and the complete attachment/prompt fixtures were
read; the final attachment change was re-read in its helper context. Crypto setup
and the two selected methods, Room's schema block and complete Gradle properties/
attributes were inspected. Retained plugin conclusions are reused with their
published source-review limits; no new full plugin/runtime semantic review is
claimed. Its retained source-data packet still hashes to
`2730e2ec45b5638f7e58712d3a64960e81d61324c9a8ae8bbaa74066f8db73ee`.

## Guard challenge, correction and compatibility

The initial plan (`ea6908326a08698ccacf5f15e023bbbea0ae2c490b046e181640fc0ccc1661e9`)
left an opt-in fixture guard UNBOUND. Independent challenge: losing both an
audit-required flag and parent through the same environment channel can choose
an unguarded default; detecting a missing receipt afterward is too late. Root
explicitly accepted a **mandatory synthetic-parent caller contract**, including
ordinary developer invocations of these seven tests. No generated audit mode,
copy-only patch, new test declaration, dependency or production behavior was added.
The pre-guard fixture read was
`e8c466aa2ca2adeb57d4a276eb315f3162bd751bbf8653c20b653e5414e6bdcd`.

The final guard satisfies that narrow source contract:

- Both helpers call it before fixture file access. Absent all environment,
  `checkNotNull(getenv("PASSVAULT_IOS_TEST_PARENT"))` fails first. The expected-
  failure assertions are inside subsequent fixture blocks, so they cannot swallow
  a failed admission and manufacture a passing protection/cancellation case.
- Parent is absolute, bounded and control-free, with no empty/dot components and
  exact synthetic basename. `realpath(parent) == parent` rejects spellings changed
  by canonical resolution; actual Foundation temp must equal it or start with `parent + "/"`.
  A broad `/`, `parent-other` prefix or child symlink resolving outside cannot pass.
  Directory type, exact mode0700 and effective-UID ownership are checked before
  receipt/path return. Native `realpath` allocations are freed in `finally`.
- Independent review caught a receipt bound missing from the first patch
  (`c2051cfd18c1fa8c66bdd407eb73aa0fa6a6cd053563b8ab6577d03448c8f46c`):
  a contained actual path with tab/newline could produce an ambiguous receipt.
  Final source also bounds the actual canonical temp path to2048 characters and
  the same control-free character allowlist. Both helper paths derive from the
  checked result; the seven case bodies/production assertions remain unchanged.

This is **not** fresh-root/UDID/ACL/cleanup proof or an atomic filesystem capability.
A preexisting lookalike parent or a same-user writer racing after checks cannot be
excluded by name/mode/UID alone. The runner must exclusively create and bind the
new private parent and own its lifecycle with no competing writers. Foundation
numeric bridging, POSIX API availability and actual guest behavior remain
unexecuted; unexpected attribute representations fail closed, not as successful
evidence. Existing fixture removal return values remain unchecked. Independent
settled parent/device closeout is still necessary; do not credit fixture `finally`
alone. Receipt paths are synthetic evidence, not physical protection evidence.

## Disposable raw-source replica: supported, implementation still unreviewed

Room writes to `$projectDir/schemas`; redirecting only build outputs is not enough.
A newly created, explicitly disposable raw-source replica is the smallest supported
direction without changing schema configuration or touching the original checkout.
Concrete admission must enforce the proposal's complete parent-tree inventory,
regular Git modes, byte/count/depth bounds, exclusive no-follow copy, per-blob hashes
and executable classification. Also compare the actual destination inventory to
the exact source path inventory: case/Unicode directory aliases on the target
filesystem must not silently merge distinct Git paths. Reject links, unsafe or
extra entries, omissions and attribute-induced substitutions; do not normalize
checkout EOLs or copy `.git`, ambient metadata, untracked source or caches.

Keep the replica outside an ambient checkout-discovery boundary, with private
HOME/tmp/Gradle/Konan/project-cache roots. Verify original input bytes/modes after
settlement; changed inputs invalidate the freeze. Preserve compact evidence before
disposing only the identity-bound new replica/runtime roots. Root's acceptance of
this disposable-copy direction does not authorize deleting the original checkout,
permanent tests/schemas/reports, shared caches/tools or uncertain live output.
Unknown generated contents/links and incomplete settlement need fail-closed,
allowlisted cleanup handling in the independently reviewed implementation. No old
archive/helper, Linux HOLD runtime or Mac02 CMake cleanup authority is reused.

## Remaining concrete execution gates

1. Bind one newly owned simulator, exact create/UDID/start/standalone/guest-env/root
   mapping and shutdown/delete/settlement contract. Host TMPDIR or a separate
   preflight process is not same-process Foundation proof. No default/preexisting
   device, erase-all/sweep, guessed mapping or immediate-process-kill substitute.
2. Resolve admitted arm64 tools/JDK17 and use the checked-in wrapper with strict
   verification, one worker, non-daemon, parallel/CoD/build/config caches off and
   in-process Kotlin. Explicitly override the checked-in4GiB heap and enabled
   parallel/cache defaults; no toolchain installation fallback. A JVM heap cap or
   worker count does not cap LLVM/native/CoreSimulator memory. The proposed
   disk/RAM floors and30-minute total still need concrete monitoring and a distinct
   cleanup deadline, including wrapper `--stop` against only the private home.
3. Preserve a fresh allowlisted parent environment: retained plugin execution
   adds task variables without clearing inherited environment. Bind filter/runtime
   and raw XML identities, not task exit0, display-name normalization or all-tests
   fallback. Reconcile exactly **7 attachment +1 prompt +2 real-crypto cases once
   each**, with same-process guard receipts and no unexpected/missing/skipped/failed cases.
4. Source/helper/workflow/activation identity, the cross-host sole-owner slot and
   cancellation/cleanup authority need independent admission before any run.
   Forced/ambiguous settlement is HOLD; hosted disposal is not cleanup proof.

The real-provider positive control and typed wrong-KEK test are meaningful PVA-038
compatibility discriminators, not native tag-tamper/repository/enrollment-preservation
tests. The LAContext case checks properties, not displayed/authenticated prompts.
PVA-008 protection/copy seams are not physical failure observations. **Ten declarations,
zero new executions/fixes/closures.** Physical iPhone security gaps remain BLOCKED;
no closure denominator or PVD decision changes. PVU-007 STOP, PVU-011 NO-RETRY,
PVA-029 recorded failure/no automatic retry, G7/G8 CLOSED and all consumed/HOLD,
non-publishing/candidate1017001 restrictions remain intact.

Only bounded source/data reads, hashes, coordination and this permanent review
were performed. No Git/network, helper import/execution, build/test/CI, simulator,
cache/runtime probe, temporary replica or background worker was created. No new
runtime cleanup obligation arose; this discharges none of the historical ones.
