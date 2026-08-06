package com.passvault.core.testing.fakes

import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricType
import com.passvault.core.security.BiometricUnlockService
import com.passvault.core.security.BiometricUnlockStatus

class FakeBiometricUnlockService : BiometricUnlockService {
    private var status = BiometricUnlockStatus(
        capability = BiometricCapability(
            type = BiometricType.GENERIC,
            availability = BiometricAvailability.UNAVAILABLE,
        ),
        isEnabled = false,
    )
    private var enableResult: BiometricOperationResult = BiometricOperationResult.Success
    private var disableResult: BiometricOperationResult = BiometricOperationResult.Success
    private var unlockResult: BiometricOperationResult = BiometricOperationResult.Success

    var enableCalls: Int = 0
        private set
    var disableCalls: Int = 0
        private set
    var unlockCalls: Int = 0
        private set

    override suspend fun getStatus(): BiometricUnlockStatus = status

    override suspend fun enable(): BiometricOperationResult {
        enableCalls++
        if (enableResult == BiometricOperationResult.Success) status = status.copy(isEnabled = true)
        return enableResult
    }

    override suspend fun disable(): BiometricOperationResult {
        disableCalls++
        if (disableResult == BiometricOperationResult.Success) status = status.copy(isEnabled = false)
        return disableResult
    }

    override suspend fun unlock(): BiometricOperationResult {
        unlockCalls++
        return unlockResult
    }

    fun setStatus(
        availability: BiometricAvailability,
        enabled: Boolean,
        type: BiometricType = BiometricType.GENERIC,
    ) {
        status = BiometricUnlockStatus(
            capability = BiometricCapability(type, availability),
            isEnabled = enabled,
        )
    }

    fun setEnableResult(result: BiometricOperationResult) {
        enableResult = result
    }

    fun setDisableResult(result: BiometricOperationResult) {
        disableResult = result
    }

    fun setUnlockResult(result: BiometricOperationResult) {
        unlockResult = result
    }
}
