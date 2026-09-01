package com.passvault.core.database

import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.entity.MigrationStateEntity
import com.passvault.core.database.entity.CurrentVersionInfoEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for database entities to verify they compile and work correctly.
 */
class EntityTest {

    @Test
    fun credentialRecordEntity_equalityWorks() {
        val entity1 = CredentialRecordEntity(
            id = "test-id",
            type = "Login",
            summaryPayload = byteArrayOf(4, 5, 6),
            summaryNonce = byteArrayOf(7, 8, 9),
            secretPayload = byteArrayOf(10, 11, 12),
            secretNonce = byteArrayOf(13, 14, 15),
            folderId = null,
            isFavorite = false,
            createdAt = 1234567890L,
            updatedAt = 1234567890L,
            lastUsedAt = null
        )

        val entity2 = CredentialRecordEntity(
            id = "test-id",
            type = "Login",
            summaryPayload = byteArrayOf(4, 5, 6),
            summaryNonce = byteArrayOf(7, 8, 9),
            secretPayload = byteArrayOf(10, 11, 12),
            secretNonce = byteArrayOf(13, 14, 15),
            folderId = null,
            isFavorite = false,
            createdAt = 1234567890L,
            updatedAt = 1234567890L,
            lastUsedAt = null
        )

        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun credentialRecordEntity_differentIdsNotEqual() {
        val entity1 = CredentialRecordEntity(
            id = "test-id-1",
            type = "Login",
            summaryPayload = byteArrayOf(4, 5, 6),
            summaryNonce = byteArrayOf(7, 8, 9),
            secretPayload = byteArrayOf(10, 11, 12),
            secretNonce = byteArrayOf(13, 14, 15),
            folderId = null,
            isFavorite = false,
            createdAt = 1234567890L,
            updatedAt = 1234567890L,
            lastUsedAt = null
        )

        val entity2 = CredentialRecordEntity(
            id = "test-id-2",
            type = "Login",
            summaryPayload = byteArrayOf(4, 5, 6),
            summaryNonce = byteArrayOf(7, 8, 9),
            secretPayload = byteArrayOf(10, 11, 12),
            secretNonce = byteArrayOf(13, 14, 15),
            folderId = null,
            isFavorite = false,
            createdAt = 1234567890L,
            updatedAt = 1234567890L,
            lastUsedAt = null
        )

        assertNotEquals(entity1, entity2)
    }

    @Test
    fun folderRecordEntity_equalityWorks() {
        val entity1 = FolderRecordEntity(
            id = "folder-id",
            parentId = null,
            nameHash = byteArrayOf(1, 2, 3),
            encryptedPayload = byteArrayOf(4, 5, 6),
            payloadNonce = byteArrayOf(7, 8, 9),
            icon = "📁",
            sortOrder = 0,
            createdAt = 1234567890L,
            updatedAt = 1234567890L
        )

        val entity2 = FolderRecordEntity(
            id = "folder-id",
            parentId = null,
            nameHash = byteArrayOf(1, 2, 3),
            encryptedPayload = byteArrayOf(4, 5, 6),
            payloadNonce = byteArrayOf(7, 8, 9),
            icon = "📁",
            sortOrder = 0,
            createdAt = 1234567890L,
            updatedAt = 1234567890L
        )

        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun tagRecordEntity_equalityWorks() {
        val entity1 = TagRecordEntity(
            id = "tag-id",
            nameHash = byteArrayOf(1, 2, 3),
            encryptedPayload = byteArrayOf(4, 5, 6),
            payloadNonce = byteArrayOf(7, 8, 9),
            color = "#FF0000",
            createdAt = 1234567890L
        )

        val entity2 = TagRecordEntity(
            id = "tag-id",
            nameHash = byteArrayOf(1, 2, 3),
            encryptedPayload = byteArrayOf(4, 5, 6),
            payloadNonce = byteArrayOf(7, 8, 9),
            color = "#FF0000",
            createdAt = 1234567890L
        )

        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun credentialTagCrossRef_equalityWorks() {
        val ref1 = CredentialTagCrossRef(
            credentialId = "cred-id",
            tagId = "tag-id"
        )

        val ref2 = CredentialTagCrossRef(
            credentialId = "cred-id",
            tagId = "tag-id"
        )

        assertEquals(ref1, ref2)
    }

    @Test
    fun attachmentRecordEntity_equalityWorks() {
        val entity1 = AttachmentRecordEntity(
            id = "attach-id",
            credentialId = "cred-id",
            encryptedFilename = byteArrayOf(1, 2, 3),
            filenameNonce = byteArrayOf(4, 5, 6),
            mimeType = "application/pdf",
            sizeBytes = 1024L,
            storagePath = "attachments/abc.pdf",
            keyDerivationContext = "attachment:abc",
            createdAt = 1234567890L
        )

        val entity2 = AttachmentRecordEntity(
            id = "attach-id",
            credentialId = "cred-id",
            encryptedFilename = byteArrayOf(1, 2, 3),
            filenameNonce = byteArrayOf(4, 5, 6),
            mimeType = "application/pdf",
            sizeBytes = 1024L,
            storagePath = "attachments/abc.pdf",
            keyDerivationContext = "attachment:abc",
            createdAt = 1234567890L
        )

        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun passwordHistoryRecordEntity_equalityWorks() {
        val entity1 = PasswordHistoryRecordEntity(
            id = "history-id",
            credentialId = "cred-id",
            encryptedPassword = byteArrayOf(1, 2, 3),
            passwordNonce = byteArrayOf(4, 5, 6),
            changedAt = 1234567890L
        )

        val entity2 = PasswordHistoryRecordEntity(
            id = "history-id",
            credentialId = "cred-id",
            encryptedPassword = byteArrayOf(1, 2, 3),
            passwordNonce = byteArrayOf(4, 5, 6),
            changedAt = 1234567890L
        )

        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun migrationStateEntity_equalityWorks() {
        val entity1 = MigrationStateEntity(
            id = 1,
            fromVersion = 1,
            toVersion = 2,
            migrationName = "AddIndex",
            checksum = byteArrayOf(1, 2, 3),
            isSuccessful = true,
            errorMessage = null,
            startedAt = 1234567890L,
            durationMs = 100L
        )

        val entity2 = MigrationStateEntity(
            id = 1,
            fromVersion = 1,
            toVersion = 2,
            migrationName = "AddIndex",
            checksum = byteArrayOf(1, 2, 3),
            isSuccessful = true,
            errorMessage = null,
            startedAt = 1234567890L,
            durationMs = 100L
        )

        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun currentVersionInfoEntity_equalityWorks() {
        val entity1 = CurrentVersionInfoEntity(
            singletonKey = 1,
            currentVersion = 1,
            lastMigrationId = null,
            lastCheckedAt = 1234567890L,
            isHealthy = true
        )

        val entity2 = CurrentVersionInfoEntity(
            singletonKey = 1,
            currentVersion = 1,
            lastMigrationId = null,
            lastCheckedAt = 1234567890L,
            isHealthy = true
        )

        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }
}
