package com.passvault.core.database

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Non-destructive migrations for every database version shipped by PassVault.
 *
 * Keep each version step explicit. This makes intermediate upgrades testable
 * and prevents a later schema change from silently altering an older path.
 */
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_folder_records_name_hash` " +
                "ON `folder_records` (`name_hash`)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_tag_records_name_hash` " +
                "ON `tag_records` (`name_hash`)",
        )
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE `attachment_records` ADD COLUMN " +
                "`content_format_version` INTEGER NOT NULL DEFAULT 0",
        )
        connection.execSQL(
            "ALTER TABLE `attachment_records` ADD COLUMN " +
                "`storage_state` TEXT NOT NULL DEFAULT 'LEGACY'",
        )
    }
}

internal fun RoomDatabase.Builder<VaultDatabase>.addVaultMigrations(): RoomDatabase.Builder<VaultDatabase> =
    addMigrations(MIGRATION_1_2, MIGRATION_2_3)
