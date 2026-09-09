# Isolated-batch01: genuine independent exact-instance decision

Reviewer `/root/verification`; 2026-09-09. **ACCEPT_EXACT_NEW_INSTANCE**, solely
request `e15e29322dc3a6f018b3abfc053ee7cc0fb2d000f7ad878144055958bbeaf58f` for
`ONE_LINUX_ISOLATED_BATCH01`. This independently reviews the corrected source and
actual packet; it is not an author-provided acceptance label, an executed test
result, an old HOLD release, or unconditional permission to launch immediately.

The genuine reviewer-owned `LINUX-ISOLATED-BATCH01-INSTANCE-ACCEPT.json` has
SHA-256 `e266daffd496e3612f26379d60462e09e27fb68076bdbf730c5f61ab06347122`.
Root must independently freeze that image immediately before entry AND under
the original lock, along with the exact request/source images. The request does
not contain an approval hash, avoiding circularity. Root alone may execute once
only after the fresh coordination/resource gates below actually hold.

## Exact packet and independent data verification

Paths here are relative to `docs/audit-continuation/2026-09-08-linux/` unless
otherwise stated. Refreshed `requests/LINUX-ISOLATED-BATCH-01.json` is6806B,
regular0600/root/single-link, dev24/inode14253233,
mtime_ns=ctime_ns=1788964277765553854. Its hash is the decision identity above.
An independent bounded standard-library DATA reader, tool `cc24fc` exit0,
verified stable no-follow file/ancestor reads, all13 requested image tuples,
all four original parent dev/inode/UID/mode identities, the complete original
empty lock pin, and absence of the two proposed fresh R/E names. It did not
acquire the lock, execute/import/syntax-check a subject, or perform any process,
namespace, mount, CI or old-runtime probe. This is13 image checks, NOT13 tests.

- Corrected outer `reviews/isolated-batch/LAUNCH.py`:34820B/612LF,
  `c5e0a4b5ca35a9dced05e2b3a2795d9a0d4169629c8ce85b2d963b28a4cce405`.
- Inner `scripts/audit/linux_isolated_batch.py` (repository-relative):37171B/669LF,
  `15cbad1ce8af84d6fb1f56a92ba70494148ffc0a19e3ca9af7eb16d2dfa0ada5`.
- Init `scripts/audit/isolated_batch_tests.init.gradle` (repository-relative):5430B/101LF,
  `24a301c6cc8818eef5e61c8a10fe03a4b4f781bd4abac4ab4266bc6985583ab1`.
- Full SOURCE manifest:
  `2e8b82c91215bbfee9cf4a2a3f09bad386fc8515d7fd20263fbbfa5fe1217003`;
  CLASSES `95e6ed4f8e5cbb92e4e6840a08e16613b14674555f7f99cd24e34b3ddcd18bda`;
  METHODS `10cb11cd6392e43ab7ec3e94e2f348ccd2caf6cd2127830fe9e1287ea10210be`.
- The remaining verified image tuples are JDK17 `bin/java`/`release`, Git,
  unshare, Python3.12, the exact `python3 -> python3.12` alias plus its separate
  link pin, and env. Their full hashes/pins remain in the exact request; this
  does not attest every shared library or installed toolchain byte.
- Parent identities: workspace parent dev23/inode498323/mode0755; runs parent
  dev23/inode642474/mode0700; preserved Git-store directory
  dev23/inode504168/mode0700; coordination directory
  dev23/inode661121/mode0700; all UID0. Mutable directory nlink is not equality
  authority. Original lock is dev24/inode14189001/regular0600/UID0/nlink1/0B,
  mtime_ns=ctime_ns=1788910891124735946. Reading it does not prove lock availability.

Full product source remains **C4**, commit
`da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`, all1572 raw Git blobs. It is not C7
or a checkout-normalized copy. Fixed archive member/type/mode/blob checks precede
any namespace/build; both checkout-EOL qualifications must remain visible in
actual evidence. No fake/borrowed `.git` is added to the disposable checkout.

## Independently challenged source corrections

The complete original outer was read; its corrected diff was independently
checked against the preserved literal before-image
`reviews/isolated-batch/LAUNCH-before-9db5d9bd.py.txt`, SHA-256
`9db5d9bdbe1d6832f8528b855fb8bfbc37f06d8904638641b24a4795522bc00a`.
Exactly four bounded corrections were made in response to this reviewer:

1. A final positive comm read is now followed by exact birth/namespace identity
   equality. Otherwise an owned PID could vanish/reuse as an outside same-name
   process between observations while a stale owned row was accepted. Positive
   disappearance/change/unreadability remains sticky failure, not benign churn.
2. Host screening is bounded to8192 directory entries/5s. A disappearance before
   ANY positive indication is unclassified churn, not proof of unrelatedness.
   The comm list is explicitly nonexhaustive, with generic-interpreter/rename
   blind spots. Known outside audit work overrides a negative screen.
3. SIGCHLD is defaulted, cancellation handlers installed, and the inherited
   signal mask reset/verified empty before any child. Installing a handler alone
   would not make a blocked cancellation signal deliverable.
