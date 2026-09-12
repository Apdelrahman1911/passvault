# One historical-layout-origin backup regression — proposal only

Author `/root/c20_cleanup_author`, independent reviewer `/root/c20_cleanup_review`.
Root alone may apply, admit and run. No build/test/helper/SDK/Git/held-runtime
operation occurred here; source/report editing is not regression evidence.

## Risk, reachability and reuse

Migration MATRIX.md §3 explicitly retains historical-layout-origin backup as a
missing contract. Its existing migration graph stores byte sentinels, not usable
AEAD ciphertext; the existing six pagination backups start from current schema5.
Reuse all earlier migration/recovery and39 streaming successes. Do not replay
those classes or the new separate rollback/reopen method for this proposal.

Exactly one added method in the existing `VaultBackupUnicodePaginationTest`:
`schema one encrypted records survive current backup restore and a fresh reopen`.
The existing six methods and old helpers remain text-identical. It reuses the
real Fixture, repository seed/decode checks, bounded MemorySink/MemorySource,
password clearing and original non-following owned-root deletion implementation.
Five private helpers isolate the new open/schema-origin/snapshot/cleanup path.
No production, dependency, schema, version, identity or backup-format change.

## Proposed boundary (not observed outcomes)

1. Current production repositories/crypto create two credential/folder/tag rows
   and three history rows in a file-backed seed DB. Verify decrypted graph, then
   lock the source session and close Room before copying any encrypted rows.
2. Build all genuine exported schema1 DDL/indices/setup identity in another
   owned file. Parameter-bound ATTACH reads only the closed synthetic seed.
   Copy seven selected tables with exact column names; the obsolete title_hash
   column alone receives zero32 (it is discarded by production3→4 and is not
   claimed as a valid blind-index producer). All live encrypted fields, nonces,
   KDF parameters, salts, wrapped key and active folder/tag hashes remain exact.
   Assert actual main.user_version1, exported Room identity, title_hash presence
   and no FK violations before the current Room builder can migrate anything.
3. Open through unchanged addVaultMigrations1→2→3→4→5, recover the key through
   production unlock, verify decrypted graph and content-aware entity equality.
   Export through the current public V2 service and save the small encrypted
   container as CREATE_NEW under the owned fixture. Close origin Room/session.
4. Reopen bounded encrypted file bytes; initialize/unlock a distinct-password
   fresh target and seed a sentinel. Restore through public production service,
   compare returned counts/inspection and assert sentinel removal/source closure.
   Close target Room/session, then reopen new objects and unlock solely using
   restored metadata plus the original master password. Assert schema5, title,
   secrets, folder/tag/history relationships and immutable encrypted graph.

Snapshot equality uses the entities' existing content-aware equals methods and
sorts IDs/cross-refs, ignoring only intentional last-access time and deriving
count from row membership. The final restored entry_count is asserted separately.
The backup's bytes are <=256KiB; source adapters retain19-byte fragmented reads.
All new cleanup is NonCancellable, locks before Room close, preserves primary
errors with secondary suppression, and uses the existing exact owned-root
no-FOLLOW_LINKS deletion; unknown foreign links fail rather than escape.

## Honest limits and smallest future validation

This is a **current synthetic producer in an authentic historical physical
layout**, not a backup from an old application binary. One schema1 full-chain
origin, current V2 container, and current crypto format are exercised. It does
not establish every predecessor/format combination, old producer provenance,
legacy attachment-content compatibility, hardware behavior, provider streaming,
fsync/power loss or killed-process durability. Attachments intentionally absent;
legacy metadata-only attachment policy is not redesigned.

Root may batch only this one selected Desktop method with other already needed
core-database work, after exact source/preimage adoption, independent review and
fresh execution/retention/cleanup admission. Compilation is not a test pass.
Expected one JUnit testcase in the existing class, not six replays or task-count
credit. All STOP/NO-RETRY/CLOSED/native-refusal, held scopes, protected branches,
owner decisions and build1017001 restrictions remain. Current family/test counters
stay unchanged until actual results are independently reconciled.
