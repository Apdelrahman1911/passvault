package com.passvault.android.di

import com.passvault.android.security.AndroidBiometricKeyStore
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricPromptController
import org.koin.core.annotation.KoinInternalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(KoinInternalApi::class)
class AndroidModuleTest {

    @Test
    fun biometricInterfacesShareTheAndroidKeyStoreSingletonDefinition() {
        val definitions = androidModule.mappings.values.toSet()
        val androidKeyStoreDefinition = definitions.single { factory ->
            factory.beanDefinition.primaryType == AndroidBiometricKeyStore::class
        }.beanDefinition
        val controllerDefinitions = definitions.filter { factory ->
            factory.beanDefinition.hasType(BiometricPromptController::class)
        }

        assertEquals(1, controllerDefinitions.size)
        assertEquals(AndroidBiometricKeyStore::class, controllerDefinitions.single().beanDefinition.primaryType)
        assertTrue(androidKeyStoreDefinition.hasType(BiometricKeyStore::class))
        assertTrue(androidKeyStoreDefinition.hasType(BiometricPromptController::class))
    }
}
