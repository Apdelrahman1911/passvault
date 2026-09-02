# Backup capacity and memory

Last reviewed: 2026-09-03

This document derives limits from `BackupLimits`, `BackupEntityBinaryCodec`, repository validation, and
`AttachmentPolicy`. MiB/GiB use binary units.

## Exact accepted limits

| Boundary | Format 2 (new export/import) | Legacy format 1 import |
|---|---:|---:|
| complete encrypted backup | 16 GiB | 128 MiB |
| aggregate decrypted metadata snapshot | none; row streamed | 64 MiB |
| largest independently materialized metadata record | 33,688,811 bytes (~32.128 MiB) | entire snapshot, up to 64 MiB |
| credential summary encrypted payload | 128 KiB | 32 MiB |
| credential secret encrypted payload | 32 MiB | 32 MiB |
| folder/tag encrypted payload | 64 KiB each | 64 KiB each |
| attachment filename/password-history encrypted payload | 128 KiB each | 32 MiB each |
| attachment outer backup chunk | 256 KiB | not supported |
| attachment outer content records | 65,536 per object | not supported |
| attachment plaintext file | 100 MiB | not supported |
| attachment encrypted object at the 100 MiB boundary | 104,882,093 bytes | not supported |
| managed attachment plaintext per credential | 512 MiB | not supported |
| retained validator identifier occurrences | 1,000,000 across all row types | not applicable |
| retained validator identifier UTF-8 bytes | 64 MiB across all row types | not applicable |

The 100 MiB attachment produces exactly 400 data chunks. Its independently encrypted object adds 24,493 bytes:
16 bytes of container header, 61 bytes per chunk, and a 77-byte authenticated final record. The format-2 backup then
streams that object through a second 256 KiB authenticated record layer.

Each manifest category accepts at most **1,000,000 rows**, subject to the smaller 16 GiB complete-container limit and
the aggregate validator limits above. Identifier occurrences count every identifier field that must survive for a
later uniqueness, relationship, or per-owner check; this prevents independently maximal categories from multiplying
into unbounded heap retention. Limits that materially affect growth are:

- one folder per credential and at most 100 tags per credential;
- at most 10 password-history rows per credential;
- at most 20 visible attachment rows per credential, including metadata-only legacy rows, and 512 MiB of managed
  attachment plaintext per credential;
- at most 50 custom fields per credential, each name up to 200 code points and value up to 20,000 code points;
- at most 100 URLs, 100 recovery codes, 100 API keys, and 100 license keys per credential;
- notes up to 100,000 code points and common sensitive values up to 4,096 code points;
- zero or one TOTP configuration per credential, with a decoded setup key up to 128 bytes and label up to 200 code
  points; and
- up to 1,000,000 folders and 1,000,000 tags globally, although storage/container bounds normally win first.

Backup validation enforces these relationship/history/managed-attachment limits again; an authenticated but
policy-violating input cannot bypass the ordinary repository limits. Legacy attachment rows consume visible slots
for new imports but have no current-store object, so their historical declared sizes do not consume the 512 MiB
managed-object quota. Existing migrated vaults above the slot limit remain readable and exportable; the write path
rejects additional attachments instead of making those vaults unavailable.

Format-2 metadata ceilings are derived from each field's length prefix, marker, scalar, and accepted payload limit:
44 bytes for manifests, 1,681 for vault metadata, 67,982 for folders, 66,941 for tags, 2,056 for relationship rows,
33,688,811 for legacy-compatible credentials, 265,588 for attachment rows, and 133,208 for password-history
rows. Control and attachment-content records have separate 64-byte, 1 KiB, and 256 KiB limits.

## Import and export symmetry

Format-2 create, inspect, and restore use the same 16 GiB byte counter, record lengths, entity counts, relationship
limits, attachment size/count/aggregate limits, and exact EOF rule. Export also verifies every inner attachment
container with the active VEK and requires the authenticated and packaged byte streams to have the same SHA-256
fingerprint. Import authenticates every outer record, stages complete encrypted objects atomically, and restores only
after all records and EOF validate.

Before staging, restore reads the exact encrypted-object total from the authenticated manifest and compares it with
free space on the attachment volume. Android uses `StatFs`, Desktop uses the target `FileStore`, and iOS uses
filesystem attributes. The check is repeated before every object and retains one maximum-size encrypted object as a
reserve for allocation overhead and concurrent capacity changes. Existing vault objects are already reflected in the
reported free-space value. If capacity reporting is unavailable, restore retains its bounded writes and cleanup
behavior rather than rejecting a valid backup solely because the platform query failed.

Legacy format 1 is intentionally asymmetric: it remains readable with its historical 128 MiB/64 MiB bounds, omits
attachments, and is not the default export route. The old in-memory byte-array creation API exists only for legacy
compatibility/tests; user-facing file export uses format 2.

## Peak-memory behavior

Format 2 never holds the complete backup, all Room rows, or all attachment bytes in memory. Its bounded peaks are:

