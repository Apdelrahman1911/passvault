package com.passvault.core.database.backup

import com.passvault.core.database.repository.MAX_RECORD_IDENTIFIER_LENGTH
import com.passvault.core.database.repository.requireRecordIdentifier

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
