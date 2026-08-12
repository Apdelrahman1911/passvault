package com.passvault.feature.vault.presentation

import app.cash.turbine.test
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.core.testing.fakes.FakeFolderRepository
import com.passvault.core.testing.fakes.FakeTagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModelTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var credentialRepository: FakeCredentialRepository
    private lateinit var folderRepository: FakeFolderRepository
    private lateinit var tagRepository: FakeTagRepository

    @BeforeTest
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        credentialRepository = FakeCredentialRepository()
        folderRepository = FakeFolderRepository()
        tagRepository = FakeTagRepository()
    }

    @AfterTest
    fun tearDown() {
        credentialRepository.reset()
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load publishes credentials folders and tags`() = runTest(dispatcher) {
        credentialRepository.setupCredentials(
            TestData.credential(id = "one", title = "One"),
            TestData.credential(id = "two", title = "Two"),
        )
        folderRepository.setupFolders(TestData.folder(id = "work", name = "Work"))
        tagRepository.setupTags(TestData.tag(id = "important", name = "Important"))

        val viewModel = createViewModel()
        assertTrue(viewModel.state.value.isLoading)

        runCurrent()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(2, viewModel.state.value.credentials.size)
        assertEquals(1, viewModel.state.value.folders.size)
        assertEquals(1, viewModel.state.value.tags.size)
        assertEquals(2, viewModel.state.value.filteredCredentials.size)
    }

    @Test
    fun `load failure is redacted and retryable`() = runTest(dispatcher) {
        credentialRepository.setShouldFail(IllegalStateException("C:\\private\\vault.db"))
        val viewModel = createViewModel()

        runCurrent()

        assertEquals(
            Res.string.error_vault_load,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
        assertTrue(viewModel.state.value.canRetryLoad)

        viewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
        runCurrent()
        assertNull(viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.canRetryLoad)
    }

    @Test
    fun `search matches title and complete unlocked username without case sensitivity`() = runTest(dispatcher) {
        credentialRepository.setupCredentials(
            TestData.credential(id = "mail", title = "Personal Mail", username = "Ada.Lovelace"),
            TestData.credential(id = "bank", title = "Bank", username = "different-user"),
        )
        val viewModel = createLoadedViewModel()

        viewModel.onEvent(VaultViewModel.VaultEvent.OnSearchQueryChanged("LOVELACE"))

        assertEquals(listOf(CredentialId("mail")), viewModel.state.value.filteredCredentials.map { it.id })
    }

    @Test
    fun `search input is bounded and clearing it restores the list`() = runTest(dispatcher) {
        credentialRepository.setupCredentials(TestData.credential(id = "one", title = "One"))
        val viewModel = createLoadedViewModel()

        viewModel.onEvent(
            VaultViewModel.VaultEvent.OnSearchQueryChanged("🔐".repeat(1_000)),
        )
        assertEquals(256, viewModel.state.value.searchQuery.codePointLength())
        assertEquals("🔐".repeat(256), viewModel.state.value.searchQuery)
        assertTrue(viewModel.state.value.filteredCredentials.isEmpty())

        viewModel.onEvent(VaultViewModel.VaultEvent.OnSearchDismiss)
        assertEquals("", viewModel.state.value.searchQuery)
        assertEquals(1, viewModel.state.value.filteredCredentials.size)
    }

    @Test
    fun `folder and tag filters are mutually exclusive`() = runTest(dispatcher) {
        credentialRepository.setupCredentials(
            TestData.credential(id = "work", folderId = "folder", tagIds = setOf("tag")),
            TestData.credential(id = "other"),
        )
        val viewModel = createLoadedViewModel()

        viewModel.onEvent(VaultViewModel.VaultEvent.OnFolderSelected("folder"))
        assertEquals(FolderId("folder"), viewModel.state.value.selectedFolderId)
        assertEquals(listOf(CredentialId("work")), viewModel.state.value.filteredCredentials.map { it.id })

        viewModel.onEvent(VaultViewModel.VaultEvent.OnTagSelected("tag"))
        assertNull(viewModel.state.value.selectedFolderId)
        assertEquals(TagId("tag"), viewModel.state.value.selectedTagId)
        assertEquals(listOf(CredentialId("work")), viewModel.state.value.filteredCredentials.map { it.id })
    }

    @Test
    fun `refresh removes selections that no longer exist`() = runTest(dispatcher) {
        val folder = TestData.folder(id = "folder", name = "Folder")
        val tag = TestData.tag(id = "tag", name = "Tag")
        folderRepository.setupFolders(folder)
        tagRepository.setupTags(tag)
        val viewModel = createLoadedViewModel()

        viewModel.onEvent(VaultViewModel.VaultEvent.OnFolderSelected(folder.id.value))
        folderRepository.delete(folder.id)
        viewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
        runCurrent()
        assertNull(viewModel.state.value.selectedFolderId)

        viewModel.onEvent(VaultViewModel.VaultEvent.OnTagSelected(tag.id.value))
        tagRepository.delete(tag.id)
        viewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
        runCurrent()
        assertNull(viewModel.state.value.selectedTagId)
    }

    @Test
    fun `favorite and health filters use repository summary state`() = runTest(dispatcher) {
        val favorite = TestData.credential(id = "favorite", isFavorite = true)
        val weak = TestData.credential(id = "weak")
        credentialRepository.setupCredentials(favorite, weak)
        credentialRepository.updateHealth(
            weak.id,
            PasswordHealth(
                score = PasswordScore.WEAK,
                isDuplicate = false,
                isWeak = true,
                isOld = false,
                ageDays = 2,
            ),
        )
        val viewModel = createLoadedViewModel()

        viewModel.onEvent(
            VaultViewModel.VaultEvent.OnFilterChanged(VaultViewModel.CredentialFilter.FAVORITES),
        )
        assertEquals(listOf(favorite.id), viewModel.state.value.filteredCredentials.map { it.id })

        viewModel.onEvent(
            VaultViewModel.VaultEvent.OnFilterChanged(VaultViewModel.CredentialFilter.WEAK_PASSWORDS),
        )
        assertEquals(listOf(weak.id), viewModel.state.value.filteredCredentials.map { it.id })
    }

    @Test
    fun `name sorting is deterministic in both directions`() = runTest(dispatcher) {
        credentialRepository.setupCredentials(
            TestData.credential(id = "z", title = "Zulu"),
            TestData.credential(id = "a", title = "alpha"),
        )
        val viewModel = createLoadedViewModel()

        viewModel.onEvent(
            VaultViewModel.VaultEvent.OnSortChanged(VaultViewModel.SortOrder.NAME_ASC),
        )
        assertEquals(listOf("alpha", "Zulu"), viewModel.state.value.filteredCredentials.map { it.title })

        viewModel.onEvent(
            VaultViewModel.VaultEvent.OnSortChanged(VaultViewModel.SortOrder.NAME_DESC),
        )
        assertEquals(listOf("Zulu", "alpha"), viewModel.state.value.filteredCredentials.map { it.title })
    }

    @Test
    fun `new folder validates blank and duplicate names`() = runTest(dispatcher) {
        folderRepository.setupFolders(TestData.folder(id = "work", name = "Work"))
        val viewModel = createLoadedViewModel()

        viewModel.onEvent(VaultViewModel.VaultEvent.OnNewFolderClick)
        viewModel.onEvent(VaultViewModel.VaultEvent.OnCreateFolderClick)
        runCurrent()
        assertEquals(
            Res.string.error_folder_name_required,
            (viewModel.state.value.folderError as UiText.Resource).resource,
        )

        viewModel.onEvent(VaultViewModel.VaultEvent.OnNewFolderNameChanged(" work "))
        viewModel.onEvent(VaultViewModel.VaultEvent.OnCreateFolderClick)
        runCurrent()
        assertEquals(
            Res.string.error_folder_duplicate,
            (viewModel.state.value.folderError as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.canCreateFolder)
    }

    @Test
    fun `new folder name keeps complete supplementary characters at its boundary`() = runTest(dispatcher) {
        val viewModel = createLoadedViewModel()

        viewModel.onEvent(VaultViewModel.VaultEvent.OnNewFolderNameChanged("🔐".repeat(300)))

        assertEquals(257, viewModel.state.value.newFolderName.codePointLength())
        assertEquals("🔐".repeat(257), viewModel.state.value.newFolderName)
        assertEquals(
            Res.string.error_folder_name_too_long,
            (viewModel.state.value.folderError as UiText.Resource).resource,
        )
    }

    @Test
    fun `new folder is persisted once and closes the dialog`() = runTest(dispatcher) {
        val viewModel = createLoadedViewModel()
        viewModel.onEvent(VaultViewModel.VaultEvent.OnNewFolderClick)
        viewModel.onEvent(VaultViewModel.VaultEvent.OnNewFolderNameChanged("Personal"))

        repeat(2) {
            viewModel.onEvent(VaultViewModel.VaultEvent.OnCreateFolderClick)
        }
        runCurrent()

        assertEquals(1, viewModel.state.value.folders.count { it.name == "Personal" })
        assertFalse(viewModel.state.value.showNewFolderDialog)
        assertFalse(viewModel.state.value.isCreatingFolder)
    }

    @Test
    fun `deleting selected folder confirms then returns credentials to all items`() = runTest(dispatcher) {
        val folder = TestData.folder(id = "personal", name = "Personal")
        folderRepository.setupFolders(folder)
        credentialRepository.setupCredentials(
            TestData.credential(id = "mail", folderId = folder.id.value),
        )
        val viewModel = createLoadedViewModel()
        viewModel.onEvent(VaultViewModel.VaultEvent.OnFolderSelected(folder.id.value))

        viewModel.onEvent(VaultViewModel.VaultEvent.OnDeleteFolderClick(folder.id))
        assertEquals(folder, viewModel.state.value.folderPendingDeletion)

        viewModel.onEvent(VaultViewModel.VaultEvent.OnConfirmDeleteFolder)
        runCurrent()

        assertTrue(viewModel.state.value.folders.isEmpty())
        assertNull(viewModel.state.value.selectedFolderId)
        assertNull(viewModel.state.value.folderPendingDeletion)
        assertFalse(viewModel.state.value.isDeletingFolder)
        assertEquals(
            listOf(CredentialId("mail")),
            viewModel.state.value.filteredCredentials.map { it.id },
        )
    }

    @Test
    fun `folder deletion failure is recoverable and keeps confirmation open`() = runTest(dispatcher) {
        val folder = TestData.folder(id = "personal", name = "Personal")
        folderRepository.setupFolders(folder)
        val viewModel = createLoadedViewModel()
        viewModel.onEvent(VaultViewModel.VaultEvent.OnDeleteFolderClick(folder.id))
        folderRepository.setShouldFail(true)

        viewModel.onEvent(VaultViewModel.VaultEvent.OnConfirmDeleteFolder)
        runCurrent()

        assertEquals(folder, viewModel.state.value.folderPendingDeletion)
        assertFalse(viewModel.state.value.isDeletingFolder)
        assertEquals(
            Res.string.error_folder_delete,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
    }

    @Test
    fun `favorite toggle persists and refreshes the summary`() = runTest(dispatcher) {
        val credential = TestData.credential(id = "one", isFavorite = false)
        credentialRepository.setupCredentials(credential)
        val viewModel = createLoadedViewModel()

        viewModel.onEvent(VaultViewModel.VaultEvent.OnCredentialFavoriteClick(credential.id))
        runCurrent()

        assertTrue(viewModel.state.value.credentials.single().isFavorite)
    }

    @Test
    fun `rapid favorite changes are persisted in user-visible order`() = runTest(dispatcher) {
        val credential = TestData.credential(id = "one", isFavorite = false)
        credentialRepository.setupCredentials(credential)
        val reorderingRepository = ReorderingFavoriteRepository(credentialRepository)
        val viewModel = VaultViewModel(reorderingRepository, folderRepository, tagRepository)
        runCurrent()

        viewModel.onEvent(VaultViewModel.VaultEvent.OnCredentialFavoriteClick(credential.id))
        runCurrent()
        viewModel.onEvent(VaultViewModel.VaultEvent.OnCredentialFavoriteClick(credential.id))
        runCurrent()
        advanceTimeBy(110)
        runCurrent()

        assertFalse(viewModel.state.value.credentials.single().isFavorite)
        assertFalse(
            credentialRepository.getAllSummaries().getOrThrow().single().isFavorite,
        )
    }

    @Test
    fun `navigation events emit explicit one shot effects`() = runTest(dispatcher) {
        val viewModel = createLoadedViewModel()

        viewModel.effect.test {
            viewModel.onEvent(VaultViewModel.VaultEvent.OnAddCredentialClick)
            assertEquals(
                VaultViewModel.VaultEffect.NavigateToCredentialEdit(null),
                awaitItem(),
            )

            viewModel.onEvent(VaultViewModel.VaultEvent.OnLockClick)
            assertIs<VaultViewModel.VaultEffect.LockVault>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failed lock request remains unlocked and surfaces a retryable error`() = runTest(dispatcher) {
        val viewModel = createLoadedViewModel()

        viewModel.onEvent(VaultViewModel.VaultEvent.OnLockFailed)

        assertEquals(
            Res.string.error_settings_lock,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.canRetryLoad)
    }

    @Test
    fun `navigation effects are not replayed to a later screen`() = runTest(dispatcher) {
        val viewModel = createLoadedViewModel()
        viewModel.onEvent(VaultViewModel.VaultEvent.OnSettingsClick)

        viewModel.effect.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear for lock removes decrypted vault summaries and filters`() = runTest(dispatcher) {
        credentialRepository.setupCredentials(TestData.credential(id = "one", title = "One"))
        val viewModel = createLoadedViewModel()
        viewModel.onEvent(VaultViewModel.VaultEvent.OnSearchQueryChanged("One"))

        viewModel.clearForLock()

        assertTrue(viewModel.state.value.credentials.isEmpty())
        assertTrue(viewModel.state.value.filteredCredentials.isEmpty())
        assertEquals("", viewModel.state.value.searchQuery)
        assertFalse(viewModel.state.value.isLoading)
    }

    private fun createViewModel(): VaultViewModel =
        VaultViewModel(credentialRepository, folderRepository, tagRepository)

    private fun createLoadedViewModel(): VaultViewModel {
        val viewModel = createViewModel()
        dispatcher.scheduler.runCurrent()
        return viewModel
    }
}

private class ReorderingFavoriteRepository(
    private val delegate: FakeCredentialRepository,
) : CredentialRepository by delegate {
    override suspend fun updateFavorite(id: CredentialId, isFavorite: Boolean): Result<Unit> {
        if (isFavorite) {
            withContext(NonCancellable) { delay(100) }
        } else {
            delay(10)
        }
        return delegate.updateFavorite(id, isFavorite)
    }
}
