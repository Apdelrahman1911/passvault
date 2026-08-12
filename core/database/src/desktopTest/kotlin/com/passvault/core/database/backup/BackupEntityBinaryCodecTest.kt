package com.passvault.core.database.backup

import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.repository.MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES
import kotlin.test.Test
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
        )
        val encoded = BackupEntityBinaryCodec.encodeManifest(manifest)

        assertEquals(manifest, BackupEntityBinaryCodec.decodeManifest(encoded))
        assertTrue(encoded.size <= BackupEntityBinaryCodec.maximumPlaintextBytes(BackupRecordType.MANIFEST))
        encoded.fill(0)
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
        titleHash = ByteArray(32),
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

    private companion object {
        const val MINIMUM_ENVELOPE_BYTES = 20
        const val REPRESENTATIVE_CREDENTIAL_COUNT = 50_000
    }
}
