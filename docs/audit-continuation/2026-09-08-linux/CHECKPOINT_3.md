# Checkpoint 3 — source correction and controls; remediation incomplete

This checkpoint continues published `f18995e6633904a6845e09eab8a481635290894d`
(tree `8948bb6c92b5baaf406f838b48bf8e31064d1195`). Its containing commit/tree
must be read from Git after publication. **Zero new application test executions,
zero new qualified closures.** No release, protected-branch, tag, dependency,
identity, signing or Store-version changes. The preserved handoff commit/tree
still match the user's expected identities.

Read this with the frozen handoff, central overlays and `EXECUTION_SLOT.json`.
The current snapshot is not execution permission for any archived or new helper.

## Actual evidence since checkpoint 2

### Immutable handoff selection

`reviews/linux-runner/SOURCE.json` has SHA-256
`3b34d8c862d33ccda9df0d77bb4507a2504c0612a236f36d6d89a60c8a7485ce`.
Four serial read-only Git commands exited and settled. Independent reconstruction
of all **1198** tuples reproduced the exact handoff tree: 809 raw-equal application
members, two declared checkout-EOL exceptions and 387 handoff additions. This is
transport/selection, not semantic source coverage or test admission; capture
consumed. Internal1.016910791s, pre-final1.026899099s and tool0.935093527s are distinct
clock/scope observations, not a full external monotonic duration.

### Two distinct NEW runner-control batches

| Scope | Actually executed | PASS | FAIL | Unstarted |
| --- | ---: | ---: | ---: | ---: |
| controls01 | 22 | 20 | 2 | 0 |
| separately admitted corrected controls02 | 22 | 22 | 0 | 0 |

Controls01 LC17/LC22 failed in `FakeChild` construction (`len(None)`) before the
intended subject branches. C-FIXTURE-01 (alias LC-FIXTURE-01) is an infrastructure
fixture defect, not a new product family or established runner fault. The original
acceptance reviewer missed it; original source, reviews and failure are preserved.
A minimal nullable guard correction received independent source challenge, then
controls02 had a fresh source/launcher/request/actual-instance review and ran once.
LC17/LC22 reached the intended mocked direct-child branches in02.

Both batches exercise **real synthetic POSIX file/pipe I/O and mocked
subprocess/proc/pidfd/signal/stop APIs**. Each has **zero real children, real process
signals or application cases**. A green control batch does not test Gradle or
prove real child-process behavior.

Each213-record journal,22 full case rows and duplicate report was independently
reconciled. Each removed24 original fixture directories,27 original file/aliases
and its original scratch. Every case's literal `descriptor_settlement_hold=false`
was retained. External tool exits were1 then0; original controller/scratch absence
was subsequently observed by root and reviewer. No wrapper ran: `--stop` was
not applicable, not a waiver of any earlier obligation. Both admissions are
consumed. Slot release is not another run's admission or product closure.

| Exact evidence | SHA-256 |
| --- | --- |
| controls01 journal | `75fcebbd3c666518f9b2160d186e83ad9eebb9b8774c016eae043e88e40c6eb7` |
| controls01 external receipt | `39a7daf48e4096fab5245c858f71aef1fe7eae507dd88b424078774fca1217f1` |
| controls01 independent result JSON | `cb7e5629fad46d4287f6e65043fd0ba8b544bf14519991120a12da5d3fb3aede` |
| controls02 journal | `28f806393e0adacfdcd13913b57b2639aa0ae57010347fa67370aae1dd3aa863` |
| controls02 external receipt | `88ee8b5264e3924aca77e392b3f69c9ad68a67b1a7cc7b9227ece35135a402b5` |
| controls02 independent result JSON | `8284e904b16f0b4bb87f05dee4cd7fe59f2b850dcd562fc4dafc5752547e7f62` |

See `reviews/verification/` and `reviews/editor-independent/`. Preserve the
original Windows-count acceptance erratum, controls02 additive global-accounting
qualification and root release's additive editorial qualification. Frozen report
22/37 wording is an inherited baseline, not the current total; PVA-038 confirmation
was not incorporated into that report. It is not newly reviewed by the controls
reviewer. The root release's abandoned “Original36? no:” fragment does not change
its actual24/27 accounting; the original bytes remain untouched.

**Timing is qualified:** controls01 tool0.255451913s / journal0.311654695s /
batch0.2103432939911727s; controls02 tool0.215379499s / journal0.276773090s /
batch0.185738811s. These have different scopes/clocks. No full external monotonic
elapsed-time or hard-time proof is claimed.

