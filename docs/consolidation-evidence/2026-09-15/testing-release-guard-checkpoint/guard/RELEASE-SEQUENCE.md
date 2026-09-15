# Pending testing release sequence (not execution admission)

Owner-approved destination: 1.0.8/build1017002; Play internal then existing
closed alpha; TestFlight internal then external email-list; unsigned Desktop
prerelease. No production, occupied1017001, tag replacement or automatic retry.
Owner confirmed no outside-GitHub uploads. Allocation is not a live Store query.
Deferred findings stay open. Existing STOP/NO-RETRY/CLOSED/HOLD unchanged.

1. PR186 source1e66eb545a8658273d499ffb079b4cd1eb760824,
   tree9c221eaa84de5dff6d2a3bc1c737253f4fb74b05: wait for CI34966696185
   completion, reconcile executed merge/tree, compact test results and cleanup.
   Obtain independent GitHub approval and merge through normal protections.
   Independent agent review is NOT the GitHub approval.
2. Refresh exact main source/remote protection/active jobs after merge. Reuse
   source-affine PR evidence, not an assumed SHA. Allow automatic main CI to
   settle before scheduling another owned workflow.
3. Refresh PR185 by a normal new linear commit on its current branch to the
   exact final main tree. Verify common history, unchanged Store identities,
   metadata, no unexpected release changes and the original provenance gate.
   Never force-push. Run normal required PR checks and obtain actual approval.
4. Record final release admission: exact post-merge testing source/tree;
   workflows/commands and toolchains from that exact tree; worker1/JDK17,
   <=50min guarded batches/90min jobs, bounded root observation and no competing
   owned workflow. Only owner-approved hosted parallel jobs, no local heavy job.
   Private Gradle/Konan/DerivedData/synthetic smoke HOME; original-wrapper stop,
   positive scope settlement, stage required packages and retain before deleting.
   Xcode interruption/uncertain cleanup means HOLD, never a passed receipt.
5. Coordinate testing push CI versus Testing Candidate before enabling any
   automatic flag or dispatch. Do not launch duplicate release runs, rely on
   queue timing, or cancel unrelated work. Release switch currently remains OFF.
   Fresh workflow execution may establish native packaged smoke and signed
   Xcode evidence itself; no redundant signing rehearsal is prescribed here.
6. Verify signed APK/AAB and archive/export identity plus provenance before
   upload. Record exact source/build/run/artifact hashes and actual Store
   receipts/status after upload and promotion. External environment review,
   Apple beta review and tester availability are separate from successful upload.
7. If any platform upload occupies1017002, stop automatic retry. Preserve
   receipts and assess permitted exact-build promotion/resume or request a
   new allocation; never rebuild/reupload blindly or weaken attestation gates.

Current blocker: PR186 still requires GitHub approval and successful CI.
This plan does not establish final release admission or close PVA029, physical
mobile security/compatibility/data-loss gaps, or any deferred audit family.

## Bounded PVA029 applicability review
Independent reviewer admits consideration only of a FRESH candidate, with
resume_existing_internal_uploads=false. For final testing C, checkout, mobile and
Desktop receipts, manifest, attestation invocation and release target all use C.
This is the historical same-SHA countercontrol, not the failed subjectC /
certificateE!=C promotion path. Exclude resume/readiness/production/stable.
Require actual strict attestation reconciliation with --source-digest C; no
weakening, family closure, historical helper replay or automatic upload retry.
Final source/CI/protected review and fresh execution admission still required.
