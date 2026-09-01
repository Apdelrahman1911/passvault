package com.passvault.core.otp

import com.passvault.core.domain.model.TotpAlgorithm
import java.security.MessageDigest

internal actual fun platformTotpDigest(
    algorithm: TotpAlgorithm,
    first: ByteArray,
    second: ByteArray,
): ByteArray {
    val digest = MessageDigest.getInstance(algorithm.digestName())
    return try {
        digest.update(first)
        digest.digest(second)
    } finally {
        // Provider-internal buffers are outside application control. Resetting
        // here complements clearing every mutable buffer owned by PassVault.
        digest.reset()
    }
}

private fun TotpAlgorithm.digestName(): String = when (this) {
    TotpAlgorithm.SHA1 -> "SHA-1"
    TotpAlgorithm.SHA256 -> "SHA-256"
    TotpAlgorithm.SHA512 -> "SHA-512"
}
