# Detekt02 — C16 literal bindings and retained-index review

**ACCEPT_EXACT_BINDING_DELTA_AND_RETAINED_INDEX_ONLY.** Reviewer `/root/verification`; binding/capture owner `/root`. No source blocker found. This record does not approve an execution, coordination/cleanup scope or the separately supplied new instance packet. No instance ACCEPT is created by this binding-only review.

B=`docs/audit-continuation/2026-09-08-linux`, relative to W=`/root/projects/PassVault/passvault-linux`.

## Exact bindings, not a second source audit

| Subject | Accepted unbound SHA256 | Bound SHA256 | Bound bytes / LF |
|---|---|---|---|
| `scripts/audit/linux_detekt_02.py` | `e5b2352080d4eafc2d22c3a50a7d3f4b31f4b324672cab1e59bdef8b91a2d294` | `6dd7aeaca35c19b9406eb72f1c49578433ff71f0ce4d80c26c812542543d9bfe` |55392/955|
| `B/reviews/detekt02-outer/LAUNCH.py` | `545de8da07253be6153533d8e97b583aab3b1c78f9d39f0c399d277509d4ce3a` | `f5dc403ea773ae4d844a617a38da2b75a768b93a03d2233c968ddaad824950e1` |53730/912|

`B/reviews/detekt02/binding01/BINDING-DELTA.json`:3390B/87LF, SHA `952e1dfd1dfae1b51bf78e9f9055c758936642aefbec25287be1455e1f90fef6`. Init remains2838B/55LF, SHA `2ff75e0d26f4bd2d7bca7a0c62d929fed9174115ad20b665e5b10caabe51b9e6`.

Independent full forward and inverse substitutions recover the exact accepted unbound bytes:
- Inner: **five** single-occurrence literals — commit,tree,members,exclude state,SOURCE hash.
- Outer: **eight** — those five categories plus DEVICE,EXPECTED_LOCK and bound INNER hash.
- No functional body, parser, task list, `--continue`, return-code/retention, process/original-lock/source/index, environment, resource, cleanup or cancellation machinery changed.

Reuse the earlier source reviews and unchanged successful parser controls; no control/test was rerun or credited here. My exact outer review `B/reviews/verification/LINUX-DETEKT02-OUTER-DELTA-REVIEW.md` (SHA `0afae9eb0de75f840b4530d4e8f330f5397b38c6b27d8190a89e93033247bb55`) accepted the unbound545de8… body and its inner contract, retaining the separate inner/parser review lanes.

Crucially, C16's manifest contains the **unbound** helpers, not silently edited published source. The independently recovered inner/outer bytes match the manifest's raw size/hash and Git OIDs `5eeb6748566572fcc4e61077313abdb4e1378f5d` and `9bc4a7ad2546241b9b8ad00c998ca80725e4c61d`. The published init also matches. Bound external control helpers remain separately identified; there is no raw-checkout identity substitution.

## Source identity and retained preparation

C16 commit `e6738b17a7c783383a4f0ae0f17af726cffb9a40`; tree `c4009ab5f9ba23bb8097d64cc54328b131baa0c8`; **2887 members /97700141 raw bytes**.

- `B/reviews/detekt02/source-prepare01/SOURCE.json`:1575985B,SHA `2af03198511f26cfafb0ddd01470e66585697507e95c654530e1621b5ce0831b`.
- Same directory `SOURCE-CAPTURE.json`:5880B,SHA `02c13da143a18494504277ddc10dad8bc2496e2eabcece71cda8df5d858faae1`.
- Independent preparation review `B/reviews/android32/c16-source-prepare/C16-SOURCE-PREPARE-ACTUAL-REVIEW.md`:4210B,SHA `c0a7ec26d898bc924e71e86799e959164eb1afd7d0db51d70dd6cabcb35c576b`.

All were rehashed/read. The preparation reviewer accepts consumed metadata work, not validation admission: two read-only Git children0/reaped, source/listing consistency,15 qualified CRLF differences and the two preserved historical EOL qualifications. Those two scripts overlap the15; they are not extra normalization paths. Observed checkout buffers and OID-qualified raw candidates remain distinct; no new raw transport or engine-visitation proof is invented. The later current Detekt01 cleanup changed workspace-parent nlink; preparation's earlier parent pins are not fresh instance facts.

