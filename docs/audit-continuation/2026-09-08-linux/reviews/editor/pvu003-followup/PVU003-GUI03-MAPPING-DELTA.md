# PVU-003: smallest future GUI03 infrastructure mapping delta

2026-09-10; `/root/editor`. **SOURCE PROPOSAL ONLY; no current source/helper edits.**
This is a separate future one-case scope, NOT an addition/retry to integration02/03 or C15.
The actual-Main adoption proposal remains frozen: patch `1bec90498dfee2375b74bcab23053214755a9a504fe943352c538084d04de23a`,
hypothetical fixture `8a666a5df0c303d50ab57aa4ef51907f7c3a06ef69f6c7679519d5e992867f6c` (not written).
Native independent proposal acceptance: `reviews/native-independent/pvu003-followup/`
`PVU003-ACTUAL-MAIN-ADOPTION-INDEPENDENT.md`, `a2eb8d102a3dd1efbfb8a6dc2de484c505e643eaa00bd10b8fe990c521426c8a`.
That does not independently accept the new mapping proposed here or admit execution.

## Exact source inputs (line references below use these images)

| Alias | Path | SHA256 |
|---|---|---|
| inner | `scripts/audit/linux_desktop_integration_03.py` | `86cd40c4681665cc723afeb3c38f3fda3f82c56bda233c284cd802d55212b4c0` |
| init | `scripts/audit/desktop_integration_03.init.gradle` | `9bf2cc5f32af4447f9ad38c4104b486b30a40e18cf2e0757da1e4e465d02637d` |
| outer | `reviews/desktop-integration03-outer/LAUNCH.py` | `e59be5da85e9f52e3e6e0f6e9b764fe3b12e01da023b4f838bcfbf9c6fa65ca2` |
| plan | `reviews/build-config/integration03/PLAN.md` | `31c3e98929ddaa42713adac411f661d2090840a508fee3925a9560a3f86def66` |

`reviews/` is under this continuation directory. These were read as bounded inert text, never imported.

## One selected case: concrete fixed-key changes

1. **Inner76-89 / init19-24:** retain only the app-desktop selection; remove shared/editor-room.
   Set `class` / sole `selectedClasses` to
   `com.passvault.desktop.Pvu003NativeChooserAdmissionIntegrationTest`, `suite` to
   `Pvu003NativeChooserAdmissionIntegrationTest[desktop]`, and sole `methods` entry to
   `actual Main samples Home while the export chooser is modal then cancels`.
   Keep `fixture` pointing to `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/`
   `CredentialMainNavHostRoomIntegrationTest.kt`: the proposed class shares that file/private primitives.
   Bind its actual independently adopted source, not the current32c01 parent or an unmaterialized hash.
2. **Init78-86:** retain actual Test `classpath.asPath` forwarding and existing mainnav runtime/evidence
   properties. Add only in the render/app block:
   `test.systemProperty('passvault.mainnav.pvu003Chooser', 'true')`.
   Do not put it on the Gradle client or infer it from the existing synthetic-display property. Without
   this flag the new case is ignored, and the unchanged XML no-skip oracle must reject that as success.
3. **Inner114 / outer57:** `WORKERS` contains only `mainnav`; retain derived private children.
   Keep `R/mainnav`, `E/mainnav-evidence`, `MAINNAV_ROLES`, all nine role files, per-file/aggregate/crash
   caps and ownership/settlement/adoption code. These suffixes are reused ONLY beneath fresh future roots.
4. **Inner128-132:** render argv contains just `:app-desktop:desktopTest --tests` plus that exact new class,
   then `--offline` and unchanged FLAGS. Remove `SELECTIONS[1]` references; do not use `--continue`.
   **Init120-122:** remove the absent shared-task `mustRunAfter` reference. Init87-92's Room-only branch
   can be removed; no credential Room fixture may remain selected. The same preparation task design
   still resolves/compiles the single selected Test runtime without running Test. Keep graph exactness.
5. **Inner656-657:** require exactly ONE runtime-classpath record (currently hard-coded2); retain the
   task/class Counter and all artifact guards. Rename its prefix together with init166 for the new scope.
   **Init181-182:** selected-task equality remains; wording becomes one fixed Test task, not two.
6. **Inner1213-1215,1369 / outer727-732:** case/suite/task declarations and success predicates become
   **1/1/1**, including `preserved_xml_suites == 1`. Update the old Main+Room qualification text.
   Expected source XML is `app-desktop/build/test-results/desktopTest/`
   `TEST-com.passvault.desktop.Pvu003NativeChooserAdmissionIntegrationTest.xml`; preserved name prepends
   `app-desktop--`. Exact case name has `[desktop]` suffix. Retain zero failures/errors/skips and rejection
   of extra XML/cases: role processes, Gradle tasks and receipt combinations are not additional tests.

