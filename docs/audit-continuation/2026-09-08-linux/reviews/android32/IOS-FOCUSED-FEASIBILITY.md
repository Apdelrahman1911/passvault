# Focused iOS feasibility: existing shared8 plus real-crypto2

Author `/root/android32`, 2026-09-10. **SOURCE/RETAINED-EVIDENCE ONLY. No
implementation, build, simulator, network, Git, cache or old-runtime access.**
Initial feasibility was sent to root before authoring. This supplements the
existing Apple matrix/native reviews rather than restarting them.

## Smallest meaningful existing selection

Use one future serial job/owned arm64 simulator with **two module test tasks**,
not an all-tests run or one shared binary. `shared` depends on crypto **main**
through `api(project(":core:crypto"))`; it does not inherit crypto `commonTest`.
No dependency/source-set change or copied/new test is necessary for this batch.

Existing `shared/src/iosTest/kotlin/com/passvault/shared/platform/` cases:

- `IosAttachmentFileStoreTest` (PVA-008), seven exact source methods:
  1. `cancelled return after move removes the adopted plaintext directory`
  2. `cancelled return after copy fallback removes both plaintext locations`
  3. `successful adoption transfers directory cleanup to source close`
  4. `destination protection failure removes the moved file and owned directory`
  5. `cancelled admission removes the original picker copy without adopting it`
  6. `picker copy is protected before the import path is returned`
  7. `picker copy is deleted when immediate protection fails`
- `IosBiometricPromptStringsTest` (PVA-014), one exact source method:
  `both native prompt operations receive the selected English or Arabic text`.

Existing `core/crypto/src/commonTest/kotlin/com/passvault/core/crypto/SecurityTest.kt`
adds two useful methods, not a larger crypto/KDF matrix:

1. `VEK wrapping roundtrip` — real-provider positive control.
2. `wrong KEK fails VEK unwrapping` — real-provider wrong-key rejection with
   `assertIs<CiphertextAuthenticationException>` at line350, not just failure.

That is **10 declarations = shared8 + crypto2, zero current iOS executions**.
The second task adds actual typed-provider compatibility evidence relevant to
PVA-038, not a full iOS repository/biometric/provider-failure reproduction.

### Prospective task/option groups, not an admitted command

```text
:core:crypto:iosSimulatorArm64Test
  --tests 'com.passvault.core.crypto.SecurityTest.VEK wrapping roundtrip'
  --tests 'com.passvault.core.crypto.SecurityTest.wrong KEK fails VEK unwrapping'
  --device <one-new-owned-UDID>

:shared:iosSimulatorArm64Test
  --tests 'com.passvault.shared.platform.IosAttachmentFileStoreTest'
  --tests 'com.passvault.shared.platform.IosBiometricPromptStringsTest'
  --device <the-same-owned-UDID>
```

The crypto task was registered in retained Linux03 `ordinary.log:207`; that
registration is **not** proof of an enabled/runnable Apple target on Linux. The
shared task remains source-conventional, not an observed realized task graph.
The retained Kotlin2.4.10 plugin source supports `--device` and include forwarding
to `--ktest_gradle_filter`; native runtime matching and exact emitted XML/name
mapping remain unproved. Do not invent normalized names or an all-tests fallback.
`isFailOnNoMatchingTests=false`, missing-executable/host skip guards and
`--ktest_no_exit_code` make task/process success insufficient. A future admission
must establish exactly these ten cases once each, not task counts. Missing,
unexpected, duplicate, skipped, failed or unstarted cases are not passes.

## Existing source guards and counterexamples

- `IosAttachmentFileStore.kt:300–334` retains `ownedPath` inside the dispatched
  worker, checks cancellation before ownership transfer, and uses
  `NonCancellable` cleanup if the return handoff is discarded. Adoption validates
  an absolute non-parent path, a regular file and attachment size/name; move or
  copy/delete precedes destination protection. `close():408–412` deletes only the
  owned attachment directory. Immediate picker protection is synchronous before
  returning the path (`541–573`). The fixtures use real Foundation files and a
  bounded manually drained dispatcher. Forced-copy is an injected false move
  result, not an observed OS move failure; most protection-failure paths use a
  seam. Fixture finalizers' unchecked removal returns do not prove host/guest
  cleanup. The independently owned simulator/storage closeout is still needed.
