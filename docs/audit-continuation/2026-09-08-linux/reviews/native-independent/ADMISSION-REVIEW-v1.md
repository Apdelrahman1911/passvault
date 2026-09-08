# Independent Windows execution admission review — v1

**REVISE — NOT ADMITTED.** Reviewer `/root/native_review`; author `/root/native`.
This is a source review of a new proposal, not execution/recovery of an archived
runner, a product finding, an executed negative control, or a test result.

Fully displayed subjects:

| Subject | Lines | SHA-256 |
| --- | ---: | --- |
| `scripts/audit/windows_native_validation.py` | 597 | `cf42afe0b0d78bcbe01f9eb3b74819c319fd74bb78df099d1f0f8f545ab9cb89` |
| `.github/workflows/audit-windows-native-validation.yml` | 53 | `643fcba6cb393bd6bbd8497cfd03a52ccc43fa217a9d1d728c453d29ddb87b8c` |

The helper was read as text only, not imported, parsed with an AST, syntax
checked or executed. Windows API behavior was not exercised on Linux. These
specific hashes may subsequently be corrected; this record preserves the
original challenge rather than silently accepting a replacement.

## Narrow positive source conclusions

- Exact branch and repository, request-path-only push, attempt 1 only, one
  concurrency group and `cancel-in-progress: false`; no release workflow,
  environment, signing/store secret, Gradle, package/install or binary upload.
- Hosted Windows x64, preinstalled Python >=3.11/CMake/CTest, VS 2022/MSVC and
  SDK `10.0.26100.0`; no dependency/toolchain installation or substitution.
- Actual CMake sources preserve C++20, warning-as-error and hardening options.
  The proposal checks generated projects for unwind/warning/CFG/SDL settings,
  records compiler-related cache entries and hashes PE AMD64 intermediates.
- Clean child environment isolates HOME/TEMP/TMP/APPDATA and disables MSBuild
  node reuse; one build worker and one CTest command at a time.
- Suspended CreateProcess → exact-handle job assignment → ResumeThread prevents
  a normally launched child from running before containment. Nonbreakaway Job
  object uses kill-on-close, 3 GiB committed-memory and 16-process limits. No
  process-name/PID-reacquisition killing is used.
- Fresh and ongoing resource samples enforce 12 GiB disk/25% available RAM at
  launch, and 8 GiB/20% while commands run. These are sampled observations, not
  an atomic host resource reservation.
- Exact 14 registrations are compared against CTest inventory, then invoked
  individually with anchored names and single-case XML. See
  `NATIVE-TEST-SCOPE.md` for the independently challenged actual test boundaries.
- Cleanup opens no-reparse/single-link descendants and marks deletion through
  retained handles rather than a broad `Remove-Item`/`rmtree` or process-name kill.

## Required corrections/challenges

These are conditional source paths, **not observed failures**. They do not
reopen G7/G8 or count as new PVA families.

### NR-01 — Admission validation follows runtime allocation

Lines 437–457 create evidence, a Job object, the generated root and five
subdirectories before reading the request or accepting review (458–481).
A missing/wrong-subject/nonaccepting request can therefore mutate the runtime
namespace before its admission gate. The same-image parse/hash binding also
needs explicit treatment: `digest` and `read_json` reread report/input paths.

Validate the immutable request, normative reviewer acceptance, exact source
tuple and cancellation before runtime allocation, or separately seal and review
a minimal bootstrap ownership intent. Bind captured input bytes, not an
author-supplied ACCEPT label or a later different read. Source/job/cleanup
acceptance must remain separate from runtime success.

### NR-02 — Termination failure can skip close and trigger automatic retry

`Run.command`'s finally (350–360) invokes termination and waits before reaching
the handle-close loop. An exception there skips those closes. `main` then
calls TerminateJobObject again if it observes active members (563–568), without
a recorded one-shot termination state. The same second call can follow an
unsettled first termination/wait.

Preserve a monotone single termination obligation, install nested-finally
handle closure, retain the original failure/ambiguous settlement and do not
automatically retry a failed/unsettled stop. Kill-on-handle-close under host
termination is containment, not observed stop success or renewed execution
permission. Never turn unknown settlement into delete authority.

### NR-03 — Actual failing test can be reported as zero application cases

Lines 545–547 call `Run.command`, which raises on nonzero exit before XML is
read. `application_testcases` increments only at 553 after a passing XML.
Therefore a first CTest test failure may have executed a case and produced
failure XML while the aggregate says `application_testcases: 0`.

Record exact planned/started/observed case identities and preserve/interpret
bounded XML for nonzero exits without suppressing the failure. Distinguish
passed, failed, skipped, unstarted and unknown outcomes. A stopped sequence
does not execute its remaining cases. No automatic retry is requested.

### NR-04 — Root-close lifecycle remains ambiguous after later failure

Cleanup closes its root handle at 387, then performs fallible path/evidence
work (388–389). The caller clears its numeric handle only after the whole call
returns (571–572); otherwise line 580 closes it again. Record original-handle
closure immediately in explicit shared lifecycle state before fallible receipt
publication. A partial deletion/receipt failure stays HOLD, not inferred success
from absence or permission to re-adopt/re-delete a path.

Root/parent bindings also need to bound the pathname-based enumeration: retain
the existing namespace parent and confirm the owned-root relationship rather
than relying only on a resolved pathname and a temporary-leaf handle. This is
a cooperative fresh-runner contract, not a hostile same-user sandbox.

### NR-05 — Compact logs are a polling limit, not a hard write cap

The child writes directly to disk (298–306); line 330 samples size every 500 ms.
An arbitrarily large final burst can exceed the nominal 2 MiB before rejection,
and the artifact step still selects that full `.log`. Explicitly bound retained
and uploaded evidence after overflow, preserve original length/hash/failed
outcome, and qualify sampling/interruption limits. Do not claim a hard file-size
limit or discard the overflow failure to produce PASS.

### NR-06 — Final exact packet remains incomplete at this review

The expected source-binding/author admission documents had not yet been
provided to this reviewer. Exact commands, environment, class/case mapping,
resource/cleanup conditions and one-shot root coordination must be bound to
the independently authored acceptance, not merely a path containing a chosen
label. The reviewer will not manufacture the helper's requested accepting JSON
until the final candidate and required conditions are accepted.

## Action-pin provenance

A bounded, credential-disabled public `git ls-remote` observation independently
returned:

```text
3d3c42e5aac5ba805825da76410c181273ba90b1 refs/tags/v7   actions/checkout
043fb46d1a93c77aae656e7c1c64a875d1fc6a0a refs/tags/v7  actions/upload-artifact
```

The combined inspection shell exited 0. Both observed tag identities match
the immutable workflow pins. This verifies the public ref association at that
observation, not a full audit of either action's implementation. No Actions
job was dispatched and no remote branch/tag was changed.

## Outcome and preserved restrictions

No native build/test/toolchain execution, background worker or large output
was created by this review. There are **zero new executed application cases**
and no closure/count change. PVA-010 native lifetime, PVA-014 actual prompt
rendering, fixed-output KDF compatibility, production allocator cuts and real
Windows Hello remain separate gaps. A later corrected helper must receive
fresh exact review; this v1 REVISE never grants automatic invocation/retry.

PVU-007 STOP; PVU-011 NO RETRY; PVA-029 FAIL/no automatic retry;
G7/G8 runtime/recovery/cache/helper scopes CLOSED. No archived helper was
executed/imported or adopted as current authority. Candidate 1017001, all
protected refs, identities, versions, dependencies and PVD choices are unchanged.
