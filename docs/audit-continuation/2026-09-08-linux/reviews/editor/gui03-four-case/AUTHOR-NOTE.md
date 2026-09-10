# Explicit four-case GUI03 source delta

Author `/root/editor`, 2026-09-10; independent reviewer `/root/native_review`.
**Actual source changes only; uncompiled/unexecuted and NOT execution admission.**
Root explicitly chose this scope after adopting main-followups17; no inherited two-case expansion authority.

## Exact sources / pointers

F = `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`
- SHA256 `cdb4002e4be7ca4c2c7c6dcb42a586ad5429618693c8c38c63bbcfc52302a2c4`.
I = `scripts/audit/desktop_integration_03.init.gradle`
- SHA256 `45ba75218a6bf7ebd7ede95c4d9b968a78250e914967e9d5dfc4b46a865d2292`.
P = `scripts/audit/linux_desktop_integration_03.py`
- SHA256 `39909c06e6354f78dd4f51c2fa0eedd521bf5bb1c0a7f726499e1f56efb4eba8`.

Exact before-images, sizes, preserved-block hashes and reversible text proof are in `BEFORE.json`/`DELTA.json`.
`FOUR-CASE.patch.txt` SHA256 `e23e676484658fb0fe79137cf6fb2da62bf92ed5e37091ca3f7e4ff548fb4f51`.
No generic Test clone, new method/suppression, second harness, production or outer/T/central edit.

## Minimal case and evidence changes

- **F104-105,1167-1171:** mandatory enum-selected root properties. Original `runtimeDir`/`evidenceDir`
  unchanged; new `pvu003RuntimeDir`/`pvu003EvidenceDir` and `pva027RuntimeDir`/`pva027EvidenceDir`, all
  under `passvault.mainnav.`. No fallback, in-process HOME change or scenario/terminal/Room change.
- **I19-27,72-95,124-132; P77-102,109-153:** same two KMP tasks. Shared Room first; app selects exactly
  original Main, PVU003 and PVA027. App `failFast=true`, one fork; sibling class order is **unspecified**.
  Existing opt-ins `pvu003Chooser=true` and `pva027SettingsTray=true` are explicit. Original worker roots
  stay `mainnav`/`editor-room`; app parents remain metadata-only, child roles have distinct synthetic homes.
- R = `/root/projects/PassVault/audit-runtime-linux-desktop-integration03`, E = this continuation's
  `runs/linux-desktop-integration03`. Preallocate separate empty R/`mainnav`, R/`pvu003`, R/`pva027`, and
  E/`mainnav-evidence`, E/`pvu003-evidence`, E/`pva027-evidence`; intake and pre-render check all six.
- **P1226-1383,1459-1464:** four exact one-case XML suites / two tasks. Each Main case retains nine bounded
  files, three serial roles and its own result JSON (`MAINNAV-RESULT`, `PVU003-RESULT`, `PVA027-RESULT`).
  Events total13/7/9 respectively, not test counts. PVU's complete three-line regex accepts real observation
  values without forcing the hypothesis; PVA requires its three language receipts then original terminals.
  All require matching XML, exit0, original seed/verify, no terminal/crash diagnostics. One failed collection
  still permits one preservation attempt for the other cases, but leaves cleanup HOLD; no retry. Missing/
  skipped failFast siblings are not passes. Original force/fsync/descriptor and native cleanup checks remain.

## Bounds and remaining integration

**I111-114; P188-189,403-406,690,733-747,1213-1218:** prepare1800s, app1500s, render2000s; shared360s,
stops600s and outer work5250/terminal6000 unchanged. Original outer `WORK_END` supplies integer
`OUTER-INTENT.work_deadline_monotonic_ns`; never reset. Pre-isolation/GUI needs3500s remaining
(render2000+stop600+GUI allowance600+source-after180+settlement120), records `GUI-BUDGET.json`;
pre-render rechecks2900s. Reduced prepare/insufficient remainder can leave GUI or tests unstarted,
with no automatic retry. These are cooperative reservations, not admitted aggregate/hard syscall proof.

Root's outer `831c2017f7cb5bc53986b77ec9fac3371925a7e803033b1bc7773d52b0a12add` supplies the field
(line781), but its two-case allocations/count schema still need root's corresponding update and review
(lines57,727,825-828), including deadline echo consistency. That outer is **not usable as-is** with this delta.
P's COMMIT/TREE/MEMBERS/manifest hash stay UNBOUND; shared fresh manifest is
`docs/audit-continuation/2026-09-08-linux/reviews/checkpoint17/source-prepare01/SOURCE.json`.
Current Room source was hash-bound to `291c47edcca2de797d1a669947a2ded246ec5b32c120feec8843a2d41ec9ef57`.

Accepted main-followups17 source/review retains all AX/geometry, property-only/non-atomic/callback/visual/
persistence/PVU cleanup qualifications. Passed lifecycle/tray cases stay excluded. Existing STOP/NO-RETRY/
CLOSED/HOLD/PVD/hardware/publication limits are unchanged. Zero executed tests or closure credit.
Only bounded no-follow data reads/writes/text diffs; no project parser/import/build/process/runtime access.
DELTA preserves the data-writer quoting/literal-guard failures; none executed a runner or modified its source
on rejection. No temporary output/background worker/cache; root owns the next single static/GUI admissions.
