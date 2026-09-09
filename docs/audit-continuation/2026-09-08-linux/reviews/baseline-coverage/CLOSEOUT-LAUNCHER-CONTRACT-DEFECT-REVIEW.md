# Closer launcher contract — independently confirmed impossible admission

Finding **LC-LAUNCHER-CONTRACT-001** (audit infrastructure, not a PVA family).
Finding/helper author `/root/editor`; independent challenger
`/root/baseline_coverage`; sole execution/publication owner `/root`.

**CONFIRMED_SOURCE_DEFECT; PREBUILD_CLOSEOUT_VIABILITY_HOLD.** No helper was
imported, syntax-checked, invoked or tested. This report adds a late explicit
qualification to the unchanged C3 freeze, not a silent source/review rewrite.

## Exact affected identities

- `scripts/audit/linux_database_closeout.py`:
  `b16774ff6d0c14ece40dbe2c75684a2b07e7c90c9da4373ec0a6e12e87cc7bec`
  (59,183 bytes/1,094 LF).
- `reviews/linux-closeout/PLAN.md`:
  `cffc69357a0c4f0c5e46fd4d282439ece513eb2f166074c05f2ab4e679f8a57d`
  (29,298 bytes/474 LF).

Both remain unchanged members of selected C3 manifest3ac00208. The paired JSON
binds their exact bytes, pertinent source ranges, surrounding consumer/producer
contracts and prior reviews. It grants no authority to edit accepted inputs.

## Independent proof and counterexamples

`run_contract` parses the captured `RUN/LAUNCHER.json` once (455-462), using
`strict_json` (118-126). Its object-pairs hook creates ordinary dictionaries and
rejects duplicate parsed keys; no custom object decoder, mutable proxy, eval or
user-defined equality is involved. Standard JSON strings/lists remain ordinary
Python builtins. The fixed `E` is a nonempty absolute Path whose `str(E)` is the
required evidence-directory string.

1. Lines511-519 require `launcher['evidence'] == str(E)` alongside fixed format,
   run ID, runtime, argv, completed/exit and coordination assertions.
2. Without reassignment or mutation of that parsed launcher, line527 requires
   `[row['path'] for row in launcher['evidence']]` to equal the four fixed ordered
   external-evidence paths. Admission694 later uses the same field as rows with
   `path`, `identity` and `sha256`, reading/checking their original files.
3. The PLAN repeats the contradiction:153 requires `evidence=E`;170 requires
   that very key to hold four objects. This is not merely one stale comment
   contradicted by a working implementation.

A string equal to E passes the directory conjunct, but its first iterated value
at527 is a one-character string, not a mapping. `row['path']` therefore predicts
a TypeError under ordinary Python semantics. A list of the four valid objects
fails the earlier equality. A dict, number, boolean, null, different string or
empty value cannot satisfy equality to the nonempty fixed string. Duplicate
`evidence` keys, including equivalent escaped spellings, are rejected by the
parsed-key guard rather than providing two separate values. A nested second key
is not the top-level field read at both sites. Unused metadata/nonfinite-exponent
quirks cannot supply a JSON object with custom equality/index behavior.

There is no file reread, callback-provided launcher value, conditional branch or
in-place conversion between these checks. A pathname race cannot change the
already parsed local value. Even omitting either field or invoking a cancellation
path only fails earlier; it does not create a successful admission alternative.
No finite valid JSON receipt satisfies this conjunction.

This is a static success-path impossibility proof, **not an executed exception**
or a claim that a complete otherwise-valid original-run packet was constructed.
Preceding input, actual-approval, original-binding, terminal/stop/source and time
guards can reject first; that changes the rejection point, not the impossibility
of the success path. The author independently identified the contradiction and
confirmed no alternate valid representation; this nonauthor independently traced
and challenged the parser, data flow, guards and candidate counterexamples.

## Reachability, surrounding protections and impact

`admission()` unconditionally calls `run_contract` at652 after captured-input and
actual C-approval checks. `main()` requires that admission to return at999-1000
before opening/acquiring the original lock (1007-1009), creating the closeout
journal (1017) or reaching the deletion loop (1040). The ordinary exception path
1062-1090 records an admission HOLD on stderr and attempts owned descriptor
closes; no journal exists at this failure point. No actual close/exit result is
claimed here, and interruption/host-loss limitations remain.

The defect is **fail-closed loss of cleanup availability**, not a demonstrated
unsafe deletion, auth bypass or application data-loss vulnerability. Both the
successful-stop and positive-no-Gradle variants share this earlier contradiction.
No source-time assumption that later guards will save a malformed instance makes
a valid instance possible. Launching the retained-output database batch while
relying on this exact closer could strand its large generated/private outputs;
that violates the intended viable prompt-closeout prerequisite. It does not
authorize whole-runtime deletion, stop replay, alternative namespace or recovery.

