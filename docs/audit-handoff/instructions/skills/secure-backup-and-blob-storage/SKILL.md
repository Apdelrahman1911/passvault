---
name: secure-backup-and-blob-storage
description: Design, implement, diagnose, or audit encrypted backup, import, export, restore, attachment, and large-blob storage across Android, iOS, and Desktop. Use when code reads whole backups into memory, adds attachments, changes formats or limits, handles user-selected files, stages restores, or must prove bounded memory, authenticated encryption, atomicity, corruption detection, cancellation safety, and legacy compatibility.
---

# Secure Backup and Blob Storage

Bound memory and filesystem authority explicitly. Treat backup streams, filenames, providers, and restored metadata as untrusted input.

Load `$database-migration-integrity` whenever blob metadata, relationships, format markers, or restore staging change the application schema.

## Stable invariants

1. Define exact byte, record, count, field, attachment, and aggregate limits in code and tests.
2. Enforce lengths before allocating or running expensive cryptography.
3. Stream large content through bounded authenticated records; do not materialize whole-vault plaintext/ciphertext/Base64 copies.
4. Store large blobs outside the database as independently encrypted app-private objects.
5. Bind object identity, owner, format, metadata, chunk index/length, and final totals cryptographically.
6. Expose restored state only after complete authentication and transactional validation.
7. Abort cancellation/failure without live partial rows, objects, or retained plaintext staging files.
8. Version formats explicitly and preserve deliberate legacy read compatibility.

## Inspection

Search for whole-file reads, `readBytes`, aggregate JSON DTOs, Base64, repeated `ByteArray` copies, database BLOBs, plaintext temp files, unbounded collection decoding, filename-derived paths, MIME/extension trust, symlink following, and delete/write transactions spanning database and filesystem.

Inventory import and export bounds separately. Model peak amplification for reading, decoding, KDF, decrypting, validation, database insertion, encrypted object staging, and external-provider copies. Run `scripts/estimate_storage.py` with measured record assumptions; do not substitute its model for profiling.

## Remediation architecture

### Backup container

- Use a versioned header with bounded KDF parameters and fresh salt.
- Encode ordered authenticated records with monotonic indexes and explicit lengths.
- Bind header, type, index, length, and final totals through AEAD associated data/final records.
- Query/encode metadata by row or bounded page.
- Carry already-encrypted attachment objects through a bounded outer record layer.

### Restore

- Inspect/authenticate without mutating live state.
- Stage new encrypted objects under random app-private names.
- Re-open or rewind and replay metadata inside one database transaction.
- Bind passes with an authenticated transcript so mutable providers cannot switch content.
- Commit database authority before removing superseded objects; clean staged/orphaned objects deterministically.

### Attachments

- Detect MIME from bounded content, not extension alone.
- Validate filename length, Unicode controls, separators, path components, declared size, count, and aggregate size.
- Reject symlinks/reparse points and traversal.
- Encrypt each object independently with per-object context/key material and authenticated final totals.
- Use atomic create/replace and explicit operation states for crash recovery.
- Coordinate credential deletion, attachment deletion, and failed imports so neither rows nor live objects orphan silently.

## Verification

Test exact limits and one-byte/one-item over; fragmented/unknown-size reads; 1,000–50,000 representative records; realistic and conservative growth; wrong password; tamper, reorder, duplication, truncation, swap, and source changes; filesystem/provider failures; cancellation at every phase; concurrent duplicate operations; migration from every supported format; transaction rollback; orphan recovery; and cleanup after restart.

Measure actual peak memory on each platform. Report amplification, not only final file size. Document where platform providers cannot guarantee atomic replacement while preserving internal atomicity.

## Failure lessons

- Backup file size is not peak memory.
- Attachments can make a previously acceptable in-memory architecture unsafe.
- Authenticated encryption does not prevent path traversal or row/file orphaning.
- Database transactions cannot atomically cover external files or OS key stores; design staged authority and compensation.
- A user-selected provider may leave a partial external destination even when the application correctly aborts.

## Resources

- Read [`references/streaming-design.md`](references/streaming-design.md) before designing a format or attachment store.
- Run [`scripts/estimate_storage.py`](scripts/estimate_storage.py) for transparent typical/conservative capacity models.
