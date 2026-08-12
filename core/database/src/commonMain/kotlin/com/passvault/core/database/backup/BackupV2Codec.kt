package com.passvault.core.database.backup

import com.passvault.core.crypto.Argon2Parameters
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.DerivedKey
import com.passvault.core.domain.model.BackupPasswordPolicy
import com.passvault.core.domain.model.SensitiveText
import okio.Buffer
import okio.HashingSink
import okio.blackholeSink

internal object BackupRecordType {
    const val MANIFEST = 1
    const val METADATA = 2
    const val FOLDER = 3
    const val TAG = 4
    const val CREDENTIAL = 5
    const val CREDENTIAL_FOLDER_REFERENCE = 6
    const val CREDENTIAL_TAG_REFERENCE = 7
    const val ATTACHMENT = 8
    const val PASSWORD_HISTORY = 9
    const val METADATA_END = 10
    const val ATTACHMENT_START = 11
    const val ATTACHMENT_CONTENT = 12
    const val ATTACHMENT_END = 13
    const val FINAL = 14

    fun isValid(type: Int): Boolean = type in MANIFEST..FINAL
}

internal data class BackupRecord(
    val type: Int,
    val index: Long,
    val plaintext: ByteArray,
)

internal class BackupV2Writer private constructor(
    private val sink: BackupContentSink,
    private val cryptoEngine: CryptoEngine,
    private val derivedKey: DerivedKey,
    private val authenticatedHeader: ByteArray,
) {
    private var recordIndex = 0L
    private var writtenBytes = authenticatedHeader.size.toLong()

    suspend fun writeRecord(type: Int, plaintext: ByteArray) {
        require(BackupRecordType.isValid(type))
        require(plaintext.size <= BackupLimits.MAX_ENTITY_RECORD_BYTES)
        val associatedData = recordAssociatedData(type, recordIndex, plaintext.size)
        val encrypted = try {
            cryptoEngine.encrypt(plaintext, derivedKey.key, associatedData).getOrThrow()
        } finally {
            cryptoEngine.secureWipe(associatedData)
        }
        try {
            require(encrypted.nonce.size == NONCE_BYTES)
            require(encrypted.ciphertext.size == plaintext.size + ENCRYPTION_OVERHEAD_BYTES)
            val header = Buffer()
                .writeByte(type)
                .writeLong(recordIndex)
                .writeInt(plaintext.size)
                .write(encrypted.nonce)
                .writeInt(encrypted.ciphertext.size)
                .readByteArray()
            try {
                writeBounded(header)
                writeBounded(encrypted.ciphertext)
            } finally {
                cryptoEngine.secureWipe(header)
            }
            recordIndex++
        } finally {
            encrypted.clear()
        }
    }

    fun nextRecordIndex(): Long = recordIndex

    fun clear() {
        derivedKey.clear()
        cryptoEngine.secureWipe(authenticatedHeader)
    }

    private suspend fun writeBounded(bytes: ByteArray) {
        val nextSize = writtenBytes + bytes.size
        require(nextSize <= BackupLimits.MAX_BACKUP_BYTES)
        sink.write(bytes, bytes.size)
        writtenBytes = nextSize
    }

    private fun recordAssociatedData(type: Int, index: Long, plaintextSize: Int): ByteArray = Buffer()
        .write(authenticatedHeader)
        .writeUtf8(RECORD_AAD_DOMAIN)
        .writeByte(type)
        .writeLong(index)
        .writeInt(plaintextSize)
        .readByteArray()

    companion object {
        suspend fun create(
            sink: BackupContentSink,
            password: SensitiveText,
            cryptoEngine: CryptoEngine,
        ): BackupV2Writer {
            require(BackupPasswordPolicy.acceptsNew(password))
            var passwordBytes: ByteArray? = null
            var salt: ByteArray? = null
            var derivedKey: DerivedKey? = null
            var header: ByteArray? = null
            return try {
                val parameters = cryptoEngine.benchmarkArgon2().safeForBackup()
                salt = cryptoEngine.generateRandom(SALT_BYTES).getOrThrow()
                passwordBytes = password.toUtf8ByteArray()
                derivedKey = cryptoEngine.deriveKey(
                    password = passwordBytes,
                    salt = salt,
                    opsLimit = parameters.opsLimit,
                    memLimit = parameters.memLimit,
                ).getOrThrow()
                header = Buffer()
                    .write(MAGIC)
                    .writeInt(BackupLimits.FORMAT_VERSION)
                    .writeInt(parameters.opsLimit)
                    .writeInt(parameters.memLimit)
                    .writeInt(PARALLELISM)
                    .writeInt(BackupLimits.RECORD_PLAINTEXT_BYTES)
                    .write(salt)
                    .readByteArray()
                sink.write(header, header.size)
                BackupV2Writer(
                    sink = sink,
                    cryptoEngine = cryptoEngine,
                    derivedKey = derivedKey,
                    authenticatedHeader = header,
                ).also {
                    derivedKey = null
                    header = null
                }
            } finally {
                passwordBytes?.let(cryptoEngine::secureWipe)
                salt?.let(cryptoEngine::secureWipe)
                derivedKey?.clear()
                header?.let(cryptoEngine::secureWipe)
            }
        }
    }
}

