package com.passvault.core.database.repository

import androidx.room.Room
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.PaddedPayload
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.dao.CredentialDao
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.Tag
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.testing.fakes.FakeCryptoEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Real bundled SQLite, Room queries and repository mapping at the SQL parameter
 * boundary. Fake crypto only keeps the 32k-row synthetic fixture cheap; this test
 * makes no cryptographic-security or unlocked-session-authorization claim.
 */
class CredentialSummaryBatchingTest {
    @Test
    fun `large summary lists preserve all rows and tags without oversized IN queries`() = runTest {
        val database = Room.inMemoryDatabaseBuilder<VaultDatabase>().setDriver(BundledSQLiteDriver()).build()
        val engine = FakeCryptoEngine()
        val session = SyntheticSession()
        val querySizes = mutableListOf<Int>()
        val delegate = database.credentialDao()
        val dao = object : CredentialDao by delegate {
            override suspend fun getTagCrossRefsForCredentials(
                credentialIds: List<String>,
            ): List<CredentialTagCrossRef> {
                querySizes += credentialIds.size
                return delegate.getTagCrossRefsForCredentials(credentialIds)
            }
        }
        val repository = CredentialRepositoryImpl(
            credentialDao = dao,
            folderDao = database.folderDao(),
            tagDao = database.tagDao(),
            attachmentDao = database.attachmentDao(),
            passwordHistoryDao = database.passwordHistoryDao(),
            cryptoEngine = engine,
            sessionManager = session,
        )
        try {
            assertTrue(repository.getAllSummaries().getOrThrow().isEmpty())
            assertTrue(querySizes.isEmpty())
            var seeded = 0
            for (count in listOf(899, 900, 901, 32_765, 32_766, 32_767)) {
                seed(database, engine, session, seeded, count)
                seeded = count
                querySizes.clear()
                val summaries = repository.getAllSummaries().getOrThrow()
                assertEquals(count, summaries.size)
                assertEquals(count, summaries.map { it.id }.toSet().size)
                assertEquals(id(count - 1), summaries.first().id.value)
                assertEquals(id(0), summaries.last().id.value)
                assertTrue(summaries.all { it.tagIds.isEmpty() })
                assertEquals(count, querySizes.sum())
                assertTrue(querySizes.all { it in 1..900 })
            }

            val tag = Tag(TagId("large-list-tag"), "Synthetic tag", null)
            TagRepositoryImpl(database.tagDao(), engine, session).save(tag).getOrThrow()
            val taggedIds = setOf(id(0), id(899), id(900), id(32_765), id(32_766))
            delegate.addTagCrossRefs(taggedIds.map { CredentialTagCrossRef(it, tag.id.value) })
            val summaries = repository.getAllSummaries().getOrThrow()
            assertEquals(taggedIds, summaries.filter { it.tagIds == setOf(tag.id) }.map { it.id.value }.toSet())
            assertEquals(32_767, summaries.size)
            val credential = assertNotNull(repository.getById(CredentialId(id(0))).getOrThrow())
            try {
                assertEquals("Synthetic 0", credential.title)
                assertEquals(setOf(tag.id), credential.tagIds)
            } finally {
                credential.clearSensitiveValues()
            }
        } finally {
            session.clear()
            database.close()
        }
    }

    private suspend fun seed(
        database: VaultDatabase,
        engine: FakeCryptoEngine,
        session: SyntheticSession,
        from: Int,
        toExclusive: Int,
    ) {
        // Bounded seed pages avoid retaining a second whole-vault fixture and
        // bypass per-save count refresh without changing any production query.
        for (indexes in (from until toExclusive).chunked(500)) {
            val rows = session.withUnlockedSession { key -> indexes.map { encryptedRecord(engine, key, it) } }
            database.useWriterConnection { connection ->
                connection.immediateTransaction {
                    connection.usePrepared(
                        """
                        INSERT INTO credential_records (
                            id, type, summary_payload, summary_nonce, secret_payload, secret_nonce,
                            folder_id, is_favorite, created_at, updated_at, last_used_at
                        ) VALUES (?, 'Login', ?, ?, ?, ?, NULL, 0, 0, ?, NULL)
                        """.trimIndent(),
                    ) { statement ->
                        rows.forEach { row ->
                            statement.bindText(1, row.id)
                            statement.bindBlob(2, row.summaryPayload)
                            statement.bindBlob(3, row.summaryNonce)
                            statement.bindBlob(4, row.secretPayload)
                            statement.bindBlob(5, row.secretNonce)
                            statement.bindLong(6, row.updatedAt)
                            check(!statement.step())
                            statement.reset()
                        }
                    }
                }
            }
        }
    }

    private suspend fun encryptedRecord(
        engine: FakeCryptoEngine,
        vek: ByteArray,
        index: Int,
    ): CredentialRecordEntity {
        val id = id(index)
        val key = engine.deriveSubkey(vek, "record:$id", 32).getOrThrow()
        val summary = """{"title":"Synthetic $index","usernameHint":null,"emailHint":null}""".encodeToByteArray()
        val secret = """
            {
                "username":null,"email":null,"password":null,"urls":[],"notes":null,
                "recoveryCodes":[],"apiKeys":[],"licenseKeys":[],"customFields":[]
            }
        """.trimIndent().encodeToByteArray()
        try {
            val encryptedSummary = PaddedPayload.encrypt(
                engine, summary, key, "passvault:credential:$id:summary:v1".encodeToByteArray(), 4096,
            ).getOrThrow()
            try {
                val encryptedSecret = PaddedPayload.encrypt(
                    engine, secret, key, "passvault:credential:$id:secret:v1".encodeToByteArray(), 4096,
                ).getOrThrow()
                try {
                    return CredentialRecordEntity(
                        id = id,
                        type = "Login",
                        summaryPayload = CryptoEnvelope.encode(encryptedSummary),
                        summaryNonce = encryptedSummary.nonce.copyOf(),
                        secretPayload = CryptoEnvelope.encode(encryptedSecret),
                        secretNonce = encryptedSecret.nonce.copyOf(),
                        folderId = null,
                        createdAt = 0,
                        updatedAt = index.toLong(),
                        lastUsedAt = null,
                    )
                } finally {
                    encryptedSecret.clear()
                }
            } finally {
                encryptedSummary.clear()
            }
        } finally {
            key.fill(0)
            summary.fill(0)
            secret.fill(0)
        }
    }

    private fun id(index: Int) = "large-credential-$index"

    private class SyntheticSession : VaultSessionManager {
        private val key = ByteArray(32) { 0x5a }
        override suspend fun <T> withUnlockedSession(block: suspend (ByteArray) -> T): T = block(key)
        override suspend fun <T> lockAndRun(reason: LockReason, block: suspend () -> T): T = block()
        override suspend fun matchesMasterPassword(candidate: SensitiveText): Boolean = false
        fun clear() = key.fill(0)
    }
}
