# iOS01 request-only activation: independent source-delta review

Reviewer `/root/storage`; author `/root`; 2026-09-10.
**ACCEPTED LIMITED SOURCE DELTA; NOT AN INSTANCE/EXECUTION ADMISSION.**

All paths below are under `docs/audit-continuation/2026-09-08-linux/`.

| Input | SHA256 |
| --- | --- |
| `publication/IOS-01-ACTIVATE.py` — final236LF/16664B | `57347b91dd6af7d03ad71f094fe3b7d3dafa53468f359f27667521c1d69713ac` |
| `publication/WINDOWS-05-ACTIVATE.py` — consumed comparison source only | `35d1548bf2206e0adb9fc4db5d406c6ce90a6c166c8b2b0ac54dfc5077b20cbc` |
| `publication/IOS-01-ACTIVATION-REQUEST-PARSER-DELTA.json` | `b4d20260a97a735a32cd794881d21442aa7e03848fc1ebd68283367506f413d5` |

The complete inert text delta and changed request/admission/publication/closeout
contexts were inspected. Unchanged accepted publication utilities are reused in
their existing qualified scope, **not re-audited or imported/executed here**.

## Changed contract and independent correction

- Config/whitespace-log paths, commit message, reviewer (`/root/storage`), iOS
  admission/slot/status labels and final pending-result wording consistently name
  iOS01. The exact request path matches the accepted focused-iOS workflow/helper:
  `docs/audit-continuation/2026-09-08-linux/requests/ios-focused-01.json`.
- The request must contain exactly `source_commit`, `source_tree`, `nonce`.
  Commit/tree must match the configured source base, which the unchanged Git
  publication checks subsequently bind to the actual base. Nonce must be a string
  matching32 lowercase hexadecimal characters. Windows-only `suite`, `owner` and
  `exclusive_build_slot` fields would be rejected, not silently forwarded.
- Independent challenge of the initial iOS source
  (`8d1f4b20bfff5481e7fe0f624e7e669b2ca96f2f7dc4394fb974d797059a03f6`):
  plain `json.loads` collapses duplicate raw keys, so a key-set check alone did not
  enforce the helper's duplicate-free request contract. The final request-only
  `object_pairs_hook` rejects duplicates before overwriting a value or publication. Its
  complete six-line addition/call-site were independently checked; no other JSON
  parser or publication utility was broadened. The author's AST parse is syntax
  evidence only, not a request execution/regression or CI pass.
- Removing owner/slot fields from the request does not remove their separate
  gates: exact reviewed request/source bytes, the named independent instance
  review, root-owned iOS execution/cleanup admission and exact active iOS slot
  reservation remain mandatory. A Windows reservation/admission cannot satisfy
  the changed labels. Source/slot/remote drift guards and request-only sole-parent
  commit/tree checks are unchanged; only the continuation ref is pushed.

## Preserved limits / next admission

The accepted iOS helper/workflow/fixture tuple remains unchanged at point hash
checks (`61f7e362…` / `d1a1b3b5…` / `406c47fd…`), bound in
`reviews/storage/IOS10-HELPER-WORKFLOW-INDEPENDENT.md`, SHA256
`f3cf067598a7ddb4fb7dcb1c0b93cc94f95765dd65944a13e63846328fe7ba4c`.
Aligned plan identity was hash-checked only:
`da3f69651b706991a3a53b886b1faaa020db243873cb7ec2092e3f3493f511f2`.
This delta review does not grant additional semantic review credit for that plan.

Actual published C12 commit/tree, synthetic nonce/request bytes, exact config and
root/independent admission records, fresh CI-slot observations and original
publication/coordination identities remain a separate precise instance review.
Hard-coded namespace/lock guards are not current execution authority merely
because their source is unchanged. No request/config/runtime/CI is created or
admitted by this note. The publication clone remains retained; there is no new
local build/wrapper-stop duty from source review. Actual iOS worker/device cleanup
and raw exact-ten results still need independent reconciliation before slot release;
publication success alone supplies neither. Abnormal descendants, interruption,
store/HOLD and no-automatic-retry limits are not relaxed.

Only bounded source reads/diffs/hashes, coordination and this permanent note were
performed. No Git/network, helper import/execution, request activation or runtime
probe occurred. Zero tests/passes/closures; no coverage denominator or PVD decision
changes. PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry,
G7/G8 CLOSED and all non-publishing/build1017001 fences remain intact.
