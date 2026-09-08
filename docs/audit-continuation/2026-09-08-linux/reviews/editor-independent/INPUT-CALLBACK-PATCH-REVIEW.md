# PVA-007 property-only input correction — independent after-image review

Reviewer `/root/editor_review`; product/test author `/root/editor`.
2026-09-08. **SOURCE ACCEPTED; EXECUTION PENDING; NO CLOSURE OR RUN ADMISSION.**

## Exact reviewed successor

`INPUT-CALLBACK-PATCH-INPUTS.json` binds this successor to the containing commit
and raw on-disk LF source. It explicitly supersedes the four overlapping product
files and six-method test identity in `ROW-SAVE-PATCH-INPUTS.json`; earlier
intermediate evidence is retained, not relabelled current. Independent finding
confirmation is `INPUT-CALLBACK-FINDING.md` and its before-image manifest.

| Final reviewed file | SHA-256 |
| --- | --- |
| CredentialViewModel.kt | `b9a9c5e6a9e9a5eee86a13a639fa32826dbfdfbd09fa81a032935a37f58f0611` |
| CredentialEventRouter.kt | `b1f18f9e32beff113d0c6d26dc16fc3b9e24460ef6039a3dc144554815431f5a` |
| CredentialFormSupport.kt | `b3403f38c94808df3d9f9d227353fc0505bb0b3848d20a69da63689424a2a66c` |
| CustomFieldsEditor.kt | `dd60f4a65b4cf951a8ba94b2b6e4cab8440d1a33942eb046f1d74ccfcf9b31db` |
| CredentialCustomFieldSaveFreshnessTest.kt | `d492ed289c21904c6f1145580aaf2980bd99e9a2ae6ccaf624b03c3d4faef0af` |

The new test is now **532 physical LF / 26,117 bytes / 11 declared methods**.
Lengths/hashes are source identities, not review percentages or test results.

## Product challenge and compatibility

The actual name, plain/secret value and secret-toggle paths now emit their field
id plus **only the edited property**. Their callbacks no longer submit a
`draft.copy(...)` containing sibling properties captured by an older composition.
Both value-input branches use the same bounded property callback. The row's
stable field key/id, `enabled` checks, display state, Save and Cancel behavior
remain in place.

The router routes all three events to their corresponding editor operation and
classifies each as a custom-field edit mutation. Its existing busy guard therefore
rejects these events before changing the draft. Each accepted property transform
reads the latest map entry **inside** `state.update`, preserving its current
siblings on every CAS attempt. Missing/cancelled/removed/locked drafts remain
missing; the helper does not create a draft or mutate a stored sensitive owner.
The changed name/value retain the existing code-point limits, and an accepted
edit clears the existing validation error just as the old whole-draft path did.

Attempted counterexamples were checked rather than assumed away:

- A full tuple intentionally submitted via `OnCustomFieldDraftChanged` still
  replaces that tuple, with the original clamping and map-existence guard. The
  implementation does not guess which properties an explicit caller meant.
- `OnCustomFieldUpdated` remains the separately accepted explicit stored-field
  update API; the new property events do not convert it into a draft commit.
- A property event cannot discard another row's pending draft because the map
  update replaces only the entry for its own id.
- Same-property last supplied value wins, including returning the secret flag to
  false. This fixes sibling overwrites; it does not claim the framework delivers
  a current semantic Boolean for arbitrary rapid repeated toggles, or that an
  IME's still-unsubmitted buffer is already part of owned VM state.
- Page/row Save adopts the current draft after these merges. This does not alter
  persistence format, ownership boundaries, dependencies, identities, Store
  versions, PVD product decisions or native capabilities.

No blocking source defect was found in this bounded successor.

## Regression quality and limits

Five methods were appended to the six accepted row methods:

1. One method applies all **six permutations** of name/value/secret events to
   separately reset drafts before any scheduler/frame advancement. It checks the
   complete resulting tuple, the other pending row, and the untouched stored
   owner for every order. Only the **last iteration** then performs row Save and
   production page Save through the copying fake repository. These are six
   scenario iterations, **not six JUnit methods or six persistence roundtrips**.
