package com.passvault.core.database

import androidx.room.PooledConnection
import androidx.room.useReaderConnection
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_NOFOLLOW
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.time.Clock

/** Result of opening and structurally checking the application database. */
sealed interface VaultDatabaseBootstrapResult {
    data object Ready : VaultDatabaseBootstrapResult

    /** The database is structurally damaged and must not be used. */
    data class RecoveryRequired(
        /** True only while the Room database has not opened the damaged files. */
        val canPreserveAndReset: Boolean,
    ) : VaultDatabaseBootstrapResult

    /** Storage, schema migration, or another non-corruption open failure. */
    data object Unavailable : VaultDatabaseBootstrapResult
}

/**
 * Opens the Room database behind a one-time structural health gate.
 *
 * Existing files are checked through a separate read-only SQLite connection before Room can run
 * migrations or issue application queries. Room is then opened for real and checked again. The
 * same lazily-created Room instance backs every DAO, so dependency resolution cannot create an
 * unchecked second Room handle before the Compose bootstrap screen is shown.
 */
class VaultDatabaseBootstrap internal constructor(
    private val storage: VaultDatabaseStorage,
    databaseFactory: () -> VaultDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val databaseDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED, databaseFactory)
    private val mutex = Mutex()
    private var ready = false
    private var recoveryCanPreserveAndReset = false

    /** Returns the stable, lazily-built Room instance used by dependency injection. */
    fun database(): VaultDatabase = databaseDelegate.value

    /** Runs at most one successful check per process; failed checks can be retried. */
    @Suppress("TooGenericExceptionCaught") // SQLite exposes platform-specific exception implementations.
    suspend fun openAndVerify(): VaultDatabaseBootstrapResult = mutex.withLock {
        if (ready) return@withLock VaultDatabaseBootstrapResult.Ready
        withContext(dispatcher) {
            try {
                storage.prepareForOpen()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                return@withContext publishFailure(VaultDatabaseFailure.Unavailable, beforeRoomOpen = true)
            }
            val preflight = inspectExistingFile()
            if (preflight != null) return@withContext publishFailure(preflight, beforeRoomOpen = true)

            try {
                database().useReaderConnection { connection -> requireQuickCheck(connection) }
                ready = true
                recoveryCanPreserveAndReset = false
                VaultDatabaseBootstrapResult.Ready
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                publishFailure(classifyOpenFailure(error), beforeRoomOpen = false)
            }
        }
    }

    /**
     * Moves the damaged database bundle and encrypted attachment store into app-private recovery
     * storage. Nothing is deleted or repaired. A fresh database is created only by a later
     * [openAndVerify] call.
     */
    suspend fun preserveAndReset(): Result<Unit> = mutex.withLock {
        withContext(dispatcher) {
            if (!recoveryCanPreserveAndReset) {
                return@withContext Result.failure(
                    IllegalStateException("The database is not eligible for safe preservation"),
                )
            }
            try {
                storage.preserveForRecovery()
                recoveryCanPreserveAndReset = false
                storage.record(VaultDatabaseDiagnosticCode.RECOVERY_COPY_PRESERVED)
                Result.success(Unit)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                storage.record(VaultDatabaseDiagnosticCode.RECOVERY_PRESERVATION_FAILED)
                Result.failure(IllegalStateException("The database could not be preserved"))
            }
        }
    }

    /** Closes the Room instance if any platform component caused it to be constructed. */
    fun close() {
        if (databaseDelegate.isInitialized()) databaseDelegate.value.close()
    }

    @Suppress("TooGenericExceptionCaught") // SQLite exposes platform-specific exception implementations.
    private fun inspectExistingFile(): VaultDatabaseFailure? {
        if (!storage.databaseExists()) return null
        return try {
            BundledSQLiteDriver().open(
                storage.databasePath,
                SQLITE_OPEN_READONLY or SQLITE_OPEN_NOFOLLOW,
            ).use { connection ->
                requireQuickCheck(connection)
            }
            null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            classifyOpenFailure(error)
        }
    }

    private fun publishFailure(
        failure: VaultDatabaseFailure,
        beforeRoomOpen: Boolean,
    ): VaultDatabaseBootstrapResult {
        storage.record(failure.diagnosticCode)
        return when (failure) {
            is VaultDatabaseFailure.Corrupt -> {
                recoveryCanPreserveAndReset = beforeRoomOpen
                VaultDatabaseBootstrapResult.RecoveryRequired(
                    canPreserveAndReset = recoveryCanPreserveAndReset,
                )
            }
            VaultDatabaseFailure.Unavailable -> {
                recoveryCanPreserveAndReset = false
                VaultDatabaseBootstrapResult.Unavailable
            }
        }
    }
}

