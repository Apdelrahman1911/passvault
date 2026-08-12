@file:OptIn(
    kotlinx.cinterop.BetaInteropApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class,
)

package com.passvault.shared.platform

import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentException
import com.passvault.core.domain.repository.AttachmentPolicy
import com.passvault.feature.credential.AttachmentFileSelectionCancelled
import com.passvault.feature.credential.AttachmentFileStore
import com.passvault.feature.credential.AttachmentOutputAction
import com.passvault.feature.credential.PreparedAttachmentOutput
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
import platform.Foundation.NSFileType
import platform.Foundation.NSFileTypeRegular
import platform.Foundation.NSInputStream
import platform.Foundation.NSOutputStream
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSUUID
import platform.Foundation.inputStreamWithFileAtPath
import platform.Foundation.outputStreamToFileAtPath
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIModalPresentationFormSheet
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

class IosAttachmentFileStore(
    private val fileManager: NSFileManager = NSFileManager.defaultManager,
) : AttachmentFileStore {
    private var activeDelegate: NSObject? = null
    private var activeController: UIViewController? = null
    private var presentationActive = false

    private val cacheRoot: String by lazy {
        fileManager.URLForDirectory(
            directory = NSCachesDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )?.path?.also { root -> cleanupIosAttachmentCache(root, fileManager) }
            ?: error("The iOS cache directory is unavailable")
    }

    @Suppress("TooGenericExceptionCaught") // Every native adoption failure must remove both picker and owned copies.
    override suspend fun selectForImport(): Result<AttachmentContentSource> = try {
        val selected = presentImportPicker()
        val selectedPath = selected.getOrElse { return Result.failure(it) }
        try {
            val adoptedPath = withContext(Dispatchers.Default) { adoptSelectedImport(selectedPath) }
            try {
                Result.success(IosAttachmentSource(adoptedPath, cacheRoot, fileManager))
            } catch (error: Exception) {
                deleteOwnedIosAttachmentDirectory(adoptedPath, cacheRoot, fileManager)
                throw error
            }
        } catch (error: Exception) {
            deleteExactPath(selectedPath)
            throw error
        }
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
        val fileName = AttachmentPolicy.validateFileName(attachment.fileName)
        val directory = "$cacheRoot/$IOS_ATTACHMENT_DIRECTORY_PREFIX${randomToken()}"
        check(
            fileManager.createDirectoryAtPath(
                path = directory,
                withIntermediateDirectories = true,
                attributes = IOS_PLAINTEXT_PROTECTION,
                error = null,
            ),
        )
        val path = "$directory/$fileName"
        Result.success(
            IosPreparedAttachmentOutput(
                path = path,
                directory = directory,
                action = action,
                presenter = ::presentOutput,
                fileManager = fileManager,
            ),
        )
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (error: AttachmentException) {
        Result.failure(error)
    } catch (_: Exception) {
        Result.failure(IllegalStateException("The attachment output could not be prepared"))
    }

    private suspend fun presentImportPicker(): Result<String> = withContext(Dispatchers.Main) {
        if (presentationActive) {
            return@withContext Result.failure(IllegalStateException("Another document action is active"))
        }
        val presenter = resolveAttachmentPresenter()
            ?: return@withContext Result.failure(IllegalStateException("The document picker is unavailable"))
        suspendCancellableCoroutine { continuation ->
            presentationActive = true
            lateinit var picker: UIDocumentPickerViewController
            var finished = false
            var selectedPath: String? = null

            fun finish(result: Result<String>?) {
                if (finished) return
                finished = true
                presentationActive = false
                activeDelegate = null
                activeController = null
                if (result != null && continuation.isActive) {
                    selectedPath = result.getOrNull()
                    continuation.resume(result)
                } else {
                    result?.getOrNull()?.let(::deleteExactPath)
                }
            }

            val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAtURLs: List<*>,
                ) {
                    val path = (didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.path
                    finish(
                        path?.let(Result.Companion::success)
                            ?: Result.failure(IllegalStateException("No attachment file was selected")),
                    )
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    finish(Result.failure(AttachmentFileSelectionCancelled()))
                }
            }
            picker = UIDocumentPickerViewController(
                forOpeningContentTypes = listOf(UTTypeData),
                asCopy = true,
            )
            picker.delegate = delegate
            activeDelegate = delegate
            activeController = picker
            continuation.invokeOnCancellation {
                dispatch_async(dispatch_get_main_queue()) {
                    selectedPath?.let(::deleteExactPath)
                    picker.dismissViewControllerAnimated(true, completion = null)
                    finish(null)
                }
            }
            try {
                presenter.presentViewController(picker, animated = true, completion = null)
            } catch (_: Exception) {
                finish(Result.failure(IllegalStateException("The document picker could not be opened")))
            }
        }
    }

    private suspend fun presentOutput(path: String, action: AttachmentOutputAction): Result<Unit> =
        withContext(Dispatchers.Main) {
            if (presentationActive) {
                return@withContext Result.failure(IllegalStateException("Another document action is active"))
            }
            val presenter = resolveAttachmentPresenter()
                ?: return@withContext Result.failure(IllegalStateException("The document presenter is unavailable"))
            when (action) {
                AttachmentOutputAction.EXPORT -> presentExportPicker(presenter, path)
                AttachmentOutputAction.OPEN -> presentActivityController(presenter, path)
            }
        }

    @Suppress("TooGenericExceptionCaught") // UIKit presentation failures must reset the single-presentation guard.
    private suspend fun presentExportPicker(
        presenter: UIViewController,
        path: String,
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        presentationActive = true
        lateinit var picker: UIDocumentPickerViewController
        var finished = false

        fun finish(result: Result<Unit>?) {
            if (finished) return
            finished = true
            presentationActive = false
            activeDelegate = null
            activeController = null
            if (result != null && continuation.isActive) continuation.resume(result)
        }

        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>,
            ) {
                finish(Result.success(Unit))
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                finish(Result.failure(AttachmentFileSelectionCancelled()))
            }
        }
        picker = UIDocumentPickerViewController(
            forExportingURLs = listOf(NSURL.fileURLWithPath(path)),
            asCopy = true,
        )
        picker.delegate = delegate
        activeDelegate = delegate
        activeController = picker
        continuation.invokeOnCancellation {
            dispatch_async(dispatch_get_main_queue()) {
                picker.dismissViewControllerAnimated(true, completion = null)
                finish(null)
            }
        }
        try {
            presenter.presentViewController(picker, animated = true, completion = null)
        } catch (_: Exception) {
            finish(Result.failure(IllegalStateException("The attachment export picker could not be opened")))
        }
    }

    @Suppress("TooGenericExceptionCaught") // UIKit presentation failures must reset the single-presentation guard.
    private suspend fun presentActivityController(
        presenter: UIViewController,
        path: String,
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        presentationActive = true
        val controller = UIActivityViewController(
            activityItems = listOf(NSURL.fileURLWithPath(path)),
            applicationActivities = null,
        )
        var finished = false

        fun finish(result: Result<Unit>?) {
            if (finished) return
            finished = true
            presentationActive = false
            activeController = null
            if (result != null && continuation.isActive) continuation.resume(result)
        }

        controller.completionWithItemsHandler = { _, _, _, _ -> finish(Result.success(Unit)) }
        controller.modalPresentationStyle = UIModalPresentationFormSheet
        controller.popoverPresentationController?.apply {
            sourceView = presenter.view
            sourceRect = presenter.view.bounds
            permittedArrowDirections = NO_POPOVER_ARROW_DIRECTIONS
        }
        activeController = controller
        continuation.invokeOnCancellation {
            dispatch_async(dispatch_get_main_queue()) {
                controller.dismissViewControllerAnimated(true, completion = null)
                finish(null)
            }
        }
        try {
            presenter.presentViewController(controller, animated = true, completion = null)
        } catch (_: Exception) {
            finish(Result.failure(IllegalStateException("The attachment viewer could not be opened")))
        }
    }

    private fun deleteExactPath(path: String) {
        if (path.startsWith('/') && !path.split('/').contains("..") && fileManager.fileExistsAtPath(path)) {
            fileManager.removeItemAtPath(path, error = null)
        }
    }

    @Suppress("TooGenericExceptionCaught") // Foundation failures must clean the partially adopted plaintext copy.
    private fun adoptSelectedImport(sourcePath: String): String {
        require(sourcePath.startsWith('/') && !sourcePath.split('/').contains(".."))
        val sourceAttributes = fileManager.attributesOfItemAtPath(sourcePath, error = null)
            ?: error("The selected attachment disappeared")
        require(sourceAttributes[NSFileType] == NSFileTypeRegular)
        val size = (sourceAttributes["NSFileSize"] as? Number)?.toLong()
            ?: error("The attachment size is unavailable")
        AttachmentPolicy.validateFileSize(size)
        val fileName = AttachmentPolicy.validateFileName(sourcePath.substringAfterLast('/'))
        val directory = "$cacheRoot/$IOS_ATTACHMENT_DIRECTORY_PREFIX${randomToken()}"
        check(
            fileManager.createDirectoryAtPath(
                path = directory,
                withIntermediateDirectories = false,
                attributes = IOS_PLAINTEXT_PROTECTION,
                error = null,
            ),
        )
        val destination = "$directory/$fileName"
        return try {
            val moved = fileManager.moveItemAtPath(sourcePath, destination, error = null)
            if (!moved) {
                check(fileManager.copyItemAtPath(sourcePath, destination, error = null))
                check(fileManager.removeItemAtPath(sourcePath, error = null))
            }
            check(fileManager.setAttributes(IOS_PLAINTEXT_PROTECTION, ofItemAtPath = destination, error = null))
            destination
        } catch (error: Exception) {
            fileManager.removeItemAtPath(directory, error = null)
            throw error
        }
    }
}

