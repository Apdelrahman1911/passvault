package com.passvault.feature.credential.presentation

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.AttachmentAvailability
import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentCorruptedException
import com.passvault.core.domain.repository.AttachmentRepository
import com.passvault.feature.credential.AttachmentFileSelectionCancelled
import com.passvault.feature.credential.AttachmentFileStore
import com.passvault.feature.credential.AttachmentOutputAction
import com.passvault.feature.credential.PreparedAttachmentOutput
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialAttachmentControllerTest {

    @Test
    fun `import selection cancellation is silent and always clears busy state`() = runTest {
        val state = loadedState()
        val repository = RecordingAttachmentRepository()
        val files = RecordingAttachmentFileStore().apply {
            importResult = Result.failure(AttachmentFileSelectionCancelled())
        }
        val controller = CredentialAttachmentController(state, this, repository, files)

        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentAddClick)
        runCurrent()

        assertFalse(state.value.isAttachmentBusy)
        assertEquals(null, state.value.errorMessage)
        assertEquals(0, repository.importCount)
    }

    @Test
    fun `import rename and delete update only the loaded credential attachment state`() = runTest {
        val state = loadedState()
        val repository = RecordingAttachmentRepository()
        val files = RecordingAttachmentFileStore()
        val controller = CredentialAttachmentController(state, this, repository, files)

        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentAddClick)
        runCurrent()
        assertEquals("document.txt", state.value.attachments.single().fileName)

        val id = state.value.attachments.single().id
        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentRenameClick(id))
        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentRenameChanged("renamed.txt"))
        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentRenameConfirm)
        runCurrent()
        assertEquals("renamed.txt", state.value.attachments.single().fileName)

        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentDeleteClick(id))
        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentDeleteConfirm)
        runCurrent()
        assertTrue(state.value.attachments.isEmpty())
        assertFalse(state.value.isAttachmentBusy)
    }

    @Test
    fun `authenticated content failure uses the safe corruption message and repository owns abort`() = runTest {
        val attachment = metadata()
        val state = loadedState(attachments = listOf(attachment))
        val repository = RecordingAttachmentRepository().apply {
            copyFailure = AttachmentCorruptedException()
        }
        val files = RecordingAttachmentFileStore()
        val controller = CredentialAttachmentController(state, this, repository, files)

        controller.handle(
            CredentialViewModel.CredentialEvent.OnAttachmentOpenClick(attachment.id),
        )
        runCurrent()

        val error = state.value.errorMessage as UiText.Resource
        assertEquals(Res.string.error_attachment_corrupted, error.resource)
        assertEquals(AttachmentOutputAction.OPEN, files.lastOutputAction)
        assertEquals(0, files.output.presentCount)
        assertEquals(1, files.output.abortCount)
        assertFalse(state.value.isAttachmentBusy)
    }

    @Test
    fun `platform presentation starts only after authenticated copy commits`() = runTest {
        val attachment = metadata()
        val state = loadedState(attachments = listOf(attachment))
        val repository = RecordingAttachmentRepository()
        val files = RecordingAttachmentFileStore().apply {
            output.onPresent = { assertTrue(repository.copyCommitted) }
        }
        val controller = CredentialAttachmentController(state, this, repository, files)

        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentOpenClick(attachment.id))
        runCurrent()

        assertTrue(repository.copyCommitted)
        assertEquals(1, files.output.presentCount)
        assertEquals(0, files.output.abortCount)
        assertFalse(state.value.isAttachmentBusy)
    }

    @Test
    fun `corrupted filename can be renamed or deleted but cannot create plaintext output`() = runTest {
        val attachment = metadata().copy(
            fileName = "unreadable-attachment-${metadata().id.value}",
            availability = AttachmentAvailability.CORRUPTED_FILENAME,
        )
        val state = loadedState(attachments = listOf(attachment))
        val repository = RecordingAttachmentRepository()
        val files = RecordingAttachmentFileStore()
        val controller = CredentialAttachmentController(state, this, repository, files)

        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentOpenClick(attachment.id))
        runCurrent()
        assertEquals(null, files.lastOutputAction)

        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentRenameClick(attachment.id))
        assertEquals(attachment.id, state.value.attachmentRenameTarget)
        assertEquals("", state.value.attachmentRenameInput)

        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentRenameChanged("repaired.txt"))
        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentRenameConfirm)
        runCurrent()
        assertEquals("repaired.txt", state.value.attachments.single().fileName)

        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentDeleteClick(attachment.id))
        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentDeleteConfirm)
        runCurrent()
        assertTrue(state.value.attachments.isEmpty())
    }

    @Test
    fun `controller cancellation cancels an active picker without mutating attachment state`() = runTest {
        val pickerStarted = CompletableDeferred<Unit>()
        val state = loadedState()
        val repository = RecordingAttachmentRepository()
        val files = RecordingAttachmentFileStore().apply {
            suspendImport = pickerStarted
        }
        val controller = CredentialAttachmentController(state, this, repository, files)

        controller.handle(CredentialViewModel.CredentialEvent.OnAttachmentAddClick)
        pickerStarted.await()
        assertTrue(state.value.isAttachmentBusy)
        controller.cancel()
        runCurrent()

        assertFalse(state.value.isAttachmentBusy)
        assertTrue(state.value.attachments.isEmpty())
        assertEquals(null, state.value.errorMessage)
    }

    private fun loadedState(
        attachments: List<AttachmentMetadata> = emptyList(),
    ) = MutableStateFlow(
        CredentialViewModel.CredentialState(
            credentialId = CREDENTIAL_ID,
            title = "Loaded",
            attachments = attachments,
            isCredentialLoaded = true,
        ),
    )

    private class RecordingAttachmentRepository : AttachmentRepository {
        var importCount = 0
        var copyFailure: Throwable? = null
        var copyCommitted = false

        override suspend fun import(
            credentialId: CredentialId,
            source: AttachmentContentSource,
        ): Result<AttachmentMetadata> {
            importCount++
            source.close()
            return Result.success(metadata())
        }

        override suspend fun rename(
            credentialId: CredentialId,
            attachmentId: AttachmentId,
            newFileName: String,
        ): Result<AttachmentMetadata> = Result.success(metadata().copy(fileName = newFileName))

        override suspend fun delete(
            credentialId: CredentialId,
            attachmentId: AttachmentId,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun copyContentTo(
            credentialId: CredentialId,
            attachmentId: AttachmentId,
            sink: AttachmentContentSink,
        ): Result<Unit> {
            val failure = copyFailure
            if (failure != null) {
                sink.abort()
                return Result.failure(failure)
            }
            sink.commit()
            copyCommitted = true
            return Result.success(Unit)
        }

        override suspend fun verify(
            credentialId: CredentialId,
            attachmentId: AttachmentId,
        ): Result<Unit> = Result.success(Unit)
    }

    private class RecordingAttachmentFileStore : AttachmentFileStore {
        var importResult: Result<AttachmentContentSource> = Result.success(EmptySource())
        var suspendImport: CompletableDeferred<Unit>? = null
        var lastOutputAction: AttachmentOutputAction? = null
        val output = RecordingPreparedOutput()

        override suspend fun selectForImport(): Result<AttachmentContentSource> {
            suspendImport?.let {
                it.complete(Unit)
                awaitCancellation()
            }
            return importResult
        }

        override suspend fun createOutput(
            attachment: AttachmentMetadata,
            action: AttachmentOutputAction,
        ): Result<PreparedAttachmentOutput> {
            lastOutputAction = action
            return Result.success(output)
        }
    }

    private class EmptySource : AttachmentContentSource {
        override val displayName = "document.txt"
        override val claimedMimeType: String? = "text/plain"
        override val declaredSizeBytes = 0L

        override suspend fun read(buffer: ByteArray): Int = -1
        override suspend fun close() = Unit
    }

    private class RecordingPreparedOutput : PreparedAttachmentOutput {
        var presentCount = 0
        var abortCount = 0
        var onPresent: () -> Unit = {}

        override val sink = object : AttachmentContentSink {
            override suspend fun write(buffer: ByteArray, byteCount: Int) = Unit
            override suspend fun commit() = Unit
            override suspend fun abort() = Unit
        }

        override suspend fun present(): Result<Unit> {
            presentCount++
            onPresent()
            return Result.success(Unit)
        }

        override suspend fun abort() {
            abortCount++
        }
    }

    private companion object {
        val CREDENTIAL_ID = CredentialId("credential-controller-test")

        fun metadata() = AttachmentMetadata(
            id = AttachmentId("attachment-controller-test"),
            fileName = "document.txt",
            mimeType = "text/plain",
            sizeBytes = 12,
            createdAt = Instant.fromEpochMilliseconds(1),
            availability = AttachmentAvailability.AVAILABLE,
        )
    }
}
