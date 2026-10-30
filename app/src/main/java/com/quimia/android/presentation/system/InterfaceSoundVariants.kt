package com.quimia.android.presentation.system

import feedback.QuimiaSound
import kotlin.random.Random

/** Picks only decoded samples, without repeating the last texture for the same cue. */
internal class InterfaceSoundVariants(private val choose: (Int) -> Int = Random::nextInt) {
    private val previous = mutableMapOf<QuimiaSound, Int>()

    fun next(cue: QuimiaSound, available: List<Int>): Int? {
        if (available.isEmpty()) return null
        val candidates = available.filter { it != previous[cue] }.ifEmpty { available }
        return candidates[choose(candidates.size)].also { previous[cue] = it }
    }
}
