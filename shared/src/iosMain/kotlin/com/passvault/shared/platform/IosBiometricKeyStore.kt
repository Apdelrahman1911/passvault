@file:OptIn(kotlinx.cinterop.BetaInteropApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.shared.platform

import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricType
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import platform.LocalAuthentication.LAErrorBiometryLockout
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
import platform.Security.kSecUseAuthenticationContext
import platform.Security.kSecValueData
import kotlin.coroutines.resume

/**
 * Stores the VEK in a device-only Keychain item protected by the current Face
 * ID or Touch ID enrollment. The item is not synchronizable or transferable.
 */
class IosBiometricKeyStore : BiometricKeyStore {
    private val mutex = Mutex()

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
                LABiometryTypeFaceID -> BiometricType.FACE_ID
                LABiometryTypeTouchID -> BiometricType.TOUCH_ID
                else -> BiometricType.GENERIC
            }
            val availability = when {
                available -> BiometricAvailability.AVAILABLE
                error.value?.code == LAErrorBiometryNotEnrolled -> BiometricAvailability.NOT_ENROLLED
                error.value?.code == LAErrorBiometryLockout -> BiometricAvailability.LOCKED_OUT
                else -> BiometricAvailability.UNAVAILABLE
            }
            BiometricCapability(type, availability)
        }
    }

    override suspend fun contains(vaultId: String): Boolean = mutex.withLock {
        defaults.boolForKey(markerKey(vaultId))
    }

    override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> = mutex.withLock {
        val capability = getCapability()
        val prerequisiteFailure = enrollmentFailure(vaultKey.size, capability.availability)
        val authentication = if (prerequisiteFailure == null) {
            authenticateForEnrollment()
        } else {
            Result.failure(prerequisiteFailure)
        }
        authentication.fold(
            onSuccess = {
                withContext(Dispatchers.Default) {
                    // Clear the marker before replacement so a failed add cannot advertise
                    // a Keychain item that no longer exists.
                    defaults.removeObjectForKey(markerKey(vaultId))
                    val deleteResult = deleteKeychainItem(vaultId)
                    val result = deleteResult.exceptionOrNull()?.let { error -> Result.failure<Unit>(error) }
                        ?: addKeychainItem(vaultId, vaultKey)
                    if (result.isSuccess) {
                        defaults.setBool(true, markerKey(vaultId))
                    }
                    result
                }
            },
            onFailure = { error ->
                Result.failure(error)
            },
        )
    }

    override suspend fun retrieve(vaultId: String): Result<ByteArray> {
        var producedKey: ByteArray? = null
        var transferredToCaller = false
        return try {
            val result = mutex.withLock {
                if (!defaults.boolForKey(markerKey(vaultId))) {
                    return@withLock Result.failure(BiometricKeyStoreException.NotEnabled())
                }
                readKeychainItemCancellable(vaultId)
                    .onSuccess { producedKey = it }
                    .onFailure { error ->
                        if (error is BiometricKeyStoreException.Invalidated ||
                            error is BiometricKeyStoreException.NotEnabled
                        ) {
                            defaults.removeObjectForKey(markerKey(vaultId))
                            deleteKeychainItem(vaultId)
                        }
                    }
            }
            transferredToCaller = true
            result
        } finally {
            if (!transferredToCaller) producedKey?.fill(0)
        }
    }

    override suspend fun delete(vaultId: String): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.Default) {
            try {
                defaults.removeObjectForKey(markerKey(vaultId))
                deleteKeychainItem(vaultId)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                Result.failure(IllegalStateException("Unable to remove biometric unlock"))
            }
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
        val service = cfString(SERVICE_NAME)
        val account = service?.let { cfString(vaultId) }
        val data = account?.let {
            vaultKey.usePinned { pinned ->
                CFDataCreate(
                    null,
                    pinned.addressOf(0).reinterpret<UByteVar>(),
                    vaultKey.size.toLong(),
                )
            }
        }
        val accessControl = data?.let {
            SecAccessControlCreateWithFlags(
                allocator = null,
                protection = kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
                flags = kSecAccessControlBiometryCurrentSet,
                error = null,
            )
        }
        val query = if (service != null && account != null && accessControl != null) {
            baseQuery(service, account)
        } else {
            null
        }
        val result: Result<Unit> = when {
            service == null || account == null || data == null -> internalFailure()
            accessControl == null -> Result.failure(BiometricKeyStoreException.NotAvailable())
            query == null -> internalFailure()
            else -> {
                CFDictionarySetValue(query, kSecValueData, data)
                CFDictionarySetValue(query, kSecAttrAccessControl, accessControl)
                CFDictionarySetValue(query, kSecAttrSynchronizable, kCFBooleanFalse)
                val status = SecItemAdd(query, null)
                if (status == errSecSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(status.toBiometricException())
                }
            }
        }
        releaseCf(query)
        releaseCf(accessControl)
        releaseCf(data)
        releaseCf(account)
        releaseCf(service)
        return result
    }

    /**
     * SecItemCopyMatching is synchronous while iOS presents authentication.
     * Run it as a child so cancellation can invalidate the supplied LAContext
     * immediately, then wipe any key produced after the caller was cancelled.
     */
    private suspend fun readKeychainItemCancellable(vaultId: String): Result<ByteArray> {
        var producedKey: ByteArray? = null
        var transferred = false
        return try {
            val result = coroutineScope {
                val context = LAContext().apply { localizedReason = UNLOCK_REASON }
                val read = async(Dispatchers.Default) {
                    readKeychainItem(vaultId, context).onSuccess { producedKey = it }
                }
                try {
                    read.await()
                } finally {
                    // This runs promptly when the awaiting parent is cancelled.
                    // coroutineScope then waits until the synchronous Keychain
                    // call returns before the outer finally checks ownership.
                    context.invalidate()
                }
            }
            currentCoroutineContext().ensureActive()
            transferred = true
            result
        } finally {
            if (!transferred) producedKey?.fill(0)
        }
    }

    private fun readKeychainItem(vaultId: String, context: LAContext): Result<ByteArray> {
        val service = cfString(SERVICE_NAME)
        val account = service?.let { cfString(vaultId) }
        val authenticationContext = interpretCPointer<CPointed>(context.objcPtr())
        val query = if (service != null && account != null && authenticationContext != null) {
            baseQuery(service, account)
        } else {
            null
        }
        val outcome: Result<ByteArray> = if (query == null || authenticationContext == null) {
            internalFailure()
        } else {
            CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
            CFDictionarySetValue(query, kSecUseAuthenticationContext, authenticationContext)
            memScoped {
                val result = alloc<CFTypeRefVar>()
                result.value = null
                val status = SecItemCopyMatching(query, result.ptr)
                val value = result.value
                if (status != errSecSuccess) {
                    Result.failure(status.toBiometricException())
                } else if (value == null) {
                    internalFailure()
                } else {
                    try {
                        if (CFGetTypeID(value) != CFDataGetTypeID()) {
                            internalFailure()
                        } else {
                            val data = value.reinterpret<cnames.structs.__CFData>()
                            val length = CFDataGetLength(data)
                            val bytes = CFDataGetBytePtr(data)
                            if (length != VAULT_KEY_BYTES.toLong() || bytes == null) {
                                Result.failure(BiometricKeyStoreException.Invalidated())
                            } else {
                                Result.success(ByteArray(length.toInt()) { index -> bytes[index].toByte() })
                            }
                        }
                    } finally {
                        CFRelease(value)
                    }
                }
            }
        }
        releaseCf(query)
        releaseCf(account)
        releaseCf(service)
        return outcome
    }

    private fun deleteKeychainItem(vaultId: String): Result<Unit> {
        val service = cfString(SERVICE_NAME)
        val account = if (service == null) null else cfString(vaultId)
        val query = if (service != null && account != null) {
            baseQuery(service, account)
        } else {
            null
        }
        val result = if (query == null) {
            internalFailure()
        } else {
            val status = SecItemDelete(query)
            if (status == errSecSuccess || status == errSecItemNotFound) {
                Result.success(Unit)
            } else {
                Result.failure(status.toBiometricException())
            }
        }
        releaseCf(query)
        releaseCf(account)
        releaseCf(service)
        return result
    }

    private fun baseQuery(
        service: platform.CoreFoundation.CFStringRef,
        account: platform.CoreFoundation.CFStringRef,
    ): platform.CoreFoundation.CFMutableDictionaryRef? {
        val query = CFDictionaryCreateMutable(null, 0, null, null) ?: return null
        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(query, kSecAttrService, service)
        CFDictionarySetValue(query, kSecAttrAccount, account)
        CFDictionarySetValue(query, kSecAttrSynchronizable, kCFBooleanFalse)
        return query
    }
}

