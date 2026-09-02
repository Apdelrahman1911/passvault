package com.passvault.core.database

import androidx.room.Room
import androidx.room.PooledConnection
import androidx.room.migration.Migration
import androidx.room.useReaderConnection
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
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultMigrationTest {
    @Test
    fun `version one data survives the folder and tag index migration`() = runTest {
        withVersionOneDatabase { path ->
            val beforeFolderPlan = queryPlan(path, FOLDER_LOOKUP_SQL)
            val beforeTagPlan = queryPlan(path, TAG_LOOKUP_SQL)
            assertTrue(beforeFolderPlan.any { "SCAN folder_records" in it })
            assertTrue(beforeTagPlan.any { "SCAN tag_records" in it })

            val database = openMigratedDatabase(path)
            try {
                assertEquals("folder-existing", database.folderDao().getById("folder-existing")?.id)
                assertEquals("tag-existing", database.tagDao().getById("tag-existing")?.id)
            } finally {
                database.close()
            }

            assertEquals(4L, queryLong(path, "PRAGMA user_version"))
            assertUsesIndex(queryPlan(path, FOLDER_LOOKUP_SQL), "index_folder_records_name_hash")
            assertUsesIndex(queryPlan(path, TAG_LOOKUP_SQL), "index_tag_records_name_hash")
        }
    }

    @Test
    fun `fresh version four schema contains only justified blind indexes`() = runTest {
        val database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        try {
            database.folderDao().getAllSummaries()
            database.useReaderConnection { connection ->
                assertUsesIndex(connection.queryPlan(FOLDER_LOOKUP_SQL), "index_folder_records_name_hash")
                assertUsesIndex(connection.queryPlan(TAG_LOOKUP_SQL), "index_tag_records_name_hash")
                assertFalse(connection.columnNames("credential_records").contains("title_hash"))
            }
        } finally {
            database.close()
        }
    }

    @Test
    fun `version two legacy attachment metadata survives version three migration`() = runTest {
        val directory = Files.createTempDirectory("passvault-room-migration-v2-")
        val path = directory.resolve("vault.db")
        try {
            createDatabaseFromSchema(path, version = 2)
            BundledSQLiteDriver().open(path.toString()).use { connection ->
                insertCredentialForAttachment(connection)
                connection.execSQL(
                    """
                    INSERT INTO attachment_records (
                        id, credential_id, encrypted_filename, filename_nonce,
                        mime_type, size_bytes, storage_path, key_derivation_context, created_at
                    ) VALUES (
                        'legacy-attachment', 'credential-existing', zeroblob(20), zeroblob(24),
                        'application/pdf', 1024, 'attachments/legacy.enc', 'legacy-context', 100
                    )
                    """.trimIndent(),
                )
            }

            val database = Room.databaseBuilder<VaultDatabase>(name = path.toString())
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
            try {
                val attachment = database.attachmentDao()
                    .getByCredential("credential-existing")
                    .single()
                assertEquals("legacy-attachment", attachment.id)
                assertEquals(0, attachment.contentFormatVersion)
                assertEquals("LEGACY", attachment.storageState)
                assertEquals("attachments/legacy.enc", attachment.storagePath)
                assertEquals(1, database.attachmentDao().getOccupiedSlotCount("credential-existing"))
                assertEquals(0, database.attachmentDao().getManagedSizeBytes("credential-existing"))
            } finally {
                database.close()
            }
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `version three migration removes title hash and preserves every dependent row`() = runTest {
        val directory = Files.createTempDirectory("passvault-room-migration-v3-")
        val path = directory.resolve("vault.db")
        try {
            createVersionThreeDatabaseWithCredentialGraph(path)

            val database = Room.databaseBuilder<VaultDatabase>(name = path.toString())
                .addMigrations(MIGRATION_3_4)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
            try {
                val credential = requireNotNull(database.credentialDao().getById("credential-existing"))
                assertEquals("Login", credential.type)
                assertEquals(listOf(1, 2, 3), credential.summaryPayload.map(Byte::toInt))
                assertEquals("attachment-existing", database.attachmentDao()
                    .getByCredential("credential-existing").single().id)
                assertEquals("history-existing", database.passwordHistoryDao()
                    .getByCredential("credential-existing").single().id)
                assertEquals("tag-existing", database.credentialDao()
                    .getTagCrossRefsForCredential("credential-existing").single().tagId)
                assertEquals("credential-existing", database.credentialDao()
                    .getByFolderCrossRef("folder-existing").single().id)
            } finally {
                database.close()
            }

            assertEquals(4L, queryLong(path, "PRAGMA user_version"))
            assertFalse(columnNames(path, "credential_records").contains("title_hash"))
            assertFalse(indexNames(path, "credential_records").contains("index_credential_records_title_hash"))
            assertEquals(0L, queryLong(path, "SELECT COUNT(*) FROM pragma_foreign_key_check"))
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `failed version four migration rolls back rebuilt credential graph`() = runTest {
        val directory = Files.createTempDirectory("passvault-room-migration-v3-rollback-")
        val path = directory.resolve("vault.db")
        try {
            createVersionThreeDatabaseWithCredentialGraph(path)
            val failingMigration = object : Migration(3, 4) {
                override fun migrate(connection: SQLiteConnection) {
                    MIGRATION_3_4.migrate(connection)
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

            assertEquals(3L, queryLong(path, "PRAGMA user_version"))
            assertTrue(columnNames(path, "credential_records").contains("title_hash"))
            assertEquals(1L, queryLong(path, "SELECT COUNT(*) FROM attachment_records"))
            assertEquals(1L, queryLong(path, "SELECT COUNT(*) FROM password_history_records"))
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `failed migration rolls back schema changes and preserves version one data`() = runTest {
        withVersionOneDatabase { path ->
            val failingMigration = object : Migration(1, 2) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "CREATE INDEX `index_folder_records_name_hash` " +
                            "ON `folder_records` (`name_hash`)",
                    )
                    connection.execSQL("CREATE INDEX invalid_migration ON missing_table (missing_column)")
                }
            }
            val database = Room.databaseBuilder<VaultDatabase>(name = path.toString())
                .addMigrations(failingMigration, MIGRATION_2_3, MIGRATION_3_4)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
            try {
                assertFails { database.folderDao().getAllSummaries() }
            } finally {
                database.close()
            }

            assertEquals(1L, queryLong(path, "PRAGMA user_version"))
            assertEquals(1L, queryLong(path, "SELECT COUNT(*) FROM folder_records"))
            assertFalse(indexNames(path, "folder_records").contains("index_folder_records_name_hash"))
        }
    }

    private fun openMigratedDatabase(path: Path): VaultDatabase =
        Room.databaseBuilder<VaultDatabase>(name = path.toString())
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    private suspend fun withVersionOneDatabase(block: suspend (Path) -> Unit) {
        val directory = Files.createTempDirectory("passvault-room-migration-")
        val path = directory.resolve("vault.db")
        try {
            createVersionOneDatabase(path)
            block(path)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    private fun createVersionOneDatabase(path: Path) {
        createDatabaseFromSchema(path, version = 1)
        BundledSQLiteDriver().open(path.toString()).use { connection ->
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
        }
    }

    @Suppress("LongMethod") // One fixture makes every row preserved by the migration explicit.
    private fun createVersionThreeDatabaseWithCredentialGraph(path: Path) {
        createDatabaseFromSchema(path, version = 3)
        BundledSQLiteDriver().open(path.toString()).use { connection ->
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
            connection.execSQL(
                """
                INSERT INTO credential_records (
                    id, type, title_hash, summary_payload, summary_nonce, secret_payload,
                    secret_nonce, folder_id, is_favorite, created_at, updated_at, last_used_at
                ) VALUES (
                    'credential-existing', 'Login', zeroblob(32), x'010203', zeroblob(24),
                    x'040506', zeroblob(24), 'folder-existing', 1, 100, 200, 300
                )
                """.trimIndent(),
            )
            connection.execSQL(
                "INSERT INTO credential_folder_cross_ref VALUES ('credential-existing', 'folder-existing')",
            )
            connection.execSQL(
                "INSERT INTO credential_tag_cross_ref VALUES ('credential-existing', 'tag-existing')",
            )
            connection.execSQL(
                """
                INSERT INTO attachment_records (
                    id, credential_id, encrypted_filename, filename_nonce, mime_type,
                    size_bytes, storage_path, key_derivation_context, created_at,
                    content_format_version, storage_state
                ) VALUES (
                    'attachment-existing', 'credential-existing', zeroblob(20), zeroblob(24),
                    'application/pdf', 1024, 'attachments/legacy.enc', 'legacy-context', 100, 0, 'LEGACY'
                )
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO password_history_records (
                    id, credential_id, encrypted_password, password_nonce, changed_at
                ) VALUES (
                    'history-existing', 'credential-existing', zeroblob(20), zeroblob(24), 100
                )
                """.trimIndent(),
            )
        }
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

    private fun insertCredentialForAttachment(connection: SQLiteConnection) {
        connection.execSQL(
            """
            INSERT INTO credential_records (
                id, type, title_hash, summary_payload, summary_nonce, secret_payload,
                secret_nonce, folder_id, is_favorite, created_at, updated_at, last_used_at
            ) VALUES (
                'credential-existing', 'Login', zeroblob(32), zeroblob(20), zeroblob(24),
                zeroblob(20), zeroblob(24), NULL, 0, 100, 100, NULL
            )
            """.trimIndent(),
        )
    }

    private fun exportedSchema(version: Int): ExportedSchema {
        val resource = requireNotNull(
            VaultMigrationTest::class.java.classLoader
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
                )
            },
            setupQueries = database.getValue("setupQueries").jsonArray.map { it.jsonPrimitive.content },
        )
    }

    private fun queryPlan(path: Path, sql: String): List<String> =
        BundledSQLiteDriver().open(path.toString()).use { connection -> connection.queryPlan(sql) }

    private fun SQLiteConnection.queryPlan(sql: String): List<String> = buildList {
        prepare("EXPLAIN QUERY PLAN $sql").use { statement ->
            while (statement.step()) add(statement.getText(3))
        }
    }

    private suspend fun PooledConnection.queryPlan(sql: String): List<String> {
        val plan = mutableListOf<String>()
        usePrepared("EXPLAIN QUERY PLAN $sql") { statement ->
            while (statement.step()) plan += statement.getText(3)
        }
        return plan
    }

    private fun queryLong(path: Path, sql: String): Long =
        BundledSQLiteDriver().open(path.toString()).use { connection ->
            connection.prepare(sql).use { statement ->
                check(statement.step())
                statement.getLong(0)
            }
        }

    private fun indexNames(path: Path, tableName: String): Set<String> =
        BundledSQLiteDriver().open(path.toString()).use { connection ->
            buildSet {
                connection.prepare("PRAGMA index_list(`$tableName`)").use { statement ->
                    while (statement.step()) add(statement.getText(1))
                }
            }
        }

    private fun columnNames(path: Path, tableName: String): Set<String> =
        BundledSQLiteDriver().open(path.toString()).use { connection -> connection.columnNames(tableName) }

    private fun SQLiteConnection.columnNames(tableName: String): Set<String> = buildSet {
        prepare("PRAGMA table_info(`$tableName`)").use { statement ->
            while (statement.step()) add(statement.getText(1))
        }
    }

    private suspend fun PooledConnection.columnNames(tableName: String): Set<String> {
        val columns = mutableSetOf<String>()
        usePrepared("PRAGMA table_info(`$tableName`)") { statement ->
            while (statement.step()) columns += statement.getText(1)
        }
        return columns
    }

    private fun assertUsesIndex(plan: List<String>, indexName: String) {
        assertTrue(plan.any { "USING INDEX $indexName" in it }, "Query plan did not use $indexName: $plan")
    }

    private data class ExportedSchema(
        val entities: List<ExportedEntity>,
        val setupQueries: List<String>,
    )

    private data class ExportedEntity(
        val tableName: String,
        val createSql: String,
        val indices: List<String>,
    )

    private companion object {
        const val FOLDER_LOOKUP_SQL = "SELECT id FROM folder_records WHERE name_hash = zeroblob(32)"
        const val TAG_LOOKUP_SQL = "SELECT id FROM tag_records WHERE name_hash = zeroblob(32)"
    }
}
