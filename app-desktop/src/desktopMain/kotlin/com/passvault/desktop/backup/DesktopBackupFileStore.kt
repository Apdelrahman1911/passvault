package com.passvault.desktop.backup

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.desktop_backup_open_title
import com.passvault.core.designsystem.generated.resources.desktop_backup_save_title
import com.passvault.core.database.backup.BackupContentSink
import com.passvault.core.database.backup.BackupContentSource
import com.passvault.core.database.backup.BackupLimits
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.hasOnlySafeSingleLineCodePoints
import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.backup.BackupFileSelectionCancelled
import com.passvault.feature.backup.BackupOutput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import java.awt.FileDialog
import java.awt.Frame
import java.awt.KeyboardFocusManager
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

class DesktopBackupFileStore : BackupFileStore {
    override suspend fun create(suggestedName: String): Result<BackupOutput> {
        val dialogTitle = getString(Res.string.desktop_backup_save_title)
        return try {
            val selected = withContext(Dispatchers.Swing) {
                val safeSuggestedName = safeDesktopBackupFileName(suggestedName, DEFAULT_BACKUP_NAME)
                    .takeIf { it.endsWith(BACKUP_EXTENSION, ignoreCase = true) }
                    ?: DEFAULT_BACKUP_NAME
                chooseFile(
                    FileDialog.SAVE,
                    safeSuggestedName,
                    dialogTitle,
                )
            } ?: return Result.failure(BackupFileSelectionCancelled())
            Result.success(createDesktopBackupOutput(selected))
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("The backup file could not be saved"))
        }
    }

    override suspend fun open(): Result<BackupFile> {
        val dialogTitle = getString(Res.string.desktop_backup_open_title)
        return try {
            val selected = withContext(Dispatchers.Swing) {
                chooseFile(FileDialog.LOAD, null, dialogTitle)
            } ?: return Result.failure(BackupFileSelectionCancelled())
            withContext(Dispatchers.IO) {
                val path = Path.of(selected).toAbsolutePath().normalize()
                require(Files.isRegularFile(path))
                Result.success(
                    BackupFile(
                        path.toString(),
                        safeDesktopBackupFileName(path.fileName.toString(), DEFAULT_IMPORT_NAME),
                    ),
                )
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("The backup file could not be opened"))
        }
    }

    override suspend fun source(file: BackupFile): Result<BackupContentSource> =
        withContext(Dispatchers.IO) {
            try {
                val path = Path.of(file.path).toAbsolutePath().normalize()
                require(Files.isRegularFile(path, java.nio.file.LinkOption.NOFOLLOW_LINKS))
                require(!Files.isSymbolicLink(path))
                val size = Files.size(path)
                require(size in 1..BackupLimits.MAX_BACKUP_BYTES)
                Result.success(DesktopBackupSource(path, size))
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                Result.failure(IllegalStateException("The backup file could not be read"))
            }
        }

    private fun chooseFile(mode: Int, suggestedName: String?, title: String): String? {
        val dialog = FileDialog(
            activeOwnerFrame(),
            title,
            mode,
        )
        dialog.file = suggestedName
        dialog.isMultipleMode = false
        return try {
            dialog.isVisible = true
            val directory = dialog.directory
            val file = dialog.file
            if (directory == null || file == null) null else Path.of(directory, file).toString()
        } finally {
            dialog.dispose()
        }
    }

    private fun activeOwnerFrame(): Frame? {
        val activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
        return activeWindow as? Frame ?: Frame.getFrames().firstOrNull { it.isActive }
    }

    private companion object {
        const val BACKUP_EXTENSION = ".pvault"
        const val DEFAULT_IMPORT_NAME = "backup"
    }
}

private class DesktopBackupSource(
    private val path: Path,
    override val declaredSizeBytes: Long,
) : BackupContentSource {
    private var input: InputStream? = null

    override suspend fun read(buffer: ByteArray): Int = withContext(Dispatchers.IO) {
        require(buffer.isNotEmpty())
        val stream = input ?: Files.newInputStream(path).also { input = it }
        stream.read(buffer).also { require(it == -1 || it in 1..buffer.size) }
    }

    override suspend fun rewind() = withContext(Dispatchers.IO) {
        input?.close()
        input = null
        require(Files.isRegularFile(path, java.nio.file.LinkOption.NOFOLLOW_LINKS))
        require(!Files.isSymbolicLink(path))
        require(Files.size(path) == declaredSizeBytes)
    }

    override suspend fun close() = withContext(NonCancellable + Dispatchers.IO) {
        input?.close()
        input = null
    }
}

internal class DesktopBackupSink(
    private val temporary: Path,
    private val target: Path,
) : BackupContentSink {
    private var output: OutputStream? = null
    private var byteCount = 0L
    private var finished = false

    override suspend fun write(buffer: ByteArray, byteCount: Int) = withContext(Dispatchers.IO) {
        check(!finished)
        require(byteCount in 0..buffer.size)
        this@DesktopBackupSink.byteCount += byteCount
        require(this@DesktopBackupSink.byteCount <= BackupLimits.MAX_BACKUP_BYTES)
        val stream = output ?: Files.newOutputStream(
            temporary,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING,
        ).also { output = it }
        stream.write(buffer, 0, byteCount)
    }

    override suspend fun commit() = withContext(NonCancellable + Dispatchers.IO) {
        check(!finished)
        output?.flush()
        output?.close()
        output = null
        try {
            Files.move(
                temporary,
                target,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
        }
        finished = true
        Unit
    }

    override suspend fun abort() = withContext(NonCancellable + Dispatchers.IO) {
        if (finished) return@withContext
        runCatching { output?.close() }
        output = null
        Files.deleteIfExists(temporary)
        finished = true
    }
}

/**
 * Creates the destination-directory temporary and retains ownership until the
 * dispatcher handoff has delivered the sink to its caller.
 */
internal suspend fun createDesktopBackupOutput(
    selected: String,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): BackupOutput {
    var unclaimedSink: DesktopBackupSink? = null
    return try {
        val output = withContext(ioDispatcher) {
            val selectedPath = Path.of(selected).toAbsolutePath().normalize()
            require(selectedPath.fileName.toString().isNotBlank())
            val parent = selectedPath.parent ?: Path.of(System.getProperty("user.dir"))
            Files.createDirectories(parent)
            val realParent = parent.toRealPath()
            val target = realParent.resolve(selectedPath.fileName.toString())
            require(!Files.isSymbolicLink(target))
            val sink = DesktopBackupSink(
                temporary = Files.createTempFile(realParent, ".passvault-", ".tmp"),
                target = target,
            )
            unclaimedSink = sink
            BackupOutput(
                file = BackupFile(
                    target.toString(),
                    safeDesktopBackupFileName(target.fileName.toString(), DEFAULT_BACKUP_NAME),
                ),
                sink = sink,
            )
        }
        currentCoroutineContext().ensureActive()
        unclaimedSink = null
        output
    } finally {
        unclaimedSink?.let { sink ->
            withContext(NonCancellable) { runCatching { sink.abort() } }
        }
    }
}

internal fun safeDesktopBackupFileName(name: String, fallback: String): String =
    name.takeIf {
        it.isNotBlank() &&
            it != "." &&
            it != ".." &&
            '/' !in it &&
            '\\' !in it &&
            it.codePointLength() <= MAX_DISPLAY_NAME_CODE_POINTS &&
            it.hasOnlySafeSingleLineCodePoints()
    } ?: fallback

private const val MAX_DISPLAY_NAME_CODE_POINTS = 160
private const val DEFAULT_BACKUP_NAME = "passvault-backup.pvault"
