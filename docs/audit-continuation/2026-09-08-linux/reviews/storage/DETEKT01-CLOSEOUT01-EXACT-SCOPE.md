# Current Detekt01 closeout01 — exact minimal source contract

2026-09-10, `/root/storage`. **SOURCE PROPOSAL ONLY; new closer and request still require independent acceptance. No execution, runtime-read, cleanup or safety-flag waiver.** This narrows the preceding `DETEKT01-CURRENT-CLOSEOUT-CONTRACT.md` (8506B, SHA256 `29e321dd7cbf7dac73f8102373f02adfe681c4dea9661d1678b286f2d1af77a2`); its retained-evidence bindings and failure reconciliation remain applicable. Only this newly failed current instance is in scope.

W=`/root/projects/PassVault/passvault-linux`; B=`W/docs/audit-continuation/2026-09-08-linux`; E=`B/runs/linux-detekt01`; R=`/root/projects/PassVault/audit-runtime-linux-detekt01`; T=`/root/projects/PassVault/passvault-publication-20260910-01`.

## Origin and settlement admission, not a second run

Bind the actual external terminal/completion record to current OUTER-RECEIPT (`6fa674ea620303612d0f71290a1210368dcc1bae98ac4be0c62e1b88edf3caa6`) and INNER-RESULT (`aa8813acebfc2d9308926d179845a37303f85ef7652cf0c97aee8849883cfc46`). The retained outer receipt is preterminal/HOLD and cleanup NOT_ATTEMPTED; inner `static_reports_preserved=false` and `cleanup_safe=false` stay false. Root separately binds sole-slot ownership and uninterrupted original-R writer freeze. The original successful wrapper stop and inner namespace-empty records support settlement; they do not alone admit this closer.

Fresh no-follow root AND parent reacquisition is expressly proposed, using OUTER-ALLOCATION (`52216e32746c8614ee15d078274c505d31f3d689bb0974b0b958107fc15892f8`): R `(dev23,ino1304148,uid0,dir0700)` / parent `/root/projects/PassVault` `(dev23,ino498323,uid0,dir0755)`; E `(dev23,ino1304147,uid0,dir0700)` / parent `B/runs` `(dev23,ino642474,uid0,dir0700)`. Validate original-name bindings and no-follow ancestry, retain the NEW descriptors through closeout, and reconcile with the original allocation/provenance. Old outer descriptors closed at its terminal path; there is no continuity claim. Directory nlink changes from admitted descendants are not origin drift. Directory device23 and regular-file device24 are distinct recorded types, not a same-device-equality rule.

Before R traversal and immediately before deletion, check the admitted closer's parent mount table for no mount at/below R, including same-device binds. Bind current namespace/origin to original parent provenance; no historical PID, namespace identity or lock is execution authority. No namespace entry, mounts/unmounts, process signals, Git, Gradle, second stop, candidate/helper execution or imports. Keep cooperative quiescent-host/no-escape qualifications; inode tuples alone do not defeat hostile-root inode reuse.

## Retain exactly four logged failure reports first

Under `R/checkout`, these four exact files are required; 4MiB each, 16MiB aggregate:

1. `app-android/build/reports/detekt/detekt.xml`
2. `app-android/build/reports/detekt/detekt.html`
3. `app-android/build/reports/detekt/detekt.sarif`
4. `build/reports/problems/problems-report.html`

Use original-root/parent descriptors; require bounded regular uid0 single-link files with unchanged original/named metadata. Copy original bytes to fresh exclusive `E/closeout01/reports` without running/rendering them. Retain source path, metadata, byte count and SHA256; fsync and descriptor readback of each destination and fsync destination parents before deletion. Missing/changed/unsafe/oversize files or copy/fsync/readback/close uncertainty => HOLD, no deletion. Preserve bounded failing report bytes without imposing successful-analysis assertions. This replaces the draft's optional63-report scan; there is no report discovery pass.

