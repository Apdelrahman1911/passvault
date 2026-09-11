# AndroidCompile02 — minimal prerequisite proposal

Author `/root/android_compile_author`, 2026-09-11. **UNAPPLIED, UNBOUND, SOURCE ONLY.**
Independent counterpart: `/root/android_compile_review`. B is
`docs/audit-continuation/2026-09-08-linux`; all paths below are W-relative unless noted.
This is a new execution02 candidate, not the historical Compile01 init candidate02.
Root alone may release the GUI03 freeze, integrate, bind, admit or execute anything.

## Deliverables and frozen text bases

Only these three `.patch.txt` proposals and this note were written in this lane's
directory. Each patch is a standalone **new-file addition from `/dev/null`**. Full
source text is intentional: applying a copy/delta patch must not modify consumed01.
No proposed `.py`/`.gradle` destination, request, approval, capture or runtime was created.

| Proposal in this directory | Proposed new destination |
|---|---|
| `ANDROID-COMPILE02-INIT.patch.txt` | `scripts/audit/android_compile_02.init.gradle` |
| `ANDROID-COMPILE02-INNER.patch.txt` | `scripts/audit/linux_android_compile_02.py` |
| `ANDROID-COMPILE02-OUTER.patch.txt` | `B/reviews/android-compile02-outer/LAUNCH.py` |

Existing Compile01 base identities, verified by source-data hashing (not execution):

| Base | SHA256 |
|---|---|
| `scripts/audit/android_compile_01.init.gradle` | `c6e7c4f36ce9909414be548e867cb11bf9186f0dc09c5246b44c82cd62a00ffe` |
| `scripts/audit/linux_android_compile_01.py` | `e4f31a57a16d91dc956969621f238c37b8acfee258dee9a6c7d2f333b50218b6` |
| `B/reviews/android-compile01-outer/LAUNCH.py` | `bb3cc62765f128a0122f2b0d614a38f731985402bbde5dd21766a5fcec33f54b` |

Proposed payload identities are **review locators, not populated runtime bindings**:

| New payload | Bytes / LF | SHA256 |
|---|---:|---|
| Init | 12826 / 195 | `fccb4952a892ca5d0a223a6484d63497000b76e4a08eae8dc260d8534729bcd9` |
| Inner | 63316 / 1038 | `a10b713e5b7a760475d7a87ed11ee7d9cc9bf4e106f29604c01369b238cfcaf6` |
| Outer | 63570 / 1059 | `c43deecd539de80c530b391257bac5caea9de394c333aa75a1236df3fa88a962` |

| Patch | SHA256 |
|---|---|
| Init | `bf68a4646a253e28b53717242a35f37b406fd4909936d828a231d3dbdf140758` |
| Inner | `ddc62e40fc8aec8fab7e9d6c844f78e77e606509314717e1ba83ff53e80516f4` |
| Outer | `fa109886f52a4856123c77f35ffbc543bb55873bdda6fc8b1d7d9c3a16a8befc` |

## Exact semantic delta

1. Add only `:core:crypto:androidJar` to both identical finite25 task-name sets:
   they now contain26 unique paths; no old name is removed. Init graph ceiling64
   tightens to26, inner header domain25 becomes26, and inner observed graph2..25
   becomes3..26 because MAIN, DEVICE and the exact Jar must all be present.
2. Before task actions, require this exact Jar enabled with concrete type
   `org.gradle.jvm.tasks.Jar_Decorated`, zero dependency/finalizer/mustRunAfter/
   shouldRunAfter edges, and only one incoming relationship: DEVICE dependencies.
   DEVICE must also directly depend on MAIN. The collector independently requires
   the same structure, within its existing semantic-mapping failure block. No
   wildcard Jar, skipped task, dependency rewiring or relaxed Test/Exec guard.
3. Preserve exactly10 successful receipt roles: graph, finished, and four roles
   per compiler. There is **no eleventh Jar receipt or invented archive proof**.
   Raw graph/header evidence retains the Jar; both compilers still must actually
   succeed freshly with pinned generic source inputs and fresh JVM17 class readback.
