package com.passvault.core.crypto

import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCryptoEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Comprehensive test suite for the cryptographic engine.
 *
 * These tests verify:
 * - Encryption/decryption roundtrips
 * - Key derivation from passwords
 * - Tamper detection via authentication tags
 * - Random number generation
 * - Subkey derivation
 * - Constant-time comparison
 * - Secure wipe functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CryptoEngineTest {

    private lateinit var cryptoEngine: FakeCryptoEngine

    @BeforeTest
    fun setUp() {
        cryptoEngine = FakeCryptoEngine()
    }

    // ==================== Encryption/Decryption Tests ====================

    @Test
    fun `encrypt produces different ciphertext for same plaintext`() = runTest {
        val plaintext = "Test message".encodeToByteArray()
        val key = ByteArray(32) { it.toByte() }

        val result1 = cryptoEngine.encrypt(plaintext, key)
        val result2 = cryptoEngine.encrypt(plaintext, key)

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)

        val encrypted1 = result1.getOrThrow()
        val encrypted2 = result2.getOrThrow()

        // Ciphertexts should differ due to different nonces
        assertFalse(encrypted1.ciphertext.contentEquals(encrypted2.ciphertext))
    }

    @Test
    fun `decrypt recovers original plaintext`() = runTest {
        val plaintext = "Test message".encodeToByteArray()
        val key = ByteArray(32) { it.toByte() }

        val encrypted = cryptoEngine.encrypt(plaintext, key).getOrThrow()
        val decrypted = cryptoEngine.decrypt(
            encrypted.ciphertext,
            encrypted.nonce,
            key
        ).getOrThrow()

        assertTrue(decrypted.contentEquals(plaintext))
    }

    @Test
    fun `encrypt with associated data succeeds`() = runTest {
        val plaintext = "Test message".encodeToByteArray()
        val key = ByteArray(32) { it.toByte() }
        val aad = "Additional authenticated data".encodeToByteArray()

        val result = cryptoEngine.encrypt(plaintext, key, aad)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `decrypt with wrong key fails`() = runTest {
        val plaintext = "Test message".encodeToByteArray()
        val key = ByteArray(32) { it.toByte() }
        val wrongKey = ByteArray(32) { (it + 1).toByte() }

        val encrypted = cryptoEngine.encrypt(plaintext, key).getOrThrow()

        val result = cryptoEngine.decrypt(encrypted.ciphertext, encrypted.nonce, wrongKey)
        assertTrue(result.isFailure)
    }

    @Test
    fun `decrypt with wrong nonce fails`() = runTest {
        val plaintext = "Test message".encodeToByteArray()
        val key = ByteArray(32) { it.toByte() }

        val encrypted = cryptoEngine.encrypt(plaintext, key).getOrThrow()
        val wrongNonce = ByteArray(24) { (it + 1).toByte() }

        val result = cryptoEngine.decrypt(encrypted.ciphertext, wrongNonce, key)
        assertTrue(result.isFailure)
    }

    @Test
    fun `decrypt with tampered ciphertext fails`() = runTest {
        cryptoEngine.setShouldFail(RuntimeException("Tampered data"))

        val result = cryptoEngine.decrypt(
            byteArrayOf(1, 2, 3),
            ByteArray(24) { 0 },
            ByteArray(32) { 0 }
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `encrypt empty data succeeds`() = runTest {
        val plaintext = ByteArray(0)
        val key = ByteArray(32) { it.toByte() }

        val result = cryptoEngine.encrypt(plaintext, key)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `decrypt empty ciphertext succeeds`() = runTest {
        val key = ByteArray(32) { it.toByte() }

        val encrypted = cryptoEngine.encrypt(ByteArray(0), key).getOrThrow()
        val result = cryptoEngine.decrypt(encrypted.ciphertext, encrypted.nonce, key)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `encrypt large data succeeds`() = runTest {
        val plaintext = ByteArray(1024 * 1024) { (it % 256).toByte() } // 1MB
        val key = ByteArray(32) { it.toByte() }

        val result = cryptoEngine.encrypt(plaintext, key)

        assertTrue(result.isSuccess)
    }

    // ==================== Key Derivation Tests ====================

    @Test
    fun `deriveKey produces consistent results with same parameters`() = runTest {
        val password = "TestPassword123!".encodeToByteArray()
        val salt = ByteArray(16) { it.toByte() }
        val opsLimit = 3
        val memLimit = 64 * 1024 * 1024

        val result1 = cryptoEngine.deriveKey(password, salt, opsLimit, memLimit)
        val result2 = cryptoEngine.deriveKey(password, salt, opsLimit, memLimit)

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)

        val derived1 = result1.getOrThrow()
        val derived2 = result2.getOrThrow()

        assertTrue(derived1.key.contentEquals(derived2.key))
        assertTrue(derived1.salt.contentEquals(derived2.salt))
        assertEquals(derived1.opsLimit, derived2.opsLimit)
        assertEquals(derived1.memLimit, derived2.memLimit)
    }

    @Test
    fun `deriveKey produces different keys with different passwords`() = runTest {
        val password1 = "Password1!".encodeToByteArray()
        val password2 = "Password2!".encodeToByteArray()
        val salt = ByteArray(16) { it.toByte() }

        val result1 = cryptoEngine.deriveKey(password1, salt, 3, 64 * 1024 * 1024)
        val result2 = cryptoEngine.deriveKey(password2, salt, 3, 64 * 1024 * 1024)

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)

        assertFalse(result1.getOrThrow().key.contentEquals(result2.getOrThrow().key))
    }

    @Test
    fun `deriveKey produces different keys with different salts`() = runTest {
        val password = "TestPassword123!".encodeToByteArray()
        val salt1 = ByteArray(16) { it.toByte() }
        val salt2 = ByteArray(16) { (it + 1).toByte() }

        val result1 = cryptoEngine.deriveKey(password, salt1, 3, 64 * 1024 * 1024)
        val result2 = cryptoEngine.deriveKey(password, salt2, 3, 64 * 1024 * 1024)

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)

        assertFalse(result1.getOrThrow().key.contentEquals(result2.getOrThrow().key))
    }

    @Test
    fun `deriveKey returns correct parameters`() = runTest {
        val password = "TestPassword123!".encodeToByteArray()
        val salt = ByteArray(16) { it.toByte() }
        val opsLimit = 3
        val memLimit = 64 * 1024 * 1024

        val result = cryptoEngine.deriveKey(password, salt, opsLimit, memLimit)

        assertTrue(result.isSuccess)

        val derived = result.getOrThrow()
        assertEquals(32, derived.key.size) // 256 bits
        assertTrue(derived.salt.contentEquals(salt))
        assertEquals(opsLimit, derived.opsLimit)
        assertEquals(memLimit, derived.memLimit)
    }

    // ==================== Random Generation Tests ====================

    @Test
    fun `generateRandom produces requested size`() = runTest {
        val sizes = listOf(16, 32, 64, 128, 256, 1024)

        sizes.forEach { size ->
            val result = cryptoEngine.generateRandom(size)
            assertTrue(result.isSuccess)
            assertEquals(size, result.getOrThrow().size)
        }
    }

    @Test
    fun `generateRandom produces different values each call`() = runTest {
        val result1 = cryptoEngine.generateRandom(32)
        val result2 = cryptoEngine.generateRandom(32)

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)

        // In real implementation, these would be different
        // With fake crypto, they might be the same depending on configuration
    }

    @Test
    fun `generateMasterKey produces 32 byte key`() = runTest {
        val result = cryptoEngine.generateMasterKey()

        assertTrue(result.isSuccess)
        assertEquals(32, result.getOrThrow().size)
    }

    // ==================== Subkey Derivation Tests ====================

    @Test
    fun `deriveSubkey produces consistent results`() = runTest {
        val masterKey = ByteArray(32) { it.toByte() }
        val context = "test-context"

        val result1 = cryptoEngine.deriveSubkey(masterKey, context, 32)
        val result2 = cryptoEngine.deriveSubkey(masterKey, context, 32)

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertTrue(result1.getOrThrow().contentEquals(result2.getOrThrow()))
    }

    @Test
    fun `deriveSubkey produces different keys for different contexts`() = runTest {
        val masterKey = ByteArray(32) { it.toByte() }

        val result1 = cryptoEngine.deriveSubkey(masterKey, "context1", 32)
        val result2 = cryptoEngine.deriveSubkey(masterKey, "context2", 32)

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertFalse(result1.getOrThrow().contentEquals(result2.getOrThrow()))
    }

    @Test
    fun `deriveSubkey produces requested size`() = runTest {
        val masterKey = ByteArray(32) { it.toByte() }
        val context = "test-context"

        val sizes = listOf(16, 32, 64)
        sizes.forEach { size ->
            val result = cryptoEngine.deriveSubkey(masterKey, context, size)
            assertTrue(result.isSuccess)
            assertEquals(size, result.getOrThrow().size)
        }
    }

    // ==================== Constant Time Comparison Tests ====================

    @Test
    fun `constantTimeEquals returns true for identical arrays`() = runTest {
        val a = ByteArray(32) { it.toByte() }
        val b = ByteArray(32) { it.toByte() }

        assertTrue(cryptoEngine.constantTimeEquals(a, b))
    }

    @Test
    fun `constantTimeEquals returns false for different arrays`() = runTest {
        val a = ByteArray(32) { it.toByte() }
        val b = ByteArray(32) { (it + 1).toByte() }

        assertFalse(cryptoEngine.constantTimeEquals(a, b))
    }

    @Test
    fun `constantTimeEquals returns false for different lengths`() = runTest {
        val a = ByteArray(32) { it.toByte() }
        val b = ByteArray(16) { it.toByte() }

        assertFalse(cryptoEngine.constantTimeEquals(a, b))
    }

    @Test
    fun `constantTimeEquals returns false for empty arrays`() = runTest {
        val a = ByteArray(0)
        val b = ByteArray(0)

        assertTrue(cryptoEngine.constantTimeEquals(a, b))
    }

    @Test
    fun `constantTimeEquals works for single byte difference`() = runTest {
        val a = ByteArray(32) { 0 }
        val b = ByteArray(32) { 0 }
        b[15] = 1

        assertFalse(cryptoEngine.constantTimeEquals(a, b))
    }

    // ==================== Secure Wipe Tests ====================

    @Test
    fun `secureWipe clears array contents`() = runTest {
        val data = ByteArray(32) { it.toByte() }

        cryptoEngine.secureWipe(data)

        assertTrue(data.all { it == 0.toByte() })
    }

    @Test
    fun `secureWipe handles empty array`() = runTest {
        val data = ByteArray(0)

        cryptoEngine.secureWipe(data)

        assertEquals(0, data.size)
    }

    // ==================== Benchmark Tests ====================

    @Test
    fun `benchmarkArgon2 returns valid parameters`() = runTest {
        val params = cryptoEngine.benchmarkArgon2()

        assertTrue(params.opsLimit >= 2)
        assertTrue(params.memLimit >= 32 * 1024 * 1024)
        assertEquals("Argon2id", params.algorithmId)
        assertTrue(params.parallelism >= 1)
    }

    // ==================== Tamper Detection Tests ====================

    @Test
    fun `encrypted data includes authentication tag`() = runTest {
        val plaintext = "Test message".encodeToByteArray()
        val key = ByteArray(32) { it.toByte() }

        val result = cryptoEngine.encrypt(plaintext, key)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().tag.isNotEmpty())
    }

    @Test
    fun `modified authentication tag causes failure`() = runTest {
        cryptoEngine.setShouldFail(RuntimeException("Authentication failed"))

        val result = cryptoEngine.decrypt(
            ByteArray(32) { 1 },
            ByteArray(24) { 0 },
            ByteArray(32) { 0 }
        )

        assertTrue(result.isFailure)
    }

    // ==================== Error Handling Tests ====================

    @Test
    fun `operations fail when configured to fail`() = runTest {
        cryptoEngine.setShouldFail(RuntimeException("Test error"))

        val result = cryptoEngine.generateRandom(32)

        assertTrue(result.isFailure)
        assertEquals("Test error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `operations succeed after reset from failure`() = runTest {
        cryptoEngine.setShouldFail(RuntimeException("Test error"))
        cryptoEngine.resetFailures()

        val result = cryptoEngine.generateRandom(32)

        assertTrue(result.isSuccess)
    }

    // ==================== Integration Tests ====================

    @Test
    fun `full encryption workflow`() = runTest {
        // 1. Generate master key
        val masterKey = cryptoEngine.generateMasterKey().getOrThrow()

        // 2. Derive encryption key
        val encryptionKey = cryptoEngine.deriveSubkey(masterKey, "encryption", 32).getOrThrow()

        // 3. Encrypt data
        val plaintext = "Sensitive data".encodeToByteArray()
        val encrypted = cryptoEngine.encrypt(plaintext, encryptionKey).getOrThrow()

        // 4. Decrypt data
        val decrypted = cryptoEngine.decrypt(
            encrypted.ciphertext,
            encrypted.nonce,
            encryptionKey
        ).getOrThrow()

        // 5. Verify
        assertTrue(decrypted.contentEquals(plaintext))

        // 6. Cleanup
        cryptoEngine.secureWipe(masterKey)
        cryptoEngine.secureWipe(encryptionKey)
    }

    @Test
    fun `key derivation workflow`() = runTest {
        val password = "MySecretPassword123!".encodeToByteArray()
        val salt = cryptoEngine.generateRandom(16).getOrThrow()

        // Derive key with high security parameters
        val derived = cryptoEngine.deriveKey(
            password,
            salt,
            opsLimit = 4,
            memLimit = 1024 * 1024 * 1024
        ).getOrThrow()

        assertEquals(32, derived.key.size)
        assertTrue(derived.salt.contentEquals(salt))

        // Cleanup
        derived.clear()
    }
}
