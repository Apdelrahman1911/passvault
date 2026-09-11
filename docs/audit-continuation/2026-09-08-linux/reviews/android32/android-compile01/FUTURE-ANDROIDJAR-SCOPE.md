# Future androidJar prerequisite: grounded candidate, not admission

Reviewer `/root/android32`; separately identified from consumed Compile01 evidence.

**A narrow future compilation proposal is grounded, but not authorized here.**
The original graph/log names exactly `:core:crypto:androidJar`, reports
`org.gradle.jvm.tasks.Jar_Decorated`, enabled with no prerequisite/finalizer/order
edges, and places it directly in `compileAndroidDeviceTest` dependencies. Main
compilation is also a direct prerequisite. No target-module task action ran.
This makes the one missing ordinary module archive prerequisite concrete, rather
than a speculative platform/test/package matrix or a reason to rerun discovery.

C17 `core/crypto/build.gradle.kts` SHA256
`a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112`
uses the Kotlin multiplatform and Android KMP library plugins and the fixed device
instrumentation configuration. Catalog SHA256
`49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a`
binds Kotlin2.4.10/AGP9.4.0; wrapper remains9.7.1/JDK17. All34 C17 project Gradle
source descriptors were hash-matched and text-checked: no literal `androidJar`
definition. This supports ordinary plugin-origin context, not exhaustive plugin
implementation inspection or proof of archive contents/actions.

A separately authored and independently reviewed future control may explicitly
account for this exact prerequisite within a finite crypto-only scope, retaining
the one compile selector, real main dependency, source/output evidence, verified
plugins/dependencies, strict resources/stop/settlement and fresh isolated cleanup.
Do not allow generic `*Jar`, disable the fail-closed graph guard, skip the task,
change project dependencies, or permit APK/AAR assembly, tests, install/sign/
publish. A Jar class name alone is not an arbitrary-action safety proof; this
attempt observed neither its input/output inventory nor produced archive bytes.
Any future guard/evidence choice must state that limit rather than fabricate
compiler-classpath or runtime/packaging attribution.

Compile01 stays failed and consumed. No existing01 file is changed, no scope is
expanded, no retry/02 adoption/build/SDK action is requested by this note. A new
exact instance, independent source/scope review, coordination and root admission
would be required. Four native KDF cases and physical/security gaps remain open.
