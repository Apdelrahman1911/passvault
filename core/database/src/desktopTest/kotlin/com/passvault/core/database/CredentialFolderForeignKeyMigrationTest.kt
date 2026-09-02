package com.passvault.core.database

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CredentialFolderForeignKeyMigrationTest {
    @Test
    fun `every shipped schema migrates to version five without losing the credential graph`() = runTest {
        for (version in 1..4) {
            val directory = Files.createTempDirectory("passvault-room-migration-v$version-")
            val path = directory.resolve("vault.db")
            try {
                createDatabaseWithCredentialGraph(path, version)

                val database = openMigratedDatabase(path)
                try {
                    val credential = requireNotNull(database.credentialDao().getById("credential-existing"))
                    assertEquals("folder-existing", credential.folderId)
                    assertContentEquals(byteArrayOf(1, 2, 3), credential.summaryPayload)
                    assertContentEquals(byteArrayOf(4, 5, 6), credential.secretPayload)

                    val orphan = requireNotNull(database.credentialDao().getById("credential-orphan"))
                    assertNull(orphan.folderId)
                    assertContentEquals(byteArrayOf(7, 8), orphan.summaryPayload)
                    assertContentEquals(byteArrayOf(9, 10), orphan.secretPayload)

                    assertEquals(
                        "credential-existing",
                        database.credentialDao().getByFolderCrossRef("folder-existing").single().id,
                    )
                    assertEquals(
                        "tag-existing",
                        database.credentialDao()
                            .getTagCrossRefsForCredential("credential-existing")
                            .single()
                            .tagId,
                    )
                    val attachment = database.attachmentDao()
                        .getByCredential("credential-existing")
                        .single()
                    assertEquals("attachment-existing", attachment.id)
                    assertContentEquals(byteArrayOf(10, 11), attachment.encryptedFilename)
                    val history = database.passwordHistoryDao()
                        .getByCredential("credential-existing")
                        .single()
                    assertEquals("history-existing", history.id)
                    assertContentEquals(byteArrayOf(12, 13), history.encryptedPassword)
                } finally {
                    database.close()
                }

                assertEquals(5L, queryLong(path, "PRAGMA user_version"), "source version $version")
                assertEquals(
                    exportedForeignKeys(version = 5, tableName = "credential_records"),
                    foreignKeys(path, "credential_records"),
                    "source version $version",
                )
                assertEquals(0L, queryLong(path, "SELECT COUNT(*) FROM pragma_foreign_key_check"))
            } finally {
                directory.toFile().deleteRecursively()
            }
        }
    }

    @Test
    fun `failed version five migration restores the intact version four graph`() = runTest {
        val directory = Files.createTempDirectory("passvault-room-migration-v4-rollback-")
        val path = directory.resolve("vault.db")
        try {
            createDatabaseWithCredentialGraph(path, version = 4)
            val failingMigration = object : Migration(4, 5) {
                override fun migrate(connection: SQLiteConnection) {
                    MIGRATION_4_5.migrate(connection)
                    connection.execSQL("CREATE INDEX invalid_migration ON missing_table (missing_column)")
                }
            }
            val database = Room.databaseBuilder<VaultDatabase>(name = path.toString())
                .addMigrations(failingMigration)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
            try {
                assertFails { database.credentialDao().getById("credential-existing") }
            } finally {
                database.close()
            }

            assertEquals(4L, queryLong(path, "PRAGMA user_version"))
            assertTrue(foreignKeys(path, "credential_records").isEmpty())
            assertEquals(
                "missing-folder",
                queryText(path, "SELECT folder_id FROM credential_records WHERE id = 'credential-orphan'"),
            )
            assertContentEquals(
                byteArrayOf(1, 2, 3),
                queryBlob(path, "SELECT summary_payload FROM credential_records WHERE id = 'credential-existing'"),
            )
            assertContentEquals(
                byteArrayOf(4, 5, 6),
                queryBlob(path, "SELECT secret_payload FROM credential_records WHERE id = 'credential-existing'"),
            )
            assertEquals(2L, queryLong(path, "SELECT COUNT(*) FROM credential_folder_cross_ref"))
            assertEquals(1L, queryLong(path, "SELECT COUNT(*) FROM credential_tag_cross_ref"))
            assertEquals(1L, queryLong(path, "SELECT COUNT(*) FROM attachment_records"))
            assertEquals(1L, queryLong(path, "SELECT COUNT(*) FROM password_history_records"))
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    private fun openMigratedDatabase(path: Path): VaultDatabase =
        Room.databaseBuilder<VaultDatabase>(name = path.toString())
            .addVaultMigrations()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    @Suppress("LongMethod") // The fixture documents all encrypted/dependent rows that must survive.
    private fun createDatabaseWithCredentialGraph(path: Path, version: Int) {
        require(version in 1..4)
        createDatabaseFromSchema(path, version)
        BundledSQLiteDriver().open(path.toString()).use { connection ->
            connection.execSQL("PRAGMA foreign_keys = ON")
            connection.execSQL(
                """
                INSERT INTO folder_records (
                    id, parent_id, name_hash, encrypted_payload, payload_nonce,
                    icon, sort_order, created_at, updated_at
                ) VALUES (
                    'folder-existing', NULL, zeroblob(32), zeroblob(20), zeroblob(24),
                    NULL, 0, 100, 100
                )
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO tag_records (
                    id, name_hash, encrypted_payload, payload_nonce, color, created_at
                ) VALUES (
                    'tag-existing', zeroblob(32), zeroblob(20), zeroblob(24), NULL, 100
                )
                """.trimIndent(),
            )
            insertCredentials(connection, version)
            connection.execSQL(
                """
                INSERT INTO credential_folder_cross_ref VALUES
                    ('credential-existing', 'folder-existing'),
                    ('credential-orphan', 'folder-existing')
                """.trimIndent(),
            )
            connection.execSQL(
                "INSERT INTO credential_tag_cross_ref VALUES ('credential-existing', 'tag-existing')",
            )
            insertAttachment(connection, version)
            connection.execSQL(
                """
                INSERT INTO password_history_records (
                    id, credential_id, encrypted_password, password_nonce, changed_at
                ) VALUES (
                    'history-existing', 'credential-existing', x'0C0D', zeroblob(24), 100
                )
                """.trimIndent(),
            )
        }
    }

    private fun insertCredentials(connection: SQLiteConnection, version: Int) {
        val titleHashColumn = if (version < 4) "title_hash," else ""
        val titleHashValue = if (version < 4) "zeroblob(32)," else ""
        connection.execSQL(
            """
            INSERT INTO credential_records (
                id, type, $titleHashColumn summary_payload, summary_nonce, secret_payload,
                secret_nonce, folder_id, is_favorite, created_at, updated_at, last_used_at
            ) VALUES
                (
                    'credential-existing', 'Login', $titleHashValue x'010203', zeroblob(24),
                    x'040506', zeroblob(24), 'folder-existing', 1, 100, 200, 300
                ),
                (
                    'credential-orphan', 'SecureNote', $titleHashValue x'0708', zeroblob(24),
                    x'090A', zeroblob(24), 'missing-folder', 0, 101, 201, NULL
                )
            """.trimIndent(),
        )
    }

    private fun insertAttachment(connection: SQLiteConnection, version: Int) {
        val formatColumns = if (version >= 3) ", content_format_version, storage_state" else ""
        val formatValues = if (version >= 3) ", 0, 'LEGACY'" else ""
        connection.execSQL(
            """
            INSERT INTO attachment_records (
                id, credential_id, encrypted_filename, filename_nonce, mime_type,
                size_bytes, storage_path, key_derivation_context, created_at$formatColumns
            ) VALUES (
                'attachment-existing', 'credential-existing', x'0A0B', zeroblob(24),
                'application/pdf', 1024, 'attachments/legacy.enc', 'legacy-context', 100$formatValues
            )
            """.trimIndent(),
        )
    }

    private fun createDatabaseFromSchema(path: Path, version: Int) {
        BundledSQLiteDriver().open(path.toString()).use { connection ->
            val schema = exportedSchema(version)
            schema.entities.forEach { entity ->
                connection.execSQL(entity.createSql.replace("${'$'}{TABLE_NAME}", entity.tableName))
                entity.indices.forEach { indexSql ->
                    connection.execSQL(indexSql.replace("${'$'}{TABLE_NAME}", entity.tableName))
                }
            }
            schema.setupQueries.forEach(connection::execSQL)
            connection.execSQL("PRAGMA user_version = $version")
        }
    }

    private fun exportedSchema(version: Int): ExportedSchema {
        val resource = requireNotNull(
            CredentialFolderForeignKeyMigrationTest::class.java.classLoader
                .getResourceAsStream("com.passvault.core.database.VaultDatabase/$version.json"),
        ) { "The exported Room version-$version schema is missing from test resources" }
        val root = resource.bufferedReader().use { Json.parseToJsonElement(it.readText()).jsonObject }
        val database = root.getValue("database").jsonObject
        return ExportedSchema(
            entities = database.getValue("entities").jsonArray.map { element ->
                val entity = element.jsonObject
                ExportedEntity(
                    tableName = entity.getValue("tableName").jsonPrimitive.content,
                    createSql = entity.getValue("createSql").jsonPrimitive.content,
                    indices = (entity["indices"] as? JsonArray)
                        .orEmpty()
                        .map { index -> index.jsonObject.getValue("createSql").jsonPrimitive.content },
                    foreignKeys = (entity["foreignKeys"] as? JsonArray)
                        .orEmpty()
                        .flatMap { foreignKey ->
                            val value = foreignKey.jsonObject
                            val columns = value.getValue("columns").jsonArray
                            val referencedColumns = value.getValue("referencedColumns").jsonArray
                            columns.zip(referencedColumns).map { (column, referencedColumn) ->
                                ForeignKeyInfo(
                                    table = value.getValue("table").jsonPrimitive.content,
                                    from = column.jsonPrimitive.content,
                                    to = referencedColumn.jsonPrimitive.content,
                                    onUpdate = value.getValue("onUpdate").jsonPrimitive.content,
                                    onDelete = value.getValue("onDelete").jsonPrimitive.content,
                                )
                            }
                        }
                        .sortedForComparison(),
                )
            },
            setupQueries = database.getValue("setupQueries").jsonArray.map { it.jsonPrimitive.content },
        )
    }

    private fun queryLong(path: Path, sql: String): Long =
        BundledSQLiteDriver().open(path.toString()).use { connection ->
            connection.prepare(sql).use { statement ->
                check(statement.step())
                statement.getLong(0)
            }
        }

    private fun queryText(path: Path, sql: String): String =
        BundledSQLiteDriver().open(path.toString()).use { connection ->
            connection.prepare(sql).use { statement ->
                check(statement.step())
                statement.getText(0)
            }
        }

    private fun queryBlob(path: Path, sql: String): ByteArray =
        BundledSQLiteDriver().open(path.toString()).use { connection ->
            connection.prepare(sql).use { statement ->
                check(statement.step())
                statement.getBlob(0)
            }
        }

    private fun exportedForeignKeys(version: Int, tableName: String): List<ForeignKeyInfo> =
        exportedSchema(version).entities.single { it.tableName == tableName }.foreignKeys

    private fun foreignKeys(path: Path, tableName: String): List<ForeignKeyInfo> =
        BundledSQLiteDriver().open(path.toString()).use { connection -> connection.foreignKeys(tableName) }

    private fun SQLiteConnection.foreignKeys(tableName: String): List<ForeignKeyInfo> = buildList {
        prepare("PRAGMA foreign_key_list(`$tableName`)").use { statement ->
            while (statement.step()) {
                add(
                    ForeignKeyInfo(
                        table = statement.getText(2),
                        from = statement.getText(3),
                        to = statement.getText(4),
                        onUpdate = statement.getText(5),
                        onDelete = statement.getText(6),
                    ),
                )
            }
        }
    }.sortedForComparison()

    private fun List<ForeignKeyInfo>.sortedForComparison(): List<ForeignKeyInfo> = sortedWith(
        compareBy(
            ForeignKeyInfo::table,
            ForeignKeyInfo::from,
            ForeignKeyInfo::to,
            ForeignKeyInfo::onUpdate,
            ForeignKeyInfo::onDelete,
        ),
    )

    private data class ExportedSchema(
        val entities: List<ExportedEntity>,
        val setupQueries: List<String>,
    )

    private data class ExportedEntity(
        val tableName: String,
        val createSql: String,
        val indices: List<String>,
        val foreignKeys: List<ForeignKeyInfo>,
    )

    private data class ForeignKeyInfo(
        val table: String,
        val from: String,
        val to: String,
        val onUpdate: String,
        val onDelete: String,
    )
}
