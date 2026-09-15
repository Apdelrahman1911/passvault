package com.passvault.feature.vault.presentation

import app.cash.turbine.test
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpConfiguration
import com.passvault.core.domain.repository.CredentialTotpInput
import com.passvault.core.domain.repository.CredentialTotpInputLease
import com.passvault.core.domain.repository.CredentialTotpRepository
import com.passvault.core.otp.StandardTotpService
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TwoFactorCodesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var backingRepository: FakeCredentialRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        backingRepository = FakeCredentialRepository()
    }

    @AfterTest
    fun tearDown() {
        backingRepository.reset()
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `visible screen lists only credentials with authenticators and updates at period boundary`() =
        runTest(dispatcher) {
            backingRepository.setupCredentials(
                credential(id = "with-totp", withTotp = true),
                credential(id = "without-totp", withTotp = false),
            )
            val clock = MutableClock(Instant.fromEpochSeconds(59))
            val viewModel = TwoFactorCodesViewModel(backingRepository, StandardTotpService(), clock)

            viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnScreenVisible)
            runCurrent()

            val first = viewModel.state.value.items.single()
            assertEquals(CredentialId("with-totp"), first.credentialId)
            assertFalse(first.generationFailed)
            assertEquals(1, first.secondsRemaining)
            val firstCode = first.code

            clock.instant = Instant.fromEpochSeconds(60)
            advanceTimeBy(1_000)
            runCurrent()

            val refreshed = viewModel.state.value.items.single()
            assertFalse(refreshed.generationFailed)
            assertTrue(refreshed.secondsRemaining > 1)
            assertTrue(refreshed.code != firstCode)
            viewModel.clearForLock()
        }

    @Test
    fun `hiding screen cancels ticker clears rows and wipes repository seed copy`() = runTest(dispatcher) {
        backingRepository.setupCredentials(credential(id = "clearable", withTotp = true))
        val viewModel = TwoFactorCodesViewModel(
            credentialRepository = backingRepository,
            totpService = StandardTotpService(),
            clock = MutableClock(Instant.fromEpochSeconds(59)),
        )

        viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnScreenVisible)
        runCurrent()
        val ownedInput = backingRepository.getLastTotpInputsForTest().single()
        assertEquals(TEST_TOTP_SECRET, ownedInput.configuration.secret.toStringUnsafe())

        viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnScreenHidden)

        assertTrue(viewModel.state.value.items.isEmpty())
        assertTrue(ownedInput.configuration.secret.toStringUnsafe().all { it == '\u0000' })
    }

    @Test
    fun `lock after production but before handoff wipes the leased seed`() = runTest(dispatcher) {
        val input = totpInput("cancelled-boundary", TEST_TOTP_SECRET)
        val repository = ControlledTotpRepository(input)
        val viewModel = TwoFactorCodesViewModel(repository, StandardTotpService())

        viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnScreenVisible)
        runCurrent()
        assertEquals(TEST_TOTP_SECRET, input.configuration.secret.toStringUnsafe())

        repository.release(0)
        viewModel.clearForLock()
        runCurrent()

        assertTrue(viewModel.state.value.items.isEmpty())
        assertTrue(input.configuration.secret.toStringUnsafe().all { it == '\u0000' })
    }

    @Test
    fun `refresh wipes a superseded leased batch and retains only the replacement`() = runTest(dispatcher) {
        val first = totpInput("first", TEST_TOTP_SECRET)
        val second = totpInput("second", SECOND_TOTP_SECRET)
        val repository = ControlledTotpRepository(first, second)
        val viewModel = TwoFactorCodesViewModel(repository, StandardTotpService())

        viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnScreenVisible)
        runCurrent()
        viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnRefresh)
        runCurrent()

        assertTrue(first.configuration.secret.toStringUnsafe().all { it == '\u0000' })
        assertEquals(SECOND_TOTP_SECRET, second.configuration.secret.toStringUnsafe())

        repository.release(1)
        runCurrent()

        assertEquals(CredentialId("second"), viewModel.state.value.items.single().credentialId)
        viewModel.clearForLock()
        assertTrue(second.configuration.secret.toStringUnsafe().all { it == '\u0000' })
    }

    @Test
    fun `copy emits current code once and reports result`() = runTest(dispatcher) {
        backingRepository.setupCredentials(credential(id = "copy", withTotp = true))
        val viewModel = TwoFactorCodesViewModel(
            backingRepository,
            StandardTotpService(),
            MutableClock(Instant.fromEpochSeconds(59)),
        )
        viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnScreenVisible)
        runCurrent()
        val item = viewModel.state.value.items.single()

        viewModel.effect.test {
            viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnCopyCodeClick(item.credentialId))
            assertEquals(
                TwoFactorCodesViewModel.TwoFactorCodesEffect.CopyCode(item.code),
                awaitItem(),
            )
            viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnCopyResult(true))
            assertTrue(viewModel.state.value.statusMessage != null)
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.clearForLock()
    }

    @Test
    fun `repository failure exposes retry state without stale rows`() = runTest(dispatcher) {
        backingRepository.setupCredentials(credential(id = "failure", withTotp = true))
        backingRepository.setShouldFail()
        val viewModel = TwoFactorCodesViewModel(
            backingRepository,
            StandardTotpService(),
            Clock.System,
        )

        viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnScreenVisible)
        runCurrent()

        assertTrue(viewModel.state.value.loadFailed)
        assertTrue(viewModel.state.value.items.isEmpty())
        assertFalse(viewModel.state.value.isLoading)
    }

    private fun credential(id: String, withTotp: Boolean): Credential = TestData.credential(id = id).copy(
        title = id,
        totp = if (withTotp) {
            TotpConfiguration(
                secret = SensitiveText.from(TEST_TOTP_SECRET),
                issuer = "Example",
                accountName = "$id@example.com",
            )
        } else {
            null
        },
    )

    private class MutableClock(var instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private fun totpInput(id: String, secret: String): CredentialTotpInput = CredentialTotpInput(
        id = CredentialId(id),
        title = id,
        displayUsername = "$id@example.com",
        configuration = TotpConfiguration(
            secret = SensitiveText.from(secret),
            issuer = "Example",
            accountName = "$id@example.com",
        ),
    )

    private class ControlledTotpRepository(
        vararg inputs: CredentialTotpInput,
    ) : CredentialTotpRepository {
        private val pendingInputs = ArrayDeque(inputs.toList())
        private val releases = mutableListOf<CompletableDeferred<Unit>>()

        override suspend fun getCredentialsForTotpDisplay(): Result<CredentialTotpInputLease> {
            val input = pendingInputs.removeFirst()
            val lease = CredentialTotpInputLease.ownedByCurrentCoroutine(listOf(input))
            val release = CompletableDeferred<Unit>()
            releases += release
            release.await()
            return Result.success(lease)
        }

        fun release(index: Int) {
            releases[index].complete(Unit)
        }
    }

    private companion object {
        const val TEST_TOTP_SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
        const val SECOND_TOTP_SECRET = "JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP"
    }
}