internal interface VaultDatabaseStorage {
    val databasePath: String
    fun prepareForOpen()
    fun databaseExists(): Boolean
    fun preserveForRecovery()
    fun record(code: VaultDatabaseDiagnosticCode)
}

internal enum class VaultDatabaseDiagnosticCode {
    QUICK_CHECK_FAILED,
    INVALID_DATABASE_FILE,
    DATABASE_OPEN_FAILED,
    RECOVERY_COPY_PRESERVED,
    RECOVERY_PRESERVATION_FAILED,
}

private sealed interface VaultDatabaseFailure {
    val diagnosticCode: VaultDatabaseDiagnosticCode

    data class Corrupt(
        override val diagnosticCode: VaultDatabaseDiagnosticCode,
    ) : VaultDatabaseFailure

    data object Unavailable : VaultDatabaseFailure {
        override val diagnosticCode = VaultDatabaseDiagnosticCode.DATABASE_OPEN_FAILED
    }
}

private class QuickCheckFailedException : IllegalStateException("Vault database integrity check failed")

private fun requireQuickCheck(connection: SQLiteConnection) {
    connection.prepare("PRAGMA quick_check(1)").use { statement ->
        val healthy = statement.step() && statement.getText(0) == QUICK_CHECK_OK
        val unexpectedAdditionalRow = statement.step()
        if (!healthy || unexpectedAdditionalRow) throw QuickCheckFailedException()
    }
}

private suspend fun requireQuickCheck(connection: PooledConnection) {
    connection.usePrepared("PRAGMA quick_check(1)") { statement ->
        val healthy = statement.step() && statement.getText(0) == QUICK_CHECK_OK
        val unexpectedAdditionalRow = statement.step()
        if (!healthy || unexpectedAdditionalRow) throw QuickCheckFailedException()
    }
}

private fun classifyOpenFailure(error: Throwable): VaultDatabaseFailure = when {
    error.hasCause<QuickCheckFailedException>() -> VaultDatabaseFailure.Corrupt(
        VaultDatabaseDiagnosticCode.QUICK_CHECK_FAILED,
    )
    error.sqlitePrimaryResultCode() in CORRUPTION_RESULT_CODES -> VaultDatabaseFailure.Corrupt(
        VaultDatabaseDiagnosticCode.INVALID_DATABASE_FILE,
    )
    else -> VaultDatabaseFailure.Unavailable
}

private inline fun <reified T : Throwable> Throwable.hasCause(): Boolean =
    causeSequence().any { it is T }

private fun Throwable.sqlitePrimaryResultCode(): Int? = causeSequence()
    .mapNotNull { error ->
        SQLITE_ERROR_CODE.find(error.message.orEmpty())
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
    }
    .firstOrNull()
    ?.and(SQLITE_PRIMARY_RESULT_CODE_MASK)

private fun Throwable.causeSequence(): Sequence<Throwable> = sequence {
    var current: Throwable? = this@causeSequence
    var depth = 0
    while (current != null && depth < MAX_CAUSE_DEPTH) {
        yield(current)
        current = current.cause.takeUnless { it === current }
        depth++
    }
}

