package com.passvault.core.testing.fakes

import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.TagId
import com.passvault.core.testing.TestData
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FolderTagRepositoryTest {
    @Test
    fun `folder failure injection applies to the next operation only`() = runTest {
        val repository = FakeFolderRepository()
        val folder = TestData.folder()
        repository.setShouldFail(true)

        assertTrue(repository.save(folder).isFailure)
        assertTrue(repository.save(folder).isSuccess)
    }

    @Test
    fun `folder mutations reject missing records and invalid order`() = runTest {
        val repository = FakeFolderRepository()
        val folder = TestData.folder()
        repository.setupFolders(folder)

        assertTrue(repository.delete(FolderId("missing")).isFailure)
        assertTrue(repository.reorder(FolderId("missing"), 0).isFailure)
        assertTrue(repository.reorder(folder.id, -1).isFailure)
    }

    @Test
    fun `deleting a folder reparents its direct children like production`() = runTest {
        val repository = FakeFolderRepository()
        val root = TestData.folder(id = "root")
        val parent = TestData.folder(id = "parent", parentId = root.id.value)
        val child = TestData.folder(id = "child", parentId = parent.id.value)
        repository.setupFolders(root, parent, child)

        repository.delete(parent.id).getOrThrow()

        assertEquals(root.id, repository.getById(child.id).getOrThrow()?.parentId)
    }

    @Test
    fun `folder save normalizes names and preserves production managed metadata`() = runTest {
        val repository = FakeFolderRepository()
        val original = TestData.folder(id = "folder").copy(createdAt = Instant.fromEpochSeconds(10))
        repository.setupFolders(original)

        repository.save(
            original.copy(
                name = "  Updated  ",
                createdAt = Instant.fromEpochSeconds(20),
            ),
        ).getOrThrow()

        val stored = repository.getById(original.id).getOrThrow()
        assertEquals("Updated", stored?.name)
        assertEquals(Instant.fromEpochSeconds(10), stored?.createdAt)
    }

    @Test
    fun `folder save rejects duplicates missing parents cycles and invalid metadata`() = runTest {
        val repository = FakeFolderRepository()
        val first = TestData.folder(id = "first").copy(name = "Personal")
        val child = TestData.folder(id = "child", parentId = first.id.value)
        repository.setupFolders(first, child)

        assertTrue(repository.save(TestData.folder(id = "duplicate").copy(name = " personal ")).isFailure)
        assertTrue(repository.save(TestData.folder(id = "orphan", parentId = "missing")).isFailure)
        assertTrue(repository.save(first.copy(parentId = child.id)).isFailure)
        assertTrue(repository.save(TestData.folder(id = "negative").copy(sortOrder = -1)).isFailure)
        assertTrue(repository.save(TestData.folder(id = "bad-icon").copy(icon = "\u0000")).isFailure)
    }

    @Test
    fun `folder reads reject malformed stored hierarchy`() = runTest {
        val repository = FakeFolderRepository()
        repository.setupFolders(TestData.folder(id = "orphan", parentId = "missing"))

        assertTrue(repository.getAll().isFailure)
        assertTrue(repository.getById(FolderId("orphan")).isFailure)
        assertNull(repository.getById(FolderId("absent")).getOrThrow())
    }

    @Test
    fun `tag failure injection and missing deletion match production semantics`() = runTest {
        val repository = FakeTagRepository()
        val tag = TestData.tag()
        repository.setShouldFail(true)

        assertTrue(repository.getById(tag.id).isFailure)
        assertTrue(repository.save(tag).isSuccess)
        assertTrue(repository.delete(TagId("missing")).isFailure)
    }

    @Test
    fun `tag save normalizes names and rejects duplicates and invalid metadata`() = runTest {
        val repository = FakeTagRepository()
        val existing = TestData.tag(id = "existing").copy(name = "Work")
        repository.setupTags(existing)

        val normalized = TestData.tag(id = "normalized").copy(name = "  Personal  ")
        repository.save(normalized).getOrThrow()

        assertEquals("Personal", repository.getById(normalized.id).getOrThrow()?.name)
        assertTrue(repository.save(TestData.tag(id = "duplicate").copy(name = " work ")).isFailure)
        assertTrue(repository.save(TestData.tag(id = "blank").copy(name = "   ")).isFailure)
        assertTrue(repository.save(TestData.tag(id = "bad-color").copy(color = "\u0000")).isFailure)
    }
}
