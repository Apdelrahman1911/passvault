package com.passvault.desktop.attachment

import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentException
import com.passvault.core.domain.repository.AttachmentPolicy
import com.passvault.core.domain.repository.VaultRepository
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
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.UserPrincipal
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

class DesktopAttachmentFileStore internal constructor(
    vaultRepository: VaultRepository,
    private val cleanupScope: CoroutineScope,
    private val previewManager: DesktopAttachmentPreviewManager = DesktopAttachmentPreviewManager(),
) : AttachmentFileStore {

    init {
        bindDesktopAttachmentPreviewLifecycle(
            scope = cleanupScope,
            sessionStates = vaultRepository.getSessionState(),
            previewManager = previewManager,
        )
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
            protectExportTemporaryFile(temporary)
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
            previewManager.createOutput(attachment, cleanupScope)
        }

    internal suspend fun purgePreviews() = withContext(NonCancellable + Dispatchers.IO) {
        previewManager.disableAndPurge()
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
    private val previewLease: DesktopAttachmentPreviewLease? = null,
    private val openPreview: (Path) -> Unit = {},
    private val previewLifetimeMilliseconds: Long = DESKTOP_PREVIEW_LIFETIME_MILLISECONDS,
) : PreparedAttachmentOutput, AttachmentContentSink {
    private val outputLock = Any()
    @Volatile
    private var output: OutputStream? = null
    private var bytesWritten = 0L
    private var sinkCommitted = false
    private var handedOff = false

    override val sink: AttachmentContentSink
        get() = this

    init {
        previewLease?.setBeforeDelete(::closeOutputForPreviewCleanup)
    }

    override suspend fun write(buffer: ByteArray, byteCount: Int) = withContext(Dispatchers.IO) {
        synchronized(outputLock) {
            check(!sinkCommitted)
            require(byteCount in 0..buffer.size)
            bytesWritten += byteCount
            require(bytesWritten <= AttachmentPolicy.MAX_FILE_SIZE_BYTES)
            val stream = output ?: BufferedOutputStream(Files.newOutputStream(temporary)).also { output = it }
            stream.write(buffer, 0, byteCount)
        }
    }

    override suspend fun commit() = withContext(NonCancellable + Dispatchers.IO) {
        synchronized(outputLock) {
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
    }

    @Suppress("TooGenericExceptionCaught") // Normalize native Desktop viewer failures at the platform boundary.
    override suspend fun present(): Result<Unit> = try {
        check(sinkCommitted)
        check(!handedOff)
        if (openAfterCommit) {
            withContext(Dispatchers.IO) { openPreview(target) }
            registerPreviewDeleteOnExit()
            requireNotNull(previewLease).scheduleExpiry(cleanupScope, previewLifetimeMilliseconds)
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
        closeOutputForPreviewCleanup()
        Files.deleteIfExists(temporary)
        if (openAfterCommit) {
            requireNotNull(previewLease).delete(cancelExpiry = true)
        }
    }

    private fun registerPreviewDeleteOnExit() {
        val root = requireNotNull(previewLease).root
        // Delete-on-exit runs in reverse registration order.
        root.toFile().deleteOnExit()
        target.parent.toFile().deleteOnExit()
        root.resolve(PREVIEW_LOCK_FILE).toFile().deleteOnExit()
        target.toFile().deleteOnExit()
    }

    private fun closeOutputForPreviewCleanup() = synchronized(outputLock) {
        runCatching { output?.close() }
        output = null
    }
}

internal class DesktopAttachmentPreviewManager(
    private val temporaryRoot: Path = Path.of(System.getProperty("java.io.tmpdir")),
    private val previewLifetimeMilliseconds: Long = DESKTOP_PREVIEW_LIFETIME_MILLISECONDS,
    private val openPreview: (Path) -> Unit = { path -> Desktop.getDesktop().open(path.toFile()) },
) {
    private val activeLeases = mutableSetOf<DesktopAttachmentPreviewLease>()
    private var previewsEnabled = false

    @Synchronized
    fun enable() {
        previewsEnabled = true
    }

    @Suppress("TooGenericExceptionCaught") // Every partial allocation must release its lock and plaintext.
    fun createOutput(
        attachment: AttachmentMetadata,
        cleanupScope: CoroutineScope,
    ): Result<PreparedAttachmentOutput> {
        val root = createPrivatePreviewRoot(temporaryRoot)
        var lockChannel: FileChannel? = null
        var lock: FileLock? = null
        var lease: DesktopAttachmentPreviewLease? = null
        return try {
            val content = createPrivateDesktopPreviewDirectory(root.resolve(PREVIEW_CONTENT_DIRECTORY))
            val lockPath = createPrivateDesktopPreviewFile(root.resolve(PREVIEW_LOCK_FILE))
            lockChannel = FileChannel.open(
                lockPath,
                StandardOpenOption.WRITE,
                LinkOption.NOFOLLOW_LINKS,
            )
            lock = lockChannel.lock()
            val target = content.resolve(attachment.fileName)
            require(target.normalize().parent == content)
            val temporary = createPrivateDesktopPreviewFile(content.resolve(".${UUID.randomUUID()}.tmp"))
            lease = DesktopAttachmentPreviewLease(
                root = root,
                lock = lock,
                lockChannel = lockChannel,
                onClosed = ::remove,
            )
            check(addIfEnabled(lease)) { "Attachment previews require an unlocked vault session" }
            Result.success(
                DesktopPreparedAttachmentOutput(
                    temporary = temporary,
                    target = target,
                    openAfterCommit = true,
                    cleanupScope = cleanupScope,
                    previewLease = lease,
                    openPreview = openPreview,
                    previewLifetimeMilliseconds = previewLifetimeMilliseconds,
                ),
            )
        } catch (error: Exception) {
            if (lease == null) {
                runCatching { lock?.release() }
                runCatching { lockChannel?.close() }
                runCatching { deleteDesktopPreviewTree(root) }
            } else {
                runCatching { lease.delete(cancelExpiry = true) }
            }
            Result.failure(error)
        }
    }

    fun disableAndPurge() {
        val leases = synchronized(this) {
            previewsEnabled = false
            activeLeases.toList()
        }
        val failures = mutableListOf<Throwable>()
        leases.forEach { lease ->
            runCatching { lease.delete(cancelExpiry = true) }
                .exceptionOrNull()
                ?.let(failures::add)
        }
        runCatching { cleanupAbandonedDesktopAttachmentPreviews(temporaryRoot) }
            .exceptionOrNull()
            ?.let(failures::add)
        if (failures.isNotEmpty()) {
            throw IllegalStateException("Desktop attachment preview cleanup failed").also { failure ->
                failures.forEach(failure::addSuppressed)
            }
        }
    }

    @Synchronized
    private fun addIfEnabled(lease: DesktopAttachmentPreviewLease): Boolean =
        previewsEnabled && activeLeases.add(lease)

    @Synchronized
    private fun remove(lease: DesktopAttachmentPreviewLease) {
        activeLeases.remove(lease)
    }
}

internal class DesktopAttachmentPreviewLease(
    val root: Path,
    private val lock: FileLock,
    private val lockChannel: FileChannel,
    private val onClosed: (DesktopAttachmentPreviewLease) -> Unit,
) {
    private var resourcesReleased = false
    private var deletionComplete = false
    private var expiryJob: Job? = null
    private var beforeDelete: (() -> Unit)? = null

    @Synchronized
    fun setBeforeDelete(callback: () -> Unit) {
        if (resourcesReleased) {
            callback()
        } else {
            beforeDelete = callback
        }
    }

    @Synchronized
    fun scheduleExpiry(scope: CoroutineScope, lifetimeMilliseconds: Long) {
        require(lifetimeMilliseconds > 0L)
        if (resourcesReleased || deletionComplete || expiryJob != null) return
        expiryJob = scope.launch {
            delay(lifetimeMilliseconds)
            runCatching { delete(cancelExpiry = false) }
        }
    }

    @Synchronized
    fun delete(cancelExpiry: Boolean) {
        if (deletionComplete) return
        if (cancelExpiry) expiryJob?.cancel()
        expiryJob = null
        if (!resourcesReleased) {
            beforeDelete?.invoke()
            beforeDelete = null
            runCatching { lock.release() }
            runCatching { lockChannel.close() }
            resourcesReleased = true
        }
        try {
            deleteDesktopPreviewTree(root)
            deletionComplete = true
            onClosed(this)
        } finally {
            if (deletionComplete) beforeDelete = null
        }
    }
}

internal fun bindDesktopAttachmentPreviewLifecycle(
    scope: CoroutineScope,
    sessionStates: Flow<VaultSessionState>,
    previewManager: DesktopAttachmentPreviewManager,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): Job = scope.launch(ioDispatcher) {
    runCatching { previewManager.disableAndPurge() }
    var activeSessionId: SessionId? = null
    sessionStates.collect { state ->
        if (state is VaultSessionState.Unlocked) {
            if (activeSessionId != state.sessionId) {
                val clean = runCatching { previewManager.disableAndPurge() }.isSuccess
                if (clean) {
                    activeSessionId = state.sessionId
                    previewManager.enable()
                }
            }
        } else {
            activeSessionId = null
            runCatching { previewManager.disableAndPurge() }
        }
    }
}

internal fun cleanupAbandonedDesktopAttachmentPreviews(
    temporaryRoot: Path = Path.of(System.getProperty("java.io.tmpdir")),
) {
    val root = temporaryRoot.toRealPath(LinkOption.NOFOLLOW_LINKS)
    val currentOwner = currentProcessOwnerFor(root)
    Files.newDirectoryStream(root).use { entries ->
        entries.forEach { candidate -> cleanupDesktopPreviewCandidate(candidate, currentOwner) }
    }
}

/**
 * Resolve the process's effective file owner on the same filesystem as the
 * preview root. On Windows, an administrator token can give the user profile
 * and newly-created temporary files different owners, so using user.home as
 * the identity oracle incorrectly leaves abandoned plaintext previews behind.
 */
private fun currentProcessOwnerFor(root: Path): UserPrincipal {
    val probe = Files.createTempFile(root, ".passvault-owner-probe-", ".tmp")
    return try {
        Files.getOwner(probe, LinkOption.NOFOLLOW_LINKS)
    } finally {
        Files.deleteIfExists(probe)
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

private fun createPrivatePreviewRoot(temporaryDirectory: Path): Path {
    val temporaryRoot = temporaryDirectory.toRealPath(LinkOption.NOFOLLOW_LINKS)
    repeat(PREVIEW_DIRECTORY_CREATE_ATTEMPTS) {
        val candidate = temporaryRoot.resolve(PREVIEW_DIRECTORY_PREFIX + UUID.randomUUID().toString())
        try {
            return createPrivateDesktopPreviewDirectory(candidate)
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

private fun protectExportTemporaryFile(path: Path) {
    Files.getFileAttributeView(path, java.nio.file.attribute.PosixFileAttributeView::class.java)
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
internal const val DESKTOP_PREVIEW_LIFETIME_MILLISECONDS = 60_000L
private const val UUID_TEXT_LENGTH = 36
private val UUID_HYPHEN_INDICES = setOf(8, 13, 18, 23)
