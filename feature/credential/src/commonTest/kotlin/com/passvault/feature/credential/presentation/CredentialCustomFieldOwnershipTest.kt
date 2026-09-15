package com.passvault.feature.credential.presentation

import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.SensitiveText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** PVA-026 backport without the unfinished inline-draft editor changes. */
class CredentialCustomFieldOwnershipTest {
    @Test
    fun equalUpdateRetainsTheLiveOwnerInCleanAndDirtyStates() {
        for (dirty in listOf(false, true)) {
            val original = SensitiveText.from("synthetic value")
            val field = CustomField(CustomFieldId("field"), "Name", original, true)
            val state = MutableStateFlow(CredentialViewModel.CredentialState(
                customFields = listOf(field), isDirty = dirty,
            ))
            try {
                CredentialCustomFieldEditor(state).update(field.id, "Name", "synthetic value", true)
                assertSame(original, state.value.customFields.single().value)
                assertEquals("synthetic value", original.toStringUnsafe())
                assertTrue(state.value.isDirty)
            } finally {
                state.value.customFields.forEach { it.value.clear() }
                original.clear()
            }
        }
    }

    @Test
    fun changedUpdateTransfersNewOwnerAndClearsTheOldValue() {
        val original = SensitiveText.from("old synthetic value")
        val field = CustomField(CustomFieldId("field"), "Name", original, true)
        val state = MutableStateFlow(CredentialViewModel.CredentialState(
            customFields = listOf(field), isDirty = true,
        ))
        try {
            CredentialCustomFieldEditor(state).update(field.id, "Name", "new synthetic value", true)
            assertEquals("new synthetic value", state.value.customFields.single().value.toStringUnsafe())
            assertTrue(original.toStringUnsafe().all { it == '\u0000' })
        } finally {
            state.value.customFields.forEach { it.value.clear() }
            original.clear()
        }
    }
}
