# Independent native-14 admission acceptance

Reviewer: `/root/native_review`; helper/workflow author: `/root/native`.
**ACCEPT the exact bounded proposal below for one root-admitted Windows run.**
This is independently authored source/environment/cleanup admission review,
not an executed runner control, application pass, physical-device result or
permission to activate without root's separate source/coordination decision.
`ADMISSION.json` is the normative machine-readable attestation.

## Exact accepted packet

| Subject | SHA-256 |
| --- | --- |
| `scripts/audit/windows_native_validation.py` (39,470 bytes / 719 LF) | `67bcd0aeaa03fa829d55fecfbd640d185163805e4c0a067b281bd807333af243` |
| `.github/workflows/audit-windows-native-validation.yml` (53 LF) | `643fcba6cb393bd6bbd8497cfd03a52ccc43fa217a9d1d728c453d29ddb87b8c` |
| `reviews/native/SOURCE_BINDINGS.json` | `27152712c1ac99ea1fcf739210764b0a5cabf8f45f1edbec2ecb3b143d2ebcb9` |
| `reviews/native/WINDOWS_ADMISSION.md` | `870acc8431b65b59d1534413e6fc09126d9309802dbdc90771327b2c3cb811f2` |
| `reviews/native/REPORT.md` | `ed83393e8bfd19898cf31ce2041fd2bc748c9475f03cef98da558a6f760eb3c7` |

Review paths in the table are relative to this continuation directory. All
seven source-binding path/byte/LF/hash tuples independently match current local
files: six native inputs plus `.gitattributes`. The six native byte hashes are
also listed in `NATIVE-TEST-SCOPE.md`. No new source/member denominator is
created by these checks.

The reviewer fully read the 715-LF intermediate helper
`546b92ab26ad7da97eec73e1f5f22799d9f56dbfa0ecfb1dbfc0e20385e2e09d`,
then read the exact final changes at 418–427 and 576–592. A data-only in-memory
reversal of those changes recreated that exact intermediate SHA-256 and 715
LF, proving no unrelated helper delta was silently accepted. This did not
execute/import/compile the helper. Workflow, bindings and both author documents
were read in full and their hashes rechecked. Underlying test/caller inspection
and its bounded source ranges are separately recorded in `NATIVE-TEST-SCOPE.md`.

## Independent challenge disposition

The first proposal's **REVISE remains preserved** in `ADMISSION-REVIEW-v1.md`.
It is not retroactively accepted and none of its conditional counterexamples
was represented as an executed failure.

| Challenge | Accepted correction / remaining qualification |
| --- | --- |
| Admission before allocation | Request, normative reviewer disposition and exact H/W/B/source bytes are validated from the same stable captures before allocation. Whole authority is reread under original namespace-parent handles before generated-root allocation. Evidence-directory bootstrap remains the explicitly bounded initial mutation, not old namespace adoption. |
| Fork/assignment and inherited handles | JOB_LIST binds the fresh child to the nonbreakaway Job atomically with creation. HANDLE_LIST permits only owned log and NUL handles. Suspended child resumes only after renewed cancellation/deadline checks. |
| Stop/handle failure | One monotone explicit termination attempt; failures/unsettled state cannot issue a second TerminateJobObject. Nested-finally closes original command handles even when termination/wait fails. A failed close is not automatically retried. Job-handle closure is containment, not a fabricated successful stop. |
| Root removal lifecycle | Descendant close attempts are tracked before fallible checks. Root disposition returns the still-owned handle to its caller, which clears its authority variable before close and subsequent receipt/absence checks. Failures stay HOLD. |
| Actual case accounting | The machine tally is now explicitly `passed_cases_machine_scored`, not total executed cases. A failure stops the sequence; available failure XML/logs remain in evidence. Independent reconciliation must count actual executed/failed/skipped/unstarted/unknown outcomes, not infer them from that tally. |
| Compact evidence / RAM | Raw logs stay under the generated root; only a 2 MiB prefix plus observed size and settled full-file hash is retained. A final post-descendant-settlement cap plus bounded `read(LOG_LIMIT + 1)` removes the late-child-burst unbounded Python read. Raw-file size/resource enforcement remains sampled, not a hard write cap. |
| Whole command deadline | Entry, precreation and preresume checks refuse a new command after the global 900-second budget or observed cancellation. The running poll independently checks command/global deadlines. |

The reviewed exact binding JSON is the immutable seven-path source allowlist;
its hash must match both root's request and the actual independently authored
acceptance. The helper does not obtain authority from arbitrary report names or
an author-supplied ACCEPT label. Full helper/workflow hashes bind the fixed
command vectors, case list, SDK, child environment and cleanup implementation;
the normative attestation also spells out those contracts.

## Permitted run and preconditions

