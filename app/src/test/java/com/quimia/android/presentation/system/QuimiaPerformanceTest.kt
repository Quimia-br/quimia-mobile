package com.quimia.android.presentation.system

import motion.QuimiaRenderingBudget
import org.junit.Assert.assertEquals
import org.junit.Test

class QuimiaPerformanceTest {
    private val gb = 1024L * 1024 * 1024
    @Test fun constrainedTabletKeepsACompactRenderingBudget() {
        assertEquals(QuimiaRenderingBudget.Compact, renderingBudget(false, 192, 2_891_198_464L))
        assertEquals(QuimiaRenderingBudget.Compact, renderingBudget(true, 256, 4 * gb))
    }
    @Test fun modernPhonesAndLargerHeapsKeepTheOriginalVisuals() {
        assertEquals(QuimiaRenderingBudget.Standard, renderingBudget(false, 256, 8 * gb))
        assertEquals(QuimiaRenderingBudget.Standard, renderingBudget(false, 256, 4 * gb))
        assertEquals(QuimiaRenderingBudget.Standard, renderingBudget(false, 192, 6 * gb))
    }
    @Test fun missingMemoryInformationDoesNotForceADeviceIntoCompactMode() {
        assertEquals(QuimiaRenderingBudget.Standard, renderingBudget(false, 0, 0))
        assertEquals(QuimiaRenderingBudget.Standard, renderingBudget(false, 192, 0))
    }
}
