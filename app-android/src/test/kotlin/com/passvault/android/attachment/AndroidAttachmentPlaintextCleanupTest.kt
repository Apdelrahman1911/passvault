package com.passvault.android.attachment

import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidAttachmentPlaintextCleanupTest {

    @Test
    fun exportLeasePersistsCleanupBeforeCreatingPlaintext() {
        val cache = Files.createTempDirectory("passvault-android-attachment-cache-")
        val operationId = "f100b9e4-bbde-469f-a10b-6b55d61570a0"
        var scheduled = false
        try {
            val lease = createAttachmentPlaintextLease(
                cacheDirectory = cache.toFile(),
                cacheRoot = AttachmentPlaintextCacheRoot.EXPORT,
                fileName = "document.txt",
                operationId = operationId,
                scheduleCleanup = { root, id ->
                    assertEquals(AttachmentPlaintextCacheRoot.EXPORT, root)
                    assertEquals(operationId, id)
                    assertFalse(Files.exists(cache.resolve(root.directoryName).resolve(id)))
                    scheduled = true
                    42
                },
                cancelCleanup = { error("A valid lease must not be cancelled during creation") },
            )
            assertTrue(scheduled)
            lease.temporary.writeText("plaintext")

            cleanupAttachmentPlaintextOperation(
                cache.toFile(),
                AttachmentPlaintextCacheRoot.EXPORT,
                operationId,
            )

            assertFalse(lease.directory.exists())
        } finally {
            cache.toFile().deleteRecursively()
        }
    }

    @Test
    fun exportLeaseFailsClosedBeforeCreatingAFileWhenSchedulingFails() {
        val cache = Files.createTempDirectory("passvault-android-attachment-cache-")
        try {
            assertFailsWith<IllegalStateException> {
                createAttachmentPlaintextLease(
                    cacheDirectory = cache.toFile(),
                    cacheRoot = AttachmentPlaintextCacheRoot.EXPORT,
                    fileName = "document.txt",
                    operationId = "f100b9e4-bbde-469f-a10b-6b55d61570a0",
                    scheduleCleanup = { _, _ -> null },
                    cancelCleanup = { error("An unaccepted job cannot be cancelled") },
                )
            }
            assertFalse(Files.exists(cache.resolve("attachment-exports")))
        } finally {
            cache.toFile().deleteRecursively()
        }
    }

    @Test
    fun startupCleanupRemovesOnlyOwnedAttachmentPlaintextRoots() {
        val cache = Files.createTempDirectory("passvault-android-attachment-cache-")
        try {
            val preview = cache.resolve("attachment-previews/operation/document.txt")
            val export = cache.resolve("attachment-exports/operation/document.txt")
            val unrelated = cache.resolve("unrelated.txt")
            preview.parent.createDirectories()
            export.parent.createDirectories()
            preview.writeText("preview")
            export.writeText("export")
            unrelated.writeText("keep")

            cleanupAttachmentPlaintextCache(cache.toFile())

            assertFalse(Files.exists(cache.resolve("attachment-previews")))
            assertFalse(Files.exists(cache.resolve("attachment-exports")))
            assertTrue(Files.isRegularFile(unrelated))
        } finally {
            cache.toFile().deleteRecursively()
        }
    }

    @Test
    fun startupCleanupDeletesCacheSymlinkWithoutFollowingIt() {
        val cache = Files.createTempDirectory("passvault-android-attachment-cache-")
        val outside = Files.createTempDirectory("passvault-android-attachment-outside-")
        try {
            val outsideFile = outside.resolve("must-remain.txt")
            outsideFile.writeText("keep")
            Files.createSymbolicLink(cache.resolve("attachment-previews"), outside)

            cleanupAttachmentPlaintextCache(cache.toFile())

            assertFalse(Files.exists(cache.resolve("attachment-previews")))
            assertTrue(Files.isRegularFile(outsideFile))
        } finally {
            cache.toFile().deleteRecursively()
            outside.toFile().deleteRecursively()
        }
    }

    @Test
    fun durableCleanupDeletesOnlyItsPreviewLease() {
        val cache = Files.createTempDirectory("passvault-android-attachment-cache-")
        val expiredId = "f100b9e4-bbde-469f-a10b-6b55d61570a0"
        val activeId = "93cbfb73-cc6f-49d1-8782-899d3044b2a4"
        try {
            val expired = cache.resolve("attachment-previews/$expiredId/document.txt")
            val active = cache.resolve("attachment-previews/$activeId/document.txt")
            expired.parent.createDirectories()
            active.parent.createDirectories()
            expired.writeText("expired plaintext")
            active.writeText("active plaintext")

            cleanupAttachmentPlaintextOperation(
                cache.toFile(),
                AttachmentPlaintextCacheRoot.PREVIEW,
                expiredId,
            )

            assertFalse(Files.exists(expired.parent))
            assertTrue(Files.isRegularFile(active))
        } finally {
            cache.toFile().deleteRecursively()
        }
    }

    @Test
    fun durableCleanupDeletesOnlyItsExportLease() {
        val cache = Files.createTempDirectory("passvault-android-attachment-cache-")
        val expiredId = "f100b9e4-bbde-469f-a10b-6b55d61570a0"
        val activeId = "93cbfb73-cc6f-49d1-8782-899d3044b2a4"
        try {
            val expired = cache.resolve("attachment-exports/$expiredId/document.txt")
            val active = cache.resolve("attachment-exports/$activeId/document.txt")
            val preview = cache.resolve("attachment-previews/$expiredId/document.txt")
            expired.parent.createDirectories()
            active.parent.createDirectories()
            preview.parent.createDirectories()
            expired.writeText("expired plaintext")
            active.writeText("active plaintext")
            preview.writeText("preview plaintext")

            cleanupAttachmentPlaintextOperation(
                cache.toFile(),
                AttachmentPlaintextCacheRoot.EXPORT,
                expiredId,
            )

            assertFalse(Files.exists(expired.parent))
            assertTrue(Files.isRegularFile(active))
            assertTrue(Files.isRegularFile(preview))
        } finally {
            cache.toFile().deleteRecursively()
        }
    }

    @Test
    fun durableCleanupRejectsAnArbitraryPath() {
        val cache = Files.createTempDirectory("passvault-android-attachment-cache-")
        try {
            assertFailsWith<IllegalArgumentException> {
                cleanupAttachmentPlaintextOperation(
                    cache.toFile(),
                    AttachmentPlaintextCacheRoot.EXPORT,
                    "../unrelated",
                )
            }
        } finally {
            cache.toFile().deleteRecursively()
        }
    }

    @Test
    fun durableCleanupJobContractIsBoundedAndStable() {
        assertEquals(PLAINTEXT_JOB_ID_MIN + 1, nextAttachmentPlaintextCleanupJobId(PLAINTEXT_JOB_ID_MIN))
        assertEquals(PLAINTEXT_JOB_ID_MIN, nextAttachmentPlaintextCleanupJobId(PLAINTEXT_JOB_ID_MAX))
        assertTrue(isAttachmentPlaintextCleanupJobId(PLAINTEXT_JOB_ID_MIN))
        assertTrue(isAttachmentPlaintextCleanupJobId(PLAINTEXT_JOB_ID_MAX))
        assertTrue(PREVIEW_LIFETIME_MILLISECONDS <= 60_000L)
        assertTrue(PREVIEW_CLEANUP_DEADLINE_MILLISECONDS <= 90_000L)
        assertTrue(PREVIEW_CLEANUP_DEADLINE_MILLISECONDS >= PREVIEW_LIFETIME_MILLISECONDS)
        assertTrue(EXPORT_STAGING_LIFETIME_MILLISECONDS <= 60_000L)
        assertTrue(EXPORT_STAGING_CLEANUP_DEADLINE_MILLISECONDS <= 90_000L)
        assertTrue(EXPORT_STAGING_CLEANUP_DEADLINE_MILLISECONDS >= EXPORT_STAGING_LIFETIME_MILLISECONDS)
        assertEquals(
            AttachmentPlaintextCacheRoot.PREVIEW,
            persistedAttachmentPlaintextRoot(null),
        )
        assertEquals(
            AttachmentPlaintextCacheRoot.EXPORT,
            persistedAttachmentPlaintextRoot("export"),
        )
        assertEquals(null, persistedAttachmentPlaintextRoot("../../outside"))
    }

    @Test
    fun vaultLockStatesRequestPlaintextCleanup() {
        assertFalse(shouldCleanupAttachmentPlaintext(VaultSessionState.Unlocked(SessionId("session"))))
        assertTrue(shouldCleanupAttachmentPlaintext(VaultSessionState.Locking(LockReason.Manual)))
        assertTrue(shouldCleanupAttachmentPlaintext(VaultSessionState.Locked(LockReason.Manual)))
        assertTrue(shouldCleanupAttachmentPlaintext(VaultSessionState.Unlocking))
        assertTrue(shouldCleanupAttachmentPlaintext(VaultSessionState.Uninitialized))
    }
}
