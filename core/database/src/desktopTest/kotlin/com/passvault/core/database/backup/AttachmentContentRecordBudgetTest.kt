package com.passvault.core.database.backup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AttachmentContentRecordBudgetTest {
    @Test
    fun `record ceiling is tied to declared bytes and the absolute limit`() {
        assertEquals(3L, AttachmentContentRecordBudget(declaredBytes = 3L).maximumAllowedRecords)
        assertEquals(
            BackupLimits.MAX_ATTACHMENT_CONTENT_RECORDS,
            AttachmentContentRecordBudget(declaredBytes = Long.MAX_VALUE).maximumAllowedRecords,
        )
    }

    @Test
    fun `exact record ceiling completes and one extra record fails`() {
        val exact = AttachmentContentRecordBudget(declaredBytes = 3L, maximumRecords = 3L)
        repeat(3) { exact.accept(recordBytes = 1) }
        assertTrue(exact.isComplete)
        assertEquals(3L, exact.consumedRecords)

        val over = AttachmentContentRecordBudget(declaredBytes = 4L, maximumRecords = 3L)
        repeat(3) { over.accept(recordBytes = 1) }
        assertFailsWith<IllegalArgumentException> { over.requireRecordAvailable() }
        assertFailsWith<IllegalArgumentException> { over.accept(recordBytes = 1) }
    }

    @Test
    fun `empty and oversized records fail without advancing accounting`() {
        val budget = AttachmentContentRecordBudget(declaredBytes = 2L)

        assertFailsWith<IllegalArgumentException> { budget.accept(recordBytes = 0) }
        assertFailsWith<IllegalArgumentException> { budget.accept(recordBytes = 3) }
        assertEquals(0L, budget.consumedBytes)
        assertEquals(0L, budget.consumedRecords)
    }
}