| Operation | Simultaneous materialization |
|---|---|
| password KDF | Argon2id working memory, exactly 64 MiB; released before row streaming |
| metadata export | one large Room row, its compact binary encoding, and its encrypted record; about 3× one row in the worst case |
| metadata inspect/restore | ciphertext + plaintext during AEAD, then plaintext + decoded row arrays; about 2× the single-record size |
| folder/tag export paging | at most 64 rows, each capped at 64 KiB payload |
| relationship export paging | at most 512 pairs of bounded identifiers |
| attachment verify/package/restore | 256 KiB chunks and their owned/encrypted copies; normally under 1 MiB plus I/O buffers |
| referential validation | exact identifier sets/maps and counters, capped at 1,000,000 retained occurrences and 64 MiB of identifier UTF-8 |

The deliberately conservative theoretical metadata peak is therefore about **97 MiB on export** (three ~32.13 MiB
representations). Import normally holds two representations (~65 MiB at the exact ceiling). For ciphertext above
256 KiB, however, the reader first collects bytes in 64 KiB chunks before creating the contiguous AEAD input; a
runtime that has not reclaimed those wiped chunks can transiently approach three representations (~97 MiB). This
tradeoff prevents a short or size-misreporting source from triggering a large allocation. Repository-created normal
credentials are far smaller. Format 2 admits only the 64 MiB Argon2id profile emitted by its writer, and the KDF
workspace is released before that row peak.

Format 1 is different: the input bytes, UTF-8/UTF-16 JSON, Base64 fields, decoded ciphertext, decrypted snapshot, DTO
graph, and decoded entity arrays can overlap until garbage collection. Its worst peak is runtime-dependent and can
exceed several times the 128 MiB input limit. This is why format 1 is compatibility-only and why attachments are not
added to it.

Mutable byte arrays are wiped best-effort after each record. Managed strings, native crypto internals, garbage-
collected copies, swap/pagefile, and crash dumps cannot be guaranteed wipeable by common Kotlin code.

## Realistic growth model

These are capacity estimates, not quotas. Attachment content is not compressed, so file choice dominates storage.
Local vault storage and a format-2 backup are usually within a few percent of one another: both contain the encrypted
objects, while SQLite and the outer backup record layer add different small overheads.

**Typical model per credential:** approximately 4.5 KiB for the encrypted credential, three history entries, three
tag references, folder/tag amortization, one small custom field, occasional OTP metadata, and record overhead; plus
0.1 attachment averaging 512 KiB (one such attachment per ten credentials). Total: about **55.7 KiB/credential**.

**Conservative normal-use model per credential:** approximately 32 KiB for rich notes, ten history entries, ten tags,
five custom fields, and frequent OTP metadata; plus 0.25 attachment averaging 2 MiB (one attachment per four
credentials). Total: about **544 KiB/credential**.

| Credentials | Typical | Conservative normal use |
|---:|---:|---:|
| 1,000 | ~54 MiB | ~531 MiB |
| 5,000 | ~272 MiB | ~2.59 GiB |
| 10,000 | ~544 MiB | ~5.19 GiB |
| 50,000 | ~2.66 GiB | ~25.9 GiB |

At the typical rate, the 16 GiB container is reached around 300,000 credentials. At the conservative rate it is
reached around 30,800 credentials, so a 50,000-credential vault with that attachment density cannot fit one backup.
Attachment-heavy use reaches the cap much sooner: roughly 31 credentials each filled to the 512 MiB aggregate limit,
or about 163 independent 100 MiB files, exhaust 16 GiB after encryption/record overhead.

Metadata alone no longer has the old 64 MiB aggregate ceiling. With the assumptions above, 50,000 metadata-only
credentials are roughly 220 MiB typical or 1.53 GiB conservative and stream successfully within the outer limit.

## Failure behavior

- A declared format-2 size above 16 GiB is rejected before expensive KDF/record processing; unknown-size sources are
  stopped by the same running byte counter.
- A record type is checked against the expected stream position before its remaining framing is read. Its length is
  checked against the exact type-specific maximum. Ciphertext through one 256 KiB content record is allocated
  directly; larger ciphertext is accumulated from actual input in wiped 64 KiB chunks before the contiguous AEAD
  input is allocated. A short, unknown-size, or size-misreporting provider therefore cannot trigger the full declared
  allocation. Reported file size remains an additional early bound, never the sole defense.
- One attachment may use at most 65,536 non-empty outer content records, additionally bounded by its declared
  encrypted-object byte count; export applies the same limit before publishing a backup.
- Declared attachment size/count/aggregate failures happen before reading plaintext. Unknown-size input that crosses
  100 MiB or 512 MiB aborts its atomic object write and removes staging metadata/files.
- Insufficient attachment-volume capacity is rejected before staging starts, or before the next object if capacity
  shrinks during restore, and is reported separately from password/corruption failures.
- Export overflow or sink failure aborts the candidate output. Restore limit, authentication, transcript, relation,
  constraint, I/O, or cancellation failure leaves Room unchanged and removes newly staged objects.
- Android document providers cannot guarantee atomic replacement of the external selected URI; a provider failure may
  leave a partial destination, but never a committed PassVault backup result or retained private plaintext staging file.

Automated evidence covers the exact 100 MiB attachment, one-byte-over attachment/count limits, the field-derived
maximum credential record, exact/max+1 record limits, pre-allocation truncated-source rejection,
unknown and overstated source lengths, 50,000 independently encoded representative rows, fragmented reads, 16 GiB
declared-size rejection, tamper/truncation, source changes between passes, transaction rollback, cancellation, and
staging cleanup.