internal class BackupV2Reader private constructor(
    private val source: BackupContentSource,
    private val cryptoEngine: CryptoEngine,
    private val derivedKey: DerivedKey,
    private val authenticatedHeader: ByteArray,
    private var consumedBytes: Long,
) {
    private var expectedRecordIndex = 0L
    private val metadataTranscriptSink = HashingSink.sha256(blackholeSink())
    private var transcriptFinalized = false

    init {
        writeTranscriptBytes(authenticatedHeader)
    }

    suspend fun readRecord(
        maxPlaintextBytes: Int,
        includeInMetadataTranscript: Boolean = false,
    ): BackupRecord {
        require(maxPlaintextBytes in 0..BackupLimits.MAX_ENTITY_RECORD_BYTES)
        val type = readByte()
        val index = readLong()
        val plaintextSize = readInt()
        require(BackupRecordType.isValid(type))
        require(index == expectedRecordIndex)
        require(plaintextSize in 0..maxPlaintextBytes)
        val nonce = readExact(NONCE_BYTES)
        val ciphertextSize = readInt()
        require(ciphertextSize == plaintextSize + ENCRYPTION_OVERHEAD_BYTES)
        val ciphertext = readExact(ciphertextSize)
        val associatedData = recordAssociatedData(type, index, plaintextSize)
        return try {
            val plaintext = cryptoEngine.decrypt(
                ciphertext = ciphertext,
                nonce = nonce,
                key = derivedKey.key,
                associatedData = associatedData,
            ).getOrThrow()
            require(plaintext.size == plaintextSize)
            if (includeInMetadataTranscript) {
                require(!transcriptFinalized)
                writeRecordProof(type, index, plaintextSize, nonce, ciphertext)
            }
            expectedRecordIndex++
            BackupRecord(type, index, plaintext)
        } finally {
            cryptoEngine.secureWipe(nonce)
            cryptoEngine.secureWipe(ciphertext)
            cryptoEngine.secureWipe(associatedData)
        }
    }

    suspend fun requireExhausted() {
        val probe = ByteArray(1)
        try {
            require(source.read(probe) == -1)
            source.declaredSizeBytes?.let { require(consumedBytes == it) }
        } finally {
            cryptoEngine.secureWipe(probe)
        }
    }

    fun metadataTranscript(): ByteArray {
        check(!transcriptFinalized)
        transcriptFinalized = true
        return metadataTranscriptSink.hash.toByteArray()
    }

    suspend fun close() {
        derivedKey.clear()
        cryptoEngine.secureWipe(authenticatedHeader)
        metadataTranscriptSink.close()
        source.close()
    }

    private fun writeRecordProof(
        type: Int,
        index: Long,
        plaintextSize: Int,
        nonce: ByteArray,
        ciphertext: ByteArray,
    ) {
        require(ciphertext.size >= TRANSCRIPT_TAG_BYTES)
        val proof = Buffer()
            .writeInt(type)
            .writeLong(index)
            .writeInt(plaintextSize)
            .write(nonce)
            .write(ciphertext, ciphertext.size - TRANSCRIPT_TAG_BYTES, TRANSCRIPT_TAG_BYTES)
        metadataTranscriptSink.write(proof, proof.size)
    }

    private fun writeTranscriptBytes(bytes: ByteArray) {
        val source = Buffer().write(bytes)
        metadataTranscriptSink.write(source, source.size)
    }

    private suspend fun readByte(): Int = readExact(1).let { bytes ->
        try {
            bytes[0].toInt() and 0xff
        } finally {
            cryptoEngine.secureWipe(bytes)
        }
    }

    private suspend fun readInt(): Int = readExact(Int.SIZE_BYTES).let { bytes ->
        try {
            Buffer().write(bytes).readInt()
        } finally {
            cryptoEngine.secureWipe(bytes)
        }
    }

    private suspend fun readLong(): Long = readExact(Long.SIZE_BYTES).let { bytes ->
        try {
            Buffer().write(bytes).readLong()
        } finally {
            cryptoEngine.secureWipe(bytes)
        }
    }

    private suspend fun readExact(size: Int): ByteArray {
        require(size >= 0)
        require(consumedBytes + size <= BackupLimits.MAX_BACKUP_BYTES)
        val bytes = ByteArray(size)
        var offset = 0
        var completed = false
        try {
            while (offset < size) {
                val temporary = ByteArray(minOf(READ_BUFFER_BYTES, size - offset))
                try {
                    val count = source.read(temporary)
                    require(count in 1..temporary.size)
                    temporary.copyInto(bytes, destinationOffset = offset, endIndex = count)
                    offset += count
                    consumedBytes += count
                } finally {
                    cryptoEngine.secureWipe(temporary)
                }
            }
            completed = true
            return bytes
        } finally {
            if (!completed) cryptoEngine.secureWipe(bytes)
        }
    }

    private fun recordAssociatedData(type: Int, index: Long, plaintextSize: Int): ByteArray = Buffer()
        .write(authenticatedHeader)
        .writeUtf8(RECORD_AAD_DOMAIN)
        .writeByte(type)
        .writeLong(index)
        .writeInt(plaintextSize)
        .readByteArray()

    companion object {
        suspend fun createAfterMagic(
            source: BackupContentSource,
            password: SensitiveText,
            cryptoEngine: CryptoEngine,
            magic: ByteArray,
        ): BackupV2Reader {
            require(BackupPasswordPolicy.acceptsExisting(password))
            require(magic.contentEquals(MAGIC))
            source.declaredSizeBytes?.let { require(it in HEADER_BYTES.toLong()..BackupLimits.MAX_BACKUP_BYTES) }
            var passwordBytes: ByteArray? = null
            var salt: ByteArray? = null
            var derivedKey: DerivedKey? = null
            var remainingHeader: ByteArray? = null
            return try {
                remainingHeader = readHeaderRemainder(source, cryptoEngine)
                val headerReader = Buffer().write(remainingHeader)
                val version = headerReader.readInt()
                val opsLimit = headerReader.readInt()
                val memLimit = headerReader.readInt()
                val parallelism = headerReader.readInt()
                val chunkBytes = headerReader.readInt()
                salt = headerReader.readByteArray(SALT_BYTES.toLong())
                require(headerReader.exhausted())
                require(version == BackupLimits.FORMAT_VERSION)
                require(opsLimit in MIN_ARGON2_OPS..MAX_ARGON2_OPS)
                require(memLimit in MIN_ARGON2_MEM..MAX_ARGON2_MEM)
                require(parallelism == PARALLELISM)
                require(chunkBytes == BackupLimits.RECORD_PLAINTEXT_BYTES)
                passwordBytes = password.toUtf8ByteArray()
                derivedKey = cryptoEngine.deriveKey(passwordBytes, salt, opsLimit, memLimit).getOrThrow()
                val authenticatedHeader = magic + remainingHeader
                BackupV2Reader(
                    source = source,
                    cryptoEngine = cryptoEngine,
                    derivedKey = derivedKey,
                    authenticatedHeader = authenticatedHeader,
                    consumedBytes = authenticatedHeader.size.toLong(),
                ).also { derivedKey = null }
            } finally {
                passwordBytes?.let(cryptoEngine::secureWipe)
                salt?.let(cryptoEngine::secureWipe)
                derivedKey?.clear()
                remainingHeader?.let(cryptoEngine::secureWipe)
                cryptoEngine.secureWipe(magic)
            }
        }

        private suspend fun readHeaderRemainder(
            source: BackupContentSource,
            cryptoEngine: CryptoEngine,
        ): ByteArray {
            val result = ByteArray(HEADER_BYTES - MAGIC.size)
            var offset = 0
            var completed = false
            try {
                while (offset < result.size) {
                    val temporary = ByteArray(minOf(READ_BUFFER_BYTES, result.size - offset))
                    try {
                        val count = source.read(temporary)
                        require(count in 1..temporary.size)
                        temporary.copyInto(result, destinationOffset = offset, endIndex = count)
                        offset += count
                    } finally {
                        cryptoEngine.secureWipe(temporary)
                    }
                }
                completed = true
                return result
            } finally {
                if (!completed) cryptoEngine.secureWipe(result)
            }
        }
    }
}

