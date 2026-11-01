package com.quimia.android.presentation.system

import android.app.NotificationManager
import android.media.AudioManager

internal enum class InterfaceSoundBlock(val message: String) {
    Disabled("Ative os efeitos sonoros para ouvir a prévia."),
    Muted("O volume de mídia está zerado. Aumente o volume de mídia do aparelho e tente novamente."),
    Call("Os efeitos estão pausados durante uma chamada ou conversa por áudio."),
    DoNotDisturb("O modo Não perturbe está ativo. Desative-o para testar os efeitos."),
}

/** Ringer mode controls calls, not these opt-in media effects. Never raise the system volume here. */
internal fun interfaceSoundBlock(enabled: Boolean, mediaVolume: Int, audioMode: Int, interruptionFilter: Int): InterfaceSoundBlock? = when {
    !enabled -> InterfaceSoundBlock.Disabled
    mediaVolume <= 0 -> InterfaceSoundBlock.Muted
    audioMode != AudioManager.MODE_NORMAL -> InterfaceSoundBlock.Call
    interruptionFilter !in setOf(NotificationManager.INTERRUPTION_FILTER_ALL, NotificationManager.INTERRUPTION_FILTER_UNKNOWN) -> InterfaceSoundBlock.DoNotDisturb
    else -> null
}
