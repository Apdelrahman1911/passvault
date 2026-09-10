# Windows cohort 04 — SDK-only original-handle read

Author: `/root/native`. **SOURCE ONLY; NOT EXECUTED OR ADMITTED.** Root agreed
this bounded SDK-reader correction, not another run, automatic retry or a cause
claim. Linux03 has next scheduling priority. Future04 needs its own exact
independent source acceptance and root's separate source/instance/coordination
admission. No request, probe-only workflow or activation is supplied here.

## Preserve03, including the operational failure and review limitation

Before editing, exact03 before-images were copied to the new
`windows-cohort-03-before/` directory as read-only, non-executable `.txt` evidence:

| Inert file | LF / bytes | SHA256 |
| --- | --- | --- |
| `windows_native_validation.py.txt` | 1076 / 60280 | `153d20b008896810b1929be2f4b2d97ef7d8699a46989e05a5dd9ac65982f7e5` |
| `audit-windows-native-validation.yml.txt` | 54 / 2177 | `a547a24a547f8bef8d983212741556528f8d424b14fb14f8568a8bc31560a088` |
| `WINDOWS-COHORT-03-SCOPE.md.txt` | 122 / 7710 | `9f20d0e2b933590dc54144ab989558465b7456855bec0e7a6cdf6119e88d9ca3` |

All hashes/lengths matched before edits. GNU `cp --no-clobber` emitted portability
warnings, not copy failures. Existing03 request/scope/reviews/results, earlier
02 before-images and all predecessor outcomes stay unchanged and non-replayable.

Run34391056257 remains **operational FAIL;14UNSTARTED/0XML**. Source parent
`80766c8c4b42e2e42c45527f62aa86f6ed0c1225`, activation
`b2e826fd534be8fefede169d28e30c22e37530a9`. Four Git parents returned0, including
one-parent/A-only/clean-checkout checks. The first immediate Job1 sample was
followed by read-only natural-zero observation; no member identity/cause follows.
Then `Frozen input changed during read` stopped progress before command05,
toolchain.json, configure, product compilation or any CTest. Final12 checkout
captures matched; Job0/no termination and10entries+root removal support only
the independently reviewed owned-cleanup/scheduling outcome.

`../native-independent/WINDOWS-COHORT-03-ACTUAL-REVIEW.json`, SHA256
`5bef23de89a4fd804daea7e83501c7c71ee96689552ea97c5fbe0d4acfc8f5fc`, reconciles
that outcome. Source order localizes the failing call to `frozen_bytes(sdk_header)`
(exact03 line920; tuple guard99), but no failing path, tuples, SDK byte digest
or traceback was retained. Actual mutation, timestamp differences, stat API
semantics, caching, filesystem or installer explanations remain **UNPROVED**.
Prior author/reviewer source acceptance did not establish SDK cross-interface
metadata compatibility or capture that failing comparison; this limitation and
the actual failure are not retroactively erased by the correction.

## Narrow runtime delta — no timestamp waiver

Only the SDK-header reader changes; the existing `frozen_bytes` implementation
and all bootstrap/source/generated-file callers remain untouched. Main still
selects the installed SDK10.0.26100.0 `um/webauthn.h`, demands the same API8/PRF
declarations and records toolchain evidence. The new `Run.read_sdk_header`:

1. Uses existing parent-component no-reparse checks, then opens the leaf once
   through `CreateFileW`: `GENERIC_READ | FILE_READ_ATTRIBUTES`, **FILE_SHARE_READ
   only** (no write/delete sharing), `OPEN_EXISTING`, `FILE_FLAG_OPEN_REPARSE_POINT`,
   no inherited handle. Open failure has no fallback or retry.
2. Uses the existing native `FILE_INFO`/`GetFileInformationByHandle` primitive
   for a single consistent before/after tuple: volume/file index, attributes,
   link count, byte size, creation FILETIME and last-write FILETIME. No SDK
   `Path.lstat`/`os.fstat` tuple equality is assumed. Last-access time is not a
   stability field, as in the old reader; creation/write fields are not waived.
3. Rejects non-single-linked, reparse/directory or over1MiB input before reading.
   A directory that cannot be opened as a file fails at the open stage. No
   reparse target, SDK link tag, private file or arbitrary inventory is read.
