package com.passvault.core.database.attachment

import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentCorruptedException
import com.passvault.core.domain.repository.AttachmentPolicy
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.BufferedSink
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

class AttachmentContainerCodecCompatibilityTest {
    @Test
    fun `fragmented patterned bytes survive multiple chunk spill boundaries in order`() = runTest {
        withCodecFixture { fixture ->
            val size = 2L * AttachmentPolicy.CONTENT_CHUNK_BYTES + 17L
            val source = PatternedSource(size, maximumReadBytes = SHORT_RECORD_BYTES)
            val path = newObjectPath()
            val stored = fixture.codec.encryptToObject(path, source, fixture.key, BINDING, 0)

            assertEquals(size, stored.sizeBytes)
            assertEquals(BINDING.mimeType, stored.mimeType)
            assertEquals(HEADER_BYTES + size + 3L * RECORD_OVERHEAD_BYTES + FINAL_RECORD_BYTES, fixture.size(path))
            assertEquals(((size + SHORT_RECORD_BYTES - 1) / SHORT_RECORD_BYTES + 1).toInt(), source.readCalls)

            val sink = PatternCheckingSink(size)
            fixture.codec.decryptObject(path, size, fixture.key, BINDING, sink)
            assertEquals(size, sink.byteCount)
            assertEquals(3, sink.writeCalls)
        }
    }

    @Test
    fun `empty source contains only authenticated final totals and round trips no plaintext`() = runTest {
        withCodecFixture { fixture ->
            val source = PatternedSource(0, maximumReadBytes = SHORT_RECORD_BYTES)
            val path = newObjectPath()
            val stored = fixture.codec.encryptToObject(path, source, fixture.key, BINDING, 0)

            assertEquals(0L, stored.sizeBytes)
            assertEquals(1, source.readCalls)
            assertEquals(HEADER_BYTES + FINAL_RECORD_BYTES, fixture.size(path))
            val sink = PatternCheckingSink(0)
            fixture.codec.decryptObject(path, 0, fixture.key, BINDING, sink)
            assertEquals(0L, sink.byteCount)
            assertEquals(0, sink.writeCalls)
        }
    }

    @Test
    fun `historical v1 short nonfinal data records remain readable byte for byte`() = runTest {
        withCodecFixture { fixture ->
            val path = fixture.writeHistoricalShortRecordObject()
            val recordCount = (HISTORICAL_SIZE + SHORT_RECORD_BYTES - 1) / SHORT_RECORD_BYTES
            assertTrue(recordCount > 2)
            assertEquals(
                HEADER_BYTES + HISTORICAL_SIZE + recordCount * RECORD_OVERHEAD_BYTES + FINAL_RECORD_BYTES,
                fixture.size(path),
            )

            val sink = PatternCheckingSink(HISTORICAL_SIZE)
            fixture.codec.decryptObject(path, HISTORICAL_SIZE, fixture.key, BINDING, sink)
            assertEquals(HISTORICAL_SIZE, sink.byteCount)
            assertEquals(recordCount.toInt(), sink.writeCalls)
        }
    }

    @Test
    fun `historical short records still reject reordered data altered authentication and truncation`() = runTest {
        withCodecFixture { fixture ->
            listOf<(RandomAccessFile) -> Unit>(
                { file ->
                    // Move complete authenticated records, not freshly encrypted
                    // replacements. Their original indices must not be accepted.
                    val recordBytes = SHORT_RECORD_BYTES + RECORD_OVERHEAD_BYTES.toInt()
                    val first = ByteArray(recordBytes)
                    val second = ByteArray(recordBytes)
                    file.seek(HEADER_BYTES)
                    file.readFully(first)
                    file.readFully(second)
                    file.seek(HEADER_BYTES)
                    file.write(second)
                    file.write(first)
                },
                { file ->
                    file.seek(file.length() - 1)
                    val last = file.readUnsignedByte()
                    file.seek(file.length() - 1)
                    file.writeByte(last xor 1)
                },
                { file -> file.setLength(file.length() - 1) },
            ).forEach { tamper ->
                val path = fixture.writeHistoricalShortRecordObject()
                RandomAccessFile(fixture.root.resolve(path).toFile(), "rw").use(tamper)
                assertFailsWith<AttachmentCorruptedException> {
                    fixture.codec.decryptObject(
                        path,
                        HISTORICAL_SIZE,
                        fixture.key,
                        BINDING,
                        PatternCheckingSink(HISTORICAL_SIZE),
                    )
                }
            }
        }
    }

    private suspend fun withCodecFixture(block: suspend (CodecFixture) -> Unit) {
        val root = Files.createTempDirectory("passvault-attachment-codec-compat-")
        val key = ByteArray(32) { (it + 1).toByte() }
        try {
            block(CodecFixture(root, key))
        } finally {
            key.fill(0)
            root.toFile().deleteRecursively()
        }
    }

