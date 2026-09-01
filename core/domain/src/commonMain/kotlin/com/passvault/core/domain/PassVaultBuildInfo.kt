package com.passvault.core.domain

/**
 * User-visible application version shared by every platform.
 *
 * Release validation enforces that this value matches `VERSION_NAME` in
 * `version.properties`, preventing platform titles and settings from drifting.
 */
object PassVaultBuildInfo {
    const val VERSION: String = "1.0.7"
}
