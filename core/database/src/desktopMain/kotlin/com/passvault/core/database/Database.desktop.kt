package com.passvault.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.desktop.security.createOrHardenPrivateDesktopDirectory
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
private fun getDatabaseBuilder(): RoomDatabase.Builder<VaultDatabase> {
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
    val resolvedAppDataPath = createOrHardenPrivateDesktopDirectory(appDataPath)

    // SQLite's NOFOLLOW open mode rejects a path when any component is a symbolic link. Resolve
    // trusted ancestors after proving the application directory itself is not a link.
    val databasePath = resolvedAppDataPath.resolve("vault.db")
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
private fun createDatabase(): VaultDatabase {
    return getDatabaseBuilder()
        .addVaultMigrations()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

/** Creates the process-wide health gate and its lazily-opened Room database. */
fun createDatabaseBootstrap(): VaultDatabaseBootstrap {
    val database = getDatabaseFile().toPath()
    val appDataPath = requireNotNull(database.parent)
    val recoveryRoot = appDataPath.resolve(RECOVERY_DIRECTORY)
    return VaultDatabaseBootstrap(
        storage = LocalVaultDatabaseStorage(
            databasePath = database.toString(),
            attachmentRootPath = appDataPath.resolve(ATTACHMENT_DIRECTORY).toString(),
            databaseRecoveryRootPath = recoveryRoot.toString(),
            attachmentRecoveryRootPath = recoveryRoot.toString(),
            diagnosticPath = appDataPath.resolve(DATABASE_DIAGNOSTIC_FILE).toString(),
            protectPath = ::hardenRecoveryPath,
        ),
        databaseFactory = ::createDatabase,
    )
}

private fun hardenRecoveryPath(rawPath: String) {
    val path = Path.of(rawPath)
    val permissions = if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
        setOf(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE,
        )
    } else {
        setOf(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
        )
    }
    Files.getFileAttributeView(path, PosixFileAttributeView::class.java)
        ?.setPermissions(permissions)
}

private val DATABASE_FILE_SUFFIXES = listOf("", "-wal", "-shm", "-journal")
private const val ATTACHMENT_DIRECTORY = "attachments"
private const val RECOVERY_DIRECTORY = "recovery"
private const val DATABASE_DIAGNOSTIC_FILE = "database-health.events"
