package com.passvault.shared.di

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.AclFileAttributeView
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AttachmentStorageProviderDesktopTest {
    @Test
    fun `attachment storage children use the host private-access mechanism`() = runTest {
        val temporaryRoot = Files.createTempDirectory("passvault-attachment-storage-test-")
        try {
            val dataRoot = temporaryRoot.resolve(".passvault")

            val store = createDesktopAttachmentBlobStore(dataRoot)
            assertFalse(store.exists("objects/00000000-0000-4000-8000-000000000001.pva"))

            listOf(
                dataRoot,
                dataRoot.resolve("attachments"),
                dataRoot.resolve("attachments/objects"),
                dataRoot.resolve("attachments/staging"),
            ).forEach(::assertProtectedDirectory)
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `existing POSIX attachment children are repaired to owner-only access`() {
        val temporaryRoot = Files.createTempDirectory("passvault-attachment-storage-test-")
        val dataRoot = temporaryRoot.resolve(".passvault")
        val objects = dataRoot.resolve("attachments/objects")
        val staging = dataRoot.resolve("attachments/staging")
        try {
            Files.createDirectories(objects)
            Files.createDirectories(staging)
            val objectView = Files.getFileAttributeView(objects, PosixFileAttributeView::class.java) ?: return
            val stagingView = Files.getFileAttributeView(staging, PosixFileAttributeView::class.java) ?: return
            objectView.setPermissions(PosixFilePermission.entries.toSet())
            stagingView.setPermissions(PosixFilePermission.entries.toSet())

            createDesktopAttachmentBlobStore(dataRoot)

            assertEquals(OWNER_ONLY_DIRECTORY_PERMISSIONS, objectView.readAttributes().permissions())
            assertEquals(OWNER_ONLY_DIRECTORY_PERMISSIONS, stagingView.readAttributes().permissions())
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    private fun assertProtectedDirectory(path: Path) {
        assertTrue(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
        val posixView = Files.getFileAttributeView(
            path,
            PosixFileAttributeView::class.java,
            LinkOption.NOFOLLOW_LINKS,
        )
        if (posixView != null) {
            assertEquals(OWNER_ONLY_DIRECTORY_PERMISSIONS, posixView.readAttributes().permissions())
        } else {
            val aclView = assertNotNull(
                Files.getFileAttributeView(
                    path,
                    AclFileAttributeView::class.java,
                    LinkOption.NOFOLLOW_LINKS,
                ),
            )
            assertTrue(aclView.acl.isNotEmpty())
        }
    }

    private companion object {
        val OWNER_ONLY_DIRECTORY_PERMISSIONS = setOf(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE,
        )
    }
}
