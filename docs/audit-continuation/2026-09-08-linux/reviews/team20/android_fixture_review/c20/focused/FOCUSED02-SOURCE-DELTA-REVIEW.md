# C20 focused02 — independent small-delta review

Reviewer `/root/android_fixture_review`; 2026-09-11.
W = `/root/projects/PassVault/passvault-linux`; B = W/`docs/audit-continuation/2026-09-08-linux`.

**ACCEPT_INERT_FOCUSED9_SELECTION_AND_LITERAL_DELTA. NOT ADMITTED:** true C20
source/instance bindings and sufficient exact source capacities remain HOLD.
This accepts the narrow successor preparation, not a build, full-harness safety
recertification, source capture, promotion, current-instance approval or testcase.
No additional harness or algorithm refactor is requested by this review.

## Exact reviewed packet

Author directory: B/`reviews/team20/android_fixture_author/c20/focused-batch/`.
Whole-file hashes bind bytes, not whole-file semantic read coverage.

| File | Bytes / LF | SHA-256 |
| --- | --- | --- |
| `linux_focused_regression_02.py.txt` | 40078 /704 | `1bae5c731ace9d49589caf7198a38bb642cb998e6936b2be7c7ccf9b42ad9d41` |
| `focused_regression_02.init.gradle.txt` | 7675 /117 | `90b828b5d97b3007604792a0196c5394190ce925e1ba2794926d0e7d26d225d8` |
| `LAUNCH.py.txt` | 51207 /861 | `eab843bdf20a06116228e9dd5100e1b567dbf76a3dfc8bd5a09dd506360599e9` |
| `CLASSES.tsv.txt` | 1374 /4 | `e8e707ff65f4a2e286b5a84bfc8d42db49406cfccf6fd9959f66e3ca4f96ae8c` |
| `METHODS.tsv.txt` | 812 /10 | `56840f6aba3a41ef5131c30190924834be430f31e8efef540bcb3bdd34e9fd5c` |
| `FOCUSED02-CONTROLS.diff.txt` | 27145 /308 | `41317fbcd7a2abeefb462b38070046c5feb62b3261c7677bd6a557d1d4f88784` |
| `FOCUSED02-PROPOSAL.json.txt` | 13442 /379 | `bc72c0ceabe228fd588ad4330971f9aecceb17e84e525d94a319cae1053b7382` |

Directly rechecked original baselines:

- `scripts/audit/linux_focused_regression_01.py`: `d84cc3fdfb097fcae00c16c1b18404c7a2cdf638e357d45d6dbc94df15e84715`.
- `scripts/audit/focused_regression_01.init.gradle`: `e945045a8de9db6b8b773b1a49fade926b6dd55494a4f65cc6bc089816c16a79`.
- B/`reviews/focused-regression01-host02/LAUNCH.py`: `7ff5cc456d36dab5f588810dcf782e29defc769a15e6ea3f7c11b4414f5d13a8`.
- B/`reviews/focused-regression01/CLASSES.tsv`: `c973df4ac8a9e0057d01a17f0266b46c4e3c1873fd35cd72185a8cec859bda53`.
- B/`reviews/focused-regression01/METHODS.tsv`: `eed756d95856424716cdce0c7b1dafd3986acd6636b45ea95933ea5e8a1c22b5`.

The reviewer independently generated the five actual file-to-file unified text
diffs in the supplied order: their combined bytes exactly match the sealed diff
(`cmp` exit0). Thus the author's changed-line account is not merely trusted.
All308 diff lines, complete proposal/init/tables, inner490-616/626-686 and
outer425-451/641-663/814-827 were substantively read. Other algorithm bodies are
reused as byte-unchanged baseline text, not re-audited or executed.

## Finite selected work, not old12 replay

Inert TSV/init-literal comparisons independently establish:

- Exactly `DB1,C01,C02`, with method counts `1,6,2`; nine unique qualified names.
- C01/C02 class rows and all eight method rows are byte-identical to the already
  accepted missing8 packet, including source hashes. Their init selectors are
  unchanged; whole-class selection would include other methods and is rejected.
- DB1 is only
  `com.passvault.core.database.CredentialFolderForeignKeyMigrationTest.`
  `Room validation failure rolls back the full upgrade and a clean reopen persists it`.
  Its source declaration at131-133 and package/class match. Neither old FK method
  nor any D03 BiometricUnlockFreshnessIntegrationTest method is selected.
- All nine ordered init method literals exactly equal the ledger-qualified names.
  The DB XML path names only the FK migration class; the two credential XML paths
  are unchanged. No Android host1, static7/analyzer, provider, GUI or native case.

Current selected source hashes were independently checked and rechecked:

| Class | W-relative source | SHA-256 |
| --- | --- | --- |
| DB1 | `core/database/src/desktopTest/kotlin/com/passvault/core/database/CredentialFolderForeignKeyMigrationTest.kt` | `5d80abe70b5667a4c874e65c6b61e56f80625260bbeae8002b3ba0eb3c5de40c` |
| C01 | `feature/credential/src/commonTest/kotlin/com/passvault/feature/credential/presentation/CredentialCustomFieldSaveFreshnessTest.kt` | `68eb476cd47b9cda26c86b1ecb2ce9e64f167770291a818beb41400e96953819` |
| C02 | `feature/credential/src/commonTest/kotlin/com/passvault/feature/credential/presentation/CredentialCustomFieldDraftTest.kt` | `b45ded2bb7e092e73975f7b7d4e77c8e649ae72f1b7f07db9445b47559c176c2` |

