# Independent source review — clone helper v1 → v3

## Disposition

**QUALIFIED SOURCE ACCEPT for v3 only.** No material source objection remains under the bounded, cooperative Git/evidence-transport contract. This is not execution permission, runtime verification, application readiness, or admission of any archived/closed runner.

- v1: two material source blockers, preserved as historical findings.
- v2: addresses those blockers and the provisional-log observation; required narrower final-journal wording.
- v3: exact docstring narrowing and fresh RUN namespace only; accepted with the limitations below.

The parent supplied these as new, unexecuted source versions at their review handoffs. This reviewer never imported or executed any version and consulted no clone execution receipt. Any subsequent root activity is outside this source-only packet.

## Exact source and delta identity

| Input | Bytes | LF | SHA-256 |
|---|---:|---:|---|
| v1 | 14688 | 283 | `cb5555bc2d39de8a1fb691120087e3193de064eff8996d24016fa33256fa61fe` |
| v2 | 18270 | 345 | `a97e05a08b5c656f23b23bf3c08b35a85c7a26c79beffd43dccc0aa2a507a697` |
| v3 | 18374 | 346 | `3bb706229a82d1aa12ceba8f8d9762ca3e68e8994636e2d2afcd3512834ed988` |
| v1→v2 inert unified delta | 24408 | 413 | `b5790944d31cd4e599640a163fe0aafcc066b9227c8271d7aa9440a1009a798a` |
| v2→v3 inert unified delta | 1084 | 22 | `afaa5366cfb14b725b6e4cf048fe15f5c723922b491cfa335573e309cb956327` |

Exact source bytes are retained with `.py.txt` inert suffixes. The v1→v2 delta was generated from independently pinned reads. The v2→v3 byte delta contains only the docstring change and `remote-clone-verification-v2` → `remote-clone-verification-v3`. An AST text comparison, excluding only the module docstring and RUN assignment, was equal; the RUN AST differs only by that namespace literal. AST parsing is not import, execution, compilation/runtime evidence, or a fixture test.

## Findings and resolution

### CHV-NAV-01 — inherited configuration (v1 material blocker)

V1 L24–L30 inherited the entire environment. Hook/template/trace settings did not disable Git's repository, index, object and config redirects, global/system user configuration, URL rewriting, credential helpers or HOME/netrc. Its evidence-reader calls at L236–L239 omitted isolated Python mode.

V3 L31–L44 uses an explicit child environment, absolute Git program, fresh owned HOME/config/tmp, disabled global/system config and credential helpers, noninteractive prompt settings, HTTPS-only transport, and constrained hooks/templates/fsmonitor/automatic maintenance/attribute-file behavior. V3 L236 requires isolated/no-bytecode flags; L288–L291 passes `-I -B` to both reviewed-reader calls. These changes close the inherited-context objection within the trusted installed runtime/cooperative namespace assumption, not a general sandbox guarantee.

### CHV-NAV-02 — launch and session registration (v1 material blocker)

V1 L79–L104 launched and journaled before entering the command try/finally, then appended the command only after fallible log reads. Final group checks at L257–L269 used that late list. Interruption or a failed CHILD journal/log read could leave a launched session absent from the observed groups, especially after the direct child exited.

V3 L116–L151 pre-registers the event/child before journals, sets a launch-attempt sentinel before Popen, retains the returned object directly, records registration separately, and covers launch/journal/wait with finally. L299–L328 checks all CHILDREN, retains stop failures, rejects attempted-but-unregistered launches and pre-cleanup journal ambiguity, requires direct children settled, and checks the recorded groups. An interruption before returned-object registration therefore preserves residual rather than falsely concluding that no child exists.

The final stop pass is limited to newly owned, still-unreaped Popen children; a prior stop error stays disqualifying even if a later stop attempt settles the process. It does not replay a Git command or revive old application/Gradle process authority.

### CHV-NAV-03 — final log identity (supporting v1 observation)

V1 L93–L103 could read size and hash while a producer remained live or stopping was ambiguous. V3 explicitly labels command journals provisional and avoids hashing there. Final snapshots use no-follow regular/single-link descriptors with identity, size and mtime consistency checks after the owned-group point observation (L100–L113, L309–L331).

This establishes only the stated stable read. The 16 MiB final snapshot cap is not a quota on child log writes/disk allocation; intermediate stdout consumption uses a size point-check then a read, under the trusted/cooperative producer assumption. It is not a hostile-growing-writer memory proof.

### CHV-NAV-04 — final receipt ordering (v2 claim qualification)

V2 broadly promised residual preservation for journal failure even though RESULT.json follows deletion. V3 L1–L7 now explicitly limits preservation to pre-cleanup ambiguity and disclaims a durable final-result guarantee before deletion. L332–L340 retains the actual ordering. No transaction/receipt-before-delete property is claimed.

## Other source checks

