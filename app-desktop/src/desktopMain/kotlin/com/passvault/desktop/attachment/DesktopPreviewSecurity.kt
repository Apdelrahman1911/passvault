package com.passvault.desktop.attachment

import com.passvault.desktop.OperatingSystem
import com.passvault.desktop.getOperatingSystem
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import com.sun.jna.ptr.ShortByReference
import com.sun.jna.win32.StdCallLibrary
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

internal fun createPrivateDesktopPreviewDirectory(path: Path): Path {
    val parent = requireNotNull(path.parent)
    val supportsPosix = Files.getFileAttributeView(
        parent,
        PosixFileAttributeView::class.java,
        LinkOption.NOFOLLOW_LINKS,
    ) != null
    val created = if (supportsPosix) {
        Files.createDirectory(
            path,
            PosixFilePermissions.asFileAttribute(OWNER_ONLY_DIRECTORY_PERMISSIONS),
        )
    } else {
        Files.createDirectory(path)
    }
    return protectCreatedPath(created, ::protectDesktopPreviewDirectory)
}

internal fun createPrivateDesktopPreviewFile(path: Path): Path {
    val parent = requireNotNull(path.parent)
    val supportsPosix = Files.getFileAttributeView(
        parent,
        PosixFileAttributeView::class.java,
        LinkOption.NOFOLLOW_LINKS,
    ) != null
    val created = if (supportsPosix) {
        Files.createFile(
            path,
            PosixFilePermissions.asFileAttribute(OWNER_ONLY_FILE_PERMISSIONS),
        )
    } else {
        Files.createFile(path)
    }
    return protectCreatedPath(created, ::protectDesktopPreviewFile)
}

private fun protectCreatedPath(path: Path, protect: (Path) -> Unit): Path = try {
    protect(path)
    path
} catch (error: IOException) {
    removeUnprotectedPath(path, error)
} catch (error: IllegalStateException) {
    removeUnprotectedPath(path, error)
} catch (error: SecurityException) {
    removeUnprotectedPath(path, error)
}

private fun removeUnprotectedPath(path: Path, error: Exception): Nothing {
    runCatching { Files.deleteIfExists(path) }
        .exceptionOrNull()
        ?.let(error::addSuppressed)
    throw error
}

internal fun protectDesktopPreviewDirectory(path: Path) {
    protectDesktopPreviewPath(path, OWNER_ONLY_DIRECTORY_PERMISSIONS, isDirectory = true)
}

internal fun protectDesktopPreviewFile(path: Path) {
    protectDesktopPreviewPath(path, OWNER_ONLY_FILE_PERMISSIONS, isDirectory = false)
}

private fun protectDesktopPreviewPath(
    path: Path,
    posixPermissions: Set<PosixFilePermission>,
    isDirectory: Boolean,
) {
    val posixView = Files.getFileAttributeView(
        path,
        PosixFileAttributeView::class.java,
        LinkOption.NOFOLLOW_LINKS,
    )
    if (posixView != null) {
        posixView.setPermissions(posixPermissions)
        return
    }
    check(getOperatingSystem() == OperatingSystem.WINDOWS) {
        "The Desktop attachment preview filesystem cannot enforce owner-only access"
    }
    try {
        WindowsPreviewAcl.protect(path, isDirectory)
    } catch (error: LinkageError) {
        throw IllegalStateException("The Windows preview DACL provider is unavailable", error)
    }
}

/** Applies a protected DACL containing only the current process TokenUser SID. */
private object WindowsPreviewAcl {
    private val securityApi: WindowsSecurityApi by lazy {
        Native.load("Advapi32", WindowsSecurityApi::class.java)
    }
    private val kernelApi: WindowsKernelApi by lazy {
        Native.load("Kernel32", WindowsKernelApi::class.java)
    }

    fun protect(path: Path, isDirectory: Boolean) {
        val tokenReference = PointerByReference()
        check(
            securityApi.OpenProcessToken(
                kernelApi.GetCurrentProcess(),
                TOKEN_QUERY,
                tokenReference,
            ),
        ) { "The current Windows user token could not be opened" }
        val token = checkNotNull(tokenReference.value)
        try {
            withTokenUserSid(token) { sid ->
                val sidText = sid.toWindowsSidString()
                val descriptor = createSecurityDescriptor(windowsPreviewAclSddl(sidText, isDirectory))
                try {
                    applyDescriptor(path, descriptor)
                } finally {
                    kernelApi.LocalFree(descriptor)
                }
            }
        } finally {
            check(kernelApi.CloseHandle(token)) { "The Windows user token could not be closed" }
        }
        check(isProtected(path)) { "The Desktop attachment preview DACL was not protected" }
    }

    private inline fun withTokenUserSid(token: Pointer, block: (Pointer) -> Unit) {
        val requiredSize = IntByReference()
        securityApi.GetTokenInformation(token, TOKEN_USER, null, 0, requiredSize)
        check(requiredSize.value >= Native.POINTER_SIZE) { "The Windows user token is unavailable" }
        val tokenInformation = Memory(requiredSize.value.toLong())
        try {
            check(
                securityApi.GetTokenInformation(
                    token,
                    TOKEN_USER,
                    tokenInformation,
                    requiredSize.value,
                    requiredSize,
                ),
            ) { "The Windows user token could not be read" }
            val sid = tokenInformation.getPointer(0L)
            check(sid != null && securityApi.IsValidSid(sid)) { "The Windows user SID is invalid" }
            block(sid)
        } finally {
            tokenInformation.clear()
            tokenInformation.close()
        }
    }

