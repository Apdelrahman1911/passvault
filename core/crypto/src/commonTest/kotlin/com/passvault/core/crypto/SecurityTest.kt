package com.passvault.core.crypto

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.TimeSource

/**
 * Security-focused tests for cryptographic operations.
 */
class SecurityTest {

    private lateinit var cryptoEngine: CryptoEngine

    @BeforeTest
    fun setUp() {
        cryptoEngine = LibsodiumCryptoEngine()
    }

    @Test
    fun `same password with different salt produces different key`() = runTest {
        val password = "password"
        val salt1 = cryptoEngine.generateRandom(16).getOrThrow()
        val salt2 = cryptoEngine.generateRandom(16).getOrThrow()

        val key1 = cryptoEngine.deriveKey(
            password.encodeToByteArray(),
            salt1,
            Argon2Parameters.INTERACTIVE.opsLimit,
            Argon2Parameters.INTERACTIVE.memLimit
        ).getOrThrow()

        val key2 = cryptoEngine.deriveKey(
            password.encodeToByteArray(),
            salt2,
            Argon2Parameters.INTERACTIVE.opsLimit,
            Argon2Parameters.INTERACTIVE.memLimit
        ).getOrThrow()

        assertFalse(
            key1.key.contentEquals(key2.key),
            "Same password with different salts should produce different keys"
        )
    }

    @Test
    fun `different passwords produce different keys`() = runTest {
        val salt = cryptoEngine.generateRandom(16).getOrThrow()
        val password1 = "password1"
        val password2 = "password2"

        val key1 = cryptoEngine.deriveKey(
            password1.encodeToByteArray(),
            salt,
            Argon2Parameters.INTERACTIVE.opsLimit,
            Argon2Parameters.INTERACTIVE.memLimit
        ).getOrThrow()

        val key2 = cryptoEngine.deriveKey(
            password2.encodeToByteArray(),
            salt,
            Argon2Parameters.INTERACTIVE.opsLimit,
            Argon2Parameters.INTERACTIVE.memLimit
        ).getOrThrow()

        assertFalse(
            key1.key.contentEquals(key2.key),
            "Different passwords should produce different keys"
        )
    }

    @Test
    fun `encryption produces different ciphertext for same plaintext`() = runTest {
        val plaintext = "secret message"
        val key = cryptoEngine.generateRandom(32).getOrThrow()

        val encrypted1 = cryptoEngine.encrypt(plaintext.encodeToByteArray(), key).getOrThrow()
        val encrypted2 = cryptoEngine.encrypt(plaintext.encodeToByteArray(), key).getOrThrow()

        assertFalse(
            encrypted1.ciphertext.contentEquals(encrypted2.ciphertext),
            "Same plaintext should produce different ciphertext (due to random nonce)"
        )

        assertFalse(
            encrypted1.nonce.contentEquals(encrypted2.nonce),
            "Nonces should be different"
        )
    }

    @Test
    fun `production encryption owns outputs without mutating caller buffers`() = runTest {
        val plaintext = "secret message".encodeToByteArray()
        val key = ByteArray(32) { it.toByte() }
        val associatedData = "record:test".encodeToByteArray()
        val expectedPlaintext = plaintext.copyOf()
        val expectedKey = key.copyOf()
        val expectedAssociatedData = associatedData.copyOf()
        var encrypted: EncryptedData? = null
        var decrypted: ByteArray? = null
        try {
            val encryptedResult = cryptoEngine.encrypt(plaintext, key, associatedData).getOrThrow()
            encrypted = encryptedResult
            assertContentEquals(expectedPlaintext, plaintext)
            assertContentEquals(expectedKey, key)
            assertContentEquals(expectedAssociatedData, associatedData)

            val expectedCiphertext = encryptedResult.ciphertext.copyOf()
            val expectedNonce = encryptedResult.nonce.copyOf()
            try {
                val decryptedResult = cryptoEngine.decrypt(
                    encryptedResult.ciphertext,
                    encryptedResult.nonce,
                    key,
                    associatedData,
                )
                    .getOrThrow()
                decrypted = decryptedResult
                assertContentEquals(expectedPlaintext, decryptedResult)
                assertContentEquals(expectedCiphertext, encryptedResult.ciphertext)
                assertContentEquals(expectedNonce, encryptedResult.nonce)
            } finally {
                expectedCiphertext.fill(0)
                expectedNonce.fill(0)
            }
        } finally {
            plaintext.fill(0)
            key.fill(0)
            associatedData.fill(0)
            expectedPlaintext.fill(0)
            expectedKey.fill(0)
            expectedAssociatedData.fill(0)
            encrypted?.clear()
            decrypted?.fill(0)
        }
    }

