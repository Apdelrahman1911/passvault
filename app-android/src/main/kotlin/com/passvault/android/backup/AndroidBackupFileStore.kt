package com.passvault.android.backup

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.backup.BackupFileSelectionCancelled
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.coroutines.resume

/**
 * Storage Access Framework adapter. The activity registers the launchers
 * before it reaches STARTED; the common ViewModel only sees a suspendable
 * file operation and never receives an Activity or Android URI directly.
 */
class AndroidBackupFileStore(
    context: Context,
) : BackupFileStore {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private var attachedActivity: ComponentActivity? = null
    private var openLauncher: ActivityResultLauncher<Array<String>>? = null
    private var saveLauncher: ActivityResultLauncher<String>? = null
    private var pending: PendingRequest? = null
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun attach(activity: ComponentActivity) {
        if (attachedActivity === activity) return
        check(attachedActivity == null || attachedActivity?.isFinishing == true) {
            "Another activity is already attached to the backup file store"
        }
        attachedActivity = activity
        openLauncher = activity.registerForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri -> complete(uri, isSave = false) }
        saveLauncher = activity.registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream"),
        ) { uri -> complete(uri, isSave = true) }
    }

    fun detach(activity: ComponentActivity) {
        if (attachedActivity !== activity) return
        attachedActivity = null
        openLauncher = null
        saveLauncher = null
        val request = pending
        pending = null
        request?.continuation?.let {
            resumeSafely(it, Result.failure(BackupFileSelectionCancelled()))
        }
        request?.bytes?.fill(0)
    }

    override suspend fun save(bytes: ByteArray, suggestedName: String): Result<BackupFile> {
        val launcher = saveLauncher
            ?: return Result.failure(IllegalStateException("File picker is not ready"))
        return request(PendingRequest.Kind.SAVE, bytes.copyOf(), suggestedName, launcher)
    }

    override suspend fun open(): Result<BackupFile> {
        val launcher = openLauncher
            ?: return Result.failure(IllegalStateException("File picker is not ready"))
        return request(PendingRequest.Kind.OPEN, null, null, launcher)
    }

    override suspend fun read(file: BackupFile): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val uri = file.path.toUri()
            require(uri.scheme == "content" || uri.scheme == "file")
            resolver.openInputStream(uri)?.use { input ->
                Result.success(readBounded(input))
            } ?: Result.failure(IllegalStateException("The backup file could not be opened"))
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("The backup file could not be read"))
        }
    }

    private suspend fun request(
        kind: PendingRequest.Kind,
        bytes: ByteArray?,
        suggestedName: String?,
        launcher: Any,
    ): Result<BackupFile> = suspendCancellableCoroutine { continuation ->
        val request = PendingRequest(kind, bytes, suggestedName, continuation)
        synchronized(this) {
            if (pending != null) {
                bytes?.fill(0)
                continuation.resume(Result.failure(IllegalStateException("Another file operation is active")))
                return@suspendCancellableCoroutine
            }
            pending = request
            continuation.invokeOnCancellation {
                synchronized(this) {
                    if (pending?.continuation === continuation) {
                        pending?.bytes?.fill(0)
                        pending = null
                    }
                }
            }
        }

        when (launcher) {
            is ActivityResultLauncher<*> -> {
                @Suppress("UNCHECKED_CAST")
                when (kind) {
                    PendingRequest.Kind.OPEN ->
                        (launcher as ActivityResultLauncher<Array<String>>).launch(
                            arrayOf("application/octet-stream", "application/zip", "application/json"),
                        )
                    PendingRequest.Kind.SAVE ->
                        (launcher as ActivityResultLauncher<String>).launch(suggestedName ?: "passvault-backup.pvault")
                }
            }
        }
    }

    private fun complete(uri: Uri?, isSave: Boolean) {
        val request: PendingRequest
        synchronized(this) {
            request = pending ?: return
            pending = null
        }

        if (uri == null) {
            request.bytes?.fill(0)
            request.continuation.resume(Result.failure(BackupFileSelectionCancelled()))
            return
        }

        ioScope.launch {
            val result = try {
                if (isSave) {
                    val bytes = request.bytes ?: error("Missing backup bytes")
                    resolver.openOutputStream(uri, "wt")?.use { it.write(bytes) }
                        ?: error("The backup destination could not be opened")
                    BackupFile(uri.toString(), displayName(uri) ?: "passvault-backup.pvault")
                } else {
                    BackupFile(uri.toString(), displayName(uri) ?: "backup")
                }.let(Result.Companion::success)
            } catch (_: Exception) {
                Result.failure(IllegalStateException("The backup file could not be saved"))
            } finally {
                request.bytes?.fill(0)
            }
            resumeSafely(request.continuation, result)
        }
    }

    private fun displayName(uri: Uri): String? {
        if (uri.scheme != "content") return uri.lastPathSegment
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            return if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            } else {
                null
            }
        }
        return null
    }

    private fun readBounded(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            require(total <= MAX_BACKUP_BYTES)
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
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
        val bytes: ByteArray?,
        val suggestedName: String?,
        val continuation: kotlinx.coroutines.CancellableContinuation<Result<BackupFile>>,
    ) {
        enum class Kind {
            OPEN,
            SAVE,
        }
    }

    private companion object {
        const val MAX_BACKUP_BYTES = 128L * 1024L * 1024L
    }
}
