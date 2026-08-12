package com.passvault.core.crypto

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class Argon2Test {
    @Test
    fun `built-in profiles meet the supported minimum and increase monotonically`() {
        val profiles = listOf(
            Argon2Parameters.MINIMUM,
            Argon2Parameters.INTERACTIVE,
            Argon2Parameters.MODERATE,
            Argon2Parameters.SENSITIVE,
        )

        profiles.forEach { profile ->
            assertTrue(profile.opsLimit in 2..10)
            assertTrue(profile.memLimit >= 32 * 1024 * 1024)
        }
        profiles.zipWithNext().forEach { (lower, higher) ->
            assertTrue(higher.opsLimit >= lower.opsLimit)
            assertTrue(higher.memLimit >= lower.memLimit)
        }
    }

    @Test
    fun `derived key uses content equality and clears owned arrays`() {
        val first = DerivedKey(
            key = ByteArray(32) { it.toByte() },
            salt = ByteArray(16) { (it + 32).toByte() },
            opsLimit = 3,
            memLimit = 64 * 1024 * 1024,
        )
        val second = DerivedKey(
            key = first.key.copyOf(),
            salt = first.salt.copyOf(),
            opsLimit = first.opsLimit,
            memLimit = first.memLimit,
        )

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        second.clear()
        assertTrue(second.key.all { it == 0.toByte() })
        assertTrue(second.salt.all { it == 0.toByte() })
        assertNotEquals(first, second)
    }

    @Test
    fun `encrypted data uses content equality and clears every component`() {
        val first = EncryptedData(
            ciphertext = ByteArray(48) { it.toByte() },
            nonce = ByteArray(24) { (it + 48).toByte() },
            tag = ByteArray(16) { (it + 72).toByte() },
        )
        val second = EncryptedData(
            ciphertext = first.ciphertext.copyOf(),
            nonce = first.nonce.copyOf(),
            tag = first.tag.copyOf(),
        )

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        second.clear()
        assertTrue(second.ciphertext.all { it == 0.toByte() })
        assertTrue(second.nonce.all { it == 0.toByte() })
        assertTrue(second.tag.all { it == 0.toByte() })
    }

    @Test
    fun `wrapped key compares byte-array contents`() {
        val ciphertext = ByteArray(48) { it.toByte() }
        val nonce = ByteArray(24) { (it + 1).toByte() }

        val first = WrappedKey(ciphertext.copyOf(), nonce.copyOf())
        val second = WrappedKey(ciphertext.copyOf(), nonce.copyOf())

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertContentEquals(ciphertext, first.ciphertext)
        assertContentEquals(nonce, first.nonce)
    }
}
