# Candidate pipeline contract

## Authority chain

Use this ordering:

```text
reviewed commit/tree
  -> candidate manifest
  -> immutable build artifacts
  -> signed artifacts and upload receipts
  -> store build IDs / frozen Desktop bundle
  -> protected promotion
  -> publication read-back
```

Every arrow needs a deterministic identifier or cryptographic digest. A branch, display version, tag, workflow name, or human description is not enough.

## Candidate manifest fields

Require:

| Field | Rule |
|---|---|
| `schemaVersion` | Reject unsupported versions. |
| `source.commit` | Full object ID. |
| `source.tree` | Full Git tree ID. |
| `version.marketing` | Equal across intended artifacts. |
| `version.build` | Unique under each Store's rules. |
| `artifacts[]` | Stable logical name, file name, byte size, SHA-256, platform, architecture. |
| `signing` | Public identity/fingerprint and policy; never private material. |
| `storeReceipts[]` | Store app ID, uploaded build ID, version/build, track/channel, workflow run. |
| `createdBy` | Workflow and run attempt. |
| `createdAt` | UTC timestamp for audit only, never authority. |

Canonicalize serialization before signing or hashing. Reject unknown critical fields rather than ignoring security-relevant changes.

## Protected rebases

An ancestry-only rule rejects a valid protected rebase. A tree-only rule accepts unrelated copied history. Accept either direct ancestry or complete-tree equality with a real merge base. Fetch enough history for the comparison; shallow clones can produce false negatives.

## Build-once rules

- Upload one iOS/Android build to a testing channel and retain its Store build ID.
- Promote that Store build through testing and production stages.
- Freeze Desktop release bytes after production signing/notarization validation.
- Publication downloads and re-verifies frozen bytes; it does not compile or re-sign.

If a platform requires technically unavoidable processing that changes bytes, record both the submitted and Store-processed identities and do not claim byte equality that the platform cannot provide.

## Environment boundaries

| Stage | Typical authority |
|---|---|
| Source/candidate checks | Read repository and CI metadata. |
| Testing upload | Testing environment credentials and approval. |
| External promotion | Testing promotion approval; no compiler. |
| Production signing | Production signing environment; no publication. |
| Production promotion/publication | Production environment approval and exact receipt binding. |

Use non-cancelling concurrency for a shared release lifecycle. A newer run must not cancel an older run after it has mutated a Store.

## Rerun and cleanup matrix

| State found on rerun | Action |
|---|---|
| No external build | Build/upload the declared candidate. |
| Matching build receipt | Resume from the next incomplete gate. |
| Same version but different hash/source | Fail; allocate a new candidate/build number. |
| Partial Desktop signing output | Delete only scoped transient output and restart signing. |
| Immutable tag/release exists with different manifest | Fail; never move or overwrite silently. |

Install cleanup traps before importing keys or creating temporary material. Validate cleanup through an injected failure, not only the happy path.
