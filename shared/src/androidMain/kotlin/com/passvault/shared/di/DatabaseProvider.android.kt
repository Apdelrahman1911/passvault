package com.passvault.shared.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.passvault.core.database.VaultDatabase
import java.io.File

actual fun createDatabase(context: Any): VaultDatabase {
    val appContext = context as Context
    val dbFile = File(appContext.filesDir, "passvault.db")
    
    return Room.databaseBuilder<VaultDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .build()
}
