package com.passvault.feature.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import kotlinx.coroutines.flow.collect

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToCreatePassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingViewModel.OnboardingEffect.NavigateToMasterPasswordCreation ->
                    onNavigateToCreatePassword()
                else -> Unit
            }
        }
    }

    WelcomeScreen(
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun CreatePasswordScreen(
    viewModel: OnboardingViewModel,
    onNavigateToConfirm: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingViewModel.OnboardingEffect.NavigateToMasterPasswordConfirmation ->
                    onNavigateToConfirm()
                OnboardingViewModel.OnboardingEffect.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    MasterPasswordCreationScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun ConfirmPasswordScreen(
    viewModel: OnboardingViewModel,
    onNavigateToSecurity: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingViewModel.OnboardingEffect.NavigateToSecurityExplanation ->
                    onNavigateToSecurity()
                OnboardingViewModel.OnboardingEffect.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    MasterPasswordConfirmationScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun SecurityExplanationRoute(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingViewModel.OnboardingEffect.NavigateToVault ->
                    onComplete()
                OnboardingViewModel.OnboardingEffect.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    SecurityExplanationScreen(
        canNavigateBack = !state.vaultCreated,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}
