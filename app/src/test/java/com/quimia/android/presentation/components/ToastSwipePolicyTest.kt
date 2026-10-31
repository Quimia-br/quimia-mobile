package com.quimia.android.presentation.components

import org.junit.Assert.*
import org.junit.Test
import toast.shouldDismissToast

class ToastSwipePolicyTest {
    @Test fun deliberatePullsWorkToTheLeftAndRight() {
        assertTrue(shouldDismissToast(130f, 360f, 0f, 1f))
        assertTrue(shouldDismissToast(-130f, 360f, 0f, 1f))
        assertFalse(shouldDismissToast(60f, 360f, 0f, 1f))
    }
    @Test fun aShortFastFlingDismissesButTinyMovementsAndReversalsDoNot() {
        assertTrue(shouldDismissToast(30f, 360f, 1_500f, 1f))
        assertTrue(shouldDismissToast(-30f, 360f, -1_500f, 1f))
        assertFalse(shouldDismissToast(10f, 360f, 2_000f, 1f))
        assertFalse(shouldDismissToast(30f, 360f, -1_500f, 1f))
    }
    @Test fun thresholdsScaleWithDensityAndRemainReachableOnTablets() {
        assertFalse(shouldDismissToast(150f, 720f, 0f, 2f))
        assertTrue(shouldDismissToast(230f, 720f, 0f, 2f))
        assertTrue(shouldDismissToast(330f, 1_680f, 0f, 2f))
        assertFalse(shouldDismissToast(300f, 1_680f, 0f, 2f))
    }
    @Test fun cardsWithoutMeasuredBoundsCannotBeDismissed() {
        assertFalse(shouldDismissToast(130f, 0f, 1_500f, 1f))
        assertFalse(shouldDismissToast(130f, 360f, 1_500f, 0f))
    }
}
