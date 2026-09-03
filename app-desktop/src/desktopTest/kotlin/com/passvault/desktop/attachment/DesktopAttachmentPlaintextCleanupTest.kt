package com.passvault.desktop.attachment

import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.desktop.OperatingSystem
import com.passvault.desktop.getOperatingSystem
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.AclEntryPermission
import java.nio.file.attribute.AclEntryType
import java.nio.file.attribute.AclFileAttributeView
import java.util.EnumSet
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.writeText
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopAttachmentPlaintextCleanupTest {

    @Test
    fun `leaving the unlocked session purges handed-off previews`() = runTest {
        val temporaryRoot = Files.createTempDirectory("passvault-desktop-preview-test-")
        try {
            val manager = DesktopAttachmentPreviewManager(
                temporaryRoot = temporaryRoot,
                openPreview = {},
            )
            val states = MutableStateFlow<VaultSessionState>(
                VaultSessionState.Unlocked(SessionId("preview-session")),
            )
            bindDesktopAttachmentPreviewLifecycle(
                scope = backgroundScope,
                sessionStates = states,
                previewManager = manager,
                ioDispatcher = StandardTestDispatcher(testScheduler),
            )
            runCurrent()
            val output = manager.createOutput(metadata(), backgroundScope).getOrThrow()
            output.sink.write("secret".encodeToByteArray(), 6)
            output.sink.commit()
            output.present().getOrThrow()
            assertTrue(hasPreviewDirectory(temporaryRoot))

            states.value = VaultSessionState.Locking(LockReason.Manual)
            runCurrent()

            assertFalse(hasPreviewDirectory(temporaryRoot))
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `preview expiry is bounded to one minute`() = runTest {
        val temporaryRoot = Files.createTempDirectory("passvault-desktop-preview-test-")
        try {
            val manager = DesktopAttachmentPreviewManager(
                temporaryRoot = temporaryRoot,
                previewLifetimeMilliseconds = DESKTOP_PREVIEW_LIFETIME_MILLISECONDS,
                openPreview = {},
            )
            manager.enable()
            val output = manager.createOutput(metadata(), backgroundScope).getOrThrow()
            output.sink.write("secret".encodeToByteArray(), 6)
            output.sink.commit()
            output.present().getOrThrow()

            advanceTimeBy(DESKTOP_PREVIEW_LIFETIME_MILLISECONDS)
            runCurrent()

            assertFalse(hasPreviewDirectory(temporaryRoot))
            assertTrue(DESKTOP_PREVIEW_LIFETIME_MILLISECONDS <= 60_000L)
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `session purge closes an in-progress preview before deleting it`() = runTest {
        val temporaryRoot = Files.createTempDirectory("passvault-desktop-preview-test-")
        try {
            val manager = DesktopAttachmentPreviewManager(temporaryRoot = temporaryRoot, openPreview = {})
            manager.enable()
            val output = manager.createOutput(metadata(), backgroundScope).getOrThrow()
            output.sink.write("secret".encodeToByteArray(), 6)

            manager.disableAndPurge()

            assertFalse(hasPreviewDirectory(temporaryRoot))
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `disabled preview manager rejects new plaintext without residue`() = runTest {
        val temporaryRoot = Files.createTempDirectory("passvault-desktop-preview-test-")
        try {
            val manager = DesktopAttachmentPreviewManager(temporaryRoot = temporaryRoot, openPreview = {})

            assertTrue(manager.createOutput(metadata(), backgroundScope).isFailure)
            assertFalse(hasPreviewDirectory(temporaryRoot))
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `Windows preview paths receive one protected current-user ACL`() {
        if (getOperatingSystem() != OperatingSystem.WINDOWS) return
        val temporaryRoot = Files.createTempDirectory("passvault-desktop-preview-acl-test-")
        try {
            val directory = createPrivateDesktopPreviewDirectory(temporaryRoot.resolve("private"))
            val file = createPrivateDesktopPreviewFile(directory.resolve("preview.txt"))

            listOf(directory, file).forEach { path ->
                val view = assertNotNull(Files.getFileAttributeView(path, AclFileAttributeView::class.java))
                val entry = view.acl.single()
                assertEquals(AclEntryType.ALLOW, entry.type())
                assertEquals(EnumSet.allOf(AclEntryPermission::class.java), entry.permissions())
            }
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `Windows preview ACL grants only the current SID and protects inheritance`() {
        val sid = "S-1-5-21-1000"

        assertEquals("D:P(A;OICI;FA;;;$sid)", windowsPreviewAclSddl(sid, isDirectory = true))
        assertEquals("D:P(A;;FA;;;$sid)", windowsPreviewAclSddl(sid, isDirectory = false))
        assertFailsWith<IllegalArgumentException> {
            windowsPreviewAclSddl("S-1-5-21);(A;;FA;;;WD", isDirectory = false)
        }
    }

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

    private fun hasPreviewDirectory(temporaryRoot: java.nio.file.Path): Boolean =
        Files.newDirectoryStream(temporaryRoot).use { entries ->
            entries.any { it.fileName.toString().startsWith("passvault-attachment-preview-") }
        }

    private fun metadata() = AttachmentMetadata(
        id = AttachmentId("attachment-preview-test"),
        fileName = "secret.txt",
        mimeType = "text/plain",
        sizeBytes = 6,
        createdAt = Instant.fromEpochMilliseconds(1),
    )
}
