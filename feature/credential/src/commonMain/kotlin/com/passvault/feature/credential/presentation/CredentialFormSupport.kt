package com.passvault.feature.credential.presentation

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.core.domain.model.UrlValue
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.takeCodePoints
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal const val MAX_TITLE_LENGTH = 200
internal const val MAX_USERNAME_LENGTH = 4_096
internal const val MAX_EMAIL_LENGTH = 4_096
internal const val MAX_PASSWORD_LENGTH = 4_096
internal const val MAX_URL_LENGTH = 2_048
internal const val MAX_URL_COUNT = 100
internal const val MAX_NOTES_LENGTH = 100_000
internal const val MAX_CUSTOM_FIELDS = 50
internal const val MAX_CUSTOM_FIELD_NAME_LENGTH = 200
internal const val MAX_CUSTOM_FIELD_VALUE_LENGTH = 20_000
internal const val DEFAULT_TOTP_DIGITS = 6
internal const val DEFAULT_TOTP_PERIOD = "30"
internal const val MAX_TOTP_SETUP_INPUT_LENGTH = 4_096
internal const val MAX_TOTP_PERIOD_DIGITS = 3
private const val MIN_CREDENTIAL_PASSWORD_LENGTH = 8

internal data class CredentialValidationResult(
    val titleError: UiText?,
    val urlErrors: Map<Int, UiText>,
    val fieldError: UiText?,
) {
    val isValid: Boolean get() = titleError == null && urlErrors.isEmpty() && fieldError == null
}

internal fun evaluateCredentialValidation(
    state: CredentialViewModel.CredentialState,
): CredentialValidationResult = CredentialValidationResult(
    titleError = validateCredentialTitle(state.title),
    urlErrors = validateCredentialUrls(state.urls),
    fieldError = validateCredentialFields(state),
)

private fun validateCredentialTitle(title: String): UiText? = when {
    title.isBlank() -> uiText(Res.string.validation_credential_title_required)
    title.codePointLength() > MAX_TITLE_LENGTH ->
        uiText(Res.string.validation_credential_title_too_long)
    else -> null
}

private fun validateCredentialUrls(urls: List<String>): Map<Int, UiText> =
    urls.mapIndexedNotNull { index, url ->
        when {
            url.isBlank() -> null
            url.codePointLength() > MAX_URL_LENGTH ->
                index to uiText(Res.string.validation_credential_url_too_long)
            normalizeCredentialUrl(url) == null ->
                index to uiText(Res.string.validation_credential_url_invalid)
            else -> null
        }
    }.toMap()

private fun validateCredentialFields(state: CredentialViewModel.CredentialState): UiText? = when {
    state.urls.size > MAX_URL_COUNT -> uiText(Res.string.validation_credential_urls_too_many)
    state.username.codePointLength() > MAX_USERNAME_LENGTH ->
        uiText(Res.string.validation_credential_username_too_long)
    state.email.codePointLength() > MAX_EMAIL_LENGTH ->
        uiText(Res.string.validation_credential_email_too_long)
    state.password.codePointLength() > MAX_PASSWORD_LENGTH ->
        uiText(Res.string.validation_credential_password_too_long)
    state.notes.codePointLength() > MAX_NOTES_LENGTH ->
        uiText(Res.string.validation_credential_notes_too_long)
    state.customFields.size > MAX_CUSTOM_FIELDS ->
        uiText(Res.string.validation_credential_custom_fields_too_many)
    state.customFields.any(::hasInvalidCustomFieldName) ->
        uiText(Res.string.validation_credential_custom_field_name)
    state.customFields.any { it.value.length > MAX_CUSTOM_FIELD_VALUE_LENGTH } ->
        uiText(Res.string.validation_credential_custom_field_value)
    else -> null
}

private fun hasInvalidCustomFieldName(field: CustomField): Boolean =
    field.name.isBlank() || field.name.codePointLength() > MAX_CUSTOM_FIELD_NAME_LENGTH

internal fun calculateCredentialPasswordStrength(
    password: String,
): CredentialViewModel.PasswordStrength = when {
    password.isEmpty() -> CredentialViewModel.PasswordStrength.EMPTY
    password.codePointLength() < MIN_CREDENTIAL_PASSWORD_LENGTH ->
        CredentialViewModel.PasswordStrength.TOO_SHORT
    else -> when (PasswordStrengthEvaluator.score(password)) {
        PasswordScore.UNKNOWN -> CredentialViewModel.PasswordStrength.EMPTY
        PasswordScore.VERY_WEAK -> CredentialViewModel.PasswordStrength.VERY_WEAK
        PasswordScore.WEAK -> CredentialViewModel.PasswordStrength.WEAK
        PasswordScore.FAIR -> CredentialViewModel.PasswordStrength.FAIR
        PasswordScore.GOOD -> CredentialViewModel.PasswordStrength.GOOD
        PasswordScore.STRONG -> CredentialViewModel.PasswordStrength.STRONG
        PasswordScore.VERY_STRONG -> CredentialViewModel.PasswordStrength.VERY_STRONG
    }
}

