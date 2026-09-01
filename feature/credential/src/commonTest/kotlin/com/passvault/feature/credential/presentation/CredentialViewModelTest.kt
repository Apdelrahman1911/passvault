package com.passvault.feature.credential.presentation

import app.cash.turbine.test
import com.passvault.core.crypto.SecurePasswordGenerator
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordHistoryEntry
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.core.domain.model.TotpConfiguration
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.core.testing.fakes.FakeFolderRepository
import com.passvault.core.testing.fakes.FakeCryptoEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialViewModelTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var repository: FakeCredentialRepository
    private lateinit var folderRepository: FakeFolderRepository

    @BeforeTest
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        repository = FakeCredentialRepository()
        folderRepository = FakeFolderRepository()
    }

    @AfterTest
    fun tearDown() {
        repository.reset()
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `loading a credential preserves every sensitive field`() = runTest(dispatcher) {
        val source = TestData.credential(
            id = "full",
            title = "Full record",
            username = "user",
            email = "user@example.com",
            password = "secret",
            folderId = "work",
            tagIds = setOf("important"),
        ).copy(
            notes = SensitiveText.from("private note"),
            recoveryCodes = listOf(SensitiveText.from("recovery-1")),
            apiKeys = listOf(SensitiveText.from("api-key")),
            licenseKeys = listOf(SensitiveText.from("license-key")),
            customFields = listOf(
                CustomField(
                    id = CustomFieldId("field"),
                    name = "Account number",
                    value = SensitiveText.from("1234"),
                    isSecret = true,
                ),
            ),
            passwordHistory = listOf(
                PasswordHistoryEntry(
                    password = SensitiveText.from("old-secret"),
                    changedAt = TestData.now,
                ),
            ),
            isFavorite = true,
            passwordHealth = PasswordHealth(
                score = PasswordScore.WEAK,
                isDuplicate = true,
                isWeak = true,
                isOld = false,
                ageDays = 12,
            ),
        )
        repository.setupCredentials(source)

        val viewModel = createViewModel()
        viewModel.loadCredential(source.id)
        runCurrent()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Full record", state.title)
        assertEquals("user", state.username)
        assertEquals("user@example.com", state.email)
        assertEquals("secret", state.password)
        assertEquals("private note", state.notes)
        assertEquals("recovery-1", state.recoveryCodes.single().toStringUnsafe())
        assertEquals("api-key", state.apiKeys.single().toStringUnsafe())
        assertEquals("license-key", state.licenseKeys.single().toStringUnsafe())
        assertEquals("1234", state.customFields.single().value.toStringUnsafe())
        assertTrue(state.isFavorite)
        assertEquals(source.passwordHealth, state.passwordHealth)

        // The ViewModel must clear only its own copies, not mutate a fake or
        // repository-owned record returned by the data layer.
        val stored = repository.getAllCredentials().single()
        assertEquals("secret", stored.password?.toStringUnsafe())
        assertEquals("old-secret", stored.passwordHistory.single().password.toStringUnsafe())
    }

    @Test
    fun `new credential loads folders and defaults to no folder`() = runTest(dispatcher) {
        val workFolder = TestData.folder(id = "work", name = "Work")
        folderRepository.setupFolders(workFolder)
        val viewModel = createViewModel()

        viewModel.createNewCredential(CredentialType.Login)
        runCurrent()

        assertEquals(listOf(workFolder), viewModel.state.value.folders)
        assertEquals(null, viewModel.state.value.folderId)

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Unfiled"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        assertEquals(null, repository.getAllCredentials().single().folderId)
    }

    @Test
    fun `new credential can be saved in a selected folder`() = runTest(dispatcher) {
        val workFolder = TestData.folder(id = "work", name = "Work")
        folderRepository.setupFolders(workFolder)
        val viewModel = createViewModel()

        viewModel.createNewCredential(CredentialType.Login)
        runCurrent()
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnFolderChanged(workFolder.id.value),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Work account"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        assertEquals(workFolder.id, repository.getAllCredentials().single().folderId)
    }

    @Test
    fun `editing a credential moves it to another folder`() = runTest(dispatcher) {
        val oldFolder = TestData.folder(id = "old", name = "Old")
        val newFolder = TestData.folder(id = "new", name = "New", sortOrder = 1)
        val credential = TestData.credential(id = "account", folderId = oldFolder.id.value)
        folderRepository.setupFolders(oldFolder, newFolder)
        repository.setupCredentials(credential)
        val viewModel = createViewModel()

        viewModel.loadCredential(credential.id)
        runCurrent()
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnFolderChanged(newFolder.id.value),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        assertEquals(FolderId("new"), repository.getAllCredentials().single().folderId)
    }

    @Test
    fun `invalid title and url are rejected before save`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(com.passvault.core.domain.model.CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged(""))
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnUrlChanged(0, "javascript:alert(1)"),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        assertEquals(
            Res.string.validation_credential_title_required,
            (viewModel.state.value.titleError as UiText.Resource).resource,
        )
        assertEquals(
            Res.string.validation_credential_url_invalid,
            (viewModel.state.value.urlErrors[0] as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.isSaving)
        assertEquals(0, repository.getCredentialCount())
    }

    @Test
    fun `repeated save submits persist one credential and emit one completion`() = runTest(dispatcher) {
        repository.setOperationDelay(100)
        val viewModel = createViewModel()
        viewModel.createNewCredential(com.passvault.core.domain.model.CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("New"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnPasswordChanged("StrongPassword123!"))

        viewModel.effect.test {
            repeat(3) {
                viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
            }
            runCurrent()
            assertTrue(viewModel.state.value.isSaving)
            advanceTimeBy(100)
            runCurrent()

            assertIs<CredentialViewModel.CredentialEffect.SaveCompleted>(awaitItem())
            expectNoEvents()
            assertEquals(1, repository.getCredentialCount())
            assertFalse(viewModel.state.value.isSaving)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `editable state cannot change while a save is in progress`() = runTest(dispatcher) {
        repository.setOperationDelay(100)
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Original"))

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Too late"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnFavoriteChanged(true))
        runCurrent()
        advanceTimeBy(100)
        runCurrent()

        val stored = repository.getAllCredentials().single()
        assertEquals("Original", stored.title)
        assertFalse(stored.isFavorite)
        assertEquals("Original", viewModel.state.value.title)
        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `clear for lock cancels pending save and clears all sensitive state`() = runTest(dispatcher) {
        repository.setOperationDelay(1_000)
        val viewModel = createViewModel()
        viewModel.createNewCredential(com.passvault.core.domain.model.CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Pending"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnPasswordChanged("sensitive"))
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged("JBSWY3DPEHPK3PXP"),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        viewModel.clearForLock()
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals("", viewModel.state.value.password)
        assertEquals(null, viewModel.state.value.totpConfiguration)
        assertEquals("", viewModel.state.value.currentTotpCode)
        assertEquals(0, repository.getCredentialCount())
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `copy and launch events are explicit and normalize safe urls`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(com.passvault.core.domain.model.CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUsernameChanged("ada"))

        viewModel.effect.test {
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnLaunchUrlClick("example.com"))
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyUsernameClick)
            assertEquals(
                CredentialViewModel.CredentialEffect.LaunchUrl("https://example.com"),
                awaitItem(),
            )
            assertEquals(
                CredentialViewModel.CredentialEffect.CopyToClipboard("ada"),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successful copy and url launch record the latest credential use`() = runTest(dispatcher) {
        val firstUse = Instant.fromEpochSeconds(1_700_000_100)
        val secondUse = Instant.fromEpochSeconds(1_700_000_200)
        val clock = MutableClock(firstUse)
        val credential = TestData.credential(id = "used")
        repository.setupCredentials(credential)
        val viewModel = createViewModel(clock)
        viewModel.loadCredential(credential.id)
        runCurrent()

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyResult(succeeded = true))
        runCurrent()
        assertEquals(firstUse, repository.getAllCredentials().single().lastUsedAt)
        assertEquals(firstUse, viewModel.state.value.lastUsedAt)

        clock.instant = secondUse
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUrlLaunchResult(succeeded = true))
        runCurrent()
        assertEquals(secondUse, repository.getAllCredentials().single().lastUsedAt)
        assertEquals(secondUse, viewModel.state.value.lastUsedAt)
    }

    @Test
    fun `failed copy is reported and does not record credential use`() = runTest(dispatcher) {
        val credential = TestData.credential(id = "copy-failure")
        repository.setupCredentials(credential)
        val viewModel = createViewModel()
        viewModel.loadCredential(credential.id)
        runCurrent()

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyResult(succeeded = false))
        runCurrent()

        assertEquals(
            Res.string.error_credential_copy,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
        assertEquals(null, repository.getAllCredentials().single().lastUsedAt)
    }

    @Test
    fun `successful save preserves creation timestamp in editable state`() = runTest(dispatcher) {
        val now = Instant.fromEpochSeconds(1_700_000_300)
        val viewModel = createViewModel(FixedClock(now))
        viewModel.createNewCredential(CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Created"))

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        assertEquals(now, viewModel.state.value.createdAt)
        assertEquals(now, viewModel.state.value.updatedAt)
    }

    @Test
    fun `replacing and removing custom fields clears superseded sensitive values`() =
        runTest(dispatcher) {
            val viewModel = createViewModel()
            viewModel.createNewCredential(CredentialType.SecureNote)
            viewModel.onEvent(
                CredentialViewModel.CredentialEvent.OnCustomFieldAdded(
                    name = "First",
                    value = "old-secret",
                    isSecret = true,
                ),
            )
            val field = viewModel.state.value.customFields.single()
            val originalValue = field.value

            viewModel.onEvent(
                CredentialViewModel.CredentialEvent.OnCustomFieldUpdated(
                    fieldId = field.id,
                    name = "Second",
                    value = "new-secret",
                    isSecret = true,
                ),
            )

            assertTrue(originalValue.toStringUnsafe().all { it == '\u0000' })
            val replacement = viewModel.state.value.customFields.single().value
            assertEquals("new-secret", replacement.toStringUnsafe())

            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCustomFieldRemoved(field.id))

            assertTrue(replacement.toStringUnsafe().all { it == '\u0000' })
            assertTrue(viewModel.state.value.customFields.isEmpty())
        }

    @Test
    fun `generate action fills password without navigation effect`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)

        viewModel.effect.test {
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnGeneratePasswordClick)
            runCurrent()

            expectNoEvents()
            assertEquals(16, viewModel.state.value.password.length)
            assertTrue(viewModel.state.value.isDirty)
            assertFalse(viewModel.state.value.isGeneratingPassword)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `back closes delete confirmation before navigating`() = runTest(dispatcher) {
        val credential = TestData.credential(id = "delete-dialog")
        repository.setupCredentials(credential)
        val viewModel = createViewModel()
        viewModel.loadCredential(credential.id)
        runCurrent()
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDeleteClick)

        viewModel.effect.test {
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnBackClick)
            assertFalse(viewModel.state.value.showDeleteConfirmation)
            expectNoEvents()

            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnBackClick)
            assertEquals(CredentialViewModel.CredentialEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `back from an edited existing credential requires discard confirmation`() = runTest(dispatcher) {
        val credential = TestData.credential(id = "edited")
        repository.setupCredentials(credential)
        val viewModel = createViewModel()
        viewModel.loadCredential(credential.id)
        runCurrent()
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Changed"))

        viewModel.effect.test {
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnBackClick)
            assertTrue(viewModel.state.value.showDiscardConfirmation)
            expectNoEvents()

            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDiscardConfirm)
            assertEquals(CredentialViewModel.CredentialEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failed load cannot be overwritten or deleted`() = runTest(dispatcher) {
        val credential = TestData.credential(id = "protected")
        repository.setupCredentials(credential)
        repository.setShouldFail()
        val viewModel = createViewModel()

        viewModel.loadCredential(credential.id)
        runCurrent()
        assertFalse(viewModel.state.value.isCredentialLoaded)
        assertFalse(viewModel.state.value.canSave)

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Replacement"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDeleteClick)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDeleteConfirm)
        runCurrent()

        assertFalse(viewModel.state.value.showDeleteConfirmation)
        assertEquals("Test Credential", repository.getAllCredentials().single().title)
    }

    @Test
    fun `sensitive collection copies require an existing index`() = runTest(dispatcher) {
        val credential = TestData.credential(id = "collections").copy(
            recoveryCodes = listOf(SensitiveText.from("recovery")),
            apiKeys = listOf(SensitiveText.from("api")),
            licenseKeys = listOf(SensitiveText.from("license")),
        )
        repository.setupCredentials(credential)
        credential.recoveryCodes.forEach(SensitiveText::clear)
        credential.apiKeys.forEach(SensitiveText::clear)
        credential.licenseKeys.forEach(SensitiveText::clear)
        val viewModel = createViewModel()
        viewModel.loadCredential(credential.id)
        runCurrent()

        viewModel.effect.test {
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyRecoveryCodeClick(0))
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyApiKeyClick(0))
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyLicenseKeyClick(0))
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyRecoveryCodeClick(1))

            assertEquals(CredentialViewModel.CredentialEffect.CopyToClipboard("recovery"), awaitItem())
            assertEquals(CredentialViewModel.CredentialEffect.CopyToClipboard("api"), awaitItem())
            assertEquals(CredentialViewModel.CredentialEffect.CopyToClipboard("license"), awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `text and collection inputs are bounded before entering state`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("t".repeat(500)))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUsernameChanged("u".repeat(5_000)))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnEmailChanged("e".repeat(5_000)))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnPasswordChanged("p".repeat(5_000)))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnNotesChanged("n".repeat(100_500)))
        repeat(105) {
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUrlAdded("https://example.com/$it"))
        }

        val state = viewModel.state.value
        assertEquals(201, state.title.length)
        assertEquals(4_097, state.username.length)
        assertEquals(4_097, state.email.length)
        assertEquals(4_097, state.password.length)
        assertEquals(100_001, state.notes.length)
        assertEquals(100, state.urls.size)
    }

    @Test
    fun `text boundaries preserve complete supplementary characters`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("🔐".repeat(500)))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnPasswordChanged("🔐".repeat(5_000)))

        assertEquals(201, viewModel.state.value.title.codePointLength())
        assertEquals("🔐".repeat(201), viewModel.state.value.title)
        assertEquals(4_097, viewModel.state.value.password.codePointLength())
        assertEquals("🔐".repeat(4_097), viewModel.state.value.password)
    }

    @Test
    fun `supplementary characters count as one character for strength feedback`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnPasswordChanged("🔐🔑🔒🔓"))

        assertEquals(CredentialViewModel.PasswordStrength.TOO_SHORT, viewModel.state.value.passwordStrength)
    }

    @Test
    fun `TOTP setup generates a live code copies it and persists with the login`() =
        runTest(dispatcher) {
            val clock = FixedClock(Instant.fromEpochSeconds(59))
            val viewModel = createViewModel(clock)
            viewModel.createNewCredential(CredentialType.Login)
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Example"))
            viewModel.onEvent(
                CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged(
                    "otpauth://totp/Example:alice?" +
                        "secret=GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ&digits=8",
                ),
            )
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick)
            runCurrent()

            assertEquals("94287082", viewModel.state.value.currentTotpCode)
            assertEquals("Example", viewModel.state.value.totpConfiguration?.issuer)
            assertEquals("alice", viewModel.state.value.totpConfiguration?.accountName)

            viewModel.effect.test {
                viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyTotpClick)
                assertEquals(
                    CredentialViewModel.CredentialEffect.CopyToClipboard("94287082"),
                    awaitItem(),
                )
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
            runCurrent()
            val stored = repository.getAllCredentials().single()
            assertEquals(TotpAlgorithm.SHA1, stored.totp?.algorithm)
            assertEquals(8, stored.totp?.digits)
            assertEquals(
                "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ",
                stored.totp?.secret?.toStringUnsafe(),
            )
            viewModel.clearForLock()
        }

    @Test
    fun `scanned TOTP URI ignores empty and conflicting manual setup values`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAlgorithmChanged(TotpAlgorithm.SHA512))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpDigitsChanged(8))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpPeriodChanged(""))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpScanClick)

        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnTotpQrScanned(
                "otpauth://totp/Example:alice?secret=GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ" +
                    "&algorithm=SHA256&digits=6&period=60",
            ),
        )

        val configuration = viewModel.state.value.totpConfiguration
        assertEquals(TotpAlgorithm.SHA256, configuration?.algorithm)
        assertEquals(6, configuration?.digits)
        assertEquals(60, configuration?.periodSeconds)
        assertFalse(viewModel.state.value.showTotpScanner)
        assertEquals(null, viewModel.state.value.totpSetupError)
        viewModel.clearForLock()
    }

    @Test
    fun `manual TOTP setup rejects an empty period`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged("JBSWY3DPEHPK3PXP"),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpPeriodChanged(""))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick)

        assertEquals(null, viewModel.state.value.totpConfiguration)
        assertEquals(
            Res.string.error_totp_invalid_setup,
            (viewModel.state.value.totpSetupError as UiText.Resource).resource,
        )
        viewModel.clearForLock()
    }

    @Test
    fun `replacing and removing TOTP require confirmation`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged(
                "JBSWY3DPEHPK3PXP",
            ),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick)
        val original = viewModel.state.value.totpConfiguration?.secret?.toStringUnsafe()

        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged(
                "KRUGS4ZANFZSAYJA",
            ),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick)
        assertTrue(viewModel.state.value.showTotpReplaceConfirmation)
        assertEquals(original, viewModel.state.value.totpConfiguration?.secret?.toStringUnsafe())

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpReplaceConfirm)
        assertEquals(
            "KRUGS4ZANFZSAYJA",
            viewModel.state.value.totpConfiguration?.secret?.toStringUnsafe(),
        )

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpRemoveClick)
        assertTrue(viewModel.state.value.showTotpRemoveConfirmation)
        assertTrue(viewModel.state.value.totpConfiguration != null)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpRemoveConfirm)
        assertEquals(null, viewModel.state.value.totpConfiguration)
        viewModel.clearForLock()
    }

    @Test
    fun `canceling a staged TOTP replacement clears the new secret without dirtying the credential`() =
        runTest(dispatcher) {
            val sourceTotp = TotpConfiguration(secret = SensitiveText.from("JBSWY3DPEHPK3PXP"))
            val credential = TestData.credential(id = "totp-cancel").copy(totp = sourceTotp)
            repository.setupCredentials(credential)
            sourceTotp.clear()
            val viewModel = createViewModel()
            viewModel.loadCredential(credential.id)
            runCurrent()

            viewModel.onEvent(
                CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged("KRUGS4ZANFZSAYJA"),
            )
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick)
            val pending = viewModel.state.value.pendingTotpConfiguration
            assertTrue(viewModel.state.value.showTotpReplaceConfirmation)
            assertFalse(viewModel.state.value.isDirty)

            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpReplaceCancel)

            assertEquals(null, viewModel.state.value.pendingTotpConfiguration)
            assertEquals("", viewModel.state.value.totpSetupInput)
            assertFalse(viewModel.state.value.isDirty)
            assertTrue(pending?.secret?.toStringUnsafe()?.all { it == '\u0000' } == true)
            viewModel.clearForLock()
        }

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private class MutableClock(var instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private fun createViewModel(clock: Clock = Clock.System): CredentialViewModel =
        CredentialViewModel(
            credentialRepository = repository,
            folderRepository = folderRepository,
            passwordGenerator = SecurePasswordGenerator(FakeCryptoEngine()),
            clock = clock,
        )
}
