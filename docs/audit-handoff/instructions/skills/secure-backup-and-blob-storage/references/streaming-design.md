# Streaming backup and encrypted blob design

## Container records

Use a bounded binary or streaming format with explicit types:

```text
Header(format, algorithms, salt, bounded KDF parameters, vault identity)
MetadataRecord(index, type, plaintextLength, ciphertextLength, ciphertext, tag)
BlobStart(index, attachment identity, owner identity, metadata, total length)
BlobChunk(index, chunk index, plaintext length, ciphertext, tag)
BlobEnd(index, chunk count, total plaintext length, transcript authenticator)
Final(record counts, byte totals, transcript authenticator)
```

Include format, record type, record index, owner/object identity, declared lengths, and critical metadata in AEAD associated data. Reject reordering, duplication, gaps, overflow, unknown critical records, trailing bytes, and totals that disagree.

## Bounds

Define and test independently:

- container and record byte sizes;
- decrypted metadata record size;
- credential/folder/tag/history/OTP/custom-field counts;
- filename and MIME metadata lengths;
- attachment count, per-object size, aggregate size, and chunk size;
- KDF cost bounds and parser nesting/depth.

Enforce a bound before allocating or deriving keys. Import and export limits may differ, but both must be explicit.

## Two-pass restore

When the input can be reopened:

1. First pass authenticates and validates every record and computes an authenticated transcript.
2. Stage encrypted blob objects under random app-private names.
3. Reopen and replay metadata into a database transaction.
4. Require the second transcript to equal the first before commit.
5. Commit rows/references, then retire superseded objects.

For non-rewindable providers, stage the encrypted container under a bounded app-private file, verify it, and restore from that file. Never expose partially restored live state.

## Attachment authority

Use Room/SQLite for metadata and relationships, not large blob bytes. The encrypted object filename is a random internal identifier, never the user filename. Bind object ID and credential/owner ID cryptographically so swapping two valid encrypted files fails authentication.

Coordinate filesystem and database using explicit staged/committed states plus startup orphan recovery. Database rollback cannot undo an external file write.

## Plaintext handling

Prefer streaming directly from a selected provider into encryption and from decryption into a user-authorized destination/viewer. If a platform requires plaintext temporary files, use a private non-symlink directory, random names, restrictive permissions, bounded lifetime, independent cleanup, and a documented best-effort deletion limitation.

## Peak-memory evidence

Measure each phase separately. Include input buffers, parser/JSON trees, Base64 expansion, ciphertext/plaintext coexistence, KDF memory, validation DTOs, database batches, attachment chunks, and platform-provider copies. A 100 MB limit can still create several hundred MB of peak memory in an in-memory design.
