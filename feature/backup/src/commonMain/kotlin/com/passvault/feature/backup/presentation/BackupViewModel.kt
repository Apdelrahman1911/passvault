package com.passvault.feature.backup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.database.backup.VaultBackupService
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.BackupPasswordPolicy
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.hasWellFormedUnicode
import com.passvault.core.domain.model.takeCodePoints
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.backup.BackupFileSelectionCancelled
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Coordinates encrypted backup export and restore.
 *
 * The feature deliberately exposes one format and one restore policy:
 * backups are authenticated snapshots and restore replaces the vault only
 * after the complete file has been decrypted and validated. There is no
 * simulated CSV/merge path that could suggest data was imported when it was
 * not.
 */
class BackupViewModel(
    private val vaultRepository: VaultRepository,
    private val backupService: VaultBackupService,
    private val fileStore: BackupFileStore,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupState())
    val state: StateFlow<BackupState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<BackupEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<BackupEffect> = _effect.asSharedFlow()

    private val operationMutex = Mutex()
    private var exportJob: Job? = null
    private var importJob: Job? = null
    private var previewJob: Job? = null
    private var filePickerJob: Job? = null
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun onEvent(event: BackupEvent) {
        when (event) {
            is BackupEvent.OnPasswordChanged -> {
                val password = event.password.takeCodePoints(BackupPasswordPolicy.MAX_LENGTH + 1)
                _state.update {
                    it.copy(
                        exportPassword = password,
                        passwordStrength = passwordStrength(password),
                        errorMessage = password.backupPasswordInputError,
                    )
                }
            }
            BackupEvent.OnExportClick -> startExport()
            BackupEvent.OnImportFilePickerClick -> chooseImportFile()
            is BackupEvent.OnImportFileSelected -> {
                if (!_state.value.hasActiveOperation) {
                    previewJob?.cancel()
                    discardSelectedImportFile(viewModelScope, fileStore, _state.value)
                    val displayName = event.filePath.substringAfterLast('/').substringAfterLast('\\')
                    _state.update {
                        it.copy(
                            selectedImportFile = event.filePath,
                            selectedImportDisplayName = displayName,
                            detectedImportFormat = detectFormat(displayName),
                            importPreview = null,
                            importError = null,
                        )
                    }
                    previewImportIfPossible()
                }
            }
            is BackupEvent.OnImportPasswordChanged -> {
                val password = event.password.takeCodePoints(BackupPasswordPolicy.MAX_LENGTH + 1)
                _state.update {
                    it.copy(
                        importPassword = password,
                        importPreview = null,
                        importError = password.backupPasswordInputError,
                    )
                }
                previewImportIfPossible()
            }
            BackupEvent.OnImportClick -> _state.update(BackupState::withRestoreConfirmationRequest)
            BackupEvent.OnRestoreConfirmClick -> {
                val current = _state.value
                current.confirmedRestoreFile()?.let { file ->
                    launchImport(file, current.importPassword)
                }
            }
            else -> onSecondaryEvent(event)
        }
    }

    private fun onSecondaryEvent(event: BackupEvent) {
        when (event) {
            is BackupEvent.OnTabChanged -> {
                if (!_state.value.hasActiveOperation && !_state.value.showRestoreConfirmation) {
                    _state.update { it.copy(selectedTab = event.tab) }
                }
            }
            BackupEvent.OnRestoreCancelClick -> _state.update { it.copy(showRestoreConfirmation = false) }
            BackupEvent.OnDismissError -> _state.update { it.copy(errorMessage = null, importError = null) }
            BackupEvent.OnDismissSuccess -> _state.update { it.copy(successMessage = null) }
            BackupEvent.OnBackClick -> {
                val current = _state.value
                when {
                    current.showRestoreConfirmation ->
                        _state.update { it.copy(showRestoreConfirmation = false) }
                    current.hasActiveOperation -> Unit
                    else -> _effect.tryEmit(BackupEffect.NavigateBack)
                }
            }
            BackupEvent.OnCancelOperation -> cancelOperation()
            else -> Unit
        }
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val metadata = vaultRepository.getMetadata().getOrThrow()
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        isLoading = false,
                        vaultCreatedAt = metadata.createdAt,
                        vaultEntryCount = metadata.entryCount,
                        // Metadata is intentionally used here instead of
                        // decrypting every summary. It remains available
                        // while the vault is locked (including immediately
                        // after a restore) and avoids an O(n) unlock-only read.
                        credentialCount = metadata.entryCount,
                    )
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = uiText(Res.string.error_backup_vault_info),
                    )
                }
            }
        }
    }

    private fun startExport() {
        val current = _state.value
        val validationError = current.exportValidationError()
        when {
            validationError != null -> _state.update { it.copy(errorMessage = validationError) }
            current.hasActiveOperation -> Unit
            else -> launchExport(current.exportPassword)
        }
    }

    private fun launchExport(password: String) {
        exportJob?.cancel()
        _state.update {
            it.copy(
                isExporting = true,
                exportProgress = 0,
                errorMessage = null,
                successMessage = null,
            )
        }
        exportJob = viewModelScope.launch {
            operationMutex.withLock {
                try {
                    val file = createBackupFile(
                        backupService = backupService,
                        fileStore = fileStore,
                        password = password,
                        onProgress = { progress ->
                            _state.update { it.copy(exportProgress = progress) }
                        },
                    )
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportProgress = 100,
                            exportPassword = "",
                            passwordStrength = PasswordStrength.EMPTY,
                            successMessage = uiText(
                                Res.string.message_backup_saved,
                                file.displayName,
                            ),
                        )
                    }
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportProgress = 0,
                            errorMessage = uiText(Res.string.error_backup_save),
                        )
                    }
                } finally {
                    _state.update { it.copy(isExporting = false) }
                }
            }
        }
    }

    private fun chooseImportFile() {
        if (_state.value.hasActiveOperation) return
        filePickerJob?.cancel()
        _state.update { it.copy(isSelectingImportFile = true, importError = null) }
        filePickerJob = viewModelScope.launch {
            var unclaimedFile: BackupFile? = null
            try {
                val result = fileStore.open()
                unclaimedFile = result.getOrNull()
                currentCoroutineContext().ensureActive()
                result.fold(
                    onSuccess = { file ->
                        discardSelectedImportFile(viewModelScope, fileStore, _state.value)
                        _state.update {
                            it.copy(
                                selectedImportFile = file.path,
                                selectedImportDisplayName = file.displayName,
                                detectedImportFormat = detectFormat(file.displayName),
                                importPreview = null,
                                importError = null,
                            )
                        }
                        unclaimedFile = null
                        previewImportIfPossible()
                    },
                    onFailure = { error ->
                        when (error) {
                            is CancellationException -> throw error
                            is BackupFileSelectionCancelled -> Unit
                            else -> _state.update {
                                it.copy(importError = uiText(Res.string.error_backup_file_select))
                            }
                        }
                    },
                )
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update { it.copy(importError = uiText(Res.string.error_backup_file_select)) }
            } finally {
                unclaimedFile?.let { file ->
                    withContext(NonCancellable) {
                        discardIgnoringFailure(fileStore, file)
                    }
                }
                _state.update { it.copy(isSelectingImportFile = false) }
            }
        }
    }

    private fun previewImportIfPossible() {
        previewJob?.cancel()
        previewJob = null
        _state.update {
            it.copy(
                isAnalyzingFile = false,
                importPreview = null,
            )
        }
        val current = _state.value
        val canPreview = listOf(
            current.selectedImportFile != null,
            current.detectedImportFormat == ImportFormat.ENCRYPTED,
            BackupPasswordPolicy.acceptsExisting(current.importPassword),
            !current.isImporting,
        ).all { it }
        if (!canPreview) {
            return
        }
        val file = BackupFile(requireNotNull(current.selectedImportFile), current.selectedImportDisplayName)
        val passwordValue = current.importPassword
        _state.update { it.copy(isAnalyzingFile = true, importError = null) }
        previewJob = viewModelScope.launch {
            var password: SensitiveText? = null
            try {
                delay(IMPORT_PREVIEW_DEBOUNCE_MS)
                val sensitivePassword = SensitiveText.from(passwordValue)
                password = sensitivePassword
                val source = fileStore.source(file).getOrThrow()
                val preview = backupService.inspectBackup(source, sensitivePassword).getOrThrow()
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        isAnalyzingFile = false,
                        importPreview = preview.toImportPreview(),
                    )
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        isAnalyzingFile = false,
                        importPreview = null,
                        importError = uiText(Res.string.error_backup_invalid),
                    )
                }
            } finally {
                password?.clear()
            }
        }
    }

    private fun launchImport(file: BackupFile, password: String) {
        importJob?.cancel()
        _state.update { it.restoreStarted() }
        importJob = viewModelScope.launch {
            operationMutex.withLock {
                var restoreCompleted = false
                try {
                    val restored = restoreBackupFile(
                        backupService = backupService,
                        fileStore = fileStore,
                        file = file,
                        password = password,
                        onProgress = { progress ->
                            _state.update { it.copy(importProgress = progress) }
                        },
                    )
                    restoreCompleted = true
                    _state.update {
                        it.copy(
                            isImporting = false,
                            importProgress = 100,
                            successMessage = uiText(
                                Res.string.message_backup_restore_complete,
                            ),
                            importPassword = "",
                            selectedImportFile = null,
                            selectedImportDisplayName = "",
                            importPreview = restored.toImportPreview(),
                        )
                    }
                    _effect.tryEmit(BackupEffect.ShowImportSuccess)
                    refresh()
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    _state.update {
                        it.copy(
                            isImporting = false,
                            importProgress = 0,
                            importError = uiText(Res.string.error_backup_invalid),
                        )
                    }
                } finally {
                    if (shouldDiscardImportFileAfterRestore(file, _state.value, restoreCompleted)) {
                        // The restore has committed, so temporary-file cleanup
                        // must survive a cancellation arriving during teardown.
                        // A failed post-lock restore also clears the selected
                        // path from state and has no retry route, so its owned
                        // platform copy must be released here as well.
                        withContext(NonCancellable) {
                            discardIgnoringFailure(fileStore, file)
                        }
                    }
                    _state.update { it.copy(isImporting = false) }
                }
            }
        }
    }

    private fun cancelOperation() {
        exportJob?.cancel()
        importJob?.cancel()
        previewJob?.cancel()
        filePickerJob?.cancel()
        exportJob = null
        importJob = null
        previewJob = null
        filePickerJob = null
        _state.update {
            it.copy(
                showRestoreConfirmation = false,
                isExporting = false,
                isImporting = false,
                isAnalyzingFile = false,
                isSelectingImportFile = false,
                exportProgress = 0,
                importProgress = 0,
            )
        }
    }

    fun clearForLock() {
        refreshJob?.cancel()
        refreshJob = null
        discardSelectedImportFile(viewModelScope, fileStore, _state.value)
        cancelOperation()
        _state.value = BackupState()
    }

    /**
     * Scrubs UI-owned secrets when this ViewModel's confirmed restore is the
     * operation locking the vault. The import job must remain alive: cancelling
     * it here would interrupt the validated restore between lock and commit.
     * External/manual locks continue to use [clearForLock] and cancel it.
     */
    fun clearForRestoreLock() {
        exportJob?.cancel()
        previewJob?.cancel()
        filePickerJob?.cancel()
        refreshJob?.cancel()
        exportJob = null
        previewJob = null
        filePickerJob = null
        refreshJob = null
        _state.update { current ->
            BackupState(
                selectedTab = current.selectedTab,
                isImporting = current.isImporting,
                importProgress = current.importProgress,
            )
        }
    }

    data class BackupState(
        val selectedTab: BackupTab = BackupTab.EXPORT,
        val isLoading: Boolean = false,
        val vaultCreatedAt: Instant? = null,
        val vaultEntryCount: Int = 0,
        val credentialCount: Int = 0,
        val exportPassword: String = "",
        val passwordStrength: PasswordStrength = PasswordStrength.EMPTY,
        val isExporting: Boolean = false,
        val exportProgress: Int = 0,
        val selectedImportFile: String? = null,
        val selectedImportDisplayName: String = "",
        val importPassword: String = "",
        val isSelectingImportFile: Boolean = false,
        val detectedImportFormat: ImportFormat = ImportFormat.UNKNOWN,
        val isAnalyzingFile: Boolean = false,
        val isImporting: Boolean = false,
        val importProgress: Int = 0,
        val importPreview: ImportPreview? = null,
        val showRestoreConfirmation: Boolean = false,
        val importError: UiText? = null,
        val errorMessage: UiText? = null,
        val successMessage: UiText? = null,
    ) {
        val hasActiveOperation: Boolean
            get() = isExporting || isImporting || isAnalyzingFile || isSelectingImportFile

        val canExport: Boolean
            get() = BackupPasswordPolicy.acceptsNew(exportPassword) &&
                passwordStrength >= PasswordStrength.GOOD &&
                !hasActiveOperation

        val canImport: Boolean
            get() = selectedImportFile != null &&
                detectedImportFormat == ImportFormat.ENCRYPTED &&
                BackupPasswordPolicy.acceptsExisting(importPassword) &&
                importPreview != null &&
                !hasActiveOperation
    }

    data class ImportPreview(
        val credentialCount: Int,
        val folderCount: Int,
        val tagCount: Int,
        val attachmentCount: Int,
        val warnings: List<UiText>,
    )

    sealed interface BackupEvent {
        data class OnTabChanged(val tab: BackupTab) : BackupEvent
        data class OnPasswordChanged(val password: String) : BackupEvent
        data object OnExportClick : BackupEvent
        data object OnImportFilePickerClick : BackupEvent
        data class OnImportFileSelected(val filePath: String) : BackupEvent
        data class OnImportPasswordChanged(val password: String) : BackupEvent
        data object OnImportClick : BackupEvent
        data object OnRestoreConfirmClick : BackupEvent
        data object OnRestoreCancelClick : BackupEvent
        data object OnDismissError : BackupEvent
        data object OnDismissSuccess : BackupEvent
        data object OnBackClick : BackupEvent
        data object OnCancelOperation : BackupEvent
    }

    sealed interface BackupEffect {
        data object NavigateBack : BackupEffect
        data object ShowImportSuccess : BackupEffect
    }

    enum class BackupTab {
        EXPORT,
        IMPORT,
    }

    enum class ImportFormat {
        ENCRYPTED,
        UNKNOWN,
    }

    enum class PasswordStrength {
        EMPTY,
        TOO_SHORT,
        WEAK,
        GOOD,
        STRONG,
    }
}

