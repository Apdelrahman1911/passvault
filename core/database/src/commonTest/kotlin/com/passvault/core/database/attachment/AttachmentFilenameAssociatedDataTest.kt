package com.passvault.core.database.attachment

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class AttachmentFilenameAssociatedDataTest {
    @Test
    fun `version one bytes remain compatible for generated identifiers`() {
        val attachmentId = "00000000-0000-0000-0000-000000000001"
        val credentialId = "00000000-0000-0000-0000-000000000002"

        assertContentEquals(
            "passvault:attachment:$attachmentId:$credentialId:filename:v1".encodeToByteArray(),
            attachmentFilenameAssociatedData(attachmentId, credentialId),
        )
    }

    @Test
    fun `version one encoding rejects ambiguous identifier boundaries`() {
        assertFailsWith<IllegalArgumentException> {
            attachmentFilenameAssociatedData("attachment:segment", "credential")
        }
        assertFailsWith<IllegalArgumentException> {
            attachmentFilenameAssociatedData("attachment", "credential:segment")
        }
    }
}
