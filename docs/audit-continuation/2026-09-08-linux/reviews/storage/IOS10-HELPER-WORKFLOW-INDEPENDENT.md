# Independent focused iOS10 helper/workflow review

Reviewer `/root/storage`; original helper/workflow author `/root/android32`;
bounded finalization author `/root`; 2026-09-10. **SOURCE-ONLY ACCEPTANCE OF THE
BOUND CANDIDATE; NO EXECUTION/INSTANCE ADMISSION OR TEST PASS.** C11 remains frozen
and excludes this unfinished iOS work. Root alone owns builds, Git, CI and the
cross-host slot. The prior guard/boundary review is not overwritten.

## Bound inputs and actual review

| Input | SHA256 |
| --- | --- |
| `scripts/audit/ios_focused_validation.py` | `61f7e3627758c95eed5d03aa2dd8e165e3b6fab6f3e7be7a6d3da244d1e88fc9` |
| `.github/workflows/audit-ios-focused-validation.yml` | `d1a1b3b5c06b46446e935ce3ff2e75ec2a829d9d6179df0b90f75ddf7c2a9dfa` |
| `shared/src/iosTest/kotlin/com/passvault/shared/platform/IosAttachmentFileStoreTest.kt` | `406c47fd2ea71afe50a57acfbc5050bcda05a3050e94a16c5555a5e053bdc337` |
| `reviews/storage/IOS10-RUNNER-BOUNDARY-REVIEW.md` | `0bf7d1110f0090359e5e08b8134bd363c99ee680f1a0631928838f43a50ecb96` |
| `reviews/android32/ios-focused-before-root-finalization.py.txt` | `e3543aee6ab989086140b42b80eef75f753aa72b0ef381209d6b18b4b5823f9c` |

Review paths are under `docs/audit-continuation/2026-09-08-linux/`. The complete
1,152-line incomplete helper and complete workflow were read as inert source.
All changes to the final1,192-line helper were independently read through a full
text diff and focused final-context reads; hashes matched afterward. The nested
Groovy init is bound by the helper bytes and was reviewed as source, not imported
or evaluated. This was not a syntax/compile/test run. The original author's
platform-rate-limit interruption was not a product execution failure. The preserved
before-image is evidence, never an execution/import fallback.

## Concrete challenges and accepted source corrections

| Challenge | Final source disposition |
| --- | --- |
| Raw transport verified open descriptors without full named/original-FD agreement. | `transport_pin` is restricted to the two owned transport leaves, compares complete named/FD pins, regular type, owner, mode0600, link count and filesystem. Request identity is checked around the child; output non-mutating fields survive the authorized writer, then complete pins bind both raw passes. Nested finalization attempts input close even if output close fails. |
| Copied-source checks ignored executable mode. | `read_leaf(with_stat=True)` returns metadata from the same complete before/FD/after/named stable capture as the bytes. `source_check` verifies both blob hash and exact0644/0755 mode, including wrapper checks before run/stop and the final full source check. |
| Cleanup always required the not-yet-created init leaf. | An explicit init-attempt flag admits only the fixed generated leaf's presence or absence after its attempt; before that only the exact child-directory set is admitted. Failed Java-version/init preparation no longer necessarily strands otherwise settled private output. Unknown top-level files remain rejected. |
| Successful root mkdir followed by failed open could falsely report no allocation. | Attempt/created/original-handle-bound markers precede/follow those operations. An attempted but unbound root now records HOLD, not inferred absence or deletion authority. |
| Resource floors omitted a potentially different simulator storage filesystem. | Sampling includes `fstatvfs` of the identity-checked original device-data descriptor; the launch floor is checked immediately after binding it, before fixture mkdir. No assumed same-volume mapping is necessary. |

The first three were already identified as unfinished in the author's last note;
this reviewer confirmed their concrete reachable source paths. The last two were
independently challenged here and corrected by root. These are reviewed source
corrections, **not executed cleanup/failure regressions or new product-family
closures**. No additional concrete unsafe source fallthrough was found in this
bounded review; that is not a general-purpose process/filesystem security proof.

## Actual fixed workload and isolation

- The workflow permits only the dedicated continuation branch/new request path,
  exact repository/ref/push and attempt1. It uses one macos-15 job, shared audit
  concurrency without auto-cancel, read-only contents permission, immutable
  checkout/upload pins, no retained checkout credentials and seven-day compact
  JSON/log/XML artifacts. No signing/store secrets, app scheme, publication,
  candidate replacement or app binary/archive upload is selected.
