package com.passvault.android.attachment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.passvault.android.lifecycle.AndroidLifecycleLockCoordinator
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentException
import com.passvault.core.domain.repository.AttachmentPolicy
import com.passvault.feature.credential.AttachmentFileSelectionCancelled
import com.passvault.feature.credential.AttachmentFileStore
import com.passvault.feature.credential.AttachmentOutputAction
import com.passvault.feature.credential.PreparedAttachmentOutput
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class AndroidAttachmentFileStore(
    context: Context,
    private val lifecycleLockCoordinator: AndroidLifecycleLockCoordinator,
) : AttachmentFileStore {
    private val appContext = context.applicationContext
    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val startupCleanup = cleanupScope.async {
        cleanupAttachmentPlaintextCache(appContext.cacheDir)
    }
    private var attachedActivity: ComponentActivity? = null
    private var importLauncher: ActivityResultLauncher<Array<String>>? = null
    private var exportLauncher: ActivityResultLauncher<String>? = null
    private var pending: PendingPicker? = null

    fun attach(activity: ComponentActivity) {
        if (attachedActivity === activity) return
        check(attachedActivity == null || attachedActivity?.isFinishing == true) {
            "Another activity is already attached to the attachment file store"
        }
        attachedActivity = activity
        importLauncher = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            completePicker(uri, PickerKind.IMPORT)
        }
        exportLauncher = activity.registerForActivityResult(
            ActivityResultContracts.CreateDocument(DEFAULT_MIME_TYPE),
        ) { uri ->
            completePicker(uri, PickerKind.EXPORT)
        }
    }

    fun detach(activity: ComponentActivity, isChangingConfigurations: Boolean = false) {
        val request = synchronized(this) {
            if (attachedActivity !== activity) return
            attachedActivity = null
            importLauncher = null
            exportLauncher = null
            if (isChangingConfigurations) null else pending.also { pending = null }
        }
        request?.token?.close()
        request?.continuation?.let { resumeSafely(it, Result.failure(AttachmentFileSelectionCancelled())) }
    }

    override suspend fun selectForImport(): Result<AttachmentContentSource> {
        val launcher = importLauncher
            ?: return Result.failure(IllegalStateException("The attachment picker is not ready"))
        return request(PickerKind.IMPORT) { launcher.launch(arrayOf("*/*")) }
            .mapCatching { uri -> AndroidAttachmentSource(appContext, uri) }
    }

    override suspend fun createOutput(
        attachment: AttachmentMetadata,
        action: AttachmentOutputAction,
    ): Result<PreparedAttachmentOutput> = try {
        startupCleanup.await()
        val safeName = AttachmentPolicy.validateFileName(attachment.fileName)
        val destination = if (action == AttachmentOutputAction.EXPORT) {
            val launcher = exportLauncher
                ?: return Result.failure(IllegalStateException("The attachment picker is not ready"))
            request(PickerKind.EXPORT) { launcher.launch(safeName) }.getOrElse { return Result.failure(it) }
        } else {
            null
        }
        Result.success(
            AndroidPreparedAttachmentOutput(
                appContext = appContext,
                destination = destination,
                attachment = attachment.copy(fileName = safeName),
                action = action,
                cleanupScope = cleanupScope,
            ),
        )
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (error: AttachmentException) {
        Result.failure(error)
    } catch (_: Exception) {
        Result.failure(IllegalStateException("The attachment output could not be prepared"))
    }

    @Suppress("TooGenericExceptionCaught") // Every launcher failure must release the lifecycle lock and pending slot.
    private suspend fun request(kind: PickerKind, launch: () -> Unit): Result<Uri> =
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val request = PendingPicker(
                    kind = kind,
                    continuation = continuation,
                    token = lifecycleLockCoordinator.beginSystemFlow(),
                )
                synchronized(this@AndroidAttachmentFileStore) {
                    if (pending != null) {
                        request.token.close()
                        continuation.resume(
                            Result.failure(IllegalStateException("Another attachment picker is active")),
                        )
                        return@suspendCancellableCoroutine
                    }
                    pending = request
                    continuation.invokeOnCancellation {
                        synchronized(this@AndroidAttachmentFileStore) {
                            if (pending?.continuation === continuation) {
                                pending?.token?.close()
                                pending = null
                            }
                        }
                    }
                    try {
                        launch()
                    } catch (error: RuntimeException) {
                        failPickerLaunch(request, continuation, error)
                    }
                }
            }
        }

    private fun failPickerLaunch(
        request: PendingPicker,
        continuation: CancellableContinuation<Result<Uri>>,
        error: RuntimeException,
    ) {
        pending = null
        request.token.close()
        resumeSafely(continuation, Result.failure(error))
    }

    private fun completePicker(uri: Uri?, kind: PickerKind) {
        val request = synchronized(this) {
            pending?.takeIf { it.kind == kind }?.also { pending = null }
        } ?: return
        request.token.returnedToActivity()
        val result = uri?.let(Result.Companion::success)
            ?: Result.failure(AttachmentFileSelectionCancelled())
        resumeSafely(request.continuation, result)
    }

    private fun resumeSafely(
        continuation: CancellableContinuation<Result<Uri>>,
        result: Result<Uri>,
    ) {
        if (continuation.isActive) continuation.resume(result)
    }

    private data class PendingPicker(
        val kind: PickerKind,
        val continuation: CancellableContinuation<Result<Uri>>,
        val token: AndroidLifecycleLockCoordinator.SystemFlowToken,
    )

    private enum class PickerKind { IMPORT, EXPORT }

    private companion object {
        const val DEFAULT_MIME_TYPE = "application/octet-stream"
    }
}