Only one dedicated-branch request push for suite `windows-native-14-v1`, attempt
1, on a fresh GitHub-hosted `windows-2022` x64 runner. Root must first commit
this accepted packet, bind that exact source commit/tree, and create a direct
child commit changing only the request. Root separately checks the final
request identity and reserves the sole build slot across **local and CI** until
run termination and evidence/cleanup reconciliation. The JSON slot boolean is
not proof that root actually held that reservation.

The workflow is request-path/branch/repository restricted, `contents: read`,
one job, no automatic cancelling or retries. Official immutable checkout and
artifact pins were independently associated with their public v7 tags as
recorded in the v1 review. No release workflow, signing/store environment,
secret, install/package, tag movement, protected-branch push, store upload,
candidate replacement or application publication is admitted.

Preinstalled Python >=3.11 x64; matching-directory CMake/CTest >=3.25; VS 2022
MSVC C++20; SDK `10.0.26100.0` with the required WebAuthn API8 declarations.
Missing tools/configuration fail without install/fallback. Generated projects
must contain synchronous exception handling, /W4, /WX, /sdl and /guard:cf;
the actual compiler/toolchain/configuration and PE AMD64 hashes are retained.
No JDK/Gradle invocation is part of this run; Gradle `--stop` is NOT_APPLICABLE,
not an old stop-obligation discharge.

Only the exact 14 named registrations in `ADMISSION.json` are permitted:
eight meaningful Win32 writer cases, four actual borrowed-array guard cases,
one real DLL ABI control and one synthetic native security/CNG sibling.
They run individually, serially, with exact-name XML mapping. Their meaningful
oracles, guards, counterexamples and compatibility limits were independently
challenged; the 12 main cases cannot be replaced by compilation or mock-only
assertions. The two siblings cannot be relabeled as Windows Hello or prompt
rendering evidence.

## Resource, cleanup and interruption bounds

- 12 GiB free on checkout/generated volumes and 25% available physical RAM at
  launch; 8 GiB/20% while running, sampled every five seconds.
- One child worker by default; MSBuild node reuse disabled. Nonbreakaway owned
  Job: 16 processes maximum, 3 GiB committed child memory, kill-on-close. Python
  and host memory are outside that Job cap and remain subject to resource checks.
- Configure 120s, build 420s, metadata/inventory commands 30s, each CTest 45s
  outer /30s child, 900s global commands, 20-minute Actions job.
- Unique generated root `RUNNER_TEMP/passvault-native-<run_id>-1`, absent before
  allocation; isolated build/tmp/home/appdata/localappdata/logs. Evidence is a
  separate run-ID directory and never a cleanup target.
- Original namespace parent/root handles; no reparse or multiply-linked
  cleanup entries; at most 5,000 descendants; retain original handles before
  bottom-up disposition, with prior fsynced identity intents. Only the owned
  generated tree is deletable, after Job settlement. Source, permanent tests,
  reports, shared caches, SDKs, toolchains and unrelated files are excluded.
- One explicit Job termination at most; only owned handles are used, never
  names/PID reacquisition/taskkill. Unknown settlement blocks destructive cleanup.
- At most 2 MiB uploaded per log, exact bounded available XMLs, compact JSON/
  journal/toolchain/binary hashes, three-day artifact retention. No binaries,
  PDBs, complete build trees or application archives are uploaded.

This is an isolated, cooperative hosted-runner namespace, **not a hostile
same-user filesystem sandbox**. Initial mkdir-to-handle binding and test-fixture
`remove_all` retain that stated limitation. Inherited runner ACLs are not a
claim about an installed user's vault. Native file tests replace fixture ACLs
using their actual current-token code; only synthetic bytes are involved.

Hard kill/runner loss can prevent finally, durable terminal receipts or artifact
upload. No successful cleanup is inferred from hosted disposal or later path
absence. A late signal can leave an earlier result image while the helper/job
fails; the external job outcome and all available evidence must be reconciled.
No report alone overrides a cancelled/failed run. No retry/recovery namespace
or old STOP/CLOSED scope is authorized by this acceptance.

## Not established

No helper controls, native compile/link, CTest or platform test was executed by
this reviewer. Runtime API/cleanup outcomes remain to be observed once, not
assumed from source review. Even all-green XMLs await independent semantic,
source, command, interruption and cleanup reconciliation. Missing/extra/
duplicate/error/failure/skip cases, failed commands and partial cleanup cannot
be normalized into PASS.

PVA-010 concurrent native lifetime, PVA-014 actual selected-language prompt
rendering, PVA-036 historical red controls, fixed-output KDF vectors, PVA-037
real production allocator cuts and physical Windows Hello remain separate
gaps. No closure/denominator, source-coverage, whole-project readiness or PVD
decision changes follow here. PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no
automatic retry and G7/G8 CLOSED remain binding. Candidate 1017001 is untouched.
