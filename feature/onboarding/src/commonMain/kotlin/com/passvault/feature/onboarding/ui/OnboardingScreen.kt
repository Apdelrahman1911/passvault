package com.passvault.feature.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.passvault.feature.onboarding.presentation.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    WelcomeScreen(
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun CreatePasswordScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    MasterPasswordCreationScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun ConfirmPasswordScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    MasterPasswordConfirmationScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun SecurityExplanationRoute(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    SecurityExplanationScreen(
        canNavigateBack = !state.vaultCreated,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}
