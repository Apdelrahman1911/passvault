# AndroidCompile02 independent baseline challenge

Reviewer `/root/android_compile_review`, 2026-09-11. B is
`docs/audit-continuation/2026-09-08-linux`. Source/retained-data review only.

**A new exact prerequisite proposal is supported. No proposal acceptance,
application, execution admission, compiler pass or product closure follows.**
Compile01 remains consumed failure with its independently accepted original
cleanup. C17 application/tests/helpers and publication T remain frozen.

## What the retained failure actually establishes

I independently rehashed all ten path/size/SHA256 bindings in
`B/reviews/android32/android-compile01/ACTUAL-REVIEW.json`; all match. The original
log's markers at lines 3508 and 3510 reproduce the two saved JSON files byte for
byte. The log has zero `> Task ` headers. The finished record reports phase
`graph`, no completed compilers, and `evidenceComplete=false`.

The graph contains 14 distinct enabled nodes, all reachable by dependency edges
from the sole selector `:core:crypto:compileAndroidDeviceTest` (DEVICE). Text-only
extraction gives equal 25-name domains in init and inner. Their only observed
omission is:

```text
path:        :core:crypto:androidJar
type:        org.gradle.jvm.tasks.Jar_Decorated
enabled:     true
dependencies/finalizers/mustRunAfter/shouldRunAfter: all empty
sole incoming reference: DEVICE.dependencies
```

DEVICE also directly depends on `:core:crypto:compileAndroidMain` (MAIN).
The init membership guard at lines 80-82 throws through line 33; the retained log
names that guard, not a Kotlin source error. Configuration, resolution and DSL
work did occur, but neither target compiler action ran. The 25 default UNSTARTED
entries include 12 names absent from the actual graph; they are not attempted
tasks. Collector `main_transitive_dependency_observed=false` is unevaluated after
its early two-versus-ten receipt rejection, not evidence that MAIN was absent.

The original inner says evidence preserved, mapping false, stop satisfied,
namespace empty and cleanup safe. The outer says `VALIDATION_FAILED_CLEANED`,
13,111/13,111 inventoried descendants removed, with no reasons. The independent
actual review and separate original session57369 terminal exit1 corroborate
normal failed completion. This review does not replay cleanup or inspect R.

## Source context and counterexamples

All 34 non-doc Gradle source descriptors in the retained C17 SOURCE manifest
still match their raw SHA256/size tuples; none contains literal `androidJar`.
These include dormant audit init files, not 34 executed build scripts. The four
required production/harness source tuples also still match C17. Crypto applies
the Kotlin multiplatform and Android KMP library plugins, selects JVM17 and the
fixed device instrumentation configuration. Catalog/wrapper pins remain Kotlin
2.4.10, AGP9.4.0, Gradle9.7.1. This supports ordinary plugin-origin context, not
an exhaustive plugin-source or arbitrary-task-action safety proof.

The narrow new control must reject these source-level counterexamples before
task actions, then independently map corresponding retained evidence:

- A different Jar task/path/type, disabled androidJar, or a new outgoing edge.
- An incoming androidJar reference other than DEVICE's direct dependency.
- Missing direct DEVICE-to-MAIN dependency, or absent/disabled real compilers.
- Expansion to generic `*Jar`, tests/Exec/JavaExec, APK/AAR assembly, install,
  sign/publish or another selector.
- Updating only the allowed set while retaining either inner 25 ceiling: header
  parsing (line152) and observed graph mapping (line804) both require 26. Tighten
  the init graph ceiling from64 to26; do not inflate it or the receipt count.
- Reusing C17/01 source or instance pins after fixture corrections. All four
  required-source hashes, full SOURCE/helper hashes, commit/tree/member count,
  readRoots/offline, device/lock/exclude and request/approval facts require fresh
  root binding. A null marker must reject rather than supply an ambient default.

The exact graph places androidJar before MAIN and declares no androidJar-to-MAIN
edge. Do not manufacture such an edge or claim this archive contains main
classes. No archive inputs, outputs or bytes were observed. The new allowance
must retain ordinary frozen-source/strictly verified-plugin trust explicitly.

Ten successful receipt roles remain graph+finished and before/input/state/classes
for each compiler; none is an application/native test. Generic task inputs still
do not establish classpath/visitation, and compilation cannot establish 32-bit
native KDF, JNA reflection, APK/DEX/ABI/minification, business or hardware behavior.

## Error/cleanup compatibility required in the proposal

Original `preserve_compilation` retains raw marker bytes before interpretation.
Semantic guard/mapping failure can be preserved evidence and must not become a
cleanup veto by itself. Retention/readback uncertainty must still prevent cleanup.
Original compile-finally stop, settlement, cancellation/source/binding checks,
outer safe-failure return, original-namespace/descriptor ownership and allowlist
cleanup must remain unchanged. No retry of01, new SDK access/agreement change,
dependency/version change or cap relaxation is justified by this omission.

## Identity anchors

| Input | SHA256 |
| --- | --- |
| consumed01 actual review | `c6bf30fcdee251b7358bcd9059e897c1807af5175f8d02e59028acee06a27a7b` |
| consumed01 original graph | `7130a1eb3fbe8f8be2592d1241c7ac86551f3c102c8b4d542993418dc388ca96` |
| consumed01 original log | `c5840e1a9fee47cdc1c0f8d8bfc8f259a1caf313c6cccb2e7620c9997ad75666` |
| current bound01 init | `c6e7c4f36ce9909414be548e867cb11bf9186f0dc09c5246b44c82cd62a00ffe` |
| current bound01 inner | `e4f31a57a16d91dc956969621f238c37b8acfee258dee9a6c7d2f333b50218b6` |
| current bound01 outer | `bb3cc62765f128a0122f2b0d614a38f731985402bbde5dd21766a5fcec33f54b` |
| core/crypto/build.gradle.kts | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| gradle/libs.versions.toml | `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a` |
| gradle/wrapper/gradle-wrapper.properties | `245b6dba5960c9c54a8495bf16195fbebe2d91a825238edbde1ac2b858c86f6f` |

## Reporting/activity limits

Root was told that the slot's nested android_compile01 `state` still reads
`ADMITTED_NOT_YET_INVOKED` beside consumed=true and failed/cleaned status. That
stale field must not authorize a replay; reviewer made no central-ledger edit.

I read the saved pause, current team authority, handoff/permissions/assembly,
published-payload qualifications, W AGENTS and focused verification/readiness/
native-packaging instructions. Only bounded source/evidence reads, standard-library
data/hash/text comparisons and this own-directory report followed. No project
helper import/execution/AST, build/test, Git/CI/network, SDK/process probe, deletion
or background job occurred. One initial instruction-file discovery `find` across
the top workspace returned AGENTS pathnames in T/held directories; this was
disclosed to root. No contents/state from those locations were opened, and all
later reads were restricted to W source/evidence. No new stop obligation arose.

All STOP/NO-RETRY/CLOSED/held-runtime obligations, eight PVD boundaries, protected
refs, signing/Store/publication and occupied1017001 fences remain. Zero new cases,
findings, closures or denominator changes. Candidate-byte review is separate.