- The helper binds a clean single-parent activation adding only the exact request
  to the requested source commit/tree. It verifies helper/workflow checkout bytes
  and selected fixture hashes, then copies complete raw Git blobs with fresh
  `cat-file --batch`, framing/OID/payload hashes and mode checks. No archive filter,
  text conversion, borrowed `.git`, ambient source or old helper is used. Exclusive
  directory/file creation rejects colliding creations rather than merging them.
  Room's `$projectDir/schemas` and build output belong to the disposable replica,
  not the original checkout. Linux03 supports choosing raw transport; Linux02's
  earlier raw-blob mismatch cause/path remain UNKNOWN, not proven normalization.
- Installed fixed help/tool/runtime/type observations precede device creation.
  The helper requires a fresh create-returned UDID absent from observed inventories
  and a matching unique post-create name/runtime/Shutdown row with canonical
  UDID/data path. It creates a private empty parent there and forwards
  `SIMCTL_CHILD_PASSVAULT_IOS_TEST_PARENT` plus `SIMCTL_CHILD_TMPDIR` through the
  allowlisted parent environment. No private-device-set discovery assumption,
  default device, guessed dataPath or all-device operation is used.
- The retained mandatory fixture guard, not host TMPDIR alone, validates actual
  Foundation storage before either fixture helper's file access. Lost forwarding
  fails closed. Seven receipts must name the original parent; absent, inconsistent
  or residual fixture state cannot become success. No optional developer fallback
  or copy-only fixture patch was reintroduced.
- Exactly two serial selected tasks use the same new UDID: two existing real
  `SecurityTest` methods, then seven attachment cases plus the one prompt-property
  case. The init rejects additional test/app-project tasks. JDK17, checked-in
  wrapper, strict verification, one worker, non-daemon, disabled parallel/CoD/
  build/config caches and in-process Kotlin remain explicit. Private HOME/tmp/
  Gradle/Konan/project caches,2GiB Gradle heap and512MiB child default are bounded
  choices, not a bound on all native/LLVM/simulator memory.

## Settlement is qualified, not universal

Signal handling/finalization is installed before preparation. Work has a24-minute
deadline; cleanup uses a separate absolute27-minute deadline within the30-minute
job. Resource samples and bounded logs are not hard OS memory/process containment;
hard runner termination or failed cleanup still needs honest retained HOLD status.

Before each stop, the same copied wrapper is byte/mode checked. One `--stop` and
one `--status` use only its private Gradle home; a changed wrapper, stop/status
failure or absent empty-registry message leaves uncertainty and no next module.
**An empty version-specific Gradle registry is not a census of native compilers,
LLVM processes, guest children or shared CoreSimulator services.** Normal original
Popen return/EOF is qualified only to the explicitly trusted synchronous workload;
the in-process Kotlin flag does not upgrade that into arbitrary descendant proof.
The code marks forced/signalled/nonzero module outcomes uncertain and refuses
private generated-tree deletion on uncertainty/cancellation/incomplete stop.

Device closeout is separately limited to the bound new UDID: observed identity,
one necessary shutdown, observed Shutdown, one deletion, registry absence and
dataPath absence. An unbound creation outcome cannot license name-based recovery.
Shared CoreSimulator services are deliberately not signalled or claimed settled.
This API-level closeout does not settle unknown host Gradle descendants. Disposal
after a failure is not a passing fixture finalizer, and hosted runner disposal
does not prove cleanup. On normal qualified settlement, only the original owned
replica/private-output tree is allowlisted for deletion; evidence, original source,
permanent tests/schemas/reports, shared caches and toolchains are excluded.

## Remaining admission and evidence limits

The final plan/admission must bind this concrete source tuple and actual source
commit/tree; the older archive-proposal wording is not this implementation's
command contract. Actual image/tool availability, help semantics, guest forwarding,
native task behavior, stop/registry observations and filesystem cleanup remain
unexecuted. Root must independently admit the fresh instance/coordination/cleanup
boundary; no extra speculative preflight job, test-list build or automatic retry
is requested by this review.

The helper retains raw XML and deliberately does **not** score method-name
decoration. Its exit0/evidence-captured state is not ten test passes. Independent
exact-ten, once-each raw case/result reconciliation is required; unexpected names
remain unverified, not normalized or repaired by another run. Positive real-provider
VEK wrapping plus typed wrong-KEK rejection can exercise native AEAD compatibility,
but do not prove typed tag-tamper handling, provider-initialization/operational-fault
preservation, repository/session enrollment deletion behavior or complete PVA-038
closure. One LAContext property case is not four prompted/authenticated operations;
attachment protection/copy seams are not physical iPhone failure evidence.

**Ten source declarations, zero executions/passes/closures in this review.** Genuine
iPhone/security-device gaps remain BLOCKED; all denominators/PVD decisions and C11
totals are unchanged. PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic
retry, G7/G8 CLOSED, consumed/HOLD and non-publishing/build1017001 fences survive.
Only source reads/diffs/hashes, coordination and this permanent report occurred;
no Git/network, helper import/execution, build/test/CI, runtime/cache probe,
simulator or temporary/background workload was started by this reviewer.
