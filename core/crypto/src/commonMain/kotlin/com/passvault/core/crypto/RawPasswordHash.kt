package com.passvault.core.crypto

/**
 * Calls libsodium's byte-oriented `crypto_pwhash` entry point. The public
 * multiplatform binding accepts only immutable strings, so each platform uses
 * the same loaded native library through a narrower mutable-buffer adapter.
 */
internal expect fun rawPasswordHash(
    outputLength: Int,
    password: ByteArray,
    salt: ByteArray,
    opsLimit: Int,
    memLimit: Int,
    algorithm: Int,
): ByteArray

/**
 * Retains PassVault's historical KDF input exactly: lowercase ASCII hex of
 * the caller's bytes. The temporary is mutable and cleared on every exit.
 */
internal inline fun <T> ByteArray.withLowercaseHexBytes(block: (ByteArray) -> T): T {
    val encoded = toLowercaseHexByteArray()
    return try {
        block(encoded)
    } finally {
        encoded.fill(0)
    }
}

internal fun ByteArray.toLowercaseHexByteArray(): ByteArray {
    require(size <= Int.MAX_VALUE / HEX_BYTES_PER_INPUT_BYTE) { "Password is too large" }
    val encoded = ByteArray(size * HEX_BYTES_PER_INPUT_BYTE)
    forEachIndexed { index, byte ->
        val value = byte.toInt() and BYTE_MASK
        encoded[index * HEX_BYTES_PER_INPUT_BYTE] = (value ushr NIBBLE_BITS).lowercaseHexDigit()
        encoded[index * HEX_BYTES_PER_INPUT_BYTE + 1] = (value and NIBBLE_MASK).lowercaseHexDigit()
    }
    return encoded
}

private fun Int.lowercaseHexDigit(): Byte =
    if (this < DECIMAL_DIGITS) {
        (ASCII_ZERO + this).toByte()
    } else {
        (ASCII_LOWER_A + this - DECIMAL_DIGITS).toByte()
    }

private const val HEX_BYTES_PER_INPUT_BYTE = 2
private const val NIBBLE_BITS = 4
private const val NIBBLE_MASK = 0x0F
private const val BYTE_MASK = 0xFF
private const val DECIMAL_DIGITS = 10
private const val ASCII_ZERO = 0x30
private const val ASCII_LOWER_A = 0x61
