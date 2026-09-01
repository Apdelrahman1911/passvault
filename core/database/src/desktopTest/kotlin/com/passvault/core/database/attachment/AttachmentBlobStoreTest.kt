package com.passvault.core.database.attachment

import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.Path
import java.io.IOException
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AttachmentBlobStoreTest {
    @Test
    fun `storage full while initializing directories is classified`() = runTest {
        val parent = Files.createTempDirectory("passvault-blob-capacity-")
        val fileSystem = object : ForwardingFileSystem(FileSystem.SYSTEM) {
            override fun createDirectory(dir: Path, mustCreate: Boolean) {
                throw IOException("No space left on device")
            }
        }
        val store = LocalAttachmentBlobStore(
            rootPath = parent.resolve("missing-root").toString(),
            fileSystem = fileSystem,
        )

        try {
            assertFailsWith<AttachmentStorageFullException> {
                store.writeAtomically("objects/00000000-0000-4000-8000-000000000001.pva") { Unit }
            }
        } finally {
            parent.toFile().deleteRecursively()
        }
    }
}
