# Independent database proposal review — source accepted, UNAPPLIED

Reviewer: `/root/database_review`, 2026-09-11.

**Independent disposition: accept the exact three proposals below at source
level, including the precisely scoped intentional LargeClass exception.** No
semantic blocker was found in the reviewed delta and relevant guards. This is
not a claim of no possible defects, a successful Detekt/compile/test result,
root integration authority or release of any freeze. Root must adjudicate and
authorize exact paths/source generation separately. Nothing was applied.

Finalized after root's explicit source-only resume. GUI03 execution disposition,
cleanup/hold and admission are separate root-owned work; their status does not
release the C17 source freeze through this report.

## Exact accepted artifacts

Paths below are in `../database_author/`. Apply order, if separately authorized,
is 01, 02, 03; **03's declared repository before-image includes 02**.

| Proposal | SHA-256 | Independent source disposition |
|---|---|---|
| `01-pagination-declaration-and-fixture-format.patch.txt` | `45ea3727ff8a44f07834ac864d78d2caa8a8f4292086d1231d56d4da3542bef1` | Accept same-facade comparator relocation and seven formatting corrections |
| `02-biometric-admission-and-fixture-returns.patch.txt` | `fbccacba0abbaab83346c05dd3f50e90e90b2e4d34d0e6d4aacf171107ad6ee1` | Accept private initial-admission predicate and equivalent fake candidate selection |
| `03-sole-vek-owner-size-exception.patch.txt` | `1bf34df5ab49d21d65cfd515de4353dc8a7443da7c8c06ab48c30868e37bc312` | Accept exactly this class's intentional LargeClass exception; not a smaller-class or false-metric claim |

Final author rationale: `RATIONALE.md`, SHA-256
`48d4d9166c6735b8d1d4ec4370a88038481521f1e5590da739fa412ce98c5e97`.
Final author source identities: `SOURCE_IDENTITIES.json`, SHA-256
`812eccb9b3f5d4a47d450538477067040bd3aaee187fd6f3bf8ac66cacb9462f`.
The final metadata additions qualify initial directory discovery and narrow
future validation; every patch and per-step before/after identity is unchanged
from the independently reconstructed comparison packet.

`BASELINE-SOURCE-DIAGNOSTICS.json` binds four affected original files and ten
related source/config files to retained C17 raw tuples. A final bounded re-read
confirmed all fourteen remain byte-identical; proposed
`BackupDatabaseTextOrder.kt` remains absent. Retained C17 commit/tree are
`0563e31adc9a66aefc3e74b99b9a24d17bdcdd49` /
`d1bd6ca5b18d08ff3ff15896d9a78af9016be799`; no fresh Git/ref check was performed.

## Independent challenge and compatibility

Detailed pre-proposal guard inspection, counterexamples, exact diagnostics and
review limits are retained in `BASELINE-GUARD-REVIEW.md`.
`PROPOSAL-TEXT-COMPARISON.json` records exact-context, in-memory reconstruction
of the three diffs and independent agreement with every per-step and combined
after-image hash. No patch command or Kotlin/project code was executed.

### 01 — declaration naming and fixture layout

- Enum and all three private comparison helpers are byte-identical except for
  file-boundary blank-line separation. Package, enum identity, values and
  `fromPragma` contract do not change. The identifier guard and both pager
  implementations remain byte-identical in `BackupPagination.kt`, preserving
  the `BackupPaginationKt` callable facade. Compiler-generated private helper
  accessors move with their implementation; no supported public contract,
  encryption/backup format or schema changes.
- UTF8 scalar, UTF16LE unsigned low-byte-first and UTF16BE ordering remain
  distinct. U+FB00/U+10000 and U+0100/U+00FF are counterexamples to replacing
  this logic with a platform String comparator. Both tuple keys remain validated;
  duplicate/backward progress is rejected before consumption, and cursor
  advancement still follows successful consumption.
- The seven Unicode fixture edits preserve ordered lexical tokens except
  whitespace and optional trailing commas. All six test declarations, fixed
  expected vectors, reverse insertion, before-export validity checks,
  ciphertext/nonces, relationships/history/content assertions, `getOrThrow`
  and cleanup scopes remain. No oracle is regenerated from production ordering.
- Production snapshot/PRAGMA selection, manifest, eight DAO routes, managed
  object stability, sink abort and metadata wiping are untouched. No migration
  or transaction restructuring is smuggled into the file split.
- The new file must enter a **new** root-owned source inventory. A preserved
  callable facade does not permit retrofitting consumed manifests or using a
  C17-bound request against a changed source generation.

### 02 — biometric admission and synthetic key selection

- The only new production helper is private, in the same VEK owner, and takes
  no key. Its sole call remains immediately after `ensureActive` inside the
  unchanged transition -> operation -> session mutex envelope. Identity is
  checked before touching the lock tracker; only `UnlockPreemptedException`
  becomes false. Cancellation and other failures are not newly intercepted.
- Foreign/stale attempts still return VaultSessionLockedException **before**
  candidate-size validation or session mutation. The stale malformed-candidate
  case therefore cannot delete valid enrollment, and stale completion cannot
  wipe/relock a newer password-opened session. No fresh generation is captured
  after OS retrieval. Moving the read of immutable `attempt.lockGeneration`
  after the helper introduces no replacement admission.
