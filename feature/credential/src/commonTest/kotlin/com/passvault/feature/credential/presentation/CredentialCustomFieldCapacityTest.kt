package com.passvault.feature.credential.presentation

import com.passvault.core.crypto.SecurePasswordGenerator
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.core.testing.fakes.FakeCryptoEngine
import com.passvault.core.testing.fakes.FakeFolderRepository
import com.passvault.feature.credential.presentation.CredentialViewModel.CredentialEvent
import com.passvault.feature.credential.presentation.CredentialViewModel.CredentialState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Real admission/event/save state with synthetic repositories; not a rendered dialog test. */
@OptIn(ExperimentalCoroutinesApi::class)
class CredentialCustomFieldCapacityTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeCredentialRepository()
    private val owners = mutableListOf<CredentialViewModel>()

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
    fun `new and loaded states stop offering Add at the canonical limit but retain page Save`() {
        val fields = fields(MAX_CUSTOM_FIELDS + 1)
        try {
            for (loaded in listOf(false, true)) {
                val state = CredentialState(isNewCredential = !loaded, isCredentialLoaded = loaded)
                assertTrue(state.canAddCustomField)
                assertTrue(state.copy(customFields = fields.take(MAX_CUSTOM_FIELDS - 1)).canAddCustomField)
                val full = state.copy(customFields = fields.take(MAX_CUSTOM_FIELDS))
                assertFalse(full.canAddCustomField)
                assertTrue(full.canSave)
                assertFalse(state.copy(customFields = fields).canAddCustomField)
            }
        } finally {
            fields.forEach { it.value.clear() }
        }
    }

    @Test
    fun `Add admission preserves every busy and unloaded restriction`() {
        assertFalse(CredentialState().canAddCustomField)
        for (loaded in listOf(false, true)) {
            val ready = CredentialState(isNewCredential = !loaded, isCredentialLoaded = loaded)
            assertTrue(ready.canAddCustomField)
            val blocked = listOf(
                ready.copy(isLoading = true),
                ready.copy(isSaving = true),
                ready.copy(isDeleting = true),
                ready.copy(isAttachmentBusy = true),
                ready.copy(isGeneratingPassword = true),
            )
            blocked.forEach { state ->
                assertFalse(state.canSave)
                assertFalse(state.canAddCustomField)
            }
        }
    }

    @Test
    fun `real Add accepts the final slot and keeps every previous field intact`() = runTest(dispatcher) {
        val model = newModel()
        repeat(MAX_CUSTOM_FIELDS - 1) { add(model, it) }
        val previous = model.state.value.customFields.toList()
        assertTrue(model.state.value.canAddCustomField)

        add(model, MAX_CUSTOM_FIELDS - 1)

        val full = model.state.value
        assertEquals(MAX_CUSTOM_FIELDS, full.customFields.size)
        assertFalse(full.canAddCustomField)
        assertTrue(full.canSave)
        previous.forEachIndexed { index, field ->
            assertSame(field.value, full.customFields[index].value)
            assertEquals("value-$index", field.value.toStringUnsafe())
        }
        assertEquals("value-${MAX_CUSTOM_FIELDS - 1}", full.customFields.last().value.toStringUnsafe())
        assertEquals(MAX_CUSTOM_FIELDS, full.customFields.map { it.id }.toSet().size)
    }

    @Test
    fun `backend capacity rejection still preserves all accepted fields`() = runTest(dispatcher) {
        val model = newModel()
        repeat(MAX_CUSTOM_FIELDS) { add(model, it) }
        val previous = model.state.value.customFields.toList()

        // Defense in depth: this direct event is no longer offered by the full-capacity UI.
        add(model, MAX_CUSTOM_FIELDS)

        assertEquals(previous.map { it.id }, model.state.value.customFields.map { it.id })
        previous.forEachIndexed { index, field ->
            assertSame(field.value, model.state.value.customFields[index].value)
            assertEquals("value-$index", field.value.toStringUnsafe())
        }
        assertFalse(model.state.value.canAddCustomField)
        assertTrue(model.state.value.canSave)
        assertTrue(model.state.value.errorMessage != null)
    }

    @Test
    fun `removing one field reopens one slot without changing surviving fields`() = runTest(dispatcher) {
        val model = newModel()
        repeat(MAX_CUSTOM_FIELDS) { add(model, it) }
        val previous = model.state.value.customFields.toList()
        val removed = previous.first()

        model.onEvent(CredentialEvent.OnCustomFieldRemoved(removed.id))

        assertTrue(model.state.value.canAddCustomField)
        assertTrue(removed.value.toStringUnsafe().all { it == '\u0000' })
        previous.drop(1).forEachIndexed { index, field ->
            assertSame(field.value, model.state.value.customFields[index].value)
            assertEquals("value-${index + 1}", field.value.toStringUnsafe())
        }
        add(model, MAX_CUSTOM_FIELDS)
        assertEquals(MAX_CUSTOM_FIELDS, model.state.value.customFields.size)
        assertFalse(model.state.value.canAddCustomField)
        assertEquals(previous.drop(1).map { it.id }, model.state.value.customFields.dropLast(1).map { it.id })
    }

    @Test
    fun `loading a full credential disables only Add and keeps every stored field`() = runTest(dispatcher) {
        val credential = TestData.credential(id = "capacity-existing").copy(customFields = fields(MAX_CUSTOM_FIELDS))
        try {
            repository.setupCredentials(credential)
        } finally {
            credential.clearSensitiveValues()
        }
        val model = model()
        model.loadCredential(credential.id)
        runCurrent()

        assertTrue(model.state.value.isCredentialLoaded)
        assertFalse(model.state.value.canAddCustomField)
        assertTrue(model.state.value.canSave)
        assertEquals((0 until MAX_CUSTOM_FIELDS).map { "value-$it" },
            model.state.value.customFields.map { it.value.toStringUnsafe() })
    }

    @Test
    fun `page Save at capacity still persists the accepted names and values`() = runTest(dispatcher) {
        val model = newModel()
        repeat(MAX_CUSTOM_FIELDS) { add(model, it) }
        model.onEvent(CredentialEvent.OnTitleChanged("Synthetic capacity record"))
        val ids = model.state.value.customFields.map { it.id }
        assertTrue(model.state.value.canSave)

        model.onEvent(CredentialEvent.OnSaveClick)
        assertFalse(model.state.value.canAddCustomField)
        runCurrent()

        assertFalse(model.state.value.isSaving)
        assertFalse(model.state.value.hasUnsavedChanges)
        val copies = repository.getAllCredentials()
        try {
            val saved = copies.single()
            assertEquals(ids, saved.customFields.map { it.id })
            assertEquals((0 until MAX_CUSTOM_FIELDS).map { "Field $it" }, saved.customFields.map { it.name })
            assertEquals((0 until MAX_CUSTOM_FIELDS).map { "value-$it" },
                saved.customFields.map { it.value.toStringUnsafe() })
        } finally {
            copies.forEach(Credential::clearSensitiveValues)
        }
    }

    private fun model(): CredentialViewModel = CredentialViewModel(
        repository,
        FakeFolderRepository(),
        SecurePasswordGenerator(FakeCryptoEngine()),
    ).also { owners += it }

    private fun newModel(): CredentialViewModel = model().also { it.createNewCredential(CredentialType.Login) }

    private fun add(model: CredentialViewModel, index: Int) {
        model.onEvent(CredentialEvent.OnCustomFieldAdded("Field $index", "value-$index", index % 2 == 0))
    }

    private fun fields(count: Int): List<CustomField> = List(count) { index ->
        CustomField(CustomFieldId("capacity-$index"), "Field $index", SensitiveText.from("value-$index"), index % 2 == 0)
    }
}
