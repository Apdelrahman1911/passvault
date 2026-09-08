# Independent challenge of the frozen G10 v2 runner

**SOURCE-INSPECTED; CORRECTIONS REQUIRED; UNADMITTED — DO NOT EXECUTE.**

Root's incomplete/unadmitted conclusion is sustained. F04's stable reader and F07's exact nonempty TEST XML minimum deserve narrow source-level credit; neither admits the runner.

- Reviewer: /root/audit_navigation; target author: /root.
- Frozen source: reviews/g12-database-runner-root-v1/v2-original.py.txt.
- SHA-256: 15f04a23b56a65ee476e723a2c4989f2110970f8ca13f6b64bef93c9c6104c27; **46,144 bytes / 935 LF**, fully displayed/read.
- Root review: reviews/g12-database-runner-root-v1/REVIEW.json, SHA-256 895f488a6610ba2b3c6f072e0ef167cb8e70914e7b040ad22fbe69c3963cb55c.
- Earlier self-authored v1 comparison: reviews/g10db-runner-draft-navigation-v1/REPORT.json, SHA-256 6d0a70655f931eeb838dc24cdd7afd8bd67147b6a7a79613034f3641bbb05b74.
- Seven pre-existing runner-correction assessments; **0 application LF, builds/tests, executed inert cases, new PVA families, product findings, closures or runtime admissions**.

## Reachability qualification

If earlier closed admission/resource gates hypothetically pass, empty original directory authority makes INPUT899 fail before first Journal after evidence-directory creation. Consequently later cancellation/deletion counterexamples are conditional source paths after prerequisites are repaired, not observed executable end-to-end v2 incidents.

## Seven-correction challenge

### F01 — Outcome propagation improved; late cancellation is not consistently committed

**PARTIAL_SOURCE_CORRECTION; TERMINAL_AND_PREFORK_CANCELLATION_CONTROL_INCOMPLETE**

- Root claim: PARTIAL: command failure latch present; final late interruption window still cached phase check.
- Prior requested correction: Latch command failure/unstarted/ambiguous outcome and cancellation durably and monotonically. Aggregate0 must require command actually started and exit0, required stop actually completed with exit0 and settled owned work, successful evidence/source/cleanup gates, no latched interruption/error, and the settled phase. Keep operational settlement distinct from independently verified105-method PASS and cache closeout.
- Current source spans: S06 373–399, S07 409–443, S11 618–683, S14 747–790, S16 874–935, T02 923–931.

**Credited source changes**

- The full operational_success predicate requires actual command0, required started/completed stop0, evidence/XML/output/run/source gates, and no current or latched interruption/errors (383-389).
- Normal nonzero command completion is now appended durably as an error (679-682). This defeats the earlier v1 clean-settlement/command23 counterexample at the source level.
- Journal.save latches interruption at entry (421), preserving many stop/cleanup interruption outcomes. Settled-failed and independently-unverified labels remain distinct.

**Attempted contrary explanation:** Perhaps operational_success and the final Journal.save make every late cancellation nonzero, so cached main state is harmless.

**Evaluation:** Not for every relevant pre-terminal schedule. finish computes phase at786 and completes its last save at789. The handler877-879 only changes global CANCELLED. A signal after that save but before/during final print924-928 or return929 adds no state error; return929 consults cached phase/errors rather than a fresh latch/full predicate. Also the command cancellation guard624 precedes environment626, journal632, log633 and Popen636, so a signal in that gap can still be followed by a command fork.

**Conclusion:** Root's partial assessment is sustained, not upgraded to acceptance.

**Reachability/limits:** The terminal example is latent: the as-written F02 bootstrap barrier, and then F03 removal lifecycle, prevent ordinary standalone successful settlement. The schedule applies only after preceding barriers are separately repaired; no executed false success or signal incident is alleged.

**Minimum prerequisites (not implementation or admission):**

