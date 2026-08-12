package com.passvault.core.crypto

/**
 * Core cryptographic engine interface.
 * All crypto operations go through this interface.
 */
interface CryptoEngine {
    /**
     * Derive key from password using Argon2id.
     * Returns the derived key and the parameters used.
     */
    suspend fun deriveKey(
        password: ByteArray,
        salt: ByteArray,
        opsLimit: Int,
        memLimit: Int,
    ): Result<DerivedKey>

    /**
     * Generate random bytes.
     */
    suspend fun generateRandom(size: Int): Result<ByteArray>

    /**
     * Generate a cryptographically secure random master key.
     */
    suspend fun generateMasterKey(): Result<ByteArray>

    /**
     * Encrypt data with XChaCha20-Poly1305.
     */
    suspend fun encrypt(
        plaintext: ByteArray,
        key: ByteArray,
        associatedData: ByteArray? = null,
    ): Result<EncryptedData>

    /**
     * Decrypt data with XChaCha20-Poly1305.
     */
    suspend fun decrypt(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray? = null,
    ): Result<ByteArray>

    /**
     * Derive a domain-separated subkey from a master key.
     *
     * The implementation uses libsodium's keyed BLAKE2b (`crypto_generichash`)
     * rather than a password KDF.  Callers must provide a stable, purpose-specific
     * context (for example `record:<id>`).
     */
    suspend fun deriveSubkey(
        masterKey: ByteArray,
        context: String,
        size: Int = 32,
    ): Result<ByteArray>

    /**
     * Compare two byte arrays in constant time.
     */
    suspend fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean

    /**
     * Securely wipe sensitive data.
     * Note: Best effort on managed runtimes.
     */
    fun secureWipe(data: ByteArray)

    /**
     * Benchmark Argon2 parameters for this device.
     * Returns recommended parameters for interactive use.
     */
    suspend fun benchmarkArgon2(): Argon2Parameters
}

data class DerivedKey(
    val key: ByteArray,
    val salt: ByteArray,
    val opsLimit: Int,
    val memLimit: Int,
) {
    override fun equals(other: Any?): Boolean =
        other is DerivedKey &&
            key.contentEquals(other.key) &&
            salt.contentEquals(other.salt) &&
            opsLimit == other.opsLimit &&
            memLimit == other.memLimit

    override fun hashCode(): Int {
        var result = key.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + opsLimit
        return 31 * result + memLimit
    }

    fun clear() {
        key.fill(0)
        salt.fill(0)
    }
}

data class EncryptedData(
    val ciphertext: ByteArray,
    val nonce: ByteArray,
    val tag: ByteArray, // Poly1305 tag
) {
    override fun equals(other: Any?): Boolean =
        other is EncryptedData &&
            ciphertext.contentEquals(other.ciphertext) &&
            nonce.contentEquals(other.nonce) &&
            tag.contentEquals(other.tag)

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + nonce.contentHashCode()
        return 31 * result + tag.contentHashCode()
    }

    fun clear() {
        ciphertext.fill(0)
        nonce.fill(0)
        tag.fill(0)
    }
}

data class Argon2Parameters(
    val opsLimit: Int,
    val memLimit: Int, // in bytes
) {
    companion object {
        /**
         * Interactive use - moderate security, fast unlock.
         * ~100ms on modern hardware.
         */
        val INTERACTIVE = Argon2Parameters(
            opsLimit = 3,
            memLimit = 64 * 1024 * 1024, // 64 MB
        )

        /**
         * Moderate use - balanced security and performance.
         * ~500ms on modern hardware.
         */
        val MODERATE = Argon2Parameters(
            opsLimit = 3,
            memLimit = 256 * 1024 * 1024, // 256 MB
        )

        /**
         * Sensitive use - high security.
         * ~1-2s on modern hardware.
         */
        val SENSITIVE = Argon2Parameters(
            opsLimit = 4,
            memLimit = 1024 * 1024 * 1024, // 1 GB
        )

        /**
         * Minimum acceptable parameters.
         */
        val MINIMUM = Argon2Parameters(
            opsLimit = 2,
            memLimit = 32 * 1024 * 1024, // 32 MB
        )
    }
}

/**
 * Vault key hierarchy.
 */
