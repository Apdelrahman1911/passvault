# Bounded team ownership

Root alone edits central ledgers and this file. Agents write their named review
subdirectory and only explicitly assigned source paths; report proposed overlaps
before editing. No agent launches builds/tests or commits/pushes.

| Task | Initial scope | Write authority |
| --- | --- | --- |
| android32 | PVA-001 source/ABI/native target and compatibility evidence gap | Three explicitly authorized, now source-reviewed test/config files and own reports; no execution |
| storage | PVU-001/002/003 remaining admitted source investigation, storage/session guards | Review report; proposed fixes require independent confirmation |
| editor | PVA-007/031, PVU-004/005/006 unfinished UI/lifecycle work | Review report; propose nonoverlapping implementation/tests |
| native | PVA-010/014/036/037 Windows/macOS native test scope and CI evidence | Review report and new dedicated validation workflow/scripts after notifying root |
| build_config | Exact Gradle graph, JDK17, dependency/bootstrap diagnosis, confinement | Review report only; no dependency/version changes |
| verification | Independent 105-method matrix/source binding, runner/admission review | Review report only |
| baseline_coverage | Handoff transport qualifications, independent final coverage/report challenge | Review report only |
| editor_review | Independent PVA-007 findings, patches and integrated source challenge | Own reviewer reports only; no build/test execution |
| native_review | Independent Windows source/admission/activation/result challenge | Own reviewer reports only; no execution or retry |

Additional bounded work: `editor` authors the NEW fixed
`scripts/audit/linux_database_closeout.py` and `reviews/linux-closeout/`;
`baseline_coverage` independently reviews that cleanup contract. Source,
permanent tests/reports, `.git`, the retained checkout and SDK/toolchains are
not cleanup targets. `storage` and root contribute to the NEW fixed Linux
database runner; root's review is owner/coauthor approval, **not a second
independent vote**. `verification` supplies the other-agent source challenge
and separately authored new inert controls. No import or execution is admitted
by any of these assignments; old runners and recovery scopes stay closed.

Pending results and findings are sent to root. Authorial findings do not enter
the confirmed count until challenged; edited source does not count as closure
until the required evidence is independently adjudicated.

## Current bounded follow-ups — 2026-09-09

- `verification` authored NEW controls01 and its distinct v4 correction;
  `storage` independently challenged that correction. `editor_review` reviewed
  root's separate controls02 launcher/instance and reconciled both actual batches.
  Controls01 remains 20 PASS/2 FAIL; controls02 is 22 PASS. Both are consumed;
  neither is application or real-process evidence. Root alone executes.
- `storage` authors the fixed Linux database outer launcher, with root design
  contribution; `verification` is its independent reviewer. The exact database
  request/acceptances and postrun closeout instance remain unfilled.
- `android32` authors only the bounded three-XSD data capture; `build_config`
  independently reviewed source and actual once-only result. The first source
  proposal was rejected before execution. No image download, installation,
  emulator or native KDF is admitted by that result.
- `native` and `native_review` handle the synthetic-only macOS fixture correction
  and independent challenge, respectively. No Apple run or Windows retry follows.
- `editor` authored NAV-FRESH-SUS-001; root independently confirmed narrow
  Desktop PVA-038. Editor may change only VaultRepositoryImpl.kt and the two new
  Desktop BiometricVerificationFailureIntegrationTest.kt and
  BiometricProviderInitializationFaultIntegrationTest.kt files plus own reports.
  `storage` independently challenges the patch and real-provider test oracles;
  no code or test is accepted merely because the finding was confirmed.
- `build_config` authors only new closeout LC-C05/06 source/design controls in
  own review directory; `android32` independently reviews them. Source-only;
  no scratch, imports, tests or cleanup are admitted by this assignment.
- Root authors the new empty database-evidence bootstrap; `verification`
  challenges exact source and separately the filled actual instance. No runtime,
  build or deletion is implied by creation of an empty evidence directory.
- `native` authors only the new five-case synthetic macOS validation plan;
  `native_review` challenges it. Future workflow/helper paths require separate
  root assignment. No Apple CI run or Windows retry is admitted.
- `baseline_coverage` independently reviews the central checkpoint/coverage
  delta. No author report or method count substitutes for runtime evidence.

The previously accepted read-only Git-blob `show` reader may be used only for
explicitly approved, bounded non-stopped source-data lookups after rehashing. Its
successful original transport verification is not a license to execute any
packed program. These data lookups launch no competing builds or tests.