private fun passwordStrength(password: String): BackupViewModel.PasswordStrength {
    val score = if (password.isEmpty()) PasswordScore.UNKNOWN else PasswordStrengthEvaluator.score(password)
    return when {
        password.isEmpty() -> BackupViewModel.PasswordStrength.EMPTY
        password.codePointLength() < BackupPasswordPolicy.MIN_LENGTH -> BackupViewModel.PasswordStrength.TOO_SHORT
        score <= PasswordScore.WEAK -> BackupViewModel.PasswordStrength.WEAK
        score <= PasswordScore.GOOD -> BackupViewModel.PasswordStrength.GOOD
        else -> BackupViewModel.PasswordStrength.STRONG
    }
}

private fun BackupViewModel.BackupState.exportValidationError(): UiText? = when {
    exportPassword.codePointLength() < BackupPasswordPolicy.MIN_LENGTH -> uiText(
        Res.string.error_backup_password_length,
        BackupPasswordPolicy.MIN_LENGTH,
    )
    exportPassword.codePointLength() > BackupPasswordPolicy.MAX_LENGTH -> uiText(
        Res.string.error_backup_password_too_long,
        BackupPasswordPolicy.MAX_LENGTH,
    )
    !exportPassword.hasWellFormedUnicode() -> uiText(Res.string.error_backup_password_invalid)
    PasswordStrengthEvaluator.score(exportPassword) < PasswordScore.FAIR ->
        uiText(Res.string.error_backup_password_weak)
    else -> null
}

