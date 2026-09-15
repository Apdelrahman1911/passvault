@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileProtectionComplete
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUserDomainMask

/**
 * iOS-specific database builder using one private, backup-excluded location.
 */
private fun getDatabaseBuilder(): RoomDatabase.Builder<VaultDatabase> {
    val dbFilePath = secureDatabasePath()
    return Room.databaseBuilder<VaultDatabase>(
        name = dbFilePath,
    )
}

private fun secureDatabasePath(): String {
    val fileManager = NSFileManager.defaultManager
    val applicationSupport = fileManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )?.URLByResolvingSymlinksInPath?.path
        ?: error("The iOS Application Support directory is unavailable")
    val databaseDirectory = "$applicationSupport/PassVault"
    check(
        fileManager.createDirectoryAtPath(
            path = databaseDirectory,
            withIntermediateDirectories = true,
            attributes = iosDatabaseProtectionAttributes(),
            error = null,
        ),
    ) { "The private iOS database directory could not be created" }
    check(
        fileManager.setAttributes(
            iosDatabaseProtectionAttributes(),
            ofItemAtPath = databaseDirectory,
            error = null,
        ),
    ) {
        "The private iOS database directory could not be protected"
    }

    excludeFromBackup(databaseDirectory)

    val databasePath = "$databaseDirectory/passvault.db"
    migrateLegacyDatabase(fileManager, legacyDatabasePath(fileManager), databasePath)
    protectExistingDatabaseFiles(fileManager, databasePath)
    return databasePath
}

/**
 * Moves files without opening them. WAL and SHM move before the main database,
 * so the main file is the migration commit marker. A launch interrupted before
 * that final move can safely continue on the next launch.
 */
private fun migrateLegacyDatabase(
    fileManager: NSFileManager,
    legacyPath: String,
    destinationPath: String,
) {
    if (fileManager.fileExistsAtPath(destinationPath)) return
    if (!fileManager.fileExistsAtPath(legacyPath)) return

    val movedThisAttempt = mutableListOf<Pair<String, String>>()
    try {
        IOS_DATABASE_SUFFIXES.forEach { suffix ->
            val source = legacyPath + suffix
            val destination = destinationPath + suffix
            val sourceExists = fileManager.fileExistsAtPath(source)
            val destinationExists = fileManager.fileExistsAtPath(destination)
            check(!(sourceExists && destinationExists)) {
                "The iOS database migration found conflicting files"
            }
            if (sourceExists) {
                check(fileManager.moveItemAtPath(source, destination, error = null)) {
                    "The iOS database could not be moved to private storage"
                }
                movedThisAttempt += source to destination
            }
        }
        check(fileManager.fileExistsAtPath(destinationPath)) {
            "The iOS database migration did not complete"
        }
    } catch (error: IllegalStateException) {
        movedThisAttempt.asReversed().forEach { (source, destination) ->
            if (!fileManager.fileExistsAtPath(source) && fileManager.fileExistsAtPath(destination)) {
                fileManager.moveItemAtPath(destination, source, error = null)
            }
        }
        throw error
    }
}

private fun legacyDatabasePath(fileManager: NSFileManager): String {
    val documents = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )?.path ?: error("The iOS Documents directory is unavailable")
    return "$documents/passvault.db"
}

private fun protectExistingDatabaseFiles(fileManager: NSFileManager, databasePath: String) {
    IOS_DATABASE_SUFFIXES.forEach { suffix ->
        val path = databasePath + suffix
        if (fileManager.fileExistsAtPath(path)) {
            check(fileManager.setAttributes(iosDatabaseProtectionAttributes(), ofItemAtPath = path, error = null)) {
                "The iOS database protection attributes could not be applied"
            }
            excludeFromBackup(path)
        }
    }
}

/**
 * Complete protection intentionally makes open database files unavailable after device lock.
 * The Swift host must therefore dismantle the Compose runtime and checkpoint/close Room on the
 * protected-data notification, then create a fresh runtime only after data becomes available.
 */
internal fun iosDatabaseProtectionAttributes(): Map<Any?, *> = mapOf(
    NSFileProtectionKey to NSFileProtectionComplete,
)

private fun excludeFromBackup(path: String) {
    val url = NSURL.fileURLWithPath(path)
    check(url.setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)) {
        "The iOS database could not be excluded from device backups"
    }
}

/**
 * Creates the database instance for iOS.
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
    val databasePath = secureDatabasePath()
    val appDataPath = databasePath.substringBeforeLast('/')
    val recoveryRoot = "$appDataPath/$RECOVERY_DIRECTORY"
    return VaultDatabaseBootstrap(
        storage = LocalVaultDatabaseStorage(
            databasePath = databasePath,
            attachmentRootPath = "$appDataPath/$ATTACHMENT_DIRECTORY",
            databaseRecoveryRootPath = recoveryRoot,
            attachmentRecoveryRootPath = recoveryRoot,
            diagnosticPath = "$appDataPath/$DATABASE_DIAGNOSTIC_FILE",
            protectPath = ::protectRecoveryPath,
        ),
        databaseFactory = ::createDatabase,
    )
}

private fun protectRecoveryPath(path: String) {
    val fileManager = NSFileManager.defaultManager
    check(fileManager.setAttributes(iosDatabaseProtectionAttributes(), ofItemAtPath = path, error = null)) {
        "The iOS recovery data could not be protected"
    }
    excludeFromBackup(path)
}

internal val IOS_DATABASE_SUFFIXES = listOf("-wal", "-shm", "-journal", "")
private const val ATTACHMENT_DIRECTORY = "attachments"
private const val RECOVERY_DIRECTORY = "recovery"
private const val DATABASE_DIAGNOSTIC_FILE = "database-health.events"
