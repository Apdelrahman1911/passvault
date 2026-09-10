# Isolated-batch03: independent exact source-delta review

Reviewer `/root/verification`; 2026-09-09.
**SUPPORT_FROZEN_SOURCE_DELTA_ONLY. No actual03 request or instance is accepted.**
No material source blocker found in this bounded delta. No standalone harness,
syntax/AST check, subject import/execution, Git, CI or runtime/probe was performed.

## Frozen sources and review extent

W=`/root/projects/PassVault/passvault-linux`;
B=`W/docs/audit-continuation/2026-09-08-linux`.

| Source | SHA256 | Bytes/LF |
| --- | --- | --- |
| B/reviews/isolated-batch03/LAUNCH.py | `29893099b6412be4b258978cf01695d8727f5e01ba508d7fe018bab07564038e` | 40945/719 |
| B/reviews/isolated-batch03/PLAN.md | `087c52a8bf1202ead8818b712062f82c741cc80814e36af6ce6dae26c73d7101` | 9011/140 |
| W/scripts/audit/linux_isolated_batch_03.py | `4ceaef097a1db97b673e7233aac5ff77acf817d298c2aa8adc33ec86be075c79` | 37204/669 |
| W/scripts/audit/isolated_batch_tests_03.init.gradle | `6e333500d4a9d56599bd22417d89005d26940240228b1f0710773f5f441d0f45` | 5430/101 |

Tool `60f2e4` completed0, rehashing all old/new pairs and reading textual diffs
plus the complete plan. Its displayed final outer hunk was truncated; `62786e`
completed0 and displayed that ENTIRE hunk plus the unchanged origin-write helpers.
Together the COMPLETE deltas were independently examined. All four new source
images were rehashed unchanged immediately before writing this report.

Old02 source hashes remain unchanged at comparison: outer
`f65aecef66207da898c7bfda7d966ea235a2fe70f96b523ff977f32f8a80a6af`;
inner `a1da36534c856ef9a5dddab303c7ad397b3bf2e7ca5d2ab5c07f4685d8255a31`;
init `b8c443198bf6cd33860a704d0ee5bb6d054eae877b92bd17187f20d6e844fdcc`.
Reuse the unchanged body reviews referenced by02 source-delta report SHA256
`b9d0c95e7fea2f555c2155c5c56126098ea16095c5ccf55b6b54bc875f39530c`;
no whole-runner reaudit or old instance-approval transfer is claimed.
The overlooked old archive/raw contract remains preserved in source challenge
SHA256 `e2e0c6a05fb981b7452f0bc8cc2522aca4312cb26edd792098a99288cdf69e9e`.

## Complete delta and compatibility challenge

- Inner changes eight lines only:03 R/E/SELF/INIT/run/hash bindings and three
  transport-only archive descriptions. Init changes three lines: instance label,
  explicit-mode diagnostic and runtime literal. Workload/functional guards,
  modes, fixture semantics, source checks and cleanup restrictions are unchanged.
- Outer changes only03 bindings/pins, raw transport and its stdin argument,
  early allocation evidence, and the two exact transport cleanup filenames.
  Unchanged pidfd/signal/namespace/host-screen/resource/inner/final cleanup bodies
  retain their previously reviewed qualifications.
- `source_inventory` checks fixed1572 rows before allocation: unique relative
  paths without empty/dot/traversal/`.git`/NUL components, admitted regular Git
  modes,40-hex OIDs, executable gradlew and no file/directory ancestor collision.
  Expected maps are keyed by PATH, preserving manifest order and repeated OIDs
  at distinct paths. No checkout path is sent as a Git object expression.
- Plain `cat-file --batch` uses exact OIDs in the original admitted Gitdir with
  existing no-replace/no-lazy/no-network and private clean environment guards.
  No filter/textconv/attribute conversion is requested. Removal of archive-only
  attributes/tar settings does not weaken raw SHA1 or later source_check.
- `source.oids` is exclusive0600 and exactly64,452 bytes. Its capture is bound
  to the originally created FD pin; the read-only/no-follow/nonblocking stdin
  FD is checked against that pin/name and exact request bytes/hash, then rewound
  before launch. The same checks occur only AFTER original Git supervision
  returns. Nested finally blocks close each original stdin/output/error FD;
  isolated execution explicitly retains DEVNULL. No interactive pipe pair or
  response-sized memory buffer is introduced.
