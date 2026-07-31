package com.passvault.core.crypto

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Test suite for Argon2 password hashing parameters.
 *
 * Argon2id is the recommended algorithm for password hashing.
 * These tests verify:
 * - Predefined parameter sets are valid
 * - Parameter bounds checking
 * - Memory and iteration requirements
 * - Security level appropriateness
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Argon2Test {

    // ==================== Parameter Set Tests ====================

    @Test
    fun `INTERACTIVE parameters meet minimum requirements`() {
        val params = Argon2Parameters.INTERACTIVE

        assertTrue(params.opsLimit >= 2, "Ops limit should be at least 2")
        assertTrue(params.memLimit >= 32 * 1024 * 1024, "Memory limit should be at least 32MB")
        assertTrue(params.parallelism >= 1, "Parallelism should be at least 1")
        assertEquals("Argon2id", params.algorithmId)
        assertEquals(19, params.version)
    }

    @Test
    fun `MODERATE parameters are stronger than INTERACTIVE`() {
        val interactive = Argon2Parameters.INTERACTIVE
        val moderate = Argon2Parameters.MODERATE

        assertTrue(
            moderate.memLimit >= interactive.memLimit,
            "MODERATE should use at least as much memory as INTERACTIVE"
        )
        assertTrue(
            moderate.opsLimit >= interactive.opsLimit,
            "MODERATE should use at least as many operations as INTERACTIVE"
        )
    }

    @Test
    fun `SENSITIVE parameters are stronger than MODERATE`() {
        val moderate = Argon2Parameters.MODERATE
        val sensitive = Argon2Parameters.SENSITIVE

        assertTrue(
            sensitive.memLimit >= moderate.memLimit,
            "SENSITIVE should use at least as much memory as MODERATE"
        )
        assertTrue(
            sensitive.opsLimit >= moderate.opsLimit,
            "SENSITIVE should use at least as many operations as MODERATE"
        )
    }

    @Test
    fun `SENSITIVE parameters use at least 1GB memory`() {
        val sensitive = Argon2Parameters.SENSITIVE

        assertTrue(
            sensitive.memLimit >= 1024 * 1024 * 1024,
            "SENSITIVE should use at least 1GB memory"
        )
    }

    @Test
    fun `MINIMUM parameters meet absolute minimum`() {
        val minimum = Argon2Parameters.MINIMUM

        assertTrue(minimum.opsLimit >= 2, "MINIMUM ops limit should be at least 2")
        assertTrue(minimum.memLimit >= 32 * 1024 * 1024, "MINIMUM memory should be at least 32MB")
    }

    // ==================== Parameter Comparison Tests ====================

    @Test
    fun `memory limits increase with security level`() {
        assertTrue(Argon2Parameters.MINIMUM.memLimit <= Argon2Parameters.INTERACTIVE.memLimit)
        assertTrue(Argon2Parameters.INTERACTIVE.memLimit <= Argon2Parameters.MODERATE.memLimit)
        assertTrue(Argon2Parameters.MODERATE.memLimit <= Argon2Parameters.SENSITIVE.memLimit)
    }

    @Test
    fun `ops limits are reasonable`() {
        // Ops limits shouldn't be excessive (max 10)
        val allParams = listOf(
            Argon2Parameters.MINIMUM,
            Argon2Parameters.INTERACTIVE,
            Argon2Parameters.MODERATE,
            Argon2Parameters.SENSITIVE
        )

        allParams.forEach { params ->
            assertTrue(params.opsLimit <= 10, "Ops limit should not exceed 10")
            assertTrue(params.opsLimit >= 1, "Ops limit should be at least 1")
        }
    }

    // ==================== Custom Parameter Tests ====================

    @Test
    fun `custom parameters can be created`() {
        val custom = Argon2Parameters(
            algorithmId = "Argon2id",
            version = 19,
            opsLimit = 5,
            memLimit = 512 * 1024 * 1024,
            parallelism = 2
        )

        assertEquals("Argon2id", custom.algorithmId)
        assertEquals(19, custom.version)
        assertEquals(5, custom.opsLimit)
        assertEquals(512 * 1024 * 1024, custom.memLimit)
        assertEquals(2, custom.parallelism)
    }

    @Test
    fun `custom parameters with different algorithm`() {
        // Argon2d (not recommended for passwords, but testable)
        val argon2d = Argon2Parameters(
            algorithmId = "Argon2d",
            version = 19,
            opsLimit = 3,
            memLimit = 64 * 1024 * 1024,
            parallelism = 1
        )

        assertEquals("Argon2d", argon2d.algorithmId)
    }

    @Test
    fun `custom parameters with different version`() {
        // Version 16 (0x10) is the first version
        val oldVersion = Argon2Parameters(
            algorithmId = "Argon2id",
            version = 16,
            opsLimit = 3,
            memLimit = 64 * 1024 * 1024,
            parallelism = 1
        )

        assertEquals(16, oldVersion.version)
    }

    // ==================== Data Class Behavior Tests ====================

    @Test
    fun `Argon2Parameters equality works correctly`() {
        val params1 = Argon2Parameters(opsLimit = 3, memLimit = 64 * 1024 * 1024)
        val params2 = Argon2Parameters(opsLimit = 3, memLimit = 64 * 1024 * 1024)
        val params3 = Argon2Parameters(opsLimit = 4, memLimit = 64 * 1024 * 1024)

        assertEquals(params1, params2)
        assertNotEquals(params1, params3)
    }

    @Test
    fun `Argon2Parameters copy works correctly`() {
        val original = Argon2Parameters.INTERACTIVE
        val modified = original.copy(opsLimit = 5)

        assertEquals(original.memLimit, modified.memLimit)
        assertEquals(original.parallelism, modified.parallelism)
        assertEquals(5, modified.opsLimit)
        assertNotEquals(original.opsLimit, modified.opsLimit)
    }

    @Test
    fun `Argon2Parameters hashCode is consistent`() {
        val params1 = Argon2Parameters(opsLimit = 3, memLimit = 64 * 1024 * 1024)
        val params2 = Argon2Parameters(opsLimit = 3, memLimit = 64 * 1024 * 1024)

        assertEquals(params1.hashCode(), params2.hashCode())
    }

    // ==================== DerivedKey Tests ====================

    @Test
    fun `DerivedKey equality works correctly`() = runTest {
        val key1 = DerivedKey(
            key = ByteArray(32) { it.toByte() },
            salt = ByteArray(16) { (it + 100).toByte() },
            opsLimit = 3,
            memLimit = 64 * 1024 * 1024
        )

        val key2 = DerivedKey(
            key = ByteArray(32) { it.toByte() },
            salt = ByteArray(16) { (it + 100).toByte() },
            opsLimit = 3,
            memLimit = 64 * 1024 * 1024
        )

        assertEquals(key1, key2)
        assertEquals(key1.hashCode(), key2.hashCode())
    }

    @Test
    fun `DerivedKey clear zeros sensitive data`() = runTest {
        val key = DerivedKey(
            key = ByteArray(32) { it.toByte() },
            salt = ByteArray(16) { (it + 100).toByte() },
            opsLimit = 3,
            memLimit = 64 * 1024 * 1024
        )

        key.clear()

        assertTrue(key.key.all { it == 0.toByte() })
        assertTrue(key.salt.all { it == 0.toByte() })
    }

    @Test
    fun `DerivedKey stores parameters correctly`() = runTest {
        val salt = ByteArray(16) { it.toByte() }
        val key = DerivedKey(
            key = ByteArray(32) { (it + 1).toByte() },
            salt = salt,
            opsLimit = 4,
            memLimit = 128 * 1024 * 1024
        )

        assertEquals(4, key.opsLimit)
        assertEquals(128 * 1024 * 1024, key.memLimit)
        assertTrue(key.salt.contentEquals(salt))
    }

    // ==================== EncryptedData Tests ====================

    @Test
    fun `EncryptedData equality works correctly`() = runTest {
        val data1 = EncryptedData(
            ciphertext = ByteArray(32) { it.toByte() },
            nonce = ByteArray(24) { (it + 50).toByte() },
            tag = ByteArray(16) { (it + 100).toByte() }
        )

        val data2 = EncryptedData(
            ciphertext = ByteArray(32) { it.toByte() },
            nonce = ByteArray(24) { (it + 50).toByte() },
            tag = ByteArray(16) { (it + 100).toByte() }
        )

        assertEquals(data1, data2)
        assertEquals(data1.hashCode(), data2.hashCode())
    }

    @Test
    fun `EncryptedData clear zeros all fields`() = runTest {
        val data = EncryptedData(
            ciphertext = ByteArray(32) { 1 },
            nonce = ByteArray(24) { 2 },
            tag = ByteArray(16) { 3 }
        )

        data.clear()

        assertTrue(data.ciphertext.all { it == 0.toByte() })
        assertTrue(data.nonce.all { it == 0.toByte() })
        assertTrue(data.tag.all { it == 0.toByte() })
    }

    @Test
    fun `EncryptedData stores components correctly`() = runTest {
        val ciphertext = ByteArray(32) { 1 }
        val nonce = ByteArray(24) { 2 }
        val tag = ByteArray(16) { 3 }

        val data = EncryptedData(
            ciphertext = ciphertext,
            nonce = nonce,
            tag = tag
        )

        assertTrue(data.ciphertext.contentEquals(ciphertext))
        assertTrue(data.nonce.contentEquals(nonce))
        assertTrue(data.tag.contentEquals(tag))
    }

    // ==================== WrappedKey Tests ====================

    @Test
    fun `WrappedKey equality works correctly`() = runTest {
        val key1 = WrappedKey(
            ciphertext = ByteArray(32) { it.toByte() },
            nonce = ByteArray(24) { (it + 50).toByte() }
        )

        val key2 = WrappedKey(
            ciphertext = ByteArray(32) { it.toByte() },
            nonce = ByteArray(24) { (it + 50).toByte() }
        )

        assertEquals(key1, key2)
    }

    @Test
    fun `WrappedKey stores components correctly`() = runTest {
        val ciphertext = ByteArray(32) { 1 }
        val nonce = ByteArray(24) { 2 }

        val wrapped = WrappedKey(
            ciphertext = ciphertext,
            nonce = nonce
        )

        assertTrue(wrapped.ciphertext.contentEquals(ciphertext))
        assertTrue(wrapped.nonce.contentEquals(nonce))
    }

    // ==================== Security Level Tests ====================

    @Test
    fun `recommended parameters are documented`() {
        // Verify each parameter set has reasonable values
        val interactive = Argon2Parameters.INTERACTIVE
        assertEquals(3, interactive.opsLimit)
        assertEquals(64 * 1024 * 1024, interactive.memLimit) // 64MB

        val moderate = Argon2Parameters.MODERATE
        assertEquals(3, moderate.opsLimit)
        assertEquals(256 * 1024 * 1024, moderate.memLimit) // 256MB

        val sensitive = Argon2Parameters.SENSITIVE
        assertEquals(4, sensitive.opsLimit)
        assertEquals(1024 * 1024 * 1024, sensitive.memLimit) // 1GB

        val minimum = Argon2Parameters.MINIMUM
        assertEquals(2, minimum.opsLimit)
        assertEquals(32 * 1024 * 1024, minimum.memLimit) // 32MB
    }

    // ==================== Serialization Tests ====================

    @Test
    fun `Argon2Parameters can be used in collections`() {
        val params = listOf(
            Argon2Parameters.INTERACTIVE,
            Argon2Parameters.MODERATE,
            Argon2Parameters.SENSITIVE
        )

        assertEquals(3, params.size)
        assertTrue(params.contains(Argon2Parameters.INTERACTIVE))
    }

    @Test
    fun `DerivedKey components are accessible`() = runTest {
        val key = DerivedKey(
            key = ByteArray(32) { 1 },
            salt = ByteArray(16) { 2 },
            opsLimit = 3,
            memLimit = 64 * 1024 * 1024
        )

        assertNotNull(key.key)
        assertNotNull(key.salt)
        assertNotNull(key.opsLimit)
        assertNotNull(key.memLimit)
    }

    // ==================== Edge Case Tests ====================

    @Test
    fun `Argon2Parameters with zero memory is possible but not recommended`() {
        // Technically possible but not secure
        val params = Argon2Parameters(opsLimit = 1, memLimit = 0)

        assertEquals(0, params.memLimit)
    }

    @Test
    fun `Argon2Parameters with high parallelism`() {
        val params = Argon2Parameters(
            opsLimit = 3,
            memLimit = 64 * 1024 * 1024,
            parallelism = 8
        )

        assertEquals(8, params.parallelism)
    }

    @Test
    fun `DerivedKey with empty arrays`() = runTest {
        val key = DerivedKey(
            key = ByteArray(0),
            salt = ByteArray(0),
            opsLimit = 3,
            memLimit = 64 * 1024 * 1024
        )

        assertEquals(0, key.key.size)
        assertEquals(0, key.salt.size)
    }

    @Test
    fun `EncryptedData with empty components`() = runTest {
        val data = EncryptedData(
            ciphertext = ByteArray(0),
            nonce = ByteArray(0),
            tag = ByteArray(0)
        )

        assertEquals(0, data.ciphertext.size)
        assertEquals(0, data.nonce.size)
        assertEquals(0, data.tag.size)
    }

    // ==================== Benchmark Test ====================

    @Test
    fun `parameter selection guidance`() {
        // Document when to use each parameter set
        val guidance = mapOf(
            "Interactive mobile apps" to Argon2Parameters.INTERACTIVE,
            "Desktop applications" to Argon2Parameters.MODERATE,
            "High-security environments" to Argon2Parameters.SENSITIVE,
            "Testing/Development" to Argon2Parameters.MINIMUM,
        )

        assertEquals(4, guidance.size)
        assertTrue(guidance.containsKey("Interactive mobile apps"))
        assertTrue(guidance.containsKey("Desktop applications"))
        assertTrue(guidance.containsKey("High-security environments"))
        assertTrue(guidance.containsKey("Testing/Development"))
    }
}