private fun enrollmentFailure(
    vaultKeySize: Int,
    availability: BiometricAvailability,
): BiometricKeyStoreException? = when {
    vaultKeySize != VAULT_KEY_BYTES -> BiometricKeyStoreException.AuthenticationFailed()
    availability == BiometricAvailability.NOT_ENROLLED -> BiometricKeyStoreException.NotEnrolled()
    availability == BiometricAvailability.LOCKED_OUT -> BiometricKeyStoreException.LockedOut()
    availability == BiometricAvailability.UNAVAILABLE -> BiometricKeyStoreException.NotAvailable()
    else -> null
}

private fun cfString(value: String): platform.CoreFoundation.CFStringRef? =
    CFStringCreateWithCString(null, value, kCFStringEncodingUTF8)

private fun releaseCf(value: COpaquePointer?) {
    if (value != null) {
        CFRelease(value)
    }
}

private fun NSError?.toBiometricException(): BiometricKeyStoreException = when (this?.code) {
    LAErrorUserCancel,
    LAErrorUserFallback,
    LAErrorSystemCancel,
    LAErrorAppCancel,
    -> BiometricKeyStoreException.Cancelled()
    LAErrorBiometryNotEnrolled -> BiometricKeyStoreException.NotEnrolled()
    LAErrorBiometryLockout -> BiometricKeyStoreException.LockedOut()
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

private const val SERVICE_NAME = "com.passvault.biometric-unlock"
private const val ENROLLMENT_REASON = "Enable biometric unlock for this vault"
private const val UNLOCK_REASON = "Unlock PassVault"
private const val VAULT_KEY_BYTES = 32
