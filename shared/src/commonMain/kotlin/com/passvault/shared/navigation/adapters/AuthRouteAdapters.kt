package com.passvault.shared.navigation.adapters

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import com.passvault.core.navigation.AuthRoute
import com.passvault.core.navigation.BackDisposition
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.PassVaultRouteKind
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import com.passvault.feature.onboarding.ui.ConfirmPasswordScreen
import com.passvault.feature.onboarding.ui.CreatePasswordScreen
import com.passvault.feature.onboarding.ui.OnboardingScreen
import com.passvault.feature.onboarding.ui.SecurityExplanationRoute
import com.passvault.feature.unlock.presentation.UnlockScreen
import com.passvault.feature.unlock.presentation.UnlockViewModel
import com.passvault.shared.navigation.RegisterBackDisposition
import com.passvault.shared.navigation.RouteAdapterContext
import com.passvault.shared.navigation.checkExpected
import com.passvault.shared.navigation.entryNavigationToken

internal val authRouteAdapterKinds = setOf(
    PassVaultRouteKind.AUTH_ONBOARDING,
    PassVaultRouteKind.AUTH_CREATE_PASSWORD,
    PassVaultRouteKind.AUTH_CONFIRM_PASSWORD,
    PassVaultRouteKind.AUTH_SECURITY_EXPLANATION,
    PassVaultRouteKind.AUTH_UNLOCK,
)

internal fun EntryProviderScope<PassVaultRoute>.authRouteAdapters(context: RouteAdapterContext) {
    entry<AuthRoute.Onboarding> { route ->
        val state by context.onboardingViewModel.state.collectAsState()
        val token = entryNavigationToken(context.navigator, route)
        RegisterBackDisposition(
            coordinator = context.backCoordinator,
            token = token,
            disposition = if (state.isLoading || state.vaultCreated) {
                BackDisposition.Blocked
            } else {
                BackDisposition.ExitApplication
            },
        )
        OnboardingScreen(
            viewModel = context.onboardingViewModel,
        )
    }

    entry<AuthRoute.CreatePassword> { route ->
        OnboardingFlowEntry(context, route) {
            CreatePasswordScreen(viewModel = context.onboardingViewModel)
        }
    }

    entry<AuthRoute.ConfirmPassword> { route ->
        OnboardingFlowEntry(context, route) {
            ConfirmPasswordScreen(viewModel = context.onboardingViewModel)
        }
    }

    entry<AuthRoute.SecurityExplanation> { route ->
        OnboardingFlowEntry(context, route) {
            SecurityExplanationRoute(viewModel = context.onboardingViewModel)
        }
    }

    entry<AuthRoute.Unlock> { route -> UnlockEntry(context, route) }
}

@androidx.compose.runtime.Composable
private fun UnlockEntry(context: RouteAdapterContext, route: AuthRoute.Unlock) {
    val state by context.unlockViewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    val disposition = when {
        state.isLoading || state.isBiometricLoading -> BackDisposition.Blocked
        state.showRecoveryInfo -> BackDisposition.HandleInPlace
        else -> BackDisposition.ExitApplication
    }
    RegisterBackDisposition(
        coordinator = context.backCoordinator,
        token = token,
        disposition = disposition,
        handleInPlace = {
            context.unlockViewModel.onEvent(UnlockViewModel.UnlockEvent.OnDismissRecoveryInfo)
        },
    )
    LaunchedEffect(context.unlockViewModel, token) {
        context.unlockViewModel.effect.collect { effect ->
            if (!context.navigator.isCurrent(token)) return@collect
            when (effect) {
                UnlockViewModel.UnlockEffect.NavigateToVault -> Unit
                UnlockViewModel.UnlockEffect.NavigateToOnboarding -> context.navigator.requireOnboarding()
            }
        }
    }
    UnlockScreen(state = state, onEvent = context.unlockViewModel::onEvent)
}

@androidx.compose.runtime.Composable
private fun OnboardingFlowEntry(
    context: RouteAdapterContext,
    route: AuthRoute,
    content: @androidx.compose.runtime.Composable (com.passvault.core.navigation.NavigationToken) -> Unit,
) {
    val state by context.onboardingViewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    val disposition = if (state.isLoading || state.vaultCreated) {
        BackDisposition.Blocked
    } else {
        BackDisposition.PopNow
    }
    RegisterBackDisposition(
        coordinator = context.backCoordinator,
        token = token,
        disposition = disposition,
        beforePop = context.onboardingViewModel::clearForLock,
    )
    content(token)
}

@androidx.compose.runtime.Composable
internal fun ObserveAuthenticationNavigationEffects(context: RouteAdapterContext) {
    androidx.compose.runtime.LaunchedEffect(context.onboardingViewModel, context.navigator) {
        context.onboardingViewModel.effect.collect { effect ->
            val route = context.navigator.state.currentRoute()
            val token = context.navigator.currentToken()
            when (effect) {
                OnboardingViewModel.OnboardingEffect.NavigateToMasterPasswordCreation ->
                    if (route == AuthRoute.Onboarding) {
                        context.navigator.pushAuthentication(AuthRoute.CreatePassword, token).checkExpected()
                    }
                OnboardingViewModel.OnboardingEffect.NavigateToMasterPasswordConfirmation ->
                    if (route == AuthRoute.CreatePassword) {
                        context.navigator.pushAuthentication(AuthRoute.ConfirmPassword, token).checkExpected()
                    }
                OnboardingViewModel.OnboardingEffect.NavigateToSecurityExplanation ->
                    if (route == AuthRoute.ConfirmPassword) {
                        context.navigator.pushAuthentication(AuthRoute.SecurityExplanation, token).checkExpected()
                    }
                OnboardingViewModel.OnboardingEffect.NavigateBack ->
                    if (route is AuthRoute) context.popAfterGuard(token)
                OnboardingViewModel.OnboardingEffect.NavigateToVault -> Unit
            }
        }
    }
}