/** Okio-backed recovery storage shared by all supported platforms. */
internal class LocalVaultDatabaseStorage(
    override val databasePath: String,
    attachmentRootPath: String,
    databaseRecoveryRootPath: String,
    attachmentRecoveryRootPath: String,
    diagnosticPath: String,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val clock: Clock = Clock.System,
    private val protectPath: (String) -> Unit = {},
) : VaultDatabaseStorage {
    private val database = databasePath.toPath(normalize = true)
    private val attachmentRoot = attachmentRootPath.toPath(normalize = true)
    private val databaseRecoveryRoot = databaseRecoveryRootPath.toPath(normalize = true)
    private val attachmentRecoveryRoot = attachmentRecoveryRootPath.toPath(normalize = true)
    private val diagnosticFile = diagnosticPath.toPath(normalize = true)

    override fun prepareForOpen() {
        restoreInterruptedPreservations()
        if (fileSystem.metadataOrNull(database) == null) {
            check(DATABASE_SIDECAR_SUFFIXES.none { suffix ->
                fileSystem.metadataOrNull(databasePath.plus(suffix).toPath(normalize = true)) != null
            }) {
                "An orphaned SQLite sidecar cannot be opened as a fresh database"
            }
        }
    }

    override fun databaseExists(): Boolean = fileSystem.metadataOrNull(database) != null

    override fun preserveForRecovery() {
        requireRegularFile(database)
        val token = nextRecoveryToken()
        val databaseDestination = prepareDirectory(databaseRecoveryRoot / token)
        val attachmentDestination = if (attachmentRecoveryRoot == databaseRecoveryRoot) {
            databaseDestination
        } else {
            prepareDirectory(attachmentRecoveryRoot / token)
        }
        val moves = buildList {
            val attachmentMetadata = fileSystem.metadataOrNull(attachmentRoot)
            if (attachmentMetadata != null) {
                require(attachmentMetadata.symlinkTarget == null && attachmentMetadata.isDirectory) {
                    "Attachment recovery source is not a real directory"
                }
                add(attachmentRoot to (attachmentDestination / ATTACHMENT_DIRECTORY_NAME))
            }
            DATABASE_SIDECAR_SUFFIXES.forEach { suffix ->
                val source = databasePath.plus(suffix).toPath(normalize = true)
                val metadata = fileSystem.metadataOrNull(source)
                if (metadata != null) {
                    require(metadata.symlinkTarget == null && metadata.isRegularFile) {
                        "Database recovery source is not a regular file"
                    }
                    add(source to (databaseDestination / (database.name + suffix)))
                }
            }
            // Moving the main file last makes it the preservation commit marker.
            add(database to (databaseDestination / database.name))
        }
        moveWithRollback(moves)
    }

    @Suppress("TooGenericExceptionCaught") // Diagnostics are best effort and never replace the real failure.
    override fun record(code: VaultDatabaseDiagnosticCode) {
        try {
            val parent = requireNotNull(diagnosticFile.parent)
            prepareDirectory(parent)
            val metadata = fileSystem.metadataOrNull(diagnosticFile)
            require(metadata == null || (metadata.symlinkTarget == null && metadata.isRegularFile)) {
                "The database diagnostic path must be a regular file"
            }
            val diagnosticSize = metadata?.size
            val retained = if (
                metadata == null ||
                diagnosticSize == null ||
                diagnosticSize > MAX_DIAGNOSTIC_FILE_BYTES
            ) {
                emptyList()
            } else {
                fileSystem.source(diagnosticFile).buffer().use { source ->
                    source.readUtf8()
                        .lineSequence()
                        .filter(DIAGNOSTIC_LINE::matches)
                        .toList()
                        .takeLast(MAX_RETAINED_DIAGNOSTICS - 1)
                }
            }
            fileSystem.sink(diagnosticFile).buffer().use { sink ->
                (retained + "${clock.now().toEpochMilliseconds()} ${code.name}").forEach { line ->
                    sink.writeUtf8(line)
                    sink.writeByte('\n'.code)
                }
            }
            protectPath(diagnosticFile.toString())
        } catch (_: Exception) {
            // Health classification must still reach the UI if diagnostics cannot be persisted.
        }
    }

    private fun nextRecoveryToken(): String {
        val epoch = clock.now().toEpochMilliseconds()
        repeat(MAX_RECOVERY_TOKEN_ATTEMPTS) { counter ->
            val token = "$epoch-$counter"
            if (
                fileSystem.metadataOrNull(databaseRecoveryRoot / token) == null &&
                fileSystem.metadataOrNull(attachmentRecoveryRoot / token) == null
            ) {
                return token
            }
        }
        error("A unique recovery directory could not be allocated")
    }

    private fun restoreInterruptedPreservations() {
        val rootMetadata = fileSystem.metadataOrNull(databaseRecoveryRoot) ?: return
        require(rootMetadata.symlinkTarget == null && rootMetadata.isDirectory) {
            "Database recovery storage must be a real directory"
        }
        fileSystem.list(databaseRecoveryRoot)
            .filter { candidate -> RECOVERY_TOKEN.matches(candidate.name) }
            .forEach { databaseRecovery ->
                val recoveryMetadata = fileSystem.metadata(databaseRecovery)
                require(recoveryMetadata.symlinkTarget == null && recoveryMetadata.isDirectory) {
                    "Database recovery entries must be real directories"
                }
                if (fileSystem.metadataOrNull(databaseRecovery / database.name) != null) return@forEach

                val attachmentRecovery = attachmentRecoveryRoot / databaseRecovery.name
                val restores = buildList {
                    DATABASE_SIDECAR_SUFFIXES.forEach { suffix ->
                        val preserved = databaseRecovery / (database.name + suffix)
                        if (fileSystem.metadataOrNull(preserved) != null) {
                            add(preserved to databasePath.plus(suffix).toPath(normalize = true))
                        }
                    }
                    val preservedAttachments = attachmentRecovery / ATTACHMENT_DIRECTORY_NAME
                    if (fileSystem.metadataOrNull(preservedAttachments) != null) {
                        add(preservedAttachments to attachmentRoot)
                    }
                }
                moveWithRollback(restores)
            }
    }

    private fun prepareDirectory(path: Path): Path {
        fileSystem.createDirectories(path)
        val metadata = fileSystem.metadata(path)
        require(metadata.symlinkTarget == null && metadata.isDirectory) {
            "Recovery storage must be a real directory"
        }
        protectPath(path.toString())
        return path
    }

    private fun requireRegularFile(path: Path) {
        val metadata = fileSystem.metadata(path)
        require(metadata.symlinkTarget == null && metadata.isRegularFile) {
            "Database recovery source is not a regular file"
        }
    }

    @Suppress("TooGenericExceptionCaught") // Every failed move/protection operation requires compensation.
    private fun moveWithRollback(moves: List<Pair<Path, Path>>) {
        val moved = mutableListOf<Pair<Path, Path>>()
        try {
            moves.forEach { (source, destination) ->
                check(fileSystem.metadataOrNull(destination) == null) {
                    "Recovery destination already exists"
                }
                fileSystem.atomicMove(source, destination)
                moved += source to destination
                protectPath(destination.toString())
            }
        } catch (failure: Exception) {
            var rollbackFailure: Exception? = null
            moved.asReversed().forEach { (source, destination) ->
                try {
                    if (fileSystem.metadataOrNull(destination) != null) {
                        fileSystem.atomicMove(destination, source)
                    }
                } catch (error: Exception) {
                    rollbackFailure = rollbackFailure?.apply { addSuppressed(error) } ?: error
                }
            }
            rollbackFailure?.let(failure::addSuppressed)
            throw failure
        }
    }
}

