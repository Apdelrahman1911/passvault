# GUI03 four-case: independent final source-delta review

Reviewer: `/root/native_review`. Author lanes: `/root/editor` (fixture/init/inner) and `/root` (outer). This is the completed, bounded review, not a new audit or execution admission.

**Disposition: ACCEPT FINAL SOURCE DELTA ONLY. No remaining required correction was found in this delta.** Root owns adoption and any later admission. C17 source, instance and panel-route bindings remain unbound; this report does not authorize a run, retry, cleanup, publication or a closure.

## Scope and final byte identities

`W=/root/projects/PassVault/passvault-linux`; `B=W/docs/audit-continuation/2026-09-08-linux`. W's Git store is retired: no Git/store/index/T probe was made, and no W HEAD/tree identity is asserted. Paths below are relative to W unless prefixed B.

| Input | Bytes / LF | SHA-256 |
|---|---:|---|
| `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt` | 66405 / 1212 | `cdb4002e4be7ca4c2c7c6dcb42a586ad5429618693c8c38c63bbcfc52302a2c4` |
| `scripts/audit/desktop_integration_03.init.gradle` | 11662 / 194 | `45ba75218a6bf7ebd7ede95c4d9b968a78250e914967e9d5dfc4b46a865d2292` |
| `scripts/audit/linux_desktop_integration_03.py` (inner) | 91342 / 1494 | `d79fb973dacd13b9c2c4ba2b11336ca44ac5d4e73104208c96c9a9a1b56adf8d` |
| `B/reviews/desktop-integration03-outer/LAUNCH.py` (outer, final corrected header) | 54743 / 912 | `9fb9d6a73f07fb928c1bfc7505d29d291f20a5ee5ef6b8524dbced4799ed538e` |
| `shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRoomIntegrationTest.kt` | 36796 / 714 | `291c47edcca2de797d1a669947a2ded246ec5b32c120feec8843a2d41ec9ef57` |

The Room fixture was hash-bound and its one named Test declaration located, not substantively rereviewed. Immediately before sealing, all five inputs and fourteen supporting records below matched their expected exact bytes/hashes via bounded no-follow regular-file reads with stable before/open/after metadata. This is byte binding at review time, not a future execution identity.

## Independent checks and challenges

### Exact delta boundaries

Bounded unified-text comparisons and inverses matched the archived before-images exactly: fixture 2 hunks, init 5, inner 22, cap correction 1, outer 6, then the outer's single three-line header replacement. Intermediate inner `39909c06e6354f78dd4f51c2fa0eedd521bf5bb1c0a7f726499e1f56efb4eba8` (91328 bytes) reverses to the archived inner; final d79 differs from it only in the cap comment/count. Intermediate outer `0e9b14e50d0fb50fe67a953f4a3da0d83e54849e234c9deec3386d919530a750` (54735 bytes) was the behavioral review target; independently undoing only the final docstring replacement from 9fb reproduces 0e9. No hypothetical source was written or imported.

Preserved inner blocks were independently located and hash-matched across the delta: owned settlement/command block `efcf02e5fb2e0c4066e321dd505d3752f2e8f3a42e763c455b6621f885c9c77e` (8491 bytes/149 LF), wrapper-stop block `e7a33f1446f4de9cf595b4e8bb321a87c4d490db4ad85ada06515dc06cbe7222` (429/9), and native isolation/setup/cleanup block `a1c065ee128335827d4c05fbe4e7fac12a507d2817df52eb7730a241966dbd05` (28595/446). Unchanged code is not thereby proved operational.

### Four cases, four XML suites, two tasks

The selection contains four distinct FQCNs, each with one Test method:

- `com.passvault.shared.credential.CredentialEditorRoomIntegrationTest`;
- `com.passvault.desktop.CredentialMainNavHostRoomIntegrationTest`;
- `com.passvault.desktop.Pvu003NativeChooserAdmissionIntegrationTest`;
- `com.passvault.desktop.Pva027MainSettingsTrayPropagationIntegrationTest`.

The same two KMP Test tasks remain; no task clones/new Test types. Existing app `mustRunAfter(shared)` and explicit argv put Room first. The app task selects three classes, uses one fork and fail-fast, with no promise of class ordering. No `--continue`; failures are not ignored. Previously successful lifecycle/tray cases are not selected or rerun.

Inner lines 1226-1282 bind four distinct XML paths to exact suite/class/method identities (including the expected `[desktop]` suffix), one testcase each, without skips/failures/errors, and require the exact observed path set. Each module is inventoried once. Fail-fast siblings that never start are missing evidence, not passing cases. Sharing a Kotlin source file does not make these distinct classes share an XML suite. This is prospective selection/collector source proof: **zero actual test cases executed in this review**. Nine child roles and 27 raw child files are planned evidence, not nine tests or actual executions.

### Mandatory per-case roots and child continuity

