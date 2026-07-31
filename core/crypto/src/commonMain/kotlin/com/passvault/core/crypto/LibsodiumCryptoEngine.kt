@file:OptIn(ExperimentalUnsignedTypes::class)

package com.passvault.core.crypto

import com.ionspin.kotlin.crypto.LibsodiumInitializer
import com.ionspin.kotlin.crypto.generichash.GenericHash
import com.ionspin.kotlin.crypto.generichash.crypto_generichash_blake2b_BYTES_MAX
import com.ionspin.kotlin.crypto.generichash.crypto_generichash_blake2b_BYTES_MIN
import com.ionspin.kotlin.crypto.generichash.crypto_generichash_blake2b_KEYBYTES_MAX
import com.ionspin.kotlin.crypto.generichash.crypto_generichash_blake2b_KEYBYTES_MIN
import com.ionspin.kotlin.crypto.pwhash.PasswordHash
import com.ionspin.kotlin.crypto.pwhash.crypto_pwhash_OPSLIMIT_INTERACTIVE
import com.ionspin.kotlin.crypto.pwhash.crypto_pwhash_MEMLIMIT_INTERACTIVE
import com.ionspin.kotlin.crypto.pwhash.crypto_pwhash_ALG_DEFAULT
import com.ionspin.kotlin.crypto.aead.AuthenticatedEncryptionWithAssociatedData
import com.ionspin.kotlin.crypto.aead.crypto_aead_xchacha20poly1305_ietf_ABYTES
import com.ionspin.kotlin.crypto.aead.crypto_aead_xchacha20poly1305_ietf_NPUBBYTES
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
    ): Result<DerivedKey> = withContext(Dispatchers.Default) {
        safeResult {
            ensureInitialized()
            require(password.isNotEmpty()) { "Password must not be empty" }
            require(salt.size >= ARGON2_SALT_BYTES) {
                "Argon2 salt must be at least $ARGON2_SALT_BYTES bytes"
            }
            require(opsLimit > 0) { "Argon2 operations limit must be positive" }
            require(memLimit >= ARGON2_MIN_MEMORY_BYTES) {
                "Argon2 memory limit is too small"
            }
            val nativeSalt = salt.copyOf(ARGON2_SALT_BYTES)
            try {
                val passwordHex = password.toHexString()
                val derived = PasswordHash.pwhash(
                    outputLength = 32,
                    password = passwordHex,
                    salt = nativeSalt.asUByteArray(),
                    opsLimit = opsLimit.toULong(),
                    memLimit = memLimit,
                    algorithm = crypto_pwhash_ALG_DEFAULT,
                )
                DerivedKey(
                    key = derived.asByteArray(),
                    // Keep an owned copy. Callers may wipe their input salt
                    // after derivation without invalidating the returned value.
                    salt = salt.copyOf(),
                    opsLimit = opsLimit,
                    memLimit = memLimit,
                )
            } finally {
                nativeSalt.fill(0)
            }
        }
    }
    
    override suspend fun generateRandom(size: Int): Result<ByteArray> = withContext(Dispatchers.Default) {
        safeResult {
            ensureInitialized()
            require(size >= 0) { "Random byte count must not be negative" }
            LibsodiumRandom.buf(size).asByteArray()
        }
    }
    
    override suspend fun generateMasterKey(): Result<ByteArray> = generateRandom(32)
    
    override suspend fun encrypt(
        plaintext: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<EncryptedData> = withContext(Dispatchers.Default) {
        safeResult {
            ensureInitialized()
            require(key.size == KEY_BYTES) { "XChaCha20-Poly1305 keys must be 32 bytes" }
            val nonce = LibsodiumRandom.buf(crypto_aead_xchacha20poly1305_ietf_NPUBBYTES)
            val ciphertextAndTag =
                AuthenticatedEncryptionWithAssociatedData.xChaCha20Poly1305IetfEncrypt(
                    message = plaintext.asUByteArray(),
                    associatedData = (associatedData ?: ByteArray(0)).asUByteArray(),
                    nonce = nonce,
                    key = key.asUByteArray(),
                )
            val combined = AEAD_ENVELOPE_MAGIC + ciphertextAndTag.asByteArray()
            
            EncryptedData(
                ciphertext = combined,
                nonce = nonce.asByteArray(),
                tag = ciphertextAndTag
                    .takeLast(crypto_aead_xchacha20poly1305_ietf_ABYTES)
                    .toUByteArray()
                    .asByteArray(),
            )
        }
    }
    
    override suspend fun decrypt(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<ByteArray> = withContext(Dispatchers.Default) {
        safeResult {
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
            AuthenticatedEncryptionWithAssociatedData.xChaCha20Poly1305IetfDecrypt(
                ciphertextAndTag = ciphertext
                    .copyOfRange(AEAD_ENVELOPE_MAGIC.size, ciphertext.size)
                    .asUByteArray(),
                associatedData = (associatedData ?: ByteArray(0)).asUByteArray(),
                nonce = nonce.asUByteArray(),
                key = key.asUByteArray(),
            ).asByteArray()
        }
    }
    
    override suspend fun deriveSubkey(
        masterKey: ByteArray,
        context: String,
        size: Int,
    ): Result<ByteArray> = withContext(Dispatchers.Default) {
        safeResult {
            ensureInitialized()
            require(masterKey.size == KEY_BYTES) { "Master keys must be 32 bytes" }
            require(size in crypto_generichash_blake2b_BYTES_MIN..crypto_generichash_blake2b_BYTES_MAX) {
                "BLAKE2b subkeys must be between $crypto_generichash_blake2b_BYTES_MIN and " +
                    "$crypto_generichash_blake2b_BYTES_MAX bytes"
            }
            val contextBytes = context.encodeToByteArray()
            require(contextBytes.isNotEmpty()) { "Subkey context must not be empty" }
            val message = SUBKEY_DOMAIN +
                contextBytes.size.toString().encodeToByteArray() +
                byteArrayOf(0) +
                contextBytes
            GenericHash.genericHash(
                message = message.asUByteArray(),
                requestedHashLength = size,
                key = masterKey.asUByteArray(),
            ).asByteArray()
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
    
    override suspend fun secureWipe(data: ByteArray) {
        data.fill(0)
    }
    
    override suspend fun benchmarkArgon2(): Argon2Parameters = withContext(Dispatchers.Default) {
        safeResult {
            ensureInitialized()
            val testPassword = "passvault-benchmark"
            val testSalt = LibsodiumRandom.buf(16).asByteArray()
            try {
                val start = TimeSource.Monotonic.markNow()
                PasswordHash.pwhash(
                    outputLength = 32,
                    password = testPassword,
                    salt = testSalt.asUByteArray(),
                    opsLimit = crypto_pwhash_OPSLIMIT_INTERACTIVE.toULong(),
                    memLimit = crypto_pwhash_MEMLIMIT_INTERACTIVE,
                    algorithm = crypto_pwhash_ALG_DEFAULT,
                )
                val duration = start.elapsedNow().inWholeMilliseconds
                when {
                    duration < 50 -> Argon2Parameters.INTERACTIVE
                    duration < 150 -> Argon2Parameters.MODERATE
                    else -> Argon2Parameters.INTERACTIVE
                }
            } finally {
                testSalt.fill(0)
            }
        }.getOrElse { error ->
            if (error is CancellationException) throw error
            Argon2Parameters.INTERACTIVE
        }
    }

    private suspend inline fun <T> safeResult(crossinline block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
}

class CryptoException(message: String) : Exception(message)

private const val KEY_BYTES = 32
private const val ARGON2_SALT_BYTES = 16
private const val ARGON2_MIN_MEMORY_BYTES = 8 * 1024
private val SUBKEY_DOMAIN = "passvault-subkey-v1\u0000".encodeToByteArray()
internal val AEAD_ENVELOPE_MAGIC = byteArrayOf(0x50, 0x56, 0x02, 0x00)

private fun ByteArray.hasAeadEnvelopeMagic(): Boolean =
    size >= AEAD_ENVELOPE_MAGIC.size &&
        copyOfRange(0, AEAD_ENVELOPE_MAGIC.size).contentEquals(AEAD_ENVELOPE_MAGIC)

private fun ByteArray.toHexString(): String =
    joinToString(separator = "") { byte -> byte.toUByte().toString(16).padStart(2, '0') }