4. Use fresh02 run/helper/init/outer/request/marker/receipt-format identities.
   All COMMIT/TREE/MEMBERS/FROZEN/exclude/device/original-lock bindings are `None`;
   readRoots/offline are `None`/`null`. All four required source hashes, including
   the still-frozen Android fixture, are `None`/`null` in inner/init.
   New fail-closed hash-shape guards run before inner intake or init task actions.
   No old source, instance, approval, SDK/tool image or cleanup authority is adopted.

The source reference `B/reviews/android-compile02/source-prepare01/SOURCE.json` is
an **unbound placeholder**, not a required new capture or a claimed source identity.
Root may bind an appropriate reviewed current capture after freeze reconciliation;
any path/pin changes need review. Proposed request is
`B/requests/LINUX-ANDROID-COMPILE-02.json`; the independent approval reference is
`B/reviews/team20/android_compile_review/INSTANCE-ACCEPT.json`. Naming the paired
reviewer is a future role constraint, not an acceptance. Neither packet was authored.

## Grounding, limits and unchanged protections

Grounding is the retained `B/reviews/android32/android-compile01/ACTUAL-REVIEW.json`
and adjacent `FUTURE-ANDROIDJAR-SCOPE.md`, not a fresh discovery/retry. Direct JSON
comparison confirms the saved14-node graph contains the exact leaf/type/incoming
edge above, with DEVICE also directly depending on MAIN. In particular, **do not
invent Jar-to-MAIN ordering or claim that this Jar contains production classes**.

Compile01 remains a consumed envelope failure before target task actions: zero
compiler successes, test/native/KDF/XML cases or compilation credit. Its collector's
`main_transitive_dependency_observed=false` was an unevaluated default after the
two-receipt failure, not a disproof of the graph's real MAIN dependency. Original
independently accepted cleanup applies only to that attempt; no01 file is rewritten.

Exact task/type/edges do not establish arbitrary-action safety, Jar input/output
inventory, archive contents, compiler classpath/visitation, or native execution.
Carry the existing ordinary frozen-source/strictly verified-plugin trust, plus
`HELPER-SOURCE-INDEPENDENT.md`'s qualified Files/SDK/namespace/cleanup lineage. The
new source has not been parsed, compiled or executed; compatibility remains untested.

No lifecycle/resource/mount/stop/cleanup semantic change is proposed. Preserve the
single DEVICE selector, JDK17/wrapper9.7.1/one-worker/non-daemon/no-CoD/no-cache/strict
verification flags, no SDK/JDK acquisition, existing read-only SDK operation,
source/index checks and genuine fresh admission. Keep 3196 source members maximum,
compile900s/stop600s/outer6000s/work5250s, 12GiB/25% launch and 8GiB/20% running floors,
4GiB logical runtime threshold, 4MiB command log, 32MiB evidence/class-readback bounds,
and inherited point-sample/userspace/cooperative-trust qualifications. Semantic
mapping refusal remains distinct from uncertain retention/ownership/stop/settlement;
the latter cannot silently authorize cleanup. No automatic expansion or retry.

This proposal changes no application/dependency/version/build identity, SDK/license,
APK/AAR/device/signing/Store/publication scope. Physical Android32/native KDF/API24
consent/target gaps remain separate; source work is not a surrogate. Preserve C17/T
freeze, old HOLD obligations, PVU007 STOP, PVU011 NO RETRY, PVA029 FAIL/no automatic
retry, G7/G8 CLOSED, the native-agent refusal, eight PVD limitations and occupied1017001.
Zero findings/closures/suspicion resolutions/denominator changes are claimed.

## Author activity qualification

Only inert text/JSON/hash/diff comparisons were used; no project/helper execution,
import, AST/syntax checks, build/test/Git/CI/SDK or process probe occurred. An initial
overbroad AGENTS filename locator traversed project-container directory metadata,
including held/T paths; only W/AGENTS contents were opened. This deviation was
reported to root and not repeated. No held/T/private contents were inspected or
modified, and no cleanup/stop obligation arose. Later reads were explicit W source
and retained evidence. Final proposal readback confirmed new-file-only patch
envelopes, equal26-name domains with only androidJar added, and no retained literal
40/64-hex source pins. These are source-data comparisons, **not runtime test passes**.