- Original Git output FD pin is retained after fsync and must match the captured
  `source.blobs` image before parsing. One original reopened FD is used for two
  complete passes under one180-second deadline. Pass1 validates all objects
  before nested source directories/files are written; pass2 seeks to0 and
  repeats framing/digests while extracting, rather than trusting saved offsets.
- At-most64-byte headers must exactly match expected lowercase OID, literal
  `blob`, canonical decimal0..32MiB and LF. No checkout_size substitution,
  negative/leading-zero size, missing/type/error header, wrong distinct OID or
  malformed delimiter is accepted. Payload reads are at most64KiB and strictly
  size-counted, SHA1 includes the canonical raw object header, and a separate
  single LF is required after each payload. Empty and binary NUL/newline/CRLF
  blobs remain raw bytes. Duplicate OIDs still require one response per path;
  swapping two identical-OID payloads is immaterial to their identical identity.
- Both passes require exactly1572 responses, true EOF/no trailing junk, exact
  original total byte count and whole-stream SHA256, plus stable original
  FD/name pins at start/end. A missing/short/extra response or in-place/renamed
  stream change fails closed. The unchanged full source_check and another
  original stream capture run before inner launch. Trusted cooperative-root
  assumptions remain; this is not concurrent-hostile-root isolation.
- Manifest/request memory is small and bounded by frozen inputs; only64KiB
  payload chunks are retained, not a32MiB object or128MiB response buffer.
  Existing120-second/128MiB Git supervision remains. Parser/header/body/write
  loops tick the shared180-second materialization deadline and WORK_END, keeping
  cancellation/resource checks. Blocking syscall limits remain unchanged.
- Extraction retains original-directory no-follow/O_EXCL, UID/device/type/link
  checks, raw digest/framing before final Git mode and fsync, and before/after
  raw SHA1/SHA256 source checks. The two EOL qualifications and calculated
  checkout differences remain recorded; no normalization/compatibility redesign.

These are SOURCE counterexample analyses, not executed test cases. Actual
stdin/protocol behavior, transport bytes, materialization and product regressions
remain to be established by any separately admitted03 invocation.

## Early origin, cleanup and fresh binding boundaries

Only after validated new E/R allocation, an in-memory origin record and exclusive
fsynced `OUTER-ALLOCATION.json` retain their original pins, parent pins, exact
source/request/approval images and03 run/purpose. File and directory/parent fsync
ordering was checked. Subsequent subdirectory/materialization work follows that
record. Its1572/raw labels describe INTENT, not completed materialization.
Failure before record construction or hard loss may still leave no durable
record; the final receipt fallback is qualified, not guaranteed recovery.

The only top-level cleanup delta replaces `source.tar` with `source.oids` and
`source.blobs`. No early-failure delete path was added. All exact inner safety,
source-after, original authority, namespace and mount/inventory/deletion gates
remain prerequisites. This adds no origin/cleanup authority for old02 or another
consumed runtime; preterminal/final-close uncertainty is unchanged.

03 R is `/root/projects/PassVault/audit-runtime-linux-isolated-batch03`; E is
`B/runs/linux-isolated-batch03`. SELF/INNER/INIT, request03, reviewer approval03,
RUN `linux-isolated-batch03` and PURPOSE `ONE_LINUX_ISOLATED_BATCH03` agree, as do
the independently hashed inner/init pins. Original lock path and strict11-key
request/seven-key approval formats remain. No current parent/tool/lock pins,
R/E absence, processes/resources/CI or actual request were checked here.

C4 remains commit `da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`, pinned1572-raw-object SOURCE manifest.
Publication HEAD is not this workload.166 regressions plus one producer and17
expected XML captures gain ZERO execution/closure credit from this source review.

Root must await the sole local/CI slot, obtain genuinely fresh coordination and
independent exact-instance acceptance, freeze source/request/genuine approval
before and under the original lock, and enter at most once. Old02 actual review
SHA256 `d34f4e9890a35a72aa54a36c9dcbf09511396d49f2c4b8b95f5e5e4fb9379eca`
remains consumed failure/runtime HOLD with scheduling-only release, unknown
actual failed path/cause and no automatic retry/adoption/deletion.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED,
all old FAIL/HOLD, hardware/native/Android32 gaps and publication fences remain.
Counts remain19/25 original confirmed,22/38 all confirmed,2/12 original suspicions;
eight PVD explanations separate. Only this permanent report was written; no
runtime, temporary files, caches or workers were created, and all reader FDs closed.