private const val QUICK_CHECK_OK = "ok"
private const val SQLITE_PRIMARY_RESULT_CODE_MASK = 0xff
private const val SQLITE_CORRUPT = 11
private const val SQLITE_NOTADB = 26
private const val MAX_CAUSE_DEPTH = 16
private const val MAX_RETAINED_DIAGNOSTICS = 16
private const val MAX_DIAGNOSTIC_FILE_BYTES = 4_096L
private const val MAX_RECOVERY_TOKEN_ATTEMPTS = 100
private const val ATTACHMENT_DIRECTORY_NAME = "attachments"
private val CORRUPTION_RESULT_CODES = setOf(SQLITE_CORRUPT, SQLITE_NOTADB)
private val SQLITE_ERROR_CODE = Regex("Error code: ([0-9]+)")
private val DIAGNOSTIC_LINE = Regex(
    "[0-9]{1,20} (QUICK_CHECK_FAILED|INVALID_DATABASE_FILE|DATABASE_OPEN_FAILED|" +
        "RECOVERY_COPY_PRESERVED|RECOVERY_PRESERVATION_FAILED)",
)
private val RECOVERY_TOKEN = Regex("[0-9]{1,20}-[0-9]{1,3}")
private val DATABASE_SIDECAR_SUFFIXES = listOf("-wal", "-shm", "-journal")
