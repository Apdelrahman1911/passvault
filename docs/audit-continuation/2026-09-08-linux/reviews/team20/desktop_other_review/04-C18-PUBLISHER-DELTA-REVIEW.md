# C18 publisher delta: independent inert-text review

- Reviewer: `/root/desktop_other_review`
- Author: `/root/desktop_other_author`
- Date: 2026-09-11 UTC
- Disposition: **ACCEPT_C18_PUBLISHER_TEXT_DELTA_ONLY_WITH_FINAL_BINDING_HOLDS**
- Scope: the new, unbound C18 publisher candidate derived from C17, not another
  historical audit and not the final C18 payload, coverage acceptance, execution
  admission, or publication result.

**Accepted candidate: v3 only**, including root's required original
T/.git/config preservation guard. The initially reviewed v2 was superseded
before this report was sealed; it and the rejected v1 are retained provenance,
not promotable alternatives.

This is **not** `ACCEPT_C18_SOURCE_PUBLICATION`. Only the genuine final
`/root/coverage_review` acceptance may supply that disposition for the eventual
selection. Root alone may promote the candidate, author the real config/payload,
reconcile their final freezes, and separately admit actual Git/publication work.
No build, test, CI, runtime, helper invocation, replay, cleanup, or new closure is
authorized by this report.

## Exact reviewed objects and method

All paths below are relative to
`docs/audit-continuation/2026-09-08-linux/`.

| Object | Path | Bytes / lines | SHA256 |
| --- | --- | --- | --- |
| Consumed C17 source baseline | `publication/CHECKPOINT-17-PUBLISH.py` | 18687 / 266 | `3232ee3a4fbc6efbfc691c94f2a5abe9f712336aabe0753a9ca866dfe61dfaf4` |
| Active C18 candidate, v3 | `reviews/team20/desktop_other_author/c18-publication/CHECKPOINT-18-PUBLISH.py.txt` | 19384 / 274 | `85f9840ecaf4fa5657f63ae11b3e9eb20560de9492d0173c4b5842e7b28a1345` |
| Exact C17-to-C18 text delta | `reviews/team20/desktop_other_author/c18-publication/C17-TO-C18-PUBLISHER.patch.txt` | 12419 / 152 | `7435560e0f260a9c97726cfea96cb16820a8a74d1b7f87a713c5568acf29c0b6` |
| Retained superseded v2, **not active** | `reviews/team20/desktop_other_author/c18-publication/CHECKPOINT-18-PUBLISH.v2-superseded.py.txt` | 18196 / 263 | `88e7229c5a96dce5c6bba4b20c51eea59481127fbad4305dc432c79b24e77fcf` |
| Retained rejected v1, **not active** | `reviews/team20/desktop_other_author/c18-publication/CHECKPOINT-18-PUBLISH.v1-rejected.py.txt` | 18240 / 263 | `a756c0e76174327ffc31e426b0afa2afe2a707df6c3cb573dac3daa3834b4e45` |
| Authoritative prospective GUI03 release review | `reviews/team20/pva031_review/gui03-closeout/PUBLICATION-RELEASE-REVIEW.json` | 8030 / 170 | `8a4868834e2ace3a63cde9afffa71acd18418f3153f220c5dce22bf477bcff7c` |

Read the complete candidate and every changed block against the exact C17 text.
Independent standard-library data-only SHA256 and unified-text comparisons
confirmed the authored delta exactly equals the baseline-to-candidate diff.
Forward reconstruction reproduces the candidate byte-for-byte; inverse
reconstruction reproduces the baseline byte-for-byte. The author provenance
record's baseline, active-candidate, delta, rejected-v1, and superseded-v2 hashes
agree with the independent reads. The final author rationale and text-comparison
report were also read as data, not taken as substitutes for independent checks.

The material v2-to-v3 delta is exactly two insertions: seven lines at 197-203 and
four lines at 255-258, with no deletion or replacement. Removing just those 11
lines reproduces the retained v2 bytes exactly. Both new blocks were reviewed
against the authoritative release record, whose hash and full config pin were
independently compared as JSON/text data.

These checks neither imported nor executed either recipe or the patch. No
AST/compile/syntax check, Git/T access, process/SDK/runtime probe, build/test/CI,
credential operation, original-store operation, cleanup, or subdelegation was
performed. Reviewer writes are confined to this review lane.

The historical C17 config is not a C18 config: it used C16 as predecessor. The
new candidate instead requires the retained C17 pair explicitly:

