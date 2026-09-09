# Partial Mac helper/workflow/plan: independent source review

Reviewer `/root/native_review`; author `/root/native`; 2026-09-09.
Root authorized this bounded **SOURCE ONLY** review after capture02 review.
**REVISE the two prospective contracts below; current entry remains HARD HOLD.**
**No runner/controller, workflow activation, target or cleanup admission.**

Entire frozen inputs read, with hashes checked before and after inspection:

| Input | LF / bytes | SHA-256 |
| --- | ---: | --- |
| `scripts/audit/macos_native_validation.py` | 373 /16441 | `fd9382c0a7d94eb30cdb48f3c09bf41b1f69926387b837a487f8f6a55b7ff4d8` |
| `.github/workflows/audit-macos-native-validation.yml` | 83 /3777 | `4f6ffba8376203095f14ca4f1ddbb6db126e6b5d6506e3171fdc6d432bbe485d` |
| `reviews/native/MACOS-VALIDATION-PLAN.md` | 410 /27140 | `ef06fbec02816f4b4a087c4c717798151d21d3791fbf55926a779428db434348` |

The plan path is beneath `docs/audit-continuation/2026-09-08-linux/`.
The original338LF plan remains hash-identical in `MACOS-VALIDATION-PLAN.initial-v1.md`
(`fcacf2f9075585995cb511c3de6e83f05782b63f4adc505465bc024c7a651795`).
Its prior qualified scope review and the accepted fixture patch are preserved,
not reclassified as runner acceptance. No reviewed helper was imported, run,
syntax-probed or used for process/filesystem controls. No GET or CI query ran.

## MP-R1 — Prospective XML outcome contract is too permissive

Helper300–325 accepts the one selected `testcase` anywhere below the root using
`root.iter("testcase")`318, then rejects only the literal tags `failure`, `error`
and `skipped`323. Unknown wrappers and namespaced/unknown outcome elements are
not rejected. Given the exact selected first Case and an integer actual_exit0,
this **inert, unexecuted counterexample** reaches PASS_CANDIDATE by source trace:

```xml
<testsuite tests="1" xmlns:x="urn:unaccepted-outcome">
  <unknown>
    <testcase name="passvault_biometric_macos_fixture_normal">
      <x:failure/>
    </testcase>
  </unknown>
</testsuite>
```

The root/count checks pass; omitted suite outcomes default to0, omitted case
status defaults to run, the descendant selector finds exactly one named case,
and the expanded namespace tag does not equal the three literal rejected tags.
The validator therefore calls an ambiguous/nonconforming record a pass candidate.
The actual sealed CTest producer and observed exit may exclude this input. This
is **not** an observed CTest failure, executed parser test or false audit pass;
main never calls this function in the frozen source.

Before any backend relies on it, require the narrow accepted producer schema:
one direct selected testcase, bounded explicitly allowed metadata/log leaves,
and rejection of unknown/namespaced/nested outcomes or structural wrappers.
Do not invent compatibility by excluding legitimate CTest output without a
source/fixture-bound schema. Future separately admitted controls should include
a representative positive and extra/duplicate/missing/namespaced/unknown/failure/
error/skipped/disabled negatives, while keeping actual exit independently bound.
No such parser control is authorized or credited by this review.

The author independently raised the broad XML concern, and then confirmed this
reviewer's specific structure/namespace trace. A future patch needs fresh review.

## MP-R2 — Required tool roles can silently escape record validation

`Toolchain.validate_records`205–208 only validates values that already satisfy
`isinstance(value, SealedTool)`. Dataclass annotations do not enforce those
types. A Toolchain whose otherwise shaped tools/sdk/driver-record are valid but
whose `shell` and `xcrun` are None skips both required roles. `command_vectors`
218–256 never dereferences those two fields, so it can still return vectors.
No command was constructed or executed in this review; this is a source trace.

Explicitly enumerate and validate **all13 required tool roles** as SealedTool
records before returning vectors, rejecting missing/wrong-type roles rather
than filtering them out. Preserve the separate MAC-TOOLS gate: record shape,
even after correction, does not prove installed executable identities, actual
compiler-driver subprocess resolution or normative independent acceptance.
The author independently confirmed the missing-role counterexample. No runtime
backend currently reaches it, and no unsealed target tool was observed running.

## Qualified source credit, not executable acceptance

- Entry352–369 installs local Python cancellation handlers, parses the fixed
  request argument, then encounters the nonempty constant eight-gate tuple.
  It prints HOLD/UNSTARTED and returns78 before request/native input reads,
  tool lookup, namespace allocation or project/process/provider work. Even an
  empty tuple would reach the explicit no-backend exception369, not commands.
  Help/invalid argv can exit earlier but cannot enable target work. This is a
  statement about the frozen source's normal control flow, not an execution,
  installed-Python/startup attestation or proof of arbitrary platform behavior.
- The Cancellation object is not a controller: it records signals, has no
  implemented worker settlement/finalization backend, and stderr output can
  block/fail. The source-leaf reader does perform prospective fd I/O/close,
  not mathematical pure computation; it is not an independently safe ancestor,
  leaf-type-before-open, timeout or cleanup primitive. None is called by main.
- The finite command vectors declare one x86_64 Unix Makefiles configuration,
  two test build targets with parallel1, one inventory and five individually
  anchored CTest selections. No app scheme, Gradle, packaging, installation,
  signing, publication or build1017001 command is present. Paths/digests and
  `driver_chain_acceptance` are only shaped records; source/instance, installed
  versions, expected SDK and make/compiler/shell/subtools must still be sealed
  **before configure**, which itself executes language-detection probes.
