package com.passvault.desktop.di

import com.passvault.desktop.security.DesktopWindowProtection
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertSame

class DesktopModuleTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `window protection is a singleton`() {
        val koin = startKoin {
            modules(desktopModule)
        }.koin

        val first = koin.get<DesktopWindowProtection>()
        val second = koin.get<DesktopWindowProtection>()

        assertSame(first, second)
    }
}
