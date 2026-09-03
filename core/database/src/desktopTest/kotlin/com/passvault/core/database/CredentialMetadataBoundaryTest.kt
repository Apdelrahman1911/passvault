package com.passvault.core.database

import androidx.room.PooledConnection
import androidx.room.Room
import androidx.room.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CredentialMetadataBoundaryTest {
    @Test
    fun `credential schema classifies every persisted column`() = runTest {
        val database = openDatabase()
        try {
            database.useReaderConnection { connection ->
                val columns = connection.columnNames("credential_records")

                assertEquals(
                    ENCRYPTED_CONTAINER_COLUMNS,
                    columns.intersect(ENCRYPTED_CONTAINER_COLUMNS),
                )
                assertEquals(
                    REVIEWED_STRUCTURAL_COLUMNS,
                    columns - ENCRYPTED_CONTAINER_COLUMNS,
                    "A credential column was added outside the reviewed plaintext-metadata boundary",
                )
                assertEquals(REVIEWED_CREDENTIAL_INDEXES, connection.explicitIndexNames("credential_records"))
            }
        } finally {
            database.close()
        }
    }

    @Test
    fun `relationship schema exposes only reviewed identifier pairs`() = runTest {
        val database = openDatabase()
        try {
            database.useReaderConnection { connection ->
                assertEquals(
                    setOf("credential_id", "folder_id"),
                    connection.columnNames("credential_folder_cross_ref"),
                )
                assertEquals(
                    setOf("credential_id", "tag_id"),
                    connection.columnNames("credential_tag_cross_ref"),
                )
            }
        } finally {
            database.close()
        }
    }

    private fun openDatabase(): VaultDatabase = Room.inMemoryDatabaseBuilder<VaultDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private suspend fun PooledConnection.columnNames(tableName: String): Set<String> = buildSet {
        usePrepared("PRAGMA table_info(`$tableName`)") { statement ->
            while (statement.step()) add(statement.getText(1))
        }
    }

    private suspend fun PooledConnection.explicitIndexNames(tableName: String): Set<String> = buildSet {
        usePrepared("PRAGMA index_list(`$tableName`)") { statement ->
            while (statement.step()) {
                val name = statement.getText(1)
                if (!name.startsWith("sqlite_autoindex_")) add(name)
            }
        }
    }

    private companion object {
        val ENCRYPTED_CONTAINER_COLUMNS = setOf(
            "summary_payload",
            "summary_nonce",
            "secret_payload",
            "secret_nonce",
        )
        val REVIEWED_STRUCTURAL_COLUMNS = setOf(
            "id",
            "type",
            "folder_id",
            "is_favorite",
            "created_at",
            "updated_at",
            "last_used_at",
        )
        val REVIEWED_CREDENTIAL_INDEXES = setOf(
            "index_credential_records_folder_id",
            "index_credential_records_is_favorite",
            "index_credential_records_type",
            "index_credential_records_created_at",
            "index_credential_records_updated_at",
            "index_credential_records_last_used_at",
        )
    }
}
