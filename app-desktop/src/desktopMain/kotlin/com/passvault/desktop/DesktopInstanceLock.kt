package com.passvault.desktop

import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission

/**
 * Holds exclusive ownership of the Desktop vault data directory for one process.
 *
 * The operating system releases the lock if this process crashes. The lock file is deliberately
 * retained so later launches can validate its type before attempting to acquire it.
 */
internal class DesktopInstanceLock private constructor(
    private val channel: FileChannel,
    private val lock: FileLock,
) : AutoCloseable {
    override fun close() {
        runCatching { lock.release() }
        runCatching { channel.close() }
    }

    internal companion object {
        @Suppress("TooGenericExceptionCaught") // Release the channel before propagating any setup or lock error.
        fun acquire(dataDirectory: Path = defaultDataDirectory()): DesktopInstanceLock? {
            prepareDataDirectory(dataDirectory)
            val lockPath = dataDirectory.resolve(INSTANCE_LOCK_FILE)
            check(!Files.isSymbolicLink(lockPath)) {
                "PassVault's instance lock must not be a symbolic link"
            }

            val channel = FileChannel.open(
                lockPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                LinkOption.NOFOLLOW_LINKS,
            )
            return try {
                check(Files.isRegularFile(lockPath, LinkOption.NOFOLLOW_LINKS)) {
                    "PassVault's instance lock is not a regular file"
                }
                protectLockFile(lockPath)
                val lock = try {
                    channel.tryLock()
                } catch (_: OverlappingFileLockException) {
                    null
                }
                lock?.let { DesktopInstanceLock(channel, it) }
            } catch (error: Exception) {
                runCatching { channel.close() }
                throw error
            }.also { acquired ->
                if (acquired == null) channel.close()
            }
        }

        private fun defaultDataDirectory(): Path =
            Path.of(System.getProperty("user.home"), PASSVAULT_DATA_DIRECTORY)

        private fun prepareDataDirectory(directory: Path) {
            check(!Files.isSymbolicLink(directory)) {
                "PassVault's private data path must not be a symbolic link"
            }
            Files.createDirectories(directory)
            check(Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
                "PassVault's private data path is not a directory"
            }
            Files.getFileAttributeView(directory, PosixFileAttributeView::class.java)
                ?.setPermissions(OWNER_ONLY_DIRECTORY_PERMISSIONS)
        }

        private fun protectLockFile(lockPath: Path) {
            Files.getFileAttributeView(lockPath, PosixFileAttributeView::class.java)
                ?.setPermissions(OWNER_ONLY_FILE_PERMISSIONS)
        }
    }
}

private const val PASSVAULT_DATA_DIRECTORY = ".passvault"
private const val INSTANCE_LOCK_FILE = ".instance.lock"

private val OWNER_ONLY_DIRECTORY_PERMISSIONS = setOf(
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE,
    PosixFilePermission.OWNER_EXECUTE,
)

private val OWNER_ONLY_FILE_PERMISSIONS = setOf(
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE,
)
