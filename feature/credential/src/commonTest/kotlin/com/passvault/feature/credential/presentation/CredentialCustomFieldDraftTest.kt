package com.passvault.feature.credential.presentation

import app.cash.turbine.test
import com.passvault.core.crypto.SecurePasswordGenerator
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.validation_credential_custom_field_name
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.core.testing.fakes.FakeCryptoEngine
import com.passvault.core.testing.fakes.FakeFolderRepository
import com.passvault.feature.credential.presentation.CredentialViewModel.CredentialEffect
import com.passvault.feature.credential.presentation.CredentialViewModel.CredentialEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Exercises real VM/event/save ownership; the repository is synthetic, not a UI/device test. */
@OptIn(ExperimentalCoroutinesApi::class)
class CredentialCustomFieldDraftTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeCredentialRepository()
    private val owners = mutableListOf<CredentialViewModel>()
    private val firstId = CustomFieldId("first-field")
    private val secondId = CustomFieldId("second-field")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        owners.forEach(CredentialViewModel::clearForLock)
        dispatcher.scheduler.runCurrent()
        repository.reset()
        Dispatchers.resetMain()
    }

    @Test
    fun `page Save adopts every active draft without requiring row Save`() = runTest(dispatcher) {
        val model = loadedModel()
        change(model, firstId, CredentialCustomFieldDraft("Changed first", "new first", true))
        change(model, secondId, CredentialCustomFieldDraft("Changed second", "new second", false))
        assertFalse(model.state.value.isDirty)
        assertTrue(model.state.value.hasUnsavedChanges)

        model.effect.test {
            model.onEvent(CredentialEvent.OnSaveClick)
            runCurrent()
            assertIs<CredentialEffect.SaveCompleted>(awaitItem())
            expectNoEvents()
        }

        stored { credential ->
            assertEquals(listOf("Changed first", "Changed second"), credential.customFields.map { it.name })
            assertEquals(listOf("new first", "new second"), credential.customFields.map { it.value.toStringUnsafe() })
            assertEquals(listOf(true, false), credential.customFields.map { it.isSecret })
        }
        assertTrue(model.state.value.customFieldDrafts.isEmpty())
        assertFalse(model.state.value.hasUnsavedChanges)
    }

    @Test
    fun `toolbar and central Back both warn about inline draft changes`() = runTest(dispatcher) {
        val model = loadedModel()
        val draft = CredentialCustomFieldDraft("First", "visible unsaved value", false)
        change(model, firstId, draft)

        model.effect.test {
            model.onEvent(CredentialEvent.OnCancelClick)
            assertTrue(model.state.value.showDiscardConfirmation)
            expectNoEvents()
            model.onEvent(CredentialEvent.OnDiscardCancel)
            assertEquals(draft, model.state.value.customFieldDrafts[firstId])
            model.onEvent(CredentialEvent.OnBackClick)
            assertTrue(model.state.value.showDiscardConfirmation)
            expectNoEvents()
            model.onEvent(CredentialEvent.OnDiscardConfirm)
            assertEquals(CredentialEffect.NavigateBack, awaitItem())
        }
        assertEquals("old first", stored { it.customFields.first().value.toStringUnsafe() })
    }

    @Test
    fun `opening an unchanged draft and reverting edits leave a clean editor`() = runTest(dispatcher) {
        val model = loadedModel()
        model.onEvent(CredentialEvent.OnCustomFieldEditStarted(firstId))
        val original = model.state.value.customFieldDrafts.getValue(firstId)
        assertFalse(model.state.value.hasUnsavedChanges)
        model.onEvent(CredentialEvent.OnCustomFieldDraftChanged(firstId, original.copy(value = "changed")))
        assertTrue(model.state.value.hasUnsavedChanges)
        model.onEvent(CredentialEvent.OnCustomFieldDraftChanged(firstId, original))
        assertFalse(model.state.value.hasUnsavedChanges)
        model.effect.test {
            model.onEvent(CredentialEvent.OnBackClick)
            assertEquals(CredentialEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `row Cancel drops only its draft and preserves other unsaved work`() = runTest(dispatcher) {
        val model = loadedModel()
        change(model, firstId, CredentialCustomFieldDraft("First", "cancel this", true))
        change(model, secondId, CredentialCustomFieldDraft("Second", "keep this", false))
        model.onEvent(CredentialEvent.OnCustomFieldEditCancelled(firstId))
        assertEquals(setOf(secondId), model.state.value.customFieldDrafts.keys)
        assertEquals("old first", model.state.value.customFields.first().value.toStringUnsafe())
        assertTrue(model.state.value.hasUnsavedChanges)
        model.onEvent(CredentialEvent.OnTitleChanged("Other edit"))
        model.onEvent(CredentialEvent.OnCustomFieldEditCancelled(secondId))
        assertTrue(model.state.value.hasUnsavedChanges)
        assertEquals("Other edit", model.state.value.title)
    }

    @Test
    fun `blank draft blocks whole page Save without partially adopting other drafts`() = runTest(dispatcher) {
        val model = loadedModel()
        change(model, firstId, CredentialCustomFieldDraft("Valid", "new first", true))
        change(model, secondId, CredentialCustomFieldDraft(" ", "new second", true))
        model.effect.test {
            model.onEvent(CredentialEvent.OnSaveClick)
            runCurrent()
            expectNoEvents()
        }
        assertFalse(model.state.value.isSaving)
        assertEquals(2, model.state.value.customFieldDrafts.size)
        assertEquals("old first", model.state.value.customFields.first().value.toStringUnsafe())
        assertEquals(
            Res.string.validation_credential_custom_field_name,
            assertIs<UiText.Resource>(model.state.value.errorMessage).resource,
        )
        change(model, secondId, CredentialCustomFieldDraft("Now valid", "new second", true))
        model.onEvent(CredentialEvent.OnSaveClick)
        runCurrent()
        assertEquals("new first", stored { it.customFields.first().value.toStringUnsafe() })
    }

    @Test
    fun `failed page Save keeps adopted values dirty and retry persists them`() = runTest(dispatcher) {
        val model = loadedModel()
        change(model, firstId, CredentialCustomFieldDraft("Retry", "must survive", true))
        repository.setShouldFail()
        model.onEvent(CredentialEvent.OnSaveClick)
        runCurrent()
        assertTrue(model.state.value.hasUnsavedChanges)
        assertEquals("must survive", model.state.value.customFields.first().value.toStringUnsafe())
        assertEquals("old first", stored { it.customFields.first().value.toStringUnsafe() })
        repository.setShouldFail(null)
        model.onEvent(CredentialEvent.OnSaveClick)
        runCurrent()
        assertEquals("must survive", stored { it.customFields.first().value.toStringUnsafe() })
        assertFalse(model.state.value.hasUnsavedChanges)
    }

    @Test
    fun `row Save and removal clear their owned draft without losing another draft`() = runTest(dispatcher) {
        val model = loadedModel()
        val oldValue = model.state.value.customFields.first().value
        change(model, firstId, CredentialCustomFieldDraft("First", "row saved", true))
        change(model, secondId, CredentialCustomFieldDraft("Second", "pending", true))
        model.onEvent(CredentialEvent.OnCustomFieldUpdated(firstId, "First", "row saved", true))
        assertEquals(setOf(secondId), model.state.value.customFieldDrafts.keys)
        assertTrue(oldValue.toStringUnsafe().all { it == '\u0000' })
        assertEquals("row saved", model.state.value.customFields.first().value.toStringUnsafe())
        model.onEvent(CredentialEvent.OnCustomFieldRemoved(secondId))
        assertTrue(model.state.value.customFieldDrafts.isEmpty())
        assertEquals(listOf(firstId), model.state.value.customFields.map { it.id })
        assertTrue(model.state.value.hasUnsavedChanges)
    }

    @Test
    fun `duplicate row Save preserves the live value and page Save persists it`() = runTest(dispatcher) {
        val model = loadedModel()
        change(model, firstId, CredentialCustomFieldDraft("First", "keep duplicate save", true))
        val save = CredentialEvent.OnCustomFieldUpdated(firstId, "First", "keep duplicate save", true)
        model.onEvent(save)
        val adoptedValue = model.state.value.customFields.first().value

        // A stale row callback may arrive before Compose removes the edit card.
        model.onEvent(save)
        assertSame(adoptedValue, model.state.value.customFields.first().value)
        assertEquals("keep duplicate save", adoptedValue.toStringUnsafe())
        assertTrue(model.state.value.customFieldDrafts.isEmpty())
        model.onEvent(CredentialEvent.OnSaveClick)
        runCurrent()
        assertEquals("keep duplicate save", stored { it.customFields.first().value.toStringUnsafe() })
        assertFalse(model.state.value.hasUnsavedChanges)
    }

    @Test
    fun `unchanged row Save in a dirty editor never clears the live owner`() = runTest(dispatcher) {
        val model = loadedModel()
        model.onEvent(CredentialEvent.OnTitleChanged("Other dirty edit"))
        val originalValue = model.state.value.customFields.first().value

        model.onEvent(CredentialEvent.OnCustomFieldUpdated(firstId, "First", "old first", false))
        assertSame(originalValue, model.state.value.customFields.first().value)
        assertEquals("old first", originalValue.toStringUnsafe())
        assertTrue(model.state.value.hasUnsavedChanges)
        assertEquals("Other dirty edit", model.state.value.title)
    }

    @Test
    fun `unchanged row Save closes its draft without replacing or wiping the live value`() = runTest(dispatcher) {
        val model = loadedModel()
        val originalValue = model.state.value.customFields.first().value
        model.onEvent(CredentialEvent.OnCustomFieldEditStarted(firstId))
        assertFalse(model.state.value.hasUnsavedChanges)

        model.onEvent(CredentialEvent.OnCustomFieldUpdated(firstId, "First", "old first", false))
        assertSame(originalValue, model.state.value.customFields.first().value)
        assertEquals("old first", originalValue.toStringUnsafe())
        assertTrue(model.state.value.customFieldDrafts.isEmpty())
    }

    @Test
    fun `busy save rejects repeat saves and late inline edits`() = runTest(dispatcher) {
        val model = loadedModel()
        change(model, firstId, CredentialCustomFieldDraft("First", "save snapshot", true))
        repository.setOperationDelay(1_000)
        model.onEvent(CredentialEvent.OnSaveClick)
        runCurrent()
        assertTrue(model.state.value.isSaving)
        change(model, firstId, CredentialCustomFieldDraft("First", "too late", true))
        model.onEvent(CredentialEvent.OnSaveClick)
        model.onEvent(CredentialEvent.OnCustomFieldEditCancelled(firstId))
        assertTrue(model.state.value.customFieldDrafts.isEmpty())
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals("save snapshot", stored { it.customFields.first().value.toStringUnsafe() })
    }

    @Test
    fun `lock drops draft references and late draft events cannot resurrect them`() = runTest(dispatcher) {
        val model = loadedModel()
        val draft = CredentialCustomFieldDraft("First", "synthetic private draft", true)
        change(model, firstId, draft)
        model.clearForLock()
        change(model, firstId, draft)
        model.onEvent(CredentialEvent.OnCustomFieldUpdated(firstId, draft.name, draft.value, draft.isSecret))
        assertTrue(model.state.value.customFields.isEmpty())
        assertTrue(model.state.value.customFieldDrafts.isEmpty())
        assertFalse(model.state.value.canSave)
        assertFalse(model.state.value.hasUnsavedChanges)
    }

    @Test
    fun `draft inputs retain Unicode bounds and unknown identities cannot create fields`() = runTest(dispatcher) {
        val model = loadedModel()
        val draft = CredentialCustomFieldDraft("🔐".repeat(201), "🔐".repeat(20_001), true)
        change(model, firstId, draft)
        val bounded = model.state.value.customFieldDrafts.getValue(firstId)
        assertEquals(200, bounded.name.codePointLength())
        assertEquals(20_000, bounded.value.codePointLength())
        change(model, CustomFieldId("unknown"), draft)
        assertEquals(setOf(firstId), model.state.value.customFieldDrafts.keys)
        assertFalse(draft.toString().contains("🔐"))
        assertEquals(0, draft.hashCode())
    }

    private fun loadedModel(): CredentialViewModel {
        val credential = TestData.credential(id = "draft-owner").copy(
            customFields = listOf(
                CustomField(firstId, "First", SensitiveText.from("old first"), false),
                CustomField(secondId, "Second", SensitiveText.from("old second"), true),
            ),
        )
        repository.setupCredentials(credential)
        credential.clearSensitiveValues()
        return CredentialViewModel(
            repository,
            FakeFolderRepository(),
            SecurePasswordGenerator(FakeCryptoEngine()),
        ).also { model ->
            owners += model
            model.loadCredential(credential.id)
            dispatcher.scheduler.runCurrent()
            assertTrue(model.state.value.isCredentialLoaded)
        }
    }

    private fun <T> stored(block: (Credential) -> T): T {
        val copies = repository.getAllCredentials()
        return try {
            block(copies.single())
        } finally {
            copies.forEach(Credential::clearSensitiveValues)
        }
    }

    private fun change(model: CredentialViewModel, id: CustomFieldId, draft: CredentialCustomFieldDraft) {
        model.onEvent(CredentialEvent.OnCustomFieldEditStarted(id))
        model.onEvent(CredentialEvent.OnCustomFieldDraftChanged(id, draft))
    }
}
