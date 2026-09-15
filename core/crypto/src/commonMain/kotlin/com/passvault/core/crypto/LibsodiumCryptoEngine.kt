@file:OptIn(ExperimentalUnsignedTypes::class)

package com.passvault.core.crypto

import com.ionspin.kotlin.crypto.LibsodiumInitializer
import com.ionspin.kotlin.crypto.aead.AeadCorrupedOrTamperedDataException
import com.ionspin.kotlin.crypto.aead.AuthenticatedEncryptionWithAssociatedData
import com.ionspin.kotlin.crypto.aead.crypto_aead_xchacha20poly1305_ietf_ABYTES
import com.ionspin.kotlin.crypto.aead.crypto_aead_xchacha20poly1305_ietf_NPUBBYTES
import com.ionspin.kotlin.crypto.generichash.GenericHash
import com.ionspin.kotlin.crypto.generichash.crypto_generichash_blake2b_BYTES_MAX
import com.ionspin.kotlin.crypto.generichash.crypto_generichash_blake2b_BYTES_MIN
import com.ionspin.kotlin.crypto.pwhash.crypto_pwhash_ALG_DEFAULT
import com.ionspin.kotlin.crypto.pwhash.crypto_pwhash_MEMLIMIT_INTERACTIVE
import com.ionspin.kotlin.crypto.pwhash.crypto_pwhash_OPSLIMIT_INTERACTIVE
import com.ionspin.kotlin.crypto.util.LibsodiumRandom
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.TimeSource

class LibsodiumCryptoEngine : CryptoEngine {
    private val initializationMutex = Mutex()
    private var initialized = false

    private suspend fun ensureInitialized() {
        initializationMutex.withLock {
            if (!initialized) {
                LibsodiumInitializer.initialize()
                initialized = true
            }
        }
    }

    override suspend fun deriveKey(
        password: ByteArray,
        salt: ByteArray,
        opsLimit: Int,
        memLimit: Int,
    ): Result<DerivedKey> = cryptoOperation(DerivedKey::clear) {
        ensureInitialized()
        require(password.isNotEmpty()) { "Password must not be empty" }
        require(salt.size == ARGON2_SALT_BYTES) {
            "Argon2 salt must be exactly $ARGON2_SALT_BYTES bytes"
        }
        require(opsLimit in ARGON2_MIN_OPS..ARGON2_MAX_OPS) {
            "Argon2 operations limit is outside the supported range"
        }
        require(memLimit in ARGON2_MIN_MEMORY_BYTES..ARGON2_MAX_MEMORY_BYTES) {
            "Argon2 memory limit is outside the supported range"
        }
        val nativeSalt = salt.copyOf()
        var ownedKey: ByteArray? = null
        var ownedSalt: ByteArray? = null
        var ownershipTransferred = false
        try {
            val nativeDerivedBytes = password.withLowercaseHexBytes { encodedPassword ->
                rawPasswordHash(
                    outputLength = 32,
                    password = encodedPassword,
                    salt = nativeSalt,
                    opsLimit = opsLimit,
                    memLimit = memLimit,
                    algorithm = crypto_pwhash_ALG_DEFAULT,
                )
            }
            val keyCopy = try {
                nativeDerivedBytes.copyOf()
            } finally {
                nativeDerivedBytes.fill(0)
            }
            ownedKey = keyCopy
            val saltCopy = salt.copyOf()
            ownedSalt = saltCopy
            DerivedKey(
                key = keyCopy,
                salt = saltCopy,
                opsLimit = opsLimit,
                memLimit = memLimit,
            ).also { ownershipTransferred = true }
        } finally {
            nativeSalt.fill(0)
            if (!ownershipTransferred) {
                ownedKey?.fill(0)
                ownedSalt?.fill(0)
            }
        }
    }

    override suspend fun generateRandom(size: Int): Result<ByteArray> = cryptoOperation({ it.fill(0) }) {
        ensureInitialized()
        require(size >= 0) { "Random byte count must not be negative" }
        val nativeRandom = LibsodiumRandom.buf(size).asByteArray()
        try {
            nativeRandom.copyOf()
        } finally {
            nativeRandom.fill(0)
        }
    }

    override suspend fun generateMasterKey(): Result<ByteArray> = generateRandom(32)

