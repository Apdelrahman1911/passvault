@file:OptIn(kotlinx.cinterop.BetaInteropApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.shared.platform

import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricType
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataGetTypeID
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFGetTypeID
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.NSError
import platform.Foundation.NSUserDefaults
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LABiometryTypeFaceID
import platform.LocalAuthentication.LABiometryTypeTouchID
import platform.LocalAuthentication.LAErrorAppCancel
import platform.LocalAuthentication.LAErrorBiometryNotEnrolled
import platform.LocalAuthentication.LAErrorSystemCancel
import platform.LocalAuthentication.LAErrorUserCancel
import platform.LocalAuthentication.LAErrorUserFallback
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.Security.SecAccessControlCreateWithFlags
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecAuthFailed
import platform.Security.errSecDecode
import platform.Security.errSecInteractionNotAllowed
import platform.Security.errSecItemNotFound
import platform.Security.errSecNotAvailable
import platform.Security.errSecSuccess
import platform.Security.errSecUserCanceled
import platform.Security.kSecAccessControlBiometryCurrentSet
import platform.Security.kSecAttrAccessControl
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecAttrSynchronizable
import platform.Security.kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecUseOperationPrompt
import platform.Security.kSecValueData
import kotlin.coroutines.resume

/**
 * Stores the VEK in a device-only Keychain item protected by the current Face
 * ID or Touch ID enrollment. The item is not synchronizable or transferable.
 */
class IosBiometricKeyStore : BiometricKeyStore {
    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    override suspend fun getCapability(): BiometricCapability {
        val context = LAContext()
        return memScoped {
            val error = alloc<kotlinx.cinterop.ObjCObjectVar<NSError?>>()
            error.value = null
            val available = context.canEvaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                error.ptr,
            )
            val type = when (context.biometryType) {
                LABiometryTypeFaceID -> BiometricType.FACE
                LABiometryTypeTouchID -> BiometricType.FINGERPRINT
                else -> BiometricType.GENERIC
            }
            val availability = when {
                available -> BiometricAvailability.AVAILABLE
                error.value?.code == LAErrorBiometryNotEnrolled -> BiometricAvailability.NOT_ENROLLED
                else -> BiometricAvailability.UNAVAILABLE
            }
            BiometricCapability(type, availability)
        }
    }

    override suspend fun contains(vaultId: String): Boolean = defaults.boolForKey(markerKey(vaultId))

    override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> {
        if (vaultKey.size != VAULT_KEY_BYTES) {
            return Result.failure(BiometricKeyStoreException.AuthenticationFailed())
        }
        val capability = getCapability()
        when (capability.availability) {
            BiometricAvailability.NOT_ENROLLED -> {
                return Result.failure(BiometricKeyStoreException.NotEnrolled())
            }
            BiometricAvailability.UNAVAILABLE -> {
                return Result.failure(BiometricKeyStoreException.NotAvailable())
            }
            BiometricAvailability.AVAILABLE -> Unit
        }

        authenticateForEnrollment().getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            deleteKeychainItem(vaultId)
            addKeychainItem(vaultId, vaultKey).also { result ->
                if (result.isSuccess) defaults.setBool(true, markerKey(vaultId))
            }
        }
    }

    override suspend fun retrieve(vaultId: String): Result<ByteArray> = withContext(Dispatchers.Default) {
        if (!defaults.boolForKey(markerKey(vaultId))) {
            return@withContext Result.failure(BiometricKeyStoreException.NotEnabled())
        }
        readKeychainItem(vaultId).onFailure { error ->
            if (error is BiometricKeyStoreException.Invalidated ||
                error is BiometricKeyStoreException.NotEnabled
            ) {
                defaults.removeObjectForKey(markerKey(vaultId))
                deleteKeychainItem(vaultId)
            }
        }
    }

    override suspend fun delete(vaultId: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            defaults.removeObjectForKey(markerKey(vaultId))
            deleteKeychainItem(vaultId)
            Result.success(Unit)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("Unable to remove biometric unlock"))
        }
    }

    private suspend fun authenticateForEnrollment(): Result<Unit> = withContext(Dispatchers.Main) {
        val context = LAContext()
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { context.invalidate() }
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = ENROLLMENT_REASON,
            ) { success, error ->
                if (continuation.isActive) {
                    continuation.resume(
                        if (success) Result.success(Unit) else Result.failure(error.toBiometricException()),
                    )
                }
            }
        }
    }

    private fun addKeychainItem(vaultId: String, vaultKey: ByteArray): Result<Unit> {
        val service = cfString(SERVICE_NAME) ?: return internalFailure()
        val account = cfString(vaultId) ?: run {
            CFRelease(service)
            return internalFailure()
        }
        val data = vaultKey.usePinned { pinned ->
            CFDataCreate(
                null,
                pinned.addressOf(0).reinterpret<UByteVar>(),
                vaultKey.size.toLong(),
            )
        } ?: run {
            CFRelease(account)
            CFRelease(service)
            return internalFailure()
        }
        val accessControl = SecAccessControlCreateWithFlags(
            allocator = null,
            protection = kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
            flags = kSecAccessControlBiometryCurrentSet,
            error = null,
        ) ?: run {
            CFRelease(data)
            CFRelease(account)
            CFRelease(service)
            return Result.failure(BiometricKeyStoreException.NotAvailable())
        }
        val query = baseQuery(service, account) ?: run {
            CFRelease(accessControl)
            CFRelease(data)
            CFRelease(account)
            CFRelease(service)
            return internalFailure()
        }

        CFDictionarySetValue(query, kSecValueData, data)
        CFDictionarySetValue(query, kSecAttrAccessControl, accessControl)
        CFDictionarySetValue(query, kSecAttrSynchronizable, kCFBooleanFalse)
        val status = SecItemAdd(query, null)

        CFRelease(query)
        CFRelease(accessControl)
        CFRelease(data)
        CFRelease(account)
        CFRelease(service)
        return if (status == errSecSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(status.toBiometricException())
        }
    }

    private fun readKeychainItem(vaultId: String): Result<ByteArray> {
        val service = cfString(SERVICE_NAME) ?: return internalFailure()
        val account = cfString(vaultId) ?: run {
            CFRelease(service)
            return internalFailure()
        }
        val prompt = cfString(UNLOCK_REASON) ?: run {
            CFRelease(account)
            CFRelease(service)
            return internalFailure()
        }
        val query = baseQuery(service, account) ?: run {
            CFRelease(prompt)
            CFRelease(account)
            CFRelease(service)
            return internalFailure()
        }
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
        CFDictionarySetValue(query, kSecUseOperationPrompt, prompt)

        val outcome = memScoped {
            val result = alloc<CFTypeRefVar>()
            result.value = null
            val status = SecItemCopyMatching(query, result.ptr)
            if (status != errSecSuccess) {
                Result.failure(status.toBiometricException())
            } else {
                val value = result.value
                    ?: return@memScoped internalFailure()
                try {
                    if (CFGetTypeID(value) != CFDataGetTypeID()) {
                        internalFailure()
                    } else {
                        val data = value.reinterpret<cnames.structs.__CFData>()
                        val length = CFDataGetLength(data).toInt()
                        val bytes = CFDataGetBytePtr(data)
                        if (length <= 0 || bytes == null) {
                            internalFailure()
                        } else {
                            Result.success(ByteArray(length) { index -> bytes[index].toByte() })
                        }
                    }
                } finally {
                    CFRelease(value)
                }
            }
        }

        CFRelease(query)
        CFRelease(prompt)
        CFRelease(account)
        CFRelease(service)
        return outcome
    }

    private fun deleteKeychainItem(vaultId: String) {
        val service = cfString(SERVICE_NAME) ?: return
        val account = cfString(vaultId) ?: run {
            CFRelease(service)
            return
        }
        val query = baseQuery(service, account)
        if (query != null) {
            SecItemDelete(query)
            CFRelease(query)
        }
        CFRelease(account)
        CFRelease(service)
    }

    private fun baseQuery(
        service: platform.CoreFoundation.CFStringRef,
        account: platform.CoreFoundation.CFStringRef,
    ): platform.CoreFoundation.CFMutableDictionaryRef? {
        val query = CFDictionaryCreateMutable(null, 0, null, null) ?: return null
        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(query, kSecAttrService, service)
        CFDictionarySetValue(query, kSecAttrAccount, account)
        return query
    }

    private fun cfString(value: String): platform.CoreFoundation.CFStringRef? =
        CFStringCreateWithCString(null, value, kCFStringEncodingUTF8)

    private fun NSError?.toBiometricException(): BiometricKeyStoreException = when (this?.code) {
        LAErrorUserCancel,
        LAErrorUserFallback,
        LAErrorSystemCancel,
        LAErrorAppCancel,
        -> BiometricKeyStoreException.Cancelled()
        LAErrorBiometryNotEnrolled -> BiometricKeyStoreException.NotEnrolled()
        else -> BiometricKeyStoreException.AuthenticationFailed()
    }

    private fun Int.toBiometricException(): BiometricKeyStoreException = when (this) {
        errSecUserCanceled -> BiometricKeyStoreException.Cancelled()
        errSecItemNotFound,
        errSecDecode,
        -> BiometricKeyStoreException.Invalidated()
        errSecNotAvailable,
        errSecInteractionNotAllowed,
        -> BiometricKeyStoreException.NotAvailable()
        errSecAuthFailed -> BiometricKeyStoreException.AuthenticationFailed()
        else -> BiometricKeyStoreException.AuthenticationFailed()
    }

    private fun markerKey(vaultId: String): String = "biometric.unlock.enabled.$vaultId"

    private fun <T> internalFailure(): Result<T> =
        Result.failure(IllegalStateException("Biometric Keychain operation failed"))

    private companion object {
        const val SERVICE_NAME = "com.passvault.biometric-unlock"
        const val ENROLLMENT_REASON = "Enable biometric unlock for this vault"
        const val UNLOCK_REASON = "Unlock PassVault"
        const val VAULT_KEY_BYTES = 32
    }
}