- Inventory259–290 checks exactly the five name/argv set members, not CMake's
  registration order. It then returns fixture-normal, early-return, C++-exception,
  ABI, default-security order. Version dictionary equality is not strict numeric
  typing; duplicate equal WORKING_DIRECTORY entries are not independently
  rejected. Neither permits a different case/argv in this trace, but do not
  call it a fully typed whole-inventory schema or observed CTest compatibility.
- Resource328–336 enforces the declared integer12GiB/25% launch and8GiB/20%
  running arithmetic given caller-supplied facts. No Mac available-memory
  observer, periodic sampling, global budget, process/RSS/output bound or
  namespace deletion backend exists. Constants alone implement none of those
  obligations. Nonfinite JSON literal rejection also is not a blanket unused-
  numeric overflow validator; no parser/runtime success is inferred.

## Native reachability and compatibility revalidation

All five helper NATIVE_INPUTS hashes still match the exact previously reviewed
fixture/CMake/production/header/ABI bytes. Fresh focused reads are listed in
the JSON; prior full reads are reused by identity, not counted as new coverage.
CMake57–68 and178–181 declare the three fixtures, security and ABI cases with
the exact vectors; declaration order and desired execution order are separate.
The tests use explicit return checks rather than NDEBUG-elided assertions.

The ABI source supplies null contexts: production enrollment709–714 returns
before LAContext715, and retrieval817–835 wipes output then returns before any
provider path. The default security case uses its newly empty synthetic
`biometric` directory; create567 selects `macos-v1.meta`, distinct from fixture
test metadata. Both retrievals return NOT_ENABLED834–835 before Keychain867.
The later valid metadata/symlink direct-positive controls do not retry retrieval.
No capability/contains/delete or successful enrollment call is selected.

Test-owned LAContext initialization, property reads/writes and invalidate are
real native API activity explicitly permitted in the proposed scope. They do
not prove displayed prompts, capability/enrollment/authentication or hardware
behavior. Existing raw-context/async-launch exceptions, possible blocking and
swallowed-destroy/forced-death limitations remain; default security stays last.
Fixture once-only child cleanup and sibling checks are not process settlement.
The parent/ancestor/ACL gap remains external; no fixture-HOLD child sweep is
authorized. Prior MAC-FIXTURE-001 qualified source acceptance is unchanged.

## Workflow shape and unresolved activation authority

Workflow6–29 declares one request-path push trigger on only the continuation
branch, exact repository/ref/push/not-deleted/attempt1 job guards, one
macos-15-intel job and20-minute bound. No dispatch, PR, tag, release, schedule,
reusable-workflow or protected-branch trigger exists. A path filter is routing,
not a request-only-diff/one-shot/source-acceptance check; those gates are absent.
The request file was absent at passive inspection. Do not create/push it here.

Permissions are contents:read only; no environment, signing/store secret, OIDC,
deployment or write permission is bound. Checkout specifies the activation SHA,
depth2, no persisted credentials/LFS/submodules and no source-clean sweep.
The two action references are literal40-hex immutable pins matching the plan;
this review did **not** remotely resolve or independently attest action contents.
The fixed isolated Python bootstrap path/version is still unobserved/unadmitted.

Artifact routing lists only json/jsonl/log/xml with3-day retention, not application
binaries. Globs do not enforce16MiB/leaf identity/ownership/content bounds, and
the future E/nonce naming and output contract must be reconciled with those exact
paths. An activated hard-HOLD workflow could still allocate a hosted job, perform
checkout and attempt the always-upload step; source refusal is not CI admission.
GitHub's group/cancel-in-progress:false does not coordinate local workers or
prove predecessor settlement and does not prevent pending-run supersession.
Root must hold the single cross-local/CI slot through actual closeout.

All eight MAC-PROC/CTEST/MEMORY/ACL/CLEANUP/TOOLS/CONTROLS/INSTANCE gates remain
open. Source correction, original request/activation S/T and ancestry, tool
identity, fresh real-process/filesystem controls, owned cleanup/settlement and
root admission must be completed and independently reviewed before target work.
No timeout label or hosted disposal substitutes for observed cleanup success.

## Accounting and retained boundaries

Five declared cases; **zero executions, XML results, new declarations or closures**
from this packet/review. The plan explicitly supersedes its historical22/37
snapshot with root's post-PVA03822/38 snapshot, without closing PVA038. Original
19/25 and suspicion2/12 remain separate; eight design explanations are not owner
approvals or readiness percentages. Root's live ledger remains authoritative.
No physical iPhone/Touch ID/Windows Hello, iOS PVA-008 or full PVA-010/PVA-014
provider/packaged/interleaving evidence follows from this preparation.

Capture01 remains consumed FAIL; capture02 source-only correction acceptance
does not authorize GETs. Windows01 remains operational FAIL/filesystem HOLD with
14 cases UNSTARTED and no retry/recovery. PVU-007 STOP, PVU-011 NO RETRY, PVA-029
FAIL/no automatic retry, G7/G8 CLOSED and protected-ref/publication/version/
dependency/identity/build1017001 fences remain unchanged.

Only this compact permanent review pair was created; no generated build/cache,
temporary executable, background worker, request or namespace was allocated.
No deletion, stop, termination or recovery ran. Passive resource snapshot at
2026-09-09T04:02:56Z:23,940,608KiB filesystem available and41,293,300KiB
MemAvailable; not a future admission. Gradle --stop is not applicable here and
discharges no historical obligation. Author will preserve this frozen packet
and review before making versioned corrections; revised bytes need fresh review.
