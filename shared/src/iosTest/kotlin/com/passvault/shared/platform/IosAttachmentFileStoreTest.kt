@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
)

package com.passvault.shared.platform

import kotlinx.cinterop.toKString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileOwnerAccountID
import platform.Foundation.NSFilePosixPermissions
import platform.Foundation.NSFileProtectionComplete
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSFileType
import platform.Foundation.NSFileTypeDirectory
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.posix.free
import platform.posix.geteuid
import platform.posix.getenv
import platform.posix.realpath
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IosAttachmentFileStoreTest {
    @Test
    fun `cancelled return after move removes the adopted plaintext directory`() = runTest {
        verifyCancelledAdoption(forceCopyFallback = false)
    }

    @Test
    fun `cancelled return after copy fallback removes both plaintext locations`() = runTest {
        verifyCancelledAdoption(forceCopyFallback = true)
    }

    private suspend fun TestScope.verifyCancelledAdoption(forceCopyFallback: Boolean) {
        withAdoptionFixture { fileManager, sourcePath, cacheRoot ->
            val worker = AttachmentQueuedDispatcher()
            var destination: String? = null
            val adoption = async {
                openIosAttachmentImport(
                    sourcePath,
                    cacheRoot,
                    fileManager,
                    protectPath = { destination = it },
                    dispatcher = worker,
                    moveFile = { source, target ->
                        if (forceCopyFallback) false else fileManager.moveItemAtPath(source, target, error = null)
                    },
                )
            }
            try {
                runCurrent()
                worker.runAll()
                assertTrue(fileManager.fileExistsAtPath(checkNotNull(destination)))
                assertFalse(fileManager.fileExistsAtPath(sourcePath))

                // The source exists, but its return continuation has not run yet.
                cancelAndDrainAdoption(adoption, worker)

                assertFailsWith<CancellationException> { adoption.await() }
                assertFalse(fileManager.fileExistsAtPath(checkNotNull(destination)))
                assertFalse(fileManager.fileExistsAtPath(sourcePath))
                assertTrue(fileManager.contentsOfDirectoryAtPath(cacheRoot, error = null).orEmpty().isEmpty())
            } finally {
                // Failed assertions must not strand NonCancellable cleanup on
                // the manually driven dispatcher or retain the fixture files.
                cancelAndDrainAdoption(adoption, worker)
            }
        }
    }

    @Test
    fun `successful adoption transfers directory cleanup to source close`() = runTest {
        withAdoptionFixture { fileManager, sourcePath, cacheRoot ->
            var destination: String? = null
            val source = openIosAttachmentImport(
                sourcePath,
                cacheRoot,
                fileManager,
                protectPath = { destination = it },
            )
            try {
                assertTrue(fileManager.fileExistsAtPath(checkNotNull(destination)))
                assertFalse(fileManager.fileExistsAtPath(sourcePath))
            } finally {
                source.close()
            }
            assertFalse(fileManager.fileExistsAtPath(checkNotNull(destination)))
            assertTrue(fileManager.contentsOfDirectoryAtPath(cacheRoot, error = null).orEmpty().isEmpty())
        }
    }

    @Test
    fun `destination protection failure removes the moved file and owned directory`() = runTest {
        withAdoptionFixture { fileManager, sourcePath, cacheRoot ->
            assertFailsWith<IllegalStateException> {
                openIosAttachmentImport(
                    sourcePath,
                    cacheRoot,
                    fileManager,
                    protectPath = { error("Synthetic protection failure") },
                )
            }
            assertFalse(fileManager.fileExistsAtPath(sourcePath))
            assertTrue(fileManager.contentsOfDirectoryAtPath(cacheRoot, error = null).orEmpty().isEmpty())
        }
    }

    @Test
    fun `cancelled admission removes the original picker copy without adopting it`() = runTest {
        withAdoptionFixture { fileManager, sourcePath, cacheRoot ->
            val worker = AttachmentQueuedDispatcher()
            var protectionCalls = 0
            val adoption = async {
                openIosAttachmentImport(
                    sourcePath,
                    cacheRoot,
                    fileManager,
                    protectPath = { protectionCalls++ },
                    dispatcher = worker,
                )
            }
            try {
                runCurrent()
                cancelAndDrainAdoption(adoption, worker)

                assertEquals(0, protectionCalls)
                assertFalse(fileManager.fileExistsAtPath(sourcePath))
                assertTrue(fileManager.contentsOfDirectoryAtPath(cacheRoot, error = null).orEmpty().isEmpty())
            } finally {
                cancelAndDrainAdoption(adoption, worker)
            }
        }
    }

    private suspend fun TestScope.cancelAndDrainAdoption(adoption: Job, worker: AttachmentQueuedDispatcher) {
        withContext(NonCancellable) {
            adoption.cancel()
            // The worker and caller each own one side of the return handoff;
            // cancellation adds another worker/caller pair for final cleanup.
            repeat(8) {
                worker.runAll()
                testScheduler.runCurrent()
                if (adoption.isCompleted) {
                    adoption.join()
                    return@withContext
                }
            }
            error("Synthetic adoption did not settle after draining both dispatchers")
        }
    }

    @Test
    fun `picker copy is protected before the import path is returned`() {
        withTemporaryFile { fileManager, path ->
            val events = mutableListOf<String>()

            val result = prepareIosAttachmentImportPath(
                path = path,
                protectPath = {
                    events += "protect"
                    protectIosAttachmentPath(fileManager, it)
                },
                deletePath = { events += "delete" },
            )

            assertEquals(path, result.getOrThrow())
            assertEquals(listOf("protect"), events)
            assertEquals(NSFileProtectionComplete, iosAttachmentProtectionAttributes()[NSFileProtectionKey])
        }
    }

    @Test
    fun `picker copy is deleted when immediate protection fails`() {
        withTemporaryFile { fileManager, path ->
            val events = mutableListOf<String>()

            val result = prepareIosAttachmentImportPath(
                path = path,
                protectPath = {
                    events += "protect"
                    error("protection unavailable")
                },
                deletePath = {
                    events += "delete"
                    assertTrue(fileManager.removeItemAtPath(it, error = null))
                },
            )

            assertTrue(result.isFailure)
            assertEquals(listOf("protect", "delete"), events)
            assertFalse(fileManager.fileExistsAtPath(path))
        }
    }

    private fun withTemporaryFile(block: (NSFileManager, String) -> Unit) {
        val temporaryDirectory = admittedTemporaryDirectory()
        val fileManager = NSFileManager.defaultManager
        val path = "${temporaryDirectory}passvault-picker-${NSUUID.UUID().UUIDString}"
        assertTrue(fileManager.createFileAtPath(path, contents = null, attributes = null))
        try {
            block(fileManager, path)
        } finally {
            if (fileManager.fileExistsAtPath(path)) fileManager.removeItemAtPath(path, error = null)
        }
    }

    private suspend fun withAdoptionFixture(block: suspend (NSFileManager, String, String) -> Unit) {
        val temporaryDirectory = admittedTemporaryDirectory()
        val fileManager = NSFileManager.defaultManager
        val root = "${temporaryDirectory}passvault-adoption-test-${NSUUID.UUID().UUIDString}"
        val cacheRoot = "$root/cache"
        val sourcePath = "$root/synthetic.txt"
        try {
            assertTrue(fileManager.createDirectoryAtPath(cacheRoot, true, null, error = null))
            assertTrue(fileManager.createFileAtPath(sourcePath, contents = null, attributes = null))
            block(fileManager, sourcePath, cacheRoot)
        } finally {
            fileManager.removeItemAtPath(root, error = null)
        }
    }
}

