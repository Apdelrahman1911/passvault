package com.passvault.feature.credential.presentation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.passvault.core.crypto.SecurePasswordGenerator
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.validation_credential_custom_field_name
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.core.testing.fakes.FakeCryptoEngine
import com.passvault.core.testing.fakes.FakeFolderRepository
import com.passvault.feature.credential.presentation.CredentialViewModel.CredentialEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Production VM/event/draft ownership with copied, synthetic in-memory persistence.
 * Capturing an event models an old callback; this is not a rendered row, click or IME test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CredentialCustomFieldSaveFreshnessTest {
    @Test
    fun `row Save captured before further edits adopts the latest draft and preserves another row`() = runTest {
        withEditor {
            model.onEvent(CredentialEvent.OnCustomFieldEditStarted(firstId))
            val composedDraft = model.state.value.customFieldDrafts.getValue(firstId)
            val rowSave = CredentialEvent.OnCustomFieldEditSaved(firstId)
            val oldValue = field(firstId).value
            val otherValue = field(secondId).value
            val latest = CredentialCustomFieldDraft("Latest name", "latest synthetic value", true)
            val otherDraft = CredentialCustomFieldDraft("Other pending", "other synthetic value", false)
            changeDraft(firstId, latest)
            changeDraft(secondId, otherDraft)

            // No scheduler drain or recomposition supplies the Save event with newer arguments.
            model.onEvent(rowSave)

            assertEquals("old first", composedDraft.value)
            assertEquals(latest.name, field(firstId).name)
            assertEquals(latest.value, field(firstId).value.toStringUnsafe())
            assertEquals(latest.isSecret, field(firstId).isSecret)
            assertTrue(oldValue.toStringUnsafe().all { it == '\u0000' })
            assertSame(otherValue, field(secondId).value)
            assertEquals(mapOf(secondId to otherDraft), model.state.value.customFieldDrafts)
            assertTrue(model.state.value.hasUnsavedChanges)
            val adoptedValue = field(firstId).value

            model.onEvent(CredentialEvent.OnSaveClick)
            runCurrent()

            stored { credential ->
                assertEquals(listOf(latest.name, otherDraft.name), credential.customFields.map { it.name })
                assertEquals(
                    listOf(latest.value, otherDraft.value),
                    credential.customFields.map { it.value.toStringUnsafe() },
                )
                assertEquals(listOf(true, false), credential.customFields.map { it.isSecret })
                assertNotSame(adoptedValue, credential.customFields.first().value)
            }
            assertTrue(model.state.value.customFieldDrafts.isEmpty())
            assertFalse(model.state.value.hasUnsavedChanges)
        }
    }

    @Test
    fun `a latest blank name rejects stale enabled row Save without losing its draft`() = runTest {
        withEditor {
            model.onEvent(CredentialEvent.OnCustomFieldEditStarted(firstId))
            val composedDraft = model.state.value.customFieldDrafts.getValue(firstId)
            val rowSave = CredentialEvent.OnCustomFieldEditSaved(firstId)
            val liveValue = field(firstId).value
            val latest = CredentialCustomFieldDraft(" \t", "keep this uncommitted value", true)
            changeDraft(firstId, latest)
            assertTrue(composedDraft.name.isNotBlank())

            model.onEvent(rowSave)

            assertSame(liveValue, field(firstId).value)
            assertEquals("First", field(firstId).name)
            assertEquals("old first", liveValue.toStringUnsafe())
            assertEquals(latest, model.state.value.customFieldDrafts[firstId])
            assertFalse(model.state.value.isDirty)
            assertTrue(model.state.value.hasUnsavedChanges)
            assertEquals(
                Res.string.validation_credential_custom_field_name,
                assertIs<UiText.Resource>(model.state.value.errorMessage).resource,
            )

            model.onEvent(CredentialEvent.OnSaveClick)
            runCurrent()

            assertFalse(model.state.value.isSaving)
            assertEquals(latest, model.state.value.customFieldDrafts[firstId])
            assertEquals("old first", stored { it.customFields.first().value.toStringUnsafe() })
        }
    }

    @Test
    fun `unchanged and duplicate row Saves preserve the live value owner`() = runTest {
        withEditor {
            val originalValue = field(firstId).value
            model.onEvent(CredentialEvent.OnCustomFieldEditStarted(firstId))
            val rowSave = CredentialEvent.OnCustomFieldEditSaved(firstId)

            model.onEvent(rowSave)

            assertSame(originalValue, field(firstId).value)
            assertEquals("old first", originalValue.toStringUnsafe())
            assertTrue(model.state.value.customFieldDrafts.isEmpty())
            val afterUnchangedSave = model.state.value
            model.onEvent(rowSave)
            assertSame(afterUnchangedSave, model.state.value)
            assertSame(originalValue, field(firstId).value)

            changeDraft(firstId, CredentialCustomFieldDraft("Changed", "duplicate must keep this", true))
            model.onEvent(rowSave)
            val adoptedValue = field(firstId).value
            val afterChangedSave = model.state.value
            model.onEvent(rowSave)

            assertSame(afterChangedSave, model.state.value)
            assertSame(adoptedValue, field(firstId).value)
            assertEquals("duplicate must keep this", adoptedValue.toStringUnsafe())
            assertTrue(originalValue.toStringUnsafe().all { it == '\u0000' })
            model.onEvent(CredentialEvent.OnSaveClick)
            runCurrent()
            assertEquals("duplicate must keep this", stored { it.customFields.first().value.toStringUnsafe() })
        }
    }

    @Test
    fun `late row Save cannot resurrect an unknown cancelled removed or locked draft`() = runTest {
        withEditor {
            val rowSave = CredentialEvent.OnCustomFieldEditSaved(firstId)
            val initial = model.state.value
            model.onEvent(CredentialEvent.OnCustomFieldEditSaved(CustomFieldId("unknown-field")))
            model.onEvent(rowSave)
            assertSame(initial, model.state.value)

            changeDraft(firstId, CredentialCustomFieldDraft("Cancelled", "must not adopt", true))
            model.onEvent(CredentialEvent.OnCustomFieldEditCancelled(firstId))
            val afterCancel = model.state.value
            model.onEvent(rowSave)
            assertSame(afterCancel, model.state.value)
            assertEquals("old first", field(firstId).value.toStringUnsafe())

            changeDraft(firstId, CredentialCustomFieldDraft("Removed", "must not resurrect", true))
            val removedValue = field(firstId).value
            model.onEvent(CredentialEvent.OnCustomFieldRemoved(firstId))
            val afterRemove = model.state.value
            model.onEvent(rowSave)
            assertSame(afterRemove, model.state.value)
            assertEquals(listOf(secondId), model.state.value.customFields.map { it.id })
            assertTrue(removedValue.toStringUnsafe().all { it == '\u0000' })

            changeDraft(secondId, CredentialCustomFieldDraft("Locked", "must not return", false))
            val saveAfterLock = CredentialEvent.OnCustomFieldEditSaved(secondId)
            model.clearForLock()
            val afterLock = model.state.value
            model.onEvent(saveAfterLock)
            assertSame(afterLock, model.state.value)
            assertTrue(model.state.value.customFields.isEmpty())
            assertTrue(model.state.value.customFieldDrafts.isEmpty())
            assertFalse(model.state.value.canSave)
            assertFalse(model.state.value.hasUnsavedChanges)
            assertEquals(listOf("old first", "old second"), stored { credential ->
                credential.customFields.map { it.value.toStringUnsafe() }
            })
        }
    }

    @Test
    fun `busy generation rejects row Save while the pending draft still exists`() = runTest {
        withEditor {
            val latest = CredentialCustomFieldDraft("Pending", "wait for generation", true)
            changeDraft(firstId, latest)
            val oldValue = field(firstId).value
            val rowSave = CredentialEvent.OnCustomFieldEditSaved(firstId)
            model.onEvent(CredentialEvent.OnGeneratePasswordClick)
            assertTrue(model.state.value.isGeneratingPassword)
            assertTrue(model.state.value.isBusy)
            val busyState = model.state.value

            model.onEvent(rowSave)

            assertSame(busyState, model.state.value)
            assertSame(oldValue, field(firstId).value)
            assertEquals(latest, model.state.value.customFieldDrafts[firstId])
            runCurrent()
            assertFalse(model.state.value.isBusy)
            model.onEvent(rowSave)
            assertEquals(latest.value, field(firstId).value.toStringUnsafe())
            assertEquals(latest.name, field(firstId).name)
            assertTrue(model.state.value.customFieldDrafts.isEmpty())
        }
    }

    @Test
    fun `explicit update keeps supplied payload bounds and does not become draft Save`() = runTest {
        withEditor {
            changeDraft(firstId, CredentialCustomFieldDraft("Ignored draft", "not the explicit payload", false))
            val originalValue = field(firstId).value
            val suppliedName = "🔐".repeat(MAX_CUSTOM_FIELD_NAME_LENGTH + 1)
            val suppliedValue = "🔐".repeat(MAX_CUSTOM_FIELD_VALUE_LENGTH + 1)

            model.onEvent(CredentialEvent.OnCustomFieldUpdated(firstId, suppliedName, suppliedValue, true))

            assertEquals(MAX_CUSTOM_FIELD_NAME_LENGTH, field(firstId).name.codePointLength())
            assertEquals("🔐".repeat(MAX_CUSTOM_FIELD_NAME_LENGTH), field(firstId).name)
            assertEquals("🔐".repeat(MAX_CUSTOM_FIELD_VALUE_LENGTH), field(firstId).value.toStringUnsafe())
            assertTrue(field(firstId).isSecret)
            assertTrue(originalValue.toStringUnsafe().all { it == '\u0000' })
            assertTrue(model.state.value.customFieldDrafts.isEmpty())

            // Compatibility: explicit updates can still act without an active draft;
            // blank-name validation for this existing API remains a page-Save concern.
            model.onEvent(CredentialEvent.OnCustomFieldUpdated(secondId, "", "explicit no-draft value", false))
            assertEquals("", field(secondId).name)
            assertEquals("explicit no-draft value", field(secondId).value.toStringUnsafe())
            assertFalse(field(secondId).isSecret)
            assertNull(model.state.value.errorMessage)
        }
    }

    @Test
    fun `separate control events preserve all accepted properties in every order before row Save`() = runTest {
        withEditor {
            val latest = CredentialCustomFieldDraft("Latest name", "latest input value", true)
            val otherDraft = CredentialCustomFieldDraft("Other row", "other pending value", false)
            val name = CredentialEvent.OnCustomFieldDraftNameChanged(firstId, latest.name)
            val value = CredentialEvent.OnCustomFieldDraftValueChanged(firstId, latest.value)
            val secret = CredentialEvent.OnCustomFieldDraftSecretChanged(firstId, latest.isSecret)
            val orders: List<List<CredentialEvent>> = listOf(
                listOf(name, value, secret),
                listOf(name, secret, value),
                listOf(value, name, secret),
                listOf(value, secret, name),
                listOf(secret, name, value),
                listOf(secret, value, name),
            )
            val originalValue = field(firstId).value
            changeDraft(secondId, otherDraft)

            orders.forEachIndexed { index, events ->
                model.onEvent(CredentialEvent.OnCustomFieldEditCancelled(firstId))
                model.onEvent(CredentialEvent.OnCustomFieldEditStarted(firstId))
                // Events contain only their changed property and were constructed
                // before these edits; no frame can refresh a captured draft tuple.
                events.forEach(model::onEvent)
                assertEquals(latest, model.state.value.customFieldDrafts[firstId], "control order $index")
                assertEquals(otherDraft, model.state.value.customFieldDrafts[secondId])
                assertSame(originalValue, field(firstId).value)
                assertEquals("old first", originalValue.toStringUnsafe())
            }

            model.onEvent(CredentialEvent.OnCustomFieldEditSaved(firstId))
            assertEquals(latest.value, field(firstId).value.toStringUnsafe())
            assertEquals(mapOf(secondId to otherDraft), model.state.value.customFieldDrafts)
            model.onEvent(CredentialEvent.OnSaveClick)
            runCurrent()
            stored { credential ->
                assertEquals(listOf(latest.name, otherDraft.name), credential.customFields.map { it.name })
                assertEquals(
                    listOf(latest.value, otherDraft.value),
                    credential.customFields.map { it.value.toStringUnsafe() },
                )
                assertEquals(listOf(true, false), credential.customFields.map { it.isSecret })
            }
        }
    }

    @Test
    fun `property edits retain Unicode bounds and the latest sibling properties`() = runTest {
        withEditor {
            model.onEvent(CredentialEvent.OnCustomFieldEditStarted(firstId))
            model.onEvent(CredentialEvent.OnCustomFieldDraftNameChanged(firstId, "🔐".repeat(201)))
            model.onEvent(CredentialEvent.OnCustomFieldDraftValueChanged(firstId, "🔐".repeat(20_001)))
            model.onEvent(CredentialEvent.OnCustomFieldDraftSecretChanged(firstId, true))
            val bounded = model.state.value.customFieldDrafts.getValue(firstId)
            assertEquals("🔐".repeat(MAX_CUSTOM_FIELD_NAME_LENGTH), bounded.name)
            assertEquals("🔐".repeat(MAX_CUSTOM_FIELD_VALUE_LENGTH), bounded.value)
            assertTrue(bounded.isSecret)

            model.onEvent(CredentialEvent.OnCustomFieldDraftValueChanged(firstId, "most recent value"))
            model.onEvent(CredentialEvent.OnCustomFieldDraftNameChanged(firstId, "Latest valid name"))
            model.onEvent(CredentialEvent.OnCustomFieldDraftSecretChanged(firstId, false))
            val latest = CredentialCustomFieldDraft("Latest valid name", "most recent value", false)
            assertEquals(latest, model.state.value.customFieldDrafts[firstId])
            model.onEvent(CredentialEvent.OnCustomFieldEditSaved(firstId))
            assertEquals(latest.name, field(firstId).name)
            assertEquals(latest.value, field(firstId).value.toStringUnsafe())
            assertFalse(field(firstId).isSecret)
        }
    }

    @Test
    fun `property events cannot create cancelled removed unknown or locked drafts`() = runTest {
        withEditor {
            val events = listOf(
                CredentialEvent.OnCustomFieldDraftNameChanged(firstId, "late name"),
                CredentialEvent.OnCustomFieldDraftValueChanged(firstId, "late value"),
                CredentialEvent.OnCustomFieldDraftSecretChanged(firstId, true),
            )
            val originalValue = field(firstId).value
            val initial = model.state.value
            events.forEach(model::onEvent)
            model.onEvent(CredentialEvent.OnCustomFieldDraftNameChanged(CustomFieldId("unknown"), "late"))
            assertSame(initial, model.state.value)
            assertSame(originalValue, field(firstId).value)
            assertEquals("old first", originalValue.toStringUnsafe())

            model.onEvent(CredentialEvent.OnCustomFieldEditStarted(firstId))
            events.forEach(model::onEvent)
            model.onEvent(CredentialEvent.OnCustomFieldEditCancelled(firstId))
            val afterCancel = model.state.value
            events.forEach(model::onEvent)
            assertSame(afterCancel, model.state.value)
            assertEquals("old first", originalValue.toStringUnsafe())
            assertFalse(model.state.value.hasUnsavedChanges)

            model.onEvent(CredentialEvent.OnCustomFieldEditStarted(firstId))
            model.onEvent(CredentialEvent.OnCustomFieldRemoved(firstId))
            val afterRemove = model.state.value
            events.forEach(model::onEvent)
            assertSame(afterRemove, model.state.value)
            assertEquals(listOf(secondId), model.state.value.customFields.map { it.id })

            model.onEvent(CredentialEvent.OnCustomFieldEditStarted(secondId))
            model.clearForLock()
            val afterLock = model.state.value
            events.forEach(model::onEvent)
            model.onEvent(CredentialEvent.OnCustomFieldDraftNameChanged(secondId, "locked name"))
            model.onEvent(CredentialEvent.OnCustomFieldDraftValueChanged(secondId, "locked value"))
            model.onEvent(CredentialEvent.OnCustomFieldDraftSecretChanged(secondId, false))
            assertSame(afterLock, model.state.value)
            assertTrue(model.state.value.customFields.isEmpty())
            assertTrue(model.state.value.customFieldDrafts.isEmpty())
        }
    }

    @Test
    fun `busy generation rejects every property event without dropping the active draft`() = runTest {
        withEditor {
            val pending = CredentialCustomFieldDraft("Pending", "pending value", false)
            val latest = CredentialCustomFieldDraft("Accepted", "accepted value", true)
            changeDraft(firstId, pending)
            val events = listOf(
                CredentialEvent.OnCustomFieldDraftNameChanged(firstId, latest.name),
                CredentialEvent.OnCustomFieldDraftValueChanged(firstId, latest.value),
                CredentialEvent.OnCustomFieldDraftSecretChanged(firstId, latest.isSecret),
            )
            val originalValue = field(firstId).value
            model.onEvent(CredentialEvent.OnGeneratePasswordClick)
            assertTrue(model.state.value.isBusy)
            val busy = model.state.value

            events.forEach(model::onEvent)

            assertSame(busy, model.state.value)
            assertEquals(pending, model.state.value.customFieldDrafts[firstId])
            assertSame(originalValue, field(firstId).value)
            runCurrent()
            assertFalse(model.state.value.isBusy)
            events.forEach(model::onEvent)
            assertEquals(latest, model.state.value.customFieldDrafts[firstId])
            assertSame(originalValue, field(firstId).value)
            assertEquals("old first", originalValue.toStringUnsafe())
        }
    }

    @Test
    fun `whole tuple draft change remains an explicit bounded replacement after property edits`() = runTest {
        withEditor {
            model.onEvent(CredentialEvent.OnCustomFieldEditStarted(firstId))
            model.onEvent(CredentialEvent.OnCustomFieldDraftNameChanged(firstId, "not retained"))
            model.onEvent(CredentialEvent.OnCustomFieldDraftValueChanged(firstId, "not retained"))
            model.onEvent(CredentialEvent.OnCustomFieldDraftSecretChanged(firstId, true))
            val originalValue = field(firstId).value
            val replacement = CredentialCustomFieldDraft("🔐".repeat(201), "🔐".repeat(20_001), false)

            model.onEvent(CredentialEvent.OnCustomFieldDraftChanged(firstId, replacement))

            val bounded = model.state.value.customFieldDrafts.getValue(firstId)
            assertEquals("🔐".repeat(MAX_CUSTOM_FIELD_NAME_LENGTH), bounded.name)
            assertEquals("🔐".repeat(MAX_CUSTOM_FIELD_VALUE_LENGTH), bounded.value)
            assertFalse(bounded.isSecret)
            assertSame(originalValue, field(firstId).value)
            assertEquals("old first", originalValue.toStringUnsafe())
            model.onEvent(CredentialEvent.OnCustomFieldEditCancelled(firstId))
            assertTrue(model.state.value.customFieldDrafts.isEmpty())
            assertFalse(model.state.value.hasUnsavedChanges)
        }
    }

    private suspend fun TestScope.withEditor(block: suspend EditorFixture.() -> Unit) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var ownedRepository: FakeCredentialRepository? = null
        var ownedStore: ViewModelStore? = null
        var modelJob: Job? = null
        var primaryFailure: Throwable? = null
        try {
            val repository = FakeCredentialRepository()
            ownedRepository = repository
            val store = ViewModelStore()
            ownedStore = store
            seedRepository(repository)
            val model = ViewModelProvider.create(
                store,
                viewModelFactory {
                    initializer {
                        CredentialViewModel(
                            repository,
                            FakeFolderRepository(),
                            SecurePasswordGenerator(FakeCryptoEngine()),
                        )
                    }
                },
            )[CredentialViewModel::class]
            modelJob = checkNotNull(model.viewModelScope.coroutineContext[Job])
            model.loadCredential(CredentialId(CREDENTIAL_ID))
            runCurrent()
            assertTrue(model.state.value.isCredentialLoaded)
            EditorFixture(model, repository).block()
        } catch (failure: Throwable) {
            primaryFailure = failure
            throw failure
        } finally {
            try {
                closeFixture(ownedStore, modelJob, ownedRepository)
            } catch (cleanupFailure: Throwable) {
                val failure = primaryFailure
                if (failure == null) throw cleanupFailure else failure.addSuppressed(cleanupFailure)
            }
        }
    }

    private suspend fun closeFixture(
        store: ViewModelStore?,
        modelJob: Job?,
        repository: FakeCredentialRepository?,
    ) = withContext(NonCancellable) {
        var cleanupFailure: Throwable? = null
        suspend fun release(action: suspend () -> Unit) {
            try {
                action()
            } catch (failure: Throwable) {
                val previousFailure = cleanupFailure
                if (previousFailure == null) cleanupFailure = failure else previousFailure.addSuppressed(failure)
            }
        }

        release { store?.clear() }
        release { modelJob?.cancel() }
        release {
            withTimeout(CLEANUP_TIMEOUT_MILLIS) { modelJob?.join() }
            assertTrue(modelJob?.isCompleted != false)
        }
        release { repository?.reset() }
        release { Dispatchers.resetMain() }
        cleanupFailure?.let { throw it }
    }

    private fun seedRepository(repository: FakeCredentialRepository) {
        val credential = TestData.credential(id = CREDENTIAL_ID).copy(
            customFields = listOf(
                CustomField(firstId, "First", SensitiveText.from("old first"), false),
                CustomField(secondId, "Second", SensitiveText.from("old second"), true),
            ),
        )
        try {
            repository.setupCredentials(credential)
        } finally {
            credential.clearSensitiveValues()
        }
    }

    private class EditorFixture(
        val model: CredentialViewModel,
        private val repository: FakeCredentialRepository,
    ) {
        fun field(id: CustomFieldId): CustomField = model.state.value.customFields.single { it.id == id }

        fun changeDraft(id: CustomFieldId, draft: CredentialCustomFieldDraft) {
            model.onEvent(CredentialEvent.OnCustomFieldEditStarted(id))
            model.onEvent(CredentialEvent.OnCustomFieldDraftChanged(id, draft))
        }

        fun <T> stored(block: (Credential) -> T): T {
            val copies = repository.getAllCredentials()
            return try {
                block(copies.single())
            } finally {
                copies.forEach(Credential::clearSensitiveValues)
            }
        }
    }

    private companion object {
        const val CREDENTIAL_ID = "synthetic-row-save-freshness"
        const val CLEANUP_TIMEOUT_MILLIS = 1_000L
        val firstId = CustomFieldId("first-field")
        val secondId = CustomFieldId("second-field")
    }
}
