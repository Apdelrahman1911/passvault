# Shared fixture baseline — independent review

Reviewer `/root/shared_fixture_review`, 2026-09-11. **Source/retained-data review
only; proposal adjudication follows separately. No source edits or execution.**

## Authority and exact inputs

Read TEAM_20_RESUME, saved pause, AUDIT_HANDOFF, handoff START_HERE/PERMISSIONS/
ASSEMBLY, PUBLISHED_PAYLOAD, applicable root AGENTS (identical to inherited
G12-AGENTS), and focused production-readiness, database, secure-backup,
navigation-verification/architecture/Back/iOS instruction snapshots. Their
example commands remain dormant. Root owns all execution and central ledgers.

`REVIEW_INPUTS.json` SHA256
`7b322828e5236e9d21da6d84f104f5678012c390c70020c8bec47098d2575247`
records all 37 original path/line/column/rule/message tuples, source bytes,
declared methods, exact evidence identities and bounded comparison results.
Both retained Checkstyle and SARIF identify the same 37 rule/location tuples.

The three current source buffers match the exact raw/checkout tuples in retained
C17 SOURCE.json: commit `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49`, tree
`d1bd6ca5b18d08ff3ff15896d9a78af9016be799`. These are retained manifest identities,
not a fresh Git or whole-workspace cleanliness check.

| Fixture basename | SHA256 | Diagnostics |
| --- | --- | ---: |
| CredentialEditorRenderingTest.kt | `6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a` | 13 |
| CredentialEditorRoomIntegrationTest.kt | `291c47edcca2de797d1a669947a2ded246ec5b32c120feec8843a2d41ec9ef57` | 15 |
| CredentialBackFreshnessCompositionTest.kt | `8df5c4f32ab2002bc602c6963f0ac1f86bfb1ea2518f5f3a9cda15e981bd75d0` | 9 |

Rule totals: TooGenericExceptionCaught21, ThrowingExceptionFromFinally6,
CyclomaticComplexMethod3, TooManyFunctions2, LongMethod2, ReturnCount2,
ThrowsCount1. These are static diagnostics, not 37 product defects or cases.
Detekt03 remains consumed failure with zero application cases. This review does
not independently adjudicate its whole result/cleanup packet or release a slot.

## Substantive oracles that must survive

### Rendering: three existing native-input cases

- Page Save consumes the **pending** row draft without clicking row Save; exact
  id/name/value/secret tuples survive a new ViewModel and fresh rendered form.
- Row Save commits all tuple properties. Subsequent native row Cancel must not
  persist later name/value/secret changes; reload and hidden-value semantics
  remain asserted, not replaced with callback counts or screenshots alone.
- At50 fields, disabled Add cannot open a dialog. Removing one keeps49 exact
  survivors. Native dialog text remains while explicit synthetic VM interference
  fills capacity; disabled confirmation is not a backend-rejection execution.
  Removing the injected field re-enables actual confirmation, yielding50 unique
  IDs and exact survivor/new tuples through Save/new-VM reload.
- FakeCredentialRepository deep-copies seed/get/save secret owners; clearing
  seed or old VM is not allowed to mutate the stored fake. This is not Room,
  full NavHost, IME, physical gesture, or universal stale-owner retention proof.

### Real Room: one still-pending Save/close/reopen case

- Opt-in absence skips **before** opening storage. The private admitted-home
  prerequisite precedes bootstrap/native setup; requires matching user.home,
  canonical non-symlink components, unchanged directory key/owner/private mode,
  empty fresh home, and no previous `.passvault`. Allocation authority still
  belongs to root; metadata checks alone do not prove it.
- The production Desktop bootstrap, DesktopCryptoEngine, VaultRepositoryImpl,
  CredentialRepositoryImpl and FolderRepositoryImpl remain real. The production
  save passes the encrypted entity, relations/history and format update through
  CredentialDao's `@Transaction updateCredentialWithTagsAndHistory`; do not
  replace this with a fake repository, direct row rewrite, or reseeding reopen.
-49 seed fields plus native Add become50 unique exact tuples; the last row's
  pending name/value/secret draft is consumed by page Save without row Save.
  Wait for non-saving/clean/error-free state, detach the old form across real
  frames, clear its store, join all jobs, checkpoint/close the first bootstrap,
  then open a **different Room instance** with `seed=false` over the same
  regular database file identity. Fresh loaded VM must have exact50 tuples,
  no drafts/dirty flag, disabled Add and rendered secret masking.
- Bootstrap/Room owners are registered before native work. Synthetic password
  and seed credential owners clear in finally. No fixture filesystem deletion;
  generated home/bundle remains root-owned until original-worker settlement.
- This flow exercises transactional persistence but is not an injected rollback,
  migration, corruption or crash-durability test. Those qualifications stay open.

### Back: seven existing discriminating composition cases

