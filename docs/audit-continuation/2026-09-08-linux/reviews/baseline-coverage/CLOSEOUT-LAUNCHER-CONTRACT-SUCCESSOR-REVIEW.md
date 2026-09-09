# Closer launcher contract successor — narrow independent source review

Finding LC-LAUNCHER-CONTRACT-001. Source/PLAN author `/root/editor`;
independent correction reviewer `/root/baseline_coverage`; root alone executes.
**ACCEPT_NARROW_SOURCE_CORRECTION_PENDING_REGRESSION_AND_INSTANCE.**
This is not completed regression, overall closeout viability, an actual F/C
approval, cleanup permission or a new qualified closure.

## Exact before/after and review method

| Role | SHA-256 | Bytes / physical LF |
| --- | --- | ---: |
| Preserved rejected helper | `b16774ff6d0c14ece40dbe2c75684a2b07e7c90c9da4373ec0a6e12e87cc7bec` |59,183 /1,094|
| Successor helper | `0bed64d9ecb2ebb3ff7a41d04d8c66240e9c82c161f28f13860b8c54f93f5c02` |59,443 /1,097|
| Preserved rejected PLAN | `cffc69357a0c4f0c5e46fd4d282439ece513eb2f166074c05f2ab4e679f8a57d` |29,298 /474|
| Successor PLAN | `bdd2f816592d409c7211375673ba3507375c52af8d41bf0688ab196fe8f0392c` |32,246 /521|

Source is `scripts/audit/linux_database_closeout.py`; PLAN is
`reviews/linux-closeout/PLAN.md`. Original copies are
`launcher-contract-rejected.py.txt` and `launcher-contract-rejected-PLAN.md`
beside the PLAN. Both copies match exact publishedbb094f8 Git blobs, not just
new author-provided hashes. Author reports exclusive preservation before edit;
this reviewer establishes their byte identity, not a retrospective allocation
observation or permission to execute the rejected copy.

Read both complete literal diffs (ce98a7), then the entire current run_contract
and relevant admission/PLAN context (fec771). Data-only in-memory substitution
of precisely two old source expressions reproduces every successor source byte.
The PLAN has only two delta blocks: a46-line v3/history/compatibility/accounting
insertion and the one-row-field schema replacement. All other source and PLAN
bytes are preserved. This reuses the prior exact source review without claiming
another full-file semantic audit or executing/importing either helper.

## Independent correction challenge

The old source's single `evidence` field cannot simultaneously equal fixed E and
iterate four dict rows. The new source leaves directory identity at513 unchanged,
then at527-531 obtains **distinct `evidence_files`**. It must be a list with the
same four entries as fixed LAUNCHER_FILES, every entry an ordinary dict with
exactly `{path,sha256,identity}` keys, and exactly the ordered fixed path values.
The actual admission consumer at697-700 uses that same new field and retains
`dirs.read(path, MIB, durable=True)` plus complete pin and SHA equality unchanged.
There is no missed old row-array consumer; the other `launcher['evidence']` is
the intended directory check. PLAN199 and216-217 now express disjoint shapes.

The key constraints are no longer contradictory: the directory string and
four-object list occupy different members. This demonstrates removal of the
specific static impossibility, **not construction or execution of a complete
valid original-run packet** or proof that every other admission predicate passes.

Counterexamples retained/challenged:

- Old list-in-evidence still fails the directory identity. Correct E without
  evidence_files fails lookup; the ordinary exception path remains admission HOLD.
  No missing-field KeyError is misreported as an executed test or successful hold.
- A string/dict/null/number instead of the row list fails the list guard before
  projection. Wrong count fails before row processing. Non-dict entries and
  missing or extra row keys fail before `row['path']` projection.
- Reordered, duplicate, missing, extra or unknown paths cannot equal the four
  distinct ordered fixed paths. Ordinary JSON values cannot inject custom
  equality or dict/list subclasses. Duplicate/equivalently escaped object keys
  remain rejected by unchanged strict_json.
- Correct keys alone are not sufficient: wrong per-file hash or original pin
  still fails the unchanged second durable-reader comparison. No producer
  modification or in-place launcher mutation occurs between run_contract return
  and this consumer. No alias or deprecated-field fallback was added; an unused
  extra top-level member is not read as an alternate source of row authority.