- `IosBiometricKeyStore.kt:655–662` constructs an `LAContext` and assigns reason
  and cancel title. The one test checks enrollment/unlock in English and Arabic
  and invalidates the contexts; it calls no authentication, capability or
  Keychain operation. Four successful context constructions are still one case,
  not four displayed-prompt or hardware tests.
- `SecurityTest.setUp():20–23` installs `LibsodiumCryptoEngine`, not a fake.
  Its positive and wrong-KEK cases obtain real32-byte keys and a successfully
  encrypted wrapped VEK. `CryptoEngine.kt:192–221` preserves the envelope/nonce
  and `VEK_WRAP` AAD and delegates unwrap directly to the real engine. The wrong
  key therefore reaches AEAD with valid key/nonce sizes and envelope framing.
- `LibsodiumCryptoEngine.kt:154–196` initializes first, validates32-byte key,
  24-byte nonce, magic and minimum tag-bearing length, then invokes dependency
  XChaCha20-Poly1305 decrypt. Only `AeadCorrupedOrTamperedDataException` becomes
  `CiphertextAuthenticationException`; temporary ciphertext/plaintext are wiped.
  `cryptoOperation:286–307` preserves ordinary exception identity in `Result`
  while rethrowing cancellation. Other provider/init errors are not deliberately
  relabeled as authentication failures.
- `VaultRepositoryImpl.kt:702–731` decrypts normalized verification data with
  verification AAD, wipes plaintext, and maps only the app's typed authentication
  exception or private invalid-plaintext marker to `BiometricVaultKeyRejectedException`;
  it does not itself perform enrollment deletion. Metadata/key/
  lock-generation guards and the existing generic operational-failure path
  remain separate. The two crypto cases do **not** execute this repository route,
  protected-key release, deletion/preservation or native initialization faults.
- Do not select `CryptoEngineTest` as native evidence: it uses `FakeCryptoEngine`.
  `SecurityTest.tampered ciphertext fails decryption` flips ciphertext byte0,
  changing the app envelope magic; it can fail before AEAD. Its nonce-tamper and
  direct wrong-key methods assert only `isFailure`. None proves **typed native
  tag-tamper classification**. The typed wrong-key/tag-tamper PVA-038 repository
  tests are under `core/database/src/desktopTest`, not inherited into iOS. No
  native typed tag-tamper regression was identified in the inspected test sources.

## Dependency source: what is actually retained

`reviews/editor/biometric-verifier-sources/common-aead.stdout.txt` retains the
dependency's `expect` API and `AeadCorrupedOrTamperedDataException` declaration;
`android-aead.stdout.txt:33–55` retains the Android/JNA implementation throwing
that type on a failed status. **Neither is the Kotlin/Native actual body.** Their
recorded source-data commands are historical evidence, not commands to replay.
The PVA-038 independent patch review explicitly leaves native AEAD unverified.

The project selects libsodium bindings0.9.5. Checked-in verification metadata
contains iosSimulatorArm64 module/metadata/main-KLIB/cinterop-KLIB hashes, not
the native AEAD source body or proof of a locally available/resolved artifact.
The project's `RawPasswordHash.ios.kt`/`rawSodium` cinterop implement password
hashing, not this upstream AEAD decrypt boundary. No exact native AEAD source
was located in the inspected retained capture/report set; this is **not** an
exhaustive cache/store absence claim. No cache/store scan or invented source URL
is proposed. Root can separately obtain exact authoritative version-bound source
if needed; binary/source equivalence remains a distinct question.

Kotlin2.4.10 **test-plugin** source is retained in
`reviews/build-config/KOTLIN2410-NATIVE-TEST-SOURCE.json` (ten selected members),
with independent review `reviews/native/APPLE-KOTLIN2410-INDEPENDENT.md`.
Reuse those contracts; do not re-fetch them. They do not contain the native
test-runtime filter implementation or Gradle XML writer.

Independent `/root/editor` response-only challenge supports the two-module
selection, positive/typed-negative discriminator and rejected weak substitutes,
subject to native filter/runtime/cleanup admission. The reply performed no new
source reads and is **not** an exact-byte review of this note. The original
PVA-038 author knows of no later retained native AEAD capture; that is a knowledge
limit, not an absence proof. No new source acquisition was requested of that agent.

## Remaining boundaries

