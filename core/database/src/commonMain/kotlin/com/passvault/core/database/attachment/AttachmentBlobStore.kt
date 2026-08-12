package com.passvault.core.database.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer

/** App-private storage for encrypted attachment objects. */
interface AttachmentBlobStore {
    suspend fun <T> writeAtomically(
        relativePath: String,
        writer: suspend (BufferedSink) -> T,
    ): T

    suspend fun <T> read(
        relativePath: String,
        maxBytes: Long,
        reader: suspend (BufferedSource, Long) -> T,
    ): T

    suspend fun delete(relativePath: String)
    suspend fun exists(relativePath: String): Boolean
    suspend fun removeUnreferencedObjects(referencedPaths: Set<String>)
}

/**
 * Okio-backed implementation shared by Android, iOS, macOS, Windows, and
 * Linux. Platform factories choose and protect the private root directory.
 */
class LocalAttachmentBlobStore(
    rootPath: String,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
) : AttachmentBlobStore {
    private val root = rootPath.toPath(normalize = true)
    private val objects = root / OBJECTS_DIRECTORY
    private val staging = root / STAGING_DIRECTORY

    override suspend fun <T> writeAtomically(
        relativePath: String,
        writer: suspend (BufferedSink) -> T,
    ): T = withContext(Dispatchers.Default) {
        initializeAndVerifyDirectories()
        val target = resolveObjectPath(relativePath)
        check(fileSystem.metadataOrNull(target) == null) { "The attachment object already exists" }
        val temporary = staging / (target.name + TEMPORARY_SUFFIX)
        fileSystem.delete(temporary, mustExist = false)
        try {
            val sink = fileSystem.sink(temporary, mustCreate = true).buffer()
            val result = try {
                writer(sink).also { sink.flush() }
            } finally {
                sink.close()
            }
            requireRegularFile(temporary)
            check(fileSystem.metadataOrNull(target) == null) { "The attachment object already exists" }
            fileSystem.atomicMove(temporary, target)
            requireRegularFile(target)
            result
        } finally {
            fileSystem.delete(temporary, mustExist = false)
        }
    }

    override suspend fun <T> read(
        relativePath: String,
        maxBytes: Long,
        reader: suspend (BufferedSource, Long) -> T,
    ): T = withContext(Dispatchers.Default) {
        require(maxBytes >= 0)
        initializeAndVerifyDirectories()
        val path = resolveObjectPath(relativePath)
        val metadata = requireRegularFile(path)
        val size = requireNotNull(metadata.size) { "The attachment object size is unavailable" }
        require(size in 0..maxBytes) { "The encrypted attachment object is too large" }
        val source = fileSystem.source(path).buffer()
        try {
            reader(source, size)
        } finally {
            source.close()
        }
    }

    override suspend fun delete(relativePath: String) = withContext(Dispatchers.Default) {
        initializeAndVerifyDirectories()
        val path = resolveObjectPath(relativePath)
        val metadata = fileSystem.metadataOrNull(path)
        if (metadata != null) {
            require(metadata.symlinkTarget == null && metadata.isRegularFile) {
                "The attachment object is not a regular file"
            }
            fileSystem.delete(path)
        }
    }

    override suspend fun exists(relativePath: String): Boolean = withContext(Dispatchers.Default) {
        initializeAndVerifyDirectories()
        val metadata = fileSystem.metadataOrNull(resolveObjectPath(relativePath))
        metadata != null && metadata.symlinkTarget == null && metadata.isRegularFile
    }

    override suspend fun removeUnreferencedObjects(referencedPaths: Set<String>) =
        withContext(Dispatchers.Default) {
            initializeAndVerifyDirectories()
            val validatedReferences = referencedPaths.onEach(::requireValidObjectPath)
            fileSystem.list(objects).forEach { path ->
                val relativePath = "$OBJECTS_DIRECTORY/${path.name}"
                requireValidObjectPath(relativePath)
                val metadata = fileSystem.metadata(path)
                require(metadata.symlinkTarget == null && metadata.isRegularFile) {
                    "An unsafe entry was found in attachment object storage"
                }
                if (relativePath !in validatedReferences) {
                    fileSystem.delete(path)
                }
            }
            fileSystem.list(staging).forEach { path ->
                requireValidStagingPath(path)
                val metadata = fileSystem.metadata(path)
                require(metadata.symlinkTarget == null && metadata.isRegularFile) {
                    "An unsafe entry was found in attachment staging storage"
                }
                fileSystem.delete(path)
            }
        }

    private fun initializeAndVerifyDirectories() {
        fileSystem.createDirectories(root)
        fileSystem.createDirectories(objects)
        fileSystem.createDirectories(staging)
        listOf(root, objects, staging).forEach { path ->
            val metadata = fileSystem.metadata(path)
            require(metadata.symlinkTarget == null && metadata.isDirectory) {
                "The attachment storage path must be a real directory"
            }
        }
        val canonicalRoot = fileSystem.canonicalize(root)
        require(fileSystem.canonicalize(objects).parent == canonicalRoot)
        require(fileSystem.canonicalize(staging).parent == canonicalRoot)
    }

    private fun resolveObjectPath(relativePath: String): Path {
        requireValidObjectPath(relativePath)
        val resolved = root.resolve(relativePath, normalize = true)
        require(resolved.parent == objects) { "The attachment object path escapes private storage" }
        return resolved
    }

    private fun requireValidObjectPath(relativePath: String) {
        require(relativePath.length == OBJECT_PATH_LENGTH)
        require(relativePath.startsWith("$OBJECTS_DIRECTORY/"))
        require(relativePath.endsWith(OBJECT_EXTENSION))
        val objectId = relativePath.removePrefix("$OBJECTS_DIRECTORY/").removeSuffix(OBJECT_EXTENSION)
        require(objectId.length == UUID_TEXT_LENGTH)
        require(objectId.indices.all { index ->
            if (index in UUID_HYPHEN_INDICES) objectId[index] == '-' else objectId[index].isLowerHexDigit()
        }) { "The attachment object identifier is invalid" }
    }

    private fun requireValidStagingPath(path: Path) {
        require(path.parent == staging)
        require(path.name.endsWith(TEMPORARY_SUFFIX))
        val objectName = path.name.removeSuffix(TEMPORARY_SUFFIX)
        requireValidObjectPath("$OBJECTS_DIRECTORY/$objectName")
    }

    private fun requireRegularFile(path: Path): okio.FileMetadata {
        val metadata = fileSystem.metadata(path)
        require(metadata.symlinkTarget == null && metadata.isRegularFile) {
            "The attachment object is not a regular file"
        }
        return metadata
    }

    private fun Char.isLowerHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f'

    private companion object {
        const val OBJECTS_DIRECTORY = "objects"
        const val STAGING_DIRECTORY = "staging"
        const val OBJECT_EXTENSION = ".pva"
        const val TEMPORARY_SUFFIX = ".tmp"
        const val UUID_TEXT_LENGTH = 36
        const val OBJECT_PATH_LENGTH = 8 + UUID_TEXT_LENGTH + OBJECT_EXTENSION.length
        val UUID_HYPHEN_INDICES = setOf(8, 13, 18, 23)
    }
}
