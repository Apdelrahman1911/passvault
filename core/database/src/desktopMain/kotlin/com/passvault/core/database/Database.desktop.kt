package com.passvault.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import java.io.File

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
 * Gets the desktop database file location.
 * Uses platform-specific app data directory.
 */
private fun getDatabaseFile(): File {
    val appDataDir = when {
        System.getProperty("os.name").contains("Mac", ignoreCase = true) -> {
            File(System.getProperty("user.home"), "Library/Application Support/PassVault")
        }
        System.getProperty("os.name").contains("Windows", ignoreCase = true) -> {
            File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "PassVault")
        }
        else -> {
            // Linux and other Unix-like systems
            File(System.getProperty("user.home"), ".config/passvault")
        }
    }

    if (!appDataDir.exists()) {
        appDataDir.mkdirs()
    }

    return File(appDataDir, "passvault.db")
}

/**
 * Creates the database instance for Desktop.
 */
fun createDatabase(): VaultDatabase {
    return getDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
