@file:OptIn(
    kotlinx.cinterop.BetaInteropApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class,
)

package com.passvault.shared.platform

import com.passvault.core.database.backup.BackupContentSink
import com.passvault.core.database.backup.BackupContentSource
import com.passvault.core.database.backup.BackupLimits
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.hasOnlySafeSingleLineCodePoints
import com.passvault.core.domain.model.takeCodePoints
import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileSelectionCancelled
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.backup.BackupOutput
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionComplete
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSInputStream
import platform.Foundation.NSOutputStream
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.inputStreamWithFileAtPath
import platform.Foundation.outputStreamToFileAtPath
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

/** Native Files/iCloud import and export for the encrypted `.pvault` format. */
class IosBackupFileStore(
    private val fileManager: NSFileManager = NSFileManager.defaultManager,
    private val protectPath: (String) -> Unit = { path -> protectIosBackupPath(fileManager, path) },
) : BackupFileStore {
    // UIDocumentPickerViewController retains its delegate weakly.
    private var activeDelegate: NSObject? = null
    private var pickerActive = false

    /** Main-thread-confined paths copied into the sandbox by the picker. */
    private val ownedImportPaths = mutableSetOf<String>()

    private val cacheRoot: String by lazy {
        fileManager.URLForDirectory(
            directory = NSCachesDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )?.path ?: error("The iOS cache directory is unavailable")
    }

    override suspend fun create(suggestedName: String): Result<BackupOutput> {
        val safeName = safeBackupFileName(suggestedName)
        // Keep uniqueness in the private parent directory so Files presents
        // exactly the user-facing backup name rather than an internal token.
        val path = "$cacheRoot/passvault-export-${randomToken()}/$safeName"
        return Result.success(
            BackupOutput(
                file = BackupFile(path, safeName),
                sink = IosBackupSink(
                    path = path,
                    fileManager = fileManager,
                    protectPath = protectPath,
                    present = {
                        presentPicker(exportPath = path, suggestedName = safeName).getOrThrow()
                    },
                    cleanup = { deleteOwnedExport(path) },
                ),
            ),
        )
    }

    override suspend fun open(): Result<BackupFile> = try {
        presentPicker(exportPath = null, suggestedName = null)
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (_: Exception) {
        Result.failure(IllegalStateException("The backup file could not be opened"))
    }

    override suspend fun source(file: BackupFile): Result<BackupContentSource> =
        withContext(Dispatchers.Default) {
            try {
                val path = normalizedPath(file.path)
                protectPath(path)
                val attributes = fileManager.attributesOfItemAtPath(path, error = null)
                    ?: error("Missing backup file")
                val size = (attributes["NSFileSize"] as? Number)?.toLong()
                    ?: error("Missing backup size")
                require(size in 1..BackupLimits.MAX_BACKUP_BYTES)
                Result.success(IosBackupSource(path, size))
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                Result.failure(IllegalStateException("The backup file could not be read"))
            }
        }

    override suspend fun discard(file: BackupFile) = withContext(NonCancellable) {
        val normalized = runCatching { normalizedPath(file.path) }.getOrNull()
            ?: return@withContext
        val wasOwned = withContext(Dispatchers.Main) { ownedImportPaths.remove(normalized) }
        if (wasOwned) {
            withContext(Dispatchers.Default) { deleteExactPath(normalized) }
        }
    }

    private suspend fun presentPicker(
        exportPath: String?,
        suggestedName: String?,
    ): Result<BackupFile> = withContext(Dispatchers.Main) {
        if (pickerActive) {
            return@withContext Result.failure(IllegalStateException("Another file operation is active"))
        }
        val presenter = resolveIosPresenter()
            ?: return@withContext Result.failure(IllegalStateException("The document picker is unavailable"))
        awaitPicker(presenter, exportPath, suggestedName)
    }

    private suspend fun awaitPicker(
        presenter: UIViewController,
        exportPath: String?,
        suggestedName: String?,
    ): Result<BackupFile> = suspendCancellableCoroutine { continuation ->
        pickerActive = true
        lateinit var picker: UIDocumentPickerViewController
        var finished = false
        var resumedImportPath: String? = null

        fun deleteCancelledImport() {
            val path = resumedImportPath ?: return
            resumedImportPath = null
            ownedImportPaths.remove(path)
            deleteExactPath(path)
        }

        fun finish(result: Result<BackupFile>?) {
            if (finished) return
            finished = true
            finishPicker()
            if (result != null && continuation.isActive) {
                if (exportPath == null) {
                    result.getOrNull()?.let { importedFile ->
                        resumedImportPath = importedFile.path
                        ownedImportPaths += importedFile.path
                    }
                }
                continuation.resume(result)
            } else if (exportPath == null) {
                result?.getOrNull()?.let { deleteExactPath(it.path) }
            }
        }

        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>,
            ) {
                val result = try {
                    pickedFileResult(exportPath, suggestedName, didPickDocumentsAtURLs)
                } catch (_: Exception) {
                    Result.failure(IllegalStateException("The selected backup file is invalid"))
                }
                finish(result)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                finish(Result.failure(BackupFileSelectionCancelled()))
            }
        }
        activeDelegate = delegate
        picker = if (exportPath != null) {
            UIDocumentPickerViewController(
                forExportingURLs = listOf(NSURL.fileURLWithPath(exportPath)),
                asCopy = true,
            )
        } else {
            UIDocumentPickerViewController(
                forOpeningContentTypes = listOf(UTTypeData),
                asCopy = true,
            )
        }
        picker.delegate = delegate
        continuation.invokeOnCancellation {
            dispatch_async(dispatch_get_main_queue()) {
                if (finished) {
                    // Cancellation can win after the picker callback has
                    // resumed the continuation but before its result is
                    // dispatched to the caller. In that race no ViewModel
                    // receives the copied file, so this boundary must
                    // remove it from both the registry and the sandbox.
                    deleteCancelledImport()
                    return@dispatch_async
                }
                picker.dismissViewControllerAnimated(true, completion = null)
                finish(result = null)
            }
        }
        try {
            presenter.presentViewController(picker, animated = true, completion = null)
        } catch (_: Exception) {
            // Do not leave the singleton permanently marked busy when UIKit
            // rejects presentation during a scene transition.
            finish(Result.failure(IllegalStateException("The document picker could not be opened")))
        }
    }

    private fun pickedFileResult(
        exportPath: String?,
        suggestedName: String?,
        selectedUrls: List<*>,
    ): Result<BackupFile> {
        return if (exportPath != null) {
            Result.success(
                BackupFile(exportPath, suggestedName ?: "passvault-backup.pvault"),
            )
        } else {
            val path = (selectedUrls.firstOrNull() as? NSURL)?.path
            if (path == null) {
                Result.failure(IllegalStateException("No backup file was selected"))
            } else {
                val normalized = normalizedPath(path)
                try {
                    protectPath(normalized)
                } catch (error: IllegalStateException) {
                    deleteExactPath(normalized)
                    throw error
                }
                Result.success(
                    BackupFile(
                        normalized,
                        safeImportedDisplayName(normalized.substringAfterLast('/')),
                    ),
                )
            }
        }
    }

    private fun finishPicker() {
        pickerActive = false
        activeDelegate = null
    }

    private fun deleteOwnedExport(path: String) {
        val normalized = runCatching { normalizedPath(path) }.getOrNull() ?: return
        val exportDirectory = normalized.substringBeforeLast('/', missingDelimiterValue = "")
        if (!exportDirectory.startsWith("$cacheRoot/passvault-export-")) return
        deleteExactPath(exportDirectory)
    }

    private fun deleteExactPath(path: String) {
        val normalized = normalizedPath(path)
        if (fileManager.fileExistsAtPath(normalized)) {
            fileManager.removeItemAtPath(normalized, error = null)
        }
    }

}

