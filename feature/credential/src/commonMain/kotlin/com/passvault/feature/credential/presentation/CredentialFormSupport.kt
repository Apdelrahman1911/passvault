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
                isDirty = true,
            )
            if (state.compareAndSet(current, updated)) {
                removed.value.clear()
                return
            }
        }
    }

    fun update(fieldId: CustomFieldId, name: String, value: String, isSecret: Boolean) {
        val replacementValue = SensitiveText.from(value.takeCodePoints(MAX_CUSTOM_FIELD_VALUE_LENGTH))
        val replacementName = name.takeCodePoints(MAX_CUSTOM_FIELD_NAME_LENGTH)
        while (true) {
            val current = state.value
            val replaced = current.customFields.firstOrNull { it.id == fieldId }
            if (replaced == null) {
                replacementValue.clear()
                return
            }
            val updated = current.copy(
                customFields = current.customFields.map { field ->
                    if (field.id == fieldId) {
                        field.copy(name = replacementName, value = replacementValue, isSecret = isSecret)
                    } else {
                        field
                    }
                },
                isDirty = true,
            )
            if (state.compareAndSet(current, updated)) {
                replaced.value.clear()
                return
            }
        }
    }
}