- Commit: `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49`
- Tree: `d1bd6ca5b18d08ff3ff15896d9a78af9016be799`

## Changed blocks accepted within this scope

Line references are to the exact active candidate above.

### 1. New identity and real independent-acceptance gate

The header marks C18 as new and unbound. Config, payload, whitespace-log, and
published-status names are C18 names, not reuse of consumed predecessor output
names (lines 2-7, 106, 152, 259, 274). Lines 151-155 require both root's config
and selection to use the exact retained C17 predecessor.

Lines 168-172 require the config-bound review bytes' SHA256, exact reviewer
`/root/coverage_review`, exact disposition `ACCEPT_C18_SOURCE_PUBLICATION`, and
exact selection SHA256. They do not author or manufacture an acceptance. The
already-existing coverage integration review is not the final selection
acceptance, and this publisher-delta review is not a substitute. Root must bind
the actual final review path/hash and preserve the genuine reviewer provenance.

### 2. C17's two removals cannot be reused

Line 158 requires `selection['removals'] == []`. The old exact-two removal
constant, alias/removal validation, T unlink loop, mode-zero index records,
expected-tree deletions, and removal-name union are removed. Line 230 records
an empty removal list. Lines 236-239 preserve a complete base-plus-selected
expected index and exact changed-name equality; lines 247-248 preserve the
sole-parent and complete committed-tree checks. There is no new deletion scope.

### 3. Canonical namespace guards precede selected reads

The guard at lines 159-167 covers **selected plus companion rows**, before the
first `frozen(S, allrows)` at line 173. Canonical relative POSIX spelling is
required: empty/dot, absolute, parent-traversal, newline/NUL, and normalized
aliases such as `./...` or repeated separators cannot bypass a raw prefix test.
Malformed non-string descriptors also fail before a selected source read.

Any `.git` path component is forbidden, including nested components, so selected
descriptors cannot name W's retired Git store or T's Git internals/config.
Canonical `.github/workflows/` descendants are forbidden. Root additionally
requires **all** `.github` paths to remain unselected in the actual payload;
the candidate's narrower workflow-prefix check is not represented as a blanket
`.github` ban.

Within the canonical audit `requests/` subtree, only these exact top-level
leaves are permitted:

1. `LINUX-DETEKT-03.json`
2. `LINUX-ANDROID-COMPILE-01.json`
3. `LINUX-DESKTOP-TRAY-01.json`
4. `LINUX-DESKTOP-INTEGRATION-02.json`
5. `LINUX-DESKTOP-INTEGRATION-03.json`

Unknown leaves, nested entries, case variants, and descendants of an allowed
leaf do not equal this set. The guard is applied to companions as well as the
main selected list. It permits this set; it does not prove a record was
consumed, or require all five records to appear. The final exact selection and
consumption ledger must establish those facts. Root's preliminary comparison
reported five new request paths and no changed request paths; that preliminary
observation is not a replacement for the final frozen comparison.

**Clarified review boundary:** root explicitly requires necessary inert
`REQUEST.json` evidence under `reviews/` to remain transportable, including
unbound/held control evidence that does not match a workflow trigger. There is
therefore no global basename prohibition. The retained rejected v1 had that
overbroad prohibition; it is not the accepted candidate. Transporting those
records must preserve their unresolved/consumed state, not activate them or
convert them into admission. Final payload coverage remains responsible for
that distinction.

### 4. Only the selected-path count ceiling is raised

Lines 160 and 174 impose a positive maximum of **450 selected-plus-companion
paths**, with exact uniqueness checked after freezing. This is not 450 selected
paths plus an unbounded companion allowance. It is a ceiling, not a final count
or a claim that the preliminary 392 paths are the complete payload.

All other inherited numerical bounds are textually unchanged:

- 600 seconds overall; 60 seconds per ordinary Git command; 120 seconds for push.
- 131072-byte stdin, 4 MiB stdout, 1 MiB stderr; no success with truncated output.
- 16 MiB per bounded source read.
- T inventories: 100000 files and 512 MiB logical bytes.
- Launch floors: 12 GiB available disk and 25% available RAM; running floors:
  8 GiB and 20%.

The 512 MiB check is a before/after inventory condition, **not** a hard
intermediate-growth cap. Raising the path count does not enlarge it or prove
the final selection fits it. Final row count, stdin size, byte volume, namespace
inventory, and resource admission remain root responsibilities.

### 5. Authored-whitespace scope is bound to config and selection

