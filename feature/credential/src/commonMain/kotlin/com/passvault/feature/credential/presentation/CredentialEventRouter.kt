package com.passvault.feature.credential.presentation

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.takeCodePoints
import com.passvault.feature.credential.presentation.CredentialViewModel.CredentialEffect
import com.passvault.feature.credential.presentation.CredentialViewModel.CredentialEvent
import com.passvault.feature.credential.presentation.CredentialViewModel.CredentialState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal data class CredentialEventCallbacks(
    val save: () -> Unit,
    val delete: () -> Unit,
    val recordUsage: () -> Unit,
    val generatePassword: () -> Unit,
)

internal class CredentialEventRouter(
    private val state: MutableStateFlow<CredentialState>,
    private val effect: MutableSharedFlow<CredentialEffect>,
    private val totp: CredentialTotpController,
    private val customFields: CredentialCustomFieldEditor,
    private val attachments: CredentialAttachmentController,
    private val callbacks: CredentialEventCallbacks,
) {
    fun handle(event: CredentialEvent) {
        if (state.value.isBusy && event.isEditMutation()) return
        when (event.category()) {
            EventCategory.BASIC_EDIT -> handleBasicEdit(event)
            EventCategory.ORGANIZATION -> handleOrganization(event)
            EventCategory.TOTP_SETUP -> handleTotpSetup(event)
            EventCategory.TOTP_ACTION -> handleTotpAction(event)
            EventCategory.CUSTOM_FIELD -> handleCustomField(event)
            EventCategory.ATTACHMENT -> attachments.handle(event)
            EventCategory.PERSISTENCE -> handlePersistence(event)
            EventCategory.EXTERNAL_ACTION -> handleExternalAction(event)
            EventCategory.OTHER -> handleOther(event)
        }
    }

    private fun handleBasicEdit(event: CredentialEvent) {
        when (event) {
            is CredentialEvent.OnTitleChanged -> updateState {
                it.copy(title = event.title.takeCodePoints(MAX_TITLE_LENGTH + 1), titleError = null)
            }
            is CredentialEvent.OnUsernameChanged -> updateState {
                it.copy(username = event.username.takeCodePoints(MAX_USERNAME_LENGTH + 1))
            }
            is CredentialEvent.OnEmailChanged -> updateState {
                it.copy(email = event.email.takeCodePoints(MAX_EMAIL_LENGTH + 1))
            }
            is CredentialEvent.OnPasswordChanged -> {
                val password = event.password.takeCodePoints(MAX_PASSWORD_LENGTH + 1)
                updateState {
                    it.copy(password = password, passwordStrength = calculateCredentialPasswordStrength(password))
                }
            }
            is CredentialEvent.OnNotesChanged -> updateState {
                it.copy(notes = event.notes.takeCodePoints(MAX_NOTES_LENGTH + 1))
            }
            else -> error("Unexpected basic credential event")
        }
    }

    private fun handleOrganization(event: CredentialEvent) {
        when (event) {
            is CredentialEvent.OnUrlAdded -> {
                if (state.value.urls.size >= MAX_URL_COUNT) {
                    state.update { it.copy(errorMessage = uiText(Res.string.validation_credential_urls_too_many)) }
                } else {
                    updateState {
                        it.copy(
                            urls = it.urls + event.url.takeCodePoints(MAX_URL_LENGTH + 1),
                            urlErrors = emptyMap(),
                        )
                    }
                }
            }
            is CredentialEvent.OnUrlChanged -> updateUrl(event.index, event.url)
            is CredentialEvent.OnUrlRemoved -> updateState {
                it.copy(
                    urls = it.urls.filterIndexed { index, _ -> index != event.index },
                    urlErrors = emptyMap(),
                )
            }
            is CredentialEvent.OnFolderChanged -> updateState {
                it.copy(folderId = event.folderId?.let(::FolderId))
            }
            is CredentialEvent.OnTagsChanged -> updateState {
                it.copy(tagIds = event.tagIds.map(::TagId).toSet())
            }
            is CredentialEvent.OnFavoriteChanged -> updateState { it.copy(isFavorite = event.isFavorite) }
            else -> error("Unexpected credential organization event")
        }
    }

    private fun handleTotpSetup(event: CredentialEvent) {
        when (event) {
            is CredentialEvent.OnTotpSetupInputChanged -> state.update {
                it.copy(
                    totpSetupInput = event.value.takeCodePoints(MAX_TOTP_SETUP_INPUT_LENGTH),
                    totpSetupError = null,
                )
            }
            is CredentialEvent.OnTotpAlgorithmChanged -> state.update {
                it.copy(totpAlgorithm = event.algorithm, totpSetupError = null)
            }
            is CredentialEvent.OnTotpDigitsChanged -> state.update {
                it.copy(totpDigits = event.digits, totpSetupError = null)
            }
            is CredentialEvent.OnTotpPeriodChanged -> state.update {
                it.copy(
                    totpPeriodInput = event.value.filter(Char::isDigit).take(MAX_TOTP_PERIOD_DIGITS),
                    totpSetupError = null,
                )
            }
            CredentialEvent.OnTotpAddClick -> totp.parseEnrollment(state.value.totpSetupInput)
            CredentialEvent.OnTotpScanClick -> state.update {
                it.copy(showTotpScanner = true, totpSetupError = null)
            }
            CredentialEvent.OnTotpScanCancel -> state.update { it.copy(showTotpScanner = false) }
            is CredentialEvent.OnTotpQrScanned -> totp.parseEnrollment(event.payload)
            CredentialEvent.OnTotpScanError -> state.update {
                it.copy(showTotpScanner = false, totpSetupError = uiText(Res.string.error_totp_scan))
            }
            else -> error("Unexpected TOTP setup event")
        }
    }

    private fun handleTotpAction(event: CredentialEvent) {
        when (event) {
            CredentialEvent.OnTotpReplaceConfirm -> totp.confirmReplacement()
            CredentialEvent.OnTotpReplaceCancel -> totp.cancelReplacement()
            CredentialEvent.OnTotpRemoveClick -> state.update {
                it.copy(showTotpRemoveConfirmation = true)
            }
            CredentialEvent.OnTotpRemoveConfirm -> totp.remove()
            CredentialEvent.OnTotpRemoveCancel -> state.update {
                it.copy(showTotpRemoveConfirmation = false)
            }
            CredentialEvent.OnCopyTotpClick -> copySensitive(state.value.currentTotpCode)
            else -> error("Unexpected TOTP action event")
        }
    }

    private fun handleCustomField(event: CredentialEvent) {
        when (event) {
            is CredentialEvent.OnCustomFieldAdded -> customFields.add(event.name, event.value, event.isSecret)
            is CredentialEvent.OnCustomFieldRemoved -> customFields.remove(event.fieldId)
            is CredentialEvent.OnCustomFieldUpdated ->
                customFields.update(event.fieldId, event.name, event.value, event.isSecret)
            is CredentialEvent.OnCopyCustomFieldClick -> state.value.customFields
                .firstOrNull { it.id == event.fieldId }
                ?.value
                ?.toStringUnsafe()
                ?.let(::copySensitive)
            else -> error("Unexpected custom-field event")
        }
    }

    private fun handlePersistence(event: CredentialEvent) {
        when (event) {
            CredentialEvent.OnSaveClick -> callbacks.save()
            CredentialEvent.OnDeleteClick -> {
                val current = state.value
                if (current.isCredentialLoaded && !current.isBusy) {
                    state.update { it.copy(showDeleteConfirmation = true) }
                }
            }
            CredentialEvent.OnDeleteConfirm -> {
                state.update { it.copy(showDeleteConfirmation = false) }
                callbacks.delete()
            }
            CredentialEvent.OnDeleteCancel -> state.update { it.copy(showDeleteConfirmation = false) }
            CredentialEvent.OnCancelClick,
            CredentialEvent.OnBackClick,
            -> requestLeave()
            CredentialEvent.OnDiscardConfirm -> {
                state.update { it.copy(showDiscardConfirmation = false) }
                effect.tryEmit(CredentialEffect.NavigateBack)
            }
            CredentialEvent.OnDiscardCancel -> state.update { it.copy(showDiscardConfirmation = false) }
            else -> error("Unexpected persistence event")
        }
    }

    private fun handleExternalAction(event: CredentialEvent) {
        when (event) {
            CredentialEvent.OnCopyPasswordClick -> copySensitive(state.value.password)
            CredentialEvent.OnCopyUsernameClick -> copySensitive(state.value.username)
            CredentialEvent.OnCopyEmailClick -> copySensitive(state.value.email)
            is CredentialEvent.OnCopyRecoveryCodeClick -> copySensitiveAt(state.value.recoveryCodes, event.index)
            is CredentialEvent.OnCopyApiKeyClick -> copySensitiveAt(state.value.apiKeys, event.index)
            is CredentialEvent.OnCopyLicenseKeyClick -> copySensitiveAt(state.value.licenseKeys, event.index)
            is CredentialEvent.OnLaunchUrlClick -> launchUrl(event.url)
            is CredentialEvent.OnUrlLaunchResult -> applyExternalResult(
                event.succeeded,
                Res.string.error_credential_link_open,
            )
            is CredentialEvent.OnCopyResult -> applyExternalResult(
                event.succeeded,
                Res.string.error_credential_copy,
            )
            else -> error("Unexpected external credential event")
        }
    }

    private fun handleOther(event: CredentialEvent) {
        when (event) {
            CredentialEvent.OnDismissError -> state.update {
                it.copy(
                    errorMessage = null,
                    titleError = null,
                    urlErrors = emptyMap(),
                    totpSetupError = null,
                )
            }
            CredentialEvent.OnGeneratePasswordClick -> callbacks.generatePassword()
            else -> error("Unexpected credential event")
        }
    }

    private fun requestLeave() {
        val current = state.value
        when {
            current.isBusy -> Unit
            current.showTotpScanner -> state.update { it.copy(showTotpScanner = false) }
            current.showTotpReplaceConfirmation -> totp.cancelReplacement()
            current.showTotpRemoveConfirmation -> state.update { it.copy(showTotpRemoveConfirmation = false) }
            current.showDeleteConfirmation -> state.update { it.copy(showDeleteConfirmation = false) }
            current.attachmentRenameTarget != null -> state.update {
                it.copy(attachmentRenameTarget = null, attachmentRenameInput = "")
            }
            current.attachmentDeleteTarget != null -> state.update { it.copy(attachmentDeleteTarget = null) }
            current.showDiscardConfirmation -> state.update { it.copy(showDiscardConfirmation = false) }
            current.isDirty -> state.update { it.copy(showDiscardConfirmation = true) }
            else -> effect.tryEmit(CredentialEffect.NavigateBack)
        }
    }

    private fun updateState(transform: (CredentialState) -> CredentialState) {
        state.update { transform(it).copy(isDirty = true) }
    }

    private fun updateUrl(index: Int, value: String) {
        if (index !in 0 until MAX_URL_COUNT) return
        updateState {
            val urls = it.urls.toMutableList()
            while (urls.size <= index) urls += ""
            urls[index] = value.takeCodePoints(MAX_URL_LENGTH + 1)
            it.copy(urls = urls, urlErrors = it.urlErrors - index)
        }
    }

    private fun copySensitive(value: String) {
        if (value.isNotEmpty()) effect.tryEmit(CredentialEffect.CopyToClipboard(value))
    }

    private fun copySensitiveAt(values: List<SensitiveText>, index: Int) {
        values.getOrNull(index)?.toStringUnsafe()?.let(::copySensitive)
    }

    private fun launchUrl(rawUrl: String) {
        val normalized = normalizeCredentialUrl(rawUrl)
        if (normalized == null) {
            state.update { it.copy(errorMessage = uiText(Res.string.error_credential_link_invalid)) }
        } else {
            effect.tryEmit(CredentialEffect.LaunchUrl(normalized))
        }
    }

    private fun applyExternalResult(
        succeeded: Boolean,
        failureMessage: org.jetbrains.compose.resources.StringResource,
    ) {
        if (succeeded) {
            callbacks.recordUsage()
        } else {
            state.update { it.copy(errorMessage = uiText(failureMessage)) }
        }
    }
}

