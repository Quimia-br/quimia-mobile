package com.quimia.android.presentation.system

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.quimia.android.R
import feedback.LocalQuimiaSound
import feedback.QuimiaSound

@Composable
internal fun QuimiaSoundProvider(enabled: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val player = remember(context) { InterfaceSounds(context) }
    SideEffect { player.enabled = enabled }
    DisposableEffect(player) { onDispose { player.release() } }
    CompositionLocalProvider(LocalQuimiaSound provides player::play, content = content)
}

/** Custom app effects use media volume, independently of Android's keyboard/touch sound setting. */
private class InterfaceSounds(context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val handler = Handler(Looper.getMainLooper())
    private val gate = InterfaceSoundGate()
    private val ready = mutableSetOf<Int>()
    private val variants = InterfaceSoundVariants()
    private var released = false
    var enabled: Boolean
        get() = gate.enabled
        set(value) { gate.enabled = value }
    private val pool = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    init {
        pool.setOnLoadCompleteListener { _, id, status ->
            if (status == 0) handler.post {
                if (!released) sounds.entries.firstOrNull { id in it.value }?.key?.let { cue ->
                    ready += id
                    gate.loaded(cue, SystemClock.elapsedRealtime())?.let(::output)
                }
            }
        }
    }
    private val sounds = mapOf(
        QuimiaSound.Tap to listOf(R.raw.ui_tap, R.raw.ui_tap_2, R.raw.ui_tap_3),
        QuimiaSound.Selection to listOf(R.raw.ui_selection, R.raw.ui_selection_2, R.raw.ui_selection_3),
        QuimiaSound.Success to listOf(R.raw.ui_success, R.raw.ui_success_2),
        QuimiaSound.Error to listOf(R.raw.ui_error, R.raw.ui_error_2),
    ).mapValues { (_, resources) -> resources.map { pool.load(context, it, 1) } }
    private fun permitted(): Boolean = !released && enabled && audio.ringerMode == AudioManager.RINGER_MODE_NORMAL &&
        audio.mode == AudioManager.MODE_NORMAL && audio.getStreamVolume(AudioManager.STREAM_MUSIC) > 0 &&
        notifications.currentInterruptionFilter in setOf(NotificationManager.INTERRUPTION_FILTER_ALL, NotificationManager.INTERRUPTION_FILTER_UNKNOWN)

    fun play(cue: QuimiaSound) {
        if (permitted()) gate.request(cue, SystemClock.elapsedRealtime())?.let(::output)
    }
    private fun output(cue: QuimiaSound) {
        if (!permitted()) return
        val sample = variants.next(cue, sounds.getValue(cue).filter { it in ready }) ?: return
        val volume = when (cue) {
            QuimiaSound.Tap -> .55f
            QuimiaSound.Selection -> .45f
            QuimiaSound.Success -> .65f
            QuimiaSound.Error -> .60f
        }
        pool.play(sample, volume, volume, if (cue == QuimiaSound.Tap) 1 else 2, 0, 1f)
    }
    fun release() {
        released = true; enabled = false
        handler.removeCallbacksAndMessages(null)
        pool.setOnLoadCompleteListener(null)
        pool.release()
    }
}
