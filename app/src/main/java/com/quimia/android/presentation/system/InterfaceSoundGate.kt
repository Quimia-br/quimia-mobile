package com.quimia.android.presentation.system

import feedback.QuimiaSound

/** Keeps a first interaction while SoundPool decodes; old interactions are never replayed later. */
internal class InterfaceSoundGate {
    private val ready = mutableSetOf<QuimiaSound>()
    private var pending: Pair<QuimiaSound, Long>? = null
    private var lastTap: Long? = null
    var enabled: Boolean = true
        set(value) { field = value; if (!value) pending = null }

    fun request(cue: QuimiaSound, now: Long): QuimiaSound? {
        if (!enabled) return null
        if (cue == QuimiaSound.Tap && lastTap?.let { now - it < 70 } == true) return null
        if (cue !in ready) {
            if (pending == null || cue != QuimiaSound.Tap || pending?.first == QuimiaSound.Tap) pending = cue to now
            return null
        }
        if (cue == QuimiaSound.Tap) lastTap = now
        return cue
    }

    fun loaded(cue: QuimiaSound, now: Long): QuimiaSound? {
        ready.add(cue)
        val queued = pending ?: return null
        if (queued.first != cue) return null
        pending = null
        return if (now - queued.second in 0..350) request(cue, now) else null
    }
}
