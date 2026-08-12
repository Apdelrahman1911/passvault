# PassVault encrypted backup format

Last reviewed: 2026-08-11

The `.pvault` extension is used for both supported encrypted containers. New exports use binary **format 2**.
Strict read compatibility with legacy JSON **format 1** is retained.

## Format 2: bounded record stream

### Header

Every format-2 file starts with this 44-byte binary header:

| Field | Bytes | Accepted value |
|---|---:|---|
| magic | 8 | `50 56 42 41 43 4b 02 00` |
| format version | 4 | `2` |
| Argon2id operations | 4 | 2–10 |
| Argon2id memory bytes | 4 | 32–256 MiB |
| parallelism | 4 | `1` |
| attachment/record chunk bytes | 4 | 256 KiB |
| random salt | 16 | exactly 16 bytes |

The backup password is independent of the vault master password. Argon2id derives one 32-byte backup key from the
password and fresh salt. The device benchmark selects parameters and the writer clamps them to the accepted range.

### Authenticated records

The header is followed by ordered XChaCha20-Poly1305 records. Each record contains:

```text
type (1) | monotonically increasing index (8) | plaintext length (4)
nonce (24) | ciphertext length (4) | CryptoEnvelope-v2 ciphertext + tag
```

The fixed outer overhead is 61 bytes per record. Associated data binds the complete format-2 header, domain
`passvault:backup-record:v2`, record type, index, and plaintext length. Removing, duplicating, reordering, changing,
or moving a record therefore fails authentication.

Metadata records occur in this exact order:

1. manifest/counts;
2. one vault-metadata row;
3. folder rows;
4. tag rows;
5. credential rows;
6. canonical credential/folder references;
7. credential/tag references;
8. attachment metadata rows;
9. password-history rows; and
10. a zero-length metadata-end marker.

Each Room value uses a compact length-prefixed binary codec. Encrypted payloads remain encrypted; format 2 does not
materialize decrypted credential fields. Large-value tables are queried one row at a time, small folder/tag pages
contain at most 64 rows, and relationship pages at most 512 rows. There is no whole-vault DTO, JSON string, Base64
copy, aggregate plaintext buffer, or aggregate ciphertext buffer.

Managed attachments follow the metadata section in attachment-ID order. An attachment start record declares the
encrypted object size, 256 KiB content records carry the already encrypted object, and an attachment-end record
authenticates its ID, total bytes, and chunk count. A final record authenticates the total record count, managed
attachment count, and total encrypted-object bytes. Attachment content therefore has two independent authenticated
layers: its per-attachment vault-key container and the backup-password record layer.

## Validation and two-pass restore

Inspection performs one pass and writes nothing. Restore deliberately performs two passes over the same selected
object:

1. authenticate and validate every record and relationship; stream each attachment object to a new app-private
   atomic staging path; validate the final record and exact EOF;
2. rewind/reopen the platform selection, authenticate the header/manifest again, then replay only metadata rows
   inside one immediate Room transaction.

The first and second metadata passes must have the same SHA-256 transcript. The transcript commits the authenticated
header and each metadata record's type, index, length, nonce, and Poly1305 tag. A file/provider that changes between
passes cannot mix first-pass attachment objects with second-pass metadata. Transcript mismatch or any parsing,
constraint, cancellation, or insertion failure rolls back Room and deletes all staged objects.

Before Room replacement, restore locks the current vault and removes its external biometric enrollment. If Room
rolls back after that deletion, PassVault makes one non-cancellable best-effort attempt to re-enroll the prior VEK;
the old database remains intact. After Room commits, new attachment paths become authoritative and unreferenced old
encrypted objects are removed. The restored vault remains locked and requires the restored vault's master password.

See [Backup capacity and memory](BACKUP_CAPACITY.md) for exact bounds, amplification, and growth estimates.

## Format 1 compatibility

Legacy format 1 is a UTF-8 JSON envelope with a Base64 `CryptoEnvelope` ciphertext, Argon2id parameters, 16-byte
salt, and 24-byte nonce. Its XChaCha20-Poly1305 associated data is `passvault:backup:v1`. The decrypted strict JSON
snapshot contains raw encrypted Room rows.

Format 1 has a 128 MiB container limit and a 64 MiB decrypted-snapshot limit. It predates attachment-file support:
accepted version-1 backups must declare `attachmentsIncluded = false`, contain no attachment rows, and may report an
omitted attachment count. Preview/restore warns about those omitted legacy files. Format-1 restore remains one
validated whole-snapshot Room transaction for compatibility; new exports do not use it.

## Platform file semantics

- **Android:** the Storage Access Framework owns user-facing locations. Export is fully generated in the app's
  `noBackupFilesDir` before copying to the selected URI. Providers do not offer a universal atomic-replace primitive,
  so a provider/output failure can leave a partial user-selected destination even though PassVault aborts and removes
  its private staging file.
- **Desktop:** input must be a regular non-symlink file. Export writes a temporary sibling and uses atomic replacement
  where the filesystem supports it, with a replace fallback when it does not.
- **iOS:** imported selections are read from the app-owned picker copy and exports use an app-controlled temporary
  object before the document/share presentation.

All sources enforce the common byte counter even when the platform cannot declare size in advance. Format-2 restore
can close and reopen/rewind each platform source; the cryptographic transcript, not mutable filesystem metadata, is
the authority for two-pass identity.

## Compatibility policy

The implementation accepts backup formats 1 and 2, vault formats 1–2, and crypto format 2. A future format must use
a new magic/version and add explicit compatibility, wrong-password, tamper, truncation, transaction, cancellation,
source-change, and boundary tests. There is no CSV/plaintext export, cloud upload, or merge restore.
