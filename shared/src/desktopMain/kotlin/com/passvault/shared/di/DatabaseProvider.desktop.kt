package com.passvault.shared.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.database.VaultDatabase
import java.io.File

actual fun createDatabase(context: Any): VaultDatabase {
    val dbDir = File(System.getProperty("user.home"), ".passvault")
    if (!dbDir.exists()) {
        dbDir.mkdirs()
    }
    val dbFile = File(dbDir, "vault.db")

    return Room.databaseBuilder<VaultDatabase>(
        name = dbFile.absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        .build()
}
