# Static04 closeout: independent source-only challenge

Reviewer `/root/android_compile_review`, 2026-09-11.

**CONDITIONAL_FEASIBILITY_ONLY — NO EXECUTION OR DELETION ADMISSION.**
The author's separately bound, no-child closeout concept can be sound under the
conditions below. Retained evidence alone is insufficient; the new static04 R
remains HOLD. No executable closeout candidate or final request was reviewed.

Scope: only `/root/projects/PassVault/audit-runtime-linux-detekt04` (R).
`B = docs/audit-continuation/2026-09-08-linux` under W;
`E = B/runs/linux-detekt04`; `O = B/reviews/detekt04-host02`.
Read only named retained evidence and relevant W source text; no live metadata,
proc/PID/runtime probing, helper import/evaluation, execution, stop, cleanup,
replay, old-R access, or duplicate 27-file actual audit. The separate actual review
is `B/reviews/team20/focused_validation_review/DETEKT04-HOST02-ACTUAL-REVIEW.json`,
root-supplied SHA256 `d91b3e6e51e34cc93685bbdbaa1e9fea472d6e0a8fd07a9b33169a83eafda734`;
this report does not replace it.

## What the retained/source evidence does and does not establish

- Original session15959 launch `c9ea6f`, final `8247a0` exit70, no remaining
  session. Detekt exited143; its original isolated stop exited0. Namespace
  preflight requires PID1/PPID0; `settle()` requires two private `/proc` views
  containing only PID1. `namespace_empty_before_exit:true` plus original child
  exit supports **historical own-namespace settlement**, not global settlement.
- First host-screen failure is positively observed `javac`, PID26628/start891349,
  with stable birth and live candidate pidfd: parent namespaces1836/1841, not
  expected private2116/2115; `owned_proof:false`. Outer lines399–402 explicitly
  reject that mismatch. It is not proven-owned churn or merely a parser error.
  Its purpose, relation to R, foreign references and current state are unknown;
  neither an escape nor unrelatedness is proved. Later failures cannot inherit
  missing facts from this first record. Historical PID/namespace numbers are not
  reusable ownership or signaling authority.
- Inner lines918–927 skip source AFTER when cancelled. Thus source_after=false
  is **no final check**, not a demonstrated source mutation. Nevertheless
  cleanup_safe=false is real: cancellation, incomplete/error command evidence and
  final evidence acceptance also gate it (lines931–937). New hashes cannot turn
  this consumed run into an originally safe/successful one.
- Outer line929 refuses before its final inner-proof/source/mount/removal chain
  at lines955–1002. Cleanup is NOT_ATTEMPTED; there is no retained final parent
  mount-absence/underlying-target or deletion proof. Allocation proves original
  creation, not present identity or ownership of every current descendant.

## Minimum independent gates for a future, distinct closeout

1. **New authority and custody.** An exact cleanup-only request/approval must bind
   original receipts, the new inert source/entrypoint, targets and finite bounds,
   use the same original coordination lock without replacement, and explicitly
   establish cooperative exclusive custody/write-and-mount freeze of this R.
   That basis must address the foreign-workload uncertainty; stop0, own namespace
   exit, allocation or a new empty comm scan cannot silently supply it. If root
   cannot support the custody basis, HOLD is the minimum safe disposition.
2. **Current target identity and boundaries.** Separately admitted fresh checks
   must anchor original parent/R/subroots by no-follow descriptors. R's original
   directory identity is dev23/ino307671/mode0700/uid0; original nlink2 is not a
   populated-directory invariant. E's ino307670 is evidence, never a deletion
   target. Check bounded caller mountinfo for mounts at/below R, including
   same-device binds, before descent and again immediately before destruction;
   reject malformed, incomplete or changed observations. Self mountinfo is not
   proof that no foreign namespace holds references to R.
3. **Preserve evidence and source.** Retain original E unchanged and write any
   closeout evidence separately. Re-establish the original empty underlying
   git-metadata target and sealed-index image. Fresh full raw-C18 source-copy
   agreement is required before treating a disposable checkout replica as
   removable; it is a new observation, never a retroactive source-after pass.
   Authoritative source is excluded. Whole-R disposal must explicitly classify
   verified disposable replicas; “generated” must not silently include source.
   Changed/missing/unclassified content means HOLD, not repair, copying over,
   git reset, regeneration, or deletion to make verification pass.
4. **Finite filesystem transaction, not rm -rf.** Complete the bounded,
   no-follow, explicitly allowlisted snapshot before the first unlink. Reject
   links, special files, unexpected roots, ownership/device/identity changes and
   cap/deadline uncertainty. Descriptor-relative, bottom-up deletion must compare
   each original snapshot entry immediately beforehand; verify final original-R
   identity/emptiness, unlink through the original parent, and retain root-link
   and name-absence results. Failure after partial progress remains recorded
   partial HOLD; no automatic retry, salvage sweep or replacement tree.
5. **Distinct completion.** Reconcile the new external terminal and new receipt
   independently. Do not rewrite original cleanup_safe/source_after or static
   success. Path removal is not secure erasure, foreign-reference absence,
   guaranteed disk reclamation, global-idleness or clearance of other holds.

## Challenge outcome / avoiding unnecessary process work

The author's provisional assessment explicitly accepts these separations and
gates. No remaining disagreement in that conditional concept; the missing
custody basis and current checks remain prerequisites, not passed findings.
A fresh all-PID/comm census, historical-PID investigation/signaling, another
Gradle stop, a static replay, or a full T/tool/SDK/index audit is neither supplied
by this review nor inherently needed for a narrowly authorized filesystem-only
closeout. Eliminating that ceremony does **not** eliminate target custody,
mount-boundary, source-copy, identity and evidence obligations.

Do not import or call the consumed launcher's `main()`/`remove_runtime()`:
the latter depends on original live descriptors/state, END, index_copy and
`tick()`/`watch()`; `authority()` also touches T/tools. A distinct no-child,
no-project-import implementation and its concrete bounds still need review.

All other held/consumed/STOP/NO-RETRY/CLOSED/native-refusal/protected-ref/
publication/build1017001/currentGUI03 fences survive. W/T/authoritative source,
permanent reports, SDKs, credentials, shared caches/outputs and every old R are
excluded. Zero new static/build/test/GUI/runtime or closure credit.

## Directly read identity anchors (SHA256)

| Record/source | SHA256 |
|---|---|
| E/OUTER-RECEIPT.json | 375ddf97539c26b3eb0fb2e0d03707ae2050e43a176f4b1c89ceafe597564803 |
| E/INNER-RESULT.json | 15c32402f1571485bed91c49a1f9000b2c0ac2dfba315b1959df00716a7c33f8 |
| E/OUTER-ALLOCATION.json | 69c6818d487292c53bef0c27e3d4d7bbbbf3a9944e4ad8778fc4efddf6176d3c |
| E/OUTER-INTENT.json | 3a2ec7c2f9dcc40e712c5c837f5a8b7b686893086d6c726f29308e545552e19c |
| O/EXTERNAL-RESULT.json | a082d32315cbe5eef67706008e16577e18c38a4d04bb75fa69f0eba7f2f94361 |
| O/LAUNCH.py | 0a42b59a5a14b3c8d607b4ca09a04ad6ca60ff340bf14006d07e7d3790c3a130 |
| W/scripts/audit/linux_detekt_04.py | baabb5fa66f36e83580a688b7ab13224790b5f0d99c994233e8476802e4e7c38 |
