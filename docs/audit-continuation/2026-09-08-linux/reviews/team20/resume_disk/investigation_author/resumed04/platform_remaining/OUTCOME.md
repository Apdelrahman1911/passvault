# Remaining Apple/Android work — source-only triage

2026-09-12; `/root/investigation_author`, independent reviewer
`/root/investigation_review`. **No new finding, patch, execution or closure.**
This is a bounded clarification of named accepted records, not a restarted audit.
Root owns the actual build/CI schedule, integration, live facts and cleanup.

## Useful work versus genuinely missing prerequisites

| Existing selection | Smallest remaining useful work | Honest blocker boundary |
| --- | --- | --- |
| Android app instrumentation compilation | Establish/consume the actual narrow compiler task/type/edges and compile the existing app androidTest source; no new fixture or device is needed. Coordinate with the Android lane, not duplicate it. | Internal software work. Crypto Compile03 and a unit-test graph are not app-instrumentation compilation. Report02 predates later Graph02 actual evidence: consume its genuine review, do not repeat its old pending status as current or infer compiler coverage. |
| API35 framework pair, PVA009/030 | Existing `realClipboardOwnershipAndForegroundRetry` and `frameworkLocaleRecreationAndExplicitToSystem`, serial; exact starts/passes, terminal RESULT_OK and independently reconciled cleanup. | Existing API35/default/x86_64 rev2 fits API29+ on paper, not an admitted target. Root still needs exact image/emulator use authority, current execution route/bounds and all guest/user/AVD/ADB/Debug/host-clipboard isolation BEFORE boot/Application/Koin startup. These preparations are engineering work; ask the owner only for genuinely missing specific lawful-use authority. |
| Android32/PVA001 | Retain the already-accepted current PVA001 target/license prerequisite and existing four-case harness; no target rediscovery here. | Approved32bit target/use authority and actual32bit native/KDF/create/unlock/backup compatibility remain distinct. API35's64-only image cannot substitute. No blanket API24 license blocker is transferred to API35. |
| Apple existing ARM simulator ten | Preserve exact selection in accepted SOURCE-INPUTS: real crypto2 (VEK roundtrip/wrong KEK), Foundation attachment7 (adoption/copy/protection/cancellation cleanup), LAContext prompt-property1. Prefer no-new-ABI ARM route. | A currently admitted compatible Apple host is not established. It may be an already-authorized, repository-accessible hosted Actions runner, not necessarily an owner-supplied physical Mac. One standard-hosted metadata question below is still internal actionable work. |
| Intel alternative for the same Apple ten | Do not scaffold another workflow or iosX64 target merely to use Mac02's host. |17 production-linked project variants, rawSodium/cinterop, Room/KSP, exact-version x64 support and trusted native artifacts plus installed simulator/JDK host remain unestablished. This is conditional source/ABI/toolchain work, not proven upstream impossibility or purely hardware. No dependency/trust change is authorized. |
| Actual device/provider behavior | Preserve physical iPhone/iPad data-protection/biometric and interactive Windows Hello gaps; actual-framework requirements remain distinct. | Genuine device/provider evidence cannot be manufactured by a simulator or hosted machine. Owner credentials/Store/signing/design decisions remain separate; no security closure from source, compiler or property checks. |

API35 evidence stays endpoint/same-PID lifecycle scoped: no external-UID history,
timer causality, cold durable preferences, displayed prompts, OEM biometrics,
Android32 or complete family closure. The cooperative90s/case+30s cleanup is not
outer settlement of a stalled Binder/main thread. Reuse the accepted pair rather
than multiply low-value mocks or redo unchanged source review.

## Challenge: is an Apple host truly an unavoidable external blocker?

**Not demonstrated as a blanket external-only/no-host condition.** Accepted
Appleavailability01 establishes zero listed repository-visible self-hosted
runners at that instant. It does not enumerate standard hosted runners or owner
machines. Its official larger-runner document names an ARM64 M2 XLarge class,
but repository allocation/access/spend and actual tools/resources are UNKNOWN,
not absent or ineligible because the owner is a User account. Do not purchase,
enable billing/runner settings or assume a paid entitlement.

The named retained evidence does **not** establish a launch-ready standard-hosted
ARM alternative either. IOS01 consumed a resource failure on its precise image:
186630144/7516192768 free+speculative bytes (~2.483%, below25%); ten UNSTARTED,
zero XML, no Xcode/Gradle/simulator launch. This was not an application OOM or a
proof that all Apple hosts fail. Mac02's accepted five native cases need no rerun
and prove neither iOS-simulator/JDK availability nor this Kotlin-Native graph.

