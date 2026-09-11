# AndroidCompile01 — independent inner/init/outer source-delta review

Reviewer `/root/android32`; author `/root/editor_review`, 2026-09-10.
B = `docs/audit-continuation/2026-09-08-linux`. Root alone owns execution/publication.

**ACCEPT REVISED SOURCE DELTA; SOURCE/INSTANCE BINDINGS REMAIN UNBOUND.**
This is not execution admission, a new whole-runner audit, syntax/runtime validation,
compiler success or application/native evidence. Frozen C17 static/GUI work need not
wait for this separate helper. Its later binding can use the same application source.

## Exact accepted source

| Input | SHA256 | Bytes / LF |
|---|---|---:|
| `scripts/audit/linux_android_compile_01.py` | `0410f49fff9a1706cd20a48eb598e6677d9304b7424fc718ec7e870b375155c6` | 62345 / 1023 |
| `scripts/audit/android_compile_01.init.gradle` | `aafa194609929b85ab975de27cdaf5e29766e81f3c02f5d2e21b119d76bc6bee` | 11921 / 180 |
| `B/reviews/android-compile01-outer/LAUNCH.py` | `4f91b1315d6ee29868a42fc1aa08a8713ffdc13dbf34f3f0e7167b277ec79945` | 63470 / 1058 |
| `B/reviews/android-compile01/SOURCE-DELTA.md` | `b32bdcf91f79e00c9f66ad7de5f0d28989aacbbfec0eaebe1c3fc5a12b070694` | 9539 / 136 |

The installed init is independently byte-matched to accepted candidate02. Text-only
extraction matched its exact25 names to the inner's exact25 names, without executing
or parsing either helper as code. Prior init acceptance:
`CANDIDATE02-INDEPENDENT-REVIEW.md`, SHA256
`a2c3ef3527834a97e787883cf2bfb0074c2ebd7b4b8a3eb813babb8955065446`.

## Review method and inherited qualifications

Compared actual source text with static03 inner
`19766a81e97f2d8f7fe214356f6c5e3742b813387fde955de4b729b9198bca45`
and outer `e7a568f3c00a7239e7fd5f51fcbd77a55ec422e94dfd50c19d46a6c36a197359`.
Read the complete outer unified delta, the complete inner change-location index,
all new material regions and relevant surrounding Files/mount/cleanup/finalization
code. The first combined inner diff display was truncated; separate source-range
reads covered the material mount/collector/finalization regions rather than treating
that display as a complete read. No helper import, AST/syntax check or execution.

Carry forward, rather than re-audit or upgrade, root's independent static03 source
review `B/reviews/detekt03/ROOT-INDEPENDENT-REVIEW.json`, SHA256
`11b1e26838379250b5ab5a9fe3a416c834ef3d621db86f21d564cd1c8b291b60`,
including its earlier qualified Files, raw-source/index/store, coordination screen,
pidfd/direct-child settlement, original-wrapper stop and cleanup lineage. Reusing
source mechanics supplies no previous instance's execution/recovery authority.

## Two concrete objections, corrected before admission

Initial inner was `dab69dd52e9b410092ca8a388c00a638c51a2b6f8018a5e9ba894fb0a5d70b9d`
(62047B / 1020LF); initial note was
`be4f01b27c1bc2f0ebc7a2258bf9adbb9240d14347a15e915858771f04882bef`.
The author accepted both independent counterexamples and retained them in the delta.
These are source counterexamples, **not executed failures or new PVA families**.

1. Initial class readback charged32MiB after a read, then caught the failure and
   continued: up to256 ×1MiB reads could occur despite the stated aggregate limit.
   Revised lines870–881 reject exhausted budget before reading, cap each read by
   `min(1MiB, remaining32MiB)` and rethrow the first readback error out of both class
   loops. No subsequent class reads continue after uncertainty. The inherited
   bounded reader's single failing +1 sentinel is honestly qualified, not repeated.
2. Checking nested mounts only below a nonrecursive SDK alias can miss nested
   mounts in the original SDK; the alias can expose shadowed underlying tools even
   when the five metadata files match. Revised lines641–642 also reject nested
   mounts below the original SDK, before alias metadata reads or compiler actions.
   This uses the existing admitted mountinfo inspection, not a new discovery tool,
   recursive bind, fallback SDK or source-path normalization.

Both corrections were independently reread in final bytes. No further source-delta
blocker was found; the init and outer remained unchanged through these corrections.

## Scope, evidence and failure behavior

- One `:core:crypto:compileAndroidDeviceTest --info` invocation, without --continue,
  rerun, root test/check, task discovery, device execution or publication. Its25-name
  upper-bound prerequisite set remains exact; compile-JAR/resource-table exceptions
  are not final APK/AAR/runtime packaging. Ordinary trust in frozen source and
  strictly verified plugins is explicit, not independently proven safe task types.
