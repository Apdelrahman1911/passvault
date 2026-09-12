# C20 PVA014: default publication into real LAContext — inert existing-test delta

Author `/root/apple_platform_author`, 2026-09-11. **PROPOSED ONLY; NOT APPLIED,
COMPILED, RUN OR INDEPENDENTLY ACCEPTED.** Intended source path, if root later
grants it: `shared/src/iosTest/kotlin/com/passvault/shared/platform/IosBiometricPromptStringsTest.kt`.
Current SHA256 `9e18f0bd193ba3308982ec05b45ab6d025e40f9b8b35baf880aa9ff66d640400`.
The complete inert after-image is `IosBiometricPromptStringsTest.kt.proposal.txt`.

## Distinct source-supported gap, not a new product finding

The existing iOS case passes `strings = strings` explicitly for both operations.
Production enrollment and retrieval call `iosBiometricPromptContext(enrolling)`
without that argument (`IosBiometricKeyStore.kt:245,328,655-660`). A wrong or
stale default lookup could therefore leave the existing explicit-argument case
passing. Current source does use the proper default getter; no defect is alleged.

The common `NativeBiometricPromptStringsTest` already checks literals, regional
normalization and publication/getter behavior. Do not rerun or take new credit for
those pure checks. It does not join publication to the real LAContext defaults.
`AppLanguageProvider.ios.kt` remains the production resolver/publisher, but that
Composable and NSUserDefaults/System-language routing are outside this delta.

## Smallest bounded correction to the verification selection

Keep the exact existing one `@Test` method and both explicit English/Arabic
context/property checks. Extract only their four existing assertions into a
private helper taking a context factory. Nest its invalidation finally blocks
so an exception constructing the second context cannot strand the first, and
the first invalidation is still attempted if the second invalidation fails.

Within the same method, add just one serial Arabic-regional→English publication
sequence with the factory calling `iosBiometricPromptContext(enrolling)` using
its default. Expected values come from the selected language, **not from the
current getter under test**. Save the original public language tag before the
sequence and restore it in `finally`, matching the existing common fixture's
restoration contract. The private publication store is only written through the
normalizing publisher, so its public language tag reconstructs the prior value.

This remains **one test method** (and ten selected test methods overall), not
four tests or eight passes. The private helper adds no `@Test` declaration.
There are eight factory calls in four paired helper invocations, with two
operation contexts per pair. No coroutine, Keychain,
permission prompt, capability/authentication call, persistent setting, file,
app window or dependency is introduced. The selected native test process must
remain isolated and serial; no concurrent app-language publisher is admitted.

Discrimination, reasoned from source only: hard-coding the helper's default to
English leaves the old explicit loop green but should fail the new Arabic
LAContext assertion; retaining Arabic after the English publication should fail
the second default-path evaluation. No mutation, helper import or test execution
was performed to claim those are observed failures.

Cleanup qualification: if the production factory throws internally after a
context allocation, it has not returned an owned reference to this helper; that
internal allocation is not covered by the helper's invalidation finally blocks.
An invalidation error can mask an earlier assertion error, although both
owned-context invalidations are attempted and the case still fails rather than
passes. No allocation/invalidation-fault control was run or claimed. This small
coverage extension is not a general native-resource failure harness.

## What this does not accomplish

No compatible host is created; no actual-framework evidence exists until the
independently admitted Apple run executes. This does not invoke the actual
enrollment/retrieve callers, test active-prompt language switching, display any
prompt, resolve System language, touch Keychain or prove authentication/physical
protection. It does not close PVA014 or PVU004 or replace their native/physical
requirements.

The consumed IOS01 helper pins the old fixture hash. If this proposal is accepted
and applied, that old helper is deliberately incompatible and remains consumed;
do not edit/replay it. Only a separately assigned NEW, independently reviewed
successor could bind the accepted after-image and host/source/cleanup instance.
No workflow or request changes are proposed by this file.

Independent review must challenge assertion preservation, default-path reach,
expected-value independence, both allocation/invalidation failure paths, prior
language restoration, and the lack of new execution/closure credit. All C20
Apple route prerequisites and handoff safety fences remain binding.
