# macOS resource metric follow-up — source/data only

Author: `/root/build_config`. No helper edit/import/execution, runner query,
network retrieval, build, test, or Git mutation. This note does not admit a run.

## Result

**A narrow evidence correction is justified; a reclaimability-metric replacement
is not yet justified by the available captured sources.** Preserve the 25%
launch/20% running floors and the consumed Mac5 failure. Missing evidence is not
proof that the runner had enough memory or that the guard was incorrect.

### Exact existing evidence

- Helper `scripts/audit/macos_focused_validation.py`, SHA-256
  `4448517d4615a34090d2acc61cd8eb872d3e8c1445fd711e8d7aa146f243b69c`;
  still matches the admitted source.
- Mac5 `runs/macos-focused-01/result.json`, SHA-256
  `f5eff0cd7251d755d542b566a1972e359369ce409e70c41b31bbcd23c2666d1d`.
  Run `34333087199`, source commit
  `96f7758de9984528f4624944c0594d47fc20f14b`, tree
  `7167789b46cb29b198aed70eb5e21d9eabc4d5ec`.
- Existing independent result review, SHA-256
  `c6b9f37d076f64dd33ca6bab5a5626172937a4960f9b7d613019777bd2fa2d69`:
  `reviews/native-independent/MACOS-FOCUSED-01-RESULT-REVIEW.json`.

The sole retained sample is free physical **3,195,535,360 bytes**, disk free
**116,283,990,016 bytes**, elapsed 6.729s. Commands are empty and all five product
cases remain **UNSTARTED**. This is zero executed product cases, not five failures.
Preserve helper exit 1, qualified generated-root cleanup, and the unsatisfied
`PENDING_REQUIRE_ACTUAL_HELPER_EXIT_ZERO` finalization qualification.

### Reachability and the narrow evidence defect

Helper lines 399–401 read `sysctl -n hw.memsize` into positive `self.total_ram`,
but do not retain it. Lines 218–230 multiply the parsed `vm_stat` page size by
`Pages free`, append only the derived free/disk values, then apply:

`free > total OR disk < disk_floor OR free * 100 < total * percent`.

The launch floors are 12 GiB/25%; running floors are 8 GiB/20%. `command()` checks
launch resources before its command record or `Popen`. Thus the recorded failure
is reachable before configure/build/tests. Disk exceeded its floor; the missing
total prevents independent recomputation of the fraction or differentiation of
the two rejecting RAM subconditions. Do not infer total from a runner label.
This omission was already identified by the independent result review above;
it is not a new product finding or closure.

For a **future separately reviewed helper revision**, retain the already-read
total, page size/free-page count, metric name, launch/running phase, exact floors,
and failed predicate(s) **before** rejecting the sample. This needs no extra
runner command or changed arithmetic. Keep original evidence immutable. Logging
alone cannot justify replaying the unchanged failed workload.

## Why no substitute metric is accepted here

The current free-only reading is a deliberately conservative proxy, not a
demonstrated available/reclaimable-memory metric. Keeping the numeric percentages
while increasing their numerator is not automatically equivalent protection.
Before adopting an additional page category, source evidence must establish its
physical/reclaimable meaning, units, non-overlap, and relationship to the exact
tool's printed free count. In particular, do not blindly sum free, inactive,
speculative and purgeable counters; do not replace bytes/total with an unexplained
pressure percentage, total RAM, process memory allowance, or a third-party
estimate. No purge, pressure-inducing action, cache eviction, or unrelated-process
termination is an acceptable way to pass this gate.

The existing source inventory names Apple XNU `osfmk/mach/vm_statistics.h` and
`osfmk/kern/host.c`, but **does not contain verified captured bodies**. Capture01
failed with zero verified responses/source files; capture02's qualified status
remained source-only with wrapper/instance/network/execution HOLD. Its reader
and archived commands were not imported or replayed for this review. A proposed
source path/header-field name is not authoritative accounting evidence.

## Smallest actionable next step / precise blockers

1. The evidence-only logging change above can be authored and independently
   challenged on Linux without launching a runner; do not count it as completed
   until implemented/reviewed.
2. A metric change first needs bounded, independently reviewed **official source
   or documentation** defining the selected OS/tool's accounting, including any
   free/speculative transformation and reclaimable-category overlap. The two
   already-named XNU paths are relevant but a header alone is insufficient;
   the actual `vm_stat` printing/accounting definition is also missing. No new
   retrieval is authorized by this note, and no source version is invented.
3. Even with that proof, live available/total memory and observer/tool compatibility
   on a fresh intended macOS runner remain unobserved. The existing result cannot
   establish them, and a new runner's suitability cannot be promised from Linux.
   This is a later, separately admitted platform-evidence requirement—not a reason
   to repeat the unchanged failed job or create a probe campaign now.

No new tests, closures, runner-readiness claim, or weakened resource floor follows.
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED,
physical-hardware gaps and publication/dependency/identity/1017001 fences remain.
Only this compact permanent note was created; no generated output, cache,
temporary file, daemon, worker or background job requires cleanup.
