package com.passvault.core.database.backup

import com.passvault.core.database.repository.MAX_RECORD_IDENTIFIER_LENGTH
import com.passvault.core.database.repository.requireRecordIdentifier

/**
 * SQLite BINARY order for the database encoding reported by PRAGMA encoding.
 * Inputs are validated identifiers, or the empty initial cursor. No normalization
 * or case folding is allowed: each key must retain its cryptographic identity.
 */
internal enum class BackupDatabaseTextOrder : Comparator<String> {
    UTF8,
    UTF16_LE,
    UTF16_BE;

    override fun compare(a: String, b: String): Int = when (this) {
        UTF8 -> compareScalars(a, b)
        UTF16_LE -> compareCodeUnits(a, b, littleEndian = true)
        UTF16_BE -> compareCodeUnits(a, b, littleEndian = false)
    }

    companion object {
        fun fromPragma(value: String): BackupDatabaseTextOrder = when (value) {
            "UTF-8" -> UTF8
            "UTF-16le" -> UTF16_LE
            "UTF-16be" -> UTF16_BE
            else -> throw IllegalArgumentException("Unsupported database encoding")
        }
    }
}

// Unsigned UTF-8 byte order preserves Unicode scalar order for well-formed text.
// Decode pairs without allocating an encoded copy of each key on every page.
private fun compareScalars(left: String, right: String): Int {
    var leftIndex = 0
    var rightIndex = 0
    while (leftIndex < left.length && rightIndex < right.length) {
        val leftScalar = left.scalarAt(leftIndex)
        val rightScalar = right.scalarAt(rightIndex)
        if (leftScalar != rightScalar) return leftScalar.compareTo(rightScalar)
        leftIndex += if (leftScalar >= 0x10000) 2 else 1
        rightIndex += if (rightScalar >= 0x10000) 2 else 1
    }
    return (left.length - leftIndex).compareTo(right.length - rightIndex)
}

private fun String.scalarAt(index: Int): Int {
    val first = this[index]
    return if (first.isHighSurrogate()) {
        0x10000 + ((first.code - 0xD800) shl 10) + (this[index + 1].code - 0xDC00)
    } else {
        first.code
    }
}

private fun compareCodeUnits(left: String, right: String, littleEndian: Boolean): Int {
    for (index in 0 until minOf(left.length, right.length)) {
        val first = left[index].code
        val second = right[index].code
        // UTF-16LE compares each low byte before its high byte, both unsigned.
        val leftBytes = if (littleEndian) ((first and 0xFF) shl 8) or (first ushr 8) else first
        val rightBytes = if (littleEndian) ((second and 0xFF) shl 8) or (second ushr 8) else second
        if (leftBytes != rightBytes) return leftBytes.compareTo(rightBytes)
    }
    return left.length.compareTo(right.length)
}

private fun String.requirePaginationKey() {
    // A valid 256-code-point key occupies at most 512 UTF-16 units. Bound the
    // scans before the ordinary policy check, including on the first raw row.
    require(length <= MAX_RECORD_IDENTIFIER_LENGTH * 2) { "Backup pagination key is invalid" }
    requireRecordIdentifier("Backup pagination key")
}

internal suspend fun <T> BackupDatabaseTextOrder.emitSingleKeyPages(
    limit: Int,
    fetch: suspend (String, Int) -> List<T>,
    key: (T) -> String,
    block: suspend (T) -> Unit,
) {
    require(limit > 0) { "Backup page limit must be positive" }
    var after = ""
    while (true) {
        val page = fetch(after, limit)
        require(page.size <= limit) { "Backup page exceeds its limit" }
        if (page.isEmpty()) return
        page.forEach { value ->
            val next = key(value)
            next.requirePaginationKey()
            require(compare(next, after) > 0) { "Backup page did not advance" }
            block(value)
            after = next
        }
    }
}

internal suspend fun <T> BackupDatabaseTextOrder.emitCompositeKeyPages(
    limit: Int,
    fetch: suspend (String, String, Int) -> List<T>,
    firstKey: (T) -> String,
    secondKey: (T) -> String,
    block: suspend (T) -> Unit,
) {
    require(limit > 0) { "Backup page limit must be positive" }
    var afterFirst = ""
    var afterSecond = ""
    while (true) {
        val page = fetch(afterFirst, afterSecond, limit)
        require(page.size <= limit) { "Backup page exceeds its limit" }
        if (page.isEmpty()) return
        page.forEach { value ->
            val nextFirst = firstKey(value)
            val nextSecond = secondKey(value)
            nextFirst.requirePaginationKey()
            nextSecond.requirePaginationKey()
            val firstComparison = compare(nextFirst, afterFirst)
            require(firstComparison > 0 || firstComparison == 0 && compare(nextSecond, afterSecond) > 0) {
                "Backup page did not advance"
            }
            block(value)
            afterFirst = nextFirst
            afterSecond = nextSecond
        }
    }
}
