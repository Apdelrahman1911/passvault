@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.core.otp

import com.passvault.core.domain.model.TotpAlgorithm
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA1_CTX
import platform.CoreCrypto.CC_SHA1_Final
import platform.CoreCrypto.CC_SHA1_Init
import platform.CoreCrypto.CC_SHA1_Update
import platform.CoreCrypto.CC_SHA256_CTX
import platform.CoreCrypto.CC_SHA256_Final
import platform.CoreCrypto.CC_SHA256_Init
import platform.CoreCrypto.CC_SHA256_Update
import platform.CoreCrypto.CC_SHA512_CTX
import platform.CoreCrypto.CC_SHA512_Final
import platform.CoreCrypto.CC_SHA512_Init
import platform.CoreCrypto.CC_SHA512_Update
import platform.posix.memset_s

internal actual fun platformTotpDigest(
    algorithm: TotpAlgorithm,
    first: ByteArray,
    second: ByteArray,
): ByteArray = when (algorithm) {
    TotpAlgorithm.SHA1 -> sha1(first, second)
    TotpAlgorithm.SHA256 -> sha256(first, second)
    TotpAlgorithm.SHA512 -> sha512(first, second)
}

private fun sha1(first: ByteArray, second: ByteArray): ByteArray {
    val output = ByteArray(SHA1_BYTES)
    var completed = false
    try {
        memScoped {
            val context = alloc<CC_SHA1_CTX>()
            try {
                check(CC_SHA1_Init(context.ptr) == DIGEST_SUCCESS)
                first.updateIfNotEmpty { pointer, size -> CC_SHA1_Update(context.ptr, pointer, size) }
                second.updateIfNotEmpty { pointer, size -> CC_SHA1_Update(context.ptr, pointer, size) }
                output.usePinned { pinned ->
                    check(CC_SHA1_Final(pinned.addressOf(0).reinterpret<UByteVar>(), context.ptr) == DIGEST_SUCCESS)
                }
            } finally {
                val contextSize = sizeOf<CC_SHA1_CTX>().toULong()
                memset_s(context.ptr, contextSize, 0, contextSize)
            }
        }
        completed = true
        return output
    } finally {
        if (!completed) output.fill(0)
    }
}

private fun sha256(first: ByteArray, second: ByteArray): ByteArray {
    val output = ByteArray(SHA256_BYTES)
    var completed = false
    try {
        memScoped {
            val context = alloc<CC_SHA256_CTX>()
            try {
                check(CC_SHA256_Init(context.ptr) == DIGEST_SUCCESS)
                first.updateIfNotEmpty { pointer, size -> CC_SHA256_Update(context.ptr, pointer, size) }
                second.updateIfNotEmpty { pointer, size -> CC_SHA256_Update(context.ptr, pointer, size) }
                output.usePinned { pinned ->
                    check(CC_SHA256_Final(pinned.addressOf(0).reinterpret<UByteVar>(), context.ptr) == DIGEST_SUCCESS)
                }
            } finally {
                val contextSize = sizeOf<CC_SHA256_CTX>().toULong()
                memset_s(context.ptr, contextSize, 0, contextSize)
            }
        }
        completed = true
        return output
    } finally {
        if (!completed) output.fill(0)
    }
}

private fun sha512(first: ByteArray, second: ByteArray): ByteArray {
    val output = ByteArray(SHA512_BYTES)
    var completed = false
    try {
        memScoped {
            val context = alloc<CC_SHA512_CTX>()
            try {
                check(CC_SHA512_Init(context.ptr) == DIGEST_SUCCESS)
                first.updateIfNotEmpty { pointer, size -> CC_SHA512_Update(context.ptr, pointer, size) }
                second.updateIfNotEmpty { pointer, size -> CC_SHA512_Update(context.ptr, pointer, size) }
                output.usePinned { pinned ->
                    check(CC_SHA512_Final(pinned.addressOf(0).reinterpret<UByteVar>(), context.ptr) == DIGEST_SUCCESS)
                }
            } finally {
                val contextSize = sizeOf<CC_SHA512_CTX>().toULong()
                memset_s(context.ptr, contextSize, 0, contextSize)
            }
        }
        completed = true
        return output
    } finally {
        if (!completed) output.fill(0)
    }
}

private inline fun ByteArray.updateIfNotEmpty(update: (kotlinx.cinterop.CValuesRef<*>?, UInt) -> Int) {
    if (isNotEmpty()) {
        usePinned { pinned -> check(update(pinned.addressOf(0), size.toUInt()) == DIGEST_SUCCESS) }
    }
}

private const val DIGEST_SUCCESS = 1
private const val SHA1_BYTES = 20
private const val SHA256_BYTES = 32
private const val SHA512_BYTES = 64