private enum class EventCategory {
    BASIC_EDIT,
    ORGANIZATION,
    TOTP_SETUP,
    TOTP_ACTION,
    CUSTOM_FIELD,
    ATTACHMENT,
    PERSISTENCE,
    EXTERNAL_ACTION,
    OTHER,
}

@Suppress("LongMethod") // Exhaustive declarative mapping keeps new sealed events compiler-checked.
private fun CredentialEvent.category(): EventCategory = when (this) {
    is CredentialEvent.OnTitleChanged,
    is CredentialEvent.OnUsernameChanged,
    is CredentialEvent.OnEmailChanged,
    is CredentialEvent.OnPasswordChanged,
    is CredentialEvent.OnNotesChanged,
    -> EventCategory.BASIC_EDIT
    is CredentialEvent.OnUrlAdded,
    is CredentialEvent.OnUrlChanged,
    is CredentialEvent.OnUrlRemoved,
    is CredentialEvent.OnFolderChanged,
    is CredentialEvent.OnTagsChanged,
    is CredentialEvent.OnFavoriteChanged,
    -> EventCategory.ORGANIZATION
    is CredentialEvent.OnTotpSetupInputChanged,
    is CredentialEvent.OnTotpAlgorithmChanged,
    is CredentialEvent.OnTotpDigitsChanged,
    is CredentialEvent.OnTotpPeriodChanged,
    is CredentialEvent.OnTotpQrScanned,
    CredentialEvent.OnTotpAddClick,
    CredentialEvent.OnTotpScanClick,
    CredentialEvent.OnTotpScanCancel,
    CredentialEvent.OnTotpScanError,
    -> EventCategory.TOTP_SETUP
    CredentialEvent.OnTotpReplaceConfirm,
    CredentialEvent.OnTotpReplaceCancel,
    CredentialEvent.OnTotpRemoveClick,
    CredentialEvent.OnTotpRemoveConfirm,
    CredentialEvent.OnTotpRemoveCancel,
    CredentialEvent.OnCopyTotpClick,
    -> EventCategory.TOTP_ACTION
    is CredentialEvent.OnCustomFieldAdded,
    is CredentialEvent.OnCustomFieldRemoved,
    is CredentialEvent.OnCustomFieldUpdated,
    is CredentialEvent.OnCopyCustomFieldClick,
    -> EventCategory.CUSTOM_FIELD
    CredentialEvent.OnAttachmentAddClick,
    is CredentialEvent.OnAttachmentOpenClick,
    is CredentialEvent.OnAttachmentExportClick,
    is CredentialEvent.OnAttachmentRenameClick,
    is CredentialEvent.OnAttachmentRenameChanged,
    CredentialEvent.OnAttachmentRenameConfirm,
    CredentialEvent.OnAttachmentRenameCancel,
    is CredentialEvent.OnAttachmentDeleteClick,
    CredentialEvent.OnAttachmentDeleteConfirm,
    CredentialEvent.OnAttachmentDeleteCancel,
    -> EventCategory.ATTACHMENT
    CredentialEvent.OnSaveClick,
    CredentialEvent.OnDeleteClick,
    CredentialEvent.OnDeleteConfirm,
    CredentialEvent.OnDeleteCancel,
    CredentialEvent.OnCancelClick,
    CredentialEvent.OnBackClick,
    CredentialEvent.OnDiscardConfirm,
    CredentialEvent.OnDiscardCancel,
    -> EventCategory.PERSISTENCE
    CredentialEvent.OnCopyPasswordClick,
    CredentialEvent.OnCopyUsernameClick,
    CredentialEvent.OnCopyEmailClick,
    is CredentialEvent.OnCopyRecoveryCodeClick,
    is CredentialEvent.OnCopyApiKeyClick,
    is CredentialEvent.OnCopyLicenseKeyClick,
    is CredentialEvent.OnLaunchUrlClick,
    is CredentialEvent.OnUrlLaunchResult,
    is CredentialEvent.OnCopyResult,
    -> EventCategory.EXTERNAL_ACTION
    CredentialEvent.OnDismissError,
    CredentialEvent.OnGeneratePasswordClick,
    -> EventCategory.OTHER
}

private fun CredentialEvent.isEditMutation(): Boolean = when (category()) {
    EventCategory.BASIC_EDIT,
    EventCategory.ORGANIZATION,
    EventCategory.TOTP_SETUP,
    -> true
    EventCategory.TOTP_ACTION -> this != CredentialEvent.OnCopyTotpClick
    EventCategory.CUSTOM_FIELD -> this !is CredentialEvent.OnCopyCustomFieldClick
    EventCategory.ATTACHMENT -> false
    EventCategory.PERSISTENCE,
    EventCategory.EXTERNAL_ACTION,
    EventCategory.OTHER,
    -> false
}
