package com.passvault.android.backup

import android.content.Context
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.passvault.android.lifecycle.AndroidLifecycleLockCoordinator
import com.passvault.android.picker.AndroidPickerHostState
import com.passvault.android.picker.assertAndroidMainThread
import com.passvault.core.database.backup.BackupContentSink
import com.passvault.core.database.backup.BackupContentSource
import com.passvault.core.database.backup.BackupLimits
import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.backup.BackupFileSelectionCancelled
import com.passvault.feature.backup.BackupOutput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Storage Access Framework adapter. The activity registers the launchers
 * before it reaches STARTED; the common ViewModel only sees a suspendable
 * file operation and never receives an Activity or Android URI directly.
 */
class AndroidBackupFileStore(
    context: Context,
    private val lifecycleLockCoordinator: AndroidLifecycleLockCoordinator,
) : BackupFileStore {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private val pickerHost = AndroidPickerHostState<ComponentActivity, PickerLaunchers>(
        description = "backup file store",
        isFinishing = ComponentActivity::isFinishing,
        assertOwnerThread = ::assertAndroidMainThread,
    )
    private var pending: PendingRequest? = null

    fun attach(activity: ComponentActivity) {
        pickerHost.attach(activity) {
            val openLauncher = activity.registerForActivityResult(
                ActivityResultContracts.OpenDocument(),
            ) { uri -> complete(uri, PendingRequest.Kind.OPEN) }
            val saveLauncher = activity.registerForActivityResult(
                ActivityResultContracts.CreateDocument("application/octet-stream"),
            ) { uri -> complete(uri, PendingRequest.Kind.SAVE) }
            PickerLaunchers(openLauncher, saveLauncher)
        }
    }

    fun detach(activity: ComponentActivity, isChangingConfigurations: Boolean = false) {
        val decision = pickerHost.detach(activity, isChangingConfigurations)
        if (!decision.cancelPending) return
        val request = synchronized(this) { pending.also { pending = null } }
        request?.systemFlowToken?.close()
        request?.continuation?.let {
            resumeSafely(it, Result.failure(BackupFileSelectionCancelled()))
        }
    }

    override suspend fun create(suggestedName: String): Result<BackupOutput> {
        val safeSuggestedName = suggestedName.validatedDisplayName(MAX_DISPLAY_NAME_CHARS)
            ?.takeIf { it.endsWith(BACKUP_EXTENSION, ignoreCase = true) }
            ?: DEFAULT_BACKUP_NAME
        val selected = withContext(Dispatchers.Main.immediate) {
            val launcher = pickerHost.launchersOrNull()?.save
                ?: return@withContext Result.failure(IllegalStateException("File picker is not ready"))
            request(PendingRequest.Kind.SAVE) { launcher.launch(safeSuggestedName) }
        }
        return selected.map { file ->
            BackupOutput(
                file = file,
                sink = AndroidBackupSink(appContext, file.path.toUri()),
            )
        }
    }

    override suspend fun open(): Result<BackupFile> = withContext(Dispatchers.Main.immediate) {
        val launcher = pickerHost.launchersOrNull()?.open
            ?: return@withContext Result.failure(IllegalStateException("File picker is not ready"))
        request(PendingRequest.Kind.OPEN) {
            launcher.launch(arrayOf("application/octet-stream", "application/zip", "application/json"))
        }
    }

    override suspend fun source(file: BackupFile): Result<BackupContentSource> =
        withContext(Dispatchers.IO) {
            try {
                val uri = file.path.toUri()
                require(uri.scheme == "content" || uri.scheme == "file")
                val size = resolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                    descriptor.length.takeIf { it >= 0 }
                }
                if (size != null) require(size in 1..BackupLimits.MAX_BACKUP_BYTES)
                Result.success(AndroidBackupSource(resolver, uri, size))
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                Result.failure(IllegalStateException("The backup file could not be read"))
            }
        }

    private suspend fun request(
        kind: PendingRequest.Kind,
        launchPicker: () -> Unit,
    ): Result<BackupFile> = suspendCancellableCoroutine { continuation ->
        assertAndroidMainThread()
        val request = PendingRequest(
            kind = kind,
            continuation = continuation,
            systemFlowToken = lifecycleLockCoordinator.beginSystemFlow(),
        )
        val accepted = synchronized(this) {
            if (pending == null) {
                pending = request
                true
            } else {
                false
            }
        }
        if (!accepted) {
            request.systemFlowToken.close()
            continuation.resume(Result.failure(IllegalStateException("Another file operation is active")))
            return@suspendCancellableCoroutine
        }
        continuation.invokeOnCancellation {
            val cancelledRequest = synchronized(this) {
                pending
                    ?.takeIf { it.continuation === continuation }
                    ?.also { pending = null }
            }
            cancelledRequest?.systemFlowToken?.close()
        }

        // invokeOnCancellation removes and wipes the pending request. Avoid
        // opening a system picker when cancellation won before the launcher
        // handoff reached the main thread.
        if (!continuation.isActive) return@suspendCancellableCoroutine

        try {
            launchPicker()
        } catch (_: Exception) {
            val ownedRequest = synchronized(this) {
                (pending === request).also { owned -> if (owned) pending = null }
            }
            if (!ownedRequest) return@suspendCancellableCoroutine
            request.systemFlowToken.close()
            resumeSafely(
                continuation,
                Result.failure(IllegalStateException("The file picker could not be opened")),
            )
        }
    }

    private fun complete(uri: Uri?, kind: PendingRequest.Kind) {
        val request = synchronized(this) {
            pending
                ?.takeIf { it.kind == kind }
                ?.also { pending = null }
        } ?: return
        request.systemFlowToken.returnedToActivity()

        if (uri == null) {
            resumeSafely(request.continuation, Result.failure(BackupFileSelectionCancelled()))
            return
        }
        val fallback = if (kind == PendingRequest.Kind.SAVE) DEFAULT_BACKUP_NAME else "backup"
        resumeSafely(
            request.continuation,
            Result.success(BackupFile(uri.toString(), displayNameSafely(uri) ?: fallback)),
        )
    }

    private fun displayNameSafely(uri: Uri): String? = runCatching {
        displayName(uri)?.validatedDisplayName(MAX_DISPLAY_NAME_CHARS)
    }.getOrNull()

    private fun displayName(uri: Uri): String? {
        return if (uri.scheme != "content") {
            uri.lastPathSegment
        } else {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                } else {
                    null
                }
            }
        }
    }

    private fun resumeSafely(
        continuation: kotlinx.coroutines.CancellableContinuation<Result<BackupFile>>,
        result: Result<BackupFile>,
    ) {
        try {
            if (continuation.isActive) continuation.resume(result)
        } catch (_: IllegalStateException) {
            // The caller may have cancelled while the picker callback was
            // completing. The bytes are wiped by the owning request.
        }
    }

    private data class PendingRequest(
        val kind: Kind,
        val continuation: kotlinx.coroutines.CancellableContinuation<Result<BackupFile>>,
        val systemFlowToken: AndroidLifecycleLockCoordinator.SystemFlowToken,
    ) {
        enum class Kind {
            OPEN,
            SAVE,
        }
    }

    private data class PickerLaunchers(
        val open: ActivityResultLauncher<Array<String>>,
        val save: ActivityResultLauncher<String>,
    )

    private companion object {
        const val MAX_DISPLAY_NAME_CHARS = 160
        const val BACKUP_EXTENSION = ".pvault"
        const val DEFAULT_BACKUP_NAME = "passvault-backup.pvault"
    }
}

