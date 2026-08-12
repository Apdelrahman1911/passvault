package com.passvault.android.attachment

import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
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
}
