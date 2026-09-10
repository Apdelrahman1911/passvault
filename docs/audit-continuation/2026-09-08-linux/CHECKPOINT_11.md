# Checkpoint 11 — focused native evidence and next product checks

**INCOMPLETE / NOT READY.** Continue in place; this is a non-publishing
source/evidence checkpoint, not a replacement release candidate.

## Completed

- Preserve Linux03's **166 passing regressions + one separate fixture producer**
  in 17 XML files, at `da8ff89b9a8579017d5d524e628dff5251f2bb90` /
  `cf0a8a702e7cd6948236be18b491bb5a21b5886e`. This supports the qualified
  PVA-033/034/035 and narrow Desktop PVA-038 closures. **Do not rerun it unchanged.**
- Mac02 run **34430921453**, attempt1, passed **five CTests**: two native
  ABI/security cases and three synthetic fixture-lifecycle controls, five XMLs,
  no failures/skips. Actual source `6838961e966975532c714d60fd72e3f590f0c8ab`,
  tree `88f37c328a84322d6d97d04200711cde45ca98d4`; activation
  `7b288271d6c139921812caf5050b7f994f5ce1b2` /
  `d2a6b22bf39a13d5360aed31f879613660576a96`.
  macos-15-intel/x86_64, Xcode16.4, SDK15.5, CMake/CTest4.4.2.
  Nine workload commands and actual final helper exit0 were independently
  reconciled, including the deliberately failing inner early-return control.
  Original synthetic root cleanup is qualified; **no physical authenticator,
  Keychain operation, displayed prompt, packaged/sanitizer or family-closure claim**.
- Windows04 run **34427448784** failed after configuration, before product build:
  **14 UNSTARTED, zero XML**. SDK handle reading succeeded; a generated-text
  frozen-read failed. Exact path, differing metadata and cause remain **UNKNOWN**.
  Original Job termination/final zero permit scheduling release only;
  **filesystem HOLD and no automatic retry remain**.
- Added independently challenged Windows defensive regression source: eight
  exact historical writer controls and two real-CNG PRK allocation-cut controls,
  isolated behind a default-OFF audit option. The normal DLL/current14 do not
  receive test hooks. Windows05 changes only selected generated-text reads to
  one coherent original Win32-handle reader and batches the **24 prospective
  cases**. Source acceptance is not execution or diagnosis of Windows04.
- Prepared GUI02: **three rendered editor cases plus the existing curtain case**,
  real Compose/native input with synthetic data. Editor persistence is a deep-copy
  fake, not Room/full NavHost/mobile/IME evidence. Source/runner reviews exist;
  actual source-store, tool/lock/instance binding and execution remain pending.
- Bounded PVU-003 caller analysis is independently reviewed: a conditional lock
  cycle is not proof of a reachable Export/chooser/Tab deadlock. No artificial
  writer stalls or unwired cancellation schedule is accepted as a finding.

Actual native reviews: `reviews/native-independent/{WINDOWS-COHORT-04,
MACOS-FOCUSED-02}-ACTUAL-REVIEW.json`. Raw evidence is under
`runs/windows-cohort04/` and `runs/macos-focused02/`. Raw final-close-pending
labels are not rewritten: Mac02's actual CLI/workflow exit0 supplies that final
condition. All older failures, disagreements and cleanup HOLDs remain intact.

## Progress — separate denominators

| Measure | Done | Remaining |
|---|---:|---:|
| Original confirmed families | **19/25 — 76%** | 6 |
| All confirmed families | **26/38 — 68.4%** | 12 |
| Original suspicions conclusively resolved | **2/12 — 16.7%** | 10 |
| Original design explanations | **8/8 documented** | Owner decisions separate |

There is no defensible overall-readiness percentage. Since the handoff, four
additional qualified closures and one confirmed family were added. New XML
execution-event accounting is **168 application cases + three infrastructure
fixture controls + one fixture producer = 172 elements**, not unique assertions,
task counts, device tests or an overall coverage denominator.

## What still needs doing