Real Composition/Recomposer/RegisterCredentialBack, production VM and navigator
with copied fake persistence remain intact. Tests intentionally make no frame or
scheduler drain between accepted input/save and Back/forward/completion. Keep
the stale composed-policy value AND unchanged applied-composition counter
assertions, route/full-stack outcomes, synchronous busy blocking, row-Cancel
compatibility, exactly-one-pop check and disposed-registration fail-closed case.
Do not substitute a settled-frame happy path or mistake UnitApplier for rendered
NavDisplay/native input or gesture-preview evidence.

## Native driver, snapshot and cleanup challenge

- Native input remains Robot key/mouse input, not accessibility actions, direct
  editable-text mutation or invented events. Event type counters wrap and
  delegate the production callback. Fresh context identity, geometry, enabled
  state, active owned-window checks and cardinality/unique-extremal selectors
  remain before input. Only controlled capacity interference is direct VM input.
- Snapshot traverses only fixture-owned showing windows with depth40/nodes2048/
  children2048/text256 bounds, identity-cycle protection and read-only metadata.
  Room's editable-only payload capture must stay exactly intact. The existing
  IllegalComponentStateException geometry handling does not swallow arbitrary
  assertion/provider errors. Names, roles, values, focus and bounds may still
  fail; no universal accessibility-safety assertion follows.
- Rendering PNGs remain fixed-name exclusive0600, memory-only encoding, forced
  while the original channel is open, parent-force and size-bound checks, and
  pixel-buffer finally flush. They supplement assertions, not OCR/pixel oracles.
- Key/mouse cleanup is registered BEFORE press; release runs even after partial
  native failure. A failed release leaves its tracked input for outer cleanup.
  Replacing Throwable with Exception can miss AssertionError, leaving the
  primary slot null and allowing a cleanup failure to replace the real failure.
- Native close independently attempts every owned input/window/store/job action,
  requests cancellation before joins, retains multiple failures, clears and
  restores entry/later interruption, and checks settlement. No global window,
  process, cache or storage enumeration/deletion is introduced.
- Room close attempts lock and terminal checkpoint/close once per owner;
  attempted flags precede work. Lock failure does not skip close; live-VM checks,
  distinct-error suppression, `closed` success flag and interrupt restoration
  remain. Failed/uncertain close is not retried. NonCancellable bootstrap close
  means coroutine timeouts do not constitute hard native/worker termination.
- Back cleanup stays NonCancellable: dispose composition, clear store, request
  all cancellations, separately bound/join runner, Recomposer shutdown and VM,
  reset fake secret copies, then restore Main even after cleanup/body failure.
  Primary failure and suppressed cleanup evidence must survive. Flattening this
  solely to reduce LongMethod/ThrowsCount risks reordering resource authority.
- `onEdt` must still cancel(false) after every failed FutureTask wait before
  rethrow. This prevents pending setup from running after failure, not already
  running EDT/native work; external root settlement remains mandatory.

## Pre-proposal recommendation and evidence reuse

Two ReturnCount sites have a tiny equivalent correction: initialize ancestor
from `unique(titleMatches)?.parent` instead of an early-null title return.
Missing/root-only title still returns null; duplicate title still fails unique;
first qualifying ancestor, exact-two-editable/named-field requirement and unique
Add behavior must remain unchanged. No new trivial test is warranted.

The other35 diagnostics can be considered individually for reasoned symbol-local
fixture exceptions, not blanket file/config/baseline suppression. Throwable and
primary-aware finally paths are intentional cleanup boundaries; owner lifecycle
metrics are accurate but do not justify splitting security/native ownership just
to meet a threshold. Exact annotations and rationale still require review.

Prior retained XML has Rendering3 and Back7 cases, zero failures/errors/skips.
These are existing qualified results, not new executions. Preserve GUI02/software
renderer/fake-storage and Back/non-rendered limitations. Room is unexecuted under
the pending exact GUI03 request. C17 GUI03 source/index/control freeze remains:
even annotation-only edits change bound bytes and need root's explicit freeze
reconciliation, not silent rebinding or reuse of consumed requests.

Minimum future static verification is `:shared:detekt` in root's new, independently
admitted focused batch, not a full all22 rerun. If a still-needed Room execution
must bind a successor source, use its one existing native Add/page-Save/reopen
case rather than new lint tests or repeated successful Rendering/Back suites.
If C17 GUI03 is consumed first, preserve that exact actual identity; annotation/
equivalent-selector source review is not a fresh modified-source runtime pass.

No new cases, findings, closures, denominator changes or READY claim. Only
bounded foreground source/retained-data reads and own review writes; no Git, CI,
build/test/helper execution/import, runtime/SDK/process probe, credential/real
data access, cleanup, or central-ledger edits. STOP/NO-RETRY/CLOSED/HOLD and
publication fences remain intact. Initial oversized output was reread in bounded
source ranges; missing inferred config path was resolved to root detekt.yml.
