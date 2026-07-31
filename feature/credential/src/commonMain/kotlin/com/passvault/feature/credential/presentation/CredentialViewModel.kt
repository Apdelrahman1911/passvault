package com.passvault.feature.credential.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordHistoryEntry
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.UrlValue
import com.passvault.core.domain.repository.CredentialRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * State owner for credential detail and edit flows.
 *
 * The ViewModel never logs or exposes repository exception text. It validates
 * user input before constructing a domain object and clears all wrapped
 * sensitive values when the flow is left or the vault is locked.
 */
class CredentialViewModel(
    private val credentialRepository: CredentialRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CredentialState())
    val state: StateFlow<CredentialState> = _state.asStateFlow()

    private val _effect = Channel<CredentialEffect>(Channel.BUFFERED)
    val effect: Flow<CredentialEffect> = _effect.receiveAsFlow()

    private val saveMutex = Mutex()
    private val deleteMutex = Mutex()
    private var loadJob: Job? = null
    private var saveJob: Job? = null
    private var deleteJob: Job? = null

    fun loadCredential(credentialId: CredentialId) {
        cancelPendingOperations()
        clearStateSensitiveValues()
        _state.value = CredentialState(
            credentialId = credentialId,
            isLoading = true,
            isNewCredential = false,
        )
        loadJob = viewModelScope.launch {
            val result = credentialRepository.getById(credentialId)
            if (result.isFailure) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = uiText(Res.string.error_credential_load),
                    )
                }
                return@launch
            }

            val credential = result.getOrNull()
            if (credential == null) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = uiText(Res.string.error_credential_not_found))
                }
                return@launch
            }

            try {
                val copiedCustomFields = credential.customFields.map { field ->
                    field.copy(value = SensitiveText.from(field.value.toStringUnsafe()))
                }
                val copiedHistory = credential.passwordHistory.map { entry ->
                    PasswordHistoryEntry(
                        password = SensitiveText.from(entry.password.toStringUnsafe()),
                        changedAt = entry.changedAt,
                    )
                }
                _state.value = CredentialState(
                    credentialId = credential.id,
                    credentialType = credential.type,
                    title = credential.title,
                    username = credential.username?.toStringUnsafe().orEmpty(),
                    email = credential.email?.toStringUnsafe().orEmpty(),
                    password = credential.password?.toStringUnsafe().orEmpty(),
                    urls = credential.urls.map(UrlValue::value),
                    notes = credential.notes?.toStringUnsafe().orEmpty(),
                    customFields = copiedCustomFields,
                    recoveryCodes = credential.recoveryCodes.map {
                        SensitiveText.from(it.toStringUnsafe())
                    },
                    apiKeys = credential.apiKeys.map {
                        SensitiveText.from(it.toStringUnsafe())
                    },
                    licenseKeys = credential.licenseKeys.map {
                        SensitiveText.from(it.toStringUnsafe())
                    },
                    attachments = credential.attachments,
                    passwordHistory = copiedHistory,
                    folderId = credential.folderId,
                    tagIds = credential.tagIds,
                    isFavorite = credential.isFavorite,
                    passwordHealth = credential.passwordHealth,
                    createdAt = credential.createdAt,
                    updatedAt = credential.updatedAt,
                    lastUsedAt = credential.lastUsedAt,
                    passwordStrength = calculatePasswordStrength(
                        credential.password?.toStringUnsafe().orEmpty(),
                    ),
                    isLoading = false,
                    isEditing = false,
                    isNewCredential = false,
                    isDirty = false,
                )
            } finally {
                credential.clearSensitiveValues()
            }
        }
    }

    fun createNewCredential(type: CredentialType) {
        cancelPendingOperations()
        clearStateSensitiveValues()
        _state.value = CredentialState(
            credentialType = type,
            isEditing = true,
            isNewCredential = true,
            isDirty = false,
        )
    }

    fun onEvent(event: CredentialEvent) {
        when (event) {
            is CredentialEvent.OnTitleChanged ->
                updateState { it.copy(title = event.title, titleError = null) }
            is CredentialEvent.OnUsernameChanged ->
                updateState { it.copy(username = event.username) }
            is CredentialEvent.OnEmailChanged ->
                updateState { it.copy(email = event.email) }
            is CredentialEvent.OnPasswordChanged -> {
                updateState {
                    it.copy(
                        password = event.password,
                        passwordStrength = calculatePasswordStrength(event.password),
                    )
                }
            }
            is CredentialEvent.OnNotesChanged ->
                updateState { it.copy(notes = event.notes) }
            is CredentialEvent.OnUrlAdded ->
                updateState { it.copy(urls = it.urls + event.url, urlErrors = emptyMap()) }
            is CredentialEvent.OnPrimaryUrlChanged ->
                onUrlChanged(0, event.url)
            is CredentialEvent.OnUrlChanged ->
                onUrlChanged(event.index, event.url)
            is CredentialEvent.OnUrlRemoved -> {
                updateState {
                    it.copy(
                        urls = it.urls.filterIndexed { index, _ -> index != event.index },
                        urlErrors = emptyMap(),
                    )
                }
            }
            is CredentialEvent.OnFolderChanged ->
                updateState { it.copy(folderId = event.folderId?.let(::FolderId)) }
            is CredentialEvent.OnTagsChanged ->
                updateState { it.copy(tagIds = event.tagIds.map(::TagId).toSet()) }
            is CredentialEvent.OnFavoriteChanged ->
                updateState { it.copy(isFavorite = event.isFavorite) }
            is CredentialEvent.OnCustomFieldAdded ->
                addCustomField(event.name, event.value, event.isSecret)
            is CredentialEvent.OnCustomFieldRemoved ->
                removeCustomField(event.fieldId)
            is CredentialEvent.OnCustomFieldUpdated ->
                updateCustomField(event.fieldId, event.name, event.value, event.isSecret)
            CredentialEvent.OnEditClick ->
                _state.update { it.copy(isEditing = true) }
            CredentialEvent.OnSaveClick -> saveCredential()
            CredentialEvent.OnDeleteClick ->
                _state.update { it.copy(showDeleteConfirmation = true) }
            CredentialEvent.OnDeleteConfirm -> {
                _state.update { it.copy(showDeleteConfirmation = false) }
                deleteCredential()
            }
            CredentialEvent.OnDeleteCancel ->
                _state.update { it.copy(showDeleteConfirmation = false) }
            CredentialEvent.OnCancelClick,
            CredentialEvent.OnBackClick,
            -> requestLeave()
            CredentialEvent.OnDiscardConfirm -> {
                _state.update { it.copy(showDiscardConfirmation = false) }
                _effect.trySend(CredentialEffect.NavigateBack)
            }
            CredentialEvent.OnDiscardCancel ->
                _state.update { it.copy(showDiscardConfirmation = false) }
            CredentialEvent.OnCopyPasswordClick ->
                copySensitiveValue(_state.value.password)
            CredentialEvent.OnCopyUsernameClick ->
                copySensitiveValue(_state.value.username)
            CredentialEvent.OnCopyEmailClick ->
                copySensitiveValue(_state.value.email)
            is CredentialEvent.OnCopyCustomFieldClick -> {
                _state.value.customFields
                    .firstOrNull { it.id == event.fieldId }
                    ?.value
                    ?.toStringUnsafe()
                    ?.let(::copySensitiveValue)
            }
            is CredentialEvent.OnLaunchUrlClick -> launchUrl(event.url)
            CredentialEvent.OnDismissError ->
                _state.update { it.copy(errorMessage = null, titleError = null, urlErrors = emptyMap()) }
            CredentialEvent.OnGeneratePasswordClick ->
                _effect.trySend(CredentialEffect.NavigateToGenerator)
            is CredentialEvent.OnUrlLaunchResult -> {
                if (!event.succeeded) {
                    _state.update { it.copy(errorMessage = uiText(Res.string.error_credential_link_open)) }
                }
            }
        }
    }

    private fun onUrlChanged(index: Int, value: String) {
        updateState {
            val urls = it.urls.toMutableList()
            while (urls.size <= index) urls += ""
            urls[index] = value
            it.copy(urls = urls, urlErrors = it.urlErrors - index)
        }
    }

    private fun requestLeave() {
        val current = _state.value
        if (current.isDirty && (current.isEditing || current.isNewCredential)) {
            _state.update { it.copy(showDiscardConfirmation = true) }
        } else {
            _effect.trySend(CredentialEffect.NavigateBack)
        }
    }

    private fun copySensitiveValue(value: String) {
        if (value.isNotEmpty()) _effect.trySend(CredentialEffect.CopyToClipboard(value))
    }

    private fun launchUrl(rawUrl: String) {
        val normalized = normalizeUrl(rawUrl)
        if (normalized == null) {
            _state.update { it.copy(errorMessage = uiText(Res.string.error_credential_link_invalid)) }
        } else {
            _effect.trySend(CredentialEffect.LaunchUrl(normalized))
        }
    }

    private fun evaluateValidation(state: CredentialState): ValidationResult {
        val titleError = when {
            state.title.isBlank() -> uiText(Res.string.validation_credential_title_required)
            state.title.length > MAX_TITLE_LENGTH -> uiText(Res.string.validation_credential_title_too_long)
            else -> null
        }
        val urlErrors = state.urls.mapIndexedNotNull { index, url ->
            if (url.isBlank()) {
                null
            } else if (url.length > MAX_URL_LENGTH) {
                index to uiText(Res.string.validation_credential_url_too_long)
            } else if (normalizeUrl(url) == null) {
                index to uiText(Res.string.validation_credential_url_invalid)
            } else {
                null
            }
        }.toMap()
        val fieldError = when {
            state.username.length > MAX_USERNAME_LENGTH ->
                uiText(Res.string.validation_credential_username_too_long)
            state.email.length > MAX_EMAIL_LENGTH ->
                uiText(Res.string.validation_credential_email_too_long)
            state.password.length > MAX_PASSWORD_LENGTH ->
                uiText(Res.string.validation_credential_password_too_long)
            state.notes.length > MAX_NOTES_LENGTH ->
                uiText(Res.string.validation_credential_notes_too_long)
            state.customFields.size > MAX_CUSTOM_FIELDS ->
                uiText(Res.string.validation_credential_custom_fields_too_many)
            state.customFields.any { it.name.isBlank() || it.name.length > MAX_CUSTOM_FIELD_NAME_LENGTH } ->
                uiText(Res.string.validation_credential_custom_field_name)
            state.customFields.any { it.value.length > MAX_CUSTOM_FIELD_VALUE_LENGTH } ->
                uiText(Res.string.validation_credential_custom_field_value)
            else -> null
        }
        return ValidationResult(titleError, urlErrors, fieldError)
    }

    private fun saveCredential() {
        val beforeSave = _state.value
        if (beforeSave.isBusy) return
        val validation = evaluateValidation(beforeSave)
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
                val current = _state.value
                if (!current.isSaving) return@withLock
                val credential = try {
                    createCredentialFromState(current)
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = uiText(Res.string.error_credential_invalid_data),
                        )
                    }
                    return@withLock
                }

                try {
                    val result = credentialRepository.save(credential)
                    if (result.isSuccess) {
                        val id = result.getOrThrow()
                        _state.update {
                            it.copy(
                                isSaving = false,
                                isEditing = false,
                                isNewCredential = false,
                                isDirty = false,
                                credentialId = id,
                                updatedAt = credential.updatedAt,
                            )
                        }
                        _effect.trySend(CredentialEffect.SaveCompleted(id))
                    } else {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = uiText(Res.string.error_credential_save),
                            )
                        }
                    }
                } finally {
                    credential.clearSensitiveValues()
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createCredentialFromState(state: CredentialState): Credential {
        val now = Clock.System.now()
        return Credential(
            id = state.credentialId ?: CredentialId(Uuid.random().toString()),
            type = state.credentialType,
            title = state.title.trim(),
            username = state.username.takeIf(String::isNotBlank)?.let(SensitiveText::from),
            email = state.email.takeIf(String::isNotBlank)?.let(SensitiveText::from),
            password = state.password.takeIf(String::isNotBlank)?.let(SensitiveText::from),
            urls = state.urls
                .filter(String::isNotBlank)
                .map { UrlValue(requireNotNull(normalizeUrl(it))) },
            notes = state.notes.takeIf(String::isNotBlank)?.let(SensitiveText::from),
            customFields = state.customFields.map { field ->
                field.copy(value = SensitiveText.from(field.value.toStringUnsafe()))
            },
            recoveryCodes = state.recoveryCodes.map { SensitiveText.from(it.toStringUnsafe()) },
            apiKeys = state.apiKeys.map { SensitiveText.from(it.toStringUnsafe()) },
            licenseKeys = state.licenseKeys.map { SensitiveText.from(it.toStringUnsafe()) },
            folderId = state.folderId,
            tagIds = state.tagIds,
            isFavorite = state.isFavorite,
            attachments = state.attachments,
            passwordHistory = state.passwordHistory.map {
                PasswordHistoryEntry(
                    password = SensitiveText.from(it.password.toStringUnsafe()),
                    changedAt = it.changedAt,
                )
            },
            createdAt = state.createdAt ?: now,
            updatedAt = now,
            lastUsedAt = state.lastUsedAt,
            passwordHealth = state.passwordHealth,
        )
    }

    private fun deleteCredential() {
        val credentialId = _state.value.credentialId ?: return
        if (_state.value.isBusy) return
        _state.update { it.copy(isDeleting = true, errorMessage = null) }
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            deleteMutex.withLock {
                val result = credentialRepository.delete(credentialId)
                if (result.isSuccess) {
                    _state.update { it.copy(isDeleting = false) }
                    _effect.trySend(CredentialEffect.NavigateBack)
                } else {
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

    private fun updateState(transform: (CredentialState) -> CredentialState) {
        _state.update { transform(it).copy(isDirty = true) }
    }

    fun clearForLock() {
        cancelPendingOperations()
        clearStateSensitiveValues()
        _state.value = CredentialState()
    }

    private fun cancelPendingOperations() {
        loadJob?.cancel()
        saveJob?.cancel()
        deleteJob?.cancel()
        loadJob = null
        saveJob = null
        deleteJob = null
    }

    private fun clearStateSensitiveValues() {
        val current = _state.value
        current.customFields.forEach { it.value.clear() }
        current.recoveryCodes.forEach(SensitiveText::clear)
        current.apiKeys.forEach(SensitiveText::clear)
        current.licenseKeys.forEach(SensitiveText::clear)
        current.passwordHistory.forEach { it.password.clear() }
    }

    private fun Credential.clearSensitiveValues() {
        username?.clear()
        email?.clear()
        password?.clear()
        notes?.clear()
        recoveryCodes.forEach(SensitiveText::clear)
        apiKeys.forEach(SensitiveText::clear)
        licenseKeys.forEach(SensitiveText::clear)
        customFields.forEach { it.value.clear() }
        passwordHistory.forEach { it.password.clear() }
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.EMPTY
        if (password.length < 8) return PasswordStrength.TOO_SHORT
        return when (PasswordStrengthEvaluator.score(password)) {
            PasswordScore.UNKNOWN -> PasswordStrength.EMPTY
            PasswordScore.VERY_WEAK -> PasswordStrength.VERY_WEAK
            PasswordScore.WEAK -> PasswordStrength.WEAK
            PasswordScore.FAIR -> PasswordStrength.FAIR
            PasswordScore.GOOD -> PasswordStrength.GOOD
            PasswordScore.STRONG -> PasswordStrength.STRONG
            PasswordScore.VERY_STRONG -> PasswordStrength.VERY_STRONG
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun addCustomField(name: String, value: String, isSecret: Boolean) {
        if (_state.value.customFields.size >= MAX_CUSTOM_FIELDS) {
            _state.update {
                it.copy(
                    errorMessage = uiText(
                        Res.string.validation_credential_custom_field_limit,
                        MAX_CUSTOM_FIELDS,
                    ),
                )
            }
            return
        }
        val field = CustomField(
            id = CustomFieldId(Uuid.random().toString()),
            name = name.take(MAX_CUSTOM_FIELD_NAME_LENGTH),
            value = SensitiveText.from(value.take(MAX_CUSTOM_FIELD_VALUE_LENGTH)),
            isSecret = isSecret,
        )
        updateState { it.copy(customFields = it.customFields + field, errorMessage = null) }
    }

    private fun removeCustomField(fieldId: CustomFieldId) {
        _state.value.customFields.firstOrNull { it.id == fieldId }?.value?.clear()
        updateState {
            it.copy(customFields = it.customFields.filterNot { field -> field.id == fieldId })
        }
    }

    private fun updateCustomField(
        fieldId: CustomFieldId,
        name: String,
        value: String,
        isSecret: Boolean,
    ) {
        updateState { state ->
            state.copy(
                customFields = state.customFields.map { field ->
                    if (field.id == fieldId) {
                        field.value.clear()
                        field.copy(
                            name = name.take(MAX_CUSTOM_FIELD_NAME_LENGTH),
                            value = SensitiveText.from(value.take(MAX_CUSTOM_FIELD_VALUE_LENGTH)),
                            isSecret = isSecret,
                        )
                    } else {
                        field
                    }
                },
            )
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
        val passwordHistory: List<PasswordHistoryEntry> = emptyList(),
        val folderId: FolderId? = null,
        val tagIds: Set<TagId> = emptySet(),
        val isFavorite: Boolean = false,
        val passwordHealth: PasswordHealth = PasswordHealth.UNKNOWN,
        val createdAt: Instant? = null,
        val updatedAt: Instant? = null,
        val lastUsedAt: Instant? = null,
        val passwordStrength: PasswordStrength = PasswordStrength.EMPTY,
        val isEditing: Boolean = false,
        val isNewCredential: Boolean = false,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val isDeleting: Boolean = false,
        val isDirty: Boolean = false,
        val errorMessage: UiText? = null,
        val titleError: UiText? = null,
        val urlErrors: Map<Int, UiText> = emptyMap(),
        val showDeleteConfirmation: Boolean = false,
        val showDiscardConfirmation: Boolean = false,
    ) {
        val isBusy: Boolean get() = isLoading || isSaving || isDeleting
        val canSave: Boolean get() = !isBusy
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
        data class OnPrimaryUrlChanged(val url: String) : CredentialEvent
        data class OnUrlChanged(val index: Int, val url: String) : CredentialEvent
        data class OnUrlRemoved(val index: Int) : CredentialEvent
        data class OnFolderChanged(val folderId: String?) : CredentialEvent
        data class OnTagsChanged(val tagIds: List<String>) : CredentialEvent
        data class OnFavoriteChanged(val isFavorite: Boolean) : CredentialEvent
        data class OnCustomFieldAdded(val name: String, val value: String, val isSecret: Boolean) : CredentialEvent
        data class OnCustomFieldRemoved(val fieldId: CustomFieldId) : CredentialEvent
        data class OnCustomFieldUpdated(
            val fieldId: CustomFieldId,
            val name: String,
            val value: String,
            val isSecret: Boolean,
        ) : CredentialEvent
        data class OnCopyCustomFieldClick(val fieldId: CustomFieldId) : CredentialEvent
        data class OnLaunchUrlClick(val url: String) : CredentialEvent
        data class OnUrlLaunchResult(val succeeded: Boolean) : CredentialEvent
        data object OnEditClick : CredentialEvent
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
        data object OnBackClick : CredentialEvent
        data object OnDismissError : CredentialEvent
        data object OnGeneratePasswordClick : CredentialEvent
    }

    sealed interface CredentialEffect {
        data object NavigateBack : CredentialEffect
        data object NavigateToGenerator : CredentialEffect
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

    private data class ValidationResult(
        val titleError: UiText?,
        val urlErrors: Map<Int, UiText>,
        val fieldError: UiText?,
    ) {
        val isValid: Boolean get() = titleError == null && urlErrors.isEmpty() && fieldError == null
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 200
        const val MAX_USERNAME_LENGTH = 4_096
        const val MAX_EMAIL_LENGTH = 4_096
        const val MAX_PASSWORD_LENGTH = 4_096
        const val MAX_URL_LENGTH = 2_048
        const val MAX_NOTES_LENGTH = 100_000
        const val MAX_CUSTOM_FIELDS = 50
        const val MAX_CUSTOM_FIELD_NAME_LENGTH = 200
        const val MAX_CUSTOM_FIELD_VALUE_LENGTH = 20_000

        fun normalizeUrl(raw: String): String? {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return null
            val candidate = if ("://" in trimmed) trimmed else "https://$trimmed"
            return UrlValue(candidate).host()?.let { candidate }
        }
    }
}
