## Fix the actual iOS release blocker
Release35003648380 failed before compilation because the existing Kotlin2.4.10 Intel macOS archive lacked a verification checksum. Strict verification correctly refused it. This is not another heap failure.

- Add only missing host SHA256, independently matched from streamed Maven bytes to official JetBrains release asset476576010. No dependency/version/trust-policy change.
- Existing verifyDependencies now requires both Apple host compilers for the pinned catalog version.
- Replace existing iOS simulator CI job with release-host Intel/Xcode26 simulator compilation plus an **unsigned optimized iosArm64 framework link**. Same number of jobs; no signing/Store inputs. Opt-in4GiB heap requiresmacOS>=12GiB; defaults/cleanup unchanged.
- Focused local mocked heap-contract test, AST2/YAML/staticCIsecurity/XMLpin/diff checksPASS; cleanupPASS. Actual native compile/link still pending this CI.

Prior iOS cleanupHOLD remains. Overall release was subsequently cancelled during GooglePlayupload;1017003 Store outcomeUNKNOWN/non-reusable. This PR does not bump a Store number or authorize a release/retry. Normal protected review required. No production.