- The six planned commands are fixed public-HTTPS clone, commit/tree identity, tracked paths, pinned evidence-reader verify, pinned source verify and clean status. The helper contains no push, application/Gradle/native/device/store command. Its only other explicit subprocess call in the wrapper is the bounded local recorded-group ps point observation.
- Expected commit and stage-index digest are external arguments. Source checks bind the observed commit/tree, unique/count-equal tracked set, permitted regular executable modes, checkout byte lengths and SHA-256, reviewed reader/package hashes, reader result assertions and final status (v3 L232–L294). These are inspected checks, not observed outcomes.
- New owned root is exactly `/Users/abdelrahman/Projects/passvault/passvault-handoff-verify-20260908T091500Z`, containing repo plus empty home/config/tmp. RUN is a separate retained report namespace. Existing RUN or owned-root paths are refused, not reset.
- Cleanup records a birth identity, constrains the exact direct child of BASE, verifies parent/root identities, uses directory-relative no-follow traversal, rejects foreign-device/uid members, symlinks, nonregular files and hardlinks, preflights the whole tree, and rechecks entries before unlink/rmdir (v3 L157–L229). No source/report/cache/SDK/unrelated path is the deletion allowlist.
- An owned path created before birth identity can be retained rather than deleted; ambiguous launch/journal/stop or observed recorded-group members block cleanup. A normal failed transport may still remove its safe exact owned root. Transport failure is not erased by cleanup, and return zero requires both transport PASS and exact removal (L294–L342).
- RECOVERY precedes owned-root creation and warns against blind replay. Historical PIDs or birth journals are evidence, not automatic future cleanup/kill authority. Hard kill/power loss and early/final receipt failures need separately reviewed recovery.

## Retained qualifications

- **L1:** Source-only qualified acceptance of the exact v3 helper, not a clone run, command outcome, execution authorization, general runner admission, production readiness or app/runtime coverage.
- **L2:** Cooperative newly owned namespace and trusted installed Git/Python/OS are explicit assumptions. No adversarial same-user, hostile remote/program, escaped-session, mount-race or general sandbox guarantee is claimed.
- **L3:** The helper's group scan observes recorded PGIDs at a point only; it may conservatively block on numeric reuse. It does not authorize later PID-based killing or prove that a process cannot escape its group.
- **L4:** Final log identity covers the stable descriptor read after the group point check, not all future writes or pathname replacement. The final 16 MiB snapshot cap does not cap child writes/disk usage; intermediate stdout stat/read assumes the cooperative trusted producer.
- **L5:** Disk free-before/free-after are point observations, and four times indexed checkout bytes is a Git-transport headroom heuristic, not a storage reservation, hard resource budget, or app-build admission floor.
- **L6:** Command failure can safely remove the exact owned root when no launch/stop/journal/group ambiguity remains; ambiguity blocks deletion. Safe cleanup does not erase transport failure. Return zero requires both transport PASS and exact owned-root removal in source.
- **L7:** Final RESULT.json is written after cleanup and may fail; files are fsynced but this is not a crash-atomic receipt/cleanup transaction. Early failures or hard kill may leave partial reporting/residuals. Recovery requires fresh review, not replay.
- **L8:** The externally supplied expected commit/index digest, reader/package pins and reader output assertions were inspected as source. This child did not read the actual stage index, execute the reader, consult runtime receipts, validate the remote, or independently re-establish pack/source semantic coverage in this task.
- **L9:** This review only supports the separately authorized dedicated-branch handoff's bounded verification after its push. It does not authorize a push, main/testing/release mutations, merge, tags, signing, stores or replacement of a candidate.
- **L10:** V1/v2 remain historical source versions; their source defects or supersession do not justify running/retrying them. V3 is one-shot in its new namespace. Any existing output/root or ambiguity requires preservation and fresh authorization/review.

## Scope and unchanged gates

Only guarded named-file reads, inert diffs/AST analysis, reasoning and this report publication were performed. There was no helper import/execution, fixture/runtime test, Git/network/clone/push, application edit/build/test, process/provider/cache/cleanup operation, private-file read or excluded STOP/procedure-body inquiry.

PVU-007 STOP; PVU-011 NO RETRY; PVA-029 no automatic retry; G7/G8 CLOSED. The old Gradle/application runner remains outside this task and unadmitted. No main/testing/release mutation, merge, tag, signing/store action or candidate replacement is authorized. Archived commands/instructions in this packet are inert.

The relevant release-provenance skill was applied only for exact-byte/commit authority, staged publication, failure and authorization boundaries. Store/build/promotion checks are not made applicable to this Git-only helper. This packet neither revises previous reviews nor modifies frozen HD; it is supplemental report evidence for the parent.

## Publication metadata incident

The first report-only writer failed during Python input parsing with a non-UTF-8 diagnostic (capture `2a6fab`, exit 1). Its module body did not run. A separate guarded exact-path check (`c92c92`) confirmed that this report namespace was still absent. The parent explicitly authorized a corrected ASCII-only report writer. Exact failure output, the fingerprint of the command text as supplied, and the absence result are retained in `PUBLICATION-METADATA-INCIDENT.json` and the cited captures. This was not a helper/clone/application attempt or recovery of an archived runtime operation.

## Evidence locations

`INPUTS.json` maps exact inputs/deltas; `FINDINGS.json` is the resolution ledger; `SCOPE.json` retains limitations and authority boundaries; `STATIC-CHECKS.json` records text-only checks. Captured command text and stdout are retained as inert files, with tool capture IDs in `CAPTURES.json`. `PUBLICATION.json` indexes all packet members other than itself. Its pin and the fresh independent post-seal rehash are reported externally so the completed packet is not reopened.