The fixture delta changes only case property selection and `runCase` lookup. Under `passvault.mainnav.`, CREDENTIAL requires `runtimeDir/evidenceDir`; PVU003 requires `pvu003RuntimeDir/pvu003EvidenceDir`; PVA027 requires `pva027RuntimeDir/pva027EvidenceDir`. Init binds the three runtime roots `R/mainnav`, `R/pvu003`, `R/pva027` and evidence roots `E/mainnav-evidence`, `E/pvu003-evidence`, `E/pva027-evidence`. Existing explicit opt-ins are enabled only for render.

All six roots are in outer allocation/intake and inner original-directory/emptiness checks; original root permissions remain 0700. The unchanged child builder forwards each resolved case evidence root as the standard child `evidenceDir`, so trace intake uses the correct sibling. Parent stays metadata-only; existing serial seed/Main/verify roles remain isolated from parent AWT/Koin/Room and from a shared child HOME. The previous scenario implementations are not expanded by this root-selection change.

### Three-case collector and outer evidence/cleanup interface

Inner lines 1284-1383 attempt collection once for each of the three fixed Main cases. Errors are preserved per case while other cases are collected once; there is no same-case retry. Missing/unstarted cases with empty allocated roots can be safely recorded as preserved with mapping false, never as passes. Uncertain preservation blocks cleanup.

Each case adopts nine bounded original raw files through the existing descriptor/fsync/pin discipline (per-case bound 811056 bytes). Seed/verify require exact events and zero exit; original Main requires its nine exact events; PVA requires EN1/AR/EN2 plus its two terminal events. PVU full-matches the entire three-line byte receipt:

```
PVU003_CHOOSER_HOME=[01];AFTER_CANCEL=[01];FRAME_DELIVERY=[0-9a-f]\n
DRIVER_ASSERTIONS_COMPLETE\n
QUIT_KEY_CALLS_COMPLETE\n
```

Here `\n` denotes a single LF; the source uses whole-receipt matching, not substring acceptance. Observation values are not forced. The 64 syntactic receipts are not 64 tests. Terminal diagnostics reject mapping; up to one bounded crash copy per role prevents mapping success.

Distinct `MAINNAV-RESULT.json`, `PVU003-RESULT.json`, `PVA027-RESULT.json` records and the saved aggregate image are bound by outer lines 800-825: exact three-row schema, saved image pin/hash equality, case/runtime/evidence paths, preservation/mapping/crash consistency. Code zero additionally requires all mappings true, zero crashes and nine complete images per case. The expanded outer TOP allowlist reaches both new runtime siblings in `remove_runtime` (613-650), while E remains preserved. Existing source/stops/namespace/safety gates still precede cleanup. This verifies source wiring only, not that any future cleanup will settle.

### Original outer deadline is preserved, not renewed

Outer sets `END=START+6000` and `WORK_END=START+5250` (65-66), and emits the original integer work deadline at line 782. Inner lines 403-406 require an exact integer with positive remaining time no greater than 5250 seconds, without resetting it. After prepare, before isolation/GUI, lines 733-746 require 3500 seconds remaining and record GUI-BUDGET: render 2000 + stop 600 + GUI 600 + source-after 180 + settlement 120. Immediately before render, line 1213 requires 2900 seconds: render + stop + source-after + settlement.

The bounded prepare/render/app/shared/stop values are respectively 1800/2000/1500/360/600 seconds; three nominal Main role chains are at most 395 seconds each. Outer lines 800-802 require the unchanged deadline echo; 841-851 bind any attempted GUI budget to the original deadline, exact allowance map and arithmetic. Expensive prepare/setup can leave GUI/Test unstarted; this does not permit a retry or clock renewal. These are cooperative reservations, not proof of hard syscall/native/EDT termination. The unchanged launch path adds no time namespace.

### Finite inner evidence census and resolved objections

I independently read emission sites/fixed loops and counted unique in-memory path strings only:

- 25 fixed commands x log/intent = 50;
- four GUI helpers x log/intent = 8;
- 18 standalone JSON records;
- 27 Main raw child files;
- four selected XML files.

Thus 107 unique successful inner-tracked locations; plus nine possible role crash copies and `(8 total allowed XML - 4 selected baseline)` yields **120**, matching the corrected cap. The extra-four term is relative to a complete selected baseline; a failed run may instead have as many as eight unexpected XML files. Unexpected XML still cannot pass mapping. SOURCE-STOP is not emitted on this wrapper-only path. Outer inputs/outputs and private GUI files are not silently counted as inner-tracked evidence. The separate unchanged 112 MiB aggregate bound does not allow all per-file maxima to saturate together, nor represent total physical E usage. Pending/uncertain writes or closes remain HOLD conditions.

