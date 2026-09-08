package com.passvault.feature.credential.presentation

import com.passvault.core.domain.model.CustomField

/**
 * Entry-owned, non-saveable editor input. Like the other text-field inputs this
 * contains immutable UI strings, not memory that can be reliably overwritten.
 * Dropping a draft on cancel/lock releases the owner's reference, not OS copies.
 */
data class CredentialCustomFieldDraft(
    val name: String,
    val value: String,
    val isSecret: Boolean,
) {
    internal fun matches(field: CustomField): Boolean =
        name == field.name && isSecret == field.isSecret && field.value.withExposed { characters ->
            characters.size == value.length && characters.indices.all { characters[it] == value[it] }
        }

    override fun toString(): String = "CredentialCustomFieldDraft([REDACTED])"

    override fun hashCode(): Int = 0
}