1. Keep command/unstarted/ambiguous failure and cancellation monotone and durably distinguish operational failure from semantic result and cache closeout.
2. Define and review a terminal publication/exit commit boundary; synchronize final phase, interruption latch, full success predicate, durable terminal state and exit decision at that boundary. An extra early check alone is not a proof, and no promise is made about signals after the defined terminal boundary/process exit.
3. Define the cancellation/fork boundary so an interruption observed between the present guard and launch is not silently ignored. Recheck immediately before the command launch commitment; a concurrent or ambiguous launch retains its original stop obligation and narrow owned-child handling, never retry authority.
4. Retain permitted stop/independently safe cleanup after cancellation; do not clear errors or weaken retention to obtain aggregate0.

Unexecuted inert specifications: C01, C02, C07, C08, C17, C19.

No command, signal, child, journal, stop or cleanup was executed. This is not a PassVault regression finding.

### F02 — Original bootstrap authority and whole under-lock admission remain absent

**NOT_COMPLETE; STANDALONE_BOOTSTRAP_REFUSES_BEFORE_FIRST_INPUT_JOURNAL**

- Root claim: NOT_COMPLETE: bootstrap pins/intent and under-lock revalidation missing; standalone draft blocked before initial journal.
- Prior requested correction: Revalidate the exact immutable admission/input/source/root tuple after acquiring the original lock and before first mutation. Use one separately sealed bootstrap intent/ownership record in a pinned R parent (or separately admitted, once-created evidence namespace), then pin EV and its children plus required root/parent identities and verify-only thereafter. Partial registration or replacement is explicit HOLD; no marker repair/current-inode adoption. This can remain a small fixed bootstrap, not a generic cache manager.
- Current source spans: S01 1–82, S02 142–195, S03 214–242, S09 516–554, S10 556–579, S15 793–871, S16 874–935, T01 882–905, T04 800–805.

**Credited source changes**

- pinned_directory_fd refuses an unregistered or removed parent (158-174). Descriptor-relative exclusive writes and per-runtime allocation intent are useful fail-closed building blocks.
- prepare rechecks descriptor inputs and source before runtime allocation (540-554); this does not precede main's earlier evidence bootstrap.

**Attempted contrary explanation:** Perhaps the new pin verifier/allocate path establishes all original roots before mutation, or prepare's recheck suffices under the lock.

**Evaluation:** PINNED_DIRECTORIES starts empty81; the only population in all935LF is runtime allocate533. main calls load_admission882 before flock893, then only the resource/cancellation/EV-absence checks precede mkdir896-898 and write_new(INPUT)899. That write requires a pinned EV parent216/160, but prepare905 has not run. If earlier gates hypothetically pass, the source refuses the first INPUT write after unjournaled EV/journal/xml creation, before Journal construction900/save901. There is no whole admission/root/source/self revalidation under the acquired lock before that mutation.

**Conclusion:** Root's incompleteness and fail-closed standalone barrier are sustained. It is not evidence that the draft is safe or ready to execute.

**Reachability/limits:** All described bootstrap actions are source order only, conditional on earlier closed admission/resource gates passing. No EV/config/runtime existence or allocation was probed.

**Minimum prerequisites (not implementation or admission):**

1. Bind exact externally admitted original roots/parents and immutable runner/source/plan/review/admission tuple; revalidate the whole tuple under the original shared lock before the first mkdir or mutable artifact write.
2. Use a separately sealed once-only bootstrap intent/ownership control in the pinned R parent, or an explicitly separately admitted once-created evidence namespace. Capture exclusively created EV/journal/xml originals before writes and use verify-only thereafter.
3. Keep partial creation/registration as explicit HOLD; never fill missing originals from current inodes, copied receipts, marker repair, import-side map injection or namespace reuse.
4. Hash and parse each admission/plan/descriptor input from the same captured stable bytes, bind those captured images, and recheck under the lock. PLAN is parsed804 and reread for its hash805 (and again865); individually stable reads alone do not guarantee the parsed image is the hashed image.
5. Integrate original root/parent pins with subsequent allocation and verified-removal lifecycle, without overwriting original authority.

Unexecuted inert specifications: C03, C04, C05, C06, C18.

No bootstrap correction, new root intent, owner choice, runtime namespace or configuration has been written by this reviewer.

