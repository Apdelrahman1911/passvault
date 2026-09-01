package com.passvault.core.database.backup

import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.repository.MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BackupEntityBinaryCodecTest {
    @Test
    fun `manifest round trips every exact entity count`() {
        val manifest = BackupStreamManifest(
            credentialCount = 50_000,
            folderCount = 1_000,
            tagCount = 2_000,
            credentialFolderReferenceCount = 40_000,
            credentialTagReferenceCount = 150_000,
            attachmentCount = 75_000,
            managedAttachmentCount = 74_000,
            passwordHistoryCount = 250_000,
            managedAttachmentObjectBytes = 7_654_321,
        )
        val encoded = BackupEntityBinaryCodec.encodeManifest(manifest)

        assertEquals(manifest, BackupEntityBinaryCodec.decodeManifest(encoded))
        assertTrue(encoded.size <= BackupEntityBinaryCodec.maximumPlaintextBytes(BackupRecordType.MANIFEST))
        encoded.fill(0)
    }

    @Test
    fun `pre storage accounting manifest remains readable without an object byte total`() {
        val manifest = BackupStreamManifest(
            credentialCount = 1,
            folderCount = 0,
            tagCount = 0,
            credentialFolderReferenceCount = 0,
            credentialTagReferenceCount = 0,
            attachmentCount = 1,
            managedAttachmentCount = 1,
            passwordHistoryCount = 0,
            metadataSchemaVersion = PRE_STORAGE_ACCOUNTING_METADATA_SCHEMA_VERSION,
            managedAttachmentObjectBytes = null,
        )

        val encoded = BackupEntityBinaryCodec.encodeManifest(manifest)

        assertEquals(manifest, BackupEntityBinaryCodec.decodeManifest(encoded))
    }

    @Test
    fun `fixed legacy manifest fixtures retain their original wire layout`() {
        listOf(
            LEGACY_BACKUP_METADATA_SCHEMA_VERSION,
            PRE_STORAGE_ACCOUNTING_METADATA_SCHEMA_VERSION,
        ).forEach { version ->
            val fixture = manifestFixture(version)
            val decoded = BackupEntityBinaryCodec.decodeManifest(fixture)

            assertEquals(version, decoded.metadataSchemaVersion)
            assertEquals(null, decoded.managedAttachmentObjectBytes)
            assertContentEquals(fixture, BackupEntityBinaryCodec.encodeManifest(decoded))
        }
    }

    @Test
    fun `fixed current manifest fixture authenticates object storage bytes`() {
        val fixture = manifestFixture(
            version = STORAGE_ACCOUNTING_METADATA_SCHEMA_VERSION,
            objectBytes = 9L,
        )
        val decoded = BackupEntityBinaryCodec.decodeManifest(fixture)

        assertEquals(9L, decoded.managedAttachmentObjectBytes)
        assertContentEquals(fixture, BackupEntityBinaryCodec.encodeManifest(decoded))
    }

    @Test
    fun `current manifest rejects malformed object byte totals`() {
        val valid = manifestFixture(
            version = STORAGE_ACCOUNTING_METADATA_SCHEMA_VERSION,
            objectBytes = 9L,
        )
        val negative = valid.copyOf().also { bytes ->
            repeat(Long.SIZE_BYTES) { offset -> bytes[bytes.lastIndex - offset] = -1 }
        }
        val oversized = manifestFixture(
            version = STORAGE_ACCOUNTING_METADATA_SCHEMA_VERSION,
            objectBytes = BackupLimits.MAX_BACKUP_BYTES + 1L,
        )

        assertFails { BackupEntityBinaryCodec.decodeManifest(valid.copyOf(valid.size - 1)) }
        assertFails { BackupEntityBinaryCodec.decodeManifest(valid + 0) }
        assertFails { BackupEntityBinaryCodec.decodeManifest(negative) }
        assertFails { BackupEntityBinaryCodec.decodeManifest(oversized) }
    }

    @Test
    fun `legacy metadata schema decodes and discards the title blind index`() {
        val entity = credential(summaryBytes = 20, secretBytes = 20)
        val legacyHash = ByteArray(32) { index -> (index + 1).toByte() }
        val legacyBytes = encodeLegacyCredential(entity, legacyHash)

        val decoded = assertIs<BackupMetadataValue.Credential>(
            BackupEntityBinaryCodec.decode(
                BackupRecordType.CREDENTIAL,
                legacyBytes,
                LEGACY_BACKUP_METADATA_SCHEMA_VERSION,
            ),
        )
        val currentBytes = BackupEntityBinaryCodec.encode(BackupMetadataValue.Credential(entity))
        try {
            assertEquals(entity, decoded.value)
            assertEquals(legacyBytes.size - Int.SIZE_BYTES - legacyHash.size, currentBytes.size)
            assertContentEquals(
                currentBytes,
                BackupEntityBinaryCodec.encode(decoded),
            )
        } finally {
            entity.clearArrays()
            decoded.clear()
            legacyHash.fill(0)
            legacyBytes.fill(0)
            currentBytes.fill(0)
        }
    }

    @Test
    fun `maximum credential payloads fit one bounded record and round trip`() {
        val entity = credential(
            summaryBytes = MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES,
            secretBytes = MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES,
        )
        val value = BackupMetadataValue.Credential(entity)
        val encoded = BackupEntityBinaryCodec.encode(value)
        val decoded = assertIs<BackupMetadataValue.Credential>(
            BackupEntityBinaryCodec.decode(BackupRecordType.CREDENTIAL, encoded),
        )
        try {
            assertEquals(MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES, decoded.value.summaryPayload.size)
            assertEquals(MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES, decoded.value.secretPayload.size)
            assertTrue(encoded.size <= BackupLimits.MAX_ENTITY_RECORD_BYTES)
        } finally {
            value.clear()
            decoded.clear()
            encoded.fill(0)
        }
    }

    @Test
    fun `one byte beyond an encrypted credential field limit is rejected`() {
        val value = BackupMetadataValue.Credential(
            credential(
                summaryBytes = MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES + 1,
                secretBytes = MINIMUM_ENVELOPE_BYTES,
            ),
        )
        try {
            assertFails { BackupEntityBinaryCodec.encode(value) }
        } finally {
            value.clear()
        }
    }

    @Test
    fun `fifty thousand representative credentials are encoded without aggregate buffering`() {
        val entity = credential(summaryBytes = 2 * 1024, secretBytes = 6 * 1024)
        val value = BackupMetadataValue.Credential(entity)
        var totalBytes = 0L
        var largestRecord = 0
        try {
            repeat(REPRESENTATIVE_CREDENTIAL_COUNT) {
                val encoded = BackupEntityBinaryCodec.encode(value)
                totalBytes += encoded.size
                largestRecord = maxOf(largestRecord, encoded.size)
                encoded.fill(0)
            }
            assertTrue(totalBytes > 390L * 1024L * 1024L)
            assertTrue(largestRecord < 16 * 1024)
        } finally {
            value.clear()
        }
    }

    private fun credential(summaryBytes: Int, secretBytes: Int) = CredentialRecordEntity(
        id = "00000000-0000-4000-8000-000000000001",
        type = "Login",
        summaryPayload = ByteArray(summaryBytes),
        summaryNonce = ByteArray(24),
        secretPayload = ByteArray(secretBytes),
        secretNonce = ByteArray(24),
        folderId = null,
        isFavorite = false,
        createdAt = 1,
        updatedAt = 1,
        lastUsedAt = null,
    )

    private fun encodeLegacyCredential(entity: CredentialRecordEntity, titleHash: ByteArray): ByteArray = Buffer()
        .writeString(entity.id)
        .writeString(entity.type)
        .writeByteArray(titleHash)
        .writeByteArray(entity.summaryPayload)
        .writeByteArray(entity.summaryNonce)
        .writeByteArray(entity.secretPayload)
        .writeByteArray(entity.secretNonce)
        .writeByte(0)
        .writeByte(0)
        .writeLong(entity.createdAt)
        .writeLong(entity.updatedAt)
        .writeByte(0)
        .readByteArray()

    private fun Buffer.writeString(value: String): Buffer = writeByteArray(value.encodeToByteArray())

    private fun Buffer.writeByteArray(value: ByteArray): Buffer = writeInt(value.size).write(value)

    private fun manifestFixture(version: Int, objectBytes: Long? = null): ByteArray = Buffer()
        .writeInt(version)
        .writeInt(1)
        .writeInt(2)
        .writeInt(3)
        .writeInt(4)
        .writeInt(5)
        .writeInt(6)
        .writeInt(7)
        .writeInt(8)
        .apply { objectBytes?.let { writeLong(it) } }
        .readByteArray()

    private fun CredentialRecordEntity.clearArrays() {
        summaryPayload.fill(0)
        summaryNonce.fill(0)
        secretPayload.fill(0)
        secretNonce.fill(0)
    }

    private companion object {
        const val MINIMUM_ENVELOPE_BYTES = 20
        const val REPRESENTATIVE_CREDENTIAL_COUNT = 50_000
    }
}
