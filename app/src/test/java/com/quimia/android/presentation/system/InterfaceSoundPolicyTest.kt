package com.quimia.android.presentation.system

import android.app.NotificationManager
import android.media.AudioManager
import org.junit.Assert.*
import org.junit.Test

class InterfaceSoundPolicyTest {
    @Test fun enabledEffectsWithMediaVolumeCanPlay() {
        assertNull(interfaceSoundBlock(true, 4, AudioManager.MODE_NORMAL, NotificationManager.INTERRUPTION_FILTER_ALL))
        assertNull(interfaceSoundBlock(true, 4, AudioManager.MODE_NORMAL, NotificationManager.INTERRUPTION_FILTER_UNKNOWN))
    }
    @Test fun userOptOutIsStillRespected() {
        assertEquals(InterfaceSoundBlock.Disabled,
            interfaceSoundBlock(false, 4, AudioManager.MODE_NORMAL, NotificationManager.INTERRUPTION_FILTER_ALL))
    }
    @Test fun zeroMediaVolumeProducesAnActionableExplanation() {
        assertEquals(InterfaceSoundBlock.Muted,
            interfaceSoundBlock(true, 0, AudioManager.MODE_NORMAL, NotificationManager.INTERRUPTION_FILTER_ALL))
    }
    @Test fun callsAndAudioConversationsPauseEffects() {
        for (mode in listOf(AudioManager.MODE_IN_CALL, AudioManager.MODE_IN_COMMUNICATION)) {
            assertEquals(InterfaceSoundBlock.Call,
                interfaceSoundBlock(true, 4, mode, NotificationManager.INTERRUPTION_FILTER_ALL))
        }
    }
    @Test fun doNotDisturbIsNotBypassed() {
        for (filter in listOf(NotificationManager.INTERRUPTION_FILTER_NONE, NotificationManager.INTERRUPTION_FILTER_PRIORITY, NotificationManager.INTERRUPTION_FILTER_ALARMS)) {
            assertEquals(InterfaceSoundBlock.DoNotDisturb,
                interfaceSoundBlock(true, 4, AudioManager.MODE_NORMAL, filter))
        }
    }
}
