package com.quimia.android.presentation.system

import feedback.QuimiaSound
import org.junit.Assert.*
import org.junit.Test

class InterfaceSoundVariantsTest {
    @Test fun repeatedInteractionsDoNotRepeatTheSameTexture() {
        val picker = InterfaceSoundVariants { 0 }
        val samples = List(20) { picker.next(QuimiaSound.Tap, listOf(1, 2, 3)) }
        assertTrue(samples.zipWithNext().all { (a, b) -> a != b })
    }

    @Test fun onlyLoadedSamplesAreEligibleAndOneSampleStillPlays() {
        val picker = InterfaceSoundVariants { 0 }
        assertNull(picker.next(QuimiaSound.Success, emptyList()))
        repeat(3) { assertEquals(7, picker.next(QuimiaSound.Success, listOf(7))) }
        assertEquals(9, picker.next(QuimiaSound.Success, listOf(7, 9)))
    }

    @Test fun eachCueKeepsItsOwnPreviousSample() {
        val picker = InterfaceSoundVariants { 0 }
        assertEquals(1, picker.next(QuimiaSound.Tap, listOf(1, 2)))
        assertEquals(1, picker.next(QuimiaSound.Selection, listOf(1, 2)))
        assertEquals(2, picker.next(QuimiaSound.Tap, listOf(1, 2)))
    }
}
