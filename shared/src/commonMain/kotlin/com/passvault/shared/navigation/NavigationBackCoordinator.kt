package com.passvault.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation3.runtime.NavEntry
import com.passvault.core.navigation.AppNavigator
import com.passvault.core.navigation.BackDisposition
import com.passvault.core.navigation.NavigationMutation
import com.passvault.core.navigation.NavigationToken
import com.passvault.core.navigation.PassVaultRoute

internal data class BackPolicy(
    val disposition: BackDisposition,
    val blocksForwardNavigation: Boolean,
)

internal class BackRegistration(
    val token: NavigationToken,
    val disposition: BackDisposition,
    val handleInPlace: () -> Unit,
    val beforePop: () -> Unit,
    val blocksForwardNavigation: Boolean,
    private val readCurrentPolicy: (() -> BackPolicy)? = null,
) {
    fun resolvePolicy(): BackPolicy = readCurrentPolicy?.invoke()
        ?: BackPolicy(disposition, blocksForwardNavigation)
}

/** One policy boundary shared by system, gesture, toolbar, and keyboard Back. */
internal class NavigationBackCoordinator(
    private val navigator: AppNavigator,
) {
    private val registrations = mutableStateMapOf<PassVaultRoute, BackRegistration>()

    private fun activeRegistration(): BackRegistration? =
        registrations[navigator.state.currentRoute()]?.takeIf { candidate ->
            navigator.isCurrentEntry(candidate.token)
        }

    fun effectiveDisposition(hostResumed: Boolean = navigator.isHostResumed()): BackDisposition {
        if (!hostResumed) return BackDisposition.Blocked
        val current = activeRegistration()
        val disposition = current?.resolvePolicy()?.disposition
            ?: navigator.defaultBackDisposition(conservativeGuard = true)
        return if (disposition == BackDisposition.PopNow && !navigator.canPop()) {
            navigator.defaultBackDisposition(conservativeGuard = false)
        } else {
            disposition
        }
    }

    fun requestBack(): Boolean {
        if (!navigator.isHostResumed()) return true
        val active = activeRegistration()
        val policy = active?.resolvePolicy()
        return when (policy?.disposition ?: navigator.defaultBackDisposition(conservativeGuard = true)) {
            BackDisposition.PopNow -> {
                val token = active?.token ?: navigator.currentToken()
                active?.beforePop?.invoke()
                navigator.popAfterGuard(token) is NavigationMutation.Applied
            }
            BackDisposition.HandleInPlace -> {
                if (active != null) {
                    active.handleInPlace()
                    true
                } else {
                    navigator.handleDefaultInPlaceBack(navigator.currentToken()) is NavigationMutation.Applied
                }
            }
            BackDisposition.Blocked -> true
            BackDisposition.ExitApplication -> false
        }
    }

    fun canLeaveForForwardNavigation(): Boolean {
        return if (!navigator.isHostResumed()) {
            false
        } else {
            val active = activeRegistration()
            if (active == null) {
                navigator.defaultBackDisposition(conservativeGuard = true) != BackDisposition.Blocked
            } else {
                val policy = active.resolvePolicy()
                policy.disposition != BackDisposition.Blocked && !policy.blocksForwardNavigation
            }
        }
    }

    fun completeInteractivePop(): NavigationMutation {
        val hostResumed = navigator.isHostResumed()
        val active = if (hostResumed) {
            activeRegistration()?.takeIf { candidate ->
                candidate.resolvePolicy().disposition == BackDisposition.PopNow &&
                    navigator.isCurrentEntry(candidate.token)
            }
        } else {
            null
        }
        return when {
            !hostResumed -> NavigationMutation.Rejected(
                com.passvault.core.navigation.NavigationRejection.HostInactive,
            )
            active == null -> NavigationMutation.Rejected(
                com.passvault.core.navigation.NavigationRejection.EntryInactive,
            )
            else -> {
                active.beforePop()
                navigator.popAfterGuard(active.token)
            }
        }
    }

    internal fun register(value: BackRegistration) {
        if (registrations[value.token.route] === value) return
        registrations[value.token.route] = value
    }

    internal fun unregister(token: NavigationToken) {
        if (registrations[token.route]?.token === token) registrations.remove(token.route)
    }
}

@Composable
internal fun entryNavigationToken(
    navigator: AppNavigator,
    route: PassVaultRoute,
): NavigationToken {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val resumed = lifecycleState == Lifecycle.State.RESUMED
    return remember(navigator, route, resumed) {
        navigator.createToken(route, entryResumed = resumed)
    }
}

@Composable
internal fun RegisterBackDisposition(
    coordinator: NavigationBackCoordinator,
    token: NavigationToken,
    disposition: BackDisposition,
    handleInPlace: () -> Unit = {},
    beforePop: () -> Unit = {},
    blocksForwardNavigation: Boolean = disposition == BackDisposition.Blocked,
    currentPolicy: (() -> BackPolicy)? = null,
) {
    val currentHandleInPlace by rememberUpdatedState(handleInPlace)
    val currentBeforePop by rememberUpdatedState(beforePop)
    val currentPolicyProvider by rememberUpdatedState(currentPolicy)
    // The composed snapshot still invalidates rendering. Input can arrive before
    // the next frame, so entry-owned guards may also supply a synchronous policy.
    val registration = remember(token, disposition, blocksForwardNavigation) {
        BackRegistration(
            token = token,
            disposition = disposition,
            handleInPlace = { currentHandleInPlace() },
            beforePop = { currentBeforePop() },
            blocksForwardNavigation = blocksForwardNavigation,
            readCurrentPolicy = {
                currentPolicyProvider?.invoke() ?: BackPolicy(disposition, blocksForwardNavigation)
            },
        )
    }
    SideEffect {
        coordinator.register(registration)
    }
    DisposableEffect(token) {
        onDispose { coordinator.unregister(token) }
    }
}

internal fun <T : Any> entriesAllowedByBackPolicy(
    entries: List<NavEntry<T>>,
    disposition: BackDisposition,
): List<NavEntry<T>> = if (disposition == BackDisposition.PopNow) entries else entries.takeLast(1)
