# F02 dependency/legal input applicability

Author `/root/session_investigation_author`, 2026-09-11; independent review requested
from `/root/session_investigation_review`. **NEW BOUNDED INVENTORY REUSE; NOT A
CURRENT DEPENDENCY-GATE PASS.** No dependency, policy, source or test correction.

## What this reconciliation adds

The actual merged resolved inventory preserved by historical
`static-dependencies-online-01` is **byte-identical** to current
`legal/third-party-dependencies.lock`: 17,552 bytes, SHA-256
`252661ae65b520539b6d7a1e301cb1f6de505a17576301e970668b1778d150e2`.
Its three separately retained scope outputs union exactly to the merged rows:
**151 Android + 80 Desktop + 66 iOSArm64 = 297 scoped rows / 277 unique coordinates**.
This is an actual historical resolved-output/current expected-input match, not
merely matching declarations or an equal-size inference.

The independently adopted historical gate still records PASS/exit0; its original
offline predecessor remains BLOCKED/exit1. The bound command log records seven
executed tasks, 297/277 attribution entries, 15 native components, 25 reproduced
files, release labels1.0.7/1017001 and checksum metadata822452 bytes. These are
**historical task/log facts**, not new execution or current input identities.

Exact chain (R=`remediation-reports/20260905T222925Z/`):

1. `R/verification/gates.json`, selected completed online row.
2. `R/evidence/runs/static-dependencies-online-01/input.json` and
   `R/verification/results-static-dependencies-online-01.json`.
3. Their exact final-state descriptor points to `journal/00000221.json`; only
   its input/source/evidence fields were used, not old operations or recovery.
4. Its `evidence-manifest-00000017.json` SHA
   `3764522ef5c638d545e1d8fdb784c1e79d14cbed071f6a3984e65c71dd345fb7`
   binds the four inventory members and original command log.

`F02-EVIDENCE-TUPLES.json` supplies all exact index paths, pack/member identities,
sizes and SHA-256 values. Each relied-on inventory was decoded and verified in
memory as data; none was extracted as an executable/current generated output.

## Source affinity is still narrower than F02 completion

The run reports source-manifest identity
`3cd8973f34dc87edc11057879d5fca7182d3b16a6c8f14496cad3d488fe7669a`.
Its input freeze digest resolves exactly to the retained
`evidence/corrections/android-detekt-style4/freeze-before.json`; that binds
**freeze generation3** and the same source identity. Base0dbc12/tree20fb8 are
retained baseline fields, not a claim that the modified G3 source equals that Git
tree. The raw manifest-file descriptor and canonical tuple binding were not located
through these bounded references. Exact known-digest index/reference-locator
queries supplied no raw3cd member/candidate. This is not proof the manifest does
not exist, nor permission for a broad archive search or guessed filename.

Current root/settings/catalog/XML equality to the later build-config review remains
useful but is not G3 equality. Current pins now explicitly cover a bounded **60-file
comparison set**: root/settings/properties/catalog/XML; 19 included module build
scripts; verifier, expected lock, map, notices, project license and 25 reproduced
files; three version-parity inputs; wrapper script/properties/JAR. The set is not
all environment/configuration inputs or execution admission; JAR handling is hash
only. No secret/signing/environment values, SDKs or caches were accessed.

The existing generator resolves only ModuleComponentIdentifier coordinate strings
for Android releaseRuntimeClasspath, Desktop desktopRuntimeClasspath and shared
iosArm64CompileKlibraries. It normalizes Desktop Skiko OS/architecture runtime names
to `skiko-awt-runtime-current-os` and deduplicates. Thus neither current expected-lock
equality nor a future normalized inventory equality establishes the selected Linux
artifact bytes, another OS/architecture artifact, native shipped components or
legal documents actually included in a package.

## Precise remaining small gate; no blanket rerun

First complete **G3 input-affinity reconciliation**, using an authoritative exact
manifest descriptor/raw hash plus its tuple canonicalization binding, then compare
only the enumerated dependency/policy/legal/version/tooling inputs and relevant
configuration differences. An unrelated application-source change is not reason
to rerun all resolution; an unchanged catalog alone is not enough to skip it.

If that source affinity cannot be supplied, keep the verified historical-output
match and separate the missing proofs:

- Current attribution-map/notices/license agreement can be checked by the existing
  attribution verifier against the **already-bound historical merged inventory**;
  it does not require inventing a new inventory or resolving every platform merely
  to check legal mappings. This remains a future separately admitted check, not an
  execution here and not evidence of the current resolved graph.
- Any needed current checksum-policy result is the metadata predicate check, not
  implicit permission to call aggregate `verifyDependencies`.
- A current resolved-graph claim needs only the still-unmatched/affected production
  scope inventory resolution after independent source-affinity review. Preserve
  proven scopes; **this report promotes no current Android, Desktop or iOSArm64
  resolved scope**. Do not request platform compilation/device tests or blindly run
  `verifyDependencies`, whose dependencies add all three inventories, attribution
  and version parity. No new command or task graph is admitted by this report.
- Package inclusion, artifact provenance, vulnerability applicability and publisher/
  Store decisions remain their own gates. PVU012 is not supply-chain clearance.

Only source/data reads, standard-library bounded decoding/comparison, and own-leaf
records. Zero builds/tests/helper execution, dependency resolutions, new application
cases, fixes, closures or cleanup. Reader/schema errors and truncation limits are
retained in the tuples record; no conclusion relies on them. All STOP/NO-RETRY/
CLOSED/native-refusal/HOLD/protected-ref/build1017001/design/version fences persist.