private class IosAttachmentSource(
    private val path: String,
    private val cacheRoot: String,
    private val fileManager: NSFileManager,
) : AttachmentContentSource {
    private var stream: NSInputStream? = null
    private val attributes = fileManager.attributesOfItemAtPath(path, error = null)
        ?: error("The selected attachment disappeared")

    init {
        require(attributes[NSFileType] == NSFileTypeRegular)
    }

    override val displayName: String = AttachmentPolicy.validateFileName(path.substringAfterLast('/'))
    override val claimedMimeType: String? = null
    override val declaredSizeBytes: Long = (attributes["NSFileSize"] as? Number)?.toLong()
        ?.also(AttachmentPolicy::validateFileSize)
        ?: error("The attachment size is unavailable")

    override suspend fun read(buffer: ByteArray): Int = withContext(Dispatchers.Default) {
        val input = stream ?: NSInputStream.inputStreamWithFileAtPath(path)
            ?.also {
                it.open()
                stream = it
            }
            ?: error("The selected attachment disappeared")
        val count = buffer.usePinned { pinned ->
            input.read(pinned.addressOf(0).reinterpret(), buffer.size.toULong())
        }
        check(count >= 0) { "The selected attachment could not be read" }
        if (count == 0L) -1 else count.toInt()
    }

    override suspend fun close() = withContext(NonCancellable + Dispatchers.Default) {
        stream?.close()
        stream = null
        deleteOwnedIosAttachmentDirectory(path, cacheRoot, fileManager)
    }
}