### F03 — Path-based deletion remains; verified removal is not integrated into pin state

**NOT_COMPLETE; ORIGINAL_DELETION_AUTHORITY_AND_REMOVAL_LIFECYCLE_INCOMPLETE**

- Root claim: NOT_COMPLETE: still path-rmtree; no descendant device binding; removal pin state not updated.
- Prior requested correction: Make destructive traversal act through a retained original target/parent descriptor with original identity and tree/device boundary checks, never re-acquiring deletion authority from a fresh pathname. If that cannot be kept explicit and bounded, retain outputs/runtime for separate exact root cleanup rather than claim safe automatic deletion. Preserve per-path durable intent, parent durability and partial/ambiguous HOLD; never infer success from later absence.
- Current source spans: S01 1–82, S02 142–195, S10 556–579, S13 716–745, S14 747–790.

**Credited source changes**

- The allowlist/protected-root checks, outstanding-stop retention, original identity comparisons, bounded no-symlink/type inventory and durable per-target deletion intent remain valuable (716-739).
- Partial or ambiguous deletion is not automatically retried by a recovery CLI; source comments1-13 explicitly disclaim hostile same-user namespace protection.

**Attempted contrary explanation:** Perhaps shutil.rmtree.avoids_symlink_attacks and the second original identity check supply the required original-directory authority, and the cleanup record permits subsequent absence.

**Evaluation:** rmtree740 still selects its own initial target by pathname after check739. A different real directory installed between them is not bound to the recorded original merely by rmtree's symlink-resistant implementation. The descendant scan727-735 has no same-device check, so a different-device mounted subtree is not refused there. Separately, REMOVED_DIRECTORIES is initialized82 and checked152/160 but never updated anywhere in935LF. After a hypothetically successful deletion and cleanup append743, the next inputs569 globally verifies the missing still-pinned directory before ownership(allow_removed_outputs=True)579 can use the cleanup record.

**Conclusion:** Root's three-part assessment is sustained. A successful-removal lifecycle is an additional integration gap within existing F03, not a new product/PVA family.

**Reachability/limits:** Deletion and subsequent pin failure are conditional on earlier admission/bootstrap/ownership/process/evidence gates being repaired and satisfied. No deletion, replacement or mount experiment was performed.

**Minimum prerequisites (not implementation or admission):**

1. Retain original parent/target descriptor authority for the allowed destructive traversal and parent durability, with bounded descendant device/type/identity checks. Do not re-acquire deletion authority from a fresh pathname or from the capability flag alone.
2. If that authority cannot be kept explicit and bounded, retain outputs/runtime for separate exact root disposition instead of claiming safe automatic deletion.
3. Tie any monotone verified-removed state to original identity, durable pre-deletion intent, verified removal, parent durability and durable settlement; make later pin/ownership gates recognize only that authorized lifecycle without replacing original pins.
4. Partial deletion, receipt/fsync failure, recreation or identity/device uncertainty remains HOLD/incomplete. Never infer success from later absence, insert removal markers merely to silence a verifier, re-adopt/recreate a path or retry automatically.
5. Keep the cooperative-namespace limitation: portable unlink/rmdir are not atomic inode-conditional operations. The correction does not promise hostile-same-UID-proof cleanup.

Unexecuted inert specifications: C09, C10, C19.

No cleanup authority or cache deletion is granted; shared lock, R evidence, protected roots and historical runtimes remain outside deletion authority.

### F04 — Per-read source/evidence identity is genuinely stronger

**READER_SOURCE_CORRECTION_PRESENT; END_TO_END_ORIGINAL_ROOT_INTEGRATION_PENDING**

- Root claim: READER_SOURCE_CORRECTION_PRESENT; end-to-end root pin/bootstrap incomplete.
- Prior requested correction: Use a stable no-follow reader that checks pre/open/post-fd/post-path identity and relevant metadata, including link/type/uid/mode and size/mtime/ctime, against original root/parent bindings. Replacement or ambiguity must fail the source/input gate; do not rebase the original digest. This improves the point-in-time claim, not atomic whole-tree preservation.
- Current source spans: S02 142–195, S04 261–270, S15 793–871, T04 800–805.

