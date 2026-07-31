package com.passvault.desktop.di

import com.passvault.core.security.WindowProtection
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
    fun `window protection resolves by concrete and shared types`() {
        val koin = startKoin {
            modules(desktopModule)
        }.koin

        val concrete = koin.get<DesktopWindowProtection>()
        val shared = koin.get<WindowProtection>()

        assertSame(concrete, shared)
    }
}