No completed database/closer run or actual original receipt was supplied for this
review. No current process/filesystem-original probe or instance approval was
performed. This report establishes no existing stranded allocation.

## Prior reviewer miss and publication consequence

I previously authored source acceptancec47bb8c9 and overlooked this contradictory
field contract. **That was a reviewer miss.** Preserve that acceptance, its
source snapshots and earlier successful/corrected source findings verbatim as
historical evidence; they no longer establish viable prebuild F03 closeout for
b16774ff. My C3 review4bcf59d5 and source/timing descriptions must be read with
this later qualification, not as still-current acceptance of that viability.
No previous test PASS is revoked or fabricated: this closer was unexecuted.

The gate timing distinction in13679c9c is unchanged: prompt-closeout design is
required before the build; closer controls and actual C admission are separate
obligations. **The required viable design is now HOLD for this exact version.**
Runner22 controls did not exercise the closer and do not answer this finding.

Unqualified publication presenting this closer as viable is blocked. Root may
publish the immutable historical source/reviews only together with a prominent,
linked additive late-defect/HOLD qualification. Root must separately freeze and
review any authorized successor; changing this report or adding future receipts
cannot make the old source satisfiable. There is no new product-family count,
qualified closure, semantic LF, application execution or actual cleanup credit.
Current progress remains19/25 original,22/38 all confirmed,2/12 suspicions.

## Narrow successor proposal — sufficient only for this contradiction

Subject to root authorization, keep the unambiguous directory member
`evidence: E` and introduce the separate receipt array `evidence_files`.
Change **both** list consumers (527 and694), and the PLAN's row-list member170,
without weakening the exact four-path order, per-file size/durability/hash/full
identity checks or the original-directory assertion. Explicitly validate array
and row shapes; old list-in-evidence, missing evidence_files or ambiguous alias
fallback must not silently become accepted forms.

This proposed separation removes this specific string/list contradiction; it
is not a guarantee that all other admission predicates can be met or that
closeout/process/resource behavior works. Do not broadly rename other objects:
`request.evidence`, acceptance.evidence and EVIDENCE.json.evidence remain directory
strings; EVIDENCE.json.files is the separate run-evidence manifest, and the
numeric LIMITS.evidence_files is a file-count cap.

Producer/consumer compatibility still requires explicit reconciliation:

- Accepted database outeree46cfd9 does not write or parse the completed
  RUN/LAUNCHER.json. Its PLANc5bb3831 lines177-185 delegates that future root
  receipt to the closer PLAN. Update/add a bound qualification for that contract;
  no source rerun or alternate collection is justified by this schema correction.
- Inner runner346e1655 lines843-844 requires only the retained-closeout
  attestation; it has no completed-LAUNCHER consumer. It neither fixes nor needs
  an evidence-field code change merely to resolve this defect.
- The selected instance field map0edf9d58 and future root receipt writer/new
  closer-supervisor assumptions need an exact successor schema reference. Root
  must freshly bind successor helper/PLAN/control/request and both role-correct
  actual approvals; old hashes/acceptance do not float to new source.
- No database product format, dependency, app identity/version, vault/backup
  compatibility, occupied mobile candidate or PVD boundary needs redesign.

The separately assigned regression designer was notified. A meaningful proposed
control should exercise the actual target run_contract/parser with a coherent
synthetic precondition packet, reaching its normal return for distinct directory
and four-object fields, while preserving old-version rejection as a discriminating
counterexample. Negative controls should cover ambiguous/duplicate parsed keys,
wrong/missing types and reordered/missing/duplicate external rows; the second
admission consumer must demonstrably retain exact durable file bindings.
Hardcoding only a source substring, bypassing preceding guards or asserting a
mock return is not that evidence. These are proposed synthetic schema controls,
not real-original provenance, deletion/process proof, automatic execution or a
replacement for still-mandatory LC-C05/06 effects. Root alone can admit them.

## Operations and restrictions

Only bounded source/data reads and these two compact permanent reports were
performed. Data files were read no-follow with stable eight-field identity/hash
checks. No helper/parser import, syntax check, parser experiment, build/test,
original-process probe, temporary/cache allocation, signal, daemon, deletion,
staging or source edit occurred; metadata FDs closed. Wrapper stop is not
applicable. The source-derived predicted TypeError is never counted as an actual
failed test or consumed invocation.

PVU007 STOP, PVU011 NO RETRY, PVA02949 checks/44 PASS/5 FAIL/no automatic retry,
G7/G8 CLOSED and old helper/runner prohibitions remain. Keep Windows FAIL/cleanup
HOLD/all14 unstarted, synthetic-only, PVD/hardware and all protected-ref/tag/
dependency/version/identity/signing/store/publication/occupied1017001 boundaries.
Root owns admission, execution, cleanup coordination and publication. This
reviewer remains available for the separately frozen new outer-closeout source;
that unlisted work is not reviewed by this defect report.
