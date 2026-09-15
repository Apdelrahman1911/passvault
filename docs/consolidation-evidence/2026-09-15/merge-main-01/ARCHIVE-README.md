# Protected-main merge preparation, 2026-09-15

This compact record preserves PR183 preparation and local verification, including
failures and the local03 UNKNOWN/HOLD. It is not a merged-main, completed-CI,
beta-readiness or publication claim. CI01 final results and accepted cleanup are retained. CI02 launch observations
are not a final result; consult the named run and subsequent final evidence.

CI01 source:f8f3c9c0bd61af79e41a7a8e5efe6c0a3167ff15,
tree312791149ba33ebd9d67aaf67819ab075e1f0b84. Preliminary mergebc9ff6f was
replaced by actual checkout53acb726339c57eca6abe66be914374fb5de063b with
the same tree/parents. CI01 failed on the stale backup partial-parser pin;
wrapper/dependency checks passed, downstream platform jobs were skipped.
CI02 corrected source:4fc32944df1672dfb502b1e80671c500b1a067bd,
treed2da382909d5e2d26a36271de597ab7976c7529c. The independently reviewed
pin refresh and validation-only Android versionCode1 do not close beta gates.

This archive's first parent preserves previous evidence
f733441da87c3a3a00c181f832a96644b08a56c9. Its second parent preserves exact
reviewed integration commits even after future squash/rebase and branch retirement.
Its application tree is inherited from the archival first parent: it is NOT the
integration build source. Only allowlisted compact evidence paths were added.
Never merge this archival tree/history into main as a product fix.

Six synthetic cleanup tests passed; eight iOS entitlement cases passed within a
failed full shell invocation. The corrected379-line source-validation suffix
passed separately; its generic success footer applies only to that suffix. Earlier
107 release-regression cases and184 JUnit cases remain qualified in previous evidence.
Do not add these execution counts as unique total tests or claim the corrected
whole shell suite passed locally. The normal CI runs the complete suite.

Original compact afterimage review precedes one reviewer-requested documentation
clarification distinguishing original72+6 selection from12-file CI follow-up;
SOURCE-IDENTITY.json records that qualification. No executable afterimage changed
in that initial documentation clarification; CI02 subsequently changed the
validation sentinel/predicate and parser pin as separately reviewed.
Local03 empty private directories remain on the originating VPS under HOLD and
are intentionally not uploaded. Other run-specific outputs/private roots were
removed only after recorded settlement. No local Gradle invocation in this bundle.

Main/test/release/tags/build1017001 and all branch names remain unchanged by these
records, except dedicated continuation/evidence pushes. Main merge requires normal
CI and a qualified independent GitHub approval, never administrator bypass. See
BRANCH-DISPOSITION.json before deleting any branch; it admits zero deletions now.
