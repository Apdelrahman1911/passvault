# PassVault encrypted backup format

Last reviewed: 2026-09-03

The `.pvault` extension is used for both supported encrypted containers. New exports use binary **format 2**.
Strict read compatibility with legacy JSON **format 1** is retained.

## Format 2: bounded record stream

### Header

Every format-2 file starts with this 44-byte binary header:

| Field | Bytes | Accepted value |
|---|---:|---|
| magic | 8 | `50 56 42 41 43 4b 02 00` |
| format version | 4 | `2` |
| Argon2id operations | 4 | 3–4 |
| Argon2id memory bytes | 4 | exactly 64 MiB |
| parallelism | 4 | `1` |
| attachment/record chunk bytes | 4 | 256 KiB |
| random salt | 16 | exactly 16 bytes |

The backup key hierarchy is separate from the vault master-password hierarchy: a fresh salt and the supplied backup
passphrase derive a distinct key. New exports reject a passphrase that exactly matches the current master password;
users must still choose a distinct, unpredictable passphrase rather than a trivial variation. PassVault verifies this
without retaining or exposing the master password by unwrapping a candidate VEK and comparing it with the active VEK
in constant time. Existing backups remain readable even if their passphrase matched the master password.

PassVault strictly UTF-8 encodes the backup passphrase and then uses the lowercase ASCII hexadecimal bytes as the
compatibility-critical Argon2id input; changing to raw UTF-8 would make existing backups unreadable. Mutable UTF-8 and
hexadecimal buffers are cleared best-effort after deriving the 32-byte backup key. The device benchmark selects one
of the two profiles format 2 has emitted: 64 MiB with either three or four operations. Writers and readers reject
every other profile before deriving a key; future KDF profiles require a new backup format version. The serialized
parallelism value is fixed at `1`: the current libsodium `crypto_pwhash` binding exposes no lanes argument, so writers
must not claim a parallelism value they cannot apply. Readers reject any other value.

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
layers: its per-attachment vault-key container and the backup-password record layer. During export, PassVault compares
SHA-256 fingerprints of the exact inner-container bytes it authenticated and packaged, aborting if an object changes
between those streaming reads.

The current metadata schema authenticates the aggregate encrypted-object byte total in the manifest. Restore requires
free space for the remaining objects plus one maximum-size object of reserve when the platform can report capacity,
and repeats the check before each object write. Older format-2 metadata schemas did not carry the aggregate; they
remain readable and receive the per-object running reserve check as each authenticated start record is decoded.

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
validated whole-snapshot Room transaction for compatibility; after it commits, object storage is reconciled against
the format's guaranteed empty attachment set. New exports do not use format 1.

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
