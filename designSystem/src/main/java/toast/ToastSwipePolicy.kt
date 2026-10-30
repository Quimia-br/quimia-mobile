package toast

import kotlin.math.abs

/** A deliberate pull or a quick fling dismisses in either direction; taps and small drifts do not. */
fun shouldDismissToast(distancePx: Float, widthPx: Float, velocityPx: Float, density: Float): Boolean {
    if (widthPx <= 0f || density <= 0f || !distancePx.isFinite() || !velocityPx.isFinite()) return false
    val distance = abs(distancePx)
    val threshold = (widthPx * .30f).coerceIn(64f * density, 160f * density)
    val sameDirection = distancePx * velocityPx > 0
    return distance >= threshold || (distance >= 24f * density && sameDirection && abs(velocityPx) >= 1_100f * density)
}