private class AndroidBackupSource(
    private val resolver: ContentResolver,
    private val uri: Uri,
    override val declaredSizeBytes: Long?,
) : BackupContentSource {
    private var input: InputStream? = null
    private var byteCount = 0L

    override suspend fun read(buffer: ByteArray): Int = withContext(Dispatchers.IO) {
        require(buffer.isNotEmpty())
        val stream = input ?: resolver.openInputStream(uri)
            ?.also { input = it }
            ?: error("The backup file could not be opened")
        stream.read(buffer).also { count ->
            require(count == -1 || count in 1..buffer.size)
            if (count > 0) {
                byteCount += count
                require(byteCount <= BackupLimits.MAX_BACKUP_BYTES)
            }
        }
    }

    override suspend fun rewind() = withContext(Dispatchers.IO) {
        input?.close()
        input = null
        byteCount = 0L
    }

    override suspend fun close() = withContext(Dispatchers.IO) {
        input?.close()
        input = null
    }
}

private class AndroidBackupSink(
    context: Context,
    private val destination: Uri,
) : BackupContentSink {
    private val resolver = context.contentResolver
    private val privateRoot = context.noBackupFilesDir.canonicalFile
    private val stagingDirectory = File(privateRoot, "backup-export-staging")
    private val temporary = File(stagingDirectory, "${UUID.randomUUID()}.tmp")
    private var output: OutputStream? = null
    private var byteCount = 0L
    private var finished = false
    private var stagingInitialized = false

    override suspend fun write(buffer: ByteArray, byteCount: Int) = withContext(Dispatchers.IO) {
        check(!finished)
        require(byteCount in 0..buffer.size)
        this@AndroidBackupSink.byteCount += byteCount
        require(this@AndroidBackupSink.byteCount <= BackupLimits.MAX_BACKUP_BYTES)
        ensureStagingDirectory()
        val stream = output ?: temporary.outputStream().also { output = it }
        stream.write(buffer, 0, byteCount)
    }

    override suspend fun commit() = withContext(Dispatchers.IO) {
        check(!finished)
        output?.flush()
        output?.close()
        output = null
        val destinationStream = resolver.openOutputStream(destination, "wt")
            ?: error("The backup destination could not be opened")
        try {
            destinationStream.use { target ->
                temporary.inputStream().use { source -> source.copyTo(target, DEFAULT_BUFFER_SIZE) }
            }
            finished = true
        } finally {
            temporary.delete()
        }
    }

    override suspend fun abort() = withContext(Dispatchers.IO) {
        if (finished) return@withContext
        finished = true
        runCatching { output?.close() }
        output = null
        temporary.delete()
    }

    private fun ensureStagingDirectory() {
        check(!stagingDirectory.isFile)
        check(stagingDirectory.mkdirs() || stagingDirectory.isDirectory)
        val canonicalStaging = stagingDirectory.canonicalFile
        check(canonicalStaging.parentFile == privateRoot)
        check(canonicalStaging == stagingDirectory.absoluteFile)
        if (!stagingInitialized) {
            stagingDirectory.listFiles().orEmpty().forEach { candidate ->
                if (candidate != temporary && candidate.extension == TEMPORARY_EXTENSION) {
                    check(candidate.canonicalFile.parentFile == canonicalStaging)
                    // Canonical and lexical paths differ for a symlink. This remains available on API 24,
                    // unlike java.nio.file.Files.isSymbolicLink (API 26).
                    check(candidate.canonicalFile == candidate.absoluteFile)
                    if (candidate.isFile) candidate.delete()
                }
            }
            stagingInitialized = true
        }
    }

    private companion object {
        const val TEMPORARY_EXTENSION = "tmp"
    }
}

internal fun String.validatedDisplayName(maxCodePoints: Int): String? {
    val sanitized = StringBuilder(length.coerceAtMost(maxCodePoints))
    var index = 0
    var codePoints = 0
    var isValid = true
    while (index < length && codePoints < maxCodePoints && isValid) {
        val codePoint = codePointAt(index)
        val unsafe = when (Character.getType(codePoint)) {
            Character.CONTROL.toInt(),
            Character.FORMAT.toInt(),
            Character.LINE_SEPARATOR.toInt(),
            Character.PARAGRAPH_SEPARATOR.toInt(),
            Character.SURROGATE.toInt(),
            -> true
            else -> false
        }
        isValid = !unsafe && codePoint != '/'.code && codePoint != '\\'.code
        if (isValid) {
            sanitized.appendCodePoint(codePoint)
            index += Character.charCount(codePoint)
            codePoints++
        }
    }
    return sanitized.toString()
        .trim()
        .takeIf { isValid && index == length && it.isNotBlank() && it != "." && it != ".." }
}
