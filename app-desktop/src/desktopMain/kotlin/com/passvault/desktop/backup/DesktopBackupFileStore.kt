package com.passvault.desktop.backup

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.backup.BackupFileSelectionCancelled
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

class DesktopBackupFileStore : BackupFileStore {
    override suspend fun save(bytes: ByteArray, suggestedName: String): Result<BackupFile> {
        val dialogTitle = getString(Res.string.desktop_backup_save_title)
        return withContext(Dispatchers.IO) {
            try {
                val selected = chooseFile(FileDialog.SAVE, suggestedName, dialogTitle)
                    ?: return@withContext Result.failure(BackupFileSelectionCancelled())
                val target = Path.of(selected).toAbsolutePath().normalize()
                require(target.fileName.toString().isNotBlank())
                val parent = target.parent ?: Path.of(System.getProperty("user.dir"))
                Files.createDirectories(parent)
                val temporary = Files.createTempFile(parent, ".passvault-", ".tmp")
                try {
                    Files.write(
                        temporary,
                        bytes,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                    )
                    try {
                        Files.move(
                            temporary,
                            target,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING,
                        )
                    } catch (_: Exception) {
                        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
                    }
                } finally {
                    Files.deleteIfExists(temporary)
                }
                Result.success(BackupFile(target.toString(), target.fileName.toString()))
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                Result.failure(IllegalStateException("The backup file could not be saved"))
            }
        }
    }

    override suspend fun open(): Result<BackupFile> {
        val dialogTitle = getString(Res.string.desktop_backup_open_title)
        return withContext(Dispatchers.IO) {
            try {
                val selected = chooseFile(FileDialog.LOAD, null, dialogTitle)
                    ?: return@withContext Result.failure(BackupFileSelectionCancelled())
                val path = Path.of(selected).toAbsolutePath().normalize()
                require(Files.isRegularFile(path))
                Result.success(BackupFile(path.toString(), path.fileName.toString()))
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                Result.failure(IllegalStateException("The backup file could not be opened"))
            }
        }
    }

    override suspend fun read(file: BackupFile): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val path = Path.of(file.path).toAbsolutePath().normalize()
            require(Files.isRegularFile(path))
            require(Files.size(path) <= MAX_BACKUP_BYTES)
            Result.success(Files.readAllBytes(path))
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("The backup file could not be read"))
        }
    }

    private fun chooseFile(mode: Int, suggestedName: String?, title: String): String? {
        val dialog = FileDialog(
            null as Frame?,
            title,
            mode,
        )
        dialog.file = suggestedName
        dialog.isMultipleMode = false
        dialog.isVisible = true
        val directory = dialog.directory ?: return null
        val file = dialog.file ?: return null
        return Path.of(directory, file).toString()
    }

    private companion object {
        const val MAX_BACKUP_BYTES = 128L * 1024L * 1024L
    }
}
