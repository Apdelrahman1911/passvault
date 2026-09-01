package com.passvault.core.crypto

/**
 * Versioned, authenticated bucket padding for variable-length encrypted data.
 *
 * New values use a distinct outer envelope marker and a format-specific AEAD
 * domain. The domain makes marker downgrades fail authentication, while the
 * marker permits unambiguous legacy reads without guessing from plaintext.
 */
object PaddedPayload {
    private const val HEADER_BYTES = 8
    private const val MIN_BUCKET_BYTES = 32
    private val MAGIC = byteArrayOf(0x50, 0x56, 0x50, 0x01)
    private val AAD_DOMAIN = "passvault:padded-payload:v1\u0000".encodeToByteArray()

    /** Maximum encrypted plaintext size for an input bounded by [maxPlaintextBytes]. */
    fun maximumEncodedSize(maxPlaintextBytes: Int): Int {
        require(maxPlaintextBytes >= 0) { "Maximum plaintext size must not be negative" }
        return bucketSize(maxPlaintextBytes.toLong() + HEADER_BYTES)
    }

    /** Largest plaintext whose padded representation fits [maxEncodedBytes]. */
    fun maximumPlaintextSize(maxEncodedBytes: Int): Int {
        require(maxEncodedBytes >= MIN_BUCKET_BYTES) { "Maximum encoded size is too small" }
        var bucket = MIN_BUCKET_BYTES
        while (bucket <= maxEncodedBytes / 2) bucket *= 2
        return bucket - HEADER_BYTES
    }

    /** Encrypts a padded value and marks its outer envelope as the padded format. */
    @Suppress("TooGenericExceptionCaught") // Result boundary preserves validation and engine failures.
    suspend fun encrypt(
        cryptoEngine: CryptoEngine,
        plaintext: ByteArray,
        key: ByteArray,
        associatedData: ByteArray? = null,
        maxPlaintextBytes: Int,
    ): Result<EncryptedData> {
        val encoded = encode(plaintext, maxPlaintextBytes)
        val versionedAssociatedData = versionedAssociatedData(associatedData)
        return try {
            val result = cryptoEngine.encrypt(encoded, key, versionedAssociatedData)
            val encrypted = result.getOrNull() ?: return result
            try {
                val paddedCiphertext = CryptoEnvelope.markPadded(encrypted.ciphertext)
                Result.success(
                    EncryptedData(
                        ciphertext = paddedCiphertext,
                        nonce = encrypted.nonce.copyOf(),
                        tag = encrypted.tag.copyOf(),
                    ),
                )
            } catch (error: Exception) {
                Result.failure(error)
            } finally {
                encrypted.clear()
            }
        } finally {
            cryptoEngine.secureWipe(encoded)
            cryptoEngine.secureWipe(versionedAssociatedData)
        }
    }

    /** Decrypts the padded format, while retaining explicit legacy-v2 read compatibility. */
    @Suppress("TooGenericExceptionCaught", "ReturnCount") // Result boundary clears each owned plaintext path.
    suspend fun decrypt(
        cryptoEngine: CryptoEngine,
        storedCiphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray? = null,
        maxPlaintextBytes: Int,
    ): Result<ByteArray> {
        val isPadded = CryptoEnvelope.isPaddedPayload(storedCiphertext)
        val normalized = CryptoEnvelope.normalize(storedCiphertext)
        val versionedAssociatedData = if (isPadded) versionedAssociatedData(associatedData) else associatedData
        try {
            val result = cryptoEngine.decrypt(normalized, nonce, key, versionedAssociatedData)
            val decrypted = result.getOrNull() ?: return result
            if (!isPadded) {
                if (decrypted.size <= maxPlaintextBytes) return Result.success(decrypted)
                cryptoEngine.secureWipe(decrypted)
                return Result.failure(IllegalArgumentException("Legacy plaintext exceeds the supported limit"))
            }
            return try {
                Result.success(decode(decrypted, maxPlaintextBytes))
            } catch (error: Exception) {
                Result.failure(error)
            } finally {
                cryptoEngine.secureWipe(decrypted)
            }
        } finally {
            cryptoEngine.secureWipe(normalized)
            if (isPadded) cryptoEngine.secureWipe(requireNotNull(versionedAssociatedData))
        }
    }