- Every statement from the key-size guard through verification, existing/new
  session commits, failure mapping and candidate finally is byte-identical.
  Initial admission is not treated as durable authorization: both commit paths
  retain the original generation check. The final-write preemption distinction
  (service INTERNAL_ERROR rather than early VAULT_LOCKED) is preserved.
- Provider failure, ordinary argument failure and cancellation remain distinct
  from typed key rejection. Service-owned released key, repository candidate
  copy, committed session copy and verification plaintext retain their separate
  existing owners/finally paths. Password, lease and lock-failure machinery is
  not decomposed.
- The fake's equality branch has the same short-circuit behavior as the old
  mismatch return. Wrong vault never evaluates/copies/consumes a candidate or
  gate. Matching vault without a candidate still leaves the gate untouched.
  Injected candidate identity and one-copy stored-key fallback are unchanged.
  Its complete post-selection gate/transfer/finally body is byte-identical:
  pre-transfer cancellation wipes locally; cancel-on-return transfers the same
  array for the service's cancellation check and wipe. All twelve declarations
  and existing assertions remain.

The expected complexity reduction is 15 to 14 and expected fixture ReturnCount
is two. These are source expectations, **not a newly executed Detekt result**.

### 03 — exactly one intentional structural exception

LargeClass is a real threshold diagnostic, not a demonstrated defect or a false
measurement. This repository deliberately co-locates live VEK state, serialized
password/biometric transitions, lock-intent publication checks and revocable
lease cancellation/wiping. Moving key ownership, state or finally boundaries
merely to satisfy size would enlarge the security change without a demonstrated
semantic benefit. The existing private admission extraction is non-owning and
does not require that risky split.

The proposal adds only `LargeClass` to this class's existing `TooManyFunctions`
annotation and gives the shared serialized-owner reason beside it. It changes
no runtime statement, configuration, baseline or module/file-wide exclusion.
I independently accept this precise exception. It does not endorse arbitrary
future growth, suppress other diagnostics, certify the class, or grant authority
to restructure secret ownership. If root declines it, retain LargeClass as
unresolved rather than broadening suppression or silently replacing the patch.

## Smallest meaningful future validation

This is a recommendation for **fresh root-owned admission after explicit freeze
reconciliation**, not an executable request or permission to replay anything.

1. Include only `:core:database:detekt` for this lane in the affected-module static
   successor; retain exact reports and actual errors. Do not repeat all22.
2. For changed admission/fake control flow, select exactly the existing
   `com.passvault.core.database.repository.BiometricUnlockFreshnessIntegrationTest`
   class in a new admitted `:core:database:desktopTest` selection. Require all
   twelve named outcomes below, with no missing/extra/duplicate/skipped cases.
   Normal compilation also covers the relocated commonMain enum/private helpers
   and the formatted desktop fixture. A compile failure is not a test pass.
3. Reuse previously accepted C4 evidence for the thirteen pager cases, six Room
   Unicode cases, nine verifier-routing cases and three dedicated cold-provider
   consumers with the exact unchanged-logic/layout qualifications above. Do not
   rerun those successful scopes, all105/166, or cold-provider setup merely for
   reassurance. A later substantive delta requires a separate scope decision.

Exact twelve freshness test names:

1. `completed lock rejects a key returned by an earlier biometric attempt`
2. `admission precedes the biometric metadata lookup`
3. `admission precedes the platform enrollment lookup`
4. `a genuinely fresh attempt after completed lock still opens the vault`
5. `a stale completion cannot succeed against or relock a newer valid session`
6. `stale admission takes precedence over malformed candidate invalidation`
7. `a fresh invalid candidate still invalidates its enrollment`
8. `pending lock refuses admission without metadata or platform access`
9. `cancellation after key transfer propagates and wipes the received array`
10. `lock during the last access write still prevents biometric publication`
11. `an already open session still succeeds without an intervening lock`
12. `an admission from another repository cannot authorize this repository`

Root may batch this exact selection with other justified work under independently
reviewed admission/cleanup. Source review does not prove physical biometrics,
OS callback timing, exhaustive Unicode, universal cross-pass snapshots,
schema1–4 migration, Android/iOS provider parity or native/package provenance.

## Boundaries and actual status

Eleven database diagnostics remain the **observed Detekt03 result**: proposals
01/02 target ten by code/layout correction and 03 proposes one accepted local
exception. None is recorded as newly cleared by execution. Qualified prior
PVA-033/035/038 results and all other current denominators are unchanged.

This lane performed source/evidence reads and bounded standard-library text/data
comparisons only, writing its own reports. The initial overbroad AGENTS locator
enumerated sibling directory/filename metadata including T/held paths; no sibling
content was opened, executed, changed or cleaned. That deviation and the initial
JSON-shape reader error were reported and retained in the baseline packet; all
subsequent task work was W-bound. No repeated sibling discovery occurred after
resume. No application source/helper change, build, test, Git, CI, SDK/process
probe, runtime/helper execution or Telegram duplication occurred.

PVU007 STOP, PVU011 NO RETRY, PVA029's retained failure, G7/G8 CLOSED, consumed
executions, the native refusal and every held-runtime obligation remain intact.
Only root adjudicates integration, admission, cleanup, central records and
publication. **Formal independent source review is complete; application and
fresh validation remain unperformed.**