| Families / scope | Remaining meaningful work |
|---|---|
| **PVA-001 — first priority** | Actual supported Android32 ABI/KDF compatibility and synthetic create/unlock/backup. Owner SDK-license scope and usable32-bit target are prerequisites. Preserve historical lowercase-hex password encoding. Packaged/minified loaded-ABI provenance remains separate. |
| **PVA-007/031** | Execute GUI02; finish real storage/reopen/Back/tab/forward timing, delivered keyboard/IME/a11y/LTR-RTL and mobile input evidence not covered by its fake repository. Reuse already-passing49 logical/Composition cases. |
| **PVA-008** | Seven existing iOS attachment move/copy/cancel/cleanup cases on an admitted Apple target; synthetic picker/lock and effective physical file protection separately. |
| **PVA-009/030** | Real isolated Android ClipboardManager lifecycle/ownership and EN/AR/SYSTEM/recreation/cold-process locale behavior. Existing fakes/static predicates are not target evidence. |
| **PVA-010/014/027** | Remaining native concurrency/sanitizer/packaged lifecycle; actual localized platform prompts and installed Desktop tray EN→AR→EN. Mac02 narrows only010/014 native evidence, not the tray gap or whole-family closure. |
| **PVA-036/037** | Execute Windows24 once after fresh admission; independently reconcile exact historical/fixed writer and live PRK guards. Production caller PRF/wrapping-array cuts and Hello/authenticated provider behavior remain separate. |
| **PVA-029** | Preserve historical49 checks/44PASS/5FAIL:48 belong to PVA029(43PASS/5FAIL), one to PVA028(PASS). **No automatic retry.** Five capture-cleanup failures are not five proven identity-policy errors; no justified patch or oracle relaxation yet. Exact failing primitive/cause remains unresolved. |
| Other platform/integration gates | Narrow iOS/Kotlin-Native AEAD/provider error evidence (two existing crypto cases), iOS prompt-property case, serial changed-product Detekt, and still-applicable dependency/native/package/migration/provenance evidence. Do not claim these from compilation or Linux03. |

Eight unresolved suspicions permit bounded progress: **PVU-001/002/003** real
UI/provider lease/deletion/lock ordering; **004** iOS gesture plus another shipped
Back source; **005** rendered curtain (GUI02); **006** harmful late work during
real terminal teardown; **008** applicable CNG destruction/lifetime contract or
newly admitted status evidence; **009** actual native prompt cancellation latency.
**PVU-007 STOP and PVU-011 NO RETRY are not executable backlog.** PVU-010→PVA-027
and PVU-012's scoped advisory false positive stay conclusively resolved.

The eight PVD choices remain in the handoff's `OWNER_DESIGN_DECISIONS.md`:
metadata visibility, required existing password compatibility, best-effort memory
erasure, iOS paste policy, plaintext external handoff, legacy TOTP compatibility,
restore compensation/recovery and real-boundary evidence investment. No silent
product redesign or relaxation is authorized. Physical iPhone security,
Android/OEM biometrics and Windows Hello/interactive-device behavior are genuine
hardware/provider gaps; hosted runners cannot settle them.

## Estimates and immediate continuation

Conditional active effort, overlapping parallel source work but serial builds:

- Desktop GUI/editor/curtain: **2–4 working days**, plus device coverage.
- Android32/clipboard/locale: **3–5 working days after license/target access**.
- Remaining Apple/Windows native work: **4–8 working days**, subject to findings.
- Remaining permitted investigations/static/integration/report: **3–6 working days**.

Planning envelope: **roughly 2–4 engineering weeks** for permitted software/hosted
work, excluding external waits and new defects. These are planning ranges, not
measured run durations or a promise to close every family. No honest final date
is available for hardware, owner decisions or stopped scopes.

Next: publish this non-activating checkpoint, bind GUI02 to its actual raw source
and the new publication store, then execute the smallest independently admitted
GUI/Windows/iOS/Detekt batches **one at a time**. iOS helper and Detekt preparation
are separate unfinished work; no extra CI or full successful-suite replay merely
for reassurance. Move on from unavailable external requirements while useful
software work remains.

## Resumption and resource boundaries

Edit ordinary files in `passvault-linux`; **do not run Git against its retired
object store**. Git publication uses only the separately owned
`/root/projects/PassVault/passvault-publication-20260910-01`, original directory
identity `(23,1080545,0,16832)`. It is not a build workspace. C11's parent is the
Mac02 activation above; resulting commit/tree belong in the publication receipt.
The original handoff `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed` /
`05014e9f635131d5db06701e4013b4b5a746465a` stays preserved.

Root alone owns builds/Git/CI/central ledgers. `EXECUTION_SLOT.json` records the
cross-host scheduling reservation; release does not admit another job or remove
an older HOLD. JDK17, checked-in wrapper, worker1, non-daemon, CoD off, strict
dependency verification and serial Detekt apply. Install cleanup first, retain
compact evidence, perform original stops and verify owned settlement. Remove
only validated allowlisted generated outputs/dedicated caches; source/tests,
reports, shared caches/SDKs/toolchains and old held roots are not disposable.
Latest resumed VPS observation: **19GiB free disk,33GiB available RAM**; refresh
at admission and periodically during long work. No extra local daemon/emulator
or background task was started by report/source preparation.

Preserve both checkout-EOL qualifications, coverage/reference omissions and the
missing authoritative skill-tool gap (`validate_evidence_ledger.py` not supplied,
not run). PVU007 STOP, PVU011 NO RETRY, PVA029 FAIL/no automatic retry, G7/G8
CLOSED remain. No protected branches/tags/protections, dependencies, versions,
application identities, signing, Store actions or occupied build **1017001**
changes. Desktop publication remains deferred.