private class IosBackupSource(
    private val path: String,
    override val declaredSizeBytes: Long,
) : BackupContentSource {
    private var stream: NSInputStream? = null
    private var totalBytes = 0L

    override suspend fun read(buffer: ByteArray): Int = withContext(Dispatchers.Default) {
        require(buffer.isNotEmpty())
        val input = stream ?: NSInputStream.inputStreamWithFileAtPath(path)
            ?.also {
                it.open()
                stream = it
            }
            ?: error("Unreadable backup file")
        val count = buffer.usePinned { pinned ->
            input.read(
                buffer = pinned.addressOf(0).reinterpret(),
                maxLength = buffer.size.toULong(),
            )
        }
        check(count >= 0) { "Unreadable backup file" }
        if (count == 0L) {
            -1
        } else {
            totalBytes += count
            require(totalBytes <= BackupLimits.MAX_BACKUP_BYTES)
            count.toInt()
        }
    }

    override suspend fun rewind() = withContext(Dispatchers.Default) {
        stream?.close()
        stream = null
        totalBytes = 0L
    }

    override suspend fun close() = withContext(Dispatchers.Default) {
        stream?.close()
        stream = null
    }
}

private class IosBackupSink(
    private val path: String,
    private val fileManager: NSFileManager,
    private val protectPath: (String) -> Unit,
    private val present: suspend () -> Unit,
    private val cleanup: () -> Unit,
) : BackupContentSink {
    private var stream: NSOutputStream? = null
    private var totalBytes = 0L
    private var finished = false

    override suspend fun write(buffer: ByteArray, byteCount: Int) = withContext(Dispatchers.Default) {
        check(!finished)
        require(byteCount in 0..buffer.size)
        totalBytes += byteCount
        require(totalBytes <= BackupLimits.MAX_BACKUP_BYTES)
        val output = stream ?: createOutputStream()
        var offset = 0
        while (offset < byteCount) {
            val written = buffer.usePinned { pinned ->
                output.write(
                    buffer = pinned.addressOf(offset).reinterpret(),
                    maxLength = (byteCount - offset).toULong(),
                )
            }
            check(written > 0) { "The backup output could not be written" }
            offset += written.toInt()
        }
    }

    override suspend fun commit() {
        check(!finished)
        withContext(NonCancellable + Dispatchers.Default) {
            if (stream == null) createOutputStream()
            stream?.close()
            stream = null
        }
        try {
            present()
            finished = true
        } finally {
            withContext(NonCancellable + Dispatchers.Default) { cleanup() }
        }
    }

    override suspend fun abort() = withContext(NonCancellable + Dispatchers.Default) {
        if (finished) return@withContext
        finished = true
        stream?.close()
        stream = null
        cleanup()
    }

    private fun createOutputStream(): NSOutputStream {
        val directory = path.substringBeforeLast('/')
        check(
            fileManager.createDirectoryAtPath(
                path = directory,
                withIntermediateDirectories = true,
                attributes = IOS_BACKUP_PROTECTION,
                error = null,
            ),
        ) { "The temporary export directory could not be created" }
        protectPath(directory)
        check(fileManager.createFileAtPath(path, contents = null, attributes = IOS_BACKUP_PROTECTION)) {
            "The temporary export file could not be created"
        }
        protectPath(path)
        return NSOutputStream.outputStreamToFileAtPath(path, append = false).also {
            it.open()
            stream = it
        }
    }
}

