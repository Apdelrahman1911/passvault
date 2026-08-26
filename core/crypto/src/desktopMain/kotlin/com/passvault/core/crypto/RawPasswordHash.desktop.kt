package com.passvault.core.crypto

import com.ionspin.kotlin.crypto.LibsodiumInitializer
import com.ionspin.kotlin.crypto.pwhash.PasswordHashingFailed
import com.sun.jna.Function
import com.sun.jna.Library
import com.sun.jna.Memory
import java.lang.reflect.Proxy

internal actual fun rawPasswordHash(
    outputLength: Int,
    password: ByteArray,
    salt: ByteArray,
    opsLimit: Int,
    memLimit: Int,
    algorithm: Int,
): ByteArray = invokeRawPasswordHash(
    outputLength = outputLength,
    password = password,
    salt = salt,
    opsLimit = opsLimit,
    memLimit = memLimit,
    algorithm = algorithm,
)

private fun invokeRawPasswordHash(
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
        withClearedNativeMemory(outputLength) { outputMemory ->
            withClearedNativeMemory(password.size) { passwordMemory ->
                withClearedNativeMemory(salt.size) { saltMemory ->
                    passwordMemory.write(0, password, 0, password.size)
                    saltMemory.write(0, salt, 0, salt.size)
                    val status = passwordHashFunction().invokeInt(
                        arrayOf(
                            outputMemory,
                            outputLength.toLong(),
                            passwordMemory,
                            password.size.toLong(),
                            saltMemory,
                            opsLimit.toLong(),
                            memLimit.toLong(),
                            algorithm,
                        ),
                    )
                    if (status != LIBSODIUM_SUCCESS) throw PasswordHashingFailed()
                    outputMemory.read(0, derived, 0, derived.size)
                }
            }
        }
        completed = true
        return derived
    } finally {
        if (!completed) derived.fill(0)
    }
}

private fun passwordHashFunction(): Function {
    val handler = Proxy.getInvocationHandler(LibsodiumInitializer.sodiumJna)
    check(handler is Library.Handler) { "Unexpected libsodium JNA adapter" }
    return handler.nativeLibrary.getFunction("crypto_pwhash")
}

private inline fun <T> withClearedNativeMemory(size: Int, block: (Memory) -> T): T =
    Memory(size.toLong()).use { memory ->
        try {
            block(memory)
        } finally {
            memory.clear()
        }
    }

private const val LIBSODIUM_SUCCESS = 0
