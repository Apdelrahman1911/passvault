package com.passvault.core.crypto

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class Argon2Test {
    @Test
    fun `lowercase hexadecimal KDF input is byte exact and always cleared`() {
        val password = byteArrayOf(0x00, 0x0F, 0x10, 0xFF.toByte())
        var successfulTemporary: ByteArray? = null
        var failedTemporary: ByteArray? = null

        val marker = password.withLowercaseHexBytes { encoded ->
            successfulTemporary = encoded
            assertContentEquals("000f10ff".encodeToByteArray(), encoded)
            42
        }
        assertEquals(42, marker)
        assertTrue(successfulTemporary?.all { it == 0.toByte() } == true)

        assertFailsWith<IllegalStateException> {
            password.withLowercaseHexBytes { encoded ->
                failedTemporary = encoded
                throw IllegalStateException("expected")
            }
        }
        assertTrue(failedTemporary?.all { it == 0.toByte() } == true)
    }

    @Test
    fun `production Argon2 preserves the historical lowercase hex input`() = runTest {
        val password = byteArrayOf(0x00, 0xFF.toByte(), 0x01)
        val salt = ByteArray(16) { it.toByte() }
        val expected = decodeHex("2bc5a714c8397bb9e89e70c957231bd3cd4f0b4ffe32bb6ca1f5067196b60b15")
        var derived: DerivedKey? = null
        try {
            derived = LibsodiumCryptoEngine().deriveKey(
                password = password,
                salt = salt,
                opsLimit = 1,
                memLimit = 8 * 1024,
            ).getOrThrow()

            assertContentEquals(expected, derived.key)
        } finally {
            derived?.clear()
            password.fill(0)
            salt.fill(0)
            expected.fill(0)
        }
    }

    @Test
    fun `production Argon2 matches the established text password vector`() = runTest {
        val password = "TestPassword123!".encodeToByteArray()
        val salt = ByteArray(16) { it.toByte() }
        val expected = decodeHex("4c1422bcf6ea79ca8b843170ffbaf713f854f1b97e4fd0df07d741a650cacab7")
        var derived: DerivedKey? = null
        try {
            derived = LibsodiumCryptoEngine().deriveKey(
                password = password,
                salt = salt,
                opsLimit = 1,
                memLimit = 8 * 1024,
            ).getOrThrow()

            assertContentEquals(expected, derived.key)
        } finally {
            derived?.clear()
            password.fill(0)
            salt.fill(0)
            expected.fill(0)
        }
    }

    @Test
    fun `production profiles match independent Argon2id reference vectors`() = runTest {
        /*
         * Generated independently with the upstream Argon2 reference CLI,
         * tag 20190702 (commit 62358ba2123abd17fccf2a108a301d4b52c01a7c):
         * https://github.com/P-H-C/phc-winner-argon2/tree/20190702
         * printf '5465737450617373776f726431323321' |
         *   ./argon2 0123456789abcdef -id -v 13 -t {3,4} -m 16 -p 1 -l 32 -r
         *
         * The CLI input is PassVault's compatibility-preserving lowercase
         * hexadecimal encoding of the original password bytes. `-m 16`
         * means 2^16 KiB, matching the 64 MiB production profile.
         */
        val password = "TestPassword123!".encodeToByteArray()
        val salt = "0123456789abcdef".encodeToByteArray()
        val vectors = listOf(
            Argon2Parameters.INTERACTIVE to
                "dc1aff4d7c74898c0aca2da51e33760dbe716e70abc86a89f6e9d55d5451b03c",
            selectArgon2Parameters(durationMilliseconds = 0L) to
                "ccd29a7f8888cf2ddce1df111f8ad5ba7939b0a177ac6606ad98beb6ac160172",
        )
        try {
            vectors.forEach { (parameters, expectedHex) ->
                val expected = decodeHex(expectedHex)
                val derived = LibsodiumCryptoEngine().deriveKey(
                    password = password,
                    salt = salt,
                    opsLimit = parameters.opsLimit,
                    memLimit = parameters.memLimit,
                ).getOrThrow()
                try {
                    assertContentEquals(expected, derived.key)
                } finally {
                    derived.clear()
                    expected.fill(0)
                }
            }
        } finally {
            password.fill(0)
            salt.fill(0)
        }
    }

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

private fun decodeHex(value: String): ByteArray {
    require(value.length % 2 == 0)
    return ByteArray(value.length / 2) { index ->
        value.substring(index * 2, index * 2 + 2).toInt(16).toByte()
    }
}
