# Continuation guide

## 1. What is actually delivered

This is an **incomplete audit/remediation checkpoint**. The latest application
correction is PVA-037 (Windows borrowed-secret-array lifetime guard). Its target
tests remain unexecuted. The handoff itself adds no product fix or runtime pass.

The original audit and remediation base is
`0dbc12c7f1b7770e75963c751c8c67af6e8b057a`, committed Git tree
`20fb8f6f2c6fb11ad99af80c170b4150c16beb6f`. The branch contains all 811 G12 source
members, with 122 intended changed-file after-images; it is not a delta-only
export. The canonical SHA-256 of the complete source manifest is
`7fd2dab3eac6269baede8a0ca550f0cbdfee20391a12dfbd9ac84cdac5401d91`.
See `current/source-manifest-v12.json` and `current/delivery-generation12.json`.
This describes the original raw snapshot. Ordinary Git checkout normalizes two
PowerShell files under the existing attributes: see `EVIDENCE.md` and
`current/source-transport.json`. Their exact raw originals are included too;
do not call the normalized checkout an 811/811 raw-byte match.

`git rev-parse HEAD` identifies the containing handoff commit. Its Git tree also
includes this handoff documentation/evidence, so it must not be confused with
the committed baseline tree or the G12 application-only manifest.

The original primary checkout's local work was not applied to this branch.
Local-only findings remain local-only. Frozen reports describe the state at
their capture times, including historically “uncommitted” source; the owner's
later commit/push authorization is recorded separately in `PERMISSIONS.md`.

## 2. Progress — defined, separate denominators

| Measure | Established | Remaining |
| --- | ---: | ---: |
| Original PVA-001–025 qualified closures | 19/25 (76%) | 6 |
| All independently confirmed families PVA-001–037 | 22/37 (59.5%) | 15 |
| Conclusive original PVU-001–012 outcomes | 2/12 (16.7%) | 10 |
| Original PVD-001–008 explanations | 8/8 documented and independently checked at G11 | Owner decisions remain separate |

These are not percentages of overall readiness, elapsed effort, source
correctness, platform validation or completion of the entire audit.

The current coverage accounting attributes 129167 physical LF across 714 owned
text/documentation members; 97 excluded/generated/binary/third-party categories
remain explicit. This is mixed historical/exact-source attribution, **not a
fresh “100% reviewed” claim**. Preserve every condition/qualification in
`current/coverage-CURRENT.json` and its referenced reviews: 19 false script flags,
six missing original range hashes, 14 timestamp qualifications, 80 metadata-only
reads, cutoff123 and the prior failed/corrected administrative diagnostics.
The 18 current conditions, 12 materializer limits and 176 predecessor qualifier
elements remain binding through 35 exact review references. The two declared
checkout-EOL identities do not automatically inherit exact raw G12 coverage.

## 3. Read in this order

1. `PERMISSIONS.md` — current authority and non-negotiable safety rules.
2. `current/issue-to-fix.json` — all 37 families, grouped variants, current
   statuses, exact corrections/evidence/reviewers and remaining requirements.
   Top-level status is current; embedded author records can be historical.
3. `current/unresolved-investigations.json` and
   `current/additional-investigations.json` — dispositions and unresolved work.
4. `current/g12-current-gates.json` and
   `current/historical-verification-summary-adoption-g12.json` — current blockers
   separately from actual historical executions.
5. `current/OWNER_DESIGN_DECISIONS.md` — the original eight PVD choices only.
6. `current/NEXT_REGRESSION_MATRIX.md` and `current/RUNNER_READINESS.md` — precise
   next host-regression work and why the old runner remains unadmitted.
7. `EVIDENCE.md`, then use the read-only evidence index/viewer to follow exact
   source reviews, original witnesses, commands/logs/XML, coverage ancestry and
   rejection records as needed. Do not replace raw evidence with summaries.

## 4. Do not repeat or silently skip these outcomes

- **Implemented; essential target/runtime evidence missing:** PVA-001, 007, 008,
  009, 010, 014, 027, 030, 036, 037.
- **PVA-029:** actual regression failure (49 named checks, 44 pass, five fail).
  No automatic retry. Unrelated passing suites do not erase this failure.