internal fun normalizeCredentialUrl(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    val candidate = if ("://" in trimmed) trimmed else "https://$trimmed"
    return UrlValue(candidate).host()?.let { candidate }
}

@OptIn(ExperimentalUuidApi::class)
internal fun createCredentialFromState(
    state: CredentialViewModel.CredentialState,
    now: Instant,
): Credential = Credential(
    id = state.credentialId ?: CredentialId(Uuid.random().toString()),
    type = state.credentialType,
    title = state.title.trim(),
    username = state.username.takeIf(String::isNotBlank)?.let(SensitiveText::from),
    email = state.email.takeIf(String::isNotBlank)?.let(SensitiveText::from),
    password = state.password.takeIf(String::isNotBlank)?.let(SensitiveText::from),
    urls = state.urls.filter(String::isNotBlank).map {
        UrlValue(requireNotNull(normalizeCredentialUrl(it)))
    },
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
    // Password history remains repository-owned so editable state never holds
    // every historic secret. save() reads and appends it independently.
    passwordHistory = emptyList(),
    createdAt = state.createdAt ?: now,
    updatedAt = now,
    lastUsedAt = state.lastUsedAt,
    passwordHealth = state.passwordHealth,
    totp = state.totpConfiguration?.deepCopy(),
)

internal fun Credential.toEditableState(
    folderState: CredentialViewModel.CredentialState,
): CredentialViewModel.CredentialState {
    val copiedPassword = password?.toStringUnsafe().orEmpty()
    val copiedTotp = totp?.deepCopy()
    return CredentialViewModel.CredentialState(
        credentialId = id,
        credentialType = type,
        title = title,
        username = username?.toStringUnsafe().orEmpty(),
        email = email?.toStringUnsafe().orEmpty(),
        password = copiedPassword,
        urls = urls.map(UrlValue::value),
        notes = notes?.toStringUnsafe().orEmpty(),
        customFields = customFields.map { it.copy(value = SensitiveText.from(it.value.toStringUnsafe())) },
        recoveryCodes = recoveryCodes.map { SensitiveText.from(it.toStringUnsafe()) },
        apiKeys = apiKeys.map { SensitiveText.from(it.toStringUnsafe()) },
        licenseKeys = licenseKeys.map { SensitiveText.from(it.toStringUnsafe()) },
        attachments = attachments,
        folderId = folderId,
        folders = folderState.folders,
        isLoadingFolders = folderState.isLoadingFolders,
        folderLoadFailed = folderState.folderLoadFailed,
        tagIds = tagIds,
        isFavorite = isFavorite,
        passwordHealth = passwordHealth,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastUsedAt = lastUsedAt,
        passwordStrength = calculateCredentialPasswordStrength(copiedPassword),
        totpConfiguration = copiedTotp,
        totpAlgorithm = copiedTotp?.algorithm ?: TotpAlgorithm.SHA1,
        totpDigits = copiedTotp?.digits ?: DEFAULT_TOTP_DIGITS,
        totpPeriodInput = copiedTotp?.periodSeconds?.toString() ?: DEFAULT_TOTP_PERIOD,
        isLoading = false,
        isCredentialLoaded = true,
        isNewCredential = false,
        isDirty = false,
    )
}

