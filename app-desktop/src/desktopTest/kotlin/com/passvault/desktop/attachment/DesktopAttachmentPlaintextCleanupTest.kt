package com.passvault.desktop.attachment

import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopAttachmentPlaintextCleanupTest {

    @Test
    fun abandonedPreviewCleanupDoesNotFollowNestedSymbolicLinks() {
        val temporaryRoot = Files.createTempDirectory("passvault-desktop-preview-test-")
        val outside = Files.createTempDirectory("passvault-desktop-preview-outside-")
        try {
            val candidate = temporaryRoot.resolve(
                "passvault-attachment-preview-00000000-0000-0000-0000-000000000001",
            )
            candidate.createDirectories()
            candidate.resolve(".passvault-preview.lock").createFile()
            val content = candidate.resolve("content").createDirectories()
            content.resolve("preview.txt").writeText("secret")
            val outsideFile = outside.resolve("must-remain.txt")
            outsideFile.writeText("keep")
            Files.createSymbolicLink(content.resolve("outside-link"), outside)

            cleanupAbandonedDesktopAttachmentPreviews(temporaryRoot)

            assertFalse(Files.exists(candidate))
            assertTrue(Files.isRegularFile(outsideFile))
        } finally {
            temporaryRoot.toFile().deleteRecursively()
            outside.toFile().deleteRecursively()
        }
    }

    @Test
    fun cleanupSkipsPreviewWhoseLiveProcessLockIsHeld() {
        val temporaryRoot = Files.createTempDirectory("passvault-desktop-preview-test-")
        try {
            val candidate = temporaryRoot.resolve(
                "passvault-attachment-preview-00000000-0000-0000-0000-000000000002",
            )
            candidate.createDirectories()
            val lockPath = candidate.resolve(".passvault-preview.lock").createFile()
            FileChannel.open(lockPath, StandardOpenOption.WRITE).use { channel ->
                channel.lock().use {
                    cleanupAbandonedDesktopAttachmentPreviews(temporaryRoot)
                    assertTrue(Files.isDirectory(candidate))
                }
            }

            cleanupAbandonedDesktopAttachmentPreviews(temporaryRoot)
            assertFalse(Files.exists(candidate))
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun cleanupDoesNotDeleteAnUnownedPrefixLookalike() {
        val temporaryRoot = Files.createTempDirectory("passvault-desktop-preview-test-")
        try {
            val lookalike = temporaryRoot.resolve("passvault-attachment-preview-not-owned")
            lookalike.createDirectories()
            lookalike.resolve(".passvault-preview.lock").createFile()
            lookalike.resolve("must-remain.txt").writeText("keep")

            cleanupAbandonedDesktopAttachmentPreviews(temporaryRoot)

            assertTrue(Files.isRegularFile(lookalike.resolve("must-remain.txt")))
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun cleanupDoesNotDeleteAValidOwnedNameWithUnknownRootEntries() {
        val temporaryRoot = Files.createTempDirectory("passvault-desktop-preview-test-")
        try {
            val candidate = temporaryRoot.resolve(
                "passvault-attachment-preview-00000000-0000-0000-0000-000000000004",
            )
            candidate.createDirectories()
            candidate.resolve(".passvault-preview.lock").createFile()
            candidate.resolve("must-remain.txt").writeText("keep")

            cleanupAbandonedDesktopAttachmentPreviews(temporaryRoot)

            assertTrue(Files.isRegularFile(candidate.resolve("must-remain.txt")))
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun cleanupRemovesAnInterruptedOwnedDirectoryBeforeLockCreation() {
        val temporaryRoot = Files.createTempDirectory("passvault-desktop-preview-test-")
        try {
            val candidate = temporaryRoot.resolve(
                "passvault-attachment-preview-00000000-0000-0000-0000-000000000003",
            )
            candidate.createDirectories()

            cleanupAbandonedDesktopAttachmentPreviews(temporaryRoot)

            assertFalse(Files.exists(candidate))
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }
}
