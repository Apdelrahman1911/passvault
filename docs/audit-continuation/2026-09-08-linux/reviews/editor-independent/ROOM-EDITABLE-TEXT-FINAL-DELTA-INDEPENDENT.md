# Room editable-only payload correction — exact final-byte review

Reviewer `/root/editor_review`, 2026-09-10; source author `/root`.
**Accept this exact fixture-only narrowing. Risk reduction, not an observed Room-NPE fix,
runtime verification, new case or execution admission.**

Input source: `shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRoomIntegrationTest.kt`.
- Before: `a67ba81ad875a4c611208d30b51e40caf87cf5381622f5d6ababf38d2953cff9`,36660B/712LF.
- After: `291c47edcca2de797d1a669947a2ded246ec5b32c120feec8843a2d41ec9ef57`,36796B/714LF.

Under `docs/audit-continuation/2026-09-08-linux/reviews/`:
- room-editable-text/SOURCE-DELTA.json: `03a403e2996d71780059112c9a3b611697dfa1255c2188f5dfe950a313a3ecd5`,1866B/20LF.
- room-editable-text/ROOM-EDITABLE-TEXT.patch.txt: `28d1e3dbd1ea83cbd9d3348d5725489dc930141efb194943e7bf91f44cd1479b`,1563B/22LF.
- editor-independent/ROOM-EDITABLE-TEXT-NARROWING-SOURCE-REVIEW.md:
  `e02eb94d48c4ed5c7bf7275dd217a5aa959c979e48b59bec07a837502fabd9dd`,4734B/66LF.

The full root delta note and inert patch were read. Independent in-memory inverse matched
the exact before hash; after/source/artifact guards all matched. The patch has **two literal
unified-diff hunks for one logical narrowing**. My first data check reached the matching before
hash but then failed an incorrect one-hunk-count expectation. Correcting that metadata
expectation to two passed; no source change, application/helper execution or test retry occurred.
This also qualifies the earlier recommendation's prospective “one-hunk” shorthand.

The exact delta only adds a comment, captures accessibleEditableText, uses that optional
provider for the existing character loop and reuses its non-nullness as the editable flag.
All other bytes reconstruct unchanged. Both .text consumers still require an editable target;
the0..256 assertion and nullable getAtIndex/append behavior remain for those values. Metadata,
identity/traversal bounds, names/roles/focus/geometry, input ordering, persistence/capacity
assertions and cleanup are unchanged. No catch, fallback, relaxed editable assertion or new
retry was introduced. Unused non-editable character payload/bound reads intentionally cease.

Retain all counterexamples in the bound recommendation: analogous GUI02 success is preserved;
integration02 Room was unstarted, and no Room NPE was observed. Editable/stale-provider and
metadata failures remain possible; the two accessibility getters are not proven identical for
arbitrary providers. Main's distinct NPE and unverified correction are not resolved by analogy.
No universal accessibility safety or product defect is claimed.

Future GUI03 source binding must use the new hash and be separately admitted; consumed C15/T/
Detekt01 identities are not silently relabeled. No new test cycle or replay is requested.
Zero new cases, runs, findings, product fixes, closures or denominator changes.

Only source/data reads and this exclusive0600/fsynced/read-back note; no source edits by this
reviewer, Git/network, helper execution/import, build/test, live process/runtime/SDK/cache probe,
background worker or temporary/generated runtime output. All STOP/NO-RETRY/CLOSED/HOLD/consumed
scopes, PVU-007/PVU-011/PVA-029 and G7/G8, root build/cleanup/admission ownership and existing
hardware/PVD/non-publishing limits are preserved.
