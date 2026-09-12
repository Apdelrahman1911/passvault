# C20 SOURCE capacity worksheet — prospective arithmetic, not facts

Bounded task: obtain one adequate, finite SOURCE output ceiling from the actual final publication inventory before root admits the single capture. No current N, Q, SOURCE size, identity or metadata is asserted here; no helper/capture runs. Candidate baseline: `SOURCE-PREPARE.py.txt` SHA256 `9ebf3df7eddbf3501806143886f428c579fb7985baf8840a3a6bd31ae4510321`. Candidate code is unchanged by this worksheet.

## Inputs that root must actually bind

- N: actual committed full-tree member count from the independently accepted final C20 publication; N>0, never selected-row count or C18/C19 count.
- Q: sum of byte lengths of each full-tree path's compact `ensure_ascii=True` JSON string literal, UTF-8 encoded, including its quotes and escapes. This is the prospective publisher-v2 `source_capture_path_aggregates.Q`, not raw file/path bytes.
- Z: byte length of compact ensure_ascii JSON encoding of the exact fresh `SOURCE-STORE-ADMISSION.json` object at `facts['shallow']`, without a trailing LF. The whole facts file and its hash/pins/publication linkage need their separate acceptance; no old shallow object is adopted.
- Fixed existing T selector, Linux64 numeric ABI/kernel-format assumptions below, unchanged old EOL objects/hashes, and the finally reviewed SOURCE scalar/identity delta. If any assumption does not hold, this worksheet is not a fit proof; do not invent a replacement value or launch.

The prospective publisher-v2 source records N/Q/max JSON-quoted path length only after its actual committed full-tree equality check. Those prospective fields are not actual until root's publication and independent actual review settle. No new listing/capture is needed for these aggregates.

## Conservative scalar widths, not observed pins

The admitted environment is Linux64 with unsigned64 dev/ino/statvfs quantities and signed64 timestamp seconds, not an adversarial replacement kernel or root. Fixed root-owned/single-link file guards make uid=0/nlink=1. Existing per-member/tool read cap is32MiB.

| JSON integer field | Maximum character width used |
|---|---:|
| pin.dev, pin.ino |20 each|
| pin.uid, pin.nlink |1 each|
| pin.mode (unsigned32, deliberately looser than ordinary regular modes) |10|
| pin.bytes, git_size, raw_size, checkout_size (<=33554432) |8 each|
| pin.mtime_ns, pin.ctime_ns |29 each|
| resource.free_disk |39|
| resource.available_ram, resource.total_ram |23 each|
| raw_source_bytes, observed_checkout_bytes (<=134217728) |9 each|

Timestamp bound is the longest signed64-seconds-to-nanoseconds integer, e.g. width of `-(2**63)*10**9`; no timestamp value is claimed. Disk bound is width of `(2**64-1)**2` because free blocks multiply fragment size. Kernel `/proc/meminfo` unsigned64 KiB values multiplied by1024 have at most23 decimal digits. Finite binary64 JSON `elapsed` has at most24 characters; the admitted source already refuses nonfinite JSON. None of these are observed capacity/pin/resource guarantees or new reader relaxations.

## Exact fixed-schema arithmetic

Let `d(x)` be decimal digit count of positive integer x and `M=3+ceil(N/128)`.

- A source row has at most528 bytes excluding its JSON path token: keys/punctuation, fixed6/40/64-character identities and the above bounded numbers. Nested pin contributes190 bytes. Thus the full N-row array adds at most `529*N+Q-1` bytes to an initially empty `[]` (brackets already counted; N-1 commas).
- Each normal resource row has at most129 bytes. Exactly M resource observations occur on the successful source path: one before each of two Git children, one at indices0,128,... in the member loop, and one before final serialization. Git selector-loop iterations do not append resource observations. Replacing `[]` adds at most `130*M-1` bytes.
- The fixed manifest skeleton is5385 bytes including its final LF. It uses empty files/resources arrays; member placeholder0 (one character); shallow placeholdernull (four); maximum-width pins/size totals/elapsed; fixed current40-hex commit/tree placeholders and64-hex hashes; successful complete/reaped/exit0/empty-stderr command shapes; stdout byte widths bounded by82 and2097152; and all15 possible derived-EOL names. Placeholders here are width arithmetic only, not serialized evidence files.
- The two command objects together encode to1383 bytes at their bounds. Historical qualification array encodes to1116 bytes, and all15 allowed EOL paths to627 bytes. These derive only from the immutable historical data below; order does not affect lengths.
- Skeleton report alone is3605 bytes without LF, before final source_sha256/source_bytes fields. Adding those final fields to a nonempty report costs `99+d(actual_source_bytes)` bytes.

