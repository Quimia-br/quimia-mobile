package com.quimia.android.presentation.system

import feedback.QuimiaSound

/** Keeps a first interaction while SoundPool decodes; old interactions are never replayed later. */
internal class InterfaceSoundGate {
    private val ready = mutableSetOf<QuimiaSound>()
    private var pending: Pair<QuimiaSound, Long>? = null
    private val lastPlayed = mutableMapOf<QuimiaSound, Long>()
    var enabled: Boolean = true
        set(value) { field = value; if (!value) pending = null }

    fun request(cue: QuimiaSound, now: Long): QuimiaSound? {
        if (!enabled) return null
        if (lastPlayed[cue]?.let { now - it < cue.mix().minimumInterval } == true) return null
        if (cue !in ready) {
            if (pending == null || cue.mix().priority >= pending!!.first.mix().priority) pending = cue to now
            return null
        }
        lastPlayed[cue] = now
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
