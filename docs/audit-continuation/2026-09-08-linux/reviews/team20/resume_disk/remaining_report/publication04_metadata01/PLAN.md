# Publication04 metadata01 — SOURCE ONLY, root admission pending

This is one new **at-most-90-second metadata phase**, not an extension/reset of
the elapsed original20minute publication phase. The author has not executed,
imported, compiled, AST-parsed or syntax-probed this supervisor, run Git, opened
the retained Git store, allocated its runtime paths, or inspected current tools,
processes or resource state. Root reviews/adopts the exact seal before execution.
The request template is deliberately unbound; it is not launch authority.

## Exactly three local Git builtins

All use the single fixed `/usr/bin/git`, common flags/environment in the source,
and working directory `/root/projects/PassVault/passvault-publication-20260914-04`:

1. `cat-file commit HEAD`: retain raw commit; recompute its object ID and require
   HEAD `832ed5f5aee56fbbf4c298ef0d2b10de154484cf` and its declared tree
   `24d4157fff0d5fcc5cb694e598f21f561d1921d4`.
2. `ls-tree -r -z HEAD`: retain the complete, unquoted, NUL-delimited leaf modes,
   object types/IDs and paths. Parse every leaf and independently reconstruct the
   tree object IDs, requiring the expected root. Keep the raw stdout descriptor
   and leaf count in `METADATA.json`, not a redundant second complete entry list.
3. `cat-file --batch`: stdin is only the distinct, exact 40-hex **blob** IDs parsed
   from every leaf below `.github/workflows/`, not merely `.yml`/`.yaml` files.
   Preserve all workflow path/mode/ID associations. Require exact batch headers,
   sizes, body object IDs, delimiter bytes and no trailing data. Record body
   offsets/lengths/SHA256 into the retained batch stdout instead of copying bodies.

There are three successful commands only on a successful phase; any failed
admission/query/parse stops, never retries or substitutes another query. Even a
zero-workflow tree uses the third builtin with EOF stdin. Up to64 distinct IDs
produce at most2624 stdin bytes; one nonblocking, PIPE_BUF-checked write then
closes the original writer. No fourth Git invocation exists in the source.

No refs/clone replay, checkout, read-tree, staging, hash-object/write, persisted
configuration change, fetch/lazy fetch, network, PR, push, build or generated
project output. HOME/config/TMP paths are `/dev/null`; no HOME/TMP allocation.
Process-local `-c` overrides disable credential/askpass/hooks/fsmonitor,
maintenance/gc/signing; global/system config and external Git exec-path are
disabled. All protocols are denied. These raw builtins use no filters/textconv.
Nothing is deleted, including on failure.

## Fresh root admission and launch

Root alone binds a genuine independent source review, the exact source/tool
hashes and full file pins, the original repository identity `[23,250699,0,448]`,
a fresh private empty evidence directory, and an original empty root0600
single-link coordinator. Its **original parent** must also be opened/bound to
`[23,534,0,448]` at
`/root/projects/PassVault/.audit-coordination-linux-20260914-c20`;
open/check/lock the coordinator leaf relative to that parent FD.
The parent and leaf pathnames must be canonical and agree. The evidence pathname
must be canonical below the source's fixed root/publication04 prefix; its
root0700 identity and initial emptiness are checked. No result is written into
an evidence directory whose initial admission did not finish.

The template's proposed evidence pathname is not an allocation. Root must choose
an unused admissible scope and record fresh identities, including the exact
coordinator leaf pathname/full pin. Root supplies current lock DATA
`[24,13719326,0,384]`, distinct from historical creation inode239778; the latter
is not reused. No missing path/pin is inferred from an old invocation.
Source and tools are read/hash/pin checked, the running Python is bound through
`/proc/self/exe`, and pins are rechecked around the commands. Historical pins
in the template are retained DATA to be freshly checked, not current observations.