Consequently, an upper bound for the normal SOURCE manifest including LF is:

    Smax = 5378 + 529*N + Q + 130*M + Z + d(N)

This is exactly `5385 + (529*N+Q-1) + (130*M-1) + (Z-4) + (d(N)-1)`. The complete final SOURCE-CAPTURE report including LF is bounded by:

    Rmax = 3699 + 130*M + Z + d(N) + d(Smax)

Root can independently check the arithmetic and use `max(2097152, Smax+1, Rmax+1)` as the smallest scalar justified by this conservative envelope while retaining the old floor. The +1 is necessary: source write175 uses strict `<cap`, not `<=cap`. If a raise is needed, change only that numeric ceiling and the matching readback189 ceiling in a separately reviewed exact delta; no new generalized capacity framework, runtime sizing or unbounded fallback. No value is chosen before actual N/Q/Z exist.

The maximum-width skeleton is conservative, not a claim that actual SOURCE is Smax bytes. Root retains actual source length/hash after the one successful capture. Only that actual length then determines whether INDEX's2MiB SOURCE reader needs a replacement. Consumer SOURCE readers already32MiB need no change merely because2MiB was insufficient here.

## Other coupled bounds, checked without another capture

For an ordinary safe path, raw UTF-8 length is no larger than JSON-quoted length minus2. Under unchanged32MiB member size limit (at most eight decimal size digits), full ls-tree -l/NUL listing is at most `61*N+Q` bytes; the full stage0/NUL listing is at most `49*N+Q`. Check these against their unchanged2MiB caps, not against manifest length. `max_json_quoted_path_bytes-2<=1024` is a sufficient (not necessary) precheck for the unchanged raw-path cap. Do not relax that path cap if the sufficient check fails.

OID vector is exactly `41*N` bytes, including one LF per full40-hex member OID; both consumer write/read caps must fit. SOURCE3500 and INDEX3500 member ceilings are independent scalar sites; GUI's prior3432 sites are different and must not be overlooked. Use current complete N at every admitted consumer rather than an old subset count.

This arithmetic does not establish actual raw total, maximum member size, original index size/content, current tool/config/exclude state or executable availability: publisher path aggregates do not contain those facts. Existing128MiB raw/checkout totals,32MiB members,4MiB original index, metadata/tool/output/time/resource/lock/freeze guards remain. Any required fresh fact, full identity linkage, original terminal or independent acceptance missing means no admission, not a capture-and-retry strategy.

## Immutable data and activity

All paths relative to B=`/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux`:
- `reviews/desktop-gui02/SOURCE.json`: SHA256 `6d593b947d76f0a7b3e7929a7ae54e6968cb0a36b69bf78d771b33226bd396ee` (only historical qualification array used).
- `reviews/desktop-gui02/SOURCE-CAPTURE.json`: SHA256 `5b89c5c90f40b34ff92a633fb2e93f3174a65451aaf8642ce8cdc4c9822fe0a9` (only15 permitted derived-path names used).

Only these exact W retained JSON bytes and accepted source text were read; Python-stdlib in-memory JSON-width/integer arithmetic produced the stated constants, not a source helper import/AST/syntax/execution or a capture. Only this own inert worksheet was written. No Git/T/tool/process/SDK/provider/network/build/test probe, source/runtime capture, cleanup or broader authority. All STOP/NO-RETRY/CLOSED/HOLD/native/protected/Store/1017001 boundaries remain; parent owns notifications.
