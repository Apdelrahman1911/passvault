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

/**
 * Removes the unused title blind index without losing rows that reference a
 * credential. SQLite versions on supported Android releases cannot drop a
 * column in place, so dependent rows are staged while the parent and its
 * foreign-key tables are rebuilt.
 */
val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    @Suppress("LongMethod") // Keep the atomic parent/dependent-table rebuild auditable in execution order.
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TEMP TABLE `_pv50_credential_folder_cross_ref` AS " +
                "SELECT `credential_id`, `folder_id` FROM `credential_folder_cross_ref`",
        )
        connection.execSQL(
            "CREATE TEMP TABLE `_pv50_credential_tag_cross_ref` AS " +
                "SELECT `credential_id`, `tag_id` FROM `credential_tag_cross_ref`",
        )
        connection.execSQL(
            "CREATE TEMP TABLE `_pv50_attachment_records` AS " +
                "SELECT * FROM `attachment_records`",
        )
        connection.execSQL(
            "CREATE TEMP TABLE `_pv50_password_history_records` AS " +
                "SELECT * FROM `password_history_records`",
        )

        connection.execSQL("DROP TABLE `credential_folder_cross_ref`")
        connection.execSQL("DROP TABLE `credential_tag_cross_ref`")
        connection.execSQL("DROP TABLE `attachment_records`")
        connection.execSQL("DROP TABLE `password_history_records`")

        connection.execSQL(
            """
            CREATE TABLE `credential_records_new` (
                `id` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `summary_payload` BLOB NOT NULL,
                `summary_nonce` BLOB NOT NULL,
                `secret_payload` BLOB NOT NULL,
                `secret_nonce` BLOB NOT NULL,
                `folder_id` TEXT,
                `is_favorite` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `last_used_at` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            INSERT INTO `credential_records_new` (
                `id`, `type`, `summary_payload`, `summary_nonce`, `secret_payload`,
                `secret_nonce`, `folder_id`, `is_favorite`, `created_at`, `updated_at`, `last_used_at`
            ) SELECT
                `id`, `type`, `summary_payload`, `summary_nonce`, `secret_payload`,
                `secret_nonce`, `folder_id`, `is_favorite`, `created_at`, `updated_at`, `last_used_at`
            FROM `credential_records`
            """.trimIndent(),
        )
        connection.execSQL("DROP TABLE `credential_records`")
        connection.execSQL("ALTER TABLE `credential_records_new` RENAME TO `credential_records`")
        listOf("folder_id", "is_favorite", "type", "created_at", "updated_at", "last_used_at")
            .forEach { column ->
                connection.execSQL(
                    "CREATE INDEX `index_credential_records_$column` " +
                        "ON `credential_records` (`$column`)",
                )
            }

        connection.execSQL(
            """
            CREATE TABLE `credential_folder_cross_ref` (
                `credential_id` TEXT NOT NULL,
                `folder_id` TEXT NOT NULL,
                PRIMARY KEY(`credential_id`, `folder_id`),
                FOREIGN KEY(`credential_id`) REFERENCES `credential_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`folder_id`) REFERENCES `folder_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "INSERT INTO `credential_folder_cross_ref` SELECT * FROM `_pv50_credential_folder_cross_ref`",
        )
        connection.execSQL(
            "CREATE INDEX `index_credential_folder_cross_ref_folder_id` " +
                "ON `credential_folder_cross_ref` (`folder_id`)",
        )

        connection.execSQL(
            """
            CREATE TABLE `credential_tag_cross_ref` (
                `credential_id` TEXT NOT NULL,
                `tag_id` TEXT NOT NULL,
                PRIMARY KEY(`credential_id`, `tag_id`),
                FOREIGN KEY(`credential_id`) REFERENCES `credential_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`tag_id`) REFERENCES `tag_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "INSERT INTO `credential_tag_cross_ref` SELECT * FROM `_pv50_credential_tag_cross_ref`",
        )
        connection.execSQL(
            "CREATE INDEX `index_credential_tag_cross_ref_tag_id` " +
                "ON `credential_tag_cross_ref` (`tag_id`)",
        )

        connection.execSQL(
            """
            CREATE TABLE `attachment_records` (
                `id` TEXT NOT NULL,
                `credential_id` TEXT NOT NULL,
                `encrypted_filename` BLOB NOT NULL,
                `filename_nonce` BLOB NOT NULL,
                `mime_type` TEXT NOT NULL,
                `size_bytes` INTEGER NOT NULL,
                `storage_path` TEXT NOT NULL,
                `key_derivation_context` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `content_format_version` INTEGER NOT NULL DEFAULT 0,
                `storage_state` TEXT NOT NULL DEFAULT 'LEGACY',
                PRIMARY KEY(`id`),
                FOREIGN KEY(`credential_id`) REFERENCES `credential_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        connection.execSQL("INSERT INTO `attachment_records` SELECT * FROM `_pv50_attachment_records`")
        connection.execSQL(
            "CREATE INDEX `index_attachment_records_credential_id` " +
                "ON `attachment_records` (`credential_id`)",
        )
        connection.execSQL(
            "CREATE INDEX `index_attachment_records_created_at` ON `attachment_records` (`created_at`)",
        )

        connection.execSQL(
            """
            CREATE TABLE `password_history_records` (
                `id` TEXT NOT NULL,
                `credential_id` TEXT NOT NULL,
                `encrypted_password` BLOB NOT NULL,
                `password_nonce` BLOB NOT NULL,
                `changed_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`credential_id`) REFERENCES `credential_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "INSERT INTO `password_history_records` SELECT * FROM `_pv50_password_history_records`",
        )
        connection.execSQL(
            "CREATE INDEX `index_password_history_records_credential_id` " +
                "ON `password_history_records` (`credential_id`)",
        )
        connection.execSQL(
            "CREATE INDEX `index_password_history_records_changed_at` " +
                "ON `password_history_records` (`changed_at`)",
        )

        connection.execSQL("DROP TABLE `_pv50_credential_folder_cross_ref`")
        connection.execSQL("DROP TABLE `_pv50_credential_tag_cross_ref`")
        connection.execSQL("DROP TABLE `_pv50_attachment_records`")
        connection.execSQL("DROP TABLE `_pv50_password_history_records`")
    }
}

internal fun RoomDatabase.Builder<VaultDatabase>.addVaultMigrations(): RoomDatabase.Builder<VaultDatabase> =
    addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
