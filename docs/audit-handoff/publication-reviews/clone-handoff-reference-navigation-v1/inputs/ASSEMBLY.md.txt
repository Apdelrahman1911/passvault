# What the portable handoff establishes

Read `START_HERE.md` and `PERMISSIONS.md` first. This is a source/evidence
transport, **not completion of remediation or permission to run archived tools**.
No application correction or application test execution was added while preparing
this handoff. The 122 copied after-images are the existing G12 changes, not new
changes made during publication.

## Payload and independent checks

- The historical index contains **21,378 original report paths**, deduplicated
  into **16,510 exact blobs** in **11 packs** (20,443,296 compressed bytes).
- **226 paths are explicitly omitted**: 216 stopped-scope filename associations,
  eight original investigation-ledger aliases, and two public website captures
  with embedded API-key-shaped data. Included same-hash aliases of the seven
  distinct hashed omissions are also absent. No omitted bytes were fabricated
  or rewritten under their old hashes. The current outcome projection remains.
- New handoff reviews are separate in `publication-reviews/`, rather than being
  recursively inserted into the historical packs. Their exact original-path
  mappings and publication hashes are retained with the assembly receipts.
- The platform reviewer checked **94 selected high-value descriptors**: 93 exact
  path/hash/size matches and one justified original-ledger omission with its
  explicit current projection. This is a bounded check, not closure of every
  historical reference.
- The storage reviewer independently checked the original 811 raw source
  tuples, all 37 finding families and variants, eight PVDs, 12 PVU identities,
  and declared Git line-ending transport. Its reader review is source-only.
- Navigation independently reviewed the handoff qualifications and challenged
  the reference-inventory method. Keep each review's precise scope; none alone
  certifies archive decoding, actual Git checkout, public secret clearance,
  application correctness, or runtime readiness.

`PACKAGE.json` inventories this directory except itself. The Git commit binds
that manifest. Actual command results and Git/clone checks are recorded
separately under `../audit-publication/`; this avoids self-referential evidence
manifests. A manifest hash proves bytes, not the truth of the reported claims.

## Mechanical reference locators: useful, but not full reference closure

`reference-locators.json` is a **candidate-locator inventory**, not a proof that
every occurrence or every Markdown reference is resolved. The owner-controlled
writer read included report JSON/JSONL and public baseline Git blobs as data; it
did not import or execute referenced code. It used a cooperative frozen-input
envelope, not an adversarial same-user sandbox.

The v1 writer failed during Python parsing, before its body executed (exit 1).
That source and failure are retained. V2 was a syntax-corrected, unexecuted draft.
Following independent challenge, v3 ran once and completed (exit 0). These are
transport-tool outcomes, not application test cases or changes to audit gates.

V3 considered 15,890 JSON/JSONL files: 15,886 parsed and four explicitly retained
as unparsed. It observed 266,962 descriptor occurrences, grouped into **13,927
distinct `(declared path, digest)` candidates**:

| Candidate classification | Count |
| --- | ---: |
| Included path/hash exists in at least one context | 12,134 |
| Included same-hash alias, not the declared path | 1,027 |
| G12 raw-source locator, not fresh-clone proof | 536 |
| Baseline Git or calculated CRLF locator, not observed checkout | 83 |
| Unresolved or external descriptor | **109** |
| Withheld path policy; descriptor hash unresolved | **31** |
| Exact explicitly withheld hash | 7 |

Do not turn this table into an evidence-completeness percentage:

1. Grouping merges contexts. An exact candidate in one document does not prove
   that every occurrence names the same intended object. Only three example
   contexts are retained; JSONL examples include physical line numbers.
2. Size, mode, range and generation semantics are not validated. A generic
   `lines` key is only a range-like heuristic, not proof of a line interval.
3. Parsing uses Python's `json` behavior, **not an RFC-strict JSON validator**.
   Duplicate-key rejection applies to scanned documents, not the three startup
   inputs. The writer's unparsed-record phrase “strict metadata” must not be
   read as a stronger parser guarantee. Startup inputs were separately pinned.
4. Calculated baseline CRLF forms are suffix-based lookup candidates, distinct
   from the separately pinned source-transport contract. Actual Git/clone
   observations belong only to the publication receipts.
5. The three startup inputs are parsed before their in-program hash bindings
   are reread. Root retained pre/post hash checks and the cooperative freeze;
   the program alone does not establish continuous hostile-mutation isolation.
6. Markdown/free-form references, intended interval/canonical hashes, missing
   historical versions and inaccessible private runtime resources are outside
   this mechanical resolution. The **109 + 31 unresolved candidates stay open**.

Before relying on a historical proof, resolve its exact generation and intended
object through the current ledger, original review, index and available aliases.
If necessary evidence is unavailable, leave the affected conclusion blocked.
Do not recreate withheld scopes, repeat stopped procedures, or call a missing
reference a passing gate.

## Publication and resource limits

This is a dedicated Git branch only. No release tag, app/store build, version,
environment, approval, production signing, or store state changes with it.
Static screening of audit-owned source/reports is recorded, with explicit
omissions; it is not a guarantee that arbitrary text can contain no secrets.

Only transport/report commands ran during handoff assembly. There was no Gradle,
Xcode, native build, application test, app launch, or persistent worker to stop;
Gradle `--stop` is therefore **NOT_APPLICABLE for this assembly**, not a claimed
successful stop of earlier obligations. Earlier CLOSED gates remain CLOSED.
Fresh resource checks and any disposable-clone cleanup are in the publication
receipts. Required source, regression tests, frozen evidence and compressed
handoff packs are deliverables, not cleanup targets.
