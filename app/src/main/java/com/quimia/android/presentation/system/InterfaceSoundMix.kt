package com.quimia.android.presentation.system

import feedback.QuimiaSound

/** Levels match the mastered assets; short interactions stay quieter than operation results. */
internal data class InterfaceSoundMix(val volume: Float, val minimumInterval: Long, val priority: Int)

internal fun QuimiaSound.mix() = when (this) {
    QuimiaSound.Tap -> InterfaceSoundMix(.60f, 100, 1)
    QuimiaSound.Navigation -> InterfaceSoundMix(.62f, 160, 1)
    QuimiaSound.Selection -> InterfaceSoundMix(.52f, 90, 1)
    QuimiaSound.Success -> InterfaceSoundMix(.72f, 650, 2)
    QuimiaSound.Error -> InterfaceSoundMix(.68f, 900, 2)
}