- Additional exact-key rejection intentionally narrows this new audit-receipt
  shape. It does not change a product/vault/backup format or adopt an old invalid
  receipt. The reviewer was not supplied a completed actual old receipt/run to
  migrate, and made no filesystem-original existence claim.

All prior original input, source, actual-role/F/C approval, lock,35-allocation/
33-target, stop-versus-positive-no-Gradle, cancellation/process/mount/resource and
retention constraints are byte-unchanged. The new guard creates no paths,
launches/signals no process and changes no deletion target. Whole-runtime,
checkout/source/test/report deletion and missing-original adoption remain barred.
The schema correction must not be used to bypass any subsequent failed gate.

## Protocol compatibility and independent regression still pending

PLAN v3 explicitly preserves the earlier defect/source acceptance/reviewer miss,
C3 publication authority and exact rejected copies. It retains the outer/inner
producer distinction: ee46cfd9 database outer does not write/read completed
RUN/LAUNCHER.json; its c5bb3831 PLAN delegates to the closer contract. Inner
346e1655 only attests retained closeout. No broad `.evidence` rename is warranted:
request/acceptance/evidence-manifest identity members stay strings; manifest.files
and numeric LIMITS.evidence_files retain their separate meanings.

The current field map and root's future receipt producer still need explicit
successor bindings. The separately frozen new outer closer is not reviewed by
this narrow report. New helper/PLAN hashes must enter freshly reviewed controls,
requests, outer integration and actual role-correct approvals. Old hashes/reviews
must not float to this successor. The immutable C3 source remains rejected for
this contract; this source-only correction does not retroactively make it viable.

build_config proposed coherent synthetic full-run-contract controls, including
both stop-success and affirmative-no-Gradle branches, malformed/duplicate shapes,
row ordering and second-consumer binding failures. This reviewer requested an
extra-row-key negative for the tightened exact-key guard. That discussion is
**not a frozen/accepted test inventory or execution admission**. Full-source
control review and meaningful actual results remain required before root's
chosen prebuild gate can clear.

The positive should exercise actual successor run_contract using serialized,
internally coherent preceding request/approval/journal/source records and reach
normal return. Do not stub require/get/parser/hash or reduce1198/105 constants
merely to manufacture a pass. A verbatim extracted second-admission loop, if
separately admitted, can supply only its labeled component/durable-reader scope,
not full admission or original runtime provenance. Synthetic35 allocation and
1198 source records are not35 created runtime directories or1198 real source
files. Legacy-invalid receipt shapes can be rejected by the successor without
executing b167 or any archived helper. Still-mandatory LC-C05/06 controls are
separate; runner22 controls do not substitute for this different closer.

## Status, operations and preserved limitations

The **specific schema contradiction is source-corrected**. Runtime regression,
whole admission satisfiability, outer supervision, original-instance evidence,
process/directory cleanup and net disk reclamation remain unverified. Overall
build/closeout admission remains ungranted; this report does not supply F03
completion or actual C01-C07 ACCEPT files. Root retains the explicit prompt
resource-closeout scheduling and controls-before-build decision.

Prior miss disclosure and defect review3982b56c remain normative history; this
reviewer contributed bounded design and reviewed the editor-authored correction
but did not edit its source/PLAN. Source-only dispositions, precise author roles
and no independent vote on this reviewer's own documents remain explicit.

Operations: bounded no-follow regular source/data reads with stable eight-field
hash checks, literal text differencing/reconstruction, one read-only Git batch
for the two published before-images, and these two compact permanent reports.
No helper import/AST/syntax/parser execution, build/test, original-process probe,
temporary/cache allocation, daemon/worker, signal, deletion, staging or source
change occurred. All read/write FDs closed; wrapper stop not applicable.

Current counts remain19/25 original confirmed,22/38 all confirmed,2/12 original
suspicions; eight PVD explanations/owner decisions remain separate. No product
family, qualified closure, application case, hardware test or semantic LF is
added. Preserve PVU007 STOP/PVU011 NO RETRY/PVA02949 checks44 PASS5 FAIL/no automatic
retry/G7-G8 CLOSED/old-runner prohibition/Windows FAIL-cleanupHOLD14 unstarted,
synthetic-only, root sole execution owner, PVD/hardware and protected-ref/tag/
dependency/version/identity/signing/store/publication/occupied1017001 fences.
