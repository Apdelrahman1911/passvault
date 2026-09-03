package com.passvault.android.lifecycle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

/** Receives screen-off only while a caller explicitly owns an observation window. */
internal interface ScreenOffObserver {
    /** Returns false when observation could not be installed and the caller must fail closed. */
    fun start(onScreenOff: () -> Unit): Boolean

    fun stop()
}

internal fun interface ScreenOffRegistrationFactory {
    fun register(onScreenOff: () -> Unit): AutoCloseable
}

internal class AndroidScreenOffObserver(
    private val registrationFactory: ScreenOffRegistrationFactory,
) : ScreenOffObserver {
    constructor(context: Context) : this(context.applicationContext.screenOffRegistrationFactory())

    private var registration: AutoCloseable? = null

    @Synchronized
    override fun start(onScreenOff: () -> Unit): Boolean {
        if (registration != null) return true
        // Report registration failure without throwing so the coordinator can deny the grace
        // without leaving system-flow accounting half-started.
        registration = try {
            registrationFactory.register(onScreenOff)
        } catch (_: Exception) {
            null
        }
        return registration != null
    }

    @Synchronized
    override fun stop() {
        val activeRegistration = registration ?: return
        registration = null
        try {
            activeRegistration.close()
        } catch (_: Exception) {
            // Treat an already-unregistered platform receiver as stopped. A stale callback remains
            // harmless because the lock policy rejects screen-off signals without an active flow.
        }
    }
}

private fun Context.screenOffRegistrationFactory(): ScreenOffRegistrationFactory =
    ScreenOffRegistrationFactory { onScreenOff ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) onScreenOff()
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        ContextCompat.registerReceiver(
            this,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        AutoCloseable {
            unregisterReceiver(receiver)
        }
    }
