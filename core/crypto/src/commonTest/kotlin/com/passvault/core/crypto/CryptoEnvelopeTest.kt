package com.passvault.core.crypto

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CryptoEnvelopeTest {
    @Test
    fun `encode preserves one authenticated tag for a versioned ciphertext`() {
        val tag = ByteArray(TAG_BYTES) { it.toByte() }
        val ciphertext = MAGIC + byteArrayOf(1, 2, 3) + tag

        val stored = CryptoEnvelope.encode(
            EncryptedData(ciphertext = ciphertext, nonce = ByteArray(24), tag = tag),
        )

        assertContentEquals(ciphertext, stored)
        assertTrue(CryptoEnvelope.isSupportedPayload(stored))
    }

    @Test
    fun `encode rejects a versioned ciphertext whose separate tag disagrees`() {
        val ciphertext = MAGIC + byteArrayOf(1, 2, 3) + ByteArray(TAG_BYTES) { 1 }

        assertFailsWith<IllegalArgumentException> {
            CryptoEnvelope.encode(
                EncryptedData(
                    ciphertext = ciphertext,
                    nonce = ByteArray(24),
                    tag = ByteArray(TAG_BYTES) { 2 },
                ),
            )
        }
    }

    @Test
    fun `normalize removes only an identical duplicated trailing tag`() {
        val tag = ByteArray(TAG_BYTES) { (it + 1).toByte() }
        val canonical = MAGIC + byteArrayOf(9, 8, 7) + tag

        assertContentEquals(canonical, CryptoEnvelope.normalize(canonical + tag))
        assertContentEquals(canonical, CryptoEnvelope.normalize(canonical))
        assertFalse(CryptoEnvelope.isSupportedPayload(byteArrayOf(1, 2, 3)))
    }

    private companion object {
        const val TAG_BYTES = 16
        val MAGIC = byteArrayOf(0x50, 0x56, 0x02, 0x00)
    }
}