2. Supplementary-Unicode limits and repeated updates preserve current sibling
   properties, including changing the secret flag back to false, followed by row
   adoption.
3. All property events with no active draft, after Cancel, after removal, and
   after lock are no-ops. The lock control also targets the still-present second
   row before clearing, rather than relying only on the already removed first
   row. Unknown-id behavior is checked through the shared helper's name event.
4. Each of the three properties is rejected while generation is busy with a
   genuine pending draft, then accepted after that busy operation settles. The
   stored sensitive owner remains unchanged during draft edits.
5. The whole-tuple compatibility event replaces all three previously changed
   properties, retains bounds, and can be cancelled back to clean stored state.

This reviewer independently reconstructed the previous file by concatenating
final raw lines 1–252 and 423–532. Its SHA-256 is exactly
`94131184ff94fec3f4c243385a61af3f731ed146dd681a7f7857e323a7b0f35c`, 16,670 bytes and
362 LF: the initial six methods and the previously reviewed cleanup fixture are
**byte-for-byte preserved**. This was an in-memory source-identity operation,
not test execution or a mutant run.

The new class plus the unchanged 13-method companion would select 24 declarations.
Together with both Back classes the bounded four-class selection has 42
declarations, **20 newly authored** (11 VM/event, seven registration-composition
and two coordinator controls) and 22 preexisting. **Zero new cases were executed
by this reviewer.** Do not infer passes, task counts, application behavior or an
increase to historical execution totals from these declarations.

The fixture still owns and bounded-joins the real VM scope, clears synthetic
copying repository ownership and restores Main with primary/cleanup failures
preserved. It is real VM/router/editor logic over fakes, not a rendered row,
delivered key/pointer/IME sequence, Room/native storage or an application reopen.
No compiler or before-fail/after-pass result exists. Forced concurrent CAS
interleavings were source-reviewed only. Source wiring is independently checked;
it does not substitute for rendered UI regressions.

## Back and row integration revalidation

`BACK-ROW-INPUT-INTEGRATION-REBIND.json` records the following independently
checked facts:

- All four Back product/test files retain their accepted hashes. Removing only
  the three new property-event declarations reconstructs the previously sealed
  row-stage VM hash. Its Back-relevant state, Save/lock/Cancel and dirty predicate
  have no other changes.
- Substituting only the expanded draft-input helper block with its unchanged
  before-image reconstructs the accepted row-stage form hash. Everything outside
  that block, including page Save and row commit/ownership CAS logic, is unchanged.
- New router/category entries preserve the busy guard, and the UI retains the
  id-only row Save and Cancel while sending the appropriate property-only event.
  The live Back policy continues to read the resulting current state.

Both earlier source acceptances are therefore retained for this integration,
with an explicit new source binding rather than a claim that the old whole-file
manifests still name final bytes. No runtime acceptance is manufactured.

PVA-007 remains open. Rendered editing, row/page Save/reopen/Cancel, lifecycle/IME,
native/predictive interaction and genuine hardware gaps remain separate.
Denominators stay **19/25 original confirmed families**, **22/37 all confirmed
families**, and **2/12 original suspicions** at this source-only checkpoint.
STOP/NO-RETRY/CLOSED, PVD and publication/build-1017001 restrictions are unchanged.

Only source reads, bounded metadata hashing and compact report writes ran here.
There was no build/test/native/application/old-runner invocation, daemon, emulator,
background worker or generated build/cache output to clean; wrapper stop is not
applicable. No source, permanent evidence, shared cache or unrelated process was
removed. Root remains sole execution/admission/central-ledger/publication owner.
At 21:48:20 UTC the filesystem had 29,170,184 KiB available and RAM had 43,075 MiB
available. The 17 necessary reviewer evidence files occupied 118,528 bytes
(164 KiB filesystem allocation) before this final resource note.