## Independent ordinary-index decoding

Under `B/reviews/detekt02/c16-preflight/`:
- `INDEX-SOURCE-CAPTURE.json`:9341B,SHA `7627358285d9349a1ed32387ffb8079b962f5868e1837030b8fa23bd532cd7e2`.
- `INDEX.bin`:461842B,SHA `8944700e1ea719c4e0a10e3236e0f680d8c98f441293c2e94ae067d81f9dd253`.
- `INDEX-STAGE0.raw`:387558B,SHA `2e1d74406734b3a2d2b7af3d37e1698b5c8afd2c5b376f7bf3f140af0b14d2a2`.

I parsed the retained binary directly as inert data, without Git or a target helper:
1. DIRC **version2**,2887 bounded complete entries; SHA1 footer `3d983e2e3c9c1dd1cbf613f18cfb1935bcd293c7` matches all preceding bytes.
2. Strictly ordered unique safe UTF-8 paths, matching pathname-length flags, NUL terminators and zero8-byte-relative padding. Every high flag bit is zero: no assume-valid,extended,skip-worktree/intent-to-add or nonzero stage entry. No sparse-directory,symlink,gitlink or null-OID entry.
3. **2813 mode100644 +74 mode100755** entries. Full ordered path/mode/OID equality holds between all2887 binary rows, all2887 independently parsed NUL stage-listing rows, and the exact C16 manifest. No missing or extra source/index path, sparse/split or unmerged state is hidden by a task count.
4. Exactly one bounded optional **TREE** extension:29578B,SHA `1bc021f6f3545c36f3b31088480355d001942e01c44ba3651935e983f70e2fc4`; no mandatory link/sparse extension or trailing unaccounted bytes. TREE is an opaque checked cache payload here, not extra index membership or an independently verified Git tree.

The capture's sizes,histogram-relevant count,version,extension hash and full source identity agree. Its one retained read-only stage command records exit0/direct-child-reaped,empty stderr,and the same387558B stdout hash; the index is recorded unchanged afterward. These are retained/root-recorded observations, not another live-store read.

## Config/tool/origin qualifications and next boundary

The retained435B config text reproduces SHA `036c10a0cc4303fa7de6578390ad7c8094d2a64796f8bb6a6b63cc7954f662d5`: ordinary repositoryformatversion0,filemode=true,bare=false,logallrefupdates and the known continuation remote/branch mapping; no include,filter,hook,fsmonitor,worktree or executable-setting directive appears. Captured exclude state is INFO_ABSENT, matching both bound constants. Root records absent commondir/config.worktree/object alternates, tool aliases/images and preceding-PATH shadows.

The bound lock tuple exactly matches this retained Linux capture; directory device23/file device24 match its metadata/index facts. These are **not** current host/exclusivity or future deletion authority. No live T/config/index/lock/tool/proc/SDK/runtime/CI probe occurred. Independent instance review must bind the exact current index/image/config/exclude/parent/request/approval set and root's cooperative store/coordination freeze; outer must recheck it under the original lock and seal only the admitted index copy. No old store, fabricated index or previous parent nlink is adopted.

Static scope remains22 generic Detekt tasks plus coverage, zero application cases; ordinary retained static failure remains failure, uncertainty remains HOLD. A successful preparation or index mapping is not a Detekt PASS.

Reader tools:c1a43d(packet),7fa558(capture/manifest/preparation review),d28620(complete literal diff/inverse and independent binary/stage/source decoding); all returned exit0. Only bounded named W source/retained data were read. No target execution/import/AST/syntax check, redundant control,test,Git,live store/runtime access,signal,stop or deletion. Only this permanent review is created, exclusive0600,file/parent fsync and exact readback; no cache,temp/build output or worker created.

Zero cases,fixes,closures or denominator changes. Current Detekt01 cleanup remains consumed/accepted; original Detekt failure,other older HOLD/consumed scopes,PVU-007 STOP,PVU-011 NO-RETRY,PVA-029 recorded failure/no automatic retry,G7/G8 CLOSED,eight PVD boundaries,hardware gaps and protected-ref/signing/store/publication/occupied1017001 fences remain. The newly supplied instance request is a **separate** review, not approved by this record.