private fun BackupViewModel.BackupState.withRestoreConfirmationRequest(): BackupViewModel.BackupState {
    val validationError = when {
        selectedImportFile == null -> uiText(Res.string.error_backup_select_first)
        detectedImportFormat != BackupViewModel.ImportFormat.ENCRYPTED ->
            uiText(Res.string.error_backup_select_passvault)
        importPassword.codePointLength() > BackupPasswordPolicy.MAX_LENGTH -> uiText(
            Res.string.error_backup_password_too_long,
            BackupPasswordPolicy.MAX_LENGTH,
        )
        !importPassword.hasWellFormedUnicode() -> uiText(Res.string.error_backup_password_invalid)
        !BackupPasswordPolicy.acceptsExisting(importPassword) || importPreview == null ->
            uiText(Res.string.error_backup_validate_first)
        else -> null
    }
    return when {
        hasActiveOperation -> this
        validationError != null -> copy(importError = validationError)
        else -> copy(showRestoreConfirmation = true, importError = null)
    }
}

private fun BackupViewModel.BackupState.confirmedRestoreFile(): BackupFile? =
    selectedImportFile
        ?.takeIf { showRestoreConfirmation && importPreview != null }
        ?.let { path -> BackupFile(path, selectedImportDisplayName) }

private fun BackupViewModel.BackupState.restoreStarted(): BackupViewModel.BackupState = copy(
    showRestoreConfirmation = false,
    isImporting = true,
    importProgress = 0,
    importError = null,
    successMessage = null,
)

