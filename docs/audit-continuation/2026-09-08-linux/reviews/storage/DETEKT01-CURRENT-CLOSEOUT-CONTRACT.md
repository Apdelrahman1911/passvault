# Current Detekt01 failure — minimal closeout contract

Independent source/evidence preparation by `/root/storage`, 2026-09-10. **PROPOSAL ONLY: no runtime read, probe, stop, deletion, admission or safety-flag waiver.** Applies solely to the newly consumed `linux-detekt01` attempt below, never any older HOLD.

W=`/root/projects/PassVault/passvault-linux`; B=`W/docs/audit-continuation/2026-09-08-linux`; E=`B/runs/linux-detekt01`; R=`/root/projects/PassVault/audit-runtime-linux-detekt01`.

## Exact retained bindings

| Relative evidence/source | SHA256 |
|---|---|
| E/OUTER-ALLOCATION.json | `52216e32746c8614ee15d078274c505d31f3d689bb0974b0b958107fc15892f8` |
| E/OUTER-RECEIPT.json | `6fa674ea620303612d0f71290a1210368dcc1bae98ac4be0c62e1b88edf3caa6` |
| E/INNER-RESULT.json | `aa8813acebfc2d9308926d179845a37303f85ef7652cf0c97aee8849883cfc46` |
| E/PHASE-detekt.json | `1263a51c670738f796d0129768ab9dd28cd2a4165670b09bc2f4b1da60c4da5a` |
| E/SOURCE-BEFORE.json and SOURCE-AFTER.json, identical | `a71df23efdc330470cf1c71ce34e5344893473da0efa6506d5d4f699352237bc` |
| E/logs/detekt.log,71728B | `13c30804b78f425f8ec21b204ed117ba3143990f0b99a232b073bde1342e4218` |
| E/logs/detekt-stop.log,406B | `e99efde14fa98d945008d4e456023a6830343bd898e2dbd476436f3ca61691b4` |
| W/scripts/audit/linux_detekt_01.py,47751B | `fceab30448da506edd3010d432bc489e238defcaa6f01f455ba205b095d2e2f7` |
| B/reviews/detekt01-outer/LAUNCH.py,53689B | `a0be29d62dfaab5db7b8b4638cc28b70829bd84750a9f403181ac9f42c0cc091` |

Source commit `8f42274b04e206ff7254ca33d686a9666fce6723`, tree `3a8f53dddd54f5c42c34f772975f02619be118d5`,2757 raw members/87770345B. Preserve raw/checkout-EOL qualifications. Manifest bound by the attempt: `1a501d764306c1729cef49a411207ca840740d5fe6e284555767182478b55b23`.

## Independent failure reconciliation

Detekt itself failed with **7 reported issues**, exit1; this is not merely a report-reader failure. Log74 records `:app-android:detekt`,87 records the same task `FAILED`; source653–656 rejects these duplicate headers **before** its report-copy loop. There are22 planned task records/66 declared report paths, but only three header lines (coverage, app-android start, app-android FAILED), not23 completed tasks and not application cases.

Original stop completed0 and says no Gradle daemons are running. Inner records settled namespace, source-before/after, stable bindings/index and stops true, but `static_reports_preserved=false`, `cleanup_safe=false`, mappings false. Preserve those values. Outer child exit70 caused HOLD/NOT_ATTEMPTED; its receipt is preterminal and contains no parent-mount cleanup guard. It did not complete ordinary post-child cleanup admission. No current runtime presence, file hashes or global idleness were observed by this reviewer.

## Smallest proposed new one-time contract

1. **New closeout request and independent acceptance, not rerunning either helper.** Bind the retained hashes, actual external terminal/completion record, root's sole-slot reservation and uninterrupted current-R writer freeze. Old original directory descriptors are closed by outer905–911; explicitly admit new no-follow descriptor acquisition checked against the original allocation, not fictitious descriptor continuity. Recorded original R is `(dev23,ino1304148,uid0,dir0700)`; E `(dev23,ino1304147,uid0,dir0700)`. Revalidate parent/name/origin and hold those new descriptors throughout this attempt. Directory nlink may have changed as admitted children were created; do not confuse this with file single-link requirements or ignore origin drift.

