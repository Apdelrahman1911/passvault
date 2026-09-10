# iOS same-ten Intel feasibility after IOS01

Author `/root/build_config`, 2026-09-10. **SOURCE/RETAINED-EVIDENCE ONLY;
no new run proposed or admitted.** `B` means `docs/audit-continuation/2026-09-08-linux`.
This supplements the existing Apple matrix; it is not a new product finding,
fix, test pass or closure. Independent challenge remains for root to assign.

## Decision: not feasible with the checked-in target contract

An Intel label substitution cannot run the same existing ten cases unchanged:

- `shared/build.gradle.kts:28-39` declares only `iosArm64` and
  `iosSimulatorArm64`; there is no `iosX64`/`iosX64Test` target.
- `core/crypto/build.gradle.kts:34-49` declares those same two targets and
  configures `rawSodium` cinterop separately for each. Database declares the same
  targets (`29-30`), with only `kspIosArm64`/`kspIosSimulatorArm64` Apple KSP
  entries (`75-76`). The inspected relevant core/feature wiring is likewise
  ARM64-only. `settings.gradle.kts` supplies no included convention build that
  adds an Intel target.
- The existing workflow selects `macos-15` and explicitly requires
  `JAVA_HOME_17_arm64` (`26`, `59`). The existing feasibility/validation plan
  requires Apple Silicon and the ARM64 simulator target. Changing `runs-on`
  alone does not satisfy that source/host/JDK contract or provide an Intel
  simulator executable.
- The retained Apple matrix already records this boundary (`26-30`). There is
  no checked-in Intel route here to validate merely by observing more RAM.
  Compatible Intel iOS dependency/cinterop/Room and actual simulator execution
  evidence also remain absent; absence of evidence is not proof that every
  dependency lacks an Intel artifact.

The unchanged selection remains **two task identifiers, ten case declarations**:
`:core:crypto:iosSimulatorArm64Test` runs the two `SecurityTest` VEK wrapping/
wrong-KEK cases (PVA-038 compatibility evidence), while
`:shared:iosSimulatorArm64Test` runs seven `IosAttachmentFileStoreTest` cases
(PVA-008) and one `IosBiometricPromptStringsTest` case (PVA-014). The raw IOS01
`declared_cases` retains all exact names. Shared does not inherit crypto tests.
No target additions, dependency changes, emulation, simulator installation,
alternative-label matrix or resource-floor changes are proposed here.

## What the two actual runs establish

**IOS01**, run `34454557777`, attempt1, source
`6220812e4602369d4853b763465a92e6291bb971` /
tree `3defb2a459ffbbddb51b3baa0f088d438edf74c3`; activation
`d4b72e0a30e2b53283a3f23b13b942afc6cbd38b` /
tree `291a75853d8f81c5b1da5b4dfe653896cf79a1bc`:

- Setup reports `macos-15-arm64`, image `20260829.0321.1`, macOS15.7.9;
  Python3.14.7 was observed. No Xcode/SDK/simctl/JDK release/provider pass follows.
- Physical RAM was **7,516,192,768 bytes (7GiB)**. At launch,
  `(9,386 free + 2,005 speculative) * 16,384 = 186,630,144` bytes,
  **2.4830%**, below the unchanged25% floor (1,879,048,192 bytes).
  Disk45,334,519,808 bytes exceeded12GiB. This was a pre-workload resource
  rejection, not application OOM or measured project memory demand.
- **10 UNSTARTED; 0 XML.** The first xcode-version record is an unlaunched intent,
  as independently source-qualified in the actual-result review. Metadata/source/
  ACL checks and evidence allocation did occur; no private runtime, simulator,
  wrapper or Gradle was started. Wrapper stop was not applicable.
- Actual helper exit1 and terminal failure leave
  `PENDING_REQUIRE_ACTUAL_HELPER_EXIT_ZERO` **unmet**. No reported close error
  does not prove a complete close PASS or a close failure. IOS01 stays consumed,
  with no automatic retry or recovery authority.

**Mac02**, run `34430921453`, attempt1, source
`6838961e966975532c714d60fd72e3f590f0c8ab` /
tree `88f37c328a84322d6d97d04200711cde45ca98d4`; activation
`7b288271d6c139921812caf5050b7f994f5ce1b2` /
tree `d2a6b22bf39a13d5360aed31f879613660576a96`:

- `macos-15-intel`, image `20260824.0482.1`, x86_64, macOS15.7.9;
  Xcode16.4/16F6, **macOS** SDK15.5, CMake/CTest4.4.2 and Python3.14.7.
- The24 retained samples (9 launch,15 running) show14GiB total,
  5,666,586,624–6,169,923,584 bytes free+speculative (37.6959–41.0442%),
  and disk at least116,386,357,248 bytes. That run passed the same25/20% RAM
  and12/8GiB disk floors; it does not reserve a future host's resources.
- **Five independently accepted native CTests**, x86_64 Mach-O evidence and
  actual exit0/qualified original cleanup are preserved for that exact run.
  They provide no Gradle, iOS SDK/simulator, Kotlin target or same-ten runtime
  evidence. No Mac02 runtime is reused or accessed by this review.

## Resumption boundary and byte identities

Keep the iOS ten unexecuted and genuine physical-iPhone security/prompt behavior
hardware-BLOCKED. Root alone decides any future materially different, independently
reviewed execution admission after Linux. Do not infer failure of all Apple hosts
from IOS01 or bypass PVU-007/PVU-011/PVA-029, G7/G8 CLOSED, consumed/HOLD scopes,
PVD decisions or existing cleanup restrictions. No central counts change here.

Current source and retained evidence were read/hashed as inert bytes, without
Git, network, SDK, build/test, helper import/execution or old-root probes. No
temporary archive/cache, generated application artifact or background worker
was created. Historical plan admission language does not reopen consumed IOS01.

| Input (relative to repository, or `B/`) | SHA256 |
|---|---|
| `.github/workflows/audit-ios-focused-validation.yml` | `d1a1b3b5c06b46446e935ce3ff2e75ec2a829d9d6179df0b90f75ddf7c2a9dfa` |
| `settings.gradle.kts` | `a2a3336bd1cce66d616c2a49a1773f4d3c93d437759a093ff28e92e4bd646734` |
| `shared/build.gradle.kts` | `826cc234a59c05efdbbcd6a0ea62f25362f0a54d588e996582903f44d09949ad` |
| `core/crypto/build.gradle.kts` | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| `core/database/build.gradle.kts` | `e83ca3a79cafd99f25d9d5a34c4db15f30ba1784a2e7613cfb44d9cbd7ffdbbc` |
| `B/reviews/build-config/APPLE-TARGET-MATRIX.md` | `338f3d6265993693400d06056802065391bd8bca5c9d98a9d2ddb85c9979d5ef` |
| `B/reviews/android32/IOS-FOCUSED-FEASIBILITY.md` | `b12138ca015a97e61c0f06381d7aad0f3b2cd51e03c5f3cf08c85d229b5509db` |
| `B/reviews/android32/IOS-FOCUSED-VALIDATION-PLAN.md` | `da3f69651b706991a3a53b886b1faaa020db243873cb7ec2092e3f3493f511f2` |
| `B/runs/ios-focused01/artifact/result.json` | `ff5c54e508b71c3ec04765005e9d96b1379b6eae5bc39a04cb362986f50a31d1` |
| `B/runs/ios-focused01/workflow-logs/ios-ten/1_Set up job.txt` | `e0595b12d7752da96d5d0e76f1284c4153297cf433b8f2dbc83dbe1dc3a95373` |
| `B/reviews/storage/IOS01-ACTUAL-RESULT-REVIEW.json` | `59bb624896d25ab72edf4d2f05310f24bf8296a9e58379875aa5e2de20ca850d` |
| `B/runs/macos-focused02/result.json` | `cd97b458a944810397ffe51b1ff6e3418322657e4e2c0f36428b3928c1d58241` |
| `B/runs/macos-focused02/architecture.log` | `aed7eab2db8004014015bd77fb65f673b5797b73dad4f7e8e72f5cde290dc880` |
| `B/reviews/native-independent/MACOS-FOCUSED-02-ACTUAL-REVIEW.json` | `73aef6e8b91a225594a378e812785c2e162f961ed62fde0be547516eae0b8285` |