Lines 176-179 take the final list from
`CFG['authored_whitespace_paths']` and require a nonempty list of strings,
maximum 450, no duplicates, a subset of the frozen selected/companion paths,
and exact ordered equality with the final selection's whitespace list.
Therefore an unbound config cannot silently narrow an already-reviewed
selection list while still satisfying this gate.

The inherited actual `git diff --cached --check -- <paths>` remains at line 240,
with literal pathspec behavior, nonzero exit rejection, the additional empty
stdout requirement, and a new exclusive C18 whitespace log. This is neither an
empty-list bypass nor a formatting rewrite of raw evidence. The prior raw-
evidence whitespace failure remains evidence; this review did not rerun it or
claim the eventual C18 whitespace command has passed. The final authored-path
list itself is not yet supplied or accepted here.

### 6. Exact original T/.git/config preservation is required, not inferred

Root's material release-condition amendment is implemented only in v3. The
authoritative `/root/pva031_review` record identified above permits a
**prospective GUI03-only** release for separately scoped C18 publication; it
does not itself admit a publication command/result, release R, authorize
observer/closer retries, prove global idle, or discharge older holds or the C17
external-terminal qualification. It explicitly prohibits original T config
mutation.

The new fixed expected config SHA256 is
`036c10a0cc4303fa7de6578390ad7c8094d2a64796f8bb6a6b63cc7954f662d5`,
with all eight original pin fields:

| Field | Exact value |
| --- | --- |
| dev | 24 |
| ino | 14293053 |
| uid | 0 |
| mode | 33152 |
| nlink | 1 |
| bytes / stat size | 435 |
| mtime_ns | 1789001766988319322 |
| ctime_ns | 1789001766988319322 |

Lines 197-203 use the existing unchanged bounded, no-follow, single-link,
stable-identity `read_file(gd/'config')`, after original T identity/inventory
checks and under the original coordination lock. Both the SHA256 and full pin
must match the fixed released values **before the first Git command** at line
204. There is no new config schema, configurable relaxation, reader framework,
config write, redirect, or helper invocation.

Lines 255-258 perform a second stable read after the final Git/remote check and
post-publication inventory/resource checks, still under the same lock. Exact
raw bytes **and all eight pin fields** must equal the pre-read before line 259
can claim success. The original bytes are retained only in memory for this
comparison. The two receipt observations contain SHA256 and pin metadata only,
mapping stat `size` to the release's `bytes` key; no raw config contents are
persisted there. Atime is outside the authoritative eight-field pin, not an
omitted released condition.

Receipt observations are recorded before their respective equality assertions,
so a populated observation field by itself is not a passed preservation gate.
Failed assertions retain the default failure/HOLD outcome. Earlier failure,
cancellation, deadline, or hard loss can prevent the final observation; that
does not authorize an inferred after-check or config repair. A post-push failure
still leaves any actual side effects for root to reconcile under HOLD/no retry,
not for an automatic reset/replay. These are two stable observations, not a
continuous or hostile-host proof.

This review read only the retained release **record**, never T or its config.
It certifies the exact new source guard, not that the live pin currently matches
or that either prospective read has occurred. V2 lacks this required gate and
must not be promoted.

## Inherited protections retained, not newly certified by execution

Independent exact-text span comparisons found no other changes to:

- The utility/read/freeze, Git wrapper/environment, resource, inventory, and
  identity functions, except the C18 whitespace-log filename.
- The slot/original-lock binding, T identity/no-sharing/base/branch/index/
  unstaged-and-untracked gates, exact push-URL check, initial remote snapshot,
  source copy, and source/destination re-freeze block, after excluding only the
  exact seven-line config-precondition insertion described above.
- The whitespace result record, final source freeze, precommit remote snapshot,
  commit, sole-parent/complete-tree checks, prepush snapshot, single push,
  postpush snapshot, and post-publication inventory/resource checks, after
  excluding only the exact four-line config-postcondition insertion above.
- The exception/finalization block, except the C18 success-status literal.

The retained Git prefix disables hooks, fsmonitor, automatic maintenance, and
commit signing, and filters ambient Git configuration variables. Actual recipe
Git calls target T; the delta introduces no Git command in W, original-store
object copying, or Git-config editing. The `.git` descriptor exclusion above is
an additional selected-path guard, not a claim that C17 already had it.

T is still required to be the originally identified dedicated clone without
commondir/alternates sharing. The expected base index is complete, and staged
and committed trees are checked in full rather than only selected names. The
unchanged unstaged/untracked commands retain their exact inherited scope
(including `--exclude-standard` on the untracked query).

