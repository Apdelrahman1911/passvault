# Independent source review: Windows native-14 cohort 02

Reviewer: `/root/native_review`; author: `/root/native`.
**ACCEPT the exact prospective source/admission contract, with the qualifications
below.** Root still owns the fresh source/publication/request and actual shared
execution-slot admission. No request, execution, settlement or test pass is
created by this review. No implementation change is requested.

## Exact reviewed inputs

| Input | LF / bytes | SHA-256 |
| --- | ---: | --- |
| `scripts/audit/windows_native_validation.py` | 1061 / 59191 | `4bbb467ef222292071f693df22cd4fb3af467749d1f05dc3ea07cb94edb19e9a` |
| `.github/workflows/audit-windows-native-validation.yml` | 54 / 2165 | `ac14ac156b2aab5906064f3f6d6a915d6fda43972b2d8faac52461901723cf01` |
| `reviews/native/WINDOWS-COHORT-02-SCOPE.md` | 200 / 12162 | `0bcea8071094d849d013760c46bbb02e7668623763bea7e6e88a2ae5d221c3ee` |
| Native `CMakeLists.txt` | 187 / 6626 | `f72420bc39a5c5dad514b60eb0583880fc329e0a1a57a9b5dd12c8eeeb3c54e3` |

All seven inline source hashes match current bytes. The helper/workflow/scope
were read in full and rehashed unchanged after root released review quiescence.
Current CMake was read in full: Windows still registers exactly the selected14;
the added Apple fixtures do not enter WIN32. Neither install nor packaging is
selected. Reuse the retained native test-scope challenge, SHA-256
`8159311732ae671fce99d4bdaa71d8ae634b031d96c5f930867b26de5a157f86`.
Hashing the remaining native files is not a new full-source semantic review.
No Git/source-commit identity inquiry was performed; the fresh request must bind
its exact reviewed parent commit/tree and the runtime must retain both identities.

## Independent challenge

- **Cohort versus command:** creation-time Job-list assignment occurs before
  suspended children resume. The non-breakaway Job has kill-on-close,16-process
  and3GiB committed-child-memory limits. Read-only preflight requires Job zero;
  configure through CTests intentionally permits owned descendants across phases.
  Immediate-parent exits are stored before fallible Job/log observations. Parent
  return and point active counts are explicitly not whole-cohort settlement.
- **Handles and output:** explicit HANDLE_LIST contains only the current NUL and
  combined stdout/stderr handles. Job/source/root and earlier logs are excluded.
  Original log handles remain open, deny deletion sharing, and every old log is
  included in sampled checks while later commands run and during final waiting.
  Limits are24 logs,2MiB each/8MiB total,512KiB per XML and bounded JSON/journal.
  Retention prefixes share8MiB; incomplete/unsettled logs cannot gain a settled
  full-file hash or permit generated-root deletion. These are sampled bounds,
  not a disk reservation or protection against an adversarial same-user writer.
- **Failure/cancellation:** any command failure prevents later commands. Natural
  final drain is bounded; query, journal, output or resource failure cannot veto
  the one exact-Job termination attempt. Post-stop zero is separately observed;
  kill-on-close alone is not settlement evidence. Cancellation remains sticky,
  and default handlers are restored before terminal return. Runner loss/hard kill
  can still prevent finally/evidence/upload; hosted disposal is not a cleanup pass.
- **Cleanup provenance:** after Job zero and evidence capture, original logs close
  once. All descendant no-follow handles are gathered before any deletion, with
  a5000-entry limit and bottom-up handle-bound disposition. Reparse/non-single-link
  refusal retains lexical owned-relative provenance and same-handle identity,
  attributes, link count and close result, without reading a target or tag.
  Rejection during gathering deletes nothing; failures are not retried. Bootstrap,
  path reads and enumeration remain cooperative, not hostile-writer isolation.
- **Activation/publication:** exact branch/repository/push/attempt1 and fresh
  request-addition-only checks are present; the activation must have exactly one
  reviewed parent. Stable bounded bytes bind helper/workflow/scope/reviewer and
  current native inputs before allocation and again after settlement. Pinned
  checkout/upload, contents-read only, no persisted checkout credential, no
  secrets/environment/release step and3-day compact retention are appropriate.
  A Boolean slot attestation is not cross-host coordination: root must hold the
  actual single local/CI slot through result reconciliation.
- **Resources and scope:** Windows2022/x64, preinstalled Python>=3.11, CMake/CTest,
  VS2022 and SDK10.0.26100.0 are required without installation. Serial native-only
  commands use1-way MSBuild/CTest and disabled node reuse, isolated child storage,
  sanitized environment,12GiB/25% launch and8GiB/20% running floors. Commands share
  the480s deadline, cleanup has a540s cutoff, Actions10min. No Gradle is selected;
  wrapper stop is NOT_APPLICABLE to this run, not discharge of an earlier stop.

## Mandatory interpretation of future evidence

A natural-drain timeout can lead to one successful termination and observed Job
zero without an added error. A subsequent helper return0 means only the declared
commands returned and bounded raw XML awaits independent review; it does not
prove natural descendant exit, benign origin, completed product cases or PASS.

Parent output/project snapshots are not settled hashes. Final PE hashes establish
post-settlement bytes, not by themselves the bytes of every earlier executing
image. Effective compiler commands, named actual XML cases, failures/skips and
cleanup must be reconciled independently;14 registrations and22 intended parent
invocations are not14 or22 executed tests. Missing/empty XML cannot complete the
helper's successful operational state. No custom XML scoring is performed.

A generated-root removal receipt can coexist with a later parent/Job CloseHandle
failure; that is not overall cleanup success. Inspect all `failures`, not only
`original_handle_close_failures`, and reconcile helper/job exit status. The source
keeps such runs FAILED_OR_INCOMPLETE even if the specific root was removed.

## Unchanged restrictions and result

Windows01 run34284083351/attempt1 remains **FAIL/filesystem HOLD,14UNSTARTED**.
No child/reparse cause is diagnosed, old source/evidence is not rewritten, and no
automatic retry/recovery is admitted. PVU-008 remains unresolved; the14-case
successor adds no provider-content-lifetime experiment. Historical red controls,
production allocator-cut injection, fixed CNG known-answer and hardware/Hello/
actual-prompt gaps remain. Eight PVD choices and every existing FAIL/HOLD remain
separate. PVU-007 STOP, PVU-011 NO RETRY, PVA-029 failure/no automatic retry and
G7/G8 CLOSED persist, as do protected-ref/tag/version/identity/dependency/signing/
publication and occupied1017001 fences.

This reviewer only read inert source/data and wrote compact permanent reviews:
no helper execution/import/syntax test, build, test, probe, Git, API, CI or old
runtime traversal. **Zero product cases executed; zero closure-count changes.**
