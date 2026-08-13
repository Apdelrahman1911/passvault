package com.passvault.shared.platform

import com.passvault.core.domain.repository.LanguagePreference

/** Resolves an explicit app-language preference without losing the native system direction. */
internal fun LanguagePreference.usesRtlLayout(systemUsesRtl: Boolean): Boolean = when (this) {
    LanguagePreference.SYSTEM -> systemUsesRtl
    LanguagePreference.ENGLISH -> false
    LanguagePreference.ARABIC -> true
}
