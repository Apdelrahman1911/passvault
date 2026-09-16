# Pending post-run verification — no execution yet

Target: Testing CandidateNEW RUN ID FROM RUN-ACTIVE.json, C=25880c3b1168dcd1f97876866341338f097d41fa,
T=deab190809ff00030407703dbd3f70481de251b5,1.0.10/1017004.

After actual run/producer settlement, read job statuses and compact cleanup
artifacts; preserve any failure/HOLD. Do not rerun the release. Verify the new
prerelease tag points to C and is not a production/stable release.

Download only candidate-manifest.json, android-artifact-receipt.json,
ios-artifact-receipt.json and desktop-artifact-receipt.json from that exact
public prerelease, unique filenames, bounded sizes. Do not download large
applications, private signing inputs, tester files or full signing/Fastlane logs.
Record asset IDs, sizes, SHA256 and source metadata.

Use prepared official CLI2.100.0 (binary SHA256
553949e2efa12842771efe6012aa4de21f1d591530ec17fc435f610f10e017ee) to execute,
for each actual small JSON artifact:

 gh attestation verify <artifact> --repo Apdelrahman1911/passvault \
   --signer-workflow Apdelrahman1911/passvault/.github/workflows/testing-release.yml \
   --source-ref refs/heads/testing --source-digest 25880c3b1168dcd1f97876866341338f097d41fa \
   --deny-self-hosted-runners --format json

This is the existing strict fresh-C policy, not a readiness workflow dispatch or
an archived replacement helper. Do not relax flags or replace certificate checks
with self-asserted predicate fields. Explicitly validate1.0.10/1017004 and reuse
current selected scripts/validate-candidate-artifact-provenance.rb on the four
JSONs with C/T and WITHOUT a Desktop binary-directory argument. Its existing
child validators bind receipt hashes, identifiers and fingerprints. This does
not independently rehash downloaded large app binaries; the workflow's final
signature/artifact checks remain separately described evidence.

Actual execution still needs fresh bounded read-only admission. Use isolated
HOME/config/tmp/cache, update checks disabled, no credential serialization/logs;
reuse authorized GitHub authentication only through private process environment
or existing authentication context. At most120s per network verifier, <=10min
batch, modest metadata/output bounds. Install owned cleanup before invocation;
reap/settle only owned command scopes before removing newly created temporary
caches. Preserve compact results and public receipts, not transient caches.
Remove only the validated temporary CLI namespace once no further verification
needs it. Any uncertainty means HOLD, not cleanupPASS or an automatic retry.

Report pipeline upload/processing/promotion assertions distinctly from direct
Store readback and actual tester availability. External environment approval,
Apple beta review and physical-device/security gaps remain separate. The open
audit is not closed by a successful testing upload.

Officialgh2.100.0 binary remains at sibling testing-publication-1.0.8-20260915/attestation-tool-2.100.0/bin/gh; verify recordedbinaryhash beforeuse. No archived controllerexecution. Actual read-onlypostrunverification stillrequiresfreshadmission.
