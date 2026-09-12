# DSL-FIX01 — independent source review

Reviewer `/root/c20_remaining_review`; author `/root/c20_remaining_author`.
**ACCEPT_MINIMAL_DSL_IMPORT_CORRECTION_SOURCE_ONLY.** Root integration and actual
compiler acceptance remain separate. No test, compilation, task completion,
closure, execution admission or clean-runtime claim is added by this review.

## Exact inputs

Author packet is `reviews/team20/resume_disk/native_lifetime_author/dsl_fix01/`,
relative to `docs/audit-continuation/2026-09-08-linux` (B).

| Input | Bytes / LF | SHA256 |
| --- | ---: | --- |
| Current `app-desktop/build.gradle.kts` | 63886 /1535 | `0fd4293e5f4741a1a10ef049dc42f24e7f002d2303cb6779087eaa3bacf24ab6` |
| `app-desktop-build.gradle.kts.txt` afterimage | 63918 /1538 | `9263cf5e857f55afd4669aa524eaf36f37d3b5a18f4eca293da148bd55b3e709` |
| `NATIVE-HOST-DSL-IMPORTS.diff.txt` | 2012 /38 | `335e60df67dd3e72841768d8c56990ae86cf5267c9fe62e8d2a0d01bb82f69f0` |
| `CAUSE.md` | 3173 /51 | `9f5ae40e8d6bcdc32977a32c5f8695b9bfa84b94dc57b17ec8de7ba542811447` |
| B`/runs/linux-detekt05/logs/detekt.log` | 41261 /377 | `9d1558f5927ab6011dc71269755c4f14ed9dd16978ba63dec7a1fade568cee25` |

Direct current-to-afterimage comparison matches the submitted complete delta:
three explicit imports and four expression replacements; nothing else changes.

## Independent challenges

1. **Reachability is real, even on Linux.** The retained log reports
   `Configure project :app-desktop`, then script compilation failure at original
   lines1089/1111/1112/1115. The native-platform condition is false on Linux, but
   Kotlin must still type-check that branch before executing script configuration.
   Task laziness, `doFirst`, nine admission properties or the runtime platform
   guard cannot shield this compile error. No native Test action is evidenced by
   this failure. Twelve diagnostics are not twelve failed testcases/defect families.
2. **Cause and counterexamples.** `time`/`nio` qualification fails first; Path
   inference and `isAbsolute`/`toRealPath`/`toFile` errors cascade. This is consistent
   with Kotlin DSL's `java` receiver/accessor shadowing the Java package root, not
   absent JDK17 APIs. The exact generated accessor identity was not inspected or
   compiled here; source review does not promote that implementation detail to an
   independently observed runtime fact. Explicit class imports avoid the ambiguous
   receiver lookup directly rather than relaxing a guard or adding a workaround.
3. **Imports and compatibility.** `java.time.Duration`, `java.nio.file.Path` and
   `java.nio.file.LinkOption` have no conflicting explicit import or local
   declaration in the complete named script. Explicit imports also disambiguate
   any star-imported names. Existing `Files` stays unchanged. The same JDK17 APIs
   remain:120-second `Duration`, `Path.of(String)`, and `Files.isDirectory` with
   `NOFOLLOW_LINKS`; no minimum JDK, dependency or platform contract changes.
4. **Isolation and native scope are unchanged.** The full delta leaves classpath,
   compile dependency, task-name staging exemption, selected class/method,
   fail-on-no-match, one fork/512MiB, timeout, output/cache policy and all nine
   admission properties intact. Absolute/normalized/existing/non-symlink/canonical
   directory checks, distinct home/temp requirement and working directory remain
   identical. No native/provider/lifetime behavior is changed or newly proved.

## Smallest useful follow-through

The original failed compiler result is already the regression's negative evidence;
do not add a mock or dedicated test matrix for these imports. The next separately
admitted affected-project configuration/validation should establish actual compiler
acceptance. Source review alone is not that PASS; retain Static05's failure and its
separate actual-result/cleanup disposition.

Integration changes one complete-source row and its hash. A corrected run must
not silently reuse the old C20 complete-source binding as if the script were
unchanged. Root owns publication ordering and affected-consumer rebinding. This
does not invalidate unrelated historical native successes at their original
identities or require repeating them. Frozen Windows activation constraints stay
binding; this review grants no source/control publication or execution permission.

Activity: named retained-log/source reads, import/declaration searches, full text
delta/hash/length comparisons, author feedback and this own review leaf. No
canonical edit, Git/network, Java/build/test, helper import/execution, live
process/runtime/SDK probe, cleanup or worker/cache/runtime creation. All STOP,
NO-RETRY, CLOSED, native-refusal, prior FAIL/HOLD, protected-ref/build1017001 and
owner-design boundaries remain unchanged. No new PVA family/count is proposed.
