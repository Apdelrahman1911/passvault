# GUI03 C15 Room-first and binding delta: independent source disposition

Reviewer: `/root/native_review`; author: `/root/build_config`; date: 2026-09-10.

**ACCEPT SOURCE DELTA ONLY.** No required correction found within this bounded
ordering/binding review. This is not a whole-runner rereview, execution grant,
cleanup admission, runtime success, or new family closure. Root owns outer-control
alignment and fresh independently reviewed route/instance/execution admission.
`PANEL_ROUTE_ADMITTED` is still unset.

## Exact inert inputs

`W=/root/projects/PassVault/passvault-linux`;
`B=W/docs/audit-continuation/2026-09-08-linux`.
All SHA256 identities were checked on bounded no-follow reads and rechecked before
sealing this report. W has no asserted HEAD/tree; no Git/T/index/store was probed.

| Input | SHA256 | Bytes / LF |
|---|---|---|
| `B/reviews/build-config/integration03/C15-ROOM-FIRST-BINDING.json` | `9f9d61fc66da03863523b3913e8ed9ffb18a727e43e3d777f3d5aeeb03727f11` | 10960 / 153 |
| `W/scripts/audit/linux_desktop_integration_03.py` | `42e4968552fafbecf032211dbd385cac9821e7bc7b79a7b59fa2864d38f55da6` | 85087 / 1403 |
| `W/scripts/audit/desktop_integration_03.init.gradle` | `2b9aec1c7366bff5d39a62f9b1aaefb60e4884866fca3ac309dc4b00940c43fb` | 10645 / 184 |
| `B/reviews/desktop-integration03/source-prepare01/SOURCE.json` | `1a501d764306c1729cef49a411207ca840740d5fe6e284555767182478b55b23` | 1504175 / 1 |
| `W/app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt` | `32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5` | 40561 / 755 |
| `W/shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRoomIntegrationTest.kt` | `a67ba81ad875a4c611208d30b51e40caf87cf5381622f5d6ababf38d2953cff9` | 36660 / 712 |
| `W/app-desktop/src/desktopMain/kotlin/com/passvault/desktop/Main.kt` | `9a67a8a5c2f05331e91ec5c18f6317a3562bd26e2c720105a8a21773edd8ae93` | 7614 / 253 |
| `W/app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopApplicationLoop.kt` | `fc8c553ee02f6c84fc535ea0672436b9688dde6753101f9d952858a05516bfac` | 402 / 10 |

## Independently challenged delta

1. **Only the declared substitutions.** Reversing the record's substitutions in
   reverse order, each with exactly one matching after-image, recovers these full
   baseline bytes entirely in memory (no hypothetical helper file written):
   - Runner: 7 substitutions; `86cd40c4681665cc723afeb3c38f3fda3f82c56bda233c284cd802d55212b4c0`,
     84825 bytes /1403 LF.
   - Init: 2 substitutions; `9bf2cc5f32af4447f9ad38c4104b486b30a40e18cf2e0757da1e4e465d02637d`,
     10645 bytes /184 LF.
   These also equal both raw and checkout helper hashes in the bound C15 manifest,
   independently corroborating the record's original identities. The runner suffix
   from `def require(` through EOF is byte-identical to the reconstructed baseline.

2. **Room precedes Main when both Test tasks execute.** Init lines19-24 place
   shared/editor-room first. Lines120-122 now apply
   `app-desktop:desktopTest.mustRunAfter(shared:desktopTest)`. This is an ordering
   edge, not a dependency that independently schedules Room. Both tasks are
   explicitly requested by unchanged runner lines128-132, whose indexed selection
   now expands to `:shared:desktopTest --tests
   com.passvault.shared.credential.CredentialEditorRoomIntegrationTest` followed by
   `:app-desktop:desktopTest --tests
   com.passvault.desktop.CredentialMainNavHostRoomIntegrationTest`, then `--offline`
   and unchanged flags. No argv was executed. Render's Test graph guard still
   rejects any task set other than those two; prepare still prohibits Test tasks.

