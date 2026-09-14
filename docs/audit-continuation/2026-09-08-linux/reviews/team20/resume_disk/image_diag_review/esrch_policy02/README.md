# Prospective V5 anchored-read errno policy — SOURCE ONLY

Author: `/root/image_diag_review`; independent review/execution owner: `/root`.
This packet implements root's separately requested prospective policy:
`GUI7_SCHEDULING_ONLY_BOUND_NONLIVE_UNKNOWN_ERRNO_V5`.
It does **not** admit execution or change a GUI07 afterimage. V4 source,
fixtures, successful actuals and historical failed/HOLD runs remain immutable.

## Small source delta

`screen_replacement_v5.py` still contains only the inert screen definition.
After the new exact policy guard, a local stdlib `errno` import supports an
explicit ENOENT/ESRCH allowlist. `proc_read()` wraps only the three anchored
operations: leaf open, leaf read and namespace readlink. It never wraps close,
clock, parsing, surrounding policy or readiness guards. Numeric proc-anchor
opening and initial pidfd handling are unchanged.

An anchored missing read is still insufficient: the same original live-bound
flags0 pidfd must now have understood terminal readiness. Prepositive errors
remain churn only. Full ownership cannot downgrade to UNKNOWN: its original
owner-live check remains unchanged. Foreign-prefix, direct-parent/child,
birth/comm/namespace mismatch, owner-proof, invalid-readiness and close failures
remain sticky. ENOENT keeps its existing labels; ESRCH uses `read_esrch`,
`reread_esrch`, `candidate-at-esrch`, `owner-at-esrch` and owned-terminal ESRCH
keys, never misleading ENOENT labels. No global-idle/descendant/cleanup
authority is added.

## Focused fixture, not a repeated suite

`test_screen_replacement_v5.py` declares **seven new deterministic methods**:
anchored open/read/readlink ESRCH with live versus terminal group; prepositive
churn and fatal prebinding ESRCH; sticky foreign/birth mismatches; owned
departure with live/terminal owner; nonmissing errors and ESRCH-from-close;
ENOENT compatibility; invalid readiness and rejection of the old V4 policy.

The existing revision02 `Kernel` class is byte-identical; its close-attempt
accounting is preserved. The invocation helper is identical except the new
policy string. One tiny subclass injects ESRCH from `read` after a successful
anchored open, protecting a boundary the donor's open/readlink faults do not
exercise. No old test class is inherited or silently selected. `REUSE.json`
records those DATA comparisons. `FIXTURE.diff` contrasts the new companion
with its donor; its removed lines do not mean the donor was edited/deleted.

No source was compiled/imported/executed, no tests were run, and no runtime,
coordination, process, filesystem-resource or kernel probe occurred. Author
work is limited to DATA reads/comparisons and new files under this directory.
Root must review the concrete source and cases, then separately admit only
the new fake batch if accepted. Register the exact new subject bytes as module
`screen_replacement_v5` before loading the companion; do not accidentally
resolve the preserved V4 module or import an archived runner.

No repeat native/kernel/high-FD/GUI campaign is requested for unchanged proven
primitives. Any future controller transplant requires its own exact delta
review and fresh admission. This packet adds zero product/kernel/application
test cases or qualified product closures. All STOP/NO-RETRY/CLOSED boundaries,
old HOLDs, protected refs and build1017001 remain unchanged.
