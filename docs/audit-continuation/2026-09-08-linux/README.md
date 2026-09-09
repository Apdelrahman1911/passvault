# Linux continuation — in progress, not a release

Resume from this directory **and** `../../../AUDIT_HANDOFF.md`. The frozen
`docs/audit-handoff/` and `docs/audit-publication/` payloads are preserved. Their
old commands, locks, runtimes and cleanup receipts are not Linux authority.

## Current continuation checkpoint

Read `CHECKPOINT_5.md`, the updated issue/coverage/verification overlays, and
`EXECUTION_SLOT.json` before choosing work. Three grouped PVA-007 freshness
corrections and the Android32 test harness are independently source-reviewed;
**zero application test cases have executed**. The first Windows CI attempt
ran compiler-identification/ABI probes but stopped before product compilation
or any of its 14 tests. Its operational FAIL and filesystem-cleanup HOLD are
independently reconciled; no automatic retry is authorized. Its owned Job
reported zero workers. The cross-host slot was released after reconciliation,
without representing the cleanup HOLD as resolved.

Checkpoint5 records the separately accepted **28/17/6** cleanup-control results,
their original settlement and consumed admissions, **current-C4 targeted167**
scheduling (166 regression methods plus one fixture producer), a narrower Mac5
workflow and independent PVU-006 source narrowing. The original105 selection is
retained unexecuted, superseded for scheduling only. **No new product tests or
qualified closures** are implied. Later sections retain earlier checkpoint facts;
the C5 overlays supersede their pending-control/preparation status, not failures.

Android's installed image is 64-bit-only and was rejected. An official catalog
identifies a 299 MiB API24/default/x86 archive candidate. A once-only three-XSD
data capture and independent schema review establish the catalog's static SHA-1
checksum linkage, not archive integrity, license authority or actual consumer
behavior. Download, staging/cleanup, software-emulation and execution admission
remain open. No emulator, ADB or SDK archive was launched/fetched.

The first NEW Linux runner-control batch remains **20 PASS / 2 FAIL**. Its two
mock-child fixture failures and original reviewer miss are preserved. After a
narrow source correction and separate reviewed admission, controls02 executed
**22 PASS / 0 FAIL**. Both batches used synthetic file/pipe I/O and mocked process
APIs: **zero real children, process signals or application cases**. Their original
scratch/process settlement was independently reconciled; both admissions are
consumed. Exact handoff-source capture, runner/outer-supervisor and generated-output
closeout source reviews are separate from actual database admission. No local
Gradle or old runner has executed.

Additional investigation NAV-FRESH-SUS-001 is independently confirmed as
**PVA-038**: a cold Desktop crypto-loader I/O failure is misclassified as a
rejected biometric key, attempting deletion of valid enrollment. Deletion success
is required for persistent enrollment loss; there is no vault/password deletion
or authentication bypass. Its active correction/tests are outside checkpoint3's
published source scope until separately reviewed. The macOS synthetic fixture
cleanup correction is source-accepted with three added, unexecuted CTest cases.
Existing macOS Gradle/Desktop tests now require an independently admitted
`PASSVAULT_NATIVE_TEST_PARENT`; this is an intentional fail-closed prerequisite.

Checkpoint4 adds the independently source-reviewed PVA-038 correction and its
nine routing declarations plus one provider-fixture producer and three separate
fresh-provider consumer declarations. None compiled or ran. The earlier
checkpoint3 descriptions of active/excluded PVA-038 work are historical, not a
claim that this later patch remained unreviewed.

**Before execution, read
[`publication/CHECKPOINT-3-PUBLICATION-ADDENDUM.md`](publication/CHECKPOINT-3-PUBLICATION-ADDENDUM.md).**
The published b167 closer's `LAUNCHER.json` contract is impossible: one field is
required as both a directory string and an array. Its old acceptance is historical
only; the independent reviewer miss is preserved. A corrected successor, its
meaningful controls and new outer supervisor remain separate pending work.
The database build is still BLOCKED. Empty-E bootstrap01 alone executed once and
was independently reconciled; its completed metadata slot is released, with no
runtime, cache, helper child or application test created. Its original empty E
and 722-byte journal remain needed for the eventual original database admission.