The only push remains one nonforce, unsigned, no-follow-tags,
no-recursive-submodules push of `HEAD` to
`refs/heads/codex/audit-continuation-linux-20260908`. Protected-ref expectations
come from the future root-bound `remotes` mapping; that mapping must include the
required protected refs as well as the dedicated branch. The before/precommit/
prepush/after comparisons are retained, not a promise about unlisted refs or
future external changes. No protected-ref reset, force push, old-history repair,
or external workflow dispatch is introduced.

### Nonactivation qualification

The reviewed audit push-trigger leaves are `ios-focused-01.json`,
`macos-focused-02.json`, `macos-native-01.json`, and `windows-cohort-05.json`;
none equals the five allowed Linux leaves. Workflow files remain unchanged and
unselected under root's final-payload restriction. Other reviewed push triggers
target main/testing/release rather than this dedicated branch, or are
manual/reusable.

Generic `ci.yml` also has pull-request events for protected base branches.
Consequently an existing open PR could make a dedicated-branch push synchronize
a PR even when the request allowlist is safe. Root reports a fresh,
repository-specific read-only open-PR query for this head returned `[]`
(`toola3ff93`, exit 0), with the tool object to be retained in C18 root admission.
This reviewer did not perform that API query. The reported result is bounded
current external-state evidence, not a future guarantee; no PR creation is
authorized or planned. Final root non-CI/remote admission must retain that
qualification rather than treating filenames alone as proof of nonactivation.

### Normal and abnormal cleanup; external terminal evidence

Normal foreground Git direct children are waited/reaped and their pipes closed.
On abnormal handling, the existing wrapper kills only its captured direct child
and attempts a bounded wait. `start_new_session=True` does **not** turn that into
process-group cleanup or prove descendants settled. Descendant settlement is
`NOT_PROVEN`; timeout, hard loss, or unsettled state remains HOLD/no automatic
retry. This delta adds no signals, broad cleanup, deleted namespace, SDK/cache/
service operation, or original-lock replacement. T, source, and evidence are
retained; the original coordination descriptors are closed/unlocked as before.

The final durable status is still written **before** receipt-FD close, final
stdout, and the external terminal/tool boundary. Therefore a durable published
receipt alone cannot establish that later close/stdout/tool exit succeeded.
The historical C17 external-terminal gap is not repaired by C18 and must not be
manufactured as exit 0 or replayed. Actual C18 receipt, remote/tree result, and
external terminal settlement must be reconciled separately after an admitted
invocation; this text review supplies none of those execution outcomes.

## Holds before root could admit a new invocation

1. Promote only the exact accepted candidate bytes (or return a changed delta
   for review). Do not execute or import either author-lane candidate or the
   consumed C17 predecessor.
2. Create and freeze the real C18 config and complete selected/companion payload:
   exact retained C17 predecessor, no removals, at most 450 unique paths, final
   hashes/bytes/Git modes, protected-ref expectations, slot/original namespace
   bindings, new receipt identity, and the complete authored-whitespace list.
   This is a bound-config recipe, not an endorsement of arbitrary config paths
   or values.
3. Obtain the genuine final `/root/coverage_review` acceptance of that exact
   selection hash. Keep this report and the earlier integration-only acceptance
   distinct from final source-publication acceptance.
4. Independently reconcile actual consumed status for the exact five Linux
   request records; every other path in the canonical audit `requests/` subtree
   and every workflow path remains unselected and unchanged, with all `.github`
   paths excluded. Preserve unresolved/held review
   controls as inert evidence. Reconcile the final source/evidence freeze after
   root's accepted C18 integrations, rather than reusing earlier unapplied-source
   observations from this review lane.
5. Retain root's explicit prospective GUI03-only release and separate scoped C18
   authority, including the original config condition above; neither a matching
   pin nor this review supplies that authority. Retain final no-CI/external-state,
   resource, original-lock, and protected-store admission. Root alone owns any
   actual Git/publication work and any subsequent
   receipt/terminal/remote verification. STOP/NO-RETRY/CLOSED, held-runtime,
   original-store/lock, and publication restrictions remain in force.

**Result:** no unresolved defect found in the specified new C18 **v3** text
delta, including the material original-config preservation amendment. Accepted
only with the final binding/admission holds above. No final payload acceptance,
actual publication, or new successful execution/closure is claimed.
