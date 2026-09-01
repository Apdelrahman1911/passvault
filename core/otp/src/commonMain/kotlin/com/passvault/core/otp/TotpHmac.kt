package com.passvault.core.otp

import com.passvault.core.domain.model.TotpAlgorithm

/**
 * Computes TOTP's HMAC without converting the seed to immutable text or byte
 * wrappers. PassVault clears every application-owned temporary below; platform
 * digest implementations may still maintain runtime-internal state that the
 * application cannot prove has been erased.
 */
internal fun calculateTotpHmac(
    algorithm: TotpAlgorithm,
    key: ByteArray,
    message: ByteArray,
): ByteArray {
    require(key.isNotEmpty()) { "TOTP HMAC key must not be empty" }
    val blockSize = algorithm.hmacBlockSize()
    val keyBlock = ByteArray(blockSize)
    val pad = ByteArray(blockSize)
    var shortenedKey: ByteArray? = null
    var innerDigest: ByteArray? = null
    return try {
        val effectiveKey = if (key.size > blockSize) {
            platformTotpDigest(algorithm, key, EMPTY_BYTES).also { shortenedKey = it }
        } else {
            key
        }
        effectiveKey.copyInto(keyBlock)

        keyBlock.indices.forEach { index ->
            pad[index] = (keyBlock[index].toInt() xor INNER_PAD).toByte()
        }
        val inner = platformTotpDigest(algorithm, pad, message)
        innerDigest = inner

        keyBlock.indices.forEach { index ->
            pad[index] = (keyBlock[index].toInt() xor OUTER_PAD).toByte()
        }
        platformTotpDigest(algorithm, pad, inner)
    } finally {
        shortenedKey?.fill(0)
        innerDigest?.fill(0)
        keyBlock.fill(0)
        pad.fill(0)
    }
}

/** Hashes two buffers in order without joining them into another managed array. */
internal expect fun platformTotpDigest(
    algorithm: TotpAlgorithm,
    first: ByteArray,
    second: ByteArray,
): ByteArray

private fun TotpAlgorithm.hmacBlockSize(): Int = when (this) {
    TotpAlgorithm.SHA1,
    TotpAlgorithm.SHA256,
    -> SHA_256_BLOCK_BYTES
    TotpAlgorithm.SHA512 -> SHA_512_BLOCK_BYTES
}

private val EMPTY_BYTES = ByteArray(0)
private const val INNER_PAD = 0x36
private const val OUTER_PAD = 0x5C
private const val SHA_256_BLOCK_BYTES = 64
private const val SHA_512_BLOCK_BYTES = 128
