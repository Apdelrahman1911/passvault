package com.passvault.core.database.backup

import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LengthPrefixedUtf8Test {
    @Test
    fun `malformed length prefixed UTF-8 is rejected without replacement characters`() {
        listOf(
            byteArrayOf(0x80.toByte()),
            byteArrayOf(0xe2.toByte(), 0x82.toByte()),
            byteArrayOf(0xc0.toByte(), 0x80.toByte()),
        ).forEach { malformed ->
            assertFailsWith<Exception> {
                Buffer().writeInt(malformed.size).write(malformed).readLengthPrefixedUtf8()
            }
        }
    }

    @Test
    fun `length prefixed UTF-8 preserves valid multibyte attachment identifiers`() {
        val value = "attachment-📎-résumé"
        val encoded = value.encodeToByteArray()

        assertEquals(value, Buffer().writeInt(encoded.size).write(encoded).readLengthPrefixedUtf8())
    }
}
