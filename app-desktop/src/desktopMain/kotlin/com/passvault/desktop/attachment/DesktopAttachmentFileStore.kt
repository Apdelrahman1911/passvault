package com.passvault.desktop.attachment

import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentException
import com.passvault.core.domain.repository.AttachmentPolicy
import com.passvault.feature.credential.AttachmentFileSelectionCancelled
import com.passvault.feature.credential.AttachmentFileStore
import com.passvault.feature.credential.AttachmentOutputAction
import com.passvault.feature.credential.PreparedAttachmentOutput
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.awt.KeyboardFocusManager
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Files
import java.nio.file.FileVisitResult
import java.nio.file.FileAlreadyExistsException
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.attribute.UserPrincipal
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

class DesktopAttachmentFileStore : AttachmentFileStore {
    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        cleanupScope.launch { runCatching { cleanupAbandonedDesktopAttachmentPreviews() } }
    }

    override suspend fun selectForImport(): Result<AttachmentContentSource> = try {
        val selected = withContext(Dispatchers.Swing) { chooseFile(FileDialog.LOAD, null) }
            ?: return Result.failure(AttachmentFileSelectionCancelled())
        val path = withContext(Dispatchers.IO) {
            Path.of(selected).toAbsolutePath().normalize().also(::requireSafeSource)
        }
        Result.success(DesktopAttachmentSource(path))
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (error: AttachmentException) {
        Result.failure(error)
    } catch (_: Exception) {
        Result.failure(IllegalStateException("The attachment file could not be opened"))
    }

    override suspend fun createOutput(
        attachment: AttachmentMetadata,
        action: AttachmentOutputAction,
    ): Result<PreparedAttachmentOutput> = try {
        AttachmentPolicy.validateFileName(attachment.fileName)
        when (action) {
            AttachmentOutputAction.EXPORT -> createExportSink(attachment)
            AttachmentOutputAction.OPEN -> createOpenSink(attachment)
        }
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (error: AttachmentException) {
        Result.failure(error)
    } catch (_: Exception) {
        Result.failure(IllegalStateException("The attachment output could not be prepared"))
    }

    private suspend fun createExportSink(attachment: AttachmentMetadata): Result<PreparedAttachmentOutput> {
        val selected = withContext(Dispatchers.Swing) {
            chooseFile(FileDialog.SAVE, attachment.fileName)
        } ?: return Result.failure(AttachmentFileSelectionCancelled())
        return withContext(Dispatchers.IO) {
            val target = Path.of(selected).toAbsolutePath().normalize()
            require(target.fileName.toString().isNotBlank())
            require(!Files.isSymbolicLink(target))
            val existing = readAttributesIfPresent(target)
            require(existing == null || existing.isRegularFile)
            val parent = requireNotNull(target.parent)
            require(Files.isDirectory(parent, LinkOption.NOFOLLOW_LINKS))
            val realParent = parent.toRealPath()
            require(Files.isDirectory(realParent, LinkOption.NOFOLLOW_LINKS))
            val realTarget = realParent.resolve(target.fileName.toString())
            require(!Files.isSymbolicLink(realTarget))
            val temporary = Files.createTempFile(realParent, ".passvault-attachment-", ".tmp")
            protectTemporaryFile(temporary)
            Result.success(
                DesktopPreparedAttachmentOutput(
                    temporary = temporary,
                    target = realTarget,
                    openAfterCommit = false,
                    cleanupScope = cleanupScope,
                ),
            )
        }
    }

    private suspend fun createOpenSink(attachment: AttachmentMetadata): Result<PreparedAttachmentOutput> =
        withContext(Dispatchers.IO) {
            require(Desktop.isDesktopSupported())
            require(Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
            createDesktopPreviewOutput(attachment, cleanupScope)
        }

    private fun chooseFile(mode: Int, suggestedName: String?): String? {
        val dialog = FileDialog(activeOwnerFrame(), null, mode)
        dialog.file = suggestedName
        dialog.isMultipleMode = false
        return try {
            dialog.isVisible = true
            val directory = dialog.directory
            val file = dialog.file
            if (directory == null || file == null) null else Path.of(directory, file).toString()
        } finally {
            dialog.dispose()
        }
    }

    private fun activeOwnerFrame(): Frame? =
        KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow as? Frame
            ?: Frame.getFrames().firstOrNull { it.isActive }

    private fun requireSafeSource(path: Path) {
        require(!Files.isSymbolicLink(path))
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
        AttachmentPolicy.validateFileName(path.fileName.toString())
        AttachmentPolicy.validateFileSize(Files.size(path))
    }

}

private class DesktopAttachmentSource(
    private val path: Path,
) : AttachmentContentSource {
    private var input: InputStream? = null
    private val attributes = Files.readAttributes(
        path,
        BasicFileAttributes::class.java,
        LinkOption.NOFOLLOW_LINKS,
    )

    init {
        require(attributes.isRegularFile)
    }

    override val displayName: String = AttachmentPolicy.validateFileName(path.fileName.toString())
    override val claimedMimeType: String? = null
    override val declaredSizeBytes: Long = attributes.size().also(AttachmentPolicy::validateFileSize)

    override suspend fun read(buffer: ByteArray): Int = withContext(Dispatchers.IO) {
        check(!Files.isSymbolicLink(path))
        val stream = input ?: BufferedInputStream(
            Channels.newInputStream(
                Files.newByteChannel(
                    path,
                    setOf(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS),
                ),
            ),
        ).also { input = it }
        stream.read(buffer)
    }

    override suspend fun close() = withContext(NonCancellable + Dispatchers.IO) {
        input?.close()
        input = null
    }
}

private class DesktopPreparedAttachmentOutput(
    private val temporary: Path,
    private val target: Path,
    private val openAfterCommit: Boolean,
    private val cleanupScope: CoroutineScope,
    private val previewRoot: Path? = null,
    private val previewLock: FileLock? = null,
    private val previewLockChannel: FileChannel? = null,
) : PreparedAttachmentOutput, AttachmentContentSink {
    private var output: OutputStream? = null
    private var bytesWritten = 0L
    private var sinkCommitted = false
    private var handedOff = false

    override val sink: AttachmentContentSink
        get() = this

    override suspend fun write(buffer: ByteArray, byteCount: Int) = withContext(Dispatchers.IO) {
        check(!sinkCommitted)
        require(byteCount in 0..buffer.size)
        bytesWritten += byteCount
        require(bytesWritten <= AttachmentPolicy.MAX_FILE_SIZE_BYTES)
        val stream = output ?: BufferedOutputStream(Files.newOutputStream(temporary)).also { output = it }
        stream.write(buffer, 0, byteCount)
    }

    override suspend fun commit() = withContext(NonCancellable + Dispatchers.IO) {
        check(!sinkCommitted)
        output?.flush()
        output?.close()
        output = null
        if (openAfterCommit) {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE)
        } else {
            val parent = requireNotNull(target.parent)
            require(parent.toRealPath(LinkOption.NOFOLLOW_LINKS) == parent)
            require(!Files.isSymbolicLink(target))
            Files.move(
                temporary,
                target,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
        sinkCommitted = true
    }

    @Suppress("TooGenericExceptionCaught") // Normalize native Desktop viewer failures at the platform boundary.
    override suspend fun present(): Result<Unit> = try {
        check(sinkCommitted)
        check(!handedOff)
        if (openAfterCommit) {
            withContext(Dispatchers.IO) { Desktop.getDesktop().open(target.toFile()) }
            registerPreviewDeleteOnExit()
            cleanupScope.launch {
                delay(PREVIEW_LIFETIME_MILLISECONDS)
                runCatching { deletePreviewOutput() }
            }
        }
        handedOff = true
        Result.success(Unit)
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun abort() = withContext(NonCancellable + Dispatchers.IO) {
        if (handedOff) return@withContext
        runCatching { output?.close() }
        output = null
        Files.deleteIfExists(temporary)
        if (openAfterCommit) {
            deletePreviewOutput()
        }
    }

    private fun registerPreviewDeleteOnExit() {
        val root = requireNotNull(previewRoot)
        root.toFile().deleteOnExit()
        target.parent.toFile().deleteOnExit()
        root.resolve(PREVIEW_LOCK_FILE).toFile().deleteOnExit()
        target.toFile().deleteOnExit()
    }

    private fun deletePreviewOutput() {
        val root = requireNotNull(previewRoot)
        runCatching { previewLock?.release() }
        runCatching { previewLockChannel?.close() }
        deleteDesktopPreviewTree(root)
    }

    private companion object {
        const val PREVIEW_LIFETIME_MILLISECONDS = 10L * 60L * 1000L
    }
}

@Suppress("TooGenericExceptionCaught") // Every partial filesystem allocation must release its lock and plaintext.
private fun createDesktopPreviewOutput(
    attachment: AttachmentMetadata,
    cleanupScope: CoroutineScope,
): Result<PreparedAttachmentOutput> {
    val root = createPrivatePreviewRoot()
    var lockChannel: FileChannel? = null
    var lock: FileLock? = null
    return try {
        protectPrivateDirectory(root)
        val content = Files.createDirectory(root.resolve(PREVIEW_CONTENT_DIRECTORY))
        protectPrivateDirectory(content)
        val lockPath = root.resolve(PREVIEW_LOCK_FILE)
        lockChannel = FileChannel.open(
            lockPath,
            StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE,
            LinkOption.NOFOLLOW_LINKS,
        )
        protectTemporaryFile(lockPath)
        lock = lockChannel.lock()
        val target = content.resolve(attachment.fileName)
        require(target.normalize().parent == content)
        val temporary = content.resolve(".${UUID.randomUUID()}.tmp")
        Files.createFile(temporary)
        protectTemporaryFile(temporary)
        Result.success(
            DesktopPreparedAttachmentOutput(
                temporary = temporary,
                target = target,
                openAfterCommit = true,
                cleanupScope = cleanupScope,
                previewRoot = root,
                previewLock = lock,
                previewLockChannel = lockChannel,
            ),
        )
    } catch (error: Exception) {
        runCatching { lock?.release() }
        runCatching { lockChannel?.close() }
        runCatching { deleteDesktopPreviewTree(root) }
        Result.failure(error)
    }
}

internal fun cleanupAbandonedDesktopAttachmentPreviews(
    temporaryRoot: Path = Path.of(System.getProperty("java.io.tmpdir")),
) {
    val root = temporaryRoot.toRealPath(LinkOption.NOFOLLOW_LINKS)
    val currentUserHome = Path.of(System.getProperty("user.home")).toRealPath()
    val currentOwner = Files.getOwner(currentUserHome, LinkOption.NOFOLLOW_LINKS)
    Files.newDirectoryStream(root).use { entries ->
        entries.forEach { candidate -> cleanupDesktopPreviewCandidate(candidate, currentOwner) }
    }
}

private fun cleanupDesktopPreviewCandidate(candidate: Path, currentOwner: UserPrincipal) {
    if (!isSafeDesktopPreviewCandidate(candidate, currentOwner)) return
    val lockPath = candidate.resolve(PREVIEW_LOCK_FILE)
    if (!Files.exists(lockPath, LinkOption.NOFOLLOW_LINKS)) {
        deleteDesktopPreviewTree(candidate)
    } else if (hasUnlockedDesktopPreviewLock(lockPath)) {
        deleteDesktopPreviewTree(candidate)
    }
}

private fun isSafeDesktopPreviewCandidate(candidate: Path, currentOwner: UserPrincipal): Boolean {
    if (
        !isOwnedDesktopPreviewDirectoryName(candidate.fileName.toString()) ||
        !Files.isDirectory(candidate, LinkOption.NOFOLLOW_LINKS) ||
        Files.getOwner(candidate, LinkOption.NOFOLLOW_LINKS) != currentOwner
    ) {
        return false
    }
    val rootEntries = Files.newDirectoryStream(candidate).use { children ->
        children.mapTo(mutableSetOf()) { child -> child.fileName.toString() }
    }
    return rootEntries.all { entry -> entry == PREVIEW_LOCK_FILE || entry == PREVIEW_CONTENT_DIRECTORY }
}

private fun hasUnlockedDesktopPreviewLock(lockPath: Path): Boolean {
    if (!Files.isRegularFile(lockPath, LinkOption.NOFOLLOW_LINKS)) return false
    val channel = runCatching {
        FileChannel.open(lockPath, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)
    }.getOrNull()
    return channel?.use {
        val lock = try {
            it.tryLock()
        } catch (_: OverlappingFileLockException) {
            null
        }
        lock?.release()
        lock != null
    } ?: false
}

private fun createPrivatePreviewRoot(): Path {
    val temporaryRoot = Path.of(System.getProperty("java.io.tmpdir")).toRealPath(LinkOption.NOFOLLOW_LINKS)
    repeat(PREVIEW_DIRECTORY_CREATE_ATTEMPTS) {
        val candidate = temporaryRoot.resolve(PREVIEW_DIRECTORY_PREFIX + UUID.randomUUID().toString())
        try {
            val posixView = Files.getFileAttributeView(
                temporaryRoot,
                PosixFileAttributeView::class.java,
                LinkOption.NOFOLLOW_LINKS,
            )
            val created = if (posixView == null) {
                Files.createDirectory(candidate)
            } else {
                Files.createDirectory(
                    candidate,
                    PosixFilePermissions.asFileAttribute(OWNER_ONLY_DIRECTORY_PERMISSIONS),
                )
            }
            protectPrivateDirectory(created)
            return created
        } catch (_: FileAlreadyExistsException) {
            // Generate another cryptographically unpredictable UUID path.
        }
    }
    error("A private attachment preview directory could not be allocated")
}

private fun isOwnedDesktopPreviewDirectoryName(value: String): Boolean {
    if (!value.startsWith(PREVIEW_DIRECTORY_PREFIX)) return false
    val token = value.removePrefix(PREVIEW_DIRECTORY_PREFIX)
    return token.length == UUID_TEXT_LENGTH && token.indices.all { index ->
        if (index in UUID_HYPHEN_INDICES) {
            token[index] == '-'
        } else {
            token[index] in '0'..'9' || token[index] in 'a'..'f'
        }
    }
}

private fun deleteDesktopPreviewTree(root: Path) {
    if (Files.isSymbolicLink(root)) {
        Files.deleteIfExists(root)
        return
    }
    Files.walkFileTree(
        root,
        object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.deleteIfExists(file)
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(directory: Path, error: java.io.IOException?): FileVisitResult {
                if (error != null) throw error
                Files.deleteIfExists(directory)
                return FileVisitResult.CONTINUE
            }
        },
    )
}

private fun protectPrivateDirectory(path: Path) {
    Files.getFileAttributeView(path, PosixFileAttributeView::class.java)
        ?.setPermissions(OWNER_ONLY_DIRECTORY_PERMISSIONS)
}

private fun protectTemporaryFile(path: Path) {
    Files.getFileAttributeView(path, PosixFileAttributeView::class.java)
        ?.setPermissions(
            setOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
            ),
        )
}

private fun readAttributesIfPresent(path: Path): java.nio.file.attribute.BasicFileAttributes? =
    if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
        Files.readAttributes(path, java.nio.file.attribute.BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
    } else {
        null
    }

private const val PREVIEW_DIRECTORY_PREFIX = "passvault-attachment-preview-"
private const val PREVIEW_CONTENT_DIRECTORY = "content"
private const val PREVIEW_LOCK_FILE = ".passvault-preview.lock"
private const val PREVIEW_DIRECTORY_CREATE_ATTEMPTS = 4
private const val UUID_TEXT_LENGTH = 36
private val UUID_HYPHEN_INDICES = setOf(8, 13, 18, 23)
private val OWNER_ONLY_DIRECTORY_PERMISSIONS = setOf(
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE,
    PosixFilePermission.OWNER_EXECUTE,
)
