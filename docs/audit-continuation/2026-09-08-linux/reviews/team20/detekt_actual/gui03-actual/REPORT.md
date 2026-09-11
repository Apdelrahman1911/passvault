# GUI03 actual-run independent reconciliation

Reviewer: `/root/detekt_actual` · 2026-09-11  
Run: `linux-desktop-integration03` · **consumed; no automatic retry**

## Disposition

**Accept the retained terminal failure for reconciliation, not execution success.**
Root may release **only the consumed GUI03 scheduler reservation** after accepting
this review. Current-R cleanup remains **HOLD / NOT_ATTEMPTED** and requires a
separate, genuine independently reviewed current-R admission. This review performs
or authorizes no runtime probe, stop, cleanup, build, Test, replay or publication.

Root may also **separately and narrowly release the already accepted W
application/test after-images**, outside the bound GUI03 control packet, once
C17 before tuples and the consumed disposition are preserved. This is not a
blanket source/T/index/config release and is not a re-review of those after-images.
The concrete preservation boundary is below.

## Exact identity and review method

- C17 commit: `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49`.
- C17 tree: `d1bd6ca5b18d08ff3ff15896d9a78af9016be799`.
- Read only GUI03 E, its C17-preflight `EXTERNAL-RESULT.json`, and the exact
  bound inner/init/outer/SOURCE/approval as data. The request file itself was not
  read: its historical image agrees across allocation, intent, receipt and approval.
- No helper execution/import/AST, Git/T access, runtime/SDK/process probe, build,
  test, cleanup or subdelegation. Source control flow was read as text.
- **56 retained-data comparisons agree.** All 19 retained files
  (**48,396 bytes**) and six evidence directories were reconciled. Strict JSON
  rejects duplicate/nonfinite values. No-follow bounded reads checked original
  metadata around each read; second reads and a final exact-input refresh agree.
  Full SHA256/pins are in `RECONCILIATION.json`.
- The four bound sources and approval still exactly match their retained
  hash/pin images. Historical tool/R/T images were not freshly probed.

## Actual result, not planned coverage

| Layer | Reconciled observation |
| --- | --- |
| Root admission | Tool `ee8136`, exit 0; bounded negative host point, not continuous idleness |
| Raw source Git child | Original PID 15510, exit 0 |
| Preparation | Started/completed original wrapper, namespace PID 2, **exit 143** |
| Original preparation stop | Same wrapper/environment, namespace PID 378, **exit 0** |
| Original unshare | PID 15641, **exit 70**; no outer pidfd-kill attempt |
| Inner result | Safety path **70**: source-after and cleanup-safe false |
| External authoritative final | Launch `595e57`, session 28793; final `e68e02`, **exit 70** |
| Outer receipt | **HOLD**, preterminal receipt, elapsed 92.72s; cleanup **NOT_ATTEMPTED** |
| GUI/render/Test | **Unstarted; zero supported executed application cases, zero XML suites, zero passes** |

Preparation intent/phase/log and the original stop agree exactly, including
1800s/600s envelopes and environment SHA256
`337ba095c634a7c52a57e9efa75847433b5b00edcd3422762b4b6201f3c4855f`.
The preparation log contains only Gradle 9.7.1 distribution/bootstrap and
single-use-daemon messages: **zero `> Task` headers and zero runtime-classpath
receipts**. The stop log says `No Gradle daemons are running.`
No compiler, executed Gradle-task, application or JUnit pass is inferred.
Command `complete:true` means direct terminal plus log EOF, not exit-zero success.

The declared four cases/four XML suites in two KMP Test tasks never became
execution evidence. The three Main/PVU003/PVA027 records have
`preserved:true` but empty required images, zero required bytes, no observation,
no crash diagnostics and `mapping_ok:false`: available empty metadata was
preserved, not tests passed. Nine child roles are not nine cases.
Private network/tmpfs, panel and tray-owner booleans are false because their
stages did not start; this is not a native-stage failure or success.

## Cancellation, host uncertainty and signalling

`CANCEL` exactly matches the first retained reason:
`RuntimeError:host-screen candidate uncertainty: 17963:proof-owned-domain`.

The detailed candidate is Java PID 17963, PPID 17929, start 35919598, with stable
birth reread and an original candidate pidfd observation showing no terminal
readiness. Its PID/mount namespaces equal the **parent** pair
`pid:[4026531836]` / `mnt:[4026531841]`, not the admitted original owned pair
`pid:[4026532116]` / `mnt:[4026532115]`. `owned_proof:false` is correct.
This is foreign **to that admitted domain**; it identifies neither a responsible
user/build nor a process root may adopt, stop or kill.

The later retained reason is
`RuntimeError:host-screen candidate uncertainty: 16024:proof-namespace:mnt`,
followed by `RuntimeError:child failure/uncertain finalization`. No detailed
second identity is retained. Do **not** relabel PID 16024 as positively owned,
attribute its executable/cause, or import an older namespace-number association.

The inner command records `command timeout/cancel/resource/output failure`;
its abort path includes SIGTERM through the original wrapper pidfd. Cancellation
is sticky, while the already-required original stop is still allowed. Preparation
failure prevents GUI/Test entry. The total outer duration is far below the
preparation envelope, but no timestamped complete causal trace exists: reason
order and file metadata do not justify a finer chronology or a claimed 1800s
timeout. The negative admission point and later positive candidate are not a
contradiction.

