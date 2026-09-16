## Approved next testing version
Owner approved **1.0.10 / Store build1017004**. Align version.properties, shared display version and Xcode build/marketing settings; metadata build counter12. Keep existing identities, minimum supported version and RC/testing destinations. No runtime dependency or workflow changes; no production.

PR191’s CI35009834688 passed all12jobs, including actual Intel unsigned optimized iOS link; 15cleanupPASS. Independently reconciled compact evidence retained at dd86ede2526d9b83227f081fdb2546563281968d. Actual Store signing/archive/processing still requires fresh release evidence.

MainCI35023335669 subsequently failed **only Intel macOS packaging**, before compilation, resolving unchanged Foojay1.0.0 from configured repositories. Exact repository/transport cause unknown; failed cleanupPASS/stop0. Other platform/unit/iOS checks passed. Do not call that main run passed or weaken dependency policy. This necessary version PR must pass existing CI, including packaging. No unchanged-workflow retry requested.

1017001/1017002/1017003 must not be reused; interrupted1017003 GooglePlay outcome remains unknown. Every STOP/NO-RETRY/CLOSED/HOLD and deferred audit limitation remains. Normal protected main review and later testing promotion, final-source history/allocation checks and fresh release admission required. This PR starts **non-publishing CI only**, not a Store upload.

Focused local verification: checked-in release metadata validator PASS, shared/Xcode/store version alignment PASS, whitespace check PASS; owned temporary HOME/TMP and children settled/cleaned. Zero test cases/no Gradle or signing execution; existing PR CI remains required.
