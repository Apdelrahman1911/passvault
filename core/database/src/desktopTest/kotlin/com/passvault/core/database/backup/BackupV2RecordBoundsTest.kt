package com.passvault.core.database.backup

import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.testing.fakes.FakeCryptoEngine
import kotlinx.coroutines.test.runTest
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackupV2RecordBoundsTest {
    @Test
    fun `every record type accepts its exact plaintext ceiling and rejects one byte more`() {
        for (type in BackupRecordType.MANIFEST..BackupRecordType.FINAL) {
            val maximum = maximumBackupRecordPlaintextBytes(type)

            requireValidBackupRecordPlaintextSize(type, maximum)
            assertFailsWith<IllegalArgumentException>("type=$type") {
                requireValidBackupRecordPlaintextSize(type, maximum + 1)
            }
        }
    }

    @Test
    fun `metadata ceilings follow their encoded fields rather than rounded megabyte limits`() {
        assertEquals(44, maximumBackupRecordPlaintextBytes(BackupRecordType.MANIFEST))
        assertEquals(1_681, maximumBackupRecordPlaintextBytes(BackupRecordType.METADATA))
        assertEquals(67_982, maximumBackupRecordPlaintextBytes(BackupRecordType.FOLDER))
        assertEquals(66_941, maximumBackupRecordPlaintextBytes(BackupRecordType.TAG))
        assertEquals(33_688_811, maximumBackupRecordPlaintextBytes(BackupRecordType.CREDENTIAL))
        assertEquals(2_056, maximumBackupRecordPlaintextBytes(BackupRecordType.CREDENTIAL_TAG_REFERENCE))
        assertEquals(265_588, maximumBackupRecordPlaintextBytes(BackupRecordType.ATTACHMENT))
        assertEquals(133_208, maximumBackupRecordPlaintextBytes(BackupRecordType.PASSWORD_HISTORY))
        assertTrue(maximumBackupRecordPlaintextBytes(BackupRecordType.CREDENTIAL) < 33 * 1024 * 1024)
    }

    @Test
    fun `truncated declared source is rejected before allocating or requesting ciphertext`() = runTest {
        val maximum = maximumBackupRecordPlaintextBytes(BackupRecordType.CREDENTIAL)
        val source = TrackingSource(
            headerRemainder() + recordPrefix(BackupRecordType.CREDENTIAL, maximum),
        )

        withReader(source) { reader ->
            assertFailsWith<IllegalArgumentException> {
                reader.readRecord(BackupRecordType.CREDENTIAL)
            }
        }

        assertFalse(source.readPastAvailableBytes)
    }

    @Test
    fun `large ciphertext from unknown or overstated sources is accumulated with bounded reads`() = runTest {
        val maximum = maximumBackupRecordPlaintextBytes(BackupRecordType.CREDENTIAL)
        val bytes = headerRemainder() + recordPrefix(BackupRecordType.CREDENTIAL, maximum)
        val claimedCompleteSize = BACKUP_V2_MAGIC.size.toLong() + bytes.size + maximum + ENCRYPTION_OVERHEAD_BYTES

        listOf<Long?>(null, claimedCompleteSize).forEach { declaredSize ->
            val source = TrackingSource(bytes.copyOf(), declaredSize)
            withReader(source) { reader ->
                assertFailsWith<IllegalArgumentException> {
                    reader.readRecord(BackupRecordType.CREDENTIAL)
                }
            }

            assertTrue(source.readPastAvailableBytes)
            assertTrue(source.maximumReadRequestBytes <= MAX_CIPHERTEXT_READ_BYTES)
        }
    }

    @Test
    fun `large valid record round trips through deferred ciphertext accumulation`() = runTest {
        val plaintext = ByteArray(BackupLimits.RECORD_PLAINTEXT_BYTES + 1) { index -> index.toByte() }
        val sink = CollectingSink()
        val password = SensitiveText.from(TEST_PASSWORD)
        try {
            val writer = BackupV2Writer.create(sink, password, FakeCryptoEngine())
            try {
                writer.writeRecord(BackupRecordType.CREDENTIAL, plaintext)
            } finally {
                writer.clear()
            }
        } finally {
            password.clear()
        }

        val backupBytes = sink.bytes()
        assertContentEquals(BACKUP_V2_MAGIC, backupBytes.copyOf(BACKUP_V2_MAGIC.size))
        val source = TrackingSource(backupBytes.copyOfRange(BACKUP_V2_MAGIC.size, backupBytes.size))
        try {
            withReader(source) { reader ->
                val record = reader.readRecord(BackupRecordType.CREDENTIAL)
                try {
                    assertContentEquals(plaintext, record.plaintext)
                } finally {
                    record.plaintext.fill(0)
                }
                reader.requireExhausted()
            }
        } finally {
            plaintext.fill(0)
            backupBytes.fill(0)
        }
    }

    @Test
    fun `wrong type and maximum plus one fail before reading large record fields`() = runTest {
        val maximum = maximumBackupRecordPlaintextBytes(BackupRecordType.CREDENTIAL)
        val wrongTypeSource = TrackingSource(
            headerRemainder() + recordPrefix(BackupRecordType.CREDENTIAL, maximum),
        )
        withReader(wrongTypeSource) { reader ->
            assertFailsWith<IllegalArgumentException> {
                reader.readRecord(BackupRecordType.MANIFEST)
            }
        }
        assertEquals(HEADER_REMAINDER_BYTES + 1, wrongTypeSource.deliveredBytes)

        val oversizedSource = TrackingSource(
            headerRemainder() + recordPrefix(BackupRecordType.CREDENTIAL, maximum + 1),
        )
        withReader(oversizedSource) { reader ->
            assertFailsWith<IllegalArgumentException> {
                reader.readRecord(BackupRecordType.CREDENTIAL)
            }
        }
        assertEquals(HEADER_REMAINDER_BYTES + RECORD_SIZE_PREFIX_BYTES, oversizedSource.deliveredBytes)
    }

    private suspend fun withReader(
        source: TrackingSource,
        block: suspend (BackupV2Reader) -> Unit,
    ) {
        val password = SensitiveText.from(TEST_PASSWORD)
        val reader = try {
            BackupV2Reader.createAfterMagic(
                source = source,
                password = password,
                cryptoEngine = FakeCryptoEngine(),
                magic = BACKUP_V2_MAGIC,
            )
        } finally {
            password.clear()
        }
        try {
            block(reader)
        } finally {
            reader.close()
            source.clear()
        }
    }

    private fun headerRemainder(): ByteArray = Buffer()
        .writeInt(BackupLimits.FORMAT_VERSION)
        .writeInt(3)
        .writeInt(64 * 1024 * 1024)
        .writeInt(1)
        .writeInt(BackupLimits.RECORD_PLAINTEXT_BYTES)
        .write(ByteArray(SALT_BYTES))
        .readByteArray()

    private fun recordPrefix(type: Int, plaintextBytes: Int): ByteArray = Buffer()
        .writeByte(type)
        .writeLong(0L)
        .writeInt(plaintextBytes)
        .write(ByteArray(NONCE_BYTES))
        .writeInt(plaintextBytes + ENCRYPTION_OVERHEAD_BYTES)
        .readByteArray()

    private class TrackingSource(
        private val bytes: ByteArray,
        override val declaredSizeBytes: Long? = (BACKUP_V2_MAGIC.size + bytes.size).toLong(),
    ) : BackupContentSource {
        private var offset = 0
        var deliveredBytes = 0
            private set
        var readPastAvailableBytes = false
            private set
        var maximumReadRequestBytes = 0
            private set

        override suspend fun read(buffer: ByteArray): Int {
            maximumReadRequestBytes = maxOf(maximumReadRequestBytes, buffer.size)
            if (offset == bytes.size) {
                readPastAvailableBytes = true
                return -1
            }
            val count = minOf(buffer.size, bytes.size - offset)
            bytes.copyInto(buffer, startIndex = offset, endIndex = offset + count)
            offset += count
            deliveredBytes += count
            return count
        }

        override suspend fun rewind() {
            offset = 0
            deliveredBytes = 0
            readPastAvailableBytes = false
        }

        override suspend fun close() = Unit

        fun clear() = bytes.fill(0)
    }

    private class CollectingSink : BackupContentSink {
        private val buffer = Buffer()

        override suspend fun write(buffer: ByteArray, byteCount: Int) {
            require(byteCount in 0..buffer.size)
            this.buffer.write(buffer, 0, byteCount)
        }

        override suspend fun commit() = Unit

        override suspend fun abort() = buffer.clear()

        fun bytes(): ByteArray = buffer.readByteArray()
    }

    private companion object {
        const val TEST_PASSWORD = "record bounds test password"
        const val SALT_BYTES = 16
        const val NONCE_BYTES = 24
        const val ENCRYPTION_OVERHEAD_BYTES = 20
        const val MAX_CIPHERTEXT_READ_BYTES = 64 * 1024
        const val HEADER_REMAINDER_BYTES = 5 * Int.SIZE_BYTES + SALT_BYTES
        const val RECORD_SIZE_PREFIX_BYTES = 1 + Long.SIZE_BYTES + Int.SIZE_BYTES
    }
}