Root supplies an original monotonic start/deadline separated by at most90s;
the supervisor must start inside that interval and cannot reset/extend it.
Launch the exact text with the pinned `/usr/bin/python3.12 -I -S -B`, followed by
the actual private request path and its SHA256. No GNU timeout/group wrapper is
part of this source. Root provides the sterile own-tool launch context and
**already inherited** limit pairs; the supervisor never calls setrlimit.
All four pairs are mandatory and must exactly match getrlimit: CORE=0;
positive finite AS<=2GiB, FSIZE<=4MiB, CPU<=90s, with soft<=hard. Root must admit
sufficient actual limits; the template does not assert observed values.

The bounded `/proc/meminfo` read keeps only current/min MemAvailable and total:
entry requires12GiB and25%; running requires8GiB and20%. Open original directory
FDs supply current/min free-byte observations, always requiring3GiB. Retained
historical free/RAM numbers do not admit this phase. Request claims about genuine
independent review, no overlap/helper/reaper and authority are externally checked
root obligations: a well-shaped review hash/boolean does not authenticate them.

## Custody, limits and acceptance

INT/TERM/HUP handlers are installed before spawn and latch cancellation without
interrupting pidfd acquisition. Each direct child is the checked Git builtin;
its original pidfd is captured before any wait/reap. No PID/group signal,
descendant/host scan, competing reaper, shell or helper is used. Cancellation or
error sends only original-pidfd TERM, KILL after5s if needed, and allows at most10s
further pipe/child settlement. An exhausted final settlement window attempts
original-pidfd KILL if not already attempted. Each command gets at most20s;
work stops18s before the original phase deadline, leaving bounded settlement
and finalization. A final direct-child wait is bounded to at most1s and remaining
phase time. Two original pipe EOFs plus actual own-child wait status0 are required.

On pidfd-capture failure, the original stdin is still closed exactly once;
original pipes are closed and only the bounded own-child wait is attempted.
There is **no unsafe numeric-PID fallback**, no next command, and no settlement
claim for an unknown original child. Root retains HOLD/unknown evidence and must
not turn that failure into cleanup authority. The no-helper premise comes from
the exact builtins and root's exclusive admission, not a manufactured host scan.

Generated evidence (overall and per-command intents, raw stdout/stderr,
`METADATA.json`, `RESULT.json`) shares one4MiB aggregate payload cap, with64KiB
reserved for the final record. All created files are retained, mode0600,
exclusive/no-follow, fsynced; none is replaced or removed. Read/cap/cancellation
failures can leave explicitly unsuccessful partial output. A write failure may
prevent complete retention and is never success. Source/control/review files
are separate pre-existing admission material; root must budget those too.

`RESULT.json` is explicitly **preterminal**, not proof of process exit or late
fd/record settlement. Root acceptance additionally needs the original tool's
actual terminal0, complete terminal stdout/stderr settlement, a final
`SUPERVISOR_COMPLETED` receipt without errors/signals, all three original-child
0/reaped/pidfd/two-EOF records, complete descriptor-matching raw output and
metadata, resource floors and the original<=90s phase. Late pin/resource/fd/
write/terminal errors are nonzero even if an earlier record says DATA_COMPLETE.
Bounded polling does not manufacture a hard-real-time guarantee for an OS/filesystem
stall; an overrun or uncertain original terminal remains unaccepted.

The parser conservatively rejects unsupported/noncanonical modes, duplicate or
prefix-colliding paths, over20000 leaves, non-blob workflow leaves, non-UTF8
workflow paths, over64 workflow blob IDs, and a tree that cannot be reconstructed
from the complete recursive leaf output (including invisible empty subtrees).
Such a failure is HOLD, not permission to add commands. Product cases remain0;
this is neither test execution nor publication/branch/worktree correctness proof.

## Retained basis (DATA only)

`T` = `/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk`.
`D` = `T/root`.

- `D/publication04/clone01/REQUEST.json`: exact historical Git/Python pins.
- `D/publication04/clone01/ACTUAL-ADOPTION.json`: original clone adoption.
- `T/image_cleanup_prep/publication04/CLONE01-ACTUAL-REVIEW.json`: independent
  acceptance of original clone DATA/settlement, not future metadata proof.

Exact descriptors are in `MANIFEST.json`. No original GUI/Image HOLD, provider
refusal, testing/runtime status or previous authority boundary is changed.
