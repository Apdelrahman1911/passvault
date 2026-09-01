package com.passvault.core.domain.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AttachmentPolicyTest {
    @Test
    fun `new attachment names reject Windows device aliases and namespace characters`() {
        val invalidNames = buildList {
            addAll(listOf("CON", "nul.txt", "PrN.pdf", "report.pdf.", "..."))
            addAll(listOf("report*", "report?", "report\"", "report<", "report>", "report|"))
            (1..9).forEach { number ->
                add("COM$number")
                add("lpt$number.txt")
            }
        }

        invalidNames.forEach { name ->
            assertFailsWith<AttachmentInvalidFileNameException> {
                AttachmentPolicy.validateFileName(name)
            }
        }
    }

    @Test
    fun `portable names retain Unicode support and use one Windows collision key`() {
        assertEquals("résumé 📄.pdf", AttachmentPolicy.validateFileName(" résumé 📄.pdf "))
        assertEquals(
            AttachmentPolicy.canonicalFileNameKey("Report.pdf"),
            AttachmentPolicy.canonicalFileNameKey("report.pdf..."),
        )
    }

    @Test
    fun `historical Windows-invalid name remains readable but cannot become a new output name`() {
        assertEquals("report.pdf.", AttachmentPolicy.validateStoredFileName("report.pdf."))
        assertEquals("historical:name.txt", AttachmentPolicy.validateStoredFileName("historical:name.txt"))
        assertEquals("historical\\name.txt", AttachmentPolicy.validateStoredFileName("historical\\name.txt"))
        assertFailsWith<AttachmentInvalidFileNameException> {
            AttachmentPolicy.validateFileName("report.pdf.")
        }
        assertFailsWith<AttachmentInvalidFileNameException> {
            AttachmentPolicy.validateFileName("historical:name.txt")
        }
    }
}
