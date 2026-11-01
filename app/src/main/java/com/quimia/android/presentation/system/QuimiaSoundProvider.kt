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
import feedback.LocalQuimiaSoundStatus

@Composable
internal fun QuimiaSoundProvider(enabled: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val player = remember(context) { InterfaceSounds(context) }
    SideEffect { player.enabled = enabled }
    DisposableEffect(player) { onDispose { player.release() } }
    CompositionLocalProvider(LocalQuimiaSound provides player::play,
        LocalQuimiaSoundStatus provides player::statusMessage, content = content)
}

/** Custom app effects use media volume, independently of Android's keyboard/touch sound setting. */
internal class InterfaceSounds(context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val handler = Handler(Looper.getMainLooper())
    private val gate = InterfaceSoundGate()
    private val ready = mutableSetOf<Int>()
    private val failed = mutableSetOf<Int>()
    private val variants = InterfaceSoundVariants()
    private var released = false
    private var interactionStream = 0
    private var resultStream = 0
    private var lastPlaybackFailed = false
    var enabled: Boolean
        get() = gate.enabled
        set(value) {
            gate.enabled = value
            if (!value) {
                pool.stop(interactionStream)
                pool.stop(resultStream)
                interactionStream = 0
                resultStream = 0
            }
        }
    private val pool = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    init {
        pool.setOnLoadCompleteListener { _, id, status ->
            handler.post {
                if (!released) sounds.entries.firstOrNull { id in it.value }?.key?.let { cue ->
                    if (status == 0) {
                        ready += id
                        gate.loaded(cue, SystemClock.elapsedRealtime())?.let(::output)
                    } else failed += id
                }
            }
        }
    }
    private val sounds = mapOf(
        QuimiaSound.Tap to listOf(R.raw.ui_tap, R.raw.ui_tap_2, R.raw.ui_tap_3),
        QuimiaSound.Navigation to listOf(R.raw.ui_navigation, R.raw.ui_navigation_2, R.raw.ui_navigation_3),
        QuimiaSound.Selection to listOf(R.raw.ui_selection, R.raw.ui_selection_2, R.raw.ui_selection_3),
        QuimiaSound.Success to listOf(R.raw.ui_success, R.raw.ui_success_2),
        QuimiaSound.Error to listOf(R.raw.ui_error, R.raw.ui_error_2),
    ).mapValues { (_, resources) -> resources.map { pool.load(context, it, 1) } }
    internal fun blockReason(): InterfaceSoundBlock? = interfaceSoundBlock(enabled,
        audio.getStreamVolume(AudioManager.STREAM_MUSIC), audio.mode, notifications.currentInterruptionFilter)

    private fun permitted(): Boolean = !released && blockReason() == null

    fun statusMessage(): String? {
        blockReason()?.let { return it.message }
        if (ready.isEmpty()) return if (failed.size == sounds.values.sumOf { it.size })
            "Não foi possível carregar os efeitos sonoros. Feche e abra o app para tentar novamente."
        else "Os efeitos ainda estão sendo preparados. Aguarde um instante e toque na prévia novamente."
        if (lastPlaybackFailed) {
            lastPlaybackFailed = false // Report this attempt once, allowing the next preview to retry playback.
            return "O Android não conseguiu reproduzir o efeito. Confira a saída de áudio selecionada e tente novamente."
        }
        return null
    }

    fun play(cue: QuimiaSound) {
        if (permitted()) gate.request(cue, SystemClock.elapsedRealtime())?.let(::output)
    }
    private fun output(cue: QuimiaSound) {
        if (!permitted()) return
        val sample = variants.next(cue, sounds.getValue(cue).filter { it in ready }) ?: return
        val mix = cue.mix()
        // Cut the previous interaction so a quick succession of controls doesn't turn into a pile of clicks.
        pool.stop(interactionStream)
        interactionStream = 0
        if (mix.priority > 1) {
            pool.stop(resultStream)
            resultStream = pool.play(sample, mix.volume, mix.volume, mix.priority, 0, 1f)
            lastPlaybackFailed = resultStream == 0
        } else {
            interactionStream = pool.play(sample, mix.volume, mix.volume, mix.priority, 0, 1f)
            lastPlaybackFailed = interactionStream == 0
        }
    }
    fun release() {
        released = true; enabled = false
        handler.removeCallbacksAndMessages(null)
        pool.setOnLoadCompleteListener(null)
        pool.release()
    }
}
