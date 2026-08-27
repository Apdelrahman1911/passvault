package com.passvault.core.database.backup

/** Restore cannot safely stage its authenticated attachment set on the target volume. */
class BackupInsufficientStorageException(
    val requiredBytes: Long?,
    val availableBytes: Long?,
    cause: Throwable? = null,
) : Exception("The backup needs more free attachment storage", cause)
