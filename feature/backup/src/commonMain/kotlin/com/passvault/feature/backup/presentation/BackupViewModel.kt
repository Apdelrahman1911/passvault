package com.passvault.feature.backup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.database.backup.VaultBackupService
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.backup.BackupFileSelectionCancelled
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
import kotlin.time.Instant
import kotlin.time.Clock

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

    private val _effect = Channel<BackupEffect>(Channel.BUFFERED)
    val effect: Flow<BackupEffect> = _effect.receiveAsFlow()

    private val operationMutex = Mutex()
    private var exportJob: Job? = null
    private var importJob: Job? = null
    private var previewJob: Job? = null

    init {
        refresh()
    }

    fun onEvent(event: BackupEvent) {
        when (event) {
            is BackupEvent.OnTabChanged -> _state.update { it.copy(selectedTab = event.tab) }
            is BackupEvent.OnPasswordChanged -> {
                if (event.password.length <= MAX_PASSWORD_LENGTH) {
                    _state.update { it.copy(exportPassword = event.password, errorMessage = null) }
                    evaluatePasswordStrength(event.password)
                }
            }
            BackupEvent.OnExportClick -> startExport()
            BackupEvent.OnImportFilePickerClick -> chooseImportFile()
            is BackupEvent.OnImportFileSelected -> {
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
            is BackupEvent.OnImportPasswordChanged -> {
                if (event.password.length <= MAX_PASSWORD_LENGTH) {
                    _state.update {
                        it.copy(
                            importPassword = event.password,
                            importPreview = null,
                            importError = null,
                        )
                    }
                    previewImportIfPossible()
                }
            }
            BackupEvent.OnImportClick -> requestRestoreConfirmation()
            BackupEvent.OnRestoreConfirmClick -> startImport()
            BackupEvent.OnRestoreCancelClick -> _state.update { it.copy(showRestoreConfirmation = false) }
            BackupEvent.OnDismissError -> _state.update { it.copy(errorMessage = null, importError = null) }
            BackupEvent.OnDismissSuccess -> _state.update { it.copy(successMessage = null) }
            BackupEvent.OnBackClick -> {
                if (!_state.value.isExporting && !_state.value.isImporting) {
                    _effect.trySend(BackupEffect.NavigateBack)
                }
            }
            BackupEvent.OnCancelOperation -> cancelOperation()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val metadata = vaultRepository.getMetadata().getOrThrow()
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
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = uiText(Res.string.error_backup_vault_info),
                    )
                }
            }
        }
    }

    private fun evaluatePasswordStrength(password: String) {
        val score = if (password.isEmpty()) PasswordScore.UNKNOWN else PasswordStrengthEvaluator.score(password)
        val strength = when {
            password.isEmpty() -> PasswordStrength.EMPTY
            password.length < MIN_PASSWORD_LENGTH -> PasswordStrength.TOO_SHORT
            score <= PasswordScore.WEAK -> PasswordStrength.WEAK
            score <= PasswordScore.GOOD -> PasswordStrength.GOOD
            else -> PasswordStrength.STRONG
        }
        _state.update { it.copy(passwordStrength = strength) }
    }

    private fun startExport() {
        val current = _state.value
        when {
            current.exportPassword.length < MIN_PASSWORD_LENGTH -> {
                _state.update {
                    it.copy(
                        errorMessage = uiText(
                            Res.string.error_backup_password_length,
                            MIN_PASSWORD_LENGTH,
                        ),
                    )
                }
                return
            }
            PasswordStrengthEvaluator.score(current.exportPassword) < PasswordScore.FAIR -> {
                _state.update {
                    it.copy(errorMessage = uiText(Res.string.error_backup_password_weak))
                }
                return
            }
            current.isExporting || current.isImporting -> return
        }

        exportJob?.cancel()
        val password = SensitiveText.from(current.exportPassword)
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
                var bytes: ByteArray? = null
                try {
                    _state.update { it.copy(exportProgress = 15) }
                    bytes = backupService.createBackup(password).getOrThrow()
                    _state.update { it.copy(exportProgress = 70) }
                    val file = fileStore.save(bytes, suggestedBackupName()).getOrThrow()
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportProgress = 100,
                            successMessage = uiText(
                                Res.string.message_backup_saved,
                                file.displayName,
                            ),
                        )
                    }
                    _effect.trySend(BackupEffect.ShowExportSuccess)
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportProgress = 0,
                            errorMessage = uiText(Res.string.error_backup_save),
                        )
                    }
                } finally {
                    bytes?.fill(0)
                    password.clear()
                    _state.update { it.copy(isExporting = false) }
                }
            }
        }
    }

    private fun chooseImportFile() {
        if (_state.value.isImporting || _state.value.isExporting) return
        viewModelScope.launch {
            fileStore.open()
                .onSuccess { file ->
                    _state.update {
                        it.copy(
                            selectedImportFile = file.path,
                            selectedImportDisplayName = file.displayName,
                            detectedImportFormat = detectFormat(file.displayName),
                            importPreview = null,
                            importError = null,
                        )
                    }
                    previewImportIfPossible()
                }
                .onFailure { error ->
                    if (error !is BackupFileSelectionCancelled) {
                        _state.update {
                            it.copy(importError = uiText(Res.string.error_backup_file_select))
                        }
                    }
                }
        }
    }

    private fun previewImportIfPossible() {
        val current = _state.value
        if (current.selectedImportFile == null ||
            current.detectedImportFormat != ImportFormat.ENCRYPTED ||
            current.importPassword.length < MIN_PASSWORD_LENGTH ||
            current.isImporting
        ) {
            return
        }

        previewJob?.cancel()
        val file = BackupFile(current.selectedImportFile, current.selectedImportDisplayName)
        val password = SensitiveText.from(current.importPassword)
        _state.update { it.copy(isAnalyzingFile = true, importError = null) }
        previewJob = viewModelScope.launch {
            var bytes: ByteArray? = null
            try {
                bytes = fileStore.read(file).getOrThrow()
                val preview = backupService.inspectBackup(bytes, password).getOrThrow()
                _state.update {
                    it.copy(
                        isAnalyzingFile = false,
                        importPreview = ImportPreview(
                            credentialCount = preview.credentialCount,
                            folderCount = preview.folderCount,
                            tagCount = preview.tagCount,
                            attachmentCount = preview.attachmentCount,
                            warnings = preview.warnings.map(::backupWarningText),
                        ),
                    )
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        isAnalyzingFile = false,
                        importPreview = null,
                        importError = uiText(Res.string.error_backup_invalid),
                    )
                }
            } finally {
                bytes?.fill(0)
                password.clear()
            }
        }
    }

    private fun requestRestoreConfirmation() {
        val current = _state.value
        when {
            current.selectedImportFile == null ->
                _state.update {
                    it.copy(importError = uiText(Res.string.error_backup_select_first))
                }
            current.detectedImportFormat != ImportFormat.ENCRYPTED ->
                _state.update {
                    it.copy(importError = uiText(Res.string.error_backup_select_passvault))
                }
            current.importPassword.length < MIN_PASSWORD_LENGTH ->
                _state.update {
                    it.copy(
                        importError = uiText(
                            Res.string.error_backup_password_length,
                            MIN_PASSWORD_LENGTH,
                        ),
                    )
                }
            current.importPreview == null ->
                _state.update {
                    it.copy(importError = uiText(Res.string.error_backup_validate_first))
                }
            current.isImporting || current.isExporting || current.isAnalyzingFile -> Unit
            else -> _state.update { it.copy(showRestoreConfirmation = true, importError = null) }
        }
    }

    private fun startImport() {
        val current = _state.value
        val path = current.selectedImportFile ?: return
        if (!current.showRestoreConfirmation || current.importPreview == null) return

        importJob?.cancel()
        val file = BackupFile(path, current.selectedImportDisplayName)
        val password = SensitiveText.from(current.importPassword)
        _state.update {
            it.copy(
                showRestoreConfirmation = false,
                isImporting = true,
                importProgress = 0,
                importError = null,
                successMessage = null,
            )
        }
        importJob = viewModelScope.launch {
            operationMutex.withLock {
                var bytes: ByteArray? = null
                try {
                    _state.update { it.copy(importProgress = 20) }
                    bytes = fileStore.read(file).getOrThrow()
                    _state.update { it.copy(importProgress = 45) }
                    val restored = backupService.restoreBackup(bytes, password).getOrThrow()
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
                            importPreview = ImportPreview(
                                credentialCount = restored.credentialCount,
                                folderCount = restored.folderCount,
                                tagCount = restored.tagCount,
                                attachmentCount = restored.attachmentCount,
                                warnings = restored.warnings.map(::backupWarningText),
                            ),
                        )
                    }
                    _effect.trySend(BackupEffect.ShowImportSuccess)
                    refresh()
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    _state.update {
                        it.copy(
                            isImporting = false,
                            importProgress = 0,
                            importError = uiText(Res.string.error_backup_invalid),
                        )
                    }
                } finally {
                    bytes?.fill(0)
                    password.clear()
                    _state.update { it.copy(isImporting = false) }
                }
            }
        }
    }

    private fun detectFormat(name: String): ImportFormat =
        if (name.endsWith(".pvault", ignoreCase = true) ||
            name.endsWith(".encrypted", ignoreCase = true) ||
            name.endsWith(".json", ignoreCase = true)
        ) {
            ImportFormat.ENCRYPTED
        } else {
            ImportFormat.UNKNOWN
        }

    private fun suggestedBackupName(): String =
        "passvault-${Clock.System.now().epochSeconds}.pvault"

    private fun backupWarningText(warning: VaultBackupService.BackupWarning): UiText =
        when (warning) {
            VaultBackupService.BackupWarning.ATTACHMENT_FILES_NOT_INCLUDED_IN_PREVIEW ->
                uiText(Res.string.warning_backup_attachment_preview)
            VaultBackupService.BackupWarning.ATTACHMENT_FILES_NOT_INCLUDED_AFTER_RESTORE ->
                uiText(Res.string.warning_backup_attachment_restored)
        }

    private fun cancelOperation() {
        exportJob?.cancel()
        importJob?.cancel()
        previewJob?.cancel()
        _state.update {
            it.copy(
                showRestoreConfirmation = false,
                isExporting = false,
                isImporting = false,
                isAnalyzingFile = false,
                exportProgress = 0,
                importProgress = 0,
            )
        }
    }

    fun clearForLock() {
        cancelOperation()
        _state.value = BackupState()
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
        val canExport: Boolean
            get() = exportPassword.length >= MIN_PASSWORD_LENGTH &&
                passwordStrength >= PasswordStrength.GOOD &&
                !isExporting &&
                !isImporting

        val canImport: Boolean
            get() = selectedImportFile != null &&
                detectedImportFormat == ImportFormat.ENCRYPTED &&
                importPassword.length >= MIN_PASSWORD_LENGTH &&
                importPreview != null &&
                !isImporting &&
                !isExporting &&
                !isAnalyzingFile
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
        data object ShowExportSuccess : BackupEffect
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

    private companion object {
        const val MIN_PASSWORD_LENGTH = 12
        const val MAX_PASSWORD_LENGTH = 1_024
    }
}
