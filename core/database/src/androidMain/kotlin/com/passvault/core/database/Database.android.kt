package com.passvault.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

/**
 * Android-specific database builder.
 */
fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<VaultDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("passvault.db")
    return Room.databaseBuilder<VaultDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

/**
 * Creates the database instance for Android.
 */
fun createDatabase(context: Context): VaultDatabase {
    return getDatabaseBuilder(context)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
