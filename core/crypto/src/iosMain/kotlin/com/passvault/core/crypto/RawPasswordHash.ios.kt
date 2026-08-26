@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.core.crypto

import com.ionspin.kotlin.crypto.pwhash.PasswordHashingFailed
import com.passvault.core.crypto.rawsodium.passvault_crypto_pwhash
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned

internal actual fun rawPasswordHash(
    outputLength: Int,
    password: ByteArray,
    salt: ByteArray,
    opsLimit: Int,
    memLimit: Int,
    algorithm: Int,
): ByteArray {
    require(outputLength > 0) { "Password-hash output must not be empty" }
    require(password.isNotEmpty()) { "Password must not be empty" }
    require(salt.isNotEmpty()) { "Salt must not be empty" }

    val derived = ByteArray(outputLength)
    var completed = false
    try {
        val status = derived.usePinned { outputPinned ->
            password.usePinned { passwordPinned ->
                salt.usePinned { saltPinned ->
                    passvault_crypto_pwhash(
                        out = outputPinned.addressOf(0).reinterpret(),
                        outlen = outputLength.toULong(),
                        password = passwordPinned.addressOf(0).reinterpret(),
                        password_length = password.size.toULong(),
                        salt = saltPinned.addressOf(0).reinterpret(),
                        opslimit = opsLimit.toULong(),
                        memlimit = memLimit.toULong(),
                        algorithm = algorithm,
                    )
                }
            }
        }
        if (status != LIBSODIUM_SUCCESS) throw PasswordHashingFailed()
        completed = true
        return derived
    } finally {
        if (!completed) derived.fill(0)
    }
}

private const val LIBSODIUM_SUCCESS = 0
