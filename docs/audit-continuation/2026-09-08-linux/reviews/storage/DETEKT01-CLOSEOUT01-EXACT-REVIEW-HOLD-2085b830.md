# Detekt01 closeout01 exact implementation review — HOLD delta

2026-09-10, reviewer `/root/storage`. Source-only review; **not accepted, no ACCEPT.json authored, no runtime access/import/execution or new probe**. Root remains sole executor. This identifies two small local guard/error-reporting fixes, not a new cleanup design or external blocker.

Inputs read with bounded no-follow original/named metadata and SHA256 checks:

- `reviews/detekt01-closeout01/CLOSEOUT.py`:22129B/449LF,SHA256 `2085b830dbe0d3d87a530a48ae185369edb208db4947065b8807154deae48012`.
- `reviews/detekt01-closeout01/REQUEST.json`:2678B,SHA256 `0ea332b29501e281b643f43bcdd65ba1e73e05040876c7f8a77e40dea93d9add`.
- Current original external-result record:8032B,SHA256 `e7ffb842fa14d6dfc8ef971a2f647594d563a6f3f1fc6fb776e64e0d5e37beaf`.
- Independent original actual-result review:15471B,SHA256 `83f5c80bf1934177c7de042b88d327da46929d3b9de21198f918dfb2b813ca05`.
- Original scheduling-only release:2139B,SHA256 `dfb683efea321a591e51aa4aeb5ad391fda47e964a2510a65724bae1a3c0530f`.
- Bound SOURCE manifest:1504175B,SHA256 `1a501d764306c1729cef49a411207ca840740d5fe6e284555767182478b55b23`.

## Two concrete blockers

1. **Terminal failures lose explicit HOLD/reason.** After main sets its pending-reconciliation success status, terminal `flush()` failure at435-437 or descriptor-close failure at443-445 changes only code/flags, not status. The final stdout446-448 omits both flags, and the preterminal receipt may not contain them. An ordinary fsync/write/close error can therefore leave success-worded status plus unexplained external code1. This is a source-reachable counterexample, not an observed runtime failure. Small fix: on each branch set `status=HOLD_CONSUMED_NO_AUTOMATIC_RETRY`; expose `receipt_error` and `descriptor_close_uncertainty` in stdout. Do not introduce a receipt retry after descriptors are closed. External actual exit remains a mandatory reconciliation gate.

2. **ABSENT swallows loss of a bound parent.** The `try` at367 includes `directory(path.parent)`, whose recursive named-binding check can raise FileNotFoundError for an already-bound original/source ancestor. The catch370 records an allowed missing generated leaf instead of HOLD. R-private roots have original R as parent; the24 checkout prefixes use checkout or one of22 source-bearing module parents. A fresh bounded manifest check confirms every one of those22 parent prefixes has retained source members. Thus parent disappearance is origin/source drift, not a legitimate absence allowance. Small fix: obtain/validate the parent outside the catch; catch only the selected leaf's no-follow stat ENOENT.

Optional simplification, not a separate blocker under the frozen-manifest qualification: parse SOURCE from the same digest-verified input buffer rather than rereading an un-hash-compared second buffer at333.

## Other challenged paths reconcile

- Exact source/request/purpose/independent reviewer match precedes lock/slot/current namespace validation and original E/R-parent reacquisition. Original external70, Gradle1/stop0 and empty original namespace are retained supporting settlement, not a fresh stop or old-HOLD permission.
- Mount table gate is before runtime descendant traversal and immediately before deletion. The explicit three suffix fields accept a valid empty SOURCE without dropping that field. Encoded whitespace/backslash mountpoints are decoded before exact R-prefix exclusion. No mount, signal, child, Git, Gradle or helper import exists.
- Exactly four nonempty source reports,4MiB/file,16MiB aggregate; original/named pin validation, exclusive destination, fsync and readback precede deletion. Inventory retention fsyncs OUT after reports-directory creation, completing directory-entry durability before removal. A report source changed after retention fails its original-pin comparison in snapshot; unexpected report/test evidence fails closed.
- Exactly35 mutually disjoint selected roots equal the accepted scope; source prefixes do not intersect the fixed C15 manifest. Snapshot completes before removal. Each file/dir/type/owner/device/link and per-entry binding is rechecked; originals' directory nlink/time changes from admitted removals are deliberately not treated as replacements.
- Cached report-source directories can become unlinked during deletion, but subsequent removal uses manually opened descriptors and source-parent caches, never reacquires those deleted target descendants. Final closure handles cached FDs without calling directory() on an unlinked name. No root-is-own-child cycle remains.
-40000 inventory entries/16MiB encoded inventory, per-directory listing cap,64-depth bound,300s cooperative deadline and1s resource observations are finite; launch12GiB/25%, ongoing8GiB/20%. These are not a hard kernel-stall/RSS guarantee. The original retained resource review records12651 examined entries, not current presence or deletion permission.
- Main failure/cancellation leaves files in place or records partial counts; no automatic retry. Receipt and actual external terminal state remain distinct. The two terminal branches above need the minimal diagnostic/HOLD repair.

Only bounded retained-data/source reads and this exclusive permanent review were performed; no runtime data, control run, source mutation, lock/slot change, child process or cleanup. Existing exact-scope contract remains unchanged. Preserve original false safety flags, successful/rejected/grouped evidence, all prior HOLDs/consumed scopes, PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 no automatic retry, TRAY01 and G7/G8 CLOSED. Zero application cases or closure credit.