**Credited source changes**

- stable_file_identity142-144 covers device/inode/uid/full mode/link count/size/mtime/ctime.
- read_with_metadata177-195 compares pre-open/opened/post-fd/final-path identities and byte bounds/length, uses no-follow access, and checks applicable original ancestor pins.
- source_row269-270 returns hash and mode from the same captured read metadata instead of combining old-fd bytes with a fresh unrelated pathname stat.

**Attempted contrary explanation:** Perhaps F02's empty original pin map means no part of F04 improved, or the stable reader alone proves all immutable input binding.

**Evaluation:** Both extremes are wrong. The reader itself now rejects the old hypothetical pathname-replacement schedule at the intended per-read check, so the source-level correction deserves credit. But original root pins are not bootstrapped (F02), and repeated parse/hash reads can still concern different individually stable images. A per-read point-in-time check is not whole-tree atomicity, immutable namespace authority or hostile-writer proof.

**Conclusion:** Root's narrow positive assessment is sustained, with no whole-runner or runtime acceptance.

**Reachability/limits:** This is inspection of helper definitions, not a successful call or executed race test.

**Minimum prerequisites (not implementation or admission):**

1. Preserve these stable-reader and same-capture source metadata checks.
2. Complete F02's original bootstrap/root/parent and same-image descriptor integration; retain original digests rather than rebasing when drift is found.
3. Before any future use, require separately admitted exact-source inert checks of pre/open/post-fd/post-path mutation schedules. None was run here.

Unexecuted inert specifications: C05, C11.

The reviewer read matching frozen reports, not live application source. No mutation/race, syntax, import or OS-behavior test passed.

### F05 — Point-in-time disclaimer does not repair an incomplete final process sample

**NOT_COMPLETE; AFTER_ONLY_PROCESS_ROWS_STILL_DROPPED**

- Root claim: NOT_COMPLETE: before/after process intersection unchanged.
- Prior requested correction: Account for the complete relevant after-set and explicit birth/cwd changes. Exempt only positively identified collector transients; classify after-only or unclassifiable non-observer rows as unknown/HOLD or perform a bounded conservative re-observation. Preserve original birth/live-parent/group anchors, no unanchored group/name killing, and immediate fresh ownership/cwd checks before signals.
- Current source spans: S05 356–370, S08 467–514, S11 618–683, S14 747–790.

**Credited source changes**

- idle506-514 expressly labels its observation point-in-time, not global exclusivity.
- Known birth/live-parent/group anchors and the narrow unreaped direct-Popen exception must remain; unknown rows do not acquire signal authority merely by being observed.

**Attempted contrary explanation:** Perhaps the before/after intersection deliberately filters transient collector rows, and a later sampler or the disclaimer makes it adequate.

**Evaluation:** process_snapshot356-370 returns only before keys present in after. A non-observer worker/descendant born after the first sample and present in the final sample is omitted, not classified unknown. observe467-504 cannot restore a row never returned to it. Later observation may catch that row, but does not make this decision point's final sample complete; the omission affects idle/stop/deletion gates.

**Conclusion:** Root's unchanged-incompleteness assessment is sustained, without alleging a permanent runtime bypass.

**Reachability/limits:** This is a set-selection/source argument using hypothetical process records. No process/resource/cwd/provider observation or signal occurred.

**Minimum prerequisites (not implementation or admission):**

1. Account conservatively for the complete relevant after-set/union and explicit birth/cwd churn; only positively identified collector transients may be excluded.
2. Use birth-bound fresh cwd authority and bounded conservative re-observation or HOLD for new, reused, disappearing or unclassifiable identities. Re-observation is not a command/stop replay permission.
3. Preserve current original-owned identity checks immediately before any authorized signal and the narrow unreaped direct-Popen exception. Never expand to process-name or unanchored-group killing.
4. Keep claims point-in-time/advisory, not a host sandbox or global-idle lease.

Unexecuted inert specifications: C12, C13, C19.

