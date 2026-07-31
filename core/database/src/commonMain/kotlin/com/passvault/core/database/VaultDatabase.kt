package com.passvault.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.passvault.core.database.dao.*
import com.passvault.core.database.entity.*
import kotlin.time.Instant
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Database(
    entities = [
        VaultMetadataEntity::class,
        CredentialRecordEntity::class,
        FolderRecordEntity::class,
        TagRecordEntity::class,
        CredentialFolderCrossRef::class,
        CredentialTagCrossRef::class,
        AttachmentRecordEntity::class,
        PasswordHistoryRecordEntity::class,
        MigrationStateEntity::class,
        CurrentVersionInfoEntity::class,
        CorruptionLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(VaultDatabaseConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultMetadataDao(): VaultMetadataDao
    abstract fun credentialDao(): CredentialDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun passwordHistoryDao(): PasswordHistoryDao
    abstract fun migrationStateDao(): MigrationStateDao
    abstract fun vaultBackupDao(): VaultBackupDao
}

@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<VaultDatabase> {
    override fun initialize(): VaultDatabase
}

@OptIn(ExperimentalEncodingApi::class)
class VaultDatabaseConverters {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(millis: Long?): Instant? {
        return millis?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun fromByteArray(bytes: ByteArray?): String? {
        return bytes?.let { Base64.encode(it) }
    }

    @TypeConverter
    fun toByteArray(base64: String?): ByteArray? {
        return base64?.let { Base64.decode(it) }
    }
}