private fun detectFormat(name: String): BackupViewModel.ImportFormat =
    if (name.endsWith(".pvault", ignoreCase = true) ||
        name.endsWith(".encrypted", ignoreCase = true) ||
        name.endsWith(".json", ignoreCase = true)
    ) {
        BackupViewModel.ImportFormat.ENCRYPTED
    } else {
        BackupViewModel.ImportFormat.UNKNOWN
    }

private val String.backupPasswordInputError: UiText?
    get() = when {
        codePointLength() > BackupPasswordPolicy.MAX_LENGTH -> uiText(
            Res.string.error_backup_password_too_long,
            BackupPasswordPolicy.MAX_LENGTH,
        )
        !hasWellFormedUnicode() -> uiText(Res.string.error_backup_password_invalid)
        else -> null
    }

private const val IMPORT_PREVIEW_DEBOUNCE_MS = 300L

private fun VaultBackupService.BackupInspection.toImportPreview(): BackupViewModel.ImportPreview =
    BackupViewModel.ImportPreview(
        credentialCount = credentialCount,
        folderCount = folderCount,
        tagCount = tagCount,
        attachmentCount = attachmentCount,
        warnings = warnings.map { warning ->
            when (warning) {
                VaultBackupService.BackupWarning.ATTACHMENT_FILES_NOT_INCLUDED_IN_PREVIEW ->
                    uiText(Res.string.warning_backup_attachment_preview)
                VaultBackupService.BackupWarning.ATTACHMENT_FILES_NOT_INCLUDED_AFTER_RESTORE ->
                    uiText(Res.string.warning_backup_attachment_restored)
            }
        },
    )

private suspend fun createBackupFile(
    backupService: VaultBackupService,
    fileStore: BackupFileStore,
    password: String,
    onProgress: (Int) -> Unit,
): BackupFile {
    val sensitivePassword = SensitiveText.from(password)
    try {
        val output = fileStore.create(
            "passvault-${Clock.System.now().epochSeconds}.pvault",
        ).getOrThrow()
        backupService.createBackup(
            password = sensitivePassword,
            sink = output.sink,
            onProgress = onProgress,
        ).getOrThrow()
        return output.file
    } finally {
        sensitivePassword.clear()
    }
}

private suspend fun restoreBackupFile(
    backupService: VaultBackupService,
    fileStore: BackupFileStore,
    file: BackupFile,
    password: String,
    onProgress: (Int) -> Unit,
): VaultBackupService.BackupInspection {
    val sensitivePassword = SensitiveText.from(password)
    try {
        onProgress(20)
        val source = fileStore.source(file).getOrThrow()
        return backupService.restoreBackup(source, sensitivePassword, onProgress).getOrThrow()
    } finally {
        sensitivePassword.clear()
    }
}