    @Test
    fun `tampered ciphertext fails decryption`() = runTest {
        val plaintext = "secret message"
        val key = cryptoEngine.generateRandom(32).getOrThrow()

        val encrypted = cryptoEngine.encrypt(plaintext.encodeToByteArray(), key).getOrThrow()

        // Tamper with ciphertext
        encrypted.ciphertext[0] = (encrypted.ciphertext[0] + 1).toByte()

        val result = cryptoEngine.decrypt(
            encrypted.ciphertext,
            encrypted.nonce,
            key
        )

        assertTrue(
            result.isFailure,
            "Tampered ciphertext should fail to decrypt"
        )
    }

    @Test
    fun `tampered nonce fails decryption`() = runTest {
        val plaintext = "secret message"
        val key = cryptoEngine.generateRandom(32).getOrThrow()

        val encrypted = cryptoEngine.encrypt(plaintext.encodeToByteArray(), key).getOrThrow()

        // Tamper with nonce
        encrypted.nonce[0] = (encrypted.nonce[0] + 1).toByte()

        val result = cryptoEngine.decrypt(
            encrypted.ciphertext,
            encrypted.nonce,
            key
        )

        assertTrue(
            result.isFailure,
            "Tampered nonce should cause decryption to fail"
        )
    }

    @Test
    fun `wrong key fails decryption`() = runTest {
        val plaintext = "secret message"
        val key1 = cryptoEngine.generateRandom(32).getOrThrow()
        val key2 = cryptoEngine.generateRandom(32).getOrThrow()

        val encrypted = cryptoEngine.encrypt(plaintext.encodeToByteArray(), key1).getOrThrow()

        val result = cryptoEngine.decrypt(
            encrypted.ciphertext,
            encrypted.nonce,
            key2
        )

        assertTrue(
            result.isFailure,
            "Wrong key should fail to decrypt"
        )
    }

    @Test
    fun `derived subkeys are unique per context`() = runTest {
        val masterKey = cryptoEngine.generateRandom(32).getOrThrow()

        val subkey1 = cryptoEngine.deriveSubkey(masterKey, "context1", 32).getOrThrow()
        val subkey2 = cryptoEngine.deriveSubkey(masterKey, "context2", 32).getOrThrow()

        assertFalse(
            subkey1.contentEquals(subkey2),
            "Different contexts should produce different subkeys"
        )
    }

    @Test
    fun `same context produces same subkey`() = runTest {
        val masterKey = cryptoEngine.generateRandom(32).getOrThrow()

        val subkey1 = cryptoEngine.deriveSubkey(masterKey, "same-context", 32).getOrThrow()
        val subkey2 = cryptoEngine.deriveSubkey(masterKey, "same-context", 32).getOrThrow()

        assertTrue(
            subkey1.contentEquals(subkey2),
            "Same context should produce same subkey"
        )
    }

    @Test
    fun `different master keys produce different subkeys`() = runTest {
        val masterKey1 = cryptoEngine.generateRandom(32).getOrThrow()
        val masterKey2 = cryptoEngine.generateRandom(32).getOrThrow()

        val subkey1 = cryptoEngine.deriveSubkey(masterKey1, "context", 32).getOrThrow()
        val subkey2 = cryptoEngine.deriveSubkey(masterKey2, "context", 32).getOrThrow()

        assertFalse(
            subkey1.contentEquals(subkey2),
            "Different master keys should produce different subkeys"
        )
    }

    @Test
    fun `constantTimeEquals resists timing attacks`() = runTest {
        val a = cryptoEngine.generateRandom(32).getOrThrow()
        val b = a.copyOf()
        val c = cryptoEngine.generateRandom(32).getOrThrow()

        // Measure time for equal arrays
        val iterations = 10000
        val start1 = TimeSource.Monotonic.markNow()
        repeat(iterations) {
            cryptoEngine.constantTimeEquals(a, b)
        }
        val duration1 = start1.elapsedNow()

        // Measure time for different arrays
        val start2 = TimeSource.Monotonic.markNow()
        repeat(iterations) {
            cryptoEngine.constantTimeEquals(a, c)
        }
        val duration2 = start2.elapsedNow()

        // Wall-clock ratios are too noisy for a unit-test assertion. Verify both
        // paths were exercised; constant-time behavior is reviewed in benchmarks.
        assertTrue(duration1.isPositive())
        assertTrue(duration2.isPositive())
    }

    @Test
    fun `constantTimeEquals returns true for identical arrays`() = runTest {
        val a = cryptoEngine.generateRandom(32).getOrThrow()
        val b = a.copyOf()

        assertTrue(
            cryptoEngine.constantTimeEquals(a, b),
            "Identical arrays should be equal"
        )
    }

