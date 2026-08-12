package com.passvault.core.testing.fakes

import com.passvault.core.crypto.Argon2Parameters
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.DerivedKey
import com.passvault.core.crypto.EncryptedData
import kotlinx.coroutines.CancellationException
import kotlin.text.CharacterCodingException

/**
 * Fake crypto engine for testing.
 * Provides deterministic, fast cryptographic operations without actual security.
 * DO NOT USE IN PRODUCTION.
 *
 * CryptoEngine overrides and deterministic failure controls form one cohesive
 * test double and share mutable failure state.
 */
@Suppress("TooManyFunctions")
open class FakeCryptoEngine : CryptoEngine {

    private val randomBytes = mutableMapOf<Int, ByteArray>()
    private var nextRandomIndex = 0
    private var generatedRandomCounter = 0
    private var shouldFailNext = false
    private var failWith: Throwable? = null

    /**
     * Pre-configure random bytes for deterministic testing.
     */
    fun setNextRandomBytes(bytes: ByteArray) {
        randomBytes[nextRandomIndex++] = bytes.copyOf()
    }

    /**
     * Configure the next operation to fail.
     */
    fun setShouldFail(error: Throwable? = RuntimeException("Fake crypto error")) {
        shouldFailNext = error != null
        failWith = error
    }

    /**
     * Reset failure state.
     */
    fun resetFailures() {
        shouldFailNext = false
        failWith = null
    }

    /**
     * Clear all pre-configured random bytes.
     */
    fun clearRandomBytes() {
        randomBytes.values.forEach { it.fill(0) }
        randomBytes.clear()
        nextRandomIndex = 0
        generatedRandomCounter = 0
    }

    override suspend fun deriveKey(
        password: ByteArray,
        salt: ByteArray,
        opsLimit: Int,
        memLimit: Int,
    ): Result<DerivedKey> {
        val failure = consumeConfiguredFailure("Key derivation failed") ?: when {
            password.isEmpty() -> IllegalArgumentException("Password must not be empty")
            salt.size != ARGON2_SALT_BYTES ->
                IllegalArgumentException("Argon2 salt must be exactly $ARGON2_SALT_BYTES bytes")
            opsLimit !in ARGON2_MIN_OPS..ARGON2_MAX_OPS ->
                IllegalArgumentException("Argon2 operations limit is outside the supported range")
            memLimit !in ARGON2_MIN_MEMORY_BYTES..ARGON2_MAX_MEMORY_BYTES ->
                IllegalArgumentException("Argon2 memory limit is outside the supported range")
            else -> null
        }

        return if (failure != null) {
            Result.failure(failure)
        } else {
            val opsBytes = opsLimit.toBytes()
            val memoryBytes = memLimit.toBytes()
            try {
                Result.success(
                    DerivedKey(
                        key = deriveBytes(password, salt, opsBytes, memoryBytes, size = 32),
                        salt = salt.copyOf(),
                        opsLimit = opsLimit,
                        memLimit = memLimit,
                    ),
                )
            } finally {
                opsBytes.fill(0)
                memoryBytes.fill(0)
            }
        }
    }

    override suspend fun generateRandom(size: Int): Result<ByteArray> {
        val failure = consumeConfiguredFailure("Random generation failed")
            ?: if (size < 0) IllegalArgumentException("Random byte count must not be negative") else null

        return if (failure != null) {
            Result.failure(failure)
        } else {
            val configuredKey = randomBytes.keys.minOrNull()
            val configuredBytes = configuredKey?.let(randomBytes::remove)
            if (configuredBytes != null && configuredBytes.size != size) {
                configuredBytes.fill(0)
                Result.failure(
                    IllegalArgumentException("Configured random value must contain exactly $size bytes"),
                )
            } else {
                val value = configuredBytes ?: ByteArray(size) { index ->
                    (index + generatedRandomCounter).toByte()
                }.also {
                    generatedRandomCounter++
                }
                Result.success(value)
            }
        }
    }

    override suspend fun generateMasterKey(): Result<ByteArray> {
        return generateRandom(32)
    }

