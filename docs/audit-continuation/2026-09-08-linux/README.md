# Linux continuation — in progress, not a release

Resume from this directory **and** `../../../AUDIT_HANDOFF.md`. The frozen
`docs/audit-handoff/` and `docs/audit-publication/` payloads are preserved. Their
old commands, locks, runtimes and cleanup receipts are not Linux authority.

## Baseline

- Fresh clone: `/root/projects/PassVault/passvault` (preserved, no edits).
- Handoff commit: `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`.
- Handoff tree: `05014e9f635131d5db06701e4013b4b5a746465a` (both matched).
- Continuation worktree: `/root/projects/PassVault/passvault-linux`.
- Dedicated branch: `codex/audit-continuation-linux-20260908`.
- Remote observation on 2026-09-08 at 20:30 UTC: main remains
  `0dbc12c7f1b7770e75963c751c8c67af6e8b057a`, testing remains
  `2ae65df7111a9c5493740e8b772932be77eb98bc`, release and
  `v1.0.7-rc.1017001` remain `61f55216302023d9872aba17546126450e5fbad3`.
  Handoff ref is unchanged. Explicit `--no-tags` fetch refreshed only remote
  tracking branches; no protected local branch or tag was moved.
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

No project build/test has run. One JSON read-only summary command failed with
`AttributeError` because the issue ledger is a list; a corrected data-only read
succeeded. This is an inspection-tool failure, not an application test.

## Starting denominators (unchanged until independently supported)

| Measure | Qualified/conclusive | Remaining |
| --- | ---: | ---: |
| Original confirmed PVA-001–025 | 19/25 (76%) | 6 |
| All confirmed PVA-001–037 | 22/37 (59.5%) | 15 |
| Original PVU-001–012 | 2/12 (16.7%) | 10 |
| PVD-001–008 explanations | 8/8 documented | Owner choices separate |

These are separate denominators, not overall readiness. Historical passes,
failures, rejected concerns, grouped variants and coverage qualifications remain
in the handoff. Pending work is not silently dropped because Linux lacks a
target or sufficient disk.
