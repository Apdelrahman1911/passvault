package com.passvault.core.database.backup

import com.passvault.core.crypto.Argon2Parameters
import com.passvault.core.crypto.DerivedKey
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.testing.fakes.FakeCryptoEngine
import kotlinx.coroutines.test.runTest
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupV2KdfPolicyTest {
    @Test
    fun `reader rejects profiles never emitted by format two before key derivation`() = runTest {
        val rejectedProfiles = listOf(
            Argon2Parameters(opsLimit = 2, memLimit = V2_MEMORY_BYTES),
            Argon2Parameters(opsLimit = 5, memLimit = V2_MEMORY_BYTES),
            Argon2Parameters(opsLimit = 3, memLimit = 32 * MIB),
            Argon2Parameters(opsLimit = 3, memLimit = 256 * MIB),
            Argon2Parameters(opsLimit = 10, memLimit = 256 * MIB),
        )

        rejectedProfiles.forEach { parameters ->
            val cryptoEngine = RecordingCryptoEngine()
            val source = HeaderSource(headerRemainder(parameters))
            val password = SensitiveText.from(TEST_PASSWORD)
            val result = try {
                runCatching {
                    BackupV2Reader.createAfterMagic(
                        source = source,
                        password = password,
                        cryptoEngine = cryptoEngine,
                        magic = BACKUP_V2_MAGIC,
                    )
                }
            } finally {
                password.clear()
            }
            result.getOrNull()?.close()

            assertTrue(result.isFailure, "Unexpectedly accepted $parameters")
            assertEquals(0, cryptoEngine.deriveCalls, "Derived a key for $parameters")
            source.clear()
        }
    }

    @Test
    fun `writer profiles are exactly the profiles accepted by the reader`() = runTest {
        listOf(
            Argon2Parameters(opsLimit = 3, memLimit = V2_MEMORY_BYTES),
            Argon2Parameters(opsLimit = 4, memLimit = V2_MEMORY_BYTES),
        ).forEach { parameters ->
            val cryptoEngine = RecordingCryptoEngine(parameters)
            val sink = HeaderSink()
            val password = SensitiveText.from(TEST_PASSWORD)
            try {
                BackupV2Writer.create(sink, password, cryptoEngine).clear()
                assertEquals(listOf(parameters), cryptoEngine.derivedParameters)

                val header = sink.bytes()
                val magic = header.copyOfRange(0, BACKUP_V2_MAGIC.size)
                val source = HeaderSource(header.copyOfRange(BACKUP_V2_MAGIC.size, header.size))
                try {
                    BackupV2Reader.createAfterMagic(source, password, cryptoEngine, magic).close()
                    assertEquals(listOf(parameters, parameters), cryptoEngine.derivedParameters)
                    assertContentEquals(BACKUP_V2_MAGIC, header.copyOfRange(0, BACKUP_V2_MAGIC.size))
                } finally {
                    header.fill(0)
                    source.clear()
                }
            } finally {
                password.clear()
            }
        }
    }

    @Test
    fun `writer fails closed if benchmark drifts outside the format two profile`() = runTest {
        listOf(
            Argon2Parameters(opsLimit = 2, memLimit = V2_MEMORY_BYTES),
            Argon2Parameters(opsLimit = 5, memLimit = V2_MEMORY_BYTES),
            Argon2Parameters(opsLimit = 3, memLimit = 32 * MIB),
            Argon2Parameters(opsLimit = 3, memLimit = 256 * MIB),
        ).forEach { parameters ->
            val cryptoEngine = RecordingCryptoEngine(parameters)
            val password = SensitiveText.from(TEST_PASSWORD)
            val result = try {
                runCatching { BackupV2Writer.create(HeaderSink(), password, cryptoEngine) }
            } finally {
                password.clear()
            }
            result.getOrNull()?.clear()

            assertTrue(result.isFailure, "Unexpectedly emitted $parameters")
            assertEquals(0, cryptoEngine.deriveCalls, "Derived a key for $parameters")
        }
    }

    private fun headerRemainder(parameters: Argon2Parameters): ByteArray = Buffer()
        .writeInt(BackupLimits.FORMAT_VERSION)
        .writeInt(parameters.opsLimit)
        .writeInt(parameters.memLimit)
        .writeInt(V2_PARALLELISM)
        .writeInt(BackupLimits.RECORD_PLAINTEXT_BYTES)
        .write(ByteArray(V2_SALT_BYTES) { it.toByte() })
        .readByteArray()

    private class RecordingCryptoEngine(
        private val benchmarkParameters: Argon2Parameters = Argon2Parameters.INTERACTIVE,
    ) : FakeCryptoEngine() {
        val derivedParameters = mutableListOf<Argon2Parameters>()
        val deriveCalls: Int get() = derivedParameters.size

        override suspend fun deriveKey(
            password: ByteArray,
            salt: ByteArray,
            opsLimit: Int,
            memLimit: Int,
        ): Result<DerivedKey> {
            derivedParameters += Argon2Parameters(opsLimit, memLimit)
            return super.deriveKey(password, salt, opsLimit, memLimit)
        }

        override suspend fun benchmarkArgon2(): Argon2Parameters = benchmarkParameters
    }

    private class HeaderSource(private val bytes: ByteArray) : BackupContentSource {
        private var offset = 0
        override val declaredSizeBytes: Long = (BACKUP_V2_MAGIC.size + bytes.size).toLong()

        override suspend fun read(buffer: ByteArray): Int {
            if (offset == bytes.size) return -1
            val count = minOf(buffer.size, bytes.size - offset)
            bytes.copyInto(buffer, endIndex = offset + count, startIndex = offset)
            offset += count
            return count
        }

        override suspend fun rewind() {
            offset = 0
        }

        override suspend fun close() = Unit

        fun clear() = bytes.fill(0)
    }

    private class HeaderSink : BackupContentSink {
        private val buffer = Buffer()

        override suspend fun write(buffer: ByteArray, byteCount: Int) {
            this.buffer.write(buffer, 0, byteCount)
        }

        override suspend fun commit() = Unit

        override suspend fun abort() = Unit

        fun bytes(): ByteArray = buffer.clone().readByteArray()
    }

    private companion object {
        const val MIB = 1024 * 1024
        const val V2_MEMORY_BYTES = 64 * MIB
        const val V2_PARALLELISM = 1
        const val V2_SALT_BYTES = 16
        const val TEST_PASSWORD = "format two KDF policy password"
    }
}
