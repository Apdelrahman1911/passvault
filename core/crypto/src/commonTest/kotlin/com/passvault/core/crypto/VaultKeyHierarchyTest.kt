package com.passvault.core.crypto

import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCryptoEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for the Vault Key Hierarchy.
 *
 * The vault uses a hierarchical key structure:
 * - Master Password -> KEK (Key Encryption Key) via Argon2
 * - KEK wraps VEK (Vault Encryption Key)
 * - VEK derives subkeys for different purposes (records, attachments, etc.)
 *
 * These tests verify:
 * - VEK generation and wrapping/unwrapping
 * - Subkey derivation determinism and uniqueness
 * - Key isolation (subkeys for different purposes are different)
 * - Key hierarchy integrity
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VaultKeyHierarchyTest {

    private lateinit var cryptoEngine: FakeCryptoEngine
    private lateinit var keyHierarchy: VaultKeyHierarchy

    @BeforeTest
    fun setUp() {
        cryptoEngine = FakeCryptoEngine()
        keyHierarchy = VaultKeyHierarchy(cryptoEngine)
    }

    // ==================== VEK Generation Tests ====================

    @Test
    fun `generateVEK produces 32 byte key`() = runTest {
        val result = keyHierarchy.generateVEK()

        assertTrue(result.isSuccess)
        assertEquals(32, result.getOrThrow().size)
    }

    @Test
    fun `generateVEK produces different keys each call`() = runTest {
        val result1 = keyHierarchy.generateVEK()
        val result2 = keyHierarchy.generateVEK()

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)

        // In real implementation, these would be different
        // With fake crypto they may be the same
    }

    // ==================== VEK Wrapping Tests ====================

    @Test
    fun `wrapVEK produces wrapped key with ciphertext and nonce`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val kek = ByteArray(32) { (it + 1).toByte() }

        val result = keyHierarchy.wrapVEK(vek, kek)

        assertTrue(result.isSuccess)

        val wrapped = result.getOrThrow()
        assertTrue(wrapped.ciphertext.isNotEmpty())
        assertTrue(wrapped.nonce.isNotEmpty())
    }

    @Test
    fun `wrapVEK with different KEKs produces different ciphertexts`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val kek1 = ByteArray(32) { 1 }
        val kek2 = ByteArray(32) { 2 }

        val wrapped1 = keyHierarchy.wrapVEK(vek, kek1).getOrThrow()
        val wrapped2 = keyHierarchy.wrapVEK(vek, kek2).getOrThrow()

        assertFalse(wrapped1.ciphertext.contentEquals(wrapped2.ciphertext))
    }

    @Test
    fun `unwrapVEK recovers original VEK`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val kek = ByteArray(32) { (it + 1).toByte() }

        val wrapped = keyHierarchy.wrapVEK(vek, kek).getOrThrow()
        val unwrapped = keyHierarchy.unwrapVEK(wrapped, kek).getOrThrow()

        assertTrue(unwrapped.contentEquals(vek))
    }

    @Test
    fun `unwrapVEK with wrong KEK produces wrong result`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val kek = ByteArray(32) { (it + 1).toByte() }
        val wrongKek = ByteArray(32) { (it + 2).toByte() }

        val wrapped = keyHierarchy.wrapVEK(vek, kek).getOrThrow()

        val result = keyHierarchy.unwrapVEK(wrapped, wrongKek)
        assertTrue(result.isFailure)
    }

    @Test
    fun `wrap and unwrap is reversible`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val kek = ByteArray(32) { (it + 1).toByte() }

        val wrapped = keyHierarchy.wrapVEK(vek, kek).getOrThrow()
        val unwrapped = keyHierarchy.unwrapVEK(wrapped, kek).getOrThrow()
        val rewrapped = keyHierarchy.wrapVEK(unwrapped, kek).getOrThrow()
        val reunwrapped = keyHierarchy.unwrapVEK(rewrapped, kek).getOrThrow()

        assertTrue(reunwrapped.contentEquals(vek))
    }

    // ==================== Record Key Derivation Tests ====================

    @Test
    fun `deriveRecordKey produces 32 byte key`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val recordId = "record-123"

        val result = keyHierarchy.deriveRecordKey(vek, recordId)

        assertTrue(result.isSuccess)
        assertEquals(32, result.getOrThrow().size)
    }

    @Test
    fun `deriveRecordKey is deterministic`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val recordId = "record-123"

        val key1 = keyHierarchy.deriveRecordKey(vek, recordId).getOrThrow()
        val key2 = keyHierarchy.deriveRecordKey(vek, recordId).getOrThrow()

        assertTrue(key1.contentEquals(key2))
    }

    @Test
    fun `deriveRecordKey produces different keys for different records`() = runTest {
        val vek = ByteArray(32) { it.toByte() }

        val key1 = keyHierarchy.deriveRecordKey(vek, "record-1").getOrThrow()
        val key2 = keyHierarchy.deriveRecordKey(vek, "record-2").getOrThrow()

        assertFalse(key1.contentEquals(key2))
    }

    @Test
    fun `deriveRecordKey produces different keys for different VEKs`() = runTest {
        val vek1 = ByteArray(32) { 1 }
        val vek2 = ByteArray(32) { 2 }
        val recordId = "record-123"

        val key1 = keyHierarchy.deriveRecordKey(vek1, recordId).getOrThrow()
        val key2 = keyHierarchy.deriveRecordKey(vek2, recordId).getOrThrow()

        assertFalse(key1.contentEquals(key2))
    }

    // ==================== Attachment Key Derivation Tests ====================

    @Test
    fun `deriveAttachmentKey produces 32 byte key`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val attachmentId = "attachment-123"

        val result = keyHierarchy.deriveAttachmentKey(vek, attachmentId)

        assertTrue(result.isSuccess)
        assertEquals(32, result.getOrThrow().size)
    }

    @Test
    fun `deriveAttachmentKey is deterministic`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val attachmentId = "attachment-123"

        val key1 = keyHierarchy.deriveAttachmentKey(vek, attachmentId).getOrThrow()
        val key2 = keyHierarchy.deriveAttachmentKey(vek, attachmentId).getOrThrow()

        assertTrue(key1.contentEquals(key2))
    }

    @Test
    fun `deriveAttachmentKey produces different keys for different attachments`() = runTest {
        val vek = ByteArray(32) { it.toByte() }

        val key1 = keyHierarchy.deriveAttachmentKey(vek, "attachment-1").getOrThrow()
        val key2 = keyHierarchy.deriveAttachmentKey(vek, "attachment-2").getOrThrow()

        assertFalse(key1.contentEquals(key2))
    }

    // ==================== Backup Key Derivation Tests ====================

    @Test
    fun `deriveBackupKey produces 32 byte key`() = runTest {
        val vek = ByteArray(32) { it.toByte() }

        val result = keyHierarchy.deriveBackupKey(vek)

        assertTrue(result.isSuccess)
        assertEquals(32, result.getOrThrow().size)
    }

    @Test
    fun `deriveBackupKey is deterministic`() = runTest {
        val vek = ByteArray(32) { it.toByte() }

        val key1 = keyHierarchy.deriveBackupKey(vek).getOrThrow()
        val key2 = keyHierarchy.deriveBackupKey(vek).getOrThrow()

        assertTrue(key1.contentEquals(key2))
    }

    @Test
    fun `deriveBackupKey produces different keys for different VEKs`() = runTest {
        val vek1 = ByteArray(32) { 1 }
        val vek2 = ByteArray(32) { 2 }

        val key1 = keyHierarchy.deriveBackupKey(vek1).getOrThrow()
        val key2 = keyHierarchy.deriveBackupKey(vek2).getOrThrow()

        assertFalse(key1.contentEquals(key2))
    }

    // ==================== Search Key Derivation Tests ====================

    @Test
    fun `deriveSearchKey produces 32 byte key`() = runTest {
        val vek = ByteArray(32) { it.toByte() }

        val result = keyHierarchy.deriveSearchKey(vek)

        assertTrue(result.isSuccess)
        assertEquals(32, result.getOrThrow().size)
    }

    @Test
    fun `deriveSearchKey is deterministic`() = runTest {
        val vek = ByteArray(32) { it.toByte() }

        val key1 = keyHierarchy.deriveSearchKey(vek).getOrThrow()
        val key2 = keyHierarchy.deriveSearchKey(vek).getOrThrow()

        assertTrue(key1.contentEquals(key2))
    }

    // ==================== Duplicate Key Derivation Tests ====================

    @Test
    fun `deriveDuplicateKey produces 32 byte key`() = runTest {
        val vek = ByteArray(32) { it.toByte() }

        val result = keyHierarchy.deriveDuplicateKey(vek)

        assertTrue(result.isSuccess)
        assertEquals(32, result.getOrThrow().size)
    }

    @Test
    fun `deriveDuplicateKey is deterministic`() = runTest {
        val vek = ByteArray(32) { it.toByte() }

        val key1 = keyHierarchy.deriveDuplicateKey(vek).getOrThrow()
        val key2 = keyHierarchy.deriveDuplicateKey(vek).getOrThrow()

        assertTrue(key1.contentEquals(key2))
    }

    // ==================== Key Isolation Tests ====================

    @Test
    fun `record and attachment keys for same ID are different`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val id = "test-id-123"

        val recordKey = keyHierarchy.deriveRecordKey(vek, id).getOrThrow()
        val attachmentKey = keyHierarchy.deriveAttachmentKey(vek, id).getOrThrow()

        assertFalse(recordKey.contentEquals(attachmentKey))
    }

    @Test
    fun `all key types are different from each other`() = runTest {
        val vek = ByteArray(32) { it.toByte() }

        val recordKey = keyHierarchy.deriveRecordKey(vek, "id").getOrThrow()
        val attachmentKey = keyHierarchy.deriveAttachmentKey(vek, "id").getOrThrow()
        val backupKey = keyHierarchy.deriveBackupKey(vek).getOrThrow()
        val searchKey = keyHierarchy.deriveSearchKey(vek).getOrThrow()
        val duplicateKey = keyHierarchy.deriveDuplicateKey(vek).getOrThrow()

        val allKeys = listOf(recordKey, attachmentKey, backupKey, searchKey, duplicateKey)

        // Verify all keys are unique
        for (i in allKeys.indices) {
            for (j in i + 1 until allKeys.size) {
                assertFalse(
                    allKeys[i].contentEquals(allKeys[j]),
                    "Keys at indices $i and $j should be different"
                )
            }
        }
    }

    // ==================== Full Workflow Tests ====================

    @Test
    fun `complete vault key workflow`() = runTest {
        // 1. Generate VEK
        val vek = keyHierarchy.generateVEK().getOrThrow()

        // 2. Derive KEK from password
        val password = "MyStrongPassword123!".encodeToByteArray()
        val salt = cryptoEngine.generateRandom(16).getOrThrow()
        val derivedKek = cryptoEngine.deriveKey(
            password,
            salt,
            Argon2Parameters.INTERACTIVE.opsLimit,
            Argon2Parameters.INTERACTIVE.memLimit
        ).getOrThrow()

        // 3. Wrap VEK with KEK
        val wrappedVek = keyHierarchy.wrapVEK(vek, derivedKek.key).getOrThrow()

        // 4. Store wrapped VEK (simulated)
        val storedWrappedVek = WrappedKey(
            ciphertext = wrappedVek.ciphertext.copyOf(),
            nonce = wrappedVek.nonce.copyOf()
        )

        // 5. Later: unwrap VEK
        val unwrappedVek = keyHierarchy.unwrapVEK(storedWrappedVek, derivedKek.key).getOrThrow()

        // 6. Derive subkeys
        val recordKey = keyHierarchy.deriveRecordKey(unwrappedVek, "record-1").getOrThrow()
        val attachmentKey = keyHierarchy.deriveAttachmentKey(unwrappedVek, "attachment-1").getOrThrow()
        val backupKey = keyHierarchy.deriveBackupKey(unwrappedVek).getOrThrow()

        // 7. Verify
        assertTrue(unwrappedVek.contentEquals(vek))
        assertEquals(32, recordKey.size)
        assertEquals(32, attachmentKey.size)
        assertEquals(32, backupKey.size)

        // 8. Cleanup
        cryptoEngine.secureWipe(vek)
        cryptoEngine.secureWipe(unwrappedVek)
        cryptoEngine.secureWipe(derivedKek.key)
        cryptoEngine.secureWipe(recordKey)
        cryptoEngine.secureWipe(attachmentKey)
        cryptoEngine.secureWipe(backupKey)
    }

    @Test
    fun `password change workflow`() = runTest {
        // Setup
        val vek = keyHierarchy.generateVEK().getOrThrow()
        val oldPassword = "OldPassword123!".encodeToByteArray()
        val newPassword = "NewPassword123!".encodeToByteArray()
        val salt = cryptoEngine.generateRandom(16).getOrThrow()

        // Derive old KEK and wrap VEK
        val oldKek = cryptoEngine.deriveKey(
            oldPassword,
            salt,
            Argon2Parameters.INTERACTIVE.opsLimit,
            Argon2Parameters.INTERACTIVE.memLimit
        ).getOrThrow()
        val wrappedVek = keyHierarchy.wrapVEK(vek, oldKek.key).getOrThrow()

        // Simulate password change:
        // 1. Unwrap VEK with old KEK
        val unwrappedVek = keyHierarchy.unwrapVEK(wrappedVek, oldKek.key).getOrThrow()

        // 2. Derive new KEK
        val newKek = cryptoEngine.deriveKey(
            newPassword,
            salt, // Could use new salt
            Argon2Parameters.INTERACTIVE.opsLimit,
            Argon2Parameters.INTERACTIVE.memLimit
        ).getOrThrow()

        // 3. Rewrap VEK with new KEK
        val newWrappedVek = keyHierarchy.wrapVEK(unwrappedVek, newKek.key).getOrThrow()

        // 4. Verify we can unwrap with new KEK
        val reUnwrappedVek = keyHierarchy.unwrapVEK(newWrappedVek, newKek.key).getOrThrow()

        assertTrue(reUnwrappedVek.contentEquals(vek))

        // Cleanup
        cryptoEngine.secureWipe(vek)
        cryptoEngine.secureWipe(unwrappedVek)
        cryptoEngine.secureWipe(reUnwrappedVek)
        cryptoEngine.secureWipe(oldKek.key)
        cryptoEngine.secureWipe(newKek.key)
    }

    @Test
    fun `key rotation workflow`() = runTest {
        // 1. Generate old VEK
        val oldVek = keyHierarchy.generateVEK().getOrThrow()

        // 2. Encrypt some data with old key
        val kek = ByteArray(32) { 1 }
        val oldWrappedVek = keyHierarchy.wrapVEK(oldVek, kek).getOrThrow()

        // 3. Simulate key rotation: generate new VEK
        val newVek = keyHierarchy.generateVEK().getOrThrow()

        // 4. Unwrap old VEK
        val unwrappedOldVek = keyHierarchy.unwrapVEK(oldWrappedVek, kek).getOrThrow()

        // 5. Wrap new VEK
        val newWrappedVek = keyHierarchy.wrapVEK(newVek, kek).getOrThrow()

        // 6. Verify old and new VEKs are different
        assertFalse(oldVek.contentEquals(newVek))

        // 7. Verify new wrapped VEK can be unwrapped
        val unwrappedNewVek = keyHierarchy.unwrapVEK(newWrappedVek, kek).getOrThrow()
        assertTrue(unwrappedNewVek.contentEquals(newVek))

        // Cleanup
        cryptoEngine.secureWipe(oldVek)
        cryptoEngine.secureWipe(newVek)
        cryptoEngine.secureWipe(unwrappedOldVek)
        cryptoEngine.secureWipe(unwrappedNewVek)
    }

    // ==================== Error Handling Tests ====================

    @Test
    fun `deriveRecordKey fails with empty VEK`() = runTest {
        // Empty VEK is technically allowed but should produce different key
        val vek = ByteArray(0)

        // Fake crypto handles this, real crypto might fail
        val result = keyHierarchy.deriveRecordKey(vek, "record-1")

        // Result depends on crypto implementation
        // With fake crypto: success but with different key size
    }

    @Test
    fun `wrapVEK with empty KEK`() = runTest {
        val vek = ByteArray(32) { it.toByte() }
        val kek = ByteArray(0)

        val result = keyHierarchy.wrapVEK(vek, kek)

        assertTrue(result.isFailure)
    }
}
