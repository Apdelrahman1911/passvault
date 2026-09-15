package com.passvault.core.database.backup

/**
 * Bounds the number and aggregate bytes of outer backup records used to carry
 * one already-encrypted attachment object.
 */
internal class AttachmentContentRecordBudget(
    private val declaredBytes: Long,
    maximumRecords: Long = BackupLimits.MAX_ATTACHMENT_CONTENT_RECORDS,
) {
    val maximumAllowedRecords = minOf(declaredBytes, maximumRecords)
    var consumedBytes = 0L
        private set
    var consumedRecords = 0L
        private set

    val isComplete: Boolean
        get() = consumedBytes == declaredBytes

    init {
        require(declaredBytes > 0L)
        require(maximumRecords > 0L)
    }

    fun requireRecordAvailable() {
        require(consumedRecords < maximumAllowedRecords) {
            "An attachment uses too many backup content records"
        }
    }

    fun accept(recordBytes: Int) {
        require(recordBytes > 0)
        requireRecordAvailable()
        require(recordBytes.toLong() <= declaredBytes - consumedBytes)
        consumedBytes += recordBytes
        consumedRecords++
    }
}
