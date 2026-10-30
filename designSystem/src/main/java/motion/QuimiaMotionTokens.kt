package motion

import androidx.compose.animation.core.*

/** Shared motion vocabulary; values are deliberately short to keep navigation responsive. */
object QuimiaMotion {
    const val SkeletonPeriod = 850
    const val SkeletonMin = .10f
    const val SkeletonMax = .26f
    const val ImageFade = 180
    const val Quick = 90
    const val Standard = 180
    const val Entrance = 220
    const val PressScale = .98f
    const val EmphasisScale = .96f
    val Easing = FastOutSlowInEasing
    fun <T> tweenSpec(enabled: Boolean, duration: Int = Standard, delay: Int = 0): TweenSpec<T> =
        tween(if (enabled) duration else 0, if (enabled) delay else 0, Easing)
}