4. Parent mountinfo is bounded to128KiB/2048rows/5s and rejects a mount at/below
   R before cleanup inventory and again immediately before deletion. Separate
   directorydev23/filedev24 checks alone could miss a same-device bind mount and
   expose outside contents to deletion. This remains a cooperative-host check,
   not protection from concurrently hostile root mount mutation.

These are unexecuted source corrections, not observed infrastructure failures
or PassVault finding closures. The corrected inner/init review is separately
frozen at `reviews/verification/LINUX-ISOLATED-BATCH01-INNER-SOURCE-REVIEW.md`,
SHA-256 `2b0ea3cb39264ea17a234b66e031846054c3657de38615cec759fe0d3f31501d`.
Its source/EOL, private daemon channels, XML, stop, cancellation and incomplete
resource-sample qualifications apply unchanged.

The unadmitted request before-image is preserved at
`reviews/isolated-batch/REQUEST-7b5e5f9c-UNADMITTED.json`, SHA-256
`7b5e5f9c19f6f80c3d37ecc3bc70eca2a040a2ef92d298c0ea97aef3f3e408ee`.
Only the corrected outer image tuple changed. Independently read amendment
`reviews/isolated-batch/REQUEST-SOURCE-AMENDMENT.json` has SHA-256
`82fb91f1368d6c17c940e49366f0403bcdb5547d943d952b23e36f417f87ea96`.
Old source/request were never admitted or executed; amendment is not a retry.
Plan SHA-256 is `3a57c7478d94fbee85b7ee256398d9db63b19caad1f203bbe1d0fc20daed8259`.
Root's retained14:10 CI/syntax snapshot and corrected-outer AST-only amendment
are historical/static evidence, not a current coordination or product PASS.

## Remaining mandatory launch and actual-result gates

- Root must freshly establish all agents quiescent, no other audit-owned local
  or CI job, original lock/slot availability, exact input/approval/request pins,
  required namespace pair, still-fresh R/E names, disk/RAM floors and sufficient
  time. Request coordination booleans are these launch preconditions; they do
  not claim this reviewer was already idle when root prepared the request.
- Transient parent PID was correctly removed from pre-written request authority.
  The outer records a stable actual original parent at entry/underlock, verifies
  the request namespace pair against caller AND parent, and never signals or
  adopts by parent PID. Original Popen child pidfd is acquired before poll/wait;
  unreaped original birth/pid_for_children/mount binding must agree with the
  inner's positive private-proc preflight before cleanup can be considered.
- This is a trusted synthetic workload, not a hostile-code security sandbox.
  Fresh HOME/Java home/temp/Gradle/Kotlin channels address external daemon reuse;
  PID membership alone would not. Only positively owned direct pidfds are
  signalled; no numeric/name-based or unowned host signal is admitted. No prior
  unknown PID row, old held runtime or closed execution scope is reclassified.
- Outer work ends by5250s in a6000s cooperative userspace envelope, with up to750s
  cancellation drain and a once-only original pidfd kill/final5s wait. Inner
  stop600s/aggregate settlement120s and syscall/resource-sampling qualifications
  remain. Blocking kernel I/O or hard caller loss can defeat userspace handling;
  parent provenance is not a parent-death/descendant-kill guarantee.
- Cleanup requires normal original wait0/1, positive exact inner source/stop/
  settlement/evidence booleans, bound namespace/preflight, outer source-after,
  unchanged authority, mount refusal checks and complete descriptor-relative
  original snapshot. It removes only this newly allocated disposable R,
  including its generated source COPY/private caches, never permanent W source,
  tests, retained E reports, old runtimes, shared caches, SDKs or toolchains.
  Any incomplete/failed/uncertain stage remains HOLD with no automatic retry.
- `OUTER-RECEIPT.json` is expressly PRETERMINAL. Final descriptor-close/actual
  process exit must be reconciled independently; a positive JSON alone cannot
  hide a later exit70 or prove cleanup/validation. Actual namespace/materialized
  source/XML/process/cleanup evidence is not yet present at this decision.

Expected selection is **166 regressions +1 separate fixture producer**, across
five phases and17 XML captures. Independently reconcile actual case IDs, exact
source, failures/skips and producer-to-consumer identity; task counts,
compilation, mocks, syntax and source review are not substitutes. PVA-007/031/
033/034/035/038 are the scoped contracts. Android32, physical biometric/iPhone,
rendered Desktop, Apple/Windows native and PVA-029 evidence are not supplied.
PVU-007 STOP, PVU-011 NO RETRY, PVA-029 no automatic retry, G7/G8 CLOSED,
Linux01/02 HOLD/consumed, namespace01 consumed FAIL/no retry and Windows/macOS
failure restrictions remain. Counts are unchanged:19/25 original confirmed,
22/38 all confirmed,2/12 original suspicions; eight PVD explanations stay separate.

This decision adds **zero executed product cases/closures**. Only the compact
permanent reviewer report and genuine approval were written; no runtime,
build/cache/temp objects, namespaces or owned background workers were created.
