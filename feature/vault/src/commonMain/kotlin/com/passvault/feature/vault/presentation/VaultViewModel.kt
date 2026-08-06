package com.passvault.feature.vault.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.*
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.FolderRepository
import com.passvault.core.domain.repository.TagRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
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

    private val _effect = Channel<VaultEffect>(Channel.BUFFERED)
    val effect: Flow<VaultEffect> = _effect.receiveAsFlow()
    private val loadMutex = Mutex()
    private val favoriteMutex = Mutex()
    private val folderMutex = Mutex()
    private var loadJob: Job? = null
    private var favoriteJob: Job? = null
    private var folderJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadMutex.withLock {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    val credentials = credentialRepository.getAllSummaries().getOrThrow()
                    val folders = folderRepository.getAll().getOrThrow()
                    val tags = tagRepository.getAll().getOrThrow()
                    _state.update { current ->
                        val loaded = current.copy(
                            isLoading = false,
                            credentials = credentials,
                            folders = folders,
                            tags = tags,
                            errorMessage = null,
                        )
                        loaded.copy(filteredCredentials = filterCredentials(loaded))
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = uiText(Res.string.error_vault_load),
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: VaultEvent) {
        when (event) {
            is VaultEvent.OnSearchQueryChanged -> {
                updateFilters {
                    it.copy(
                        searchQuery = event.query.take(MAX_SEARCH_QUERY_LENGTH),
                        errorMessage = null,
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
                _effect.trySend(VaultEffect.NavigateToCredentialDetail(event.credentialId))
            }
            is VaultEvent.OnCredentialFavoriteClick -> {
                toggleFavorite(event.credentialId)
            }
            VaultEvent.OnAddCredentialClick -> {
                _effect.trySend(VaultEffect.NavigateToCredentialEdit(null))
            }
            VaultEvent.OnSearchClick -> {
                _state.update { it.copy(isSearchActive = true) }
                _effect.trySend(VaultEffect.NavigateToSearch(_state.value.searchQuery))
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
                _effect.trySend(VaultEffect.NavigateToSettings)
            }
            VaultEvent.OnGeneratorClick -> {
                _effect.trySend(VaultEffect.NavigateToGenerator)
            }
            VaultEvent.OnHealthClick -> {
                _effect.trySend(VaultEffect.NavigateToHealth)
            }
            VaultEvent.OnLockClick -> {
                _effect.trySend(VaultEffect.LockVault)
            }
            VaultEvent.OnRefresh -> {
                loadData()
            }
            VaultEvent.OnDismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }
            is VaultEvent.OnFilterChanged -> {
                updateFilters { it.copy(activeFilter = event.filter) }
            }
            is VaultEvent.OnSortChanged -> {
                updateFilters { it.copy(sortOrder = event.sortOrder) }
            }
            VaultEvent.OnNewFolderClick -> {
                _state.update { it.copy(showNewFolderDialog = true, newFolderName = "") }
            }
            is VaultEvent.OnNewFolderNameChanged -> {
                _state.update { it.copy(newFolderName = event.name, errorMessage = null) }
            }
            VaultEvent.OnCreateFolderClick -> createFolder()
            VaultEvent.OnDismissNewFolder -> {
                _state.update { it.copy(showNewFolderDialog = false, newFolderName = "") }
            }
            is VaultEvent.OnDeleteFolderClick -> {
                val folder = _state.value.folders.firstOrNull { it.id == event.folderId }
                if (folder != null) {
                    _state.update {
                        it.copy(
                            folderPendingDeletion = folder,
                            errorMessage = null,
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
        if (name.isEmpty()) {
            _state.update { it.copy(errorMessage = uiText(Res.string.error_folder_name_required)) }
            return
        }
        if (_state.value.folders.any { it.name.equals(name, ignoreCase = true) }) {
            _state.update { it.copy(errorMessage = uiText(Res.string.error_folder_duplicate)) }
            return
        }
        _state.update { it.copy(isCreatingFolder = true, errorMessage = null) }

        folderJob?.cancel()
        folderJob = viewModelScope.launch {
            folderMutex.withLock {
                try {
                    folderRepository.save(
                        Folder(
                            id = FolderId(Uuid.random().toString()),
                            parentId = null,
                            name = name,
                            icon = null,
                            sortOrder = _state.value.folders.size,
                            createdAt = Clock.System.now(),
                        )
                    ).onSuccess {
                        _state.update {
                            it.copy(
                                showNewFolderDialog = false,
                                newFolderName = "",
                                isCreatingFolder = false,
                            )
                        }
                        loadData()
                    }.onFailure {
                        _state.update {
                            it.copy(
                                isCreatingFolder = false,
                                errorMessage = uiText(Res.string.error_folder_create),
                            )
                        }
                    }
                } catch (cancelled: CancellationException) {
                    _state.update { it.copy(isCreatingFolder = false) }
                    throw cancelled
                } catch (_: Exception) {
                    _state.update {
                        it.copy(
                            isCreatingFolder = false,
                            errorMessage = uiText(Res.string.error_folder_create),
                        )
                    }
                }
            }
        }
    }

    private fun deleteFolder() {
        val currentState = _state.value
        val folder = currentState.folderPendingDeletion ?: return
        if (currentState.isDeletingFolder || currentState.isCreatingFolder) return

        _state.update { it.copy(isDeletingFolder = true, errorMessage = null) }
        folderJob?.cancel()
        folderJob = viewModelScope.launch {
            folderMutex.withLock {
                try {
                    folderRepository.delete(folder.id)
                        .onSuccess {
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
                                )
                            }
                        }
                } catch (cancelled: CancellationException) {
                    _state.update { it.copy(isDeletingFolder = false) }
                    throw cancelled
                } catch (_: Exception) {
                    _state.update {
                        it.copy(
                            isDeletingFolder = false,
                            errorMessage = uiText(Res.string.error_folder_delete),
                        )
                    }
                }
            }
        }
    }

    private fun updateFilters(transform: (VaultState) -> VaultState) {
        _state.update { current -> transform(current).withFilteredCredentials() }
    }

    private fun filterCredentials(currentState: VaultState = _state.value): List<CredentialSummary.Decrypted> {
        var filtered = currentState.credentials

        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            filtered = filtered.filter { credential ->
                credential.title.lowercase().contains(query) ||
                        credential.displayUsername?.lowercase()?.contains(query) == true
            }
        }

        currentState.selectedFolderId?.let { folderId ->
            filtered = filtered.filter { it.folderId == folderId }
        }

        currentState.selectedTagId?.let { tagId ->
            filtered = filtered.filter { tagId in it.tagIds }
        }

        filtered = when (currentState.activeFilter) {
            CredentialFilter.FAVORITES -> filtered.filter { it.isFavorite }
            CredentialFilter.WEAK_PASSWORDS -> filtered.filter { it.passwordHealth.isWeak }
            CredentialFilter.DUPLICATES -> filtered.filter { it.passwordHealth.isDuplicate }
            CredentialFilter.EXPIRED -> filtered.filter { it.passwordHealth.isOld }
            CredentialFilter.ALL -> filtered
        }

        filtered = when (currentState.sortOrder) {
            SortOrder.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortOrder.LAST_USED -> filtered.sortedByDescending { it.lastUsedAt }
            SortOrder.CREATED -> filtered.sortedByDescending { it.createdAt }
        }

        return filtered
    }

    private fun VaultState.withFilteredCredentials(): VaultState =
        copy(filteredCredentials = filterCredentials(this))

    private fun toggleFavorite(credentialId: CredentialId) {
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            favoriteMutex.withLock {
                val credential = state.value.credentials.find { it.id == credentialId }
                credential?.let {
                    credentialRepository.updateFavorite(credentialId, !it.isFavorite)
                        .onSuccess { loadData() }
                        .onFailure {
                            _state.update { current ->
                                current.copy(errorMessage = uiText(Res.string.error_favorite_update))
                            }
                        }
                }
            }
        }
    }

    fun clearForLock() {
        loadJob?.cancel()
        favoriteJob?.cancel()
        folderJob?.cancel()
        loadJob = null
        favoriteJob = null
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
        val isSearchActive: Boolean = false,
        val showNewFolderDialog: Boolean = false,
        val newFolderName: String = "",
        val isCreatingFolder: Boolean = false,
        val folderPendingDeletion: Folder? = null,
        val isDeletingFolder: Boolean = false,
    ) {
        val hasCredentials: Boolean get() = credentials.isNotEmpty()
        val isEmpty: Boolean get() = filteredCredentials.isEmpty() && !isLoading
        val credentialCount: Int get() = credentials.size
        val favoriteCount: Int get() = credentials.count { it.isFavorite }
        val weakPasswordCount: Int get() = credentials.count { it.passwordHealth.isWeak }
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
        data object OnHealthClick : VaultEvent
        data object OnLockClick : VaultEvent
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
        data class NavigateToSearch(val query: String) : VaultEffect
        data class NavigateToFolder(val folderId: FolderId) : VaultEffect
        data class NavigateToTag(val tagId: TagId) : VaultEffect
        data object NavigateToSettings : VaultEffect
        data object NavigateToGenerator : VaultEffect
        data object NavigateToHealth : VaultEffect
        data object LockVault : VaultEffect
    }

    enum class CredentialFilter {
        ALL, FAVORITES, WEAK_PASSWORDS, DUPLICATES, EXPIRED
    }

    enum class SortOrder {
        NAME_ASC, NAME_DESC, LAST_USED, CREATED
    }

    private companion object {
        const val MAX_SEARCH_QUERY_LENGTH = 256
    }
}
