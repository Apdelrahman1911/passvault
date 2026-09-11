# Checkpoint 18 — Reviewed corrections; focused validation still pending

**INCOMPLETE / NOT READY.** This is source/evidence continuation, not a release.
Published parent C17: `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49` /
`d1bd6ca5b18d08ff3ff15896d9a78af9016be799`. The subsequent publication receipt
records C18's actual commit/tree and push; this document alone does not.

## What has been done

- Continued the existing twenty-agent team with non-overlapping author/reviewer
  lanes. Root remains the only build, CI, Git and cleanup owner.
- Applied ten independently challenged patches across seventeen application/test
  paths: Android32 fixture predicates, credential secret-ownership CAS flow,
  biometric freshness checks under the existing mutex, unchanged SQLite text-order
  extraction, and narrow fixture cleanup/static-analysis corrections. No blanket
  Detekt suppression, assertion removal or new low-value tests. Rejected Main/tray
  v1 proposals remain retained; accepted v2 after-images were verified.
- Independent integration review reconciles ten patches/eighteen edit events/
  seventeen paths/one new file. The current application byte registry has
  thirty-six entries, with the prior thirty-one retained. This is not whole-source
  semantic coverage or a post-change test pass.
- Reconciled Detekt03: fourteen clean module analyses, five failed modules with
  103 diagnostics, two NO-SOURCE groups and root's twenty-one script inputs
  unstarted. Its original stop and disposable cleanup remain qualified successes.
  New source corrections still need the affected-only static check.
- Integrated eleven independently reviewed, UNBOUND inputs for Android crypto
  compilation, focused static analysis and twenty existing regressions plus
  Desktop test compilation. They use one future shared C18 source capture;
  unchanged successful checks will not be rerun merely for reassurance.
- Completed the bounded PVA031 supported-input investigation without a justified
  new patch or conclusive outcome. Existing successful evidence, caller guards,
  counterexamples and remaining framework/input-lifetime qualifications survive.
- Independently checked the proposed Apple alternative without creating another
  speculative workflow: Intel's seventeen target variants and x64 cinterop/KSP/
  dependency support are not established. A concretely available compatible ARM
  host with evidenced resources, or supported x64 closure and a compatible host,
  is needed. The consumed iOS01 failure/ten unstarted cases remain unchanged.

## Failures and cleanup retained honestly

GUI03 stopped in preparation: **143 / original stop0 / terminal70**. All four
GUI cases remain UNSTARTED; zero task headers, cases or XML. Original
`source_after=false` and `cleanup_safe=false` remain unchanged.

A separately source-reviewed current-runtime closer was not invoked: its
read-only prerequisite observation ended **exit1/HOLD**, with sixty-four
unclassified metadata rows and an incomplete scan. Empty retained conflict/
reference lists do not establish absence. Exact failing paths/stages were not
captured; kernel-thread attribution remains a hypothesis. No runtime descent,
deletion, process signal, extra stop or automatic retry followed.

The current runtime remains HOLD. Independent review and root's explicit release
remove only GUI03's prospective publication working-tree/current-index freeze:
the original T config, C17 objects, old index snapshots and evidence stay protected.
This does not admit cleanup/build execution or repair older terminal-evidence gaps.

## Progress — separate defined denominators

| Measure | Qualified completion | Remaining |
|---|---:|---:|
| Original confirmed families | **19/25 — 76%** | 6 |
| All confirmed families | **26/39 — 66.7%** | 13 |
| Original suspicions conclusively resolved | **2/12 — 16.7%** | 10 |
| Original design explanations | **8/8 documented** | Owner decisions separate |

No new closure or application execution from this checkpoint. These percentages
are not overall readiness. Historical continuation XML accounting stays
**199 mixed application-intended/native/regression/investigation/control events
+3 infrastructure +1 producer =203 elements**, not 199 passes. Non-XML control
results and source declarations remain separate.

## What still needs doing and estimated active effort

| Workstream | Required next work / blocker | Estimate |
|---|---|---:|
| Android32 **001**, first priority | Compile crypto instrumentation; then approved usable 32-bit target/image and actual ABI/KDF, create/unlock/backup, minified/ARM32 compatibility. Compilation is not device proof. | **3–5 days after target access** |
| Current Linux corrections | One shared source capture; AndroidCompile02 first, root + five affected analyzers with exact unchanged-source reuse, then 12 Room freshness + 8 credential methods and Desktop test compilation. | **About one engineering day**, if admission/dependencies cooperate |
| Linux **007/027/031/039** | Four still-unexecuted GUI requirements; source changes/coordination need fresh admission, not GUI03 replay. Preserve passing lifecycle/tray evidence and remaining IME/mobile/backend limits. | **1–2 days**, result-dependent |
| Permitted suspicions | Remaining bounded storage/session/chooser/input/provider witnesses; negative source observations are not universal race/deadlock clearance. | **2–5 days**, possibly inconclusive |
| Android **009/030**, Apple **008/014** | Actual-framework tests, isolated synthetic target/storage admission and suitable Apple resources/toolchain. Genuine physical protection/authentication remains hardware-blocked. | **1–3 days per platform after access** |
| Native **010/014/036/037**, PVU008/009 | Remaining provider/lifetime/concurrency/packaged and actual-device evidence. Preserve Mac02/Windows05 successes and cleanup qualifications; no unchanged replay or native-refusal reformulation. | **4–8 days after access** |
| **029** and stopped scopes | Preserve 49 checks: 44 PASS / 5 FAIL, no automatic retry. PVU007 STOP, PVU011 NO RETRY and G7/G8 CLOSED stay non-executable. | **Not scheduled** |
| Final integrity/provenance and external decisions | Applicable remaining migration/fault/package/provenance evidence, independent final report, eight separate PVD choices, legal/accounts/hardware/Store/release boundaries. Authoritative skill validator remains NOT_SUPPLIED_NOT_RUN. | **3–6 days**, external waits excluded |

Overall planning estimate remains **2–4 engineering weeks with useful parallel
work**, not the sum of rows or a promised completion date. External waits,
prohibited scopes and newly discovered defects are excluded. Unfinished local
validation is not disguised as hardware-only work.

## Resume and resource discipline

Use the current three ledgers, `EXECUTION_SLOT.json`, `RESUMED_SESSION_2026-09-11.md`
and `TEAM_20_RESUME.md`. Preserve the handoff checkpoint and all old holds. W is
for edits; only T is for Git, never builds. Publish the exact reviewed nonactivating
selection, then bind one shared C18 source and separately admit each smallest
meaningful validation. No candidate/build 1017001 replacement or protected-ref,
dependency, identity, version, signing, Store or release change.

Recent point observations: about **18 GiB disk free / 39 GiB RAM available**,
no swap. `/tmp` is below the launch disk floor; future build temporary paths must
remain inside their admitted isolated runtime. No audit build/test/CI is active.
Necessary source/tests/compact reports are retained; unsafe held runtimes are not
generic deletion targets. Use JDK17, the checked-in wrapper, one worker,
non-daemon/no configure-on-demand, serial Detekt and intact dependency verification.
Install cleanup before every invocation, preserve evidence, run its original
wrapper stop and verify owned settlement before allowlisted output removal.
