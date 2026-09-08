package com.passvault.shared.platform

import kotlinx.coroutines.flow.MutableStateFlow

/** App-authored native text; OS-owned buttons and permission dialogs remain OS-owned. */
data class NativeBiometricPromptStrings(
    val languageTag: String,
    val enrollmentReason: String,
    val unlockReason: String,
    val cancel: String,
) {
    companion object {
        /** Every supported locale supplies the same mandatory prompt fields. */
        fun forLanguageTag(languageTag: String): NativeBiometricPromptStrings =
            when (languageTag.substringBefore('-').substringBefore('_').lowercase()) {
                "ar" -> NativeBiometricPromptStrings(
                    languageTag = "ar",
                    enrollmentReason = "فعّل الفتح باستخدام المقاييس الحيوية لهذه الخزنة",
                    unlockReason = "افتح PassVault",
                    cancel = "إلغاء",
                )
                else -> NativeBiometricPromptStrings(
                    languageTag = "en",
                    enrollmentReason = "Enable biometric unlock for this vault",
                    unlockReason = "Unlock PassVault",
                    cancel = "Cancel",
                )
            }
    }
}

/**
 * Published only by AppLanguageProvider after it resolves the effective app
 * locale. Native adapters snapshot it at prompt creation, never an unrelated
 * Android Context resource locale or a JVM/Apple OS default of their own.
 */
private val nativeBiometricPromptStrings = MutableStateFlow(NativeBiometricPromptStrings.forLanguageTag("en"))

internal fun publishNativeBiometricPromptLanguage(languageTag: String) {
    nativeBiometricPromptStrings.value = NativeBiometricPromptStrings.forLanguageTag(languageTag)
}

fun currentNativeBiometricPromptStrings(): NativeBiometricPromptStrings = nativeBiometricPromptStrings.value
