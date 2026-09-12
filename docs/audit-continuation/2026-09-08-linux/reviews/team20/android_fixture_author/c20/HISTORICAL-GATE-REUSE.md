# Historical gate reuse — bounded retained-data summary

Owner: `/root/android_fixture_author`. **Historical applicability only; no current-source/runtime/admission credit.**
`H` = `W/docs/audit-handoff`; historical keys below are relative to
`remediation-reports/20260905T222925Z/`.

## Exact retained authority

Read `H/EVIDENCE.md` and the index schema first. Used only the exact indexed key
`verification/gates.json`, decoded its single content-addressed member in memory,
and checked the full226694-byte subject SHA256:
`e262c78f24da0f7a164e71fd29fdb91b87b5833e5197782dcb606d0fc3eb1543`.

`H/current/historical-verification-summary-adoption-g12.json` (3030 bytes,
`3d75ff60fb3129b28120e827361f64b65101f9bb25ecc404a53351a9fb72f957`)
adopts this same gate subject as **ADOPTED_QUALIFIED_HISTORICAL_COUNTS_NOT_FRESH_G12_TEST_RESULT**.
It expressly does not infer current-source or target-device passes. The named
G12 summary/review artifacts were not recursively reopened in this lane.

Source identities actually supplied by these gate rows:
- **S1:** `3cd8973f34dc87edc11057879d5fca7182d3b16a6c8f14496cad3d488fe7669a`
  (earlier static-source manifest; these selected rows do not supply its full commit/tree or generation-number tuple).
- **S5:** `c79fe054bf8eafc692ea1c7a1119556f945924babbdbc1a60956e79731836ade`
  (explicitly Generation5).

## Selected gates and legal extent of reuse

All six rows below record CLOSED, source_preserved=true, original stop0 and
cleanup_complete=true **for their own historical instance**. Each has zero
application testcases; none grants present cleanup, replay or execution authority.

| Exact gate id | Retained status / command exit | Source | Supported scope and limit |
|---|---|---|---|
| `static-dependencies-01` | BLOCKED /1 | S1 | Offline `:verifyDependencies` could not resolve an uncached production artifact. Root adjudication preserves exit1 but establishes no source defect or inventory/checksum/attribution decision. |
| `static-dependencies-online-01` | PASS /0 | S1 | `:verifyDependencies`: declared/resolved inventory, legal attribution, version consistency and checked-in checksum metadata validation for that source/run. Not current resolved graph, vulnerability applicability, platform compilation, publication or package legal inclusion. |
| `static-release-version-01` | PASS /0 | S1 | `:verifyReleaseVersion` checks source version alignment only; labels1.0.7/1017001. No artifact build/replacement, signing, promotion or uploaded-byte identity. Occupied1017001 remains fenced. |
| `static-localization-01` | PASS /0 | S1 | `:verifyLocalization`: English/Arabic resource keys, placeholders/plurals and prohibited source patterns. No rendered UI, translation quality, switching, accessibility or RTL-device proof. |
| `static-detekt-online-01` | FAIL /1 | S1 | `:detekt` online diagnostic run; partial reports are not a passing lint gate. Later success does not erase this event. |
| `static-detekt-generation5-03` | PASS /0 | S5 | Complete serial `:detekt --continue`,19 configured module reports plus root. Static/build analysis only, not cases/runtime/security completeness. Its row still says independent actual-result review pending; this lane does not replace that qualification with a newly inspected review. |

## What remains missing, not inherited

- **Legal:** historical dependency attribution/checksum PASS is reusable only
  with its exact S1 applicability. Legal documents actually included in a built
  package are not established by these selected gates.
- **Packaging/provenance:** no package-byte, signing, notarization, upload/store
  or released-artifact identity PASS is established here. The version-alignment
  PASS explicitly excludes those claims. No extra package/provenance member was
  supplied or sought.
- **Current source:** selected rows give manifest hashes, not embedded
  root/settings/catalog/dependency-XML/legal expected/resolved/map/notices file
  tuples. Their referenced input/manifests were not dereferenced. Current W
  equality to a different initial build-config review is not equality to S1/S5
  and cannot establish the current resolved graph or current-source static PASS.
  No C18/C19/C20, Linux, or runtime result is inherited. C19 publication acceptance
  is separate and was not repeated.
- The missing requested tool remains **NOT_SUPPLIED_NOT_RUN**; nothing was
  installed, substituted or launched to turn that status into a pass.

## Transport identity and scope

- `H/EVIDENCE.md`:6702 bytes; `189b65e6e06dc240dd5e6b559920256b731a78184d79800280fe6194c7d14104`.
- `H/evidence-index.json`:6048018 bytes; `47a3429d173a5373c7948d579677e1f12a151c54bc7f06b65f7e21ca61e56b70`; schema format1 / files[].
- Exact pack `H/evidence-packs/evidence-011.tar.xz`:2811328 bytes; observed SHA256 `308cee6de9ac256e7510a0fe240f952dfa2472113f450541d0a0fa8a251c540b`.
- Exact member `blobs/e262c78f24da0f7a164e71fd29fdb91b87b5833e5197782dcb606d0fc3eb1543`; its bytes/hash agree with both index and adoption. No independently supplied pack-hash manifest was additionally adopted.

Only standard-library JSON/hash/XZ/TAR data decoding and this own report were
used; evidence.py transport-layout text was inspected, never imported or run.
Compressed-input cap32MiB, XZ decoder memlimit32MiB, expanded-TAR cap72MiB
(not a total-process memory guarantee); actual TAR35747840 bytes. No helper
import/execution, disk extraction, blanket archive or index search, omitted/
stopped-path inquiry, product-source/runtime/held-R/SDK access,
network/build/Git operation, cleanup or new tests. All existing HOLD/STOP/NO RETRY/
CLOSED/native-refusal fences remain. Retained old paths/stats are inert data.
