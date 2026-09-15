package com.passvault.desktop.security

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.AclFileAttributeView
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DesktopPrivateDirectoryTest {
    @Test
    fun `new nested directories use the host private-access mechanism`() {
        val temporaryRoot = Files.createTempDirectory("passvault-private-directory-test-")
        try {
            val parent = temporaryRoot.resolve("private-parent")
            val target = parent.resolve("private-child")

            val resolved = createOrHardenPrivateDesktopDirectory(target)

            assertEquals(target.toRealPath(), resolved)
            assertProtectedDirectory(parent)
            assertProtectedDirectory(target)
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `existing POSIX directory is repaired to owner-only access`() {
        val temporaryRoot = Files.createTempDirectory("passvault-private-directory-test-")
        val target = temporaryRoot.resolve("existing")
        try {
            Files.createDirectory(target)
            val posixView = Files.getFileAttributeView(target, PosixFileAttributeView::class.java)
                ?: return
            posixView.setPermissions(PosixFilePermission.entries.toSet())

            createOrHardenPrivateDesktopDirectory(target)

            assertEquals(OWNER_ONLY_DIRECTORY_PERMISSIONS, posixView.readAttributes().permissions())
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `symbolic-link directory is rejected`() {
        val temporaryRoot = Files.createTempDirectory("passvault-private-directory-test-")
        try {
            val destination = Files.createDirectory(temporaryRoot.resolve("destination"))
            val link = temporaryRoot.resolve("private-link")
            runCatching { Files.createSymbolicLink(link, destination) }.getOrElse { return }

            assertFailsWith<IllegalStateException> {
                createOrHardenPrivateDesktopDirectory(link)
            }
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `trusted symbolic-link ancestors are canonicalized without accepting a linked leaf`() {
        val temporaryRoot = Files.createTempDirectory("passvault-private-directory-test-")
        try {
            val realParent = Files.createDirectory(temporaryRoot.resolve("real-parent"))
            val linkedParent = temporaryRoot.resolve("linked-parent")
            runCatching { Files.createSymbolicLink(linkedParent, realParent) }.getOrElse { return }

            val resolved = createOrHardenPrivateDesktopDirectory(linkedParent.resolve("private-child"))

            assertEquals(realParent.resolve("private-child").toRealPath(), resolved)
            assertProtectedDirectory(resolved)
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    private fun assertProtectedDirectory(path: java.nio.file.Path) {
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
