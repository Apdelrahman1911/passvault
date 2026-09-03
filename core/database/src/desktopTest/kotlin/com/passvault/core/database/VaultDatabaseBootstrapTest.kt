package com.passvault.core.database

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.room.useReaderConnection
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class VaultDatabaseBootstrapTest {
    @Test
    fun `fresh and existing databases pass both health gates`() = runTest {
        withTempDirectory { directory ->
            val path = directory.resolve("vault.db")
            var factoryCalls = 0
            val bootstrap = bootstrap(path) {
                factoryCalls++
                openDatabase(path)
            }

            assertEquals(VaultDatabaseBootstrapResult.Ready, bootstrap.openAndVerify())
            assertTrue(Files.isRegularFile(path))
            assertEquals(VaultDatabaseBootstrapResult.Ready, bootstrap.openAndVerify())
            assertEquals(1, factoryCalls)
            bootstrap.close()

            val reopened = bootstrap(path) { openDatabase(path) }
            assertEquals(VaultDatabaseBootstrapResult.Ready, reopened.openAndVerify())
            reopened.close()
        }
    }

    @Test
    fun `damaged leaf is detected before Room and preserved byte for byte`() = runTest {
        withTempDirectory { directory ->
            val path = directory.resolve("vault.db")
            createDatabaseWithFiller(path)
            corruptLastLeafPage(path)
            val originalDatabase = Files.readAllBytes(path)
            val attachmentRoot = directory.resolve("attachments")
            Files.createDirectories(attachmentRoot.resolve("nested"))
            val attachment = attachmentRoot.resolve("nested/object.bin")
            val attachmentBytes = ByteArray(1_017) { index -> (index % 251).toByte() }
            Files.write(attachment, attachmentBytes)

            var factoryCalls = 0
            val bootstrap = bootstrap(path) {
                factoryCalls++
                openDatabase(path)
            }
            val failure = assertIs<VaultDatabaseBootstrapResult.RecoveryRequired>(
                bootstrap.openAndVerify(),
            )

            assertTrue(failure.canPreserveAndReset)
            assertEquals(0, factoryCalls, "Room must not touch a database rejected by preflight")
            assertContentEquals(originalDatabase, Files.readAllBytes(path))
            assertTrue(bootstrap.preserveAndReset().isSuccess)
            assertFalse(Files.exists(path))
            assertFalse(Files.exists(attachmentRoot))

            val databaseRecovery = singleRecoveryDirectory(directory.resolve("database-recovery"))
            val attachmentRecovery = singleRecoveryDirectory(directory.resolve("attachment-recovery"))
            assertContentEquals(originalDatabase, Files.readAllBytes(databaseRecovery.resolve("vault.db")))
            assertContentEquals(
                attachmentBytes,
                Files.readAllBytes(attachmentRecovery.resolve("attachments/nested/object.bin")),
            )

            assertEquals(VaultDatabaseBootstrapResult.Ready, bootstrap.openAndVerify())
            assertEquals(1, factoryCalls)
            assertTrue(Files.isRegularFile(path))
            bootstrap.close()
        }
    }

    @Test
    fun `bad header and truncated page are classified without recreating either file`() = runTest {
        for (damage in listOf(Damage.BAD_HEADER, Damage.TRUNCATED_PAGE)) {
            withTempDirectory { directory ->
                val path = directory.resolve("vault.db")
                createValidDatabase(path)
                when (damage) {
                    Damage.BAD_HEADER -> RandomAccessFile(path.toFile(), "rw").use { file ->
                        file.seek(0)
                        file.write(ByteArray(SQLITE_HEADER_BYTES))
                    }
                    Damage.TRUNCATED_PAGE -> RandomAccessFile(path.toFile(), "rw").use { file ->
                        file.setLength(file.length() - TRUNCATION_BYTES)
                    }
                }
                val damagedBytes = Files.readAllBytes(path)
                var factoryCalls = 0
                val bootstrap = bootstrap(path) {
                    factoryCalls++
                    openDatabase(path)
                }

                val result = assertIs<VaultDatabaseBootstrapResult.RecoveryRequired>(
                    bootstrap.openAndVerify(),
                    damage.name,
                )
                assertTrue(result.canPreserveAndReset)
                assertEquals(0, factoryCalls)
                assertContentEquals(damagedBytes, Files.readAllBytes(path))
                bootstrap.close()
            }
        }
    }

    @Test
    fun `a complete WAL bundle is accepted and its committed content is visible`() = runTest {
        withTempDirectory { directory ->
            val source = directory.resolve("source.db")
            val target = directory.resolve("target/vault.db")
            createValidDatabase(source)
            Files.createDirectories(target.parent)

            BundledSQLiteDriver().open(source.toString()).use { connection ->
                connection.prepare("PRAGMA journal_mode = WAL").use { statement ->
                    assertTrue(statement.step())
                    assertEquals("wal", statement.getText(0).lowercase())
                }
                connection.execSQL("PRAGMA wal_autocheckpoint = 0")
                connection.execSQL("CREATE TABLE wal_probe (id INTEGER PRIMARY KEY, value TEXT NOT NULL)")
                connection.execSQL("INSERT INTO wal_probe (value) VALUES ('committed-in-wal')")
                assertTrue(Files.size(Path.of(source.toString() + "-wal")) > 0L)

                copyDatabaseBundle(source, target)
            }

            val bootstrap = bootstrap(target) { openDatabase(target) }
            assertEquals(VaultDatabaseBootstrapResult.Ready, bootstrap.openAndVerify())
            val value = bootstrap.database().useReaderConnection { pooled ->
                pooled.usePrepared("SELECT value FROM wal_probe") { statement ->
                    check(statement.step())
                    statement.getText(0)
                }
            }
            assertEquals("committed-in-wal", value)
            bootstrap.close()
        }
    }

    @Test
    fun `missing sidecars after a clean close do not imply corruption`() = runTest {
        withTempDirectory { directory ->
            val path = directory.resolve("vault.db")
            createValidDatabase(path)
            Files.deleteIfExists(Path.of(path.toString() + "-wal"))
            Files.deleteIfExists(Path.of(path.toString() + "-shm"))

            val bootstrap = bootstrap(path) { openDatabase(path) }
            assertEquals(VaultDatabaseBootstrapResult.Ready, bootstrap.openAndVerify())
            bootstrap.close()
        }
    }

    @Test
    fun `damage introduced while Room opens is caught by the second gate`() = runTest {
        withTempDirectory { directory ->
            val path = directory.resolve("vault.db")
            createDatabaseWithFiller(path)
            val bootstrap = bootstrap(path) {
                corruptLastLeafPage(path)
                openDatabase(path)
            }

            val result = assertIs<VaultDatabaseBootstrapResult.RecoveryRequired>(
                bootstrap.openAndVerify(),
            )

            assertFalse(result.canPreserveAndReset)
            assertTrue(bootstrap.preserveAndReset().isFailure)
            assertTrue(Files.isRegularFile(path))
            bootstrap.close()
        }
    }

    @Test
    fun `migration failure stays unavailable and rolls back without corruption recovery`() = runTest {
        withTempDirectory { directory ->
            val path = directory.resolve("vault.db")
            createDatabaseFromSchema(path, version = 4)
            val failingMigration = object : Migration(4, 5) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL("CREATE TABLE migration_probe (id INTEGER PRIMARY KEY)")
                    connection.execSQL("CREATE INDEX invalid_migration ON missing_table (missing_column)")
                }
            }
            val bootstrap = bootstrap(path) {
                Room.databaseBuilder<VaultDatabase>(name = path.toString())
                    .addMigrations(failingMigration)
                    .setDriver(BundledSQLiteDriver())
                    .setQueryCoroutineContext(Dispatchers.IO)
                    .build()
            }

            assertEquals(VaultDatabaseBootstrapResult.Unavailable, bootstrap.openAndVerify())
            assertEquals(4L, queryLong(path, "PRAGMA user_version"))
            assertEquals(
                0L,
                queryLong(
                    path,
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='migration_probe'",
                ),
            )
            assertTrue(bootstrap.preserveAndReset().isFailure)
            assertTrue(Files.isRegularFile(path))
            bootstrap.close()
        }
    }

    private fun bootstrap(
        path: Path,
        databaseFactory: () -> VaultDatabase,
    ): VaultDatabaseBootstrap = VaultDatabaseBootstrap(
        storage = LocalVaultDatabaseStorage(
            databasePath = path.toString(),
            attachmentRootPath = path.parent.resolve("attachments").toString(),
            databaseRecoveryRootPath = path.parent.resolve("database-recovery").toString(),
            attachmentRecoveryRootPath = path.parent.resolve("attachment-recovery").toString(),
            diagnosticPath = path.parent.resolve("database-health.events").toString(),
        ),
        databaseFactory = databaseFactory,
    )

    private suspend fun createValidDatabase(path: Path) {
        val database = openDatabase(path)
        try {
            database.vaultMetadataDao().exists()
        } finally {
            database.close()
        }
    }

    private suspend fun createDatabaseWithFiller(path: Path) {
        createValidDatabase(path)
        BundledSQLiteDriver().open(path.toString()).use { connection ->
            connection.execSQL("CREATE TABLE integrity_filler (id INTEGER PRIMARY KEY, payload BLOB NOT NULL)")
            connection.execSQL(
                """
                WITH RECURSIVE rows(value) AS (
                    SELECT 1
                    UNION ALL
                    SELECT value + 1 FROM rows WHERE value < $FILLER_ROWS
                )
                INSERT INTO integrity_filler(payload) SELECT zeroblob($FILLER_BYTES) FROM rows
                """.trimIndent(),
            )
        }
    }

    private fun corruptLastLeafPage(path: Path) {
        RandomAccessFile(path.toFile(), "rw").use { file ->
            val header = ByteArray(SQLITE_HEADER_BYTES)
            file.readFully(header)
            val encodedPageSize = ((header[16].toInt() and 0xff) shl 8) or
                (header[17].toInt() and 0xff)
            val pageSize = if (encodedPageSize == 1) 65_536 else encodedPageSize
            check(pageSize >= 512)
            val pageCount = file.length() / pageSize
            val leafOffset = (pageCount - 1 downTo 1)
                .map { pageIndex -> pageIndex * pageSize }
                .first { offset ->
                    file.seek(offset)
                    file.readUnsignedByte() == SQLITE_TABLE_LEAF_PAGE
                }
            file.seek(leafOffset)
            file.writeByte(0)
        }
    }

    private fun copyDatabaseBundle(source: Path, target: Path) {
        listOf("", "-wal", "-shm").forEach { suffix ->
            Files.copy(
                Path.of(source.toString() + suffix),
                Path.of(target.toString() + suffix),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }

    private fun openDatabase(path: Path): VaultDatabase =
        Room.databaseBuilder<VaultDatabase>(name = path.toString())
            .addVaultMigrations()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    private fun createDatabaseFromSchema(path: Path, version: Int) {
        BundledSQLiteDriver().open(path.toString()).use { connection ->
            val schema = exportedSchema(version)
            schema.entities.forEach { entity ->
                connection.execSQL(entity.createSql.replace("${'$'}{TABLE_NAME}", entity.tableName))
                entity.indices.forEach { sql ->
                    connection.execSQL(sql.replace("${'$'}{TABLE_NAME}", entity.tableName))
                }
            }
            schema.setupQueries.forEach(connection::execSQL)
            connection.execSQL("PRAGMA user_version = $version")
        }
    }

    private fun exportedSchema(version: Int): ExportedSchema {
        val resource = requireNotNull(
            VaultDatabaseBootstrapTest::class.java.classLoader
                .getResourceAsStream("com.passvault.core.database.VaultDatabase/$version.json"),
        )
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

    private fun queryLong(path: Path, sql: String): Long =
        BundledSQLiteDriver().open(path.toString()).use { connection ->
            connection.prepare(sql).use { statement ->
                check(statement.step())
                statement.getLong(0)
            }
        }

    private fun singleRecoveryDirectory(root: Path): Path = Files.list(root).use { entries ->
        val directories = entries.filter(Files::isDirectory).toList()
        assertEquals(1, directories.size)
        directories.single()
    }

    private suspend inline fun withTempDirectory(block: suspend (Path) -> Unit) {
        val directory = Files.createTempDirectory(
            Path.of(System.getProperty("user.dir")).toRealPath(),
            "passvault-database-bootstrap-",
        )
        try {
            block(directory)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    private enum class Damage {
        BAD_HEADER,
        TRUNCATED_PAGE,
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
        const val SQLITE_HEADER_BYTES = 100
        const val SQLITE_TABLE_LEAF_PAGE = 0x0d
        const val TRUNCATION_BYTES = 37L
        const val FILLER_ROWS = 2_000
        const val FILLER_BYTES = 200
    }
}
