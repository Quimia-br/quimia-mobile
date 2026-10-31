package com.quimia.android.presentation.system

import feedback.QuimiaSound
import org.junit.Assert.*
import org.junit.Test

class InterfaceSoundGateTest {
    @Test fun firstTapWaitsForItsSampleAndPlaysOnlyOnce() {
        val gate = InterfaceSoundGate()
        assertNull(gate.request(QuimiaSound.Tap, 100))
        assertNull(gate.loaded(QuimiaSound.Error, 110))
        assertEquals(QuimiaSound.Tap, gate.loaded(QuimiaSound.Tap, 120))
        assertNull(gate.loaded(QuimiaSound.Tap, 130))
    }
    @Test fun soundsAreNotReplayedLongAfterTheInteraction() {
        val gate = InterfaceSoundGate()
        gate.request(QuimiaSound.Success, 100)
        assertNull(gate.loaded(QuimiaSound.Success, 500))
        assertEquals(QuimiaSound.Success, gate.request(QuimiaSound.Success, 510))
    }
    @Test fun disablingSoundCancelsTheQueuedCueAndEnablingAllowsNewOnes() {
        val gate = InterfaceSoundGate()
        gate.request(QuimiaSound.Tap, 100)
        gate.enabled = false
        assertNull(gate.loaded(QuimiaSound.Tap, 120))
        assertNull(gate.request(QuimiaSound.Tap, 130))
        gate.enabled = true
        assertEquals(QuimiaSound.Tap, gate.request(QuimiaSound.Tap, 140))
    }
    @Test fun successImmediatelyFollowingAButtonTapIsNotSuppressed() {
        val gate = InterfaceSoundGate()
        gate.loaded(QuimiaSound.Tap, 0); gate.loaded(QuimiaSound.Success, 0)
        assertEquals(QuimiaSound.Tap, gate.request(QuimiaSound.Tap, 100))
        assertNull(gate.request(QuimiaSound.Tap, 120))
        assertEquals(QuimiaSound.Success, gate.request(QuimiaSound.Success, 121))
        assertEquals(QuimiaSound.Tap, gate.request(QuimiaSound.Tap, 200))
    }
    @Test fun aLateTapDoesNotReplaceAnImportantQueuedConfirmation() {
        val gate = InterfaceSoundGate()
        gate.request(QuimiaSound.Success, 100)
        gate.request(QuimiaSound.Tap, 120)
        assertNull(gate.loaded(QuimiaSound.Tap, 130))
        assertEquals(QuimiaSound.Success, gate.loaded(QuimiaSound.Success, 140))
    }

    @Test fun repeatedResultToastsDoNotRepeatTheirSoundImmediately() {
        val gate = InterfaceSoundGate()
        gate.loaded(QuimiaSound.Error, 0)
        assertEquals(QuimiaSound.Error, gate.request(QuimiaSound.Error, 100))
        assertNull(gate.request(QuimiaSound.Error, 300))
        assertEquals(QuimiaSound.Error, gate.request(QuimiaSound.Error, 1000))
    }

    @Test fun navigationHasItsOwnCueAndResultsAreStillImmediate() {
        val gate = InterfaceSoundGate()
        gate.loaded(QuimiaSound.Navigation, 0)
        gate.loaded(QuimiaSound.Success, 0)
        assertEquals(QuimiaSound.Navigation, gate.request(QuimiaSound.Navigation, 100))
        assertNull(gate.request(QuimiaSound.Navigation, 150))
        assertEquals(QuimiaSound.Success, gate.request(QuimiaSound.Success, 151))
    }
}
