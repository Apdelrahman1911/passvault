# C20 Android32 — independent prerequisite challenge

Reviewer: `/root/android_compile_review`; 2026-09-11 UTC. **Retained-data/source
review only; no execution admission, source patch or native verification.**

## Disposition and smallest prerequisite

**Accept the author's no-new-action disposition, with the classification
qualification below.** No evidence-supported prerequisite source correction or
presently approved usable Android32 target was identified. Reuse Compile03;
do not create another harness or rerun compilation to manufacture progress.
PVA-001 remains implemented with target-runtime verification blocked.

The smallest owner clarification is whether an authorized owner's existing
acceptance covers the exact retained January 16, 2019 `android-sdk-license`
agreement and intended system-image use; otherwise obtain explicit authority for
that agreement and principal. An already-approved, suitable 32-bit Android target
could avoid acquiring this image, but its availability/authority cannot be
presumed. Root then owns any separate, exact target/preparation/package/run
admission. No blanket permission, automatic license acceptance or hardware-only
requirement is inferred here. This prerequisite was promptly relayed to root.

The author's phrase **“EXTERNAL AVAILABILITY/ADMISSION GATE”** is accepted only as
“no currently established approved target.” It does not make all remaining work
external: licensed archive verification/staging, packaging, containment and
closeout are unfinished internal work. None is admitted by these reports.

## Independent challenges

- **Existing target is contrary evidence, not a candidate to retry.** Retained
  `REPORT.md:135–150` records API35/default/x86_64 revision2 with
  `ro.system.product.cpu.abilist=x86_64`, empty `abilist32`, and native bridge0.
  ABI-preference manifest flags cannot make that unchanged image a 32-bit target.
  These are historical retained file observations, not a new installed-target
  inventory. Historical absent `/dev/kvm` does not prove software emulation
  impossible; a backend filename does not prove successful boot either.
- **Official metadata is not a downloaded or booted target.** The retained
  catalog selects API24/default/x86 revision8, `x86-24_r08.zip`, 313489224 bytes,
  declared digest `c1cae7634b0216c0b5990f2c144eb8ca948e3511`; archive requests0,
  `archive_fetched=false`, integrity-against-archive=false. The accepted
  `sys-img2/01 -> repository-common/01 -> complete/archiveFields` documentation
  resolves the algorithm as SHA-1, superseding the catalog result's historical
  `checksum_type=unspecified`. It is not whole-catalog/JAXB validation, archive
  integrity, a publisher signature, license authority or native evidence. No
  invented mandatory upstream SHA-256 or new catalog/XSD capture is needed.
- **Known acceptance metadata is neither absence nor proven coverage.** The
  retained prerequisites record an existing 41-byte license marker, not who
  accepted or the principal/use scope. Raw/trimmed text hashes do not establish
  the SDK's canonical normalization or a mismatch. Compile03 SDK preflight says
  `READ_ONLY_EXISTING_COMPILE_SDK_NO_COPY_INSTALL_LICENSE_CHANGES`; this is not
  acquisition/acceptance authority for a new image. The retained selected-license
  text identity is `de5fa465e908e7f098e718082f013135b9a36ee706d032cf510f4f1153424b2a`.
- **Unchanged source is not a reason for redesign.** Independently rehashed all
  six current fixture/engine/raw-adapter/helper/build/manifest entries in the
  sealed author map; all match its identities, including compiled fixture
  `9c4567e4a9cd1f6023534d3b5a40dfb21981cd49cc8eff0b4d1e453429246ca8`.
  Reuse the accepted same-loaded-proxy, 64-bit outlen/passwdlen/opslimit and
  width-selected boxed-Int Android32 `size_t` contract. Preserve strict caller
  UTF-8 encoding followed by historical **lowercase ASCII hex of caller bytes**;
  do not substitute raw UTF-8, uppercase hex, NativeLong, profiles or dependencies.
  Fixture lines42–91/125–162 independently sustain the fail-closed treatment of
  the retained nullable-reflection warning: a null/Proxy failure is caught as
  failure/error, not success. No cosmetic patch is justified by that warning.
