package com.passvault.core.testing.fakes

import com.passvault.core.crypto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fake crypto engine for testing.
 * Provides deterministic, fast cryptographic operations without actual security.
 * DO NOT USE IN PRODUCTION.
 */
open class FakeCryptoEngine : CryptoEngine {
    
    private val randomBytes = mutableMapOf<Int, ByteArray>()
    private var nextRandomIndex = 0
    private var generatedRandomCounter = 0
    private var shouldFailNext = false
    private var failWith: Throwable? = null
    private val encryptionRecords = mutableListOf<EncryptionRecord>()

    private data class EncryptionRecord(
        val plaintext: ByteArray,
        val ciphertext: ByteArray,
        val nonce: ByteArray,
        val key: ByteArray,
        val associatedData: ByteArray?,
    )
    
    /**
     * Pre-configure random bytes for deterministic testing.
     */
    fun setNextRandomBytes(bytes: ByteArray) {
        randomBytes[nextRandomIndex++] = bytes
    }
    
    /**
     * Configure the next operation to fail.
     */
    fun setShouldFail(error: Throwable? = RuntimeException("Fake crypto error")) {
        shouldFailNext = true
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
        if (shouldFailNext) {
            shouldFailNext = false
            return Result.failure(failWith ?: RuntimeException("Key derivation failed"))
        }
        
        // Deterministic "derivation" for testing
        val derived = deriveBytes(password, salt, size = 32)
        
        return Result.success(
            DerivedKey(
                key = derived,
                salt = salt,
                opsLimit = opsLimit,
                memLimit = memLimit,
            )
        )
    }
    
    override suspend fun generateRandom(size: Int): Result<ByteArray> {
        if (shouldFailNext) {
            shouldFailNext = false
            return Result.failure(failWith ?: RuntimeException("Random generation failed"))
        }
        
        val configuredKey = randomBytes.keys.minOrNull()
        val configuredBytes = configuredKey?.let(randomBytes::remove)
        val generated = ByteArray(size) { index ->
            (index + generatedRandomCounter).toByte()
        }
        generatedRandomCounter++
        return Result.success(configuredBytes ?: generated)
    }
    
    override suspend fun generateMasterKey(): Result<ByteArray> {
        return generateRandom(32)
    }
    
    override suspend fun encrypt(
        plaintext: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<EncryptedData> {
        if (shouldFailNext) {
            shouldFailNext = false
            return Result.failure(failWith ?: RuntimeException("Encryption failed"))
        }

        if (key.isEmpty()) {
            return Result.failure(IllegalArgumentException("Encryption key must not be empty"))
        }
        
        val nonce = generateRandom(24).getOrThrow()
        val ciphertext = xorWithKeyAndNonce(plaintext, key, nonce)
        encryptionRecords += EncryptionRecord(
            plaintext = plaintext.copyOf(),
            ciphertext = ciphertext.copyOf(),
            nonce = nonce.copyOf(),
            key = key.copyOf(),
            associatedData = associatedData?.copyOf(),
        )
        
        return Result.success(
            EncryptedData(
                ciphertext = ciphertext,
                nonce = nonce,
                tag = ByteArray(16) { (it + 100).toByte() }, // Fake tag
            )
        )
    }
    
    override suspend fun decrypt(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<ByteArray> {
        if (shouldFailNext) {
            shouldFailNext = false
            return Result.failure(failWith ?: RuntimeException("Decryption failed"))
        }
        
        val record = encryptionRecords.lastOrNull { candidate ->
            candidate.ciphertext.contentEquals(ciphertext) &&
                candidate.nonce.contentEquals(nonce) &&
                candidate.key.contentEquals(key) &&
                nullableContentEquals(candidate.associatedData, associatedData)
        } ?: return Result.failure(IllegalArgumentException("Authentication failed"))

        return Result.success(record.plaintext.copyOf())
    }
    
    override suspend fun deriveSubkey(
        masterKey: ByteArray,
        context: String,
        size: Int,
    ): Result<ByteArray> {
        if (shouldFailNext) {
            shouldFailNext = false
            return Result.failure(failWith ?: RuntimeException("Subkey derivation failed"))
        }
        
        // Deterministic subkey derivation
        val subkey = deriveBytes(masterKey, context.encodeToByteArray(), size = size)
        
        return Result.success(subkey)
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
        // Return fast parameters for testing
        Argon2Parameters.INTERACTIVE
    }
    
    /**
     * Simple XOR for fake encryption (deterministic but NOT SECURE).
     */
    private fun xorWithKey(data: ByteArray, key: ByteArray): ByteArray {
        return ByteArray(data.size) { i ->
            (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
    }

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

    private fun nullableContentEquals(
        first: ByteArray?,
        second: ByteArray?,
    ): Boolean =
        when {
            first == null -> second == null
            second == null -> false
            else -> first.contentEquals(second)
        }
}

/**
 * Fake crypto engine with predefined test vectors for verification.
 */
class FakeCryptoEngineWithVectors : FakeCryptoEngine() {
    
    private val testVectors = mutableMapOf<String, TestVector>()
    
    data class TestVector(
        val plaintext: ByteArray,
        val key: ByteArray,
        val expectedCiphertext: ByteArray,
        val nonce: ByteArray,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is TestVector) return false
            return plaintext.contentEquals(other.plaintext) &&
                   key.contentEquals(other.key) &&
                   expectedCiphertext.contentEquals(other.expectedCiphertext) &&
                   nonce.contentEquals(other.nonce)
        }
        
        override fun hashCode(): Int {
            var result = plaintext.contentHashCode()
            result = 31 * result + key.contentHashCode()
            result = 31 * result + expectedCiphertext.contentHashCode()
            result = 31 * result + nonce.contentHashCode()
            return result
        }
    }
    
    /**
     * Add a test vector for verification.
     */
    fun addTestVector(name: String, vector: TestVector) {
        testVectors[name] = vector
    }
    
    /**
     * Verify encryption produces expected ciphertext.
     */
    suspend fun verifyEncryption(vectorName: String): Boolean {
        val vector = testVectors[vectorName] ?: return false
        val result = encrypt(vector.plaintext, vector.key)
        return result.isSuccess && result.getOrThrow().ciphertext.contentEquals(vector.expectedCiphertext)
    }
    
    /**
     * Verify decryption produces expected plaintext.
     */
    suspend fun verifyDecryption(vectorName: String): Boolean {
        val vector = testVectors[vectorName] ?: return false
        val result = decrypt(vector.expectedCiphertext, vector.nonce, vector.key)
        return result.isSuccess && result.getOrThrow().contentEquals(vector.plaintext)
    }
    
    /**
     * Run all test vectors.
     */
    suspend fun runAllTestVectors(): Map<String, Boolean> {
        return testVectors.mapValues { (name, _) ->
            verifyEncryption(name) && verifyDecryption(name)
        }
    }
}