No ownership of a real process, settled stop, exclusive host or safe live deletion has been established.

### F06 — Hashed report bytes are not the same as an accepting reviewer-authored attestation

**NOT_COMPLETE; NORMATIVE_REVIEW_ATTESTATION_NOT_VALIDATED**

- Root claim: NOT_COMPLETE: admission labels not report-author normative attestation.
- Prior requested correction: Define the small exact machine-readable acceptance schema and validate the referenced report's actual reviewer, disposition, purpose and exact target runner/source/instance bindings. If formats differ, require an immutable reviewer-authored canonical attestation bound to the full report. Do not count root-authored labels as independent acceptance. Reject wrong-purpose, stale, duplicate-author or nonaccepting evidence before allocation.
- Current source spans: S15 793–871, T03 848–863, T04 800–805.

**Credited source changes**

- The loop checks referenced report path/input membership and raw digest, and requires at least two distinct allowed reviewer labels.
- Independent result/cache-closeout labels790/927 are correct controls; they are not the subject of F06 and should remain.

**Attempted contrary explanation:** Perhaps hashing a report and checking admission's ACCEPT/reviewer strings proves two independent approvals.

**Evaluation:** The booleans848-851 and reviewer/disposition values858/860-862 are read from ADMISSION, not the referenced reports' normative content. The report bytes are hashed but their actual author, disposition, review purpose, target hash/tuple and limits are not parsed/validated. A correctly hashed historical, HOLD or wrong-subject report can be labeled ACCEPT in that reference object. Two supplied names are not two actual independent accepting reports.

**Conclusion:** Root's F06 assessment is sustained as an admission-contract gap, not a claim of malicious-root compromise.

**Reachability/limits:** This is a hypothetical malformed-admission schedule, conditional on earlier gates. No live admission file or external reviewer identity was opened or authenticated.

**Minimum prerequisites (not implementation or admission):**

1. Consume an exact small normative report-author acceptance schema; if report formats differ, require an immutable reviewer-authored canonical attestation bound to the full report.
2. Validate each report's actual reviewer, disposition, purpose, exact subject and required runner/source/instance/command/environment/condition bindings for that review role. Reject stale, wrong-purpose, wrong-subject, nonaccepting and duplicate-author evidence before mutation.
3. Count two actual distinct non-author accepting reviews where the admission contract requires them, not root-authored labels or historical acceptance of another task.
4. Keep this report's source-challenge limits normative: it does not accept the runner, runtime execution, graph/environment/closeout or test results.

Unexecuted inert specifications: C14.

The earlier v1 report and recommendation/method inventory were authored by this same reviewer; they are historical self-comparison, not a second independent rediscovery or admission vote.

### F07 — The seven-file minimum is improved; 105-method semantic PASS remains a separate gate

**EXACT_NONEMPTY_TEST_XML_MINIMUM_SOURCE_CORRECTION_PRESENT; SEMANTIC_RESULT_RECONCILIATION_SEPARATE**

- Root claim: XML_FILE_MINIMUM_SOURCE_CORRECTION_PRESENT; semantic105-method review still required.
- Prior requested correction: Before success-path cleanup, require the exact seven mandatory regular nonempty XML files and reject/retain unexpected file-set evidence under the frozen policy, while durably preserving all bounded available evidence. Failed/unstarted compilation may retain its existing independently safe cleanup policy but must remain aggregate-failed. Keep the later independent bounded XML105-method/name/failure/error/skip reconciliation explicit; do not add a generic runner framework or call file count/exit0 semantic PASS.
- Current source spans: S12 685–714, S14 747–790, S15 793–871, S16 874–935, T05 703–714.

**Credited source changes**

- preserve685-714 now records missing, unexpected and empty TEST-*.xml evidence, durably preserves bounded available evidence and requires the exact seven expected nonempty files after command0 before output cleanup.
- Operational labels remain NOT_YET_INDEPENDENTLY_VERIFIED with separate cache closeout; failing/unstarted command evidence can retain its independently safe cleanup policy without test credit.

