# PVA-038 source freeze V2 — independent test-oracle correction

Author `/root/editor`; independent challenger `/root/storage`; 2026-09-09 UTC.
**Source-only revision; final independent patch review remains pending. No test
was run, failed, skipped, passed or compiled in this turn.**

Use this correction with the unchanged V1 contract, provider admission design,
limitations and source bindings. V1 is retained as rejected/pending historical
source evidence, not silently rewritten:

- `PVA-038-PATCH-SOURCE-V1.md` SHA-256
  `e51061a3899af459d76d08ee392d383d543f6fa8eb928ede526afef8f6ef28ac`.
- `PVA-038-PATCH-SOURCE-V1.json` SHA-256
  `7153498eb2dba7586fbfe9fcb8da7dfe099286bd8c04c04f3f7ba69915f28787`.
- **Exact rejected test bytes**, copied without overwrite before revision:
  `PVA-038-ROUTING-REJECTED-V1.kt.txt`, SHA-256
  `22a47793df143dcd9db299e04f2a83451e97011886b64da9b5245770fc78a017`,
  13,654 bytes /336 LF. This is inert source evidence, not another runnable test.

## Independent challenge T01 (test source, not a new application family)

Reviewer observed that the malformed-metadata test replaces the nonce **before**
calling `service.unlock()`. The service first calls `repository.getMetadata()`;
that method validates metadata. Consequently the invalid nonce produces a
generic failure and no vault ID, before `keyStore.retrieve` or the repository's
`unlockWithBiometricKey` transition. The test incorrectly called an assertion
helper expecting `Locked` and a non-null returned key to wipe. A newly created
repository instead remains `Uninitialized`, with no returned/candidate key.

Author independently reread and challenged this diagnosis against:

- repository `getMetadata`477–489 and generic `operationResult`665–673;
- service unlock95–117, including nullable vault-ID routing before retrieval;
- repository initial state57–65;
- the exact V1 test schedule136–147.

The diagnosis is correct. V1's test and its table claim of an OS-returned-key wipe
on that schedule are withdrawn. This is a source-discovered incorrect oracle,
**not an executed test failure**, a product defect, or any additional denominator.

## Bounded correction

The same nine-declaration inventory is retained: the malformed-metadata case
now checks the actual **pre-release** boundary. It snapshots prior session state
and requires it unchanged, no published `Unlocked` state, no unlocked repository,
enrollment retained, zero deletion/retrieval/verifier calls, and null returned
and verifier-candidate keys. A retrieval counter was added to the synthetic
store. It does not manufacture a post-release corruption schedule.

The invalid `expectCandidate=false` branch was removed from the general
post-release wipe assertion. All tests that call that helper must now observe
both independent key buffers and their wipes. The provider fixture uses the same
store with the new inert counter but is otherwise byte-for-byte unchanged.

### Current three after-images

| Source | SHA-256 | Bytes / LF |
| --- | --- | --- |
| `VaultRepositoryImpl.kt` (unchanged from V1) | `aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630` | 37053 /846 |
| `BiometricVerificationFailureIntegrationTest.kt` (V2) | `79e9d892d5aa0711facce39968ce6ce8d3a1a9dcefe47853af70e62e14168b10` | 14243 /348 |
| `BiometricProviderInitializationFaultIntegrationTest.kt` (unchanged from V1) | `25a05900371704e67c96b3940869a8529490e0e2ca2d927c1267311418e4f0d0` | 16180 /356 |

All paths are the exact production/new-test paths listed in V1. No service,
crypto provider, dependency, workflow, frozen105 selection or other agent's
source was edited. Root remains sole build/test/publication owner.

The current two test files total30,423 bytes. The rejected13,654-byte before-image
is necessary compact permanent review evidence; no build/fixture/cache/native
output or process was created. New declarations still total13:9 routing/real
control cases plus1 producer and3 prospective fresh-provider consumers. All
execution counts, coverage credit and qualified closure increments remain zero.

Actual-provider, compiler/Detekt, binary/dependency equivalence, execution,
coordination, cleanup, native-iOS and hardware admissions/gaps are unchanged from
V1. Default assumptions would be SKIP, not PASS; they have not been executed.

Accounting stays **19/25 original confirmed closures (76%),6 open**;
**22/38 all confirmed closures (57.9%),16 open**; **2/12 original suspicions
resolved,10 open**;8 PVD explanations/owner decisions separate. All STOP, NO-RETRY,
CLOSED, Windows01 cleanup-HOLD, PVA-029, protected-ref and publication restrictions
remain. PVA-038 is not qualified closed.