## Mandatory receipt delta: old exact13-event mapping would reject the new case

Inner100-105 keeps seed/verify tuples; its new main terminal tuple is exactly
`('DRIVER_ASSERTIONS_COMPLETE', 'QUIT_KEY_CALLS_COMPLETE')`. The main producer includes one additional
fixed-shape observation line, so the total across roles becomes **7**, not13. At inner1277-1284, replace
only the expectation construction and its mapping term with this concrete bounded variant (inert code):

```python
expected = {role + '.events': ('\n'.join(MAINNAV_EVENTS[role]) + '\n').encode()
            for role in ('seed', 'verify')}
expected.update({role + '.exit': b'0\n' for role in MAINNAV_ROLES})
main_match = re.fullmatch(
    rb'PVU003_CHOOSER_HOME=([01]);AFTER_CANCEL=([01]);FRAME_DELIVERY=([0-9a-f])\n'
    rb'DRIVER_ASSERTIONS_COMPLETE\nQUIT_KEY_CALLS_COMPLETE\n',
    contents.get('main.events', b''),
)
observation = None if main_match is None else {
    'home_selected_while_chooser_showing': main_match.group(1) == b'1',
    'home_selected_after_native_cancel': main_match.group(2) == b'1',
    'frame_delivery_bits': int(main_match.group(3), 16),
}
self.mainnav_ok = (observed == set(MAINNAV_FILES) and not diagnostics
                  and main_match is not None
                  and all(contents.get(name) == value for name, value in expected.items())
                  and all(not any(marker in contents.get(role + '.log', b'')
                                  for marker in MAINNAV_LOG_FAILURES) for role in MAINNAV_ROLES))
```

Keep this AFTER whole owned GUI settlement and original-descriptor evidence adoption, where the present
body already runs. Inner1285-1292's existing `MAINNAV-RESULT.json` gains `observation: observation`, changes
`ordered_events_required`13 to7 and replaces dirty-Back claims with modal Home/cancel qualification.
Keep `declared_cases=1`, `child_roles_planned=3`, the nine required files and the existing XML conjunction.
Missing/malformed/extra lines preserve failure evidence but cannot map true. No outcome Boolean is forced:
HOME=true is sampled modal selection; AFTER alone is post-cancel selection; both false is a bounded
counterobservation.64 syntactically valid receipts are **not64 tests**, schedule trials or a deadlock result.
The existing outer typed Main/XML preservation and mapping checks remain; only its declared counts/text
change. No second receipt file, logger, native observer, lifecycle owner or harness is necessary.

## Fresh identity, bounds and what stays untouched

Root must choose NEW runtime/evidence/private-display/source-manifest/request/approval/helper paths,
RUN/PURPOSE/RUN_ID, format strings, init mode/task names and matching classpath-record prefix. None is
allocated or execution-admitted here. COMMIT/TREE/MEMBERS, FROZEN images, tool/namespace/lock/parent pins
and panel-route admission must be freshly reviewed/bound, not copied from current03. Existing sources
and C15 selection stay unchanged; apply the fixture and mapping only after separate adoption/review.

Keep Main's panel/systray/window/instance-lock infrastructure, original native-owner capture/settlement,
JDK17/one worker/fork, strict verification, flags, stop/cleanup ownership and all current time/resource
bounds. No second Room Test task does NOT remove the three real Room/Main child roles. The 150s driver
clock also caps cleanup: expiry/focus loss can prevent Escape; 5s EDT waits do not settle begun work.
No cleanup retry, deadline expansion, forced-dispose success or release of an old HOLD follows.
Heap limits are not RSS caps. Retain disk/RAM monitoring and floor checks in the sole root-owned run.

With only one expected XML, the plan's source-derived success inventory84 becomes83 (same commands/helper
pairs/metadata/nine role files). Keeping the existing maximum8 failure XML gives83+3 crash+7 extra XML=93:
no evidence cap increase. The112MiB inner cap stays. The same six normal outer files yield89 success and
conservative100 including failure extras and optional CANCEL. These are prospective ceilings, not observed
files or tests; a final adopted helper delta still needs independent count/cleanup review.

Only this small permanent note was written. No source/helper changes, execution/import, AST/syntax check,
Git, SDK/device/process/cache/native probe, runtime/temp/cache outputs or persistent worker. The first
combined textual display truncated; narrowed fixed-source reads completed missing mapping lines.
Foreground readers settled; wrapper stop N/A. Root remains sole build/Git/ledger/cleanup owner.
All STOP/NO-RETRY/CLOSED/consumed/HOLD scopes, PVU-007, PVU-011, PVA-029 no automatic retry, G7/G8 and PVD
boundaries remain. **PVU-003 unresolved: this is Linux software evidence preparation, not hardware-only
blocking or proof about pre-chooser/post-dismiss admission, Job survival, A/V cycles or durable harm.**
