# Protected merge CI preparation — not application/beta readiness

Source: integration beb131add14a53ad13d9b7ebb30a5ab4738adec2 plus current resource-safety patch;
exact proposed file hashes and commit will be recorded before trigger. No PR yet.
Root remains sole CI/build owner; reviewer /root/current_ledger read-only.

Actual existing CI coverage/check names and all-job CI Gate retained. Serialize
validate → dependency-verification → test → Android → Desktop matrix1 → macOS
package matrix1 → shared compile → iOS simulator compile. No release workflow,
Store/signing credentials, production signing, upload, tag or version mutations.
Ephemeral Android validation certificate and isolated synthetic Apple importer test
only; unsigned Desktop packaging. Baseline synthetic native ABI/security helpers
are not the archived stopped audit helpers. GUI opt-in stays disabled.

Target evidence: selective integration regression/compilation/package checks on
Ubuntu (JDK17), hosted Windows (JDK17/CMake), hosted macOS arm64/intel (JDK17,
CMake/Xcode). Simulator compile does not prove physical iOS security. Full normal
CI is necessary for the integration gate; do not infer PVA001/029/mobile closure.
Exact commands remain in .github/workflows/ci.yml; branch PR base main triggers
normal protected checks. Required approving GitHub review remains external.

Before trigger: independently review fresh ci-run.py, synthetic safety tests,
CMake bounds, private native temp and Apple test cleanup patch; validate workflow
security/source-aware release suite and synthetic process-safety cases once locally.
These local checks use only synthetic files/short sleep processes, no Gradle,
application, native provider, keychain or platform build. Use coordination lock,
180s bound, compact stdout/stderr and hashes, fixture auto cleanup; no daemon to stop.

Hosted batch35min/job45min maximum, wrapper stop60s, settlement15s; job matrix1.
RAM launch/running floors25%/20%, polled with disk every2s. Private HOME/TMP/Gradle/Konan, original wrapper, serial worker/Detekt/Kotlin, strict
dependency verification unchanged; no cached app data. 3GiB absolute disk abort
floor (owner fallback), monitor every2s. Source safety rejects pre-existing outputs
and tracked/symlink cleanup roots. POSIX owned session with explicit unique Gradle JVM argv marker
check; Windows no-breakaway kill-on-close JobObject assigned before shell launch.
Stop wrapper, settle owned scopes, preserve bounded XML/HTML/cleanup records, then
remove allowlisted generated roots/private caches only. Failures retain HOLD;
forced runner loss leaves hosted VM disposal as explicit fallback, never a PASS.
Reports ≤25MiB per step, artifact retention3days, no APK/DMG/archive uploads.
No local heavy work while CI is active. Refresh remote jobs before PR, never cancel
unrelated runs. Do not retry failed unchanged checks automatically.

Admitted source commit: `f8f3c9c0bd61af79e41a7a8e5efe6c0a3167ff15`; tree `312791149ba33ebd9d67aaf67819ab075e1f0b84`.
Normal PR synthetic merge source must have this exact tree while base main remains
`0dbc12c7f1b7770e75963c751c8c67af6e8b057a`; otherwise stop our run and re-review.
