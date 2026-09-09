# Corrected partial Mac packet: independent source review v2

Reviewer `/root/native_review`; author `/root/native`; 2026-09-09.
Root-authorized **SOURCE ONLY** follow-up. **QUALIFIED SOURCE-CORRECTION
ACCEPTANCE of MP-R1/MP-R2 only; eight gates and entry HARD HOLD remain.**
**No executable runner, CTest compatibility, controls, CI, target or cleanup admission.**

Entire frozen inputs read, with beginning/end hash confirmation:

| Input | LF / bytes | SHA-256 |
| --- | ---: | --- |
| `scripts/audit/macos_native_validation.py` | 426 /19092 | `d9eb3ae6f8aaa0b407069c51ad912d16e930a76805e82808aee9276336be1f79` |
| `.github/workflows/audit-macos-native-validation.yml` | 83 /3777 | `4f6ffba8376203095f14ca4f1ddbb6db126e6b5d6506e3171fdc6d432bbe485d` |
| `reviews/native/MACOS-VALIDATION-PLAN.md` (plan v3) | 540 /35759 | `efde8a14202426d3470eb522dba77fd460162acb93beb8d1548fce0fdc3eb487` |

Report-relative paths are below `docs/audit-continuation/2026-09-08-linux/`.
The prior REVISE pair is unchanged: Markdown
`0dda43d0be86daa21d9ab2944052e2935c3a0110305c64bcaec9b7bc0d28fa73`, JSON
`5ee5c63d902f9e11e9479adfce39a68513770e1dda471ab5e313bb8eeb087d52`.
Its findings/limitations remain valid for the archived original bytes. Exact
helper/workflow/plan-v2 archives and the initial plan were hash-revalidated;
their paths/hashes are in the JSON. No archive or capture file was edited.

## MP-R1 — Structure/namespace rejection source-corrected

Helper335–378 now requires a literal testsuite, tests=1, exactly one **direct**
selected testcase, permitted bounded suite/case attributes and a run status.
The existing exact-integer actual_exit0 guard remains. Root outcomes must be
decimal zero; omitted outcome counts still default to0 and omitted status to
run as explicitly documented, not as a newly proven producer contract.

Independent manual traces against the previous counterexample and variants:

- The v1 unknown wrapper around the selected testcase leaves no direct case
  at369–371. It now fails before becoming a candidate.
- Removing that wrapper but keeping its namespaced failure leaves an unknown
  case child. The allowlist316–332, invoked at377, rejects it. Literal failure,
  error, skipped, unknown wrappers and properties/extensions also are not logs.
- A second direct case fails the cardinality check. An additional unknown or
  namespaced root child fails the root log allowlist even if one direct correct
  case exists. Namespaced root/used attributes are separately rejected.
- Logs may only be unique-per-container system-out/system-err, without attributes
  or children. Nested outcomes/comments/PIs inside a log fail childlessness;
  duplicate logs fail the seen set. In-root comment/PI nodes are retained by the
  proposed TreeBuilder and rejected as unknown children rather than discarded.
- Nonwhitespace container text and child tails fail. An escaped/CDATA diagnostic
  containing the word "failure" is still informational log text, not an XML
  outcome. This matters for the deliberately exercised assertion-return case;
  the source does not confuse its expected diagnostic words with a failed case.

These are **source traces only**, not executed XML inputs or regression cases.
The author independently agreed with the traces and informational-log distinction.
No actual CTest record was supplied, parsed or shown to pass in this review.

Raw XML is bounded at512KiB, each decoded attribute value at4096 UTF-8 bytes
and each decoded log at256KiB. Attributes such as time/hostname/timestamp remain
opaque metadata, not verified measurements. Unused namespace declarations and
prolog/epilog comments/PIs are not claimed rejected by this used-node contract.
Whitespace checking uses Python string whitespace semantics, not a separately
proved XML-S grammar. Raw caps are not a measured parser peak-memory/time bound.
Conservative declaration/log rejection may reject legitimate data; compatibility
must not be inferred merely because the previous false-candidate trace now fails.

**MAC-CTEST remains fully open.** The exact CTest4.4.2 producer/source schema and
representative positive/negative fixtures have not been bound or executed. The
proposal must still independently establish accepted producer compatibility,
wrong-exit/status/count/identity rejection, structural/outcome/log/comment/PI
and size-boundary controls before any backend relies on this grammar. It is
not a completed tested validator or permission to execute a parser control.

In particular, capture02's frozen finite inventory includes cmProcess.cxx and
conditionally cmUVProcessChain.cxx, **not the JUnit producer/schema source or
representative XML**. Even successful separately admitted capture02 requests
would not close this compatibility gap. No extra GET/path substitution is
authorized; any necessary later source-data scope requires separate review.

## MP-R2 — Every finite tool role now checked

