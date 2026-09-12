# C20 bounded supplement — older existing methods are not globally unexecuted

Reviewer: `/root/migration_integrity_review`  
Scope: selected older retained migration/bootstrap/recovery results, requested
by the author to avoid unnecessary existing-method reruns. **No new execution**.

This supplements, rather than replaces, `REUSE-AND-GAP-REVIEW.md`. It does not
increase C19/C20 runtime counters, alter a historical gate, or refresh a whole
class to current-source Linux runtime status.

## Initial-audit full-database run: selected raw XML

The following original locators are relative to
`audit-reports/20260905T083114Z/evidence/runs/database-full-desktop-resumed/`
in the handoff evidence index. Each selected blob was decoded as inert data in
memory and SHA256 checked. Nothing from that run was executed/replayed.

`run.json`, 234301 bytes, SHA256
`2ebfc34169557d81f1a0b22acb6561fb45accdf1658018d97efd03ff88f173bd`, records
exit0, original stop0, cleanup/evidence complete and frozen identity preserved.
Its input and after identities name baseline commit
`0dbc12c7f1b7770e75963c751c8c67af6e8b057a` / tree
`20fb8f6f2c6fb11ad99af80c170b4150c16beb6f`. These are historical receipt fields,
not fresh Git/cleanup observations. Only selected XML/source-identity fields
were followed; this review does not recertify the entire original invocation.

XML prefix: `test-results/core/database/build/test-results/desktopTest/`.

| XML | SHA256 | Observed methods | Qualification |
|---|---|---:|---|
| `TEST-com.passvault.core.database.VaultMigrationTest.xml` | `f1675912767e00d89c71e25933a9be8db1698092ee6cba3f70a95105b846ef62` | 6 | All six original methods, no failures/errors/skips. |
| `TEST-com.passvault.core.database.VaultDatabaseRecoveryStorageTest.xml` | `c155802fcdb9bf5ae0dca9c3b042c1a284839d2dfd5a3f7e9065603d0fec7613` | 5 | All five original methods, no failures/errors/skips. |
| `TEST-com.passvault.core.database.VaultDatabaseBootstrapTest.xml` | `933d75b223f51548f22e832c7e51606ddbc19df6790beb90f61945cfb68a17fe` | 8 | Baseline eight, not current eleven; no failures/errors/skips. |

Every name below has the exact `[desktop]` decoration in its XML.

### `VaultMigrationTest` six

- `fresh version five schema contains only justified blind indexes`
- `version three migration removes title hash and preserves every dependent row`
- `version one data survives the folder and tag index migration`
- `version two legacy attachment metadata survives version three migration`
- `failed migration rolls back schema changes and preserves version one data`
- `failed version four migration rolls back rebuilt credential graph`

The retained G12 delivery explicitly reconstructs source from baseline plus its
122 named changed files. `VaultMigrationTest.kt`, `VaultMigrations.kt` and all five
schema files are not in that changed-file set. Current W bytes also match the
later historical source manifest, as recorded in the main reuse note. This
supports **bounded historical unchanged-source reuse through retained delivery
provenance**, not a newly performed baseline Git comparison or runtime test.
Do not rerun the four methods missing from migration6 merely because that later
selection chose only two. The new C20 post-validation whole-chain method is still
distinct from all six original methods.

### `VaultDatabaseRecoveryStorageTest` five

- `failed preservation rolls every moved source back into place`
- `oversized diagnostic input is discarded before a bounded rewrite`
- `diagnostics are bounded fixed codes with no retained arbitrary text`
- `startup restores an interrupted preservation before inspecting SQLite`
- `missing main file with an orphaned sidecar cannot become a fresh database`

The test file is also absent from the G12 changed-file set, with the exact
current/historical hash in the main note. Shared `VaultDatabaseBootstrap.kt`
**did change** for PVA004, so whole-production-file baseline equality is not
asserted. The current issue ledger's PVA004 `file_locations` bounds all production
delta hunks to the bootstrap ownership section (through current line218), above
the `LocalVaultDatabaseStorage` implementation and diagnostic constants. Current
source inspection confirms the recovery tests use that separate local storage
implementation. Thus the two diagnostics controls have bounded historical reuse
with this retained source-impact qualification; three move/recovery controls
also have the later exact-current-source recovery6 observations. No fake
interruption is upgraded into process-death/fsync proof.

### Baseline `VaultDatabaseBootstrapTest` eight

- `damage introduced while Room opens is caught by the second gate`
- `a complete WAL bundle is accepted and its committed content is visible`
- `damaged leaf is detected before Room and preserved byte for byte`
- `missing sidecars after a clean close do not imply corruption`
- `terminal close checkpoints committed WAL frames before releasing Room`
- `bad header and truncated page are classified without recreating either file`
- `migration failure stays unavailable and rolls back without corruption recovery`
- `fresh and existing databases pass both health gates`

The bootstrap implementation and this test file changed for PVA004. Their
baseline file hashes, recorded by the current ledger, are respectively
`21330e4fa715d2d7aa76b2539ccad7ed720110bd1eb037aefb599957c7a21f8a` and
`c52fef3185ffb510b5bec7bac2e7d9a225674815229e71e4e4141a7d124fd9f2`.
No exact raw-source alias for either hash was located in the included evidence
index during this bounded read. The old eight observations are preserved, not
discarded, but **not promoted to an exact-current-file eleven-case pass**.
Current-family adjudication and the later specifically executed guards remain
the authority for PVA004, not an invented replay requirement.

## Later exact-current-source bootstrap guards

The additional retained locator
`remediation-reports/20260905T222925Z/evidence/runs/storage-bootstrap-handoff-02/`
has:

- `source-manifest.json`, SHA256
  `69ec7a7008e34931854e97d75f47fbc117d82f717ddc475c25b863c8db767165`.
  Its two bootstrap implementation/test entries exactly match current W:
  `5726afb24093afa8c56c57321d64f0ea747db69910f0b7aa984e9d18a86f2598` and
  `5cfb78294ea63f66b9fb07f7b694f69afd6d7e0a30907cbc8bb47774b472f2b4`.
- `artifacts/worktree/core/database/build/test-results/desktopTest/TEST-com.passvault.core.database.VaultDatabaseBootstrapTest.xml`,
  SHA256 `4a7a39ef67cc6512bd4f7ade25ed1e97a450bd5f3331d925ea20bb75facf4056`,
  **four**, not five, methods, all with zero failures/errors/skips:
  - `damage introduced while Room opens is caught by the second gate[desktop]`
  - `claiming Room after a preflight failure revokes preservation eligibility[desktop]`
  - `Room cannot be constructed while recovery owns filesystem replacement[desktop]`
  - `a failed Room factory cannot regain file replacement authority on retry[desktop]`

The current PVA004 ledger records this run as bootstrap4 plus a separate
attachment-handoff3, seven total. This supplement follows only bootstrap4, not
the sibling attachment XML. Together with recovery6's three distinct bootstrap
methods, there are **seven exact-current-file historical bootstrap method
observations**, not a newly run seven-case batch. The four remaining current
names have the baseline observations above, with their source-generation limit.

## Accounting decision

- Never call the old unselected methods globally "never executed".
- Do not add overlapping historical case observations to current counters or
  confuse a class/loop/row count with new methods.
- Reuse original migrations, source-compatible diagnostics and exact PVA004
  guards in their supported scopes. Additional validation must address a real
  unmet property or changed-source impact, not manufacture a repeat backlog.
- This supplement establishes no current full-class runtime pass, platform
  parity, complete historical-backup origin compatibility, power-loss guarantee,
  broad correctness or family closure.