    override suspend fun encrypt(
        plaintext: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<EncryptedData> = cryptoOperation(EncryptedData::clear) {
        ensureInitialized()
        require(key.size == KEY_BYTES) { "XChaCha20-Poly1305 keys must be 32 bytes" }
        val nonce = LibsodiumRandom.buf(crypto_aead_xchacha20poly1305_ietf_NPUBBYTES)
        try {
            val ciphertextAndTag =
                AuthenticatedEncryptionWithAssociatedData.xChaCha20Poly1305IetfEncrypt(
                    message = plaintext.asUByteArray(),
                    associatedData = (associatedData ?: ByteArray(0)).asUByteArray(),
                    nonce = nonce,
                    key = key.asUByteArray(),
                )
            val ciphertextBytes = ciphertextAndTag.asByteArray()
            var ownedCiphertext: ByteArray? = null
            var ownedNonce: ByteArray? = null
            var ownedTag: ByteArray? = null
            var ownershipTransferred = false
            try {
                val ciphertextCopy = AEAD_ENVELOPE_MAGIC + ciphertextBytes
                ownedCiphertext = ciphertextCopy
                val nonceCopy = nonce.asByteArray().copyOf()
                ownedNonce = nonceCopy
                val tagCopy = ciphertextBytes.copyOfRange(
                    ciphertextBytes.size - crypto_aead_xchacha20poly1305_ietf_ABYTES,
                    ciphertextBytes.size,
                )
                ownedTag = tagCopy
                EncryptedData(
                    ciphertext = ciphertextCopy,
                    nonce = nonceCopy,
                    tag = tagCopy,
                ).also { ownershipTransferred = true }
            } finally {
                ciphertextBytes.fill(0)
                if (!ownershipTransferred) {
                    ownedCiphertext?.fill(0)
                    ownedNonce?.fill(0)
                    ownedTag?.fill(0)
                }
            }
        } finally {
            nonce.asByteArray().fill(0)
        }
    }

    override suspend fun decrypt(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<ByteArray> = cryptoOperation({ it.fill(0) }) {
        ensureInitialized()
        require(key.size == KEY_BYTES) { "XChaCha20-Poly1305 keys must be 32 bytes" }
        require(nonce.size == crypto_aead_xchacha20poly1305_ietf_NPUBBYTES) {
            "XChaCha20-Poly1305 nonces must be $crypto_aead_xchacha20poly1305_ietf_NPUBBYTES bytes"
        }
        require(ciphertext.hasAeadEnvelopeMagic()) {
            "Unsupported encrypted payload version"
        }
        require(ciphertext.size >= AEAD_ENVELOPE_MAGIC.size + crypto_aead_xchacha20poly1305_ietf_ABYTES) {
            "Encrypted payload is truncated"
        }
        val nativeCiphertext = ciphertext.copyOfRange(AEAD_ENVELOPE_MAGIC.size, ciphertext.size)
        try {
            val nativePlaintext = try {
                AuthenticatedEncryptionWithAssociatedData.xChaCha20Poly1305IetfDecrypt(
                    ciphertextAndTag = nativeCiphertext.asUByteArray(),
                    associatedData = (associatedData ?: ByteArray(0)).asUByteArray(),
                    nonce = nonce.asUByteArray(),
                    key = key.asUByteArray(),
                ).asByteArray()
            } catch (_: AeadCorrupedOrTamperedDataException) {
                throw CiphertextAuthenticationException()
            }
            try {
                nativePlaintext.copyOf()
            } finally {
                nativePlaintext.fill(0)
            }
        } finally {
            nativeCiphertext.fill(0)
        }
    }

    override suspend fun deriveSubkey(
        masterKey: ByteArray,
        context: String,
        size: Int,
    ): Result<ByteArray> = cryptoOperation({ it.fill(0) }) {
        ensureInitialized()
        require(masterKey.size == KEY_BYTES) { "Master keys must be 32 bytes" }
        require(size in crypto_generichash_blake2b_BYTES_MIN..crypto_generichash_blake2b_BYTES_MAX) {
            "BLAKE2b subkeys must be between $crypto_generichash_blake2b_BYTES_MIN and " +
                "$crypto_generichash_blake2b_BYTES_MAX bytes"
        }
        require(context.length <= MAX_SUBKEY_CONTEXT_CODE_UNITS) { "Subkey context is too long" }
        val contextBytes = context.encodeToByteArray(throwOnInvalidSequence = true)
        var lengthBytes: ByteArray? = null
        var message: ByteArray? = null
        try {
            require(contextBytes.isNotEmpty() && contextBytes.size <= MAX_SUBKEY_CONTEXT_BYTES) {
                "Subkey context must have a supported UTF-8 length"
            }
            val encodedLength = contextBytes.size.toString().encodeToByteArray()
            lengthBytes = encodedLength
            val hashMessage = ByteArray(SUBKEY_DOMAIN.size + encodedLength.size + 1 + contextBytes.size)
            message = hashMessage
            var offset = 0
            SUBKEY_DOMAIN.copyInto(hashMessage, destinationOffset = offset)
            offset += SUBKEY_DOMAIN.size
            encodedLength.copyInto(hashMessage, destinationOffset = offset)
            offset += encodedLength.size
            hashMessage[offset] = 0
            contextBytes.copyInto(hashMessage, destinationOffset = offset + 1)
            val nativeHash = GenericHash.genericHash(
                message = hashMessage.asUByteArray(),
                requestedHashLength = size,
                key = masterKey.asUByteArray(),
            ).asByteArray()
            try {
                nativeHash.copyOf()
            } finally {
                nativeHash.fill(0)
            }
        } finally {
            contextBytes.fill(0)
            lengthBytes?.fill(0)
            message?.fill(0)
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

    override suspend fun benchmarkArgon2(): Argon2Parameters = cryptoOperation({}) {
        ensureInitialized()
        val testPassword = "passvault-benchmark".encodeToByteArray()
        val testSalt = LibsodiumRandom.buf(16).asByteArray()
        var benchmarkOutput: ByteArray? = null
        try {
            val start = TimeSource.Monotonic.markNow()
            benchmarkOutput = rawPasswordHash(
                outputLength = 32,
                password = testPassword,
                salt = testSalt,
                opsLimit = crypto_pwhash_OPSLIMIT_INTERACTIVE,
                memLimit = crypto_pwhash_MEMLIMIT_INTERACTIVE,
                algorithm = crypto_pwhash_ALG_DEFAULT,
            )
            val duration = start.elapsedNow().inWholeMilliseconds
            selectArgon2Parameters(duration)
        } finally {
            benchmarkOutput?.fill(0)
            testPassword.fill(0)
            testSalt.fill(0)
        }
    }.getOrElse { error ->
        if (error is CancellationException) throw error
        Argon2Parameters.INTERACTIVE
    }

    /**
     * A dispatched coroutine can be cancelled after [block] has produced an
     * owned secret but before `withContext` delivers it to the caller. Track
     * that result in the worker context so the cancellation path can clear it.
     */
    @Suppress("TooGenericExceptionCaught")
    private suspend inline fun <T> cryptoOperation(
        crossinline clearOnCancellation: (T) -> Unit,
        crossinline block: suspend () -> T,
    ): Result<T> {
        var completedResult: Result<T>? = null
        return try {
            withContext(Dispatchers.Default) {
                val result = try {
                    Result.success(block())
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Result.failure(error)
                }
                completedResult = result
                result
            }
        } catch (cancelled: CancellationException) {
            completedResult?.getOrNull()?.let(clearOnCancellation)
            throw cancelled
        }
    }
}

/**
 * Selects one of the two shipped 64 MiB profiles. Fast devices receive the
 * higher operation count; slower benchmark results never select a stronger profile.
 */
internal fun selectArgon2Parameters(durationMilliseconds: Long): Argon2Parameters =
    if (durationMilliseconds < FAST_ARGON2_BENCHMARK_MILLISECONDS) {
        Argon2Parameters.INTERACTIVE.copy(opsLimit = FAST_DEVICE_OPS_LIMIT)
    } else {
        Argon2Parameters.INTERACTIVE
    }

private const val KEY_BYTES = 32
private const val ARGON2_SALT_BYTES = 16
private const val ARGON2_MIN_OPS = 1
private const val ARGON2_MAX_OPS = 10
private const val ARGON2_MIN_MEMORY_BYTES = 8 * 1024
private const val ARGON2_MAX_MEMORY_BYTES = 1024 * 1024 * 1024
private const val FAST_ARGON2_BENCHMARK_MILLISECONDS = 50L
private const val FAST_DEVICE_OPS_LIMIT = 4
private const val MAX_SUBKEY_CONTEXT_CODE_UNITS = 16 * 1024
private const val MAX_SUBKEY_CONTEXT_BYTES = 64 * 1024
private val SUBKEY_DOMAIN = "passvault-subkey-v1\u0000".encodeToByteArray()
private val AEAD_ENVELOPE_MAGIC = byteArrayOf(0x50, 0x56, 0x02, 0x00)

private fun ByteArray.hasAeadEnvelopeMagic(): Boolean =
    size >= AEAD_ENVELOPE_MAGIC.size &&
        AEAD_ENVELOPE_MAGIC.indices.all { index -> this[index] == AEAD_ENVELOPE_MAGIC[index] }
