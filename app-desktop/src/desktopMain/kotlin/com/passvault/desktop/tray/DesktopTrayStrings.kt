package com.passvault.desktop.tray

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.desktop_tray_exit
import com.passvault.core.designsystem.generated.resources.desktop_tray_lock
import com.passvault.core.designsystem.generated.resources.desktop_tray_show
import com.passvault.core.designsystem.generated.resources.desktop_tray_tooltip
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.getString

/** Resolve all labels against one app-published environment, not a later JVM default. */
internal suspend fun desktopTrayStrings(environment: ResourceEnvironment): DesktopTrayStrings = DesktopTrayStrings(
    tooltip = getString(environment, Res.string.desktop_tray_tooltip),
    showApp = getString(environment, Res.string.desktop_tray_show),
    lockVault = getString(environment, Res.string.desktop_tray_lock),
    exit = getString(environment, Res.string.desktop_tray_exit),
)