Helper209–217 explicitly enumerates cmake, ctest, make, shell, cc, cxx, objcxx,
linker, archiver, ranlib, nm, strip and xcrun: all13 declared tool roles.
Each getattr result must have exact type SealedTool before validate_record.
The previous shell=None/xcrun=None counterexample reaches rejection, including
roles that command_vectors does not itself dereference. Missing roles, other
types and SealedTool subclasses no longer silently escape this method's loop.
The vector function calls this validation at231 before constructing its vectors.

This accepts only the identified **record-shape correction**. Arbitrary caller
Python code/overridden outer objects are not authenticated by it. A shaped
record or driver_chain_acceptance hash does not attest installed bytes, resolve
the real compiler/shell/subtool chain or prove a normative independent review.
Expected version constants/SDK path prefix are not actual toolchain observations.
MAC-TOOLS and the preconfigure admission requirement remain open. No tool record
was instantiated, command vector evaluated or target tool invoked in this review.

## Retained hard-HOLD, workflow and native scope

The exact main405–422 still installs only its prospective local signal observer,
parses the fixed argument, then reaches the nonempty eight-gate tuple. It returns
the HOLD/UNSTARTED declaration before request/native reads, namespace/tool/process/
provider work; removing the tuple still reaches the no-backend exception. Help,
invalid argv, runtime/import or stderr failures may exit earlier. This is normal
source control-flow reasoning, not an observed exit or startup/platform proof.
The signal observer and possibly blocking stderr are not a process controller.
The uncalled source-leaf fd I/O is now correctly described as non-pure and not
independently admitted ancestor, leaf-type-before-open, timing or cleanup safety.

Full re-read of the unchanged workflow retains only the prior qualified source
shape: dedicated branch/request-path push, guarded repository/ref/event/attempt,
one Intel Mac job, contents:read, literal immutable action pins, no signing/store/
deployment/environment binding and short compact-evidence routing. Pin contents,
bootstrap interpreter, actual image and E/nonce/output bounds remain unverified.
Path filters are not activation/source attestation; globs are not ownership/size
validation. An accidental activation could still allocate a job/check out/upload
despite helper HOLD. Concurrency does not coordinate local workers or establish
settlement and does not preclude pending-run supersession. Root retains the one
cross-local/CI slot through actual closeout. No request was created or authorized.

All five native input hashes were rechecked and match the preceding independent
review. Its null-context and missing-macos-v1.meta guards, explicit-return tests,
fixture/sibling controls and limited LAContext properties/invalidate reachability
are reused by identity, not claimed as another full native source read or run.
The five existing CTest names/argv/order are unchanged: fixtures, ABI, security
last. Existing raw-context/async/blocking/swallowed-destroy and fixture-parent/
ACL/once-only cleanup limits stay separate. No physical iPhone/Touch ID/Windows
Hello, displayed prompt, successful authentication/Keychain, iOS or full PVA-010/
PVA-014 provider/packaged/JVM/native interleaving evidence follows.

The eight MAC-PROC/CTEST/MEMORY/ACL/CLEANUP/TOOLS/CONTROLS/INSTANCE gates remain
unimplemented/unadmitted. Integer resource arithmetic is not a Mac observer,
lease or periodic enforcement. Inventory's numeric-equality/duplicate-property
and JSON's nonfinite-literal-only qualifications remain as documented. Plan v3
correctly preserves these gaps and historical source identities/denominators,
and distinguishes capture02 source acceptance from transport/instance admission.

## Accounting, preservation and resource discipline

Five existing case declarations; **zero executions, XML results, imports, syntax
probes, controls, GETs or CI queries** in this review. No new product finding,
test declaration, qualified family closure or denominator change. MP-R1/MP-R2
are partial infrastructure source corrections only. Root's live ledger remains
authoritative; original19/25, the separately identified post-PVA03822/38 snapshot
and original suspicions2/12 are not overall readiness. Eight design explanations
remain separate from owner decisions.

The native request and capture02 evidence parent were both absent at this review's
passive observation. Capture01 remains consumed FAIL; capture02 remains source-only
with no new request admission. Windows01 operational FAIL/filesystem HOLD/14 cases
UNSTARTED/no retry, PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry,
G7/G8 CLOSED and protected-ref/publication/version/dependency/identity/build1017001
fences are unchanged. No old runner or archived application/recovery helper was
executed/imported, and no old cleanup obligation was retried or discharged.

Only this compact permanent review pair was created. No reviewer-owned build,
cache, temporary executable, background worker, request or namespace was allocated;
no deletion, stop, termination or recovery ran. Passive Linux resource point at
2026-09-09T04:46:13Z:22,738,372KiB filesystem availability and40,062,244KiB
MemAvailable, not target memory semantics or future admission. Gradle --stop is
not applicable to source inspection and grants no historical clearance.
