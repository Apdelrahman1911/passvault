package com.passvault.desktop

import kotlin.test.Test
import kotlin.test.assertEquals

class OperatingSystemTest {

    @Test
    fun recognizesSupportedJvmOperatingSystemNames() {
        assertEquals(OperatingSystem.MACOS, operatingSystemFromName("Mac OS X"))
        assertEquals(OperatingSystem.MACOS, operatingSystemFromName("Darwin"))
        assertEquals(OperatingSystem.WINDOWS, operatingSystemFromName("Windows 11"))
        assertEquals(OperatingSystem.LINUX, operatingSystemFromName("Linux"))
        assertEquals(OperatingSystem.LINUX, operatingSystemFromName("AIX"))
    }

    @Test
    fun unknownOrAbsentOperatingSystemNamesStayUnknown() {
        assertEquals(OperatingSystem.UNKNOWN, operatingSystemFromName("FreeBSD"))
        assertEquals(OperatingSystem.UNKNOWN, operatingSystemFromName(""))
    }
}
