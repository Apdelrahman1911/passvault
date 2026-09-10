# Detekt01: independent focused source review

Reviewer `/root/verification`; authors `/root/build_config` (inner/init) and `/root/editor` (outer/PLAN). Review date 2026-09-10.

**ACCEPT_EXACT_FROZEN_SOURCE_DELTA_ONLY. No concrete source blocker found.** This supports the bounded source design, not source/index/tool binding, a genuine request, an execution/coordination/cleanup admission or a successful static-analysis result. All such gates remain pending. No build, test, target import/syntax/AST check, Git, network or runtime/store probe was performed.

## Exact inputs and reuse

W=`/root/projects/PassVault/passvault-linux`; B=`W/docs/audit-continuation/2026-09-08-linux`.

| Frozen candidate | SHA256 | Bytes / LF |
| --- | --- | --- |
| W/scripts/audit/linux_detekt_01.py | `13894d0354fb0e607f588981cb2d12276a5fbba28bc23e890f843ad7202b918e` | 47451 / 816 |
| W/scripts/audit/detekt_01.init.gradle | `5beb8165ae491258c0fae5a5a4f119cbe778157d6a577aacff79fe93fc33ae97` | 2799 / 55 |
| B/reviews/detekt01-outer/LAUNCH.py | `1c5755bcda9c6eb3d83ce6827ec42c0d1cebdc468512593096b391b5391670b6` | 53064 / 905 |
| B/reviews/detekt01-outer/PLAN.md | `f540e81bfa127892bd2293040d2da11233b3df31e3072e7f57009535e953d160` | 15876 / 244 |
| W/reviews/build-config/DETEKT-01-SOURCE-NOTE.md (actual original root-level location, not B) | `61238df18f3fcc14e325db03b2735629afe57744a0635c4fbdf89362f8e0cf63` | 4273 / 70 |

The author's original note was read in place, not edited, moved or copied. The earlier outer PLAN hash `88ceacc93ad2277d6264c5201454a1e61da598926c548ed9529e0d44f2ef5b3b` (15624/240) was read completely. Final PLAN differs only by the disclosed paragraph replacing an unsupported missing-note cause with the exact wrong-prefix diagnostic, original note location/hash and retained no-retry qualification. In-memory reversal of that one textual replacement exactly recovered the earlier PLAN hash; no file was changed by the reviewer.

Baselines rehashed for inert comparison:

- Linux03 inner `W/scripts/audit/linux_isolated_batch_03.py`: `4ceaef097a1db97b673e7233aac5ff77acf817d298c2aa8adc33ec86be075c79`, 37204/669. Complete **679-line textual diff** read. Files/namespace/pidfd/stop/settlement mechanisms are reused, not re-executed or redesigned. The old Test-selection/producer/XML bodies are removed from this new static-only driver.
- Linux03 init `W/scripts/audit/isolated_batch_tests_03.init.gradle`: `6e333500d4a9d56599bd22417d89005d26940240228b1f0710773f5f441d0f45`, 5430/101. Hash reconciled; its Test wiring is not reused. The new 55-line Detekt init was read completely.
- GUI02 outer `B/reviews/desktop-gui02-outer/LAUNCH.py`: `0390e12249d584c333656e80e83f8a353b111fb07ba07c7f6acbf4c32e627398`, 44600/763. Complete **453-line textual diff** read. Reuse accepted transport/supervision/descriptor-cleanup reasoning from `reviews/verification/LINUX-DESKTOP-GUI02-OUTER-SOURCE-DELTA-REVIEW.md`, `1a4784706ffeaafece85b9338d058db23e6bbdf8745a4158d753113205e1cb80` (10164 bytes), which was rehashed/read completely and refers to its accepted curtain predecessor.
- Accepted Linux03 actual review `reviews/verification/LINUX-ISOLATED-BATCH03-ACTUAL-RESULT-REVIEW.json`, `f428020cf798125507c514c8b4d632dc58ab4f401494a29b0eaeb5001799df45` (30443 bytes), was rehashed and its source/reuse fields read. Its prior 166 regressions plus one producer, source qualifications, incomplete resource sample and cleanup disposition remain historical evidence, not permission for this run. No old runtime or store was accessed.