The user's important nonpublishing Actions permission already supplies the
execution-policy route for an appropriate **supported hosted Apple target**.
A new generic permission request, owner physical-Mac requirement or speculative
paid allocation is not justified. Actual support/access/tool/resource facts and
fresh source/instance/cleanup admission still must be supplied; permission is
not measured capability. The smallest remaining documentary question is below.

## Once-only official standard-hosted metadata proposal — not executed/admitted

Root explicitly selected this **unqueried official-domain route proposal**:

`https://docs.github.com/en/actions/reference/runners/github-hosted-runners`

One unauthenticated GET of that exact standard-runner reference document, in a
root-owned idle slot, would address a **different missing question** from the
consumed larger-runner metadata capture: current documented standard/public-repo
macOS ARM64 labels, architecture/capacity and relevant eligibility/limitations.
It is not another availability01 run, a candidate matrix or a job dispatch.

Prospective command shape, not launch-ready executable authority:

```text
<trusted-curl> --disable --silent --show-error --proto '=https' --tlsv1.2 \
  --connect-timeout 10 --max-time 30 --max-filesize 1048576 \
  --output <new-owned-body> --dump-header <new-owned-headers> \
  --write-out '%{http_code} %{url_effective}\n' \
  https://docs.github.com/en/actions/reference/runners/github-hosted-runners
```

Root first binds trusted curl identity, clean credential/proxy/cookie/config-free
environment, fresh allowlisted output paths, outer deadline/capture limits and
original-child settlement. Proposed bounds: one invocation,30s request/60s outer,
body1MiB, headers64KiB, diagnostics8KiB, compact retained evidence2MiB maximum.
No redirect following, retry, pagination, linked assets, repository/account API,
workflow launch, SDK/tool provisioning or settings/spend change. A redirect,
HTTP error, truncation or unrecognizable page is NOT_RESOLVED, not absence and
not automatic authority for a follow-up. Root must ensure its chosen tool/capture
enforces these bounds; this document implements no collector or cleanup helper.

Retain exact URL/status/effective URL, UTC, body size/hash and only relevant
standard/public ARM rows/limitations plus output/settlement status. Treat HTML as
inert data. Root may discard safely owned unnecessary temporaries after compact
evidence retention. No Gradle/app was requested, so no new wrapper-stop is due.

- If only the unchanged standard ARM resource class is documented, do not replay
  IOS01 on another label/day/nonce or change the RAM metric to force admission.
- If a materially supported **standard/nonpaid** ARM alternative is documented,
  record it as a candidate for a new narrowly scoped source/instance assessment,
  not proof of free RAM/disk, installed/licensed Xcode/iOS/JDK, exclusive custody,
  successful dispatch or completed application tests. Image labels alone are not
  exact image identities. Do not fetch an additional toolchain matrix by default.
- If no supported standard route is established, retain that precise documentary
  limit. A verified already-allocated compatible ARM host remains the concrete
  external input; absence of a useful row still does not prove no possible host.

Preserve Apple launch12GiB/25% and running8GiB/20% original free+speculative
floors and per-filesystem semantics; the user's temporary **VPS**3GiB floor does
not itself change those Apple admission rules. A genuine successor needs a
materially supported delta and fresh source/request/instance/synthetic storage/
simulator/cleanup admission, never reuse of consumed IOS01 controls. Its original
helperexit1 close qualification remains unmet, not a cleanup PASS or proven
close-descriptor defect; no stop/recovery inquiry or old helper access follows.

## Accounting and scope

Named retained reports and their independent reviews are reused, not raw CI
recertification or a live-source/host census. current_report02 is a dated snapshot
before later Linux/Graph02 actuals; root confirms Apple facts unchanged. Exact
input descriptors are adjacent. No new cases/runs/fixes/closures/percentages.
All PVU007 STOP, PVU011 NO RETRY/no procedure inquiry, PVA02949/44PASS/5FAIL/no
automatic retry, G7/G8 CLOSED, native refusal/HOLD, required compatibility/PVD,
protected refs/tags/versions/dependencies/identities/signing/Store/build1017001
restrictions survive. No Git/network/SDK/process/runtime/oldhelper probe or build
occurred; no temporary runtime, daemon or cleanup obligation was created.