    private class CodecFixture(val root: Path, val key: ByteArray) {
        private val crypto = DesktopCryptoEngine()
        private val store = LocalAttachmentBlobStore(root.toString())
        val codec = AttachmentContainerCodec(store, crypto)

        fun size(path: String): Long = Files.size(root.resolve(path))

        /**
         * Explicit deployed v1 fixture, independent of encryptToObject/ChunkReader:
         * the historical writer sealed each positive source read as its own record.
         * Keep this small; no whole-file plaintext or maximum-object copy is needed.
         */
        suspend fun writeHistoricalShortRecordObject(): String {
            val path = newObjectPath()
            store.writeAtomically(path) { output ->
                output.write(byteArrayOf(0x50, 0x56, 0x41, 0x54, 0x54, 0x00, 0x01, 0x00))
                output.writeInt(1)
                output.writeInt(256 * 1024)
                var offset = 0L
                var index = 0L
                while (offset < HISTORICAL_SIZE) {
                    val count = minOf(SHORT_RECORD_BYTES.toLong(), HISTORICAL_SIZE - offset).toInt()
                    val plaintext = ByteArray(count) { byte -> patternedByte(offset + byte) }
                    writeHistoricalRecord(output, type = 1, index = index, plaintext = plaintext)
                    offset += count
                    index++
                }
                val totals = Buffer().writeLong(offset).writeLong(index).readByteArray()
                writeHistoricalRecord(output, type = 2, index = index, plaintext = totals)
            }
            return path
        }

        private suspend fun writeHistoricalRecord(output: BufferedSink, type: Int, index: Long, plaintext: ByteArray) {
            // Do not call the production AAD/framing builder: this intentionally
            // fixes the historical wire contract while exercising today's reader.
            val aad = Buffer().apply {
                writeUtf8("passvault:attachment-content:v1")
                listOf(BINDING.attachmentId, BINDING.credentialId, BINDING.keyDerivationContext, BINDING.mimeType)
                    .forEach { value ->
                        val bytes = value.encodeToByteArray()
                        writeInt(bytes.size)
                        write(bytes)
                        bytes.fill(0)
                    }
                writeInt(type)
                writeLong(index)
                writeInt(plaintext.size)
            }.readByteArray()
            try {
                val encrypted = crypto.encrypt(plaintext, key, aad).getOrThrow()
                try {
                    output.writeByte(type)
                    output.writeLong(index)
                    output.writeInt(plaintext.size)
                    output.write(encrypted.nonce)
                    output.writeInt(encrypted.ciphertext.size)
                    output.write(encrypted.ciphertext)
                } finally {
                    encrypted.clear()
                }
            } finally {
                plaintext.fill(0)
                aad.fill(0)
            }
        }
    }

    private class PatternedSource(private val size: Long, private val maximumReadBytes: Int) : AttachmentContentSource {
        override val displayName = "synthetic-pattern.bin"
        override val claimedMimeType: String? = null
        override val declaredSizeBytes = size
        var readCalls = 0
        private var offset = 0L

        override suspend fun read(buffer: ByteArray): Int {
            readCalls++
            if (offset == size) return -1
            val count = minOf(buffer.size.toLong(), maximumReadBytes.toLong(), size - offset).toInt()
            repeat(count) { index -> buffer[index] = patternedByte(offset + index) }
            offset += count
            return count
        }

        override suspend fun close() = Unit
    }

    private class PatternCheckingSink(private val expectedSize: Long) : AttachmentContentSink {
        var byteCount = 0L
        var writeCalls = 0

        override suspend fun write(buffer: ByteArray, byteCount: Int) {
            assertTrue(byteCount in 1..AttachmentPolicy.CONTENT_CHUNK_BYTES)
            assertTrue(this.byteCount + byteCount <= expectedSize)
            repeat(byteCount) { index ->
                val position = this.byteCount + index
                if (buffer[index] != patternedByte(position)) fail("Synthetic plaintext mismatch at byte $position")
            }
            this.byteCount += byteCount
            writeCalls++
        }

        override suspend fun commit() = Unit
        override suspend fun abort() = Unit
    }

    private companion object {
        const val SHORT_RECORD_BYTES = 113
        const val HISTORICAL_SIZE = 4_097L
        const val HEADER_BYTES = 16L
        const val RECORD_OVERHEAD_BYTES = 61L
        const val FINAL_RECORD_BYTES = 77L
        val BINDING = AttachmentContentBinding(
            "synthetic-attachment",
            "synthetic-credential",
            "synthetic-key",
            "application/octet-stream",
        )

        fun newObjectPath(): String = "objects/${UUID.randomUUID()}.pva"

        // Depend on multiple index bytes, not just position modulo 256: chunk
        // reordering and 113-byte spill duplication must change the expected data.
        fun patternedByte(position: Long): Byte =
            ((position * 73 + (position shr 8) * 19 + (position shr 16) * 11) and 0xff).toByte()
    }
}