2. **Retain reports before any deletion.** The three producer-logged paths under `R/checkout` are exactly:
   - `app-android/build/reports/detekt/detekt.xml`
   - `app-android/build/reports/detekt/detekt.html`
   - `app-android/build/reports/detekt/detekt.sarif`
   Read each once under original-root/parent descriptors and preserve original bytes, metadata and SHA256 into a fresh exclusive `E/closeout01/reports` destination. Missing/changed required files, unsafe type/link/ownership, oversize, write/fsync/readback/close uncertainty: HOLD, no cleanup. Producer log messages are not substitutes for current original-file validation.

3. **Optional reports are a fixed set, not a filesystem search.** Derive the other63 candidates only from the22 bound `PASSVAULT_DETEKT01_TASK` records, checked against inner56–61 fixed module/task mapping and exact `build/reports/detekt/detekt.{xml,html,sarif}` names. Record absence without claiming tasks passed; retain every present bounded candidate. Reuse4MiB/file and32MiB aggregate Detekt limits. Do not demand successful analysis/XML findings to preserve failure bytes, and do not normalize the duplicate headers into PASS. Separately, log89 announces `R/checkout/build/reports/problems/problems-report.html`: explicitly add that exact bounded diagnostic to admission, or exclude its subtree from deletion. It is outside the planned66; silently deleting it is not acceptable.

4. **Fresh origin/namespace/mount gate is mandatory.** Bind the actual terminal record plus original inner/preflight and namespace/stop provenance. The retained true flags are supporting evidence, not fresh authority. Before any runtime traversal and again before first deletion, root's newly admitted closeout must exclude mounts at/below R (including same-device binds), validate the original underlying `git-metadata` mountpoint versus its alias and keep T untouched. Do not signal historical PIDs, re-enter a namespace, mount/unmount, run Git/Gradle or repeat the successful original stop. Cooperative freeze/no-escape qualifications stay explicit; dev/ino tuples alone are not hostile-root/inode-reuse proof.

5. **Reuse only the reviewed snapshot/recheck/unlink concept.** Current outer628–674 has the familiar complete bounded snapshot BEFORE deletion, no-follow owner/type/device/single-link checks, original per-entry recheck and bottom-up deletion. Use a small new current-instance delta of that concept after independent review; do not import/call the consumed function, copy its whole-R allowlist, or bring along its process/store/watch machinery. New cancellation/deadline handling must be installed before action; failure or partial deletion consumes this attempt and leaves an explicit HOLD, never automatic retry.

6. **Generated-only deletion boundary.** Under the literal no-source requirement, allow only freshly validated task-private cache/temp roots from the original allocation and manifest-disjoint generated roots: the fixed22 modules' `build` directories, checkout `.gradle`/`.kotlin`, and explicitly listed original R-private Gradle/Konan/Android-user/tmp/JNA/SQLite/HOME/XDG roots. Check the raw source manifest has no member under a deletion prefix. Preserve all required report bytes first; preinventory all selected entries, cap time/count/bytes and reject links, device/type/owner drift or unexpected paths before deleting anything. Keep R/checkout source, R itself, transport blobs/OIDs, index/metadata mountpoint, all W/permanent tests/reports, T and shared caches/toolchains outside this smallest contract. Removing the entire disposable source copy would need an explicit broader boundary, not accidental reuse of `remove_runtime()`.

## Remaining blockers / disposition

Source-concept reuse is reasonable; **execution/cleanup is not admitted by this note**. Still needed: independently reviewed small delta; exact new request/acceptance/output bounds; root's actual external completion and original-instance/freeze provenance; newly admitted descriptor/mount/origin checks; report-preservation receipt; explicit decision on the logged Problems diagnostic and exact generated-root allowlist. No current-R probing was done to prefill those gates. Record final closeout separately with captured-report hashes, actual removed/retained roots, failure/partial-work state and external termination. Never rewrite the failed run to success or alter original false safety flags.

Only authorized bounded retained-file/source data readers and this permanent note were used; no subject helper import, target subprocess, runtime access or cleanup. Readable duplicate-header/report facts were independently derived, not just accepted from root. Preserve PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, all older HOLDs/TRAY01/retired scopes and G7/G8 CLOSED. This current closeout gives none of them authority. Zero application cases or closure credit.