/**
 * The caller must create/own an empty private synthetic parent and arrange the
 * guest's real Foundation temporary directory beneath it before these tests.
 * This is mandatory even outside the audit: lost environment forwarding must
 * fail before any fixture access, not silently use a host/default directory.
 * The receipt is not creation ownership, cleanup or physical protection proof.
 */
private fun admittedTemporaryDirectory(): String {
    val parent = checkNotNull(getenv("PASSVAULT_IOS_TEST_PARENT")) {
        "An explicitly owned synthetic iOS test parent is required"
    }.toKString()
    check(parent.length <= 2048 && parent.startsWith('/'))
    check(parent.all { it.isLetterOrDigit() || it in "/-._" })
    check(parent.split('/').drop(1).none { it.isEmpty() || it == "." || it == ".." })
    check(Regex("passvault-ios-test-parent-[0-9a-f]{32}").matches(parent.substringAfterLast('/')))

    val resolvedParent = resolveFixturePath(parent)
    check(resolvedParent == parent) { "The supplied synthetic parent must already be canonical" }
    val temporaryDirectory = resolveFixturePath(NSTemporaryDirectory())
    check(temporaryDirectory.length <= 2048 && temporaryDirectory.all { it.isLetterOrDigit() || it in "/-._" })
    check(temporaryDirectory == parent || temporaryDirectory.startsWith("$parent/")) {
        "Foundation temporary storage is outside the owned synthetic parent"
    }

    val attributes = checkNotNull(NSFileManager.defaultManager.attributesOfItemAtPath(parent, error = null))
    check(attributes[NSFileType] == NSFileTypeDirectory)
    check((attributes[NSFilePosixPermissions] as? Number)?.toInt() == 0b111000000)
    check((attributes[NSFileOwnerAccountID] as? Number)?.toLong() == geteuid().toLong())
    println("PASSVAULT_IOS_FIXTURE_PARENT\t$parent\t$temporaryDirectory")
    return "$temporaryDirectory/"
}

private fun resolveFixturePath(path: String): String {
    val resolved = checkNotNull(realpath(path, null)) { "The synthetic fixture directory is unavailable" }
    return try {
        resolved.toKString()
    } finally {
        free(resolved)
    }
}

private class AttachmentQueuedDispatcher : CoroutineDispatcher() {
    private val tasks = ArrayDeque<Runnable>()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        tasks.addLast(block)
    }

    fun runAll() {
        repeat(32) {
            val next = tasks.removeFirstOrNull() ?: return
            next.run()
        }
        check(tasks.isEmpty()) { "Synthetic adoption exceeded the queued-task drain bound" }
    }
}