4. Makes one bounded synchronous `ReadFile` on **that same original handle**,
   with a buffer of observed size+1 (at most1MiB+1). Requires successful read,
   exact original byte count and equality of the complete native tuple before
   accepting the bytes. Existing command/global deadline and sticky cancellation
   checks remain; there is no additional time budget or read retry.
5. Retains compact public SDK path/stage, available native before/after states,
   read status/count and SHA256 of the returned bytes **before rejection** in
   the journal and an in-memory record included in final result.json. Missing
   states/unread bytes remain null, not invented evidence. A failed/partial read
   hash is not an accepted full-file identity; `read_validated` describes only
   the byte/state gates, never handle cleanup or application correctness.
6. Attempts original-handle close once in `finally`, using existing close-failure
   accounting. A failure records the SDK close outcome and forbids generated-root
   cleanup through the existing HOLD gate; no close retry occurs. An earlier
   SDK failure remains in the record even if a close failure also propagates.

The toolchain's header hash now comes from the **same returned bytes** used for
declaration checks, not a second path-based `digest` read. No new dependency,
SDK selection, mutation exemption, source/oracle change or general reader
framework is introduced. This design removes an unproved cross-interface
comparison and denies concurrent write/delete opens; it does **not** establish
what happened to the03 SDK metadata. A share conflict, native mismatch, short
read or failed diagnostic/close stays failure, not a silent fallback.

This is still the cooperative hosted-namespace contract. Parent checks/open can
race hostile same-user mutation; the new reader is not a filesystem sandbox.
Synchronous OS calls/hard termination can interrupt deadline handling, final
records or upload. The retained SDK snapshot is not proof that every later
compiler input or executing image remained identical after its handle closed.

## Fresh04 bindings; all other scope reused

Only administrative04 substitutions accompany the SDK delta:

| Field | Required04 value |
| --- | --- |
| Request | `docs/audit-continuation/2026-09-08-linux/requests/windows-cohort-04.json` |
| Suite | `windows-native-14-cohort-v4` |
| Scope | `docs/audit-continuation/2026-09-08-linux/reviews/native/WINDOWS-COHORT-04-SCOPE.md` |
| Exact independent review | `docs/audit-continuation/2026-09-08-linux/reviews/native-independent/WINDOWS-COHORT-04-ADMISSION.json` |
| Reviewer/disposition | `/root/native_review` / `ACCEPT_WINDOWS_NATIVE_14_COHORT_04_ADMISSION` |
| Generated/evidence names | `passvault-windows-cohort-04-<run_id>-1-<nonce>` / `passvault-windows-cohort-04-<run_id>-1-evidence` |
| Artifact | `audit-windows-cohort-04-<run_id>-1` |

Schema1, owner/root exclusive-slot attestation, fresh nonce, exact source/tree,
helper/workflow/scope/review hashes, SDK10.0.26100.0, max_seconds480 and ordered14
case names remain required. Dedicated branch/repository/push/attempt1 guards,
one-parent source identity and **A-only** fresh-request activation are unchanged.
No ordinary source/report push activates it; no old request/admission is reused.

Except for the SDK-only reader and04 identity substitutions, reuse the exact03
scope (hash above), its inherited02 contract and
`../native-independent/NATIVE-TEST-SCOPE.md`. Same22 parent commands/same14
synthetic credential/Hello-excluded CTests, including real-CNG HMAC/AES controls;
same native sources, flags, test oracles, Win32/array/ABI scope and platform need.
No PVU-008 experiment or additional provider path is added. Parent exits, bounded
read-only Job drain, compiler cohort, final single termination/observed zero,
unscored raw XML, final hashes and original-handle cleanup stay unchanged.

Keep12GiB/25% launch and8GiB/20% running floors, one worker,16-process/3GiB child
Job caps,480s commands/540s cleanup/10-minute job, bounded logs/XML/JSON,
short3-day compact artifacts, exact pinned actions and minimal permissions.
No Gradle, packaging, app/device launch, private data, signing, store operation,
protected-ref/tag/version/identity/dependency change or replacement1017001.
Hardware/provider, historical-control, production-cut and fixed-vector gaps,
independent actual-result review, PVD choices and all PVU-007 STOP/PVU-011 NO
RETRY/PVA-029 FAIL/no automatic retry/G7-G8 CLOSED restrictions remain binding.