class VaultKeyHierarchy(
    private val cryptoEngine: CryptoEngine,
) {
    /**
     * The Vault Encryption Key (VEK) - random 256-bit key.
     * Generated once at vault creation.
     */
    suspend fun generateVEK(): Result<ByteArray> {
        return cryptoEngine.generateMasterKey()
    }

    /**
     * Wrap VEK with Key Encryption Key (derived from master password).
     */
    suspend fun wrapVEK(
        vek: ByteArray,
        kek: ByteArray,
    ): Result<WrappedKey> {
        return cryptoEngine.encrypt(vek, kek, "VEK_WRAP".encodeToByteArray())
            .map { encrypted ->
                val wrapped = WrappedKey(
                    ciphertext = encrypted.ciphertext.copyOf(),
                    nonce = encrypted.nonce.copyOf(),
                )
                // WrappedKey takes independent ownership before every temporary
                // representation, including the duplicate tag, is cleared.
                encrypted.clear()
                wrapped
            }
    }

    /**
     * Unwrap VEK with Key Encryption Key.
     */
    suspend fun unwrapVEK(
        wrapped: WrappedKey,
        kek: ByteArray,
    ): Result<ByteArray> {
        return cryptoEngine.decrypt(
            wrapped.ciphertext,
            wrapped.nonce,
            kek,
            "VEK_WRAP".encodeToByteArray(),
        )
    }

    /**
     * Derive record encryption key from VEK.
     */
    suspend fun deriveRecordKey(
        vek: ByteArray,
        recordId: String,
    ): Result<ByteArray> {
        return cryptoEngine.deriveSubkey(
            vek,
            "record:$recordId",
            32,
        )
    }

    /**
     * Derive attachment encryption key from VEK.
     */
    suspend fun deriveAttachmentKey(
        vek: ByteArray,
        attachmentId: String,
    ): Result<ByteArray> {
        return cryptoEngine.deriveSubkey(
            vek,
            "attachment:$attachmentId",
            32,
        )
    }

    /**
     * Derive backup encryption key from VEK.
     */
    suspend fun deriveBackupKey(
        vek: ByteArray,
    ): Result<ByteArray> {
        return cryptoEngine.deriveSubkey(
            vek,
            "backup",
            32,
        )
    }

    /**
     * Derive search index key from VEK.
     */
    suspend fun deriveSearchKey(
        vek: ByteArray,
    ): Result<ByteArray> {
        return cryptoEngine.deriveSubkey(
            vek,
            "search",
            32,
        )
    }

    /**
     * Derive duplicate detection key from VEK.
     */
    suspend fun deriveDuplicateKey(
        vek: ByteArray,
    ): Result<ByteArray> {
        return cryptoEngine.deriveSubkey(
            vek,
            "duplicate",
            32,
        )
    }
}

data class WrappedKey(
    val ciphertext: ByteArray,
    val nonce: ByteArray,
) {
    override fun equals(other: Any?): Boolean =
        other is WrappedKey &&
            ciphertext.contentEquals(other.ciphertext) &&
            nonce.contentEquals(other.nonce)

    override fun hashCode(): Int =
        31 * ciphertext.contentHashCode() + nonce.contentHashCode()

    fun clear() {
        ciphertext.fill(0)
        nonce.fill(0)
    }
}

/**
 * Canonical representation used when encrypted values are persisted.
 *
 * `EncryptedData` keeps the tag as a separate field for API compatibility, while
 * the libsodium AEAD implementation returns an authenticated ciphertext that
 * already contains the tag.  This adapter makes the on-disk representation
 * unambiguous and reads the duplicated-tag representation written by older builds.
 */
object CryptoEnvelope {
    private const val MAGIC_SIZE = 4
    private val MAGIC = byteArrayOf(0x50, 0x56, 0x02, 0x00)
    private const val TAG_SIZE = 16

    fun encode(encrypted: EncryptedData): ByteArray {
        require(encrypted.tag.size == TAG_SIZE) { "AEAD tags must be exactly $TAG_SIZE bytes" }
        return if (encrypted.ciphertext.startsWithMagic()) {
            require(
                encrypted.ciphertext.size >= MAGIC_SIZE + TAG_SIZE &&
                    encrypted.ciphertext.endsWith(encrypted.tag),
            ) { "Versioned ciphertext does not contain its authentication tag" }
            encrypted.ciphertext.copyOf()
        } else {
            encrypted.ciphertext + encrypted.tag
        }
    }

    /**
     * Returns true only for a structurally valid versioned AEAD payload.
     * Authentication is still performed by [CryptoEngine.decrypt]; this check
     * lets import boundaries reject legacy, truncated, or unversioned records
     * before they can replace a healthy vault.
     */
    fun isSupportedPayload(stored: ByteArray): Boolean {
        if (!stored.startsWithMagic()) return false
        val normalizedSize =
            if (stored.hasDuplicatedTrailingTag()) stored.size - TAG_SIZE else stored.size
        return normalizedSize >= MAGIC_SIZE + TAG_SIZE
    }

    /**
     * Normalizes a stored payload before passing it to `CryptoEngine.decrypt`.
     *
     * New AEAD values are already `MAGIC || ciphertext || tag`.  Older PassVault
     * records may be `MAGIC || ciphertext || tag || tag`; the final duplicate tag
     * is removed only when it is byte-for-byte identical to the preceding tag.
     * Fake/test engines use an unversioned ciphertext plus a trailing tag.
     */
    fun normalize(stored: ByteArray): ByteArray {
        val isVersioned = stored.startsWithMagic()
        return when {
            isVersioned && stored.hasDuplicatedTrailingTag() -> {
                stored.copyOfRange(0, stored.size - TAG_SIZE)
            }
            isVersioned -> stored.copyOf()
            stored.size >= TAG_SIZE -> stored.copyOfRange(0, stored.size - TAG_SIZE)
            else -> stored.copyOf()
        }
    }

    private fun ByteArray.startsWithMagic(): Boolean =
        size >= MAGIC_SIZE && copyOfRange(0, MAGIC_SIZE).contentEquals(MAGIC)

    private fun ByteArray.endsWith(suffix: ByteArray): Boolean =
        size >= suffix.size && suffix.indices.all { index ->
            this[size - suffix.size + index] == suffix[index]
        }

    private fun ByteArray.hasDuplicatedTrailingTag(): Boolean {
        if (size < MAGIC_SIZE + TAG_SIZE * 2) return false
        val lastStart = size - TAG_SIZE
        val previousStart = lastStart - TAG_SIZE
        return (0 until TAG_SIZE).all { offset ->
            this[lastStart + offset] == this[previousStart + offset]
        }
    }
}
