package com.passvault.shared.navigation.adapters

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import com.passvault.core.designsystem.text.resolveSuspending
import com.passvault.core.navigation.BackDisposition
import com.passvault.core.navigation.GeneratorRoute
import com.passvault.core.navigation.HealthRoute
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.PassVaultRouteKind
import com.passvault.core.navigation.TwoFactorRoute
import com.passvault.core.navigation.VaultRoute
import com.passvault.feature.generator.presentation.GeneratorViewModel
import com.passvault.feature.generator.ui.GeneratorScreen
import com.passvault.feature.health.presentation.HealthViewModel
import com.passvault.feature.health.ui.HealthScreen
import com.passvault.feature.vault.presentation.TwoFactorCodesViewModel
import com.passvault.feature.vault.ui.TwoFactorCodesScreen
import com.passvault.shared.navigation.RegisterBackDisposition
import com.passvault.shared.navigation.RouteAdapterContext
import com.passvault.shared.navigation.checkExpected
import com.passvault.shared.navigation.entryNavigationToken
import org.koin.compose.viewmodel.koinViewModel

internal val toolsRouteAdapterKinds = setOf(
    PassVaultRouteKind.GENERATOR_ROOT,
    PassVaultRouteKind.HEALTH_ROOT,
    PassVaultRouteKind.TWO_FACTOR_ROOT,
)

internal fun EntryProviderScope<PassVaultRoute>.toolsRouteAdapters(context: RouteAdapterContext) {
    entry<GeneratorRoute.Generator> { route -> GeneratorEntry(context, route) }
    entry<HealthRoute.Health> { route -> HealthEntry(context, route) }
    entry<TwoFactorRoute.Codes> { route -> TwoFactorCodesEntry(context, route) }
}

@androidx.compose.runtime.Composable
private fun GeneratorEntry(context: RouteAdapterContext, route: GeneratorRoute.Generator) {
    val viewModel: GeneratorViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterBackDisposition(
        coordinator = context.backCoordinator,
        token = token,
        disposition = context.navigator.defaultBackDisposition(),
        handleInPlace = {
            context.navigator.handleDefaultInPlaceBack(token).checkExpected()
        },
    )
    LaunchedEffect(viewModel) { viewModel.ensureGenerated() }
    LaunchedEffect(viewModel, token) {
        viewModel.effect.collect { effect ->
            if (!context.navigator.isCurrent(token)) return@collect
            val value = when (effect) {
                is GeneratorViewModel.GeneratorEffect.CopyToClipboard -> effect.password
                is GeneratorViewModel.GeneratorEffect.UsePassword -> effect.password
            }
            viewModel.onEvent(
                GeneratorViewModel.GeneratorEvent.OnCopyResult(context.copySensitive(value)),
            )
        }
    }
    GeneratorScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = { context.backCoordinator.requestBack() },
        onNavigateToHealth = { context.push(HealthRoute.Health, token) },
        showBackButton = false,
    )
}

@androidx.compose.runtime.Composable
private fun HealthEntry(context: RouteAdapterContext, route: HealthRoute.Health) {
    val viewModel: HealthViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    val disposition = if (state.showingDuplicateGroup != null) {
        BackDisposition.HandleInPlace
    } else {
        BackDisposition.PopNow
    }
    RegisterBackDisposition(
        coordinator = context.backCoordinator,
        token = token,
        disposition = disposition,
        handleInPlace = { viewModel.onEvent(HealthViewModel.HealthEvent.OnDismissDuplicateGroup) },
        blocksForwardNavigation = disposition == BackDisposition.HandleInPlace,
    )
    LaunchedEffect(viewModel) { viewModel.onEvent(HealthViewModel.HealthEvent.OnRefreshScan) }
    LaunchedEffect(viewModel, token) {
        viewModel.effect.collect { effect ->
            if (!context.navigator.isCurrent(token)) return@collect
            when (effect) {
                HealthViewModel.HealthEffect.NavigateBack -> context.popAfterGuard(token)
                is HealthViewModel.HealthEffect.NavigateToCredential ->
                    context.openCredential(effect.credentialId, token)
                is HealthViewModel.HealthEffect.NavigateToEditCredential ->
                    context.push(VaultRoute.CredentialEdit(effect.credentialId.value), token)
                is HealthViewModel.HealthEffect.CopySummary -> {
                    val copied = context.copySensitive(effect.report.resolveSuspending())
                    viewModel.onEvent(HealthViewModel.HealthEvent.OnCopySummaryResult(copied))
                }
            }
        }
    }
    HealthScreen(state = state, onEvent = viewModel::onEvent)
}

@androidx.compose.runtime.Composable
private fun TwoFactorCodesEntry(context: RouteAdapterContext, route: TwoFactorRoute.Codes) {
    val viewModel: TwoFactorCodesViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterBackDisposition(
        coordinator = context.backCoordinator,
        token = token,
        disposition = context.navigator.defaultBackDisposition(),
        handleInPlace = {
            context.navigator.handleDefaultInPlaceBack(token).checkExpected()
        },
    )
    LaunchedEffect(viewModel, token) {
        viewModel.effect.collect { effect ->
            if (!context.navigator.isCurrent(token)) return@collect
            when (effect) {
                TwoFactorCodesViewModel.TwoFactorCodesEffect.NavigateBack ->
                    context.backCoordinator.requestBack()
                is TwoFactorCodesViewModel.TwoFactorCodesEffect.NavigateToCredential ->
                    context.openCredential(effect.credentialId, token)
                is TwoFactorCodesViewModel.TwoFactorCodesEffect.CopyCode -> {
                    val copied = context.copySensitive(effect.code)
                    viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnCopyResult(copied))
                }
            }
        }
    }
    TwoFactorCodesScreen(
        state = state,
        onEvent = viewModel::onEvent,
        showBackButton = false,
    )
}
