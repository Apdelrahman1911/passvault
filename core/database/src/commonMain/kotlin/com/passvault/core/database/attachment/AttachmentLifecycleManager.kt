package com.passvault.core.database.attachment

/** Coordinates filesystem cleanup with a credential's cascading Room delete. */
interface AttachmentLifecycleManager {
    suspend fun <T> withStableAttachments(block: suspend () -> T): T

    suspend fun deleteCredentialAndAttachments(
        credentialId: String,
        deleteCredential: suspend () -> Unit,
    )
}

object DatabaseOnlyAttachmentLifecycleManager : AttachmentLifecycleManager {
    override suspend fun <T> withStableAttachments(block: suspend () -> T): T = block()

    override suspend fun deleteCredentialAndAttachments(
        credentialId: String,
        deleteCredential: suspend () -> Unit,
    ) = deleteCredential()
}
