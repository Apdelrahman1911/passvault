# Capture04 reconciliation and prospective memory metric

Reviewer `/root/build_config`; inert retained-data/source reads only. No new
network, probe, helper edit/import, build, test, CI, Git, runtime or cache access.

**Result:** captured source supports a narrowly scoped prospective correction:
`(printed Pages free + printed Pages speculative) * printed page size` reconstructs
the kernel's reported free-page bytes. This is not a full available-memory
estimator, an installed-version match, or proof that any runner meets its floor.
Independent challenge is still required before adopting this recommendation.

## Evidence reconciliation

Directory: `reviews/build-config/macos-memory-capture04/`.

- `RESULT.json` independently rehashed to
  `0f3ffaffa63395abdcfe760f2217313e436026dbff729d1ec9ff50422ce04235`.
- `ROOT-ADMISSION.json` independently rehashed to
  `916314a3f3397631517fa0a1a47056f4a9ea481b6f3142a4cf0035048419904e`.
- Receipts record six HTTP200 responses, unchanged final URLs, **214,480 body
  bytes**, all within their caps (aggregate caps 1,736,704), and elapsed **2.489s**.
  Root reports direct-cell exit0. No transport/cleanup operation was replayed.
- All three retained sources independently match recorded byte length, SHA-256
  and Git blob SHA-1 (`blob LENGTH\0` plus exact bytes):

| Retained source | Bytes | SHA-256 | Git blob |
| --- | ---: | --- | --- |
| `xnu-vm_statistics.h.txt` | 35904 | `834b85450d28b76b657f451c9e3eadb35b39a561e269023dadf9e29ca51fd329` | `006575f5e4c75a7a5b6c2f2befc707bd7dc112d8` |
| `xnu-host.c.txt` | 44473 | `a935841c1b49e830c0baa99316e93a6c2004aa7ff65b56790f6a1efacf461198` | `653d1d60a9cf3f4fc192db5134ea4f246717865d` |
| `system_cmds-vm_stat.c.txt` | 7821 | `62980e37ef86e48132e91395b67a5569eb703d3d4e28de5c0cfffea91a7e92eb` | `67938449a0a9c15ffc2f86a4f7d4061c90f04b1a` |

XNU's two literal paths are API-reported at commit
`f6217f891ac0bb64f3d375211650a4c1ff8ca1ea`. The actual printing-source path selected
from repository metadata is **`vm_stat/vm_stat.c`**, tree
`408bba7453608006b89772db185defbac8fe2fd0` in
`apple-oss-distributions/system_cmds`; its commit/release was **not observed**.
Discarded API envelopes cannot be independently rehashed/reparsed here. The
retained API path/commit assertions and blob hashes do not reconstruct Git's
whole tree graph or attest the binaries installed on the earlier macOS runner.

## Accounting proof and limits

1. `system_cmds-vm_stat.c.txt:105–116,127–138,259–267`: default invocation takes
   one `HOST_VM_INFO64` snapshot. It prints the dynamic kernel page size, then
   `Pages free = free_count - speculative_count` and separately
   `Pages speculative = speculative_count`. Snapshot integers are printed in
   full with a trailing period (157–160); interval-mode abbreviations are not the
   helper's selected interface.
2. `xnu-vm_statistics.h.txt:142–174`, especially158–163: speculative pages are
   **already included** in raw `free_count`; the comment identifies them as free
   pages holding speculative disk reads not yet used. The speculative-queue
   discussion at270–273 also describes these pages being stolen first when
   memory runs low.
3. `xnu-host.c.txt:808,818,848`: `vm_stats()` snapshots the speculative counter
   into one local value, adds that value to `vm_page_free_count` for `free_count`,
   and returns the same value as `speculative_count`. `HOST_VM_INFO64` dispatches
   to it at935–944. Therefore adding **the two printed values from the same
   response** reconstructs reported `free_count` without counting speculative
   pages twice. This algebra is not an atomic whole-machine memory reservation;
   other counters can change and host-statistics wrappers include caching paths.

The captured bodies do **not** establish a disjoint, immediately reclaimable
sum involving inactive, purgeable or file-backed counts. Host lines830,845,
871–873 expose separate counters, not such a union proof. Compressor occupied
pages are physical memory already in use; uncompressed/lifetime counters are
not additional free physical pages. Exclude all those categories from the
smallest correction; do not invent a `free + inactive + purgeable` formula.

## Smallest prospective implementation and challenge points

- Keep the current single stock `vm_stat` snapshot command. Parse one page-size
  declaration and exactly one complete integer each for `Pages free` and
  `Pages speculative`; missing, duplicate, malformed or unsupported-format data
  must HOLD, not silently become zero. Do not hardcode a 4096-byte page.
- Compute a clearly named **kernel-free-equivalent** byte count from those two
  printed counters. Retain raw free/speculative counts, page size, derived bytes,
  already-read total physical bytes, launch/running phase and unchanged floors
  before evaluating the gate. Preserve the old unused-free byte value under its
  existing name; do not silently relabel it as the new metric.
- Require positive-total/page-size, nonnegative-counter and derived-bytes≤total
  validation, with exact integer **25% launch /20% running** comparisons and
  unchanged **12/8-GiB** disk floors. Do not clamp an impossible value into a pass.
- This explicitly changes the conservative proxy from unused pages alone to
  OS-classified free pages including speculative cache. It can admit samples
  the old proxy rejected; it is not permission to relabel the old failure or
  weaken percentages. It credits no speculative estimate of all reclaimable RAM.
- Source counterexamples to carry into regression review: speculative=0 leaves
  the old result unchanged; adding speculative to a **raw Mach `free_count`**
  double-counts and is wrong; absent/duplicated fields or sum>total must reject.
  These are source-derived test requirements, **not executed test cases**.

The captured upstream identities are not a mapping to installed macOS15.7.9/
kernel24G830. Prospective parser/metric code, compatibility qualifications and
any new one-shot target admission need independent review. No extra source
fetch/probe is authorized or prescribed by this note. Actual new-runner total,
speculative/free values and resource suitability remain unobserved; historical
Mac5 retained neither total nor speculative count, so no corrected historical
fraction or hypothetical pass can be computed.

Capture03 remains consumed FAIL, exact rejecting field/cause **UNKNOWN**. Mac5
remains failed-before-workload with five UNSTARTED cases and its prior cleanup/
final-exit qualifications. No product tests, closures or hardware credit accrue.
STOP/NO-RETRY/CLOSED and publication/identity/dependency/1017001 fences remain.
Only this compact permanent note was created; no temporary/generated output,
daemon, worker or background job was created or needs a cleanup action.
