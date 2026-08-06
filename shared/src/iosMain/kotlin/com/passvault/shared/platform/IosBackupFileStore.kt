@file:OptIn(
    kotlinx.cinterop.BetaInteropApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class,
)

package com.passvault.shared.platform

import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileSelectionCancelled
import com.passvault.feature.backup.BackupFileStore
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSDataWritingAtomic
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.coroutines.resume

/** Native Files/iCloud import and export for the encrypted `.pvault` format. */
class IosBackupFileStore(
    private val fileManager: NSFileManager = NSFileManager.defaultManager,
) : BackupFileStore {
    // UIDocumentPickerViewController retains its delegate weakly.
    private var activeDelegate: NSObject? = null
    private var pickerActive = false

    private val cacheRoot: String by lazy {
        fileManager.URLForDirectory(
            directory = NSCachesDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )?.path ?: error("The iOS cache directory is unavailable")
    }

    override suspend fun save(bytes: ByteArray, suggestedName: String): Result<BackupFile> {
        val safeName = safeFileName(suggestedName)
        val path = "$cacheRoot/passvault-export-${randomToken()}-$safeName"
        return try {
            withContext(Dispatchers.Default) { writeTemporary(path, bytes) }
            presentPicker(exportPath = path, suggestedName = safeName)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("The backup file could not be saved"))
        } finally {
            withContext(NonCancellable + Dispatchers.Default) { deleteIfTemporary(path) }
        }
    }

    override suspend fun open(): Result<BackupFile> = try {
        presentPicker(exportPath = null, suggestedName = null)
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (_: Exception) {
        Result.failure(IllegalStateException("The backup file could not be opened"))
    }

    override suspend fun read(file: BackupFile): Result<ByteArray> = withContext(Dispatchers.Default) {
        try {
            val path = normalizedPath(file.path)
            val attributes = fileManager.attributesOfItemAtPath(path, error = null)
                ?: error("Missing backup file")
            val size = (attributes["NSFileSize"] as? Number)?.toLong()
                ?: error("Missing backup size")
            require(size in 0..MAX_BACKUP_BYTES)
            val data = memScoped {
                val error = alloc<ObjCObjectVar<NSError?>>()
                error.value = null
                NSData.dataWithContentsOfFile(path, options = 0u, error = error.ptr)
                    ?: error("Unreadable backup file")
            }
            require(data.length <= MAX_BACKUP_BYTES.toULong())
            Result.success(data.toByteArray())
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("The backup file could not be read"))
        }
    }

    override suspend fun discard(file: BackupFile) {
        withContext(Dispatchers.Default) { deleteIfTemporary(file.path) }
    }

    private suspend fun presentPicker(
        exportPath: String?,
        suggestedName: String?,
    ): Result<BackupFile> = withContext(Dispatchers.Main) {
        if (pickerActive) {
            return@withContext Result.failure(IllegalStateException("Another file operation is active"))
        }
        val presenter = resolvePresenter()
            ?: return@withContext Result.failure(IllegalStateException("The document picker is unavailable"))

        suspendCancellableCoroutine { continuation ->
            pickerActive = true
            lateinit var picker: UIDocumentPickerViewController
            val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAtURLs: List<*>,
                ) {
                    finishPicker()
                    val selectedUrl = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                    val result = if (exportPath != null) {
                        Result.success(BackupFile(exportPath, suggestedName ?: "passvault-backup.pvault"))
                    } else {
                        val path = selectedUrl?.path
                        if (path == null) {
                            Result.failure(IllegalStateException("No backup file was selected"))
                        } else {
                            Result.success(BackupFile(path, path.substringAfterLast('/')))
                        }
                    }
                    if (continuation.isActive) continuation.resume(result)
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    finishPicker()
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(BackupFileSelectionCancelled()))
                    }
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
                // The picker callback owns final state cleanup. UIKit callbacks
                // arrive on the main thread even if the caller was cancelled.
                picker.dismissViewControllerAnimated(true, completion = null)
                finishPicker()
            }
            presenter.presentViewController(picker, animated = true, completion = null)
        }
    }

    private fun finishPicker() {
        pickerActive = false
        activeDelegate = null
    }

    private fun writeTemporary(path: String, bytes: ByteArray) {
        require(bytes.size.toLong() <= MAX_BACKUP_BYTES)
        val data = bytes.toNSData()
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            error.value = null
            if (!data.writeToFile(path, options = NSDataWritingAtomic, error = error.ptr)) {
                throw IllegalStateException("The temporary backup could not be written")
            }
        }
    }

    private fun deleteIfTemporary(path: String) {
        val normalized = runCatching { normalizedPath(path) }.getOrNull() ?: return
        val allowedPrefixes = listOf(
            "$cacheRoot/passvault-export-",
            "$cacheRoot/passvault-import-",
            "${NSTemporaryDirectoryPath()}/",
        )
        if (allowedPrefixes.none(normalized::startsWith)) return
        if (fileManager.fileExistsAtPath(normalized)) {
            fileManager.removeItemAtPath(normalized, error = null)
        }
    }

    private fun normalizedPath(path: String): String {
        require(path.startsWith('/'))
        require(!path.split('/').contains(".."))
        return path
    }

    private fun safeFileName(name: String): String {
        val sanitized = name.substringAfterLast('/').substringAfterLast('\\')
            .filter { it.isLetterOrDigit() || it in "._-" }
            .take(96)
        return sanitized.takeIf { it.endsWith(".pvault", ignoreCase = true) }
            ?: "passvault-backup.pvault"
    }

    private fun randomToken(): String = platform.Foundation.NSUUID.UUID().UUIDString.lowercase()

    @Suppress("DEPRECATION")
    private fun resolvePresenter(): UIViewController? {
        val app = UIApplication.sharedApplication
        val sceneWindow = app.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?.let { scene ->
                val windows = scene.windows.filterIsInstance<UIWindow>()
                windows.firstOrNull { it.isKeyWindow() } ?: windows.firstOrNull()
            }
        var controller = (sceneWindow ?: app.keyWindow)?.rootViewController
        while (controller?.presentedViewController != null) {
            controller = controller.presentedViewController
        }
        return controller
    }

    private fun NSData.toByteArray(): ByteArray {
        val size = length.toInt()
        if (size == 0) return ByteArray(0)
        return ByteArray(size).also { destination ->
            destination.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
        }
    }

    private fun ByteArray.toNSData(): NSData = if (isEmpty()) {
        NSData.create(bytes = null, length = 0u)
    } else {
        usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = size.toULong()) }
    }

    private fun NSTemporaryDirectoryPath(): String = platform.Foundation.NSTemporaryDirectory().trimEnd('/')

    private companion object {
        const val MAX_BACKUP_BYTES = 128L * 1024L * 1024L
    }
}
