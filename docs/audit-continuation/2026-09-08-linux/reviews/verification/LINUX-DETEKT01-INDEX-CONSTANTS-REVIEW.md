# Detekt01 — independent index and constants linkage review

- Reviewer: `/root/verification`, independent of the Detekt helper/binding author.
- Date: 2026-09-10. Own stable index observation: `2026-09-10T12:04:03.266217+00:00`; successful inert-read/reversal receipt: `8fc27a` (exit 0).
- Scope: retained stage0/source/index metadata and constants-only bindings. Reuse the accepted whole-body review below; **this is not execution, instance, coordination, cleanup, or tool-resolution admission**. TRAY01 is recorded terminal exit 70 after a host conflict during prepare; its runtime/attempt HOLD remains root/storage-owned. This note does not itself release a slot or authorize Detekt.
- Paths below are relative to `docs/audit-continuation/2026-09-08-linux/` unless stated otherwise. No Git command, helper execution/import/AST/syntax test, build, CI, runtime/process/cache/SDK/network probe, index copy, or source mutation was performed in this review. Only this note is newly written.

## Inputs and source linkage

| Input | SHA-256 |
|---|---|
| Accepted whole-body review: `reviews/verification/LINUX-DETEKT01-SOURCE-DELTA-REVIEW.md` (17,050 B) | `a3f6935fc90b92d76d1719cc9121889cae106a31b0f6baf816fd8353700defb8` |
| Reused Source02 manifest: `reviews/desktop-tray/source-prepare02/SOURCE.json` | `ee9361f96da7cc7de04b8949453acf971027640144527961af0d38388a561ffc` |
| Root capture: `reviews/detekt01/INDEX-SOURCE-CAPTURE.json` (4,868 B) | `164330da94ae2675c6e20b3af975698dcdba15a92899526d430d3f0c08dea959` |
| Binding record: `reviews/detekt01/CONSTANTS-BINDING.json` (4,345 B) | `92be1e9ed7586d0909c3cae4dc43bfc3cfae6908f23be003dce570abb9bf1bd7` |
| Existing Source02/route review: `reviews/storage/TRAY01-SOURCE02-CONSTANTS-ROUTE-REVIEW.json` | `29f212c56cb0ca683a0db89814bc642738d06a4475556af1388c880984fdf479` |
| Subsequent root PATH snapshot: `reviews/detekt01/PATH-SHADOW-OBSERVATION.json` (1,686 B) | `b204548163d0c9ca49497aa4643471a79ff91f4648c605c81edcc6666ce443bb` |

C13 is consistently bound to commit `d3d46db51d9fa69a4060250e71e0477c6b930d3a`, tree `245160648cb79873f41da41969a5b48d4c2057e1`, and **2,495 unique files**. Source02 is reused, not recaptured. Its representation remains `RAW_IDENTITIES_FROM_OBSERVED_CHECKOUT_OR_EXACT_OID_VERIFIED_CRLF_TO_LF_CANDIDATE`, with two historical checkout-EOL qualifications. Checkout fields describe actual observed T-buffer bytes; raw identities are those same bytes or an exact OID/size/mode-verified CRLF-to-LF candidate. **This is not observed cat-file transport.** The prior actual Source02/route review is retained, not redone or upgraded here.

`reviews/detekt01/INDEX-STAGE0.raw` is 329,860 B, SHA-256 `08fd39e48f812cedd274b61b0d0ba7d4695a98ccf6586fb4f63b14860b7fdca5`. Independent parsing found complete NUL termination and 2,495 unique stage-0 records. The entire mode/OID/path map exactly equals Source02: no missing or extra entries. The capture's `git ls-files --stage -z --full-name`, exit 0, empty stderr, and reaped direct child are **root-recorded execution facts**, not commands this reviewer ran.

With root's explicit one-read permission, I separately read the current binary index at `/root/projects/PassVault/passvault-publication-20260910-01/.git/index` through stable, bounded, no-follow reads. It is 396,316 B, SHA-256 `a6d0cc478dc014fe6668988296ea623fc304dad00534e23bd79174d0c0b4ece4`. Its current pin exactly matched root's earlier capture:

```json
{"dev":24,"ino":14296732,"uid":0,"mode":33152,"nlink":1,"bytes":396316,"mtime_ns":1789035283789321565,"ctime_ns":1789035283790321565}
```

Independent binary parsing verified DIRC v2, 2,495 entries, the SHA-1 trailer, valid zero padding, unique byte-sorted names, correct pathname-length flag bits, and zero high four flag bits (no extended/assume-valid/unmerged state). Its full mode/OID/path map exactly equals both retained stage0 and Source02. The sole extension is `TREE`, 27,812 B, SHA-256 `760f51ba9b946f9ea42776b6195d63d97d9cc92bb6ac8879e590bc2e14f489c0`. I do not claim semantic interpretation of its cache payload or equate index stat-cache sizes with raw Git sizes. This current stable data observation is distinct from the earlier root Git-command capture and does not warrant future index stability.

## Exact constants-only proof