private class AndroidAttachmentSource(
    private val context: Context,
    private val uri: Uri,
) : AttachmentContentSource {
    private var input: InputStream? = null
    private val metadata = queryMetadata(context, uri)

    override val displayName: String = metadata.first
    override val claimedMimeType: String? = context.contentResolver.getType(uri)
    override val declaredSizeBytes: Long? = metadata.second

    override suspend fun read(buffer: ByteArray): Int = withContext(Dispatchers.IO) {
        require(uri.scheme == "content")
        val stream = input ?: context.contentResolver.openInputStream(uri)
            ?.let(::BufferedInputStream)
            ?.also { input = it }
            ?: error("The selected attachment disappeared")
        stream.read(buffer)
    }

    override suspend fun close() = withContext(NonCancellable + Dispatchers.IO) {
        input?.close()
        input = null
    }
}

private class AndroidPreparedAttachmentOutput(
    private val appContext: Context,
    private val destination: Uri?,
    private val attachment: AttachmentMetadata,
    private val action: AttachmentOutputAction,
    private val cleanupScope: CoroutineScope,
) : PreparedAttachmentOutput, AttachmentContentSink {
    private val directory = File(
        appContext.cacheDir,
        if (action == AttachmentOutputAction.OPEN) "attachment-previews" else "attachment-exports",
    ).resolve(UUID.randomUUID().toString())
    private val temporary = directory.resolve(attachment.fileName)
    private var output: OutputStream? = null
    private var bytesWritten = 0L
    private var sinkCommitted = false
    private var handedOff = false

    override val sink: AttachmentContentSink
        get() = this

    init {
        check(directory.mkdirs())
        check(temporary.createNewFile())
    }

    override suspend fun write(buffer: ByteArray, byteCount: Int) = withContext(Dispatchers.IO) {
        check(!sinkCommitted)
        require(byteCount in 0..buffer.size)
        bytesWritten += byteCount
        require(bytesWritten <= AttachmentPolicy.MAX_FILE_SIZE_BYTES)
        val stream = output ?: BufferedOutputStream(FileOutputStream(temporary, false)).also { output = it }
        stream.write(buffer, 0, byteCount)
    }

    override suspend fun commit() = withContext(NonCancellable + Dispatchers.IO) {
        check(!sinkCommitted)
        output?.flush()
        output?.close()
        output = null
        sinkCommitted = true
    }

    @Suppress("TooGenericExceptionCaught") // Document providers expose implementation-specific write failures.
    override suspend fun present(): Result<Unit> = try {
        check(sinkCommitted)
        check(!handedOff)
        when (action) {
            AttachmentOutputAction.EXPORT -> withContext(Dispatchers.IO) {
                exportToSelectedDocument()
            }
            AttachmentOutputAction.OPEN -> withContext(Dispatchers.Main.immediate) {
                openWithExternalViewer()
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
        deleteTemporary()
    }

    private fun exportToSelectedDocument() {
        val uri = requireNotNull(destination)
        require(uri.scheme == "content")
        try {
            appContext.contentResolver.openOutputStream(uri, "wt")?.use { destinationStream ->
                BufferedInputStream(FileInputStream(temporary)).use { source ->
                    source.copyTo(destinationStream, DEFAULT_BUFFER_SIZE)
                }
            } ?: error("The selected attachment destination is unavailable")
        } finally {
            deleteTemporary()
        }
    }

    private fun openWithExternalViewer() {
        val contentUri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.attachment-files",
            temporary,
        )
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(contentUri, attachment.mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        cleanupScope.launch {
            delay(PREVIEW_LIFETIME_MILLISECONDS)
            deleteTemporary()
        }
    }

    private fun deleteTemporary() {
        temporary.delete()
        directory.delete()
    }

    private companion object {
        const val PREVIEW_LIFETIME_MILLISECONDS = 10L * 60L * 1000L
    }
}

/** Removes only PassVault-owned plaintext cache trees and never follows a symbolic-link boundary. */
internal fun cleanupAttachmentPlaintextCache(cacheDirectory: File) {
    val canonicalCache = cacheDirectory.canonicalFile
    require(canonicalCache.isDirectory) { "The application cache directory is unavailable" }
    listOf("attachment-previews", "attachment-exports").forEach { directoryName ->
        val root = File(canonicalCache, directoryName).absoluteFile
        if (root.exists()) deleteOwnedCacheTree(root)
    }
}

private fun deleteOwnedCacheTree(path: File) {
    val absolutePath = path.absoluteFile
    val canonicalPath = path.canonicalFile
    if (canonicalPath != absolutePath) {
        check(path.delete()) { "A symbolic link in the attachment cache could not be removed" }
        return
    }
    if (path.isDirectory) {
        path.listFiles()?.forEach(::deleteOwnedCacheTree)
            ?: error("The attachment cache directory could not be read")
    }
    check(path.delete()) { "The attachment cache path could not be removed" }
}

private fun queryMetadata(context: Context, uri: Uri): Pair<String, Long?> {
    require(uri.scheme == "content")
    var name: String? = null
    var size: Long? = null
    context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
        null,
        null,
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIndex >= 0 && !cursor.isNull(nameIndex)) name = cursor.getString(nameIndex)
            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
        }
    }
    val safeName = AttachmentPolicy.validateFileName(name ?: "attachment")
    size?.let(AttachmentPolicy::validateFileSize)
    return safeName to size
}
