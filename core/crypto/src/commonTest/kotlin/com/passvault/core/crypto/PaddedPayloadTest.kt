package com.passvault.core.crypto

import com.passvault.core.testing.fakes.FakeCryptoEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaddedPayloadTest {
    @Test
    fun `lengths in the same bucket encode to the same size`() {
        assertEquals(32, PaddedPayload.encode(ByteArray(4), MAX_BYTES).size)
        assertEquals(32, PaddedPayload.encode(ByteArray(24), MAX_BYTES).size)
        assertEquals(64, PaddedPayload.encode(ByteArray(25), MAX_BYTES).size)
        assertEquals(64, PaddedPayload.encode(ByteArray(56), MAX_BYTES).size)
        assertEquals(128, PaddedPayload.encode(ByteArray(57), MAX_BYTES).size)
    }

    @Test
    fun `round trip preserves every byte including padding-like suffixes`() {
        listOf(
            byteArrayOf(),
            byteArrayOf(0),
            byteArrayOf(0x80.toByte()),
            ByteArray(24) { it.toByte() },
            ByteArray(25) { if (it == 24) 0 else it.toByte() },
            ByteArray(56) { if (it == 55) 0x80.toByte() else it.toByte() },
            ByteArray(57) { it.toByte() },
        ).forEach { plaintext ->
            val encoded = PaddedPayload.encode(plaintext, MAX_BYTES)
            assertContentEquals(plaintext, PaddedPayload.decode(encoded, MAX_BYTES))
        }
    }

    @Test
    fun `legacy v2 ciphertext remains readable and padded v3 ciphertext is distinguishable`() = runTest {
        val cryptoEngine = FakeCryptoEngine()
        val key = ByteArray(32) { it.toByte() }
        val aad = "record".encodeToByteArray()
        val plaintext = "legacy".encodeToByteArray()
        val legacy = cryptoEngine.encrypt(plaintext, key, aad).getOrThrow()
        val padded = PaddedPayload.encrypt(cryptoEngine, plaintext, key, aad, MAX_BYTES).getOrThrow()

        try {
            assertTrue(!CryptoEnvelope.isPaddedPayload(legacy.ciphertext))
            assertTrue(CryptoEnvelope.isPaddedPayload(padded.ciphertext))
            assertContentEquals(
                plaintext,
                PaddedPayload.decrypt(cryptoEngine, legacy.ciphertext, legacy.nonce, key, aad, MAX_BYTES)
                    .getOrThrow(),
            )
            assertContentEquals(
                plaintext,
                PaddedPayload.decrypt(cryptoEngine, padded.ciphertext, padded.nonce, key, aad, MAX_BYTES)
                    .getOrThrow(),
            )
        } finally {
            legacy.clear()
            padded.clear()
        }
    }

    @Test
    fun `different plaintext lengths collapse into encrypted buckets`() = runTest {
        val cryptoEngine = FakeCryptoEngine()
        val key = ByteArray(32)
        val aad = "bucket".encodeToByteArray()
        val lengths = listOf(4, 7, 12, 23, 24, 25, 31, 33, 56, 57)
        val storedLengths = lengths.map { length ->
            val encrypted = PaddedPayload.encrypt(
                cryptoEngine,
                ByteArray(length),
                key,
                aad,
                MAX_BYTES,
            ).getOrThrow()
            try {
                encrypted.ciphertext.size
            } finally {
                encrypted.clear()
            }
        }

        assertTrue(storedLengths.toSet().size < lengths.size)
        assertEquals(storedLengths[0], storedLengths[4])
        assertEquals(storedLengths[5], storedLengths[8])
        assertFalse(storedLengths.zipWithNext().any { (first, second) -> second < first })
    }

    @Test
    fun `downgrading the padded envelope marker fails authentication`() = runTest {
        val cryptoEngine = FakeCryptoEngine()
        val key = ByteArray(32)
        val aad = "record".encodeToByteArray()
        val encrypted = PaddedPayload.encrypt(
            cryptoEngine,
            "secret".encodeToByteArray(),
            key,
            aad,
            MAX_BYTES,
        ).getOrThrow()
        val downgraded = encrypted.ciphertext.copyOf().also { it[2] = 0x02 }

        assertTrue(
            PaddedPayload.decrypt(cryptoEngine, downgraded, encrypted.nonce, key, aad, MAX_BYTES).isFailure,
        )
        encrypted.clear()
        downgraded.fill(0)
    }

    @Test
    fun `malformed authenticated framing fails closed`() {
        val nonZeroPadding = PaddedPayload.encode(byteArrayOf(1), MAX_BYTES).also { it[it.lastIndex] = 1 }
        val impossibleLength = PaddedPayload.encode(byteArrayOf(1), MAX_BYTES).also {
            it[4] = 0x7f
            it[5] = -1
            it[6] = -1
            it[7] = -1
        }
        val nonCanonicalBucket = PaddedPayload.encode(ByteArray(25), MAX_BYTES).copyOf(128)

        assertFailsWith<IllegalArgumentException> { PaddedPayload.decode(nonZeroPadding, MAX_BYTES) }
        assertFailsWith<IllegalArgumentException> { PaddedPayload.decode(impossibleLength, MAX_BYTES) }
        assertFailsWith<IllegalArgumentException> { PaddedPayload.decode(nonCanonicalBucket, MAX_BYTES) }
    }

    @Test
    fun `maximum size accounts for header and bucket expansion`() {
        assertEquals(32, PaddedPayload.maximumEncodedSize(24))
        assertEquals(128, PaddedPayload.maximumEncodedSize(64))
        assertEquals(56, PaddedPayload.maximumPlaintextSize(100))
        assertEquals(120, PaddedPayload.maximumPlaintextSize(128))
        assertFailsWith<IllegalArgumentException> { PaddedPayload.encode(ByteArray(65), 64) }
    }

    private companion object {
        const val MAX_BYTES = 1_024
    }
}