Both **outer** original-child `pidfd_kill_attempted` flags are false. Candidate
screening is observation-only. The external qualification's broad “no process
adopted or killed” must **not** be promoted to “no owned process signalling”:
the inner abort path is separately scoped to its own original child.

## Settlement is not final safety or filesystem cleanup

Original preparation stop, phase settlement, whole private namespace emptiness
and GUI-helper settlement are retained true. `GUI-CLEANUP.json` has no helpers,
native owners or remaining-descendant records, and no populated native-owner
descriptor-settlement proof. It is an **unstarted-GUI namespace-settlement
record**, not a filesystem-cleanup receipt.

The inner finalizer preserves available Main metadata after settlement, skips
the full AFTER source scan when cancelled, and rejects cancelled final evidence
acceptance. Hence `source_before:true`, **`source_after:false`** and
**`cleanup_safe:false`**. Preparation's error-bearing command also fails the
complete-success prerequisite. Missing final acceptance is **not proof of
source drift**, but cannot prove source unchanged.

The outer driver sees cancellation reasons/child 70 and fails before its normal
inner-result acceptance, final raw-source check, authority/watch and
`remove_runtime()`. Thus the missing outer `inner_result` acceptance image,
GUI scope and mount/cleanup proofs are consistent, not fabricated successes.
The preterminal receipt is reconciled against the actual external exit 70.

Original R allocation is recorded as
`/root/projects/PassVault/audit-runtime-linux-desktop-integration03`,
dev 23 / inode 1552647 / mode 0700. That historical tuple is **not current
cleanup authority**. R was not accessed in this review. No automatic replay,
repeat stop or cleanup is admitted.

## Source and evidence accounting

Manifest-derived retained-data reconstruction agrees for 3,042 unique raw
members / 103,345,745 bytes, the 15 checkout-normalization differences and the
two separate historical EOL qualifications. The canonical raw tuple SHA256 is
`ba8d76961cec8b96dcd168880bd2a456175fcbbfbbb50fb217f991c8ff49dd9a`.
The manifest-ordered OID request reconstructs to 124,722 bytes and SHA256
`6dddf0bc1d59e425fa85fd0eafad02deec739aa741c05b28b0927cb4b737039e`.
Expected raw stream framing is 103,503,964 bytes. The retained stream image and
two-pass record agree internally; **the runtime stream itself was not read**.
Selected fixture/production hash references agree with the bound manifest,
not freshly inspected W application/test files.

Four outer resource points remain above their floors; sampled minima are
18,576,900,096 free disk bytes and 40,628,170,752 available memory bytes out of
67,435,888,640. Four inner inventories report finished traversal/no vanished
entries. These are historical point samples, not continuous resource, current
host-idle, ownership or cleanup proof.

## Scheduler and concrete source/T freeze boundary

1. **Scheduler:** root may record this reconciled consumed terminal failure and
   retire its specific execution reservation. No original admitted child is
   left active in the retained terminal record. This says nothing about current
   unrelated host work. A later permissible action needs its own fresh
   conflict/resource/coordination admission and required independent review.
2. **Preserve frozen:** current R; all GUI03 E evidence; W
   `scripts/audit/linux_desktop_integration_03.py` and
   `scripts/audit/desktop_integration_03.init.gradle`; B outer
   `LAUNCH.py`, C17 `SOURCE.json`, exact request and genuine approval.
   Preserve their images and allocation provenance for the separate closer.
   Keep immutable C17 T objects/manifest, the old `INDEX.bin` and T/index/config
   state frozen until that closer is reconciled.
3. **W application/test edits:** consumed GUI03 materialized these bytes from
   C17 T raw objects into **R/checkout**. The inner source checks address that
   disposable checkout; selected W-looking paths are manifest keys, not
   reads of current W application/test files. Once this actual review is
   accepted, root may explicitly release only the previously accepted,
   unapplied W application/test after-images **outside the frozen packet**,
   preserving their C17 before tuples. No unresolved GUI03 execution requires
   freezing unrelated W implementation indefinitely.
4. **Do not conflate states:** that W-only permission neither repairs
   `source_after:false` nor changes cleanup HOLD, T/index/config, source
   identities or publication authority. It does not itself authorize builds,
   staging, config/dependency changes or edits to bound helpers. Record changed
   W after-images as new/unvalidated source; neither C17 GUI03 failure nor a
   prior unrelated pass validates them. This review has not inspected or
   newly accepted the after-images.
5. **No automatic retry:** the exact GUI03 name/packet remains consumed.
   Cleanup needs a genuinely new independently reviewed current-R admission;
   its scope and proofs cannot be borrowed from this scheduler recommendation.

PVU007 STOP; PVU011 NO RETRY; PVA029 49 checks / 44 PASS / 5 FAIL with no automatic
retry; G7/G8 CLOSED; all older held-runtime/consumed/native-refusal fences remain.
No protected refs, dependencies, identities, signing, Store/publication or
occupied 1017001 changes are authorized.

## Review artifact

`RECONCILIATION.json` SHA256:
`d94b78c84909e7e2728e4ad4da2eae874814f573a01e005ded088f2d88302830`.

All comparison checks above are **review calculations, not application tests**.
