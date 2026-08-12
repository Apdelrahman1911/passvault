package com.passvault.shared.security

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/** Records pointer activity without reading or writing Compose snapshot state. */
internal fun Modifier.recordUserActivity(signal: UserActivitySignal): Modifier =
    pointerInput(signal) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent()
                signal.recordActivity()
            }
        }
    }
