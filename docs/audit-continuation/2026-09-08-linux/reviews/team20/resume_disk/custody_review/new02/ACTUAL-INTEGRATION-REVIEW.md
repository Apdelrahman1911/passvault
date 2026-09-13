# Post-Sfix seven-path integration — independent actual review

Reviewer `/root/custody_review`; 2026-09-13.
**ACCEPT_EXACT_SELECTED_W_INTEGRATION_NOT_PUBLICATION_OR_RUNTIME_VALIDATION**.

Root receipt: `root/POST-SFIX-SEVEN-PATH-INTEGRATION01.json`, 6918B,
`2eeed77dd3524d99e6106384eb5117eeb49782942ee36cfc23ca694abddd0f2c`.
Paths abbreviated below are relative to
`docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk` unless stated.
Exact independent observations are in `custody_review/new02/ACTUAL-READSET.json`,
14155B, `91dc6fbb93fed3242b84b92eb50978f2d40c27745bbc534639ce69369ed5864b`.

## Actual selected delta

Verified **2D + 3A + 2M**, net **+1 member / +9084 raw bytes** for this finite
selection, not the future complete source tree's member count.

| Current W object | Bytes | SHA256 |
|---|---:|---|
| `core/database/.../CredentialFolderForeignKeyMigrationTest.kt` |22968|`7f444c01177999b2fd2271e2d7ed1f005c54670fc9e2364795d37e2b2bedd2cc`|
| `shared/.../CredentialEditorRoomIntegrationTest.kt` |45386|`00c4396acca707549075816da9fca43048c6c0c78f6e4f076e7f47ca39787e40`|
| Exact historical backup **before** `.kt.txt` |27452|`acc3c5a8d29f2f976ed5dfd5612b325624915953937ff48b438ad6e5bcb63554`|
| Exact historical backup **after** `.kt.txt` |39219|`a136ec3fe0ca70b469f8b6ecf2555da65f1a480d2d39a43d21255175f96b944d`|
| Exact historical backup `INERT-EVIDENCE-PATH-MAP-STATIC06.json` |3719|`e0bc06acb16554c9009fb535d21090e01bae796aee313259258d3625918e5317`|

All five current files were stable regular single-link files across lstat/open/
fstat/read/recheck, with actual **0600** host permissions. Both old full archive
`.kt` names were absent; no stub/symlink remains at those names. The two archived
contents and calculated Git blobs equal the original Sfix SOURCE rows exactly.
They were not confused with the separate permanent module test of the same basename.
The locator is byte-identical to the accepted inert proposal, including its deliberately
historical status; this actual receipt/review, not rewritten old locator text,
establishes the current selected W path state.

For both test replacements I compared current bytes with accepted afterimages,
then independently inverted the retained unified diffs **in memory**. The DB1
one-hunk inverse produces 22952B/`5d80abe70b5667a4c874e65c6b61e56f80625260bbeae8002b3ba0eb3c5de40c`;
the Room six-hunk inverse produces 40037B/`bec3d1d8d6be6aad88585103788975f7292010282422a2bbe844efb2d5309b7c`.
Both reconstructed SHA256, sizes and calculated Git blobs match original Sfix
SOURCE rows. This proves the selected current deltas, not execution of either test.

## Reused logic review; meaningful guards still present

* DB1: reused `dependency_review/resumed05/migration_fixture_cleanup01/SOURCE-REVIEW.json`
  (`e45d9cfc2a6687abb7cc193976a43e0a7e528d2419eaf66d81b1120cf0b24965`).
  The sole change is exact `vault.db.lck` in this synthetic fixture's existing
  direct-child allowlist. Current prefix, real temporary-parent, NOFOLLOW regular-file,
  unknown-child refusal and original-error/suppressed-cleanup-error guards remain.
  No recursion, catch-and-ignore, production migration or dependency change is added.