3. **Fail-fast is not weakened.** `ignoreFailures=false`, one worker/fork and
   `--no-parallel` remain; `--continue` is absent. There is no new claim that Gradle
   Test's separate `failFast` property was enabled. Shared failure can still leave
   Main UNSTARTED, as intended. The change prevents a later Main failure from being
   the reason Room never starts; it does not guarantee either task starts or passes.
   Separate worker/home roots and task-time Main classpath forwarding are unchanged.
   An ordering edge does not prove inter-task process settlement; original stop and
   whole-owned-namespace settlement duties remain after rendering.

4. **Exactly two prospective cases, tasks and suites, not a three-case batch.**
   Each selected fixture currently declares its one named `@Test`; the selector
   methods/classes/suites and fixture-specific oracle mappings are unchanged.
   Init's exact two-Test graph guard and one-class-per-task filters remain. Runner
   lines1163-1218 map the two module XML paths to their own class and single method
   with `[desktop]`, require exact testcase multiplicity, zero failure/error/skip,
   and equality of observed and expected XML paths. Changing iteration order does
   not change those keyed mappings. Missing/extra/failed/skipped XML cannot become
   success. Preparation's two classpath records are not cases. Main's9 required
   receipt files,3 child roles and13 ordered events are unchanged, not extra cases.
   No tray/lifecycle case or unadopted PVU003 proposal was added.

5. **C15/Main32c binding is coherent as data.** Manifest format is
   `passvault-linux-checkout-source-v1`, with commit
   `8f42274b04e206ff7254ca33d686a9666fce6723`, tree
   `3a8f53dddd54f5c42c34f772975f02619be118d5`,2757 unique members
   (within3196), and the two retained checkout-EOL qualifications. These match the
   new runner literals and record. Both fixtures and both production-loop sources
   match the manifest's raw/checkout SHA256 and sizes; their Git blob IDs also match
   independent in-memory blob-header hashing, not a Git/store observation. Main is
   exactly32c01af..., not failed02 or the later unadopted PVU003 hypothetical result.
   `FROZEN[SOURCE]` and `FROZEN[INIT]` match the actual read images. Historical raw
   C15 helpers remain unbound baseline bytes, whereas W's separately pinned control
   helpers are these reviewed after-images; argv uses W's external INIT. No raw C15
   payload was changed. The manifest's observed-checkout/raw-identity and historical
   EOL qualifications remain intact; this review does not revalidate publication,
   transport, checkout instances or all2757 source files. The substitution record
   explicitly supersedes only old ordering/unbound source fields in historical
   planning prose; none of that prose grants execution admission.

6. **Admission remains closed.** Runner line62 is
   `PANEL_ROUTE_ADMITTED = None`; its only other occurrence, lines1305-1306, requires
   `is True` before umask/resource setup, object construction or batch work. The
   delta does not set or bypass it. Binding source fields is not route/instance
   permission. Outer alignment is separately root-owned and was not reviewed here.

## Activity, evidence and retained limitations

Only bounded plain data/source reads, in-memory comparisons and this new permanent
report were performed. No helper/source import or execution, AST/syntax probe,
build/test/GUI/network/Git command, source adoption, frozen payload edit,
T/index/runtime/process/cache/SDK traversal, or process signaling occurred.
No runtime/cache/temp output or persistent background worker was created; wrapper
stop and build-artifact cleanup are N/A in this lane. Root retains build ownership,
resource monitoring and all outstanding settlement/cleanup duties.

Actual executed tests:0; new findings/qualified closures:0. This is source-delta
review evidence only, not compilation, mock evidence, hardware evidence or runtime
safety proof. Preserve prior passes/failures/rejections/grouped variants, PVU-007
STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED,
Windows05 filesystem HOLD, all consumed/HOLD/retired-store restrictions, the
separately errored native-agent task, PVD/hardware/publication/occupied1017001
boundaries. No old obligation is discharged and no closed scope is reopened.
