package com.passvault.core.domain.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TotpConfiguration(
    val secret: SensitiveText,
    val issuer: String? = null,
    val accountName: String? = null,
    val algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    val digits: Int = 6,
    val periodSeconds: Int = 30,
) {
    fun deepCopy(): TotpConfiguration {
        val secretCopy = secret.expose()
        return try {
            copy(secret = SensitiveText.from(secretCopy))
        } finally {
            secretCopy.fill('\u0000')
        }
    }

    fun clear() {
        secret.clear()
    }
}

@Serializable
enum class TotpAlgorithm {
    SHA1,
    SHA256,
    SHA512,
}

data class TotpCode(
    val value: String,
    val expiresAt: Instant,
)
