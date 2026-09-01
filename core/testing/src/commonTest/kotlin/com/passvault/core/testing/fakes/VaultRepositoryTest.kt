package com.passvault.core.testing.fakes

import app.cash.turbine.test
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.VaultId
import com.passvault.core.domain.model.VaultMetadata
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VaultRepositoryTest {

    private lateinit var repository: FakeVaultRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeVaultRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exists returns false when no vault`() = runTest {
        repository.reset()

        val result = repository.exists()

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
    }

    @Test
    fun `exists returns true when vault exists`() = runTest {
        repository.setupExistingVault()

        val result = repository.exists()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun `create vault returns vault id`() = runTest {
        val password = SensitiveText.from("StrongPassword123!")

        val result = repository.create(password)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrThrow())
    }

    @Test
    fun `create vault sets exists to true`() = runTest {
        val password = SensitiveText.from("StrongPassword123!")

        repository.create(password)
        val exists = repository.exists().getOrThrow()

        assertTrue(exists)
    }

    @Test
    fun `create vault emits locked state`() = runTest {
        val password = SensitiveText.from("StrongPassword123!")

        repository.create(password)

        assertEquals(
            VaultSessionState.Locked(),
            repository.currentSessionState
        )
    }

    @Test
    fun `unlock with correct password succeeds`() = runTest {
        val password = SensitiveText.from("StrongPassword123!")
        repository.setupExistingVault()

        val result = repository.unlock(password)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrThrow())
    }

    @Test
    fun `unlock emits unlocking then unlocked state`() = runTest {
        val password = SensitiveText.from("StrongPassword123!")
        repository.setupExistingVault()

        repository.getSessionState().test {
            assertEquals(VaultSessionState.Uninitialized, awaitItem())

            repository.unlock(password)
            testDispatcher.scheduler.advanceUntilIdle()

            skipItems(1) // Unlocking state
            val state = awaitItem()
            assertIs<VaultSessionState.Unlocked>(state)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `lock sets session to locked`() = runTest {
        val password = SensitiveText.from("StrongPassword123!")
        repository.setupExistingVault()
        repository.unlock(password)

        val result = repository.lock()

        assertTrue(result.isSuccess)
        assertEquals(VaultSessionState.Locked(LockReason.Manual), repository.currentSessionState)
    }

    @Test
    fun `lock emits locked state`() = runTest {
        val password = SensitiveText.from("StrongPassword123!")
        repository.setupExistingVault()
        repository.unlock(password)

        repository.getSessionState().test {
            assertIs<VaultSessionState.Unlocked>(awaitItem())

            repository.lock()

            assertIs<VaultSessionState.Locking>(awaitItem())
            assertEquals(VaultSessionState.Locked(LockReason.Manual), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `get metadata returns vault info`() = runTest {
        repository.setupExistingVault(
            vaultId = VaultId("test-vault"),
            metadata = VaultMetadata(
                id = VaultId("test-vault"),
                formatVersion = 1,
                createdAt = kotlin.time.Clock.System.now(),
                lastAccessedAt = null,
                entryCount = 5
            )
        )

        val result = repository.getMetadata()

        assertTrue(result.isSuccess)
        assertEquals("test-vault", result.getOrThrow().id.value)
        assertEquals(5, result.getOrThrow().entryCount)
    }

    @Test
    fun `get metadata fails when no vault`() = runTest {
        repository.reset()

        val result = repository.getMetadata()

        assertTrue(result.isFailure)
    }

    @Test
    fun `change password succeeds`() = runTest {
        val currentPassword = SensitiveText.from("OldPassword123!")
        val newPassword = SensitiveText.from("Cedar-Lantern_92!Orbit")
        repository.setupExistingVault()
        repository.unlock(currentPassword).getOrThrow()

        val result = repository.changeMasterPassword(currentPassword, newPassword)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `change password validation precedence matches production while locked`() = runTest {
        repository.setupExistingVault()
        val invalidCurrent = SensitiveText.from("")
        val invalidNew = SensitiveText.from("short")
        val validNew = SensitiveText.from("Cedar-Lantern_92!Orbit")

        val newPasswordFailure = repository.changeMasterPassword(invalidCurrent, invalidNew)
        val currentPasswordFailure = repository.changeMasterPassword(invalidCurrent, validNew)

        assertEquals(
            "New master password does not meet policy",
            newPasswordFailure.exceptionOrNull()?.message,
        )
        assertEquals(
            "Current master password length is invalid",
            currentPasswordFailure.exceptionOrNull()?.message,
        )
    }

    @Test
    fun `session state flow emits current state`() = runTest {
        repository.setupExistingVault()

        val state = repository.getSessionState().first()

        assertEquals(VaultSessionState.Uninitialized, state)
    }

    @Test
    fun `operation failure returns failure result`() = runTest {
        repository.setShouldFail(RuntimeException("Test error"))

        val result = repository.exists()

        assertTrue(result.isFailure)
        assertEquals("Test error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `configured cancellation remains coroutine control flow and is consumed once`() = runTest {
        repository.setShouldFail(CancellationException("cancelled"))

        assertFailsWith<CancellationException> {
            repository.exists()
        }
        assertTrue(repository.exists().isSuccess)
    }

    @Test
    fun `failed lock result still revokes the fake session and retains its reason`() = runTest {
        repository.currentSessionState = VaultSessionState.Unlocked(SessionId("active"))
        repository.setShouldFail(IllegalStateException("cleanup failed"))

        val result = repository.lock(LockReason.Background)

        assertTrue(result.isFailure)
        assertEquals(
            VaultSessionState.Locked(LockReason.Background),
            repository.currentSessionState,
        )
    }

    @Test
    fun `unlock with delay allows loading state`() = runTest {
        val password = SensitiveText.from("StrongPassword123!")
        repository.setupExistingVault()
        repository.unlockDelayMillis = 100

        repository.getSessionState().test {
            assertEquals(VaultSessionState.Uninitialized, awaitItem())

            val job = launch {
                repository.unlock(password)
            }

            testDispatcher.scheduler.advanceTimeBy(50)
            val unlockingState = awaitItem()
            assertIs<VaultSessionState.Unlocking>(unlockingState)

            testDispatcher.scheduler.advanceUntilIdle()
            val unlockedState = awaitItem()
            assertIs<VaultSessionState.Unlocked>(unlockedState)

            job.join()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unlock failure returns failure result`() = runTest {
        val password = SensitiveText.from("WrongPassword123!")
        repository.setupExistingVault()
        repository.setShouldFail(IllegalStateException("Invalid password"))

        val result = repository.unlock(password)

        assertTrue(result.isFailure)
    }

    @Test
    fun `multiple unlock attempts are handled`() = runTest {
        val password = SensitiveText.from("StrongPassword123!")
        repository.setupExistingVault()

        // First unlock
        val result1 = repository.unlock(password)
        assertTrue(result1.isSuccess)

        // Lock
        repository.lock()

        // Second unlock
        val result2 = repository.unlock(password)
        assertTrue(result2.isSuccess)
    }

    @Test
    fun `reset clears all state`() = runTest {
        repository.setupExistingVault()
        repository.unlock(SensitiveText.from("password"))

        repository.reset()

        assertFalse(repository.exists().getOrThrow())
        assertEquals(VaultSessionState.Uninitialized, repository.currentSessionState)
    }

    @Test
    fun `session state survives configuration change simulation`() = runTest {
        val password = SensitiveText.from("StrongPassword123!")
        repository.setupExistingVault()
        repository.unlock(password)

        val state = repository.currentSessionState
        assertIs<VaultSessionState.Unlocked>(state)

        // Simulate by checking state is still valid
        val metadata = repository.getMetadata()
        assertTrue(metadata.isSuccess)
    }
}
