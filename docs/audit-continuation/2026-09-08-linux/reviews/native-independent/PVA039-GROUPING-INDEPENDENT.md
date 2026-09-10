# Proposed PVA-039 — independent family-grouping challenge

Reviewer: `/root/native_review`; 2026-09-10.

**ACCEPT a distinct family** for `Compose normal exit bypasses Desktop-owned terminal finalizer/deadline`.
Implementation/test challenge remains pending its author's ready notice. This note does not accept an
unread patch, execute a regression, or resolve original PVU-006.

## Bounded evidence

- Current `ISSUE_LEDGER.json` observed SHA-256
  `a629928d0f44bfeae94883984eefcb942191176144f82b80b7fc252b62b9fad9`: 38 parsed confirmed-family IDs/titles
  through PVA-038; PVU-006's parsed ID retained. Only identification/grouping fields were inspected for
  this task; no fresh raw outcome/evidence-pack search.
- Original `docs/audit-handoff/current/issue-to-fix.json`, SHA-256
  `5782666330bc2eb017e42b7a8725db1efdf54223462f3648c8acf25cb21b245e`: 37 rows. Inspected parsed IDs,
  titles and explicit grouping/variants-preserved fields, with bounded nested grouping projections for
  PVA-005/010/033/036/037. PVA-010's `variant_details.platform_scope` repeats its native-context title;
  `variants_preserved=true` is not a claim that every historical variant was freshly re-audited.
- The independently accepted concrete source defect is already sealed in
  `PVU006-COMPOSE-EXIT-DEFAULT-INDEPENDENT.md`, SHA-256
  `01e681a426e0e868bdea6f4efa19a400bb6c1d4d4c66c57ec594f2381c9a5579`.
  This grouping review preserves its dependency-source/binary qualification, clipboard-hook mitigation,
  exception-path counterexample and absence of proven downstream data loss.

## Why this is not another closure of an existing family

| Comparison | Distinction |
| --- | --- |
| PVA-010: Desktop close-time native cancellation outside context lifetime accounting | The new defect is the Main/Compose process-exit ownership contract. Its normal default-exit bypass does not require an active native call, concurrent context destruction, or even an available native biometric provider. Setting the Compose exit flag does not repair or replace native lifetime accounting. |
| Original PVU-006: singleton/late-operation lifetime, including the unproved onboarding interstage schedule | Earlier work asks whether independent singleton work causes harm across terminal disposal and other guards. This new control-flow defect exists without proving any such late operation. Restoring Main's finally does not give those ViewModels a terminal owner, settle the onboarding schedule, or resolve the original suspicion. |
| PVA-005/006: produced secret or attachment-key ownership | The new bypass needs neither produced-but-undelivered data nor a suspending attachment handoff. No secret-buffer owner correction is proposed. |
| PVA-033: pre-repository biometric unlock freshness | No lock-generation race is required. The proposed fix does not change attempt admission or ordinary post-lock unlock compatibility. |
| PVA-036/037/038: Windows temporary-file/array cleanup and Desktop crypto-initialization failure | Platform failure/exception paths are separate triggers and owners. The new defect is present on normal successful Compose completion, including Linux without those native operations. |

None of the inspected ID/title/grouping fields identifies this Compose-default finalizer bypass as an
already grouped variant. The positive distinguishing trigger/owner/fix above, not merely absence of a
matching title, supports the separate family. This is a bounded grouping challenge, not a reopened audit
of every historical payload. Preserve all older variants and their credit.

## Accounting and remaining work

Root may register proposed PVA-039 as one newly confirmed, initially unclosed family. Relative to the
observed 38-family identification list, that changes the all-confirmed denominator to 39, **not** its
qualified-closure numerator. Original-confirmed and original-suspicion denominators/credit do not change;
no original PVU-006 conclusive resolution is earned. No aggregate readiness percentage is computed here.

The forthcoming Main/production-loop helper/child-JVM test still needs independent source challenge and
fresh execution admission/results. I have not inspected or accepted those future bytes in this note.
Only parsed permanent-ledger reads and this exclusive small report write occurred. No Git, network,
app/helper import, build/test, process probe, temporary/runtime storage or background worker was used.
All STOP/NO-RETRY/CLOSED/HOLD restrictions remain; root owns build/resource/publication coordination.
