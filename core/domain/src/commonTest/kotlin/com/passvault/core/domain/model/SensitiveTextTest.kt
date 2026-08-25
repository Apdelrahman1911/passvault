package com.passvault.core.domain.model

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.text.CharacterCodingException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Unit tests for SensitiveText value type.
 */
class SensitiveTextTest {

    @Test
    fun `create from string`() {
        val sensitive = SensitiveText.from("secret")

        assertThat(sensitive.toStringUnsafe()).isEqualTo("secret")
    }

    @Test
    fun `create from char array`() {
        val chars = charArrayOf('s', 'e', 'c', 'r', 'e', 't')
        val sensitive = SensitiveText.from(chars)

        assertThat(sensitive.toStringUnsafe()).isEqualTo("secret")
    }

    @Test
    fun `create from byte array`() {
        val bytes = "secret".encodeToByteArray()
        val sensitive = SensitiveText.from(bytes)

        assertThat(sensitive.toStringUnsafe()).isEqualTo("secret")
    }

    @Test
    fun `create from byte array rejects malformed UTF-8`() {
        val malformed = byteArrayOf(0xC3.toByte(), 0x28)

        assertFailsWith<CharacterCodingException> {
            SensitiveText.from(malformed)
        }
    }

    @Test
    fun `strict UTF-8 encoding rejects malformed UTF-16`() {
        val sensitive = SensitiveText.from("secret\uD800")
        try {
            assertThat(sensitive.hasWellFormedUnicode()).isFalse()
            assertFailsWith<CharacterCodingException> {
                sensitive.toUtf8ByteArray()
            }
        } finally {
            sensitive.clear()
        }
    }

    @Test
    fun `single-line validation rejects controls without exposing the secret`() {
        val safe = SensitiveText.from("PassVault العربية 🔐")
        val unsafe = SensitiveText.from("invoice\u202Efdp.exe")
        try {
            assertThat(safe.hasOnlySafeSingleLineCodePoints()).isTrue()
            assertThat(unsafe.hasOnlySafeSingleLineCodePoints()).isFalse()
        } finally {
            safe.clear()
            unsafe.clear()
        }
    }

    @Test
    fun `toString masks content`() {
        val sensitive = SensitiveText.from("secret")

        assertThat(sensitive.toString()).isEqualTo("[REDACTED]")
    }

    @Test
    fun `toString does not reveal length`() {
        val sensitive = SensitiveText.from("longsecret")

        assertThat(sensitive.toString()).isEqualTo("[REDACTED]")
    }

    @Test
    fun `expose returns copy`() {
        val sensitive = SensitiveText.from("secret")
        val exposed = sensitive.expose()

        try {
            assertThat(exposed.concatToString()).isEqualTo("secret")
        } finally {
            exposed.fill('\u0000')
            sensitive.clear()
        }
    }

    @Test
    fun `with exposed clears its temporary copy after completion`() {
        val sensitive = SensitiveText.from("secret")
        var exposed: CharArray? = null

        try {
            val result = sensitive.withExposed { characters ->
                exposed = characters
                characters.concatToString()
            }

            assertThat(result).isEqualTo("secret")
            assertTrue(exposed?.all { it == '\u0000' } == true)
            assertThat(sensitive.toStringUnsafe()).isEqualTo("secret")
        } finally {
            sensitive.clear()
        }
    }

    @Test
    fun `with exposed clears its temporary copy when the block throws`() {
        val sensitive = SensitiveText.from("secret")
        var exposed: CharArray? = null

        try {
            assertFailsWith<IllegalStateException> {
                sensitive.withExposed { characters ->
                    exposed = characters
                    throw IllegalStateException("expected")
                }
            }

            assertTrue(exposed?.all { it == '\u0000' } == true)
            assertThat(sensitive.toStringUnsafe()).isEqualTo("secret")
        } finally {
            sensitive.clear()
        }
    }

    @Test
    fun `clear wipes data`() {
        val sensitive = SensitiveText.from("secret")
        sensitive.clear()
        val cleared = sensitive.expose()
        try {
            assertTrue(cleared.all { it == '\u0000' })
        } finally {
            cleared.fill('\u0000')
        }
    }

    @Test
    fun `isEmpty returns true for empty string`() {
        val sensitive = SensitiveText.from("")

        assertThat(sensitive.isEmpty()).isTrue()
    }