The bound log (`13c30804b78f425f8ec21b204ed117ba3143990f0b99a232b073bde1342e4218`,71728B) independently has22 unique planned analyzer records, but only coverage header71 and app-android headers74/87. `:app-android:detekt` failed exit1 with7 issues; record the other21 analyzers **UNSTARTED in this serial attempt from the complete retained log**, not PASS or completed tasks. Coverage is not an analyzer/application test. Duplicate app-android start/FAILED headers triggered inner report-mapping rejection before copying. The Problems report is explicitly logged at89 and is now included, not silently deleted. Zero application cases.

## Exact generated-only deletion roots

The allowlist is these35 roots, all relative to R; absence is recorded, not a failure or permission to search elsewhere:

```text
home
tmp
jna
sqlite
gradle-home
konan
android-user
xdg-cache
xdg-config
xdg-data
xdg-state
checkout/.gradle
checkout/.kotlin
checkout/build
checkout/app-android/build
checkout/app-desktop/build
checkout/shared/build
checkout/core/build
checkout/feature/build
checkout/core/domain/build
checkout/core/database/build
checkout/core/crypto/build
checkout/core/security/build
checkout/core/designsystem/build
checkout/core/navigation/build
checkout/core/otp/build
checkout/core/testing/build
checkout/feature/onboarding/build
checkout/feature/unlock/build
checkout/feature/vault/build
checkout/feature/credential/build
checkout/feature/generator/build
checkout/feature/health/build
checkout/feature/settings/build
checkout/feature/backup/build
```

This is11 original private generated/cache/temp roots plus24 checkout prefixes, not whole-R cleanup. The fixed modules come from consumed inner52-61, read only as text. Independent bounded manifest read found **2757 unique members/87770345 raw bytes, zero equality/ancestor/descendant conflicts with all24 checkout prefixes**. Rebind the same manifest before destructive admission: `B/reviews/desktop-integration03/source-prepare01/SOURCE.json`,1504175B,SHA256 `1a501d764306c1729cef49a411207ca840740d5fe6e284555767182478b55b23`; C15 commit `8f42274b04e206ff7254ca33d686a9666fce6723`, tree `3a8f53dddd54f5c42c34f772975f02619be118d5`. Source-before/after receipts are identical (`a71df23efdc330470cf1c71ce34e5344893473da0efa6506d5d4f699352237bc`); retain raw/check-out EOL qualifications.

Snapshot all present selected roots before deletion, with original no-follow parent/root identity checks; reject symlinks, nonregular/nondirectory entries, non-root ownership, regular-file multilinks and type/device drift. Never traverse or delete unlisted roots. Any unexpected report/test evidence encountered in the mandatory inventory is a HOLD, not authority to silently discard evidence or widen retention. Only after four-report retention and a complete bounded safe snapshot, recheck mounts, then bottom-up per-entry original-identity recheck/unlink/rmdir. Directory comparison must tolerate link-count/time changes caused by the closer's own admitted removals. Adopt the reviewed descriptor-bound concept only; do not execute/import old `remove_runtime()` or its whole-R allowlist.

The new implementation/request must seal finite wall-clock, entry-count and inventory-byte bounds before acceptance, with no more than the original250000-entry ceiling. Install cancellation/failure handling before action. Bound reached or partial failure consumes this attempt, preserves counts/first failure and leaves HOLD; no automatic retry. Source-concept approval is not approval of unspecified implementation/resource limits.

## Protected remainder and completion

Keep unchanged C15 source materialization at `R/checkout` (outside those24 prefixes), R itself, `R/git-metadata`, `R/git-index`, `R/source.oids` and `R/source.blobs` held. They may later receive a separately reviewed temporary-materialization allowlist; retaining them now is a deliberately narrow local scope, **not an invented external blocker**. W, T, permanent source/tests/reports, retained E evidence, shared caches, SDKs/toolchains and every older HOLD remain protected. Do not inspect the excluded metadata alias merely to clean disjoint generated roots; parent mount absence is still mandatory.

Write a separate compact closeout receipt with four report hashes, selected-present/absent roots, removed/retained counts, original settlement references, actual external completion and any partial cleanup. Preserve original failed records unchanged; success here means only report retention and allowlisted generated cleanup, not Detekt PASS, source closure, hardware proof or release readiness. No new helper was authored and no R read/probe occurred in this review. PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, TRAY01/all older HOLDs/consumed scopes, and G7/G8 CLOSED remain untouched.
