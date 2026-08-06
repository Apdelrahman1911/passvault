package com.passvault.android.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.coroutines.resume

/**
 * Protects the VEK with a non-exportable Android Keystore key whose every use
 * must be authorized by a Class 3 (strong) biometric prompt.
 *
 * Provider and OEM exceptions are intentionally collapsed at this platform
 * boundary so the common layer always fails closed with a stable reason.
 */
@Suppress("TooGenericExceptionCaught", "TooManyFunctions")
class AndroidBiometricKeyStore(
    private val context: Context,
) : BiometricKeyStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val mutex = Mutex()

    @Volatile
    private var attachedActivity: FragmentActivity? = null

    fun attach(activity: FragmentActivity) {
        attachedActivity = activity
    }

    fun detach(activity: FragmentActivity) {
        if (attachedActivity === activity) attachedActivity = null
    }

    override suspend fun getCapability(): BiometricCapability {
        val availability = try {
            when (BiometricManager.from(context).canAuthenticate(AUTHENTICATORS)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
                else -> BiometricAvailability.UNAVAILABLE
            }
        } catch (_: Exception) {
            BiometricAvailability.UNAVAILABLE
        }
        return BiometricCapability(BiometricType.GENERIC, availability)
    }

    override suspend fun contains(vaultId: String): Boolean = mutex.withLock {
        val suffix = vaultSuffix(vaultId)
        preferences.contains(ciphertextKey(suffix)) &&
            preferences.contains(ivKey(suffix)) &&
            loadKeyStore().containsAlias(alias(suffix))
    }

    override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> =
        mutex.withLock {
            if (vaultKey.size != VAULT_KEY_BYTES) {
                return@withLock Result.failure(BiometricKeyStoreException.AuthenticationFailed())
            }
            if (getCapability().availability != BiometricAvailability.AVAILABLE) {
                return@withLock Result.failure(BiometricKeyStoreException.NotAvailable())
            }

            val suffix = vaultSuffix(vaultId)
            deleteLocked(suffix)
            try {
                val secretKey = generateKey(alias(suffix))
                val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                    init(Cipher.ENCRYPT_MODE, secretKey)
                }
                val authenticatedCipher = authenticate(cipher).getOrThrow()
                val ciphertext = authenticatedCipher.doFinal(vaultKey)
                val iv = authenticatedCipher.iv
                check(iv.isNotEmpty() && ciphertext.isNotEmpty())
                check(
                    preferences.edit()
                        .putString(ciphertextKey(suffix), ciphertext.toBase64())
                        .putString(ivKey(suffix), iv.toBase64())
                        .commit(),
                )
                Result.success(Unit)
            } catch (cancel: CancellationException) {
                deleteLocked(suffix)
                throw cancel
            } catch (error: Exception) {
                deleteLocked(suffix)
                Result.failure(error.toBiometricException())
            }
        }

    override suspend fun retrieve(vaultId: String): Result<ByteArray> = mutex.withLock {
        val suffix = vaultSuffix(vaultId)
        val ciphertext = preferences.getString(ciphertextKey(suffix), null)?.fromBase64OrNull()
        val iv = preferences.getString(ivKey(suffix), null)?.fromBase64OrNull()
        if (ciphertext == null || iv == null || iv.isEmpty()) {
            return@withLock Result.failure(BiometricKeyStoreException.NotEnabled())
        }

        try {
            val key = loadKeyStore().getKey(alias(suffix), null) as? SecretKey
                ?: throw BiometricKeyStoreException.NotEnabled()
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
            }
            val authenticatedCipher = authenticate(cipher).getOrThrow()
            Result.success(authenticatedCipher.doFinal(ciphertext))
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (error: Exception) {
            val mapped = error.toBiometricException()
            if (mapped is BiometricKeyStoreException.Invalidated ||
                mapped is BiometricKeyStoreException.NotEnabled
            ) {
                deleteLocked(suffix)
            }
            Result.failure(mapped)
        }
    }

    override suspend fun delete(vaultId: String): Result<Unit> = mutex.withLock {
        try {
            deleteLocked(vaultSuffix(vaultId))
            Result.success(Unit)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("Unable to remove biometric unlock"))
        }
    }

    private suspend fun authenticate(cipher: Cipher): Result<Cipher> = withContext(Dispatchers.Main) {
        val activity = attachedActivity
            ?: return@withContext Result.failure(BiometricKeyStoreException.NotAvailable())
        suspendCancellableCoroutine { continuation ->
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        val authenticatedCipher = result.cryptoObject?.cipher
                        if (continuation.isActive) {
                            continuation.resume(
                                authenticatedCipher?.let(Result.Companion::success)
                                    ?: Result.failure(BiometricKeyStoreException.AuthenticationFailed()),
                            )
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(errorCode.toBiometricException()))
                        }
                    }
                },
            )
            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(com.passvault.android.R.string.app_name))
                .setSubtitle(context.getString(com.passvault.android.R.string.biometric_prompt_subtitle))
                .setAllowedAuthenticators(AUTHENTICATORS)
                .setNegativeButtonText(context.getString(android.R.string.cancel))
                .build()
            prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun generateKey(keyAlias: String): SecretKey {
        val builder = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE).run {
            init(builder.build())
            generateKey()
        }
    }

    private fun deleteLocked(suffix: String) {
        preferences.edit().remove(ciphertextKey(suffix)).remove(ivKey(suffix)).commit()
        loadKeyStore().deleteEntry(alias(suffix))
    }

    private fun loadKeyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    private fun vaultSuffix(vaultId: String): String = MessageDigest.getInstance("SHA-256")
        .digest(vaultId.encodeToByteArray())
        .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private fun alias(suffix: String): String = "passvault.biometric.$suffix"
    private fun ciphertextKey(suffix: String): String = "ciphertext.$suffix"
    private fun ivKey(suffix: String): String = "iv.$suffix"

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.fromBase64OrNull(): ByteArray? = try {
        Base64.decode(this, Base64.NO_WRAP)
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun Exception.toBiometricException(): Exception = when (this) {
        is BiometricKeyStoreException -> this
        is KeyPermanentlyInvalidatedException -> BiometricKeyStoreException.Invalidated()
        is AEADBadTagException -> BiometricKeyStoreException.Invalidated()
        else -> BiometricKeyStoreException.AuthenticationFailed()
    }

    private fun Int.toBiometricException(): BiometricKeyStoreException = when (this) {
        BiometricPrompt.ERROR_CANCELED,
        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
        BiometricPrompt.ERROR_USER_CANCELED,
        -> BiometricKeyStoreException.Cancelled()
        BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometricKeyStoreException.NotEnrolled()
        BiometricPrompt.ERROR_HW_NOT_PRESENT,
        BiometricPrompt.ERROR_HW_UNAVAILABLE,
        -> BiometricKeyStoreException.NotAvailable()
        else -> BiometricKeyStoreException.AuthenticationFailed()
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val PREFERENCES_NAME = "biometric_unlock"
        const val KEY_SIZE_BITS = 256
        const val VAULT_KEY_BYTES = 32
        const val GCM_TAG_BITS = 128
        const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG
    }
}
