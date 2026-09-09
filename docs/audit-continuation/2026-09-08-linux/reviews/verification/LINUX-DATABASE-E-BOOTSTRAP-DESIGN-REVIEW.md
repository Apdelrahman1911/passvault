# Database01 evidence bootstrap — bounded design review only

Reviewer `/root/verification`; proposer/executor `/root`; 2026-09-09 UTC.

Disposition: **ACCEPT_BOUNDED_METADATA_DESIGN_ONLY**. The exact implementation
and actual original-identity packet still need independent review before the
one invocation. This note is not an operational runner acceptance, a filled
REQUEST, a C-closeout approval or proof that a directory exists/was created.

## Received proposal and scope

Root requested review of creating only the new empty evidence directory

`/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/runs/linux-database-01`

through a durable original-parent intent, no-follow descriptor-relative single
exclusive mkdir0700, original child identity/emptiness checks and parent fsync.
The compact permanent bootstrap journal is outside that directory:

`/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/linux-runner/E-BOOTSTRAP.jsonl`.

The existing original coordination record/lock is reused without recreation,
replacement, normalization or borrowing old macOS identities. All other parent
directories already exist according to the proposal; this review does not
observe or independently establish that fact. Root retains actual external tool
exit/close evidence separately and prepares the database REQUEST only after its
source freeze and fresh preflight. No runtime, checkout or generated root is
allocated by this bootstrap.

## Challenges and accepted limits

1. **Original ancestry, not present-path adoption.** Bind the existing original
   `runs` and `linux-runner` parent chains and relevant original coordination
   evidence first. Open required directories no-follow through their checked
   parent descriptors, retaining original tuples. A pathname observation or
   a new pin after drift must not silently replace a missing original.
2. **Durable intent precedes allocation.** The journal must be bounded,
   exclusive0600, single-link and no-follow, not an append/reuse of a previous
   attempt. Its original file/parent and write/fsync results are evidence. The
   allocation intent naming the exact child and original parent must be durable
   before the one fd-relative mkdir. Record actual ownership separately from
   a merely proposed future tuple.
3. **One original allocation.** Use mkdir's exclusive failure behavior, not a
   check-then-adopt path or `exist_ok`. Open the created child no-follow and
   compare its original descriptor/path observations, uid and exact0700 mode;
   verify it is empty. Retain parent fsync and durable original-child record.
   On this cooperative namespace, a same-UID race is not a proven hostile-UID
   sandbox guarantee; root's freeze/coordination remains substantive.
4. **Honest finality and settlement.** Install cleanup/FD handling before
   effects, attempt each owned descriptor close once, and retain failures.
   Durable preterminal journal data cannot claim an as-yet-unobserved tool exit
   or its own successful final close. Root supplies that observed outer result.
   No unrelated process, signal, daemon or wrapper invocation is needed here.
5. **Fail retained, no alternate action.** Any EEXIST, drift, ambiguity,
   cancellation, partial allocation, failed fsync or close is HOLD. Preserve
   whichever tiny original namespace/evidence actually exists; no deletion,
   retry, replacement, adoption, chmod normalization or second namespace is
   implied. Missing originals remain missing. Do not create R or precreate the
   inner runner's future35 allocation witnesses.

The frozen inner runner PLAN `74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f`
requires root's original empty0700 E before filled-instance acceptance, but
defines no generic bootstrap schema. This review does not invent one. Actual
E identity will be a member of the later complete original-directory map and
must be revalidated unchanged at operational admission. Bootstrap evidence
remains outside E. In particular, it is **not** the completed `LAUNCHER.json`
receipt that must stay absent until the database outer invocation has exited.

## Remaining authority and accounting

Root still supplies the exact proposed command/helper and original bindings
for source/instance challenge before effect. The bounded design acceptance in
this note alone must not be treated as approval of arbitrary implementation
bytes, database launch, cleanup/deletion, stop replay or recovery.

Controls02 independent acceptance `8284e904…` settles only its own22 controls and
namespace; it neither proves bootstrap originals nor admits another job. All
PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED,
Windows cleanup HOLD, PVD/hardware and publication restrictions remain.

This review used only source/data reads and wrote compact permanent review
documentation. No runtime/process/original-identity probe, code import, build,
test, temporary allocation, worker or daemon was performed. Wrapper stop is
not applicable. Closure delta0; no application execution credit is added.