- **PVA-031:** setup/plugin resolution failed before assertions (zero cases,
  zero XML); the exact cause remains unverified. Do not call it a passing test.
- **PVA-033/034/035:** corrections/source reviews exist; regression execution is
  pending. Seven selected classes in five source files declare 105 methods,
  **zero executions** for this pending selection. Do not use the obsolete
  115/130 counts. PVA-034 is a discriminating test-oracle correction.
- **PVU-010:** confirmed as PVA-027, which is implemented but target-blocked.
- **PVU-012:** scoped false positive for advisory applicability, not blanket
  dependency security clearance.
- **PVU-007: STOP** — do not investigate, retry or reformulate it.
- **PVU-011: NO RETRY** — do not relax containment or revisit its procedure.

Historical verification retains 70 run/status events, not cases. The 684 passing
JUnit execution events include 623 in PASS gates and 61 in BLOCKED gates; 402
literal IDs are not a count of unique logical tests. The latest G5
feature/shared/OTP suite had 242 cases. Android host tests used fakes; Desktop's
40-case run still had an overall BLOCKED gate. **No fresh G11/G12 runtime pass
is implied.** Follow the exact historical summary and raw results.

The latest report reconciliation corrected only stale PVA-030 prose: G6 already
had six passing static source-contract predicates and one language
original-fail/corrected-pass comparison. These are not fresh G12 or Android
runtime evidence. Counts and statuses did not change.

## 5. Concrete next work, in order

1. Verify the delivered source/issue/evidence identity and refresh public
   `main`/candidate refs. Revalidate affected findings if source has changed;
   never blindly apply patches already present in this branch.
2. Assign exactly one build owner. Keep other agents read-only until bounded
   author/reviewer assignments explicitly change. Preserve independent
   verification for every new defect and every fix.
3. Complete a **new, current-generation** validation runner and its source,
   configuration, environment, retention, cleanup and result-mapping review.
   The frozen G10v2 runner was independently read in full and remains incomplete:
   cancellation/terminal outcome, bootstrap/under-lock binding, descriptor-bound
   deletion/removal lifecycle, after-only process births and real admission
   acceptance still require correction. F04 stable reads and F07 exact nonempty
   XML minimum improved only narrowly. The 19 inert cases are specifications,
   not executed tests. Do not execute/import any old runner to “see if it works.”
4. Establish fresh local resource/ownership admission and reviewed cleanup
   **before** any new invocation. The previous host had about 5 GiB free at
   handoff preparation, below the 12 GiB launch floor; no audit build outputs
   were available to reclaim. Disk alone is not the only blocker.
5. Once independently admitted, execute the pending synthetic regression
   selection; retain exact command, exit, all seven XMLs, all 105 expected method
   outcomes and cleanup proof. Independently reconcile the results. Then finish
   the still-applicable feature/shared/security/static/platform matrix.
6. Continue other unresolved concerns within the allowed scopes, testing caller
   guards and counterexamples rather than patching suspicions. Retain genuine
   hardware/provider/GUI verification gaps; do not use mocks as device proof.
7. Prioritize Android32 KDF validation, storage/data-loss/recovery/editor/session
   correctness, then platform plaintext/native/privacy behavior, then remaining
   provenance/packaging/localization checks. Keep the eight PVD choices separate.

New machine/path: original lock inode/device values, caches, journals and
absolute paths are **historical evidence only**. They cannot authorize operations
on a clone. Do not recreate/adopt the old lock or revive old run namespaces.
Design and independently review fresh coordination/isolation/cleanup admission
for the new machine; retain the old CLOSED gates and STOP/NO-RETRY scopes.

## 6. Candidate and publication are separate

At the last read-only ref check, main was the baseline above, testing was
`2ae65df7111a9c5493740e8b772932be77eb98bc`, and release plus
`v1.0.7-rc.1017001` pointed to
`61f55216302023d9872aba17546126450e5fbad3`. Main source has database schema5;
the candidate source has schema4. Matching version labels do not mean matching
code or bytes. No store state was queried or inferred in this handoff.

Do not move the tag, alter versions/identities/dependencies, rebuild or reupload
1017001, approve deployments, change protections or publish. Desktop production
publication stays deferred and separate from mobile. Uploading this Git handoff
does not publish an app or prove production readiness.
