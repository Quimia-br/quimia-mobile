package com.quimia.android.presentation.components

/** Keep an already-visible loading video until both its first playback and the request have finished. */
internal data class LoadingPlaybackState(val showing: Boolean = false, val firstCycleFinished: Boolean = false, val appearance: Int = 0) {
    fun update(requested: Boolean, animated: Boolean): LoadingPlaybackState = when {
        !animated -> copy(showing = requested, firstCycleFinished = true)
        !showing && requested -> copy(showing = true, firstCycleFinished = false, appearance = appearance + 1)
        showing && firstCycleFinished && !requested -> copy(showing = false)
        else -> this
    }

    fun cycleFinished(completedAppearance: Int = appearance): LoadingPlaybackState =
        if (completedAppearance == appearance) copy(firstCycleFinished = true) else this
}
