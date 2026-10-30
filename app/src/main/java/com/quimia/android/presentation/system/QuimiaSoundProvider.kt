package com.quimia.android.presentation.system

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
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
    DisposableEffect(player) { onDispose { player.release() } }
    CompositionLocalProvider(LocalQuimiaSound provides { sound -> if (enabled) player.play(sound) }, content = content)
}

/** Short original cues. No playback in silent/vibrate, during calls, or when system effects are off. */
private class InterfaceSounds(context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val resolver = context.contentResolver
    private val pool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private val ready = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()
    private var lastPlayed = 0L
    init { pool.setOnLoadCompleteListener { _, id, status -> if (status == 0) ready.add(id) } }
    private val sounds = mapOf(
        QuimiaSound.Tap to pool.load(context, R.raw.ui_tap, 1),
        QuimiaSound.Success to pool.load(context, R.raw.ui_success, 1),
        QuimiaSound.Error to pool.load(context, R.raw.ui_error, 1),
    )
    fun play(sound: QuimiaSound) {
        if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL || audio.mode != AudioManager.MODE_NORMAL ||
            android.provider.Settings.System.getInt(resolver, android.provider.Settings.System.SOUND_EFFECTS_ENABLED, 1) == 0) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastPlayed < 180L) return
        val id = sounds[sound]?.takeIf(ready::contains) ?: return
        lastPlayed = now
        val volume = if (sound == QuimiaSound.Tap) .15f else .22f
        pool.play(id, volume, volume, 1, 0, 1f)
    }
    fun release() { pool.release() }
}
