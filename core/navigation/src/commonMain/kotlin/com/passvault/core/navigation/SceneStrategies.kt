package com.passvault.core.navigation

/**
 * Returns whether this route may only be displayed while a vault session is
 * unlocked. Keeping the policy beside the route hierarchy prevents a new
 * route from accidentally bypassing the lock-to-authentication redirect.
 */
fun PassVaultRoute.requiresUnlockedVault(): Boolean = this !is AuthRoute
