package com.passvault.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.passvault.core.database.dao.AttachmentDao
import com.passvault.core.database.dao.CredentialDao
import com.passvault.core.database.dao.FolderDao
import com.passvault.core.database.dao.PasswordHistoryDao
import com.passvault.core.database.dao.TagDao
import com.passvault.core.database.dao.VaultBackupDao
import com.passvault.core.database.dao.VaultMetadataDao
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.CorruptionLogEntity
import com.passvault.core.database.entity.CredentialFolderCrossRef
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.CurrentVersionInfoEntity
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.MigrationStateEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.VaultMetadataEntity
import kotlin.time.Instant

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
    version = 3,
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
    abstract fun vaultBackupDao(): VaultBackupDao
}

@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<VaultDatabase> {
    override fun initialize(): VaultDatabase
}

class VaultDatabaseConverters {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(millis: Long?): Instant? {
        return millis?.let { Instant.fromEpochMilliseconds(it) }
    }
}