    @Test
    fun `isEmpty returns false for non-empty string`() {
        val sensitive = SensitiveText.from("secret")

        assertThat(sensitive.isEmpty()).isFalse()
    }

    @Test
    fun `isNotEmpty returns true for non-empty string`() {
        val sensitive = SensitiveText.from("secret")

        assertThat(sensitive.isNotEmpty()).isTrue()
    }

    @Test
    fun `isNotEmpty returns false for empty string`() {
        val sensitive = SensitiveText.from("")

        assertThat(sensitive.isNotEmpty()).isFalse()
    }

    @Test
    fun `length returns correct size`() {
        val sensitive = SensitiveText.from("secret")

        assertThat(sensitive.length).isEqualTo(6)
    }

    @Test
    fun `mask shows dots for short value`() {
        val sensitive = SensitiveText.from("abc")

        assertThat(sensitive.mask()).isEqualTo("•••")
    }

    @Test
    fun `mask shows partial reveal for long value`() {
        val sensitive = SensitiveText.from("supersecretpassword")

        // First 3, dots, last 3
        assertThat(sensitive.mask()).isEqualTo("sup•••ord")
    }

    @Test
    fun `mask works for exactly 6 characters`() {
        val sensitive = SensitiveText.from("secret")

        assertThat(sensitive.mask()).isEqualTo("••••••")
    }

    @Test
    fun `mask works for 7 characters`() {
        val sensitive = SensitiveText.from("secrets")

        assertThat(sensitive.mask()).isEqualTo("sec•••ets")
    }

    @Test
    fun `equals compares content`() {
        val sensitive1 = SensitiveText.from("secret")
        val sensitive2 = SensitiveText.from("secret")
        val sensitive3 = SensitiveText.from("different")

        assertThat(sensitive1).isEqualTo(sensitive2)
        assertThat(sensitive1).isNotEqualTo(sensitive3)
    }

    @Test
    fun `equals returns false for different type`() {
        val sensitive = SensitiveText.from("secret")

        assertThat(sensitive).isNotEqualTo("secret")
    }

    @Test
    fun `equals returns true for same instance`() {
        val sensitive = SensitiveText.from("secret")

        assertThat(sensitive).isEqualTo(sensitive)
    }

    @Test
    fun `hashCode is consistent`() {
        val sensitive1 = SensitiveText.from("secret")
        val sensitive2 = SensitiveText.from("secret")

        assertThat(sensitive1.hashCode()).isEqualTo(sensitive2.hashCode())
    }

    @Test
    fun `hashCode does not fingerprint different secret content`() {
        val sensitive1 = SensitiveText.from("secret1")
        val sensitive2 = SensitiveText.from("secret2")

        assertThat(sensitive1.hashCode()).isEqualTo(sensitive2.hashCode())
    }

    @Test
    fun `exposed array is independent`() {
        val sensitive = SensitiveText.from("secret")
        val exposed = sensitive.expose()

        // Modify exposed array
        exposed[0] = 'x'

        // Original should be unchanged
        assertThat(sensitive.toStringUnsafe()).isEqualTo("secret")
    }

    @Test
    fun `empty string has zero length`() {
        val sensitive = SensitiveText.from("")

        assertThat(sensitive.length).isEqualTo(0)
        assertThat(sensitive.mask()).isEqualTo("")
    }

    @Test
    fun `single character masks as single dot`() {
        val sensitive = SensitiveText.from("a")

        assertThat(sensitive.mask()).isEqualTo("•")
    }

    @Test
    fun `two characters mask as two dots`() {
        val sensitive = SensitiveText.from("ab")

        assertThat(sensitive.mask()).isEqualTo("••")
    }

    @Test
    fun `unicode characters counted correctly`() {
        val sensitive = SensitiveText.from("日本語")

        assertThat(sensitive.length).isEqualTo(3)
        assertThat(sensitive.mask()).isEqualTo("•••")
    }

    @Test
    fun `emojis counted as single characters`() {
        val sensitive = SensitiveText.from("🔐🔑🔒")

        assertThat(sensitive.length).isEqualTo(3)
    }

    @Test
    fun `serialization fails closed instead of emitting a lossy marker`() {
        val sensitive = SensitiveText.from("secret")

        try {
            assertFailsWith<SerializationException> {
                Json.encodeToString(sensitive)
            }
        } finally {
            sensitive.clear()
        }
    }
}
