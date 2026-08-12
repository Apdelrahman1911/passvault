package com.passvault.feature.vault.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.Tag
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.takeCodePoints
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.FolderRepository
import com.passvault.core.domain.repository.TagRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.uuid.Uuid

class VaultViewModel(
    private val credentialRepository: CredentialRepository,
    private val folderRepository: FolderRepository,
    private val tagRepository: TagRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VaultState(isLoading = true))
    val state: StateFlow<VaultState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<VaultEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<VaultEffect> = _effect.asSharedFlow()
    private val loadMutex = Mutex()
    private val folderMutex = Mutex()
    private var loadJob: Job? = null
    private var favoriteRevision = 0L
    private val favoriteTargets = mutableMapOf<CredentialId, Boolean>()
    private val favoriteJobs = mutableMapOf<CredentialId, Job>()
    private var folderJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        loadJob?.cancel()
        val favoriteRevisionAtStart = favoriteRevision
        loadJob = viewModelScope.launch {
            loadMutex.withLock {
                _state.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null,
                        canRetryLoad = false,
                    )
                }
                try {
                    val credentials = credentialRepository.getAllSummaries().getOrThrow()
                    currentCoroutineContext().ensureActive()
                    val folders = folderRepository.getAll().getOrThrow()
                    currentCoroutineContext().ensureActive()
                    val tags = tagRepository.getAll().getOrThrow()
                    currentCoroutineContext().ensureActive()
                    _state.update { current ->
                        val loaded = current.copy(
                            isLoading = false,
                            credentials = reconcileLoadedCredentials(
                                loaded = credentials,
                                current = current.credentials,
                                favoriteRevisionAtStart = favoriteRevisionAtStart,
                            ),
                            folders = folders,
                            tags = tags,
                            selectedFolderId = current.selectedFolderId?.takeIf { selectedId ->
                                folders.any { it.id == selectedId }
                            },
                            selectedTagId = current.selectedTagId?.takeIf { selectedId ->
                                tags.any { it.id == selectedId }
                            },
                            folderPendingDeletion = current.folderPendingDeletion?.takeIf { pending ->
                                folders.any { it.id == pending.id }
                            },
                            errorMessage = null,
                            canRetryLoad = false,
                        )
                        loaded.copy(filteredCredentials = filterCredentials(loaded))
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = uiText(Res.string.error_vault_load),
                            canRetryLoad = true,
                        )
                    }
                }
            }
        }
    }

    /* This exhaustive router keeps every public UI event visible at one boundary. */
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun onEvent(event: VaultEvent) {
        when (event) {
            is VaultEvent.OnSearchQueryChanged -> {
                updateFilters {
                    it.copy(
                        searchQuery = event.query.takeCodePoints(MAX_SEARCH_QUERY_LENGTH),
                        errorMessage = null,
                        canRetryLoad = false,
                    )
                }
            }
            is VaultEvent.OnFolderSelected -> {
                updateFilters {
                    it.copy(
                        selectedFolderId = event.folderId?.let { FolderId(it) },
                        selectedTagId = null
                    )
                }
            }
            is VaultEvent.OnTagSelected -> {
                updateFilters {
                    it.copy(
                        selectedTagId = event.tagId?.let { TagId(it) },
                        selectedFolderId = null
                    )
                }
            }
            is VaultEvent.OnCredentialClick -> {
                _effect.tryEmit(VaultEffect.NavigateToCredentialDetail(event.credentialId))
            }
            is VaultEvent.OnCredentialFavoriteClick -> {
                toggleFavorite(event.credentialId)
            }
            VaultEvent.OnAddCredentialClick -> {
                _effect.tryEmit(VaultEffect.NavigateToCredentialEdit(null))
            }
            VaultEvent.OnSearchClick -> {
                _state.update { it.copy(isSearchActive = true) }
            }
            VaultEvent.OnSearchDismiss -> {
                updateFilters {
                    it.copy(
                        isSearchActive = false,
                        searchQuery = "",
                    )
                }
            }
            VaultEvent.OnSettingsClick -> {
                _effect.tryEmit(VaultEffect.NavigateToSettings)
            }
            VaultEvent.OnGeneratorClick -> {
                _effect.tryEmit(VaultEffect.NavigateToGenerator)
            }
            VaultEvent.OnTwoFactorCodesClick -> {
                _effect.tryEmit(VaultEffect.NavigateToTwoFactorCodes)
            }
            VaultEvent.OnLockClick -> {
                _effect.tryEmit(VaultEffect.LockVault)
            }
            VaultEvent.OnLockFailed -> {
                _state.update {
                    it.copy(
                        errorMessage = uiText(Res.string.error_settings_lock),
                        canRetryLoad = false,
                    )
                }
            }
            VaultEvent.OnRefresh -> {
                loadData()
            }
            VaultEvent.OnDismissError -> {
                _state.update { it.copy(errorMessage = null, canRetryLoad = false) }
            }
            is VaultEvent.OnFilterChanged -> {
                updateFilters { it.copy(activeFilter = event.filter) }
            }
            is VaultEvent.OnSortChanged -> {
                updateFilters { it.copy(sortOrder = event.sortOrder) }
            }
            VaultEvent.OnNewFolderClick -> {
                _state.update {
                    it.copy(
                        showNewFolderDialog = true,
                        newFolderName = "",
                        folderError = null,
                    )
                }
            }
            is VaultEvent.OnNewFolderNameChanged -> {
                val boundedName = event.name.takeCodePoints(MAX_FOLDER_NAME_LENGTH + 1)
                val normalizedName = boundedName.trim()
                val folderError = when {
                    event.name.codePointLength() > MAX_FOLDER_NAME_LENGTH ->
                        uiText(Res.string.error_folder_name_too_long)
                    normalizedName.isNotEmpty() && _state.value.folders.any {
                        it.name.equals(normalizedName, ignoreCase = true)
                    } -> uiText(Res.string.error_folder_duplicate)
                    else -> null
                }
                _state.update {
                    it.copy(
                        newFolderName = boundedName,
                        folderError = folderError,
                        errorMessage = null,
                        canRetryLoad = false,
                    )
                }
            }
            VaultEvent.OnCreateFolderClick -> createFolder()
            VaultEvent.OnDismissNewFolder -> {
                if (!_state.value.isCreatingFolder) {
                    _state.update {
                        it.copy(
                            showNewFolderDialog = false,
                            newFolderName = "",
                            folderError = null,
                        )
                    }
                }
            }
            is VaultEvent.OnDeleteFolderClick -> {
                val folder = _state.value.folders.firstOrNull { it.id == event.folderId }
                if (folder != null) {
                    _state.update {
                        it.copy(
                            folderPendingDeletion = folder,
                            errorMessage = null,
                            canRetryLoad = false,
                        )
                    }
                }
            }
            VaultEvent.OnConfirmDeleteFolder -> deleteFolder()
            VaultEvent.OnDismissDeleteFolder -> {
                if (!_state.value.isDeletingFolder) {
                    _state.update { it.copy(folderPendingDeletion = null) }
                }
            }
        }
    }

    private fun createFolder() {
        if (_state.value.isCreatingFolder || _state.value.isDeletingFolder) return
        val name = _state.value.newFolderName.trim()
        val validationError = folderNameError(name, _state.value.folders)
        if (validationError != null) {
            _state.update { it.copy(folderError = validationError) }
            return
        }
        _state.update {
            it.copy(
                isCreatingFolder = true,
                folderError = null,
                errorMessage = null,
                canRetryLoad = false,
            )
        }

        folderJob?.cancel()
        folderJob = viewModelScope.launch {
            folderMutex.withLock { persistFolder(name) }
        }
    }

    private suspend fun persistFolder(name: String) {
        try {
            val folder = Folder(
                id = FolderId(Uuid.random().toString()),
                parentId = null,
                name = name,
                icon = null,
                sortOrder = _state.value.folders.size,
                createdAt = Clock.System.now(),
            )
            val result = folderRepository.save(folder)
            currentCoroutineContext().ensureActive()
            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        showNewFolderDialog = false,
                        newFolderName = "",
                        isCreatingFolder = false,
                        folderError = null,
                    )
                }
                loadData()
            } else {
                showFolderCreateError()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            showFolderCreateError()
        }
    }

    private fun showFolderCreateError() {
        _state.update {
            it.copy(
                isCreatingFolder = false,
                folderError = uiText(Res.string.error_folder_create),
            )
        }
    }

    private fun deleteFolder() {
        val currentState = _state.value
        val folder = currentState.folderPendingDeletion ?: return
        if (currentState.isDeletingFolder || currentState.isCreatingFolder) return

        _state.update {
            it.copy(
                isDeletingFolder = true,
                errorMessage = null,
                canRetryLoad = false,
            )
        }
        folderJob?.cancel()
        folderJob = viewModelScope.launch {
            folderMutex.withLock {
                try {
                    val result = folderRepository.delete(folder.id)
                    currentCoroutineContext().ensureActive()
                    result.onSuccess {
                        _state.update { current ->
                            current.copy(
                                folders = current.folders.filterNot { it.id == folder.id },
                                selectedFolderId = current.selectedFolderId
                                    .takeUnless { it == folder.id },
                                folderPendingDeletion = null,
                                isDeletingFolder = false,
                            ).withFilteredCredentials()
                        }
                        loadData()
                    }
                        .onFailure {
                            _state.update {
                                it.copy(
                                    isDeletingFolder = false,
                                    errorMessage = uiText(Res.string.error_folder_delete),
                                    canRetryLoad = false,
                                )
                            }
                        }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    _state.update {
                        it.copy(
                            isDeletingFolder = false,
                            errorMessage = uiText(Res.string.error_folder_delete),
                            canRetryLoad = false,
                        )
                    }
                }
            }
        }
    }

    private fun updateFilters(transform: (VaultState) -> VaultState) {
        _state.update { current -> transform(current).withFilteredCredentials() }
    }

    private fun reconcileLoadedCredentials(
        loaded: List<CredentialSummary.Decrypted>,
        current: List<CredentialSummary.Decrypted>,
        favoriteRevisionAtStart: Long,
    ): List<CredentialSummary.Decrypted> {
        val currentFavorites = current.associate { it.id to it.isFavorite }
        val favoriteChangedDuringLoad = favoriteRevision != favoriteRevisionAtStart
        return loaded.map { credential ->
            val favorite = favoriteTargets[credential.id]
                ?: currentFavorites[credential.id]?.takeIf { favoriteChangedDuringLoad }
            favorite?.let { credential.copy(isFavorite = it) } ?: credential
        }
    }

    private fun toggleFavorite(credentialId: CredentialId) {
        val credential = state.value.credentials.find { it.id == credentialId } ?: return
        val originalFavorite = credential.isFavorite
        val targetFavorite = !originalFavorite
        favoriteRevision++
        _state.update { current ->
            current.copy(
                credentials = current.credentials.map {
                    if (it.id == credentialId) it.copy(isFavorite = targetFavorite) else it
                },
                errorMessage = null,
                canRetryLoad = false,
            ).withFilteredCredentials()
        }

        favoriteTargets[credentialId] = targetFavorite
        if (favoriteJobs[credentialId]?.isActive == true) return
        val job = viewModelScope.launch {
            persistLatestFavorite(credentialId, originalFavorite)
        }
        favoriteJobs[credentialId] = job
        job.invokeOnCompletion {
            if (favoriteJobs[credentialId] === job) {
                favoriteJobs.remove(credentialId)
            }
        }
    }

    private suspend fun persistLatestFavorite(
        credentialId: CredentialId,
        initialFavorite: Boolean,
    ) {
        var persistedFavorite = initialFavorite
        while (true) {
            val targetFavorite = favoriteTargets[credentialId] ?: return
            val succeeded = try {
                credentialRepository.updateFavorite(credentialId, targetFavorite).isSuccess
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                false
            }
            currentCoroutineContext().ensureActive()
            if (succeeded) persistedFavorite = targetFavorite
            favoriteRevision++

            if (favoriteTargets[credentialId] != targetFavorite) continue
            favoriteTargets.remove(credentialId)
            _state.update { current ->
                current.copy(
                    credentials = current.credentials.map { credential ->
                        if (credential.id == credentialId) {
                            credential.copy(
                                isFavorite = if (succeeded) targetFavorite else persistedFavorite,
                            )
                        } else {
                            credential
                        }
                    },
                    errorMessage = if (succeeded) current.errorMessage else {
                        uiText(Res.string.error_favorite_update)
                    },
                    canRetryLoad = if (succeeded) current.canRetryLoad else false,
                ).withFilteredCredentials()
            }
            return
        }
    }

    fun clearForLock() {
        loadJob?.cancel()
        favoriteJobs.values.forEach(Job::cancel)
        favoriteJobs.clear()
        favoriteTargets.clear()
        folderJob?.cancel()
        loadJob = null
        folderJob = null
        _state.value = VaultState(isLoading = false)
    }

    data class VaultState(
        val isLoading: Boolean = false,
        val credentials: List<CredentialSummary.Decrypted> = emptyList(),
        val filteredCredentials: List<CredentialSummary.Decrypted> = emptyList(),
        val folders: List<Folder> = emptyList(),
        val tags: List<Tag> = emptyList(),
        val searchQuery: String = "",
        val selectedFolderId: FolderId? = null,
        val selectedTagId: TagId? = null,
        val activeFilter: CredentialFilter = CredentialFilter.ALL,
        val sortOrder: SortOrder = SortOrder.LAST_USED,
        val errorMessage: UiText? = null,
        val canRetryLoad: Boolean = false,
        val isSearchActive: Boolean = false,
        val showNewFolderDialog: Boolean = false,
        val newFolderName: String = "",
        val isCreatingFolder: Boolean = false,
        val folderError: UiText? = null,
        val folderPendingDeletion: Folder? = null,
        val isDeletingFolder: Boolean = false,
    ) {
        val hasCredentials: Boolean get() = credentials.isNotEmpty()
        val isEmpty: Boolean get() = filteredCredentials.isEmpty() && !isLoading
        val credentialCount: Int get() = credentials.size
        val canCreateFolder: Boolean
            get() = newFolderName.trim().isNotEmpty() &&
                folderError == null &&
                !isCreatingFolder &&
                !isDeletingFolder
    }

    sealed interface VaultEvent {
        data class OnSearchQueryChanged(val query: String) : VaultEvent
        data class OnFolderSelected(val folderId: String?) : VaultEvent
        data class OnTagSelected(val tagId: String?) : VaultEvent
        data class OnCredentialClick(val credentialId: CredentialId) : VaultEvent
        data class OnCredentialFavoriteClick(val credentialId: CredentialId) : VaultEvent
        data class OnFilterChanged(val filter: CredentialFilter) : VaultEvent
        data class OnSortChanged(val sortOrder: SortOrder) : VaultEvent
        data object OnAddCredentialClick : VaultEvent
        data object OnSearchClick : VaultEvent
        data object OnSearchDismiss : VaultEvent
        data object OnSettingsClick : VaultEvent
        data object OnGeneratorClick : VaultEvent
        data object OnTwoFactorCodesClick : VaultEvent
        data object OnLockClick : VaultEvent
        data object OnLockFailed : VaultEvent
        data object OnRefresh : VaultEvent
        data object OnDismissError : VaultEvent
        data object OnNewFolderClick : VaultEvent
        data class OnNewFolderNameChanged(val name: String) : VaultEvent
        data object OnCreateFolderClick : VaultEvent
        data object OnDismissNewFolder : VaultEvent
        data class OnDeleteFolderClick(val folderId: FolderId) : VaultEvent
        data object OnConfirmDeleteFolder : VaultEvent
        data object OnDismissDeleteFolder : VaultEvent
    }

    sealed interface VaultEffect {
        data class NavigateToCredentialDetail(val credentialId: CredentialId) : VaultEffect
        data class NavigateToCredentialEdit(val credentialId: CredentialId?) : VaultEffect
        data object NavigateToSettings : VaultEffect
        data object NavigateToGenerator : VaultEffect
        data object NavigateToTwoFactorCodes : VaultEffect
        data object LockVault : VaultEffect
    }

    enum class CredentialFilter {
        ALL, FAVORITES, WEAK_PASSWORDS, DUPLICATES, EXPIRED
    }

    enum class SortOrder {
        NAME_ASC, NAME_DESC, LAST_USED, CREATED
    }

}