* Room: reused `gui_review/resumed01/pva007_ime01/REVIEW.md`
  (`36c288ca56960b84749dd62fee2b7c049076830cb8b550ad3a7fbf370854b84c`).
  Current selected method actually calls blank-draft rejection before repair/save.
  Native focused Enter must produce exactly one Save callback; there is no click
  fallback. Exact validation resource/draft/dirty-state assertions and the real
  repository read before repair remain, followed by the original close/reopen oracle.
  This does not establish PVA031 stale-modal/full/busy reachability or mobile IME behavior.
* Archives: reused `investigation_review/resumed01/static06_evidence_suffix01/REVIEW.md`
  (`201849fc8d7e9c72007492ac800d4aeed0bbd006df2ef50dac90b33813726498`).
  The exact old active suffixes disappear; no docs-wide exclusion/baseline weakening
  substitutes for the correction. A successful inventory guard/analyzer run is still needed.

I also hash-compared **19 named unchanged inputs** against original Sfix SOURCE:
the three archive neighbor documents, permanent backup test, coverage guard/baseline,
root/settings Gradle scripts, Main integration fixture, version catalog/database
build script, and eight accepted Room reachability/resource/test inputs. All match.
This supports reuse of the existing focused logic reviews without another whole-code audit.

## Custody, permissions and failure accounting

Root's prior `SFIX-PROSPECTIVE-W-SOURCE-CUSTODY-RELEASE02.json` is exactly5191B,
`32877b39837cc479ff04a49e2a7ef0fbd1cfb0412f096387d14a5738c9a05e34`.
It adopts the bounded prospective-only GUI4/Dependency3 disposition, separately
references entered consumers' existing dispositions, and expressly preserves T,
original captures/config/index, entered or uncertain debts and historical HOLDs.
This implements the scope accepted in my new01 review; no universal launch-absence
or retrospective settlement/cleanup claim is introduced.

Original Sfix SOURCE remains2266654B/`887d3b93b82ec9cc3b2b3ef33ed67f3ab0f96d2e4aab33e73d0a3ed5e4287d74`;
INDEX remains677943B/`af34295e4d563f8226e3fce23c673d0d4d281d821219bc0ee270090a258d7add`.
Both were independently rehashed. Neither describes the changed current W product
as an unchanged4113-member checkout. Both historical checkout-EOL qualifications remain.

The initial source preflight failure is retained in
`root/POST-SFIX-INTEGRATION-PREFLIGHT01-FAILURE.json`,821B,
`505555c0f127e16ce1a789b47a8e4ceb264acb8b4f2f789e1258a052ecd0b58d`.
Root records failure before any selected source replacement/move, caused by assuming
0644 instead of W's0600. I did not observe that earlier execution directly; current
exact afterimages and0600 permissions are independently observed. Preserving0600
does not widen permissions. Git100644 denotes non-executable tracked mode, not a
requirement to chmod host files0644; actual new Git publication is not yet claimed.

## Limits and next required work

This is exact **selected W** integration review. It is not an independent complete
filesystem delta, remote publication, Git-index capture or all-source preservation
proof. Root's command-scoped preservation assertion remains attributed to root;
the fresh removal-capable publisher must still prove its finite complete-map delta.
No T/U or held runtime was accessed. No private fixture residue contents were read,
copied or selected for publication.

Next: finite reviewed publication; one new complete SOURCE/INDEX; corrected DB1 plus
eight unstarted credential methods and Desktop test compilation; four unstarted GUI
methods/five independently viewed crops; seven analyzers plus unchanged inventory
guard; three dependency producers. Reuse unchanged passed DB2 and13 completed static
analyses at actual relevant affinity. Future Windows graph/real-JNA require the same
new complete SOURCE. No old source/nonce/failed helper is replayed by this review.

No build, compiler, test, native/GUI invocation, project/helper execution, Git/network,
runtime/process probe, cleanup or stop was performed. No temporary workspace, worker
or cache was created. All observations are retained source/data comparisons; zero
new cases/closures/readiness credit. All STOP/NO-RETRY/CLOSED/native/HOLD/PVD and
protected-ref/tag/version/dependency/identity/Store/signing/build1017001 restrictions
remain. Original DB1FAIL/DB2PASS and other consumed failures are not rewritten.
