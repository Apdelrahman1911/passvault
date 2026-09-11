# Detekt03 — outstanding static checks, one serial cycle

Root sole build/CI owner. **Pending exact independent instance acceptance.**
AndroidCompile01 has priority; this slot cannot overlap it or any audit CI job.

PVA-039 has passed its meaningful real-Compose return/finally regression but still
needs affected-source static checks. PVA-007/031 and the changed Android fixtures
also need static acceptance. Detekt02's coverage failure left all22 analyzers
unstarted. C17 preserves both historical drafts as byte-identical `.kt.txt` files
and removes only their two misleading `.kt` paths. The real source guard and live
tests remain unchanged. One `:detekt --continue` is the existing22-analyzer plus
coverage-task graph, not a full build/test/platform matrix. No Test/compiler/
package task is admitted, no successful lifecycle/tray/control case is repeated,
and there are zero application/JUnit cases in this cycle.

C17 `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49`, tree
`d1bd6ca5b18d08ff3ff15896d9a78af9016be799`, 3042 raw Git members; shared SOURCE
SHA256 `e03d29e54974413ff893f96d9bcf0c70bbf11eeeaf114ac96296987456db1f7f`.
`BINDING.json` records exact literal inverses to accepted C17 unbound controls.
`INSTANCE-FACTS.json` and `requests/LINUX-DETEKT-03.json` bind the current source,
ordinary full index, config/excludes, tools, original lock and parent namespaces.
The accepted shared source/index captures are reused without another Git cycle.

Fixed entry from W, with only this environment:

```text
/usr/bin/env -i PATH=/usr/bin:/bin LANG=C.UTF-8 LC_ALL=C.UTF-8 TZ=UTC
  /usr/bin/python3.12 -I -B -S <B>/reviews/detekt03-outer/LAUNCH.py
```

The pinned inner invokes only the raw checkout's checked-in wrapper, bound init,
`:detekt --continue` and its exact FLAGS/ENV, followed by its original `--stop`.
JDK17, one worker, no daemon/parallel/CoD/configuration or build cache, strict
dependency verification, in-process Kotlin, no SDK/JDK download. Private HOME,
TMP, Gradle, Konan and related user/cache paths; no credentials or real app data.

Fresh R `/root/projects/PassVault/audit-runtime-linux-detekt03`; E
`<B>/runs/linux-detekt03`. Old held runtimes are not accessed. Outer6000s total /
5250s work; static3600s, original stop600s, runtime6GiB, inner96files/64MiB.
At most66 static reports plus optional1MiB Problems HTML share32MiB retention.
Launch12GiB disk/25% available RAM, running8GiB/20%; periodic resource/conflict
checks are point observations, not hard kernel caps or global-idleness proof.

Root will freeze T/source/index/config/excludes/tools, quiesce agent tools,
confirm no audit CI/other local slot, reserve this exact slot and issue once-only
admission after genuine review. Request true coordination/freeze flags are those
future launch conditions, not present reviewer-idleness claims. Outer revalidates
under the original existing lock; that lock is never recreated. The shared CI
snapshot is retained; root has initiated no CI since it and will initiate none
during a local slot. Any intervening conflicting state invalidates admission.

Handlers/finally cleanup precede children. Preserve raw logs, task/input mapping,
source-before/after, all generated static reports before interpretation, original
wrapper-stop results and owned namespace/worker settlement. A static failure
remains FAIL; --continue does not waive it. Only a complete original allowlisted
inventory after evidence/stop/settlement permits new-R deletion. Safe semantic
failure may still clean; uncertainty, cancellation or70 stays HOLD/no automatic
retry. Never delete permanent source/tests/reports, shared caches/SDK/toolchains,
or old held roots, and never signal unrelated processes. Hard interruption and
blocked-syscall limitations remain explicit. Independent actual reconciliation
is required before any finding status or closure changes.

No protected ref/tag/version/identity/dependency/signing/Store/release/build1017001
change. PVU007 STOP, PVU011 NO RETRY, PVA029's recorded failure, G7/G8 CLOSED,
the native-agent refusal and every older HOLD remain unchanged.