internal class CredentialCustomFieldEditor(
    private val state: MutableStateFlow<CredentialViewModel.CredentialState>,
) {
    fun beginDraft(fieldId: CustomFieldId) {
        state.update { current ->
            val field = current.customFields.firstOrNull { it.id == fieldId }
            if (field == null || fieldId in current.customFieldDrafts) {
                current
            } else {
                current.copy(
                    customFieldDrafts = current.customFieldDrafts + (fieldId to CredentialCustomFieldDraft(
                        name = field.name,
                        value = field.value.toStringUnsafe(),
                        isSecret = field.isSecret,
                    )),
                )
            }
        }
    }

    /** Existing whole-draft callers explicitly replace the tuple. */
    fun changeDraft(fieldId: CustomFieldId, draft: CredentialCustomFieldDraft) {
        updateDraft(fieldId) {
            draft.copy(
                name = draft.name.takeCodePoints(MAX_CUSTOM_FIELD_NAME_LENGTH),
                value = draft.value.takeCodePoints(MAX_CUSTOM_FIELD_VALUE_LENGTH),
            )
        }
    }

    /** UI input changes only its property of the latest owned draft. */
    fun changeDraftName(fieldId: CustomFieldId, name: String) {
        updateDraft(fieldId) { it.copy(name = name.takeCodePoints(MAX_CUSTOM_FIELD_NAME_LENGTH)) }
    }

    fun changeDraftValue(fieldId: CustomFieldId, value: String) {
        updateDraft(fieldId) { it.copy(value = value.takeCodePoints(MAX_CUSTOM_FIELD_VALUE_LENGTH)) }
    }

    fun changeDraftSecret(fieldId: CustomFieldId, isSecret: Boolean) {
        updateDraft(fieldId) { it.copy(isSecret = isSecret) }
    }

    private fun updateDraft(
        fieldId: CustomFieldId,
        transform: (CredentialCustomFieldDraft) -> CredentialCustomFieldDraft,
    ) {
        state.update { current ->
            val draft = current.customFieldDrafts[fieldId]
            if (draft == null) {
                current
            } else {
                current.copy(
                    customFieldDrafts = current.customFieldDrafts + (fieldId to transform(draft)),
                    errorMessage = null,
                )
            }
        }
    }

    fun cancelDraft(fieldId: CustomFieldId) {
        state.update { it.copy(customFieldDrafts = it.customFieldDrafts - fieldId) }
    }

    /** Page Save adopts every visible draft before validation and persistence. */
    fun commitDrafts(): Boolean {
        var current = state.value
        while (current.customFieldDrafts.isNotEmpty()) {
            if (current.customFieldDrafts.values.any { it.name.isBlank() }) {
                state.update { it.copy(errorMessage = uiText(Res.string.validation_credential_custom_field_name)) }
                return false
            }
            val replacements = current.customFields.mapNotNull { field ->
                current.customFieldDrafts[field.id]?.let { draft ->
                    field.id to field.copy(
                        name = draft.name,
                        value = SensitiveText.from(draft.value),
                        isSecret = draft.isSecret,
                    )
                }
            }.toMap()
            val updated = current.copy(
                customFields = current.customFields.map { replacements[it.id] ?: it },
                customFieldDrafts = emptyMap(),
                isDirty = current.hasUnsavedChanges,
            )
            if (state.compareAndSet(current, updated)) {
                current.customFields.filter { it.id in replacements }.forEach { it.value.clear() }
                break
            }
            replacements.values.forEach { it.value.clear() }
            current = state.value
        }
        return true
    }

    @OptIn(ExperimentalUuidApi::class)
    fun add(name: String, value: String, isSecret: Boolean) {
        if (state.value.customFields.size >= MAX_CUSTOM_FIELDS) {
            state.update {
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
            name = name.takeCodePoints(MAX_CUSTOM_FIELD_NAME_LENGTH),
            value = SensitiveText.from(value.takeCodePoints(MAX_CUSTOM_FIELD_VALUE_LENGTH)),
            isSecret = isSecret,
        )
        state.update { it.copy(customFields = it.customFields + field, errorMessage = null, isDirty = true) }
    }

    fun remove(fieldId: CustomFieldId) {
        while (true) {
            val current = state.value
            val removed = current.customFields.firstOrNull { it.id == fieldId } ?: return
            val updated = current.copy(
                customFields = current.customFields.filterNot { it.id == fieldId },
                customFieldDrafts = current.customFieldDrafts - fieldId,
                isDirty = true,
            )
            if (state.compareAndSet(current, updated)) {
                removed.value.clear()
                return
            }
        }
    }

    /** Row Save commits the owned draft, never a payload captured by an earlier composition. */
    fun commitDraft(fieldId: CustomFieldId) {
        updateField(fieldId, requireNonBlankName = true) { it.customFieldDrafts[fieldId] }
    }

    fun update(fieldId: CustomFieldId, name: String, value: String, isSecret: Boolean) {
        val draft = CredentialCustomFieldDraft(
            name = name.takeCodePoints(MAX_CUSTOM_FIELD_NAME_LENGTH),
            value = value.takeCodePoints(MAX_CUSTOM_FIELD_VALUE_LENGTH),
            isSecret = isSecret,
        )
        updateField(fieldId, requireNonBlankName = false) { draft }
    }

    private fun updateField(
        fieldId: CustomFieldId,
        requireNonBlankName: Boolean,
        draftForState: (CredentialViewModel.CredentialState) -> CredentialCustomFieldDraft?,
    ) {
        while (true) {
            val current = state.value
            val draft = draftForState(current) ?: return
            val replaced = current.customFields.firstOrNull { it.id == fieldId } ?: return
            if (requireNonBlankName && draft.name.isBlank()) {
                val invalid = current.copy(errorMessage = uiText(Res.string.validation_credential_custom_field_name))
                if (state.compareAndSet(current, invalid)) return
                continue
            }
            val replacementValue = SensitiveText.from(draft.value)
            val unchangedField = replaced.name == draft.name &&
                replaced.value == replacementValue && replaced.isSecret == draft.isSecret
            val updated = current.copy(
                customFields = if (unchangedField) current.customFields else current.customFields.map { field ->
                    if (field.id == fieldId) {
                        field.copy(name = draft.name, value = replacementValue, isSecret = draft.isSecret)
                    } else {
                        field
                    }
                },
                customFieldDrafts = current.customFieldDrafts - fieldId,
                isDirty = true,
            )
            if (state.compareAndSet(current, updated)) {
                // StateFlow CAS uses equality: a repeated row Save can succeed
                // without installing a new state reference. Keep the live owner
                // for unchanged fields and wipe only the unused replacement.
                if (unchangedField) replacementValue.clear() else replaced.value.clear()
                return
            }
            replacementValue.clear()
        }
    }
}
