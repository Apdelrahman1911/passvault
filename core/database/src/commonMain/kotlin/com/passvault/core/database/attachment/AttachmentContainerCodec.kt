package com.passvault.core.database.attachment

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentCorruptedException
import com.passvault.core.domain.repository.AttachmentFileTooLargeException
import com.passvault.core.domain.repository.AttachmentPolicy
import com.passvault.core.domain.repository.AttachmentTotalSizeLimitException
import kotlinx.coroutines.CancellationException
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.HashingSource
import okio.buffer

internal data class AttachmentContentBinding(
    val attachmentId: String,
    val credentialId: String,
    val keyDerivationContext: String,
    val mimeType: String,
)

internal data class StoredAttachmentContent(
    val sizeBytes: Long,
    val mimeType: String,
)

private data class InitialChunk(
    val count: Int,
    val reachedEndOfFile: Boolean,
)

/** Versioned, chunked XChaCha20-Poly1305 attachment container. */
internal class AttachmentContainerCodec(
    private val blobStore: AttachmentBlobStore,
    private val cryptoEngine: CryptoEngine,
) {
    suspend fun encryptToObject(
        relativePath: String,
        source: AttachmentContentSource,
        key: ByteArray,
        bindingWithoutMime: AttachmentContentBinding,
        existingCredentialBytes: Long,
    ): StoredAttachmentContent {
        require(key.size == KEY_BYTES)
        require(existingCredentialBytes in 0..AttachmentPolicy.MAX_TOTAL_SIZE_PER_CREDENTIAL_BYTES)
        val plaintext = ByteArray(AttachmentPolicy.CONTENT_CHUNK_BYTES)
        return try {
            val initialChunk = readInitialChunk(source, plaintext)
            val mimeType = detectMimeType(plaintext, initialChunk.count)
            val binding = bindingWithoutMime.copy(mimeType = mimeType)
            val size = blobStore.writeAtomically(relativePath) { sink ->
                writeHeader(sink)
                var totalBytes = 0L
                var chunkIndex = 0L
                var count = initialChunk.count
                while (count >= 0) {
                    if (count > 0) {
                        totalBytes += count
                        if (totalBytes > AttachmentPolicy.MAX_FILE_SIZE_BYTES) {
                            throw AttachmentFileTooLargeException()
                        }
                        if (
                            existingCredentialBytes + totalBytes >
                            AttachmentPolicy.MAX_TOTAL_SIZE_PER_CREDENTIAL_BYTES
                        ) {
                            throw AttachmentTotalSizeLimitException()
                        }
                        writeEncryptedRecord(
                            sink = sink,
                            recordType = RECORD_TYPE_DATA,
                            recordIndex = chunkIndex,
                            plaintext = plaintext,
                            plaintextSize = count,
                            key = key,
                            binding = binding,
                        )
                        chunkIndex++
                    }
                    count = if (initialChunk.reachedEndOfFile) {
                        -1
                    } else {
                        source.read(plaintext).validatedReadCount(plaintext.size)
                    }
                }
                writeFinalRecord(sink, key, binding, totalBytes, chunkIndex)
                totalBytes
            }
            StoredAttachmentContent(sizeBytes = size, mimeType = mimeType)
        } finally {
            plaintext.fill(0)
        }
    }

    private suspend fun readInitialChunk(
        source: AttachmentContentSource,
        plaintext: ByteArray,
    ): InitialChunk {
        val firstRead = source.read(plaintext).validatedReadCount(plaintext.size)
        if (firstRead < 0) return InitialChunk(count = 0, reachedEndOfFile = true)
        var count = firstRead
        var reachedEndOfFile = false
        while (count < MIME_SNIFF_BYTES && !reachedEndOfFile) {
            val prefixRemainder = ByteArray(MIME_SNIFF_BYTES - count)
            try {
                val read = source.read(prefixRemainder).validatedReadCount(prefixRemainder.size)
                if (read < 0) {
                    reachedEndOfFile = true
                } else {
                    prefixRemainder.copyInto(plaintext, destinationOffset = count, endIndex = read)
                    count += read
                }
            } finally {
                prefixRemainder.fill(0)
            }
        }
        return InitialChunk(count, reachedEndOfFile)
    }

    suspend fun decryptObject(
        relativePath: String,
        expectedSizeBytes: Long,
        key: ByteArray,
        binding: AttachmentContentBinding,
        sink: AttachmentContentSink,
    ) {
        val fingerprint = decryptObjectAndFingerprint(relativePath, expectedSizeBytes, key, binding, sink)
        cryptoEngine.secureWipe(fingerprint)
    }

    /**
     * Authenticates the complete container and returns a SHA-256 fingerprint of
     * the exact encrypted bytes that were authenticated.
     */
    @Suppress("TooGenericExceptionCaught") // Normalize arbitrary filesystem/crypto failures at this trust boundary.
    suspend fun decryptObjectAndFingerprint(
        relativePath: String,
        expectedSizeBytes: Long,
        key: ByteArray,
        binding: AttachmentContentBinding,
        sink: AttachmentContentSink,
    ): ByteArray {
        require(expectedSizeBytes in 0..AttachmentPolicy.MAX_FILE_SIZE_BYTES)
        require(key.size == KEY_BYTES)
        return try {
            blobStore.read(relativePath, MAX_ENCRYPTED_OBJECT_BYTES) { source, fileSize ->
                val hashingSource = HashingSource.sha256(source)
                readObject(hashingSource.buffer(), fileSize, expectedSizeBytes, key, binding, sink)
                hashingSource.hash.toByteArray()
            }
        } catch (error: Exception) {
            throw when (error) {
                is CancellationException -> error
                is AttachmentSinkException -> error.failure
                is AttachmentCorruptedException -> error
                else -> AttachmentCorruptedException()
            }
        }
    }

    private suspend fun readObject(
        source: BufferedSource,
        fileSize: Long,
        expectedSizeBytes: Long,
        key: ByteArray,
        binding: AttachmentContentBinding,
        sink: AttachmentContentSink,
    ) {
        require(fileSize >= MIN_ENCRYPTED_OBJECT_BYTES) { "The encrypted attachment is truncated" }
        readAndValidateHeader(source)
        var totalBytes = 0L
        var expectedIndex = 0L
        while (true) {
            val recordType = source.readByte().toInt() and 0xff
            val recordIndex = source.readLong()
            val plaintextSize = source.readInt()
            require(recordIndex == expectedIndex) { "Attachment chunks are out of order" }
            when (recordType) {
                RECORD_TYPE_DATA -> {
                    totalBytes += readDataRecord(
                        source,
                        recordIndex,
                        plaintextSize,
                        key,
                        binding,
                        sink,
                    )
                    require(totalBytes <= AttachmentPolicy.MAX_FILE_SIZE_BYTES)
                    expectedIndex++
                }
                RECORD_TYPE_FINAL -> {
                    readFinalRecord(source, recordIndex, plaintextSize, key, binding, totalBytes, expectedIndex)
                    require(totalBytes == expectedSizeBytes) {
                        "The attachment size does not match its authenticated metadata"
                    }
                    require(source.exhausted()) { "The encrypted attachment has trailing data" }
                    return
                }
                else -> error("The encrypted attachment record type is unsupported")
            }
        }
    }

    @Suppress("TooGenericExceptionCaught") // Platform sinks can fail with implementation-specific exceptions.
    private suspend fun readDataRecord(
        source: BufferedSource,
        recordIndex: Long,
        plaintextSize: Int,
        key: ByteArray,
        binding: AttachmentContentBinding,
        sink: AttachmentContentSink,
    ): Int {
        require(plaintextSize in 1..AttachmentPolicy.CONTENT_CHUNK_BYTES)
        val plaintext = readAndDecryptRecord(
            source = source,
            recordType = RECORD_TYPE_DATA,
            recordIndex = recordIndex,
            plaintextSize = plaintextSize,
            key = key,
            binding = binding,
        )
        try {
            try {
                sink.write(plaintext, plaintext.size)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                throw AttachmentSinkException(error)
            }
            return plaintext.size
        } finally {
            cryptoEngine.secureWipe(plaintext)
        }
    }

    private suspend fun readFinalRecord(
        source: BufferedSource,
        recordIndex: Long,
        plaintextSize: Int,
        key: ByteArray,
        binding: AttachmentContentBinding,
        totalBytes: Long,
        expectedIndex: Long,
    ) {
        require(plaintextSize == FINAL_PAYLOAD_BYTES)
        val finalPayload = readAndDecryptRecord(
            source = source,
            recordType = RECORD_TYPE_FINAL,
            recordIndex = recordIndex,
            plaintextSize = plaintextSize,
            key = key,
            binding = binding,
        )
        try {
            val final = Buffer().write(finalPayload)
            require(final.readLong() == totalBytes)
            require(final.readLong() == expectedIndex)
            require(final.exhausted())
        } finally {
            cryptoEngine.secureWipe(finalPayload)
        }
    }

    private fun writeHeader(sink: BufferedSink) {
        sink.write(CONTAINER_MAGIC)
        sink.writeInt(AttachmentPolicy.CONTENT_FORMAT_VERSION)
        sink.writeInt(AttachmentPolicy.CONTENT_CHUNK_BYTES)
    }

    private fun readAndValidateHeader(source: BufferedSource) {
        require(source.readByteArray(CONTAINER_MAGIC.size.toLong()).contentEquals(CONTAINER_MAGIC)) {
            "The encrypted attachment header is invalid"
        }
        require(source.readInt() == AttachmentPolicy.CONTENT_FORMAT_VERSION) {
            "The encrypted attachment version is unsupported"
        }
        require(source.readInt() == AttachmentPolicy.CONTENT_CHUNK_BYTES) {
            "The encrypted attachment chunk size is unsupported"
        }
    }

    private suspend fun writeFinalRecord(
        sink: BufferedSink,
        key: ByteArray,
        binding: AttachmentContentBinding,
        totalBytes: Long,
        chunkCount: Long,
    ) {
        val finalPayload = Buffer()
            .writeLong(totalBytes)
            .writeLong(chunkCount)
            .readByteArray()
        try {
            writeEncryptedRecord(
                sink = sink,
                recordType = RECORD_TYPE_FINAL,
                recordIndex = chunkCount,
                plaintext = finalPayload,
                plaintextSize = finalPayload.size,
                key = key,
                binding = binding,
            )
        } finally {
            cryptoEngine.secureWipe(finalPayload)
        }
    }

    private suspend fun writeEncryptedRecord(
        sink: BufferedSink,
        recordType: Int,
        recordIndex: Long,
        plaintext: ByteArray,
        plaintextSize: Int,
        key: ByteArray,
        binding: AttachmentContentBinding,
    ) {
        val ownedPlaintext = plaintext.copyOf(plaintextSize)
        val associatedData = recordAssociatedData(binding, recordType, recordIndex, plaintextSize)
        try {
            val encrypted = cryptoEngine.encrypt(ownedPlaintext, key, associatedData).getOrThrow()
            try {
                require(encrypted.nonce.size == NONCE_BYTES)
                require(encrypted.ciphertext.size == plaintextSize + ENCRYPTION_OVERHEAD_BYTES)
                sink.writeByte(recordType)
                sink.writeLong(recordIndex)
                sink.writeInt(plaintextSize)
                sink.write(encrypted.nonce)
                sink.writeInt(encrypted.ciphertext.size)
                sink.write(encrypted.ciphertext)
            } finally {
                encrypted.clear()
            }
        } finally {
            cryptoEngine.secureWipe(ownedPlaintext)
            cryptoEngine.secureWipe(associatedData)
        }
    }

    private suspend fun readAndDecryptRecord(
        source: BufferedSource,
        recordType: Int,
        recordIndex: Long,
        plaintextSize: Int,
        key: ByteArray,
        binding: AttachmentContentBinding,
    ): ByteArray {
        val nonce = source.readByteArray(NONCE_BYTES.toLong())
        val ciphertextSize = source.readInt()
        require(ciphertextSize == plaintextSize + ENCRYPTION_OVERHEAD_BYTES)
        val ciphertext = source.readByteArray(ciphertextSize.toLong())
        val associatedData = recordAssociatedData(binding, recordType, recordIndex, plaintextSize)
        return try {
            cryptoEngine.decrypt(ciphertext, nonce, key, associatedData)
                .getOrElse { throw AttachmentCorruptedException() }
                .also {
                require(it.size == plaintextSize)
            }
        } finally {
            cryptoEngine.secureWipe(nonce)
            cryptoEngine.secureWipe(ciphertext)
            cryptoEngine.secureWipe(associatedData)
        }
    }

    private fun recordAssociatedData(
        binding: AttachmentContentBinding,
        recordType: Int,
        recordIndex: Long,
        plaintextSize: Int,
    ): ByteArray = Buffer()
        .writeUtf8(AAD_DOMAIN)
        .writeLengthPrefixedUtf8(binding.attachmentId)
        .writeLengthPrefixedUtf8(binding.credentialId)
        .writeLengthPrefixedUtf8(binding.keyDerivationContext)
        .writeLengthPrefixedUtf8(binding.mimeType)
        .writeInt(recordType)
        .writeLong(recordIndex)
        .writeInt(plaintextSize)
        .readByteArray()

    private fun Buffer.writeLengthPrefixedUtf8(value: String): Buffer {
        val encoded = value.encodeToByteArray(throwOnInvalidSequence = true)
        try {
            writeInt(encoded.size)
            write(encoded)
        } finally {
            encoded.fill(0)
        }
        return this
    }

    private fun Int.validatedReadCount(bufferSize: Int): Int {
        require(this == -1 || this in 1..bufferSize) { "The attachment source returned an invalid byte count" }
        return this
    }

    private fun detectMimeType(bytes: ByteArray, byteCount: Int): String = when {
        bytes.startsWith(byteCount, PDF_MAGIC) -> "application/pdf"
        bytes.startsWith(byteCount, PNG_MAGIC) -> "image/png"
        bytes.startsWith(byteCount, JPEG_MAGIC) -> "image/jpeg"
        bytes.startsWith(byteCount, GIF87_MAGIC) || bytes.startsWith(byteCount, GIF89_MAGIC) -> "image/gif"
        bytes.startsWith(byteCount, ZIP_MAGIC) -> "application/zip"
        bytes.isWebp(byteCount) -> "image/webp"
        else -> "application/octet-stream"
    }

    private fun ByteArray.startsWith(byteCount: Int, prefix: ByteArray): Boolean =
        byteCount >= prefix.size && prefix.indices.all { index -> this[index] == prefix[index] }

    private fun ByteArray.isWebp(byteCount: Int): Boolean =
        byteCount >= WEBP_HEADER_BYTES &&
            startsWith(byteCount, RIFF_MAGIC) &&
            WEBP_MAGIC.indices.all { index -> this[WEBP_OFFSET + index] == WEBP_MAGIC[index] }

    companion object {
        private const val MIME_SNIFF_BYTES = 12
        private const val KEY_BYTES = 32
        private const val NONCE_BYTES = 24
        private const val ENCRYPTION_OVERHEAD_BYTES = 20
        private const val RECORD_HEADER_BYTES = 1 + 8 + 4 + NONCE_BYTES + 4
        private const val FINAL_PAYLOAD_BYTES = 16
        private const val CONTAINER_HEADER_BYTES = 8 + 4 + 4
        private const val RECORD_TYPE_DATA = 1
        private const val RECORD_TYPE_FINAL = 2
        private const val WEBP_HEADER_BYTES = 12
        private const val WEBP_OFFSET = 8
        private const val AAD_DOMAIN = "passvault:attachment-content:v1"
        private val CONTAINER_MAGIC = byteArrayOf(0x50, 0x56, 0x41, 0x54, 0x54, 0x00, 0x01, 0x00)
        private val PDF_MAGIC = "%PDF-".encodeToByteArray()
        private val PNG_MAGIC = byteArrayOf(-119, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)
        private val JPEG_MAGIC = byteArrayOf(-1, -40, -1)
        private val GIF87_MAGIC = "GIF87a".encodeToByteArray()
        private val GIF89_MAGIC = "GIF89a".encodeToByteArray()
        private val ZIP_MAGIC = byteArrayOf(0x50, 0x4b, 0x03, 0x04)
        private val RIFF_MAGIC = "RIFF".encodeToByteArray()
        private val WEBP_MAGIC = "WEBP".encodeToByteArray()
        private val MAX_CHUNKS =
            (AttachmentPolicy.MAX_FILE_SIZE_BYTES + AttachmentPolicy.CONTENT_CHUNK_BYTES - 1) /
                AttachmentPolicy.CONTENT_CHUNK_BYTES
        internal val MAX_ENCRYPTED_OBJECT_BYTES =
            CONTAINER_HEADER_BYTES +
                AttachmentPolicy.MAX_FILE_SIZE_BYTES +
                MAX_CHUNKS * (RECORD_HEADER_BYTES + ENCRYPTION_OVERHEAD_BYTES) +
                RECORD_HEADER_BYTES + FINAL_PAYLOAD_BYTES + ENCRYPTION_OVERHEAD_BYTES
        private val MIN_ENCRYPTED_OBJECT_BYTES =
            (CONTAINER_HEADER_BYTES + RECORD_HEADER_BYTES + FINAL_PAYLOAD_BYTES +
                ENCRYPTION_OVERHEAD_BYTES).toLong()
    }

    private class AttachmentSinkException(val failure: Exception) : Exception()
}