    private fun Pointer.toWindowsSidString(): String {
        val value = PointerByReference()
        check(securityApi.ConvertSidToStringSidW(this, value)) {
            "The Windows user SID could not be encoded"
        }
        val encoded = checkNotNull(value.value)
        return try {
            encoded.getWideString(0L)
        } finally {
            kernelApi.LocalFree(encoded)
        }
    }

    private fun createSecurityDescriptor(sddl: String): Pointer {
        val descriptor = PointerByReference()
        check(
            securityApi.ConvertStringSecurityDescriptorToSecurityDescriptorW(
                WString(sddl),
                SDDL_REVISION_1,
                descriptor,
                null,
            ),
        ) { "The Windows preview security descriptor could not be created" }
        return checkNotNull(descriptor.value)
    }

    private fun applyDescriptor(path: Path, descriptor: Pointer) {
        val present = IntByReference()
        val defaulted = IntByReference()
        val dacl = PointerByReference()
        check(securityApi.GetSecurityDescriptorDacl(descriptor, present, dacl, defaulted)) {
            "The Windows preview DACL could not be read"
        }
        check(present.value != 0 && dacl.value != null) { "The Windows preview DACL is missing" }
        val result = securityApi.SetNamedSecurityInfoW(
            WString(path.toAbsolutePath().normalize().toString()),
            SE_FILE_OBJECT,
            DACL_SECURITY_INFORMATION or PROTECTED_DACL_SECURITY_INFORMATION,
            null,
            null,
            dacl.value,
            null,
        )
        check(result == ERROR_SUCCESS) { "The Windows preview DACL could not be applied (error $result)" }
    }

    private fun isProtected(path: Path): Boolean {
        val descriptor = PointerByReference()
        val dacl = PointerByReference()
        val result = securityApi.GetNamedSecurityInfoW(
            WString(path.toAbsolutePath().normalize().toString()),
            SE_FILE_OBJECT,
            DACL_SECURITY_INFORMATION,
            null,
            null,
            dacl,
            null,
            descriptor,
        )
        if (result != ERROR_SUCCESS || descriptor.value == null) return false
        return try {
            val control = ShortByReference()
            val revision = IntByReference()
            securityApi.GetSecurityDescriptorControl(descriptor.value, control, revision) &&
                (control.value.toInt() and SE_DACL_PROTECTED) != 0
        } finally {
            kernelApi.LocalFree(descriptor.value)
        }
    }
}

internal fun windowsPreviewAclSddl(userSid: String, isDirectory: Boolean): String {
    require(WINDOWS_SID.matches(userSid)) { "The Windows user SID is malformed" }
    val inheritance = if (isDirectory) "OICI" else ""
    return "D:P(A;$inheritance;FA;;;$userSid)"
}

/** Win32 ABI names are fixed by the operating-system API. */
@Suppress("FunctionNaming", "LongParameterList")
private interface WindowsSecurityApi : StdCallLibrary {
    fun OpenProcessToken(process: Pointer, desiredAccess: Int, token: PointerByReference): Boolean
    fun GetTokenInformation(
        token: Pointer,
        informationClass: Int,
        information: Pointer?,
        informationLength: Int,
        returnLength: IntByReference,
    ): Boolean
    fun IsValidSid(sid: Pointer): Boolean
    fun ConvertSidToStringSidW(sid: Pointer, encoded: PointerByReference): Boolean
    fun ConvertStringSecurityDescriptorToSecurityDescriptorW(
        descriptor: WString,
        revision: Int,
        output: PointerByReference,
        outputSize: IntByReference?,
    ): Boolean
    fun GetSecurityDescriptorDacl(
        descriptor: Pointer,
        present: IntByReference,
        dacl: PointerByReference,
        defaulted: IntByReference,
    ): Boolean
    fun SetNamedSecurityInfoW(
        objectName: WString,
        objectType: Int,
        securityInformation: Int,
        owner: Pointer?,
        group: Pointer?,
        dacl: Pointer?,
        sacl: Pointer?,
    ): Int
    fun GetNamedSecurityInfoW(
        objectName: WString,
        objectType: Int,
        securityInformation: Int,
        owner: PointerByReference?,
        group: PointerByReference?,
        dacl: PointerByReference?,
        sacl: PointerByReference?,
        securityDescriptor: PointerByReference,
    ): Int
    fun GetSecurityDescriptorControl(
        descriptor: Pointer,
        control: ShortByReference,
        revision: IntByReference,
    ): Boolean
}

/** Win32 ABI names are fixed by the operating-system API. */
@Suppress("FunctionNaming")
private interface WindowsKernelApi : StdCallLibrary {
    fun GetCurrentProcess(): Pointer
    fun CloseHandle(handle: Pointer): Boolean
    fun LocalFree(memory: Pointer): Pointer?
}

private const val TOKEN_QUERY = 0x0008
private const val TOKEN_USER = 1
private const val SDDL_REVISION_1 = 1
private const val SE_FILE_OBJECT = 1
private const val ERROR_SUCCESS = 0
private const val DACL_SECURITY_INFORMATION = 0x00000004
private const val PROTECTED_DACL_SECURITY_INFORMATION = 0x80000000.toInt()
private const val SE_DACL_PROTECTED = 0x1000
private val WINDOWS_SID = Regex("S-[0-9]+-(?:[0-9]+|0x[0-9A-Fa-f]+)(?:-[0-9]+)*")

private val OWNER_ONLY_DIRECTORY_PERMISSIONS = setOf(
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE,
    PosixFilePermission.OWNER_EXECUTE,
)
private val OWNER_ONLY_FILE_PERMISSIONS = setOf(
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE,
)
