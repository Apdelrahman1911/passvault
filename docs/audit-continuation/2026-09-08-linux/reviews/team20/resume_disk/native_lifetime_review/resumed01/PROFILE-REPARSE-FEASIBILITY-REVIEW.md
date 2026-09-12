# Future Windows profile reparse leaf — independent challenge

Reviewer `/root/native_review_c20`; author `/root/native_author`; 2026-09-12.
**ACCEPT_FUTURE_ONLY_FEASIBILITY; NOT CODE/API/EXECUTION/CLEANUP ADMISSION.**
No recovery, retry, target access or source change is authorized. The bounded
next engineering step may be a reviewed exact-leaf implementation; prevention
and successful future cleanup are not established by this assessment.

## Exact reviewed inputs

Author packet `native_lifetime_author/resumed01/profile_reparse01/`
`PROFILE-REPARSE-FEASIBILITY.json`:2474B,
`ff35645e1177599911437123813e28b949f76bd3b58b0bf318c3c2525a159dff`.
Its `FEASIBILITY.txt`:6703B,
`6e44d56b8f2f373d7a37f220eb066632c5b682d274de592df957fb1b4c559562`.
Prefixes above are under B`/reviews/team20/resume_disk/`.

All four packet input tuples plus the note matched independently. The actual
Windows result18843B hash
`a48a56750b39f6e82321a81f682752edb0926a3d2ddfe6ec42b99c0e8fee2022`
records exactly the named relative Content.IE5 entry, attributes9238/0x2416,
links1, reparse=true, target_read=false, original_handle_closed=true. Tag,
payload/target, creator and creation phase are absent. Job settlement has
observed_zero=true **after termination_attempted=true**, errors[], and no
recorded original handle-close failures. Cleanup remains HOLD; hosted disposal
is not a cleanup pass. Four CTest exits0 are not this review's case adjudication.
No XML or original Windows runtime was accessed by this lane.

## Source-supported relevance; counterexamples preserved

- Windows4 and the graph adapter route HOME/USERPROFILE to G/home with private
  APPDATA/LOCALAPPDATA. Their generic owned-entry opener rejects every reparse
  before directory traversal. The same observed shape would therefore refuse
  prospective cleanup. Ordinary-DLL preparation uses this profile with the
  VS/MSBuild route, making it materially relevant. Graph-only uses different
  tools: its recurrence is **unproven**, not an inevitable failure claim.
- Redirected LOCALAPPDATA did not prevent this observation. There is no source
  basis to blame one tool, infer a benign junction, or promise prevention via
  environment/registry/profile changes or regular-directory precreation. Do
  not point tools at the real runner profile or turn off existing isolation.
- Keeping all reparse refusals is safe but leaves this observed cleanup shape
  unsupported. A narrowly defined **fresh, owned entry unlink** is a reasonable
  proposed remedy; following, cleaning or classifying its target is not needed
  and is outside scope. It is not a blanket allowance for reparse directories.

## Binding qualifications from independent challenge

1. The only proposed special route is the literal fresh-G-relative
   `home/AppData/Local/Microsoft/Windows/INetCache/Content.IE5`. Preserve original
   run/root custody, same-volume plain retained ancestors, fulfilled original
   wrapper stop if armed, Job-zero, evidence retention and handle-close gates.
   Existing Windows4/05 roots, identities, refusals and closed handles can never
   be inputs or adopted authority for this route.
2. Open that exact entry **once**, before generic refusal, with DELETE plus
   FILE_READ_ATTRIBUTES, OPEN_EXISTING, BACKUP_SEMANTICS and OPEN_REPARSE_POINT;
   deny write/delete sharing. Never catch the old rejection and reopen. Query
   FileAttributeTagInfo and ordinary identity/attribute metadata through that
   **same retained no-follow handle**. Require original volume/file identity,
   links1, directory/reparse bits and admitted attributes; preserve generic
   refusal for every other pathname, tag, link/type or drift.
3. **Mount-point tag is not a benign-junction proof.** IO_REPARSE_TAG_MOUNT_POINT
   can also describe a volume mount point. The author agrees that the API proof
   must establish entry-only disposition for *all accepted tag subtypes*, with
   no assumptions that a target is local, owned, empty or harmless. The old
   result did not record this tag. Do not read FSCTL reparse payloads, resolve
   paths or infer target properties to fill that missing evidence.
4. Insert an accepted special entry only as a **nonrecursive leaf** into the
   existing all-before-any original-handle inventory. Same-handle revalidation
   precedes original-handle FileDispositionInfo and one close. No scandir on
   the leaf, removal of reparse data, attribute changes, fallback, target delete,
   path-recursive deletion or repeated close/disposition. Absence evidence must
   itself use a reviewed **no-follow** observation beneath retained ancestors;
   successful API return or delete-pending alone must not become removed=true.
5. Exact ctypes layout/access/share/flag/class constants and Win32/filesystem
   semantics are **not supplied as an implemented reviewed delta yet**. The
   original plain-entry opener/disposition precedent alone does not prove the
   new reparse exception. Source/API/walker integration review remains required.
   If entry-only no-follow semantics cannot be established, leave this extension
   blocked rather than guessing a tag, inspecting a target or broadening policy.

These qualifications are incorporated into the accepted feasibility assessment
with the author's agreement; no extra author rewrite/review loop is necessary.
A fresh actual tag is a prospective gate, not a condition to investigate the old
root. A future unknown tag refuses without automatic retry.

## Next useful work and evidence limits

Implement/review only the exact future leaf adapter if root chooses to proceed;
keep unchanged ordinary-entry rules, cleanup ownership, entry/time/resource
bounds and retained failure handling. Then exercise it, if naturally encountered,
inside the **next already-needed separately admitted Windows graph/native job**.
No cleanup-only CI, old-root recovery or repeat native suite is justified. If
that future run never creates the leaf, record this branch unexercised. If tag,
API, disappearance or source/handle evidence is uncertain, retain HOLD.

This review neither decides the separate Windows4 actual cases nor repairs its
cleanup result. Sfix/Linux work need not wait for this future Windows component.
No product finding/fix, run, case or closure was added. All PVU007 STOP, PVU011
NO RETRY/no procedure inquiry, PVA02949/44PASS/5FAIL/no automatic retry, G7/G8
CLOSED, original native refusal and earlier HOLDs, PVD decisions and protected
refs/tags/versions/dependencies/identities/signing/Store/build1017001 remain.

Activity: named retained result/source reads, five exact tuple comparisons,
author challenge and this exclusive review write. No old runtime/target, SDK,
registry/cache/process inspection; no helper import/compile/execution, Git,
network, build/test, cleanup, daemon or temporary run/stop obligation.
