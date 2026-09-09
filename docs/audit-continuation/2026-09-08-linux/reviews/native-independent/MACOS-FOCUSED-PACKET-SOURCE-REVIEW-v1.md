# Focused macOS five: independent source review v1

Reviewer `/root/native_review`; author `/root/native`; 2026-09-09.
**REVISE: one narrow finalization finding, MF-R1. Source review only.**
No helper execution/import, syntax probe, control, build/test, GET, CI query,
activation or execution admission occurred. No product family/count changes.

## Frozen inputs independently read and identity-checked

| Input | LF / bytes | SHA-256 |
| --- | ---: | --- |
| `scripts/audit/macos_focused_validation.py` | 606 / 32562 | `98fa2ccae3ec771fc4f3b841d38b61a9267546da8e149818646b20d6bd5f8f72` |
| `.github/workflows/audit-macos-focused-validation.yml` | 68 / 2645 | `3d9c947eaacaabec26a8bea26810305ec37021773a83edee38ee1887e1356ac5` |
| `reviews/native/MACOS-FOCUSED-VALIDATION-PLAN.md` | 102 / 6615 | `bfa26b13cdab4477b9f5e118b0b7369184be1af10b9c9c47441c66fabe9d9f07` |

Review-relative paths start at `docs/audit-continuation/2026-09-08-linux/`.
This new packet is separate from the dormant partial Mac runner/capture history.
It does not import that runner or reactivate any of its unexecuted preparation.

## MF-R1: observed late cancellation can still produce helper exit zero

Source trace, **not an executed signal test**:

1. All five cases return normally, and cleanup succeeds. The status is
   `EXECUTION_COMPLETE_INDEPENDENT_RESULT_REVIEW_REQUIRED`.
2. `finish()` chooses `code = 0` at571 solely from status, after testing cleanup
   and errors at569. Neither check includes `cancelled` or `uncertain`.
3. A handled SIGINT/SIGTERM/SIGHUP during the final evidence write/fsync at575
   or final descriptor closes at579-584 sets both flags at90-92.
4. No subsequent check consumes those flags. With no close error, the console
   and return at585-587 still report success/zero. The flags are not serialized.

This does not demonstrate unsafe deletion or a still-running native child: the
counterexample deliberately occurs after qualified workload/tree settlement.
It does demonstrate a false finalization success relative to the packet's
explicit cancellation/uncertainty => HOLD contract. A stale success snapshot
must not be sufficient when finalization itself has observed cancellation.

Author independently agreed the source trace and materiality. Proposed minimal
correction: inspect the flags before status/result/code selection and after
final closes/before return, expose them in the final console, and continue
requiring actual helper exit zero in addition to the pending snapshot. No
arbitrary descendant controller or new multi-gate framework is requested.

## Other reviewed paths: qualified design/source support, not execution proof

- Branch/path trigger, repository/ref/event/attempt guards, immutable action
  pins, contents:read, ten-minute job and seven-day flat compact artifact scope
  are narrow and non-publishing. The add-only single-parent clean-checkout
  request and source/native hash checks bind a later root-admitted activation;
  this source packet alone is not that admission or proof the shared slot is idle.
- Root explicitly permits normal-path trust in synchronous stock tools and this
  closed workload. Configure/build nonzero normal returns are qualified by that
  assumption; the wrapper does not assert arbitrary descendant containment.
  Forced interruption, lost EOF, ambiguous launch or nonzero CTest sets HOLD.
  Nonzero CTest cannot license cleanup merely because CTest itself returned.
- Case argv/inventory checks map exactly to the five reviewed CMake declarations.
  Raw XML and actual exits remain unadjudicated until independent case-by-case
  review. Empty/missing XML is not a pass. The early-return control's intentional
  failed-check diagnostic is informational when its outer expectations succeed.
- No-follow original handles and exclusive owned0700 roots avoid adoption of
  old work. Ancestor ACL acceptance is narrowly noACL or exactly one indexed,
  non-inheriting `group:everyone deny delete`; private roots/children require no
  ACL. No ancestor is deleted. Actual hosted filesystem compatibility is unknown.
- Normal cleanup traverses only this cycle's allowlisted generated children,
  checks device/owner/type/link count and finite depth/entry budgets, and makes
  no second close/deletion attempt after a consumed failure. Native-parent
  residue is never traversed or swept; safe other outputs may be removed only
  under qualified normal settlement. Incomplete binding/uncertainty preserves
  the tree. Hosted runner disposal is containment, not observed cleanup.
- Streaming command logs are capped at2MiB, retained XML at512KiB and total
  evidence at16MiB, with64KiB final reserve. `os_read()` instead buffers fixed
  trusted stock metadata and checks its cap after `communicate`; this is not a
  hard streaming limit for arbitrary output. Author confirmed this distinction.
  Resource floors use conservative free physical pages, not claimed available
  RAM; version/path expectations and sampling/deadlines are not target readings.

## Reused native evidence and remaining boundaries

All five native source SHA256s in the companion JSON matched the previously
reviewed inputs; relevant CMake/test selectors were reread, not re-audited from
scratch. Fixture normal/assertion-return/C++-exception controls observe actual
synthetic filesystem cleanup, subject absence and sibling-sentinel preservation.
The ABI case rejects null contexts before provider calls. The security case's
two retrievals see missing `macos-v1.meta` and return before Keychain operations;
later metadata fixtures use different names. Permitted test-owned LAContext
initialization/properties/invalidate and successful async `destroy.get()` do not
prove provider, displayed prompt, arbitrary exceptional settlement or hardware.

Five selected CTest declarations are not five family closures. Physical iPhone,
Touch ID, Windows Hello, displayed-language/provider and full JVM/packaged
lifecycle gaps remain blocked/outside this batch. PVU-007 STOP, PVU-011 NO RETRY,
PVA-029 failure/no automatic retry, G7/G8 CLOSED and Windows01's consumed failure/
filesystem HOLD are unchanged. No old runner, capture, application or recovery
helper was executed, imported or retried; no request file was created here.

Only compact permanent reports were created. No reviewer-owned build output,
cache, namespace, worker or background process required cleanup. Passive VPS
sample at2026-09-09T08:00:45Z: filesystem available16,713,104KiB (82% used),
MemAvailable39,584,496KiB; communicated to root, not treated as execution admission.
