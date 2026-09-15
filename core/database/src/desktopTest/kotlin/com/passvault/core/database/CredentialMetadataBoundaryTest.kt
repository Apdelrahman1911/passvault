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
                connection.assertBoundary(
                    tableName = "credential_records",
                    protectedColumns = CREDENTIAL_PROTECTED_COLUMNS,
                    visibleColumns = CREDENTIAL_VISIBLE_COLUMNS,
                    indexes = CREDENTIAL_INDEXES,
                )
            }
        } finally {
            database.close()
        }
    }

    @Test
    fun `folder and tag schemas classify every persisted column`() = runTest {
        val database = openDatabase()
        try {
            database.useReaderConnection { connection ->
                connection.assertBoundary(
                    tableName = "folder_records",
                    protectedColumns = FOLDER_PROTECTED_COLUMNS,
                    visibleColumns = FOLDER_VISIBLE_COLUMNS,
                    indexes = FOLDER_INDEXES,
                )
                connection.assertBoundary(
                    tableName = "tag_records",
                    protectedColumns = TAG_PROTECTED_COLUMNS,
                    visibleColumns = TAG_VISIBLE_COLUMNS,
                    indexes = TAG_INDEXES,
                )
            }
        } finally {
            database.close()
        }
    }

    @Test
    fun `attachment schema classifies every persisted column`() = runTest {
        val database = openDatabase()
        try {
            database.useReaderConnection { connection ->
                connection.assertBoundary(
                    tableName = "attachment_records",
                    protectedColumns = ATTACHMENT_PROTECTED_COLUMNS,
                    visibleColumns = ATTACHMENT_VISIBLE_COLUMNS,
                    indexes = ATTACHMENT_INDEXES,
                )
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

    private suspend fun PooledConnection.assertBoundary(
        tableName: String,
        protectedColumns: Set<String>,
        visibleColumns: Set<String>,
        indexes: Set<String>,
    ) {
        val columns = columnNames(tableName)
        assertEquals(protectedColumns, columns.intersect(protectedColumns))
        assertEquals(
            visibleColumns,
            columns - protectedColumns,
            "A $tableName column was added outside the reviewed plaintext-metadata boundary",
        )
        assertEquals(indexes, explicitIndexNames(tableName))
    }

    private companion object {
        val CREDENTIAL_PROTECTED_COLUMNS = setOf(
            "summary_payload",
            "summary_nonce",
            "secret_payload",
            "secret_nonce",
        )
        val CREDENTIAL_VISIBLE_COLUMNS = setOf(
            "id",
            "type",
            "folder_id",
            "is_favorite",
            "created_at",
            "updated_at",
            "last_used_at",
        )
        val CREDENTIAL_INDEXES = setOf(
            "index_credential_records_folder_id",
            "index_credential_records_is_favorite",
            "index_credential_records_type",
            "index_credential_records_created_at",
            "index_credential_records_updated_at",
            "index_credential_records_last_used_at",
        )
        val FOLDER_PROTECTED_COLUMNS = setOf("name_hash", "encrypted_payload", "payload_nonce")
        val FOLDER_VISIBLE_COLUMNS = setOf(
            "id",
            "parent_id",
            "icon",
            "sort_order",
            "created_at",
            "updated_at",
        )
        val FOLDER_INDEXES = setOf(
            "index_folder_records_parent_id",
            "index_folder_records_name_hash",
            "index_folder_records_sort_order",
            "index_folder_records_created_at",
        )
        val TAG_PROTECTED_COLUMNS = setOf("name_hash", "encrypted_payload", "payload_nonce")
        val TAG_VISIBLE_COLUMNS = setOf("id", "color", "created_at")
        val TAG_INDEXES = setOf(
            "index_tag_records_name_hash",
            "index_tag_records_color",
            "index_tag_records_created_at",
        )
        val ATTACHMENT_PROTECTED_COLUMNS = setOf("encrypted_filename", "filename_nonce")
        val ATTACHMENT_VISIBLE_COLUMNS = setOf(
            "id",
            "credential_id",
            "mime_type",
            "size_bytes",
            "storage_path",
            "key_derivation_context",
            "created_at",
            "content_format_version",
            "storage_state",
        )
        val ATTACHMENT_INDEXES = setOf(
            "index_attachment_records_credential_id",
            "index_attachment_records_created_at",
        )
    }
}
