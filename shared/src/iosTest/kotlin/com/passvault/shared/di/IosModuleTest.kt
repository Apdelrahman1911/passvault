package com.passvault.shared.di

import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricPromptController
import com.passvault.shared.platform.IosBiometricKeyStore
import org.koin.core.annotation.KoinInternalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(KoinInternalApi::class)
class IosModuleTest {

    @Test
    fun biometricInterfacesShareTheIosKeyStoreSingletonDefinition() {
        val definitions = iosModule.mappings.values.toSet()
        val iosKeyStoreDefinition = definitions.single { factory ->
            factory.beanDefinition.primaryType == IosBiometricKeyStore::class
        }.beanDefinition
        val controllerDefinitions = definitions.filter { factory ->
            factory.beanDefinition.hasType(BiometricPromptController::class)
        }

        assertEquals(1, controllerDefinitions.size)
        assertEquals(IosBiometricKeyStore::class, controllerDefinitions.single().beanDefinition.primaryType)
        assertTrue(iosKeyStoreDefinition.hasType(BiometricKeyStore::class))
        assertTrue(iosKeyStoreDefinition.hasType(BiometricPromptController::class))
    }
}
