package com.quimia.android.presentation.components

import org.junit.Assert.*
import org.junit.Test

class LoadingPlaybackStateTest {
    @Test fun roomsCanRevealTheirContentAsSoonAsTheRequestFinishes() {
        val started = LoadingPlaybackState().update(true, true, requireFullCycle = false)
        assertTrue(started.showing)
        assertFalse(started.update(false, true, requireFullCycle = false).showing)
    }
    @Test fun aFastResponseCannotCutTheFirstVideoShort() {
        val started = LoadingPlaybackState().update(requested = true, animated = true)
        assertTrue(started.update(requested = false, animated = true).showing)
        assertFalse(started.cycleFinished().update(requested = false, animated = true).showing)
    }
    @Test fun aSlowRequestKeepsTheLoopAfterTheFirstCycle() {
        val playing = LoadingPlaybackState().update(true, true).cycleFinished()
        assertTrue(playing.update(true, true).showing)
        assertFalse(playing.update(false, true).showing)
    }
    @Test fun aNewAppearanceNeedsItsOwnFullCycle() {
        val dismissed = LoadingPlaybackState().update(true, true).cycleFinished().update(false, true)
        assertFalse(dismissed.update(true, true).firstCycleFinished)
        assertTrue(dismissed.update(true, true).update(false, true).showing)
        assertNotEquals(dismissed.appearance, dismissed.update(true, true).appearance)
    }
    @Test fun aLateCallbackFromThePreviousVideoCannotCompleteTheNewAppearance() {
        val old = LoadingPlaybackState().update(true, true)
        val next = old.cycleFinished().update(false, true).update(true, true)
        assertFalse(next.cycleFinished(old.appearance).firstCycleFinished)
        assertTrue(next.cycleFinished(next.appearance).firstCycleFinished)
    }
    @Test fun reducedMotionAndPlaybackFailureNeverTrapTheUser() {
        val playing = LoadingPlaybackState().update(true, true)
        assertFalse(playing.update(false, false).showing)
        assertFalse(playing.cycleFinished().update(false, true).showing)
    }
    @Test fun aMenuTabThatNeverRequestedLoadingDoesNotStartAVideo() {
        assertFalse(LoadingPlaybackState().update(false, true).showing)
    }
}
