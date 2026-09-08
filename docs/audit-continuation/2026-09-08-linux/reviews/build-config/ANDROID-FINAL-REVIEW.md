# PVA-001: independent Android32 harness source review

Reviewer `/root/build_config`; implementation author `/root/android32`.

**ACCEPT_BOUNDED_SOURCE_PATCH. NOT_EXECUTED. NOT_ADMITTED. PVA-001 remains open.**

This is independent review of the final two-file patch, not acceptance of an
Android build, device, execution helper, output parser or cleanup plan. There
were **zero compiled or executed application test cases** in this review.

## Exact reviewed bytes

| File | SHA-256 |
|---|---|
| `core/crypto/build.gradle.kts` | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| `core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt` | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` |

Both were read in full. The configuration diff is eight added lines; the new
harness is 291 physical LF lines. `ANDROID-FINAL-INPUTS.json` also binds selected
surrounding source/configuration. These are point-in-time file identities in the
continuation worktree, **not a frozen whole-tree or commit identity**. Re-review
changed bytes before using this disposition.

## Reachability, dependency and compatibility challenges

1. **Compilation is not runtime filtering.** The original proposal would have
   included all `commonTest` sources, fake/test dependencies and backtick/space
   JVM method names. The accepted patch instead uses
   `withDeviceTestBuilder { sourceSetTreeName = null }.configure { ... }`.
   The official AGP9.4 builder contract explicitly excludes common test source
   sets for null; runner/HOST properties and builder chaining are separately
   documented in `ANDROID_API.json`. The original rejected proposal is retained
   in `ANDROID-PROPOSAL-REVIEW.md`, not erased.
2. **No new runner dependency, not dependency-free.** The diff adds no dependency
   coordinate, version, production namespace or application identity. The
   harness uses platform `Instrumentation`, coroutines and production crypto
   code. The tested main source graph already declares coroutines and libsodium;
   device-test access to that main graph, the generated manifest/test APK,
   plugin-injected dependencies, task names and realized classpath are still
   **compile/graph evidence pending**. The API documentation is not downloaded
   plugin-bytecode proof. Keep strict dependency verification; do not add a
   runner library or change checksums to conceal a classpath failure.
3. **Runtime JNA is not the compile-only JNA.** Android main uses compile-only
   JNA5.19.1, while the legal inventory records Android runtime JNA5.18.1 and
   Desktop5.19.1. Existing verification metadata contains the5.18.1 AAR and
   both relevant JARs. These are recorded inputs, not a newly resolved test APK.
   Reflection prevents the harness from adding a compile dependency or inlining
   the wrong `VERSION` constant. The exact5.18.1 historical `Native` source was
   reread through the independently admitted Git data reader: it implements
   `Version`, does not redeclare `VERSION`, and has a private `getNativeVersion`.
   Narrow `isAccessible = true` on the one inherited app-library `VERSION`
   field addresses the package-private-interface counterexample; there is no
   platform hidden-API call, arbitrary field access or native-memory read.
   The observed string is a Java-library version, **not the jnidispatch binary
   version or an ELF hash**. Missing/incompatible reflective APIs fail the case.
4. **The production boundary is reachable in source.** Cases call
   `LibsodiumCryptoEngine.deriveKey`, not a fake engine. Its common code preserves
   the historical lowercase-hex byte encoding and calls `rawPasswordHash`;
   Android's actual implementation obtains `crypto_pwhash` from the libsodium
   JNA proxy and supplies the memory limit according to `Native.SIZE_T_SIZE`.
   The test's direct reference is public; no access to an internal helper is
   needed. No production implementation changes are present in this patch.
5. **A64-bit install cannot count as32-bit evidence.** The first case rejects
   `Process.is64Bit()` and requires JNA pointer and size_t widths of four.
   Native `long` width is recorded separately. A default install on a mixed-ABI
   guest may select64-bit and therefore fail deliberately. The patch does not
   force32-bit ABI. A separate reviewed target/installation command must make
   that path reachable; x86, ARM32 and different Android API levels must not be
   conflated. No emulator, physical device, ADB process or installation was used.

## Case/oracle and failure-path challenges

The fixed inventory contains four unique case IDs:

1. `nativeRuntimeIs32BitAndLoadsSodium`
2. `historicalBinaryPasswordVector`
3. `historicalTextPasswordVector`
4. `productionProfilesMatchReferenceVectors`

The initial case performs no KDF. The other three contain **four KDF calls**:
two8KiB-memory binary/text compatibility vectors and two64MiB profiles with
operation limits3 and4. Constants agree byte-for-byte with the existing
`Argon2Test.kt` values. The production profile comments retain the independently
generated upstream reference provenance; this review did **not** regenerate an
oracle or rerun the archived reference tool. The harness does not validate
benchmark timing or profile-selection policy.

- An early size/unique-name guard protects the four-case inventory. All cases
  run serially. A64-bit/width/linkage failure stops before any KDF case.
- The initial argument blocklist was challenged: aliases/listing/sharding could
  otherwise be silently ignored. The final runner accepts only an empty bundle
  or one bounded String `additionalTestOutputDir` transport field. That field is
  never opened, used as a path or claimed as output. Any other key fails before
  `start()`. Runtime field/type behavior remains unexecuted.
- Standard per-case start1/pass0/assertion-failure-2/error-1 records include
  exact class, test name, current index and fixed count. Any case `Throwable`
  produces a failed terminal result; unstarted cases are absent, not fabricated
  skips/passes. Success `RESULT_OK` is reachable only after all four pass records
  have been sent. A `sendStatus`/process/native crash outside the catch can leave
  incomplete results, **not a valid success**; the future parser must reject it.
- Reflection obtains the actual proxy library and `crypto_pwhash` symbol. The
  optional own-process mapping read records only the containing symbol row,
  with8192-line/4096-character limits; denied or missing maps is explicitly
  `NOT_ESTABLISHED`. This is not a process-memory dump or packaged ELF binding.
  Mapping-line and stack-trace truncation occur after string allocation, so
  they are not hard transient-memory limits. A host deadline remains necessary.
- Synthetic password/salt/expected/key arrays are cleared in `finally`. This
  does not erase constant Strings, prove physical-memory erasure or remove
  PVD-003. No vault, backup, Activity, clipboard, keystore or provider/prompt
  operation is reachable from this harness.

## Required follow-up, with no closure credit

- Freeze the full continuation source identity and independently admit the
  exact narrow compile/package commands, JDK17/toolchain/SDK, one-job resource
  ownership, output allowlist and cleanup before a build.
- Establish device-test compilation, generated manifest/isolated synthetic
  package identity, verified dependency graph, actual test APK hashes/ELFs and
  the32-bit target. An application build1017001 replacement is forbidden.
- Independently review exact instrumentation arguments and a fail-closed
  transcript/XML parser. Require equality of four case identities and statuses,
  expected/started/passed counts and terminal status. Shell/task exit alone is
  insufficient. Keep raw compact evidence, including failures/missing output.
- Execute real Android32 native vectors when an independently admitted target
  exists. Keep ARM32/x86 and hardware security gaps explicit; do not substitute
  host JVM mocks, compilation or a64-bit emulator label.

All original STOP/NO-RETRY/CLOSED scopes, PVA-029's failure, candidate1017001 and
the eight PVD owner decisions remain unchanged. The existing denominators gain
**zero** qualified closures or conclusively resolved suspicions from this review.