private fun resolveIosPresenter(): UIViewController? {
    val app = UIApplication.sharedApplication
    val sceneWindow = app.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        ?.let { scene ->
            val windows = scene.windows.filterIsInstance<UIWindow>()
            windows.firstOrNull { it.isKeyWindow() } ?: windows.firstOrNull()
        }
    var controller = sceneWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

private fun normalizedPath(path: String): String {
    require(path.startsWith('/'))
    require(!path.split('/').contains(".."))
    return path
}

private fun safeBackupFileName(name: String): String {
    val sanitized = name.substringAfterLast('/').substringAfterLast('\\')
        .filter { it.isLetterOrDigit() || it in "._-" }
        // APFS limits a filename component to 255 UTF-8 bytes. Retained
        // characters are BMP letters/digits or ASCII punctuation, so 64 code
        // points remain below that limit even at three bytes per character.
        .takeCodePoints(MAX_EXPORT_FILE_NAME_CODE_POINTS)
    return sanitized.takeIf { it.endsWith(".pvault", ignoreCase = true) }
        ?: "passvault-backup.pvault"
}

private fun safeImportedDisplayName(name: String): String = name.takeIf {
    it.isNotBlank() &&
        it != "." &&
        it != ".." &&
        it.codePointLength() <= MAX_DISPLAY_NAME_CODE_POINTS &&
        it.hasOnlySafeSingleLineCodePoints()
} ?: "backup"

internal fun protectIosBackupPath(fileManager: NSFileManager, path: String) {
    check(fileManager.setAttributes(IOS_BACKUP_PROTECTION, ofItemAtPath = path, error = null)) {
        "The temporary backup file could not be protected"
    }
}

internal fun iosBackupProtectionAttributes(): Map<Any?, Any?> = IOS_BACKUP_PROTECTION

private fun randomToken(): String = platform.Foundation.NSUUID.UUID().UUIDString.lowercase()

private const val MAX_DISPLAY_NAME_CODE_POINTS = 160
private const val MAX_EXPORT_FILE_NAME_CODE_POINTS = 64
private val IOS_BACKUP_PROTECTION = mapOf<Any?, Any?>(NSFileProtectionKey to NSFileProtectionComplete)
