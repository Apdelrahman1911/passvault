# Independent review: prospective C7 loose-object timestamps

Reviewer: `/root/native_review`; author: `/root`.
Verdict: **ACCEPT, source-only and qualified**. This is a prospective,
C7-only exception, not a reinterpretation of earlier prohibitions or evidence
that Git publication has executed safely.

## Reviewed identity

- `publication/C7-LOOSE-OBJECT-TIMESTAMP-SCOPE.md`: 30 LF, 1,937 bytes.
- SHA-256: `7308723b53684a46efb5ccefe26b020de69b7d0ecd175abfca4357145be61439`.
- Inherits the previously reviewed frozen amended publication scope,
  SHA-256 `bfa801d07d6f53eff5221f62c8224324206513fe0557761b3fdfbc9fe1f6c476`;
  that scope and its original baseline were not reopened in this review.

## Challenge and result

The exception is limited to **mtime/ctime** on pre-existing regular loose-object
files with exact relative names `[0-9a-f]{2}/[0-9a-f]{38}`. It does not exempt
pack/index/info/configuration or other metadata files, nor expand the inherited
directory allowances. Original paths, bytes/hashes, sizes, device/inode,
type/mode, owner, link counts, readability and ancestor identity/mode remain
required. Overwrite, replacement, permission/ownership/link changes, removal,
maintenance, repair and retries remain prohibited.

Before-staging and post-stage/commit/push comparisons retain the original
baseline rather than resetting it. Every allowed timestamp delta must retain
original and observed values; other drift requires STOP without repair.
No timestamp restoration or blanket metadata waiver is authorized. Point
comparisons are cooperative preservation checks, not proof that no intervening
writer existed or that any observed timestamp change was caused by Git.

The proposal explicitly preserves the selected C7 publication bounds, disabled
hooks/signing/maintenance, protected-ref/tag fences and all existing FAIL/HOLD
restrictions. This acceptance does not establish Linux02 process settlement,
release cleanup HOLD, admit recovery/runtime work or authorize an automatic retry.

## Evidence boundary

Read only the new 30-line proposal as inert data and wrote this permanent note.
No Git/environment/source inquiry, runtime probe, build, test, CI, baseline
recomparison or mutation was performed by this reviewer. The author's reported
pre-proposal exact comparison is not independently repeated evidence here.
Zero product tests and zero closure/coverage-count changes.