    override suspend fun encrypt(
        plaintext: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<EncryptedData> {
        val failure = consumeConfiguredFailure("Encryption failed")
            ?: if (key.size != KEY_BYTES) {
                IllegalArgumentException("Encryption keys must be exactly $KEY_BYTES bytes")
            } else {
                null
            }
        return if (failure != null) {
            Result.failure(failure)
        } else {
            generateRandom(NONCE_BYTES).map { nonce ->
                val encryptedBody = xorWithKeyAndNonce(plaintext, key, nonce)
                val tag = fakeAuthenticationTag(plaintext, key, nonce, associatedData)
                try {
                    EncryptedData(
                        ciphertext = ENVELOPE_MAGIC + encryptedBody + tag,
                        nonce = nonce,
                        tag = tag.copyOf(),
                    )
                } finally {
                    encryptedBody.fill(0)
                    tag.fill(0)
                }
            }
        }
    }

    override suspend fun decrypt(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<ByteArray> {
        val failure = consumeConfiguredFailure("Decryption failed") ?: when {
            key.size != KEY_BYTES || nonce.size != NONCE_BYTES -> {
                IllegalArgumentException("Invalid encryption parameters")
            }
            ciphertext.size < ENVELOPE_MAGIC.size + AUTHENTICATION_TAG_BYTES ||
                !ciphertext.copyOfRange(0, ENVELOPE_MAGIC.size).contentEquals(ENVELOPE_MAGIC) -> {
                IllegalArgumentException("Authentication failed")
            }
            else -> null
        }
        return if (failure != null) {
            Result.failure(failure)
        } else {
            decryptAuthenticatedPayload(ciphertext, nonce, key, associatedData)
        }
    }

    private suspend fun decryptAuthenticatedPayload(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<ByteArray> {
        val bodyStart = ENVELOPE_MAGIC.size
        val tagStart = ciphertext.size - AUTHENTICATION_TAG_BYTES
        val encryptedBody = ciphertext.copyOfRange(bodyStart, tagStart)
        val suppliedTag = ciphertext.copyOfRange(tagStart, ciphertext.size)
        val plaintext = xorWithKeyAndNonce(encryptedBody, key, nonce)
        val expectedTag = fakeAuthenticationTag(plaintext, key, nonce, associatedData)
        return try {
            if (constantTimeEquals(suppliedTag, expectedTag)) {
                Result.success(plaintext)
            } else {
                plaintext.fill(0)
                Result.failure(IllegalArgumentException("Authentication failed"))
            }
        } finally {
            encryptedBody.fill(0)
            suppliedTag.fill(0)
            expectedTag.fill(0)
        }
    }

    override suspend fun deriveSubkey(
        masterKey: ByteArray,
        context: String,
        size: Int,
    ): Result<ByteArray> {
        val failure = consumeConfiguredFailure("Subkey derivation failed") ?: when {
            masterKey.size != KEY_BYTES ->
                IllegalArgumentException("Master keys must be exactly $KEY_BYTES bytes")
            context.isEmpty() -> IllegalArgumentException("Subkey context must not be empty")
            context.length > MAX_SUBKEY_CONTEXT_CODE_UNITS ->
                IllegalArgumentException("Subkey context is too long")
            size !in MIN_SUBKEY_BYTES..MAX_SUBKEY_BYTES -> IllegalArgumentException(
                "Subkeys must be between $MIN_SUBKEY_BYTES and $MAX_SUBKEY_BYTES bytes",
            )
            else -> null
        }

        return if (failure != null) {
            Result.failure(failure)
        } else {
            val encodedContext: Result<ByteArray> = try {
                Result.success(context.encodeToByteArray(throwOnInvalidSequence = true))
            } catch (error: CharacterCodingException) {
                Result.failure(error)
            }
            encodedContext.fold(
                onSuccess = { contextBytes ->
                    if (contextBytes.size > MAX_SUBKEY_CONTEXT_BYTES) {
                        contextBytes.fill(0)
                        Result.failure(IllegalArgumentException("Subkey context is too long"))
                    } else {
                        val sizeBytes = size.toBytes()
                        try {
                            Result.success(deriveBytes(masterKey, contextBytes, sizeBytes, size = size))
                        } finally {
                            contextBytes.fill(0)
                            sizeBytes.fill(0)
                        }
                    }
                },
                onFailure = { error -> Result.failure(error) },
            )
        }
    }

    override suspend fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }

    override fun secureWipe(data: ByteArray) {
        data.fill(0)
    }

    override suspend fun benchmarkArgon2(): Argon2Parameters = Argon2Parameters.INTERACTIVE

    private fun xorWithKeyAndNonce(
        data: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
    ): ByteArray =
        ByteArray(data.size) { index ->
            (
                data[index].toInt() xor
                    key[index % key.size].toInt() xor
                    nonce[index % nonce.size].toInt()
            ).toByte()
        }

    private fun deriveBytes(
        vararg inputs: ByteArray,
        size: Int,
    ): ByteArray {
        var state = 0x811C9DC5.toInt()
        inputs.forEach { input ->
            input.forEach { byte ->
                state = (state xor (byte.toInt() and 0xFF)) * 16777619
            }
        }
        return ByteArray(size) { index ->
            state = state * 1664525 + 1013904223 + index
            (state ushr 24).toByte()
        }
    }

    private fun fakeAuthenticationTag(
        plaintext: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        associatedData: ByteArray?,
    ): ByteArray = deriveBytes(
        key,
        nonce,
        associatedData ?: ByteArray(0),
        plaintext,
        size = AUTHENTICATION_TAG_BYTES,
    )

    private fun consumeConfiguredFailure(defaultMessage: String): Throwable? =
        if (shouldFailNext) {
            shouldFailNext = false
            val error = failWith ?: RuntimeException(defaultMessage)
            if (error is CancellationException) throw error
            error
        } else {
            null
        }

    private fun Int.toBytes(): ByteArray = byteArrayOf(
        (this ushr 24).toByte(),
        (this ushr 16).toByte(),
        (this ushr 8).toByte(),
        toByte(),
    )

    private companion object {
        const val KEY_BYTES = 32
        const val NONCE_BYTES = 24
        const val AUTHENTICATION_TAG_BYTES = 16
        const val ARGON2_SALT_BYTES = 16
        const val ARGON2_MIN_OPS = 1
        const val ARGON2_MAX_OPS = 10
        const val ARGON2_MIN_MEMORY_BYTES = 8 * 1024
        const val ARGON2_MAX_MEMORY_BYTES = 1024 * 1024 * 1024
        const val MIN_SUBKEY_BYTES = 16
        const val MAX_SUBKEY_BYTES = 64
        const val MAX_SUBKEY_CONTEXT_CODE_UNITS = 16 * 1024
        const val MAX_SUBKEY_CONTEXT_BYTES = 64 * 1024
        val ENVELOPE_MAGIC = byteArrayOf(0x50, 0x56, 0x02, 0x00)
    }
}