Apple Silicon/macOS with compatible Xcode/iOS runtime is required by the declared
arm64 target/host guard. Two filters do not shrink shared/main/commonTest/KSP/Room/
cinterop or all-project configuration. Root must freeze the complete source,
keep JDK17/wrapper/strict verification/one-worker/non-daemon/CoD-off rules, admit
an allowlisted parent environment, one new simulator and effective synthetic
Foundation temporary storage, and install bounded original-wrapper stop,
owned-process/simulator settlement and allowlisted cleanup **before execution**.
No Xcode app scheme, embed/sign framework, release/candidate1017001 operation,
Store/private material or extra matrix is needed. No runner/workflow is authored.

Even ten passes would not establish real iPhone file-protection, picker/lock/
background behavior, displayed/authenticated biometric security, enrollment
preservation under a real native provider fault, or complete PVA-038 closure.
Hardware gaps, all STOP/NO-RETRY/CLOSED restrictions and separate denominators
remain unchanged. Android SDK-license/usable32-bit-target blockers are untouched.

## Exact point-read identities (SHA-256)

These bind this report's source observations, **not** a new whole-tree freeze.

| Repository-relative input | SHA-256 |
| --- | --- |
| `shared/build.gradle.kts` | `826cc234a59c05efdbbcd6a0ea62f25362f0a54d588e996582903f44d09949ad` |
| `core/crypto/build.gradle.kts` | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| `shared/src/iosTest/kotlin/com/passvault/shared/platform/IosAttachmentFileStoreTest.kt` | `e8c466aa2ca2adeb57d4a276eb315f3162bd751bbf8653c20b653e5414e6bdcd` |
| `shared/src/iosTest/kotlin/com/passvault/shared/platform/IosBiometricPromptStringsTest.kt` | `9e18f0bd193ba3308982ec05b45ab6d025e40f9b8b35baf880aa9ff66d640400` |
| `core/crypto/src/commonTest/kotlin/com/passvault/core/crypto/SecurityTest.kt` | `867a98c43713ce879e129953e4fcdd4c314f3e737a85a5b7e7b78ed5360639f6` |
| `core/crypto/src/commonTest/kotlin/com/passvault/core/crypto/CryptoEngineTest.kt` | `64a61dac56d054c9351600b08b65bb843064b9adb3490309e445bd15d68c2a49` |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/LibsodiumCryptoEngine.kt` | `2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57` |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/CryptoEngine.kt` | `571ff97bf0758e788e8ad405ab0c147b52e040fcc291c28080329d44ba8516b9` |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt` | `aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630` |
| `shared/src/iosMain/kotlin/com/passvault/shared/platform/IosAttachmentFileStore.kt` | `7f6a7e3efdeca79ae1410b98110ffd13160dd36a9aabd4cf046ceb5338096488` |
| `shared/src/iosMain/kotlin/com/passvault/shared/platform/IosBiometricKeyStore.kt` | `c0cdb7627c0bdc3a7c23c526deb70ef27a38261ab2241571616905a48498ce17` |

Under `docs/audit-continuation/2026-09-08-linux/`:

- `runs/linux-isolated-batch03/logs/ordinary.log`:
  `e79ed666f239eed08e4cfe606fcb1f41b19069301c3b768c40d42ab00aaaca5b`
  (reused prior admitted-read identity; C4 task registration only).
- `reviews/editor/biometric-verifier-sources/common-aead.stdout.txt`:
  `243dccc28f03b94f5c4f323c8e0139ac1088e2da091524bedfcae7a7f011ea7a`
- `reviews/editor/biometric-verifier-sources/android-aead.stdout.txt`:
  `2eac8066b9b07b7dd920d684f650c9279c8ce911f322800c2a8611cdd2fd48da`
- `reviews/build-config/KOTLIN2410-NATIVE-TEST-SOURCE.json`:
  `2730e2ec45b5638f7e58712d3a64960e81d61324c9a8ae8bbaa74066f8db73ee`
- `reviews/native/APPLE-KOTLIN2410-INDEPENDENT.md`:
  `949b7dab3427f6861866258ccccb007d64e32c9fee93dae594fbbcc45a90da17`

Only this permanent note was created. No new tests, source patch, runtime output,
worker or cleanup obligation arose. One source-query shell quoting error occurred
before commands ran; no project invocation or operational test was attempted.
