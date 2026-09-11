# GUI03 root observation source review

Reviewer: `/root/detekt_actual` · 2026-09-11

**ACCEPT_EXACT_SOURCE_FOR_ONE_ROOT_READ_ONLY_METADATA_OBSERVATION**

- Source: `B/reviews/desktop-integration03-closeout01/ROOT-OBSERVE01.py.txt`
- SHA256: `7d3b3f829f44e5d88097038ce1a4da37d9d0ab3b40483c3b7fabe2e4615e3edc`
- 17,353 bytes / 351 LF lines; final stable no-follow read and second read agree.
- Root must promote unchanged bytes to the fixed `ROOT-OBSERVE01.py` SELF path.
  The `.txt` candidate is inert under its entry guard.

This accepts only the exact observation source. **No actual observation packet,
root closeout admission, runtime source-after proof or cleanup authority is
accepted here.** No script/helper execution/import/AST, `/proc`, runtime, SDK,
Git/T probe or old-result replay was performed by this reviewer. The separate
closer admission interface/template was read as data only; its author/reviewer
retain their own responsibilities.

## Scope and addressed findings

The reader checks the original parent namespace tuple, one reader thread, exact
W cwd and self namespace/inherited-fd links. It reads only named R/root/lock-parent
metadata (no R descent), stable named SLOT bytes and original lock pins. The one
nonblocking exclusive flock is transient kernel coordination, not a write to
lock/slot/runtime contents; its release is not continuous exclusivity.

External process inspection is limited to visible `/proc` birth, per-thread
namespace/**pid_for_children**/cwd/root/exe/fd-link and per-process maps metadata.
It neither follows those references into private contents nor reads argv/env,
starts children, enters namespaces or adopts/signals any PID.

Material earlier findings are addressed: PID/TID birth maps and final checks;
sticky identity/churn/parse/cap/deadline errors despite terminal pidfds; only
narrow missing-proc ENOENT/ESRCH classification with an original terminal pidfd;
fd-target and cwd/root/exe rereads; `pid_for_children`; explicit self checks;
SLOT pre/open/post/path pins and strict duplicate/nonfinite JSON rejection;
unique memory rows; broader explicitly finite build-name matching; bounded issue
lists; final cooperative deadline check after main/lock close.

Current parent mountinfo checks decode root/point fields and derive the
filesystem-relative R position from its unique containing mount. Same-device/
fstype visible containing/descendant aliases cause HOLD; before/after mount table
and parent namespace comparisons are required. Resource floors are checked from
bounded memory metadata and R-parent capacity. No current observation is inferred
from reading this source.

## Minimum honest root-admission interpretation

1. Retain the exact promoted observer image, fixed invocation and complete
   authoritative tool packet. Require **exit0, no live session, CLEAR-not-cleanup
   status, empty conflicts/references/unclassified, and untruncated output**.
   Bind the actual parent namespace, R/lock tuples, idle-slot digest, resources
   and monotonic stamp; do not infer these from source constants alone.
2. The closer interface requires complete exit0 packets with nonempty outputs
   at most65,536 bytes, no `session_id`, at most32 packets and observation age
   at most900s at its coordination checks. The exact closer reviewer must judge
   actual packet completeness; booleans/nonempty tool objects are not evidence.
3. Root must separately and honestly attest **maintained original-R cooperative
   writer freeze since GUI03 terminal, sole-root closeout ownership, all-tool/
   agent quiescence and no concurrent build/test/CI**, maintaining that freeze
   through closer settlement. If this cannot be attested, retain UNBOUND/HOLD.
4. Reference/alias-absence flags mean only the **explicitly scoped observed
   metadata and parent-visible mount checks under those cooperative assumptions**.
   They are not arbitrary/global absence, continuous idleness, ancestor-capability
   exclusion or other-namespace/hidden-alias proof. Finite leader-comm matching is
   not standalone no-build evidence. Sweeps/rereads are not an atomic snapshot;
   later PID/fd reuse and blocked syscalls are not ruled out. Original pidfd
   terminal departures are classified, not counted as complete live scans.
5. Any unknown, conflict/reference, stale/incomplete packet, image drift or lost
   writer freeze blocks admission. **No automatic retry, extra stop, process
   adoption/kill, runtime read/cleanup or broad source/T release follows here.**
   Namespace-number matches confer no ownership. Actual external exit governs.

`REVIEW.json` SHA256: `bd0550bcdba2c60704fe8f643bb0bf556e97f5cc18a2ef8a2c4769813830fdf9`.
