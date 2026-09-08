# Clone-handoff evidence viewer — independent storage review

**REVISE — inert source review only; no viewer execution/transport acceptance.**

Reviewer: `/root/remed_storage`.
Target: new handoff root `docs/audit-handoff/evidence.py`, 7977 bytes, mode0644,186 physical LF,
SHA-256 `98fe54c801007d6a7d87f3bc6ee69e17d48d153ec93c748aed68f1d86bb6da56`.

This target is a new handoff document/helper, not one of the811 frozen G12 source members. Root alone authors revisions and authorizes any narrowly bounded transport check. PACK/INDEX/PACKAGE bytes are not yet frozen or reviewed here. Nothing in this review admits app builds/tests, imports packed code, reopens old runtime/cache/recovery, or changes issue status.

## V-F01 — pathname checks do not bind the opened file (39–53)

The component `is_symlink()` loop and later `path.open()` are separate pathname operations. There are no retained root/component descriptors or no-follow final open; a component/leaf can be replaced after its check. No pre-open regular-file identity is bound to the opened descriptor. The after-read comparison covers only inode/size/mtime of the descriptor, not device/ctime/mode/link count or the final pathname.

Consequences within the advertised reader boundary: a replaced path may be followed outside the intended root; an old opened file can be hash-checked and represented as current pathname content; a non-symlink FIFO replacement may block at open before the regular-file check. No hostile-path schedule was executed.

Required: descriptor-relative, no-follow original root/parent/leaf handling with explicit supported-platform policy; bounded regular files and suitable nonblocking/special-file refusal; stable pre/open/post-fd/post-path identity without digest rebasing. Retain and verify root/ancestor identity through the read. Do not silently downgrade to a pathname race on unsupported platforms.

## V-F02 — archive limits omit decoder memory and pre-yield TAR expansion (96–120)

The32MiB compressed cap,16MiB yielded member cap and80MiB sum of `member.size + 1024` do not bound all expanded TAR bytes or the XZ decoder dictionary. TAR extension metadata, GNU long-name/PAX framing and padding may be processed before a regular member reaches the application loop. The sum is applied only after a yielded member. No decoder memory limit is supplied.

Required: explicitly bound XZ decoder memory and total expanded output **before** TAR parsing can allocate/consume unbounded framing. A small approach is a fixed-format, memlimit-bound `LZMADecompressor` with an expanded-byte cap, exact EOF and no unsupported concatenated/trailing compressed stream, followed by parsing only those bounded TAR bytes. An equivalently bounded streaming adapter must charge every decoded byte. Reject extension/sparse framing outside the writer's declared plain-blob format; do not rely on a truthiness check of a sparse map as a complete format contract.

Compressed hash agreement is necessary integrity within the supplied package, but not a substitute for parser/decoder bounds. No archive, decoder or hostile fixture was run here.

## V-F03 — PACKAGE-only packs can escape membership validation (172–176)

The first loop hash-reads all PACKAGE entries. The second chooses only pack names referenced by indexed blobs. A PACKAGE-listed evidence pack with no index references is therefore never passed through `read_pack`; its unknown/extra blobs do not participate in the later exact membership test.

Required: validate pack path/schema, require equality of the PACKAGE evidence-pack set and the index-referenced pack set, and verify each resulting exact per-pack member set. Missing, extra or orphan packs must refuse rather than disappear behind an aggregate path/blob count. This is separate from legitimate multiple logical-path aliases for one exact blob.

## V-F04 — terminal-control neutralization is incomplete (31–35,155–169)

Logical paths allow control/format characters, and `list` prints names without escaping. `show` neutralizes C0 except LF/CR/tab and DEL, but permits C1 controls such as U+009B/U+009D and bidirectional format controls. CR can overwrite the beginning of a displayed line. Terminal behavior varies, so this is a reader/display-boundary concern rather than a demonstrated terminal exploit.

Required: reject control/format characters in logical paths and consistently escape nonprinting C0/C1/DEL/format characters in list/show output. State the LF/tab policy explicitly and escape CR if source fidelity is represented through escapes. Stored evidence bytes/hashes must remain unchanged.

## Working protections and scope limits

- No packed-code import/execution, archive extraction to disk, subprocess/network operation or write/delete path was found in the186-line target.
- `extractfile` is used only after the yielded member's regular-file/name/size checks; it is not `extractall`.
- Source verification is bound to the exact raw G12 manifest hash and canonical identity, checks811 unique source paths, and correctly disclaims extra-file inventory, semantic review and build admission.
- Indexed path aliases are explicit; the same blob hash must agree on size and pack. Source/member path counts must not be replaced by deduplicated blob counts.
- PACKAGE itself is the local authority read by this helper. Integrity against that manifest is not authenticity of a mutated package+manifest pair; root must bind the final helper/PACKAGE/INDEX/pack hashes to the reviewed publication/commit.
- Parsing an index does not prove37 issue records, all family variants,12 PVUs,8 PVDs or the19/25,22/37,2/12 denominator meanings. Direct copied-ledger and root-document reconciliation is separate and pending.
- Original source-copy verification has independently matched all811 source tuples and122 changed members. It does not accept this viewer or root's still-changing extra docs.

## Required follow-up

Root should author a new exact revision, preserve this target/review as historical evidence, provide its hash and final package schema, and obtain fresh inert source feedback. Any transport-only execution remains a distinct root decision; an unexecuted source review is not a successful transport run. No original-source/frame mutation controls are automatically added.

The copied release-provenance skill was consulted for immutable source/artifact identity, original-versus-derived evidence, and publication-authority separation. Its release scripts/build/rehearsal directions are not applicable authorizations for this narrow read-only task and were not run.

**Permanent fences:** G7/G8 CLOSED; PVA029 FAIL/no automatic retry; PVU007 STOP/no investigation/reformulation; PVU011 NO RETRY/no containment relaxation. No product or central coverage credit; no source edits or Git mutation by this reviewer.
