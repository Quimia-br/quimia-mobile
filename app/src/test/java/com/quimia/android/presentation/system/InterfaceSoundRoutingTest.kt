package com.quimia.android.presentation.system

import android.content.Context
import android.media.AudioManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InterfaceSoundRoutingTest {
    @Test fun vibratingOrSilencingCallsDoesNotBlockTheAppsMediaEffects() {
        val context: Context = RuntimeEnvironment.getApplication()
        val audio = context.getSystemService(AudioManager::class.java)
        audio.mode = AudioManager.MODE_NORMAL
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0)
        val player = InterfaceSounds(context)
        try {
            for (mode in listOf(AudioManager.RINGER_MODE_NORMAL, AudioManager.RINGER_MODE_VIBRATE, AudioManager.RINGER_MODE_SILENT)) {
                audio.ringerMode = mode
                assertNull("Ringer mode $mode must not mute media effects", player.blockReason())
            }
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            assertEquals(InterfaceSoundBlock.Muted, player.blockReason())
        } finally { player.release() }
    }
}