    /** Copies [plaintext] into a versioned, zero-filled bucket. */
    internal fun encode(plaintext: ByteArray, maxPlaintextBytes: Int): ByteArray {
        require(plaintext.size <= maxPlaintextBytes) { "Plaintext exceeds the supported limit" }
        val encoded = ByteArray(bucketSize(plaintext.size.toLong() + HEADER_BYTES))
        MAGIC.copyInto(encoded)
        encoded.writeInt(HEADER_BYTES - Int.SIZE_BYTES, plaintext.size)
        plaintext.copyInto(encoded, destinationOffset = HEADER_BYTES)
        return encoded
    }

    /** Extracts and validates the original bytes from authenticated padded plaintext. */
    internal fun decode(encoded: ByteArray, maxPlaintextBytes: Int): ByteArray {
        require(maxPlaintextBytes >= 0) { "Maximum plaintext size must not be negative" }
        require(encoded.startsWithMagic()) { "Unsupported padded plaintext version" }
        require(encoded.size >= MIN_BUCKET_BYTES && encoded.size.isPowerOfTwo()) {
            "Padded payload has a non-canonical bucket size"
        }
        require(encoded.size <= maximumEncodedSize(maxPlaintextBytes)) {
            "Padded payload exceeds the supported limit"
        }
        val plaintextSize = encoded.readInt(HEADER_BYTES - Int.SIZE_BYTES)
        require(plaintextSize in 0..maxPlaintextBytes && plaintextSize <= encoded.size - HEADER_BYTES) {
            "Padded payload length is invalid"
        }
        require(encoded.size == bucketSize(plaintextSize.toLong() + HEADER_BYTES)) {
            "Padded payload is not in its canonical bucket"
        }
        require((HEADER_BYTES + plaintextSize until encoded.size).all { encoded[it] == 0.toByte() }) {
            "Padded payload contains invalid padding"
        }
        return encoded.copyOfRange(HEADER_BYTES, HEADER_BYTES + plaintextSize)
    }

    private fun versionedAssociatedData(associatedData: ByteArray?): ByteArray =
        AAD_DOMAIN + (associatedData ?: ByteArray(0))

    private fun bucketSize(requiredBytes: Long): Int {
        require(requiredBytes in 0..Int.MAX_VALUE.toLong()) { "Padded payload is too large" }
        var bucket = MIN_BUCKET_BYTES.toLong()
        while (bucket < requiredBytes) {
            require(bucket <= Int.MAX_VALUE.toLong() / 2) { "Padded payload is too large" }
            bucket *= 2
        }
        return bucket.toInt()
    }

    private fun ByteArray.startsWithMagic(): Boolean =
        size >= MAGIC.size && MAGIC.indices.all { index -> this[index] == MAGIC[index] }

    private fun Int.isPowerOfTwo(): Boolean = this > 0 && this and (this - 1) == 0

    private fun ByteArray.writeInt(offset: Int, value: Int) {
        this[offset] = (value ushr 24).toByte()
        this[offset + 1] = (value ushr 16).toByte()
        this[offset + 2] = (value ushr 8).toByte()
        this[offset + 3] = value.toByte()
    }

    private fun ByteArray.readInt(offset: Int): Int =
        ((this[offset].toInt() and 0xff) shl 24) or
            ((this[offset + 1].toInt() and 0xff) shl 16) or
            ((this[offset + 2].toInt() and 0xff) shl 8) or
            (this[offset + 3].toInt() and 0xff)
}
