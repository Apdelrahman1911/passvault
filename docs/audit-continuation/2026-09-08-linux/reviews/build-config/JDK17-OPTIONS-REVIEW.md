# Linux worker JVM option ordering: source review only

Reviewer `/root/build_config`; root/storage own the runner and execution gate.
**ACCEPT_BOUNDED_OPTION_ORDERING_CONTRACT; NOT_RUN_ADMISSION.**

Root selected generated, fixed `JAVA_TOOL_OPTIONS=-Xmx512m <confined-properties>`
and an explicit Gradle daemon command-line `-Xmx2g`, rather than ergonomic-only
`MaxRAM`/`MaxRAMPercentage`. No product build-script heap change is implied.

`JDK17-OPTIONS-SOURCE.json` records one bounded HTTPS read of official OpenJDK
`jdk17u` tag `jdk-17.0.20+8`, `src/hotspot/share/runtime/arguments.cpp`:
162,431bytes, SHA-256
`723384a32a668f3a84c799fdc1a3ab0447c41e4595498e7646b81b32a2ad8811`.
The installed JDK release metadata names Ubuntu17.0.20+8, x86_64, but has an empty
`SOURCE` field: matching version numbers are **not binary/source equivalence**.

The reviewed upstream implementation:

- lines2140–55 parses `JAVA_TOOL_OPTIONS` before command-line flags, then parses
  `_JAVA_OPTIONS` last;
- lines2528–38 maps `-Xmx`/`-XX:MaxHeapSize=` to a `MaxHeapSize` assignment;
- lines3278–83 ignores option environment variables under special privileges,
  so this depends on the reviewed ordinary, non-setuid JDK launch;
- lines3290–91 prints the picked-up option string to stderr. Only fixed public
  run paths/options belong in it; do not inherit secrets or arbitrary flags.

Consequently, with the sanitized runner environment, explicit daemon `-Xmx2g`
can override the earlier512MiB inherited heap option. A worker with no later
heap override inherits512MiB; a later worker `-Xmx` can still override it. Require
the actual **owned** worker command/option evidence and reject an unexpected
override. This is not proof that Gradle9.7.1 emits the expected worker command or
that its classpath/configuration is already admitted. The wrapper's own explicit
small bootstrap heap is also distinct from daemon/test heaps.

Exclude ambient `_JAVA_OPTIONS`, `JDK_JAVA_OPTIONS`, `JAVA_OPTS`, `GRADLE_OPTS`,
init scripts, injected properties and toolchain substitution as the runner
requires. Bind private0700 `user.home`, `java.io.tmpdir`, `jna.tmpdir` and XDG
directories before any loader initializes. The subsequent official SQLite JVM
source read in `LINUX-RUNNER-CONFIG-REVIEW.md` shows that this loader does not
consult `org.sqlite.tmpdir`; do not treat its presence as proof.

Heap maxima do not bound native Argon2/SQLite/JNA memory, code cache, metaspace,
thread stacks or total RSS. Root's resource floors/monitoring, sole job ownership,
owned-child cancellation and separately reviewed cleanup remain necessary;
there is no hard OS-memory sandbox claim.

No Java/Gradle/worker process was launched. The read used12seconds/request,
512KiB body cap, verified TLS, explicit empty proxy handling, zero retries and
an outer30second timeout. The HTTP handle/interpreter ended; only compact
source excerpts/hash remain. Wrapper `--stop` is not applicable to this data read.
