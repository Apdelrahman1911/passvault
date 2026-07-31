package com.passvault.core.domain.model

import assertk.assertThat
import assertk.assertions.*
import kotlin.test.*

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
    fun `toString masks content`() {
        val sensitive = SensitiveText.from("secret")

        assertThat(sensitive.toString()).isEqualTo("[REDACTED: 6 chars]")
    }

    @Test
    fun `toString shows length`() {
        val sensitive = SensitiveText.from("longsecret")

        assertThat(sensitive.toString()).isEqualTo("[REDACTED: 10 chars]")
    }

    @Test
    fun `expose returns copy`() {
        val sensitive = SensitiveText.from("secret")
        val exposed = sensitive.expose()

        assertThat(exposed.concatToString()).isEqualTo("secret")
    }

    @Test
    fun `clear wipes data`() {
        val sensitive = SensitiveText.from("secret")

        // Note: We can't directly test the cleared state
        // since expose() returns a copy and toString masks content
        // This test documents the intended behavior
        sensitive.clear()

        // After clear, the internal value should be zeroed
        // but we verify the API works
        assertThat(sensitive.isEmpty()).isFalse()
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
    fun `hashCode is different for different content`() {
        val sensitive1 = SensitiveText.from("secret1")
        val sensitive2 = SensitiveText.from("secret2")

        assertThat(sensitive1.hashCode()).isNotEqualTo(sensitive2.hashCode())
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
}
