package com.passvault.core.domain.model

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import kotlin.test.Test

/**
 * Unit tests for strongly-typed ID value classes.
 */
class TypedIdTest {

    @Test
    fun `credential id holds value`() {
        val id = CredentialId("cred-123")
        assertThat(id.value).isEqualTo("cred-123")
    }

    @Test
    fun `folder id holds value`() {
        val id = FolderId("folder-456")
        assertThat(id.value).isEqualTo("folder-456")
    }

    @Test
    fun `tag id holds value`() {
        val id = TagId("tag-789")
        assertThat(id.value).isEqualTo("tag-789")
    }

    @Test
    fun `attachment id holds value`() {
        val id = AttachmentId("att-000")
        assertThat(id.value).isEqualTo("att-000")
    }

    @Test
    fun `custom field id holds value`() {
        val id = CustomFieldId("cf-111")
        assertThat(id.value).isEqualTo("cf-111")
    }

    @Test
    fun `vault id holds value`() {
        val id = VaultId("vault-222")
        assertThat(id.value).isEqualTo("vault-222")
    }

    @Test
    fun `session id holds value`() {
        val id = SessionId("sess-333")
        assertThat(id.value).isEqualTo("sess-333")
    }

    @Test
    fun `typed ids are equal by value`() {
        val id1 = CredentialId("same")
        val id2 = CredentialId("same")

        assertThat(id1).isEqualTo(id2)
    }

    @Test
    fun `typed ids are not equal when values differ`() {
        val id1 = CredentialId("one")
        val id2 = CredentialId("two")

        assertThat(id1).isNotEqualTo(id2)
    }

    @Test
    fun `typed ids are not equal to strings`() {
        val id = CredentialId("test")

        assertThat(id).isNotEqualTo("test")
        assertThat(id).isNotEqualTo(Any())
    }

    @Test
    fun `typed ids have consistent hashCode`() {
        val id1 = CredentialId("test")
        val id2 = CredentialId("test")

        assertThat(id1.hashCode()).isEqualTo(id2.hashCode())
    }

    @Test
    fun `different typed ids have different hashCodes usually`() {
        val id1 = CredentialId("one")
        val id2 = CredentialId("two")

        // Not guaranteed, but very likely
        assertThat(id1.hashCode()).isNotEqualTo(id2.hashCode())
    }

    @Test
    fun `credential ids implement TypedId`() {
        val id: TypedId = CredentialId("test")
        assertThat(id).isInstanceOf(CredentialId::class)
    }

    @Test
    fun `folder ids implement TypedId`() {
        val id: TypedId = FolderId("test")
        assertThat(id).isInstanceOf(FolderId::class)
    }

    @Test
    fun `typed ids are value classes`() {
        // Value classes should have value semantics
        val id = CredentialId("test")
        val copy = CredentialId("test")

        assertThat(id).isEqualTo(copy)
        assertThat(id.value).isEqualTo(copy.value)
    }

    @Test
    fun `empty string is valid id`() {
        val id = CredentialId("")
        assertThat(id.value).isEqualTo("")
    }

    @Test
    fun `uuid format is valid id`() {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        val id = CredentialId(uuid)
        assertThat(id.value).isEqualTo(uuid)
    }

    @Test
    fun `special characters are valid in id`() {
        val special = "id_with-special.chars"
        val id = CredentialId(special)
        assertThat(id.value).isEqualTo(special)
    }

    @Test
    fun `unicode is valid in id`() {
        val unicode = "日本語-id"
        val id = CredentialId(unicode)
        assertThat(id.value).isEqualTo(unicode)
    }

    @Test
    fun `toString returns the value`() {
        val id = CredentialId("test")
        assertThat(id.toString()).isEqualTo("CredentialId(value=test)")
    }

    @Test
    fun `ids can be used as map keys`() {
        val map = mutableMapOf<CredentialId, String>()
        map[CredentialId("1")] = "first"
        map[CredentialId("2")] = "second"

        assertThat(map[CredentialId("1")]).isEqualTo("first")
        assertThat(map[CredentialId("2")]).isEqualTo("second")
    }

    @Test
    fun `ids can be used in sets`() {
        val set = mutableSetOf<CredentialId>()
        set.add(CredentialId("1"))
        set.add(CredentialId("2"))
        set.add(CredentialId("1")) // Duplicate

        assertThat(set).hasSize(2)
    }

    @Test
    fun `different id types are not equal`() {
        val credId = CredentialId("same-value")
        val folderId = FolderId("same-value")

        assertThat(credId).isNotEqualTo(folderId)
    }

    @Test
    fun `ids can be sorted`() {
        val ids = listOf(
            CredentialId("c"),
            CredentialId("a"),
            CredentialId("b")
        )
        val sorted = ids.sortedBy { it.value }

        assertThat(sorted[0].value).isEqualTo("a")
        assertThat(sorted[1].value).isEqualTo("b")
        assertThat(sorted[2].value).isEqualTo("c")
    }
}