private const val MAX_SEARCH_QUERY_LENGTH = 256
private const val MAX_FOLDER_NAME_LENGTH = 256

private fun folderNameError(name: String, folders: List<Folder>): UiText? = when {
    name.isEmpty() -> uiText(Res.string.error_folder_name_required)
    name.codePointLength() > MAX_FOLDER_NAME_LENGTH -> uiText(Res.string.error_folder_name_too_long)
    folders.any { it.name.equals(name, ignoreCase = true) } ->
        uiText(Res.string.error_folder_duplicate)
    else -> null
}

private fun filterCredentials(
    state: VaultViewModel.VaultState,
): List<CredentialSummary.Decrypted> {
    val query = state.searchQuery.takeIf(String::isNotBlank)?.lowercase()
    val matchingText = state.credentials.filter { it.matchesQuery(query) }
    val matchingFolder = state.selectedFolderId?.let { folderId ->
        matchingText.filter { it.folderId == folderId }
    } ?: matchingText
    val matchingTag = state.selectedTagId?.let { tagId ->
        matchingFolder.filter { tagId in it.tagIds }
    } ?: matchingFolder
    return matchingTag
        .matching(state.activeFilter)
        .sorted(state.sortOrder)
}

private fun CredentialSummary.Decrypted.matchesQuery(query: String?): Boolean =
    query == null ||
        title.lowercase().contains(query) ||
        displayUsername?.lowercase()?.contains(query) == true

