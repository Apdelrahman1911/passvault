# Detekt01: bounded source handoff

Status: **new source only; independent review and execution admission pending**.
No helper was imported/executed, no build/test/Git/cache/CI operation occurred,
and no runtime allocation was made. GUI02 and historical helpers are unchanged.

## New files

| Path | SHA-256 | Bytes / LF |
| --- | --- | --- |
| `scripts/audit/linux_detekt_01.py` | `13894d0354fb0e607f588981cb2d12276a5fbba28bc23e890f843ad7202b918e` | 47451 / 816 |
| `scripts/audit/detekt_01.init.gradle` | `5beb8165ae491258c0fae5a5a4f119cbe778157d6a577aacff79fe93fc33ae97` | 2799 / 55 |

Text-only reuse: Linux03
`scripts/audit/linux_isolated_batch_03.py`, SHA-256
`4ceaef097a1db97b673e7233aac5ff77acf817d298c2aa8adc33ec86be075c79`;
GUI02 setup-versus-Gradle command distinction grounded in
`scripts/audit/linux_desktop_gui_02.py`, SHA-256
`4d7750ad6342ca85c036b81f111022c2fdb8ccaa7b3909500bbb9d668d2790de`.
No historical helper import, new generic framework, Test fixture, or GUI phase.

## Narrow execution proposal, not authority

- One checked-in-wrapper **`:detekt`** invocation, then the original wrapper's
  `--stop`. Root task covers Gradle scripts missed by the five-module subset.
- Init rejects graph expansion/Test tasks and altered Ruby command wiring;
  checks serial/JDK17/one-worker/strict dependency verification; prints planned
  source/report paths without changing source exclusions, rules, or dependencies.
- Four supervised namespace-only mount commands establish a read-only view of
  the new publication metadata and a read-only sealed private index. No new outer
  mount shim. PID/mount namespace proof precedes mounts; nothing signals host PIDs.
- Exact `GIT_DIR=R/git-metadata`, `GIT_WORK_TREE=R/checkout`, and
  `GIT_INDEX_FILE=R/git-index`; no ambient Git environment, global/system config,
  fsmonitor, user excludes, hooks, credential helper, or prompting. Local config
  and exact info/exclude presence **or absence** still require external review.
- Ruby inventory remains SHA-256
  `7bc2995f85a77cbe45621592a2584908b8802e412a4f90c15130eb65a7960945`.
  It runs its unchanged default inventory/floor mode, **not OpenGrep**.
- Linux03 disk/RAM floors, 6 GiB runtime cap, pidfd supervision, stop and namespace
  settlement are retained. Read-only metadata is excluded from generated-runtime
  traversal. Floor errors now retain numeric RAM/disk values. Compact limits:
  4 MiB/log or report, 32 MiB aggregate static reports, 96 files/64 MiB evidence.
- Cooperative bounds: Detekt 3600 s; stop 600 s including its termination tail;
  each mount command 20 s plus existing abort handling; shared settlement 120 s;
  existing overall workload deadline 6000 s. Syscall/interruption limitations and
  outer supervision remain; these are not hard wall-clock guarantees.

## Remaining binding and review

`R=/root/projects/PassVault/audit-runtime-linux-detekt01`;
evidence `docs/audit-continuation/2026-09-08-linux/runs/linux-detekt01`;
manifest `.../reviews/detekt01/SOURCE.json`. COMMIT/TREE/MEMBERS, source/init
hashes, exact regular Git/Ruby images and optional-exclude state remain `None`.
The approved outer author is `/root/editor`, separately adapting
`.../reviews/detekt01-outer/{LAUNCH.py,PLAN.md}` with the agreed intent/index-copy
and direct-tool-alias contract. No guessed Ruby version or exclude-file creation.

Root must bind the final raw source, ordinary full stage-0 index, fresh metadata
origin/configuration, tool aliases, original directories/lock and cleanup scope.
Boolean review assertions are **not** an independent index parser or proof.
Read-only metadata does not prevent mutation through an outside alias; root's
publication-store freeze is necessary. No retired store may be adopted.

Preserved evidence is static task/input/report evidence: planned source lists are
not per-file engine-visitation proofs, task counts are not test cases, and there
is **zero regression/closure credit** here. Reports and lint failures are kept;
mapping/pass flags are separate from cleanup safety. Bind mounts remain until
namespace exit; outer must independently prove exit, absence of runtime mounts,
original ownership and allowlisted deletion. No automatic retries or relaxation
of STOP/NO-RETRY/CLOSED restrictions is introduced.