Supplementary current source proof: `scripts/verify-static-analysis-coverage.rb`, `7bc2995f85a77cbe45621592a2584908b8802e412a4f90c15130eb65a7960945` (9488/231), read completely; `build.gradle.kts`, `6c35802a0f779baa1b4d5a65396a07824653761882ddfbfaa718a46c59fa5ee0` (24486/635), relevant imports and lines302-410 read. These are exact inert file observations, not a future source commit/tree binding.

## Reachability, guards and counterexamples

### 1. The actual static guard and task scope are retained

The Ruby guard still invokes bare `git -C root ls-files --cached --others --exclude-standard -z`, rejects invalid/nonregular listed files and applies its existing project/source/security floors. The source hash is checked in the new inner's full raw-source scans. Default Ruby mode is inventory/floors, **not OpenGrep result verification or execution**. Neither filenames substitution, empty inventory fallback nor source/exclusion/rule/dependency edits were introduced.

Root build lines355-393 retain the exact Ruby Exec command and root working directory, make every generic subproject Detekt depend on the coverage task, and make root `:detekt` depend on all generic subproject tasks. The suspected root-only selector problem is therefore rejected by surrounding source, not by task execution. The new init requires only `:detekt`, no excluded tasks, one worker, serial execution, configure-on-demand off, build cache off and strict dependency verification; it requires the exact graph of all generic Detekt tasks plus the unchanged coverage task, no Test, enabled/serial JVM17 analyzers and existing checkstyle/html/sarif reports. It does not replace the analyzer or mutate sources.

Expected declared scope is **22 generic Detekt tasks + 1 coverage task = 23 prospective task events, zero application test cases**. The driver expects actual ordinary success for nonempty source sets and coverage, and NO-SOURCE only for empty source sets. Unexpected/missing/duplicate task events, skipped/cached/up-to-date nonempty tasks or different planned source lists cannot establish mapping PASS. Source lists are recomputed from the raw manifest using the reviewed root Gradle-script/project-src roots and existing excludes. Planned lists remain metadata, not proof the engine visited each file.

### 2. Full-index semantics are explicitly external, not manufactured

The raw checkout stays Git-free. The outer copies one separately admitted <=4MiB ordinary index using exclusive creation, mode0400, file/parent fsync and exact hash/pin readback. The index-copy receipt binds source image/hash and the new runtime image; both layers cross-check the same source/index hash. The external index is not a cleanup target. No helper generates, fabricates or semantically parses an index to satisfy its own declaration.

The 13-key request adds exact Git-inventory and two-tool-alias packets; the genuine seven-key approval remains bound to request SHA and outer/inner/init/SOURCE images. Six reviewed assertions are required literally true, but both plans and source accurately state that they are **not evidence by themselves**. Before execution, an independent review must establish the actual complete ordinary stage0 path/mode/OID mapping against the exact published raw source, including absence of split/sparse/unmerged state; actual standalone/config/exclude/executable-mechanism facts; and the cooperative store freeze. No SOURCE manifest, sealed index input, request or approval was supplied to this review or created by it.

### 3. Config, exclude, tool and alias boundaries

The new publication .git path is fixed, must be outside R/E, is admitted separately and is never a build workspace. Config and optional exclude images are bounded64KiB and pinned. INFO_ABSENT, EXCLUDE_ABSENT and FILE have distinct contracts; absent directories/files are not invented. Both layers recheck the reviewed presence/absence and config/exclude images. Bounded commondir/config.worktree/alternates guards supplement, but do not replace, the required standalone-store/config semantic review.

Exact environment construction drops ambient Git/Ruby configuration. The inner supplies the metadata alias, raw work tree and sealed index; disables global/system config, fsmonitor, untracked cache, hooks, user excludes and credential prompts/helpers while preserving source .gitignore and the reviewed local info/exclude behavior. The outer raw cat-file command uses the pinned real Git image with no replacement/lazy-fetch/filter transport and additional protocol/hook/config safeguards. Local includes, extensions, executable mechanisms and redirected origins remain a real admission question, not proved merely by these switches.