I read the final helpers as inert bytes and reversed every recorded substitution **exactly once, in reverse order**. Whole reconstructed byte buffers recovered the accepted originals exactly; no generic-body edits remain outside that recorded delta. Accordingly the prior whole-body review is reused, not repeated.

| File | Final bytes / LF / SHA-256 | Reversal result |
|---|---|---|
| Repo `scripts/audit/linux_detekt_01.py` | 47,742 / 821 / `a7895c455f7cf01577a46890d9fc0b72aca94bfc7766c6b9cd9bde299f03140b` | 3 substitutions; 47,451 B original, `13894d0354fb0e607f588981cb2d12276a5fbba28bc23e890f843ad7202b918e` |
| `reviews/detekt01-outer/LAUNCH.py` | 53,680 / 912 / `4422ae62c1fd461de9f66b2b880567c8cd52bfc2b8cab159ee9692f3d64cae2c` | 5 substitutions; 53,064 B original, `1c5755bcda9c6eb3d83ce6827ec42c0d1cebdc468512593096b391b5391670b6` |
| Repo `scripts/audit/detekt_01.init.gradle` | 2,799 / 55 / `5beb8165ae491258c0fae5a5a4f119cbe778157d6a577aacff79fe93fc33ae97` | Unchanged |

The declared bindings agree with the retained captures: Source02 path, C13 commit/tree/count, Git image `/usr/bin/git`, resolved Ruby image `/usr/bin/ruby3.2`, `EXCLUDE_STATE='INFO_ABSENT'`, new-store `GITDIR/index`, outer directory/file devices 23/24, original lock pin, and FROZEN helper/init/source hashes. This establishes metadata/constants linkage, not current live tool/config/exclude validity.

The captured 435-byte config's embedded text independently rehashes to `036c10a0cc4303fa7de6578390ad7c8094d2a64796f8bb6a6b63cc7954f662d5`: basic core settings, public HTTPS origin, and explicit continuation-branch fetch/tracking, with no include/promisor/worktree/custom-executable/config-redirection entry in that captured text. Root recorded `.git/info` and redirects absent, a regular Git alias, and `/usr/bin/ruby -> ruby3.2` plus the resolved target. I did **not** re-probe those live paths.

A subsequent root data-only PATH observation at `2026-09-10T12:08:23.480296+00:00` was independently read/hash-checked/JSON-parsed in receipt `87c473`, not re-probed. It records the exact selected PATH `/usr/lib/jvm/java-17-openjdk-amd64/bin:/usr/bin:/bin`, `git` and `ruby` absent in the earlier JDK/bin component, a regular `/usr/bin/git`, and `/usr/bin/ruby -> ruby3.2`. Its alias pins and image hashes/sizes agree with the earlier capture: Git `2a8c18fbf43da9f692d75474c72bea9dfd796c260b0f3dfe456376abc3bbd668` (4,066,232 B), Ruby `ead53704d7da35c85a28db19070d34e7ecef9461ecf8ae5e9d51ae79125bb731` (14,488 B). This supplies the formerly omitted earlier-PATH **snapshot evidence** for the two fixed commands and is consistent with their recorded `/usr/bin` route. It does not prove actual invocation, future namespace stability, or an independent current live-tool observation by this reviewer.

## Decision and remaining gates

**Accept exact retained metadata and constants linkage only. No Detekt execution admission or result is granted.**

1. The earlier-PATH snapshot omission is now addressed by independently reviewed **root-recorded data** above. Fresh exact runtime tool identity, namespace/freeze, and bare-command PATH-resolution admission remain necessary; no future resolution or generic PATH/security inventory is established by those snapshots.
2. A fresh exact request and genuine independent instance/coordination/tool/config/index/cleanup admission, including a cooperative store freeze, remain necessary. Root subsequently reported independent TRAY01 reconciliation and release of **scheduling/new-T active-use freeze only**, through `reviews/desktop-tray/SLOT-RELEASE.json` (reported SHA-256 `6eb61d38c054a777e2a201bf4f3a17e4b5da6d63d23d17cbb3828addbb2cbc68`; reported slot SHA-256 `bb2e931b9412af93ea5c8ffccb17006565e8780b011e21330277734662b391e9`). This is attributed root coordination evidence, not a release adjudicated or independently re-read in this metadata lane. Runtime/attempt HOLD remains unchanged; await the explicit fresh Detekt instance packet.
3. Actual Detekt execution, reports, wrapper stop, owned-worker settlement, and namespace cleanup reconciliation remain future evidence. No stopped/clean claim is inferred from this static review.
4. Intended scope remains **22 generic Detekt tasks plus 1 coverage task, zero application test cases**. Neither metadata agreement nor task completion would itself prove Detekt engine visitation, hardware behavior, a product fix, or readiness.

All PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED, consumed/HOLD scopes, eight PVD boundaries, and publication/signing/store/mobile-1017001 restrictions remain unchanged. No closure numerator, application-test count, hardware result, or overall-readiness percentage changes. No persistent process, cache, generated build output, or large temporary artifact was created by this review.
