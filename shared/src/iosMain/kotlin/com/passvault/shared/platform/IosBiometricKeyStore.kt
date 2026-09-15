@file:OptIn(kotlinx.cinterop.BetaInteropApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.shared.platform

import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricPromptController
import com.passvault.core.security.BiometricType
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFArrayGetCount
import platform.CoreFoundation.CFArrayGetTypeID
import platform.CoreFoundation.CFArrayGetValueAtIndex
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataGetTypeID
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryGetTypeID
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFGetTypeID
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringGetCString
import platform.CoreFoundation.CFStringGetLength
import platform.CoreFoundation.CFStringGetMaximumSizeForEncoding
import platform.CoreFoundation.CFStringGetTypeID
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
import platform.Security.kSecMatchLimitAll
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnAttributes
import platform.Security.kSecReturnData
import platform.Security.kSecUseAuthenticationContext
import platform.Security.kSecValueData

/**
 * Stores the VEK in a device-only Keychain item protected by the current Face
 * ID or Touch ID enrollment. The item is not synchronizable or transferable.
 */
class IosBiometricKeyStore : BiometricKeyStore, BiometricPromptController {
    private val mutex = Mutex()
    private val promptCoordinator = IosBiometricPromptCoordinator<LAContext>(LAContext::invalidate)

    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    private val enrollmentState = IosBiometricEnrollmentState(
        marker = object : IosBiometricEnrollmentMarker {
            override fun mark(vaultId: String) {
                defaults.setBool(true, markerKey(vaultId))
            }

            override fun clear(vaultId: String) {
                defaults.removeObjectForKey(markerKey(vaultId))
            }
        },
        keychain = object : IosBiometricKeychainLifecycle {
            override fun containsWithoutAuthentication(vaultId: String): Result<Boolean> =
                probeKeychainItem(vaultId)

            override fun accountsWithoutAuthentication(): Result<Set<String>> = listKeychainAccounts()

            override fun delete(vaultId: String): Result<Unit> = deleteKeychainItem(vaultId)

            override fun deleteAll(): Result<Unit> = deleteAllKeychainItems()
        },
    )

    override fun cancelActive() = promptCoordinator.cancelActive()

    override suspend fun getCapability(): BiometricCapability = withNewLAContext { context ->
        memScoped {
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
        withContext(Dispatchers.Default) {
            enrollmentState.contains(vaultId).getOrThrow()
        }
    }

    override suspend fun reconcile(activeVaultId: String?): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.Default) {
            enrollmentState.reconcile(activeVaultId)
        }
    }

    override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> =
        promptCoordinator.withOperation(
            onBusy = { Result.failure(BiometricKeyStoreException.Cancelled()) },
        ) { promptOperation ->
            mutex.withLock {
                val capability = getCapability()
                val prerequisiteFailure = enrollmentFailure(vaultKey.size, capability.availability)
                val authentication = if (prerequisiteFailure == null) {
                    authenticateForEnrollment(promptOperation)
                } else {
                    Result.failure(prerequisiteFailure)
                }
                authentication.fold(
                    onSuccess = {
                        withContext(Dispatchers.Default) {
                            enrollmentState.replace(vaultId) {
                                addKeychainItem(vaultId, vaultKey)
                            }
                        }
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    },
                )
            }
        }

    override suspend fun retrieve(vaultId: String): Result<ByteArray> =
        promptCoordinator.withOperation(
            onBusy = { Result.failure(BiometricKeyStoreException.Cancelled()) },
        ) { promptOperation ->
            var producedKey: ByteArray? = null
            var transferredToCaller = false
            try {
                val result = mutex.withLock {
                    val presence = withContext(Dispatchers.Default) {
                        enrollmentState.contains(vaultId)
                    }
                    if (presence.isFailure) {
                        return@withLock Result.failure(requireNotNull(presence.exceptionOrNull()))
                    }
                    if (presence.getOrThrow() != true) {
                        return@withLock Result.failure(BiometricKeyStoreException.NotEnabled())
                    }
                    val readResult = readKeychainItemCancellable(vaultId, promptOperation)
                        .onSuccess { producedKey = it }
                    val error = readResult.exceptionOrNull()
                    if (error is BiometricKeyStoreException.Invalidated ||
                        error is BiometricKeyStoreException.NotEnabled
                    ) {
                        withContext(Dispatchers.Default) {
                            enrollmentState.delete(vaultId)
                        }
                    }
                    readResult
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
                enrollmentState.delete(vaultId)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                Result.failure(IllegalStateException("Unable to remove biometric unlock"))
            }
        }
    }

    private suspend fun authenticateForEnrollment(
        operation: IosBiometricPromptCoordinator.Operation,
    ): Result<Unit> = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val context = LAContext()
            continuation.invokeOnCancellation { promptCoordinator.cancel(operation) }
            if (
                promptCoordinator.activate(
                    operation = operation,
                    context = context,
                    reportCancelled = {
                        continuation.resumeIfPending(Result.failure(BiometricKeyStoreException.Cancelled()))
                    },
                )
            ) {
                context.evaluatePolicy(
                    LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                    localizedReason = ENROLLMENT_REASON,
                ) { success, error ->
                    promptCoordinator.finishPrompt(operation, context) {
                        continuation.resumeIfPending(
                            if (success) Result.success(Unit) else Result.failure(error.toBiometricException()),
                        )
                    }
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
     * Dispatch it outside the caller job so cancellation can invalidate the
     * LAContext without waiting for the blocking call. Any late key is wiped.
     */
    private suspend fun readKeychainItemCancellable(
        vaultId: String,
        operation: IosBiometricPromptCoordinator.Operation,
    ): Result<ByteArray> = suspendCancellableCoroutine { continuation ->
        val context = LAContext().apply { localizedReason = UNLOCK_REASON }
        continuation.invokeOnCancellation { promptCoordinator.cancel(operation) }
        if (
            promptCoordinator.activate(
                operation = operation,
                context = context,
                reportCancelled = {
                    continuation.resumeKeyResult(Result.failure(BiometricKeyStoreException.Cancelled()))
                },
            )
        ) {
            val read = Runnable {
                val result = try {
                    readKeychainItem(vaultId, context)
                } catch (_: Exception) {
                    Result.failure(BiometricKeyStoreException.AuthenticationFailed())
                }
                val accepted = promptCoordinator.finishPrompt(operation, context) {
                    continuation.resumeKeyResult(result)
                }
                if (!accepted) result.getOrNull()?.fill(0)
            }
            try {
                Dispatchers.Default.dispatch(continuation.context, read)
            } catch (_: Exception) {
                promptCoordinator.finishPrompt(operation, context) {
                    continuation.resumeKeyResult(Result.failure(BiometricKeyStoreException.AuthenticationFailed()))
                }
            }
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

    /** Checks item metadata with UI explicitly disabled; no VEK bytes are requested. */
    private fun probeKeychainItem(vaultId: String): Result<Boolean> = withNonInteractiveLAContext { context ->
        val service = cfString(SERVICE_NAME)
        val account = service?.let { cfString(vaultId) }
        val authenticationContext = interpretCPointer<CPointed>(context.objcPtr())
        val query = if (service != null && account != null && authenticationContext != null) {
            baseQuery(service, account)
        } else {
            null
        }
        val result = if (query == null || authenticationContext == null) {
            internalFailure()
        } else {
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
            CFDictionarySetValue(query, kSecUseAuthenticationContext, authenticationContext)
            when (val status = SecItemCopyMatching(query, null)) {
                errSecSuccess,
                errSecInteractionNotAllowed,
                -> Result.success(true)
                errSecItemNotFound -> Result.success(false)
                else -> Result.failure(status.toBiometricException())
            }
        }
        releaseCf(query)
        releaseCf(account)
        releaseCf(service)
        result
    }

    /** Enumerates only non-secret account attributes so old-install orphans can be retired. */
    private fun listKeychainAccounts(): Result<Set<String>> = withNonInteractiveLAContext { context ->
        val service = cfString(SERVICE_NAME)
        val authenticationContext = interpretCPointer<CPointed>(context.objcPtr())
        val query = service?.let(::serviceQuery)
        val outcome = if (query == null || authenticationContext == null) {
            internalFailure()
        } else {
            CFDictionarySetValue(query, kSecReturnAttributes, kCFBooleanTrue)
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitAll)
            CFDictionarySetValue(query, kSecUseAuthenticationContext, authenticationContext)
            memScoped {
                val result = alloc<CFTypeRefVar>()
                result.value = null
                when (val status = SecItemCopyMatching(query, result.ptr)) {
                    errSecItemNotFound -> Result.success(emptySet())
                    errSecSuccess -> result.value?.let(::keychainAccountsFromAttributes) ?: internalFailure()
                    else -> Result.failure(status.toBiometricException())
                }.also {
                    result.value?.let(::CFRelease)
                }
            }
        }
        releaseCf(query)
        releaseCf(service)
        outcome
    }

    private fun deleteKeychainItem(vaultId: String): Result<Unit> = deleteKeychainItems(vaultId)

    private fun deleteAllKeychainItems(): Result<Unit> = deleteKeychainItems(vaultId = null)

    /** Deletion must never turn startup reconciliation into an authentication prompt. */
    private fun deleteKeychainItems(vaultId: String?): Result<Unit> = withNonInteractiveLAContext { context ->
        val service = cfString(SERVICE_NAME)
        val account = if (service != null && vaultId != null) cfString(vaultId) else null
        val authenticationContext = interpretCPointer<CPointed>(context.objcPtr())
        val query = when {
            service == null || authenticationContext == null -> null
            vaultId == null -> serviceQuery(service)
            account != null -> baseQuery(service, account)
            else -> null
        }
        val result = if (query == null || authenticationContext == null) {
            internalFailure()
        } else {
            CFDictionarySetValue(query, kSecUseAuthenticationContext, authenticationContext)
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
        result
    }

    private fun baseQuery(
        service: platform.CoreFoundation.CFStringRef,
        account: platform.CoreFoundation.CFStringRef,
    ): platform.CoreFoundation.CFMutableDictionaryRef? {
        val query = serviceQuery(service) ?: return null
        CFDictionarySetValue(query, kSecAttrAccount, account)
        return query
    }

    private fun serviceQuery(
        service: platform.CoreFoundation.CFStringRef,
    ): platform.CoreFoundation.CFMutableDictionaryRef? {
        val query = CFDictionaryCreateMutable(null, 0, null, null) ?: return null
        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(query, kSecAttrService, service)
        CFDictionarySetValue(query, kSecAttrSynchronizable, kCFBooleanFalse)
        return query
    }
}

private inline fun <T> withNonInteractiveLAContext(block: (LAContext) -> T): T {
    val context = LAContext().apply { interactionNotAllowed = true }
    return try {
        block(context)
    } finally {
        context.invalidate()
    }
}

private fun keychainAccountsFromAttributes(value: COpaquePointer): Result<Set<String>> {
    val dictionaries = keychainAttributeDictionaries(value) ?: return internalFailure()
    val accounts = linkedSetOf<String>()
    var valid = true
    dictionaries.forEach { attributes ->
        val account = attributes
            ?.takeIf { CFGetTypeID(it) == CFDictionaryGetTypeID() }
            ?.reinterpret<cnames.structs.__CFDictionary>()
            ?.let { CFDictionaryGetValue(it, kSecAttrAccount) }
            ?.let(::cfStringValue)
        if (account == null) valid = false else accounts += account
    }
    return if (valid) Result.success(accounts) else internalFailure()
}

private fun keychainAttributeDictionaries(value: COpaquePointer): List<COpaquePointer?>? =
    when (CFGetTypeID(value)) {
        CFArrayGetTypeID() -> {
            val array = value.reinterpret<cnames.structs.__CFArray>()
            val count = CFArrayGetCount(array)
            if (count in 0..MAX_KEYCHAIN_ACCOUNT_COUNT) {
                List(count.toInt()) { index -> CFArrayGetValueAtIndex(array, index.toLong()) }
            } else {
                null
            }
        }
        CFDictionaryGetTypeID() -> listOf(value)
        else -> null
    }

private fun cfStringValue(value: COpaquePointer?): String? {
    val string = value
        ?.takeIf { CFGetTypeID(it) == CFStringGetTypeID() }
        ?.reinterpret<cnames.structs.__CFString>()
    val maximumBytes = string?.let {
        CFStringGetMaximumSizeForEncoding(CFStringGetLength(it), kCFStringEncodingUTF8) + 1
    }
    val bufferSize = maximumBytes
        ?.takeIf { it > 1 }
        ?.takeIf { it <= MAX_KEYCHAIN_ACCOUNT_UTF8_BYTES }
    return if (string == null || bufferSize == null) {
        null
    } else {
        memScoped {
            val buffer = allocArray<ByteVar>(bufferSize)
            if (CFStringGetCString(string, buffer, bufferSize, kCFStringEncodingUTF8)) {
                buffer.toKString()
            } else {
                null
            }
        }
    }
}

private fun <T> CancellableContinuation<T>.resumeIfPending(value: T) {
    resume(value) { _, _, _ -> }
}

private fun CancellableContinuation<Result<ByteArray>>.resumeKeyResult(value: Result<ByteArray>) {
    resume(value) { _, undelivered, _ ->
        undelivered.getOrNull()?.fill(0)
    }
}

private suspend fun <T> withNewLAContext(
    configure: LAContext.() -> Unit = {},
    block: suspend (LAContext) -> T,
): T = withInvalidatedResource(LAContext().apply(configure), LAContext::invalidate, block)

internal suspend fun <Resource, Result> withInvalidatedResource(
    resource: Resource,
    invalidate: (Resource) -> Unit,
    block: suspend (Resource) -> Result,
): Result = try {
    block(resource)
} finally {
    invalidate(resource)
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
private const val MAX_KEYCHAIN_ACCOUNT_COUNT = 256L
private const val MAX_KEYCHAIN_ACCOUNT_UTF8_BYTES = 4_096L
