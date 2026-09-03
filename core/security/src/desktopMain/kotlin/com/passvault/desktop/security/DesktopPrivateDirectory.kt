package com.passvault.desktop.security

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.AclFileAttributeView
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

/**
 * Creates a Desktop-private directory without a permissive POSIX creation window.
 *
 * Existing POSIX directories are repaired to owner-only access. Windows keeps the
 * parent ACL inherited atomically by CreateDirectory; replacing that ACL
 * would also discard required SYSTEM and administrator access. Filesystems that
 * expose neither protection model are rejected instead of silently continuing.
 */
fun createOrHardenPrivateDesktopDirectory(path: Path): Path {
    val normalized = path.toAbsolutePath().normalize()
    val parent = checkNotNull(normalized.parent) {
        "A filesystem root cannot be used as a Desktop private directory"
    }
    check(!Files.isSymbolicLink(normalized)) {
        "A Desktop private directory must not be a symbolic link"
    }

    if ("posix" in normalized.fileSystem.supportedFileAttributeViews()) {
        Files.createDirectories(
            normalized,
            PosixFilePermissions.asFileAttribute(OWNER_ONLY_DIRECTORY_PERMISSIONS),
        )
    } else {
        Files.createDirectories(normalized)
    }
    check(Files.isDirectory(normalized, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(normalized)) {
        "A Desktop private path must be a real directory"
    }

    // Canonicalize trusted ancestors so consumers such as SQLite do not inherit a
    // symlinked-home component, while continuing to reject rather than follow the leaf.
    val resolved = parent.toRealPath().resolve(normalized.fileName)
    check(Files.isDirectory(resolved, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(resolved)) {
        "A Desktop private path must remain a real directory"
    }
    val posixView = Files.getFileAttributeView(
        resolved,
        PosixFileAttributeView::class.java,
        LinkOption.NOFOLLOW_LINKS,
    )
    if (posixView != null) {
        posixView.setPermissions(OWNER_ONLY_DIRECTORY_PERMISSIONS)
        check(posixView.readAttributes().permissions() == OWNER_ONLY_DIRECTORY_PERMISSIONS) {
            "A Desktop private directory must have owner-only POSIX permissions"
        }
    } else {
        val aclView = Files.getFileAttributeView(
            resolved,
            AclFileAttributeView::class.java,
            LinkOption.NOFOLLOW_LINKS,
        )
        check(aclView != null && aclView.acl.isNotEmpty()) {
            "A Desktop private directory must inherit a non-empty filesystem ACL"
        }
    }
    return resolved
}

private val OWNER_ONLY_DIRECTORY_PERMISSIONS = setOf(
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE,
    PosixFilePermission.OWNER_EXECUTE,
)
