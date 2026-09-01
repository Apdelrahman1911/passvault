package com.passvault.android.attachment

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

            cleanupAttachmentPreviewOperation(cache.toFile(), expiredId)

            assertFalse(Files.exists(expired.parent))
            assertTrue(Files.isRegularFile(active))
        } finally {
            cache.toFile().deleteRecursively()
        }
    }

    @Test
    fun durableCleanupRejectsAnArbitraryPath() {
        val cache = Files.createTempDirectory("passvault-android-attachment-cache-")
        try {
            assertFailsWith<IllegalArgumentException> {
                cleanupAttachmentPreviewOperation(cache.toFile(), "../unrelated")
            }
        } finally {
            cache.toFile().deleteRecursively()
        }
    }

    @Test
    fun durableCleanupJobContractIsBoundedAndStable() {
        assertEquals(PREVIEW_JOB_ID_MIN + 1, nextPreviewCleanupJobId(PREVIEW_JOB_ID_MIN))
        assertEquals(PREVIEW_JOB_ID_MIN, nextPreviewCleanupJobId(PREVIEW_JOB_ID_MAX))
        assertTrue(isPreviewCleanupJobId(PREVIEW_JOB_ID_MIN))
        assertTrue(isPreviewCleanupJobId(PREVIEW_JOB_ID_MAX))
        assertTrue(PREVIEW_LIFETIME_MILLISECONDS <= 60_000L)
        assertTrue(PREVIEW_CLEANUP_DEADLINE_MILLISECONDS <= 90_000L)
        assertTrue(PREVIEW_CLEANUP_DEADLINE_MILLISECONDS >= PREVIEW_LIFETIME_MILLISECONDS)
    }
}
