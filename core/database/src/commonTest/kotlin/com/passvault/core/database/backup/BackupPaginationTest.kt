package com.passvault.core.database.backup

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BackupPaginationTest {
    @Test
    fun `encoding selection accepts explicit SQLite values without a guessed default`() {
        assertEquals(BackupDatabaseTextOrder.UTF8, BackupDatabaseTextOrder.fromPragma("UTF-8"))
        assertEquals(BackupDatabaseTextOrder.UTF16_LE, BackupDatabaseTextOrder.fromPragma("UTF-16le"))
        assertEquals(BackupDatabaseTextOrder.UTF16_BE, BackupDatabaseTextOrder.fromPragma("UTF-16be"))
        listOf("", "UTF-16", "UTF-32", "UTF-8 ").forEach { invalid ->
            assertFailsWith<IllegalArgumentException> { BackupDatabaseTextOrder.fromPragma(invalid) }
        }
    }

    @Test
    fun `comparison follows unsigned encoding bytes rather than platform String order`() {
        // Expected signs are independent byte-order vectors, not a String sort.
        val vectors = listOf(
            Vector("\uFB00", "\uD800\uDC00", -1, 1, 1),
            Vector("\u0100", "\u00FF", 1, -1, 1),
            Vector("z", "\u00E9", -1, -1, -1),
            Vector("a", "aa", -1, -1, -1),
            Vector("A", "a", -1, -1, -1),
            Vector("\u00E9", "e\u0301", 1, 1, 1),
            Vector("\uD800\uDC00", "\uD800\uDC01", -1, -1, -1),
            Vector("\uD800\uDCFF", "\uD800\uDD00", -1, 1, -1),
            Vector("", "\u0100", -1, -1, -1),
        )
        for (vector in vectors) {
            val expected = listOf(vector.utf8, vector.utf16le, vector.utf16be)
            BackupDatabaseTextOrder.entries.forEachIndexed { index, order ->
                assertEquals(expected[index], order.compare(vector.left, vector.right).sign(), order.name)
                assertEquals(-expected[index], order.compare(vector.right, vector.left).sign(), order.name)
                assertEquals(0, order.compare(vector.left, vector.left), order.name)
            }
        }
    }

    @Test
    fun `single keys advance within pages and across pages in native order`() = runTest {
        for ((order, keys) in nativePairs) {
            for (limit in listOf(1, 2)) {
                val pages = ArrayDeque(keys.chunked(limit))
                val cursors = mutableListOf<String>()
                val emitted = mutableListOf<String>()
                order.emitSingleKeyPages(limit, fetch = { after, requested ->
                    assertEquals(limit, requested)
                    cursors += after
                    pages.removeFirstOrNull().orEmpty()
                }, key = { it }) { emitted += it }
                assertEquals(keys, emitted)
                assertEquals(listOf("") + keys.chunked(limit).map { it.last() }, cursors)
            }
        }
    }

    @Test
    fun `single key repeat and backward progress fail before the offending consumer`() = runTest {
        for ((order, keys) in nativePairs) {
            val (low, high) = keys
            for (rows in listOf(listOf(low, low), listOf(high, low))) {
                for (limit in listOf(1, 2)) {
                    val pages = ArrayDeque(rows.chunked(limit))
                    val emitted = mutableListOf<String>()
                    val error = assertFailsWith<IllegalArgumentException> {
                        order.emitSingleKeyPages(limit, fetch = { _, _ ->
                            pages.removeFirstOrNull().orEmpty()
                        }, key = { it }) { emitted += it }
                    }
                    assertEquals("Backup page did not advance", error.message)
                    assertEquals(rows.take(1), emitted)
                }
            }
        }
    }

    @Test
    fun `empty and oversized pages do not invoke a consumer`() = runTest {
        var fetches = 0
        BackupDatabaseTextOrder.UTF8.emitSingleKeyPages<String>(1, fetch = { after, _ ->
            assertEquals("", after)
            fetches++
            emptyList()
        }, key = { it }) { error("Empty page was consumed") }
        assertEquals(1, fetches)
        val failure = assertFailsWith<IllegalArgumentException> {
            BackupDatabaseTextOrder.UTF8.emitSingleKeyPages(1, fetch = { _, _ ->
                listOf("a", "b")
            }, key = { it }) { error("Oversized page was consumed") }
        }
        assertEquals("Backup page exceeds its limit", failure.message)
    }

    @Test
    fun `invalid row keys are rejected while the existing maximum Unicode key remains accepted`() = runTest {
        val invalid = listOf(
            "", " ", "\u0000", "\u202E", "\uD800", "\uDC00", "/", "\\", "a".repeat(257),
            "\uD800\uDC00".repeat(257),
        )
        for (order in BackupDatabaseTextOrder.entries) {
            for (key in invalid) {
                val failure = assertFailsWith<IllegalArgumentException> {
                    order.emitSingleKeyPages(1, fetch = { _, _ -> listOf(key) }, key = { it }) {
                        error("Invalid key reached its consumer")
                    }
                }
                assertEquals("Backup pagination key is invalid", failure.message)
            }
            val accepted = "\uD800\uDC00".repeat(256)
            var emitted = 0
            order.emitSingleKeyPages(1, fetch = { after, _ ->
                if (after.isEmpty()) listOf(accepted) else emptyList()
            }, key = { it }) {
                assertEquals(accepted, it)
                emitted++
            }
            assertEquals(1, emitted)
        }
    }

    @Test
    fun `tuple primary advance permits secondary reset and equal primary uses native secondary order`() = runTest {
        for ((order, keys) in nativePairs) {
            val (low, high) = keys
            val rows = listOf(low to high, high to low, high to high)
            for (limit in listOf(1, 2)) {
                val pages = ArrayDeque(rows.chunked(limit))
                val emitted = mutableListOf<Pair<String, String>>()
                val cursors = mutableListOf<Pair<String, String>>()
                order.emitCompositeKeyPages(limit, fetch = { first, second, requested ->
                    assertEquals(limit, requested)
                    cursors += first to second
                    pages.removeFirstOrNull().orEmpty()
                }, firstKey = { it.first }, secondKey = { it.second }) { emitted += it }
                assertEquals(rows, emitted)
                assertEquals(listOf("" to "") + rows.chunked(limit).map { it.last() }, cursors)
            }
        }
    }

    @Test
    fun `tuple duplicate primary regression and secondary regression are independently rejected`() = runTest {
        for ((order, keys) in nativePairs) {
            val (low, high) = keys
            val initial = high to low
            val invalid = listOf(initial, low to high, high to low)
            for ((index, candidate) in invalid.withIndex()) {
                // Last case uses a higher initial secondary to isolate that dimension.
                val first = if (index == 2) high to high else initial
                for (limit in listOf(1, 2)) {
                    val pages = ArrayDeque(listOf(first, candidate).chunked(limit))
                    val emitted = mutableListOf<Pair<String, String>>()
                    val failure = assertFailsWith<IllegalArgumentException> {
                        order.emitCompositeKeyPages(limit, fetch = { _, _, _ ->
                            pages.removeFirstOrNull().orEmpty()
                        }, firstKey = { it.first }, secondKey = { it.second }) { emitted += it }
                    }
                    assertEquals("Backup page did not advance", failure.message)
                    assertEquals(listOf(first), emitted)
                }
            }
        }
    }

    @Test
    fun `tuple validates both keys even when the primary alone would advance`() = runTest {
        for (row in listOf("a" to "\uD800", "\uD800" to "a")) {
            val failure = assertFailsWith<IllegalArgumentException> {
                BackupDatabaseTextOrder.UTF8.emitCompositeKeyPages(1, fetch = { _, _, _ -> listOf(row) },
                    firstKey = { it.first }, secondKey = { it.second }) { error("Invalid tuple was consumed") }
            }
            assertEquals("Backup pagination key is invalid", failure.message)
        }
    }

    @Test
    fun `tuple empty oversized and invalid limit paths retain their guards`() = runTest {
        var fetched = 0
        BackupDatabaseTextOrder.UTF8.emitCompositeKeyPages<Pair<String, String>>(1, fetch = { _, _, _ ->
            fetched++
            emptyList()
        }, firstKey = { it.first }, secondKey = { it.second }) { error("Empty tuple page was consumed") }
        assertEquals(1, fetched)
        val oversized = assertFailsWith<IllegalArgumentException> {
            BackupDatabaseTextOrder.UTF8.emitCompositeKeyPages(1,
                fetch = { _, _, _ -> listOf("a" to "a", "a" to "b") },
                firstKey = { it.first }, secondKey = { it.second }) { error("Oversized tuple page was consumed") }
        }
        assertEquals("Backup page exceeds its limit", oversized.message)
        for (limit in listOf(0, -1)) {
            assertFailsWith<IllegalArgumentException> {
                BackupDatabaseTextOrder.UTF8.emitSingleKeyPages<String>(limit,
                    fetch = { _, _ -> error("Invalid limit fetched") }, key = { it }) {}
            }
            assertFailsWith<IllegalArgumentException> {
                BackupDatabaseTextOrder.UTF8.emitCompositeKeyPages<Pair<String, String>>(limit,
                    fetch = { _, _, _ -> error("Invalid limit fetched") },
                    firstKey = { it.first }, secondKey = { it.second }) {}
            }
        }
    }

    @Test
    fun `consumer exceptions propagate through both pagers without another fetch`() = runTest {
        val failures = listOf(IllegalStateException("synthetic consumer failure"), CancellationException("cancelled"))
        for (failure in failures) {
            var fetches = 0
            val simple = assertFailsWith<Exception> {
                BackupDatabaseTextOrder.UTF8.emitSingleKeyPages(2, fetch = { _, _ ->
                    fetches++
                    listOf("a", "b")
                }, key = { it }) { throw failure }
            }
            assertSame(failure, simple)
            assertEquals(1, fetches)
            fetches = 0
            val tuple = assertFailsWith<Exception> {
                BackupDatabaseTextOrder.UTF8.emitCompositeKeyPages(2, fetch = { _, _, _ ->
                    fetches++
                    listOf("a" to "a", "a" to "b")
                }, firstKey = { it.first }, secondKey = { it.second }) { throw failure }
            }
            assertSame(failure, tuple)
            assertEquals(1, fetches)
        }
    }

    @Test
    fun `job cancellation during a consumer stops both pagers`() = runTest {
        for (composite in listOf(false, true)) {
            var fetches = 0
            var consumed = 0
            val entered = CompletableDeferred<Unit>()
            val job = launch {
                val consume: suspend (String) -> Unit = {
                    consumed++
                    entered.complete(Unit)
                    awaitCancellation()
                }
                if (composite) {
                    BackupDatabaseTextOrder.UTF8.emitCompositeKeyPages(2, fetch = { _, _, _ ->
                        fetches++
                        listOf("a" to "a", "a" to "b")
                    }, firstKey = { it.first }, secondKey = { it.second }) { consume(it.first) }
                } else {
                    BackupDatabaseTextOrder.UTF8.emitSingleKeyPages(2, fetch = { _, _ ->
                        fetches++
                        listOf("a", "b")
                    }, key = { it }, block = consume)
                }
            }
            entered.await()
            job.cancelAndJoin()
            assertTrue(job.isCancelled)
            assertEquals(1, fetches)
            assertEquals(1, consumed)
        }
    }

    @Test
    fun `ordinary ASCII and exact equality retain BINARY semantics on all encodings`() {
        for (order in BackupDatabaseTextOrder.entries) {
            val first = "00000000-0000-0000-0000-000000000001"
            val second = "00000000-0000-0000-0000-000000000002"
            assertTrue(order.compare(first, second) < 0)
            assertEquals(0, order.compare("", ""))
            assertEquals(0, order.compare("same-key", "same-key"))
        }
    }

    private data class Vector(val left: String, val right: String, val utf8: Int, val utf16le: Int, val utf16be: Int)

    private fun Int.sign(): Int = compareTo(0)

    private val nativePairs = listOf(
        BackupDatabaseTextOrder.UTF8 to listOf("\uFB00", "\uD800\uDC00"),
        BackupDatabaseTextOrder.UTF16_LE to listOf("\u0100", "\u00FF"),
        BackupDatabaseTextOrder.UTF16_BE to listOf("\uD800\uDC00", "\uFB00"),
    )
}