private class IosPreparedAttachmentOutput(
    private val path: String,
    private val directory: String,
    private val action: AttachmentOutputAction,
    private val presenter: suspend (String, AttachmentOutputAction) -> Result<Unit>,
    private val fileManager: NSFileManager,
) : PreparedAttachmentOutput, AttachmentContentSink {
    private var stream: NSOutputStream? = null
    private var bytesWritten = 0L
    private var sinkCommitted = false
    private var handedOff = false

    override val sink: AttachmentContentSink
        get() = this

    override suspend fun write(buffer: ByteArray, byteCount: Int) = withContext(Dispatchers.Default) {
        check(!sinkCommitted)
        require(byteCount in 0..buffer.size)
        bytesWritten += byteCount
        require(bytesWritten <= AttachmentPolicy.MAX_FILE_SIZE_BYTES)
        val output = stream ?: NSOutputStream.outputStreamToFileAtPath(path, append = false)
            .also {
                it.open()
                stream = it
            }
        var offset = 0
        while (offset < byteCount) {
            val written = buffer.usePinned { pinned ->
                output.write(
                    buffer = pinned.addressOf(offset).reinterpret(),
                    maxLength = (byteCount - offset).toULong(),
                )
            }
            check(written > 0) { "The attachment output could not be written" }
            offset += written.toInt()
        }
    }

    override suspend fun commit() {
        check(!sinkCommitted)
        withContext(NonCancellable + Dispatchers.Default) {
            stream?.close()
            stream = null
            if (!fileManager.fileExistsAtPath(path)) {
                // Ensure an empty attachment still has a real output file.
                val empty = NSOutputStream.outputStreamToFileAtPath(path, append = false)
                empty.open()
                empty.close()
            }
            check(fileManager.setAttributes(IOS_PLAINTEXT_PROTECTION, ofItemAtPath = path, error = null))
        }
        sinkCommitted = true
    }

    override suspend fun present(): Result<Unit> {
        check(sinkCommitted)
        check(!handedOff)
        return try {
            presenter(path, action).also { result -> handedOff = result.isSuccess }
        } finally {
            withContext(NonCancellable + Dispatchers.Default) { deleteOwnedOutput() }
        }
    }

    override suspend fun abort() = withContext(NonCancellable + Dispatchers.Default) {
        if (handedOff) return@withContext
        stream?.close()
        stream = null
        deleteOwnedOutput()
    }

    private fun deleteOwnedOutput() {
        if (directory.startsWith('/') && !directory.split('/').contains("..")) {
            fileManager.removeItemAtPath(directory, error = null)
        }
    }
}