### Android32 source-data progress, not a target result

The fixed three-XSD reader ran **once**, exit0, after independent source review
and root admission. Rejected v1 is retained. `schema-capture-01/` contains exactly
five permanent packet files,46,105 bytes. The terminal stream records42 CLOSED
own-FD events and an empty registry; RESULT itself precedes the final three closes.
No JAR code, network, SDK installer, archive download, emulator, ADB or test ran.
No temporary files/cache/SDK changes were made. Foreground timeout completed
normally; no separate original controller/reader PID census was captured.

The external receipt preserves a later metadata-only postreader failure: whole
`stat_result` comparison failed before receipt creation. The exact changed field
was not logged, so its cause is unproved. A corrected original8-field comparison
succeeded; **the capture was not rerun**. Tool0.00001326s, program0.021638s and
orchestration wall0.162s remain separate timing observations.

Independent `ANDROID-XSD-CAPTURE-ACTUAL-AND-SCHEMA-REVIEW.md` (SHA-256
`a97a44c24580df4e65c1d96ba1dc71666d9d6bf7ed437bdb4550ec384dd94acb`)
traces sys-img2/01 through common/01 repository/archive/complete/archiveFields
and its checksum documentation specifying **SHA-1**. Common/02's required type is
a distinct namespace, not a default for01. The separate sdk-common/01 import was
not read; whole-catalog/JAXB/actual-consumer validation is not established.

API24/default/x86 revision8 (313,489,224-byte archive) remains a candidate. The
installed API35 image is64-bit-only and rejected. `/dev/kvm` was absent; software
emulation is **unmeasured, not proven impossible**. License authority, legacy
SHA-1/TLS trust, artifact/download/staging/cleanup and emulator/ADB/network/process
containment/boot admission remain distinct gates. Android32 KDF compatibility,
synthetic create/unlock/backup, ARM32 and minified packaged provenance are not
supplied by schemas. Android32 remains first in risk priority.

## Product finding and source correction

**PVA-038 independently confirmed:** on a valid enrolled supported Desktop vault,
first CryptoEngine use can fail in the ordinary resource-loader temporary-directory
operation. `ResourceLoaderException` wrapping `IOException` is misclassified as
biometric-key rejection; the service attempts enrollment deletion and returns
INVALIDATED before any authentication failure. Actual persistent enrollment loss
requires successful deletion. No vault/password deletion, bypass or session
publication is claimed. Eager initialization, static-initializer Error wrapping,
platform-store ordering, metadata/key and generation/session/cancellation guards
were challenged; mobile/warm/Error and Linux real-biometric generalizations were
excluded. Dependency source/POM linkage is not a new resolved binary graph.

`ROOT-PVA-038-REVIEW.json` SHA-256
`8cd66918d13af25d14bda9cfd2c113fa0b57cf69f65aaeaa5551b866fe7781d5`
binds30 before-images atf18995e and six retained source captures. This continues
NAV-FRESH-SUS-001; its frozen additional-investigation row remains unchanged and
it does **not** resolve an original PVU. The narrow patch and9 routing/4 opt-in
provider declarations are active separately reviewed work **excluded from this
checkpoint's published source delta**. They have no execution/closure credit.

**MAC-FIXTURE-001 source correction accepted with qualifications:** synthetic
macOS fixture RAII cleanup uses retained original descriptors, bounded known
leaves and an owned symlink sentinel. Three new CTest declarations, **zero runtime
executions/XML**. The entire changed fixture/CMake/README have exact independent
source bindings; this infrastructure issue is not a new product family.
Required addendum `MACOS-FIXTURE-PATCH-QUALIFICATIONS.md` SHA-256
`f821e6db983d0cc647710a7edf1f227fdcb27e1c6e5154a04814356a9a65e97c`
limits preallocation to member paths and close reporting to the three retained
fixture directory FDs, not production helpers/native workers.

**Caller prerequisite:** existing macOS Gradle/Desktop tests now intentionally
fail closed without an independently admitted `PASSVAULT_NATIVE_TEST_PARENT`.
No behavior-identical caller compatibility is claimed. Windows/common CMake
intervals remain byte-equal, but the whole CMake hash changed; consumed Windows
source evidence is not retargeted. The active five-case Mac validation plan is
outside this frozen patch packet and admits no CI run.

## Database verification and prompt cleanup still pending

