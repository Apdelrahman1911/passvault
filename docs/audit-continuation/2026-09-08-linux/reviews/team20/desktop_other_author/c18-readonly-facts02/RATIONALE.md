# Read-only facts02 — minimal inert successor, not01 replay

Author `/root/desktop_other_author`; reviewer `/root/desktop_other_review`.
Candidate `C18-READONLY-FACTS-02.py.txt`: SHA256
`e6899d29b8d9da5855c64c805f324339fcca9c66b30fb9ccd5deb4b3fbc5dc04` (21246 bytes /420 lines).
Derivation `01-TO-02-UNAPPLIED.patch.txt`: SHA256
`62eb2d3ed849253edfe46a9765f877447ced0cc401fe9b5ba606e6708865b1f7`.

Consumed01 source was read as text only from
`B/reviews/checkpoint18/C18-READONLY-FACTS-01.py`, SHA256
`203060e9a869c80f695c74b10a16cd19d679c7b8c1542441e54a08da1248331b`; it equals the retained author01 source.
The retained01 result SHA256 is `2c6dd1e80133b23eac2d94d376eb69bdfd3bd34e99691dff3dc5b0e3471776da`:
HOLD, `TypeError: unhashable type: 'bytearray'`, no completed resource point.
Root reported actual terminal70. Independent01 reconciliation remains the
reviewer's separate task; no01 record/source/intent/result is changed or replayed.

## Entire material delta

1. `for line in data.splitlines():` becomes
   `for line in bytes(data).splitlines():` in `resource_point` only. The existing
   <=64KiB bytearray buffer is converted once, after its bounded read. Split and
   partition now produce immutable bytes keys for dictionary membership/store;
   the old bytearray key failed first at `name not in values`. No guard is
   removed, error swallowed, expected field changed or resource floor relaxed.
2. Only fixed SELF/INTENT/RESULT basenames and the two format identifiers change
   from01 to02. All other source bytes remain identical. Reversing exactly these
   six literal substitutions reconstructs the consumed01 bytes and SHA256.

Future root-owned source: `B/reviews/checkpoint18/C18-READONLY-FACTS-02.py`.
Future exclusive outputs: `C18-READONLY-FACTS-02-INTENT.json` and
`C18-READONLY-FACTS-02.json` in that same directory. Result format is
`passvault-c18-readonly-facts-02-v1`; intent format is
`passvault-c18-readonly-facts-02-intent-v1`. These paths were NOT written here.

Output keys/types are otherwise **the same**: absolute-keyed images with pin/
sha256; absolute-keyed git/ruby tool_aliases; single python3_alias; resolved_image
strings; metadata4pin; excludes state/info5pin; JDK home/release strings;
JDK-bin git/ruby absence; separate SDK metadata and resource-point evidence.
No reviewed_assertions, behavior/scope/SDK/bounds or old-HOLD expansion.
Original noncreated lock, no-follow/stable reads, fixed source/admission hashes,
180s cooperative work bound, exclusive evidence, finally cleanup and external-
terminal qualifications remain byte-identical outside the listed substitutions.

Only this new own-lane source/diff/rationale packet was written. String/hash/
JSON comparisons only; no helper execution/import/AST/compile/syntax check,
parser test, or live T/tool/store/SDK/resource/process/CI probe. This candidate
is unpromoted/unexecuted, pending exact independent review and root admission.
