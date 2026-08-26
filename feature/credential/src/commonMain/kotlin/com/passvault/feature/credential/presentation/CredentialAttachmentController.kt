package com.passvault.feature.credential.presentation

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.AttachmentAvailability
import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentCorruptedException
import com.passvault.core.domain.repository.AttachmentCountLimitException
import com.passvault.core.domain.repository.AttachmentFileTooLargeException
import com.passvault.core.domain.repository.AttachmentInvalidFileNameException
import com.passvault.core.domain.repository.AttachmentLegacyContentUnavailableException
import com.passvault.core.domain.repository.AttachmentRepository
import com.passvault.core.domain.repository.AttachmentTotalSizeLimitException
import com.passvault.feature.credential.AttachmentFileSelectionCancelled
import com.passvault.feature.credential.AttachmentFileStore
import com.passvault.feature.credential.AttachmentOutputAction
import com.passvault.feature.credential.PreparedAttachmentOutput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class CredentialAttachmentController(
    private val state: MutableStateFlow<CredentialViewModel.CredentialState>,
    private val scope: CoroutineScope,
    private val repository: AttachmentRepository,
    private val fileStore: AttachmentFileStore,
) {
    private var operationJob: Job? = null

    fun handle(event: CredentialViewModel.CredentialEvent) {
        when (event) {
            CredentialViewModel.CredentialEvent.OnAttachmentAddClick -> importAttachment()
            is CredentialViewModel.CredentialEvent.OnAttachmentOpenClick -> {
                writeAttachment(event.attachmentId, AttachmentOutputAction.OPEN)
            }
            is CredentialViewModel.CredentialEvent.OnAttachmentExportClick -> {
                writeAttachment(event.attachmentId, AttachmentOutputAction.EXPORT)
            }
            is CredentialViewModel.CredentialEvent.OnAttachmentRenameClick -> beginRename(event.attachmentId)
            is CredentialViewModel.CredentialEvent.OnAttachmentRenameChanged -> state.update {
                it.copy(attachmentRenameInput = event.fileName)
            }
            CredentialViewModel.CredentialEvent.OnAttachmentRenameConfirm -> renameAttachment()
            CredentialViewModel.CredentialEvent.OnAttachmentRenameCancel -> state.update {
                it.copy(attachmentRenameTarget = null, attachmentRenameInput = "")
            }
            is CredentialViewModel.CredentialEvent.OnAttachmentDeleteClick -> state.update {
                it.copy(attachmentDeleteTarget = event.attachmentId)
            }
            CredentialViewModel.CredentialEvent.OnAttachmentDeleteConfirm -> deleteAttachment()
            CredentialViewModel.CredentialEvent.OnAttachmentDeleteCancel -> state.update {
                it.copy(attachmentDeleteTarget = null)
            }
            else -> error("Unexpected attachment event")
        }
    }

    fun cancel() {
        operationJob?.cancel()
        operationJob = null
    }

    private fun importAttachment() {
        val credentialId = state.value.credentialId ?: return
        if (!beginOperation()) return
        launchOperation(AttachmentOperation.IMPORT) {
            val selected = fileStore.selectForImport()
            val selectionFailure = selected.exceptionOrNull()
            when {
                selectionFailure is AttachmentFileSelectionCancelled -> Unit
                selectionFailure != null -> setFailure(selectionFailure, AttachmentOperation.IMPORT)
                else -> applyImportedAttachment(
                    credentialId,
                    repository.import(credentialId, selected.getOrThrow()),
                )
            }
        }
    }

    private fun writeAttachment(attachmentId: AttachmentId, action: AttachmentOutputAction) {
        val current = state.value
        val credentialId = current.credentialId
        val attachment = current.attachments.firstOrNull { it.id == attachmentId }
        if (
            credentialId != null &&
            attachment?.availability == AttachmentAvailability.AVAILABLE &&
            beginOperation()
        ) {
            launchOperation(action.toOperation()) {
                val output = fileStore.createOutput(attachment, action)
                val outputFailure = output.exceptionOrNull()
                when {
                    outputFailure is AttachmentFileSelectionCancelled -> Unit
                    outputFailure != null -> setFailure(outputFailure, action.toOperation())
                    else -> copyAttachmentContent(credentialId, attachmentId, output.getOrThrow(), action)
                }
            }
        }
    }

    private fun beginRename(attachmentId: AttachmentId) {
        if (state.value.isAttachmentBusy) return
        val attachment = state.value.attachments.firstOrNull { it.id == attachmentId } ?: return
        state.update {
            it.copy(
                attachmentRenameTarget = attachmentId,
                attachmentRenameInput = if (
                    attachment.availability == AttachmentAvailability.CORRUPTED_FILENAME
                ) {
                    ""
                } else {
                    attachment.fileName
                },
            )
        }
    }

    private fun renameAttachment() {
        val current = state.value
        val credentialId = current.credentialId
        val attachmentId = current.attachmentRenameTarget
        val fileName = current.attachmentRenameInput
        if (credentialId != null && attachmentId != null && beginOperation()) {
            state.update { it.copy(attachmentRenameTarget = null, attachmentRenameInput = "") }
            launchOperation(AttachmentOperation.RENAME) {
                val result = repository.rename(credentialId, attachmentId, fileName)
                currentCoroutineContext().ensureActive()
                result.fold(
                    onSuccess = { renamed ->
                        state.update { loaded ->
                            if (loaded.credentialId == credentialId) {
                                loaded.copy(
                                    attachments = loaded.attachments.map {
                                        if (it.id == attachmentId) renamed else it
                                    },
                                )
                            } else {
                                loaded
                            }
                        }
                    },
                    onFailure = { setFailure(it, AttachmentOperation.RENAME) },
                )
            }
        }
    }

    private fun deleteAttachment() {
        val current = state.value
        val credentialId = current.credentialId
        val attachmentId = current.attachmentDeleteTarget
        if (credentialId != null && attachmentId != null && beginOperation()) {
            state.update { it.copy(attachmentDeleteTarget = null) }
            launchOperation(AttachmentOperation.DELETE) {
                val result = repository.delete(credentialId, attachmentId)
                currentCoroutineContext().ensureActive()
                if (result.isSuccess) {
                    state.update { loaded ->
                        if (loaded.credentialId == credentialId) {
                            loaded.copy(attachments = loaded.attachments.filterNot { it.id == attachmentId })
                        } else {
                            loaded
                        }
                    }
                } else {
                    setFailure(result.exceptionOrNull(), AttachmentOperation.DELETE)
                }
            }
        }
    }

    private suspend fun applyImportedAttachment(
        credentialId: CredentialId,
        result: Result<AttachmentMetadata>,
    ) {
        currentCoroutineContext().ensureActive()
        result.fold(
            onSuccess = { attachment ->
                state.update { current ->
                    if (current.credentialId == credentialId) {
                        current.copy(attachments = current.attachments + attachment)
                    } else {
                        current
                    }
                }
            },
            onFailure = { setFailure(it, AttachmentOperation.IMPORT) },
        )
    }

    private suspend fun copyAttachmentContent(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
        output: PreparedAttachmentOutput,
        action: AttachmentOutputAction,
    ) {
        var handedOff = false
        try {
            val result = repository.copyContentTo(
                credentialId = credentialId,
                attachmentId = attachmentId,
                sink = output.sink,
            )
            currentCoroutineContext().ensureActive()
            val contentFailure = result.exceptionOrNull()
            if (contentFailure != null) {
                setFailure(contentFailure, action.toOperation())
                return
            }

            val presentation = output.present()
            handedOff = presentation.isSuccess
            currentCoroutineContext().ensureActive()
            val presentationFailure = presentation.exceptionOrNull()
            when {
                presentationFailure is AttachmentFileSelectionCancelled -> Unit
                presentationFailure != null -> setFailure(presentationFailure, action.toOperation())
            }
        } finally {
            if (!handedOff) {
                withContext(NonCancellable) { runCatching { output.abort() } }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught") // UI boundary converts unexpected platform failures to safe text.
    private fun launchOperation(
        operation: AttachmentOperation,
        block: suspend () -> Unit,
    ) {
        operationJob = scope.launch {
            try {
                block()
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (error: Exception) {
                currentCoroutineContext().ensureActive()
                setFailure(error, operation)
            } finally {
                finishOperation()
            }
        }
    }

    private fun beginOperation(): Boolean {
        val current = state.value
        if (current.isBusy || !current.isCredentialLoaded) return false
        state.update { it.copy(isAttachmentBusy = true, errorMessage = null) }
        return true
    }

    private fun finishOperation() {
        state.update { it.copy(isAttachmentBusy = false) }
        operationJob = null
    }

    private fun setFailure(error: Throwable?, operation: AttachmentOperation) {
        state.update {
            it.copy(errorMessage = error.toSafeMessage(operation))
        }
    }
}

private enum class AttachmentOperation {
    IMPORT,
    OPEN,
    EXPORT,
    RENAME,
    DELETE,
}

private fun AttachmentOutputAction.toOperation(): AttachmentOperation = when (this) {
    AttachmentOutputAction.OPEN -> AttachmentOperation.OPEN
    AttachmentOutputAction.EXPORT -> AttachmentOperation.EXPORT
}

private fun Throwable?.toSafeMessage(operation: AttachmentOperation): UiText = when (this) {
    is AttachmentFileTooLargeException -> uiText(Res.string.error_attachment_file_too_large)
    is AttachmentCountLimitException -> uiText(Res.string.error_attachment_count_limit)
    is AttachmentTotalSizeLimitException -> uiText(Res.string.error_attachment_total_limit)
    is AttachmentInvalidFileNameException -> uiText(Res.string.error_attachment_invalid_filename)
    is AttachmentLegacyContentUnavailableException -> uiText(Res.string.error_attachment_legacy_unavailable)
    is AttachmentCorruptedException -> uiText(Res.string.error_attachment_corrupted)
    is IllegalStateException -> uiText(Res.string.error_attachment_unavailable)
    else -> when (operation) {
        AttachmentOperation.IMPORT -> uiText(Res.string.error_attachment_add)
        AttachmentOperation.OPEN -> uiText(Res.string.error_attachment_open)
        AttachmentOperation.EXPORT -> uiText(Res.string.error_attachment_export)
        AttachmentOperation.RENAME -> uiText(Res.string.error_attachment_rename)
        AttachmentOperation.DELETE -> uiText(Res.string.error_attachment_delete)
    }
}

private object UnavailableAttachmentRepository : AttachmentRepository {
    private val failure = IllegalStateException("Attachment repository is unavailable")

    override suspend fun import(
        credentialId: CredentialId,
        source: AttachmentContentSource,
    ) = Result.failure<AttachmentMetadata>(failure)

    override suspend fun rename(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
        newFileName: String,
    ) = Result.failure<AttachmentMetadata>(failure)

    override suspend fun delete(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
    ) = Result.failure<Unit>(failure)

    override suspend fun copyContentTo(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
        sink: AttachmentContentSink,
    ) = Result.failure<Unit>(failure)

    override suspend fun verify(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
    ) = Result.failure<Unit>(failure)
}

internal val defaultAttachmentRepository: AttachmentRepository = UnavailableAttachmentRepository