- NEW fixed runner SHA-256 `346e1655…`, PLAN `74739fd3…`, METHODS `40ac2f07…`
  and handoff1198 SOURCE are independently source-reviewed. The intended task is
  only `:core:database:desktopTest`: seven classes/five files/**105 methods**.
  This immutable handoff selection **does not test continuation editor/Android,
  macOS or PVA-038 changes**. No local Gradle/JVM/compiler/application test ran.
- Outer supervisor `ee46cfd9…` / PLAN `c5bb3831…` is independently source-accepted,
  **unexecuted**. Outer exit0 may collect an inner failure; it is not a test PASS.
  Supervision must accommodate6000+900+60+30+120s plus setup margin. The original
  outer PLAN snapshot/read failure and rejected versions remain preserved.
- Empty evidence-bootstrap v2 `a49f750c…` / PLAN `87963b73…` has independent
  source acceptance only. Rejected v1's missing externally comparable original
  journal pin and timeout process-group claim were challenged and corrected;
  actual original instance/preflight approval and invocation remain pending.
- Closeout `b16774ff…` / PLAN `cffc6935…` is source-accepted, with33 deletion
  targets/35 original allocations. All source/tests, `.git`, retained checkout,
  **every reports/test-results subtree**, SDKs/toolchains/shared caches stay
  protected. Actual original stop0 (or positive no-Gradle proof), worker settlement,
  original lock and filled actual-instance acceptance are still required.
- Gate timing is explicit: a viable, accepted prompt-closeout **design is a
  prebuild gate**. Meaningful LC-C05/06 closer controls are mandatory **before
  closer invocation/actual C acceptance**. Root will finish them before building
  to avoid stranded large caches. Runner22 controls do not substitute. New closer
  controls, evidence bootstrap, original database request/approvals/preflight and
  a bounded outer closeout launcher are unfinished work, not external blockers.
- Any failure/ambiguous stop retains the original wrapper/environment/namespace
  and active stop obligation; no retry, normalization, adoption or guessed cleanup.
  Unknown process birth/cwd/identity is HOLD. Quiesce team tool commands and freeze
  shared Git objects for actual runner admission; do not weaken conservative census.

## Unchanged failures, limits and progress

Windows run34284083351 remains **FAIL / filesystem-cleanup HOLD**. MSVC configure
probes ran; configure exit is unknown, product compilation/CTest did not start,
and all14 cases are unstarted. Exact Job reported zero workers, which does not
prove filesystem cleanup. Unknown reparse entry was refused before deletion.
Hosted disposal is not cleanup proof; inert observability source is unapplied,
with no retry/recovery admission. Physical iPhone/Windows Hello and interactive
provider gaps remain BLOCKED; hosted tests cannot replace them.

| Defined measure | Established | Remaining |
| --- | ---: | ---: |
| Original confirmed families |19/25 qualified closures (76%)|6|
| All confirmed families, now PVA-001–038 |22/38 qualified closures (57.9%)|16|
| Original suspicions |2/12 conclusive outcomes|10|
| Original PVD explanations |8 documented|Owner decisions separate|

Separate denominators, **not overall readiness percentages**. Twenty published
editor JUnit declarations and four Android harness IDs remain unexecuted; three
added Mac CTest declarations are also unexecuted. No new semantic-LF credit or
whole-project coverage percentage is claimed. Historical successes, rejected
findings, grouped variants and every handoff coverage qualification remain.
The authoritative skill ledger-validator script is unavailable/unrun; explicitly
labeled independent manual/data-only review is not that missing tool.

PVU-007 STOP; PVU-011 NO RETRY; PVA-02949 checks/44 PASS/five FAIL/no automatic
retry; G7/G8 execution/recovery/cache/helper scopes CLOSED. No old runner import,
real vault/backup/clipboard/private signing data, dependency/identity/Store changes,
protected-ref moves, signing/publishing or replacement of occupied1017001.
Eight PVD design choices remain separate, including mandatory existing lowercase
ASCII-hex password compatibility.

At02:05UTC remote main/testing/release/candidate/handoff/continuation refs had not
advanced since the previous observation; exact command exits and identities are
in `publication/CHECKPOINT-3-REMOTE-REFRESH.json`. Root is sole build/test/CI/Git
publication owner. Fresh resource floors and cleanup admission precede every
invocation; preserve compact evidence and promptly remove only admitted owned
outputs. No unrelated process, source, permanent report, shared cache, SDK or
toolchain is a cleanup target. `EXECUTION_SLOT.json`, not this snapshot, is live
coordination. Next: finish reviewed closer controls/actual admission, run the
105-method selection once, independently score XML and immediately close out
owned outputs. Continue Android32, PVA-038 and important Mac evidence in parallel
source-only lanes without treating this checkpoint as completion.
