package com.passvault.core.database.backup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BackupValidationLimitsTest {
    @Test
    fun `retained identifier count accepts exact aggregate ceiling and rejects one beyond it`() {
        val exact = manifest(credentialCount = 999_998, folderCount = 1)
        assertEquals(BackupLimits.MAX_RETAINED_IDENTIFIER_COUNT, exact.retainedIdentifierCount())
        exact.requireRetentionBound()

        assertFailsWith<IllegalArgumentException> {
            manifest(credentialCount = 999_999, folderCount = 1).requireRetentionBound()
        }
    }

    @Test
    fun `identifier byte budget counts multibyte UTF-8 exactly`() {
        RetainedIdentifierBudget(maximumBytes = 7L).apply {
            retain("a", "é")
            retain("🔐")
        }

        assertFailsWith<IllegalArgumentException> {
            RetainedIdentifierBudget(maximumBytes = 6L).apply {
                retain("a", "é")
                retain("🔐")
            }
        }
    }

    @Test
    fun `identifier byte budget rejects malformed surrogate input`() {
        assertFailsWith<IllegalArgumentException> {
            RetainedIdentifierBudget(maximumBytes = 8L).retain("invalid\uD800")
        }
    }

    private fun manifest(credentialCount: Int, folderCount: Int) = BackupStreamManifest(
        credentialCount = credentialCount,
        folderCount = folderCount,
        tagCount = 0,
        credentialFolderReferenceCount = 0,
        credentialTagReferenceCount = 0,
        attachmentCount = 0,
        managedAttachmentCount = 0,
        passwordHistoryCount = 0,
        managedAttachmentObjectBytes = 0L,
    )
}