## Baseline

- Fresh clone: `/root/projects/PassVault/passvault` (preserved, no edits).
- Handoff commit: `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`.
- Handoff tree: `05014e9f635131d5db06701e4013b4b5a746465a` (both matched).
- Continuation worktree: `/root/projects/PassVault/passvault-linux`.
- Dedicated branch: `codex/audit-continuation-linux-20260908`.
- Remote refresh on 2026-09-09 at 02:05 UTC: main remains
  `0dbc12c7f1b7770e75963c751c8c67af6e8b057a`, testing remains
  `2ae65df7111a9c5493740e8b772932be77eb98bc`, release and
  `v1.0.7-rc.1017001` remain `61f55216302023d9872aba17546126450e5fbad3`.
  Handoff ref is unchanged; continuation still points to published `f18995e…`
  before checkpoint3. Explicit `--no-tags` fetch refreshed only remote tracking
  branches; candidate tag was read separately, not moved. Exact results are in
  `publication/CHECKPOINT-3-REMOTE-REFRESH.json`.
- The two documented PowerShell checkout-EOL qualifications remain binding.

## Coordination and safety

Root is the **sole build/test execution, Git commit/push and central-ledger
owner**. Agents may read source and write only their assigned source/report
paths. No project script, archived helper, runner import, build, test, Gradle,
native compiler, emulator or application launch is admitted by an assignment.
New findings and fixes require an agent other than the author to challenge
reachability, guards, counterexamples and compatibility. A source review is not
an executed regression. Report manifests and exact source hashes will bind each
checkpoint; no historic evidence will be rewritten.

Permanent fences: PVU-007 STOP (no investigation/reformulation), PVU-011 NO
RETRY (no procedure inquiry/containment relaxation), PVA-029 recorded FAIL with
no automatic retry, G7/G8 execution/recovery/cache/helper scopes CLOSED. The
unadmitted old validation runner must never be executed/imported. All eight PVD
design decisions remain separate; historical password encoding is required.

Only synthetic data and isolated storage are permitted. One audit-owned
build/test job at a time across local and CI. Every invocation needs fresh,
independent execution/environment/coordination/cleanup admission. JDK17, the
checked-in wrapper, one worker, non-daemon, configure-on-demand disabled,
serial Detekt and intact dependency verification are mandatory. CI, if admitted,
is dedicated-branch-only and non-publishing with pinned actions, minimal
permissions, no signing/store secrets and compact short-retention evidence.

## Initial resource observation — not execution admission

Linux x86_64, default Java21, JDK17 exists at
`/usr/lib/jvm/java-17-openjdk-amd64`. Android SDK exists at `/opt/android-sdk`;
no emulator/device capability has yet been established. At approximately
20:32 UTC, `df -Pk` reported 4,697,412 KiB available on the worktree filesystem
and 3,543,848 KiB on `/tmp`; **both are below the 12 GiB launch floor**.
`free -m` reported 23,691 MiB available out of 64,311 MiB. No generated build,
`.gradle` or `.kotlin` output directory exists in the continuation worktree.
No unrelated files/caches will be deleted to manufacture admission.

At that initial observation no project build/test had run. One JSON read-only summary command failed with
`AttributeError` because the issue ledger is a list; a corrected data-only read
succeeded. This is an inspection-tool failure, not an application test.

## Current defined denominators

| Measure | Qualified/conclusive | Remaining |
| --- | ---: | ---: |
| Original confirmed PVA-001–025 | 19/25 (76%) | 6 |
| All confirmed PVA-001–038 | 22/38 (57.9%) | 16 |
| Original PVU-001–012 | 2/12 (16.7%) | 10 |
| PVD-001–008 explanations | 8/8 documented | Owner choices separate |

The all-confirmed denominator increased by one source-confirmed family; no new
qualified closure occurred. Frozen reports with 22/37 retain their historical
scope, not the current global total. These are separate denominators, not overall readiness. Historical passes,
failures, rejected concerns, grouped variants and coverage qualifications remain
in the handoff. Pending work is not silently dropped because Linux lacks a
target or sufficient disk.