Both layers compare exact /usr/bin/git and /usr/bin/ruby lstat/readlink/resolved-image identities and allow only a regular alias or the exact direct single-hop target. **Outstanding tool gate:** the inner PATH is JAVA/bin:/usr/bin:/bin, whereas the Ruby coverage task and its Git child use bare command names. /usr/bin alias pins do not independently prove those names resolve there. Fresh tool admission must establish absence of earlier JAVA/bin shadows and the actual compatible resolution, or a separately reviewed deterministic-path revision is needed. Both authors were explicitly challenged and retained this requirement; no PATH edit or host probe was made. This source acceptance is not acceptance of the tool binding.

### 4. Mounted alias versus original mountpoint is handled distinctly

Outer allocates the original empty R/git-metadata and sealed R/git-index alongside the12 existing private directories. Inner removes only the future metadata alias from its original-directory expectation before caching that path. It manually validates/closes an FD for the original empty mountpoint, and independently checks the new publication metadata origin before mount setup; it does not cache the empty inode as the future mounted view.

After positive PID1/private pid-mnt/nonpropagating preflight and namespace settlement, four supervised fixed `mount --no-mtab --internal-only` commands bind metadata and index, then remount each ro,nosuid,nodev,noexec. Setup commands explicitly use gradle=False, so no spurious Gradle stop is inferred; Detekt alone retains the original wrapper-stop obligation. The mounted metadata identity is adopted only after setup. Positional mountinfo, exact single alias/index rows, nonpropagation/no nested metadata mount, ST_RDONLY, original index pin/hash and config/exclude identities are checked. The source's before/after checks still forbid a checkout/.git.

Mounted external metadata is excluded from generated-runtime traversal, not from identity/config checks. Read-only aliases do not make the origin globally immutable; outside mutation, metadata redirects and tool changes remain cooperative-freeze/admission concerns. Partial bind failure is a no-retry failure/HOLD, not permission to add generic unmount recovery or transfer old namespace authority.

### 5. Reports, failure preservation and cleanup safety stay separate

The one wrapper command is the checked-in wrapper plus exact init and `:detekt` under JDK17, one worker, --no-daemon, --no-parallel, --no-configure-on-demand, disabled configuration/build caches and strict dependency verification. Original private HOME/TMP/Gradle/Konan/Android-user/XDG locations and fixed Java options are retained. There are no Test/compile/package/release selectors, GUI workers or native tasks added. Gradle-internal configuration work is not being relabeled as an application test.

The new reporter reads the complete bounded detekt log, requires unique expected metadata/events, preserves bounded per-project checkstyle XML, HTML and SARIF under separate E/reports names, and records task outcomes, planned source sets, report hashes and Checkstyle finding count. DOCTYPE/entity declarations are rejected before parse; SARIF must be JSON object data. No-source tasks may legitimately lack reports; missing reports for expected nonempty sources prevent PASS. Unreadable/malformed/oversized/uncertain evidence remains HOLD rather than manufactured success. A graph/configuration/lint failure may yield no analyzer reports; retained log/events and mapping=false do not become execution credit.

After any required original stop, namespace settlement and report preservation precede the phase result. Final safety separately requires raw source-before/after, original stops, complete command/log evidence, original namespace settlement, unchanged inputs and Git bindings/index, no cancellation and readable attempted-Detekt evidence. Success additionally needs attempted Detekt, exact mapping and successful setup/build. Thus ordinary lint/task failure can be cleaned if every safety/evidence obligation is met; failure mapping alone does not require retaining up to6GiB. It remains failure, not PASS.