Two finite objections were corrected without erasing history: root identified the stale inner count 93, leading to the author's cap correction to 120; I identified the outer's stale two-case header, leading to root's exact three-line documentation-only correction. Initial author note/patch pins remain intact. FINAL-FREEZE supersedes the inner pin; the final outer pin is explicitly bound in this report rather than inherited from that initial note.

## Retained limits and non-results

Reuse, do not redo, `B/reviews/native-independent/main-followups17/MAIN-FOLLOWUPS17-INDEPENDENT.md` at `1872307a0db317e3f81c71bd709a11e244c4ba5a77dd3dbf8faa2ea105c3e038` (10297 bytes/146 LF). It accepted the prior 96c fixture source-only, later adopted by root. The current root-selection-only delta leaves the scenario/terminal/Room and PVU 156-member block unchanged. PVA sampled properties remain non-atomic and do not prove callbacks, displayed tooltips, continuous registration or persistence; SCROLL_PANE and English/Arabic geometry remain runtime-unverified. PVU chooser-visible Home/cancel evidence cannot prove or disprove the recorded A/V/deletion/deadlock behaviors; a 150-second driver can preclude Escape, and a five-second EDT wait does not settle already-started work.

The older candidate-screen delta is outside this review; reported synthetic screen controls are not live OS screen proof. Both helpers still reference the intentionally unbound C17 `source-prepare01/SOURCE.json`; COMMIT/TREE/MEMBERS/source digest remain None and inner PANEL_ROUTE_ADMITTED remains None. No such manifest, instance, panel route, runtime or process was probed here.

All STOP/NO-RETRY/CLOSED/HOLD restrictions persist, including PVU-007, PVU-011, PVA-029's recorded failure/no automatic retry, G7/G8, Windows05 HOLD, and PVD/hardware/publication/occupied-1017001 boundaries. No archived application/recovery helper or unadmitted old runner was executed/imported. No build/test/CI/GUI/network, runtime/process/cache/SDK inspection, signaling or source edit occurred in this lane. Builds, test cases, runtime verification and new closures attributable to this review: **zero**; no denominator changes.

Only bounded inert reads/comparisons and this new permanent report were produced. No temporary/runtime/cache output or persistent worker was created; wrapper stop is not applicable. Root remains sole build owner and owner of resource monitoring and any separately admitted execution/cleanup/publication. Report creation uses exclusive no-follow mode 0600, file/parent fsync and exact readback/hash; existing files are never overwritten.

## Supporting artifact bindings

All editor paths below are beneath `B/reviews/editor/gui03-four-case/`; outer paths beneath `B/reviews/desktop-integration03-outer/c17-delta/`. These records preserve the before-images, initial limitations and corrections; none was changed by this reviewer.

| Lane / artifact | SHA-256 |
|---|---|
| editor / `BEFORE.json` | `bff90acb34a582d1f8a3c0afad91b4dab2ea99aad36be1164dffa0f9c8747163` |
| editor / `DELTA.json` | `a5df1c888d17686b89952d68dad61aef4bbd945684fc468576d225504c07e67f` |
| editor / `FOUR-CASE.patch.txt` | `e23e676484658fb0fe79137cf6fb2da62bf92ed5e37091ca3f7e4ff548fb4f51` |
| editor / `EVIDENCE-CAP-CORRECTION.patch.txt` | `8dbd8f6cc343fdb91dad8b451f8bf05056e35557ab104c52352a0473d34fa5c7` |
| editor / `FINAL-FREEZE.json` | `b41107e1cfd10b6405a45b8347c89851f98b99758031abd91d1f57ac41afad0d` |
| editor / `AUTHOR-NOTE.md` | `9b5b371b9e9a2a34c4d845d9cacb5f189c5975b87293a5a5451d79928fe2b755` |
| editor / `CredentialMainNavHostRoomIntegrationTest.kt.before.txt` | `96c995d0f74d7df8196fca26770e104ad2d2d2dbb9e1462790e4f7dcca294911` |
| editor / `desktop_integration_03.init.gradle.before.txt` | `2b9aec1c7366bff5d39a62f9b1aaefb60e4884866fca3ac309dc4b00940c43fb` |
| editor / `linux_desktop_integration_03.py.before.txt` | `42e4968552fafbecf032211dbd385cac9821e7bc7b79a7b59fa2864d38f55da6` |
| outer / `BEFORE-FOUR-CASE.py.txt` | `831c2017f7cb5bc53986b77ec9fac3371925a7e803033b1bc7773d52b0a12add` |
| outer / `FOUR-CASE.patch.txt` | `3c51ce319da478139d060c535f66b6f92398c097038fad9042def9337c3673cf` |
| outer / `FOUR-CASE-DELTA.json` | `0e44f75fe583d41a4d36a4c938b871846281cf897a5141d6a20ed7d7178d3b09` |
| outer / `HEADER-CORRECTION.json` | `62d4053c2f7a59ea483a4263c94b4f6c5de0942a2d4acd6d5cd61eb3ab3d4972` |