internal val BACKUP_V2_MAGIC: ByteArray
    get() = MAGIC.copyOf()

private fun Argon2Parameters.safeForBackup(): Argon2Parameters = copy(
    opsLimit = opsLimit.coerceIn(MIN_ARGON2_OPS, MAX_ARGON2_OPS),
    memLimit = memLimit.coerceIn(MIN_ARGON2_MEM, MAX_ARGON2_MEM),
)

private const val RECORD_AAD_DOMAIN = "passvault:backup-record:v2"
private const val SALT_BYTES = 16
private const val NONCE_BYTES = 24
private const val ENCRYPTION_OVERHEAD_BYTES = 20
private const val TRANSCRIPT_TAG_BYTES = 16
private const val READ_BUFFER_BYTES = 64 * 1024
private const val PARALLELISM = 1
private const val MIN_ARGON2_OPS = 2
private const val MAX_ARGON2_OPS = 10
private const val MIN_ARGON2_MEM = 32 * 1024 * 1024
private const val MAX_ARGON2_MEM = 256 * 1024 * 1024
private val MAGIC = byteArrayOf(0x50, 0x56, 0x42, 0x41, 0x43, 0x4b, 0x02, 0x00)
private val HEADER_BYTES = MAGIC.size + Int.SIZE_BYTES * 5 + SALT_BYTES
