# PVU-008: bounded content-contract search remains inconclusive

Author `/root/native`, 2026-09-09. **UNRESOLVED / VERIFICATION BLOCKED.**
No new confirmed finding, patch, execution or closure. Independent challenge
requested from `/root/native_review`; this author note is not its own acceptance.

## Existing facts reused and current surrounding paths

The handoff's `current/unresolved-investigations-outcome-only.json` already
rejects **freed-before-destroy**: backing vectors remain allocated when
`HashHandle`/`KeyHandle` destructors call CNG. That rejection remains intact;
allocation-lifetime documentation is not a content-lifetime contract.

Current source: `app-desktop/native/biometric-bridge/src/windows/passvault_biometric_windows.cpp`,
2571 LF / 97737 bytes, SHA256
`8ae2d6294ca523c763bf055bb3acee755b63e0867f7fb98e3926980c74fe6630`.
Read range49–384 SHA256 (inclusive LF bytes)
`ef595373cb64756b2fa324fddbceacc497552b551905351d21fa35ef4d76916a`;
the full-file hash is an identity, not full-file review credit.

- Guards reject invalid inputs/provider-open/object-length failures before the
  caller-owned object is created. They do not remove the reachable later wipe.
- SHA256/HMAC and AES-GCM encrypt/decrypt explicitly zero object **contents**
  before returning after ordinary success or downstream CNG error. The handle
  object's destructor runs afterward, before vector deallocation/provider close;
  its BCryptDestroyHash/Key call is conditional on the stored handle being non-null.
- Destroy statuses remain ignored (HashHandle123–139, KeyHandle141–157);
  helper booleans are computed earlier. A correct digest/ciphertext alone cannot
  establish destruction success, failure, leakage or harmlessness.
- AES encryption's `ciphertext->assign` at306 can throw before its explicit wipe,
  even when GenerateSymmetricKey returned failure. The key-handle object still
  unwinds before the vector, but no valid handle/destruction call is guaranteed
  on every failure. This is not the ordinary wipe-before-destroy schedule and
  supplies no provider-content erasure guarantee or new confirmed defect by itself.
- Provider selection is still `nullptr`; no exact Windows provider implementation
  is source-pinned. A future change must cover hash **and** key handles, ordinary
  errors and unwinding without double destruction or format/dependency changes.
  No such change is proposed or authorized by this outcome.

## Bounded public data reads, not provider experiments

Three anonymous HTTPS GETs, no redirects/retries/credentials, connect5s/total20s
each. JSON search caps128KiB; article cap256KiB. All three curl/inline-standard-
library data-reader pipelines returned0. No downloaded code or project/archived
helper was executed; no raw-response files/cache/archive were created. Search
metadata is discovery only; only its first ten results per query were inspected.

| UTC / source | Bytes | Raw response SHA256 |
| --- | ---: | --- |
| 11:18:01Z / [search1][s1] | 12244 | `a4cf2e10328ad41b954a1db9ee095c38276ae21fc84fcbee2ab18d6d61e6076a` |
| 11:19:48Z / [MSDN article][article] | 80386 | `c700a81f2695491ba4e3fca3dc906024708ee8abbb55939023f2b17baabc9aa9` |
| 11:21:09Z / [search2][s2] | 12258 | `004edf756d6d1ee501199f52858fee9c4412142673560ae944faf92e1f1192a8` |

The newly followed Microsoft-hosted article is Kenny Kerr's July2007 Windows
Vista CNG overview, **not exact current provider source**. Relevant text:

> Before you call it, however, you need to have allocated a buffer that the hash function will use for processing.

> With the exception of kernel mode, it does not matter too much where you allocate this buffer, and you are free to use whatever storage is most appropriate as long as it is stable (in other words, you cannot use an unpinned managed byte array).

These are normalized-main lines116 and135 (whitespace collapsed; tags removed).
Normalized whole-main SHA256:
`6ce9209959018749f58105b4e6daba6d81d6858e7e488369de1c727fe36822c8`.
The captured buffer/memory/destroy/modify/state excerpts' SHA256:
`765747f1926c78a1b1998baa720edc651fb27bbb55a840a774b913466bc4776a`.
Raw bodies/full extracted text are not retained; the exact relevant quotations
above and response identities are retained, not an exhaustive document archive.

The parenthetical explains **address/storage stability**, not an express
prohibition or permission to overwrite provider-private bytes before destruction.
The article's separate destroy-then-free examples add no missing content contract.
The already-reviewed CreateHash/GenerateSymmetricKey allocation documentation was
not refetched. Search results did not yield exact applicable Windows provider
implementation/source or an explicit pre-destroy content-mutation contract.

## Outcome and stop boundary

This bounded search **did not locate** the needed authoritative content contract;
it does not prove that none exists. Do not promote inference from an opaque object,
an example's ordering, allocation lifetime, Wine/ReactOS or successful crypto
output into exact Windows destruction behavior. No analogue was used.

The remaining gap is the existing one: an applicable authoritative content
contract/source or a separately admitted isolated Windows CNG-only investigation
with actual destroy statuses and resource consequences. None is launched here;
Windows01 remains consumed FAIL/filesystem HOLD, not a reusable fixture or retry.
Stop this search rather than speculate. All STOP/NO-RETRY/CLOSED, hardware,
publication and build1017001 boundaries remain unchanged. No count changes.

No task-owned runtime artifacts or background workers remain. Point11:22:40Z:
disk available20247360KiB, MemAvailable37874028KiB, swap0; no cleanup of another
lane's outputs was attempted.

[s1]: https://learn.microsoft.com/api/search?search=CNG%20object%20buffer%20contents%20BCryptDestroyHash%20BCryptDestroyKey&locale=en-us&%24top=10
[article]: https://learn.microsoft.com/en-us/archive/msdn-magazine/2007/july/applying-cryptography-using-the-cng-api-in-windows-vista
[s2]: https://learn.microsoft.com/api/search?search=%22BCryptDestroyHash%22%20%22overwrite%22%20%22buffer%22&locale=en-us&%24top=10
