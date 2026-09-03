package com.passvault.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlinx.coroutines.Dispatchers

/**
 * Android-specific database builder.
 */
private fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<VaultDatabase> {
    val dbFile = databaseFile(context.applicationContext)
    return Room.databaseBuilder<VaultDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath,
    )
}

/**
 * Creates the database instance for Android.
 */
private fun createDatabase(context: Context): VaultDatabase {
    return getDatabaseBuilder(context)
        .addVaultMigrations()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

/** Creates the process-wide health gate and its lazily-opened Room database. */
fun createDatabaseBootstrap(context: Context): VaultDatabaseBootstrap {
    val appContext = context.applicationContext
    val database = databaseFile(appContext)
    val noBackupRoot = appContext.noBackupFilesDir.canonicalFile
    return VaultDatabaseBootstrap(
        storage = LocalVaultDatabaseStorage(
            databasePath = database.absolutePath,
            attachmentRootPath = File(noBackupRoot, ATTACHMENT_DIRECTORY).absolutePath,
            databaseRecoveryRootPath = File(appContext.filesDir, RECOVERY_DIRECTORY).absolutePath,
            attachmentRecoveryRootPath = File(noBackupRoot, RECOVERY_DIRECTORY).absolutePath,
            diagnosticPath = File(noBackupRoot, DATABASE_DIAGNOSTIC_FILE).absolutePath,
        ),
        databaseFactory = { createDatabase(appContext) },
    )
}

private fun databaseFile(context: Context): File {
    // This private filesDir location is the path shipped by the first release.
    // Moving it requires an explicit, transactional migration.
    // Resolve trusted ancestors because SQLite's NOFOLLOW mode rejects a path containing links.
    return File(context.filesDir.canonicalFile, "passvault.db")
}

private const val ATTACHMENT_DIRECTORY = "attachments"
private const val RECOVERY_DIRECTORY = "vault-recovery"
private const val DATABASE_DIAGNOSTIC_FILE = "database-health.events"