Outer checks the Detekt-only result contract, original inner/preflight namespaces, Git preflight/index binding and exact static selector/zero test declarations. It requires actual success mapping only for exit0. Before existing descriptor-bound allowlisted R removal, it independently rechecks original request/approval/tools/aliases/config/excludes/source/index, original child settlement and parent mount absence, then verifies the original underlying metadata mountpoint is empty and the original index unchanged. The cleanup allowlist adds only these two new runtime names. Publication .git, external index, E reports, source/tests, SDK/toolchains/shared caches and all old runtimes remain outside deletion scope. Preterminal JSON is not the external process-exit/cleanup proof.

## Pending admission, resources and preserved boundaries

COMMIT/TREE/MEMBERS, source/index/tool/exclude identities, device/lock pins and FROZEN hashes remain None as designed. Outer rejects before packet reads, lock entry, allocations or child launch; inner rejects before its run setup. A fresh binding must supply actual complete raw inventory (<=3196 members), both exact checkout-EOL qualifications, original published store/config/exclude/index proofs, tool/path resolution, and consistent source hashes across both scripts/plans. Filling constants alone is not admission. Root reported C11 selection excludes Detekt; this review does not attach Detekt to C11 or any GUI instance.

Then require fresh exact binding-delta review and a genuine current request/independent instance, coordination and cleanup acceptance. Strict sole_build_owner:/root, agents_quiescent:true and no_ci:true remain; parallel source authoring/review is not execution quiescence. No Linux03 parallel-source scheduling rule is transferred to this new run.

Bounds remain cooperative: outer6000s total/5250s work/750s drain, one original pidfd final kill/wait envelope; raw OIDs128KiB and response128MiB with two streaming verification passes; Detekt3600s, original stop600s including its termination tail, each mount20s nominal plus existing abort handling, shared settlement120s. Entry12GiB free disk/25% available RAM and running8GiB/20% floors remain, now with numeric failure diagnostics. Existing runtime6GiB/count/time limits and incomplete point-sample qualifications remain. Logs/reports cap4MiB each, static reports32MiB, inner evidence96 files/64MiB. No current resource/SDK/tool readiness was probed. Blocked syscalls, parent loss, concurrent hostile root, generic-interpreter screening, global-idle/no-escape and cleanup-interruption limits remain unchanged.

**Zero executions, application cases, new fixes or family closures in this review.** Actual task/log/report, source/index, stop/settlement/resource and external cleanup reconciliation will still be required; static mappings are not engine-visitation, hardware, compatibility-runtime or product-readiness proof. No coverage denominator changes. All earlier successes/rejects/grouped variants/FAIL/HOLD remain; PVU-007 STOP, PVU-011 NO RETRY, PVA-029 failure/no automatic retry and G7/G8 CLOSED are preserved. No retired store/helper replay, protected refs/tags, dependency/version/application identity, signing/store/occupied1017001 operation, or PVD boundary redesign is authorized. GUI02 is unchanged.

## Inert inspection receipts

All reads used inline `python3 -I -B -S` stdlib, bounded ordinary single-link file reads, component-wise O_NOFOLLOW, matching pre/open/post identity and closed descriptors. Whole diffs are textual comparisons, not syntax/AST/compile probes. Receipt chunks: accepted GUI review `8d8761`; initial whole outer PLAN `fb3ffe`; complete outer diff `93264e`/`37b346`; inner interface/init `43b09c`; focused inner contexts `9bae14`/`76a255`/`ae3143`; unchanged Ruby/root-build source `c990c4`; accepted Linux03 reuse fields `bdfb7f`/`cbf608`; author note/final PLAN paragraph `c2ad7a`; complete inner diff/baseline hashes `702126`/`b9b775`/`4295f0`; exact sole PLAN correction `9ff4fe`. All reported exit0. Final report writer rechecks the frozen candidate/note and supporting current source hashes.

Reviewer activity created only this compact permanent report (exclusive0600, file+parent fsync, exact readback/hash). No author file edit/move, temporary output/cache, target execution/import, Git/network, process/namespace/mount probe, old store/runtime access, build/test, background worker, signal or deletion occurred. Reviewer has no newly owned build/test cleanup obligation to discharge; root remains sole execution and cleanup owner.
