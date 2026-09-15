## Fix the actual iOS release blocker
Release35003648380 failed before compilation because the existing Kotlin2.4.10 Intel macOS archive lacked a verification checksum. Strict verification correctly refused it. This is not another heap failure.

- Add only missing host SHA256, independently matched from streamed Maven bytes to official JetBrains release asset476576010. No dependency/version/trust-policy change.
- Existing verifyDependencies now requires both Apple host compilers for the pinned catalog version.
- Replace existing iOS simulator CI job with release-host Intel/Xcode26 simulator compilation plus an **unsigned optimized iosArm64 framework link**. Same number of jobs; no signing/Store inputs. Opt-in4GiB heap requiresmacOS>=12GiB; defaults/cleanup unchanged.
- Focused local mocked heap-contract test, AST2/YAML/staticCIsecurity/XMLpin/diff checksPASS; cleanupPASS. Actual native compile/link still pending this CI.

Prior iOS cleanupHOLD remains. Overall release was subsequently cancelled during GooglePlayupload;1017003 Store outcomeUNKNOWN/non-reusable. This PR does not bump a Store number or authorize a release/retry. Normal protected review required. No production.

## Follow-up correction and current verification
Initial PR CI [35007304336](https://github.com/Apdelrahman1911/passvault/actions/runs/35007304336) failed during root Kotlin DSL compilation: the `kotlin` and `kotlin-test` catalog aliases make `libs.versions.kotlin` a subgroup accessor, not a direct provider. Commit `0f38e9dedcba7e1154145e5f62cca7924566487f` uses `.asProvider()`. Independently source-reviewed; original CI failure and cleanup PASS are preserved.

Changed-source CI [35008085858](https://github.com/Apdelrahman1911/passvault/actions/runs/35008085858) is running. Root Gradle DSL compilation, dependency verification and attribution now **PASS** on checkout `e87c6ebaf65535eafa5e9556f7cf2bb2ccae4a0c` (tree `91198329c4ec4d2ee2e48b41e136192132dae896`, matching the PR source tree). Cleanup PASS, wrapper stop 0. Optimized native linking remains **queued/pending**, not claimed passed. No Store upload/retry or build-number change.

## Detekt follow-up
CI35008085858's `Run Tests` job failed root Detekt: the added `verifyMetadata` body exceeded 60 lines and its compiler-component regex exceeded the line-length limit. The 198 unit XML reports contain **1,604 passed, 5 skipped, zero failed/error test cases**; this does not waive the failed static gate. Failed batch cleanup passed and wrapper stop returned 0. Shared compilation and Linux/Windows Desktop jobs passed; full CI/native link did not.

Commit `c3038a0eae1d38f1e1e8fb820f85745ecd862f7b` extracts the unchanged host check into a private method and concatenates the same regex string. Independent source-equivalence review passed; no Detekt suppression, threshold change or security-policy weakening. Actual corrected Detekt remains pending. The superseded owned non-publishing run was cancelled under fresh review; missing interruption cleanup evidence remains UNKNOWN/HOLD, not cleanup success. A new changed-source CI will follow only after remote settlement and coordination release.