private fun cleanupIosAttachmentCache(cacheRoot: String, fileManager: NSFileManager) {
    fileManager.contentsOfDirectoryAtPath(cacheRoot, error = null)
        ?.filterIsInstance<String>()
        .orEmpty()
        .filter(::isOwnedIosAttachmentDirectoryName)
        .forEach { directoryName ->
            fileManager.removeItemAtPath("$cacheRoot/$directoryName", error = null)
        }
}

private fun deleteOwnedIosAttachmentDirectory(
    path: String,
    cacheRoot: String,
    fileManager: NSFileManager,
) {
    if (!path.startsWith("$cacheRoot/") || path.split('/').contains("..")) return
    val relativePath = path.removePrefix("$cacheRoot/")
    val directoryName = relativePath.substringBefore('/')
    if (!isOwnedIosAttachmentDirectoryName(directoryName)) return
    fileManager.removeItemAtPath("$cacheRoot/$directoryName", error = null)
}

private fun isOwnedIosAttachmentDirectoryName(value: String): Boolean {
    if (!value.startsWith(IOS_ATTACHMENT_DIRECTORY_PREFIX)) return false
    val token = value.removePrefix(IOS_ATTACHMENT_DIRECTORY_PREFIX)
    return token.length == UUID_TEXT_LENGTH && token.indices.all { index ->
        if (index in UUID_HYPHEN_INDICES) {
            token[index] == '-'
        } else {
            token[index] in '0'..'9' || token[index] in 'a'..'f'
        }
    }
}

private fun resolveAttachmentPresenter(): UIViewController? {
    val window = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        ?.windows
        ?.filterIsInstance<UIWindow>()
        ?.let { windows -> windows.firstOrNull { it.isKeyWindow() } ?: windows.firstOrNull() }
    var controller = window?.rootViewController
    while (controller?.presentedViewController != null) controller = controller.presentedViewController
    return controller
}

private fun randomToken(): String = NSUUID.UUID().UUIDString.lowercase()

private const val IOS_ATTACHMENT_DIRECTORY_PREFIX = "passvault-attachment-"
private const val UUID_TEXT_LENGTH = 36
private const val NO_POPOVER_ARROW_DIRECTIONS = 0uL
private val UUID_HYPHEN_INDICES = setOf(8, 13, 18, 23)
private val IOS_PLAINTEXT_PROTECTION = mapOf<Any?, Any?>(NSFileProtectionKey to NSFileProtectionComplete)
