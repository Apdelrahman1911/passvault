package com.passvault.core.database.attachment

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.PaddedPayload
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.repository.requireRecordIdentifier
import com.passvault.core.domain.repository.AttachmentInvalidFileNameException
import com.passvault.core.domain.repository.AttachmentPolicy
import kotlinx.coroutines.CancellationException

/** A filename read is isolated from sibling rows without losing the row's stable identity. */
internal sealed interface AttachmentFilenameRead {
    val collisionName: String

    data class Readable(
        val value: String,
        val requiresRename: Boolean,
    ) : AttachmentFilenameRead {
        override val collisionName: String = value
    }

    data class Corrupted(
        val attachmentId: String,
    ) : AttachmentFilenameRead {
        override val collisionName: String = "unreadable-attachment-$attachmentId"
    }
}

/**
 * Authenticates one filename while keeping corruption local to that row.
 *
 * Structural identity/state checks remain fail-closed. Only failures in the independently encrypted filename are
 * converted to [AttachmentFilenameRead.Corrupted], so callers can expose a repair/delete path for the stable row.
 */
internal suspend fun AttachmentRecordEntity.readAttachmentFilename(
    expectedCredentialId: String,
    vek: ByteArray,
    cryptoEngine: CryptoEngine,
): AttachmentFilenameRead {
    id.requireRecordIdentifier("Attachment ID")
    credentialId.requireRecordIdentifier("Credential ID")
    require(credentialId == expectedCredentialId)
    keyDerivationContext.requireRecordIdentifier("Attachment key context")
    requireStableStorageKind()

    var key: ByteArray? = null
    var plaintext: ByteArray? = null
    val associatedData = attachmentFilenameAssociatedData(id, credentialId)
    return try {
        key = cryptoEngine.deriveSubkey(vek, "attachment:$keyDerivationContext", KEY_BYTES).getOrThrow()
        plaintext = PaddedPayload.decrypt(
            cryptoEngine = cryptoEngine,
            storedCiphertext = encryptedFilename,
            nonce = filenameNonce,
            key = key,
            associatedData = associatedData,
            maxPlaintextBytes = MAX_FILENAME_UTF8_BYTES,
        ).getOrThrow()
        val fileName = AttachmentPolicy.validateStoredFileName(
            plaintext.decodeToString(throwOnInvalidSequence = true),
        )
        AttachmentFilenameRead.Readable(
            value = fileName,
            requiresRename = fileName.requiresCurrentPolicyRename(),
        )
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (_: Exception) {
        AttachmentFilenameRead.Corrupted(id)
    } finally {
        plaintext?.let(cryptoEngine::secureWipe)
        key?.let(cryptoEngine::secureWipe)
        cryptoEngine.secureWipe(associatedData)
    }
}

internal fun attachmentFilenameAssociatedData(attachmentId: String, credentialId: String): ByteArray =
    "passvault:attachment:$attachmentId:$credentialId:filename:v1".encodeToByteArray()

private fun String.requiresCurrentPolicyRename(): Boolean = try {
    AttachmentPolicy.validateFileName(this) != this
} catch (_: AttachmentInvalidFileNameException) {
    true
}

private const val KEY_BYTES = 32
private const val MAX_FILENAME_UTF8_BYTES = AttachmentPolicy.MAX_FILE_NAME_CODE_POINTS * 4