private fun List<CredentialSummary.Decrypted>.matching(
    filter: VaultViewModel.CredentialFilter,
): List<CredentialSummary.Decrypted> = when (filter) {
    VaultViewModel.CredentialFilter.FAVORITES -> filter(CredentialSummary.Decrypted::isFavorite)
    VaultViewModel.CredentialFilter.WEAK_PASSWORDS -> filter { it.passwordHealth.isWeak }
    VaultViewModel.CredentialFilter.DUPLICATES -> filter { it.passwordHealth.isDuplicate }
    VaultViewModel.CredentialFilter.EXPIRED -> filter { it.passwordHealth.isOld }
    VaultViewModel.CredentialFilter.ALL -> this
}

private fun List<CredentialSummary.Decrypted>.sorted(
    order: VaultViewModel.SortOrder,
): List<CredentialSummary.Decrypted> = when (order) {
    VaultViewModel.SortOrder.NAME_ASC -> sortedBy { it.title.lowercase() }
    VaultViewModel.SortOrder.NAME_DESC -> sortedByDescending { it.title.lowercase() }
    VaultViewModel.SortOrder.LAST_USED -> sortedByDescending { it.lastUsedAt }
    VaultViewModel.SortOrder.CREATED -> sortedByDescending { it.createdAt }
}

private fun VaultViewModel.VaultState.withFilteredCredentials(): VaultViewModel.VaultState =
    copy(filteredCredentials = filterCredentials(this))
