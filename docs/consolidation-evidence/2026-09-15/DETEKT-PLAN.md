# Narrow correction verification, not a replay of the original batch

The original 184 JUnit cases passed (17 XML suites, zero failures/errors/skips).
Three selected Detekt tasks passed, while app-desktop Detekt failed solely because
preserving the baseline prompt-coordinator case made KeyStoreTest exceed the
11-public-function threshold. Original failure and successful cleanup remain.

Move that exact unchanged test method into DesktopBiometricPromptCoordinatorTest
in the SAME file. No product code, assertions, imports, fixture, or release tool
changes. Independent review checks the move. Reuse original successful test bodies;
no repeat crypto/storage test cycle. Run only :app-desktop:detekt using the original
controller design under new independently sealed source/namespace/admission.
15-minute maximum. Correct read-only dependency-cache root to /root/.gradle/caches;
this is an efficiency fix only, no dependency verification changes. Original8/5GiB
fallback guards, cleanup/stop/settlement and selected reports preserved. No old
runtime or runner imported or replayed; old result is immutable evidence.
