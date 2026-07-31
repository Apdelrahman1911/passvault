package com.passvault.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS-specific database builder.
 */
fun getDatabaseBuilder(): RoomDatabase.Builder<VaultDatabase> {
    val dbFilePath = documentDirectory() + "/passvault.db"
    return Room.databaseBuilder<VaultDatabase>(
        name = dbFilePath,
    )
}

/**
 * Gets the iOS documents directory.
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}

/**
 * Creates the database instance for iOS.
 */
fun createDatabase(): VaultDatabase {
    return getDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
