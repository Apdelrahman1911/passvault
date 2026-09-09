# PVU-008 content lifetime: independent bounded challenge

Reviewer `/root/native_review`; author `/root/native`; 2026-09-09.
**ACCEPT the corrected bounded outcome: UNRESOLVED / VERIFICATION BLOCKED.**
No provider-content consequence, new confirmed defect, patch or closure follows.
The rejected freed-before-destroy allegation remains rejected.

## Identities and actual review scope

Paths below are relative to `docs/audit-continuation/2026-09-08-linux/` unless
they begin `app-desktop/` or `docs/audit-handoff/`.

| Input | LF / bytes | SHA-256 |
| --- | ---: | --- |
| `reviews/native/PVU008-CONTENT-CONTRACT-OUTCOME.md`, initial wording | 94 / 5987 | `6d1e913fd744e48a07eb9f72e850aa4964b31fda54d9cb8620f2a39a090a7386` |
| Same note, corrected wording | 96 / 6181 | `a99f342ac00a483cf40e488db832e9f2c5423f9ced4c02ec7d7205c22077866a` |
| `app-desktop/native/biometric-bridge/src/windows/passvault_biometric_windows.cpp` | 2571 / 97737 | `8ae2d6294ca523c763bf055bb3acee755b63e0867f7fb98e3926980c74fe6630` |

Both author-note revisions were read completely. The focused source49-384 LF
slice independently hashes to
`ef595373cb64756b2fa324fddbceacc497552b551905351d21fa35ef4d76916a` and was
read completely. Full-file hashing is identity evidence, not full-file audit
credit. Only bounded caller windows1099-1131/1751-1775/1945-1975/2180-2210 were
additionally read to confirm continued production use, not to re-audit Hello.
The preserved PVU-008 record in
`docs/audit-handoff/current/unresolved-investigations-outcome-only.json` was
reread narrowly (file SHA256
`420b7eb1a419aabec37cbc323bc7d9a9b32ca160395dcc2d5f109488820eed19`). Its
historical whole-file equality statements were not applied to today's source.

## Independent source traces and author correction

- Algorithm handles precede the object vectors; HashHandle/KeyHandle objects
  follow them. Reverse automatic destruction therefore unwinds the handle
  object before the vector and algorithm handle. `secure_wipe`55-68 changes
  bytes only: it does not resize/free/swap either provider-object vector.
  AES failure-path `ciphertext->clear()`327 concerns the separate output vector,
  not the provider-object allocation. Allocation-before-destroy rejection holds.
- Input/provider-open/object-length guards can return before allocation; they
  do not make the later schedule unreachable. SHA256/HMAC wipe at206/242 and
  AES-GCM encrypt/decrypt at324/376 after ordinary success or CNG-status failure.
  Failed create/generate skips later crypto calls, but still reaches the manual
  wipe if intervening C++ work returns normally. Empty hash input legitimately
  skips HashData, not FinishHash or the wipe.
- Helpers remain called from assertion/relying-party hashing and from wrapping-
  key derivation plus encrypt/decrypt in existing production paths. Those paths
  have surrounding validation/provider prerequisites; source reachability is
  not a claim that any such provider/device path was executed here.
- Native destroy calls at128/146 are guarded by a **non-null stored handle**;
  their NTSTATUS results are discarded. Helper booleans are computed before
  these destructors. Correct crypto output alone cannot prove destroy success,
  failure, retained resources or harmlessness.
- The initial note said AES assignment unwinding “still destroys the key.”
  Independent counterexample: GenerateSymmetricKey may have returned failure,
  and `ciphertext->assign`306 still runs and can throw. The handle object always
  unwinds before the vector, but a valid handle/native destroy call is not
  guaranteed on every error. The author agreed and corrected this distinction
  at25-26/30-34. No code changed; the initial wording/hash are preserved above.
  Assignment exceptions bypass the manual wipe, unlike normal status-return
  paths; this alone does not establish a new provider-erasure or leakage defect.

## First-party quotation and search limits

The retained Microsoft-hosted July2007 article quotation requires stable storage
and explains it with an **unpinned managed byte array** counterexample. That
parenthetical concerns relocation/address stability. It is not an express
permission or prohibition on clearing provider-private bytes before destruction.
Destroy-then-free examples and allocation-lifetime documentation likewise do not
supply the missing content contract. A historical Vista overview is not exact
current Windows provider implementation/source; provider selection here remains
`nullptr`, not a source-pinned implementation.

This review evaluates the exact quotations retained in the author note. It did
not refetch the article/allocation docs or independently reconstruct/hash raw
responses, which were not retained as files. The note preserves URLs, response
sizes/hashes and normalized-quotation provenance; those are not a full independent
document archive or proof of an exhaustive search. Two searches inspected only
their first ten results each, with one followed historical article—not twenty
unique fully reviewed documents or every applicable provider contract.

The justified conclusion is **this bounded search did not locate the needed
contract**, not that none exists. Neither opaque-object intuition, successful
vectors, Wine/ReactOS analogues nor the new article resolves exact Windows
destruction behavior. Author agreed the bounded interpretation. No repeated
search, provider experiment, compatibility redesign or speculative patch is
warranted by this outcome alone.

## Unchanged stop boundary

Remaining evidence is the existing applicable authoritative content contract/
source or a separately admitted isolated Windows CNG-only investigation of both
handle types, actual destroy statuses and resource consequences. No such fixture
or execution is admitted here. Windows01 remains consumed FAIL/filesystem HOLD;
neither it nor any old helper is reusable for this purpose. All STOP/NO-RETRY/
CLOSED, hardware, publication and build1017001 restrictions remain unchanged.

Only this compact permanent note was written. Zero provider/device/helper/CI/
test/network operations, patches, temporary runtime artifacts or workers were
created by this reviewer. All family/suspicion/closure counts remain unchanged.
