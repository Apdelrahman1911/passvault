package com.passvault.core.database.attachment

/** Coordinates filesystem cleanup with a credential's cascading Room delete. */
interface AttachmentLifecycleManager {
    /**
     * Runs [block] while attachment mutation is serialised and interrupted work is recovered.
     *
     * The block must not call [com.passvault.core.domain.repository.AttachmentRepository] or a
     * credential delete that reaches this lifecycle manager. Those operations acquire the same
     * non-reentrant lock and fail fast rather than waiting indefinitely.
     */
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