- **Four native cases remain unexecuted.** Preserve the first-case process and
  POINTER4/SIZE_T4 gates, four exact start/pass identities and terminal accounting.
  Four cases contain four KDF calls, not four calls in the loader/width case.
  Linux32/JVM/JNA/mock/64-only execution is not this Android32 slice. x86 is not
  ARM32; debug instrumentation is not packaged/minified application, business
  create/unlock/credential/backup compatibility or hardware security proof.

## Reused compilation and current C19 binding

The accepted Compile03 actual review remains compiler-only: fresh MAIN/DEVICE,
14 observed graph/header nodes within a 26-name ceiling, 10 actionable executed,
3 UP-TO-DATE and1 SKIPPED; 27 selected class records (16 engine-family +11
fixture-family). Generic task inputs are not proved compiler classpaths or
visitation. No binary class bundle/APK/runtime is made reusable by these hashes.
Preserve the original stop/settlement/removal result and all qualifications;
do not rerun or upgrade it to any test/KDF/native32/ABI/DEX/shrinker credit.

Retained C19 publication records commit
`3b2130fce1d15c7686f068ae994e4cb8a80714de`, tree
`603ed0fbe94721d75893ba3307119523189eca85`, parent C18
`6489252e88ad553a867d67578eff45a402e62a48`; additional tests0/closures0 and
`UNCHANGED_IDLE_NO_BUILD_OR_CI_ADMITTED`. This is a retained result binding, not
a fresh Git/remote/current-host observation. C19 publication/nonactivation
neither admits this target nor guarantees future external CI state. The older
prerequisites' “uncompiled” wording is superseded only by Compile03 compilation.

## Exact retained anchors

Paths below are relative to `docs/audit-continuation/2026-09-08-linux/`.
Whole-file hashes were rechecked; semantic review was bounded as described.

| Input | Bytes | SHA-256 |
|---|---:|---|
| `reviews/team20/android_compile_author/c20/ANDROID32-BLOCKER-ACTION-MAP.md` |10477|`fa2196d7d327df791952b818987597b9d4ca8fa67a0d58ae8c9bfcc838e69ea3`|
| `reviews/android32/ANDROID32-ADMISSION-PREREQUISITES.md` |20564|`5c2966372abb1aebb56175a7062e4ceef8fa990875a2c6d332c9b136e3397089`|
| `reviews/android32/REPORT.md` |32468|`b800ecf4e48dc51eee34ca5bfb6ede09d55ac2b9fa7d555910fd7d1c3fd660e5`|
| `reviews/android32/SDK-CATALOG-RESULT.json` |19940|`abe9675dde3224b784c928b2df33103e23c18ed487f51a9f7b8895e9c3e1177b`|
| `reviews/build-config/ANDROID-XSD-CAPTURE-ACTUAL-AND-SCHEMA-REVIEW.md` |12839|`a97a44c24580df4e65c1d96ba1dc71666d9d6bf7ed437bdb4550ec384dd94acb`|
| `reviews/team20/android_compile_review/COMPILE03-ACTUAL-REVIEW.md` |10126|`c385c30dff777e00b78c334ba07b6b4e7e954e089aaae5d9efe8e573db865606`|
| `runs/linux-android-compile03/SDK-PREFLIGHT.json` |2026|`7cfbe7a43e85369a0b7bd1e5ae68ec69895e18efde930ce7d7a7839aaac78902`|
| `publication/CHECKPOINT-19-PUBLISHED.json` |67057|`02bfa6144fb3640cc5fe9dd0bdd75edc997efc4137f2e3410c546f9b031ade63`|

Inspection caveats: one retained-JSON display was truncated; claimed fields were
reread narrowly. One display-only TypeError exited1 because path counts were
integers, not arrays; the corrected reader exited0. These were reader-output
errors, not product/target failures or changed evidence.

Only this new own-lane report is written. No repository-helper import/execution,
SDK/ADB/device/build/test/Git/network operation or held/T filesystem access.
No admission, download, license change, cleanup release, publication or central
edit. Preserve every old/new runtime HOLD, consumed/STOP/NO-RETRY/CLOSED,
native-refusal, original-store/lock, protected-ref, publication and occupied
build1017001 fence. **Zero additional tests, native calls, closures or denominator
changes.** Root should reassign rather than commission another unlaunchable plan
while the precise prerequisite remains unresolved.
