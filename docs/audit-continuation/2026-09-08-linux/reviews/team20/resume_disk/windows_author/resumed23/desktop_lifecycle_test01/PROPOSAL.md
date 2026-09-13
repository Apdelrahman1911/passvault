# Focused05 Desktop test classpath: exact proposal, authorization required

**Not applied.** The original Focused05 is consumed; its failure and complete
cleanup stay retained. This proposal author has run no build/test or retry.

## Cause and smallest correct delta

The nine diagnostics at focused.log382–390 are one compiler/classpath problem:
`androidx.lifecycle.ViewModel` is unavailable while Kotlin resolves
BackupViewModel.state (six sites) and SettingsViewModel.state (three sites).
Both production classes directly extend ViewModel and expose their actual
StateFlow as a class member, not through an existing lifecycle-free interface.

Desktop main depends on shared, which publicly exposes both feature modules.
But shared and each feature declare `libs.lifecycle.viewmodel` as
`implementation`, not an API dependency. Desktop test declares only kotlin(test)
and coroutines-test. The source therefore supports the missing test compile
dependency reported by the actual compiler; no new runtime diagnosis or
independent resolution graph is claimed. Runtime opt-in/X-display guards are
later than compilation. Filtering test names or leaving opt-ins false cannot
repair this source-set compile failure.

Proposed one-line addition in app-desktop/build.gradle.kts desktopTest only:

    implementation(libs.lifecycle.viewmodel)

No test, assertion, API, production dependency scope, version, catalog,
verification metadata or release configuration changes. This explicitly gives
the tests the existing versioned superclass they legitimately observe; it does
not construct a replacement ViewModel, invoke callbacks, alter Koin, or remove
native/Room/modal/tray observations. All nine failing read sites remain exact.

## Authorization boundary — owner decision required

This **is a dependency declaration change**, even though the alias is already
used by the project and its version/artifacts are already checked in. The user
prohibits dependency changes without separate authorization. Root should ask:

> Approve adding the existing libs.lifecycle.viewmodel dependency solely to
> app-desktop's desktopTest source set, with no version/catalog/verification
> or production dependency changes, to fix the nine Focused05 compile errors?

Catalog: org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.11.0. Relevant
existing verification entries include JetBrains common/desktop and AndroidX
common/desktop2.11.0; exact checksums are in PROPOSAL.json. These establish
existing fixed-version and checksum provenance, **not a resolved configuration
lock, artifact availability, or proof of the eventual selected transitive graph**.
Strict dependency verification stays intact; no metadata regeneration/update,
version override, network probe, dependency resolution or tool access occurred.

A no-dependency source workaround is not recommended: suppressing missing
supertype diagnostics or using reflection hides a real compile contract;
exporting a new product interface/broadening shared api for tests changes the
product boundary; deleting/excluding tests or substituting mocks removes the
important real-state observations. None is proposed.

## Smallest useful later verification

After independent source challenge, explicit owner approval and root's fresh
source/execution/cleanup admission, the direct risk check is
`:app-desktop:compileTestKotlinDesktop` with the established JDK17/wrapper/serial
strict-verification settings. It proves the nine compile errors are resolved
without removing sources. Batch it with the next needed Desktop test cycle
instead of re-running already-successful DB/credential tests. No full Android/
Apple/Windows matrix is justified by a Desktop test-only declaration.

A successful compile alone does not close PVU003, PVA027, Desktop integration,
or any product family. Their genuine platform/UI/native/Room tests remain
separately admitted work; no opt-in or hardware behavior was executed here.
Preserve Focused05 as failed, not retried or rewritten.

All STOP/NO-RETRY/CLOSED/HOLDs, original native refusals, protected refs/tags/
build1017001 and eight PVD boundaries remain. Only this own inert proposal leaf
is written; zero canonical edits, builds/tests, new closure credit or cleanup
obligations. Root owns application of any later explicitly authorized change.