**Attempted contrary explanation:** Perhaps seven nonempty named XML files and the METHODS counts prove 105 passing methods; alternatively, absence of an inline XML parser means no F07 correction is present.

**Evaluation:** The old missing exact/nonempty file minimum is present as source logic, so its narrow correction deserves credit. Seven one-byte malformed matching files could still meet that minimum, and METHODS818-822 checks class ordering/counts and within-class unique-name totals rather than equality with the approved source-name inventory. These facts require the separately admitted semantic gate; deferring it is intentional and not itself a new runner defect.

**Conclusion:** Root's narrow positive assessment is sustained; file count/size, command0 or cleanup cannot be promoted to semantic PASS.

**Reachability/limits:** No XML/test-result file was opened or parsed and no method ran. Malformed/wrong-name examples are inert specifications only.

**Minimum prerequisites (not implementation or admission):**

1. Preserve the exact nonempty expected TEST-*.xml minimum and bounded evidence/retention behavior.
2. Bind an immutable reviewed exact class/method and display-name mapping to the final source and admission before any invocation or result interpretation; 105 arbitrary names must not substitute for the approved inventory.
3. Require separately admitted independent bounded semantic reconciliation of all105 expected class/method pairs exactly once, with no missing/extra/duplicate/failure/error/skip under the approved policy, plus actual command/stop/source/evidence/cleanup outcomes.
4. Keep semantic PASS and cache closeout distinct from operational aggregate0. No parser implementation or execution is authorized by this review.

Unexecuted inert specifications: C15, C16, C17, C19.

No 105-test pass, database validation success, product readiness or cache closeout is established.

## Preserve scope and remaining closed gates

- Historical fixed G10 SELF/source/freeze/instance/U/configuration28-46/793-871 cannot be reused as currentG12 admission or made valid by a filename/label change.
- The existing exact fixed command/stop policies, explicit no-retry obligations, independent result label and separate cache retention/closeout limits should be preserved.
- The final Gradle/settings/plugin/dependency/native-loader graph, effective forked-worker HOME/TMP/native properties, current JDK/SDK policy and exact approved method/display inventory are not established by JVM filters or this runner review.
- Current original shared-lock authority, output absence, W/U/R capacity/resource floors and nonoverlap/runtime identity admission were not observed and remain separate closed gates.
- MAX_LOG is a polling check655, with child-exit break645-648 preceding it; it is not a hard write cap or host resource lease. No exhaustion or threshold event was observed.
- Only explicit operational entry is main-guarded934-935. Do not import to test this or elevate line7's comment into a literal guarantee that Python standard-library importing entails no filesystem access.
- No arbitrary hostile-writer/sandbox, atomic whole-tree, kernel-race or global exclusivity guarantee is claimed.

## Independence and evidence limits

- The runner and challenged root review are root-authored; this reviewer changed neither. The reviewer independently read the frozen v2 before root's review metadata.
- The v1 review, earlier recommendation and method inventory were authored by this same reviewer. They are historical self-comparison, not new independent rediscovery, a second reviewer or currentG12 method binding.
- The earlier M1-M7 contract text/hash is quoted through the pinned v1 REPORT.json; its separately authored original artifact was not reopened here.
- Root's prior display capture IDs/timestamps are report declarations only. Only this reviewer's four stored captures are claimed as its actual935LF source displays.
- Root states no helper corrections were attempted and current admission/resource/other gates remain closed; this reviewer did not inspect live runtime/configuration state.

The original helper was not reopened. SOURCE-EVIDENCE.json binds the four actual displays, all935LF,21 focused source ranges and the six exact retained inputs. INERT-REGRESSION-COMPARISON.json compares19 historical specifications; none is test code, executed, passing or admitted.

## Permanent fences

- G7/G8 CLOSED: no runtime/replay/recovery/cache authority.
- PVU007 STOP: no investigation or reformulation.
- PVU011 NO RETRY.
- PVA029: no automatic retry.
- No build while resource or other admission gates remain closed.

Only the fresh report packet is authored. Root may use its narrow source conclusions for audit wording; no implementation, runtime admission, validation PASS, closure, replay or central adoption is performed by this reviewer.
