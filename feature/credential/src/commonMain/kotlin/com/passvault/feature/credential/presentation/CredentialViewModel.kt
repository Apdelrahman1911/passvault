package com.passvault.feature.credential.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.crypto.PasswordGenerationOptions
import com.passvault.core.crypto.PasswordGenerator
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.core.domain.model.TotpConfiguration
import com.passvault.core.domain.repository.AttachmentRepository
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.FolderRepository
import com.passvault.core.otp.StandardTotpService
import com.passvault.core.otp.TotpService
import com.passvault.core.security.EntrySensitiveStateOwner
import com.passvault.feature.credential.AttachmentFileStore
import com.passvault.feature.credential.UnavailableAttachmentFileStore
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * State owner for credential detail and edit flows.
 *
 * The ViewModel never logs or exposes repository exception text. It validates
 * user input before constructing a domain object and clears all wrapped
 * sensitive values when the flow is left or the vault is locked.
 */
class CredentialViewModel(
    private val credentialRepository: CredentialRepository,
    private val folderRepository: FolderRepository,
    private val passwordGenerator: PasswordGenerator,
    private val totpService: TotpService = StandardTotpService(),
    private val clock: Clock = Clock.System,
    attachmentRepository: AttachmentRepository = defaultAttachmentRepository,
    attachmentFileStore: AttachmentFileStore = UnavailableAttachmentFileStore,
) : ViewModel(), EntrySensitiveStateOwner {

    private val _state = MutableStateFlow(CredentialState())
    val state: StateFlow<CredentialState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CredentialEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<CredentialEffect> = _effect.asSharedFlow()

    private val saveMutex = Mutex()
    private val deleteMutex = Mutex()
    private var loadJob: Job? = null
    private var saveJob: Job? = null
    private var deleteJob: Job? = null
    private var folderJob: Job? = null
    private var usageJob: Job? = null
    private var passwordGenerationJob: Job? = null
    private var editingRevision = 0L

    private val totpController = CredentialTotpController(_state, viewModelScope, totpService, clock)
    private val customFieldEditor = CredentialCustomFieldEditor(_state)
    private val attachmentController = CredentialAttachmentController(
        state = _state,
        scope = viewModelScope,
        repository = attachmentRepository,
        fileStore = attachmentFileStore,
    )

    override fun onCleared() {
        clearForLock()
        super.onCleared()
    }
    private val eventRouter = CredentialEventRouter(
        state = _state,
        effect = _effect,
        totp = totpController,
        customFields = customFieldEditor,
        attachments = attachmentController,
        callbacks = CredentialEventCallbacks(
            save = ::saveCredential,
            delete = ::deleteCredential,
            recordUsage = ::recordCredentialUsage,
            generatePassword = ::generatePassword,
        ),
    )

    fun loadCredential(credentialId: CredentialId) {
        cancelPendingOperations()
        clearStateSensitiveValues()
        editingRevision++
        _state.value = CredentialState(
            credentialId = credentialId,
            isLoading = true,
            isNewCredential = false,
        )
        loadFolders()
        loadJob = viewModelScope.launch { performCredentialLoad(credentialId) }
    }

    private suspend fun performCredentialLoad(credentialId: CredentialId) {
        var credential: Credential? = null
        try {
            val result = credentialRepository.getById(credentialId)
            currentCoroutineContext().ensureActive()
            if (result.isFailure) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = uiText(Res.string.error_credential_load))
                }
                return
            }
            credential = result.getOrNull()
            val loadedCredential = credential
            if (loadedCredential == null) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = uiText(Res.string.error_credential_not_found))
                }
                return
            }
            currentCoroutineContext().ensureActive()
            _state.value = loadedCredential.toEditableState(_state.value)
            totpController.start()
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            _state.update {
                it.copy(isLoading = false, errorMessage = uiText(Res.string.error_credential_load))
            }
        } finally {
            credential?.clearSensitiveValues()
        }
    }

    fun createNewCredential(type: CredentialType) {
        cancelPendingOperations()
        clearStateSensitiveValues()
        editingRevision++
        _state.value = CredentialState(
            credentialType = type,
            isNewCredential = true,
            isDirty = false,
        )
        loadFolders()
    }

    private fun loadFolders() {
        folderJob?.cancel()
        _state.update {
            it.copy(
                isLoadingFolders = true,
                folderLoadFailed = false,
            )
        }
        folderJob = viewModelScope.launch {
            try {
                val result = folderRepository.getAll()
                if (!isActive) return@launch
                _state.update {
                    it.copy(
                        folders = result.getOrDefault(emptyList()),
                        isLoadingFolders = false,
                        folderLoadFailed = result.isFailure,
                    )
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        folders = emptyList(),
                        isLoadingFolders = false,
                        folderLoadFailed = true,
                    )
                }
            }
        }
    }

    fun onEvent(event: CredentialEvent) = eventRouter.handle(event)

    private fun saveCredential() {
        val beforeSave = _state.value
        if (beforeSave.isBusy || (!beforeSave.isNewCredential && !beforeSave.isCredentialLoaded)) return
        val validation = evaluateCredentialValidation(beforeSave)
        if (!validation.isValid) {
            _state.update {
                it.copy(
                    titleError = validation.titleError,
                    urlErrors = validation.urlErrors,
                    errorMessage = validation.fieldError,
                )
            }
            return
        }
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            saveMutex.withLock {
                if (_state.value.isSaving) performSave(beforeSave)
            }
        }
    }

    private suspend fun performSave(beforeSave: CredentialState) {
        val credential = try {
            createCredentialFromState(beforeSave, clock.now())
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            _state.update {
                it.copy(isSaving = false, errorMessage = uiText(Res.string.error_credential_invalid_data))
            }
            return
        }
        try {
            val result = credentialRepository.save(credential)
            currentCoroutineContext().ensureActive()
            if (result.isSuccess) {
                val id = result.getOrThrow()
                _state.update {
                    it.copy(
                        isSaving = false,
                        isNewCredential = false,
                        isCredentialLoaded = true,
                        isDirty = false,
                        credentialId = id,
                        createdAt = credential.createdAt,
                        updatedAt = credential.updatedAt,
                    )
                }
                _effect.tryEmit(CredentialEffect.SaveCompleted(id))
            } else {
                _state.update {
                    it.copy(isSaving = false, errorMessage = uiText(Res.string.error_credential_save))
                }
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            _state.update {
                it.copy(isSaving = false, errorMessage = uiText(Res.string.error_credential_save))
            }
        } finally {
            credential.clearSensitiveValues()
        }
    }

    private fun deleteCredential() {
        val current = _state.value
        val credentialId = current.credentialId ?: return
        if (current.isBusy || !current.isCredentialLoaded) return
        _state.update { it.copy(isDeleting = true, errorMessage = null) }
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            deleteMutex.withLock {
                try {
                    val result = credentialRepository.delete(credentialId)
                    if (!isActive) return@withLock
                    if (result.isSuccess) {
                        totpController.stop()
                        clearStateSensitiveValues()
                        _state.value = CredentialState()
                        _effect.tryEmit(CredentialEffect.NavigateBack)
                    } else {
                        _state.update {
                            it.copy(
                                isDeleting = false,
                                errorMessage = uiText(Res.string.error_credential_delete),
                            )
                        }
                    }
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = uiText(Res.string.error_credential_delete),
                        )
                    }
                }
            }
        }
    }

    override fun clearForLock() {
        cancelPendingOperations()
        clearStateSensitiveValues()
        editingRevision++
        _state.value = CredentialState()
    }

    private fun cancelPendingOperations() {
        loadJob?.cancel()
        saveJob?.cancel()
        deleteJob?.cancel()
        folderJob?.cancel()
        totpController.stop()
        usageJob?.cancel()
        passwordGenerationJob?.cancel()
        attachmentController.cancel()
        loadJob = null
        saveJob = null
        deleteJob = null
        folderJob = null
        usageJob = null
        passwordGenerationJob = null
    }

    private fun generatePassword() {
        val current = _state.value
        if (current.hasBlockingOperation()) return
        val revision = editingRevision
        passwordGenerationJob?.cancel()
        _state.update { it.copy(isGeneratingPassword = true, errorMessage = null) }
        passwordGenerationJob = viewModelScope.launch {
            try {
                val result = passwordGenerator.generate(PasswordGenerationOptions())
                currentCoroutineContext().ensureActive()
                if (revision != editingRevision) return@launch
                result.fold(
                    onSuccess = { password ->
                        _state.update {
                            it.copy(
                                password = password,
                                passwordStrength = calculateCredentialPasswordStrength(password),
                                isGeneratingPassword = false,
                                isDirty = true,
                                errorMessage = null,
                            )
                        }
                    },
                    onFailure = {
                        _state.update {
                            it.copy(
                                isGeneratingPassword = false,
                                errorMessage = uiText(Res.string.error_generator_password_failed),
                            )
                        }
                    },
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                if (revision == editingRevision) {
                    _state.update {
                        it.copy(
                            isGeneratingPassword = false,
                            errorMessage = uiText(Res.string.error_generator_password_failed),
                        )
                    }
                }
            }
        }
    }

    private fun CredentialState.hasBlockingOperation(): Boolean =
        isLoading || isSaving || isDeleting || isAttachmentBusy

    private fun clearStateSensitiveValues() {
        val current = _state.value
        current.customFields.forEach { it.value.clear() }
        current.recoveryCodes.forEach(SensitiveText::clear)
        current.apiKeys.forEach(SensitiveText::clear)
        current.licenseKeys.forEach(SensitiveText::clear)
        current.totpConfiguration?.clear()
        current.pendingTotpConfiguration?.clear()
    }

    private fun recordCredentialUsage() {
        val current = _state.value
        val credentialId = current.credentialId ?: return
        if (!current.isCredentialLoaded) return
        val usedAt = clock.now()
        usageJob?.cancel()
        usageJob = viewModelScope.launch {
            try {
                val result = credentialRepository.recordUsage(credentialId, usedAt)
                if (result.isSuccess && isActive && _state.value.credentialId == credentialId) {
                    _state.update { state ->
                        if (state.credentialId == credentialId) state.copy(lastUsedAt = usedAt) else state
                    }
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                // Usage metadata is best effort and must not turn a successful
                // copy or URL launch into an error for the user.
            }
        }
    }

    data class CredentialState(
        val credentialId: CredentialId? = null,
        val credentialType: CredentialType = CredentialType.Login,
        val title: String = "",
        val username: String = "",
        val email: String = "",
        val password: String = "",
        val urls: List<String> = emptyList(),
        val notes: String = "",
        val customFields: List<CustomField> = emptyList(),
        val recoveryCodes: List<SensitiveText> = emptyList(),
        val apiKeys: List<SensitiveText> = emptyList(),
        val licenseKeys: List<SensitiveText> = emptyList(),
        val attachments: List<AttachmentMetadata> = emptyList(),
        val folderId: FolderId? = null,
        val folders: List<Folder> = emptyList(),
        val isLoadingFolders: Boolean = false,
        val folderLoadFailed: Boolean = false,
        val tagIds: Set<TagId> = emptySet(),
        val isFavorite: Boolean = false,
        val passwordHealth: PasswordHealth = PasswordHealth.UNKNOWN,
        val createdAt: Instant? = null,
        val updatedAt: Instant? = null,
        val lastUsedAt: Instant? = null,
        val passwordStrength: PasswordStrength = PasswordStrength.EMPTY,
        val totpConfiguration: TotpConfiguration? = null,
        val pendingTotpConfiguration: TotpConfiguration? = null,
        val totpSetupInput: String = "",
        val totpAlgorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
        val totpDigits: Int = DEFAULT_TOTP_DIGITS,
        val totpPeriodInput: String = DEFAULT_TOTP_PERIOD,
        val currentTotpCode: String = "",
        val totpSecondsRemaining: Int = 0,
        val totpProgress: Float = 0f,
        val totpGenerationError: Boolean = false,
        val totpSetupError: UiText? = null,
        val showTotpScanner: Boolean = false,
        val showTotpReplaceConfirmation: Boolean = false,
        val showTotpRemoveConfirmation: Boolean = false,
        val isCredentialLoaded: Boolean = false,
        val isNewCredential: Boolean = false,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val isDeleting: Boolean = false,
        val isAttachmentBusy: Boolean = false,
        val isGeneratingPassword: Boolean = false,
        val isDirty: Boolean = false,
        val errorMessage: UiText? = null,
        val titleError: UiText? = null,
        val urlErrors: Map<Int, UiText> = emptyMap(),
        val showDeleteConfirmation: Boolean = false,
        val showDiscardConfirmation: Boolean = false,
        val attachmentRenameTarget: AttachmentId? = null,
        val attachmentRenameInput: String = "",
        val attachmentDeleteTarget: AttachmentId? = null,
    ) {
        val isBusy: Boolean
            get() = isLoading || isSaving || isDeleting || isAttachmentBusy || isGeneratingPassword
        val canSave: Boolean get() = !isBusy && (isNewCredential || isCredentialLoaded)
        val displayTitle: UiText
            get() = title
                .takeIf(String::isNotBlank)
                ?.let(UiText::Dynamic)
                ?: uiText(Res.string.ui_untitled_credential)
    }

    sealed interface CredentialEvent {
        data class OnTitleChanged(val title: String) : CredentialEvent
        data class OnUsernameChanged(val username: String) : CredentialEvent
        data class OnEmailChanged(val email: String) : CredentialEvent
        data class OnPasswordChanged(val password: String) : CredentialEvent
        data class OnNotesChanged(val notes: String) : CredentialEvent
        data class OnUrlAdded(val url: String) : CredentialEvent
        data class OnUrlChanged(val index: Int, val url: String) : CredentialEvent
        data class OnUrlRemoved(val index: Int) : CredentialEvent
        data class OnFolderChanged(val folderId: String?) : CredentialEvent
        data class OnTagsChanged(val tagIds: List<String>) : CredentialEvent
        data class OnFavoriteChanged(val isFavorite: Boolean) : CredentialEvent
        data class OnTotpSetupInputChanged(val value: String) : CredentialEvent
        data class OnTotpAlgorithmChanged(val algorithm: TotpAlgorithm) : CredentialEvent
        data class OnTotpDigitsChanged(val digits: Int) : CredentialEvent
        data class OnTotpPeriodChanged(val value: String) : CredentialEvent
        data class OnTotpQrScanned(val payload: String) : CredentialEvent
        data class OnCustomFieldAdded(val name: String, val value: String, val isSecret: Boolean) : CredentialEvent
        data class OnCustomFieldRemoved(val fieldId: CustomFieldId) : CredentialEvent
        data class OnCustomFieldUpdated(
            val fieldId: CustomFieldId,
            val name: String,
            val value: String,
            val isSecret: Boolean,
        ) : CredentialEvent
        data class OnCopyCustomFieldClick(val fieldId: CustomFieldId) : CredentialEvent
        data class OnCopyRecoveryCodeClick(val index: Int) : CredentialEvent
        data class OnCopyApiKeyClick(val index: Int) : CredentialEvent
        data class OnCopyLicenseKeyClick(val index: Int) : CredentialEvent
        data class OnLaunchUrlClick(val url: String) : CredentialEvent
        data class OnUrlLaunchResult(val succeeded: Boolean) : CredentialEvent
        data class OnCopyResult(val succeeded: Boolean) : CredentialEvent
        data class OnAttachmentOpenClick(val attachmentId: AttachmentId) : CredentialEvent
        data class OnAttachmentExportClick(val attachmentId: AttachmentId) : CredentialEvent
        data class OnAttachmentRenameClick(val attachmentId: AttachmentId) : CredentialEvent
        data class OnAttachmentRenameChanged(val fileName: String) : CredentialEvent
        data class OnAttachmentDeleteClick(val attachmentId: AttachmentId) : CredentialEvent
        data object OnSaveClick : CredentialEvent
        data object OnDeleteClick : CredentialEvent
        data object OnDeleteConfirm : CredentialEvent
        data object OnDeleteCancel : CredentialEvent
        data object OnCancelClick : CredentialEvent
        data object OnDiscardConfirm : CredentialEvent
        data object OnDiscardCancel : CredentialEvent
        data object OnCopyPasswordClick : CredentialEvent
        data object OnCopyUsernameClick : CredentialEvent
        data object OnCopyEmailClick : CredentialEvent
        data object OnTotpAddClick : CredentialEvent
        data object OnTotpScanClick : CredentialEvent
        data object OnTotpScanCancel : CredentialEvent
        data object OnTotpScanError : CredentialEvent
        data object OnTotpReplaceConfirm : CredentialEvent
        data object OnTotpReplaceCancel : CredentialEvent
        data object OnTotpRemoveClick : CredentialEvent
        data object OnTotpRemoveConfirm : CredentialEvent
        data object OnTotpRemoveCancel : CredentialEvent
        data object OnCopyTotpClick : CredentialEvent
        data object OnBackClick : CredentialEvent
        data object OnDismissError : CredentialEvent
        data object OnGeneratePasswordClick : CredentialEvent
        data object OnAttachmentAddClick : CredentialEvent
        data object OnAttachmentRenameConfirm : CredentialEvent
        data object OnAttachmentRenameCancel : CredentialEvent
        data object OnAttachmentDeleteConfirm : CredentialEvent
        data object OnAttachmentDeleteCancel : CredentialEvent
    }

    sealed interface CredentialEffect {
        data object NavigateBack : CredentialEffect
        data class SaveCompleted(val credentialId: CredentialId) : CredentialEffect
        data class CopyToClipboard(val text: String) : CredentialEffect
        data class LaunchUrl(val url: String) : CredentialEffect
    }

    enum class PasswordStrength {
        EMPTY,
        TOO_SHORT,
        VERY_WEAK,
        WEAK,
        FAIR,
        GOOD,
        STRONG,
        VERY_STRONG,
    }

}
