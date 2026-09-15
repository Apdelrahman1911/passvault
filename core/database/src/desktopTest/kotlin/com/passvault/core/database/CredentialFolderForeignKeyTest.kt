package com.passvault.core.database

import androidx.room.PooledConnection
import androidx.room.Room
import androidx.room.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.FolderRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CredentialFolderForeignKeyTest {
    @Test
    fun `fresh schema declares and enforces the credential folder foreign key`() = runTest {
        val database = openDatabase()
        try {
            database.useReaderConnection { connection ->
                assertEquals(
                    listOf(
                        ForeignKeyInfo(
                            table = "folder_records",
                            from = "folder_id",
                            to = "id",
                            onUpdate = "NO ACTION",
                            onDelete = "SET NULL",
                        ),
                    ),
                    connection.foreignKeys("credential_records"),
                )
            }

            assertFails {
                database.credentialDao().insertOrUpdate(
                    credential(id = "invalid-insert", folderId = "missing-folder"),
                )
            }
            assertNull(database.credentialDao().getById("invalid-insert"))

            database.credentialDao().insertOrUpdate(credential(id = "invalid-update", folderId = null))
            assertFails {
                database.credentialDao().updateFolder("invalid-update", "missing-folder")
            }
            assertNull(database.credentialDao().getById("invalid-update")?.folderId)
        } finally {
            database.close()
        }
    }

    @Test
    fun `direct folder deletion nulls the canonical pointer without deleting ciphertext`() = runTest {
        val database = openDatabase()
        try {
            database.folderDao().insertOrUpdate(folder("folder-existing"))
            val credential = credential(id = "credential-existing", folderId = "folder-existing")
            database.credentialDao().insertOrUpdate(credential)
            database.credentialDao().replaceFolderForCredential(credential.id, credential.folderId)

            database.folderDao().deleteById("folder-existing")

            val retained = requireNotNull(database.credentialDao().getById(credential.id))
            assertNull(retained.folderId)
            assertContentEquals(credential.summaryPayload, retained.summaryPayload)
            assertContentEquals(credential.summaryNonce, retained.summaryNonce)
            assertContentEquals(credential.secretPayload, retained.secretPayload)
            assertContentEquals(credential.secretNonce, retained.secretNonce)
            assertTrue(database.credentialDao().getByFolderCrossRef("folder-existing").isEmpty())
        } finally {
            database.close()
        }
    }

    private fun openDatabase(): VaultDatabase = Room.inMemoryDatabaseBuilder<VaultDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private fun credential(id: String, folderId: String?): CredentialRecordEntity = CredentialRecordEntity(
        id = id,
        type = "Login",
        summaryPayload = byteArrayOf(1, 2, 3),
        summaryNonce = ByteArray(24) { 2 },
        secretPayload = byteArrayOf(4, 5, 6),
        secretNonce = ByteArray(24) { 3 },
        folderId = folderId,
        isFavorite = false,
        createdAt = 100,
        updatedAt = 200,
        lastUsedAt = null,
    )

    private fun folder(id: String): FolderRecordEntity = FolderRecordEntity(
        id = id,
        parentId = null,
        nameHash = ByteArray(32),
        encryptedPayload = byteArrayOf(1, 2),
        payloadNonce = ByteArray(24),
        icon = null,
        sortOrder = 0,
        createdAt = 100,
        updatedAt = 100,
    )

    private suspend fun PooledConnection.foreignKeys(tableName: String): List<ForeignKeyInfo> {
        val foreignKeys = mutableListOf<ForeignKeyInfo>()
        usePrepared("PRAGMA foreign_key_list(`$tableName`)") { statement ->
            while (statement.step()) {
                foreignKeys += ForeignKeyInfo(
                    table = statement.getText(2),
                    from = statement.getText(3),
                    to = statement.getText(4),
                    onUpdate = statement.getText(5),
                    onDelete = statement.getText(6),
                )
            }
        }
        return foreignKeys
    }

    private data class ForeignKeyInfo(
        val table: String,
        val from: String,
        val to: String,
        val onUpdate: String,
        val onDelete: String,
    )
}