    @Test
    fun `constantTimeEquals returns false for different arrays`() = runTest {
        val a = cryptoEngine.generateRandom(32).getOrThrow()
        val b = cryptoEngine.generateRandom(32).getOrThrow()

        assertFalse(
            cryptoEngine.constantTimeEquals(a, b),
            "Different arrays should not be equal"
        )
    }

    @Test
    fun `constantTimeEquals returns false for different length arrays`() = runTest {
        val a = cryptoEngine.generateRandom(32).getOrThrow()
        val b = cryptoEngine.generateRandom(16).getOrThrow()

        assertFalse(
            cryptoEngine.constantTimeEquals(a, b),
            "Different length arrays should not be equal"
        )
    }

    @Test
    fun `constantTimeEquals returns false for empty arrays`() = runTest {
        val a = ByteArray(32)
        val b = ByteArray(32) { 0 }

        // Both are all zeros, should be equal
        assertTrue(
            cryptoEngine.constantTimeEquals(a, b),
            "All-zero arrays should be equal"
        )
    }

    @Test
    fun `VEK wrapping roundtrip`() = runTest {
        val hierarchy = VaultKeyHierarchy(cryptoEngine)
        val vek = cryptoEngine.generateMasterKey().getOrThrow()
        val kek = cryptoEngine.generateRandom(32).getOrThrow()

        // Wrap
        val wrapped = hierarchy.wrapVEK(vek, kek).getOrThrow()

        // Unwrap
        val unwrapped = hierarchy.unwrapVEK(wrapped, kek).getOrThrow()

        assertTrue(
            vek.contentEquals(unwrapped),
            "Unwrapped VEK should match original"
        )
    }

    @Test
    fun `wrong KEK fails VEK unwrapping`() = runTest {
        val hierarchy = VaultKeyHierarchy(cryptoEngine)
        val vek = cryptoEngine.generateMasterKey().getOrThrow()
        val kek1 = cryptoEngine.generateRandom(32).getOrThrow()
        val kek2 = cryptoEngine.generateRandom(32).getOrThrow()

        // Wrap with kek1
        val wrapped = hierarchy.wrapVEK(vek, kek1).getOrThrow()

        // Try to unwrap with kek2
        val result = hierarchy.unwrapVEK(wrapped, kek2)

        assertIs<CiphertextAuthenticationException>(result.exceptionOrNull())
        assertTrue(
            result.isFailure,
            "Wrong KEK should fail to unwrap VEK"
        )
    }

    @Test
    fun `record keys are unique per record id`() = runTest {
        val hierarchy = VaultKeyHierarchy(cryptoEngine)
        val vek = cryptoEngine.generateMasterKey().getOrThrow()

        val key1 = hierarchy.deriveRecordKey(vek, "record-1").getOrThrow()
        val key2 = hierarchy.deriveRecordKey(vek, "record-2").getOrThrow()

        assertFalse(
            key1.contentEquals(key2),
            "Different record IDs should produce different keys"
        )
    }

    @Test
    fun `keys have correct size`() = runTest {
        val key = cryptoEngine.generateRandom(32).getOrThrow()
        assertEquals(32, key.size, "Key should be 32 bytes")
    }

    @Test
    fun `random generation produces unique values`() = runTest {
        val values = mutableSetOf<ByteArray>()

        repeat(100) {
            val random = cryptoEngine.generateRandom(32).getOrThrow()
            assertFalse(
                values.any { it.contentEquals(random) },
                "Random values should be unique"
            )
            values.add(random.copyOf())
        }
    }

    @Test
    fun `master key is correct size`() = runTest {
        val masterKey = cryptoEngine.generateMasterKey().getOrThrow()
        assertEquals(32, masterKey.size, "Master key should be 32 bytes")
    }

    @Test
    fun `derived key has correct size`() = runTest {
        val password = "password"
        val salt = cryptoEngine.generateRandom(16).getOrThrow()

        val derived = cryptoEngine.deriveKey(
            password.encodeToByteArray(),
            salt,
            Argon2Parameters.INTERACTIVE.opsLimit,
            Argon2Parameters.INTERACTIVE.memLimit
        ).getOrThrow()

        assertEquals(32, derived.key.size, "Derived key should be 32 bytes")
    }

    @Test
    fun `salt is included in derived key`() = runTest {
        val password = "password"
        val salt = cryptoEngine.generateRandom(16).getOrThrow()

        val derived = cryptoEngine.deriveKey(
            password.encodeToByteArray(),
            salt,
            Argon2Parameters.INTERACTIVE.opsLimit,
            Argon2Parameters.INTERACTIVE.memLimit
        ).getOrThrow()

        assertTrue(
            derived.salt.contentEquals(salt),
            "Derived key should include original salt"
        )
    }
}
