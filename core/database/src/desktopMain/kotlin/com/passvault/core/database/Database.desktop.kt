package com.passvault.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission

/**
 * Desktop-specific database builder.
 */
fun getDatabaseBuilder(): RoomDatabase.Builder<VaultDatabase> {
    val dbFile = getDatabaseFile()
    return Room.databaseBuilder<VaultDatabase>(
        name = dbFile.absolutePath,
    )
}

/**
 * Gets the desktop database file location shipped by the first release.
 * Changing this path requires an explicit, transactional migration.
 */
private fun getDatabaseFile(): File {
    val appDataPath = Path.of(System.getProperty("user.home"), ".passvault")
    check(!Files.isSymbolicLink(appDataPath)) {
        "PassVault's private data path must not be a symbolic link"
    }
    Files.createDirectories(appDataPath)
    check(Files.isDirectory(appDataPath, LinkOption.NOFOLLOW_LINKS)) {
        "PassVault's private data path is not a directory"
    }

    // macOS and Linux commonly create application directories as 0755 under
    // the user's default umask. The database fields are encrypted, but the
    // directory still contains vault metadata and SQLite sidecars, so remove
    // all group/other access where POSIX permissions are available. Windows
    // inherits the user's profile ACL and does not expose this attribute view.
    Files.getFileAttributeView(appDataPath, PosixFileAttributeView::class.java)
        ?.setPermissions(
            setOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
            ),
    )

    val databasePath = appDataPath.resolve("vault.db")
    DATABASE_FILE_SUFFIXES.forEach { suffix ->
        check(!Files.isSymbolicLink(Path.of(databasePath.toString() + suffix))) {
            "PassVault's database files must not be symbolic links"
        }
    }
    return databasePath.toFile()
}

/**
 * Creates the database instance for Desktop.
 */
fun createDatabase(): VaultDatabase {
    return getDatabaseBuilder()
        .addVaultMigrations()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

private val DATABASE_FILE_SUFFIXES = listOf("", "-wal", "-shm", "-journal")
