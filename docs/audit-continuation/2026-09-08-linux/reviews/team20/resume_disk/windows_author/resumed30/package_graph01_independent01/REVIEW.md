# PackageGraph01 independent delta review

**CHANGES REQUIRED before a run.** Scope: postfix's new delta/composition only;
this is not a new self-review of the old independently reviewed Graph04 body.

The sole material blocker is the27 output getter/provider realizations. Public
output annotations and DirectoryProperty/RegularFileProperty return types do
not prove absence of indirect signing/key-input reads. The init calls
`getter.invoke(task)` then `provider.getOrNull()` before checking its path.
The author confirms no named retained pinned provider-chain proof is available.
No private read was observed/alleged; this is a pre-run admission gap.

Small correction: signature/annotation observations only, **no getter/provider
invocation**, `root=null` and explicit unadmitted/realization=false state. Update
the inner mapper/policy labels and new hash cascade. Alternatively supply named
independently reviewed chain-source evidence. No SDK/cache hunt or probe.

Other new composition checks pass source/data review: fixed crypto package
selector/null filter; eight signing probes are noninvoked; exact9 native route
and denial latch; unchanged original refusal/exit1/stop/cleanup controls, bounds
and immutable P/SOURCE/index lineage. Fresh root facts remain unbound and the
partial request rejects. These partial checks do not accept the entire delta.

Four patch sections reconstructed as strings match all expected hashes. No
import/eval/AST/compile/execution or runtime/probe/canonical edit occurred.
Zero actual tests/fixes/closures. Exact evidence/correction is recorded in
`PACKAGE-GRAPH01-DELTA-REVIEW.json`.