For DB1, reuse the substantively read independent
B/`reviews/team20/migration_integrity_review/c20/PROPOSAL-v2-REVIEW.md`,
`d44dd62e03274dd06c7e1d5b6a254873f48c6dd801ea5566af0c89a94b9321eb`, and root's
`C20-KOTLIN-TEST-INTEGRATION.json`, `c80dcd359ae8d23c40fd5930a43c5172ccffb3c45dc672b35d430fd5a90c980f`.
It is the distinct real-Room post-validation whole-chain rollback/clean-reopen
boundary, not a new product defect or observed pass. These three current file
identities are not a full C20 manifest, dependency image or commit/tree proof.

The phase still requests, in order, `:core:database:desktopTest`,
`:feature:credential:desktopTest`, `:app-desktop:compileTestKotlinDesktop`.
The first two are the only Test tasks; the third is the real enabled Kotlin test
compiler and earns zero testcase credit. Unchanged graph/filter checks forbid
CLI overrides, exclusions, dry-run, continue, broad/extra Tests and out-of-scope
execution roots. Success still requires exactly three XML suites with the exact
nine `(fqcn, method[desktop])` pairs, zero failures/errors/skips, one graph record
and three unique executed/didWork/non-up-to-date/non-skipped target states.
Compilation or a graph node alone cannot replace either XML or compiler actions.

## Literal delta and preserved boundary

Actual differences are only distinct02 runtime/evidence/self/init/control/request/
review paths and format/marker tags; the DB selector/row; 20->9 and12->1 count
literals; selected-current hashes; dependent FROZEN hashes; and explicit unbound
C20 source/store/device/original-lock literals. Inner/init/outer marker and format
pairs agree; FROZEN init/ledger/inner entries match the actual proposed hashes.

This is the root-selected **separate fixed cohort**, not the superseded common
cache/lifecycle-manager proposal: R=`/root/projects/PassVault/audit-runtime-linux-focused-regression02`,
E=B/`runs/linux-focused-regression02`. There is one unchanged serial wrapper
build phase and its original stop, under the existing single-PID1 semantics.
No old R/control/request is revived, shared cache adopted, static05 body copied,
or Android resource phase folded into this graph. Names do not prove unusedness.

The complete text diff preserves host screening, containment/ownership, one
absolute clock, cancellation, original-stop provenance, source-before/after,
evidence checks, settlement and descriptor-based cleanup algorithms. In
particular inner658-675 still requires full AFTER and no cancellation before
safety/pass; outer780-782/820 still requires inner safety and full source equality.
No control/evidence point check is substituted for checkout-after proof.
Preservation does not create unconditional safety or timing adequacy: the inherited
750s outer cancellation drain can truncate worst-case settlement; uncertainty
still yields HOLD rather than invented source-after/cleanup credit.

## Mandatory remaining binding/capacity gate

The proposed entry guards cannot accept the placeholder COMMIT/TREE/MEMBERS,
manifest digest, devices or original-lock pin. This was assessed as text only.
No request or genuine `ACCEPT_EXACT_NEW_INSTANCE` is supplied by this review.

Before any runtime admission, root must supply its one final current source image
and exact footprint, followed by the smallest independently reviewed binding/
capacity literals and refreshed hash chain:

1. Exact manifest path/digest, full commit/tree/count and current standalone store;
   current tools/parents/namespaces, real original lock/device pins, and unused
   new R/E facts. Only the original existing build.lock may be bound; never repair
   identity drift by replacing or recreating it.
2. Fix the known incompatible source guards: inner633 and outer647 still cap
   members at3500; outer432/446 cap OID input AND its capture at147456 bytes.
   The root-reported C19 floor3734 already needs153094 OID bytes. That reported
   floor is not a C20 capture; no guessed/padded limit is approved here. Root must
   provide exact C20 member/OID/framed-blob sizes and establish sufficiency of the
   inherited128MiB stream and32MiB individual-file ceilings or obtain the minimal
   separately reviewed capacity delta. These unchanged limits are **not admissible**.
3. Recompute init/tables -> inner FROZEN -> outer FROZEN -> final source hashes,
   then a fresh request/genuine reviewer instance acceptance and root execution
   decision under its quiet serial-cohort allocation. This report is not a quiet
   reservation, runtime ownership proof or permission to test entry rejection.

Reuse the qualified old12 observations under the earlier
`c20/FOCUSED-MISSING8-AND-PARTIAL12-REVIEW.md`,
`4d036c54d749ce60dadffd3040de102a6d4f11844c30c250f899ac3f814db43c`.
A future9 success cannot retroactively make the consumed old20 clean. Its
external70/original stop0/source-after false/cleanup-safe false/R HOLD, and all
other consumed failure/HOLD/STOP/NO-RETRY/CLOSED/native-refusal/occupied1017001
boundaries remain. No case count, closure or central ledger changed.

Only bounded inert source/data reads, hashes/counts, TSV/literal/text comparisons
and this own mode0600 report were performed. No helper import/evaluation/AST,
build/test, Git, network, SDK/tool/process/runtime probe, full source capture,
held-R access, cleanup, promotion, new runner or background/stop obligation.