- JDK17/wrapper/one-worker/non-daemon/nonparallel/no-CoD/no-config-cache/no-build-cache,
  in-process Kotlin and strict verification survive. SDK download is false. Offline
  is a reviewed literal, not an ambient switch or online/offline retry. The original
  stop command remains separate and unchanged; cleanup handling precedes invocation.
- C17 COMMIT/TREE/MEMBERS, source/init/inner hashes, store/index/excludes, installed
  tool images, original lock/device/parent facts and genuine request/approval are
  still unbound. Inner/outer compile read-root/offline literals and init ADMITTED
  must agree through independently reviewed final source bytes. The existing3196
  source-member ceiling was not enlarged. No old instance is revived.
- The original bounded compile log is retained first. Init payload bytes are saved
  before strict UTF-8/duplicate-key/nonfinite JSON interpretation; malformed or
  partial evidence remains failure evidence. Success needs exactly10 roles: graph,
  finished and before/input/state/classes for each compiler—not ten test cases.
- The collector now checks unique finite graph membership, actual task headers,
  MAIN's transitive dependency from DEVICE, enabled bare compiler headers, terminal
  order/completion and non-skipped/non-cache/non-NO-SOURCE states. This addresses the
  init-only review's membership-versus-reachability qualification without new APIs.
- Four expected generic source-input hashes are independently read back. Declared
  output roots stay under crypto/build; empty-before receipts, same-package unique
  base/companions and fresh file/hash/JVM17-header readbacks are required. Files are
  read, not loaded or retained as binaries. Generic task inputs are explicitly
  **not compiler classpaths, variants, argument order or compiler visitation**.
- Original `--info` compiler actions still require independent actual reconciliation.
  Source acceptance and synthetic/mocked receipts cannot remove the uncompiled
  harness gap. Semantic mapping failure can coexist with safely cleaned outputs;
  uncertain byte retention/readback, original stop, ownership or settlement cannot
  silently become cleanup success or an automatic retry.

## SDK alias and cleanup

The new delta uses two fixed20s mount commands for a nonrecursive installed-SDK
bind and ro,nosuid,nodev remount within the already positively identified private,
nonpropagating namespace. It intentionally does not require noexec for compile
tools. Before compilation it requires exactly one alias mount, the required flags,
readonly statvfs state, source-directory identity, no original/alias nested SDK
mount and five bounded metadata alias/source matches. SDK paths retain literal
`android-37.0`; no normalization, copy, installation or license acceptance occurs.

ANDROID_HOME/ANDROID_SDK_ROOT point to the alias. Inventory excludes that installed
view from generated-disk accounting. The outer's added SDK contract/images/authority
rechecks and dedicated SDK-PREFLIGHT bind this new scope. SDK root and metadata are
inputs, never cleanup targets. Only the new empty underlying alias mountpoint joins
the existing allowlist, after proved namespace exit, repeated parent no-runtime-mount
checks, original inode/owner/device validation and empty-underlying confirmation.

An alias is not host-wide SDK write isolation or an immutable full-tool snapshot;
the original path remains visible and outside writers/trusted plugins remain a
cooperative assumption. Neither mount flags nor metadata establish complete tool
provenance, AGP compatibility or legal principal/acceptance. Carry the already
independently challenged ordinary existing-tool-use decision, without reopening or
claiming resolution of the separate new API24-image agreement/copying/target gap.

## Resource/accounting boundaries

Compile900s and original stop600s include their20s TERM/KILL tails. Outer6000s and
work cutoff5250s are inherited source bounds, not the earlier proposed1800s total;
the actual execution admission must name these bounds rather than silently claiming
the shorter proposal. Blocked syscalls/kernel failure can defeat userspace timing.
The4GiB runtime logical-inventory and32MiB durable-evidence thresholds tighten their
predecessor limits; neither is an aggregate kernel RSS cap. Existing launch12GiB/25%
and running8GiB/20% floors, point monitoring and resource qualifications survive.

The inherited4MiB command-log cap includes receipt prefixes and all --info output;
it is stricter than the init's8MiB JSON-payload ceiling. Overflow is retained failure/
HOLD, not permission to raise a bound or retry. Class readbacks are1MiB/file, at most
128/compiler and remaining-budget-capped32MiB plus the one possible failing sentinel.

This review used only bounded source/retained-report reads, hashing, textual diffs,
set comparison and this permanent note. No T/R/proc/SDK/Git/network/build/test probe,
helper import/execution, temporary runtime/cache/binary or background worker. No new
wrapper-stop obligation. All original STOP/NO-RETRY/CLOSED, consumed/HOLD failures,
PVU007/PVU011/PVA029/G7/G8, eight PVD boundaries including lowercase-hex compatibility,
and non-publishing/occupied1017001 restrictions remain unchanged.

**Zero test/native/KDF/hardware cases, new findings, closures or denominator changes.**
Actual compilation, four intended native cases/calls,32-bit process/JNA reflection,
APK/DEX/ABI/minification, business flows and genuine hardware remain distinct gaps.
