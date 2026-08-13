package com.passvault.shared.platform

import androidx.compose.ui.unit.LayoutDirection
import com.passvault.core.domain.repository.LanguagePreference
import com.passvault.feature.settings.presentation.SettingsViewModel
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplication
import platform.UIKit.UISemanticContentAttributeForceLeftToRight
import platform.UIKit.UISemanticContentAttributeForceRightToLeft
import platform.UIKit.UIUserInterfaceLayoutDirection.UIUserInterfaceLayoutDirectionRightToLeft
import platform.UIKit.UIViewController

internal const val IOS_APP_LANGUAGE_KEY = "language"

internal fun initialIosLayoutDirection(
    defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
): LayoutDirection {
    val preference = defaults.stringForKey(IOS_APP_LANGUAGE_KEY)
        ?.let { stored -> LanguagePreference.entries.firstOrNull { it.name == stored } }
        ?: LanguagePreference.SYSTEM
    return preference.toIosLayoutDirection()
}

internal fun SettingsViewModel.AppLanguage.toIosLayoutDirection(): LayoutDirection {
    val preference = when (this) {
        SettingsViewModel.AppLanguage.SYSTEM -> LanguagePreference.SYSTEM
        SettingsViewModel.AppLanguage.ENGLISH -> LanguagePreference.ENGLISH
        SettingsViewModel.AppLanguage.ARABIC -> LanguagePreference.ARABIC
    }
    return preference.toIosLayoutDirection()
}

private fun LanguagePreference.toIosLayoutDirection(): LayoutDirection {
    val systemUsesRtl = UIApplication.sharedApplication.userInterfaceLayoutDirection ==
        UIUserInterfaceLayoutDirectionRightToLeft
    return if (usesRtlLayout(systemUsesRtl)) LayoutDirection.Rtl else LayoutDirection.Ltr
}

/**
 * Compose UI 1.11.x derives its public start-edge navigation input from the
 * host view's effective UIKit direction. Refresh attachment only after a real
 * semantic-direction change; never inspect or reorder Compose recognizers.
 * Revalidate this compatibility call after every Compose UI upgrade.
 */
internal fun applyIosNativeLayoutDirection(
    viewController: UIViewController,
    direction: LayoutDirection,
) {
    val rootView = viewController.view
    val semanticDirection = when (direction) {
        LayoutDirection.Ltr -> UISemanticContentAttributeForceLeftToRight
        LayoutDirection.Rtl -> UISemanticContentAttributeForceRightToLeft
    }
    if (rootView.semanticContentAttribute == semanticDirection) return
    rootView.semanticContentAttribute = semanticDirection
    if (rootView.window != null) rootView.didMoveToWindow()
}
